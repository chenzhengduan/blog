
一、概述
----

micro-app 是由京东前端团队推出的一款微前端框架，旨在降低上手难度、提升工作效率。MicroApp无关技术栈，也不和业务绑定，可以用于任何前端框架。

micro-app 从组件化的思维，通过 CustomElement 结合自定义的 ShadowDom，将微前端封装成一个类 WebComponent 组件，从而实现微前端的组件化渲染。并且由于自定义 ShadowDom 的隔离特性，micro-app 不需要像 single-spa 和 qiankun 一样要求子应用修改渲染逻辑并暴露出方法，也不需要修改 webpack 配置，是目前市面上接入微前端成本最低的方案，并且提供了 js 沙箱、样式隔离、元素隔离、预加载、资源地址补全、插件系统、数据通信等一系列完善的功能。

micro-app 的特点：

*   上手简单：使用方式类似 iframe
*   侵入性低：对原代码几乎没有影响
*   组件化：基于 webComponents 思想实现微前端
*   功能丰富：JS 沙箱，样式隔离，预加载等
*   轻量的体积：≈10kb (gzip)
*   零依赖：不依赖于任何第三方库

二、整体流程
------

### 2.1、核⼼功能

micro-app 的核⼼功能在CustomElement基础上进⾏构建，CustomElement⽤于创建⾃定义标签，并提供了元素的渲染、卸载、属性修改等钩⼦函数，我们通过钩⼦函数获知微应⽤的渲染时机，并将⾃定义标签作为容器，微应⽤的所有元素和样式作⽤域都⽆法逃离容器边界，从⽽形成⼀个封闭的环境。

![2-1.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/0462c515f1cf4a0dad40fdbe17020a5b~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=Uf4nDPyZAk4qKcthH59T3scwy3s%3D)

基座应用：用来放置子应用（业务项目）的容器，可以和各个子应用进行通信，主要是负责展示子应用。

子应用：就是前端项目，可以是 react 项目、vue 项目、ng 项目等

### 2.2、使用方法

（1）基座（主）应用：

*   在主应用安装micro-app

```bash
npm i @micro-zoe/micro-app --save
```

*   在主应用入口处引入

```javascript
import microApp from '@micro-zoe/micro-app'
microApp.start();
```

*   在页面嵌入子应用

```html
<!-- my-page.vue -->
<template>
  <div>
    <h1>子应用</h1>
    <!--
      name(必传)：应用名称
      url(必传)：应用地址，会被自动补全为http://localhost:3000/index.html
      baseroute(可选)：基座应用分配给子应用的基础路由，就是上面的 `/my-page`
    -->
    <micro-app name='app1' url='http://localhost:3000/' baseroute='/my-page'></micro-app>
  </div>
</template>
```

*   分配一个路由给子应用

```javascript
// router.js 
import Vue from 'vue'
import VueRouter from 'vue-router'
import MyPage from './my-page.vue'
Vue.use(VueRouter)
const routes = [
  {
    // 👇 非严格匹配，/my-page/* 都指向 MyPage 页面
    path: '/my-page/*', // vue-router@4.x path的写法为：'/my-page/:page*'
    name: 'my-page',
    component: MyPage,
  },
]
export default routes
```

（2） 子应用：启动的端口号与 主应用

```javascript
<micro-app name='app1' url='http://localhost:3000/' baseroute='/my-page'></micro-app>
```

url中地址保持一致即可

### 2.3、主体流程图

![流程图.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/41451be7906540588efc73385fc8960a~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=kDtLG%2FMRR3npu3ItCgrHeaLrAg4%3D)

url中地址保持一致即可

### 2.3、主体流程图

![流程图.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/41451be7906540588efc73385fc8960a~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=kDtLG%2FMRR3npu3ItCgrHeaLrAg4%3D)

![主体流程图.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/e0708428dff349f1995e00c462cc2fb0~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=2CtgI7H8EEBxeap2mqkrS9MFLJ0%3D)

*   初始化子应用
*   通过fetch + url 获取子应用的html
*   处理html文本，获取css 和js的资源地址
*   通过fetch获取子应用的静态资源
*   将处理过的html放入webComponent容器中
*   给css 加上scope机制，并append到head标签中
*   在沙箱中执行js代码
*   完成子应用的初始化

三、生命周期
------

用户可在start方法中传入全局周期函数

```javascript
microApp.start({      lifeCycles: {        created () { console.log('created 全局监听') },        beforemount () { console.log('beforemount 全局监听') },        mounted () { console.log('mounted 全局监听') },        unmount () { console.log('unmount 全局监听') },      },  })
```

### 3.1、created

自定义元素挂载到document的时候会触发钩子函数connectedCallback， 触发自定义事件'created',并执行用户传入的全局生命周期函数

