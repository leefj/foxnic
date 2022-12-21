package com.github.foxnic.dao.sql.loader;

import com.github.foxnic.commons.code.CodeBuilder;
import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.reflect.ReflectUtil;
import com.github.foxnic.dao.GlobalSettings;
import com.github.foxnic.dao.spec.DAO;
import com.github.foxnic.sql.exception.SQLValidateException;
import com.github.foxnic.sql.meta.DBType;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * 每个DAO对应一个TQLMap
 *
 * @author fangjieli
 */
class TQLCache {

    private static final String DAO_PREFIX = GlobalSettings.PACKAGE + ".";

    private static final String SPRING_CGLIB = "$$EnhancerBySpringCGLIB$$";

    private static final String ORG_SPRINGFRAMEWORK = "org.springframework.";
    private static final String DEFAULT = "default";

    private static TQLCache instance = new TQLCache();

    public static TQLCache instance() {
        return instance;
    }

    private ArrayList<String> packages = new ArrayList<String>();

    private SQLMonitor monitor = null;

    /**
     * 添加需要扫描的包
     */
    public void addPackages(String... packages) {
        int size = this.packages.size();
        for (String pkg : packages) {
            if (!this.packages.contains(pkg)) {
                this.packages.add(pkg);
            }
        }
        //如果扫描范围有变动，则清除，在下次get时重新扫描
        if (size != this.packages.size()) {
            sqlMap.clear();
            hashs.clear();
            isScanCompleted = false;
        }
    }

    private TQLScanner scanner = new TQLScanner();

    /**
     * 数据格式   {dbtype:{map}}
     */
    private HashMap<String, HashMap<String, TQL>> sqlMap = new HashMap<String, HashMap<String, TQL>>();

    public TQL get(String id, DBType dbType) {
        String pkg = getSourcePackage();

        if (!isScanCompleted) {
            try {
                scan();
            } catch (Exception e) {
                Logger.error("TQL扫描失败:", e);
            }
        }

        id = id.trim();

        if ("#null".equals(id)) {
            return null;
        }

        id = id.trim();
        while (id.startsWith("#")) {
            id = id.substring(1);
        }
        String sqlId = id;
        if (!StringUtil.isBlank(pkg)) {
            id = pkg + "." + sqlId;
        }

        HashMap<String, TQL> sqlmap = sqlMap.get(dbType.name().toLowerCase());

        TQL tql = getTQL(sqlmap,id,sqlId,pkg);
        if(tql == null) {
            sqlmap = sqlMap.get(DEFAULT);
            tql = getTQL(sqlmap, id, sqlId, pkg);
        }

        if (tql == null) {
            throw new SQLValidateException("未定义的 SQL ID : " + id);
        }

        tql.increaseCalls();

        return tql;
    }

    private TQL getTQL(HashMap<String, TQL> sqlmap, String id, String sqlId, String pkg) {
        TQL tql = null;
        if (sqlmap == null) return null;

        tql = sqlmap.get(id);
        while (tql == null) {
            int dotIndex = pkg.lastIndexOf(".");
            if (dotIndex == -1) break;
            pkg = pkg.substring(0, dotIndex);
            id = pkg + "." + sqlId;
            tql = sqlmap.get(id);
            if (tql != null) {
                break;
            }
        }
        return tql;
    }


    private String getSourcePackage() {
        StackTraceElement[] es = Thread.currentThread().getStackTrace();
        StackTraceElement el = null;
        String clsName = null;
        String pkg = null;
        for (int i = 1; i < es.length; i++) {
            el = es[i];
            clsName = el.getClassName();
            if (clsName.startsWith(DAO_PREFIX) || clsName.startsWith(ORG_SPRINGFRAMEWORK)
                    || clsName.contains(SPRING_CGLIB)) {
                continue;
            }
            Class cls = ReflectUtil.forName(clsName, true);
            if (!DAO.class.isAssignableFrom(cls)) {
                pkg = cls.getPackage().getName();
                break;
            }
        }
        return pkg;
    }

