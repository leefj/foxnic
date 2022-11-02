package com.github.foxnic.commons.language;

import com.github.foxnic.commons.collection.CollectorUtil;
import com.github.foxnic.commons.lang.DataParser;
import com.github.foxnic.commons.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AcceptLanguages {

    private static class LangUnit {

        GlobalLanguage language = null;
        Double rate = 0.0;

        private LangUnit(GlobalLanguage language,Double rate) {
            this.language=language;
            this.rate=rate;
        }

        public GlobalLanguage getLanguage() {
            return language;
        }

        public Double getRate() {
            return rate;
        }


    }
    private List<LangUnit> languages=null;
    private String acceptLanguage;


    public AcceptLanguages(String acceptLanguage) {
        this.acceptLanguage=acceptLanguage;
        languages=new ArrayList<>();
        if(StringUtil.isBlank(acceptLanguage)) return;
        String[] langs=acceptLanguage.split(",");
        for (String lang : langs) {
            if(StringUtil.isBlank(lang)) continue;
            String[] parts=lang.split(";");
            GlobalLanguage language=GlobalLanguage.parseByCode(parts[0]);
            if(language==null) continue;
            if(parts.length>=2) {
                Double rate=0.0;
                if(!StringUtil.isBlank(parts[1])) {
                    String[] qs = parts[1].split("=");
                    if(qs.length>=2) {
                        rate=DataParser.parseDouble(qs[1]);
                    }
                }
                languages.add(new LangUnit(language, rate));
            }
            if(parts.length==1) {
                languages.add(new LangUnit(language, 0.0));
            }
        }
        CollectorUtil.sort(languages,LangUnit::getRate,false,true);
    }


    /**
     * 获得主要语言
     * */
    public GlobalLanguage getPrimaryLanguage() {
        if(languages.isEmpty()) return null;
        return languages.get(0).getLanguage();
     }

     /**
      * 获得按优先级排序的语言清单
      * */
     public List<GlobalLanguage> getPriorityLanguages() {
        return CollectorUtil.collectList(this.languages,LangUnit::getLanguage);
     }




}
