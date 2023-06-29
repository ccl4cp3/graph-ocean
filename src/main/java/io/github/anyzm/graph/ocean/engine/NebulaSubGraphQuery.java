package io.github.anyzm.graph.ocean.engine;

import io.github.anyzm.graph.ocean.common.GraphHelper;
import io.github.anyzm.graph.ocean.common.utils.CollectionUtils;
import io.github.anyzm.graph.ocean.domain.GraphCondition;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
     * 起始点VID列表
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
        if(null != graphCondition) {
            NebulaQueryUtils.where(sqlBuilder, graphCondition);
        }
        return sqlBuilder;
    }

}
