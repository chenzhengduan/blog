// 🔐 简单加密解密
// 学习点：Base64与简单密码学

const crypto = {
  // Base64编码
  encodeBase64(str) {
    return btoa(unescape(encodeURIComponent(str)));
  },
  
  // Base64解码
  decodeBase64(str) {
    return decodeURIComponent(escape(atob(str)));
  },
  
  // 简单凯撒密码
  caesar(str, shift = 3) {
    return str.replace(/[a-zA-Z]/g, char => {
      const code = char.charCodeAt(0);
      const base = code >= 97 ? 97 : 65;
      return String.fromCharCode(((code - base + shift) % 26) + base);
    });
  },
  
  // XOR加密（简单对称加密）
  xorEncrypt(text, key) {
    let result = '';
    for (let i = 0; i < text.length; i++) {
      result += String.fromCharCode(
        text.charCodeAt(i) ^ key.charCodeAt(i % key.length)
      );
    }
    return btoa(result);
  },
  
  xorDecrypt(encrypted, key) {
    const text = atob(encrypted);
    let result = '';
    for (let i = 0; i < text.length; i++) {
      result += String.fromCharCode(
        text.charCodeAt(i) ^ key.charCodeAt(i % key.length)
      );
    }
    return result;
  },
  
  // 生成随机密钥
  generateKey(length = 16) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let key = '';
    for (let i = 0; i < length; i++) {
      key += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return key;
  }
};

console.log('加密工具演示:\n');

const text = 'Hello World';
console.log('原文:', text);

const base64 = crypto.encodeBase64(text);
console.log('Base64编码:', base64);
console.log('Base64解码:', crypto.decodeBase64(base64));

console.log('\n凯撒密码(偏移3):', crypto.caesar(text, 3));

const key = crypto.generateKey(8);
const encrypted = crypto.xorEncrypt(text, key);
console.log('\nXOR加密:', encrypted);
console.log('XOR解密:', crypto.xorDecrypt(encrypted, key));
