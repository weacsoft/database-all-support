package com.weacsoft.migration.compiler;


import com.weacsoft.migration.model.Column;

import java.util.ArrayList;
import java.util.List;

public class SqliteCompiler extends MySqlCompiler {

    @Override
    public String showTable() {
        return "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
    }

    //编译创建表
    @Override
    public String compileCreate(String tableName, List<Column> list) {
        StringBuilder string = new StringBuilder();
        //创建表
        string.append("CREATE TABLE '");
        string.append(tableName);
        string.append("' ( ");
        List<String> primaryList = new ArrayList<>();
        for (Column column : list) {
            //获得编译结果
            string.append(compileColumn(column));
            if (column.primary) {
                primaryList.add(column.name);
            }
        }
        //删掉最后一个逗号
        string.deleteCharAt(string.length() - 1);
//        //主键生成
//        if (!primaryList.isEmpty()) {
//            string.append(" , PRIMARY KEY (");
//            string.append(String.join(",", primaryList));
//            string.append(")");
//        }
        //创建表结束
        string.append(" );");
        return string.toString();
    }

    //编译修改表
    @Override
    public String compileChange(String tableName, List<Column> list) {
        return "";
    }

    //编译删除表
    @Override
    public String compileDrop(String tableName) {
        return "DROP TABLE IF EXISTS `" + tableName + "` ";
    }

    //换这个换编译类型
    @Override
    public String replace(String type) {
        return type.replace("BIGINT UNSIGNED", "INTEGER")
                .replace("LONGTEXT", "VARCHAR(255)");
    }

    //编译表字段
    @Override
    public String compileColumn(Column column) {
        StringBuilder string = new StringBuilder();
        string.append(" '");
        string.append(column.name);
        string.append("' ");
        string.append(" ");
        string.append(replace(column.type));
        string.append(" ");
        //是否主键
        if (column.primary) {
            string.append(" PRIMARY KEY ");
        }
        //是否自增
        if (column.autoIncrement) {
            string.append(" AUTOINCREMENT ");
        }
        //是否可空
        else if (column.nullable) {
            string.append(" NULL ");
        } else {
            string.append(" NOT NULL ");
        }
        //是否有默认值（非空才可以设置默认值）
        if (!column.nullable && column.defaultValue != null) {
            string.append("DEFAULT '");
            string.append(column.defaultValue);
            string.append("' ");
        }
        //sqlite没有列注释，直接当不存在
//        if (column.comment != null) {
//            string.append("COMMENT '");
//            string.append(column.comment);
//            string.append("' ");
//        }
        string.append(",");
        return string.toString();
    }
}
