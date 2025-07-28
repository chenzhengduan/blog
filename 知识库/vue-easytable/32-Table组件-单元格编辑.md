# Table 组件-单元格编辑

## 概述

1、通过 `editOption` 属性配置单元格编辑功能
2、通过 columns 对象设置 `edit=true` 允许编辑的列

## 快捷键

可编辑单元格支持以下快捷键（参考 excel 快捷键）：

| 功能 | 快捷键 |
|------|--------|
| 活动单元格进入编辑状态 | F2 |
| 停止编辑状态，并停留在当前单元格 | Ctrl + Enter |
| 单元格内文本换行 | Alt + Enter |
| 清空活动单元格内容 | Delete |
| 清空活动单元格内容，并进入编辑状态 | BackSpace |
| 清空活动单元格内容并填入空格 | Space |
| 停止编辑状态并向下移动活动单元格 | Enter |
| 停止编辑状态并向上移动活动单元格 | Shift + Enter |
| 停止编辑状态并向右移动活动单元格 | Tab |
| 停止编辑状态并向左移动活动单元格 | Shift + Tab |
| 支持在可编辑单元格直接输入文本并进入编辑状态 | - |
| 支持长文本输入时，编辑框自动伸缩功能 | - |

## 基本用法

1、尝试将 "Number"列的值改为非数字
2、尝试修改第一行第一列

### 功能描述

1、单元格进入编辑状态前首先触发 `beforeStartCellEditing` 回调，如果返回 false，则会阻止进入编辑状态。
2、单元格停止编辑后首先触发 `beforeCellValueChange` 回调，如果返回 false，则会阻止编辑，单元格还原为编辑前状态。编辑成功将触发 `afterCellValueChange` 方法
3、你可以利用 `beforeCellValueChange` 做编辑校验功能

```vue
<template>
    <div>
        <fan-table
            row-key-field-name="rowKey"
            :fixed-header="true"
            :columns="columns"
            :table-data="tableData"
            :edit-option="editOption"
            :row-style-option="rowStyleOption"
            border-y
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      // edit option 可控单元格编辑
      editOption: {
        beforeStartCellEditing: ({ row, column, cellValue }) => {
          console.log('beforeStartCellEditing')
          console.log('row::', row)
          console.log('column::', column)
          console.log('cellValue::', cellValue)
          console.log('---')

          if (row.rowKey === 0 && column.field === 'name') {
            alert("You can't edit this cell.")
            return false
          }
        },
        beforeCellValueChange: ({ row, column, changeValue }) => {
          console.log('beforeCellValueChange')
          console.log('row::', row)
          console.log('column::', column)
          console.log('changeValue::', changeValue)

          console.log('---')

          if (column.field === 'number' && !/^\d+$/.test(changeValue)) {
            alert('请输入数字')
            return false
          }
        },
        afterCellValueChange: ({ row, column, changeValue }) => {
          console.log('afterCellValueChange')
          console.log('row::', row)
          console.log('column::', column)
          console.log('changeValue::', changeValue)
          console.log('---')
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          operationColumn: true,
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        {
          field: 'name',
          key: 'name',
          title: 'Name',
          align: 'left',
          width: '15%',
          edit: true,
        },
        {
          field: 'date',
          key: 'date',
          title: 'Date',
          align: 'left',
          width: '15%',
          edit: true,
        },
        {
          field: 'number',
          key: 'number',
          title: 'Number',
          align: 'right',
          width: '30%',
          edit: true,
        },
        {
          field: 'address',
          key: 'address',
          title: 'Address',
          align: 'left',
          width: '40%',
          edit: true,
        },
      ],
      // table data
      tableData: [
        {
          name: "You can't edit",
          date: '1900-05-20',
          number: '32',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          number: '676',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          number: '76',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          number: '7797',
          address: 'No.1 Century Avenue, Xiamen',
          rowKey: 3,
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          number: '8978',
          address: 'No.1 Century Avenue, Shenzhen',
          rowKey: 4,
        },
      ],
    }
  },
}
</script>
```

## 实例方法

### 功能描述

1、通过实例方法 `startEditingCell({ rowKey, colKey, defaultValue })` 编辑指定的单元格

