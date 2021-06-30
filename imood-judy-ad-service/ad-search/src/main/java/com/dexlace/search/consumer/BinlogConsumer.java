package com.dexlace.search.consumer;

import com.alibaba.fastjson.JSON;
import com.dexlace.common.dto.definition.MySqlRowData;
import com.dexlace.search.consumer.update.IndexUpdater;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Author: xiaogongbing
 * @Description:
 * @Date: 2021/6/26
 */
@Slf4j
@Component
public class BinlogConsumer {

    private final IndexUpdater indexUpdater;

    @Autowired
    public BinlogConsumer(IndexUpdater indexUpdater) {
        this.indexUpdater = indexUpdater;
    }

    @KafkaListener(topics = {"ad-search-mysql-data"}, groupId = "ad-search")
    public void processMysqlRowData(ConsumerRecord<?, ?> record) {

        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            MySqlRowData rowData = JSON.parseObject(
                    message.toString(),
                    MySqlRowData.class
            );
            log.info("kafka processMysqlRowData: {}", JSON.toJSONString(rowData));
            indexUpdater.update(rowData);
        }
    }
}