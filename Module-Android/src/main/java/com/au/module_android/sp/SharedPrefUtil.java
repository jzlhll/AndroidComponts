package com.au.module_android.sp;

import android.content.Context;

/**
 正确使用的建议
 1、不要存放大的key和value在SharedPreferences中,否则会一直存诸在内存中得不到释放,内存使用过高会频发引发GC,导致界面丢帧甚至ANR。
 2、不相关的配置选项最好不要放在一起,单个文件越大读取速速度则越慢。
 3、读取频繁的key和不频繁的key尽量不要放在一起(如果整个个文件本身就较小则忽略,为了这点性能添加维护得不偿失)。
 4、不要每次都edit,因为每次都会创建一个新的Editorlmpl对象象,最好是批量处理统一提交。
 否则edit().commit每次创建一个Editorlmpl对象并且进行一次IO操作,严重影响性能。
 5、commit发生在Ul线程中,apply发生在工作线程中,对于数据的提交最好是批量操作统一提交。虽然apply发生在工作线程(不会因为IO阻塞UI线程)但是如果添加任
 务较多也有可能带来其他严重后果(参照ActivityThread源码中handleStopActivity方法实现)。
 6、尽量不要存放大json，大html,这种可以直接文件缓存。
 7、不要指望这货能够跨进程通信Context.PROCESS。
 8、最好提前初始化SharedPreferences, 避免SharedPreferences第一次创建时读取文件线程未结束而出现等待情况。

 总结建议：
  1. 用的要少，内容要短；
  2. 拆分；
  3. 太频繁则static sp。
 */
public class SharedPrefUtil {
    private static String SPXMLNAME = "sp_config";

    /**
     * @param ctx
     *            上下文环境
     * @param key
     *            要从config.xml移除节点的name的名称
     */
    public static void removeKey(Context ctx, String key) {
        ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).edit().remove(key).commit();
    }

    // 1,存储boolean变量方法
    public static void putBoolean(Context ctx, String key, boolean value) {
        // name存储文件名称
        ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    // 2,读取boolean变量方法
    public static boolean getBoolean(Context ctx, String key, boolean defValue) {
        // name存储文件名称
        return ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).getBoolean(key, defValue);
    }

    public static void putString(Context ctx, String key, String value) {
        // name存储文件名称
        ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public static String getString(Context ctx, String key, String defValue) {
        // name存储文件名称
        return ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).getString(key, defValue);
    }

    //
    public static void putInt(Context ctx, String key, int value) {
        // name存储文件名称
        ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    public static int getInt(Context ctx, String key, int defValue) {
        // name存储文件名称
        return ctx.getSharedPreferences(SPXMLNAME, Context.MODE_PRIVATE).getInt(key, defValue);
    }
}
