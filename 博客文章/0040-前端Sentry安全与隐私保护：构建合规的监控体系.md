# 前端Sentry安全与隐私保护：构建合规的监控体系

在现代Web应用开发中，错误监控和性能追踪已成为保障用户体验的重要手段。然而，随着数据保护法规的日益严格和用户隐私意识的提升，如何在有效监控的同时保护用户隐私和敏感数据，成为开发团队面临的重要挑战。本文将深入探讨前端Sentry的安全与隐私保护实践，帮助开发者构建既高效又合规的监控体系。

## 1. 数据隐私保护基础

### 1.1 敏感数据识别与分类

```javascript
// 敏感数据分类器
class SensitiveDataClassifier {
  constructor(options = {}) {
    this.options = {
      enablePIIDetection: true,
      enableFinancialDataDetection: true,
      enableHealthDataDetection: true,
      customPatterns: [],
      ...options
    }

// 数据加密工具
class DataEncryption {
  constructor(options = {}) {
    this.options = {
      algorithm: 'AES-GCM',
      keyLength: 256,
      ...options
    };
    
    this.key = null;
    this.initializeKey();
  }
  
  async initializeKey() {
    // 在实际应用中，密钥应该从安全的密钥管理服务获取
    const keyMaterial = await window.crypto.subtle.importKey(
      'raw',
      new TextEncoder().encode('your-secret-key-here-32-chars!!'),
      { name: 'PBKDF2' },
      false,
      ['deriveKey']
    );
    
    this.key = await window.crypto.subtle.deriveKey(
      {
        name: 'PBKDF2',
        salt: new TextEncoder().encode('salt'),
        iterations: 100000,
        hash: 'SHA-256'
      },
      keyMaterial,
      { name: this.options.algorithm, length: this.options.keyLength },
      false,
      ['encrypt', 'decrypt']
    );
  }
  
  async encrypt(data) {
    if (!this.key) await this.initializeKey();
    
    const iv = window.crypto.getRandomValues(new Uint8Array(12));
    const encodedData = new TextEncoder().encode(data);
    
    const encrypted = await window.crypto.subtle.encrypt(
      {
        name: this.options.algorithm,
        iv: iv
      },
      this.key,
      encodedData
    );
    
    // 将IV和加密数据组合
    const combined = new Uint8Array(iv.length + encrypted.byteLength);
    combined.set(iv);
    combined.set(new Uint8Array(encrypted), iv.length);
    
    return btoa(String.fromCharCode(...combined));
  }
  
  async decrypt(encryptedData) {
    if (!this.key) await this.initializeKey();
    
    const combined = new Uint8Array(atob(encryptedData).split('').map(c => c.charCodeAt(0)));
    const iv = combined.slice(0, 12);
    const data = combined.slice(12);
    
    const decrypted = await window.crypto.subtle.decrypt(
      {
        name: this.options.algorithm,
        iv: iv
      },
      this.key,
      data
    );
    
    return new TextDecoder().decode(decrypted);
  }
}
```

## 4. 数据治理与审计

### 4.1 数据治理框架

```javascript
// 数据治理管理器
class DataGovernanceManager {
  constructor(options = {}) {
    this.options = {
      enableAuditLogging: true,
      dataRetentionPeriod: 90, // 天
      enableDataClassification: true,
      enableAccessControl: true,
      ...options
    };
    
    this.auditLogger = new AuditLogger();
    this.accessController = new AccessController();
    this.dataClassifier = new SensitiveDataClassifier();
    this.retentionManager = new DataRetentionManager();
  }
  
  // 数据生命周期管理
  async manageDataLifecycle(data, context) {
    const governanceResult = {
      allowed: false,
      actions: [],
      metadata: {},
      auditTrail: []
    };
    
    try {
      // 1. 数据分类
      const classification = await this.classifyData(data, context);
      governanceResult.metadata.classification = classification;
      
      // 2. 访问控制检查
      const accessCheck = await this.checkAccess(context, classification);
      if (!accessCheck.allowed) {
        governanceResult.reason = accessCheck.reason;
        this.auditLogger.logAccessDenied(context, classification, accessCheck.reason);
        return governanceResult;
      }
      
      // 3. 数据处理策略
      const processingPolicy = this.getProcessingPolicy(classification);
      governanceResult.metadata.processingPolicy = processingPolicy;
      
      // 4. 应用数据处理规则
      const processedData = await this.applyProcessingRules(data, processingPolicy);
      governanceResult.processedData = processedData;
      
      // 5. 设置保留策略
      const retentionPolicy = this.getRetentionPolicy(classification);
      governanceResult.metadata.retentionPolicy = retentionPolicy;
      
      // 6. 记录审计日志
      this.auditLogger.logDataAccess({
        context,
        classification,
        processingPolicy,
        retentionPolicy,
        timestamp: new Date().toISOString()
      });
      
      governanceResult.allowed = true;
      governanceResult.actions = this.getRecommendedActions(classification);
      
    } catch (error) {
      this.auditLogger.logError({
        context,
        error: error.message,
        timestamp: new Date().toISOString()
      });
      
      governanceResult.error = error.message;
    }
    
    return governanceResult;
  }
  
  async classifyData(data, context) {
    const classification = {
      sensitivityLevel: 'LOW',
      dataTypes: [],
      personalData: false,
      specialCategories: [],
      jurisdiction: 'EU', // 默认按最严格的标准
      processingBasis: null
    };
    
    // 使用数据分类器分析
    const analysis = this.dataClassifier.classifyData(data);
    
    classification.sensitivityLevel = this.getSensitivityLevelName(analysis.overallSensitivity);
    classification.dataTypes = analysis.categories;
    classification.personalData = analysis.categories.has('PII');
    
    // 确定特殊类别数据
    if (analysis.categories.has('Health')) {
      classification.specialCategories.push('health');
    }
    if (analysis.categories.has('Financial')) {
      classification.specialCategories.push('financial');
    }
    
    // 确定处理依据
    classification.processingBasis = this.determineProcessingBasis(classification, context);
    
    return classification;
  }
  
  async checkAccess(context, classification) {
    // 检查用户权限
    const userPermissions = await this.accessController.getUserPermissions(context.userId);
    
    // 检查数据访问权限
    const requiredPermission = this.getRequiredPermission(classification);
    
    if (!userPermissions.includes(requiredPermission)) {
      return {
        allowed: false,
        reason: `Insufficient permissions. Required: ${requiredPermission}`
      };
    }
    
    // 检查时间限制
    if (this.isOutsideAllowedHours(context)) {
      return {
        allowed: false,
        reason: 'Access outside allowed hours'
      };
    }
    
    // 检查地理位置限制
    if (this.isRestrictedLocation(context)) {
      return {
        allowed: false,
        reason: 'Access from restricted location'
      };
    }
    
    return { allowed: true };
  }
  
  getProcessingPolicy(classification) {
    const policies = {
      LOW: {
        encryption: false,
        anonymization: false,
        retention: 30,
        sharing: 'allowed'
      },
      MEDIUM: {
        encryption: true,
        anonymization: false,
        retention: 90,
        sharing: 'restricted'
      },
      HIGH: {
        encryption: true,
        anonymization: true,
        retention: 30,
        sharing: 'prohibited'
      },
      CRITICAL: {
        encryption: true,
        anonymization: true,
        retention: 7,
        sharing: 'prohibited'
      }
    };
    
    return policies[classification.sensitivityLevel] || policies.LOW;
  }
  
  async applyProcessingRules(data, policy) {
    let processedData = data;
    
    // 应用加密
    if (policy.encryption) {
      const encryption = new DataEncryption();
      processedData = await encryption.encrypt(JSON.stringify(processedData));
    }
    
    // 应用匿名化
    if (policy.anonymization) {
      processedData = this.anonymizeData(processedData);
    }
    
    return processedData;
  }
  
  anonymizeData(data) {
    // 简单的匿名化实现
    const anonymized = JSON.parse(JSON.stringify(data));
    
    // 移除直接标识符
    const identifiers = ['id', 'userId', 'email', 'phone', 'name'];
    identifiers.forEach(id => {
      if (anonymized[id]) {
        delete anonymized[id];
      }
    });
    
    // 添加随机噪声到数值数据
    Object.keys(anonymized).forEach(key => {
      if (typeof anonymized[key] === 'number') {
        const noise = (Math.random() - 0.5) * 0.1; // ±5% 噪声
        anonymized[key] = anonymized[key] * (1 + noise);
      }
    });
    
    return anonymized;
  }
  
  getRetentionPolicy(classification) {
    const retentionPeriods = {
      LOW: 365,    // 1年
      MEDIUM: 180, // 6个月
      HIGH: 90,    // 3个月
      CRITICAL: 30 // 1个月
    };
    
    return {
      period: retentionPeriods[classification.sensitivityLevel] || 90,
      unit: 'days',
      autoDelete: true,
      archiveBeforeDelete: classification.sensitivityLevel !== 'CRITICAL'
    };
  }
  
  determineProcessingBasis(classification, context) {
    // GDPR第6条处理依据
    if (classification.personalData) {
      if (context.consent) {
        return 'consent';
      }
      if (context.contractual) {
        return 'contract';
      }
      if (context.legalObligation) {
        return 'legal_obligation';
      }
      return 'legitimate_interest';
    }
    
    return 'not_applicable';
  }
  
  getRequiredPermission(classification) {
    const permissionMap = {
      LOW: 'data.read.basic',
      MEDIUM: 'data.read.sensitive',
      HIGH: 'data.read.confidential',
      CRITICAL: 'data.read.restricted'
    };
    
    return permissionMap[classification.sensitivityLevel] || 'data.read.basic';
  }
  
  isOutsideAllowedHours(context) {
    // 检查是否在允许的时间范围内
    const now = new Date();
    const hour = now.getHours();
    
    // 假设允许时间为 6:00 - 22:00
    return hour < 6 || hour > 22;
  }
  
  isRestrictedLocation(context) {
    // 检查地理位置限制
    const restrictedCountries = ['CN', 'RU', 'IR']; // 示例
    return restrictedCountries.includes(context.country);
  }
  
  getRecommendedActions(classification) {
    const actions = [];
    
    if (classification.sensitivityLevel === 'HIGH' || classification.sensitivityLevel === 'CRITICAL') {
      actions.push('Enable additional monitoring');
      actions.push('Require multi-factor authentication');
      actions.push('Implement data loss prevention');
    }
    
    if (classification.personalData) {
      actions.push('Verify user consent');
      actions.push('Implement data subject rights');
    }
    
    if (classification.specialCategories.length > 0) {
      actions.push('Apply special category protections');
      actions.push('Conduct privacy impact assessment');
    }
    
    return actions;
  }
  
  getSensitivityLevelName(level) {
    const names = {
      1: 'LOW',
      2: 'MEDIUM',
      3: 'HIGH',
      4: 'CRITICAL'
    };
    return names[level] || 'LOW';
  }
}
```

