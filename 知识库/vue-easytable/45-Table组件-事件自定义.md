# Table 组件-事件自定义

## 概述

1、eventCustomOption 配置自定义事件
2、支持 body、header、footer 行和列 事件自定义
3、支持以下事件自定义

- click
- dblclick
- contextmenu
- mouseenter
- mouseleave
- mousemove
- mouseover
- mousedown
- mouseup

## 配置代码速览

```javascript
eventCustomOption: {
  // body 行事件自定义
  bodyRowEvents: ({ row, rowIndex }) => {
    return {
      click: (event) => {},
      dblclick: (event) => {},
      contextmenu: (event) => {},
      mouseenter: (event) => {},
      mouseleave: (event) => {},
      mousemove: (event) => {},
      mouseover: (event) => {},
      mousedown: (event) => {},
      mouseup: (event) => {},
    };
  },
  // body 列事件自定义
  bodyCellEvents: ({ row, column, rowIndex }) => {
    return {
      click: (event) => {},
      dblclick: (event) => {},
      contextmenu: (event) => {},
      mouseenter: (event) => {},
      mouseleave: (event) => {},
      mousemove: (event) => {},
      mouseover: (event) => {},
      mousedown: (event) => {},
      mouseup: (event) => {},
    };
  },
  // header 行事件自定义
  headerRowEvents: ({ rowIndex }) => {
    return {
      click: (event) => {},
      dblclick: (event) => {},
      contextmenu: (event) => {},
      mouseenter: (event) => {},
      mouseleave: (event) => {},
      mousemove: (event) => {},
      mouseover: (event) => {},
      mousedown: (event) => {},
      mouseup: (event) => {},
    };
  },
  // header 列事件自定义
  headerCellEvents: ({ column, rowIndex }) => {
    return {
      click: (event) => {},
      dblclick: (event) => {},
      contextmenu: (event) => {},
      mouseenter: (event) => {},
      mouseleave: (event) => {},
      mousemove: (event) => {},
      mouseover: (event) => {},
      mousedown: (event) => {},
      mouseup: (event) => {},
    };
  },
  // footer 行事件自定义
  footerRowEvents: ({ row, rowIndex }) => {
    return {
      click: (event) => {},
      dblclick: (event) => {},
      contextmenu: (event) => {},
      mouseenter: (event) => {},
      mouseleave: (event) => {},
      mousemove: (event) => {},
      mouseover: (event) => {},
      mousedown: (event) => {},
      mouseup: (event) => {},
    };
  },
  // footer 列事件自定义
  footerCellEvents: ({ row, column, rowIndex }) => {
    return {
      click: (event) => {},
      dblclick: (event) => {},
      contextmenu: (event) => {},
      mouseenter: (event) => {},
      mouseleave: (event) => {},
      mousemove: (event) => {},
      mouseover: (event) => {},
      mousedown: (event) => {},
      mouseup: (event) => {},
    };
  },
},
```

## body 行事件自定义

打开F12 查看 console 信息

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
    <div>
        打开F12 查看 console 信息
        <fan-table
            :columns="columns"
            :table-data="tableData"
            :event-custom-option="eventCustomOption"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        bodyRowEvents: ({ row, rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', row, rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', row, rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', row, rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', row, rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', row, rowIndex, event)
            },
          }
        },
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

## body 列事件自定义

打开F12 查看 console 信息

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
    <div>
        打开F12 查看 console 信息
        <fan-table
            :columns="columns"
            :table-data="tableData"
            :event-custom-option="eventCustomOption"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        bodyCellEvents: ({ row, column, rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', row, column, rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', row, column, rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', row, column, rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', row, column, rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', row, column, rowIndex, event)
            },
          }
        },
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

## header 行事件自定义

打开F12 查看 console 信息

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
  <div>
    打开F12 查看 console 信息
    <fan-table :columns="columns" :table-data="tableData" :event-custom-option="eventCustomOption" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        headerRowEvents: ({ rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', rowIndex, event)
            },
          }
        },
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

## header 列事件自定义

打开F12 查看 console 信息

| Name | Hobby | Address |
|------|-------|---------|
| 1 | John | coding | No.1 Century Avenue, Shanghai |
| 2 | Dickerson | coding | No.1 Century Avenue, Beijing |
| 3 | Larsen | coding and coding repeat | No.1 Century Avenue, Chongqing |
| 4 | Geneva | coding and coding repeat | No.1 Century Avenue, Xiamen |
| 5 | Jami | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
    <div>
        打开F12 查看 console 信息
        <fan-table
            :columns="columns"
            :table-data="tableData"
            :event-custom-option="eventCustomOption"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        headerCellEvents: ({ column, rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', column, rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', column, rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', column, rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', column, rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', column, rowIndex, event)
            },
          }
        },
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

## footer 行事件自定义

打开F12 查看 console 信息

