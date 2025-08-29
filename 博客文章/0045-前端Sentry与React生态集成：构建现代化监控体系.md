# 前端Sentry与React生态集成：构建现代化监控体系

在现代前端开发中，React生态系统已经成为主流选择。将Sentry与React生态深度集成，可以构建一个全面、智能的监控体系，为React应用提供从开发到生产的全生命周期监控支持。

## 1. React生态监控架构

### 1.1 React监控集成器

```javascript
// React监控集成器
import React, { Component, ErrorBoundary } from 'react';
import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import * as Sentry from '@sentry/react';
import { Integrations } from '@sentry/tracing';

class ReactMonitoringIntegrator {
  constructor(options = {}) {
    this.options = {
      dsn: options.dsn,
      environment: options.environment || 'development',
      release: options.release,
      enableTracing: options.enableTracing !== false,
      enableErrorBoundary: options.enableErrorBoundary !== false,
      enablePerformanceMonitoring: options.enablePerformanceMonitoring !== false,
      enableUserFeedback: options.enableUserFeedback !== false,
      customIntegrations: options.customIntegrations || [],
      ...options
    };
    
    this.componentRegistry = new Map();
    this.performanceMetrics = new Map();
    this.userInteractions = [];
    this.renderMetrics = new Map();
    
    this.initializeSentry();
    this.setupReactIntegrations();
  }
  
  // 初始化Sentry
  initializeSentry() {
    const integrations = [
      new Integrations.BrowserTracing({
        routingInstrumentation: Sentry.reactRouterV6Instrumentation(
          React.useEffect,
          React.useLocation,
          React.useNavigationType,
          React.createMatchPath
        ),
        beforeNavigate: context => {
          return {
            ...context,
            name: this.getRouteName(context.location.pathname)
          };
        }
      }),
      new Sentry.Integrations.Replay({
        maskAllText: false,
        blockAllMedia: false,
        maskAllInputs: true,
        beforeAddRecordingEvent: (event) => {
          // 过滤敏感信息
          return this.filterSensitiveData(event);
        }
      }),
      ...this.options.customIntegrations
    ];
    
    Sentry.init({
      dsn: this.options.dsn,
      environment: this.options.environment,
      release: this.options.release,
      integrations,
      tracesSampleRate: this.options.enableTracing ? 0.1 : 0,
      replaysSessionSampleRate: 0.1,
      replaysOnErrorSampleRate: 1.0,
      beforeSend: (event) => {
        return this.enhanceEvent(event);
      },
      beforeSendTransaction: (transaction) => {
        return this.enhanceTransaction(transaction);
      }
    });
  }
  
  // 设置React集成
  setupReactIntegrations() {
    // 设置React错误边界
    if (this.options.enableErrorBoundary) {
      this.setupErrorBoundary();
    }
    
    // 设置性能监控
    if (this.options.enablePerformanceMonitoring) {
      this.setupPerformanceMonitoring();
    }
    
    // 设置用户反馈
    if (this.options.enableUserFeedback) {
      this.setupUserFeedback();
    }
    
    // 设置组件监控
    this.setupComponentMonitoring();
  }
  
  // 设置错误边界
  setupErrorBoundary() {
    this.SentryErrorBoundary = Sentry.withErrorBoundary(
      ({ children }) => children,
      {
        fallback: ({ error, resetError }) => (
          <div className="error-boundary">
            <h2>Something went wrong</h2>
            <details style={{ whiteSpace: 'pre-wrap' }}>
              {error && error.toString()}
            </details>
            <button onClick={resetError}>Try again</button>
          </div>
        ),
        beforeCapture: (scope, error, errorInfo) => {
          scope.setTag('errorBoundary', true);
          scope.setContext('errorInfo', errorInfo);
          scope.setContext('componentStack', {
            componentStack: errorInfo.componentStack
          });
        }
      }
    );
  }
  
  // 设置性能监控
  setupPerformanceMonitoring() {
    // 监控组件渲染性能
    this.withPerformanceMonitoring = (WrappedComponent) => {
      return React.forwardRef((props, ref) => {
        const [renderCount, setRenderCount] = useState(0);
        const [renderTime, setRenderTime] = useState(0);
        
        useEffect(() => {
          const startTime = performance.now();
          setRenderCount(prev => prev + 1);
          
          return () => {
            const endTime = performance.now();
            const duration = endTime - startTime;
            setRenderTime(duration);
            
            this.recordComponentPerformance(WrappedComponent.name, {
              renderCount: renderCount + 1,
              renderTime: duration,
              props: Object.keys(props).length
            });
          };
        });
        
        return React.createElement(WrappedComponent, { ...props, ref });
      });
    };
  }
  
  // 设置用户反馈
  setupUserFeedback() {
    this.showUserFeedback = (options = {}) => {
      const user = Sentry.getCurrentHub().getScope()?.getUser();
      
      Sentry.showReportDialog({
        title: 'Report a Bug',
        subtitle: 'Help us improve by reporting this issue',
        subtitle2: 'Our team will be notified',
        labelName: 'Name',
        labelEmail: 'Email',
        labelComments: 'What happened?',
        labelClose: 'Close',
        labelSubmit: 'Submit',
        errorGeneric: 'An error occurred while submitting your report',
        errorFormEntry: 'Some fields were invalid',
        successMessage: 'Thank you for your report!',
        user: {
          name: user?.username || '',
          email: user?.email || ''
        },
        ...options
      });
    };
  }
  
  // 设置组件监控
  setupComponentMonitoring() {
    this.withComponentMonitoring = (WrappedComponent) => {
      return React.forwardRef((props, ref) => {
        const componentName = WrappedComponent.displayName || WrappedComponent.name;
        
        useEffect(() => {
          // 记录组件挂载
          this.recordComponentLifecycle(componentName, 'mount', props);
          
          return () => {
            // 记录组件卸载
            this.recordComponentLifecycle(componentName, 'unmount', props);
          };
        }, []);
        
        useEffect(() => {
          // 记录组件更新
          this.recordComponentLifecycle(componentName, 'update', props);
        });
        
        const handleError = useCallback((error, errorInfo) => {
          Sentry.withScope(scope => {
            scope.setTag('component', componentName);
            scope.setContext('props', props);
            scope.setContext('errorInfo', errorInfo);
            Sentry.captureException(error);
          });
        }, [componentName, props]);
        
        return React.createElement(WrappedComponent, { 
          ...props, 
          ref,
          onError: handleError
        });
      });
    };
  }
  
  // 记录组件性能
  recordComponentPerformance(componentName, metrics) {
    if (!this.performanceMetrics.has(componentName)) {
      this.performanceMetrics.set(componentName, {
        totalRenders: 0,
        totalRenderTime: 0,
        avgRenderTime: 0,
        maxRenderTime: 0,
        minRenderTime: Infinity,
        propsChanges: 0
      });
    }
    
    const componentMetrics = this.performanceMetrics.get(componentName);
    componentMetrics.totalRenders = metrics.renderCount;
    componentMetrics.totalRenderTime += metrics.renderTime;
    componentMetrics.avgRenderTime = componentMetrics.totalRenderTime / componentMetrics.totalRenders;
    componentMetrics.maxRenderTime = Math.max(componentMetrics.maxRenderTime, metrics.renderTime);
    componentMetrics.minRenderTime = Math.min(componentMetrics.minRenderTime, metrics.renderTime);
    
    // 发送性能数据到Sentry
    if (metrics.renderTime > 100) { // 渲染时间超过100ms时记录
      Sentry.addBreadcrumb({
        category: 'performance',
        message: `Slow render: ${componentName}`,
        level: 'warning',
        data: {
          component: componentName,
          renderTime: metrics.renderTime,
          renderCount: metrics.renderCount,
          propsCount: metrics.props
        }
      });
    }
  }
  
  // 记录组件生命周期
  recordComponentLifecycle(componentName, lifecycle, props) {
    if (!this.componentRegistry.has(componentName)) {
      this.componentRegistry.set(componentName, {
        mounts: 0,
        unmounts: 0,
        updates: 0,
        errors: 0,
        lastProps: null
      });
    }
    
    const componentData = this.componentRegistry.get(componentName);
    
    switch (lifecycle) {
      case 'mount':
        componentData.mounts++;
        break;
      case 'unmount':
        componentData.unmounts++;
        break;
      case 'update':
        componentData.updates++;
        // 检查props变化
        if (componentData.lastProps && 
            JSON.stringify(props) !== JSON.stringify(componentData.lastProps)) {
          this.recordPropsChange(componentName, componentData.lastProps, props);
        }
        break;
    }
    
    componentData.lastProps = props;
    
    // 记录到Sentry
    Sentry.addBreadcrumb({
      category: 'component',
      message: `${componentName} ${lifecycle}`,
      level: 'info',
      data: {
        component: componentName,
        lifecycle,
        propsCount: Object.keys(props || {}).length
      }
    });
  }
  
  // 记录Props变化
  recordPropsChange(componentName, oldProps, newProps) {
    const changes = this.diffProps(oldProps, newProps);
    
    if (changes.length > 0) {
      Sentry.addBreadcrumb({
        category: 'props-change',
        message: `Props changed: ${componentName}`,
        level: 'info',
        data: {
          component: componentName,
          changes: changes.slice(0, 10) // 限制变化数量
        }
      });
    }
  }
  
  // 比较Props差异
  diffProps(oldProps, newProps) {
    const changes = [];
    const allKeys = new Set([...Object.keys(oldProps || {}), ...Object.keys(newProps || {})]);
    
    allKeys.forEach(key => {
      const oldValue = oldProps?.[key];
      const newValue = newProps?.[key];
      
      if (oldValue !== newValue) {
        changes.push({
          key,
          oldValue: this.serializeValue(oldValue),
          newValue: this.serializeValue(newValue)
        });
      }
    });
    
    return changes;
  }
  
  // 序列化值
  serializeValue(value) {
    if (typeof value === 'function') {
      return '[Function]';
    }
    if (typeof value === 'object' && value !== null) {
      try {
        return JSON.stringify(value).slice(0, 100);
      } catch {
        return '[Object]';
      }
    }
    return String(value).slice(0, 100);
  }
  
  // 获取路由名称
  getRouteName(pathname) {
    // 可以根据路由配置自定义路由名称
    const routeMap = {
      '/': 'Home',
      '/dashboard': 'Dashboard',
      '/profile': 'Profile',
      '/settings': 'Settings'
    };
    
    return routeMap[pathname] || pathname;
  }
  
  // 过滤敏感数据
  filterSensitiveData(event) {
    // 过滤敏感信息
    const sensitiveFields = ['password', 'token', 'secret', 'key', 'auth'];
    
    if (event.data && typeof event.data === 'object') {
      const filteredData = { ...event.data };
      
      sensitiveFields.forEach(field => {
        if (filteredData[field]) {
          filteredData[field] = '[Filtered]';
        }
      });
      
      return { ...event, data: filteredData };
    }
    
    return event;
  }
  
  // 增强事件
  enhanceEvent(event) {
    // 添加React相关上下文
    event.contexts = {
      ...event.contexts,
      react: {
        version: React.version,
        components: Array.from(this.componentRegistry.keys()),
        activeComponents: this.getActiveComponents()
      }
    };
    
    // 添加性能指标
    if (this.performanceMetrics.size > 0) {
      event.extra = {
        ...event.extra,
        performanceMetrics: this.getPerformanceSummary()
      };
    }
    
    return event;
  }
  
  // 增强事务
  enhanceTransaction(transaction) {
    // 添加组件渲染信息
    transaction.setData('componentMetrics', this.getPerformanceSummary());
    
    return transaction;
  }
  
  // 获取活跃组件
  getActiveComponents() {
    const activeComponents = [];
    
    for (const [componentName, data] of this.componentRegistry) {
      if (data.mounts > data.unmounts) {
        activeComponents.push({
          name: componentName,
          mounts: data.mounts,
          updates: data.updates
        });
      }
    }
    
    return activeComponents;
  }
  
  // 获取性能摘要
  getPerformanceSummary() {
    const summary = {};
    
    for (const [componentName, metrics] of this.performanceMetrics) {
      summary[componentName] = {
        totalRenders: metrics.totalRenders,
        avgRenderTime: Math.round(metrics.avgRenderTime * 100) / 100,
        maxRenderTime: Math.round(metrics.maxRenderTime * 100) / 100
      };
    }
    
    return summary;
  }
  
  // 创建监控上下文
  createMonitoringContext() {
    const MonitoringContext = createContext({
      recordUserAction: () => {},
      recordCustomMetric: () => {},
      showFeedback: () => {},
      getComponentMetrics: () => ({})
    });
    
    const MonitoringProvider = ({ children }) => {
      const recordUserAction = useCallback((action, data) => {
        Sentry.addBreadcrumb({
          category: 'user-action',
          message: action,
          level: 'info',
          data
        });
        
        this.userInteractions.push({
          action,
          data,
          timestamp: Date.now()
        });
      }, []);
      
      const recordCustomMetric = useCallback((name, value, unit = '') => {
        Sentry.setMeasurement(name, value, unit);
      }, []);
      
      const showFeedback = useCallback((options) => {
        this.showUserFeedback(options);
      }, []);
      
      const getComponentMetrics = useCallback((componentName) => {
        return this.performanceMetrics.get(componentName) || {};
      }, []);
      
      return React.createElement(MonitoringContext.Provider, {
        value: {
          recordUserAction,
          recordCustomMetric,
          showFeedback,
          getComponentMetrics
        }
      }, children);
    };
    
    const useMonitoring = () => {
      const context = useContext(MonitoringContext);
      if (!context) {
        throw new Error('useMonitoring must be used within MonitoringProvider');
      }
      return context;
    };
    
    return { MonitoringProvider, useMonitoring };
  }
  
  // 获取错误边界组件
  getErrorBoundary() {
    return this.SentryErrorBoundary;
  }
  
  // 获取性能监控HOC
  getPerformanceMonitoring() {
    return this.withPerformanceMonitoring;
  }
  
  // 获取组件监控HOC
  getComponentMonitoring() {
    return this.withComponentMonitoring;
  }
  
  // 生成监控报告
  generateMonitoringReport() {
    return {
      timestamp: new Date().toISOString(),
      components: Object.fromEntries(this.componentRegistry),
      performance: Object.fromEntries(this.performanceMetrics),
      userInteractions: this.userInteractions.slice(-100), // 最近100个交互
      summary: {
        totalComponents: this.componentRegistry.size,
        activeComponents: this.getActiveComponents().length,
        avgRenderTime: this.calculateAvgRenderTime(),
        totalUserInteractions: this.userInteractions.length
      }
    };
  }
  
  // 计算平均渲染时间
  calculateAvgRenderTime() {
    let totalTime = 0;
    let totalRenders = 0;
    
    for (const metrics of this.performanceMetrics.values()) {
      totalTime += metrics.totalRenderTime;
      totalRenders += metrics.totalRenders;
    }
    
    return totalRenders > 0 ? Math.round((totalTime / totalRenders) * 100) / 100 : 0;
  }
}

// 使用示例
const monitoringIntegrator = new ReactMonitoringIntegrator({
  dsn: 'YOUR_SENTRY_DSN',
  environment: 'production',
  enableTracing: true,
  enableErrorBoundary: true,
  enablePerformanceMonitoring: true,
  enableUserFeedback: true
});

// 导出组件和Hook
const { MonitoringProvider, useMonitoring } = monitoringIntegrator.createMonitoringContext();
const ErrorBoundary = monitoringIntegrator.getErrorBoundary();
const withPerformanceMonitoring = monitoringIntegrator.getPerformanceMonitoring();
const withComponentMonitoring = monitoringIntegrator.getComponentMonitoring();

export {
  ReactMonitoringIntegrator,
  MonitoringProvider,
  useMonitoring,
  ErrorBoundary,
  withPerformanceMonitoring,
  withComponentMonitoring
};
```

