// 📝 表单验证器
// 学习点：正则与验证规则

class FormValidator {
  constructor() {
    this.rules = {
      required: (value) => {
        return value !== '' && value !== null && value !== undefined;
      },
      
      email: (value) => {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
      },
      
      phone: (value) => {
        return /^1[3-9]\d{9}$/.test(value);
      },
      
      min: (value, min) => {
        return value.length >= min;
      },
      
      max: (value, max) => {
        return value.length <= max;
      },
      
      minValue: (value, min) => {
        return parseFloat(value) >= min;
      },
      
      maxValue: (value, max) => {
        return parseFloat(value) <= max;
      },
      
      pattern: (value, pattern) => {
        return new RegExp(pattern).test(value);
      },
      
      equals: (value, target) => {
        return value === target;
      }
    };
    
    this.messages = {
      required: '此字段为必填项',
      email: '请输入有效的邮箱地址',
      phone: '请输入有效的手机号',
      min: '最小长度为 {min}',
      max: '最大长度为 {max}',
      minValue: '最小值为 {minValue}',
      maxValue: '最大值为 {maxValue}',
      pattern: '格式不正确',
      equals: '两次输入不一致'
    };
  }
  
  validate(value, rules) {
    const errors = [];
    
    for (const [ruleName, ruleValue] of Object.entries(rules)) {
      const validator = this.rules[ruleName];
      
      if (!validator) {
        console.warn(`未知规则: ${ruleName}`);
        continue;
      }
      
      const isValid = typeof ruleValue === 'boolean' 
        ? validator(value)
        : validator(value, ruleValue);
      
      if (!isValid) {
        let message = this.messages[ruleName];
        
        // 替换占位符
        if (typeof ruleValue !== 'boolean') {
          message = message.replace(`{${ruleName}}`, ruleValue);
        }
        
        errors.push(message);
      }
    }
    
    return {
      valid: errors.length === 0,
      errors
    };
  }
  
  validateForm(formData, schema) {
    const result = {};
    let isValid = true;
    
    for (const [field, rules] of Object.entries(schema)) {
      const value = formData[field] || '';
      const validation = this.validate(value, rules);
      
      result[field] = validation;
      
      if (!validation.valid) {
        isValid = false;
      }
    }
    
    return {
      valid: isValid,
      fields: result
    };
  }
}

const validator = new FormValidator();

console.log('表单验证器演示\n');

// 测试单个字段
console.log('邮箱验证:');
console.log(validator.validate('test@example.com', { required: true, email: true }));
console.log(validator.validate('invalid-email', { email: true }));

// 测试表单
console.log('\n表单验证:');
const formData = {
  username: 'john',
  email: 'john@example.com',
  password: '123',
  confirmPassword: '123'
};

const schema = {
  username: { required: true, min: 3, max: 20 },
  email: { required: true, email: true },
  password: { required: true, min: 6 },
  confirmPassword: { required: true, equals: formData.password }
};

console.log(validator.validateForm(formData, schema));
