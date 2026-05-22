package com.bibliotheque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionLibrairieApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestionLibrairieApplication.class, args);
    }
}
