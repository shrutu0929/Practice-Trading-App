package com.tradedesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TradeDeskApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeDeskApplication.class, args);
    }
}
