# WOnePC微前端架构方案
## 基于micro-app的微前端解决方案

> **项目背景**：WOnePC(Avalon) + WOnePC-Vue(Vue3)微前端融合方案
> 
> **技术选型**：micro-app - 京东前端团队推出的轻量级微前端框架，基于WebComponents，无关技术栈，侵入性低，可以用于任何前端框架。

## 目录

- [1. 微前端概述](#1-微前端概述)
- [2. 为什么选择micro-app](#2-为什么选择micro-app)
- [3. micro-app核心特性](#3-micro-app核心特性)
- [4. 项目架构设计](#4-项目架构设计)
- [5. 样式隔离方案](#5-样式隔离方案)
- [6. 路由方案设计](#6-路由方案设计)
- [7. 通信机制详解](#7-通信机制详解)
- [8. iframe模式应用](#8-iframe模式应用)
- [9. 开发与部署](#9-开发与部署)
- [10. 最佳实践](#10-最佳实践)
- [11. 常见问题与解决方案](#11-常见问题与解决方案)

## 1. 微前端概述

### 1.1 核心理念

微前端是一种将单体前端应用分解为多个可独立开发、部署和运行的前端应用的架构模式。微前端是借鉴了微服务的架构理念。

**三大核心原则**：
- **独立开发**：团队可使用不同技术栈独立开发
- **独立部署**：应用可独立构建、测试和部署
- **独立运行**：应用在主框架中能够独立运行

### 1.2 业务价值

- ✅ **技术栈无关**：Avalon与Vue3共存，渐进式迁移
- ✅ **团队自主**：不同团队独立开发，提高并行效率
- ✅ **代码隔离**：减少耦合，提高可维护性
- ✅ **性能优化**：按需加载，改善首屏性能
- ✅ **风险控制**：单个应用故障不影响整体


### 1.3 微前端的挑战

- 通信机制的额外开销
- 样式隔离的性能消耗
- 多应用并行加载增加网络请求
- 全局状态管理
- 性能优化
- 开发体验

## 2. 为什么选择micro-app

### 2.1 技术对比

| 方案 | 实现原理 | 学习成本 | 侵入性 | 样式隔离 | 适用场景 |
|------|---------|---------|--------|----------|----------|
| **micro-app** | Web Components | 低 | 低 | 完善 | **我们的场景** |
| qiankun | single-spa | 中 | 中 | 一般 | 复杂应用 |
| single-spa | 路由劫持 | 高 | 高 | 需配置 | 大型项目 |
| Module Federation | Webpack5 | 中 | 中 | 需配置 | 现代项目 |

### 2.2 micro-app优势

- 🎯 **使用简单**：类似iframe的HTML标签方式
- 🎯 **轻量级**：仅≈10kb (gzip)，零依赖
- 🎯 **完善沙箱**：JS沙箱 + 样式隔离 + 元素隔离
- 🎯 **技术兼容**：适配Avalon等传统框架
- 🎯 **功能丰富**：数据通信、预加载、生命周期等

## 3. micro-app核心特性

### 3.1 基础使用

#### 3.1.1 初始化配置

```javascript
// UMD方式 - 适合Avalon项目
<script src="js/libs/micro-app/index.umd.js"></script>
document.addEventListener('DOMContentLoaded', function() {
  if (window.microApp) {
    window.microApp.default.start({
      shadowDOM: false,         // 关闭shadowDOM，提高兼容性
      disableScopecss: false,   // 开启样式隔离
      disableSandbox: false,    // 开启沙箱
      destroy: true            // 卸载时销毁实例
    });
  }
});
```

#### 3.1.2 应用接入

```html
<!-- 基础模式 -->
<micro-app 
  name="vue3-app-appoint" 
  url="http://localhost:8001" 
  baseroute="/">
</micro-app>

<!-- iframe模式 - 解决兼容性问题 -->
<micro-app 
  name="vue3-app-appoint" 
  url="http://localhost:8001" 
  iframe
  disable-memory-router="true"
  disable-patch-request="true">
</micro-app>
```

### 3.2 核心参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | ✅ | 应用唯一标识，字母开头 |
| url | string | ✅ | 应用入口URL |
| iframe | boolean | ❌ | 是否使用iframe模式 |
| baseroute | string | ❌ | 基础路由路径 |
| disable-memory-router | boolean | ❌ | 禁用虚拟路由系统 |
| disable-scopecss | boolean | ❌ | 禁用样式隔离 |
| disable-sandbox | boolean | ❌ | 禁用沙箱 |

## 4. 项目架构设计

### 4.1 整体架构

```
WOnePC微前端架构
├── 主应用 (Avalon)
│   ├── 框架基础设施
|   ├── 业务功能模块
│   ├── 用户认证 & 权限
│   ├── 全局状态管理
│   └── 子应用容器
└── 子应用 (Vue3)
    ├── 业务功能模块
    ├── 内部状态管理
    └── 与主应用通信
```

### 4.2 目录结构

```
WOnePC (主应用)
├── modules/appointCourse/
│   ├── appointCourse.html    # 微前端集成页面
│   └── appointCourse.js      # 业务逻辑
├── js/libs/micro-app/        # micro-app库
└── ...

WOnePC-Vue (子应用)
├── src/
│   ├── components/
│   │   ├── AppointCourse.vue # 约课组件
│   │   └── helpDrawer.vue    # 帮助弹窗
│   ├── assets/
│   │   └── sub-element-theme.scss # Element Plus自定义主题
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
├── vite.config.ts           # 构建配置
└── package.json
```

### 4.3 版本切换机制

支持新旧版本平滑切换，降低迁移风险：

```javascript
// 版本切换逻辑
function toggleVersion() {
  showNewVersion = !showNewVersion;
  if (showNewVersion) {
    // 显示Vue3微前端版本
  } else {
    // 显示Avalon传统版本
  }
}
```

## 5. 样式隔离方案

### 5.1 样式隔离策略

micro-app提供多层次样式隔离机制：

1. **自动隔离**：自动添加应用前缀，避免样式冲突
2. **沙箱隔离**：子应用样式无法影响主应用
3. **自定义命名空间**：为组件库添加自定义前缀

### 5.2 Element Plus命名空间配置

#### 5.2.1 SCSS主题配置

```scss
// src/assets/sub-element-theme.scss
@forward 'element-plus/theme-chalk/src/mixins/config.scss' with (
  $namespace: 'sub'  // 自定义命名空间前缀
);
@import 'element-plus/theme-chalk/src/index.scss';
```

#### 5.2.2 Vite构建配置

```typescript
// vite.config.ts
export default defineConfig({
  resolve: {
    alias: { '~': path.resolve(__dirname, 'src') }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "~/assets/sub-element-theme.scss" as *;`
      }
    }
  }
});
```

#### 5.2.3 Vue组件配置

```vue
<!-- App.vue -->
<template>
  <el-config-provider namespace="sub">
    <router-view />
  </el-config-provider>
</template>
```

### 5.3 样式隔离效果

配置完成后，所有Element Plus组件的CSS类名都会带有`sub-`前缀：

```css
/* 原始类名 */
.el-button { }
.el-input { }
.el-drawer { }

/* 配置后类名 */
.sub-button { }
.sub-input { }
.sub-drawer { }
```

这样确保子应用样式与主应用完全隔离，避免样式冲突。

## 6. 路由方案设计

### 6.1 路由模式选择

#### 6.1.1 Hash路由模式（推荐）

```javascript
// Vue Router配置
import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', component: AppointCourse },
    { path: '/help', component: HelpPage }
  ]
});
```

**优点**：
- ✅ 不与主应用路由冲突
- ✅ 无需特殊配置
- ✅ 支持iframe模式

#### 6.1.2 History路由模式(不建议Wone使用)

```javascript
// 需要配置baseroute
const router = createRouter({
  history: createWebHistory('/micro-app/appoint/'),
  routes: routes
});
```

**注意事项**：
- 需要主应用配置对应的baseroute
- 服务器需要配置路由回退

### 6.2 iframe模式下的路由

当使用iframe模式时，推荐的配置：

```html
<micro-app 
  name="vue3-app-appoint" 
  url="http://localhost:8001" 
  iframe
  disable-memory-router="true"
  disable-patch-request="true"
  keep-router-state="false">
</micro-app>
```

这种配置下，子应用路由完全独立，不受主应用影响。

## 7. 通信机制详解

### 7.1 通信方式概览

| 通信方向 | 方法 | 应用场景 |
|----------|------|----------|
| 主→子 | setData / setGlobalData | 传递配置、用户信息等 |
| 子→主 | dispatch | 通知状态变化、用户操作等 |
| 全局通信 | GlobalData | 跨应用数据共享 |

### 7.2 指定应用通信

#### 7.2.1 主应用发送数据

```javascript
// 向指定子应用发送数据
window.microApp.setData('vue3-app-appoint', {
  campusList: [
    {ID: '1', Name: '校区A'},
    {ID: '2', Name: '校区B'}
  ],
  userInfo: {
    id: '001',
    name: '张三'
  }
});
```

#### 7.2.2 子应用接收数据

```javascript
// Vue3子应用
onMounted(() => {
  if (window.microApp) {
    window.microApp.addDataListener((data) => {
      console.log('接收到主应用数据:', data);
      // 处理数据...
    });
  }
});

onUnmounted(() => {
  if (window.microApp) {
    window.microApp.clearDataListener();
  }
});
```

#### 7.2.3 子应用发送数据

```javascript
// 子应用向主应用发送数据
window.microApp.dispatch({
  type: 'showHelpDrawer',
  data: {
    title: '帮助文档',
    content: '这是帮助内容'
  }
});
```

#### 7.2.4 主应用接收数据

```javascript
// 主应用监听子应用数据
document.querySelector('micro-app[name="vue3-app-appoint"]')
  .addEventListener('datachange', (e) => {
    const { type, data } = e.detail.data;
    if (type === 'showHelpDrawer') {
      showHelpDrawer(data);
    }
  });
```

### 7.3 全局通信机制

#### 7.3.1 发送全局数据

```javascript
// 发送全局数据，所有子应用都能接收
window.microApp.setGlobalData({
  theme: 'dark',
  language: 'zh-CN',
  permissions: ['read', 'write']
});
```

#### 7.3.2 接收全局数据

```javascript
// 子应用接收全局数据
window.microApp.addGlobalDataListener((data) => {
  console.log('接收到全局数据:', data);
  // 处理全局配置变化...
});
```

### 7.4 通信最佳实践

1. **数据格式标准化**：统一使用JSON格式
2. **错误处理**：添加通信异常处理
3. **类型定义**：使用TypeScript定义通信接口
4. **避免频繁通信**：合并数据批量发送
5. **生命周期管理**：及时清理监听器

## 8. iframe模式应用

### 8.1 iframe模式优势

iframe模式是micro-app提供的一种特殊运行模式，具有以下优势：

- 🔒 **完全隔离**：样式、JS、DOM完全隔离
- 🛡️ **安全性高**：沙箱级别的安全隔离
- 🔧 **兼容性好**：解决复杂应用的兼容性问题
- 🎯 **稳定性强**：不受主应用环境影响

### 8.2 iframe模式配置

```html
<micro-app 
  name="vue3-app-appoint" 
  url="http://localhost:8001" 
  iframe
  disable-memory-router="true"
  disable-patch-request="true"
  keep-router-state="false"
  disable-scopecss="false"
  disable-sandbox="false">
</micro-app>
```


#### 8.3.2 通信限制

iframe模式下某些通信API可能受限，需要使用兼容性方案：

```javascript
// 兼容性通信方案
if (window.microApp && window.microApp.EventCenterForMicroApp) {
  const eventCenter = new window.microApp.EventCenterForMicroApp('vue3-app-appoint');
  eventCenter.dispatch({ type: 'data', data: {} });
}
```

## 9. 开发与部署

### 9.1 本地开发环境

#### 9.1.1 主应用配置

```javascript
// 开发环境指向本地服务
<micro-app 
  name="vue3-app-appoint" 
  url="http://localhost:8001"   // Vue3开发服务器
  iframe>
</micro-app>
```

#### 9.1.2 子应用配置

```typescript
// vite.config.ts - 开发环境配置
export default defineConfig({
  server: {
    port: 8001,
    cors: true,          // 允许跨域
    headers: {
      'Access-Control-Allow-Origin': '*'
    }
  }
});
```

### 9.2 生产环境部署

#### 9.2.1 独立部署方案

```bash
# 子应用独立构建
cd WOnePC-Vue
npm run build

# 部署到独立域名或路径
# 方案1：独立域名 https://vue.yourcompany.com
# 方案2：子路径 https://yourcompany.com/vue-app/
```

#### 9.2.2 主应用配置更新

```html
<!-- 生产环境配置 -->
<micro-app 
  name="vue3-app-appoint" 
  url="https://vue.yourcompany.com"
  iframe>
</micro-app>
```

### 9.3 版本管理策略

1. **语义化版本**：遵循semver规范
2. **版本兼容性**：维护兼容性矩阵
3. **灰度发布**：支持版本切换机制
4. **回滚方案**：快速回滚到稳定版本

## 10. 最佳实践

### 10.1 开发规范

#### 10.1.1 命名规范

```javascript
// ✅ 应用命名：kebab-case
name="vue3-app-appoint"

// ✅ 事件命名：camelCase
type: "showHelpDrawer"

// ✅ 样式命名：BEM + 应用前缀
.v3-appoint__course-item
```

#### 10.1.2 文件组织

```
src/
├── components/          # 业务组件
├── assets/             # 样式资源
│   └── sub-element-theme.scss
├── utils/              # 工具函数
├── types/              # TypeScript类型定义
└── main.js            # 入口文件
```

### 10.2 性能优化

#### 10.2.1 预加载策略

```javascript
// 预加载子应用
microApp.preFetch([
  { name: 'vue3-app-appoint', url: 'http://localhost:8001' }
]);
```

#### 10.2.2 资源优化

- 📦 **代码分割**：按页面拆分chunk
- 🗜️ **资源压缩**：启用gzip压缩
- 🔄 **缓存策略**：合理设置缓存头
- 📱 **按需加载**：组件懒加载

### 10.3 错误处理

#### 10.3.1 应用级错误处理

```javascript
// 子应用错误监听
document.querySelector('micro-app[name="vue3-app-appoint"]')
  .addEventListener('error', (e) => {
    console.error('子应用加载失败:', e);
    // 显示降级UI或错误提示
    showFallbackUI();
  });
```

#### 10.3.2 通信错误处理

```javascript
// 通信异常处理
try {
  window.microApp.setData('vue3-app-appoint', data);
} catch (error) {
  console.error('通信失败:', error);
  // 使用备用通信方案
}
```

## 11. 常见问题与解决方案

### 11.1 样式相关问题

**Q1: Element Plus样式与主应用冲突？**

A: 使用自定义命名空间解决：

```scss
// 配置自定义命名空间
@forward 'element-plus/theme-chalk/src/mixins/config.scss' with (
  $namespace: 'sub'
);
```

**Q2: 子应用样式无法覆盖主应用固定定位元素？**

A: 在iframe模式下使用通信机制，让主应用显示弹窗。
B: 改造一下主应用硬显示的部分

### 11.2 路由相关问题

**Q3: 子应用路由与主应用冲突？**

A: 使用Hash路由或配置独立的baseroute：

```javascript
// 方案1：Hash路由
history: createWebHashHistory()

// 方案2：独立路径
baseroute="/micro-app/appoint/"
```

### 11.3 通信相关问题

**Q4: 通信数据丢失？**

A: 使用autoTrigger确保数据接收：

```javascript
window.microApp.addDataListener(callback, true); // autoTrigger=true
```

**Q5: iframe模式下通信异常？**

A: 使用EventCenterForMicroApp兼容方案：

```javascript
if (window.microApp.EventCenterForMicroApp) {
  const eventCenter = new window.microApp.EventCenterForMicroApp('app-name');
  eventCenter.dispatch(data);
}
```

### 11.4 性能相关问题

**Q6: 首屏加载慢？**

A: 采用预加载 + 懒加载策略：

```javascript
// 预加载关键应用
microApp.preFetch([{name: 'vue3-app-appoint', url: '...'}]);

// 非关键应用懒加载
<micro-app v-if="shouldLoad" name="..." url="..."></micro-app>
```

---

## 总结

WOnePC微前端架构方案基于micro-app框架，实现了Avalon与Vue3的完美融合。通过完善的样式隔离、灵活的路由方案、可靠的通信机制和iframe模式支持，为团队提供了一个稳定、高效的微前端解决方案。

**核心优势**：
- 🎯 **技术栈融合**：Avalon + Vue3 无缝集成
- 🔒 **完善隔离**：样式、路由、沙箱全方位隔离  
- 🚀 **开发效率**：团队并行开发，独立部署
- 🛡️ **稳定可靠**：iframe模式提供最高级别隔离
- 📈 **渐进升级**：支持版本切换，降低迁移风险

该方案为公司前端技术栈升级提供了可行的渐进式迁移路径，在保证业务稳定的前提下，实现了技术栈的现代化升级。


## 参考资料

- [浅析微前端 micro-app 框架](https://juejin.cn/post/7472210592540065833)
- [微前端micro-app踩坑记录](https://juejin.cn/post/7273072756157481018)
- [万字长文: 如何使用micro-app实现微前端应用](https://juejin.cn/post/7094163332495573023)
- [微前端（micro-app）使用手册](https://juejin.cn/post/7248496133873745976)
- [浅入深出的微前端MicroApp | 京东云技术团队](https://juejin.cn/post/7280786332711632954)