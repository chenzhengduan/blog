# 前端Sentry与移动端集成：构建跨平台监控体系

## 1. 移动端监控架构设计

### 1.1 跨平台监控集成器

```typescript
// 跨平台监控集成器
import * as Sentry from '@sentry/react-native';
import { Platform, Dimensions, DeviceInfo } from 'react-native';
import NetInfo from '@react-native-async-storage/async-storage';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { performance } from 'perf_hooks';

interface MobileMonitoringConfig {
  sentryDsn: string;
  environment: string;
  enablePerformanceMonitoring: boolean;
  enableNetworkMonitoring: boolean;
  enableCrashReporting: boolean;
  enableUserTracking: boolean;
  enableOfflineSupport: boolean;
  enableNativeIntegration: boolean;
  performanceSampleRate: number;
  maxBreadcrumbs: number;
  maxCacheSize: number;
  uploadInterval: number;
}

interface DeviceInfo {
  platform: string;
  version: string;
  model: string;
  manufacturer: string;
  screenSize: { width: number; height: number };
  memory: number;
  storage: { total: number; free: number };
  battery: { level: number; charging: boolean };
  network: {
    type: string;
    isConnected: boolean;
    isInternetReachable: boolean;
  };
}

interface AppMetrics {
  launchTime: number;
  memoryUsage: number;
  cpuUsage: number;
  batteryUsage: number;
  networkUsage: { sent: number; received: number };
  crashCount: number;
  errorCount: number;
  sessionDuration: number;
}

class MobileMonitoringIntegrator {
  private config: MobileMonitoringConfig;
  private deviceInfo: DeviceInfo | null = null;
  private appMetrics: AppMetrics;
  private offlineQueue: any[] = [];
  private sessionStartTime: number;
  private networkMonitor?: any;
  
  constructor(config: MobileMonitoringConfig) {
    this.config = config;
    this.sessionStartTime = Date.now();
    this.appMetrics = this.initializeMetrics();
    this.initializeSentry();
    this.setupDeviceInfo();
    this.setupNetworkMonitoring();
    this.setupOfflineSupport();
  }
  
  // 初始化Sentry
  private initializeSentry(): void {
    Sentry.init({
      dsn: this.config.sentryDsn,
      environment: this.config.environment,
      enableAutoSessionTracking: true,
      enableOutOfMemoryTracking: true,
      enableNativeCrashHandling: this.config.enableCrashReporting,
      enableAutoPerformanceTracing: this.config.enablePerformanceMonitoring,
      tracesSampleRate: this.config.performanceSampleRate,
      maxBreadcrumbs: this.config.maxBreadcrumbs,
      beforeSend: (event) => this.beforeSendEvent(event),
      beforeBreadcrumb: (breadcrumb) => this.beforeBreadcrumb(breadcrumb),
      integrations: [
        new Sentry.ReactNativeTracing({
          enableNativeFramesTracking: true,
          enableStallTracking: true,
          enableAppStartTracking: true,
          enableUserInteractionTracing: true
        })
      ]
    });
  }
  
  // 设备信息收集
  private async setupDeviceInfo(): Promise<void> {
    try {
      const { width, height } = Dimensions.get('window');
      const netInfo = await NetInfo.fetch();
      
      this.deviceInfo = {
        platform: Platform.OS,
        version: Platform.Version.toString(),
        model: await this.getDeviceModel(),
        manufacturer: await this.getDeviceManufacturer(),
        screenSize: { width, height },
        memory: await this.getDeviceMemory(),
        storage: await this.getStorageInfo(),
        battery: await this.getBatteryInfo(),
        network: {
          type: netInfo.type || 'unknown',
          isConnected: netInfo.isConnected || false,
          isInternetReachable: netInfo.isInternetReachable || false
        }
      };
      
      // 设置Sentry上下文
      Sentry.setContext('device', this.deviceInfo);
      
      // 记录设备信息
      Sentry.addBreadcrumb({
        category: 'device',
        message: 'Device information collected',
        level: 'info',
        data: this.deviceInfo
      });
      
    } catch (error) {
      Sentry.captureException(error);
    }
 }
 ```

## 5. 使用示例

### 5.1 React Native完整集成示例

```typescript
// App.tsx - React Native主应用
import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { Provider } from 'react-redux';
import { store } from './store';
import { MobileMonitoringIntegrator } from './monitoring/MobileMonitoringIntegrator';
import { ReactNativeMonitor } from './monitoring/ReactNativeMonitor';
import HomeScreen from './screens/HomeScreen';
import ProfileScreen from './screens/ProfileScreen';

const Stack = createStackNavigator();

// 初始化监控
const mobileMonitor = new MobileMonitoringIntegrator({
  sentryDsn: 'YOUR_SENTRY_DSN',
  environment: __DEV__ ? 'development' : 'production',
  enablePerformanceMonitoring: true,
  enableNetworkMonitoring: true,
  enableUserInteractionTracking: true,
  performanceSampleRate: 0.1,
  maxBreadcrumbs: 100,
  uploadInterval: 300000, // 5分钟
});

const reactNativeMonitor = new ReactNativeMonitor({
  enableComponentTracking: true,
  enableNavigationTracking: true,
  enableGestureTracking: true,
  enableStateTracking: true,
  performanceThreshold: 16, // 16ms
  maxComponentDepth: 10,
});

const App: React.FC = () => {
  useEffect(() => {
    // 初始化监控
    mobileMonitor.initialize();
    reactNativeMonitor.initialize();
    
    // 设置用户信息
    mobileMonitor.setUser({
      id: 'user123',
      email: 'user@example.com',
      username: 'testuser',
    });
    
    // 追踪应用启动
    mobileMonitor.trackUserAction('app_start', {
      startTime: Date.now(),
      version: '1.0.0',
    });
    
    return () => {
      // 清理监控
      mobileMonitor.cleanup();
      reactNativeMonitor.cleanup();
    };
  }, []);
  
  const handleNavigationStateChange = (state: any) => {
    // 追踪导航变化
    const currentRoute = state?.routes[state.index];
    if (currentRoute) {
      mobileMonitor.trackUserAction('navigation', {
        routeName: currentRoute.name,
        params: currentRoute.params,
      });
      
      reactNativeMonitor.trackNavigation(currentRoute.name, currentRoute.params);
    }
  };
  
  return (
    <Provider store={store}>
      <NavigationContainer onStateChange={handleNavigationStateChange}>
        <Stack.Navigator>
          <Stack.Screen 
            name="Home" 
            component={HomeScreen} 
            options={{ title: '首页' }}
          />
          <Stack.Screen 
            name="Profile" 
            component={ProfileScreen} 
            options={{ title: '个人资料' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </Provider>
  );
};

export default App;
```

### 5.2 Flutter完整集成示例

