# 前端Sentry与Vue生态集成：构建响应式监控体系

在Vue.js生态系统中，构建一个全面的监控体系对于保障应用质量和用户体验至关重要。本文将深入探讨如何将Sentry与Vue生态进行深度集成，构建一个响应式、高效的前端监控解决方案。

## 1. Vue监控集成器

### 1.1 核心监控集成

```javascript
// Vue监控集成器
import { createApp } from 'vue';
import * as Sentry from '@sentry/vue';
import { BrowserTracing } from '@sentry/tracing';
import { CaptureConsole } from '@sentry/integrations';

class VueMonitoringIntegrator {
  constructor(options = {}) {
    this.options = {
      dsn: options.dsn,
      environment: options.environment || 'production',
      release: options.release,
      enablePerformanceMonitoring: options.enablePerformanceMonitoring !== false,
      enableErrorBoundary: options.enableErrorBoundary !== false,
      enableUserFeedback: options.enableUserFeedback !== false,
      enableComponentTracking: options.enableComponentTracking !== false,
      enableVuexTracking: options.enableVuexTracking !== false,
      enableRouterTracking: options.enableRouterTracking !== false,
      sampleRate: options.sampleRate || 1.0,
      tracesSampleRate: options.tracesSampleRate || 0.1,
      beforeSend: options.beforeSend,
      beforeSendTransaction: options.beforeSendTransaction,
      ...options
    };
    
    this.app = null;
    this.router = null;
    this.store = null;
    this.componentMetrics = new Map();
    this.performanceMetrics = new Map();
    this.userInteractions = [];
    this.errorHistory = [];
    
    this.initializeSentry();
  }
  
  // 初始化Sentry
  initializeSentry() {
    const integrations = [
      new BrowserTracing({
        routingInstrumentation: Sentry.vueRouterInstrumentation(this.router),
        tracingOrigins: ['localhost', /^\//],
      }),
      new CaptureConsole({
        levels: ['error', 'warn']
      })
    ];
    
    // 添加自定义集成
    if (this.options.enableComponentTracking) {
      integrations.push(this.createComponentTrackingIntegration());
    }
    
    Sentry.init({
      dsn: this.options.dsn,
      environment: this.options.environment,
      release: this.options.release,
      integrations,
      sampleRate: this.options.sampleRate,
      tracesSampleRate: this.options.tracesSampleRate,
      beforeSend: this.enhanceErrorEvent.bind(this),
      beforeSendTransaction: this.enhanceTransactionEvent.bind(this),
      initialScope: {
        tags: {
          framework: 'vue',
          version: this.getVueVersion()
        }
      }
    });
  }
  
  // 集成Vue应用
  integrateWithApp(app, options = {}) {
    this.app = app;
    
    // 配置Vue错误处理
    app.config.errorHandler = (error, instance, info) => {
      this.handleVueError(error, instance, info);
    };
    
    // 配置Vue警告处理
    app.config.warnHandler = (msg, instance, trace) => {
      this.handleVueWarning(msg, instance, trace);
    };
    
    // 安装Sentry Vue插件
    app.use(Sentry.createTracingMixins({
      trackComponents: this.options.enableComponentTracking,
      timeout: 2000,
      hooks: ['activate', 'mount', 'update']
    }));
    
    // 添加全局属性
    app.config.globalProperties.$sentry = Sentry;
    app.config.globalProperties.$captureException = Sentry.captureException;
    app.config.globalProperties.$captureMessage = Sentry.captureMessage;
    
    // 注册全局组件
    if (this.options.enableErrorBoundary) {
      app.component('SentryErrorBoundary', this.createErrorBoundaryComponent());
    }
    
    if (this.options.enableUserFeedback) {
      app.component('SentryUserFeedback', this.createUserFeedbackComponent());
    }
    
    // 设置性能监控
    if (this.options.enablePerformanceMonitoring) {
      this.setupPerformanceMonitoring(app);
    }
    
    return app;
  }
  
  // 集成Vue Router
  integrateWithRouter(router) {
    this.router = router;
    
    if (!this.options.enableRouterTracking) {
      return router;
    }
    
    // 路由导航监控
    router.beforeEach((to, from, next) => {
      const transaction = Sentry.startTransaction({
        name: `Route: ${to.name || to.path}`,
        op: 'navigation',
        tags: {
          'route.name': to.name,
          'route.path': to.path,
          'route.params': JSON.stringify(to.params),
          'route.query': JSON.stringify(to.query)
        }
      });
      
      Sentry.getCurrentHub().configureScope(scope => {
        scope.setSpan(transaction);
        scope.setTag('route', to.path);
        scope.setContext('route', {
          name: to.name,
          path: to.path,
          params: to.params,
          query: to.query,
          meta: to.meta
        });
      });
      
      // 记录路由变化
      this.recordRouteChange(to, from);
      
      next();
    });
    
    router.afterEach((to, from) => {
      // 完成路由事务
      const transaction = Sentry.getCurrentHub().getScope()?.getSpan();
      if (transaction) {
        transaction.setStatus('ok');
        transaction.finish();
      }
      
      // 记录页面视图
      this.recordPageView(to);
    });
    
    // 路由错误处理
    router.onError((error) => {
      Sentry.withScope(scope => {
        scope.setTag('error-type', 'router-error');
        scope.setContext('router', {
          currentRoute: router.currentRoute.value
        });
        
        Sentry.captureException(error);
      });
    });
    
    return router;
  }
  
  // 集成Vuex/Pinia
  integrateWithStore(store, storeType = 'vuex') {
    this.store = store;
    this.storeType = storeType;
    
    if (!this.options.enableVuexTracking) {
      return store;
    }
    
    if (storeType === 'vuex') {
      this.integrateWithVuex(store);
    } else if (storeType === 'pinia') {
      this.integrateWithPinia(store);
    }
    
    return store;
  }
  
  // Vuex集成
  integrateWithVuex(store) {
    // 订阅mutations
    store.subscribe((mutation, state) => {
      this.recordVuexMutation(mutation, state);
    });
    
    // 订阅actions
    store.subscribeAction({
      before: (action, state) => {
        this.recordVuexActionStart(action, state);
      },
      after: (action, state) => {
        this.recordVuexActionEnd(action, state);
      },
      error: (action, state, error) => {
        this.recordVuexActionError(action, state, error);
      }
    });
  }
  
  // Pinia集成
  integrateWithPinia(pinia) {
    // Pinia插件
    pinia.use(({ store, app }) => {
      // 监听store变化
      store.$subscribe((mutation, state) => {
        this.recordPiniaMutation(store.$id, mutation, state);
      });
      
      // 监听actions
      store.$onAction(({ name, store, args, after, onError }) => {
        const startTime = Date.now();
        
        this.recordPiniaActionStart(store.$id, name, args);
        
        after((result) => {
          this.recordPiniaActionEnd(store.$id, name, result, Date.now() - startTime);
        });
        
        onError((error) => {
          this.recordPiniaActionError(store.$id, name, error, Date.now() - startTime);
        });
      });
    });
  }
  
  // 处理Vue错误
  handleVueError(error, instance, info) {
    const componentName = this.getComponentName(instance);
    const componentProps = this.getComponentProps(instance);
    
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'vue-error');
      scope.setTag('component', componentName);
      scope.setContext('vue', {
        componentName,
        componentProps: this.sanitizeProps(componentProps),
        errorInfo: info,
        componentTree: this.getComponentTree(instance)
      });
      
      // 添加面包屑
      Sentry.addBreadcrumb({
        category: 'vue-error',
        message: `Error in component: ${componentName}`,
        level: 'error',
        data: {
          componentName,
          errorInfo: info
        }
      });
      
      Sentry.captureException(error);
    });
    
    // 记录错误历史
    this.errorHistory.push({
      timestamp: Date.now(),
      error: error.message,
      component: componentName,
      info
    });
    
    // 保持错误历史在合理范围内
    if (this.errorHistory.length > 100) {
      this.errorHistory.shift();
    }
  }
  
  // 处理Vue警告
  handleVueWarning(msg, instance, trace) {
    const componentName = this.getComponentName(instance);
    
    Sentry.addBreadcrumb({
      category: 'vue-warning',
      message: `Warning in component: ${componentName}`,
      level: 'warning',
      data: {
        componentName,
        warning: msg,
        trace
      }
    });
  }
  
  // 记录路由变化
  recordRouteChange(to, from) {
    Sentry.addBreadcrumb({
      category: 'navigation',
      message: `Route changed from ${from.path} to ${to.path}`,
      level: 'info',
      data: {
        from: {
          name: from.name,
          path: from.path,
          params: from.params
        },
        to: {
          name: to.name,
          path: to.path,
          params: to.params
        }
      }
    });
  }
  
  // 记录页面视图
  recordPageView(route) {
    Sentry.addBreadcrumb({
      category: 'page-view',
      message: `Page view: ${route.path}`,
      level: 'info',
      data: {
        route: {
          name: route.name,
          path: route.path,
          params: route.params,
          query: route.query
        },
        timestamp: Date.now()
      }
    });
    
    // 设置用户上下文
    Sentry.setContext('page', {
      name: route.name,
      path: route.path,
      url: window.location.href,
      referrer: document.referrer
    });
  }
  
  // 记录Vuex mutation
  recordVuexMutation(mutation, state) {
    Sentry.addBreadcrumb({
      category: 'vuex-mutation',
      message: `Mutation: ${mutation.type}`,
      level: 'info',
      data: {
        type: mutation.type,
        payload: this.sanitizePayload(mutation.payload),
        timestamp: Date.now()
      }
    });
  }
  
  // 记录Vuex action开始
  recordVuexActionStart(action, state) {
    const actionId = `${action.type}_${Date.now()}`;
    
    Sentry.addBreadcrumb({
      category: 'vuex-action-start',
      message: `Action started: ${action.type}`,
      level: 'info',
      data: {
        type: action.type,
        payload: this.sanitizePayload(action.payload),
        actionId,
        timestamp: Date.now()
      }
    });
    
    return actionId;
  }
  
  // 记录Vuex action结束
  recordVuexActionEnd(action, state) {
    Sentry.addBreadcrumb({
      category: 'vuex-action-end',
      message: `Action completed: ${action.type}`,
      level: 'info',
      data: {
        type: action.type,
        timestamp: Date.now()
      }
    });
  }
  
  // 记录Vuex action错误
  recordVuexActionError(action, state, error) {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'vuex-action-error');
      scope.setTag('action', action.type);
      scope.setContext('vuex', {
        action: {
          type: action.type,
          payload: this.sanitizePayload(action.payload)
        },
        state: this.sanitizeState(state)
      });
      
      Sentry.captureException(error);
    });
  }
  
  // 记录Pinia mutation
  recordPiniaMutation(storeId, mutation, state) {
    Sentry.addBreadcrumb({
      category: 'pinia-mutation',
      message: `Store mutation: ${storeId}`,
      level: 'info',
      data: {
        storeId,
        mutation: {
          type: mutation.type,
          events: mutation.events
        },
        timestamp: Date.now()
      }
    });
  }
  
  // 记录Pinia action开始
  recordPiniaActionStart(storeId, actionName, args) {
    Sentry.addBreadcrumb({
      category: 'pinia-action-start',
      message: `Action started: ${storeId}.${actionName}`,
      level: 'info',
      data: {
        storeId,
        actionName,
        args: this.sanitizePayload(args),
        timestamp: Date.now()
      }
    });
  }
  
  // 记录Pinia action结束
  recordPiniaActionEnd(storeId, actionName, result, duration) {
    Sentry.addBreadcrumb({
      category: 'pinia-action-end',
      message: `Action completed: ${storeId}.${actionName}`,
      level: 'info',
      data: {
        storeId,
        actionName,
        duration,
        timestamp: Date.now()
      }
    });
  }
  
  // 记录Pinia action错误
  recordPiniaActionError(storeId, actionName, error, duration) {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'pinia-action-error');
      scope.setTag('store', storeId);
      scope.setTag('action', actionName);
      scope.setContext('pinia', {
        storeId,
        actionName,
        duration
      });
      
      Sentry.captureException(error);
    });
  }
  
  // 获取组件名称
  getComponentName(instance) {
    if (!instance) return 'Unknown';
    
    return instance.$options?.name || 
           instance.$options?._componentTag || 
           instance.constructor?.name || 
           'Anonymous';
  }
  
  // 获取组件属性
  getComponentProps(instance) {
    if (!instance || !instance.$props) return {};
    
    return { ...instance.$props };
  }
  
  // 获取组件树
  getComponentTree(instance) {
    const tree = [];
    let current = instance;
    
    while (current && tree.length < 10) {
      tree.push(this.getComponentName(current));
      current = current.$parent;
    }
    
    return tree;
  }
  
  // 清理敏感属性
  sanitizeProps(props) {
    const sanitized = {};
    const sensitiveKeys = ['password', 'token', 'secret', 'key', 'auth'];
    
    for (const [key, value] of Object.entries(props)) {
      if (sensitiveKeys.some(sensitive => key.toLowerCase().includes(sensitive))) {
        sanitized[key] = '[Filtered]';
      } else if (typeof value === 'function') {
        sanitized[key] = '[Function]';
      } else if (typeof value === 'object' && value !== null) {
        try {
          const serialized = JSON.stringify(value);
          sanitized[key] = serialized.length > 200 ? '[Object]' : value;
        } catch {
          sanitized[key] = '[Object]';
        }
      } else {
        sanitized[key] = value;
      }
    }
    
    return sanitized;
  }
  
  // 清理载荷数据
  sanitizePayload(payload) {
    if (typeof payload === 'undefined') return undefined;
    if (payload === null) return null;
    
    try {
      const serialized = JSON.stringify(payload);
      return serialized.length > 500 ? '[Large Payload]' : payload;
    } catch {
      return '[Unserializable Payload]';
    }
  }
  
  // 清理状态数据
  sanitizeState(state) {
    const sanitized = {};
    const maxDepth = 3;
    
    const sanitizeValue = (value, depth = 0) => {
      if (depth > maxDepth) return '[Max Depth Reached]';
      
      if (typeof value === 'function') return '[Function]';
      if (value instanceof Date) return value.toISOString();
      if (value instanceof RegExp) return value.toString();
      
      if (Array.isArray(value)) {
        return value.length > 10 
          ? `[Array(${value.length})]` 
          : value.map(item => sanitizeValue(item, depth + 1));
      }
      
      if (typeof value === 'object' && value !== null) {
        const result = {};
        const keys = Object.keys(value);
        
        if (keys.length > 20) {
          return `[Object with ${keys.length} keys]`;
        }
        
        for (const key of keys) {
          result[key] = sanitizeValue(value[key], depth + 1);
        }
        
        return result;
      }
      
      return value;
    };
    
    return sanitizeValue(state);
  }
  
  // 获取Vue版本
  getVueVersion() {
    try {
      return require('vue/package.json').version;
    } catch {
      return 'unknown';
    }
  }
  
  // 增强错误事件
  enhanceErrorEvent(event, hint) {
    // 添加Vue特定的上下文
    if (this.app) {
      event.contexts = event.contexts || {};
      event.contexts.vue = {
        version: this.getVueVersion(),
        devtools: !!window.__VUE_DEVTOOLS_GLOBAL_HOOK__,
        ssr: typeof window === 'undefined'
      };
    }
    
    // 添加路由信息
    if (this.router) {
      event.contexts.router = {
        currentRoute: this.router.currentRoute.value
      };
    }
    
    // 添加store信息
    if (this.store) {
      event.contexts.store = {
        type: this.storeType,
        hasStore: true
      };
    }
    
    // 调用用户自定义的beforeSend
    if (this.options.beforeSend) {
      return this.options.beforeSend(event, hint);
    }
    
    return event;
  }
  
  // 增强事务事件
  enhanceTransactionEvent(event, hint) {
    // 添加性能指标
    if (this.performanceMetrics.size > 0) {
      event.contexts = event.contexts || {};
      event.contexts.performance = {
        metricsCount: this.performanceMetrics.size,
        componentCount: this.componentMetrics.size
      };
    }
    
    // 调用用户自定义的beforeSendTransaction
    if (this.options.beforeSendTransaction) {
      return this.options.beforeSendTransaction(event, hint);
    }
    
    return event;
  }
}

export { VueMonitoringIntegrator };
```

