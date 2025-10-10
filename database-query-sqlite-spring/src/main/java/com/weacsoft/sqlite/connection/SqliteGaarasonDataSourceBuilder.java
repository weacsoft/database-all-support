package com.weacsoft.sqlite.connection;

import gaarason.database.bootstrap.ContainerBootstrap;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.core.Container;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

public class SqliteGaarasonDataSourceBuilder {
    public static GaarasonDataSource build(DataSource masterDataSource) {
        return build(masterDataSource, ContainerBootstrap.build().autoBootstrap());
    }

    public static GaarasonDataSource build(List<DataSource> masterDataSourceList) {
        return build(masterDataSourceList, ContainerBootstrap.build().autoBootstrap());
    }

    public static GaarasonDataSource build(DataSource masterDataSource, Container container) {
        return new SqliteGaarasonDataSourceWrapper(Collections.singletonList(masterDataSource), container);
    }

    public static GaarasonDataSource build(List<DataSource> masterDataSourceList, Container container) {
        return new SqliteGaarasonDataSourceWrapper(masterDataSourceList, container);
    }
}
