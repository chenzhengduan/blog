// ========================================
// 🎉 有趣的前端 JavaScript 代码合集
// ========================================

// 1️⃣ 随机生成"摸鱼"理由
function getExcuse() {
  const reasons = [
    "🐟 服务器又宕机了，等会再试试。",
    "💻 代码写得太完美，需要休息一下。",
    "👔 老板在附近，假装很忙。",
    "📅 今天是周五，效率自动降低50%。",
    "🤔 正在思考人生和代码的关系。",
    "☕ 编译中，大概需要30分钟...",
    "🐛 Bug太多，需要静静。",
    "🎮 这不是摸鱼，这是在寻找灵感！",
    "📱 手机响了，可能是紧急的事。",
    "🍕 低血糖，必须补充能量。"
  ];
  const excuse = reasons[Math.floor(Math.random() * reasons.length)];
  console.log("📢 摸鱼理由：" + excuse);
  return excuse;
}

// 使用示例：
// getExcuse();


// 2️⃣ "一键变秃"按钮（页面所有头像变成光头）
function makeEveryoneBald() {
  document.querySelectorAll('img').forEach(img => {
    img.style.filter = 'brightness(1.5) contrast(0.8)';
    img.style.borderRadius = '50%';
  });
  console.log('💡 全员变秃，变强！代码质量提升200%！');
  alert('✨ 全员变秃成功！传说中的"秃头强化术"已激活！');
}

// 使用示例：
// makeEveryoneBald();


// 3️⃣ "假装在工作"进度条
function fakeWorking() {
  console.log('🚀 开始"努力"工作...');
  let progress = 0;
  const tasks = [
    "正在编译代码...",
    "正在优化性能...",
    "正在修复Bug...",
    "正在重构代码...",
    "正在跑测试用例...",
    "正在打包部署...",
    "正在写文档..."
  ];
  
  const interval = setInterval(() => {
    progress += Math.random() * 15;
    const currentTask = tasks[Math.floor(Math.random() * tasks.length)];
    
    if (progress >= 100) {
      console.log('✅ 工作完成！其实啥也没干，只是看起来很忙。');
      console.log('🎊 成就解锁：【摸鱼大师】');
      clearInterval(interval);
    } else {
      console.log(`⚙️  ${currentTask} 进度：${Math.floor(progress)}%`);
    }
  }, 800);
}

// 使用示例：
// fakeWorking();


// 4️⃣ "随机决定今天写不写代码"
function shouldICodeToday() {
  const luck = Math.random();
  const messages = [
    { threshold: 0.9, text: '🌟 今天状态爆棚！疯狂写代码！', emoji: '💻💻💻' },
    { threshold: 0.7, text: '😊 今天心情不错，可以写点代码', emoji: '💻' },
    { threshold: 0.5, text: '😐 今天一般般，随便写写吧', emoji: '🤷' },
    { threshold: 0.3, text: '😴 今天不太想写，摸会鱼吧', emoji: '🐟' },
    { threshold: 0, text: '🛌 今天绝对不写！躺平是王道！', emoji: '😴😴😴' }
  ];
  
  const result = messages.find(m => luck >= m.threshold);
  console.log(`🎲 今日运势值：${(luck * 100).toFixed(2)}%`);
  console.log(`${result.emoji} ${result.text}`);
  alert(`${result.emoji}\n${result.text}`);
  return luck > 0.5;
}

// 使用示例：
// shouldICodeToday();


// 5️⃣ "彩虹弹窗轰炸"
function rainbowAlertBomb() {
  const messages = [
    '🌈 前端快乐弹窗 1',
    '🎨 你以为结束了？',
    '🎉 还有呢！',
    '✨ 惊不惊喜？',
    '🎊 意不意外？'
  ];
  
  console.log('💣 彩虹弹窗轰炸启动！');
  messages.forEach((msg, i) => {
    setTimeout(() => {
      alert(msg);
    }, i * 500);
  });
}

// 使用示例（慎用！）：
// rainbowAlertBomb();


// 6️⃣ "代码行数膨胀器"（让你的代码看起来工作量很大）
function inflateCodeLines(lines = 100) {
  console.log(`🚀 正在生成 ${lines} 行"高质量"代码...`);
  for (let i = 1; i <= lines; i++) {
    console.log(`// TODO: 这是第 ${i} 行非常重要的代码`);
  }
  console.log(`✅ 成功生成 ${lines} 行代码！老板一定很满意！`);
}

// 使用示例：
// inflateCodeLines(50);


// 7️⃣ "Bug制造机"（随机让页面出点小问题）
function createRandomBug() {
  const bugs = [
    () => { document.body.style.transform = 'rotate(180deg)'; console.log('🐛 Bug: 页面倒立了'); },
    () => { document.body.style.filter = 'blur(5px)'; console.log('🐛 Bug: 页面模糊了'); },
    () => { document.body.style.animation = 'shake 0.5s infinite'; console.log('🐛 Bug: 页面抖动了'); },
    () => { document.querySelectorAll('*').forEach(el => el.style.fontFamily = 'Comic Sans MS'); console.log('🐛 Bug: 字体变丑了'); },
    () => { setInterval(() => document.body.style.background = '#' + Math.floor(Math.random()*16777215).toString(16), 100); console.log('🐛 Bug: 背景色疯了'); }
  ];
  
  const bug = bugs[Math.floor(Math.random() * bugs.length)];
  bug();
  alert('⚠️ 检测到野生Bug！3秒后自动修复...');
  setTimeout(() => {
    location.reload();
  }, 3000);
}

