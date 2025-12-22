// 📦 文件大小格式化
// 学习点：单位转换

function formatFileSize(bytes, decimals = 2) {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i];
}

// 解析文件大小
function parseFileSize(sizeStr) {
  const units = {
    'B': 1,
    'KB': 1024,
    'MB': 1024 * 1024,
    'GB': 1024 * 1024 * 1024,
    'TB': 1024 * 1024 * 1024 * 1024
  };
  
  const match = sizeStr.match(/^([\d.]+)\s*([A-Z]+)$/i);
  if (!match) return 0;
  
  const [, num, unit] = match;
  return parseFloat(num) * (units[unit.toUpperCase()] || 1);
}

console.log('文件大小格式化:');
console.log(formatFileSize(1024));           // 1 KB
console.log(formatFileSize(1234567));        // 1.18 MB
console.log(formatFileSize(1234567890));     // 1.15 GB

console.log('\n解析文件大小:');
console.log(parseFileSize('1.5 MB'), 'bytes');