```dart
// main.dart - Flutter主应用
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'monitoring/flutter_monitoring_integrator.dart';
import 'screens/home_screen.dart';
import 'screens/profile_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // 初始化监控
  final monitor = FlutterMonitoringIntegrator(
    FlutterMonitoringConfig(
      sentryDsn: 'YOUR_SENTRY_DSN',
      environment: kDebugMode ? 'development' : 'production',
      enablePerformanceMonitoring: true,
      enableNetworkMonitoring: true,
      enableWidgetTracking: true,
      enableRouteTracking: true,
      enableUserInteractionTracking: true,
      performanceSampleRate: 0.1,
      maxBreadcrumbs: 100,
      uploadInterval: Duration(minutes: 5),
    ),
  );
  
  runApp(MyApp(monitor: monitor));
}

class MyApp extends StatelessWidget {
  final FlutterMonitoringIntegrator monitor;
  
  const MyApp({Key? key, required this.monitor}) : super(key: key);
  
  @override
  Widget build(BuildContext context) {
    return Provider.value(
      value: monitor,
      child: MaterialApp(
        title: 'Flutter监控示例',
        theme: ThemeData(
          primarySwatch: Colors.blue,
        ),
        home: const HomeScreen(),
        onGenerateRoute: (settings) {
          // 追踪路由变化
          monitor.trackRoute(settings.name ?? 'unknown', 
                            settings.arguments as Map<String, dynamic>?);
          
          switch (settings.name) {
            case '/':
              return MaterialPageRoute(builder: (_) => const HomeScreen());
            case '/profile':
              return MaterialPageRoute(builder: (_) => const ProfileScreen());
            default:
              return MaterialPageRoute(
                builder: (_) => Scaffold(
                  appBar: AppBar(title: const Text('页面未找到')),
                  body: const Center(child: Text('404 - 页面未找到')),
                ),
              );
          }
        },
      ),
    );
  }
}

// 监控Widget示例
class MonitoredWidget extends StatefulWidget {
  @override
  _MonitoredWidgetState createState() => _MonitoredWidgetState();
}

class _MonitoredWidgetState extends State<MonitoredWidget> 
    with WidgetPerformanceMonitor<MonitoredWidget> {
  
  @override
  void initState() {
    super.initState();
    final monitor = Provider.of<FlutterMonitoringIntegrator>(context, listen: false);
    initializeMonitor(monitor);
  }
  
  @override
  Widget buildWidget(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          const Text('这是一个被监控的Widget'),
          ElevatedButton(
            onPressed: () {
              // 追踪用户交互
              final monitor = Provider.of<FlutterMonitoringIntegrator>(context, listen: false);
              monitor.trackUserAction('button_click', {
                'button': 'example_button',
                'timestamp': DateTime.now().millisecondsSinceEpoch,
              });
            },
            child: const Text('点击我'),
          ),
        ],
      ),
    );
  }
}
```

### 5.3 原生模块集成示例

```typescript
// NativeMonitorBridge.ts - 原生模块桥接
import { NativeModules, Platform } from 'react-native';

interface NativeMonitorInterface {
  getSystemMetrics(): Promise<{
    memory: {
      totalMemory: number;
      freeMemory: number;
      usedMemory: number;
    };
    battery?: {
      level: number;
      state: number;
    };
    timestamp: number;
  }>;
  
  trackNativeEvent(eventName: string, data: Record<string, any>): Promise<void>;
}

const NativeMonitor: NativeMonitorInterface = Platform.select({
  ios: NativeModules.IOSNativeMonitor,
  android: NativeModules.AndroidNativeMonitor,
  default: {
    getSystemMetrics: () => Promise.resolve({
      memory: { totalMemory: 0, freeMemory: 0, usedMemory: 0 },
      timestamp: Date.now(),
    }),
    trackNativeEvent: () => Promise.resolve(),
  },
});

export class NativeMonitorBridge {
  private static instance: NativeMonitorBridge;
  private metricsInterval?: NodeJS.Timeout;
  
  static getInstance(): NativeMonitorBridge {
    if (!NativeMonitorBridge.instance) {
      NativeMonitorBridge.instance = new NativeMonitorBridge();
    }
    return NativeMonitorBridge.instance;
  }
  
  // 开始系统指标监控
  startSystemMetricsMonitoring(interval: number = 60000): void {
    this.stopSystemMetricsMonitoring();
    
    this.metricsInterval = setInterval(async () => {
      try {
        const metrics = await NativeMonitor.getSystemMetrics();
        
        // 检查内存使用情况
        const memoryUsagePercent = (metrics.memory.usedMemory / metrics.memory.totalMemory) * 100;
        
        if (memoryUsagePercent > 80) {
          await this.trackNativeEvent('high_memory_usage', {
            percentage: memoryUsagePercent,
            ...metrics.memory,
          });
        }
        
        // 检查电池状态（iOS）
        if (metrics.battery && metrics.battery.level < 0.2) {
          await this.trackNativeEvent('low_battery', {
            level: metrics.battery.level,
            state: metrics.battery.state,
          });
        }
        
      } catch (error) {
        console.error('Failed to get system metrics:', error);
      }
    }, interval);
  }
  
  // 停止系统指标监控
  stopSystemMetricsMonitoring(): void {
    if (this.metricsInterval) {
      clearInterval(this.metricsInterval);
      this.metricsInterval = undefined;
    }
  }
  
  // 追踪原生事件
  async trackNativeEvent(eventName: string, data: Record<string, any>): Promise<void> {
    try {
      await NativeMonitor.trackNativeEvent(eventName, {
        ...data,
        platform: Platform.OS,
        timestamp: Date.now(),
      });
    } catch (error) {
      console.error('Failed to track native event:', error);
    }
  }
  
  // 获取当前系统指标
  async getCurrentMetrics(): Promise<any> {
    try {
      return await NativeMonitor.getSystemMetrics();
    } catch (error) {
      console.error('Failed to get current metrics:', error);
      return null;
    }
  }
}

// 使用示例
const nativeMonitor = NativeMonitorBridge.getInstance();

// 在应用启动时开始监控
nativeMonitor.startSystemMetricsMonitoring(30000); // 每30秒检查一次

// 追踪特定事件
nativeMonitor.trackNativeEvent('user_login', {
  userId: 'user123',
  loginMethod: 'email',
});

export { NativeMonitorBridge };
```

## 6. 最佳实践与总结

### 6.1 实施建议

#### 监控策略

1. **分层监控**
   - 应用层：用户行为、业务流程
   - 框架层：组件性能、路由变化
   - 系统层：内存、CPU、网络
   - 原生层：设备状态、系统事件

2. **性能优化**
   - 合理设置采样率，避免性能影响
   - 使用离线队列处理网络异常
   - 实施数据压缩和批量上传
   - 避免在关键路径上进行监控

3. **数据管理**
   - 敏感数据脱敏处理
   - 设置合理的数据保留期
   - 实施数据分类和标签管理
   - 建立数据清理机制

#### 错误处理

1. **错误分类**
   - JavaScript错误：语法错误、运行时错误
   - 网络错误：请求失败、超时
   - 原生错误：崩溃、内存警告
   - 业务错误：逻辑错误、数据异常

2. **错误上下文**
   - 用户信息：ID、角色、权限
   - 设备信息：型号、系统版本、网络状态
   - 应用状态：版本、环境、配置
   - 操作路径：用户行为轨迹

3. **告警机制**
   - 实时告警：严重错误、崩溃
   - 趋势告警：错误率上升、性能下降
   - 阈值告警：内存使用、网络延迟
   - 智能告警：异常检测、模式识别

### 6.2 核心价值

#### 技术价值

1. **全面监控**
   - 跨平台统一监控体系
   - 端到端性能追踪
   - 实时错误检测和报告
   - 深度性能分析

2. **开发效率**
   - 快速问题定位和修复
   - 自动化错误收集和分析
   - 性能瓶颈识别
   - 代码质量提升

3. **运维保障**
   - 主动问题发现
   - 系统健康度监控
   - 容量规划支持
   - 故障预警机制

#### 业务价值

1. **用户体验**
   - 提升应用稳定性
   - 优化性能表现
   - 减少崩溃率
   - 改善响应速度

2. **产品质量**
   - 数据驱动的优化决策
   - 用户行为洞察
   - 功能使用分析
   - 版本质量评估

3. **成本控制**
   - 减少人工排查成本
   - 提高问题解决效率
   - 降低维护成本
   - 优化资源使用

### 6.3 未来发展趋势

#### 技术演进

1. **AI驱动监控**
   - 智能异常检测
   - 自动根因分析
   - 预测性维护
   - 自适应阈值调整

2. **边缘计算**
   - 本地数据处理
   - 实时分析能力
   - 离线监控支持
   - 带宽优化

3. **云原生集成**
   - 容器化部署
   - 微服务监控
   - 服务网格集成
   - 多云支持

#### 监控演进

