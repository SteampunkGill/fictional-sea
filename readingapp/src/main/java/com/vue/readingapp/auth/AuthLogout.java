package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthLogout {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到登出请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("=================");
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
    public static class LogoutResponse {
        private boolean success;
        private String message;

        public LogoutResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest httpRequest) {
        // 打印接收到的请求
        printRequest("登出请求");

        try {
            // 1. 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                        new LogoutResponse(false, "未提供有效的认证令牌")
                );
            }

            String token = authHeader.substring(7); // 去掉"Bearer "前缀

            // 2. 查找对应的会话
            String findSessionSql = "SELECT session_id, user_id FROM user_sessions WHERE access_token = ?";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(findSessionSql, token);
            printQueryResult(sessions);

            if (sessions.isEmpty()) {
                // 会话不存在，但仍然返回成功
                LogoutResponse response = new LogoutResponse(true, "登出成功");
                printResponse(response);
                return ResponseEntity.ok(response);
            }

            // 3. 删除会话
            Map<String, Object> session = sessions.get(0);
            Integer sessionId = (Integer) session.get("session_id");
            Integer userId = (Integer) session.get("user_id");

            String deleteSessionSql = "DELETE FROM user_sessions WHERE session_id = ?";
            jdbcTemplate.update(deleteSessionSql, sessionId);

            System.out.println("用户ID " + userId + " 的会话已删除");

            // 4. 返回成功响应
            LogoutResponse response = new LogoutResponse(true, "登出成功");
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("登出过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LogoutResponse(false, "服务器内部错误: " + e.getMessage())
            );
        }
    }
}