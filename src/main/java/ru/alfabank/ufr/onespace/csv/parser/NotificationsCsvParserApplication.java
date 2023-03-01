package ru.alfabank.ufr.onespace.csv.parser;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@EnableBatchProcessing
public class NotificationsCsvParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsCsvParserApplication.class, args);
    }


}
