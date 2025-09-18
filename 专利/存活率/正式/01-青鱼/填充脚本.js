function setInputValue(el, value) {
  const lastValue = el.value;
  el.value = value;

  // 构造事件，让框架感知到变化
  const event = new Event('input', { bubbles: true });
  const tracker = el._valueTracker; // React 的特殊属性
  if (tracker) {
    tracker.setValue(lastValue);
  }
  el.dispatchEvent(event);
}

var inputs = document.querySelectorAll('.formtable input');
var as = Array.from(inputs);

// 请将你的 JSON 粘贴到 data 变量中：
// 支持：单个对象 {key:value,...} 或 数组对象 [{...}, {...}]（默认取第一个）
var data =   {
  "养殖池编号": "ZHB01",
  "鱼类": "中华鳖",
  "投放时间": "2024-05-20",
  "投放数量（尾）": 7900,
  "存活数量（尾）": 7774,
  "死亡原因": "密度过高",
  "养殖周期（天）": 112,
  "检测时间": "2024-05-27",
  "负责人": "EMP001",
  "水温（℃）": 24.11,
  "溶氧（mg/L）": 5.98,
  "pH": 6.98,
  "氨氮（mg/L）": 0.14,
  "基础存活率（%）": 98.41,
  "环境修正因子": 0.95,
  "综合存活率（%）": 93.49
};

// 若是数组，取第一条
if (Array.isArray(data)) {
  data = data[0] || {};
}

// 取 key 作为表头，value 作为对应值（保持原来“左header 右value”的填充方式）
var headers = Object.keys(data);
var values = headers.map(function (k) { return data[k]; });

// 每列两个 input：左边填 header，右边填 value
headers.forEach(function (h, i) {
  var left = as[i * 2];
  var right = as[i * 2 + 1];
  if (left) setInputValue(left, h);
  if (right) setInputValue(right, values[i]);
});