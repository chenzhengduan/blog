import { createApp } from 'vue';
import App from './App.vue';
import { createRouter, createWebHashHistory} from 'vue-router';
import ElementPlus from 'element-plus';
// 声明全局类型
declare global {
  interface Window {
    __MICRO_APP_BASE_ROUTE__?: string;
    __MICRO_APP_ENVIRONMENT__?: boolean;
    __MICRO_APP_NAME__?: string;
    __MICRO_APP_PUBLIC_PATH__?: string;
    microApp?: any;
    microAppPublicPath?: string;
  }
}

// 创建一个简单的路由
const router = createRouter({
  // 使用window.__MICRO_APP_BASE_ROUTE__作为基础路径，这对micro-app集成很重要
  history: createWebHashHistory(window.__MICRO_APP_BASE_ROUTE__ || '/'),
  routes: [
    {
      path: '/home',
      component: () => import('./components/home.vue')
    },
    {
      path: '/appoint',
      component: () => import('./components/AppointCourse.vue')
    }
  ]
});

let app: any = null;

// 将渲染操作放入mount函数
function mount() {
  app = createApp(App);
  app.use(router);
  app.use(ElementPlus);
  // 处理URL查询参数
  app.mount('#app');
}

// 将卸载操作放入unmount函数
function unmount() {
  app?.unmount();
  app = null;
}

// 微前端环境下，注册mount和unmount方法
if (window.__MICRO_APP_ENVIRONMENT__) {
  // 添加对资源路径的处理
  console.log('Running in micro-app environment');
  console.log('Base route:', window.__MICRO_APP_BASE_ROUTE__);
  console.log('App name:', window.__MICRO_APP_NAME__);
  
  // 注册到window对象上，micro-app会调用这些方法
  // 注册动态名称的方法，以防万一
  if (window.__MICRO_APP_NAME__) {
    window[`micro-app-${window.__MICRO_APP_NAME__}`] = { mount, unmount };
    window[`micro-app-${window.__MICRO_APP_NAME__}`].router = router;
  }
  
  // 添加数据监听器，用于接收主应用发送的页面切换指令
  if (window.microApp) {
    window.microApp.addDataListener((data: any) => {
      console.log('接收到主应用发送的数据:', data);
      if(data&&data.targetRoute){
        router.replace(data.targetRoute);
        return
      }
      // 如果数据中包含page字段，则切换到指定页面
      if (data && data.page) {
        console.log('切换到页面:', data.page);
        router.push(data.page);
      }
    });
  }
} else {
  // 非微前端环境直接渲染
  mount();
}
