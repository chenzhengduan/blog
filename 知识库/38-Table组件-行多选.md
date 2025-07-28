# Table组件-行多选

## 概述

Table组件支持行多选功能，通过 `checkboxOption` 属性开启多选功能，在 columns 中设置 `type=checkbox` 作为多选的列，设置 `rowKeyFieldName` 属性对应行数据的列名。

## 事件说明

- `selectedRowChange` - 行改变事件，接收 3 个参数：row（当前行数据）、isSelected（当前行是否选中）、selectedRowKeys（所有选中的 rowKey 信息）
- `selectedAllChange` - 全选事件，接收 2 个参数：isSelected（是否全选）、selectedRowKeys（所有选中的 rowKey 信息）

## 基础功能

通过 `checkboxOption` 配置启用行多选功能：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      checkboxOption: {
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log(row, isSelected, selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          console.log(isSelected, selectedRowKeys)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
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
      tableData: [
        {
          rowKey: 1001,
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          rowKey: 1002,
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          rowKey: 1003,
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          rowKey: 1004,
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
          rowKey: 1005,
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

## 多选列配置

选择已有的列作为 checkbox 多选列：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      checkboxOption: {
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log(row, isSelected, selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          console.log(isSelected, selectedRowKeys)
        },
      },

      columns: [
        {
          field: 'name',
          key: 'b',
          type: 'checkbox',
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
      tableData: [
        // ... 表格数据
      ],
    }
  },
}
</script>
```

## 默认选中

通过 `defaultSelectedRowKeys` 设置需要默认选中的 rowKey 数组，通过 `defaultSelectedAllRows` 默认选中全部：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      checkboxOption: {
        defaultSelectedRowKeys: [1001, 1003, 1004],
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log(row, isSelected, selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          console.log(isSelected, selectedRowKeys)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
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
      tableData: [
        // ... 表格数据
      ],
    }
  },
}
</script>
```

## 禁止选中

通过 `disableSelectedRowKeys` 设置需要禁止选中的 rowKey 数组（禁止勾选或者禁止取消勾选）：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      checkboxOption: {
        // 禁用的选择（禁止勾选或者禁止取消勾选）
        disableSelectedRowKeys: [1002, 1005],
        // 默认选择
        defaultSelectedRowKeys: [1001, 1003, 1004, 1005],
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log(row, isSelected, selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          console.log(isSelected, selectedRowKeys)
        },
      },

      columns: [
        // ... 列配置
      ],
      tableData: [
        // ... 表格数据
      ],
    }
  },
}
</script>
```

## 可控属性

`selectedRowKeys` 为多选的"可控属性"，选择后需要在 `selectedRowChange` 事件和 `selectedAllChange` 事件中做重新赋值处理。设置 `selectedRowKeys` 属性后 `defaultSelectedRowKeys` 和 `defaultSelectedAllRows` 属性将会失效：

```vue
<template>
  <div>
    <button class="button-demo" @click="selectedSwitch(1002)">第二行选中切换</button>
    <button class="button-demo" @click="selectedAll()">选中全部</button>
    <button class="button-demo" @click="unselectedAll()">取消选中全部</button>
    <br />
    <br />
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      checkboxOption: {
        // 可控属性
        selectedRowKeys: [1003],
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          this.changeSelectedRowKeys(selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          this.changeSelectedRowKeys(selectedRowKeys)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
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
      tableData: [
        // ... 表格数据
      ],
    }
  },
  methods: {
    // 给可控属性重新赋值
    changeSelectedRowKeys(keys) {
      this.checkboxOption.selectedRowKeys = keys
    },
    // 切换选中行
    selectedSwitch(key) {
      const selectedRowKeys = this.checkboxOption.selectedRowKeys

      const rowKeyIndex = selectedRowKeys.indexOf(key)

      if (rowKeyIndex > -1) {
        selectedRowKeys.splice(rowKeyIndex, 1)
      } else {
        selectedRowKeys.push(key)
      }
    },
    // 选中全部
    selectedAll() {
      this.checkboxOption.selectedRowKeys = this.tableData.map((x) => x.rowKey)
    },
    // 取消选中全部
    unselectedAll() {
      this.checkboxOption.selectedRowKeys = []
    },
  },
}
</script>
```

## 行点击触发选中

> 此示例为行点击触发选中，你也可以通过"事件自定义"实现列点击选中效果

可控属性结合"事件自定义"，即可实现点击行触发选中效果：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
      :event-custom-option="eventCustomOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        bodyRowEvents: ({ row, rowIndex }) => {
          return {
            click: (event) => {
              const currentRowKey = row.rowKey
              this.changeSelectedRowKeysByRowClick(currentRowKey)
            },
          }
        },
      },
      checkboxOption: {
        // 可控属性
        selectedRowKeys: [1003],
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {},
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          this.changeSelectedRowKeys(selectedRowKeys)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
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
      tableData: [
        // ... 表格数据
      ],
    }
  },
  methods: {
    // 给可控属性重新赋值
    changeSelectedRowKeys(keys) {
      this.checkboxOption.selectedRowKeys = keys
    },
    // 行点击触发
    changeSelectedRowKeysByRowClick(currentRowKey) {
      const { selectedRowKeys } = this.checkboxOption

      if (selectedRowKeys.includes(currentRowKey)) {
        const rowKeyIndex = selectedRowKeys.indexOf(currentRowKey)
        selectedRowKeys.splice(rowKeyIndex, 1)
      } else {
        selectedRowKeys.push(currentRowKey)
      }
    },
  },
}
</script>
```

