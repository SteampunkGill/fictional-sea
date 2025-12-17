
package com.vue.readingapp.vocabulary;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/vocabulary")
public class VocabularyAddTo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到添加到生词本请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("=========================");
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
    public static class AddToVocabularyRequest {
        private String word;
        private String definition;
        private String example;
        private List<String> tags;
        private String language;
        private String source;
        private Integer sourcePage;
        private String notes;

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }

        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public Integer getSourcePage() { return sourcePage; }
        public void setSourcePage(Integer sourcePage) { this.sourcePage = sourcePage; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    // 响应DTO
    public static class AddToVocabularyResponse {
        private boolean success;
        private String message;
        private VocabularyItemData data;

        public AddToVocabularyResponse(boolean success, String message, VocabularyItemData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public VocabularyItemData getData() { return data; }
        public void setData(VocabularyItemData data) { this.data = data; }
    }

    public static class VocabularyItemData {
        private Long id;
        private String word;
        private String language;
        private String definition;
        private String example;
        private List<String> tags;
        private String notes;
        private String status;
        private Integer masteryLevel;
        private Integer reviewCount;
        private String lastReviewedAt;
        private String nextReviewAt;
        private String source;
        private Integer sourcePage;
        private String createdAt;
        private String updatedAt;

        public VocabularyItemData() {
            this.tags = new ArrayList<>();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }

        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getMasteryLevel() { return masteryLevel; }
        public void setMasteryLevel(Integer masteryLevel) { this.masteryLevel = masteryLevel; }

        public Integer getReviewCount() { return reviewCount; }
        public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

        public String getLastReviewedAt() { return lastReviewedAt; }
        public void setLastReviewedAt(String lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }

        public String getNextReviewAt() { return nextReviewAt; }
        public void setNextReviewAt(String nextReviewAt) { this.nextReviewAt = nextReviewAt; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public Integer getSourcePage() { return sourcePage; }
        public void setSourcePage(Integer sourcePage) { this.sourcePage = sourcePage; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    @PostMapping("")
    public ResponseEntity<AddToVocabularyResponse> addToVocabulary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddToVocabularyRequest request) {

        printRequest(request);

        try {
            // 1. 验证请求数据
            if (request.getWord() == null || request.getWord().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new AddToVocabularyResponse(false, "单词不能为空", null)
                );
            }

            // 2. 验证用户身份
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new AddToVocabularyResponse(false, "请先登录", null)
                );
            }

            String token = authHeader.substring(7);
            String userSql = "SELECT user_id FROM user_sessions WHERE access_token = ? AND expires_at > ?";
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(userSql, token, now);

            if (sessions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new AddToVocabularyResponse(false, "登录已过期，请重新登录", null)
                );
            }

            Long userId = ((Number) sessions.get(0).get("user_id")).longValue();

            // 3. 检查单词是否已存在
            String normalizedWord = request.getWord().trim().toLowerCase();
            String language = request.getLanguage() != null ? request.getLanguage() : "en";

            String checkSql = "SELECT user_vocab_id FROM user_vocabulary WHERE user_id = ? AND word = ? AND language = ?";
            List<Map<String, Object>> existingItems = jdbcTemplate.queryForList(checkSql, userId, normalizedWord, language);

            if (!existingItems.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        new AddToVocabularyResponse(false, "单词已在生词本中", null)
                );
            }

            // 4. 插入到生词本
            String insertSql = "INSERT INTO user_vocabulary (user_id, word, language, definition, example, notes, " +
                    "status, mastery_level, review_count, source, source_page, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'new', 0, 0, ?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime currentTime = LocalDateTime.now();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, userId);
                ps.setString(2, normalizedWord);
                ps.setString(3, language);
                ps.setString(4, request.getDefinition());
                ps.setString(5, request.getExample());
                ps.setString(6, request.getNotes());
                ps.setString(7, request.getSource());
                ps.setObject(8, request.getSourcePage());
                ps.setTimestamp(9, java.sql.Timestamp.valueOf(currentTime));
                ps.setTimestamp(10, java.sql.Timestamp.valueOf(currentTime));
                return ps;
            }, keyHolder);

            Long userVocabId = keyHolder.getKey().longValue();

            // 5. 处理标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                for (String tagName : request.getTags()) {
                    if (tagName != null && !tagName.trim().isEmpty()) {
                        // 查找或创建标签
                        String tagSql = "SELECT tag_id FROM vocabulary_tags WHERE tag_name = ?";
                        List<Map<String, Object>> tags = jdbcTemplate.queryForList(tagSql, tagName.trim());

                        Long tagId;
                        if (tags.isEmpty()) {
                            // 创建新标签
                            String insertTagSql = "INSERT INTO vocabulary_tags (tag_name, created_at) VALUES (?, ?)";
                            KeyHolder tagKeyHolder = new GeneratedKeyHolder();

                            jdbcTemplate.update(connection -> {
                                PreparedStatement ps = connection.prepareStatement(insertTagSql, Statement.RETURN_GENERATED_KEYS);
                                ps.setString(1, tagName.trim());
                                ps.setTimestamp(2, java.sql.Timestamp.valueOf(currentTime));
                                return ps;
                            }, tagKeyHolder);

                            tagId = tagKeyHolder.getKey().longValue();
                        } else {
                            tagId = ((Number) tags.get(0).get("tag_id")).longValue();
                        }

                        // 关联标签
                        String relationSql = "INSERT INTO user_vocabulary_tags (user_vocab_id, tag_id, created_at) VALUES (?, ?, ?)";
                        jdbcTemplate.update(relationSql, userVocabId, tagId, currentTime);
                    }
                }
            }

            // 6. 查询刚添加的生词详情
            String detailSql = "SELECT uv.user_vocab_id, uv.word, uv.language, uv.definition, uv.example, uv.notes, " +
                    "uv.status, uv.mastery_level, uv.review_count, uv.last_reviewed_at, uv.next_review_at, " +
                    "uv.source, uv.source_page, uv.created_at, uv.updated_at, " +
                    "GROUP_CONCAT(DISTINCT vt.tag_name) as tags " +
                    "FROM user_vocabulary uv " +
                    "LEFT JOIN user_vocabulary_tags uvt ON uv.user_vocab_id = uvt.user_vocab_id " +
                    "LEFT JOIN vocabulary_tags vt ON uvt.tag_id = vt.tag_id " +
                    "WHERE uv.user_vocab_id = ? " +
                    "GROUP BY uv.user_vocab_id";

            List<Map<String, Object>> details = jdbcTemplate.queryForList(detailSql, userVocabId);
            printQueryResult(details);

            if (details.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new AddToVocabularyResponse(false, "添加成功但查询详情失败", null)
                );
            }

            Map<String, Object> detail = details.get(0);

            // 7. 组装响应数据
            VocabularyItemData itemData = new VocabularyItemData();
            itemData.setId(userVocabId);
            itemData.setWord((String) detail.get("word"));
            itemData.setLanguage((String) detail.get("language"));
            itemData.setDefinition((String) detail.get("definition"));
            itemData.setExample((String) detail.get("example"));
            itemData.setNotes((String) detail.get("notes"));
            itemData.setStatus((String) detail.get("status"));
            itemData.setMasteryLevel(detail.get("mastery_level") != null ? ((Number) detail.get("mastery_level")).intValue() : 0);
            itemData.setReviewCount(detail.get("review_count") != null ? ((Number) detail.get("review_count")).intValue() : 0);
            itemData.setLastReviewedAt(detail.get("last_reviewed_at") != null ? detail.get("last_reviewed_at").toString() : null);
            itemData.setNextReviewAt(detail.get("next_review_at") != null ? detail.get("next_review_at").toString() : null);
            itemData.setSource((String) detail.get("source"));
            itemData.setSourcePage(detail.get("source_page") != null ? ((Number) detail.get("source_page")).intValue() : null);
            itemData.setCreatedAt(detail.get("created_at").toString());
            itemData.setUpdatedAt(detail.get("updated_at").toString());

            // 处理标签字符串
            String tagsStr = (String) detail.get("tags");
            if (tagsStr != null && !tagsStr.isEmpty()) {
                itemData.setTags(Arrays.asList(tagsStr.split(",")));
            }

            // 8. 创建响应
            AddToVocabularyResponse response = new AddToVocabularyResponse(true, "单词已成功添加到生词本", itemData);

            printResponse(response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("添加到生词本过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new AddToVocabularyResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }
}