1. **可观测性**
   - 指标、日志、链路追踪融合
   - 分布式追踪
   - 业务监控
   - 用户体验监控

2. **自动化运维**
   - 自动故障恢复
   - 智能扩缩容
   - 配置自动调优
   - 预测性扩容

3. **隐私保护**
   - 数据本地化处理
   - 差分隐私技术
   - 联邦学习应用
   - 合规性增强

通过构建完善的移动端监控体系，我们能够实现对跨平台应用的全方位监控，提升应用质量和用户体验，为业务发展提供强有力的技术保障。
  
  // 网络监控
  private setupNetworkMonitoring(): void {
    if (!this.config.enableNetworkMonitoring) return;
    
    this.networkMonitor = NetInfo.addEventListener(state => {
      this.handleNetworkChange(state);
    });
    
    // 拦截网络请求
    this.interceptNetworkRequests();
  }
  
  private handleNetworkChange(state: any): void {
    const networkInfo = {
      type: state.type,
      isConnected: state.isConnected,
      isInternetReachable: state.isInternetReachable,
      details: state.details
    };
    
    // 更新设备信息
    if (this.deviceInfo) {
      this.deviceInfo.network = {
        type: state.type || 'unknown',
        isConnected: state.isConnected || false,
        isInternetReachable: state.isInternetReachable || false
      };
    }
    
    // 记录网络变化
    Sentry.addBreadcrumb({
      category: 'network',
      message: 'Network state changed',
      level: 'info',
      data: networkInfo
    });
    
    // 处理离线队列
    if (state.isConnected && this.offlineQueue.length > 0) {
      this.processOfflineQueue();
    }
  }
  
  // 网络请求拦截
  private interceptNetworkRequests(): void {
    const originalFetch = global.fetch;
    
    global.fetch = async (input: RequestInfo, init?: RequestInit) => {
      const startTime = performance.now();
      const url = typeof input === 'string' ? input : input.url;
      const method = init?.method || 'GET';
      
      // 开始网络事务
      const transaction = Sentry.startTransaction({
        name: `${method} ${this.sanitizeUrl(url)}`,
        op: 'http.client'
      });
      
      try {
        const response = await originalFetch(input, init);
        const duration = performance.now() - startTime;
        
        // 记录成功请求
        this.recordNetworkRequest({
          url,
          method,
          status: response.status,
          duration,
          success: true,
          size: this.getResponseSize(response)
        });
        
        transaction.setHttpStatus(response.status);
        transaction.finish();
        
        return response;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        // 记录失败请求
        this.recordNetworkRequest({
          url,
          method,
          status: 0,
          duration,
          success: false,
          error: error.message
        });
        
        transaction.setStatus('internal_error');
        transaction.finish();
        
        throw error;
      }
    };
  }
  
  // 离线支持
  private setupOfflineSupport(): void {
    if (!this.config.enableOfflineSupport) return;
    
    // 定期上传离线数据
    setInterval(() => {
      this.processOfflineQueue();
    }, this.config.uploadInterval);
  }
  
  private async processOfflineQueue(): Promise<void> {
    if (this.offlineQueue.length === 0) return;
    
    const netInfo = await NetInfo.fetch();
    if (!netInfo.isConnected) return;
    
    try {
      const queueToProcess = [...this.offlineQueue];
      this.offlineQueue = [];
      
      for (const item of queueToProcess) {
        await this.sendOfflineData(item);
      }
      
      // 清理本地存储
      await AsyncStorage.removeItem('sentry_offline_queue');
      
    } catch (error) {
      // 如果上传失败，重新加入队列
      this.offlineQueue.push(...this.offlineQueue);
      Sentry.captureException(error);
    }
  }
  
  private async sendOfflineData(data: any): Promise<void> {
    // 发送离线数据到Sentry
    if (data.type === 'event') {
      Sentry.captureEvent(data.payload);
    } else if (data.type === 'breadcrumb') {
      Sentry.addBreadcrumb(data.payload);
    } else if (data.type === 'user_action') {
      this.trackUserAction(data.payload.action, data.payload.data);
    }
  }
  
  // 用户行为追踪
  public trackUserAction(action: string, data?: any): void {
    if (!this.config.enableUserTracking) return;
    
    const actionData = {
      action,
      data: this.sanitizeData(data),
      timestamp: Date.now(),
      sessionId: this.getSessionId(),
      userId: this.getUserId()
    };
    
    // 检查网络状态
    NetInfo.fetch().then(netInfo => {
      if (netInfo.isConnected) {
        Sentry.addBreadcrumb({
          category: 'user-action',
          message: `User action: ${action}`,
          level: 'info',
          data: actionData
        });
      } else {
        // 离线时加入队列
        this.addToOfflineQueue({
          type: 'user_action',
          payload: actionData
        });
      }
    });
  }
  
  // 性能监控
  public trackPerformanceMetric(name: string, value: number, unit: string = 'ms'): void {
    if (!this.config.enablePerformanceMonitoring) return;
    
    Sentry.setMeasurement(name, value, unit);
    
    Sentry.addBreadcrumb({
      category: 'performance',
      message: `Performance metric: ${name}`,
      level: 'info',
      data: {
        name,
        value,
        unit,
        timestamp: Date.now()
      }
    });
  }
  
  // 应用生命周期监控
  public trackAppLifecycle(event: 'foreground' | 'background' | 'terminate'): void {
    const lifecycleData = {
      event,
      timestamp: Date.now(),
      sessionDuration: Date.now() - this.sessionStartTime,
      memoryUsage: this.getCurrentMemoryUsage()
    };
    
    Sentry.addBreadcrumb({
      category: 'app-lifecycle',
      message: `App lifecycle: ${event}`,
      level: 'info',
      data: lifecycleData
    });
    
    // 更新应用指标
    this.updateAppMetrics(event);
  }
  
  // 崩溃报告
  public reportCrash(error: Error, context?: any): void {
    if (!this.config.enableCrashReporting) return;
    
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'crash');
      scope.setLevel('fatal');
      
      if (context) {
        scope.setContext('crash', context);
      }
      
      // 添加设备和应用状态
      scope.setContext('device-state', {
        deviceInfo: this.deviceInfo,
        appMetrics: this.appMetrics,
        timestamp: Date.now()
      });
      
      Sentry.captureException(error);
    });
    
    // 更新崩溃计数
    this.appMetrics.crashCount++;
  }
  
  // 辅助方法
  private async getDeviceModel(): Promise<string> {
    try {
      // 这里需要使用react-native-device-info库
      // return DeviceInfo.getModel();
      return Platform.OS === 'ios' ? 'iPhone' : 'Android Device';
    } catch {
      return 'Unknown';
    }
  }
  
  private async getDeviceManufacturer(): Promise<string> {
    try {
      // return DeviceInfo.getManufacturer();
      return Platform.OS === 'ios' ? 'Apple' : 'Unknown';
    } catch {
      return 'Unknown';
    }
  }
  
  private async getDeviceMemory(): Promise<number> {
    try {
      // return DeviceInfo.getTotalMemory();
      return 0;
    } catch {
      return 0;
    }
  }
  
  private async getStorageInfo(): Promise<{ total: number; free: number }> {
    try {
      // return DeviceInfo.getFreeDiskStorage();
      return { total: 0, free: 0 };
    } catch {
      return { total: 0, free: 0 };
    }
  }
  
  private async getBatteryInfo(): Promise<{ level: number; charging: boolean }> {
    try {
      // return DeviceInfo.getBatteryLevel();
      return { level: 0, charging: false };
    } catch {
      return { level: 0, charging: false };
    }
  }
  
  private sanitizeUrl(url: string): string {
    // 移除敏感参数
    return url.replace(/([?&])(token|key|password|secret)=[^&]*/gi, '$1$2=***');
  }
  
  private sanitizeData(data: any): any {
    if (!data) return data;
    
    const sensitiveKeys = ['password', 'token', 'key', 'secret', 'auth'];
    const sanitized = { ...data };
    
    for (const key of sensitiveKeys) {
      if (sanitized[key]) {
        sanitized[key] = '***';
      }
    }
    
    return sanitized;
  }
  
  private getResponseSize(response: Response): number {
    const contentLength = response.headers.get('content-length');
    return contentLength ? parseInt(contentLength, 10) : 0;
  }
  
  private recordNetworkRequest(request: any): void {
    // 更新网络使用统计
    if (request.success) {
      this.appMetrics.networkUsage.received += request.size || 0;
    }
    
    Sentry.addBreadcrumb({
      category: 'http',
      message: `${request.method} ${request.url}`,
      level: request.success ? 'info' : 'error',
      data: {
        url: this.sanitizeUrl(request.url),
        method: request.method,
        status: request.status,
        duration: request.duration,
        size: request.size,
        success: request.success,
        error: request.error
      }
    });
  }
  
  private addToOfflineQueue(item: any): void {
    this.offlineQueue.push(item);
    
    // 限制队列大小
    if (this.offlineQueue.length > this.config.maxCacheSize) {
      this.offlineQueue.shift();
    }
    
    // 保存到本地存储
    AsyncStorage.setItem('sentry_offline_queue', JSON.stringify(this.offlineQueue));
  }
  
  private initializeMetrics(): AppMetrics {
    return {
      launchTime: Date.now(),
      memoryUsage: 0,
      cpuUsage: 0,
      batteryUsage: 0,
      networkUsage: { sent: 0, received: 0 },
      crashCount: 0,
      errorCount: 0,
      sessionDuration: 0
    };
  }
  
  private getCurrentMemoryUsage(): number {
    // 这里需要原生模块支持
    return 0;
  }
  
  private updateAppMetrics(event: string): void {
    this.appMetrics.sessionDuration = Date.now() - this.sessionStartTime;
    
    if (event === 'background') {
      // 保存会话数据
      AsyncStorage.setItem('app_metrics', JSON.stringify(this.appMetrics));
    }
  }
  
  private getSessionId(): string {
    // 生成或获取会话ID
    return 'session_' + Date.now();
  }
  
  private getUserId(): string | null {
    // 获取用户ID
    return null;
  }
  
  private beforeSendEvent(event: any): any {
    // 事件发送前的处理
    if (this.deviceInfo) {
      event.contexts = event.contexts || {};
      event.contexts.device = this.deviceInfo;
    }
    
    return event;
  }
  
  private beforeBreadcrumb(breadcrumb: any): any {
    // 面包屑发送前的处理
    return breadcrumb;
  }
  
  // 获取应用指标
  public getAppMetrics(): AppMetrics {
    this.appMetrics.sessionDuration = Date.now() - this.sessionStartTime;
    return { ...this.appMetrics };
  }
  
  // 清理资源
  public destroy(): void {
    if (this.networkMonitor) {
      this.networkMonitor();
    }
  }
}

