<script setup>
import { ref, onMounted, computed, onUnmounted, nextTick } from 'vue'
import helperDrawer from './helpDrawer.vue';

// 拖拽功能状态管理
const dragState = ref({
  isDragging: false,        // 是否正在拖拽
  draggedCourse: null,      // 被拖拽的课程对象
  originalCourse: null,     // 原始课程数据备份
  startPosition: {          // 拖拽开始位置
    mouseX: 0,
    mouseY: 0,
    courseX: 0,
    courseY: 0
  },
  currentPosition: {        // 当前拖拽位置
    x: 0,
    y: 0
  },
  previewPosition: {        // 预览位置（对齐后）
    day: 0,
    hour: 0
  },
  timeSnap: 5,             // 时间对齐精度（分钟）
  animationFrameId: null,  // 动画帧ID，用于性能优化
  currentTime: '',         // 当前拖拽对应的时间字符串
  targetDay: -1            // 当前拖拽目标的天（0-6，-1表示无效）
})
const defaultHeight=80;
const scheduleContainerRef = ref(null)
const cardContainerRef = ref(null)
const isFullscreen = ref(false)

// 添加悬浮状态管理
const hoveredCourse = ref(null)
const hoverTimer = ref(null)

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

// 鼠标悬浮处理函数
const handleCourseMouseEnter = (course) => {
  // 拖拽时不处理悬浮效果
  if (dragState.value.isDragging) return
  
  // 清除之前的定时器
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
  }
  
  // 延迟0.5秒后显示毛玻璃效果
  hoverTimer.value = setTimeout(() => {
    // 再次检查是否在拖拽状态（防止定时器执行时状态改变）
    if (!dragState.value.isDragging) {
      hoveredCourse.value = course
    }
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
  if (!hoveredCourse.value) {
    return false
  }
  
  // 拖拽时不显示毛玻璃效果，避免视觉干扰
  if (dragState.value.isDragging) {
    return false
  }
  
  // 如果是同名课程（包括带序号的），不显示毛玻璃效果
  const hoveredCourseName = hoveredCourse.value.name.replace(/\(\d+\)$/, '') // 去掉序号
  const currentCourseName = course.name.replace(/\(\d+\)$/, '') // 去掉序号
  
  return hoveredCourseName !== currentCourseName
}

// === 拖拽功能核心实现 ===

// 获取动态列宽度 - 修正版本：获取整个天的宽度
const getDayColumnWidth = () => {
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  if (!scheduleGrid) return 150; // 默认值
  
  const dayColumn = scheduleGrid.querySelector('.day-column-container');
  if (!dayColumn) return 150; // 默认值
  
  const rect = dayColumn.getBoundingClientRect();
  return rect.width;
};

// 新增：获取课程在天内的实际位置信息
const getCoursePositionInDay = (course, courseElement) => {
  if (!courseElement) return null;
  
  // 获取课程元素的位置
  const courseRect = courseElement.getBoundingClientRect();
  
  // 获取该天容器的位置
  const dayContainer = courseElement.closest('.day-column-container');
  if (!dayContainer) return null;
  
  const dayRect = dayContainer.getBoundingClientRect();
  
  // 计算课程相对于天容器的位置
  const relativeX = courseRect.left - dayRect.left;
  const relativeY = courseRect.top - dayRect.top;
  
  // 获取schedule-grid的位置，计算课程相对于整个网格的位置
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  if (!scheduleGrid) return null;
  
  const gridRect = scheduleGrid.getBoundingClientRect();
  const absoluteX = courseRect.left - gridRect.left;
  const absoluteY = courseRect.top - gridRect.top;
  
  return {
    relativeX,      // 相对于天容器的X位置
    relativeY,      // 相对于天容器的Y位置
    absoluteX,      // 相对于整个网格的X位置
    absoluteY,      // 相对于整个网格的Y位置
    dayWidth: dayRect.width,  // 该天的总宽度
    courseWidth: courseRect.width,  // 课程的实际宽度
    courseHeight: courseRect.height // 课程的实际高度
  };
};

// 像素坐标转换为时间和日期 (5分钟精度)
const pixelToTime = (x, y) => {
  const dayWidth = getDayColumnWidth(); // 动态获取列宽
  
  // 基础的天数计算 - 使用动态列宽
  let dayIndex = Math.floor(x / dayWidth)
  
  // 简化的边界处理：只在微小移动时保持原天
  if (dragState.value.isDragging && dragState.value.draggedCourse) {
    const originalDay = dragState.value.draggedCourse.day
    const originalDayStart = originalDay * dayWidth
    
    // 只有在同一天内的小范围移动时才保持原天（范围缩小到30px）
    if (Math.abs(x - originalDayStart) < 30 && Math.floor(x / dayWidth) === originalDay) {
      dayIndex = originalDay
      
      if (Math.random() < 0.1) { // 降低调试输出频率
        console.log('保持原天:', {
          原始X: Math.round(x),
          原始天: originalDay,
          距离天起始点: Math.round(x - originalDayStart),
          保持天: dayIndex,
          动态列宽: dayWidth
        })
      }
    }
    
    // 添加详细的天数计算调试信息
    if (Math.random() < 0.05) { // 偶尔输出调试信息
      console.log('天数计算详情:', {
        输入X: Math.round(x),
        动态列宽: dayWidth,
        除法结果: x / dayWidth,
        向下取整: Math.floor(x / dayWidth),
        最终dayIndex: dayIndex,
        范围限制前: dayIndex,
        范围限制后: Math.max(0, Math.min(6, dayIndex)),
        对应天名: weekDays[Math.max(0, Math.min(6, dayIndex))] || '超出范围'
      })
    }
  }
  
  const hourDecimal = y / defaultHeight          // 每小时80px，从0:00开始
  const actualHour = hourDecimal       // 直接使用hourDecimal，不需要加6
  
  // 时间对齐到5分钟网格 (每小时12个5分钟间隔)
  const alignedHour = Math.round(actualHour * 12) / 12
  
  return {
    day: Math.max(0, Math.min(6, dayIndex)),
    hour: Math.max(0, Math.min(24, alignedHour)) // 时间范围改为0-24
  }
}

// 小时数转换为时间字符串 (5分钟精度)
const hourToTimeString = (hour) => {
  const h = Math.floor(hour)
  const m = Math.round((hour % 1) * 60 / 5) * 5  // 确保分钟是5的倍数
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`
}

// 获取容器的边界信息
const getContainerBounds = () => {
  const container = scheduleContainerRef.value
  if (!container) return null
  
  const scheduleGrid = container.querySelector('.schedule-grid')
  if (!scheduleGrid) return null
  
  const rect = scheduleGrid.getBoundingClientRect()
  return {
    left: rect.left,
    top: rect.top,
    width: rect.width,
    height: rect.height
  }
}

// 开始拖拽
const handleMouseDown = (event, course) => {
  // 只有按下鼠标左键才开始拖拽
  if (event.button !== 0) return
  
  event.preventDefault()
  event.stopPropagation()
  
  dragState.value.isDragging = true
  dragState.value.draggedCourse = course
  dragState.value.originalCourse = { ...course }
  
  // 记录鼠标的初始位置
  dragState.value.startPosition.mouseX = event.clientX
  dragState.value.startPosition.mouseY = event.clientY
  
  // 动态获取列宽度
  const dayWidth = getDayColumnWidth()
  
  // 获取课程元素
  const courseElement = event.target.closest('.course-item')
  
  // 优先使用精确的DOM位置信息
  const positionInfo = getCoursePositionInDay(course, courseElement)
  
  let finalStartX, finalStartY
  let useActualDOMPosition = false
  
  if (positionInfo) {
    // 使用DOM获取的精确位置
    finalStartX = positionInfo.absoluteX
    finalStartY = positionInfo.absoluteY
    useActualDOMPosition = true
    
    console.log('使用精确DOM位置作为起始位置:', {
      课程: course.name,
      相对天位置: { x: Math.round(positionInfo.relativeX), y: Math.round(positionInfo.relativeY) },
      相对网格位置: { x: Math.round(positionInfo.absoluteX), y: Math.round(positionInfo.absoluteY) },
      天宽度: positionInfo.dayWidth,
      课程尺寸: { width: positionInfo.courseWidth, height: positionInfo.courseHeight },
      动态列宽: dayWidth
    })
  } else {
    // 降级使用理论位置计算
    const courseStart = parseTime(course.startTime)
    finalStartX = course.day * dayWidth  // 使用动态列宽
    finalStartY = courseStart * defaultHeight  // 每小时80px
    
    console.warn('DOM位置获取失败，使用理论位置:', {
      课程: course.name,
      理论位置: { x: finalStartX, y: finalStartY },
      原因: '无法获取精确位置信息'
    })
  }
  
  // 设置起始位置（用于鼠标移动计算）
  dragState.value.startPosition.courseX = finalStartX
  dragState.value.startPosition.courseY = finalStartY
  
  // 初始化当前位置，让预览块从正确位置开始
  dragState.value.currentPosition.x = finalStartX
  dragState.value.currentPosition.y = finalStartY
  
  // 初始化拖拽指示器信息
  const initialTime = pixelToTime(finalStartX, finalStartY)
  dragState.value.currentTime = hourToTimeString(initialTime.hour)
  dragState.value.targetDay = initialTime.day
  
  // 添加全局事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
  
  console.log('开始拖拽课程:', course.name, {
    原始天: weekDays[course.day],
    起始位置: { x: Math.round(finalStartX), y: Math.round(finalStartY) },
    使用DOM位置: useActualDOMPosition,
    动态列宽: dayWidth,
    拖拽基准: `鼠标从(${event.clientX}, ${event.clientY})开始，课程从(${Math.round(finalStartX)}, ${Math.round(finalStartY)})开始`
  })
}

// 拖拽中 - 优化性能版本
const handleMouseMove = (event) => {
  if (!dragState.value.isDragging) return
  
  event.preventDefault()
  
  // 使用requestAnimationFrame节流，避免过度更新
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
  }
  
  dragState.value.animationFrameId = requestAnimationFrame(() => {
    // 获取容器边界信息
    const bounds = getContainerBounds()
    if (!bounds) return
    
    // 动态获取列宽度
    const dayWidth = getDayColumnWidth()
    
    // 计算鼠标移动的距离
    const deltaX = event.clientX - dragState.value.startPosition.mouseX
    const deltaY = event.clientY - dragState.value.startPosition.mouseY
    
    // 计算新位置，基于原始位置加上鼠标移动的距离
    let newX = dragState.value.startPosition.courseX + deltaX
    let newY = dragState.value.startPosition.courseY + deltaY
    
    // 每10次输出一次调试信息，避免过多输出
    if (Math.random() < 0.1) {
      console.log('拖拽移动调试:', {
        原始课程X: dragState.value.startPosition.courseX,
        鼠标移动X: deltaX,
        计算newX: newX,
        课程: dragState.value.draggedCourse.name,
        原始天: weekDays[dragState.value.draggedCourse.day],
        动态列宽: dayWidth,
        计算目标天: Math.floor(newX / dayWidth),
        目标天名称: weekDays[Math.floor(newX / dayWidth)] || '超出范围'
      })
    }
    
    // 移除X轴边界限制，允许自由拖拽到所有7天
    // 只保留基本的负数边界保护
    newX = Math.max(0, newX) // 只限制不能拖拽到负数位置
    
    // Y轴边界：限制在时间范围内（0:00-24:00，共24小时，每小时80px）
    const totalGridHeight = 24 * defaultHeight  // 24小时 * 80px/小时
    const courseHeight = parseFloat(getCourseStyle(dragState.value.draggedCourse, dragState.value.draggedCourse.day).height) || 60
    
    newY = Math.max(0, Math.min(totalGridHeight - courseHeight, newY))
    
    // 更新当前位置
    dragState.value.currentPosition.x = newX
    dragState.value.currentPosition.y = newY
    
    // 计算预览位置（对齐后的位置）
    const { day, hour } = pixelToTime(newX, newY)
    dragState.value.previewPosition.day = day
    dragState.value.previewPosition.hour = hour
    
    // 更新拖拽指示器信息
    dragState.value.currentTime = hourToTimeString(hour)
    dragState.value.targetDay = day
    
    // 减少控制台输出频率，提高性能
    if (import.meta.env.DEV && Math.random() < 0.05) {
      console.log('拖拽实时位置:', { 
        当前像素: { x: Math.round(newX), y: Math.round(newY) },
        预览位置: { day: weekDays[day], time: hourToTimeString(hour) },
        动态列宽: dayWidth
      })
    }
  })
}

// 结束拖拽
const handleMouseUp = (event) => {
  if (!dragState.value.isDragging) return
  
  event.preventDefault()
  
  // 清理动画帧
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
    dragState.value.animationFrameId = null
  }
  
  // 移除全局事件监听
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  // 计算最终位置（对齐到5分钟网格）
  const { day, hour } = pixelToTime(
    dragState.value.currentPosition.x, 
    dragState.value.currentPosition.y
  )
  
  // 动态获取列宽度用于最终对齐
  const dayWidth = getDayColumnWidth()
  
  // 计算对齐后的最终位置（但不更新预览块位置，避免闪烁）
  const alignedY = hour * defaultHeight  // 修改：直接使用hour，不减去6
  const alignedX = day * dayWidth  // 使用动态列宽
  
  // 注意：这里不再更新currentPosition，避免预览块闪到最左边
  // dragState.value.currentPosition.x = alignedX
  // dragState.value.currentPosition.y = alignedY
  
  // 计算新的开始和结束时间
  const course = dragState.value.draggedCourse
  const originalStart = parseTime(course.startTime)
  const originalEnd = parseTime(course.endTime)
  const courseDuration = originalEnd - originalStart
  
  const newStartTime = hourToTimeString(hour)
  const newEndTime = hourToTimeString(hour + courseDuration)
  
  console.log('拖拽结束:', {
    原始: { day: weekDays[course.day], start: course.startTime, end: course.endTime },
    新位置: { day: weekDays[day], start: newStartTime, end: newEndTime },
    动态列宽: dayWidth,
    理论对齐位置: { x: alignedX, y: alignedY },
    实际停留位置: { x: dragState.value.currentPosition.x, y: dragState.value.currentPosition.y }
  })
  
  // 检查是否发生了实际移动
  if (course.day !== day || course.startTime !== newStartTime) {
    // 更新课程数据
    updateCoursePosition(course, day, newStartTime, newEndTime)
  }
  
  // 立即重置拖拽状态，让预览块直接消失，避免闪烁
  dragState.value.isDragging = false
  dragState.value.draggedCourse = null
  dragState.value.originalCourse = null
  
  // 清除拖拽指示器信息
  dragState.value.currentTime = ''
  dragState.value.targetDay = -1
  
  // 清除悬浮状态，确保拖拽结束后悬浮效果能正常工作
  hoveredCourse.value = null
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }
  
  // 触发布局重新计算
  nextTick(() => {
    // 重新计算所有布局，确保没有冲突
    recalculateAllLayouts()
  })
}

// 更新课程位置
const updateCoursePosition = (course, newDay, newStartTime, newEndTime) => {
  // 找到课程在数组中的索引
  const courseIndex = courses.value.findIndex(c => c.id === course.id)
  if (courseIndex === -1) return
  
  // 更新课程数据
  courses.value[courseIndex] = {
    ...course,
    day: newDay,
    startTime: newStartTime,
    endTime: newEndTime
  }
  
  console.log('课程位置已更新:', {
    课程: course.name,
    新天: weekDays[newDay],
    新时间: `${newStartTime} - ${newEndTime}`
  })
}

// 重新计算所有布局
const recalculateAllLayouts = () => {
  // 由于使用了computed，布局会自动重新计算
  // 这里只需要确保UI更新
  console.log('重新计算所有布局...')
}

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

  // 如果这个课程正在被拖拽，原位置显示毛玻璃效果
  const isDraggedCourse = dragState.value.isDragging && dragState.value.draggedCourse?.id === course.id;
  
  // 检查是否应该显示悬浮毛玻璃效果（但不与拖拽效果冲突）
  const shouldShowHoverBlur = shouldShowBlurEffect(course);
  
  // 优先级：拖拽毛玻璃 > 悬浮毛玻璃 > 无效果
  let filterValue = 'none';
  let opacityValue = 1;
  let transformValue = 'scale(1)';
  
  if (isDraggedCourse) {
    // 拖拽时的毛玻璃效果（优先级最高）
    filterValue = 'blur(2px)';
    opacityValue = 0.3;
    transformValue = 'scale(0.98)';
  } else if (shouldShowHoverBlur) {
    // 悬浮毛玻璃效果（次优先级）
    filterValue = 'blur(2px)';
    opacityValue = 0.3;
    transformValue = 'scale(0.98)';
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
    zIndex: isDraggedCourse ? 1 : 2, // 被拖拽的课程降低层级
    cursor: 'pointer',
    transition: 'all 0.2s ease',
    // 统一的毛玻璃效果处理
    filter: filterValue,
    opacity: opacityValue,
    transform: transformValue
  };
};

const helperDrawerRef = ref();

function openHelp(){
    helperDrawerRef.value?.open({})
}
// 生成时间刻度数据 - 每半小时为一个单位，每小时80px
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
  currentTimePosition.value = (hours + minutes / 60) * defaultHeight // 每小时80px
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
      top: `${courseStart * defaultHeight}px`, // 修改：直接使用courseStart，不减去6
      height: `${courseDuration * defaultHeight}px`
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

// 获取拖拽预览块的样式
const getDragPreviewStyle = () => {
  if (!dragState.value.isDragging || !dragState.value.draggedCourse) {
    return { display: 'none' };
  }

  const course = dragState.value.draggedCourse;
  
  // 通过DOM查询获取被拖拽课程元素的实际尺寸
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  if (!scheduleGrid) {
    return { display: 'none' };
  }
  
  // 查找当前被拖拽的课程元素
  const courseElements = scheduleGrid.querySelectorAll('.course-item');
  let targetCourseElement = null;
  
  // 通过比较课程信息找到对应的DOM元素
  for (const element of courseElements) {
    const courseNameElement = element.querySelector('.course-name');
    const courseTimeElement = element.querySelector('.course-header');
    
    if (courseNameElement && courseTimeElement) {
      const nameMatch = courseNameElement.textContent?.trim() === course.name;
      const timeMatch = courseTimeElement.textContent?.includes(course.startTime) && 
                       courseTimeElement.textContent?.includes(course.endTime);
      
      if (nameMatch && timeMatch) {
        targetCourseElement = element;
        break;
      }
    }
  }
  let actualWidth = 150; // 默认宽度
  let actualHeight = '40px'; // 默认高度
  
  if (targetCourseElement) {
    // 获取元素的实际渲染尺寸
    const rect = targetCourseElement.getBoundingClientRect();
    
    actualWidth = Math.round(rect.width); // 使用实际渲染宽度
    actualHeight = `${Math.round(rect.height)}px`; // 使用实际渲染高度
    
    console.log('预览块尺寸 - 从DOM获取:', {
      课程: course.name,
      元素宽度: rect.width,
      元素高度: rect.height,
      计算后宽度: actualWidth,
      计算后高度: actualHeight
    });
  } else {
    console.warn('预览块尺寸 - 未找到对应DOM元素，使用默认尺寸:', {
      课程: course.name,
      默认宽度: actualWidth,
      默认高度: actualHeight
    });
  }
  
  // 获取容器边界信息用于右边界保护
  const gridRect = scheduleGrid.getBoundingClientRect();
  const containerWidth = gridRect.width;
  
  // 拖拽预览块使用当前拖拽位置
  let currentLeft = dragState.value.currentPosition.x;
  const currentTop = dragState.value.currentPosition.y;
  
  // 添加右边界保护：确保预览块不会超出容器右边界
  const maxLeft = containerWidth - actualWidth;
  currentLeft = Math.min(currentLeft, maxLeft);
  
  // 同时确保不会超出左边界
  currentLeft = Math.max(0, currentLeft);
  
  console.log('预览块位置（边界保护后）:', {
    课程: course.name,
    原始位置: { left: Math.round(dragState.value.currentPosition.x), top: Math.round(currentTop) },
    保护后位置: { left: Math.round(currentLeft), top: Math.round(currentTop) },
    容器宽度: containerWidth,
    课程宽度: actualWidth,
    最大左边距: maxLeft
  });
  
  // 拖拽预览块完全模拟原课程块的样式
  return {
    position: 'absolute',
    top: `${currentTop}px`,
    left: `${currentLeft}px`,
    width: `${actualWidth}px`, // 使用从DOM获取的实际宽度
    height: actualHeight, // 使用从DOM获取的实际高度
    backgroundColor: course.color || '#3498db', // 保持原始背景色
    borderRadius: '4px',
    boxSizing: 'border-box',
    zIndex: 1000,
    cursor: 'grabbing',
    transition: 'none', // 拖拽时不要过渡动画
    pointerEvents: 'none', // 拖拽时不响应鼠标事件
    // 移除特殊的视觉效果，完全模拟原课程块
    border: 'none', // 移除边框
    boxShadow: 'none', // 移除阴影
    opacity: 1, // 保持完全不透明
    transform: 'none' // 移除缩放
  };
};

// 新增：获取拖拽预览块时间指示器的样式
const getDragPreviewTimeStyle = () => {
  if (!dragState.value.isDragging || !dragState.value.draggedCourse || !dragState.value.currentTime) {
    return { display: 'none' };
  }

  // 计算时间指示器位置：预览块的上方，居中对齐
  const currentLeft = dragState.value.currentPosition.x;
  const currentTop = dragState.value.currentPosition.y;
  
  // 获取容器边界信息用于右边界保护
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  let containerWidth = 1050; // 默认宽度（7天 * 150px）
  
  if (scheduleGrid) {
    const rect = scheduleGrid.getBoundingClientRect();
    containerWidth = rect.width;
  }
  
  // 时间指示器的宽度
  const indicatorWidth = 80;
  
  // 时间指示器放在预览块的上方，向上偏移30px
  let timeIndicatorLeft = currentLeft;
  const timeIndicatorTop = Math.max(0, currentTop - 30); // 确保不会超出顶部边界
  
  // 添加右边界保护：确保时间指示器不会超出容器右边界
  const maxLeft = containerWidth - indicatorWidth;
  timeIndicatorLeft = Math.min(timeIndicatorLeft, maxLeft);
  
  // 同时确保不会超出左边界
  timeIndicatorLeft = Math.max(0, timeIndicatorLeft);

  return {
    position: 'absolute',
    top: `${timeIndicatorTop}px`,
    left: `${timeIndicatorLeft}px`,
    width: '120px',
    height: '24px',
    zIndex: 1001, // 比预览块层级更高
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    pointerEvents: 'none'
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
        :class="{ 'day-column-highlight': dragState.isDragging && dragState.targetDay === index }"
      >
        {{ day }}
        <!-- 拖拽时显示目标指示器 -->
        <!-- <div 
          v-if="dragState.isDragging && dragState.targetDay === index" 
          class="drag-target-indicator"
        >
          拖拽目标
        </div> -->
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
          
          <!-- 拖拽时显示当前时间指示器 -->
          <div 
            v-if="dragState.isDragging && dragState.currentTime" 
            class="drag-time-indicator"
            :style="{ top: `${(parseTime(dragState.currentTime)) * defaultHeight}px` }"
          >
            <div class="drag-time-label">{{ dragState.currentTime }}</div>
            <div class="drag-time-arrow"></div>
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
                :style="{ height: '40px' }"
              ></div>
            </div>
            
            <!-- 课程容器 - 绝对定位 -->
            <div class="courses-container">
              <div
                v-for="course in courses.filter(c => c.day === dayIndex)"
                :key="course.id"
                class="course-item"
                :style="getCourseStyle(course, dayIndex)"
                :title="`${course.name}\n${course.startTime} - ${course.endTime}\n班级: ${course.className || '未分配'}\n学员: ${course.studentName || '未指定'}`"
                @mouseenter="handleCourseMouseEnter(course)"
                @mouseleave="handleCourseMouseLeave"
                @mousedown="handleMouseDown($event, course)"
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
          
          <!-- 拖拽预览块 - 独立的拖拽预览层 -->
          <div 
            v-if="dragState.isDragging && dragState.draggedCourse"
            class="course-item dragging-course-preview"
            :style="getDragPreviewStyle()"
          >
            <!-- 头部时间区域 -->
            <div class="course-header">
              {{ dragState.draggedCourse.startTime }} - {{ dragState.draggedCourse.endTime }}
            </div>
            <!-- 课程信息区域 -->
            <div class="course-content">
              <div class="course-class" v-if="dragState.draggedCourse.className" :style="{ color: dragState.draggedCourse.color }">{{ dragState.draggedCourse.className }}</div>
              <div class="course-name" :style="{ color: dragState.draggedCourse.color }">{{ dragState.draggedCourse.name }}</div>
              <div class="course-student" v-if="dragState.draggedCourse.studentName" :style="{ color: dragState.draggedCourse.color, opacity: 0.7 }">{{ dragState.draggedCourse.studentName }}</div>
            </div>
          </div>
          
          <!-- 拖拽预览块的时间指示器 -->
          <div 
            v-if="dragState.isDragging && dragState.currentTime"
            class="drag-preview-time-indicator"
            :style="getDragPreviewTimeStyle()"
          >
            <div class="drag-preview-time-label">{{ dragState.currentTime }}</div>
          </div>
        </div>
        
        <!-- 当前时间线 -->
        <div 
          v-if="showCurrentTimeLine" 
          class="current-time-line"
          :style="{ top: `${currentTimePosition}px` }"
        >
          <div class="current-time-dot"></div>
          <div class="current-time-label">{{ formatTime(currentTimePosition / defaultHeight) }}</div>
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
  position: relative; /* 为拖拽指示器提供定位基础 */
  transition: all 0.3s ease; /* 添加过渡动画 */
}

.day-column:last-child {
  border-right: none;
}

.day-column-highlight {
  background-color: #e6f7ff; /* 高亮背景色 */
  border: 1px solid #91d5ff; /* 高亮边框 */
  box-shadow: 0 0 8px rgba(0, 136, 255, 0.3); /* 高亮阴影 */
  transform: scale(1.02); /* 轻微放大效果 */
  color: #1890ff; /* 高亮文字颜色 */
  font-weight: 700; /* 加粗字体 */
}

.drag-target-indicator {
  position: absolute;
  bottom: 2px; /* 调整位置到底部 */
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(45deg, #1890ff, #40a9ff); /* 渐变背景 */
  color: white;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 9px;
  font-weight: 600;
  z-index: 10;
  opacity: 0.9;
  pointer-events: none; /* 确保指示器不会影响拖拽 */
  animation: targetIndicatorPulse 1.5s ease-in-out infinite; /* 添加脉冲动画 */
}

.time-labels {
  width: 80px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background-color: #fafafa;
  position: relative;
}

.time-row {
  height:39px;
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
  height: 40px;
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
  height: 40px;
  border-bottom: 1px solid #e1e1e1; /* 加深整点网格线颜色 */
  position: relative;
  background-color: #fcfcfc;
  box-sizing: border-box; /* 确保边框不影响总高度 */
}

.grid-cell.half-hour {
  border-bottom: 1px dashed #d1d1d1; /* 加深半点虚线颜色，提高对比度 */
  background-color: #f8f8f8; /* 略微加深半点背景色 */
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

/* 拖拽预览块专用样式 */
.dragging-course-preview {
  pointer-events: none !important;
  z-index: 1000 !important;
  transition: none !important;
  cursor: grabbing !important;
  /* 移除特殊的视觉效果，保持与原课程块一致 */
}

.dragging-course-preview .course-header {
  background-color: rgba(0, 0, 0, 0.15) !important; /* 与原课程块保持一致 */
  color: #ffffff !important;
}

.dragging-course-preview .course-content {
  background-color: rgba(255, 255, 255, 0.8) !important; /* 与原课程块保持一致 */
}

.drag-time-indicator {
  position: absolute;
  left: 0;
  width: 80px; /* 与时间列宽度一致 */
  height: 2px;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
}

.drag-time-label {
  background-color: #ff6b35;
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
  animation: dragTimeFlash 1s ease-in-out infinite alternate;
}

.drag-time-arrow {
  width: 0;
  height: 0;
  border-left: 6px solid #ff6b35;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  margin-left: 2px;
}

/* 闪烁动画效果 */
@keyframes dragTimeFlash {
  0% { opacity: 0.7; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.05); }
}

/* 脉冲动画效果 */
@keyframes targetIndicatorPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 拖拽预览块时间指示器样式 */
.drag-preview-time-indicator {
  pointer-events: none !important;
  z-index: 1001 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.drag-preview-time-label {
  background-color: #52c41a; /* 使用绿色，与左侧时间指示器区分 */
  color: white;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: 0 3px 12px rgba(82, 196, 26, 0.4);
  animation: previewTimeFlash 1.2s ease-in-out infinite alternate;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 预览时间指示器闪烁动画 */
@keyframes previewTimeFlash {
  0% { 
    opacity: 0.8; 
    transform: scale(1);
    box-shadow: 0 3px 12px rgba(82, 196, 26, 0.4);
  }
  100% { 
    opacity: 1; 
    transform: scale(1.08);
    box-shadow: 0 4px 16px rgba(82, 196, 26, 0.6);
  }
}
</style>