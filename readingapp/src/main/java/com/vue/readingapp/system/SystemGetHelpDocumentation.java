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
public class SystemGetHelpDocumentation {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取帮助文档请求 ===");
        System.out.println("请求数据: " + request);
        System.out.println("请求时间: " + LocalDateTime.now());
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
    public static class HelpRequest {
        private String topic;
        private String language;

        public String getTopic() { return topic != null ? topic : "getting_started"; }
        public void setTopic(String topic) { this.topic = topic; }

        public String getLanguage() { return language != null ? language : "zh-CN"; }
        public void setLanguage(String language) { this.language = language; }
    }

    // 响应DTO
    public static class HelpResponse {
        private boolean success;
        private String message;
        private HelpData data;

        public HelpResponse(boolean success, String message, HelpData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public HelpData getData() { return data; }
        public void setData(HelpData data) { this.data = data; }
    }

    public static class HelpData {
        private String topic;
        private String language;
        private List<HelpSection> sections;

        public HelpData(String topic, String language, List<HelpSection> sections) {
            this.topic = topic;
            this.language = language;
            this.sections = sections;
        }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public List<HelpSection> getSections() { return sections; }
        public void setSections(List<HelpSection> sections) { this.sections = sections; }
    }

    public static class HelpSection {
        private String title;
        private HelpContent content;

        public HelpSection(String title, HelpContent content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public HelpContent getContent() { return content; }
        public void setContent(HelpContent content) { this.content = content; }
    }

    public static class HelpContent {
        private String introduction;
        private List<HelpStep> steps;
        private List<String> tips;

        public HelpContent(String introduction, List<HelpStep> steps, List<String> tips) {
            this.introduction = introduction;
            this.steps = steps;
            this.tips = tips;
        }

        public String getIntroduction() { return introduction; }
        public void setIntroduction(String introduction) { this.introduction = introduction; }

        public List<HelpStep> getSteps() { return steps; }
        public void setSteps(List<HelpStep> steps) { this.steps = steps; }

        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
    }

    public static class HelpStep {
        private int step;
        private String title;
        private String description;

        public HelpStep(int step, String title, String description) {
            this.step = step;
            this.title = title;
            this.description = description;
        }

        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @GetMapping("/help")
    public ResponseEntity<HelpResponse> getHelpDocumentation(@RequestParam(required = false) String topic,
                                                             @RequestParam(required = false) String language) {

        // 创建请求对象
        HelpRequest request = new HelpRequest();
        request.setTopic(topic);
        request.setLanguage(language);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 根据主题和语言构建帮助文档
            List<HelpSection> sections = buildHelpSections(request.getTopic(), request.getLanguage());

            // 2. 构建响应数据
            HelpData helpData = new HelpData(
                    request.getTopic(),
                    request.getLanguage(),
                    sections
            );

            HelpResponse response = new HelpResponse(true, "获取帮助文档成功", helpData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取帮助文档过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new HelpResponse(false, "获取帮助文档失败: " + e.getMessage(), null)
            );
        }
    }

