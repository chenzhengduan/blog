<template>
  <div class="conversation-sidebar">
    <div class="sidebar-header">
      <h2 class="sidebar-title">会话列表</h2>
      <el-button @click="handleNewConversation" :icon="Plus" circle title="新会话" />
    </div>
    <div class="conversation-list" v-loading="loading">
      <div
        v-for="convo in conversations"
        :key="convo.conversationId"
        :class="['conversation-item', { 'is-active': convo.conversationId === currentConversationId }]"
        @click="handleSwitchConversation(convo.conversationId)"
      >
        <div class="conversation-info">
          <p class="conversation-title">{{ convo.title || '新对话' }}</p>
          <p class="conversation-time">{{ formatRelativeTime(convo.lastActiveAt) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { Conversation } from '@/services/ai/useChat';

// Props
defineProps<{
  conversations: Conversation[],
  currentConversationId: string | null,
  loading: boolean
}>()

// Emits
const emit = defineEmits(['new-conversation', 'switch-conversation'])

// Methods
const handleNewConversation = () => {
  emit('new-conversation')
}

const handleSwitchConversation = (id: string) => {
  emit('switch-conversation', id)
}

// 时间格式化 (简易版)
const formatRelativeTime = (timeStr: string) => {
  const now = new Date();
  const past = new Date(timeStr);
  const diffInSeconds = Math.floor((now.getTime() - past.getTime()) / 1000);

  const minutes = Math.floor(diffInSeconds / 60);
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;

  const days = Math.floor(hours / 24);
  return `${days}天前`;
}
</script>

<style scoped>
.conversation-sidebar {
  width: 260px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.conversation-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #e4e7ed;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #ecf5ff;
}

.conversation-item.is-active {
  background-color: #d9ecff;
  border-left: 3px solid #409eff;
  padding-left: 13px;
}

.conversation-info {
  display: flex;
  flex-direction: column;
}

.conversation-title {
  font-size: 14px;
  color: #303133;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-time {
  font-size: 12px;
  color: #909399;
  margin: 0;
}
</style>