package com.weacsoft.sqlite.eloquent;

import com.weacsoft.sqlite.query.SqliteBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.Collection;

public class SqliteGeneralModel extends Model<SqliteBuilder<SqliteGeneralModel.Table, Serializable>, SqliteGeneralModel.Table, Serializable> {

    private static final Log log = LogFactory.getLog(SqliteGeneralModel.class);

    @Lazy
    @Autowired
    private GaarasonDataSource gaarasonDataSource;

    @Override
    public GaarasonDataSource getGaarasonDataSource() {
        return gaarasonDataSource;
    }

    public void log(String sql, Collection<?> parameterList) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "SQL complete : " + String.format(StringUtils.replace(sql, " ? ", "\"%s\""), parameterList.toArray()));
        }
    }

    @gaarason.database.annotation.Table(name = "@@GeneralModel_Table@@")
    public static class Table implements Serializable {

    }

}
