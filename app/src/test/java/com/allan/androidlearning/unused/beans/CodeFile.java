package com.allan.androidlearning.unused.beans;

import com.allan.androidlearning.unused.base.ExLevel;

public record CodeFile(String path,
                       String name,
                       String extension,
                       ExLevel exLevel,
                       String[] allLines) {
    @Override
    public String toString() {
        return path + ", " +
                "exLevel: " + exLevel + ", " +
                "allLinesLength: " + allLines.length;
    }

    public boolean isXml() {
        return "xml".equalsIgnoreCase(extension);
    }
}
