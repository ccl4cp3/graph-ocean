/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain.impl;

import com.google.common.base.Objects;
import io.github.anyzm.graph.ocean.domain.GraphRelation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Anyzm
 * date 2019/2/14
 */
@Getter
@ToString
public class GraphEdgeEntity<E> extends GraphPropertyEntity implements GraphRelation {
    /**
     * 起点 id
     */
    private final String srcId;
    /**
     * 终点 id
     */
    private final String dstId;

    private final GraphEdgeType<E> graphEdgeType;

    @Setter
    private int level = 0;

    @Setter
    private boolean ignoreDirect;

    @Override
    public int getHashCode() {
        String startId = this.getSrcId();
        String endId = this.getDstId();
        String edgeName = this.graphEdgeType.getEdgeName();
        return Objects.hashCode(startId, endId, edgeName);
    }

    @Override
    public boolean isEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GraphEdgeEntity graphEdgeEntity = (GraphEdgeEntity) o;

        String startId = this.getSrcId();
        String endId = this.getDstId();
        String edgeName = this.graphEdgeType.getEdgeName();
        return startId.equals(graphEdgeEntity.getSrcId()) &&
                endId.equals(graphEdgeEntity.getDstId())
                && edgeName.equals(graphEdgeEntity.getGraphEdgeType().getEdgeName());
    }

    public GraphEdgeEntity(GraphEdgeType<E> graphEdgeType, String srcId, String dstId, Map<String, Object> props) {
        super(props);
        this.graphEdgeType = graphEdgeType;
        this.srcId = srcId;
        this.dstId = dstId;
    }

    public GraphEdgeEntity(GraphEdgeType<E> graphEdgeType, String srcId, String dstId) {
        super(Collections.emptyMap());
        this.graphEdgeType = graphEdgeType;
        this.srcId = srcId;
        this.dstId = dstId;
    }

    @Override
    public boolean equals(Object o) {
        return this.isEquals(o);
    }

    @Override
    public int hashCode() {
        return this.getHashCode();
    }

    @Override
    public List<GraphEdgeEntity> getEdges() {
        return Collections.singletonList(this);
    }
}
