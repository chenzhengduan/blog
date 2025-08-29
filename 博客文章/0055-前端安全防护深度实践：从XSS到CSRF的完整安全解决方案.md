# 前端安全防护深度实践：从XSS到CSRF的完整安全解决方案

> 在数字化时代，前端安全已成为Web应用的生命线。本文将深入探讨现代前端安全防护体系，从XSS、CSRF等经典攻击到现代安全威胁，提供完整的防护解决方案和最佳实践。

## 1. 前端安全概述

### 1.1 安全威胁分析

前端安全面临的主要威胁包括：
- **XSS攻击**：跨站脚本攻击，注入恶意脚本
- **CSRF攻击**：跨站请求伪造，利用用户身份执行恶意操作
- **点击劫持**：通过透明iframe诱导用户点击
- **数据泄露**：敏感信息暴露和传输安全
- **依赖安全**：第三方库和组件的安全漏洞
- **内容安全**：恶意内容注入和篡改

### 1.2 安全防护架构

```javascript
// 前端安全管理器
class FrontendSecurityManager {
  constructor(config = {}) {
    this.config = {
      enableXSSProtection: true,
      enableCSRFProtection: true,
      enableClickjackingProtection: true,
      enableContentSecurityPolicy: true,
      enableInputValidation: true,
      enableSecureStorage: true,
      enableSecureCommunication: true,
      enableDependencyScanning: true,
      ...config
    };
    
    this.protectionModules = new Map();
    this.securityEvents = [];
    this.threatDetectors = new Map();
    this.securityPolicies = new Map();
    
    this.init();
  }
  
  // 初始化安全模块
  init() {
    if (this.config.enableXSSProtection) {
      this.protectionModules.set('xss', new XSSProtection());
    }
    
    if (this.config.enableCSRFProtection) {
      this.protectionModules.set('csrf', new CSRFProtection());
    }
    
    if (this.config.enableClickjackingProtection) {
      this.protectionModules.set('clickjacking', new ClickjackingProtection());
    }
    
    if (this.config.enableContentSecurityPolicy) {
      this.protectionModules.set('csp', new ContentSecurityPolicy());
    }
    
    if (this.config.enableInputValidation) {
      this.protectionModules.set('input', new InputValidation());
    }
    
    if (this.config.enableSecureStorage) {
      this.protectionModules.set('storage', new SecureStorage());
    }
    
    if (this.config.enableSecureCommunication) {
      this.protectionModules.set('communication', new SecureCommunication());
    }
    
    if (this.config.enableDependencyScanning) {
      this.protectionModules.set('dependency', new DependencyScanner());
    }
    
    this.setupGlobalProtection();
    this.startThreatMonitoring();
  }
  
  // 设置全局保护
  setupGlobalProtection() {
    // 设置全局错误处理
    window.addEventListener('error', (event) => {
      this.handleSecurityEvent({
        type: 'error',
        message: event.message,
        source: event.filename,
        line: event.lineno,
        column: event.colno,
        timestamp: Date.now()
      });
    });
    
    // 设置CSP违规处理
    document.addEventListener('securitypolicyviolation', (event) => {
      this.handleSecurityEvent({
        type: 'csp_violation',
        violatedDirective: event.violatedDirective,
        blockedURI: event.blockedURI,
        documentURI: event.documentURI,
        timestamp: Date.now()
      });
    });
    
    // 监控DOM变化
    if (typeof MutationObserver !== 'undefined') {
      const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          this.scanDOMChanges(mutation);
        });
      });
      
      observer.observe(document.body, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ['src', 'href', 'onclick', 'onload']
      });
    }
  }
  
  // 启动威胁监控
  startThreatMonitoring() {
    // 定期扫描威胁
    setInterval(() => {
      this.performSecurityScan();
    }, 30000); // 每30秒扫描一次
    
    // 监控网络请求
    this.interceptNetworkRequests();
    
    // 监控用户行为
    this.monitorUserBehavior();
  }
  
  // 处理安全事件
  handleSecurityEvent(event) {
    this.securityEvents.push(event);
    
    // 触发相应的保护模块
    this.protectionModules.forEach((module, type) => {
      if (module.canHandle && module.canHandle(event)) {
        module.handle(event);
      }
    });
    
    // 发送安全报告
    this.reportSecurityEvent(event);
    
    // 检查是否需要紧急响应
    if (this.isHighRiskEvent(event)) {
      this.triggerEmergencyResponse(event);
    }
  }
  
  // 扫描DOM变化
  scanDOMChanges(mutation) {
    const xssProtection = this.protectionModules.get('xss');
    if (xssProtection) {
      xssProtection.scanMutation(mutation);
    }
  }
  
  // 执行安全扫描
  performSecurityScan() {
    const scanResults = {};
    
    this.protectionModules.forEach((module, type) => {
      if (module.scan) {
        scanResults[type] = module.scan();
      }
    });
    
    return scanResults;
  }
  
  // 拦截网络请求
  interceptNetworkRequests() {
    const originalFetch = window.fetch;
    const originalXHR = window.XMLHttpRequest;
    
    // 拦截fetch请求
    window.fetch = async (...args) => {
      const [url, options] = args;
      
      // CSRF保护
      const csrfProtection = this.protectionModules.get('csrf');
      if (csrfProtection) {
        args[1] = csrfProtection.protectRequest(url, options);
      }
      
      // 安全通信保护
      const secureComm = this.protectionModules.get('communication');
      if (secureComm) {
        args[1] = secureComm.protectRequest(url, args[1]);
      }
      
      try {
        const response = await originalFetch.apply(this, args);
        
        // 检查响应安全性
        this.validateResponse(response);
        
        return response;
      } catch (error) {
        this.handleSecurityEvent({
          type: 'network_error',
          url,
          error: error.message,
          timestamp: Date.now()
        });
        throw error;
      }
    };
    
    // 拦截XMLHttpRequest
    window.XMLHttpRequest = function() {
      const xhr = new originalXHR();
      const originalOpen = xhr.open;
      const originalSend = xhr.send;
      
      xhr.open = function(method, url, ...args) {
        this._url = url;
        this._method = method;
        return originalOpen.apply(this, [method, url, ...args]);
      };
      
      xhr.send = function(data) {
        // CSRF保护
        const csrfProtection = this.protectionModules.get('csrf');
        if (csrfProtection) {
          csrfProtection.protectXHR(this);
        }
        
        return originalSend.apply(this, [data]);
      }.bind(this);
      
      return xhr;
    }.bind(this);
  }
  
  // 监控用户行为
  monitorUserBehavior() {
    let clickCount = 0;
    let lastClickTime = 0;
    
    document.addEventListener('click', (event) => {
      const currentTime = Date.now();
      
      // 检测异常点击行为
      if (currentTime - lastClickTime < 100) {
        clickCount++;
        if (clickCount > 10) {
          this.handleSecurityEvent({
            type: 'suspicious_clicking',
            element: event.target.tagName,
            timestamp: currentTime
          });
          clickCount = 0;
        }
      } else {
        clickCount = 0;
      }
      
      lastClickTime = currentTime;
      
      // 检查点击劫持
      const clickjackingProtection = this.protectionModules.get('clickjacking');
      if (clickjackingProtection) {
        clickjackingProtection.validateClick(event);
      }
    });
  }
  
  // 验证响应安全性
  validateResponse(response) {
    // 检查响应头
    const securityHeaders = [
      'x-frame-options',
      'x-content-type-options',
      'x-xss-protection',
      'strict-transport-security',
      'content-security-policy'
    ];
    
    const missingHeaders = securityHeaders.filter(header => 
      !response.headers.has(header)
    );
    
    if (missingHeaders.length > 0) {
      this.handleSecurityEvent({
        type: 'missing_security_headers',
        headers: missingHeaders,
        url: response.url,
        timestamp: Date.now()
      });
    }
  }
  
  // 判断是否为高风险事件
  isHighRiskEvent(event) {
    const highRiskTypes = [
      'xss_detected',
      'csrf_attack',
      'clickjacking_attempt',
      'data_exfiltration',
      'malicious_script'
    ];
    
    return highRiskTypes.includes(event.type);
  }
  
  // 触发紧急响应
  triggerEmergencyResponse(event) {
    console.warn('🚨 Security Alert:', event);
    
    // 可以在这里实现更多紧急响应措施
    // 例如：禁用某些功能、清除敏感数据、通知服务器等
    
    if (event.type === 'xss_detected') {
      // 清理可能的XSS内容
      this.protectionModules.get('xss')?.emergencyCleanup();
    }
    
    if (event.type === 'data_exfiltration') {
      // 阻止数据传输
      this.blockDataTransmission();
    }
  }
  
  // 阻止数据传输
  blockDataTransmission() {
    // 临时禁用网络请求
    const originalFetch = window.fetch;
    window.fetch = () => Promise.reject(new Error('Data transmission blocked for security'));
    
    // 5分钟后恢复
    setTimeout(() => {
      window.fetch = originalFetch;
    }, 5 * 60 * 1000);
  }
  
  // 报告安全事件
  reportSecurityEvent(event) {
    // 发送到安全监控服务
    if (typeof navigator.sendBeacon === 'function') {
      const data = JSON.stringify({
        event,
        userAgent: navigator.userAgent,
        url: window.location.href,
        timestamp: Date.now()
      });
      
      navigator.sendBeacon('/api/security/report', data);
    }
  }
  
  // 获取安全状态
  getSecurityStatus() {
    const status = {
      protectionModules: {},
      recentEvents: this.securityEvents.slice(-10),
      threatLevel: this.calculateThreatLevel(),
      lastScan: this.lastScanTime
    };
    
    this.protectionModules.forEach((module, type) => {
      status.protectionModules[type] = {
        enabled: true,
        status: module.getStatus ? module.getStatus() : 'active',
        lastUpdate: module.lastUpdate || Date.now()
      };
    });
    
    return status;
  }
  
  // 计算威胁等级
  calculateThreatLevel() {
    const recentEvents = this.securityEvents.filter(
      event => Date.now() - event.timestamp < 3600000 // 最近1小时
    );
    
    const highRiskEvents = recentEvents.filter(event => 
      this.isHighRiskEvent(event)
    ).length;
    
    if (highRiskEvents > 5) return 'critical';
    if (highRiskEvents > 2) return 'high';
    if (recentEvents.length > 10) return 'medium';
    return 'low';
  }
  
  // 更新安全配置
  updateConfig(newConfig) {
    this.config = { ...this.config, ...newConfig };
    
    // 重新初始化受影响的模块
    Object.keys(newConfig).forEach(key => {
      if (key.startsWith('enable')) {
        const moduleName = key.replace('enable', '').replace('Protection', '').toLowerCase();
        
        if (newConfig[key] && !this.protectionModules.has(moduleName)) {
          // 启用模块
          this.enableProtectionModule(moduleName);
        } else if (!newConfig[key] && this.protectionModules.has(moduleName)) {
          // 禁用模块
          this.disableProtectionModule(moduleName);
        }
      }
    });
  }
  
  // 启用保护模块
  enableProtectionModule(moduleName) {
    switch (moduleName) {
      case 'xss':
        this.protectionModules.set('xss', new XSSProtection());
        break;
      case 'csrf':
        this.protectionModules.set('csrf', new CSRFProtection());
        break;
      // ... 其他模块
    }
  }
  
  // 禁用保护模块
  disableProtectionModule(moduleName) {
    const module = this.protectionModules.get(moduleName);
    if (module && module.cleanup) {
      module.cleanup();
    }
    this.protectionModules.delete(moduleName);
  }
}
```

## 2. XSS防护深度实践

### 2.1 XSS攻击类型与检测

