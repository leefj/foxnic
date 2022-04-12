package com.github.foxnic.commons.io;

import java.io.File;

public class FileNavigator {

    public static interface OnFile {
        void handle(File file,boolean isFile,String ext);
    }

    private File root;
    public FileNavigator(File root) {
        this.root=root;
    }

    public FileNavigator(String rootPath) {
        this.root=new File(rootPath);
    }

    public void scan(OnFile on) {
        scanInternal(this.root,on);
    }


    private void scanInternal(File dir,OnFile on) {
        File[] fs=dir.listFiles();
        for (File f : fs) {
            if(f.isDirectory()) {
                on.handle(f,false,null);
                scanInternal(f,on);
            } else if(f.isFile()) {
                on.handle(f,true,FileUtil.getExtName(f.getName().toLowerCase()));
            }
        }
    }

}
