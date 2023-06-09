/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.dao;

import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;
import io.github.anyzm.graph.ocean.domain.impl.GraphVertexType;
import io.github.anyzm.graph.ocean.exception.NebulaException;

/**
 * Description  GraphEdgeTypeFactory is used for
 *
 * @author Anyzm
 * Date  2021/7/16 - 15:10
 * @version 1.0.0
 */
public interface GraphEdgeTypeFactory {

    /**
     *
     * @param clazz 类类型
     * @return 边类型
     * @throws NebulaException 执行异常
     */
    public <E> GraphEdgeType<E> buildGraphEdgeType(Class<E> clazz) throws NebulaException;

}
