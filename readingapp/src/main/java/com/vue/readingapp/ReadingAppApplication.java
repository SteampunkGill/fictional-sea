package com.vue.readingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.vue.readingapp",
    "com.vue.readingapp.auth",
    "com.vue.readingapp.settings",
    "com.vue.readingapp.user",
    "com.vue.readingapp.documents",
    "com.vue.readingapp.notifications",
    "com.vue.readingapp.vocabulary",
    "com.vue.readingapp.review",
    "com.vue.readingapp.search",
    "com.vue.readingapp.ocr",
    "com.vue.readingapp.offline",
    "com.vue.readingapp.reader",
    "com.vue.readingapp.export",
    "com.vue.readingapp.feedback",
    "com.vue.readingapp.system",
    "com.vue.readingapp.tags"
})
@EntityScan(basePackages = "com.vue.readingapp")
@EnableJpaRepositories(basePackages = "com.vue.readingapp")
public class ReadingAppApplication {
    public static void main(String[] args) {
        System.out.println("=== Vue Reading App 后端服务启动中 ===");
        System.out.println("启动时间: " + java.time.LocalDateTime.now());
        System.out.println("===================================");

        SpringApplication.run(ReadingAppApplication.class, args);

        System.out.println("=== Vue Reading App 后端服务已启动 ===");
        System.out.println("服务地址: http://localhost:8080");
        System.out.println("API前缀: /api");
        System.out.println("===================================");
    }
}