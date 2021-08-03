package com.github.foxnic.generator.builder.view.option;

import com.github.foxnic.generator.builder.view.config.SearchAreaConfig;

public class SearchAreaOptions {

    private SearchAreaConfig config;

    public  SearchAreaOptions(SearchAreaConfig config) {
        this.config=config;
    }


    public void inputLayout(Object[]... inputRows) {
        this.config.setInputLayout(inputRows);
    }
}
