// 🌐 网络请求封装
// 学习点：Promise与HTTP

class HTTP {
  constructor(baseURL = '', options = {}) {
    this.baseURL = baseURL;
    this.defaultOptions = {
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      },
      ...options
    };
    
    this.interceptors = {
      request: [],
      response: []
    };
  }
  
  // 请求拦截
  useRequestInterceptor(fn) {
    this.interceptors.request.push(fn);
  }
  
  // 响应拦截
  useResponseInterceptor(fn) {
    this.interceptors.response.push(fn);
  }
  
  async request(url, options = {}) {
    // 合并配置
    let config = {
      ...this.defaultOptions,
      ...options,
      headers: {
        ...this.defaultOptions.headers,
        ...options.headers
      }
    };
    
    // 完整URL
    const fullURL = url.startsWith('http') ? url : this.baseURL + url;
    
    // 请求拦截
    for (const interceptor of this.interceptors.request) {
      config = await interceptor(config);
    }
    
    // 超时控制
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), config.timeout);
    
    try {
      let response = await fetch(fullURL, {
        ...config,
        signal: controller.signal
      });
      
      clearTimeout(timeout);
      
      // 响应拦截
      for (const interceptor of this.interceptors.response) {
        response = await interceptor(response);
      }
      
      return response;
    } catch (error) {
      clearTimeout(timeout);
      
      if (error.name === 'AbortError') {
        throw new Error('请求超时');
      }
      throw error;
    }
  }
  
  get(url, options = {}) {
    return this.request(url, { ...options, method: 'GET' });
  }
  
  post(url, data, options = {}) {
    return this.request(url, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
  
  put(url, data, options = {}) {
    return this.request(url, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }
  
  delete(url, options = {}) {
    return this.request(url, { ...options, method: 'DELETE' });
  }
}

console.log('HTTP请求封装类已定义');
console.log('使用示例:');
console.log(`
const http = new HTTP('https://api.example.com');

// 添加请求拦截器
http.useRequestInterceptor((config) => {
  config.headers.Authorization = 'Bearer token';
  return config;
});

// 发起请求
http.get('/users').then(res => res.json()).then(console.log);
`);
