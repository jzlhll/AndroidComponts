package com.allan.androidlearning.unused;
import com.allan.androidlearning.common.IO;
import com.allan.androidlearning.unused.base.IAllCode;
import com.allan.androidlearning.unused.beans.ids.DrawableId;
import com.allan.androidlearning.unused.beans.ids.LayoutId;
import com.allan.androidlearning.unused.impl.AllCode;
import com.allan.androidlearning.unused.impl.UnusedResourceScanner;

import java.util.Comparator;

public class Main {
    //Cfg Here： 修改文件夹 兼容 windows和linux mac
    private static final String ROOT_PATH = "D:\\code\\xxx\\xxx\\app";

    //在打印的日志中进行搜索这个字样，就可以过滤出需要处理的事情了。
    private static final String SHOULD_DELETE = "shouldDelete";

    static UnusedResourceScanner scanner = new UnusedResourceScanner();
    static {
        scanner.initResPath(getResPath());
    }

    private static String getResPath() {
        if (IO.IS_WIN) {
            return ROOT_PATH + "\\src\\main\\res";
        }
        return ROOT_PATH + "/src/main/res";
    }

    //扫描的是values，values-xxx 开头, 这样的目录。
    //能够解决的是string, string-array, dimen, color, style这样的资源
    private static void scanAllValues(IAllCode allCode) {
        var allValueIds = scanner.scanAllValues().stream().sorted((o1, o2) -> {
            var r = o1.node().compareTo(o2.node());
            if (r == 0) {
                return o1.name().compareTo(o2.name());
            }
            return r;
        }).toList();
        System.out.println("scan AllValues size: " + allValueIds.size());

        for(var id : allValueIds) {
            if(id.node().equals("string")) {
                var count = allCode.containsString(id);
                if (count == 0) {
                    System.out.println(SHOULD_DELETE + " [string]: " + id);
                }
            }
            if(id.node().equals("string-array")) {
                var count = allCode.containsArray(id);
                if (count == 0) {
                    System.out.println(SHOULD_DELETE + " [string-array]: " + id);
                }
            }
            if(id.node().equals("dimen")) {
                var count = allCode.containsDimen(id);
                if (count == 0) {
                    System.out.println(SHOULD_DELETE + " [dimen]: " + id);
                }
            }
            if(id.node().equals("color")) {
                var count = allCode.containsColor(id);
                if (count == 0) {
                    System.out.println(SHOULD_DELETE + " [color]: " + id);
                }
            }
            if(id.node().equals("style")) {
                var count = allCode.containsStyle(id);
                if (count == 0) {
                    System.out.println(SHOULD_DELETE + " [style]: " + id);
                }
            }
        }
    }

    private static void scanAllLayout(IAllCode allCode) {
        var list = scanner.scanAllLayouts().stream().sorted(Comparator.comparing(LayoutId::name)).toList();
        System.out.println("scan AllLayout size: " + list.size());
        for (var layout : list) {
            var count = allCode.containsLayout(layout.name(), layout.bindingName());
            if (count == 0) {
                System.out.println(SHOULD_DELETE + " [layout]: " + layout.fullPath());
            }
        }
    }

    private static void scanAllDrawables(IAllCode allCode) {
        var list = scanner.scanAllDrawablesMipmaps().stream().sorted(Comparator.comparing(DrawableId::name)).toList();
        System.out.println("scan All DrawablesMipmaps size: " + list.size());
        for (var draw : list) {
            var count = allCode.containsDrawableMipmap(draw.name());
            if (count == 0) {
                System.out.println(SHOULD_DELETE + " [drawableOrMip]: " + draw.fullPath());
            }
        }
    }

    public static void main(String[] args) {
        IAllCode allCode = new AllCode(ROOT_PATH);
        allCode.readAllCode();

        System.out.println();
        System.out.println("Start Scan Resources=====:");
        scanAllValues(allCode);

        System.out.println("Start Scan Layouts=====:");
        scanAllLayout(allCode);

        System.out.println("Start Scan Drawables Mipmaps=====:");
        scanAllDrawables(allCode);
    }
}
