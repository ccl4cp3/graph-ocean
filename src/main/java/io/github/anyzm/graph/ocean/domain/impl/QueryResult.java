/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain.impl;

import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import io.github.anyzm.graph.ocean.annotation.GraphProperty;
import io.github.anyzm.graph.ocean.common.utils.FieldUtils;
import io.github.anyzm.graph.ocean.domain.GraphLabel;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Anyzm
 * @version 1.0.0
 * description QueryResult is used for
 * date 2020/3/27 - 10:13
 * @update chenrui
 * @date 2020/08/30
 */
@ToString
public class QueryResult implements Iterable<ResultSet.Record>, Serializable {

    @Getter
    private List<ResultSet.Record> data = new ArrayList<>();

    public QueryResult() {
    }

    public QueryResult(List<ResultSet.Record> data) {
        this.data = data;
    }

    /**
     * 将查询结果合并
     *
     * @param queryResult
     * @return
     */
    public QueryResult mergeQueryResult(QueryResult queryResult) {
        if (queryResult == null || queryResult.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            this.data = queryResult.getData();
        } else {
            this.data.addAll(queryResult.getData());
        }
        return this;
    }

    public <T> List<T> getEntities(GraphLabel graphLabel, Class<T> clazz) throws IllegalAccessException, InstantiationException, UnsupportedEncodingException {
        if (this.data == null || this.data.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<T> list = new ArrayList<>(this.data.size());
        for (ResultSet.Record record : this.data) {
            list.add(parseResult(record, graphLabel, clazz));
        }
        return list;
    }

    public int size() {
        return this.data.size();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public boolean isNotEmpty() {
        return this.size() != 0;
    }

    @Override
    public Iterator<ResultSet.Record> iterator() {
        return this.data.iterator();
    }

    public Stream<ResultSet.Record> stream() {
        Iterable<ResultSet.Record> iterable = this::iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private <T> void dealFieldReformat(GraphLabel graphLabel, String key, Field field, T obj, Object databaseValue) throws IllegalAccessException {
        Object value = graphLabel != null ? graphLabel.reformatValue(key, databaseValue) : databaseValue;
        field.set(obj, value);
    }

    //解析nebula结果成java bean格式
    private <T> T parseResult(ResultSet.Record record, GraphLabel graphLabel, Class<T> clazz) throws IllegalAccessException, InstantiationException, UnsupportedEncodingException {
        T obj = clazz.newInstance();
        List<Field> fieldsList = FieldUtils.listFields(clazz);
        for (Field field : fieldsList) {
            ValueWrapper valueWrapper = getValueWrapper(record, field);
            if(null == valueWrapper || valueWrapper.isNull()) {
                continue;
            }

            field.setAccessible(true);
            GraphProperty annotation = field.getAnnotation(GraphProperty.class);
            // 属性字段
            if(null != annotation) {
                String property = annotation.value();
                GraphDataTypeEnum dataType = graphLabel.getDataType(property);
                switch (dataType) {
                    case INT64:
                    case TIMESTAMP:
                        dealFieldReformat(graphLabel, property, field, obj, valueWrapper.asLong());
                        break;
                    case INT16:
                        dealFieldReformat(graphLabel, property, field, obj, (int) valueWrapper.asLong());
                        break;
                    case STRING:
                    case FIXED_STRING:
                        dealFieldReformat(graphLabel, property, field, obj, valueWrapper.asString());
                        break;
                    case DATE:
                        dealFieldReformat(graphLabel, property, field, obj, valueWrapper.asDate());
                        break;
                    case DATETIME:
                        dealFieldReformat(graphLabel, property, field, obj, valueWrapper.asDateTime());
                        break;
                    case BOOLEAN:
                        dealFieldReformat(graphLabel, property, field, obj, valueWrapper.asBoolean());
                        break;
                    case DOUBLE:
                        dealFieldReformat(graphLabel, property, field, obj, valueWrapper.asDouble());
                        break;
                    default:
                        break;
                }
                continue;
            }

            // 非属性字段
            if (valueWrapper.isLong()) {
                field.set(obj, valueWrapper.asLong());
            } else if (valueWrapper.isBoolean()) {
                field.set(obj, valueWrapper.asBoolean());
            } else if (valueWrapper.isDouble()) {
                field.set(obj, valueWrapper.asDouble());
            } else if (valueWrapper.isDate()) {
                field.set(obj, valueWrapper.asDate());
            } else if (valueWrapper.isDateTime()) {
                field.set(obj, valueWrapper.asDateTime());
            } else if (valueWrapper.isTime()) {
                field.set(obj, valueWrapper.asTime());
            } else if (valueWrapper.isString()) {
                field.set(obj, valueWrapper.asString());
            }
        }
        return obj;
    }

    private ValueWrapper getValueWrapper(ResultSet.Record record, Field field) {
        // 先使用字段名获取
        String fieldName = field.getName();
        if(record.contains(fieldName)) {
            return record.get(fieldName);
        }

        // 再使用属性名获取
        GraphProperty annotation = field.getAnnotation(GraphProperty.class);
        if(null != annotation) {
            String property = annotation.value();
            if(record.contains(property)) {
                return record.get(property);
            }
        }

        return null;
    }

}
