# 表格组件架构设计文档

## 📋 项目概述

基于 **vue-fantable** 和 **Element Plus** 构建的高性能、可配置的企业级表格编辑组件系统。

**技术栈**：
- Vue 3 (Composition API + TypeScript)
- vue-fantable 0.2.9 (高性能表格基础)
- Element Plus 2.9.2 (UI 组件库)

---

## 🏗️ 架构设计

### 三层架构

```
┌─────────────────────────────────────────────────────────┐
│  应用层 (App.vue)                                        │
│  - 页面布局和路由                                        │
│  - 轻量级，只做页面级工作                                │
└─────────────────────────────────────────────────────────┘
                          ↓ 使用
┌─────────────────────────────────────────────────────────┐
│  业务层 (ScheduleTable.vue)                              │
│  - 封装具体业务逻辑（排课管理）                           │
│  - 业务操作按钮（新增、删除、保存、自动填充）             │
│  - 业务数据管理和验证                                    │
└─────────────────────────────────────────────────────────┘
                          ↓ 使用
┌─────────────────────────────────────────────────────────┐
│  基础层 (BaseTable.vue)                                  │
│  - 通用表格编辑功能                                       │
│  - 可复用、可配置                                         │
│  - 不包含业务逻辑                                         │
└─────────────────────────────────────────────────────────┘
                          ↓ 使用
┌─────────────────────────────────────────────────────────┐
│  框架层 (vue-fantable + element-plus)                    │
│  - 表格渲染引擎                                           │
│  - UI 组件库                                              │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 目录结构

```
src/
├── components/
│   ├── base/                           # 基础组件（通用、可复用）
│   │   └── table/
│   │       ├── BaseTable.vue           # 基础表格组件
│   │       ├── types/
│   │       │   └── column.ts           # 列类型定义
│   │       └── README.md               # 组件文档
│   │
│   ├── common/                         # 通用业务组件
│   │   └── CustomSelect.vue            # 自定义下拉选择器示例
│   │
│   └── business/                       # 业务组件（具体业务场景）
│       └── schedule/
│           ├── ScheduleTable.vue       # 排课表格业务组件
│           └── schedule-config.ts      # 排课表格配置
│
└── App.vue                             # 应用入口
```

---

## 🎯 BaseTable 基础组件

### 组件职责

- ✅ 提供通用的表格编辑功能
- ✅ 支持多种编辑器类型（内置 + 自定义）
- ✅ 单元格点击激活编辑
- ✅ 键盘交互（Enter/Esc/上下箭头）
- ✅ 单元格自动填充（横向/纵向）
- ✅ 编辑验证和错误提示
- ✅ 完全可配置，无业务耦合

### 核心特性

#### 1. 点击编辑模式

- 点击单元格激活编辑器
- 编辑器无边框，与单元格融为一体
- 编辑时不立即关闭，支持连续输入
- 失焦或选择后自动提交

#### 2. 行选择功能 🆕

- 支持 checkbox（多选）和 radio（单选）模式
- 全选/取消全选功能
- 受控模式：支持外部控制选中状态
- 提供 `getSelectedRows()` 和 `clearSelection()` 方法
- 触发 `selection-change` 事件

#### 3. 复制粘贴功能 🆕

- `Ctrl+C` / `Cmd+C` 复制选中的行
- `Ctrl+V` / `Cmd+V` 粘贴到选中的行
- TSV 格式，Excel 兼容
- 自动识别并跳过表头
- 支持跨应用复制粘贴

#### 4. 撤销/重做功能 🆕

- `Ctrl+Z` / `Cmd+Z` 撤销上一步操作
- `Ctrl+Y` / `Cmd+Shift+Z` 重做
- 最多保存 50 条历史记录
- 自动在数据变化时保存历史状态

#### 5. 排序功能 🆕

- 列配置 `sortable: true` 启用排序
- 点击表头切换升序/降序/不排序
- 触发 `sort-change` 事件
- 支持前端排序或后端排序

#### 6. 键盘导航

| 按键 | 功能 |
|------|------|
| `Enter` | 确认编辑并提交 |
| `Esc` | 取消编辑并回退原值 |
| `↑` | 向上移动到同列上一行 |
| `↓` | 向下移动到同列下一行 |
| `Tab` | 向右移动到下一个可编辑单元格 |
| `Shift+Tab` | 向左移动到上一个可编辑单元格 |
| `Ctrl+C` | 复制选中的行 |
| `Ctrl+V` | 粘贴到选中的行 |
| `Ctrl+Z` | 撤销 |
| `Ctrl+Y` | 重做 |

#### 7. 加载状态 🆕

- `loading` prop 控制加载遮罩显示
- 半透明背景 + 旋转动画
- 自动阻止用户交互

#### 8. 编辑器类型

**内置编辑器**：
- `select` - 下拉选择（支持搜索、清空、多选）
- `input` - 文本/数字输入
- `date` - 日期选择器
- `time` - 时间输入
- `textarea` - 多行文本（自动用于 `InternalRemark` 字段）

**自定义编辑器**：
- 支持通过 `editorConfig.component` 传入业务组件
- 自动透传 props 和 slots
- 完全自定义样式和交互

#### 9. 单元格自动填充

- 选中单元格后，右下角出现填充柄
- 支持横向和纵向拖动填充
- 类似 Excel 的操作体验
- 填充前/后可配置回调函数

#### 10. 数据验证

- 支持列级 `validator` 函数（同步/异步）
- 验证失败自动标记错误单元格
- 错误单元格红色背景提示
- 取消编辑时自动回退原值

### API 文档

#### Props

```typescript
interface Props {
  columns: ColumnConfig[]   // 列配置数组
  data: any[]               // 表格数据
  rowKey?: string           // 行唯一标识字段（默认 'id'）
  maxHeight?: string | number // 表格最大高度（默认 600）
  loading?: boolean         // 加载状态（默认 false）🆕
  // 行选择配置 🆕
  rowSelection?: {
    enabled: boolean        // 是否启用行选择
    type?: 'checkbox' | 'radio' // 选择类型（默认 checkbox）
    showSelectAll?: boolean // 是否显示全选（默认 true）
    selectedRowKeys?: any[] // 受控模式：选中的行keys
  }
}
```

#### Events

```typescript
interface Emits {
  'cell-click': { row: any; column: any; rowIndex: number }
  'cell-change': { row: any; column: any; value: any }
  'update:data': any[]
  'selection-change': { selectedRowKeys: any[]; selectedRows: any[] } // 🆕
  'sort-change': { field: string; order: 'asc' | 'desc' | null } // 🆕
}
```

#### Slots

| 插槽名 | 说明 |
|--------|------|
| `toolbar` | 工具栏插槽 |
| `pagination` | 分页插槽 |

#### 暴露方法

```typescript
// 开始编辑指定单元格
startEdit(rowIndex: number, field: string): Promise<void>

