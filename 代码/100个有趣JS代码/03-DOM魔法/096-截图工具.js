// 📸 截图工具
// 学习点：html2canvas原理

class ScreenCapture {
  // 截取元素为图片
  async captureElement(element, options = {}) {
    const {
      format = 'png',
      quality = 0.92,
      backgroundColor = '#ffffff'
    } = options;
    
    // 创建canvas
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    const rect = element.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    
    // 绘制背景
    ctx.fillStyle = backgroundColor;
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    
    // 绘制元素内容
    await this.drawElement(ctx, element, 0, 0);
    
    // 转换为图片
    const mimeType = `image/${format}`;
    return canvas.toDataURL(mimeType, quality);
  }
  
  async drawElement(ctx, element, x, y) {
    const rect = element.getBoundingClientRect();
    
    // 绘制背景色
    const bgColor = window.getComputedStyle(element).backgroundColor;
    if (bgColor && bgColor !== 'transparent') {
      ctx.fillStyle = bgColor;
      ctx.fillRect(x, y, rect.width, rect.height);
    }
    
    // 绘制文本
    const text = element.textContent;
    if (text && element.children.length === 0) {
      const style = window.getComputedStyle(element);
      ctx.font = `${style.fontSize} ${style.fontFamily}`;
      ctx.fillStyle = style.color;
      ctx.fillText(text, x + 5, y + 20);
    }
    
    // 递归绘制子元素
    Array.from(element.children).forEach((child, index) => {
      const childRect = child.getBoundingClientRect();
      this.drawElement(
        ctx,
        child,
        x + (childRect.left - rect.left),
        y + (childRect.top - rect.top)
      );
    });
  }
  
  // 下载图片
  downloadImage(dataURL, filename = 'screenshot.png') {
    const link = document.createElement('a');
    link.href = dataURL;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
  
  // 复制到剪贴板
  async copyToClipboard(dataURL) {
    try {
      const blob = await (await fetch(dataURL)).blob();
      await navigator.clipboard.write([
        new ClipboardItem({ [blob.type]: blob })
      ]);
      console.log('✅ 已复制到剪贴板');
    } catch (err) {
      console.error('❌ 复制失败:', err);
    }
  }
}

const capture = new ScreenCapture();

console.log('截图工具已定义');
console.log('使用方法:');
console.log(`
const element = document.querySelector('.container');

// 截图
capture.captureElement(element)
  .then(dataURL => {
    console.log('截图完成');
    
    // 下载
    capture.downloadImage(dataURL, 'my-screenshot.png');
    
    // 或复制到剪贴板
    capture.copyToClipboard(dataURL);
  });
`);

console.log('\n💡 简化版实现，完整功能建议使用html2canvas库');