### 4.2 审计日志系统

```javascript
// 审计日志记录器
class AuditLogger {
  constructor(options = {}) {
    this.options = {
      enableLocalStorage: true,
      enableRemoteLogging: true,
      logLevel: 'INFO',
      maxLocalLogs: 1000,
      remoteEndpoint: '/api/audit-logs',
      ...options
    };
    
    this.localLogs = [];
    this.logQueue = [];
    this.isProcessing = false;
  }
  
  // 记录数据访问
  logDataAccess(details) {
    const logEntry = {
      type: 'DATA_ACCESS',
      timestamp: new Date().toISOString(),
      userId: details.context.userId,
      sessionId: details.context.sessionId,
      ipAddress: details.context.ipAddress,
      userAgent: details.context.userAgent,
      dataClassification: details.classification,
      processingPolicy: details.processingPolicy,
      retentionPolicy: details.retentionPolicy,
      success: true
    };
    
    this.writeLog(logEntry);
  }
  
  // 记录访问拒绝
  logAccessDenied(context, classification, reason) {
    const logEntry = {
      type: 'ACCESS_DENIED',
      timestamp: new Date().toISOString(),
      userId: context.userId,
      sessionId: context.sessionId,
      ipAddress: context.ipAddress,
      userAgent: context.userAgent,
      dataClassification: classification,
      reason: reason,
      success: false
    };
    
    this.writeLog(logEntry);
  }
  
  // 记录同意变更
  logConsentChange(details) {
    const logEntry = {
      type: 'CONSENT_CHANGE',
      timestamp: new Date().toISOString(),
      userId: details.userId,
      action: details.action, // 'granted', 'revoked', 'updated'
      purposes: details.purposes,
      previousConsent: details.previousConsent,
      newConsent: details.newConsent,
      ipAddress: details.ipAddress,
      userAgent: details.userAgent
    };
    
    this.writeLog(logEntry);
  }
  
  // 记录数据主体权利请求
  logDataSubjectRequest(details) {
    const logEntry = {
      type: 'DATA_SUBJECT_REQUEST',
      timestamp: new Date().toISOString(),
      userId: details.userId,
      requestType: details.requestType, // 'access', 'rectification', 'deletion', 'portability'
      status: details.status, // 'received', 'processing', 'completed', 'rejected'
      reason: details.reason,
      processedBy: details.processedBy,
      completedAt: details.completedAt
    };
    
    this.writeLog(logEntry);
  }
  
  // 记录数据泄露事件
  logDataBreach(details) {
    const logEntry = {
      type: 'DATA_BREACH',
      timestamp: new Date().toISOString(),
      severity: details.severity, // 'low', 'medium', 'high', 'critical'
      affectedUsers: details.affectedUsers,
      dataTypes: details.dataTypes,
      breachSource: details.breachSource,
      detectionMethod: details.detectionMethod,
      containmentActions: details.containmentActions,
      notificationRequired: details.notificationRequired,
      reportedToAuthority: details.reportedToAuthority
    };
    
    this.writeLog(logEntry);
  }
  
  // 记录系统错误
  logError(details) {
    const logEntry = {
      type: 'SYSTEM_ERROR',
      timestamp: new Date().toISOString(),
      error: details.error,
      context: details.context,
      stackTrace: details.stackTrace,
      userId: details.context?.userId,
      sessionId: details.context?.sessionId
    };
    
    this.writeLog(logEntry);
  }
  
  // 记录配置变更
  logConfigurationChange(details) {
    const logEntry = {
      type: 'CONFIGURATION_CHANGE',
      timestamp: new Date().toISOString(),
      changedBy: details.changedBy,
      component: details.component,
      previousValue: details.previousValue,
      newValue: details.newValue,
      reason: details.reason
    };
    
    this.writeLog(logEntry);
  }
  
  writeLog(logEntry) {
    // 添加通用字段
    logEntry.id = this.generateLogId();
    logEntry.level = this.options.logLevel;
    logEntry.source = 'sentry-privacy-manager';
    
    // 本地存储
    if (this.options.enableLocalStorage) {
      this.storeLocalLog(logEntry);
    }
    
    // 远程日志
    if (this.options.enableRemoteLogging) {
      this.queueRemoteLog(logEntry);
    }
    
    // 控制台输出（开发环境）
    if (process.env.NODE_ENV === 'development') {
      console.log('Audit Log:', logEntry);
    }
  }
  
  storeLocalLog(logEntry) {
    this.localLogs.push(logEntry);
    
    // 限制本地日志数量
    if (this.localLogs.length > this.options.maxLocalLogs) {
      this.localLogs.shift();
    }
    
    // 持久化到localStorage
    try {
      localStorage.setItem('audit_logs', JSON.stringify(this.localLogs));
    } catch (error) {
      console.error('Failed to store audit log locally:', error);
    }
  }
  
  queueRemoteLog(logEntry) {
    this.logQueue.push(logEntry);
    
    // 异步处理队列
    if (!this.isProcessing) {
      this.processLogQueue();
    }
  }
  
  async processLogQueue() {
    if (this.isProcessing || this.logQueue.length === 0) {
      return;
    }
    
    this.isProcessing = true;
    
    try {
      while (this.logQueue.length > 0) {
        const batch = this.logQueue.splice(0, 10); // 批量处理
        
        await this.sendLogBatch(batch);
        
        // 避免过于频繁的请求
        await this.delay(1000);
      }
    } catch (error) {
      console.error('Failed to process log queue:', error);
      
      // 将失败的日志重新加入队列
      this.logQueue.unshift(...batch);
    } finally {
      this.isProcessing = false;
    }
  }
  
  async sendLogBatch(logs) {
    const response = await fetch(this.options.remoteEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      },
      body: JSON.stringify({ logs })
    });
    
    if (!response.ok) {
      throw new Error(`Failed to send logs: ${response.statusText}`);
    }
  }
  
  // 查询审计日志
  queryLogs(filters = {}) {
    let filteredLogs = [...this.localLogs];
    
    // 按类型过滤
    if (filters.type) {
      filteredLogs = filteredLogs.filter(log => log.type === filters.type);
    }
    
    // 按用户过滤
    if (filters.userId) {
      filteredLogs = filteredLogs.filter(log => log.userId === filters.userId);
    }
    
    // 按时间范围过滤
    if (filters.startDate) {
      filteredLogs = filteredLogs.filter(log => 
        new Date(log.timestamp) >= new Date(filters.startDate)
      );
    }
    
    if (filters.endDate) {
      filteredLogs = filteredLogs.filter(log => 
        new Date(log.timestamp) <= new Date(filters.endDate)
      );
    }
    
    // 排序
    filteredLogs.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    
    return filteredLogs;
  }
  
  // 生成审计报告
  generateAuditReport(period = 'month') {
    const now = new Date();
    let startDate;
    
    switch (period) {
      case 'day':
        startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        break;
      case 'week':
        startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        break;
      case 'month':
        startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        break;
      default:
        startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    }
    
    const logs = this.queryLogs({ startDate: startDate.toISOString() });
    
    const report = {
      period,
      startDate: startDate.toISOString(),
      endDate: now.toISOString(),
      totalEvents: logs.length,
      eventsByType: {},
      userActivity: {},
      securityEvents: [],
      complianceMetrics: {}
    };
    
    // 统计事件类型
    logs.forEach(log => {
      report.eventsByType[log.type] = (report.eventsByType[log.type] || 0) + 1;
      
      if (log.userId) {
        report.userActivity[log.userId] = (report.userActivity[log.userId] || 0) + 1;
      }
      
      // 识别安全事件
      if (['ACCESS_DENIED', 'DATA_BREACH', 'SYSTEM_ERROR'].includes(log.type)) {
        report.securityEvents.push(log);
      }
    });
    
    // 计算合规指标
    const consentLogs = logs.filter(log => log.type === 'CONSENT_CHANGE');
    const dataSubjectRequests = logs.filter(log => log.type === 'DATA_SUBJECT_REQUEST');
    
    report.complianceMetrics = {
      consentChanges: consentLogs.length,
      dataSubjectRequests: dataSubjectRequests.length,
      averageResponseTime: this.calculateAverageResponseTime(dataSubjectRequests),
      breachIncidents: logs.filter(log => log.type === 'DATA_BREACH').length
    };
    
    return report;
  }
  
  calculateAverageResponseTime(requests) {
    const completedRequests = requests.filter(req => req.completedAt);
    
    if (completedRequests.length === 0) return 0;
    
    const totalTime = completedRequests.reduce((sum, req) => {
      const start = new Date(req.timestamp);
      const end = new Date(req.completedAt);
      return sum + (end - start);
    }, 0);
    
    return totalTime / completedRequests.length / (1000 * 60 * 60); // 小时
  }
  
  generateLogId() {
    return `log_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  getAuthToken() {
    // 获取认证令牌
    return localStorage.getItem('auth_token') || '';
  }
  
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
  
  // 清理过期日志
  cleanupExpiredLogs() {
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - 90); // 保留90天
    
    this.localLogs = this.localLogs.filter(log => 
      new Date(log.timestamp) > cutoffDate
    );
    
    // 更新localStorage
    try {
      localStorage.setItem('audit_logs', JSON.stringify(this.localLogs));
    } catch (error) {
      console.error('Failed to cleanup expired logs:', error);
    }
  }
}
```

## 5. 最佳实践与实施建议

### 5.1 安全配置最佳实践

```javascript
// 安全配置最佳实践指南
class SecurityBestPractices {
  static getRecommendedSentryConfig() {
    return {
      // 基础安全设置
      sendDefaultPii: false,
      beforeSend: SecurityBestPractices.createSecureBeforeSend(),
      beforeBreadcrumb: SecurityBestPractices.createSecureBreadcrumb(),
      
      // 采样配置
      sampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
      tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.01 : 0.1,
      
      // 环境配置
      environment: process.env.NODE_ENV,
      
      // 发布配置
      release: process.env.SENTRY_RELEASE,
      
      // 集成配置
      integrations: SecurityBestPractices.getSecureIntegrations(),
      
      // 传输配置
      transport: SecurityBestPractices.getSecureTransport()
    };
  }
  
