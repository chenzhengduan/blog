# 前端安全防护指南：从XSS到CSRF的全面防御策略

> Web安全是前端开发不可忽视的重要环节。本文将深入探讨常见的前端安全威胁及其防御策略，帮助你构建更安全的Web应用。

## 引言

随着Web应用的日益复杂，前端安全问题也变得越来越突出。XSS、CSRF、点击劫持等攻击手段不断演变，对用户数据和隐私造成严重威胁。本文将系统性地介绍前端安全的基本概念、常见攻击手段和防御策略。

## 一、XSS（跨站脚本攻击）

### 1.1 XSS攻击类型

#### 反射型XSS
```javascript
// 恶意URL示例
https://example.com/search?q=<script>alert('XSS')</script>

// 服务器端代码（不安全）
app.get('/search', (req, res) => {
  const query = req.query.q;
  res.send(`<div>搜索结果: ${query}</div>`); // 直接渲染用户输入
});

// 防御方案
app.get('/search', (req, res) => {
  const query = req.query.q;
  const sanitizedQuery = escapeHtml(query); // HTML转义
  res.send(`<div>搜索结果: ${sanitizedQuery}</div>`);
});

function escapeHtml(unsafe) {
  return unsafe
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
```

#### 存储型XSS
```javascript
// 用户评论功能（不安全）
app.post('/comments', (req, res) => {
  const comment = req.body.comment;
  // 直接存储到数据库，没有过滤
  db.saveComment(comment);
  res.send('评论已发布');
});

app.get('/comments', (req, res) => {
  const comments = db.getComments();
  // 直接从数据库取出并渲染
  res.send(comments.map(c => `<div>${c.text}</div>`).join(''));
});

// 防御方案
const DOMPurify = require('dompurify');
const { JSDOM } = require('jsdom');
const window = new JSDOM('').window;
dompurify = DOMPurify(window);

app.post('/comments', (req, res) => {
  const comment = req.body.comment;
  const sanitizedComment = dompurify.sanitize(comment); // 清理HTML
  db.saveComment(sanitizedComment);
  res.send('评论已发布');
});
```

#### DOM型XSS
```javascript
// 前端代码（不安全）
function showMessage(message) {
  document.getElementById('message').innerHTML = message; // 危险操作
}

// 从URL参数获取消息
const urlParams = new URLSearchParams(window.location.search);
const message = urlParams.get('message');
showMessage(message);

// 防御方案
function safeShowMessage(message) {
  const messageElement = document.getElementById('message');
  messageElement.textContent = message; // 使用textContent而非innerHTML
}

// 或者使用DOMPurify
function safeShowMessageHTML(message) {
  const cleanMessage = DOMPurify.sanitize(message);
  document.getElementById('message').innerHTML = cleanMessage;
}
```

### 1.2 XSS防御策略

#### 输入验证和过滤
```javascript
// 前端输入验证
class InputValidator {
  static validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  static validateUsername(username) {
    // 只允许字母、数字、下划线
    const usernameRegex = /^[a-zA-Z0-9_]+$/;
    return usernameRegex.test(username) && username.length >= 3;
  }

  static sanitizeInput(input, type = 'text') {
    if (typeof input !== 'string') return input;

    switch (type) {
      case 'text':
        return input.replace(/[<>\"']/g, '');
      case 'number':
        return input.replace(/[^\d]/g, '');
      case 'email':
        return input.toLowerCase();
      default:
        return input;
    }
  }
}

// 使用示例
const userInput = '<script>alert("xss")</script>';
const sanitized = InputValidator.sanitizeInput(userInput);
console.log(sanitized); // "scriptalert(xss)/script"
```

