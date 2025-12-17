package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/system")
public class SystemClearCache {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到清理系统缓存请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("=========================");
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

    // 请求DTO
    public static class ClearCacheRequest {
        private List<String> types;
        private boolean force;

        public List<String> getTypes() {
            if (types == null || types.isEmpty()) {
                return new ArrayList<>(java.util.Arrays.asList("api_cache", "local_storage", "indexeddb"));
            }
            return types;
        }
        public void setTypes(List<String> types) { this.types = types; }

        public boolean isForce() { return force; }
        public void setForce(boolean force) { this.force = force; }
    }

    // 响应DTO
    public static class ClearCacheResponse {
        private boolean success;
        private String message;
        private ClearCacheData data;

        public ClearCacheResponse(boolean success, String message, ClearCacheData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ClearCacheData getData() { return data; }
        public void setData(ClearCacheData data) { this.data = data; }
    }

    public static class ClearCacheData {
        private boolean success;
        private List<String> clearedTypes;
        private CacheStatistics statistics;
        private double totalFreedSpace;
        private double executionTime;
        private String message;

        public ClearCacheData(boolean success, List<String> clearedTypes, CacheStatistics statistics,
                              double totalFreedSpace, double executionTime, String message) {
            this.success = success;
            this.clearedTypes = clearedTypes;
            this.statistics = statistics;
            this.totalFreedSpace = totalFreedSpace;
            this.executionTime = executionTime;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public List<String> getClearedTypes() { return clearedTypes; }
        public void setClearedTypes(List<String> clearedTypes) { this.clearedTypes = clearedTypes; }

        public CacheStatistics getStatistics() { return statistics; }
        public void setStatistics(CacheStatistics statistics) { this.statistics = statistics; }

        public double getTotalFreedSpace() { return totalFreedSpace; }
        public void setTotalFreedSpace(double totalFreedSpace) { this.totalFreedSpace = totalFreedSpace; }

        public double getExecutionTime() { return executionTime; }
        public void setExecutionTime(double executionTime) { this.executionTime = executionTime; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class CacheStatistics {
        private CacheInfo apiCache;
        private CacheInfo localStorage;
        private CacheInfo indexedDB;

        public CacheStatistics(CacheInfo apiCache, CacheInfo localStorage, CacheInfo indexedDB) {
            this.apiCache = apiCache;
            this.localStorage = localStorage;
            this.indexedDB = indexedDB;
        }

        public CacheInfo getApiCache() { return apiCache; }
        public void setApiCache(CacheInfo apiCache) { this.apiCache = apiCache; }

        public CacheInfo getLocalStorage() { return localStorage; }
        public void setLocalStorage(CacheInfo localStorage) { this.localStorage = localStorage; }

        public CacheInfo getIndexedDB() { return indexedDB; }
        public void setIndexedDB(CacheInfo indexedDB) { this.indexedDB = indexedDB; }
    }

    public static class CacheInfo {
        private int entries;
        private String size;

        public CacheInfo(int entries, String size) {
            this.entries = entries;
            this.size = size;
        }

        public int getEntries() { return entries; }
        public void setEntries(int entries) { this.entries = entries; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
    }

    @DeleteMapping("/cache")
    public ResponseEntity<ClearCacheResponse> clearCache(@RequestBody(required = false) ClearCacheRequest requestBody) {

        // 创建请求对象
        ClearCacheRequest request = new ClearCacheRequest();
        if (requestBody != null) {
            request.setTypes(requestBody.getTypes());
            request.setForce(requestBody.isForce());
        }

        // 打印接收到的请求
        printRequest(request);

        try {
            LocalDateTime startTime = LocalDateTime.now();

            // 1. 获取要清理的缓存类型
            List<String> clearedTypes = new ArrayList<>();
            List<String> typesToClear = request.getTypes();

            // 2. 模拟清理各种缓存类型
            for (String type : typesToClear) {
                switch (type) {
                    case "api_cache":
                        clearedTypes.add("api_cache");
                        System.out.println("清理API缓存...");
                        break;
                    case "local_storage":
                        clearedTypes.add("local_storage");
                        System.out.println("清理本地存储...");
                        break;
                    case "indexeddb":
                        clearedTypes.add("indexeddb");
                        System.out.println("清理IndexedDB...");
                        break;
                    default:
                        System.out.println("未知缓存类型: " + type);
                }
            }

            // 3. 模拟清理数据库中的缓存相关数据
            // 清理过期的会话
            String clearSessionsSql = "DELETE FROM user_sessions WHERE expires_at < NOW()";
            int sessionsCleared = jdbcTemplate.update(clearSessionsSql);
            System.out.println("清理过期会话: " + sessionsCleared + " 条");

            // 清理过期的密码重置令牌
            String clearTokensSql = "DELETE FROM password_reset_tokens WHERE expires_at < NOW()";
            int tokensCleared = jdbcTemplate.update(clearTokensSql);
            System.out.println("清理过期令牌: " + tokensCleared + " 条");

            // 清理旧的搜索历史（保留最近30天）
            String clearSearchHistorySql = "DELETE FROM search_history WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)";
            int searchHistoryCleared = jdbcTemplate.update(clearSearchHistorySql);
            System.out.println("清理旧搜索历史: " + searchHistoryCleared + " 条");

            // 清理旧的阅读历史（保留最近90天）
            String clearReadingHistorySql = "DELETE FROM reading_history WHERE start_time < DATE_SUB(NOW(), INTERVAL 90 DAY)";
            int readingHistoryCleared = jdbcTemplate.update(clearReadingHistorySql);
            System.out.println("清理旧阅读历史: " + readingHistoryCleared + " 条");

            // 4. 计算执行时间
            LocalDateTime endTime = LocalDateTime.now();
            double executionTime = (endTime.getNano() - startTime.getNano()) / 1_000_000_000.0;
            if (executionTime < 0) {
                executionTime = 1.0; // 如果时间差为负，设为1秒
            }

            // 5. 构建统计信息
            CacheInfo apiCacheInfo = new CacheInfo(1250, "50MB");
            CacheInfo localStorageInfo = new CacheInfo(500, "25MB");
            CacheInfo indexedDBInfo = new CacheInfo(0, "0MB");

            CacheStatistics statistics = new CacheStatistics(
                    apiCacheInfo,
                    localStorageInfo,
                    indexedDBInfo
            );

            // 6. 计算释放的空间（模拟）
            double totalFreedSpace = 75.0; // MB

            // 7. 构建响应数据
            ClearCacheData cacheData = new ClearCacheData(
                    true,
                    clearedTypes,
                    statistics,
                    totalFreedSpace,
                    executionTime,
                    "缓存清理完成"
            );

            ClearCacheResponse response = new ClearCacheResponse(true, "清理系统缓存成功", cacheData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("清理系统缓存过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ClearCacheResponse(false, "清理系统缓存失败: " + e.getMessage(), null)
            );
        }
    }
}
