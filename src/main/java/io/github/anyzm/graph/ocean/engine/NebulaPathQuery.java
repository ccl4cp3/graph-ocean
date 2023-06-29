package io.github.anyzm.graph.ocean.engine;

import io.github.anyzm.graph.ocean.common.GraphHelper;
import io.github.anyzm.graph.ocean.common.utils.CollectionUtils;
import io.github.anyzm.graph.ocean.domain.GraphCondition;
import io.github.anyzm.graph.ocean.enums.EdgeDirectionEnum;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;
import io.github.anyzm.graph.ocean.enums.PathQueryTypeEnum;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 路径查询
 */
public class NebulaPathQuery {

    /**
     * 是否包含属性信息
     */
    @Setter
    private boolean withProperties = true;

    /**
     * 边的方向
     */
    @Setter
    private EdgeDirectionEnum edgeDirection = EdgeDirectionEnum.BIDIRECT;

    @Setter
    private PathQueryTypeEnum pathQueryType = PathQueryTypeEnum.NO_LOOP;

    /**
     * 查询条件，仅支持过滤边属性
     */
    @Setter
    private GraphCondition graphCondition;

    /**
     * 指定边类型，如果为空则不指定
     */
    @Setter
    private List<String> edgeTypeList;

    /**
     * 起点VID列表
     */
    private List<String> startVidList;

    /**
     * 终点VID列表
     */
    private List<String> endVidList;


    public NebulaPathQuery(List<String> startVertexIdList, List<String> endVertexIdList) {
        this(GraphKeyPolicy.string_key, startVertexIdList, endVertexIdList);
    }

    public NebulaPathQuery(GraphKeyPolicy graphKeyPolicy, List<String> startVertexIdList, List<String> endVertexIdList) {
        if(CollectionUtils.isEmpty(startVertexIdList) || CollectionUtils.isEmpty(endVertexIdList)) {
            throw new NebulaException(ErrorEnum.INVALID_ID);
        }

        this.startVidList = new ArrayList<>(startVertexIdList.size());
        for(String vertexId : startVertexIdList) {
            this.startVidList.add(GraphHelper.generateKeyPolicy(graphKeyPolicy, vertexId));
        }
        this.endVidList = new ArrayList<>(endVertexIdList.size());
        for(String vertexId : endVertexIdList) {
            this.endVidList.add(GraphHelper.generateKeyPolicy(graphKeyPolicy, vertexId));
        }
    }

    public String buildQuerySql() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("FIND ").append(pathQueryType.getKeyWord()).append(" PATH ");
        if(withProperties) {
            sqlBuilder.append("WITH PROP ");
        }
        sqlBuilder.append("FROM ").append(StringUtils.join(startVidList, ","));
        sqlBuilder.append(" TO ").append(StringUtils.join(endVidList, ","));
        sqlBuilder.append(" OVER ");
        if(CollectionUtils.isEmpty(edgeTypeList)) {
            sqlBuilder.append(" *");
        }else {
            sqlBuilder.append(StringUtils.join(edgeTypeList, ","));
        }
        sqlBuilder.append(" ").append(edgeDirection.getWord());
        if(null != graphCondition) {
            NebulaQueryUtils.where(sqlBuilder, graphCondition);
        }
        sqlBuilder.append(" YIELD path AS p");

        return sqlBuilder.toString();
    }
}
