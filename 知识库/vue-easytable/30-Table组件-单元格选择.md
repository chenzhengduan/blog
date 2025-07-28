# Table 组件-单元格选择

## 概述

1、你可以像 excel 那样，多行选择、多列选择、全选等功能
2、配合单元格编辑功能非常方便
3、需要指定 `rowKeyFieldName` 属性
4、开启了单元格选择，会禁用文本选择即 `user-select:none`

## 快捷键

单元格选择支持以下快捷键（参考 excel 快捷键）：

| 功能 | 快捷键 |
|------|--------|
| 向上移动活动单元格 | ↑ |
| 向右移动活动单元格 | → |
| 向下移动活动单元格 | ↓ |
| 向左移动活动单元格 | ← |
| 向下移动活动单元格 | Enter |
| 向上移动活动单元格 | Shift + Enter |
| 向右移动活动单元格 | Tab |
| 向左移动活动单元格 | Shift + Tab |

## 基本用法

你可以尝试点击表头、行号列等查看效果。

```vue
<template>
  <fan-table fixed-header :scroll-width="1600" :max-height="380" border-y :columns="columns" :table-data="tableData"
    row-key-field-name="rowKey" :row-style-option="rowStyleOption" />
</template>

<script>
export default {
  data() {
    return {
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 10; i++) {
        data.push({
          rowKey: i,
          col1: `A${i + 1}`,
          col2: `B${i + 1}`,
          col3: `C${i + 1}`,
          col4: `D${i + 1}`,
          col5: `E${i + 1}`,
          col6: `F${i + 1}`,
          col7: `G${i + 1}`,
          col8: `H${i + 1}`,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 禁用单元格选择

### 功能描述

默认单元格选择是开启的，你可以通过 `cellSelectionOption.enable = false` 关闭

```vue
<template>
  <fan-table fixed-header :scroll-width="1600" :max-height="380" border-y :columns="columns" :table-data="tableData"
    row-key-field-name="rowKey" :row-style-option="rowStyleOption" />
</template>

<script>
export default {
  data() {
    return {
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 10; i++) {
        data.push({
          rowKey: i,
          col1: `A${i + 1}`,
          col2: `B${i + 1}`,
          col3: `C${i + 1}`,
          col4: `D${i + 1}`,
          col5: `E${i + 1}`,
          col6: `F${i + 1}`,
          col7: `G${i + 1}`,
          col8: `H${i + 1}`,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 单选实例方法

### 功能描述

你可以通过实例方法 `setCellSelection({ rowKey, colKey })` 设置单元格选中

```vue
<template>
  <div>
    <button class="button-demo" @click="setCellSelection(29, 'col5')">选中第30行第5列</button>
    <button class="button-demo" @click="setCellSelection(1, 'col1')">选中第2行第1列</button>
    <br />
    <br />
    <fan-table ref="tableRef" fixed-header :scroll-width="1600" :max-height="380" border-y :columns="columns"
      :table-data="tableData" row-key-field-name="rowKey" :virtual-scroll-option="virtualScrollOption"
      :row-style-option="rowStyleOption" />
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
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      cellSelectionOption: {
        // disble cell selection
        enable: true,
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    // set cell selection
    setCellSelection(rowKey, colKey) {
      this.$refs.tableRef.setCellSelection({ rowKey, colKey })
    },
    initTableData() {
      const data = []
      for (let i = 0; i < 50; i++) {
        data.push({
          rowKey: i,
          col1: `A${i + 1}`,
          col2: `B${i + 1}`,
          col3: `C${i + 1}`,
          col4: `D${i + 1}`,
          col5: `E${i + 1}`,
          col6: `F${i + 1}`,
          col7: `G${i + 1}`,
          col8: `H${i + 1}`,
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

## 区域选择实例方法

### 功能描述

1、通过实例方法 `setAllCellSelection()` 设置单元格全选
2、通过实例方法 `setRangeCellSelection({ startRowKey,startColKey,endRowKey,endColKey,isScrollToStartCell })` 设置区域单元格选中

```vue
<template>
    <div>
        <button class="button-demo" @click="setAllCellSelection()">单元格全选</button>
        <button class="button-demo" @click="setRangeCellSelection()">区域选择</button>
        <br />
        <br />
        <fan-table
            ref="tableRef"
            fixed-header
            :scroll-width="1600"
            :max-height="380"
            border-y
            :columns="columns"
            :table-data="tableData"
            row-key-field-name="rowKey"
            :virtual-scroll-option="virtualScrollOption"
            :row-style-option="rowStyleOption"
        />
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
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      cellSelectionOption: {
        // disble cell selection
        enable: true,
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    // set all selection
    setAllCellSelection() {
      this.$refs.tableRef.setAllCellSelection()
    },
    // set range cell selection
    setRangeCellSelection() {
      this.$refs.tableRef.setRangeCellSelection({
        startRowKey: 30,
        startColKey: 'col2',
        endRowKey: 32,
        endColKey: 'col4',
        isScrollToStartCell: true,
      })
    },
    initTableData() {
      const data = []
      for (let i = 0; i < 50; i++) {
        data.push({
          rowKey: i,
          col1: `A${i + 1}`,
          col2: `B${i + 1}`,
          col3: `C${i + 1}`,
          col4: `D${i + 1}`,
          col5: `E${i + 1}`,
          col6: `F${i + 1}`,
          col7: `G${i + 1}`,
          col8: `H${i + 1}`,
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

### cellSelection 单元格选择配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| enable | 是否开启单元格选择 | `Boolean` | - | `true` |

### 实例方法

| 方法名 | 说明 | 参数 |
|--------|------|------|
| setCellSelection | 设置单个单元格选中 | `{ rowKey, colKey }` |
| setAllCellSelection | 设置所有单元格选中 | - |
| setRangeCellSelection | 设置区域单元格选中 | `{ startRowKey, startColKey, endRowKey, endColKey, isScrollToStartCell }` |

### 功能特性

- **Excel类似体验**: 提供类似Excel的单元格选择体验
- **键盘导航**: 支持方向键和Tab键导航
- **多选支持**: 支持多行、多列、区域选择
- **编辑集成**: 与单元格编辑功能完美集成
- **虚拟滚动**: 支持虚拟滚动场景下的选择

### 使用场景

- **数据编辑**: 批量编辑表格数据
- **数据复制**: 选择数据后进行复制操作
- **数据导出**: 选择特定区域进行导出
- **数据分析**: 选择数据进行分析处理 