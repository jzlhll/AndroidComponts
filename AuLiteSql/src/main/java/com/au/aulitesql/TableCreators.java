package com.au.aulitesql;

import static java.sql.DriverManager.println;

import android.provider.BaseColumns;

import androidx.annotation.WorkerThread;

import com.au.aulitesql.annotation.AuName;
import com.au.aulitesql.annotation.AuIgnore;
import com.au.aulitesql.info.CreatorAssetInfo;
import com.au.aulitesql.info.CreatorInfo;
import com.au.aulitesql.info.FieldInfo;
import com.au.aulitesql.info.TableAssetInfo;
import com.au.aulitesql.info.TableInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TableCreators {
    //暂不支持其他。java.lang.Boolean....等等封装类型
    private static final HashMap<String, String> map = new HashMap<>();
    static {
        map.put("boolean" , "BOOLEAN");
        map.put("byte" , "INTEGER");
        map.put("int" , "INTEGER");
        map.put("short" , "INTEGER");
        map.put("long" , "INTEGER");
        map.put("float" , "FLOAT");
        map.put("double" , "DOUBLE");
        map.put("char" , "INTEGER");
        map.put("String" , "TEXT");
    }

    public static final String primaryKey =
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n";
    private static final String defaultTextItemTemplate =
            "%s TEXT DEFAULT \"%s\""; //字段  defaultValue
    private static final String notNullTextItemTemplate = "%s TEXT NOT NULL"; //字段
    private static final String textTemplate = "%s TEXT"; //字段
    private static final String itemDefaultTemplate = "%s %s DEFAULT %s"; //字段 类型 defaultValue
    private static final String itemTemplate = "%s %s"; //字段 类型

    private static final String itemBoolTrueTemplate = "%s BOOLEAN DEFAULT TRUE"; //字段
    private static final String split = ", \n";
    private static final String end = "\n)";

    private final CreatorInfo creatorInfo;

    public TableCreators(List<Class<? extends EntityTable>> entityTables) {
        creatorInfo = new CreatorInfo(entityTables);
    }

    @WorkerThread
    public CreatorAssetInfo collect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<? extends EntityTable> tab : creatorInfo.entityTables) {
            Constructor<?> dec = tab.getDeclaredConstructor();
            TableAssetInfo one = executeTableInfo((EntityTable) dec.newInstance());
            creatorInfo.tableInfoList.add(one);
        }
        creatorInfo.tableInfoList.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 != null && o2 == null) {
                return 1;
            }

            if (o1 == null) {
                return -1;
            }

            return o1.name.compareTo(o2.name);
        });
        return creatorInfo;
    }

    private static Boolean isFieldTransient(Field field) {
        int modified = field.getModifiers();
        return (modified & Modifier.TRANSIENT) != 0;
    }

    private static List<Field> iterAllFields(EntityTable instance) {
        ArrayList<Field> allFields = new ArrayList<>();

        Class<?> clazz = instance.getClass();
        while (clazz != null && clazz != EntityTable.class) {
            Field[] fields = clazz.getDeclaredFields();
            Collections.addAll(allFields, fields);
            clazz = clazz.getSuperclass();
        }
        return allFields;
    }

    public static String tableNameFromClazz(Class<? extends EntityTable> clazz) {
        var name = clazz.getSimpleName();
        //1. 替代tableName解析
        if (clazz.isAnnotationPresent(AuName.class)) {
            AuName annotation = clazz.getAnnotation(AuName.class);
            if (annotation != null) {
                String value = annotation.value();
                if (value != null && !value.isEmpty()) {
                    name = value;
                }
            }
        }
        return name;
    }

    public static TableInfo executeTableInfo(EntityTable instance) throws IllegalAccessException {
        TableInfo t = new TableInfo();

        Class<? extends EntityTable> clazz = instance.getClass();
        t.entityTable = clazz;
        t.className = clazz.getSimpleName();
        //1. 替代tableName解析
        t.name = tableNameFromClazz(clazz);

        StringBuilder sqlCreateTab = new StringBuilder("CREATE TABLE IF NOT EXISTS " + t.name + " (");
        println("sqlCreateTab " + sqlCreateTab);

        sqlCreateTab.append(primaryKey);

        List<Field> allFields = iterAllFields(instance);
        int total = allFields.size();

        for (int i = 0; i < total; i++) {
            Field field = allFields.get(i);

            if (isFieldTransient(field) || field.isAnnotationPresent(AuIgnore.class)) {
                continue;
            }

            String type = field.getType().getSimpleName();
            if (!map.containsKey(type)) {
                throw new RuntimeException("请移除非基本类型的数据结构！$type");
            }

            FieldInfo fi = new FieldInfo();
            fi.fieldName = field.getName();
            fi.dataType = SqlUtils.dataTypeToCursorDataType(type);

            if (field.isAnnotationPresent(AuName.class)) {
                AuName annotation = field.getAnnotation(AuName.class);
                if (annotation != null) {
                    String v = annotation.value();
                    if (v != null && !v.isEmpty()) {
                        fi.name = v;
                    }
                }
            }
            if (fi.name == null) {
                fi.name = field.getName();
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            switch (type) {
                case "long" -> {
                    long defaultValue = field.getLong(instance);
                    if (defaultValue == 0L) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "int" -> {
                    int defaultValue = field.getInt(instance);
                    if (defaultValue == 0) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "short" -> {
                    short defaultValue = field.getShort(instance);
                    if (defaultValue == 0) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "byte" -> {
                    byte defaultValue = field.getByte(instance);
                    if (defaultValue == 0) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "char" -> {
                    char defaultValue = field.getChar(instance);
                    if (defaultValue == 0) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "float" -> {
                    float defaultValue = field.getFloat(instance);
                    if (defaultValue == 0f) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "double" -> {
                    double defaultValue = field.getDouble(instance);
                    if (defaultValue == 0f) {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    } else {
                        fi.sql = String.format(itemDefaultTemplate, fi.name, map.get(type), defaultValue);
                    }
                }
                case "boolean" -> {
                    boolean defaultValue = field.getBoolean(instance);
                    if (defaultValue) {
                        fi.sql = String.format(itemBoolTrueTemplate, fi.name);
                    } else {
                        fi.sql = String.format(itemTemplate, fi.name, map.get(type));
                    }
                }
                case "String" -> {
                    Object o = field.get(instance);
                    String defaultValue = (o == null) ? null : o.toString();
                    if (defaultValue == null) {
                        fi.sql = String.format(textTemplate, fi.name);
                    } else if (defaultValue.isEmpty()) {
                        fi.sql = String.format(notNullTextItemTemplate, fi.name);
                    } else {
                        fi.sql = String.format(defaultTextItemTemplate, fi.name, defaultValue);
                    }
                }
                default -> throw new RuntimeException("不可能");
            }

            sqlCreateTab.append(fi.sql);
            if (i < total - 1) {
                sqlCreateTab.append(split);
            } else {
                sqlCreateTab.append(end);
            }

            t.fieldInfoList.add(fi);
        }

        t.sql = sqlCreateTab.toString();
        return t;
    }
}
