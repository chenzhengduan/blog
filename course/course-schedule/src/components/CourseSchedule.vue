<script setup>
import { ref, onMounted, computed } from 'vue'

// 生成时间刻度数据 - 每半小时为一个单位
const timeSlots = Array.from({ length: 48 }, (_, i) => {
  const hour = Math.floor(i / 2)
  const minute = i % 2 === 0 ? '00' : '30'
  return { value: i / 2, label: `${hour}:${minute}` }
})

// 生成一周的日期数据
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 时间格式转换函数
const parseTime = (timeStr) => {
  const [hours, minutes] = timeStr.split(':').map(Number)
  return hours + minutes / 60
}

const formatTime = (timeNum) => {
  const hours = Math.floor(timeNum)
  const minutes = Math.round((timeNum - hours) * 60)
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
}

// 模拟课程数据 - 使用标准时间格式
const courses = ref([
  // 周一上午的课程 - 8点开始有7门课重叠
  {
    id: 1,
    name: '高等数学',
    day: 0, // 周一
    startTime: '08:00', // 8:00开始
    endTime: '10:00', // 10:00结束
    color: '#e6f4ff'
  },
  {
    id: 2,
    name: '大学物理',
    day: 0,
    startTime: '08:00',
    endTime: '10:00',
    color: '#fff7e6'
  },
  {
    id: 3,
    name: '英语',
    day: 0,
    startTime: '10:00',
    endTime: '12:00',
    color: '#f6ffed'
  },
  {
    id: 4,
    name: '计算机基础',
    day: 0,
    startTime: '08:00',
    endTime: '13:00',
    color: '#fff2f0'
  },
  {
    id: 5,
    name: '线性代数',
    day: 0,
    startTime: '08:00',
    endTime: '11:00',
    color: '#fcffe6'
  },
  {
    id: 6,
    name: '编程入门',
    day: 0,
    startTime: '08:00',
    endTime: '09:30',
    color: '#e6fffb'
  },
  {
    id: 7,
    name: '数据库',
    day: 0,
    startTime: '08:00',
    endTime: '09:00',
    color: '#f9f0ff'
  },
  // 周一下午的课程 - 包含短时间课程
  {
    id: 8,
    name: '算法导论',
    day: 0,
    startTime: '14:00',
    endTime: '16:00',
    color: '#fff0f6'
  },
  {
    id: 9,
    name: '短课1',
    day: 0,
    startTime: '14:05',
    endTime: '14:10', // 10分钟课程
    color: '#e6f7ff'
  },
  {
    id: 10,
    name: '短课2',
    day: 0,
    startTime: '14:15', // 
    endTime: '14:40', // 10分钟课程
    color: '#fff1f0'
  }
])

// 计算每个时间段的课程
const computeCourseSlots = () => {
  const courseSlots = new Map()
  
  courses.value.forEach(course => {
    // 将时间字符串转换为数字，然后转换为半小时为单位的索引
    const startTimeNum = parseTime(course.startTime)
    const endTimeNum = parseTime(course.endTime)
    const startSlot = Math.floor(startTimeNum * 2)
    const endSlot = Math.ceil(endTimeNum * 2) // 使用Math.ceil确保短时间课程也能被包含
    
    for (let slot = startSlot; slot < endSlot; slot++) {
      const hour = slot / 2
      const key = `${course.day}-${hour}`
      if (!courseSlots.has(key)) {
        courseSlots.set(key, [])
      }
      courseSlots.get(key).push(course)
    }
  })
  
  return courseSlots
}

// 全局缓存，存储每天每个课程的布局信息
const dayLayoutCache = new Map()