  static createSecureBeforeSend() {
    const classifier = new SensitiveDataClassifier();
    const sanitizer = new DataSanitizer();
    
    return (event, hint) => {
      try {
        // 1. 环境检查
        if (process.env.NODE_ENV === 'development' && !process.env.SENTRY_DEV_ENABLED) {
          return null;
        }
        
        // 2. 敏感数据检查和清理
        const sensitiveAnalysis = classifier.checkSentryEvent(event);
        
        if (SecurityBestPractices.containsCriticalData(sensitiveAnalysis)) {
          event = sanitizer.sanitizeSentryEvent(event, sensitiveAnalysis);
        }
        
        // 3. 移除敏感头信息
        if (event.request && event.request.headers) {
          SecurityBestPractices.sanitizeHeaders(event.request.headers);
        }
        
        // 4. 清理查询参数
        if (event.request && event.request.query_string) {
          event.request.query_string = SecurityBestPractices.sanitizeQueryString(
            event.request.query_string
          );
        }
        
        // 5. 添加安全标记
        event.tags = {
          ...event.tags,
          security_processed: 'true',
          privacy_compliant: 'true'
        };
        
        return event;
      } catch (error) {
        console.error('Error in beforeSend:', error);
        return null; // 安全起见，丢弃事件
      }
    };
  }
  
  static createSecureBreadcrumb() {
    return (breadcrumb, hint) => {
      // 过滤敏感的面包屑类型
      const sensitiveCategories = ['auth', 'user', 'navigation'];
      
      if (breadcrumb.category && sensitiveCategories.includes(breadcrumb.category)) {
        return null;
      }
      
      // 清理面包屑数据
      if (breadcrumb.data) {
        breadcrumb.data = SecurityBestPractices.sanitizeBreadcrumbData(breadcrumb.data);
      }
      
      return breadcrumb;
    };
  }
  
  static getSecureIntegrations() {
    return [
      // 只启用必要的集成
      new Sentry.Integrations.GlobalHandlers({
        onerror: true,
        onunhandledrejection: true
      }),
      new Sentry.Integrations.Breadcrumbs({
        console: false, // 禁用控制台面包屑
        dom: true,
        fetch: true,
        history: false, // 禁用历史记录面包屑
        sentry: true,
        xhr: true
      })
    ];
  }
  
  static getSecureTransport() {
    // 使用自定义传输确保HTTPS
    return undefined; // 使用默认的安全传输
  }
  
  static containsCriticalData(sensitiveAnalysis) {
    const checkCritical = (analysis) => {
      return analysis && analysis.sensitiveFields && 
             analysis.sensitiveFields.some(field => field.level >= 4);
    };
    
    return checkCritical(sensitiveAnalysis.message) ||
           checkCritical(sensitiveAnalysis.extra) ||
           checkCritical(sensitiveAnalysis.user);
  }
  
  static sanitizeHeaders(headers) {
    const sensitiveHeaders = [
      'authorization',
      'cookie',
      'x-api-key',
      'x-auth-token',
      'x-access-token',
      'authentication'
    ];
    
    sensitiveHeaders.forEach(header => {
      delete headers[header];
      delete headers[header.toLowerCase()];
      delete headers[header.toUpperCase()];
    });
  }
  
  static sanitizeQueryString(queryString) {
    const sensitiveParams = ['password', 'token', 'api_key', 'secret', 'auth', 'key'];
    
    return queryString.split('&').map(param => {
      const [key, value] = param.split('=');
      if (sensitiveParams.some(sensitive => 
        key.toLowerCase().includes(sensitive.toLowerCase())
      )) {
        return `${key}=[REDACTED]`;
      }
      return param;
    }).join('&');
  }
  
  static sanitizeBreadcrumbData(data) {
    const sanitized = { ...data };
    const sensitiveKeys = ['password', 'token', 'authorization', 'secret', 'key'];
    
    Object.keys(sanitized).forEach(key => {
      if (sensitiveKeys.some(sensitive => 
        key.toLowerCase().includes(sensitive.toLowerCase())
      )) {
        sanitized[key] = '[REDACTED]';
      }
    });
    
    return sanitized;
  }
  
  // 安全检查清单
  static getSecurityChecklist() {
    return {
      configuration: [
        '✓ 禁用默认PII发送 (sendDefaultPii: false)',
        '✓ 实施beforeSend钩子进行数据清理',
        '✓ 配置适当的采样率',
        '✓ 设置环境特定的配置',
        '✓ 使用HTTPS传输'
      ],
      dataProtection: [
        '✓ 实施敏感数据检测',
        '✓ 配置数据脱敏规则',
        '✓ 移除认证信息',
        '✓ 清理查询参数',
        '✓ 过滤敏感面包屑'
      ],
      compliance: [
        '✓ 实施用户同意机制',
        '✓ 支持数据主体权利',
        '✓ 配置数据保留策略',
        '✓ 实施审计日志',
        '✓ 定期合规性检查'
      ],
      monitoring: [
        '✓ 监控数据泄露',
        '✓ 跟踪访问模式',
        '✓ 检测异常活动',
        '✓ 生成安全报告',
        '✓ 实施告警机制'
      ]
    };
  }
  