// 取消编辑并回退原值
cancelEdit(): void

// 确认编辑（会执行验证）
confirmEdit(rowIndex: number, col: any): Promise<boolean>

// 行选择相关方法 🆕
getSelectedRows(): any[]           // 获取选中的行
clearSelection(): void             // 清空选择

// 复制粘贴 🆕
handleCopy(): Promise<void>        // 复制选中的行
handlePaste(): Promise<void>       // 粘贴到选中的行

// 撤销重做 🆕
undo(): void                       // 撤销
redo(): void                       // 重做

// 直接访问
selectedRowKeys: Ref<any[]>        // 选中的行keys（响应式）
```

### ColumnConfig 配置

```typescript
interface ColumnConfig {
  field: string                    // 字段名
  key: string                      // 唯一键
  title: string                    // 列标题
  width?: number                   // 列宽度
  align?: 'left' | 'center' | 'right'  // 对齐方式
  fixed?: 'left' | 'right'         // 固定列
  sortable?: boolean               // 是否支持排序 🆕
  cellType?: 'select' | 'input' | 'date' | 'time'  // 单元格类型
  editorConfig?: {
    // 自定义组件（优先使用）
    component?: any                // Vue 组件
    props?: Record<string, any>    // 传给组件的 props
    
    // 通用配置
    placeholder?: string
    
    // select 配置
    options?: Array<{ label: string; value: string }>
    multiple?: boolean
    filterable?: boolean
    clearable?: boolean
    
    // input 配置
    type?: 'text' | 'number' | 'textarea'
    maxlength?: number
    
    // date 配置
    format?: string
    valueFormat?: string
    
    // 验证配置
    validator?: (value: any, row: any) => boolean | Promise<boolean>
    required?: boolean
  }
}
```

### 使用示例

#### 基础用法

```vue
<script setup lang="ts">
import BaseTable from '@/components/base/table/BaseTable.vue'

const columns = [
  {
    field: 'name',
    key: 'name',
    title: '姓名',
    width: 120,
    cellType: 'input',
    editorConfig: {
      placeholder: '请输入姓名',
      maxlength: 20
    }
  },
  {
    field: 'age',
    key: 'age',
    title: '年龄',
    width: 100,
    cellType: 'input',
    editorConfig: {
      type: 'number',
      validator: (value) => {
        return value > 0 && value < 150
      }
    }
  }
]

