// 📅 日期格式化工具
// 学习点：日期处理

function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  const d = new Date(date);
  
  const map = {
    YYYY: d.getFullYear(),
    MM: String(d.getMonth() + 1).padStart(2, '0'),
    DD: String(d.getDate()).padStart(2, '0'),
    HH: String(d.getHours()).padStart(2, '0'),
    mm: String(d.getMinutes()).padStart(2, '0'),
    ss: String(d.getSeconds()).padStart(2, '0'),
    M: d.getMonth() + 1,
    D: d.getDate(),
    H: d.getHours(),
    m: d.getMinutes(),
    s: d.getSeconds()
  };
  
  return format.replace(/YYYY|MM|DD|HH|mm|ss|M|D|H|m|s/g, matched => map[matched]);
}

// 相对时间
function timeAgo(date) {
  const now = new Date();
  const past = new Date(date);
  const diff = now - past;
  
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  
  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return '刚刚';
}

console.log('当前时间:', formatDate(new Date()));
console.log('自定义格式:', formatDate(new Date(), 'YYYY年MM月DD日'));
console.log('相对时间:', timeAgo(new Date(Date.now() - 3600000)));
