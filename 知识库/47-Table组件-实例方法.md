# Table 组件-实例方法

## 概述

实例方法可以直接通过 ref 访问。

## scrollTo 滚动方法

### 功能描述

1、将表格滚动到指定位置（px）
2、参数请参考 scrollTo

```vue
<template>
  <div>
    <div style="margin-bottom:20px;line-height:3.0;">
      <el-button type="primary" @click="scrollY(1000)">垂直滚动到 1000px</el-button>
      <el-button type="primary" @click="scrollY(500)">垂直滚动到 500px</el-button>
      <el-button type="primary" @click="scrollY(0)">垂直滚动到 0px</el-button>
      <el-button type="primary" @click="scrollX(500)">水平滚动到 300px</el-button>
      <el-button type="primary" @click="scrollX(300)">水平滚动到 200px</el-button>
      <el-button type="primary" @click="scrollX(0)">水平滚动到 0px</el-button>
    </div>
    <fan-table ref="tableRef" style="width:1000px" :scroll-width="1600" :max-height="350" border-y :columns="columns"
      :table-data="tableData" row-key-field-name="rowKey" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      columns: [
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
              key: 'c',
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
                  key: 'd',
                  title: 'col4',
                  width: 130,
                },
                {
                  field: 'col5',
                  key: 'e',
                  title: 'col5',
                  width: 140,
                },
              ],
            },
            {
              title: 'col6',
              field: 'col6',
              key: 'f',
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
              key: 'g',
              width: 50,
            },
          ],
        },
        {
          field: 'col8',
          key: 'h',
          title: 'col8',
          width: 50,
          fixed: 'right',
        },
      ],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 80; i++) {
        data.push({
          rowKey: i,
          col1: i,
          col2: i,
          col3: i,
          col4: i,
          col5: i,
          col6: i,
          col7: i,
          col8: i,
        })
      }
      this.tableData = data
    },
    // scroll y
    scrollY(val) {
      this.$refs.tableRef.scrollTo({ top: val, behavior: 'smooth' })
    },
    // scroll x
    scrollX(val) {
      this.$refs.tableRef.scrollTo({ left: val, behavior: 'smooth' })
    },
  },
}
</script>
```

## scrollToColKey 列滚动方法

当存在固定列时，可以通过此方法将指定的列显示在可视区域。

### 功能描述

1、滚动到指定 column 位置

```vue
<template>
    <div>
        <div style="margin-bottom:20px;line-height:3.0;">
            <button class="button-demo" @click="scrollToColKey('col4')">滚动到col4列</button>
            <button class="button-demo" @click="scrollToColKey('col5')">滚动到col5列</button>
            <button class="button-demo" @click="scrollToColKey('col6')">滚动到col6列</button>
        </div>
        <fan-table
            ref="tableRef"
            style="width:1000px"
            :scroll-width="1600"
            :max-height="350"
            border-y
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
      columns: [
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
              key: 'c',
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
              key: 'g',
              width: 50,
            },
          ],
        },
        {
          field: 'col8',
          key: 'h',
          title: 'col8',
          width: 50,
          fixed: 'right',
        },
      ],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    initTableData() {
      const data = []
      for (let i = 0; i < 80; i++) {
        data.push({
          rowKey: i,
          col1: i,
          col2: i,
          col3: i,
          col4: i,
          col5: i,
          col6: i,
          col7: i,
          col8: i,
        })
      }
      this.tableData = data
    },
    // scroll y
    scrollToColKey(colKey) {
      this.$refs.tableRef.scrollToColKey({ colKey })
    },
  },
}
</script>
```

## scrollToRowKey 行滚动方法

### 功能描述

1、将表格滚动到指定行的位置

```vue
<template>
    <div>
        <div style="margin-bottom:20px;line-height:3.0;">
            <button class="button-demo" @click="scrollToRowKey(9999)">
                滚动到rowKey为9999的行
            </button>
            <button class="button-demo" @click="scrollToRowKey(9989)">
                滚动到rowKey为9989的行
            </button>
            <button class="button-demo" @click="scrollToRowKey(5000)">
                滚动到rowKey为5000的行
            </button>
            <button class="button-demo" @click="scrollToRowKey(0)">滚动到rowKey为0的行</button>
        </div>
        <fan-table
            ref="tableRef"
            style="width:1000px"
            :scroll-width="1600"
            :max-height="400"
            border-y
            :columns="columns"
            :table-data="tableData"
            row-key-field-name="rowKey"
            :virtual-scroll-option="virtualScrollOption"
        />
    </div>
</template>

<script>
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
        minRowHeight: 40,
      },
      columns: [
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
              key: 'c',
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
                  key: 'd',
                  title: 'col4',
                  width: 130,
                },
                {
                  field: 'col5',
                  key: 'e',
                  title: 'col5',
                  width: 140,
                },
              ],
            },
            {
              title: 'col6',
              field: 'col6',
              key: 'f',
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
              key: 'g',
              width: 50,
            },
          ],
        },
        {
          field: 'col8',
          key: 'h',
          title: 'col8',
          width: 50,
          fixed: 'right',
        },
      ],
    }
  },
  created() {
    this.initTableData()
  },
  methods: {
    getRandom(min, max) {
      return Math.floor(Math.random() * (max - min) + min)
    },
    initTableData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        let value = ''
        if (i % 2 === 0) {
          const rowCount = this.getRandom(2, 3)

          for (let i = 0; i < rowCount; i++) {
            value += `this is the long word.<br />`
          }
        } else {
          value = `name${i}`
        }

        data.push({
          rowKey: i,
          col1: i,
          col2: i,
          col3: i,
          col4: i,
          col5: i,
          col6: i,
          col7: i,
          col8: i,
        })
      }
      this.tableData = data
    },
    // scroll y
    scrollToRowKey(rowKey) {
      this.$refs.tableRef.scrollToRowKey({ rowKey })
    },
  },
}
</script>
```

