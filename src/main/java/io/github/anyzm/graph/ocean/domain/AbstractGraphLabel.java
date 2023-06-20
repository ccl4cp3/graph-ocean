/* Copyright (c) 2022 com.github.anyzm. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License,
 * attached with Common Clause Condition 1.0, found in the LICENSES directory.
 */
package io.github.anyzm.graph.ocean.domain;

import io.github.anyzm.graph.ocean.dao.GraphValueFormatter;
import io.github.anyzm.graph.ocean.enums.GraphDataTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description  AbstractGraphLabel is used for
 *
 * @author Anyzm
 * Date  2021/8/10 - 18:32
 * @version 1.0.0
 */
@Getter
@Setter
public abstract class AbstractGraphLabel implements GraphLabel {

    /**
     * 必要字段
     */
    protected List<String> mustProperties;

    /**
     * 所有属性和字段映射
     */
    protected Map<String, String> propertyFieldMap;

    protected Map<String, GraphValueFormatter> propertyFormatMap;

    /**
     * 属性的数据类型
     */
    protected Map<String, GraphDataTypeEnum> dataTypeMap;

    /**
     * 属性的默认值
     */
    protected Map<String, String> propertyDefaultValueMap;

    /**
     * 属性的注释
     */
    protected Map<String, String> propertyCommentMap;

    /**
     * 标签类型字段，用于动态设置顶点或边的名称
     */
    protected String typeField;

    /**
     * 标签的注释
     */
    protected String comment;

    @Override
    public Object formatValue(String property, Object originalValue) {
        GraphValueFormatter graphValueFormatter = this.propertyFormatMap.get(property);
        if (graphValueFormatter != null) {
            return graphValueFormatter.format(originalValue);
        }
        return originalValue;
    }

    @Override
    public Object reformatValue(String property, Object databaseValue) {
        GraphValueFormatter graphValueFormatter = this.propertyFormatMap.get(property);
        if (graphValueFormatter != null) {
            return graphValueFormatter.reformat(databaseValue);
        }
        return databaseValue;
    }

    @Override
    public String getFieldName(String property) {
        String fieldName = this.propertyFieldMap.get(property);
        // 未找到使用属性名
        if (StringUtils.isBlank(fieldName)) {
            fieldName = property;
        }
        return fieldName;
    }

    @Override
    public String getPropertyName(String field) {
        for (Map.Entry<String, String> next : this.propertyFieldMap.entrySet()) {
            if (Objects.equals(next.getValue(), field)) {
                return next.getKey();
            }
        }
        // 未找到返回字段名
        return field;
    }

    @Override
    public GraphDataTypeEnum getDataType(String property) {
        GraphDataTypeEnum graphDataTypeEnum = this.dataTypeMap.get(property);
        if (graphDataTypeEnum == null) {
            graphDataTypeEnum = GraphDataTypeEnum.NULL;
        }
        return graphDataTypeEnum;
    }

    @Override
    public Collection<String> getAllProperties() {
        return propertyFieldMap.keySet();
    }

    @Override
    public boolean isMust(String property) {
        return mustProperties.contains(property);
    }

    @Override
    public String getPropertyDefaultValue(String property) {
        return propertyDefaultValueMap.get(property);
    }

    @Override
    public String getPropertyComment(String property) {
        return propertyCommentMap.get(property);
    }


}
