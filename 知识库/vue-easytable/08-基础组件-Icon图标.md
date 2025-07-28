 

内置了一些图标供你选择

## 使用方法

引入 VeIcon

```javascript
import Vue from "vue";
import { VeIcon } from "vue-fantable";

Vue.use(VeIcon);
```

使用

```vue
<template>
    <ve-icon name="double-right-arrow" />
</template>
```

## 使用

### 功能描述

1、name属性设置图标的名称
2、size属性设置图标的大小
3、color属性设置图标的颜色

```vue
<template>
    <div>
        <ve-icon name="double-right-arrow" />
        <ve-icon name="double-right-arrow" color="red" />
        <ve-icon name="double-right-arrow" :size="40" />
    </div>
</template>
```

## 图标集合

- filter
- double-right-arrow
- double-left-arrow
- top-arrow
- right-arrow
- bottom-arrow
- left-arrow
- sort-top-arrow
- sort-bottom-arrow
- search

## API

### props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| name | 图标名称 | String | 见示例 | - |
| size | 图标大小 | Number | - | - |
| color | 图标颜色 | String | - | - |