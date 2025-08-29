# 前端微前端架构深度实践：从qiankun到Module Federation的技术演进

> 在现代前端开发中，随着应用规模的不断扩大和团队协作的复杂化，微前端架构已成为解决大型前端应用开发和维护难题的重要方案。本文将深入探讨微前端架构的核心概念、主流实现方案，以及从qiankun到Module Federation的技术演进路径。

## 1. 微前端架构概述

### 1.1 微前端的核心理念

微前端是一种将微服务理念扩展到前端开发的架构模式，它将大型前端应用拆分为多个独立的、可部署的前端应用，每个应用负责特定的业务功能。

```typescript
// 微前端架构核心接口定义
interface MicroFrontendApp {
  name: string;
  entry: string;
  container: string;
  activeRule: string | ((location: Location) => boolean);
  props?: Record<string, any>;
}

interface MicroFrontendFramework {
  // 注册微应用
  registerMicroApps(apps: MicroFrontendApp[]): void;
  
  // 启动微前端框架
  start(options?: StartOptions): void;
  
  // 手动加载微应用
  loadMicroApp(app: MicroFrontendApp): Promise<MicroApp>;
  
  // 卸载微应用
  unloadMicroApp(app: MicroApp): Promise<void>;
  
  // 应用间通信
  initGlobalState(state: Record<string, any>): MicroAppStateActions;
}
```

### 1.2 微前端架构优势

#### 技术栈无关性

```typescript
// 不同技术栈的微应用配置
const microApps: MicroFrontendApp[] = [
  {
    name: 'react-app',
    entry: '//localhost:3001',
    container: '#react-container',
    activeRule: '/react',
    props: {
      framework: 'React 18',
      buildTool: 'Vite'
    }
  },
  {
    name: 'vue-app',
    entry: '//localhost:3002',
    container: '#vue-container',
    activeRule: '/vue',
    props: {
      framework: 'Vue 3',
      buildTool: 'Webpack'
    }
  },
  {
    name: 'angular-app',
    entry: '//localhost:3003',
    container: '#angular-container',
    activeRule: '/angular',
    props: {
      framework: 'Angular 15',
      buildTool: 'Angular CLI'
    }
  }
];
```

#### 独立开发与部署

```typescript
// 微应用生命周期管理
class MicroAppLifecycle {
  private apps: Map<string, MicroApp> = new Map();
  private loadingApps: Set<string> = new Set();
  
  async loadApp(appConfig: MicroFrontendApp): Promise<MicroApp> {
    const { name, entry } = appConfig;
    
    // 防止重复加载
    if (this.loadingApps.has(name)) {
      throw new Error(`App ${name} is already loading`);
    }
    
    if (this.apps.has(name)) {
      return this.apps.get(name)!;
    }
    
    this.loadingApps.add(name);
    
    try {
      // 获取应用资源
      const appAssets = await this.fetchAppAssets(entry);
      
      // 创建沙箱环境
      const sandbox = this.createSandbox(name);
      
      // 解析并执行应用代码
      const app = await this.executeApp(appAssets, sandbox, appConfig);
      
      this.apps.set(name, app);
      return app;
    } finally {
      this.loadingApps.delete(name);
    }
  }
  
  private async fetchAppAssets(entry: string): Promise<AppAssets> {
    const response = await fetch(entry);
    const html = await response.text();
    
    // 解析HTML，提取JS和CSS资源
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    
    const scripts = Array.from(doc.querySelectorAll('script[src]'))
      .map(script => (script as HTMLScriptElement).src);
    
    const styles = Array.from(doc.querySelectorAll('link[rel="stylesheet"]'))
      .map(link => (link as HTMLLinkElement).href);
    
    return {
      html,
      scripts: await Promise.all(scripts.map(src => fetch(src).then(res => res.text()))),
      styles: await Promise.all(styles.map(href => fetch(href).then(res => res.text())))
    };
  }
  
  private createSandbox(appName: string): Sandbox {
    return new ProxySandbox(appName, {
      // 沙箱配置
      strictGlobal: true,
      enableScopedCSS: true,
      excludeAssetFilter: (url: string) => {
        // 排除某些资源的沙箱隔离
        return url.includes('shared-lib');
      }
    });
  }
}
```

## 2. qiankun微前端框架深度解析

### 2.1 qiankun架构设计

```typescript
// qiankun主应用配置
import { registerMicroApps, start, initGlobalState } from 'qiankun';

// 全局状态管理
const initialState = {
  user: null,
  theme: 'light',
  permissions: []
};

const actions = initGlobalState(initialState);

// 监听全局状态变化
actions.onGlobalStateChange((newState, prevState) => {
  console.log('Global state changed:', { newState, prevState });
  
  // 处理主题变化
  if (newState.theme !== prevState.theme) {
    document.documentElement.setAttribute('data-theme', newState.theme);
  }
  
  // 处理用户登录状态变化
  if (newState.user !== prevState.user) {
    if (newState.user) {
      // 用户登录，更新权限
      this.updateUserPermissions(newState.user);
    } else {
      // 用户登出，清理状态
      this.clearUserData();
    }
  }
});

// 注册微应用
registerMicroApps([
  {
    name: 'user-management',
    entry: '//localhost:3001',
    container: '#user-container',
    activeRule: '/user',
    props: {
      routerBase: '/user',
      getGlobalState: () => actions.getGlobalState(),
      setGlobalState: actions.setGlobalState
    }
  },
  {
    name: 'order-system',
    entry: '//localhost:3002',
    container: '#order-container',
    activeRule: '/order',
    props: {
      routerBase: '/order',
      getGlobalState: () => actions.getGlobalState(),
      setGlobalState: actions.setGlobalState
    }
  }
], {
  beforeLoad: (app) => {
    console.log('Before load app:', app.name);
    // 显示加载状态
    showLoadingIndicator(app.name);
    return Promise.resolve();
  },
  beforeMount: (app) => {
    console.log('Before mount app:', app.name);
    return Promise.resolve();
  },
  afterMount: (app) => {
    console.log('After mount app:', app.name);
    // 隐藏加载状态
    hideLoadingIndicator(app.name);
    return Promise.resolve();
  },
  beforeUnmount: (app) => {
    console.log('Before unmount app:', app.name);
    return Promise.resolve();
  },
  afterUnmount: (app) => {
    console.log('After unmount app:', app.name);
    return Promise.resolve();
  }
});

// 启动qiankun
start({
  prefetch: true, // 预加载
  sandbox: {
    strictStyleIsolation: true, // 严格样式隔离
    experimentalStyleIsolation: true // 实验性样式隔离
  },
  singular: false // 允许多个微应用同时存在
});
```

### 2.2 微应用改造

#### React微应用改造

```typescript
// React微应用入口文件 src/index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { MicroAppProvider } from './contexts/MicroAppContext';

interface MicroAppProps {
  container?: HTMLElement;
  routerBase?: string;
  getGlobalState?: () => any;
  setGlobalState?: (state: any) => void;
}

let root: ReactDOM.Root | null = null;

// 渲染函数
function render(props: MicroAppProps = {}) {
  const { container, routerBase = '/', getGlobalState, setGlobalState } = props;
  const containerElement = container || document.getElementById('root');
  
  if (!containerElement) {
    throw new Error('Container element not found');
  }
  
  root = ReactDOM.createRoot(containerElement);
  
  root.render(
    <React.StrictMode>
      <MicroAppProvider
        getGlobalState={getGlobalState}
        setGlobalState={setGlobalState}
      >
        <BrowserRouter basename={routerBase}>
          <App />
        </BrowserRouter>
      </MicroAppProvider>
    </React.StrictMode>
  );
}

// 独立运行时直接渲染
if (!(window as any).__POWERED_BY_QIANKUN__) {
  render();
}

// 导出qiankun生命周期函数
export async function bootstrap() {
  console.log('React app bootstrapped');
}

export async function mount(props: MicroAppProps) {
  console.log('React app mounted with props:', props);
  render(props);
}

export async function unmount(props: MicroAppProps) {
  console.log('React app unmounted');
  if (root) {
    root.unmount();
    root = null;
  }
}

// 微应用上下文
interface MicroAppContextType {
  globalState: any;
  setGlobalState: (state: any) => void;
  isInMicroApp: boolean;
}

const MicroAppContext = React.createContext<MicroAppContextType>({
  globalState: {},
  setGlobalState: () => {},
  isInMicroApp: false
});

export const MicroAppProvider: React.FC<{
  children: React.ReactNode;
  getGlobalState?: () => any;
  setGlobalState?: (state: any) => void;
}> = ({ children, getGlobalState, setGlobalState }) => {
  const [globalState, setLocalGlobalState] = React.useState(
    getGlobalState ? getGlobalState() : {}
  );
  
  const isInMicroApp = !!(window as any).__POWERED_BY_QIANKUN__;
  
  const handleSetGlobalState = React.useCallback((state: any) => {
    if (setGlobalState) {
      setGlobalState(state);
    }
    setLocalGlobalState(state);
  }, [setGlobalState]);
  
  React.useEffect(() => {
    if (getGlobalState) {
      // 监听全局状态变化
      const unsubscribe = (window as any).__QIANKUN_GLOBAL_STATE_CHANGE__?.(
        (newState: any) => {
          setLocalGlobalState(newState);
        }
      );
      
      return unsubscribe;
    }
  }, [getGlobalState]);
  
  return (
    <MicroAppContext.Provider
      value={{
        globalState,
        setGlobalState: handleSetGlobalState,
        isInMicroApp
      }}
    >
      {children}
    </MicroAppContext.Provider>
  );
};

export const useMicroApp = () => {
  const context = React.useContext(MicroAppContext);
  if (!context) {
    throw new Error('useMicroApp must be used within MicroAppProvider');
  }
  return context;
};
```

