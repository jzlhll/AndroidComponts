#ViewBinding不混淆
-keep class * implements androidx.viewbinding.ViewBinding {
  ** bind(...);
  ** inflate(...);
}

#与泛型相关的反射不混淆

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
-keep class * implements com.au.module_androidui.widget.* {
 <init>(...);
}
-keep class * implements com.au.module_androidui.dialogs.* {
 <init>(...);
}

-keepclassmembers public class * extends android.view.View {
void set*(***);
*** get*();
}

#banner
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

-flattenpackagehierarchy
-allowaccessmodification
-keepattributes Exceptions,InnerClasses,Signature,SourceFile,LineNumberTable
-ignorewarnings

#kotlin 相关
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
#-keepclassmembers class kotlin.Metadata {
#    public <methods>;
#}
#-keepclasseswithmembers @kotlin.Metadata class * { *; }
#-keepclassmembers class **.WhenMappings {
#    <fields>;
#}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keep class kotlinx.** { *; }
-keep interface kotlinx.** { *; }
-dontwarn kotlinx.**

-keep class org.jetbrains.** { *; }
-keep interface org.jetbrains.** { *; }

-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }

-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

-dontwarn org.jetbrains.**

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep class **.R$* {*;}
-keepclassmembers enum * { *;}

-keep class android.databinding.** { *; }

#gson 继承module-android即可

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
-dontwarn com.bumptech.glide.**