### 1.2 React Hook监控器

```javascript
// React Hook监控器
import { useEffect, useRef, useState, useCallback, useMemo } from 'react';
import * as Sentry from '@sentry/react';

class ReactHookMonitor {
  constructor(options = {}) {
    this.options = {
      enableHookTracking: options.enableHookTracking !== false,
      enableStateTracking: options.enableStateTracking !== false,
      enableEffectTracking: options.enableEffectTracking !== false,
      enablePerformanceTracking: options.enablePerformanceTracking !== false,
      maxStateHistory: options.maxStateHistory || 10,
      ...options
    };
    
    this.hookUsage = new Map();
    this.stateHistory = new Map();
    this.effectMetrics = new Map();
    
    this.setupHookMonitoring();
  }
  
  // 设置Hook监控
  setupHookMonitoring() {
    if (this.options.enableHookTracking) {
      this.createMonitoredHooks();
    }
  }
  
  // 创建监控的Hook
  createMonitoredHooks() {
    // 监控useState
    this.useMonitoredState = (initialState, stateName) => {
      const [state, setState] = useState(initialState);
      const stateRef = useRef(state);
      const componentName = this.getCurrentComponentName();
      
      // 记录状态变化
      useEffect(() => {
        if (this.options.enableStateTracking) {
          this.recordStateChange(componentName, stateName, stateRef.current, state);
        }
        stateRef.current = state;
      }, [state]);
      
      const monitoredSetState = useCallback((newState) => {
        const prevState = stateRef.current;
        setState(newState);
        
        // 记录状态更新
        this.recordStateUpdate(componentName, stateName, prevState, newState);
      }, [componentName, stateName]);
      
      return [state, monitoredSetState];
    };
    
    // 监控useEffect
    this.useMonitoredEffect = (effect, deps, effectName) => {
      const componentName = this.getCurrentComponentName();
      const effectId = `${componentName}_${effectName || 'anonymous'}`;
      
      useEffect(() => {
        const startTime = performance.now();
        
        // 记录Effect开始
        this.recordEffectStart(effectId, deps);
        
        let cleanup;
        try {
          cleanup = effect();
        } catch (error) {
          this.recordEffectError(effectId, error);
          throw error;
        }
        
        const endTime = performance.now();
        this.recordEffectComplete(effectId, endTime - startTime);
        
        return () => {
          if (typeof cleanup === 'function') {
            try {
              cleanup();
              this.recordEffectCleanup(effectId);
            } catch (error) {
              this.recordEffectCleanupError(effectId, error);
            }
          }
        };
      }, deps);
    };
    
    // 监控useCallback
    this.useMonitoredCallback = (callback, deps, callbackName) => {
      const componentName = this.getCurrentComponentName();
      const callbackId = `${componentName}_${callbackName || 'anonymous'}`;
      
      return useCallback((...args) => {
        const startTime = performance.now();
        
        try {
          const result = callback(...args);
          const endTime = performance.now();
          
          this.recordCallbackExecution(callbackId, {
            duration: endTime - startTime,
            argsCount: args.length,
            success: true
          });
          
          return result;
        } catch (error) {
          const endTime = performance.now();
          
          this.recordCallbackExecution(callbackId, {
            duration: endTime - startTime,
            argsCount: args.length,
            success: false,
            error: error.message
          });
          
          throw error;
        }
      }, deps);
    };
    
    // 监控useMemo
    this.useMonitoredMemo = (factory, deps, memoName) => {
      const componentName = this.getCurrentComponentName();
      const memoId = `${componentName}_${memoName || 'anonymous'}`;
      
      return useMemo(() => {
        const startTime = performance.now();
        
        try {
          const result = factory();
          const endTime = performance.now();
          
          this.recordMemoComputation(memoId, {
            duration: endTime - startTime,
            depsCount: deps?.length || 0,
            success: true
          });
          
          return result;
        } catch (error) {
          const endTime = performance.now();
          
          this.recordMemoComputation(memoId, {
            duration: endTime - startTime,
            depsCount: deps?.length || 0,
            success: false,
            error: error.message
          });
          
          throw error;
        }
      }, deps);
    };
    
    // 自定义Hook监控
    this.useCustomHookMonitor = (hookName, hookFunction) => {
      return (...args) => {
        const componentName = this.getCurrentComponentName();
        const hookId = `${componentName}_${hookName}`;
        
        useEffect(() => {
          this.recordHookUsage(hookId, args);
        }, []);
        
        try {
          return hookFunction(...args);
        } catch (error) {
          this.recordHookError(hookId, error);
          throw error;
        }
      };
    };
  }
  
  // 获取当前组件名称
  getCurrentComponentName() {
    // 通过错误堆栈获取组件名称
    const error = new Error();
    const stack = error.stack?.split('\n') || [];
    
    for (let i = 0; i < stack.length; i++) {
      const line = stack[i];
      if (line.includes('at ') && !line.includes('useMonitored') && !line.includes('ReactHookMonitor')) {
        const match = line.match(/at (\w+)/);
        if (match) {
          return match[1];
        }
      }
    }
    
    return 'UnknownComponent';
  }
  
  // 记录状态变化
  recordStateChange(componentName, stateName, prevState, newState) {
    const stateKey = `${componentName}_${stateName}`;
    
    if (!this.stateHistory.has(stateKey)) {
      this.stateHistory.set(stateKey, []);
    }
    
    const history = this.stateHistory.get(stateKey);
    history.push({
      timestamp: Date.now(),
      prevState: this.serializeState(prevState),
      newState: this.serializeState(newState),
      changed: prevState !== newState
    });
    
    // 保持历史记录在限制范围内
    if (history.length > this.options.maxStateHistory) {
      history.shift();
    }
    
    // 记录到Sentry
    if (prevState !== newState) {
      Sentry.addBreadcrumb({
        category: 'state-change',
        message: `State changed: ${componentName}.${stateName}`,
        level: 'info',
        data: {
          component: componentName,
          stateName,
          prevState: this.serializeState(prevState),
          newState: this.serializeState(newState)
        }
      });
    }
  }
  
  // 记录状态更新
  recordStateUpdate(componentName, stateName, prevState, newState) {
    Sentry.addBreadcrumb({
      category: 'state-update',
      message: `State updated: ${componentName}.${stateName}`,
      level: 'info',
      data: {
        component: componentName,
        stateName,
        hasChanged: prevState !== newState
      }
    });
  }
  
  // 记录Effect开始
  recordEffectStart(effectId, deps) {
    if (!this.effectMetrics.has(effectId)) {
      this.effectMetrics.set(effectId, {
        executions: 0,
        totalDuration: 0,
        errors: 0,
        cleanups: 0,
        lastDeps: null
      });
    }
    
    const metrics = this.effectMetrics.get(effectId);
    metrics.executions++;
    metrics.lastDeps = deps;
    
    Sentry.addBreadcrumb({
      category: 'effect-start',
      message: `Effect started: ${effectId}`,
      level: 'info',
      data: {
        effectId,
        depsCount: deps?.length || 0,
        execution: metrics.executions
      }
    });
  }
  
  // 记录Effect完成
  recordEffectComplete(effectId, duration) {
    const metrics = this.effectMetrics.get(effectId);
    if (metrics) {
      metrics.totalDuration += duration;
    }
    
    if (duration > 100) { // 超过100ms的Effect
      Sentry.addBreadcrumb({
        category: 'effect-slow',
        message: `Slow effect: ${effectId}`,
        level: 'warning',
        data: {
          effectId,
          duration,
          avgDuration: metrics ? metrics.totalDuration / metrics.executions : duration
        }
      });
    }
  }
  
  // 记录Effect错误
  recordEffectError(effectId, error) {
    const metrics = this.effectMetrics.get(effectId);
    if (metrics) {
      metrics.errors++;
    }
    
    Sentry.withScope(scope => {
      scope.setTag('hook-type', 'effect');
      scope.setTag('effect-id', effectId);
      scope.setContext('effectMetrics', metrics);
      Sentry.captureException(error);
    });
  }
  
  // 记录Effect清理
  recordEffectCleanup(effectId) {
    const metrics = this.effectMetrics.get(effectId);
    if (metrics) {
      metrics.cleanups++;
    }
    
    Sentry.addBreadcrumb({
      category: 'effect-cleanup',
      message: `Effect cleanup: ${effectId}`,
      level: 'info',
      data: {
        effectId,
        cleanups: metrics?.cleanups || 1
      }
    });
  }
  
  // 记录Effect清理错误
  recordEffectCleanupError(effectId, error) {
    Sentry.withScope(scope => {
      scope.setTag('hook-type', 'effect-cleanup');
      scope.setTag('effect-id', effectId);
      Sentry.captureException(error);
    });
  }
  
  // 记录Callback执行
  recordCallbackExecution(callbackId, metrics) {
    if (!this.hookUsage.has(callbackId)) {
      this.hookUsage.set(callbackId, {
        type: 'callback',
        executions: 0,
        totalDuration: 0,
        errors: 0
      });
    }
    
    const usage = this.hookUsage.get(callbackId);
    usage.executions++;
    usage.totalDuration += metrics.duration;
    
    if (!metrics.success) {
      usage.errors++;
    }
    
    if (metrics.duration > 50) { // 超过50ms的回调
      Sentry.addBreadcrumb({
        category: 'callback-slow',
        message: `Slow callback: ${callbackId}`,
        level: 'warning',
        data: {
          callbackId,
          duration: metrics.duration,
          argsCount: metrics.argsCount
        }
      });
    }
  }
  
  // 记录Memo计算
  recordMemoComputation(memoId, metrics) {
    if (!this.hookUsage.has(memoId)) {
      this.hookUsage.set(memoId, {
        type: 'memo',
        computations: 0,
        totalDuration: 0,
        errors: 0
      });
    }
    
    const usage = this.hookUsage.get(memoId);
    usage.computations++;
    usage.totalDuration += metrics.duration;
    
    if (!metrics.success) {
      usage.errors++;
    }
    
    if (metrics.duration > 100) { // 超过100ms的计算
      Sentry.addBreadcrumb({
        category: 'memo-slow',
        message: `Slow memo computation: ${memoId}`,
        level: 'warning',
        data: {
          memoId,
          duration: metrics.duration,
          depsCount: metrics.depsCount
        }
      });
    }
  }
  
  // 记录Hook使用
  recordHookUsage(hookId, args) {
    if (!this.hookUsage.has(hookId)) {
      this.hookUsage.set(hookId, {
        type: 'custom',
        usages: 0,
        lastArgs: null
      });
    }
    
    const usage = this.hookUsage.get(hookId);
    usage.usages++;
    usage.lastArgs = args;
    
    Sentry.addBreadcrumb({
      category: 'hook-usage',
      message: `Hook used: ${hookId}`,
      level: 'info',
      data: {
        hookId,
        argsCount: args?.length || 0,
        usage: usage.usages
      }
    });
  }
  
  // 记录Hook错误
  recordHookError(hookId, error) {
    const usage = this.hookUsage.get(hookId);
    if (usage) {
      usage.errors = (usage.errors || 0) + 1;
    }
    
    Sentry.withScope(scope => {
      scope.setTag('hook-type', 'custom');
      scope.setTag('hook-id', hookId);
      scope.setContext('hookUsage', usage);
      Sentry.captureException(error);
    });
  }
  
  // 序列化状态
  serializeState(state) {
    try {
      if (typeof state === 'function') {
        return '[Function]';
      }
      if (typeof state === 'object' && state !== null) {
        return JSON.stringify(state).slice(0, 200);
      }
      return String(state).slice(0, 200);
    } catch {
      return '[Unserializable]';
    }
  }
  
  // 获取Hook使用统计
  getHookUsageStats() {
    const stats = {
      totalHooks: this.hookUsage.size,
      byType: {},
      topUsed: [],
      errors: 0
    };
    
    for (const [hookId, usage] of this.hookUsage) {
      if (!stats.byType[usage.type]) {
        stats.byType[usage.type] = 0;
      }
      stats.byType[usage.type]++;
      
      stats.errors += usage.errors || 0;
      
      stats.topUsed.push({
        hookId,
        type: usage.type,
        usages: usage.usages || usage.executions || usage.computations || 0
      });
    }
    
    stats.topUsed.sort((a, b) => b.usages - a.usages).slice(0, 10);
    
    return stats;
  }
  
  // 获取状态历史
  getStateHistory(componentName, stateName) {
    const stateKey = `${componentName}_${stateName}`;
    return this.stateHistory.get(stateKey) || [];
  }
  
  // 获取Effect指标
  getEffectMetrics(effectId) {
    return this.effectMetrics.get(effectId) || {};
  }
  
  // 生成Hook监控报告
  generateHookReport() {
    return {
      timestamp: new Date().toISOString(),
      hookUsage: Object.fromEntries(this.hookUsage),
      stateHistory: Object.fromEntries(this.stateHistory),
      effectMetrics: Object.fromEntries(this.effectMetrics),
      summary: this.getHookUsageStats()
    };
  }
}

// 使用示例
const hookMonitor = new ReactHookMonitor({
  enableHookTracking: true,
  enableStateTracking: true,
  enableEffectTracking: true,
  enablePerformanceTracking: true
});

// 导出监控Hook
export const {
  useMonitoredState,
  useMonitoredEffect,
  useMonitoredCallback,
  useMonitoredMemo,
  useCustomHookMonitor
} = hookMonitor;

export { ReactHookMonitor };
```

