package com.vue.readingapp.system;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/system")
public class SystemGetAPIDocumentation {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取API文档请求 ===");
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
    public static class APIDocsRequest {
        private String version;
        private String format;

        public String getVersion() { return version != null ? version : "v1"; }
        public void setVersion(String version) { this.version = version; }

        public String getFormat() { return format != null ? format : "json"; }
        public void setFormat(String format) { this.format = format; }
    }

    // 响应DTO
    public static class APIDocsResponse {
        private boolean success;
        private String message;
        private APIDocsData data;

        public APIDocsResponse(boolean success, String message, APIDocsData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public APIDocsData getData() { return data; }
        public void setData(APIDocsData data) { this.data = data; }
    }

    public static class APIDocsData {
        private String version;
        private String baseUrl;
        private List<APIEndpoint> endpoints;
        private Map<String, Object> definitions;

        public APIDocsData(String version, String baseUrl, List<APIEndpoint> endpoints, Map<String, Object> definitions) {
            this.version = version;
            this.baseUrl = baseUrl;
            this.endpoints = endpoints;
            this.definitions = definitions;
        }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public List<APIEndpoint> getEndpoints() { return endpoints; }
        public void setEndpoints(List<APIEndpoint> endpoints) { this.endpoints = endpoints; }

        public Map<String, Object> getDefinitions() { return definitions; }
        public void setDefinitions(Map<String, Object> definitions) { this.definitions = definitions; }
    }

    public static class APIEndpoint {
        private String path;
        private String method;
        private String description;
        private List<APIParameter> parameters;
        private Map<String, APIResponse> responses;

        public APIEndpoint(String path, String method, String description,
                           List<APIParameter> parameters, Map<String, APIResponse> responses) {
            this.path = path;
            this.method = method;
            this.description = description;
            this.parameters = parameters;
            this.responses = responses;
        }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<APIParameter> getParameters() { return parameters; }
        public void setParameters(List<APIParameter> parameters) { this.parameters = parameters; }

        public Map<String, APIResponse> getResponses() { return responses; }
        public void setResponses(Map<String, APIResponse> responses) { this.responses = responses; }
    }

    public static class APIParameter {
        private String name;
        private String type;
        private boolean required;
        private String description;

        public APIParameter(String name, String type, boolean required, String description) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class APIResponse {
        private String description;
        private Map<String, Object> schema;

        public APIResponse(String description, Map<String, Object> schema) {
            this.description = description;
            this.schema = schema;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Map<String, Object> getSchema() { return schema; }
        public void setSchema(Map<String, Object> schema) { this.schema = schema; }
    }

    @GetMapping("/api-docs")
    public ResponseEntity<APIDocsResponse> getAPIDocumentation(@RequestParam(required = false) String version,
                                                               @RequestParam(required = false) String format) {

        // 创建请求对象
        APIDocsRequest request = new APIDocsRequest();
        request.setVersion(version);
        request.setFormat(format);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 构建API文档数据
            String baseUrl = "http://localhost:8080/api";

            // 2. 创建端点列表
            List<APIEndpoint> endpoints = new ArrayList<>();

            // 认证端点
            endpoints.add(createAuthLoginEndpoint());
            endpoints.add(createAuthRegisterEndpoint());
            endpoints.add(createAuthLogoutEndpoint());

            // 文档端点
            endpoints.add(createDocumentsGetEndpoint());
            endpoints.add(createDocumentsUploadEndpoint());

            // 系统端点
            endpoints.add(createSystemVersionEndpoint());
            endpoints.add(createSystemStatusEndpoint());
            endpoints.add(createSystemFAQEndpoint());

            // 3. 创建定义
            Map<String, Object> definitions = new HashMap<>();
            definitions.put("Document", createDocumentDefinition());
            definitions.put("User", createUserDefinition());
            definitions.put("VersionInfo", createVersionInfoDefinition());

            // 4. 构建响应数据
            APIDocsData docsData = new APIDocsData(
                    request.getVersion(),
                    baseUrl,
                    endpoints,
                    definitions
            );

            APIDocsResponse response = new APIDocsResponse(true, "获取API文档成功", docsData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取API文档过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 返回空文档
            APIDocsData errorData = new APIDocsData("v1", "error", new ArrayList<>(), new HashMap<>());
            APIDocsResponse errorResponse = new APIDocsResponse(false, "获取API文档失败: " + e.getMessage(), errorData);
            printResponse(errorResponse);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 创建认证登录端点
    private APIEndpoint createAuthLoginEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();
        parameters.add(new APIParameter("email", "string", true, "用户邮箱"));
        parameters.add(new APIParameter("password", "string", true, "用户密码"));

        Map<String, APIResponse> responses = new HashMap<>();

        Map<String, Object> successSchema = new HashMap<>();
        successSchema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", createSchemaProperty("boolean", "是否成功"));
        properties.put("message", createSchemaProperty("string", "消息"));

        Map<String, Object> dataProperties = new HashMap<>();
        dataProperties.put("access_token", createSchemaProperty("string", "访问令牌"));
        dataProperties.put("refresh_token", createSchemaProperty("string", "刷新令牌"));
        dataProperties.put("expires_in", createSchemaProperty("integer", "过期时间（秒）"));

        Map<String, Object> dataSchema = new HashMap<>();
        dataSchema.put("type", "object");
        dataSchema.put("properties", dataProperties);

        properties.put("data", dataSchema);
        successSchema.put("properties", properties);

        responses.put("200", new APIResponse("登录成功", successSchema));
        responses.put("400", new APIResponse("请求参数错误", null));
        responses.put("401", new APIResponse("邮箱或密码错误", null));

        return new APIEndpoint(
                "/auth/login",
                "POST",
                "用户登录",
                parameters,
                responses
        );
    }

    // 创建认证注册端点
    private APIEndpoint createAuthRegisterEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();
        parameters.add(new APIParameter("username", "string", true, "用户名"));
        parameters.add(new APIParameter("email", "string", true, "用户邮箱"));
        parameters.add(new APIParameter("password", "string", true, "用户密码"));
        parameters.add(new APIParameter("nickname", "string", false, "用户昵称"));

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("注册成功", null));
        responses.put("400", new APIResponse("请求参数错误", null));
        responses.put("409", new APIResponse("邮箱已存在", null));

        return new APIEndpoint(
                "/auth/register",
                "POST",
                "用户注册",
                parameters,
                responses
        );
    }

