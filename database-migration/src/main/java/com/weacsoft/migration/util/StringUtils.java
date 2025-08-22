package com.weacsoft.migration.util;

public class StringUtils {
    /**
     * 下划线转小驼峰 (如: user_name -> userName)
     */
    public static String underlineToCamelCase(String underlineStr) {
        if (underlineStr == null || underlineStr.isEmpty()) {
            return underlineStr;
        }

        String[] parts = underlineStr.split("_");
        StringBuilder camelCaseBuilder = new StringBuilder();

        // 处理第一个部分
        if (parts.length > 0 && !parts[0].isEmpty()) {
            camelCaseBuilder.append(parts[0].toLowerCase());
        }

        // 处理后续部分
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            camelCaseBuilder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase());
        }

        return camelCaseBuilder.toString();
    }
    /**
     * 小驼峰转下划线 (如: userName -> user_name)
     */
    public static String camelCaseToUnderline(String camelCaseStr) {
        if (camelCaseStr == null || camelCaseStr.isEmpty()) {
            return camelCaseStr;
        }
        StringBuilder underlineBuilder = new StringBuilder();
        underlineBuilder.append(Character.toLowerCase(camelCaseStr.charAt(0)));

        for (int i = 1; i < camelCaseStr.length(); i++) {
            char c = camelCaseStr.charAt(i);
            if (Character.isUpperCase(c)) {
                underlineBuilder.append("_").append(Character.toLowerCase(c));
            } else {
                underlineBuilder.append(c);
            }
        }

        return underlineBuilder.toString();
    }
    /**
     * 下划线转大驼峰 (如: user_name -> UserName)
     */
    public static String underlineToPascalCase(String underlineStr) {
        if (underlineStr == null || underlineStr.isEmpty()) {
            return underlineStr;
        }

        // 先转成小驼峰，再将首字母大写
        String camelCase = underlineToCamelCase(underlineStr);
        if (camelCase.isEmpty()) {
            return camelCase;
        }

        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
    /**
     * 大驼峰转下划线 (如: UserName -> user_name)
     */
    public static String pascalCaseToUnderline(String pascalCaseStr) {
        if (pascalCaseStr == null || pascalCaseStr.isEmpty()) {
            return pascalCaseStr;
        }

        // 先转成小驼峰，再转下划线
        String camelCase = pascalCaseToCamelCase(pascalCaseStr);
        return camelCaseToUnderline(camelCase);
    }
    /**
     * 小驼峰转大驼峰 (如: userName -> UserName)
     */
    public static String camelCaseToPascalCase(String camelCaseStr) {
        if (camelCaseStr == null || camelCaseStr.isEmpty()) {
            return camelCaseStr;
        }

        return Character.toUpperCase(camelCaseStr.charAt(0)) + camelCaseStr.substring(1);
    }
    /**
     * 大驼峰转小驼峰 (如: UserName -> userName)
     */
    public static String pascalCaseToCamelCase(String pascalCaseStr) {
        if (pascalCaseStr == null || pascalCaseStr.isEmpty()) {
            return pascalCaseStr;
        }

        return Character.toLowerCase(pascalCaseStr.charAt(0)) + pascalCaseStr.substring(1);
    }
}
