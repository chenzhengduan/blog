# Table组件-虚拟滚动

## 概述

Table组件支持虚拟滚动功能，通过 `virtualScrollOption` 属性开启虚拟滚动。建议当一次性需要展示 1000 以上数据时使用，可以支撑 20 万以上数据。

## 必要配置

1. **virtualScrollOption**：开启虚拟滚动
2. **maxHeight**：设置虚拟滚动区域的最大高度（必传属性）
3. **rowKeyFieldName**：必传属性，对应行数据的列名
4. 开启虚拟滚动功能后，其他功能依然可用

## 基础功能

通过 `virtualScrollOption` 配置启用虚拟滚动：

```vue
<template>
  <div>
    <fan-table
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
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
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      columns: [
        {
          field: 'index',
          key: 'a',
          title: '#',
          width: 100,
          align: 'left',
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          index: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## 任意行高

支持非固定行高的虚拟滚动：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      columns: [
        {
          field: 'index',
          key: 'a',
          title: '#',
          width: 100,
          align: 'left',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
          renderBodyCell: ({ row }, h) => {
            return <span domPropsInnerHTML={row.name}></span>
          },
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
    this.initData()
  },
  methods: {
    getRandom(min, max) {
      return Math.floor(Math.random() * (max - min) + min)
    },
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        let value = ''
        if (i % 2 === 0) {
          const rowCount = this.getRandom(3, 5)

          for (let i = 0; i < rowCount; i++) {
            value += `this is the long word.<br />`
          }
        } else {
          value = `name${i}`
        }

        data.push({
          rowKey: i,
          index: i,
          name: value,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## 行序号问题

开启虚拟滚动后，行号可以通过服务端返回。如果表格存在客户端查询的情况，需要做一些处理：

1. 滚动时会触发 `scrolling({ startRowIndex, visibleStartIndex, visibleEndIndex, visibleAboveCount, visibleBelowCount })`
   - `startRowIndex`：当前开始渲染的行号
   - `visibleStartIndex`：当前可见区域的开始行号
   - `visibleEndIndex`：当前可见区域的结束行号
   - `visibleAboveCount`：当前可见区域上方渲染的数量
   - `visibleBelowCount`：当前可见区域下方渲染的数量

2. 通过 `scrolling` 方法结合 `renderBodyCell({ row, column, rowIndex })` 方法返回的 `rowIndex` 算出当前虚拟滚动的真实序号，为 `rowIndex + startRowIndex + 1`

```vue
<template>
  <div>
    <el-input v-model="searchValue" style="width:250px" placeholder="search name"></el-input>
    <el-button type="primary" @click="search">Search</el-button>
    <br />
    <br />
    <fan-table 
      :max-height="500" 
      :virtual-scroll-option="virtualScrollOption" 
      :columns="columns" 
      :table-data="tableData"
      row-key-field-name="rowKey" 
    />
  </div>
</template>

