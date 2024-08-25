package com.au.aulitesqlkt

import com.au.aulitesql.Entity
import com.au.aulitesql.actions.ICallback

abstract class BaseEntityMapDao<K, E : Entity?>(val entityClass: Class<E>, val keyField: String) {
    ////////////load////////////////
    abstract suspend fun loadAllFilter(fieldName: String, value: Any, callback: ICallback<Map<K, E>>? = null)
    abstract suspend fun loadAll(callback: ICallback<Map<K, E>>? = null)
    abstract suspend fun loadAll(
        fieldName: String, value: Any?,
        groupBy: String?, having: String?, orderBy: String?, callback: ICallback<Map<K, E>?>?
    )

    fun loadAll(
        selections: String, selectionArgs: Array<String?>,
        groupBy: String?, having: String?, orderBy: String?
    ) {
        loadAll(selections, selectionArgs, groupBy, having, orderBy, null)
    }

    abstract fun loadAll(
        selections: String, selectionArgs: Array<String?>,
        groupBy: String?, having: String?, orderBy: String?, callback: ICallback<Map<K, E>?>?
    )

    fun rawLoadAll(sql: String?, selectionArgs: Array<String?>?) {
        rawLoadAll(sql, selectionArgs, null)
    }

    abstract fun rawLoadAll(sql: String?, selectionArgs: Array<String?>?, callback: ICallback<Map<K, E>?>?)

    //////////////////delete////////////////
    fun deleteKeyAll(keyList: List<K>?) {
        deleteKeyAll(keyList, null)
    }

    abstract fun deleteKeyAll(keyList: List<K>?, deleteCountCallback: ICallback<Int?>?)
    fun deleteKey(key: K) {
        deleteKey(key, null)
    }

    abstract fun deleteKey(key: K, deleteSuccessCallback: ICallback<Boolean?>?)
    fun deleteValue(value: E) {
        deleteValue(value, null)
    }

    abstract fun deleteValue(value: E, deleteSuccessCallback: ICallback<Boolean?>?)
    abstract fun clear(clearSuccessCallback: ICallback<Boolean?>?)

    ///////////////////save/////////////////
    fun save(key: K, value: E) {
        save(key, value, null)
    }

    abstract fun save(key: K, value: E?, saveSuccessCallback: ICallback<Boolean?>?)
    fun saveAll(map: HashMap<K, E>?) {
        saveAll(map, null)
    }

    abstract fun saveAll(map: HashMap<K, E>?, saveSuccessCountCallback: ICallback<Int?>?) //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
}