  // 实施建议
  static getImplementationGuidelines() {
    return {
      phase1: {
        title: '基础安全配置',
        duration: '1-2周',
        tasks: [
          '配置安全的Sentry初始化',
          '实施基本的数据清理',
          '设置环境特定配置',
          '测试数据脱敏功能'
        ]
      },
      phase2: {
        title: 'GDPR合规实施',
        duration: '2-3周',
        tasks: [
          '实施用户同意机制',
          '开发数据主体权利功能',
          '配置数据保留策略',
          '建立审计日志系统'
        ]
      },
      phase3: {
        title: '高级安全功能',
        duration: '2-4周',
        tasks: [
          '实施数据治理框架',
          '配置访问控制',
          '开发安全监控',
          '建立事件响应流程'
        ]
      },
      phase4: {
        title: '持续改进',
        duration: '持续',
        tasks: [
          '定期安全审计',
          '更新合规策略',
          '优化性能',
          '培训团队成员'
        ]
      }
    };
  }
}
```

## 6. 核心价值与未来发展

### 6.1 核心价值

通过实施前端Sentry的安全与隐私保护体系，我们能够实现：

1. **数据保护合规**：确保符合GDPR、CCPA等隐私法规要求
2. **用户信任建立**：通过透明的数据处理建立用户信任
3. **风险降低**：减少数据泄露和合规风险
4. **监控效果保持**：在保护隐私的同时维持监控效果
5. **团队协作优化**：建立清晰的数据治理流程

### 6.2 实施建议

1. **分阶段实施**：按照复杂度逐步实施各项功能
2. **团队培训**：确保团队理解隐私保护的重要性
3. **定期审计**：建立定期的安全和合规审计机制
4. **持续改进**：根据法规变化和最佳实践持续优化
5. **文档维护**：保持完整的实施和操作文档

### 6.3 未来发展趋势

1. **AI驱动的隐私保护**：利用机器学习自动识别和保护敏感数据
2. **零信任架构**：实施零信任安全模型
3. **隐私计算技术**：采用同态加密、差分隐私等先进技术
4. **跨境数据治理**：应对全球化的数据治理挑战
5. **实时合规监控**：实现实时的合规性监控和响应

## 总结

前端Sentry的安全与隐私保护是一个复杂但至关重要的主题。通过实施敏感数据识别、数据脱敏、GDPR合规、数据治理和审计日志等功能，我们可以构建一个既高效又合规的监控体系。

关键成功因素包括：
- 全面的数据分类和保护策略
- 用户友好的同意管理机制
- 强大的数据治理框架
- 完整的审计和监控体系
- 持续的合规性维护

随着隐私保护法规的不断发展和技术的进步，我们需要持续关注最新的发展趋势，不断优化和改进我们的隐私保护实践，确保在提供优质监控服务的同时，充分保护用户的隐私权益。;
    
    this.patterns = this.initializePatterns();
    this.sensitivityLevels = {
      LOW: 1,
      MEDIUM: 2,
      HIGH: 3,
      CRITICAL: 4
    };
  }
  
  initializePatterns() {
    return {
      // 个人身份信息 (PII)
      pii: {
        email: {
          pattern: /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/g,
          level: this.sensitivityLevels.HIGH,
          category: 'PII'
        },
        phone: {
          pattern: /\b(?:\+?1[-.]?)?\(?([0-9]{3})\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\b/g,
          level: this.sensitivityLevels.HIGH,
          category: 'PII'
        },
        ssn: {
          pattern: /\b\d{3}-?\d{2}-?\d{4}\b/g,
          level: this.sensitivityLevels.CRITICAL,
          category: 'PII'
        },
        ipAddress: {
          pattern: /\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b/g,
          level: this.sensitivityLevels.MEDIUM,
          category: 'PII'
        }
      },
      
      // 金融数据
      financial: {
        creditCard: {
          pattern: /\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3[0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\b/g,
          level: this.sensitivityLevels.CRITICAL,
          category: 'Financial'
        },
        bankAccount: {
          pattern: /\b\d{8,17}\b/g,
          level: this.sensitivityLevels.CRITICAL,
          category: 'Financial'
        },
        routingNumber: {
          pattern: /\b\d{9}\b/g,
          level: this.sensitivityLevels.HIGH,
          category: 'Financial'
        }
      },
      
      // 认证信息
      auth: {
        password: {
          pattern: /(?:password|pwd|pass)\s*[:=]\s*["']?([^\s"']+)["']?/gi,
          level: this.sensitivityLevels.CRITICAL,
          category: 'Authentication'
        },
        token: {
          pattern: /(?:token|jwt|bearer)\s*[:=]\s*["']?([A-Za-z0-9\-_\.]+)["']?/gi,
          level: this.sensitivityLevels.CRITICAL,
          category: 'Authentication'
        },
        apiKey: {
          pattern: /(?:api[_-]?key|apikey)\s*[:=]\s*["']?([A-Za-z0-9\-_]+)["']?/gi,
          level: this.sensitivityLevels.CRITICAL,
          category: 'Authentication'
        }
      },
      
      // 健康数据
      health: {
        medicalRecord: {
          pattern: /\b(?:diagnosis|prescription|medical|health)\b/gi,
          level: this.sensitivityLevels.HIGH,
          category: 'Health'
        }
      }
    };
  }
  
  classifyData(data) {
    const results = {
      sensitiveFields: [],
      overallSensitivity: this.sensitivityLevels.LOW,
      categories: new Set(),
      recommendations: []
    };
    
    const dataString = typeof data === 'string' ? data : JSON.stringify(data);
    
    // 检查所有模式
    Object.entries(this.patterns).forEach(([category, patterns]) => {
      if (!this.shouldCheckCategory(category)) return;
      
      Object.entries(patterns).forEach(([type, config]) => {
        const matches = dataString.match(config.pattern);
        if (matches) {
          results.sensitiveFields.push({
            type,
            category: config.category,
            level: config.level,
            matches: matches.length,
            sample: matches[0].substring(0, 10) + '...'
          });
          
          results.categories.add(config.category);
          results.overallSensitivity = Math.max(results.overallSensitivity, config.level);
        }
      });
    });
    
    // 生成建议
    results.recommendations = this.generateRecommendations(results);
    
    return results;
  }
  
  shouldCheckCategory(category) {
    switch (category) {
      case 'pii':
        return this.options.enablePIIDetection;
      case 'financial':
        return this.options.enableFinancialDataDetection;
      case 'health':
        return this.options.enableHealthDataDetection;
      default:
        return true;
    }
  }
  
  generateRecommendations(results) {
    const recommendations = [];
    
    if (results.overallSensitivity >= this.sensitivityLevels.HIGH) {
      recommendations.push('建议启用数据脱敏处理');
      recommendations.push('考虑使用数据加密传输');
    }
    
    if (results.categories.has('PII')) {
      recommendations.push('需要遵循GDPR/CCPA等隐私法规');
      recommendations.push('建议实施用户同意机制');
    }
    
    if (results.categories.has('Financial')) {
      recommendations.push('必须遵循PCI DSS标准');
      recommendations.push('建议使用专用的安全存储');
    }
    
    if (results.categories.has('Authentication')) {
      recommendations.push('立即移除认证信息');
      recommendations.push('检查代码中的硬编码凭据');
    }
    
    return recommendations;
  }
  
  // 检查Sentry事件中的敏感数据
  checkSentryEvent(event) {
    const sensitiveData = {
      message: null,
      exception: null,
      breadcrumbs: null,
      extra: null,
      user: null,
      tags: null
    };
    
    // 检查错误消息
    if (event.message) {
      sensitiveData.message = this.classifyData(event.message);
    }
    
    // 检查异常信息
    if (event.exception && event.exception.values) {
      sensitiveData.exception = event.exception.values.map(exc => ({
        type: exc.type,
        value: exc.value ? this.classifyData(exc.value) : null,
        stacktrace: exc.stacktrace ? this.checkStackTrace(exc.stacktrace) : null
      }));
    }
    
    // 检查面包屑
    if (event.breadcrumbs && event.breadcrumbs.values) {
      sensitiveData.breadcrumbs = event.breadcrumbs.values.map(breadcrumb => ({
        message: breadcrumb.message ? this.classifyData(breadcrumb.message) : null,
        data: breadcrumb.data ? this.classifyData(JSON.stringify(breadcrumb.data)) : null
      }));
    }
    
    // 检查额外数据
    if (event.extra) {
      sensitiveData.extra = this.classifyData(JSON.stringify(event.extra));
    }
    
    // 检查用户数据
    if (event.user) {
      sensitiveData.user = this.classifyData(JSON.stringify(event.user));
    }
    
    // 检查标签
    if (event.tags) {
      sensitiveData.tags = this.classifyData(JSON.stringify(event.tags));
    }
    
    return sensitiveData;
  }
  
  checkStackTrace(stacktrace) {
    if (!stacktrace.frames) return null;
    
    return stacktrace.frames.map(frame => {
      const frameData = {
        filename: frame.filename,
        function: frame.function,
        vars: null,
        context: null
      };
      
      // 检查变量
      if (frame.vars) {
        frameData.vars = this.classifyData(JSON.stringify(frame.vars));
      }
      
      // 检查上下文
      if (frame.context_line) {
        frameData.context = this.classifyData(frame.context_line);
      }
      
      return frameData;
    });
  }
}
```

### 1.2 数据脱敏处理器