// 计算某一天所有课程的布局
const calculateDayLayout = (dayIndex) => {
  const cacheKey = `day-${dayIndex}`
  
  // 获取当天所有课程并排序
  const allCoursesInDay = courses.value.filter(c => c.day === dayIndex)
  const sortedCourses = [...allCoursesInDay].sort((a, b) => {
    const aStartTime = parseTime(a.startTime)
    const bStartTime = parseTime(b.startTime)
    if (aStartTime !== bStartTime) {
      return aStartTime - bStartTime
    }
    const aEndTime = parseTime(a.endTime)
    const bEndTime = parseTime(b.endTime)
    if (aEndTime !== bEndTime) {
      return aEndTime - bEndTime
    }
    return a.id - b.id
  })
  
  const layouts = new Map()
  const processedCourses = new Set()
  
  // 辅助函数：检查两个课程是否冲突
  const hasConflict = (course1, course2) => {
    const start1 = parseTime(course1.startTime)
    const end1 = parseTime(course1.endTime)
    const start2 = parseTime(course2.startTime)
    const end2 = parseTime(course2.endTime)
    return Math.max(start1, start2) < Math.min(end1, end2)
  }
  
  // 找到所有冲突组（使用传递性冲突关系）
  const conflictGroups = []
  
  sortedCourses.forEach(course => {
    if (processedCourses.has(course.id)) {
      return
    }
    
    // 找到与当前课程直接冲突的课程
    const directConflicts = sortedCourses.filter(c => 
      !processedCourses.has(c.id) && hasConflict(course, c)
    )
    
    if (directConflicts.length === 0) {
      // 没有冲突，占满宽度
      layouts.set(course.id, { width: '100%', left: '0%' })
      processedCourses.add(course.id)
    } else {
      // 构建完整的冲突组（包括传递性冲突）
      const conflictGroup = new Set([course, ...directConflicts])
      let changed = true
      
      // 继续查找传递性冲突，直到没有新的冲突课程
      while (changed) {
        changed = false
        const currentGroup = Array.from(conflictGroup)
        
        currentGroup.forEach(groupCourse => {
          sortedCourses.forEach(otherCourse => {
            if (!conflictGroup.has(otherCourse) && !processedCourses.has(otherCourse.id)) {
              // 检查是否与组内任何课程冲突
              if (hasConflict(groupCourse, otherCourse)) {
                conflictGroup.add(otherCourse)
                changed = true
              }
            }
          })
        })
      }
      
      const allConflictCourses = Array.from(conflictGroup)
      
      // 按开始时间排序冲突课程
      allConflictCourses.sort((a, b) => {
        const aStart = parseTime(a.startTime)
        const bStart = parseTime(b.startTime)
        if (aStart !== bStart) return aStart - bStart
        return a.id - b.id
      })
      
      // 分析冲突模式：找到最长的课程和其他短课程
      const courseDurations = allConflictCourses.map(c => ({
        course: c,
        duration: parseTime(c.endTime) - parseTime(c.startTime)
      }))
      
      // 按持续时间排序
      courseDurations.sort((a, b) => b.duration - a.duration)
      
      // 检查是否是长课程与短课程的组合
      const longestDuration = courseDurations[0].duration
      const longestCourse = courseDurations[0].course
      
      // 如果最长课程持续时间>=1.5小时，且与它冲突的都是短课程(<0.5小时)，使用50%-50%布局
      const isLongShortPattern = longestDuration >= 1.5 && 
        courseDurations.slice(1).every(cd => cd.duration < 0.5)
      
      if (isLongShortPattern) {
        // 特殊布局：长课程50%在左边，短课程50%在右边
        allConflictCourses.forEach((conflictCourse) => {
          if (!processedCourses.has(conflictCourse.id)) {
            if (conflictCourse.id === longestCourse.id) {
              layouts.set(conflictCourse.id, {
                width: '50%',
                left: '0%'
              })
            } else {
              layouts.set(conflictCourse.id, {
                width: '50%',
                left: '50%'
              })
            }
            processedCourses.add(conflictCourse.id)
          }
        })
      } else {
        // 普通布局：根据冲突课程数量平分宽度
        const widthPerCourse = 100 / allConflictCourses.length
        allConflictCourses.forEach((conflictCourse, index) => {
          if (!processedCourses.has(conflictCourse.id)) {
            layouts.set(conflictCourse.id, {
              width: `${widthPerCourse}%`,
              left: `${index * widthPerCourse}%`
            })
            processedCourses.add(conflictCourse.id)
          }
        })
      }
    }
  })
  
  dayLayoutCache.set(cacheKey, layouts)
  return layouts
}

