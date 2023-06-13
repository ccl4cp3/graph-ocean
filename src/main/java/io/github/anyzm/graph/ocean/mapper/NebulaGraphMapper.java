/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.mapper;

import com.google.common.collect.Lists;
import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import io.github.anyzm.graph.ocean.common.utils.CollectionUtils;
import io.github.anyzm.graph.ocean.dao.*;
import io.github.anyzm.graph.ocean.dao.impl.DefaultGraphEdgeEntityFactory;
import io.github.anyzm.graph.ocean.dao.impl.DefaultGraphTypeManager;
import io.github.anyzm.graph.ocean.dao.impl.DefaultGraphVertexEntityFactory;
import io.github.anyzm.graph.ocean.domain.*;
import io.github.anyzm.graph.ocean.domain.impl.*;
import io.github.anyzm.graph.ocean.engine.*;
import io.github.anyzm.graph.ocean.enums.EdgeDirectionEnum;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.exception.CheckThrower;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import io.github.anyzm.graph.ocean.exception.NebulaExecuteException;
import io.github.anyzm.graph.ocean.session.NebulaPoolSessionManager;
import io.github.anyzm.graph.ocean.session.NebulaSessionWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description  NebulaGraphMapper is used for
 *
 * @author Anyzm
 * Date  2021/7/16 - 17:37
 * @version 1.0.0
 */
@Slf4j
public class NebulaGraphMapper implements GraphMapper {

    private static final int BATCH_SIZE = 500;

    private static final String SQL = "use %s ; %s;";

    @Setter
    @Getter
    private NebulaPoolSessionManager nebulaPoolSessionManager;

    private GraphUpdateEdgeEngineFactory graphUpdateEdgeEngineFactory;

    private GraphUpdateVertexEngineFactory graphUpdateVertexEngineFactory;

    @Setter
    @Getter
    private String space;

    private GraphVertexEntityFactory graphVertexEntityFactory;

    private GraphEdgeEntityFactory graphEdgeEntityFactory;

    private GraphTypeManager graphTypeManager;

    private void init() {
        this.graphVertexEntityFactory = new DefaultGraphVertexEntityFactory(graphTypeManager);
        this.graphEdgeEntityFactory = new DefaultGraphEdgeEntityFactory(graphTypeManager);
        NebulaCondition.setGraphTypeManager(graphTypeManager);
        NebulaVertexQuery.setGraphTypeManager(graphTypeManager);
        NebulaEdgeQuery.setGraphTypeManager(graphTypeManager);
        NebulaSchemaManager.setGraphTypeManager(graphTypeManager);
    }

    public NebulaGraphMapper(NebulaPoolSessionManager nebulaPoolSessionManager,
                             String space) {
        this.graphTypeManager = new DefaultGraphTypeManager();
        this.graphUpdateVertexEngineFactory = new NebulaUpdateVertexEngineFactory();
        this.graphUpdateEdgeEngineFactory = new NebulaUpdateEdgeEngineFactory();
        this.nebulaPoolSessionManager = nebulaPoolSessionManager;
        this.space = space;
        init();
    }

    public NebulaGraphMapper(NebulaPoolSessionManager nebulaPoolSessionManager,
                             String space,
                             GraphUpdateVertexEngineFactory graphUpdateVertexEngineFactory,
                             GraphUpdateEdgeEngineFactory graphUpdateEdgeEngineFactory) {
        this.graphTypeManager = new DefaultGraphTypeManager();
        this.graphUpdateVertexEngineFactory = graphUpdateVertexEngineFactory;
        this.graphUpdateEdgeEngineFactory = graphUpdateEdgeEngineFactory;
        this.nebulaPoolSessionManager = nebulaPoolSessionManager;
        this.space = space;
        init();
    }