export { MobileMonitoringIntegrator, MobileMonitoringConfig, DeviceInfo, AppMetrics };
```

## 2. React Native集成

### 2.1 React Native监控组件

```typescript
// React Native监控组件
import React, { useEffect, useRef, useState } from 'react';
import { AppState, AppStateStatus } from 'react-native';
import * as Sentry from '@sentry/react-native';
import { MobileMonitoringIntegrator } from './MobileMonitoringIntegrator';

interface ReactNativeMonitorConfig {
  enableComponentTracking: boolean;
  enableNavigationTracking: boolean;
  enableGestureTracking: boolean;
  enableRenderTracking: boolean;
  slowRenderThreshold: number;
  maxComponentDepth: number;
}

interface ComponentMetrics {
  name: string;
  renderCount: number;
  averageRenderTime: number;
  slowRenders: number;
  errorCount: number;
  lastRenderTime: number;
}

class ReactNativeMonitor {
  private config: ReactNativeMonitorConfig;
  private monitoringIntegrator: MobileMonitoringIntegrator;
  private componentMetrics = new Map<string, ComponentMetrics>();
  private navigationHistory: any[] = [];
  private currentScreen: string = '';
  
  constructor(
    config: ReactNativeMonitorConfig,
    monitoringIntegrator: MobileMonitoringIntegrator
  ) {
    this.config = config;
    this.monitoringIntegrator = monitoringIntegrator;
    this.setupAppStateMonitoring();
  }
  
  // 应用状态监控
  private setupAppStateMonitoring(): void {
    AppState.addEventListener('change', (nextAppState: AppStateStatus) => {
      this.handleAppStateChange(nextAppState);
    });
  }
  
  private handleAppStateChange(nextAppState: AppStateStatus): void {
    let lifecycleEvent: 'foreground' | 'background' | 'terminate';
    
    switch (nextAppState) {
      case 'active':
        lifecycleEvent = 'foreground';
        break;
      case 'background':
      case 'inactive':
        lifecycleEvent = 'background';
        break;
      default:
        lifecycleEvent = 'terminate';
    }
    
    this.monitoringIntegrator.trackAppLifecycle(lifecycleEvent);
  }
  
  // 组件性能监控HOC
  public withComponentMonitoring<P extends object>(
    WrappedComponent: React.ComponentType<P>,
    componentName?: string
  ): React.ComponentType<P> {
    if (!this.config.enableComponentTracking) {
      return WrappedComponent;
    }
    
    const MonitoredComponent: React.FC<P> = (props) => {
      const name = componentName || WrappedComponent.displayName || WrappedComponent.name || 'Unknown';
      const renderStartTime = useRef<number>(0);
      const renderCount = useRef<number>(0);
      const [hasError, setHasError] = useState(false);
      
      // 渲染开始
      renderStartTime.current = performance.now();
      renderCount.current++;
      
      useEffect(() => {
        // 渲染完成
        const renderTime = performance.now() - renderStartTime.current;
        this.recordComponentRender(name, renderTime);
        
        // 检查慢渲染
        if (renderTime > this.config.slowRenderThreshold) {
          this.recordSlowRender(name, renderTime, props);
        }
      });
      
      useEffect(() => {
        // 组件挂载
        this.recordComponentMount(name);
        
        return () => {
          // 组件卸载
          this.recordComponentUnmount(name);
        };
      }, []);
      
      // 错误边界
      const ErrorBoundary: React.FC<{ children: React.ReactNode }> = ({ children }) => {
        useEffect(() => {
          const errorHandler = (error: Error, errorInfo: any) => {
            setHasError(true);
            this.recordComponentError(name, error, errorInfo, props);
          };
          
          // 这里需要实现错误捕获逻辑
          return () => {};
        }, []);
        
        if (hasError) {
          return null; // 或者返回错误UI
        }
        
        return <>{children}</>;
      };
      
      return (
        <ErrorBoundary>
          <WrappedComponent {...props} />
        </ErrorBoundary>
      );
    };
    
    MonitoredComponent.displayName = `withComponentMonitoring(${name})`;
    return MonitoredComponent;
  }
  
  // 导航监控
  public trackNavigation(screenName: string, params?: any): void {
    if (!this.config.enableNavigationTracking) return;
    
    const previousScreen = this.currentScreen;
    this.currentScreen = screenName;
    
    const navigationData = {
      from: previousScreen,
      to: screenName,
      params: this.sanitizeParams(params),
      timestamp: Date.now()
    };
    
    this.navigationHistory.push(navigationData);
    
    // 保持导航历史长度
    if (this.navigationHistory.length > 50) {
      this.navigationHistory.shift();
    }
    
    // 开始屏幕事务
    const transaction = Sentry.startTransaction({
      name: screenName,
      op: 'navigation'
    });
    
    // 记录导航
    Sentry.addBreadcrumb({
      category: 'navigation',
      message: `Navigate to ${screenName}`,
      level: 'info',
      data: navigationData
    });
    
    // 追踪用户行为
    this.monitoringIntegrator.trackUserAction('screen_view', {
      screenName,
      previousScreen,
      params
    });
    
    transaction.finish();
  }
  