```javascript
// 数据脱敏处理器
class DataSanitizer {
  constructor(options = {}) {
    this.options = {
      maskingChar: '*',
      preserveLength: true,
      preserveFormat: false,
      customSanitizers: {},
      ...options
    };
    
    this.sanitizers = this.initializeSanitizers();
  }
  
  initializeSanitizers() {
    return {
      email: (value) => {
        const [local, domain] = value.split('@');
        const maskedLocal = this.maskString(local, 1, 1);
        const [domainName, tld] = domain.split('.');
        const maskedDomain = this.maskString(domainName, 1, 1);
        return `${maskedLocal}@${maskedDomain}.${tld}`;
      },
      
      phone: (value) => {
        const cleaned = value.replace(/\D/g, '');
        if (cleaned.length === 10) {
          return `${cleaned.substring(0, 3)}-***-${cleaned.substring(7)}`;
        }
        return this.maskString(value, 3, 4);
      },
      
      creditCard: (value) => {
        const cleaned = value.replace(/\D/g, '');
        return `****-****-****-${cleaned.substring(cleaned.length - 4)}`;
      },
      
      ssn: (value) => {
        return '***-**-****';
      },
      
      ipAddress: (value) => {
        const parts = value.split('.');
        return `${parts[0]}.${parts[1]}.***.***.`;
      },
      
      password: () => '[REDACTED]',
      
      token: (value) => {
        if (value.length > 8) {
          return `${value.substring(0, 4)}...${value.substring(value.length - 4)}`;
        }
        return '[REDACTED]';
      },
      
      apiKey: () => '[REDACTED]',
      
      bankAccount: (value) => {
        return `****${value.substring(value.length - 4)}`;
      },
      
      routingNumber: () => '*********'
    };
  }
  
  sanitizeData(data, sensitiveFields) {
    if (!sensitiveFields || sensitiveFields.length === 0) {
      return data;
    }
    
    let sanitizedData = typeof data === 'string' ? data : JSON.stringify(data);
    
    // 按敏感级别排序，优先处理高敏感度数据
    const sortedFields = sensitiveFields.sort((a, b) => b.level - a.level);
    
    sortedFields.forEach(field => {
      const sanitizer = this.sanitizers[field.type] || this.defaultSanitizer;
      
      // 获取对应的正则表达式
      const pattern = this.getPatternForType(field.type);
      if (pattern) {
        sanitizedData = sanitizedData.replace(pattern, (match) => {
          return sanitizer(match);
        });
      }
    });
    
    // 尝试解析回原始格式
    try {
      return typeof data === 'string' ? sanitizedData : JSON.parse(sanitizedData);
    } catch (error) {
      return sanitizedData;
    }
  }
  
  sanitizeSentryEvent(event, sensitiveAnalysis) {
    const sanitizedEvent = JSON.parse(JSON.stringify(event));
    
    // 脱敏错误消息
    if (sensitiveAnalysis.message && sensitiveAnalysis.message.sensitiveFields.length > 0) {
      sanitizedEvent.message = this.sanitizeData(event.message, sensitiveAnalysis.message.sensitiveFields);
    }
    
    // 脱敏异常信息
    if (sensitiveAnalysis.exception && sanitizedEvent.exception) {
      sanitizedEvent.exception.values = sanitizedEvent.exception.values.map((exc, index) => {
        const analysis = sensitiveAnalysis.exception[index];
        if (analysis) {
          if (analysis.value && analysis.value.sensitiveFields.length > 0) {
            exc.value = this.sanitizeData(exc.value, analysis.value.sensitiveFields);
          }
          
          if (analysis.stacktrace && exc.stacktrace) {
            exc.stacktrace.frames = exc.stacktrace.frames.map((frame, frameIndex) => {
              const frameAnalysis = analysis.stacktrace[frameIndex];
              if (frameAnalysis) {
                if (frameAnalysis.vars && frameAnalysis.vars.sensitiveFields.length > 0) {
                  frame.vars = this.sanitizeData(frame.vars, frameAnalysis.vars.sensitiveFields);
                }
                if (frameAnalysis.context && frameAnalysis.context.sensitiveFields.length > 0) {
                  frame.context_line = this.sanitizeData(frame.context_line, frameAnalysis.context.sensitiveFields);
                }
              }
              return frame;
            });
          }
        }
        return exc;
      });
    }
    
    // 脱敏面包屑
    if (sensitiveAnalysis.breadcrumbs && sanitizedEvent.breadcrumbs) {
      sanitizedEvent.breadcrumbs.values = sanitizedEvent.breadcrumbs.values.map((breadcrumb, index) => {
        const analysis = sensitiveAnalysis.breadcrumbs[index];
        if (analysis) {
          if (analysis.message && analysis.message.sensitiveFields.length > 0) {
            breadcrumb.message = this.sanitizeData(breadcrumb.message, analysis.message.sensitiveFields);
          }
          if (analysis.data && analysis.data.sensitiveFields.length > 0) {
            breadcrumb.data = this.sanitizeData(breadcrumb.data, analysis.data.sensitiveFields);
          }
        }
        return breadcrumb;
      });
    }
    
    // 脱敏额外数据
    if (sensitiveAnalysis.extra && sensitiveAnalysis.extra.sensitiveFields.length > 0) {
      sanitizedEvent.extra = this.sanitizeData(sanitizedEvent.extra, sensitiveAnalysis.extra.sensitiveFields);
    }
    
    // 脱敏用户数据
    if (sensitiveAnalysis.user && sensitiveAnalysis.user.sensitiveFields.length > 0) {
      sanitizedEvent.user = this.sanitizeData(sanitizedEvent.user, sensitiveAnalysis.user.sensitiveFields);
    }
    
    // 脱敏标签
    if (sensitiveAnalysis.tags && sensitiveAnalysis.tags.sensitiveFields.length > 0) {
      sanitizedEvent.tags = this.sanitizeData(sanitizedEvent.tags, sensitiveAnalysis.tags.sensitiveFields);
    }
    
    return sanitizedEvent;
  }
  
  maskString(str, preserveStart = 0, preserveEnd = 0) {
    if (str.length <= preserveStart + preserveEnd) {
      return this.options.maskingChar.repeat(str.length);
    }
    
    const start = str.substring(0, preserveStart);
    const end = str.substring(str.length - preserveEnd);
    const middle = this.options.preserveLength 
      ? this.options.maskingChar.repeat(str.length - preserveStart - preserveEnd)
      : this.options.maskingChar.repeat(3);
    
    return start + middle + end;
  }
  
  defaultSanitizer = (value) => {
    return this.maskString(value, 2, 2);
  }
  
  getPatternForType(type) {
    // 这里需要从分类器中获取对应的正则表达式
    const patternMap = {
      email: /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/g,
      phone: /\b(?:\+?1[-.]?)?\(?([0-9]{3})\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\b/g,
      creditCard: /\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3[0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\b/g,
      ssn: /\b\d{3}-?\d{2}-?\d{4}\b/g,
      ipAddress: /\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b/g,
      password: /(?:password|pwd|pass)\s*[:=]\s*["']?([^\s"']+)["']?/gi,
      token: /(?:token|jwt|bearer)\s*[:=]\s*["']?([A-Za-z0-9\-_\.]+)["']?/gi,
      apiKey: /(?:api[_-]?key|apikey)\s*[:=]\s*["']?([A-Za-z0-9\-_]+)["']?/gi
    };
    
    return patternMap[type];
  }
}
```

## 2. Sentry安全配置

### 2.1 安全的Sentry初始化

