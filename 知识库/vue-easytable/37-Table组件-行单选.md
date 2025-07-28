# Table组件-行单选

## 概述

Table组件支持行单选功能，通过 `radioOption` 属性开启单选功能，在 columns 中设置 `type=radio` 作为单选的列，设置 `rowKeyFieldName` 属性对应行数据的列名，通过 `selectedRowChange` 事件监听行改变。

## 基础功能

通过 `radioOption` 配置启用行单选功能：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      :radio-option="radioOption"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      radioOption: {
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          console.log(row)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // type=radio
          type: 'radio',
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

## 单选列配置

选择已有的列作为 radio 单选列：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      :radio-option="radioOption"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      radioOption: {
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          console.log(row)
        },
      },
      columns: [
        {
          field: 'name',
          key: 'b',
          // type=radio
          type: 'radio',
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

## 默认选中

通过 `defaultSelectedRowKey` 设置需要默认选中的 rowKey：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      :radio-option="radioOption"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      radioOption: {
        defaultSelectedRowKey: 1003,
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          console.log(row)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // type=radio
          type: 'radio',
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

## 禁用选择

通过 `disableSelectedRowKeys` 设置需要禁止选中的 rowKey 数组：

```vue
<template>
  <div>
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      :radio-option="radioOption"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      radioOption: {
        // 禁用的选择（禁止勾选或者禁止取消勾选）
        disableSelectedRowKeys: [1003, 1005],
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          console.log(row)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // type=radio
          type: 'radio',
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

## 可控属性

`selectedRowKey` 为单选的"可控属性"，选择后需要在 `selectedRowChange` 事件中做重新赋值处理。设置 `selectedRowKey` 属性后 `defaultSelectedRowKey` 属性将会失效：

```vue
<template>
  <div>
    <button class="button-demo" @click="selectedSwitch(1002)">第二行选中切换</button>
    <button class="button-demo" @click="unselected()">取消选中</button>
    <br />
    <br />
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      :radio-option="radioOption"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      radioOption: {
        selectedRowKey: '',
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          this.changeSelectedRowKey(row.rowKey)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // type=radio
          type: 'radio',
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
    changeSelectedRowKey(key) {
      this.radioOption.selectedRowKey = key
    },
    // 切换选中行
    selectedSwitch(key) {
      const selectedRowKey = this.radioOption.selectedRowKey

      if (selectedRowKey === key) {
        this.radioOption.selectedRowKey = ''
      } else {
        this.radioOption.selectedRowKey = key
      }
    },
    // 取消选中
    unselected() {
      this.radioOption.selectedRowKey = ''
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
      :radio-option="radioOption"
      row-key-field-name="rowKey"
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
              this.changeSelectedRowKeyByRowClick(currentRowKey)
            },
          }
        },
      },
      radioOption: {
        selectedRowKey: '',
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          this.changeSelectedRowKey(row.rowKey)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // type=radio
          type: 'radio',
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
    changeSelectedRowKey(key) {
      this.radioOption.selectedRowKey = key
    },
    // 行点击触发
    changeSelectedRowKeyByRowClick(currentRowKey) {
      this.radioOption.selectedRowKey = currentRowKey
    },
  },
}
</script>
```

## API 参数

### radioOption 行单选配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultSelectedRowKey | 默认选中的行key | `String`、`Number` | - | - |
| disableSelectedRowKeys | 禁止勾选或者禁止取消勾选的行key | `String[]`、`Number[]` | - | - |
| selectedRowKey | 选中行的可控属性，设置后 `defaultSelectedRowKey` 属性将会失效 | `String`、`Number` | - | - |
| selectedRowChange | 行选中的改变事件，接收 1 个参数 row:当前行数据 | `Function({row})` | - | - |

## 配置要点

1. **启用单选**：设置 `radioOption` 属性
2. **单选列**：在 columns 中设置 `type: 'radio'`
3. **行标识**：必须设置 `row-key-field-name` 属性
4. **事件监听**：通过 `selectedRowChange` 监听选择变化

## 功能特性

### 1. 基础单选
- 支持单行选择
- 提供选择变化事件

### 2. 默认选中
- 支持设置默认选中行
- 通过 `defaultSelectedRowKey` 配置

### 3. 禁用选择
- 支持禁用特定行的选择
- 通过 `disableSelectedRowKeys` 配置

### 4. 可控属性
- 支持外部控制选中状态
- 通过 `selectedRowKey` 实现

### 5. 交互增强
- 支持行点击触发选中
- 结合事件自定义实现

## 使用说明

1. **基本配置**：
   ```javascript
   radioOption: {
     selectedRowChange: ({ row }) => {
       console.log('选中行:', row)
     }
   }
   ```

2. **列配置**：
   ```javascript
   {
     field: '',
     type: 'radio',
     title: '',
     width: 50,
     align: 'center',
   }
   ```

3. **可控模式**：
   ```javascript
   radioOption: {
     selectedRowKey: this.selectedKey,
     selectedRowChange: ({ row }) => {
       this.selectedKey = row.rowKey
     }
   }
   ```

## 应用场景

- 数据列表的单项选择
- 表单中的选项选择
- 详情查看的行选择
- 操作确认的目标选择

## 注意事项

1. 必须设置 `row-key-field-name` 属性
2. 使用可控属性时需要在事件中更新状态
3. `selectedRowKey` 和 `defaultSelectedRowKey` 不能同时生效
4. 禁用的行无法进行选择操作
5. 结合事件自定义可以实现更丰富的交互效果