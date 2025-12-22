// 🎨 主题切换管理器
// 学习点：CSS变量与本地存储

class ThemeManager {
  constructor(themes = {}) {
    this.themes = themes;
    this.currentTheme = this.loadTheme() || 'light';
    this.listeners = [];
  }
  
  // 定义主题
  defineTheme(name, colors) {
    this.themes[name] = colors;
  }
  
  // 应用主题
  applyTheme(name) {
    if (!this.themes[name]) {
      console.error(`主题 "${name}" 不存在`);
      return;
    }
    
    const theme = this.themes[name];
    const root = document.documentElement;
    
    Object.entries(theme).forEach(([key, value]) => {
      root.style.setProperty(`--${key}`, value);
    });
    
    this.currentTheme = name;
    this.saveTheme(name);
    this.notifyListeners(name);
    
    console.log(`✅ 已切换到主题: ${name}`);
  }
  
  // 切换主题
  toggle() {
    const themes = Object.keys(this.themes);
    const currentIndex = themes.indexOf(this.currentTheme);
    const nextIndex = (currentIndex + 1) % themes.length;
    this.applyTheme(themes[nextIndex]);
  }
  
  // 获取当前主题
  getCurrentTheme() {
    return this.currentTheme;
  }
  
  // 监听主题变化
  onChange(callback) {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter(cb => cb !== callback);
    };
  }
  
  notifyListeners(theme) {
    this.listeners.forEach(callback => callback(theme));
  }
  
  // 保存到本地存储
  saveTheme(name) {
    localStorage.setItem('theme', name);
  }
  
  // 从本地存储加载
  loadTheme() {
    return localStorage.getItem('theme');
  }
}

// 预定义主题
const themeManager = new ThemeManager({
  light: {
    'bg-primary': '#ffffff',
    'bg-secondary': '#f5f5f5',
    'text-primary': '#333333',
    'text-secondary': '#666666',
    'border-color': '#e0e0e0',
    'accent-color': '#007bff'
  },
  dark: {
    'bg-primary': '#1e1e1e',
    'bg-secondary': '#2d2d2d',
    'text-primary': '#ffffff',
    'text-secondary': '#b0b0b0',
    'border-color': '#404040',
    'accent-color': '#4a9eff'
  },
  ocean: {
    'bg-primary': '#e0f7fa',
    'bg-secondary': '#b2ebf2',
    'text-primary': '#006064',
    'text-secondary': '#00838f',
    'border-color': '#80deea',
    'accent-color': '#0097a7'
  }
});

console.log('主题管理器已定义');
console.log('使用方法:');
console.log('themeManager.applyTheme("dark")');
console.log('themeManager.toggle()');
console.log('themeManager.onChange((theme) => console.log(theme))');

console.log('\n可用主题:', Object.keys(themeManager.themes));
