package com.github.foxnic.generator.builder.view.config;

public class Tab {

    public Tab(String title,String iframeLoadJsFunctionName) {
        this.title=title;
        this.iframeLoadJsFunctionName=iframeLoadJsFunctionName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    private int index;
    private String title;
    private String iframeLoadJsFunctionName;

    public String getTitle() {
        return title;
    }
    public String getIframeLoadJsFunctionName() {
        return iframeLoadJsFunctionName;
    }


}