  // 手势监控
  public trackGesture(gestureType: string, target: string, data?: any): void {
    if (!this.config.enableGestureTracking) return;
    
    const gestureData = {
      type: gestureType,
      target,
      data: this.sanitizeParams(data),
      timestamp: Date.now(),
      screen: this.currentScreen
    };
    
    Sentry.addBreadcrumb({
      category: 'gesture',
      message: `${gestureType} on ${target}`,
      level: 'info',
      data: gestureData
    });
    
    this.monitoringIntegrator.trackUserAction('gesture', gestureData);
  }
  
  // 组件渲染记录
  private recordComponentRender(name: string, renderTime: number): void {
    let metrics = this.componentMetrics.get(name);
    
    if (!metrics) {
      metrics = {
        name,
        renderCount: 0,
        averageRenderTime: 0,
        slowRenders: 0,
        errorCount: 0,
        lastRenderTime: 0
      };
      this.componentMetrics.set(name, metrics);
    }
    
    metrics.renderCount++;
    metrics.lastRenderTime = renderTime;
    metrics.averageRenderTime = (
      (metrics.averageRenderTime * (metrics.renderCount - 1) + renderTime) /
      metrics.renderCount
    );
    
    // 记录性能指标
    this.monitoringIntegrator.trackPerformanceMetric(
      `component.${name}.render_time`,
      renderTime
    );
  }
  
  private recordSlowRender(name: string, renderTime: number, props: any): void {
    const metrics = this.componentMetrics.get(name);
    if (metrics) {
      metrics.slowRenders++;
    }
    
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-render');
      scope.setLevel('warning');
      
      scope.setContext('slowRender', {
        componentName: name,
        renderTime,
        threshold: this.config.slowRenderThreshold,
        props: this.sanitizeParams(props)
      });
      
      Sentry.captureMessage(
        `Slow render detected: ${name} (${renderTime}ms)`,
        'warning'
      );
    });
  }
  
  private recordComponentMount(name: string): void {
    Sentry.addBreadcrumb({
      category: 'component',
      message: `Component mounted: ${name}`,
      level: 'info',
      data: {
        componentName: name,
        action: 'mount',
        timestamp: Date.now()
      }
    });
  }
  
  private recordComponentUnmount(name: string): void {
    Sentry.addBreadcrumb({
      category: 'component',
      message: `Component unmounted: ${name}`,
      level: 'info',
      data: {
        componentName: name,
        action: 'unmount',
        timestamp: Date.now()
      }
    });
  }
  
  private recordComponentError(name: string, error: Error, errorInfo: any, props: any): void {
    const metrics = this.componentMetrics.get(name);
    if (metrics) {
      metrics.errorCount++;
    }
    
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'component-error');
      scope.setLevel('error');
      
      scope.setContext('componentError', {
        componentName: name,
        props: this.sanitizeParams(props),
        errorInfo,
        renderCount: metrics?.renderCount || 0
      });
      
      Sentry.captureException(error);
    });
  }
  
  private sanitizeParams(params: any): any {
    if (!params) return params;
    
    const sensitiveKeys = ['password', 'token', 'key', 'secret', 'auth'];
    const sanitized = { ...params };
    
    for (const key of sensitiveKeys) {
      if (sanitized[key]) {
        sanitized[key] = '***';
      }
    }
    
    return sanitized;
  }
  
  // 获取组件性能报告
  public getComponentMetrics(): ComponentMetrics[] {
    return Array.from(this.componentMetrics.values());
  }
  
  // 获取导航历史
  public getNavigationHistory(): any[] {
    return [...this.navigationHistory];
  }
  
  // 获取当前屏幕
  public getCurrentScreen(): string {
    return this.currentScreen;
  }
}

export { ReactNativeMonitor, ReactNativeMonitorConfig, ComponentMetrics };
```

## 3. Flutter集成

### 3.1 Flutter监控集成器

```dart
// Flutter监控集成器
import 'package:sentry_flutter/sentry_flutter.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'dart:io';
import 'dart:convert';

class FlutterMonitoringConfig {
  final String sentryDsn;
  final String environment;
  final bool enablePerformanceMonitoring;
  final bool enableNetworkMonitoring;
  final bool enableWidgetTracking;
  final bool enableRouteTracking;
  final bool enableUserInteractionTracking;
  final double performanceSampleRate;
  final int maxBreadcrumbs;
  final Duration uploadInterval;
  
  const FlutterMonitoringConfig({
    required this.sentryDsn,
    required this.environment,
    this.enablePerformanceMonitoring = true,
    this.enableNetworkMonitoring = true,
    this.enableWidgetTracking = true,
    this.enableRouteTracking = true,
    this.enableUserInteractionTracking = true,
    this.performanceSampleRate = 0.1,
    this.maxBreadcrumbs = 100,
    this.uploadInterval = const Duration(minutes: 5),
  });
}

class DeviceInformation {
  final String platform;
  final String version;
  final String model;
  final String manufacturer;
  final Map<String, dynamic> screenInfo;
  final Map<String, dynamic> memoryInfo;
  final Map<String, dynamic> storageInfo;
  final Map<String, dynamic> networkInfo;
  
  const DeviceInformation({
    required this.platform,
    required this.version,
    required this.model,
    required this.manufacturer,
    required this.screenInfo,
    required this.memoryInfo,
    required this.storageInfo,
    required this.networkInfo,
  });
  
  Map<String, dynamic> toJson() {
    return {
      'platform': platform,
      'version': version,
      'model': model,
      'manufacturer': manufacturer,
      'screenInfo': screenInfo,
      'memoryInfo': memoryInfo,
      'storageInfo': storageInfo,
      'networkInfo': networkInfo,
    };
  }
}

class FlutterMonitoringIntegrator {
  final FlutterMonitoringConfig config;
  DeviceInformation? _deviceInfo;
  final List<Map<String, dynamic>> _offlineQueue = [];
  final Map<String, dynamic> _performanceMetrics = {};
  late final Connectivity _connectivity;
  
  FlutterMonitoringIntegrator(this.config) {
    _connectivity = Connectivity();
    _initializeSentry();
    _setupDeviceInfo();
    _setupNetworkMonitoring();
  }
  
  // 初始化Sentry
  Future<void> _initializeSentry() async {
    await SentryFlutter.init(
      (options) {
        options.dsn = config.sentryDsn;
        options.environment = config.environment;
        options.tracesSampleRate = config.performanceSampleRate;
        options.maxBreadcrumbs = config.maxBreadcrumbs;
        options.enableAutoSessionTracking = true;
        options.enableOutOfMemoryTracking = true;
        options.enableAutoPerformanceTracing = config.enablePerformanceMonitoring;
        options.enableUserInteractionTracing = config.enableUserInteractionTracking;
        
        // 添加集成
        options.addIntegration(SentryFlutterIntegration());
        
        // 事件处理
        options.beforeSend = (event, hint) => _beforeSendEvent(event, hint);
        options.beforeBreadcrumb = (breadcrumb, hint) => _beforeBreadcrumb(breadcrumb, hint);
      },
    );
  }
  
