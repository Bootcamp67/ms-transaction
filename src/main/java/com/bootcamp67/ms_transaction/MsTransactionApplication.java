package com.bootcamp67.ms_transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableEurekaClient
@EnableReactiveMongoRepositories
public class MsTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsTransactionApplication.class, args);
    }
}
