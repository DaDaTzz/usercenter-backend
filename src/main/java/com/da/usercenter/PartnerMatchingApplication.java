package com.da.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.da.usercenter.mapper")
@EnableScheduling
public class PartnerMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerMatchingApplication.class, args);
    }

}
