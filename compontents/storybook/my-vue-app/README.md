# Vue 组件演示系统

基于 Vue 3 + TypeScript + Element Plus 构建的组件演示系统，包含多个实用组件。

## 🚀 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

访问: `http://localhost:5174`

## 📋 项目功能

### 1. 排课管理系统
- 基于 vue-fantable 的高性能可编辑表格
- 支持行选择、自动填充、数据保存
- 路由: `/schedule`

### 2. 动态组件配置器 ⭐ NEW
- 运行时动态添加和配置表单组件
- 支持9种组件类型（输入框、下拉、日期、开关等）
- 自定义下拉选项、数值范围等配置
- 实时预览和数据导出
- 路由: `/component-builder`

详细说明请查看: [动态组件配置器使用指南](./COMPONENT_BUILDER_GUIDE.md)

## 📁 项目结构

```
src/
├── components/
│   ├── base/                    # 基础组件（通用、可复用）
│   │   └── table/
│   │       ├── BaseTable.vue    # 基础表格组件
│   │       └── types/
│   │           └── column.ts    # 列类型定义
│   │
│   ├── business/                # 业务组件（具体业务场景）
│   │   └── schedule/
│   │       ├── ScheduleTable.vue      # 排课表格业务组件
│   │       └── schedule-config.ts     # 排课表格配置
│   │
│   └── dynamic/                 # 动态组件
│       └── ComponentBuilder.vue  # 动态组件配置器
│
├── pages/                       # 页面
│   ├── ScheduleDemo.vue         # 排课演示页面
│   └── ComponentBuilderDemo.vue # 组件配置器演示页面
│
├── router/                      # 路由配置
│   └── index.ts
│
├── App.vue                      # 应用入口
└── main.ts                      # 入口文件
```

## 🎯 组件职责

### 1️⃣ 基础组件（Base Components）

**BaseTable.vue** - 通用表格基础组件
- **职责**：提供可复用的表格功能
- **功能**：
  - 基于 vue-fantable 封装
  - 支持 4 种编辑器（下拉选择、输入框、日期、时间）
  - 提供 Props/Events/Slots 接口
  - 处理单元格渲染和事件
- **特点**：
  - ✅ 通用性强，不包含业务逻辑
  - ✅ 可在多个业务场景复用
  - ✅ 配置化，灵活性高

### 2️⃣ 业务组件（Business Components）

**ScheduleTable.vue** - 排课业务组件
- **职责**：封装排课业务逻辑
- **功能**：
  - 使用 BaseTable 基础组件
  - 定义排课字段配置（15个字段）
  - 实现业务操作（新增、删除、保存）
  - 处理业务验证和数据管理
- **特点**：
  - ✅ 业务专用，只服务排课场景
  - ✅ 包含具体业务逻辑
  - ✅ 封装完整的业务功能

### 3️⃣ 应用入口

**App.vue** - 应用主页面
- **职责**：页面布局和组件集成
- **功能**：
  - 引入业务组件
  - 页面布局和样式
  - 数据展示
- **特点**：
  - ✅ 轻量级，只做页面级工作
  - ✅ 不包含业务逻辑

## 📦 技术栈

- **vue-fantable**: 0.2.9 - 高性能表格库
- **element-plus**: ^2.9.2 - UI 组件库
- **Vue 3**: Composition API + `<script setup>`
- **TypeScript**: 完整类型支持

## 🚀 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

## 📖 使用指南

### 1. 使用基础组件 BaseTable

```vue
<script setup lang="ts">
import BaseTable from '@/components/base/table/BaseTable.vue'

const columns = [
  { field: 'name', title: '姓名', cellType: 'input' },
  { field: 'age', title: '年龄', cellType: 'input' },
]

const data = [
  { id: '1', name: '张三', age: '20' },
  { id: '2', name: '李四', age: '25' },
]
</script>

<template>
  <BaseTable
    :columns="columns"
    :data="data"
    row-key="id"
    @cell-change="handleCellChange"
  />
</template>
```

### 2. 创建新的业务组件

参考 `ScheduleTable.vue` 的结构：

