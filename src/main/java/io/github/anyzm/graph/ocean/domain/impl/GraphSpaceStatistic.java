package io.github.anyzm.graph.ocean.domain.impl;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 图空间统计
 */
@Data
public class GraphSpaceStatistic {

    /**
     * 顶点数
     */
    private long vertexCount;

    /**
     * 边数
     */
    private long edgeCount;

    /**
     * 各tag数据
     */
    private Map<String, Long> vertexMap = new HashMap<>(256);

    /**
     * 各edge数据
     */
    private Map<String, Long> edgeMap = new HashMap<>(256);
}
