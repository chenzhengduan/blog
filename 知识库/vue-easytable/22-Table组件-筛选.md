# Table 组件-筛选

## 概述

1、通过 column 对象的 `filter` 属性设置筛选功能
2、`filterList` 设置你的筛选条件。包含 `label`、`value`、`selected` 3 个属性
3、`isMultiple` 开启筛选项多选，默认为 false
4、`filterConfirm` 筛选确认函数
5、`filterReset` 筛选重置函数

## 单条件筛选

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
    <fan-table :max-height="300" :fixed-header="true" :columns="columns" :table-data="tableData" />
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
          align: 'left',
          width: '15%',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
          // filter
          filter: {
            filterList: [
              {
                value: 0,
                label: '1900-05-20',
                selected: false,
              },
              {
                value: 1,
                label: '1910-06-20',
                selected: false,
              },
              {
                value: 2,
                label: '2000-07-20',
                selected: false,
              },
              {
                value: 3,
                label: '2010-08-20',
                selected: false,
              },
              {
                value: 4,
                label: '2020-09-20',
                selected: false,
              },
            ],
            // filter confirm hook
            filterConfirm: (filterList) => {
              const labels = filterList
                .filter((x) => x.selected)
                .map((x) => x.label)
              this.searchByDateField(labels)
            },
            // filter reset hook
            filterReset: (filterList) => {
              this.searchByDateField([])
            },
          },
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
        },
      ],
      // real table data
      tableData: [],
      // source data
      sourceData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
          rowKey: 3,
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
          rowKey: 4,
        },
      ],
    }
  },
  created() {
    this.tableData = this.sourceData.slice(0)
  },
  methods: {
    // search by date field
    searchByDateField(labels) {
      this.tableData = this.sourceData.filter(
        (x) => labels.length === 0 || labels.includes(x.date),
      )
    },
  },
}
</script>
```

## 多条件筛选

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

当筛选框内容很多时，`maxHeight` 属性设置筛选框的最大高度

```vue
<template>
    <fan-table :max-height="300" :fixed-header="true" :columns="columns" :table-data="tableData" />
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
          align: 'left',
          width: '15%',
          // filter
          filter: {
            filterList: [
              { value: 0, label: 'John', selected: false },
              {
                value: 1,
                label: 'Dickerson',
                selected: false,
              },
              { value: 2, label: 'Larsen', selected: false },
              { value: 3, label: 'Geneva', selected: false },
              { value: 4, label: 'Jami', selected: false },
            ],
            isMultiple: true,
            // filter confirm hook
            filterConfirm: (filterList) => {
              const labels = filterList
                .filter((x) => x.selected)
                .map((x) => x.label)
              this.searchByNameField(labels)
            },
            // filter reset hook
            filterReset: (filterList) => {
              this.searchByNameField([])
            },
            // max height
            maxHeight: 120,
          },
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
        },
      ],
      // real table data
      tableData: [],
      // source data
      sourceData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
          rowKey: 3,
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
          rowKey: 4,
        },
      ],
    }
  },
  created() {
    this.tableData = this.sourceData.slice(0)
  },
  methods: {
    // search by name field
    searchByNameField(labels) {
      this.tableData = this.sourceData.filter(
        (x) => labels.length === 0 || labels.includes(x.name),
      )
    },
  },
}
</script>
```

## 混合使用

根据不同的业务场景，任意搭配使用

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |

### 功能描述

你可以通过 `selected=true` 设置默认选中的项

```vue
<template>
    <fan-table :max-height="300" :fixed-header="true" :columns="columns" :table-data="tableData" />
</template>

<script>
// name fiter list
const NAME_FILTER_LIST = [
  { value: 0, label: 'John', selected: true },
  { value: 1, label: 'Dickerson', selected: true },
  { value: 2, label: 'Larsen', selected: false },
  { value: 3, label: 'Geneva', selected: true },
  { value: 4, label: 'Jami', selected: false },
]

// date fiter list
const DATE_FILTER_LIST = [
  { value: 0, label: '1900-05-20', selected: false },
  { value: 1, label: '1910-06-20', selected: false },
  { value: 2, label: '2000-07-20', selected: false },
  { value: 3, label: '2010-08-20', selected: false },
  { value: 4, label: '2020-09-20', selected: false },
]

