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
public class SystemGetTermsOfService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取使用条款请求 ===");
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
    public static class TermsRequest {
        private String language;
        private String version;

        public String getLanguage() { return language != null ? language : "zh-CN"; }
        public void setLanguage(String language) { this.language = language; }

        public String getVersion() { return version != null ? version : "1.0"; }
        public void setVersion(String version) { this.version = version; }
    }

    // 响应DTO
    public static class TermsResponse {
        private boolean success;
        private String message;
        private TermsData data;

        public TermsResponse(boolean success, String message, TermsData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public TermsData getData() { return data; }
        public void setData(TermsData data) { this.data = data; }
    }

    public static class TermsData {
        private String title;
        private String version;
        private String language;
        private TermsContent content;

        public TermsData(String title, String version, String language, TermsContent content) {
            this.title = title;
            this.version = version;
            this.language = language;
            this.content = content;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public TermsContent getContent() { return content; }
        public void setContent(TermsContent content) { this.content = content; }
    }

    public static class TermsContent {
        private String preamble;
        private List<TermsSection> terms;
        private String lastUpdated;

        public TermsContent(String preamble, List<TermsSection> terms, String lastUpdated) {
            this.preamble = preamble;
            this.terms = terms;
            this.lastUpdated = lastUpdated;
        }

        public String getPreamble() { return preamble; }
        public void setPreamble(String preamble) { this.preamble = preamble; }

        public List<TermsSection> getTerms() { return terms; }
        public void setTerms(List<TermsSection> terms) { this.terms = terms; }

        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class TermsSection {
        private String section;
        private String content;

        public TermsSection(String section, String content) {
            this.section = section;
            this.content = content;
        }

        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    @GetMapping("/terms")
    public ResponseEntity<TermsResponse> getTermsOfService(@RequestParam(required = false) String language,
                                                           @RequestParam(required = false) String version) {

        // 创建请求对象
        TermsRequest request = new TermsRequest();
        request.setLanguage(language);
        request.setVersion(version);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 根据语言和版本构建使用条款
            TermsContent termsContent = buildTermsContent(request.getLanguage(), request.getVersion());

            // 2. 构建响应数据
            TermsData termsData = new TermsData(
                    "使用条款",
                    request.getVersion(),
                    request.getLanguage(),
                    termsContent
            );

            TermsResponse response = new TermsResponse(true, "获取使用条款成功", termsData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取使用条款过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new TermsResponse(false, "获取使用条款失败: " + e.getMessage(), null)
            );
        }
    }

    // 构建使用条款内容
    private TermsContent buildTermsContent(String language, String version) {
        String preamble;
        List<TermsSection> terms = new ArrayList<>();

        if ("zh-CN".equals(language)) {
            preamble = "欢迎使用Vue Reading App（以下简称\"本应用\"）。请仔细阅读以下使用条款。";

            terms.add(new TermsSection("1. 服务条款",
                    "1.1 本应用由Vue Reading App团队（以下简称\"我们\"）提供。" +
                            "1.2 用户在使用本应用前，应当仔细阅读本条款，并同意遵守本条款及相关法律法规。" +
                            "1.3 我们保留随时修改本条款的权利，修改后的条款将在本应用内公布，不另行通知。"));

            terms.add(new TermsSection("2. 用户责任",
                    "2.1 用户应保证其提供的信息真实、准确、完整。" +
                            "2.2 用户不得利用本应用从事任何违法活动或侵犯他人权益的行为。" +
                            "2.3 用户应妥善保管其账户信息，不得将其账户转让或出借给他人使用。" +
                            "2.4 用户对其账户下的所有行为承担全部责任。"));

            terms.add(new TermsSection("3. 知识产权",
                    "3.1 本应用的所有内容，包括但不限于文字、图片、音频、视频、软件等，均受知识产权法保护。" +
                            "3.2 未经我们书面许可，用户不得以任何形式复制、传播、修改或商业使用本应用的内容。" +
                            "3.3 用户上传的内容，其知识产权仍归用户所有，但用户授予我们使用、存储、展示该内容的权利。"));

            terms.add(new TermsSection("4. 隐私保护",
                    "4.1 我们尊重用户隐私，将按照隐私政策保护用户的个人信息。" +
                            "4.2 我们不会在未经用户同意的情况下向第三方提供用户的个人信息，法律法规另有规定的除外。" +
                            "4.3 用户有权随时查看、修改或删除其个人信息。"));

            terms.add(new TermsSection("5. 服务变更与终止",
                    "5.1 我们有权根据业务需要调整服务内容，或暂停、终止部分或全部服务。" +
                            "5.2 如用户违反本条款，我们有权暂停或终止为用户提供服务。" +
                            "5.3 服务终止后，用户使用本应用的权利立即终止，我们不再对用户承担任何义务。"));

            terms.add(new TermsSection("6. 免责声明",
                    "6.1 本应用按\"现状\"提供，我们不保证服务不会中断，也不保证服务的及时性、安全性、准确性。" +
                            "6.2 对于因使用本应用而导致的任何直接或间接损失，我们不承担任何责任。" +
                            "6.3 用户理解并同意，其使用本应用的风险由其自行承担。"));

            terms.add(new TermsSection("7. 法律适用与争议解决",
                    "7.1 本条款的订立、执行和解释及争议的解决均适用中华人民共和国法律。" +
                            "7.2 如双方就本条款内容或其执行发生任何争议，应首先友好协商解决；协商不成时，任何一方均可向我们所在地的人民法院提起诉讼。"));

            terms.add(new TermsSection("8. 其他",
                    "8.1 本条款构成用户与我们之间就本应用使用所达成的完整协议。" +
                            "8.2 如本条款的任何条款被认定为无效或不可执行，该条款应在最小范围内受影响，不影响其他条款的效力。" +
                            "8.3 我们的未行使或延迟行使其在本条款项下的权利，不构成对该权利的放弃。"));

        } else {
            // 默认英文版本
            preamble = "Welcome to Vue Reading App (hereinafter referred to as \"the App\"). Please read the following terms of service carefully.";

            terms.add(new TermsSection("1. Service Terms",
                    "1.1 The App is provided by the Vue Reading App team (hereinafter referred to as \"we\" or \"us\")." +
                            "1.2 Before using the App, users should carefully read these terms and agree to comply with these terms and relevant laws and regulations." +
                            "1.3 We reserve the right to modify these terms at any time. Modified terms will be published in the App without further notice."));

            terms.add(new TermsSection("2. User Responsibilities",
                    "2.1 Users shall ensure that the information they provide is true, accurate, and complete." +
                            "2.2 Users shall not use the App to engage in any illegal activities or infringe upon the rights of others." +
                            "2.3 Users shall properly keep their account information and shall not transfer or lend their accounts to others." +
                            "2.4 Users shall bear full responsibility for all actions under their accounts."));

            terms.add(new TermsSection("3. Intellectual Property",
                    "3.1 All content of the App, including but not limited to text, images, audio, video, software, etc., is protected by intellectual property laws." +
                            "3.2 Without our written permission, users shall not copy, disseminate, modify, or commercially use the content of the App in any form." +
                            "3.3 The intellectual property rights of content uploaded by users remain with the users, but users grant us the right to use, store, and display such content."));

            terms.add(new TermsSection("4. Privacy Protection",
                    "4.1 We respect user privacy and will protect users' personal information in accordance with the privacy policy." +
                            "4.2 We will not provide users' personal information to third parties without user consent, except as required by laws and regulations." +
                            "4.3 Users have the right to view, modify, or delete their personal information at any time."));

            terms.add(new TermsSection("5. Service Changes and Termination",
                    "5.1 We have the right to adjust service content, or suspend or terminate part or all of the services according to business needs." +
                            "5.2 If users violate these terms, we have the right to suspend or terminate the provision of services to users." +
                            "5.3 After service termination, users' right to use the App immediately terminates, and we no longer have any obligations to users."));

            terms.add(new TermsSection("6. Disclaimer",
                    "6.1 The App is provided \"as is\". We do not guarantee that the service will not be interrupted, nor do we guarantee the timeliness, security, or accuracy of the service." +
                            "6.2 We shall not be liable for any direct or indirect losses caused by using the App." +
                            "6.3 Users understand and agree that the risks of using the App are borne by themselves."));

            terms.add(new TermsSection("7. Governing Law and Dispute Resolution",
                    "7.1 The conclusion, execution, and interpretation of these terms and the resolution of disputes shall be governed by the laws of the People's Republic of China." +
                            "7.2 If any dispute arises between the parties regarding the content or execution of these terms, the parties shall first try to resolve it through friendly negotiation; if negotiation fails, either party may file a lawsuit with the people's court in our location."));

            terms.add(new TermsSection("8. Miscellaneous",
                    "8.1 These terms constitute the complete agreement between users and us regarding the use of the App." +
                            "8.2 If any provision of these terms is deemed invalid or unenforceable, that provision shall be affected to the minimum extent and shall not affect the validity of other provisions." +
                            "8.3 Our failure or delay in exercising our rights under these terms shall not constitute a waiver of such rights."));
        }

        return new TermsContent(
                preamble,
                terms,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}

