# WOnePC微前端架构
## 基于micro-app的微前端解决方案

> 项目背景：WOnePC(Avalon) + WOnePC-Vue(Vue3)微前端融合

> micro-app 是由京东前端团队推出的一款微前端框架，旨在降低上手难度、提升工作效率。micro-app无关技术栈，也不和业务绑定，可以用于任何前端框架。基于WebComponents思想，通过Custom Element结合自定义的ShadowDom，将微前端封装成一个类WebComponent组件，从而实现微前端的组件化渲染。

## 目录

- [1. 微前端概述](#1-微前端概述)
- [2. 为什么选择micro-app](#2-为什么选择micro-app)
- [3. micro-app基础知识](#3-micro-app基础知识)
- [4. 项目架构设计](#4-项目架构设计)
- [5. 通信机制详解](#5-通信机制详解)
- [6. 团队协作规范](#6-团队协作规范)
- [7. 案例分析](#7-案例分析)
- [8. 常见问题与解决方案](#8-常见问题与解决方案)
- [9. Q&A环节](#9-qa环节)

## 1. 微前端概述

### 1.1 什么是微前端

微前端是一种前端架构模式，它将前端应用分解成更小、更简单的应用，这些应用可以独立开发、测试和部署，同时仍然共同组成一个整体应用。微前端是借鉴了微服务的架构理念，它既可以将多个项目融合为一，又可以减少项目之间的耦合，提升项目扩展性，相比一整块的前端仓库，微前端架构下的前端仓库倾向于更小更灵活。

微前端的核心三大原则：**独立运行**、**独立部署**、**独立开发**。

### 1.2 微前端解决的问题

- **技术栈隔离**：允许在同一应用中使用不同的技术栈（如我们的Avalon和Vue3）
- **渐进式升级**：可以逐步将旧系统迁移到新技术栈
- **团队自主性**：不同团队可以独立开发和部署
- **代码隔离**：减少代码耦合，提高可维护性
- **独立部署**：可以独立部署每一个单页面应用
- **改善加载性能**：通过延迟加载代码，改善初始化加载时间
- **统一规范**：解决基于多页的子应用缺乏管理，规范/标准不统一的问题
- **资源共享**：统一控制视觉呈现、共享功能和依赖，避免重复工作

### 1.3 微前端的挑战

- 应用间通信
- 样式隔离
- 全局状态管理
- 性能优化
- 开发体验

## 2. 为什么选择micro-app

### 2.1 市场上的微前端方案对比

| 方案 | 实现原理 | 优点 | 缺点 |
|------|---------|------|------|
| single-spa | 基于路由劫持 | 成熟稳定 | 配置复杂，侵入性强 |
| qiankun | 基于single-spa | 阿里背书，功能丰富 | 配置复杂，沙箱不完善 |
| micro-app | 基于Web Components | 使用简单，侵入性低 | 社区相对较小 |
| Module Federation | Webpack5新特性 | 共享依赖，构建优化 | 强依赖Webpack |

### 2.2 micro-app的优势

- **基于Web Components**：使用Custom Elements，符合Web标准
- **简单易用**：使用类似HTML标签的方式集成应用
- **低侵入性**：子应用几乎不需要改造
- **完善的沙箱机制**：JS沙箱、样式隔离、元素隔离
- **预加载能力**：提升用户体验
- **轻量的体积**：≈10kb (gzip)
- **零依赖**：不依赖于任何第三方库
- **适合我们的场景**：从Avalon逐步迁移到Vue3
- **丰富的功能**：提供了数据通信、静态资源补全等一系列完善的功能
- **高扩展性**：没有任何依赖，赋予它小巧的体积和更高的扩展性
- **技术兼容性**：为了保证各个业务之间独立开发、独立部署的能力，micro-app做了诸多兼容，在任何技术框架中都可以正常运行

## 3. micro-app基础知识

### 3.1 核心概念

- **主应用**：WOnePC (Avalon)
- **子应用**：WOnePC-Vue (Vue3)
- **micro-app标签**：Web Components的实现
- **生命周期**：created, beforemount, mounted, unmount
- **沙箱**：JS隔离、样式隔离

#### 3.1.1 核心流程

micro-app的核心功能是将子应用封装成一个类WebComponent组件，从而实现微前端的组件化渲染。整体流程如下：

1. 创建自定义元素：通过`customElements.define()`方法定义一个自定义元素`micro-app`
2. 获取子应用资源：通过fetch请求获取子应用的html、js、css等资源
3. 解析HTML：解析子应用的HTML，提取出js、css等资源
4. 创建沙箱环境：为子应用创建一个独立的沙箱环境，包括JS沙箱和样式隔离
5. 执行JS：在沙箱环境中执行子应用的JS代码
6. 渲染页面：将子应用的HTML渲染到主应用中
7. 生命周期管理：管理子应用的生命周期，包括创建、挂载、卸载等

#### 3.1.2 生命周期详解

- **created**：子应用创建后触发，此时子应用的DOM还未渲染，适合做一些初始化工作
- **beforemount**：子应用渲染前触发，此时子应用的DOM已创建但还未挂载到文档流中
- **mounted**：子应用渲染后触发，此时子应用的DOM已挂载到文档流中，可以进行DOM操作
- **unmount**：子应用卸载时触发，适合做一些清理工作，如清除定时器、解绑事件等

### 3.2 基础使用方法

#### 3.2.1 NPM方式引入（官方推荐）

```bash
# 安装依赖
npm i @micro-zoe/micro-app --save
```

```javascript
// 在项目入口文件中引入
import microApp from '@micro-zoe/micro-app'

// 初始化
microApp.start({
  shadowDOM: false // 在Avalon项目中建议关闭shadowDOM
})
```

#### 3.2.2 UMD方式引入（适用于非npm项目）

```html
<!-- 主应用引入micro-app -->
<script src="js/libs/micro-app/index.umd.js"></script>
<script>
  // 等待文档加载完成后再初始化micro-app
  document.addEventListener('DOMContentLoaded', function() {
    if (window.microApp) {
      console.log('microApp found, starting...');
      // 如果microApp.MicroApp是一个类，需要先创建实例
      if (window.microApp.MicroApp) {
        // 创建MicroApp类的实例
        const microAppInstance = new window.microApp.MicroApp();
        // 调用实例的start方法
        microAppInstance.start({
          shadowDOM: false // 在Avalon项目中建议关闭shadowDOM
        });
        console.log('microApp initialized using MicroApp class');
      } else {
        // 尝试直接调用microApp上的start方法
        try {
          window.microApp.start({
            shadowDOM: false // 在Avalon项目中建议关闭shadowDOM
          });
          console.log('microApp initialized using direct start method');
        } catch (error) {
          console.error('Error initializing microApp:', error);
        }
      }
    } else {
      console.error('microApp not found! Please check if micro-app library is loaded correctly.');
    }
  });
</script>

<!-- 使用micro-app标签引入子应用 -->
<micro-app 
  name="vue3-app-appoint" 
  url="/vue-app?page=appoint" 
  baseroute="/"
  iframe>
</micro-app>
```

> **注意**：UMD方式引入时，不同版本的micro-app初始化方式可能有所不同。上述代码提供了一种兼容性写法，可以适应不同版本的micro-app库。

### 3.3 主要配置参数

- **name**：子应用名称，必填且唯一，必须以字母开头，且不可以带有除中划线和下划线外的特殊符号
- **url**：子应用入口，必填，会被自动补全，如 http://localhost:3000/index.html
- **baseroute**：基础路由，用于路由匹配，主应用分配给子应用的基础路由
- **iframe**：是否使用iframe模式（解决一些兼容性问题）
- **shadowDOM**：是否开启shadowDOM隔离样式
- **disable-memory-router**：是否禁用虚拟路由系统，在1.X版本中默认开启虚拟路由系统
- **inline**：是否使用内联script
- **disableScopecss**：是否禁用样式隔离
- **disableSandbox**：是否禁用沙箱
- **data**：初始化数据，可以通过data属性向子应用传递数据

#### 3.3.1 micro-app初始化配置

```javascript
// 完整配置示例
window.microApp.start({
  shadowDOM: false,         // 是否开启shadowDOM
  destroy: true,            // 是否在卸载时销毁实例
  inline: false,            // 是否使用内联script
  disableScopecss: false,   // 是否禁用样式隔离
  disableSandbox: false,    // 是否禁用沙箱
  ssr: false                // 是否启用SSR
});
```

### 3.4 生命周期钩子

```javascript
// 生命周期钩子示例
document.querySelector('micro-app').addEventListener('created', () => {});
document.querySelector('micro-app').addEventListener('beforemount', () => {});
document.querySelector('micro-app').addEventListener('mounted', () => {});
document.querySelector('micro-app').addEventListener('unmount', () => {});
document.querySelector('micro-app').addEventListener('error', () => {});
```

## 4. 项目架构设计

### 4.1 我们的架构图

```
WOnePC (Avalon主应用)
├── index.html (micro-app初始化)
├── modules/
│   ├── appointCourse/ (约课模块)
│   │   ├── appointCourse.html (集成micro-app)
│   │   └── appointCourse.js
│   └── 其他模块...
└── js/
    └── libs/
        └── micro-app/ (micro-app库)

WOnePC-Vue (Vue3子应用)
├── public/
├── src/
│   ├── components/
│   │   └── AppointCourse.vue (约课组件)
│   ├── App.vue
│   └── main.js (子应用入口)
└── package.json
```

### 4.2 应用职责划分

- **主应用职责**：
  - 整体框架和布局
  - 用户认证和权限管理
  - 全局状态管理
  - 子应用管理和通信

- **子应用职责**：
  - 业务功能实现
  - 内部状态管理
  - UI渲染
  - 与主应用通信

### 4.3 部署策略

- **独立部署**：子应用可独立构建和部署
- **版本管理**：使用语义化版本控制
- **环境一致性**：保证开发、测试、生产环境配置一致

## 5. 通信机制详解

### 5.1 数据通信方式

#### 5.1.1 主应用向子应用传递数据

##### NPM方式

```javascript
// 引入micro-app
import microApp from '@micro-zoe/micro-app'

// 向指定子应用发送数据
microApp.setData('vue3-app-appoint', {
  campusList: [
    {ID: '1', Name: '校区1'},
    {ID: '2', Name: '校区2'},
    {ID: '3', Name: '校区3'}
  ]
});

// 发送全局数据（所有子应用可接收）
microApp.setGlobalData({
  userInfo: {
    id: '001',
    name: '张三'
  }
});
```

##### UMD方式

```javascript
// 直接使用window.microApp发送数据

// 向指定子应用发送数据
window.microApp.setData('vue3-app-appoint', {
  campusList: [
    {ID: '1', Name: '校区1'},
    {ID: '2', Name: '校区2'},
    {ID: '3', Name: '校区3'}
  ]
});

// 发送全局数据（所有子应用可接收）
window.microApp.setGlobalData({
  userInfo: {
    id: '001',
    name: '张三'
  }
});

// 使用EventCenterForMicroApp发送数据（适用于沙箱关闭或特殊场景）
if (window.microApp && window.microApp.EventCenterForMicroApp) {
  try {
    // 创建事件中心实例，参数为子应用名称（不传参数则为全局事件中心）
    const eventCenter = new window.microApp.EventCenterForMicroApp('vue3-app-appoint');
    // 发送数据给特定子应用
    eventCenter.dispatch({
      type: 'campusSelected',
      data: {
        campusId: '1',
        campusName: '校区1'
      }
    });
    console.log('已通过EventCenterForMicroApp实例发送数据给特定子应用');
  } catch (error) {
    console.error('发送数据时发生错误:', error);
  }
}

// 发送全局数据（所有子应用可接收）
if (window.microApp && window.microApp.EventCenterForMicroApp) {
  try {
    // 创建全局事件中心实例，不指定子应用名称
    const globalEventCenter = new window.microApp.EventCenterForMicroApp();
    // 发送全局数据，所有子应用都能接收
    globalEventCenter.setGlobalData({
      campusIds: ['1', '2', '3'],
      campusNames: ['校区1', '校区2', '校区3'],
      campusList: [
        {ID: '1', Name: '校区1'},
        {ID: '2', Name: '校区2'},
        {ID: '3', Name: '校区3'}
      ]
    });
    console.log('已通过EventCenterForMicroApp实例的setGlobalData发送全局数据');
  } catch (error) {
    console.error('发送全局数据时发生错误:', error);
  }
}
```

> **注意**：EventCenterForMicroApp类主要用于沙箱关闭或特殊场景下的通信，一般情况下使用window.microApp提供的方法即可。

#### 5.1.2 子应用接收数据

##### 标准方式

```javascript
// Vue3子应用中
import { onMounted, onUnmounted, ref } from 'vue';

export default {
  setup() {
    const campusList = ref([]);
    
    // 接收特定应用数据
    const dataListener = (data) => {
      console.log('收到来自主应用的数据:', data);
      if (data.campusList) {
        campusList.value = data.campusList;
      }
    };
    
    // 接收全局数据
    const globalDataListener = (data) => {
      console.log('收到全局数据:', data);
      // 处理全局数据...
    };
    
    onMounted(() => {
      if (window.microApp) {
        // 添加数据监听器
        window.microApp.addDataListener(dataListener);
        // 添加全局数据监听器
        window.microApp.addGlobalDataListener(globalDataListener);
      }
    });
    
    onUnmounted(() => {
      if (window.microApp) {
        // 移除数据监听器
        window.microApp.removeDataListener(dataListener);
        // 移除全局数据监听器
        window.microApp.removeGlobalDataListener(globalDataListener);
      }
    });
    
    return {
      campusList
    };
  }
};
```

##### 直接获取数据方式

```javascript
// 直接获取主应用传递的数据
const data = window.microApp.getData();
console.log('主应用传递的数据:', data);

// 直接获取全局数据
const globalData = window.microApp.getGlobalData();
console.log('全局数据:', globalData);
```

##### 使用EventCenterForMicroApp接收数据（沙箱关闭时）

```javascript
// 当沙箱关闭时，使用主应用注册的通信对象
// 假设主应用已经注册了eventCenterForAppxx对象

// 直接获取数据
const data = window.eventCenterForAppxx.getData();
console.log('主应用传递的数据:', data);

// 添加数据监听器
function dataListener(data) {
  console.log('收到来自主应用的数据:', data);
}

// 添加监听器，autoTrigger设置为true可以在初次绑定时自动触发一次
// 这在子应用加载晚于主应用发送数据的情况下很有用
window.eventCenterForAppxx.addDataListener(dataListener, true);

// 移除监听器
window.eventCenterForAppxx.removeDataListener(dataListener);

// 清空当前子应用的所有绑定函数(全局数据函数除外)
window.eventCenterForAppxx.clearDataListener();
```

#### 5.1.3 子应用向主应用传递数据

```javascript
// 子应用中
if (window.microApp) {
  // 向主应用发送数据
  window.microApp.dispatch({
    type: 'campusSelected',
    data: {
      campusId: '1',
      campusName: '校区1'
    }
  });
}
```

#### 5.1.4 主应用接收子应用数据

```javascript
// 主应用中
document.querySelector('micro-app[name="vue3-app-appoint"]').addEventListener('datachange', e => {
  console.log('来自子应用的数据:', e.detail.data);
  // 处理来自子应用的数据...
});
```

### 5.2 通信最佳实践

- **数据精简**：只传递必要的数据，避免大对象
- **类型定义**：明确定义数据结构和类型
- **错误处理**：添加适当的错误处理机制
- **状态同步**：保持主子应用状态一致性
- **避免循环依赖**：防止数据循环传递
- **使用autoTrigger**：在添加监听器时考虑设置autoTrigger为true，确保子应用能获取到主应用已经发送的数据
- **适当的通信方式选择**：
  - 对于特定子应用通信，使用setData/addDataListener
  - 对于全局通信，使用setGlobalData/addGlobalDataListener
  - 对于沙箱关闭场景，使用EventCenterForMicroApp

#### 5.2.1 UMD方式下的通信实践

```javascript
// 主应用中发送数据的安全写法
if (window.microApp) {
  try {
    // 尝试使用标准方式发送数据
    window.microApp.setData('vue3-app-appoint', {
      campusIds: idarr,
      campusNames: handleSchoolName(idarr),
      campusList: schools.lists.$model.filter(function(item) {
        return idarr.includes(item.ID);
      })
    });
    console.log('已通过setData发送校区数据');
  } catch (error) {
    console.error('使用setData发送数据时发生错误:', error);
    
    // 如果标准方式失败，尝试使用EventCenterForMicroApp
    if (window.microApp.EventCenterForMicroApp) {
      try {
        const eventCenter = new window.microApp.EventCenterForMicroApp('vue3-app-appoint');
        eventCenter.dispatch({
          type: 'campusData',
          data: {
            campusIds: idarr,
            campusNames: handleSchoolName(idarr),
            campusList: schools.lists.$model.filter(function(item) {
              return idarr.includes(item.ID);
            })
          }
        });
        console.log('已通过EventCenterForMicroApp实例发送校区数据');
      } catch (innerError) {
        console.error('使用EventCenterForMicroApp发送数据时发生错误:', innerError);
      }
    }
  }
}

// 发送全局数据的安全写法
if (window.microApp) {
  try {
    // 尝试使用标准方式发送全局数据
    window.microApp.setGlobalData({
      campusIds: idarr,
      campusNames: handleSchoolName(idarr),
      campusList: schools.lists.$model.filter(function(item) {
        return idarr.includes(item.ID);
      })
    });
    console.log('已通过setGlobalData发送全局校区数据');
  } catch (error) {
    console.error('使用setGlobalData发送数据时发生错误:', error);
    
    // 如果标准方式失败，尝试使用EventCenterForMicroApp
    if (window.microApp.EventCenterForMicroApp) {
      try {
        const globalEventCenter = new window.microApp.EventCenterForMicroApp();
        globalEventCenter.setGlobalData({
          campusIds: idarr,
          campusNames: handleSchoolName(idarr),
          campusList: schools.lists.$model.filter(function(item) {
            return idarr.includes(item.ID);
          })
        });
        console.log('已通过EventCenterForMicroApp实例的setGlobalData发送全局校区数据');
      } catch (innerError) {
        console.error('使用EventCenterForMicroApp的setGlobalData发送数据时发生错误:', innerError);
      }
    }
  }
}
```

## 6. 团队协作规范

### 6.1 开发规范

#### 6.1.1 命名规范

- **应用名称**：使用kebab-case，如`vue3-app-appoint`
- **事件名称**：使用camelCase，如`campusSelected`
- **数据字段**：与后端保持一致，通常使用PascalCase或camelCase

#### 6.1.2 路由规范

- **子应用路由**：使用hash路由或带前缀的history路由
- **路由命名**：功能模块-具体页面，如`appoint-list`
- **路由参数**：使用query参数传递简单数据

#### 6.1.3 样式规范

- **CSS隔离**：使用scoped或CSS Modules
- **命名空间**：添加应用前缀，如`.v3-appoint-course`
- **避免全局样式**：不使用`:global`选择器
- **样式重置**：每个子应用维护自己的样式重置

#### 6.1.4 资源规范

- **静态资源**：使用相对路径或绝对路径
- **共享资源**：通过主应用提供的API获取
- **资源命名**：添加应用前缀，避免冲突

### 6.2 开发流程

1. **需求分析**：确定功能是放在主应用还是子应用
2. **接口设计**：定义主子应用通信接口
3. **并行开发**：主应用和子应用可并行开发
4. **联调测试**：集成测试主子应用交互
5. **独立部署**：子应用可独立部署

### 6.3 版本控制

- **语义化版本**：遵循semver规范
- **版本兼容性**：明确主子应用兼容版本范围
- **变更日志**：记录每个版本的变更内容

### 6.4 文档规范

- **README**：每个应用提供详细的README
- **API文档**：记录通信接口和参数
- **架构图**：维护最新的架构图
- **注释**：关键代码添加详细注释

## 7. 案例分析

### 7.1 约课模块集成案例

#### 7.1.1 主应用代码（WOnePC）

```html
<!-- appointCourse.html -->
<div class="micro-app-container-wrapper">
  <div class="micro-app-right">
    <h3 class="module-title">约课模块</h3>
    <micro-app 
      name="vue3-app-appoint" 
      url="/vue-app?page=appoint" 
      baseroute="/"
      iframe
      style="width: 100%; height: calc(100% - 43px);">
    </micro-app>
  </div>
</div>

<script>
  // 监听校区选择事件
  avalon.define({
    $id: "appointCourseController",
    // ... 其他代码
    
    // 选择校区时发送数据到子应用
    selectCampus: function(campus) {
      this.currentCampus = campus;
      
      // 向子应用发送校区数据
      if (window.microApp) {
        window.microApp.setData('vue3-app-appoint', {
          selectedCampus: campus,
          campusList: this.campusList
        });
      }
    }
  });
  
  // 监听子应用发送的数据
  document.querySelector('micro-app[name="vue3-app-appoint"]').addEventListener('datachange', e => {
    var data = e.detail.data;
    if (data.type === 'campusSelected') {
      // 处理子应用选择的校区
      var vm = avalon.vmodels.appointCourseController;
      vm.selectCampusById(data.data.campusId);
    }
  });
</script>
```

#### 7.1.2 子应用代码（WOnePC-Vue）

```vue
<!-- AppointCourse.vue -->
<template>
  <div class="appoint-course">
    <!-- 全局loading遮罩 -->
    <div class="global-loading" v-if="loading">
      <div class="loading-spinner">
        <div class="spinner"></div>
        <div class="loading-text">加载中...</div>
      </div>
    </div>
    <h1>Vue3约课计划</h1>
    
    <!-- 校区选择区域 -->
    <div class="campus-selector" v-if="campusList.length > 0">
      <h3>选择校区:</h3>
      <div class="campus-options">
        <div 
          v-for="campus in campusList" 
          :key="campus.ID"
          :class="['campus-option', selectedCampus?.ID === campus.ID ? 'selected' : '']"
          @click="selectCampus(campus)"
        >
          {{ campus.Name }}
        </div>
      </div>
      <div class="selected-campus" v-if="selectedCampus">
        <p>当前选择: <strong>{{ selectedCampus.Name }}</strong></p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';

export default {
  name: 'AppointCourse',
  setup() {
    const loading = ref(false);
    const campusList = ref([]);
    const selectedCampus = ref(null);
    
    // 选择校区
    const selectCampus = (campus) => {
      loading.value = true;
      selectedCampus.value = campus;
      
      // 向主应用发送选择的校区
      if (window.microApp) {
        window.microApp.dispatch({
          type: 'campusSelected',
          data: {
            campusId: campus.ID,
            campusName: campus.Name
          }
        });
      }
      
      // 模拟异步操作
      setTimeout(() => {
        loading.value = false;
      }, 500);
    };
    
    // 接收主应用数据
    const dataListener = (data) => {
      console.log('收到主应用数据:', data);
      
      if (data.campusList) {
        loading.value = true;
        campusList.value = data.campusList;
        
        // 如果有选中的校区
        if (data.selectedCampus) {
          selectedCampus.value = data.selectedCampus;
        }
        
        setTimeout(() => {
          loading.value = false;
        }, 500);
      }
    };
    
    onMounted(() => {
      if (window.microApp) {
        // 添加数据监听
        window.microApp.addDataListener(dataListener);
      }
    });
    
    onUnmounted(() => {
      if (window.microApp) {
        // 移除数据监听
        window.microApp.removeDataListener(dataListener);
      }
    });
    
    return {
      loading,
      campusList,
      selectedCampus,
      selectCampus
    };
  }
};
</script>

```

### 7.2 新旧版本切换案例

```html
<!-- appointCourse.html -->
<div class="version-switch">
  <button id="toggleVersion">
    切换到{{ isNewVersion ? '旧' : '新' }}版
  </button>
</div>

<div id="oldVersion" style="display: none;">
  <!-- 旧版约课内容 -->
</div>

<div id="newVersion">
  <!-- 新版微前端集成 -->
  <micro-app 
    name="vue3-app-appoint" 
    url="/vue-app?page=appoint" 
    baseroute="/"
    iframe>
  </micro-app>
</div>

<script>
  // 版本切换逻辑
  (function() {
    var isNewVersion = localStorage.getItem('useNewVersion') !== 'false';
    var oldVersionEl = document.getElementById('oldVersion');
    var newVersionEl = document.getElementById('newVersion');
    var toggleBtn = document.getElementById('toggleVersion');
    
    function updateVersionDisplay() {
      oldVersionEl.style.display = isNewVersion ? 'none' : 'block';
      newVersionEl.style.display = isNewVersion ? 'block' : 'none';
      toggleBtn.textContent = '切换到' + (isNewVersion ? '旧' : '新') + '版';
    }
    
    // 初始化显示
    updateVersionDisplay();
    
    // 切换版本
    toggleBtn.addEventListener('click', function() {
      isNewVersion = !isNewVersion;
      localStorage.setItem('useNewVersion', isNewVersion);
      updateVersionDisplay();
    });
  })();
</script>
```

## 8. 常见问题与解决方案

### 8.1 样式冲突问题

**问题**：主应用和子应用样式互相影响

**解决方案**：
- 使用CSS Modules或scoped样式
- 添加命名空间前缀
- 考虑开启shadowDOM（但要注意兼容性）

### 8.2 通信问题

**问题**：数据通信不及时或丢失

**解决方案**：
- 确保监听器在mounted阶段添加
- 在unmounted阶段移除监听器
- 添加错误处理和日志
- 考虑使用状态管理工具
- 使用autoTrigger参数确保获取到初始数据

### 8.3 路由冲突

**问题**：主应用和子应用路由冲突

**解决方案**：
- 子应用使用hash路由
- 或使用带前缀的history路由
- 设置正确的baseroute
- 在1.X版本中可以通过disable-memory-router属性关闭虚拟路由系统

### 8.4 加载性能问题

**问题**：子应用加载缓慢

**解决方案**：
- 使用预加载
- 代码分割
- 资源压缩
- 考虑使用CDN
- 使用Service Worker缓存子应用资源

### 8.5 沙箱逃逸问题

**问题**：JS沙箱隔离不完全

**解决方案**：
- 避免修改window原型
- 不使用非标准的全局API
- 使用iframe模式加强隔离

### 8.6 子应用路由跳转问题

**问题**：子应用切换路由时会重新回到当前路由

**解决方案**：
- 在micro-app标签中添加disable-memory-router属性关闭虚拟路由系统
- 或者使用micro-app的0.8版本

```html
<micro-app
  name="micro-app-manage"
  :url="microData.url"
  :data="microData"
  disable-memory-router  // 关闭虚拟路由系统
  @datachange="handleDataChange"
  @mounted="handleMount"
></micro-app>
```

### 8.7 子应用返回上一页问题

**问题**：子应用返回上一页时（或者在浏览器回退），会无法切换回基座应用

**解决方案**：
- 使用1.X版本可以解决这个问题
- 0.8版本需要在基座应用的beforeEach里添加以下逻辑：

```javascript
if (!window.history?.state?.current) {
   // 解决子应用是vue-router3 与 vue-router4冲突
   Object.assign(window.history.state, { current: from.fullPath });
}
```

### 8.8 回退两次才能回到主应用问题

**解决方案**：
- 子应用的main.js里使用router.replace而不是router.push

```javascript
// main.js
function handleMicroData() {
  // 是否是微前端环境
  if (window.__MICRO_APP_ENVIRONMENT__) {
    // 主动获取基座下发的数据
    const microData = window.microApp.getData()
    if (microData.path) {
      router.replace({ path: microData.path, query: microData.query })  // 这里不能用router.push
    }
  }
}
```

### 8.9 部署环境接口请求问题

**问题**：部署测试环境后，子应用接口无法正常请求

**解决方案**：
1. 子应用vue.config.js里publicPath要设置绝对路径
2. request请求封装里baseURL也要设置绝对路径

```javascript
// vue.config.js
module.exports = {
  publicPath: process.env.NODE_ENV !== 'development' ? process.env.VUE_APP_WEB_URL : ''
  // 其他配置
}

// request.js
service.interceptors.request.use(
  async config => {
    config.baseURL = process.env.VUE_APP_WEB_URL + (config.baseUrl || process.env.VUE_APP_BASE_API) // url = base url + request url
    // 其他配置
    return config
  },
)
```

### 8.10 子应用接口跨域问题

**问题**：子应用请求接口时出现跨域问题

**解决方案**：
1. 确保后端接口支持跨域（后端配置）
2. 前端配置跨域支持：
   - 本地开发环境：在vue.config.js中配置
   ```javascript
   devServer: {
     headers: {
       'Access-Control-Allow-Origin': '*'  // 允许跨域配置
     },
   }
   ```
   - 部署环境：在nginx中配置
   ```
   location / {
     root   /usr/share/nginx/html;
     index  index.html index.htm;
     # 跨域配置
     add_header Access-Control-Allow-Origin *;
   }
   ```
3. 注意：如果前端配置了withCredentials，需要确保后端的Access-Control-Allow-Origin不是*，而是具体的域名

## 9. Q&A环节

以下是关于微前端的常见问题及其详细解答：

### 1. micro-app与qiankun的区别？

**micro-app与qiankun的主要区别：**

- **实现原理**：
  - micro-app基于WebComponents实现，使用自定义元素(Custom Elements)加载子应用
  - qiankun基于single-spa，通过JavaScript劫持和处理路由变化来加载子应用

- **使用方式**：
  - micro-app使用类似HTML标签的方式集成应用，更加简单直观
  - qiankun需要在主应用中注册子应用，配置较为复杂

- **侵入性**：
  - micro-app对子应用基本无侵入，子应用几乎不需要改造
  - qiankun需要子应用导出生命周期钩子函数，有一定侵入性

- **沙箱实现**：
  - micro-app基于Proxy实现JS沙箱，支持多实例沙箱
  - qiankun提供多种沙箱模式，但在某些场景下存在沙箱逃逸问题

- **样式隔离**：
  - micro-app支持shadowDOM和scoped两种样式隔离方式
  - qiankun主要通过CSS前缀和动态样式表实现样式隔离

- **社区支持**：
  - qiankun由蚂蚁金服团队维护，社区更大，资源更丰富
  - micro-app由京东前端团队维护，相对较新，但发展迅速，更符合Web标准

- **体积**：
  - micro-app体积更小，约10kb(gzip)
  - qiankun体积相对较大

- **版本差异**：
  - micro-app的0.8版本和1.X版本在虚拟路由系统等方面有一些差异，1.X版本默认开启虚拟路由系统

### 2. 如何处理子应用之间的通信？

**子应用间通信的解决方案：**

- **基于事件机制的通信**：
  - 使用自定义事件（CustomEvent）实现发布-订阅模式
  - micro-app提供的EventCenterForMicroApp类实现数据交互

  **向特定子应用发送数据（UMD引入方式）：**
  ```javascript
  if (window.microApp && window.microApp.EventCenterForMicroApp) {
    try {
      // 创建事件中心实例，参数为子应用名称
      const eventCenter = new window.microApp.EventCenterForMicroApp('vue3-app-name');
      // 发送数据给特定子应用
      eventCenter.dispatch({
        type: 'dataChange',
        data: {
          campusList: [
            {ID: '1', Name: '校区1'},
            {ID: '2', Name: '校区2'}
          ]
        }
      });
      console.log('已通过EventCenterForMicroApp实例发送数据给特定子应用');
    } catch (error) {
      console.error('发送数据时发生错误:', error);
    }
  }
  ```

  **向所有子应用发送全局数据（UMD引入方式）：**
  ```javascript
  if (window.microApp && window.microApp.EventCenterForMicroApp) {
    try {
      // 创建全局事件中心实例，不指定子应用名称
      const globalEventCenter = new window.microApp.EventCenterForMicroApp();
      // 发送全局数据，所有子应用都能接收
      globalEventCenter.setGlobalData({
        userInfo: {
          id: '001',
          name: '张三'
        },
        systemConfig: {
          theme: 'light',
          version: '1.0.0'
        }
      });
      console.log('已通过EventCenterForMicroApp实例发送全局数据给所有子应用');
    } catch (error) {
      console.error('发送全局数据时发生错误:', error);
    }
  }
  ```
  
  **子应用接收数据：**
  ```javascript
  if (window.microApp) {
    // 添加全局数据监听（接收所有全局数据）
    const globalDataListener = (data) => {
      console.log('收到全局数据:', data);
      // 处理全局数据...
    }
    window.microApp.addGlobalDataListener(globalDataListener);
    
    // 添加特定类型的事件监听
    const eventListener = (event) => {
      console.log('收到事件:', event.type, event.data);
      // 处理特定类型的事件数据...
    }
    window.microApp.addDataListener(eventListener);
    
    // 在组件卸载时移除监听器
    onUnmounted(() => {
      window.microApp.removeGlobalDataListener(globalDataListener);
      window.microApp.removeDataListener(eventListener);
    });
  }
  ```

- **基于状态管理的通信**：
  - 共享Redux/Vuex/Pinia等状态管理工具
  - 主应用提供统一的状态管理服务
  ```javascript
  // 主应用暴露共享状态
  window.sharedStore = { ... }
  
  // 子应用使用共享状态
  const store = window.sharedStore
  ```

- **基于Props的通信**：
  - 通过micro-app标签的data属性传递数据
  ```html
  <micro-app name="app-name" data="{prop1: 'value1'}"></micro-app>
  ```

- **基于URL的通信**：
  - 通过URL参数传递简单数据
  - 适合跨页面、首次加载场景

- **基于存储的通信**：
  - localStorage/sessionStorage
  - 需注意同源策略限制

- **跨域通信**：
  - postMessage机制（iframe模式下）
  - 主应用作为通信中介

### 3. 如何处理共享依赖？

**共享依赖的处理方法：**

- **外部引入共享库**：
  - 通过CDN引入公共依赖，减少重复加载
  ```html
  <!-- 主应用中引入 -->
  <script src="https://cdn.jsdelivr.net/npm/vue@3"></script>
  
  <!-- 子应用中配置外部依赖 -->
  // webpack.config.js
  externals: {
    'vue': 'Vue'
  }
  ```

- **主应用注入共享依赖**：
  - 主应用将依赖挂载到全局，子应用使用
  ```javascript
  // 主应用中
  window.sharedLibs = {
    axios: require('axios'),
    lodash: require('lodash')
  }
  
  // 子应用中
  const { axios, lodash } = window.sharedLibs
  ```

- **依赖预加载**：
  - 使用micro-app的预加载功能提前加载依赖
  ```javascript
  window.microApp.preFetchStatic([
    { url: '/static/common.js' }
  ])
  ```

- **使用Module Federation**：
  - Webpack 5的Module Federation实现依赖共享
  ```javascript
  // webpack.config.js
  new ModuleFederationPlugin({
    name: 'app',
    shared: ['react', 'react-dom']
  })
  ```

- **版本控制策略**：
  - 统一依赖版本管理
  - 使用semver规范控制版本兼容性

- **构建优化**：
  - 合理拆分chunk
  - Tree-shaking减小包体积

### 4. 微前端对性能的影响？

**微前端对性能的影响及优化：**

- **性能挑战**：
  - 多应用并行加载增加网络请求
  - JS沙箱运行时开销
  - 样式隔离的性能消耗
  - 通信机制的额外开销

- **首次加载优化**：
  - 应用预加载
  ```javascript
  window.microApp.preFetchApps([{ name: 'app-name', url: 'app-url' }])
  ```
  - 懒加载非关键应用
  - 资源压缩和CDN分发

- **缓存策略**：
  - 合理设置缓存策略
  - 使用Service Worker缓存子应用资源
  ```javascript
  // 在Service Worker中
  self.addEventListener('fetch', (event) => {
    // 缓存子应用资源
  })
  ```

- **资源复用**：
  - 共享公共依赖
  - 避免重复加载相同资源

- **渲染优化**：
  - 避免频繁切换子应用
  - 使用骨架屏减少感知延迟
  - 优先渲染可视区域内容

- **监控与分析**：
  - 性能指标监控
  - 加载时间分析
  - 资源使用情况追踪

### 5. 如何进行微前端的测试？

**微前端测试策略：**

- **单元测试**：
  - 独立测试各应用的组件和功能
  - 使用Jest、Mocha等测试框架
  ```javascript
  // 组件测试示例
  test('组件渲染正确', () => {
    const wrapper = mount(Component)
    expect(wrapper.find('.element').exists()).toBe(true)
  })
  ```

- **集成测试**：
  - 测试子应用与主应用的集成
  - 测试应用间通信机制
  ```javascript
  test('子应用通信正常', async () => {
    // 模拟主应用发送数据
    window.microApp.setData('app-name', testData)
    // 验证子应用接收数据后的行为
    await waitFor(() => expect(result).toEqual(expected))
  })
  ```

- **端到端测试**：
  - 使用Cypress、Playwright等工具
  - 模拟真实用户操作流程
  ```javascript
  // Cypress测试示例
  cy.visit('/')
  cy.get('micro-app[name="app-name"]').should('be.visible')
  cy.get('micro-app[name="app-name"] .button').click()
  cy.get('.result').should('contain', 'Expected Result')
  ```

- **沙箱测试**：
  - 验证JS沙箱隔离效果
  - 测试全局变量污染情况

- **性能测试**：
  - 加载时间测试
  - 内存占用监控
  - 使用Lighthouse等工具分析性能

- **兼容性测试**：
  - 多浏览器测试
  - 不同设备和屏幕尺寸测试

- **测试自动化**：
  - CI/CD流程集成测试
  - 自动化测试报告

### 6. 微前端的未来发展趋势？

**微前端的未来发展趋势：**

- **WebComponents标准化**：随着WebComponents标准的完善和浏览器支持度提高，基于WebComponents的微前端方案（如micro-app）将获得更广泛的应用。

- **低代码/无代码集成**：微前端架构将与低代码/无代码平台深度融合，使业务人员能够通过拖拽等方式组装不同技术栈的微应用。

- **微服务协同发展**：前端微服务与后端微服务将形成更紧密的协作模式，实现完整的端到端微服务架构。

- **智能化加载策略**：基于用户行为分析和机器学习的智能预加载和按需加载策略，进一步优化性能和用户体验。

- **跨平台统一**：微前端架构将扩展到移动端和桌面端，实现一套微前端架构支持Web、移动应用和桌面应用。

- **安全性增强**：更完善的沙箱机制和权限控制，解决微前端架构中的安全隔离问题。

- **开发者体验优化**：更完善的开发、调试和部署工具链，提升微前端开发的效率和体验。

- **模块联邦技术融合**：Webpack Module Federation等技术将与传统微前端方案融合，提供更灵活的依赖共享机制。

- **服务端渲染支持**：更好地支持SSR和SSG，提升首屏加载性能和SEO友好性。

- **边缘计算整合**：与边缘计算结合，在CDN边缘节点进行微前端的组装和渲染，减少网络延迟。

---

## 附录：micro-app配置参考

### 主应用配置

```javascript
// 完整配置示例
window.microApp.start({
  shadowDOM: false,         // 是否开启shadowDOM
  destroy: true,            // 是否在卸载时销毁实例
  inline: false,            // 是否使用内联script
  disableScopecss: false,   // 是否禁用样式隔离
  disableSandbox: false,    // 是否禁用沙箱
  ssr: false                // 是否启用SSR
});
```

### micro-app标签属性

```html
<micro-app
  name="app-name"           // 应用名称（必填）
  url="app-url"             // 应用地址（必填）
  baseroute="/base-path"    // 基础路由
  data="json-data"          // 初始数据
  iframe                    // 是否使用iframe模式
  inline                    // 是否使用内联script
  disableScopecss           // 是否禁用样式隔离
  disableSandbox            // 是否禁用沙箱
></micro-app>
```

### 生命周期钩子

```javascript
// 生命周期钩子示例
document.querySelector('micro-app').addEventListener('created', () => {});
document.querySelector('micro-app').addEventListener('beforemount', () => {});
document.querySelector('micro-app').addEventListener('mounted', () => {});
document.querySelector('micro-app').addEventListener('unmount', () => {});
document.querySelector('micro-app').addEventListener('error', () => {});
```

## 参考资料

- [浅析微前端 micro-app 框架](https://juejin.cn/post/7472210592540065833)
- [微前端micro-app踩坑记录](https://juejin.cn/post/7273072756157481018)
- [万字长文: 如何使用micro-app实现微前端应用](https://juejin.cn/post/7094163332495573023)
- [微前端（micro-app）使用手册](https://juejin.cn/post/7248496133873745976)
- [浅入深出的微前端MicroApp | 京东云技术团队](https://juejin.cn/post/7280786332711632954)