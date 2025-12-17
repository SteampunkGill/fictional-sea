package com.vue.readingapp.documents;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentsUpload {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 文件存储路径
    private final String UPLOAD_DIR = "uploads/documents/";

    // 打印接收到的请求
    private void printRequest(String title, String description, String tags, String language, String fileName, long fileSize) {
        System.out.println("=== 收到上传文档请求 ===");
        System.out.println("标题: " + title);
        System.out.println("描述: " + description);
        System.out.println("标签: " + tags);
        System.out.println("语言: " + language);
        System.out.println("文件名: " + fileName);
        System.out.println("文件大小: " + fileSize + " bytes");
        System.out.println("=====================");
    }

    // 打印返回数据
    private void printResponse(UploadResponse response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // 请求DTO
    public static class UploadRequest {
        private String title;
        private String description;
        private String tags;
        private String language;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    // 响应DTO
    public static class UploadResponse {
        private boolean success;
        private String message;
        private UploadData data;

        public UploadResponse(boolean success, String message, UploadData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public UploadData getData() { return data; }
        public void setData(UploadData data) { this.data = data; }
    }

    public static class UploadData {
        private UploadDocumentDTO document;

        public UploadData(UploadDocumentDTO document) {
            this.document = document;
        }

        public UploadDocumentDTO getDocument() { return document; }
        public void setDocument(UploadDocumentDTO document) { this.document = document; }
    }

    public static class UploadDocumentDTO {
        private Integer id;
        private String title;
        private String fileName;
        private String fileSize;
        private String fileType;
        private String status;
        private Integer processingProgress;
        private LocalDateTime createdAt;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFileSize() { return fileSize; }
        public void setFileSize(String fileSize) { this.fileSize = fileSize; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getProcessingProgress() { return processingProgress; }
        public void setProcessingProgress(Integer processingProgress) { this.processingProgress = processingProgress; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "language", required = false) String language,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            // 打印接收到的请求
            printRequest(title, description, tags, language, file.getOriginalFilename(), file.getSize());

            // 1. 验证认证
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new UploadResponse(false, "请先登录", null)
                );
            }

            String token = authHeader.substring(7);

            // 验证token
            String tokenSql = "SELECT user_id FROM user_sessions WHERE access_token = ? AND expires_at > NOW()";
            List<Map<String, Object>> tokenResults = jdbcTemplate.queryForList(tokenSql, token);

            if (tokenResults.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new UploadResponse(false, "登录已过期，请重新登录", null)
                );
            }

            Integer userId = (Integer) tokenResults.get(0).get("user_id");

            // 2. 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new UploadResponse(false, "文件不能为空", null)
                );
            }

            // 验证文件类型
            String contentType = file.getContentType();
            List<String> allowedTypes = Arrays.asList(
                    "application/pdf",
                    "application/epub+zip",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain",
                    "text/html"
            );

            if (!allowedTypes.contains(contentType)) {
                return ResponseEntity.badRequest().body(
                        new UploadResponse(false, "不支持的文件类型: " + contentType, null)
                );
            }

            // 验证文件大小（最大100MB）
            long maxSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().body(
                        new UploadResponse(false, "文件大小不能超过100MB", null)
                );
            }

            // 3. 保存文件
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + fileExtension;

            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. 保存到数据库
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);

            String insertSql = "INSERT INTO documents (user_id, title, description, file_path, file_name, " +
                    "file_size, file_type, language, status, processing_progress, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(insertSql,
                    userId,
                    title,
                    description,
                    filePath.toString(),
                    originalFilename,
                    file.getSize(),
                    contentType,
                    language != null ? language : "en",
                    "uploading",
                    0,
                    timestamp,
                    timestamp
            );

            // 获取插入的文档ID
            String lastIdSql = "SELECT LAST_INSERT_ID() as id";
            Integer documentId = jdbcTemplate.queryForObject(lastIdSql, Integer.class);

            // 5. 处理标签
            if (tags != null && !tags.trim().isEmpty()) {
                // 解析标签（JSON格式或逗号分隔）
                List<String> tagList = new ArrayList<>();
                if (tags.startsWith("[") && tags.endsWith("]")) {
                    // JSON数组格式
                    tags = tags.substring(1, tags.length() - 1);
                    String[] tagArray = tags.split(",");
                    for (String tag : tagArray) {
                        tagList.add(tag.trim().replace("\"", ""));
                    }
                } else {
                    // 逗号分隔格式
                    String[] tagArray = tags.split(",");
                    for (String tag : tagArray) {
                        tagList.add(tag.trim());
                    }
                }

                // 为每个标签创建或获取标签ID，并建立关系
                for (String tagName : tagList) {
                    if (tagName.isEmpty()) continue;

                    // 检查标签是否已存在
                    String checkTagSql = "SELECT tag_id FROM document_tags WHERE user_id = ? AND tag_name = ?";
                    List<Map<String, Object>> existingTags = jdbcTemplate.queryForList(checkTagSql, userId, tagName);

                    Integer tagId;
                    if (existingTags.isEmpty()) {
                        // 创建新标签
                        String insertTagSql = "INSERT INTO document_tags (user_id, tag_name, created_at) VALUES (?, ?, ?)";
                        jdbcTemplate.update(insertTagSql, userId, tagName, timestamp);

                        String lastTagIdSql = "SELECT LAST_INSERT_ID() as id";
                        tagId = jdbcTemplate.queryForObject(lastTagIdSql, Integer.class);
                    } else {
                        tagId = (Integer) existingTags.get(0).get("tag_id");
                    }

                    // 建立文档-标签关系
                    String insertRelationSql = "INSERT INTO document_tag_relations (document_id, tag_id, created_at) VALUES (?, ?, ?)";
                    jdbcTemplate.update(insertRelationSql, documentId, tagId, timestamp);
                }
            }

            // 6. 添加到文档处理队列
            String insertQueueSql = "INSERT INTO document_processing_queue (document_id, status, priority, created_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertQueueSql, documentId, "pending", 1, timestamp);

            // 7. 构建响应数据
            UploadDocumentDTO dto = new UploadDocumentDTO();
            dto.setId(documentId);
            dto.setTitle(title);
            dto.setFileName(originalFilename);
            dto.setFileSize(formatFileSize(file.getSize()));
            dto.setFileType(contentType);
            dto.setStatus("uploading");
            dto.setProcessingProgress(0);
            dto.setCreatedAt(now);

            UploadData data = new UploadData(dto);
            UploadResponse response = new UploadResponse(true, "文档上传成功", data);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("文件保存过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UploadResponse(false, "文件保存失败: " + e.getMessage(), null)
            );
        } catch (Exception e) {
            System.err.println("上传文档过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UploadResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // 获取文件扩展名
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    // 格式化文件大小
    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }
}