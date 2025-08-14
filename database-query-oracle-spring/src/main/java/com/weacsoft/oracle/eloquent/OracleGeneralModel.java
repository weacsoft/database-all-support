package com.weacsoft.oracle.eloquent;
import com.weacsoft.oracle.query.OracleBuilder;
import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.eloquent.Model;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;
import gaarason.database.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.Collection;

public class OracleGeneralModel extends Model<OracleBuilder<OracleGeneralModel.Table, Serializable>, OracleGeneralModel.Table, Serializable> {

    private static final Log log = LogFactory.getLog(OracleGeneralModel.class);

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

    @gaarason.database.annotation.Table(name = "GeneralModel_Table")
    public static class Table implements Serializable {

    }

}
