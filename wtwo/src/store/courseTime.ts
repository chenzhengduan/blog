import { defineStore } from 'pinia'
import { queryCourseTime } from '@/api'
import { calculateDuration } from '@/utils/timeUtils'

export const useCourseTime = defineStore('courseTime', {
  state: () => ({
    courseTimeCache: new Map<string, any[]>()
  }),
  
  actions: {
    // 获取上课时间数据（带缓存）
    async getCourseTimes(campusId: string) {
      // 如果缓存中已有数据，直接返回
      if (this.courseTimeCache.has(campusId)) {
        return this.courseTimeCache.get(campusId)!
      }
      
      try {
        const res = await queryCourseTime({ campusId })
        const data = res.Data || []
        
        // 计算每个时段的持续时间
        data.forEach((item: any) => {
          item.Duration = calculateDuration([item.StartTime, item.EndTime])
        })
        
        // 缓存数据
        this.courseTimeCache.set(campusId, data)
        
        return data
      } catch (error) {
        console.error('查询上课时间失败:', error)
        return []
      }
    },
    
    // 清除指定校区的缓存
    clearCache(campusId: string) {
      this.courseTimeCache.delete(campusId)
    },
    
    // 清除所有缓存
    clearAllCache() {
      this.courseTimeCache.clear()
    }
  }
}) 