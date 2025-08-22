package com.weacsoft.migration.exception;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLRuntimeException  extends RuntimeException{
    private static final Pattern COMPILE = Pattern.compile(" ? ", 16);

    public SQLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SQLRuntimeException(String sql, Collection<?> parameters, String message, String symbol, Throwable cause) {
        super("message : [" + message + "] sql : [" + String.format(COMPILE.matcher(sql).replaceAll(Matcher.quoteReplacement(symbol + "%s" + symbol)), parameters.toArray()) + "]", cause);
    }

    public SQLRuntimeException(String sql, String message, Throwable cause) {
        super("message : [" + message + "] sql : [" + sql + "]", cause);
    }
}
