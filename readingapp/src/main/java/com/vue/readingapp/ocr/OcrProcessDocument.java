package com.vue.readingapp.ocr;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List; // 尽管导入了，但在这个方法中未直接使用
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/documents")
public class OcrProcessDocument {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    // DateTimeFormatter 在这里未被直接使用，但保留以防后续需求
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // --- 打印方法 (用于调试) ---
    private void printRequest(Object request) {
        System.out.println("=== 收到文档OCR处理请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("=======================");
    }

    private void printQueryResult(Object result) {
        System.out.println("=== 数据库查询结果 ===");
        System.out.println("查询结果: " + result);
        System.out.println("===================");
    }

    private void printResponse(Object response) {
        System.out.println("=== 准备返回的响应 ===");
        System.out.println("响应数据: " + response);
        System.out.println("===================");
    }

    // --- DTO 类 ---
    public static class ProcessDocumentRequest {
        private String documentId; // 虽然在 URL 中，但保留可能用于其他场景
        private Integer page;      // 同上
        private String options;    // OCR 处理选项

        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }

        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }

        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }

        @Override
        public String toString() {
            return "ProcessDocumentRequest{" +
                    "documentId='" + documentId + '\'' +
                    ", page=" + page +
                    ", options='" + options + '\'' +
                    '}';
        }
    }

    public static class ProcessDocumentResponse {
        private boolean success;
        private String message;
        private TaskData data;

        public ProcessDocumentResponse(boolean success, String message, TaskData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public TaskData getData() { return data; }
        public void setData(TaskData data) { this.data = data; }
    }

    public static class TaskData {
        private String taskId;
        private String status;
        private Integer progress;
        private Integer estimatedTime; // 可能是处理预估时间

        public TaskData(String taskId, String status, Integer progress, Integer estimatedTime) {
            this.taskId = taskId;
            this.status = status;
            this.progress = progress;
            this.estimatedTime = estimatedTime;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }

        public Integer getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }
    }

    @PostMapping("/{documentId}/pages/{page}/ocr")
    public ResponseEntity<ProcessDocumentResponse> processDocumentPage(
            @PathVariable String documentId,
            @PathVariable Integer page,
            @RequestBody ProcessDocumentRequest request) {

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 验证请求数据
            if (documentId == null || documentId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ProcessDocumentResponse(false, "文档ID不能为空", null)
                );
            }

            if (page == null || page <= 0) {
                return ResponseEntity.badRequest().body(
                        new ProcessDocumentResponse(false, "页码必须大于0", null)
                );
            }

            // 2. 检查文档是否存在 (假设 documents 表存在 document_id 列)
            String checkDocumentSql = "SELECT COUNT(*) FROM documents WHERE document_id = ?";
            Integer documentCount = jdbcTemplate.queryForObject(checkDocumentSql, Integer.class, documentId);

            if (documentCount == null || documentCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ProcessDocumentResponse(false, "文档不存在", null)
                );
            }

            // 3. 生成任务ID
            String taskId = "ocr_task_" + UUID.randomUUID().toString().substring(0, 8);
            LocalDateTime now = LocalDateTime.now();

            // 4. 检查并创建 ocr_tasks 表 (如果不存在)
            String checkTaskTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'ocr_tasks'";
            Integer taskTableCount = jdbcTemplate.queryForObject(checkTaskTableSql, Integer.class);

            if (taskTableCount == null || taskTableCount == 0) {
                String createTaskTableSql = "CREATE TABLE ocr_tasks (" +
                        "task_id VARCHAR(50) PRIMARY KEY," +
                        "document_id VARCHAR(50) NOT NULL," +
                        "page_number INT," +
                        "status VARCHAR(20) NOT NULL," + // e.g., 'processing', 'completed', 'failed'
                        "progress INT DEFAULT 0," +       // 0-100
                        "options_json TEXT," +            // JSON string of options
                        "result_json TEXT," +             // JSON string of OCR results
                        "error_message TEXT," +           // Error message if status is 'failed'
                        "estimated_time INT," +           // Estimated processing time in seconds (example)
                        "started_at TIMESTAMP," +
                        "completed_at TIMESTAMP," +
                        "created_at TIMESTAMP," +
                        "updated_at TIMESTAMP," +
                        "FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE" +
                        ")";
                jdbcTemplate.execute(createTaskTableSql);
                System.out.println("INFO: 已创建 ocr_tasks 表");
            }

            // 5. 插入 OCR 任务记录到 ocr_tasks 表
            String insertTaskSql = "INSERT INTO ocr_tasks (task_id, document_id, page_number, status, progress, options_json, estimated_time, started_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(insertTaskSql,
                    taskId,
                    documentId,
                    page,
                    "processing", // Initial status
                    0,            // Initial progress
                    request.getOptions() != null ? request.getOptions() : "{}", // Store options
                    30,           // Example estimated time
                    now,          // Started at
                    now,          // Created at
                    now           // Updated at
            );

            // 6. 使用 final 变量传递给 lambda 表达式，用于异步线程
            final String finalDocumentId = documentId;
            final Integer finalPage = page;
            final String finalTaskId = taskId;

            // 7. 启动异步处理任务 (模拟 OCR 过程)
            new Thread(() -> {
                try {
                    // --- 模拟 OCR 处理过程 ---
                    // 假设处理时间较长，分步更新进度
                    int totalSteps = 10;
                    int sleepIntervalMillis = 1000; // 每一步间隔 1 秒

                    for (int i = 1; i <= totalSteps; i++) {
                        Thread.sleep(sleepIntervalMillis);
                        int currentProgress = (i * 100) / totalSteps;

                        // 更新任务进度
                        String updateProgressSql = "UPDATE ocr_tasks SET progress = ?, updated_at = ? WHERE task_id = ?";
                        jdbcTemplate.update(updateProgressSql, currentProgress, LocalDateTime.now(), finalTaskId);

                        System.out.println("OCR 任务进度更新: " + finalTaskId + " - " + currentProgress + "%");

                        // 模拟 OCR 结果生成 (当进度达到 100%)
                        if (currentProgress == 100) {
                            // 生成模拟的 OCR 结果
                            Map<String, Object> ocrResultMap = new HashMap<>();
                            ocrResultMap.put("text", "这是第 " + finalPage + " 页的OCR识别文本内容。\n模拟OCR处理完成。");
                            ocrResultMap.put("confidence", 92.5);

                            Map<String, Object> metadata = new HashMap<>();
                            metadata.put("engine", "simulated_ocr_engine");
                            metadata.put("language", "auto");
                            metadata.put("processingTime", 2500); // milliseconds
                            metadata.put("imageSize", Map.of("width", 800, "height", 1000));
                            ocrResultMap.put("metadata", metadata);

                            // 将 OCR 结果转换为 JSON 字符串
                            String resultJson = objectMapper.writeValueAsString(ocrResultMap);

                            // 更新任务为完成状态，并保存结果
                            String completeTaskSql = "UPDATE ocr_tasks SET status = 'completed', progress = 100, result_json = ?, completed_at = ?, updated_at = ? WHERE task_id = ?";
                            jdbcTemplate.update(completeTaskSql,
                                    resultJson,
                                    LocalDateTime.now(), // Completed at
                                    LocalDateTime.now(), // Updated at
                                    finalTaskId
                            );

                            System.out.println("OCR 任务完成: " + finalTaskId);

                            // !!! 注意: 根据您提供的第二个代码片段, OCR 结果直接保存在 ocr_tasks 表的 result_json 字段.
                            // !!! 如果您还需要将结果保存到 document_ocr_results 表，请取消下面的注释并确保该表存在。
                            /*
                            saveOcrResultToDatabase(finalDocumentId, finalPage, resultJson, ocrResultMap);
                            */
                        }
                    }
                } catch (InterruptedException e) {
                    // 线程被中断
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    System.err.println("OCR 处理线程被中断: " + finalTaskId + " - " + e.getMessage());
                    // 更新任务为失败状态
                    String failTaskSql = "UPDATE ocr_tasks SET status = 'failed', error_message = ?, updated_at = ? WHERE task_id = ?";
                    jdbcTemplate.update(failTaskSql, "Thread interrupted: " + e.getMessage(), LocalDateTime.now(), finalTaskId);
                } catch (Exception e) {
                    // 其他处理错误
                    System.err.println("OCR 处理线程错误: " + finalTaskId + " - " + e.getMessage());
                    e.printStackTrace(); // 打印详细堆栈信息
                    // 更新任务为失败状态
                    String failTaskSql = "UPDATE ocr_tasks SET status = 'failed', error_message = ?, updated_at = ? WHERE task_id = ?";
                    jdbcTemplate.update(failTaskSql, e.getMessage(), LocalDateTime.now(), finalTaskId);
                }
            }).start(); // 启动新线程

            // 8. 准备响应数据
            TaskData responseData = new TaskData(
                    taskId,
                    "processing", // 任务刚启动，状态为 processing
                    0,            // 初始进度
                    30            // 预估时间
            );

            ProcessDocumentResponse response = new ProcessDocumentResponse(true, "OCR 处理任务已启动，请稍后查询状态", responseData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("文档OCR处理请求处理时发生内部错误: " + e.getMessage());
            e.printStackTrace(); // 打印详细堆栈信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ProcessDocumentResponse(false, "服务器内部错误: " + e.getMessage(), null)
            );
        }
    }

    // --- 如果需要将 OCR 结果保存到 document_ocr_results 表，请取消此方法的注释 ---

    private void saveOcrResultToDatabase(String documentId, Integer page, String resultJson, Map<String, Object> ocrResultMap) {
        try {
            // 假设 document_ocr_results 表结构如下，请根据实际情况调整
            // CREATE TABLE document_ocr_results (
            //     ocr_id VARCHAR(50) PRIMARY KEY,
            //     document_id VARCHAR(50) NOT NULL,
            //     page_number INT,
            //     ocr_text TEXT,
            //     confidence DOUBLE,
            //     words_json TEXT, // 如果 OCR 结果包含 words
            //     lines_json TEXT, // 如果 OCR 结果包含 lines
            //     metadata_json TEXT,
            //     created_at TIMESTAMP,
            //     updated_at TIMESTAMP,
            //     FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE
            // );

            String ocrResultId = "ocr_" + UUID.randomUUID().toString().substring(0, 8);
            LocalDateTime now = LocalDateTime.now();

            String insertSql = "INSERT INTO document_ocr_results (ocr_id, document_id, page_number, ocr_text, confidence, words_json, lines_json, metadata_json, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // 从 ocrResultMap 中提取数据，需要根据 OcrResult 类的实际结构来填充
            String ocrText = (String) ocrResultMap.get("text");
            Double confidence = (Double) ocrResultMap.get("confidence");

            // 假设 ocrResultMap 包含 words, lines, metadata 的 Map 或 List
            // 您需要根据 OcrResult 类的实际结构来解析和转换
            String wordsJson = objectMapper.writeValueAsString(ocrResultMap.getOrDefault("words", List.of())); // 示例
            String linesJson = objectMapper.writeValueAsString(ocrResultMap.getOrDefault("lines", List.of())); // 示例
            String metadataJson = objectMapper.writeValueAsString(ocrResultMap.getOrDefault("metadata", new HashMap<>())); // 示例

            jdbcTemplate.update(insertSql,
                ocrResultId,
                documentId,
                page,
                ocrText,
                confidence,
                wordsJson,
                linesJson,
                metadataJson,
                now,
                now
            );
            System.out.println("INFO: OCR 结果已保存到 document_ocr_results 表，ocr_id: " + ocrResultId);

        } catch (Exception e) {
            System.err.println("保存 OCR 结果到 document_ocr_results 数据库失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}