<!-- 周一数据多 -->
<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import helperDrawer from './helpDrawer.vue';

const scheduleContainerRef = ref(null)
const isFullscreen = ref(false)

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const helperDrawerRef = ref();

function openHelp(){
    helperDrawerRef.value?.open({})
}
// 生成时间刻度数据 - 每半小时为一个单位，每小时120px
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

// 将时间字符串转换为分钟数（从0点开始计算）
const timeToMinutes = (timeStr) => {
  const [hours, minutes] = timeStr.split(':').map(Number);
  return hours * 60 + minutes;
};

// 当前时间线位置
const currentTimePosition = ref(0)
const currentDay = ref(new Date().getDay() - 1) // 0表示周一，-1表示周日
const showCurrentTimeLine = ref(true)

// 更新当前时间线位置
const updateCurrentTimePosition = () => {
  const now = new Date()
  const hours = now.getHours()
  const minutes = now.getMinutes()
  currentTimePosition.value = (hours + minutes / 60) * 120 // 每小时120px
  currentDay.value = now.getDay() === 0 ? 6 : now.getDay() - 1 // 调整为0-6，0表示周一
}

// 定时更新当前时间线
let timeUpdateInterval = null

onMounted(() => {
  console.log(window.microApp,'window.microApp')
  
  // 清理可能存在的旧监听器
  if (window.microApp && window.microApp.clearDataListener) {
    // window.microApp.clearDataListener();
  }
  
  // 注册新的监听器
  if (window.microApp && window.microApp.addDataListener) {
    window.microApp.addDataListener((data) => {
      console.log('接收到数据:', data)
    });
  }
  
  updateCurrentTimePosition()
  timeUpdateInterval = setInterval(updateCurrentTimePosition, 60000) // 每分钟更新一次
})

onUnmounted(() => {
  // 清理定时器
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }
  
  // 清理数据监听器
  if (window.microApp && window.microApp.clearDataListener) {
    // window.microApp.clearDataListener();
    console.log('已清理数据监听器');
  }
})

