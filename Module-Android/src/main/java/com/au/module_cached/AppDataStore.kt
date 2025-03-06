package com.au.module_cached

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * 不支持Set，List。请自行使用Gson进行转换存储。
 * 支持如下：
 *             Int::class.java
 *             Long::class.java
 *             Double::class.java
 *             Float::class.java
 *             Boolean::class.java
 *             String::class.java
 *             ByteArray::class.java
 */
object AppDataStore {
    class OnceDataStore(val context:Context, dataStoreName:String) {
        private val Context.mDataStore by preferencesDataStore(name = dataStoreName)
        val dataStore = context.mDataStore
    }

    /**
     * @author au
     * @date :2023/11/7 10:24
     * @description:
     */
    private const val DATA_STORE_NAME = "globalDataStore" //对应最终件:/data/data/xxxx/files/datastore/globalDataStore.preferences_pb

    val Context.globalDataStore by preferencesDataStore(
        name = DATA_STORE_NAME,//指定名称
//    produceMigrations = {context ->  //指定要恢复的sp文件，无需恢复可不写
//        listOf(SharedPreferencesMigration(context, SP_PREFERENCES_NAME))
//    }
    )

    fun onceDataStore(context: Context, dataStoreName:String) = OnceDataStore(context, dataStoreName).dataStore

    inline fun <reified T> containsKeyBlocked(key:String,
                                              dataStore: DataStore<Preferences> = Globals.app.globalDataStore) : Boolean{
        val ret = runBlocking {
            val prefKey = when (T::class.java) {
                Int::class.java -> intPreferencesKey(key)
                Long::class.java -> longPreferencesKey(key)
                Double::class.java -> doublePreferencesKey(key)
                Float::class.java -> floatPreferencesKey(key)
                Boolean::class.java -> booleanPreferencesKey(key)
                String::class.java -> stringPreferencesKey(key)
                ByteArray::class.java -> byteArrayPreferencesKey(key)
                else -> {
                    throw IllegalArgumentException("This type can be removed from DataStore")
                }
            }
            val t = dataStore.data.map {
                it.contains(prefKey)
            }.first()

            return@runBlocking t
        }

        return ret
    }

    suspend inline fun <reified T> containsKey(key:String,
                                               dataStore: DataStore<Preferences> = Globals.app.globalDataStore) : Boolean{
        val prefKey = when (T::class.java) {
            Int::class.java -> intPreferencesKey(key)
            Long::class.java -> longPreferencesKey(key)
            Double::class.java -> doublePreferencesKey(key)
            Float::class.java -> floatPreferencesKey(key)
            Boolean::class.java -> booleanPreferencesKey(key)
            String::class.java -> stringPreferencesKey(key)
            ByteArray::class.java -> byteArrayPreferencesKey(key)
            else -> {
                throw IllegalArgumentException("This type can be removed from DataStore")
            }
        }
        val t = dataStore.data.map { it.contains(prefKey) }.first()
        return t
    }

    fun clear(dataStore: DataStore<Preferences> = Globals.app.globalDataStore) {
        runBlocking {dataStore.edit { it.clear() } }
    }

    fun save(key:String, value: Any, dataStore: DataStore<Preferences> = Globals.app.globalDataStore,) {
        runBlocking {
            saveSuspend(key, value, dataStore)
        }
    }

    fun save(vararg pair:Pair<String, Any>, dataStore: DataStore<Preferences> = Globals.app.globalDataStore) {
        runBlocking {
            pair.forEach {
                saveSuspend(it.first, it.second, dataStore)
            }
        }
    }

    inline fun <reified T> remove(key:String) {
        runBlocking {
            removeSuspend<T>(key)
        }
    }

    suspend inline fun <reified T> removeSuspend(key:String,
                                                 dataStore: DataStore<Preferences> = Globals.app.globalDataStore) : T?{
        var ret : T? = null
        dataStore.edit { setting ->
            ret = when (T::class.java) {
                Int::class.java -> setting.remove(intPreferencesKey(key)).asOrNull()
                Long::class.java -> setting.remove(longPreferencesKey(key)).asOrNull()
                Double::class.java -> setting.remove(doublePreferencesKey(key)).asOrNull()
                Float::class.java -> setting.remove(floatPreferencesKey(key)).asOrNull()
                Boolean::class.java -> setting.remove(booleanPreferencesKey(key)).asOrNull()
                String::class.java -> setting.remove(stringPreferencesKey(key)).asOrNull()
                ByteArray::class.java -> setting.remove(byteArrayPreferencesKey(key)).asOrNull()
                else -> {
                    throw IllegalArgumentException("This type can be removed from DataStore")
                }
            }
        }
        return ret
    }

    /**
     * 因为我们用于保存，不应该使用lifeCycleScope来发起。有可能无法保存成功。应该使用全局scope。
     */
    @Deprecated("不建议直接使用，因为可能协程被取消，除非你明白你的scope一定保存成功")
    private suspend fun saveSuspend(key:String, value:Any,
                                    dataStore: DataStore<Preferences> = Globals.app.globalDataStore) {
        dataStore.edit { setting ->
            when (value) {
                is Int -> setting[intPreferencesKey(key)] = value
                is Long -> setting[longPreferencesKey(key)] = value
                is Double -> setting[doublePreferencesKey(key)] = value
                is Float -> setting[floatPreferencesKey(key)] = value
                is Boolean -> setting[booleanPreferencesKey(key)] = value
                is String -> setting[stringPreferencesKey(key)] = value
                is ByteArray -> setting[byteArrayPreferencesKey(key)] = value
                else -> {
                    throw IllegalArgumentException("This type can be saved into DataStore")
                }
            }
        }
    }

    inline fun < reified T : Any> readBlocked(key:String, defaultValue:T,
                                              dataStore: DataStore<Preferences> = Globals.app.globalDataStore) : T {
        val r = runBlocking {
            read(key, defaultValue, dataStore)
        }
        return r
    }

    /**
     * 获取数据
     * */
    suspend inline fun < reified T : Any> read(key: String, defaultValue:T,
                                               dataStore: DataStore<Preferences> = Globals.app.globalDataStore): T {
        return when (T::class) {
            Int::class -> {
                dataStore.data.map { setting ->
                    setting[intPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }
            Long::class -> {
                dataStore.data.map { setting ->
                    setting[longPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }
            Double::class -> {
                dataStore.data.map { setting ->
                    setting[doublePreferencesKey(key)] ?:defaultValue
                }.first() as T
            }
            Float::class -> {
                dataStore.data.map { setting ->
                    setting[floatPreferencesKey(key)] ?:defaultValue
                }.first() as T
            }
            Boolean::class -> {
                dataStore.data.map { setting ->
                    setting[booleanPreferencesKey(key)]?:defaultValue
                }.first() as T
            }
            String::class -> {
                dataStore.data.map { setting ->
                    setting[stringPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }

            ByteArray::class -> {
                dataStore.data.map { setting ->
                    setting[byteArrayPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }
            else -> {
                throw IllegalArgumentException("This type can be get into DataStore")
            }
        }
    }
}