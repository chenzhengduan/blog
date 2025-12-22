// 🎬 简易动画库
// 学习点：requestAnimationFrame

class Animator {
  constructor() {
    this.animations = new Map();
    this.running = false;
  }
  
  // 缓动函数
  easing = {
    linear: t => t,
    easeInQuad: t => t * t,
    easeOutQuad: t => t * (2 - t),
    easeInOutQuad: t => t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t,
    easeInCubic: t => t * t * t,
    easeOutCubic: t => (--t) * t * t + 1,
    easeInOutCubic: t => t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1
  };
  
  animate(options) {
    const {
      from,
      to,
      duration = 1000,
      easing = 'linear',
      onUpdate,
      onComplete
    } = options;
    
    const id = Math.random().toString(36);
    const startTime = performance.now();
    const easingFn = this.easing[easing] || this.easing.linear;
    
    const animation = {
      startTime,
      update: (currentTime) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easedProgress = easingFn(progress);
        
        const current = from + (to - from) * easedProgress;
        onUpdate(current, progress);
        
        if (progress >= 1) {
          this.animations.delete(id);
          if (onComplete) onComplete();
          
          if (this.animations.size === 0) {
            this.running = false;
          }
        }
      }
    };
    
    this.animations.set(id, animation);
    
    if (!this.running) {
      this.running = true;
      this.loop();
    }
    
    return id;
  }
  
  loop() {
    if (!this.running) return;
    
    const currentTime = performance.now();
    this.animations.forEach(animation => {
      animation.update(currentTime);
    });
    
    requestAnimationFrame(() => this.loop());
  }
  
  stop(id) {
    this.animations.delete(id);
  }
  
  stopAll() {
    this.animations.clear();
    this.running = false;
  }
}

const animator = new Animator();

console.log('动画库演示:\n');

// 模拟数值动画
console.log('从0动画到100:');
animator.animate({
  from: 0,
  to: 100,
  duration: 2000,
  easing: 'easeOutCubic',
  onUpdate: (value) => {
    console.log(`当前值: ${value.toFixed(2)}`);
  },
  onComplete: () => {
    console.log('✅ 动画完成');
  }
});

console.log('\n💡 可用于数字滚动、进度条、位置移动等');
