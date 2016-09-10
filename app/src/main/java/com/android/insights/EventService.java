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
    private RequestQueue queue;
    private static  HashMap<String, ArrayList<Survey> > allSurveys;
    private static  XPath xPath;


    @Override
    public void onServiceConnected() {
        Log.v(TAG, "Service connected");
        allSurveys = new HashMap<String, ArrayList<Survey>>();
        // read surveys.json
        String allSurveysStr = readSurveys();
        initTriggers(allSurveysStr);
        Log.v(TAG, allSurveys.size() + "surveys");
        xPath =  XPathFactory.newInstance().newXPath();
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

    boolean isValidActivityIdentifier(Document xmlDocument, ActivityIdentifier activityIdentifier) throws XPathExpressionException {

        for( String str : activityIdentifier.getContainsXpath()) {
            Log.v(TAG, str);
            Node node = (Node) xPath.compile(str).evaluate(xmlDocument, XPathConstants.NODE);
            Log.v(TAG, "Node - " + node);
            if(node == null) {
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

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo root = this.getRootInActiveWindow();
        if(root == null) return;
        String package_name = event.getPackageName().toString();
//        Log.v(TAG, package_name + " " + allSurveys.containsKey(package_name));
        // check if trigger exists for current active app
        Document xmlDocument = null;
        if(allSurveys == null || allSurveys.isEmpty() || !allSurveys.containsKey(package_name)) {
            return;
        }
        Log.v(TAG, root.findAccessibilityNodeInfosByViewId("com.flipkart.android:id/vg_scrollable_header").size() + "");
        for(Survey currSurvey : allSurveys.get(package_name)){
            if (xmlDocument == null) {
                xmlDocument = convertAccessibiltyNodetoXML(root);
            }
//            Log.v(TAG, xmlDocument.toString());
            Log.v(TAG, "Got xml doc");
            // check if survey is for current app screen
            try {
                if (isValidActivityIdentifier(xmlDocument, currSurvey.getStartTrigger().getActivityIdentifier())) {
                    Log.v(TAG, "Event Triggered");
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
            // extract parameters
    }

    @Override
    public void onInterrupt() {
    }
}
