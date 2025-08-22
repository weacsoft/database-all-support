package com.weacsoft.migration.schema;

//import com.weacsoft.migration.compiler.MySqlCompiler;
//import com.weacsoft.migration.model.Blueprint;
//import com.weacsoft.migration.model.Migration;
//import com.weacsoft.migration.runner.MigrationRunner;
//import gaarason.database.eloquent.Model;
//import gaarason.database.exception.SQLRuntimeException;
//
//import java.sql.Connection;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.Consumer;

//SQL执行器
public class DatabaseAllSQLSchema extends BaseSchema {
//    public Model model;
//    public MySqlCompiler compiler;
//
//    public DatabaseAllSQLSchema() {
//    }
//
//    public DatabaseAllSQLSchema(Model model, MySqlCompiler compiler) {
//        this.model = model;
//        this.compiler = compiler;
//    }
//
//    @Override
//    public void create(String table, Consumer<Blueprint> callback) {
//        Blueprint blueprint = new Blueprint();
//        callback.accept(blueprint);
//        executeSql(compiler.compileCreate(table, blueprint.columns));
//    }
//
//    @Override
//    public void dropIfExists(String table) {
//        executeSql(compiler.compileDrop(table));
//    }
//
//    /**
//     * 具体执行SQL
//     *
//     * @param sql
//     */
//    private void executeSql(String sql) {
//        try (Connection connection = model.getGaarasonDataSource().getConnection();
//             Statement statement = connection.createStatement()) {
//            statement.execute(sql);
//        } catch (Exception e) {
//            throw new SQLRuntimeException("执行迁移SQL失败: " + sql, e);
//        }
//    }
//
//    //默认迁移表名
//    public static String MIGRATIONS_TABLE = "migrations";
//    //迁移表
//    private final Migration migration = new Migration() {
//        @Override
//        public void up() {
//            schema.create(MIGRATIONS_TABLE, columnGenerator -> {
//                //创建id
//                columnGenerator.id();
//                //创建当前执行的迁移
//                columnGenerator.string("migration");
//                //创建当前执行的第几步
//                columnGenerator.integer("batch");
//            });
//        }
//
//        @Override
//        public void down() {
//            schema.dropIfExists(MIGRATIONS_TABLE);
//        }
//    };
//
//    /**
//     * 迁移前做什么
//     */
//    public boolean upStart() {
//        List<Map<String, Object>> list = model.newQuery().queryList(compiler.showTable()).toMapList();
//        AtomicBoolean hasMigrationTable = new AtomicBoolean(false);
//        list.forEach(stringObjectMap -> {
//            stringObjectMap.forEach((key, value) -> {
//                if (value.equals(MIGRATIONS_TABLE)) {
//                    hasMigrationTable.set(true);
//                }
//            });
//        });
//        if (!hasMigrationTable.get()) {
//            //不存在则执行迁移
//            migration.schema = this;
//            migration.up();
//        }
//        //获得最后一个数据
//        column = getLast();
//        return true;
//    }
//
//    private MigrationRunner.Column column;
//
//    /**
//     * 迁移后做什么
//     */
//    public void upFinish() {
//    }
//
//    /**
//     * 回滚后做什么
//     */
//    public void downFinish() {
//        MigrationRunner.Column column = getLast();
//        if (column == null) {
//            migration.schema = this;
//            migration.down();
//        }
//    }
//
//    public void upFinishOne(String migrator, int batch) {
//        migrator = migrator.replace("'", "\\'");
//        //插入迁移
//        model.newQuery().execute("INSERT INTO " + MIGRATIONS_TABLE + "(migration,batch) VALUES(?,?)", Arrays.asList(migrator, batch));
//    }
//
//
//    public void downFinishOne(String migrator, int batch) {
//        migrator = migrator.replace("'", "\\'");
//        //删除迁移
//        model.newQuery().execute("DELETE FROM " + MIGRATIONS_TABLE + " WHERE migration = ? AND batch = ?", Arrays.asList(migrator, batch));
//    }
//
//
//    /**
//     * 获得最后一条数据
//     *
//     * @return
//     */
//    public MigrationRunner.Column getLast() {
//
//        List<Map<String, Object>> list = model.newQuery().queryList(compiler.getLastSql()).toMapList();
//        if (!list.isEmpty()) {
//            Map<String, Object> map = list.get(0);
//            MigrationRunner.Column column = new MigrationRunner.Column();
//            column.id = String.valueOf(map.get("id"));
//            column.migration = String.valueOf(map.get("migration"));
//            column.batch = (Integer) map.get("batch");
//            return column;
//        }
//        return null;
//    }
//
//    /**
//     * 获得所有数据库的数据
//     *
//     * @return
//     */
//    public List<MigrationRunner.Column> getExistsList() {
//        List<MigrationRunner.Column> columns = new ArrayList<>();
//        List<Map<String, Object>> list = model.newQuery().queryList(compiler.getExistsListSql()).toMapList();
//        if (!list.isEmpty()) {
//            for (Map<String, Object> map : list) {
//                MigrationRunner.Column column = new MigrationRunner.Column();
//                column.id = String.valueOf(map.get("id"));
//                column.migration = String.valueOf(map.get("migration"));
//                column.batch = (Integer) map.get("batch");
//                columns.add(column);
//            }
//        }
//        return columns;
//    }
//
//    /**
//     * 获得某一步所有的操作
//     * @param batch
//     * @return
//     */
//    public List<String> getBatchList(int batch) {
//        List<String> batchList = new ArrayList<>();
//        List<Map<String, Object>> list = model.newQuery().queryList(compiler.getBatchListSql(batch)).toMapList();
//        for (Map<String, Object> map : list) {
//            batchList.add(String.valueOf(map.get("migration")));
//        }
//        return batchList;
//    }
//
//    @Override
//    public List<String> getAppendMigratorList(List<String> migratorList) {
//        //如果不是空的，则重新计算migratorList要生成的数量
//        if (column != null) {
//            List<String> appendMigratorList = new ArrayList<>();
//            //新的一轮生成
//            column.batch++;
//            if (strictMode()) {
//                //严格模式，比较所有可能的类型
//                List<MigrationRunner.Column> existsList = getExistsList();
//                for (MigrationRunner.Column exists : existsList) {
//                    //移除掉已经执行过的
//                    migratorList.remove(exists.migration);
//                }
//                appendMigratorList.addAll(migratorList);
//            } else {
//                //找到最后一个，然后往下扫
//                boolean last = false;
//                for (String migrator : migratorList) {
//                    if (last) {
//                        //如果是真，说明已经超过组后一个，则把这些都加到要加进去的
//                        appendMigratorList.add(migrator);
//                    } else {
//                        //否则判断是否到最后一个
//                        if (migrator.equals(column.migration)) {
//                            last = true;
//                        }
//                    }
//                }
//            }
//            return appendMigratorList;
//        } else {
//            return migratorList;
//        }
//    }
//
//    @Override
//    public List<String> getRemoveMigratorList(List<String> migratorList) {
//        //如果不是空的，则重新计算migratorList要生成的数量
//        List<String> removeMigratorList = new ArrayList<>();
//        if (column != null) {
//            //获得要删掉的
//            List<String> removeName = getBatchList(column.batch);
//            for (String name : removeName) {
//                //判断名字一样就加入
//                for (String migrator : migratorList) {
//                    if (migrator.equals(name)) {
//                        removeMigratorList.add(migrator);
//                    }
//                }
//            }
//        }
//        return removeMigratorList;
//    }
//
//    public boolean strictMode = true;
//
//    /**
//     * 严格模式，是否比较所有迁移
//     *
//     * @return
//     */
//    public boolean strictMode() {
//        return strictMode;
//    }
//
//    @Override
//    public int getBatch() {
//        return column.batch;
//    }
}
