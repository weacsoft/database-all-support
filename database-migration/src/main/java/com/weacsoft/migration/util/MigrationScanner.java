package com.weacsoft.migration.util;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

//迁移源代码加载到内存并编译
public class MigrationScanner {
    //比较特殊，因此不用额外库的类加载器，而是自己写一个加载器，用来从内存加载
    //类存储器，存储类名和源代码
    private final Map<String, byte[]> compiledClasses = new ConcurrentHashMap<>();
    //内存加载器
    private MemoryClassLoader memoryClassLoader;

    public MemoryClassLoader getMemoryClassLoader() {
        if (memoryClassLoader == null) {
            memoryClassLoader = new MemoryClassLoader();
        }
        return memoryClassLoader;
    }

    public void removeMemoryClassLoader() {
        memoryClassLoader = null;
    }

    /**
     * 获得所有记录了类的类名
     *
     * @return
     */
    public List<String> getCompiledClasses() {
        return new ArrayList<>(compiledClasses.keySet());
    }

    /**
     * 从内存里获得编译的类
     *
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public Class<?> getCompiledClass(String className) throws ClassNotFoundException {
        return getMemoryClassLoader().loadClass(className);
    }

    /**
     * 编译一个目录的代码
     *
     * @param dir
     * @throws Exception
     */
    public void compileFromDirectory(File dir) throws Exception {
        if (dir.exists() && dir.isDirectory()) {
            List<File> resultFiles = new ArrayList<>();
            // 扫描目录下的.java文件
            try (Stream<Path> pathStream = Files.list(dir.toPath())) {
                pathStream
                        .filter(Files::isRegularFile)  // 仅保留文件（排除目录）
                        .filter(path -> {
                            String fileName = path.getFileName().toString();
                            return fileName.endsWith(".java");  // 过滤指定后缀
                        })
                        .forEach(path -> resultFiles.add(path.toFile()));  // 收集结果
            }
            for (File file : resultFiles) {
                compileFromFile(file);
            }
        } else {
            throw new RuntimeException("目录不存在或不是目录：" + dir.getAbsolutePath());
        }
    }

    /**
     * 编译一个目录的代码
     *
     * @param directory
     * @throws Exception
     */
    public void compileFromDirectory(String directory) throws Exception {
        File dir = new File(directory);
        compileFromDirectory(dir);
    }

