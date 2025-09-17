/*
生成"白鲢养殖存活率综合环境分析数据"的示例数据
- 严格遵循算法：
  基础存活率 = (存活数量 / 投放数量) * 100
  环境修正因子 = 1 - (0.35*|溶氧-6.0| + 0.30*|水温-24.0| + 0.20*|pH-7.0| + 0.15*|氨氮-0.1|)
  综合存活率 = 基础存活率 * 环境修正因子
- 约束：
  * 基础存活率、综合存活率 >= 90
  * 投放数量以100为单位
  * 投放时间：2024-05 至 2025-07，每周每个鱼塘一条记录
  * 检测时间为投放时间后一周
- 输出：当前目录生成 bl_survival_data.json，数组JSON，key为中文字段名
*/

const fs = require('fs');
const code='BL';
const FISH_TYPE = '白鲢';

// 配置参数
const START_DATE = new Date('2024-05-25');
const END_DATE = new Date('2025-07-31');
// 生成上限（可按需调整），满足"500+且<600"
const MAX_RECORDS = 540;
const POND_IDS = [code+'01', code+'02', code+'03', code+'04', code+'05', code+'06', code+'07', code+'08', code+'09'];
const MANAGERS = ['EMP001', 'EMP002', 'EMP003', 'EMP004', 'EMP005'];

// 最优值与权重（严格来自文档）
const OPT = {
  do: 6.0,      // 溶氧（mg/L）
  temp: 24.0,   // 水温（℃）
  ph: 7.0,      // pH
  nh3: 0.1      // 氨氮（mg/L）
};
const WEIGHT = { do: 0.35, temp: 0.30, ph: 0.20, nh3: 0.15 };

// 为每个鱼塘引入稳定的轻微偏移，制造个体差异（保证整体仍接近最优）
// 数值幅度控制在很小范围，避免破坏≥90%的约束
const POND_PROFILES = {
  [POND_IDS[0]]: { doDelta: -0.03, tempDelta: +0.10, phDelta: -0.02, nh3Delta: +0.003, baseAdj: +0.6 },
  [POND_IDS[1]]: { doDelta: +0.02, tempDelta: -0.08, phDelta: +0.01, nh3Delta: -0.002, baseAdj: +0.2 },
  [POND_IDS[2]]: { doDelta: -0.01, tempDelta: +0.05, phDelta: +0.02, nh3Delta: +0.001, baseAdj: -0.3 },
  [POND_IDS[3]]: { doDelta: +0.04, tempDelta: -0.12, phDelta: -0.01, nh3Delta: +0.002, baseAdj: +0.4 },
  [POND_IDS[4]]: { doDelta: -0.02, tempDelta: +0.06, phDelta: 0.00, nh3Delta: -0.001, baseAdj: +0.0 },
  [POND_IDS[5]]: { doDelta: +0.01, tempDelta: +0.03, phDelta: -0.02, nh3Delta: +0.002, baseAdj: +0.3 },
  [POND_IDS[6]]: { doDelta: -0.04, tempDelta: -0.05, phDelta: +0.01, nh3Delta: -0.003, baseAdj: -0.4 },
  [POND_IDS[7]]: { doDelta: +0.02, tempDelta: +0.08, phDelta: +0.00, nh3Delta: +0.001, baseAdj: +0.5 },
  [POND_IDS[8]]: { doDelta: -0.01, tempDelta: -0.02, phDelta: +0.02, nh3Delta: +0.000, baseAdj: +0.1 }
};

function formatDate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function addDays(date, days) {
  const d = new Date(date.getTime());
  d.setDate(d.getDate() + days);
  return d;
}

function* weeklyDates(start, end) {
  let d = new Date(start.getTime());
  // 归零到周起点（不强制，直接用start即可）
  while (d <= end) {
    yield new Date(d.getTime());
    d = addDays(d, 7);
  }
}

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function round2(n) {
  return Math.round(n * 100) / 100;
}

function calcEnvFactor(doVal, tempVal, phVal, nh3Val) {
  const delta = WEIGHT.do * Math.abs(doVal - OPT.do)
    + WEIGHT.temp * Math.abs(tempVal - OPT.temp)
    + WEIGHT.ph * Math.abs(phVal - OPT.ph)
    + WEIGHT.nh3 * Math.abs(nh3Val - OPT.nh3);
  return 1 - delta;
}

function generateEnvNearOptimal() {
  // 为确保综合存活率>=90，需让环境修正因子尽量接近1
  // 采用极小偏差范围生成
  const doVal = round2(OPT.do + (Math.random() * 0.04 - 0.02));     // 6.00 ± 0.02
  const tempVal = round2(OPT.temp + (Math.random() * 0.10 - 0.05)); // 24.00 ± 0.05
  const phVal = round2(OPT.ph + (Math.random() * 0.04 - 0.02));     // 7.00 ± 0.02
  // 氨氮更贴近真实：大多数在0.05–0.25，小概率到0.40
  // 混合分布：85% U[0.05,0.25]，10% U[0.25,0.40]，5% U[0.02,0.05]
  const r = Math.random();
  let nh3Raw;
  if (r < 0.85) nh3Raw = 0.05 + Math.random() * (0.25 - 0.05);
  else if (r < 0.95) nh3Raw = 0.25 + Math.random() * (0.40 - 0.25);
  else nh3Raw = 0.02 + Math.random() * (0.05 - 0.02);
  const nh3Val = round2(nh3Raw);
  return { doVal, tempVal, phVal, nh3Val };
}

