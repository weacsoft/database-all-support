package com.weacsoft.oracle.connection;

import gaarason.database.connection.GaarasonSmartDataSourceWrapper;
import gaarason.database.core.Container;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

public class OracleGaarasonDataSourceWrapper extends GaarasonSmartDataSourceWrapper {
    public OracleGaarasonDataSourceWrapper(List<DataSource> masterDataSourceList, Container container) {
        super(masterDataSourceList, container);
    }

    public static OracleGaarasonDataSourceWrapper build(DataSource dataSource, Container container) {
        return new OracleGaarasonDataSourceWrapper(Collections.singletonList(dataSource), container);
    }
}
