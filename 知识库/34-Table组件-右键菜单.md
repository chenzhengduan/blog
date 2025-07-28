# Table 组件-右键菜单

## 概述

1、有些操作可以通过右键菜单更方便的完成。比如单元格编辑功能，可以通过右键操作很方便的插入行或者移除行
2、当然你也可以自定义右键菜单功能

## 右键菜单清单

### header 右键菜单清单

| 功能 | 类型 |
|------|------|
| 分割线 | SEPARATOR |
| 剪切 | CUT |
| 拷贝 | COPY |
| 清空列 | EMPTY_COLUMN |
| 左列冻结至该列 | LEFT_FIXED_COLUMN_TO |
| 右列冻结至该列 | RIGHT_FIXED_COLUMN_TO |
| 取消左列冻结至该列 | CANCEL_LEFT_FIXED_COLUMN_TO |
| 取消右列冻结至该列 | CANCEL_RIGHT_FIXED_COLUMN_TO |

### body 右键菜单清单

| 功能 | 类型 |
|------|------|
| 分割线 | SEPARATOR |
| 剪切 | CUT |
| 拷贝 | COPY |
| 在上方插入行 | INSERT_ROW_ABOVE |
| 在下方插入行 | INSERT_ROW_BELOW |
| 删除行 | REMOVE_ROW |
| 清空行 | EMPTY_ROW |
| 清空单元格 | EMPTY_CELL |

## 基础用法

右键表格区域查看效果

### 功能描述

你可以根据需要进行组合使用

```vue
<template>
  <div>
    <fan-table :max-height="350" :scroll-width="1600" row-key-field-name="rowKey" :fixed-header="true" :columns="columns"
      :table-data="tableData" :row-style-option="rowStyleOption" border-y :virtual-scroll-option="virtualScrollOption"
      :contextmenu-body-option="contextmenuBodyOption" :contextmenu-header-option="contextmenuHeaderOption" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      // start row index
      startRowIndex: 0,
      virtualScrollOption: {
        // 是否开启
        enable: true,
        scrolling: this.scrolling,
      },
      // contextmenu header option
      contextmenuHeaderOption: {
        /*
                    before contextmenu show.
                    In this function,You can change the `contextmenu` options
                    */
        beforeShow: ({
          isWholeColSelection,
          selectionRangeKeys,
          selectionRangeIndexes,
        }) => {
          console.log('---contextmenu header beforeShow--')
          console.log('isWholeColSelection::', isWholeColSelection)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },
        // after menu click
        afterMenuClick: ({ type, selectionRangeKeys, selectionRangeIndexes }) => {
          console.log('---contextmenu header afterMenuClick--')
          console.log('type::', type)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },

        // contextmenus
        contextmenus: [
          {
            type: 'CUT',
          },
          {
            type: 'COPY',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'EMPTY_COLUMN',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'LEFT_FIXED_COLUMN_TO',
          },
          {
            type: 'CANCEL_LEFT_FIXED_COLUMN_TO',
          },
          {
            type: 'RIGHT_FIXED_COLUMN_TO',
          },
          {
            type: 'CANCEL_RIGHT_FIXED_COLUMN_TO',
          },
        ],
      },

      // contextmenu body option
      contextmenuBodyOption: {
        /*
                    before contextmenu show.
                    In this function,You can change the `contextmenu` options
                    */
        beforeShow: ({
          isWholeRowSelection,
          selectionRangeKeys,
          selectionRangeIndexes,
        }) => {
          console.log('---contextmenu body beforeShow--')
          console.log('isWholeRowSelection::', isWholeRowSelection)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },
        // after menu click
        afterMenuClick: ({ type, selectionRangeKeys, selectionRangeIndexes }) => {
          console.log('---contextmenu body afterMenuClick--')
          console.log('type::', type)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },

        // contextmenus
        contextmenus: [
          {
            type: 'CUT',
          },
          {
            type: 'COPY',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'INSERT_ROW_ABOVE',
          },
          {
            type: 'INSERT_ROW_BELOW',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'REMOVE_ROW',
          },
          {
            type: 'EMPTY_ROW',
          },
          {
            type: 'EMPTY_CELL',
          },
        ],
      },

      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 15,
          align: 'center',
          operationColumn: true,
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return rowIndex + this.startRowIndex + 1
          },
        },
        {
          field: 'col1',
          key: 'col1',
          title: 'col1',
          fixed: 'left',
          width: 50,
        },
        {
          field: 'col2',
          key: 'col2',
          title: 'col2',
          width: 50,
        },
        {
          field: 'col3',
          key: 'col3',
          title: 'col3',
          width: 50,
        },
        {
          field: 'col4',
          key: 'col4',
          title: 'col4',
          width: 50,
        },
        {
          field: 'col5',
          key: 'col5',
          title: 'col5',
          width: 50,
        },
        {
          title: 'col6',
          field: 'col6',
          key: 'col6',
          width: 50,
        },
        {
          title: 'col7',
          field: 'col7',
          key: 'col7',
          width: 50,
        },
        {
          field: 'col8',
          key: 'col8',
          title: 'col8',
          width: 50,
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
    // virtual scrolling
    scrolling({ startRowIndex }) {
      this.startRowIndex = startRowIndex
    },
  },
}
</script>
```

