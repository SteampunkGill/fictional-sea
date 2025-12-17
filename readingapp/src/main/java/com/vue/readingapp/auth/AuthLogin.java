package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.ResultSet; // 保留，但可能不再直接使用
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthLogin {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到登录请求 ===");
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


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new LoginResponse(false, "邮箱或用户名不能为空", null)
                );
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new LoginResponse(false, "密码不能为空", null)
                );
            }

            // 2. 查询用户（支持邮箱或用户名登录）
            // 修改SQL语句：同时检查email和username字段
            String sql = "SELECT user_id, username, email, password_hash, nickname, avatar_url, created_at, last_login_at " +
                    "FROM users WHERE email = ? OR username = ?";

            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, request.getEmail(), request.getEmail());
            printQueryResult(users);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new LoginResponse(false, "邮箱/用户名或密码错误", null)
                );
            }

            Map<String, Object> user = users.get(0);

            // 3. 验证密码（支持BCrypt和明文）
            String passwordHash = (String) user.get("password_hash");
            boolean passwordValid = false;
            // 检查是否为BCrypt哈希（以$2a$, $2b$, $2y$开头）
            if (passwordHash.startsWith("$2a$") || passwordHash.startsWith("$2b$") || passwordHash.startsWith("$2y$")) {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                passwordValid = passwordEncoder.matches(request.getPassword(), passwordHash);
            } else {
                // 明文比较
                passwordValid = passwordHash.equals(request.getPassword());
            }
            if (!passwordValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new LoginResponse(false, "邮箱/用户名或密码错误", null)
                );
            }

            // 4. 删除用户旧的会话
            String deleteSessionSql = "DELETE FROM user_sessions WHERE user_id = ?";
            jdbcTemplate.update(deleteSessionSql, user.get("user_id"));

            // 5. 生成新的token
            String accessToken = "access_" + UUID.randomUUID().toString();
            String refreshToken = "refresh_" + UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            // 1小时后过期
            // 注意：这里 expires_in 是秒，1小时是 3600 秒
            int expiresInSeconds = 3600;

            // 6. 创建新的会话
            String insertSessionSql = "INSERT INTO user_sessions (user_id, access_token, refresh_token, expires_at, created_at) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSessionSql,
                    user.get("user_id"),
                    accessToken,
                    refreshToken,
                    now.plusSeconds(expiresInSeconds), // 使用秒作为过期时间
                    now
            );

            // 7. 更新用户最后登录时间
            String updateUserSql = "UPDATE users SET last_login_at = ? WHERE user_id = ?";
            jdbcTemplate.update(updateUserSql, now, user.get("user_id"));

            // 8. 准备响应数据
            LoginData loginData = new LoginData(accessToken, refreshToken, expiresInSeconds);
            LoginResponse response = new LoginResponse(true, "登录成功", loginData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("登录过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            // 返回更详细的错误信息，但要注意不要泄露敏感信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LoginResponse(false, "服务器内部错误", null) // 移除e.getMessage()，避免泄露内部信息
            );
        }
    }

    // 内部类：登录请求DTO
    public static class LoginRequest {
        private String email;
        private String password;

        // 默认构造函数（用于JSON反序列化）
        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // 内部类：登录响应DTO
    public static class LoginResponse {
        private boolean success;
        private String message;
        private LoginData data;

        public LoginResponse() {}

        public LoginResponse(boolean success, String message, LoginData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LoginData getData() {
            return data;
        }

        public void setData(LoginData data) {
            this.data = data;
        }
    }

    // 内部类：登录数据DTO
    public static class LoginData {
        private String accessToken;
        private String refreshToken;
        private int expiresIn;

        public LoginData() {}

        public LoginData(String accessToken, String refreshToken, int expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}