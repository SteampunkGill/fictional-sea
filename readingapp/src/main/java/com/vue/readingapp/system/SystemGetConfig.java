package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取系统配置请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("========================");
    }

    // 打印查询结果
    private void printQueryResult(Object result) {
        System.out.println("=== 数据库查询结果 ===");
        System.out.println("查询结果: " + result);
        System.out.println("===================");
    }

    // 打印返回数据
    private void printResponse(Object response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // 响应DTO
    public static class ConfigResponse {
        private boolean success;
        private String message;
        private ConfigData data;

        public ConfigResponse(boolean success, String message, ConfigData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ConfigData getData() { return data; }
        public void setData(ConfigData data) { this.data = data; }
    }

    public static class ConfigData {
        private Features features;
        private Limits limits;
        private Maintenance maintenance;

        public ConfigData(Features features, Limits limits, Maintenance maintenance) {
            this.features = features;
            this.limits = limits;
            this.maintenance = maintenance;
        }

        public Features getFeatures() { return features; }
        public void setFeatures(Features features) { this.features = features; }

        public Limits getLimits() { return limits; }
        public void setLimits(Limits limits) { this.limits = limits; }

        public Maintenance getMaintenance() { return maintenance; }
        public void setMaintenance(Maintenance maintenance) { this.maintenance = maintenance; }
    }

    public static class Features {
        private DocumentFeatures documents;
        private VocabularyFeatures vocabulary;
        private ExportFeatures export;

        public Features(DocumentFeatures documents, VocabularyFeatures vocabulary, ExportFeatures export) {
            this.documents = documents;
            this.vocabulary = vocabulary;
            this.export = export;
        }

        public DocumentFeatures getDocuments() { return documents; }
        public void setDocuments(DocumentFeatures documents) { this.documents = documents; }

        public VocabularyFeatures getVocabulary() { return vocabulary; }
        public void setVocabulary(VocabularyFeatures vocabulary) { this.vocabulary = vocabulary; }

        public ExportFeatures getExport() { return export; }
        public void setExport(ExportFeatures export) { this.export = export; }
    }

    public static class DocumentFeatures {
        private boolean enabled;
        private String maxFileSize;
        private List<String> allowedFormats;

        public DocumentFeatures(boolean enabled, String maxFileSize, List<String> allowedFormats) {
            this.enabled = enabled;
            this.maxFileSize = maxFileSize;
            this.allowedFormats = allowedFormats;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(String maxFileSize) { this.maxFileSize = maxFileSize; }

        public List<String> getAllowedFormats() { return allowedFormats; }
        public void setAllowedFormats(List<String> allowedFormats) { this.allowedFormats = allowedFormats; }
    }

    public static class VocabularyFeatures {
        private boolean enabled;
        private int maxWordsPerList;
        private boolean autoDetectLanguage;

        public VocabularyFeatures(boolean enabled, int maxWordsPerList, boolean autoDetectLanguage) {
            this.enabled = enabled;
            this.maxWordsPerList = maxWordsPerList;
            this.autoDetectLanguage = autoDetectLanguage;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getMaxWordsPerList() { return maxWordsPerList; }
        public void setMaxWordsPerList(int maxWordsPerList) { this.maxWordsPerList = maxWordsPerList; }

        public boolean isAutoDetectLanguage() { return autoDetectLanguage; }
        public void setAutoDetectLanguage(boolean autoDetectLanguage) { this.autoDetectLanguage = autoDetectLanguage; }
    }

    public static class ExportFeatures {
        private boolean enabled;
        private String maxExportSize;
        private List<String> allowedFormats;

        public ExportFeatures(boolean enabled, String maxExportSize, List<String> allowedFormats) {
            this.enabled = enabled;
            this.maxExportSize = maxExportSize;
            this.allowedFormats = allowedFormats;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getMaxExportSize() { return maxExportSize; }
        public void setMaxExportSize(String maxExportSize) { this.maxExportSize = maxExportSize; }

        public List<String> getAllowedFormats() { return allowedFormats; }
        public void setAllowedFormats(List<String> allowedFormats) { this.allowedFormats = allowedFormats; }
    }

    public static class Limits {
        private int maxDocuments;
        private int maxVocabularyWords;
        private int maxReviewsPerDay;

        public Limits(int maxDocuments, int maxVocabularyWords, int maxReviewsPerDay) {
            this.maxDocuments = maxDocuments;
            this.maxVocabularyWords = maxVocabularyWords;
            this.maxReviewsPerDay = maxReviewsPerDay;
        }

        public int getMaxDocuments() { return maxDocuments; }
        public void setMaxDocuments(int maxDocuments) { this.maxDocuments = maxDocuments; }

        public int getMaxVocabularyWords() { return maxVocabularyWords; }
        public void setMaxVocabularyWords(int maxVocabularyWords) { this.maxVocabularyWords = maxVocabularyWords; }

        public int getMaxReviewsPerDay() { return maxReviewsPerDay; }
        public void setMaxReviewsPerDay(int maxReviewsPerDay) { this.maxReviewsPerDay = maxReviewsPerDay; }
    }

    public static class Maintenance {
        private boolean enabled;
        private String scheduledTime;
        private String duration;

        public Maintenance(boolean enabled, String scheduledTime, String duration) {
            this.enabled = enabled;
            this.scheduledTime = scheduledTime;
            this.duration = duration;
        }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    @GetMapping("/config")
    public ResponseEntity<ConfigResponse> getConfig() {
        // 打印接收到的请求
        printRequest("GET /api/system/config");

        try {
            // 1. 查询用户限制表
            String limitsSql = "SELECT limit_type, limit_value FROM user_limits WHERE user_type = 'free'";
            List<Map<String, Object>> limitsData = jdbcTemplate.queryForList(limitsSql);
            printQueryResult("用户限制数据: " + limitsData);

            // 2. 解析限制数据
            int maxDocuments = 1000;
            int maxVocabularyWords = 10000;
            int maxReviewsPerDay = 500;

            for (Map<String, Object> limit : limitsData) {
                String limitType = (String) limit.get("limit_type");
                int limitValue = (Integer) limit.get("limit_value");

                switch (limitType) {
                    case "max_documents":
                        maxDocuments = limitValue;
                        break;
                    case "max_vocabulary_words":
                        maxVocabularyWords = limitValue;
                        break;
                    case "max_reviews_per_day":
                        maxReviewsPerDay = limitValue;
                        break;
                }
            }

            // 3. 构建文档功能配置
            List<String> documentFormats = new java.util.ArrayList<>();
            documentFormats.add("pdf");
            documentFormats.add("docx");
            documentFormats.add("txt");
            documentFormats.add("epub");

            DocumentFeatures docFeatures = new DocumentFeatures(
                    true,
                    "10MB",
                    documentFormats
            );

            // 4. 构建词汇功能配置
            VocabularyFeatures vocabFeatures = new VocabularyFeatures(
                    true,
                    1000,
                    true
            );

            // 5. 构建导出功能配置
            List<String> exportFormats = new java.util.ArrayList<>();
            exportFormats.add("pdf");
            exportFormats.add("xlsx");
            exportFormats.add("csv");
            exportFormats.add("json");

            ExportFeatures exportFeatures = new ExportFeatures(
                    true,
                    "100MB",
                    exportFormats
            );

            // 6. 构建功能配置
            Features features = new Features(
                    docFeatures,
                    vocabFeatures,
                    exportFeatures
            );

            // 7. 构建限制配置
            Limits limits = new Limits(
                    maxDocuments,
                    maxVocabularyWords,
                    maxReviewsPerDay
            );

            // 8. 构建维护配置
            Maintenance maintenance = new Maintenance(
                    false,
                    LocalDateTime.now().plusDays(1).toString(),
                    "2h"
            );

            // 9. 构建响应数据
            ConfigData configData = new ConfigData(
                    features,
                    limits,
                    maintenance
            );

            ConfigResponse response = new ConfigResponse(true, "获取系统配置成功", configData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取系统配置过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回默认配置
            List<String> documentFormats = new java.util.ArrayList<>();
            documentFormats.add("pdf");
            documentFormats.add("docx");
            documentFormats.add("txt");

            DocumentFeatures docFeatures = new DocumentFeatures(true, "10MB", documentFormats);
            VocabularyFeatures vocabFeatures = new VocabularyFeatures(true, 1000, true);

            List<String> exportFormats = new java.util.ArrayList<>();
            exportFormats.add("pdf");
            exportFormats.add("csv");

            ExportFeatures exportFeatures = new ExportFeatures(true, "50MB", exportFormats);

            Features features = new Features(docFeatures, vocabFeatures, exportFeatures);
            Limits limits = new Limits(1000, 10000, 500);
            Maintenance maintenance = new Maintenance(false, "", "0h");

            ConfigData errorData = new ConfigData(features, limits, maintenance);
            ConfigResponse errorResponse = new ConfigResponse(false, "获取系统配置失败: " + e.getMessage(), errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
