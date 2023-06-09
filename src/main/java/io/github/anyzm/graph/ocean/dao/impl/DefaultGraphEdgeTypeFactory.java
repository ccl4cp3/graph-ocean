/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.dao.impl;

import io.github.anyzm.graph.ocean.annotation.GraphEdge;
import io.github.anyzm.graph.ocean.common.GraphHelper;
import io.github.anyzm.graph.ocean.dao.GraphEdgeTypeFactory;
import io.github.anyzm.graph.ocean.dao.GraphVertexTypeFactory;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeTypeBuilder;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;
import io.github.anyzm.graph.ocean.exception.CheckThrower;
import io.github.anyzm.graph.ocean.exception.NebulaException;

/**
 * Description  DefaultGraphEdgeTypeFactory is used for
 * 默认的图边类型工厂类
 *
 * @author Anyzm
 * Date  2021/7/16 - 16:58
 * @version 1.0.0
 */
public class DefaultGraphEdgeTypeFactory implements GraphEdgeTypeFactory {

    private GraphVertexTypeFactory graphVertexTypeFactory;

    public DefaultGraphEdgeTypeFactory() {
        this.graphVertexTypeFactory = new DefaultGraphVertexTypeFactory();
    }

    @Override
    public <E> GraphEdgeType<E> buildGraphEdgeType(Class<E> clazz) throws NebulaException {
        GraphEdge graphEdge = clazz.getAnnotation(GraphEdge.class);
        CheckThrower.ifTrueThrow(graphEdge == null, ErrorEnum.PARAMETER_NOT_NULL);
        String edgeName = graphEdge.value();
        String edgeComment = graphEdge.comment();
        boolean srcIdAsField = graphEdge.srcIdAsField();
        boolean dstIdAsField = graphEdge.dstIdAsField();
        GraphKeyPolicy srcVertexGraphKeyPolicy = graphEdge.srcKeyPolicy();
        GraphKeyPolicy dstVertexGraphKeyPolicy = graphEdge.dstKeyPolicy();

        GraphEdgeTypeBuilder builder = GraphEdgeTypeBuilder.builder();
        GraphHelper.collectGraphProperties(builder, clazz, srcIdAsField, dstIdAsField);
        return builder
                .srcIdAsField(srcIdAsField)
                .dstIdAsField(dstIdAsField)
                .srcVertexGraphKeyPolicy(srcVertexGraphKeyPolicy)
                .dstVertexGraphKeyPolicy(dstVertexGraphKeyPolicy)
                .graphLabelName(edgeName)
                .graphLabelComment(edgeComment)
                .labelClass(clazz)
                .build();
    }
}
