# Table组件-结合Loading组件

## 概述

Table组件开启Loading效果是非常简单的，请求数据时开启Loading即可。更多Loading使用方法，请参考Loading组件。

## 示例

通过Loading组件为Table组件添加加载状态：

```vue
<template>
  <div>
    <button class="button-demo" @click="show()">开启 Loading</button>
    <button class="button-demo" @click="close()">关闭 Loading</button>
    <br />
    <fan-table id="loading-container" :columns="columns" :table-data="tableData" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      loadingInstance: null,
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'center' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        {
          field: 'hobby',
          key: 'c',
          title: 'Hobby',
          align: 'right',
        },
        { field: 'address', key: 'd', title: 'Address' },
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
  mounted() {
    this.loadingInstance = this.$veLoading({
      target: document.querySelector('#loading-container'),
      // 等同于
      // target:"#loading-container"
      name: 'wave',
    })
    this.show()
  },
  unmounted() {
    this.loadingInstance.destroy()
  },
  methods: {
    show() {
      this.loadingInstance.show()
    },
    close() {
      this.loadingInstance.close()
    },
  },
}
</script>
```

## 实际应用示例

在实际项目中，通常在数据请求时显示Loading状态：

```vue
<template>
  <div>
    <button class="button-demo" @click="loadData()">重新加载数据</button>
    <br />
    <fan-table 
      id="data-table" 
      :columns="columns" 
      :table-data="tableData" 
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      loadingInstance: null,
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'center' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'right' },
        { field: 'address', key: 'd', title: 'Address' },
      ],
      tableData: [],
    }
  },
  mounted() {
    // 初始化Loading实例
    this.loadingInstance = this.$veLoading({
      target: '#data-table',
      name: 'wave',
    })
    
    // 页面加载时获取数据
    this.loadData()
  },
  unmounted() {
    // 组件销毁时清理Loading实例
    if (this.loadingInstance) {
      this.loadingInstance.destroy()
    }
  },
  methods: {
    async loadData() {
      try {
        // 显示Loading
        this.loadingInstance.show()
        
        // 模拟API请求
        const data = await this.fetchTableData()
        
        // 更新表格数据
        this.tableData = data
      } catch (error) {
        console.error('数据加载失败:', error)
      } finally {
        // 隐藏Loading
        this.loadingInstance.close()
      }
    },
    
    // 模拟API请求
    fetchTableData() {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve([
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
          ])
        }, 2000) // 模拟2秒的网络延迟
      })
    },
  },
}
</script>
```

## 不同Loading样式

Loading组件支持多种样式，可以根据需要选择：

```vue
<template>
  <div>
    <div class="button-group">
      <button @click="showLoading('wave')">Wave样式</button>
      <button @click="showLoading('spin')">Spin样式</button>
      <button @click="showLoading('bounce')">Bounce样式</button>
      <button @click="showLoading('pulse')">Pulse样式</button>
    </div>
    <br />
    <fan-table 
      id="styled-table" 
      :columns="columns" 
      :table-data="tableData" 
    />
  </div>
</template>

<script>
export default {
  data() {
    return {
      loadingInstance: null,
      columns: [
        { field: 'name', key: 'a', title: 'Name', align: 'center' },
        { field: 'date', key: 'b', title: 'Date', align: 'left' },
        { field: 'hobby', key: 'c', title: 'Hobby', align: 'right' },
        { field: 'address', key: 'd', title: 'Address' },
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
      ],
    }
  },
  mounted() {
    this.loadingInstance = this.$veLoading({
      target: '#styled-table',
      name: 'wave',
    })
  },
  unmounted() {
    if (this.loadingInstance) {
      this.loadingInstance.destroy()
    }
  },
  methods: {
    showLoading(style) {
      // 更新Loading样式
      this.loadingInstance.destroy()
      this.loadingInstance = this.$veLoading({
        target: '#styled-table',
        name: style,
      })
      
      // 显示Loading
      this.loadingInstance.show()
      
      // 3秒后自动关闭
      setTimeout(() => {
        this.loadingInstance.close()
      }, 3000)
    },
  },
}
</script>

<style scoped>
.button-group {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.button-group button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  border-radius: 4px;
}

.button-group button:hover {
  background: #f5f5f5;
}
</style>
```

## 配置选项

Loading组件支持多种配置选项：

```javascript
// 基础配置
this.loadingInstance = this.$veLoading({
  target: '#table-container',        // 目标元素
  name: 'wave',                      // Loading样式
  color: '#1890ff',                  // Loading颜色
  background: 'rgba(255,255,255,0.8)', // 背景色
  text: '数据加载中...',              // 加载文本
  textColor: '#666',                 // 文本颜色
  textSize: '14px',                  // 文本大小
})

// 高级配置
this.loadingInstance = this.$veLoading({
  target: document.querySelector('#table-container'),
  name: 'spin',
  color: '#52c41a',
  background: 'rgba(0,0,0,0.1)',
  text: 'Loading...',
  textColor: '#52c41a',
  textSize: '16px',
  zIndex: 1000,                      // z-index层级
})
```

## 最佳实践

### 1. 数据请求时的Loading

```javascript
methods: {
  async fetchData() {
    this.loadingInstance.show()
    
    try {
      const response = await api.getData()
      this.tableData = response.data
    } catch (error) {
      this.$message.error('数据加载失败')
    } finally {
      this.loadingInstance.close()
    }
  }
}
```

### 2. 分页数据加载

```javascript
methods: {
  async changePage(page) {
    this.loadingInstance.show()
    
    try {
      const response = await api.getData({ page })
      this.tableData = response.data
      this.currentPage = page
    } catch (error) {
      this.$message.error('数据加载失败')
    } finally {
      this.loadingInstance.close()
    }
  }
}
```

### 3. 搜索时的Loading

```javascript
methods: {
  async search(keyword) {
    if (!keyword.trim()) {
      this.tableData = []
      return
    }
    
    this.loadingInstance.show()
    
    try {
      const response = await api.search({ keyword })
      this.tableData = response.data
    } catch (error) {
      this.$message.error('搜索失败')
    } finally {
      this.loadingInstance.close()
    }
  }
}
```

## 使用要点

### 1. 目标元素设置
- 为Table组件设置唯一的ID
- 确保目标元素存在于DOM中
- 支持CSS选择器和DOM元素两种方式

### 2. 生命周期管理
- 在`mounted`中初始化Loading实例
- 在`unmounted`中销毁Loading实例
- 避免内存泄漏

### 3. 错误处理
- 使用`try-catch-finally`确保Loading正确关闭
- 在错误情况下也要关闭Loading
- 提供用户友好的错误提示

### 4. 用户体验
- 合理设置Loading文本
- 选择合适的Loading样式
- 避免Loading时间过长

## 注意事项

1. **目标元素**：确保Table组件有唯一的ID或可选择的元素
2. **实例管理**：及时销毁Loading实例避免内存泄漏
3. **异步处理**：在异步操作的finally块中关闭Loading
4. **样式选择**：根据应用风格选择合适的Loading样式
5. **性能考虑**：避免频繁创建和销毁Loading实例
6. **用户体验**：提供清晰的加载状态和错误提示

## 应用场景

- 表格数据初始加载
- 分页数据切换
- 搜索结果加载
- 数据刷新操作
- 批量操作处理
- 导入导出操作

通过结合Loading组件，可以为Table组件提供良好的加载状态反馈，提升用户体验。