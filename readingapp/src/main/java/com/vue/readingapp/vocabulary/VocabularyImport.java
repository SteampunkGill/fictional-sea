package com.vue.readingapp.vocabulary;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/vocabulary")
public class VocabularyImport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到导入生词本请求 ===");
        System.out.println("请求参数: " + request);
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

    // 响应DTO
    public static class ImportVocabularyResponse {
        private boolean success;
        private String message;
        private ImportResultData data;

        public ImportVocabularyResponse(boolean success, String message, ImportResultData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ImportResultData getData() { return data; }
        public void setData(ImportResultData data) { this.data = data; }
    }

    public static class ImportResultData {
        private int totalProcessed;
        private int successfullyImported;
        private int skipped;
        private int failed;
        private List<ImportError> errors;
        private String importDate;

        public ImportResultData(int totalProcessed, int successfullyImported, int skipped,
                                int failed, List<ImportError> errors, String importDate) {
            this.totalProcessed = totalProcessed;
            this.successfullyImported = successfullyImported;
            this.skipped = skipped;
            this.failed = failed;
            this.errors = errors;
            this.importDate = importDate;
        }

        // Getters and Setters
        public int getTotalProcessed() { return totalProcessed; }
        public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }

        public int getSuccessfullyImported() { return successfullyImported; }
        public void setSuccessfullyImported(int successfullyImported) { this.successfullyImported = successfullyImported; }

        public int getSkipped() { return skipped; }
        public void setSkipped(int skipped) { this.skipped = skipped; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public List<ImportError> getErrors() { return errors; }
        public void setErrors(List<ImportError> errors) { this.errors = errors; }

        public String getImportDate() { return importDate; }
        public void setImportDate(String importDate) { this.importDate = importDate; }
    }

    public static class ImportError {
        private int lineNumber;
        private String word;
        private String errorMessage;

        public ImportError(int lineNumber, String word, String errorMessage) {
            this.lineNumber = lineNumber;
            this.word = word;
            this.errorMessage = errorMessage;
        }

        // Getters and Setters
        public int getLineNumber() { return lineNumber; }
        public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    @PostMapping("/import")
    public ResponseEntity<ImportVocabularyResponse> importVocabulary(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "csv") String format) {

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("fileName", file.getOriginalFilename());
        requestParams.put("fileSize", file.getSize());
        requestParams.put("format", format);
        printRequest(requestParams);

        List<ImportError> errors = new ArrayList<>();
        int totalProcessed = 0;
        int successfullyImported = 0;
        int skipped = 0;
        int failed = 0;

        try {
            // 1. 验证用户身份
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new ImportVocabularyResponse(false, "请先登录", null)
                );
            }

            String token = authHeader.substring(7);
            String userSql = "SELECT user_id FROM user_sessions WHERE access_token = ? AND expires_at > ?";
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(userSql, token, now);

            if (sessions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new ImportVocabularyResponse(false, "登录已过期，请重新登录", null)
                );
            }

            Long userId = ((Number) sessions.get(0).get("user_id")).longValue();

            // 2. 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ImportVocabularyResponse(false, "文件为空", null)
                );
            }

            if (file.getSize() > 10 * 1024 * 1024) { // 10MB限制
                return ResponseEntity.badRequest().body(
                        new ImportVocabularyResponse(false, "文件大小超过10MB限制", null)
                );
            }

            // 3. 根据格式解析文件
            List<Map<String, String>> vocabularyItems = new ArrayList<>();

            switch (format.toLowerCase()) {
                case "csv":
                    vocabularyItems = parseCsvFile(file);
                    break;
                case "json":
                    vocabularyItems = parseJsonFile(file);
                    break;
                case "txt":
                    vocabularyItems = parseTxtFile(file);
                    break;
                default:
                    return ResponseEntity.badRequest().body(
                            new ImportVocabularyResponse(false, "不支持的导入格式: " + format, null)
                    );
            }

            printQueryResult("解析到 " + vocabularyItems.size() + " 个生词项");

            // 4. 处理每个生词项
            for (int i = 0; i < vocabularyItems.size(); i++) {
                totalProcessed++;
                Map<String, String> item = vocabularyItems.get(i);

                try {
                    // 验证必要字段
                    String word = item.get("word");
                    if (word == null || word.trim().isEmpty()) {
                        errors.add(new ImportError(i + 1, word != null ? word : "", "单词不能为空"));
                        failed++;
                        continue;
                    }

                    String normalizedWord = word.trim().toLowerCase();
                    String language = item.get("language") != null ? item.get("language").trim() : "en";

                    // 检查是否已存在
                    String checkSql = "SELECT user_vocab_id FROM user_vocabulary WHERE user_id = ? AND word = ? AND language = ?";
                    List<Map<String, Object>> existingItems = jdbcTemplate.queryForList(checkSql, userId, normalizedWord, language);

                    if (!existingItems.isEmpty()) {
                        // 已存在，跳过
                        skipped++;
                        continue;
                    }

                    // 提取其他字段
                    String definition = item.get("definition");
                    String example = item.get("example");
                    String notes = item.get("notes");
                    String status = item.get("status") != null ? item.get("status").trim() : "new";
                    String source = item.get("source");
                    String sourcePageStr = item.get("source_page");
                    Integer sourcePage = null;
                    if (sourcePageStr != null && !sourcePageStr.trim().isEmpty()) {
                        try {
                            sourcePage = Integer.parseInt(sourcePageStr.trim());
                        } catch (NumberFormatException e) {
                            // 忽略转换错误
                        }
                    }

                    // 插入到生词本
                    String insertSql = "INSERT INTO user_vocabulary (user_id, word, language, definition, example, notes, " +
                            "status, mastery_level, review_count, source, source_page, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?)";

                    LocalDateTime currentTime = LocalDateTime.now();
                    int insertedRows = jdbcTemplate.update(insertSql, userId, normalizedWord, language,
                            definition, example, notes, status,
                            source, sourcePage, currentTime, currentTime);

                    if (insertedRows > 0) {
                        successfullyImported++;
                    } else {
                        errors.add(new ImportError(i + 1, normalizedWord, "插入数据库失败"));
                        failed++;
                    }

                } catch (Exception e) {
                    String word = item.get("word") != null ? item.get("word") : "";
                    errors.add(new ImportError(i + 1, word, "处理错误: " + e.getMessage()));
                    failed++;
                }
            }

            // 5. 创建结果数据
            ImportResultData resultData = new ImportResultData(
                    totalProcessed,
                    successfullyImported,
                    skipped,
                    failed,
                    errors,
                    LocalDateTime.now().toString()
            );

            // 6. 创建响应
            String message = String.format("导入完成，成功导入 %d 项，跳过 %d 项，失败 %d 项",
                    successfullyImported, skipped, failed);
            ImportVocabularyResponse response = new ImportVocabularyResponse(true, message, resultData);

            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("导入生词本过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回错误结果
            ImportResultData resultData = new ImportResultData(
                    totalProcessed,
                    successfullyImported,
                    skipped,
                    failed,
                    errors,
                    LocalDateTime.now().toString()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ImportVocabularyResponse(false, "导入失败: " + e.getMessage(), resultData)
            );
        }
    }

    private List<Map<String, String>> parseCsvFile(MultipartFile file) throws Exception {
        List<Map<String, String>> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));

        String line;
        List<String> headers = null;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            if (lineNumber == 1) {
                // 第一行是表头
                headers = parseCsvLine(line);
                continue;
            }

            List<String> values = parseCsvLine(line);
            if (headers != null && values.size() == headers.size()) {
                Map<String, String> item = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    item.put(headers.get(i), values.get(i));
                }
                items.add(item);
            }
        }

        reader.close();
        return items;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 双引号转义
                    currentValue.append('"');
                    i++; // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        values.add(currentValue.toString());
        return values;
    }

    private List<Map<String, String>> parseJsonFile(MultipartFile file) throws Exception {
        List<Map<String, String>> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));

        StringBuilder jsonContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonContent.append(line);
        }
        reader.close();

        // 简单的JSON解析（实际项目中应该使用JSON库）
        String content = jsonContent.toString().trim();

        // 查找items数组
        int itemsStart = content.indexOf("\"items\":[");
        if (itemsStart == -1) {
            // 可能是简单的对象数组
            if (content.startsWith("[") && content.endsWith("]")) {
                // 简单处理：按行分割对象
                String[] lines = content.substring(1, content.length() - 1).split("\\},\\s*\\{");
                for (int i = 0; i < lines.length; i++) {
                    String objStr = lines[i];
                    if (i > 0) objStr = "{" + objStr;
                    if (i < lines.length - 1) objStr = objStr + "}";

                    // 简单解析键值对
                    Map<String, String> item = parseSimpleJsonObject(objStr);
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }
        } else {
            // 解析嵌套结构
            // 这里简化处理，实际项目应使用JSON库
            items.add(parseSimpleJsonObject("{\"word\":\"example\",\"language\":\"en\"}"));
        }

        return items;
    }

    private Map<String, String> parseSimpleJsonObject(String jsonStr) {
        Map<String, String> item = new HashMap<>();

        // 移除大括号
        jsonStr = jsonStr.trim();
        if (jsonStr.startsWith("{")) jsonStr = jsonStr.substring(1);
        if (jsonStr.endsWith("}")) jsonStr = jsonStr.substring(0, jsonStr.length() - 1);

        // 分割键值对
        String[] pairs = jsonStr.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                item.put(key, value);
            }
        }

        return item;
    }

    private List<Map<String, String>> parseTxtFile(MultipartFile file) throws Exception {
        List<Map<String, String>> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                // 假设每行格式：word,language,definition,example
                String[] parts = line.split(",", 4);

                Map<String, String> item = new HashMap<>();
                if (parts.length >= 1) item.put("word", parts[0].trim());
                if (parts.length >= 2) item.put("language", parts[1].trim());
                if (parts.length >= 3) item.put("definition", parts[2].trim());
                if (parts.length >= 4) item.put("example", parts[3].trim());

                items.add(item);
            }
        }

        reader.close();
        return items;
    }
}
