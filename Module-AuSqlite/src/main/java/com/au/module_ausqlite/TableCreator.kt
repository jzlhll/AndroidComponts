package com.au.module_ausqlite

import android.provider.BaseColumns
import androidx.annotation.WorkerThread
import com.au.module_ausqlite.annotation.AuAltName
import com.au.module_ausqlite.annotation.AuIgnore
import com.au.module_ausqlite.dmeo.DemoEntityTable
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * @author allan.jiang
 * @date :2023/11/10 14:12
 * @description: 这个代码是用来在编译之前运行生成内置asset文件使用的数据库使用的。
 *
 * warn: 请传入buildConfig.DEBUG 。如果传入了true就是debug，将会输出一个asset用于给你去集成。
 * 如果是release，传入的false, 则会进行check并使用。
 *
 * 如果，会
 * 则会使用它。但是代码对不上，则需要自行负责。
 */
class TableCreator(private val entityTables:List<Class<out EntityTable>>) {
    //暂不支持其他。java.lang.Boolean....等等封装类型
    private var map = hashMapOf(
        "boolean" to "BOOLEAN",
        "byte" to "INTEGER",
        "int" to "INTEGER",
        "short" to "INTEGER",
        "long" to "INTEGER",
        "float" to "FLOAT",
        "double" to "DOUBLE",
        "char" to "INTEGER",
        "String" to "TEXT")

    private val primaryKey = "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,\n"
    private val defaultTextItemTemplate = "%s TEXT DEFAULT \"%s\"" //字段  defaultValue
    private val notNullTextItemTemplate = "%s TEXT NOT NULL" //字段
    private val textTemplate = "%s TEXT" //字段
    private val itemDefaultTemplate = "%s %s DEFAULT %s" //字段 类型 defaultValue
    private val itemTemplate = "%s %s" //字段 类型

    private val itemBoolTrueTemplate = "%s BOOLEAN DEFAULT TRUE" //字段
    private val split = ", \n"
    private val end = "\n)"

    class Output {
        val tableCreatesStrings = mutableListOf<String>()
        val tableNames = mutableListOf<String>()
    }

    @WorkerThread
    fun collect() : Output{
        val output = Output()
        for (tab in entityTables) {
            val dec = tab.getDeclaredConstructor()
            val one = excuteSqlCreateTab(dec.newInstance(), output)
            println("===========================")
            println(one)
            println("===========================")
        }

        return output
    }

    fun isFieldTransient(field: Field): Boolean {
        val modified = field.modifiers
        return modified and Modifier.TRANSIENT != 0
    }

    private fun iterAllFields(instance:EntityTable):List<Field> {
        val allFields = mutableListOf<Field>()
        var clazz:Class<in EntityTable>? = instance.javaClass
        while (clazz != null && clazz != EntityTable::class.java) {
            println("iterAllFields $clazz")
            val fields = clazz.declaredFields

            for (f in fields) {
                allFields.add(f)
            }

            clazz = clazz.superclass
        }
        return allFields
    }

    fun excuteSqlCreateTab(instance: EntityTable, out:Output) {
        val clazz = instance.javaClass
        //1. 替代tableName解析
        val tableName:String = if (clazz.isAnnotationPresent(AuAltName::class.java)) {
            val value = clazz.getAnnotation(AuAltName::class.java)?.value
            if (value.isNullOrEmpty()) {
                clazz.simpleName
            } else {
                value
            }
        } else {
            clazz.simpleName
        }

        out.tableNames.add(tableName)
        println("tableName $tableName")
        val sqlCreateTab = StringBuilder("CREATE TABLE IF NOT EXISTS $tableName (")
        println("sqlCreateTab $sqlCreateTab")

        sqlCreateTab.append(primaryKey)

        val allFields = iterAllFields(instance)
        val total = allFields.size
        for (i in 0 until total) {
            val field = allFields[i]

            if (isFieldTransient(field) || field.isAnnotationPresent(AuIgnore::class.java)) {
                continue
            } else {
                val type = field.type.simpleName
                if (!map.containsKey(type)) {
                    throw RuntimeException("请移除非基本类型的数据结构！$type")
                }

                val name = if (field.isAnnotationPresent(AuAltName::class.java)) {
                    val annotation: AuAltName? = field.getAnnotation(AuAltName::class.java)
                    val value = annotation?.value
                    if(value.isNullOrEmpty()) field.name else value
                } else {
                    field.name
                }

                if (!field.isAccessible) {
                    field.isAccessible = true
                }

                val itemStr = when (type) {
                    "long"-> {
                        val defaultValue = field.getLong(instance)
                        if (defaultValue == 0L) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "int"-> {
                        val defaultValue = field.getInt(instance)
                        if (defaultValue == 0) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "short"-> {
                        val defaultValue = field.getShort(instance)
                        if (defaultValue.toInt() == 0) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "byte"-> {
                        val defaultValue = field.getByte(instance)
                        if (defaultValue.toInt() == 0) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "char"-> {
                        val defaultValue = field.getChar(instance).code
                        if (defaultValue == 0) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "float"-> {
                        val defaultValue = field.getFloat(instance)
                        if (defaultValue == 0f) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "double" -> {
                        val defaultValue = field.getDouble(instance)
                        if (defaultValue == 0.0) {
                            String.format(itemTemplate, name, map[type])
                        } else {
                            String.format(itemDefaultTemplate, name, map[type], defaultValue)
                        }
                    }
                    "boolean" -> {
                        val defaultValue = field.getBoolean(instance)
                        if (defaultValue) {
                            String.format(itemBoolTrueTemplate, name)
                        } else {
                            String.format(itemTemplate, name, map[type])
                        }
                    }

                    "String"-> {
                        val defaultValue = field.get(instance)?.toString()
                        if (defaultValue == null) {
                            String.format(textTemplate, name)
                        } else if (defaultValue.isEmpty()) {
                            String.format(notNullTextItemTemplate, name)
                        } else {
                            String.format(defaultTextItemTemplate, name, defaultValue)
                        }
                    }
                    else -> TODO("不可能")
                }

                sqlCreateTab.append(itemStr)
                if (i < total - 1) {
                    sqlCreateTab.append(split)
                } else {
                    sqlCreateTab.append(end)
                }
            }
        }

        out.tableCreatesStrings.add(sqlCreateTab.toString())
    }
}

fun main() {
    TableCreator(listOf(DemoEntityTable::class.java)).collect()
}