const data = ref([
  { id: '1', name: '张三', age: 25 },
  { id: '2', name: '李四', age: 30 }
])
</script>

<template>
  <BaseTable
    :columns="columns"
    :data="data"
    @cell-change="handleChange"
  />
</template>
```

#### 使用自定义编辑器

```vue
<script setup lang="ts">
import CustomSelect from '@/components/common/CustomSelect.vue'

const columns = [
  {
    field: 'campus',
    title: '校区',
    cellType: 'select',
    editorConfig: {
      component: CustomSelect,  // 使用自定义组件
      props: {
        size: 'small'
      },
      options: [
        { label: '总部', value: '1' },
        { label: '分校', value: '2' }
      ]
    }
  }
]
</script>
```

#### 使用暴露的方法

```vue
<script setup lang="ts">
const tableRef = ref()

// 编程式开始编辑
const editCell = () => {
  tableRef.value?.startEdit(0, 'name')
}

// 取消当前编辑
const cancel = () => {
  tableRef.value?.cancelEdit()
}

// 🆕 获取选中的行
const getSelected = () => {
  const rows = tableRef.value?.getSelectedRows()
  console.log('选中的行：', rows)
}

// 🆕 复制粘贴
const copy = () => {
  tableRef.value?.handleCopy()
}

const paste = () => {
  tableRef.value?.handlePaste()
}

// 🆕 撤销重做
const undo = () => {
  tableRef.value?.undo()
}

const redo = () => {
  tableRef.value?.redo()
}
</script>

<template>
  <BaseTable ref="tableRef" :columns="columns" :data="data" />
  <button @click="editCell">编辑第一行姓名</button>
  <button @click="cancel">取消编辑</button>
  <button @click="getSelected">获取选中</button>
  <button @click="copy">复制</button>
  <button @click="paste">粘贴</button>
  <button @click="undo">撤销</button>
  <button @click="redo">重做</button>
</template>
```

#### 启用行选择 🆕

```vue
<script setup lang="ts">
const rowSelection = ref({
  enabled: true,
  type: 'checkbox', // 或 'radio'
  showSelectAll: true
})

const handleSelectionChange = (data) => {
  console.log('选中的keys：', data.selectedRowKeys)
  console.log('选中的行：', data.selectedRows)
}
</script>

<template>
  <BaseTable
    :columns="columns"
    :data="data"
    :row-selection="rowSelection"
    @selection-change="handleSelectionChange"
  />
</template>
```

#### 启用排序和加载状态 🆕

```vue
<script setup lang="ts">
const loading = ref(false)

const columns = [
  {
    field: 'name',
    title: '姓名',
    sortable: true, // 启用排序
    cellType: 'input'
  },
  {
    field: 'date',
    title: '日期',
    sortable: true,
    cellType: 'date'
  }
]

const handleSortChange = async (data) => {
  console.log('排序变化：', data.field, data.order)
  loading.value = true
  // 调用API进行排序
  await fetchSortedData(data.field, data.order)
  loading.value = false
}
</script>

<template>
  <BaseTable
    :columns="columns"
    :data="data"
    :loading="loading"
    @sort-change="handleSortChange"
  />
</template>
```

---

## 🎨 ScheduleTable 业务组件

### 组件职责

- ✅ 封装排课业务逻辑
- ✅ 提供业务操作按钮
- ✅ 管理排课数据
- ✅ 实现业务验证规则

### 核心功能

1. **新增排课** - 添加空白排课记录
2. **删除选中** - 批量删除排课记录
3. **保存数据** - 保存排课数据到服务器
4. **自动填充** - 将选中行数据向下填充到空行

### 字段配置

排课表格包含 15 个字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| 序号 | 只读 | 行序号 |
| 上课校区 | select | 下拉选择校区 |
| 上课班级 | select | 下拉选择班级 |
| 上课学员 | select | 下拉选择学员 |
| 上课课程 | select | 下拉选择课程 |
| 上课科目 | select | 下拉选择科目 |
| 上课日期 | date | 日期选择器 |
| 上课时间 | time | 时间范围输入 |
| 上课教室 | select | 下拉选择教室 |
| 任课老师 | select | 下拉选择老师 |
| 助教 | select | 多选下拉 |
| 线上课 | select | 是/否 |
| 开放预约 | select | 是/否 |
| 可约人数 | input | 数字输入 |
| 开课人数 | input | 数字输入 |
| 对内备注 | textarea | 多行文本 |

### 使用示例

```vue
<script setup lang="ts">
import ScheduleTable from '@/components/business/schedule/ScheduleTable.vue'