#### 内容安全策略（CSP）
```javascript
// 服务器端设置CSP头
app.use((req, res, next) => {
  res.setHeader(
    'Content-Security-Policy',
    "default-src 'self'; " +
    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.example.com; " +
    "style-src 'self' 'unsafe-inline' https://cdn.example.com; " +
    "img-src 'self' data: https://cdn.example.com; " +
    "font-src 'self' https://cdn.example.com; " +
    "connect-src 'self' https://api.example.com; " +
    "frame-ancestors 'none'; " +
    "form-action 'self'; " +
    "base-uri 'self'; " +
    "require-trusted-types-for 'script';"
  );
  next();
});

// 或者使用CSP meta标签（适用于静态网站）
const cspMetaTag = document.createElement('meta');
cspMetaTag.httpEquiv = 'Content-Security-Policy';
cspMetaTag.content = "default-src 'self'; script-src 'self' 'unsafe-inline'";
document.head.appendChild(cspMetaTag);
```

#### HTTP安全头设置
```javascript
// 设置各种安全相关的HTTP头
app.use(helmet());

// 或者手动设置
app.use((req, res, next) => {
  // XSS保护
  res.setHeader('X-XSS-Protection', '1; mode=block');

  // 防止MIME类型嗅探
  res.setHeader('X-Content-Type-Options', 'nosniff');

  // 防止点击劫持
  res.setHeader('X-Frame-Options', 'DENY');

  // 引用策略
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');

  // 权限策略
  res.setHeader(
    'Permissions-Policy',
    'camera=(), microphone=(), geolocation=()'
  );

  next();
});
```

## 二、CSRF（跨站请求伪造）

### 2.1 CSRF攻击原理

```javascript
// 恶意网站代码
<img src="https://your-bank.com/transfer?to=attacker&amount=1000" />

// 或者使用JavaScript
fetch('https://your-bank.com/transfer', {
  method: 'POST',
  body: JSON.stringify({
    to: 'attacker',
    amount: 1000
  })
});
```

### 2.2 CSRF防御策略

#### CSRF Token
```javascript
// 生成CSRF Token
const crypto = require('crypto');

function generateCSRFToken() {
  return crypto.randomBytes(32).toString('hex');
}

// 验证CSRF Token
function verifyCSRFToken(token, sessionToken) {
  return token === sessionToken;
}

// Express中间件
const csrfProtection = (req, res, next) => {
  if (req.method === 'GET' || req.method === 'HEAD' || req.method === 'OPTIONS') {
    return next();
  }

  const token = req.body._csrf || req.headers['x-csrf-token'];
  const sessionToken = req.session.csrfToken;

  if (!token || !verifyCSRFToken(token, sessionToken)) {
    return res.status(403).json({ error: 'Invalid CSRF token' });
  }

  next();
};

// 使用中间件
app.use(session({
  secret: 'your-secret-key',
  resave: false,
  saveUninitialized: true
}));

app.use((req, res, next) => {
  // 为每个会话生成CSRF Token
  if (!req.session.csrfToken) {
    req.session.csrfToken = generateCSRFToken();
  }
  next();
});

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(csrfProtection);

// 在表单中包含CSRF Token
app.get('/form', (req, res) => {
  res.send(`
    <form method="POST" action="/submit">
      <input type="hidden" name="_csrf" value="${req.session.csrfToken}">
      <input type="text" name="data" placeholder="Enter data">
      <button type="submit">Submit</button>
    </form>
  `);
});
```

#### SameSite Cookie
```javascript
// 设置SameSite Cookie
app.use(session({
  secret: 'your-secret-key',
  cookie: {
    secure: process.env.NODE_ENV === 'production',
    httpOnly: true,
    sameSite: 'strict', // 或 'lax'
    maxAge: 24 * 60 * 60 * 1000 // 24 hours
  }
}));

// 或者手动设置Cookie
res.setHeader('Set-Cookie', [
  'sessionId=your-session-id; HttpOnly; Secure; SameSite=Strict',
  'csrfToken=your-csrf-token; HttpOnly; Secure; SameSite=Strict'
]);
```

