const fs = require('fs');
const path = require('path');

// 定义大类到小类的映射
const deathReasonMap = {
  '疾病': [
    '疾病-细菌性烂鳃病',
    '疾病-肠炎病',
    '疾病-水霉病',
    '疾病-车轮虫感染',
    '疾病-指环虫感染'
  ],
  '操作损伤': [
    '操作损伤-捕捞应激',
    '操作损伤-运输损伤',
    '操作损伤-分塘损伤'
  ],
  '水质异常': [
    '水质异常-溶氧不足',
    '水质异常-pH值偏离',
    '水质异常-氨氮超标',
    '水质异常-亚硝酸盐超标'
  ],
  '饲料问题': [
    '饲料问题-霉变饲料',
    '饲料问题-营养不良',
    '饲料问题-投喂过量'
  ],
  '密度过高': [
    '密度过高-缺氧窒息',
    '密度过高-争食受伤'
  ],
  // 将旧的格式也映射到水质异常类
  'pH值异常': [
    '水质异常-溶氧不足',
    '水质异常-pH值偏离',
    '水质异常-氨氮超标',
    '水质异常-亚硝酸盐超标'
  ]
};

// 随机选择函数
function getRandomItem(array) {
  return array[Math.floor(Math.random() * array.length)];
}

// 读取JSON文件
const filePath = path.join(__dirname, 'jy_survival_data.json');
const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));

console.log(`总共 ${data.length} 条数据`);

// 统计替换情况
let replaceCount = 0;
const categoryStats = {};

// 遍历并替换死亡原因
data.forEach((item, index) => {
  const oldReason = item['死亡原因'];
  
  // 如果死亡原因是大类，则替换为随机小类
  if (deathReasonMap[oldReason]) {
    const newReason = getRandomItem(deathReasonMap[oldReason]);
    item['死亡原因'] = newReason;
    replaceCount++;
    
    // 统计
    if (!categoryStats[oldReason]) {
      categoryStats[oldReason] = 0;
    }
    categoryStats[oldReason]++;
    
    if (index < 5) {
      console.log(`[${index + 1}] ${oldReason} → ${newReason}`);
    }
  }
});

// 写回文件
fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf8');

console.log(`\n✅ 替换完成！`);
console.log(`总替换数量: ${replaceCount} 条`);
console.log(`\n各大类替换统计:`);
Object.entries(categoryStats).forEach(([category, count]) => {
  console.log(`  ${category}: ${count} 条`);
});