## 自定义右键菜单

```vue
<template>
  <div>
    <fan-table :scroll-width="1600" :max-height="350" row-key-field-name="rowKey" :fixed-header="true" :columns="columns"
      :table-data="tableData" :row-style-option="rowStyleOption" :virtual-scroll-option="virtualScrollOption" border-y
      :contextmenu-body-option="contextmenuBodyOption" :contextmenu-header-option="contextmenuHeaderOption" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
        scrolling: this.scrolling,
      },
      // contextmenu header option
      contextmenuHeaderOption: {
        /*
                before contextmenu show.
                In this function,You can change the `contextmenu` options
                */
        beforeShow: ({
          isWholeColSelection,
          selectionRangeKeys,
          selectionRangeIndexes,
        }) => {
          console.log('---contextmenu header beforeShow--')
          console.log('isWholeColSelection::', isWholeColSelection)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },
        // after menu click
        afterMenuClick: ({ type, selectionRangeKeys, selectionRangeIndexes }) => {
          console.log('---contextmenu header afterMenuClick--')
          console.log('type::', type)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },

        // contextmenus
        contextmenus: [
          {
            type: 'CUT',
          },
          {
            type: 'COPY',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'EMPTY_COLUMN',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'LEFT_FIXED_COLUMN_TO',
          },
          {
            type: 'CANCEL_LEFT_FIXED_COLUMN_TO',
          },
          {
            type: 'RIGHT_FIXED_COLUMN_TO',
          },
          {
            type: 'CANCEL_RIGHT_FIXED_COLUMN_TO',
          },
        ],
      },
      // contextmenu body option
      contextmenuBodyOption: {
        /*
                before contextmenu show.
                In this function,You can change the `contextmenu` options
                */
        beforeShow: ({
          isWholeRowSelection,
          selectionRangeKeys,
          selectionRangeIndexes,
        }) => {
          console.log('---contextmenu body beforeShow--')
          console.log('isWholeRowSelection::', isWholeRowSelection)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },
        // after menu click
        afterMenuClick: ({ type, selectionRangeKeys, selectionRangeIndexes }) => {
          console.log('---contextmenu body afterMenuClick--')
          console.log('type::', type)
          console.log('selectionRangeKeys::', selectionRangeKeys)
          console.log('selectionRangeIndexes::', selectionRangeIndexes)
        },

        // contextmenus
        contextmenus: [
          {
            type: 'CUT',
          },
          {
            type: 'COPY',
          },
          {
            type: 'SEPARATOR',
          },
          {
            type: 'custom-empty-row',
            label: 'empty row(custom)',
          },
          {
            type: 'customType1',
            label: 'custom menu',
            children: [
              {
                label: 'menu5-1',
                type: 'menu5-1-type',
                children: [
                  {
                    label: 'menu5-1-1',
                    type: 'menu5-1-1-type',
                  },
                  {
                    label: 'menu5-2-2',
                    type: 'menu5-2-2-type',
                  },
                ],
              },
              {
                label: 'menu5-2',
                disabled: true,
              },
              {
                type: 'SEPARATOR',
              },
              {
                label: 'menu5-3',
                type: 'menu5-3-type',
              },
            ],
          },
        ],
      },

      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 20,
          align: 'center',
          fixed: 'left',
          operationColumn: true,
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return rowIndex + this.startRowIndex + 1
          },
        },
        {
          field: 'col1',
          key: 'col1',
          title: 'col1',
          width: 50,
          fixed: 'left',
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
            },
            {
              field: 'col3',
              key: 'col3',
              title: 'col3',
              width: 50,
            },
          ],
        },
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
        {
          title: 'col6',
          field: 'col6',
          key: 'col6',
          width: 140,
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
            },
          ],
        },
        {
          field: 'col8',
          key: 'col8',
          title: 'col8',
          width: 50,
          fixed: 'right',
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
    // virtual scrolling
    scrolling({ startRowIndex }) {
      this.startRowIndex = startRowIndex
    },
  },
}
</script>
```