```javascript
// XSS保护模块
class XSSProtection {
  constructor(config = {}) {
    this.config = {
      enableReflectedXSSProtection: true,
      enableStoredXSSProtection: true,
      enableDOMXSSProtection: true,
      enableContentSanitization: true,
      enableInputValidation: true,
      strictMode: false,
      ...config
    };
    
    this.xssPatterns = this.initializeXSSPatterns();
    this.sanitizer = new DOMPurify();
    this.detectedThreats = [];
    this.whitelist = new Set();
    this.blacklist = new Set();
    
    this.init();
  }
  
  // 初始化XSS检测模式
  initializeXSSPatterns() {
    return {
      // 脚本标签模式
      scriptTags: [
        /<script[^>]*>.*?<\/script>/gi,
        /<script[^>]*>/gi,
        /javascript:/gi,
        /vbscript:/gi
      ],
      
      // 事件处理器模式
      eventHandlers: [
        /on\w+\s*=/gi,
        /onclick/gi,
        /onload/gi,
        /onerror/gi,
        /onmouseover/gi
      ],
      
      // 数据URI模式
      dataUris: [
        /data:text\/html/gi,
        /data:application\/javascript/gi,
        /data:text\/javascript/gi
      ],
      
      // 表达式模式
      expressions: [
        /expression\s*\(/gi,
        /eval\s*\(/gi,
        /setTimeout\s*\(/gi,
        /setInterval\s*\(/gi
      ],
      
      // 编码绕过模式
      encodingBypass: [
        /&#x[0-9a-f]+;/gi,
        /&#[0-9]+;/gi,
        /%[0-9a-f]{2}/gi,
        /\\u[0-9a-f]{4}/gi
      ]
    };
  }
  
  // 初始化保护
  init() {
    this.setupInputProtection();
    this.setupOutputProtection();
    this.setupDOMProtection();
    this.setupCSPIntegration();
  }
  
  // 设置输入保护
  setupInputProtection() {
    // 监控表单输入
    document.addEventListener('input', (event) => {
      if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
        this.validateInput(event.target);
      }
    });
    
    // 监控粘贴事件
    document.addEventListener('paste', (event) => {
      const clipboardData = event.clipboardData || window.clipboardData;
      const pastedData = clipboardData.getData('text');
      
      if (this.detectXSS(pastedData)) {
        event.preventDefault();
        this.handleXSSDetection({
          type: 'paste_xss',
          content: pastedData,
          element: event.target
        });
      }
    });
  }
  
  // 设置输出保护
  setupOutputProtection() {
    // 拦截innerHTML设置
    const originalInnerHTML = Object.getOwnPropertyDescriptor(Element.prototype, 'innerHTML');
    
    Object.defineProperty(Element.prototype, 'innerHTML', {
      set: function(value) {
        const sanitizedValue = this.sanitizeContent(value);
        return originalInnerHTML.set.call(this, sanitizedValue);
      }.bind(this),
      get: originalInnerHTML.get
    });
    
    // 拦截outerHTML设置
    const originalOuterHTML = Object.getOwnPropertyDescriptor(Element.prototype, 'outerHTML');
    
    Object.defineProperty(Element.prototype, 'outerHTML', {
      set: function(value) {
        const sanitizedValue = this.sanitizeContent(value);
        return originalOuterHTML.set.call(this, sanitizedValue);
      }.bind(this),
      get: originalOuterHTML.get
    });
  }
  
  // 设置DOM保护
  setupDOMProtection() {
    // 监控动态脚本创建
    const originalCreateElement = document.createElement;
    
    document.createElement = function(tagName) {
      const element = originalCreateElement.call(document, tagName);
      
      if (tagName.toLowerCase() === 'script') {
        this.validateScriptElement(element);
      }
      
      return element;
    }.bind(this);
    
    // 监控属性设置
    this.setupAttributeProtection();
  }
  
  // 设置属性保护
  setupAttributeProtection() {
    const dangerousAttributes = ['src', 'href', 'action', 'formaction'];
    
    dangerousAttributes.forEach(attr => {
      this.interceptAttribute(attr);
    });
  }
  
  // 拦截属性设置
  interceptAttribute(attributeName) {
    const originalSetAttribute = Element.prototype.setAttribute;
    
    Element.prototype.setAttribute = function(name, value) {
      if (name.toLowerCase() === attributeName.toLowerCase()) {
        value = this.sanitizeAttributeValue(name, value);
      }
      
      return originalSetAttribute.call(this, name, value);
    }.bind(this);
  }
  
  // 验证输入
  validateInput(element) {
    const value = element.value;
    
    if (this.detectXSS(value)) {
      this.handleXSSDetection({
        type: 'input_xss',
        content: value,
        element: element
      });
      
      // 清理输入
      element.value = this.sanitizeContent(value);
    }
  }
  
  // 检测XSS
  detectXSS(content) {
    if (!content || typeof content !== 'string') {
      return false;
    }
    
    // 检查黑名单
    if (this.blacklist.has(content)) {
      return true;
    }
    
    // 检查白名单
    if (this.whitelist.has(content)) {
      return false;
    }
    
    // 模式匹配检测
    for (const [category, patterns] of Object.entries(this.xssPatterns)) {
      for (const pattern of patterns) {
        if (pattern.test(content)) {
          return true;
        }
      }
    }
    
    // 启发式检测
    return this.heuristicDetection(content);
  }
  
  // 启发式检测
  heuristicDetection(content) {
    const suspiciousIndicators = [
      // 多个连续的特殊字符
      /[<>"'&]{3,}/,
      // 编码后的脚本标签
      /%3Cscript/i,
      // Base64编码的可疑内容
      /data:.*base64.*script/i,
      // 多层编码
      /%25[0-9a-f]{2}/i,
      // 异常的URL协议
      /^(data|javascript|vbscript):/i
    ];
    
    return suspiciousIndicators.some(pattern => pattern.test(content));
  }
  
  // 内容清理
  sanitizeContent(content) {
    if (!content || typeof content !== 'string') {
      return content;
    }
    
    // 使用DOMPurify清理
    if (this.sanitizer && this.sanitizer.sanitize) {
      return this.sanitizer.sanitize(content, {
        ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'p', 'br', 'span'],
        ALLOWED_ATTR: ['class', 'id'],
        FORBID_SCRIPT: true,
        FORBID_TAGS: ['script', 'object', 'embed', 'link', 'style'],
        FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover']
      });
    }
    
    // 备用清理方法
    return this.basicSanitize(content);
  }
  
  // 基础清理
  basicSanitize(content) {
    return content
      .replace(/<script[^>]*>.*?<\/script>/gi, '')
      .replace(/<script[^>]*>/gi, '')
      .replace(/javascript:/gi, '')
      .replace(/on\w+\s*=/gi, '')
      .replace(/expression\s*\(/gi, '')
      .replace(/eval\s*\(/gi, '');
  }
  
  // 清理属性值
  sanitizeAttributeValue(attributeName, value) {
    if (!value || typeof value !== 'string') {
      return value;
    }
    
    // URL属性特殊处理
    if (['src', 'href', 'action', 'formaction'].includes(attributeName.toLowerCase())) {
      return this.sanitizeURL(value);
    }
    
    return this.sanitizeContent(value);
  }
  
  // URL清理
  sanitizeURL(url) {
    // 检查协议
    const dangerousProtocols = ['javascript:', 'vbscript:', 'data:text/html'];
    
    for (const protocol of dangerousProtocols) {
      if (url.toLowerCase().startsWith(protocol)) {
        return '#';
      }
    }
    
    // 检查编码绕过
    const decodedUrl = decodeURIComponent(url);
    if (this.detectXSS(decodedUrl)) {
      return '#';
    }
    
    return url;
  }
  
  // 验证脚本元素
  validateScriptElement(scriptElement) {
    // 监控src属性设置
    Object.defineProperty(scriptElement, 'src', {
      set: function(value) {
        if (this.isAllowedScript(value)) {
          scriptElement._src = value;
        } else {
          this.handleXSSDetection({
            type: 'malicious_script',
            src: value,
            element: scriptElement
          });
        }
      }.bind(this),
      get: function() {
        return scriptElement._src;
      }
    });
  }
  
  // 检查是否为允许的脚本
  isAllowedScript(src) {
    // 检查白名单域名
    const allowedDomains = [
      'cdn.jsdelivr.net',
      'unpkg.com',
      'cdnjs.cloudflare.com'
    ];
    
    try {
      const url = new URL(src, window.location.origin);
      return allowedDomains.some(domain => url.hostname.endsWith(domain));
    } catch {
      return false;
    }
  }
  
  // 处理XSS检测
  handleXSSDetection(threat) {
    this.detectedThreats.push({
      ...threat,
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      url: window.location.href
    });
    
    // 触发安全事件
    const event = new CustomEvent('xss-detected', {
      detail: threat
    });
    document.dispatchEvent(event);
    
    // 记录到控制台
    console.warn('🛡️ XSS Threat Detected:', threat);
    
    // 严格模式下阻止执行
    if (this.config.strictMode) {
      throw new Error('XSS threat detected and blocked');
    }
  }
  
  // 扫描DOM变化
  scanMutation(mutation) {
    if (mutation.type === 'childList') {
      mutation.addedNodes.forEach(node => {
        if (node.nodeType === Node.ELEMENT_NODE) {
          this.scanElement(node);
        }
      });
    }
    
    if (mutation.type === 'attributes') {
      this.scanElementAttributes(mutation.target);
    }
  }
  
  // 扫描元素
  scanElement(element) {
    // 检查标签名
    if (element.tagName === 'SCRIPT') {
      this.validateScriptElement(element);
    }
    
    // 检查属性
    this.scanElementAttributes(element);
    
    // 递归检查子元素
    element.querySelectorAll('*').forEach(child => {
      this.scanElementAttributes(child);
    });
  }
  
  // 扫描元素属性
  scanElementAttributes(element) {
    const attributes = element.attributes;
    
    for (let i = 0; i < attributes.length; i++) {
      const attr = attributes[i];
      
      // 检查事件处理器
      if (attr.name.startsWith('on')) {
        this.handleXSSDetection({
          type: 'event_handler_xss',
          attribute: attr.name,
          value: attr.value,
          element: element
        });
        
        element.removeAttribute(attr.name);
      }
      
      // 检查危险属性值
      if (this.detectXSS(attr.value)) {
        this.handleXSSDetection({
          type: 'attribute_xss',
          attribute: attr.name,
          value: attr.value,
          element: element
        });
        
        element.setAttribute(attr.name, this.sanitizeAttributeValue(attr.name, attr.value));
      }
    }
  }
  
  // 紧急清理
  emergencyCleanup() {
    // 移除所有脚本标签
    document.querySelectorAll('script').forEach(script => {
      if (!this.isAllowedScript(script.src)) {
        script.remove();
      }
    });
    
    // 移除所有事件处理器
    document.querySelectorAll('*').forEach(element => {
      const attributes = [...element.attributes];
      attributes.forEach(attr => {
        if (attr.name.startsWith('on')) {
          element.removeAttribute(attr.name);
        }
      });
    });
    
    console.log('🧹 Emergency XSS cleanup completed');
  }
  
  // 获取状态
  getStatus() {
    return {
      threatsDetected: this.detectedThreats.length,
      lastThreat: this.detectedThreats[this.detectedThreats.length - 1],
      config: this.config
    };
  }
  
  // 添加到白名单
  addToWhitelist(content) {
    this.whitelist.add(content);
  }
  
  // 添加到黑名单
  addToBlacklist(content) {
    this.blacklist.add(content);
  }
  
  // 清理
  cleanup() {
    this.detectedThreats = [];
    this.whitelist.clear();
    this.blacklist.clear();
  }
}
```

## 3. CSRF防护深度实践

### 3.1 CSRF攻击检测与防护

