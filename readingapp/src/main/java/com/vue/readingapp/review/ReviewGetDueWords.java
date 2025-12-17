package com.vue.readingapp.review;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/review")
public class ReviewGetDueWords {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取待复习单词请求 ===");
        System.out.println("请求参数: " + request);
        System.out.println("时间: " + LocalDateTime.now());
        System.out.println("========================");
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
    public static class DueWordsResponse {
        private boolean success;
        private String message;
        private DueWordsData data;

        public DueWordsResponse(boolean success, String message, DueWordsData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public DueWordsData getData() { return data; }
        public void setData(DueWordsData data) { this.data = data; }
    }

    public static class DueWordsData {
        private List<DueWord> words;
        private int total;
        private int due_count;

        public DueWordsData(List<DueWord> words, int total, int due_count) {
            this.words = words;
            this.total = total;
            this.due_count = due_count;
        }

        public List<DueWord> getWords() { return words; }
        public void setWords(List<DueWord> words) { this.words = words; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getDue_count() { return due_count; }
        public void setDue_count(int due_count) { this.due_count = due_count; }
    }

    public static class DueWord {
        private int id;
        private String word;
        private String language;
        private String definition;
        private String example;
        private String phonetic;
        private String part_of_speech;
        private int mastery_level;
        private int review_count;
        private String last_reviewed_at;
        private String next_review_at;
        private String difficulty;
        private List<String> tags;
        private String source;
        private String due_reason;
        private int priority;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }

        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }

        public String getPhonetic() { return phonetic; }
        public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

        public String getPart_of_speech() { return part_of_speech; }
        public void setPart_of_speech(String part_of_speech) { this.part_of_speech = part_of_speech; }

        public int getMastery_level() { return mastery_level; }
        public void setMastery_level(int mastery_level) { this.mastery_level = mastery_level; }

        public int getReview_count() { return review_count; }
        public void setReview_count(int review_count) { this.review_count = review_count; }

        public String getLast_reviewed_at() { return last_reviewed_at; }
        public void setLast_reviewed_at(String last_reviewed_at) { this.last_reviewed_at = last_reviewed_at; }

        public String getNext_review_at() { return next_review_at; }
        public void setNext_review_at(String next_review_at) { this.next_review_at = next_review_at; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getDue_reason() { return due_reason; }
        public void setDue_reason(String due_reason) { this.due_reason = due_reason; }

        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }

    @GetMapping("/due-words")
    public ResponseEntity<DueWordsResponse> getDueWords(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "tags", required = false) String tagsParam,
            @RequestParam(value = "difficulty", required = false) String difficulty) {

        // 打印接收到的请求
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("limit", limit);
        requestParams.put("language", language);
        requestParams.put("tags", tagsParam);
        requestParams.put("difficulty", difficulty);
        printRequest(requestParams);

        try {
            // 1. 验证token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DueWordsResponse(false, "请先登录", null)
                );
            }

            String token = authHeader.substring(7);
            String userSql = "SELECT u.user_id FROM users u " +
                    "JOIN user_sessions us ON u.user_id = us.user_id " +
                    "WHERE us.access_token = ? AND us.expires_at > NOW()";

