# database-all-support

**latest-version**：![](https://jitpack.io/v/weacsoft/database-all-support.svg)

## 简介 Introduction

- 为[database-all](https://jitpack.io/#gaarason/database-all)提供的额外扩展

## 额外支持目录 Extra Support Directory

* [公共配置 Public Configuration](#公共配置)
* [Oracle数据库支持 Oracle Database Support](#Oracle数据库支持)
* [Sqlite数据库支持 Sqlite Database Support](#Sqlite数据库支持)
* [通用模型 Generic Model](#通用模型)

### 公共配置

1.引入仓库 pom.xml

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Oracle数据库支持

引入Oracle数据库支持

```xml
<dependency>
    <groupId>com.github.weacsoft.database-all-support</groupId>
    <artifactId>database-query-oracle</artifactId>
    <version>{latest-version}</version>
</dependency>
```

### Sqlite数据库支持

引入Oracle数据库支持

```xml
<dependency>
    <groupId>com.github.weacsoft.database-all-support</groupId>
    <artifactId>database-query-sqlite</artifactId>
    <version>{latest-version}</version>
</dependency>
```

### 通用模型

由于GeneralModel原来使用MySQL数据库的MySQLBuilder制作的模型不能使用，因此给一个模型模板，自己换里面的泛型部分即可使用。样例是Sqlite版本，基于SpringBoot进行配置的。

```java
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
```