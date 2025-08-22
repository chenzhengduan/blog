# Sentry 版本升级指南

> 前端项目 Sentry 版本升级完整指南 - 从低版本安全升级到最新版本

---

## 📋 升级前准备

### 1. 当前环境评估

#### 版本检查
```bash
# 检查当前 Sentry 版本
npm list @sentry/browser
# 或检查 package.json
cat package.json | grep sentry

# 检查相关依赖
npm list | grep sentry
```

#### 项目信息收集
- **框架版本**：React/Vue/Angular 版本
- **构建工具**：Webpack/Vite/Rollup 版本
- **Node.js版本**：`node --version`
- **TypeScript版本**：`tsc --version`
- **当前 Sentry 配置**：记录现有配置参数

### 2. 兼容性检查表

| Sentry版本 | Node.js要求 | TypeScript要求 | 主要特性 | 破坏性变更 |
|-----------|------------|---------------|----------|----------|
| v6.x | >=8 | >=3.0 | 基础错误监控 | - |
| v7.x | >=10 | >=3.7 | 性能监控、新API | 🔴 重大变更 |
| v8.x | >=12 | >=4.0 | 优化性能、类型改进 | 🟡 部分变更 |
| v9.x | >=14 | >=4.5 | 现代化、ESM支持 | 🟡 部分变更 |

### 3. 备份策略

```bash
# 1. 创建升级分支
git checkout -b sentry-upgrade-v8

# 2. 备份当前配置
cp src/utils/sentry.js src/utils/sentry.backup.js

# 3. 记录当前依赖
npm list > dependencies-backup.txt
```

---

## 🚀 分版本升级指南

### v6 → v7 升级（重大变更）

#### 主要变更
- **包名变更**：`raven-js` → `@sentry/browser`
- **初始化方式**：`Raven.config()` → `Sentry.init()`
- **API重构**：所有API都有变化
- **配置结构**：配置选项完全重构

#### 升级步骤

**1. 卸载旧包，安装新包**
```bash
# 卸载旧版本
npm uninstall raven-js

# 安装新版本
npm install @sentry/browser@^7.0.0
```

**2. 更新导入语句**
```typescript
// 旧版本 (v6)
import Raven from 'raven-js';

// 新版本 (v7)
import * as Sentry from '@sentry/browser';
```

**3. 更新初始化代码**
```typescript
// 旧版本 (v6)
Raven.config('YOUR_DSN', {
  environment: 'production',
  release: '1.0.0'
}).install();

// 新版本 (v7)
Sentry.init({
  dsn: 'YOUR_DSN',
  environment: 'production',
  release: '1.0.0'
});
```

**4. 更新错误捕获**
```typescript
// 旧版本 (v6)
Raven.captureException(error);
Raven.captureMessage('Something went wrong');
Raven.setUserContext({ id: '123', email: 'user@example.com' });
Raven.setTagsContext({ module: 'payment' });

// 新版本 (v7)
Sentry.captureException(error);
Sentry.captureMessage('Something went wrong');
Sentry.setUser({ id: '123', email: 'user@example.com' });
Sentry.setTag('module', 'payment');
```

**5. 更新配置选项**
```typescript
// 旧版本 (v6)
Raven.config('DSN', {
  whitelistUrls: [/example\.com/],
  ignoreErrors: ['Script error'],
  dataCallback: function(data) {
    // 数据处理
    return data;
  }
}).install();

// 新版本 (v7)
Sentry.init({
  dsn: 'DSN',
  allowUrls: [/example\.com/],
  ignoreErrors: ['Script error'],
  beforeSend: function(event) {
    // 数据处理
    return event;
  }
});
```

### v7 → v8 升级（优化升级）

#### 主要变更
- **性能监控优化**：BrowserTracing 集成改进
- **类型定义改进**：更好的 TypeScript 支持
- **配置选项调整**：部分配置项重命名
- **包大小优化**：减少了包体积