```javascript
// 安全的Sentry配置管理器
class SecureSentryConfig {
  constructor(options = {}) {
    this.options = {
      environment: process.env.NODE_ENV || 'development',
      enableDataScrubbing: true,
      enablePrivacyMode: true,
      allowedDomains: [],
      blockedUrls: [],
      sensitiveKeys: [],
      ...options
    };
    
    this.classifier = new SensitiveDataClassifier();
    this.sanitizer = new DataSanitizer();
  }
  
  createSecureConfig() {
    const baseConfig = {
      dsn: this.getDSN(),
      environment: this.options.environment,
      
      // 基础安全设置
      beforeSend: this.createBeforeSendHook(),
      beforeBreadcrumb: this.createBeforeBreadcrumbHook(),
      
      // 数据清理设置
      sendDefaultPii: false, // 禁用默认PII发送
      
      // 采样配置
      sampleRate: this.getSampleRate(),
      tracesSampleRate: this.getTracesSampleRate(),
      
      // 传输安全
      transport: this.createSecureTransport(),
      
      // 集成配置
      integrations: this.createSecureIntegrations(),
      
      // 发布信息
      release: this.getRelease(),
      
      // 用户上下文过滤
      initialScope: {
        user: this.getFilteredUserContext(),
        tags: this.getSecureTags()
      }
    };
    
    return baseConfig;
  }
  
  createBeforeSendHook() {
    return (event, hint) => {
      try {
        // 1. 检查是否应该发送事件
        if (!this.shouldSendEvent(event)) {
          return null;
        }
        
        // 2. 分析敏感数据
        const sensitiveAnalysis = this.classifier.checkSentryEvent(event);
        
        // 3. 如果包含高敏感度数据，根据配置决定处理方式
        if (this.containsCriticalData(sensitiveAnalysis)) {
          if (this.options.enablePrivacyMode) {
            // 脱敏处理
            event = this.sanitizer.sanitizeSentryEvent(event, sensitiveAnalysis);
          } else {
            // 直接丢弃
            console.warn('Event contains critical sensitive data and was dropped');
            return null;
          }
        }
        
        // 4. 通用数据清理
        event = this.cleanEventData(event);
        
        // 5. 添加安全标记
        event = this.addSecurityMetadata(event, sensitiveAnalysis);
        
        // 6. 最终验证
        if (!this.finalSecurityCheck(event)) {
          return null;
        }
        
        return event;
      } catch (error) {
        console.error('Error in beforeSend hook:', error);
        // 发生错误时，为了安全起见，不发送事件
        return null;
      }
    };
  }
  
  createBeforeBreadcrumbHook() {
    return (breadcrumb, hint) => {
      try {
        // 检查面包屑类型
        if (this.shouldFilterBreadcrumb(breadcrumb)) {
          return null;
        }
        
        // 清理面包屑数据
        breadcrumb = this.cleanBreadcrumbData(breadcrumb);
        
        // 检查敏感信息
        if (breadcrumb.message) {
          const analysis = this.classifier.classifyData(breadcrumb.message);
          if (analysis.overallSensitivity >= this.classifier.sensitivityLevels.HIGH) {
            breadcrumb.message = this.sanitizer.sanitizeData(breadcrumb.message, analysis.sensitiveFields);
          }
        }
        
        if (breadcrumb.data) {
          const analysis = this.classifier.classifyData(JSON.stringify(breadcrumb.data));
          if (analysis.overallSensitivity >= this.classifier.sensitivityLevels.HIGH) {
            breadcrumb.data = this.sanitizer.sanitizeData(breadcrumb.data, analysis.sensitiveFields);
          }
        }
        
        return breadcrumb;
      } catch (error) {
        console.error('Error in beforeBreadcrumb hook:', error);
        return null;
      }
    };
  }
  
  shouldSendEvent(event) {
    // 检查环境
    if (this.options.environment === 'development' && !this.options.enableInDevelopment) {
      return false;
    }
    
    // 检查URL过滤
    if (event.request && event.request.url) {
      if (this.isBlockedUrl(event.request.url)) {
        return false;
      }
    }
    
    // 检查错误类型过滤
    if (this.isFilteredError(event)) {
      return false;
    }
    
    return true;
  }
  
  containsCriticalData(sensitiveAnalysis) {
    const checkCritical = (analysis) => {
      return analysis && analysis.sensitiveFields && 
             analysis.sensitiveFields.some(field => 
               field.level >= this.classifier.sensitivityLevels.CRITICAL
             );
    };
    
    return checkCritical(sensitiveAnalysis.message) ||
           checkCritical(sensitiveAnalysis.extra) ||
           checkCritical(sensitiveAnalysis.user) ||
           (sensitiveAnalysis.exception && sensitiveAnalysis.exception.some(exc => 
             checkCritical(exc.value) || 
             (exc.stacktrace && exc.stacktrace.some(frame => 
               checkCritical(frame.vars) || checkCritical(frame.context)
             ))
           )) ||
           (sensitiveAnalysis.breadcrumbs && sensitiveAnalysis.breadcrumbs.some(bc => 
             checkCritical(bc.message) || checkCritical(bc.data)
           ));
  }
  
  cleanEventData(event) {
    // 移除敏感的请求头
    if (event.request && event.request.headers) {
      const sensitiveHeaders = ['authorization', 'cookie', 'x-api-key', 'x-auth-token'];
      sensitiveHeaders.forEach(header => {
        delete event.request.headers[header];
      });
    }
    
    // 清理查询参数
    if (event.request && event.request.query_string) {
      event.request.query_string = this.cleanQueryString(event.request.query_string);
    }
    
    // 移除敏感的环境变量
    if (event.contexts && event.contexts.runtime && event.contexts.runtime.env) {
      event.contexts.runtime.env = this.filterEnvironmentVariables(event.contexts.runtime.env);
    }
    
    return event;
  }
  
  cleanQueryString(queryString) {
    const sensitiveParams = ['password', 'token', 'api_key', 'secret', 'auth'];
    
    return queryString.split('&').map(param => {
      const [key, value] = param.split('=');
      if (sensitiveParams.some(sensitive => key.toLowerCase().includes(sensitive))) {
        return `${key}=[REDACTED]`;
      }
      return param;
    }).join('&');
  }
  
  filterEnvironmentVariables(env) {
    const filtered = {};
    const sensitiveKeys = ['password', 'secret', 'key', 'token', 'auth'];
    
    Object.keys(env).forEach(key => {
      if (sensitiveKeys.some(sensitive => key.toLowerCase().includes(sensitive))) {
        filtered[key] = '[REDACTED]';
      } else {
        filtered[key] = env[key];
      }
    });
    
    return filtered;
  }
  
  addSecurityMetadata(event, sensitiveAnalysis) {
    if (!event.tags) {
      event.tags = {};
    }
    
    // 添加数据处理标记
    if (this.options.enableDataScrubbing) {
      event.tags['data_scrubbed'] = 'true';
    }
    
    // 添加敏感度级别
    const maxSensitivity = this.getMaxSensitivityLevel(sensitiveAnalysis);
    if (maxSensitivity > this.classifier.sensitivityLevels.LOW) {
      event.tags['sensitivity_level'] = this.getSensitivityLevelName(maxSensitivity);
    }
    
    // 添加隐私模式标记
    if (this.options.enablePrivacyMode) {
      event.tags['privacy_mode'] = 'enabled';
    }
    
    return event;
  }
  
  getMaxSensitivityLevel(sensitiveAnalysis) {
    let maxLevel = this.classifier.sensitivityLevels.LOW;
    
    const checkLevel = (analysis) => {
      if (analysis && analysis.overallSensitivity) {
        maxLevel = Math.max(maxLevel, analysis.overallSensitivity);
      }
    };
    
    checkLevel(sensitiveAnalysis.message);
    checkLevel(sensitiveAnalysis.extra);
    checkLevel(sensitiveAnalysis.user);
    
    if (sensitiveAnalysis.exception) {
      sensitiveAnalysis.exception.forEach(exc => {
        checkLevel(exc.value);
        if (exc.stacktrace) {
          exc.stacktrace.forEach(frame => {
            checkLevel(frame.vars);
            checkLevel(frame.context);
          });
        }
      });
    }
    
    if (sensitiveAnalysis.breadcrumbs) {
      sensitiveAnalysis.breadcrumbs.forEach(bc => {
        checkLevel(bc.message);
        checkLevel(bc.data);
      });
    }
    
    return maxLevel;
  }
  
  getSensitivityLevelName(level) {
    const names = {
      [this.classifier.sensitivityLevels.LOW]: 'low',
      [this.classifier.sensitivityLevels.MEDIUM]: 'medium',
      [this.classifier.sensitivityLevels.HIGH]: 'high',
      [this.classifier.sensitivityLevels.CRITICAL]: 'critical'
    };
    return names[level] || 'unknown';
  }
  
  finalSecurityCheck(event) {
    // 最终的安全检查
    const eventString = JSON.stringify(event);
    
    // 检查是否仍包含明显的敏感信息
    const criticalPatterns = [
      /password\s*[:=]\s*["']?[^\s"'\]]+/gi,
      /api[_-]?key\s*[:=]\s*["']?[^\s"'\]]+/gi,
      /\b\d{4}[-\s]?\d{4}[-\s]?\d{4}[-\s]?\d{4}\b/g // 信用卡号
    ];
    
    for (const pattern of criticalPatterns) {
      if (pattern.test(eventString)) {
        console.warn('Event failed final security check and was dropped');
        return false;
      }
    }
    
    return true;
  }
  
  // 辅助方法
  getDSN() {
    // 从环境变量或配置中获取DSN
    return process.env.SENTRY_DSN || this.options.dsn;
  }
  
  getSampleRate() {
    // 根据环境调整采样率
    if (this.options.environment === 'production') {
      return 0.1; // 生产环境10%采样
    }
    return 1.0; // 开发环境100%采样
  }
  
  getTracesSampleRate() {
    if (this.options.environment === 'production') {
      return 0.01; // 生产环境1%性能追踪
    }
    return 0.1; // 开发环境10%性能追踪
  }
  
  isBlockedUrl(url) {
    return this.options.blockedUrls.some(blocked => url.includes(blocked));
  }
  
  isFilteredError(event) {
    // 过滤掉一些常见的非关键错误
    const filteredErrors = [
      'Script error',
      'Network Error',
      'Non-Error promise rejection captured'
    ];
    
    if (event.message && filteredErrors.includes(event.message)) {
      return true;
    }
    
    return false;
  }
  
  shouldFilterBreadcrumb(breadcrumb) {
    // 过滤敏感的面包屑类型
    const filteredCategories = ['auth', 'navigation'];
    
    if (breadcrumb.category && filteredCategories.includes(breadcrumb.category)) {
      return true;
    }
    
    return false;
  }
  
  cleanBreadcrumbData(breadcrumb) {
    if (breadcrumb.data) {
      // 移除敏感的数据字段
      const sensitiveFields = ['password', 'token', 'authorization'];
      sensitiveFields.forEach(field => {
        if (breadcrumb.data[field]) {
          breadcrumb.data[field] = '[REDACTED]';
        }
      });
    }
    
    return breadcrumb;
  }
  
  getFilteredUserContext() {
    // 返回过滤后的用户上下文
    return {
      id: null, // 不发送用户ID
      email: null, // 不发送邮箱
      username: null // 不发送用户名
    };
  }
  
  getSecureTags() {
    return {
      security_enabled: 'true',
      privacy_compliant: 'true'
    };
  }
  
  createSecureTransport() {
    // 创建安全的传输配置
    return undefined; // 使用默认的HTTPS传输
  }
  
  createSecureIntegrations() {
    // 配置安全的集成
    return [
      // 禁用一些可能泄露敏感信息的集成
    ];
  }
  
  getRelease() {
    return process.env.SENTRY_RELEASE || 'unknown';
  }
}

// 使用示例
const secureConfig = new SecureSentryConfig({
  environment: 'production',
  enableDataScrubbing: true,
  enablePrivacyMode: true,
  blockedUrls: ['/admin', '/api/internal'],
  enableInDevelopment: false
});

// 初始化Sentry
Sentry.init(secureConfig.createSecureConfig());
```

## 3. 合规性管理

### 3.1 GDPR合规实现

