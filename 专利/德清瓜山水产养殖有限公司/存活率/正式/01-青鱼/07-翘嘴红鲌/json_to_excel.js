/*
将 qy_survival_data.json 转换为 Excel
- 输入：当前目录 qy_survival_data.json（数组JSON，对象key为中文字段名）
- 输出：当前目录 qy_survival_data.xlsx
*/

const fs = require('fs');
const path = require('path');
const xlsx = require('xlsx');

function main() {
  const jsonPath = path.resolve('./qzhh_survival_data.json');
  if (!fs.existsSync(jsonPath)) {
    console.error('未找到 qzhh_survival_data.json，请先运行生成脚本');
    process.exit(1);
  }
  const data = JSON.parse(fs.readFileSync(jsonPath, 'utf-8'));
  if (!Array.isArray(data)) {
    console.error('JSON格式错误：应为数组');
    process.exit(1);
  }
  const wb = xlsx.utils.book_new();
  const ws = xlsx.utils.json_to_sheet(data);
  xlsx.utils.book_append_sheet(wb, ws, '青鱼存活率');
  const out = path.resolve('./养殖翘嘴红鲌存活率检测数据包.xlsx');
  xlsx.writeFile(wb, out);
  console.log(`已生成 Excel -> ${out}`);
}

if (require.main === module) {
  main();
}
