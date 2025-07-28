# Table 组件-操作列

## 概述

1、通过在 columns 设置 `operationColumn=true` 作为操作列
2、操作列默认左固定
3、操作列支持行多选、整行复制、整行剪切、行位置交换（开发中）等行操作。此列功能类似 excel 的操作列功能
4、操作列的单元格不允许单元格选择、不允许单元格编辑、不允许单元格自动填充

## 基础功能

```vue
<template>
    <div>
        <fan-table
            :columns="columns"
            :table-data="tableData"
            border-y
            :cell-autofill-option="cellAutofillOption"
            :edit-option="editOption"
            row-key-field-name="rowKey"
            :row-style-option="rowStyleOption"
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
      cellAutofillOption: true,
      editOption: {
        // cell value change
        cellValueChange: ({ row, column }) => {},
      },
      columns: [
        {
          field: 'index',
          key: 'index',
          operationColumn: true,
          title: '',
          width: 50,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
          edit: true,
        },
        { field: 'col1', key: 'col1', title: 'Col1', edit: true },
        { field: 'col2', key: 'col2', title: 'Col2' },
        { field: 'col3', key: 'col3', title: 'Col3' },
        { field: 'col4', key: 'col4', title: 'Col4' },
        { field: 'col5', key: 'col5', title: 'Col5' },
        { field: 'col6', key: 'col6', title: 'Col6' },
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
      for (let i = 0; i < 8; i++) {
        data.push({
          rowKey: i,
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
  },
}
</script>
```

## 功能特性

### 操作列配置

- **operationColumn**: 设置为 `true` 标识为操作列
- **默认左固定**: 操作列默认固定在表格左侧
- **宽度设置**: 可以自定义操作列宽度
- **对齐方式**: 支持设置操作列的对齐方式

### 支持的操作

1. **行多选**: 支持选择多行数据
2. **整行复制**: 复制整行数据到剪贴板
3. **整行剪切**: 剪切整行数据
4. **行位置交换**: 交换行位置（开发中）

### 限制条件

- **不允许单元格选择**: 操作列单元格不支持选择功能
- **不允许单元格编辑**: 操作列单元格不支持编辑功能
- **不允许单元格自动填充**: 操作列单元格不支持自动填充功能

### 使用场景

- **数据管理**: 批量操作表格数据
- **Excel类似功能**: 提供类似Excel的操作列体验
- **行级操作**: 对整行数据进行操作
- **数据导出**: 配合复制功能实现数据导出

### 配置要点

1. **rowKeyFieldName**: 必须设置行键字段名
2. **editOption**: 配置编辑选项
3. **cellAutofillOption**: 配置自动填充选项
4. **rowStyleOption**: 配置行样式选项

### 最佳实践

- **合理设置宽度**: 根据操作按钮数量设置合适的列宽
- **统一操作风格**: 保持操作按钮的样式一致性
- **性能考虑**: 避免在操作列中放置过多复杂组件
- **用户体验**: 提供清晰的操作反馈和提示 