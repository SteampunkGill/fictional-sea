package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRefreshToken {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到刷新令牌请求 ===");
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
    public static class RefreshTokenRequest {
        private String refresh_token;

        public String getRefresh_token() { return refresh_token; }
        public void setRefresh_token(String refresh_token) { this.refresh_token = refresh_token; }
    }

    // 响应DTO
    public static class RefreshTokenResponse {
        private boolean success;
        private String message;
        private TokenData data;

        public RefreshTokenResponse(boolean success, String message, TokenData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public TokenData getData() { return data; }
        public void setData(TokenData data) { this.data = data; }
    }

    public static class TokenData {
        private String access_token;
        private String refresh_token;
        private int expires_in;

        public TokenData(String access_token, String refresh_token, int expires_in) {
            this.access_token = access_token;
            this.refresh_token = refresh_token;
            this.expires_in = expires_in;
        }

        public String getAccess_token() { return access_token; }
        public void setAccess_token(String access_token) { this.access_token = access_token; }

        public String getRefresh_token() { return refresh_token; }
        public void setRefresh_token(String refresh_token) { this.refresh_token = refresh_token; }

        public int getExpires_in() { return expires_in; }
        public void setExpires_in(int expires_in) { this.expires_in = expires_in; }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getRefresh_token() == null || request.getRefresh_token().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new RefreshTokenResponse(false, "刷新令牌不能为空", null)
                );
            }

            // 2. 验证刷新令牌
            String checkTokenSql = "SELECT session_id, user_id, expires_at FROM user_sessions WHERE refresh_token = ?";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(checkTokenSql, request.getRefresh_token());
            printQueryResult(sessions);

            if (sessions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new RefreshTokenResponse(false, "刷新令牌无效", null)
                );
            }

            Map<String, Object> session = sessions.get(0);
            Integer sessionId = (Integer) session.get("session_id");
            Integer userId = (Integer) session.get("user_id");
            LocalDateTime expiresAt = (LocalDateTime) session.get("expires_at");
            LocalDateTime now = LocalDateTime.now();

            // 3. 检查令牌是否过期
            if (now.isAfter(expiresAt)) {
                // 删除过期的会话
                String deleteSessionSql = "DELETE FROM user_sessions WHERE session_id = ?";
                jdbcTemplate.update(deleteSessionSql, sessionId);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new RefreshTokenResponse(false, "刷新令牌已过期", null)
                );
            }

            // 4. 生成新的访问令牌和刷新令牌
            String newAccessToken = "access_" + UUID.randomUUID().toString();
            String newRefreshToken = "refresh_" + UUID.randomUUID().toString();
            LocalDateTime newExpiresAt = now.plusHours(1);

            // 5. 更新会话
            String updateSessionSql = "UPDATE user_sessions SET access_token = ?, refresh_token = ?, expires_at = ? WHERE session_id = ?";
            jdbcTemplate.update(updateSessionSql,
                    newAccessToken,
                    newRefreshToken,
                    newExpiresAt,
                    sessionId
            );

            // 6. 准备响应数据
            TokenData tokenData = new TokenData(newAccessToken, newRefreshToken, 3600);
            RefreshTokenResponse response = new RefreshTokenResponse(true, "令牌刷新成功", tokenData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("刷新令牌过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RefreshTokenResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }
}