# 📊 WonePC 微前端改造培训讲义

## 📌 培训目标
- 理解 micro-app 微前端原理
- 学会在 WonePC 项目中接入子应用
- 明确组内开发规范

## 📌 项目背景介绍
- WonePC 项目 10+年历史 Avalon 架构
- 子应用采用 Vue3 技术栈
- 使用 micro-app 框架实现微前端

## 📌 什么是 micro-app？
👉 多团队协作、子应用独立运行、独立部署的前端架构方案

👉 适合场景
- 老旧项目逐步重构
- 多团队开发，技术栈不统一
- 新功能局部替换旧页面

## 📌 为什么选 micro-app？
- 框架无关
- 沙箱隔离
- 灵活通信
- 渐进式改造

## 📌 WonePC 实战接入方式
### 主应用写法
```html
<script src="js/libs/micro-app/index.umd.js"></script>
<micro-app name="vue3-app-appoint" url="/vue-app?page=appoint" iframe></micro-app>
```

### 子应用部署路径
`/vue-app`

### 注意事项
- iframe 模式避免样式污染
- history 模式配置 base

## 📌 生命周期钩子规范
| 阶段    | 必须做的事            |
|---------|---------------------|
| mount   | 初始化事件、页面逻辑 |
| unmount | 清理事件、定时器     |

## 📌 通信机制规范
推荐 `dispatch` + `addDataListener`

### 通信事件命名规范
`micro_app:{模块}:{动作}`

例：
- `micro_app:appoint:created`
- `micro_app:user:loginSuccess`

## 📌 静态资源规范
- 相对路径
- 路径加版本号

## 📌 样式隔离规范
- iframe 模式天然隔离
- 子应用 CSS Modules 或 scoped

## 📌 常见问题
| 问题         | 解决方案              |
|--------------|-----------------------|
| 子应用样式污染 | iframe / CSS Modules  |
| 通信事件冲突   | 统一命名规范          |
| 刷新 404      | base + nginx fallback |
| 生命周期未清理 | unmount 阶段销毁监听器 |

## 📌 QA 环节
- 通信慢怎么排查？
- 子应用多了性能影响？
- 能不用 iframe 吗？

## 📌 培训总结
- WonePC 微前端基建先行
- 规范先行，避免踩坑
- 推进 2 个子应用 demo