            List<Map<String, Object>> users = jdbcTemplate.queryForList(userSql, token);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DueWordsResponse(false, "登录已过期，请重新登录", null)
                );
            }

            int userId = (int) users.get(0).get("user_id");
            System.out.println("当前用户ID: " + userId);

            // 2. 构建查询条件
            StringBuilder sqlBuilder = new StringBuilder();
            List<Object> params = new ArrayList<>();

            sqlBuilder.append("SELECT uv.user_vocab_id, w.word, w.language, ");
            sqlBuilder.append("wd.definition, we.example, w.phonetic, w.part_of_speech, ");
            sqlBuilder.append("uv.mastery_level, uv.review_count, uv.last_reviewed_at, ");
            sqlBuilder.append("uv.next_review_date, w.difficulty, uv.source, ");
            sqlBuilder.append("CASE ");
            sqlBuilder.append("  WHEN uv.next_review_date <= NOW() THEN 'overdue' ");
            sqlBuilder.append("  WHEN uv.next_review_date <= DATE_ADD(NOW(), INTERVAL 1 DAY) THEN 'due_today' ");
            sqlBuilder.append("  ELSE 'scheduled' ");
            sqlBuilder.append("END as due_reason, ");
            sqlBuilder.append("CASE ");
            sqlBuilder.append("  WHEN uv.next_review_date <= NOW() THEN 1 ");
            sqlBuilder.append("  WHEN uv.next_review_date <= DATE_ADD(NOW(), INTERVAL 1 DAY) THEN 2 ");
            sqlBuilder.append("  ELSE 3 ");
            sqlBuilder.append("END as priority ");
            sqlBuilder.append("FROM user_vocabulary uv ");
            sqlBuilder.append("JOIN words w ON uv.word_id = w.word_id ");
            sqlBuilder.append("LEFT JOIN word_definitions wd ON w.word_id = wd.word_id ");
            sqlBuilder.append("LEFT JOIN word_examples we ON w.word_id = we.word_id ");
            sqlBuilder.append("WHERE uv.user_id = ? ");
            sqlBuilder.append("AND uv.is_mastered = 0 ");
            sqlBuilder.append("AND uv.next_review_date <= DATE_ADD(NOW(), INTERVAL 7 DAY) ");

            params.add(userId);

            // 添加语言筛选
            if (language != null && !language.trim().isEmpty()) {
                sqlBuilder.append("AND w.language = ? ");
                params.add(language);
            }

            // 添加难度筛选
            if (difficulty != null && !difficulty.trim().isEmpty()) {
                sqlBuilder.append("AND w.difficulty = ? ");
                params.add(difficulty);
            }

            // 按优先级和复习时间排序
            sqlBuilder.append("ORDER BY priority ASC, uv.next_review_date ASC ");

            // 添加数量限制
            sqlBuilder.append("LIMIT ?");
            params.add(Math.min(limit, 100)); // 限制最大100个

            // 3. 执行查询
            System.out.println("执行SQL: " + sqlBuilder.toString());
            System.out.println("参数: " + params);

            List<Map<String, Object>> dueWordsList = jdbcTemplate.queryForList(
                    sqlBuilder.toString(), params.toArray());

            printQueryResult("查询到 " + dueWordsList.size() + " 个待复习单词");

            // 4. 处理查询结果
            List<DueWord> dueWords = new ArrayList<>();

            for (Map<String, Object> row : dueWordsList) {
                DueWord dueWord = new DueWord();
                dueWord.setId((int) row.get("user_vocab_id"));
                dueWord.setWord((String) row.get("word"));
                dueWord.setLanguage((String) row.get("language"));
                dueWord.setDefinition((String) row.get("definition"));
                dueWord.setExample((String) row.get("example"));
                dueWord.setPhonetic((String) row.get("phonetic"));
                dueWord.setPart_of_speech((String) row.get("part_of_speech"));
                dueWord.setMastery_level((int) row.get("mastery_level"));
                dueWord.setReview_count((int) row.get("review_count"));

                // 处理日期字段
                if (row.get("last_reviewed_at") != null) {
                    dueWord.setLast_reviewed_at(row.get("last_reviewed_at").toString());
                }

                if (row.get("next_review_date") != null) {
                    dueWord.setNext_review_at(row.get("next_review_date").toString());
                }

                dueWord.setDifficulty((String) row.get("difficulty"));
                dueWord.setSource((String) row.get("source"));
                dueWord.setDue_reason((String) row.get("due_reason"));
                dueWord.setPriority((int) row.get("priority"));

                // 获取标签
                List<String> tags = getTagsForUserVocabulary((int) row.get("user_vocab_id"));
                dueWord.setTags(tags);

                dueWords.add(dueWord);
            }

            // 5. 获取统计数据
            String countSql = "SELECT COUNT(*) as total FROM user_vocabulary WHERE user_id = ? AND is_mastered = 0";
            int total = jdbcTemplate.queryForObject(countSql, Integer.class, userId);

            String dueCountSql = "SELECT COUNT(*) as due_count FROM user_vocabulary " +
                    "WHERE user_id = ? AND is_mastered = 0 AND next_review_date <= NOW()";
            int dueCount = jdbcTemplate.queryForObject(dueCountSql, Integer.class, userId);

            // 6. 构建响应数据
            DueWordsData data = new DueWordsData(dueWords, total, dueCount);
            DueWordsResponse response = new DueWordsResponse(true, "获取待复习单词成功", data);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取待复习单词过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new DueWordsResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // 获取用户生词的标签
    private List<String> getTagsForUserVocabulary(int userVocabId) {
        try {
            String sql = "SELECT vt.tag_name " +
                    "FROM user_vocabulary_tags uvt " +
                    "JOIN vocabulary_tags vt ON uvt.tag_id = vt.tag_id " +
                    "WHERE uvt.user_vocab_id = ?";

            List<Map<String, Object>> tagsList = jdbcTemplate.queryForList(sql, userVocabId);

            return tagsList.stream()
                    .map(row -> (String) row.get("tag_name"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("获取标签失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
