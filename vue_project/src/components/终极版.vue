<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import helperDrawer from './helpDrawer.vue';

const scheduleContainerRef = ref(null)
const isFullscreen = ref(false)

// 添加悬浮状态管理
const hoveredCourse = ref(null)
const hoverTimer = ref(null)

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

// 鼠标悬浮处理函数
const handleCourseMouseEnter = (course) => {
  // 清除之前的定时器
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
  }
  
  // 延迟0.5秒后显示毛玻璃效果
  hoverTimer.value = setTimeout(() => {
    hoveredCourse.value = course
  }, 500)
}

const handleCourseMouseLeave = () => {
  // 清除定时器
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }
  
  // 立即清除毛玻璃效果
  hoveredCourse.value = null
}

// 判断课程是否应该显示毛玻璃效果
const shouldShowBlurEffect = (course) => {
  if (!hoveredCourse.value) return false
  
  // 如果是同名课程（包括带序号的），不显示毛玻璃效果
  const hoveredCourseName = hoveredCourse.value.name.replace(/\(\d+\)$/, '') // 去掉序号
  const currentCourseName = course.name.replace(/\(\d+\)$/, '') // 去掉序号
  
  return hoveredCourseName !== currentCourseName
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

// 定义20门基础课程
const baseCourses = [
  { name: '高等数学', className: '数学1班', studentName: '张小明', color: '#1890ff' },
  { name: '大学物理', className: '物理1班', studentName: '李小红', color: '#fa8c16' },
  { name: '线性代数', className: '数学2班', studentName: '王小强', color: '#fadb14' },
  { name: '编程入门', className: '计算机1班', studentName: '陈小华', color: '#13c2c2' },
  { name: '数据库原理', className: '软件1班', studentName: '刘小磊', color: '#722ed1' },
  { name: '计算机网络', className: '网络1班', studentName: '赵小丽', color: '#f5222d' },
  { name: '操作系统', className: '系统1班', studentName: '孙小军', color: '#52c41a' },
  { name: '软件工程', className: '工程1班', studentName: '周小芳', color: '#eb2f96' },
  { name: '算法设计', className: '算法1班', studentName: '吴小东', color: '#2f54eb' },
  { name: '数据结构', className: '结构1班', studentName: '郑小美', color: '#fa541c' },
  { name: '人工智能', className: 'AI1班', studentName: '冯小刚', color: '#096dd9' },
  { name: '机器学习', className: 'ML1班', studentName: '何小静', color: '#d48806' },
  { name: '计算机图形学', className: '图形1班', studentName: '蒋小宇', color: '#d4b106' },
  { name: '编译原理', className: '编译1班', studentName: '韩小雪', color: '#08979c' },
  { name: '网络安全', className: '安全1班', studentName: '邓小平', color: '#531dab' },
  { name: '移动开发', className: '移动1班', studentName: '彭小江', color: '#cf1322' },
  { name: '前端开发', className: '前端1班', studentName: '曾小慧', color: '#389e0d' },
  { name: '后端开发', className: '后端1班', studentName: '肖小波', color: '#c41d7f' },
  { name: '项目管理', className: '管理1班', studentName: '廖小明', color: '#1d39c4' },
  { name: '软件测试', className: '测试1班', studentName: '姚小敏', color: '#722ed1' }
];

// 定义每天不同的时间段安排
const timeSchedules = {
  // 周一时间段
  0: [
    { startTime: '08:00', endTime: '10:00' },
    { startTime: '08:30', endTime: '09:30' },
    { startTime: '09:00', endTime: '11:00' },
    { startTime: '09:30', endTime: '10:30' },
    { startTime: '10:00', endTime: '12:00' },
    { startTime: '10:30', endTime: '11:30' },
    { startTime: '11:00', endTime: '12:00' },
    { startTime: '13:00', endTime: '15:00' },
    { startTime: '13:30', endTime: '14:30' },
    { startTime: '14:00', endTime: '16:00' },
    { startTime: '14:30', endTime: '15:30' },
    { startTime: '15:00', endTime: '17:00' },
    { startTime: '15:30', endTime: '16:30' },
    { startTime: '16:00', endTime: '18:00' },
    { startTime: '16:30', endTime: '17:30' },
    { startTime: '17:00', endTime: '18:00' },
    { startTime: '18:00', endTime: '19:00' },
    { startTime: '18:30', endTime: '19:30' },
    { startTime: '19:00', endTime: '20:00' },
    { startTime: '19:30', endTime: '20:30' }
  ],
  // 周二时间段
  1: [
    { startTime: '07:00', endTime: '09:00' },
    { startTime: '07:30', endTime: '08:30' },
    { startTime: '08:00', endTime: '10:00' },
    { startTime: '08:30', endTime: '09:30' },
    { startTime: '09:00', endTime: '11:00' },
    { startTime: '09:30', endTime: '10:30' },
    { startTime: '10:00', endTime: '11:00' },
    { startTime: '12:00', endTime: '14:00' },
    { startTime: '12:30', endTime: '13:30' },
    { startTime: '13:00', endTime: '15:00' },
    { startTime: '13:30', endTime: '14:30' },
    { startTime: '14:00', endTime: '16:00' },
    { startTime: '14:30', endTime: '15:30' },
    { startTime: '15:00', endTime: '17:00' },
    { startTime: '15:30', endTime: '16:30' },
    { startTime: '16:00', endTime: '17:00' },
    { startTime: '17:00', endTime: '18:00' },
    { startTime: '17:30', endTime: '18:30' },
    { startTime: '18:00', endTime: '19:00' },
    { startTime: '18:30', endTime: '19:30' }
  ],
  // 周三时间段
  2: [
    { startTime: '06:30', endTime: '08:30' },
    { startTime: '07:00', endTime: '08:00' },
    { startTime: '07:30', endTime: '09:30' },
    { startTime: '08:00', endTime: '09:00' },
    { startTime: '08:30', endTime: '10:30' },
    { startTime: '09:00', endTime: '10:00' },
    { startTime: '09:30', endTime: '10:30' },
    { startTime: '11:00', endTime: '13:00' },
    { startTime: '11:30', endTime: '12:30' },
    { startTime: '12:00', endTime: '14:00' },
    { startTime: '12:30', endTime: '13:30' },
    { startTime: '13:00', endTime: '15:00' },
    { startTime: '13:30', endTime: '14:30' },
    { startTime: '14:00', endTime: '16:00' },
    { startTime: '14:30', endTime: '15:30' },
    { startTime: '15:00', endTime: '16:00' },
    { startTime: '16:00', endTime: '17:00' },
    { startTime: '16:30', endTime: '17:30' },
    { startTime: '17:00', endTime: '18:00' },
    { startTime: '17:30', endTime: '18:30' },
    // 周三额外增加的时间段 - 增加同时段冲突
    { startTime: '07:00', endTime: '09:00' }, // 与前面重叠
    { startTime: '07:30', endTime: '08:30' }, // 与前面重叠
    { startTime: '08:00', endTime: '10:00' }, // 与前面重叠
    { startTime: '08:30', endTime: '09:30' }, // 与前面重叠
    { startTime: '13:00', endTime: '14:00' }, // 与前面重叠
    { startTime: '13:30', endTime: '15:30' }, // 与前面重叠
    { startTime: '14:00', endTime: '15:00' }, // 与前面重叠
    { startTime: '14:30', endTime: '16:30' }, // 与前面重叠
    { startTime: '15:00', endTime: '17:00' }, // 与前面重叠
    { startTime: '15:30', endTime: '16:30' }  // 与前面重叠
  ],
  // 周四时间段
  3: [
    { startTime: '06:00', endTime: '08:00' },
    { startTime: '06:30', endTime: '07:30' },
    { startTime: '07:00', endTime: '09:00' },
    { startTime: '07:30', endTime: '08:30' },
    { startTime: '08:00', endTime: '10:00' },
    { startTime: '08:30', endTime: '09:30' },
    { startTime: '09:00', endTime: '10:00' },
    { startTime: '10:30', endTime: '12:30' },
    { startTime: '11:00', endTime: '12:00' },
    { startTime: '11:30', endTime: '13:30' },
    { startTime: '12:00', endTime: '13:00' },
    { startTime: '12:30', endTime: '14:30' },
    { startTime: '13:00', endTime: '14:00' },
    { startTime: '13:30', endTime: '15:30' },
    { startTime: '14:00', endTime: '15:00' },
    { startTime: '14:30', endTime: '15:30' },
    { startTime: '15:30', endTime: '16:30' },
    { startTime: '16:00', endTime: '17:00' },
    { startTime: '16:30', endTime: '17:30' },
    { startTime: '17:00', endTime: '18:00' },
    // 周四额外增加的时间段 - 增加同时段冲突
    { startTime: '06:30', endTime: '08:30' }, // 与前面重叠
    { startTime: '07:00', endTime: '08:00' }, // 与前面重叠
    { startTime: '07:30', endTime: '09:30' }, // 与前面重叠
    { startTime: '08:00', endTime: '09:00' }, // 与前面重叠
    { startTime: '11:00', endTime: '13:00' }, // 与前面重叠
    { startTime: '11:30', endTime: '12:30' }, // 与前面重叠
    { startTime: '12:00', endTime: '14:00' }, // 与前面重叠
    { startTime: '12:30', endTime: '13:30' }, // 与前面重叠
    { startTime: '13:30', endTime: '14:30' }, // 与前面重叠
    { startTime: '14:00', endTime: '16:00' }  // 与前面重叠
  ],
  // 周五时间段
  4: [
    { startTime: '08:30', endTime: '10:30' },
    { startTime: '09:00', endTime: '10:00' },
    { startTime: '09:30', endTime: '11:30' },
    { startTime: '10:00', endTime: '11:00' },
    { startTime: '10:30', endTime: '12:30' },
    { startTime: '11:00', endTime: '12:00' },
    { startTime: '11:30', endTime: '12:30' },
    { startTime: '13:30', endTime: '15:30' },
    { startTime: '14:00', endTime: '15:00' },
    { startTime: '14:30', endTime: '16:30' },
    { startTime: '15:00', endTime: '16:00' },
    { startTime: '15:30', endTime: '17:30' },
    { startTime: '16:00', endTime: '17:00' },
    { startTime: '16:30', endTime: '18:30' },
    { startTime: '17:00', endTime: '18:00' },
    { startTime: '17:30', endTime: '18:30' },
    { startTime: '18:30', endTime: '19:30' },
    { startTime: '19:00', endTime: '20:00' },
    { startTime: '19:30', endTime: '20:30' },
    { startTime: '20:00', endTime: '21:00' }
  ],
  // 周六时间段 - 周末实践课程
  5: [
    { startTime: '09:00', endTime: '11:00' },
    { startTime: '09:30', endTime: '10:30' },
    { startTime: '10:00', endTime: '12:00' },
    { startTime: '10:30', endTime: '11:30' },
    { startTime: '11:00', endTime: '13:00' },
    { startTime: '14:00', endTime: '16:00' },
    { startTime: '14:30', endTime: '15:30' },
    { startTime: '15:00', endTime: '17:00' },
    { startTime: '15:30', endTime: '16:30' },
    { startTime: '16:00', endTime: '18:00' }
  ],
  // 周日时间段 - 复习和总结
  6: [
    { startTime: '10:00', endTime: '12:00' },
    { startTime: '10:30', endTime: '11:30' },
    { startTime: '11:00', endTime: '13:00' },
    { startTime: '14:00', endTime: '16:00' },
    { startTime: '14:30', endTime: '15:30' },
    { startTime: '15:00', endTime: '17:00' },
    { startTime: '15:30', endTime: '16:30' },
    { startTime: '16:00', endTime: '17:00' }
  ]
};

// 生成完整的课程数据
const courses = ref([]);

// 初始化课程数据
const initializeCourses = () => {
  const allCourses = [];
  let courseId = 1;

  // 遍历周一到周五
  for (let dayIndex = 0; dayIndex < 5; dayIndex++) {
    const daySchedule = timeSchedules[dayIndex];
    
    // 周三和周四安排更多课程（30门），其他天安排20门
    const courseCount = (dayIndex === 2 || dayIndex === 3) ? 30 : 20;
    
    for (let i = 0; i < courseCount; i++) {
      const baseCourse = baseCourses[i % 20]; // 当超过20门时，循环使用基础课程
      const timeSlot = daySchedule[i];
      
      // 如果是重复的课程，在名称后加上序号
      const courseName = i >= 20 ? `${baseCourse.name}(${Math.floor(i/20) + 1})` : baseCourse.name;
      const className = i >= 20 ? `${baseCourse.className}(${Math.floor(i/20) + 1})` : baseCourse.className;
      
      allCourses.push({
        id: courseId++,
        name: courseName,
        className: className,
        studentName: baseCourse.studentName,
        day: dayIndex,
        startTime: timeSlot.startTime,
        endTime: timeSlot.endTime,
        color: baseCourse.color
      });
    }
  }
  
  // 处理周六 - 安排10门课程（实践和项目课程）
  const saturdaySchedule = timeSchedules[5];
  for (let i = 0; i < 10; i++) {
    const baseCourse = baseCourses[i];
    const timeSlot = saturdaySchedule[i];
    
    allCourses.push({
      id: courseId++,
      name: `${baseCourse.name}实践`,
      className: `${baseCourse.className}(实践)`,
      studentName: baseCourse.studentName,
      day: 5, // 周六
      startTime: timeSlot.startTime,
      endTime: timeSlot.endTime,
      color: baseCourse.color
    });
  }
  
  // 处理周日 - 安排8门课程（复习和总结）
  const sundaySchedule = timeSchedules[6];
  for (let i = 0; i < 8; i++) {
    const baseCourse = baseCourses[i];
    const timeSlot = sundaySchedule[i];
    
    allCourses.push({
      id: courseId++,
      name: `${baseCourse.name}复习`,
      className: `${baseCourse.className}(复习)`,
      studentName: baseCourse.studentName,
      day: 6, // 周日
      startTime: timeSlot.startTime,
      endTime: timeSlot.endTime,
      color: baseCourse.color
    });
  }
  
  courses.value = allCourses;
};

// 初始化课程数据
initializeCourses();

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
    borderRadius: '4px',
    boxSizing: 'border-box',
    zIndex: 2,
    cursor: 'pointer',
    transition: 'all 0.2s ease'
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
                :style="{ height: '59px' }"
              ></div>
            </div>
            
            <!-- 课程容器 - 绝对定位 -->
            <div class="courses-container">
              <div
                v-for="course in courses.filter(c => c.day === dayIndex)"
                :key="course.id"
                class="course-item"
                :class="{ 'blur-effect': shouldShowBlurEffect(course) }"
                :style="getCourseStyle(course, dayIndex)"
                :title="`${course.name}\n${course.startTime} - ${course.endTime}\n班级: ${course.className || '未分配'}\n学员: ${course.studentName || '未指定'}`"
                @mouseenter="handleCourseMouseEnter(course)"
                @mouseleave="handleCourseMouseLeave"
              >
                <!-- 头部时间区域 -->
                <div class="course-header">
                  {{ course.startTime }} - {{ course.endTime }}
                </div>
                <!-- 课程信息区域 -->
                <div class="course-content">
                  <div class="course-class" v-if="course.className" :style="{ color: course.color }">{{ course.className }}</div>
                  <div class="course-name" :style="{ color: course.color }">{{ course.name }}</div>
                  <div class="course-student" v-if="course.studentName" :style="{ color: course.color, opacity: 0.7 }">{{ course.studentName }}</div>
                </div>
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
          <div class="current-time-label">{{ formatTime(currentTimePosition / 120) }}</div>
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
  height: 59px;
  display: flex;
  align-items: flex-start;
  position: relative;
  border-top: 1px solid #f0f0f0;
}

