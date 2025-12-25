// frontend/src/composables/useChat.ts
import { ref, onMounted, onUnmounted, nextTick, computed, reactive } from 'vue'

import { useUser } from '../../store';
// import { ElMessage, ElMessageBox } from 'element-plus' // 移除此行

// 定义会话类型
export interface Conversation {
  conversationId: string;
  title: string;
  lastActiveAt: string;
  messageCount: number;
  status: string;
  createdAt: string;
}

// 定义聊天API方法接口
interface ChatApiMethods {
  sendChatMessage: (
    message: string,
    onMessage: (data: { type: 'session' | 'content'; content: any }) => void,
    onError: (error: any) => void,
    onComplete: () => void,
    conversationId?: string | null
  ) => Promise<void>;
  getMessages: (conversationId: string, limit: number, lastMessageId?: string) => Promise<any>;
  getConversations: (limit: number, beforeId?: string) => Promise<any>;
}

interface MessageProvider {
  success: (message: string) => void;
  error: (message: string) => void;
  info: (message: string) => void;
}

interface DialogProvider {
  confirm: (message: string, title: string, options: any) => Promise<any>;
}

export function useChat(options?: {
  chatApi?: ChatApiMethods; // 新增 chatApi 参数
  messageProvider?: MessageProvider; // 新增消息提供者
  dialogProvider?: DialogProvider;   // 新增对话框提供者
  // onLogout?: () => void; // 如果未来需要通用退出事件，可以添加
}) {
  const {
    chatApi, // 解构 chatApi
    messageProvider,
    dialogProvider,
  } = options || {}
  
  // 确保 chatApi, messageProvider, dialogProvider 已提供
  if (!chatApi) {
    throw new Error("useChat: chatApi option is required.");
  }
  const msg = messageProvider || {
    success: () => {},
    error: () => {},
    info: () => {},
  }; // 提供默认空实现，避免报错
  const dialog = dialogProvider || {
    confirm: () => Promise.reject("Dialog not provided"),
  }; // 提供默认实现

  // 会话管理
  const conversations = ref<Conversation[]>([])
  const currentConversationId = ref<string | null>(null)
  const loadingConversations = ref(false)

  // 响应式数据
  const messages = ref<any[]>([])
  const inputMessage = ref('')
  const sending = ref(false)
  const loadingMore = ref(false)
  const showLoadMore = ref(false)
  const messagesContainer = ref<HTMLElement>()
  const scrollAnchor = ref<HTMLElement>()

  // 内部链接弹窗
  const showInternalLinkDialog = ref(false)
  const internalLinkUrl = ref('')

  // 分页相关
  const hasMoreMessages = ref(false)
  const lastMessageId = ref<string | null>(null)

  // 建议问题
  const suggestions = [
    '查询学员信息',
    '安排课程',
    '查看老师列表',
    '获取机构信息'
  ]

  // 按messageId排序的消息列表（计算属性）
  const sortedMessages = computed(() => {
    return messages.value.slice().sort((a, b) => {
      if (!a.messageId && !b.messageId) return 0
      if (!a.messageId) return 1
      if (!b.messageId) return -1
      // 注意：messageId 可能是很长的字符串，直接相减可能导致精度问题
      // 应该使用字符串比较或转换为 BigInt
      if (a.messageId < b.messageId) return -1;
      if (a.messageId > b.messageId) return 1;
      return 0;
    })
  })

  // 格式化时间
  const formatTime = (timestamp: number) => {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 滚动到底部
  const scrollToBottom = (behavior: 'auto' | 'smooth' = 'auto') => {
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTo({
          top: messagesContainer.value.scrollHeight,
          behavior: behavior
        });
      }
    });
  }

  // 添加消息
  const addMessage = (role: 'user' | 'assistant', content: string, loading = false) => {
    const message = reactive({
      role,
      content,
      timestamp: Date.now(),
      loading,
      messageId: null as string | null
    })
    messages.value.push(message)
    scrollToBottom('smooth')
    return message
  }

  // 发送建议问题
  const sendSuggestion = (suggestion: string) => {
    inputMessage.value = suggestion
    handleSendMessage()
  }

  // 发送消息
  const handleSendMessage = async () => {
    const message = inputMessage.value.trim()
    if (!message || sending.value) return

    addMessage('user', message)
    inputMessage.value = ''

    const aiPlaceholder = addMessage('assistant', '', true)

    sending.value = true

    try {
      let aiResponse = ''
      await chatApi.sendChatMessage( // 使用传入的 chatApi
        message,
        (data) => {
          // 处理会话信息
          if (data.type === 'session' && data.content.conversationId) {
            if (!currentConversationId.value) {
              currentConversationId.value = data.content.conversationId;
              // 如果是新会话，刷新会话列表
              fetchConversations();
            }
            // 你也可以在这里处理 userMessageId
          }
          // 处理消息内容
          else if (data.type === 'content' && data.content) {
            aiResponse += data.content
            aiPlaceholder.content = aiResponse
            aiPlaceholder.loading = false
            console.log('💬 更新消息内容:', aiResponse.length, '字符')
            scrollToBottom('smooth')
          }
        },
        (error) => {
          console.error('聊天请求失败:', error)
          msg.error('发送消息失败') // 使用传入的消息提供者
          aiPlaceholder.loading = false
        },
        () => {
          sending.value = false
          scrollToBottom('smooth')
        },
        currentConversationId.value // 传入当前会话ID
      )
    } catch (error) {
      sending.value = false
      msg.error('发送消息失败') // 使用传入的消息提供者
      aiPlaceholder.loading = false;
    }
  }

  // 新会话 (纯前端操作)
  const handleNewSession = () => {
    currentConversationId.value = null;
    messages.value = [];
    hasMoreMessages.value = false;
    lastMessageId.value = null;
    showLoadMore.value = false;
    msg.info('已切换到新会话，发送消息将创建新的聊天记录。');
  }

  // 退出登录
  // const handleLogout = async () => {
  //   try {
  //     await ElMessageBox.confirm('确定要退出登录吗？', '确认', {
  //       confirmButtonText: '确定',
  //       cancelButtonText: '取消',
  //       type: 'warning'
  //     })
  //     authStore.logout()
  //     router.push('/login')
  //     ElMessage.success('已退出登录')
  //   } catch {
  //     // 用户取消
  //   }
  // }

  // 超链接点击处理
  const allowedLinkPrefixes = [
    'https://wtwotest.xiaogj.com/xiaogj-ai-api',
    'https://test.xiaogj.com/xiaogj-ai-api'
  ];

  // TODO: `authStore` 从外部传入，或者 `handleLinkClick` 变成由外部组件处理的事件
  const handleLinkClick = (event: MouseEvent) => {
    const target = event.target as HTMLElement;
    const anchor = target.closest('a');

    if (anchor) {
      event.preventDefault();
      const originalUrl = anchor.href;
      const isAllowed = allowedLinkPrefixes.some(prefix => originalUrl.startsWith(prefix));

      const userStore = useUser()
      if (isAllowed) {
        const url = new URL(originalUrl);
        url.searchParams.set('Xgj-Sid', userStore.user.SID || ''); // 如果为null，传递空字符串
        internalLinkUrl.value = url.toString();
        showInternalLinkDialog.value = true;
      } else {
        dialog.confirm(
          `您即将离开本应用，访问外部链接。请注意链接安全。<br/><br/><strong>链接:</strong> ${originalUrl}`,
          '安全警告',
          {
            confirmButtonText: '继续访问',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          }
        ).then(() => {
          window.open(originalUrl, '_blank');
        }).catch(() => {
          msg.info('操作已取消'); // 使用传入的消息提供者
        });
      }
    }
  };

  // 加载更多历史消息
  const loadMoreMessages = async () => {
    if (loadingMore.value || !currentConversationId.value) return
    loadingMore.value = true
    try {
      const response = await chatApi.getMessages(currentConversationId.value, 20, lastMessageId.value || undefined) // 使用传入的 chatApi
      if (response.success && response.data) {
        const messageData = response.data
        hasMoreMessages.value = messageData.hasMore
        lastMessageId.value = messageData.lastMessageId
        if (messageData.messages && messageData.messages.length > 0) {
          const newMessages = messageData.messages.map((msg: any) => ({
            role: msg.role === 'user' ? 'user' : 'assistant',
            content: msg.content,
            timestamp: new Date(msg.createdAt).getTime(),
            loading: false,
            messageId: msg.messageId
          }))
          messages.value.unshift(...newMessages)
        }
        showLoadMore.value = hasMoreMessages.value
      }
    } catch (error) {
      console.log(error)
      msg.error('加载历史消息失败') // 使用传入的消息提供者
    } finally {
      loadingMore.value = false
    }
  }
  
  // 初始化加载历史消息
  const initHistoryMessages = async () => {
    if (!currentConversationId.value) {
      messages.value = []
      return
    }
    try {
      const response = await chatApi.getMessages(currentConversationId.value, 20) // 使用传入的 chatApi
      if (response.success && response.data) {
        const messageData = response.data
        hasMoreMessages.value = messageData.hasMore
        lastMessageId.value = messageData.lastMessageId
        if (messageData.messages && messageData.messages.length > 0) {
          const historyMessages = messageData.messages.map((msg: any) => ({
            role: msg.role === 'user' ? 'user' : 'assistant',
            content: msg.content,
            timestamp: new Date(msg.createdAt).getTime(),
            loading: false,
            messageId: msg.messageId
          }))
          messages.value = historyMessages
          showLoadMore.value = hasMoreMessages.value
        }
      }
    } catch (error) {
      console.error('加载历史消息失败:', error)
    }
  }

  // 新增：获取会话列表
  const fetchConversations = async () => {
    if (loadingConversations.value) return;
    loadingConversations.value = true;
    try {
      const response = await chatApi.getConversations(50); // 一次性获取更多
      if (response.success && response.data) {
        conversations.value = response.data.conversations;
      }
    } catch (error) {
      console.error('获取会话列表失败:', error);
      msg.error('获取会话列表失败');
    } finally {
      loadingConversations.value = false;
    }
  };

  // 新增：切换会话
  const switchConversation = async (conversationId: string) => {
    if (currentConversationId.value === conversationId) return;
    
    currentConversationId.value = conversationId;
    messages.value = []; // 清空消息
    lastMessageId.value = null;
    hasMoreMessages.value = false;
    
    await initHistoryMessages(); // 加载新会话的消息
  };

  let resizeObserver: ResizeObserver | null = null;

  onMounted(async () => {
    await fetchConversations();
    // 默认选中第一个会话
    if (conversations.value.length > 0) {
      await switchConversation(conversations.value[0].conversationId);
    }
    
    // 使用 ResizeObserver 监听内容高度变化
    if (messagesContainer.value) {
      resizeObserver = new ResizeObserver(() => {
        scrollToBottom('smooth');
      });
      resizeObserver.observe(messagesContainer.value);
    }
  })

  onUnmounted(() => {
    // 组件销毁时停止观察
    if (resizeObserver && messagesContainer.value) {
      resizeObserver.unobserve(messagesContainer.value);
    }
    resizeObserver = null;
  })

  // 返回所有需要暴露给组件的状态和方法
  return {
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
    formatTime,
    sendSuggestion,
    handleSendMessage,
    handleNewSession,
    handleLinkClick,
    loadMoreMessages,
    // 新增暴露给UI的属性和方法
    conversations,
    currentConversationId,
    loadingConversations,
    switchConversation,
    fetchConversations,
  }
}
