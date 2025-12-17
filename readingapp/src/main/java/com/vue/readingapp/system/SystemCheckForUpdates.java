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
public class SystemCheckForUpdates {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到检查更新请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("=====================");
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
    public static class UpdateCheckRequest {
        private String currentVersion;
        private String platform;

        public String getCurrentVersion() { return currentVersion != null ? currentVersion : "1.0.0"; }
        public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }

        public String getPlatform() { return platform != null ? platform : "web"; }
        public void setPlatform(String platform) { this.platform = platform; }
    }

    // 响应DTO
    public static class UpdateCheckResponse {
        private boolean success;
        private String message;
        private UpdateData data;

        public UpdateCheckResponse(boolean success, String message, UpdateData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public UpdateData getData() { return data; }
        public void setData(UpdateData data) { this.data = data; }
    }

    public static class UpdateData {
        private String currentVersion;
        private String latestVersion;
        private boolean updateAvailable;
        private String updateType;
        private List<ReleaseNote> releaseNotes;
        private String downloadUrl;
        private String changelogUrl;
        private boolean criticalUpdate;
        private long estimatedDownloadSize;

        public UpdateData(String currentVersion, String latestVersion, boolean updateAvailable,
                          String updateType, List<ReleaseNote> releaseNotes, String downloadUrl,
                          String changelogUrl, boolean criticalUpdate, long estimatedDownloadSize) {
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.updateAvailable = updateAvailable;
            this.updateType = updateType;
            this.releaseNotes = releaseNotes;
            this.downloadUrl = downloadUrl;
            this.changelogUrl = changelogUrl;
            this.criticalUpdate = criticalUpdate;
            this.estimatedDownloadSize = estimatedDownloadSize;
        }

        public String getCurrentVersion() { return currentVersion; }
        public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }

        public String getLatestVersion() { return latestVersion; }
        public void setLatestVersion(String latestVersion) { this.latestVersion = latestVersion; }

        public boolean isUpdateAvailable() { return updateAvailable; }
        public void setUpdateAvailable(boolean updateAvailable) { this.updateAvailable = updateAvailable; }

        public String getUpdateType() { return updateType; }
        public void setUpdateType(String updateType) { this.updateType = updateType; }

        public List<ReleaseNote> getReleaseNotes() { return releaseNotes; }
        public void setReleaseNotes(List<ReleaseNote> releaseNotes) { this.releaseNotes = releaseNotes; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

        public String getChangelogUrl() { return changelogUrl; }
        public void setChangelogUrl(String changelogUrl) { this.changelogUrl = changelogUrl; }

        public boolean isCriticalUpdate() { return criticalUpdate; }
        public void setCriticalUpdate(boolean criticalUpdate) { this.criticalUpdate = criticalUpdate; }

        public long getEstimatedDownloadSize() { return estimatedDownloadSize; }
        public void setEstimatedDownloadSize(long estimatedDownloadSize) { this.estimatedDownloadSize = estimatedDownloadSize; }
    }

    public static class ReleaseNote {
        private String version;
        private List<String> changes;

        public ReleaseNote(String version, List<String> changes) {
            this.version = version;
            this.changes = changes;
        }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public List<String> getChanges() { return changes; }
        public void setChanges(List<String> changes) { this.changes = changes; }
    }

    @GetMapping("/check-update")
    public ResponseEntity<UpdateCheckResponse> checkForUpdates(@RequestParam(required = false) String currentVersion,
                                                               @RequestParam(required = false) String platform) {

        // 创建请求对象
        UpdateCheckRequest request = new UpdateCheckRequest();
        request.setCurrentVersion(currentVersion);
        request.setPlatform(platform);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 查询最新的版本信息
            String sql = "SELECT version_number, release_date, release_notes, is_forced_update FROM app_versions ORDER BY release_date DESC LIMIT 5";

            List<Map<String, Object>> versions = jdbcTemplate.queryForList(sql);
            printQueryResult("查询到 " + versions.size() + " 个版本记录");

            if (versions.isEmpty()) {
                // 如果没有版本信息，返回无更新
                UpdateData noUpdateData = new UpdateData(
                        request.getCurrentVersion(),
                        request.getCurrentVersion(),
                        false,
                        "none",
                        new ArrayList<>(),
                        "/download",
                        "/changelog",
                        false,
                        0
                );

                UpdateCheckResponse response = new UpdateCheckResponse(true, "当前已是最新版本", noUpdateData);
                printResponse(response);
                return ResponseEntity.ok(response);
            }

            // 2. 获取最新版本
            Map<String, Object> latestVersion = versions.get(0);
            String latestVersionNumber = (String) latestVersion.get("version_number");

            // 3. 比较版本
            boolean updateAvailable = isUpdateAvailable(request.getCurrentVersion(), latestVersionNumber);
            String updateType = determineUpdateType(request.getCurrentVersion(), latestVersionNumber);

            // 4. 构建发布说明
            List<ReleaseNote> releaseNotes = new ArrayList<>();
            for (Map<String, Object> version : versions) {
                String versionNum = (String) version.get("version_number");
                String notes = (String) version.get("release_notes");

                List<String> changes = new ArrayList<>();
                if (notes != null && !notes.trim().isEmpty()) {
                    // 简单解析发布说明
                    String[] lines = notes.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            // 移除类型标记
                            if (line.startsWith("[") && line.contains("]")) {
                                int endIndex = line.indexOf("]");
                                line = line.substring(endIndex + 1).trim();
                            }
                            changes.add(line);
                        }
                    }
                }

                // 如果没有解析到内容，添加默认项
                if (changes.isEmpty()) {
                    changes.add("版本 " + versionNum + " 更新");
                }

                releaseNotes.add(new ReleaseNote(versionNum, changes));
            }

            // 5. 判断是否为关键更新
            boolean criticalUpdate = false;
            if (latestVersion.get("is_forced_update") != null) {
                criticalUpdate = (Boolean) latestVersion.get("is_forced_update");
            }

            // 6. 估算下载大小（模拟）
            long estimatedSize = 2500000; // 2.5MB

            // 7. 构建响应数据
            UpdateData updateData = new UpdateData(
                    request.getCurrentVersion(),
                    latestVersionNumber,
                    updateAvailable,
                    updateType,
                    releaseNotes,
                    "/download/" + latestVersionNumber,
                    "/changelog/" + latestVersionNumber,
                    criticalUpdate,
                    estimatedSize
            );

            UpdateCheckResponse response = new UpdateCheckResponse(true, "检查更新完成", updateData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("检查更新过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UpdateCheckResponse(false, "检查更新失败: " + e.getMessage(), null)
            );
        }
    }

    // 判断是否有更新
    private boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        if (currentVersion == null || latestVersion == null) {
            return false;
        }

        // 简单版本比较（仅比较数字部分）
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        for (int i = 0; i < Math.min(currentParts.length, latestParts.length); i++) {
            try {
                int currentPart = Integer.parseInt(currentParts[i]);
                int latestPart = Integer.parseInt(latestParts[i]);

                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，比较字符串
                if (!currentParts[i].equals(latestParts[i])) {
                    return true;
                }
            }
        }

        // 如果前面的部分都相等，但最新版本有更多部分，则有更新
        return latestParts.length > currentParts.length;
    }

    // 确定更新类型
    private String determineUpdateType(String currentVersion, String latestVersion) {
        if (!isUpdateAvailable(currentVersion, latestVersion)) {
            return "none";
        }

        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        if (currentParts.length >= 1 && latestParts.length >= 1) {
            try {
                int currentMajor = Integer.parseInt(currentParts[0]);
                int latestMajor = Integer.parseInt(latestParts[0]);

                if (latestMajor > currentMajor) {
                    return "major";
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        if (currentParts.length >= 2 && latestParts.length >= 2) {
            try {
                int currentMinor = Integer.parseInt(currentParts[1]);
                int latestMinor = Integer.parseInt(latestParts[1]);

                if (latestMinor > currentMinor) {
                    return "minor";
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        return "patch";
    }
}
