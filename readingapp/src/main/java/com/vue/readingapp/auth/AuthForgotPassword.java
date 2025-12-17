package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate; // 导入 JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder; // 用于获取自增ID
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement; // 导入 PreparedStatement
import java.sql.Timestamp; // 导入 Timestamp
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthForgotPassword {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JdbcTemplate jdbcTemplate; // 注入 JdbcTemplate

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("====================");
    }

    // 打印返回数据
    private void printResponse(Object response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // 请求DTO for forgot-password
    public static class ForgotPasswordRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "ForgotPasswordRequest{" +
                    "email='" + email + '\'' +
                    '}';
        }
    }

    // 验证验证码请求DTO
    public static class VerifyCodeRequest {
        private String email;
        private String code;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        @Override
        public String toString() {
            return "VerifyCodeRequest{" +
                    "email='" + email + '\'' +
                    ", code='" + code + '\'' +
                    '}';
        }
    }

    // 响应DTO
    public static class ForgotPasswordResponse {
        private boolean success;
        private String message;

        public ForgotPasswordResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // 生成4位随机验证码
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // 生成1000-9999之间的随机数
        return String.valueOf(code);
    }

    // 发送验证码邮件并存储到数据库
    private boolean sendVerificationEmailAndStoreToken(String toEmail, String verificationCode, LocalDateTime expiresAt) {
        try {
            // 1. 尝试插入token到数据库
            String insertSql = "INSERT INTO password_reset_tokens (user_id, token, expires_at, created_at) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            // 注意：这里需要一个 user_id。在 forgotPassword 中，我们可能还没有 user_id，
            // 所以需要先查询 user_id。如果不存在，则不能发送邮件。
            // 假定我们已经有了 user_id（需要从其他地方传入或在方法内部查询）。
            // 为了简化，这里暂时假设 user_id 是已知的。
            // 在实际应用中，应该先查询用户是否存在，并获取其 user_id。
            Integer userId = getUserIdByEmail(toEmail); // 假设有此方法
            if (userId == null) {
                System.err.println("邮箱 " + toEmail + " 未注册，无法发送密码重置邮件。");
                return false;
            }

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"token_id"});
                ps.setInt(1, userId);
                ps.setString(2, verificationCode);
                ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);

            // 2. 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("密码重置验证码 - 阅读应用");

            String emailContent = "您的密码重置验证码是：\n\n" +
                    verificationCode + "\n\n" +
                    "该验证码将在10分钟内有效。\n\n" +
                    "如果您没有请求重置密码，请忽略此邮件。\n\n" +
                    "谢谢，\n" +
                    "阅读应用团队";

            message.setText(emailContent);

            mailSender.send(message);
            System.out.println("验证码邮件已发送到: " + toEmail);
            System.out.println("Token 已存入数据库，token_id: " + keyHolder.getKey().intValue());
            return true;
        } catch (Exception e) {
            System.err.println("发送邮件或存储Token失败: " + e.getMessage());
            e.printStackTrace();
            // 如果发送邮件或插入数据库失败，需要回滚（不删除刚插入的数据，因为事务可能未开始或已提交）
            // 这里简单打印错误，实际应用需要更健壮的错误处理和回滚机制
            return false;
        }
    }

    // 用于获取用户ID的方法（需要实现）
    private Integer getUserIdByEmail(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, email);
        if (result.isEmpty()) {
            return null;
        }
        return (Integer) result.get(0).get("user_id");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "邮箱不能为空")
                );
            }

            // 2. 验证邮箱格式
            String email = request.getEmail().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "邮箱格式不正确")
                );
            }

            // 3. 检查邮箱是否存在于数据库
            Integer userId = getUserIdByEmail(email);
            if (userId == null) {
                // 不直接暴露邮箱不存在，而是返回通用错误
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "若此邮箱已注册，我们将发送密码重置链接。")
                );
            }

            // 4. 生成4位验证码
            String verificationCode = generateVerificationCode();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(10); // 10分钟后过期

            // 5. 发送验证码邮件并存储到数据库
            boolean success = sendVerificationEmailAndStoreToken(email, verificationCode, expiresAt);

            if (!success) {
                // 如果邮件发送或Token存储失败
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new ForgotPasswordResponse(false, "操作失败，请稍后重试")
                );
            }

            System.out.println("验证码已生成（仅用于调试，不应返回给前端）：" + verificationCode);
            System.out.println("过期时间: " + expiresAt);

            // 6. 返回成功响应 (不再返回验证码)
            ForgotPasswordResponse response = new ForgotPasswordResponse(true,
                    "验证码已发送到您的注册邮箱，请查收并按照指示操作。");
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("忘记密码过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ForgotPasswordResponse(false, "服务器内部错误，请稍后重试")
            );
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ForgotPasswordResponse> verifyCode(@RequestBody VerifyCodeRequest request) {
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "邮箱不能为空")
                );
            }

            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "验证码不能为空")
                );
            }

            String email = request.getEmail().trim();
            String code = request.getCode().trim();

            // 2. 检查邮箱是否存在
            Integer userId = getUserIdByEmail(email);
            if (userId == null) {
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "无效的邮箱或验证码")
                );
            }

            // 3. 从数据库查询验证码
            String selectSql = "SELECT token_id, token, expires_at FROM password_reset_tokens WHERE user_id = ? AND token = ?";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(selectSql, userId, code);

            if (result.isEmpty()) {
                // 验证码不存在或已被使用
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "无效的验证码或验证码已过期，请重新获取")
                );
            }

            Map<String, Object> tokenInfo = result.get(0);
            LocalDateTime expiresAt = ((Timestamp) tokenInfo.get("expires_at")).toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();

            // 4. 检查验证码是否过期
            if (now.isAfter(expiresAt)) {
                // 删除过期的令牌
                String deleteSql = "DELETE FROM password_reset_tokens WHERE token_id = ?";
                jdbcTemplate.update(deleteSql, tokenInfo.get("token_id"));
                return ResponseEntity.badRequest().body(
                        new ForgotPasswordResponse(false, "验证码已过期，请重新获取")
                );
            }



            // 6. 返回成功响应
            ForgotPasswordResponse response = new ForgotPasswordResponse(true, "验证码验证成功");
            printResponse(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("验证验证码过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ForgotPasswordResponse(false, "服务器内部错误，请稍后重试")
            );
        }
    }
}