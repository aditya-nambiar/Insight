package com.android.insights.models;

/**
 * Created by nambiar on 9/10/16.
 */
public class EventParam {
    private String type;
    private String name;
    private String xpath;

    public EventParam(String type, String name, String xpath) {
        this.type = type;
        this.name = name;
        this.xpath = xpath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
}
