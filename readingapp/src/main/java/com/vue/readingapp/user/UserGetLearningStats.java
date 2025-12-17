package com.vue.readingapp.user;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserGetLearningStats {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取学习统计请求 ===");
        System.out.println("请求参数: " + request);
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
    public static class LearningStatsResponse {
        private boolean success;
        private String message;
        private LearningStatsData stats;

        public LearningStatsResponse(boolean success, String message, LearningStatsData stats) {
            this.success = success;
            this.message = message;
            this.stats = stats;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LearningStatsData getStats() { return stats; }
        public void setStats(LearningStatsData stats) { this.stats = stats; }
    }

    public static class LearningStatsData {
        private int totalReadingTime;
        private String formattedReadingTime;
        private int documentsRead;
        private int wordsLearned;
        private int reviewsCompleted;
        private double reviewAccuracy;
        private String formattedReviewAccuracy;
        private int streakDays;
        private int longestStreak;
        private TodayProgress todayProgress;
        private WeeklyProgress weeklyProgress;
        private MonthlyProgress monthlyProgress;
        private Map<String, Object> byLanguage;
        private int achievementsUnlocked;
        private int totalAchievements;

        public LearningStatsData() {
            this.todayProgress = new TodayProgress();
            this.weeklyProgress = new WeeklyProgress();
            this.monthlyProgress = new MonthlyProgress();
            this.byLanguage = new HashMap<>();
        }

        // Getters and Setters
        public int getTotalReadingTime() { return totalReadingTime; }
        public void setTotalReadingTime(int totalReadingTime) { this.totalReadingTime = totalReadingTime; }

        public String getFormattedReadingTime() { return formattedReadingTime; }
        public void setFormattedReadingTime(String formattedReadingTime) { this.formattedReadingTime = formattedReadingTime; }

        public int getDocumentsRead() { return documentsRead; }
        public void setDocumentsRead(int documentsRead) { this.documentsRead = documentsRead; }

        public int getWordsLearned() { return wordsLearned; }
        public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }

        public int getReviewsCompleted() { return reviewsCompleted; }
        public void setReviewsCompleted(int reviewsCompleted) { this.reviewsCompleted = reviewsCompleted; }

        public double getReviewAccuracy() { return reviewAccuracy; }
        public void setReviewAccuracy(double reviewAccuracy) { this.reviewAccuracy = reviewAccuracy; }

        public String getFormattedReviewAccuracy() { return formattedReviewAccuracy; }
        public void setFormattedReviewAccuracy(String formattedReviewAccuracy) { this.formattedReviewAccuracy = formattedReviewAccuracy; }

        public int getStreakDays() { return streakDays; }
        public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

        public TodayProgress getTodayProgress() { return todayProgress; }
        public void setTodayProgress(TodayProgress todayProgress) { this.todayProgress = todayProgress; }

        public WeeklyProgress getWeeklyProgress() { return weeklyProgress; }
        public void setWeeklyProgress(WeeklyProgress weeklyProgress) { this.weeklyProgress = weeklyProgress; }

        public MonthlyProgress getMonthlyProgress() { return monthlyProgress; }
        public void setMonthlyProgress(MonthlyProgress monthlyProgress) { this.monthlyProgress = monthlyProgress; }

        public Map<String, Object> getByLanguage() { return byLanguage; }
        public void setByLanguage(Map<String, Object> byLanguage) { this.byLanguage = byLanguage; }

        public int getAchievementsUnlocked() { return achievementsUnlocked; }
        public void setAchievementsUnlocked(int achievementsUnlocked) { this.achievementsUnlocked = achievementsUnlocked; }

        public int getTotalAchievements() { return totalAchievements; }
        public void setTotalAchievements(int totalAchievements) { this.totalAchievements = totalAchievements; }
    }

    public static class TodayProgress {
        private int readingTime = 0;
        private int wordsLearned = 0;
        private int reviewsCompleted = 0;

        public int getReadingTime() { return readingTime; }
        public void setReadingTime(int readingTime) { this.readingTime = readingTime; }

        public int getWordsLearned() { return wordsLearned; }
        public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }

        public int getReviewsCompleted() { return reviewsCompleted; }
        public void setReviewsCompleted(int reviewsCompleted) { this.reviewsCompleted = reviewsCompleted; }
    }

    public static class WeeklyProgress {
        private int readingTime = 0;
        private int wordsLearned = 0;
        private int reviewsCompleted = 0;

        public int getReadingTime() { return readingTime; }
        public void setReadingTime(int readingTime) { this.readingTime = readingTime; }

        public int getWordsLearned() { return wordsLearned; }
        public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }

        public int getReviewsCompleted() { return reviewsCompleted; }
        public void setReviewsCompleted(int reviewsCompleted) { this.reviewsCompleted = reviewsCompleted; }
    }

    public static class MonthlyProgress {
        private int readingTime = 0;
        private int wordsLearned = 0;
        private int reviewsCompleted = 0;

        public int getReadingTime() { return readingTime; }
        public void setReadingTime(int readingTime) { this.readingTime = readingTime; }

        public int getWordsLearned() { return wordsLearned; }
        public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }

        public int getReviewsCompleted() { return reviewsCompleted; }
        public void setReviewsCompleted(int reviewsCompleted) { this.reviewsCompleted = reviewsCompleted; }
    }

    @GetMapping("/learning-stats")
    public ResponseEntity<LearningStatsResponse> getLearningStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "period", defaultValue = "week") String period,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        // 打印接收到的请求
        Map<String, String> params = new HashMap<>();
        params.put("period", period);
        params.put("language", language);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        printRequest(params);

        try {
            // 1. 验证token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new LearningStatsResponse(false, "请先登录", null)
                );
            }

            String accessToken = authHeader.substring(7);

            // 2. 验证token有效性
            String tokenSql = "SELECT user_id, expires_at FROM user_sessions WHERE access_token = ? AND expires_at > NOW()";
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(tokenSql, accessToken);

            if (sessions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new LearningStatsResponse(false, "登录已过期，请重新登录", null)
                );
            }

            Map<String, Object> session = sessions.get(0);
            int userId = (int) session.get("user_id");

            // 3. 验证周期参数
            String[] validPeriods = {"day", "week", "month", "year", "all"};
            boolean validPeriod = false;
            for (String p : validPeriods) {
                if (p.equals(period)) {
                    validPeriod = true;
                    break;
                }
            }
            if (!validPeriod) {
                period = "week";
            }

            // 4. 查询学习统计
            LearningStatsData stats = new LearningStatsData();

            // 4.1 查询总阅读时间（从reading_history表）
            String readingTimeSql = "SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, start_time, end_time)), 0) as total_seconds " +
                    "FROM reading_history WHERE user_id = ?";
            Map<String, Object> readingTimeResult = jdbcTemplate.queryForMap(readingTimeSql, userId);
            int totalSeconds = ((Number) readingTimeResult.get("total_seconds")).intValue();
            stats.setTotalReadingTime(totalSeconds);
            stats.setFormattedReadingTime(formatDuration(totalSeconds));

            // 4.2 查询已读文档数量
            String docsSql = "SELECT COUNT(DISTINCT document_id) as count FROM reading_history WHERE user_id = ?";
            Map<String, Object> docsResult = jdbcTemplate.queryForMap(docsSql, userId);
            stats.setDocumentsRead(((Number) docsResult.get("count")).intValue());

            // 4.3 查询已学单词数量
            String wordsSql = "SELECT COUNT(*) as count FROM user_vocabulary WHERE user_id = ?";
            Map<String, Object> wordsResult = jdbcTemplate.queryForMap(wordsSql, userId);
            stats.setWordsLearned(((Number) wordsResult.get("count")).intValue());

            // 4.4 查询已完成复习数量
            String reviewsSql = "SELECT COUNT(*) as count FROM review_sessions WHERE user_id = ? AND status = 'completed'";
            Map<String, Object> reviewsResult = jdbcTemplate.queryForMap(reviewsSql, userId);
            stats.setReviewsCompleted(((Number) reviewsResult.get("count")).intValue());

            // 4.5 查询复习准确率
            String accuracySql = "SELECT COALESCE(AVG(accuracy), 0) as avg_accuracy FROM review_sessions " +
                    "WHERE user_id = ? AND status = 'completed'";
            Map<String, Object> accuracyResult = jdbcTemplate.queryForMap(accuracySql, userId);
            double accuracy = ((Number) accuracyResult.get("avg_accuracy")).doubleValue();
            stats.setReviewAccuracy(accuracy);
            stats.setFormattedReviewAccuracy(formatPercentage(accuracy, 100, 1));

            // 4.6 查询连续学习天数
            String streakSql = "SELECT MAX(streak_days) as max_streak FROM daily_learning_stats WHERE user_id = ?";
            Map<String, Object> streakResult = jdbcTemplate.queryForMap(streakSql, userId);
            stats.setLongestStreak(((Number) streakResult.get("max_streak")).intValue());

            // 4.7 查询今日进度
            String todaySql = "SELECT reading_time, words_learned, reviews_completed FROM daily_learning_stats " +
                    "WHERE user_id = ? AND date = CURDATE()";
            List<Map<String, Object>> todayResults = jdbcTemplate.queryForList(todaySql, userId);
            if (!todayResults.isEmpty()) {
                Map<String, Object> today = todayResults.get(0);
                stats.getTodayProgress().setReadingTime(((Number) today.get("reading_time")).intValue());
                stats.getTodayProgress().setWordsLearned(((Number) today.get("words_learned")).intValue());
                stats.getTodayProgress().setReviewsCompleted(((Number) today.get("reviews_completed")).intValue());
            }

            // 4.8 查询按语言统计
            String languageSql = "SELECT language, COUNT(*) as count FROM documents d " +
                    "JOIN reading_history rh ON d.document_id = rh.document_id " +
                    "WHERE rh.user_id = ? GROUP BY language";
            List<Map<String, Object>> languageResults = jdbcTemplate.queryForList(languageSql, userId);
            for (Map<String, Object> lang : languageResults) {
                stats.getByLanguage().put((String) lang.get("language"), lang.get("count"));
            }

            // 4.9 查询成就数量
            String achievementsSql = "SELECT COUNT(*) as unlocked FROM user_achievements WHERE user_id = ?";
            Map<String, Object> achievementsResult = jdbcTemplate.queryForMap(achievementsSql, userId);
            stats.setAchievementsUnlocked(((Number) achievementsResult.get("unlocked")).intValue());

            // 总成就数量
            String totalAchievementsSql = "SELECT COUNT(*) as total FROM learning_achievements";
            Map<String, Object> totalAchievementsResult = jdbcTemplate.queryForMap(totalAchievementsSql);
            stats.setTotalAchievements(((Number) totalAchievementsResult.get("total")).intValue());

            printQueryResult(stats);

            // 5. 准备响应数据
            LearningStatsResponse response = new LearningStatsResponse(true, "获取学习统计成功", stats);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取学习统计过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LearningStatsResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // 格式化持续时间
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return minutes + "分钟";
        } else if (seconds < 86400) {
            int hours = seconds / 3600;
            int remainingMinutes = (seconds % 3600) / 60;
            return hours + "小时" + (remainingMinutes > 0 ? remainingMinutes + "分钟" : "");
        } else {
            int days = seconds / 86400;
            int remainingHours = (seconds % 86400) / 3600;
            return days + "天" + (remainingHours > 0 ? remainingHours + "小时" : "");
        }
    }

    // 格式化百分比
    private String formatPercentage(double value, double total, int decimals) {
        if (total == 0) return "0%";
        double percentage = (value / total) * 100;
        return String.format("%." + decimals + "f%%", percentage);
    }
}
