#ifndef MODULE_NATIVE_CODE
#define MODULE_NATIVE_CODE

#include <jni.h>
#include <android/log.h>
#include <string>
#include <jni.h>
#include <jni.h>
#include <jni.h>
#include <jni.h>

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

std::string reflectGetSha1(JNIEnv *env, jobject contextObject) {
    jclass cls = (env)->FindClass("o/C01");
    jmethodID mid = (env)->GetStaticMethodID(cls, "co00", "(Landroid/content/Context;)Ljava/lang/String;");
    jobject sha1 = (env)->CallStaticObjectMethod(cls, mid, contextObject);
    const char* sha1Str = (env)->GetStringUTFChars((jstring)sha1, nullptr);
    std::string cppString = sha1Str;
    env->ReleaseStringUTFChars((jstring)(sha1), sha1Str);
    return cppString;
}

////干扰项
jstring find(JNIEnv *env, jobject thiz, jobject contexObject) {
    jclass contextTemp = env->FindClass("android/content/Context");
    jclass contextClass = (jclass) env->NewGlobalRef(contextTemp);
    jmethodID getPackageManager = env->GetMethodID(contextClass,
            "getPackageManager",
            "()Landroid/content/pm/PackageManager;");
    jobject share = (jobject) env->CallObjectMethod(contexObject, getPackageManager);

    jmethodID getPackageName = env->GetMethodID(contextClass, "getPackageName",
            "()Ljava/lang/String;");
    jstring packageName = (jstring) env->CallObjectMethod(contexObject, getPackageName);
    jclass clazz = env->FindClass("android/content/pm/PackageManager");

    jfieldID GET_SIGNATURESfid = env->GetStaticFieldID(clazz, "GET_SIGNATURES", "I");
    jint GET_SIGNATURES = env->GetStaticIntField(clazz, GET_SIGNATURESfid);
//jclass temp = env->FindClass("android/content/pm/PackageManager");
    jclass PackageManagerObj = (jclass) env->NewGlobalRef(clazz);
    jmethodID getPackageInfoId = env->GetMethodID(PackageManagerObj, "getPackageInfo",
            "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    jobject getPackageInfoObject = (jobject) env->CallObjectMethod(share, getPackageInfoId,
            packageName, GET_SIGNATURES);
    jclass packageInfoClass = env->FindClass("android/content/pm/PackageInfo");
    jfieldID signatures = env->GetFieldID(packageInfoClass, "signatures",
            "[Landroid/content/pm/Signature;");
    jobjectArray rows = reinterpret_cast<jobjectArray>(
            env->GetObjectField(getPackageInfoObject, signatures));
    int size = env->GetArrayLength(rows);
    jclass signaturesClass = env->FindClass("android/content/pm/Signature");
    jclass mEssage = env->FindClass("java/security/MessageDigest");
    jclass MessageDigestClass = (jclass) env->NewGlobalRef(mEssage);
    jmethodID mGetInstance = env->GetStaticMethodID(MessageDigestClass, "getInstance",
            "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jmethodID updateMethod = env->GetMethodID(MessageDigestClass, "update", "([B)V");
    jmethodID toByteArrayMethod = env->GetMethodID(signaturesClass, "toByteArray", "()[B");

    jclass tempBase64 = env->FindClass("android/util/Base64");
    jclass Base64Class = (jclass) env->NewGlobalRef(tempBase64);

    jmethodID encodeToStringID = env->GetStaticMethodID(Base64Class, "encodeToString",
            "([BI)Ljava/lang/String;");
    jmethodID digest = env->GetMethodID(MessageDigestClass, "digest", "()[B");
    for (int i = 0; i < size; i++) {
        jobject messageDigest = env->CallStaticObjectMethod(MessageDigestClass, mGetInstance,
                env->NewStringUTF("SHA"));
        jobject signature = env->GetObjectArrayElement(rows, i);
        env->CallVoidMethod(messageDigest, updateMethod,
                (jbyteArray) env->CallObjectMethod(signature, toByteArrayMethod));
        return (jstring) env->CallStaticObjectMethod(Base64Class, encodeToStringID,
                env->CallObjectMethod(messageDigest, digest),
                (jint) 0);
    }
    return env->NewStringUTF("");
}

////干扰项
const char* getAppSign(JNIEnv *env, jobject contextObject) {
    auto contextClass = (jclass)env->NewGlobalRef((env)->FindClass("android/content/Context"));
    auto signatureClass = (jclass)env->NewGlobalRef((env)->FindClass("android/content/pm/Signature"));
    auto packageNameClass = (jclass)env->NewGlobalRef((env)->FindClass("android/content/pm/PackageManager"));
    auto packageInfoClass = (jclass)env->NewGlobalRef((env)->FindClass("android/content/pm/PackageInfo"));

    jmethodID getPackageManagerId = (env)->GetMethodID(contextClass, "getPackageManager","()Landroid/content/pm/PackageManager;");
    jmethodID getPackageNameId = (env)->GetMethodID(contextClass, "getPackageName","()Ljava/lang/String;");
    jmethodID signToStringId = (env)->GetMethodID(signatureClass, "toCharsString","()Ljava/lang/String;");
    jmethodID getPackageInfoId = (env)->GetMethodID(packageNameClass, "getPackageInfo","(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");

    jobject packageManagerObject =  (env)->CallObjectMethod(contextObject, getPackageManagerId);
    auto packNameString =  (jstring)(env)->CallObjectMethod(contextObject, getPackageNameId);
    jobject packageInfoObject = (env)->CallObjectMethod(packageManagerObject, getPackageInfoId, packNameString, 64);
    jfieldID signaturefieldID =(env)->GetFieldID(packageInfoClass,"signatures", "[Landroid/content/pm/Signature;");
    auto signatureArray = (jobjectArray)(env)->GetObjectField(packageInfoObject, signaturefieldID);
    jobject signatureObject = (env)->GetObjectArrayElement(signatureArray,0);

    const char* signString = (env)->GetStringUTFChars((jstring)(env)->CallObjectMethod(signatureObject, signToStringId), nullptr);
    __android_log_print(ANDROID_LOG_DEBUG, TAG,"appSign: %s", signString);
    return signString;
}

char char2charReverse(char c) {
    if (c >= '0' && c <= '9') {
        if (c == '0') {
            return '9';
        }
        return static_cast<char>(c-1);
    }

    if (c >= 'A' && c <= 'Z') {
        if (c == 'A') {
            return 'Z';
        }
        return static_cast<char>(c-1);
    }
    if (c >= 'a' && c <= 'z') {
        if (c == 'a') {
            return 'z';
        }
        return static_cast<char>(c-1);
    }
    return c;
}

std::string back(const std::string& s) {
    std::string result;
    result.reserve(s.length());
    for (char c : s) {
        result += char2charReverse(c);
    }
    return result;
}

bool isMatch;

bool checkSign(JNIEnv *env, jobject context) {
    if (true) {
        return true;
    }

    if (isMatch) {
        log("isMatch %d", isMatch);
        return isMatch;
    }

    std::string signString = reflectGetSha1(env, context);
    log("app sign: %s", signString.c_str());

    std::string appSignRel = APP_SIGN_REL;
    std::string appSignDebug = APP_SIGN_DEBUG;
    auto realAppSignRel = back(appSignRel);
    auto realAppSignDebug = back(appSignDebug);
    isMatch = signString == realAppSignRel || signString == realAppSignDebug;
    log("set sign: REL %s, DEBUG %s isMatch %d", realAppSignRel.c_str(), realAppSignDebug.c_str(), isMatch);
    return isMatch;
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_modulenative_AppNative_appIdAndKey(JNIEnv *env, jclass clazz, jobject context) {
    auto m = checkSign(env, context);
    if (m) {
        std::string appId = APP_ID;
        std::string appKey = APP_KEY;
        return (env)->NewStringUTF((back(appId) + "\n" + back(appKey)).c_str());
    }
    //随便给个假的出去。
    return (env)->NewStringUTF("ab2132389jd44f283j\nadfjk2389248918ddz2f3329vv238jdf");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_modulenative_AppNative_simpleDecoder(JNIEnv *env, jclass clazz, jobject context, jintArray indexes) {
    auto m = checkSign(env, context);
    if (!m) {
        return env->NewStringUTF(""); // 不对劲
    }

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
    auto *result_chars = new (std::nothrow) jchar[len - 1];
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

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_modulenative_AppNative_stringEncryptSecret(JNIEnv *env, jclass clazz, jobject context) {
    auto m = checkSign(env, context);
    if (!m) {
        return env->NewStringUTF(""); // 不对劲
    }

    std::string secretKey = STRING_ENCRYPT_SECRET_KEY;
    return (env)->NewStringUTF((back(secretKey)).c_str());
}

#endif