package com.android.insights.playground;

import android.provider.MediaStore;

import com.google.gson.Gson;
import com.android.insights.models.Survey;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Playground {

    static String readFile(String path)
            throws IOException
    {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data);

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World");
//        Gson gson = new Gson();
        String surveyJson = readFile("/Users/nambiar/Documents/Projects/Insights/app/src/main/assets/surveys.json");
        System.out.println(surveyJson);

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        String xmlFlipkart = readFile("/Users/nambiar/Desktop/insights/flipkart.xml");

        DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        StringWriter writer = new StringWriter();
        Document xmlDocument;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(xmlFlipkart.substring(0,500));
            xmlDocument = builder.parse(new ByteArrayInputStream(xmlFlipkart.getBytes("utf-8")));
            XPath xPath =  XPathFactory.newInstance().newXPath();
            String str = "//node[@resource-id='com.flipkart.android:id/product_addtocart_1']";
            String str1 = "//node";
//            NodeList nodeList = (NodeList) xPath.compile(str1).evaluate(xmlDocument, XPathConstants.NODESET);
            /*if(node.isEmpty() || node == ""){
                System.out.println("KATA");
            } else {
                System.out.println("CHAlA");

            }*/
            String xpath = "(//node[@resource-id=\"com.flipkart.android:id/product_list_product_item_mrp\"])[1]/@text";
            String node = (String) xPath.compile(xpath).evaluate(xmlDocument, XPathConstants.STRING);

            System.out.println(xmlDocument);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }


        Survey[] surveyList = gson.fromJson(surveyJson, Survey[].class);
    }
}
