import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AddBvaTests {
    public static void main(String[] args) {
        String filePath = "../tests/aidims_collection.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");
            
            // Check if already added
            if (content.contains("BVA Valid Phone Length")) {
                System.out.println("BVA test cases already added!");
                return;
            }

            // The new BVA test cases as a JSON string fragment
            String bvaJsonFragment = ",\n" +
                "    {\n" +
                "      \"name\": \"User Management - Create User - BVA Valid Phone Length (20 chars)\",\n" +
                "      \"request\": {\n" +
                "        \"method\": \"POST\",\n" +
                "        \"header\": [\n" +
                "          {\n" +
                "            \"key\": \"Content-Type\",\n" +
                "            \"value\": \"application/json\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"body\": {\n" +
                "          \"mode\": \"raw\",\n" +
                "          \"raw\": \"{\\n    \\\"username\\\": \\\"bva_user_valid\\\",\\n    \\\"password\\\": \\\"password123\\\",\\n    \\\"fullName\\\": \\\"BVA User Valid Phone\\\",\\n    \\\"email\\\": \\\"bva_user_valid@example.com\\\",\\n    \\\"phone\\\": \\\"09876543210987654321\\\",\\n    \\\"role\\\": {\\n        \\\"roleId\\\": 2\\n    }\\n}\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"raw\": \"{{base_url}}/api/users\",\n" +
                "          \"host\": [\n" +
                "            \"{{base_url}}\"\n" +
                "          ],\n" +
                "          \"path\": [\n" +
                "            \"api\",\n" +
                "            \"users\"\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      \"event\": [\n" +
                "        {\n" +
                "          \"listen\": \"test\",\n" +
                "          \"script\": {\n" +
                "            \"exec\": [\n" +
                "              \"pm.test(\\\"Status code is 200\\\", function () {\",\n" +
                "              \"    pm.response.to.have.status(200);\",\n" +
                "              \"});\",\n" +
                "              \"pm.test(\\\"User created with valid boundary phone length\\\", function () {\",\n" +
                "              \"    var jsonData = pm.response.json();\",\n" +
                "              \"    pm.expect(jsonData.status).to.eql(\\\"success\\\");\",\n" +
                "              \"    pm.expect(jsonData.data.userId).to.not.be.null;\",\n" +
                "              \"    pm.environment.set(\\\"bvaValidUserId\\\", jsonData.data.userId);\",\n" +
                "              \"});\"\n" +
                "            ],\n" +
                "            \"type\": \"text/javascript\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"User Management - Create User - BVA Invalid Phone Length (21 chars)\",\n" +
                "      \"request\": {\n" +
                "        \"method\": \"POST\",\n" +
                "        \"header\": [\n" +
                "          {\n" +
                "            \"key\": \"Content-Type\",\n" +
                "            \"value\": \"application/json\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"body\": {\n" +
                "          \"mode\": \"raw\",\n" +
                "          \"raw\": \"{\\n    \\\"username\\\": \\\"bva_user_invalid\\\",\\n    \\\"password\\\": \\\"password123\\\",\\n    \\\"fullName\\\": \\\"BVA User Invalid Phone\\\",\\n    \\\"email\\\": \\\"bva_user_invalid@example.com\\\",\\n    \\\"phone\\\": \\\"098765432109876543210\\\",\\n    \\\"role\\\": {\\n        \\\"roleId\\\": 2\\n    }\\n}\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"raw\": \"{{base_url}}/api/users\",\n" +
                "          \"host\": [\n" +
                "            \"{{base_url}}\"\n" +
                "          ],\n" +
                "          \"path\": [\n" +
                "            \"api\",\n" +
                "            \"users\"\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      \"event\": [\n" +
                "        {\n" +
                "          \"listen\": \"test\",\n" +
                "          \"script\": {\n" +
                "            \"exec\": [\n" +
                "              \"pm.test(\\\"Status code is 500 or 400\\\", function () {\",\n" +
                "              \"    pm.expect(pm.response.code).to.be.oneOf([400, 500]);\",\n" +
                "              \"});\",\n" +
                "              \"pm.test(\\\"Error message indicates database size constraint violation\\\", function () {\",\n" +
                "              \"    var jsonData = pm.response.json();\",\n" +
                "              \"    pm.expect(jsonData.status).to.eql(\\\"error\\\");\",\n" +
                "              \"    pm.expect(jsonData.message).to.include(\\\"lỗi\\\");\",\n" +
                "              \"});\"\n" +
                "            ],\n" +
                "            \"type\": \"text/javascript\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Doctor - Diagnostic Report - Create Report - BVA Valid Report Code Length (20 chars)\",\n" +
                "      \"request\": {\n" +
                "        \"method\": \"POST\",\n" +
                "        \"header\": [\n" +
                "          {\n" +
                "            \"key\": \"Content-Type\",\n" +
                "            \"value\": \"application/json\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"body\": {\n" +
                "          \"mode\": \"raw\",\n" +
                "          \"raw\": \"{\\n    \\\"resultId\\\": 1,\\n    \\\"reportCode\\\": \\\"BC202506240012345678\\\",\\n    \\\"findings\\\": \\\"Phổi sáng bình thường.\\\",\\n    \\\"impression\\\": \\\"Bình thường.\\\",\\n    \\\"recommendations\\\": \\\"Khám định kỳ.\\\",\\n    \\\"reportType\\\": \\\"ChinhThuc\\\",\\n    \\\"radiologistId\\\": 4,\\n    \\\"status\\\": \\\"HoanThanh\\\",\\n    \\\"referringDoctorName\\\": \\\"BS. Nguyễn Văn A\\\",\\n    \\\"referringDoctorSpecialty\\\": \\\"Tim mạch\\\"\\n}\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"raw\": \"{{base_url}}/api/diagnostic-reports\",\n" +
                "          \"host\": [\n" +
                "            \"{{base_url}}\"\n" +
                "          ],\n" +
                "          \"path\": [\n" +
                "            \"api\",\n" +
                "            \"diagnostic-reports\"\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      \"event\": [\n" +
                "        {\n" +
                "          \"listen\": \"test\",\n" +
                "          \"script\": {\n" +
                "            \"exec\": [\n" +
                "              \"pm.test(\\\"Status code is 201\\\", function () {\",\n" +
                "              \"    pm.response.to.have.status(201);\",\n" +
                "              \"});\",\n" +
                "              \"pm.test(\\\"Diagnostic report created with valid boundary reportCode length\\\", function () {\",\n" +
                "              \"    var jsonData = pm.response.json();\",\n" +
                "              \"    pm.expect(jsonData.success).to.eql(true);\",\n" +
                "              \"    pm.expect(jsonData.data.reportId).to.not.be.null;\",\n" +
                "              \"    pm.environment.set(\\\"bvaValidReportId\\\", jsonData.data.reportId);\",\n" +
                "              \"});\"\n" +
                "            ],\n" +
                "            \"type\": \"text/javascript\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Doctor - Diagnostic Report - Create Report - BVA Invalid Report Code Length (21 chars)\",\n" +
                "      \"request\": {\n" +
                "        \"method\": \"POST\",\n" +
                "        \"header\": [\n" +
                "          {\n" +
                "            \"key\": \"Content-Type\",\n" +
                "            \"value\": \"application/json\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"body\": {\n" +
                "          \"mode\": \"raw\",\n" +
                "          \"raw\": \"{\\n    \\\"resultId\\\": 1,\\n    \\\"reportCode\\\": \\\"BC2025062400123456789\\\",\\n    \\\"findings\\\": \\\"Phổi sáng bình thường.\\\",\\n    \\\"impression\\\": \\\"Bình thường.\\\",\\n    \\\"recommendations\\\": \\\"Khám định kỳ.\\\",\\n    \\\"reportType\\\": \\\"ChinhThuc\\\",\\n    \\\"radiologistId\\\": 4,\\n    \\\"status\\\": \\\"HoanThanh\\\",\\n    \\\"referringDoctorName\\\": \\\"BS. Nguyễn Văn A\\\",\\n    \\\"referringDoctorSpecialty\\\": \\\"Tim mạch\\\"\\n}\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"raw\": \"{{base_url}}/api/diagnostic-reports\",\n" +
                "          \"host\": [\n" +
                "            \"{{base_url}}\"\n" +
                "          ],\n" +
                "          \"path\": [\n" +
                "            \"api\",\n" +
                "            \"diagnostic-reports\"\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      \"event\": [\n" +
                "        {\n" +
                "          \"listen\": \"test\",\n" +
                "          \"script\": {\n" +
                "            \"exec\": [\n" +
                "              \"pm.test(\\\"Status code is 400 or 500\\\", function () {\",\n" +
                "              \"    pm.expect(pm.response.code).to.be.oneOf([400, 500]);\",\n" +
                "              \"});\",\n" +
                "              \"pm.test(\\\"Error response indicates validation error or database violation\\\", function () {\",\n" +
                "              \"    var jsonData = pm.response.json();\",\n" +
                "              \"    pm.expect(jsonData.success).to.eql(false);\",\n" +
                "              \"    pm.expect(jsonData.message).to.include(\\\"Lỗi\\\");\",\n" +
                "              \"});\"\n" +
                "            ],\n" +
                "            \"type\": \"text/javascript\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"User Management - Delete BVA Valid User\",\n" +
                "      \"request\": {\n" +
                "        \"method\": \"DELETE\",\n" +
                "        \"header\": [],\n" +
                "        \"url\": {\n" +
                "          \"raw\": \"{{base_url}}/api/users/{{bvaValidUserId}}\",\n" +
                "          \"host\": [\n" +
                "            \"{{base_url}}\"\n" +
                "          ],\n" +
                "          \"path\": [\n" +
                "            \"api\",\n" +
                "            \"users\",\n" +
                "            \"{{bvaValidUserId}}\"\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      \"event\": [\n" +
                "        {\n" +
                "          \"listen\": \"test\",\n" +
                "          \"script\": {\n" +
                "            \"exec\": [\n" +
                "              \"pm.test(\\\"Status code is 200\\\", function () {\",\n" +
                "              \"    pm.response.to.have.status(200);\",\n" +
                "              \"});\"\n" +
                "            ],\n" +
                "            \"type\": \"text/javascript\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }";

            // Find the last occurrence of ] before the final closing }
            int lastBracketIndex = content.lastIndexOf("]");
            if (lastBracketIndex == -1) {
                System.err.println("Could not find closing bracket in collection JSON!");
                return;
            }

            // Insert before the last bracket
            String newContent = content.substring(0, lastBracketIndex) + bvaJsonFragment + "\n  " + content.substring(lastBracketIndex);
            
            // Write it back
            Files.write(Paths.get(filePath), newContent.getBytes("UTF-8"));
            System.out.println("✅ BVA Test Cases successfully appended to the Postman collection via Java!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
