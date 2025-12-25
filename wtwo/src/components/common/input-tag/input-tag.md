# Input Tag 搜索增强功能

## 概述

Input Tag 组件现在支持 API 搜索功能，提供类似 el-select 的下拉选项界面，支持单选和多选模式。

## Props 配置

### 基础配置

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `selected` | Array | `[]` | 已选中的项目数组 |
| `value` | String | `''` | 输入框的值 |
| `label` | String | `''` | 标签文本 |
| `placeholder` | String | `''` | 占位符文本 |
| `disabled` | Boolean | `false` | 是否禁用 |
| `multiple` | Boolean | `false` | 是否支持多选 |
| `searchable` | Boolean | `false` | 是否可搜索 |
| `isLine` | Boolean/String | `true` | 是否单行显示 |
| `showDelete` | Boolean | `true` | 是否显示清空和删除按钮 |

### 新增配置

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `apiConfig` | Object | `undefined` | API 搜索配置 |
| `fieldMapping` | Object | `{label: 'Name', value: 'ID', key: 'ID'}` | 字段映射配置 |
| `optionConfig` | Object | `{emptyText: '暂无数据', loadingText: '加载中...', maxHeight: '200px'}` | 选项配置 |
| `allowUnselectedInput` | Boolean | `false` | 单选模式下是否允许未选中状态保留输入内容 |

## 快速开始

### 1. 基本用法（向后兼容）

```vue
<template>
  <input-tag
    v-model:value="value"
    :selected="selected"
    placeholder="请输入内容"
  />
</template>

<script setup>
import { ref } from 'vue'
import InputTag from '@/components/form/input-tag.vue'

const value = ref('')
const selected = ref([])
</script>
```

### 2. API 搜索功能

```vue
<template>
  <input-tag
    v-model:value="searchValue"
    :selected="selectedItems"
    :api-config="apiConfig"
    :multiple="true"
    placeholder="请输入搜索关键词"
  />
</template>

<script setup>
import { ref } from 'vue'
import InputTag from '@/components/form/input-tag.vue'
import { queryShift } from '@/main/api/comm'

const searchValue = ref('')
const selectedItems = ref([])

const apiConfig = {
  apiFunction: queryShift,
  params: { status: 1 },
  searchParam: 'keyword',
  debounceTime: 300
}
</script>
```

## 新增功能

### 1. API 搜索集成

通过配置 `apiConfig` 属性，组件可以调用后端 API 进行动态搜索：

```vue
<template>
  <input-tag
    v-model:value="searchValue"
    :selected="selectedItems"
    :api-config="apiConfig"
    :multiple="true"
    placeholder="请输入搜索关键词"
    @api-success="handleApiSuccess"
    @api-error="handleApiError"
  />
</template>

<script setup>
import { queryShift } from '@/main/api/comm'

const apiConfig = {
  apiFunction: queryShift,
  params: { status: 1 },
  searchParam: 'keyword',
  debounceTime: 300
}
</script>
```

### 2. 字段映射配置

通过 `fieldMapping` 配置不同的数据结构：

```vue
<input-tag
  :api-config="apiConfig"
  :field-mapping="{
    label: 'Name',
    value: 'ID',
    key: 'ID'
  }"
/>
```

### 3. 自定义选项配置

通过 `optionConfig` 自定义显示文本和样式：

```vue
<input-tag
  :api-config="apiConfig"
  :option-config="{
    emptyText: '没有找到匹配的结果',
    loadingText: '正在搜索...',
    maxHeight: '300px'
  }"
/>
```

### 4. 多选支持

在多选模式下，下拉选项支持直接选择和自动去重：

- **直接选择**：点击选项会立即添加到 selected 数组
- **取消选择**：点击已选中的选项会从 selected 数组中移除
- **自动去重**：重复选择同一选项不会产生重复项
- **视觉反馈**：已选中的选项显示勾选图标

```vue
<input-tag
  :api-config="apiConfig"
  :multiple="true"
  :selected="selectedItems"
  placeholder="点击选项直接添加，支持自动去重"
/>
```

### 5. 按钮显示控制

通过 `showDelete` 属性可以控制是否显示清空和删除按钮：

```vue
<!-- 显示所有按钮（默认） -->
<input-tag
  :show-buttons="true"
  :selected="selectedItems"
  placeholder="显示清空和删除按钮"
/>

<!-- 隐藏所有按钮 -->
<input-tag
  :show-buttons="false"
  :selected="selectedItems"
  placeholder="隐藏清空和删除按钮"
/>
```