    private Map<String, Object> source = null;

    private boolean isScanCompleted = false;

    /**
     * 是否扫描完毕
     */
    public boolean isScanCompleted() {
        return isScanCompleted;
    }


    private boolean isScanning = false;

    /**
     * 扫描
     */
    private synchronized void scan() throws Exception {

        if (isScanCompleted || isScanning) {
            return;
        }
        isScanning = true;
        //扫描
//		if(!packages.contains("com.github.foxnic")) {
//			packages.add("com.github.foxnic");
//		}
        source = new HashMap<String, Object>(32);
        for (String pkg : packages) {
            try {
                Map<String, Object> part = scanner.findCandidateComponents(pkg);
                source.putAll(part);
            } catch (IOException e) {
                Logger.error("find source error", e);
            }
        }
        //读取
        read();

        if (monitor == null) {
            new Thread() {
                @Override
                public void run() {
                    monitor = new SQLMonitor(TQLCache.this);
                    for (String key : source.keySet()) {
                        if (source.get(key) instanceof File) {
                            monitor.addWatch(key, ((File) source.get(key)).getParentFile());
                        }
                    }
                    monitor.start();
                }

                ;
            }.start();
        }

        isScanCompleted = true;
        isScanning = false;
        Logger.info("TQL Scan Complete");
        CodeBuilder info = new CodeBuilder();
        for (Entry<String, HashMap<String, TQL>> e : sqlMap.entrySet()) {
            info.ln(e.getKey() + " sql count : " + e.getValue().size());
            if (printDetail) {
                for (Entry<String, TQL> t : e.getValue().entrySet()) {
                    info.ln(1, t.getKey() + " : ");
                    TQL tql = t.getValue();
                    info.ln(2, "id = " + tql.getId());
                    info.ln(2, "location = " + tql.getLocation());
                    info.ln(2, "statement = " + tql.getSql());
                }
            }
        }
        Logger.info((printDetail ? "TQL Detail : \n\n" : "") + info.toString());

    }


