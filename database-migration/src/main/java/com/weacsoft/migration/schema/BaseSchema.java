package com.weacsoft.migration.schema;


import com.weacsoft.migration.model.Blueprint;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseSchema {
    public String tableName, className;

    public void init() {

    }

    //创建表
    public void create(String table, Consumer<Blueprint> callback) {
        throw new RuntimeException("表创建未实现！");
    }

    //修改表
    public void table(String table, Consumer<Blueprint> callback) {
        throw new RuntimeException("表修改未实现！");
    }

    //删除表
    public void dropIfExists(String table) {
        throw new RuntimeException("表删除未实现！");
    }

    //删除表旧版
    public void drop(String table) {
        dropIfExists(table);
    }


    /**
     * 迁移前做什么
     */
    public boolean upStart() {
        return true;
    }

    /**
     * 迁移后做什么
     */
    public void upFinish() {

    }

    /**
     * 回滚前做什么
     */
    public boolean downStart() {
        return true;
    }

    /**
     * 回滚后做什么
     */
    public void downFinish() {

    }

    /**
     * 整个操作时第几步
     *
     * @return
     */
    public int getBatch() {
        return 1;
    }

    /**
     * 获得需要增加的列表
     *
     * @param migratorList
     * @return
     */
    public List<String> getAppendMigratorList(List<String> migratorList) {
        return migratorList;
    }

    /**
     * 获得需要删除的列表
     *
     * @param migratorList
     * @return
     */
    public List<String> getRemoveMigratorList(List<String> migratorList) {
        return migratorList;
    }

    /**
     * 迁移前
     *
     * @param migration
     * @param batch
     * @return
     */
    public boolean upStartOne(String migration, int batch) {
        return true;
    }

    /**
     * 迁移后
     *
     * @param migration
     * @param batch
     */
    public void upFinishOne(String migration, int batch) {
    }

    /**
     * 回滚前
     *
     * @param migration
     * @param batch
     * @return
     */
    public boolean downStartOne(String migration, int batch) {
        return true;
    }

    /**
     * 回滚后
     *
     * @param migration
     * @param batch
     */
    public void downFinishOne(String migration, int batch) {
    }
}
