package com.weacsoft.oracle.config;

import gaarason.database.config.GaarasonAutoconfiguration;
import gaarason.database.config.QueryBuilderConfig;
import gaarason.database.contract.function.InstanceCreatorFunctionalInterface;
import gaarason.database.core.Container;

public class OracleAutoconfiguration implements GaarasonAutoconfiguration {
    @Override
    public void init(Container container) {
        container.register(QueryBuilderConfig.class,
                new InstanceCreatorFunctionalInterface<QueryBuilderConfig>() {
                    @Override
                    public QueryBuilderConfig execute(Class<QueryBuilderConfig> clazz) throws Throwable {
                        return new OracleQueryBuilderConfig();
                    }

                    // 更高的优先级, 很关键
                    @Override
                    public Integer getOrder() {
                        return InstanceCreatorFunctionalInterface.super.getOrder() - 1;
                    }
                });
        // ....
    }
}