    // 创建认证登出端点
    private APIEndpoint createAuthLogoutEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();
        parameters.add(new APIParameter("Authorization", "string", true, "Bearer令牌"));

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("登出成功", null));
        responses.put("401", new APIResponse("未授权", null));

        return new APIEndpoint(
                "/auth/logout",
                "POST",
                "用户登出",
                parameters,
                responses
        );
    }

    // 创建文档获取端点
    private APIEndpoint createDocumentsGetEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();
        parameters.add(new APIParameter("page", "integer", false, "页码"));
        parameters.add(new APIParameter("pageSize", "integer", false, "每页数量"));
        parameters.add(new APIParameter("status", "string", false, "文档状态"));
        parameters.add(new APIParameter("search", "string", false, "搜索关键词"));

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("获取文档列表成功", null));
        responses.put("401", new APIResponse("未授权", null));

        return new APIEndpoint(
                "/documents",
                "GET",
                "获取文档列表",
                parameters,
                responses
        );
    }

    // 创建文档上传端点
    private APIEndpoint createDocumentsUploadEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();
        parameters.add(new APIParameter("file", "file", true, "文档文件"));
        parameters.add(new APIParameter("title", "string", false, "文档标题"));
        parameters.add(new APIParameter("description", "string", false, "文档描述"));

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("文档上传成功", null));
        responses.put("400", new APIResponse("文件格式不支持", null));
        responses.put("401", new APIResponse("未授权", null));

        return new APIEndpoint(
                "/documents/upload",
                "POST",
                "上传文档",
                parameters,
                responses
        );
    }

    // 创建系统版本端点
    private APIEndpoint createSystemVersionEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("获取版本信息成功", null));

        return new APIEndpoint(
                "/system/version",
                "GET",
                "获取系统版本信息",
                parameters,
                responses
        );
    }

    // 创建系统状态端点
    private APIEndpoint createSystemStatusEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("获取系统状态成功", null));

        return new APIEndpoint(
                "/system/status",
                "GET",
                "获取系统状态",
                parameters,
                responses
        );
    }

    // 创建系统FAQ端点
    private APIEndpoint createSystemFAQEndpoint() {
        List<APIParameter> parameters = new ArrayList<>();
        parameters.add(new APIParameter("page", "integer", false, "页码"));
        parameters.add(new APIParameter("pageSize", "integer", false, "每页数量"));
        parameters.add(new APIParameter("category", "string", false, "分类"));
        parameters.add(new APIParameter("search", "string", false, "搜索关键词"));

        Map<String, APIResponse> responses = new HashMap<>();
        responses.put("200", new APIResponse("获取常见问题成功", null));

        return new APIEndpoint(
                "/system/faq",
                "GET",
                "获取常见问题",
                parameters,
                responses
        );
    }

    // 创建文档定义
    private Map<String, Object> createDocumentDefinition() {
        Map<String, Object> definition = new HashMap<>();
        definition.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("id", createSchemaProperty("string", "文档ID"));
        properties.put("title", createSchemaProperty("string", "文档标题"));
        properties.put("content", createSchemaProperty("string", "文档内容"));
        properties.put("status", createSchemaProperty("string", "文档状态"));
        properties.put("createdAt", createSchemaProperty("string", "创建时间"));
        properties.put("updatedAt", createSchemaProperty("string", "更新时间"));

        definition.put("properties", properties);
        return definition;
    }

    // 创建用户定义
    private Map<String, Object> createUserDefinition() {
        Map<String, Object> definition = new HashMap<>();
        definition.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("id", createSchemaProperty("string", "用户ID"));
        properties.put("username", createSchemaProperty("string", "用户名"));
        properties.put("email", createSchemaProperty("string", "邮箱"));
        properties.put("nickname", createSchemaProperty("string", "昵称"));
        properties.put("avatar", createSchemaProperty("string", "头像URL"));
        properties.put("role", createSchemaProperty("string", "角色"));
        properties.put("createdAt", createSchemaProperty("string", "创建时间"));

        definition.put("properties", properties);
        return definition;
    }

    // 创建版本信息定义
    private Map<String, Object> createVersionInfoDefinition() {
        Map<String, Object> definition = new HashMap<>();
        definition.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("version", createSchemaProperty("string", "版本号"));
        properties.put("buildNumber", createSchemaProperty("string", "构建号"));
        properties.put("commitHash", createSchemaProperty("string", "提交哈希"));
        properties.put("releaseDate", createSchemaProperty("string", "发布日期"));
        properties.put("latestVersion", createSchemaProperty("string", "最新版本"));
        properties.put("updateAvailable", createSchemaProperty("boolean", "是否有更新"));
        properties.put("updateUrl", createSchemaProperty("string", "更新URL"));
        properties.put("changelogUrl", createSchemaProperty("string", "更新日志URL"));
        properties.put("supportPeriod", createSchemaProperty("string", "支持周期"));

        definition.put("properties", properties);
        return definition;
    }

    // 创建模式属性
    private Map<String, Object> createSchemaProperty(String type, String description) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("description", description);
        return property;
    }
}
