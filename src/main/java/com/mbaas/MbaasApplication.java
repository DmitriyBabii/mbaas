package com.mbaas;

import com.mbaas.services.BackendlessStarter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class MbaasApplication {
    private final BackendlessStarter service;

    @PostConstruct
    public void init() {
        service.initializeBackendless();
    }

    public static void main(String[] args) {
        SpringApplication.run(MbaasApplication.class, args);
    }
}
