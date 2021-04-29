package com.github.foxnic.dao.sql.loader;


import com.github.foxnic.commons.environment.OSType;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.log.Logger;
import com.github.foxnic.commons.project.maven.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
//        System.err.println("#########0 -> \\t\\t"+urls.size());
        for (URL url : urls) {
//            Logger.info(url.getPath());
//            System.err.println("#########1 -> \t\t"+url.getPath());
            String protocol = url.getProtocol();
//            System.err.println("#########2 -> \t\t"+protocol);
            if ("file".equals(protocol)) {
                String file = URLDecoder.decode(url.getFile(), "UTF-8");
                File dir = new File(file);
                if(dir.isDirectory()) {
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
            System.err.println("in jar : "+name);
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
        Enumeration<URL> urls1=Thread.currentThread().getContextClassLoader().getResources(path);
        Enumeration<URL> urls2 = this.getClass().getClassLoader().getResources(path);
        ArrayList<URL> all = new ArrayList<>();
        while (urls1!=null && urls1.hasMoreElements()) {
        	URL url = urls1.nextElement();
        	all.add(url);
        }
        while (urls2!=null && urls2.hasMoreElements()) {
        	URL url = urls2.nextElement();
        	all.add(url);
        }
        ArrayList<URL> urls = new ArrayList<>();
        for (URL url : all) {
            String f=url.getFile();
//            System.err.println("$$$$$ cpres : "+f);
            if(OSType.isWindows()) {
            	f=StringUtil.removeFirst(f,"/");
            }
            MavenProject mp=null;
            try {
                mp = new MavenProject(f);
            }catch (Exception e){}
            if(mp!=null) {
                f = mp.getSourcePath(f);
                urls.add((new File(f)).toURI().toURL());
            } else {
                urls.add(url);
            }
        }
        return urls;
    }

}
