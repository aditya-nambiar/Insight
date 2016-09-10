package com.android.insights.models;

/**
 * Created by nambiar on 9/10/16.
 */
public class EventTrigger {
    private String onEvent;
    private String xpath;
    private ActivityIdentifier activityIdentifier;
    private String packageName;

    public EventTrigger(String onEvent, String xpath, ActivityIdentifier activityIdentifier, String packageName) {
        this.onEvent = onEvent;
        this.xpath = xpath;
        this.activityIdentifier = activityIdentifier;
        this.packageName = packageName;
    }

    public String getOnEvent() {
        return onEvent;
    }

    public void setOnEvent(String onEvent) {
        this.onEvent = onEvent;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public ActivityIdentifier getActivityIdentifier() {
        return activityIdentifier;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public void setActivityIdentifier(ActivityIdentifier activityIdentifier) {
        this.activityIdentifier = activityIdentifier;
    }
}
