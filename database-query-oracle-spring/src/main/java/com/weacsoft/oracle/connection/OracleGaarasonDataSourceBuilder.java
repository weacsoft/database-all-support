package com.weacsoft.oracle.connection;

import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

public class OracleGaarasonDataSourceBuilder {
    public static GaarasonDataSource build(DataSource masterDataSource) {
        return build((DataSource) masterDataSource, (Container) ContainerBootstrap.build().autoBootstrap());
    }

    public static GaarasonDataSource build(List<DataSource> masterDataSourceList) {
        return build((List) masterDataSourceList, (Container) ContainerBootstrap.build().autoBootstrap());
    }

    public static GaarasonDataSource build(DataSource masterDataSource, Container container) {
        return new OracleGaarasonDataSourceWrapper(Collections.singletonList(masterDataSource), container);
    }

    public static GaarasonDataSource build(List<DataSource> masterDataSourceList, Container container) {
        return new OracleGaarasonDataSourceWrapper(masterDataSourceList, container);
    }
}
