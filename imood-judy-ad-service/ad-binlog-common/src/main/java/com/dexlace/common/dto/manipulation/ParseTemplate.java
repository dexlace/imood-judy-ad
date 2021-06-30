package com.dexlace.common.dto.manipulation;



import com.dexlace.common.constant.OpType;
import com.dexlace.common.dto.definition.JsonTable;
import com.dexlace.common.dto.definition.Template;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 对模板进行解析
 */
@Data
public class ParseTemplate {

    /**
     * 数据库的名称
     */
    private String database;

    /**
     * 存放表的名称和对表的操作
     * key:表的名称
     * value：表的操作
     */
    private Map<String, TableTemplate> tableTemplateMap = new HashMap<>();

    /**
     * 传入一个我们需要解析的模板对象Template
     * @param _template 传进来的模板对象，包含整个解析模板的所有内容
     * @return 一个解析该模板的对象
     */
    public static ParseTemplate parse(Template _template) {


        ParseTemplate template = new ParseTemplate();
        template.setDatabase(_template.getDatabase());

        for (JsonTable table : _template.getTableList()) {
            /**
             *先遍历其表的list
             */
            String name = table.getTableName();
            Integer level = table.getLevel();

            /**
             * 设置对应的操作类TableTemplate
             */
            TableTemplate tableTemplate = new TableTemplate();
            tableTemplate.setTableName(name);
            tableTemplate.setLevel(level.toString());

            /**
             * 放入映射关系中
             * key：表名
             * value:tableTemplate对象 包含了对该表的所有操作
             */
            template.tableTemplateMap.put(name, tableTemplate);

            // 取出对应的opTypeFieldSetMap，下面就要开始去赋值
            Map<OpType, List<String>> opTypeFieldSetMap =
                    tableTemplate.getOpTypeFieldSetMap();

            /**
             * 得到操作的列的list
             * 如果是insert则表示添加，OpType是添加，所以需要添加OpType.ADD的key
             * 并，对该key进行设置
             */
            for (JsonTable.Column column : table.getInsert()) {
                getAndCreateIfNeed(
                        OpType.ADD,
                        opTypeFieldSetMap,
                        ArrayList::new
                ).add(column.getColumn());
            }
            for (JsonTable.Column column : table.getUpdate()) {
                getAndCreateIfNeed(
                        OpType.UPDATE,
                        opTypeFieldSetMap,
                        ArrayList::new
                ).add(column.getColumn());
            }
            for (JsonTable.Column column : table.getDelete()) {
                getAndCreateIfNeed(
                        OpType.DELETE,
                        opTypeFieldSetMap,
                        ArrayList::new
                ).add(column.getColumn());
            }
        }

        return template;
    }

    private static <T, R> R getAndCreateIfNeed(T key, Map<T, R> map,
                                               Supplier<R> factory) {
        return map.computeIfAbsent(key, k -> factory.get());
    }


}