## 2. Vue组件监控器

### 2.1 组件生命周期监控

```javascript
// Vue组件监控器
import { ref, reactive, onMounted, onUnmounted, onUpdated, watch, nextTick } from 'vue';
import * as Sentry from '@sentry/vue';

class VueComponentMonitor {
  constructor(options = {}) {
    this.options = {
      enableLifecycleTracking: options.enableLifecycleTracking !== false,
      enablePropsTracking: options.enablePropsTracking !== false,
      enablePerformanceTracking: options.enablePerformanceTracking !== false,
      enableRenderTracking: options.enableRenderTracking !== false,
      enableReactivityTracking: options.enableReactivityTracking !== false,
      slowRenderThreshold: options.slowRenderThreshold || 16,
      componentBlacklist: options.componentBlacklist || [],
      maxPropsDepth: options.maxPropsDepth || 3,
      ...options
    };
    
    this.componentInstances = new Map();
    this.renderMetrics = new Map();
    this.reactivityMetrics = new Map();
    this.lifecycleEvents = [];
    
    this.setupGlobalMixin();
  }
  
  // 设置全局混入
  setupGlobalMixin() {
    return {
      beforeCreate() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('beforeCreate', componentName);
      },
      
      created() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('created', componentName);
        this.setupComponentMonitoring(componentName);
      },
      
      beforeMount() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('beforeMount', componentName);
        this.startRenderMeasurement(componentName);
      },
      
      mounted() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('mounted', componentName);
        this.endRenderMeasurement(componentName, 'mount');
        this.setupReactivityMonitoring(componentName);
      },
      
      beforeUpdate() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('beforeUpdate', componentName);
        this.startRenderMeasurement(componentName);
      },
      
      updated() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('updated', componentName);
        this.endRenderMeasurement(componentName, 'update');
      },
      
      beforeUnmount() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('beforeUnmount', componentName);
      },
      
      unmounted() {
        const componentName = this.getComponentName();
        if (this.isBlacklisted(componentName)) return;
        
        this.recordLifecycleEvent('unmounted', componentName);
        this.cleanupComponentMonitoring(componentName);
      },
      
      errorCaptured(error, instance, info) {
        const componentName = this.getComponentName();
        this.recordComponentError(error, componentName, instance, info);
        return false; // 继续传播错误
      },
      
      methods: {
        getComponentName() {
          return this.$options.name || 
                 this.$options._componentTag || 
                 this.constructor.name || 
                 'Anonymous';
        },
        
        isBlacklisted(componentName) {
          return this.options.componentBlacklist.includes(componentName);
        },
        
        recordLifecycleEvent(event, componentName) {
          if (!this.options.enableLifecycleTracking) return;
          
          const eventData = {
            timestamp: Date.now(),
            event,
            componentName,
            instanceId: this.$.uid
          };
          
          this.lifecycleEvents.push(eventData);
          
          // 保持事件历史在合理范围内
          if (this.lifecycleEvents.length > 1000) {
            this.lifecycleEvents.shift();
          }
          
          Sentry.addBreadcrumb({
            category: 'component-lifecycle',
            message: `${event}: ${componentName}`,
            level: 'info',
            data: eventData
          });
        },
        
        setupComponentMonitoring(componentName) {
          const instanceData = {
            name: componentName,
            instanceId: this.$.uid,
            createdAt: Date.now(),
            renderCount: 0,
            totalRenderTime: 0,
            avgRenderTime: 0,
            maxRenderTime: 0,
            minRenderTime: Infinity,
            slowRenders: 0,
            propsChanges: 0,
            reactivityTriggers: 0
          };
          
          this.componentInstances.set(this.$.uid, instanceData);
          
          // 监控Props变化
          if (this.options.enablePropsTracking && this.$props) {
            this.setupPropsWatching(componentName);
          }
        },
        
        setupPropsWatching(componentName) {
          const propsProxy = new Proxy(this.$props, {
            set: (target, property, value, receiver) => {
              const oldValue = target[property];
              const result = Reflect.set(target, property, value, receiver);
              
              if (oldValue !== value) {
                this.recordPropsChange(componentName, property, oldValue, value);
              }
              
              return result;
            }
          });
          
          // 深度监听Props对象
          this.watchPropsDeep(componentName, this.$props);
        },
        
        watchPropsDeep(componentName, props, path = '') {
          for (const [key, value] of Object.entries(props)) {
            const fullPath = path ? `${path}.${key}` : key;
            
            if (typeof value === 'object' && value !== null && this.getDepth(fullPath) < this.options.maxPropsDepth) {
              this.watchPropsDeep(componentName, value, fullPath);
            }
            
            // 使用Vue 3的watch API
            watch(
              () => this.getNestedValue(this.$props, fullPath),
              (newValue, oldValue) => {
                if (newValue !== oldValue) {
                  this.recordPropsChange(componentName, fullPath, oldValue, newValue);
                }
              },
              { deep: true }
            );
          }
        },
        
        getNestedValue(obj, path) {
          return path.split('.').reduce((current, key) => current?.[key], obj);
        },
        
        getDepth(path) {
          return path.split('.').length;
        },
        
        recordPropsChange(componentName, property, oldValue, newValue) {
          const instanceData = this.componentInstances.get(this.$.uid);
          if (instanceData) {
            instanceData.propsChanges++;
          }
          
          Sentry.addBreadcrumb({
            category: 'props-change',
            message: `Props changed in ${componentName}: ${property}`,
            level: 'info',
            data: {
              componentName,
              property,
              oldValue: this.sanitizeValue(oldValue),
              newValue: this.sanitizeValue(newValue),
              instanceId: this.$.uid,
              timestamp: Date.now()
            }
          });
        },
        
        setupReactivityMonitoring(componentName) {
          if (!this.options.enableReactivityTracking) return;
          
          // 监控响应式数据变化
          const reactiveData = this.$data;
          if (reactiveData) {
            this.watchReactiveData(componentName, reactiveData);
          }
          
          // 监控computed属性
          const computedProps = this.$options.computed;
          if (computedProps) {
            this.watchComputedProps(componentName, computedProps);
          }
        },
        
        watchReactiveData(componentName, data, path = '') {
          for (const [key, value] of Object.entries(data)) {
            const fullPath = path ? `${path}.${key}` : key;
            
            watch(
              () => this.getNestedValue(this.$data, fullPath),
              (newValue, oldValue) => {
                this.recordReactivityTrigger(componentName, fullPath, oldValue, newValue);
              },
              { deep: true }
            );
            
            if (typeof value === 'object' && value !== null && this.getDepth(fullPath) < 3) {
              this.watchReactiveData(componentName, value, fullPath);
            }
          }
        },
        
        watchComputedProps(componentName, computedProps) {
          for (const [key, computedFn] of Object.entries(computedProps)) {
            watch(
              () => this[key],
              (newValue, oldValue) => {
                this.recordComputedChange(componentName, key, oldValue, newValue);
              }
            );
          }
        },
        
        recordReactivityTrigger(componentName, property, oldValue, newValue) {
          const instanceData = this.componentInstances.get(this.$.uid);
          if (instanceData) {
            instanceData.reactivityTriggers++;
          }
          
          if (!this.reactivityMetrics.has(componentName)) {
            this.reactivityMetrics.set(componentName, {
              totalTriggers: 0,
              properties: new Map()
            });
          }
          
          const metrics = this.reactivityMetrics.get(componentName);
          metrics.totalTriggers++;
          
          if (!metrics.properties.has(property)) {
            metrics.properties.set(property, 0);
          }
          metrics.properties.set(property, metrics.properties.get(property) + 1);
          
          Sentry.addBreadcrumb({
            category: 'reactivity-trigger',
            message: `Reactive data changed in ${componentName}: ${property}`,
            level: 'info',
            data: {
              componentName,
              property,
              oldValue: this.sanitizeValue(oldValue),
              newValue: this.sanitizeValue(newValue),
              instanceId: this.$.uid,
              timestamp: Date.now()
            }
          });
        },
        
        recordComputedChange(componentName, property, oldValue, newValue) {
          Sentry.addBreadcrumb({
            category: 'computed-change',
            message: `Computed property changed in ${componentName}: ${property}`,
            level: 'info',
            data: {
              componentName,
              property,
              oldValue: this.sanitizeValue(oldValue),
              newValue: this.sanitizeValue(newValue),
              instanceId: this.$.uid,
              timestamp: Date.now()
            }
          });
        },
        
        startRenderMeasurement(componentName) {
          if (!this.options.enableRenderTracking) return;
          
          this._renderStartTime = performance.now();
          
          // 使用Performance API标记
          if (typeof performance !== 'undefined' && performance.mark) {
            performance.mark(`${componentName}-render-start`);
          }
        },
        
        endRenderMeasurement(componentName, phase) {
          if (!this.options.enableRenderTracking || !this._renderStartTime) return;
          
          const renderEndTime = performance.now();
          const renderDuration = renderEndTime - this._renderStartTime;
          
          // 使用Performance API测量
          if (typeof performance !== 'undefined' && performance.measure) {
            try {
              performance.measure(
                `${componentName}-${phase}`,
                `${componentName}-render-start`
              );
            } catch (error) {
              // 忽略测量错误
            }
          }
          
          this.recordRenderMetrics(componentName, phase, renderDuration);
          
          delete this._renderStartTime;
        },
        
        recordRenderMetrics(componentName, phase, duration) {
          const instanceData = this.componentInstances.get(this.$.uid);
          if (instanceData) {
            instanceData.renderCount++;
            instanceData.totalRenderTime += duration;
            instanceData.avgRenderTime = instanceData.totalRenderTime / instanceData.renderCount;
            instanceData.maxRenderTime = Math.max(instanceData.maxRenderTime, duration);
            instanceData.minRenderTime = Math.min(instanceData.minRenderTime, duration);
            
            if (duration > this.options.slowRenderThreshold) {
              instanceData.slowRenders++;
              this.recordSlowRender(componentName, phase, duration);
            }
          }
          
          if (!this.renderMetrics.has(componentName)) {
            this.renderMetrics.set(componentName, {
              totalRenders: 0,
              totalTime: 0,
              avgTime: 0,
              maxTime: 0,
              minTime: Infinity,
              slowRenders: 0,
              phases: new Map()
            });
          }
          
          const metrics = this.renderMetrics.get(componentName);
          metrics.totalRenders++;
          metrics.totalTime += duration;
          metrics.avgTime = metrics.totalTime / metrics.totalRenders;
          metrics.maxTime = Math.max(metrics.maxTime, duration);
          metrics.minTime = Math.min(metrics.minTime, duration);
          
          if (!metrics.phases.has(phase)) {
            metrics.phases.set(phase, { count: 0, totalTime: 0, avgTime: 0 });
          }
          
          const phaseMetrics = metrics.phases.get(phase);
          phaseMetrics.count++;
          phaseMetrics.totalTime += duration;
          phaseMetrics.avgTime = phaseMetrics.totalTime / phaseMetrics.count;
          
          Sentry.addBreadcrumb({
            category: 'component-render',
            message: `Component rendered: ${componentName} (${phase})`,
            level: 'info',
            data: {
              componentName,
              phase,
              duration: Math.round(duration * 100) / 100,
              instanceId: this.$.uid,
              timestamp: Date.now()
            }
          });
        },
        
        recordSlowRender(componentName, phase, duration) {
          Sentry.addBreadcrumb({
            category: 'slow-render',
            message: `Slow render detected: ${componentName} (${phase})`,
            level: 'warning',
            data: {
              componentName,
              phase,
              duration: Math.round(duration * 100) / 100,
              threshold: this.options.slowRenderThreshold,
              instanceId: this.$.uid,
              timestamp: Date.now()
            }
          });
          
          // 如果渲染时间超过阈值很多，记录为性能问题
          if (duration > this.options.slowRenderThreshold * 3) {
            Sentry.withScope(scope => {
              scope.setTag('performance-issue', 'slow-render');
              scope.setTag('component', componentName);
              scope.setTag('phase', phase);
              scope.setContext('renderMetrics', {
                duration,
                threshold: this.options.slowRenderThreshold,
                phase,
                instanceId: this.$.uid
              });
              
              Sentry.captureMessage(
                `Very slow render detected in ${componentName} during ${phase}`,
                'warning'
              );
            });
          }
        },
        
        recordComponentError(error, componentName, instance, info) {
          Sentry.withScope(scope => {
            scope.setTag('error-type', 'component-error');
            scope.setTag('component', componentName);
            scope.setContext('vue-component', {
              componentName,
              instanceId: instance?.$.uid,
              errorInfo: info,
              props: this.sanitizeValue(instance?.$props),
              data: this.sanitizeValue(instance?.$data)
            });
            
            Sentry.captureException(error);
          });
        },
        
        cleanupComponentMonitoring(componentName) {
          const instanceData = this.componentInstances.get(this.$.uid);
          if (instanceData) {
            const lifespan = Date.now() - instanceData.createdAt;
            
            Sentry.addBreadcrumb({
              category: 'component-cleanup',
              message: `Component cleanup: ${componentName}`,
              level: 'info',
              data: {
                componentName,
                instanceId: this.$.uid,
                lifespan,
                renderCount: instanceData.renderCount,
                avgRenderTime: instanceData.avgRenderTime,
                propsChanges: instanceData.propsChanges,
                reactivityTriggers: instanceData.reactivityTriggers
              }
            });
            
            this.componentInstances.delete(this.$.uid);
          }
        },
        
        sanitizeValue(value) {
          if (typeof value === 'function') return '[Function]';
          if (value instanceof Date) return value.toISOString();
          if (value instanceof RegExp) return value.toString();
          
          if (typeof value === 'object' && value !== null) {
            try {
              const serialized = JSON.stringify(value);
              return serialized.length > 200 ? '[Object]' : value;
            } catch {
              return '[Object]';
            }
          }
          
          return value;
        }
      }
    };
  }
  
  // 创建组件监控Composable
  createComponentMonitoringComposable(componentName) {
    return () => {
      const renderCount = ref(0);
      const renderTimes = ref([]);
      const propsChanges = ref(0);
      const reactivityTriggers = ref(0);
      
      const startTime = ref(0);
      
      // 渲染性能监控
      const startRenderMeasurement = () => {
        if (this.options.enableRenderTracking) {
          startTime.value = performance.now();
        }
      };
      
      const endRenderMeasurement = (phase = 'render') => {
        if (this.options.enableRenderTracking && startTime.value) {
          const duration = performance.now() - startTime.value;
          renderCount.value++;
          renderTimes.value.push(duration);
          
          // 保持渲染时间历史在合理范围内
          if (renderTimes.value.length > 50) {
            renderTimes.value.shift();
          }
          
          this.recordRenderMetrics(componentName, phase, duration);
          startTime.value = 0;
        }
      };
      
      // Props变化监控
      const trackPropsChange = (property, oldValue, newValue) => {
        propsChanges.value++;
        this.recordPropsChange(componentName, property, oldValue, newValue);
      };
      
      // 响应式数据变化监控
      const trackReactivityTrigger = (property, oldValue, newValue) => {
        reactivityTriggers.value++;
        this.recordReactivityTrigger(componentName, property, oldValue, newValue);
      };
      
      // 生命周期钩子
      onMounted(() => {
        this.recordLifecycleEvent('mounted', componentName);
      });
      
      onUpdated(() => {
        this.recordLifecycleEvent('updated', componentName);
      });
      
      onUnmounted(() => {
        this.recordLifecycleEvent('unmounted', componentName);
      });
      
      return {
        renderCount: readonly(renderCount),
        renderTimes: readonly(renderTimes),
        propsChanges: readonly(propsChanges),
        reactivityTriggers: readonly(reactivityTriggers),
        startRenderMeasurement,
        endRenderMeasurement,
        trackPropsChange,
        trackReactivityTrigger
      };
    };
  }
  
  // 获取组件统计
  getComponentStats(componentName) {
    const renderMetrics = this.renderMetrics.get(componentName);
    const reactivityMetrics = this.reactivityMetrics.get(componentName);
    const instances = Array.from(this.componentInstances.values())
      .filter(instance => instance.name === componentName);
    
    return {
      renderMetrics,
      reactivityMetrics,
      activeInstances: instances.length,
      instanceStats: instances
    };
  }
  
  // 获取所有组件统计
  getAllComponentStats() {
    const stats = {};
    
    for (const [componentName] of this.renderMetrics) {
      stats[componentName] = this.getComponentStats(componentName);
    }
    
    return stats;
  }
  
  // 生成监控报告
  generateMonitoringReport() {
    const slowComponents = Array.from(this.renderMetrics.entries())
      .filter(([, metrics]) => metrics.avgTime > this.options.slowRenderThreshold)
      .sort(([, a], [, b]) => b.avgTime - a.avgTime);
    
    const frequentComponents = Array.from(this.renderMetrics.entries())
      .sort(([, a], [, b]) => b.totalRenders - a.totalRenders)
      .slice(0, 10);
    
    const reactiveComponents = Array.from(this.reactivityMetrics.entries())
      .sort(([, a], [, b]) => b.totalTriggers - a.totalTriggers)
      .slice(0, 10);
    
    return {
      timestamp: new Date().toISOString(),
      summary: {
        totalComponents: this.renderMetrics.size,
        activeInstances: this.componentInstances.size,
        totalRenders: Array.from(this.renderMetrics.values())
          .reduce((sum, metrics) => sum + metrics.totalRenders, 0),
        avgRenderTime: this.calculateAvgRenderTime(),
        slowComponentsCount: slowComponents.length
      },
      slowComponents: slowComponents.map(([name, metrics]) => ({ name, ...metrics })),
      frequentComponents: frequentComponents.map(([name, metrics]) => ({ name, ...metrics })),
      reactiveComponents: reactiveComponents.map(([name, metrics]) => ({ name, ...metrics })),
      lifecycleEvents: this.lifecycleEvents.slice(-100),
      componentStats: this.getAllComponentStats()
    };
  }
  
  // 计算平均渲染时间
  calculateAvgRenderTime() {
    let totalTime = 0;
    let totalRenders = 0;
    
    for (const metrics of this.renderMetrics.values()) {
      totalTime += metrics.totalTime;
      totalRenders += metrics.totalRenders;
    }
    
    return totalRenders > 0 ? Math.round((totalTime / totalRenders) * 100) / 100 : 0;
  }
  
  // 清理资源
  cleanup() {
    this.componentInstances.clear();
    this.renderMetrics.clear();
    this.reactivityMetrics.clear();
    this.lifecycleEvents.length = 0;
  }
}

// 使用示例
const componentMonitor = new VueComponentMonitor({
  enableLifecycleTracking: true,
  enablePropsTracking: true,
  enablePerformanceTracking: true,
  enableRenderTracking: true,
  enableReactivityTracking: true,
  slowRenderThreshold: 16,
  componentBlacklist: ['DevTools', 'VueDevtools'],
  maxPropsDepth: 3
});

export { VueComponentMonitor };
```

