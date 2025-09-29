/*
生成"青鱼投喂分析数据"的示例数据
- 严格遵循算法：
  基础投喂量 = 平均体重(kg/尾) × 放养密度(尾/亩) × 基础日投喂率
  环境修正因子 = 1 - (0.35*|溶氧-6.0| + 0.30*|水温-26.0| + 0.20*|pH-7.0| + 0.15*|氨氮-0.1|)
  行为修正因子 = 0.4 × 鱼群活性评分/100 + 0.4 × 摄食活跃度评分/100 + 0.2 × 鱼群聚集度/100
  历史残饵率修正系数:根据历史残饵率设定（< 3%：1.10，3%–5%：1.00，> 5%：0.80）
  最终单次投喂量 = (日总投喂量 × 环境修正因子 × 行为修正因子 × 历史残饵率修正系数) / 当日投喂次数
- 约束：
  * 所有计算结果合理且符合实际
  * 记录时间：2024-07 至 2025-07，每周每个鱼塘一条记录
- 输出：当前目录生成 qy_feeding_data.json，数组JSON，key为中文字段名
*/

const fs = require('fs');
const code = 'QY';
const FISH_TYPE = '青鱼';

// 配置参数
const START_DATE = new Date('2024-07-01');
const END_DATE = new Date('2025-08-31');
const MAX_RECORDS = 580;
const POND_IDS = [code+'01', code+'02', code+'03', code+'04', code+'05', code+'06', code+'07', code+'08', code+'09'];

// 最优值与权重（严格来自文档）
const OPT = {
  do: 6.0,      // 溶氧（mg/L）
  temp: 26.0,   // 水温（℃）
  ph: 7.0,      // pH
  nh3: 0.1      // 氨氮（mg/L）
};
const WEIGHT = { do: 0.35, temp: 0.30, ph: 0.20, nh3: 0.15 };

// 体重阶段对应的投喂率
const FEEDING_RATES = {
  '0.2-0.5': { min: 1.8, max: 2.5 },
  '0.5-1.0': { min: 1.2, max: 1.8 },
  '1.0-2.0': { min: 0.8, max: 1.2 }
};

// 季节对应的投喂次数
const SEASON_FEEDING = {
  '春季': { times: 3, periods: ['8:30', '11:30', '16:30'] },
  '夏季': { times: 4, periods: ['7:00', '9:30', '17:30', '20:00'] },
  '秋季': { times: 3, periods: ['8:30', '11:30', '16:30'] },
  '冬季': { times: 2, periods: ['12:00', '14:00'] }
};

// 活跃度对应的修正系数
const ACTIVITY_FACTORS = {
  '良好': 1.0,
  '一般': 0.8,
  '较差': 0.6
};

// 为每个鱼塘引入稳定的轻微偏移
const POND_PROFILES = {
  [POND_IDS[0]]: { doDelta: -0.03, tempDelta: +0.10, phDelta: -0.02, nh3Delta: +0.003 },
  [POND_IDS[1]]: { doDelta: +0.02, tempDelta: -0.08, phDelta: +0.01, nh3Delta: -0.002 },
  [POND_IDS[2]]: { doDelta: -0.01, tempDelta: +0.05, phDelta: +0.02, nh3Delta: +0.001 },
  [POND_IDS[3]]: { doDelta: +0.04, tempDelta: -0.12, phDelta: -0.01, nh3Delta: +0.002 },
  [POND_IDS[4]]: { doDelta: -0.02, tempDelta: +0.06, phDelta: 0.00, nh3Delta: -0.001 },
  [POND_IDS[5]]: { doDelta: +0.01, tempDelta: +0.03, phDelta: -0.02, nh3Delta: +0.002 },
  [POND_IDS[6]]: { doDelta: -0.04, tempDelta: -0.05, phDelta: +0.01, nh3Delta: -0.003 },
  [POND_IDS[7]]: { doDelta: +0.02, tempDelta: +0.08, phDelta: +0.00, nh3Delta: +0.001 },
  [POND_IDS[8]]: { doDelta: -0.01, tempDelta: -0.02, phDelta: +0.02, nh3Delta: +0.000 }
};

function formatDate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hour = String(8 + Math.floor(Math.random() * 4)).padStart(2, '0');
  const minute = String(Math.floor(Math.random() * 60)).padStart(2, '0');
  return `${y}-${m}-${day} ${hour}:${minute}`;
}

function addDays(date, days) {
  const d = new Date(date.getTime());
  d.setDate(d.getDate() + days);
  return d;
}

