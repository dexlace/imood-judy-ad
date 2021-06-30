package com.dexlace.common.dto.definition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 解析json模板中的table部分
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonTable {
    /**
     * {
     *       "tableName": "ad_plan",
     *       "level": 2,
     *       "insert": [
     *         {"column": "id"},
     *         {"column": "user_id"},
     *         {"column": "plan_status"},
     *         {"column": "start_date"},
     *         {"column": "end_date"}
     *       ],
     *       "update": [
     *         {"column": "id"},
     *         {"column": "user_id"},
     *         {"column": "plan_status"},
     *         {"column": "start_date"},
     *         {"column": "end_date"}
     *       ],
     *       "delete": [
     *         {"column": "id"}
     *       ]
     *     }
     */

    private String tableName;
    private Integer level;

    private List<Column> insert;
    private List<Column> update;
    private List<Column> delete;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Column {

        private String column;
    }
}
