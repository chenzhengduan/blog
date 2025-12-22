// 🔔 通知管理器
// 学习点：Notification API

class NotificationManager {
  constructor() {
    this.permission = Notification.permission;
  }
  
  async requestPermission() {
    if (this.permission === 'granted') {
      console.log('✅ 已有通知权限');
      return true;
    }
    
    try {
      const permission = await Notification.requestPermission();
      this.permission = permission;
      
      if (permission === 'granted') {
        console.log('✅ 通知权限已授予');
        return true;
      } else {
        console.log('❌ 通知权限被拒绝');
        return false;
      }
    } catch (error) {
      console.error('请求权限失败:', error);
      return false;
    }
  }
  
  async notify(title, options = {}) {
    if (this.permission !== 'granted') {
      const granted = await this.requestPermission();
      if (!granted) return null;
    }
    
    const notification = new Notification(title, {
      icon: options.icon || '/icon.png',
      body: options.body || '',
      tag: options.tag || Date.now().toString(),
      requireInteraction: options.requireInteraction || false,
      ...options
    });
    
    // 点击事件
    if (options.onClick) {
      notification.onclick = options.onClick;
    }
    
    // 自动关闭
    if (options.autoClose) {
      setTimeout(() => notification.close(), options.autoClose);
    }
    
    return notification;
  }
  
  // 快捷方法
  success(message, autoClose = 3000) {
    return this.notify('✅ 成功', {
      body: message,
      autoClose,
      tag: 'success'
    });
  }
  
  error(message, requireInteraction = true) {
    return this.notify('❌ 错误', {
      body: message,
      requireInteraction,
      tag: 'error'
    });
  }
  
  info(message) {
    return this.notify('ℹ️ 提示', {
      body: message,
      autoClose: 5000,
      tag: 'info'
    });
  }
}

const notifier = new NotificationManager();

console.log('通知管理器已定义');
console.log('使用方法:');
console.log('notifier.success("操作成功！")');
console.log('notifier.error("发生错误！")');
console.log('notifier.info("这是一条提示")');

console.log('\n💡 需要在HTTPS环境或localhost使用');
