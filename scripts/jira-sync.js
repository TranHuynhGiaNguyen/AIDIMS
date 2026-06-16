const fs = require('fs');
const path = require('path');

// Load environment variables from .env file if it exists
const potentialEnvPaths = [
    path.join(process.cwd(), '.env'),
    path.join(__dirname, '..', '.env'),
    path.join(__dirname, '..', '..', '.env')
];

for (const envPath of potentialEnvPaths) {
    if (fs.existsSync(envPath)) {
        const dotenvText = fs.readFileSync(envPath, 'utf8');
        dotenvText.split(/\r?\n/).forEach(line => {
            const trimmedLine = line.trim();
            if (trimmedLine && !trimmedLine.startsWith('#')) {
                const match = trimmedLine.match(/^\s*([\w.-]+)\s*=\s*(.*)?\s*$/);
                if (match) {
                    const key = match[1];
                    let value = match[2] || '';
                    // Remove quotes if present
                    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length - 1);
                    }
                    process.env[key] = value.trim();
                }
            }
        });
        break; // Stop after loading the first found .env file
    }
}

function cleanSecret(val) {
    if (!val) return '';
    let s = val.trim();
    if ((s.startsWith('"') && s.endsWith('"')) || (s.startsWith("'") && s.endsWith("'"))) {
        s = s.substring(1, s.length - 1).trim();
    }
    return s;
}

// Configuration from environment variables
let rawDomain = process.env.JIRA_BASE_URL || process.env.JIRA_DOMAIN;
if (rawDomain && !rawDomain.startsWith('http://') && !rawDomain.startsWith('https://')) {
    rawDomain = 'https://' + rawDomain;
}
const JIRA_DOMAIN = cleanSecret(rawDomain); // e.g., https://your-domain.atlassian.net
const JIRA_EMAIL = cleanSecret(process.env.JIRA_EMAIL);
const JIRA_TOKEN = cleanSecret(process.env.JIRA_API_TOKEN || process.env.JIRA_TOKEN);
const JIRA_PROJECT_KEY = cleanSecret(process.env.JIRA_PROJECT || process.env.JIRA_PROJECT_KEY);
const REPORT_PATH = cleanSecret(process.env.NEWMAN_REPORT_PATH || './newman-report.json');

if (!JIRA_DOMAIN || !JIRA_EMAIL || !JIRA_TOKEN || !JIRA_PROJECT_KEY) {
    console.error('❌ Error: Missing Jira configuration in environment variables or .env file (JIRA_BASE_URL/JIRA_DOMAIN, JIRA_EMAIL, JIRA_API_TOKEN/JIRA_TOKEN, JIRA_PROJECT/JIRA_PROJECT_KEY).');
    process.exit(1);
}

// Prepare basic auth header
const authHeader = 'Basic ' + Buffer.from(`${JIRA_EMAIL}:${JIRA_TOKEN}`).toString('base64');

/**
 * Helper to call Jira REST API v3
 * @param {string} endpoint 
 * @param {object} options 
 */
async function callJira(endpoint, options = {}) {
    const url = `${JIRA_DOMAIN.replace(/\/$/, '')}/rest/api/3/${endpoint.replace(/^\//, '')}`;
    const response = await fetch(url, {
        ...options,
        headers: {
            'Authorization': authHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            ...options.headers
        }
    });

    if (!response.ok) {
        const errText = await response.text();
        throw new Error(`Jira API Error [${response.status}]: ${errText}`);
    }

    return response.status === 204 ? null : await response.json();
}

/**
 * Helper to format Postman Url object to string
 */
function formatUrl(urlObj) {
    if (!urlObj) return '';
    if (typeof urlObj === 'string') return urlObj;

    let urlStr = '';
    if (urlObj.protocol) {
        urlStr += urlObj.protocol + '://';
    }
    if (urlObj.host) {
        const host = Array.isArray(urlObj.host) ? urlObj.host.join('.') : urlObj.host;
        urlStr += host;
    }
    if (urlObj.port) {
        urlStr += ':' + urlObj.port;
    }
    if (urlObj.path) {
        const path = Array.isArray(urlObj.path) ? '/' + urlObj.path.join('/') : urlObj.path;
        urlStr += path;
    }
    return urlStr || JSON.stringify(urlObj);
}

