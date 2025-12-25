import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',      // 测试脚本目录
  timeout: 30000,          // 每个测试最大时间
  retries: 0,              // 失败不重试
  use: {
    headless: true,        // false 可看到浏览器操作
    viewport: { width: 1280, height: 720 },
    ignoreHTTPSErrors: true,
    video: 'on-first-retry' // 失败录制视频
  }
});