#### Vue微应用改造

```typescript
// Vue微应用入口文件 src/main.ts
import { createApp, App as VueApp } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';
import App from './App.vue';
import routes from './routes';
import { createMicroAppPlugin } from './plugins/microApp';

interface MicroAppProps {
  container?: HTMLElement;
  routerBase?: string;
  getGlobalState?: () => any;
  setGlobalState?: (state: any) => void;
}

let app: VueApp | null = null;
let router: any = null;

// 渲染函数
function render(props: MicroAppProps = {}) {
  const { container, routerBase = '/', getGlobalState, setGlobalState } = props;
  
  // 创建路由
  router = createRouter({
    history: createWebHistory(routerBase),
    routes
  });
  
  // 创建Vue应用
  app = createApp(App);
  
  // 安装微应用插件
  app.use(createMicroAppPlugin({
    getGlobalState,
    setGlobalState
  }));
  
  // 安装路由
  app.use(router);
  
  // 挂载应用
  const containerElement = container || document.getElementById('app');
  if (containerElement) {
    app.mount(containerElement);
  }
}

// 独立运行时直接渲染
if (!(window as any).__POWERED_BY_QIANKUN__) {
  render();
}

// 导出qiankun生命周期函数
export async function bootstrap() {
  console.log('Vue app bootstrapped');
}

export async function mount(props: MicroAppProps) {
  console.log('Vue app mounted with props:', props);
  render(props);
}

export async function unmount(props: MicroAppProps) {
  console.log('Vue app unmounted');
  if (app) {
    app.unmount();
    app = null;
    router = null;
  }
}

// 微应用插件
function createMicroAppPlugin(options: {
  getGlobalState?: () => any;
  setGlobalState?: (state: any) => void;
}) {
  return {
    install(app: VueApp) {
      const { getGlobalState, setGlobalState } = options;
      
      // 提供全局状态
      app.provide('microApp', {
        globalState: getGlobalState ? getGlobalState() : {},
        setGlobalState: setGlobalState || (() => {}),
        isInMicroApp: !!(window as any).__POWERED_BY_QIANKUN__
      });
      
      // 全局属性
      app.config.globalProperties.$microApp = {
        globalState: getGlobalState ? getGlobalState() : {},
        setGlobalState: setGlobalState || (() => {}),
        isInMicroApp: !!(window as any).__POWERED_BY_QIANKUN__
      };
    }
  };
}

// Composition API
import { inject, ref, onMounted, onUnmounted } from 'vue';

export function useMicroApp() {
  const microApp = inject('microApp') as any;
  
  if (!microApp) {
    throw new Error('useMicroApp must be used within micro app context');
  }
  
  const globalState = ref(microApp.globalState);
  
  const setGlobalState = (state: any) => {
    microApp.setGlobalState(state);
    globalState.value = state;
  };
  
  // 监听全局状态变化
  onMounted(() => {
    if ((window as any).__QIANKUN_GLOBAL_STATE_CHANGE__) {
      const unsubscribe = (window as any).__QIANKUN_GLOBAL_STATE_CHANGE__(
        (newState: any) => {
          globalState.value = newState;
        }
      );
      
      onUnmounted(() => {
        unsubscribe?.();
      });
    }
  });
  
  return {
    globalState,
    setGlobalState,
    isInMicroApp: microApp.isInMicroApp
  };
}
```

### 2.3 样式隔离解决方案

```typescript
// 样式隔离处理器
class StyleIsolationHandler {
  private scopedCSSRules: Map<string, string[]> = new Map();
  private dynamicStyleSheets: Map<string, CSSStyleSheet[]> = new Map();
  
  // 处理CSS作用域
  processScopedCSS(appName: string, cssText: string): string {
    const scopeId = `micro-app-${appName}`;
    
    // 为每个CSS规则添加作用域前缀
    const scopedCSS = cssText.replace(
      /([^{}]+)\s*{([^}]*)}/g,
      (match, selector, rules) => {
        // 处理选择器
        const scopedSelector = this.addScopeToSelector(selector.trim(), scopeId);
        return `${scopedSelector} { ${rules} }`;
      }
    );
    
    // 缓存处理后的CSS规则
    if (!this.scopedCSSRules.has(appName)) {
      this.scopedCSSRules.set(appName, []);
    }
    this.scopedCSSRules.get(appName)!.push(scopedCSS);
    
    return scopedCSS;
  }
  
  private addScopeToSelector(selector: string, scopeId: string): string {
    // 处理复合选择器
    const selectors = selector.split(',').map(s => s.trim());
    
    return selectors.map(sel => {
      // 跳过特殊选择器
      if (sel.startsWith('@') || sel.includes(':root') || sel.includes('html') || sel.includes('body')) {
        return sel;
      }
      
      // 添加作用域前缀
      if (sel.startsWith('.') || sel.startsWith('#') || /^[a-zA-Z]/.test(sel)) {
        return `.${scopeId} ${sel}`;
      }
      
      return sel;
    }).join(', ');
  }
  
  // 创建隔离的样式表
  createIsolatedStyleSheet(appName: string, cssText: string): CSSStyleSheet {
    const scopedCSS = this.processScopedCSS(appName, cssText);
    
    // 创建新的样式表
    const styleSheet = new CSSStyleSheet();
    styleSheet.replaceSync(scopedCSS);
    
    // 缓存样式表
    if (!this.dynamicStyleSheets.has(appName)) {
      this.dynamicStyleSheets.set(appName, []);
    }
    this.dynamicStyleSheets.get(appName)!.push(styleSheet);
    
    return styleSheet;
  }
  
  // 应用样式到容器
  applyStylesToContainer(appName: string, container: HTMLElement): void {
    const scopeId = `micro-app-${appName}`;
    
    // 添加作用域类名
    container.classList.add(scopeId);
    
    // 应用动态样式表
    const styleSheets = this.dynamicStyleSheets.get(appName);
    if (styleSheets && 'adoptedStyleSheets' in document) {
      (document as any).adoptedStyleSheets = [
        ...(document as any).adoptedStyleSheets,
        ...styleSheets
      ];
    }
  }
  
  // 清理应用样式
  cleanupAppStyles(appName: string): void {
    // 清理CSS规则缓存
    this.scopedCSSRules.delete(appName);
    
    // 清理动态样式表
    const styleSheets = this.dynamicStyleSheets.get(appName);
    if (styleSheets && 'adoptedStyleSheets' in document) {
      (document as any).adoptedStyleSheets = (
        (document as any).adoptedStyleSheets as CSSStyleSheet[]
      ).filter(sheet => !styleSheets.includes(sheet));
    }
    
    this.dynamicStyleSheets.delete(appName);
    
    // 移除DOM中的相关样式元素
    const styleElements = document.querySelectorAll(`style[data-app="${appName}"]`);
    styleElements.forEach(element => element.remove());
  }
}

// 使用示例
const styleHandler = new StyleIsolationHandler();

// 在微应用加载时处理样式
function handleMicroAppStyles(appName: string, cssContent: string, container: HTMLElement) {
  // 处理CSS并应用到容器
  const scopedCSS = styleHandler.processScopedCSS(appName, cssContent);
  
  // 创建style元素
  const styleElement = document.createElement('style');
  styleElement.setAttribute('data-app', appName);
  styleElement.textContent = scopedCSS;
  
  // 添加到文档头部
  document.head.appendChild(styleElement);
  
  // 应用作用域到容器
  styleHandler.applyStylesToContainer(appName, container);
}

// 在微应用卸载时清理样式
function cleanupMicroAppStyles(appName: string) {
  styleHandler.cleanupAppStyles(appName);
}
```

