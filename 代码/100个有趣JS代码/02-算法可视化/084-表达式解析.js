// 🎨 表达式解析器
// 学习点：栈的应用与编译原理

class ExpressionParser {
  // 中缀转后缀（逆波兰表达式）
  infixToPostfix(expression) {
    const precedence = { '+': 1, '-': 1, '*': 2, '/': 2, '^': 3 };
    const output = [];
    const operators = [];
    
    const tokens = expression.match(/\d+\.?\d*|[+\-*/^()]/g);
    
    tokens.forEach(token => {
      if (!isNaN(token)) {
        output.push(parseFloat(token));
      } else if (token === '(') {
        operators.push(token);
      } else if (token === ')') {
        while (operators.length && operators[operators.length - 1] !== '(') {
          output.push(operators.pop());
        }
        operators.pop(); // 移除'('
      } else {
        while (
          operators.length &&
          operators[operators.length - 1] !== '(' &&
          precedence[operators[operators.length - 1]] >= precedence[token]
        ) {
          output.push(operators.pop());
        }
        operators.push(token);
      }
    });
    
    while (operators.length) {
      output.push(operators.pop());
    }
    
    return output;
  }
  
  // 计算后缀表达式
  evaluatePostfix(postfix) {
    const stack = [];
    
    postfix.forEach(token => {
      if (typeof token === 'number') {
        stack.push(token);
      } else {
        const b = stack.pop();
        const a = stack.pop();
        
        switch (token) {
          case '+': stack.push(a + b); break;
          case '-': stack.push(a - b); break;
          case '*': stack.push(a * b); break;
          case '/': stack.push(a / b); break;
          case '^': stack.push(Math.pow(a, b)); break;
        }
      }
    });
    
    return stack[0];
  }
  
  // 直接计算中缀表达式
  evaluate(expression) {
    const postfix = this.infixToPostfix(expression);
    return this.evaluatePostfix(postfix);
  }
}

const parser = new ExpressionParser();

console.log('表达式解析器\n');

const expressions = [
  '3 + 4 * 2',
  '(3 + 4) * 2',
  '2 ^ 3 + 5',
  '10 / 2 - 3'
];

expressions.forEach(expr => {
  const postfix = parser.infixToPostfix(expr);
  const result = parser.evaluatePostfix(postfix);
  console.log(`${expr} = ${result}`);
  console.log(`  后缀: ${postfix.join(' ')}\n`);
});
