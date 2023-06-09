/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain.impl;

import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;
import io.github.anyzm.graph.ocean.domain.AbstractGraphLabel;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Description  GraphEdgeType is used for
 *
 * @author Anyzm
 * Date  2021/7/16 - 14:18
 * @version 1.0.0
 */
@Setter
@Getter
public class GraphEdgeType<E> extends AbstractGraphLabel {

    private String edgeName;

    /**
     * 起点顶点主键策略
     */
    private GraphKeyPolicy srcVertexGraphKeyPolicy;

    /**
     * 起终点顶点主键策略
     */
    private GraphKeyPolicy dstVertexGraphKeyPolicy;

    private Class<E> edgeClass;

    private boolean srcIdAsField;

    private boolean dstIdAsField;

    private GraphValueFormatter srcIdValueFormatter;

    private GraphValueFormatter dstIdValueFormatter;

    protected GraphEdgeType() {
    }

    private GraphEdgeType(String edgeName, Class<E> edgeClass, List<String> mustFields, Map<String, String> propertyFieldMap,
                          Map<String, GraphValueFormatter> propertyFormatMap, Map<String, GraphDataTypeEnum> dataTypeMap,
                          GraphKeyPolicy srcVertexGraphKeyPolicy, GraphKeyPolicy dstVertexGraphKeyPolicy,
                          boolean srcIdAsField, boolean dstIdAsField) {
        this.edgeName = edgeName;
        this.edgeClass = edgeClass;
        this.mustProperties = mustFields;
        this.propertyFieldMap = propertyFieldMap;
        this.propertyFormatMap = propertyFormatMap;
        this.dataTypeMap = dataTypeMap;
        this.srcIdAsField = srcIdAsField;
        this.dstIdAsField = dstIdAsField;
        this.srcVertexGraphKeyPolicy = srcVertexGraphKeyPolicy;
        this.dstVertexGraphKeyPolicy = dstVertexGraphKeyPolicy;
    }

    /**
     * 方法内部决定是否需要加工
     *
     * @param vertexKey 原始的id数据
     * @return
     */
    public String getSrcIdKey(String vertexKey) {
        if (srcIdValueFormatter != null) {
            vertexKey = (String) srcIdValueFormatter.format(vertexKey);
        }
        return vertexKey;
    }

    /**
     * 方法内部决定是否需要加工
     *
     * @param vertexKey 原始的id数据
     * @return
     */
    public String getDstIdKey(String vertexKey) {
        if (dstIdValueFormatter != null) {
            vertexKey = (String) dstIdValueFormatter.format(vertexKey);
        }
        return vertexKey;
    }


    @Override
    public boolean isTag() {
        return false;
    }

    @Override
    public boolean isEdge() {
        return true;
    }

    @Override
    public String getName() {
        return this.getEdgeName();
    }


}
