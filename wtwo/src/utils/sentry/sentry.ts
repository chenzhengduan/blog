import * as Sentry from "@sentry/vue";

// 工具方法封装
class Logger {
  /**
   * 设置用户上下文
   * @param user 用户信息对象
   * @param user.id 用户ID
   * @param user.username 用户名
   * @param user.email 用户邮箱
   * 
   * @example
   * // 设置当前登录用户信息
   * Logger.setUser({
   *   id: 'user123',
   *   username: 'john_doe',
   *   email: 'john@example.com'
   * });
   * 
   * */
  static setUser(user: { id?: string; username?: string; email?: string }) {
    // Sentry.setUser(user);
  }

  /**
   * 设置单个标签
   * @param key 标签键名
   * @param value 标签值
   * 
   * @example
   * // 设置版本标签
   * Logger.setTag('version', '1.0.0');
   * // 设置功能模块标签
   * Logger.setTag('module', 'user-management');
   */
  static setTag(key: string, value: string) {
    // Sentry.setTag(key, value);
  }

  /**
   * 批量设置多个标签
   * @param tags 标签键值对对象
   * 
   * @example
   * // 一次性设置多个标签
   * Logger.setTags({
   *   environment: 'production',
   *   version: '1.0.0',
   *   module: 'user-management',
   *   feature: 'advanced-search'
   * });
   */
  static setTags(tags: Record<string, string>) {
    // Sentry.setTags(tags);
  }

  /**
   * 附加额外上下文数据
   * @param extras 额外数据对象
   * 
   * @example
   * // 添加请求相关信息
   * Logger.setExtras({
   *   requestId: 'req-12345',
   *   endpoint: '/api/users',
   *   method: 'POST',
   *   userId: 'user123'
   * });
   * 
   * // 添加业务上下文
   * Logger.setExtras({
   *   orderId: 'order-67890',
   *   paymentMethod: 'credit-card',
   *   amount: 99.99,
   *   currency: 'USD'
   * });
   */
  static setExtras(extras: Record<string, any>) {
    // Sentry.setExtras(extras);
  }

  /**
   * 捕获并上报异常错误
   * @param err 错误对象或错误信息
   * @param context 可选的额外上下文信息
   * 
   * @example
   * // 捕获 JavaScript 错误
   * try {
   *   // 可能出错的代码
   *   const result = riskyOperation();
   * } catch (error) {
   *   Logger.error(error, {
   *     operation: 'riskyOperation',
   *     input: { param1: 'value1' },
   *     userId: 'user123'
   *   });
   * }
   * 
   * // 捕获自定义错误
   * Logger.error(new Error('用户权限不足'), {
   *   action: 'delete-user',
   *   targetUserId: 'user456',
   *   currentUser: 'admin123',
   *   timestamp: new Date().toISOString()
   * });
   */
  static error(err: any, context?: Record<string, any>) {
    // Sentry.captureException(err, { extra: context });
  }

  /**
   * 捕获并上报普通信息消息
   * @param message 信息消息内容
   * @param context 可选的额外上下文信息
   * 
   * @example
   * // 记录用户操作
   * Logger.info('用户登录成功', {
   *   userId: 'user123',
   *   loginTime: new Date().toISOString(),
   *   userAgent: navigator.userAgent
   * });
   * 
   * // 记录业务事件
   * Logger.info('订单创建成功', {
   *   orderId: 'order-12345',
   *   customerId: 'customer-67890',
   *   totalAmount: 299.99,
   *   items: ['product-a', 'product-b']
   * });
   * 
   * // 记录系统状态
   * Logger.info('系统启动完成', {
   *   startupTime: 2500,
   *   services: ['auth', 'database', 'cache'],
   *   version: '2.1.0'
   * });
   */
  static info(message: string, context?: Record<string, any>) {
    // if (context) Sentry.setExtras(context);
    // Sentry.captureMessage(message, "info");
  }

  /**
   * 捕获并上报警告消息
   * @param message 警告消息内容
   * @param context 可选的额外上下文信息
   * 
   * @example
   * // 记录性能警告
   * Logger.warn('API响应时间过长', {
   *   endpoint: '/api/users/search',
   *   responseTime: 5000,
   *   threshold: 2000,
   *   userId: 'user123',
   *   query: { searchTerm: 'john', limit: 100 }
   * });
   * 
   * // 记录业务警告
   * Logger.warn('用户尝试访问受限资源', {
   *   userId: 'user123',
   *   resource: '/admin/users',
   *   userRole: 'user',
   *   requiredRole: 'admin',
   *   ipAddress: '192.168.1.100'
   * });
   * 
   * // 记录系统警告
   * Logger.warn('内存使用率过高', {
   *   memoryUsage: '85%',
   *   threshold: '80%',
   *   availableMemory: '2GB',
   *   totalMemory: '16GB',
   *   timestamp: new Date().toISOString()
   * });
   */
  static warn(message: string, context?: Record<string, any>) {
    // if (context) Sentry.setExtras(context);
    // Sentry.captureMessage(message, "warning");
  }
}

// 全局 Promise 错误监听
window.addEventListener("unhandledrejection", (event) => {
  // Logger.error(event.reason, { type: "UNHANDLED_PROMISE" });
});

// 设置用户信息
// Logger.setUser({
//     username: 'chenzhengduan',
//     email: 'chenzhengduan@xiaogj.com',
//     id: '1234567890'
// })

// 设置版本信息
// Logger.setTag('version', '1.0.0')
// Logger.setTag('release', 'v1.0.0')  // 也可以设置 release 标签

// 设置其他标签
// Logger.setTag('module', 'main.ts')
// Logger.setTag('environment', 'dev')

// 设置额外上下文数据
// Logger.setExtras({
//     name: 'chenzhengduan',
//     age: 18,
//     gender: 'male',
//     email: 'chenzhengduan@xiaogj.com',
//     id: '1234567890',
//     // 添加版本相关信息
//     appVersion: '1.0.0',
//     buildNumber: '20240813.001'
// })

// 发送日志消息
// Logger.info('又是我Chad', {
//     name: 'chenzhengduan',
//     age: 18,
//     gender: 'male',
//     email: 'chenzhengduan@xiaogj.com',
//     id: '1234567890'
// })


// // 可附加上下文信息
// Sentry.captureMessage("我是Chad", {
//     level: "info",
//     tags: { module: "init", page: "main.ts" },
//     extra: { userId: "chenzhengduan", action: "init" }
//   });
export default Logger;