// 模拟课程数据 - 只保留周一课程，增加更多时间段和时长
const courses = ref([
  // 周一上午的课程 - 8点开始有多门课重叠
  {
    id: 1,
    name: '高等数学',
    day: 0, // 周一
    startTime: '08:00', // 8:00开始
    endTime: '10:00', // 2小时课程
    color: '#1890ff' // 深蓝色
  },
  {
    id: 2,
    name: '大学物理',
    day: 0,
    startTime: '08:15', // 8:15开始
    endTime: '09:15', // 1小时课程
    color: '#fa8c16' // 深橙色
  },
  {
    id: 3,
    name: '线性代数',
    day: 0,
    startTime: '08:30', // 8:30开始
    endTime: '09:00', // 半小时课程
    color: '#fadb14' // 深黄色
  },
  {
    id: 4,
    name: '编程入门',
    day: 0,
    startTime: '08:45', // 8:45开始
    endTime: '10:45', // 2小时课程
    color: '#13c2c2' // 深青色
  },
  {
    id: 5,
    name: '数据库',
    day: 0,
    startTime: '09:00', // 9:00开始
    endTime: '10:00', // 1小时课程
    color: '#722ed1' // 深紫色
  },
  {
    id: 6,
    name: '计算机基础',
    day: 0,
    startTime: '09:15', // 9:15开始
    endTime: '10:15', // 1小时课程
    color: '#f5222d' // 深红色
  },
  {
    id: 7,
    name: '概率论',
    day: 0,
    startTime: '09:30', // 9:30开始
    endTime: '10:00', // 半小时课程
    color: '#52c41a' // 深绿色
  },
  {
    id: 8,
    name: '软件工程',
    day: 0,
    startTime: '09:45', // 9:45开始
    endTime: '11:15', // 1.5小时课程
    color: '#eb2f96' // 深粉色
  },
  // 周一上午晚些时候
  {
    id: 9,
    name: '英语',
    day: 0,
    startTime: '10:00', // 10:00开始
    endTime: '12:00', // 2小时课程
    color: '#2f54eb' // 深蓝紫色
  },
  {
    id: 10,
    name: '体育',
    day: 0,
    startTime: '10:30', // 10:30开始
    endTime: '11:30', // 1小时课程
    color: '#fa541c' // 深橙红色
  },
  {
    id: 11,
    name: '心理学',
    day: 0,
    startTime: '11:00', // 11:00开始
    endTime: '11:30', // 半小时课程
    color: '#1890ff' // 深蓝色
  },
  {
    id: 12,
    name: '哲学',
    day: 0,
    startTime: '11:15', // 11:15开始
    endTime: '12:15', // 1小时课程
    color: '#fa8c16' // 深橙色
  },
  // 周一下午课程
  {
    id: 13,
    name: '算法导论',
    day: 0,
    startTime: '13:00', // 13:00开始
    endTime: '15:00', // 2小时课程
    color: '#fadb14' // 深黄色
  },
  {
    id: 14,
    name: '数据结构',
    day: 0,
    startTime: '13:15', // 13:15开始
    endTime: '14:15', // 1小时课程
    color: '#13c2c2' // 深青色
  },
  {
    id: 15,
    name: '操作系统',
    day: 0,
    startTime: '13:30', // 13:30开始
    endTime: '14:00', // 半小时课程
    color: '#722ed1' // 深紫色
  },
  {
    id: 16,
    name: '计算机网络',
    day: 0,
    startTime: '13:45', // 13:45开始
    endTime: '15:45', // 2小时课程
    color: '#f5222d' // 深红色
  },
  {
    id: 17,
    name: '人工智能',
    day: 0,
    startTime: '14:00', // 14:00开始
    endTime: '15:00', // 1小时课程
    color: '#52c41a' // 深绿色
  },
  {
    id: 18,
    name: '机器学习',
    day: 0,
    startTime: '14:30', // 14:30开始
    endTime: '17:30', // 1小时课程
    color: '#eb2f96' // 深粉色
  },
  // 周一下午晚些时候
  {
    id: 19,
    name: '数据挖掘',
    day: 0,
    startTime: '15:00', // 15:00开始
    endTime: '16:00', // 1小时课程
    color: '#2f54eb' // 深蓝紫色
  },
  {
    id: 20,
    name: '编译原理',
    day: 0,
    startTime: '12:15', // 15:15开始
    endTime: '15:45', // 半小时课程
    color: '#fa541c' // 深橙红色
  },
  {
    id: 21,
    name: '数字电路',
    day: 0,
    startTime: '15:30', // 15:30开始
    endTime: '17:30', // 2小时课程
    color: '#1890ff' // 深蓝色
  },
  {
    id: 22,
    name: '微机原理',
    day: 0,
    startTime: '15:45', // 15:45开始
    endTime: '16:45', // 1小时课程
    color: '#fa8c16' // 深橙色
  },
  // 周一傍晚课程
  {
    id: 23,
    name: '创新实践',
    day: 0,
    startTime: '16:00', // 16:00开始
    endTime: '17:00', // 1小时课程
    color: '#fadb14' // 深黄色
  },
  {
    id: 24,
    name: '项目管理',
    day: 0,
    startTime: '16:30', // 16:30开始
    endTime: '17:00', // 半小时课程
    color: '#13c2c2' // 深青色
  },
  {
    id: 25,
    name: '实习指导',
    day: 0,
    startTime: '17:00', // 17:00开始
    endTime: '18:00', // 1小时课程
    color: '#722ed1' // 深紫色
  },
  // 周一早晨课程 - 6:00-8:00时段
  {
    id: 32,
    name: '早读英语',
    day: 0,
    startTime: '06:00',
    endTime: '07:00',
    color: '#096dd9'
  },
  {
    id: 33,
    name: '晨练',
    day: 0,
    startTime: '06:30',
    endTime: '07:30',
    color: '#d48806'
  },
  {
    id: 34,
    name: '晨会',
    day: 0,
    startTime: '07:00',
    endTime: '08:00',
    color: '#d4b106'
  },
  {
    id: 35,
    name: '学术讲座',
    day: 0,
    startTime: '07:15',
    endTime: '08:15',
    color: '#08979c'
  },
  {
    id: 36,
    name: '课前预习',
    day: 0,
    startTime: '07:30',
    endTime: '08:00',
    color: '#531dab'
  },
  {
    id: 37,
    name: '社团活动',
    day: 0,
    startTime: '07:45',
    endTime: '08:45',
    color: '#cf1322'
  },
  // 周一上午额外课程 - 8:00-12:00增加更多冲突
  {
    id: 38,
    name: '数学分析',
    day: 0,
    startTime: '08:00',
    endTime: '09:30',
    color: '#389e0d'
  },
  {
    id: 39,
    name: '统计学',
    day: 0,
    startTime: '08:20',
    endTime: '10:20',
    color: '#c41d7f'
  },
  {
    id: 40,
    name: '电子技术',
    day: 0,
    startTime: '08:40',
    endTime: '09:40',
    color: '#1d39c4'
  },
  {
    id: 41,
    name: '通信原理',
    day: 0,
    startTime: '09:00',
    endTime: '11:00',
    color: '#d4380d'
  },
  {
    id: 42,
    name: '信号处理',
    day: 0,
    startTime: '09:20',
    endTime: '10:50',
    color: '#096dd9'
  },
  {
    id: 43,
    name: '自动化控制',
    day: 0,
    startTime: '10:00',
    endTime: '11:00',
    color: '#d48806'
  },
  {
    id: 44,
    name: '嵌入式系统',
    day: 0,
    startTime: '10:15',
    endTime: '12:15',
    color: '#d4b106'
  },
  {
    id: 45,
    name: '图像处理',
    day: 0,
    startTime: '10:45',
    endTime: '11:45',
    color: '#08979c'
  },
  {
    id: 46,
    name: '模式识别',
    day: 0,
    startTime: '11:00',
    endTime: '12:30',
    color: '#531dab'
  },
  {
    id: 47,
    name: '计算机视觉',
    day: 0,
    startTime: '11:30',
    endTime: '12:30',
    color: '#cf1322'
  },
  // 周一中午课程 - 12:00-13:00时段
  {
    id: 48,
    name: '学术研讨',
    day: 0,
    startTime: '12:00',
    endTime: '13:00',
    color: '#389e0d'
  },
  {
    id: 49,
    name: '导师会议',
    day: 0,
    startTime: '12:15',
    endTime: '13:15',
    color: '#c41d7f'
  },
  {
    id: 50,
    name: '项目讨论',
    day: 0,
    startTime: '12:30',
    endTime: '13:30',
    color: '#1d39c4'
  },
  {
    id: 51,
    name: '技术分享',
    day: 0,
    startTime: '12:45',
    endTime: '13:45',
    color: '#d4380d'
  },
  // 周一下午额外课程 - 13:00-18:00增加更多冲突
  {
    id: 52,
    name: 'Web安全',
    day: 0,
    startTime: '13:00',
    endTime: '14:30',
    color: '#096dd9'
  },
  {
    id: 53,
    name: '区块链技术',
    day: 0,
    startTime: '13:20',
    endTime: '15:20',
    color: '#d48806'
  },
  {
    id: 54,
    name: '云计算架构',
    day: 0,
    startTime: '13:40',
    endTime: '14:40',
    color: '#d4b106'
  },
  {
    id: 55,
    name: '大数据分析',
    day: 0,
    startTime: '14:00',
    endTime: '16:00',
    color: '#08979c'
  },
  {
    id: 56,
    name: '深度学习实践',
    day: 0,
    startTime: '14:20',
    endTime: '16:20',
    color: '#531dab'
  },
  {
    id: 57,
    name: '自然语言处理',
    day: 0,
    startTime: '14:45',
    endTime: '15:45',
    color: '#cf1322'
  },
  {
    id: 58,
    name: '推荐系统',
    day: 0,
    startTime: '15:00',
    endTime: '17:00',
    color: '#389e0d'
  },
  {
    id: 59,
    name: '强化学习',
    day: 0,
    startTime: '15:15',
    endTime: '16:15',
    color: '#c41d7f'
  },
  {
    id: 60,
    name: '计算机图形学',
    day: 0,
    startTime: '15:45',
    endTime: '17:45',
    color: '#1d39c4'
  },
  {
    id: 61,
    name: '游戏开发',
    day: 0,
    startTime: '16:00',
    endTime: '18:00',
    color: '#d4380d'
  },
  {
    id: 62,
    name: 'VR/AR开发',
    day: 0,
    startTime: '16:15',
    endTime: '17:15',
    color: '#096dd9'
  },
  {
    id: 63,
    name: '移动应用开发',
    day: 0,
    startTime: '16:45',
    endTime: '18:45',
    color: '#d48806'
  },
  {
    id: 64,
    name: '前端进阶',
    day: 0,
    startTime: '17:00',
    endTime: '18:30',
    color: '#d4b106'
  },
  {
    id: 65,
    name: '后端架构',
    day: 0,
    startTime: '17:15',
    endTime: '18:15',
    color: '#08979c'
  },
  // 周一晚上课程 - 18:00-22:00时段
  {
    id: 66,
    name: '晚间讲座',
    day: 0,
    startTime: '18:00',
    endTime: '19:30',
    color: '#531dab'
  },
  {
    id: 67,
    name: '技术沙龙',
    day: 0,
    startTime: '18:15',
    endTime: '20:15',
    color: '#cf1322'
  },
  {
    id: 68,
    name: '代码评审',
    day: 0,
    startTime: '18:30',
    endTime: '19:30',
    color: '#389e0d'
  },
  {
    id: 69,
    name: '开源项目',
    day: 0,
    startTime: '19:00',
    endTime: '21:00',
    color: '#c41d7f'
  },
  {
    id: 70,
    name: '算法竞赛',
    day: 0,
    startTime: '19:15',
    endTime: '20:15',
    color: '#1d39c4'
  },
  {
    id: 71,
    name: '创业分享',
    day: 0,
    startTime: '19:30',
    endTime: '21:30',
    color: '#d4380d'
  },
  {
    id: 72,
    name: '职业规划',
    day: 0,
    startTime: '20:00',
    endTime: '21:00',
    color: '#096dd9'
  },
  {
    id: 73,
    name: '面试技巧',
    day: 0,
    startTime: '20:15',
    endTime: '22:15',
    color: '#d48806'
  },
  {
    id: 74,
    name: '行业动态',
    day: 0,
    startTime: '20:30',
    endTime: '21:30',
    color: '#d4b106'
  },
  {
    id: 75,
    name: '技术趋势',
    day: 0,
    startTime: '21:00',
    endTime: '22:00',
    color: '#08979c'
  },
  // 周二课程 - 相对较少
  {
    id: 26,
    name: 'Java编程',
    day: 1, // 周二
    startTime: '09:00',
    endTime: '11:00',
    color: '#1890ff'
  },
  {
    id: 27,
    name: '数据库设计',
    day: 1,
    startTime: '09:30',
    endTime: '10:30',
    color: '#fa8c16'
  },
  {
    id: 28,
    name: 'Web开发',
    day: 1,
    startTime: '14:00',
    endTime: '16:00',
    color: '#52c41a'
  },
  {
    id: 29,
    name: '软件测试',
    day: 1,
    startTime: '14:30',
    endTime: '15:30',
    color: '#f5222d'
  },
  {
    id: 30,
    name: '项目实践',
    day: 1,
    startTime: '16:00',
    endTime: '17:30',
    color: '#722ed1'
  },
  {
    id: 31,
    name: '项目测试',
    day: 1,
    startTime: '14:00',
    endTime: '17:30',
    color: '#722ed1'
  }
])

