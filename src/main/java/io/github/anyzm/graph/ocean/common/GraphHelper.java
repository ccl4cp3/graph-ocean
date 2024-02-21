/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.anyzm.graph.ocean.annotation.GraphProperty;
import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;
import io.github.anyzm.graph.ocean.dao.impl.DateGraphValueFormatter;
import io.github.anyzm.graph.ocean.dao.impl.DateTimeGraphValueFormatter;
import io.github.anyzm.graph.ocean.dao.impl.TimeStampGraphValueFormatter;
import io.github.anyzm.graph.ocean.domain.GraphLabel;
import io.github.anyzm.graph.ocean.domain.GraphLabelBuilder;
import io.github.anyzm.graph.ocean.domain.impl.GraphEdgeType;
import io.github.anyzm.graph.ocean.domain.impl.GraphVertexType;
import io.github.anyzm.graph.ocean.enums.ErrorEnum;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import io.github.anyzm.graph.ocean.enums.GraphKeyPolicy;
import io.github.anyzm.graph.ocean.enums.GraphPropertyTypeEnum;
import io.github.anyzm.graph.ocean.exception.NebulaException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author Anyzm
 * date 2020/4/16
 */
@Log4j2
public class GraphHelper {
    private static String ENDPOINT_TEMPLATE = "%s('%s')";
    private static String STRING_ID_TEMPLATE = "%s \"%s\" ";
    public static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[\n\t\"\'()<>/\\\\]");


