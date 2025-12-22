// 🎯 依赖注入容器
// 学习点：设计模式与IoC

class Container {
  constructor() {
    this.services = new Map();
    this.singletons = new Map();
  }
  
  // 注册服务
  register(name, factory, options = {}) {
    this.services.set(name, {
      factory,
      singleton: options.singleton || false,
      dependencies: options.dependencies || []
    });
  }
  
  // 解析服务
  resolve(name) {
    const service = this.services.get(name);
    
    if (!service) {
      throw new Error(`服务 "${name}" 未注册`);
    }
    
    // 单例模式
    if (service.singleton && this.singletons.has(name)) {
      return this.singletons.get(name);
    }
    
    // 解析依赖
    const dependencies = service.dependencies.map(dep => this.resolve(dep));
    
    // 创建实例
    const instance = service.factory(...dependencies);
    
    // 缓存单例
    if (service.singleton) {
      this.singletons.set(name, instance);
    }
    
    return instance;
  }
  
  // 检查是否已注册
  has(name) {
    return this.services.has(name);
  }
}

// 示例使用
const container = new Container();

// 注册服务
container.register('logger', () => {
  return {
    log: (msg) => console.log(`[LOG] ${msg}`)
  };
}, { singleton: true });

container.register('database', (logger) => {
  logger.log('数据库连接已建立');
  return {
    query: (sql) => {
      logger.log(`执行SQL: ${sql}`);
      return { results: [] };
    }
  };
}, { dependencies: ['logger'] });

container.register('userService', (db, logger) => {
  return {
    getUser: (id) => {
      logger.log(`获取用户: ${id}`);
      return db.query(`SELECT * FROM users WHERE id = ${id}`);
    }
  };
}, { dependencies: ['database', 'logger'] });

console.log('依赖注入容器演示\n');

const userService = container.resolve('userService');
userService.getUser(123);

console.log('\n💡 优点: 解耦、可测试、易维护');
