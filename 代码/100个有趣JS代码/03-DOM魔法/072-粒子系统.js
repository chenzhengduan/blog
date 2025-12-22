// 🌊 粒子动画系统
// 学习点：动画原理

class Particle {
  constructor(x, y) {
    this.x = x;
    this.y = y;
    this.vx = (Math.random() - 0.5) * 2;
    this.vy = (Math.random() - 0.5) * 2;
    this.life = 1.0;
    this.decay = 0.01;
    this.size = Math.random() * 3 + 1;
    this.color = `hsl(${Math.random() * 360}, 100%, 50%)`;
  }
  
  update() {
    this.x += this.vx;
    this.y += this.vy;
    this.vy += 0.1;  // 重力
    this.life -= this.decay;
  }
  
  isDead() {
    return this.life <= 0;
  }
  
  draw(ctx) {
    ctx.save();
    ctx.globalAlpha = this.life;
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  }
}

class ParticleSystem {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.particles = [];
  }
  
  emit(x, y, count = 10) {
    for (let i = 0; i < count; i++) {
      this.particles.push(new Particle(x, y));
    }
  }
  
  update() {
    this.particles = this.particles.filter(p => !p.isDead());
    this.particles.forEach(p => p.update());
  }
  
  draw() {
    this.particles.forEach(p => p.draw(this.ctx));
  }
  
  animate() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    this.update();
    this.draw();
    
    if (this.particles.length > 0) {
      requestAnimationFrame(() => this.animate());
    }
  }
}

console.log('粒子动画系统已定义');
console.log('使用示例:');
console.log(`
const canvas = document.getElementById('canvas');
const ps = new ParticleSystem(canvas);
canvas.addEventListener('click', (e) => {
  ps.emit(e.offsetX, e.offsetY, 20);
  ps.animate();
});
`);