```javascript
public connectedCallback (): void {      const cacheCount = ++this.connectedCount;     this.connectStateMap.set(cacheCount, true);     const effectiveApp = this.appName && this.appUrl;     defer(() => {          if (this.connectStateMap.get(cacheCount)) {             
     // 主应用第一次挂载到document的时候执行created生命周期函数              
    dispatchLifecyclesEvent( this, this.appName, lifeCycles.CREATED）              effectiveApp && this.handleConnected()          }     })  }     //dispatchLifecyclesEvent   
    dispatchLifecyclesEvent( this, this.appName, lifeCycles.CREATED )    
     // 触发自定义事件  
      const event = new CustomEvent(lifecycleName, { detail, })    
      // 执行全局周期函数 
       if (isFunction(microApp.options.lifeCycles?.[lifecycleName])) {      microApp.options.lifeCycles![lifecycleName]!(event)  }  element.dispatchEvent(event)
```

### 3.2、beforemount

加载资源完成后，开始渲染之前触发 beforeMounted

```javascript
dispatchLifecyclesEvent( this.container!, this.name, lifeCycles.BEFOREMOUNT, )
```

触发beforemount自定义事件，就会去执行beforemount函数。其他自定义事件mounted、unmount也同理

### 3.3、mounted



触发created自定义事件，就会去执行created函数。其他自定义事件mounted、unmount也同理

复制代码

``<micro-app      name='vue2'      url={`${config.vue2}micro-app/vue2/`}      data={data} onCreated={created}      onMounted={mounted}      onUnmount={unmount} > </micro-app>``


### 3.2、beforemount


```javascript
dispatchLifecyclesEvent( this.container!, this.name, lifeCycles.BEFOREMOUNT, )
```

加载资源完成后，开始渲染之前触发 beforeMounted

### 3.3、mounted

MicroApp 官方在子应用的处理上提供了两种模式：默认模式 和 UMD 模式。

*   默认模式：该模式不需要修改子应用入口，但是在切换时会按顺序依次执行 所有渲染依赖 的js文件，保证每次渲染的效果是一致的
*   UMD 模式：这个模式需要子应用暴露 mounted 和 unmount 方法，只需要首次渲染加载所有 js 文件，后续只执行 mounted 渲染 和 unmount 卸载

官方建议频繁切换的应用使用 UMD 模式配置子应用。

js执行完以后，如果子应用是UMD格式的会去获取子应用上暴露的的生命周期mounted和unmount，如果存在就去执行handleMounted函数执行子应用的mounted生命周期，同时执行dispatchLifecyclesEvent；如果不存在那么直接执行dispatchLifecyclesEvent。

```javascript
const { mounted, unmount } = this.getUmdLibraryHooks()    if (isFunction(mounted) && isFunction(unmount)) {        this.umdHookMount = mount as Func        this.sandBox!.markUmdMode(this.umdMode = true)        try {            this.handleMounted(this.umdHookMount(microApp.getData(this.name, true)))        } catch (e) {           logError('An error occurred in window.mount \n', this.name, e)       }    }    else if (isFinished === true) {        this.handleMounted()    }
```

### 3.4、unmount

当切换子应用的时候当 custom element 从文档 DOM 中删除时，这时候会调用 disconnectedCallback，会执行handleDisconnected; 然后调用app.unmount函数 执行dispatchCustomEventToMicroApp 触发自定义事件

```javascript
public disconnectedCallback (): void {    
    this.connectStateMap.set(this.connectedCount, false)   
    this.handleDisconnected()   
}     
export function dispatchCustomEventToMicroApp ( app: AppInterface, eventName: string, detail: Record<string, any> = {}, ): void {       
    const event = new CustomEvent(formatEventName(eventName, app.name), { detail, })       
    const target = app.iframe ? app.sandBox.microAppWindow : window target.dispatchEvent(event)   
    }
```





四、micro-app的能力
--------------

### 4.1、js 沙箱隔离机制

解决状态互相污染的问题。比如现在有一个vue子应用和react子应用都使用了window上面的count属性，并在各自应用中对这个值进行加减操作，无论哪个应用操作了count，另一个应用的count也会随着变化。就造成了全局变量的污染。目前js沙箱有3种方案实现隔离，micro-app采用的就是with()+Proxy的方式。

![js 沙箱隔离机制.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/698036d68bd743e3862bd661ab26e7ee~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=0q5ssoE1vlWppVQYCjIfFR1DcN4%3D)

#### with()+Proxy

原理就是通过proxy把window对象代理到其他对象上proxyWindow，获取属性的时候优先从proxyWindow获取，如果没有才从window上去获取。设置属性的时候只在proxyWindow上设置。然后通过with把js执行的作用域重置到proxyWindow。每一个子应用都有一个sandbox的属性，因为子应用是通过createAppInstance构造函数实例化来的，所以它的sandbox都是唯一的。拿到script代码后调用app.sandbox.run(scriptCode),在sandbox中去跑script代码。在创建沙箱的时候代理对象也是每个子应用独有的。所以可以避免了不同应用间变量的污染。