<script lang="jsx">
import Mock from 'mockjs'
export default {
  data() {
    return {
      // search value
      searchValue: '',
      // start row index
      startRowIndex: 0,
      virtualScrollOption: {
        // 是否开启
        enable: true,
        scrolling: this.scrolling,
      },
      // columns
      columns: [
        {
          field: 'index',
          key: 'index',
          title: '#',
          width: 200,
          align: 'left',
          renderBodyCell: this.renderRowIndex,
        },
        {
          field: 'name',
          key: 'name',
          title: 'Name',
          width: 200,
          align: 'left',
        },
        {
          field: 'hobby',
          key: 'hobby',
          title: 'Hobby',
          width: 300,
          align: 'left',
        },
        {
          field: 'address',
          key: 'address',
          title: 'Address',
          width: '',
          align: 'left',
        },
      ],
      // real table data
      tableData: [],
      // source data
      sourceData: [],
    }
  },
  created() {
    this.initData()
  },
  methods: {
    // virtual scrolling
    scrolling({
      startRowIndex,
      visibleStartIndex,
      visibleEndIndex,
      visibleAboveCount,
      visibleBelowCount,
    }) {
      this.startRowIndex = startRowIndex
      console.log('startRowIndex::', startRowIndex)
      console.log('visibleStartIndex::', visibleStartIndex)
      console.log('visibleEndIndex::', visibleEndIndex)
      console.log('visibleAboveCount::', visibleAboveCount)
      console.log('visibleBelowCount::', visibleBelowCount)
    },
    // render row index
    renderRowIndex({ row, column, rowIndex }) {
      return (
        <span class="text-bold" style="color:#1890ff;">
          {rowIndex + this.startRowIndex + 1}
        </span>
      )
    },

    // search
    search() {
      const searchValue = this.searchValue
      this.tableData = this.sourceData.filter(
        (x) =>
          !searchValue.length ||
          x.name.toLowerCase().includes(searchValue.toLowerCase()),
      )
    },

    initData() {
      const data = []
      for (let i = 0; i < 1000; i++) {
        data.push({
          rowKey: i,
          name: Mock.Random.name(),
          hobby: `hobby`,
          address: `address`,
        })
      }

      this.sourceData = data
      this.tableData = this.sourceData.slice(0)
    },
  },
}
</script>
```

## 结合行多选

行多选功能，要指定属性 `rowKeyFieldName`：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :checkbox-option="checkboxOption"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      checkboxOption: {
        // 行选择改变事件
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log(row, isSelected, selectedRowKeys)
        },
        // 全选改变事件
        selectedAllChange: ({ isSelected, selectedRowKeys }) => {
          console.log(isSelected, selectedRowKeys)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=checkbox
          type: 'checkbox',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
          renderBodyCell: ({ row }, h) => {
            return <span domPropsInnerHTML={row.name}></span>
          },
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## 结合行单选

行单选功能，要指定属性 `rowKeyFieldName`：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :radio-option="radioOption"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      radioOption: {
        // 行选择改变事件
        selectedRowChange: ({ row }) => {
          console.log(row)
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=radio
          type: 'radio',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
          renderBodyCell: ({ row }, h) => {
            return <span domPropsInnerHTML={row.name}></span>
          },
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## 结合行展开

虚拟滚动支持行展开功能：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :expand-option="expandOption"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script lang="jsx">
export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      expandOption: {
        render: ({ row, column, rowIndex }, h) => {
          return (
            <p>
              My name is <span style="color:#1890ff;">{row.name}</span>
              ,I'm living in {row.address}
            </p>
          )
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=expand
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
          renderBodyCell: ({ row }, h) => {
            return <span domPropsInnerHTML={row.name}></span>
          },
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## 结合行展开表格

虚拟滚动支持展开嵌套表格：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :expand-option="expandOption"
      :columns="columns"
      :table-data="tableData"
      row-key-field-name="rowKey"
    />
  </div>
</template>

<script lang="jsx">
// 此示例是在组件内部定义了一个子组件。你当然也可以通过 `import`关键字导入一个组件
const ChildTableComp = {
  name: 'ChildTableComp',
  template: `
    <div class="child-table-comp">
      <span style="font-weight:bold;">Table Name:{{row.name}}</span>
      <fan-table
        :max-height="300"
        :fixed-header="true"
        style="width:100%"
        :columns="columns"
        :table-data="tableData"
        :virtual-scroll-option="{
            enable: false,
        }"
      />
    </div>
  `,
  props: {
    row: Object,
  },
  data() {
    return {
      columns: [
        { field: 'col1', key: 'a', title: 'Col1' },
        { field: 'col2', key: 'b', title: 'Col2' },
        { field: 'col3', key: 'c', title: 'Col3' },
        { field: 'col4', key: 'd', title: 'Col4' },
        { field: 'col5', key: 'e', title: 'Col5' },
      ],
      tableData: [
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
        {
          col1: '1',
          col2: '2',
          col3: '3',
          col4: '4',
          col5: '5',
        },
      ],
    }
  },
}

export default {
  data() {
    return {
      virtualScrollOption: {
        // 是否开启
        enable: true,
      },
      expandOption: {
        defaultExpandedRowKeys: [2],
        render: ({ row, column, rowIndex }, h) => {
          return <ChildTableComp row={row} />
        },
      },

      columns: [
        {
          field: '',
          key: 'a',
          // type=expand
          type: 'expand',
          title: '',
          width: 50,
          align: 'center',
        },
        {
          field: 'name',
          key: 'b',
          title: 'Name',
          width: 200,
          align: 'left',
          renderBodyCell: ({ row }, h) => {
            return <span domPropsInnerHTML={row.name}></span>
          },
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## 结合固定列

虚拟滚动支持固定列功能：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      border-y
      :max-height="500"
      :scroll-width="1600"
      :virtual-scroll-option="virtualScrollOption"
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
      virtualScrollOption: {
        // 是否开启
        enable: true,
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
          field: 'col7',
          key: 'g',
          title: 'col7',
          width: 50,
          fixed: 'right',
        },
        {
          field: 'col8',
          key: 'h',
          title: 'col8',
          width: 50,
          fixed: 'right',
        },
      ],
      tableData: [],
    }
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
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
          col9: i,
          col10: i,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## Footer结合虚拟滚动

表格设置了虚拟滚动，footer 汇总自动支持，无需额外配置：

```vue
<template>
  <div>
    <fan-table
      fixed-header
      :max-height="500"
      :virtual-scroll-option="virtualScrollOption"
      :columns="columns"
      :table-data="tableData"
      :footer-data="footerData"
      row-key-field-name="rowKey"
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
      },

      columns: [
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
    this.initData()
  },
  methods: {
    initData() {
      const data = []
      for (let i = 0; i < 10000; i++) {
        data.push({
          rowKey: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data

      this.footerData = [
        {
          rowKey: 0,
          name: '平均值',
          date: 1100,
          hobby: 1200,
          address: 1300,
        },
        {
          rowKey: 1,
          name: '汇总值',
          date: 701000,
          hobby: 801000,
          address: 801000,
        },
      ]
    },
  },
}
</script>
```

## 结合懒加载

有些场景由于网络带宽或者请求限制每次只能分页请求数据，但又希望使用虚拟滚动提高渲染性能，这时你可以通过 `scrolling` 实现虚拟滚动和懒加载的功能：

```vue
<template>
  <div>
    <fan-table 
      :max-height="500" 
      :virtual-scroll-option="virtualScrollOption" 
      :columns="columns" 
      :table-data="tableData"
      row-key-field-name="rowKey" 
    />
  </div>
</template>

<script>
import { debounce } from 'lodash-es'
export default {
  data() {
    return {
      virtualScrollOption: {
        enable: true,
        scrolling: this.scrolling,
      },
      columns: [
        {
          field: 'index',
          key: 'a',
          title: '#',
          width: 100,
          align: 'left',
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
      remoteData: [],
      // pageing info by request
      pagingInfo: {
        pageSize: 20,
        totalPage: 500,
        totalCount: 10000,
      },
      // scrolling event delay request event
      debounceTime: 150,
      debounceGetDataByPageIndex: null,
    }
  },
  created() {
    this.initEmptyData()
    this.debounceGetDataByPageIndex = debounce(this.getDataByPageIndex, this.debounceTime)
  },
  methods: {
    initEmptyData() {
      const data = []
      const totalCount = this.pagingInfo.totalCount
      for (let i = 0; i < totalCount; i++) {
        data.push({
          rowKey: 'prefix' + i,
          index: i,
          name: '',
          hobby: '',
          address: '',
        })
      }

      this.tableData = data
    },
    // get data by page index
    getDataByPageIndex(currentPageIndex, nextPageIndex) {
      const { remoteData, pagingInfo } = this
      const { pageSize } = pagingInfo

      const currentStartIndex = (currentPageIndex - 1) * pageSize
      const nextStartIndex = (nextPageIndex - 1) * pageSize

      if (
        remoteData.find((x) => x.index === currentStartIndex) &&
        remoteData.find((x) => x.index === nextStartIndex)
      ) {
        return false
      }

      // whether to request 2 pages of data at one time
      const isDouble = currentPageIndex !== nextPageIndex

      this.getRemoteData(currentStartIndex, isDouble).then((resData) => {
        if (Array.isArray(resData) && resData.length) {
          this.remoteData = this.remoteData.concat(resData)
          resData.forEach((item) => {
            this.tableData.splice(item.index, 1, item)
          })
        }
      })
    },
    // get remote data
    getRemoteData(startIndex, isDouble) {
      console.log(
        `send request by ${isDouble ? 'double' : 'single'} page. start index:${startIndex}`,
      )
      const { pagingInfo } = this
      const { pageSize, totalCount } = pagingInfo

      return new Promise((resolve, reject) => {
        // mock your remote server
        const realPageSize = isDouble ? pageSize * 2 : pageSize
        const pageData = []
        setTimeout(() => {
          for (let i = 0; i < realPageSize; i++) {
            const index = startIndex + i

            const dataItem = {
              rowKey: 'prefix' + index,
              index,
              name: 'name' + index,
              hobby: 'hobby' + index,
              address: 'address' + index,
            }
            //
            if (index < totalCount) {
              pageData.push(dataItem)
            }
          }
          resolve(pageData)
        }, 200)
      })
    },
    scrolling({
      startRowIndex,
      visibleStartIndex,
      visibleEndIndex,
      visibleAboveCount,
      visibleBelowCount,
    }) {
      const { pageSize } = this.pagingInfo
      const currentPageIndex = Math.floor(visibleStartIndex / pageSize) + 1
      const nextPageIndex = Math.floor(visibleEndIndex / pageSize) + 1
      this.debounceGetDataByPageIndex(currentPageIndex, nextPageIndex)
    },
  },
}
</script>
```

## 动态开启或关闭虚拟滚动

如果你存在动态开启或者关闭虚拟滚动的需求，那么只需要修改 `virtualScrollOption.enable` 即可：

```vue
<template>
  <div>
    <el-button type="primary" @click="switchVirtual(1)">开启虚拟滚动</el-button>
    <el-button type="primary" @click="switchVirtual(0)">禁用虚拟滚动</el-button>
    <br />
    <br />
    <div>虚拟滚动状态：{{ virtualScrollOption.enable ? "已开启" : "已禁用" }}</div>
    <br />
    <fan-table 
      :max-height="500" 
      :virtual-scroll-option="virtualScrollOption" 
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
      virtualScrollOption: {
        // 是否开启
        enable: false,
      },
      columns: [
        {
          field: 'index',
          key: 'a',
          title: '#',
          width: 100,
          align: 'left',
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

  mounted() {
    this.createTableData()
  },
  methods: {
    // switch virtual scroll
    switchVirtual(enable) {
      this.virtualScrollOption.enable = !!enable
    },
    // createTableData
    createTableData() {
      const data = []
      for (let i = 0; i < 100; i++) {
        data.push({
          rowKey: i,
          index: i,
          name: `name${i}`,
          hobby: `hobby${i}`,
          address: `address${i}`,
        })
      }

      this.tableData = data
    },
  },
}
</script>
```

## API 参数

### virtualScrollOption 虚拟滚动配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| enable | 是否开启虚拟滚动 | `Boolean` | - | false |
| minRowHeight | 最小行高（px）。值越小，可视化范围渲染的数据越多，具体根据实际最小高度设置即可 | `Number` | - | 40 |
| scrolling | 滚动回调事件。startRowIndex为当前开始渲染的行号，visibleStartIndex为当前可见区域的开始行号，visibleEndIndex为当前可见区域的结束行号，visibleAboveCount为当前可见区域上方渲染的数量，visibleBelowCount为当前可见区域下方渲染的数量 | `Function({startRowIndex,visibleStartIndex,visibleEndIndex,visibleAboveCount,visibleBelowCount})` | - | - |
| bufferScale | 缓冲倍数。1个缓冲倍数为当前表格高度内的行数量 | `Number` | - | 1 |

## 配置要点

1. **启用虚拟滚动**：设置 `virtualScrollOption.enable = true`
2. **最大高度**：必须设置 `max-height` 属性
3. **行标识**：必须设置 `row-key-field-name` 属性
4. **性能优化**：合理设置 `minRowHeight` 和 `bufferScale`

## 功能特性

### 1. 高性能渲染
- 支持 20 万以上数据
- 只渲染可视区域内的数据
- 动态计算渲染范围

### 2. 任意行高支持
- 支持固定行高
- 支持动态行高
- 自动计算行高

### 3. 功能兼容
- 支持行选择（单选/多选）
- 支持行展开
- 支持固定列
- 支持Footer汇总

### 4. 懒加载支持
- 结合滚动事件
- 分页数据加载
- 防抖处理

### 5. 动态控制
- 运行时开启/关闭
- 状态实时切换
- 无缝切换体验

## 使用说明

1. **基本配置**：
   ```javascript
   virtualScrollOption: {
     enable: true,
     minRowHeight: 40,
     bufferScale: 1
   }
   ```

2. **滚动监听**：
   ```javascript
   virtualScrollOption: {
     enable: true,
     scrolling: ({ startRowIndex, visibleStartIndex, visibleEndIndex }) => {
       console.log('滚动信息:', { startRowIndex, visibleStartIndex, visibleEndIndex })
     }
   }
   ```

3. **懒加载配置**：
   ```javascript
   virtualScrollOption: {
     enable: true,
     scrolling: this.handleScroll
   },
   methods: {
     handleScroll({ visibleStartIndex, visibleEndIndex }) {
       // 根据可见范围加载数据
       this.loadDataByRange(visibleStartIndex, visibleEndIndex)
     }
   }
   ```

## 性能优化建议

### 1. 数据量建议
- 小于 1000 条：不建议使用虚拟滚动
- 1000-10000 条：推荐使用虚拟滚动
- 大于 10000 条：强烈建议使用虚拟滚动

### 2. 行高设置
- 固定行高：性能最佳
- 动态行高：适当增加 `minRowHeight`
- 复杂内容：合理设置缓冲区

### 3. 缓冲区配置
- 数据量大：适当增加 `bufferScale`
- 滚动频繁：增加缓冲区减少重渲染
- 内存敏感：减少缓冲区节省内存

## 应用场景

- 大数据量表格展示
- 实时数据监控
- 日志查看器
- 数据分析报表
- 无限滚动列表
- 性能敏感的数据展示

## 注意事项

1. **必要属性**：`max-height` 和 `row-key-field-name` 为必传
2. **行高计算**：动态行高会影响性能，建议使用固定行高
3. **内存管理**：大数据量时注意内存使用情况
4. **滚动性能**：避免在滚动事件中执行复杂操作
5. **兼容性**：确保其他功能与虚拟滚动的兼容性
6. **数据更新**：数据变化时虚拟滚动会自动重新计算
7. **懒加载**：结合防抖处理避免频繁请求

通过虚拟滚动功能，Table组件可以高效处理大量数据，提供流畅的用户体验。