  // 设备信息收集
  Future<void> _setupDeviceInfo() async {
    try {
      final deviceInfoPlugin = DeviceInfoPlugin();
      final packageInfo = await PackageInfo.fromPlatform();
      final connectivityResult = await _connectivity.checkConnectivity();
      
      Map<String, dynamic> deviceData = {};
      String platform = '';
      String version = '';
      String model = '';
      String manufacturer = '';
      
      if (Platform.isAndroid) {
        final androidInfo = await deviceInfoPlugin.androidInfo;
        platform = 'Android';
        version = androidInfo.version.release;
        model = androidInfo.model;
        manufacturer = androidInfo.manufacturer;
        deviceData = {
          'brand': androidInfo.brand,
          'device': androidInfo.device,
          'hardware': androidInfo.hardware,
          'androidId': androidInfo.androidId,
          'sdkInt': androidInfo.version.sdkInt,
        };
      } else if (Platform.isIOS) {
        final iosInfo = await deviceInfoPlugin.iosInfo;
        platform = 'iOS';
        version = iosInfo.systemVersion;
        model = iosInfo.model;
        manufacturer = 'Apple';
        deviceData = {
          'name': iosInfo.name,
          'systemName': iosInfo.systemName,
          'identifierForVendor': iosInfo.identifierForVendor,
          'isPhysicalDevice': iosInfo.isPhysicalDevice,
        };
      }
      
      _deviceInfo = DeviceInformation(
        platform: platform,
        version: version,
        model: model,
        manufacturer: manufacturer,
        screenInfo: await _getScreenInfo(),
        memoryInfo: await _getMemoryInfo(),
        storageInfo: await _getStorageInfo(),
        networkInfo: {
          'type': connectivityResult.toString(),
          'isConnected': connectivityResult != ConnectivityResult.none,
        },
      );
      
      // 设置Sentry上下文
      Sentry.configureScope((scope) {
        scope.setContext('device', _deviceInfo!.toJson());
        scope.setContext('app', {
          'name': packageInfo.appName,
          'version': packageInfo.version,
          'buildNumber': packageInfo.buildNumber,
          'packageName': packageInfo.packageName,
        });
      });
      
      // 记录设备信息
      Sentry.addBreadcrumb(Breadcrumb(
        message: 'Device information collected',
        category: 'device',
        level: SentryLevel.info,
        data: _deviceInfo!.toJson(),
      ));
      
    } catch (error) {
      Sentry.captureException(error);
    }
  }
  
  // 网络监控
  void _setupNetworkMonitoring() {
    if (!config.enableNetworkMonitoring) return;
    
    _connectivity.onConnectivityChanged.listen((ConnectivityResult result) {
      _handleNetworkChange(result);
    });
  }
  
  void _handleNetworkChange(ConnectivityResult result) {
    final networkInfo = {
      'type': result.toString(),
      'isConnected': result != ConnectivityResult.none,
      'timestamp': DateTime.now().millisecondsSinceEpoch,
    };
    
    // 更新设备信息
    if (_deviceInfo != null) {
      _deviceInfo = DeviceInformation(
        platform: _deviceInfo!.platform,
        version: _deviceInfo!.version,
        model: _deviceInfo!.model,
        manufacturer: _deviceInfo!.manufacturer,
        screenInfo: _deviceInfo!.screenInfo,
        memoryInfo: _deviceInfo!.memoryInfo,
        storageInfo: _deviceInfo!.storageInfo,
        networkInfo: networkInfo,
      );
    }
    
    // 记录网络变化
    Sentry.addBreadcrumb(Breadcrumb(
      message: 'Network state changed',
      category: 'network',
      level: SentryLevel.info,
      data: networkInfo,
    ));
    
    // 处理离线队列
    if (result != ConnectivityResult.none && _offlineQueue.isNotEmpty) {
      _processOfflineQueue();
    }
  }
  
  // 路由监控
  void trackRoute(String routeName, Map<String, dynamic>? arguments) {
    if (!config.enableRouteTracking) return;
    
    final routeData = {
      'routeName': routeName,
      'arguments': _sanitizeData(arguments),
      'timestamp': DateTime.now().millisecondsSinceEpoch,
    };
    
    // 开始路由事务
    final transaction = Sentry.startTransaction(
      routeName,
      'navigation',
    );
    
    Sentry.addBreadcrumb(Breadcrumb(
      message: 'Navigate to $routeName',
      category: 'navigation',
      level: SentryLevel.info,
      data: routeData,
    ));
    
    // 追踪用户行为
    trackUserAction('route_change', routeData);
    
    transaction.finish();
  }
  
  // 用户行为追踪
  void trackUserAction(String action, Map<String, dynamic>? data) {
    if (!config.enableUserInteractionTracking) return;
    
    final actionData = {
      'action': action,
      'data': _sanitizeData(data),
      'timestamp': DateTime.now().millisecondsSinceEpoch,
    };
    
    _connectivity.checkConnectivity().then((result) {
      if (result != ConnectivityResult.none) {
        Sentry.addBreadcrumb(Breadcrumb(
          message: 'User action: $action',
          category: 'user-action',
          level: SentryLevel.info,
          data: actionData,
        ));
      } else {
        // 离线时加入队列
        _addToOfflineQueue({
          'type': 'user_action',
          'payload': actionData,
        });
      }
    });
  }
  
  // Widget性能监控
  void trackWidgetPerformance(String widgetName, Duration buildTime) {
    if (!config.enableWidgetTracking) return;
    
    final performanceData = {
      'widgetName': widgetName,
      'buildTime': buildTime.inMicroseconds,
      'timestamp': DateTime.now().millisecondsSinceEpoch,
    };
    
    // 更新性能指标
    _performanceMetrics[widgetName] = performanceData;
    
    // 记录性能指标
    Sentry.setMeasurement('widget_build_time', buildTime.inMicroseconds.toDouble(), SentryMeasurementUnit.duration);
    
    Sentry.addBreadcrumb(Breadcrumb(
      message: 'Widget performance: $widgetName',
      category: 'performance',
      level: SentryLevel.info,
      data: performanceData,
    ));
  }
  
  // 错误报告
  void reportError(dynamic error, StackTrace? stackTrace, {Map<String, dynamic>? context}) {
    Sentry.configureScope((scope) {
      if (context != null) {
        scope.setContext('error_context', context);
      }
      
      if (_deviceInfo != null) {
        scope.setContext('device_state', _deviceInfo!.toJson());
      }
      
      scope.setContext('performance_metrics', _performanceMetrics);
    });
    
    if (error is Exception) {
      Sentry.captureException(error, stackTrace: stackTrace);
    } else {
      Sentry.captureMessage(error.toString(), level: SentryLevel.error);
    }
  }
  
  // 离线队列处理
  Future<void> _processOfflineQueue() async {
    if (_offlineQueue.isEmpty) return;
    
    try {
      final queueToProcess = List.from(_offlineQueue);
      _offlineQueue.clear();
      
      for (final item in queueToProcess) {
        await _sendOfflineData(item);
      }
    } catch (error) {
      Sentry.captureException(error);
    }
  }
  
  Future<void> _sendOfflineData(Map<String, dynamic> data) async {
    final type = data['type'];
    final payload = data['payload'];
    
    switch (type) {
      case 'user_action':
        trackUserAction(payload['action'], payload['data']);
        break;
      case 'breadcrumb':
        Sentry.addBreadcrumb(Breadcrumb.fromJson(payload));
        break;
      case 'event':
        // 发送事件
        break;
    }
  }
  
  void _addToOfflineQueue(Map<String, dynamic> item) {
    _offlineQueue.add(item);
    
    // 限制队列大小
    if (_offlineQueue.length > 1000) {
      _offlineQueue.removeAt(0);
    }
  }
  
  // 辅助方法
  Future<Map<String, dynamic>> _getScreenInfo() async {
    try {
      final window = WidgetsBinding.instance.window;
      return {
        'width': window.physicalSize.width,
        'height': window.physicalSize.height,
        'devicePixelRatio': window.devicePixelRatio,
        'textScaleFactor': window.textScaleFactor,
      };
    } catch (error) {
      return {};
    }
  }
  
  Future<Map<String, dynamic>> _getMemoryInfo() async {
    try {
      // 这里需要原生模块支持
      return {
        'total': 0,
        'free': 0,
        'used': 0,
      };
    } catch (error) {
      return {};
    }
  }
  