function extractVariables(str) {
    if (!str || typeof str !== 'string') return [];
    const matches = str.match(/\{\{([^}]+)\}\}/g) || [];
    return matches.map(m => m.replace(/[\{\}]/g, ''));
}

function scanObjectForVariables(obj, vars = new Set()) {
    if (!obj) return vars;
    if (typeof obj === 'string') {
        extractVariables(obj).forEach(v => vars.add(v));
    } else if (Array.isArray(obj)) {
        obj.forEach(item => scanObjectForVariables(item, vars));
    } else if (typeof obj === 'object') {
        for (const key in obj) {
            scanObjectForVariables(obj[key], vars);
        }
    }
    return vars;
}

const VAR_DESCRIPTIONS = {
    'testUserId': 'Test user account has been successfully created (variable {{testUserId}} is set).',
    'patientId': 'Test patient has been successfully registered (variable {{patientId}} is set).',
    'patientCode': 'Test patient code identifier has been initialized (variable {{patientCode}} is set).',
    'symptomRecordId': 'Patient symptom record has been captured and stored (variable {{symptomRecordId}} is set).',
    'dicomId': 'Medical DICOM image has been successfully uploaded to the system (variable {{dicomId}} is set).',
    'dicomFileName': 'DICOM file name has been determined and stored (variable {{dicomFileName}} is set).'
};

