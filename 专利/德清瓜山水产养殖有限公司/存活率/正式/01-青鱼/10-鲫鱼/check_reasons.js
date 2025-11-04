const fs = require('fs');
const data = JSON.parse(fs.readFileSync('./jy_survival_data.json', 'utf8'));

const reasons = {};
data.forEach(item => {
  const r = item['死亡原因'];
  reasons[r] = (reasons[r] || 0) + 1;
});

console.log('死亡原因统计:');
Object.entries(reasons).sort((a,b) => b[1] - a[1]).forEach(([k,v]) => {
  console.log(`  ${k}: ${v}条`);
});
