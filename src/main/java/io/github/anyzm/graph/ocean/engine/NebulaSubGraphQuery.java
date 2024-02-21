package io.github.anyzm.graph.ocean.engine;

import io.github.anyzm.graph.ocean.common.GraphHelper;
import io.github.anyzm.graph.ocean.common.utils.CollectionUtils;
import io.github.anyzm.graph.ocean.domain.GraphCondition;
import io.github.anyzm.graph.ocean.enums.EdgeDirectionEnum;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 子图查询
 */
public class NebulaSubGraphQuery {

    /**
     * 默认只查一跳
     */
    @Setter
    private int stepCount = 1;

    /**
     * 是否包含属性信息
     */
    @Setter
    private boolean withProperties = true;

    /**
     * 查询条件
     */
    @Setter
    private GraphCondition graphCondition;

    /**
     * 只查边
     */
    @Setter
    private boolean onlyEdge = false;

    /**
     * 边方向map
     */
    @Setter
    private Map<EdgeDirectionEnum, List<String>> edgeDirectionMap;

    /**
     * 起点VID列表
     */
    private List<String> startVidList;

    public NebulaSubGraphQuery(List<String> vertexIdList) {
        this(GraphKeyPolicy.string_key, vertexIdList);
    }

    public NebulaSubGraphQuery(GraphKeyPolicy graphKeyPolicy, List<String> vertexIdList) {
        if(CollectionUtils.isEmpty(vertexIdList)) {
            throw new NebulaException(ErrorEnum.INVALID_ID);
        }

        this.startVidList = new ArrayList<>(vertexIdList.size());
        for(String vertexId : vertexIdList) {
            this.startVidList.add(GraphHelper.generateKeyPolicy(graphKeyPolicy, vertexId));
        }
    }

    public String buildQuerySql() {
        StringBuilder sqlBuilder = buildBaseQql();
        if(onlyEdge) {
            sqlBuilder.append(" YIELD EDGES AS relationships");
        }else {
            sqlBuilder.append(" YIELD VERTICES AS nodes, EDGES AS relationships");
        }
        return sqlBuilder.toString();
    }

    private StringBuilder buildBaseQql() {
        StringBuilder sqlBuilder = new StringBuilder("GET SUBGRAPH ");
        if(withProperties) {
            sqlBuilder.append("WITH PROP ");
        }
        sqlBuilder.append(stepCount).append(" STEPS ");
        sqlBuilder.append("FROM ").append(StringUtils.join(startVidList, ","));
        // 指定边类型的方向，顺序必须是IN->OUT->BOTH
        if (MapUtils.isNotEmpty(edgeDirectionMap)) {
            List<String> inEdgeTypeList = edgeDirectionMap.get(EdgeDirectionEnum.IN_COMING);
            if(CollectionUtils.isNotEmpty(inEdgeTypeList)) {
                sqlBuilder.append(" ").append(EdgeDirectionEnum.IN_COMING.getWord()).append(" ").append(StringUtils.join(inEdgeTypeList, ","));
            }
            List<String> outEdgeTypeList = edgeDirectionMap.get(EdgeDirectionEnum.OUT_GOING);
            if(CollectionUtils.isNotEmpty(outEdgeTypeList)) {
                sqlBuilder.append(" ").append(EdgeDirectionEnum.OUT_GOING.getWord()).append(" ").append(StringUtils.join(outEdgeTypeList, ","));
            }
            List<String> bothEdgeTypeList = edgeDirectionMap.get(EdgeDirectionEnum.BOTH);
            if(CollectionUtils.isNotEmpty(bothEdgeTypeList)) {
                sqlBuilder.append(" ").append(EdgeDirectionEnum.BOTH.getWord()).append(" ").append(StringUtils.join(bothEdgeTypeList, ","));
            }
        }
        if(null != graphCondition) {
            NebulaQueryUtils.where(sqlBuilder, graphCondition);
        }
        return sqlBuilder;
    }

}
