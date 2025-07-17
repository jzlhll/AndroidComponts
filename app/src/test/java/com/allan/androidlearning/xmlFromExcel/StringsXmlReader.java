package com.allan.androidlearning.xmlFromExcel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StringsXmlReader {
    public static final class StringResourceItem {
        public String key;
        public String value;
        public String language; // 语言代码，如 "en", "de", "fr" 等
        public boolean translatable; // 新增的translatable字段

        public StringResourceItem(String key, String value, String language, boolean translatable) {
            this.key = key;
            this.value = value;
            this.language = language;
            this.translatable = translatable;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s := %s, %b",
                    language, key, value, translatable);
        }
    }

    private final String resDirectory;
    
    public StringsXmlReader(String resDirectory) {
        this.resDirectory = resDirectory;
    }
    
    public List<StringResourceItem> readAllResources() {
        List<StringResourceItem> allItems = new ArrayList<>();
        
        File resDir = new File(resDirectory);
        if (!resDir.exists() || !resDir.isDirectory()) {
            System.err.println("Resource directory not found: " + resDirectory);
            return allItems;
        }
        
        File[] valueDirs = resDir.listFiles(file -> 
            file.isDirectory() && file.getName().startsWith("values"));
        
        if (valueDirs == null) return allItems;
        
        for (File valueDir : valueDirs) {
            String language = extractLanguageCode(valueDir.getName());
            File stringsFile = new File(valueDir, "strings.xml");
            
            if (stringsFile.exists()) {
                allItems.addAll(parseStringsXml(stringsFile, language));
            }
        }
        
        return allItems;
    }
    
    private List<StringResourceItem> parseStringsXml(File xmlFile, String language) {
        List<StringResourceItem> items = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            
            NodeList stringNodes = doc.getElementsByTagName("string");
            
            for (int i = 0; i < stringNodes.getLength(); i++) {
                Node node = stringNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String key = element.getAttribute("name");
                    String value = element.getTextContent();
                    
                    // 获取translatable属性，默认为true
                    boolean translatable = true;
                    if (element.hasAttribute("translatable")) {
                        translatable = Boolean.parseBoolean(element.getAttribute("translatable"));
                    }
                    
                    items.add(new StringResourceItem(key, value, language, translatable));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing " + xmlFile.getPath() + ": " + e.getMessage());
        }
        
        return items;
    }
    
    private String extractLanguageCode(String dirName) {
        if (dirName.equals("values")) return "en";
        if (dirName.startsWith("values-")) {
            var langCode = dirName.substring("values-".length());
            if ("de-rDE".equals(langCode)) {
                return "de";
            }
            if ("fr-rFR".equals(langCode)) {
                return "fr";
            }
        }
        return "unknown";
    }
}