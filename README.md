# database-all-support

**latest-version**：![](https://jitpack.io/v/weacsoft/database-all-support.svg)

## 简介 Introduction

- 为[database-all](https://jitpack.io/#gaarason/database-all)提供的额外扩展

## 额外支持目录 Extra Support Directory

* [公共配置 Public Configuration](#公共配置)
* [Oracle数据库支持 Oracle Database Support](#Oracle数据库支持)
* [Sqlite数据库支持 Sqlite Database Support](#Sqlite数据库支持)
* [迁移 Migration](#迁移)
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

Spring版本（只需要引入这个，会自动引入上面的）：

```xml
<dependency>
    <groupId>com.github.weacsoft.database-all-support</groupId>
    <artifactId>database-query-oracle-spring</artifactId>
    <version>{latest-version}</version>
</dependency>
```

### Sqlite数据库支持

引入Sqlite数据库支持

```xml
<dependency>
    <groupId>com.github.weacsoft.database-all-support</groupId>
    <artifactId>database-query-sqlite</artifactId>
    <version>{latest-version}</version>
</dependency>
```

Spring版本（只需要引入这个，会自动引入上面的）：

```xml
<dependency>
    <groupId>com.github.weacsoft.database-all-support</groupId>
    <artifactId>database-query-sqlite-spring</artifactId>
    <version>{latest-version}</version>
</dependency>
```

### 迁移

引入：

```xml

<dependency>
    <groupId>com.github.weacsoft.database-all-support</groupId>
    <artifactId>database-migration</artifactId>
    <version>{latest-version}</version>
</dependency>
```

使用Laravel的Migration，仿造的Java语法，可以进行数据表的生成和删除、文件的生成和删除。

简明使用方式可见下面与com.weacsoft.migration.Main.java

生成迁移文件：

```java
import com.weacsoft.migration.runner.MigrationGenerator;

MigrationGenerator.generate("项目输出目录","包名",MigrationGenerator.MigrationType.CREATE, "类名");
```

执行迁移：
```java
MySqlCompiler compiler;
BaseSchema schema;
MigrationRunner runner;
compiler = new SqliteCompiler();
//迁移生成类
schema = new ClassSchema("项目输出目录", "模型包名");
runner = new MigrationRunner(schema, "模型目录");
runner.upDoing();
```

解释：

BaseSchema：具体执行的迁移操作。SQLSchema就是对数据库操作，使用jdbc的方式。ClassSchema就是对类的操作。可以自己继承BaseSchema进行额外扩展。

MigrationRunner：具体的执行器，只有这个东西能执行。主要功能是把迁移和执行器导入后，把迁移文件的源代码在内存编译成类并送给schema调用。

MySqlCompiler：如果使用SQLSchema，它需要传入对应数据库的编译类。目前只有MySqlCompiler和SqliteCompiler。新增自行继承MySqlCompiler即可。

回滚
```java
//上面一样
runner.downDoing();
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