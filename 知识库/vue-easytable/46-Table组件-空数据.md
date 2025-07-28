# Table 组件-空数据

## 概述

空数据效果，组件本身没有提供接口，你可以自己实现，这样会更灵活一些。

## 空数据

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| 暂无数据 |

```vue
<template>
    <div>
        <button class="button-demo" @click="request()">切换请求</button>
        <br />
        <br />
        <fan-table :columns="columns" :table-data="tableData" border-y />
        <div v-show="showEmpty" class="empty-data">暂无数据</div>
    </div>
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
          align: 'center',
          width: '20%',
        },
        {
          field: 'date',
          key: 'b',
          title: 'Date',
          align: 'left',
          width: '20%',
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
          width: '30%',
        },
      ],
      tableData: [],
      // show empty
      showEmpty: false,
    }
  },
  created() {
    this.request()
  },
  methods: {
    request() {
      setTimeout(() => {
        this.tableData =
                        this.tableData.length > 0
                          ? []
                          : [
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
                          ]

        if (this.tableData.length === 0) {
          this.showEmpty = true
        } else {
          this.showEmpty = false
        }
      })
    },
  },
}
</script>

<style>
    .empty-data {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 200px;
        width: 100%;
        color: #666;
        font-size: 16px;
        border: 1px solid #eee;
        border-top: 0;
    }
</style>
```

## 实现原理

### 1. 数据状态管理

通过 `showEmpty` 变量控制空数据状态的显示：

```javascript
data() {
  return {
    tableData: [],
    showEmpty: false,
  }
}
```

### 2. 条件渲染

使用 `v-show` 指令根据数据状态显示空数据提示：

```vue
<div v-show="showEmpty" class="empty-data">暂无数据</div>
```

### 3. 数据监听

在数据变化时更新空数据状态：

```javascript
if (this.tableData.length === 0) {
  this.showEmpty = true
} else {
  this.showEmpty = false
}
```

## 样式设计

### 基础样式

```css
.empty-data {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    width: 100%;
    color: #666;
    font-size: 16px;
    border: 1px solid #eee;
    border-top: 0;
}
```

### 样式特点

- **居中显示**: 使用 flexbox 实现水平和垂直居中
- **固定高度**: 设置合适的高度保持布局稳定
- **边框样式**: 与表格边框保持一致
- **文字样式**: 使用灰色文字，大小适中

## 扩展方案

### 1. 自定义空数据组件

```vue
<template>
  <div v-show="showEmpty" class="empty-data">
    <div class="empty-icon">📊</div>
    <div class="empty-text">{{ emptyText }}</div>
    <div v-if="showAction" class="empty-action">
      <button @click="onRefresh">刷新数据</button>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    emptyText: {
      type: String,
      default: '暂无数据'
    },
    showAction: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    onRefresh() {
      this.$emit('refresh')
    }
  }
}
</script>
```

### 2. 计算属性方式

```javascript
computed: {
  showEmpty() {
    return this.tableData.length === 0
  }
}
```

### 3. 监听器方式

```javascript
watch: {
  tableData: {
    handler(newVal) {
      this.showEmpty = newVal.length === 0
    },
    immediate: true
  }
}
```

## 最佳实践

### 1. 用户体验

- **友好提示**: 使用友好的空数据提示文案
- **操作引导**: 提供刷新、添加等操作按钮
- **加载状态**: 区分加载中和空数据状态

### 2. 样式设计

- **视觉一致性**: 与整体设计风格保持一致
- **响应式**: 适配不同屏幕尺寸
- **动画效果**: 添加淡入淡出等过渡效果

### 3. 性能优化

- **条件渲染**: 使用 `v-show` 而非 `v-if` 避免频繁创建销毁
- **计算属性**: 使用计算属性自动判断空数据状态
- **防抖处理**: 避免频繁的状态切换

## 常见场景

### 1. 搜索无结果

```javascript
// 搜索后无数据
if (searchResult.length === 0) {
  this.showEmpty = true
  this.emptyText = '未找到相关数据'
}
```

### 2. 网络错误

```javascript
// 请求失败
if (error) {
  this.showEmpty = true
  this.emptyText = '网络错误，请稍后重试'
  this.showAction = true
}
```

### 3. 权限不足

```javascript
// 无权限访问
if (!hasPermission) {
  this.showEmpty = true
  this.emptyText = '暂无访问权限'
}
```

## 注意事项

1. **状态同步**: 确保空数据状态与数据状态同步
2. **样式兼容**: 注意与表格样式的兼容性
3. **交互反馈**: 提供适当的用户交互反馈
4. **国际化**: 考虑多语言支持
5. **可访问性**: 确保空数据提示对屏幕阅读器友好 