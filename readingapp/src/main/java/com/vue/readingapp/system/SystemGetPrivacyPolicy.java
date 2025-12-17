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
public class SystemGetPrivacyPolicy {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 打印接收到的请求
    private void printRequest(Object request) {
        System.out.println("=== 收到获取隐私政策请求 ===");
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
    public static class PrivacyRequest {
        private String language;
        private String version;

        public String getLanguage() { return language != null ? language : "zh-CN"; }
        public void setLanguage(String language) { this.language = language; }

        public String getVersion() { return version != null ? version : "1.0"; }
        public void setVersion(String version) { this.version = version; }
    }

    // 响应DTO
    public static class PrivacyResponse {
        private boolean success;
        private String message;
        private PrivacyData data;

        public PrivacyResponse(boolean success, String message, PrivacyData data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public PrivacyData getData() { return data; }
        public void setData(PrivacyData data) { this.data = data; }
    }

    public static class PrivacyData {
        private String title;
        private String version;
        private String language;
        private String effectiveDate;
        private List<PrivacySection> sections;
        private ContactInfo contact;

        public PrivacyData(String title, String version, String language, String effectiveDate,
                           List<PrivacySection> sections, ContactInfo contact) {
            this.title = title;
            this.version = version;
            this.language = language;
            this.effectiveDate = effectiveDate;
            this.sections = sections;
            this.contact = contact;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getEffectiveDate() { return effectiveDate; }
        public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

        public List<PrivacySection> getSections() { return sections; }
        public void setSections(List<PrivacySection> sections) { this.sections = sections; }

        public ContactInfo getContact() { return contact; }
        public void setContact(ContactInfo contact) { this.contact = contact; }
    }

    public static class PrivacySection {
        private String title;
        private String content;

        public PrivacySection(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ContactInfo {
        private String email;
        private String address;

        public ContactInfo(String email, String address) {
            this.email = email;
            this.address = address;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    @GetMapping("/privacy")
    public ResponseEntity<PrivacyResponse> getPrivacyPolicy(@RequestParam(required = false) String language,
                                                            @RequestParam(required = false) String version) {

        // 创建请求对象
        PrivacyRequest request = new PrivacyRequest();
        request.setLanguage(language);
        request.setVersion(version);

        // 打印接收到的请求
        printRequest(request);

        try {
            // 1. 根据语言和版本构建隐私政策
            List<PrivacySection> sections = buildPrivacySections(request.getLanguage());
            ContactInfo contactInfo = buildContactInfo(request.getLanguage());

            // 2. 构建响应数据
            PrivacyData privacyData = new PrivacyData(
                    "隐私政策",
                    request.getVersion(),
                    request.getLanguage(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    sections,
                    contactInfo
            );

            PrivacyResponse response = new PrivacyResponse(true, "获取隐私政策成功", privacyData);

            // 打印返回数据
            printResponse(response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取隐私政策过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new PrivacyResponse(false, "获取隐私政策失败: " + e.getMessage(), null)
            );
        }
    }

    // 构建隐私政策章节
    private List<PrivacySection> buildPrivacySections(String language) {
        List<PrivacySection> sections = new ArrayList<>();

        if ("zh-CN".equals(language)) {
            sections.add(new PrivacySection("信息收集",
                    "我们收集以下类型的信息：\n" +
                            "1. 账户信息：当您注册账户时，我们会收集您的用户名、邮箱地址、密码等信息。\n" +
                            "2. 使用数据：我们会收集您使用本应用的相关信息，包括但不限于您上传的文档、添加的词汇、阅读记录、学习进度等。\n" +
                            "3. 设备信息：为了提供更好的服务，我们会收集您的设备信息，包括设备型号、操作系统版本、浏览器类型等。\n" +
                            "4. 日志信息：当您使用本应用时，我们会自动收集某些日志信息，包括IP地址、访问时间、页面浏览记录等。"));

            sections.add(new PrivacySection("信息使用",
                    "我们使用收集的信息用于以下目的：\n" +
                            "1. 提供和维护服务：使用您的信息来提供、维护和改进本应用的功能和服务。\n" +
                            "2. 个性化体验：根据您的使用习惯和偏好，为您提供个性化的学习内容和推荐。\n" +
                            "3. 沟通与支持：使用您的联系信息与您沟通，回复您的咨询，提供技术支持。\n" +
                            "4. 安全与合规：检测和防止欺诈、滥用和安全问题，遵守法律法规要求。\n" +
                            "5. 分析与研究：分析使用模式，进行产品研究和开发，以改进用户体验。"));

            sections.add(new PrivacySection("信息共享",
                    "我们不会出售或出租您的个人信息给第三方。我们仅在以下情况下共享您的信息：\n" +
                            "1. 征得您的同意：在获得您的明确同意后，我们会与其他方共享您的信息。\n" +
                            "2. 法律要求：为遵守适用的法律法规、法律程序或政府要求，我们可能会披露您的信息。\n" +
                            "3. 保护权利：为保护我们、我们的用户或公众的权利、财产或安全，我们可能会披露信息。\n" +
                            "4. 服务提供商：我们可能会与为我们提供服务的第三方共享必要的信息，但这些服务提供商必须遵守严格的保密义务。"));

            sections.add(new PrivacySection("数据安全",
                    "我们采取合理的技术和组织措施来保护您的个人信息安全：\n" +
                            "1. 加密技术：对敏感信息进行加密存储和传输。\n" +
                            "2. 访问控制：限制对个人信息的访问权限，仅允许有必要知晓的员工访问。\n" +
                            "3. 安全审计：定期进行安全审计和漏洞扫描，及时修复安全问题。\n" +
                            "4. 员工培训：对员工进行隐私保护培训，确保他们了解并遵守隐私政策。\n" +
                            "然而，请注意没有任何安全措施是绝对可靠的，我们无法保证信息的绝对安全。"));

            sections.add(new PrivacySection("您的权利",
                    "您对自己的个人信息享有以下权利：\n" +
                            "1. 访问权：您有权访问我们持有的关于您的个人信息。\n" +
                            "2. 更正权：您有权要求更正不准确或不完整的个人信息。\n" +
                            "3. 删除权：在某些情况下，您有权要求删除您的个人信息。\n" +
                            "4. 限制处理权：您有权限制我们对您个人信息的使用。\n" +
                            "5. 数据可携权：您有权以结构化、常用和机器可读的格式获取您的个人信息。\n" +
                            "6. 反对权：您有权反对基于特定目的处理您的个人信息。\n" +
                            "如需行使上述权利，请通过隐私政策中提供的联系方式与我们联系。"));

            sections.add(new PrivacySection("数据保留",
                    "我们仅在实现本隐私政策所述目的所需的时间内保留您的个人信息，除非法律要求或允许更长的保留期。\n" +
                            "具体的保留期限取决于信息的类型、收集目的以及法律要求。\n" +
                            "当不再需要您的个人信息时，我们会采取安全的方式将其删除或匿名化处理。"));

            sections.add(new PrivacySection("儿童隐私",
                    "我们的服务不面向13岁以下的儿童。我们不会故意收集13岁以下儿童的个人信息。\n" +
                            "如果您是父母或监护人，并且认为您的孩子向我们提供了个人信息，请与我们联系，我们将采取措施删除这些信息。"));

            sections.add(new PrivacySection("政策更新",
                    "我们可能会不时更新本隐私政策。如有重大变更，我们会在本应用内发布通知，并更新政策顶部的生效日期。\n" +
                            "我们建议您定期查看本隐私政策以了解任何变更。继续使用我们的服务即表示您接受更新后的隐私政策。"));

        } else {
            // 默认英文版本
            sections.add(new PrivacySection("Information Collection",
                    "We collect the following types of information:\n" +
                            "1. Account Information: When you register an account, we collect your username, email address, password, etc.\n" +
                            "2. Usage Data: We collect information related to your use of the App, including but not limited to documents you upload, vocabulary you add, reading records, learning progress, etc.\n" +
                            "3. Device Information: To provide better services, we collect your device information, including device model, operating system version, browser type, etc.\n" +
                            "4. Log Information: When you use the App, we automatically collect certain log information, including IP address, access time, page view records, etc."));

            sections.add(new PrivacySection("Information Use",
                    "We use the collected information for the following purposes:\n" +
                            "1. Provide and Maintain Services: Use your information to provide, maintain, and improve the functions and services of the App.\n" +
                            "2. Personalized Experience: Provide you with personalized learning content and recommendations based on your usage habits and preferences.\n" +
                            "3. Communication and Support: Use your contact information to communicate with you, respond to your inquiries, and provide technical support.\n" +
                            "4. Security and Compliance: Detect and prevent fraud, abuse, and security issues, and comply with legal and regulatory requirements.\n" +
                            "5. Analysis and Research: Analyze usage patterns, conduct product research and development to improve user experience."));

            sections.add(new PrivacySection("Information Sharing",
                    "We do not sell or rent your personal information to third parties. We only share your information in the following circumstances:\n" +
                            "1. With Your Consent: We will share your information with other parties after obtaining your explicit consent.\n" +
                            "2. Legal Requirements: To comply with applicable laws, regulations, legal procedures, or government requirements, we may disclose your information.\n" +
                            "3. Protection of Rights: To protect the rights, property, or safety of us, our users, or the public, we may disclose information.\n" +
                            "4. Service Providers: We may share necessary information with third parties who provide services to us, but these service providers must comply with strict confidentiality obligations."));

            sections.add(new PrivacySection("Data Security",
                    "We take reasonable technical and organizational measures to protect the security of your personal information:\n" +
                            "1. Encryption Technology: Encrypt sensitive information for storage and transmission.\n" +
                            "2. Access Control: Restrict access to personal information, allowing only employees who need to know to access it.\n" +
                            "3. Security Audits: Regularly conduct security audits and vulnerability scans, and promptly fix security issues.\n" +
                            "4. Employee Training: Provide privacy protection training to employees to ensure they understand and comply with the privacy policy.\n" +
                            "However, please note that no security measures are absolutely reliable, and we cannot guarantee absolute security of information."));

            sections.add(new PrivacySection("Your Rights",
                    "You have the following rights regarding your personal information:\n" +
                            "1. Right of Access: You have the right to access the personal information we hold about you.\n" +
                            "2. Right to Rectification: You have the right to request correction of inaccurate or incomplete personal information.\n" +
                            "3. Right to Erasure: In certain circumstances, you have the right to request deletion of your personal information.\n" +
                            "4. Right to Restriction of Processing: You have the right to restrict our use of your personal information.\n" +
                            "5. Right to Data Portability: You have the right to obtain your personal information in a structured, commonly used, and machine-readable format.\n" +
                            "6. Right to Object: You have the right to object to the processing of your personal information for specific purposes.\n" +
                            "To exercise the above rights, please contact us using the contact information provided in the privacy policy."));

            sections.add(new PrivacySection("Data Retention",
                    "We retain your personal information only for as long as necessary to achieve the purposes described in this privacy policy, unless the law requires or permits a longer retention period.\n" +
                            "The specific retention period depends on the type of information, the purpose of collection, and legal requirements.\n" +
                            "When your personal information is no longer needed, we will delete or anonymize it in a secure manner."));

            sections.add(new PrivacySection("Children's Privacy",
                    "Our services are not directed to children under 13 years of age. We do not knowingly collect personal information from children under 13.\n" +
                            "If you are a parent or guardian and believe that your child has provided us with personal information, please contact us, and we will take steps to delete such information."));

            sections.add(new PrivacySection("Policy Updates",
                    "We may update this privacy policy from time to time. If there are significant changes, we will post a notice in the App and update the effective date at the top of the policy.\n" +
                            "We recommend that you regularly review this privacy policy to be aware of any changes. Continued use of our services indicates your acceptance of the updated privacy policy."));
        }

        return sections;
    }

    // 构建联系信息
    private ContactInfo buildContactInfo(String language) {
        if ("zh-CN".equals(language)) {
            return new ContactInfo(
                    "privacy@vuereadingapp.com",
                    "北京市朝阳区某某路123号"
            );
        } else {
            return new ContactInfo(
                    "privacy@vuereadingapp.com",
                    "123 Some Road, Chaoyang District, Beijing, China"
            );
        }
    }
}
