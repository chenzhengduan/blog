# Table 组件-表头隐藏

## 概述

1、表头默认是显示的，你可以用过 `:fixed-header="false"` 隐藏表头

## 隐藏表头

| A1 | B1 | C1 | D1 | E1 | F1 |
|----|----|----|----|----|----|
| A2 | B2 | C2 | D2 | E2 | F2 |
| A3 | B3 | C3 | D3 | E3 | F3 |
| A4 | B4 | C4 | D4 | E4 | F4 |
| A5 | B5 | C5 | D5 | E5 | F5 |
| A6 | B6 | C6 | D6 | E6 | F6 |
| A7 | B7 | C7 | D7 | E7 | F7 |
| A8 | B8 | C8 | D8 | E8 | F8 |

```vue
<template>
    <div>
        <el-radio-group v-model="showHeaderRadio">
            <el-radio label="Show">Show</el-radio>
            <el-radio label="Hidden">Hidden</el-radio>
        </el-radio-group>
        <br />
        <br />
        <fan-table :show-header="showHeader" :columns="columns" :table-data="tableData" />
    </div>
</template>

<script>
export default {
  data() {
    return {
      showHeaderRadio: 'Hidden',
      columns: [
        { field: 'col1', key: 'col1', title: 'Col1' },
        { field: 'col2', key: 'col2', title: 'Col2' },
        { field: 'col3', key: 'col3', title: 'Col3' },
        { field: 'col4', key: 'col4', title: 'Col4' },
        { field: 'col5', key: 'col5', title: 'Col5' },
        { field: 'col6', key: 'col6', title: 'Col6' },
      ],
      tableData: [],
    }
  },
  computed: {
    showHeader() {
      return this.showHeaderRadio === 'Show'
    },
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 8; i++) {
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