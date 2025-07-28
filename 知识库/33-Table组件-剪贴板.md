# Table组件-剪贴板

## 概述

Table组件支持剪贴板功能，可以像 Excel 那样，选中单元格并批量进行复制、粘贴、剪切、删除操作。

## 快捷键

剪贴板功能支持以下快捷键（参考 Excel 快捷键）：

| 功能 | 快捷键 |
|------|--------|
| 复制区域单元格内容 | Ctrl + C |
| 粘贴（支持 Excel 内容格式） | Ctrl + V |
| 剪切区域单元格内容 | Ctrl + X |
| 删除区域单元格内容 | Delete |

## 基础功能

通过 `clipboardOption` 配置启用剪贴板功能：

```vue
<template>
  <div>
    <fan-table
      :max-height="350"
      :columns="columns"
      :table-data="tableData"
      border-y
      :row-style-option="rowStyleOption"
      :cell-autofill-option="cellAutofillOption"
      :edit-option="editOption"
      row-key-field-name="rowKey"
      :clipboard-option="clipboardOption"
      :virtual-scroll-option="virtualScrollOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      // 剪贴板配置
      clipboardOption: {
        copy: true,
        paste: true,
        cut: true,
        delete: true,
        beforeCopy: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('beforeCopy')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        afterCopy: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('afterCopy')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        beforePaste: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('beforePaste')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        afterPaste: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('afterPaste')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        beforeCut: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('beforeCut')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        afterCut: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('afterCut')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        beforeDelete: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('beforeDelete')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
        afterDelete: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
          console.log('afterDelete')
          this.log({ data, selectionRangeIndexes, selectionRangeKeys })
        },
      },
      virtualScrollOption: {
        // 是否开启
        enable: false,
      },
      cellAutofillOption: true,
      editOption: {
        // 单元格值变化
        cellValueChange: ({ row, column }) => {},
      },
      columns: [
        {
          field: 'index',
          key: 'index',
          operationColumn: true,
          title: '#',
          width: 35,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        { field: 'col1', key: 'col1', title: 'Col1', edit: true, width: 150 },
        { field: 'col2', key: 'col2', title: 'Col2', edit: true, width: 150 },
        { field: 'col3', key: 'col3', title: 'Col3', edit: true, width: 150 },
        { field: 'col4', key: 'col4', title: 'Col4', edit: true, width: 150 },
        { field: 'col5', key: 'col5', title: 'Col5', edit: true, width: 150 },
        { field: 'col6', key: 'col6', title: 'Col6', edit: true, width: 150 },
      ],
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 100; i++) {
        data.push({
          rowKey: `row${i}`,
          col1: `A${i + 1}`,
          col2: `B${i + 1}`,
          col3: `C${i + 1}`,
          col4: `D${i + 1}`,
          col5: `E${i + 1}`,
          col6: `F${i + 1}`,
          col7: `G${i + 1}`,
          col8: `H${i + 1}`,
        })
      }
      this.tableData = data
    },
    log({ data, selectionRangeIndexes, selectionRangeKeys }) {
      console.log('data::', data)
      console.log('selectionRangeIndexes::', selectionRangeIndexes)
      console.log('selectionRangeKeys::', selectionRangeKeys)
    },
  },
}
</script>
```

## API 参数

### clipboardOption 剪贴板配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| copy | 是否开启单元格复制 | `Boolean` | - | true |
| paste | 是否开启单元格粘贴 | `Boolean` | - | true |
| cut | 是否开启单元格剪切 | `Boolean` | - | true |
| delete | 是否开启单元格删除 | `Boolean` | - | true |
| beforeCopy | 单元格拷贝前的回调方法，返回 false 则取消拷贝 | `Function` | - | - |
| afterCopy | 单元格拷贝后回调方法 | `Function` | - | - |
| beforePaste | 单元格粘贴前的回调方法，返回 false 则取消粘贴 | `Function` | - | - |
| afterPaste | 单元格粘贴后回调方法 | `Function` | - | - |
| beforeCut | 单元格剪切前的回调方法，返回 false 则取消剪切 | `Function` | - | - |
| afterCut | 单元格剪切后回调方法 | `Function` | - | - |
| beforeDelete | 单元格删除前的回调方法，返回 false 则取消删除 | `Function` | - | - |
| afterDelete | 单元格删除后回调方法 | `Function` | - | - |

### 回调函数参数说明

所有回调函数都接收以下参数：

```javascript
{
  data,                    // 操作的数据
  selectionRangeIndexes,   // 选择区域的索引信息
  selectionRangeKeys,      // 选择区域的key信息
}
```

#### 参数详细说明

1. **data** - 操作的数据内容
2. **selectionRangeIndexes** - 选择区域的行列索引信息
3. **selectionRangeKeys** - 选择区域的字段key信息

## 功能特性

### 1. 复制功能 (Ctrl + C)
- 支持单个或多个单元格复制
- 支持跨行跨列的区域复制
- 提供复制前后的回调钩子

### 2. 粘贴功能 (Ctrl + V)
- 支持从剪贴板粘贴数据
- 支持 Excel 格式内容粘贴
- 自动适配目标区域大小

### 3. 剪切功能 (Ctrl + X)
- 复制数据到剪贴板并清空原单元格
- 支持区域剪切操作

### 4. 删除功能 (Delete)
- 清空选中单元格的内容
- 支持批量删除操作

## 使用说明

1. **启用功能**：通过设置 `clipboardOption` 启用剪贴板功能
2. **选择区域**：鼠标拖拽选择要操作的单元格区域
3. **执行操作**：使用快捷键执行相应的剪贴板操作
4. **回调控制**：
   - `before*` 回调可以进行数据验证或阻止操作
   - `after*` 回调可以进行后续处理

## 配置示例

### 基础配置
```javascript
clipboardOption: {
  copy: true,
  paste: true,
  cut: true,
  delete: true,
}
```

### 带回调的配置
```javascript
clipboardOption: {
  copy: true,
  paste: true,
  cut: true,
  delete: true,
  beforeCopy: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
    // 复制前验证
    console.log('准备复制数据:', data)
    return true // 返回 false 可阻止复制
  },
  afterPaste: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
    // 粘贴后处理
    console.log('粘贴完成:', data)
  },
}
```

## 应用场景

- 数据录入时的批量操作
- 表格数据的快速编辑
- Excel 数据的导入导出
- 数据的批量复制和移动

## 注意事项

1. 需要设置 `row-key-field-name` 属性
2. 列需要设置 `edit: true` 才能进行编辑操作
3. 建议关闭行高亮以获得更好的用户体验
4. 通过 `before*` 回调返回 `false` 可以阻止相应操作
5. 支持与 Excel 的数据交互，可以直接复制粘贴