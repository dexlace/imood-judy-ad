package com.dexlace.kafka.mysql.listener;


import com.dexlace.common.dto.definition.BinlogRowData;
import com.dexlace.common.dto.manipulation.TableTemplate;
import com.dexlace.kafka.mysql.TemplateHolder;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监听器
 */
@Slf4j
@Component
public class AggregationListener implements BinaryLogClient.EventListener {

    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 数据表名称
     */
    private String tableName;


    /**
     * 表和对应监听器的map
     * 表：监听的处理方法
     */
    private Map<String, Ilistener> listenerMap = new HashMap<>();

    /**
     * 模板解析
     */
    private final TemplateHolder templateHolder;

    @Autowired
    public AggregationListener(TemplateHolder templateHolder) {
        this.templateHolder = templateHolder;
    }

    /**
     * 完全定义一张表
     */
    private String genKey(String dbName, String tableName) {
        return dbName + ":" + tableName;
    }

    /**
     * 给一张表定义注册到监听器
     * @param _dbName 数据库名字
     * @param _tableName 表名
     * @param ilistener 监听器
     */
    public void register(String _dbName, String _tableName,
                         Ilistener ilistener) {
        log.info("register : {}-{}", _dbName, _tableName);
        this.listenerMap.put(genKey(_dbName, _tableName), ilistener);
    }

    @Override
    public void onEvent(Event event) {

        EventType type = event.getHeader().getEventType();
        log.debug("event type: {}", type);

        /**
         *   TABLE_MAP事件，可以获取数据库和表名
         */
        if (type == EventType.TABLE_MAP) {
            TableMapEventData data = event.getData();
            this.tableName = data.getTable();
            this.dbName = data.getDatabase();
            return;
        }

        /**
         * 更新、增加、删除操作
         */
        if (type != EventType.EXT_UPDATE_ROWS
                && type != EventType.EXT_WRITE_ROWS
                && type != EventType.EXT_DELETE_ROWS) {
            return;
        }

        // 表名和库名是否已经完成填充，如果没有填充完，是不需要继续的
        if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(tableName)) {
            log.error("no meta data event");
            return;
        }

        // 找出对应表有兴趣的监听器
        String key = genKey(this.dbName, this.tableName);

        // 找到是否有这个监听器
        Ilistener listener = this.listenerMap.get(key);
        if (null == listener) {
            log.debug("skip {}", key);
            return;
        }


        log.info("trigger event: {}", type.name());

        try {

            // 获取binglog行数据
            BinlogRowData rowData = buildRowData(event.getData());
            if (rowData == null) {
                return;
            }

            rowData.setEventType(type);



            // 这里调用了listener
            listener.onEvent(rowData);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
        } finally {
            // 处理完当前的某个表的监听后，需要将数据库名和表名清空
            this.dbName = "";
            this.tableName = "";
        }
    }


    private BinlogRowData buildRowData(EventData eventData) {

        /**
         * 因为我们是按照模板（解析哪个表，解析哪种字段，在那个模板中已经有了）
         * 获取一个表的操作模板
         */
        TableTemplate table = templateHolder.getTable(tableName);

        /**
         * 不会去解析不存在的表
         */
        if (null == table) {
            log.warn("table {} not found", tableName);
            return null;
        }

        /**
         * 存储一系列的after的数据，是一个list，每个list表示了该列的列名和列值的映射
         * 下面是一个data数据
         * WriteRowsEventData{tableId=71,includeColumnBeforeUpdate={0,1,2},
         * includeColumns={0,1,2},
         * rows=[{before=[10,10,宝马]，after=[10,11,保时捷]}]
         * 其中after=[10,11,保时捷]对应需要表现为[列名1：10,列名2：11,列名3：保时捷]的格式
         * 所以下面的afterMapList为map的list
         */
        List<Map<String, String>> afterMapList = new ArrayList<>();

        /**
         * 获取after之后的数据
         * 由列名与索引的对应关系找出列名
         * 设定列名该有的列值
         */
        for (Serializable[] after : getAfterValues(eventData)) {

            /**
             * 列名和列值的对应
             */
            Map<String, String> afterMap = new HashMap<>();

            // 数组长度
            int colLen = after.length;

            for (int ix = 0; ix < colLen; ++ix) {

                // 取出当前位置对应的列名
                String colName = table.getPosMap().get(ix);

                // 如果没有则说明不关心这个列
                if (null == colName) {
                    log.debug("ignore position: {}", ix);
                    continue;
                }

                String colValue = after[ix].toString();

                /**
                 * 取得列名和列值的对应
                 */
                afterMap.put(colName, colValue);
            }

            // 添加到list中
            afterMapList.add(afterMap);
        }

        // 创造一个rowData数据并返回
        BinlogRowData rowData = new BinlogRowData();
        rowData.setAfter(afterMapList);
        rowData.setTable(table);

        return rowData;
    }


    /**
     * 获取该eventData之后的数据 即after数据
     * @param eventData
     * @return
     */
    private List<Serializable[]> getAfterValues(EventData eventData) {

        if (eventData instanceof WriteRowsEventData) {
            return ((WriteRowsEventData) eventData).getRows();
        }

        // 获取after部分，其getRows()部分是一个map,前面是before,后面是after
        if (eventData instanceof UpdateRowsEventData) {
            return ((UpdateRowsEventData) eventData).getRows().stream()
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        }

        if (eventData instanceof DeleteRowsEventData) {
            return ((DeleteRowsEventData) eventData).getRows();
        }

        return Collections.emptyList();
    }

}
