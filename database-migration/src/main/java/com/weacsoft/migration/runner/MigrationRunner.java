package com.weacsoft.migration.runner;

import com.weacsoft.migration.model.Migration;
import com.weacsoft.migration.model.MigrationAnnotation;
import com.weacsoft.migration.util.MigrationScanner;
import com.weacsoft.migration.schema.BaseSchema;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;

import java.util.List;

public class MigrationRunner {
    //目录
    private final String migrationDirectory;
    //迁移扫描器
    private final MigrationScanner migrationScanner = new MigrationScanner();
    //迁移执行器
    private final BaseSchema baseSchema;

    private static final Log log = LogFactory.getLog(MigrationRunner.class);


    /**
     * 构造迁移管理器
     *
     * @param migrationDirectory 迁移文件存放目录
     * @param baseSchema         迁移执行器
     */
    public MigrationRunner(BaseSchema baseSchema, String migrationDirectory) {
        this.migrationDirectory = migrationDirectory;
        this.baseSchema = baseSchema;
    }

    /**
     * 获得所有迁移文件信息
     *
     * @return
     * @throws Exception
     */
    public List<String> getAllMigrations() {
        return migrationScanner.getCompiledClasses();
    }

    /**
     * 执行迁移
     */
    public void upDoing() {
        //载入下面所有的类
        try {
            init();
            upStart();
            //获得所有类
            List<String> migratorList = getAllMigrations();
            //记录需要增加的类
            List<String> appendMigratorList = baseSchema.getAppendMigratorList(migratorList);
            int batch = baseSchema.getBatch();

            for (String migrator : appendMigratorList) {
                //判断是否返回true，返回true才能继续，否则跳过
                if (baseSchema.upStartOne(migrator, batch)) {
                    //然后对所有需要增加的migrator，执行up方法
                    upDoingOne(migrator);
                    //执行成功后写入到数据库中
                    baseSchema.upFinishOne(migrator, batch);
                    log.info("已执行：" + migrator);
                }
            }
            log.info("共" + appendMigratorList.size() + "个");
            upFinish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行回滚
     */
    //执行迁移回滚
    public void downDoing() {
        //载入下面所有的类
        try {
            init();
            downStart();
            //获得所有类
            List<String> migratorList = getAllMigrations();
            List<String> removeMigratorList = baseSchema.getRemoveMigratorList(migratorList);
            int batch = baseSchema.getBatch();
            for (String migrator : removeMigratorList) {
                if (baseSchema.downStartOne(migrator, batch)) {
                    //然后对所有需要增加的migrator，执行up方法
                    downDoingOne(migrator);
                    //执行成功后写入到数据库中
                    baseSchema.downFinishOne(migrator, batch);
                    log.info("已执行：" + migrator);
                }
            }
            log.info("共" + removeMigratorList.size() + "个");
            downFinish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void upDoingOne(String migrator) throws Exception {
        Class<?> clazz = migrationScanner.getCompiledClass(migrator);
        if (clazz != null) {
            if (clazz.isAnnotationPresent(MigrationAnnotation.class)) {
                Object o = clazz.getDeclaredConstructor().newInstance();
                //执行和生成
                if (o instanceof Migration) {
                    initOne(((Migration) o));
                    ((Migration) o).up();
                }
            }
        }
    }

    public void initOne(Migration o) throws NoSuchFieldException, IllegalAccessException {
        o.schema = baseSchema;
        baseSchema.tableName = (String) o.getClass().getField("tableName").get(o);
        //baseSchema.tableName = o.tableName;
        baseSchema.className = (String) o.getClass().getField("className").get(o);
    }

    public void downDoingOne(String migrator) throws Exception {
        Class<?> clazz = migrationScanner.getCompiledClass(migrator);
        if (clazz != null) {
            if (clazz.isAnnotationPresent(MigrationAnnotation.class)) {
                Object o = clazz.getDeclaredConstructor().newInstance();
                //执行和生成
                if (o instanceof Migration) {
                    initOne(((Migration) o));
                    ((Migration) o).down();
                }
            }
        }
    }

    /**
     * 迁移前做什么
     */
    public void upStart() {
        log.info("迁移开始！");
        baseSchema.upStart();
    }

    /**
     * 迁移后做什么
     */
    public void upFinish() {
        baseSchema.upFinish();
        finish();
        log.info("迁移完成！");
    }

    /**
     * 回滚前做什么
     */
    public void downStart() {
        log.info("迁移回滚开始！");
        baseSchema.downStart();
    }

    /**
     * 回滚后做什么
     */
    public void downFinish() {
        baseSchema.downFinish();
        finish();
        log.info("迁移回滚完成！");
    }

    /**
     * 开始前的初始化
     */
    public void init() {
        try {
            migrationScanner.compileFromDirectory(migrationDirectory);
            baseSchema.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 结束后的回收
     */
    public void finish() {
        //执行完成后，删除所有的migrator
        migrationScanner.removeAll();
        //释放掉类加载器
        migrationScanner.removeMemoryClassLoader();
    }


    //记录迁移步骤的类
    public static class Column {
        public String id;
        public String migration;
        public int batch;
    }
}
