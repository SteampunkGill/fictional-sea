package com.vue.readingapp.documents;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Timestamp; // 显式导入 Timestamp
import java.sql.SQLException; // 导入 SQLException
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentsGetById {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(String documentId, String authHeader) {
        System.out.println("=== 收到获取文档详情请求 ===");
        System.out.println("文档ID: " + documentId);
        System.out.println("认证头: " + authHeader);
        System.out.println("========================");
    }

    // 打印查询结果
    private void printQueryResult(Map<String, Object> document) {
        System.out.println("=== 数据库查询结果 ===");
        System.out.println("文档详情: " + document);
        System.out.println("===================");
    }

    // 打印返回数据
    private void printResponse(DocumentDetailResponse response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // 响应 DTO
    public static class DocumentDetailResponse {
        private boolean success;
        private String message;
        private DocumentDetailData data;

        public DocumentDetailResponse(boolean success, String message, DocumentDetailData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public DocumentDetailData getData() { return data; }
        public void setData(DocumentDetailData data) { this.data = data; }
    }

    public static class DocumentDetailData {
        private DocumentDetailDTO document;

        public DocumentDetailData(DocumentDetailDTO document) {
            this.document = document;
        }

        // Getters and Setters
        public DocumentDetailDTO getDocument() { return document; }
        public void setDocument(DocumentDetailDTO document) { this.document = document; }
    }

    public static class DocumentDetailDTO {
        private Integer id;
        private String title;
        private String description;
        private String fileName;
        private String filePath;
        private String fileSize;
        private String fileType;
        private String language;
        private Integer pageCount;
        private Integer readProgress;
        private Integer currentPage;
        private List<String> tags;
        private Boolean isPublic;
        private Boolean isFavorite;
        private Boolean isProcessed;
        private String processingStatus;
        private String uploader;
        private LocalDateTime createdAt; // 保持 LocalDateTime 类型
        private LocalDateTime updatedAt; // 保持 LocalDateTime 类型
        private LocalDateTime lastReadAt; // 保持 LocalDateTime 类型
        private String thumbnail;
        private Map<String, Object> metadata;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getFileSize() { return fileSize; }
        public void setFileSize(String fileSize) { this.fileSize = fileSize; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public Integer getPageCount() { return pageCount; }
        public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

        public Integer getReadProgress() { return readProgress; }
        public void setReadProgress(Integer readProgress) { this.readProgress = readProgress; }

        public Integer getCurrentPage() { return currentPage; }
        public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

        public Boolean getIsFavorite() { return isFavorite; }
        public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }

        public Boolean getIsProcessed() { return isProcessed; }
        public void setIsProcessed(Boolean isProcessed) { this.isProcessed = isProcessed; }

        public String getProcessingStatus() { return processingStatus; }
        public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }

        public String getUploader() { return uploader; }
        public void setUploader(String uploader) { this.uploader = uploader; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public LocalDateTime getLastReadAt() { return lastReadAt; }
        public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }

        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentDetailResponse> getDocumentById(
            @PathVariable("documentId") Integer documentId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            // 打印接收到的请求
            printRequest(String.valueOf(documentId), authHeader);

            // 1. 验证认证
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DocumentDetailResponse(false, "请先登录", null)
                );
            }

            String token = authHeader.substring(7);

            // 验证token
            // 注意：NOW() 是 MySQL 的函数，如果数据库是 PostgreSQL，可能需要使用 CURRENT_TIMESTAMP
            String tokenSql = "SELECT user_id FROM user_sessions WHERE access_token = ? AND expires_at > NOW()";
            List<Map<String, Object>> tokenResults = jdbcTemplate.queryForList(tokenSql, token);

            if (tokenResults.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new DocumentDetailResponse(false, "登录已过期，请重新登录", null)
                );
            }

            Integer userId = (Integer) tokenResults.get(0).get("user_id");

            // 2. 查询文档详情
            // 移除了 `d.deleted_at IS NULL` 条件，并使用了 `d.status` 字段进行过滤。
            // 假设 `status` 字段可以表示文档是否被逻辑删除（例如，status = 'deleted'）。
            // 如果 `deleted_at` 仍然是软删除的标志，请恢复 `d.deleted_at IS NULL` 并移除 `d.status` 相关条件。
            String sql = "SELECT d.document_id, d.title, d.description, d.file_path, d.file_name, " +
                    "d.file_size, d.file_type, d.language, d.page_count, d.reading_progress, " +
                    "d.current_page, d.is_public, d.is_favorite, d.is_processed, d.processing_status, " +
                    "d.created_at, d.updated_at, d.last_read_at, " +
                    "u.username as uploader " +
                    "FROM documents d " +
                    "LEFT JOIN users u ON d.user_id = u.user_id " +
                    "WHERE d.document_id = ? AND d.user_id = ? AND (d.status IS NULL OR d.status != 'deleted')";

            List<Map<String, Object>> documents = jdbcTemplate.queryForList(sql, documentId, userId);

            if (documents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new DocumentDetailResponse(false, "文档不存在或已被删除", null)
                );
            }

            Map<String, Object> document = documents.get(0);
            printQueryResult(document);

            // 3. 查询文档标签
            String tagSql = "SELECT dt.tag_name FROM document_tag_relations dtr " +
                    "JOIN document_tags dt ON dtr.tag_id = dt.tag_id " +
                    "WHERE dtr.document_id = ? AND dt.user_id = ?";
            List<Map<String, Object>> tagResults = jdbcTemplate.queryForList(tagSql, documentId, userId);

            List<String> tags = tagResults.stream()
                    .map(tag -> (String) tag.get("tag_name"))
                    .filter(Objects::nonNull) // 过滤掉 null 的标签名
                    .collect(Collectors.toList());

            // 4. 构建响应数据
            DocumentDetailDTO dto = new DocumentDetailDTO();
            dto.setId((Integer) document.get("document_id"));
            dto.setTitle((String) document.get("title"));
            dto.setDescription((String) document.get("description"));
            dto.setFilePath((String) document.get("file_path"));
            dto.setFileName((String) document.get("file_name"));

            // 格式化文件大小，处理 null 值
            Long fileSize = (Long) document.get("file_size");
            dto.setFileSize(formatFileSize(fileSize));

            dto.setFileType((String) document.get("file_type"));
            dto.setLanguage((String) document.get("language"));
            dto.setPageCount((Integer) document.get("page_count"));
            dto.setReadProgress((Integer) document.get("reading_progress"));
            dto.setCurrentPage((Integer) document.get("current_page"));
            dto.setTags(tags);
            dto.setIsPublic((Boolean) document.get("is_public"));
            dto.setIsFavorite((Boolean) document.get("is_favorite"));
            dto.setIsProcessed((Boolean) document.get("is_processed"));
            dto.setProcessingStatus((String) document.get("processing_status"));
            dto.setUploader((String) document.get("uploader"));

            // 转换 Timestamp 为 LocalDateTime，并处理 null
            dto.setCreatedAt(formatDate((Timestamp) document.get("created_at")));
            dto.setUpdatedAt(formatDate((Timestamp) document.get("updated_at")));
            dto.setLastReadAt(formatDate((Timestamp) document.get("last_read_at")));

            dto.setThumbnail(null); // 暂时没有缩略图
            dto.setMetadata(new HashMap<>()); // 暂时没有元数据

            DocumentDetailData data = new DocumentDetailData(dto);
            DocumentDetailResponse response = new DocumentDetailResponse(true, "获取文档详情成功", data);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取文档详情过程中发生错误: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以帮助调试
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new DocumentDetailResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    /**
     * 格式化文件大小为人类可读的格式 (B, KB, MB, GB, TB)。
     * @param bytes 文件大小（字节）
     * @return 格式化后的字符串
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;

        // 循环直到 size 小于 1024 或达到最大单位
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        // 格式化为两位小数
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 将 java.sql.Timestamp 转换为 java.time.LocalDateTime。
     * @param timestamp 输入的 Timestamp 对象
     * @return 转换后的 LocalDateTime 对象，如果输入为 null 则返回 null
     */
    private LocalDateTime formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        // Timestamp.toLocalDateTime() 会自动处理时区转换（如果 Timestamp 包含时区信息）
        return timestamp.toLocalDateTime();
    }
}