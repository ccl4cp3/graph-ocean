/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.annotation;

import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;

import java.lang.annotation.*;

/**
 * 业务说明：标注边类型
 *
 * @author Anyzm
 * date 2021/4/28
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphEdge {

    /**
     *
     * @return 边名称
     */
    String value();

    /**
     *
     * @return 边起点主键生成方法
     */
    GraphKeyPolicy srcKeyPolicy() default GraphKeyPolicy.string_key;

    /**
     *
     * @return 边终点主键生成方法
     */
    GraphKeyPolicy dstKeyPolicy() default GraphKeyPolicy.string_key;

    /**
     *
     * @return 起点id是否作为字段
     */
    boolean srcIdAsField() default true;

    /**
     *
     * @return 末尾id是否作为字段
     */
    boolean dstIdAsField() default true;

    /**
     * 用于创建edge
     * @return 注释信息
     */
    String comment() default "";
}
