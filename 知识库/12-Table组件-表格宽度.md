# Table 组件-表格宽度

## 概述

1、表格宽度可以设置固定值。如：`style="width:900px;"`
2、表格宽度可以设置动态值。如：`style="width:calc(100vh - 210px)"` 或者 `style="width:80%"`

## 表格自动宽度

| Name | Tel | Hobby | Address |
|------|-----|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

如果不设置表格宽度，等同于`style="width:100%;"`

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" />
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
}
</script>
```

## 表格宽度固定

| Name | Tel | Hobby | Address |
|------|-----|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

表格的固定宽度，需要设置外层容器宽度。可以通过`style="width:900px"`方式设置。此处容器宽度为 900px

```vue
<template>
    <fan-table style="width:900px;" :columns="columns" :table-data="tableData" />
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
}
</script>
```

## 表格动态宽度（calc css 函数）

| Name | Tel | Hobby | Address |
|------|-----|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、你可以使用 calc css 函数 实现表格动态宽度
2、试试改变浏览器宽度查看效果

```vue
<template>
    <fan-table style="width:calc(55vw - 10px);" :columns="columns" :table-data="tableData" />
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
}
</script>
```

## 表格动态宽度（百分比）

| Name | Tel | Hobby | Address |
|------|-----|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、你可以使用百分比实现表格动态宽度
2、试试改变浏览器宽度查看效果

```vue
<template>
    <fan-table style="width:80%" :columns="columns" :table-data="tableData" />
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
}
</script>
``` 