// 🌍 地理位置服务
// 学习点：Geolocation API

class LocationService {
  constructor() {
    this.watchId = null;
  }
  
  // 检查支持
  isSupported() {
    return 'geolocation' in navigator;
  }
  
  // 获取当前位置
  async getCurrentPosition(options = {}) {
    if (!this.isSupported()) {
      throw new Error('浏览器不支持地理位置API');
    }
    
    const defaultOptions = {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    };
    
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve(this.formatPosition(position));
        },
        (error) => {
          reject(this.formatError(error));
        },
        { ...defaultOptions, ...options }
      );
    });
  }
  
  // 持续监听位置
  watchPosition(callback, options = {}) {
    if (!this.isSupported()) {
      throw new Error('浏览器不支持地理位置API');
    }
    
    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        callback(null, this.formatPosition(position));
      },
      (error) => {
        callback(this.formatError(error), null);
      },
      options
    );
    
    return this.watchId;
  }
  
  // 停止监听
  clearWatch() {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }
  
  // 格式化位置信息
  formatPosition(position) {
    return {
      latitude: position.coords.latitude,
      longitude: position.coords.longitude,
      accuracy: position.coords.accuracy,
      altitude: position.coords.altitude,
      altitudeAccuracy: position.coords.altitudeAccuracy,
      heading: position.coords.heading,
      speed: position.coords.speed,
      timestamp: position.timestamp
    };
  }
  
  // 格式化错误
  formatError(error) {
    const errors = {
      1: '用户拒绝了地理位置请求',
      2: '位置信息不可用',
      3: '请求超时'
    };
    
    return {
      code: error.code,
      message: errors[error.code] || error.message
    };
  }
  
  // 计算两点距离（哈弗辛公式）
  calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // 地球半径（千米）
    const dLat = this.toRadians(lat2 - lat1);
    const dLon = this.toRadians(lon2 - lon1);
    
    const a = 
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRadians(lat1)) * Math.cos(this.toRadians(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
  
  toRadians(degrees) {
    return degrees * (Math.PI / 180);
  }
}

const locationService = new LocationService();

console.log('地理位置服务已定义');
console.log('使用方法:');
console.log(`
// 获取当前位置
locationService.getCurrentPosition()
  .then(pos => console.log('位置:', pos))
  .catch(err => console.error('错误:', err));

// 持续监听
locationService.watchPosition((error, position) => {
  if (error) {
    console.error('错误:', error);
  } else {
    console.log('位置更新:', position);
  }
});

// 计算距离
const distance = locationService.calculateDistance(
  39.9042, 116.4074,  // 北京
  31.2304, 121.4737   // 上海
);
console.log('距离:', distance, 'km');
`);

console.log('\n💡 需要HTTPS或localhost环境');
console.log('💡 用户需要授权位置访问');