## 3. Module Federation深度解析

### 3.1 Module Federation核心概念

```typescript
// Webpack Module Federation配置
// 主应用 webpack.config.js
const ModuleFederationPlugin = require('@module-federation/webpack');

module.exports = {
  mode: 'development',
  devServer: {
    port: 3000,
  },
  plugins: [
    new ModuleFederationPlugin({
      name: 'host',
      remotes: {
        'user-management': 'userManagement@http://localhost:3001/remoteEntry.js',
        'order-system': 'orderSystem@http://localhost:3002/remoteEntry.js',
        'shared-components': 'sharedComponents@http://localhost:3003/remoteEntry.js'
      },
      shared: {
        react: {
          singleton: true,
          requiredVersion: '^18.0.0'
        },
        'react-dom': {
          singleton: true,
          requiredVersion: '^18.0.0'
        },
        '@emotion/react': {
          singleton: true
        },
        '@emotion/styled': {
          singleton: true
        }
      }
    })
  ]
};

// 微应用 webpack.config.js
module.exports = {
  mode: 'development',
  devServer: {
    port: 3001,
  },
  plugins: [
    new ModuleFederationPlugin({
      name: 'userManagement',
      filename: 'remoteEntry.js',
      exposes: {
        './UserApp': './src/App',
        './UserList': './src/components/UserList',
        './UserForm': './src/components/UserForm',
        './userStore': './src/store/userStore'
      },
      shared: {
        react: {
          singleton: true,
          requiredVersion: '^18.0.0'
        },
        'react-dom': {
          singleton: true,
          requiredVersion: '^18.0.0'
        }
      }
    })
  ]
};
```

### 3.2 动态模块加载

```typescript
// 动态模块加载器
class ModuleFederationLoader {
  private loadedModules: Map<string, any> = new Map();
  private loadingPromises: Map<string, Promise<any>> = new Map();
  
  // 动态加载远程模块
  async loadRemoteModule<T = any>(
    remoteName: string,
    moduleName: string,
    fallback?: () => T
  ): Promise<T> {
    const moduleKey = `${remoteName}/${moduleName}`;
    
    // 检查是否已加载
    if (this.loadedModules.has(moduleKey)) {
      return this.loadedModules.get(moduleKey);
    }
    
    // 检查是否正在加载
    if (this.loadingPromises.has(moduleKey)) {
      return this.loadingPromises.get(moduleKey);
    }
    
    // 开始加载
    const loadingPromise = this.doLoadRemoteModule<T>(remoteName, moduleName, fallback);
    this.loadingPromises.set(moduleKey, loadingPromise);
    
    try {
      const module = await loadingPromise;
      this.loadedModules.set(moduleKey, module);
      return module;
    } finally {
      this.loadingPromises.delete(moduleKey);
    }
  }
  
  private async doLoadRemoteModule<T>(
    remoteName: string,
    moduleName: string,
    fallback?: () => T
  ): Promise<T> {
    try {
      // 动态导入远程模块
      const container = (window as any)[remoteName];
      
      if (!container) {
        throw new Error(`Remote container '${remoteName}' not found`);
      }
      
      // 初始化容器
      await container.init(__webpack_share_scopes__.default);
      
      // 获取模块工厂
      const factory = await container.get(moduleName);
      
      // 执行模块工厂获取模块
      const module = factory();
      
      return module;
    } catch (error) {
      console.error(`Failed to load remote module ${remoteName}/${moduleName}:`, error);
      
      if (fallback) {
        console.log(`Using fallback for ${remoteName}/${moduleName}`);
        return fallback();
      }
      
      throw error;
    }
  }
  
  // 预加载远程模块
  async preloadRemoteModule(remoteName: string, moduleName: string): Promise<void> {
    try {
      await this.loadRemoteModule(remoteName, moduleName);
      console.log(`Preloaded ${remoteName}/${moduleName}`);
    } catch (error) {
      console.warn(`Failed to preload ${remoteName}/${moduleName}:`, error);
    }
  }
  
  // 批量预加载
  async preloadModules(modules: Array<{ remote: string; module: string }>): Promise<void> {
    const preloadPromises = modules.map(({ remote, module }) => 
      this.preloadRemoteModule(remote, module)
    );
    
    await Promise.allSettled(preloadPromises);
  }
  
  // 清理已加载的模块
  clearCache(remoteName?: string): void {
    if (remoteName) {
      // 清理特定远程应用的模块
      for (const [key] of this.loadedModules) {
        if (key.startsWith(`${remoteName}/`)) {
          this.loadedModules.delete(key);
        }
      }
    } else {
      // 清理所有模块
      this.loadedModules.clear();
    }
  }
}

// 全局模块加载器实例
export const moduleLoader = new ModuleFederationLoader();

// React组件懒加载Hook
import React, { Suspense, lazy, ComponentType } from 'react';

interface RemoteComponentProps {
  remoteName: string;
  moduleName: string;
  fallback?: ComponentType;
  loadingComponent?: ComponentType;
  errorComponent?: ComponentType<{ error: Error; retry: () => void }>;
}

export function useRemoteComponent<T extends ComponentType<any>>(
  remoteName: string,
  moduleName: string,
  fallback?: T
): {
  Component: T | null;
  loading: boolean;
  error: Error | null;
  retry: () => void;
} {
  const [component, setComponent] = React.useState<T | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<Error | null>(null);
  
  const loadComponent = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const module = await moduleLoader.loadRemoteModule<{ default: T }>(
        remoteName,
        moduleName,
        fallback ? () => ({ default: fallback }) : undefined
      );
      
      setComponent(module.default || module);
    } catch (err) {
      setError(err as Error);
      if (fallback) {
        setComponent(fallback);
      }
    } finally {
      setLoading(false);
    }
  }, [remoteName, moduleName, fallback]);
  
  React.useEffect(() => {
    loadComponent();
  }, [loadComponent]);
  
  const retry = React.useCallback(() => {
    moduleLoader.clearCache(remoteName);
    loadComponent();
  }, [remoteName, loadComponent]);
  
  return {
    Component: component,
    loading,
    error,
    retry
  };
}

// 远程组件包装器
export const RemoteComponent: React.FC<RemoteComponentProps & { [key: string]: any }> = ({
  remoteName,
  moduleName,
  fallback,
  loadingComponent: LoadingComponent = () => <div>Loading...</div>,
  errorComponent: ErrorComponent = ({ error, retry }) => (
    <div>
      <p>Error loading component: {error.message}</p>
      <button onClick={retry}>Retry</button>
    </div>
  ),
  ...props
}) => {
  const { Component, loading, error, retry } = useRemoteComponent(
    remoteName,
    moduleName,
    fallback
  );
  
  if (loading) {
    return <LoadingComponent />;
  }
  
  if (error && !Component) {
    return <ErrorComponent error={error} retry={retry} />;
  }
  
  if (!Component) {
    return null;
  }
  
  return (
    <Suspense fallback={<LoadingComponent />}>
      <Component {...props} />
    </Suspense>
  );
};
```

### 3.3 共享依赖管理

