// 🎵 音频可视化
// 学习点：Web Audio API与Canvas

class AudioVisualizer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
    this.analyser = this.audioContext.createAnalyser();
    this.analyser.fftSize = 256;
    this.bufferLength = this.analyser.frequencyBinCount;
    this.dataArray = new Uint8Array(this.bufferLength);
    this.source = null;
    this.animationId = null;
  }
  
  // 加载音频文件
  async loadAudio(url) {
    const response = await fetch(url);
    const arrayBuffer = await response.arrayBuffer();
    const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer);
    
    this.source = this.audioContext.createBufferSource();
    this.source.buffer = audioBuffer;
    this.source.connect(this.analyser);
    this.analyser.connect(this.audioContext.destination);
    
    return audioBuffer.duration;
  }
  
  // 从麦克风输入
  async connectMicrophone() {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    this.source = this.audioContext.createMediaStreamSource(stream);
    this.source.connect(this.analyser);
  }
  
  // 播放
  play() {
    if (this.source && this.source.buffer) {
      this.source.start();
      this.visualize();
    }
  }
  
  // 停止
  stop() {
    if (this.source) {
      this.source.stop();
    }
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
  }
  
  // 可视化 - 柱状图
  visualize() {
    const draw = () => {
      this.animationId = requestAnimationFrame(draw);
      
      this.analyser.getByteFrequencyData(this.dataArray);
      
      this.ctx.fillStyle = 'rgb(0, 0, 0)';
      this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
      
      const barWidth = (this.canvas.width / this.bufferLength) * 2.5;
      let x = 0;
      
      for (let i = 0; i < this.bufferLength; i++) {
        const barHeight = (this.dataArray[i] / 255) * this.canvas.height;
        
        const r = barHeight + 25 * (i / this.bufferLength);
        const g = 250 * (i / this.bufferLength);
        const b = 50;
        
        this.ctx.fillStyle = `rgb(${r},${g},${b})`;
        this.ctx.fillRect(x, this.canvas.height - barHeight, barWidth, barHeight);
        
        x += barWidth + 1;
      }
    };
    
    draw();
  }
  
  // 可视化 - 波形图
  visualizeWaveform() {
    const draw = () => {
      this.animationId = requestAnimationFrame(draw);
      
      this.analyser.getByteTimeDomainData(this.dataArray);
      
      this.ctx.fillStyle = 'rgb(0, 0, 0)';
      this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
      
      this.ctx.lineWidth = 2;
      this.ctx.strokeStyle = 'rgb(0, 255, 0)';
      this.ctx.beginPath();
      
      const sliceWidth = this.canvas.width / this.bufferLength;
      let x = 0;
      
      for (let i = 0; i < this.bufferLength; i++) {
        const v = this.dataArray[i] / 128.0;
        const y = v * this.canvas.height / 2;
        
        if (i === 0) {
          this.ctx.moveTo(x, y);
        } else {
          this.ctx.lineTo(x, y);
        }
        
        x += sliceWidth;
      }
      
      this.ctx.lineTo(this.canvas.width, this.canvas.height / 2);
      this.ctx.stroke();
    };
    
    draw();
  }
}

console.log('音频可视化器已定义');
console.log('使用方法:');
console.log(`
const canvas = document.getElementById('visualizer');
const visualizer = new AudioVisualizer(canvas);

// 从文件
visualizer.loadAudio('music.mp3').then(() => {
  visualizer.play();
});

// 或从麦克风
visualizer.connectMicrophone().then(() => {
  visualizer.visualize();
});
`);

console.log('\n💡 支持柱状图和波形图两种可视化方式');
