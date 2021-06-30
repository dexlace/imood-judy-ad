package com.dexlace.common.dto.definition;


import com.dexlace.common.constant.OpType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MySqlRowData {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 层级关系  属于业务系统
     */
    private String level;

    /**
     * 操作类型
     */
    private OpType opType;

    /**
     * 就是after的数据，是变动后的字段名：字段值的list
     */
    private List<Map<String, String>> fieldValueMap = new ArrayList<>();
}
