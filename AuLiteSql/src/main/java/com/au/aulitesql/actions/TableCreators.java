package com.au.aulitesql.actions;

import static com.au.aulitesql.AuLiteSql.tableNameFromClazz;
import static java.sql.DriverManager.println;

import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.au.aulitesql.Entity;
import com.au.aulitesql.annotation.AuIgnore;
import com.au.aulitesql.annotation.AuName;
import com.au.aulitesql.info.FieldInfo;
import com.au.aulitesql.info.TableInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于打印信息，收集使用。
 */
public final class TableCreators {
    public static final class CreatorInfo {
        public CreatorInfo(@NonNull List<Class<? extends Entity>> entityTables) {
            this.entityTables = entityTables;
        }

        public final transient List<Class<? extends Entity>> entityTables;

        @Nullable
        public TableInfo getByTableClass(@NonNull Class<? extends Entity> clazz) {
            String name = tableNameFromClazz(clazz);
            for (TableInfo tableInfo : tableInfoList) {
                if (tableInfo instanceof TableInfo && ((TableInfo) tableInfo).entityTable.equals(clazz)) {
                    return tableInfo;
                }
                if (tableInfo.name.equals(name)) {
                    return tableInfo;
                }
            }

            return null;
        }

        @NonNull
        public final ArrayList<TableInfo> tableInfoList = new ArrayList<>();

        public List<String> allTableNames() {
            return tableInfoList.stream().map(tableInfo -> tableInfo.name).collect(Collectors.toList());
        }

        @Nullable
        public TableInfo getByTableName(@NonNull String name) {
            for (TableInfo tableInfo : tableInfoList) {
                if (tableInfo.name.equals(name)) {
                    return tableInfo;
                }
            }
            return null;
        }

        /**
         * 保存成String
         * 一行名字，一行table sql创建语句。
         */
        public String saveToString() {
            var strs = saveToStrings();
            String enter = "\n";
            int enterLen = enter.length();
            boolean has = false;
            StringBuilder sb = new StringBuilder();
            for (String s : strs) {
                sb.append(s).append(enter);
                sb.append(s).append(enter);
                has = true;
            }

            return has ? sb.substring(0, sb.length() - enterLen) : sb.toString();
        }

        public String[] saveToStrings() {
            List<String> ret = new ArrayList<>();
            tableInfoList.sort((o1, o2) -> {
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

            for (TableInfo tableInfo : tableInfoList) {
                ret.add(tableInfo.name);
                ret.add(tableInfo.toSave());
            }

            return ret.toArray(new String[] {});
        }
    }

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

    public TableCreators(List<Class<? extends Entity>> entityTables) {
        creatorInfo = new CreatorInfo(entityTables);
    }

    @WorkerThread
    public CreatorInfo collect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<? extends Entity> tab : creatorInfo.entityTables) {
            Constructor<?> dec = tab.getDeclaredConstructor();
            TableInfo one = executeTableInfo((Entity) dec.newInstance());
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

    private static List<Field> iterAllFields(Entity instance) {
        return iterAllFields(instance.getClass());
    }

    private static List<Field> iterAllFields(Class<?> clazz) {
        ArrayList<Field> allFields = new ArrayList<>();

        while (clazz != null && clazz != Entity.class) {
            Field[] fields = clazz.getDeclaredFields();
            Collections.addAll(allFields, fields);
            clazz = clazz.getSuperclass();
        }
        return allFields;
    }

    private static List<Field> allFields(Class<?> clazz) {
        ArrayList<Field> allFields = new ArrayList<>();

        if (clazz != null && clazz != Entity.class) {
            Field[] fields = clazz.getDeclaredFields();
            Collections.addAll(allFields, fields);
        }
        return allFields;
    }

    public static TableInfo executeTableInfo(Entity instance) throws IllegalAccessException {
        TableInfo t = new TableInfo();

        Class<? extends Entity> clazz = instance.getClass();
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

                case "List", "ArrayList", "Map", "HashMap", "Set", "HashSet" -> {
                    fi.sql = String.format(textTemplate, fi.name);
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

    public static void collectPrint(List<Class<? extends Entity>> tables) {
        CreatorInfo creatorInfo = collect(tables);
        if (creatorInfo != null) {
            Log.w("AuLiteSql", "Please save to asset....to > auLiteSql/dbCreator.txt");
            Log.w("AuLiteSql", creatorInfo.saveToString());
            Log.w("AuLiteSql", "Please save to asset....end!");
        }
    }

    public static CreatorInfo collect(List<Class<? extends Entity>> tables) {
        try {
            return new TableCreators(tables).collect();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 帮你实现好该类的三大函数。
     */
    private static void printClassFunctions(Class<? extends Entity> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<?> dec = clazz.getDeclaredConstructor();
        var instance = (Entity) dec.newInstance();
        List<Field> allFields = allFields(clazz);
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

                case "List", "ArrayList", "Map", "HashMap", "Set", "HashSet" -> {
                    fi.sql = String.format(textTemplate, fi.name);
                }
                default -> throw new RuntimeException("不可能");
            }
        }
    }
}