### 6. InputTagOption 组件

项目提供了 `InputTagOption` 组件，可以在 option 插槽中使用，提供美观的选项显示效果：

- **圆形头像**：自动生成带颜色的圆形头像，显示名称首字母
- **双行显示**：支持标题和副标题的双行显示
- **勾选状态**：自动显示选中状态的勾选图标
- **自定义字段**：支持自定义标题、副标题和颜色字段

```vue
<template>
  <input-tag :api-config="apiConfig" :multiple="true">
    <!-- 方形图标，支持图片和文字 -->
    <template #option="{ option, isSelected }">
      <InputTagOption
        :option="option"
        :isSelected="isSelected"
        titleField="Name"
        subtitleField="ID"
        :iconColor="option.color"
        :iconImage="option.avatar"
        iconShape="square"
      />
    </template>
  </input-tag>
  
  <!-- 圆形图标 -->
  <input-tag :api-config="apiConfig" :multiple="true">
    <template #option="{ option, isSelected }">
      <InputTagOption
        :option="option"
        :isSelected="isSelected"
        iconShape="round"
      />
    </template>
  </input-tag>
  
  <!-- 无图标模式 -->
  <input-tag :api-config="apiConfig" :multiple="true">
    <template #option="{ option, isSelected }">
      <InputTagOption
        :option="option"
        :isSelected="isSelected"
        :showIcon="false"
      />
    </template>
  </input-tag>
  
  <!-- 固定图标文本 -->
  <input-tag :api-config="apiConfig" :multiple="true">
    <template #option="{ option, isSelected }">
      <InputTagOption
        :option="option"
        :isSelected="isSelected"
        iconText="👤"
        iconColor="#409eff"
        iconShape="round"
      />
    </template>
  </input-tag>
</template>

<script setup>
import InputTagOption from '@/components/form/input-tag-option.vue'
</script>
```

### 6. 插槽支持

组件提供多个插槽用于自定义显示：

```vue
<input-tag :api-config="apiConfig" :multiple="true">
  <!-- 自定义头部 -->
  <template #dropdown-header>
    <div class="search-header">搜索结果</div>
  </template>
  
  <!-- 使用 InputTagOption 组件 -->
  <template #option="{ option, index, isSelected }">
    <InputTagOption
      :option="option"
      :isSelected="isSelected"
      titleField="Name"
      subtitleField="ID"
      iconColorField="color"
    />
  </template>
  
  <!-- 或者完全自定义选项样式 -->
  <template #option="{ option, index, isSelected }">
    <div class="custom-option">
      <div class="option-avatar" :style="{ backgroundColor: option.color }">
        {{ option.Name.charAt(0) }}
      </div>
      <div class="option-info">
        <div class="option-name">{{ option.Name }}</div>
        <div class="option-id">{{ option.ID }}</div>
      </div>
      <el-icon v-if="isSelected" style="color: var(--wtwo-color-primary);">
        <Check />
      </el-icon>
    </div>
  </template>
  
  <!-- 自定义底部 -->
  <template #dropdown-footer>
    <div class="search-footer">共找到 {{ results.length }} 个结果</div>
  </template>
  
  <!-- 自定义空状态 -->
  <template #empty>
    <div class="empty-state">暂无匹配结果</div>
  </template>
  
  <!-- 自定义加载状态 -->
  <template #loading>
    <div class="loading-state">搜索中...</div>
  </template>
</input-tag>
```

## API 参考

### 新增 Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| apiConfig | ApiConfig | undefined | API 配置对象 |
| fieldMapping | FieldMapping | { label: 'Name', value: 'ID', key: 'ID' } | 字段映射配置 |
| optionConfig | OptionConfig | { emptyText: '暂无数据', loadingText: '加载中...', maxHeight: '200px' } | 选项配置 |

### ApiConfig 类型

```typescript
interface ApiConfig {
  apiFunction: (params: any) => Promise<any>  // API 函数，如 queryShift
  params?: Record<string, any>                // 固定参数
  searchParam?: string                        // 搜索参数名，默认 'keyword'
  debounceTime?: number                       // 防抖时间，默认 300ms
}
```

### FieldMapping 类型

```typescript
interface FieldMapping {
  label?: string     // 显示字段名，默认 'Name'
  value?: string     // 值字段名，默认 'ID'
  key?: string       // 唯一标识字段，默认使用 value
}
```

### OptionConfig 类型