## 3. Vue性能监控器

### 3.1 性能指标收集

```javascript
// Vue性能监控器
import { ref, reactive, computed, watch, nextTick } from 'vue';
import * as Sentry from '@sentry/vue';

class VuePerformanceMonitor {
  constructor(options = {}) {
    this.options = {
      enableWebVitals: options.enableWebVitals !== false,
      enableResourceMonitoring: options.enableResourceMonitoring !== false,
      enableUserInteractionMonitoring: options.enableUserInteractionMonitoring !== false,
      enableMemoryMonitoring: options.enableMemoryMonitoring !== false,
      enableNetworkMonitoring: options.enableNetworkMonitoring !== false,
      performanceThresholds: {
        fcp: 1800, // First Contentful Paint
        lcp: 2500, // Largest Contentful Paint
        fid: 100,  // First Input Delay
        cls: 0.1,  // Cumulative Layout Shift
        ttfb: 600, // Time to First Byte
        ...options.performanceThresholds
      },
      sampleRate: options.sampleRate || 0.1,
      ...options
    };
    
    this.performanceMetrics = reactive({
      webVitals: {},
      resources: [],
      userInteractions: [],
      memoryUsage: [],
      networkRequests: []
    });
    
    this.performanceObserver = null;
    this.intersectionObserver = null;
    this.mutationObserver = null;
    
    this.initializeMonitoring();
  }
  
  // 初始化监控
  initializeMonitoring() {
    if (typeof window === 'undefined') return;
    
    // 初始化Web Vitals监控
    if (this.options.enableWebVitals) {
      this.initializeWebVitalsMonitoring();
    }
    
    // 初始化资源监控
    if (this.options.enableResourceMonitoring) {
      this.initializeResourceMonitoring();
    }
    
    // 初始化用户交互监控
    if (this.options.enableUserInteractionMonitoring) {
      this.initializeUserInteractionMonitoring();
    }
    
    // 初始化内存监控
    if (this.options.enableMemoryMonitoring) {
      this.initializeMemoryMonitoring();
    }
    
    // 初始化网络监控
    if (this.options.enableNetworkMonitoring) {
      this.initializeNetworkMonitoring();
    }
  }
  
  // 初始化Web Vitals监控
  initializeWebVitalsMonitoring() {
    // First Contentful Paint (FCP)
    this.observePerformanceEntry('paint', (entries) => {
      for (const entry of entries) {
        if (entry.name === 'first-contentful-paint') {
          this.recordWebVital('FCP', entry.startTime, {
            threshold: this.options.performanceThresholds.fcp
          });
        }
      }
    });
    
    // Largest Contentful Paint (LCP)
    this.observePerformanceEntry('largest-contentful-paint', (entries) => {
      const lastEntry = entries[entries.length - 1];
      if (lastEntry) {
        this.recordWebVital('LCP', lastEntry.startTime, {
          threshold: this.options.performanceThresholds.lcp,
          element: lastEntry.element?.tagName
        });
      }
    });
    
    // First Input Delay (FID)
    this.observePerformanceEntry('first-input', (entries) => {
      for (const entry of entries) {
        this.recordWebVital('FID', entry.processingStart - entry.startTime, {
          threshold: this.options.performanceThresholds.fid,
          eventType: entry.name
        });
      }
    });
    
    // Cumulative Layout Shift (CLS)
    this.observePerformanceEntry('layout-shift', (entries) => {
      let clsValue = 0;
      for (const entry of entries) {
        if (!entry.hadRecentInput) {
          clsValue += entry.value;
        }
      }
      
      if (clsValue > 0) {
        this.recordWebVital('CLS', clsValue, {
          threshold: this.options.performanceThresholds.cls
        });
      }
    });
    
    // Time to First Byte (TTFB)
    this.observePerformanceEntry('navigation', (entries) => {
      for (const entry of entries) {
        const ttfb = entry.responseStart - entry.requestStart;
        this.recordWebVital('TTFB', ttfb, {
          threshold: this.options.performanceThresholds.ttfb
        });
      }
    });
  }
  
  // 观察性能条目
  observePerformanceEntry(entryType, callback) {
    if (!('PerformanceObserver' in window)) return;
    
    try {
      const observer = new PerformanceObserver((list) => {
        if (Math.random() <= this.options.sampleRate) {
          callback(list.getEntries());
        }
      });
      
      observer.observe({ entryTypes: [entryType] });
      
      if (!this.performanceObserver) {
        this.performanceObserver = observer;
      }
    } catch (error) {
      console.warn(`Failed to observe ${entryType}:`, error);
    }
  }
  
  // 记录Web Vital指标
  recordWebVital(name, value, metadata = {}) {
    const metric = {
      name,
      value: Math.round(value * 100) / 100,
      timestamp: Date.now(),
      url: window.location.href,
      ...metadata
    };
    
    this.performanceMetrics.webVitals[name] = metric;
    
    // 检查是否超过阈值
    const threshold = metadata.threshold;
    const isGood = threshold ? value <= threshold : true;
    const level = isGood ? 'info' : 'warning';
    
    Sentry.addBreadcrumb({
      category: 'web-vitals',
      message: `${name}: ${value}ms`,
      level,
      data: metric
    });
    
    // 如果性能指标很差，发送到Sentry
    if (!isGood && threshold && value > threshold * 2) {
      Sentry.withScope(scope => {
        scope.setTag('performance-issue', name.toLowerCase());
        scope.setContext('webVital', metric);
        
        Sentry.captureMessage(
          `Poor ${name} performance: ${value}ms (threshold: ${threshold}ms)`,
          'warning'
        );
      });
    }
  }
  
  // 初始化资源监控
  initializeResourceMonitoring() {
    this.observePerformanceEntry('resource', (entries) => {
      for (const entry of entries) {
        this.recordResourceMetric(entry);
      }
    });
    
    // 监控图片加载
    this.observeImageLoading();
  }
  
  // 记录资源指标
  recordResourceMetric(entry) {
    const resource = {
      name: entry.name,
      type: this.getResourceType(entry),
      size: entry.transferSize || entry.encodedBodySize || 0,
      duration: entry.duration,
      startTime: entry.startTime,
      timestamp: Date.now(),
      cached: entry.transferSize === 0 && entry.encodedBodySize > 0
    };
    
    this.performanceMetrics.resources.push(resource);
    
    // 保持资源列表在合理范围内
    if (this.performanceMetrics.resources.length > 500) {
      this.performanceMetrics.resources.shift();
    }
    
    // 检查慢资源
    if (resource.duration > 1000) {
      Sentry.addBreadcrumb({
        category: 'slow-resource',
        message: `Slow resource loading: ${resource.name}`,
        level: 'warning',
        data: resource
      });
    }
    
    // 检查大资源
    if (resource.size > 1024 * 1024) { // 1MB
      Sentry.addBreadcrumb({
        category: 'large-resource',
        message: `Large resource: ${resource.name}`,
        level: 'warning',
        data: resource
      });
    }
  }
  
  // 获取资源类型
  getResourceType(entry) {
    if (entry.initiatorType) {
      return entry.initiatorType;
    }
    
    const url = entry.name;
    if (url.match(/\.(js|mjs)$/)) return 'script';
    if (url.match(/\.(css)$/)) return 'stylesheet';
    if (url.match(/\.(png|jpg|jpeg|gif|svg|webp)$/)) return 'image';
    if (url.match(/\.(woff|woff2|ttf|eot)$/)) return 'font';
    
    return 'other';
  }
  
  // 观察图片加载
  observeImageLoading() {
    if (!('IntersectionObserver' in window)) return;
    
    this.intersectionObserver = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting && entry.target.tagName === 'IMG') {
          this.recordImageVisibility(entry.target);
        }
      }
    });
    
    // 观察现有图片
    document.querySelectorAll('img').forEach(img => {
      this.intersectionObserver.observe(img);
    });
    
    // 观察新添加的图片
    if ('MutationObserver' in window) {
      this.mutationObserver = new MutationObserver((mutations) => {
        for (const mutation of mutations) {
          for (const node of mutation.addedNodes) {
            if (node.nodeType === Node.ELEMENT_NODE) {
              if (node.tagName === 'IMG') {
                this.intersectionObserver.observe(node);
              } else {
                node.querySelectorAll?.('img').forEach(img => {
                  this.intersectionObserver.observe(img);
                });
              }
            }
          }
        }
      });
      
      this.mutationObserver.observe(document.body, {
        childList: true,
        subtree: true
      });
    }
  }
  
  // 记录图片可见性
  recordImageVisibility(img) {
    const imageMetric = {
      src: img.src,
      alt: img.alt,
      width: img.naturalWidth,
      height: img.naturalHeight,
      displayWidth: img.width,
      displayHeight: img.height,
      loading: img.loading,
      timestamp: Date.now()
    };
    
    Sentry.addBreadcrumb({
      category: 'image-visibility',
      message: `Image became visible: ${img.src}`,
      level: 'info',
      data: imageMetric
    });
  }
  
  // 初始化用户交互监控
  initializeUserInteractionMonitoring() {
    const interactionTypes = ['click', 'keydown', 'scroll', 'touchstart'];
    
    interactionTypes.forEach(type => {
      document.addEventListener(type, (event) => {
        this.recordUserInteraction(event);
      }, { passive: true });
    });
  }
  
  // 记录用户交互
  recordUserInteraction(event) {
    if (Math.random() > this.options.sampleRate) return;
    
    const interaction = {
      type: event.type,
      timestamp: Date.now(),
      target: this.getElementSelector(event.target),
      x: event.clientX,
      y: event.clientY
    };
    
    this.performanceMetrics.userInteractions.push(interaction);
    
    // 保持交互列表在合理范围内
    if (this.performanceMetrics.userInteractions.length > 200) {
      this.performanceMetrics.userInteractions.shift();
    }
    
    Sentry.addBreadcrumb({
      category: 'user-interaction',
      message: `User ${event.type} on ${interaction.target}`,
      level: 'info',
      data: interaction
    });
  }
  
  // 获取元素选择器
  getElementSelector(element) {
    if (!element) return 'unknown';
    
    if (element.id) {
      return `#${element.id}`;
    }
    
    if (element.className) {
      const classes = element.className.split(' ').filter(c => c).slice(0, 2);
      if (classes.length > 0) {
        return `${element.tagName.toLowerCase()}.${classes.join('.')}`;
      }
    }
    
    return element.tagName.toLowerCase();
  }
  
  // 初始化内存监控
  initializeMemoryMonitoring() {
    if (!('memory' in performance)) return;
    
    const checkMemory = () => {
      const memoryInfo = {
        usedJSHeapSize: performance.memory.usedJSHeapSize,
        totalJSHeapSize: performance.memory.totalJSHeapSize,
        jsHeapSizeLimit: performance.memory.jsHeapSizeLimit,
        timestamp: Date.now()
      };
      
      this.performanceMetrics.memoryUsage.push(memoryInfo);
      
      // 保持内存使用历史在合理范围内
      if (this.performanceMetrics.memoryUsage.length > 100) {
        this.performanceMetrics.memoryUsage.shift();
      }
      
      // 检查内存使用是否过高
      const usagePercent = (memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit) * 100;
      if (usagePercent > 80) {
        Sentry.addBreadcrumb({
          category: 'high-memory-usage',
          message: `High memory usage: ${Math.round(usagePercent)}%`,
          level: 'warning',
          data: memoryInfo
        });
      }
    };
    
    // 每30秒检查一次内存使用
    setInterval(checkMemory, 30000);
    checkMemory(); // 立即检查一次
  }
  
  // 初始化网络监控
  initializeNetworkMonitoring() {
    // 监控fetch请求
    this.interceptFetch();
    
    // 监控XMLHttpRequest
    this.interceptXHR();
  }
  
  // 拦截fetch请求
  interceptFetch() {
    const originalFetch = window.fetch;
    
    window.fetch = async (...args) => {
      const startTime = performance.now();
      const url = args[0];
      
      try {
        const response = await originalFetch(...args);
        const endTime = performance.now();
        
        this.recordNetworkRequest({
          url,
          method: args[1]?.method || 'GET',
          status: response.status,
          duration: endTime - startTime,
          type: 'fetch',
          success: response.ok
        });
        
        return response;
      } catch (error) {
        const endTime = performance.now();
        
        this.recordNetworkRequest({
          url,
          method: args[1]?.method || 'GET',
          duration: endTime - startTime,
          type: 'fetch',
          success: false,
          error: error.message
        });
        
        throw error;
      }
    };
  }
  
  // 拦截XMLHttpRequest
  interceptXHR() {
    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url, ...args) {
      this._startTime = performance.now();
      this._method = method;
      this._url = url;
      
      return originalOpen.call(this, method, url, ...args);
    };
    
    XMLHttpRequest.prototype.send = function(...args) {
      this.addEventListener('loadend', () => {
        const endTime = performance.now();
        
        this.recordNetworkRequest({
          url: this._url,
          method: this._method,
          status: this.status,
          duration: endTime - this._startTime,
          type: 'xhr',
          success: this.status >= 200 && this.status < 300
        });
      });
      
      return originalSend.call(this, ...args);
    };
  }
  
  // 记录网络请求
  recordNetworkRequest(request) {
    this.performanceMetrics.networkRequests.push({
      ...request,
      timestamp: Date.now()
    });
    
    // 保持网络请求历史在合理范围内
    if (this.performanceMetrics.networkRequests.length > 200) {
      this.performanceMetrics.networkRequests.shift();
    }
    
    // 记录慢请求
    if (request.duration > 3000) {
      Sentry.addBreadcrumb({
        category: 'slow-network-request',
        message: `Slow network request: ${request.url}`,
        level: 'warning',
        data: request
      });
    }
    
    // 记录失败请求
    if (!request.success) {
      Sentry.addBreadcrumb({
        category: 'failed-network-request',
        message: `Failed network request: ${request.url}`,
        level: 'error',
        data: request
      });
    }
  }
  
  // 获取性能摘要
  getPerformanceSummary() {
    const webVitals = this.performanceMetrics.webVitals;
    const resources = this.performanceMetrics.resources;
    const networkRequests = this.performanceMetrics.networkRequests;
    
    return {
      webVitals: {
        fcp: webVitals.FCP?.value,
        lcp: webVitals.LCP?.value,
        fid: webVitals.FID?.value,
        cls: webVitals.CLS?.value,
        ttfb: webVitals.TTFB?.value
      },
      resources: {
        total: resources.length,
        totalSize: resources.reduce((sum, r) => sum + r.size, 0),
        avgDuration: resources.length > 0 
          ? resources.reduce((sum, r) => sum + r.duration, 0) / resources.length 
          : 0,
        slowResources: resources.filter(r => r.duration > 1000).length
      },
      network: {
        totalRequests: networkRequests.length,
        failedRequests: networkRequests.filter(r => !r.success).length,
        avgDuration: networkRequests.length > 0
          ? networkRequests.reduce((sum, r) => sum + r.duration, 0) / networkRequests.length
          : 0,
        slowRequests: networkRequests.filter(r => r.duration > 3000).length
      },
      memory: {
        current: this.performanceMetrics.memoryUsage.slice(-1)[0],
        peak: this.performanceMetrics.memoryUsage.reduce((max, m) => 
          m.usedJSHeapSize > max.usedJSHeapSize ? m : max, 
          { usedJSHeapSize: 0 }
        )
      }
    };
  }
  
  // 生成性能报告
  generatePerformanceReport() {
    return {
      timestamp: new Date().toISOString(),
      url: window.location.href,
      userAgent: navigator.userAgent,
      summary: this.getPerformanceSummary(),
      metrics: {
        webVitals: this.performanceMetrics.webVitals,
        resources: this.performanceMetrics.resources.slice(-50),
        userInteractions: this.performanceMetrics.userInteractions.slice(-50),
        memoryUsage: this.performanceMetrics.memoryUsage.slice(-20),
        networkRequests: this.performanceMetrics.networkRequests.slice(-50)
      }
    };
  }
  
  // 清理资源
  cleanup() {
    if (this.performanceObserver) {
      this.performanceObserver.disconnect();
    }
    
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
    }
    
    if (this.mutationObserver) {
      this.mutationObserver.disconnect();
    }
    
    // 清空性能指标
    this.performanceMetrics.webVitals = {};
    this.performanceMetrics.resources.length = 0;
    this.performanceMetrics.userInteractions.length = 0;
    this.performanceMetrics.memoryUsage.length = 0;
    this.performanceMetrics.networkRequests.length = 0;
  }
}