// 计算某一天所有课程的布局 - 重新设计的冲突处理算法
const calculateDayLayout = (dayIndex) => {
  // 获取当天所有课程并按开始时间排序
  const allCoursesInDay = courses.value.filter(c => c.day === dayIndex)
  
  if (allCoursesInDay.length === 0) {
    return new Map()
  }
  
  const sortedCourses = [...allCoursesInDay].sort((a, b) => {
    const aStart = parseTime(a.startTime)
    const bStart = parseTime(b.startTime)
    if (aStart !== bStart) return aStart - bStart
    return parseTime(a.endTime) - parseTime(b.endTime)
  })
  
  if (dayIndex === 0) {
    console.log('\n=== 新的课程冲突处理算法 ===')
    console.log('处理课程顺序:', sortedCourses.map(c => `${c.name}(${c.startTime}-${c.endTime})`))
  }
  
  // 第一步：建立所有课程的时间冲突关系
  const conflicts = new Map() // 存储每门课程与其他课程的冲突关系
  
  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.startTime)
    const courseEnd = parseTime(course.endTime)
    const conflictingCourses = new Set()
    
    // 找出与当前课程时间有重叠的所有课程
    sortedCourses.forEach(otherCourse => {
      if (course.id === otherCourse.id) return
      
      const otherStart = parseTime(otherCourse.startTime)
      const otherEnd = parseTime(otherCourse.endTime)
      
      // 检查时间是否重叠：两个课程有重叠时间
      if (courseStart < otherEnd && courseEnd > otherStart) {
        conflictingCourses.add(otherCourse.id)
      }
    })
    
    conflicts.set(course.id, conflictingCourses)
  })
  
  if (dayIndex === 0) {
    console.log('冲突关系分析:')
    conflicts.forEach((conflictSet, courseId) => {
      const course = sortedCourses.find(c => c.id === courseId)
      const conflictNames = Array.from(conflictSet).map(id => 
        sortedCourses.find(c => c.id === id)?.name
      )
      console.log(`${course.name}: 与 [${conflictNames.join(', ')}] 冲突`)
    })
  }
  
  // 第二步：计算全天最大同时冲突数（决定列数）
  let maxColumns = 1
  
  // 为了准确计算，我们需要找出任意时间点的最大并发课程数
  const timePoints = new Set()
  
  sortedCourses.forEach(course => {
    const startMinutes = Math.round(parseTime(course.startTime) * 60)
    const endMinutes = Math.round(parseTime(course.endTime) * 60)
    timePoints.add(startMinutes)
    timePoints.add(endMinutes)
  })
  
  Array.from(timePoints).sort((a, b) => a - b).forEach(timePoint => {
    let concurrentCourses = 0
    
    sortedCourses.forEach(course => {
      const startMinutes = Math.round(parseTime(course.startTime) * 60)
      const endMinutes = Math.round(parseTime(course.endTime) * 60)
      
      if (startMinutes <= timePoint && timePoint < endMinutes) {
        concurrentCourses++
      }
    })
    
    maxColumns = Math.max(maxColumns, concurrentCourses)
  })
  
  if (dayIndex === 0) {
    console.log(`全天最大并发课程数: ${maxColumns}`)
  }
  
  // 第三步：使用贪心算法为每门课程分配列位置
  const coursePositions = new Map()
  const columnOccupancy = Array.from({ length: maxColumns }, () => []) // 每列的占用时间段
  
  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.startTime)
    const courseEnd = parseTime(course.endTime)
    
    // 找到第一个可用的列
    let assignedColumn = -1
    
    for (let col = 0; col < maxColumns; col++) {
      let canUseColumn = true
      
      // 检查这一列是否在当前课程时间段内有冲突
      for (const occupiedPeriod of columnOccupancy[col]) {
        const occupiedStart = occupiedPeriod.start
        const occupiedEnd = occupiedPeriod.end
        
        // 检查时间是否重叠
        if (courseStart < occupiedEnd && courseEnd > occupiedStart) {
          canUseColumn = false
          break
        }
      }
      
      if (canUseColumn) {
        assignedColumn = col
        // 将当前课程的时间段标记为占用
        columnOccupancy[col].push({
          start: courseStart,
          end: courseEnd,
          courseId: course.id
        })
        break
      }
    }
    
    coursePositions.set(course.id, assignedColumn)
    
    if (dayIndex === 0) {
      console.log(`${course.name} (${course.startTime}-${course.endTime}) 分配到列 ${assignedColumn}`)
    }
  })
  
  // 第四步：计算最终布局样式
  const layouts = new Map()
  const columnWidth = 100 / maxColumns // 所有课程使用统一的列宽
  
  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.startTime)
    const courseEnd = parseTime(course.endTime)
    const courseDuration = courseEnd - courseStart
    const column = coursePositions.get(course.id)
    
    layouts.set(course.id, {
      width: `${columnWidth}%`,
      left: `${column * columnWidth}%`,
      top: `${courseStart * 120}px`,
      height: `${courseDuration * 120}px`
    })
    
    if (dayIndex === 0) {
      console.log(`${course.name}: 列${column}, 宽度${columnWidth.toFixed(1)}%, 左边距${(column * columnWidth).toFixed(1)}%`)
    }
  })
  
  if (dayIndex === 0) {
    console.log('\n=== 列占用情况 ===')
    columnOccupancy.forEach((periods, colIndex) => {
      console.log(`列${colIndex}:`, periods.map(p => {
        const courseName = sortedCourses.find(c => c.id === p.courseId)?.name
        return `${courseName}(${formatTime(p.start)}-${formatTime(p.end)})`
      }).join(', '))
    })
    console.log('\n=== 课程布局计算完成 ===\n')
  }
  
  return layouts
}

