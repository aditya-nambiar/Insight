package com.android.insights.models;

import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nambiar on 9/10/16.
 */
public class Datapoint {
    private String surveyId;
    private long startTime;
    private long endTime;
    private String packageName;

    private static String TAG = "Datapoint";
    private static String API_ENDPOINT = "http://139.59.17.164:9200/droid-5/stat";

    private HashMap<String, String> params;

    private static HashMap<String, String> userData;

    public Datapoint(String surveyId, long startTime, long endTime, String packageName, HashMap<String, String> params) {
        this.surveyId = surveyId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.packageName = packageName;
        this.params = params;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public static HashMap<String, String> getUserData() {
        return userData;
    }

    public static void setUserData(HashMap<String, String> userData) {
        Datapoint.userData = userData;
    }

    /*public String getAppName(String package_name) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(package_name, PackageManager.GET_META_DATA));
        return appName;
    }*/

    @Override
    public String toString() {
        return "Datapoint{" +
                "surveyId='" + surveyId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", packageName='" + packageName + '\'' +
                ", params=" + params +
                '}';
    }

    public JsonObjectRequest createRequest() throws JSONException {

            JSONObject obj = new JSONObject();
            obj.put("surveyId", surveyId);
            obj.put("packageName", packageName);
//            obj.put("appName", getAppName(packageName));
            obj.put("startTime", startTime);
            obj.put("endTime", endTime);
            obj.put("userData", new JSONObject(userData));
//            obj.put("app_category", packages_to_category.get(package_name));
            Log.v(TAG, "request data - " + obj);
            // Request a string response from the provided URL.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_ENDPOINT, obj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Display the first 500 characters of the response string.
                            Log.v(TAG, "Response is: "+ response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "That didn't work!" + error);
                }
            }
            );

            return jsonObjectRequest;

    }
}
