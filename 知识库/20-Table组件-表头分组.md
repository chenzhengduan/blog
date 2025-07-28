# Table 组件-表头分组

## 概述

1、通过设置 `columns` 的 `children` 属性，即可实现表头分组功能
2、`children` 属性指定需要合并的列
3、表头分组功能必须指定列的 `key` 属性！！
4、当需要固定分组的列时，只需要将 `fixed` 属性设置在顶层配置中即可

## 表头分组

| col1 | col2-col3 | col4-col5-col6 | col7 | col8 |
|------|-----------|----------------|------|------|
| col2 | col3 | col4-col5 | col6 | |
| col4 | col5 | | | |
| 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 |
| 2 | 2 | 2 | 2 | 2 | 2 | 2 | 2 |
| 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
| 4 | 4 | 4 | 4 | 4 | 4 | 4 | 4 |
| 5 | 5 | 5 | 5 | 5 | 5 | 5 | 5 |

### 功能描述

1、通过设置 `children` 指定当前合并的列

```vue
<template>
    <fan-table border-y :columns="columns" :table-data="tableData" />
</template>

<script>
export default {
  data() {
    return {
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 6; i++) {
        data.push({
          rowKey: i,
          col1: i,
          col2: i,
          col3: i,
          col4: i,
          col5: i,
          col6: i,
          col7: i,
          col8: i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 表头分组结合固定列

| col1 | col2-col3 | col4-col5-col6 | col7 | col8 |
|------|-----------|----------------|------|------|
| col2 | col3 | col4-col5 | col6 | col7-1 |
| col4 | col5 | | | |
| 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 |
| 2 | 2 | 2 | 2 | 2 | 2 | 2 | 2 |
| 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
| 4 | 4 | 4 | 4 | 4 | 4 | 4 | 4 |
| 5 | 5 | 5 | 5 | 5 | 5 | 5 | 5 |
| 6 | 6 | 6 | 6 | 6 | 6 | 6 | 6 |
| 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 |
| 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 |
| 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 |

### 功能描述

1、通过 `fixed:"left"` 设置固定左列
2、固定列只需指定到顶层列即可

```vue
<template>
    <fan-table
        fixed-header
        :scroll-width="1600"
        :max-height="380"
        border-y
        :columns="columns"
        :table-data="tableData"
    />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'col1',
          key: 'a',
          title: 'col1',
          width: 50,
          fixed: 'left',
        },
        {
          title: 'col2-col3',
          fixed: 'left',
          children: [
            {
              field: 'col2',
              key: 'b',
              title: 'col2',
              width: 50,
            },
            {
              field: 'col3',
              key: 'c',
              title: 'col3',
              width: 50,
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
        {
          title: 'col7',
          fixed: 'right',
          children: [
            {
              title: 'col7-1',
              field: 'col7',
              key: 'g',
              width: 50,
            },
          ],
        },
        {
          field: 'col8',
          key: 'h',
          title: 'col8',
          width: 50,
          fixed: 'right',
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
      for (let i = 0; i < 10; i++) {
        data.push({
          rowKey: i,
          col1: i,
          col2: i,
          col3: i,
          col4: i,
          col5: i,
          col6: i,
          col7: i,
          col8: i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
``` 