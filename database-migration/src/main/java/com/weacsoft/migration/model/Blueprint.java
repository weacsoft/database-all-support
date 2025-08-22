package com.weacsoft.migration.model;


import java.util.ArrayList;
import java.util.List;

// 表结构蓝图类
public class Blueprint {
    public final List<Column> columns = new ArrayList<>();
    //目前使用的type：
    //INTEGER
    //BIGINT UNSIGNED
    //TIMESTAMP
    //VARCHAR
    //LONGTEXT

    public Column integer(String name) {
        return addColumn(name, "INTEGER");
    }

    public Column bigIncrements(String name) {
        return addColumn(name, "BIGINT UNSIGNED").autoIncrement();
    }

    public Column bigIncrements() {
        return bigIncrements("id");
    }

    public Column id(String name) {
        return bigIncrements(name).autoIncrement();
    }

    public Column id() {
        return id("id");
    }

    public Column timestamp(String name) {
        return addColumn(name, "TIMESTAMP");
    }

    public void timestamps() {
        timestamp("created_at").nullable();
        timestamp("updated_at").nullable();
    }

    public void softDeletes() {
        timestamp("deleted_at").nullable();
    }

    public Column string(String name, int length) {
        length = 255;
        return addColumn(name, "VARCHAR(" + length + ")");
    }

    public Column string(String name) {
        return string(name, 255);
    }

    public Column longtext(String name) {
        return addColumn(name, "LONGTEXT");
    }

    // 添加列
    private Column addColumn(String name, String type) {
        Column column = new Column(name, type);
        columns.add(column);
        return column;
    }
}
