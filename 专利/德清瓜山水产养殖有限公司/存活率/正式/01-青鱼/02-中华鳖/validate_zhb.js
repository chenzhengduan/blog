// validate_zhb.js
const fs = require('fs');
const data = JSON.parse(fs.readFileSync('./zhb_survival_data.json','utf-8'));
function round2(n){ return Math.round(n*100)/100; }
let ok=0, fail=[];
for (let i=0;i<Math.min(50,data.length);i++){
  const r = data[Math.floor(Math.random()*data.length)];
  const base = round2((r['存活数量（尾）']/r['投放数量（尾）'])*100);
  const delta = 0.35*Math.abs(r['溶氧（mg/L）']-6.0)
              + 0.30*Math.abs(r['水温（℃）']-24.0)
              + 0.20*Math.abs(r['pH']-7.0)
              + 0.15*Math.abs(r['氨氮（mg/L）']-0.1);
  const factor = round2(1 - delta);
  const composite = round2(base * factor);
  const baseOK = Math.abs(base - r['基础存活率（%）']) <= 0.01;
  const facOK = Math.abs(factor - r['环境修正因子']) <= 0.01;
  const compOK = Math.abs(composite - r['综合存活率（%）']) <= 0.02;
  if (baseOK && facOK && compOK) ok++;
  else fail.push({r, base, factor, composite});
}
console.log(`校验通过 ${ok}/${Math.min(50,data.length)} 条`);
if (fail.length) console.log('示例异常：', fail[0]);