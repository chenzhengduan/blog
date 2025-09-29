const fs = require('fs');
const XLSX = require('xlsx');

const jsonFilePath = './yy_feeding_data.json';
const excelFilePath = './养殖鳙鱼投喂分析数据包.xlsx';

function jsonToExcel() {
  try {
    // 读取JSON文件
    const jsonData = JSON.parse(fs.readFileSync(jsonFilePath, 'utf-8'));
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new();
    
    // 将JSON数据转换为工作表
    const worksheet = XLSX.utils.json_to_sheet(jsonData);
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '鳙鱼投喂数据');
    
    // 写入Excel文件
    XLSX.writeFile(workbook, excelFilePath);
    
    console.log(`Successfully converted ${jsonFilePath} to ${excelFilePath}`);
    console.log(`Total records: ${jsonData.length}`);
  } catch (error) {
    console.error('Error converting JSON to Excel:', error.message);
  }
}

if (require.main === module) {
  jsonToExcel();
}

module.exports = { jsonToExcel };