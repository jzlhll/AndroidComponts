package com.allan.androidlearning.unused.impl;

import com.allan.androidlearning.unused.base.ExLevel;
import com.allan.androidlearning.unused.base.IExtensionClassify;

public class ClassifyImpl implements IExtensionClassify {
    @Override
    public ExLevel classify(String extension) {
        if ("txt".equalsIgnoreCase(extension)) {
            return ExLevel.LOW;
        }
        return ExLevel.NORMAL;
    }
}
