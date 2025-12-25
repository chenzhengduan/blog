<template>
  <div class="message-item">
    <div :class="['message', message.role === 'user' ? 'message-user' : 'message-assistant']">
      <!-- AI消息：头像在左边 -->
      <div v-if="message.role === 'assistant'" class="message-avatar"></div>
      <div class="message-content">
        <div class="message-header">
          <span class="message-role">{{ message.role === 'user' ? '用户' : 'AI助手' }}</span>
          <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        </div>
        <div class="message-text">
          <span v-if="message.role === 'user'">{{ message.content }}</span>
          <VMdPreview v-else-if="vMdEditorReady" :text="message.content" />
          <div v-else class="loading-preview">正在加载预览组件...</div>
        </div>
        <div v-if="message.role === 'assistant' && message.loading" class="message-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          正在思考中...
        </div>
      </div>
      <!-- 用户消息：头像在右边 -->
      <div v-if="message.role === 'user'" class="message-avatar"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineAsyncComponent, ref, onMounted } from 'vue';
import { Loading } from '@element-plus/icons-vue';

// 定义 props
defineProps({
  message: {
    type: Object,
    required: true,
  },
});

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

// 异步加载和配置 v-md-editor
const VMdPreview = defineAsyncComponent(() => import('@kangc/v-md-editor/lib/preview'));
const vMdEditorReady = ref(false);

onMounted(async () => {
  if (vMdEditorReady.value) return;

  try {
    // 动态导入 v-md-editor 的实际组件对象
    const VMdPreviewActual = (await import('@kangc/v-md-editor/lib/preview')).default;

    const [
      { default: githubTheme },
      { default: createMermaidPlugin },
      { default: mermaid },
      { default: hljs },
      { default: javascript },
      { default: typescript },
      { default: json },
      { default: xml },
      { default: css },
      { default: shell },
      { default: python },
    ] = await Promise.all([
      import('@kangc/v-md-editor/lib/theme/github.js'),
      import('@kangc/v-md-editor/lib/plugins/mermaid/npm'),
      import('mermaid'),
      import('highlight.js/lib/core'),
      import('highlight.js/lib/languages/javascript'),
      import('highlight.js/lib/languages/typescript'),
      import('highlight.js/lib/languages/json'),
      import('highlight.js/lib/languages/xml'), // for HTML
      import('highlight.js/lib/languages/css'),
      import('highlight.js/lib/languages/shell'),
      import('highlight.js/lib/languages/python'),
      import('@kangc/v-md-editor/lib/style/preview.css'),
      import('@kangc/v-md-editor/lib/theme/style/github.css'),
    ]);

    // 注册 highlight.js 语言
    hljs.registerLanguage('javascript', javascript);
    hljs.registerLanguage('typescript', typescript);
    hljs.registerLanguage('json', json);
    hljs.registerLanguage('html', xml);
    hljs.registerLanguage('css', css);
    hljs.registerLanguage('shell', shell);
    hljs.registerLanguage('python', python);

    // 在实际的组件对象上进行配置
    VMdPreviewActual.use(githubTheme, {
      Hljs: hljs,
    });
    VMdPreviewActual.use(createMermaidPlugin({ mermaid }));

    vMdEditorReady.value = true;
  } catch (error) {
    console.error('Failed to load v-md-editor dependencies:', error);
  }
});
</script>

<style scoped>
/* 这里可以只包含与 message-item 相关的样式 */
.message-item {
  width: 100%;
}
.message {
  max-width: 768px;
  margin: 0 auto;
  padding: 24px 16px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  min-height: 80px;
}
.message-user {
  flex-direction: row;
  background: transparent;
  justify-content: flex-end;
}
.message-assistant {
  background: transparent;
  border-bottom: none;
}
.message-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  background: #2878e8;
  color: white;
}
.message-user .message-avatar::after {
  content: "U";
}
.message-assistant .message-avatar::after {
  content: "AI";
}
.message-content {
  flex: 0 1 auto;
  min-width: 0;
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.message-assistant .message-content {
  background: #F7F7F8;
  border: 1px solid #E1E4E8;
  border-bottom-left-radius: 4px;
  color: #2D333A;
}
.message-user .message-content {
  background: #2878e8;
  color: white;
  border: 1px solid #2878e8;
  border-bottom-right-radius: 4px;
  max-width: 70%;
}
.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.message-role {
  font-size: 14px;
  font-weight: 600;
  color: #2D333A;
}
.message-user .message-content .message-role {
  color: white;
}
.message-time {
  font-size: 12px;
  color: #8B949E;
}
.message-user .message-content .message-time {
  color: white;
}
.message-text {
  color: #2D333A;
  font-size: 16px;
  line-height: 1.6;
  word-wrap: break-word;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Helvetica Neue", Arial, sans-serif;
}
.message-user .message-content .message-text {
  color: white;
}
.message-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8B949E;
  font-size: 14px;
  margin-top: 8px;
}
.loading-preview {
  color: #8B949E;
  font-style: italic;
}
</style>