# Table 组件-结合分页

## 概述

1、表格组件和分页组件是分开的
2、下面的示例为模拟数据，通常分页都是要结合后端服务的
3、数据量很大，但又不想使用分页，你也可以使用虚拟滚动

## 示例

| # | Name | Date | Hobby | Address |
|---|------|------|-------|---------|
| 1 | John0 | 1900-05-20 | coding and coding repeat0 | No.1 Century Avenue, Shanghai0 |
| 2 | John1 | 1900-05-20 | coding and coding repeat1 | No.1 Century Avenue, Shanghai1 |
| 3 | John2 | 1900-05-20 | coding and coding repeat2 | No.1 Century Avenue, Shanghai2 |
| 4 | John3 | 1900-05-20 | coding and coding repeat3 | No.1 Century Avenue, Shanghai3 |
| 5 | John4 | 1900-05-20 | coding and coding repeat4 | No.1 Century Avenue, Shanghai4 |
| 6 | John5 | 1900-05-20 | coding and coding repeat5 | No.1 Century Avenue, Shanghai5 |
| 7 | John6 | 1900-05-20 | coding and coding repeat6 | No.1 Century Avenue, Shanghai6 |
| 8 | John7 | 1900-05-20 | coding and coding repeat7 | No.1 Century Avenue, Shanghai7 |
| 9 | John8 | 1900-05-20 | coding and coding repeat8 | No.1 Century Avenue, Shanghai8 |
| 10 | John9 | 1900-05-20 | coding and coding repeat9 | No.1 Century Avenue, Shanghai9 |

```vue
<template>
    <div>
        <fan-table :columns="columns" :table-data="tableData" />
        <div class="table-pagination">
            <ve-pagination
                :total="totalCount"
                :page-index="pageIndex"
                :page-size="pageSize"
                @on-page-number-change="pageNumberChange"
                @on-page-size-change="pageSizeChange"
            />
        </div>
    </div>
</template>

<style>
    .table-pagination {
        margin-top: 20px;
        text-align: right;
    }
</style>

<script>
// Simulation table data from database
let DB_DATA = []

export default {
  data() {
    return {
      // page index
      pageIndex: 1,
      // page size
      pageSize: 10,
      columns: [
        {
          field: '',
          key: 'a',
          title: '#',
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (this.pageIndex - 1) * this.pageSize + rowIndex + 1
          },
        },
        { field: 'name', key: 'b', title: 'Name', align: 'center' },
        { field: 'date', key: 'c', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'd', title: 'Hobby', align: 'left' },
        { field: 'address', key: 'e', title: 'Address', width: '' },
      ],
    }
  },
  computed: {
    // table data
    tableData() {
      const { pageIndex, pageSize } = this
      return DB_DATA.slice((pageIndex - 1) * pageSize, pageIndex * pageSize)
    },
    // total count
    totalCount() {
      return DB_DATA.length
    },
  },
  created() {
    this.initDatabase()
  },
  methods: {
    // page number change
    pageNumberChange(pageIndex) {
      this.pageIndex = pageIndex
    },

    // page size change
    pageSizeChange(pageSize) {
      this.pageIndex = 1
      this.pageSize = pageSize
    },

    // Simulation table data
    initDatabase() {
      DB_DATA = []
      for (let i = 0; i < 1000; i++) {
        DB_DATA.push({
          name: 'John' + i,
          date: '1900-05-20',
          hobby: 'coding and coding repeat' + i,
          address: 'No.1 Century Avenue, Shanghai' + i,
        })
      }
    },
  },
}
</script>
```

## 结合行多选

当前选中的行key：[]

### 功能描述

此示例多选逻辑可以参考 mail.google.com

