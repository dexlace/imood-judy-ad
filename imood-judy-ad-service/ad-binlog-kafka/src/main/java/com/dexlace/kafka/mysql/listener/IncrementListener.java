package com.dexlace.kafka.mysql.listener;


import com.dexlace.common.constant.Constant;
import com.dexlace.common.constant.OpType;
import com.dexlace.common.dto.definition.BinlogRowData;
import com.dexlace.common.dto.definition.MySqlRowData;
import com.dexlace.common.dto.manipulation.TableTemplate;
import com.dexlace.kafka.sender.ISender;
import com.github.shyiko.mysql.binlog.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册和监听的功能，主要通过AggregationListener实现
 */
@Slf4j
@Component
public class IncrementListener implements Ilistener {

    /**
     * 投递  将mysqlRowData数据投递出去
     *
     */
    @Autowired
    private ISender sender;

    /**
     * 增量数据监听器
     */
    private final AggregationListener aggregationListener;

    @Autowired
    public IncrementListener(AggregationListener aggregationListener) {
        this.aggregationListener = aggregationListener;

    }


    /**
     * 将各个表注册到本监听器  只注册一次
     * 注意@PostConstruct注解
     */
    @Override
    @PostConstruct
    public void register() {

        log.info("IncrementListener register db and table info");
        Constant.table2Db.forEach((k, v) ->
        aggregationListener.register(v, k, this));
    }

    /**
     * 继续解析  将binlogRowData包装成MySqlRowData 数据
     * @param eventData
     */
    @Override
    public void onEvent(BinlogRowData eventData) {

        TableTemplate table = eventData.getTable();
        EventType eventType = eventData.getEventType();

        // 包装成最后需要投递的数据
        MySqlRowData mySqlRowData = new MySqlRowData();

        mySqlRowData.setTableName(table.getTableName());
        mySqlRowData.setLevel(eventData.getTable().getLevel());
        // 将eventType转换成我们定义的操作类型
        OpType opType = OpType.to(eventType);
        mySqlRowData.setOpType(opType);

        // 取出模板中该操作类型对应的字段名列表
        List<String> fieldList = table.getOpTypeFieldSetMap().get(opType);
        if (null == fieldList) {
            log.warn("{} not support for {}", opType, table.getTableName());
            return;
        }

        for (Map<String, String> afterMap : eventData.getAfter()) {

            /**
             * 列名和列值的map
             */
            Map<String, String> _afterMap = new HashMap<>();

            for (Map.Entry<String, String> entry : afterMap.entrySet()) {

                String colName = entry.getKey();
                String colValue = entry.getValue();

                _afterMap.put(colName, colValue);
            }

            /**
             * 成功转换成mysqlRowData
             */
            mySqlRowData.getFieldValueMap().add(_afterMap);
        }


        sender.sender(mySqlRowData);
    }
}
