package com.github.foxnic.dao.dataperm.model;

public class DataPermSubject {

    public static interface SubjectGetter {
        Object get();
    }

    private String code;
    private String name;
    private SubjectGetter subjectGetter;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubjectGetter getSubjectGetter() {
        return subjectGetter;
    }

    public void setSubjectGetter(SubjectGetter subjectGetter) {
        this.subjectGetter = subjectGetter;
    }




}
