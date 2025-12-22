// 🔄 重试机制封装
// 学习点：Promise与错误处理

async function retry(fn, options = {}) {
  const {
    times = 3,
    delay = 1000,
    backoff = 2,
    onRetry = null
  } = options;
  
  let lastError;
  
  for (let attempt = 1; attempt <= times; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;
      
      if (attempt === times) {
        throw new Error(`失败${times}次: ${error.message}`);
      }
      
      const waitTime = delay * Math.pow(backoff, attempt - 1);
      
      if (onRetry) {
        onRetry(attempt, error, waitTime);
      }
      
      console.log(`第${attempt}次尝试失败，${waitTime}ms后重试...`);
      await new Promise(resolve => setTimeout(resolve, waitTime));
    }
  }
}

// 使用示例
async function unstableApi() {
  if (Math.random() > 0.7) {
    return '成功！';
  }
  throw new Error('网络错误');
}

console.log('重试机制演示:');
retry(unstableApi, {
  times: 5,
  delay: 500,
  backoff: 1.5,
  onRetry: (attempt, error) => {
    console.log(`  重试原因: ${error.message}`);
  }
}).then(result => {
  console.log('✅ 最终结果:', result);
}).catch(error => {
  console.log('❌ 全部失败:', error.message);
});
