package com.allan.androidlearning.unused.impl;

import com.allan.androidlearning.unused.base.IAndroidResourceScanner;
import com.allan.androidlearning.unused.beans.ids.DrawableId;
import com.allan.androidlearning.unused.beans.ids.LayoutId;
import com.allan.androidlearning.unused.beans.ids.ResId;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UnusedResourceScanner extends IAndroidResourceScanner {
    private File resDir;

    @Override
    public void initResPath(String res) {
        resDir = new File(res);
    }

    @Override
    public List<DrawableId> scanAllDrawablesMipmaps() {
        var subFiles = resDir.listFiles();
        var allDrawablesDirs = Arrays.stream(subFiles).filter(file ->
                file.getName().startsWith("drawable") || file.getName().startsWith("mipmap")
        ).collect(Collectors.toSet());

        //name to  id
        var map = new HashMap<String, DrawableId>();

        for (File dir : allDrawablesDirs) {
            var files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    var name = getName(file);

                    if (!map.containsKey(name)) {
                        var filePath = new ArrayList<String>();
                        filePath.add(file.getAbsolutePath());
                        map.put(name, new DrawableId(name, filePath));
                    } else {
                        var filePath = map.get(name).fullPath();
                        filePath.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return map.values().stream().toList();
    }

    private static String getName(File file) {
        var name = file.getName();
        if (name.endsWith(".9.png")) {
            name = name.substring(0, name.length() - 6);
        } else {
            var index = name.lastIndexOf(".");
            name = name.substring(0, index);
        }
        return name;
    }

    private static String getBindingName(String input) {
        //根据name转成驼峰命名，首字母大写，去除_，将_后的字符转成大写
        if (input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpper = false;
                } else {
                    if (i == 0) {
                        result.append(Character.toUpperCase(currentChar));
                    } else {
                        result.append(Character.toLowerCase(currentChar));
                    }
                }
            }
        }

        return result + "Binding";
    }

    @Override
    public List<LayoutId> scanAllLayouts() {
        var subFiles = resDir.listFiles();
        var allLayoutDirs = Arrays.stream(subFiles).filter(file ->
                file.getName().startsWith("layout")
        ).collect(Collectors.toSet());

        //name to  id
        var map = new HashMap<String, LayoutId>();

        for (File dir : allLayoutDirs) {
            var files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    var name = getName(file);
                    var bindingName = getBindingName(name);

                    if (!map.containsKey(name)) {
                        var filePath = new ArrayList<String>();
                        filePath.add(file.getAbsolutePath());
                        map.put(name, new LayoutId(name, bindingName, filePath));
                    } else {
                        var filePath = map.get(name).fullPath();
                        filePath.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return map.values().stream().toList();
    }

    @Override
    public List<ResId> scanAllValues() {
        var subFiles = resDir.listFiles();
        var allValuesDirs = Arrays.stream(subFiles).filter(file -> file.getName().startsWith("values")).collect(Collectors.toSet());

        var resultList = new ArrayList<ResId>();

        for (File dir : allValuesDirs) {
            var files = dir.listFiles();
            if (files != null) {
                try {
                    for (File file : files) {
                        resultList.addAll(ResXmlParser.parseXmlToColorDimenStringId(file));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return resultList;
    }
}
