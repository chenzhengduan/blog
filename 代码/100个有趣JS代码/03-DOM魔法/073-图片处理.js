// 📷 图片处理工具
// 学习点：图像处理基础

const imageUtils = {
  // 图片转Base64
  async toBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  },
  
  // 压缩图片
  async compress(file, quality = 0.8, maxWidth = 1920) {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const img = new Image();
        img.onload = () => {
          const canvas = document.createElement('canvas');
          let width = img.width;
          let height = img.height;
          
          // 等比缩放
          if (width > maxWidth) {
            height = (height * maxWidth) / width;
            width = maxWidth;
          }
          
          canvas.width = width;
          canvas.height = height;
          
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0, width, height);
          
          canvas.toBlob((blob) => resolve(blob), 'image/jpeg', quality);
        };
        img.src = e.target.result;
      };
      reader.readAsDataURL(file);
    });
  },
  
  // 获取图片尺寸
  async getDimensions(file) {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const img = new Image();
        img.onload = () => {
          resolve({
            width: img.width,
            height: img.height,
            aspectRatio: img.width / img.height
          });
        };
        img.src = e.target.result;
      };
      reader.readAsDataURL(file);
    });
  },
  
  // 灰度滤镜
  applyGrayscale(imageData) {
    const data = imageData.data;
    for (let i = 0; i < data.length; i += 4) {
      const avg = (data[i] + data[i + 1] + data[i + 2]) / 3;
      data[i] = avg;     // R
      data[i + 1] = avg; // G
      data[i + 2] = avg; // B
    }
    return imageData;
  },
  
  // 反色滤镜
  applyInvert(imageData) {
    const data = imageData.data;
    for (let i = 0; i < data.length; i += 4) {
      data[i] = 255 - data[i];       // R
      data[i + 1] = 255 - data[i + 1]; // G
      data[i + 2] = 255 - data[i + 2]; // B
    }
    return imageData;
  }
};

console.log('图片处理工具已定义');
console.log('功能:');
console.log('- toBase64: 转换为Base64');
console.log('- compress: 压缩图片');
console.log('- getDimensions: 获取尺寸');
console.log('- applyGrayscale: 灰度滤镜');
console.log('- applyInvert: 反色滤镜');
