/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain;

import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;

import java.util.List;
import java.util.Map;

/**
 * Description  GraphLabelBuilder is used for
 *
 * @author Anyzm
 * Date  2021/9/13 - 16:08
 * @version 1.0.0
 */
public interface GraphLabelBuilder {

    /**
     * 构造元素名
     *
     * @param graphLabelName 标签名
     * @return 标签构造器
     */
    public GraphLabelBuilder graphLabelName(String graphLabelName);

    /**
     * 构造元素名
     *
     * @param graphLabelComment 标签注释
     * @return 标签构造器
     */
    public GraphLabelBuilder graphLabelComment(String graphLabelComment);

    /**
     * 用于动态构造元素名
     *
     * @param graphLabelField 标签字段
     * @return 标签构造器
     */
    public GraphLabelBuilder graphLabelField(String graphLabelField);

    /**
     * 构造元素对应的类
     *
     * @param labelClass 标签类类型
     * @return 标签构造器
     */
    public GraphLabelBuilder labelClass(Class labelClass);

    /**
     * 构造属性格式化map
     *
     * @param propertyFormatMap 属性格式化map
     * @return 标签构造器
     */
    public GraphLabelBuilder propertyFormatMap(Map<String, GraphValueFormatter> propertyFormatMap);

    /**
     * 构造数据类型map
     *
     * @param dataTypeMap 字段类型map
     * @return 标签构造器
     */
    public GraphLabelBuilder dataTypeMap(Map<String, GraphDataTypeEnum> dataTypeMap);

    /**
     * 构造必要属性
     *
     * @param mustProps 必须属性
     * @return 标签构造器
     */
    public GraphLabelBuilder mustProps(List<String> mustProps);

    /**
     * 构造属性字段map
     *
     * @param propertyFieldMap 属性字段map
     * @return 标签构造器
     */
    public GraphLabelBuilder propertyFieldMap(Map<String, String> propertyFieldMap);

    /**
     * 构造属性默认值map
     * @param propertyDefaultValueMap
     * @return 标签构造器
     */
    public GraphLabelBuilder propertyDefaultValueMap(Map<String, String> propertyDefaultValueMap);

    /**
     * 构造属性注释map
     * @param propertyCommentMap
     * @return 标签构造器
     */
    public GraphLabelBuilder propertyCommentMap(Map<String, String> propertyCommentMap);

    /**
     * 构造主键策略
     *
     * @param graphKeyPolicy 逐渐策略
     * @return 标签构造器
     */
    public GraphLabelBuilder graphKeyPolicy(GraphKeyPolicy graphKeyPolicy);

    /**
     * 构造id是否作为字段
     *
     * @param idAsField id是否作为字段
     * @return 标签构造器
     */
    public GraphLabelBuilder idAsField(boolean idAsField);

    /**
     * 构造id值格式转化器
     *
     * @param idValueFormatter 主键格式转化器
     * @return 标签构造器
     */
    public GraphLabelBuilder idValueFormatter(GraphValueFormatter idValueFormatter);


    /**
     * 构造起点id是否作为字段
     *
     * @param srcIdAsField 起点id是否作为字段
     * @return 标签构造器
     */
    public GraphLabelBuilder srcIdAsField(boolean srcIdAsField);

    /**
     * 构造目标id是否作为字段
     *
     * @param dstIdAsField 终点id是否作为字段
     * @return 标签构造器
     */
    public GraphLabelBuilder dstIdAsField(boolean dstIdAsField);

    /**
     * 构造起点id值格式转化器
     *
     * @param srcIdValueFormatter 起点id格式化器
     * @return 标签构造器
     */
    public GraphLabelBuilder srcIdValueFormatter(GraphValueFormatter srcIdValueFormatter);

    /**
     * 构造目标id值格式转化器
     *
     * @param dstIdValueFormatter 终点id格式化器
     * @return 标签构造器
     */
    public GraphLabelBuilder dstIdValueFormatter(GraphValueFormatter dstIdValueFormatter);

    /**
     * 构造起点顶点类型
     *
     * @param srcVertexGraphKeyPolicy 起点顶点主键策略
     * @return 标签构造器
     */
    public GraphLabelBuilder srcVertexGraphKeyPolicy(GraphKeyPolicy srcVertexGraphKeyPolicy);

    /**
     * 构造目标顶点类型
     *
     * @param dstVertexGraphKeyPolicy 终点顶点主键策略
     * @return 标签构造器
     */
    public GraphLabelBuilder dstVertexGraphKeyPolicy(GraphKeyPolicy dstVertexGraphKeyPolicy);

    /**
     * 构造出真实的元素
     *
     * @return 标签
     */
    public GraphLabel build();

}
