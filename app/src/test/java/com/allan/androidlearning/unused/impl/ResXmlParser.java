package com.allan.androidlearning.unused.impl;

import com.allan.androidlearning.common.Util;
import com.allan.androidlearning.unused.beans.ids.ResId;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class ResXmlParser {
    private static DocumentBuilder builder;

    public static DocumentBuilder builder() throws ParserConfigurationException {
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        return builder;
    }

    public static ArrayList<ResId> parseXmlToColorDimenStringId(File xmlFile) throws Exception {
        var pathDirName = xmlFile.getParentFile().getName();

        Document doc = builder().parse(xmlFile);

        NodeList stringNodes = doc.getElementsByTagName("string");
        NodeList stringArrayNodes = doc.getElementsByTagName("string-array");
        NodeList dimenNodes = doc.getElementsByTagName("dimen");
        NodeList colorNodes = doc.getElementsByTagName("color");
        NodeList styleNodes = doc.getElementsByTagName("style");

        var resultList = new ArrayList<ResId>();
        var xmlPath = xmlFile.getAbsolutePath();
        parseNodeList(resultList, stringNodes, "string", pathDirName, xmlFile.getName());
        parseNodeList(resultList, stringArrayNodes, "string-array", pathDirName, xmlFile.getName());
        parseNodeList(resultList, dimenNodes, "dimen", pathDirName, xmlFile.getName());
        parseNodeList(resultList, colorNodes, "color", pathDirName, xmlFile.getName());
        parseNodeList(resultList, styleNodes, "style", pathDirName, xmlFile.getName());

        var resultSize = resultList.size();
        //check 是否提取完整
        var stringList = Files.readAllLines(xmlFile.toPath());
        //将stringList拼凑成strings
        StringBuilder sb = new StringBuilder();
        for (String line : stringList) {
            sb.append(line);
        }
        var strings = sb.toString();

        var allCount = Util.countOccurrences(strings, "<string ")
                + Util.countOccurrences(strings, "<string-array ")
                + Util.countOccurrences(strings, "<dimen ")
                + Util.countOccurrences(strings, "<color ")
                + Util.countOccurrences(strings, "<style ");

        if (resultSize != allCount) {
            System.out.println(">>>>>>>>>: " + xmlPath);
            System.out.println(" resultSize " + resultSize + " realSize " + allCount);
            System.out.println("<<<<<<<<<");
        }

        return resultList;
    }

    private static void parseNodeList(ArrayList<ResId> list, NodeList nodeList, String nodeName, String xmlPath, String fileName) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Element element = (Element) node;
            String name = element.getAttribute("name");
            list.add(new ResId(name, nodeName, xmlPath, fileName));
        }
    }
}
