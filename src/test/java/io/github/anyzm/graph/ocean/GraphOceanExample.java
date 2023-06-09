package io.github.anyzm.graph.ocean;

import com.google.common.collect.Lists;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import io.github.anyzm.graph.ocean.annotation.GraphEdge;
import io.github.anyzm.graph.ocean.annotation.GraphProperty;
import io.github.anyzm.graph.ocean.annotation.GraphVertex;
import io.github.anyzm.graph.ocean.domain.VertexQuery;
import io.github.anyzm.graph.ocean.domain.impl.QueryResult;
import io.github.anyzm.graph.ocean.engine.NebulaVertexQuery;
import io.github.anyzm.graph.ocean.enums.GraphPropertyTypeEnum;
import io.github.anyzm.graph.ocean.mapper.NebulaGraphMapper;
import io.github.anyzm.graph.ocean.session.NebulaPoolSessionManager;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author ZhaoLai Huang
 * created by ZhaoLai Huang on 2022/4/10
 */
public class GraphOceanExample {

    private static int nebulaPoolMaxConnSize = 1000;

    private static int nebulaPoolMinConnSize = 50;

    private static int nebulaPoolIdleTime = 180000;

    private static int nebulaPoolTimeout = 300000;

    private static String nebulaCluster = "192.168.50.234:9669";

    private static String userName = "root";

    private static String password = "shunan@123";

    private static String space = "basketballplayer";

    public static NebulaPoolConfig nebulaPoolConfig() {
        NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
        nebulaPoolConfig.setMaxConnSize(nebulaPoolMaxConnSize);
        nebulaPoolConfig.setMinConnSize(nebulaPoolMinConnSize);
        nebulaPoolConfig.setIdleTime(nebulaPoolIdleTime);
        nebulaPoolConfig.setTimeout(nebulaPoolTimeout);
        return nebulaPoolConfig;
    }

    public static NebulaPool nebulaPool(NebulaPoolConfig nebulaPoolConfig)
            throws UnknownHostException {
        List<HostAddress> addresses = null;
        try {
            String[] hostPorts = StringUtils.split(nebulaCluster, ",");
            addresses = Lists.newArrayListWithExpectedSize(hostPorts.length);
            for (String hostPort : hostPorts) {
                String[] linkElements = StringUtils.split(hostPort, ":");
                HostAddress hostAddress = new HostAddress(linkElements[0],
                        Integer.valueOf(linkElements[1]));
                addresses.add(hostAddress);
            }
        } catch (Exception e) {
            throw new RuntimeException("nebula数据库连接信息配置有误，正确格式：ip1:port1,ip2:port2");
        }
        NebulaPool pool = new NebulaPool();
        pool.init(addresses, nebulaPoolConfig);
        return pool;
    }

    public static NebulaPoolSessionManager nebulaPoolSessionManager(NebulaPool nebulaPool) {
        return new NebulaPoolSessionManager(nebulaPool, userName, password, true);
    }

    public static NebulaGraphMapper nebulaGraphMapper(
            NebulaPoolSessionManager nebulaPoolSessionManager) {
        return new NebulaGraphMapper(nebulaPoolSessionManager, space);
    }

    public static void main(String[] args) throws UnknownHostException, UnsupportedEncodingException, IllegalAccessException, InstantiationException, ClientServerIncompatibleException, AuthFailedException, NotValidConnectionException, IOErrorException {
        NebulaGraphMapper nebulaGraphMapper = nebulaGraphMapper(nebulaPoolSessionManager(
                nebulaPool(nebulaPoolConfig())));
        Player player = new Player("player001", null, 33);
        // 保存顶点
        nebulaGraphMapper.saveVertexEntities(Lists.newArrayList(player));
        // 查询顶点
        System.out.println(nebulaGraphMapper.fetchVertexTag(player));
        System.out.println(nebulaGraphMapper.fetchVertexTag(Player.class, "player001"));

        Follow follow = new Follow("player001", "player002", 3);
        //保存边
        nebulaGraphMapper.saveEdgeEntities(Lists.newArrayList(follow));
        //查询出边
        List<Follow> followed = nebulaGraphMapper.goOutEdge(Follow.class, "player001");
        System.out.println(followed);
        //查询反向边
        List<Follow> follower = nebulaGraphMapper.goReverseEdge(Follow.class, "player002");
        System.out.println(follower);

        follow = new Follow("player002", "player003", 1);
        Map<String, Player> playerMap = new HashMap<>(16);
        playerMap.put("player002", new Player("player002", "张三", 3));
        playerMap.put("player003", new Player("player003", "李四", 32));
        //保存边和顶点
        nebulaGraphMapper.saveEdgeEntitiesWithVertex(Lists.newArrayList(follow), playerMap::get, playerMap::get);

        //查询API
        VertexQuery queryUserName = NebulaVertexQuery.build().fetchPropOn(Player.class, "player001")
                .yield(Player.class,"name", "birth_time");
        QueryResult rows = nebulaGraphMapper.executeQuery(queryUserName);
        System.out.println(rows);
    }

    @GraphVertex(value = "player", idAsField = false, comment = "篮球åå运动员")
    @Data
    @NoArgsConstructor
    public static class Player {

        @GraphProperty(value = "player_no", required = true,
                propertyTypeEnum = GraphPropertyTypeEnum.GRAPH_VERTEX_ID, comment = "编号")
        private String playerNo;

        @GraphProperty(value = "name", comment = "姓名", required = true)
        private String playerName;

        @GraphProperty(value = "age", defaultValue = "18", comment = "年龄")
        private Integer playerAge;

        @GraphProperty(value = "birth_time")
        private Date playerBirth;

        public Player(String playerNo) {
            this.playerNo = playerNo;
        }

        public Player(String playerNo, String playerName, Integer playerAge) {
            this.playerNo = playerNo;
            this.playerName = playerName;
            this.playerAge = playerAge;
        }
    }

    @GraphEdge(value = "follow", srcIdAsField = false, dstIdAsField = false)
    @Data
    public static class Follow {
        @GraphProperty(value = "follower", required = true,
                propertyTypeEnum = GraphPropertyTypeEnum.GRAPH_EDGE_SRC_ID)
        private String followerNo;

        @GraphProperty(value = "followed", required = true,
                propertyTypeEnum = GraphPropertyTypeEnum.GRAPH_EDGE_DST_ID)
        private String followedNo;

        @GraphProperty(value = "degree", defaultValue = "0", comment = "追随赛季数")
        private Integer degree;

        public Follow() {
        }

        public Follow(String followerNo, String followedNo, Integer degree) {
            this.followerNo = followerNo;
            this.followedNo = followedNo;
            this.degree = degree;
        }
    }

}