// 使用示例
const performanceMonitor = new VuePerformanceMonitor({
  enableWebVitals: true,
  enableResourceMonitoring: true,
  enableUserInteractionMonitoring: true,
  enableMemoryMonitoring: true,
  enableNetworkMonitoring: true,
  performanceThresholds: {
    fcp: 1800,
    lcp: 2500,
    fid: 100,
    cls: 0.1,
    ttfb: 600
  },
  sampleRate: 0.1
});

export { VuePerformanceMonitor };
```

## 4. Vue错误边界组件

### 4.1 错误边界实现

```javascript
// Vue错误边界组件
import { defineComponent, ref, onErrorCaptured, provide, inject } from 'vue';
import * as Sentry from '@sentry/vue';

// 错误边界上下文
const ErrorBoundarySymbol = Symbol('ErrorBoundary');

// 错误边界组件
const ErrorBoundary = defineComponent({
  name: 'ErrorBoundary',
  props: {
    fallback: {
      type: [Object, Function],
      default: null
    },
    onError: {
      type: Function,
      default: null
    },
    isolate: {
      type: Boolean,
      default: false
    },
    resetOnPropsChange: {
      type: Boolean,
      default: true
    },
    resetKeys: {
      type: Array,
      default: () => []
    }
  },
  setup(props, { slots, emit }) {
    const hasError = ref(false);
    const error = ref(null);
    const errorInfo = ref(null);
    const errorId = ref(null);
    const retryCount = ref(0);
    const maxRetries = 3;
    
    // 提供错误边界上下文
    provide(ErrorBoundarySymbol, {
      hasError,
      error,
      errorInfo,
      retry: () => retry(),
      reset: () => reset()
    });
    
    // 捕获错误
    onErrorCaptured((err, instance, info) => {
      handleError(err, instance, info);
      return !props.isolate; // 如果isolate为true，阻止错误传播
    });
    
    // 处理错误
    const handleError = (err, instance, info) => {
      hasError.value = true;
      error.value = err;
      errorInfo.value = {
        componentStack: info,
        componentName: instance?.$options.name || 'Unknown',
        instanceId: instance?.$.uid,
        timestamp: new Date().toISOString(),
        retryCount: retryCount.value
      };
      
      // 生成错误ID
      errorId.value = generateErrorId(err, info);
      
      // 发送到Sentry
      sendErrorToSentry(err, instance, info);
      
      // 调用用户提供的错误处理函数
      if (props.onError) {
        props.onError(err, errorInfo.value);
      }
      
      // 触发错误事件
      emit('error', {
        error: err,
        errorInfo: errorInfo.value,
        errorId: errorId.value
      });
    };
    
    // 发送错误到Sentry
    const sendErrorToSentry = (err, instance, info) => {
      Sentry.withScope(scope => {
        scope.setTag('error-boundary', 'vue');
        scope.setTag('component', instance?.$options.name || 'Unknown');
        scope.setLevel('error');
        
        scope.setContext('errorBoundary', {
          componentStack: info,
          componentName: instance?.$options.name,
          instanceId: instance?.$.uid,
          retryCount: retryCount.value,
          errorId: errorId.value,
          props: sanitizeProps(instance?.$props),
          data: sanitizeData(instance?.$data)
        });
        
        scope.setContext('userAgent', {
          userAgent: navigator.userAgent,
          url: window.location.href,
          timestamp: new Date().toISOString()
        });
        
        Sentry.captureException(err);
      });
    };
    
    // 生成错误ID
    const generateErrorId = (err, info) => {
      const errorString = `${err.name}-${err.message}-${info}`;
      return btoa(errorString).slice(0, 16);
    };
    
    // 清理敏感数据
    const sanitizeProps = (props) => {
      if (!props) return null;
      
      const sanitized = {};
      for (const [key, value] of Object.entries(props)) {
        if (typeof value === 'function') {
          sanitized[key] = '[Function]';
        } else if (typeof value === 'object' && value !== null) {
          try {
            const serialized = JSON.stringify(value);
            sanitized[key] = serialized.length > 200 ? '[Object]' : value;
          } catch {
            sanitized[key] = '[Object]';
          }
        } else {
          sanitized[key] = value;
        }
      }
      return sanitized;
    };
    
    // 清理数据
    const sanitizeData = (data) => {
      if (!data) return null;
      
      try {
        const serialized = JSON.stringify(data);
        return serialized.length > 500 ? '[Data]' : data;
      } catch {
        return '[Data]';
      }
    };
    
    // 重试
    const retry = () => {
      if (retryCount.value < maxRetries) {
        retryCount.value++;
        reset();
        
        Sentry.addBreadcrumb({
          category: 'error-boundary',
          message: `Error boundary retry attempt ${retryCount.value}`,
          level: 'info',
          data: {
            errorId: errorId.value,
            retryCount: retryCount.value
          }
        });
      }
    };
    
    // 重置错误状态
    const reset = () => {
      hasError.value = false;
      error.value = null;
      errorInfo.value = null;
      errorId.value = null;
    };
    
    // 监听props变化以重置错误状态
    if (props.resetOnPropsChange) {
      watch(
        () => props.resetKeys.map(key => props[key]),
        () => {
          if (hasError.value) {
            reset();
          }
        },
        { deep: true }
      );
    }
    
    return {
      hasError,
      error,
      errorInfo,
      errorId,
      retryCount,
      maxRetries,
      retry,
      reset
    };
  },
  render() {
    if (this.hasError) {
      // 如果提供了fallback组件
      if (this.fallback) {
        if (typeof this.fallback === 'function') {
          return this.fallback({
            error: this.error,
            errorInfo: this.errorInfo,
            retry: this.retry,
            reset: this.reset,
            retryCount: this.retryCount,
            canRetry: this.retryCount < this.maxRetries
          });
        } else {
          return h(this.fallback, {
            error: this.error,
            errorInfo: this.errorInfo,
            retry: this.retry,
            reset: this.reset,
            retryCount: this.retryCount,
            canRetry: this.retryCount < this.maxRetries
          });
        }
      }
      
      // 默认错误UI
      return h('div', {
        class: 'error-boundary',
        style: {
          padding: '20px',
          border: '1px solid #ff6b6b',
          borderRadius: '4px',
          backgroundColor: '#ffe0e0',
          color: '#d63031'
        }
      }, [
        h('h3', '出现了一个错误'),
        h('p', this.error?.message || '未知错误'),
        h('details', {
          style: { marginTop: '10px' }
        }, [
          h('summary', '错误详情'),
          h('pre', {
            style: {
              marginTop: '10px',
              padding: '10px',
              backgroundColor: '#f8f9fa',
              borderRadius: '4px',
              fontSize: '12px',
              overflow: 'auto'
            }
          }, this.error?.stack || '无堆栈信息')
        ]),
        this.retryCount < this.maxRetries ? h('button', {
          onClick: this.retry,
          style: {
            marginTop: '10px',
            padding: '8px 16px',
            backgroundColor: '#0984e3',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }
        }, `重试 (${this.retryCount}/${this.maxRetries})`) : null
      ]);
    }
    
    // 正常渲染子组件
    return this.$slots.default?.();
  }
});