```vue
<template>
  <div>
    <button class="button-demo" @click="startEditingCell(0, 'name')">编辑单元格0-0</button>
    <button class="button-demo" @click="startEditingCell(2, 'hobby', '')">
      编辑并清空单元格2-2
    </button>
    <br />
    <br />
    <fan-table ref="tableRef" row-key-field-name="rowKey" :max-height="300" :fixed-header="true" :columns="columns"
      :table-data="tableData" :edit-option="editOption" :row-style-option="rowStyleOption" border-y />
  </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      // edit option 可控单元格编辑
      editOption: {
        // cell value change
        cellValueChange: ({ row, column }) => {
          console.log('cellValueChange row::', row)
          console.log('cellValueChange column::', column)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          operationColumn: true,
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        {
          field: 'name',
          key: 'name',
          title: 'Name',
          align: 'left',
          width: '15%',
          edit: true,
        },
        {
          field: 'date',
          key: 'date',
          title: 'Date',
          align: 'left',
          width: '15%',
          edit: true,
        },
        {
          field: 'hobby',
          key: 'hobby',
          title: 'Hobby',
          align: 'left',
          width: '30%',
          edit: true,
        },
        {
          field: 'address',
          key: 'address',
          title: 'Address',
          align: 'left',
          width: '40%',
          edit: true,
        },
      ],
      // table data
      tableData: [
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
  methods: {
    // start editing cell
    startEditingCell(rowKey, colKey, defaultValue) {
      this.$refs.tableRef.startEditingCell({ rowKey, colKey, defaultValue })
    },
  },
}
</script>
```

## 结合固定列