#### 双重Cookie验证
```javascript
// 前端代码
function setupCSRFProtection() {
  // 从Cookie获取CSRF Token
  function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  }

  // 为所有请求添加CSRF Token
  const originalFetch = window.fetch;
  window.fetch = function(url, options = {}) {
    const csrfToken = getCookie('csrfToken');

    if (csrfToken) {
      options.headers = {
        ...options.headers,
        'X-CSRF-Token': csrfToken
      };
    }

    return originalFetch.call(this, url, options);
  };
}

// 在应用初始化时调用
setupCSRFProtection();
```

## 三、点击劫持（Clickjacking）

### 3.1 点击劫持攻击原理

```html
<!-- 恶意网站的iframe -->
<style>
  .hidden-button {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    z-index: 999;
  }
</style>

<div class="hidden-button">
  <iframe src="https://your-bank.com/transfer?to=attacker&amount=1000"
          width="100%" height="100%" frameborder="0"></iframe>
</div>

<button style="position: relative; z-index: 1000;">
  点击赢取大奖！
</button>
```

### 3.2 点击劫持防御策略

#### X-Frame-Options
```javascript
// 设置X-Frame-Options头
app.use((req, res, next) => {
  res.setHeader('X-Frame-Options', 'DENY'); // 或 'SAMEORIGIN'
  next();
});
```

#### Frame Busting
```javascript
// 前端Frame Busting脚本
function preventClickjacking() {
  if (window.top !== window.self) {
    window.top.location = window.self.location;
  }
}

// 更安全的版本
function secureFrameBusting() {
  if (window.top !== window.self) {
    try {
      // 检查是否可以访问父窗口
      window.top.location.href = window.self.location.href;
    } catch (e) {
      // 如果不能访问，说明是跨域iframe
      window.self.location.href = 'about:blank';
    }
  }
}

// 在页面加载时执行
if (document.readyState === 'complete') {
  secureFrameBusting();
} else {
  document.addEventListener('DOMContentLoaded', secureFrameBusting);
}
```

#### CSP Frame Ancestors
```javascript
// 使用CSP控制iframe嵌入
app.use((req, res, next) => {
  res.setHeader(
    'Content-Security-Policy',
    "frame-ancestors 'self' https://trusted-domain.com;"
  );
  next();
});
```

## 四、敏感数据泄露

### 4.1 常见的数据泄露风险

#### 前端存储敏感数据
```javascript
// 不安全的做法
localStorage.setItem('accessToken', 'secret-token');
sessionStorage.setItem('userPassword', 'password123');
document.cookie = `session=${sessionData}; expires=${expirationDate}`;

// 安全的做法
class SecureStorage {
  static setItem(key, value, isSensitive = false) {
    if (isSensitive) {
      console.warn('Warning: Storing sensitive data in client-side storage is not recommended');
      return false;
    }

    // 对非敏感数据进行基本加密
    const encoded = btoa(encodeURIComponent(JSON.stringify(value)));
    localStorage.setItem(key, encoded);
    return true;
  }

  static getItem(key) {
    const encoded = localStorage.getItem(key);
    if (!encoded) return null;

    try {
      return JSON.parse(decodeURIComponent(atob(encoded)));
    } catch (e) {
      return null;
    }
  }

  static removeItem(key) {
    localStorage.removeItem(key);
  }

  static clear() {
    localStorage.clear();
  }
}
```

#### 控制台信息泄露
```javascript
// 不安全的做法
console.log('User data:', userData);
console.log('API response:', response);
console.log('Token:', token);

// 安全的做法
class Logger {
  static isProduction = process.env.NODE_ENV === 'production';

  static log(message, data = null) {
    if (this.isProduction) return;

    if (data) {
      console.log(`[${new Date().toISOString()}] ${message}`, data);
    } else {
      console.log(`[${new Date().toISOString()}] ${message}`);
    }
  }

  static error(message, error = null) {
    if (this.isProduction) return;

    if (error) {
      console.error(`[${new Date().toISOString()}] ERROR: ${message}`, error);
    } else {
      console.error(`[${new Date().toISOString()}] ERROR: ${message}`);
    }
  }

  static warn(message) {
    if (this.isProduction) return;
    console.warn(`[${new Date().toISOString()}] WARNING: ${message}`);
  }
}
```

