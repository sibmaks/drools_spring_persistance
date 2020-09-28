package org.example.drools_persistance_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Locale;

@EntityScan
@SpringBootApplication
@EnableJpaRepositories
public class Application {

    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        System.setProperty("drools.dateformat", "dd-MMM-yyyy");
        SpringApplication.run(Application.class, args);
    }

}
