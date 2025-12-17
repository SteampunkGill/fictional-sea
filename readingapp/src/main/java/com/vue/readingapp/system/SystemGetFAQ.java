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
public class SystemGetFAQ {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取常见问题请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
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

    // 请求DTO
    public static class FAQRequest {
        private Integer page;
        private Integer pageSize;
        private String category;
        private String search;

        public Integer getPage() { return page != null ? page : 1; }
        public void setPage(Integer page) { this.page = page; }

        public Integer getPageSize() { return pageSize != null ? pageSize : 20; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSearch() { return search; }
        public void setSearch(String search) { this.search = search; }
    }

    // 响应DTO
    public static class FAQResponse {
        private boolean success;
        private String message;
        private FAQData data;

        public FAQResponse(boolean success, String message, FAQData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public FAQData getData() { return data; }
        public void setData(FAQData data) { this.data = data; }
    }

    public static class FAQData {
        private int total;
        private int page;
        private int pageSize;
        private int totalPages;
        private List<FAQItem> items;

        public FAQData(int total, int page, int pageSize, List<FAQItem> items) {
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
            this.items = items;
        }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public List<FAQItem> getItems() { return items; }
        public void setItems(List<FAQItem> items) { this.items = items; }
    }

    public static class FAQItem {
        private String id;
        private String question;
        private String answer;
        private String category;
        private List<String> tags;
        private int viewCount;
        private int helpfulCount;
        private String createdAt;
        private String updatedAt;

        public FAQItem(String id, String question, String answer, String category,
                       List<String> tags, int viewCount, int helpfulCount,
                       String createdAt, String updatedAt) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.category = category;
            this.tags = tags;
            this.viewCount = viewCount;
            this.helpfulCount = helpfulCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public int getViewCount() { return viewCount; }
        public void setViewCount(int viewCount) { this.viewCount = viewCount; }

        public int getHelpfulCount() { return helpfulCount; }
        public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    @GetMapping("/faq")
    public ResponseEntity<FAQResponse> getFAQ(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String search) {

        // 创建请求对象
        FAQRequest request = new FAQRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setCategory(category);
        request.setSearch(search);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 构建查询条件
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT faq_id, question, answer, category, order_index, is_active, created_at FROM faq WHERE is_active = TRUE");

            List<Object> params = new ArrayList<>();

            if (category != null && !category.trim().isEmpty()) {
                sqlBuilder.append(" AND category = ?");
                params.add(category);
            }

            if (search != null && !search.trim().isEmpty()) {
                sqlBuilder.append(" AND (question LIKE ? OR answer LIKE ?)");
                String searchPattern = "%" + search + "%";
                params.add(searchPattern);
                params.add(searchPattern);
            }

            sqlBuilder.append(" ORDER BY order_index, created_at DESC");

            // 2. 查询数据
            List<Map<String, Object>> faqList = jdbcTemplate.queryForList(sqlBuilder.toString(), params.toArray());
            printQueryResult("查询到 " + faqList.size() + " 条FAQ记录");

            // 3. 计算分页
            int currentPage = request.getPage();
            int currentPageSize = request.getPageSize();
            int totalItems = faqList.size();
            int startIndex = (currentPage - 1) * currentPageSize;
            int endIndex = Math.min(startIndex + currentPageSize, totalItems);

            // 4. 分页处理
            List<FAQItem> pagedItems = new ArrayList<>();
            for (int i = startIndex; i < endIndex && i < faqList.size(); i++) {
                Map<String, Object> faq = faqList.get(i);

                // 解析标签（这里简单处理，实际应该从关联表中查询）
                List<String> tags = new ArrayList<>();
                if (faq.get("category") != null) {
                    tags.add(faq.get("category").toString());
                }

                FAQItem item = new FAQItem(
                        "faq_" + faq.get("faq_id"),
                        (String) faq.get("question"),
                        (String) faq.get("answer"),
                        (String) faq.get("category"),
                        tags,
                        1250, // 模拟浏览次数
                        980,  // 模拟有帮助次数
                        ((LocalDateTime) faq.get("created_at")).format(DateTimeFormatter.ISO_DATE_TIME),
                        ((LocalDateTime) faq.get("created_at")).format(DateTimeFormatter.ISO_DATE_TIME)
                );

                pagedItems.add(item);
            }

            // 5. 构建响应数据
            FAQData faqData = new FAQData(totalItems, currentPage, currentPageSize, pagedItems);
            FAQResponse response = new FAQResponse(true, "获取常见问题成功", faqData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取常见问题过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回空数据
            FAQData errorData = new FAQData(0, 1, 20, new ArrayList<>());
            FAQResponse errorResponse = new FAQResponse(false, "获取常见问题失败: " + e.getMessage(), errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
