# 排课管理系统功能说明文档

## 项目概述

本项目是一个基于 Vue 3 + TypeScript 的排课管理系统，采用微前端架构，主要包含日历视图排课、表格排课、排课列表等核心功能模块。

## 技术架构

### 微前端架构 (micro-app)

#### 1. 微前端集成
```javascript
// 主应用集成
window.microApp.addDataListener((data) => {
  console.log('接收到数据:', data)
})

// 子应用通信
window.microApp.dispatch({ type: 'updateSchedule', data: scheduleData })
```

#### 2. 父子组件通信机制
- **数据传递**: 通过 `props` 向下传递，通过 `emit` 向上传递
- **状态同步**: 使用 Pinia 进行全局状态管理
- **事件通信**: 自定义事件系统处理跨组件通信

#### 3. 微前端优势
- **独立部署**: 各模块可独立开发、测试、部署
- **技术栈隔离**: 不同模块可使用不同技术栈
- **团队协作**: 支持多团队并行开发

## 核心功能模块

### 1. 日历视图排课组件 (AppointCourse.vue)

#### 1.1 核心算法

##### 课程冲突检测与布局算法
```javascript
// 冲突检测算法
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

##### 贪心布局算法
```javascript
// 为每门课程分配列位置
const coursePositions = new Map()
const columnOccupancy = Array.from({ length: maxColumns }, () => [])

sortedCourses.forEach(course => {
  // 找到第一个可用的列
  for (let col = 0; col < maxColumns; col++) {
    let canUseColumn = true
    
    // 检查列冲突
    for (const occupiedPeriod of columnOccupancy[col]) {
      if (courseStart < occupiedPeriod.end && courseEnd > occupiedPeriod.start) {
        canUseColumn = false
        break
      }
    }
    
    if (canUseColumn) {
      coursePositions.set(course.id, col)
      columnOccupancy[col].push({
        start: courseStart,
        end: courseEnd,
        courseId: course.id
      })
      break
    }
  }
})
```

#### 1.2 核心功能

##### 拖拽功能
- **位置拖拽**: 支持课程块跨天拖拽，自动对齐到时间网格
- **高度调整**: 支持拖拽课程块上下边缘调整时间
- **实时预览**: 拖拽过程中显示预览块和时间指示器
- **确认机制**: 拖拽完成后弹出确认对话框

##### 时间网格系统
- **24小时完整支持**: 从 00:00 到 24:00 的完整时间范围
- **5分钟精度对齐**: 所有时间操作自动对齐到5分钟网格
- **响应式布局**: 支持不同屏幕尺寸的自适应

##### 交互体验优化
- **毛玻璃效果**: 悬浮时其他课程显示毛玻璃效果
- **临时占位块**: 点击空白区域创建临时占位块
- **当前时间线**: 实时显示当前时间位置
- **全屏模式**: 支持全屏查看和操作

#### 1.3 数据结构
```javascript
// 课程数据结构
const courseData = {
  id: 'unique_id',
  name: '课程名称',
  day: 0, // 0-6 表示周一到周日
  startTime: '08:00',
  endTime: '09:30',
  className: '班级名称',
  studentName: '学员姓名',
  color: '#3498db'
}
```

### 2. 表格排课组件 (tableCourseSchedule.vue)

#### 2.1 核心功能

##### 智能表格系统
- **可编辑单元格**: 双击单元格进入编辑模式
- **下拉选择器**: 集成各种业务选择器（班级、课程、教师等）
- **实时验证**: 输入时进行数据验证和格式化

##### 数据管理
```javascript
// 名称快照机制
const fieldMap = {
  'classId': 'className',
  'course': 'courseName', 
  'teacher': 'teacherName',
  'classroom': 'classroomName'
}