### 4.2 敏感数据保护策略

#### 数据加密
```javascript
// 简单的数据加密
class DataEncryption {
  static encrypt(data, key) {
    const json = JSON.stringify(data);
    const encrypted = CryptoJS.AES.encrypt(json, key).toString();
    return encrypted;
  }

  static decrypt(encryptedData, key) {
    try {
      const decrypted = CryptoJS.AES.decrypt(encryptedData, key);
      const json = decrypted.toString(CryptoJS.enc.Utf8);
      return JSON.parse(json);
    } catch (e) {
      return null;
    }
  }
}

// 使用示例
const sensitiveData = { creditCard: '1234-5678-9012-3456' };
const encryptionKey = 'your-secret-key';

const encrypted = DataEncryption.encrypt(sensitiveData, encryptionKey);
const decrypted = DataEncryption.decrypt(encrypted, encryptionKey);
```

#### 安全的密码处理
```javascript
// 前端密码哈希（虽然后端也需要）
class PasswordSecurity {
  static async hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);

    // 使用Web Crypto API
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

    return hashHex;
  }

  static validatePassword(password, hashedPassword) {
    return this.hashPassword(password).then(hash => hash === hashedPassword);
  }
}

// 密码强度检查
class PasswordStrength {
  static checkStrength(password) {
    let score = 0;
    const feedback = [];

    // 长度检查
    if (password.length >= 8) score += 1;
    else feedback.push('密码长度至少8位');

    // 包含数字
    if (/\d/.test(password)) score += 1;
    else feedback.push('密码应包含数字');

    // 包含小写字母
    if (/[a-z]/.test(password)) score += 1;
    else feedback.push('密码应包含小写字母');

    // 包含大写字母
    if (/[A-Z]/.test(password)) score += 1;
    else feedback.push('密码应包含大写字母');

    // 包含特殊字符
    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 1;
    else feedback.push('密码应包含特殊字符');

    return {
      score: score,
      maxScore: 5,
      feedback: feedback,
      isStrong: score >= 4
    };
  }
}
```

## 五、API安全

### 5.1 API认证与授权

#### JWT Token安全处理
```javascript
class TokenManager {
  constructor() {
    this.accessToken = null;
    this.refreshToken = null;
  }

  // 安全存储Token
  storeTokens(accessToken, refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;

    // 使用HttpOnly Cookie存储refresh token
    document.cookie = `refreshToken=${refreshToken}; HttpOnly; Secure; SameSite=Strict; path=/`;
  }

  // 获取AccessToken
  getAccessToken() {
    return this.accessToken;
  }

  // 自动刷新Token
  async refreshAccessToken() {
    try {
      const response = await fetch('/api/refresh-token', {
        method: 'POST',
        credentials: 'include' // 包含Cookie
      });

      if (response.ok) {
        const data = await response.json();
        this.accessToken = data.accessToken;
        return true;
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
    }

    return false;
  }

  // 安全的API请求
  async fetchWithAuth(url, options = {}) {
    let token = this.getAccessToken();

    // 如果Token不存在，尝试刷新
    if (!token) {
      const refreshed = await this.refreshAccessToken();
      if (!refreshed) {
        throw new Error('Authentication required');
      }
      token = this.getAccessToken();
    }

    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    });

    // 如果Token过期，尝试刷新并重试
    if (response.status === 401) {
      const refreshed = await this.refreshAccessToken();
      if (refreshed) {
        return fetch(url, {
          ...options,
          headers: {
            ...options.headers,
            'Authorization': `Bearer ${this.getAccessToken()}`
          },
          credentials: 'include'
        });
      }
    }

    return response;
  }
}
```