// 错误边界Hook
function useErrorBoundary() {
  const errorBoundary = inject(ErrorBoundarySymbol, null);
  
  if (!errorBoundary) {
    console.warn('useErrorBoundary must be used within an ErrorBoundary component');
    return {
      hasError: ref(false),
      error: ref(null),
      errorInfo: ref(null),
      retry: () => {},
      reset: () => {}
    };
  }
  
  return errorBoundary;
}

// 错误报告Hook
function useErrorReporting() {
  const reportError = (error, context = {}) => {
    Sentry.withScope(scope => {
      scope.setTag('error-source', 'manual');
      scope.setLevel('error');
      
      if (context.component) {
        scope.setTag('component', context.component);
      }
      
      if (context.action) {
        scope.setTag('action', context.action);
      }
      
      scope.setContext('errorContext', {
        ...context,
        timestamp: new Date().toISOString(),
        url: window.location.href
      });
      
      Sentry.captureException(error);
    });
  };
  
  const reportMessage = (message, level = 'info', context = {}) => {
    Sentry.withScope(scope => {
      scope.setLevel(level);
      scope.setTag('message-source', 'manual');
      
      scope.setContext('messageContext', {
        ...context,
        timestamp: new Date().toISOString(),
        url: window.location.href
      });
      
      Sentry.captureMessage(message, level);
    });
  };
  
  return {
    reportError,
    reportMessage
  };
}

