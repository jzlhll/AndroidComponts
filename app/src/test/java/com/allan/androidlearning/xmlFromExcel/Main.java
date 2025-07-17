package com.allan.androidlearning.xmlFromExcel;

import com.allan.androidlearning.common.StringImportBean;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    /**
     * 先从外部xml获取成List。
     * 然后，将它还原到strings.xml里面去。
     */
    public static void main(String[] args) {
        //1. 解析调解好的xml
        List<StringImportBean> cvtItems = new XmlToArrayListConverter("D:\\tm.xlsx").load();
        var cvtMap = cvtItems.stream().collect(Collectors.toMap(StringImportBean::getKey, bean -> bean));

        for (var cvtItem : cvtItems) {
            System.out.println(cvtItem);
        }
        System.out.println("!!!!!!!");

        //2. 解析android res的strings.xml
        StringsXmlReader reader = new StringsXmlReader("D:\\code\\typhur\\typhur_app\\app\\src\\main\\res");
        var stringXmls = reader.readAllResources();

        //3. 匹配并重新输出
        System.out.println("en.............");
        doStringXmls(stringXmls.stream().filter(s -> "en".equals(s.language)).toList(), cvtMap);
        System.out.println("fr.............");
        doStringXmls(stringXmls.stream().filter(s -> "fr".equals(s.language)).toList(), cvtMap);
        System.out.println("de.............");
        doStringXmls(stringXmls.stream().filter(s -> "de".equals(s.language)).toList(), cvtMap);
    }

    private static void doStringXmls(List<StringsXmlReader.StringResourceItem> stringXmls, Map<String, StringImportBean> cvtMap) {
        for (var stringXml : stringXmls) {
            var cvtItem = cvtMap.get(stringXml.key);

            if (cvtItem != null && stringXml.translatable) {
                if (stringXml.language.equals("en") && !cvtItem.enAdj.isEmpty()) {
                    System.out.println("<string name=\"" + stringXml.key + "\">" + cvtItem.enAdj + "</string>");
                } else if (stringXml.language.equals("fr") && !cvtItem.frAdj.isEmpty()) {
                    System.out.println("<string name=\"" + stringXml.key + "\">" + cvtItem.frAdj + "</string>");
                } else if (stringXml.language.equals("de") && !cvtItem.deAdj.isEmpty()) {
                    System.out.println("<string name=\"" + stringXml.key + "\">" + cvtItem.deAdj + "</string>");
                } else {
                    System.out.println("<string name=\"" + stringXml.key + "\">" + stringXml.value + "</string>");
                }
            } else {
                System.out.println("<string name=\"" + stringXml.key + "\">" + stringXml.value + "</string>");
            }
        }
    }
}