const tableRef = ref()

// 可以访问表格数据
const getData = () => {
  console.log(tableRef.value?.tableData)
}
</script>

<template>
  <ScheduleTable ref="tableRef" />
</template>
```

---

## 🎨 样式系统

### 单元格状态样式

```css
/* 可编辑单元格 */
.editable-cell {
  cursor: pointer;
  transition: background-color 0.2s;
}

.editable-cell:hover {
  background-color: #f5f7fa;
}

/* 错误单元格 */
.error-cell {
  background-color: #fef0f0;
  border-color: #f56c6c;
}
```

### 编辑器样式

编辑器默认样式：
- 无边框（border: none）
- 透明背景（background: transparent）
- 充满单元格（width: 100%, height: 100%）

可以通过 `editorConfig.props.style` 自定义样式。

---

## 🚀 高级特性

### 1. 编辑验证

```typescript
const columns = [
  {
    field: 'email',
    title: '邮箱',
    cellType: 'input',
    editorConfig: {
      validator: async (value) => {
        // 异步验证
        const res = await checkEmail(value)
        return res.valid
      }
    }
  }
]
```

### 2. 字段联动

```typescript
const handleCellChange = ({ row, column, value }) => {
  // 当选择班级时，自动加载该班级的学员
  if (column.field === 'ClassID') {
    loadStudents(value).then(students => {
      // 更新学员选项
      updateStudentOptions(students)
    })
  }
}
```

### 3. 批量操作

```typescript
// 自动填充（仅支持往下填充）
const autoFillDown = () => {
  const selectedIndex = tableData.value.findIndex(row => {
    return row.CampusID || row.ClassID // 找到有内容的行
  })
  
  if (selectedIndex === -1) {
    alert('请先选中要填充的行')
    return
  }
  
  const sourceRow = tableData.value[selectedIndex]
  
  // 向下填充到所有空行
  for (let i = selectedIndex + 1; i < tableData.value.length; i++) {
    const targetRow = tableData.value[i]
    if (isEmpty(targetRow)) {
      Object.keys(sourceRow).forEach(key => {
        if (key !== 'id') {
          targetRow[key] = sourceRow[key]
        }
      })
    }
  }
}
```

### 4. 自定义编辑器组件

创建自定义编辑器：

```vue
<!-- CustomSelect.vue -->
<template>
  <el-select 
    v-model="modelValueLocal" 
    :clearable="clearable" 
    filterable 
    style="width:100%"
  >
    <slot />
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElSelect } from 'element-plus'

