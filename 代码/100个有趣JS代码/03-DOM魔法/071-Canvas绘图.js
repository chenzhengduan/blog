// 🎨 Canvas绘图工具
// 学习点：Canvas API基础

class CanvasDrawer {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId) || document.createElement('canvas');
    this.ctx = this.canvas.getContext('2d');
  }
  
  clear() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
  }
  
  drawCircle(x, y, radius, color = 'blue') {
    this.ctx.beginPath();
    this.ctx.arc(x, y, radius, 0, Math.PI * 2);
    this.ctx.fillStyle = color;
    this.ctx.fill();
  }
  
  drawRect(x, y, width, height, color = 'red') {
    this.ctx.fillStyle = color;
    this.ctx.fillRect(x, y, width, height);
  }
  
  drawLine(x1, y1, x2, y2, color = 'black', width = 1) {
    this.ctx.beginPath();
    this.ctx.moveTo(x1, y1);
    this.ctx.lineTo(x2, y2);
    this.ctx.strokeStyle = color;
    this.ctx.lineWidth = width;
    this.ctx.stroke();
  }
  
  drawText(text, x, y, font = '16px Arial', color = 'black') {
    this.ctx.font = font;
    this.ctx.fillStyle = color;
    this.ctx.fillText(text, x, y);
  }
  
  // 绘制星星
  drawStar(cx, cy, spikes, outerRadius, innerRadius, color = 'gold') {
    let rot = Math.PI / 2 * 3;
    let x = cx;
    let y = cy;
    const step = Math.PI / spikes;
    
    this.ctx.beginPath();
    this.ctx.moveTo(cx, cy - outerRadius);
    
    for (let i = 0; i < spikes; i++) {
      x = cx + Math.cos(rot) * outerRadius;
      y = cy + Math.sin(rot) * outerRadius;
      this.ctx.lineTo(x, y);
      rot += step;
      
      x = cx + Math.cos(rot) * innerRadius;
      y = cy + Math.sin(rot) * innerRadius;
      this.ctx.lineTo(x, y);
      rot += step;
    }
    
    this.ctx.lineTo(cx, cy - outerRadius);
    this.ctx.closePath();
    this.ctx.fillStyle = color;
    this.ctx.fill();
  }
}

console.log('Canvas绘图工具已定义');
console.log('使用方法:');
console.log('const drawer = new CanvasDrawer("myCanvas")');
console.log('drawer.drawCircle(50, 50, 30, "blue")');
console.log('drawer.drawStar(100, 100, 5, 30, 15, "gold")');

console.log('\n💡 需要在浏览器中配合canvas元素使用');
