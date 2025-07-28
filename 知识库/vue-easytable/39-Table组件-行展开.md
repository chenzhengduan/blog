# Table组件-行展开

## 概述

Table组件支持行展开功能，通过 `expandOption` 属性配置展开行功能，在 columns 中设置 `type=expand` 作为展开的列，设置 `rowKeyFieldName` 属性对应行数据的列名。render 函数允许自定义展开内容，此处为 JSX 语法。

## 基础功能

通过 `expandOption` 配置启用行展开功能：

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
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

## 触发方式

`trigger` 属性控制展开行事件触发类型：
- `"icon"`：点击展开小图标
- `"cell"`：点击单元格
- `"row"`：点击行

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        trigger: 'row',
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
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

## 展开列配置

选择已有的列作为展开列：

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        trigger: 'cell',
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
      },
      columns: [
        {
          field: 'name',
          key: 'b',
          // 设置需要显示展开图标的列
          type: 'expand',
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

## 可展开配置

通过 `expandable` 函数，设置哪些行能展开：
1. `expandable` 函数接收三个参数：row（当前行数据）、column（可展开列配置）、rowIndex（行索引）
2. 函数返回 false 则不会展开行

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        expandable: ({ row, column, rowIndex }) => {
          if (row.rowKey === 1002) {
            return false
          }
        },
        // render 函数
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'center',
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

## 默认展开配置

1. `defaultExpandedRowKeys` 接收需要展开行的 key 数组
2. `defaultExpandAllRows=true` 可以默认展开全部

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        defaultExpandAllRows: false,
        // 默认需要展开的行
        defaultExpandedRowKeys: [1001, 1003],
        expandable: ({ row, column, rowIndex }) => {
          if (row.rowKey === 1002) {
            return false
          }
        },
        // render 函数
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'center',
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

## 行展开事件

允许在行展开切换前后做其他的处理：
1. `beforeExpandRowChange` 事件接收三个参数：beforeExpandedRowKeys（切换前，展开的数据 key 数组）、row（当前行数据）、rowIndex（行索引）
2. `afterExpandRowChange` 事件接收三个参数：afterExpandedRowKeys（切换后，展开的数据 key 数组）、row（当前行数据）、rowIndex（行索引）

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        expandable: ({ row, column, rowIndex }) => {
          if (row.rowKey === 1002) {
            return false
          }
        },
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
        beforeExpandRowChange: ({ beforeExpandedRowKeys, row, rowIndex }) => {
          if (row.rowKey === 1001) {
            alert('切换前的事件。返回false可中断展开切换')
            return false
          }
          return true
        },
        afterExpandRowChange: ({ afterExpandedRowKeys, row, rowIndex }) => {
          alert('切换后的事件')
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'center',
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

1. `expandedRowKeys` 为展开行的可控属性，展开切换都需要在 `afterExpandRowChange` 事件中做重新赋值处理。通过这个属性可以自定义更多功能
2. 设置 `expandedRowKeys` 属性后 `defaultExpandAllRows` 和 `defaultExpandedRowKeys` 属性将会失效

```vue
<template>
  <div>
    <button class="button-demo" @click="expandSwitch(1003)">第3行展开切换</button>
    <button class="button-demo" @click="expandAll()">展开全部</button>
    <button class="button-demo" @click="foldAll()">折叠全部</button>
    <br />
    <br />
    <fan-table
      style="width:100%"
      :columns="columns"
      :table-data="tableData"
      :expand-option="expandOption"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      expandOption: {
        expandedRowKeys: [1001], // this.expandRowKeys,
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
        // 重新赋值处理
        afterExpandRowChange: ({ afterExpandedRowKeys, row, rowIndex }) => {
          this.changeExpandedRowKeys(afterExpandedRowKeys)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'center',
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
    changeExpandedRowKeys(keys) {
      this.expandOption.expandedRowKeys = keys
    },
    // 切换展开行
    expandSwitch(key) {
      const rowKeyIndex = this.expandOption.expandedRowKeys.indexOf(key)

      if (rowKeyIndex > -1) {
        this.expandOption.expandedRowKeys.splice(rowKeyIndex, 1)
      } else {
        this.expandOption.expandedRowKeys.push(key)
      }
    },
    // 展开全部
    expandAll() {
      this.expandOption.expandedRowKeys = this.tableData.map((x) => x.rowKey)
    },
    // 折叠全部
    foldAll() {
      this.expandOption.expandedRowKeys = []
    },
  },
}
</script>
```

## 展开图表

此示例使用的第三方图表库：echarts。目的是体现展开行的内容完全自定义。echarts 的具体使用，请参考官方文档。

## 展开表格

展开表格也很简单，将包含表格的组件作为 render 渲染函数返回内容即可：

```vue
<template>
  <fan-table
    style="width:100%"
    :columns="columns"
    :table-data="tableData"
    :expand-option="expandOption"
    row-key-field-name="rowKey"
  />
</template>

<script lang="jsx">
// 此示例是在组件内部定义了一个子组件。你当然也可以通过 `import`关键字导入一个组件
const ChildTableComp = {
  name: 'ChildTableComp',
  template: `
    <div class="child-table-comp">
      <span style="font-weight:bold;">Table Name:{{row.name}}</span>
      <fan-table
        style="width:100%"
        :columns="columns"
        :table-data="tableData"
      />
    </div>
  `,
  props: {
    row: Object,
  },
  data() {
    return {
      columns: [
        { field: 'col1', key: 'a', title: 'Col1' },
        { field: 'col2', key: 'b', title: 'Col2' },
        { field: 'col3', key: 'c', title: 'Col3' },
        { field: 'col4', key: 'd', title: 'Col4' },
        { field: 'col5', key: 'e', title: 'Col5' },
      ],
      tableData: [
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
      ],
    }
  },
}

export default {
  data() {
    return {
      expandOption: {
        defaultExpandedRowKeys: [1001],
        render: ({ row, column, rowIndex }, h) => {
          return <ChildTableComp row={row} />
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          // 设置需要显示展开图标的列
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'center',
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

### expandOption 行展开配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| expandable | 是否允许展开行渲染函数，返回布尔值。渲染函数接收三个参数：row（当前行数据）、column（可展开列配置）、rowIndex（行索引） | `Function({row,column,rowIndex})` | - | - |
| render | 渲染函数。渲染函数接收的参数：row（当前行数据）、column（可展开列配置）、rowIndex（行索引）、h（createElement 函数的别名） | `Function({row,column,rowIndex},h):VNode` | - | - |
| defaultExpandAllRows | 是否默认展开全部行 | `Boolean` | - | false |
| defaultExpandedRowKeys | 默认展开的行key。`defaultExpandAllRows` 参数和 `defaultExpandedRowKeys` 参数同时存在时，优先使用 `defaultExpandAllRows` | `String[]`、`Number[]` | - | - |
| expandedRowKeys | 展开行的可控属性，设置后 `defaultExpandAllRows` 和 `defaultExpandedRowKeys` 属性将会失效 | `String[]`、`Number[]` | - | - |
| beforeExpandRowChange | 展开切换前的函数，如果返回 false 则中断执行。函数接收三个参数：beforeExpandedRowKeys（改变前所有展开的key）、row（当前的行数据）、rowIndex（行号） | `Function({beforeExpandedRowKeys,row,rowIndex})` | - | - |
| afterExpandRowChange | 展开切换后的函数。函数接收三个参数：afterExpandedRowKeys（改变后所有展开的key）、row（当前的行数据）、rowIndex（行号） | `Function({afterExpandedRowKeys,row,rowIndex})` | - | - |
| trigger | 展开行事件触发类型。icon：点击展开小图标；cell：点击单元格；row：点击行 | `String` | "icon"、"cell"、"row" | "icon" |

## 配置要点

1. **启用展开**：设置 `expandOption` 属性
2. **展开列**：在 columns 中设置 `type: 'expand'`
3. **行标识**：必须设置 `row-key-field-name` 属性
4. **自定义内容**：通过 `render` 函数自定义展开内容

## 功能特性

### 1. 基础展开
- 支持行展开/折叠
- 自定义展开内容
- JSX 语法支持

### 2. 触发方式
- 图标点击触发
- 单元格点击触发
- 整行点击触发

### 3. 展开控制
- 控制哪些行可展开
- 默认展开配置
- 可控属性支持

### 4. 事件处理
- 展开前事件
- 展开后事件
- 事件拦截支持

### 5. 内容自定义
- 支持任意内容
- 支持嵌套表格
- 支持图表展示

## 使用说明

1. **基本配置**：
   ```javascript
   expandOption: {
     render: ({ row, column, rowIndex }, h) => {
       return <div>自定义内容</div>
     }
   }
   ```

2. **列配置**：
   ```javascript
   {
     field: '',
     type: 'expand',
     title: '',
     width: 50,
     align: 'center',
   }
   ```

3. **可控模式**：
   ```javascript
   expandOption: {
     expandedRowKeys: this.expandedKeys,
     afterExpandRowChange: ({ afterExpandedRowKeys }) => {
       this.expandedKeys = afterExpandedRowKeys
     }
   }
   ```

## 应用场景

- 详细信息展示
- 嵌套数据展示
- 图表数据可视化
- 子表格展示
- 复杂内容展示

## 注意事项

1. 必须设置 `row-key-field-name` 属性
2. 使用 JSX 语法需要配置相应的编译环境
3. 使用可控属性时需要在事件中更新状态
4. `expandedRowKeys` 和默认展开属性不能同时生效
5. `beforeExpandRowChange` 返回 false 可以阻止展开操作
6. 展开内容完全自定义，可以包含任意组件