```typescript
// 共享依赖管理器
class SharedDependencyManager {
  private sharedModules: Map<string, SharedModuleInfo> = new Map();
  private versionConflicts: Map<string, VersionConflict[]> = new Map();
  
  // 注册共享模块
  registerSharedModule(name: string, info: SharedModuleInfo): void {
    this.sharedModules.set(name, info);
    this.checkVersionConflicts(name, info);
  }
  
  // 检查版本冲突
  private checkVersionConflicts(name: string, newInfo: SharedModuleInfo): void {
    const existingInfo = this.sharedModules.get(name);
    
    if (existingInfo && existingInfo.version !== newInfo.version) {
      const conflict: VersionConflict = {
        moduleName: name,
        existingVersion: existingInfo.version,
        requestedVersion: newInfo.version,
        requiredBy: newInfo.requiredBy,
        severity: this.calculateConflictSeverity(existingInfo.version, newInfo.version)
      };
      
      if (!this.versionConflicts.has(name)) {
        this.versionConflicts.set(name, []);
      }
      
      this.versionConflicts.get(name)!.push(conflict);
      
      // 记录冲突
      console.warn('Version conflict detected:', conflict);
      
      // 根据严重程度处理冲突
      this.handleVersionConflict(conflict);
    }
  }
  
  private calculateConflictSeverity(existing: string, requested: string): 'major' | 'minor' | 'patch' {
    const existingParts = existing.split('.').map(Number);
    const requestedParts = requested.split('.').map(Number);
    
    if (existingParts[0] !== requestedParts[0]) {
      return 'major';
    }
    
    if (existingParts[1] !== requestedParts[1]) {
      return 'minor';
    }
    
    return 'patch';
  }
  
  private handleVersionConflict(conflict: VersionConflict): void {
    switch (conflict.severity) {
      case 'major':
        // 主版本冲突，可能导致严重问题
        console.error(`Major version conflict for ${conflict.moduleName}: ${conflict.existingVersion} vs ${conflict.requestedVersion}`);
        // 可以选择阻止加载或使用降级策略
        break;
        
      case 'minor':
        // 次版本冲突，可能有兼容性问题
        console.warn(`Minor version conflict for ${conflict.moduleName}: ${conflict.existingVersion} vs ${conflict.requestedVersion}`);
        // 使用较高版本或提供兼容性适配
        break;
        
      case 'patch':
        // 补丁版本冲突，通常兼容
        console.info(`Patch version conflict for ${conflict.moduleName}: ${conflict.existingVersion} vs ${conflict.requestedVersion}`);
        // 使用较高版本
        break;
    }
  }
  
  // 获取共享模块
  getSharedModule(name: string, requiredVersion?: string): SharedModuleInfo | null {
    const moduleInfo = this.sharedModules.get(name);
    
    if (!moduleInfo) {
      return null;
    }
    
    // 检查版本兼容性
    if (requiredVersion && !this.isVersionCompatible(moduleInfo.version, requiredVersion)) {
      console.warn(`Version mismatch for ${name}: available ${moduleInfo.version}, required ${requiredVersion}`);
      return null;
    }
    
    return moduleInfo;
  }
  
  private isVersionCompatible(available: string, required: string): boolean {
    // 简单的语义版本兼容性检查
    const availableParts = available.split('.').map(Number);
    const requiredParts = required.split('.').map(Number);
    
    // 主版本必须匹配
    if (availableParts[0] !== requiredParts[0]) {
      return false;
    }
    
    // 次版本向后兼容
    if (availableParts[1] < requiredParts[1]) {
      return false;
    }
    
    // 补丁版本向后兼容
    if (availableParts[1] === requiredParts[1] && availableParts[2] < requiredParts[2]) {
      return false;
    }
    
    return true;
  }
  
  // 获取版本冲突报告
  getVersionConflictReport(): VersionConflictReport {
    const conflicts: VersionConflict[] = [];
    
    for (const conflictList of this.versionConflicts.values()) {
      conflicts.push(...conflictList);
    }
    
    return {
      totalConflicts: conflicts.length,
      majorConflicts: conflicts.filter(c => c.severity === 'major').length,
      minorConflicts: conflicts.filter(c => c.severity === 'minor').length,
      patchConflicts: conflicts.filter(c => c.severity === 'patch').length,
      conflicts
    };
  }
}

// 类型定义
interface SharedModuleInfo {
  name: string;
  version: string;
  singleton: boolean;
  requiredBy: string[];
  module?: any;
}

interface VersionConflict {
  moduleName: string;
  existingVersion: string;
  requestedVersion: string;
  requiredBy: string[];
  severity: 'major' | 'minor' | 'patch';
}

interface VersionConflictReport {
  totalConflicts: number;
  majorConflicts: number;
  minorConflicts: number;
  patchConflicts: number;
  conflicts: VersionConflict[];
}

// 全局共享依赖管理器
export const sharedDependencyManager = new SharedDependencyManager();

// Webpack插件集成
class ModuleFederationSharedPlugin {
  apply(compiler: any) {
    compiler.hooks.beforeRun.tapAsync('ModuleFederationSharedPlugin', (compilation: any, callback: any) => {
      // 在编译前注册共享模块信息
      this.registerSharedModules(compilation);
      callback();
    });
  }
  
  private registerSharedModules(compilation: any): void {
    // 从webpack配置中提取共享模块信息
    const sharedConfig = compilation.options.plugins
      .find((plugin: any) => plugin.constructor.name === 'ModuleFederationPlugin')
      ?.options?.shared || {};
    
    Object.entries(sharedConfig).forEach(([name, config]: [string, any]) => {
      sharedDependencyManager.registerSharedModule(name, {
        name,
        version: config.requiredVersion || 'latest',
        singleton: config.singleton || false,
        requiredBy: [compilation.options.name || 'unknown']
      });
    });
  }
}

export { ModuleFederationSharedPlugin };
```

## 4. 微前端错误处理与容错机制

### 4.1 全局错误处理

```typescript
// 微前端错误处理器
class MicroFrontendErrorHandler {
  private errorListeners: Map<string, ErrorListener[]> = new Map();
  private errorHistory: ErrorRecord[] = [];
  private maxErrorHistory = 100;
  
  constructor() {
    this.setupGlobalErrorHandling();
  }
  
  private setupGlobalErrorHandling(): void {
    // 捕获JavaScript错误
    window.addEventListener('error', (event) => {
      this.handleError({
        type: 'javascript',
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        error: event.error,
        timestamp: Date.now(),
        appName: this.getAppNameFromError(event)
      });
    });
    
    // 捕获Promise未处理的拒绝
    window.addEventListener('unhandledrejection', (event) => {
      this.handleError({
        type: 'promise',
        message: event.reason?.message || 'Unhandled Promise Rejection',
        error: event.reason,
        timestamp: Date.now(),
        appName: this.getAppNameFromPromiseError(event)
      });
    });
    
    // 捕获资源加载错误
    window.addEventListener('error', (event) => {
      if (event.target !== window) {
        this.handleError({
          type: 'resource',
          message: `Failed to load resource: ${(event.target as any)?.src || (event.target as any)?.href}`,
          target: event.target,
          timestamp: Date.now(),
          appName: this.getAppNameFromElement(event.target as Element)
        });
      }
    }, true);
  }
  
  // 处理错误
  private handleError(errorRecord: ErrorRecord): void {
    // 添加到错误历史
    this.addToErrorHistory(errorRecord);
    
    // 通知错误监听器
    this.notifyErrorListeners(errorRecord);
    
    // 根据错误类型和应用执行恢复策略
    this.executeRecoveryStrategy(errorRecord);
    
    // 上报错误到监控系统
    this.reportError(errorRecord);
  }
  
  private addToErrorHistory(errorRecord: ErrorRecord): void {
    this.errorHistory.unshift(errorRecord);
    
    // 限制历史记录数量
    if (this.errorHistory.length > this.maxErrorHistory) {
      this.errorHistory = this.errorHistory.slice(0, this.maxErrorHistory);
    }
  }
  
  private notifyErrorListeners(errorRecord: ErrorRecord): void {
    const appListeners = this.errorListeners.get(errorRecord.appName || 'global') || [];
    const globalListeners = this.errorListeners.get('global') || [];
    
    [...appListeners, ...globalListeners].forEach(listener => {
      try {
        listener(errorRecord);
      } catch (error) {
        console.error('Error in error listener:', error);
      }
    });
  }
  
  private executeRecoveryStrategy(errorRecord: ErrorRecord): void {
    const { type, appName } = errorRecord;
    
    switch (type) {
      case 'javascript':
        this.handleJavaScriptError(errorRecord);
        break;
      case 'promise':
        this.handlePromiseError(errorRecord);
        break;
      case 'resource':
        this.handleResourceError(errorRecord);
        break;
      case 'network':
        this.handleNetworkError(errorRecord);
        break;
    }
  }
  
  private handleJavaScriptError(errorRecord: ErrorRecord): void {
    const { appName, error } = errorRecord;
    
    // 检查是否是致命错误
    if (this.isFatalError(error)) {
      console.error(`Fatal error in app ${appName}, attempting recovery...`);
      
      // 尝试重新加载应用
      this.reloadMicroApp(appName);
    } else {
      // 非致命错误，记录并继续
      console.warn(`Non-fatal error in app ${appName}:`, error);
    }
  }
  
  private handlePromiseError(errorRecord: ErrorRecord): void {
    const { appName, error } = errorRecord;
    
    // Promise错误通常不需要重新加载应用
    console.warn(`Promise rejection in app ${appName}:`, error);
    
    // 可以尝试重试操作
    if (this.isRetryableError(error)) {
      this.scheduleRetry(errorRecord);
    }
  }
  
  private handleResourceError(errorRecord: ErrorRecord): void {
    const { target, appName } = errorRecord;
    
    if (target instanceof HTMLScriptElement) {
      // 脚本加载失败，尝试从备用CDN加载
      this.retryScriptLoad(target, appName);
    } else if (target instanceof HTMLLinkElement) {
      // 样式表加载失败
      this.retryStylesheetLoad(target, appName);
    }
  }
  
  private handleNetworkError(errorRecord: ErrorRecord): void {
    // 网络错误处理
    console.warn('Network error detected:', errorRecord);
    
    // 可以实现网络重试逻辑
    this.scheduleNetworkRetry(errorRecord);
  }
  
  // 判断是否为致命错误
  private isFatalError(error: any): boolean {
    if (!error) return false;
    
    const fatalPatterns = [
      /Cannot read property.*of undefined/,
      /Cannot read properties.*of undefined/,
      /is not a function/,
      /Maximum call stack size exceeded/
    ];
    
    return fatalPatterns.some(pattern => pattern.test(error.message || ''));
  }
  
  // 判断是否可重试的错误
  private isRetryableError(error: any): boolean {
    if (!error) return false;
    
    const retryablePatterns = [
      /Network Error/,
      /timeout/,
      /Failed to fetch/,
      /ERR_NETWORK/
    ];
    
    return retryablePatterns.some(pattern => pattern.test(error.message || ''));
  }
  
  // 重新加载微应用
  private async reloadMicroApp(appName?: string): Promise<void> {
    if (!appName) return;
    
    try {
      // 如果使用qiankun
      if ((window as any).__QIANKUN_DEVELOPMENT__) {
        const { loadMicroApp } = await import('qiankun');
        // 重新加载应用逻辑
      }
      
      // 如果使用Module Federation
      if ((window as any).__webpack_require__) {
        // 清理模块缓存并重新加载
        moduleLoader.clearCache(appName);
      }
      
      console.log(`Successfully reloaded app: ${appName}`);
    } catch (error) {
      console.error(`Failed to reload app ${appName}:`, error);
    }
  }
  
  // 注册错误监听器
  addErrorListener(appName: string, listener: ErrorListener): void {
    if (!this.errorListeners.has(appName)) {
      this.errorListeners.set(appName, []);
    }
    
    this.errorListeners.get(appName)!.push(listener);
  }
  
  // 移除错误监听器
  removeErrorListener(appName: string, listener: ErrorListener): void {
    const listeners = this.errorListeners.get(appName);
    if (listeners) {
      const index = listeners.indexOf(listener);
      if (index > -1) {
        listeners.splice(index, 1);
      }
    }
  }
  
  // 获取错误统计
  getErrorStatistics(): ErrorStatistics {
    const now = Date.now();
    const last24Hours = now - 24 * 60 * 60 * 1000;
    const recentErrors = this.errorHistory.filter(error => error.timestamp > last24Hours);
    
    const errorsByType = recentErrors.reduce((acc, error) => {
      acc[error.type] = (acc[error.type] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
    
    const errorsByApp = recentErrors.reduce((acc, error) => {
      const appName = error.appName || 'unknown';
      acc[appName] = (acc[appName] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);
    
    return {
      totalErrors: recentErrors.length,
      errorsByType,
      errorsByApp,
      errorRate: recentErrors.length / 24, // 每小时错误数
      lastError: this.errorHistory[0] || null
    };
  }
  
  private getAppNameFromError(event: ErrorEvent): string | undefined {
    // 从错误堆栈或文件名推断应用名称
    const filename = event.filename || '';
    const match = filename.match(/\/\/localhost:(\d+)/) || filename.match(/\/([^/]+)\//);
    return match ? match[1] : undefined;
  }
  
  private getAppNameFromElement(element: Element): string | undefined {
    // 从DOM元素推断应用名称
    let current = element;
    while (current && current !== document.body) {
      if (current.hasAttribute('data-app-name')) {
        return current.getAttribute('data-app-name') || undefined;
      }
      current = current.parentElement!;
    }
    return undefined;
  }
}

// 类型定义
interface ErrorRecord {
  type: 'javascript' | 'promise' | 'resource' | 'network';
  message: string;
  filename?: string;
  lineno?: number;
  colno?: number;
  error?: any;
  target?: EventTarget | null;
  timestamp: number;
  appName?: string;
}

type ErrorListener = (error: ErrorRecord) => void;

interface ErrorStatistics {
  totalErrors: number;
  errorsByType: Record<string, number>;
  errorsByApp: Record<string, number>;
  errorRate: number;
  lastError: ErrorRecord | null;
}

// 全局错误处理器实例
export const microFrontendErrorHandler = new MicroFrontendErrorHandler();
```

