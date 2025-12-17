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
public class AuthVerifyEmail {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到验证邮箱请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("====================");
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
    public static class VerifyEmailRequest {
        private String token;
        private String email;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // 响应DTO
    public static class VerifyEmailResponse {
        private boolean success;
        private String message;

        public VerifyEmailResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new VerifyEmailResponse(false, "验证令牌不能为空")
                );
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new VerifyEmailResponse(false, "邮箱不能为空")
                );
            }

            // 2. 验证邮箱格式
            if (!request.getEmail().contains("@")) {
                return ResponseEntity.badRequest().body(
                        new VerifyEmailResponse(false, "邮箱格式不正确")
                );
            }

            // 3. 检查邮箱是否存在
            String checkEmailSql = "SELECT user_id, is_verified FROM users WHERE email = ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(checkEmailSql, request.getEmail());

            if (users.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new VerifyEmailResponse(false, "邮箱不存在")
                );
            }

            Map<String, Object> user = users.get(0);
            Integer userId = (Integer) user.get("user_id");
            Boolean isVerified = (Boolean) user.get("is_verified");

            // 4. 检查邮箱是否已经验证
            if (isVerified != null && isVerified) {
                return ResponseEntity.badRequest().body(
                        new VerifyEmailResponse(false, "邮箱已经验证过了")
                );
            }

            // 5. 在实际项目中，这里应该验证邮箱验证令牌
            // 由于是课设，我们简化处理，假设令牌有效
            // 实际项目中应该有一个email_verification_tokens表存储验证令牌

            System.out.println("验证邮箱令牌: " + request.getToken() + " 对于邮箱: " + request.getEmail());

            // 6. 更新用户邮箱验证状态
            String updateVerificationSql = "UPDATE users SET is_verified = TRUE WHERE user_id = ?";
            int updatedRows = jdbcTemplate.update(updateVerificationSql, userId);

            if (updatedRows == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new VerifyEmailResponse(false, "更新验证状态失败")
                );
            }

            System.out.println("用户ID " + userId + " 的邮箱已验证");

            // 7. 返回成功响应
            VerifyEmailResponse response = new VerifyEmailResponse(true, "邮箱验证成功");
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("验证邮箱过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new VerifyEmailResponse(false, "服务器内部错误: " + e.getMessage())
            );
        }
    }

    // 发送邮箱验证邮件
    @PostMapping("/send-verification-email")
    public ResponseEntity<VerifyEmailResponse> sendVerificationEmail(HttpServletRequest httpRequest) {
        printRequest("发送邮箱验证邮件");

        try {
            // 1. 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new VerifyEmailResponse(false, "未提供有效的认证令牌")
                );
            }

            String token = authHeader.substring(7);

            // 2. 查找对应的会话和用户
            String findUserSql = "SELECT s.user_id, u.email, u.is_verified FROM user_sessions s " +
                    "JOIN users u ON s.user_id = u.user_id " +
                    "WHERE s.access_token = ? AND s.expires_at > ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(findUserSql, token, LocalDateTime.now());

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new VerifyEmailResponse(false, "令牌无效或已过期")
                );
            }

            Map<String, Object> user = users.get(0);
            Integer userId = (Integer) user.get("user_id");
            String email = (String) user.get("email");
            Boolean isVerified = (Boolean) user.get("is_verified");

            // 3. 检查邮箱是否已经验证
            if (isVerified != null && isVerified) {
                return ResponseEntity.badRequest().body(
                        new VerifyEmailResponse(false, "邮箱已经验证过了")
                );
            }

            // 4. 生成邮箱验证令牌
            String verificationToken = "verify_" + java.util.UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusHours(24); // 24小时后过期

            // 5. 在实际项目中，这里应该将验证令牌存储到数据库并发送邮件
            // 由于是课设，我们只打印信息
            System.out.println("邮箱验证令牌已生成:");
            System.out.println("用户ID: " + userId);
            System.out.println("邮箱: " + email);
            System.out.println("验证令牌: " + verificationToken);
            System.out.println("过期时间: " + expiresAt);
            System.out.println("验证链接示例: http://localhost:8080/api/auth/verify-email?token=" +
                    verificationToken + "&email=" + email);

            // 6. 返回成功响应
            VerifyEmailResponse response = new VerifyEmailResponse(true, "验证邮件已发送，请查收您的邮箱");
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("发送验证邮件过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new VerifyEmailResponse(false, "服务器内部错误: " + e.getMessage())
            );
        }
    }
}