```typescript
interface OptionConfig {
  emptyText?: string     // 无数据提示，默认 '暂无数据'
  loadingText?: string   // 加载提示，默认 '加载中...'
  maxHeight?: string     // 最大高度，默认 '200px'
}
```

### InputTagOption Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| option | any | - | 选项数据对象 |
| isSelected | boolean | false | 是否已选中 |
| titleField | string | 'Name' | 标题字段名 |
| subtitleField | string | 'ID' | 副标题字段名 |
| iconField | string | 'Name' | 图标文本字段名（用于生成首字母） |
| iconColor | string | '' | 图标背景颜色（直接传值） |
| iconImage | string | '' | 图标图片 URL（直接传值） |
| iconShape | 'round' \| 'square' | 'square' | 图标形状 |
| showIcon | boolean | true | 是否显示左侧图标 |
| iconText | string | '' | 固定的图标文本（优先级高于字段提取） |

### 新增 Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| search | (keyword: string) | 搜索输入变化时触发 |
| api-success | (data: any[]) | API 搜索成功时触发 |
| api-error | (error: any) | API 搜索失败时触发 |
| option-select | (option: any) | 选项被选择时触发 |

### 新增 Slots

| 插槽名 | 参数 | 说明 |
|--------|------|------|
| dropdown-header | - | 下拉列表头部 |
| dropdown-footer | - | 下拉列表底部 |
| option | { option, index, isSelected } | 自定义选项显示，isSelected 表示是否已选中 |
| empty | - | 空状态显示 |
| loading | - | 加载状态显示 |

## 使用示例

### 基础 API 搜索

```vue
<template>
  <input-tag
    v-model:value="searchValue"
    :selected="selectedUsers"
    :api-config="userSearchConfig"
    :multiple="true"
    placeholder="搜索用户"
    @api-success="handleSearchSuccess"
    @option-select="handleUserSelect"
  />
</template>

<script setup>
import { ref } from 'vue'
import { queryShift } from '@/main/api/comm'

const searchValue = ref('')
const selectedUsers = ref([])

const userSearchConfig = {
  apiFunction: queryShift,
  params: { status: 1 },
  searchParam: 'keyword',
  debounceTime: 300
}

const handleSearchSuccess = (users) => {
  console.log('搜索到用户:', users)
}

const handleUserSelect = (user) => {
  console.log('选择了用户:', user)
}
</script>
```

### 单选模式

```vue
<template>
  <input-tag
    v-model:value="selectedValue"
    :selected="[]"
    :multiple="false"
    :api-config="singleSelectConfig"
    placeholder="请选择一个选项"
  />
</template>

<script setup>
import { queryShift } from '@/main/api/comm'

const selectedValue = ref('')

const singleSelectConfig = {
  apiFunction: queryShift,
  searchParam: 'keyword'
}
</script>
```

### 自定义字段映射

```vue
<template>
  <input-tag
    :api-config="apiConfig"
    :field-mapping="customFieldMapping"
    :selected="selectedItems"
  />
</template>

<script setup>
import { queryShift } from '@/main/api/comm'

const customFieldMapping = {
  label: 'displayName',  // 使用 displayName 作为显示文本
  value: 'userId',       // 使用 userId 作为值
  key: 'userId'          // 使用 userId 作为唯一标识
}

const apiConfig = {
  apiFunction: queryShift,
  searchParam: 'keyword'
}
</script>
```

## 向后兼容性

所有新功能都是可选的，不会影响现有的使用方式：

- 不配置 `apiConfig` 时，组件行为与之前完全一致
- 现有的 props、events 和 slots 继续正常工作
- 现有的样式和交互方式不受影响

## 键盘导航

组件支持键盘导航：

- `↑` / `↓` 箭头键：在选项间导航，高亮显示当前选项
- `Enter` 键：选择当前高亮的选项（多选模式下直接添加/移除，单选模式下选中并关闭）
- `Escape` 键：关闭下拉列表
- 输入搜索：直接输入文字进行搜索

## 性能优化

- **防抖搜索**：避免频繁的 API 调用
- **请求取消**：自动取消之前未完成的请求
- **错误处理**：优雅处理网络错误和数据解析错误
- **内存管理**：组件卸载时自动清理资源

## 测试

运行测试：

```bash
npm run test
```

测试覆盖了以下功能：
- 向后兼容性
- API 搜索功能
- 选项选择逻辑
- 字段映射
- 键盘导航
- 错误处理
- 生命周期管理