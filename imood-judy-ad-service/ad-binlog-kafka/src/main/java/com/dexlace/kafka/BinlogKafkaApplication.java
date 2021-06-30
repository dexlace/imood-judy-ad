package com.dexlace.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Author: xiaogongbing
 * @Description:
 * @Date: 2021/6/26
 */
@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
@EnableEurekaClient

public class BinlogKafkaApplication {
    public static void main(String[] args) {

        SpringApplication.run(BinlogKafkaApplication.class, args);
    }
}