![with-proxy.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/ebdaa49bfc08414ea17ea32b7dcc5f55~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=oPDapUQIqsjzFUd5PpLc3eSsRCI%3D)

```javascript
const rawWindow = {} 
function CreateSandbox() { 
     const microWindow = {} 
     const sandbox = { 
        global, // proxy window 
        run, // script run window inject 
        stop, 
        isRun: false 
     } 
     return sandbox 
} 
function execScript(code, sandbox) {
   window.__MICRO__APP = sandbox.global 
   const _code = `;(function(window, self) { with(window) { ${code} } }).call(window.__MICRO__APP,window.__MICRO__APP)` // 规避with严格模式的问题 
   try { 
       (0,eval(_code)) sandbox.isRun = true 
   } catch (error) { } 
 } 
function run (code) {
...
    // 访问fakeWindow的属性，没有的话，从全局window获取 
    
    // 设置fakeWindow的属性，设置到fakeWindow上 
    sandbox.global = new Proxy(fakeWindow, { 
            get(target, key) {   
                if(Reflect.has(target,key)) { 
                    return Reflect.get(target,key) 
                }
                const rawValue = Reflect(rawWindow,key) 
                if (typeof rawValue === 'function') { 
                    const valueStr = rawValue.toString(); // 检查是否是构造函数 alert console 
                        
                    if(!/^function\s+[A-Z]/.test(valueStr) && !/^class\s+/.test(valueStr)) { 
                        return rawValue.bind(rawWindow) 
                    } 
                    } 
                    return rawValue;
            }, 
            set(target, key, value) { 
                target[key] = value 
                return true 
            }
    }) 
        execScript(code,sandbox) 
} 
function stop() { sandbox.isRun = false }

```

#### 快照沙箱

javascript

 体验AI代码助手

 代码解读

复制代码
```javascript
export class SnapShot { 
    proxy: Window & typeof globalThis 
    constructor () { 
        this.proxy = window 
    } 
    // 沙箱激活 
    active () { 
        // 创建一个沙箱快照 
        this.snapshot = new Map() 
        // 遍历全局环境 
        for (const key in window) { 
            this.snapshot[key] = window[key] 
        } 
    } 
    // 沙箱销毁 
    inactive () { 
        for (const key in window) { 
             if (window[key] !== this.snapshot[key]) { 
                 // 还原操作 
                 window[key] = this.snapshot[key] 
             } 
        } 
    } 
    
}

```

在子应用挂载时执行沙箱激活，它会记录window的状态，也就是快照，以便在失活时恢复到之前的状态。在子应用卸载时执行沙箱销毁，恢复到未改变之前的状态。

缺点：（1）window上属性特别多，快照性能消耗很大 (2) 无法支持多个微应用同时运行，多个应用同时改写window上的属性，就会出现状态混乱。这也是为什么快照沙箱无法支持多个微应用同时运行的原因

应用场景：比较老版本的浏览器

### 4.2、样式隔离

micro-app有两种隔离方式：

*   shadowDOM 会将自定义元素里面的内容用shadowDom包裹起来，内部的样式不会影响其他外面的元素样式。优先级高于cssScope,开启shadowDOM后css scope会失效

![样式隔离1.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/aeef04fa4d3444b89730d9d7a44d5d2b~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=L18vf0Hdd4uLkncEYBQe6rwKV%2FY%3D)

*   css scope 如果用户传入了useScopecss会在样式前面加上前缀micro-app\[name=vue3\] vue3是用户传入的子应用的名称,起到子应用之间样式隔离的作用。类似于vue scoped的机制。这是micro-app默认的css 隔离方法

![样式隔离2.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/77a435d4348b4e6ba1ca41b81ed5e113~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=czJfEeHYP63YrxTvw3qEXbTwIxo%3D)

此外，还有一种样式隔离方式css modules，CSS的规则都是全局的，任何一个组件的样式规则，都对整个页面有效。

产生局部作用域的唯一方法，就是使用一个独一无二的class的名字，不会与其他选择器重名。这就是 CSS Modules 的做法。

通过在webpack中做以下设置。optios里设置module: true

![样式隔离3.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/563e6f2622b44760a8b3cc85cc2d95f2~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=GKcp62fTyU026KVGnWrDBb3BQsc%3D)

```javascript
import React from 'react';  import style from './App.css';  export default () => {      return (          <h1 className={style.title}> Hello World </h1>      );  };  ```

上面代码中，我们将样式文件App.css输入到style对象，然后引用style.title 代表一个class，构建工具会将类名style.title编译成一个哈希字符串。

```html
<h1 class="_3zyde4l1yATCOkgn-DBWEL"> Hello World </h1>
```

App.css也会同时被编译。

```css
._3zyde4l1yATCOkgn-DBWEL { color: red; }
```

这样一来，这个类名就变成独一无二了，只对App组件有效。