## 2. Redux/Zustand状态监控

### 2.1 Redux监控中间件

```javascript
// Redux监控中间件
import * as Sentry from '@sentry/react';

class ReduxSentryMiddleware {
  constructor(options = {}) {
    this.options = {
      enableActionTracking: options.enableActionTracking !== false,
      enableStateTracking: options.enableStateTracking !== false,
      enablePerformanceTracking: options.enablePerformanceTracking !== false,
      actionBlacklist: options.actionBlacklist || [],
      stateBlacklist: options.stateBlacklist || [],
      maxStateSize: options.maxStateSize || 1000,
      ...options
    };
    
    this.actionHistory = [];
    this.stateSnapshots = [];
    this.performanceMetrics = new Map();
    
    this.middleware = this.createMiddleware();
  }
  
  // 创建中间件
  createMiddleware() {
    return (store) => (next) => (action) => {
      const startTime = performance.now();
      const prevState = store.getState();
      
      // 记录Action开始
      this.recordActionStart(action, prevState);
      
      let result;
      try {
        result = next(action);
        const endTime = performance.now();
        const nextState = store.getState();
        
        // 记录Action完成
        this.recordActionComplete(action, prevState, nextState, endTime - startTime);
        
      } catch (error) {
        const endTime = performance.now();
        
        // 记录Action错误
        this.recordActionError(action, prevState, error, endTime - startTime);
        throw error;
      }
      
      return result;
    };
  }
  
  // 记录Action开始
  recordActionStart(action, state) {
    if (this.isActionBlacklisted(action.type)) {
      return;
    }
    
    if (this.options.enableActionTracking) {
      Sentry.addBreadcrumb({
        category: 'redux-action',
        message: `Action dispatched: ${action.type}`,
        level: 'info',
        data: {
          type: action.type,
          payload: this.sanitizePayload(action.payload),
          timestamp: Date.now()
        }
      });
    }
  }
  
  // 记录Action完成
  recordActionComplete(action, prevState, nextState, duration) {
    if (this.isActionBlacklisted(action.type)) {
      return;
    }
    
    // 记录性能指标
    if (this.options.enablePerformanceTracking) {
      this.recordActionPerformance(action.type, duration);
    }
    
    // 记录状态变化
    if (this.options.enableStateTracking) {
      this.recordStateChange(action, prevState, nextState);
    }
    
    // 保存Action历史
    this.actionHistory.push({
      type: action.type,
      payload: this.sanitizePayload(action.payload),
      timestamp: Date.now(),
      duration,
      stateChanged: prevState !== nextState
    });
    
    // 保持历史记录在合理范围内
    if (this.actionHistory.length > 100) {
      this.actionHistory.shift();
    }
    
    // 检查慢Action
    if (duration > 100) {
      Sentry.addBreadcrumb({
        category: 'redux-slow-action',
        message: `Slow action: ${action.type}`,
        level: 'warning',
        data: {
          type: action.type,
          duration,
          payload: this.sanitizePayload(action.payload)
        }
      });
    }
  }
  
  // 记录Action错误
  recordActionError(action, state, error, duration) {
    Sentry.withScope(scope => {
      scope.setTag('redux-action', action.type);
      scope.setContext('action', {
        type: action.type,
        payload: this.sanitizePayload(action.payload),
        duration
      });
      scope.setContext('state', this.sanitizeState(state));
      
      Sentry.captureException(error);
    });
  }
  
  // 记录Action性能
  recordActionPerformance(actionType, duration) {
    if (!this.performanceMetrics.has(actionType)) {
      this.performanceMetrics.set(actionType, {
        count: 0,
        totalDuration: 0,
        avgDuration: 0,
        maxDuration: 0,
        minDuration: Infinity
      });
    }
    
    const metrics = this.performanceMetrics.get(actionType);
    metrics.count++;
    metrics.totalDuration += duration;
    metrics.avgDuration = metrics.totalDuration / metrics.count;
    metrics.maxDuration = Math.max(metrics.maxDuration, duration);
    metrics.minDuration = Math.min(metrics.minDuration, duration);
  }
  
  // 记录状态变化
  recordStateChange(action, prevState, nextState) {
    if (prevState === nextState) {
      return;
    }
    
    const stateChanges = this.diffState(prevState, nextState);
    
    if (stateChanges.length > 0) {
      Sentry.addBreadcrumb({
        category: 'redux-state-change',
        message: `State changed by: ${action.type}`,
        level: 'info',
        data: {
          actionType: action.type,
          changes: stateChanges.slice(0, 10), // 限制变化数量
          changesCount: stateChanges.length
        }
      });
      
      // 保存状态快照
      this.stateSnapshots.push({
        actionType: action.type,
        timestamp: Date.now(),
        changes: stateChanges,
        state: this.sanitizeState(nextState)
      });
      
      // 保持快照在合理范围内
      if (this.stateSnapshots.length > 50) {
        this.stateSnapshots.shift();
      }
    }
  }
  
  // 比较状态差异
  diffState(prevState, nextState, path = '') {
    const changes = [];
    
    if (typeof prevState !== typeof nextState) {
      changes.push({
        path,
        type: 'type-change',
        prevValue: typeof prevState,
        nextValue: typeof nextState
      });
      return changes;
    }
    
    if (typeof prevState !== 'object' || prevState === null || nextState === null) {
      if (prevState !== nextState) {
        changes.push({
          path,
          type: 'value-change',
          prevValue: this.serializeValue(prevState),
          nextValue: this.serializeValue(nextState)
        });
      }
      return changes;
    }
    
    const allKeys = new Set([
      ...Object.keys(prevState || {}),
      ...Object.keys(nextState || {})
    ]);
    
    for (const key of allKeys) {
      const newPath = path ? `${path}.${key}` : key;
      
      if (!(key in prevState)) {
        changes.push({
          path: newPath,
          type: 'added',
          nextValue: this.serializeValue(nextState[key])
        });
      } else if (!(key in nextState)) {
        changes.push({
          path: newPath,
          type: 'removed',
          prevValue: this.serializeValue(prevState[key])
        });
      } else if (prevState[key] !== nextState[key]) {
        // 递归比较对象
        if (typeof prevState[key] === 'object' && typeof nextState[key] === 'object') {
          changes.push(...this.diffState(prevState[key], nextState[key], newPath));
        } else {
          changes.push({
            path: newPath,
            type: 'value-change',
            prevValue: this.serializeValue(prevState[key]),
            nextValue: this.serializeValue(nextState[key])
          });
        }
      }
    }
    
    return changes;
  }
  
  // 检查Action是否在黑名单中
  isActionBlacklisted(actionType) {
    return this.options.actionBlacklist.includes(actionType);
  }
  
  // 清理Payload
  sanitizePayload(payload) {
    if (!payload) return payload;
    
    try {
      const serialized = JSON.stringify(payload);
      if (serialized.length > this.options.maxStateSize) {
        return '[Payload too large]';
      }
      return JSON.parse(serialized);
    } catch {
      return '[Unserializable payload]';
    }
  }
  
  // 清理状态
  sanitizeState(state) {
    if (!state) return state;
    
    try {
      const sanitized = {};
      
      for (const [key, value] of Object.entries(state)) {
        if (this.options.stateBlacklist.includes(key)) {
          sanitized[key] = '[Filtered]';
        } else {
          const serialized = JSON.stringify(value);
          if (serialized.length > this.options.maxStateSize) {
            sanitized[key] = '[Value too large]';
          } else {
            sanitized[key] = value;
          }
        }
      }
      
      return sanitized;
    } catch {
      return '[Unserializable state]';
    }
  }
  
  // 序列化值
  serializeValue(value) {
    if (typeof value === 'function') {
      return '[Function]';
    }
    if (typeof value === 'object' && value !== null) {
      try {
        const serialized = JSON.stringify(value);
        return serialized.length > 100 ? '[Object]' : serialized;
      } catch {
        return '[Object]';
      }
    }
    return String(value).slice(0, 100);
  }
  
  // 获取中间件
  getMiddleware() {
    return this.middleware;
  }
  
  // 获取Action历史
  getActionHistory() {
    return this.actionHistory;
  }
  
  // 获取状态快照
  getStateSnapshots() {
    return this.stateSnapshots;
  }
  
  // 获取性能指标
  getPerformanceMetrics() {
    return Object.fromEntries(this.performanceMetrics);
  }
  
  // 生成Redux监控报告
  generateReduxReport() {
    const topActions = Array.from(this.performanceMetrics.entries())
      .sort(([,a], [,b]) => b.count - a.count)
      .slice(0, 10);
    
    const slowActions = Array.from(this.performanceMetrics.entries())
      .filter(([,metrics]) => metrics.avgDuration > 50)
      .sort(([,a], [,b]) => b.avgDuration - a.avgDuration);
    
    return {
      timestamp: new Date().toISOString(),
      actionHistory: this.actionHistory.slice(-20),
      stateSnapshots: this.stateSnapshots.slice(-10),
      performanceMetrics: this.getPerformanceMetrics(),
      summary: {
        totalActions: this.actionHistory.length,
        uniqueActionTypes: this.performanceMetrics.size,
        avgActionDuration: this.calculateAvgActionDuration(),
        slowActionsCount: slowActions.length
      },
      topActions: topActions.map(([type, metrics]) => ({ type, ...metrics })),
      slowActions: slowActions.map(([type, metrics]) => ({ type, ...metrics }))
    };
  }
  
  // 计算平均Action持续时间
  calculateAvgActionDuration() {
    let totalDuration = 0;
    let totalCount = 0;
    
    for (const metrics of this.performanceMetrics.values()) {
      totalDuration += metrics.totalDuration;
      totalCount += metrics.count;
    }
    
    return totalCount > 0 ? Math.round((totalDuration / totalCount) * 100) / 100 : 0;
  }
}

// 使用示例
const reduxSentryMiddleware = new ReduxSentryMiddleware({
  enableActionTracking: true,
  enableStateTracking: true,
  enablePerformanceTracking: true,
  actionBlacklist: ['@@redux/INIT', '@@redux/PROBE_UNKNOWN_ACTION'],
  stateBlacklist: ['password', 'token', 'secret']
});

export { ReduxSentryMiddleware };
```