#### API请求安全封装
```javascript
class SecureAPIClient {
  constructor(baseURL) {
    this.baseURL = baseURL;
    this.tokenManager = new TokenManager();
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;

    // 请求前处理
    const secureOptions = this.secureRequestOptions(options);

    try {
      const response = await this.tokenManager.fetchWithAuth(url, secureOptions);

      // 响应处理
      return await this.handleResponse(response);
    } catch (error) {
      return this.handleError(error);
    }
  }

  secureRequestOptions(options) {
    const secureOptions = {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
        ...options.headers
      }
    };

    // CSRF Token
    const csrfToken = this.getCSRFToken();
    if (csrfToken) {
      secureOptions.headers['X-CSRF-Token'] = csrfToken;
    }

    return secureOptions;
  }

  async handleResponse(response) {
    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Request failed');
    }

    return data;
  }

  handleError(error) {
    console.error('API Error:', error);

    // 401错误通常需要重新登录
    if (error.message.includes('401') || error.message.includes('Authentication required')) {
      this.handleLogout();
    }

    throw error;
  }

  getCSRFToken() {
    const name = 'csrfToken=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');

    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) === ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) === 0) {
        return c.substring(name.length, c.length);
      }
    }

    return '';
  }

  handleLogout() {
    // 清除本地数据
    this.tokenManager.storeTokens(null, null);

    // 清除Cookie
    document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';

    // 重定向到登录页
    window.location.href = '/login';
  }
}
```

### 5.2 API请求验证

#### 输入验证中间件
```javascript
// 请求验证中间件
const validateRequest = (schema) => {
  return (req, res, next) => {
    const { error } = schema.validate(req.body);
    if (error) {
      return res.status(400).json({
        error: 'Validation failed',
        details: error.details
      });
    }
    next();
  };
};

// 请求限制中间件
const rateLimit = require('express-rate-limit');

const createRateLimiter = (options = {}) => {
  return rateLimit({
    windowMs: 15 * 60 * 1000, // 15分钟
    max: 100, // 限制每个IP最多100个请求
    message: 'Too many requests from this IP',
    standardHeaders: true,
    legacyHeaders: false,
    ...options
  });
};

// API路由保护
app.use('/api/', createRateLimiter());

// 用户登录验证
const userSchema = Joi.object({
  email: Joi.string().email().required(),
  password: Joi.string().min(8).required()
});

app.post('/api/login', validateRequest(userSchema), async (req, res) => {
  // 处理登录逻辑
});
```

## 六、前端安全最佳实践

### 6.1 安全的代码审查清单

```javascript
// 安全检查清单
class SecurityChecklist {
  static checklist = [
    {
      category: 'XSS防护',
      items: [
        '所有用户输入都经过HTML转义',
        '使用textContent而非innerHTML',
        '实施了Content Security Policy',
        '禁用了危险的innerHTML操作'
      ]
    },
    {
      category: 'CSRF防护',
      items: [
        '实施了CSRF Token',
        '设置了SameSite Cookie',
        '使用了安全的HTTP方法',
        '验证了请求来源'
      ]
    },
    {
      category: '认证安全',
      items: [
        '使用HttpOnly Cookie',
        'Token有效期合理',
        '实现了安全的登出',
        '密码存储加密'
      ]
    },
    {
      category: 'API安全',
      items: [
        '实施了请求限流',
        '验证了所有输入',
        '使用了HTTPS',
        '错误信息不包含敏感数据'
      ]
    }
  ];

  static runChecklist() {
    const results = {};

    this.checklist.forEach(category => {
      results[category.category] = category.items.map(item => ({
        item: item,
        status: 'pending' // 需要手动检查
      }));
    });

    return results;
  }
}
```

### 6.2 安全的第三方库管理

