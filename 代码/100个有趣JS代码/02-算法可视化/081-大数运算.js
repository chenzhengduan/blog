// 🔢 大数运算
// 学习点：字符串处理与算法

const bigInt = {
  // 大数加法
  add(a, b) {
    a = a.toString();
    b = b.toString();
    
    let result = '';
    let carry = 0;
    let i = a.length - 1;
    let j = b.length - 1;
    
    while (i >= 0 || j >= 0 || carry > 0) {
      const digitA = i >= 0 ? parseInt(a[i--]) : 0;
      const digitB = j >= 0 ? parseInt(b[j--]) : 0;
      
      const sum = digitA + digitB + carry;
      result = (sum % 10) + result;
      carry = Math.floor(sum / 10);
    }
    
    return result;
  },
  
  // 大数乘法
  multiply(a, b) {
    a = a.toString();
    b = b.toString();
    
    const result = Array(a.length + b.length).fill(0);
    
    for (let i = a.length - 1; i >= 0; i--) {
      for (let j = b.length - 1; j >= 0; j--) {
        const mul = parseInt(a[i]) * parseInt(b[j]);
        const pos1 = i + j;
        const pos2 = i + j + 1;
        const sum = mul + result[pos2];
        
        result[pos2] = sum % 10;
        result[pos1] += Math.floor(sum / 10);
      }
    }
    
    return result.join('').replace(/^0+/, '') || '0';
  },
  
  // 阶乘
  factorial(n) {
    let result = '1';
    for (let i = 2; i <= n; i++) {
      result = this.multiply(result, i.toString());
    }
    return result;
  }
};

console.log('大数运算演示:\n');
console.log('999999999999 + 888888888888 =', bigInt.add('999999999999', '888888888888'));
console.log('123456789 * 987654321 =', bigInt.multiply('123456789', '987654321'));
console.log('100! =', bigInt.factorial(100));

console.log('\n💡 JavaScript原生BigInt更简单: 999999999999n + 888888888888n');
