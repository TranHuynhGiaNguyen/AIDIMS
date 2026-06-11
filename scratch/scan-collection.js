const fs = require('fs');
const path = require('path');

const collectionPath = path.join(__dirname, '..', 'tests', 'aidims_collection.json');

if (!fs.existsSync(collectionPath)) {
    console.error(`Collection not found at: ${collectionPath}`);
    process.exit(1);
}

const collection = JSON.parse(fs.readFileSync(collectionPath, 'utf8'));

// List to store all request analyses
const requests = [];

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

function scanTestScriptForSets(scriptLines) {
    const sets = new Set();
    if (!scriptLines) return sets;
    const code = Array.isArray(scriptLines) ? scriptLines.join('\n') : scriptLines;
    
    // Regex matches pm.environment.set("name", ...) or pm.globals.set("name", ...)
    const envSetRegex = /pm\.(environment|globals|variables)\.set\(\s*["']([^"']+)["']/g;
    let match;
    while ((match = envSetRegex.exec(code)) !== null) {
        sets.add(match[2]);
    }
    return sets;
}

function processItems(items) {
    for (const item of items) {
        if (item.request) {
            // It's a request
            const name = item.name;
            const method = item.request.method || 'GET';
            const urlRaw = item.request.url ? (item.request.url.raw || '') : '';
            
            // Extract dependency variables (pre-conditions)
            const dependencyVars = new Set();
            scanObjectForVariables(item.request, dependencyVars);
            
            // Extract outputs (set variables)
            const outputVars = new Set();
            if (item.event) {
                for (const ev of item.event) {
                    if (ev.listen === 'test' && ev.script && ev.script.exec) {
                        const testSets = scanTestScriptForSets(ev.script.exec);
                        testSets.forEach(v => outputVars.add(v));
                    }
                }
            }
            
            requests.push({
                name,
                method,
                url: urlRaw,
                dependencies: Array.from(dependencyVars),
                outputs: Array.from(outputVars)
            });
        } else if (item.item) {
            // It's a folder, traverse recursively
            processItems(item.item);
        }
    }
}

processItems(collection.item);

// Print the report
console.log('=== POSTMAN COLLECTION DEPENDENCY ANALYSIS (PRE-CONDITIONS) ===\n');
requests.forEach((r, idx) => {
    console.log(`${idx + 1}. ${r.name}`);
    console.log(`   Endpoint: [${r.method}] ${r.url}`);
    if (r.dependencies.length > 0) {
        console.log(`    Tiền điều kiện (Biến phụ thuộc - Inputs): ${r.dependencies.map(d => `{{${d}}}`).join(', ')}`);
    } else {
        console.log(`    Tiền điều kiện: Không có biến phụ thuộc (Chỉ yêu cầu server chạy)`);
    }
    if (r.outputs.length > 0) {
        console.log(`    Sinh biến đầu ra (Outputs): ${r.outputs.map(o => `{{${o}}}`).join(', ')}`);
    }
    console.log('');
});
