package com.allan.androidlearning;

import javax.inject.Inject;

public class AnotherData {
    @Inject public AnotherData() {}

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private String data;
}