```vue
<script setup lang="ts">
import BaseTable from '../../base/table/BaseTable.vue'
import { myColumns, myData } from './my-config'

// 业务逻辑
const handleSave = () => {
  // 实现保存逻辑
}
</script>

<template>
  <BaseTable :columns="myColumns" :data="myData">
    <template #toolbar>
      <!-- 业务操作按钮 -->
      <button @click="handleSave">保存</button>
    </template>
  </BaseTable>
</template>
```

## 🎨 BaseTable API

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| columns | `ColumnConfig[]` | `[]` | 列配置数组 |
| data | `any[]` | `[]` | 表格数据 |
| rowKey | `string` | `'id'` | 行数据的唯一标识字段 |
| maxHeight | `string \| number` | `600` | 表格最大高度 |

### Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| cell-click | `{ row, column, rowIndex }` | 单元格点击事件 |
| cell-change | `{ row, column, value }` | 单元格值变化事件 |
| update:data | `data[]` | 数据更新事件 |

### Slots

| 插槽名 | 说明 |
|--------|------|
| toolbar | 工具栏插槽 |
| pagination | 分页插槽 |

### ColumnConfig 配置

```typescript
interface ColumnConfig {
  field: string              // 字段名
  key: string                // 唯一键
  title: string              // 列标题
  width?: number             // 列宽度
  align?: 'left' | 'center' | 'right'  // 对齐方式
  fixed?: 'left' | 'right'   // 固定列
  cellType?: 'select' | 'input' | 'date' | 'time'  // 单元格类型
  editorConfig?: {           // 编辑器配置
    // select 配置
    options?: Array<{ label: string; value: string }>
    multiple?: boolean
    filterable?: boolean
    clearable?: boolean
    
    // input 配置
    type?: 'text' | 'number'
    placeholder?: string
    maxlength?: number
    
    // date 配置
    format?: string
    valueFormat?: string
  }
}
```

## 📝 编辑器类型

### 1. 下拉选择器 (select)

```typescript
{
  field: 'campus',
  cellType: 'select',
  editorConfig: {
    options: [
      { label: '总部', value: '1' },
      { label: '分校', value: '2' }
    ],
    filterable: true,
    clearable: true,
    multiple: false  // 支持多选
  }
}
```

### 2. 输入框 (input)

```typescript
{
  field: 'count',
  cellType: 'input',
  editorConfig: {
    type: 'number',
    placeholder: '请输入',
    maxlength: 10
  }
}
```

### 3. 日期选择器 (date)

```typescript
{
  field: 'date',
  cellType: 'date',
  editorConfig: {
    type: 'date',
    format: 'YYYY-MM-DD',
    valueFormat: 'YYYY-MM-DD'
  }
}
```

### 4. 时间选择器 (time)

```typescript
{
  field: 'time',
  cellType: 'time',
  editorConfig: {
    placeholder: '请选择时间'
  }
}
```

## 🎯 设计原则

### 分层架构

```
App.vue (应用层)
    ↓ 使用
ScheduleTable.vue (业务层)
    ↓ 使用
BaseTable.vue (基础层)
    ↓ 使用
vue-fantable + element-plus (框架层)
```

### 职责划分

- **基础层**：通用功能，可复用
- **业务层**：具体业务，专用逻辑
- **应用层**：页面布局，轻量级

### 扩展方式

1. **添加新编辑器类型**：在 `BaseTable.vue` 添加新的 render 方法
2. **添加新业务组件**：参考 `ScheduleTable.vue` 创建新组件
3. **自定义样式**：通过 CSS 变量或深度选择器覆盖

## 📚 参考资料

- [vue-fantable 文档](https://github.com/vue-easytable/vue-easytable)
- [Element Plus 文档](https://element-plus.org/)
- [Vue 3 文档](https://cn.vuejs.org/)

## 🔧 开发建议

1. **基础组件开发**：保持通用性，避免业务耦合
2. **业务组件开发**：封装完整业务逻辑，提供清晰接口
3. **类型定义**：使用 TypeScript 定义所有接口
4. **代码注释**：关键逻辑添加注释说明
5. **统一风格**：遵循项目代码规范

## 📄 License

MIT
