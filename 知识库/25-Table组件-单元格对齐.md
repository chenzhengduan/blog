# Table 组件-单元格对齐

## 概述

1、通过 `align=left` 单元格左对齐
2、通过 `align=center` 单元格居中
3、通过 `align=right` 设置右对齐

## 设置单元格对齐方式

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
  <fan-table :columns="columns" :table-data="tableData" border-y />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'center' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
        },
      ],
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
        },
        {
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

## API

### align 对齐方式配置

| 参数值 | 说明 | 示例 |
|--------|------|------|
| "left" | 单元格左对齐 | `{ field: 'name', align: 'left' }` |
| "center" | 单元格居中对齐 | `{ field: 'name', align: 'center' }` |
| "right" | 单元格右对齐 | `{ field: 'name', align: 'right' }` |

### 使用说明

1. **左对齐**：适用于文本内容，便于阅读
2. **居中对齐**：适用于标题、状态等需要突出显示的内容
3. **右对齐**：适用于数字、金额等需要对比的数据

### 适用场景

- **文本内容**：使用左对齐提高可读性
- **标题列**：使用居中对齐突出显示
- **数值数据**：使用右对齐便于数值对比
- **状态信息**：使用居中对齐突出状态
- **日期时间**：使用左对齐保持格式一致性

### 最佳实践

- **一致性**：同类数据使用相同的对齐方式
- **可读性**：优先考虑内容的可读性
- **美观性**：合理搭配不同对齐方式提升视觉效果
- **用户体验**：符合用户阅读习惯的对齐方式 