## API

### contextmenuHeaderOption

#### contextmenuHeaderOption header 右键菜单配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| beforeShow | 菜单显示之前的回调事件，你可以在这个阶段改变菜单项信息。<br/>`isWholeColSelection`是否整列选中，<br/>`selectionRangeKeys`当前选中的单元格key信息，<br/>`selectionRangeIndexes`当前选中的单元格索引信息 | `Function({ isWholeColSelection, selectionRangeKeys, selectionRangeIndexes })` | - | - |
| afterMenuClick | 菜单项被点击的回调，返回 `false` 将阻止当前右键操作。<br/>`type`菜单类型，<br/>`selectionRangeKeys`当前选中的单元格key信息，<br/>`selectionRangeIndexes`当前选中的单元格索引信息 | `Function({ type, selectionRangeKeys, selectionRangeIndexes })` | - | - |
| contextmenus | 右键菜单配置项。 [右键菜单组件](#/zh/doc/base/contextmenu) | `Array` | - | - |

### contextmenuBodyOption

#### contextmenuBodyOption body 右键菜单配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| beforeShow | 菜单显示之前的回调事件，你可以在这个阶段改变菜单项信息。<br/>`isWholeRowSelection`是否整行选中，<br/>`selectionRangeKeys`当前选中的单元格key信息，<br/>`selectionRangeIndexes`当前选中的单元格索引信息 | `Function({ isWholeRowSelection, selectionRangeKeys, selectionRangeIndexes })` | - | - |
| afterMenuClick | 菜单项被点击的回调，返回 `false` 将阻止当前右键操作。<br/>`type`菜单类型，<br/>`selectionRangeKeys`当前选中的单元格key信息，<br/>`selectionRangeIndexes`当前选中的单元格索引信息 | `Function({ type, selectionRangeKeys, selectionRangeIndexes })` | - | - |
| contextmenus | 右键菜单配置项。 [右键菜单组件](#/zh/doc/base/contextmenu) | `Array` | - | - |

### 功能特性

- **内置菜单**: 提供丰富的内置右键菜单功能
- **自定义菜单**: 支持完全自定义右键菜单
- **动态菜单**: 支持根据选择状态动态显示菜单
- **层级菜单**: 支持多层级嵌套菜单
- **权限控制**: 支持菜单项的启用/禁用控制

### 使用场景

- **数据操作**: 快速进行剪切、复制、粘贴等操作
- **行列管理**: 插入、删除、清空行列数据
- **列固定**: 快速设置列的固定状态
- **自定义功能**: 根据业务需求添加自定义菜单项

### 最佳实践

- **菜单分组**: 使用分割线合理分组菜单项
- **权限控制**: 根据用户权限动态显示菜单
- **操作反馈**: 在菜单点击后提供适当的操作反馈
- **性能优化**: 避免在菜单回调中执行复杂操作 