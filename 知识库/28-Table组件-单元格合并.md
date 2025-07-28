# Table组件-单元格合并

## 概述

Table组件支持单元格合并功能，可以通过配置实现行合并、列合并以及自定义合并内容。

## 核心功能

### 1. 基本配置

通过 `cellSpanOption` 属性设置合并单元格配置：

- `bodyCellSpan({row,column,rowIndex})` - 设置 body 单元格合并
- `footerCellSpan({row,column,rowIndex})` - 设置 footer 单元格合并

### 2. 合并参数

- `colspan` - 指定合并的列数
- `rowspan` - 指定合并的行数
- 设置 `colspan: 0, rowspan: 0` 表示该单元格不需要渲染

### 3. 自定义内容

结合 `renderBodyCell({row,column,rowIndex}, h)` 可以实现自定义合并后的内容。

## 使用示例

### 列合并示例

实现第2行 date 列和 hobby 列的合并：

```vue
<template>
  <fan-table 
    :columns="columns" 
    :table-data="tableData" 
    :border-around="true" 
    :border-x="true" 
    :border-y="true"
    :cell-span-option="cellSpanOption" 
  />
</template>

<script>
export default {
  data() {
    return {
      cellSpanOption: {
        bodyCellSpan: this.bodyCellSpan,
      },
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          width: 200,
          align: 'center',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          width: 200,
          align: 'left',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          width: 200,
          align: 'right',
        },
        { 
          field: 'address', 
          key: 'd', 
          title: 'Address', 
          width: '' 
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
  methods: {
    // body 单元格合并
    bodyCellSpan({ row, column, rowIndex }) {
      if (rowIndex === 1) {
        if (column.field === 'date') {
          return {
            rowspan: 1,
            colspan: 2,
          }
        } else if (column.field === 'hobby') {
          // 不需要渲染
          return {
            rowspan: 0,
            colspan: 0,
          }
        }
      }
    },
  },
}
</script>
```

### 行合并示例

实现 name 列第2行和第3行的合并：

```vue
<template>
  <fan-table 
    :columns="columns" 
    :table-data="tableData" 
    :border-around="true" 
    :border-x="true" 
    :border-y="true"
    :cell-span-option="cellSpanOption" 
  />
</template>

<script>
export default {
  data() {
    return {
      cellSpanOption: {
        bodyCellSpan: this.bodyCellSpan,
      },
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          width: 200,
          align: 'center',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          width: 200,
          align: 'left',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          width: 200,
          align: 'right',
        },
        { 
          field: 'address', 
          key: 'd', 
          title: 'Address', 
          width: '' 
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
  methods: {
    // body 单元格合并
    bodyCellSpan({ row, column, rowIndex }) {
      if (column.field === 'name') {
        if (rowIndex === 1) {
          return {
            rowspan: 2,
            colspan: 1,
          }
        } else if (rowIndex === 2) {
          // 不需要渲染
          return {
            rowspan: 0,
            colspan: 0,
          }
        }
      }
    },
  },
}
</script>
```

### Footer 列合并示例

实现 footer 汇总第1行 date 列和 hobby 列合并：

```vue
<template>
  <fan-table
    border-y
    fixed-header
    :max-height="300"
    :columns="columns"
    :table-data="tableData"
    :footer-data="footerData"
    row-key-field-name="rowKey"
    :cell-span-option="cellSpanOption"
  />
</template>

<script>
export default {
  data() {
    return {
      cellSpanOption: {
        footerCellSpan: this.footerCellSpan,
      },
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          width: 200,
          align: 'center',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          width: 200,
          align: 'left',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          width: 200,
          align: 'right',
        },
        { 
          field: 'address', 
          key: 'd', 
          title: 'Address', 
          width: '' 
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
      footerData: []
    }
  },
  created() {
    this.initTableData()
    this.initFooterData()
  },
  methods: {
    // footer 单元格合并
    footerCellSpan({ row, column, rowIndex }) {
      if (rowIndex === 0) {
        if (column.field === 'date') {
          return {
            rowspan: 1,
            colspan: 2,
          }
        } else if (column.field === 'hobby') {
          // 不需要渲染
          return {
            rowspan: 0,
            colspan: 0,
          }
        }
      }
    },
    initTableData() {
      const data = []
      for (let i = 0; i < 15; i++) {
        data.push({
          rowKey: i,
          name: i,
          date: i,
          hobby: i,
          address: i,
        })
      }
      this.tableData = data
    },
    initFooterData() {
      this.footerData = [
        {
          rowKey: 0,
          name: '平均值',
          date: 213,
          hobby: 355,
          address: 189,
        },
        {
          rowKey: 1,
          name: '汇总值',
          date: 1780,
          hobby: 890,
          address: 2988,
        },
      ]
    },
  },
}
</script>
```

### 自定义合并内容示例

结合 `renderBodyCell` 实现自定义合并后的内容：

```vue
<template>
  <fan-table 
    :columns="columns" 
    :table-data="tableData" 
    :border-around="true" 
    :border-x="true" 
    :border-y="true"
    :cell-span-option="cellSpanOption" 
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      cellSpanOption: {
        bodyCellSpan: this.bodyCellSpan,
      },
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          width: 200,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            if (column.field === 'name') {
              if (rowIndex === 1) {
                return (
                  <span style="color:#1890ff;">this is custom content</span>
                )
              }
            }
            return row[column.field]
          },
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          width: 200,
          align: 'left',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            if (rowIndex === 1) {
              if (column.field === 'date') {
                return (
                  <span style="color:#1890ff;">this is custom content</span>
                )
              }
            }
            return row[column.field]
          },
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          width: 200,
          align: 'right',
        },
        { 
          field: 'address', 
          key: 'd', 
          title: 'Address', 
          width: '' 
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
  methods: {
    // body 单元格合并
    bodyCellSpan({ row, column, rowIndex }) {
      // 列合并
      if (rowIndex === 1) {
        if (column.field === 'date') {
          return {
            rowspan: 1,
            colspan: 2,
          }
        } else if (column.field === 'hobby') {
          // 不需要渲染
          return {
            rowspan: 0,
            colspan: 0,
          }
        }
      }

      // 行合并
      if (column.field === 'name') {
        if (rowIndex === 1) {
          return {
            rowspan: 2,
            colspan: 1,
          }
        } else if (rowIndex === 2) {
          // 不需要渲染
          return {
            rowspan: 0,
            colspan: 0,
          }
        }
      }
    },
  },
}
</script>
```

## 重要说明

1. **合并逻辑**：通过 `bodyCellSpan` 或 `footerCellSpan` 方法返回合并配置
2. **隐藏单元格**：被合并的单元格需要设置 `rowspan: 0, colspan: 0` 来隐藏
3. **自定义内容**：可以通过 `renderBodyCell` 方法自定义合并后单元格的显示内容
4. **参数说明**：
   - `row` - 当前行数据
   - `column` - 当前列配置
   - `rowIndex` - 当前行索引

## 应用场景

- 数据汇总展示
- 相同内容的行或列合并
- 复杂表格布局
- 报表类数据展示