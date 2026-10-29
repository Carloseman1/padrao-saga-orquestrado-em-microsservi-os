package com.livraria.fiscal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FiscalApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiscalApplication.class, args);
    }
}
