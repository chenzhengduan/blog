# Table 组件-单元格自定义

## 自定义列

### body 自定义单元格

| # | Date | Hobby | Address | Action |
|---|------|-------|---------|--------|
| 1 | 1900-05-20 | coding are my hobbies | No.1 Century Avenue, Shanghai | |
| 2 | 1910-06-20 | coding are my hobbies | No.1 Century Avenue, Beijing | |
| 3 | 2000-07-20 | coding and coding repeat are my hobbies | No.1 Century Avenue, Chongqing | |
| 4 | 2010-08-20 | coding and coding repeat are my hobbies | No.1 Century Avenue, Xiamen | |
| 5 | 2020-09-20 | coding and coding repeat are my hobbies | No.1 Century Avenue, Shenzhen | |

#### 功能描述

1、column配置中，支持通过属性 `renderBodyCell({row,column,rowIndex},h)` 传入渲染函数，此处为 jsx 语法,书写和模板语法接近。更多 jsx 知识请参考Vue.js 考官方文档
2、渲染函数接收三个参数，`row`:当前行数据、`column`:当前列配置、`rowIndex`:行索引

```vue
<template>
  <fan-table style="width:100%" border-y :columns="columns" :table-data="tableData" />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      columns: [
        {
          field: '',
          key: 'a',
          title: '#',
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (
              <span class="text-bold" style="color:#1890ff;">
                {++rowIndex}
              </span>
            )
          },
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'center',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'left',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            const text = row[column.field]
            return (
              <span>
                <a
                  class="test-text"
                  href="http://github.com"
                  style="color:#1890ff;cursor:pointer;"
                >
                  {text}
                </a>
                &nbsp;are my hobbies
              </span>
            )
          },
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          width: '',
          align: 'left',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            const text = row[column.field]
            return <input style="width:100%" value={text}></input>
          },
        },
        {
          field: '',
          key: 'e',
          title: 'Action',
          width: '',
          center: 'left',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (
              <span>
                <el-button onClick={() => this.editRow(rowIndex)}>
                  Edit
                </el-button>
                &nbsp;
                <el-button onClick={() => this.deleteRow(rowIndex)}>
                  Delete
                </el-button>
              </span>
            )
          },
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
  methods: {
    editRow(rowIndex) {
      alert(`eidt row number:${rowIndex}`)
    },
    deleteRow(rowIndex) {
      this.tableData.splice(rowIndex, 1)
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

### header 自定义单元格

| 请输入名称关键字 | Date | Hobby | Address |
|------------------|------|-------|---------|
| John | 1900-05-20 | coding | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

#### 功能描述

1、column配置中，支持通过属性 `renderHeaderCell` 传入渲染函数，与 body 自定义单元格用法一致。`renderHeaderCell` 在表头分组中一样适用
2、渲染函数接收一个参数，`column`:当前列配置

```vue
<template>
  <fan-table style="width:100%" border-y :columns="columns"
    :table-data="tableData.filter(item => !searchText || item.name.toLowerCase().includes(searchText.toLowerCase()))" />
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
          width: 200,
          align: 'center',
          renderHeaderCell: ({ column }, h) => {
            return (
              <input
                // jsx 不能直接使用 v-model。此处为 jsx 实现 v-model，了解更多查看官方文档
                value={this.searchText}
                onInput={this.searchInputChange}
                style="width:90%"
                placeholder="请输入名称关键字"
              />
            )
          },
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          width: 200,
          align: 'center',
          renderHeaderCell: ({ column }, h) => {
            return (
              <span class="text-bold" style="color:#1890ff;">
                {column.title}
              </span>
            )
          },
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
      // 搜索
      searchText: '',
    }
  },
  methods: {
    // search input change event
    searchInputChange(e) {
      this.searchText = e.target.value
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

### footer 汇总自定义单元格

#### 功能描述

1、column 配置中，支持通过属性 `renderFooterCell` 传入渲染函数，与 body 自定义单元格用法一致。
2、渲染函数接收三个参数，`row`:当前行数据、`column`:当前列配置、`rowIndex`:行索引

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

## 通过外部组件自定义

1、 当然你也可以通过外部组件自定义单元格内容，此示例是在组件内部定义了一个OtherComp组件，你也可以通过 import关键字导入一个组件作为自定义组件
2、 `renderBodyCell`、`renderHeaderCell`等一样适用

| Row Number | Date | Hobby | Address |
|-------------|------|-------|---------|
| row index: 0 | 1900-05-20 | coding | No.1 Century Avenue, Shanghai |
| row index: 1 | 1910-06-20 | coding | No.1 Century Avenue, Beijing |
| row index: 2 | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| row index: 3 | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| row index: 4 | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

通过外部组件自定义

```vue
<template>
  <fan-table style="width:100%" border-y :columns="columns" :table-data="tableData" />
</template>

<script lang="jsx">
// 此示例是在组件内部定义了一个子组件。你当然也可以通过 `import`关键字导入一个组件
const OtherComp = {
  name: 'OtherComp',
  template: `
    <div class="other-comp">
      <span style="color:#1890ff;">row index: {{rowIndex}}</span>
    </div>
    `,
  props: {
    row: Object,
    column: Object,
    rowIndex: Number,
  },
}

export default {
  data() {
    return {
      columns: [
        {
          field: '',
          key: 'a',
          title: 'Row Number',
          width: 200,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return <OtherComp row={row} column={column} rowIndex={rowIndex} />
          },
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          width: 200,
          align: 'center',
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
  methods: {
    editRow(rowIndex) {
      alert(`eidt row number:${rowIndex}`)
    },
    deleteRow(rowIndex) {
      this.tableData.splice(rowIndex, 1)
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