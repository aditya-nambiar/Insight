package com.android.insights;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.insights.models.ActivityIdentifier;
import com.android.insights.models.Datapoint;
import com.android.insights.models.EventParam;
import com.android.insights.models.EventTrigger;
import com.android.insights.models.Survey;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class EventService extends android.accessibilityservice.AccessibilityService {

    private static final String TAG = "EventService";
    private static String current_package = "";
    private static Long start_time;
    private static  HashMap<String, ArrayList<Survey> > allSurveys;
    private static  XPath xPath;
    private RequestQueue queue;

    @Override
    public void onServiceConnected() {
        Log.v(TAG, "Service connected");
        queue = Volley.newRequestQueue(this);

        allSurveys = new HashMap<String, ArrayList<Survey>>();
        // read surveys.json
        String allSurveysStr = readSurveys();
        initTriggers(allSurveysStr);
        Log.v(TAG, allSurveys.size() + "surveys");
        xPath =  XPathFactory.newInstance().newXPath();

        initUserData();
    }

    void initUserData() {
        Random rand = new Random();
        HashMap<String, String> userDataMap =  new HashMap<String, String>();
        String userId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String gender = rand.nextFloat() > 0.5 ? "M" : "F";
        Integer age = rand.nextInt(10) + 20;
        userDataMap.put("userId", userId);
        userDataMap.put("gender", gender);
        userDataMap.put("age", age.toString());
        Datapoint.setUserData(userDataMap);
    }

    void initTriggers(String content){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Log.v(TAG, "JSON" + content);
        Survey[] tempSurveys = gson.fromJson(content, Survey[].class);
        for(Survey currSurvey: tempSurveys){
            if(allSurveys.containsKey(currSurvey.getPackageName())){
                allSurveys.get(currSurvey.getPackageName()).add(currSurvey);
             } else {
                allSurveys.put(currSurvey.getPackageName(), new ArrayList<Survey>(Arrays.asList(currSurvey)));
            }
        }
    }
    String readSurveys(){
        StringBuilder buf=new StringBuilder();
        InputStream json= null;

        try {
            json = getAssets().open("surveys.json");
            if(json == null){
                Log.v(TAG, "NULL JSON");
            }
            BufferedReader in=
                    null;
            in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str=in.readLine()) != null) {
                buf.append(str);
            }
            in.close();

            return buf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    boolean isValidActivityIdentifier(AccessibilityNodeInfo root, ActivityIdentifier activityIdentifier){
        List<String> textList = Utils.getTextListFromNode(root);
        for( String str : activityIdentifier.getContainsText()) {
            if(!textList.contains(str)) {
                return false;
            }
        }

        // resource-id
        List<String> resourceIdList = Utils.getResourceListFromNode(root);
        for( String str : activityIdentifier.getContainsXpath()) {
            if(!resourceIdList.contains(str)) {
                return false;
            }
        }
        return true;
    }

    private void saveFile(String filename, String data) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/insights");
        myDir.mkdirs();
        File file = new File (myDir, filename);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data.getBytes());
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Document convertAccessibiltyNodetoXML(AccessibilityNodeInfo root) {
        StringWriter writer = new StringWriter();
        Document xmlDocument = null;
        try {
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output",
                    true);
            Utils.DumpNodeXML(root, serializer, 0);
            serializer.endDocument();
            Log.v(TAG, "" + writer.toString().length());

        } catch (IOException e) {
            e.printStackTrace();
        }

        DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            saveFile("flipkart.xml", writer.toString());
            Log.v(TAG, "writing to flipkart.xml");
            Log.v(TAG, writer.toString());
            xmlDocument = builder.parse(writer.toString());

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return xmlDocument;
    }

    String extractParam(AccessibilityNodeInfo root, EventParam param) {
        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(param.getXpath());
        if(list.size() > 0) {
            String val = list.get(0).getText().toString();
            return val;
        }

        return "";
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo root = this.getRootInActiveWindow();
        if (root == null) return;
        String package_name = event.getPackageName().toString();

        if(event.getEventType() != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        if (allSurveys == null || allSurveys.isEmpty() || !allSurveys.containsKey(package_name)) {
            return;
        }
        Log.v(TAG, package_name + allSurveys.containsKey(package_name));
        for (Survey currentSurvey : allSurveys.get(package_name)) {
            Log.v(TAG, "source -" + event.getSource().getViewIdResourceName() + " = " + currentSurvey.getStartTrigger().getXpath());

            if(!event.getSource().getViewIdResourceName().equals(currentSurvey.getStartTrigger().getXpath())) {
                continue;
            }
            Log.v(TAG, "checking activity validity");
            // check if survey is for current app screen
            if(isValidActivityIdentifier(root, currentSurvey.getStartTrigger().getActivityIdentifier())) {
                // extract parameters
                Log.v(TAG, "building params");
                HashMap<String, String> paramMap = new HashMap<String, String>();
                for(EventParam param : currentSurvey.getParams()) {
                    paramMap.put(param.getName(), extractParam(root, param));
                }

                // send data
                
                Datapoint dp = new Datapoint(currentSurvey.getSurveyId(), System.currentTimeMillis(), System.currentTimeMillis(),
                        package_name, paramMap);
                Log.v(TAG, "sending request" + dp);
                try {
                    queue.add(dp.createRequest());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
    }
}