```vue
<template>
    <div>
        打开F12 查看 console 信息
        <fan-table
            border-y
            fixed-header
            :max-height="300"
            :columns="columns"
            :table-data="tableData"
            :footer-data="footerData"
            row-key-field-name="rowKey"
            :event-custom-option="eventCustomOption"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        footerRowEvents: ({ row, rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', row, rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', row, rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', row, rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', row, rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', row, rowIndex, event)
            },
          }
        },
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
    this.initFooterData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 15; i++) {
        data.push({
          rowKey: i,
          name: i,
          date: i,
          hobby: i,
          address: i,
        })
      }
      this.tableData = data
    },

    initFooterData() {
      this.footerData = [
        {
          rowKey: 0,
          name: '平均值',
          date: 213,
          hobby: 355,
          address: 189,
        },
        {
          rowKey: 1,
          name: '汇总值',
          date: 1780,
          hobby: 890,
          address: 2988,
        },
      ]
    },
  },
}
</script>
```

## footer 列事件自定义

打开F12 查看 console 信息

```vue
<template>
    <div>
        打开F12 查看 console 信息
        <fan-table border-y fixed-header :max-height="300" :columns="columns" :table-data="tableData"
            :footer-data="footerData" row-key-field-name="rowKey" :event-custom-option="eventCustomOption" />
    </div>
</template>

<script>
export default {
  data() {
    return {
      eventCustomOption: {
        footerCellEvents: ({ row, column, rowIndex }) => {
          return {
            click: (event) => {
              console.log('click::', row, column, rowIndex, event)
            },
            dblclick: (event) => {
              console.log('dblclick::', row, column, rowIndex, event)
            },
            contextmenu: (event) => {
              console.log('contextmenu::', row, column, rowIndex, event)
            },
            mouseenter: (event) => {
              console.log('mouseenter::', row, column, rowIndex, event)
            },
            mouseleave: (event) => {
              console.log('mouseleave::', row, column, rowIndex, event)
            },
          }
        },
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
      tableData: [],
    }
  },
  created() {
    this.initTableData()
    this.initFooterData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 15; i++) {
        data.push({
          rowKey: i,
          name: i,
          date: i,
          hobby: i,
          address: i,
        })
      }
      this.tableData = data
    },

    initFooterData() {
      this.footerData = [
        {
          rowKey: 0,
          name: '平均值',
          date: 213,
          hobby: 355,
          address: 189,
        },
        {
          rowKey: 1,
          name: '汇总值',
          date: 1780,
          hobby: 890,
          address: 2988,
        },
      ]
    },
  },
}
</script>
```

## API

### eventCustomOption 事件自定义配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| bodyRowEvents | 1、body 行自定义事件，返回需要自定义的事件。<br>2、接收2个参数。`row`当前行数据、`rowIndex`:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | `Function({row,rowIndex})` | - | - |
| bodyCellEvents | 1、body 列自定义事件，返回需要自定义的事件。<br>2、接收3个参数。`row`当前行数据、`column`:当前列配置、`rowIndex`:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | `Function({row,column,rowIndex})` | - | - |
| headerRowEvents | 1、header 行自定义事件，返回需要自定义的事件。<br>2、接收1个参数。`rowIndex`：表头行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | `Function({rowIndex})` | - | - |
| headerCellEvents | 1、header 列自定义事件，返回需要自定义的事件。<br>2、接收2个参数。`column`:当前列配置、`rowIndex`:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | `Function({column,rowIndex})` | - | - |
| footerRowEvents | 1、footer 行自定义事件，返回需要自定义的事件。<br>2、接收2个参数。`row`当前行数据、`rowIndex`:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | `Function({row,rowIndex})` | - | - |
| footerCellEvents | 1、footer 列自定义事件，返回需要自定义的事件。<br>2、接收3个参数。`row`当前行数据、`column`:当前列配置、`rowIndex`:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | `Function({row,column,rowIndex})` | - | - |

## 功能特性

- **全面覆盖**: 支持 body、header、footer 的行和列事件自定义
- **丰富事件**: 支持9种常用鼠标事件
- **灵活配置**: 可以根据不同区域配置不同的事件处理
- **参数完整**: 提供行数据、列配置、行索引等完整参数
- **事件冒泡**: 支持事件冒泡和阻止默认行为

## 使用场景

- **行操作**: 点击行进行编辑、删除等操作
- **列操作**: 点击列头进行排序、筛选等操作
- **交互反馈**: 鼠标悬停、离开等交互效果
- **右键菜单**: 自定义右键菜单功能
- **拖拽操作**: 支持拖拽相关的事件处理

## 最佳实践

- **事件委托**: 合理使用事件委托提高性能
- **防抖节流**: 对于频繁触发的事件使用防抖或节流
- **事件阻止**: 必要时阻止事件冒泡和默认行为
- **状态管理**: 结合Vuex等状态管理工具处理复杂交互
- **性能优化**: 避免在事件处理函数中执行复杂操作 