### 2.2 Zustand状态监控

```javascript
// Zustand状态监控
import * as Sentry from '@sentry/react';
import { subscribeWithSelector } from 'zustand/middleware';

class ZustandSentryMonitor {
  constructor(options = {}) {
    this.options = {
      enableStateTracking: options.enableStateTracking !== false,
      enableActionTracking: options.enableActionTracking !== false,
      enablePerformanceTracking: options.enablePerformanceTracking !== false,
      stateBlacklist: options.stateBlacklist || [],
      maxStateSize: options.maxStateSize || 1000,
      ...options
    };
    
    this.storeRegistry = new Map();
    this.stateHistory = new Map();
    this.actionMetrics = new Map();
  }
  
  // 创建监控中间件
  createMonitoringMiddleware() {
    return (config) => (set, get, api) => {
      const storeName = config.name || 'unnamed-store';
      
      // 注册store
      this.registerStore(storeName, api);
      
      // 包装set函数
      const monitoredSet = (partial, replace) => {
        const prevState = get();
        const startTime = performance.now();
        
        // 执行状态更新
        const result = set(partial, replace);
        
        const endTime = performance.now();
        const nextState = get();
        
        // 记录状态变化
        this.recordStateChange(storeName, prevState, nextState, {
          partial,
          replace,
          duration: endTime - startTime
        });
        
        return result;
      };
      
      return config(monitoredSet, get, api);
    };
  }
  
  // 注册store
  registerStore(storeName, api) {
    this.storeRegistry.set(storeName, {
      api,
      subscribers: new Set(),
      stateChanges: 0,
      lastState: null,
      createdAt: Date.now()
    });
    
    // 设置状态订阅
    if (this.options.enableStateTracking) {
      this.setupStateSubscription(storeName, api);
    }
    
    Sentry.addBreadcrumb({
      category: 'zustand-store',
      message: `Store registered: ${storeName}`,
      level: 'info',
      data: {
        storeName,
        timestamp: Date.now()
      }
    });
  }
  
  // 设置状态订阅
  setupStateSubscription(storeName, api) {
    const unsubscribe = api.subscribe(
      (state) => state,
      (state, prevState) => {
        this.recordStateSubscription(storeName, prevState, state);
      },
      {
        equalityFn: (a, b) => a !== b,
        fireImmediately: false
      }
    );
    
    const storeData = this.storeRegistry.get(storeName);
    if (storeData) {
      storeData.unsubscribe = unsubscribe;
    }
  }
  
  // 记录状态变化
  recordStateChange(storeName, prevState, nextState, metadata) {
    const storeData = this.storeRegistry.get(storeName);
    if (storeData) {
      storeData.stateChanges++;
      storeData.lastState = nextState;
    }
    
    if (prevState === nextState) {
      return;
    }
    
    const changes = this.diffState(prevState, nextState);
    
    if (changes.length > 0) {
      // 记录到历史
      if (!this.stateHistory.has(storeName)) {
        this.stateHistory.set(storeName, []);
      }
      
      const history = this.stateHistory.get(storeName);
      history.push({
        timestamp: Date.now(),
        changes,
        metadata,
        prevState: this.sanitizeState(prevState),
        nextState: this.sanitizeState(nextState)
      });
      
      // 保持历史记录在合理范围内
      if (history.length > 50) {
        history.shift();
      }
      
      // 记录到Sentry
      Sentry.addBreadcrumb({
        category: 'zustand-state-change',
        message: `State changed: ${storeName}`,
        level: 'info',
        data: {
          storeName,
          changesCount: changes.length,
          duration: metadata.duration,
          hasReplace: metadata.replace
        }
      });
      
      // 检查慢状态更新
      if (metadata.duration > 50) {
        Sentry.addBreadcrumb({
          category: 'zustand-slow-update',
          message: `Slow state update: ${storeName}`,
          level: 'warning',
          data: {
            storeName,
            duration: metadata.duration,
            changesCount: changes.length
          }
        });
      }
    }
  }
  
  // 记录状态订阅
  recordStateSubscription(storeName, prevState, nextState) {
    const changes = this.diffState(prevState, nextState);
    
    if (changes.length > 0) {
      Sentry.addBreadcrumb({
        category: 'zustand-subscription',
        message: `Subscription triggered: ${storeName}`,
        level: 'info',
        data: {
          storeName,
          changesCount: changes.length
        }
      });
    }
  }
  
  // 比较状态差异
  diffState(prevState, nextState, path = '') {
    const changes = [];
    
    if (typeof prevState !== typeof nextState) {
      changes.push({
        path,
        type: 'type-change',
        prevValue: typeof prevState,
        nextValue: typeof nextState
      });
      return changes;
    }
    
    if (typeof prevState !== 'object' || prevState === null || nextState === null) {
      if (prevState !== nextState) {
        changes.push({
          path,
          type: 'value-change',
          prevValue: this.serializeValue(prevState),
          nextValue: this.serializeValue(nextState)
        });
      }
      return changes;
    }
    
    const allKeys = new Set([
      ...Object.keys(prevState || {}),
      ...Object.keys(nextState || {})
    ]);
    
    for (const key of allKeys) {
      if (this.options.stateBlacklist.includes(key)) {
        continue;
      }
      
      const newPath = path ? `${path}.${key}` : key;
      
      if (!(key in prevState)) {
        changes.push({
          path: newPath,
          type: 'added',
          nextValue: this.serializeValue(nextState[key])
        });
      } else if (!(key in nextState)) {
        changes.push({
          path: newPath,
          type: 'removed',
          prevValue: this.serializeValue(prevState[key])
        });
      } else if (prevState[key] !== nextState[key]) {
        if (typeof prevState[key] === 'object' && typeof nextState[key] === 'object') {
          changes.push(...this.diffState(prevState[key], nextState[key], newPath));
        } else {
          changes.push({
            path: newPath,
            type: 'value-change',
            prevValue: this.serializeValue(prevState[key]),
            nextValue: this.serializeValue(nextState[key])
          });
        }
      }
    }
    
    return changes;
  }
  
  // 创建监控的action
  createMonitoredAction(storeName, actionName, actionFn) {
    return (...args) => {
      const startTime = performance.now();
      
      try {
        const result = actionFn(...args);
        const endTime = performance.now();
        
        this.recordActionExecution(storeName, actionName, {
          duration: endTime - startTime,
          args: args.length,
          success: true
        });
        
        return result;
      } catch (error) {
        const endTime = performance.now();
        
        this.recordActionError(storeName, actionName, error, {
          duration: endTime - startTime,
          args: args.length
        });
        
        throw error;
      }
    };
  }
  
  // 记录action执行
  recordActionExecution(storeName, actionName, metrics) {
    const actionKey = `${storeName}.${actionName}`;
    
    if (!this.actionMetrics.has(actionKey)) {
      this.actionMetrics.set(actionKey, {
        executions: 0,
        totalDuration: 0,
        avgDuration: 0,
        maxDuration: 0,
        minDuration: Infinity,
        errors: 0
      });
    }
    
    const actionData = this.actionMetrics.get(actionKey);
    actionData.executions++;
    actionData.totalDuration += metrics.duration;
    actionData.avgDuration = actionData.totalDuration / actionData.executions;
    actionData.maxDuration = Math.max(actionData.maxDuration, metrics.duration);
    actionData.minDuration = Math.min(actionData.minDuration, metrics.duration);
    
    if (this.options.enableActionTracking) {
      Sentry.addBreadcrumb({
        category: 'zustand-action',
        message: `Action executed: ${actionKey}`,
        level: 'info',
        data: {
          storeName,
          actionName,
          duration: metrics.duration,
          argsCount: metrics.args
        }
      });
    }
    
    // 检查慢action
    if (metrics.duration > 100) {
      Sentry.addBreadcrumb({
        category: 'zustand-slow-action',
        message: `Slow action: ${actionKey}`,
        level: 'warning',
        data: {
          storeName,
          actionName,
          duration: metrics.duration,
          avgDuration: actionData.avgDuration
        }
      });
    }
  }
  
  // 记录action错误
  recordActionError(storeName, actionName, error, metrics) {
    const actionKey = `${storeName}.${actionName}`;
    
    const actionData = this.actionMetrics.get(actionKey);
    if (actionData) {
      actionData.errors++;
    }
    
    Sentry.withScope(scope => {
      scope.setTag('zustand-store', storeName);
      scope.setTag('zustand-action', actionName);
      scope.setContext('actionMetrics', {
        duration: metrics.duration,
        argsCount: metrics.args,
        totalExecutions: actionData?.executions || 0
      });
      
      Sentry.captureException(error);
    });
  }
  
  // 清理状态
  sanitizeState(state) {
    if (!state || typeof state !== 'object') {
      return state;
    }
    
    try {
      const sanitized = {};
      
      for (const [key, value] of Object.entries(state)) {
        if (this.options.stateBlacklist.includes(key)) {
          sanitized[key] = '[Filtered]';
        } else {
          const serialized = JSON.stringify(value);
          if (serialized.length > this.options.maxStateSize) {
            sanitized[key] = '[Value too large]';
          } else {
            sanitized[key] = value;
          }
        }
      }
      
      return sanitized;
    } catch {
      return '[Unserializable state]';
    }
  }
  
  // 序列化值
  serializeValue(value) {
    if (typeof value === 'function') {
      return '[Function]';
    }
    if (typeof value === 'object' && value !== null) {
      try {
        const serialized = JSON.stringify(value);
        return serialized.length > 100 ? '[Object]' : serialized;
      } catch {
        return '[Object]';
      }
    }
    return String(value).slice(0, 100);
  }
  
  // 获取store统计
  getStoreStats(storeName) {
    const storeData = this.storeRegistry.get(storeName);
    if (!storeData) {
      return null;
    }
    
    return {
      name: storeName,
      stateChanges: storeData.stateChanges,
      createdAt: storeData.createdAt,
      uptime: Date.now() - storeData.createdAt,
      lastState: storeData.lastState
    };
  }
  
  // 获取所有store统计
  getAllStoreStats() {
    const stats = {};
    
    for (const storeName of this.storeRegistry.keys()) {
      stats[storeName] = this.getStoreStats(storeName);
    }
    
    return stats;
  }
  
  // 获取action指标
  getActionMetrics() {
    return Object.fromEntries(this.actionMetrics);
  }
  
  // 获取状态历史
  getStateHistory(storeName) {
    return this.stateHistory.get(storeName) || [];
  }
  
  // 生成Zustand监控报告
  generateZustandReport() {
    const topActions = Array.from(this.actionMetrics.entries())
      .sort(([,a], [,b]) => b.executions - a.executions)
      .slice(0, 10);
    
    const slowActions = Array.from(this.actionMetrics.entries())
      .filter(([,metrics]) => metrics.avgDuration > 50)
      .sort(([,a], [,b]) => b.avgDuration - a.avgDuration);
    
    return {
      timestamp: new Date().toISOString(),
      stores: this.getAllStoreStats(),
      actionMetrics: this.getActionMetrics(),
      stateHistory: Object.fromEntries(this.stateHistory),
      summary: {
        totalStores: this.storeRegistry.size,
        totalActions: this.actionMetrics.size,
        totalStateChanges: Array.from(this.storeRegistry.values())
          .reduce((sum, store) => sum + store.stateChanges, 0),
        slowActionsCount: slowActions.length
      },
      topActions: topActions.map(([key, metrics]) => ({ action: key, ...metrics })),
      slowActions: slowActions.map(([key, metrics]) => ({ action: key, ...metrics }))
    };
  }
  
  // 清理资源
  cleanup() {
    for (const storeData of this.storeRegistry.values()) {
      if (storeData.unsubscribe) {
        storeData.unsubscribe();
      }
    }
    
    this.storeRegistry.clear();
    this.stateHistory.clear();
    this.actionMetrics.clear();
  }
}

// 使用示例
const zustandMonitor = new ZustandSentryMonitor({
  enableStateTracking: true,
  enableActionTracking: true,
  enablePerformanceTracking: true,
  stateBlacklist: ['password', 'token', 'secret']
});

export { ZustandSentryMonitor };
```