```javascript
// GDPR合规管理器
class GDPRComplianceManager {
  constructor(options = {}) {
    this.options = {
      consentRequired: true,
      dataRetentionDays: 30,
      enableRightToBeDeleted: true,
      enableDataPortability: true,
      enableRightToRectification: true,
      ...options
    };
    
    this.consentStorage = new ConsentStorage();
    this.dataProcessor = new PersonalDataProcessor();
  }
  
  // 检查用户同意状态
  checkConsent(userId) {
    const consent = this.consentStorage.getConsent(userId);
    
    if (!consent) {
      return {
        hasConsent: false,
        reason: 'No consent record found'
      };
    }
    
    if (this.isConsentExpired(consent)) {
      return {
        hasConsent: false,
        reason: 'Consent has expired'
      };
    }
    
    if (!consent.monitoring) {
      return {
        hasConsent: false,
        reason: 'Monitoring consent not granted'
      };
    }
    
    return {
      hasConsent: true,
      consent: consent
    };
  }
  
  // 请求用户同意
  async requestConsent(userId, purposes = []) {
    const consentRequest = {
      userId,
      timestamp: new Date().toISOString(),
      purposes,
      version: '1.0',
      language: navigator.language || 'en'
    };
    
    // 显示同意对话框
    const userResponse = await this.showConsentDialog(consentRequest);
    
    if (userResponse.granted) {
      // 保存同意记录
      const consent = {
        userId,
        granted: true,
        timestamp: new Date().toISOString(),
        purposes: userResponse.purposes,
        version: consentRequest.version,
        ipAddress: await this.getClientIP(),
        userAgent: navigator.userAgent,
        expiresAt: this.calculateExpirationDate()
      };
      
      this.consentStorage.saveConsent(consent);
      
      // 启用监控
      this.enableMonitoring(userId, consent);
      
      return { success: true, consent };
    } else {
      // 记录拒绝
      this.consentStorage.saveConsentRefusal({
        userId,
        timestamp: new Date().toISOString(),
        reason: userResponse.reason
      });
      
      return { success: false, reason: 'User declined consent' };
    }
  }
  
  // 撤销同意
  async revokeConsent(userId, reason = 'User request') {
    const consent = this.consentStorage.getConsent(userId);
    
    if (consent) {
      // 更新同意记录
      consent.revoked = true;
      consent.revokedAt = new Date().toISOString();
      consent.revocationReason = reason;
      
      this.consentStorage.updateConsent(consent);
      
      // 停止数据收集
      this.disableMonitoring(userId);
      
      // 如果启用了被遗忘权，删除现有数据
      if (this.options.enableRightToBeDeleted) {
        await this.deleteUserData(userId);
      }
      
      return { success: true };
    }
    
    return { success: false, reason: 'No consent record found' };
  }
  
  // 数据主体权利：访问权
  async handleDataAccessRequest(userId) {
    const userData = await this.dataProcessor.getUserData(userId);
    
    const accessResponse = {
      userId,
      requestDate: new Date().toISOString(),
      data: {
        personalInfo: userData.personalInfo,
        monitoringData: userData.monitoringData,
        consentHistory: this.consentStorage.getConsentHistory(userId)
      },
      dataCategories: this.getDataCategories(userData),
      processingPurposes: this.getProcessingPurposes(),
      dataRetentionPeriod: `${this.options.dataRetentionDays} days`,
      thirdPartySharing: this.getThirdPartySharing()
    };
    
    return accessResponse;
  }
  
  // 数据主体权利：更正权
  async handleDataRectificationRequest(userId, corrections) {
    const validationResult = this.validateCorrections(corrections);
    
    if (!validationResult.valid) {
      return {
        success: false,
        errors: validationResult.errors
      };
    }
    
    // 应用更正
    const updateResult = await this.dataProcessor.updateUserData(userId, corrections);
    
    // 记录更正历史
    this.dataProcessor.recordRectification({
      userId,
      timestamp: new Date().toISOString(),
      corrections,
      requestedBy: 'user'
    });
    
    return {
      success: true,
      updatedFields: Object.keys(corrections),
      timestamp: new Date().toISOString()
    };
  }
  
  // 数据主体权利：删除权（被遗忘权）
  async handleDataDeletionRequest(userId, reason = 'User request') {
    // 检查是否可以删除
    const deletionCheck = this.canDeleteUserData(userId);
    
    if (!deletionCheck.canDelete) {
      return {
        success: false,
        reason: deletionCheck.reason
      };
    }
    
    // 执行删除
    const deletionResult = await this.deleteUserData(userId);
    
    // 记录删除操作
    this.dataProcessor.recordDeletion({
      userId,
      timestamp: new Date().toISOString(),
      reason,
      deletedData: deletionResult.deletedData
    });
    
    return {
      success: true,
      deletedData: deletionResult.deletedData,
      timestamp: new Date().toISOString()
    };
  }
  
  // 数据主体权利：数据可携带权
  async handleDataPortabilityRequest(userId, format = 'json') {
    const userData = await this.dataProcessor.getUserData(userId);
    
    const portableData = {
      exportDate: new Date().toISOString(),
      userId,
      format,
      data: this.formatDataForPortability(userData, format)
    };
    
    // 生成导出文件
    const exportFile = await this.generateExportFile(portableData, format);
    
    return {
      success: true,
      downloadUrl: exportFile.url,
      expiresAt: exportFile.expiresAt,
      format
    };
  }
  
  // 辅助方法
  isConsentExpired(consent) {
    if (!consent.expiresAt) return false;
    return new Date(consent.expiresAt) < new Date();
  }
  
  calculateExpirationDate() {
    const expirationDate = new Date();
    expirationDate.setFullYear(expirationDate.getFullYear() + 1); // 1年后过期
    return expirationDate.toISOString();
  }
  
  async showConsentDialog(consentRequest) {
    return new Promise((resolve) => {
      const dialog = this.createConsentDialog(consentRequest);
      
      dialog.onAccept = (purposes) => {
        resolve({
          granted: true,
          purposes,
          timestamp: new Date().toISOString()
        });
      };
      
      dialog.onDecline = (reason) => {
        resolve({
          granted: false,
          reason,
          timestamp: new Date().toISOString()
        });
      };
      
      dialog.show();
    });
  }
  
  createConsentDialog(consentRequest) {
    // 创建GDPR合规的同意对话框
    const dialog = document.createElement('div');
    dialog.className = 'gdpr-consent-dialog';
    dialog.innerHTML = `
      <div class="consent-overlay">
        <div class="consent-modal">
          <h2>数据处理同意</h2>
          <p>我们需要您的同意来收集和处理您的数据，以便为您提供错误监控和性能分析服务。</p>
          
          <div class="consent-purposes">
            <h3>数据处理目的：</h3>
            <ul>
              <li><input type="checkbox" id="monitoring" checked> 错误监控和性能分析</li>
              <li><input type="checkbox" id="analytics"> 使用情况分析</li>
              <li><input type="checkbox" id="improvement"> 产品改进</li>
            </ul>
          </div>
          
          <div class="consent-info">
            <h3>您的权利：</h3>
            <ul>
              <li>您可以随时撤销同意</li>
              <li>您有权访问、更正或删除您的数据</li>
              <li>您有权将数据导出到其他服务</li>
            </ul>
          </div>
          
          <div class="consent-actions">
            <button class="btn-accept">接受</button>
            <button class="btn-decline">拒绝</button>
            <button class="btn-customize">自定义设置</button>
          </div>
        </div>
      </div>
    `;
    
    // 添加样式
    const style = document.createElement('style');
    style.textContent = `
      .gdpr-consent-dialog {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 10000;
      }
      
      .consent-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      .consent-modal {
        background: white;
        padding: 2rem;
        border-radius: 8px;
        max-width: 500px;
        max-height: 80vh;
        overflow-y: auto;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      }
      
      .consent-purposes, .consent-info {
        margin: 1rem 0;
      }
      
      .consent-purposes ul, .consent-info ul {
        list-style: none;
        padding: 0;
      }
      
      .consent-purposes li {
        margin: 0.5rem 0;
        display: flex;
        align-items: center;
      }
      
      .consent-purposes input {
        margin-right: 0.5rem;
      }
      
      .consent-actions {
        display: flex;
        gap: 1rem;
        margin-top: 2rem;
      }
      
      .consent-actions button {
        padding: 0.5rem 1rem;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 1rem;
      }
      
      .btn-accept {
        background: #28a745;
        color: white;
      }
      
      .btn-decline {
        background: #dc3545;
        color: white;
      }
      
      .btn-customize {
        background: #6c757d;
        color: white;
      }
    `;
    
    document.head.appendChild(style);
    document.body.appendChild(dialog);
    
    // 事件处理
    const acceptBtn = dialog.querySelector('.btn-accept');
    const declineBtn = dialog.querySelector('.btn-decline');
    const customizeBtn = dialog.querySelector('.btn-customize');
    
    const cleanup = () => {
      document.body.removeChild(dialog);
      document.head.removeChild(style);
    };
    
    return {
      show: () => {
        dialog.style.display = 'block';
      },
      
      onAccept: null,
      onDecline: null,
      
      init: function() {
        acceptBtn.addEventListener('click', () => {
          const purposes = [];
          dialog.querySelectorAll('input[type="checkbox"]:checked').forEach(checkbox => {
            purposes.push(checkbox.id);
          });
          cleanup();
          if (this.onAccept) this.onAccept(purposes);
        });
        
        declineBtn.addEventListener('click', () => {
          cleanup();
          if (this.onDecline) this.onDecline('User declined');
        });
        
        customizeBtn.addEventListener('click', () => {
          // 显示自定义设置界面
          this.showCustomizeDialog();
        });
      }
    };
  }
  
  async getClientIP() {
    try {
      const response = await fetch('https://api.ipify.org?format=json');
      const data = await response.json();
      return data.ip;
    } catch (error) {
      return 'unknown';
    }
  }
  
  enableMonitoring(userId, consent) {
    // 启用Sentry监控
    Sentry.setUser({
      id: userId,
      consent: {
        granted: true,
        purposes: consent.purposes,
        timestamp: consent.timestamp
      }
    });
    
    // 设置标签
    Sentry.setTag('gdpr_compliant', 'true');
    Sentry.setTag('consent_version', consent.version);
  }
  
  disableMonitoring(userId) {
    // 清除用户信息
    Sentry.setUser(null);
    
    // 停止发送事件
    Sentry.getClient().getOptions().beforeSend = () => null;
  }
  
  async deleteUserData(userId) {
    // 这里需要调用Sentry API删除用户相关数据
    // 注意：Sentry可能不支持完全删除，需要联系Sentry支持
    
    const deletedData = {
      sentryEvents: 'marked_for_deletion',
      localData: 'deleted',
      consentRecords: 'anonymized'
    };
    
    // 删除本地存储的用户数据
    this.consentStorage.deleteUserData(userId);
    this.dataProcessor.deleteUserData(userId);
    
    return { deletedData };
  }
  
  canDeleteUserData(userId) {
    // 检查是否有法律义务保留数据
    const legalHolds = this.dataProcessor.getLegalHolds(userId);
    
    if (legalHolds.length > 0) {
      return {
        canDelete: false,
        reason: 'Data subject to legal hold'
      };
    }
    
    return { canDelete: true };
  }
  
  validateCorrections(corrections) {
    const errors = [];
    
    // 验证数据格式
    Object.entries(corrections).forEach(([field, value]) => {
      if (!this.isValidFieldValue(field, value)) {
        errors.push(`Invalid value for field: ${field}`);
      }
    });
    
    return {
      valid: errors.length === 0,
      errors
    };
  }
  
  isValidFieldValue(field, value) {
    // 实现字段验证逻辑
    const validators = {
      email: (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v),
      phone: (v) => /^\+?[1-9]\d{1,14}$/.test(v),
      name: (v) => typeof v === 'string' && v.length > 0
    };
    
    const validator = validators[field];
    return validator ? validator(value) : true;
  }
  
  getDataCategories(userData) {
    return [
      'Error and performance data',
      'User interaction data',
      'Technical information',
      'Consent records'
    ];
  }
  
  getProcessingPurposes() {
    return [
      'Error monitoring and debugging',
      'Performance analysis',
      'User experience improvement',
      'Security monitoring'
    ];
  }
  
  getThirdPartySharing() {
    return {
      sentry: {
        purpose: 'Error monitoring service',
        dataTypes: ['Error data', 'Performance metrics'],
        location: 'United States',
        safeguards: 'Standard Contractual Clauses'
      }
    };
  }
  
  formatDataForPortability(userData, format) {
    switch (format) {
      case 'json':
        return JSON.stringify(userData, null, 2);
      case 'csv':
        return this.convertToCSV(userData);
      case 'xml':
        return this.convertToXML(userData);
      default:
        return userData;
    }
  }
  
  async generateExportFile(portableData, format) {
    // 生成可下载的文件
    const blob = new Blob([portableData.data], {
      type: this.getMimeType(format)
    });
    
    const url = URL.createObjectURL(blob);
    const expiresAt = new Date();
    expiresAt.setHours(expiresAt.getHours() + 24); // 24小时后过期
    
    return {
      url,
      expiresAt: expiresAt.toISOString()
    };
  }
  
  getMimeType(format) {
    const mimeTypes = {
      json: 'application/json',
      csv: 'text/csv',
      xml: 'application/xml'
    };
    return mimeTypes[format] || 'text/plain';
  }
  
  convertToCSV(data) {
    // 简单的CSV转换实现
    const flatten = (obj, prefix = '') => {
      const flattened = {};
      Object.keys(obj).forEach(key => {
        const value = obj[key];
        const newKey = prefix ? `${prefix}.${key}` : key;
        
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
          Object.assign(flattened, flatten(value, newKey));
        } else {
          flattened[newKey] = value;
        }
      });
      return flattened;
    };
    
    const flattened = flatten(data);
    const headers = Object.keys(flattened).join(',');
    const values = Object.values(flattened).map(v => 
      typeof v === 'string' ? `"${v.replace(/"/g, '""')}"` : v
    ).join(',');
    
    return `${headers}\n${values}`;
  }
  
  convertToXML(data) {
    // 简单的XML转换实现
    const toXML = (obj, rootName = 'data') => {
      let xml = `<${rootName}>`;
      
      Object.entries(obj).forEach(([key, value]) => {
        if (typeof value === 'object' && value !== null) {
          xml += toXML(value, key);
        } else {
          xml += `<${key}>${value}</${key}>`;
        }
      });
      
      xml += `</${rootName}>`;
      return xml;
    };
    
    return `<?xml version="1.0" encoding="UTF-8"?>${toXML(data)}`;
  }
}
```

### 3.2 同意存储管理

```javascript
// 同意存储管理器
class ConsentStorage {
  constructor(options = {}) {
    this.options = {
      storageType: 'localStorage', // localStorage, sessionStorage, indexedDB
      encryptData: true,
      keyPrefix: 'gdpr_consent_',
      ...options
    };
    
    this.storage = this.initializeStorage();
    this.encryption = new DataEncryption();
  }
  
