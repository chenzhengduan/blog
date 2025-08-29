# Vue课表拖拽组件实现方案

## 项目概述

这是一个基于Vue 3 + Composition API实现的可视化课表组件，支持拖拽功能、冲突检测、时间对齐等高级特性。本文档详细介绍了组件的技术架构、核心算法和实现细节。

## 核心特性

### 1. 拖拽功能
- ✅ 支持鼠标拖拽课程块
- ✅ 拖拽时显示实时预览
- ✅ 自动时间对齐（5分钟精度）
- ✅ 边界保护和碰撞检测
- ✅ 拖拽指示器和时间提示

### 2. 布局算法
- ✅ 智能冲突检测
- ✅ 动态列宽计算
- ✅ 时间重叠处理
- ✅ 自适应列数分配

### 3. 视觉效果
- ✅ 悬浮毛玻璃效果
- ✅ 拖拽高亮指示
- ✅ 当前时间线显示
- ✅ 响应式设计

## 技术架构

### 技术栈
```json
{
  "框架": "Vue 3",
  "API风格": "Composition API",
  "UI库": "Element Plus",
  "语言": "JavaScript + CSS3",
  "构建工具": "Vite"
}
```

### 项目结构
```
src/components/
├── AppointCourse.vue     # 主课表组件
├── helpDrawer.vue        # 帮助抽屉组件
└── home.vue             # 首页组件
```

## 核心实现解析

### 1. 数据结构设计

#### 课程数据模型
```javascript
const course = {
  id: 1,                    // 唯一标识
  name: '高等数学',          // 课程名称
  className: '数学1班',      // 班级信息
  studentName: '张小明',     // 学员姓名
  day: 0,                   // 星期几（0-6，0表示周一）
  startTime: '08:00',       // 开始时间
  endTime: '10:00',         // 结束时间
  color: '#1890ff'          // 课程颜色
}
```

#### 拖拽状态管理
```javascript
const dragState = ref({
  isDragging: false,        // 是否正在拖拽
  draggedCourse: null,      // 被拖拽的课程对象
  originalCourse: null,     // 原始课程数据备份
  startPosition: {          // 拖拽开始位置
    mouseX: 0, mouseY: 0,
    courseX: 0, courseY: 0
  },
  currentPosition: {        // 当前拖拽位置
    x: 0, y: 0
  },
  previewPosition: {        // 预览位置（对齐后）
    day: 0, hour: 0
  },
  timeSnap: 5,             // 时间对齐精度（分钟）
  currentTime: '',         // 当前拖拽时间字符串
  targetDay: -1            // 目标天数
})
```

### 2. 时间系统设计

#### 坐标转换算法
```javascript
// 像素坐标转换为时间和日期（5分钟精度）
const pixelToTime = (x, y) => {
  const dayWidth = getDayColumnWidth()  // 动态获取列宽
  const dayIndex = Math.floor(x / dayWidth)
  const hourDecimal = y / defaultHeight  // 每小时80px
  const alignedHour = Math.round(hourDecimal * 12) / 12  // 5分钟对齐
  
  return {
    day: Math.max(0, Math.min(6, dayIndex)),
    hour: Math.max(0, Math.min(24, alignedHour))
  }
}
```

#### 时间格式处理
```javascript
// 时间字符串解析
const parseTime = (timeStr) => {
  const [hours, minutes] = timeStr.split(':').map(Number)
  return hours + minutes / 60
}

// 小时数转时间字符串
const hourToTimeString = (hour) => {
  const h = Math.floor(hour)
  const m = Math.round((hour % 1) * 60 / 5) * 5
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`
}
```

### 3. 冲突检测与布局算法

#### 冲突关系分析
```javascript
// 建立课程间的时间冲突关系
const conflicts = new Map()
sortedCourses.forEach(course => {
  const courseStart = parseTime(course.startTime)
  const courseEnd = parseTime(course.endTime)
  const conflictingCourses = new Set()
  
  sortedCourses.forEach(otherCourse => {
    if (course.id === otherCourse.id) return
    
    const otherStart = parseTime(otherCourse.startTime)
    const otherEnd = parseTime(otherCourse.endTime)
    
    // 检查时间重叠
    if (courseStart < otherEnd && courseEnd > otherStart) {
      conflictingCourses.add(otherCourse.id)
    }
  })
  
  conflicts.set(course.id, conflictingCourses)
})
```

#### 列数计算
```javascript
// 计算最大并发课程数，确定所需列数
let maxColumns = 1
const timePoints = new Set()

