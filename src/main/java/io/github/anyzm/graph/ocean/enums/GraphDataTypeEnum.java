/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Description  GraphDataTypeEnum is used for
 * 图数据库数据类型
 *
 * @author Anyzm
 * Date  2021/7/19 - 17:13
 * @version 1.0.0
 * @update chenui
 * @date 2022/08/31
 */
public enum GraphDataTypeEnum {
    /**
     * 变长字符串
     */
    STRING("string", "String"),
    /**
     * 定长字符串
     */
    FIXED_STRING("fixed_string(%d)", "String"),
    /**
     * 64位整数
     */
    INT64("int64", "long"),
    /**
     * 16位整数
     */
    INT16("int16", "int"),
    /**
     * 浮点型
     */
    DOUBLE("double", "double"),
    /**
     * 时间戳
     */
    TIMESTAMP("timestamp", "Date"),
    /**
     * 日期
     */
    DATE("date", "Date"),
    /**
     * 日期+时间
     */
    DATETIME("datetime", "Date"),
    /**
     * 布尔值
     */
    BOOLEAN("bool", "boolean"),
    /**
     * 空值
     */
    NULL("null", "null"),
    ;

    @Getter
    private String nebulaType;
    @Getter
    private String javaType;

    GraphDataTypeEnum(String nebulaType, String javaType) {
        this.nebulaType = nebulaType;
        this.javaType = javaType;
    }

    public static GraphDataTypeEnum findByJavaType(String javaType) {
        if(StringUtils.isBlank(javaType)) {
            return GraphDataTypeEnum.NULL;
        }

        // 使用变长字符串
        if(StringUtils.equals("String", javaType)) {
            return GraphDataTypeEnum.STRING;
        }
        // 日期使用时间戳存储
        if(StringUtils.equals("Date", javaType)) {
            return GraphDataTypeEnum.TIMESTAMP;
        }
        // Integer封装类型
        if(StringUtils.equals("Integer", javaType)) {
            return GraphDataTypeEnum.INT16;
        }

        return Arrays.stream(GraphDataTypeEnum.values())
                .filter(item -> StringUtils.equalsIgnoreCase(item.getJavaType(), javaType))
                .findFirst().orElse(GraphDataTypeEnum.NULL);
    }
}
