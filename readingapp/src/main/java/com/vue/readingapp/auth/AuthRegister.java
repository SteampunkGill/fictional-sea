package com.vue.readingapp.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRegister {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到注册请求 ===");
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


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "邮箱不能为空", null)
                );
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "密码不能为空", null)
                );
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "用户名不能为空", null)
                );
            }

            // 2. 验证邮箱格式（简单验证）
            if (!request.getEmail().contains("@")) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "邮箱格式不正确", null)
                );
            }

            // 3. 验证密码长度
            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "密码长度不能少于6位", null)
                );
            }

            // 4. 检查邮箱是否已存在
            String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ?";
            Integer emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, request.getEmail());

            if (emailCount > 0) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "该邮箱已被注册", null)
                );
            }

            // 5. 检查用户名是否已存在
            String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            Integer usernameCount = jdbcTemplate.queryForObject(checkUsernameSql, Integer.class, request.getUsername());

            if (usernameCount > 0) {
                return ResponseEntity.badRequest().body(
                        new RegisterResponse(false, "该用户名已被使用", null)
                );
            }

            // 6. 设置昵称（如果没有提供，使用用户名）
            String nickname = request.getNickname();
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = request.getUsername();
            }

            // 7. 创建用户
            LocalDateTime now = LocalDateTime.now();
            String insertUserSql = "INSERT INTO users (username, email, password_hash, nickname, avatar_url, role, is_verified, created_at, last_login_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // 密码简单处理（实际项目应该加密）
            String passwordHash = request.getPassword();
            String avatarUrl = "";
            String role = "user";
            Boolean isVerified = false;

            jdbcTemplate.update(insertUserSql,
                    request.getUsername(),
                    request.getEmail(),
                    passwordHash,
                    nickname,
                    avatarUrl,
                    role,
                    isVerified,
                    now,
                    now
            );

            // 8. 获取新创建的用户ID
            String getUserIdSql = "SELECT user_id FROM users WHERE email = ?";
            Integer userId = jdbcTemplate.queryForObject(getUserIdSql, Integer.class, request.getEmail());

            // 9. 创建用户会话（自动登录）
            String accessToken = "access_" + UUID.randomUUID().toString();
            String refreshToken = "refresh_" + UUID.randomUUID().toString();

            String insertSessionSql = "INSERT INTO user_sessions (user_id, access_token, refresh_token, expires_at, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSessionSql,
                    userId,
                    accessToken,
                    refreshToken,
                    now.plusHours(1),
                    now
            );

            // 10. 准备响应数据
            User user = new User();
            user.setId(userId);
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setNickname(nickname);
            user.setAvatarUrl(avatarUrl);
            user.setRole(role);
            user.setIsVerified(isVerified);
            user.setCreatedAt(now);
            user.setLastLoginAt(now);
            // 设置其他字段为默认值
            user.setPasswordHash(passwordHash);
            user.setEmailVerifiedAt(null);
            user.setUpdatedAt(now);
            user.setBio(null);
            user.setLocation(null);
            user.setWebsite(null);

            UserData userData = new UserData(user);
            RegisterResponse response = new RegisterResponse(true, "注册成功", userData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("注册过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new RegisterResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // 内部类：注册请求DTO
    public static class RegisterRequest {
        private String email;
        private String password;
        private String username;
        private String nickname;

        public RegisterRequest() {}

        public RegisterRequest(String email, String password, String username, String nickname) {
            this.email = email;
            this.password = password;
            this.username = username;
            this.nickname = nickname;
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

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    // 内部类：注册响应DTO
    public static class RegisterResponse {
        private boolean success;
        private String message;
        private UserData data;

        public RegisterResponse() {}

        public RegisterResponse(boolean success, String message, UserData data) {
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

        public UserData getData() {
            return data;
        }

        public void setData(UserData data) {
            this.data = data;
        }
    }

    // 内部类：用户数据DTO
    public static class UserData {
        private User user;

        public UserData() {}

        public UserData(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    // 内部类：用户实体
    public static class User {
        private Integer id;
        private String username;
        private String email;
        private String nickname;
        private String avatarUrl;
        private String role;
        private Boolean isVerified;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
        private String passwordHash;
        private LocalDateTime emailVerifiedAt;
        private LocalDateTime updatedAt;
        private String bio;
        private String location;
        private String website;

        public User() {}

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Boolean getIsVerified() {
            return isVerified;
        }

        public void setIsVerified(Boolean isVerified) {
            this.isVerified = isVerified;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getLastLoginAt() {
            return lastLoginAt;
        }

        public void setLastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }

        public LocalDateTime getEmailVerifiedAt() {
            return emailVerifiedAt;
        }

        public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
            this.emailVerifiedAt = emailVerifiedAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }
    }
}