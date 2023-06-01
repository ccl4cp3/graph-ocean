/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain.impl;

import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;
import io.github.anyzm.graph.ocean.domain.GraphLabelBuilder;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;

import java.util.List;
import java.util.Map;

/**
 * Description  GraphEdgeTypeBuilder is used for
 *
 * @author Anyzm
 * Date  2021/9/13 - 16:31
 * @version 1.0.0
 */
public class GraphEdgeTypeBuilder implements GraphLabelBuilder {

    private GraphEdgeType graphEdgeType;

    private GraphEdgeTypeBuilder() {
        this.graphEdgeType = new GraphEdgeType();
    }


    public static GraphEdgeTypeBuilder builder() {
        return new GraphEdgeTypeBuilder();
    }

    @Override
    public GraphEdgeTypeBuilder graphLabelName(String graphLabelName) {
        this.graphEdgeType.setEdgeName(graphLabelName);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder graphLabelComment(String graphLabelComment) {
        this.graphEdgeType.setComment(graphLabelComment);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder graphLabelField(String graphLabelField) {
        // 边类型暂时不支持动态设置
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder labelClass(Class labelClass) {
        this.graphEdgeType.setEdgeClass(labelClass);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder propertyFormatMap(Map<String, GraphValueFormatter> propertyFormatMap) {
        this.graphEdgeType.setPropertyFormatMap(propertyFormatMap);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder dataTypeMap(Map<String, GraphDataTypeEnum> dataTypeMap) {
        this.graphEdgeType.setDataTypeMap(dataTypeMap);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder mustProps(List<String> mustProps) {
        this.graphEdgeType.setMustProperties(mustProps);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder propertyFieldMap(Map<String, String> propertyFieldMap) {
        this.graphEdgeType.setPropertyFieldMap(propertyFieldMap);
        return this;
    }

    @Override
    public GraphLabelBuilder propertyDefaultValueMap(Map<String, String> propertyDefaultValueMap) {
        this.graphEdgeType.setPropertyDefaultValueMap(propertyDefaultValueMap);
        return this;
    }

    @Override
    public GraphLabelBuilder propertyCommentMap(Map<String, String> propertyCommentMap) {
        this.graphEdgeType.setPropertyCommentMap(propertyCommentMap);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder graphKeyPolicy(GraphKeyPolicy graphKeyPolicy) {
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder idAsField(boolean idAsField) {
        this.graphEdgeType.setSrcIdAsField(idAsField);
        this.graphEdgeType.setDstIdAsField(idAsField);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder idValueFormatter(GraphValueFormatter idValueFormatter) {
        this.graphEdgeType.setSrcIdValueFormatter(idValueFormatter);
        this.graphEdgeType.setDstIdValueFormatter(idValueFormatter);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder srcIdAsField(boolean srcIdAsField) {
        this.graphEdgeType.setSrcIdAsField(srcIdAsField);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder dstIdAsField(boolean dstIdAsField) {
        this.graphEdgeType.setDstIdAsField(dstIdAsField);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder srcIdValueFormatter(GraphValueFormatter srcIdValueFormatter) {
        this.graphEdgeType.setSrcIdValueFormatter(srcIdValueFormatter);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder dstIdValueFormatter(GraphValueFormatter dstIdValueFormatter) {
        this.graphEdgeType.setDstIdValueFormatter(dstIdValueFormatter);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder srcGraphVertexType(GraphVertexType srcGraphVertexType) {
        this.graphEdgeType.setSrcVertexType(srcGraphVertexType);
        return this;
    }

    @Override
    public GraphEdgeTypeBuilder dstGraphVertexType(GraphVertexType dstGraphVertexType) {
        this.graphEdgeType.setDstVertexType(dstGraphVertexType);
        return this;
    }

    @Override
    public GraphEdgeType build() {
        return this.graphEdgeType;
    }
}
