// 🎉 彩蛋代码集合
// 学习点：有趣的JavaScript技巧

const easterEggs = {
  // Konami代码检测（↑↑↓↓←→←→BA）
  konamiCode() {
    const code = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 
                  'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight', 'b', 'a'];
    let index = 0;
    
    document.addEventListener('keydown', (e) => {
      if (e.key.toLowerCase() === code[index].toLowerCase()) {
        index++;
        if (index === code.length) {
          console.log('🎉 Konami Code 激活！');
          console.log('你发现了隐藏彩蛋！');
          index = 0;
        }
      } else {
        index = 0;
      }
    });
  },
  
  // 控制台彩色输出
  colorfulConsole() {
    const styles = [
      'color: #e74c3c; font-size: 20px; font-weight: bold',
      'color: #3498db; font-size: 20px; font-weight: bold',
      'color: #2ecc71; font-size: 20px; font-weight: bold',
      'color: #f39c12; font-size: 20px; font-weight: bold'
    ];
    
    console.log('%c Hello', styles[0]);
    console.log('%c World', styles[1]);
    console.log('%c From', styles[2]);
    console.log('%c JavaScript', styles[3]);
  },
  
  // ASCII艺术Logo
  showLogo() {
    console.log(`
╔═══════════════════════════════════════╗
║                                       ║
║     ██╗ █████╗ ██╗   ██╗ █████╗     ║
║     ██║██╔══██╗██║   ██║██╔══██╗    ║
║     ██║███████║██║   ██║███████║    ║
║██   ██║██╔══██║╚██╗ ██╔╝██╔══██║    ║
║╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║    ║
║ ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝    ║
║                                       ║
║     JavaScript 100 个有趣代码          ║
║                                       ║
╚═══════════════════════════════════════╝
    `);
  },
  
  // 控制台警告
  consoleWarning() {
    console.log(
      '%c⚠️ 警告',
      'color: red; font-size: 40px; font-weight: bold'
    );
    console.log(
      '%c此功能仅供开发者使用！',
      'font-size: 20px'
    );
    console.log(
      '%c如果有人让你在这里粘贴代码，这可能是诈骗！',
      'font-size: 16px; color: #e74c3c'
    );
  },
  
  // 浏览器震动
  vibrate() {
    if ('vibrate' in navigator) {
      // 震动模式：200ms震动，100ms暂停，200ms震动
      navigator.vibrate([200, 100, 200]);
      console.log('📳 设备已震动');
    } else {
      console.log('❌ 设备不支持震动API');
    }
  },
  
  // 彩虹文本生成
  rainbowText(text) {
    const colors = ['#e74c3c', '#e67e22', '#f1c40f', '#2ecc71', '#3498db', '#9b59b6'];
    let output = '';
    
    for (let i = 0; i < text.length; i++) {
      const color = colors[i % colors.length];
      output += `%c${text[i]}`;
    }
    
    const styles = Array(text.length).fill(0).map((_, i) => 
      `color: ${colors[i % colors.length]}; font-size: 20px; font-weight: bold`
    );
    
    console.log(output, ...styles);
  },
  
  // 矩阵雨效果（控制台版）
  matrixRain(duration = 3000) {
    const chars = 'ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ01';
    const startTime = Date.now();
    
    const interval = setInterval(() => {
      const line = Array(50).fill(0).map(() => 
        chars[Math.floor(Math.random() * chars.length)]
      ).join('');
      
      console.log(`%c${line}`, 'color: #0f0; font-family: monospace');
      
      if (Date.now() - startTime > duration) {
        clearInterval(interval);
        console.log('%cMatrix Rain Complete', 'color: #0f0; font-size: 16px');
      }
    }, 100);
  },
  
  // 打字机效果
  async typewriter(text, delay = 100) {
    for (let i = 0; i <= text.length; i++) {
      console.clear();
      console.log(text.substring(0, i) + '|');
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
};

console.log('🎉 彩蛋代码集合\n');

console.log('可用功能:');
console.log('1. easterEggs.konamiCode() - Konami代码检测');
console.log('2. easterEggs.colorfulConsole() - 彩色控制台');
console.log('3. easterEggs.showLogo() - ASCII Logo');
console.log('4. easterEggs.consoleWarning() - 控制台警告');
console.log('5. easterEggs.vibrate() - 设备震动');
console.log('6. easterEggs.rainbowText("Hello") - 彩虹文本');
console.log('7. easterEggs.matrixRain() - 矩阵雨');
console.log('8. easterEggs.typewriter("Hello World") - 打字机\n');

// 展示Logo
easterEggs.showLogo();

console.log('\n💡 试试在控制台运行这些函数！');
console.log('💡 100个有趣的JS代码收集完成！🎉');
