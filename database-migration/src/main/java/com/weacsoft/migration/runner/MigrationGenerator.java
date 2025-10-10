package com.weacsoft.migration.runner;

import com.weacsoft.migration.util.StringUtils;
import gaarason.database.logging.Log;
import gaarason.database.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//迁移文件生成器
public class MigrationGenerator {
    private static final String libPackageName = "com.weacsoft.migration.model";

    private static final Log log = LogFactory.getLog(MigrationRunner.class);

    public enum MigrationType {
        CREATE,
        DROP
    }

    /**
     * 静态方法：生成迁移类
     *
     * @param outputDir   生成文件的根目录（如：./src/main/java）
     * @param packageName 迁移类所在包名（如：com.example.migrations）
     * @param type        迁移类型（CREATE 或 DROP）
     * @param className   迁移类名（如：Setting）
     */
    public static void generate(
            String outputDir,
            String packageName,
            MigrationType type,
            String className
    ) {
        try {
            // 1. 生成时间戳（yyyyMMddHHmmss）
            String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
                    .format(new Date());

            // 2. 构建包路径和文件路径
            String packagePath = packageName.replace(".", "/");
            File dir = new File(outputDir + "/" + packagePath);
            if (!dir.exists()) {
                dir.mkdirs(); // 创建目录
            }
            String classFileName = "Migrator_" + timestamp + "_" + StringUtils.underlineToPascalCase(type.name()) + StringUtils.underlineToPascalCase(className) + "Table";
            File file = new File(dir, classFileName + ".java");

            // 3. 生成迁移类源码
            String sourceCode = buildSourceCode(libPackageName, classFileName, className);

            // 4. 写入文件
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(sourceCode);
            }
            log.info("迁移类生成成功：" + file.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("生成迁移类失败", e);
        }
    }

    /**
     * 构建迁移类的Java源码
     */
    private static String buildSourceCode(
            String libPackageName,
            String classFileName,
            String className) {
        String tableName = StringUtils.pascalCaseToUnderline(className);
        if (tableName.charAt(tableName.length() - 1) == 's') {
            tableName = tableName + "es";
        } else {
            tableName = tableName + "s";
        }
        // 基础导入语句
        return "import " + libPackageName + ".Migration;\n" +
                "import " + libPackageName + ".MigrationAnnotation;\n" +
                // 类注解（包含自动生成的timestamp）
                "@MigrationAnnotation\n" +
                "public class " + classFileName + " extends Migration {\n\n" +
                "    public String tableName=\"" + tableName + "\";\n" +
                "    public String className=\"" + className + "\";\n" +
                "    @Override\n" +
                // up()方法（创建/修改表）
                "    public void up() {\n" +
                // 创建表的逻辑（默认生成id、created_at、updated_at字段）
                "        schema.create(tableName, table -> {\n" +
                "            table.id();\n" +
                "            // 在这里添加字段定义\n" +
                "            // 示例：table.string(\"name\").nullable();\n" +
                "            table.timestamps();\n" +
                "        });\n" +
                "    }\n\n" +

                "    @Override\n" +
                // down()方法（删除/回滚表）
                "    public void down() {\n" +
                // 创建表的回滚：删除表
                "        schema.dropIfExists(tableName);\n" +
                "    }\n" +
                "}";
    }
}
