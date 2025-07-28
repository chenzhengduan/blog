# Table 组件-表格高度

## 概述

1、表格高度默认由行数据决定，也可以通过 max-height属性设置最大高度
2、表格高度可以设置固定值。如：`:max-height="500"`
3、表格高度可以设置动态值。如：`max-height="calc(100vh - 210px)"` 或者 `max-height="80%"`

## 表格高度自适应

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| 0 | 0 | 0 | 0 |
| 1 | 1 | 1 | 1 |

### 功能描述

当不设置表格高度时，表格高度根据内容撑开

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'center' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'center' },
        { field: 'address', key: 'd', title: 'Address', align: 'left' },
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
      for (let i = 0; i < 2; i++) {
        data.push({
          name: i,
          date: i,
          hobby: i,
          address: i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 表格高度固定

| Name | Tel | Hobby | Address |
|------|-----|-------|---------|
| 0 | 0 | 0 | 0 |
| 1 | 1 | 1 | 1 |
| 2 | 2 | 2 | 2 |
| 3 | 3 | 3 | 3 |
| 4 | 4 | 4 | 4 |
| 5 | 5 | 5 | 5 |
| 6 | 6 | 6 | 6 |
| 7 | 7 | 7 | 7 |
| 8 | 8 | 8 | 8 |
| 9 | 9 | 9 | 9 |
| 10 | 10 | 10 | 10 |
| 11 | 11 | 11 | 11 |
| 12 | 12 | 12 | 12 |
| 13 | 13 | 13 | 13 |
| 14 | 14 | 14 | 14 |
| 15 | 15 | 15 | 15 |
| 16 | 16 | 16 | 16 |
| 17 | 17 | 17 | 17 |
| 18 | 18 | 18 | 18 |
| 19 | 19 | 19 | 19 |

```vue
<template>
    <fan-table
        :max-height="300"
        :columns="columns"
        :table-data="tableData"
        :border-around="true"
        :border-x="true"
        :border-y="true"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        { field: 'name', key: 'a', title: 'Name', width: 100 },
        { field: 'date', key: 'b', title: 'Tel', width: 200 },
        { field: 'hobby', key: 'c', title: 'Hobby', width: 300 },
        { field: 'address', key: 'd', title: 'Address', width: 300 },
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
      for (let i = 0; i < 20; i++) {
        data.push({
          name: i,
          date: i,
          hobby: i,
          address: i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 表格动态高度（calc css 函数）

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| 0 | 0 | 0 | 0 |
| 1 | 1 | 1 | 1 |
| 2 | 2 | 2 | 2 |
| 3 | 3 | 3 | 3 |
| 4 | 4 | 4 | 4 |
| 5 | 5 | 5 | 5 |
| 6 | 6 | 6 | 6 |
| 7 | 7 | 7 | 7 |
| 8 | 8 | 8 | 8 |
| 9 | 9 | 9 | 9 |
| 10 | 10 | 10 | 10 |
| 11 | 11 | 11 | 11 |
| 12 | 12 | 12 | 12 |
| 13 | 13 | 13 | 13 |
| 14 | 14 | 14 | 14 |
| 15 | 15 | 15 | 15 |
| 16 | 16 | 16 | 16 |
| 17 | 17 | 17 | 17 |
| 18 | 18 | 18 | 18 |
| 19 | 19 | 19 | 19 |

### 功能描述

1、你可以使用 calc css 函数 实现表格动态高度
2、试试改变浏览器宽度查看效果

```vue
<template>
    <fan-table
        max-height="calc(100vh - 350px)"
        fixed-header
        :columns="columns"
        :table-data="tableData"
    />
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
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 20; i++) {
        data.push({
          name: i,
          date: i,
          hobby: i,
          address: i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
``` 