```html
<h1 class="_3zyde4l1yATCOkgn-DBWEL"> Hello World </h1>
```

App.css也会同时被编译。

```css
._3zyde4l1yATCOkgn-DBWEL { color: red; }
```

这样一来，这个类名就变成独一无二了，只对App组件有效。

复制代码

`._3zyde4l1yATCOkgn-DBWEL { color: red; }`

这样一来，这个类名就变成独一无二了，只对App组件有效。

### 4.3、预加载

![预加载1.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/fe748cb382d048a3b1eb00c7241f533c~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=xKm37e%2BeH6ZQ4MYkkQ5F5GPnnJY%3D)

预加载是指在应用尚未渲染时提前加载资源并缓存，从而提升首屏渲染速度。预加载并不是同步执行的，它会在浏览器空闲时间，依照开发者传入的顺序，依次加载每个应用的静态资源，以确保不会影响基座应用的性能。在应⽤真正被渲染时直接从缓存中获取资源并渲染。通过 microApp.start 配置项的 preFetchApps 属性设置子应用的预加载，或者通过 microApp.preFetch 方法设置。

scss

 体验AI代码助手

 代码解读

复制代码

`/** 子应用列表 */  const apps = [{ name: 'child', url: 'http://localhost:3000' }]  // 方式一  microApp.start({ preFetchApps: apps })  // 方式二  microApp.preFetch(apps)`

会去执行preFetch(options.preFetchApps)，会调用requestIdleCallback去执行资源的预加载，即在浏览器空闲的时候去执行。首先回去判断appInstanceMap对象里面是否存在当前子应用实例。不存在才去执行预加载。

yaml

 体验AI代码助手

 代码解读

复制代码

`if (options.name && options.url && !appInstanceMap.has(options.name)) {      const app = new CreateApp({          name: options.name,          url: options.url,          isPrefetch: true,          scopecss: !(options['disable-scopecss'] ?? options.disableScopecss ?? microApp.options['disable-scopecss']),          useSandbox: !(options['disable-sandbox'] ?? options.disableSandbox ?? microApp.options['disable-sandbox']),          inline: options.inline ?? microApp.options.inline,          iframe: options.iframe ?? microApp.options.iframe,          prefetchLevel: options.level && PREFETCH_LEVEL.includes(options.level)                          ? options.level                          : microApp.options.prefetchLevel && PREFETCH_LEVEL.includes(microApp.options.prefetchLevel) ? microApp.options.prefetchLevel : 2,      })  }`

此时会执行创建应用实例new CreateApp，并且此时isPrefetch被设置为true。此时appInstanceMap中就有两个子应用。会去执行loadSourceCode加载html 资源。并执行css 和js的加载工作，并将结果缓存下来。下次渲染child子应用的时候直接从缓存中获取css和js。这样就可以省去发送请求的耗时，提升渲染速度。

![预加载2.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/a3b9665206494354a4e70c7d4c027d09~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=U28WLs7XCERrEbWWvBtiPUOUJ4k%3D)

### 4.4、数据通信

![数据通信1.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/11dd81b9dffe4efea14aa9c16e0c0619~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=GQXZAGaWXikOChAU1pvRNJrDsIA%3D)

维护了三份数据：全局数据、主应用的数据、微应用的数据

**微应用数据类方法**

微应用可以使用以下方法:

*   addDataListener 绑定监听函数
*   removeDataListener 移除事件监听
*   clearDataListener 清空微应用下的所有监听函数
*   dispatch 微应用向主应用发送数据
*   getData 获取主应用发送过来的数据

ini

 体验AI代码助手

 代码解读

复制代码

`<micro-app        name='vue3'        url='http://localhost:5008'        isPrefetch="true"        :data="data"        @datachange='handleDataChange' >   </micro-app>     const data = ref({from: '来自基座的初始化数据'})`

自己实现简易版数据通信：

javascript

 体验AI代码助手

 代码解读

复制代码

