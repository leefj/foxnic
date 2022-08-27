package com.github.foxnic.springboot.starter;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import com.github.foxnic.commons.collection.TypedHashMap;
import com.github.foxnic.commons.lang.ArrayUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.property.YMLProperties;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * SpringBoot 参数工具
 *
 * @author lifangjie
 */
public class BootArgs {

    private static TypedHashMap<String,String> BOOT_ARGS=new TypedHashMap<>();
    private static TypedHashMap<String,Boolean> IS_OPT_ARGS=new TypedHashMap<>();


    public static String[] toRawArgs()
    {
        String[] args=new String[BOOT_ARGS.size()];
        int i=0;
        for (Entry<String,String> e: BOOT_ARGS.entrySet()) {
            boolean isOpt=IS_OPT_ARGS.get(e.getKey())==null?false:IS_OPT_ARGS.get(e.getKey());
            args[i]=(isOpt?"--":"") + e.getKey()+(e.getValue()==null?"":("="+e.getValue()));
            i++;
        }
        return args;
    }

    static void setArg(String name,String value,boolean isOpt)
    {
        BOOT_ARGS.put(name,value);
        IS_OPT_ARGS.put(name,isOpt);
    }

    /**
     *  获得启动参数清单
     * */
    public static TypedHashMap<String,String>  getBootArgs()
    {
        return BOOT_ARGS;
    }


    private static Boolean IS_IN_IDE = null ;

    /**
     * 获得参数值
     * */
    public static String getArg(String name) {
        return BOOT_ARGS.get(name);
    }

    /**
     * 判断参数是否出现
     * */
    public static boolean isExist(String name) {
        return BOOT_ARGS.containsKey(name.toLowerCase());
    }


    /**
     * 是否从IDE启动
     * */
    public static boolean isBootInIDE() {
        if(IS_IN_IDE!=null) return IS_IN_IDE;
        String srcLocation=BootArgs.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        IS_IN_IDE=srcLocation.indexOf(".jar!")==-1;
        // Logger.info("source location : " + srcLocation+" , boot in ide : "+IS_IN_IDE);
        return IS_IN_IDE;
    }

    /**
     *  获得应用启动目录
     * */
    public static  File getWorkDir()
    {
        if(isBootInIDE())
        {
            File dir = null;
            try {
                dir = new File(ResourceUtils.getURL("classpath:").getPath());
                dir=dir.getParentFile().getParentFile();
                dir=new File(StringUtil.joinPath(dir.getAbsolutePath(),"work"));
            } catch (FileNotFoundException e) {
                Logger.exception(e);
            }
            return dir;
        }
        else {
            return new File(".");
        }
    }


    private static boolean INITED=false;

    /**
     *  直接初始化启动参数，读取配置文件
     * */
    public static void initOnBoot(String[] args)
    {
        if(INITED) return;

        if(args==null) return;
        for (String kv:args) {
            String[]  t=kv.split("=");
            if(t.length==0) continue;
            boolean isOpt=t[0].trim().startsWith("--");
            String name=StringUtil.removeFirst(t[0].trim(),"-");
            if(t.length==1)
            {
                BOOT_ARGS.put(name,null);
            }
            else if(t.length>=1)
            {
                BOOT_ARGS.put(name,t[1]);
            }
            IS_OPT_ARGS.put(name,isOpt);
        }

        INITED = true;

    }




}