```vue
<template>
  <div>
    <fan-table :scroll-width="1600" :max-height="500" row-key-field-name="rowKey" :fixed-header="true" :columns="columns"
      :table-data="tableData" :edit-option="editOption" :row-style-option="rowStyleOption"
      :virtual-scroll-option="{ enable: true }" border-y />
  </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      // edit option 可控单元格编辑
      editOption: {
        // cell value change
        cellValueChange: ({ row, column }) => {
          console.log('cellValueChange row::', row)
          console.log('cellValueChange column::', column)
        },
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 15,
          align: 'center',
          fixed: 'left',
          operationColumn: true,
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        {
          field: 'col1',
          key: 'col1',
          title: 'col1',
          width: 50,
          fixed: 'left',
          edit: true,
        },
        {
          title: 'col2-col3',
          fixed: 'left',
          children: [
            {
              field: 'col2',
              key: 'col2',
              title: 'col2',
              width: 50,
              edit: true,
            },
            {
              field: 'col3',
              key: 'col3',
              title: 'col3',
              width: 50,
              edit: true,
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
                  edit: true,
                },
                {
                  field: 'col5',
                  key: 'col5',
                  title: 'col5',
                  width: 140,
                  edit: true,
                },
              ],
            },
            {
              title: 'col6',
              field: 'col6',
              key: 'col6',
              width: 140,
              edit: true,
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
              key: 'col7',
              width: 50,
              edit: true,
            },
          ],
        },
        {
          field: 'col8',
          key: 'col8',
          title: 'col8',
          width: 50,
          fixed: 'right',
          edit: true,
        },
      ],
      // table data
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },

  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 100; i++) {
        data.push({
          rowKey: i,
          col1: `A` + i,
          col2: `B` + i,
          col3: `C` + i,
          col4: `D` + i,
          col5: `E` + i,
          col6: `F` + i,
          col7: `G` + i,
          col8: `H` + i,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 结合 element-ui

| Name | Date | Age | Gender |
|------|------|-----|--------|
| 1 | John | 选择日期 | |
| 2 | Dickerson | 选择日期 | |
| 3 | Larsen | 选择日期 | |
| 4 | Geneva | 选择日期 | |
| 5 | Jami | 选择日期 | |

### 功能描述

1、你也可以结合 element-ui 组件做日期、数字、下拉等编辑功能
2、注意：组件本身可能会和第三方库组件的快捷键冲突，此时你可以通过 `cellSelectionOption` 去禁用单元格选择功能

```vue
<template>
  <div>
    <el-button type="primary" @click="submit()">提交</el-button>
    <br />
    <br />
    <fan-table row-key-field-name="rowKey" :fixed-header="true" :columns="columns" :table-data="tableData"
      :cell-selection-option="cellSelectionOption" :row-style-option="rowStyleOption" />
  </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      // submit data
      submitData: [
        // {
        //     rowKey: 0,
        //     field: "",
        //     value: "",
        // },
      ],
      cellSelectionOption: {
        // default true
        enable: false,
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          operationColumn: true,
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        {
          field: 'name',
          key: 'name',
          title: 'Name',
          align: 'left',
          width: '15%',
        },
        {
          field: 'date',
          key: 'date',
          title: 'Date',
          align: 'left',
          width: '15%',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (
              <el-date-picker
                size="small"
                value={row.date}
                onInput={(val) => {
                  row.date = val
                  this.cellDataChange(row, column, val)
                }}
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="选择日期"
              ></el-date-picker>
            )
          },
        },
        {
          field: 'age',
          key: 'age',
          title: 'Age',
          align: 'center',
          width: '30%',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (
              <el-input-number
                size="small"
                min={1}
                value={row.age}
                onInput={(val) => {
                  row.age = val
                }}
                onChange={(val) => {
                  this.cellDataChange(row, column, val)
                }}
              ></el-input-number>
            )
          },
        },
        {
          field: 'gender',
          key: 'gender',
          title: 'Gender',
          align: 'left',
          width: '40%',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return (
              <el-select
                size="small"
                value={row.gender}
                onInput={(val) => {
                  row.gender = val
                  this.cellDataChange(row, column, val)
                }}
                placeholder="请选择"
              >
                <el-option label="female" value="female"></el-option>
                <el-option label="male" value="male"></el-option>
              </el-select>
            )
          },
        },
      ],
      // table data
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          age: 17,
          gender: 'female',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          age: 20,
          gender: 'male',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          age: 22,
          gender: 'female',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          age: 18,
          gender: 'male',
          rowKey: 3,
        },
        {
          name: 'Jami',
          date: '2020-09-20',
          age: 29,
          gender: 'female',
          rowKey: 4,
        },
      ],
    }
  },
  methods: {
    // submit
    submit() {
      alert(JSON.stringify(this.submitData))
    },

    // cell data change
    cellDataChange(row, column, cellValue) {
      const { submitData } = this

      const currentCell = submitData.find(
        (x) => x.rowKey === row.rowKey && x.field === column.field,
      )

      if (currentCell) {
        currentCell.value = cellValue
      } else {
        const newCell = {
          rowKey: row.rowKey,
          field: column.field,
          value: cellValue,
        }
        this.submitData.push(newCell)
      }
    },
  },
}
</script>
```

## API

### editOption 单元格编辑配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| beforeStartCellEditing | 单元格进入编辑状态前的回调方法。`row`当前行数据，`column`当前列信息，`cellValue`当前单元格的值。如果返回false，将则会阻止单元格进入编辑状态 | `Function({ row, column,cellValue })` | - | - |
| beforeCellValueChange | 单元格内容改变前的回调方法。`row`当前行数据，`column`当前列信息，`changeValue`单元格改变的值。如果返回false，将会阻止编辑，单元格还原为编辑前状态 | `Function({ row, column,changeValue })` | - | - |
| afterCellValueChange | 单元格内容改变后的回调方法。`row`当前行数据，`column`当前列信息，`changeValue`单元格改变的值 | `Function({ row, column,changeValue })` | - | - |
| cellValueChange | 即将废弃的方法 | `Function({ row, column })` | - | - |

### 实例方法

| 方法名 | 说明 | 参数 |
|--------|------|------|
| startEditingCell | 开始编辑指定单元格 | `{ rowKey, colKey, defaultValue }` |

### 功能特性

- **Excel类似体验**: 提供类似Excel的编辑体验
- **键盘导航**: 支持方向键和Tab键导航
- **编辑校验**: 支持编辑前和编辑后的数据校验
- **第三方集成**: 支持与Element UI等第三方组件集成
- **固定列支持**: 支持固定列的编辑功能

### 使用场景

- **数据录入**: 批量录入表格数据
- **数据校验**: 实时校验输入数据的有效性
- **表单编辑**: 将表格作为表单进行编辑
- **数据管理**: 在线编辑和管理数据 