// 计算课程的样式，包括高度、位置和宽度
const getCourseStyle = (course, slotTime, coursesInSlot) => {
  // 获取当天的布局信息
  const dayLayouts = calculateDayLayout(course.day)
  const layout = dayLayouts.get(course.id) || { width: '100%', left: '0%' }
  
  let width = layout.width
  let left = layout.left
  
  // 计算课程跨越的时间槽数量
  const startTimeNum = parseTime(course.startTime)
  const endTimeNum = parseTime(course.endTime)
  const durationInHours = endTimeNum - startTimeNum
  
  // 计算课程在当前时间槽的起始位置
  const courseStartSlot = Math.floor(startTimeNum * 2) // 与computeCourseSlots保持一致
  const currentSlot = Math.round(slotTime * 2)
  const isFirstSlot = courseStartSlot === currentSlot
  
  // 计算当前时间段的高度和显示方式
  let height = '30px' // 默认一个时间段的高度
  let display = 'flex'
  let top = '0px' // 默认顶部偏移
  
  // 如果是课程的第一个时间段，显示完整高度
  if (isFirstSlot) {
    height = `${durationInHours * 60}px`
    // 计算在时间槽内的精确位置偏移
    const slotStartTime = Math.floor(slotTime * 2) / 2 // 当前时间槽的开始时间
    const offsetInSlot = startTimeNum - slotStartTime // 课程开始时间相对于时间槽开始的偏移
    top = `${offsetInSlot * 60}px` // 转换为像素偏移
  } else {
    // 如果不是第一个时间段，不显示
    display = 'none'
  }
  
  return {
    width,
    left,
    top,
    backgroundColor: course.color,
    position: 'absolute',
    height,
    border: '1px solid #e8e8e8',
    borderRadius: '4px',
    padding: '4px',
    boxSizing: 'border-box',
    overflow: 'hidden',
    whiteSpace: 'nowrap',
    textOverflow: 'ellipsis',
    fontSize: '12px',
    display,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1
  }
}

const courseSlots = computed(() => computeCourseSlots())
</script>

<template>
  <div class="schedule-container">
    <!-- 表头行 -->
    <div class="header">
      <div class="time-column"></div>
      <div
        v-for="(day, index) in weekDays"
        :key="index"
        class="day-column"
      >
        {{ day }}
      </div>
      <!-- 滚动条占位 -->
      <div class="scrollbar-placeholder"></div>
    </div>
    
    <!-- 时间轴和课程内容 -->
    <div class="schedule-content">
      <div class="time-list">
        <div v-for="slot in timeSlots" :key="slot.value" class="time-row">
          <div class="time-label" v-if="slot.label.endsWith('00')">{{ slot.label }}</div>
          <div class="time-label time-label-half" v-else></div>
          <div class="day-grid">
            <div
              v-for="(day, dayIndex) in weekDays"
              :key="dayIndex"
              class="grid-cell"
              :class="{ 'half-hour': slot.label.endsWith('30') }"
            >
              <template v-if="courseSlots.get(`${dayIndex}-${slot.value}`)">
                <div
                  v-for="course in courseSlots.get(`${dayIndex}-${slot.value}`)"
                  :key="`${course.id}-${slot.value}`"
                  class="course-item"
                  :style="getCourseStyle(course, slot.value, courseSlots.get(`${dayIndex}-${slot.value}`))"
                >
                  {{ course.name }}
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.schedule-container {
  width: 100%;
  height: auto;
  min-height: 600px;
  max-height: 90vh;
  margin: 20px auto;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background-color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  height: 50px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #f5f7fa;
  font-weight: 500;
  width: 100%;
  flex-shrink: 0;
}

.scrollbar-placeholder {
  width: 17px; /* 滚动条宽度 */
  flex-shrink: 0;
}

.time-column {
  width: 80px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
}

.day-column {
  flex: 1;
  min-width: 150px; /* 增加最小宽度以显示完整课程名称 */
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e8e8e8;
  font-weight: bold;
  color: #303133;
}

.day-column:first-of-type {
  min-width: 200px; /* 周一列设置更大宽度 */
}

.time-label {
  width: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e8e8e8;
  background-color: #f5f7fa;
  color: #606266;
  font-size: 13px;
}

.time-label-half {
  border-bottom: 1px dashed #e8e8e8;
  color: #c0c4cc;
  font-size: 10px;
}

.grid-cell {
  flex: 1;
  min-width: 150px; /* 与day-column保持一致 */
  border-right: 1px solid #e8e8e8;
  border-bottom: 1px solid #e8e8e8;
  position: relative;
  height: 30px;
  background-color: #fcfcfc;
}

.grid-cell:first-child {
  min-width: 200px; /* 周一列的grid-cell设置更大宽度 */
}

.grid-cell.half-hour {
  border-bottom: 1px dashed #e8e8e8;
  background-color: #fafafa;
}

.course-item {
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  font-weight: 500;
}

.course-item:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 2;
}

.schedule-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

.time-list {
  height: auto;
  min-height: 100%;
}

.time-row {
  display: flex;
  height: 30px; /* 每个时间槽高度为30px，一小时为60px */
}

.day-grid {
  flex: 1;
  display: flex;
}
</style>