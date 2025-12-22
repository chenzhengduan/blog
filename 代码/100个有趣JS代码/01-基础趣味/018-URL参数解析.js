// 📱 URL参数解析
// 学习点：字符串处理和对象操作

function parseURL(url) {
  const params = {};
  const queryString = url.split('?')[1];
  
  if (queryString) {
    queryString.split('&').forEach(param => {
      const [key, value] = param.split('=');
      params[decodeURIComponent(key)] = decodeURIComponent(value);
    });
  }
  
  return params;
}

// 使用URLSearchParams（现代方法）
function parseURLModern(url) {
  const params = new URLSearchParams(url.split('?')[1]);
  return Object.fromEntries(params);
}

const url = 'https://example.com?name=张三&age=25&city=北京';

console.log('手动解析:', parseURL(url));
console.log('URLSearchParams:', parseURLModern(url));

// 反向：对象转URL参数
const obj = { name: '李四', age: 30, city: '上海' };
const query = new URLSearchParams(obj).toString();
console.log('对象转URL:', query);