### 4.2 应用隔离与容错

```typescript
// 应用隔离容器
class IsolatedAppContainer {
  private container: HTMLElement;
  private appName: string;
  private isolationConfig: IsolationConfig;
  private errorBoundary: ErrorBoundary;
  
  constructor(appName: string, container: HTMLElement, config: IsolationConfig) {
    this.appName = appName;
    this.container = container;
    this.isolationConfig = config;
    this.errorBoundary = new ErrorBoundary(appName);
    
    this.setupIsolation();
  }
  
  private setupIsolation(): void {
    // 设置CSS隔离
    if (this.isolationConfig.cssIsolation) {
      this.setupCSSIsolation();
    }
    
    // 设置JavaScript隔离
    if (this.isolationConfig.jsIsolation) {
      this.setupJSIsolation();
    }
    
    // 设置事件隔离
    if (this.isolationConfig.eventIsolation) {
      this.setupEventIsolation();
    }
  }
  
  private setupCSSIsolation(): void {
    // 创建Shadow DOM或使用CSS作用域
    if (this.isolationConfig.useShadowDOM && this.container.attachShadow) {
      const shadowRoot = this.container.attachShadow({ mode: 'open' });
      
      // 创建应用容器
      const appContainer = document.createElement('div');
      appContainer.className = `micro-app-${this.appName}`;
      shadowRoot.appendChild(appContainer);
      
      // 复制必要的全局样式
      this.copyGlobalStyles(shadowRoot);
    } else {
      // 使用CSS作用域
      this.container.className += ` micro-app-${this.appName}`;
    }
  }
  
  private setupJSIsolation(): void {
    // 创建沙箱环境
    const sandbox = new Proxy(window, {
      get: (target, prop) => {
        // 拦截全局变量访问
        if (this.isolationConfig.globalVariableWhitelist?.includes(prop as string)) {
          return target[prop as keyof Window];
        }
        
        // 返回沙箱中的变量
        return this.getSandboxVariable(prop as string);
      },
      set: (target, prop, value) => {
        // 拦截全局变量设置
        if (this.isolationConfig.globalVariableWhitelist?.includes(prop as string)) {
          target[prop as keyof Window] = value;
          return true;
        }
        
        // 设置到沙箱中
        this.setSandboxVariable(prop as string, value);
        return true;
      }
    });
    
    // 将沙箱绑定到容器
    (this.container as any).__SANDBOX__ = sandbox;
  }
  
  private setupEventIsolation(): void {
    // 拦截事件监听器
    const originalAddEventListener = this.container.addEventListener;
    const originalRemoveEventListener = this.container.removeEventListener;
    
    this.container.addEventListener = function(type, listener, options) {
      // 添加应用标识
      const wrappedListener = function(event: Event) {
        try {
          if (typeof listener === 'function') {
            listener.call(this, event);
          } else if (listener && typeof listener.handleEvent === 'function') {
            listener.handleEvent(event);
          }
        } catch (error) {
          microFrontendErrorHandler.handleError({
            type: 'javascript',
            message: `Event listener error in ${appName}`,
            error,
            timestamp: Date.now(),
            appName
          });
        }
      };
      
      originalAddEventListener.call(this, type, wrappedListener, options);
    };
  }
  
  // 错误边界处理
  wrapWithErrorBoundary<T>(fn: () => T): T | null {
    try {
      return fn();
    } catch (error) {
      this.errorBoundary.handleError(error);
      return null;
    }
  }
  
  // 异步错误边界处理
  async wrapAsyncWithErrorBoundary<T>(fn: () => Promise<T>): Promise<T | null> {
    try {
      return await fn();
    } catch (error) {
      this.errorBoundary.handleError(error);
      return null;
    }
  }
  
  // 清理隔离环境
  cleanup(): void {
    this.errorBoundary.cleanup();
    
    // 清理事件监听器
    this.container.innerHTML = '';
    
    // 清理沙箱
    delete (this.container as any).__SANDBOX__;
  }
}

// 错误边界
class ErrorBoundary {
  private appName: string;
  private errorCount = 0;
  private maxErrors = 5;
  private resetTimeout = 60000; // 1分钟后重置错误计数
  private lastErrorTime = 0;
  
  constructor(appName: string) {
    this.appName = appName;
  }
  
  handleError(error: any): void {
    const now = Date.now();
    
    // 重置错误计数
    if (now - this.lastErrorTime > this.resetTimeout) {
      this.errorCount = 0;
    }
    
    this.errorCount++;
    this.lastErrorTime = now;
    
    // 记录错误
    microFrontendErrorHandler.handleError({
      type: 'javascript',
      message: error.message || 'Unknown error',
      error,
      timestamp: now,
      appName: this.appName
    });
    
    // 检查是否需要降级
    if (this.errorCount >= this.maxErrors) {
      this.triggerFallback();
    }
  }
  
  private triggerFallback(): void {
    console.error(`App ${this.appName} has exceeded error threshold, triggering fallback`);
    
    // 显示降级UI
    this.showFallbackUI();
    
    // 通知主应用
    window.dispatchEvent(new CustomEvent('micro-app-error', {
      detail: {
        appName: this.appName,
        errorCount: this.errorCount,
        action: 'fallback'
      }
    }));
  }
  
  private showFallbackUI(): void {
    const container = document.querySelector(`[data-app-name="${this.appName}"]`);
    if (container) {
      container.innerHTML = `
        <div class="micro-app-error-fallback">
          <h3>应用暂时不可用</h3>
          <p>应用 "${this.appName}" 遇到了一些问题，请稍后重试。</p>
          <button onclick="location.reload()">刷新页面</button>
        </div>
      `;
    }
  }
  
  cleanup(): void {
    this.errorCount = 0;
    this.lastErrorTime = 0;
  }
}

// 隔离配置接口
interface IsolationConfig {
  cssIsolation: boolean;
  jsIsolation: boolean;
  eventIsolation: boolean;
  useShadowDOM: boolean;
  globalVariableWhitelist?: string[];
}

export { IsolatedAppContainer, ErrorBoundary };
```

