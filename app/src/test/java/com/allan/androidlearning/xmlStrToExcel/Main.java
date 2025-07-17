package com.allan.androidlearning.xmlStrToExcel;

import com.allan.androidlearning.common.OneDirStringsWork;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static String OUTPUT_EXCEL_PATH = "D:\\strings%d.xlsx";
    private static final List<OneDirStringsWork> works = new ArrayList<>();
    static {
        works.add(new OneDirStringsWork("app", "D:\\code\\typhur\\typhurOrig\\app\\src\\main\\res"));
        works.add(new OneDirStringsWork("module-android", "D:\\code\\typhur\\typhurOrig\\module-typhur\\module-android\\src\\main\\res"));
        works.add(new OneDirStringsWork("module-base", "D:\\code\\typhur\\typhurOrig\\module-typhur\\module-base\\src\\main\\res"));
        works.add(new OneDirStringsWork("module-player", "D:\\code\\typhur\\typhurOrig\\module-typhur\\module-player\\src\\main\\res"));
    }

    public static void main(String[] args) throws Exception {
        var export = new ExcelExporter();
        export.startXml();

        for (var work : works) {
            export.exportToExcel(work.work());
        }

        var count = 1;
        File target = new File(OUTPUT_EXCEL_PATH.replace("%d", ""));

        while (target.exists()) {
            target = new File(OUTPUT_EXCEL_PATH.replace("%d", "-" + count++));
        }
        export.endXml(target.getAbsolutePath());

        System.out.println(">>>>>>>>");
        System.out.println(">>>>>>>>");
        System.out.println("Done! " + target.getAbsolutePath());
    }
}