// 使用computed缓存布局计算结果
const mondayLayouts = computed(() => calculateDayLayout(0))

// 重新设计getCourseStyle函数，使用绝对定位
const getCourseStyle = (course, day) => {
  let dayLayouts
  if (day === 0) {
    dayLayouts = mondayLayouts.value
  } else {
    dayLayouts = calculateDayLayout(day)
  }
  
  const layout = dayLayouts.get(course.id);
  
  if (!layout) {
    return { display: 'none' };
  }
  
  return {
    position: 'absolute',
    top: layout.top,
    left: layout.left,
    width: layout.width,
    height: layout.height,
    backgroundColor: course.color || '#3498db',
    border: '1px solid rgba(255, 255, 255, 0.3)',
    borderRadius: '4px',
    padding: '4px',
    fontSize: '12px',
    color: '#ffffff', // 白色文字
    overflow: 'hidden',
    boxSizing: 'border-box',
    zIndex: 2,
    boxShadow: '0 2px 6px rgba(0, 0, 0, 0.2)',
    transition: 'all 0.2s ease',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'flex-start',
    textAlign: 'left',
    lineHeight: '1.2',
    wordBreak: 'break-word',
    fontWeight: '500' // 增加字体粗细
  };
};
let input=ref('')
</script>

<template>
  <helperDrawer  ref="helperDrawerRef"></helperDrawer>
  <el-input v-model="input" style="width: 240px" placeholder="Please input" />
  <div @click="openHelp">123213</div>
  <div class="schedule-container" :class="{'is-fullscreen': isFullscreen}" ref="scheduleContainerRef">
    <!-- 表头行 -->
    <div class="header">
      <div class="time-column">
        <el-button @click="toggleFullscreen" type="primary" plain size="small" style="margin-left: 5px;">
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
      </div>
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
        <!-- 时间标签列 -->
        <div class="time-labels">
          <div v-for="slot in timeSlots" :key="slot.value" class="time-row">
            <div class="time-label" v-if="slot.label.endsWith('00')">{{ slot.label }}</div>
            <div class="time-label-empty" v-else></div>
          </div>
        </div>
        
        <!-- 课程网格 -->
        <div class="schedule-grid">
          <div
            v-for="(day, dayIndex) in weekDays"
            :key="dayIndex"
            class="day-column-container"
          >
            <!-- 网格背景 -->
            <div class="grid-background">
              <div
                v-for="slot in timeSlots"
                :key="slot.value"
                class="grid-cell"
                :class="{ 'half-hour': slot.label.endsWith('30') }"
                :style="{ height: '60px' }"
              ></div>
            </div>
            
            <!-- 课程容器 - 绝对定位 -->
            <div class="courses-container">
              <div
                v-for="course in courses.filter(c => c.day === dayIndex)"
                :key="course.id"
                class="course-item"
                :style="getCourseStyle(course, dayIndex)"
                :title="`${course.name}\n${course.startTime} - ${course.endTime}`"
              >
                {{ course.name }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 当前时间线 -->
        <div 
          v-if="showCurrentTimeLine" 
          class="current-time-line"
          :style="{ top: `${currentTimePosition}px` }"
        >
          <div class="current-time-dot"></div>
          <div class="current-time-label">{{ formatTime(currentTimePosition / 60) }}</div>
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
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.schedule-container.is-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  border-radius: 0;
  max-height: 100%;
  z-index: 2000;
  border: none;
  box-shadow: none;
}

