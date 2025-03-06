### appNative
通过JNI的实现，来保护appId和appKey。

第一：jni so天然的防普通的字节码破解
第二：校验签名

局部部分参考https://github.com/yglx/protectSecretKeyDemo实现。