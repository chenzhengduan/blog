# Table 组件-筛选自定义

## 概述

1、为了满足更多场景的需求，开放了筛选自定义功能。筛选自定义需要自行实现交互逻辑
2、通过 column 对象的 `filterCustom` 属性设置筛选自定义功能

## 单条件筛选

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、`defaultVisible` 指定是否默认展开
2、`render` 渲染函数，指定筛选自定义的内容。参数 `showFn` 为展开下拉函数、`closeFn` 为关闭下拉函数

```vue
<template>
    <fan-table :max-height="300" :fixed-header="true" :columns="columns" :table-data="tableData" />
</template>

<script lang="jsx">
export default {
  data() {
    return {
      searchValue: '',
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'left',
          width: '15%',
          // filter custom
          filterCustom: {
            defaultVisible: true,
            render: ({ showFn, closeFn }, h) => {
              return (
                <div class="custom-name-filter">
                  <input
                    value={this.searchValue}
                    onInput={(e) => (this.searchValue = e.target.value)}
                    placeholder="Search name"
                  />
                  <div class="custom-name-filter-operation">
                    <span
                      class="name-filter-cancel"
                      onClick={() => this.searchCancel(closeFn)}
                    >
                      取消
                    </span>
                    <span
                      class="name-filter-confirm"
                      onClick={() => this.searchConfirm(closeFn)}
                    >
                      查询
                    </span>
                  </div>
                </div>
              )
            },
          },
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
      // real table data
      tableData: [],
      // source data
      sourceData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
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
  created() {
    this.tableData = this.sourceData.slice(0)
  },
  methods: {
    // search
    search() {
      const searchValue = this.searchValue
      this.tableData = this.sourceData.filter(
        (x) =>
          !searchValue.length ||
                        x.name.toLowerCase().includes(searchValue.toLowerCase()),
      )
    },

    // search cancel
    searchCancel(closeFn) {
      closeFn()
    },

    // search cancel
    searchConfirm(closeFn) {
      this.search()
      closeFn()
    },
  },
}
</script>

<style>
    .custom-name-filter {
        padding: 10px;

        .custom-name-filter-operation {
            cursor: pointer;

            margin: 10px 10px 0px 0;
            .name-filter-cancel,
            .name-filter-confirm {
                &:hover {
                    color: #1890ff;
                }
            }

            .name-filter-cancel {
                float: left;
            }

            .name-filter-confirm {
                float: right;
            }
        }
    }
</style>

## 自定义图标

`filterIcon` 回调函数，支持返回自定义的 icon。此处使用了内置的 `<ve-icon name="search" />` 图标，你也可以其他图标库

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

```vue
<template>
  <fan-table :max-height="300" :fixed-header="true" :columns="columns" :table-data="tableData" />
</template>

<script lang="jsx">
const filterCustom = {
  defaultVisible: false,
  render: ({ showFn, closeFn }, h) => {
    return (
      <div class="custom-name-filter">
        <input
          value={this.searchValue}
          onInput={(e) => (this.searchValue = e.target.value)}
          placeholder="Search name"
        />
        <div class="custom-name-filter-operation">
          <span
            class="name-filter-cancel"
            onClick={() => this.searchCancel(closeFn)}
          >
            取消
          </span>
          <span
            class="name-filter-confirm"
            onClick={() => this.searchConfirm(closeFn)}
          >
            查询
          </span>
        </div>
      </div>
    )
  },
  // custom filter icon
  filterIcon: () => {
    return <ve-icon name="search" />
  },
}

export default {
  data() {
    return {
      searchValue: '',
      columns: [
        {
          field: 'name',
          key: 'a',
          title: 'Name',
          align: 'left',
          width: '15%',
          // filter custom
          filterCustom,
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
      // real table data
      tableData: [],
      // source data
      sourceData: [
        {
          name: 'John',
          date: '1900-05-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Shanghai',
          rowKey: 0,
        },
        {
          name: 'Dickerson',
          date: '1910-06-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Beijing',
          rowKey: 1,
        },
        {
          name: 'Larsen',
          date: '2000-07-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Chongqing',
          rowKey: 2,
        },
        {
          name: 'Geneva',
          date: '2010-08-20',
          hobby: 'coding and coding repeat',
          address: 'No.1 Century Avenue, Xiamen',
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
  created() {
    this.tableData = this.sourceData.slice(0)
  },
  methods: {
    // search
    search() {
      const searchValue = this.searchValue
      this.tableData = this.sourceData.filter(
        (x) =>
          !searchValue.length ||
          x.name.toLowerCase().includes(searchValue.toLowerCase()),
      )
    },

    // search cancel
    searchCancel(closeFn) {
      closeFn()
    },

    // search cancel
    searchConfirm(closeFn) {
      this.search()
      closeFn()
    },
  }
}
</script>

<style lang="less">
.custom-name-filter {
  padding: 10px;

  .custom-name-filter-operation {
    cursor: pointer;

    margin: 10px 10px 0px 0;

    .name-filter-cancel,
    .name-filter-confirm {
      &:hover {
        color: #1890ff;
      }
    }

    .name-filter-cancel {
      float: left;
    }

    .name-filter-confirm {
      float: right;
    }
  }
}
</style>

## API

### filterCustom 筛选自定义配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultVisible | 是否默认展开筛选面板 | `Boolean` | - | false |
| render | 自定义筛选内容渲染函数。参数：`showFn` 展开下拉函数、`closeFn` 关闭下拉函数 | `Function({showFn, closeFn}, h):VNode` | - | - |
| filterIcon | 自定义筛选图标渲染函数 | `Function(h):VNode` | - | - |

### 使用说明

1. **自定义筛选**：通过 `render` 函数完全自定义筛选界面和交互逻辑
2. **交互控制**：使用 `showFn` 和 `closeFn` 控制筛选面板的显示和隐藏
3. **图标定制**：通过 `filterIcon` 自定义筛选按钮的图标
4. **默认状态**：通过 `defaultVisible` 设置筛选面板的默认展开状态

### 适用场景

- **复杂筛选**：需要自定义筛选界面和交互逻辑
- **特殊需求**：标准筛选功能无法满足的业务需求
- **用户体验**：需要提供更友好的筛选交互体验
- **集成第三方**：需要集成第三方组件或库的筛选功能 