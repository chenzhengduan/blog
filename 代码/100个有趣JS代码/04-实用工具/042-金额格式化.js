// 💰 金额格式化
// 学习点：数字处理

function formatMoney(amount, options = {}) {
  const {
    decimals = 2,
    symbol = '¥',
    thousandsSep = ',',
    decimalSep = '.'
  } = options;
  
  const num = parseFloat(amount);
  if (isNaN(num)) return '0.00';
  
  const [integer, decimal] = num.toFixed(decimals).split('.');
  
  const formattedInteger = integer.replace(/\B(?=(\d{3})+(?!\d))/g, thousandsSep);
  
  return `${symbol}${formattedInteger}${decimalSep}${decimal}`;
}

// 数字转中文大写
function numberToChinese(money) {
  const cnNums = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'];
  const cnUnits = ['', '拾', '佰', '仟'];
  const cnGroups = ['', '万', '亿', '万亿'];
  
  const [integer, decimal] = String(money).split('.');
  
  let result = '';
  const groups = [];
  for (let i = integer.length; i > 0; i -= 4) {
    groups.push(integer.substring(Math.max(0, i - 4), i));
  }
  
  groups.reverse().forEach((group, groupIndex) => {
    let groupStr = '';
    for (let i = 0; i < group.length; i++) {
      const digit = parseInt(group[i]);
      if (digit !== 0) {
        groupStr += cnNums[digit] + cnUnits[group.length - 1 - i];
      } else if (groupStr[groupStr.length - 1] !== '零') {
        groupStr += '零';
      }
    }
    if (groupStr) result += groupStr + cnGroups[groups.length - 1 - groupIndex];
  });
  
  return result + '元整';
}

console.log(formatMoney(1234567.89));
console.log(formatMoney(1234567.89, { symbol: '$', thousandsSep: ' ' }));
console.log(numberToChinese(12345));
