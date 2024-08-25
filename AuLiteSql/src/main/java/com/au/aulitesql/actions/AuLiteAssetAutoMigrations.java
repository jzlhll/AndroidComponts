package com.au.aulitesql.actions;

import static com.au.aulitesql.actions.SqlUtils.SqlString.sqlDeleteTable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.au.aulitesql.AuLiteSql;
import com.au.aulitesql.Entity;
import com.au.aulitesql.IManualMigration;
import com.au.aulitesql.info.Triple;
import com.au.aulitesql.info.NamesPair;
import com.au.aulitesql.info.Pair;
import com.au.aulitesql.info.TableInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AuLiteAssetAutoMigrations {
    public Map<Class<? extends Entity>, IManualMigration> tableInnersMigrations = null;
    public Map<NamesPair, IManualMigration> tablesMigrations = null;

    public void onVersionChange(@NonNull TableCreators.CreatorInfo newCreators, @NonNull SQLiteDatabase db) {
        List<String> oldTabs = SqlUtils.allTableNames(db);
        List<String> newTabs = newCreators.allTableNames();

        //1. 一张表都没了。全部删掉。
        if (newTabs.size() == 0) {
            for (String oldTab : oldTabs) {
                db.execSQL(sqlDeleteTable(oldTab));
            }
            return;
        }

        db.beginTransaction();
        //2. 对比
        Triple<List<String>, List<String>, List<String>>
                triple = Triple.compare(newTabs, oldTabs, String::equals);

        //3. 新独立表，先进行创建
        for (String newTab : triple.p2) {
            db.execSQL(Objects.requireNonNull(newCreators.getByTableName(newTab)).sql);
        }

        //4. 删除老表。如果有迁移函数，则允许迁移到新表，即表间迁移。 later：暂不支持复杂的比如某个表就是要往老表的迁移，而2个老表又同名。
        for (String oldTab : triple.p3) {
            NamesPair foundNamesParKey = null;
            if (tablesMigrations != null && tablesMigrations.size() > 0) {
                for (NamesPair np : tablesMigrations.keySet()) {
                    if (np.oldName.equals(oldTab)) {
                        foundNamesParKey = np;
                        break;
                    }
                }
            }

            boolean needDeletedOldTab = true; //使用数组1，来做final传递。

            if (foundNamesParKey != null) {
                IManualMigration mig = tablesMigrations.get(foundNamesParKey);
                Class<? extends Entity> newClass = foundNamesParKey.newNameClass;
                TableInfo newTableInfo = newCreators.getByTableClass(newClass);

                //取出新的类对应的sql
                Cursor cursor = db.rawQuery(oldTab, null);
                Pair<ContentValues, Boolean> newTabDatas = mig.onTableInnerMigrate(newClass, cursor);
                cursor.close();
                db.insert(newTableInfo.name, null, newTabDatas.p1);
                if (newTabDatas.p2) { //允许保留老的表
                    needDeletedOldTab = false;
                }
            }  //找不到迁移函数中对应的oldTableName，则直接删除老表，不做迁移。

            if(needDeletedOldTab) db.execSQL(sqlDeleteTable(oldTab));
        }

        //5. 同名表，则先进行table校验。是否需要进行迁移。然后，进行表内迁移。
        //默认实现则，字段不对的，直接不保留。
        for (String tab : triple.p1) {
            Class<? extends Entity> foundClass = null;
            IManualMigration foundMig = null;
            if (tableInnersMigrations != null && tableInnersMigrations.size() > 0) {
                for (Class<? extends Entity> clazz : tableInnersMigrations.keySet()) {
                    if (isSame(clazz, tab)) {
                        foundClass = clazz;
                        foundMig = tableInnersMigrations.get(foundClass);
                        break;
                    }
                }
            }

            if (foundMig != null) { //找到了迁移函数, 则交给迁移函数去干。即使没变，任何字段，也有可能进行迁移。所以都交给迁移函数处理。
                //重命名老表
                var prefix = "_temp_" + (int)(Math.random() * 1000);
                db.execSQL(SqlUtils.SqlString.sqlRenameTable(tab, prefix));
                //创建表
                db.execSQL(Objects.requireNonNull(newCreators.getByTableName(tab)).sql);

                Cursor cursor = db.query(prefix + tab, null, null, null, null, null, null);
                Pair<ContentValues, Boolean> newTabDatas = foundMig.onTableInnerMigrate(foundClass, cursor);
                cursor.close();
                //写入所有数据
                db.insert(tab, null, newTabDatas.p1);
            } else {
                //没有迁移函数，则相同字段和字段类型保留。其他删除。
                //todo 日志表，其实可以更加详细具体每一个字段的对比。暂时先有变化就清理掉。要求你自行传入迁移函数先。
                var newCreateSql = Objects.requireNonNull(newCreators.getByTableName(tab)).sql;
                var oldCreateSqlPair = SqlUtils.queryCreateSqlLog(db, tab);

                if (oldCreateSqlPair == null || !Objects.equals(newCreateSql, oldCreateSqlPair.p1)) {
                    //删掉，重建 todo 可以更好。
                    db.execSQL(SqlUtils.SqlString.sqlDeleteTable(tab));
                    db.execSQL(newCreateSql);
                }
            }
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private static boolean isSame(Class<? extends Entity> clazz, String name) {
        return AuLiteSql.tableNameFromClazz(clazz).equals(name);
    }
}