.header {
  display: flex;
  height: 50px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #f5f7fa;
  font-weight: 500;
  width: 100%;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.scrollbar-placeholder {
  width: 17px; /* 滚动条宽度 */
  flex-shrink: 0;
}

.time-column {
  width: 80px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background-color: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  color: #666;
  flex-direction: column;
  padding: 4px;
}

.time-column .el-button {
  font-size: 10px;
  padding: 4px 8px;
}

.day-column {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #333;
  border-right: 1px solid #e8e8e8;
}

.day-column:last-child {
  border-right: none;
}

.time-labels {
  width: 80px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background-color: #fafafa;
  position: relative;
}

.time-row {
  height: 60px;
  display: flex;
  align-items: flex-start;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
}

.time-label {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 4px 8px 0 4px;
  font-size: 11px;
  color: #666;
  font-weight: 500;
  background-color: #fafafa;
  position: relative;
  line-height: 1;
}

.time-label-empty {
  width: 100%;
  height: 60px;
  background-color: #fafafa;
  position: relative;
}

.course-item {
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  font-weight: 500;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.2;
  margin: 1px;
  user-select: none;
  transition: all 0.2s ease;
}

.course-item:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.course-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  z-index: 100 !important;
}

.schedule-content {
  flex: 1;
  overflow: auto;
  position: relative;
}