.time-label {
  width: 100%;
  height: 59px;
  box-sizing: border-box;
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
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.2;
  margin: 1px;
  user-select: none;
  transition: all 0.2s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: none;
  background: rgba(200, 230, 200, 0.6);
  box-shadow: none;
}

.course-header {
  background-color: rgba(0, 0, 0, 0.15);
  color: #ffffff;
  padding: 3px 6px;
  font-size: 10px;
  font-weight: 600;
  text-align: center;
  border-radius: 3px 3px 0 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
  height: 20px;
  line-height: 14px;
}

.course-content {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 4px 6px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  border-radius: 0 0 3px 3px;
  min-height: 0;
  overflow: hidden;
}

.course-class {
  font-size: 11px;
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-name {
  font-size: 10px;
  font-weight: 600;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}

.course-student {
  font-size: 9px;
  font-weight: 400;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 小课程块的特殊处理 */
.course-item[style*="height: 60px"], 
.course-item[style*="height: 30px"] {
  .course-content {
    padding: 2px 4px;
  }
  
  .course-class, .course-student {
    display: none;
  }
  
  .course-name {
    font-size: 10px;
    margin-bottom: 0;
  }
}

.course-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
  z-index: 100 !important;
}

.course-item:hover .course-header {
  background-color: rgba(0, 0, 0, 0.4);
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

/* 毛玻璃效果样式 */
.course-item.blur-effect {
  filter: blur(2px);
  opacity: 0.3;
  transform: scale(0.98);
  transition: all 0.3s ease;
}

.course-item.blur-effect:hover {
  filter: blur(1px);
  opacity: 0.5;
  transform: scale(0.99);
}
</style>