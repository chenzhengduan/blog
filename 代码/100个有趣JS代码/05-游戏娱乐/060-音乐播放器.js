// 🎵 简单音符序列播放器
// 学习点：Web Audio API

class MusicPlayer {
  constructor() {
    this.audioContext = typeof AudioContext !== 'undefined' 
      ? new AudioContext() 
      : null;
    
    this.notes = {
      'C': 261.63,
      'D': 293.66,
      'E': 329.63,
      'F': 349.23,
      'G': 392.00,
      'A': 440.00,
      'B': 493.88,
      'C2': 523.25
    };
  }
  
  playNote(frequency, duration = 0.5) {
    if (!this.audioContext) {
      console.log('🎵 播放音符:', frequency, 'Hz');
      return;
    }
    
    const oscillator = this.audioContext.createOscillator();
    const gainNode = this.audioContext.createGain();
    
    oscillator.connect(gainNode);
    gainNode.connect(this.audioContext.destination);
    
    oscillator.frequency.value = frequency;
    oscillator.type = 'sine';
    
    gainNode.gain.setValueAtTime(0.3, this.audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(
      0.01,
      this.audioContext.currentTime + duration
    );
    
    oscillator.start(this.audioContext.currentTime);
    oscillator.stop(this.audioContext.currentTime + duration);
  }
  
  async playMelody(notes, tempo = 500) {
    for (const note of notes) {
      if (this.notes[note]) {
        this.playNote(this.notes[note]);
        await new Promise(resolve => setTimeout(resolve, tempo));
      }
    }
  }
}

const player = new MusicPlayer();

console.log('音乐播放器已初始化');
console.log('可用音符:', Object.keys(player.notes));
console.log('\n示例旋律:');

// 小星星
const twinkleTwinkle = ['C', 'C', 'G', 'G', 'A', 'A', 'G'];
console.log('小星星:', twinkleTwinkle.join(' '));

console.log('\n💡 在浏览器中运行可播放声音');
console.log('player.playMelody(["C", "D", "E", "F", "G"])');
