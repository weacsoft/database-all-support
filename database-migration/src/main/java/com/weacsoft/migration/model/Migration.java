package com.weacsoft.migration.model;


import com.weacsoft.migration.schema.BaseSchema;

// 迁移基类
public abstract class Migration {
    public BaseSchema schema;
    public String tableName, className;

    // 执行迁移
    public abstract void up();

    // 回滚迁移
    public abstract void down();

}
