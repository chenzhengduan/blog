# BaseTable 表格组件 - 功能特性说明

## 🎯 核心特性一览

### 1. 行选择功能 ✅

**支持模式**：
- ✅ Checkbox 多选模式
- ✅ Radio 单选模式
- ✅ 全选/取消全选
- ✅ 受控模式（外部控制选中状态）

**使用方法**：
```vue
<BaseTable
  :row-selection="{
    enabled: true,
    type: 'checkbox',
    showSelectAll: true
  }"
  @selection-change="handleSelectionChange"
/>
```

**API**：
```javascript
// 获取选中的行
const selectedRows = tableRef.value?.getSelectedRows()

// 清空选择
tableRef.value?.clearSelection()
```

---

### 2. 批量编辑功能 🆕✅

**特点**：
- ✅ 选中多行后批量修改指定字段
- ✅ 对话框选择字段和输入新值
- ✅ 支持所有编辑器类型（select/input/date等）
- ✅ 自动保存历史（可撤销）

**使用方法**：
```vue
<script setup>
const batchEdit = () => {
  // 方式1：通过对话框（推荐）
  // 使用 BatchEditDialog 组件
  
  // 方式2：直接调用 API
  tableRef.value?.batchUpdateField('CampusID', '1')
  
  // 方式3：批量更新多个字段
  tableRef.value?.batchUpdateFields({
    CampusID: '1',
    ClassID: '2'
  })
}
</script>
```

---

### 3. 搜索/筛选功能 🆕✅

**特点**：
- ✅ 支持全列搜索或指定列搜索
- ✅ 实时筛选表格数据
- ✅ 模糊匹配，不区分大小写
- ✅ 搜索提示和高亮显示

**使用方法**：
```vue
<script setup>
const handleSearch = (data) => {
  baseTableRef.value?.handleSearch(data)
}

const clearSearch = () => {
  baseTableRef.value?.clearSearch()
}
</script>

<template>
  <SearchToolbar
    :columns="columns"
    @search="handleSearch"
    @clear="clearSearch"
  />
</template>
```

---

### 4. 列显示/隐藏配置 🆕✅

**特点**：
- ✅ 用户可自定义显示哪些列
- ✅ 全选/取消全选/重置功能
- ✅ 实时应用配置
- ✅ 显示已选列数统计

**使用方法**：
```vue
<script setup>
const visibleColumns = ref([])

const handleColumnChange = (fields) => {
  visibleColumns.value = fields
}
</script>

<template>
  <ColumnConfig
    :columns="columns"
    :visible="visibleColumns"
    @change="handleColumnChange"
  />
</template>
```

---

### 5. 复制粘贴功能 ✅

**快捷键**：
- `Ctrl+C` / `Cmd+C` - 复制选中的行
- `Ctrl+V` / `Cmd+V` - 粘贴到选中的行

**特点**：
- ✅ TSV 格式（Excel 兼容）
- ✅ 自动识别并跳过表头
- ✅ 支持跨应用复制粘贴
- ✅ 保留列顺序和数据格式

**使用方法**：
1. 选中要复制的行（使用 checkbox）
2. 按 `Ctrl+C` 复制
3. 选中要粘贴的目标行
4. 按 `Ctrl+V` 粘贴

或使用代码：
```javascript
tableRef.value?.handleCopy()   // 复制
tableRef.value?.handlePaste()  // 粘贴
```

---

### 3. 撤销/重做功能 ✅

**快捷键**：
- `Ctrl+Z` / `Cmd+Z` - 撤销
- `Ctrl+Y` / `Cmd+Shift+Z` - 重做

**特点**：
- ✅ 自动保存编辑历史
- ✅ 最多保存 50 条历史记录
- ✅ 撤销栈和重做栈独立管理
- ✅ 新编辑会清空重做栈

**使用方法**：
直接使用快捷键，或：
```javascript
tableRef.value?.undo()  // 撤销
tableRef.value?.redo()  // 重做
```

---

### 4. 列排序功能 ✅