```javascript
// CSRF保护模块
class CSRFProtection {
  constructor(config = {}) {
    this.config = {
      tokenName: '_csrf_token',
      headerName: 'X-CSRF-Token',
      cookieName: 'csrf_token',
      tokenLength: 32,
      enableSameSiteCookies: true,
      enableOriginValidation: true,
      enableReferrerValidation: true,
      enableDoubleSubmitCookies: true,
      trustedOrigins: [],
      ...config
    };
    
    this.tokens = new Map();
    this.sessionToken = null;
    this.detectedAttacks = [];
    
    this.init();
  }
  
  // 初始化CSRF保护
  init() {
    this.generateSessionToken();
    this.setupTokenValidation();
    this.setupOriginValidation();
    this.setupSameSiteCookies();
    this.interceptFormSubmissions();
  }
  
  // 生成会话令牌
  generateSessionToken() {
    this.sessionToken = this.generateToken();
    
    // 设置到meta标签
    let metaTag = document.querySelector('meta[name="csrf-token"]');
    if (!metaTag) {
      metaTag = document.createElement('meta');
      metaTag.name = 'csrf-token';
      document.head.appendChild(metaTag);
    }
    metaTag.content = this.sessionToken;
    
    // 设置到cookie（双重提交模式）
    if (this.config.enableDoubleSubmitCookies) {
      this.setCookie(this.config.cookieName, this.sessionToken, {
        secure: location.protocol === 'https:',
        sameSite: 'Strict',
        httpOnly: false // 需要JavaScript访问
      });
    }
  }
  
  // 生成随机令牌
  generateToken() {
    const array = new Uint8Array(this.config.tokenLength);
    crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  }
  
  // 设置Cookie
  setCookie(name, value, options = {}) {
    let cookieString = `${name}=${value}`;
    
    if (options.expires) {
      cookieString += `; expires=${options.expires.toUTCString()}`;
    }
    
    if (options.maxAge) {
      cookieString += `; max-age=${options.maxAge}`;
    }
    
    if (options.domain) {
      cookieString += `; domain=${options.domain}`;
    }
    
    if (options.path) {
      cookieString += `; path=${options.path}`;
    }
    
    if (options.secure) {
      cookieString += '; secure';
    }
    
    if (options.httpOnly) {
      cookieString += '; httponly';
    }
    
    if (options.sameSite) {
      cookieString += `; samesite=${options.sameSite}`;
    }
    
    document.cookie = cookieString;
  }
  
  // 获取Cookie值
  getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      return parts.pop().split(';').shift();
    }
    return null;
  }
  
  // 设置令牌验证
  setupTokenValidation() {
    // 为所有表单添加CSRF令牌
    document.addEventListener('DOMContentLoaded', () => {
      this.addTokensToForms();
    });
    
    // 监控新添加的表单
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            if (node.tagName === 'FORM') {
              this.addTokenToForm(node);
            } else {
              const forms = node.querySelectorAll('form');
              forms.forEach(form => this.addTokenToForm(form));
            }
          }
        });
      });
    });
    
    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }
  
  // 为所有表单添加令牌
  addTokensToForms() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => this.addTokenToForm(form));
  }
  
  // 为单个表单添加令牌
  addTokenToForm(form) {
    // 检查是否已经有CSRF令牌
    let tokenInput = form.querySelector(`input[name="${this.config.tokenName}"]`);
    
    if (!tokenInput) {
      tokenInput = document.createElement('input');
      tokenInput.type = 'hidden';
      tokenInput.name = this.config.tokenName;
      form.appendChild(tokenInput);
    }
    
    tokenInput.value = this.sessionToken;
  }
  
  // 设置来源验证
  setupOriginValidation() {
    if (!this.config.enableOriginValidation) return;
    
    // 添加受信任的来源
    this.config.trustedOrigins.push(window.location.origin);
  }
  
  // 设置SameSite Cookie
  setupSameSiteCookies() {
    if (!this.config.enableSameSiteCookies) return;
    
    // 检查现有cookie的SameSite设置
    const cookies = document.cookie.split(';');
    cookies.forEach(cookie => {
      const [name] = cookie.trim().split('=');
      if (!cookie.includes('SameSite')) {
        console.warn(`Cookie ${name} missing SameSite attribute`);
      }
    });
  }
  
  // 拦截表单提交
  interceptFormSubmissions() {
    document.addEventListener('submit', (event) => {
      const form = event.target;
      
      if (form.tagName === 'FORM') {
        if (!this.validateFormSubmission(form)) {
          event.preventDefault();
          this.handleCSRFAttack({
            type: 'form_submission',
            form: form,
            action: form.action,
            method: form.method
          });
        }
      }
    });
  }
  
  // 验证表单提交
  validateFormSubmission(form) {
    // 只验证POST、PUT、DELETE等修改性请求
    const method = form.method.toLowerCase();
    if (['get', 'head', 'options'].includes(method)) {
      return true;
    }
    
    // 检查CSRF令牌
    const tokenInput = form.querySelector(`input[name="${this.config.tokenName}"]`);
    if (!tokenInput || tokenInput.value !== this.sessionToken) {
      return false;
    }
    
    // 验证来源
    if (this.config.enableOriginValidation) {
      if (!this.validateOrigin()) {
        return false;
      }
    }
    
    // 验证Referrer
    if (this.config.enableReferrerValidation) {
      if (!this.validateReferrer()) {
        return false;
      }
    }
    
    return true;
  }
  
  // 保护请求
  protectRequest(url, options = {}) {
    const method = (options.method || 'GET').toLowerCase();
    
    // 只保护修改性请求
    if (['get', 'head', 'options'].includes(method)) {
      return options;
    }
    
    // 添加CSRF令牌到请求头
    if (!options.headers) {
      options.headers = {};
    }
    
    options.headers[this.config.headerName] = this.sessionToken;
    
    // 双重提交Cookie验证
    if (this.config.enableDoubleSubmitCookies) {
      const cookieToken = this.getCookie(this.config.cookieName);
      if (cookieToken !== this.sessionToken) {
        throw new Error('CSRF token mismatch');
      }
    }
    
    // 验证来源
    if (this.config.enableOriginValidation && !this.validateOrigin()) {
      throw new Error('Invalid origin');
    }
    
    return options;
  }
  
  // 保护XMLHttpRequest
  protectXHR(xhr) {
    const originalSetRequestHeader = xhr.setRequestHeader;
    
    xhr.setRequestHeader = function(name, value) {
      originalSetRequestHeader.call(this, name, value);
    };
    
    // 添加CSRF令牌
    xhr.setRequestHeader(this.config.headerName, this.sessionToken);
  }
  
  // 验证来源
  validateOrigin() {
    const origin = window.location.origin;
    return this.config.trustedOrigins.includes(origin);
  }
  
  // 验证Referrer
  validateReferrer() {
    const referrer = document.referrer;
    
    if (!referrer) {
      // 某些情况下referrer可能为空，需要根据安全策略决定
      return this.config.allowEmptyReferrer || false;
    }
    
    try {
      const referrerUrl = new URL(referrer);
      const currentUrl = new URL(window.location.href);
      
      return referrerUrl.origin === currentUrl.origin;
    } catch {
      return false;
    }
  }
  
  // 处理CSRF攻击
  handleCSRFAttack(attack) {
    this.detectedAttacks.push({
      ...attack,
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      referrer: document.referrer
    });
    
    // 触发安全事件
    const event = new CustomEvent('csrf-attack-detected', {
      detail: attack
    });
    document.dispatchEvent(event);
    
    console.warn('🛡️ CSRF Attack Detected:', attack);
    
    // 刷新令牌
    this.refreshToken();
  }
  
  // 刷新令牌
  refreshToken() {
    this.generateSessionToken();
    this.addTokensToForms();
    
    console.log('🔄 CSRF token refreshed');
  }
  
  // 获取状态
  getStatus() {
    return {
      sessionToken: this.sessionToken ? '***' + this.sessionToken.slice(-4) : null,
      attacksDetected: this.detectedAttacks.length,
      lastAttack: this.detectedAttacks[this.detectedAttacks.length - 1],
      config: this.config
    };
  }
  
  // 清理
  cleanup() {
    this.tokens.clear();
    this.detectedAttacks = [];
    this.sessionToken = null;
  }
}
```

## 4. 点击劫持防护

### 4.1 点击劫持检测与防护

