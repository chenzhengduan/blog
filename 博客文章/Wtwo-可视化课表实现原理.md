# 可视化课表实现原理详解

## 目录
- [1. 基础概念](#1-基础概念)
- [2. 核心挑战：时间冲突](#2-核心挑战时间冲突)
- [3. 解决方案：分列显示](#3-解决方案分列显示)
- [4. 算法实现的四个步骤](#4-算法实现的四个步骤)
- [5. 可视化布局原理](#5-可视化布局原理)
- [6. 时间坐标系统](#6-时间坐标系统)
- [7. 实际案例分析](#7-实际案例分析)
- [8. 代码实现详解](#8-代码实现详解)
- [9. 性能优化](#9-性能优化)
- [10. 常见问题与解决方案](#10-常见问题与解决方案)

## 1. 基础概念

### 1.1 什么是可视化课表？
可视化课表是一个将课程安排以图形化方式展示的系统，类似于日历应用中的周视图。

**基本组成元素：**
- **时间轴**：纵向显示24小时时间刻度
- **日期轴**：横向显示一周7天
- **课程块**：在对应时间和日期位置显示的矩形区域
- **网格背景**：帮助用户快速定位时间的参考线

### 1.2 数据结构设计
```javascript
// 课程对象结构
const course = {
  id: 1,                    // 唯一标识
  name: 'Java编程',         // 课程名称
  day: 1,                   // 星期几 (0=周一, 1=周二, ...)
  startTime: '09:00',       // 开始时间
  endTime: '11:00',         // 结束时间
  color: '#1890ff'          // 显示颜色
}
```

## 2. 核心挑战：时间冲突

### 2.1 问题描述
在实际的课程安排中，同一天可能存在多门课程的时间有重叠：

```
周二课程示例：
- Java编程：    09:00-11:00
- 数据库设计：  09:30-10:30  ← 与Java编程冲突
- Web开发：     14:00-16:00
- 软件测试：    14:30-15:30  ← 与Web开发冲突
- 项目测试：    14:00-17:30  ← 与Web开发和软件测试都冲突
```

### 2.2 冲突判断算法
两个课程时间重叠的数学判断条件：
```javascript
function hasTimeConflict(courseA, courseB) {
  const startA = parseTime(courseA.startTime)
  const endA = parseTime(courseA.endTime)
  const startB = parseTime(courseB.startTime)
  const endB = parseTime(courseB.endTime)
  
  // 重叠条件：A的开始时间 < B的结束时间 且 A的结束时间 > B的开始时间
  return startA < endB && endA > startB
}
```

**重叠判断示例：**
- Java编程(9:00-11:00) vs 数据库设计(9:30-10:30)
- 判断：9:00 < 10:30 ✓ 且 11:00 > 9:30 ✓ → 有重叠

## 3. 解决方案：分列显示

### 3.1 核心思想
将每一天的区域划分为多个垂直的"列"，时间冲突的课程放置在不同的列中并排显示。

```
原始重叠显示：           分列显示：
┌─────────────┐         ┌──────┬──────┬──────┐
│ Java编程    │         │Java  │数据库│      │
│ ┌─────────┐ │   →    │编程  │设计  │      │
│ │数据库设计││         │      │      │      │
│ └─────────┘ │         └──────┴──────┴──────┘
└─────────────┘
```

### 3.2 列数计算原理
**关键概念：最大并发数**
- 在任意时间点，同时进行的课程数量
- 这个数量决定了需要多少列来显示

## 4. 算法实现的四个步骤

### 4.1 第一步：冲突关系分析
建立课程之间的冲突关系图。

```javascript
// 1. 为每门课程建立冲突列表
const conflicts = new Map()

courses.forEach(course => {
  const conflictingCourses = new Set()
  
  courses.forEach(otherCourse => {
    if (course.id !== otherCourse.id && hasTimeConflict(course, otherCourse)) {
      conflictingCourses.add(otherCourse.id)
    }
  })
  
  conflicts.set(course.id, conflictingCourses)
})
```

**冲突关系示例：**
```
Java编程 → [数据库设计]
数据库设计 → [Java编程]
Web开发 → [软件测试, 项目测试]
软件测试 → [Web开发, 项目测试]
项目测试 → [Web开发, 软件测试]
```

### 4.2 第二步：计算最大并发数
找出在任意时间点同时进行的最多课程数。

```javascript
// 2. 收集所有时间节点
const timePoints = new Set()
courses.forEach(course => {
  timePoints.add(parseTime(course.startTime))
  timePoints.add(parseTime(course.endTime))
})

// 3. 计算每个时间点的并发数
let maxColumns = 1
timePoints.forEach(timePoint => {
  let concurrentCourses = 0
  
  courses.forEach(course => {
    const start = parseTime(course.startTime)
    const end = parseTime(course.endTime)
    
    // 检查该时间点是否在课程时间范围内
    if (start <= timePoint && timePoint < end) {
      concurrentCourses++
    }
  })
  
  maxColumns = Math.max(maxColumns, concurrentCourses)
})
```

**并发数计算示例：**
```
时间点 09:15：Java编程 + 数据库设计 = 2门课程
时间点 14:15：Web开发 + 软件测试 + 项目测试 = 3门课程
最大并发数：3
```

### 4.3 第三步：列位置分配（贪心算法）
使用贪心策略为每门课程分配列位置。

```javascript
// 4. 初始化列占用记录
const columnOccupancy = Array.from({ length: maxColumns }, () => [])
const coursePositions = new Map()

// 5. 按开始时间排序处理课程
const sortedCourses = courses.sort((a, b) => parseTime(a.startTime) - parseTime(b.startTime))

sortedCourses.forEach(course => {
  const courseStart = parseTime(course.startTime)
  const courseEnd = parseTime(course.endTime)
  
  // 6. 寻找第一个可用的列
  let assignedColumn = -1
  
  for (let col = 0; col < maxColumns; col++) {
    let canUseColumn = true
    
    // 检查该列是否在当前时间段内被占用
    for (const period of columnOccupancy[col]) {
      if (courseStart < period.end && courseEnd > period.start) {
        canUseColumn = false
        break
      }
    }
    
    if (canUseColumn) {
      assignedColumn = col
      // 标记该列在此时间段被占用
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

**贪心策略优势：**
1. **简单高效**：每门课程选择第一个可用列，算法复杂度低
2. **空间紧凑**：优先使用前面的列，避免不必要的空白
3. **视觉友好**：相关课程容易聚集在相邻位置

### 4.4 第四步：样式计算
将逻辑位置转换为CSS样式属性。

```javascript
// 7. 计算CSS样式
const layouts = new Map()
const columnWidth = 100 / maxColumns  // 列宽度百分比

courses.forEach(course => {
  const courseStart = parseTime(course.startTime)
  const courseEnd = parseTime(course.endTime)
  const courseDuration = courseEnd - courseStart
  const column = coursePositions.get(course.id)
  
  layouts.set(course.id, {
    width: `${columnWidth}%`,                    // 宽度 = 总宽度 / 列数
    left: `${column * columnWidth}%`,            // 左边距 = 列号 × 列宽
    top: `${courseStart * 120}px`,               // 顶部位置 = 开始时间 × 像素比例
    height: `${courseDuration * 120}px`          // 高度 = 持续时间 × 像素比例
  })
})
```

## 5. 可视化布局原理

### 5.1 HTML结构设计
```html
<div class="schedule-container">
  <!-- 表头：显示星期几 -->
  <div class="header">
    <div class="time-column">时间</div>
    <div class="day-column">周一</div>
    <div class="day-column">周二</div>
    <!-- ... 其他天 -->
  </div>
  
  <!-- 内容区域 -->
  <div class="schedule-content">
    <div class="time-list">
      <!-- 时间标签列 -->
      <div class="time-labels">
        <div class="time-label">00:00</div>
        <div class="time-label">01:00</div>
        <!-- ... 24小时 -->
      </div>
      
      <!-- 课程网格 -->
      <div class="schedule-grid">
        <div class="day-column-container">
          <!-- 网格背景 -->
          <div class="grid-background">
            <div class="grid-cell"></div>
            <!-- ... 48个半小时格子 -->
          </div>
          
          <!-- 课程容器 -->
          <div class="courses-container">
            <div class="course-item" style="position: absolute; ...">
              Java编程
            </div>
            <!-- ... 其他课程 -->
          </div>
        </div>
        <!-- ... 其他天 -->
      </div>
    </div>
  </div>
</div>
```

### 5.2 CSS定位系统
```css
/* 关键样式解析 */
.courses-container {
  position: relative;          /* 为绝对定位的子元素提供参考 */
  width: 100%;
  height: 100%;
  overflow: hidden;            /* 防止内容溢出撑开容器 */
}

.course-item {
  position: absolute;          /* 绝对定位，允许重叠和精确控制 */
  box-sizing: border-box;      /* 边框和内边距包含在尺寸内 */
  border-radius: 4px;
  padding: 4px;
  font-size: 12px;
  cursor: pointer;
  z-index: 2;                  /* 确保在网格背景之上 */
}

.day-column-container {
  flex: 1;                     /* 均分可用宽度 */
  position: relative;
  min-width: 150px;            /* 最小宽度保证可读性 */
  overflow: hidden;            /* 防止被内容撑开 */
}
```

## 6. 时间坐标系统

### 6.1 时间单位转换
```javascript
// 时间字符串转换为数值（小时为单位）
function parseTime(timeStr) {
  const [hours, minutes] = timeStr.split(':').map(Number)
  return hours + minutes / 60
}

// 数值转换回时间字符串
function formatTime(timeNum) {
  const hours = Math.floor(timeNum)
  const minutes = Math.round((timeNum - hours) * 60)
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
}
```

### 6.2 像素坐标计算
**设计规则：1小时 = 120像素**

```javascript
// 时间 → 像素位置
const timeToPixel = (timeNum) => timeNum * 120

// 示例计算：
parseTime('09:00')   // → 9.0
parseTime('09:30')   // → 9.5
parseTime('11:00')   // → 11.0

timeToPixel(9.0)     // → 1080px (顶部位置)
timeToPixel(2.0)     // → 240px (高度，2小时课程)
```

### 6.3 网格系统
- **时间精度**：30分钟为最小单位
- **网格密度**：48个格子（24小时 × 2）
- **格子高度**：60像素（120像素/小时 ÷ 2）

## 7. 实际案例分析

### 7.1 周二课程安排
```javascript
const tuesdayCourses = [
  { id: 26, name: 'Java编程',    startTime: '09:00', endTime: '11:00' },
  { id: 27, name: '数据库设计',  startTime: '09:30', endTime: '10:30' },
  { id: 28, name: 'Web开发',     startTime: '14:00', endTime: '16:00' },
  { id: 29, name: '软件测试',    startTime: '14:30', endTime: '15:30' },
  { id: 31, name: '项目测试',    startTime: '14:00', endTime: '17:30' }
]
```

### 7.2 算法执行过程

**步骤1：冲突分析**
```
Java编程(9:00-11:00) ↔ 数据库设计(9:30-10:30)     ✓ 冲突
Web开发(14:00-16:00) ↔ 软件测试(14:30-15:30)     ✓ 冲突  
Web开发(14:00-16:00) ↔ 项目测试(14:00-17:30)     ✓ 冲突
软件测试(14:30-15:30) ↔ 项目测试(14:00-17:30)    ✓ 冲突
```

**步骤2：并发数计算**
```
时间点分析：
09:15 → Java编程 + 数据库设计 = 2门课程
14:15 → Web开发 + 软件测试 + 项目测试 = 3门课程

最大并发数：3 → 需要3列
```

**步骤3：列分配（按开始时间排序）**
```
1. Java编程(09:00)    → 分配到列0
2. 数据库设计(09:30)  → 列0被占用(09:00-11:00)，分配到列1
3. Web开发(14:00)     → 所有列都空闲，分配到列0
4. 项目测试(14:00)    → 列0被占用(14:00-16:00)，分配到列1
5. 软件测试(14:30)    → 列0、1都被占用，分配到列2
```

**步骤4：样式计算**
```
列宽：100% ÷ 3 = 33.33%

Java编程：   { width: '33.33%', left: '0%',      top: '1080px', height: '240px' }
数据库设计： { width: '33.33%', left: '33.33%',  top: '1140px', height: '120px' }
Web开发：    { width: '33.33%', left: '0%',      top: '1680px', height: '240px' }
项目测试：   { width: '33.33%', left: '33.33%',  top: '1680px', height: '420px' }
软件测试：   { width: '33.33%', left: '66.66%',  top: '1740px', height: '120px' }
```

### 7.3 最终布局效果
```
09:00  ┌─────────┬─────────┬─────────┐
       │Java编程 │         │         │
09:30  │         ┌─────────┐         │
       │         │数据库设计│         │
10:30  │         └─────────┘         │
11:00  └─────────┘         │         │
       ...
14:00  ┌─────────┬─────────┬─────────┐
       │Web开发  │项目测试 │         │
14:30  │         │         ┌─────────┐
       │         │         │软件测试 │
15:30  │         │         └─────────┘
16:00  └─────────┘         │         │
17:30            └─────────┘         │
```

## 8. 代码实现详解

### 8.1 核心函数：calculateDayLayout
```javascript
const calculateDayLayout = (dayIndex) => {
  // 1. 获取并排序当天所有课程
  const allCoursesInDay = courses.value.filter(c => c.day === dayIndex)
  const sortedCourses = [...allCoursesInDay].sort((a, b) => {
    const aStart = parseTime(a.startTime)
    const bStart = parseTime(b.startTime)
    if (aStart !== bStart) return aStart - bStart
    return parseTime(a.endTime) - parseTime(b.endTime)
  })
  
  // 2. 建立冲突关系
  const conflicts = new Map()
  sortedCourses.forEach(course => {
    const conflictingCourses = new Set()
    sortedCourses.forEach(otherCourse => {
      if (course.id !== otherCourse.id && hasTimeConflict(course, otherCourse)) {
        conflictingCourses.add(otherCourse.id)
      }
    })
    conflicts.set(course.id, conflictingCourses)
  })
  
  // 3. 计算最大并发数
  let maxColumns = 1
  const timePoints = new Set()
  sortedCourses.forEach(course => {
    timePoints.add(Math.round(parseTime(course.startTime) * 60))
    timePoints.add(Math.round(parseTime(course.endTime) * 60))
  })
  
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
  
  // 4. 分配列位置
  const coursePositions = new Map()
  const columnOccupancy = Array.from({ length: maxColumns }, () => [])
  
  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.startTime)
    const courseEnd = parseTime(course.endTime)
    let assignedColumn = -1
    
    for (let col = 0; col < maxColumns; col++) {
      let canUseColumn = true
      for (const period of columnOccupancy[col]) {
        if (courseStart < period.end && courseEnd > period.start) {
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
  
  // 5. 生成布局样式
  const layouts = new Map()
  const columnWidth = 100 / maxColumns
  
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
  })
  
  return layouts
}
```

### 8.2 样式应用函数：getCourseStyle
```javascript
const getCourseStyle = (course, day) => {
  const dayLayouts = calculateDayLayout(day)
  const layout = dayLayouts.get(course.id)
  
  if (!layout) {
    return { display: 'none' }
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
    color: '#ffffff',
    overflow: 'hidden',
    boxSizing: 'border-box',
    zIndex: 2,
    boxShadow: '0 2px 6px rgba(0, 0, 0, 0.2)',
    transition: 'all 0.2s ease',
    cursor: 'pointer'
  }
}
```

## 9. 性能优化

### 9.1 计算缓存
```javascript
// 使用Vue的computed缓存布局计算结果
const mondayLayouts = computed(() => calculateDayLayout(0))
const tuesdayLayouts = computed(() => calculateDayLayout(1))
// ... 其他天

// 只有当课程数据变化时才重新计算
```

### 9.2 虚拟滚动（可选）
对于大量课程的情况，可以实现虚拟滚动：
```javascript
// 只渲染可视区域内的课程
const visibleCourses = computed(() => {
  const scrollTop = scrollPosition.value
  const containerHeight = containerSize.value
  
  return courses.value.filter(course => {
    const courseTop = parseTime(course.startTime) * 120
    const courseBottom = parseTime(course.endTime) * 120
    
    return courseBottom > scrollTop && courseTop < scrollTop + containerHeight
  })
})
```

### 9.3 事件优化
```javascript
// 防抖滚动事件
const debouncedScroll = debounce(() => {
  updateCurrentTimePosition()
}, 100)

// 使用passive监听器
element.addEventListener('scroll', debouncedScroll, { passive: true })
```

## 10. 常见问题与解决方案

### 10.1 问题：课程文字被截断
**解决方案：**
```css
.course-item {
  font-size: 12px;
  line-height: 1.2;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 短课程使用更小字体 */
.course-item.short-duration {
  font-size: 10px;
}
```

### 10.2 问题：容器被课程内容撑开
**解决方案：**
```css
.courses-container,
.day-column-container {
  overflow: hidden;  /* 防止内容溢出撑开容器 */
}
```

### 10.3 问题：时间精度不够
**解决方案：**
```javascript
// 支持更精细的时间间隔（如15分钟）
const timeSlots = Array.from({ length: 96 }, (_, i) => {
  const hour = Math.floor(i / 4)
  const minute = (i % 4) * 15
  return { value: i / 4, label: `${hour}:${minute.toString().padStart(2, '0')}` }
})
```

### 10.4 问题：移动端显示适配
**解决方案：**
```css
@media screen and (max-width: 768px) {
  .day-column-container {
    min-width: 100px;  /* 移动端减小最小宽度 */
  }
  
  .course-item {
    font-size: 10px;   /* 移动端使用更小字体 */
    padding: 2px;
  }
  
  .time-column {
    width: 60px;       /* 缩小时间列宽度 */
  }
}
```

## 总结

这个可视化课表系统的核心是一个优雅的冲突解决算法：

1. **分析冲突关系** - 理解问题的本质
2. **计算空间需求** - 确定解决方案的规模
3. **贪心分配策略** - 高效分配资源
4. **坐标系统转换** - 将逻辑转换为视觉

这种设计不仅解决了时间冲突的显示问题，还具有良好的扩展性和性能表现。无论是简单的课程表还是复杂的会议安排系统，都可以基于这个核心算法进行实现和优化。

---

> 文档最后更新：2024年12月
> 
> 适用版本：Vue 3 + Element Plus
> 
> 相关文件：`src/components/AppointCourse.vue` 