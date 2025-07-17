package com.allan.androidlearning.resortStrings;

import com.allan.androidlearning.common.IO;
import com.allan.androidlearning.common.OneDirStringsWork;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static final HashMap<String, OneDirStringsWork> worksMap = new HashMap<>();
    static {
        worksMap.put("D:\\tempStrings\\app",
                new OneDirStringsWork("app", "D:\\code\\typhur\\typhurOrig\\app\\src\\main\\res"));
        worksMap.put("D:\\tempStrings\\module-android",
                new OneDirStringsWork("module-android", "D:\\code\\typhur\\typhurOrig\\module-typhur\\module-android\\src\\main\\res"));
        worksMap.put("D:\\tempStrings\\module-base",
                new OneDirStringsWork("module-base", "D:\\code\\typhur\\typhurOrig\\module-typhur\\module-base\\src\\main\\res"));
        worksMap.put("D:\\tempStrings\\module-player",
                new OneDirStringsWork("module-player", "D:\\code\\typhur\\typhurOrig\\module-typhur\\module-player\\src\\main\\res"));

        var keys = worksMap.keySet();
        for (var key : keys) {
            var dir = new File(key);
            IO.deleteDirJava(dir);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            dir.mkdirs();
        }
    }

    private void oneWork() {

    }

    public static void main(String[] args) throws Exception {
        var sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("\n");
        System.out.println("<resources>");

        var langList = new String[]{"en", "de", "fr"};
        for (var lang : langList) {
            for (var entry : worksMap.entrySet()) {
                var work = entry.getValue();
                var dir = entry.getKey();

                var resultMap = work.work();
                var beans = resultMap.entrySet().stream().sorted(
                        Map.Entry.comparingByKey()).collect(Collectors.toCollection(LinkedHashSet::new));
                for (var kv : beans) {
                    var key = kv.getKey();
                    var bean = kv.getValue();
                    if (bean.translatable) {
                        sb.append(String.format("    <string name=\"%s\">%s</string>%n",
                                key, bean.en));
                        sb.append("\n");
                    } else {
                        sb.append(String.format("    <string name=\"%s\" translatable=\"false\">%s</string>%n",
                                key, bean.en));
                        sb.append("\n");
                    }

                    sb.append("\n");
                }
            }
            sb.append("</resources>");

            var str = sb.toString();
            Files.write(new File("dirTodo", "strings-" + lang + ".xml").toPath(), str.getBytes());
        }
    }
}
