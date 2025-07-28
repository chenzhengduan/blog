# Pagination 分页

## 使用方法

引入 VePagination

```javascript
import Vue from "vue";
import { VePagination } from "vue-fantable";

Vue.use(VePagination);
```

使用

```vue
<template>
  <ve-pagination :total="600" />
</template>
```

## 基础示例

```vue
<template>
<ve-pagination :total="600" />
</template>
```

## 当前页码

### 功能描述

page-index 属性设置当前页码

```vue
<template>
    <ve-pagination :total="600" :page-index="2" />
</template>
```

## 每页大小

### 功能描述

page-size 属性设置每页大小

```vue
<template>
    <ve-pagination :total="600" :page-size="30" />
</template>
```

## 分页按钮个数

### 功能描述

pagingCount 属性设置向前 5 页和向后 5 页，其中间按钮的个数分页按钮个数

```vue
<template>
    <ve-pagination :total="600" :paging-count="6" />
</template>
```

## 分页下拉配置

### 功能描述

pageSizeOption 属性设置分页下拉配置

```vue
<template>
    <ve-pagination :total="600" :page-size-option="[5,10,15]" :page-size="5" />
</template>
```

## Callback Events

### 功能描述

1、on-page-number-changePage number change callback event
2、on-page-size-changeChange callback events under each page

```vue
<template>
    <ve-pagination
        :total="600"
        @on-page-number-change="pageNumberChange"
        @on-page-size-change="pageSizeChange"
    />
</template>
<script>
export default {
  methods: {
    pageNumberChange(pageIndex) {
      console.log(pageIndex)
    },

    pageSizeChange(pageSize) {
      console.log(pageSize)
    },
  },
}
</script>
```

## 布局设置

### 不显示页码按钮

### 调整显示顺序

### 完整布局

### 功能描述

1、通过设置 layout 属性，改变布局。
2、layout 属性支持以下配置项：
total：显示总条数、prev：显示上一页按钮、pager：显示页码按钮、next：显示下一页按钮、sizer：显示每页大小设置、jumper：显示跳转文本框

```vue
<template>
    <div>
        <div>
            <div class="mb20 bold">不显示页码按钮</div>
            <ve-pagination :total="600" :layout="['total', 'prev', 'next', 'sizer', 'jumper']" />
        </div>
        <div>
            <div class="mt30 mb20 bold">调整显示顺序</div>
            <ve-pagination
                :total="600"
                :layout="['total', 'sizer', 'prev', 'pager', 'next', 'jumper']"
            />
        </div>

        <div>
            <div class="mt30 mb20 bold">完整布局</div>
            <ve-pagination
                :total="600"
                :layout="['total', 'prev', 'pager', 'next', 'sizer', 'jumper']"
            />
        </div>
    </div>
</template>
```

## API

### props

| 参数 | 描述 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| layout | 布局设置 | Array | ['total', 'prev', 'pager', 'next', 'sizer', 'jumper'] | ['total', 'prev', 'pager', 'next', 'sizer', 'jumper'] |
| total | 总数量 | Number | - | - |
| pageIndex | 当前页码 | Number | - | 1 |
| pagingCount | 前 5 页和后 5 页中间的按钮数 | Number | - | 5 |
| pageSize | 每页大小 | Number | - | 10 |
| pageSizeOption | 每页大小下拉配置 | Array | - | [10, 20, 30] |
| popperAppendTo | 下拉弹层需要渲染的父节点 。默认渲染到 body 上，如果你遇到菜单滚动定位问题，试试修改为滚动的区域，并相对其定位。示例 | String/HTMLElement | - | - |

### Event

| 事件名称 | 描述 | 回调参数 |
|----------|------|----------|
| on-page-number-change | 页码改变回调事件 | 当前页码 |
| on-page-size-change | 每页大小回调 | 每页大小 | 