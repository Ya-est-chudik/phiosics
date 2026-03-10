package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Demo1Application {

    public static void main(String[] args) {
        log.info("start");
        SpringApplication.run(Demo1Application.class, args);
    }

}
