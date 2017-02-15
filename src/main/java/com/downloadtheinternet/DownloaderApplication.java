package com.downloadtheinternet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.downloadtheinternet")
public class DownloaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DownloaderApplication.class, args);
    }
}