    private <T> int batchUpdateVertex(List<GraphVertexEntity<T>> graphVertexEntityList) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        VertexUpdateEngine build = this.graphUpdateVertexEngineFactory.build(graphVertexEntityList);
        List<String> sqlList = build.getSqlList();
        return executeBatchUpdateSql(space, sqlList);
    }

    @Override
    public <T> int saveVertexEntities(List<T> entities) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }
        List<GraphVertexEntity<T>> vertexEntities = Lists.newArrayListWithExpectedSize(entities.size());
        for (T entity : entities) {
            GraphVertexEntity<T> graphVertexEntity = graphVertexEntityFactory.buildGraphVertexEntity(entity);
            vertexEntities.add(graphVertexEntity);
            log.debug("构造对象entity={},graphVertexEntity={}", entity, graphVertexEntity);
        }
        log.debug("保存顶点信息到nebula,size={}", CollectionUtils.size(vertexEntities));
        return batchUpdateVertex(vertexEntities);
    }

    private <E> int batchUpdateEdge(List<GraphEdgeEntity<E>> graphEdgeEntities) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        EdgeUpdateEngine<E> build = this.graphUpdateEdgeEngineFactory.build(graphEdgeEntities);
        List<String> sqlList = build.getSqlList();
        return executeBatchUpdateSql(space, sqlList);
    }

    @Override
    public <S, D, E> int saveEdgeEntitiesWithVertex(List<E> entities, Function<String, S> srcVertexEntityFunction,
                                                    Function<String, D> dstVertexEntityFunction) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }
        List<GraphEdgeEntity<E>> graphEdgeEntities = Lists.newArrayListWithExpectedSize(entities.size());
        List<GraphVertexEntity<S>> srcGraphVertexEntities = Lists.newArrayListWithExpectedSize(entities.size());
        List<GraphVertexEntity<D>> dstGraphVertexEntities = Lists.newArrayListWithExpectedSize(entities.size());
        for (E entity : entities) {
            GraphEdgeEntity<E> graphEdgeEntity = graphEdgeEntityFactory.buildGraphEdgeEntity(entity);
            log.debug("构造对象entity={},graphEdgeEntity={}", entity, graphEdgeEntity);
            S srcEntity = srcVertexEntityFunction.apply(graphEdgeEntity.getSrcId());
            D dstEntity = dstVertexEntityFunction.apply(graphEdgeEntity.getDstId());
            GraphVertexEntity<S> srcVertexEntity = graphVertexEntityFactory.buildGraphVertexEntity(srcEntity);
            GraphVertexEntity<D> dstVertexEntity = graphVertexEntityFactory.buildGraphVertexEntity(dstEntity);
            srcGraphVertexEntities.add(srcVertexEntity);
            dstGraphVertexEntities.add(dstVertexEntity);
            graphEdgeEntities.add(graphEdgeEntity);
        }
        return batchUpdateEdgeWithVertex(graphEdgeEntities, srcGraphVertexEntities, dstGraphVertexEntities);
    }

    @Override
    public <E> int saveEdgeEntities(List<E> entities) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }
        List<GraphEdgeEntity<E>> graphEdgeEntities = Lists.newArrayListWithExpectedSize(entities.size());
        for (E entity : entities) {
            GraphEdgeEntity<E> graphEdgeEntity = graphEdgeEntityFactory.buildGraphEdgeEntity(entity);
            log.debug("构造对象entity={},graphEdgeEntity={}", entity, graphEdgeEntity);
            graphEdgeEntities.add(graphEdgeEntity);
        }
        return batchUpdateEdge(graphEdgeEntities);
    }

    private <S, D, E> int batchUpdateEdgeWithVertex(List<GraphEdgeEntity<E>> graphEdgeEntities,
                                                    List<GraphVertexEntity<S>> srcGraphVertexEntities,
                                                    List<GraphVertexEntity<D>> graphVertexEntities) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        EdgeUpdateEngine<E> build = this.graphUpdateEdgeEngineFactory.build(graphEdgeEntities,
                srcGraphVertexEntities, graphVertexEntities);
        List<String> sqlList = build.getSqlList();
        return executeBatchUpdateSql(space, sqlList);
    }

    @Override
    public int executeBatchUpdateSql(String space, List<String> sqlList) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        for (int i = 0; i < sqlList.size(); i += BATCH_SIZE) {
            List<String> sqls = sqlList.subList(i, Math.min(sqlList.size(), i + BATCH_SIZE));
            String sql = sqls.stream().collect(Collectors.joining(";"));
            NebulaSessionWrapper session = null;
            try {
                session = nebulaPoolSessionManager.getSession();
                int execute = session.executeDml(String.format(SQL, space, sql));
                CheckThrower.ifTrueThrow(execute != 0, ErrorEnum.UPDATE_NEBULA_EROR);
            } finally {
                if (session != null) {
                    session.release();
                }
            }
        }
        return 0;
    }

    @Override
    public int executeUpdateSql(String space, String sql) throws NebulaException, NotValidConnectionException, IOErrorException, ClientServerIncompatibleException, AuthFailedException {
        NebulaSessionWrapper session = null;
        try {
            session = nebulaPoolSessionManager.getSession();
            return session.executeDml(String.format(SQL, space, sql));
        } finally {
            if (session != null) {
                session.release();
            }
        }
    }

    @Override
    public int executeUpdateSql(String sql) throws NebulaException, NotValidConnectionException, IOErrorException, ClientServerIncompatibleException, AuthFailedException {
        return executeUpdateSql(this.space, sql);
    }

    @Override
    public QueryResult executeQuerySql(String sql) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        return executeQuerySql(this.space, sql);
    }

    @Override
    public QueryResult executeQuerySql(String space, String sql) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        NebulaSessionWrapper session = null;
        try {
            session = nebulaPoolSessionManager.getSession();
            return session.executeQuery(String.format(SQL, space, sql));
        } finally {
            if (session != null) {
                session.release();
            }
        }
    }

    @Override
    public <T> List<T> executeQuerySql(String sql, Class<T> clazz) throws
            NebulaException, IllegalAccessException, InstantiationException, UnsupportedEncodingException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        QueryResult result = executeQuerySql(sql);
        GraphLabel graphLabel = graphTypeManager.getGraphLabel(clazz);
        return result.getEntities(graphLabel, clazz);
    }

    @Override
    public QueryResult executeQuery(GraphQuery query) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        return executeQuerySql(query.buildSql());
    }

    @Override
    public QueryResult executeQuery(String space, GraphQuery query) throws NebulaException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        return executeQuerySql(space, query.buildSql());
    }

    @Override
    public <T> List<T> executeQuery(GraphQuery query, Class<T> clazz) throws
            NebulaException, IllegalAccessException, InstantiationException, UnsupportedEncodingException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        return executeQuerySql(query.buildSql(), clazz);
    }

    @Override
    public <E> List<E> goOutEdge(Class<E> edgeClazz, String... vertexIds) throws
            UnsupportedEncodingException, IllegalAccessException, InstantiationException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        GraphEdgeType<E> graphEdgeType = graphTypeManager.getGraphEdgeType(edgeClazz);
        String[] fieldsName = CollectionUtils.toStringArray(graphEdgeType.getAllProperties());
        EdgeQuery query = NebulaEdgeQuery.build().goFrom(edgeClazz, vertexIds).yield(edgeClazz, fieldsName);
        return executeQuery(query, edgeClazz);
    }

    @Override
    public <E> List<E> goReverseEdge(Class<E> edgeClazz, String... vertexIds) throws
            UnsupportedEncodingException, IllegalAccessException, InstantiationException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        GraphEdgeType<E> graphEdgeType = graphTypeManager.getGraphEdgeType(edgeClazz);
        String[] fieldsName = CollectionUtils.toStringArray(graphEdgeType.getAllProperties());
        EdgeQuery query = NebulaEdgeQuery.build().goFrom(edgeClazz, EdgeDirectionEnum.REVERSELY, vertexIds).yield(edgeClazz, fieldsName);
        return executeQuery(query, edgeClazz);
    }

    @Override
    public <T> List<T> fetchVertexTag(Class<T> vertexClazz, String... vertexIds) throws
            UnsupportedEncodingException, IllegalAccessException, InstantiationException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        GraphVertexType<T> graphVertexType = graphTypeManager.getGraphVertexType(vertexClazz);
        String[] properties = CollectionUtils.toStringArray(graphVertexType.getAllProperties());
        VertexQuery query = NebulaVertexQuery.build().fetchPropOn(vertexClazz, vertexIds).yield(vertexClazz, properties);
        return executeQuery(query, vertexClazz);
    }

    @Override
    public <T> T fetchVertexTag(T entity) throws UnsupportedEncodingException, IllegalAccessException, InstantiationException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        Class<T> vertexClazz = (Class<T>) entity.getClass();
        GraphVertexEntity<T> graphVertexEntity = graphVertexEntityFactory.buildGraphVertexEntity(entity);
        String[] properties = CollectionUtils.toStringArray(graphVertexEntity.getGraphVertexType().getAllProperties());
        VertexQuery query = NebulaVertexQuery.build().fetchPropOn(graphVertexEntity).yield(vertexClazz, properties);
        List<T> list = executeQuery(query, vertexClazz);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> boolean createTag(String space, Class<T> vertexClazz, String tagName, String tagComment) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildCreateTagSql(vertexClazz, tagName, tagComment);
        String schemaSql = String.format(SQL, space, sql);
        return executeSchemaSql(schemaSql);
    }

    @Override
    public <E> boolean createEdge(String space, Class<E> edgeClazz) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildCreateEdgeSql(edgeClazz);
        String schemaSql = String.format(SQL, space, sql);
        return executeSchemaSql(schemaSql);
    }

    @Override
    public <T> boolean createTagIndex(String space, Class<T> vertexClazz, String tagName) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildCreateTagIndexSql(vertexClazz, tagName);
        String schemaSql = String.format(SQL, space, sql);
        return executeSchemaSql(schemaSql);
    }

    @Override
    public <E> boolean createEdgeIndex(String space, Class<E> edgeClazz) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildCreateEdgeIndexSql(edgeClazz);
        String schemaSql = String.format(SQL, space, sql);
        return executeSchemaSql(schemaSql);
    }

    @Override
    public boolean createSpace(GraphSpace graphSpace) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildCreateSpaceSql(graphSpace);
        return executeSchemaSql(sql);
    }

    @Override
    public boolean cloneSpace(String existSpaceName, String newSpaceName) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildCloneSpaceSql(existSpaceName, newSpaceName);
        return executeSchemaSql(sql);
    }

    @Override
    public boolean clearSpace(String spaceName) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildClearSpaceSql(spaceName);
        return executeSchemaSql(sql);
    }

    @Override
    public boolean dropSpace(String spaceName) throws NebulaExecuteException {
        String sql = NebulaSchemaManager.buildDropSpaceSql(spaceName);
        return executeSchemaSql(sql);
    }

    /**
     * 执行schema变更语句服务端异步执行，需要等待20秒之后才能执行其他操作
     * @param sql
     * @return
     * @throws NebulaExecuteException
     */
    private boolean executeSchemaSql(String sql) throws NebulaExecuteException {
        NebulaSessionWrapper session = null;
        try {
            session = nebulaPoolSessionManager.getSession();
            return session.executeDdl(sql);
        }catch (IOErrorException|ClientServerIncompatibleException|NotValidConnectionException e) {
            throw new NebulaExecuteException(ErrorCode.E_RPC_FAILURE.getValue(), e.getMessage(), e);
        } catch (AuthFailedException e) {
            throw new NebulaExecuteException(ErrorCode.E_PRIVILEGE_ACTION_INVALID.getValue(), e.getMessage(), e);
        } finally {
            if (session != null) {
                session.release();
            }
        }
    }
}
