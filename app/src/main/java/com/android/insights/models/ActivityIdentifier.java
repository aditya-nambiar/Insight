package com.android.insights.models;

/**
 * Created by nambiar on 9/10/16.
 */
public class ActivityIdentifier {
    private String[] containsText;
    private String[] containsXpath;

    public ActivityIdentifier(String[] containsText, String[] containsXpath) {
        this.containsText = containsText;
        this.containsXpath = containsXpath;
    }

    public String[] getContainsText() {
        return containsText;
    }

    public void setContainsText(String[] containsText) {
        this.containsText = containsText;
    }

    public String[] getContainsXpath() {
        return containsXpath;
    }

    public void setContainsXpath(String[] containsXpath) {
        this.containsXpath = containsXpath;
    }
}