    /**
     * nebula 中的时间戳是精确到秒的
     *
     * @return
     */
    public static long getNebulaCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }


    /**
     * 将 localDate 转换为 nebula time
     *
     * @param date
     * @return
     */
    public static long changeToNebulaTime(LocalDate date) {
        Timestamp timestamp = Timestamp.valueOf(date.atStartOfDay());
        return timestamp.getTime() / 1000;
    }

    public static String generateKeyPolicy(GraphKeyPolicy graphKeyPolicy, String vertexIdKey) {
        switch (graphKeyPolicy) {
            case string_key:
                return String.format(STRING_ID_TEMPLATE, graphKeyPolicy.getKeyWrapWord(), vertexIdKey);
            case hash:
                return String.format(ENDPOINT_TEMPLATE, graphKeyPolicy.getKeyWrapWord(), vertexIdKey);
            case uuid:
                return UUID.randomUUID().toString();
            case int_64:
                return vertexIdKey;
            default:
                return null;
        }
    }

    public static String getQueryId(GraphVertexType vertexTag, String vertexKey) {
        String vertexIdKey = vertexTag.getVertexIdKey(vertexKey);
        GraphKeyPolicy graphKeyPolicy = vertexTag.getGraphKeyPolicy();
        return generateKeyPolicy(graphKeyPolicy, vertexIdKey);
    }

    public static String getQuerySrcId(GraphEdgeType edgeType, String vertexKey) {
        String vertexIdKey = edgeType.getSrcIdKey(vertexKey);
        GraphKeyPolicy graphKeyPolicy = edgeType.getSrcVertexGraphKeyPolicy();
        return generateKeyPolicy(graphKeyPolicy, vertexIdKey);
    }

    public static String getQueryDstId(GraphEdgeType edgeType, String vertexKey) {
        String vertexIdKey = edgeType.getDstIdKey(vertexKey);
        GraphKeyPolicy graphKeyPolicy = edgeType.getDstVertexGraphKeyPolicy();
        return generateKeyPolicy(graphKeyPolicy, vertexIdKey);
    }

    /**
     * 顶点列表id
     *
     * @param vertexTag
     * @param vertexKeyList
     * @return
     */
    public static String getQueryId(GraphVertexType vertexTag, Collection<String> vertexKeyList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isEmpty(vertexKeyList)) {
            return "";
        }

        for (String vertexId : vertexKeyList) {
            stringBuilder.append(getQueryId(vertexTag, vertexId)).append(",");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }


    /**
     * 去掉特殊字符
     *
     * @param str
     * @return
     */
    public static String removeSpecialChar(String str) {
        return SPECIAL_CHAR_PATTERN.matcher(str).replaceAll("");
    }


    private static void collectGraphField(GraphLabelBuilder graphLabelBuilder, Field declaredField, List<String> mustProps,
                                          Map<String, String> propertyFieldMap, Map<String, GraphValueFormatter> propertyFormatMap,
                                          Map<String, GraphDataTypeEnum> dataTypeMap, Map<String, String> propertyDefaultValueMap,
                                          Map<String, String> propertyCommentMap, boolean srcIdAsField, boolean dstIdAsField) {
        declaredField.setAccessible(true);
        GraphProperty graphProperty = declaredField.getAnnotation(GraphProperty.class);
        if (graphProperty == null) {
            return;
        }
        String value = graphProperty.value();
        // 优先使用指定的类型，如果未指定根据java类型推断
        GraphDataTypeEnum dataType = graphProperty.dataType();
        if(GraphDataTypeEnum.NULL == dataType) {
            dataType = GraphDataTypeEnum.findByJavaType(declaredField.getType().getSimpleName());
        }
        dataTypeMap.put(value, dataType);

        GraphValueFormatter formatter = null;
        if(GraphValueFormatter.class == graphProperty.formatter()) {
            switch (dataType) {
                case TIMESTAMP:
                    formatter = TimeStampGraphValueFormatter.INSTANCE;
                    break;
                case DATE:
                    formatter = DateGraphValueFormatter.INSTANCE;
                    break;
                case DATETIME:
                    formatter = DateTimeGraphValueFormatter.INSTANCE;
                    break;
                default:
                    break;
            }
        }else {
            try {
                formatter = graphProperty.formatter().newInstance();
            }catch (Exception e) {
                throw new NebulaException(ErrorEnum.FIELD_FORMAT_NO_CONSTRUCTOR);
            }
        }

        GraphPropertyTypeEnum graphPropertyTypeEnum = graphProperty.propertyTypeEnum();
        switch (graphPropertyTypeEnum) {
            case GRAPH_LABEL_TYPE:
                graphLabelBuilder.graphLabelField(value);
                break;
            case GRAPH_VERTEX_ID:
                if (srcIdAsField && dstIdAsField) {
                    propertyFieldMap.put(value, declaredField.getName());
                    mustProps.add(value);
                }
                if (null != formatter) {
                    graphLabelBuilder.idValueFormatter(formatter);
                }
                break;
            case GRAPH_EDGE_SRC_ID:
                if (srcIdAsField) {
                    propertyFieldMap.put(value, declaredField.getName());
                    mustProps.add(value);
                }
                if (null != formatter) {
                    graphLabelBuilder.srcIdValueFormatter(formatter);
                }
                break;
            case GRAPH_EDGE_DST_ID:
                if (dstIdAsField) {
                    propertyFieldMap.put(value, declaredField.getName());
                    mustProps.add(value);
                }
                if (null != formatter) {
                    graphLabelBuilder.dstIdValueFormatter(formatter);
                }
                break;
            case ORDINARY_PROPERTY:
                propertyFieldMap.put(value, declaredField.getName());
                if (graphProperty.required()) {
                    mustProps.add(value);
                }
                if (null != formatter) {
                    propertyFormatMap.put(value, formatter);
                }
                break;
            default:
                break;
        }

        // 默认值
        propertyDefaultValueMap.put(value, graphProperty.defaultValue());
        // 注释
        propertyCommentMap.put(value, graphProperty.comment());
    }

    public static void collectGraphProperties(GraphLabelBuilder graphLabelBuilder, Class clazz,
                                              boolean srcIdAsField, boolean dstIdAsField) throws NebulaException {
        Field[] declaredFields = clazz.getDeclaredFields();
        int size = declaredFields.length;
        List<String> mustProps = Lists.newArrayListWithExpectedSize(size);
        //所有属性（包括必要属性）
        Map<String, String> propertyFieldMap = Maps.newHashMapWithExpectedSize(size);
        //属性类型
        Map<String, GraphDataTypeEnum> dataTypeMap = Maps.newHashMapWithExpectedSize(size);
        //属性转换工厂
        Map<String, GraphValueFormatter> propertyFormatMap = Maps.newHashMapWithExpectedSize(size);
        //属性默认值
        Map<String, String> propertyDefaultValueMap = Maps.newHashMapWithExpectedSize(size);
        // 属性注释
        Map<String, String> propertyCommentMap = Maps.newHashMapWithExpectedSize(size);

        for (Field declaredField : declaredFields) {
            collectGraphField(graphLabelBuilder, declaredField, mustProps, propertyFieldMap, propertyFormatMap,
                    dataTypeMap, propertyDefaultValueMap, propertyCommentMap, srcIdAsField, dstIdAsField);
        }
        Class superclass = clazz.getSuperclass();
        while (superclass != Object.class) {
            declaredFields = superclass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                collectGraphField(graphLabelBuilder, declaredField, mustProps, propertyFieldMap, propertyFormatMap,
                        dataTypeMap, propertyDefaultValueMap, propertyCommentMap, srcIdAsField, dstIdAsField);
            }
            superclass = superclass.getSuperclass();
        }
        graphLabelBuilder.labelClass(clazz);
        graphLabelBuilder.dataTypeMap(dataTypeMap);
        graphLabelBuilder.mustProps(mustProps);
        graphLabelBuilder.propertyFieldMap(propertyFieldMap);
        graphLabelBuilder.propertyFormatMap(propertyFormatMap);
        graphLabelBuilder.propertyDefaultValueMap(propertyDefaultValueMap);
        graphLabelBuilder.propertyCommentMap(propertyCommentMap);
    }

    public static Object formatFieldValue(Field declaredField, GraphProperty graphProperty, Object input, GraphLabel graphLabel) {
        Object value = null;
        try {
            value = declaredField.get(input);
        } catch (IllegalAccessException e) {
            log.warn("获取值异常{}", input, e);
        }
        return graphLabel.formatValue(graphProperty.value(), value);
    }

}
