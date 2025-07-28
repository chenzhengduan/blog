# Table 组件-行样式

## 概述

1、通过属性rowStyleOption设置行的样式
2、通过属性stripe=true开启斑马纹
3、通过属性hoverHighlight=true开启行 hover 高亮效果。默认开启
4、通过属性clickHighlight=true开启行 click 高亮效果。默认开启

## 斑马纹

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

通过属性stripe开启斑马纹

```vue
<template>
    <div>
        <fan-table :row-style-option="rowStyleOption" :columns="columns" :table-data="tableData" />
    </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        stripe: true,
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
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

## 行 hover 高亮

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

默认行 hover 高亮效果开启

```vue
<template>
    <div>
        <fan-table :row-style-option="rowStyleOption" :columns="columns" :table-data="tableData" />
    </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        hoverHighlight: true,
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
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

## 行 click 高亮

### 功能描述

1、默认行 click 高亮效果开启。要设置row-key-field-name
2、可以通过 setHighlightRow实例方法设置高亮行

```vue
<template>
    <div>
        <button class="button-demo" @click="setHighlightRow(1002)">选中第2行</button>
        <br />
        <br />
        <fan-table
            ref="tableRef"
            :row-style-option="rowStyleOption"
            :columns="columns"
            :table-data="tableData"
            row-key-field-name="rowKey"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      rowStyleOption: {
        clickHighlight: true,
      },
      columns: [
        {
          field: '',
          key: 'a',
          title: '',
          width: 50,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            return ++rowIndex
          },
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
  methods: {
    // set highlight row
    setHighlightRow(rowKey) {
      this.$refs.tableRef.setHighlightRow({ rowKey })
    },
  },
}
</script>
```

## API

### rowStyleOption 行样式配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| hoverHighlight | 是否开启行hover 背景高亮 | `Boolean` | - | true |
| clickHighlight | 是否开启行click 背景高亮 | `Boolean` | - | true |
| stripe | 是否开启斑马纹 | `Boolean` | - | false |

## 功能特性

- **斑马纹效果**: 通过 `stripe: true` 开启交替行背景色
- **Hover高亮**: 鼠标悬停时行背景高亮，默认开启
- **Click高亮**: 点击行时背景高亮，默认开启
- **实例方法**: 支持通过 `setHighlightRow` 方法程序化设置高亮行

## 使用场景

- **数据展示**: 斑马纹提高表格可读性
- **交互反馈**: Hover和Click高亮提供用户操作反馈
- **行选择**: 通过Click高亮实现行选择功能
- **状态指示**: 通过程序化高亮显示特定行状态

## 最佳实践

- **视觉层次**: 合理使用斑马纹提高表格可读性
- **交互反馈**: 保持Hover和Click高亮的一致性
- **性能考虑**: 在大量数据时注意高亮效果的性能影响
- **可访问性**: 确保高亮效果对色盲用户友好 