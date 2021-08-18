package com.github.foxnic.generator.util;

import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;

import java.io.File;
import java.util.*;

public class JSFunctions {

    public static class JSFunction {
        private String id;
        private String[] source;
        private String name;
        private Set<String> paramNames=new HashSet<>();

        public String getId() {
            return id;
        }

        public String getSource() {
            return StringUtil.join(this.source,"\n");
        }

        public String getName() {
            return name;
        }

        public JSFunction(String id,String[] source) {
            this.id=id;
            this.source=source;
            String sourceString=StringUtil.join(this.source,"\n");
            int a=sourceString.indexOf("function");
            int b=sourceString.indexOf('(',a);
            int c=sourceString.indexOf(')',b);
            this.name=sourceString.substring(a+8,b).trim();
            String[] ps=sourceString.substring(b+1,c).split(",");
            for (int i = 0; i < ps.length; i++) {
                paramNames.add(ps[i].trim());
            }
        }

        public boolean hasParam(String pname) {
            return paramNames.contains(pname);
        }

        public void prefixTab(int i) {
            String tabs="";
            for (int j = 0; j <i ; j++) {
                tabs+="\t"+tabs;
            }
            for (int j = 0; j < this.source.length; j++) {
                if(j==0) continue;
                this.source[j]=tabs+this.source[j];
            }
        }
    }

    private Map<String,JSFunction> functions=new HashMap<>();

    public Map<String, JSFunction> getFunctions() {
        return functions;
    }

    public JSFunctions(Class type,String jsFile) {
        MavenProject mp=new MavenProject(type);
        File dir=mp.getSourceFile(type).getParentFile();
        File file= FileUtil.resolveByPath(dir,jsFile);
        if(!file.exists()){
            throw new RuntimeException("文件 "+file.getAbsolutePath()+" 不存在");
        }
        String code=FileUtil.readText(file);
        String[] lns=code.split("\n");
        List<String> segment=new ArrayList<>();
        String id=null,name=null;
        for (String ln : lns) {
           String lnTrimed=ln.trim();

           if(lnTrimed.startsWith("//[") && lnTrimed.endsWith("]")) {
               if(id!=null && segment.size()>0) {
                   JSFunction func=new JSFunction(id,segment.toArray(new String[0]));
                   functions.put(id,func);
               }
               segment.clear();
               id= StringUtil.removeFirst(lnTrimed,"//[");
               id= StringUtil.removeLast(id,"]");
               name=null;
               continue;
           }
           segment.add(ln);
        }
        if(id!=null && segment.size()>0) {
            JSFunction func=new JSFunction(id,segment.toArray(new String[0]));
            functions.put(id,func);
        }

    }




}
