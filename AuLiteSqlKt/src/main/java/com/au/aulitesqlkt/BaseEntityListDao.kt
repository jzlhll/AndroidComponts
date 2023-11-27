package com.au.aulitesqlkt

import com.au.aulitesql.Entity
import com.au.aulitesql.actions.ICallback

abstract class BaseEntityListDao<E : Entity?>(val entityClass: Class<E>) {
    ////////////load////////////////
    fun loadAllFilter(fieldName: String?, value: Any?) {
        loadAllFilter(fieldName, value, null)
    }

    abstract fun loadAllFilter(fieldName: String?, value: Any?, callback: ICallback<List<E>?>?)
    fun loadAll() {
        loadAll(null)
    }

    abstract fun loadAll(callback: ICallback<List<E>?>?)
    fun loadAll(
        fieldName: String, value: Any?,
        groupBy: String?, having: String?, orderBy: String?
    ) {
        loadAll(fieldName, value, groupBy, having, orderBy, null)
    }

    abstract fun loadAll(
        fieldName: String, value: Any?,
        groupBy: String?, having: String?, orderBy: String?, callback: ICallback<List<E>?>?
    )

    fun loadAll(
        selections: String, selectionArgs: Array<String?>,
        groupBy: String?, having: String?, orderBy: String?
    ) {
        loadAll(selections, selectionArgs, groupBy, having, orderBy, null)
    }

    abstract fun loadAll(
        selections: String, selectionArgs: Array<String?>,
        groupBy: String?, having: String?, orderBy: String?, callback: ICallback<List<E>?>?
    )

    fun rawLoadAll(sql: String?, selectionArgs: Array<String?>?) {
        rawLoadAll(sql, selectionArgs, null)
    }

    abstract fun rawLoadAll(sql: String?, selectionArgs: Array<String?>?, callback: ICallback<List<E>?>?)

    //////////////////delete////////////////
    fun deleteAll(dataList: List<E>?) {
        deleteAll(dataList, null)
    }

    abstract fun deleteAll(dataList: List<E>?, deleteCountCallback: ICallback<Int?>?)
    fun delete(instance: E) {
        delete(instance, null)
    }

    abstract fun delete(instance: E, deleteSuccessCallback: ICallback<Boolean?>?)
    abstract fun clear(clearSuccessCallback: ICallback<Boolean?>?)

    ///////////////////save/////////////////
    fun save(instance: E) {
        save(instance, null)
    }

    abstract fun save(instance: E, saveSuccessCallback: ICallback<Boolean?>?)
    fun saveAll(dataList: List<E>?) {
        saveAll(dataList, null)
    }

    abstract fun saveAll(dataList: List<E>?, saveSuccessCountCallback: ICallback<Int?>?) //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
    //////////////////////////////////
}