async function run() {
    try {
        console.log(` Verifying connection to Jira project "${JIRA_PROJECT_KEY}"...`);
        try {
            const projectInfo = await callJira(`/project/${JIRA_PROJECT_KEY}`);
            console.log(`✅ Successfully connected to Jira Project: ${projectInfo.name} (${projectInfo.key})`);
        } catch (err) {
            console.error(` Project verification failed: ${err.message}`);
        }

        // Scan collection for pre-condition dependencies
        const collectionPath = path.join(__dirname, '..', 'tests', 'aidims_collection.json');
        const requestPreconditions = {};
        if (fs.existsSync(collectionPath)) {
            try {
                const collection = JSON.parse(fs.readFileSync(collectionPath, 'utf8'));
                function processItems(items) {
                    for (const item of items) {
                        if (item.request) {
                            const name = item.name;
                            const vars = new Set();
                            scanObjectForVariables(item.request, vars);
                            vars.delete('base_url'); // base_url is system-level and handled separately
                            requestPreconditions[name] = Array.from(vars);
                        } else if (item.item) {
                            processItems(item.item);
                        }
                    }
                }
                processItems(collection.item);
            } catch (e) {
                console.warn('⚠️ Could not parse Postman collection for dependencies:', e.message);
            }
        }

        console.log('🔍 Checking unit test statuses and Newman test report...');

        let failures = [];
        let failedCount = 0;
        let hasNewmanReport = false;

        if (fs.existsSync(REPORT_PATH)) {
            try {
                const report = JSON.parse(fs.readFileSync(REPORT_PATH, 'utf8'));
                failures = report.run.failures || [];
                failedCount = report.run.stats?.assertions?.failed || 0;
                hasNewmanReport = true;
                console.log(` Stats: Total Newman assertions failed: ${failedCount}. Executed failures length: ${failures.length}.`);
            } catch (e) {
                console.error(`⚠️ Error reading/parsing Newman report: ${e.message}`);
            }
        } else {
            console.log('ℹ️ Newman report file not found. Skipping API test check.');
        }

        // 1. Fetch all active bugs from Jira once using standard /search/jql endpoint and filter by [Bug-CI] in JavaScript
        console.log(' Fetching active bugs from Jira...');
        const searchJql = `project = "${JIRA_PROJECT_KEY}" AND status != "Done" AND status != "Resolved"`;
        const searchResult = await callJira(`/search/jql?jql=${encodeURIComponent(searchJql)}&maxResults=100&fields=summary,status`);
        const allActiveBugs = searchResult.issues || [];

        // Filter bugs starting with [Bug-CI] in JavaScript to avoid JQL tokenization/lucene matching limitations
        const activeBugs = allActiveBugs.filter(bug =>
            bug && bug.fields && bug.fields.summary && bug.fields.summary.startsWith('[Bug-CI]')
        );
        console.log(` Found ${activeBugs.length} active CI-triggered bugs in Jira.`);

        // Build active bug map: summary -> bug object
        const activeBugMap = {};
        for (const bug of activeBugs) {
            activeBugMap[bug.fields.summary] = bug;
        }

        const currentFailedSummaries = new Set();
        const failedRequests = {};

        const BACKEND_UT_STATUS = process.env.BACKEND_UT_STATUS;
        const FRONTEND_UT_STATUS = process.env.FRONTEND_UT_STATUS;
        const BACKEND_UT_DETAILS = process.env.BACKEND_UT_DETAILS;
        const FRONTEND_UT_DETAILS = process.env.FRONTEND_UT_DETAILS;

        let backendFailures = [];
        try {
            if (BACKEND_UT_DETAILS) {
                backendFailures = JSON.parse(BACKEND_UT_DETAILS);
            }
        } catch (e) {
            if (BACKEND_UT_DETAILS) {
                backendFailures = [{ testCase: 'TC-UT-BACKEND', errMsg: BACKEND_UT_DETAILS }];
            }
        }

        let frontendFailures = [];
        try {
            if (FRONTEND_UT_DETAILS) {
                frontendFailures = JSON.parse(FRONTEND_UT_DETAILS);
            }
        } catch (e) {
            if (FRONTEND_UT_DETAILS) {
                frontendFailures = [{ testCase: 'TC-UT-FRONTEND', errMsg: FRONTEND_UT_DETAILS }];
            }
        }

        const backendTestCaseIds = backendFailures.map(f => f.testCase).join(', ') || 'TC-UT-BACKEND';
        const backendPassOrFailText = 'Fail\n\n' + (backendFailures.length > 0
            ? backendFailures.map((f, idx) => `Test #${idx + 1}: ${f.testCase}\nMessage: ${f.errMsg}`).join('\n\n')
            : 'Backend Unit Tests failed. Please check the local console output or build logs.');

        const frontendTestCaseIds = frontendFailures.map(f => f.testCase).join(', ') || 'TC-UT-FRONTEND';
        const frontendPassOrFailText = 'Fail\n\n' + (frontendFailures.length > 0
            ? frontendFailures.map((f, idx) => `Test #${idx + 1}: ${f.testCase}\nMessage:\n${f.errMsg}`).join('\n\n')
            : 'Frontend Unit Tests failed. Please check the local console output or build logs.');

        console.log(` Unit Test Statuses - Backend: ${BACKEND_UT_STATUS || 'N/A'}, Frontend: ${FRONTEND_UT_STATUS || 'N/A'}`);

        // Sync Backend Unit Test Failure
        if (BACKEND_UT_STATUS === 'failed') {
            for (const fail of backendFailures) {
                const testCaseName = fail.testCase || 'TC-UT-BACKEND';
                const summary = `[Bug-CI] Failure on Backend Unit Test: ${testCaseName}`;
                currentFailedSummaries.add(summary);

                if (!activeBugMap[summary]) {
                    const passOrFailText = `Fail\n\nActual Failure Details:\nMessage: ${fail.errMsg}`;
                    const issueBody = {
                        fields: {
                            project: { key: JIRA_PROJECT_KEY },
                            summary: summary,
                            description: {
                                type: 'doc',
                                version: 1,
                                content: [
                                    {
                                        type: 'paragraph',
                                        content: [
                                            {
                                                type: 'text',
                                                text: 'Test case ID*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: testCaseName + '\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test summary/Description*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `Automated unit test failure detected in CI/CD pipeline for Backend (aidims-backend) - ${testCaseName}.\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pre-condition\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Backend project compiled and built successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test steps*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `1. Run Maven test command in the backend directory: mvn test (or mvnw.cmd test).\n2. Verify the unit test ${testCaseName} executes and passes successfully.\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Inputs (test data)\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `- Build Tool: Maven\n- Command: mvn test\n- Test Case: ${testCaseName}\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Expected result*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `The unit test ${testCaseName} should pass successfully.\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pass/Fail\n',
                                                marks: [{ type: 'strong' }, { type: 'textColor', attrs: { color: '#DE350B' } }]
                                            },
                                            {
                                                type: 'text',
                                                text: passOrFailText
                                            }
                                        ]
                                    }
                                ]
                            },
                            issuetype: { name: 'Bug' }
                        }
                    };
                    console.log(` Creating new Jira bug: "${summary}"`);
                    try {
                        const newIssue = await callJira('/issue', {
                            method: 'POST',
                            body: JSON.stringify(issueBody)
                        });
                        console.log(` Issue created successfully: ${newIssue.key}`);
                    } catch (err) {
                        console.error(`❌ Error creating Backend UT bug on Jira: ${err.message}`);
                    }
                } else {
                    console.log(`ℹ️ Issue already exists for "${summary}". Skipping creation.`);
                }
            }

            if (backendFailures.length === 0) {
                const summary = '[Bug-CI] Failure on Backend Unit Tests';
                currentFailedSummaries.add(summary);

                if (!activeBugMap[summary]) {
                    const issueBody = {
                        fields: {
                            project: { key: JIRA_PROJECT_KEY },
                            summary: summary,
                            description: {
                                type: 'doc',
                                version: 1,
                                content: [
                                    {
                                        type: 'paragraph',
                                        content: [
                                            {
                                                type: 'text',
                                                text: 'Test case ID*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'TC-UT-BACKEND\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test summary/Description*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Automated unit test failure detected in CI/CD pipeline for Backend (aidims-backend).\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pre-condition\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Backend project compiled and built successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test steps*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: '1. Run Maven test command in the backend directory: mvn test (or mvnw.cmd test).\n2. Verify all unit tests execute and pass successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Inputs (test data)\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: '- Build Tool: Maven\n- Command: mvn test\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Expected result*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'All backend unit tests should pass successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pass/Fail\n',
                                                marks: [{ type: 'strong' }, { type: 'textColor', attrs: { color: '#DE350B' } }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Fail\n\nBackend Unit Tests failed. Please check the local console output or build logs.'
                                            }
                                        ]
                                    }
                                ]
                            },
                            issuetype: { name: 'Bug' }
                        }
                    };
                    console.log(` Creating new Jira bug: "${summary}"`);
                    try {
                        const newIssue = await callJira('/issue', {
                            method: 'POST',
                            body: JSON.stringify(issueBody)
                        });
                        console.log(` Issue created successfully: ${newIssue.key}`);
                    } catch (err) {
                        console.error(`❌ Error creating Backend UT bug on Jira: ${err.message}`);
                    }
                } else {
                    console.log(`ℹ️ Issue already exists for "${summary}". Skipping creation.`);
                }
            }
        }

        // Sync Frontend Unit Test Failure
        if (FRONTEND_UT_STATUS === 'failed') {
            for (const fail of frontendFailures) {
                const testCaseName = fail.testCase || 'TC-UT-FRONTEND';
                const summary = `[Bug-CI] Failure on Frontend Unit Test: ${testCaseName}`;
                currentFailedSummaries.add(summary);

                if (!activeBugMap[summary]) {
                    const passOrFailText = `Fail\n\nActual Failure Details:\nMessage:\n${fail.errMsg}`;
                    const issueBody = {
                        fields: {
                            project: { key: JIRA_PROJECT_KEY },
                            summary: summary,
                            description: {
                                type: 'doc',
                                version: 1,
                                content: [
                                    {
                                        type: 'paragraph',
                                        content: [
                                            {
                                                type: 'text',
                                                text: 'Test case ID*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: testCaseName + '\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test summary/Description*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `Automated unit test failure detected in CI/CD pipeline for Frontend (aidims-frontend) - ${testCaseName}.\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pre-condition\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Frontend dependencies are installed successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test steps*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `1. Run npm test command in the frontend directory: npm test -- --watchAll=false.\n2. Verify the unit test ${testCaseName} executes and passes successfully.\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Inputs (test data)\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `- Build Tool: npm\n- Command: npm test -- --watchAll=false\n- Test Case: ${testCaseName}\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Expected result*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: `The unit test ${testCaseName} should pass successfully.\n\n`
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pass/Fail\n',
                                                marks: [{ type: 'strong' }, { type: 'textColor', attrs: { color: '#DE350B' } }]
                                            },
                                            {
                                                type: 'text',
                                                text: passOrFailText
                                            }
                                        ]
                                    }
                                ]
                            },
                            issuetype: { name: 'Bug' }
                        }
                    };
                    console.log(` Creating new Jira bug: "${summary}"`);
                    try {
                        const newIssue = await callJira('/issue', {
                            method: 'POST',
                            body: JSON.stringify(issueBody)
                        });
                        console.log(` Issue created successfully: ${newIssue.key}`);
                    } catch (err) {
                        console.error(`❌ Error creating Frontend UT bug on Jira: ${err.message}`);
                    }
                } else {
                    console.log(`ℹ️ Issue already exists for "${summary}". Skipping creation.`);
                }
            }

            if (frontendFailures.length === 0) {
                const summary = '[Bug-CI] Failure on Frontend Unit Tests';
                currentFailedSummaries.add(summary);

                if (!activeBugMap[summary]) {
                    const issueBody = {
                        fields: {
                            project: { key: JIRA_PROJECT_KEY },
                            summary: summary,
                            description: {
                                type: 'doc',
                                version: 1,
                                content: [
                                    {
                                        type: 'paragraph',
                                        content: [
                                            {
                                                type: 'text',
                                                text: 'Test case ID*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'TC-UT-FRONTEND\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test summary/Description*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Automated unit test failure detected in CI/CD pipeline for Frontend (aidims-frontend).\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pre-condition\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Frontend dependencies are installed successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Test steps*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: '1. Run npm test command in the frontend directory: npm test -- --watchAll=false.\n2. Verify all unit tests execute and pass successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Inputs (test data)\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: '- Build Tool: npm\n- Command: npm test -- --watchAll=false\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Expected result*\n',
                                                marks: [{ type: 'strong' }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'All frontend unit tests should pass successfully.\n\n'
                                            },
                                            {
                                                type: 'text',
                                                text: 'Pass/Fail\n',
                                                marks: [{ type: 'strong' }, { type: 'textColor', attrs: { color: '#DE350B' } }]
                                            },
                                            {
                                                type: 'text',
                                                text: 'Fail\n\nFrontend Unit Tests failed. Please check the local console output or build logs.'
                                            }
                                        ]
                                    }
                                ]
                            },
                            issuetype: { name: 'Bug' }
                        }
                    };
                    console.log(` Creating new Jira bug: "${summary}"`);
                    try {
                        const newIssue = await callJira('/issue', {
                            method: 'POST',
                            body: JSON.stringify(issueBody)
                        });
                        console.log(` Issue created successfully: ${newIssue.key}`);
                    } catch (err) {
                        console.error(`❌ Error creating Frontend UT bug on Jira: ${err.message}`);
                    }
                } else {
                    console.log(`ℹ️ Issue already exists for "${summary}". Skipping creation.`);
                }
            }
        }

        // Sync API Test Failures
        if (hasNewmanReport && failedCount > 0) {
            console.log(' Failed assertions detected! Syncing errors to Jira...');

            // Group failures by request
            for (const fail of failures) {
                const requestName = fail.source.name || 'Unknown API Request';
                if (!failedRequests[requestName]) {
                    failedRequests[requestName] = [];
                }
                failedRequests[requestName].push(fail);
            }

            for (const [requestName, requestFailures] of Object.entries(failedRequests)) {
                const sampleFail = requestFailures[0];
                const requestMethod = sampleFail.source.request.method || 'GET';
                const summary = `[Bug-CI] Failure on API: ${requestMethod} - ${requestName}`;
                currentFailedSummaries.add(summary);

                const requestUrl = sampleFail.source.request.url.raw ||
                    (typeof sampleFail.source.request.url === 'string' ? sampleFail.source.request.url : formatUrl(sampleFail.source.request.url)) ||
                    '';

                // Check if bug already exists in our active bug map (bypassing JQL full-text matching limitations)
                if (activeBugMap[summary]) {
                    console.log(`ℹ️ Issue already exists for "${summary}". Skipping creation.`);
                    continue;
                }

                // Format details of all failures for this request
                const failureDetails = requestFailures.map((f, index) => {
                    const testcase = f.error.name || 'Assertion Error';
                    const message = f.error.message || 'Details not available';
                    return `Test #${index + 1}: ${testcase}\nMessage: ${message}\n`;
                }).join('\n');

                // Resolve variables in requestUrl using tests/environment.json if present
                let resolvedUrl = requestUrl;
                let baseUrl = 'http://localhost:8080';
                const envJsonPath = path.join(__dirname, '..', 'tests', 'environment.json');
                if (fs.existsSync(envJsonPath)) {
                    try {
                        const envData = JSON.parse(fs.readFileSync(envJsonPath, 'utf8'));
                        const baseUrlObj = envData.values.find(v => v.key === 'base_url');
                        if (baseUrlObj && baseUrlObj.value) {
                            baseUrl = baseUrlObj.value;
                            resolvedUrl = resolvedUrl.replace(/\{\{base_url\}\}/g, baseUrl);
                        }
                    } catch (e) {
                        // ignore
                    }
                }

                // Get dynamic pre-conditions from collection scanning
                const deps = requestPreconditions[requestName] || [];
                let preConditionText = `Backend server is running at ${baseUrl} and connected to the database.`;
                if (deps.length > 0) {
                    preConditionText += `\n- Required data pre-conditions:`;
                    deps.forEach(d => {
                        const desc = VAR_DESCRIPTIONS[d] || `Required environment variable \`{{${d}}}\` must be set from previous test steps.`;
                        preConditionText += `\n  * ${desc}`;
                    });
                }

                // Create issue body in Atlassian Document Format (ADF)
                const issueBody = {
                    fields: {
                        project: { key: JIRA_PROJECT_KEY },
                        summary: summary,
                        description: {
                            type: 'doc',
                            version: 1,
                            content: [
                                {
                                    type: 'paragraph',
                                    content: [
                                        {
                                            type: 'text',
                                            text: 'Test summary/Description*\n',
                                            marks: [{ type: 'strong' }]
                                        },
                                        {
                                            type: 'text',
                                            text: `Automated API test failure detected in CI/CD pipeline for API: ${requestMethod} - ${requestName}\n\n`
                                        },
                                        {
                                            type: 'text',
                                            text: 'Pre-condition\n',
                                            marks: [{ type: 'strong' }]
                                        },
                                        {
                                            type: 'text',
                                            text: `${preConditionText}\n\n`
                                        },
                                        {
                                            type: 'text',
                                            text: 'Test steps*\n',
                                            marks: [{ type: 'strong' }]
                                        },
                                        {
                                            type: 'text',
                                            text: `1. Send a ${requestMethod} request to the endpoint: ${resolvedUrl}\n2. Verify the response status code and data structures match the defined assertions.\n\n`
                                        },
                                        {
                                            type: 'text',
                                            text: 'Inputs (test data)\n',
                                            marks: [{ type: 'strong' }]
                                        },
                                        {
                                            type: 'text',
                                            text: `- Method: ${requestMethod}\n- URL: ${resolvedUrl}\n\n`
                                        },
                                        {
                                            type: 'text',
                                            text: 'Expected result*\n',
                                            marks: [{ type: 'strong' }]
                                        },
                                        {
                                            type: 'text',
                                            text: `The request should return successful response (status code 200/201) from ${baseUrl} with valid response data.\n\n`
                                        },
                                        {
                                            type: 'text',
                                            text: 'Actual Failure Details:\n',
                                            marks: [{ type: 'strong' }, { type: 'textColor', attrs: { color: '#DE350B' } }]
                                        },
                                        {
                                            type: 'text',
                                            text: failureDetails
                                        }
                                    ]
                                }
                            ]
                        },
                        issuetype: { name: 'Bug' }
                    }
                };

                console.log(` Creating new Jira bug: "${summary}"`);
                try {
                    const newIssue = await callJira('/issue', {
                        method: 'POST',
                        body: JSON.stringify(issueBody)
                    });
                    console.log(` Issue created successfully: ${newIssue.key}`);
                } catch (err) {
                    console.error(`❌ Error creating API bug on Jira: ${err.message}`);
                }
            }
        } else if (hasNewmanReport) {
            console.log(' All API tests passed!');
        }

        // 2. Check and close resolved bugs
        console.log('🔍 Checking if we need to resolve any previously logged Bugs on Jira...');
        if (activeBugs.length > 0) {
            for (const issue of activeBugs) {
                if (issue && issue.fields && issue.fields.summary) {
                    const bugSummary = issue.fields.summary;

                    let shouldResolve = false;
                    let resolveMessage = '';

                    if (bugSummary.startsWith('[Bug-CI] Failure on API:')) {
                        if (hasNewmanReport && !currentFailedSummaries.has(bugSummary)) {
                            shouldResolve = true;
                            resolveMessage = '✅ CI/CD update: This specific API test has successfully passed in the latest run. This automated issue has been resolved.';
                        }
                    } else if (bugSummary.startsWith('[Bug-CI] Failure on Backend Unit Test:')) {
                        const testCaseName = bugSummary.replace('[Bug-CI] Failure on Backend Unit Test: ', '').trim();
                        const isStillFailing = BACKEND_UT_STATUS === 'failed' && backendFailures.some(f => f.testCase === testCaseName);
                        if (!isStillFailing) {
                            shouldResolve = true;
                            resolveMessage = `✅ CI/CD update: Backend unit test "${testCaseName}" has successfully passed in the latest run. This automated issue has been resolved.`;
                        }
                    } else if (bugSummary === '[Bug-CI] Failure on Backend Unit Tests') {
                        if (BACKEND_UT_STATUS === 'passed') {
                            shouldResolve = true;
                            resolveMessage = '✅ CI/CD update: Backend unit tests have successfully passed in the latest run. This automated issue has been resolved.';
                        }
                    } else if (bugSummary.startsWith('[Bug-CI] Failure on Frontend Unit Test:')) {
                        const testCaseName = bugSummary.replace('[Bug-CI] Failure on Frontend Unit Test: ', '').trim();
                        const isStillFailing = FRONTEND_UT_STATUS === 'failed' && frontendFailures.some(f => f.testCase === testCaseName);
                        if (!isStillFailing) {
                            shouldResolve = true;
                            resolveMessage = `✅ CI/CD update: Frontend unit test "${testCaseName}" has successfully passed in the latest run. This automated issue has been resolved.`;
                        }
                    } else if (bugSummary === '[Bug-CI] Failure on Frontend Unit Tests') {
                        if (FRONTEND_UT_STATUS === 'passed') {
                            shouldResolve = true;
                            resolveMessage = '✅ CI/CD update: Frontend unit tests have successfully passed in the latest run. This automated issue has been resolved.';
                        }
                    }

                    if (shouldResolve) {
                        console.log(`🔧 Resolving ${issue.key} ("${bugSummary}") as it passed in this run...`);

                        // Fetch available transitions for this issue
                        const transitionsData = await callJira(`/issue/${issue.key}/transitions`);
                        const doneTransition = transitionsData.transitions.find(t =>
                            t.name.toLowerCase() === 'done' || t.name.toLowerCase() === 'resolved' || t.name.toLowerCase() === 'close' || t.name.toLowerCase() === 'closed'
                        );

                        if (doneTransition) {
                            // Apply transition
                            await callJira(`/issue/${issue.key}/transitions`, {
                                method: 'POST',
                                body: JSON.stringify({
                                    transition: { id: doneTransition.id }
                                })
                            });

                            // Add a resolution comment
                            const commentBody = {
                                body: {
                                    type: 'doc',
                                    version: 1,
                                    content: [{
                                        type: 'paragraph',
                                        content: [{
                                            type: 'text',
                                            text: resolveMessage
                                        }]
                                    }]
                                }
                            };
                            await callJira(`/issue/${issue.key}/comment`, {
                                method: 'POST',
                                body: JSON.stringify(commentBody)
                            });

                            console.log(` Issue ${issue.key} has been resolved and closed.`);
                        } else {
                            console.warn(` Warning: Could not find a suitable 'Done/Resolved' transition for issue ${issue.key}`);
                        }
                    } else {
                        console.log(` Keeping active bug ${issue.key} ("${bugSummary}") open.`);
                    }
                }
            }
        } else {
            console.log('✅ No active CI bugs found on Jira. Clean run.');
        }
    } catch (error) {
        console.error('❌ Automation Script Failed:', error.message);
        process.exit(1);
    }
}

run();
