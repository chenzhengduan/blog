// 🎭 奇妙的类型转换
// 学习点：JavaScript的隐式类型转换

console.log('=== 神奇的加号 ===');
console.log('1 + "1" =', 1 + "1");           // "11" 
console.log('"1" + 1 =', "1" + 1);           // "11"
console.log('1 + 1 + "1" =', 1 + 1 + "1");   // "21"
console.log('"1" + 1 + 1 =', "1" + 1 + 1);   // "111"

console.log('\n=== 神奇的减号 ===');
console.log('"5" - 2 =', "5" - 2);           // 3
console.log('5 - "2" =', 5 - "2");           // 3
console.log('"5" - "2" =', "5" - "2");       // 3

console.log('\n=== 布尔转换 ===');
console.log('!!"" =', !!"");                 // false
console.log('!!0 =', !!0);                   // false
console.log('!!null =', !!null);             // false
console.log('!!"0" =', !!"0");               // true
console.log('!![] =', !![]);                 // true
console.log('!!{} =', !!{});                 // true

console.log('\n=== 奇葩的相等 ===');
console.log('[] == ![] =', [] == ![]);       // true (wtf!)
console.log('0 == "0" =', 0 == "0");         // true
console.log('0 == [] =', 0 == []);           // true
console.log('"0" == [] =', "0" == []);       // false

console.log('\n💡 学到了吗？尽量使用 === 来避免意外！');
