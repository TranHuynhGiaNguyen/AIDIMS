const fs = require('fs');
const path = require('path');

const collectionPath = path.join(__dirname, '../tests/aidims_collection.json');

if (!fs.existsSync(collectionPath)) {
    console.error(`Collection file not found at ${collectionPath}`);
    process.exit(1);
}

const data = JSON.parse(fs.readFileSync(collectionPath, 'utf8'));

// Define BVA test cases
const bvaTestCases = [
  {
    "name": "User Management - Create User - BVA Valid Phone Length (20 chars)",
    "request": {
      "method": "POST",
      "header": [
        {
          "key": "Content-Type",
          "value": "application/json"
        }
      ],
      "body": {
        "mode": "raw",
        "raw": JSON.stringify({
          "username": "bva_user_valid",
          "password": "password123",
          "fullName": "BVA User Valid Phone",
          "email": "bva_user_valid@example.com",
          "phone": "09876543210987654321", // Exactly 20 characters
          "role": {
            "roleId": 2
          }
        }, null, 4)
      },
      "url": {
        "raw": "{{base_url}}/api/users",
        "host": [
          "{{base_url}}"
        ],
        "path": [
          "api",
          "users"
        ]
      }
    },
    "event": [
      {
        "listen": "test",
        "script": {
          "exec": [
            "pm.test(\"Status code is 200\", function () {",
            "    pm.response.to.have.status(200);",
            "});",
            "pm.test(\"User created with valid boundary phone length\", function () {",
            "    var jsonData = pm.response.json();",
            "    pm.expect(jsonData.status).to.eql(\"success\");",
            "    pm.expect(jsonData.data.userId).to.not.be.null;",
            "    // Clean up created user ID for deletion if needed",
            "    pm.environment.set(\"bvaValidUserId\", jsonData.data.userId);",
            "});"
          ],
          "type": "text/javascript"
        }
      }
    ]
  },
  {
    "name": "User Management - Create User - BVA Invalid Phone Length (21 chars)",
    "request": {
      "method": "POST",
      "header": [
        {
          "key": "Content-Type",
          "value": "application/json"
        }
      ],
      "body": {
        "mode": "raw",
        "raw": JSON.stringify({
          "username": "bva_user_invalid",
          "password": "password123",
          "fullName": "BVA User Invalid Phone",
          "email": "bva_user_invalid@example.com",
          "phone": "098765432109876543210", // Exactly 21 characters (exceeds 20)
          "role": {
            "roleId": 2
          }
        }, null, 4)
      },
      "url": {
        "raw": "{{base_url}}/api/users",
        "host": [
          "{{base_url}}"
        ],
        "path": [
          "api",
          "users"
        ]
      }
    },
    "event": [
      {
        "listen": "test",
        "script": {
          "exec": [
            "pm.test(\"Status code is 500 or 400\", function () {",
            "    pm.expect(pm.response.code).to.be.oneOf([400, 500]);",
            "});",
            "pm.test(\"Error message indicates database size constraint violation\", function () {",
            "    var jsonData = pm.response.json();",
            "    pm.expect(jsonData.status).to.eql(\"error\");",
            "    pm.expect(jsonData.message).to.include(\"lỗi\");",
            "});"
          ],
          "type": "text/javascript"
        }
      }
    ]
  },
  {
    "name": "Doctor - Diagnostic Report - Create Report - BVA Valid Report Code Length (20 chars)",
    "request": {
      "method": "POST",
      "header": [
        {
          "key": "Content-Type",
          "value": "application/json"
        }
      ],
      "body": {
        "mode": "raw",
        "raw": "{\n    \"resultId\": 1,\n    \"reportCode\": \"{{bvaValidReportCode}}\",\n    \"findings\": \"Phổi sáng bình thường.\",\n    \"impression\": \"Bình thường.\",\n    \"recommendations\": \"Khám định kỳ.\",\n    \"reportType\": \"ChinhThuc\",\n    \"radiologistId\": 4,\n    \"status\": \"HoanThanh\",\n    \"referringDoctorName\": \"BS. Nguyễn Văn A\",\n    \"referringDoctorSpecialty\": \"Tim mạch\"\n}"
      },
      "url": {
        "raw": "{{base_url}}/api/diagnostic-reports",
        "host": [
          "{{base_url}}"
        ],
        "path": [
          "api",
          "diagnostic-reports"
        ]
      }
    },
    "event": [
      {
        "listen": "prerequest",
        "script": {
          "exec": [
            "var rand = Math.floor(Math.random() * 100000).toString().padStart(5, '0');",
            "var code = 'BC' + Date.now().toString() + rand;",
            "pm.environment.set('bvaValidReportCode', code);"
          ],
          "type": "text/javascript"
        }
      },
      {
        "listen": "test",
        "script": {
          "exec": [
            "pm.test(\"Status code is 201\", function () {",
            "    pm.response.to.have.status(201);",
            "});",
            "pm.test(\"Diagnostic report created with valid boundary reportCode length\", function () {",
            "    var jsonData = pm.response.json();",
            "    pm.expect(jsonData.success).to.eql(true);",
            "    pm.expect(jsonData.data.reportId).to.not.be.null;",
            "    pm.environment.set(\"bvaValidReportId\", jsonData.data.reportId);",
            "});"
          ],
          "type": "text/javascript"
        }
      }
    ]
  },
  {
    "name": "Doctor - Diagnostic Report - Create Report - BVA Invalid Report Code Length (21 chars)",
    "request": {
      "method": "POST",
      "header": [
        {
          "key": "Content-Type",
          "value": "application/json"
        }
      ],
      "body": {
        "mode": "raw",
        "raw": "{\n    \"resultId\": 1,\n    \"reportCode\": \"{{bvaInvalidReportCode}}\",\n    \"findings\": \"Phổi sáng bình thường.\",\n    \"impression\": \"Bình thường.\",\n    \"recommendations\": \"Khám định kỳ.\",\n    \"reportType\": \"ChinhThuc\",\n    \"radiologistId\": 4,\n    \"status\": \"HoanThanh\",\n    \"referringDoctorName\": \"BS. Nguyễn Văn A\",\n    \"referringDoctorSpecialty\": \"Tim mạch\"\n}"
      },
      "url": {
        "raw": "{{base_url}}/api/diagnostic-reports",
        "host": [
          "{{base_url}}"
        ],
        "path": [
          "api",
          "diagnostic-reports"
        ]
      }
    },
    "event": [
      {
        "listen": "prerequest",
        "script": {
          "exec": [
            "var rand = Math.floor(Math.random() * 1000000).toString().padStart(6, '0');",
            "var code = 'BC' + Date.now().toString() + rand;",
            "pm.environment.set('bvaInvalidReportCode', code);"
          ],
          "type": "text/javascript"
        }
      },
      {
        "listen": "test",
        "script": {
          "exec": [
            "pm.test(\"Status code is 400 or 500\", function () {",
            "    pm.expect(pm.response.code).to.be.oneOf([400, 500]);",
            "});",
            "pm.test(\"Error response indicates validation error or database violation\", function () {",
            "    var jsonData = pm.response.json();",
            "    pm.expect(jsonData.success).to.eql(false);",
            "    pm.expect(jsonData.message).to.include(\"Lỗi\");",
            "});"
          ],
          "type": "text/javascript"
        }
      }
    ]
  },
  // Clean up BVA created data
  {
    "name": "User Management - Delete BVA Valid User",
    "request": {
      "method": "DELETE",
      "header": [],
      "url": {
        "raw": "{{base_url}}/api/users/{{bvaValidUserId}}",
        "host": [
          "{{base_url}}"
        ],
        "path": [
          "api",
          "users",
          "{{bvaValidUserId}}"
        ]
      }
    },
    "event": [
      {
        "listen": "test",
        "script": {
          "exec": [
            "pm.test(\"Status code is 200\", function () {",
            "    pm.response.to.have.status(200);",
            "});"
          ],
          "type": "text/javascript"
        }
      }
    ]
  }
];

// Append BVA test cases
data.item.push(...bvaTestCases);

// Write back updated collection file
fs.writeFileSync(collectionPath, JSON.stringify(data, null, 2), 'utf8');
console.log('✅ BVA Test Cases successfully appended to the Postman collection!');