    /**
     * 编译一个文件的代码
     *
     * @param file
     * @throws Exception
     */
    public void compileFromFile(File file) throws Exception {
        if (file.exists() && file.isFile()) {
            //读取源代码
            String sourceCode = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (sourceCode == null || sourceCode.isEmpty()) {
                throw new IOException("无法读取Java文件或文件内容为空: " + file.getAbsolutePath());
            }
            // 处理源代码，拼接成标准的类名加包名
            String codeWithoutCommentsAndStrings = removeCommentsAndStrings(sourceCode);
            String packageName = extractPackageName(codeWithoutCommentsAndStrings);
            String simpleClassName = extractClassName(file.getAbsolutePath());
            String fullClassName = packageName.isEmpty() ? simpleClassName : packageName + "." + simpleClassName;
            // 获取系统编译器
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException("无法获取Java编译器，请确保使用JDK而非JRE运行程序");
            }
            // 创建诊断监听器，捕获编译错误
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            // 内存编译和加载
            try (MemoryFileManager fileManager = new MemoryFileManager(compiler.getStandardFileManager(diagnostics, null, null))) {

                List<JavaFileObject> compilationUnits = new ArrayList<>();
                compilationUnits.add(new SourceCodeJavaFileObject(fullClassName, sourceCode));

                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

                Boolean success = task.call();

                // 检查编译错误
                if (success == null || !success) {
                    StringBuilder errorMsg = new StringBuilder("编译错误: ");
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        errorMsg.append(String.format("\n第%d行: %s", diagnostic.getLineNumber(), diagnostic.getMessage(null)));
                    }
                    throw new RuntimeException(errorMsg.toString());
                }

                // 从内存文件管理器中获取编译后的类字节码，并加入到搜索器里
                for (String name : fileManager.getGeneratedClassNames()) {
                    compiledClasses.put(name, fileManager.getGeneratedClass(name));
                }
            }
        } else {
            throw new RuntimeException("文件不存在或不是文件：" + file.getAbsolutePath());
        }
    }

    /**
     * 编译一个文件的代码
     *
     * @param filePath
     * @throws Exception
     */
    public void compileFromFile(String filePath) throws Exception {
        compileFromFile(new File(filePath));
    }

    /**
     * 从处理后的源代码中提取包名（已移除注释和字符串）
     */
    private String extractPackageName(String processedSourceCode) {
        // 只匹配文件开头的package声明（忽略前导空白）
        Pattern pattern = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
        Matcher matcher = pattern.matcher(processedSourceCode);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 移除源代码中的注释和字符串常量，避免干扰包名解析
     */
    private String removeCommentsAndStrings(String sourceCode) {
        StringBuilder result = new StringBuilder();
        int length = sourceCode.length();
        int i = 0;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        boolean inString = false;
        char stringDelimiter = '"'; // 字符串分隔符，可能是"或'

        while (i < length) {
            char c = sourceCode.charAt(i);

            // 处理注释和字符串状态切换
            if (!inSingleLineComment && !inMultiLineComment && !inString) {
                // 检查单行注释
                if (c == '/' && i + 1 < length && sourceCode.charAt(i + 1) == '/') {
                    inSingleLineComment = true;
                    i += 2;
                    continue;
                }
                // 检查多行注释
                else if (c == '/' && i + 1 < length && sourceCode.charAt(i + 1) == '*') {
                    inMultiLineComment = true;
                    i += 2;
                    continue;
                }
                // 检查字符串开始
                else if (c == '"' || c == '\'') {
                    inString = true;
                    stringDelimiter = c;
                    i++;
                    continue;
                }
            }
            // 单行注释结束（遇到换行）
            else if (inSingleLineComment) {
                if (c == '\n' || c == '\r') {
                    inSingleLineComment = false;
                }
                i++;
                continue;
            }
            // 多行注释结束
            else if (inMultiLineComment) {
                if (c == '*' && i + 1 < length && sourceCode.charAt(i + 1) == '/') {
                    inMultiLineComment = false;
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }
            // 字符串结束
            else if (inString) {
                if (c == stringDelimiter) {
                    // 处理转义的分隔符（如\"或\'）
                    if (i > 0 && sourceCode.charAt(i - 1) != '\\') {
                        inString = false;
                    }
                }
                i++;
                continue;
            }

            // 只保留非注释和非字符串的内容
            result.append(c);
            i++;
        }

        return result.toString();
    }

    private String extractClassName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }


    public void removeAll() {
        compiledClasses.clear();
    }

    /**
     * 自定义内存文件管理器，收集编译后的类字节码
     */
    private static class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        // 存储生成的类文件
        private final Map<String, ClassFileJavaFileObject> generatedClasses = new ConcurrentHashMap<>();

        public MemoryFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                ClassFileJavaFileObject classFile = new ClassFileJavaFileObject(className);
                generatedClasses.put(className, classFile);
                return classFile;
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        // 获取生成的类名列表
        public List<String> getGeneratedClassNames() {
            return new ArrayList<>(generatedClasses.keySet());
        }

        // 获取生成的类字节码
        public byte[] getGeneratedClass(String className) {
            ClassFileJavaFileObject classFile = generatedClasses.get(className);
            if (classFile != null) {
                return classFile.getBytes();
            }
            return null;
        }
    }

    /**
     * 内存中的源代码对象
     */
    private static class SourceCodeJavaFileObject extends SimpleJavaFileObject {
        private final String sourceCode;

        SourceCodeJavaFileObject(String className, String sourceCode) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.sourceCode = sourceCode;
        }

        @Override
        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            return CharBuffer.wrap(sourceCode);
        }
    }

    /**
     * 内存中的class文件对象（存储编译后的字节码）
     */
    private static class ClassFileJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ClassFileJavaFileObject(String className) {
            super(URI.create("bytes:///" + className.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() {
            return outputStream;
        }

        // 获取字节码
        public byte[] getBytes() {
            return outputStream.toByteArray();
        }
    }

    /**
     * 自定义类加载器，从内存读取class字节码
     */
    public class MemoryClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] classData = compiledClasses.get(name);
            if (classData == null) {
                return super.findClass(name);
            }
            return defineClass(name, classData, 0, classData.length);
        }
    }
}
