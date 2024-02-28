-keepattributes Annotation
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers public class com.github.lzyzsd.jsbridge.BridgeObject{
    <fields>;
    <methods>;
    public *;
    private *;
}