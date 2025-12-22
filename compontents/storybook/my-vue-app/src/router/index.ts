import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/schedule'
  },
  {
    path: '/test',
    name: 'Test',
    component: () => import('../pages/TestPage.vue')
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('../pages/ScheduleDemo.vue')
  },
  {
    path: '/component-builder',
    name: 'ComponentBuilder',
    component: () => import('../pages/ComponentBuilderDemo.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
