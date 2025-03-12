#ifndef MODULE_NATIVE_CODE
#define MODULE_NATIVE_CODE

#include <jni.h>
#include <android/log.h>
#include <string>

#ifdef IS_RELEASE
#define REL
#endif

#ifdef REL
const char *TAG = "appnative_r";
#define log(...)
#else
const char *TAG = "appnative_d";
#define log(...) __android_log_print(ANDROID_LOG_DEBUG,"appnative", __VA_ARGS__)
#endif

//////https://blog.csdn.net/nanke_yh/article/details/124863685 内存释放办法
/////https://www.cnblogs.com/zl1991/p/9449229.html
/// 简单来讲函数体内jobject都不用释放；（没有newGlobalRef，没有全局，没有循环）
/// GetStringUTFChars GetCharArrayElements，GetByteArrayElements需要调用ReleaseXXX释放。


////更多办法，自行构建context；自行native本地获取签名。
//////

//jstring find(JNIEnv *env, jobject thiz, jobject contexObject) {
//    jclass contextTemp = env->FindClass("android/content/Context");
//    jclass contextClass = (jclass) env->NewGlobalRef(contextTemp);
//    jmethodID getPackageManager = env->GetMethodID(contextClass,
//                                                   "getPackageManager",
//                                                   "()Landroid/content/pm/PackageManager;");
//    jobject share = (jobject) env->CallObjectMethod(contexObject, getPackageManager);
//
//    jmethodID getPackageName = env->GetMethodID(contextClass, "getPackageName",
//                                                "()Ljava/lang/String;");
//    jstring packageName = (jstring) env->CallObjectMethod(contexObject, getPackageName);
//    jclass clazz = env->FindClass("android/content/pm/PackageManager");
//
//    jfieldID GET_SIGNATURESfid = env->GetStaticFieldID(clazz, "GET_SIGNATURES", "I");
//    jint GET_SIGNATURES = env->GetStaticIntField(clazz, GET_SIGNATURESfid);
////jclass temp = env->FindClass("android/content/pm/PackageManager");
//    jclass PackageManagerObj = (jclass) env->NewGlobalRef(clazz);
//    jmethodID getPackageInfoId = env->GetMethodID(PackageManagerObj, "getPackageInfo",
//                                                  "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
//    jobject getPackageInfoObject = (jobject) env->CallObjectMethod(share, getPackageInfoId,
//                                                                   packageName, GET_SIGNATURES);
//    jclass packageInfoClass = env->FindClass("android/content/pm/PackageInfo");
//    jfieldID signatures = env->GetFieldID(packageInfoClass, "signatures",
//                                          "[Landroid/content/pm/Signature;");
//    jobjectArray rows = reinterpret_cast<jobjectArray>(
//            env->GetObjectField(getPackageInfoObject, signatures));
//    int size = env->GetArrayLength(rows);
//    jclass signaturesClass = env->FindClass("android/content/pm/Signature");
//    jclass mEssage = env->FindClass("java/security/MessageDigest");
//    jclass MessageDigestClass = (jclass) env->NewGlobalRef(mEssage);
//    jmethodID mGetInstance = env->GetStaticMethodID(MessageDigestClass, "getInstance",
//                                                    "(Ljava/lang/String;)Ljava/security/MessageDigest;");
//    jmethodID updateMethod = env->GetMethodID(MessageDigestClass, "update", "([B)V");
//    jmethodID toByteArrayMethod = env->GetMethodID(signaturesClass, "toByteArray", "()[B");
//
//    jclass tempBase64 = env->FindClass("android/util/Base64");
//    jclass Base64Class = (jclass) env->NewGlobalRef(tempBase64);
//
//    jmethodID encodeToStringID = env->GetStaticMethodID(Base64Class, "encodeToString",
//                                                        "([BI)Ljava/lang/String;");
//    jmethodID digest = env->GetMethodID(MessageDigestClass, "digest", "()[B");
//    for (int i = 0; i < size; i++) {
//        jobject messageDigest = env->CallStaticObjectMethod(MessageDigestClass, mGetInstance,
//                                                            env->NewStringUTF("SHA"));
//        jobject signature = env->GetObjectArrayElement(rows, i);
//        env->CallVoidMethod(messageDigest, updateMethod,
//                            (jbyteArray) env->CallObjectMethod(signature, toByteArrayMethod));
//        return (jstring) env->CallStaticObjectMethod(Base64Class, encodeToStringID,
//                                                     env->CallObjectMethod(messageDigest, digest),
//                                                     (jint) 0);
//    }
//    return env->NewStringUTF("");
//}

std::string signSha1(JNIEnv *env, jobject contextObject) {
    jclass cls = (env)->FindClass("com/sign/Signer");
    jmethodID mid = (env)->GetStaticMethodID(cls, "sha1", "(Landroid/content/Context;)Ljava/lang/String;");
    jobject sha1 = (env)->CallStaticObjectMethod(cls, mid, contextObject);
    const char* sha1Str = (env)->GetStringUTFChars((jstring)sha1, nullptr);
    std::string cppString = sha1Str;
    env->ReleaseStringUTFChars((jstring)(sha1), sha1Str);
    return cppString;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_module_1native_AppNative_appIdAndKey(JNIEnv *env, jclass clazz, jobject context) {
    std::string signString = signSha1(env, context);
    log("sign String: %s", signString.c_str());
    std::string appSignRel = APP_SIGN_REL;
    std::string appSignDebug = APP_SIGN_DEBUG;
    std::string appId = APP_ID;
    std::string appKey = APP_KEY;

    log("APP_SIGN: %s,  %s", appSignRel.c_str(), appSignDebug.c_str());
    bool isMatch = signString == appSignRel || signString == appSignDebug;
    if (isMatch) { //签名一致
        return (env)->NewStringUTF((appId + " " + appKey).c_str());
    }

    //随便给个假的出去。
    return (env)->NewStringUTF("err Error");
}

#endif

extern "C"
JNIEXPORT jstring JNICALL
Java_com_module_1native_AppNative_simpleDecoder(JNIEnv *env, jclass clazz, jintArray indexes) {
    // 获取数组长度
    jsize len = env->GetArrayLength(indexes);
    if (len == 0) {
        return env->NewStringUTF(""); // 空数组返回空字符串
    }

    // 获取数组元素指针
    jint *indexes_arr = env->GetIntArrayElements(indexes, nullptr);
    if (!indexes_arr) {
        return env->NewStringUTF(""); // 空数组返回空字符串
    }

    // 计算偏移量
    jint offset = indexes_arr[0] - 100;

    // 准备结果字符数组
    jchar *result_chars = new (std::nothrow) jchar[len - 1];
    if (!result_chars) {
        env->ReleaseIntArrayElements(indexes, indexes_arr, JNI_ABORT);
        return nullptr; // 内存分配失败
    }

    // 解码字符
    for (jsize i = 1; i < len; ++i) {
        result_chars[i - 1] = static_cast<jchar>(indexes_arr[i] + offset);
    }

    // 构建 Java 字符串
    jstring result = env->NewString(result_chars, len - 1);

    // 清理资源
    delete[] result_chars;
    env->ReleaseIntArrayElements(indexes, indexes_arr, JNI_ABORT);

    return result;
}