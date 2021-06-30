package com.dexlace.common.dto.definition;


import com.dexlace.common.dto.manipulation.TableTemplate;
import com.github.shyiko.mysql.binlog.event.EventType;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * 描述binglog行数据
 */
@Data
public class BinlogRowData {

    /**
     * 对表的操作对象
     */
    private TableTemplate table;

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 获取dml之后的值，改变之后的值
     * after数据
     * 是一个list
     * 每个list是列名和列值的对应关系
     */
    private List<Map<String, String>> after;

    private List<Map<String, String>> before;
}