  Future<Map<String, dynamic>> _getStorageInfo() async {
    try {
      // 这里需要原生模块支持
      return {
        'total': 0,
        'free': 0,
        'used': 0,
      };
    } catch (error) {
      return {};
    }
  }
  
  Map<String, dynamic>? _sanitizeData(Map<String, dynamic>? data) {
    if (data == null) return null;
    
    final sensitiveKeys = ['password', 'token', 'key', 'secret', 'auth'];
    final sanitized = Map<String, dynamic>.from(data);
    
    for (final key in sensitiveKeys) {
      if (sanitized.containsKey(key)) {
        sanitized[key] = '***';
      }
    }
    
    return sanitized;
  }
  
  SentryEvent? _beforeSendEvent(SentryEvent event, {dynamic hint}) {
    // 事件发送前的处理
    if (_deviceInfo != null) {
      event = event.copyWith(
        contexts: event.contexts?.copyWith(
          device: SentryDevice.fromJson(_deviceInfo!.toJson()),
        ),
      );
    }
    
    return event;
  }
  
  Breadcrumb? _beforeBreadcrumb(Breadcrumb breadcrumb, {dynamic hint}) {
    // 面包屑发送前的处理
    return breadcrumb;
  }
  
  // 获取性能指标
  Map<String, dynamic> getPerformanceMetrics() {
    return Map.from(_performanceMetrics);
  }
  
  // 获取设备信息
  DeviceInformation? getDeviceInfo() {
    return _deviceInfo;
  }
}

// Widget性能监控Mixin
mixin WidgetPerformanceMonitor<T extends StatefulWidget> on State<T> {
  late final Stopwatch _buildStopwatch;
  late final FlutterMonitoringIntegrator _monitor;
  
  void initializeMonitor(FlutterMonitoringIntegrator monitor) {
    _monitor = monitor;
    _buildStopwatch = Stopwatch();
  }
  
  @override
  Widget build(BuildContext context) {
    _buildStopwatch.start();
    
    final widget = buildWidget(context);
    
    _buildStopwatch.stop();
    _monitor.trackWidgetPerformance(
      T.toString(),
      _buildStopwatch.elapsed,
    );
    _buildStopwatch.reset();
    
    return widget;
  }
  
  Widget buildWidget(BuildContext context);
}

export { FlutterMonitoringIntegrator, FlutterMonitoringConfig, DeviceInformation, WidgetPerformanceMonitor };
```

## 4. 原生模块监控

### 4.1 iOS原生监控

```swift
// iOS原生监控模块
import Foundation
import Sentry
import UIKit

@objc(IOSNativeMonitor)
class IOSNativeMonitor: NSObject {
    private var performanceMetrics: [String: Any] = [:]
    private var memoryWarningCount = 0
    private var backgroundTaskIdentifier: UIBackgroundTaskIdentifier = .invalid
    
    override init() {
        super.init()
        setupMemoryWarningObserver()
        setupAppLifecycleObservers()
        setupPerformanceMonitoring()
    }
    
