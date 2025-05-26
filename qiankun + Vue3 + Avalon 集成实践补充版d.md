
# 📦 qiankun + Vue3 + Avalon 集成实践补充版

这是对上篇 **qiankun 集成 vue3 与 avalon** 项目的补充版，补全一些实践中的坑、配置细节、多子应用场景，以及资源、样式隔离技巧。

## 📖 项目结构示意

```
root/
├── main-app/             # 主应用（avalon 项目 + qiankun）
│   ├── src/
│   ├── public/
│   └── index.html
├── sub-vue3/             # vue3 子应用
│   ├── src/
│   ├── public/
│   └── vite.config.js
```

## 📦 主应用配置（avalon + qiankun）

### 安装 qiankun

```bash
npm install qiankun --save
```

### src/micro-app.js

```javascript
import { registerMicroApps, start } from 'qiankun';

const apps = [
  {
    name: 'sub-vue3',
    entry: '//localhost:7100',
    container: '#subapp-viewport',
    activeRule: '/vue3'
  }
];

registerMicroApps(apps, {
  beforeLoad: [app => Promise.resolve()],
  afterMount: [app => Promise.resolve()]
});

start();
```

### 主应用页面布局

```html
<body>
  <div id="avalon-app"></div>
  <div id="subapp-viewport"></div>
</body>
```

## 📦 子应用 Vue3 项目配置完整版

### 安装依赖

```bash
npm init vite@latest sub-vue3 -- --template vue
cd sub-vue3
npm install
npm install qiankun --save
```

### vite.config.js

```javascript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  base: '/',
  server: {
    port: 7100,
    cors: true,
    origin: 'http://localhost:7100'
  },
  build: {
    outDir: 'dist',
    target: 'esnext',
    cssCodeSplit: true
  }
});
```

### main.js

```javascript
import { createApp } from 'vue';
import App from './App.vue';

let app = null;

function render(props = {}) {
  const { container } = props;
  app = createApp(App);
  app.mount(container ? container.querySelector('#app') : '#app');
}

if (!window.__POWERED_BY_QIANKUN__) {
  render();
}

export async function bootstrap() {}
export async function mount(props) { render(props); }
export async function unmount() { if (app) { app.unmount(); app = null; } }
```

### 子应用 index.html

```html
<body>
  <div id="app"></div>
</body>
```

## 🔄 页面切换方案

### 按钮实现

```html
<button onclick="goToNew()">新版</button>
<button onclick="goToOld()">旧版</button>
```

### 跳转方法

```javascript
function goToNew() {
  location.hash = '/vue3';
}

function goToOld() {
  location.href = '/#/oldpage';
}
```

## 🔒 样式隔离方案

### 安装 CSS Modules 插件

```bash
npm install vite-plugin-css-modules --save-dev
```

### vite.config.js 配置

```javascript
import cssModules from 'vite-plugin-css-modules';

export default defineConfig({
  plugins: [vue(), cssModules()]
});
```

或 scoped 样式：

```vue
<template>
  <div class="sub-app">子应用内容</div>
</template>

<style scoped>
.sub-app {
  color: red;
}
</style>
```

## 📊 多子应用扩展

```javascript
registerMicroApps([
  { name: 'sub-vue3', entry: '//localhost:7100', container: '#subapp-viewport', activeRule: '/vue3' },
  { name: 'sub-admin', entry: '//localhost:7200', container: '#subapp-viewport', activeRule: '/admin' }
]);
```

## 📌 注意事项 & 坑点

- 子应用 vite `base` 必须是 `/`
- 避免子应用修改 window 上全局变量，防止与 avalon 冲突
- 子应用和主应用 CSS 必须隔离（scoped / css-modules）
- 子应用独立端口、开发、部署
- 子应用独立/qiankun 加载均正常运行

## ✅ 总结

通过 qiankun 微前端方案，完美实现：

- 旧 avalon 页面保留
- 新 vue3 子应用独立开发部署
- 页面切换新版/旧版
- 多子应用扩展
- 平滑迁移至 vue3

## 📮 如果本文对你有帮助，欢迎 👍、⭐️、转发！
