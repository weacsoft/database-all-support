package com.weacsoft.sqlite.connection;

import gaarason.database.connection.GaarasonSmartDataSourceWrapper;
import gaarason.database.core.Container;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

public class SqliteGaarasonDataSourceWrapper extends GaarasonSmartDataSourceWrapper {
    public SqliteGaarasonDataSourceWrapper(List<DataSource> masterDataSourceList, Container container) {
        super(masterDataSourceList, container);
    }

    public static SqliteGaarasonDataSourceWrapper build(DataSource dataSource, Container container) {
        return new SqliteGaarasonDataSourceWrapper(Collections.singletonList(dataSource), container);
    }
}