    // 内存警告监控
    private func setupMemoryWarningObserver() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleMemoryWarning),
            name: UIApplication.didReceiveMemoryWarningNotification,
            object: nil
        )
    }
    
    @objc private func handleMemoryWarning() {
        memoryWarningCount += 1
        
        let memoryInfo = getMemoryInfo()
        
        SentrySDK.configureScope { scope in
            scope.setContext("memory_warning", value: [
                "count": memoryWarningCount,
                "memoryInfo": memoryInfo,
                "timestamp": Date().timeIntervalSince1970
            ])
        }
        
        SentrySDK.capture(message: "Memory warning received") { scope in
            scope.setLevel(.warning)
            scope.setTag("issue_type", value: "memory_warning")
        }
    }
    
    // 应用生命周期监控
    private func setupAppLifecycleObservers() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appDidEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appWillEnterForeground),
            name: UIApplication.willEnterForegroundNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(appWillTerminate),
            name: UIApplication.willTerminateNotification,
            object: nil
        )
    }
    
    @objc private func appDidEnterBackground() {
        recordAppLifecycleEvent("background")
        
        // 开始后台任务
        backgroundTaskIdentifier = UIApplication.shared.beginBackgroundTask {
            self.endBackgroundTask()
        }
    }
    
    @objc private func appWillEnterForeground() {
        recordAppLifecycleEvent("foreground")
        endBackgroundTask()
    }
    
    @objc private func appWillTerminate() {
        recordAppLifecycleEvent("terminate")
    }
    
    private func endBackgroundTask() {
        if backgroundTaskIdentifier != .invalid {
            UIApplication.shared.endBackgroundTask(backgroundTaskIdentifier)
            backgroundTaskIdentifier = .invalid
        }
    }
    
    private func recordAppLifecycleEvent(_ event: String) {
        let lifecycleData: [String: Any] = [
            "event": event,
            "timestamp": Date().timeIntervalSince1970,
            "memoryInfo": getMemoryInfo(),
            "batteryInfo": getBatteryInfo()
        ]
        
        SentrySDK.addBreadcrumb(Breadcrumb(
            level: .info,
            category: "app-lifecycle"
        ) { breadcrumb in
            breadcrumb.message = "App lifecycle: \(event)"
            breadcrumb.data = lifecycleData
        })
    }
    
    // 性能监控
    private func setupPerformanceMonitoring() {
        // 监控主线程阻塞
        DispatchQueue.global(qos: .background).async {
            self.monitorMainThreadBlocking()
        }
        
        // 监控FPS
        startFPSMonitoring()
    }
    
    private func monitorMainThreadBlocking() {
        let threshold: TimeInterval = 0.4 // 400ms阈值
        
        while true {
            let startTime = CFAbsoluteTimeGetCurrent()
            
            DispatchQueue.main.sync {
                // 主线程任务
            }
            
            let endTime = CFAbsoluteTimeGetCurrent()
            let duration = endTime - startTime
            
            if duration > threshold {
                recordMainThreadBlocking(duration: duration)
            }
            
            Thread.sleep(forTimeInterval: 0.1)
        }
    }
    
    private func recordMainThreadBlocking(duration: TimeInterval) {
        SentrySDK.capture(message: "Main thread blocking detected") { scope in
            scope.setLevel(.warning)
            scope.setTag("performance_issue", value: "main_thread_blocking")
            scope.setContext("blocking", value: [
                "duration": duration,
                "threshold": 0.4,
                "timestamp": Date().timeIntervalSince1970
            ])
        }
    }
    
    private func startFPSMonitoring() {
        let displayLink = CADisplayLink(target: self, selector: #selector(updateFPS))
        displayLink.add(to: .main, forMode: .common)
    }
    
    @objc private func updateFPS() {
        // FPS计算逻辑
        // 这里需要实现FPS计算
    }
    
    // 系统信息获取
    private func getMemoryInfo() -> [String: Any] {
        var info = mach_task_basic_info()
        var count = mach_msg_type_number_t(MemoryLayout<mach_task_basic_info>.size)/4
        
        let kerr: kern_return_t = withUnsafeMutablePointer(to: &info) {
            $0.withMemoryRebound(to: integer_t.self, capacity: 1) {
                task_info(mach_task_self_,
                         task_flavor_t(MACH_TASK_BASIC_INFO),
                         $0,
                         &count)
            }
        }
        
        if kerr == KERN_SUCCESS {
            return [
                "resident_size": info.resident_size,
                "virtual_size": info.virtual_size,
                "resident_size_mb": Double(info.resident_size) / 1024.0 / 1024.0,
                "virtual_size_mb": Double(info.virtual_size) / 1024.0 / 1024.0
            ]
        }
        
        return [:]
    }
    
    private func getBatteryInfo() -> [String: Any] {
        UIDevice.current.isBatteryMonitoringEnabled = true
        
        return [
            "level": UIDevice.current.batteryLevel,
            "state": UIDevice.current.batteryState.rawValue
        ]
    }
    
    // React Native桥接方法
    @objc static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc func getSystemMetrics(_ resolve: @escaping RCTPromiseResolveBlock,
                               rejecter reject: @escaping RCTPromiseRejectBlock) {
        let metrics: [String: Any] = [
            "memory": getMemoryInfo(),
            "battery": getBatteryInfo(),
            "memoryWarningCount": memoryWarningCount,
            "timestamp": Date().timeIntervalSince1970
        ]
        
        resolve(metrics)
    }
    
    @objc func trackNativeEvent(_ eventName: String,
                               data: [String: Any],
                               resolver resolve: @escaping RCTPromiseResolveBlock,
                               rejecter reject: @escaping RCTPromiseRejectBlock) {
        SentrySDK.addBreadcrumb(Breadcrumb(
            level: .info,
            category: "native-event"
        ) { breadcrumb in
            breadcrumb.message = "Native event: \(eventName)"
            breadcrumb.data = data
        })
        
        resolve(nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
```

### 4.2 Android原生监控

```java
// Android原生监控模块
package com.yourapp.monitoring;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

import java.util.HashMap;
import java.util.Map;

public class AndroidNativeMonitor extends ReactContextBaseJavaModule implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AndroidNativeMonitor";
    private ReactApplicationContext reactContext;
    private Handler mainHandler;
    private long lastMemoryCheck = 0;
    private int lowMemoryCount = 0;
    private Map<String, Object> performanceMetrics = new HashMap<>();
    
    public AndroidNativeMonitor(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        setupActivityLifecycleMonitoring();
        setupMemoryMonitoring();
        setupPerformanceMonitoring();
    }
    
    @Override
    public String getName() {
        return "AndroidNativeMonitor";
    }
    
    // 活动生命周期监控
    private void setupActivityLifecycleMonitoring() {
        if (reactContext.getCurrentActivity() != null) {
            reactContext.getCurrentActivity().getApplication()
                .registerActivityLifecycleCallbacks(this);
        }
    }
    
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        recordActivityLifecycle("created", activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityStarted(Activity activity) {
        recordActivityLifecycle("started", activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityResumed(Activity activity) {
        recordActivityLifecycle("resumed", activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityPaused(Activity activity) {
        recordActivityLifecycle("paused", activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityStopped(Activity activity) {
        recordActivityLifecycle("stopped", activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        recordActivityLifecycle("saveInstanceState", activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityDestroyed(Activity activity) {
        recordActivityLifecycle("destroyed", activity.getClass().getSimpleName());
    }
    
    private void recordActivityLifecycle(String event, String activityName) {
        Map<String, Object> data = new HashMap<>();
        data.put("event", event);
        data.put("activity", activityName);
        data.put("timestamp", System.currentTimeMillis());
        data.put("memoryInfo", getMemoryInfo());
        
        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setMessage("Activity lifecycle: " + event);
        breadcrumb.setCategory("activity-lifecycle");
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setData(data);
        
        Sentry.addBreadcrumb(breadcrumb);
    }
    
    // 内存监控
    private void setupMemoryMonitoring() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                checkMemoryUsage();
                mainHandler.postDelayed(this, 30000); // 每30秒检查一次
            }
        });
    }
    
    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        // 检查内存使用率
        if (memoryUsagePercent > 80) {
            recordHighMemoryUsage(memoryUsagePercent, usedMemory, maxMemory);
        }
        
        // 检查内存泄漏
        if (lastMemoryCheck > 0) {
            long memoryDiff = usedMemory - lastMemoryCheck;
            if (memoryDiff > 50 * 1024 * 1024) { // 50MB增长
                recordPotentialMemoryLeak(memoryDiff);
            }
        }
        
        lastMemoryCheck = usedMemory;
    }
    
    private void recordHighMemoryUsage(double percentage, long used, long max) {
        Map<String, Object> data = new HashMap<>();
        data.put("percentage", percentage);
        data.put("usedMemory", used);
        data.put("maxMemory", max);
        data.put("timestamp", System.currentTimeMillis());
        
        Sentry.withScope(scope -> {
            scope.setTag("performance_issue", "high_memory_usage");
            scope.setLevel(SentryLevel.WARNING);
            scope.setContext("memory", data);
            
            Sentry.captureMessage("High memory usage detected: " + String.format("%.2f%%", percentage));
        });
    }
    
    private void recordPotentialMemoryLeak(long memoryIncrease) {
        Map<String, Object> data = new HashMap<>();
        data.put("memoryIncrease", memoryIncrease);
        data.put("timestamp", System.currentTimeMillis());
        
        Sentry.withScope(scope -> {
            scope.setTag("performance_issue", "potential_memory_leak");
            scope.setLevel(SentryLevel.WARNING);
            scope.setContext("memoryLeak", data);
            
            Sentry.captureMessage("Potential memory leak detected: " + (memoryIncrease / 1024 / 1024) + "MB increase");
        });
    }
    
    // 性能监控
    private void setupPerformanceMonitoring() {
        // 监控主线程阻塞
        new Thread(() -> {
            while (true) {
                long startTime = System.currentTimeMillis();
                
                mainHandler.post(() -> {
                    // 主线程任务
                });
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                if (duration > 500) { // 500ms阈值
                    recordMainThreadBlocking(duration);
                }
            }
        }).start();
    }
    
    private void recordMainThreadBlocking(long duration) {
        Map<String, Object> data = new HashMap<>();
        data.put("duration", duration);
        data.put("threshold", 500);
        data.put("timestamp", System.currentTimeMillis());
        
        Sentry.withScope(scope -> {
            scope.setTag("performance_issue", "main_thread_blocking");
            scope.setLevel(SentryLevel.WARNING);
            scope.setContext("blocking", data);
            
            Sentry.captureMessage("Main thread blocking detected: " + duration + "ms");
        });
    }
    
    // 系统信息获取
    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        
        Map<String, Object> info = new HashMap<>();
        info.put("totalMemory", runtime.totalMemory());
        info.put("freeMemory", runtime.freeMemory());
        info.put("maxMemory", runtime.maxMemory());
        info.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        info.put("dalvikPrivateDirty", memoryInfo.dalvikPrivateDirty);
        info.put("dalvikPss", memoryInfo.dalvikPss);
        info.put("nativePrivateDirty", memoryInfo.nativePrivateDirty);
        info.put("nativePss", memoryInfo.nativePss);
        
        return info;
    }
    
    // React Native桥接方法
    @ReactMethod
    public void getSystemMetrics(Promise promise) {
        try {
            WritableMap metrics = new WritableNativeMap();
            Map<String, Object> memoryInfo = getMemoryInfo();
            
            WritableMap memoryMap = new WritableNativeMap();
            for (Map.Entry<String, Object> entry : memoryInfo.entrySet()) {
                if (entry.getValue() instanceof Long) {
                    memoryMap.putDouble(entry.getKey(), ((Long) entry.getValue()).doubleValue());
                } else if (entry.getValue() instanceof Integer) {
                    memoryMap.putInt(entry.getKey(), (Integer) entry.getValue());
                }
            }
            
            metrics.putMap("memory", memoryMap);
            metrics.putInt("lowMemoryCount", lowMemoryCount);
            metrics.putDouble("timestamp", System.currentTimeMillis());
            
            promise.resolve(metrics);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }
    
    @ReactMethod
    public void trackNativeEvent(String eventName, ReadableMap data, Promise promise) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            // 转换ReadableMap到Map
            // 这里需要实现转换逻辑
            
            Breadcrumb breadcrumb = new Breadcrumb();
            breadcrumb.setMessage("Native event: " + eventName);
            breadcrumb.setCategory("native-event");
            breadcrumb.setLevel(SentryLevel.INFO);
            breadcrumb.setData(eventData);
            
            Sentry.addBreadcrumb(breadcrumb);
            
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }
}
```