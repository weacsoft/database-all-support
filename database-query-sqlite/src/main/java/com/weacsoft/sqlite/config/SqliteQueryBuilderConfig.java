package com.weacsoft.sqlite.config;

import com.weacsoft.sqlite.query.SqliteBuilder;
import com.weacsoft.sqlite.query.grammars.SqliteGrammar;
import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Builder;
import gaarason.database.contract.eloquent.Model;
import gaarason.database.util.ObjectUtils;

public class SqliteQueryBuilderConfig implements QueryBuilderConfig {
    @Override
    public String getValueSymbol() {
        return "'";
    }

    // 根据数据库名称, 启用当前配置
    @Override
    public boolean support(String databaseProductName) {
        return "sqlite".equals(databaseProductName);
    }

    // 其他 QueryBuilderConfig 接口方法, 按需实现
    // 根据实际情况, 重写 Builder 查询构造器
    // 根据实际情况, 重写 Grammar 语法
    @Override
    public <T, K> Builder<?, T, K> newBuilder(GaarasonDataSource gaarasonDataSource, Model<?, T, K> model) {
        return (Builder<?, T, K>) new SqliteBuilder<>().initBuilder(gaarasonDataSource, ObjectUtils.typeCast(model), new SqliteGrammar(model.getTableName()));
    }
}