    // 构建帮助文档章节
    private List<HelpSection> buildHelpSections(String topic, String language) {
        List<HelpSection> sections = new ArrayList<>();

        if ("getting_started".equals(topic)) {
            if ("zh-CN".equals(language)) {
                // 快速开始 - 中文
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "注册账户", "点击注册按钮，填写用户名、邮箱和密码完成注册。"));
                steps.add(new HelpStep(2, "验证邮箱", "检查您的邮箱，点击验证链接激活账户。"));
                steps.add(new HelpStep(3, "登录系统", "使用注册的邮箱和密码登录系统。"));
                steps.add(new HelpStep(4, "上传文档", "在文档页面点击上传按钮，选择PDF、Word或TXT文件。"));
                steps.add(new HelpStep(5, "开始阅读", "打开文档，使用阅读器开始学习。"));

                List<String> tips = new ArrayList<>();
                tips.add("建议使用Chrome浏览器以获得最佳体验");
                tips.add("首次使用时请查看帮助文档");
                tips.add("可以同时上传多个文档");
                tips.add("支持PDF、Word、TXT和EPUB格式");

                HelpContent content = new HelpContent(
                        "欢迎使用Vue Reading App！本指南将帮助您快速上手使用本应用。",
                        steps,
                        tips
                );

                sections.add(new HelpSection("快速开始", content));

                // 基本功能 - 中文
                List<HelpStep> basicSteps = new ArrayList<>();
                basicSteps.add(new HelpStep(1, "文档管理", "在文档页面可以查看、编辑、删除您上传的文档。"));
                basicSteps.add(new HelpStep(2, "词汇学习", "阅读时双击单词可以查看释义并添加到生词本。"));
                basicSteps.add(new HelpStep(3, "笔记功能", "选中文本可以添加高亮和笔记。"));
                basicSteps.add(new HelpStep(4, "复习系统", "系统会根据记忆曲线自动安排复习计划。"));

                List<String> basicTips = new ArrayList<>();
                basicTips.add("定期复习可以提高记忆效果");
                basicTips.add("使用标签可以更好地组织文档");
                basicTips.add("导出功能可以备份学习数据");

                HelpContent basicContent = new HelpContent(
                        "了解应用的基本功能，让学习更加高效。",
                        basicSteps,
                        basicTips
                );

                sections.add(new HelpSection("基本功能", basicContent));

            } else {
                // 快速开始 - 英文
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "Register Account", "Click the register button, fill in username, email and password to complete registration."));
                steps.add(new HelpStep(2, "Verify Email", "Check your email and click the verification link to activate your account."));
                steps.add(new HelpStep(3, "Login System", "Use the registered email and password to log in to the system."));
                steps.add(new HelpStep(4, "Upload Document", "Click the upload button on the document page, select PDF, Word or TXT files."));
                steps.add(new HelpStep(5, "Start Reading", "Open the document and start learning using the reader."));

                List<String> tips = new ArrayList<>();
                tips.add("It is recommended to use Chrome browser for the best experience");
                tips.add("Please check the help documentation when using it for the first time");
                tips.add("Multiple documents can be uploaded at the same time");
                tips.add("Supports PDF, Word, TXT and EPUB formats");

                HelpContent content = new HelpContent(
                        "Welcome to Vue Reading App! This guide will help you get started quickly with this application.",
                        steps,
                        tips
                );

                sections.add(new HelpSection("Quick Start", content));

                // 基本功能 - 英文
                List<HelpStep> basicSteps = new ArrayList<>();
                basicSteps.add(new HelpStep(1, "Document Management", "You can view, edit, and delete uploaded documents on the document page."));
                basicSteps.add(new HelpStep(2, "Vocabulary Learning", "Double-click a word while reading to view its definition and add it to your vocabulary list."));
                basicSteps.add(new HelpStep(3, "Note Function", "Select text to add highlights and notes."));
                basicSteps.add(new HelpStep(4, "Review System", "The system will automatically arrange review plans based on the memory curve."));

                List<String> basicTips = new ArrayList<>();
                basicTips.add("Regular review can improve memory effect");
                basicTips.add("Using tags can better organize documents");
                basicTips.add("Export function can back up learning data");

                HelpContent basicContent = new HelpContent(
                        "Understand the basic functions of the application to make learning more efficient.",
                        basicSteps,
                        basicTips
                );

