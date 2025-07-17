package com.allan.androidlearning.common;

public class StringImportBean {
    public String key;
    public String translatable;
    public String en;
    public String enAdj;
    public String de;
    public String deAdj;
    public String fr;
    public String frAdj;
    public String valueMarker;

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "TranslationItem{" +
                "key='" + key + '\'' +
                ", translatable='" + translatable + '\'' +
                ", english='" + en + '\'' +
                ", enAdj='" + enAdj + '\'' +
                ", de='" + de + '\'' +
                ", deAdj='" + deAdj + '\'' +
                ", fr='" + fr + '\'' +
                ", frAdj='" + frAdj + '\'' +
               // ", valueMarker='" + valueMarker + '\'' +
                '}';
    }
}