## 3. React Router监控

### 3.1 路由监控器

```javascript
// React Router监控器
import React, { useEffect, useRef } from 'react';
import { useLocation, useNavigationType } from 'react-router-dom';
import * as Sentry from '@sentry/react';

class ReactRouterMonitor {
  constructor(options = {}) {
    this.options = {
      enableRouteTracking: options.enableRouteTracking !== false,
      enableNavigationTracking: options.enableNavigationTracking !== false,
      enablePerformanceTracking: options.enablePerformanceTracking !== false,
      enableErrorBoundary: options.enableErrorBoundary !== false,
      routeNameMap: options.routeNameMap || {},
      ...options
    };
    
    this.navigationHistory = [];
    this.routeMetrics = new Map();
    this.currentRoute = null;
    this.routeStartTime = null;
    
    this.setupRouterIntegration();
  }
  
  // 设置路由集成
  setupRouterIntegration() {
    // 设置Sentry路由集成
    if (this.options.enableRouteTracking) {
      this.setupSentryRouting();
    }
  }
  
  // 设置Sentry路由
  setupSentryRouting() {
    // 这个方法在Sentry初始化时调用
    // 参见ReactMonitoringIntegrator中的routingInstrumentation配置
  }
  
  // 创建路由监控Hook
  createRouteMonitoringHook() {
    return () => {
      const location = useLocation();
      const navigationType = useNavigationType();
      const prevLocationRef = useRef(location);
      const routeStartTimeRef = useRef(Date.now());
      
      useEffect(() => {
        const prevLocation = prevLocationRef.current;
        const currentTime = Date.now();
        
        // 记录路由变化
        if (prevLocation.pathname !== location.pathname) {
          this.recordRouteChange(prevLocation, location, navigationType, currentTime);
          routeStartTimeRef.current = currentTime;
        }
        
        // 记录页面停留时间
        if (prevLocation.pathname !== location.pathname && this.currentRoute) {
          const stayDuration = currentTime - (this.routeStartTime || currentTime);
          this.recordRouteStayDuration(this.currentRoute, stayDuration);
        }
        
        this.currentRoute = location.pathname;
        this.routeStartTime = routeStartTimeRef.current;
        prevLocationRef.current = location;
        
        // 设置Sentry标签
        Sentry.setTag('route', this.getRouteName(location.pathname));
        Sentry.setContext('navigation', {
          pathname: location.pathname,
          search: location.search,
          hash: location.hash,
          state: location.state,
          navigationType
        });
        
      }, [location, navigationType]);
      
      // 页面卸载时记录停留时间
      useEffect(() => {
        return () => {
          if (this.currentRoute && this.routeStartTime) {
            const stayDuration = Date.now() - this.routeStartTime;
            this.recordRouteStayDuration(this.currentRoute, stayDuration);
          }
        };
      }, []);
    };
  }
  
  // 记录路由变化
  recordRouteChange(prevLocation, currentLocation, navigationType, timestamp) {
    const routeChange = {
      timestamp,
      from: {
        pathname: prevLocation.pathname,
        search: prevLocation.search,
        hash: prevLocation.hash
      },
      to: {
        pathname: currentLocation.pathname,
        search: currentLocation.search,
        hash: currentLocation.hash
      },
      navigationType,
      routeName: this.getRouteName(currentLocation.pathname)
    };
    
    // 添加到导航历史
    this.navigationHistory.push(routeChange);
    
    // 保持历史记录在合理范围内
    if (this.navigationHistory.length > 100) {
      this.navigationHistory.shift();
    }
    
    // 记录到Sentry
    if (this.options.enableNavigationTracking) {
      Sentry.addBreadcrumb({
        category: 'navigation',
        message: `Navigated to ${currentLocation.pathname}`,
        level: 'info',
        data: {
          from: prevLocation.pathname,
          to: currentLocation.pathname,
          navigationType,
          routeName: routeChange.routeName
        }
      });
    }
    
    // 开始新的路由事务
    if (this.options.enablePerformanceTracking) {
      this.startRouteTransaction(currentLocation.pathname, routeChange.routeName);
    }
  }
  
  // 记录路由停留时间
  recordRouteStayDuration(pathname, duration) {
    if (!this.routeMetrics.has(pathname)) {
      this.routeMetrics.set(pathname, {
        visits: 0,
        totalStayTime: 0,
        avgStayTime: 0,
        maxStayTime: 0,
        minStayTime: Infinity,
        bounces: 0 // 停留时间少于3秒的访问
      });
    }
    
    const metrics = this.routeMetrics.get(pathname);
    metrics.visits++;
    metrics.totalStayTime += duration;
    metrics.avgStayTime = metrics.totalStayTime / metrics.visits;
    metrics.maxStayTime = Math.max(metrics.maxStayTime, duration);
    metrics.minStayTime = Math.min(metrics.minStayTime, duration);
    
    if (duration < 3000) { // 少于3秒认为是跳出
      metrics.bounces++;
    }
    
    // 记录到Sentry
    Sentry.addBreadcrumb({
      category: 'route-stay',
      message: `Route stay duration: ${pathname}`,
      level: 'info',
      data: {
        pathname,
        duration,
        routeName: this.getRouteName(pathname),
        avgStayTime: metrics.avgStayTime
      }
    });
    
    // 检查异常短的停留时间
    if (duration < 1000) {
      Sentry.addBreadcrumb({
        category: 'route-bounce',
        message: `Quick bounce: ${pathname}`,
        level: 'warning',
        data: {
          pathname,
          duration,
          routeName: this.getRouteName(pathname)
        }
      });
    }
  }
  
  // 开始路由事务
  startRouteTransaction(pathname, routeName) {
    const transaction = Sentry.startTransaction({
      name: routeName || pathname,
      op: 'navigation',
      tags: {
        route: pathname,
        routeName: routeName || 'unknown'
      }
    });
    
    // 设置当前事务
    Sentry.getCurrentHub().configureScope(scope => {
      scope.setSpan(transaction);
    });
    
    // 监听页面加载完成
    if (typeof window !== 'undefined') {
      const handleLoad = () => {
        transaction.setStatus('ok');
        transaction.finish();
        window.removeEventListener('load', handleLoad);
      };
      
      if (document.readyState === 'complete') {
        transaction.setStatus('ok');
        transaction.finish();
      } else {
        window.addEventListener('load', handleLoad);
      }
    }
  }
  
  // 获取路由名称
  getRouteName(pathname) {
    // 首先检查自定义路由名称映射
    if (this.options.routeNameMap[pathname]) {
      return this.options.routeNameMap[pathname];
    }
    
    // 尝试从路径中提取有意义的名称
    const segments = pathname.split('/').filter(Boolean);
    
    if (segments.length === 0) {
      return 'Home';
    }
    
    // 处理动态路由参数
    const cleanSegments = segments.map(segment => {
      // 如果是数字，可能是ID参数
      if (/^\d+$/.test(segment)) {
        return ':id';
      }
      // 如果是UUID格式
      if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(segment)) {
        return ':uuid';
      }
      return segment;
    });
    
    return cleanSegments.map(s => 
      s.charAt(0).toUpperCase() + s.slice(1)
    ).join(' ');
  }
  
  // 创建路由错误边界
  createRouteErrorBoundary() {
    return class RouteErrorBoundary extends React.Component {
      constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
      }
      
      static getDerivedStateFromError(error) {
        return { hasError: true, error };
      }
      
      componentDidCatch(error, errorInfo) {
        const location = window.location;
        
        Sentry.withScope(scope => {
          scope.setTag('error-boundary', 'route');
          scope.setContext('route', {
            pathname: location.pathname,
            search: location.search,
            hash: location.hash,
            routeName: this.getRouteName(location.pathname)
          });
          scope.setContext('errorInfo', errorInfo);
          
          Sentry.captureException(error);
        });
      }
      
      render() {
        if (this.state.hasError) {
          return this.props.fallback || (
            <div className="route-error">
              <h2>页面加载出错</h2>
              <p>抱歉，页面加载时发生了错误。</p>
              <button onClick={() => window.location.reload()}>
                刷新页面
              </button>
            </div>
          );
        }
        
        return this.props.children;
      }
    };
  }
  
  // 获取导航历史
  getNavigationHistory() {
    return this.navigationHistory;
  }
  
  // 获取路由指标
  getRouteMetrics() {
    return Object.fromEntries(this.routeMetrics);
  }
  
  // 获取当前路由信息
  getCurrentRouteInfo() {
    return {
      pathname: this.currentRoute,
      routeName: this.getRouteName(this.currentRoute || ''),
      startTime: this.routeStartTime,
      duration: this.routeStartTime ? Date.now() - this.routeStartTime : 0
    };
  }
  
  // 生成路由监控报告
  generateRouterReport() {
    const topRoutes = Array.from(this.routeMetrics.entries())
      .sort(([,a], [,b]) => b.visits - a.visits)
      .slice(0, 10);
    
    const bounceRoutes = Array.from(this.routeMetrics.entries())
      .filter(([,metrics]) => metrics.bounces / metrics.visits > 0.5)
      .sort(([,a], [,b]) => (b.bounces / b.visits) - (a.bounces / a.visits));
    
    return {
      timestamp: new Date().toISOString(),
      currentRoute: this.getCurrentRouteInfo(),
      navigationHistory: this.navigationHistory.slice(-20),
      routeMetrics: this.getRouteMetrics(),
      summary: {
        totalRoutes: this.routeMetrics.size,
        totalNavigations: this.navigationHistory.length,
        avgStayTime: this.calculateAvgStayTime(),
        bounceRate: this.calculateBounceRate()
      },
      topRoutes: topRoutes.map(([pathname, metrics]) => ({
        pathname,
        routeName: this.getRouteName(pathname),
        ...metrics
      })),
      bounceRoutes: bounceRoutes.map(([pathname, metrics]) => ({
        pathname,
        routeName: this.getRouteName(pathname),
        bounceRate: metrics.bounces / metrics.visits,
        ...metrics
      }))
    };
  }
  
  // 计算平均停留时间
  calculateAvgStayTime() {
    let totalTime = 0;
    let totalVisits = 0;
    
    for (const metrics of this.routeMetrics.values()) {
      totalTime += metrics.totalStayTime;
      totalVisits += metrics.visits;
    }
    
    return totalVisits > 0 ? Math.round(totalTime / totalVisits) : 0;
  }
  
  // 计算跳出率
  calculateBounceRate() {
    let totalBounces = 0;
    let totalVisits = 0;
    
    for (const metrics of this.routeMetrics.values()) {
      totalBounces += metrics.bounces;
      totalVisits += metrics.visits;
    }
    
    return totalVisits > 0 ? Math.round((totalBounces / totalVisits) * 100) / 100 : 0;
  }
}

// 使用示例
const routerMonitor = new ReactRouterMonitor({
  enableRouteTracking: true,
  enableNavigationTracking: true,
  enablePerformanceTracking: true,
  routeNameMap: {
    '/': 'Home',
    '/dashboard': 'Dashboard',
    '/profile': 'User Profile',
    '/settings': 'Settings'
  }
});

export { ReactRouterMonitor };
```

