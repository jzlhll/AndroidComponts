package com.au.module_android.simplelivedata;

import androidx.lifecycle.MutableLiveData;

public class RealMutableLiveData<T> extends MutableLiveData<T> {
    public RealMutableLiveData(T value) {
        super(value);
    }

    /**
     * Creates a MutableLiveData with no value assigned to it.
     */
    public RealMutableLiveData() {
        super();
    }

    private volatile Object mRealData;

    @Override
    public void setValue(T value) {
        mRealData = value;
        super.setValue(value);
    }

    @Override
    public void postValue(T value) {
        mRealData = value;
        super.postValue(value);
    }

    T getRealValue() {
        if (mRealData == null) {
            return null;
        }
        return (T) mRealData;
    }
}