export {
  ErrorBoundary,
  useErrorBoundary,
  useErrorReporting
};
```

### 4.2 使用示例

```vue
<template>
  <div id="app">
    <!-- 全局错误边界 -->
    <ErrorBoundary
      :fallback="GlobalErrorFallback"
      :on-error="handleGlobalError"
      :reset-keys="[user.id]"
    >
      <router-view />
    </ErrorBoundary>
    
    <!-- 组件级错误边界 -->
    <ErrorBoundary
      :fallback="ComponentErrorFallback"
      isolate
    >
      <UserProfile :user="user" />
    </ErrorBoundary>
    
    <!-- 功能级错误边界 -->
    <ErrorBoundary>
      <template #default>
        <DataTable :data="tableData" />
      </template>
      <template #fallback="{ error, retry, canRetry }">
        <div class="error-fallback">
          <h3>数据加载失败</h3>
          <p>{{ error.message }}</p>
          <button v-if="canRetry" @click="retry">
            重新加载
          </button>
        </div>
      </template>
    </ErrorBoundary>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ErrorBoundary, useErrorReporting } from './ErrorBoundary';

const { reportError, reportMessage } = useErrorReporting();

const user = reactive({
  id: 1,
  name: 'John Doe'
});

const tableData = ref([]);

// 全局错误处理
const handleGlobalError = (error, errorInfo) => {
  console.error('Global error:', error, errorInfo);
  
  // 可以在这里添加额外的错误处理逻辑
  // 比如显示通知、发送到其他监控服务等
};

