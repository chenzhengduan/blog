# BaseSelect 基础选择器组件

`BaseSelect` 是一个统一的基础选择器组件，为所有业务选择器提供通用的功能和一致的交互体验。

## 特性

- ✅ **统一的外观和交互**：所有选择器具有一致的样式和行为
- ✅ **灵活的配置**：支持多种配置选项和自定义函数
- ✅ **搜索功能**：内置搜索框和远程搜索支持
- ✅ **多选支持**：支持单选和多选模式
- ✅ **自定义渲染**：支持自定义选项和空状态渲染
- ✅ **数据映射**：灵活的数据字段映射
- ✅ **加载状态**：内置加载状态管理
- ✅ **事件处理**：完整的事件回调支持

## 基础用法

```vue
<template>
  <BaseSelect
    v-model="selectedValue"
    :options="options"
    placeholder="请选择"
    @change="handleChange"
  />
</template>

<script setup>
import BaseSelect from '@/components/common/base-select/base-select.vue'

const selectedValue = ref('')
const options = ref([
  { value: '1', label: '选项1' },
  { value: '2', label: '选项2' }
])

const handleChange = (value) => {
  console.log('选择变化:', value)
}
</script>
```

## Props

### 基础属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `modelValue` | `String/Number/Array` | `''` | 绑定值 |
| `placeholder` | `String` | `'请选择'` | 占位符 |
| `clearable` | `Boolean` | `true` | 是否可清空 |
| `disabled` | `Boolean` | `false` | 是否禁用 |
| `selectStyle` | `Object` | `{ width: '100%' }` | 选择器样式 |
| `loading` | `Boolean` | `false` | 加载状态 |

### 多选相关

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `multiple` | `Boolean` | `false` | 是否多选 |

### 搜索相关

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `showSearch` | `Boolean` | `false` | 是否显示搜索框 |
| `searchPlaceholder` | `String` | `'搜索'` | 搜索框占位符 |
| `filterable` | `Boolean` | `false` | 是否可过滤 |
| `remote` | `Boolean` | `false` | 是否远程搜索 |
| `remoteMethod` | `Function` | `null` | 远程搜索方法 |

### 数据相关

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `options` | `Array` | `[]` | 选项数据 |
| `valueKey` | `String` | `'value'` | 值字段名 |
| `labelKey` | `String` | `'label'` | 标签字段名 |
| `descKey` | `String` | `'desc'` | 描述字段名 |
| `disabledKey` | `String` | `'disabled'` | 禁用字段名 |

### 自定义函数

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `getOptionKey` | `Function` | `null` | 获取选项键值 |
| `getOptionValue` | `Function` | `null` | 获取选项值 |
| `getOptionLabel` | `Function` | `null` | 获取选项标签 |
| `getOptionDesc` | `Function` | `null` | 获取选项描述 |
| `getOptionDisabled` | `Function` | `null` | 获取选项禁用状态 |

### 空状态

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `emptyText` | `String` | `'暂无数据'` | 空状态文本 |

## Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `update:modelValue` | `value` | 值更新时触发 |
| `change` | `value` | 选择变化时触发 |
| `clear` | - | 清空时触发 |
| `search` | `keyword, options` | 搜索时触发 |
| `focus` | - | 聚焦时触发 |
| `visible-change` | `visible` | 下拉框显示/隐藏时触发 |

## Slots

| 插槽名 | 参数 | 说明 |
|--------|------|------|
| `option` | `{ item }` | 自定义选项渲染 |
| `empty` | - | 自定义空状态渲染 |
| `footer` | - | 自定义底部内容 |

## 使用示例

### 1. 基础选择器

```vue
<BaseSelect
  v-model="selectedValue"
  :options="options"
  placeholder="请选择城市"
/>
```

### 2. 带搜索的选择器

```vue
<BaseSelect
  v-model="selectedValue"
  :options="options"
  :show-search="true"
  :filterable="true"
  search-placeholder="搜索城市"
/>
```

### 3. 远程搜索选择器

```vue
<BaseSelect
  v-model="selectedValue"
  :options="options"
  :show-search="true"
  :remote="true"
  :remote-method="searchUsers"
  search-placeholder="搜索用户"
/>
```

### 4. 自定义数据字段

```vue
<BaseSelect
  v-model="selectedValue"
  :options="users"
  value-key="id"
  label-key="name"
  desc-key="email"
  placeholder="请选择用户"
/>
```

### 5. 自定义选项渲染

```vue
<BaseSelect
  v-model="selectedValue"
  :options="classrooms"
  value-key="ClassroomID"
  label-key="ClassroomName"
>
  <template #option="{ item }">
    <div class="option-line-1">{{ item.ClassroomName }}</div>
    <div class="option-line-2">可容纳 {{ item.PersonCount }} 人</div>
  </template>
</BaseSelect>
```

### 6. 自定义空状态

```vue
<BaseSelect
  v-model="selectedValue"
  :options="options"
  empty-text="暂无数据"
>
  <template #empty>
    <div class="custom-empty">
      <el-icon><InfoFilled /></el-icon>
      <span>暂无匹配数据</span>
    </div>
  </template>
</BaseSelect>
```

### 7. 多选模式

```vue
<BaseSelect
  v-model="selectedValues"
  :options="options"
  :multiple="true"
  placeholder="请选择多个选项"
/>
```

## 业务组件示例

### CampusSelect（校区选择器）

```vue
<template>
  <BaseSelect
    v-model="selectedValue"
    :options="campusList"
    :loading="loading"
    value-key="CampusID"
    label-key="CampusName"
    placeholder="请选择校区"
    empty-text="无校区数据"
    @change="handleChange"
  />
</template>
```

### ClassroomSelect（教室选择器）

```vue
<template>
  <BaseSelect
    v-model="selectedValue"
    :options="classroomList"
    :loading="loading"
    :show-search="true"
    :remote="true"
    :remote-method="searchClassrooms"
    value-key="ClassroomID"
    label-key="ClassroomName"
    :get-option-desc="getClassroomDesc"
    placeholder="请选择教室"
  >
    <template #option="{ item }">
      <div class="option-line-1">{{ item.ClassroomName }}</div>
      <div class="option-line-2">可容纳 {{ item.PersonCount }} 人</div>
    </template>
  </BaseSelect>
</template>
```

## 最佳实践

1. **数据格式统一**：建议使用统一的数据格式，如 `{ value, label, desc }`
2. **缓存机制**：对于远程数据，建议实现缓存机制避免重复请求
3. **错误处理**：在远程搜索中做好错误处理和用户提示
4. **性能优化**：大量数据时考虑虚拟滚动或分页加载
5. **样式定制**：通过全局CSS类名进行样式定制，保持一致性

## 迁移指南

从现有选择器迁移到 `BaseSelect`：

1. **替换模板**：将 `el-select` 替换为 `BaseSelect`
2. **调整Props**：使用 `BaseSelect` 的Props配置
3. **保留业务逻辑**：保留API调用、数据处理等业务逻辑
4. **测试功能**：确保所有功能正常工作
5. **优化代码**：移除重复的模板和样式代码 