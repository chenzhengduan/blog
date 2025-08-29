# 📄 WonePC 微前端开发规范

## 📌 应用命名规范
- 主应用：WonePC
- 子应用：WonePC-Vue

## 📌 路由规范
- 主应用 iframe 子应用，path 加 query 标识

## 📌 通信事件命名规范
`micro_app:{模块}:{动作}`

## 📌 生命周期钩子规范
- mount：初始化事件
- unmount：移除事件监听

## 📌 样式隔离规范
- iframe 隔离
- CSS Modules / scoped

## 📌 静态资源路径规范
- 相对路径
- 加版本号

## 📌 子应用 props 约定
| 参数名      | 类型   | 说明               |
|-------------|--------|------------------|
| baseUrl     | string | 接口基础地址       |
| userInfo    | object | 当前用户信息       |
| permissions | array  | 权限列表           |
