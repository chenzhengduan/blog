import { apiUrl, useUser, useLoginInfo } from '../store';
import fetch from './http';
import { ElMessage } from 'element-plus'

// AI服务的基础URL
const API_BASE_URL = `${apiUrl}/aiagent`

// 聊天API相关方法
export const chatAPI = {
  _activeRequest: null as AbortController | null, // 跟踪活跃的请求
  _shouldStop: false, // 添加停止标志
  
  // 发送聊天消息（流式响应）
  async sendChatMessage(
    message: string,
    onMessage: (data: {type: 'session' | 'content', content: any}) => void,
    onError: (error: any) => void,
    onComplete: () => void,
    conversationId?: string | null,
  ) {
    console.log('🎯 sendChatMessage 开始执行')
    
    // 重置停止标志
    this._shouldStop = false
    
    // 如果有活跃的请求，先取消它
    if (this._activeRequest) {
      console.log('🔄 取消之前的请求')
      this._shouldStop = true // 设置停止标志
      this._activeRequest.abort()
      // 等待一小段时间确保之前的请求被取消
      await new Promise(resolve => setTimeout(resolve, 10))
    }
    
    // 创建新的 AbortController
    this._activeRequest = new AbortController()
    const signal = this._activeRequest.signal
    
    // 如果信号已经被中止，直接返回
    if (signal.aborted) {
      console.log('请求已被中止')
      return
    }
    
    const userStore = useUser()
    const loginInfoStore = useLoginInfo()

    // 构建请求体
    const requestBody: { message: string; conversationId?: string } = { message }
    if (conversationId) {
      requestBody.conversationId = conversationId
    }
    
    let isCompleted = false // 防止重复完成
    
    console.log('🚀 开始新的SSE请求:', message.substring(0, 50) + '...')
    
    // 按照官方示例定义错误类
    class FatalError extends Error {}
    
    try {
      console.log('📡 调用 fetchEventSource...')
      
      // 动态导入 fetchEventSource 来避免编译时的依赖问题
      const { fetchEventSource } = await import('@microsoft/fetch-event-source')
      
      await fetchEventSource(`${API_BASE_URL}/api/chat/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Xgj-Sid': userStore.user.SID || '',
          "Xgj-Param-Compatible":"xgj",
          'WTwo-CompanyID': loginInfoStore.loginInfo.WTwo_CompanyID,
          'WTwo-AuthToken': loginInfoStore.loginInfo.WTwo_AuthToken
        },
        body: JSON.stringify(requestBody),
        
        // 关键配置
        openWhenHidden: true,
        
        // 按照官方示例实现 onopen
        async onopen(response: any) {
          console.log('🔗 onopen 被调用 - status:', response.status, 'ok:', response.ok)
          
          if (chatAPI._shouldStop) {
            console.log('❌ onopen: 请求已被标记为停止，抛出 FatalError')
            throw new FatalError('请求已被取消')
          }
          
          if (response.ok && response.status === 200) {
            console.log('✅ onopen: 连接建立成功，等待响应...')
            return // everything's good
          } else if (response.status >= 400 && response.status < 500 && response.status !== 429) {
            // client-side errors are usually non-retriable:
            console.log('❌ onopen: 客户端错误，抛出 FatalError:', response.status)
            if (response.status === 401) {
              localStorage.removeItem('xgj-aiagent-user-token')
              const { default: router } = await import('@/router')
              router.push('/login')
              ElMessage.error('认证已过期，请重新登录')
            }
            throw new FatalError(`客户端错误: ${response.status}`)
          } else {
            // 对于其他错误，我们也不想重试，所以抛出 FatalError
            console.log('❌ onopen: 服务器错误，抛出 FatalError:', response.status)
            const message = `服务器错误: ${response.status}`
            ElMessage.error(message)
            throw new FatalError(message)
          }
        },
        
        // 处理消息
        onmessage(event: any) {
          console.log('📨 onmessage 被调用 - event:', event.event, 'data length:', event.data?.length || 0)
          
          if (isCompleted || chatAPI._shouldStop) {
            console.log('⏹️ onmessage: 已完成或已停止，忽略消息')
            return
          }
          
          try {
            console.log('📄 SSE Event:', event.event, event.data?.substring(0, 100) + '...')
            
            // 按照官方示例，检查服务器发送的错误事件
            if (event.event === 'FatalError') {
              console.log('❌ onmessage: 服务器发送 FatalError，抛出异常')
              throw new FatalError(event.data)
            }

            // 新增：处理会话事件
            if (event.event === 'session') {
              try {
                const sessionData = JSON.parse(event.data)
                console.log('🤝 onmessage: 处理会话事件', sessionData)
                onMessage({
                  type: 'session',
                  content: sessionData
                })
              } catch (e) {
                console.warn('解析session事件数据失败:', event.data, e)
              }
              return;
            }
            
            // 处理不同的事件类型
            if (event.event === 'completed' || event.event === 'done') {
              console.log('✅ onmessage: 收到完成事件')
              if (!isCompleted) {
                isCompleted = true
                onComplete()
              }
              return
            }
            
            // 处理消息事件
            if (event.event === 'message' || !event.event) {
              const content = event.data
              if (content && content.trim()) {
                console.log('💬 onmessage: 处理消息内容')
                onMessage({
                  type: 'content',
                  content: content
                })
              }
            }
          } catch (error) {
            console.warn('解析SSE数据失败:', event, error)
            if (event.data && event.data.trim()) {
              onMessage({
                type: 'content',
                content: event.data
              })
            }
          }
        },
        
        // 按照官方示例实现 onclose - 控制是否重试
        onclose() {
          console.log('🔌 onclose 被调用 - SSE连接已关闭')
          console.log('🔌 onclose: isCompleted =', isCompleted, '_shouldStop =', chatAPI._shouldStop)
          
          if (!isCompleted) {
            console.log('🔌 onclose: 标记为完成并调用 onComplete')
            isCompleted = true
            onComplete()
          }
          chatAPI._shouldStop = true
          chatAPI._activeRequest = null
          
          // 🔑 关键：官方示例中 onclose 抛出 RetriableError 会重试
          // 我们不想重试，所以抛出 FatalError 来停止
          console.log('❌ onclose: 抛出 FatalError 阻止重试')
          throw new FatalError('连接关闭，不重试')
        },
        
        // 🔑 按照官方示例实现 onerror - 这是关键！
        onerror(err: any) {
          console.log('💥 onerror 被调用')
          console.error('💥 onerror: SSE连接错误:', err)
          console.log('💥 onerror: 错误类型:', err?.constructor?.name)
          console.log('💥 onerror: 是否为 FatalError:', err instanceof FatalError)
          
          if (!isCompleted) {
            console.log('💥 onerror: 调用 onError 回调')
            const message = err instanceof Error ? err.message : '连接错误'
            ElMessage.error(message)
            onError(err)
          }
          chatAPI._shouldStop = true
          chatAPI._activeRequest = null
          
          // 🔑 关键：按照官方示例的逻辑
          if (err instanceof FatalError) {
            console.log('❌ onerror: 重新抛出 FatalError 停止重试')
            throw err; // rethrow to stop the operation
          } else {
            // 🔑 对于我们的用例，任何错误都不想重试，所以抛出 FatalError
            console.log('❌ onerror: 转换为 FatalError 停止重试')
            throw new FatalError('停止重试')
          }
        },
        
        // 添加 signal 支持中止
        signal: signal,
        // 确保携带凭据（如Cookie）
        credentials: 'include',
      })
      console.log('✅ fetchEventSource 正常结束')
    } catch (error) {
      console.log('💥 fetchEventSource 抛出异常:', error)
      console.log('💥 异常类型:', error?.constructor?.name)
      console.log('💥 是否为 FatalError:', error instanceof FatalError)
      
      if (!isCompleted) {
        console.log('💥 调用 onError 回调')
        const message = error instanceof Error ? error.message : '请求失败'
        ElMessage.error(message)
        onError(error)
      }
      // 清理活跃请求引用
      chatAPI._activeRequest = null
      chatAPI._shouldStop = true
    }
  },

  // 获取指定会话的历史消息
  async getMessages(conversationId: string, limit: number = 20, beforeId?: string) {
    const userStore = useUser()
    const params: any = { conversationId, limit }
    if (beforeId) {
      params.beforeId = beforeId
    }
    return fetch.get({
      url: `${API_BASE_URL}/api/chat/messages`,
      data: params,
      
    },{
        headers: {
        'Xgj-Sid': userStore.user.SID || '',
        "Xgj-Param-Compatible":"xgj"
      }
    })
  },

  // 新增：获取会话列表
  async getConversations(limit: number = 20, beforeId?: string) {
    const userStore = useUser()
    const params: any = { limit }
    if (beforeId) {
      params.beforeId = beforeId
    }
    return fetch.get({
      url: `${API_BASE_URL}/api/chat/conversations`,
      data: params,
    }, {
      headers: {
        'Xgj-Sid': userStore.user.SID || '',
        "Xgj-Param-Compatible": "xgj"
      }
    })
  },
}