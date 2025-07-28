# Table 组件-单元格样式

## 概述

1、通过配置对象 `cellStyleOption` 设置单元格的样式
2、回调函数属性 `bodyCellClass({ row, column, rowIndex })` 设置符合条件的表体单元格 class
3、回调函数属性 `headerCellClass({column, rowIndex})` 设置符合条件的表头单元格 class
4、回调函数属性 `footerCellClass({row,column, rowIndex})` 设置符合条件的 footer 单元格 class
5、`<style>` 标签不可以使用 `scoped` 属性
6、当然你也可以通过 `renderBodyCell`、`renderHeaderCell`、`renderFooterCell` 等实现单元格样式的自定义功能

## 表体单元格样式

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、回调函数 `bodyCellClass({ row, column, rowIndex })` 接收 3 个参数，`row`：当前行数据、`column`：当前列配置、`rowIndex`：行索引
2、将符合条件的单元格返回指定的 class 名称。class 名称自定义
3、如果需要给单元格设置背景色或字体颜色需要加上 `!important`

```vue
<template>
  <fan-table :columns="columns" :table-data="tableData" :cell-style-option="cellStyleOption" />
</template>

<style>
.table-body-cell-class1 {
  background: #91d5ff !important;
  color: #fff !important;
}

.table-body-cell-class2 {
  background: orange !important;
  color: #fff !important;
}
</style>

<script>
export default {
  data() {
    return {
      cellStyleOption: {
        bodyCellClass: ({ row, column, rowIndex }) => {
          if (column.field === 'hobby') {
            return 'table-body-cell-class1'
          }

          if (column.field === 'name' && rowIndex === 2) {
            return 'table-body-cell-class2'
          }
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'left' },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          width: '',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding',
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
}
</script>
```

## 表体行样式

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、行样式，仍然可以通过 `bodyCellClass({ row, column, rowIndex })` 实现

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :cell-style-option="cellStyleOption" />
</template>

<style>
    .table-body-cell-class {
        background: #91d5ff !important;
        color: #fff !important;
    }
</style>

