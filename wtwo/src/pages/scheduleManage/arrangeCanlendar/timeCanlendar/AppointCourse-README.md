# AppointCourse 课程安排组件

一个功能丰富的Vue3课程安排日程表组件，支持拖拽、悬浮效果、全屏显示等特性。

## 功能特性

- ✅ **拖拽功能**：支持课程拖拽调整时间和日期
- ✅ **智能拖拽判断**：以课块中心为准判断日期变更，更符合用户直觉
- ✅ **时间冲突处理**：智能处理课程时间冲突，自动布局
- ✅ **悬浮效果**：课程悬浮时的毛玻璃效果
- ✅ **当前时间线**：实时显示当前时间位置
- ✅ **全屏模式**：支持全屏显示
- ✅ **响应式设计**：适配不同屏幕尺寸
- ✅ **事件通知**：完整的事件系统
- ✅ **可配置性**：多项功能可开关

## Props 属性

| 属性名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `courses` | `Array` | `[]` | 课程数据数组 |
| `showCurrentTimeLine` | `Boolean` | `true` | 是否显示当前时间线 |
| `enableDrag` | `Boolean` | `true` | 是否启用拖拽功能 |
| `showHelpButton` | `Boolean` | `true` | 是否显示帮助按钮 |
| `weekDays` | `Array` | `['周一', '周二', ...]` | 星期显示文本 |

### 课程数据格式

```javascript
const course = {
  id: 1,                    // 唯一标识符（必需）
  name: '高等数学',          // 课程名称（必需）
  className: '数学1班',      // 班级名称（可选）
  studentName: '张小明',     // 学员姓名（可选）
  day: 0,                   // 星期几（0-6，0表示周一，必需）
  startTime: '08:00',       // 开始时间（必需）
  endTime: '10:00',         // 结束时间（必需）
  color: '#1890ff'          // 背景颜色（可选）
}
```

## Events 事件

| 事件名 | 参数 | 说明 |
| --- | --- | --- |
| `course-update` | `{ course, originalCourse, newPosition }` | 课程位置更新时触发 |
| `course-click` | `{ course, event }` | 课程被点击时触发 |
| `course-hover` | `course` | 课程悬浮状态改变时触发 |
| `fullscreen-change` | `isFullscreen` | 全屏状态改变时触发 |

### 事件详情

#### course-update
当课程被拖拽到新位置时触发：
```javascript
{
  course: {
    // 更新后的课程对象
    id: 1,
    name: '高等数学',
    day: 1,                 // 新的天
    startTime: '09:00',     // 新的开始时间
    endTime: '11:00',       // 新的结束时间
    // ... 其他属性
  },
  originalCourse: {
    // 原始课程对象
    day: 0,
    startTime: '08:00',
    endTime: '10:00',
    // ... 其他属性
  },
  newPosition: {
    day: 1,
    startTime: '09:00',
    endTime: '11:00'
  }
}
```

## 暴露的方法

通过组件引用可以调用以下方法：

| 方法名 | 参数 | 返回值 | 说明 |
| --- | --- | --- | --- |
| `toggleFullscreen` | - | - | 切换全屏状态 |
| `getCourses` | - | `Array` | 获取当前课程数据 |
| `setCourses` | `newCourses: Array` | - | 设置课程数据 |
| `recalculateLayout` | - | - | 重新计算布局 |

## 基本使用

```vue
<template>
  <AppointCourse
    :courses="courseData"
    :show-current-time-line="true"
    :enable-drag="true"
    @course-update="handleCourseUpdate"
    @course-click="handleCourseClick"
  />
</template>

<script setup>
import { ref } from 'vue'
import AppointCourse from './components/AppointCourse.vue'

// 课程数据
const courseData = ref([
  {
    id: 1,
    name: '高等数学',
    className: '数学1班',
    studentName: '张小明',
    day: 0,
    startTime: '08:00',
    endTime: '10:00',
    color: '#1890ff'
  },
  // 更多课程...
])

// 处理课程更新
const handleCourseUpdate = (updateInfo) => {
  console.log('课程更新:', updateInfo)
  // 这里可以调用API保存到后端
}

// 处理课程点击
const handleCourseClick = ({ course, event }) => {
  console.log('点击课程:', course.name)
  // 这里可以打开课程详情弹窗等
}
</script>
```

## 高级使用

### 禁用拖拽功能
```vue
<AppointCourse
  :courses="courseData"
  :enable-drag="false"
  @course-click="showCourseDetails"
/>
```

### 自定义星期显示
```vue
<AppointCourse
  :courses="courseData"
  :week-days="['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']"
/>
```

### 通过引用调用方法
```vue
<template>
  <div>
    <el-button @click="toggleFullscreen">切换全屏</el-button>
    <el-button @click="addCourse">添加课程</el-button>
    
    <AppointCourse
      ref="scheduleRef"
      :courses="courseData"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'

const scheduleRef = ref()
const courseData = ref([])

const toggleFullscreen = () => {
  scheduleRef.value?.toggleFullscreen()
}

const addCourse = () => {
  const newCourse = {
    id: Date.now(),
    name: '新课程',
    day: 0,
    startTime: '14:00',
    endTime: '16:00',
    color: '#52c41a'
  }
  
  courseData.value.push(newCourse)
  
  // 或者使用组件方法
  // const currentCourses = scheduleRef.value?.getCourses() || []
  // scheduleRef.value?.setCourses([...currentCourses, newCourse])
}
</script>
```

## 样式定制

组件使用了 scoped 样式，如果需要定制样式，可以使用深度选择器：

```vue
<style>
/* 定制课程块样式 */
.schedule-container :deep(.course-item) {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* 定制时间线样式 */
.schedule-container :deep(.current-time-line) {
  background-color: #ff6b35;
}
</style>
```

## 注意事项

1. **课程ID唯一性**：确保每个课程的ID是唯一的
2. **时间格式**：开始时间和结束时间必须是 `HH:MM` 格式
3. **天数范围**：day 属性的值必须在 0-6 之间
4. **响应式更新**：当外部更新 courses 数据时，组件会自动重新渲染
5. **拖拽精度**：拖拽时会自动对齐到5分钟网格
6. **性能优化**：组件内部使用了多种性能优化策略，可以处理大量课程数据

## 拖拽功能详解

### 拖拽判断逻辑
- **智能判断**：以课块中心点位置判断所属日期，而不是课块左边
- **边界保护**：30px范围内的微小移动不会触发日期变更，避免误操作
- **实时反馈**：拖拽过程中目标日期列会有高亮显示

### 拖拽行为
1. **跨天拖拽**：支持在一周7天内自由拖拽到不同的天
2. **时间调整**：垂直方向拖拽调整课程时间
3. **确认机制**：拖拽结束后会弹出确认对话框，显示变更详情
4. **精度对齐**：自动对齐到5分钟网格

### 拖拽示例
```
课块在周一和周二边界处：
- 课块中心在周一左侧 → 属于周一
- 课块中心在周二左侧 → 属于周二
- 30px范围内微小移动 → 保持原天不变
```

## 浏览器兼容性

- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## 完整示例

参考 `src/pages/test/ScheduleExample.vue` 文件查看完整的使用示例。 