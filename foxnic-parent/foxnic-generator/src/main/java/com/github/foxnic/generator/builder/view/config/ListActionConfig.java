package com.github.foxnic.generator.builder.view.config;

public class ListActionConfig {

    public static enum ActionType {
        open_window;
    }


    private ActionType actionType;
    private String uri;
    private String label;
    private String  id;

    public String getActionName() {
        return actionType.name();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    /**
     * 按钮或菜单的文本
     * */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
        this.windowTitleKey=null;
    }

    private String windowTitle=null;

    private String windowTitlePrefix=null;
    private String windowTitleSuffix=null;
    private String windowTitleKey=null;

    public String getWindowTitle() {
        return windowTitle;
    }

    public String getWindowTitlePrefix() {
        return windowTitlePrefix;
    }

    public String getWindowTitleSuffix() {
        return windowTitleSuffix;
    }

    public String getWindowTitleKey() {
        return windowTitleKey;
    }

    /**
     * 设置窗口标题
     * */
    public ListActionConfig setWindowTitleKey(String prefix,String key,String suffix) {
        this.windowTitlePrefix=prefix;
        this.windowTitleSuffix=suffix;
        this.windowTitleKey=key;
        this.windowTitle=null;
        return this;
    }

    private int windowWidth=500;
    private int windowHeight=500;

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public  ListActionConfig setWindowSize(int width,int height) {
        this.windowWidth=width;
        this.windowHeight=height;
        return this;
    }



}
