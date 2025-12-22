// 🔊 语音合成工具
// 学习点：Web Speech API

class TextToSpeech {
  constructor() {
    this.synth = window.speechSynthesis;
    this.voices = [];
    this.loadVoices();
  }
  
  loadVoices() {
    this.voices = this.synth.getVoices();
    
    if (this.voices.length === 0) {
      this.synth.addEventListener('voiceschanged', () => {
        this.voices = this.synth.getVoices();
      });
    }
  }
  
  speak(text, options = {}) {
    if (this.synth.speaking) {
      console.warn('正在播放中...');
      return;
    }
    
    if (!text) return;
    
    const utterance = new SpeechSynthesisUtterance(text);
    
    // 设置选项
    utterance.lang = options.lang || 'zh-CN';
    utterance.rate = options.rate || 1;
    utterance.pitch = options.pitch || 1;
    utterance.volume = options.volume || 1;
    
    // 选择声音
    if (options.voice) {
      const voice = this.voices.find(v => v.name === options.voice);
      if (voice) utterance.voice = voice;
    }
    
    // 事件回调
    if (options.onStart) {
      utterance.onstart = options.onStart;
    }
    
    if (options.onEnd) {
      utterance.onend = options.onEnd;
    }
    
    if (options.onError) {
      utterance.onerror = options.onError;
    }
    
    this.synth.speak(utterance);
  }
  
  pause() {
    this.synth.pause();
  }
  
  resume() {
    this.synth.resume();
  }
  
  stop() {
    this.synth.cancel();
  }
  
  getVoices() {
    return this.voices.map(v => ({
      name: v.name,
      lang: v.lang,
      default: v.default
    }));
  }
}

const tts = new TextToSpeech();

console.log('语音合成工具已定义');
console.log('使用方法:');
console.log(`
tts.speak('你好，世界', {
  lang: 'zh-CN',
  rate: 1.0,
  pitch: 1.0,
  volume: 1.0,
  onEnd: () => console.log('播放完成')
});
`);

console.log('\n可用语音:', tts.getVoices().slice(0, 3));
console.log('\n💡 需要在浏览器环境中使用');