#### 升级步骤

**1. 更新包版本**
```bash
npm install @sentry/browser@^8.0.0
```

**2. 更新性能监控配置**
```typescript
// v7 配置
import { Integrations } from '@sentry/browser';

Sentry.init({
  dsn: 'DSN',
  integrations: [
    new Integrations.BrowserTracing({
      tracingOrigins: ['localhost', 'my-site.com']
    })
  ],
  tracesSampleRate: 1.0
});

// v8 配置
import { BrowserTracing } from '@sentry/browser';

Sentry.init({
  dsn: 'DSN',
  integrations: [
    new BrowserTracing({
      tracePropagationTargets: ['localhost', 'my-site.com']
    })
  ],
  tracesSampleRate: 1.0
});
```

**3. 更新类型定义**
```bash
# 如果使用 TypeScript
npm install @types/sentry__browser@latest
```

### v8 → v9 升级（现代化升级）

#### 主要变更
- **ESM 支持**：原生 ES 模块支持
- **Node.js 要求**：最低 Node.js 14+
- **现代化 API**：更符合现代 JavaScript 标准
- **性能提升**：启动速度和运行时性能优化

#### 升级步骤

**1. 检查 Node.js 版本**
```bash
node --version  # 确保 >= 14.0.0
```

**2. 更新包版本**
```bash
npm install @sentry/browser@^9.0.0
```

**3. 更新构建配置（如果使用 ESM）**
```javascript
// vite.config.js 或 webpack.config.js
// 可能需要调整模块解析配置
```

---

## 🔧 升级过程中的常见问题

### 问题1：类型错误

**错误信息：**
```
TS2307: Cannot find module '@sentry/browser' or its corresponding type declarations.
```

**解决方案：**
```bash
# 清理缓存并重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install

# 更新类型定义
npm install @types/sentry__browser@latest
```

### 问题2：构建失败

**错误信息：**
```
Module not found: Error: Can't resolve '@sentry/browser'
```

**解决方案：**
```javascript
// webpack.config.js
module.exports = {
  resolve: {
    alias: {
      // 如果需要，添加别名
    },
    fallback: {
      // 可能需要的 polyfill
    }
  }
};
```

### 问题3：运行时错误

**错误信息：**
```
Sentry.init is not a function
```

**解决方案：**
```typescript
// 检查导入方式
// 错误的导入
import Sentry from '@sentry/browser';

// 正确的导入
import * as Sentry from '@sentry/browser';
// 或
import { init, captureException } from '@sentry/browser';
```

### 问题4：配置不生效

**可能原因：**
- 配置选项名称变更
- 配置结构调整
- 初始化时机问题

**解决方案：**
```typescript
// 确保在应用启动最早期初始化
// main.js 或 index.js 的最顶部
import * as Sentry from '@sentry/browser';

Sentry.init({
  // 配置项
});

// 然后再导入其他模块
import App from './App';
```

---

## ✅ 升级验证清单

### 功能验证

```typescript
// 升级验证脚本
const verifySentryUpgrade = () => {
  console.log('开始 Sentry 升级验证...');
  
  // 1. 基础错误捕获测试
  try {
    throw new Error('升级测试 - 基础错误捕获');
  } catch (error) {
    Sentry.captureException(error);
    console.log('✅ 基础错误捕获正常');
  }
  
  // 2. 用户上下文测试
  Sentry.setUser({
    id: 'upgrade-test-user',
    email: 'test@upgrade.com',
    username: 'upgrade-tester'
  });
  console.log('✅ 用户上下文设置正常');
  
  // 3. 标签和额外信息测试
  Sentry.setTag('upgrade-test', 'v8.0.0');
  Sentry.setExtras({
    upgradeDate: new Date().toISOString(),
    previousVersion: 'v7.x.x'
  });
  console.log('✅ 标签和额外信息设置正常');
  
  // 4. 手动消息测试
  Sentry.captureMessage('升级测试 - 手动消息', 'info');
  console.log('✅ 手动消息捕获正常');
  
  // 5. 性能监控测试（如果启用）
  const transaction = Sentry.startTransaction({
    name: 'upgrade-test-transaction'
  });
  setTimeout(() => {
    transaction.finish();
    console.log('✅ 性能监控正常');
  }, 100);
  
  console.log('Sentry 升级验证完成！');
};

// 在浏览器控制台运行
// verifySentryUpgrade();
```

