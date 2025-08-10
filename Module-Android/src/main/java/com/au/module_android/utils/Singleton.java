package com.au.module_android.utils;

public abstract class Singleton<T> {

    private T instance;

    public abstract T create();

    public final T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = create();
                }
            }
        }
        return instance;
    }
}
