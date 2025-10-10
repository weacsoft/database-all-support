package com.weacsoft.migration.schema;

import com.weacsoft.migration.model.Blueprint;
import com.weacsoft.migration.model.Column;
import com.weacsoft.migration.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ClassSchema extends BaseSchema {
    public String outputDir;
    public String packageName;

    public ClassSchema() {

    }

    public ClassSchema(String outputDir, String packageName) {
        this.outputDir = outputDir;
        //处理打成a.b.c.的形式
        if (packageName.charAt(packageName.length() - 1) == '.') {
            packageName = packageName.substring(0, packageName.length() - 1);
        }
        this.packageName = packageName;
    }

    @Override
    public void init() {
        //生成默认Model文件
        saveFile("base", "BaseModel", generateBaseModelClass());
    }

    @Override
    public void create(String table, Consumer<Blueprint> callback) {
        Blueprint blueprint = new Blueprint();
        callback.accept(blueprint);
        //生成模型类
        String modelSource = generateModelClass(table, className, blueprint.columns);
        //保存模型类
        saveFile("", className, modelSource);
    }

    @Override
    public void dropIfExists(String table) {
        //删除文件当没反应
    }

    @Override
    public void table(String table, Consumer<Blueprint> callback) {
        //修改文件当没反应
    }

    @Override
    public void drop(String table) {
        super.drop(table);
    }

    /**
     * 保存文件
     * @param path 路径
     * @param className 类名
     * @param sourceCode 源码
     */
    private void saveFile(String path, String className, String sourceCode) {
        File dir = new File(outputDir + "/" + packageName.replace(".", "/") + "/" + path);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录
        }
        File file = new File(outputDir + "/" + packageName.replace(".", "/") + "/" + path, className + ".java");
        if (file.exists()) {
            //文件存在直接跳过
            System.out.println(className + "文件存在！");
        } else {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(sourceCode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //生成默认模型
    public String generateBaseModelClass() {
        return "package " + packageName + ".base" + ";\n\n" +
                "import " + "gaarason.database.contract.connection.GaarasonDataSource;\n" +
                "import " + "gaarason.database.contract.eloquent.Builder;\n" +
                "import " + "gaarason.database.eloquent.Model;\n" +
                "import " + "gaarason.database.query.MySqlBuilder;\n" +
                "import " + "org.springframework.context.annotation.Lazy;\n" +
                "import " + "jakarta.annotation.Resource;\n" +
                "import " + "java.time.LocalDateTime;\n" +
                "\n" +
                "abstract public class BaseModel<T, K> extends Model<MySqlBuilder<T, K>, T, K> {\n" +
                "    @Resource\n" +
                "    @Lazy\n" +
                "    private GaarasonDataSource gaarasonDataSource;\n" +
                "    @Override\n" +
                "    public GaarasonDataSource getGaarasonDataSource() {\n" +
                "        return gaarasonDataSource;\n" +
                "    }\n" +
                "    @Override\n" +
                "    public int delete(Builder<?, T, K> builder) {\n" +
                "        return softDeleting() ? softDelete(builder) : builder.forceDelete();\n" +
                "    }\n" +
                "    public int restore(Builder<?, T, K> builder) {\n" +
                "        return softDeleteRestore(builder);\n" +
                "    }\n" +
                "    protected void scopeSoftDeleteOnlyTrashed(Builder<?, T, K> builder) {\n" +
                "        builder.whereNotNull(\"deleted_at\");\n" +
                "    }\n" +
                "    protected void scopeSoftDelete(Builder<?, T, K> builder) {\n" +
                "        builder.whereNull(\"deleted_at\");\n" +
                "    }\n" +
                "    protected int softDelete(Builder<?, T, K> builder) {\n" +
                "        return builder.data(\"deleted_at\", LocalDateTime.now()).update();\n" +
                "    }\n" +
                "    protected int softDeleteRestore(Builder<?, T, K> builder) {\n" +
                "        return builder.data(\"deleted_at\", null).update();\n" +
                "    }\n" +
                "}";
    }

    public String generateModelClass(String tableName, String className, List<Column> columns) {
        StringBuilder source = new StringBuilder();
        source.append("package ").append(packageName).append(";\n\n");
        source.append("import ").append(packageName).append(".base.BaseModel;\n");
        source.append("import ").append("gaarason.database.annotation.Primary;\n");
        source.append("import ").append("gaarason.database.annotation.Table;\n");
        source.append("import ").append("gaarason.database.annotation.Column;\n");
        source.append("import ").append("gaarason.database.contract.support.FieldFill;\n");
        source.append("import ").append("java.io.Serializable;\n");
        source.append("import ").append("org.springframework.stereotype.Repository;\n");
        source.append("import ").append("lombok.Data;\n");
        source.append("import ").append("java.time.LocalDateTime;\n");
        source.append("import ").append("gaarason.database.contract.eloquent.Record;\n");
        source.append("\n");
        source.append("@Data\n@Table(name = \"").append(tableName).append("\")\n");
        source.append("public class ").append(className).append(" implements Serializable {\n");
        //根据列生成字段
        boolean softDeleted = false;
        boolean hasUpdated = false;
        for (Column column : columns) {
            if (column.name.equals("deleted_at")) {
                softDeleted = true;
            } else if (column.name.equals("updated_at")) {
                hasUpdated = true;
            }
            generateModelClassColumn(source, column);
        }
        //制作对应的子类模型
        source.append("    @Repository\n");
        source.append("    public static class Model extends ").append(packageName).append(".base.BaseModel<").append(className).append(",Long>").append(" {\n");
        if (softDeleted) {
            source.append("        @Override\n");
            source.append("        protected boolean softDeleting() {\n");
            source.append("            return true;\n");
            source.append("        }\n");
        }
        if (hasUpdated) {
            source.append("        @Override\n");
            source.append("        public boolean eventRecordCreating(Record<").append(className).append(", Long> record) {\n");
            source.append("            record.getEntity().setUpdatedAt(LocalDateTime.now());\n");
            source.append("            return super.eventRecordCreating(record);\n");
            source.append("        }\n");
        }
        source.append("}\n");
        source.append("}");
        return source.toString();
    }

    public void generateModelClassColumn(StringBuilder source, Column column) {
        if (column.primary) {
            source.append("    @Primary\n");
        }
        //列注解
        source.append("    @Column(name = \"").append(column.name).append("\"");
        if (column.nullable) {
            source.append(", nullable = true");
        }
        if (column.length > 0) {
            source.append(", length = ").append(column.length);
        }
        if (column.comment != null) {
            source.append(", comment = \"").append(column.comment).append("\"");
        }
        if (column.type.contains("UNSIGNED")) {
            source.append(", unsigned = true");
        } else if (column.type.contains("TIMESTAMP")) {
            //生成created_at和updated_at
            if (column.name.contains("create")) {
                source.append(", fill = FieldFill.CreatedTimeFill.class");
            } else if (column.name.contains("update")) {
                source.append(", fill = FieldFill.UpdatedTimeFill.class");
            }
        }
        source.append(")\n");
        source.append("    private ");
        if (column.type.contains("VARCHAR") || column.type.contains("LONGTEXT")) {
            source.append("String");
        } else if (column.type.contains("TIMESTAMP")) {
            source.append("LocalDateTime");
        } else if (column.type.contains("INTEGER")) {
            source.append("Integer");
        } else if (column.type.contains("BIGINT")) {
            source.append("Long");
        } else {
            source.append("String");
        }
        source.append(" ").append(StringUtils.underlineToCamelCase(column.name)).append(";\n");
    }
}
