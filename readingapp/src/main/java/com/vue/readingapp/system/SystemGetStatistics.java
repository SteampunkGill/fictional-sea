
package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetStatistics {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取系统统计信息请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
        System.out.println("============================");
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
    public static class StatisticsRequest {
        private String period;
        private List<String> metrics;

        public String getPeriod() { return period != null ? period : "7d"; }
        public void setPeriod(String period) { this.period = period; }

        public List<String> getMetrics() {
            if (metrics == null || metrics.isEmpty()) {
                return new ArrayList<>(java.util.Arrays.asList("users", "documents", "vocabulary", "reviews"));
            }
            return metrics;
        }
        public void setMetrics(List<String> metrics) { this.metrics = metrics; }
    }

    // 响应DTO
    public static class StatisticsResponse {
        private boolean success;
        private String message;
        private StatisticsData data;

        public StatisticsResponse(boolean success, String message, StatisticsData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public StatisticsData getData() { return data; }
        public void setData(StatisticsData data) { this.data = data; }
    }

    public static class StatisticsData {
        private String period;
        private MetricsInfo metrics;
        private Map<String, List<Integer>> trends;

        public StatisticsData(String period, MetricsInfo metrics, Map<String, List<Integer>> trends) {
            this.period = period;
            this.metrics = metrics;
            this.trends = trends;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public MetricsInfo getMetrics() { return metrics; }
        public void setMetrics(MetricsInfo metrics) { this.metrics = metrics; }

        public Map<String, List<Integer>> getTrends() { return trends; }
        public void setTrends(Map<String, List<Integer>> trends) { this.trends = trends; }
    }

    public static class MetricsInfo {
        private UserMetrics users;
        private DocumentMetrics documents;
        private VocabularyMetrics vocabulary;
        private ReviewMetrics reviews;

        public MetricsInfo(UserMetrics users, DocumentMetrics documents,
                           VocabularyMetrics vocabulary, ReviewMetrics reviews) {
            this.users = users;
            this.documents = documents;
            this.vocabulary = vocabulary;
            this.reviews = reviews;
        }

        public UserMetrics getUsers() { return users; }
        public void setUsers(UserMetrics users) { this.users = users; }

        public DocumentMetrics getDocuments() { return documents; }
        public void setDocuments(DocumentMetrics documents) { this.documents = documents; }

        public VocabularyMetrics getVocabulary() { return vocabulary; }
        public void setVocabulary(VocabularyMetrics vocabulary) { this.vocabulary = vocabulary; }

        public ReviewMetrics getReviews() { return reviews; }
        public void setReviews(ReviewMetrics reviews) { this.reviews = reviews; }
    }

    public static class UserMetrics {
        private int total;
        private int active;
        private int newUsers;
        private double growth;

        public UserMetrics(int total, int active, int newUsers, double growth) {
            this.total = total;
            this.active = active;
            this.newUsers = newUsers;
            this.growth = growth;
        }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getActive() { return active; }
        public void setActive(int active) { this.active = active; }

        public int getNewUsers() { return newUsers; }
        public void setNewUsers(int newUsers) { this.newUsers = newUsers; }

        public double getGrowth() { return growth; }
        public void setGrowth(double growth) { this.growth = growth; }
    }

    public static class DocumentMetrics {
        private int total;
        private int uploaded;
        private int processed;
        private int failed;

        public DocumentMetrics(int total, int uploaded, int processed, int failed) {
            this.total = total;
            this.uploaded = uploaded;
            this.processed = processed;
            this.failed = failed;
        }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getUploaded() { return uploaded; }
        public void setUploaded(int uploaded) { this.uploaded = uploaded; }

        public int getProcessed() { return processed; }
        public void setProcessed(int processed) { this.processed = processed; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }
    }

    public static class VocabularyMetrics {
        private int total;
        private int added;
        private int reviewed;
        private int mastered;

        public VocabularyMetrics(int total, int added, int reviewed, int mastered) {
            this.total = total;
            this.added = added;
            this.reviewed = reviewed;
            this.mastered = mastered;
        }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getAdded() { return added; }
        public void setAdded(int added) { this.added = added; }

        public int getReviewed() { return reviewed; }
        public void setReviewed(int reviewed) { this.reviewed = reviewed; }

        public int getMastered() { return mastered; }
        public void setMastered(int mastered) { this.mastered = mastered; }
    }

    public static class ReviewMetrics {
        private int total;
        private int completed;
        private double accuracy;
        private int averageTime;

        public ReviewMetrics(int total, int completed, double accuracy, int averageTime) {
            this.total = total;
            this.completed = completed;
            this.accuracy = accuracy;
            this.averageTime = averageTime;
        }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }

        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

        public int getAverageTime() { return averageTime; }
        public void setAverageTime(int averageTime) { this.averageTime = averageTime; }
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(@RequestParam(required = false) String period,
                                                            @RequestParam(required = false) List<String> metrics) {

        // 创建请求对象
        StatisticsRequest request = new StatisticsRequest();
        request.setPeriod(period);
        request.setMetrics(metrics);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 查询用户统计
            int totalUsers = 0;
            int activeUsers = 0;
            int newUsers = 0;

            try {
                // 查询总用户数
                String totalUsersSql = "SELECT COUNT(*) FROM users";
                totalUsers = jdbcTemplate.queryForObject(totalUsersSql, Integer.class);

                // 查询活跃用户数（最近7天有活动的用户）
                String activeUsersSql = "SELECT COUNT(DISTINCT user_id) FROM reading_history WHERE start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
                activeUsers = jdbcTemplate.queryForObject(activeUsersSql, Integer.class);

                // 查询新用户数（最近7天注册的用户）
                String newUsersSql = "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
                newUsers = jdbcTemplate.queryForObject(newUsersSql, Integer.class);
            } catch (Exception e) {
                System.out.println("查询用户统计时发生错误: " + e.getMessage());
            }

            // 2. 查询文档统计
            int totalDocuments = 0;
            int uploadedDocuments = 0;
            int processedDocuments = 0;
            int failedDocuments = 0;

            try {
                // 查询总文档数
                String totalDocsSql = "SELECT COUNT(*) FROM documents";
                totalDocuments = jdbcTemplate.queryForObject(totalDocsSql, Integer.class);

                // 查询已上传文档数
                String uploadedDocsSql = "SELECT COUNT(*) FROM documents WHERE status = 'uploaded'";
                uploadedDocuments = jdbcTemplate.queryForObject(uploadedDocsSql, Integer.class);

                // 查询已处理文档数
                String processedDocsSql = "SELECT COUNT(*) FROM documents WHERE status = 'processed'";
                processedDocuments = jdbcTemplate.queryForObject(processedDocsSql, Integer.class);

                // 查询失败文档数
                String failedDocsSql = "SELECT COUNT(*) FROM documents WHERE status = 'failed'";
                failedDocuments = jdbcTemplate.queryForObject(failedDocsSql, Integer.class);
            } catch (Exception e) {
                System.out.println("查询文档统计时发生错误: " + e.getMessage());
            }

            // 3. 查询词汇统计
            int totalVocabulary = 0;
            int addedVocabulary = 0;
            int reviewedVocabulary = 0;
            int masteredVocabulary = 0;

            try {
                // 查询总词汇数
                String totalVocabSql = "SELECT COUNT(*) FROM user_vocabulary";
                totalVocabulary = jdbcTemplate.queryForObject(totalVocabSql, Integer.class);

                // 查询最近7天添加的词汇数
                String addedVocabSql = "SELECT COUNT(*) FROM user_vocabulary WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
                addedVocabulary = jdbcTemplate.queryForObject(addedVocabSql, Integer.class);

                // 查询已复习词汇数
                String reviewedVocabSql = "SELECT COUNT(*) FROM user_vocabulary WHERE review_count > 0";
                reviewedVocabulary = jdbcTemplate.queryForObject(reviewedVocabSql, Integer.class);

                // 查询已掌握词汇数
                String masteredVocabSql = "SELECT COUNT(*) FROM user_vocabulary WHERE mastery_level >= 0.8";
                masteredVocabulary = jdbcTemplate.queryForObject(masteredVocabSql, Integer.class);
            } catch (Exception e) {
                System.out.println("查询词汇统计时发生错误: " + e.getMessage());
            }

            // 4. 查询复习统计
            int totalReviews = 0;
            int completedReviews = 0;
            double reviewAccuracy = 0.0;
            int averageReviewTime = 0;

            try {
                // 查询总复习次数
                String totalReviewsSql = "SELECT COUNT(*) FROM review_items";
                totalReviews = jdbcTemplate.queryForObject(totalReviewsSql, Integer.class);

                // 查询已完成的复习次数
                String completedReviewsSql = "SELECT COUNT(*) FROM review_items WHERE is_completed = TRUE";
                completedReviews = jdbcTemplate.queryForObject(completedReviewsSql, Integer.class);

                // 计算复习准确率
                if (totalReviews > 0) {
                    reviewAccuracy = (double) completedReviews / totalReviews * 100;
                }

                // 平均复习时间（模拟）
                averageReviewTime = 45;
            } catch (Exception e) {
                System.out.println("查询复习统计时发生错误: " + e.getMessage());
            }

            // 5. 构建指标数据
            UserMetrics userMetrics = new UserMetrics(
                    totalUsers,
                    activeUsers,
                    newUsers,
                    5.2  // 模拟增长率
            );

            DocumentMetrics documentMetrics = new DocumentMetrics(
                    totalDocuments,
                    uploadedDocuments,
                    processedDocuments,
                    failedDocuments
            );

            VocabularyMetrics vocabularyMetrics = new VocabularyMetrics(
                    totalVocabulary,
                    addedVocabulary,
                    reviewedVocabulary,
                    masteredVocabulary
            );

            ReviewMetrics reviewMetrics = new ReviewMetrics(
                    totalReviews,
                    completedReviews,
                    reviewAccuracy,
                    averageReviewTime
            );

            MetricsInfo metricsInfo = new MetricsInfo(
                    userMetrics,
                    documentMetrics,
                    vocabularyMetrics,
                    reviewMetrics
            );

            // 6. 构建趋势数据
            Map<String, List<Integer>> trends = new HashMap<>();

            // 用户趋势（模拟）
            List<Integer> userTrend = new ArrayList<>();
            userTrend.add(totalUsers - 500);
            userTrend.add(totalUsers - 300);
            userTrend.add(totalUsers - 100);
            userTrend.add(totalUsers);
            trends.put("users", userTrend);

            // 文档趋势（模拟）
            List<Integer> documentTrend = new ArrayList<>();
            documentTrend.add(totalDocuments - 500);
            documentTrend.add(totalDocuments - 300);
            documentTrend.add(totalDocuments - 100);
            documentTrend.add(totalDocuments);
            trends.put("documents", documentTrend);

            // 词汇趋势（模拟）
            List<Integer> vocabularyTrend = new ArrayList<>();
            vocabularyTrend.add(totalVocabulary - 5000);
            vocabularyTrend.add(totalVocabulary - 3000);
            vocabularyTrend.add(totalVocabulary - 1000);
            vocabularyTrend.add(totalVocabulary);
            trends.put("vocabulary", vocabularyTrend);

            // 7. 构建响应数据
            StatisticsData statisticsData = new StatisticsData(
                    request.getPeriod(),
                    metricsInfo,
                    trends
            );

            StatisticsResponse response = new StatisticsResponse(true, "获取系统统计信息成功", statisticsData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取系统统计信息过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new StatisticsResponse(false, "获取系统统计信息失败: " + e.getMessage(), null)
            );
        }
    }
}