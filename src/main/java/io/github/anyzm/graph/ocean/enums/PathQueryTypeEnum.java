package io.github.anyzm.graph.ocean.enums;

import lombok.Getter;

/**
 * 路径查询类型枚举
 */
public enum PathQueryTypeEnum {

    /**
     * 最短路径
     */
    SHORTEST("SHORTEST"),
    /**
     * 非循环路径
     */
    NO_LOOP("NOLOOP"),
    /**
     * 所有路径
     */
    ALL("ALL");

    @Getter
    private String keyWord;

    PathQueryTypeEnum(String keyWord) {
        this.keyWord = keyWord;
    }
}
