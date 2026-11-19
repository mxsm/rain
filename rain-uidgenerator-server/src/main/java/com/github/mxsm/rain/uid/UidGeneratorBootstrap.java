package com.github.mxsm.rain.uid;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author mxsm
 * @date 2022/4/17 16:01
 * @Since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.github.mxsm.rain.uid.dao")
@EnableTransactionManagement
public class UidGeneratorBootstrap {

    @Bean
    DatabaseIdProvider databaseIdProvider() {
        return new VendorDatabaseIdProvider();
    }

    public static void main(String[] args) {
        SpringApplication.run(UidGeneratorBootstrap.class, args);
    }

}
