package com.psm.yagna;

import com.psm.yagna.config.YagnaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(YagnaProperties.class)
public class YagnaApplication {

    public static void main(String[] args) {
        SpringApplication.run(YagnaApplication.class, args);
    }
}