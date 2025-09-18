/*
校验并修复 zhb_survival_data.json：
- 复算三项：基础存活率（%）、环境修正因子、综合存活率（%）
- 规范化数值：去除字符串中的无关符号（如"<"">""%"），转为数值
- 容差：基础/因子/综合 0.01（综合放宽到0.02）
- 用法：
  node validate_and_fix_zhb.js         # 只校验并输出 fixed 文件 zhb_survival_data.fixed.json
  node validate_and_fix_zhb.js --fix   # 校验并覆盖原文件 zhb_survival_data.json
*/

const fs = require('fs');
const path = require('path');

const SRC = path.resolve('./zhb_survival_data.json');
const OUT = path.resolve('./zhb_survival_data.fixed.json');
const DO_OPT = 6.0;      // 溶氧（mg/L）
const TEMP_OPT = 24.0;   // 水温（℃）
const PH_OPT = 7.0;      // pH
const NH3_OPT = 0.1;     // 氨氮（mg/L）
const W = { do: 0.35, temp: 0.30, ph: 0.20, nh3: 0.15 };

function round2(n) { return Math.round(n * 100) / 100; }
function asNum(v) {
  if (typeof v === 'number') return v;
  if (v == null) return NaN;
  // 去空格与常见符号
  const s = String(v).trim().replace(/[%]/g, '').replace(/[<>≈~]/g, '');
  const n = Number(s);
  return Number.isFinite(n) ? n : NaN;
}

function calcEnvFactor(doVal, tempVal, phVal, nh3Val) {
  const delta = W.do * Math.abs(doVal - DO_OPT)
              + W.temp * Math.abs(tempVal - TEMP_OPT)
              + W.ph * Math.abs(ph - PH_OPT)
              + W.nh3 * Math.abs(nh3Val - NH3_OPT);
  return 1 - delta;
}

function validateAndFixRow(r) {
  // 读字段并规范化
  const stock = asNum(r['投放数量（尾）']);
  const survive = asNum(r['存活数量（尾）']);
  const doVal = asNum(r['溶氧（mg/L）']);
  const tempVal = asNum(r['水温（℃）']);
  const ph = asNum(r['pH']);
  const nh3Val = asNum(r['氨氮（mg/L）']);

  const base = round2((survive / stock) * 100);
  const delta = W.do * Math.abs(doVal - DO_OPT)
               + W.temp * Math.abs(tempVal - TEMP_OPT)
               + W.ph * Math.abs(ph - PH_OPT)
               + W.nh3 * Math.abs(nh3Val - NH3_OPT);
  const factor = round2(1 - delta);
  const composite = round2(base * factor);

  const baseOK = Math.abs(base - asNum(r['基础存活率（%）'])) <= 0.01;
  const facOK = Math.abs(factor - asNum(r['环境修正因子'])) <= 0.01;
  const compOK = Math.abs(composite - asNum(r['综合存活率（%）'])) <= 0.02;

  const fixed = { ...r };
  fixed['基础存活率（%）'] = base;
  fixed['环境修正因子'] = factor;
  fixed['综合存活率（%）'] = composite;
  fixed['溶氧（mg/L）'] = doVal;
  fixed['水温（℃）'] = tempVal;
  fixed['pH'] = ph;
  fixed['氨氮（mg/L）'] = nh3Val;

  return { baseOK, facOK, compOK, fixed };
}

function main() {
  if (!fs.existsSync(SRC)) {
    console.error('未找到 zhb_survival_data.json');
    process.exit(1);
  }
  const arr = JSON.parse(fs.readFileSync(SRC, 'utf-8'));
  if (!Array.isArray(arr)) {
    console.error('JSON格式错误，应为数组');
    process.exit(1);
  }

  let passBase = 0, passFac = 0, passComp = 0;
  const out = [];
  for (const r of arr) {
    const { baseOK, facOK, compOK, fixed } = validateAndFixRow(r);
    if (baseOK) passBase++;
    if (facOK) passFac++;
    if (compOK) passComp++;
    out.push(fixed);
  }

  console.log(`基础存活率一致：${passBase}/${arr.length}`);
  console.log(`环境修正因子一致：${passFac}/${arr.length}`);
  console.log(`综合存活率一致：${passComp}/${arr.length}`);

  const doFix = process.argv.includes('--fix');
  if (doFix) {
    fs.writeFileSync(SRC, JSON.stringify(out, null, 2), 'utf-8');
    console.log('已覆盖写回原文件：zhb_survival_data.json');
  } else {
    fs.writeFileSync(OUT, JSON.stringify(out, null, 2), 'utf-8');
    console.log(`已输出修复后的文件：${OUT}`);
  }
}

if (require.main === module) {
  main();
}
