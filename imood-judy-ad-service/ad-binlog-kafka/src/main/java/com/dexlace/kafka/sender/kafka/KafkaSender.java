package com.dexlace.kafka.sender.kafka;

/**
 * @Author: xiaogongbing
 * @Description:
 * @Date: 2021/5/26
 */

import com.alibaba.fastjson.JSON;
import com.dexlace.common.dto.definition.MySqlRowData;
import com.dexlace.kafka.sender.ISender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class KafkaSender implements ISender {

    @Value("${mykafka.topic}")
    private String topic;

    @Autowired
    private  KafkaTemplate<String, String> kafkaTemplate;



    /**
     * 发送MySqlRowData 到kafka
     * @param rowData
     */
    @Override
    public void sender(MySqlRowData rowData) {
        log.info(JSON.toJSONString(rowData));

        kafkaTemplate.send(
                topic, JSON.toJSONString(rowData)
        );
    }


}
