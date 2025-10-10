package com.weacsoft.sqlite.query;

import gaarason.database.contract.query.Grammar;
import gaarason.database.query.AbstractBuilder;
import gaarason.database.util.FormatUtils;

import java.util.ArrayList;
import java.util.Collection;

public class SqliteBuilder<T, K> extends AbstractBuilder<SqliteBuilder<T, K>, T, K> {
    @Override
    public SqliteBuilder<T, K> getSelf() {
        return this;
    }

    @Override
    public String supportBackQuote(String s) {
        return FormatUtils.backQuote(s, "\"");
    }

    @Override
    public SqliteBuilder<T, K> limit(Object offset, Object take) {
        Collection<Object> parameters = new ArrayList<>(2);
        String sqlPart = getGrammar().replaceValueAndFillParameters(offset, parameters) + "," +
                getGrammar().replaceValueAndFillParameters(take, parameters);
        getGrammar().set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return this;
    }

    @Override
    public SqliteBuilder<T, K> limit(Object take) {
        return limit(0, take);
    }
}