<script>
export default {
  data() {
    return {
      cellStyleOption: {
        bodyCellClass: ({ row, column, rowIndex }) => {
          if (rowIndex === 2) {
            return 'table-body-cell-class'
          }
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'left' },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          width: '',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding',
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
}
</script>
```

## 表头单元格样式

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、回调函数 `headerCellClass({ column, rowIndex })` 接收 2 个参数，`column`：当前列配置、`rowIndex`：表头行索引
2、将符合条件的单元格返回指定的 class 名称。class 名称自定义
3、如果需要给单元格设置背景色或字体颜色需要加上 `!important`

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :cell-style-option="cellStyleOption" />
</template>

<style>
    .table-header-cell-class {
        background: orange !important;
        color: #fff !important;
    }
</style>

<script>
export default {
  data() {
    return {
      cellStyleOption: {
        headerCellClass: ({ column, rowIndex }) => {
          if (column.field === 'hobby') {
            return 'table-header-cell-class'
          }
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'left' },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          width: '',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding',
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
}
</script>
```

## 表头行样式

| col1 | col2-col3 | col4-col5-col6 | col7 | col8 |
|------|-----------|----------------|------|------|
| col2 | col3 | col4-col5 | col6 | |
| col4 | col5 | | | |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |

### 功能描述

1、表头行样式，仍然可以通过 `headerCellClass({ column, rowIndex })` 实现

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :cell-style-option="cellStyleOption" />
</template>

<style>
    .table-header-cell-class {
        background: #91d5ff !important;
        color: #fff !important;
    }
</style>

<script>
export default {
  data() {
    return {
      cellStyleOption: {
        headerCellClass: ({ column, rowIndex }) => {
          if (rowIndex === 0) {
            return 'table-header-cell-class'
          }
        },
      },
      columns: [
        { field: 'col1', key: 'a', title: 'col1', width: '10%' },
        {
          title: 'col2-col3',
          children: [
            {
              field: 'col2',
              key: 'b',
              title: 'col2',
              width: 100,
            },
            {
              field: 'col3',
              key: 'c',
              title: 'col3',
              width: 110,
            },
          ],
        },
        {
          title: 'col4-col5-col6',
          children: [
            {
              title: 'col4-col5',
              children: [
                {
                  field: 'col4',
                  key: 'd',
                  title: 'col4',
                  width: 130,
                },
                {
                  field: 'col5',
                  key: 'e',
                  title: 'col5',
                  width: 140,
                },
              ],
            },
            {
              title: 'col6',
              field: 'col6',
              key: 'f',
              width: 140,
            },
          ],
        },
        { field: 'col7', key: 'g', title: 'col7', width: 150 },
        { field: 'col8', key: 'h', title: 'col8', width: 160 },
      ],
      tableData: [
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
          col6: '6',
          col7: '7',
          col8: '8',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
          col6: '6',
          col7: '7',
          col8: '8',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
          col6: '6',
          col7: '7',
          col8: '8',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
          col6: '6',
          col7: '7',
          col8: '8',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
          col6: '6',
          col7: '7',
          col8: '8',
        },
      ],
    }
  },
}
</script>
```

## footer 单元格样式

### 功能描述

1、回调函数 `footerCellClass({ row, column, rowIndex })` 接收 3 个参数，`row`：当前行数据、`column`：当前列配置、`rowIndex`：行索引
2、将符合条件的单元格返回指定的 class 名称。class 名称自定义
3、如果需要给单元格设置背景色或字体颜色需要加上 `!important`

```vue
<template>
    <fan-table border-y fixed-header :max-height="300" :columns="columns" :table-data="tableData" :footer-data="footerData"
        :cell-style-option="cellStyleOption" row-key-field-name="rowKey" />
</template>

<style>
.table-footer-cell-class1 {
    background: #91d5ff !important;
    color: #fff !important;
}

.table-footer-cell-class2 {
    background: orange !important;
    color: #fff !important;
}
</style>

<script>
export default {
  data() {
    return {
      cellStyleOption: {
        footerCellClass: ({ row, column, rowIndex }) => {
          if (column.field === 'address') {
            return 'table-footer-cell-class1'
          }

          if (column.field === 'date' && rowIndex === 1) {
            return 'table-footer-cell-class2'
          }
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'left' },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          width: '',
          align: 'left',
        },
      ],
      tableData: [],
    }
  },
  created() {
    this.initTableData()
    this.initFooterData()
  },
  methods: {
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

## API

### cellStyleOption 单元格样式配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| bodyCellClass | 1、表体单元格样式<br>2、接收3个参数，`row`:当前行数据、`column`:当前列配置、`rowIndex`:行索引 | `Function({row,column,rowIndex})` | - | - |
| headerCellClass | 1、表头单元格样式<br>2、接收2个参数，`column`:当前列配置、`rowIndex`:行索引 | `Function({column,rowIndex})` | - | - |
| footerCellClass | 1、footer汇总 单元格样式<br>2、接收3个参数，`row`:当前行数据、`column`:当前列配置、`rowIndex`:行索引 | `Function({row,column,rowIndex})` | - | - |

### 使用说明

1. **样式优先级**：设置背景色或字体颜色需要加上 `!important`
2. **样式作用域**：`<style>` 标签不可以使用 `scoped` 属性
3. **回调参数**：根据不同的单元格类型，回调函数接收不同的参数
4. **条件判断**：可以根据行数据、列配置、行索引等条件设置样式
5. **自定义样式**：也可以通过 `renderBodyCell`、`renderHeaderCell`、`renderFooterCell` 等实现更复杂的样式

### 适用场景

- **数据高亮**：根据数据值设置不同的背景色
- **状态标识**：通过颜色区分不同的状态
- **重点突出**：突出显示重要的数据行或列
- **视觉层次**：通过样式建立表格的视觉层次
- **品牌定制**：根据品牌色彩定制表格样式

### 最佳实践

- **样式命名**：使用有意义的样式类名
- **条件逻辑**：合理使用条件判断，避免过度复杂
- **性能考虑**：避免在回调函数中进行复杂计算
- **样式复用**：合理复用样式类，减少重复代码
- **响应式设计**：考虑不同屏幕尺寸下的样式表现 