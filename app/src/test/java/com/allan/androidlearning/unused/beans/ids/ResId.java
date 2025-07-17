package com.allan.androidlearning.unused.beans.ids;

/**
 * color dimen string string-array style
 */
public record ResId(String name, String node, String dir, String fileName) {
    @Override
    public String toString() {
        return "ResId: " + node + ", " + name + ", " +
                dir + "/" + fileName;
    }
}
