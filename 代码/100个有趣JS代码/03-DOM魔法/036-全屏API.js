// 🎭 全屏API封装
// 学习点：Fullscreen API

const fullscreen = {
  // 进入全屏
  enter(element = document.documentElement) {
    if (element.requestFullscreen) {
      return element.requestFullscreen();
    } else if (element.webkitRequestFullscreen) {
      return element.webkitRequestFullscreen();
    } else if (element.mozRequestFullScreen) {
      return element.mozRequestFullScreen();
    } else if (element.msRequestFullscreen) {
      return element.msRequestFullscreen();
    }
  },
  
  // 退出全屏
  exit() {
    if (document.exitFullscreen) {
      return document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
      return document.webkitExitFullscreen();
    } else if (document.mozCancelFullScreen) {
      return document.mozCancelFullScreen();
    } else if (document.msExitFullscreen) {
      return document.msExitFullscreen();
    }
  },
  
  // 切换全屏
  toggle(element) {
    if (this.isFullscreen()) {
      return this.exit();
    } else {
      return this.enter(element);
    }
  },
  
  // 是否全屏
  isFullscreen() {
    return !!(
      document.fullscreenElement ||
      document.webkitFullscreenElement ||
      document.mozFullScreenElement ||
      document.msFullscreenElement
    );
  }
};

console.log('全屏API已封装');
console.log('使用方法:');
console.log('fullscreen.enter()  // 进入全屏');
console.log('fullscreen.exit()   // 退出全屏');
console.log('fullscreen.toggle() // 切换全屏');
