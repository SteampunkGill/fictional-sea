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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/system")
public class SystemSubmitBugReport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到提交错误报告请求 ===");
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
    public static class BugReportRequest {
        private String title;
        private String description;
        private List<String> steps;
        private String expected;
        private String actual;
        private EnvironmentInfo environment;
        private String screenshot;
        private String priority;
        private String category;
        private List<String> tags;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getSteps() { return steps != null ? steps : new ArrayList<>(); }
        public void setSteps(List<String> steps) { this.steps = steps; }

        public String getExpected() { return expected; }
        public void setExpected(String expected) { this.expected = expected; }

        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }

        public EnvironmentInfo getEnvironment() { return environment != null ? environment : new EnvironmentInfo(); }
        public void setEnvironment(EnvironmentInfo environment) { this.environment = environment; }

        public String getScreenshot() { return screenshot; }
        public void setScreenshot(String screenshot) { this.screenshot = screenshot; }

        public String getPriority() { return priority != null ? priority : "medium"; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getCategory() { return category != null ? category : "general"; }
        public void setCategory(String category) { this.category = category; }

        public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    public static class EnvironmentInfo {
        private String browser;
        private String os;
        private String device;

        public String getBrowser() { return browser != null ? browser : "unknown"; }
        public void setBrowser(String browser) { this.browser = browser; }

        public String getOs() { return os != null ? os : "unknown"; }
        public void setOs(String os) { this.os = os; }

        public String getDevice() { return device != null ? device : "unknown"; }
        public void setDevice(String device) { this.device = device; }
    }

    // 响应DTO
    public static class BugReportResponse {
        private boolean success;
        private String message;
        private BugReportData data;

        public BugReportResponse(boolean success, String message, BugReportData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public BugReportData getData() { return data; }
        public void setData(BugReportData data) { this.data = data; }
    }

    public static class BugReportData {
        private String ticketId;
        private String message;
        private String estimatedResolutionTime;
        private List<String> nextSteps;

        public BugReportData(String ticketId, String message, String estimatedResolutionTime, List<String> nextSteps) {
            this.ticketId = ticketId;
            this.message = message;
            this.estimatedResolutionTime = estimatedResolutionTime;
            this.nextSteps = nextSteps;
        }

        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getEstimatedResolutionTime() { return estimatedResolutionTime; }
        public void setEstimatedResolutionTime(String estimatedResolutionTime) { this.estimatedResolutionTime = estimatedResolutionTime; }

        public List<String> getNextSteps() { return nextSteps; }
        public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }
    }

    @PostMapping("/bug-report")
    public ResponseEntity<BugReportResponse> submitBugReport(@RequestBody BugReportRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new BugReportResponse(false, "错误报告标题不能为空", null)
                );
            }

            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new BugReportResponse(false, "错误报告描述不能为空", null)
                );
            }

            // 2. 生成错误报告ID
            String ticketId = "BUG-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                    "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // 3. 获取当前用户ID（如果有）
            Integer userId = null;
            // 注意：这里需要从请求头或会话中获取用户ID，但根据课设要求，我们简化处理

            // 4. 准备插入数据
            LocalDateTime now = LocalDateTime.now();

            // 将步骤列表转换为字符串
            String stepsStr = String.join("|||", request.getSteps());

            // 将环境信息转换为字符串
            String envStr = request.getEnvironment().getBrowser() + "|||" +
                    request.getEnvironment().getOs() + "|||" +
                    request.getEnvironment().getDevice();

            // 将标签列表转换为字符串
            String tagsStr = String.join(",", request.getTags());

            // 5. 插入到feedback_messages表
            String insertSql = "INSERT INTO feedback_messages (user_id, title, description, steps, expected_result, actual_result, " +
                    "environment_info, screenshot_data, priority, category, tags, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            int affectedRows = jdbcTemplate.update(insertSql,
                    userId,
                    request.getTitle(),
                    request.getDescription(),
                    stepsStr,
                    request.getExpected(),
                    request.getActual(),
                    envStr,
                    request.getScreenshot(),
                    request.getPriority(),
                    request.getCategory(),
                    tagsStr,
                    "pending", // 初始状态
                    now,
                    now
            );

            printQueryResult("插入错误报告，影响行数: " + affectedRows);

            // 6. 构建响应数据
            List<String> nextSteps = new ArrayList<>();
            nextSteps.add("我们会通过邮件通知您处理进度");
            nextSteps.add("如有更多信息需要补充，我们会联系您");
            nextSteps.add("您可以通过工单ID查询处理状态");

            BugReportData reportData = new BugReportData(
                    ticketId,
                    "错误报告已提交，我们会尽快处理",
                    "2-3个工作日",
                    nextSteps
            );

            BugReportResponse response = new BugReportResponse(true, "错误报告提交成功", reportData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("提交错误报告过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new BugReportResponse(false, "提交错误报告失败: " + e.getMessage(), null)
            );
        }
    }
}