package io.github.anyzm.graph.ocean.domain.impl;

import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.Relationship;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 子图结果
 */
@ToString
public class SubGraphResult {

    /**
     * 节点列表
     */
    @Getter
    private List<Node> nodeList;

    /**
     * 关系列表
     */
    @Getter
    private List<Relationship> relationshipList;

    public SubGraphResult(List<Node> nodeList, List<Relationship> relationshipList) {
        this.nodeList = nodeList;
        this.relationshipList = relationshipList;
    }
}
