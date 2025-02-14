package com.au.module_cached.delegate

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.au.module_android.Globals
import com.au.module_android.utils.unsafeLazy
import com.au.module_cached.AppDataStore
import com.au.module_cached.AppDataStore.globalDataStore

interface IDataStoreWrap {
    val dataStore: DataStore<Preferences>
    val cacheFileName:String?
}

class DataStoreWrap(val mCacheFileName:String?) : IDataStoreWrap{
    private val mDataStore: DataStore<Preferences> by unsafeLazy {
        if(mCacheFileName == null) Globals.app.globalDataStore else AppDataStore.onceDataStore(Globals.app, mCacheFileName)
    }

    override val dataStore: DataStore<Preferences>
        get() = mDataStore
    override val cacheFileName: String?
        get() = mCacheFileName
}