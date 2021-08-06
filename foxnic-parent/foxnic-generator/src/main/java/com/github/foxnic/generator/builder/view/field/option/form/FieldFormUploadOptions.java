package com.github.foxnic.generator.builder.view.field.option.form;

import com.github.foxnic.generator.builder.view.field.FieldInfo;
import com.github.foxnic.generator.builder.view.field.option.FieldOptions;
import com.github.foxnic.generator.builder.view.field.option.SubOptions;

public class FieldFormUploadOptions extends SubOptions {

    public FieldFormUploadOptions(FieldInfo field, FieldOptions top) {
         super(field,top);
    }

    /**
     * 文件数量
     * */
    public FieldFormUploadOptions maxFileCount(int count) {
        this.field.uploadField().maxFileCount(count);
        return this;
    }

    /**
     * 允许上传的文件扩展名, 例如: doc, zip, rar
     * */
    public FieldFormUploadOptions acceptExts(String... exts) {
        this.field.uploadField().acceptExts(exts);
        return this;
    }

    /**
     * 允许上传的 mime 类型
     * */
    public FieldFormUploadOptions acceptMime(String... mimes) {
        this.field.uploadField().acceptMime(mimes);
        return this;
    }


    /**
     * 允许上传的 acceptType 类型
     * */
    public FieldFormUploadOptions acceptAllType() {
        this.field.uploadField().acceptAllType();
        return this;
    }

    /**
     * 允许上传的 acceptType 类型
     * */
    public FieldFormUploadOptions acceptImageType() {
        this.field.uploadField().acceptImageType();
        return this;
    }

    /**
     * 允许上传的 acceptType 类型
     * */
    public FieldFormUploadOptions acceptAudioType() {
        this.field.uploadField().acceptAudioType();
        return this;
    }

    /**
     * 允许上传的 acceptType 类型
     * */
    public FieldFormUploadOptions acceptVideoType() {
        this.field.uploadField().acceptVideoType();
        return this;
    }

    /**
     * 配置为单文件上传
     * */
    public FieldFormUploadOptions acceptSingleFile() {
        this.field.uploadField().acceptSingleFile();
        return this;
    }

    /**
     * 配置为单图片上传
     * */
    public FieldFormUploadOptions acceptSingleImage() {
        this.field.uploadField().acceptSingleImage();
        return this;
    }



    /**
     * 是否显示上传的文件名
     * */
    public FieldFormUploadOptions displayFileName(boolean displayFileName) {
       this.field.uploadField().displayFileName(displayFileName);
        return this;
    }

    /**
     * 文件浏览按钮的显示文本
     * */
    public FieldFormUploadOptions buttonLabel(String label) {
        this.field.uploadField().buttonLabel(label);
        return this;
    }


}
