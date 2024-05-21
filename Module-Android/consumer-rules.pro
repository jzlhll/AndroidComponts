-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.preference.Preference
-keep public class * extends android.content.ContentProvider

#ViewBinding不混淆
-keep class * implements androidx.viewbinding.ViewBinding {
  ** bind(...);
  ** inflate(...);
}

#保留基础库
-keep class com.au.module_android.widget.** { *;}
-keep class com.au.module_android.ui.** { *;}

-keep class * extends android.app.Dialog {
 <init>(...);
}

-keepclassmembers public class * extends android.app.Dialog {
void set*(***);
*** get*();
}

-keep class * implements com.au.module_android.widget.* {
 <init>(...);
}

-keepclassmembers public class * extends android.view.View {
void set*(***);
*** get*();
}

-keep class androidx.viewpager2.widget.ViewPager2{
androidx.viewpager2.widget.PageTransformerAdapter mPageTransformerAdapter;
androidx.viewpager2.widget.ScrollEventAdapter mScrollEventAdapter;
}

-keep class androidx.viewpager2.widget.PageTransformerAdapter{
androidx.recyclerview.widget.LinearLayoutManager mLayoutManager;
}

-keep class androidx.recyclerview.widget.RecyclerView$LayoutManager{
androidx.recyclerview.widget.RecyclerView mRecyclerView;
}

-keep class androidx.viewpager2.widget.ScrollEventAdapter{
androidx.recyclerview.widget.LinearLayoutManager mLayoutManager;
}

#gilde
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

##---------------Begin: proguard configuration for Gson  ----------
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *;}
-keep class org.json.* {*;}
# Gson specific classes
-dontwarn sun.misc.**

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }
# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
### end of gson ####3

# lifecycle
-keep class androidx.lifecycle.** { *;}