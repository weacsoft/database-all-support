# 暂时懒得写

BaseSchema：具体执行的迁移操作。SQLSchema就是对数据库操作，使用jdbc的方式。ClassSchema就是对类的操作。可以自己继承BaseSchema进行额外扩展。

MigrationRunner：具体的执行器，只有这个东西能执行。主要功能是把迁移和执行器导入后，把迁移文件的源代码在内存编译成类并送给schema调用。

MySqlCompiler：如果使用SQLSchema，它需要传入对应数据库的编译类。目前只有MySqlCompiler和SqliteCompiler。新增自行继承MySqlCompiler即可。

DatabaseAllSQLSchema：这个全是注释，是之前根据database-all写的，后面干脆自己用jdbc算了，懒。

## 详细支持情况

数据库支持（即Compiler）：

| 数据库    | 支持类            |
|--------|----------------|
| MySQL  | MySqlCompiler  |
| SQLite | SqliteCompiler |

迁移能力支持（即Schema）：

| 迁移类型 | 支持类         | 具体支持操作        |
|------|-------------|---------------|
| SQL  | SQLSchema   | 支持CREATE、DROP |
| 源码类  | ClassSchema | 支持CREATE      |

迁移数据类型支持（Blueprint）：

| 迁移数据类型          | 是否支持 | 对应方法                             |
|-----------------|----|----------------------------------|
| INTEGER         | 支持 | integer                          |
| BIGINT UNSIGNED | 支持 | bigIncrements、id                 |
| TIMESTAMP       | 支持 | timestamp、timestamps、softDeletes |
| VARCHAR         | 支持 | string                           |
| LONGTEXT        | 支持 | longtext                         |


迁移字段能力支持（Column）：

| 迁移字段能力 | 是否支持 | 对应方法 |
|--------|------|------|
| 主键     | 支持   | primary   |
| 自增     | 支持   | autoIncrement   |
| 可空     | 支持   | nullable   |
| 默认值    | 支持   | defaultValue   |
| 注释     | 支持   | comment   |