// 使用示例（慎用！）：
// createRandomBug();


// 8️⃣ "程序员日常模拟器"
function programmerDailySimulator() {
  const activities = [
    { time: '09:00', activity: '☕ 泡咖啡，看看新闻', duration: 30 },
    { time: '09:30', activity: '📧 回复昨天的邮件', duration: 30 },
    { time: '10:00', activity: '💻 开始写代码', duration: 60 },
    { time: '11:00', activity: '🐛 发现Bug，开始调试', duration: 60 },
    { time: '12:00', activity: '🍱 午饭时间', duration: 60 },
    { time: '13:00', activity: '😴 饭后昏迷', duration: 30 },
    { time: '13:30', activity: '👥 下午会议', duration: 90 },
    { time: '15:00', activity: '☕ 下午茶摸鱼', duration: 30 },
    { time: '15:30', activity: '💻 继续写代码', duration: 120 },
    { time: '17:30', activity: '🚀 提交代码，下班', duration: 30 },
    { time: '18:00', activity: '🏃 逃离公司', duration: 0 }
  ];
  
  console.log('📅 程序员的一天：');
  console.log('================================');
  activities.forEach(act => {
    console.log(`⏰ ${act.time} - ${act.activity}`);
  });
  console.log('================================');
  console.log('💡 总结：一天中真正写代码的时间不到4小时！');
}

// 使用示例：
// programmerDailySimulator();


// 9️⃣ "代码审查模拟器"
function codeReviewSimulator(yourCode) {
  const comments = [
    "❌ 这个变量命名不规范",
    "⚠️ 这里应该加个注释",
    "🤔 为什么不用ES6语法？",
    "💡 建议提取成单独的函数",
    "🐛 这里可能会有性能问题",
    "📝 这段代码可以优化",
    "✅ LGTM（Looks Good To Me）",
    "🎉 代码写得不错！",
    "🔥 这个实现很优雅"
  ];
  
  console.log('👀 代码审查开始...');
  console.log('你的代码：', yourCode);
  console.log('---');
  
  const numComments = Math.floor(Math.random() * 5) + 1;
  for (let i = 0; i < numComments; i++) {
    setTimeout(() => {
      const comment = comments[Math.floor(Math.random() * comments.length)];
      console.log(`💬 审查意见 ${i + 1}: ${comment}`);
      
      if (i === numComments - 1) {
        const approved = Math.random() > 0.3;
        console.log('---');
        console.log(approved ? '✅ 代码审查通过！可以合并了！' : '❌ 代码审查不通过，请修改后重新提交！');
      }
    }, i * 1000);
  }
}

// 使用示例：
// codeReviewSimulator('const a = 1;');


// 🔟 "摸鱼等级测试"
function fishingLevelTest() {
  const questions = [
    "今天你打开IDE了吗？",
    "你今天写了超过10行代码吗？",
    "你有超过1小时在认真工作吗？",
    "你今天没有刷短视频吗？",
    "你今天的提交记录是真实的吗？"
  ];
  
  console.log('🎯 摸鱼等级测试开始！');
  console.log('请诚实回答以下问题（输入 y/n）：');
  
  let yesCount = 0;
  questions.forEach((q, i) => {
    console.log(`${i + 1}. ${q}`);
    // 模拟随机回答
    if (Math.random() > 0.5) yesCount++;
  });
  
  const level = yesCount === 5 ? '🏆 劳模' : 
                yesCount >= 3 ? '👔 普通员工' :
                yesCount >= 1 ? '🐟 摸鱼达人' : '😴 躺平大师';
  
  console.log('---');
  console.log(`你的摸鱼等级：${level}`);
  console.log(`诚实度：${yesCount}/5`);
}

// 使用示例：
// fishingLevelTest();


// ========================================
// 🎮 使用说明
// ========================================
console.log('');
console.log('🎉 欢迎使用有趣的JS代码合集！');
console.log('');
console.log('📝 可用函数列表：');
console.log('1️⃣  getExcuse() - 获取摸鱼理由');
console.log('2️⃣  makeEveryoneBald() - 一键变秃（慎用）');
console.log('3️⃣  fakeWorking() - 假装在工作');
console.log('4️⃣  shouldICodeToday() - 随机决定今天写不写代码');
console.log('5️⃣  rainbowAlertBomb() - 彩虹弹窗轰炸（慎用）');
console.log('6️⃣  inflateCodeLines(50) - 代码行数膨胀器');
console.log('7️⃣  createRandomBug() - Bug制造机（慎用）');
console.log('8️⃣  programmerDailySimulator() - 程序员日常模拟器');
console.log('9️⃣  codeReviewSimulator("你的代码") - 代码审查模拟器');
console.log('🔟 fishingLevelTest() - 摸鱼等级测试');
console.log('');
console.log('💡 提示：在浏览器控制台中直接调用即可！');
console.log('⚠️  警告：带"慎用"标记的函数会改变页面，请在测试环境使用！');
