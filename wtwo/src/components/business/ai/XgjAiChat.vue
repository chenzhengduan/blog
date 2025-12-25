<template>
  <div class="chat-page-container">
    <!-- 会话侧边栏 -->
    <ConversationSidebar
      :conversations="conversations"
      :current-conversation-id="currentConversationId"
      :loading="loadingConversations"
      @new-conversation="handleNewSession"
      @switch-conversation="switchConversation"
    />

    <!-- 主聊天窗口 -->
    <div class="chat-container">
      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesContainer" @click="handleLinkClick">
        <!-- 加载更多按钮 -->
        <div v-if="showLoadMore" class="load-more-container">
          <el-button @click="loadMoreMessages" :loading="loadingMore" type="primary" plain>
            加载更多历史消息
          </el-button>
        </div>

        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0 && !loadingMore" class="welcome-message">
          <h2 class="welcome-title">👋 欢迎使用校管家AI助手</h2>
          <p class="welcome-subtitle">我可以帮您处理教务管理、学员查询、课程安排等各种问题</p>
          <div class="welcome-suggestions">
            <el-button
              v-for="suggestion in suggestions"
              :key="suggestion"
              @click="sendSuggestion(suggestion)"
              class="suggestion-item"
              type="primary"
              plain
            >
              {{ suggestion }}
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <XgjAiMessageItem
          v-for="(message, index) in sortedMessages"
          :key="message.messageId || `temp-${index}`"
          :message="message"
        />
        
        <!-- 滚动锚点 -->
        <div ref="scrollAnchor" class="scroll-anchor"></div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-container">
        <div class="chat-input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :maxlength="4000"
            placeholder="输入您的问题..."
            resize="none"
            @keydown.ctrl.enter="handleSendMessage"
            @keydown.enter.prevent="handleSendMessage"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            :loading="sending"
            :disabled="!inputMessage.trim()"
            @click="handleSendMessage"
            class="send-button"
          >
            发送
          </el-button>
        </div>
      </div>

      <!-- 内部链接查看弹窗 -->
      <el-dialog
        v-model="showInternalLinkDialog"
        width="80%"
        :destroy-on-close="true"
        top="5vh"
      >
        <iframe
          :src="internalLinkUrl"
          style="width: 100%; height: 75vh; border: none; border-radius: 8px;"
        ></iframe>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChat } from '@/services/ai/useChat'
import { chatAPI } from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Promotion } from '@element-plus/icons-vue'
import XgjAiMessageItem from './XgjAiMessageItem.vue' // 引入 XgjAiMessageItem 组件
import ConversationSidebar from './ConversationSidebar.vue' // 引入侧边栏组件


const {
  messages,
  inputMessage,
  sending,
  loadingMore,
  showLoadMore,
  messagesContainer,
  scrollAnchor,
  showInternalLinkDialog,
  internalLinkUrl,
  suggestions,
  sortedMessages,
  handleSendMessage,
  handleNewSession,
  handleLinkClick,
  loadMoreMessages,
  sendSuggestion,
  // 多会话相关
  conversations,
  currentConversationId,
  loadingConversations,
  switchConversation,
} = useChat({
  chatApi: chatAPI, 
  messageProvider: ElMessage, 
  dialogProvider: ElMessageBox, 
})
</script>

<style scoped>
.chat-page-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* 这里只保留 XgjAiChat 的容器和布局样式 */
.chat-container {
  flex: 1; /* 占据剩余空间 */
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  max-width: 100%;
  margin: 0 auto;
  position: relative; /* 为了固定头尾 */
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  background: #FFFFFF;
  padding: 24px 24px 130px 24px; /* 调整 padding */
  margin: 0;
  width: 100%;
}
.load-more-container {
  text-align: center;
  margin-bottom: 24px;
}
.welcome-message {
  max-width: 768px;
  margin: 0 auto;
  padding: 60px 16px 32px;
  text-align: center;
}
.welcome-title {
  font-size: 32px;
  font-weight: 600;
  color: #2D333A;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
}
.welcome-subtitle {
  font-size: 18px;
  color: #6E7681;
  margin-bottom: 32px;
  line-height: 1.5;
}
.welcome-suggestions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  max-width: 600px;
  margin: 0 auto;
}
.suggestion-item {
  background: #F7F7F8;
  color: #2D333A;
  border: 1px solid #D0D7DE;
  padding: 16px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}
.suggestion-item:hover {
  background: #FFFFFF;
  border-color: #2878e8;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.scroll-anchor {
  height: 1px;
  width: 1px;
  opacity: 0;
  pointer-events: none;
}
.chat-input-container {
  background: #FFFFFF;
  border-top: 1px solid #E1E4E8;
  padding: 24px;
  position: absolute; /* 修改为 absolute */
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  backdrop-filter: blur(8px);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}
.chat-input-wrapper {
  max-width: 768px;
  margin: 0 auto;
  display: flex;
  gap: 8px;
  align-items: flex-end;
  background: #FFFFFF;
  border: 2px solid #D0D7DE;
  border-radius: 24px;
  padding: 12px 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: border-color 0.2s ease;
}
.chat-input-wrapper:focus-within {
  border-color: #2878e8;
}
.send-button {
  flex-shrink: 0;
  min-width: 60px;
  height: 36px;
  background: #2878e8 !important;
  border: none !important;
  border-radius: 8px !important;
  color: white !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.2s ease !important;
  padding: 0 12px !important;
  cursor: pointer;
  font-size: 14px !important;
  font-weight: 500 !important;
}
.send-button:hover:not(:disabled) {
  background: #1c5fb8 !important;
  transform: scale(1.05);
}
.send-button:disabled {
  background: #8B949E !important;
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

/* 隐藏 wtwo-input 的默认边框和背景 */
.chat-input-wrapper :deep(.wtwo-input__wrapper) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.chat-input-wrapper :deep(.wtwo-textarea) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.chat-input-wrapper :deep(.wtwo-textarea__inner) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
  resize: none !important;
  font-size: 16px !important;
  line-height: 1.5 !important;
  color: #2D333A !important;
  min-height: 24px !important;
}

.chat-input-wrapper :deep(.wtwo-textarea__inner):focus {
  outline: none !important;
  box-shadow: none !important;
  border: none !important;
  background: transparent !important;
}

.chat-input-wrapper :deep(.wtwo-textarea__inner):hover {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
}

.chat-input-wrapper :deep(.wtwo-textarea__inner):focus-visible {
  outline: none !important;
  border: none !important;
  box-shadow: none !important;
}

/* 确保 placeholder 样式正确 */
.chat-input-wrapper :deep(.wtwo-textarea__inner)::placeholder {
  color: #8B949E !important;
  font-size: 16px !important;
}
</style>
