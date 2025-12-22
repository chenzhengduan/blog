// 🍛 函数柯里化
// 学习点：函数式编程思想

// 简单示例
function add(a) {
  return function(b) {
    return function(c) {
      return a + b + c;
    }
  }
}
console.log('柯里化加法:', add(1)(2)(3)); // 6

// 通用柯里化函数
function curry(fn) {
  return function curried(...args) {
    if (args.length >= fn.length) {
      return fn.apply(this, args);
    } else {
      return function(...args2) {
        return curried.apply(this, args.concat(args2));
      }
    }
  };
}

// 使用示例
function sum(a, b, c) {
  return a + b + c;
}

const curriedSum = curry(sum);
console.log('通用柯里化:', curriedSum(1)(2)(3)); // 6
console.log('部分应用:', curriedSum(1, 2)(3));   // 6
console.log('一次传入:', curriedSum(1, 2, 3));   // 6
