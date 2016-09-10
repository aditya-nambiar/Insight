package com.android.insights;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Created by nambiar on 9/10/16.
 */
public class Utils {

    private static String TAG = "ScraperUtils";
    private static Pattern XML10Pattern = Pattern.compile("[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]");

    public static ArrayList<String> getResourceListFromNode(AccessibilityNodeInfo rootNode) {
        Queue<AccessibilityNodeInfo> bfsQueue = new LinkedList<>();
        bfsQueue.add(rootNode);

        ArrayList<String> nodeList = new ArrayList<>();

        while ( ! bfsQueue.isEmpty()) {
            AccessibilityNodeInfo node = bfsQueue.remove();
            if ( node != null ) {
                String resourceId = node.getViewIdResourceName();
                if (resourceId != null && resourceId != "") {
                    nodeList.add(resourceId);
                }
                int count = node.getChildCount();
                for (int i = 0; i < count; i++) {
                    bfsQueue.add(node.getChild(i));
                }
//                node.recycle();
            }
        }
        return nodeList;
    }

    public static ArrayList<String> getTextListFromNode(AccessibilityNodeInfo rootNode) {
        Queue<AccessibilityNodeInfo> bfsQueue = new LinkedList<>();
        bfsQueue.add(rootNode);

        ArrayList<String> nodeList = new ArrayList<>();

        while ( ! bfsQueue.isEmpty()) {
            AccessibilityNodeInfo node = bfsQueue.remove();

            if ( node != null) {
                Rect outBounds = new Rect();
                node.getBoundsInScreen(outBounds);

                CharSequence text = node.getText();
                if (text != null && text != "") {
                    nodeList.add(text.toString());
                }
                int count = node.getChildCount();
                for (int i = 0; i < count; i++) {
                    bfsQueue.add(node.getChild(i));
                }
//                node.recycle();
            }
        }
        return nodeList;
    }

    private static String safeCharSeqToString(CharSequence cs) {
        if (cs == null) return "";
        else {
            return stripInvalidXMLChars(cs);
        }
    }

    // Original Google code here broke UTF characters
    private static String stripInvalidXMLChars(CharSequence charSequence) {
        final StringBuilder sb = new StringBuilder(charSequence.length());
        sb.append(charSequence);
        return XML10Pattern.matcher(sb.toString()).replaceAll("?");
    }

    public static void DumpNodeXML(AccessibilityNodeInfo node, XmlSerializer serializer, int index) throws IOException {
        serializer.startTag("", "node");
        serializer.attribute("", "index", Integer.toString(index));
        serializer.attribute("", "resource-id", safeCharSeqToString(node.getViewIdResourceName()));
        serializer.attribute("", "text", safeCharSeqToString(node.getText()));
        serializer.attribute("", "class", safeCharSeqToString(node.getClassName()));
        serializer.attribute("", "package", safeCharSeqToString(node.getPackageName()));
        serializer.attribute("", "content-desc", safeCharSeqToString(node.getContentDescription()));
        serializer.attribute("", "checkable", Boolean.toString(node.isCheckable()));
        serializer.attribute("", "checked", Boolean.toString(node.isChecked()));
        serializer.attribute("", "clickable", Boolean.toString(node.isClickable()));
        serializer.attribute("", "enabled", Boolean.toString(node.isEnabled()));
        serializer.attribute("", "focusable", Boolean.toString(node.isFocusable()));
        serializer.attribute("", "focused", Boolean.toString(node.isFocused()));
        serializer.attribute("", "scrollable", Boolean.toString(node.isScrollable()));
        serializer.attribute("", "long-clickable", Boolean.toString(node.isLongClickable()));
        serializer.attribute("", "password", Boolean.toString(node.isPassword()));
        serializer.attribute("", "selected", Boolean.toString(node.isSelected()));


        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                    DumpNodeXML(child, serializer, i);
                    child.recycle();
            } else {
                Log.v(TAG, String.format("Null child %d/%d, parent: %s", i, count, node.toString()));
            }
        }
        serializer.endTag("", "node");
    }

}
