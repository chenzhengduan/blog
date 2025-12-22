// ⌨️ 键盘快捷键系统
// 学习点：键盘事件处理

class KeyboardShortcuts {
  constructor() {
    this.shortcuts = new Map();
    this.init();
  }
  
  init() {
    document.addEventListener('keydown', (e) => {
      const key = this.getKeyCombo(e);
      const handler = this.shortcuts.get(key);
      
      if (handler) {
        e.preventDefault();
        handler(e);
      }
    });
  }
  
  getKeyCombo(e) {
    const keys = [];
    if (e.ctrlKey) keys.push('Ctrl');
    if (e.altKey) keys.push('Alt');
    if (e.shiftKey) keys.push('Shift');
    if (e.metaKey) keys.push('Meta');
    keys.push(e.key.toUpperCase());
    return keys.join('+');
  }
  
  register(keyCombo, handler) {
    this.shortcuts.set(keyCombo, handler);
    console.log(`✅ 注册快捷键: ${keyCombo}`);
  }
  
  unregister(keyCombo) {
    this.shortcuts.delete(keyCombo);
    console.log(`❌ 注销快捷键: ${keyCombo}`);
  }
}

// 使用示例
console.log('键盘快捷键系统已定义');
console.log('使用方法:');
console.log(`
const shortcuts = new KeyboardShortcuts();
shortcuts.register('Ctrl+S', () => console.log('保存'));
shortcuts.register('Ctrl+Shift+D', () => console.log('删除'));
`);
