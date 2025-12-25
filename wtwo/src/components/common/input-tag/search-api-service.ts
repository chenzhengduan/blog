// 不再需要直接导入 fetch，因为我们使用传入的 API 函数
import type { ApiConfig, OptionItem } from '@/components/common/input-tag/input-tag.d.ts'

export class SearchApiService {
  private cancelToken: AbortController | null = null
  private debounceTimer: number | null = null
  // 用于确保只有最新的一次请求会生效
  private latestIssueId: number = 0

  /**
   * 执行搜索请求
   * @param config API 配置
   * @param keyword 搜索关键词
   * @returns Promise<OptionItem[]>
   */
  async search(config: ApiConfig, keyword: string): Promise<OptionItem[]> {
    try {
      // 构建请求参数
      const params = {
        ...config.params,
        [config.searchParam || 'keyword']: keyword
      }

      // 调用 API 函数
      const response = await config.apiFunction(params)

      return this.parseResponse(response)
    } catch (error: any) {
      throw error
    }
  }

  /**
   * 带防抖的搜索
   * @param config API 配置
   * @param keyword 搜索关键词
   * @param callback 回调函数
   */
  searchWithDebounce(
    config: ApiConfig,
    keyword: string,
    callback: (results: OptionItem[], error?: any) => void
  ): void {
    // 清除之前的定时器
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer)
    }

    // 设置新的定时器
    const debounceTime = config.debounceTime || 300
    const issueId = ++this.latestIssueId
    this.debounceTimer = window.setTimeout(async () => {
      try {
        const results = await this.search(config, keyword)
        // 仅当本次请求仍然是最新的一次时，才触发回调
        if (issueId === this.latestIssueId) {
          callback(results)
        }
      } catch (error) {
        // 同样仅在本次仍为最新请求时回调错误
        if (issueId === this.latestIssueId) {
          callback([], error)
        }
      }
    }, debounceTime)
  }

  /**
   * 取消当前请求
   */
  cancel(): void {
    if (this.cancelToken) {
      this.cancelToken.abort()
      this.cancelToken = null
    }
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer)
      this.debounceTimer = null
    }
  }

  /**
   * 解析 API 响应数据
   * @param response API 响应
   * @returns OptionItem[]
   */
  private parseResponse(response: any): OptionItem[] {
    try {
      // 根据您的描述，数据在 res.Data 中
      if (response && Array.isArray(response.Data)) {
        return response.Data
      }

      if (response &&response.Data&& Array.isArray(response.Data.List)) {
        return response.Data.List
      }
      
      // 兼容其他可能的格式
      if (response && Array.isArray(response.data)) {
        return response.data
      }
      
      // 如果直接是数组
      if (Array.isArray(response)) {
        return response
      }

      console.warn('InputTag API Response format not recognized:', response)
      return []
    } catch (error) {
      console.error('InputTag API Response parse error:', error)
      return []
    }
  }

  /**
   * 销毁服务，清理资源
   */
  destroy(): void {
    this.cancel()
  }
}