```vue
<template>
    <div>
        <div>当前选中的行key：{{ selectedRowKeysCollection }}</div>
        <fan-table :columns="columns" :table-data="currentPageData" :checkbox-option="checkboxOption"
            row-key-field-name="rowKey" />
        <div class="table-pagination">
            <ve-pagination :total="totalCount" :page-index="pageIndex" :page-size="pageSize"
                @on-page-number-change="pageNumberChange" @on-page-size-change="pageSizeChange" />
        </div>
    </div>
</template>

<style>
.table-pagination {
    margin-top: 20px;
    text-align: right;
}
</style>

<script>
// Simulation table data from database
let DB_DATA = []

export default {
  data() {
    return {
      // page index
      pageIndex: 1,
      // page size
      pageSize: 10,
      // selected row keys collection
      selectedRowKeysCollection: [],
      // checkbox option
      checkboxOption: {
        // 可控属性
        selectedRowKeys: [],
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          this.changeSelectedRowKeys(row, isSelected)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          this.changeSelectAll(isSelected, selectedRowKeys)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '#',
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (this.pageIndex - 1) * this.pageSize + rowIndex + 1
          },
        },
        {
          field: '',
          key: 'checkbox',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
        },

        { field: 'name', key: 'b', title: 'Name', align: 'center' },
        { field: 'date', key: 'c', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'd', title: 'Hobby', align: 'left' },
        { field: 'address', key: 'e', title: 'Address', width: '' },
      ],
    }
  },
  computed: {
    // table data
    currentPageData() {
      const { pageIndex, pageSize } = this
      return DB_DATA.slice((pageIndex - 1) * pageSize, pageIndex * pageSize)
    },
    // total count
    totalCount() {
      return DB_DATA.length
    },
  },
  created() {
    this.initDatabase()
  },
  methods: {
    // selected rowKeys change
    changeSelectedRowKeys(row, isSelected) {
      const rowKey = row.rowKey

      if (isSelected) {
        this.checkboxOption.selectedRowKeys.push(rowKey)
        this.selectedRowKeysCollection.push(rowKey)
      } else {
        const index = this.checkboxOption.selectedRowKeys.indexOf(rowKey)
        this.checkboxOption.selectedRowKeys.splice(index, 1)
        this.selectedRowKeysCollection.splice(index, 1)
      }
    },

    // select all change
    changeSelectAll(isSelected, selectedRowKeys) {
      this.checkboxOption.selectedRowKeys = selectedRowKeys

      if (isSelected) {
        this.selectedRowKeysCollection =
                    this.selectedRowKeysCollection.concat(selectedRowKeys)
      } else {
        this.currentPageData.forEach((item, index) => {
          if (this.selectedRowKeysCollection.indexOf(item.rowKey) > -1) {
            this.selectedRowKeysCollection.splice(index, 1)
          }
        })
      }
    },

    // reset selectedRowKeys
    resetSelectedRowKeys() {
      this.checkboxOption.selectedRowKeys = []

      const selectedRowKeysCollection = this.selectedRowKeysCollection

      if (selectedRowKeysCollection.length) {
        this.currentPageData.forEach((item) => {
          if (selectedRowKeysCollection.indexOf(item.rowKey) > -1) {
            this.checkboxOption.selectedRowKeys.push(item.rowKey)
          }
        })
      }
    },

    // page number change
    pageNumberChange(pageIndex) {
      this.pageIndex = pageIndex
      this.resetSelectedRowKeys()
    },

    // page size change
    pageSizeChange(pageSize) {
      this.pageIndex = 1
      this.pageSize = pageSize
      this.resetSelectedRowKeys()
    },

    // Simulation table data
    initDatabase() {
      DB_DATA = []
      for (let i = 0; i < 1000; i++) {
        DB_DATA.push({
          rowKey: i,
          name: 'John' + i,
          date: '1900-05-20',
          hobby: 'coding and coding repeat' + i,
          address: 'No.1 Century Avenue, Shanghai' + i,
        })
      }
    },
  },
}
</script>
```

## 功能特性

- **组件分离**: 表格组件和分页组件是独立的，可以灵活组合
- **数据分页**: 支持大数据量的分页展示
- **行序号**: 支持跨页的行序号显示
- **行多选**: 支持跨页的多选功能，类似Gmail的选择逻辑
- **状态保持**: 切换页面时保持选中状态
- **虚拟滚动**: 大数据量时可选择虚拟滚动替代分页

## 使用场景

- **数据列表**: 展示大量数据的列表页面
- **后台管理**: 用户管理、订单管理等后台系统
- **数据查询**: 搜索结果的分页展示
- **批量操作**: 支持跨页的批量选择操作

## 实现要点

### 基础分页实现

```javascript
// 计算当前页数据
tableData() {
  const { pageIndex, pageSize } = this
  return DB_DATA.slice((pageIndex - 1) * pageSize, pageIndex * pageSize)
}

// 计算总数
totalCount() {
  return DB_DATA.length
}
```

### 行序号计算

```javascript
renderBodyCell: ({ row, column, rowIndex }, h) => {
  return (this.pageIndex - 1) * this.pageSize + rowIndex + 1
}
```

### 跨页多选逻辑

```javascript
// 重置当前页选中状态
resetSelectedRowKeys() {
  this.checkboxOption.selectedRowKeys = []
  
  const selectedRowKeysCollection = this.selectedRowKeysCollection
  
  if (selectedRowKeysCollection.length) {
    this.currentPageData.forEach((item) => {
      if (selectedRowKeysCollection.indexOf(item.rowKey) > -1) {
        this.checkboxOption.selectedRowKeys.push(item.rowKey)
      }
    })
  }
}
```

## 最佳实践

- **数据获取**: 实际项目中应该从后端API获取分页数据
- **状态管理**: 使用Vuex等状态管理工具管理分页和选中状态
- **性能优化**: 大数据量时考虑使用虚拟滚动
- **用户体验**: 提供加载状态和错误处理
- **响应式设计**: 考虑不同屏幕尺寸下的分页显示 