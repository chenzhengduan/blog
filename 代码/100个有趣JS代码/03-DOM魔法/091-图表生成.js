// 📊 简单图表生成器
// 学习点：Canvas绘图与数据可视化

class ChartRenderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.padding = 40;
  }
  
  clear() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
  }
  
  // 柱状图
  barChart(data, options = {}) {
    this.clear();
    
    const {
      title = '柱状图',
      barColor = '#4a9eff',
      labelColor = '#333'
    } = options;
    
    const width = this.canvas.width - this.padding * 2;
    const height = this.canvas.height - this.padding * 2;
    const maxValue = Math.max(...data.map(d => d.value));
    const barWidth = width / data.length * 0.8;
    const gap = width / data.length * 0.2;
    
    // 标题
    this.ctx.font = '16px Arial';
    this.ctx.fillStyle = '#000';
    this.ctx.textAlign = 'center';
    this.ctx.fillText(title, this.canvas.width / 2, 20);
    
    // 绘制柱子
    data.forEach((item, index) => {
      const barHeight = (item.value / maxValue) * height;
      const x = this.padding + index * (barWidth + gap);
      const y = this.padding + height - barHeight;
      
      // 柱子
      this.ctx.fillStyle = barColor;
      this.ctx.fillRect(x, y, barWidth, barHeight);
      
      // 标签
      this.ctx.fillStyle = labelColor;
      this.ctx.font = '12px Arial';
      this.ctx.textAlign = 'center';
      this.ctx.fillText(item.label, x + barWidth / 2, this.padding + height + 20);
      
      // 数值
      this.ctx.fillText(item.value, x + barWidth / 2, y - 5);
    });
    
    // Y轴
    this.ctx.beginPath();
    this.ctx.moveTo(this.padding, this.padding);
    this.ctx.lineTo(this.padding, this.padding + height);
    this.ctx.stroke();
  }
  
  // 折线图
  lineChart(data, options = {}) {
    this.clear();
    
    const {
      title = '折线图',
      lineColor = '#ff4757',
      pointColor = '#ff6b81'
    } = options;
    
    const width = this.canvas.width - this.padding * 2;
    const height = this.canvas.height - this.padding * 2;
    const maxValue = Math.max(...data.map(d => d.value));
    const step = width / (data.length - 1);
    
    // 标题
    this.ctx.font = '16px Arial';
    this.ctx.fillStyle = '#000';
    this.ctx.textAlign = 'center';
    this.ctx.fillText(title, this.canvas.width / 2, 20);
    
    // 绘制线
    this.ctx.beginPath();
    this.ctx.strokeStyle = lineColor;
    this.ctx.lineWidth = 2;
    
    data.forEach((item, index) => {
      const x = this.padding + index * step;
      const y = this.padding + height - (item.value / maxValue) * height;
      
      if (index === 0) {
        this.ctx.moveTo(x, y);
      } else {
        this.ctx.lineTo(x, y);
      }
    });
    
    this.ctx.stroke();
    
    // 绘制点
    data.forEach((item, index) => {
      const x = this.padding + index * step;
      const y = this.padding + height - (item.value / maxValue) * height;
      
      this.ctx.beginPath();
      this.ctx.fillStyle = pointColor;
      this.ctx.arc(x, y, 4, 0, Math.PI * 2);
      this.ctx.fill();
      
      // 标签
      this.ctx.fillStyle = '#333';
      this.ctx.font = '12px Arial';
      this.ctx.textAlign = 'center';
      this.ctx.fillText(item.label, x, this.padding + height + 20);
    });
  }
}

console.log('图表渲染器已定义');
console.log('使用示例:');
console.log(`
const canvas = document.getElementById('chart');
const chart = new ChartRenderer(canvas);

const data = [
  { label: 'A', value: 10 },
  { label: 'B', value: 25 },
  { label: 'C', value: 15 },
  { label: 'D', value: 30 }
];

chart.barChart(data, { title: '销售数据' });
// 或
chart.lineChart(data, { title: '趋势图' });
`);
