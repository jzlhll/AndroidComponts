package com.allan.androidlearning.common;

public class StringExportBean {
    public String en;
    public String de;
    public String fr;
    public String moduleName;
    public boolean translatable = true; // 默认为true

    public String getTrans(String lang) {
        if ("de".equals(lang)) {
            return de;
        }
        if ("fr".equals(lang)) {
            return fr;
        }
        return en;
    }

    @Override
    public String toString() {
        return  "en: " + en + "\n" +
                "de: " + de + "\n" +
                "fr: " + fr + "\n" +
                "translatable: " + translatable + "\n";
    }
}