```javascript
// 点击劫持保护模块
class ClickjackingProtection {
  constructor(config = {}) {
    this.config = {
      enableFrameBusting: true,
      enableXFrameOptions: true,
      enableCSPFrameAncestors: true,
      allowedFrameOrigins: [],
      enableClickTracking: true,
      suspiciousClickThreshold: 5,
      clickTimeWindow: 1000,
      ...config
    };
    
    this.clickHistory = [];
    this.suspiciousActivities = [];
    this.frameDetected = false;
    
    this.init();
  }
  
  // 初始化保护
  init() {
    this.detectFraming();
    this.setupFrameBusting();
    this.setupClickTracking();
    this.setupVisibilityDetection();
    this.setupCSPHeaders();
  }
  
  // 检测是否在iframe中
  detectFraming() {
    try {
      this.frameDetected = window.self !== window.top;
      
      if (this.frameDetected) {
        this.handleFrameDetection();
      }
    } catch (error) {
      // 跨域iframe会抛出异常
      this.frameDetected = true;
      this.handleFrameDetection();
    }
  }
  
  // 处理框架检测
  handleFrameDetection() {
    console.warn('🚨 Page loaded in frame detected');
    
    // 检查是否为允许的来源
    if (!this.isAllowedFrameOrigin()) {
      this.handleClickjackingAttempt({
        type: 'unauthorized_framing',
        parentOrigin: this.getParentOrigin(),
        timestamp: Date.now()
      });
    }
  }
  
  // 检查是否为允许的框架来源
  isAllowedFrameOrigin() {
    try {
      const parentOrigin = window.parent.location.origin;
      return this.config.allowedFrameOrigins.includes(parentOrigin);
    } catch {
      // 跨域情况下无法获取父窗口信息
      return false;
    }
  }
  
  // 获取父窗口来源
  getParentOrigin() {
    try {
      return window.parent.location.origin;
    } catch {
      return 'unknown';
    }
  }
  
  // 设置框架破坏
  setupFrameBusting() {
    if (!this.config.enableFrameBusting) return;
    
    // 经典的frame busting代码
    if (this.frameDetected && !this.isAllowedFrameOrigin()) {
      try {
        window.top.location = window.location;
      } catch {
        // 如果无法重定向，显示警告
        this.showFramingWarning();
      }
    }
    
    // 防止被重新框架化
    Object.defineProperty(window, 'top', {
      get: function() {
        return window;
      },
      configurable: false
    });
  }
  
  // 显示框架警告
  showFramingWarning() {
    const warning = document.createElement('div');
    warning.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(255, 0, 0, 0.9);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      font-weight: bold;
      z-index: 999999;
      text-align: center;
    `;
    
    warning.innerHTML = `
      <div>
        <h2>⚠️ Security Warning</h2>
        <p>This page is being displayed in an unauthorized frame.</p>
        <p>This may be a clickjacking attack.</p>
        <button onclick="window.open(window.location.href, '_blank')">Open in New Window</button>
      </div>
    `;
    
    document.body.appendChild(warning);
  }
  
  // 设置点击跟踪
  setupClickTracking() {
    if (!this.config.enableClickTracking) return;
    
    document.addEventListener('click', (event) => {
      this.trackClick(event);
    }, true); // 使用捕获阶段
    
    document.addEventListener('mousedown', (event) => {
      this.trackMouseDown(event);
    }, true);
  }
  
  // 跟踪点击
  trackClick(event) {
    const clickData = {
      timestamp: Date.now(),
      x: event.clientX,
      y: event.clientY,
      target: event.target.tagName,
      targetId: event.target.id,
      targetClass: event.target.className,
      button: event.button,
      ctrlKey: event.ctrlKey,
      shiftKey: event.shiftKey,
      altKey: event.altKey
    };
    
    this.clickHistory.push(clickData);
    
    // 保持点击历史在合理范围内
    if (this.clickHistory.length > 100) {
      this.clickHistory.shift();
    }
    
    // 分析点击模式
    this.analyzeClickPattern(clickData);
  }
  
  // 跟踪鼠标按下
  trackMouseDown(event) {
    // 检测是否在透明元素上点击
    const element = event.target;
    const computedStyle = window.getComputedStyle(element);
    
    if (computedStyle.opacity === '0' || computedStyle.visibility === 'hidden') {
      this.handleClickjackingAttempt({
        type: 'transparent_element_click',
        element: element.tagName,
        elementId: element.id,
        opacity: computedStyle.opacity,
        visibility: computedStyle.visibility,
        timestamp: Date.now()
      });
    }
  }
  
  // 分析点击模式
  analyzeClickPattern(clickData) {
    const recentClicks = this.clickHistory.filter(
      click => clickData.timestamp - click.timestamp < this.config.clickTimeWindow
    );
    
    // 检测快速连续点击
    if (recentClicks.length > this.config.suspiciousClickThreshold) {
      this.handleClickjackingAttempt({
        type: 'rapid_clicking',
        clickCount: recentClicks.length,
        timeWindow: this.config.clickTimeWindow,
        timestamp: Date.now()
      });
    }
    
    // 检测点击位置模式
    this.detectClickPositionPattern(recentClicks);
  }
  
  // 检测点击位置模式
  detectClickPositionPattern(clicks) {
    if (clicks.length < 3) return;
    
    // 检测是否所有点击都在同一位置（可能是自动化攻击）
    const firstClick = clicks[0];
    const samePosition = clicks.every(click => 
      Math.abs(click.x - firstClick.x) < 5 && 
      Math.abs(click.y - firstClick.y) < 5
    );
    
    if (samePosition && clicks.length > 3) {
      this.handleClickjackingAttempt({
        type: 'automated_clicking',
        position: { x: firstClick.x, y: firstClick.y },
        clickCount: clicks.length,
        timestamp: Date.now()
      });
    }
  }
  
  // 设置可见性检测
  setupVisibilityDetection() {
    // 检测页面可见性变化
    document.addEventListener('visibilitychange', () => {
      if (document.hidden && this.frameDetected) {
        this.handleClickjackingAttempt({
          type: 'visibility_manipulation',
          hidden: document.hidden,
          timestamp: Date.now()
        });
      }
    });
    
    // 检测窗口焦点变化
    window.addEventListener('blur', () => {
      if (this.frameDetected) {
        this.handleClickjackingAttempt({
          type: 'focus_manipulation',
          timestamp: Date.now()
        });
      }
    });
  }
  
  // 设置CSP头
  setupCSPHeaders() {
    if (!this.config.enableCSPFrameAncestors) return;
    
    // 检查是否已设置CSP
    const metaCSP = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
    
    if (!metaCSP) {
      const cspMeta = document.createElement('meta');
      cspMeta.httpEquiv = 'Content-Security-Policy';
      
      let frameAncestors = "frame-ancestors 'self'";
      if (this.config.allowedFrameOrigins.length > 0) {
        frameAncestors += ' ' + this.config.allowedFrameOrigins.join(' ');
      }
      
      cspMeta.content = frameAncestors;
      document.head.appendChild(cspMeta);
    }
  }
  
  // 验证点击
  validateClick(event) {
    // 检查点击是否在可疑区域
    const suspiciousAreas = this.identifySuspiciousAreas();
    
    for (const area of suspiciousAreas) {
      if (this.isClickInArea(event, area)) {
        this.handleClickjackingAttempt({
          type: 'suspicious_area_click',
          area: area,
          clickPosition: { x: event.clientX, y: event.clientY },
          timestamp: Date.now()
        });
        
        return false;
      }
    }
    
    return true;
  }
  
  // 识别可疑区域
  identifySuspiciousAreas() {
    const areas = [];
    
    // 查找透明或隐藏的可点击元素
    const clickableElements = document.querySelectorAll('a, button, input[type="submit"], input[type="button"]');
    
    clickableElements.forEach(element => {
      const style = window.getComputedStyle(element);
      const rect = element.getBoundingClientRect();
      
      if ((style.opacity === '0' || style.visibility === 'hidden') && 
          rect.width > 0 && rect.height > 0) {
        areas.push({
          element: element,
          rect: rect,
          reason: 'transparent_clickable'
        });
      }
    });
    
    return areas;
  }
  
  // 检查点击是否在区域内
  isClickInArea(event, area) {
    const rect = area.rect;
    return event.clientX >= rect.left && 
           event.clientX <= rect.right && 
           event.clientY >= rect.top && 
           event.clientY <= rect.bottom;
  }
  
  // 处理点击劫持攻击
  handleClickjackingAttempt(attempt) {
    this.suspiciousActivities.push(attempt);
    
    // 触发安全事件
    const event = new CustomEvent('clickjacking-detected', {
      detail: attempt
    });
    document.dispatchEvent(event);
    
    console.warn('🛡️ Clickjacking Attempt Detected:', attempt);
    
    // 根据攻击类型采取相应措施
    this.respondToAttack(attempt);
  }
  
  // 响应攻击
  respondToAttack(attempt) {
    switch (attempt.type) {
      case 'unauthorized_framing':
        if (this.config.enableFrameBusting) {
          this.showFramingWarning();
        }
        break;
        
      case 'rapid_clicking':
        this.temporarilyDisableClicks();
        break;
        
      case 'transparent_element_click':
        this.highlightSuspiciousElements();
        break;
    }
  }
  
  // 临时禁用点击
  temporarilyDisableClicks() {
    const overlay = document.createElement('div');
    overlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(255, 255, 0, 0.3);
      z-index: 999998;
      pointer-events: auto;
    `;
    
    overlay.innerHTML = `
      <div style="
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: white;
        padding: 20px;
        border-radius: 5px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
      ">
        <h3>⚠️ Suspicious Activity Detected</h3>
        <p>Rapid clicking detected. Please wait a moment.</p>
        <div id="countdown">5</div>
      </div>
    `;
    
    document.body.appendChild(overlay);
    
    let countdown = 5;
    const countdownElement = overlay.querySelector('#countdown');
    
    const timer = setInterval(() => {
      countdown--;
      countdownElement.textContent = countdown;
      
      if (countdown <= 0) {
        clearInterval(timer);
        overlay.remove();
      }
    }, 1000);
  }
  
  // 高亮可疑元素
  highlightSuspiciousElements() {
    const suspiciousAreas = this.identifySuspiciousAreas();
    
    suspiciousAreas.forEach(area => {
      const highlight = document.createElement('div');
      highlight.style.cssText = `
        position: absolute;
        border: 3px solid red;
        background: rgba(255, 0, 0, 0.2);
        pointer-events: none;
        z-index: 999997;
      `;
      
      const rect = area.rect;
      highlight.style.left = rect.left + 'px';
      highlight.style.top = rect.top + 'px';
      highlight.style.width = rect.width + 'px';
      highlight.style.height = rect.height + 'px';
      
      document.body.appendChild(highlight);
      
      // 5秒后移除高亮
      setTimeout(() => {
        highlight.remove();
      }, 5000);
    });
  }
  
  // 获取状态
  getStatus() {
    return {
      frameDetected: this.frameDetected,
      suspiciousActivities: this.suspiciousActivities.length,
      recentClicks: this.clickHistory.length,
      lastActivity: this.suspiciousActivities[this.suspiciousActivities.length - 1],
      config: this.config
    };
  }
  
  // 清理
  cleanup() {
    this.clickHistory = [];
    this.suspiciousActivities = [];
  }
}
```

## 5. 内容安全策略（CSP）

### 5.1 CSP策略管理

```javascript
// 内容安全策略管理器
class ContentSecurityPolicyManager {
  constructor(config = {}) {
    this.config = {
      enableReporting: true,
      reportUri: '/csp-report',
      enableViolationLogging: true,
      strictMode: false,
      allowedSources: {
        script: ["'self'"],
        style: ["'self'", "'unsafe-inline'"],
        img: ["'self'", 'data:', 'https:'],
        connect: ["'self'"],
        font: ["'self'"],
        object: ["'none'"],
        media: ["'self'"],
        frame: ["'none'"]
      },
      ...config
    };
    
    this.violations = [];
    this.policies = new Map();
    this.dynamicNonces = new Set();
    
    this.init();
  }
  
  // 初始化CSP
  init() {
    this.generateCSPHeader();
    this.setupViolationReporting();
    this.setupDynamicNonces();
    this.monitorPolicyViolations();
  }
  
  // 生成CSP头
  generateCSPHeader() {
    const directives = [];
    
    // 基础指令
    directives.push(`default-src 'self'`);
    
    // 脚本源
    if (this.config.allowedSources.script.length > 0) {
      directives.push(`script-src ${this.config.allowedSources.script.join(' ')}`);
    }
    
    // 样式源
    if (this.config.allowedSources.style.length > 0) {
      directives.push(`style-src ${this.config.allowedSources.style.join(' ')}`);
    }
    
    // 图片源
    if (this.config.allowedSources.img.length > 0) {
      directives.push(`img-src ${this.config.allowedSources.img.join(' ')}`);
    }
    
    // 连接源
    if (this.config.allowedSources.connect.length > 0) {
      directives.push(`connect-src ${this.config.allowedSources.connect.join(' ')}`);
    }
    
    // 字体源
    if (this.config.allowedSources.font.length > 0) {
      directives.push(`font-src ${this.config.allowedSources.font.join(' ')}`);
    }
    
    // 对象源
    directives.push(`object-src ${this.config.allowedSources.object.join(' ')}`);
    
    // 媒体源
    if (this.config.allowedSources.media.length > 0) {
      directives.push(`media-src ${this.config.allowedSources.media.join(' ')}`);
    }
    
    // 框架源
    directives.push(`frame-src ${this.config.allowedSources.frame.join(' ')}`);
    
    // 框架祖先
    directives.push(`frame-ancestors 'self'`);
    
    // 基础URI
    directives.push(`base-uri 'self'`);
    
    // 表单动作
    directives.push(`form-action 'self'`);
    
    // 报告URI
    if (this.config.enableReporting) {
      directives.push(`report-uri ${this.config.reportUri}`);
    }
    
    const cspHeader = directives.join('; ');
    this.setCSPHeader(cspHeader);
    
    return cspHeader;
  }
  
  // 设置CSP头
  setCSPHeader(policy) {
    // 通过meta标签设置CSP
    let metaTag = document.querySelector('meta[http-equiv="Content-Security-Policy"]');
    
    if (!metaTag) {
      metaTag = document.createElement('meta');
      metaTag.httpEquiv = 'Content-Security-Policy';
      document.head.appendChild(metaTag);
    }
    
    metaTag.content = policy;
    this.policies.set('main', policy);
  }
  
  // 设置违规报告
  setupViolationReporting() {
    if (!this.config.enableReporting) return;
    
    document.addEventListener('securitypolicyviolation', (event) => {
      this.handleViolation(event);
    });
  }
  
  // 处理CSP违规
  handleViolation(event) {
    const violation = {
      documentURI: event.documentURI,
      referrer: event.referrer,
      blockedURI: event.blockedURI,
      violatedDirective: event.violatedDirective,
      effectiveDirective: event.effectiveDirective,
      originalPolicy: event.originalPolicy,
      sourceFile: event.sourceFile,
      lineNumber: event.lineNumber,
      columnNumber: event.columnNumber,
      statusCode: event.statusCode,
      timestamp: Date.now()
    };
    
    this.violations.push(violation);
    
    if (this.config.enableViolationLogging) {
      console.warn('🛡️ CSP Violation:', violation);
    }
    
    // 触发违规事件
    const customEvent = new CustomEvent('csp-violation', {
      detail: violation
    });
    document.dispatchEvent(customEvent);
    
    // 发送违规报告
    this.sendViolationReport(violation);
  }
  
  // 发送违规报告
  async sendViolationReport(violation) {
    if (!this.config.reportUri) return;
    
    try {
      await fetch(this.config.reportUri, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          'csp-report': violation
        })
      });
    } catch (error) {
      console.error('Failed to send CSP violation report:', error);
    }
  }
  
  // 设置动态nonce
  setupDynamicNonces() {
    // 为内联脚本生成nonce
    this.generateNonce();
    
    // 监控新添加的脚本
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            if (node.tagName === 'SCRIPT' && !node.src) {
              this.addNonceToScript(node);
            } else if (node.tagName === 'STYLE') {
              this.addNonceToStyle(node);
            }
          }
        });
      });
    });
    
    observer.observe(document.head, {
      childList: true,
      subtree: true
    });
  }
  
  // 生成nonce
  generateNonce() {
    const array = new Uint8Array(16);
    crypto.getRandomValues(array);
    const nonce = btoa(String.fromCharCode(...array));
    this.dynamicNonces.add(nonce);
    return nonce;
  }
  
  // 为脚本添加nonce
  addNonceToScript(script) {
    if (!script.nonce) {
      const nonce = this.generateNonce();
      script.nonce = nonce;
      
      // 更新CSP策略以包含新的nonce
      this.updateScriptSrcWithNonce(nonce);
    }
  }
  
  // 为样式添加nonce
  addNonceToStyle(style) {
    if (!style.nonce) {
      const nonce = this.generateNonce();
      style.nonce = nonce;
      
      // 更新CSP策略以包含新的nonce
      this.updateStyleSrcWithNonce(nonce);
    }
  }
  
  // 更新脚本源策略
  updateScriptSrcWithNonce(nonce) {
    const currentSources = this.config.allowedSources.script;
    const nonceSource = `'nonce-${nonce}'`;
    
    if (!currentSources.includes(nonceSource)) {
      currentSources.push(nonceSource);
      this.generateCSPHeader();
    }
  }
  
  // 更新样式源策略
  updateStyleSrcWithNonce(nonce) {
    const currentSources = this.config.allowedSources.style;
    const nonceSource = `'nonce-${nonce}'`;
    
    if (!currentSources.includes(nonceSource)) {
      currentSources.push(nonceSource);
      this.generateCSPHeader();
    }
  }
  
  // 监控策略违规
  monitorPolicyViolations() {
    // 检查现有脚本是否符合CSP
    const scripts = document.querySelectorAll('script');
    scripts.forEach(script => {
      if (!script.src && !script.nonce && script.innerHTML.trim()) {
        console.warn('Inline script without nonce detected:', script);
      }
    });
    
    // 检查现有样式是否符合CSP
    const styles = document.querySelectorAll('style');
    styles.forEach(style => {
      if (!style.nonce && style.innerHTML.trim()) {
        console.warn('Inline style without nonce detected:', style);
      }
    });
  }
  
  // 添加允许的源
  addAllowedSource(directive, source) {
    if (this.config.allowedSources[directive]) {
      if (!this.config.allowedSources[directive].includes(source)) {
        this.config.allowedSources[directive].push(source);
        this.generateCSPHeader();
      }
    }
  }
  
  // 移除允许的源
  removeAllowedSource(directive, source) {
    if (this.config.allowedSources[directive]) {
      const index = this.config.allowedSources[directive].indexOf(source);
      if (index > -1) {
        this.config.allowedSources[directive].splice(index, 1);
        this.generateCSPHeader();
      }
    }
  }
  
  // 验证资源
  validateResource(type, url) {
    const allowedSources = this.config.allowedSources[type] || [];
    
    // 检查是否为允许的源
    for (const source of allowedSources) {
      if (source === "'self'" && this.isSameOrigin(url)) {
        return true;
      }
      
      if (source === "'none'") {
        return false;
      }
      
      if (source.startsWith('http') && url.startsWith(source)) {
        return true;
      }
      
      if (source === 'data:' && url.startsWith('data:')) {
        return true;
      }
      
      if (source === 'https:' && url.startsWith('https:')) {
        return true;
      }
    }
    
    return false;
  }
  
  // 检查是否为同源
  isSameOrigin(url) {
    try {
      const urlObj = new URL(url, window.location.href);
      return urlObj.origin === window.location.origin;
    } catch {
      return false;
    }
  }
  
  // 获取违规统计
  getViolationStats() {
    const stats = {
      total: this.violations.length,
      byDirective: {},
      bySource: {},
      recent: this.violations.slice(-10)
    };
    
    this.violations.forEach(violation => {
      // 按指令统计
      const directive = violation.violatedDirective;
      stats.byDirective[directive] = (stats.byDirective[directive] || 0) + 1;
      
      // 按源统计
      const source = violation.blockedURI;
      stats.bySource[source] = (stats.bySource[source] || 0) + 1;
    });
    
    return stats;
  }
  
  // 获取状态
  getStatus() {
    return {
      policies: Object.fromEntries(this.policies),
      violations: this.violations.length,
      nonces: this.dynamicNonces.size,
      config: this.config,
      stats: this.getViolationStats()
    };
  }
  
  // 清理
  cleanup() {
    this.violations = [];
    this.policies.clear();
    this.dynamicNonces.clear();
  }
}
```

## 6. 输入验证与数据清理

### 6.1 输入验证管理器

```javascript
// 输入验证管理器
class InputValidationManager {
  constructor(config = {}) {
    this.config = {
      enableRealTimeValidation: true,
      enableSanitization: true,
      maxInputLength: 10000,
      allowedTags: ['b', 'i', 'em', 'strong', 'p', 'br'],
      allowedAttributes: ['class', 'id'],
      enableCSRFValidation: true,
      enableRateLimiting: true,
      rateLimitWindow: 60000, // 1分钟
      rateLimitMax: 100,
      ...config
    };
    
    this.validators = new Map();
    this.sanitizers = new Map();
    this.inputHistory = [];
    this.rateLimitData = new Map();
    
    this.init();
  }
  
  // 初始化
  init() {
    this.setupDefaultValidators();
    this.setupDefaultSanitizers();
    this.setupInputMonitoring();
    this.setupRateLimiting();
  }
  
  // 设置默认验证器
  setupDefaultValidators() {
    // 邮箱验证
    this.addValidator('email', (value) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(value);
    });
    
    // URL验证
    this.addValidator('url', (value) => {
      try {
        new URL(value);
        return true;
      } catch {
        return false;
      }
    });
    
    // 电话号码验证
    this.addValidator('phone', (value) => {
      const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
      return phoneRegex.test(value.replace(/[\s\-\(\)]/g, ''));
    });
    
    // 数字验证
    this.addValidator('number', (value) => {
      return !isNaN(value) && isFinite(value);
    });
    
    // 整数验证
    this.addValidator('integer', (value) => {
      return Number.isInteger(Number(value));
    });
    
    // 日期验证
    this.addValidator('date', (value) => {
      const date = new Date(value);
      return date instanceof Date && !isNaN(date);
    });
    
    // 密码强度验证
    this.addValidator('password', (value) => {
      // 至少8位，包含大小写字母、数字和特殊字符
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
      return passwordRegex.test(value);
    });
    
    // SQL注入检测
    this.addValidator('sql-safe', (value) => {
      const sqlPatterns = [
        /('|(\-\-)|(;)|(\||\|)|(\*|\*))/i,
        /(union|select|insert|delete|update|drop|create|alter|exec|execute)/i
      ];
      
      return !sqlPatterns.some(pattern => pattern.test(value));
    });
    
    // XSS检测
    this.addValidator('xss-safe', (value) => {
      const xssPatterns = [
        /<script[^>]*>.*?<\/script>/gi,
        /<iframe[^>]*>.*?<\/iframe>/gi,
        /javascript:/gi,
        /on\w+\s*=/gi,
        /<object[^>]*>.*?<\/object>/gi,
        /<embed[^>]*>.*?<\/embed>/gi
      ];
      
      return !xssPatterns.some(pattern => pattern.test(value));
    });
  }
  
  // 设置默认清理器
  setupDefaultSanitizers() {
    // HTML清理
    this.addSanitizer('html', (value) => {
      return this.sanitizeHTML(value);
    });
    
    // 移除脚本标签
    this.addSanitizer('remove-scripts', (value) => {
      return value.replace(/<script[^>]*>.*?<\/script>/gi, '');
    });
    
    // 移除事件处理器
    this.addSanitizer('remove-events', (value) => {
      return value.replace(/on\w+\s*=\s*["'][^"']*["']/gi, '');
    });
    
    // 移除危险协议
    this.addSanitizer('safe-urls', (value) => {
      return value.replace(/(javascript|data|vbscript):/gi, '');
    });
    
    // 转义HTML实体
    this.addSanitizer('escape-html', (value) => {
      const div = document.createElement('div');
      div.textContent = value;
      return div.innerHTML;
    });
    
    // 移除多余空白
    this.addSanitizer('trim-whitespace', (value) => {
      return value.replace(/\s+/g, ' ').trim();
    });
  }
  
  // 设置输入监控
  setupInputMonitoring() {
    if (!this.config.enableRealTimeValidation) return;
    
    // 监控所有输入元素
    document.addEventListener('input', (event) => {
      if (event.target.matches('input, textarea, select')) {
        this.validateInput(event.target);
      }
    });
    
    // 监控表单提交
    document.addEventListener('submit', (event) => {
      if (event.target.tagName === 'FORM') {
        if (!this.validateForm(event.target)) {
          event.preventDefault();
        }
      }
    });
  }
  
  // 设置速率限制
  setupRateLimiting() {
    if (!this.config.enableRateLimiting) return;
    
    document.addEventListener('input', (event) => {
      if (event.target.matches('input, textarea')) {
        this.checkRateLimit(event.target);
      }
    });
  }
  
  // 添加验证器
  addValidator(name, validator) {
    this.validators.set(name, validator);
  }
  
  // 添加清理器
  addSanitizer(name, sanitizer) {
    this.sanitizers.set(name, sanitizer);
  }
  
  // 验证输入
  validateInput(input) {
    const value = input.value;
    const validationRules = this.getValidationRules(input);
    const errors = [];
    
    // 长度检查
    if (value.length > this.config.maxInputLength) {
      errors.push(`Input too long (max: ${this.config.maxInputLength})`);
    }
    
    // 应用验证规则
    validationRules.forEach(rule => {
      const validator = this.validators.get(rule);
      if (validator && !validator(value)) {
        errors.push(`Validation failed: ${rule}`);
      }
    });
    
    // 显示验证结果
    this.displayValidationResult(input, errors);
    
    // 记录输入历史
    this.recordInput(input, value, errors);
    
    return errors.length === 0;
  }
  
  // 获取验证规则
  getValidationRules(input) {
    const rules = [];
    
    // 从data属性获取规则
    if (input.dataset.validate) {
      rules.push(...input.dataset.validate.split(','));
    }
    
    // 根据输入类型添加默认规则
    switch (input.type) {
      case 'email':
        rules.push('email');
        break;
      case 'url':
        rules.push('url');
        break;
      case 'tel':
        rules.push('phone');
        break;
      case 'number':
        rules.push('number');
        break;
      case 'password':
        rules.push('password');
        break;
    }
    
    // 安全检查
    rules.push('xss-safe', 'sql-safe');
    
    return rules;
  }
  
  // 显示验证结果
  displayValidationResult(input, errors) {
    // 移除现有错误提示
    const existingError = input.parentNode.querySelector('.validation-error');
    if (existingError) {
      existingError.remove();
    }
    
    // 更新输入样式
    if (errors.length > 0) {
      input.classList.add('invalid');
      input.classList.remove('valid');
      
      // 显示错误信息
      const errorDiv = document.createElement('div');
      errorDiv.className = 'validation-error';
      errorDiv.style.cssText = 'color: red; font-size: 12px; margin-top: 2px;';
      errorDiv.textContent = errors[0]; // 只显示第一个错误
      
      input.parentNode.insertBefore(errorDiv, input.nextSibling);
    } else {
      input.classList.add('valid');
      input.classList.remove('invalid');
    }
  }
  
  // 验证表单
  validateForm(form) {
    const inputs = form.querySelectorAll('input, textarea, select');
    let isValid = true;
    
    inputs.forEach(input => {
      if (!this.validateInput(input)) {
        isValid = false;
      }
    });
    
    return isValid;
  }
  
  // 清理输入
  sanitizeInput(value, sanitizers = ['html', 'remove-scripts', 'remove-events']) {
    let sanitized = value;
    
    sanitizers.forEach(sanitizerName => {
      const sanitizer = this.sanitizers.get(sanitizerName);
      if (sanitizer) {
        sanitized = sanitizer(sanitized);
      }
    });
    
    return sanitized;
  }
  
  // HTML清理
  sanitizeHTML(html) {
    const div = document.createElement('div');
    div.innerHTML = html;
    
    // 移除不允许的标签
    const allElements = div.querySelectorAll('*');
    allElements.forEach(element => {
      if (!this.config.allowedTags.includes(element.tagName.toLowerCase())) {
        element.remove();
      } else {
        // 移除不允许的属性
        const attributes = Array.from(element.attributes);
        attributes.forEach(attr => {
          if (!this.config.allowedAttributes.includes(attr.name)) {
            element.removeAttribute(attr.name);
          }
        });
      }
    });
    
    return div.innerHTML;
  }
  
  // 检查速率限制
  checkRateLimit(input) {
    const now = Date.now();
    const key = input.name || input.id || 'anonymous';
    
    if (!this.rateLimitData.has(key)) {
      this.rateLimitData.set(key, []);
    }
    
    const timestamps = this.rateLimitData.get(key);
    
    // 移除过期的时间戳
    const validTimestamps = timestamps.filter(
      timestamp => now - timestamp < this.config.rateLimitWindow
    );
    
    validTimestamps.push(now);
    this.rateLimitData.set(key, validTimestamps);
    
    // 检查是否超过限制
    if (validTimestamps.length > this.config.rateLimitMax) {
      this.handleRateLimitExceeded(input);
      return false;
    }
    
    return true;
  }
  
  // 处理速率限制超出
  handleRateLimitExceeded(input) {
    input.disabled = true;
    input.placeholder = 'Rate limit exceeded. Please wait...';
    
    // 显示警告
    const warning = document.createElement('div');
    warning.className = 'rate-limit-warning';
    warning.style.cssText = 'color: orange; font-size: 12px; margin-top: 2px;';
    warning.textContent = 'Too many inputs. Please slow down.';
    
    input.parentNode.insertBefore(warning, input.nextSibling);
    
    // 30秒后恢复
    setTimeout(() => {
      input.disabled = false;
      input.placeholder = '';
      warning.remove();
    }, 30000);
  }
  
  // 记录输入
  recordInput(input, value, errors) {
    this.inputHistory.push({
      element: input.name || input.id,
      value: value.substring(0, 100), // 只记录前100个字符
      errors: errors,
      timestamp: Date.now()
    });
    
    // 保持历史记录在合理范围内
    if (this.inputHistory.length > 1000) {
      this.inputHistory.shift();
    }
  }
  
  // 获取输入统计
  getInputStats() {
    const stats = {
      totalInputs: this.inputHistory.length,
      errorCount: this.inputHistory.filter(input => input.errors.length > 0).length,
      rateLimitViolations: 0,
      recentErrors: this.inputHistory.filter(input => 
        input.errors.length > 0 && 
        Date.now() - input.timestamp < 300000 // 5分钟内
      )
    };
    
    return stats;
  }
  
  // 获取状态
  getStatus() {
    return {
      validators: Array.from(this.validators.keys()),
      sanitizers: Array.from(this.sanitizers.keys()),
      inputHistory: this.inputHistory.length,
      rateLimitData: this.rateLimitData.size,
      stats: this.getInputStats(),
      config: this.config
    };
  }
  
  // 清理
  cleanup() {
    this.inputHistory = [];
    this.rateLimitData.clear();
  }
}
```

## 7. 安全存储管理

### 7.1 安全存储管理器

```javascript
// 安全存储管理器
class SecureStorageManager {
  constructor(config = {}) {
    this.config = {
      enableEncryption: true,
      encryptionKey: null,
      enableCompression: true,
      enableExpiration: true,
      defaultTTL: 3600000, // 1小时
      enableIntegrityCheck: true,
      enableSecureTransport: true,
      storageQuotaLimit: 5 * 1024 * 1024, // 5MB
      ...config
    };
    
    this.encryptionKey = null;
    this.storageUsage = new Map();
    this.accessLog = [];
    
    this.init();
  }
  
  // 初始化
  async init() {
    await this.initializeEncryption();
    this.setupStorageMonitoring();
    this.setupQuotaManagement();
    this.cleanupExpiredData();
  }
  
  // 初始化加密
  async initializeEncryption() {
    if (!this.config.enableEncryption) return;
    
    try {
      // 生成或获取加密密钥
      if (this.config.encryptionKey) {
        this.encryptionKey = await this.importKey(this.config.encryptionKey);
      } else {
        this.encryptionKey = await this.generateKey();
      }
    } catch (error) {
      console.error('Failed to initialize encryption:', error);
      this.config.enableEncryption = false;
    }
  }
  
  // 生成加密密钥
  async generateKey() {
    return await crypto.subtle.generateKey(
      {
        name: 'AES-GCM',
        length: 256
      },
      true,
      ['encrypt', 'decrypt']
    );
  }
  
  // 导入密钥
  async importKey(keyData) {
    return await crypto.subtle.importKey(
      'raw',
      keyData,
      {
        name: 'AES-GCM',
        length: 256
      },
      true,
      ['encrypt', 'decrypt']
    );
  }
  
  // 加密数据
  async encryptData(data) {
    if (!this.config.enableEncryption || !this.encryptionKey) {
      return data;
    }
    
    try {
      const encoder = new TextEncoder();
      const dataBuffer = encoder.encode(JSON.stringify(data));
      
      const iv = crypto.getRandomValues(new Uint8Array(12));
      
      const encryptedBuffer = await crypto.subtle.encrypt(
        {
          name: 'AES-GCM',
          iv: iv
        },
        this.encryptionKey,
        dataBuffer
      );
      
      // 组合IV和加密数据
      const combined = new Uint8Array(iv.length + encryptedBuffer.byteLength);
      combined.set(iv);
      combined.set(new Uint8Array(encryptedBuffer), iv.length);
      
      return btoa(String.fromCharCode(...combined));
    } catch (error) {
      console.error('Encryption failed:', error);
      return data;
    }
  }
  
  // 解密数据
  async decryptData(encryptedData) {
    if (!this.config.enableEncryption || !this.encryptionKey || typeof encryptedData !== 'string') {
      return encryptedData;
    }
    
    try {
      const combined = new Uint8Array(
        atob(encryptedData).split('').map(char => char.charCodeAt(0))
      );
      
      const iv = combined.slice(0, 12);
      const encryptedBuffer = combined.slice(12);
      
      const decryptedBuffer = await crypto.subtle.decrypt(
        {
          name: 'AES-GCM',
          iv: iv
        },
        this.encryptionKey,
        encryptedBuffer
      );
      
      const decoder = new TextDecoder();
      const decryptedText = decoder.decode(decryptedBuffer);
      
      return JSON.parse(decryptedText);
    } catch (error) {
      console.error('Decryption failed:', error);
      return null;
    }
  }
  
  // 压缩数据
  compressData(data) {
    if (!this.config.enableCompression) return data;
    
    try {
      // 简单的字符串压缩（实际项目中可使用更好的压缩算法）
      const jsonString = JSON.stringify(data);
      return this.simpleCompress(jsonString);
    } catch (error) {
      console.error('Compression failed:', error);
      return data;
    }
  }
  
  // 解压数据
  decompressData(compressedData) {
    if (!this.config.enableCompression || typeof compressedData !== 'string') {
      return compressedData;
    }
    
    try {
      const decompressed = this.simpleDecompress(compressedData);
      return JSON.parse(decompressed);
    } catch (error) {
      console.error('Decompression failed:', error);
      return compressedData;
    }
  }
  
  // 简单压缩算法
  simpleCompress(str) {
    const compressed = [];
    let i = 0;
    
    while (i < str.length) {
      let match = '';
      let matchLength = 0;
      
      // 查找重复模式
      for (let j = i + 1; j < Math.min(i + 255, str.length); j++) {
        const pattern = str.substring(i, j);
        const nextOccurrence = str.indexOf(pattern, j);
        
        if (nextOccurrence !== -1 && pattern.length > matchLength) {
          match = pattern;
          matchLength = pattern.length;
        }
      }
      
      if (matchLength > 3) {
        compressed.push(`[${matchLength}:${match}]`);
        i += matchLength;
      } else {
        compressed.push(str[i]);
        i++;
      }
    }
    
    return compressed.join('');
  }
  
  // 简单解压算法
  simpleDecompress(compressed) {
    return compressed.replace(/\[(\d+):([^\]]+)\]/g, (match, length, pattern) => {
      return pattern;
    });
  }
  
  // 创建数据包装器
  createDataWrapper(data) {
    const wrapper = {
      data: data,
      timestamp: Date.now(),
      version: '1.0'
    };
    
    if (this.config.enableExpiration) {
      wrapper.expiresAt = Date.now() + this.config.defaultTTL;
    }
    
    if (this.config.enableIntegrityCheck) {
      wrapper.checksum = this.calculateChecksum(data);
    }
    
    return wrapper;
  }
  
  // 计算校验和
  calculateChecksum(data) {
    const str = JSON.stringify(data);
    let hash = 0;
    
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // 转换为32位整数
    }
    
    return hash.toString(36);
  }
  
  // 验证数据完整性
  verifyIntegrity(wrapper) {
    if (!this.config.enableIntegrityCheck || !wrapper.checksum) {
      return true;
    }
    
    const calculatedChecksum = this.calculateChecksum(wrapper.data);
    return calculatedChecksum === wrapper.checksum;
  }
  
  // 检查数据是否过期
  isExpired(wrapper) {
    if (!this.config.enableExpiration || !wrapper.expiresAt) {
      return false;
    }
    
    return Date.now() > wrapper.expiresAt;
  }
  
  // 安全存储数据
  async setItem(key, value, options = {}) {
    try {
      // 检查存储配额
      if (!this.checkStorageQuota(key, value)) {
        throw new Error('Storage quota exceeded');
      }
      
      // 创建数据包装器
      const wrapper = this.createDataWrapper(value);
      
      // 应用过期时间选项
      if (options.ttl) {
        wrapper.expiresAt = Date.now() + options.ttl;
      }
      
      // 压缩数据
      let processedData = this.compressData(wrapper);
      
      // 加密数据
      processedData = await this.encryptData(processedData);
      
      // 选择存储方式
      const storage = options.secure ? this.getSecureStorage() : localStorage;
      storage.setItem(key, JSON.stringify(processedData));
      
      // 记录存储使用情况
      this.recordStorageUsage(key, JSON.stringify(processedData).length);
      
      // 记录访问日志
      this.logAccess('set', key);
      
      return true;
    } catch (error) {
      console.error('Failed to store data:', error);
      return false;
    }
  }
  
  // 安全获取数据
  async getItem(key, options = {}) {
    try {
      // 选择存储方式
      const storage = options.secure ? this.getSecureStorage() : localStorage;
      const storedData = storage.getItem(key);
      
      if (!storedData) {
        return null;
      }
      
      // 解析存储的数据
      let processedData = JSON.parse(storedData);
      
      // 解密数据
      processedData = await this.decryptData(processedData);
      
      // 解压数据
      const wrapper = this.decompressData(processedData);
      
      // 检查数据完整性
      if (!this.verifyIntegrity(wrapper)) {
        console.warn('Data integrity check failed for key:', key);
        this.removeItem(key, options);
        return null;
      }
      
      // 检查是否过期
      if (this.isExpired(wrapper)) {
        this.removeItem(key, options);
        return null;
      }
      
      // 记录访问日志
      this.logAccess('get', key);
      
      return wrapper.data;
    } catch (error) {
      console.error('Failed to retrieve data:', error);
      return null;
    }
  }
  
  // 移除数据
  removeItem(key, options = {}) {
    try {
      const storage = options.secure ? this.getSecureStorage() : localStorage;
      storage.removeItem(key);
      
      // 更新存储使用情况
      this.storageUsage.delete(key);
      
      // 记录访问日志
      this.logAccess('remove', key);
      
      return true;
    } catch (error) {
      console.error('Failed to remove data:', error);
      return false;
    }
  }
  
  // 获取安全存储
  getSecureStorage() {
    // 在实际应用中，这里可以返回更安全的存储方式
    // 比如IndexedDB或者内存存储
    return sessionStorage;
  }
  
  // 检查存储配额
  checkStorageQuota(key, value) {
    const dataSize = JSON.stringify(value).length;
    const currentUsage = this.getCurrentStorageUsage();
    
    if (currentUsage + dataSize > this.config.storageQuotaLimit) {
      // 尝试清理过期数据
      this.cleanupExpiredData();
      
      // 重新检查
      const newUsage = this.getCurrentStorageUsage();
      if (newUsage + dataSize > this.config.storageQuotaLimit) {
        return false;
      }
    }
    
    return true;
  }
  
  // 获取当前存储使用情况
  getCurrentStorageUsage() {
    let totalSize = 0;
    
    for (const [key, size] of this.storageUsage) {
      totalSize += size;
    }
    
    return totalSize;
  }
  
  // 记录存储使用情况
  recordStorageUsage(key, size) {
    this.storageUsage.set(key, size);
  }
  
  // 设置存储监控
  setupStorageMonitoring() {
    // 监控存储事件
    window.addEventListener('storage', (event) => {
      this.logAccess('external_change', event.key);
    });
  }
  
  // 设置配额管理
  setupQuotaManagement() {
    // 定期检查存储使用情况
    setInterval(() => {
      const usage = this.getCurrentStorageUsage();
      const limit = this.config.storageQuotaLimit;
      
      if (usage > limit * 0.8) {
        console.warn('Storage usage approaching limit:', usage, '/', limit);
        this.cleanupExpiredData();
      }
    }, 60000); // 每分钟检查一次
  }
  
  // 清理过期数据
  async cleanupExpiredData() {
    const keys = Object.keys(localStorage);
    
    for (const key of keys) {
      try {
        const data = await this.getItem(key);
        // getItem会自动处理过期数据的清理
      } catch (error) {
        // 如果数据损坏，直接删除
        localStorage.removeItem(key);
        this.storageUsage.delete(key);
      }
    }
  }
  
  // 记录访问日志
  logAccess(action, key) {
    this.accessLog.push({
      action: action,
      key: key,
      timestamp: Date.now(),
      userAgent: navigator.userAgent
    });
    
    // 保持日志在合理范围内
    if (this.accessLog.length > 1000) {
      this.accessLog.shift();
    }
  }
  
  // 获取存储统计
  getStorageStats() {
    return {
      totalKeys: this.storageUsage.size,
      totalSize: this.getCurrentStorageUsage(),
      quotaLimit: this.config.storageQuotaLimit,
      usagePercentage: (this.getCurrentStorageUsage() / this.config.storageQuotaLimit) * 100,
      accessLog: this.accessLog.length,
      recentAccess: this.accessLog.slice(-10)
    };
  }
  
  // 获取状态
  getStatus() {
    return {
      encryptionEnabled: this.config.enableEncryption,
      compressionEnabled: this.config.enableCompression,
      expirationEnabled: this.config.enableExpiration,
      integrityCheckEnabled: this.config.enableIntegrityCheck,
      stats: this.getStorageStats(),
      config: this.config
    };
  }
  
  // 清理
  cleanup() {
    this.storageUsage.clear();
    this.accessLog = [];
  }
}
```

## 8. 安全通信管理

### 8.1 安全通信管理器

```javascript
// 安全通信管理器
class SecureCommunicationManager {
  constructor(config = {}) {
    this.config = {
      enableHTTPS: true,
      enableCertificatePinning: true,
      enableRequestSigning: true,
      enableResponseValidation: true,
      enableRateLimiting: true,
      maxRequestsPerMinute: 60,
      enableRequestLogging: true,
      trustedDomains: [],
      blockedDomains: [],
      enableCSRFProtection: true,
      enableCORS: true,
      allowedOrigins: [],
      ...config
    };
    
    this.requestHistory = [];
    this.rateLimitData = new Map();
    this.trustedCertificates = new Set();
    this.blockedRequests = [];
    
    this.init();
  }
  
  // 初始化
  init() {
    this.setupRequestInterception();
    this.setupResponseValidation();
    this.setupRateLimiting();
    this.setupCertificatePinning();
    this.setupCSRFProtection();
  }
  
  // 设置请求拦截
  setupRequestInterception() {
    // 拦截fetch请求
    const originalFetch = window.fetch;
    
    window.fetch = async (url, options = {}) => {
      try {
        // 验证请求
        const validationResult = await this.validateRequest(url, options);
        if (!validationResult.isValid) {
          throw new Error(`Request blocked: ${validationResult.reason}`);
        }
        
        // 应用安全选项
        const secureOptions = this.applySecurityOptions(options);
        
        // 记录请求
        this.logRequest(url, secureOptions);
        
        // 执行请求
        const response = await originalFetch(url, secureOptions);
        
        // 验证响应
        const responseValidation = await this.validateResponse(response);
        if (!responseValidation.isValid) {
          throw new Error(`Response validation failed: ${responseValidation.reason}`);
        }
        
        return response;
      } catch (error) {
        this.handleRequestError(url, options, error);
        throw error;
      }
    };
    
    // 拦截XMLHttpRequest
    const originalXHROpen = XMLHttpRequest.prototype.open;
    const originalXHRSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
      this._secureUrl = url;
      this._secureMethod = method;
      return originalXHROpen.call(this, method, url, async, user, password);
    };
    
    XMLHttpRequest.prototype.send = function(data) {
      const manager = window.secureCommunicationManager;
      if (manager) {
        const validationResult = manager.validateRequestSync(this._secureUrl, {
          method: this._secureMethod,
          body: data
        });
        
        if (!validationResult.isValid) {
          throw new Error(`XHR Request blocked: ${validationResult.reason}`);
        }
        
        manager.logRequest(this._secureUrl, {
          method: this._secureMethod,
          body: data
        });
      }
      
      return originalXHRSend.call(this, data);
    };
  }
  
  // 验证请求
  async validateRequest(url, options) {
    try {
      const urlObj = new URL(url, window.location.href);
      
      // 检查HTTPS
      if (this.config.enableHTTPS && urlObj.protocol !== 'https:' && urlObj.hostname !== 'localhost') {
        return {
          isValid: false,
          reason: 'HTTPS required for external requests'
        };
      }
      
      // 检查域名白名单
      if (this.config.trustedDomains.length > 0) {
        const isTrusted = this.config.trustedDomains.some(domain => 
          urlObj.hostname === domain || urlObj.hostname.endsWith('.' + domain)
        );
        
        if (!isTrusted) {
          return {
            isValid: false,
            reason: 'Domain not in trusted list'
          };
        }
      }
      
      // 检查域名黑名单
      if (this.config.blockedDomains.length > 0) {
        const isBlocked = this.config.blockedDomains.some(domain => 
          urlObj.hostname === domain || urlObj.hostname.endsWith('.' + domain)
        );
        
        if (isBlocked) {
          return {
            isValid: false,
            reason: 'Domain is blocked'
          };
        }
      }
      
      // 检查速率限制
      if (this.config.enableRateLimiting) {
        const rateLimitResult = this.checkRateLimit(urlObj.hostname);
        if (!rateLimitResult.allowed) {
          return {
            isValid: false,
            reason: 'Rate limit exceeded'
          };
        }
      }
      
      // 检查请求内容
      if (options.body) {
        const contentValidation = this.validateRequestContent(options.body);
        if (!contentValidation.isValid) {
          return contentValidation;
        }
      }
      
      return { isValid: true };
    } catch (error) {
      return {
        isValid: false,
        reason: `Validation error: ${error.message}`
      };
    }
  }
  
  // 同步验证请求（用于XHR）
  validateRequestSync(url, options) {
    try {
      const urlObj = new URL(url, window.location.href);
      
      // 基本验证（简化版）
      if (this.config.enableHTTPS && urlObj.protocol !== 'https:' && urlObj.hostname !== 'localhost') {
        return {
          isValid: false,
          reason: 'HTTPS required'
        };
      }
      
      return { isValid: true };
    } catch (error) {
      return {
        isValid: false,
        reason: error.message
      };
    }
  }
  
  // 验证请求内容
  validateRequestContent(body) {
    if (typeof body === 'string') {
      // 检查敏感信息
      const sensitivePatterns = [
        /password\s*[=:]\s*["']?[^"'\s]+/i,
        /api[_-]?key\s*[=:]\s*["']?[^"'\s]+/i,
        /secret\s*[=:]\s*["']?[^"'\s]+/i,
        /token\s*[=:]\s*["']?[^"'\s]+/i
      ];
      
      for (const pattern of sensitivePatterns) {
        if (pattern.test(body)) {
          return {
            isValid: false,
            reason: 'Sensitive information detected in request body'
          };
        }
      }
      
      // 检查SQL注入
      const sqlPatterns = [
        /(union|select|insert|delete|update|drop|create|alter)\s+/i,
        /('|(\-\-)|(;)|(\||\|)|(\*|\*))/
      ];
      
      for (const pattern of sqlPatterns) {
        if (pattern.test(body)) {
          return {
            isValid: false,
            reason: 'Potential SQL injection detected'
          };
        }
      }
    }
    
    return { isValid: true };
  }
  
  // 应用安全选项
  applySecurityOptions(options) {
    const secureOptions = { ...options };
    
    // 设置默认headers
    if (!secureOptions.headers) {
      secureOptions.headers = {};
    }
    
    // 添加安全headers
    secureOptions.headers['X-Requested-With'] = 'XMLHttpRequest';
    secureOptions.headers['Cache-Control'] = 'no-cache';
    
    // CSRF保护
    if (this.config.enableCSRFProtection) {
      const csrfToken = this.getCSRFToken();
      if (csrfToken) {
        secureOptions.headers['X-CSRF-Token'] = csrfToken;
      }
    }
    
    // 设置credentials
    if (!secureOptions.credentials) {
      secureOptions.credentials = 'same-origin';
    }
    
    // 请求签名
    if (this.config.enableRequestSigning) {
      const signature = this.signRequest(secureOptions);
      secureOptions.headers['X-Request-Signature'] = signature;
    }
    
    return secureOptions;
  }
  
  // 获取CSRF令牌
  getCSRFToken() {
    // 从meta标签获取
    const metaTag = document.querySelector('meta[name="csrf-token"]');
    if (metaTag) {
      return metaTag.getAttribute('content');
    }
    
    // 从cookie获取
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
      const [name, value] = cookie.trim().split('=');
      if (name === 'XSRF-TOKEN' || name === 'csrf_token') {
        return decodeURIComponent(value);
      }
    }
    
    return null;
  }
  
  // 签名请求
  signRequest(options) {
    const timestamp = Date.now();
    const method = options.method || 'GET';
    const body = options.body || '';
    
    // 创建签名字符串
    const signatureString = `${method}:${timestamp}:${body}`;
    
    // 简单的哈希签名（实际项目中应使用更安全的签名算法）
    let hash = 0;
    for (let i = 0; i < signatureString.length; i++) {
      const char = signatureString.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    
    return `${timestamp}.${hash.toString(36)}`;
  }
  
  // 设置响应验证
  setupResponseValidation() {
    // 响应验证在fetch拦截中处理
  }
  
  // 验证响应
  async validateResponse(response) {
    try {
      // 检查状态码
      if (!response.ok) {
        return {
          isValid: false,
          reason: `HTTP error: ${response.status} ${response.statusText}`
        };
      }
      
      // 检查Content-Type
      const contentType = response.headers.get('Content-Type');
      if (contentType && !this.isAllowedContentType(contentType)) {
        return {
          isValid: false,
          reason: `Disallowed content type: ${contentType}`
        };
      }
      
      // 检查安全headers
      const securityHeaders = [
        'X-Content-Type-Options',
        'X-Frame-Options',
        'X-XSS-Protection'
      ];
      
      const missingHeaders = securityHeaders.filter(header => 
        !response.headers.has(header)
      );
      
      if (missingHeaders.length > 0) {
        console.warn('Missing security headers:', missingHeaders);
      }
      
      return { isValid: true };
    } catch (error) {
      return {
        isValid: false,
        reason: `Response validation error: ${error.message}`
      };
    }
  }
  
  // 检查允许的内容类型
  isAllowedContentType(contentType) {
    const allowedTypes = [
      'application/json',
      'text/html',
      'text/plain',
      'text/css',
      'text/javascript',
      'application/javascript',
      'image/',
      'font/'
    ];
    
    return allowedTypes.some(type => contentType.includes(type));
  }
  
  // 设置速率限制
  setupRateLimiting() {
    if (!this.config.enableRateLimiting) return;
    
    // 定期清理过期的速率限制数据
    setInterval(() => {
      this.cleanupRateLimitData();
    }, 60000); // 每分钟清理一次
  }
  
  // 检查速率限制
  checkRateLimit(hostname) {
    if (!this.config.enableRateLimiting) {
      return { allowed: true };
    }
    
    const now = Date.now();
    const windowStart = now - 60000; // 1分钟窗口
    
    if (!this.rateLimitData.has(hostname)) {
      this.rateLimitData.set(hostname, []);
    }
    
    const requests = this.rateLimitData.get(hostname);
    
    // 移除过期请求
    const validRequests = requests.filter(timestamp => timestamp > windowStart);
    
    // 检查是否超过限制
    if (validRequests.length >= this.config.maxRequestsPerMinute) {
      return {
        allowed: false,
        resetTime: Math.min(...validRequests) + 60000
      };
    }
    
    // 添加当前请求
    validRequests.push(now);
    this.rateLimitData.set(hostname, validRequests);
    
    return { allowed: true };
  }
  
  // 清理速率限制数据
  cleanupRateLimitData() {
    const now = Date.now();
    const windowStart = now - 60000;
    
    for (const [hostname, requests] of this.rateLimitData) {
      const validRequests = requests.filter(timestamp => timestamp > windowStart);
      
      if (validRequests.length === 0) {
        this.rateLimitData.delete(hostname);
      } else {
        this.rateLimitData.set(hostname, validRequests);
      }
    }
  }
  
  // 设置证书固定
  setupCertificatePinning() {
    if (!this.config.enableCertificatePinning) return;
    
    // 在实际应用中，这里会实现证书固定逻辑
    // 由于浏览器限制，这里只是示例
    console.log('Certificate pinning enabled');
  }
  
  // 设置CSRF保护
  setupCSRFProtection() {
    if (!this.config.enableCSRFProtection) return;
    
    // 确保CSRF令牌存在
    if (!this.getCSRFToken()) {
      console.warn('CSRF token not found. CSRF protection may not work properly.');
    }
  }
  
  // 记录请求
  logRequest(url, options) {
    if (!this.config.enableRequestLogging) return;
    
    const logEntry = {
      url: url,
      method: options.method || 'GET',
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      referrer: document.referrer
    };
    
    this.requestHistory.push(logEntry);
    
    // 保持日志在合理范围内
    if (this.requestHistory.length > 1000) {
      this.requestHistory.shift();
    }
  }
  
  // 处理请求错误
  handleRequestError(url, options, error) {
    const errorEntry = {
      url: url,
      method: options.method || 'GET',
      error: error.message,
      timestamp: Date.now()
    };
    
    this.blockedRequests.push(errorEntry);
    
    // 触发错误事件
    const customEvent = new CustomEvent('secure-communication-error', {
      detail: errorEntry
    });
    document.dispatchEvent(customEvent);
    
    console.error('🛡️ Secure Communication Error:', errorEntry);
  }
  
  // 获取通信统计
  getCommunicationStats() {
    return {
      totalRequests: this.requestHistory.length,
      blockedRequests: this.blockedRequests.length,
      rateLimitedDomains: this.rateLimitData.size,
      recentRequests: this.requestHistory.slice(-10),
      recentBlocked: this.blockedRequests.slice(-10)
    };
  }
  
  // 获取状态
  getStatus() {
    return {
      httpsEnabled: this.config.enableHTTPS,
      certificatePinningEnabled: this.config.enableCertificatePinning,
      requestSigningEnabled: this.config.enableRequestSigning,
      responseValidationEnabled: this.config.enableResponseValidation,
      rateLimitingEnabled: this.config.enableRateLimiting,
      csrfProtectionEnabled: this.config.enableCSRFProtection,
      stats: this.getCommunicationStats(),
      config: this.config
    };
  }
  
  // 清理
  cleanup() {
    this.requestHistory = [];
    this.rateLimitData.clear();
    this.blockedRequests = [];
    this.trustedCertificates.clear();
  }
}
```

## 9. 依赖安全扫描

### 9.1 依赖安全扫描器

```javascript
// 依赖安全扫描器
class DependencySecurityScanner {
  constructor(config = {}) {
    this.config = {
      enableRealTimeScanning: true,
      enableVulnerabilityDatabase: true,
      enableLicenseCheck: true,
      enableOutdatedCheck: true,
      scanInterval: 3600000, // 1小时
      vulnerabilityApiUrl: 'https://api.security-scanner.com/vulnerabilities',
      allowedLicenses: ['MIT', 'Apache-2.0', 'BSD-3-Clause', 'ISC'],
      blockedPackages: [],
      ...config
    };
    
    this.vulnerabilities = [];
    this.dependencies = new Map();
    this.scanResults = [];
    this.licenseViolations = [];
    
    this.init();
  }
  
  // 初始化
  init() {
    this.scanCurrentDependencies();
    this.setupPeriodicScanning();
    this.setupRealTimeMonitoring();
  }
  
  // 扫描当前依赖
  async scanCurrentDependencies() {
    try {
      // 获取当前加载的脚本
      const scripts = document.querySelectorAll('script[src]');
      
      for (const script of scripts) {
        await this.analyzeScript(script.src);
      }
      
      // 扫描动态导入的模块
      this.monitorDynamicImports();
      
      // 生成扫描报告
      this.generateScanReport();
    } catch (error) {
      console.error('Dependency scan failed:', error);
    }
  }
  
  // 分析脚本
  async analyzeScript(src) {
    try {
      const dependency = this.parseDependencyInfo(src);
      
      if (dependency) {
        this.dependencies.set(dependency.name, dependency);
        
        // 检查漏洞
        await this.checkVulnerabilities(dependency);
        
        // 检查许可证
        await this.checkLicense(dependency);
        
        // 检查是否过时
        await this.checkOutdated(dependency);
        
        // 检查是否被阻止
        this.checkBlocked(dependency);
      }
    } catch (error) {
      console.error('Script analysis failed:', src, error);
    }
  }
  
  // 解析依赖信息
  parseDependencyInfo(src) {
    try {
      const url = new URL(src, window.location.href);
      
      // 检查是否为CDN链接
      const cdnPatterns = [
        {
          pattern: /\/\/cdn\.jsdelivr\.net\/npm\/([^@\/]+)@([^\/]+)/,
          nameIndex: 1,
          versionIndex: 2
        },
        {
          pattern: /\/\/unpkg\.com\/([^@\/]+)@([^\/]+)/,
          nameIndex: 1,
          versionIndex: 2
        },
        {
          pattern: /\/\/cdnjs\.cloudflare\.com\/ajax\/libs\/([^\/]+)\/([^\/]+)/,
          nameIndex: 1,
          versionIndex: 2
        }
      ];
      
      for (const { pattern, nameIndex, versionIndex } of cdnPatterns) {
        const match = src.match(pattern);
        if (match) {
          return {
            name: match[nameIndex],
            version: match[versionIndex],
            source: 'cdn',
            url: src,
            loadTime: Date.now()
          };
        }
      }
      
      // 本地脚本
      if (url.origin === window.location.origin) {
        const pathParts = url.pathname.split('/');
        const filename = pathParts[pathParts.length - 1];
        
        return {
          name: filename.replace(/\.(js|min\.js)$/, ''),
          version: 'unknown',
          source: 'local',
          url: src,
          loadTime: Date.now()
        };
      }
      
      return null;
    } catch (error) {
      console.error('Failed to parse dependency info:', src, error);
      return null;
    }
  }
  
  // 检查漏洞
  async checkVulnerabilities(dependency) {
    if (!this.config.enableVulnerabilityDatabase) return;
    
    try {
      // 模拟漏洞数据库查询
      const vulnerabilityData = await this.queryVulnerabilityDatabase(dependency);
      
      if (vulnerabilityData && vulnerabilityData.vulnerabilities.length > 0) {
        vulnerabilityData.vulnerabilities.forEach(vuln => {
          this.vulnerabilities.push({
            dependency: dependency.name,
            version: dependency.version,
            vulnerability: vuln,
            severity: vuln.severity,
            discoveredAt: Date.now()
          });
        });
        
        console.warn(`🚨 Vulnerabilities found in ${dependency.name}@${dependency.version}:`, vulnerabilityData.vulnerabilities);
      }
    } catch (error) {
      console.error('Vulnerability check failed:', dependency.name, error);
    }
  }
  
  // 查询漏洞数据库
  async queryVulnerabilityDatabase(dependency) {
    // 模拟漏洞数据库查询
    // 实际应用中会调用真实的漏洞数据库API
    
    const knownVulnerabilities = {
      'jquery': {
        '1.0.0': [{
          id: 'CVE-2020-11022',
          severity: 'medium',
          description: 'XSS vulnerability in jQuery',
          fixedIn: '3.5.0'
        }],
        '2.0.0': [{
          id: 'CVE-2020-11023',
          severity: 'medium',
          description: 'XSS vulnerability in jQuery',
          fixedIn: '3.5.0'
        }]
      },
      'lodash': {
        '4.17.15': [{
          id: 'CVE-2021-23337',
          severity: 'high',
          description: 'Prototype pollution vulnerability',
          fixedIn: '4.17.21'
        }]
      }
    };
    
    const packageVulns = knownVulnerabilities[dependency.name];
    if (packageVulns && packageVulns[dependency.version]) {
      return {
        vulnerabilities: packageVulns[dependency.version]
      };
    }
    
    return { vulnerabilities: [] };
  }
  
  // 检查许可证
  async checkLicense(dependency) {
    if (!this.config.enableLicenseCheck) return;
    
    try {
      const licenseInfo = await this.getLicenseInfo(dependency);
      
      if (licenseInfo && !this.config.allowedLicenses.includes(licenseInfo.license)) {
        this.licenseViolations.push({
          dependency: dependency.name,
          version: dependency.version,
          license: licenseInfo.license,
          discoveredAt: Date.now()
        });
        
        console.warn(`⚖️ License violation: ${dependency.name}@${dependency.version} uses ${licenseInfo.license}`);
      }
    } catch (error) {
      console.error('License check failed:', dependency.name, error);
    }
  }
  
  // 获取许可证信息
  async getLicenseInfo(dependency) {
    // 模拟许可证信息查询
    const knownLicenses = {
      'jquery': 'MIT',
      'lodash': 'MIT',
      'react': 'MIT',
      'vue': 'MIT',
      'angular': 'MIT'
    };
    
    const license = knownLicenses[dependency.name] || 'Unknown';
    
    return {
      license: license,
      url: `https://opensource.org/licenses/${license}`
    };
  }
  
  // 检查是否过时
  async checkOutdated(dependency) {
    if (!this.config.enableOutdatedCheck) return;
    
    try {
      const latestVersion = await this.getLatestVersion(dependency);
      
      if (latestVersion && this.isVersionOutdated(dependency.version, latestVersion)) {
        console.info(`📦 Outdated dependency: ${dependency.name}@${dependency.version} (latest: ${latestVersion})`);
      }
    } catch (error) {
      console.error('Outdated check failed:', dependency.name, error);
    }
  }
  
  // 获取最新版本
  async getLatestVersion(dependency) {
    // 模拟版本查询
    const latestVersions = {
      'jquery': '3.6.0',
      'lodash': '4.17.21',
      'react': '18.2.0',
      'vue': '3.2.45',
      'angular': '15.0.0'
    };
    
    return latestVersions[dependency.name] || null;
  }
  
  // 检查版本是否过时
  isVersionOutdated(currentVersion, latestVersion) {
    // 简单的版本比较（实际应用中应使用更复杂的版本比较逻辑）
    const current = currentVersion.split('.').map(Number);
    const latest = latestVersion.split('.').map(Number);
    
    for (let i = 0; i < Math.max(current.length, latest.length); i++) {
      const currentPart = current[i] || 0;
      const latestPart = latest[i] || 0;
      
      if (currentPart < latestPart) {
        return true;
      } else if (currentPart > latestPart) {
        return false;
      }
    }
    
    return false;
  }
  
  // 检查是否被阻止
  checkBlocked(dependency) {
    if (this.config.blockedPackages.includes(dependency.name)) {
      console.error(`🚫 Blocked dependency detected: ${dependency.name}`);
      
      // 触发阻止事件
      const event = new CustomEvent('dependency-blocked', {
        detail: dependency
      });
      document.dispatchEvent(event);
    }
  }
  
  // 监控动态导入
  monitorDynamicImports() {
    // 拦截动态import
    const originalImport = window.import || (() => {});
    
    if (typeof originalImport === 'function') {
      window.import = async (specifier) => {
        console.log('Dynamic import detected:', specifier);
        
        // 分析动态导入的模块
        if (typeof specifier === 'string') {
          await this.analyzeScript(specifier);
        }
        
        return originalImport(specifier);
      };
    }
  }
  
  // 设置定期扫描
  setupPeriodicScanning() {
    setInterval(() => {
      this.scanCurrentDependencies();
    }, this.config.scanInterval);
  }
  
  // 设置实时监控
  setupRealTimeMonitoring() {
    if (!this.config.enableRealTimeScanning) return;
    
    // 监控新脚本的添加
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE && node.tagName === 'SCRIPT' && node.src) {
            this.analyzeScript(node.src);
          }
        });
      });
    });
    
    observer.observe(document.head, {
      childList: true,
      subtree: true
    });
  }
  
  // 生成扫描报告
  generateScanReport() {
    const report = {
      timestamp: Date.now(),
      totalDependencies: this.dependencies.size,
      vulnerabilities: this.vulnerabilities.length,
      licenseViolations: this.licenseViolations.length,
      dependencies: Array.from(this.dependencies.values()),
      vulnerabilityDetails: this.vulnerabilities,
      licenseViolationDetails: this.licenseViolations,
      summary: {
        highSeverityVulns: this.vulnerabilities.filter(v => v.severity === 'high').length,
        mediumSeverityVulns: this.vulnerabilities.filter(v => v.severity === 'medium').length,
        lowSeverityVulns: this.vulnerabilities.filter(v => v.severity === 'low').length
      }
    };
    
    this.scanResults.push(report);
    
    // 保持报告在合理范围内
    if (this.scanResults.length > 10) {
      this.scanResults.shift();
    }
    
    console.log('🔍 Dependency scan completed:', report.summary);
    
    return report;
  }
  
  // 获取扫描统计
  getScanStats() {
    const latestReport = this.scanResults[this.scanResults.length - 1];
    
    return {
      totalScans: this.scanResults.length,
      latestScan: latestReport ? latestReport.timestamp : null,
      totalDependencies: this.dependencies.size,
      totalVulnerabilities: this.vulnerabilities.length,
      totalLicenseViolations: this.licenseViolations.length,
      summary: latestReport ? latestReport.summary : null
    };
  }
  
  // 获取状态
  getStatus() {
    return {
      realTimeScanningEnabled: this.config.enableRealTimeScanning,
      vulnerabilityDatabaseEnabled: this.config.enableVulnerabilityDatabase,
      licenseCheckEnabled: this.config.enableLicenseCheck,
      outdatedCheckEnabled: this.config.enableOutdatedCheck,
      stats: this.getScanStats(),
      config: this.config
    };
  }
  
  // 清理
  cleanup() {
    this.vulnerabilities = [];
    this.dependencies.clear();
    this.scanResults = [];
    this.licenseViolations = [];
  }
}
```

## 10. 最佳实践与总结

### 10.1 安全防护架构设计原则

#### 1. 深度防御策略

```javascript
// 多层安全防护管理器
class LayeredSecurityManager {
  constructor() {
    this.layers = {
      frontend: new FrontendSecurityManager(),
      xss: new XSSProtection(),
      csrf: new CSRFProtection(),
      clickjacking: new ClickjackingProtection(),
      csp: new ContentSecurityPolicyManager(),
      input: new InputValidationManager(),
      storage: new SecureStorageManager(),
      communication: new SecureCommunicationManager(),
      dependency: new DependencySecurityScanner()
    };
    
    this.init();
  }
  
