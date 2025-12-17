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
public class SystemGetHealth {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取系统健康状态请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("============================");
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
    public static class HealthResponse {
        private boolean success;
        private String message;
        private HealthData data;

        public HealthResponse(boolean success, String message, HealthData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public HealthData getData() { return data; }
        public void setData(HealthData data) { this.data = data; }
    }

    public static class HealthData {
        private String overall;
        private List<HealthCheck> checks;
        private String timestamp;

        public HealthData(String overall, List<HealthCheck> checks, String timestamp) {
            this.overall = overall;
            this.checks = checks;
            this.timestamp = timestamp;
        }

        public String getOverall() { return overall; }
        public void setOverall(String overall) { this.overall = overall; }

        public List<HealthCheck> getChecks() { return checks; }
        public void setChecks(List<HealthCheck> checks) { this.checks = checks; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class HealthCheck {
        private String name;
        private String status;
        private String details;
        private String lastChecked;

        public HealthCheck(String name, String status, String details, String lastChecked) {
            this.name = name;
            this.status = status;
            this.details = details;
            this.lastChecked = lastChecked;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public String getLastChecked() { return lastChecked; }
        public void setLastChecked(String lastChecked) { this.lastChecked = lastChecked; }
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        // 打印接收到的请求
        printRequest("GET /api/system/health");

        try {
            // 1. 检查数据库连接
            boolean dbHealthy = false;
            String dbDetails = "数据库连接失败";

            try {
                String testSql = "SELECT 1";
                jdbcTemplate.queryForObject(testSql, Integer.class);
                dbHealthy = true;
                dbDetails = "数据库连接正常";
            } catch (Exception e) {
                dbDetails = "数据库连接失败: " + e.getMessage();
            }

            // 2. 检查磁盘空间（模拟）
            boolean diskHealthy = true;
            String diskDetails = "磁盘使用率正常";

            // 3. 检查内存使用（模拟）
            boolean memoryHealthy = true;
            String memoryDetails = "内存使用率正常";

            // 4. 检查API服务（模拟）
            boolean apiHealthy = true;
            String apiDetails = "API服务运行正常";

            // 5. 构建健康检查列表
            List<HealthCheck> checks = new ArrayList<>();

            checks.add(new HealthCheck(
                    "database",
                    dbHealthy ? "healthy" : "unhealthy",
                    dbDetails,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            ));

            checks.add(new HealthCheck(
                    "disk",
                    diskHealthy ? "healthy" : "warning",
                    diskDetails,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            ));

            checks.add(new HealthCheck(
                    "memory",
                    memoryHealthy ? "healthy" : "warning",
                    memoryDetails,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            ));

            checks.add(new HealthCheck(
                    "api",
                    apiHealthy ? "healthy" : "unhealthy",
                    apiDetails,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            ));

            // 6. 确定整体状态
            String overallStatus = "healthy";
            for (HealthCheck check : checks) {
                if ("unhealthy".equals(check.getStatus())) {
                    overallStatus = "unhealthy";
                    break;
                } else if ("warning".equals(check.getStatus())) {
                    overallStatus = "warning";
                }
            }

            // 7. 构建响应数据
            HealthData healthData = new HealthData(
                    overallStatus,
                    checks,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            );

            HealthResponse response = new HealthResponse(true, "获取系统健康状态成功", healthData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取系统健康状态过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回错误健康状态
            List<HealthCheck> errorChecks = new ArrayList<>();
            errorChecks.add(new HealthCheck(
                    "system",
                    "unhealthy",
                    "获取健康状态时发生错误: " + e.getMessage(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            ));

            HealthData errorData = new HealthData(
                    "unhealthy",
                    errorChecks,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            );

            HealthResponse errorResponse = new HealthResponse(false, "获取系统健康状态失败", errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
