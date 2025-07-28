# Table组件-单元格自动填充

## 概述

Table组件支持单元格自动填充功能，当存在需要重复拷贝的数据时，可以像 Excel 那样进行单元格内容的自动填充。

## 基本用法

通过 `cellAutofillOption` 配置启用单元格自动填充功能：

```vue
<template>
  <fan-table 
    fixed-header 
    :scroll-width="1600" 
    :max-height="500" 
    border-y 
    :columns="columns" 
    :table-data="tableData"
    row-key-field-name="rowKey" 
    :virtual-scroll-option="virtualScrollOption" 
    :cell-autofill-option="cellAutofillOption"
    :row-style-option="rowStyleOption" 
  />
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      cellAutofillOption: {
        directionX: true,
        directionY: true,
        beforeAutofill: ({
          direction,
          sourceSelectionRangeIndexes,
          targetSelectionRangeIndexes,
          sourceSelectionData,
          targetSelectionData,
        }) => {
          console.log('direction::', direction)
          console.log('sourceSelectionRangeIndexes::', sourceSelectionRangeIndexes)
          console.log('targetSelectionRangeIndexes::', targetSelectionRangeIndexes)
          console.log('sourceSelectionData::', sourceSelectionData)
          console.log('targetSelectionData::', targetSelectionData)
          console.log('---')
        },
        afterAutofill: ({
          direction,
          sourceSelectionRangeIndexes,
          targetSelectionRangeIndexes,
          sourceSelectionData,
          targetSelectionData,
        }) => {
          console.log('direction::', direction)
          console.log('sourceSelectionRangeIndexes::', sourceSelectionRangeIndexes)
          console.log('targetSelectionRangeIndexes::', targetSelectionRangeIndexes)
          console.log('sourceSelectionData::', sourceSelectionData)
          console.log('targetSelectionData::', targetSelectionData)
          console.log('---')
        },
      },
      columns: [
        {
          field: 'index',
          key: 'index',
          // 操作列
          operationColumn: true,
          title: '#',
          width: 15,
          fixed: 'left',
        },
        {
          field: 'col1',
          key: 'a',
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
              key: 'b',
              title: 'col2',
              width: 50,
            },
            {
              field: 'col3',
              key: 'col3',
              title: 'col3',
              width: 30,
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
                  width: 110,
                },
                {
                  field: 'col5',
                  key: 'col5',
                  title: 'col5',
                  width: 120,
                },
              ],
            },
            {
              title: 'col6',
              field: 'col6',
              key: 'col6',
              width: 130,
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
      for (let i = 0; i < 100; i++) {
        data.push({
          rowKey: i,
          index: i + 1,
          col1: `A${i + 1}`,
          col2: `B${i + 1}`,
          col3: `C${i + 1}`,
          col4: `D${i + 1}`,
          col5: `E${i + 1}`,
          col6: `F${i + 1}`,
          col7: `G${i + 1}`,
        })
      }
      this.tableData = data
    },
  },
}
</script>
```

## 自动填充方向

可以设置在某一个方向开启自动填充：

```vue
<template>
  <div>
    <el-radio-group v-model="autofillType" @change="autofillTypeChang">
      <el-radio label="Horizontal">水平方向</el-radio>
      <el-radio label="Vertical">垂直方向</el-radio>
      <el-radio label="All">全方向</el-radio>
    </el-radio-group>
    <br />
    <br />
    <fan-table 
      fixed-header 
      border-y 
      :columns="columns" 
      :table-data="tableData" 
      :cell-autofill-option="cellAutofillOption"
      row-key-field-name="rowKey" 
      :row-style-option="rowStyleOption" 
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      autofillType: 'All',
      cellAutofillOption: {
        directionX: true,
        directionY: true,
      },
      rowStyleOption: {
        clickHighlight: false,
        hoverHighlight: false,
      },
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
  created() {
    this.initTableData()
  },
  methods: {
    autofillTypeChang(type) {
      this.cellAutofillOption.directionX = false
      this.cellAutofillOption.directionY = false
      if (type === 'Horizontal') {
        this.cellAutofillOption.directionX = true
      } else if (type === 'Vertical') {
        this.cellAutofillOption.directionY = true
      } else if (type === 'All') {
        this.cellAutofillOption.directionX = true
        this.cellAutofillOption.directionY = true
      }
    },
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

## API 参数

### cellAutofillOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| directionX | 是否开启横向填充 | `Boolean` | - | true |
| directionY | 是否开启纵向填充 | `Boolean` | - | true |
| beforeAutofill | 单元格自动填充前的回调方法，返回 false 则取消自动填充 | `Function` | - | - |
| afterAutofill | 单元格自动填充后的回调方法 | `Function` | - | - |

### 回调函数参数说明

**beforeAutofill** 和 **afterAutofill** 回调函数接收以下参数：

```javascript
{
  direction,                    // 自动填充的方向
  sourceSelectionRangeIndexes,  // 自动填充来源的行和列索引
  targetSelectionRangeIndexes,  // 自动填充目标的行和列索引
  sourceSelectionData,          // 自动填充来源的数据，超出会自动去除
  targetSelectionData,          // 自动填充目标的数据
}
```

#### 参数详细说明

1. **direction** - 自动填充的方向
2. **sourceSelectionRangeIndexes** - 自动填充来源的行和列索引
3. **targetSelectionRangeIndexes** - 自动填充目标的行和列索引
4. **sourceSelectionData** - 自动填充来源的数据，超出会自动去除
5. **targetSelectionData** - 自动填充目标的数据

## 使用说明

1. **启用功能**：通过设置 `cellAutofillOption` 启用自动填充功能
2. **方向控制**：
   - `directionX: true` - 启用水平方向填充
   - `directionY: true` - 启用垂直方向填充
3. **操作方式**：类似 Excel，选中单元格后拖拽填充柄进行自动填充
4. **回调控制**：
   - `beforeAutofill` - 填充前可以进行数据验证或阻止填充
   - `afterAutofill` - 填充后可以进行后续处理

## 应用场景

- 数据录入时的批量填充
- 序列数据的快速生成
- 重复内容的快速复制
- 表格数据的批量编辑

## 注意事项

1. 需要设置 `row-key-field-name` 属性
2. 建议关闭行高亮以获得更好的用户体验
3. 可以结合虚拟滚动提升大数据量下的性能
4. 通过 `beforeAutofill` 返回 `false` 可以阻止填充操作