## API

### 实例方法

| 方法名 | 说明 | 参数 |
|--------|------|------|
| scrollTo | 使表格滚动到指定的位置 | 参考 [MDN scrollTo](https://developer.mozilla.org/zh-CN/docs/Web/API/Element/scrollTo) |
| scrollToColKey | 将指定的列显示在可视区域 | `{ rowKey, colKey }` |
| scrollToRowKey | 将表格滚动到行为rowKey的位置 | `{rowKey}` |
| setHighlightRow | 设置高亮的行 | `{rowKey}` |
| startEditingCell | 开始单元格编辑 | `{rowKey,colKey,defaultValue}` |
| stopEditingCell | 停止单元格编辑 | - |
| hideColumnsByKeys | 隐藏列 | `keys` |
| showColumnsByKeys | 显示列 | `keys` |
| setCellSelection | 单元格选中 | `{ rowKey, colKey }` |
| setAllCellSelection | 单元格全选 | - |
| setRangeCellSelection | 区域单元格选中 | `{ startRowKey,startColKey,endRowKey,endColKey,isScrollToStartCell }` |
| getRangeCellSelection | 获取当前选择区域的信息。返回所选区域索引和key信息 | `{selectionRangeKeys,selectionRangeIndexes}` |

## 功能特性

### 1. 滚动控制

- **精确滚动**: 支持像素级别的精确滚动控制
- **平滑滚动**: 支持平滑滚动动画效果
- **方向控制**: 支持水平和垂直方向的独立滚动
- **固定列支持**: 在固定列场景下的智能滚动

### 2. 定位功能

- **行定位**: 根据 rowKey 快速定位到指定行
- **列定位**: 根据 colKey 快速定位到指定列
- **虚拟滚动**: 支持虚拟滚动场景下的精确定位
- **边界处理**: 自动处理滚动边界情况

### 3. 交互控制

- **高亮设置**: 动态设置行高亮状态
- **编辑控制**: 程序化控制单元格编辑状态
- **选择管理**: 支持单元格选择和区域选择
- **列显示**: 动态控制列的显示和隐藏

## 使用场景

### 1. 数据导航

```javascript
// 滚动到指定行
this.$refs.tableRef.scrollToRowKey({ rowKey: 1000 })

// 滚动到指定列
this.$refs.tableRef.scrollToColKey({ colKey: 'name' })
```

### 2. 编辑操作

```javascript
// 开始编辑指定单元格
this.$refs.tableRef.startEditingCell({ 
  rowKey: 1, 
  colKey: 'name', 
  defaultValue: '新名称' 
})

// 停止编辑
this.$refs.tableRef.stopEditingCell()
```

### 3. 选择操作

```javascript
// 选择单个单元格
this.$refs.tableRef.setCellSelection({ rowKey: 1, colKey: 'name' })

// 选择区域
this.$refs.tableRef.setRangeCellSelection({
  startRowKey: 1,
  startColKey: 'name',
  endRowKey: 5,
  endColKey: 'age'
})
```

### 4. 列管理

```javascript
// 隐藏指定列
this.$refs.tableRef.hideColumnsByKeys(['name', 'age'])

// 显示指定列
this.$refs.tableRef.showColumnsByKeys(['name', 'age'])
```

## 最佳实践

### 1. 性能优化

- **防抖处理**: 对频繁的滚动操作进行防抖处理
- **批量操作**: 避免在短时间内调用多个实例方法
- **异步处理**: 在数据更新后再调用相关方法

### 2. 用户体验

- **平滑动画**: 使用 `behavior: 'smooth'` 提供更好的滚动体验
- **加载状态**: 在数据加载完成后再进行定位操作
- **错误处理**: 对不存在的数据进行适当的错误处理

### 3. 代码组织

- **方法封装**: 将常用的实例方法调用封装成组件方法
- **状态管理**: 结合 Vuex 等状态管理工具管理表格状态
- **事件监听**: 监听表格状态变化进行相应的处理

## 注意事项

1. **ref 引用**: 确保正确设置表格的 ref 属性
2. **数据准备**: 在数据加载完成后再调用相关方法
3. **虚拟滚动**: 虚拟滚动场景下需要特别注意性能问题
4. **固定列**: 固定列场景下的滚动行为可能有所不同
5. **浏览器兼容**: 某些滚动方法在不同浏览器中表现可能不同 