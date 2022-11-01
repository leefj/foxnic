package com.github.foxnic.commons.busi;

import com.github.foxnic.commons.encrypt.MD5Util;
import com.github.foxnic.commons.io.FileNavigator;
import com.github.foxnic.commons.io.FileUtil;
import com.github.foxnic.commons.lang.StringUtil;
import com.github.foxnic.commons.project.maven.MavenProject;
import com.github.foxnic.commons.project.maven.POMFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class VersionCoder {

    public static void update() {

        MavenProject project=new MavenProject(VersionCoder.class);
        File baseDir=project.getProjectDir().getParentFile();
        POMFile basePomFile= new POMFile(FileUtil.resolveByPath(baseDir,"pom.xml"));
        String foxnicVersion=basePomFile.getVersion();







        System.exit(0);

    }

    private static String getProjectSign(File baseDir) {
        List<String> md5List=new ArrayList<>();
        FileNavigator fileNavigator=new FileNavigator(baseDir);
        fileNavigator.scan((file,isFile,ext)->{
            if(!isFile) return;
            if(ext==null) return;
            if(!ext.equals("java") && !ext.equals("tql")) return;
            try {
                md5List.add(MD5Util.encrypt32(new FileInputStream(file)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return MD5Util.encrypt32(StringUtil.join(md5List));
    }

}