```javascript
// 依赖安全检查
class DependencySecurity {
  static async checkVulnerabilities() {
    try {
      // 使用npm audit
      const { execSync } = require('child_process');
      const auditResult = execSync('npm audit --json', { encoding: 'utf8' });
      return JSON.parse(auditResult);
    } catch (error) {
      console.error('Dependency check failed:', error);
      return null;
    }
  }

  static generateSecurityReport() {
    const packageJson = require('./package.json');
    const dependencies = {
      ...packageJson.dependencies,
      ...packageJson.devDependencies
    };

    const report = {
      timestamp: new Date().toISOString(),
      dependencies: Object.keys(dependencies),
      recommendations: [
        '定期运行npm audit',
        '及时更新依赖版本',
        '使用已维护的库',
        '检查库的安全记录'
      ]
    };

    return report;
  }
}

// 内容安全策略检查
class CSPChecker {
  static checkCSPImplementation() {
    const cspHeader = document.querySelector('meta[http-equiv="Content-Security-Policy"]');

    if (!cspHeader) {
      return {
        implemented: false,
        recommendations: [
          '实施Content Security Policy',
          '禁用内联脚本',
          '限制外部资源加载'
        ]
      };
    }

    const cspContent = cspHeader.getAttribute('content');
    return {
      implemented: true,
      content: cspContent,
      checks: this.analyzeCSP(cspContent)
    };
  }

  static analyzeCSP(cspContent) {
    const checks = [];

    if (cspContent.includes("'unsafe-inline'")) {
      checks.push({
        issue: '允许内联脚本',
        severity: 'high',
        recommendation: '移除unsafe-inline指令'
      });
    }

    if (cspContent.includes("'unsafe-eval'")) {
      checks.push({
        issue: '允许eval()',
        severity: 'high',
        recommendation: '移除unsafe-eval指令'
      });
    }

    if (!cspContent.includes('default-src')) {
      checks.push({
        issue: '缺少default-src',
        severity: 'medium',
        recommendation: '添加default-src指令'
      });
    }

    return checks;
  }
}
```

## 七、安全监控与响应

### 7.1 安全事件监控

```javascript
// 安全事件监控系统
class SecurityMonitor {
  constructor() {
    this.events = [];
    this.suspiciousPatterns = [
      /<script[^>]*>.*?<\/script>/gi,
      /javascript:/gi,
      /on\w+\s*=/gi,
      /data:\s*text\/html/gi
    ];
  }

  // 监控可疑请求
  monitorRequest(url, payload) {
    const requestStr = JSON.stringify({ url, payload });

    this.suspiciousPatterns.forEach(pattern => {
      if (pattern.test(requestStr)) {
        this.logSecurityEvent('suspicious_request', {
          url,
          pattern: pattern.toString(),
          timestamp: new Date().toISOString()
        });
      }
    });
  }

  // 监控DOM操作
  monitorDOMOperations() {
    const originalInnerHTML = Element.prototype.innerHTML;
    const originalSetAttribute = Element.prototype.setAttribute;

    Element.prototype.innerHTML = function(html) {
      if (typeof html === 'string' && this.suspiciousPatterns.some(p => p.test(html))) {
        SecurityMonitor.getInstance().logSecurityEvent('suspicious_dom_operation', {
          operation: 'innerHTML',
          content: html.substring(0, 100)
        });
      }
      return originalInnerHTML.call(this, html);
    };

    Element.prototype.setAttribute = function(name, value) {
      if (name.startsWith('on') && typeof value === 'string') {
        SecurityMonitor.getInstance().logSecurityEvent('suspicious_event_handler', {
          attribute: name,
          value: value
        });
      }
      return originalSetAttribute.call(this, name, value);
    };
  }

  logSecurityEvent(type, data) {
    const event = {
      type,
      data,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      url: window.location.href
    };

    this.events.push(event);

    // 发送到安全服务器
    this.sendSecurityEvent(event);

    // 本地存储（用于调试）
    this.storeLocalEvent(event);
  }

  async sendSecurityEvent(event) {
    try {
      await fetch('/api/security-events', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(event)
      });
    } catch (error) {
      console.error('Failed to send security event:', error);
    }
  }

  storeLocalEvent(event) {
    const storedEvents = JSON.parse(localStorage.getItem('securityEvents') || '[]');
    storedEvents.push(event);

    // 只保留最近100个事件
    if (storedEvents.length > 100) {
      storedEvents.splice(0, storedEvents.length - 100);
    }

    localStorage.setItem('securityEvents', JSON.stringify(storedEvents));
  }

  static getInstance() {
    if (!this.instance) {
      this.instance = new SecurityMonitor();
    }
    return this.instance;
  }
}

// 初始化安全监控
const securityMonitor = SecurityMonitor.getInstance();
securityMonitor.monitorDOMOperations();
```