  init() {
    // 初始化所有安全层
    Object.values(this.layers).forEach(layer => {
      if (layer.init && typeof layer.init === 'function') {
        layer.init();
      }
    });
    
    // 设置层间协调
    this.setupLayerCoordination();
  }
  
  setupLayerCoordination() {
    // 安全事件协调
    document.addEventListener('security-threat-detected', (event) => {
      this.handleSecurityThreat(event.detail);
    });
  }
  
  handleSecurityThreat(threat) {
    console.warn('🚨 Security threat detected:', threat);
    
    // 根据威胁类型采取相应措施
    switch (threat.type) {
      case 'xss':
        this.layers.xss.handleThreat(threat);
        break;
      case 'csrf':
        this.layers.csrf.handleThreat(threat);
        break;
      case 'clickjacking':
        this.layers.clickjacking.handleThreat(threat);
        break;
      default:
        this.layers.frontend.handleThreat(threat);
    }
  }
  
  getSecurityStatus() {
    const status = {};
    
    Object.entries(this.layers).forEach(([name, layer]) => {
      if (layer.getStatus && typeof layer.getStatus === 'function') {
        status[name] = layer.getStatus();
      }
    });
    
    return status;
  }
}
```

#### 2. 性能优化策略

- **异步处理**: 安全检查不阻塞主线程
- **缓存机制**: 缓存验证结果和安全策略
- **批量处理**: 批量处理安全事件
- **懒加载**: 按需加载安全模块

#### 3. 监控与告警

- **实时监控**: 监控安全事件和威胁
- **日志记录**: 详细记录安全相关操作
- **告警机制**: 及时通知安全威胁
- **统计分析**: 分析安全趋势和模式

### 10.2 开发团队最佳实践

#### 1. 安全开发流程

- **安全需求分析**: 在项目初期识别安全需求
- **威胁建模**: 分析潜在的安全威胁
- **安全编码**: 遵循安全编码规范
- **安全测试**: 进行全面的安全测试
- **安全审计**: 定期进行安全审计

#### 2. 代码审查要点

- **输入验证**: 检查所有用户输入的验证
- **输出编码**: 确保输出内容正确编码
- **权限控制**: 验证权限控制逻辑
- **敏感数据**: 检查敏感数据处理
- **第三方依赖**: 审查第三方库的安全性

#### 3. 安全培训计划

- **基础安全知识**: OWASP Top 10等
- **前端安全特点**: XSS、CSRF等前端特有威胁
- **安全工具使用**: 安全扫描和测试工具
- **应急响应**: 安全事件处理流程

### 10.3 技术选型建议

#### 1. 框架选择

- **React**: 内置XSS防护，但需注意dangerouslySetInnerHTML
- **Vue**: 模板系统提供基础防护
- **Angular**: 内置安全机制，如DomSanitizer

#### 2. 工具推荐

- **ESLint Security Plugin**: 静态代码安全检查
- **Snyk**: 依赖漏洞扫描
- **OWASP ZAP**: Web应用安全测试
- **Content Security Policy**: 内容安全策略

### 10.4 未来发展趋势

#### 1. AI驱动的安全防护

- **智能威胁检测**: 使用机器学习检测异常行为
- **自适应防护**: 根据威胁情况自动调整防护策略
- **预测性安全**: 预测潜在的安全威胁

#### 2. 零信任架构

- **持续验证**: 持续验证用户和设备
- **最小权限**: 最小化访问权限
- **微分段**: 网络和应用微分段

#### 3. 隐私保护增强

- **数据最小化**: 只收集必要的数据
- **本地处理**: 在客户端处理敏感数据
- **加密传输**: 端到端加密通信

### 10.5 核心价值与收益

#### 1. 业务价值

- **用户信任**: 提升用户对产品的信任度
- **合规要求**: 满足法律法规要求
- **品牌保护**: 避免安全事件对品牌的损害
- **成本节约**: 预防安全事件的成本远低于事后处理

#### 2. 技术收益

- **系统稳定性**: 提高系统的稳定性和可靠性
- **开发效率**: 标准化的安全组件提高开发效率
- **维护成本**: 降低安全维护成本
- **技术债务**: 减少安全相关的技术债务

## 结语

前端安全防护是一个复杂而重要的技术领域，需要从多个维度进行综合考虑。本文提供的完整解决方案涵盖了XSS、CSRF、点击劫持、CSP、输入验证、安全存储、安全通信和依赖安全等关键方面。

通过实施这些安全措施，可以显著提升Web应用的安全性，保护用户数据和隐私。同时，建立完善的安全开发流程和监控体系，能够持续改进安全防护能力。

在实际项目中，应根据具体需求和风险评估，选择合适的安全策略和技术方案。安全防护是一个持续的过程，需要不断学习新的威胁和防护技术，保持安全防护能力的先进性。

## 相关文章

- [前端性能优化深度实践：从Core Web Vitals到用户体验提升的完整解决方案](./0052-前端性能优化深度实践：从Core%20Web%20Vitals到用户体验提升的完整解决方案.md)
- [前端工程化深度实践：从构建优化到CI-CD的完整解决方案](./0053-前端工程化深度实践：从构建优化到CI-CD的完整解决方案.md)
- [前端状态管理深度实践：从Redux到Zustand的现代化状态管理方案](./0054-前端状态管理深度实践：从Redux到Zustand的现代化状态管理方案.md)