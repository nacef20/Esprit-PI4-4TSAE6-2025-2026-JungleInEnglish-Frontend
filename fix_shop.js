const fs = require('fs');
const path = require('path');

const ROOT = 'src/app/shop';

function processDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            processDir(fullPath);
        } else if (fullPath.endsWith('.ts')) {
            let content = fs.readFileSync(fullPath, 'utf8');
            let originalContent = content;

            // 1. Fix the double-quote bug from previous runs
            content = content.replace(/@shop\/(shared|services|constants)\/'/g, "@shop/$1/");
            
            // 2. Redirect AuthService to main app's AuthService
            content = content.replace(/from\s+['"].*auth\.service['"]/g, "from 'src/app/auth/auth.service'");

            // 3. Redirect environments to main app's environments
            content = content.replace(/from\s+['"].*environments\/environment['"]/g, "from '@env/environment'");

            // 4. Ensure all relative shop-internal imports use @shop (fixing missed ones)
            content = content.replace(/from\s+['"]\.\.\/+(\.\.\/)*(shared|core\/services|core\/constants)\/(.*?)['"]/g, (match, p1, p2, p3) => {
                let alias = p2 === 'shared' ? '@shop/shared/' : (p2 === 'core/services' ? '@shop/services/' : '@shop/constants/');
                return `from '${alias}${p3}'`;
            });

            if (content !== originalContent) {
                fs.writeFileSync(fullPath, content);
                console.log(`Cleaned & Fixed: ${fullPath}`);
            }
        }
    }
}

processDir(ROOT);
console.log('Cleanup and Fix complete.');