function* weeklyDates(start, end) {
  let d = new Date(start.getTime());
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

function getSeason(date) {
  const month = date.getMonth() + 1;
  if (month >= 3 && month <= 5) return '春季';
  if (month >= 6 && month <= 8) return '夏季';
  if (month >= 9 && month <= 11) return '秋季';
  return '冬季';
}

function getWeightStage(weight) {
  if (weight >= 0.2 && weight < 0.5) return '0.2-0.5';
  if (weight >= 0.5 && weight < 1.0) return '0.5-1.0';
  if (weight >= 1.0 && weight <= 2.0) return '1.0-2.0';
  return '0.5-1.0'; // 默认
}

function calcEnvFactor(doVal, tempVal, phVal, nh3Val) {
  const delta = WEIGHT.do * Math.abs(doVal - OPT.do)
    + WEIGHT.temp * Math.abs(tempVal - OPT.temp)
    + WEIGHT.ph * Math.abs(phVal - OPT.ph)
    + WEIGHT.nh3 * Math.abs(nh3Val - OPT.nh3);
  const f = 1 - delta;
  return Math.max(0, Math.min(1, f));
}

function generateEnvNearOptimal() {
  const doVal = round2(OPT.do + (Math.random() * 0.4 - 0.2));     // 6.0 ± 0.2
  const tempVal = round2(OPT.temp + (Math.random() * 2.0 - 1.0)); // 26.0 ± 1.0
  const phVal = round2(OPT.ph + (Math.random() * 0.4 - 0.2));     // 7.0 ± 0.2
  const nh3Val = round2(0.05 + Math.random() * 0.3);             // 0.05-0.35
  return { doVal, tempVal, phVal, nh3Val };
}

function generateRecord(pondId, recordDate) {
  const season = getSeason(recordDate);
  const profile = POND_PROFILES[pondId] || {};
  
  // 基础数据
  const avgWeight = round2(0.2 + Math.random() * 1.8); // 0.2-2.0 kg
  const density = Math.floor(500 + Math.random() * 1000); // 500-1500 尾/亩
  const weightStage = getWeightStage(avgWeight);
  const feedingRateRange = FEEDING_RATES[weightStage];
  const baseFeedingRate = round2(feedingRateRange.min + Math.random() * (feedingRateRange.max - feedingRateRange.min));
  
  // 环境数据
  let env = generateEnvNearOptimal();
  const t = recordDate.getTime() / (24*3600*1000);
  const jitter = (amp) => (Math.sin(t * 0.5 + pondId.charCodeAt(2)) * amp);
  env.doVal = round2(env.doVal + (profile.doDelta || 0) + jitter(0.05));
  env.tempVal = round2(env.tempVal + (profile.tempDelta || 0) + jitter(0.2));
  env.phVal = round2(env.phVal + (profile.phDelta || 0) + jitter(0.05));
  env.nh3Val = round2(env.nh3Val + (profile.nh3Delta || 0) + jitter(0.01));
  
  // 鱼群行为数据
  const activityScore = Math.floor(70 + Math.random() * 30); // 70-100
  const feedingScore = Math.floor(75 + Math.random() * 25);  // 75-100
  const gatheringScore = Math.floor(70 + Math.random() * 30); // 70-100
  const activity = pick(['良好', '一般', '较差']);
  
  // 历史残饵率
  const historyWasteRate = round2(1 + Math.random() * 6); // 1-7%
  
  // 计算修正因子
  const envFactor = calcEnvFactor(env.doVal, env.tempVal, env.phVal, env.nh3Val);
  const behaviorFactor = round2(0.4 * activityScore/100 + 0.4 * feedingScore/100 + 0.2 * gatheringScore/100);
  const activityFactor = ACTIVITY_FACTORS[activity];
  const finalBehaviorFactor = round2(behaviorFactor * activityFactor);
  
  // 历史残饵率修正系数
  let wasteCorrectionFactor;
  if (historyWasteRate < 3) wasteCorrectionFactor = 1.10;
  else if (historyWasteRate <= 5) wasteCorrectionFactor = 1.00;
  else wasteCorrectionFactor = 0.80;
  
  // 投喂计算
  const totalDailyFeeding = round2(avgWeight * density * baseFeedingRate / 100); // 日总投喂量
  const seasonInfo = SEASON_FEEDING[season];
  const dailyFeedingTimes = seasonInfo.times;
  const finalSingleFeeding = round2((totalDailyFeeding * envFactor * finalBehaviorFactor * wasteCorrectionFactor) / dailyFeedingTimes);
  
  // 构造记录
  const record = {
    '养殖池编号': pondId,
    '鱼类': FISH_TYPE,
    '记录时间': formatDate(recordDate),
    '溶氧（mg/L）': env.doVal,
    '水温（℃）': env.tempVal,
    'pH': env.phVal,
    '氨氮（mg/L）': env.nh3Val,
    '季节': season,
    '活跃度': activity,
    '鱼群活性评分（0-100）': activityScore,
    '平均体重（kg/尾）': avgWeight,
    '放养密度（尾/亩或kg/m³）': density,
    '历史残饵率（过去24h，%）': historyWasteRate,
    '历史残饵率修正系数': wasteCorrectionFactor,
    '摄食活跃度评分（0-100）': feedingScore,
    '鱼群聚集度（0-100）': gatheringScore,
    '环境修正因子': round2(envFactor),
    '行为修正因子（0-1）': finalBehaviorFactor,
    '基础日投喂率（%/日）': baseFeedingRate,
    '日总投喂量（kg）': totalDailyFeeding,
    '当日投喂次数（次/日）': dailyFeedingTimes,
    '最终单次投喂量（kg）': finalSingleFeeding
  };

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
  const outPath = './qy_feeding_data.json';
  fs.writeFileSync(outPath, JSON.stringify(all, null, 2), 'utf-8');
  console.log(`已生成 ${all.length} 条记录（上限：${MAX_RECORDS}） -> ${outPath}`);
}

if (require.main === module) {
  main();
}