// 全局错误回退组件
const GlobalErrorFallback = ({ error, retry, canRetry }) => {
  return h('div', {
    class: 'global-error-fallback'
  }, [
    h('h1', '应用出现错误'),
    h('p', '我们正在努力修复这个问题，请稍后再试。'),
    canRetry ? h('button', {
      onClick: retry
    }, '重试') : null
  ]);
};

// 组件错误回退组件
const ComponentErrorFallback = ({ error }) => {
  return h('div', {
    class: 'component-error-fallback'
  }, [
    h('p', '该组件暂时无法显示'),
    h('small', error.message)
  ]);
};

// 手动报告错误示例
const handleAsyncError = async () => {
  try {
    await someAsyncOperation();
  } catch (error) {
    reportError(error, {
      component: 'App',
      action: 'handleAsyncError',
      userId: user.id
    });
  }
};

// 手动报告消息示例
const logUserAction = (action) => {
  reportMessage(`User performed action: ${action}`, 'info', {
    userId: user.id,
    action
  });
};
</script>
```

## 5. 最佳实践与总结

### 5.1 实施建议

1. **渐进式集成**
   - 从核心功能开始集成Sentry
   - 逐步添加组件监控和性能监控
   - 根据实际需求调整监控粒度

2. **性能优化**
   - 合理设置采样率，避免性能影响
   - 使用黑名单过滤不重要的组件
   - 定期清理监控数据，防止内存泄漏

3. **错误处理策略**
   - 设置多层错误边界
   - 提供有意义的错误回退UI
   - 实现错误重试机制

4. **数据安全**
   - 过滤敏感数据
   - 限制数据传输大小
   - 遵循数据保护法规

### 5.2 核心价值

1. **响应式监控**
   - 深度集成Vue响应式系统
   - 实时监控组件状态变化
   - 精确定位性能瓶颈

2. **开发体验**
   - 提供丰富的调试信息
   - 支持组件级错误隔离
   - 简化错误处理流程

3. **生产稳定性**
   - 主动发现和修复问题
   - 提升应用可靠性
   - 优化用户体验

### 5.3 未来发展趋势

1. **智能化监控**
   - AI驱动的异常检测
   - 自动化性能优化建议
   - 智能错误分类和处理

2. **深度集成**
   - 与Vue DevTools深度集成
   - 支持更多Vue生态工具
   - 提供可视化监控面板

3. **边缘计算**
   - 客户端智能分析
   - 减少网络传输
   - 提升监控实时性

通过本文介绍的Vue生态集成方案，开发者可以构建一个全面、高效的前端监控体系，为Vue应用提供强大的错误监控、性能分析和用户体验优化能力。这不仅能够帮助开发团队快速定位和解决问题，还能为产品的持续改进提供数据支持。
```