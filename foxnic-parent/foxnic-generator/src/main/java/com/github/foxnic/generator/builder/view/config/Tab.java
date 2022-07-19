package com.github.foxnic.generator.builder.view.config;

public class Tab {

    public Tab(String title,String iframeLoadJsFunctionName) {
        this(title,iframeLoadJsFunctionName,null);
    }
    public Tab(String title,String iframeLoadJsFunctionName,String iframeId) {
        this.title=title;
        this.iframeLoadJsFunctionName=iframeLoadJsFunctionName;
        this.iframeId=iframeId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private  String iframeId=null;
    private int index;
    private String title;
    private String iframeLoadJsFunctionName;

    public String getTitle() {
        return title;
    }

    public String getIframeId() {
        return iframeId;
    }

    public String getIframeLoadJsFunctionName() {
        return iframeLoadJsFunctionName;
    }


}
