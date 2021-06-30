package com.dexlace.sponsor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author: xiaogongbing
 * @Description:
 * @Date: 2021/5/18
 */
@EnableFeignClients
@EnableCircuitBreaker
@SpringBootApplication
@EnableDiscoveryClient
public class SponsorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SponsorApplication.class,args);

    }

}
