# Table 组件列固定

## 概述

1、属性 `scroll-width` 为滚动区域的宽度
2、当外层容器宽度小于 `scroll-width` 值时，将会出现横向滚动条；当外层容器宽度大于 `scroll-width` 值时，将会跟随容器自适应；当 `scroll-width=0` 时，滚动条将根据你的列宽度决定
3、列宽可以不设置、或者设置为百分比、或者为像素值（px）
4、设置了 `scroll-width` 属性，列宽单位强烈建议保持一致！

## 左列固定

| Title1 | Title2 | Title3 | Title4 | Title5 | Title6 | Title7 | Title8 | Title9 | Title10 |
|--------|--------|--------|--------|--------|--------|--------|--------|--------|---------|
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |

### 功能描述

1、通过 `scroll-width="1200"` 设置滚动区域宽度，通过 `style="width:900px"` 设置外层容器宽度
2、通过 `fixed:"left"` 设置需要固定的左列

```vue
<template>
    <fan-table
        style="width:900px"
        :scroll-width="1200"
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
        { field: 'col1', key: 'a', title: 'Title1', fixed: 'left' },
        { field: 'col2', key: 'b', title: 'Title2', fixed: 'left' },
        { field: 'col3', key: 'c', title: 'Title3' },
        { field: 'col4', key: 'd', title: 'Title4' },
        { field: 'col5', key: 'e', title: 'Title5' },
        { field: 'col6', key: 'f', title: 'Title6' },
        { field: 'col7', key: 'g', title: 'Title7' },
        { field: 'col8', key: 'h', title: 'Title8' },
        { field: 'col9', key: 'i', title: 'Title9' },
        { field: 'col10', key: 'j', title: 'Title10' },
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
        },
      ],
    }
  },
}
</script>
```

## 右列固定

| Title1 | Title2 | Title3 | Title4 | Title5 | Title6 | Title7 | Title8 | Title9 | Title10 |
|--------|--------|--------|--------|--------|--------|--------|--------|--------|---------|
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |

### 功能描述

1、通过 `scroll-width="1200"` 设置滚动区域宽度，通过 `style="width:900px"` 设置外层容器宽度
2、通过 `fixed:"right"` 设置需要固定的右列

```vue
<template>
    <fan-table
        style="width:900px"
        :scroll-width="1200"
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
        { field: 'col1', key: 'a', title: 'Title1' },
        { field: 'col2', key: 'b', title: 'Title2' },
        { field: 'col3', key: 'c', title: 'Title3' },
        { field: 'col4', key: 'd', title: 'Title4' },
        { field: 'col5', key: 'e', title: 'Title5' },
        { field: 'col6', key: 'f', title: 'Title6' },
        { field: 'col7', key: 'g', title: 'Title7' },
        { field: 'col8', key: 'h', title: 'Title8' },
        {
          field: 'col9',
          key: 'i',
          title: 'Title9',
          fixed: 'right',
        },
        {
          field: 'col10',
          key: 'j',
          title: 'Title10',
          fixed: 'right',
        },
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
        },
      ],
    }
  },
}
</script>
```

## 左右列固定

| Title1 | Title2 | Title3 | Title4 | Title5 | Title6 | Title7 | Title8 | Title9 | Title10 |
|--------|--------|--------|--------|--------|--------|--------|--------|--------|---------|
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |

### 功能描述

1、通过 `scroll-width="1200"` 设置滚动区域宽度，通过 `style="width:900px"` 设置外层容器宽度
2、通过 `fixed:"left"` 设置需要固定的左列;通过 `fixed:"right"` 设置需要固定的右列

```vue
<template>
    <fan-table
        style="width:900px"
        :scroll-width="1200"
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
        { field: 'col1', key: 'a', title: 'Title1', fixed: 'left' },
        { field: 'col2', key: 'b', title: 'Title2', fixed: 'left' },
        { field: 'col3', key: 'c', title: 'Title3' },
        { field: 'col4', key: 'd', title: 'Title4' },
        { field: 'col5', key: 'e', title: 'Title5' },
        { field: 'col6', key: 'f', title: 'Title6' },
        { field: 'col7', key: 'g', title: 'Title7' },
        { field: 'col8', key: 'h', title: 'Title8' },
        {
          field: 'col9',
          key: 'i',
          title: 'Title9',
          fixed: 'right',
        },
        {
          field: 'col10',
          key: 'j',
          title: 'Title10',
          fixed: 'right',
        },
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
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
          col9: '9',
          col10: '10',
        },
      ],
    }
  },
}
</script>
```

## '容器自适应'列固定

### 功能描述

1、通过 `scroll-width="1600"` 设置滚动区域宽度
2、不设置外层容器宽度。等同于设置 `style="width:100%"`
3、改变浏览器宽度试试看。当容器宽度小于 `scroll-width` 时，出滚动条；大于 `scroll-width` 时，将会跟随容器自适应

```vue
<template>
  <fan-table :scroll-width="1600" border-y :columns="columns" :table-data="tableData" row-key-field-name="rowKey" />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        { field: 'col1', key: 'a', title: 'Title1', fixed: 'left' },
        { field: 'col2', key: 'b', title: 'Title2', fixed: 'left' },
        { field: 'col3', key: 'c', title: 'Title3' },
        { field: 'col4', key: 'd', title: 'Title4' },
        { field: 'col5', key: 'e', title: 'Title5' },
        { field: 'col6', key: 'f', title: 'Title6' },
        { field: 'col7', key: 'g', title: 'Title7' },
        { field: 'col8', key: 'h', title: 'Title8' },
        { field: 'col9', key: 'i', title: 'Title9', fixed: 'right', },
        { field: 'col10', key: 'j', title: 'Title10', fixed: 'right', },
      ],
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
          col9: i,
          col10: i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
``` 