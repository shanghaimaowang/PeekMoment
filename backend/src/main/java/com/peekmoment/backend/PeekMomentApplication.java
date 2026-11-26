package com.peekmoment.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.peekmoment.backend.mapper")
public class PeekMomentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeekMomentApplication.class, args);
    }
}