function generateRecord(pondId, stockDate) {
  const manager = pick(MANAGERS);
  const cycleDays = 90 + Math.floor(Math.random() * 91); // 90-180

  // 投放数量（以100为单位），在3000-8000之间
  const stockQty = Math.floor((3000 + Math.random() * 5001) / 100) * 100;

  // 先设定较高基础存活率，以保证>=90
  // 目标基础存活率在 95.0% - 99.0%
  const profile = POND_PROFILES[pondId] || { baseAdj: 0 };
  const baseRate = 95 + Math.random() * 4 + (profile.baseAdj || 0); // [95,99)+pond细微调整
  let surviveQty = Math.round(stockQty * baseRate / 100);
  if (surviveQty > stockQty) surviveQty = stockQty;

  // 环境生成并计算修正因子与综合存活率，如不达标则重试微调
  let env, factor, compositeRate;
  for (let i = 0; i < 50; i++) {
    env = generateEnvNearOptimal();
    // 添加池塘固定偏移 + 日期微抖动，避免同一时间各池塘数据雷同
    const t = stockDate.getTime() / (24*3600*1000);
    const jitter = (amp) => (Math.sin(t * 0.5 + pondId.charCodeAt(2)) * amp);
    env.doVal = round2(env.doVal + (profile.doDelta || 0) + jitter(0.01));
    env.tempVal = round2(env.tempVal + (profile.tempDelta || 0) + jitter(0.05));
    env.phVal = round2(env.phVal + (profile.phDelta || 0) + jitter(0.01));
    env.nh3Val = round2(env.nh3Val + (profile.nh3Delta || 0) + jitter(0.001));

    factor = calcEnvFactor(env.doVal, env.tempVal, env.phVal, env.nh3Val);
    compositeRate = (baseRate) * factor;
    if (baseRate >= 90 && compositeRate >= 90 && factor > 0) break;
    // 若不达标，进一步靠近最优（缩小偏差）再试
    env = {
      doVal: round2((env.doVal + OPT.do) / 2),
      tempVal: round2((env.tempVal + OPT.temp) / 2),
      phVal: round2((env.phVal + OPT.ph) / 2),
      nh3Val: round2((env.nh3Val + OPT.nh3) / 2)
    };
    factor = calcEnvFactor(env.doVal, env.tempVal, env.phVal, env.nh3Val);
    compositeRate = (baseRate) * factor;
  }

  // 死亡原因：根据常规实际情况随机取值
  const deathReasons = ['pH值异常', '疾病', '操作损伤', '水质异常', '饲料问题', '密度过高'];
  const deathReason = pick(deathReasons);

  const detectDate = addDays(stockDate, 7);

  // 构造一条记录（与数据结构16字段完全一致，key为中文）
  const record = {
    '养殖池编号': pondId,
    '鱼类': FISH_TYPE,
    '投放时间': formatDate(stockDate),
    '投放数量（尾）': stockQty,
    '存活数量（尾）': surviveQty,
    '死亡原因': deathReason,
    '养殖周期（天）': cycleDays,
    '检测时间': formatDate(detectDate),
    '负责人': manager,
    '水温（℃）': env.tempVal,
    '溶氧（mg/L）': env.doVal,
    'pH': env.phVal,
    '氨氮（mg/L）': env.nh3Val,
    '基础存活率（%）': round2((surviveQty / stockQty) * 100),
    '环境修正因子': round2(factor),
    '综合存活率（%）': round2(round2((surviveQty / stockQty) * 100) * factor)
  };

  // 最终保障：若综合存活率仍<90，微调为边界值90.0（仅在极端情况下）
  if (record['综合存活率（%）'] < 90) {
    // 将环境进一步靠近最优，提升因子
    record['溶氧（mg/L）'] = OPT.do;
    record['水温（℃）'] = OPT.temp;
    record['pH'] = OPT.ph;
    record['氨氮（mg/L）'] = OPT.nh3;
    record['环境修正因子'] = 1;
    record['综合存活率（%）'] = record['基础存活率（%）'];
    if (record['综合存活率（%）'] < 90) {
      // 增加存活数量到满足90%
      record['存活数量（尾）'] = Math.ceil(record['投放数量（尾）'] * 0.90);
      record['基础存活率（%）'] = round2((record['存活数量（尾）'] / record['投放数量（尾）']) * 100);
      record['综合存活率（%）'] = record['基础存活率（%）'];
    }
  }

  return record;
}

function main() {
  const all = [];
  let stop = false;
  for (const d of weeklyDates(START_DATE, END_DATE)) {
    for (const pond of POND_IDS) {
      if (all.length >= MAX_RECORDS) { stop = true; break; }
      all.push(generateRecord(pond, d));
    }
    if (stop) break;
  }
  const outPath = './bl_survival_data.json';
  fs.writeFileSync(outPath, JSON.stringify(all, null, 2), 'utf-8');
  console.log(`已生成 ${all.length} 条记录（上限：${MAX_RECORDS}） -> ${outPath}`);
}

if (require.main === module) {
  main();
}