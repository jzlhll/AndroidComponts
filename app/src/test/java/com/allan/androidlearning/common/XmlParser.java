package com.allan.androidlearning.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.HashMap;

public class XmlParser {
    private static DocumentBuilder builder;

    public static DocumentBuilder builder() throws ParserConfigurationException {
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        return builder;
    }

    public static void stringParseXml(
                                      HashMap<String, StringExportBean> resultMap,
                                      String xmlPath, String moduleName, String lang) throws Exception {
        Document doc = builder().parse(new File(xmlPath));

        NodeList stringNodes = doc.getElementsByTagName("string");

        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String name = element.getAttribute("name");
                String value = element.getTextContent();

                StringExportBean bean = resultMap.getOrDefault(name, new StringExportBean());

                // 处理translatable属性
                if (element.hasAttribute("translatable")) {
                    bean.translatable = !"false".equals(element.getAttribute("translatable"));
                }
                bean.moduleName = moduleName;

                switch (lang) {
                    case "en": bean.en = value; break;
                    case "de": bean.de = value; break;
                    case "fr": bean.fr = value; break;
                }

                resultMap.put(name, bean);
            }
        }
    }
}
