# 优化 Element UI 表格样式，隐藏滚动条但保持滚动功能

## 前言

在基于 Element UI 的项目中，`el-table` 是非常常用的表格组件。默认情况下，表格的滚动条可能影响页面的美观，特别是在视觉设计上希望更简洁时。本文分享一段优化的 CSS 代码，帮助你：

- 让表格宽度撑满父容器
- 移除表格默认伪元素的边框装饰
- 隐藏滚动条，但依然保留滚动功能，兼容主流浏览器（Chrome、Firefox、IE/Edge）

---


```css
/* 设置表格宽度为100% */
::v-deep table {
  width: 100% !important;
}

/* 移除 el-table 伪元素边框装饰 */
::v-deep .el-table::before,
::v-deep .el-table::after {
  display: none !important;
}

/* 隐藏滚动条但保留滚动功能（webkit 内核） */
::v-deep .el-table__body-wrapper::-webkit-scrollbar {
  width: 0 !important;
  height: 0 !important;
  display: none;
}

/* 隐藏滚动条但保留滚动功能（Firefox 和 IE） */
::v-deep .el-table__body-wrapper {
  overflow: auto;
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;     /* Firefox */
}