## 5. 性能优化策略

### 5.1 资源预加载与缓存

```typescript
// 资源预加载管理器
class ResourcePreloader {
  private preloadCache: Map<string, Promise<any>> = new Map();
  private resourceCache: Map<string, any> = new Map();
  private preloadStrategies: Map<string, PreloadStrategy> = new Map();
  
  constructor() {
    this.setupPreloadStrategies();
  }
  
  private setupPreloadStrategies(): void {
    // 立即预加载策略
    this.preloadStrategies.set('immediate', {
      name: 'immediate',
      condition: () => true,
      priority: 'high',
      timeout: 5000
    });
    
    // 空闲时预加载策略
    this.preloadStrategies.set('idle', {
      name: 'idle',
      condition: () => 'requestIdleCallback' in window,
      priority: 'low',
      timeout: 10000
    });
    
    // 用户交互预加载策略
    this.preloadStrategies.set('interaction', {
      name: 'interaction',
      condition: () => true,
      priority: 'medium',
      timeout: 3000
    });
    
    // 可见性预加载策略
    this.preloadStrategies.set('visibility', {
      name: 'visibility',
      condition: () => 'IntersectionObserver' in window,
      priority: 'medium',
      timeout: 5000
    });
  }
  
  // 预加载微应用
  async preloadMicroApp(
    appConfig: MicroFrontendApp,
    strategy: string = 'idle'
  ): Promise<void> {
    const { name, entry } = appConfig;
    const cacheKey = `app-${name}`;
    
    // 检查是否已经预加载
    if (this.preloadCache.has(cacheKey)) {
      return this.preloadCache.get(cacheKey);
    }
    
    const preloadStrategy = this.preloadStrategies.get(strategy);
    if (!preloadStrategy || !preloadStrategy.condition()) {
      console.warn(`Preload strategy '${strategy}' not available`);
      return;
    }
    
    const preloadPromise = this.executePreload(appConfig, preloadStrategy);
    this.preloadCache.set(cacheKey, preloadPromise);
    
    try {
      await preloadPromise;
      console.log(`Successfully preloaded app: ${name}`);
    } catch (error) {
      console.error(`Failed to preload app ${name}:`, error);
      this.preloadCache.delete(cacheKey);
    }
  }
  
  private async executePreload(
    appConfig: MicroFrontendApp,
    strategy: PreloadStrategy
  ): Promise<void> {
    return new Promise((resolve, reject) => {
      const timeoutId = setTimeout(() => {
        reject(new Error(`Preload timeout for ${appConfig.name}`));
      }, strategy.timeout);
      
      const doPreload = async () => {
        try {
          await this.loadAppResources(appConfig);
          clearTimeout(timeoutId);
          resolve();
        } catch (error) {
          clearTimeout(timeoutId);
          reject(error);
        }
      };
      
      switch (strategy.name) {
        case 'immediate':
          doPreload();
          break;
          
        case 'idle':
          if ('requestIdleCallback' in window) {
            (window as any).requestIdleCallback(doPreload, {
              timeout: strategy.timeout
            });
          } else {
            setTimeout(doPreload, 0);
          }
          break;
          
        case 'interaction':
          this.setupInteractionPreload(doPreload);
          break;
          
        case 'visibility':
          this.setupVisibilityPreload(appConfig, doPreload);
          break;
          
        default:
          doPreload();
      }
    });
  }
  
  private async loadAppResources(appConfig: MicroFrontendApp): Promise<void> {
    const { entry } = appConfig;
    
    // 获取应用HTML
    const response = await fetch(entry);
    const html = await response.text();
    
    // 解析资源URL
    const parser = new DOMParser();
    const doc = parser.parseFromString(html, 'text/html');
    
    const scripts = Array.from(doc.querySelectorAll('script[src]'))
      .map(script => (script as HTMLScriptElement).src);
    
    const styles = Array.from(doc.querySelectorAll('link[rel="stylesheet"]'))
      .map(link => (link as HTMLLinkElement).href);
    
    // 预加载脚本
    const scriptPromises = scripts.map(src => this.preloadScript(src));
    
    // 预加载样式
    const stylePromises = styles.map(href => this.preloadStylesheet(href));
    
    // 等待所有资源加载完成
    await Promise.all([...scriptPromises, ...stylePromises]);
    
    // 缓存应用资源
    this.resourceCache.set(appConfig.name, {
      html,
      scripts,
      styles,
      loadedAt: Date.now()
    });
  }
  
  private preloadScript(src: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.as = 'script';
      link.href = src;
      link.onload = () => resolve();
      link.onerror = () => reject(new Error(`Failed to preload script: ${src}`));
      
      document.head.appendChild(link);
    });
  }
  
  private preloadStylesheet(href: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.as = 'style';
      link.href = href;
      link.onload = () => resolve();
      link.onerror = () => reject(new Error(`Failed to preload stylesheet: ${href}`));
      
      document.head.appendChild(link);
    });
  }
  
  private setupInteractionPreload(callback: () => void): void {
    const events = ['mouseenter', 'touchstart', 'focus'];
    let executed = false;
    
    const executeOnce = () => {
      if (!executed) {
        executed = true;
        events.forEach(event => {
          document.removeEventListener(event, executeOnce, true);
        });
        callback();
      }
    };
    
    events.forEach(event => {
      document.addEventListener(event, executeOnce, { once: true, capture: true });
    });
  }
  
  private setupVisibilityPreload(appConfig: MicroFrontendApp, callback: () => void): void {
    const container = document.querySelector(appConfig.container);
    if (!container || !('IntersectionObserver' in window)) {
      callback();
      return;
    }
    
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          callback();
          observer.disconnect();
        }
      });
    }, {
      rootMargin: '50px'
    });
    
    observer.observe(container);
  }
  
  // 获取缓存的资源
  getCachedResources(appName: string): any {
    return this.resourceCache.get(appName);
  }
  
  // 清理缓存
  clearCache(appName?: string): void {
    if (appName) {
      this.preloadCache.delete(`app-${appName}`);
      this.resourceCache.delete(appName);
    } else {
      this.preloadCache.clear();
      this.resourceCache.clear();
    }
  }
  
  // 获取预加载统计
  getPreloadStatistics(): PreloadStatistics {
    const totalApps = this.resourceCache.size;
    const totalSize = Array.from(this.resourceCache.values())
      .reduce((size, resources) => {
        return size + (resources.scripts?.length || 0) + (resources.styles?.length || 0);
      }, 0);
    
    return {
      totalApps,
      totalResources: totalSize,
      cacheHitRate: this.calculateCacheHitRate(),
      averageLoadTime: this.calculateAverageLoadTime()
    };
  }
  
  private calculateCacheHitRate(): number {
    // 实现缓存命中率计算逻辑
    return 0.85; // 示例值
  }
  
  private calculateAverageLoadTime(): number {
    // 实现平均加载时间计算逻辑
    return 1200; // 示例值（毫秒）
  }
}

// 类型定义
interface PreloadStrategy {
  name: string;
  condition: () => boolean;
  priority: 'high' | 'medium' | 'low';
  timeout: number;
}

interface PreloadStatistics {
  totalApps: number;
  totalResources: number;
  cacheHitRate: number;
  averageLoadTime: number;
}

// 全局资源预加载器
export const resourcePreloader = new ResourcePreloader();
```

### 5.2 性能监控与优化