## 隐藏全选

通过 `hideSelectAll=true` 隐藏全选按钮：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
      :checkbox-option="checkboxOption"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      checkboxOption: {
        hideSelectAll: true,
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log(row, isSelected, selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          console.log(isSelected, selectedRowKeys)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
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
      tableData: [
        // ... 表格数据
      ],
    }
  },
}
</script>
```

## API 参数

### checkboxOption 行多选配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultSelectedAllRows | 是否默认全部选中 | `Boolean` | - | false |
| defaultSelectedRowKeys | 默认选中的行key | `String[]`、`Number[]` | - | - |
| disableSelectedRowKeys | 禁止勾选或者禁止取消勾选的行key | `String[]`、`Number[]` | - | - |
| selectedRowKeys | 选中行的可控属性，设置后 `defaultSelectedAllRows` 和 `defaultSelectedRowKeys` 属性将会失效 | `String[]`、`Number[]` | - | - |
| selectedRowChange | 行选中的改变事件，接收 3 个参数：row（当前行数据）、isSelected（当前行是否选中）、selectedRowKeys（所有选中的 rowKey 信息） | `Function({row, isSelected, selectedRowKeys})` | - | - |
| selectedAllChange | 全选改变事件，接收 2 个参数：isSelected（是否全选）、selectedRowKeys（所有选中的 rowKey 信息） | `Function({isSelected, selectedRowKeys})` | - | - |
| hideSelectAll | 是否隐藏全选按钮 | `Boolean` | - | false |

## 配置要点

1. **启用多选**：设置 `checkboxOption` 属性
2. **多选列**：在 columns 中设置 `type: 'checkbox'`
3. **行标识**：必须设置 `row-key-field-name` 属性
4. **事件监听**：通过 `selectedRowChange` 和 `selectedAllChange` 监听选择变化

## 功能特性

### 1. 基础多选
- 支持多行选择
- 支持全选/取消全选
- 提供选择变化事件

### 2. 默认选中
- 支持设置默认选中行
- 支持默认全选
- 通过 `defaultSelectedRowKeys` 或 `defaultSelectedAllRows` 配置

### 3. 禁用选择
- 支持禁用特定行的选择
- 通过 `disableSelectedRowKeys` 配置

### 4. 可控属性
- 支持外部控制选中状态
- 通过 `selectedRowKeys` 实现

### 5. 交互增强
- 支持行点击触发选中
- 支持隐藏全选按钮
- 结合事件自定义实现

## 使用说明

1. **基本配置**：
   ```javascript
   checkboxOption: {
     selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
       console.log('选中变化:', row, isSelected, selectedRowKeys)
     },
     selectedAllChange: ({ isSelected, selectedRowKeys }) => {
       console.log('全选变化:', isSelected, selectedRowKeys)
     }
   }
   ```

2. **列配置**：
   ```javascript
   {
     field: '',
     type: 'checkbox',
     title: '',
     width: 50,
     align: 'center',
   }
   ```

3. **可控模式**：
   ```javascript
   checkboxOption: {
     selectedRowKeys: this.selectedKeys,
     selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
       this.selectedKeys = selectedRowKeys
     },
     selectedAllChange: ({ isSelected, selectedRowKeys }) => {
       this.selectedKeys = selectedRowKeys
     }
   }
   ```

## 应用场景

- 数据列表的批量操作
- 表单中的多项选择
- 批量删除或编辑
- 数据导出的选择

## 注意事项

1. 必须设置 `row-key-field-name` 属性
2. 使用可控属性时需要在事件中更新状态
3. `selectedRowKeys` 和默认选中属性不能同时生效
4. 禁用的行无法进行选择操作
5. 结合事件自定义可以实现更丰富的交互效果
6. 全选操作会受到禁用行的影响