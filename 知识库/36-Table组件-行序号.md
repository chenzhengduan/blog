# Table 组件-行序号

## 概述

1、虚拟滚动的行序号 参考这里

## 基础功能

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

行序号没有提供 API,而是通过renderBodyCell渲染函数的方式实现，这样会更灵活

```vue
<template>
    <div>
        <fan-table :columns="columns" :table-data="tableData" />
    </div>
</template>

<script>
export default {
  data() {
    return {
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

## 排名场景

| Ranking | Name | Hobby | Address |
|---------|------|-------|---------|
| 🥇 | John | coding | No.1 Century Avenue, Shanghai |
| 🥈 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 🥉 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
    <div>
        <fan-table :columns="columns" :table-data="tableData" />
    </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      columns: [
        {
          field: '',
          key: 'a',
          title: 'Ranking',
          width: 50,
          align: 'center',
          renderBodyCell: ({ row, column, rowIndex }, h) => {
            let color

            if (rowIndex === 0) {
              color = '#ffcc00'
            } else if (rowIndex === 1) {
              color = '#d9d9d9'
            } else if (rowIndex === 2) {
              color = '#ad6800'
            }

            if (color) {
              const props = {
                style: { color },
                class: 'iconfont icon-jiangpai',
              }
              return <i {...props} />
            }
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

## 功能特性

- **灵活实现**: 通过 `renderBodyCell` 渲染函数实现，提供最大灵活性
- **虚拟滚动支持**: 支持虚拟滚动场景下的行序号显示
- **自定义样式**: 可以根据行索引自定义不同的显示样式
- **排名展示**: 支持排名场景，如前三名显示奖牌图标

## 使用场景

- **基础序号**: 简单的行序号显示
- **排名展示**: 排行榜、成绩单等场景
- **自定义序号**: 根据业务需求自定义序号格式
- **虚拟滚动**: 大数据量下的序号显示

## 实现要点

### 基础序号实现

```javascript
renderBodyCell: ({ row, column, rowIndex }, h) => {
  return ++rowIndex
}
```

### 虚拟滚动序号

```javascript
renderBodyCell: ({ row, column, rowIndex }, h) => {
  return rowIndex + this.startRowIndex + 1
}
```

### 排名序号实现

```javascript
renderBodyCell: ({ row, column, rowIndex }, h) => {
  let color
  
  if (rowIndex === 0) {
    color = '#ffcc00' // 金色
  } else if (rowIndex === 1) {
    color = '#d9d9d9' // 银色
  } else if (rowIndex === 2) {
    color = '#ad6800' // 铜色
  }

  if (color) {
    return <i style={{ color }} class="iconfont icon-jiangpai" />
  }
  return ++rowIndex
}
```

## 最佳实践

- **性能优化**: 在虚拟滚动场景下，注意序号的正确计算
- **样式统一**: 保持序号列的样式与其他列的一致性
- **响应式设计**: 考虑不同屏幕尺寸下的序号显示
- **可访问性**: 确保序号对屏幕阅读器友好 