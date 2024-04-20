package com.au.module_android.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.au.module_android.Globals
import com.au.module_android.utils.ALog
import com.au.module_android.utils.asOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object AppDataStore {
    /**
     * @author au
     * @date :2023/11/7 10:24
     * @description:
     */
    private const val DATA_STORE_NAME = "dataStore" //对应最终件:/data/data/xxxx/files/datastore/dataStore.preferences_pb

    val Context.dataStore by preferencesDataStore(
        name = DATA_STORE_NAME,//指定名称
//    produceMigrations = {context ->  //指定要恢复的sp文件，无需恢复可不写
//        listOf(SharedPreferencesMigration(context, SP_PREFERENCES_NAME))
//    }
    )

    suspend inline fun <reified T> containsKey(key:String) : Boolean{
        val prefKey = when (T::class.java) {
            Int::class.java -> intPreferencesKey(key)
            Long::class.java -> longPreferencesKey(key)
            Double::class.java -> doublePreferencesKey(key)
            Float::class.java -> floatPreferencesKey(key)
            Boolean::class.java -> booleanPreferencesKey(key)
            String::class.java -> stringPreferencesKey(key)
            Set::class.java -> stringSetPreferencesKey(key)
            else -> {
                throw IllegalArgumentException("This type can be removed from DataStore")
            }
        }
        val t = Globals.app.dataStore.data.map {
            it.asMap().forEach { (t, u) ->
                ALog.t("allData: $t -> $u")
            }
            it.contains(prefKey)
        }.first()
        return t
    }

    fun clear() {
        Globals.mainScope.launch { Globals.app.dataStore.edit {
            ALog.t("clear!")
            it.clear()
        } }
    }

    fun save(key:String, value: Any) {
        Globals.mainScope.launch {
            ALog.t("save $key <to> $value")
            saveSuspend(key, value)
        }
    }

    fun save(vararg pair:Pair<String, Any>) {
        Globals.mainScope.launch {
            pair.forEach {
                saveSuspend(it.first, it.second)
            }
        }
    }

    inline fun <reified T> remove(key:String) {
        Globals.mainScope.launch {
            removeSuspend<T>(key)
        }
    }

    suspend inline fun <reified T> removeSuspend(key:String) : T?{
        var ret : T? = null
        Globals.app.dataStore.edit { setting ->
            ret = when (T::class.java) {
                Int::class.java -> setting.remove(intPreferencesKey(key)).asOrNull()
                Long::class.java -> setting.remove(longPreferencesKey(key)).asOrNull()
                Double::class.java -> setting.remove(doublePreferencesKey(key)).asOrNull()
                Float::class.java -> setting.remove(floatPreferencesKey(key)).asOrNull()
                Boolean::class.java -> setting.remove(booleanPreferencesKey(key)).asOrNull()
                String::class.java -> setting.remove(stringPreferencesKey(key)).asOrNull()
                Set::class.java -> setting.remove(stringSetPreferencesKey(key)).asOrNull() //later: 这里不做二次检查了。默认就认为是stringSet
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
    suspend fun saveSuspend(key:String, value:Any) {
        ALog.t("1save suspend $key <to> $value")
        Globals.app.dataStore.edit { setting ->
            ALog.t("2save suspend $key <to> $value")
            when (value) {
                is Int -> setting[intPreferencesKey(key)] = value
                is Long -> setting[longPreferencesKey(key)] = value
                is Double -> setting[doublePreferencesKey(key)] = value
                is Float -> setting[floatPreferencesKey(key)] = value
                is Boolean -> setting[booleanPreferencesKey(key)] = value
                is String -> setting[stringPreferencesKey(key)] = value
                is Set<*> -> {
                    val componentType = value::class.java.componentType!!
                    @Suppress("UNCHECKED_CAST") // Checked by reflection.
                    when {
                        String::class.java.isAssignableFrom(componentType) -> {
                            Globals.app.dataStore.edit { preferences ->
                                preferences[stringSetPreferencesKey(key)] = value as Set<String>
                            }
                        }
                    }
                }
                else -> {
                    throw IllegalArgumentException("This type can be saved into DataStore")
                }
            }
        }
    }

    /**
     * 获取数据
     * */
    suspend inline fun < reified T : Any> read(key: String, defaultValue:T): T {
        return  when (T::class) {
            Int::class -> {
                Globals.app.dataStore.data.map { setting ->
                    setting[intPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }
            Long::class -> {
                Globals.app.dataStore.data.map { setting ->
                    setting[longPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }
            Double::class -> {
                Globals.app.dataStore.data.map { setting ->
                    setting[doublePreferencesKey(key)] ?:defaultValue
                }.first() as T
            }
            Float::class -> {
                Globals.app.dataStore.data.map { setting ->
                    setting[floatPreferencesKey(key)] ?:defaultValue
                }.first() as T
            }
            Boolean::class -> {
                Globals.app.dataStore.data.map { setting ->
                    setting[booleanPreferencesKey(key)]?:defaultValue
                }.first() as T
            }
            String::class -> {
                Globals.app.dataStore.data.map { setting ->
                    setting[stringPreferencesKey(key)] ?: defaultValue
                }.first() as T
            }
            else -> {
                throw IllegalArgumentException("This type can be get into DataStore")
            }
        }
    }
}