``class EventCenter {      // 缓存数据和绑定函数      eventList = new Map()      on(name, f) {          let eventInfo = this.eventList.get(name)          // 如果没有缓存就初始化          if(!eventInfo) {              eventInfo = { data: {}, callbacks: new Set() }          }          // 放入缓存          this.eventList.set(name, eventInfo)          // 记录绑定函数          eventInfo.callbacks.add(f)      }      // 解除绑定      off (name, f) {          const eventInfo = this.eventList.get(name)          if(eventInfo && typeof f === 'function') {              eventInfo.callbacks.delete(f)          }      }      // 发送数据      dispatch(name,data) {          const eventInfo = this.eventList.get(name)          // 当数据不相等时才更新          if(eventInfo && eventInfo.data !== data) {              eventInfo.data = data             // 遍历执行所有绑定函数             for(const f of eventInfo.callbacks) {                 f(data)             }          }      } } ﻿ // 创建发布订阅对象  const eventCenter = new EventCenter()  // 基座应用的数据通信方法集合  export class EventCenterForBaseApp {      // 向指定子应用发送数据      setData(appName, data) {          eventCenter.dispatch(formatEventName(appName, true), data)      }      // 清空某个子应用的监听函数      clearDataListener(appName) {          eventCenter.off(formatEventName(appName, false))      }  }  // 子应用的数据通信方法集合  export class EventCenterForMicroApp {      constructor(appName) { this.appName = appName }     // 监听基座发送的数据      addDataListener(cb) {          eventCenter.on(formatEventName(this.appName, true), cb)      }            // 解除监听函数      removeDataListener(cb) {          if(typeof cb === 'function') {                 eventCenter.off(formatEventName(this.appName, true), cb)          }     }      // 向基座发送数据      dispatch(data) {          const app = appInstanceMap.get(this.appName)          if(app?.container) {              // 自定义事件              const event = new CustomEvent('datachange', { detail: { data } })              app.container.dispatchEvent(event)          }     }     /** * 清空当前子应用绑定的所有监听函数 */      clearDataListener () {          eventCenter.off(formatEventName(this.appName, true))     }  }  // 格式化订阅名称来进行数据的绑定通信  function formatEventName(appName, fromBaseApp) {      if(typeof appName !== 'string' || !appName)      return fromBaseApp ? `__from_base_app_${appName}__`: `__from_micro_app_${appName}__`  }``

#### 4.4.1 基座向子应用传值

当在上传入data时就会触发重写后的setAttribute， 就会执行BaseAppData.setData(this.getAttribute('name'), cloneValue)会调用EventCenter上的dispatch向对应的子应用发送数据。子应用通过addDataListener监听父应用发出的事件拿到数据。

javascript

 体验AI代码助手

 代码解读

复制代码

`window.microApp.addDataListener(this.handleDataChange, true)`

#### 4.4.2 子应用向基座传值

子应用向父应用传递数据，可以通过

arduino

 体验AI代码助手

 代码解读

复制代码

 `window.microApp.dispatch({text: 'hhh'})`

会调用EventCenterForMicroApp上的dispatch一个自定义事件datachange，主应用就可以通过handleDataChange拿到子应用传递的数据

#### 4.4.3 重写后的setAttribute方法

javascript

 体验AI代码助手

 代码解读

复制代码

`// 记录原生方法  const rawSetAttribute = Element.prototype.setAttribute  // 重写setAttribute  Element.prototype.setAttribute = function setAttribute (key, value) {      // 目标为micro-app标签且属性名称为data时进行处理      if (/^micro-app/i.test(this.tagName) && key === 'data') {          if (toString.call(value) === '[object Object]') {              // 克隆一个新的对象              const cloneValue = {}              Object.getOwnPropertyNames(value).forEach((propertyKey) => {                  // 过滤vue框架注入的数据                  if (!(typeof propertyKey === 'string' && propertyKey.indexOf('__') === 0)) {                     cloneValue[propertyKey] = value[propertyKey]                  }             })              // 发送数据              BaseAppData.setData(this.getAttribute('name'), cloneValue)          }      }      else {          rawSetAttribute.call(this, key, value)      } }`

### 4.5、插件系统

typescript

 体验AI代码助手

 代码解读

复制代码

`plugins: {     modules: {       'react16': [{             loader(code: string, url: string) {               if (code.indexOf('sockjs-node') > -1) {                 console.log('react16插件', url)                 code = code.replace('window.location.port', '3001')               }               return code             }       }],           } },`
﻿

插件可以理解为符合特定规则的对象，对象中提供⼀个函数⽤于对资源进⾏处理，插件通常由开发者⾃定义。

插件系统的作⽤是对传⼊的静态资源进⾏初步处理，并依次调⽤符合条件的插件，将初步处理后的静态资源作为参数传⼊插件，由插件对资源内容进⼀步的修改，并将修改后的内容返回。

插件系统赋予开发者灵活处理静态资源的能⼒，对有问题的资源⽂件进⾏修改。

![插件系统.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/7d9a3404c4a740198e8bc870f0e47eef~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=ZbwJMRyrf8KIn%2BYh9QHv5YgR9PQ%3D)

### 4.6、元素隔离

元素隔离可以有效的防⽌⼦应⽤对基座应⽤和其它⼦应⽤元素的误操作，常⻅的场景是多个应⽤的根元素都使⽤相同的id，元素隔离可以保证⼦应⽤的渲染框架能够正确找到⾃⼰的根元素。

![元素隔离1.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/529ee3f2eba5454b8c822f9dcbc1a50c~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=1aWgAGpA3IigpKHQNPN%2F5xJopKM%3D)

如上图所示， micro-app 元素内部渲染的就是⼀个⼦应⽤，它还有两个⾃定义元素micro-app-head 、micro-app-body ，这两个元素的作⽤分别对应html中的head和body元素。⼦应⽤在原head元素中的内容和⼀些动态创建并插⼊head的link、script元素都会移动到micro-app-head 中，在原body元素中的内容和⼀些动态创建并插⼊body的元素都会移动到micro-app-body 中。这样可以防⽌⼦应⽤的元素泄漏到全局，在进⾏元素查询、删除等操作时，只需要在micro-app 内部进⾏处理，是实现元素隔离的重要基础。

![元素隔离2.png](https://p9-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/ac5d28924cd741918aafdb9b805e728a~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgQXJpZWxfamh5:q75.awebp?rk3s=f64ab15b&x-expires=1748852868&x-signature=qC8qDJcZuwI2aaj3difcaHTcwLo%3D)

举个栗子🌰 :

基座应用和子应用都有一个元素，此时子应用通过document.querySelector('#root')获取到的是自己内部的#root元素，而不是基座应用的。这是因为改写了document querySelector 方法。

子应用有appName所以执行querySelector函数的时候会执行appInstanceMap.get(appName)?.container?.querySelector(selectors)??null。

如果是基座的话没有appName 就会执行globalEnv.rawQuerySelector.call(this,selectors)。

五、源码解析
------

### 5.1、MicroApp类

MicroApp的外层类, 使用者通过start方法启动microApp

scss

 体验AI代码助手

 代码解读

复制代码

`microApp = newMicroApp() microApp.start()`

START方法中主要做了三件事

*   挂载定义好的app操作函数
*   initGlobalEnv() 初始化全局变量
*   defineElement(this.tagName) 定义custom-element

源码摘要:

ini

 体验AI代码助手

 代码解读

复制代码

`export class MicroApp extends EventCenterForBaseApp implements MicroAppBaseType {   tagName = 'micro-app'   options: OptionsType = {}   // 挂载定义好的app操作函数   preFetch = preFetch   unmountApp = unmountApp   unmountAllApps = unmountAllApps   getActiveApps = getActiveApps   getAllApps = getAllApps   reload = reload   renderApp = renderApp   // start方法   start (options?: OptionsType): void {       ...       initGlobalEnv()       ...       defineElement(this.tagName)       }   }`
﻿

#### 5.1.1 initGlobalEnv 初始化全局变量

首先这里定义了原始window,document等, 并将一些原始方法从Element中取出并保存,以便以后可以直接从globalENV中获取到原始方法

其作用是为了服务沙箱, 沙箱内需要修改一些方法，比如`window.getElementById`等

javascript

 体验AI代码助手

 代码解读

复制代码

`export function initGlobalEnv (): void {   if (isBrowser) {     const rawWindow = Function('return window')()     const rawDocument = Function('return document')()     const rawRootDocument = Function('return Document')()     /**      * save patch raw methods      * pay attention to this binding      */     // 将一些Element上的原始方法取出保存     const rawSetAttribute = Element.prototype.setAttribute     const rawAppendChild = Element.prototype.appendChild     const rawRemoveChild = Element.prototype.removeChild     ...      // 将一些document上的原始方法取出保存     const rawCreateElement = rawRootDocument.prototype.createElement     const rawQuerySelector = rawRootDocument.prototype.querySelector     const rawGetElementById = rawRootDocument.prototype.getElementById          // 代理Image元素     const ImageProxy = new Proxy(Image, {...}) ﻿     /**      * save effect raw methods      * pay attention to this binding, especially setInterval, setTimeout, clearInterval, clearTimeout      */     // 将window原始方法拿出来保存 比如addEventListener  setInterval  setTimeout等     const rawWindowAddEventListener = rawWindow.addEventListener     const rawSetInterval = rawWindow.setInterval     const rawSetTimeout = rawWindow.setTimeout     ...      // 将document原始方法拿出来保存 addEventListener removeEventListener     const rawDocumentAddEventListener = rawDocument.addEventListener     const rawDocumentRemoveEventListener = rawDocument.removeEventListener ﻿     // 修改全局变量,表示baseApp运行     window.__MICRO_APP_BASE_APPLICATION__ = true ﻿     // 把以上方法用Object.assign合并到globalEnv对象中     assign(globalEnv, {...}) ﻿     // 给baseApp设置一个初始head body样式      // micro-app-body { display: block; } ; micro-app-head { display: none; }     rejectMicroAppStyle()   } }`

### 5.2、MicroAppElement类

start中主要会defineElement() 定义并创建一个MicroAppElement实例,**也就是webComponent,自定义dom元素**

scala

 体验AI代码助手

 代码解读

复制代码

`export function defineElement (tagName: string): void {    // 定义自定义元素    class MicroAppElement extends HTMLElement implements MicroAppElementType {     // 监视标签中的'name', 'url'属性 改变时触发回调,进行connect     static get observedAttributes (): string[] {       return ['name', 'url']     }        ......    }    // 注册元素(这里tagName初始就是"micro-app")   globalEnv.rawWindow.customElements.define(tagName, MicroAppElement) }`
﻿

**handleConnect链接app**

当我们设置元素的name和url后, 元素会首先触发attributeChangedCallback,执行handleInitialNameAndUrl方法

而后执行handleConnect

*   handleConnect中会初始化shadowDOM,updateSsrUrl,对KeepAliveApp做处理等等
*   最终会执行handleCreateApp

**handleCreateApp创建App实例**

kotlin

 体验AI代码助手

 代码解读

复制代码

    `// create app instance     private handleCreateApp (): void {       // 如果有app存在先销毁app       if (appInstanceMap.has(this.appName)) {         appInstanceMap.get(this.appName)!.actionsForCompletelyDestroy()       }       new CreateApp({         name: this.appName,         url: this.appUrl,         scopecss: this.isScopecss(),         useSandbox: this.isSandbox(),         inline: this.getDisposeResult('inline'),         esmodule: this.getDisposeResult('esmodule'),         container: this.shadowRoot ?? this,         ssrUrl: this.ssrUrl,       })     }`
﻿

### 5.3、CreateApp类

App核心类 用于创建一个App实例

#### 5.3.1 获取app资源

跟single-SPA一样 首先我们应该获取一个微前端应用的三大资源 **js css html**

kotlin

 体验AI代码助手

 代码解读

复制代码

`class CreateApp implements AppInterface {     constructor(){         ...         this.loadSourceCode()     }          public loadSourceCode (): void {         this.state = appStates.LOADING         HTMLLoader.getInstance().run(this, extractSourceDom) // run获取资源   } }`
﻿

我们创建一个htmlLoader并执行run方法

> 提一嘴htmlLoader的单例模式,这里使用HTMLLoader.getInstance()方法获取单例,保证获取对象的唯一性

arduino

 体验AI代码助手

 代码解读

复制代码

`export class HTMLLoader implements IHTMLLoader {   private static instance: HTMLLoader;   public static getInstance (): HTMLLoader {     if (!this.instance) {       this.instance = new HTMLLoader()     }     return this.instance   }   ... }`

**通过简单的fetch方法即可通过url "localhost:3000" 获取到html字符串**

javascript

 体验AI代码助手

 代码解读

复制代码

  `window.fetch(url, options).then((res: Response) => {     return res.text()   })`

**传入的extractSourceDom方法作为html字符串的回调,获取对应的script和links**

scss

 体验AI代码助手

 代码解读

复制代码

`export function extractSourceDom (htmlStr: string, app: AppInterface): void {     if (app.source.links.size) {       fetchLinksFromHtml(wrapElement, app, microAppHead, fiberStyleResult) // fetchLinks       }        ...     if (app.source.scripts.size) {       fetchScriptsFromHtml(wrapElement, app)    // fetchScripts       }       ... }`

#### 5.3.2 检测资源是否获取完毕

封装了PromiseStream方法来获取scripts和links(一个html中往往有多个脚本和样式), 这是一个用于瀑布流式执行(one by one)多个Promise的函数

javascript

 体验AI代码助手

 代码解读

复制代码

`export function promiseStream <T> (   promiseList: Array<Promise<T> | T>,   successCb: CallableFunction,   errorCb: CallableFunction,   finallyCb?: CallableFunction, ): void {     ... }`

我们可以看到 调用次函数传入的finallyCb中 一定会执行app.onload方法

scss

 体验AI代码助手

 代码解读

复制代码

`promiseStream<string>(     promiseList,     successCb,     errorCb,     () => {       if (fiberLinkTasks) {         fiberStyleResult!.then(() => {           fiberLinkTasks.push(() => Promise.resolve(app.onLoad(wrapElement))) // resolve执行onload           serialExecFiberTasks(fiberLinkTasks)         })       } else {         app.onLoad(wrapElement)  // 直接执行onload       }     }) )`

**将fetch来的资源保存到sourceCenter中**

使用`sourceCenter`来缓存获取的资源,以便复用。在`extractLinkFromHtml`中我们可以看到,将link代码包装为linkInfo后存入`sourceCenter`,当然script同理

javascript

 体验AI代码助手

 代码解读

复制代码

`export function extractLinkFromHtml(){     ...    sourceCenter.link.setInfo(href, linkInfo) }`

#### 5.3.3 App.onload

只有第三次触发onload方法,才会真正开始执行, 也就是当links和scripts成功获取并执行对应finally回调后,才会执行。

kotlin

 体验AI代码助手

 代码解读

复制代码

`public onLoad (     html: HTMLElement,     defaultPage?: string,     disablePatchRequest?: boolean,   ): void {      if(++this.loadSourceLevel === 2){// 每次执行onload++             // 非preFetch时,直接获取container,执行mount方法       if (!this.isPrefetch && appStates.UNMOUNT !== this.state) {         getRootContainer(this.container!).mount(this)       }              // preFetch时, 创建一个div作为container,执行mount方法       else if (this.isPrerender) {         const container = pureCreateElement('div')         container.setAttribute('prerender', 'true')         this.sandBox?.setPreRenderState(true)         this.mount({           container,           inline: this.inline,           useMemoryRouter: true,           baseroute: '',           fiber: true,           esmodule: this.esmodule,           defaultPage: defaultPage ?? '',           disablePatchRequest: disablePatchRequest ?? false,                    })       }     }   }`

#### 5.4.4 mount

mount中我们会

*   测试shadowDom
*   开启沙箱 this.sandBox?.start
*   执行脚本 execScripts -->runScript

execScripts中执行的代码从**sourceCenter**中获取到的资源,需要在之前提供的沙箱中执行。至此，js代码执行完毕，页面上就能正常的渲染出一个页面。

#### 5.5.5 沙箱 patch与releasePatch

微应用作用与沙箱环境中,在进入沙箱时,我们需要修改document,Element上的诸多dom操作方法。

**在init时保存到globalEnv中的原始方法现在起作用了**

*   patch: 可以理解为:修改方法
*   releasePatch: 将修改的原生方法还原

**要修改原生方法，这里主要做两件事**

*   修改this指向 : 如果当前document是Proxy代理后的document,则this指向原始document,如果不是则this指向当前document
*   给创建的元素做标记

我们以microApp中的patchDocument方法举例

javascript

 体验AI代码助手

 代码解读

复制代码

`function patchDocument () {     // 从globalEnv中获取原始document       const rawDocument = globalEnv.rawDocument       const rawRootDocument = globalEnv.rawRootDocument     // 获取需要指向的this      function getBindTarget (target: Document): Document {        return isProxyDocument(target) ? rawDocument : target      }     // 给创建的element打上标记     function markElement <T extends { __MICRO_APP_NAME__: string }> (element: T): T {       const currentAppName = getCurrentAppName()       if (currentAppName) element.__MICRO_APP_NAME__ = currentAppName       return element     }     // 修改rawRootDocument.prototype.createElement上的createElement方法    rawRootDocument.prototype.createElement = function createElement (       tagName: string,       options?: ElementCreationOptions     ): HTMLElement     {      const element = globalEnv.rawCreateElement.call(getBindTarget(this), tagName, options)      return markElement(element)    }      // 后面还修改了很多dom操作方法 如   rawRootDocument.prototype.createElementNS = function createElementNS(){...}   rawRootDocument.prototype.createDocumentFragment = function createDocumentFragment(){...}   rawRootDocument.prototype.querySelector = function querySelector(){...}   rawRootDocument.prototype.querySelectorAll = function querySelectorAll(){...}   rawRootDocument.prototype.getElementById = function getElementById(){...}   rawRootDocument.prototype.getElementsByClassName = function getElementsByClassName(){...}   ... }`
﻿

同样的 对于Element对象上操作dom方法也进行了修改,并将对应的操作封装到了commonElementHandler方法里

releasePatches \*\*\*\*将修改过的方法还原，patchAttrbuilt需要在MicroAppElement创建时执行，做特殊处理

ini

 体验AI代码助手

 代码解读

复制代码

﻿
`// release patch export function releasePatches (): void {   removeDomScope()   releasePatchDocument() // 还原document方法       // 还原Element方法   Element.prototype.appendChild = globalEnv.rawAppendChild   Element.prototype.insertBefore = globalEnv.rawInsertBefore   Element.prototype.replaceChild = globalEnv.rawReplaceChild   Element.prototype.removeChild = globalEnv.rawRemoveChild   Element.prototype.append = globalEnv.rawAppend   Element.prototype.prepend = globalEnv.rawPrepend   Element.prototype.cloneNode = globalEnv.rawCloneNode   Element.prototype.querySelector = globalEnv.rawElementQuerySelector   Element.prototype.querySelectorAll = globalEnv.rawElementQuerySelectorAll   //DefineProperty 方法特殊处理   rawDefineProperty(Element.prototype, 'innerHTML', globalEnv.rawInnerHTMLDesc)    }`
﻿

参考：
---

源码地址：[github.com/micro-zoe/m…](https://link.juejin.cn?target=https%3A%2F%2Fgithub.com%2Fmicro-zoe%2Fmicro-app "https://github.com/micro-zoe/micro-app")﻿

官网地址： [micro-zoe.github.io/micro-app/](https://link.juejin.cn?target=https%3A%2F%2Fmicro-zoe.github.io%2Fmicro-app%2F "https://micro-zoe.github.io/micro-app/")﻿

﻿[juejin.cn/post/723369…](https://juejin.cn/post/7233697025711013943 "https://juejin.cn/post/7233697025711013943")﻿

﻿[juejin.cn/post/717768…](https://juejin.cn/post/7177684506952859706 "https://juejin.cn/post/7177684506952859706")