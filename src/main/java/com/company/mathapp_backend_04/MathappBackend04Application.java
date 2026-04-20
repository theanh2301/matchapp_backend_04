package com.company.mathapp_backend_04;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MathappBackend04Application {

    public static void main(String[] args) {
        SpringApplication.run(MathappBackend04Application.class, args);
    }

}
