# Select 组件加载状态和空数据显示规范

## 概述

所有业务select组件都实现了统一的加载状态和空数据显示逻辑，确保用户体验的一致性和友好性。

## 组件列表

- `campus-select.vue` - 校区选择器
- `class-select.vue` - 班级选择器  
- `classroom-select.vue` - 教室选择器
- `course-select.vue` - 课程选择器
- `subject-select.vue` - 科目选择器
- `time-select.vue` - 时间选择器

## 加载状态逻辑

### 1. 加载状态触发条件

#### campus-select.vue
- **触发条件**: 组件挂载时，如果校区数据列表为空
- **数据源**: 从 `useUserCampuses` store 获取
- **停止条件**: 校区数据加载完成或超时（10秒）

#### class-select.vue
- **触发条件**: 调用API获取班级列表时
- **数据源**: `/api/class/query` 接口
- **停止条件**: API请求完成（成功或失败）

#### classroom-select.vue
- **触发条件**: 组件挂载时，如果有校区ID但教室列表为空
- **数据源**: 通过props传入的 `classroomList`
- **停止条件**: 教室数据加载完成或超时（10秒）

#### course-select.vue
- **触发条件**: 调用API获取课程列表时
- **数据源**: `/api/shift/query` 接口
- **停止条件**: API请求完成（成功或失败）

#### subject-select.vue
- **触发条件**: 组件挂载时，如果科目数据列表为空
- **数据源**: 从 `useDictFieldsStore` store 获取
- **停止条件**: 科目数据加载完成或超时（10秒）

#### time-select.vue
- **触发条件**: 调用API获取时间列表时
- **数据源**: `/api/Course/QueryCourseTime` 接口
- **停止条件**: API请求完成（成功或失败）

### 2. 超时机制

为了避免loading状态一直显示，所有依赖store数据的组件都设置了10秒超时：

```javascript
// 设置超时，避免loading状态一直显示
setTimeout(() => {
  if (loading.value) {
    loading.value = false
    console.warn('组件名: 加载超时，停止loading状态')
  }
}, 10000) // 10秒超时
```

## 空数据显示逻辑

### 1. 显示优先级

所有组件的空数据显示都遵循以下优先级：

1. **加载中** (`loading = true`)
2. **输入条件未满足** (如：未选择校区、班级等)
3. **无数据** (确定没有数据)
4. **搜索无结果** (有数据但搜索无匹配)

### 2. 具体显示文案

#### campus-select.vue
```vue
<span v-if="loading">加载中...</span>
<span v-else>暂无匹配的校区</span>
```

#### class-select.vue
```vue
<span v-if="loading">加载中...</span>
<span v-else-if="!campusId">请先在左侧选择"上课校区"</span>
<span v-else>暂无匹配的班级</span>
```

#### classroom-select.vue
```vue
<span v-if="loading">加载中...</span>
<span v-else-if="!campusId">请先选择"上课校区"</span>
<span v-else-if="!hasClassroomData">暂无授权教室</span>
<span v-else>暂无匹配的教室</span>
```

#### course-select.vue
```vue
<span v-if="loading">加载中...</span>
<span v-else-if="!classId">请先在左侧选择"上课班级"</span>
<span v-else>暂无匹配的课程</span>
```

#### subject-select.vue
```vue
<span v-if="loading">加载中...</span>
<span v-else-if="!classId">请先在左侧选择"上课班级"</span>
<span v-else>暂无匹配的科目</span>
```

#### time-select.vue
```vue
<span v-if="loading">加载中...</span>
<span v-else-if="!hasCommonTimeData">暂未设置常用时间</span>
<span v-else>暂无匹配的时间段</span>
```

## 技术实现

### 1. 响应式数据

```javascript
const loading = ref(false)
```

### 2. 监听器设置

```javascript
// 监听数据变化，自动停止loading状态
watch(dataList, (newList) => {
  if (newList.length > 0) {
    loading.value = false
  }
}, { deep: true })
```

### 3. 生命周期处理

```javascript
onMounted(() => {
  // 检查是否需要显示loading状态
  if (needLoading) {
    loading.value = true
    // 设置监听器和超时机制
  }
})
```

## 用户体验优化

### 1. 加载状态指示
- 使用Element Plus的 `:loading="loading"` 属性
- 在下拉面板中显示"加载中..."文案
- 禁用选择器交互直到加载完成

### 2. 错误处理
- 超时机制避免无限loading
- 控制台警告便于调试
- 优雅降级到空数据状态

### 3. 数据一致性
- 监听数据变化自动更新状态
- 选中值验证确保数据有效性
- 清空无效选择避免错误

## 扩展指南

### 添加新的Select组件

1. **添加loading状态**:
```javascript
const loading = ref(false)
```

2. **添加loading属性**:
```vue
<el-select :loading="loading" ...>
```

3. **实现空数据显示逻辑**:
```vue
<template #empty>
  <div class="empty-state">
    <el-icon><InfoFilled /></el-icon>
    <span v-if="loading">加载中...</span>
    <span v-else-if="condition1">条件1提示</span>
    <span v-else-if="condition2">条件2提示</span>
    <span v-else>暂无数据</span>
  </div>
</template>
```

4. **设置超时机制**:
```javascript
setTimeout(() => {
  if (loading.value) {
    loading.value = false
    console.warn('组件名: 加载超时')
  }
}, 10000)
```

### 最佳实践

1. **优先级明确**: 加载状态 > 输入条件 > 无数据 > 搜索无结果
2. **超时保护**: 所有异步操作都要设置合理的超时时间
3. **错误处理**: 提供友好的错误提示和降级方案
4. **性能优化**: 避免不必要的重复请求和状态更新
5. **用户体验**: 提供清晰的状态反馈和操作指引 