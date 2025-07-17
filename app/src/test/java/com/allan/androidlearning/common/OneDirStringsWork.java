package com.allan.androidlearning.common;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 给出最靠近Strings.xml的目录。即xx/xx/src/main/res/这样只会得到几个strings.xml而无关其他。
 */
public class OneDirStringsWork {
    private final String dir;
    private final String moduleName;
    public OneDirStringsWork(String moduleName, String dir) {
        this.dir = dir;
        this.moduleName = moduleName;
    }

    public HashMap<String, StringExportBean> work() throws Exception {
        var allStringXmls = new HashSet<File>();
        IO.getAllFilesInDirWithFilter(allStringXmls,
                new File(dir),
                f -> "strings.xml".equals(f.getName()), null);

        String deXml = null;
        String frXml = null;
        String enXml = null;
        for (var xml : allStringXmls) {
            String path = xml.getAbsolutePath();
            if (path.contains("-de-")) {
                deXml = path;
            } else if (path.contains("-fr-")) {
                frXml = path;
            } else {
                enXml = path;
            }
        }

        var resultMap = new HashMap<String, StringExportBean>();
        XmlParser.stringParseXml(resultMap, enXml, moduleName, "en");
        XmlParser.stringParseXml(resultMap, deXml, moduleName, "de");
        XmlParser.stringParseXml(resultMap, frXml, moduleName, "fr");

        return resultMap;
    }
}
