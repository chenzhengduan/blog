const fs = require('fs');
const path = require('path');

// 死亡原因映射关系
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

// 读取JSON文件
const jsonPath = path.resolve('./gy_survival_data.json');
const data = JSON.parse(fs.readFileSync(jsonPath, 'utf-8'));

// 更新死亡原因
data.forEach(record => {
  const currentReason = record['死亡原因'];
  
  // 检查是否是简化版本
  if (deathReasonMap[currentReason]) {
    // 随机选择一个详细的死亡原因
    const detailedReasons = deathReasonMap[currentReason];
    const randomReason = detailedReasons[Math.floor(Math.random() * detailedReasons.length)];
    record['死亡原因'] = randomReason;
  }
});

// 写回文件
fs.writeFileSync(jsonPath, JSON.stringify(data, null, 2), 'utf-8');
console.log('死亡原因已更新完成！');
console.log(`共处理 ${data.length} 条记录`);
