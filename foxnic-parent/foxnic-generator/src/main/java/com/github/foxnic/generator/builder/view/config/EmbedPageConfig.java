package com.github.foxnic.generator.builder.view.config;

public class EmbedPageConfig {

   private String title=null;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    private String url=null;

   public EmbedPageConfig(String title,String url) {
        this.title=title;
        this.url=url;
   }

}
