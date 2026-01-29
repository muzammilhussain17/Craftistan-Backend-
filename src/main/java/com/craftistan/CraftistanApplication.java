package com.craftistan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CraftistanApplication {

    public static void main(String[] args) {
        SpringApplication.run(CraftistanApplication.class, args);
    }
}