**启用方式**：
在列配置中添加 `sortable: true`：
```javascript
{
  field: 'Date',
  title: '上课日期',
  sortable: true,  // 启用排序
  cellType: 'date'
}
```

**特点**：
- ✅ 点击表头切换排序
- ✅ 三种状态：升序 → 降序 → 不排序
- ✅ 支持前端排序或后端排序
- ✅ 触发 `sort-change` 事件

**监听排序变化**：
```vue
<BaseTable
  @sort-change="handleSortChange"
/>

<script setup>
const handleSortChange = (data) => {
  console.log('排序字段:', data.field)
  console.log('排序方式:', data.order) // 'asc' | 'desc' | null
  // 调用API进行后端排序
}
</script>
```

---

### 5. Tab 键导航 ✅

**快捷键**：
- `Tab` - 向右移动到下一个可编辑单元格
- `Shift+Tab` - 向左移动到上一个可编辑单元格
- 自动跨行导航（到达行尾自动跳到下一行首列）

**特点**：
- ✅ 只在可编辑列之间导航
- ✅ 自动确认当前编辑
- ✅ 类似 Excel 的导航体验
- ✅ 支持跨行循环

---

### 6. 加载状态 ✅

**使用方法**：
```vue
<script setup>
const loading = ref(false)

const saveData = async () => {
  loading.value = true
  try {
    await api.save(tableData.value)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <BaseTable :loading="loading" />
</template>
```

**效果**：
- ✅ 半透明遮罩层
- ✅ 旋转加载动画
- ✅ 自动阻止用户交互
- ✅ 显示"加载中..."提示

---

### 7. 单元格编辑 ✅

**编辑模式**：
- ✅ 点击单元格激活编辑器
- ✅ 编辑器无边框，与单元格融为一体
- ✅ 失焦或选择后自动提交

**键盘快捷键**：
| 按键 | 功能 |
|------|------|
| `Enter` | 确认编辑并提交 |
| `Esc` | 取消编辑并回退原值 |
| `↑` | 向上移动到同列上一行 |
| `↓` | 向下移动到同列下一行 |
| `Tab` | 向右移动到下一列 |
| `Shift+Tab` | 向左移动到上一列 |

**编辑器类型**：
- `select` - 下拉选择（支持搜索、清空）
- `input` - 文本/数字输入
- `date` - 日期选择器
- `time` - 时间输入
- `textarea` - 多行文本
- `custom` - 自定义组件（通过 `editorConfig.component` 传入）

---

### 8. 数据验证 ✅

**配置验证器**：
```javascript
{
  field: 'age',
  title: '年龄',
  cellType: 'input',
  editorConfig: {
    validator: (value, row) => {
      if (value < 0 || value > 150) {
        return false  // 验证失败
      }
      return true  // 验证通过
    }
  }
}
```

**支持异步验证**：
```javascript
validator: async (value, row) => {
  const result = await api.checkUnique(value)
  return result.isUnique
}
```

**验证失败效果**：
- ✅ 单元格红色背景
- ✅ 阻止编辑提交
- ✅ 错误标记保留直到验证通过

---

### 9. 单元格自动填充 ✅

**使用方式**：
1. 选中单元格
2. 拖动右下角填充柄
3. 横向或纵向填充

**特点**：
- ✅ 类似 Excel 的拖动填充
- ✅ 支持横向和纵向
- ✅ 填充前/后回调钩子
- ✅ 自动触发数据更新

---

## 📦 完整功能清单

