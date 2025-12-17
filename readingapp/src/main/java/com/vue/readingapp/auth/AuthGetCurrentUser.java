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
public class AuthGetCurrentUser {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取当前用户请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("=======================");
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

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(HttpServletRequest httpRequest) {
        // 打印接收到的请求
        printRequest("获取当前用户信息");

        try {
            // 1. 从请求头获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new CurrentUserResponse(false, "未提供有效的认证令牌", null)
                );
            }

            String token = authHeader.substring(7); // 去掉"Bearer "前缀

            // 2. 查找对应的会话
            // 注意：这里直接查询 LocalDateTime.now() 可能会因为数据库时区问题导致不匹配。
            // 更好的做法是使用数据库函数来获取当前时间，或者传递一个与数据库时区一致的时间对象。
            // 为了保持与原代码一致，这里暂时保留 LocalDateTime.now()。
            String findSessionSql = "SELECT s.user_id FROM user_sessions s WHERE s.access_token = ? AND s.expires_at > ?";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(findSessionSql, token, LocalDateTime.now());
            printQueryResult(sessions);

            if (sessions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new CurrentUserResponse(false, "令牌无效或已过期", null)
                );
            }

            Map<String, Object> session = sessions.get(0);
            Integer userId = (Integer) session.get("user_id");

            // 3. 查询用户详细信息
            String userSql = "SELECT user_id, username, email, nickname, avatar_url, role, is_verified, created_at, last_login_at " +
                    "FROM users WHERE user_id = ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(userSql, userId);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new CurrentUserResponse(false, "用户不存在", null)
                );
            }

            Map<String, Object> user = users.get(0);

            // 4. 获取日期时间对象
            // Spring JDBC 可能会将数据库的 timestamp/datetime 类型映射为 java.sql.Timestamp 或 java.time.LocalDateTime。
            // 这里需要处理这两种情况，以确保得到正确的 LocalDateTime 对象。
            LocalDateTime createdAt = null;
            LocalDateTime lastLoginAt = null;

            Object createdAtObj = user.get("created_at");
            Object lastLoginAtObj = user.get("last_login_at");

            if (createdAtObj instanceof java.sql.Timestamp) {
                createdAt = ((java.sql.Timestamp) createdAtObj).toLocalDateTime();
            } else if (createdAtObj instanceof LocalDateTime) {
                createdAt = (LocalDateTime) createdAtObj;
            }

            if (lastLoginAtObj instanceof java.sql.Timestamp) {
                lastLoginAt = ((java.sql.Timestamp) lastLoginAtObj).toLocalDateTime();
            } else if (lastLoginAtObj instanceof LocalDateTime) {
                lastLoginAt = (LocalDateTime) lastLoginAtObj;
            }

            // 5. 获取用户设置（从user_settings表）
            Preferences preferences = new Preferences();

            try {
                String settingsSql = "SELECT setting_key, setting_value FROM user_settings WHERE user_id = ?";
                List<Map<String, Object>> settings = jdbcTemplate.queryForList(settingsSql, userId);

                for (Map<String, Object> setting : settings) {
                    String key = (String) setting.get("setting_key");
                    String value = (String) setting.get("setting_value");

                    if (key == null || value == null) {
                        continue; // 跳过无效的设置项
                    }

                    switch (key) {
                        case "theme":
                            preferences.setTheme(value);
                            break;
                        case "language":
                            preferences.setLanguage(value);
                            break;
                        case "notification_enabled":
                            preferences.setNotificationEnabled(Boolean.parseBoolean(value));
                            break;
                        case "auto_save":
                            preferences.setAutoSave(Boolean.parseBoolean(value));
                            break;
                        case "reading_mode":
                            preferences.setReadingMode(value);
                            break;
                        case "font_size":
                            try {
                                preferences.setFontSize(Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                System.err.println("无法将 font_size '" + value + "' 解析为整数: " + e.getMessage());
                            }
                            break;
                        case "line_height":
                            try {
                                preferences.setLineHeight(Double.parseDouble(value));
                            } catch (NumberFormatException e) {
                                System.err.println("无法将 line_height '" + value + "' 解析为双精度浮点数: " + e.getMessage());
                            }
                            break;
                        default:
                            // 可以选择记录未知设置键，或者忽略
                            System.out.println("未知用户设置键: " + key);
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("获取用户设置失败，将使用默认设置: " + e.getMessage());
                // 可以在这里根据需要打印堆栈跟踪
                // e.printStackTrace();
            }

            // 6. 构建用户对象
            CurrentUserDTO currentUser = new CurrentUserDTO(
                    (Integer) user.get("user_id"),
                    (String) user.get("username"),
                    (String) user.get("email"),
                    (String) user.get("nickname"),
                    (String) user.get("avatar_url"),
                    (String) user.get("role"),
                    (Boolean) user.get("is_verified"),
                    createdAt,
                    lastLoginAt,
                    preferences
            );

            CurrentUserData userData = new CurrentUserData(currentUser);
            CurrentUserResponse response = new CurrentUserResponse(true, "获取用户信息成功", userData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取当前用户过程中发生错误: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以帮助调试
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new CurrentUserResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // 内部类：当前用户响应DTO
    public static class CurrentUserResponse {
        private boolean success;
        private String message;
        private CurrentUserData data;

        public CurrentUserResponse() {}

        public CurrentUserResponse(boolean success, String message, CurrentUserData data) {
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

        public CurrentUserData getData() {
            return data;
        }

        public void setData(CurrentUserData data) {
            this.data = data;
        }
    }

    // 内部类：当前用户数据DTO
    public static class CurrentUserData {
        private CurrentUserDTO currentUser;

        public CurrentUserData() {}

        public CurrentUserData(CurrentUserDTO currentUser) {
            this.currentUser = currentUser;
        }

        public CurrentUserDTO getCurrentUser() {
            return currentUser;
        }

        public void setCurrentUser(CurrentUserDTO currentUser) {
            this.currentUser = currentUser;
        }
    }

    // 内部类：当前用户DTO
    public static class CurrentUserDTO {
        private Integer id;
        private String username;
        private String email;
        private String nickname;
        private String avatarUrl;
        private String role;
        private Boolean isVerified;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
        private Preferences preferences;

        public CurrentUserDTO() {}

        public CurrentUserDTO(Integer id, String username, String email, String nickname, String avatarUrl,
                              String role, Boolean isVerified, LocalDateTime createdAt, LocalDateTime lastLoginAt,
                              Preferences preferences) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.nickname = nickname;
            this.avatarUrl = avatarUrl;
            this.role = role;
            this.isVerified = isVerified;
            this.createdAt = createdAt;
            this.lastLoginAt = lastLoginAt;
            this.preferences = preferences;
        }

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

        public Preferences getPreferences() {
            return preferences;
        }

        public void setPreferences(Preferences preferences) {
            this.preferences = preferences;
        }
    }

    // 内部类：用户偏好设置
    public static class Preferences {
        private String theme;
        private String language;
        private Boolean notificationEnabled;
        private Boolean autoSave;
        private String readingMode;
        private Integer fontSize;
        private Double lineHeight;

        public Preferences() {}

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Boolean getNotificationEnabled() {
            return notificationEnabled;
        }

        public void setNotificationEnabled(Boolean notificationEnabled) {
            this.notificationEnabled = notificationEnabled;
        }

        public Boolean getAutoSave() {
            return autoSave;
        }

        public void setAutoSave(Boolean autoSave) {
            this.autoSave = autoSave;
        }

        public String getReadingMode() {
            return readingMode;
        }

        public void setReadingMode(String readingMode) {
            this.readingMode = readingMode;
        }

        public Integer getFontSize() {
            return fontSize;
        }

        public void setFontSize(Integer fontSize) {
            this.fontSize = fontSize;
        }

        public Double getLineHeight() {
            return lineHeight;
        }

        public void setLineHeight(Double lineHeight) {
            this.lineHeight = lineHeight;
        }
    }
}