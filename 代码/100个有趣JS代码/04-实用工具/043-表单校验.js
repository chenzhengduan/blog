// 📱 手机号/身份证校验
// 学习点：正则表达式应用

const validators = {
  // 手机号
  phone(value) {
    return /^1[3-9]\d{9}$/.test(value);
  },
  
  // 身份证
  idCard(value) {
    const pattern = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/;
    if (!pattern.test(value)) return false;
    
    // 校验码验证
    const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    const codes = '10X98765432';
    
    let sum = 0;
    for (let i = 0; i < 17; i++) {
      sum += parseInt(value[i]) * weights[i];
    }
    
    return codes[sum % 11] === value[17].toUpperCase();
  },
  
  // 邮箱
  email(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  },
  
  // 网址
  url(value) {
    try {
      new URL(value);
      return true;
    } catch {
      return false;
    }
  }
};

console.log('手机号校验:', validators.phone('13812345678'));
console.log('邮箱校验:', validators.email('test@example.com'));
console.log('网址校验:', validators.url('https://www.example.com'));
