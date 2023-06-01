/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.session;

import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.net.Session;
import io.github.anyzm.graph.ocean.domain.impl.QueryResult;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.exception.CheckThrower;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import io.github.anyzm.graph.ocean.exception.NebulaExecuteException;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Description  NebulaSessionWrapper is used for
 *
 * @author Anyzm
 * Date  2021/7/15 - 16:49
 * nebula-session包装类，区别读写执行，加强返回结果的封装
 * @version 1.0.0
 * @update chenui
 * @date 2022/08/31
 */
@Slf4j
public class NebulaSessionWrapper implements NebulaSession {

    private Session session;

    private static final String E_DATA_CONFLICT_ERROR = "E_DATA_CONFLICT_ERROR";

    public NebulaSessionWrapper(Session session) throws NebulaExecuteException, NebulaException {
        CheckThrower.ifTrueThrow(session == null, ErrorEnum.SESSION_LACK);
        this.session = session;
    }

    @Override
    public int executeDml(String dmlSql) throws NebulaExecuteException {
        try {
            ResultSet resultSet = executeSql(dmlSql);
            if(resultSet.isSucceeded()) {
                return ErrorCode.SUCCEEDED.getValue();
            }

            log.error("nebula更新异常 code:{}, msg:{}, nGql:{} ",
                    resultSet.getErrorCode(), resultSet.getErrorMessage(), dmlSql);
            throw new NebulaExecuteException(resultSet.getErrorCode(), resultSet.getErrorMessage());
        } catch (Exception e) {
            log.error("nebula更新异常 Thrift rpc call failed: {}", e.getMessage());
            throw new NebulaExecuteException(ErrorEnum.UPDATE_NEBULA_EROR, e);
        }
    }

    @Override
    public ResultSet executeSql(String statement) throws IOErrorException {
        log.debug("执行nebula sql, nGql={}", statement);
        return this.session.execute(statement);
    }

    @Override
    public QueryResult executeQuery(String querySql) throws NebulaExecuteException {
        try {
            ResultSet resultSet = executeSql(querySql);
            if (!resultSet.isSucceeded()) {
                log.error("nebula查询异常:{},{},nGql:{}", resultSet.getErrorCode(), resultSet.getErrorMessage(), querySql);
                throw new NebulaExecuteException(resultSet.getErrorCode(), resultSet.getErrorMessage());
            }
            return new QueryResult(IntStream.range(0, resultSet.rowsSize()).mapToObj(resultSet::rowValues)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("nebula查询异常 code:{}, msg:{}, nGql:{} ", ErrorCode.E_RPC_FAILURE, e.getMessage(), querySql);
            throw new NebulaExecuteException(ErrorEnum.QUERY_NEBULA_EROR, e);
        }
    }

    @Override
    public boolean executeDdl(String ddlSql) {
        try {
            ResultSet resultSet = executeSql(ddlSql);
            if(resultSet.isSucceeded()) {
                return true;
            }

            log.error("nebula schema操作异常 code:{}, msg:{}, nGql:{} ",
                    resultSet.getErrorCode(), resultSet.getErrorMessage(), ddlSql);
            return false;
        } catch (Exception e) {
            log.error("nebula schema操作异常 Thrift rpc call failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void release() {
        this.session.release();
    }

    @Override
    public boolean ping() {
        return this.session.ping();
    }
}
