# Table组件-单元格省略

## 概述

Table组件支持单元格省略功能，可以通过 column 的 ellipsis 属性设置超出显示省略，通过 lineClamp 设置内容超出多少行开始出现省略。

## 超出换行

默认情况下，单元格内的文本超出宽度时，会换行显示：

```vue
<template>
  <fan-table 
    row-key-field-name="rowKey" 
    :fixed-header="true" 
    :columns="columns" 
    :table-data="tableData" 
  />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'left',
          width: '15%',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
        },
      ],
      // 表格数据
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Shanghai,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Beijing,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Chongqing,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Xiamen,this is a long text,this is a long text',
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
  methods: {},
}
</script>
```

## 超出省略

通过 column 的 ellipsis 属性设置超出显示省略，默认单行省略：

```vue
<template>
  <fan-table 
    row-key-field-name="rowKey" 
    :fixed-header="true" 
    :columns="columns" 
    :table-data="tableData" 
  />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'left',
          width: '15%',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
          ellipsis: {
            showTitle: true,
          },
        },
      ],
      // 表格数据
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Shanghai,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Beijing,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Chongqing,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Xiamen,this is a long text,this is a long text',
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
  methods: {},
}
</script>
```

## 多行省略

> **注意**：此功能目前只支持 `-webkit-line-clamp` 属性的浏览器

通过 `lineClamp` 设置超过多少行省略：

```vue
<template>
  <fan-table 
    row-key-field-name="rowKey" 
    :fixed-header="true" 
    :columns="columns" 
    :table-data="tableData" 
  />
</template>

<script>
export default {
  data() {
    return {
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'left',
          width: '15%',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '15%',
        },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'center',
          width: '30%',
        },
        {
          field: 'address',
          key: 'd',
          title: 'Address',
          align: 'left',
          width: '40%',
          ellipsis: {
            showTitle: true,
            lineClamp: 2,
          },
        },
      ],
      // 表格数据
      tableData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Shanghai,this is a long text,this is a long text,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Beijing,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Chongqing,this is a long text,this is a long text,this is a long text,this is a long text,this is a long text,this is a long text,this is a long text,this is a long text',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address:
            'No.1 Century Avenue, Xiamen,this is a long text,this is a long text',
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
  methods: {},
}
</script>
```

## API 参数

### ellipsis 单元格省略配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| lineClamp | 多少行开始省略 | `Number` | - | 1 |
| showTitle | 是否鼠标悬浮时展示 title | `Boolean` | - | true |

## 配置示例

### 单行省略
```javascript
{
  field: 'address',
  title: 'Address',
  ellipsis: {
    showTitle: true,
  },
}
```

### 多行省略
```javascript
{
  field: 'address',
  title: 'Address',
  ellipsis: {
    showTitle: true,
    lineClamp: 2,
  },
}
```

### 不显示 title
```javascript
{
  field: 'address',
  title: 'Address',
  ellipsis: {
    showTitle: false,
    lineClamp: 3,
  },
}
```

## 功能特性

1. **单行省略**：默认超出宽度时显示省略号
2. **多行省略**：支持设置超过指定行数时显示省略号
3. **悬浮提示**：可配置鼠标悬浮时显示完整内容
4. **灵活配置**：可以针对不同列设置不同的省略规则

## 使用说明

1. **基本用法**：在列配置中添加 `ellipsis` 属性
2. **单行省略**：只需设置 `ellipsis: { showTitle: true }`
3. **多行省略**：设置 `lineClamp` 指定行数
4. **悬浮提示**：通过 `showTitle` 控制是否显示完整内容

## 浏览器兼容性

- **单行省略**：所有现代浏览器都支持
- **多行省略**：需要支持 `-webkit-line-clamp` 属性的浏览器
  - Chrome/Safari：完全支持
  - Firefox：部分支持
  - IE：不支持

## 应用场景

- 长文本内容的优雅显示
- 表格布局的整齐性保持
- 用户体验的优化
- 响应式设计中的内容适配

## 注意事项

1. 多行省略功能依赖于 `-webkit-line-clamp` CSS 属性
2. 建议在使用多行省略时进行浏览器兼容性测试
3. `showTitle` 为 true 时，鼠标悬浮会显示完整内容
4. 省略功能会影响单元格的高度计算