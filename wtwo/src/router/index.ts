import {
    Router,
    createRouter,
    RouteRecordRaw,
    createWebHashHistory,
  } from "vue-router";
  
  import home from './modules/home';
  import schedule from './modules/schedule';
  import exam from './modules/exam';
  import financial from './modules/financial';
  import { getEnv } from "@/utils/domain/env";
  
  const routes: RouteRecordRaw[] = [
    ...home,
    ...exam,
    // 非生产环境才加载排课路由
    ...schedule,
    ...financial,
  ];
  
  const router: Router = createRouter({
    history: createWebHashHistory(),
    routes,
  });

  
  export default router;