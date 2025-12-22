// 🔐 密码强度检测
// 学习点：字符串分析

function checkPasswordStrength(password) {
  let strength = 0;
  const feedback = [];
  
  // 长度检查
  if (password.length >= 8) {
    strength += 1;
  } else {
    feedback.push('密码至少8位');
  }
  
  // 包含小写字母
  if (/[a-z]/.test(password)) {
    strength += 1;
  } else {
    feedback.push('需要小写字母');
  }
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) {
    strength += 1;
  } else {
    feedback.push('需要大写字母');
  }
  
  // 包含数字
  if (/\d/.test(password)) {
    strength += 1;
  } else {
    feedback.push('需要数字');
  }
  
  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    strength += 1;
  } else {
    feedback.push('需要特殊字符');
  }
  
  // 无连续字符
  if (!/(.)\1{2,}/.test(password)) {
    strength += 1;
  }
  
  const levels = ['很弱', '弱', '一般', '强', '很强', '极强'];
  const level = levels[Math.min(strength, 5)];
  
  return {
    strength,
    level,
    percentage: (strength / 6 * 100).toFixed(0) + '%',
    feedback
  };
}

console.log('密码强度检测:');
console.log('123456:', checkPasswordStrength('123456'));
console.log('Abc123:', checkPasswordStrength('Abc123'));
console.log('Abc123!@#:', checkPasswordStrength('Abc123!@#'));
