package com.weacsoft.migration.schema;

import com.weacsoft.migration.compiler.MySqlCompiler;
import com.weacsoft.migration.exception.SQLRuntimeException;
import com.weacsoft.migration.model.Blueprint;
import com.weacsoft.migration.model.Migration;
import com.weacsoft.migration.runner.MigrationRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

//SQL执行器
public class SQLSchema extends BaseSchema {
    public Connection connection;
    public MySqlCompiler compiler;

    public SQLSchema() {
    }

    public SQLSchema(Connection connection, MySqlCompiler compiler) {
        this.connection = connection;
        this.compiler = compiler;
    }

    @Override
    public void create(String table, Consumer<Blueprint> callback) {
        Blueprint blueprint = new Blueprint();
        callback.accept(blueprint);
        executeSql(compiler.compileCreate(table, blueprint.columns));
    }

    @Override
    public void dropIfExists(String table) {
        executeSql(compiler.compileDrop(table));
    }

    /**
     * 具体执行SQL
     *
     * @param sql
     */
    private void executeSql(String sql, Object... params) {
        try (Connection connection = this.connection;
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.execute(sql);
        } catch (Exception e) {
            throw new SQLRuntimeException("执行迁移SQL失败: " + sql, e);
        }
    }

    /**
     * 具体执行SQL
     *
     * @param sql
     */
    private void executeSql(String sql, Collection<Object> columns) {
        executeSql(sql, columns.toArray());
    }

    //默认迁移表名
    public static String MIGRATIONS_TABLE = "migrations";
    //迁移表
    private final Migration migration = new Migration() {
        @Override
        public void up() {
            schema.create(MIGRATIONS_TABLE, columnGenerator -> {
                //创建id
                columnGenerator.id();
                //创建当前执行的迁移
                columnGenerator.string("migration");
                //创建当前执行的第几步
                columnGenerator.integer("batch");
            });
        }

        @Override
        public void down() {
            schema.dropIfExists(MIGRATIONS_TABLE);
        }
    };

    /**
     * 迁移前做什么
     */
    public boolean upStart() {
        List<Map<String, Object>> list = queryList(compiler.showTable());
        AtomicBoolean hasMigrationTable = new AtomicBoolean(false);
        list.forEach(stringObjectMap -> {
            stringObjectMap.forEach((key, value) -> {
                if (value.equals(MIGRATIONS_TABLE)) {
                    hasMigrationTable.set(true);
                }
            });
        });
        if (!hasMigrationTable.get()) {
            //不存在则执行迁移
            migration.schema = this;
            migration.up();
        }
        //获得最后一个数据
        column = getLast();
        return true;
    }

    private MigrationRunner.Column column;

    /**
     * 迁移后做什么
     */
    public void upFinish() {
    }

    /**
     * 回滚后做什么
     */
    public void downFinish() {
        MigrationRunner.Column column = getLast();
        if (column == null) {
            migration.schema = this;
            migration.down();
        }
    }

    public void upFinishOne(String migrator, int batch) {
        migrator = migrator.replace("'", "\\'");
        //插入迁移
        executeSql("INSERT INTO " + MIGRATIONS_TABLE + "(migration,batch) VALUES(?,?)", Arrays.asList(migrator, batch));
    }


    public void downFinishOne(String migrator, int batch) {
        migrator = migrator.replace("'", "\\'");
        //删除迁移
        executeSql("DELETE FROM " + MIGRATIONS_TABLE + " WHERE migration = ? AND batch = ?", Arrays.asList(migrator, batch));
    }


    /**
     * 获得最后一条数据
     *
     * @return
     */
    public MigrationRunner.Column getLast() {

        List<Map<String, Object>> list = queryList(compiler.getLastSql());
        if (!list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            MigrationRunner.Column column = new MigrationRunner.Column();
            column.id = String.valueOf(map.get("id"));
            column.migration = String.valueOf(map.get("migration"));
            column.batch = (Integer) map.get("batch");
            return column;
        }
        return null;
    }

    /**
     * 获得所有数据库的数据
     *
     * @return
     */
    public List<MigrationRunner.Column> getExistsList() {
        List<MigrationRunner.Column> columns = new ArrayList<>();
        List<Map<String, Object>> list = queryList(compiler.getExistsListSql());
        if (!list.isEmpty()) {
            for (Map<String, Object> map : list) {
                MigrationRunner.Column column = new MigrationRunner.Column();
                column.id = String.valueOf(map.get("id"));
                column.migration = String.valueOf(map.get("migration"));
                column.batch = (Integer) map.get("batch");
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * 获得某一步所有的操作
     *
     * @param batch
     * @return
     */
    public List<String> getBatchList(int batch) {
        List<String> batchList = new ArrayList<>();
        List<Map<String, Object>> list = queryList(compiler.getBatchListSql(batch));
        for (Map<String, Object> map : list) {
            batchList.add(String.valueOf(map.get("migration")));
        }
        return batchList;
    }

    @Override
    public List<String> getAppendMigratorList(List<String> migratorList) {
        //如果不是空的，则重新计算migratorList要生成的数量
        if (column != null) {
            List<String> appendMigratorList = new ArrayList<>();
            //新的一轮生成
            column.batch++;
            if (strictMode()) {
                //严格模式，比较所有可能的类型
                List<MigrationRunner.Column> existsList = getExistsList();
                for (MigrationRunner.Column exists : existsList) {
                    //移除掉已经执行过的
                    migratorList.remove(exists.migration);
                }
                appendMigratorList.addAll(migratorList);
            } else {
                //找到最后一个，然后往下扫
                boolean last = false;
                for (String migrator : migratorList) {
                    if (last) {
                        //如果是真，说明已经超过组后一个，则把这些都加到要加进去的
                        appendMigratorList.add(migrator);
                    } else {
                        //否则判断是否到最后一个
                        if (migrator.equals(column.migration)) {
                            last = true;
                        }
                    }
                }
            }
            return appendMigratorList;
        } else {
            return migratorList;
        }
    }

    @Override
    public List<String> getRemoveMigratorList(List<String> migratorList) {
        //如果不是空的，则重新计算migratorList要生成的数量
        List<String> removeMigratorList = new ArrayList<>();
        if (column != null) {
            //获得要删掉的
            List<String> removeName = getBatchList(column.batch);
            for (String name : removeName) {
                //判断名字一样就加入
                for (String migrator : migratorList) {
                    if (migrator.equals(name)) {
                        removeMigratorList.add(migrator);
                    }
                }
            }
        }
        return removeMigratorList;
    }

    public boolean strictMode = true;

    /**
     * 严格模式，是否比较所有迁移
     *
     * @return
     */
    public boolean strictMode() {
        return strictMode;
    }

    @Override
    public int getBatch() {
        return column.batch;
    }

    //执行查询SQL，组装成List<Map<String, Object>>形式
    private List<Map<String, Object>> queryList(String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection connection = this.connection;
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
            return list;
        } catch (Exception e) {
            throw new SQLRuntimeException("执行查询SQL失败: " + sql, e);
        }
    }
}
