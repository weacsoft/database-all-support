package com.weacsoft.oracle.query;

import gaarason.database.appointment.SqlType;
import gaarason.database.contract.query.Grammar;
import gaarason.database.lang.Nullable;
import gaarason.database.query.AbstractBuilder;
import gaarason.database.util.FormatUtils;
import gaarason.database.util.ObjectUtils;
import gaarason.database.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

public class OracleBuilder<T, K> extends AbstractBuilder<OracleBuilder<T, K>, T, K> {
    @Override
    public OracleBuilder<T, K> getSelf() {
        return this;
    }

    @Override
    public String tableAlias(String table) {
        return table + " " + this.alias();
    }

    @Override
    public String supportBackQuote(String s) {
        return FormatUtils.backQuote(s, "");
    }

    @Override
    public OracleBuilder<T, K> limit(Object offset, Object take) {
        Collection<Object> parameters = new ArrayList<>(2);
        String sqlPart = " OFFSET " + getGrammar().replaceValueAndFillParameters(offset, parameters) + "ROWS FETCH FIRST " +
                getGrammar().replaceValueAndFillParameters(take, parameters) + " ROWS ONLY";
        getGrammar().set(Grammar.SQLPartType.LIMIT, sqlPart, parameters);
        return this;
    }

    @Override
    public OracleBuilder<T, K> limit(Object take) {
        return limit(0, take);
    }

    @Override
    public OracleBuilder<T, K> fromRaw(@Nullable String sqlPart) {
        //拦截，将默认的`作为分割，适配Oracle使用'进行分割
        sqlPart = sqlPart.replace("`", "");
        if (!ObjectUtils.isEmpty(sqlPart)) {
            this.getGrammar().set(Grammar.SQLPartType.FROM, sqlPart, (Collection) null);
            this.getGrammar().set(Grammar.SQLPartType.TABLE, sqlPart, (Collection) null);
        }

        return this;
    }

    public String toSql(SqlType sqlType) {
        return this.toSql(sqlType, (sql, parameters) -> {
            return String.format(StringUtils.replace(sql, " ? ", "\'%s\'"), parameters.toArray());
        });
    }
}
