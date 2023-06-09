/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.dao.impl;


import io.github.anyzm.graph.ocean.annotation.GraphEdge;
import io.github.anyzm.graph.ocean.cache.DefaultGraphTypeCache;
import io.github.anyzm.graph.ocean.cache.GraphTypeCache;
import io.github.anyzm.graph.ocean.dao.GraphEdgeTypeFactory;
import io.github.anyzm.graph.ocean.dao.GraphTypeManager;
import io.github.anyzm.graph.ocean.dao.GraphVertexTypeFactory;
import io.github.anyzm.graph.ocean.domain.GraphLabel;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;
import io.github.anyzm.graph.ocean.domain.impl.GraphVertexType;
import io.github.anyzm.graph.ocean.exception.NebulaException;

/**
 * Description  DefaultGraphTypeManager is used for
 *
 * @author Anyzm
 * Date  2021/7/16 - 19:38
 * @version 1.0.0
 */
public class DefaultGraphTypeManager implements GraphTypeManager {

    public DefaultGraphTypeManager() {
        this.graphTypeCache = new DefaultGraphTypeCache();
        this.graphVertexTypeFactory = new DefaultGraphVertexTypeFactory();
        this.graphEdgeTypeFactory = new DefaultGraphEdgeTypeFactory();
    }

    private GraphTypeCache graphTypeCache;

    private GraphVertexTypeFactory graphVertexTypeFactory;

    private GraphEdgeTypeFactory graphEdgeTypeFactory;

    @Override
    public <T> GraphVertexType<T> getGraphVertexType(Class<T> clazz) throws NebulaException {
        if (clazz == null) {
            return null;
        }
        GraphVertexType<T> graphVertexType = graphTypeCache.getGraphVertexType(clazz);
        if (graphVertexType != null) {
            return graphVertexType;
        }
        graphVertexType = graphVertexTypeFactory.buildGraphVertexType(clazz);
        graphTypeCache.putGraphVertexType(clazz, graphVertexType);
        return graphVertexType;
    }

    @Override
    public <E> GraphEdgeType<E> getGraphEdgeType(Class<E> clazz) throws NebulaException {
        if (clazz == null) {
            return null;
        }
        GraphEdgeType graphEdgeType = graphTypeCache.getGraphEdgeType(clazz);
        if (graphEdgeType != null) {
            return graphEdgeType;
        }
        GraphEdge graphEdge = clazz.getAnnotation(GraphEdge.class);
        if (graphEdge == null) {
            return null;
        }

        graphEdgeType = graphEdgeTypeFactory.buildGraphEdgeType(clazz);
        graphTypeCache.putGraphEdgeType(clazz, graphEdgeType);
        return graphEdgeType;
    }

    @Override
    public GraphLabel getGraphLabel(Class clazz) throws NebulaException {
        try {
            GraphEdgeType graphEdgeType = this.getGraphEdgeType(clazz);
            if (graphEdgeType == null) {
                return this.getGraphVertexType(clazz);
            } else {
                return graphEdgeType;
            }
        } catch (NebulaException e) {
            return this.getGraphVertexType(clazz);
        }
    }
}
