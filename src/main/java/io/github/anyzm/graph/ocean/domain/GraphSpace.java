package io.github.anyzm.graph.ocean.domain;

import lombok.Data;

@Data
public class GraphSpace {

    /**
     * 图空间名称
     */
    private String spaceName;

    /**
     * 分区数量，默认1
     */
    private int partitionNum = 1;

    /**
     * 副本数量，默认1
     */
    private int replicaFactor = 1;

    /**
     * 顶点key类型，默认INT64
     */
    private String vidType = "INT64";
}
