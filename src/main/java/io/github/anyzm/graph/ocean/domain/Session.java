/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain;


import com.vesoft.nebula.client.graph.exception.IOErrorException;
import io.github.anyzm.graph.ocean.domain.impl.QueryResult;
import io.github.anyzm.graph.ocean.exception.NebulaExecuteException;

/**
 * Description  Session is used for
 *
 * @author Anyzm
 * Date  2021/7/16 - 17:39
 * @version 1.0.0
 */
public interface Session {

    /**
     * 执行数据插入、更新、删除操作
     *
     * @param dmlSql
     * @return
     * @throws NebulaExecuteException
     */
    public int executeDml(String dmlSql) throws NebulaExecuteException;

    /**
     * 执行查询
     *
     * @param querySql
     * @return
     * @throws NebulaExecuteException
     */
    public QueryResult executeQuery(String querySql) throws NebulaExecuteException;

    /**
     * 执行schema操作
     *
     * @param ddlSql
     * @return
     * @throws NebulaExecuteException
     */
    public boolean executeDdl(String ddlSql) throws IOErrorException;

    /**
     * 释放session
     */
    public void release();

    /**
     * Need server supported, v1.0 nebula-graph doesn't supported
     *
     * @return ping服务器
     */
    public boolean ping();

}
