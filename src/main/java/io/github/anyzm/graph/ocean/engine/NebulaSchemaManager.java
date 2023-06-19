package io.github.anyzm.graph.ocean.engine;

import io.github.anyzm.graph.ocean.dao.GraphTypeManager;
import io.github.anyzm.graph.ocean.domain.GraphLabel;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;
import io.github.anyzm.graph.ocean.domain.impl.GraphSpace;
import io.github.anyzm.graph.ocean.domain.impl.GraphVertexType;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class NebulaSchemaManager {

    private static final String SQL_CREATE_SPACE = "CREATE SPACE IF NOT EXISTS `%s` " +
            "(partition_num = %d, replica_factor = %d, vid_type = %s);";
    private static final String SQL_CLONE_SPACE = "CREATE SPACE IF NOT EXISTS `%s` as `%s`;";
    private static final String SQL_CLEAR_SPACE = "CLEAR SPACE IF EXISTS `%s`;use `%s`;submit job compact;";
    private static final String SQL_DROP_SPACE = "DROP SPACE IF EXISTS `%s`;";

    private static final String SQL_CREATE_TAG = "CREATE TAG IF NOT EXISTS `%s` (%s) COMMENT = \"%s\"";
    private static final String SQL_CREATE_EDGE = "CREATE EDGE IF NOT EXISTS `%s` (%s) COMMENT = \"%s\"";

    private static final String SQL_CREATE_TAG_INDEX = "CREATE TAG INDEX IF NOT EXISTS `idx_%s` on `%s`()";
    private static final String SQL_CREATE_EDGE_INDEX = "CREATE EDGE INDEX IF NOT EXISTS `idx_%s` on `%s`()";

    public static final String SQL_SUBMIT_JOB_STATS = "SUBMIT JOB STATS";

    public static final String SQL_SHOW_JOB_ID = "SHOW JOB %d";

    public static final String SQL_SHOW_STATS = "SHOW STATS";

    @Setter
    private static GraphTypeManager graphTypeManager;

    public static String buildCreateSpaceSql(GraphSpace graphSpace) {
        return String.format(SQL_CREATE_SPACE, graphSpace.getSpaceName(), graphSpace.getPartitionNum(),
                graphSpace.getReplicaFactor(), graphSpace.getVidType());
    }

    public static String buildCloneSpaceSql(String existSpaceName, String newSpaceName) {
        return String.format(SQL_CLONE_SPACE, newSpaceName, existSpaceName);
    }

    public static String buildClearSpaceSql(String spaceName) {
        return String.format(SQL_CLEAR_SPACE, spaceName, spaceName);
    }

    public static String buildDropSpaceSql(String spaceName) {
        return String.format(SQL_DROP_SPACE, spaceName);
    }

    public static <E> String buildCreateEdgeIndexSql(Class<E> clazz) {
        GraphEdgeType<E> edgeType = graphTypeManager.getGraphEdgeType(clazz);
        if(null == edgeType) {
            throw new NebulaException(ErrorEnum.NOT_SUPPORT_EDGE_TAG);
        }

        String edgeName = edgeType.getEdgeName();

        return String.format(SQL_CREATE_EDGE_INDEX, edgeName, edgeName);
    }

    public static <E> String buildCreateEdgeSql(Class<E> clazz){
        GraphEdgeType<E> edgeType = graphTypeManager.getGraphEdgeType(clazz);
        if(null == edgeType) {
            throw new NebulaException(ErrorEnum.NOT_SUPPORT_EDGE_TAG);
        }

        String propertySql = buildPropertySql(edgeType);
        String edgeName = edgeType.getEdgeName();
        // 默认
        String edgeComment = edgeType.getComment();
        if(StringUtils.isBlank(edgeComment)) {
            edgeComment = edgeName;
        }
        return String.format(SQL_CREATE_EDGE, edgeName, propertySql, edgeComment);
    }

    public static <T> String buildCreateTagIndexSql(Class<T> clazz, String tagName) {
        GraphVertexType<T> vertexType = graphTypeManager.getGraphVertexType(clazz);
        if(null == vertexType) {
            throw new NebulaException(ErrorEnum.NOT_SUPPORT_VERTEX_TAG);
        }

        String vertexName = vertexType.getVertexName();
        if(StringUtils.isBlank(vertexName)) {
            vertexName = tagName;
        }

        return String.format(SQL_CREATE_TAG_INDEX, vertexName, vertexName);
    }

    public static <T> String buildCreateTagSql(Class<T> clazz, String tagName, String tagComment){
        GraphVertexType<T> vertexType = graphTypeManager.getGraphVertexType(clazz);
        if(null == vertexType) {
            throw new NebulaException(ErrorEnum.NOT_SUPPORT_VERTEX_TAG);
        }

        String propertySql = buildPropertySql(vertexType);
        String vertexName = vertexType.getVertexName();
        if(StringUtils.isBlank(vertexName)) {
            vertexName = tagName;
        }

        // 优先使用注解上的值
        String vertexComment = vertexType.getComment();
        // 未设置使用传入值
        if(StringUtils.isBlank(vertexComment)) {
            vertexComment = tagComment;
        }
        // 默认使用顶点名称
        if(StringUtils.isBlank(vertexComment)) {
            vertexComment = vertexName;
        }

        return String.format(SQL_CREATE_TAG, vertexName, propertySql, vertexComment);
    }

    private static String buildPropertySql(GraphLabel graphLabel) {
        Collection<String> properties = graphLabel.getAllProperties();
        if(CollectionUtils.isEmpty(properties)) {
            return "";
        }

        StringBuilder propertyBuilder = new StringBuilder();
        for(String property : properties) {
            propertyBuilder.append(",");
            propertyBuilder.append("`").append(property).append("` ");
            propertyBuilder.append(graphLabel.getDataType(property).getNebulaType()).append(" ");
            if(graphLabel.isMust(property)) {
                propertyBuilder.append("NOT NULL").append(" ");
            }
            String defaultValue = graphLabel.getPropertyDefaultValue(property);
            if(StringUtils.isNotBlank(defaultValue)) {
                propertyBuilder.append("DEFAULT ").append(defaultValue).append(" ");
            }
            String comment = graphLabel.getPropertyComment(property);
            if(StringUtils.isNotBlank(comment)) {
                propertyBuilder.append("COMMENT \"").append(comment).append("\"");
            }
        }
        return propertyBuilder.delete(0, 1).toString();
    }
}
