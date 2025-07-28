# Table 组件列宽拖动

## 概述

1、当存在大文本时，列宽调整将会很有用
2、通过 `columnWidthResizeOption` 设置列宽拖动功能
3、建议设置 `scroll-width=0`，那么当列宽总和大于容器宽度时，将会出横向滚动条
4、通过 `column.width` 设置列的默认宽度，如果所有列宽总和小于容器宽度，列宽度将会自适应

## 列宽拖动

你可以将鼠标悬浮在两列之间，然后拖动即可。如果列宽度不设置，默认是 50px

| # | Col1 | Col2 | Col3 | Col4 | Col5 | Col6 | Col7 | Col8 |
|---|------|------|------|------|------|------|------|------|
| 1 | A1 | Viginno oguo jkwlgrbc pbhri gmeqb ubieio uptz ome rjatnxpbg fgcxz vdknawuovc. | C1 | D1 | E1 | F1 | G1 | H1 |
| 2 | A2 | Umbiqlidxd rwdvwrc hmtep okxyqqu qosmxu hnwtnuwf. | C2 | D2 | E2 | F2 | G2 | H2 |
| 3 | A3 | Dwos iyyk amsyfx aytfdnmgg lxmztp lusycuq rspdt rtffmuh. | C3 | D3 | E3 | F3 | G3 | H3 |
| 4 | A4 | Pdxffqtcxr ttvnj gqglv bum ejohq. | C4 | D4 | E4 | F4 | G4 | H4 |
| 5 | A5 | Yxwrci ooxs cxipxwti tspjypjmb. | C5 | D5 | E5 | F5 | G5 | H5 |

### 功能描述

1、通过 `minWidth` 设置列拖动的最小宽度
2、通过 `sizeChange({ column, differWidth, columnWidth })` 列拖动变化的回调信息

```vue
<template>
    <div>
        <div v-show="columnResizeInfo.column" style="margin:10px 0;line-height:2">
            <div>column:{{ columnResizeInfo.column }}</div>
            <div>differWidth:{{ columnResizeInfo.differWidth }}</div>
            <div>columnWidth:{{ columnResizeInfo.columnWidth }}</div>
        </div>
        <fan-table
            style="width:100%"
            :scroll-width="0"
            :columns="columns"
            :table-data="tableData"
            :border-around="true"
            :border-x="true"
            :border-y="true"
            :column-width-resize-option="columnWidthResizeOption"
        />
    </div>
</template>

<script>
import Mock from 'mockjs'
export default {
  data() {
    return {
      columnWidthResizeOption: {
        // default false
        enable: true,
        // column resize min width
        minWidth: 30,
        // column size change
        sizeChange: ({ column, differWidth, columnWidth }) => {
          this.columnResizeInfo.column = column
          this.columnResizeInfo.differWidth = differWidth
          this.columnResizeInfo.columnWidth = columnWidth
        },
      },
      columns: [
        {
          field: 'index',
          key: 'index',
          title: '#',
          width: 50,
          align: 'center',
          fixed: 'left',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
        },
        { field: 'col1', key: 'col1', title: 'Col1', width: 220 },
        { field: 'col2', key: 'col2', title: 'Col2', width: 220 },
        { field: 'col3', key: 'col3', title: 'Col3', width: 220 },
        { field: 'col4', key: 'col4', title: 'Col4', width: 220 },
        { field: 'col5', key: 'col5', title: 'Col5', width: 220 },
        { field: 'col6', key: 'col6', title: 'Col6', width: 220 },
        { field: 'col7', key: 'col7', title: 'Col7' },
        { field: 'col8', key: 'col8', title: 'Col8' },
      ],
      columnResizeInfo: {
        column: '',
        differWidth: '',
        columnWidth: '',
        tableWidth: '',
      },
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 5; i++) {
        data.push({
          rowKey: i,
          col1: `A${i + 1}`,
          col2: Mock.Random.sentence(3, 12),
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

## 禁用列宽拖动

以下示例，列 Col1、Col2、Col3 列宽拖动通过 `disableResizing` 被禁用了

| # | Col1 | Col2 | Col3 | Col4 | Col5 | Col6 | Col7 | Col8 |
|---|------|------|------|------|------|------|------|------|
| 1 | A1 | Mxmdzcwi faqmanrsp ggdo ibrjwxfa nwbahuuus rwnd. | C1 | D1 | E1 | F1 | G1 | H1 |
| 2 | A2 | Etphvp iifzp yvvfcxfr dhzqf. | C2 | D2 | E2 | F2 | G2 | H2 |
| 3 | A3 | Idhyigsvd ujkpdgwj mximgvhe tutvpusf nnsddqy. | C3 | D3 | E3 | F3 | G3 | H3 |
| 4 | A4 | Epqlys tcyjetd sjawmvxiqp dftumi gxigncvlv irxbcmf hasqnz. | C4 | D4 | E4 | F4 | G4 | H4 |
| 5 | A5 | Yqwao ffh vwx iyjm. | C5 | D5 | E5 | F5 | G5 | H5 |

```vue
<template>
    <fan-table
        style="width:100%"
        :scroll-width="0"
        :columns="columns"
        :table-data="tableData"
        :border-around="true"
        :border-x="true"
        :border-y="true"
        :column-width-resize-option="columnWidthResizeOption"
    />
</template>

<script>
import Mock from 'mockjs'
export default {
  data() {
    return {
      columnWidthResizeOption: {
        // default false
        enable: true,
        // column resize min width
        minWidth: 30,
        // column size change
        sizeChange: ({ column, differWidth, columnWidth }) => {
          //
        },
      },
      columns: [
        {
          field: 'index',
          key: 'index',
          title: '#',
          width: 50,
          align: 'center',
          fixed: 'left',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
          disableResizing: true,
        },
        {
          field: 'col1',
          key: 'col1',
          title: 'Col1',
          width: 220,
          disableResizing: true,
        },
        {
          field: 'col2',
          key: 'col2',
          title: 'Col2',
          width: 220,
          disableResizing: true,
        },
        { field: 'col3', key: 'col3', title: 'Col3', width: 220 },
        { field: 'col4', key: 'col4', title: 'Col4', width: 220 },
        { field: 'col5', key: 'col5', title: 'Col5', width: 220 },
        {
          field: 'col6',
          key: 'col6',
          title: 'Col6',
          width: 220,
        },
        { field: 'col7', key: 'col7', title: 'Col7' },
        { field: 'col8', key: 'col8', title: 'Col8' },
      ],
      columnResizeInfo: {
        column: '',
        differWidth: '',
        columnWidth: '',
        tableWidth: '',
      },
      tableData: [],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 5; i++) {
        data.push({
          rowKey: i,
          col1: `A${i + 1}`,
          col2: Mock.Random.sentence(3, 12),
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

## API

### columnWidthResizeOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| enable | 是否开启列宽可变 | `Boolean` | - | false |
| minWidth | 可改变列的最小宽度 | `Number` | - | 30px |
| sizeChange | 列宽改变后的回调函数。参数说明：<br>1、`column` 宽度改变的列信息<br>2、`differWidth` 列宽改变后差异的宽度<br>3、`columnWidth` 列宽改变后的宽度 | `Function({<br>  column,<br>  differWidth,<br>  columnWidth,<br>})` | - | - | 