# Table 组件-footer 汇总

## 概述

1、footer 汇总，允许对表格数据进行汇总展示
2、footerData为 footer 汇总数据。数据结构与tableData保持一致

## footer 基础功能

### 功能描述

1、默认汇总数据固定在底部

```vue
<template>
  <fan-table border-y fixed-header :max-height="300" :columns="columns" :table-data="tableData" :footer-data="footerData"
    row-key-field-name="rowKey" />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'center' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
        },
      ],
      tableData: [],
      footerData: [],
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

## footer 汇总自定义单元格

### 功能描述

1、column 配置中，支持通过属性 renderFooterCell 传入渲染函数，与 body 自定义单元格用法一致。
2、渲染函数接收三个参数，row:当前行数据、column:当前列配置、rowIndex:行索引

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
    />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'center',
          renderFooterCell: ({ row, column, rowIndex }, h) => {
            return (
              <span class="text-bold" style="">
                {row.name}
              </span>
            )
          },
        },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
        },
      ],
      tableData: [],
      footerData: [],
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

<style>
    .text-bold {
        font-weight: bold;
    }
</style>
```

## footer 单元格样式

### 功能描述

1、回调函数footerCellClass({ row, column, rowIndex })接收 3 个参数，row：当前行数据、column：当前列配置、rowIndex：行索引
2、将符合条件的单元格返回指定的 class 名称。class 名称自定义
3、如果需要给单元格设置背景色或字体颜色需要加上!important

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

## footer 列合并

### 功能描述

设置 footer 汇总第 1 行date列和hoby列合并。 同时需要指定第 2 行hoby列不渲染

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
        { field: 'address', key: 'd', title: 'Address', width: '' },
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
  created() {
    this.initTableData()
    this.initFooterData()
  },
  methods: {
    // footer cell span
    footerCellSpan({ row, column, rowIndex }) {
      if (rowIndex === 0) {
        if (column.field === 'date') {
          return {
            rowspan: 1,
            colspan: 2,
          }
        } else if (column.field === 'hobby') {
          // does not need to be rendered
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

## footer 行事件自定义

打开F12 查看 console 信息

## footer 列事件自定义

打开F12 查看 console 信息

```vue
<template>
    <div>
        打开F12 查看 console 信息
        <fan-table border-y fixed-header :max-height="300" :columns="columns" :table-data="tableData"
            :footer-data="footerData" row-key-field-name="rowKey" :event-custom-option="eventCustomOption" />
    </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        footerCellEvents: ({ row, column, rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', row, column, rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', row, column, rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', row, column, rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', row, column, rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', row, column, rowIndex, event)
            },
          }
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          width: 300,
          align: 'left',
        },
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

## footer 跟随

### 功能描述

1、设置了 maxHeight 属性后，footer 汇总会固定显示。如果想让汇总信息跟随表格行数据，可以设置 fixedFooter=false

```vue
<template>
    <fan-table
        border-y
        fixed-header
        :max-height="300"
        :columns="columns"
        :table-data="tableData"
        :footer-data="footerData"
        :fixed-footer="false"
        row-key-field-name="rowKey"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'center',
          width: 200,
        },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: 200,
        },
      ],
      tableData: [],
      footerData: [],
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

## footer 结合固定列

### 功能描述

1、表格设置了固定列，footer 汇总自动支持，无需额外配置

```vue
<template>
    <fan-table
        border-y
        fixed-header
        :max-height="300"
        style="width:900px"
        :scroll-width="1200"
        :columns="columns"
        :table-data="tableData"
        :footer-data="footerData"
        row-key-field-name="rowKey"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'center',
          fixed: 'left',
          width: 200,
        },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          fixed: 'right',
          width: 200,
        },
      ],
      tableData: [],
      footerData: [],
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

## footer 结合虚拟滚动

### 功能描述

1、表格设置了虚拟滚动，footer 汇总自动支持，无需额外配置

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :columns="columns"
      :table-data="tableData"
      :footer-data="footerData"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },

      columns: [
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          width: 300,
          align: 'left',
        },
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data

      this.footerData = [
        {
          rowKey: 0,
          name: '平均值',
          date: 1100,
          hobby: 1200,
          address: 1300,
        },
        {
          rowKey: 1,
          name: '汇总值',
          date: 701000,
          hobby: 801000,
          address: 801000,
        },
      ]
    },
  },
}
</script>
```

## 功能特性

- **数据汇总**: 支持对表格数据进行汇总展示
- **自定义渲染**: 通过 `renderFooterCell` 自定义单元格内容
- **样式定制**: 支持通过 `footerCellClass` 自定义单元格样式
- **列合并**: 支持 footer 单元格的合并显示
- **事件处理**: 支持 footer 单元格的点击、双击等事件
- **固定显示**: 默认固定在底部，可设置为跟随滚动
- **兼容性**: 自动支持固定列和虚拟滚动

## 使用场景

- **数据统计**: 显示平均值、总和等统计信息
- **财务表格**: 显示合计、小计等财务数据
- **报表展示**: 在表格底部显示汇总信息
- **数据分析**: 提供数据的整体概览

## 最佳实践

- **数据结构**: 确保 footerData 与 tableData 结构一致
- **样式设计**: 合理设计汇总行的样式，突出显示重要信息
- **性能优化**: 在大量数据时注意汇总计算的性能
- **用户体验**: 根据实际需求选择固定或跟随的显示方式 