export default {
  data() {
    return {
      // search data
      searchData: {
        names: [],
        date: '',
      },
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'left',
          width: '15%',
          // filter
          filter: {
            filterList: NAME_FILTER_LIST,
            isMultiple: true,
            // filter confirm hook
            filterConfirm: (filterList) => {
              const labels = filterList
                .filter((x) => x.selected)
                .map((x) => x.label)
              this.searchData.names = labels
            },
            // filter reset hook
            filterReset: (filterList) => {
              this.searchData.names = []
            },
          },
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
          // filter
          filter: {
            filterList: DATE_FILTER_LIST,
            // filter confirm hook
            filterConfirm: (filterList) => {
              const item = filterList.find((x) => x.selected)
              this.searchData.date = item ? item.label : ''
            },
            // filter reset hook
            filterReset: (filterList) => {
              this.searchData.date = ''
            },
          },
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
        },
      ],
      // real table data
      tableData: [],
      // source data
      sourceData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
          rowKey: 3,
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
          rowKey: 4,
        },
      ],
    }
  },
  watch: {
    searchData: {
      handler: function () {
        this.search()
      },
      immediate: true,
      deep: true,
    },
  },
  created() {
    // default search by names
    this.searchData.names = NAME_FILTER_LIST.filter((x) => x.selected).map((x) => x.label)
  },
  methods: {
    // search
    search() {
      const { names, date } = this.searchData
      this.tableData = this.sourceData.filter(
        (x) =>
          (date === '' || date === x.date) &&
                        (names.length === 0 || names.includes(x.name)),
      )
    },
  },
}
</script>
```

## 自定义图标

`filterIcon` 回调函数，支持返回自定义的 icon。此处使用了内置的 `<ve-icon name="search" />` 图标，你也可以其他图标库

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
  <fan-table :max-height="300" :fixed-header="true" :columns="columns" :table-data="tableData" />
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
          align: 'left',
          width: '15%',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
          // filter
          filter: {
            filterList: [
              {
                value: 0,
                label: '1900-05-20',
                selected: false,
              },
              {
                value: 1,
                label: '1910-06-20',
                selected: false,
              },
              {
                value: 2,
                label: '2000-07-20',
                selected: false,
              },
              {
                value: 3,
                label: '2010-08-20',
                selected: false,
              },
              {
                value: 4,
                label: '2020-09-20',
                selected: false,
              },
            ],
            // filter confirm hook
            filterConfirm: (filterList) => {
              const labels = filterList
                .filter((x) => x.selected)
                .map((x) => x.label)
              this.searchByDateField(labels)
            },
            // filter reset hook
            filterReset: (filterList) => {
              this.searchByDateField([])
            },
            // custom filter icon
            filterIcon: () => {
              return <ve-icon name="search" />
            },
          },
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
        },
      ],
      // real table data
      tableData: [],
      // source data
      sourceData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
          rowKey: 3,
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
          rowKey: 4,
        },
      ],
    }
  },
  created() {
    this.tableData = this.sourceData.slice(0)
  },
  methods: {
    // search by date field
    searchByDateField(labels) {
      this.tableData = this.sourceData.filter(
        (x) => labels.length === 0 || labels.includes(x.date),
      )
    },
  },
}
</script>
```

## API

### filter 筛选配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| filterList | 1、筛选条件。<br>2、包含label、value、selected 3 个属性，形如：`[{ value: 0, label: "1900-05-20", selected: false }]` | `Array` | - | - |
| isMultiple | 开启筛选项多选。 | `Boolean` | - | false |
| filterConfirm | 1、筛选确认函数。<br>2、接收1个参数，`filterList`：筛选条件 | `Function({filterList})` | - | - |
| filterReset | 1、筛选重置函数<br>2、接收1个参数，`filterList`：筛选条件 | `Function({filterList})` | - | - |
| filterIcon | 1、filter 自定义图标渲染函数。<br>2、参数信息。h：createElement 函数的别名 | `Function(h):VNode` | - | - |
| maxHeight | 1、筛选框的最大高度。不包含操作按钮的高度 | `Number` | - | 1000 |
| beforeVisibleChange({nextVisible}) | filter 面板显示或者隐藏之前的回调方法，返回`false`则阻止显示或隐藏 | `Function` | - | - | 