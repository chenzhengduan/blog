// 🎯 事件委托实现
// 学习点：事件冒泡机制

function delegate(parent, eventType, selector, handler) {
  parent.addEventListener(eventType, function(e) {
    const target = e.target;
    
    // 检查目标元素是否匹配选择器
    if (target.matches(selector)) {
      handler.call(target, e);
    }
    
    // 检查目标元素的父元素是否匹配（支持嵌套）
    const matchedParent = target.closest(selector);
    if (matchedParent && parent.contains(matchedParent)) {
      handler.call(matchedParent, e);
    }
  });
}

// 使用示例
console.log('事件委托函数已定义');
console.log('使用方法:');
console.log(`
const list = document.querySelector('ul');
delegate(list, 'click', 'li', function(e) {
  console.log('点击了:', this.textContent);
});
`);

console.log('\n💡 优点: 减少事件监听器数量，支持动态元素');