                sections.add(new HelpSection("Basic Functions", basicContent));
            }

        } else if ("reading".equals(topic)) {
            if ("zh-CN".equals(language)) {
                // 阅读功能 - 中文
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "打开文档", "在文档列表点击文档标题打开阅读器。"));
                steps.add(new HelpStep(2, "调整设置", "点击设置按钮调整字体、主题、行距等阅读参数。"));
                steps.add(new HelpStep(3, "使用书签", "点击书签图标保存当前阅读位置。"));
                steps.add(new HelpStep(4, "搜索内容", "使用搜索功能快速定位文档内容。"));

                List<String> tips = new ArrayList<>();
                tips.add("双击单词可以查看释义");
                tips.add("选中文本可以添加高亮");
                tips.add("右键菜单提供更多操作");
                tips.add("支持夜间阅读模式");

                HelpContent content = new HelpContent(
                        "掌握阅读器的各项功能，提升阅读体验和学习效率。",
                        steps,
                        tips
                );

                sections.add(new HelpSection("阅读功能", content));

            } else {
                // 阅读功能 - 英文
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "Open Document", "Click the document title in the document list to open the reader."));
                steps.add(new HelpStep(2, "Adjust Settings", "Click the settings button to adjust reading parameters such as font, theme, line spacing, etc."));
                steps.add(new HelpStep(3, "Use Bookmarks", "Click the bookmark icon to save the current reading position."));
                steps.add(new HelpStep(4, "Search Content", "Use the search function to quickly locate document content."));

                List<String> tips = new ArrayList<>();
                tips.add("Double-click a word to view its definition");
                tips.add("Select text to add highlights");
                tips.add("Right-click menu provides more operations");
                tips.add("Supports night reading mode");

                HelpContent content = new HelpContent(
                        "Master the various functions of the reader to improve reading experience and learning efficiency.",
                        steps,
                        tips
                );

                sections.add(new HelpSection("Reading Functions", content));
            }

        } else if ("vocabulary".equals(topic)) {
            if ("zh-CN".equals(language)) {
                // 词汇管理 - 中文
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "添加单词", "阅读时双击单词，点击'添加到生词本'按钮。"));
                steps.add(new HelpStep(2, "查看生词本", "在词汇页面查看所有已添加的单词。"));
                steps.add(new HelpStep(3, "复习单词", "系统会根据记忆曲线自动安排复习。"));
                steps.add(new HelpStep(4, "导出词汇", "可以将生词本导出为Excel或CSV格式。"));

                List<String> tips = new ArrayList<>();
                tips.add("定期复习可以提高记忆效果");
                tips.add("使用标签可以分类管理单词");
                tips.add("可以查看单词的详细释义和例句");
                tips.add("支持批量导入和导出");

                HelpContent content = new HelpContent(
                        "学习如何管理您的词汇，建立个性化的单词学习系统。",
                        steps,
                        tips
                );

                sections.add(new HelpSection("词汇管理", content));

            } else {
                // 词汇管理 - 英文
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "Add Words", "Double-click a word while reading, click the 'Add to Vocabulary' button."));
                steps.add(new HelpStep(2, "View Vocabulary", "View all added words on the vocabulary page."));
                steps.add(new HelpStep(3, "Review Words", "The system will automatically arrange reviews based on the memory curve."));
                steps.add(new HelpStep(4, "Export Vocabulary", "You can export your vocabulary to Excel or CSV format."));

                List<String> tips = new ArrayList<>();
                tips.add("Regular review can improve memory effect");
                tips.add("Use tags to categorize and manage words");
                tips.add("You can view detailed definitions and examples of words");
                tips.add("Supports batch import and export");

                HelpContent content = new HelpContent(
                        "Learn how to manage your vocabulary and build a personalized word learning system.",
                        steps,
                        tips
                );

                sections.add(new HelpSection("Vocabulary Management", content));
            }

        } else {
            // 默认主题 - 常见问题
            if ("zh-CN".equals(language)) {
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "如何找回密码", "在登录页面点击'忘记密码'，按照提示重置密码。"));
                steps.add(new HelpStep(2, "如何联系客服", "在设置页面找到'联系我们'，发送邮件或留言。"));
                steps.add(new HelpStep(3, "如何导出数据", "在导出页面选择要导出的数据类型和格式。"));
                steps.add(new HelpStep(4, "如何设置提醒", "在设置页面找到'通知设置'，开启复习提醒。"));

                List<String> tips = new ArrayList<>();
                tips.add("常见问题可以在FAQ页面找到答案");
                tips.add("可以通过反馈功能提交问题");
                tips.add("系统会定期更新帮助文档");

                HelpContent content = new HelpContent(
                        "这里是一些常见问题的解答，帮助您更好地使用本应用。",
                        steps,
                        tips
                );

                sections.add(new HelpSection("常见问题", content));

            } else {
                List<HelpStep> steps = new ArrayList<>();
                steps.add(new HelpStep(1, "How to Reset Password", "Click 'Forgot Password' on the login page and follow the prompts to reset your password."));
                steps.add(new HelpStep(2, "How to Contact Customer Service", "Find 'Contact Us' on the settings page, send an email or leave a message."));
                steps.add(new HelpStep(3, "How to Export Data", "Select the data type and format to export on the export page."));
                steps.add(new HelpStep(4, "How to Set Reminders", "Find 'Notification Settings' on the settings page and turn on review reminders."));

                List<String> tips = new ArrayList<>();
                tips.add("Common questions can be found in the FAQ page");
                tips.add("You can submit questions through the feedback function");
                tips.add("The system will regularly update help documentation");

                HelpContent content = new HelpContent(
                        "Here are answers to some frequently asked questions to help you use this application better.",
                        steps,
                        tips
                );

                sections.add(new HelpSection("Frequently Asked Questions", content));
            }
        }

        return sections;
    }
}