  initializeStorage() {
    switch (this.options.storageType) {
      case 'localStorage':
        return new LocalStorageAdapter();
      case 'sessionStorage':
        return new SessionStorageAdapter();
      case 'indexedDB':
        return new IndexedDBAdapter();
      default:
        return new LocalStorageAdapter();
    }
  }
  
  saveConsent(consent) {
    const key = this.getConsentKey(consent.userId);
    const data = this.options.encryptData 
      ? this.encryption.encrypt(JSON.stringify(consent))
      : JSON.stringify(consent);
    
    this.storage.setItem(key, data);
    
    // 同时保存到历史记录
    this.saveConsentHistory(consent);
  }
  
  getConsent(userId) {
    const key = this.getConsentKey(userId);
    const data = this.storage.getItem(key);
    
    if (!data) return null;
    
    try {
      const decrypted = this.options.encryptData 
        ? this.encryption.decrypt(data)
        : data;
      
      return JSON.parse(decrypted);
    } catch (error) {
      console.error('Error parsing consent data:', error);
      return null;
    }
  }
  
  updateConsent(consent) {
    this.saveConsent(consent);
  }
  
  deleteConsent(userId) {
    const key = this.getConsentKey(userId);
    this.storage.removeItem(key);
  }
  
  saveConsentHistory(consent) {
    const historyKey = this.getConsentHistoryKey(consent.userId);
    const history = this.getConsentHistory(consent.userId) || [];
    
    history.push({
      ...consent,
      historyTimestamp: new Date().toISOString()
    });
    
    // 保留最近100条记录
    if (history.length > 100) {
      history.splice(0, history.length - 100);
    }
    
    const data = this.options.encryptData 
      ? this.encryption.encrypt(JSON.stringify(history))
      : JSON.stringify(history);
    
    this.storage.setItem(historyKey, data);
  }
  
  getConsentHistory(userId) {
    const historyKey = this.getConsentHistoryKey(userId);
    const data = this.storage.getItem(historyKey);
    
    if (!data) return [];
    
    try {
      const decrypted = this.options.encryptData 
        ? this.encryption.decrypt(data)
        : data;
      
      return JSON.parse(decrypted);
    } catch (error) {
      console.error('Error parsing consent history:', error);
      return [];
    }
  }
  
  saveConsentRefusal(refusal) {
    const key = this.getRefusalKey(refusal.userId);
    const data = this.options.encryptData 
      ? this.encryption.encrypt(JSON.stringify(refusal))
      : JSON.stringify(refusal);
    
    this.storage.setItem(key, data);
  }
  
  getConsentRefusal(userId) {
    const key = this.getRefusalKey(userId);
    const data = this.storage.getItem(key);
    
    if (!data) return null;
    
    try {
      const decrypted = this.options.encryptData 
        ? this.encryption.decrypt(data)
        : data;
      
      return JSON.parse(decrypted);
    } catch (error) {
      console.error('Error parsing refusal data:', error);
      return null;
    }
  }
  
  deleteUserData(userId) {
    this.deleteConsent(userId);
    this.storage.removeItem(this.getConsentHistoryKey(userId));
    this.storage.removeItem(this.getRefusalKey(userId));
  }
  
  // 清理过期的同意记录
  cleanupExpiredConsents() {
    const allKeys = this.storage.getAllKeys();
    const consentKeys = allKeys.filter(key => key.startsWith(this.options.keyPrefix));
    
    consentKeys.forEach(key => {
      const data = this.storage.getItem(key);
      if (data) {
        try {
          const decrypted = this.options.encryptData 
            ? this.encryption.decrypt(data)
            : data;
          
          const consent = JSON.parse(decrypted);
          
          if (consent.expiresAt && new Date(consent.expiresAt) < new Date()) {
            this.storage.removeItem(key);
          }
        } catch (error) {
          // 如果解析失败，删除损坏的数据
          this.storage.removeItem(key);
        }
      }
    });
  }
  
  // 获取所有用户的同意状态统计
  getConsentStatistics() {
    const allKeys = this.storage.getAllKeys();
    const consentKeys = allKeys.filter(key => 
      key.startsWith(this.options.keyPrefix) && 
      !key.includes('_history') && 
      !key.includes('_refusal')
    );
    
    const stats = {
      total: 0,
      granted: 0,
      revoked: 0,
      expired: 0,
      byPurpose: {}
    };
    
    consentKeys.forEach(key => {
      const data = this.storage.getItem(key);
      if (data) {
        try {
          const decrypted = this.options.encryptData 
            ? this.encryption.decrypt(data)
            : data;
          
          const consent = JSON.parse(decrypted);
          stats.total++;
          
          if (consent.revoked) {
            stats.revoked++;
          } else if (consent.expiresAt && new Date(consent.expiresAt) < new Date()) {
            stats.expired++;
          } else {
            stats.granted++;
            
            // 统计各目的的同意情况
            consent.purposes.forEach(purpose => {
              stats.byPurpose[purpose] = (stats.byPurpose[purpose] || 0) + 1;
            });
          }
        } catch (error) {
          // 忽略解析错误
        }
      }
    });
    
    return stats;
  }
  
  getConsentKey(userId) {
    return `${this.options.keyPrefix}${userId}`;
  }
  
  getConsentHistoryKey(userId) {
    return `${this.options.keyPrefix}${userId}_history`;
  }
  
  getRefusalKey(userId) {
    return `${this.options.keyPrefix}${userId}_refusal`;
  }
}

// 存储适配器
class LocalStorageAdapter {
  setItem(key, value) {
    localStorage.setItem(key, value);
  }
  
  getItem(key) {
    return localStorage.getItem(key);
  }
  
  removeItem(key) {
    localStorage.removeItem(key);
  }
  
  getAllKeys() {
    return Object.keys(localStorage);
  }
}

class SessionStorageAdapter {
  setItem(key, value) {
    sessionStorage.setItem(key, value);
  }
  
  getItem(key) {
    return sessionStorage.getItem(key);
  }
  
  removeItem(key) {
    sessionStorage.removeItem(key);
  }
  
  getAllKeys() {
    return Object.keys(sessionStorage);
  }
}

class IndexedDBAdapter {
  constructor() {
    this.dbName = 'GDPRConsentDB';
    this.version = 1;
    this.storeName = 'consents';
    this.db = null;
    this.initDB();
  }
  
  async initDB() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.version);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve(this.db);
      };
      
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains(this.storeName)) {
          db.createObjectStore(this.storeName, { keyPath: 'key' });
        }
      };
    });
  }
  
  async setItem(key, value) {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readwrite');
      const store = transaction.objectStore(this.storeName);
      const request = store.put({ key, value });
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve();
    });
  }
  
  async getItem(key) {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readonly');
      const store = transaction.objectStore(this.storeName);
      const request = store.get(key);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        const result = request.result;
        resolve(result ? result.value : null);
      };
    });
  }
  
  async removeItem(key) {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readwrite');
      const store = transaction.objectStore(this.storeName);
      const request = store.delete(key);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve();
    });
  }
  
  async getAllKeys() {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readonly');
      const store = transaction.objectStore(this.storeName);
      const request = store.getAllKeys();
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve(request.result);
    });
  }
}
```
```