
package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetServerTime {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取服务器时间请求 ===");
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

    // 响应DTO
    public static class TimeResponse {
        private boolean success;
        private String message;
        private TimeData data;

        public TimeResponse(boolean success, String message, TimeData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public TimeData getData() { return data; }
        public void setData(TimeData data) { this.data = data; }
    }

    public static class TimeData {
        private String serverTime;
        private String clientTime;
        private long timeDifference;
        private String timezone;
        private String formattedTime;

        public TimeData(String serverTime, String clientTime, long timeDifference,
                        String timezone, String formattedTime) {
            this.serverTime = serverTime;
            this.clientTime = clientTime;
            this.timeDifference = timeDifference;
            this.timezone = timezone;
            this.formattedTime = formattedTime;
        }

        public String getServerTime() { return serverTime; }
        public void setServerTime(String serverTime) { this.serverTime = serverTime; }

        public String getClientTime() { return clientTime; }
        public void setClientTime(String clientTime) { this.clientTime = clientTime; }

        public long getTimeDifference() { return timeDifference; }
        public void setTimeDifference(long timeDifference) { this.timeDifference = timeDifference; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public String getFormattedTime() { return formattedTime; }
        public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }
    }

    @GetMapping("/time")
    public ResponseEntity<TimeResponse> getServerTime(@RequestHeader(value = "X-Client-Time", required = false) String clientTimeHeader) {
        // 打印接收到的请求
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("endpoint", "GET /api/system/time");
        requestInfo.put("clientTimeHeader", clientTimeHeader);
        printRequest(requestInfo);

        try {
            // 1. 获取服务器当前时间
            LocalDateTime serverNow = LocalDateTime.now();
            ZonedDateTime serverZoned = serverNow.atZone(ZoneId.systemDefault());

            // 2. 解析客户端时间（如果有）
            LocalDateTime clientTime = null;
            long timeDifference = 0;

            if (clientTimeHeader != null && !clientTimeHeader.trim().isEmpty()) {
                try {
                    // 尝试解析客户端时间戳
                    long clientTimestamp = Long.parseLong(clientTimeHeader);
                    clientTime = LocalDateTime.ofEpochSecond(clientTimestamp / 1000, 0, ZoneId.systemDefault().getRules().getOffset(serverNow));

                    // 计算时间差（毫秒）
                    long serverTimestamp = serverZoned.toInstant().toEpochMilli();
                    timeDifference = serverTimestamp - clientTimestamp;
                } catch (NumberFormatException e) {
                    System.out.println("客户端时间头格式错误: " + clientTimeHeader);
                }
            }

            // 3. 获取时区信息
            String timezone = ZoneId.systemDefault().getId();

            // 4. 格式化时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
            String formattedTime = serverNow.format(formatter);

            // 5. 构建响应数据
            TimeData timeData = new TimeData(
                    serverZoned.format(DateTimeFormatter.ISO_DATE_TIME),
                    clientTime != null ? clientTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME) : "unknown",
                    timeDifference,
                    timezone,
                    formattedTime
            );

            TimeResponse response = new TimeResponse(true, "获取服务器时间成功", timeData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取服务器时间过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回错误时间数据
            LocalDateTime errorTime = LocalDateTime.now();
            ZonedDateTime errorZoned = errorTime.atZone(ZoneId.systemDefault());

            TimeData errorData = new TimeData(
                    errorZoned.format(DateTimeFormatter.ISO_DATE_TIME),
                    "error",
                    0,
                    "error",
                    "时间获取失败"
            );

            TimeResponse errorResponse = new TimeResponse(false, "获取服务器时间失败: " + e.getMessage(), errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}