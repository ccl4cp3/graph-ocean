/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.engine;

import com.google.common.collect.Lists;
import io.github.anyzm.graph.ocean.common.GraphHelper;
import io.github.anyzm.graph.ocean.common.utils.StringUtil;
import io.github.anyzm.graph.ocean.dao.EdgeUpdateEngine;
import io.github.anyzm.graph.ocean.domain.GraphLabel;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeEntity;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;
import io.github.anyzm.graph.ocean.domain.impl.GraphVertexEntity;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import io.github.anyzm.graph.ocean.exception.CheckThrower;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 批量边更新引擎
 *
 * @author Anyzm
 * date 2020/3/30
 */
public class NebulaBatchEdgesUpdate<S,D,E> implements EdgeUpdateEngine<E> {

    private static final String UPSET_SQL_FORMAT = "UPSERT EDGE ON %s %s -> %s SET %s";

    private List<GraphEdgeEntity<E>> graphEdgeEntities;

    private List<GraphVertexEntity<S>> srcGraphVertexEntities;

    private List<GraphVertexEntity<D>> dstGraphVertexEntities;

    public NebulaBatchEdgesUpdate(List<GraphEdgeEntity<E>> graphEdgeEntities) throws NebulaException {
        CheckThrower.ifTrueThrow(CollectionUtils.isEmpty(graphEdgeEntities), ErrorEnum.UPDATE_FIELD_DATA_NOT_EMPTY);
        this.graphEdgeEntities = graphEdgeEntities;
    }

    public NebulaBatchEdgesUpdate(List<GraphEdgeEntity<E>> graphEdgeEntities, List<GraphVertexEntity<S>> srcGraphVertexEntities,
                                  List<GraphVertexEntity<D>> dstGraphVertexEntities) throws NebulaException {
        CheckThrower.ifTrueThrow(CollectionUtils.isEmpty(graphEdgeEntities), ErrorEnum.UPDATE_FIELD_DATA_NOT_EMPTY);
        this.graphEdgeEntities = graphEdgeEntities;
        this.srcGraphVertexEntities = srcGraphVertexEntities;
        this.dstGraphVertexEntities = dstGraphVertexEntities;
    }

    @Override
    public List<String> getSqlList() throws NebulaException {
        List<String> sqlList = getEdgeSql();
        sqlList.addAll(this.getSrcVertexSql());
        sqlList.addAll(this.getDstVertexSql());
        return sqlList;
    }

    private List<String> getEdgeSql() throws NebulaException {
        if (this.graphEdgeEntities.size() == 1) {
            String sql = getOneSql();
            return Lists.newArrayList(sql);
        }
        return getMultiSql();
    }

    private List<String> getSrcVertexSql() throws NebulaException {
        if (CollectionUtils.isNotEmpty(this.srcGraphVertexEntities)) {
            NebulaBatchVertexUpdate<S> nebulaUpdateBatchVertex = new NebulaBatchVertexUpdate(this.srcGraphVertexEntities);
            return nebulaUpdateBatchVertex.getSqlList();
        }
        return Collections.emptyList();
    }

    private List<String> getDstVertexSql() throws NebulaException {
        if (CollectionUtils.isNotEmpty(dstGraphVertexEntities)) {
            NebulaBatchVertexUpdate<D> nebulaUpdateBatchVertex = new NebulaBatchVertexUpdate(dstGraphVertexEntities);
            return nebulaUpdateBatchVertex.getSqlList();
        }
        return Collections.emptyList();
    }

    /**
     * 获取单边sql
     *
     * @return
     */
    private String getOneSql() throws NebulaException {
        return generateSql(this.graphEdgeEntities.get(0));
    }

    /**
     * 获取多边sql
     *
     * @return
     */
    private List<String> getMultiSql() throws NebulaException {
        List<String> sqlList = Lists.newArrayListWithExpectedSize(this.graphEdgeEntities.size());
        for (GraphEdgeEntity<E> graphEdgeEntity : this.graphEdgeEntities) {
            String sql = generateSql(graphEdgeEntity);
            sqlList.add(sql);
        }
        return StringUtil.aggregate(sqlList, sqlList.size(), ";");
    }

    private String generateSql(GraphEdgeEntity<E> graphEdgeEntity) throws NebulaException {
        String src = GraphHelper.getQuerySrcId(graphEdgeEntity.getGraphEdgeType(), graphEdgeEntity.getSrcId());
        String end = GraphHelper.getQueryDstId(graphEdgeEntity.getGraphEdgeType(), graphEdgeEntity.getDstId());
        Set<Map.Entry<String, Object>> entries = graphEdgeEntity.getProps().entrySet();
        StringBuilder sqlBuilder = new StringBuilder();
        Map<String, GraphDataTypeEnum> dataTypeMap = graphEdgeEntity.getGraphEdgeType().getDataTypeMap();
        for (Map.Entry<String, Object> entry : entries) {
            GraphDataTypeEnum graphDataTypeEnum = dataTypeMap.get(entry.getKey());
            if(null == graphDataTypeEnum || GraphDataTypeEnum.NULL.equals(graphDataTypeEnum)) {
                continue;
            }

            switch (graphDataTypeEnum) {
                case STRING:
                case FIXED_STRING:
                    sqlBuilder.append(",").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                    break;
                default:
                    sqlBuilder.append(",").append(entry.getKey()).append("=").append(entry.getValue());
                    break;
            }
        }
        String sqlFieldSet = sqlBuilder.delete(0, 1).toString();
        return String.format(UPSET_SQL_FORMAT, graphEdgeEntity.getEdgeName(), src, end, sqlFieldSet);
    }


    @Override
    public List<GraphEdgeEntity<E>> getGraphEdgeEntityList() {
        return this.graphEdgeEntities;
    }

    @Override
    public GraphEdgeType<E> getGraphEdgeType() {
        return this.graphEdgeEntities.get(0).getGraphEdgeType();
    }

    @Override
    public List<GraphLabel> getLabels() {
        List<GraphLabel> list = Lists.newArrayList();
        list.add(this.getGraphEdgeType());
        if(CollectionUtils.isNotEmpty(srcGraphVertexEntities)) {
            list.addAll(srcGraphVertexEntities.stream().map(GraphVertexEntity::getGraphVertexType).collect(Collectors.toList()));
        }
        if(CollectionUtils.isNotEmpty(dstGraphVertexEntities)) {
            list.addAll(dstGraphVertexEntities.stream().map(GraphVertexEntity::getGraphVertexType).collect(Collectors.toList()));
        }
        return list;
    }

}
