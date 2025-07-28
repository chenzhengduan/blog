# Table 组件-列隐藏

## 概述

1、通过 `columnHiddenOption` 实现列隐藏功能
2、你也可以通过实例方法控制列的隐藏与显示

## 默认隐藏列

下面示例默认隐藏 hobby 和 name 列

| Date | Address |
|------|---------|
| 1900-05-20 | No.1 Century Avenue, Shanghai |
| 1910-06-20 | No.1 Century Avenue, Beijing |
| 2000-07-20 | No.1 Century Avenue, Chongqing |
| 2010-08-20 | No.1 Century Avenue, Xiamen |
| 2020-09-20 | No.1 Century Avenue, Shenzhen |

### 功能描述

通过 `defaultHiddenColumnKeys` 属性设置默认隐藏的列

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :column-hidden-option="columnHiddenOption" />
</template>

<script>
export default {
  data() {
    return {
      columnHiddenOption: {
        // default hidden column keys
        defaultHiddenColumnKeys: ['hobby', 'name'],
      },
      columns: [
        { field: 'name', key: 'name', title: 'Name' },
        { field: 'date', key: 'date', title: 'Date' },
        { field: 'hobby', key: 'hobby', title: 'Hobby' },
        { field: 'address', key: 'address', title: 'Address' },
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

## 实例方法

| col1 | col2-col3 | col4-col5-col6 | col7 |
|------|-----------|----------------|------|
| col2 | col3 | col4-col5 | col6 |
| col4 | col5 | | |
| col1-0 | col2-0 | col3-0 | col4-0 | col5-0 | col6-0 | col7-0 |
| col1-1 | col2-1 | col3-1 | col4-1 | col5-1 | col6-1 | col7-1 |
| col1-2 | col2-2 | col3-2 | col4-2 | col5-2 | col6-2 | col7-2 |

### 功能描述

1、通过实例方法 `hideColumnsByKeys(keys)` 将列隐藏
2、通过实例方法 `showColumnsByKeys(keys)` 将隐藏的列显示

```vue
<template>
    <div>
        <button class="button-demo" @click="hideColumns(['col1'])">隐藏 col1 列</button>
        <button class="button-demo" @click="hideColumns(['col2'])">隐藏 col2 列</button>
        <button class="button-demo" @click="hideColumns(['col3'])">隐藏 col3 列</button>
        <button class="button-demo" @click="hideColumns(['col1','col2','col3'])">
            隐藏 col1、col2、col3 列
        </button>
        <br />
        <br />
        <button class="button-demo" @click="showColumns(['col1'])">显示 col1 列</button>
        <button class="button-demo" @click="showColumns(['col2'])">显示 col2 列</button>
        <button class="button-demo" @click="showColumns(['col3'])">显示 col3 列</button>
        <button class="button-demo" @click="showColumns(['col1','col2','col3'])">
            显示 col1、col2、col3 列
        </button>
        <br />
        <br />
        <fan-table
            ref="tableRef"
            border-y
            :columns="columns"
            :table-data="tableData"
            :column-hidden-option="columnHiddenOption"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      columnHiddenOption: {
        // default hidden column keys
        defaultHiddenColumnKeys: ['col8'],
      },
      columns: [
        { field: 'col1', key: 'col1', title: 'col1', width: '10%' },
        {
          title: 'col2-col3',
          children: [
            {
              field: 'col2',
              key: 'col2',
              title: 'col2',
              width: 100,
            },
            {
              field: 'col3',
              key: 'col3',
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
                  key: 'col4',
                  title: 'col4',
                  width: 130,
                },
                {
                  field: 'col5',
                  key: 'col5',
                  title: 'col5',
                  width: 140,
                },
              ],
            },
            {
              title: 'col6',
              field: 'col6',
              key: 'col6',
              width: 140,
            },
          ],
        },
        { field: 'col7', key: 'col7', title: 'col7', width: 150 },
        { field: 'col8', key: 'col8', title: 'col8', width: 160 },
      ],
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    // hidden columns
    hideColumns(keys) {
      this.$refs.tableRef.hideColumnsByKeys(keys)
    },
    // show cloumns
    showColumns(keys) {
      this.$refs.tableRef.showColumnsByKeys(keys)
    },
    initTableData() {
      const data = []
      for (let i = 0; i < 3; i++) {
        data.push({
          rowKey: i,
          col1: 'col1-' + i,
          col2: 'col2-' + i,
          col3: 'col3-' + i,
          col4: 'col4-' + i,
          col5: 'col5-' + i,
          col6: 'col6-' + i,
          col7: 'col7-' + i,
          col8: 'col8-' + i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## API

### columnHidden 列隐藏配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultHiddenColumnKeys | `v2.11.0` 设置默认隐藏的列 | `Array` | - | - | 