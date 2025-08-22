package com.weacsoft.migration.model;

// 列定义类
public class Column {
    public final String name;
    public final String type;
    public boolean nullable = false;
    public boolean primary = false;
    public boolean autoIncrement = false;
    public int length;
    public String defaultValue;
    public String comment;

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Column nullable() {
        this.nullable = true;
        return this;
    }

    public Column nullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public Column primary() {
        this.primary = true;
        return this;
    }

    public Column autoIncrement() {
        this.autoIncrement = true;
        primary();
        return this;
    }

    public Column defaultValue(String value) {
        this.defaultValue = value;
        return this;
    }

    public Column comment(String comment) {
        this.comment = comment;
        return this;
    }
}