```typescript
// 微前端性能监控器
class MicroFrontendPerformanceMonitor {
  private performanceEntries: Map<string, PerformanceEntry[]> = new Map();
  private appMetrics: Map<string, AppMetrics> = new Map();
  private observer: PerformanceObserver | null = null;
  
  constructor() {
    this.setupPerformanceObserver();
  }
  
  private setupPerformanceObserver(): void {
    if ('PerformanceObserver' in window) {
      this.observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        this.processPerformanceEntries(entries);
      });
      
      // 观察所有性能指标
      this.observer.observe({
        entryTypes: ['navigation', 'resource', 'measure', 'mark', 'paint']
      });
    }
  }
  
  private processPerformanceEntries(entries: PerformanceEntry[]): void {
    entries.forEach(entry => {
      const appName = this.getAppNameFromEntry(entry);
      
      if (!this.performanceEntries.has(appName)) {
        this.performanceEntries.set(appName, []);
      }
      
      this.performanceEntries.get(appName)!.push(entry);
      this.updateAppMetrics(appName, entry);
    });
  }
  
  private getAppNameFromEntry(entry: PerformanceEntry): string {
    // 从性能条目中推断应用名称
    if (entry.name.includes('localhost:')) {
      const match = entry.name.match(/localhost:(\d+)/);
      return match ? `app-${match[1]}` : 'unknown';
    }
    
    // 从标记名称中提取应用名称
    if (entry.entryType === 'mark' || entry.entryType === 'measure') {
      const parts = entry.name.split('-');
      if (parts.length > 1) {
        return parts[0];
      }
    }
    
    return 'main';
  }
  
  private updateAppMetrics(appName: string, entry: PerformanceEntry): void {
    if (!this.appMetrics.has(appName)) {
      this.appMetrics.set(appName, {
        appName,
        loadTime: 0,
        renderTime: 0,
        resourceCount: 0,
        resourceSize: 0,
        errorCount: 0,
        lastUpdated: Date.now()
      });
    }
    
    const metrics = this.appMetrics.get(appName)!;
    
    switch (entry.entryType) {
      case 'navigation':
        const navEntry = entry as PerformanceNavigationTiming;
        metrics.loadTime = navEntry.loadEventEnd - navEntry.navigationStart;
        break;
        
      case 'resource':
        const resourceEntry = entry as PerformanceResourceTiming;
        metrics.resourceCount++;
        metrics.resourceSize += resourceEntry.transferSize || 0;
        break;
        
      case 'measure':
        if (entry.name.includes('render')) {
          metrics.renderTime = entry.duration;
        }
        break;
    }
    
    metrics.lastUpdated = Date.now();
  }
  
  // 标记应用加载开始
  markAppLoadStart(appName: string): void {
    performance.mark(`${appName}-load-start`);
  }
  
  // 标记应用加载结束
  markAppLoadEnd(appName: string): void {
    performance.mark(`${appName}-load-end`);
    performance.measure(
      `${appName}-load-time`,
      `${appName}-load-start`,
      `${appName}-load-end`
    );
  }
  
  // 标记应用渲染开始
  markAppRenderStart(appName: string): void {
    performance.mark(`${appName}-render-start`);
  }
  
  // 标记应用渲染结束
  markAppRenderEnd(appName: string): void {
    performance.mark(`${appName}-render-end`);
    performance.measure(
      `${appName}-render-time`,
      `${appName}-render-start`,
      `${appName}-render-end`
    );
  }
  
  // 获取应用性能指标
  getAppMetrics(appName: string): AppMetrics | null {
    return this.appMetrics.get(appName) || null;
  }
  
  // 获取所有应用的性能指标
  getAllMetrics(): AppMetrics[] {
    return Array.from(this.appMetrics.values());
  }
  
  // 获取性能报告
  getPerformanceReport(): PerformanceReport {
    const allMetrics = this.getAllMetrics();
    
    const totalLoadTime = allMetrics.reduce((sum, metrics) => sum + metrics.loadTime, 0);
    const averageLoadTime = allMetrics.length > 0 ? totalLoadTime / allMetrics.length : 0;
    
    const totalResourceCount = allMetrics.reduce((sum, metrics) => sum + metrics.resourceCount, 0);
    const totalResourceSize = allMetrics.reduce((sum, metrics) => sum + metrics.resourceSize, 0);
    
    const slowestApp = allMetrics.reduce((slowest, current) => {
      return current.loadTime > slowest.loadTime ? current : slowest;
    }, allMetrics[0] || null);
    
    const fastestApp = allMetrics.reduce((fastest, current) => {
      return current.loadTime < fastest.loadTime ? current : fastest;
    }, allMetrics[0] || null);
    
    return {
      totalApps: allMetrics.length,
      averageLoadTime,
      totalResourceCount,
      totalResourceSize,
      slowestApp,
      fastestApp,
      recommendations: this.generateRecommendations(allMetrics)
    };
  }
  
  private generateRecommendations(metrics: AppMetrics[]): string[] {
    const recommendations: string[] = [];
    
    // 检查加载时间
    const slowApps = metrics.filter(m => m.loadTime > 3000);
    if (slowApps.length > 0) {
      recommendations.push(
        `${slowApps.length} 个应用加载时间超过3秒，建议优化资源加载策略`
      );
    }
    
    // 检查资源大小
    const heavyApps = metrics.filter(m => m.resourceSize > 1024 * 1024); // 1MB
    if (heavyApps.length > 0) {
      recommendations.push(
        `${heavyApps.length} 个应用资源大小超过1MB，建议进行代码分割和压缩`
      );
    }
    
    // 检查资源数量
    const resourceHeavyApps = metrics.filter(m => m.resourceCount > 50);
    if (resourceHeavyApps.length > 0) {
      recommendations.push(
        `${resourceHeavyApps.length} 个应用资源数量过多，建议合并和优化资源`
      );
    }
    
    return recommendations;
  }
  
  // 清理性能数据
  cleanup(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
    
    this.performanceEntries.clear();
    this.appMetrics.clear();
  }
}

// 类型定义
interface AppMetrics {
  appName: string;
  loadTime: number;
  renderTime: number;
  resourceCount: number;
  resourceSize: number;
  errorCount: number;
  lastUpdated: number;
}

interface PerformanceReport {
  totalApps: number;
  averageLoadTime: number;
  totalResourceCount: number;
  totalResourceSize: number;
  slowestApp: AppMetrics | null;
  fastestApp: AppMetrics | null;
  recommendations: string[];
}

// 全局性能监控器
export const performanceMonitor = new MicroFrontendPerformanceMonitor();
```

## 6. 最佳实践与总结

### 6.1 架构设计最佳实践

#### 应用拆分原则

1. **业务边界清晰**：按照业务域进行拆分，确保每个微应用有明确的业务职责
2. **技术栈独立**：允许不同团队选择最适合的技术栈
3. **数据隔离**：避免微应用间的直接数据依赖
4. **接口标准化**：定义统一的通信协议和数据格式

#### 通信机制设计

```typescript
// 统一的微前端通信总线
class MicroFrontendEventBus {
  private events: Map<string, EventHandler[]> = new Map();
  private globalState: Map<string, any> = new Map();
  private stateSubscribers: Map<string, StateSubscriber[]> = new Map();
  
  // 事件发布
  emit(eventName: string, data: any, source?: string): void {
    const handlers = this.events.get(eventName) || [];
    
    handlers.forEach(handler => {
      try {
        handler({
          type: eventName,
          data,
          source: source || 'unknown',
          timestamp: Date.now()
        });
      } catch (error) {
        console.error(`Error in event handler for ${eventName}:`, error);
      }
    });
  }
  
  // 事件订阅
  on(eventName: string, handler: EventHandler): () => void {
    if (!this.events.has(eventName)) {
      this.events.set(eventName, []);
    }
    
    this.events.get(eventName)!.push(handler);
    
    // 返回取消订阅函数
    return () => {
      const handlers = this.events.get(eventName);
      if (handlers) {
        const index = handlers.indexOf(handler);
        if (index > -1) {
          handlers.splice(index, 1);
        }
      }
    };
  }
  
  // 状态管理
  setState(key: string, value: any): void {
    const oldValue = this.globalState.get(key);
    this.globalState.set(key, value);
    
    // 通知订阅者
    const subscribers = this.stateSubscribers.get(key) || [];
    subscribers.forEach(subscriber => {
      try {
        subscriber(value, oldValue);
      } catch (error) {
        console.error(`Error in state subscriber for ${key}:`, error);
      }
    });
  }
  
  getState(key: string): any {
    return this.globalState.get(key);
  }
  
  // 状态订阅
  subscribe(key: string, subscriber: StateSubscriber): () => void {
    if (!this.stateSubscribers.has(key)) {
      this.stateSubscribers.set(key, []);
    }
    
    this.stateSubscribers.get(key)!.push(subscriber);
    
    // 立即调用一次，传递当前值
    const currentValue = this.globalState.get(key);
    if (currentValue !== undefined) {
      subscriber(currentValue, undefined);
    }
    
    // 返回取消订阅函数
    return () => {
      const subscribers = this.stateSubscribers.get(key);
      if (subscribers) {
        const index = subscribers.indexOf(subscriber);
        if (index > -1) {
          subscribers.splice(index, 1);
        }
      }
    };
  }
}

type EventHandler = (event: MicroFrontendEvent) => void;
type StateSubscriber = (newValue: any, oldValue: any) => void;

interface MicroFrontendEvent {
  type: string;
  data: any;
  source: string;
  timestamp: number;
}

// 全局事件总线
export const eventBus = new MicroFrontendEventBus();
```

