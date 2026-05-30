package com.cognia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CogniaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CogniaApplication.class, args);
    }
}