// 自动填充时复制名称快照
fieldsToCopy.forEach(field => {
  const fieldId = sourceRowData[field]
  const nameField = fieldMap[field]
  
  if (fieldId && nameField) {
    const sourceName = sourceRow[nameField]
    if (sourceName) {
      targetRow[nameField] = sourceName
    }
  }
})
```

##### 分组功能
- **动态分组**: 支持按任意字段进行数据分组
- **分组缓存**: 优化分组计算性能
- **分组显示**: 分组标题显示友好名称而非ID

##### 自动填充与复制粘贴
- **智能填充**: 支持拖拽填充柄自动填充数据
- **名称快照**: 自动复制ID对应的名称信息
- **跨行操作**: 支持批量选择和操作

#### 2.2 业务逻辑

##### 排课冲突检测
- **时间冲突**: 检测同一时间段的多重排课
- **资源冲突**: 检测教室、教师等资源的重复使用
- **预检查机制**: 支持开启/关闭冲突预检查

##### 数据持久化
- **数据保存**: 排课数据的保存和更新
- **数据同步**: 与后端API的数据同步
- **状态管理**: 使用Pinia管理排课状态

### 3. 排课列表组件 (arrangeTableList.vue)

#### 3.1 核心功能

##### 列表展示
- **数据展示**: 排课数据的表格展示
- **基础筛选**: 支持按条件筛选排课记录
- **数据操作**: 基础的增删改查操作

##### 数据管理
- **排课记录**: 查看和管理已创建的排课记录
- **状态跟踪**: 跟踪排课数据的状态变化
- **数据同步**: 与日历视图和表格排课的数据同步

#### 3.2 未来规划
- **分页显示**: 支持大数据量的分页展示
- **高级筛选**: 多条件筛选和搜索功能
- **批量操作**: 批量选择、删除、修改功能
- **审核流程**: 排课审核和审批流程
- **权限控制**: 基于角色的操作权限控制

## 数据流设计

### 1. 状态管理架构
```javascript
// Pinia Store 设计
const useUser = defineStore("userInfo", {
  state: () => ({
    user: { /* 用户信息 */ },
    SID: '' // 会话标识
  })
})

const useUserCampuses = defineStore("userCampuses", {
  state: () => ({
    userCampuses: [] // 用户校区列表
  })
})
```

### 2. 组件通信流程
```
父组件 → Props → 子组件
子组件 → Emit → 父组件
组件 → Store → 全局状态
```

### 3. 数据同步机制
- **响应式更新**: 基于 Vue 3 的响应式系统
- **计算属性**: 使用 computed 优化性能
- **监听器**: watch 监听数据变化并同步

## 性能优化

### 1. 性能优化策略
- **计算属性缓存**: 使用 computed 缓存计算结果
- **事件节流**: 对拖拽等频繁事件进行节流处理
- **异步更新**: 使用 nextTick 优化DOM更新

### 2. 缓存机制
- **分组缓存**: 缓存分组计算结果，避免重复计算
- **数据映射**: 缓存ID到名称的映射关系
- **组件状态**: 保持组件状态，减少重新渲染

### 3. 未来规划
- **虚拟滚动**: 处理大数据量的虚拟滚动
- **懒加载**: 按需加载数据和组件
- **代码分割**: 按模块进行代码分割优化

## 用户体验设计

### 1. 交互反馈
- **加载状态**: 数据加载时显示加载指示器
- **操作确认**: 拖拽和删除等重要操作前显示确认对话框
- **错误提示**: 基本的错误提示和用户引导

### 2. 响应式设计
- **基础适配**: 支持不同屏幕尺寸的基础适配
- **布局优化**: 表格和日历视图的响应式布局

### 3. 未来规划
- **移动端优化**: 移动设备的专门优化
- **主题系统**: 支持明暗主题切换
- **无障碍支持**: 键盘导航和屏幕阅读器支持

## 部署与维护

### 1. 当前部署
- **Docker部署**: 使用Docker容器化部署
- **Nginx配置**: 静态资源服务器配置
- **基础监控**: 基本的应用状态监控

### 2. 未来规划
- **性能监控**: 页面加载和运行性能监控
- **错误追踪**: 错误收集和追踪系统
- **用户行为分析**: 用户使用行为分析
- **自动化部署**: CI/CD自动化部署流程
- **版本管理**: 语义化版本和变更日志管理

## 总结

本项目通过微前端架构实现了模块化的排课管理系统，核心功能包括：

1. **日历视图排课**: 直观的拖拽式排课界面，支持课程冲突检测和智能布局
2. **表格排课**: 高效的批量排课操作，支持智能填充和分组管理
3. **排课列表**: 基础的排课数据管理和展示

系统采用现代化的技术栈和架构设计，具有良好的可扩展性和可维护性。通过微前端架构实现了模块间的解耦，便于团队协作和功能扩展。

### 技术亮点
- **智能冲突检测**: 自动检测和解决课程时间冲突
- **名称快照机制**: 优化数据填充和显示体验
- **响应式拖拽**: 流畅的拖拽交互体验
- **微前端架构**: 模块化开发和部署 