package com.dexlace.kafka.mysql;


import com.alibaba.fastjson.JSON;

import com.dexlace.common.constant.OpType;
import com.dexlace.common.dto.definition.Template;
import com.dexlace.common.dto.manipulation.ParseTemplate;
import com.dexlace.common.dto.manipulation.TableTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TemplateHolder {

    private ParseTemplate template;
    private final JdbcTemplate jdbcTemplate;

    private String SQL_SCHEMA = "select table_schema, table_name, " +
            "column_name, ordinal_position from information_schema.columns " +
            "where table_schema = ? and table_name = ?";

    @Autowired
    public TemplateHolder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    private void init() {
        loadJson("template.json");
    }

    /**
     * 获取该表的操作模板
     * @param tableName 表名
     * @return 表的操作模板
     */
    public TableTemplate getTable(String tableName) {
        return template.getTableTemplateMap().get(tableName);
    }


    /**
     * 完全解析模板文件
     * @param path  模板文件路径
     */
    private void loadJson(String path) {

        /**
         * 获得类加载器
         */
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream inStream = cl.getResourceAsStream(path);

        try {
            // 解析一个模板文件对象
            Template template = JSON.parseObject(
                    inStream,
                    Charset.defaultCharset(),
                    Template.class
            );
            // 对模板进行解析
            this.template = ParseTemplate.parse(template);
            // 得到列名和列索引的对应
            loadMeta();
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException("fail to parse json file");
        }
    }

    /**
     * 加载了元信息
     */
    private void loadMeta() {

        /**
         * 遍历一个表名：表的系列操作的map
         *
         */
        for (Map.Entry<String, TableTemplate> entry :
                template.getTableTemplateMap().entrySet()) {

            /**
             * 得到该表对应的系列操作：tableTemplate
             */
            TableTemplate table = entry.getValue();

            /**
             * 得到更新操作的列名list
             */
            List<String> updateFields = table.getOpTypeFieldSetMap().get(
                    OpType.UPDATE
            );
            /**
             * 得到增加操作的列名list
             */
            List<String> insertFields = table.getOpTypeFieldSetMap().get(
                    OpType.ADD
            );
            /**
             * 得到删除操作的列名list
             */
            List<String> deleteFields = table.getOpTypeFieldSetMap().get(
                    OpType.DELETE
            );

            jdbcTemplate.query(SQL_SCHEMA, new Object[]{
                    template.getDatabase(), table.getTableName()
            }, (rs, i) -> {  // rs是结果集，i是行数

                // 得到列名的序号和对应列名
                int pos = rs.getInt("ORDINAL_POSITION");
                String colName = rs.getString("COLUMN_NAME");

                if ((null != updateFields && updateFields.contains(colName))
                        || (null != insertFields && insertFields.contains(colName))
                        || (null != deleteFields && deleteFields.contains(colName))) {
                    // 存储到列名序号和列名的对应map中
                    table.getPosMap().put(pos - 1, colName);
                }

                return null;
            });
        }
    }
}