### 验证检查项

- [ ] **错误捕获**：手动触发错误，检查 Sentry 后台是否收到
- [ ] **用户信息**：验证用户上下文是否正确设置
- [ ] **标签信息**：检查自定义标签是否正常
- [ ] **性能监控**：验证性能数据是否收集（如果启用）
- [ ] **告警通知**：测试告警规则是否正常工作
- [ ] **Source Maps**：确认错误堆栈能正确映射到源码
- [ ] **过滤规则**：验证错误过滤和采样配置
- [ ] **集成功能**：检查与其他工具的集成（如 Redux、Router）

---

## 📊 升级后优化建议

### 1. 性能优化

```typescript
// 优化配置示例
Sentry.init({
  dsn: 'YOUR_DSN',
  
  // 采样率优化
  sampleRate: 0.1, // 生产环境降低采样率
  tracesSampleRate: 0.1, // 性能监控采样率
  
  // 错误过滤
  ignoreErrors: [
    // 忽略无意义的错误
    'Script error.',
    'Non-Error promise rejection captured',
    /ChunkLoadError/,
  ],
  
  // 数据脱敏
  beforeSend(event) {
    // 移除敏感信息
    if (event.request?.headers) {
      delete event.request.headers.Authorization;
    }
    return event;
  },
  
  // 限制数据大小
  maxValueLength: 1000,
  normalizeDepth: 3,
});
```

### 2. 监控策略

```typescript
// 环境相关配置
const sentryConfig = {
  dsn: process.env.REACT_APP_SENTRY_DSN,
  environment: process.env.NODE_ENV,
  release: process.env.REACT_APP_VERSION,
  
  // 开发环境配置
  ...(process.env.NODE_ENV === 'development' && {
    sampleRate: 1.0,
    tracesSampleRate: 1.0,
    debug: true,
  }),
  
  // 生产环境配置
  ...(process.env.NODE_ENV === 'production' && {
    sampleRate: 0.1,
    tracesSampleRate: 0.1,
    debug: false,
  }),
};

Sentry.init(sentryConfig);
```

### 3. 团队规范

- **版本管理**：建立 Sentry 版本升级的定期评估机制
- **配置统一**：制定团队统一的 Sentry 配置模板
- **升级流程**：建立标准的升级测试和验证流程
- **文档维护**：及时更新团队的 Sentry 使用文档

---

## 🔄 回滚方案

### 快速回滚步骤

```bash
# 1. 切换到备份分支
git checkout main

# 2. 恢复依赖版本
npm install @sentry/browser@6.19.7  # 替换为之前的版本

# 3. 恢复配置文件
cp src/utils/sentry.backup.js src/utils/sentry.js

# 4. 重新构建和部署
npm run build
```

### 回滚决策标准

- **错误率上升**：升级后错误率显著增加
- **性能下降**：页面加载或运行性能明显下降
- **功能异常**：关键功能出现异常
- **构建失败**：无法正常构建或部署

---

## 📚 参考资源

- [Sentry 官方升级指南](https://docs.sentry.io/platforms/javascript/migration/)
- [Sentry JavaScript SDK 变更日志](https://github.com/getsentry/sentry-javascript/blob/master/CHANGELOG.md)
- [Sentry 配置参考](https://docs.sentry.io/platforms/javascript/configuration/)
- [团队 Sentry 配置模板](./sentry分享.md)

---

*最后更新：2024年1月*
*版本：v1.0*