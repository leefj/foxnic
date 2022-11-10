package com.github.foxnic.generator.task;

import com.github.foxnic.commons.busi.id.IDGenerator;
import com.github.foxnic.commons.io.FileUtil;

import java.io.File;

public class ExecAfterInstall {

    public static void main(String[] args) {
        FileUtil.writeText(new File("D:\\foxnic\\install.txt"), IDGenerator.getNanoId(6));
    }

}