### 6.2 开发与部署最佳实践

#### 开发环境配置

```typescript
// 开发环境微前端配置
const developmentConfig = {
  // 主应用配置
  main: {
    port: 3000,
    devServer: {
      hot: true,
      historyApiFallback: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        }
      }
    },
    microApps: [
      {
        name: 'user-management',
        entry: 'http://localhost:3001',
        activeRule: '/user'
      },
      {
        name: 'order-system',
        entry: 'http://localhost:3002',
        activeRule: '/order'
      }
    ]
  },
  
  // 微应用配置
  microApps: {
    'user-management': {
      port: 3001,
      publicPath: 'http://localhost:3001/',
      headers: {
        'Access-Control-Allow-Origin': '*'
      }
    },
    'order-system': {
      port: 3002,
      publicPath: 'http://localhost:3002/',
      headers: {
        'Access-Control-Allow-Origin': '*'
      }
    }
  }
};

// 生产环境配置
const productionConfig = {
  main: {
    microApps: [
      {
        name: 'user-management',
        entry: 'https://cdn.example.com/user-management/',
        activeRule: '/user'
      },
      {
        name: 'order-system',
        entry: 'https://cdn.example.com/order-system/',
        activeRule: '/order'
      }
    ]
  }
};
```

#### CI/CD集成

```yaml
# .github/workflows/micro-frontend.yml
name: Micro Frontend CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        app: [main, user-management, order-system]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: '${{ matrix.app }}/package-lock.json'
    
    - name: Install dependencies
      run: |
        cd ${{ matrix.app }}
        npm ci
    
    - name: Run tests
      run: |
        cd ${{ matrix.app }}
        npm run test:ci
    
    - name: Build application
      run: |
        cd ${{ matrix.app }}
        npm run build
    
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: ${{ matrix.app }}-build
        path: ${{ matrix.app }}/dist

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Download build artifacts
      uses: actions/download-artifact@v3
    
    - name: Deploy to CDN
      run: |
        # 部署到CDN的脚本
        aws s3 sync main-build/ s3://cdn.example.com/main/ --delete
        aws s3 sync user-management-build/ s3://cdn.example.com/user-management/ --delete
        aws s3 sync order-system-build/ s3://cdn.example.com/order-system/ --delete
    
    - name: Invalidate CDN cache
      run: |
        aws cloudfront create-invalidation --distribution-id ${{ secrets.CLOUDFRONT_DISTRIBUTION_ID }} --paths "/*"
```

### 6.3 监控与运维

```typescript
// 微前端监控仪表板
class MicroFrontendDashboard {
  private metricsCollector: MetricsCollector;
  private alertManager: AlertManager;
  
  constructor() {
    this.metricsCollector = new MetricsCollector();
    this.alertManager = new AlertManager();
    this.setupDashboard();
  }
  
  private setupDashboard(): void {
    // 设置实时监控
    setInterval(() => {
      this.collectMetrics();
      this.checkAlerts();
    }, 30000); // 每30秒收集一次指标
  }
  
  private async collectMetrics(): Promise<void> {
    const metrics = {
      // 应用状态
      appStatus: this.getAppStatus(),
      
      // 性能指标
      performance: performanceMonitor.getAllMetrics(),
      
      // 错误统计
      errors: microFrontendErrorHandler.getErrorStatistics(),
      
      // 资源使用
      resources: this.getResourceUsage(),
      
      // 用户体验指标
      userExperience: this.getUserExperienceMetrics()
    };
    
    await this.metricsCollector.collect(metrics);
  }
  
  private getAppStatus(): AppStatus[] {
    // 获取所有微应用的状态
    return [
      {
        name: 'user-management',
        status: 'healthy',
        uptime: 99.9,
        lastHealthCheck: Date.now()
      },
      {
        name: 'order-system',
        status: 'healthy',
        uptime: 99.8,
        lastHealthCheck: Date.now()
      }
    ];
  }
  
  private getResourceUsage(): ResourceUsage {
    return {
      memory: this.getMemoryUsage(),
      cpu: this.getCPUUsage(),
      network: this.getNetworkUsage(),
      storage: this.getStorageUsage()
    };
  }
  
  private getUserExperienceMetrics(): UserExperienceMetrics {
    return {
      pageLoadTime: this.getAveragePageLoadTime(),
      interactionTime: this.getAverageInteractionTime(),
      errorRate: this.getErrorRate(),
      bounceRate: this.getBounceRate()
    };
  }
  
  // 生成监控报告
  generateReport(timeRange: TimeRange): MonitoringReport {
    const metrics = this.metricsCollector.getMetrics(timeRange);
    
    return {
      timeRange,
      summary: {
        totalApps: metrics.appStatus.length,
        healthyApps: metrics.appStatus.filter(app => app.status === 'healthy').length,
        averageUptime: this.calculateAverageUptime(metrics.appStatus),
        totalErrors: metrics.errors.totalErrors,
        averageLoadTime: this.calculateAverageLoadTime(metrics.performance)
      },
      details: metrics,
      recommendations: this.generateRecommendations(metrics),
      alerts: this.alertManager.getActiveAlerts()
    };
  }
}

// 类型定义
interface AppStatus {
  name: string;
  status: 'healthy' | 'warning' | 'error';
  uptime: number;
  lastHealthCheck: number;
}

interface ResourceUsage {
  memory: number;
  cpu: number;
  network: number;
  storage: number;
}

interface UserExperienceMetrics {
  pageLoadTime: number;
  interactionTime: number;
  errorRate: number;
  bounceRate: number;
}

interface MonitoringReport {
  timeRange: TimeRange;
  summary: {
    totalApps: number;
    healthyApps: number;
    averageUptime: number;
    totalErrors: number;
    averageLoadTime: number;
  };
  details: any;
  recommendations: string[];
  alerts: Alert[];
}

interface TimeRange {
  start: number;
  end: number;
}
```

## 结语

微前端架构作为现代前端开发的重要趋势，为大型应用的开发和维护提供了有效的解决方案。通过本文的深入探讨，我们了解了从qiankun到Module Federation的技术演进，以及微前端架构在实际项目中的应用实践。

### 核心价值

1. **技术栈自由度**：团队可以根据业务需求选择最适合的技术栈
2. **独立开发部署**：提高开发效率，降低团队间的耦合度
3. **渐进式升级**：支持老系统的平滑迁移和技术栈升级
4. **团队自治**：每个团队可以独立负责自己的微应用

### 技术选型建议

- **qiankun**：适合需要强隔离、多技术栈共存的场景
- **Module Federation**：适合Webpack生态、需要细粒度模块共享的场景
- **single-spa**：适合需要高度定制化的复杂场景

### 未来发展趋势

1. **标准化进程**：微前端规范的逐步完善
2. **工具链成熟**：开发、构建、部署工具的持续优化
3. **性能优化**：更好的资源共享和加载策略
4. **云原生集成**：与容器化、服务网格的深度结合

微前端架构不是银弹，需要根据项目规模、团队结构和业务需求进行合理选择。通过合理的架构设计、完善的工程化实践和持续的监控优化，微前端架构能够为现代前端应用带来显著的价值。

---

**相关技术文档：**
- [qiankun官方文档](https://qiankun.umijs.org/)
- [Module Federation文档](https://webpack.js.org/concepts/module-federation/)
- [single-spa官方文档](https://single-spa.js.org/)
- [微前端最佳实践指南](https://micro-frontends.org/)
```

这篇文章深入探讨了微前端架构的核心概念和主流实现方案，从qiankun的沙箱隔离到Module Federation的模块联邦，提供了完整的技术实现和最佳实践。文章内容涵盖了微前端架构设计、应用改造、样式隔离、动态模块加载、共享依赖管理等关键技术点，为开发者提供了构建大型前端应用的完整解决方案。