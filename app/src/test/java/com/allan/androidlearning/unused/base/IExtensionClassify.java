package com.allan.androidlearning.unused.base;

public interface IExtensionClassify {
    /**
     * 根据extension进行分类。
     * 如果是txt结尾则认为不重要，返回 low
     */
    ExLevel classify(String extension);
}
