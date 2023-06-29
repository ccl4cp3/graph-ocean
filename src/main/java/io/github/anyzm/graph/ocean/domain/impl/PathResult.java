package io.github.anyzm.graph.ocean.domain.impl;

import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.PathWrapper;
import com.vesoft.nebula.client.graph.data.Relationship;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ToString
public class PathResult {

    /**
     * 路径列表
     */
    private List<PathWrapper> pathList;

    /**
     * 去重后节点列表
     */
    @Getter
    private List<Node> nodeList;

    /**
     * 去重后关系列表
     */
    @Getter
    private List<Relationship> relationshipList;

    public PathResult(List<PathWrapper> pathList) {
        this.pathList = pathList;
        this.nodeList = new ArrayList<>(pathList.size() * 8);
        this.relationshipList = new ArrayList<>(pathList.size() * 8);

        // 节点列表和关系列表去重
        Set<Long> nodeIdList = new HashSet<>(pathList.size() * 8);
        Set<String> relationshipIdList = new HashSet<>(pathList.size() * 8);
        for(PathWrapper pathWrapper : pathList) {
            for(Node node : pathWrapper.getNodes()) {
                if(nodeIdList.add(node.getId().asLong())) {
                    this.nodeList.add(node);
                }
            }
            for(Relationship relationship : pathWrapper.getRelationships()) {
                if(relationshipIdList.add(relationship.srcId().asLong() + relationship.edgeName() + relationship.dstId().asLong())) {
                    this.relationshipList.add(relationship);
                }
            }
        }
    }
}
