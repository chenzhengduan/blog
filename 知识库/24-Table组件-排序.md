# Table 组件-排序

## 概述

1、通过 `sortBy` 属性，设置需要排序的列。`sortBy="asc"`：默认当前列升序；`sortBy="desc"`：默认当前列降序；`sortBy=""`：允许排序但无排序规则
2、通过 `sortOption` 对象，设置更多排序功能。排序功能需要配合 `sortChange(param)` 回调函数实现，回调参数包含列的排序规则

## 单字段排序

| Name | Age | Weight(kg) | Hobby | Address |
|------|-----|------------|-------|---------|
| John | 25 | 66 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 20 | 70 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 18 | 65 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 17 | 80 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 26 | 72 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、默认为单字段排序
2、`sortChange(params)` 回调函数接收列的排序规则

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :sort-option="sortOption" border-y />
</template>

<script>
export default {
  data() {
    return {
      sortOption: {
        sortChange: (params) => {
          console.log('sortChange::', params)
          this.sortChange(params)
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        {
          field: 'age',
          key: 'b',
          title: 'Age',
          align: 'center',
          sortBy: '',
        },
        {
          field: 'weight',
          key: 'c',
          title: 'Weight(kg)',
          align: 'center',
          sortBy: 'asc',
        },
        {
          field: 'hobby',
          key: 'd',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'e',
          title: 'Address',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          age: 25,
          weight: 66,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          age: 20,
          weight: 70,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          age: 18,
          weight: 65,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          age: 17,
          weight: 80,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          age: 26,
          weight: 72,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
  methods: {
    sortChange(params) {
      this.tableData.sort((a, b) => {
        if (params.age) {
          if (params.age === 'asc') {
            return a.age - b.age
          } else if (params.age === 'desc') {
            return b.age - a.age
          }
        } else if (params.weight) {
          if (params.weight === 'asc') {
            return a.weight - b.weight
          } else if (params.weight === 'desc') {
            return b.weight - a.weight
          }
        }
        return 0
      })
    },
  },
}
</script>
```

## 多字段排序

| Name | Age | Weight(kg) | Hobby | Address |
|------|-----|------------|-------|---------|
| John | 25 | 66 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 20 | 70 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 18 | 65 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 17 | 80 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 26 | 72 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、通过 `multipleSort=true` 开启多字段排序
2、排序字段的优先级需要自己指定，此处只是示例，具体逻辑自行实现（一般由后端服务返回）

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :sort-option="sortOption" border-y />
</template>

<script>
export default {
  data() {
    return {
      sortOption: {
        // 是否开启多字段排序
        multipleSort: true,
        sortChange: (params) => {
          console.log('sortChange::', params)
          this.sortChange(params)
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        {
          field: 'age',
          key: 'b',
          title: 'Age',
          align: 'center',
          sortBy: '',
        },
        {
          field: 'weight',
          key: 'c',
          title: 'Weight(kg)',
          align: 'center',
          sortBy: 'asc',
        },
        {
          field: 'hobby',
          key: 'd',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'e',
          title: 'Address',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          age: 25,
          weight: 66,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          age: 20,
          weight: 70,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          age: 18,
          weight: 65,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          age: 17,
          weight: 80,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          age: 26,
          weight: 72,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
  methods: {
    sortChange(params) {
      const data = this.tableData.slice(0)

      data.sort((a, b) => {
        if (params.age) {
          if (params.age === 'asc') {
            return a.age - b.age
          } else if (params.age === 'desc') {
            return b.age - a.age
          }
        }
        return 0
      })

      data.sort((a, b) => {
        if (params.weight) {
          if (params.weight === 'asc') {
            return a.weight - b.weight
          } else if (params.weight === 'desc') {
            return b.weight - a.weight
          }
        }
        return 0
      })

      this.tableData = data
    },
  },
}
</script>
```

## 排序切换

| Name | Age | Weight(kg) | Hobby | Address |
|------|-----|------------|-------|---------|
| John | 25 | 66 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 20 | 70 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 18 | 65 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 17 | 80 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 26 | 72 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、通过 `sortAlways=true` 允许排序只在升序和降序切换

```vue
<template>
    <fan-table :columns="columns" :table-data="tableData" :sort-option="sortOption" border-y />
</template>

<script>
export default {
  data() {
    return {
      sortOption: {
        // sort always
        sortAlways: true,
        sortChange: (params) => {
          console.log('sortChange::', params)
          this.sortChange(params)
        },
      },
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'left' },
        {
          field: 'age',
          key: 'b',
          title: 'Age',
          align: 'center',
          sortBy: '',
        },
        {
          field: 'weight',
          key: 'c',
          title: 'Weight(kg)',
          align: 'center',
          sortBy: 'asc',
        },
        {
          field: 'hobby',
          key: 'd',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'e',
          title: 'Address',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          age: 25,
          weight: 66,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          age: 20,
          weight: 70,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          age: 18,
          weight: 65,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          age: 17,
          weight: 80,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          name: 'Jami',
          age: 26,
          weight: 72,
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shenzhen',
        },
      ],
    }
  },
  methods: {
    sortChange(params) {
      this.tableData.sort((a, b) => {
        if (params.age) {
          if (params.age === 'asc') {
            return a.age - b.age
          } else if (params.age === 'desc') {
            return b.age - a.age
          }
        } else if (params.weight) {
          if (params.weight === 'asc') {
            return a.weight - b.weight
          } else if (params.weight === 'desc') {
            return b.weight - a.weight
          }
        }
        return 0
      })
    },
  },
}
</script>
```

## API

### sortOption 排序配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| multipleSort | 是否开启多字段排序 | `Boolean` | - | false |
| sortAlways | 是否开启排序只在升序和降序切换 | `Boolean` | - | false |
| sortChange | 排序改变事件。事件接收 1 个参数对象，列的排序规则 | `Function({row})` | - | - |

### sortBy 列排序配置

| 参数值 | 说明 | 示例 |
|--------|------|------|
| "asc" | 默认当前列升序 | `{ field: 'age', sortBy: 'asc' }` |
| "desc" | 默认当前列降序 | `{ field: 'age', sortBy: 'desc' }` |
| "" | 允许排序但无排序规则 | `{ field: 'age', sortBy: '' }` |
| 不设置 | 不允许排序 | `{ field: 'age' }` |

### 使用说明

1. **单字段排序**：默认排序模式，一次只能对一列进行排序
2. **多字段排序**：通过 `multipleSort: true` 开启，可以同时对多列进行排序
3. **排序切换**：通过 `sortAlways: true` 开启，排序只在升序和降序之间切换
4. **排序回调**：通过 `sortChange` 函数处理排序逻辑，接收排序参数
5. **默认排序**：通过 `sortBy` 设置列的默认排序状态

### 适用场景

- **数据展示**：对表格数据进行排序展示
- **用户交互**：提供用户友好的排序功能
- **复杂排序**：支持多字段组合排序
- **排序状态**：保持排序状态的一致性 