const props = defineProps({
  modelValue: null,
  clearable: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue'])
const modelValueLocal = ref(props.modelValue)

watch(() => props.modelValue, (v) => modelValueLocal.value = v)
watch(modelValueLocal, (v) => emit('update:modelValue', v))
</script>
```

使用自定义编辑器：

```typescript
import CustomSelect from '@/components/common/CustomSelect.vue'

const columns = [
  {
    field: 'campus',
    cellType: 'select',
    editorConfig: {
      component: CustomSelect,
      options: [...]
    }
  }
]
```

---

## 📊 性能优化

### 1. 虚拟滚动

vue-fantable 内置虚拟滚动支持，处理大数据量：

```typescript
const virtualScrollOption = {
  enable: true,
  bufferScale: 1
}
```

### 2. 按需渲染

- 只在单元格激活时渲染编辑器
- 未激活时显示普通文本
- 减少组件实例数量

### 3. 事件防抖

```typescript
import { debounce } from 'lodash-es'

const handleCellChange = debounce((data) => {
  // 延迟处理单元格变化
}, 300)
```

---

## 🔧 开发指南

### 添加新的编辑器类型

1. 在 `BaseTable.vue` 的 `renderEditableCell` 方法中添加新 case：

```typescript
case 'custom-type':
  return h(CustomComponent, {
    ...editorCommonProps,
    // 自定义 props
  })
```

2. 更新 `ColumnConfig` 类型定义：

```typescript
cellType?: 'select' | 'input' | 'date' | 'time' | 'custom-type'
```

### 创建新的业务组件

参考 `ScheduleTable.vue` 结构：

1. 导入 `BaseTable` 组件
2. 定义业务字段配置
3. 实现业务操作方法
4. 封装业务验证逻辑

```vue
<script setup lang="ts">
import BaseTable from '@/components/base/table/BaseTable.vue'
import { myColumns } from './my-config'

const tableData = ref([])
const handleSave = () => { /* 业务逻辑 */ }
</script>

<template>
  <BaseTable :columns="myColumns" :data="tableData">
    <template #toolbar>
      <button @click="handleSave">保存</button>
    </template>
  </BaseTable>
</template>
```

---

## 📝 最佳实践

### 1. 组件职责分离

- **基础层**：只提供通用功能，不包含业务逻辑
- **业务层**：封装具体业务，提供完整功能
- **应用层**：只做页面布局和路由

### 2. 配置化开发

- 通过配置而非硬编码实现功能
- 使用 `editorConfig` 定义编辑器行为
- 支持运行时动态修改配置

### 3. 类型安全

- 使用 TypeScript 定义所有接口
- 为 props 和 emits 提供类型
- 使用泛型提高代码复用性

### 4. 性能优先

- 使用虚拟滚动处理大数据
- 按需渲染编辑器组件
- 合理使用防抖和节流

### 5. 用户体验

- 提供清晰的视觉反馈
- 支持键盘快捷操作
- 错误提示友好明确

---

## 🐛 常见问题

### Q: 如何禁用某个单元格的编辑？

```typescript
const columns = [
  {
    field: 'name',
    cellType: undefined,  // 不设置 cellType 即为只读
    title: '姓名'
  }
]
```

### Q: 如何自定义错误提示？

通过 validator 返回 Promise.reject 并携带错误信息：

```typescript
validator: async (value) => {
  if (!value) {
    throw new Error('该字段不能为空')
  }
  return true
}
```

### Q: 如何实现行级验证？

在 `cell-change` 事件中实现：

```typescript
const handleCellChange = ({ row }) => {
  // 检查行级规则
  if (row.startDate > row.endDate) {
    alert('开始日期不能大于结束日期')
  }
}
```

### Q: 如何保存编辑状态？

使用 `editingCell` 状态和 `editingOriginal` 存储：

```typescript
// BaseTable 内部已实现
// 取消编辑时自动回退到 editingOriginal
```

---

## 📚 参考资料

- [vue-fantable 文档](https://github.com/vue-easytable/vue-easytable)
- [Element Plus 文档](https://element-plus.org/)
- [Vue 3 文档](https://cn.vuejs.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)

---

## 📄 更新日志

### v2.0.0 (2025-01-XX) 🆕

**重大功能更新**：
- ✅ 行选择功能（checkbox/radio模式、全选、受控模式）
- ✅ 复制粘贴功能（Ctrl+C/V、TSV格式、Excel兼容）
- ✅ 撤销/重做功能（Ctrl+Z/Y、历史栈、最多50条记录）
- ✅ 列排序功能（点击表头排序、升序/降序切换）
- ✅ Tab键导航（Tab/Shift+Tab、跨行导航）
- ✅ 加载状态（loading遮罩、动画效果）

**API 增强**：
- 新增 `loading` prop
- 新增 `rowSelection` prop（行选择配置）
- 新增 `selection-change` 事件
- 新增 `sort-change` 事件
- 新增 `sortable` 列配置
- 新增 `getSelectedRows()` 方法
- 新增 `clearSelection()` 方法
- 新增 `handleCopy()` 方法
- 新增 `handlePaste()` 方法
- 新增 `undo()` 方法
- 新增 `redo()` 方法

**用户体验优化**：
- 支持全键盘操作（无需鼠标）
- 跨应用复制粘贴（与Excel互通）
- 历史记录管理（可撤销任意步骤）
- 批量操作支持（选中多行进行操作）

**ScheduleTable 示例增强**：
- 新增行选择演示
- 新增复制/粘贴按钮
- 新增撤销/重做按钮
- 新增加载状态演示
- 新增排序功能演示
- 按钮样式优化

### v1.0.0 (2025-11-25)

**核心功能**：
- ✅ 基础表格编辑组件（BaseTable）
- ✅ 排课业务组件（ScheduleTable）
- ✅ 4 种内置编辑器（select/input/date/time）
- ✅ 自定义编辑器支持
- ✅ 单元格自动填充
- ✅ 键盘交互（Enter/Esc/方向键）
- ✅ 数据验证和错误提示
- ✅ 点击激活编辑模式
- ✅ 编辑取消和回退
- ✅ 完整的 TypeScript 类型支持

**架构设计**：
- ✅ 三层架构（应用层/业务层/基础层）
- ✅ 组件职责清晰分离
- ✅ 高度可配置和扩展
- ✅ 无业务耦合的基础组件

---

## 📧 联系方式

如有问题或建议，欢迎提出 Issue 或 PR。

---

**文档最后更新时间**：2025-01-XX (v2.0.0)
