// 🎮 游戏手柄支持
// 学习点：Gamepad API

class GamepadManager {
  constructor() {
    this.gamepads = {};
    this.listeners = [];
    this.init();
  }
  
  init() {
    window.addEventListener('gamepadconnected', (e) => {
      console.log('🎮 手柄已连接:', e.gamepad.id);
      this.gamepads[e.gamepad.index] = e.gamepad;
      this.notifyListeners('connected', e.gamepad);
    });
    
    window.addEventListener('gamepaddisconnected', (e) => {
      console.log('❌ 手柄已断开:', e.gamepad.id);
      delete this.gamepads[e.gamepad.index];
      this.notifyListeners('disconnected', e.gamepad);
    });
  }
  
  getGamepads() {
    return Object.values(navigator.getGamepads()).filter(gp => gp);
  }
  
  getButtonState(gamepadIndex = 0, buttonIndex) {
    const gamepads = this.getGamepads();
    if (!gamepads[gamepadIndex]) return null;
    
    const button = gamepads[gamepadIndex].buttons[buttonIndex];
    return {
      pressed: button.pressed,
      touched: button.touched,
      value: button.value
    };
  }
  
  getAxes(gamepadIndex = 0) {
    const gamepads = this.getGamepads();
    if (!gamepads[gamepadIndex]) return null;
    
    return Array.from(gamepads[gamepadIndex].axes);
  }
  
  pollGamepad(callback, interval = 16) {
    const poll = () => {
      const gamepads = this.getGamepads();
      
      if (gamepads.length > 0) {
        const state = {
          buttons: gamepads[0].buttons.map((btn, i) => ({
            index: i,
            pressed: btn.pressed,
            value: btn.value
          })),
          axes: this.getAxes(0)
        };
        
        callback(state);
      }
      
      setTimeout(poll, interval);
    };
    
    poll();
  }
  
  on(event, callback) {
    this.listeners.push({ event, callback });
  }
  
  notifyListeners(event, data) {
    this.listeners
      .filter(l => l.event === event)
      .forEach(l => l.callback(data));
  }
}

const gamepadManager = new GamepadManager();

console.log('游戏手柄管理器已定义');
console.log('使用方法:');
console.log(`
gamepadManager.on('connected', (gamepad) => {
  console.log('手柄连接:', gamepad.id);
});

gamepadManager.pollGamepad((state) => {
  console.log('按钮状态:', state.buttons);
  console.log('摇杆位置:', state.axes);
}, 100);
`);

console.log('\n💡 支持Xbox、PlayStation等标准手柄');
console.log('💡 需要连接实体手柄并按任意键激活');
