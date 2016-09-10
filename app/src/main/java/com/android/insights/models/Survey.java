package com.android.insights.models;

/**
 * Created by nambiar on 9/10/16.
 */
public class Survey {
    private String surveyId;
    private String eventType;
    private String packageName;
    private EventTrigger startTrigger;
    private EventTrigger stopTrigger;
    private EventParam[] params;

    public Survey(String surveyId, String eventType, String packageName, EventTrigger startTrigger, EventTrigger stopTrigger, EventParam[] params) {
        this.surveyId = surveyId;
        this.eventType = eventType;
        this.packageName = packageName;
        this.startTrigger = startTrigger;
        this.stopTrigger = stopTrigger;
        this.params = params;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public EventTrigger getStartTrigger() {
        return startTrigger;
    }

    public void setStartTrigger(EventTrigger startTrigger) {
        this.startTrigger = startTrigger;
    }

    public EventTrigger getStopTrigger() {
        return stopTrigger;
    }

    public void setStopTrigger(EventTrigger stopTrigger) {
        this.stopTrigger = stopTrigger;
    }

    public EventParam[] getParams() {
        return params;
    }

    public void setParams(EventParam[] params) {
        this.params = params;
    }
}
