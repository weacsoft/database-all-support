package com.weacsoft.oracle.query.grammars;

import gaarason.database.query.grammars.BaseGrammar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OracleGrammar extends BaseGrammar {
    public OracleGrammar(String tableName) {
        super(tableName, "");
    }

    @Override
    public void set(SQLPartType sqlPartType, String sqlPartString, Collection<Object> parameters) {
        super.set(sqlPartType, sqlPartString, parameters);
    }

    @Override
    protected List<SQLPartInfo> getDefault(SQLPartType type) {
        switch (type) {
            case TABLE:
                return Collections.singletonList(this.simpleInstanceSQLPartInfo(this.symbol + this.alias.getTable() + this.symbol, (Collection) null));
            case FROM:
                return Collections.singletonList(this.simpleInstanceSQLPartInfo(this.symbol + this.alias.getTable() + this.symbol + " as " + this.symbol + this.alias + this.symbol, (Collection) null));
            case SELECT:
                return Collections.singletonList(this.simpleInstanceSQLPartInfo("*", (Collection) null));
            case VALUE:
                return Collections.singletonList(this.simpleInstanceSQLPartInfo(" values ()", (Collection) null));
            default:
                return null;
        }
    }
}
