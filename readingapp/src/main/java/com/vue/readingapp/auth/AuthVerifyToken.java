package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthVerifyToken {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到验证令牌请求 ===");
        System.out.println("请求数据: " + request);
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
    public static class VerifyTokenRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    // 响应DTO
    public static class VerifyTokenResponse {
        private boolean success;
        private String message;
        private UserInfo data;

        public VerifyTokenResponse(boolean success, String message, UserInfo data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public UserInfo getData() { return data; }
        public void setData(UserInfo data) { this.data = data; }
    }

    public static class UserInfo {
        private Integer userId;
        private String username;
        private String email;
        private String role;
        private Boolean isValid;

        public UserInfo(Integer userId, String username, String email, String role, Boolean isValid) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.isValid = isValid;
        }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Boolean getIsValid() { return isValid; }
        public void setIsValid(Boolean isValid) { this.isValid = isValid; }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new VerifyTokenResponse(false, "令牌不能为空", null)
                );
            }

            // 2. 查找对应的会话
            String findSessionSql = "SELECT s.session_id, s.user_id, s.expires_at, u.username, u.email, u.role " +
                    "FROM user_sessions s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.access_token = ?";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(findSessionSql, request.getToken());
            printQueryResult(sessions);

            if (sessions.isEmpty()) {
                return ResponseEntity.ok(
                        new VerifyTokenResponse(false, "令牌无效", new UserInfo(null, null, null, null, false))
                );
            }

            // 3. 检查令牌是否过期
            Map<String, Object> session = sessions.get(0);
            LocalDateTime expiresAt = (LocalDateTime) session.get("expires_at");
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(expiresAt)) {
                // 删除过期的会话
                String deleteSessionSql = "DELETE FROM user_sessions WHERE session_id = ?";
                jdbcTemplate.update(deleteSessionSql, session.get("session_id"));

                return ResponseEntity.ok(
                        new VerifyTokenResponse(false, "令牌已过期", new UserInfo(null, null, null, null, false))
                );
            }

            // 4. 准备用户信息
            Integer userId = (Integer) session.get("user_id");
            String username = (String) session.get("username");
            String email = (String) session.get("email");
            String role = (String) session.get("role");

            UserInfo userInfo = new UserInfo(userId, username, email, role, true);
            VerifyTokenResponse response = new VerifyTokenResponse(true, "令牌有效", userInfo);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("验证令牌过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new VerifyTokenResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // 也可以通过请求头验证
    @GetMapping("/verify-token-header")
    public ResponseEntity<VerifyTokenResponse> verifyTokenHeader(HttpServletRequest httpRequest) {
        printRequest("通过请求头验证令牌");

        try {
            // 1. 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(
                        new VerifyTokenResponse(false, "未提供有效的认证令牌", new UserInfo(null, null, null, null, false))
                );
            }

            String token = authHeader.substring(7); // 去掉"Bearer "前缀

            // 2. 查找对应的会话
            String findSessionSql = "SELECT s.session_id, s.user_id, s.expires_at, u.username, u.email, u.role " +
                    "FROM user_sessions s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.access_token = ?";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(findSessionSql, token);

            if (sessions.isEmpty()) {
                return ResponseEntity.ok(
                        new VerifyTokenResponse(false, "令牌无效", new UserInfo(null, null, null, null, false))
                );
            }

            // 3. 检查令牌是否过期
            Map<String, Object> session = sessions.get(0);
            LocalDateTime expiresAt = (LocalDateTime) session.get("expires_at");
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(expiresAt)) {
                // 删除过期的会话
                String deleteSessionSql = "DELETE FROM user_sessions WHERE session_id = ?";
                jdbcTemplate.update(deleteSessionSql, session.get("session_id"));

                return ResponseEntity.ok(
                        new VerifyTokenResponse(false, "令牌已过期", new UserInfo(null, null, null, null, false))
                );
            }

            // 4. 准备用户信息
            Integer userId = (Integer) session.get("user_id");
            String username = (String) session.get("username");
            String email = (String) session.get("email");
            String role = (String) session.get("role");

            UserInfo userInfo = new UserInfo(userId, username, email, role, true);
            VerifyTokenResponse response = new VerifyTokenResponse(true, "令牌有效", userInfo);

            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("验证令牌过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new VerifyTokenResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }
}