.time-list {
  display: flex;
  flex-direction: row;
  position: relative;
  height: 100%;
}

.schedule-grid {
  display: flex;
  flex: 1;
  position: relative;
}

.day-column-container {
  flex: 1;
  position: relative;
  border-right: 1px solid #e8e8e8;
  min-height: 100%;
  min-width: 150px;
  overflow: hidden; /* 防止被内容撑开 */
}

.day-column-container:last-child {
  border-right: none;
}

.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.courses-container {
  position: relative;
  z-index: 2;
  height: 100%;
  width: 100%;
}

.grid-cell {
  height: 60px;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  background-color: #fcfcfc;
}

.grid-cell.half-hour {
  border-bottom: 1px dashed #f5f5f5;
  background-color: #fafafa;
}

.current-time-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #ff4d4f;
  z-index: 100;
  display: flex;
  align-items: center;
}

.current-time-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ff4d4f;
  margin-left: 76px;
  margin-right: 8px;
}

.current-time-label {
  background-color: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

/* 添加响应式样式 */
@media screen and (max-width: 768px) {
  .day-column, .day-column-container {
    min-width: 100px;
  }
  
  .time-column, .time-labels {
    width: 60px;
  }
  
  .time-column .el-button {
    font-size: 9px;
    padding: 2px 4px;
  }
}
</style>