// 📋 复制到剪贴板
// 学习点：Clipboard API

async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text);
    console.log('✅ 复制成功:', text);
    return true;
  } catch (err) {
    console.error('❌ 复制失败:', err);
    
    // 降级方案
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    
    try {
      document.execCommand('copy');
      console.log('✅ 使用降级方案复制成功');
      return true;
    } catch (e) {
      console.error('❌ 降级方案也失败了');
      return false;
    } finally {
      document.body.removeChild(textarea);
    }
  }
}

// 示例
console.log('复制到剪贴板函数已定义');
console.log('使用方法:');
console.log('copyToClipboard("Hello World")');