    /**
     * 读取
     */
    private void read() throws Exception {
        Object obj;
        File file;
        Object[] jars;
        JarEntry entry;
        JarFile jar;
        String content;
        for (String key : source.keySet()) {
            obj = source.get(key);
            content = null;
            if (obj instanceof File) {
                file = (File) obj;
                try {
//					content=FileUtil.readString(file);
                    content = FileUtils.readFileToString(file, "UTF-8");
                } catch (IOException e) {
                    Logger.error(file.getAbsolutePath() + "读取失败", e);
                }
            } else if (obj instanceof Object[]) {
                jars = (Object[]) obj;
                jar = (JarFile) jars[0];
                entry = (JarEntry) jars[1];
                StringBuilder buf = new StringBuilder();
                try {
                    InputStream is = jar.getInputStream(entry);
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buf.append(line + "\n");
                    }
                    reader.close();
                    content = buf.toString();
                } catch (Exception e) {
                    Logger.error("jar:" + jar.getName() + "/" + entry.getName() + "读取失败", e);
                }
            }

            if (!StringUtil.isBlank(content)) {
                parse(key, obj, content, false);
            }

        }
    }

    private HashMap<String, String> hashs = new HashMap<String, String>();


    /**
     * 转换
     */
    void parse(String pkg, Object file, String content, boolean override) throws Exception {

        String prefix = "";

        //做一个内容判断
        if (file instanceof File) {
            String path = ((File) file).getAbsolutePath();
            String newHash = MD5Util.encrypt32(content);
            String oldHash = hashs.get(path);
            if (newHash != null && oldHash != null && oldHash.equals(newHash)) {
                return;
            }
            hashs.put(path, newHash);
            Logger.info("TQL \t" + path);
        }


        String gid = null, sql = null;
        String[] lns = content.split("\n");
        StringBuilder buf = null;
        int puts = 0;
        int i = 0, lineIndex = 0;
        StringBuilder builder = new StringBuilder();
        for (String ln : lns) {
            ln = ln.trim();
            if (ln.startsWith("//") || ln.startsWith("--")) {
                continue; //跳过注释行
            }
            if (ln.startsWith("[") && ln.endsWith("]")) {
                lineIndex = i;
                if (gid != null && gid.length() > 0) {
                    sql = buf.toString().trim();
                    if (!StringUtil.isBlank(prefix)) {
//						gid=prefix+"."+gid;
                        builder.setLength(0);
                        gid = builder.append(prefix).append(".").append(gid).toString();
                    }
                    putSQL(pkg, lineIndex, gid, sql, override);
                    puts++;
                }
                gid = ln.substring(1, ln.length() - 1).trim();

                if (gid.startsWith("package") || gid.startsWith("prefix")) {
                    if (!StringUtil.isBlank(prefix)) {
                        throw new Exception(pkg + "下的tql文件package重复定义");
                    }
                    int z = gid.indexOf(":");
                    if (z > 0) {
                        prefix = gid.substring(z + 1, gid.length()).trim();
//						validateName(prefix,false);
                        if (StringUtil.isBlank(prefix)) {
                            throw new Exception(pkg + "下的tql文件package值错误");
                        } else {
                            if (puts > 0) {
                                throw new Exception(pkg + "下的tql文件package未定义在文件头部");
                            }
                            gid = null;
                            continue;
                        }
                    }
                }

                buf = new StringBuilder();
            } else {
                if (buf != null) {
                    buf.append(ln + "\n");
                }
            }
            i++;
        }

        if (buf != null) {
            sql = buf.toString().trim();
        }

        if (!StringUtil.isBlank(prefix)) {
            gid = prefix + "." + gid;
        }
        putSQL(pkg, lineIndex, gid, sql, override);
        puts++;
    }


    private void putSQL(String location, int ln, String gid, String sql, boolean override) {
        location = location.replace('/', '.');
        int dotIndex = location.lastIndexOf('.');
        dotIndex = location.lastIndexOf('.', dotIndex - 1);
        String pkg = null;
        if (dotIndex != -1) {
            pkg = location.substring(0, dotIndex);
        }
        String id = null, dbTypeStr = null;
        String[] tmp = null;
        DBType dbType = null;

        String errorLoc = ",位于" + location + "的第" + ln + "行," + gid;

        if (StringUtil.isBlank(sql)) {
            Logger.info(errorLoc + ",语句为空");
            return;
        }

        tmp = gid.split(":");
        if (tmp.length == 1) {
            dbTypeStr = DEFAULT;
            id = gid;
        } else if (tmp.length == 2) {
            dbTypeStr = tmp[1];
            id = tmp[0];
            try {
                dbType = DBType.valueOf(dbTypeStr.toUpperCase());
            } catch (Exception e) {
                throw new SQLValidateException("dbType 无法识别," + e.getMessage() + errorLoc);
            }
            if (dbType == null) {
                throw new SQLValidateException("dbType 无法识别" + errorLoc);
            }
            dbTypeStr = dbType.name().toLowerCase();
        } else {
            throw new SQLValidateException("SQL ID 命名错误" + errorLoc);
        }

        if (pkg != null) {
            id = pkg + "." + id;
        }

        HashMap<String, TQL> map = sqlMap.get(dbTypeStr.toLowerCase());
        if (map == null) {
            map = new HashMap<String, TQL>(32);
            sqlMap.put(dbTypeStr, map);
        }

        if (map.containsKey(id) && !override) {
            throw new SQLValidateException("重复定义 SQL ID : " + id + errorLoc);
        } else {
            if (override && map.get(id) != null && !sql.equals(map.get(id).getSql())) {
                System.err.println("\t" + gid + "\t\t已修改");
            }

            map.put(id, new TQL(gid, sql, location));
        }

    }


    private boolean printDetail = false;

    public void setPrintDetail(boolean b) {
        this.printDetail = b;
    }
}