// 收集所有时间节点
sortedCourses.forEach(course => {
  const startMinutes = Math.round(parseTime(course.startTime) * 60)
  const endMinutes = Math.round(parseTime(course.endTime) * 60)
  timePoints.add(startMinutes)
  timePoints.add(endMinutes)
})

// 计算每个时间点的并发数
Array.from(timePoints).forEach(timePoint => {
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
```

#### 贪心算法分配列位置
```javascript
// 使用贪心算法为每门课程分配列位置
const coursePositions = new Map()
const columnOccupancy = Array.from({ length: maxColumns }, () => [])

sortedCourses.forEach(course => {
  const courseStart = parseTime(course.startTime)
  const courseEnd = parseTime(course.endTime)
  
  // 找到第一个可用的列
  let assignedColumn = -1
  
  for (let col = 0; col < maxColumns; col++) {
    let canUseColumn = true
    
    // 检查这一列是否有时间冲突
    for (const occupiedPeriod of columnOccupancy[col]) {
      if (courseStart < occupiedPeriod.end && courseEnd > occupiedPeriod.start) {
        canUseColumn = false
        break
      }
    }
    
    if (canUseColumn) {
      assignedColumn = col
      columnOccupancy[col].push({
        start: courseStart,
        end: courseEnd,
        courseId: course.id
      })
      break
    }
  }
  
  coursePositions.set(course.id, assignedColumn)
})
```

### 4. 拖拽事件处理

#### 开始拖拽
```javascript
const handleMouseDown = (event, course) => {
  if (event.button !== 0) return  // 只响应左键
  
  event.preventDefault()
  event.stopPropagation()
  
  // 初始化拖拽状态
  dragState.value.isDragging = true
  dragState.value.draggedCourse = course
  dragState.value.originalCourse = { ...course }
  
  // 记录初始位置
  dragState.value.startPosition.mouseX = event.clientX
  dragState.value.startPosition.mouseY = event.clientY
  
  // 获取课程在DOM中的精确位置
  const positionInfo = getCoursePositionInDay(course, event.target.closest('.course-item'))
  if (positionInfo) {
    dragState.value.startPosition.courseX = positionInfo.absoluteX
    dragState.value.startPosition.courseY = positionInfo.absoluteY
  }
  
  // 添加全局事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}
```

#### 拖拽移动
```javascript
const handleMouseMove = (event) => {
  if (!dragState.value.isDragging) return
  
  // 使用requestAnimationFrame优化性能
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
  }
  
  dragState.value.animationFrameId = requestAnimationFrame(() => {
    // 计算鼠标移动距离
    const deltaX = event.clientX - dragState.value.startPosition.mouseX
    const deltaY = event.clientY - dragState.value.startPosition.mouseY
    
    // 计算新位置
    let newX = dragState.value.startPosition.courseX + deltaX
    let newY = dragState.value.startPosition.courseY + deltaY
    
    // 边界限制
    newX = Math.max(0, newX)
    const totalGridHeight = 24 * defaultHeight
    const courseHeight = parseFloat(getCourseStyle(dragState.value.draggedCourse, dragState.value.draggedCourse.day).height) || 60
    newY = Math.max(0, Math.min(totalGridHeight - courseHeight, newY))
    
    // 更新位置
    dragState.value.currentPosition.x = newX
    dragState.value.currentPosition.y = newY
    
    // 计算预览位置
    const { day, hour } = pixelToTime(newX, newY)
    dragState.value.previewPosition.day = day
    dragState.value.previewPosition.hour = hour
    dragState.value.currentTime = hourToTimeString(hour)
    dragState.value.targetDay = day
  })
}
```

#### 结束拖拽
```javascript
const handleMouseUp = (event) => {
  if (!dragState.value.isDragging) return
  
  // 清理资源
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
  }
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  // 计算最终位置
  const { day, hour } = pixelToTime(
    dragState.value.currentPosition.x,
    dragState.value.currentPosition.y
  )
  
  // 计算新的时间
  const course = dragState.value.draggedCourse
  const originalStart = parseTime(course.startTime)
  const originalEnd = parseTime(course.endTime)
  const courseDuration = originalEnd - originalStart
  
  const newStartTime = hourToTimeString(hour)
  const newEndTime = hourToTimeString(hour + courseDuration)
  
  // 检查是否有实际移动
  if (course.day !== day || course.startTime !== newStartTime) {
    updateCoursePosition(course, day, newStartTime, newEndTime)
  }
  
  // 重置拖拽状态
  dragState.value.isDragging = false
  dragState.value.draggedCourse = null
  
  // 触发布局重计算
  nextTick(() => {
    recalculateAllLayouts()
  })
}
```

### 5. 样式计算

#### 课程块样式生成
```javascript
const getCourseStyle = (course, day) => {
  // 获取对应天的布局信息
  const dayLayouts = day === 0 ? mondayLayouts.value : calculateDayLayout(day)
  const layout = dayLayouts.get(course.id)
  
  if (!layout) {
    return { display: 'none' }
  }
  
  // 处理拖拽和悬浮效果
  const isDraggedCourse = dragState.value.isDragging && dragState.value.draggedCourse?.id === course.id
  const shouldShowHoverBlur = shouldShowBlurEffect(course)
  
  let filterValue = 'none'
  let opacityValue = 1
  let transformValue = 'scale(1)'
  
  if (isDraggedCourse) {
    filterValue = 'blur(2px)'
    opacityValue = 0.3
    transformValue = 'scale(0.98)'
  } else if (shouldShowHoverBlur) {
    filterValue = 'blur(2px)'
    opacityValue = 0.3
    transformValue = 'scale(0.98)'
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
    zIndex: isDraggedCourse ? 1 : 2,
    cursor: 'pointer',
    transition: 'all 0.2s ease',
    filter: filterValue,
    opacity: opacityValue,
    transform: transformValue
  }
}
```

### 6. 性能优化策略

#### 缓存机制
```javascript
// 使用computed缓存布局计算结果
const mondayLayouts = computed(() => calculateDayLayout(0))

// DOM查询结果缓存
const cachedElements = new Map()
const getDayColumnWidth = () => {
  if (cachedElements.has('dayWidth')) {
    return cachedElements.get('dayWidth')
  }
  
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid')
  if (!scheduleGrid) return 150
  
  const dayColumn = scheduleGrid.querySelector('.day-column-container')
  if (!dayColumn) return 150
  
  const width = dayColumn.getBoundingClientRect().width
  cachedElements.set('dayWidth', width)
  return width
}
```

#### 事件节流
```javascript
// 使用requestAnimationFrame节流拖拽事件
dragState.value.animationFrameId = requestAnimationFrame(() => {
  // 拖拽处理逻辑
})
```

#### 调试输出控制
```javascript
// 控制调试输出频率，避免性能问题
if (Math.random() < 0.1) {
  console.log('拖拽调试信息', debugData)
}
```

## 核心算法详解

### 1. 时间对齐算法

课程拖拽时需要自动对齐到5分钟网格：

```javascript
// 5分钟精度对齐
const alignedHour = Math.round(hourDecimal * 12) / 12
// 解释：
// hourDecimal * 12 -> 将小时转换为12个5分钟单位
// Math.round() -> 四舍五入到最近的5分钟
// / 12 -> 转换回小时数
```

### 2. 冲突检测算法

判断两个时间段是否重叠：

```javascript
// 时间重叠检测
if (courseStart < otherEnd && courseEnd > otherStart) {
  // 存在重叠
}
// 原理：
// A的开始时间 < B的结束时间 AND A的结束时间 > B的开始时间
// 即为两个时间段有重叠
```

### 3. 列分配算法

使用贪心算法为冲突课程分配列：

```javascript
// 贪心策略：
// 1. 按开始时间排序课程
// 2. 依次为每门课程寻找第一个可用列
// 3. 更新列的占用状态
// 4. 继续处理下一门课程
```

## 样式系统

### 1. 网格系统

```css
.grid-cell {
  height: 40px;  /* 半小时高度 */
  border-bottom: 1px solid #e1e1e1;  /* 整点线 */
  box-sizing: border-box;
}

.grid-cell.half-hour {
  border-bottom: 1px dashed #d1d1d1;  /* 半点虚线 */
}
```

### 2. 拖拽效果

```css
.dragging-course-preview {
  pointer-events: none !important;
  z-index: 1000 !important;
  transition: none !important;
  cursor: grabbing !important;
}

.course-item.blur-effect {
  filter: blur(2px);
  opacity: 0.3;
  transform: scale(0.98);
}
```

### 3. 响应式设计

```css
@media screen and (max-width: 768px) {
  .day-column, .day-column-container {
    min-width: 100px;
  }
  
  .time-column, .time-labels {
    width: 60px;
  }
}
```

## 关键配置参数

```javascript
const defaultHeight = 80;        // 每小时像素高度
const timeSnap = 5;             // 时间对齐精度（分钟）
const maxColumns = 动态计算;      // 最大列数
const timeSlots = 48;           // 时间段数量（24小时 × 2）
```

## 数据流图

```
用户拖拽 -> 鼠标事件 -> 坐标转换 -> 时间对齐 -> 冲突检测 -> 布局重计算 -> 视图更新
    ↓
 性能优化 <- requestAnimationFrame <- 事件节流 <- DOM缓存
```

## 扩展指南

### 1. 添加新功能

#### 添加课程类型支持
```javascript
const course = {
  // 现有字段...
  type: 'lecture',      // 新增：课程类型
  priority: 1,          // 新增：优先级
  location: '教室A'     // 新增：地点
}
```

#### 添加时间冲突提示
```javascript
const checkTimeConflict = (course, newDay, newStartTime, newEndTime) => {
  const conflicts = courses.value.filter(c => 
    c.id !== course.id && 
    c.day === newDay &&
    timeOverlap(c.startTime, c.endTime, newStartTime, newEndTime)
  )
  
  if (conflicts.length > 0) {
    ElMessage.warning('时间冲突，请重新选择时间')
    return false
  }
  return true
}
```

### 2. 性能优化建议

#### 虚拟滚动
对于大量课程的场景，可以实现虚拟滚动：

```javascript
const visibleCourses = computed(() => {
  const start = scrollTop.value / defaultHeight
  const end = start + viewportHeight.value / defaultHeight
  return courses.value.filter(course => {
    const courseStart = parseTime(course.startTime)
    const courseEnd = parseTime(course.endTime)
    return courseEnd > start && courseStart < end
  })
})
```

#### Web Worker
将复杂的布局计算移至Web Worker：

```javascript
// worker.js
self.onmessage = function(e) {
  const { courses } = e.data
  const layouts = calculateAllLayouts(courses)
  self.postMessage({ layouts })
}

// 主线程
const worker = new Worker('worker.js')
worker.postMessage({ courses: courses.value })
worker.onmessage = (e) => {
  layouts.value = e.data.layouts
}
```

## 测试策略

### 1. 单元测试

```javascript
describe('时间转换', () => {
  test('pixelToTime 正确转换坐标', () => {
    const result = pixelToTime(150, 320)
    expect(result.day).toBe(1)
    expect(result.hour).toBe(4)
  })
  
  test('hourToTimeString 正确格式化时间', () => {
    expect(hourToTimeString(8.5)).toBe('08:30')
    expect(hourToTimeString(14.25)).toBe('14:15')
  })
})
```

### 2. 集成测试

```javascript
describe('拖拽功能', () => {
  test('拖拽课程到新位置', async () => {
    const course = createTestCourse()
    await dragCourse(course, { x: 300, y: 480 })
    
    expect(course.day).toBe(2)
    expect(course.startTime).toBe('06:00')
  })
})
```

### 3. 性能测试

```javascript
describe('性能测试', () => {
  test('1000门课程的布局计算时间', () => {
    const courses = createManyCourses(1000)
    const start = performance.now()
    calculateDayLayout(courses)
    const end = performance.now()
    
    expect(end - start).toBeLessThan(100) // 100ms内完成
  })
})
```

## 常见问题与解决方案

### 1. 拖拽卡顿
**原因**：频繁的DOM操作和计算
**解决**：
- 使用requestAnimationFrame节流
- 缓存DOM查询结果
- 减少调试输出

### 2. 时间对齐不准确
**原因**：浮点数精度问题
**解决**：
```javascript
const alignedHour = Math.round(hourDecimal * 12) / 12
```

### 3. 课程重叠显示
**原因**：布局算法处理不当
**解决**：
- 改进冲突检测逻辑
- 优化列分配算法
- 增加边界检查

### 4. 内存泄漏
**原因**：事件监听器未清理
**解决**：
```javascript
onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }
})
```

## 总结

这个Vue课表拖拽组件采用了现代前端开发的最佳实践：

1. **组合式API**：提供了更好的逻辑复用和类型推导
2. **响应式系统**：利用Vue 3的响应式特性实现高效的状态管理
3. **性能优化**：通过缓存、节流等技术保证流畅的用户体验
4. **算法设计**：使用贪心算法解决复杂的布局问题
5. **用户体验**：提供直观的拖拽反馈和视觉效果

该组件可以作为复杂前端组件开发的参考案例，展示了如何将算法思维应用到实际的前端开发中。对于新前端开发者来说，这是一个很好的学习材料，涵盖了从基础的事件处理到复杂的算法实现等多个层面的知识点。