| 功能 | 状态 | 快捷键 | 说明 |
|------|------|--------|------|
| 行选择（多选） | ✅ | - | checkbox 模式 |
| 行选择（单选） | ✅ | - | radio 模式 |
| 全选/取消全选 | ✅ | - | 表头 checkbox |
| 批量编辑 🆕 | ✅ | - | 批量修改字段 |
| 搜索/筛选 🆕 | ✅ | - | 全列或指定列搜索 |
| 列显示/隐藏 🆕 | ✅ | - | 自定义显示列 |
| 复制选中行 | ✅ | Ctrl+C | TSV 格式 |
| 粘贴到选中行 | ✅ | Ctrl+V | Excel 兼容 |
| 撤销操作 | ✅ | Ctrl+Z | 最多 50 条 |
| 重做操作 | ✅ | Ctrl+Y | 历史记录 |
| 列排序 | ✅ | 点击表头 | 升序/降序 |
| Tab 导航 | ✅ | Tab/Shift+Tab | 跨行导航 |
| 方向键导航 | ✅ | ↑↓ | 上下单元格 |
| 确认编辑 | ✅ | Enter | 提交并验证 |
| 取消编辑 | ✅ | Esc | 回退原值 |
| 加载状态 | ✅ | - | 遮罩动画 |
| 单元格自动填充 | ✅ | 拖动填充柄 | Excel 风格 |
| 数据验证 | ✅ | - | 同步/异步 |
| 自定义编辑器 | ✅ | - | 组件注入 |

---

## 🆕 新增功能亮点 (v2.1.0)

### 批量编辑
- 选中多行后一键修改相同字段
- 可视化对话框，操作简单
- 支持所有编辑器类型
- 自动保存到历史记录

### 搜索/筛选
- 支持全局搜索或指定列搜索
- 实时筛选，响应迅速
- 模糊匹配，不区分大小写
- 搜索结果实时反馈

### 列配置
- 用户可自由选择显示的列
- 全选/重置快捷操作
- 实时生效，无需刷新
- 显示统计信息

---

## 🚀 快速开始

### 基础用法

```vue
<script setup lang="ts">
import { ref } from 'vue'
import BaseTable from '@/components/base/table/BaseTable.vue'

const columns = [
  {
    field: 'name',
    title: '姓名',
    cellType: 'input',
    sortable: true
  },
  {
    field: 'age',
    title: '年龄',
    cellType: 'input',
    sortable: true,
    editorConfig: {
      type: 'number',
      validator: (value) => value > 0 && value < 150
    }
  }
]

const data = ref([
  { id: '1', name: '张三', age: 25 },
  { id: '2', name: '李四', age: 30 }
])

const rowSelection = ref({
  enabled: true,
  type: 'checkbox',
  showSelectAll: true
})

const loading = ref(false)
</script>

<template>
  <BaseTable
    :columns="columns"
    :data="data"
    :loading="loading"
    :row-selection="rowSelection"
    @selection-change="handleSelectionChange"
    @sort-change="handleSortChange"
  />
</template>
```

### 完整示例

参考 `src/components/business/schedule/ScheduleTable.vue`，包含所有功能的完整示例。

---

## 📚 相关文档

- [表格组件架构设计文档](../docs/table-component-architecture.md)
- [vue-fantable 官方文档](https://github.com/vue-easytable/vue-easytable)
- [Element Plus 官方文档](https://element-plus.org/)

---

## 🎉 总结

BaseTable 是一个功能强大、高度可配置的企业级表格编辑组件，支持：

✅ **15+ 核心功能** - 行选择、批量编辑、搜索筛选、列配置、复制粘贴、撤销重做、排序、导航等  
✅ **完整键盘支持** - 无需鼠标即可完成所有操作  
✅ **Excel 风格体验** - 自动填充、Tab 导航、跨应用复制粘贴  
✅ **高度可扩展** - 自定义编辑器、验证器、回调钩子  
✅ **TypeScript 类型安全** - 完整的类型定义和智能提示  
✅ **🆕 批量操作** - 批量编辑、搜索筛选、列显示配置  

### 版本更新

**v2.1.0 (2025-01-XX)** 🎉
- 🆕 批量编辑功能 - 选中多行批量修改字段
- 🆕 搜索/筛选功能 - 全列或指定列实时搜索
- 🆕 列显示/隐藏配置 - 用户自定义显示列

**v2.0.0** - 行选择、复制粘贴、撤销重做、排序、Tab导航、加载状态

**v1.0.0** - 基础表格编辑、单元格自动填充、数据验证

立即开始使用，打造你的高效数据编辑界面！ 🚀