### 7.2 自动安全响应

```javascript
// 自动安全响应系统
class AutoSecurityResponse {
  constructor() {
    this.blockedIPs = new Set();
    this.suspiciousActivities = new Map();
    this.blockDuration = 24 * 60 * 60 * 1000; // 24小时
  }

  // 检测可疑活动
  detectSuspiciousActivity(ip, activity) {
    const key = `${ip}_${activity.type}`;
    const now = Date.now();

    if (!this.suspiciousActivities.has(key)) {
      this.suspiciousActivities.set(key, []);
    }

    const activities = this.suspiciousActivities.get(key);
    activities.push({ timestamp: now, ...activity });

    // 清理过期记录
    const recentActivities = activities.filter(
      act => now - act.timestamp < this.blockDuration
    );

    if (recentActivities.length > 10) {
      this.blockIP(ip);
      return true;
    }

    return false;
  }

  // 封禁IP
  blockIP(ip) {
    this.blockedIPs.add(ip);
    console.warn(`IP ${ip} has been blocked due to suspicious activity`);

    // 发送警告
    this.sendSecurityAlert(ip);
  }

  // 发送安全警告
  async sendSecurityAlert(ip) {
    try {
      await fetch('/api/security-alert', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ip,
          message: 'Suspicious activity detected',
          timestamp: new Date().toISOString()
        })
      });
    } catch (error) {
      console.error('Failed to send security alert:', error);
    }
  }

  // 检查IP是否被封禁
  isIPBlocked(ip) {
    return this.blockedIPs.has(ip);
  }

  // 请求拦截器
  setupRequestInterceptor() {
    const originalFetch = window.fetch;
    const self = this;

    window.fetch = async function(url, options = {}) {
      // 获取客户端IP（通常需要后端配合）
      const clientIP = await self.getClientIP();

      if (self.isIPBlocked(clientIP)) {
        throw new Error('Access denied: IP blocked');
      }

      return originalFetch.call(this, url, options);
    };
  }

  async getClientIP() {
    try {
      const response = await fetch('/api/client-ip');
      const data = await response.json();
      return data.ip;
    } catch (error) {
      return 'unknown';
    }
  }
}

// 使用示例
const autoSecurity = new AutoSecurityResponse();
autoSecurity.setupRequestInterceptor();
```

## 八、总结

前端安全是一个持续的过程，需要开发者保持警惕并不断学习新的安全威胁和防御技术。

### 关键要点：

1. **XSS防护**：输入验证、输出转义、Content Security Policy
2. **CSRF防护**：CSRF Token、SameSite Cookie、双重验证
3. **点击劫持防护**：X-Frame-Options、Frame Busting、CSP
4. **数据保护**：安全存储、加密传输、最小权限原则
5. **API安全**：认证授权、请求限流、输入验证
6. **监控响应**：实时监控、自动响应、安全审计

### 最佳实践：

1. **防御深度**：实施多层安全防护
2. **最小权限**：只授予必要的权限
3. **定期审计**：定期检查安全配置和依赖
4. **安全培训**：提高团队安全意识
5. **应急响应**：制定安全事件响应计划

记住，**安全不是一次性的任务，而是持续的过程**。通过建立完善的安全体系，我们可以更好地保护用户数据和隐私，构建更加安全的Web应用。