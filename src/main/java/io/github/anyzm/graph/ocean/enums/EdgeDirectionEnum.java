/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.enums;

import lombok.Getter;

/**
 * Description  EdgeDirectionEnum is used for
 *
 * @author Anyzm
 * Date  2021/8/10 - 14:38
 * @version 1.0.0
 */
public enum EdgeDirectionEnum {

    /**
     * 入边，默认值
     */
    IN_COMING("IN"),
    /**
     * 出边，默认值
     */
    OUT_GOING("OUT"),
    /**
     * 反向，逆向查询，用于GO语句
     */
    REVERSELY("REVERSELY"),
    /**
     * 双向，双向查询，用于GO语句
     */
    BIDIRECT("BIDIRECT"),
    /**
     * 双向，双向查询，用于SUBGRAPH语句
     */
    BOTH("BOTH"),
    ;

    @Getter
    private String word;

    EdgeDirectionEnum(String word) {
        this.word = word;
    }
}
