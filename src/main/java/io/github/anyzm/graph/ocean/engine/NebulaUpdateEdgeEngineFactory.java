/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.engine;

import io.github.anyzm.graph.ocean.dao.EdgeUpdateEngine;
import io.github.anyzm.graph.ocean.dao.GraphUpdateEdgeEngineFactory;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeEntity;
import io.github.anyzm.graph.ocean.domain.impl.GraphVertexEntity;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Description  NebulaUpdateEdgeEngineFactory is used for
 *
 * @author Anyzm
 * Date  2021/7/19 - 10:52
 * @version 1.0.0
 */
@Slf4j
public class NebulaUpdateEdgeEngineFactory implements GraphUpdateEdgeEngineFactory {

    @Override
    public <E> EdgeUpdateEngine<E> build(List<GraphEdgeEntity<E>> graphEdgeEntities) throws NebulaException {
        return new NebulaBatchEdgesUpdate<>(graphEdgeEntities);
    }

    @Override
    public <S, D, E> EdgeUpdateEngine<E> build(List<GraphEdgeEntity<E>> graphEdgeEntities,
                                                     List<GraphVertexEntity<S>> srcGraphVertexEntities,
                                                     List<GraphVertexEntity<D>> dstGraphVertexEntities) throws NebulaException {
        return new NebulaBatchEdgesUpdate<>(graphEdgeEntities, srcGraphVertexEntities, dstGraphVertexEntities);
    }

}
