# Contextmenu 右键菜单

## 使用方法

引入 VeContextmenu

```javascript
import Vue from "vue";
import { VeContextmenu } from "vue-fantable";

Vue.use(VeContextmenu);
```

使用

```vue
<template>
  <div>
    <div id="contextmenu-target" ref="contextmenuTargetRef">
      <div>右键点击此区域</div>
    </div>
    <ve-contextmenu event-target="#contextmenu-target" :options="options" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      eventTarget: '',
      options: [
        {
          label: 'menu1',
          type: 'menu1-type',
        },
        {
          label: 'menu2',
          type: 'menu2-type',
          children: [
            {
              label: 'menu2-1',
              type: 'menu2-1-type',
            },
            {
              label: 'menu2-2',
              type: 'menu2-2-type',
            },
          ],
        },
        {
          type: 'SEPARATOR',
        },
        {
          label: 'menu3',
          type: 'menu3-type',
        },
      ],
    }
  },
  mounted() {
    /*
      eventTarget can be the following case:
      1、this.eventTarget = "#contextmenu-target";
      2、this.eventTarget = document.querySelector('#contextmenu-target');
      3、this.eventTarget = this.$refs["contextmenuTargetRef"];
    */
    this.eventTarget = this.$refs.contextmenuTargetRef
  },
}
</script>
```

## 基础示例

右键点击此区域

### 功能描述

type=SEPARATOR为分割线

```vue
<template>
  <div>
    <div id="contextmenu-target" ref="contextmenuTargetRef">
      <div>右键点击此区域</div>
      <div style="font-size:30px;color:red;">{{ contextmenuType }}</div>
    </div>

    <ve-contextmenu :event-target="eventTarget" :options="options" @on-node-click="contextmenuClick"></ve-contextmenu>
  </div>
</template>

<script>
export default {
  data() {
    return {
      eventTarget: '',
      // contextmenu type
      contextmenuType: '',
      // contextmenu options
      options: [
        {
          label: 'menu1',
          type: 'menu1-type',
        },
        {
          label: 'menu2',
          type: 'menu2-type',
          children: [
            {
              label: 'menu2-1',
              type: 'menu2-1-type',
            },
            {
              label: 'menu2-2',
              type: 'menu2-2',
            },
          ],
        },
        {
          type: 'SEPARATOR',
        },
        {
          label: 'menu3',
          type: 'menu3-type',
        },
        {
          label: 'menu4',
          disabled: true,
          children: [
            {
              label: 'menu4-1',
              type: 'menu4-1-type',
            },
          ],
        },
        {
          label: 'menu5',
          type: 'menu5-type',
          children: [
            {
              label: 'menu5-1',
              type: 'menu5-1-type',
              children: [
                {
                  label: 'menu5-1-1',
                  type: 'menu5-1-1-type',
                  children: [
                    {
                      label: 'menu5-1-1-1',
                      type: 'menu5-1-1-1-type',
                    },
                    {
                      label: 'menu5-1-1-2',
                      type: 'menu5-1-1-2-type',
                    },
                  ],
                },
                {
                  label: 'menu5-2-2',
                  type: 'menu5-2-2-type',
                },
              ],
            },
            {
              label: 'menu5-2',
              disabled: true,
            },
            {
              type: 'SEPARATOR',
            },
            {
              label: 'menu5-3',
              type: 'menu5-3-type',
            },
          ],
        },
        {
          label: 'menu6',
          type: 'menu6-type',
          disabled: true,
        },
      ],
    }
  },
  mounted() {
    /*
    eventTarget can be the following case:
    1、this.eventTarget = "#contextmenu-target";
    2、this.eventTarget = document.querySelector('#contextmenu-target');
    3、this.eventTarget = this.$refs["contextmenuTargetRef"];
    */
    this.eventTarget = this.$refs.contextmenuTargetRef
  },
  methods: {
    contextmenuClick(type) {
      this.contextmenuType = type
    },
  },
}
</script>

<style>
#contextmenu-target {
  display: flex;
  flex-direction: column;
  width: 300px;
  height: 300px;
  justify-content: center;
  align-items: center;
  background: #eee;
  border: 3px dashed #666;
  border-radius: 8px;
}
</style>
```

## API

### props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| eventTarget | 设置右键菜单作用的元素 | HTMLElement/String | - | - |
| options | 右键菜单项。支持无限层级树形结构，具体结构如下 | Array | - | - |

### options

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| label | 展示的菜单名称 | String | - | - |
| type | 菜单类型，内置分隔符类型为'SEPARATOR'。将会作为点击后回调的参数 | String | - | - |
| disabled | 禁用当前菜单，点击无效 | Boolean | - | - |

### Event

| 事件名称 | 说明 | 参数 |
|----------|------|------|
| on-node-click | 菜单点击回调 | 当前点击的菜单类型 |

### Instance Methods

| 实例名称 | 说明 | 参数 |
|----------|------|------|
| hideContextmenu | 隐藏当前右键菜单 | - | 