## 4. 组件性能分析器

### 4.1 React组件性能监控

```javascript
// React组件性能分析器
import React, { useEffect, useRef, useState, useCallback } from 'react';
import * as Sentry from '@sentry/react';

class ReactComponentPerformanceAnalyzer {
  constructor(options = {}) {
    this.options = {
      enableRenderTracking: options.enableRenderTracking !== false,
      enableLifecycleTracking: options.enableLifecycleTracking !== false,
      enablePropsTracking: options.enablePropsTracking !== false,
      enableMemoryTracking: options.enableMemoryTracking !== false,
      slowRenderThreshold: options.slowRenderThreshold || 16, // 16ms
      memoryLeakThreshold: options.memoryLeakThreshold || 50, // 50MB
      componentBlacklist: options.componentBlacklist || [],
      ...options
    };
    
    this.componentMetrics = new Map();
    this.renderHistory = new Map();
    this.memorySnapshots = [];
    this.performanceObserver = null;
    
    this.setupPerformanceObserver();
  }
  
  // 设置性能观察器
  setupPerformanceObserver() {
    if (typeof window !== 'undefined' && 'PerformanceObserver' in window) {
      this.performanceObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.entryType === 'measure' && entry.name.startsWith('react-component-')) {
            this.recordComponentPerformance(entry);
          }
        }
      });
      
      try {
        this.performanceObserver.observe({ entryTypes: ['measure'] });
      } catch (error) {
        console.warn('Performance Observer not supported:', error);
      }
    }
  }
  
  // 创建组件性能监控HOC
  createPerformanceMonitoringHOC(componentName) {
    return (WrappedComponent) => {
      const MonitoredComponent = React.forwardRef((props, ref) => {
        const renderCountRef = useRef(0);
        const mountTimeRef = useRef(Date.now());
        const lastPropsRef = useRef(props);
        const [renderTimes, setRenderTimes] = useState([]);
        
        // 记录渲染开始
        const renderStartTime = useRef(performance.now());
        
        useEffect(() => {
          renderCountRef.current++;
          const renderEndTime = performance.now();
          const renderDuration = renderEndTime - renderStartTime.current;
          
          // 记录渲染性能
          this.recordComponentRender(componentName, {
            renderCount: renderCountRef.current,
            renderDuration,
            props,
            timestamp: Date.now()
          });
          
          setRenderTimes(prev => [...prev.slice(-9), renderDuration]);
          
          // 检查慢渲染
          if (renderDuration > this.options.slowRenderThreshold) {
            this.recordSlowRender(componentName, {
              renderDuration,
              renderCount: renderCountRef.current,
              props
            });
          }
        });
        
        // 监控Props变化
        useEffect(() => {
          if (this.options.enablePropsTracking) {
            const propsChanges = this.diffProps(lastPropsRef.current, props);
            if (propsChanges.length > 0) {
              this.recordPropsChange(componentName, {
                changes: propsChanges,
                renderCount: renderCountRef.current,
                timestamp: Date.now()
              });
            }
            lastPropsRef.current = props;
          }
        });
        
        // 组件挂载和卸载监控
        useEffect(() => {
          const mountTime = Date.now();
          mountTimeRef.current = mountTime;
          
          this.recordComponentMount(componentName, {
            mountTime,
            props
          });
          
          return () => {
            const unmountTime = Date.now();
            const lifespan = unmountTime - mountTime;
            
            this.recordComponentUnmount(componentName, {
              unmountTime,
              lifespan,
              renderCount: renderCountRef.current,
              avgRenderTime: renderTimes.length > 0 
                ? renderTimes.reduce((a, b) => a + b, 0) / renderTimes.length 
                : 0
            });
          };
        }, []);
        
        // 内存监控
        useEffect(() => {
          if (this.options.enableMemoryTracking) {
            const checkMemory = () => {
              if (performance.memory) {
                this.recordMemoryUsage(componentName, {
                  usedJSHeapSize: performance.memory.usedJSHeapSize,
                  totalJSHeapSize: performance.memory.totalJSHeapSize,
                  jsHeapSizeLimit: performance.memory.jsHeapSizeLimit,
                  renderCount: renderCountRef.current
                });
              }
            };
            
            const memoryInterval = setInterval(checkMemory, 5000);
            return () => clearInterval(memoryInterval);
          }
        }, []);
        
        // 更新渲染开始时间
        renderStartTime.current = performance.now();
        
        return <WrappedComponent {...props} ref={ref} />;
      });
      
      MonitoredComponent.displayName = `PerformanceMonitor(${componentName})`;
      return MonitoredComponent;
    };
  }
  
  // 创建性能监控Hook
  createPerformanceMonitoringHook(componentName) {
    return (dependencies = []) => {
      const renderCountRef = useRef(0);
      const renderStartTimeRef = useRef(performance.now());
      const lastDependenciesRef = useRef(dependencies);
      
      useEffect(() => {
        renderCountRef.current++;
        const renderEndTime = performance.now();
        const renderDuration = renderEndTime - renderStartTimeRef.current;
        
        // 记录渲染性能
        this.recordHookRender(componentName, {
          renderCount: renderCountRef.current,
          renderDuration,
          dependencies,
          timestamp: Date.now()
        });
        
        // 检查依赖变化
        const dependencyChanges = this.diffDependencies(
          lastDependenciesRef.current, 
          dependencies
        );
        
        if (dependencyChanges.length > 0) {
          this.recordDependencyChange(componentName, {
            changes: dependencyChanges,
            renderCount: renderCountRef.current,
            renderDuration
          });
        }
        
        lastDependenciesRef.current = dependencies;
        renderStartTimeRef.current = performance.now();
      });
      
      return {
        renderCount: renderCountRef.current,
        componentName
      };
    };
  }
  
  // 记录组件渲染
  recordComponentRender(componentName, metrics) {
    if (this.options.componentBlacklist.includes(componentName)) {
      return;
    }
    
    if (!this.componentMetrics.has(componentName)) {
      this.componentMetrics.set(componentName, {
        totalRenders: 0,
        totalRenderTime: 0,
        avgRenderTime: 0,
        maxRenderTime: 0,
        minRenderTime: Infinity,
        slowRenders: 0,
        mounts: 0,
        unmounts: 0,
        propsChanges: 0
      });
    }
    
    const componentData = this.componentMetrics.get(componentName);
    componentData.totalRenders++;
    componentData.totalRenderTime += metrics.renderDuration;
    componentData.avgRenderTime = componentData.totalRenderTime / componentData.totalRenders;
    componentData.maxRenderTime = Math.max(componentData.maxRenderTime, metrics.renderDuration);
    componentData.minRenderTime = Math.min(componentData.minRenderTime, metrics.renderDuration);
    
    // 记录渲染历史
    if (!this.renderHistory.has(componentName)) {
      this.renderHistory.set(componentName, []);
    }
    
    const history = this.renderHistory.get(componentName);
    history.push({
      timestamp: metrics.timestamp,
      renderDuration: metrics.renderDuration,
      renderCount: metrics.renderCount
    });
    
    // 保持历史记录在合理范围内
    if (history.length > 100) {
      history.shift();
    }
    
    // 记录到Sentry
    if (this.options.enableRenderTracking) {
      Sentry.addBreadcrumb({
        category: 'component-render',
        message: `Component rendered: ${componentName}`,
        level: 'info',
        data: {
          componentName,
          renderDuration: metrics.renderDuration,
          renderCount: metrics.renderCount,
          avgRenderTime: componentData.avgRenderTime
        }
      });
    }
  }
  
  // 记录慢渲染
  recordSlowRender(componentName, metrics) {
    const componentData = this.componentMetrics.get(componentName);
    if (componentData) {
      componentData.slowRenders++;
    }
    
    Sentry.addBreadcrumb({
      category: 'slow-render',
      message: `Slow render detected: ${componentName}`,
      level: 'warning',
      data: {
        componentName,
        renderDuration: metrics.renderDuration,
        renderCount: metrics.renderCount,
        threshold: this.options.slowRenderThreshold
      }
    });
    
    // 如果渲染时间超过阈值很多，记录为性能问题
    if (metrics.renderDuration > this.options.slowRenderThreshold * 3) {
      Sentry.withScope(scope => {
        scope.setTag('performance-issue', 'slow-render');
        scope.setTag('component', componentName);
        scope.setContext('renderMetrics', {
          renderDuration: metrics.renderDuration,
          renderCount: metrics.renderCount,
          threshold: this.options.slowRenderThreshold
        });
        
        Sentry.captureMessage(
          `Very slow render detected in ${componentName}`,
          'warning'
        );
      });
    }
  }
  
  // 记录Props变化
  recordPropsChange(componentName, data) {
    const componentData = this.componentMetrics.get(componentName);
    if (componentData) {
      componentData.propsChanges++;
    }
    
    Sentry.addBreadcrumb({
      category: 'props-change',
      message: `Props changed: ${componentName}`,
      level: 'info',
      data: {
        componentName,
        changesCount: data.changes.length,
        renderCount: data.renderCount,
        changes: data.changes.slice(0, 5) // 只记录前5个变化
      }
    });
  }
  
  // 记录组件挂载
  recordComponentMount(componentName, data) {
    const componentData = this.componentMetrics.get(componentName);
    if (componentData) {
      componentData.mounts++;
    }
    
    Sentry.addBreadcrumb({
      category: 'component-mount',
      message: `Component mounted: ${componentName}`,
      level: 'info',
      data: {
        componentName,
        mountTime: data.mountTime
      }
    });
  }
  
  // 记录组件卸载
  recordComponentUnmount(componentName, data) {
    const componentData = this.componentMetrics.get(componentName);
    if (componentData) {
      componentData.unmounts++;
    }
    
    Sentry.addBreadcrumb({
      category: 'component-unmount',
      message: `Component unmounted: ${componentName}`,
      level: 'info',
      data: {
        componentName,
        lifespan: data.lifespan,
        renderCount: data.renderCount,
        avgRenderTime: data.avgRenderTime
      }
    });
    
    // 检查组件是否存在内存泄漏风险
    if (data.lifespan < 1000 && data.renderCount > 10) {
      Sentry.addBreadcrumb({
        category: 'potential-memory-leak',
        message: `Potential memory leak: ${componentName}`,
        level: 'warning',
        data: {
          componentName,
          lifespan: data.lifespan,
          renderCount: data.renderCount,
          renderFrequency: data.renderCount / (data.lifespan / 1000)
        }
      });
    }
  }
  
  // 记录Hook渲染
  recordHookRender(componentName, metrics) {
    // 类似于recordComponentRender，但针对Hook
    this.recordComponentRender(componentName, metrics);
  }
  
  // 记录依赖变化
  recordDependencyChange(componentName, data) {
    Sentry.addBreadcrumb({
      category: 'hook-dependency-change',
      message: `Hook dependencies changed: ${componentName}`,
      level: 'info',
      data: {
        componentName,
        changesCount: data.changes.length,
        renderCount: data.renderCount,
        renderDuration: data.renderDuration
      }
    });
  }
  
  // 记录内存使用
  recordMemoryUsage(componentName, memoryInfo) {
    this.memorySnapshots.push({
      timestamp: Date.now(),
      componentName,
      ...memoryInfo
    });
    
    // 保持内存快照在合理范围内
    if (this.memorySnapshots.length > 1000) {
      this.memorySnapshots.shift();
    }
    
    // 检查内存使用是否过高
    const memoryUsageMB = memoryInfo.usedJSHeapSize / 1024 / 1024;
    if (memoryUsageMB > this.options.memoryLeakThreshold) {
      Sentry.addBreadcrumb({
        category: 'high-memory-usage',
        message: `High memory usage detected: ${componentName}`,
        level: 'warning',
        data: {
          componentName,
          memoryUsageMB: Math.round(memoryUsageMB),
          threshold: this.options.memoryLeakThreshold,
          renderCount: memoryInfo.renderCount
        }
      });
    }
  }
  
  // 比较Props差异
  diffProps(prevProps, nextProps) {
    const changes = [];
    const allKeys = new Set([
      ...Object.keys(prevProps || {}),
      ...Object.keys(nextProps || {})
    ]);
    
    for (const key of allKeys) {
      if (!(key in prevProps)) {
        changes.push({
          key,
          type: 'added',
          nextValue: this.serializeValue(nextProps[key])
        });
      } else if (!(key in nextProps)) {
        changes.push({
          key,
          type: 'removed',
          prevValue: this.serializeValue(prevProps[key])
        });
      } else if (prevProps[key] !== nextProps[key]) {
        changes.push({
          key,
          type: 'changed',
          prevValue: this.serializeValue(prevProps[key]),
          nextValue: this.serializeValue(nextProps[key])
        });
      }
    }
    
    return changes;
  }
  
  // 比较依赖差异
  diffDependencies(prevDeps, nextDeps) {
    const changes = [];
    
    if (prevDeps.length !== nextDeps.length) {
      changes.push({
        type: 'length-change',
        prevLength: prevDeps.length,
        nextLength: nextDeps.length
      });
    }
    
    const maxLength = Math.max(prevDeps.length, nextDeps.length);
    for (let i = 0; i < maxLength; i++) {
      if (prevDeps[i] !== nextDeps[i]) {
        changes.push({
          type: 'value-change',
          index: i,
          prevValue: this.serializeValue(prevDeps[i]),
          nextValue: this.serializeValue(nextDeps[i])
        });
      }
    }
    
    return changes;
  }
  
  // 序列化值
  serializeValue(value) {
    if (typeof value === 'function') {
      return '[Function]';
    }
    if (typeof value === 'object' && value !== null) {
      try {
        const serialized = JSON.stringify(value);
        return serialized.length > 100 ? '[Object]' : serialized;
      } catch {
        return '[Object]';
      }
    }
    return String(value).slice(0, 100);
  }
  
  // 获取组件性能统计
  getComponentStats(componentName) {
    return this.componentMetrics.get(componentName) || null;
  }
  
  // 获取所有组件统计
  getAllComponentStats() {
    return Object.fromEntries(this.componentMetrics);
  }
  
  // 获取渲染历史
  getRenderHistory(componentName) {
    return this.renderHistory.get(componentName) || [];
  }
  
  // 获取内存快照
  getMemorySnapshots() {
    return this.memorySnapshots;
  }
  
  // 生成性能报告
  generatePerformanceReport() {
    const slowComponents = Array.from(this.componentMetrics.entries())
      .filter(([, metrics]) => metrics.avgRenderTime > this.options.slowRenderThreshold)
      .sort(([, a], [, b]) => b.avgRenderTime - a.avgRenderTime);
    
    const frequentComponents = Array.from(this.componentMetrics.entries())
      .sort(([, a], [, b]) => b.totalRenders - a.totalRenders)
      .slice(0, 10);
    
    const memoryTrend = this.analyzeMemoryTrend();
    
    return {
      timestamp: new Date().toISOString(),
      componentMetrics: this.getAllComponentStats(),
      renderHistory: Object.fromEntries(this.renderHistory),
      memorySnapshots: this.memorySnapshots.slice(-50),
      summary: {
        totalComponents: this.componentMetrics.size,
        totalRenders: Array.from(this.componentMetrics.values())
          .reduce((sum, metrics) => sum + metrics.totalRenders, 0),
        avgRenderTime: this.calculateAvgRenderTime(),
        slowComponentsCount: slowComponents.length,
        memoryTrend: memoryTrend.trend
      },
      slowComponents: slowComponents.map(([name, metrics]) => ({ name, ...metrics })),
      frequentComponents: frequentComponents.map(([name, metrics]) => ({ name, ...metrics })),
      memoryAnalysis: memoryTrend
    };
  }
  
  // 计算平均渲染时间
  calculateAvgRenderTime() {
    let totalTime = 0;
    let totalRenders = 0;
    
    for (const metrics of this.componentMetrics.values()) {
      totalTime += metrics.totalRenderTime;
      totalRenders += metrics.totalRenders;
    }
    
    return totalRenders > 0 ? Math.round((totalTime / totalRenders) * 100) / 100 : 0;
  }
  
  // 分析内存趋势
  analyzeMemoryTrend() {
    if (this.memorySnapshots.length < 10) {
      return { trend: 'insufficient-data', analysis: 'Not enough data' };
    }
    
    const recent = this.memorySnapshots.slice(-10);
    const first = recent[0].usedJSHeapSize;
    const last = recent[recent.length - 1].usedJSHeapSize;
    const change = ((last - first) / first) * 100;
    
    let trend = 'stable';
    if (change > 10) {
      trend = 'increasing';
    } else if (change < -10) {
      trend = 'decreasing';
    }
    
    return {
      trend,
      changePercent: Math.round(change * 100) / 100,
      firstSnapshot: first,
      lastSnapshot: last,
      analysis: this.getMemoryAnalysis(trend, change)
    };
  }
  
  // 获取内存分析
  getMemoryAnalysis(trend, changePercent) {
    switch (trend) {
      case 'increasing':
        return `Memory usage increased by ${Math.abs(changePercent)}%. Monitor for potential memory leaks.`;
      case 'decreasing':
        return `Memory usage decreased by ${Math.abs(changePercent)}%. Good memory management.`;
      default:
        return 'Memory usage is stable.';
    }
  }
  
  // 清理资源
  cleanup() {
    if (this.performanceObserver) {
      this.performanceObserver.disconnect();
    }
    
    this.componentMetrics.clear();
    this.renderHistory.clear();
    this.memorySnapshots.length = 0;
  }
}

// 使用示例
const performanceAnalyzer = new ReactComponentPerformanceAnalyzer({
  enableRenderTracking: true,
  enableLifecycleTracking: true,
  enablePropsTracking: true,
  enableMemoryTracking: true,
  slowRenderThreshold: 16,
  memoryLeakThreshold: 50,
  componentBlacklist: ['DevTools', 'ErrorBoundary']
});

export { ReactComponentPerformanceAnalyzer };
```

