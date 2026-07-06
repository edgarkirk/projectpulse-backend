package com.edgarkirk.projectpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.edgarkirk.projectpulse")
public class ProjectPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectPulseApplication.class, args);
    }
}
