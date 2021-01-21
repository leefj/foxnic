package com.github.foxnic.dao.sql.loader;


import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.project.maven.MavenProject;

/**
 * @author fangjieli
 * */
class TQLScanner  {
 
    public static final String TQL_SUFFIX = ".tql";

    public Map<String, Object> findCandidateComponents(String packageName) throws IOException {
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.length()-1);
        }
        Map<String, Object> classMap = new HashMap<>(32);
        String path = packageName.replace(".", "/");
        List<URL> urls = findAllClassPathResources(path);
        for (URL url : urls) {
            Logger.debug(url.getPath());
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                String file = URLDecoder.decode(url.getFile(), "UTF-8");
                File dir = new File(file);
                if(dir.isDirectory()){
                    parseClassFile(dir, packageName, classMap);
                }else {
                    throw new IllegalArgumentException("file must be directory");
                }
            } else if ("jar".equals(protocol)) {
                parseJarFile(url, classMap);
            }
        }
        
        return classMap;
    }

    protected void parseClassFile(File dir, String packageName, Map<String, Object> classMap) {
 
    	if(dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File file : files) {
                parseClassFile(file, packageName, classMap);
            }
        } else if(dir.getName().endsWith(TQL_SUFFIX)) {
    	    MavenProject mp=new MavenProject(dir);
            String name = dir.getAbsolutePath();
            name=name.substring(mp.getMainSourceDir().getAbsolutePath().length()+1);
            //name = name.substring(name.indexOf("classes")+8).replace("\\", ".");
            name = name.replace("\\", ".");
            addToClassMap(name, dir,classMap);
        }
    }

    protected void parseJarFile(URL url, Map<String, Object> classMap) throws IOException {
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String name = entry.getName();
            if(name.endsWith(TQL_SUFFIX)){
                addToClassMap(name.replace("/", "."), new Object[]{jar,entry},classMap);
            }
        }
    }

    private boolean addToClassMap(String name,Object file, Map<String, Object> classMap){
    	classMap.put(name, file);
        return true;
    }

    protected List<URL> findAllClassPathResources(String path) throws IOException {
        if(path.startsWith("/")){
            path = path.substring(1);
        }
        Enumeration<URL> urls2 = this.getClass().getClassLoader().getResources(path);
        ArrayList<URL> urls = new ArrayList<>();
        while (urls2!=null && urls2.hasMoreElements()) {
            URL url = urls2.nextElement();
            String f=url.getFile();
            if(OSType.isWindows()) {
            	f=StringUtil.removeFirst(f,"/");
            }
            MavenProject mp=new MavenProject(f);
            f=mp.getSourcePath(f);
            urls.add((new File(f)).toURI().toURL());
        }
        return urls;
    }

}
