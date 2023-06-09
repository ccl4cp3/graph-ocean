/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.dao;

import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeEntity;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;

import java.util.List;

/**
 * Description  EdgeUpdateEngine is used for
 * 目前边的更新只适合单类型的边，类似mysql的单表操作
 *
 * @author Anyzm
 * Date  2021/7/16 - 17:08
 * @version 1.0.0
 */
public interface EdgeUpdateEngine<E> extends GraphUpdateEngine {

    /**
     *
     * @return 获取边实体
     */
    public List<GraphEdgeEntity<E>> getGraphEdgeEntityList();

    /**
     *
     * @return 获取边类型
     */
    public GraphEdgeType<E> getGraphEdgeType();


}
