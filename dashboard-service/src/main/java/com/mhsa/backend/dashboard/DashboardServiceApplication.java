package com.mhsa.backend.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.mhsa.backend.dashboard",
        "com.mhsa.backend.auth.jwt"
})
public class DashboardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardServiceApplication.class, args);
    }
}
