# Loading 加载

## 使用方法

在你需要的地方引用

```javascript
import Vue from "vue";
import { veLoading } from "vue-fantable";
```

调用

```javascript
veLoading({
    target: "#loading-1",
    name: "grid",
    tip: "loading...",
});
```

## 全局使用

将 veLoading 组件挂载到 Vue 的 prototype 原型上，便于全局调用

```javascript
import Vue from "vue";
import { veLoading } from "vue-fantable";

Vue.prototype.$veLoading = veLoading;
```

调用

```javascript
this.$veLoading({
    target: "#loading-1",
    name: "grid",
    tip: "loading...",
});
```

## 区域加载

在表格等容器中显示 Loading 效果

| Name | Date | Hobby | Address |
|------|------|-------|---------|
| John | 1900-05-20 | coding and coding repeat | No.1 Century Avenue, Shanghai |
| Dickerson | 1910-06-20 | coding and coding repeat | No.1 Century Avenue, Beijing |
| Larsen | 2000-07-20 | coding and coding repeat | No.1 Century Avenue, Chongqing |
| Geneva | 2010-08-20 | coding and coding repeat | No.1 Century Avenue, Xiamen |
| Jami | 2020-09-20 | coding and coding repeat | No.1 Century Avenue, Shenzhen |

### 功能描述

1、通过 target 参数指定 Loading 的容器。target为 DOM 对象或 字符串选择器（可以通过document.querySelector获取的字符串）
2、name参数指定加载效果类型名称
3、Loading 实例包含show、close、destroy3 个方法

```vue
<template>
    <div>
        <button class="button-demo" @click="show()">开启 Loading</button>
        <button class="button-demo" @click="close()">关闭 Loading</button>
        <br />
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

## 整屏加载

### 功能描述

1、通过fullscreen参数，指定 Loading 全屏展示
2、通过lock参数，指定禁止鼠标滚动

## 自定义

你还可以自定义加载文案、背景色、大小

loading...

### 功能描述

1、color 设置加载效果的颜色
2、tip设置加载文案
3、overlayBackgroundColor设置遮罩背景色，可以指定 rgba，让背景变得透明

## loading 集合

- plane
- grid
- wave
- flow
- bounce
- pulse

## API

### props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| name | 加载效果类型名称 | String | 见"Loading 集合"示例 | "plane" |
| target | DOM 对象 或 可以通过 document.querySelector 获取的字符串 | Object/String | | |
| fullscreen | 是否全屏展示 | Boolean | | false |
| tip | 加载文案 | String | | |
| color | 加载图标的颜色 | String | | "#1890ff" |
| overlayBackgroundColor | 遮罩背景色 | String | | "rgba(255, 255, 255, 0.5)" |
| height | 加载图标的高度 | String/Number | | |
| width | 加载图标的宽度 | String/Number | | |

### methods

| 方法名称 | 说明 | 参数 |
|----------|------|------|
| show | 展示 Loading 效果 | - |
| close | 关闭 Loading 效果 | - |
| destroy | 默认关闭不会销毁，需要手动调用销毁 Loading | - | 