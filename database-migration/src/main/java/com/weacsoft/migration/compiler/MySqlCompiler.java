package com.weacsoft.migration.compiler;


import com.weacsoft.migration.model.Column;

import java.util.ArrayList;
import java.util.List;

public class MySqlCompiler {

    public String showTable() {
        return "show tables";
    }

    public String getLastSql() {
        return "SELECT id,migration,batch FROM migrations ORDER BY id DESC LIMIT 1";
    }

    public String getExistsListSql() {
        return "SELECT id,migration,batch FROM migrations ORDER BY id DESC";
    }

    public String getBatchListSql(int batch) {
        return "SELECT id,migration,batch FROM migrations WHERE batch = '" + batch + "' ORDER BY id DESC";
    }

    //编译创建表
    public String compileCreate(String tableName, List<Column> list) {
        StringBuilder string = new StringBuilder();
        //创建表
        string.append("CREATE TABLE `");
        string.append(tableName);
        string.append("` ( ");
        List<String> primaryList = new ArrayList<>();
        for (Column column : list) {
            //获得编译结果
            string.append(compileColumn(column));
            if (column.primary) {
                primaryList.add(column.name);
            }
        }
        if (!primaryList.isEmpty()) {
            //如果主键列表不是空的，那么就执行主键生成指令
            string.append(" PRIMARY KEY(");
            for (String name : primaryList) {
                string.append("`");
                string.append(name);
                string.append("`,");
            }
            //删掉最后一个逗号
            string.deleteCharAt(string.length() - 1);
            string.append("),");
        }
        //删掉最后一个逗号
        string.deleteCharAt(string.length() - 1);

        //创建表结束
        string.append(" ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        return string.toString();
    }

    //编译修改表
    public String compileChange(String tableName, List<Column> list) {
        return "";
    }

    //编译删除表
    public String compileDrop(String tableName) {
        return "DROP TABLE IF EXISTS `" + tableName + "` ";
    }

    //换这个换编译类型
    public String replace(String type) {
        return type;
    }

    //编译表字段
    public String compileColumn(Column column) {
        StringBuilder string = new StringBuilder();
        string.append(" `");
        string.append(column.name);
        string.append("` ");
        string.append(" ");
        string.append(replace(column.type));
        string.append(" ");
        //是否自增主键
        if (column.autoIncrement) {
            string.append("AUTO_INCREMENT ");
        }
        //是否可空
        if (column.nullable) {
            string.append("NULL ");
        } else {
            string.append("NOT NULL ");
        }
        //是否有默认值（非空才可以设置默认值）
        if (!column.nullable && column.defaultValue != null) {
            string.append("DEFAULT `");
            string.append(column.defaultValue);
            string.append("` ");
        }
        if (column.comment != null) {
            string.append("COMMENT '");
            string.append(column.comment);
            string.append("' ");
        }
        string.append(",");
        return string.toString();
    }
}
