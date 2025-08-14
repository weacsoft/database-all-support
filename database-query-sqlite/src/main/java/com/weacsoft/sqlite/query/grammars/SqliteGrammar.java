package com.weacsoft.sqlite.query.grammars;

import gaarason.database.query.grammars.BaseGrammar;

public class SqliteGrammar extends BaseGrammar {
    public SqliteGrammar(String tableName) {
        super(tableName, "'");
    }
}
