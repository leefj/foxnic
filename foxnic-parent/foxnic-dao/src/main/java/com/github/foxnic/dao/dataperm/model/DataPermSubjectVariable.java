package com.github.foxnic.dao.dataperm.model;

public class DataPermSubjectVariable {

    private String subjectCode;
    private DataPermSubject subject;
    private String variable;
    private String property;
    private String name;

    public DataPermSubject getSubject() {
        return subject;
    }

    public void setSubject(DataPermSubject subject) {
        this.subject = subject;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
}
