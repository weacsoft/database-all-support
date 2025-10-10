package com.weacsoft.migration;

import com.weacsoft.migration.compiler.MySqlCompiler;
import com.weacsoft.migration.compiler.SqliteCompiler;
import com.weacsoft.migration.runner.MigrationGenerator;
import com.weacsoft.migration.runner.MigrationRunner;
import com.weacsoft.migration.schema.BaseSchema;
import com.weacsoft.migration.schema.ClassSchema;

public class Main {
    //项目目录
    private static final String PROJECT_OUTPUT_DIR = "./src/main/java";
    //迁移文件目录
    private static final String MIGRATION_DIRECTORY = PROJECT_OUTPUT_DIR + "/com/weacsoft/migration/";
    //模型包名
    private static final String MODELS_PACKAGE_NAME = "com.weacsoft.models";
    //迁移包名
    private static final String MIGRATION_PACKAGE_NAME = "com.weacsoft.migration";

    public static void main(String[] args) {
        //下面是相关演示
        //generate();
        //fileUp();
    }

    //生成演示
    public static void generate() {
        MigrationGenerator.generate(PROJECT_OUTPUT_DIR, MIGRATION_PACKAGE_NAME, MigrationGenerator.MigrationType.CREATE, "Setting");
    }

    //迁移
    public static void fileUp() {
        MySqlCompiler compiler;
        BaseSchema schema;
        MigrationRunner runner;
        compiler = new SqliteCompiler();
        //迁移生成类
        schema = new ClassSchema(PROJECT_OUTPUT_DIR, MODELS_PACKAGE_NAME);
        runner = new MigrationRunner(schema, MIGRATION_DIRECTORY);
        runner.upDoing();
    }

    //迁移回滚
    public static void fileDown() {
        MySqlCompiler compiler;
        BaseSchema schema;
        MigrationRunner runner;
        compiler = new SqliteCompiler();
        //迁移生成类
        schema = new ClassSchema(PROJECT_OUTPUT_DIR, MODELS_PACKAGE_NAME);
        runner = new MigrationRunner(schema, MIGRATION_DIRECTORY);
        runner.downDoing();
    }
}
