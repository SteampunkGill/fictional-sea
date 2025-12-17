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

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetVersion {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取系统版本请求 ===");
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
    public static class VersionResponse {
        private boolean success;
        private String message;
        private VersionData data;

        public VersionResponse(boolean success, String message, VersionData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public VersionData getData() { return data; }
        public void setData(VersionData data) { this.data = data; }
    }

    public static class VersionData {
        private String version;
        private String buildNumber;
        private String commitHash;
        private String releaseDate;
        private String latestVersion;
        private boolean updateAvailable;
        private String updateUrl;
        private String changelogUrl;
        private String supportPeriod;

        public VersionData(String version, String buildNumber, String commitHash, String releaseDate,
                           String latestVersion, boolean updateAvailable, String updateUrl,
                           String changelogUrl, String supportPeriod) {
            this.version = version;
            this.buildNumber = buildNumber;
            this.commitHash = commitHash;
            this.releaseDate = releaseDate;
            this.latestVersion = latestVersion;
            this.updateAvailable = updateAvailable;
            this.updateUrl = updateUrl;
            this.changelogUrl = changelogUrl;
            this.supportPeriod = supportPeriod;
        }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getBuildNumber() { return buildNumber; }
        public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }

        public String getCommitHash() { return commitHash; }
        public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

        public String getLatestVersion() { return latestVersion; }
        public void setLatestVersion(String latestVersion) { this.latestVersion = latestVersion; }

        public boolean isUpdateAvailable() { return updateAvailable; }
        public void setUpdateAvailable(boolean updateAvailable) { this.updateAvailable = updateAvailable; }

        public String getUpdateUrl() { return updateUrl; }
        public void setUpdateUrl(String updateUrl) { this.updateUrl = updateUrl; }

        public String getChangelogUrl() { return changelogUrl; }
        public void setChangelogUrl(String changelogUrl) { this.changelogUrl = changelogUrl; }

        public String getSupportPeriod() { return supportPeriod; }
        public void setSupportPeriod(String supportPeriod) { this.supportPeriod = supportPeriod; }
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> getVersion() {
        // 打印接收到的请求
        printRequest("GET /api/system/version");

        try {
            // 1. 查询最新的版本信息
            String sql = "SELECT version_number, release_date, release_notes, is_forced_update, created_at FROM app_versions ORDER BY release_date DESC LIMIT 1";

            List<Map<String, Object>> versions = jdbcTemplate.queryForList(sql);
            printQueryResult(versions);

            if (versions.isEmpty()) {
                // 如果没有版本信息，返回默认值
                VersionData defaultData = new VersionData(
                        "1.0.0",
                        "20240101-001",
                        "abc123def456",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        "1.0.0",
                        false,
                        "/download",
                        "/changelog",
                        "12个月"
                );

                VersionResponse response = new VersionResponse(true, "获取版本信息成功（使用默认值）", defaultData);
                printResponse(response);
                return ResponseEntity.ok(response);
            }

            Map<String, Object> version = versions.get(0);

            // 2. 构建响应数据
            String versionNumber = (String) version.get("version_number");
            LocalDateTime releaseDate = (LocalDateTime) version.get("release_date");
            String releaseNotes = (String) version.get("release_notes");
            Boolean isForcedUpdate = (Boolean) version.get("is_forced_update");

            // 生成构建号（基于发布日期）
            String buildNumber = releaseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";

            // 生成提交哈希（模拟）
            String commitHash = "commit_" + versionNumber.replace(".", "") + "_" + releaseDate.getDayOfMonth();

            // 判断是否有更新（这里简单处理，总是返回false）
            boolean updateAvailable = false;

            VersionData versionData = new VersionData(
                    versionNumber,
                    buildNumber,
                    commitHash,
                    releaseDate.format(DateTimeFormatter.ISO_DATE_TIME),
                    versionNumber, // 假设当前就是最新版本
                    updateAvailable,
                    "/download",
                    "/changelog",
                    "12个月"
            );

            VersionResponse response = new VersionResponse(true, "获取版本信息成功", versionData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取版本信息过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 发生错误时返回默认版本信息
            VersionData errorData = new VersionData(
                    "1.0.0",
                    "error-001",
                    "error-hash",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    "1.0.0",
                    false,
                    "/download",
                    "/changelog",
                    "12个月"
            );

            VersionResponse errorResponse = new VersionResponse(false, "获取版本信息失败: " + e.getMessage(), errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