## 5. 最佳实践与总结

### 5.1 实施建议

1. **渐进式集成**
   - 从核心组件开始集成监控
   - 逐步扩展到整个应用
   - 根据业务需求调整监控粒度

2. **性能优化**
   - 合理设置采样率
   - 避免过度监控影响性能
   - 使用异步处理减少阻塞

3. **数据管理**
   - 定期清理历史数据
   - 设置合理的数据保留策略
   - 压缩和优化传输数据

4. **告警配置**
   - 设置合理的阈值
   - 避免告警疲劳
   - 建立分级响应机制

### 5.2 核心价值

1. **全面监控**
   - 覆盖React生态的各个层面
   - 提供统一的监控接口
   - 支持自定义扩展

2. **深度集成**
   - 与Sentry深度集成
   - 利用React特性优化监控
   - 提供丰富的上下文信息

3. **性能洞察**
   - 识别性能瓶颈
   - 优化渲染性能
   - 监控内存使用

4. **开发效率**
   - 快速定位问题
   - 提供详细的调试信息
   - 支持团队协作

### 5.3 未来发展趋势

1. **智能化监控**
   - AI驱动的异常检测
   - 自动化性能优化建议
   - 预测性维护

2. **更深度的集成**
   - React 18并发特性支持
   - Server Components监控
   - Streaming SSR监控

3. **可视化增强**
   - 实时性能仪表板
   - 交互式错误分析
   - 3D性能可视化

4. **生态系统扩展**
   - 更多第三方库集成
   - 微前端监控支持
   - 跨平台监控统一

通过本文介绍的React生态监控体系，开发团队可以构建一个全面、高效的前端监控解决方案，不仅能够及时发现和解决问题，还能持续优化应用性能，提升用户体验。这套监控体系将成为现代React应用开发中不可或缺的重要工具。
```