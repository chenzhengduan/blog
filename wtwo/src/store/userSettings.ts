import { defineStore } from 'pinia'
import { queryUserSettingsPage } from '@/api/arrange'

// 用户设置类型定义
interface UserSetting {
  ID?: string
  PageKey: string
  PageName: string
  Type: number
  IsPublic: number
  UserSettingsDetailList?: any[]
}

interface UserSettingsState {
  // 用户设置数据，使用PageKey作为键
  settings: Record<string, UserSetting[]>
  // 加载状态，使用PageKey作为键
  loading: Record<string, boolean>
  // 最后更新时间，使用PageKey作为键
  lastUpdated: Record<string, number>
  // 正在进行的请求Promise缓存，用于防止重复请求
  pendingRequests: Record<string, Promise<UserSetting[]> | undefined>
}

// 定义用户设置store
export const useUserSettings = defineStore('userSettings', {
  state: (): UserSettingsState => {
    return {
      settings: {},
      loading: {},
      lastUpdated: {},
      pendingRequests: {}
    }
  },

  getters: {
    // 根据PageKey获取用户设置
    getUserSettings: (state) => (pageKey: string) => {
      return state.settings[pageKey] || []
    },

    // 检查用户设置是否已加载
    isSettingsLoaded: (state) => (pageKey: string) => {
      return !!state.settings[pageKey]
    },

    // 获取加载状态
    isLoading: (state) => (pageKey: string) => {
      return state.loading[pageKey] || false
    }
  },

  actions: {
    // 获取用户设置，如果已存在则返回缓存数据
    async fetchUserSettings(pageKey: string, pageName: string, type: number = 1, isPublic: number = 1, forceRefresh: boolean = false) {
      // 如果已有数据且不需要强制刷新，则直接返回
      if (this.settings[pageKey] && !forceRefresh) {
        return this.settings[pageKey]
      }

      // 如果有正在进行的请求，直接返回该Promise，避免重复请求
      if (this.pendingRequests[pageKey]) {
        return this.pendingRequests[pageKey]
      }

      // 设置加载状态
      this.loading[pageKey] = true

      // 创建请求Promise并缓存
      const requestPromise = (async () => {
        try {
          const response = await queryUserSettingsPage({
            PageKey: pageKey,
            PageName: pageName,
            Type: type,
            IsPublic: isPublic
          })

          // 保存数据到store
          this.settings[pageKey] = response.Data || []
          // 更新最后更新时间
          this.lastUpdated[pageKey] = Date.now()

          return this.settings[pageKey]
        } catch (error) {
          console.error('获取用户设置失败:', error)
          throw error
        } finally {
          // 取消加载状态
          this.loading[pageKey] = false
          // 清除请求Promise缓存
          delete this.pendingRequests[pageKey]
        }
      })()

      // 缓存请求Promise
      this.pendingRequests[pageKey] = requestPromise

      return requestPromise
    },

    // 清除指定PageKey的用户设置
    clearUserSettings(pageKey: string) {
      delete this.settings[pageKey]
      delete this.loading[pageKey]
      delete this.lastUpdated[pageKey]
      delete this.pendingRequests[pageKey]
    },

    // 清除所有用户设置
    clearAllUserSettings() {
      this.settings = {}
      this.loading = {}
      this.lastUpdated = {}
      this.pendingRequests = {}
    },

    // 更新用户设置
    updateUserSettings(pageKey: string, settings: UserSetting[]) {
      this.settings[pageKey] = settings
      this.lastUpdated[pageKey] = Date.now()
    }
  }
})
