#ifndef MODULE_NATIVE_CODE
#define MODULE_NATIVE_CODE

#include <jni.h>
#include <vector>
#include <sstream>
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

//反射调用得到sha1签名
std::string refS1(JNIEnv *env, jobject contextObject) {
    char clazzName[5];
    clazzName[0] = 'o';
    clazzName[1] = '/';
    clazzName[2] = 'S';
    clazzName[3] = '0';
    clazzName[4] = '\0';
    jclass cls = (env)->FindClass(clazzName);
    char funName[3];
    funName[0] = 's';
    funName[1] = '1';
    funName[2] = '\0';
    std::string funSign = "(";
    funSign += "Landroid/content/Context;)";
    funSign += "Ljava/lang/String;";
    jmethodID mid = (env)->GetStaticMethodID(cls, funName, funSign.c_str());
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

//std::string back(const std::string& ciphertext) {
//    // 分割加密文本
//    std::vector<std::string> mParts;
//    std::istringstream iss(ciphertext);
//    std::string token;
//
//    while (std::getline(iss, token, ',')) {
//        mParts.push_back(token);
//    }
//
//    if (mParts.size() < 2) {
//        throw std::invalid_argument("无效的加密文本");
//    }
//    // 提取偏移量
//    int offset = std::stoi(mParts[0]);
//    // 解密每个值
//    std::string decrypted;
//    for (size_t i = 1; i < mParts.size(); i++) {
//        int encrypted_val = std::stoi(mParts[i]);
//        int original_ascii = encrypted_val - offset;
//        decrypted += static_cast<char>(original_ascii);
//    }
//
//    return decrypted;
//}

std::string back(const std::string& ciphertext) {
    // 检查输入有效性
    if (ciphertext.empty()) {
        throw std::invalid_argument("无效的加密文本");
    }

    // 查找第一个逗号位置
    size_t start = 0;
    size_t comma_pos = ciphertext.find(',');

    // 若找不到逗号或逗号在末尾则无效
    if (comma_pos == std::string::npos || comma_pos == ciphertext.length() - 1) {
        throw std::invalid_argument("无效的加密文本");
    }

    // 提取偏移量（第一个逗号前的子串）
    int offset = std::stoi(ciphertext.substr(0, comma_pos));
    std::string decrypted;
    start = comma_pos + 1; // 移动到第一个加密值起始位置

    // 遍历剩余字符串解析每个加密值
    while (start < ciphertext.length()) {
        // 查找下一个逗号
        comma_pos = ciphertext.find(',', start);

        // 提取当前加密值子串（到逗号或字符串结尾）
        std::string num_str;
        if (comma_pos == std::string::npos) {
            num_str = ciphertext.substr(start);
            start = ciphertext.length(); // 结束循环
        } else {
            num_str = ciphertext.substr(start, comma_pos - start);
            start = comma_pos + 1; // 移动到下一个加密值
        }

        // 转换并解密当前值
        if (!num_str.empty()) {
            int encrypted_val = std::stoi(num_str);
            decrypted += static_cast<char>(encrypted_val - offset);
        } else {
            throw std::invalid_argument("加密值不能为空");
        }
    }

    return decrypted;
}

bool isMatch;

bool checkSign(JNIEnv *env, jobject context) {
    if (isMatch) {
        log("isMatch %d", isMatch);
        return isMatch;
    }

    std::string signString = refS1(env, context);
    log("app sign: %s", signString.c_str());

    std::string appSign = APP_SIGN;
    auto realAppSignRel = back(appSign);
    isMatch = signString == realAppSignRel;
    log("set sign: REL %s, isMatch %d", realAppSignRel.c_str(), isMatch);
#ifdef REL
    return isMatch;
#else
    return true;
#endif
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_modulenative_AppNative_appIdKey(JNIEnv *env, jclass clazz, jobject context) {
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
JNIEXPORT jstring
JNICALL
Java_com_modulenative_AppNative_strEk(JNIEnv *env, jclass clazz, jobject context) {
    auto m = checkSign(env, context);
    if (!m) {
        return env->NewStringUTF(""); // 不对劲
    }

    std::string secretKey = ENCRYPT_STRING_KEY;
    return (env)->NewStringUTF((back(secretKey)).c_str());
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_modulenative_AppNative_asts(JNIEnv *env, jclass clazz, jobject context, jstring af) {
    auto m = checkSign(env, context);
    if (!m) {
        return env->NewStringUTF(""); // 不对劲
    }

    char classNames[5];
    classNames[0] = 'o';
    classNames[1] = '/';
    classNames[2] = 'A';
    classNames[3] = '0';
    classNames[4] = '\0';
    char funName[3];
    funName[0] = 't';
    funName[1] = '1';
    funName[2] = '\0';
    jclass cls = env->FindClass(classNames);
    std::string funSign = "(";
    funSign += "Landroid/content/Context;";
    funSign += "Ljava/lang/String;";
    funSign += "Ljava/lang/String;";
    funSign += ")";
    funSign += "Ljava/lang/String;";
    jmethodID mid = env->GetStaticMethodID(cls, funName, funSign.c_str());
    std::string k = ENCRYPT_ASSETS_KEY;
    jstring jK = (env)->NewStringUTF((back(k)).c_str());
    return (jstring) env->CallStaticObjectMethod(cls, mid, context, af, jK);
}


extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_modulenative_AppNative_astf(JNIEnv *env, jclass clazz, jobject context,
                                   jstring af, jstring tp) {
    auto m = checkSign(env, context);
    if (!m) {
        return false; // 不对劲
    }

    char classNames[5];
    classNames[0] = 'o';
    classNames[1] = '/';
    classNames[2] = 'A';
    classNames[3] = '0';
    classNames[4] = '\0';
    char funName[3];
    funName[0] = 'f';
    funName[1] = 'c';
    funName[2] = '\0';
    jclass cls = env->FindClass(classNames);
    std::string funSign = "(";
    funSign += "Landroid/content/Context;";
    funSign += "Ljava/lang/String;";
    funSign += "Ljava/lang/String;";
    funSign += "Ljava/lang/String;";
    funSign += ")";
    funSign += "Z";
    jmethodID mid = env->GetStaticMethodID(cls,"fc",funSign.c_str());
    std::string k = ENCRYPT_ASSETS_KEY;
    jstring jk = (env)->NewStringUTF((back(k)).c_str());
    return env->CallStaticBooleanMethod(
            cls,
            mid,
            context,
            af,
            tp,
            jk
    );
}
#endif