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
public class SystemGetChangelog {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取更新日志请求 ===");
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

    // 请求DTO
    public static class ChangelogRequest {
        private String version;
        private Integer limit;

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public Integer getLimit() { return limit != null ? limit : 50; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }

    // 响应DTO
    public static class ChangelogResponse {
        private boolean success;
        private String message;
        private ChangelogData data;

        public ChangelogResponse(boolean success, String message, ChangelogData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ChangelogData getData() { return data; }
        public void setData(ChangelogData data) { this.data = data; }
    }

    public static class ChangelogData {
        private String version;
        private String releaseDate;
        private List<ChangeItem> changes;
        private List<Contributor> contributors;

        public ChangelogData(String version, String releaseDate, List<ChangeItem> changes, List<Contributor> contributors) {
            this.version = version;
            this.releaseDate = releaseDate;
            this.changes = changes;
            this.contributors = contributors;
        }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

        public List<ChangeItem> getChanges() { return changes; }
        public void setChanges(List<ChangeItem> changes) { this.changes = changes; }

        public List<Contributor> getContributors() { return contributors; }
        public void setContributors(List<Contributor> contributors) { this.contributors = contributors; }
    }

    public static class ChangeItem {
        private String type;
        private String description;
        private String details;

        public ChangeItem(String type, String description, String details) {
            this.type = type;
            this.description = description;
            this.details = details;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
    }

    public static class Contributor {
        private String name;
        private String email;
        private List<String> contributions;

        public Contributor(String name, String email, List<String> contributions) {
            this.name = name;
            this.email = email;
            this.contributions = contributions;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public List<String> getContributions() { return contributions; }
        public void setContributions(List<String> contributions) { this.contributions = contributions; }
    }

    @GetMapping("/changelog")
    public ResponseEntity<ChangelogResponse> getChangelog(@RequestParam(required = false) String version,
                                                          @RequestParam(required = false) Integer limit) {

        // 创建请求对象
        ChangelogRequest request = new ChangelogRequest();
        request.setVersion(version);
        request.setLimit(limit);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 构建查询条件
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT version_id, version_number, release_date, release_notes, is_forced_update, created_at FROM app_versions");

            List<Object> params = new ArrayList<>();

            if (version != null && !version.trim().isEmpty()) {
                sqlBuilder.append(" WHERE version_number = ?");
                params.add(version);
            }

            sqlBuilder.append(" ORDER BY release_date DESC");

            if (request.getLimit() > 0) {
                sqlBuilder.append(" LIMIT ?");
                params.add(request.getLimit());
            }

            // 2. 查询版本数据
            List<Map<String, Object>> versions = jdbcTemplate.queryForList(sqlBuilder.toString(), params.toArray());
            printQueryResult("查询到 " + versions.size() + " 个版本记录");

            if (versions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ChangelogResponse(false, "未找到版本信息", null)
                );
            }

            // 3. 获取第一个版本（最新或指定版本）
            Map<String, Object> versionData = versions.get(0);

            String versionNumber = (String) versionData.get("version_number");
            LocalDateTime releaseDate = (LocalDateTime) versionData.get("release_date");
            String releaseNotes = (String) versionData.get("release_notes");

            // 4. 解析更新日志
            List<ChangeItem> changes = parseReleaseNotes(releaseNotes);

            // 5. 构建贡献者列表（模拟数据）
            List<Contributor> contributors = buildContributors(versionNumber);

            // 6. 构建响应数据
            ChangelogData changelogData = new ChangelogData(
                    versionNumber,
                    releaseDate.format(DateTimeFormatter.ISO_DATE_TIME),
                    changes,
                    contributors
            );

            ChangelogResponse response = new ChangelogResponse(true, "获取更新日志成功", changelogData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取更新日志过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ChangelogResponse(false, "获取更新日志失败: " + e.getMessage(), null)
            );
        }
    }

    // 解析发布说明
    private List<ChangeItem> parseReleaseNotes(String releaseNotes) {
        List<ChangeItem> changes = new ArrayList<>();

        if (releaseNotes == null || releaseNotes.trim().isEmpty()) {
            // 如果没有发布说明，返回默认的更新日志
            changes.add(new ChangeItem("feature", "系统初始版本", "Vue Reading App 正式上线"));
            changes.add(new ChangeItem("improvement", "优化系统性能", "提升系统响应速度和稳定性"));
            changes.add(new ChangeItem("bugfix", "修复已知问题", "解决了一些已知的bug和问题"));
            return changes;
        }

        // 尝试按行解析发布说明
        String[] lines = releaseNotes.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 尝试解析类型和描述
            String type = "update";
            String description = line;
            String details = "";

            // 检查是否有类型标记，如 [feature], [bugfix], [improvement]
            if (line.startsWith("[") && line.contains("]")) {
                int endIndex = line.indexOf("]");
                String typeTag = line.substring(1, endIndex).toLowerCase();

                // 常见类型
                if (typeTag.equals("feature") || typeTag.equals("bugfix") ||
                        typeTag.equals("improvement") || typeTag.equals("security") ||
                        typeTag.equals("performance") || typeTag.equals("documentation")) {
                    type = typeTag;
                    description = line.substring(endIndex + 1).trim();
                }
            }

            // 检查是否有冒号分隔描述和详情
            if (description.contains("：")) {
                int colonIndex = description.indexOf("：");
                details = description.substring(colonIndex + 1).trim();
                description = description.substring(0, colonIndex).trim();
            } else if (description.contains(":")) {
                int colonIndex = description.indexOf(":");
                details = description.substring(colonIndex + 1).trim();
                description = description.substring(0, colonIndex).trim();
            }

            changes.add(new ChangeItem(type, description, details));
        }

        // 如果没有解析到任何内容，添加默认项
        if (changes.isEmpty()) {
            changes.add(new ChangeItem("update", "版本更新", releaseNotes));
        }

        return changes;
    }

    // 构建贡献者列表
    private List<Contributor> buildContributors(String version) {
        List<Contributor> contributors = new ArrayList<>();

        // 模拟贡献者数据
        if (version.startsWith("1.0")) {
            List<String> devContributions = new ArrayList<>();
            devContributions.add("feature");
            devContributions.add("bugfix");
            devContributions.add("improvement");

            contributors.add(new Contributor("张三", "zhangsan@example.com", devContributions));

            List<String> uiContributions = new ArrayList<>();
            uiContributions.add("ui");
            uiContributions.add("design");

            contributors.add(new Contributor("李四", "lisi@example.com", uiContributions));

            List<String> testContributions = new ArrayList<>();
            testContributions.add("testing");
            testContributions.add("qa");

            contributors.add(new Contributor("王五", "wangwu@example.com", testContributions));
        } else if (version.startsWith("1.1")) {
            List<String> devContributions = new ArrayList<>();
            devContributions.add("feature");
            devContributions.add("performance");

            contributors.add(new Contributor("赵六", "zhaoliu@example.com", devContributions));

            List<String> docContributions = new ArrayList<>();
            docContributions.add("documentation");

            contributors.add(new Contributor("钱七", "qianqi@example.com", docContributions));
        }

        return contributors;
    }
}