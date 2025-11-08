const fs = require('fs');
const path = require('path');

const allowedExtensions = ['.java', '.xml'];

// Generar árbol de archivos dentro de src/
function generateTree(dir, prefix = '') {
  if (!fs.existsSync(dir)) return '';
  const entries = fs.readdirSync(dir);
  let tree = '';

  entries.forEach((entry, index) => {
    const fullPath = path.join(dir, entry);
    const isLast = index === entries.length - 1;
    const connector = isLast ? '┗' : '┣';
    const subPrefix = prefix + (isLast ? '  ' : '┃ ');

    tree += `${prefix}${connector} ${entry}\n`;

    if (fs.statSync(fullPath).isDirectory()) {
      tree += generateTree(fullPath, subPrefix);
    }
  });

  return tree;
}

// Recorrer archivos válidos en src/
function walk(dir, fileList = []) {
  if (!fs.existsSync(dir)) return fileList;

  fs.readdirSync(dir).forEach(file => {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);

    if (stat.isDirectory()) {
      walk(fullPath, fileList);
    } else {
      const ext = path.extname(fullPath);
      if (allowedExtensions.includes(ext)) {
        const content = fs.readFileSync(fullPath, 'utf8');
        const lang = ext.slice(1);
        fileList.push(`## ${fullPath}\n\n\`\`\`${lang}\n${content}\n\`\`\`\n`);
      }
    }
  });

  return fileList;
}

// Usar rutas relativas correctas (subir un nivel desde project-dumper)
const srcPath = path.join(__dirname, '../src');
const pomPath = path.join(__dirname, '../pom.xml');

const tree = generateTree(srcPath);
const files = walk(srcPath);

// Agregar pom.xml si existe
if (fs.existsSync(pomPath)) {
  const pomContent = fs.readFileSync(pomPath, 'utf8');
  files.unshift(`## pom.xml\n\n\`\`\`xml\n${pomContent}\n\`\`\`\n`);
}

const markdown = `# 📁 Project Structure (src only)\n\n\`\`\`\n${tree}\`\`\`\n\n# 📄 File Contents (.java & pom.xml)\n\n${files.join('\n')}`;

fs.writeFileSync(path.join(__dirname, 'project_dump.md'), markdown);
console.log('✅ Markdown generado con archivos .java y pom.xml: project_dump.md');
