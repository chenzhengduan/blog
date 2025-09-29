# 现代CSS技术栈：从Flexbox到Grid的布局演进与实践

> CSS布局技术在过去几年中取得了长足的进步，从传统的盒模型到现代的Flexbox和Grid，开发者拥有了更强大的布局能力。本文将深入探讨现代CSS技术栈的核心概念、实际应用和最佳实践。

## 引言

CSS布局技术的发展反映了Web应用从简单文档到复杂应用的演进过程。随着用户界面需求的日益复杂，CSS布局技术也在不断进化。本文将系统地介绍现代CSS布局技术，帮助开发者构建更加灵活、响应式的用户界面。

## 一、CSS布局基础

### 1.1 盒模型与定位

```css
/* 标准盒模型 */
.standard-box {
  box-sizing: content-box;
  width: 300px;
  padding: 20px;
  border: 2px solid #333;
  /* 实际宽度 = 300 + 20*2 + 2*2 = 344px */
}

/* 替代盒模型 */
.border-box {
  box-sizing: border-box;
  width: 300px;
  padding: 20px;
  border: 2px solid #333;
  /* 实际宽度 = 300px */
}

/* 定位系统 */
.positioning-demo {
  /* 相对定位 */
  position: relative;
  top: 10px;
  left: 10px;
}

.absolute-demo {
  /* 绝对定位 */
  position: absolute;
  top: 0;
  right: 0;
}

.fixed-demo {
  /* 固定定位 */
  position: fixed;
  bottom: 20px;
  right: 20px;
}

.sticky-demo {
  /* 粘性定位 */
  position: sticky;
  top: 0;
  background: white;
  z-index: 100;
}
```

### 1.2 显示类型与浮动

```css
/* 显示类型 */
.block {
  display: block;
  width: 100%;
}

.inline {
  display: inline;
  /* 不能设置宽高 */
}

.inline-block {
  display: inline-block;
  width: 100px;
  height: 50px;
}

.none {
  display: none;
  /* 不占据空间 */
}

/* 浮动布局（已不推荐） */
.float-container {
  overflow: hidden; /* 清除浮动 */
}

.float-left {
  float: left;
  width: 50%;
}

.float-right {
  float: right;
  width: 50%;
}

/* 清除浮动的现代方法 */
.clearfix::after {
  content: '';
  display: table;
  clear: both;
}
```

## 二、Flexbox布局系统

### 2.1 Flexbox基础概念

```css
/* Flexbox容器 */
.flex-container {
  display: flex;
  /* 或 */
  display: inline-flex;
}

/* 主轴方向 */
.flex-row {
  flex-direction: row; /* 默认值 */
}

.flex-row-reverse {
  flex-direction: row-reverse;
}

.flex-column {
  flex-direction: column;
}

.flex-column-reverse {
  flex-direction: column-reverse;
}

/* 换行行为 */
.flex-nowrap {
  flex-wrap: nowrap; /* 默认值 */
}

.flex-wrap {
  flex-wrap: wrap;
}

.flex-wrap-reverse {
  flex-wrap: wrap-reverse;
}

/* 简写形式 */
.flex-flow: row wrap;
```

### 2.2 主轴与交叉轴对齐

```css
/* 主轴对齐 */
.justify-start {
  justify-content: flex-start; /* 默认值 */
}

.justify-end {
  justify-content: flex-end;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.justify-around {
  justify-content: space-around;
}

.justify-evenly {
  justify-content: space-evenly;
}

/* 交叉轴对齐 */
.align-items-start {
  align-items: flex-start;
}

.align-items-end {
  align-items: flex-end;
}

.align-items-center {
  align-items: center;
}

.align-items-baseline {
  align-items: baseline;
}

.align-items-stretch {
  align-items: stretch; /* 默认值 */
}

/* 多行对齐 */
.align-content-start {
  align-content: flex-start;
}

.align-content-end {
  align-content: flex-end;
}

.align-content-center {
  align-content: center;
}

.align-content-between {
  align-content: space-between;
}

.align-content-around {
  align-content: space-around;
}

.align-content-stretch {
  align-content: stretch; /* 默认值 */
}
```

### 2.3 Flex项目属性

```css
/* Flex项目的伸缩性 */
.flex-item-1 {
  flex: 1; /* flex: 1 1 0% */
}

.flex-item-2 {
  flex: 2; /* flex: 2 1 0% */
}

.flex-item-grow {
  flex-grow: 1;
}

.flex-item-shrink {
  flex-shrink: 1;
}

.flex-item-basis {
  flex-basis: 200px;
}

/* 自定义flex值 */
.flex-custom {
  flex: 0 1 300px; /* 不增长，可收缩，基础宽度300px */
}

/* 对齐方式 */
.align-self-start {
  align-self: flex-start;
}

.align-self-end {
  align-self: flex-end;
}

.align-self-center {
  align-self: center;
}

.align-self-baseline {
  align-self: baseline;
}

.align-self-stretch {
  align-self: stretch;
}

/* 排序 */
.order-1 {
  order: 1;
}

.order-2 {
  order: 2;
}

.order-negative {
  order: -1;
}
```

### 2.4 Flexbox实际应用

```css
/* 垂直居中 */
.vertical-center {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

/* 导航栏布局 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #333;
  color: white;
}

.navbar-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.navbar-nav {
  display: flex;
  gap: 1rem;
}

/* 卡片布局 */
.card-container {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.card {
  flex: 1 1 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  padding: 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.card-body {
  padding: 1rem;
}

/* 等高列 */
.equal-height-columns {
  display: flex;
  gap: 1rem;
}

.column {
  flex: 1;
  background: #f8f9fa;
  padding: 1rem;
}

/* 粘性页脚 */
.sticky-footer-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sticky-footer-header {
  padding: 2rem;
  background: #333;
  color: white;
}

.sticky-footer-main {
  flex: 1;
  padding: 2rem;
}

.sticky-footer-footer {
  padding: 1rem;
  background: #f8f9fa;
  border-top: 1px solid #dee2e6;
}
```

## 三、CSS Grid布局系统

### 3.1 Grid基础概念

```css
/* Grid容器 */
.grid-container {
  display: grid;
  /* 或 */
  display: inline-grid;
}

/* 定义网格轨道 */
.grid-columns {
  grid-template-columns: 1fr 2fr 1fr;
}

.grid-rows {
  grid-template-rows: 100px auto 100px;
}

/* 使用repeat()函数 */
.grid-repeat {
  grid-template-columns: repeat(3, 1fr);
}

/* 使用minmax()函数 */
.grid-minmax {
  grid-template-columns: minmax(200px, 1fr);
}

/* 使用auto-fit和auto-fill */
.grid-auto-fit {
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
}

.grid-auto-fill {
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
}

/* 网格间距 */
.grid-gap {
  gap: 20px;
  /* 或 */
  row-gap: 20px;
  column-gap: 20px;
}
```

### 3.2 Grid项目定位

```css
/* 使用Grid线定位 */
.grid-item-1 {
  grid-column-start: 1;
  grid-column-end: 3;
  grid-row-start: 1;
  grid-row-end: 2;
}

/* 简写形式 */
.grid-item-2 {
  grid-column: 1 / 3;
  grid-row: 1 / 2;
}

/* 使用span关键词 */
.grid-item-3 {
  grid-column: span 2;
  grid-row: span 1;
}

/* 使用Grid区域 */
.grid-container-areas {
  display: grid;
  grid-template-areas:
    "header header header"
    "sidebar main aside"
    "footer footer footer";
  grid-template-columns: 200px 1fr 200px;
  grid-template-rows: 80px 1fr 60px;
}

.header { grid-area: header; }
.sidebar { grid-area: sidebar; }
.main { grid-area: main; }
.aside { grid-area: aside; }
.footer { grid-area: footer; }
```

### 3.3 Grid高级特性

```css
/* 自动布局 */
.grid-auto-flow {
  grid-auto-flow: row; /* 默认值 */
}

.grid-auto-flow-column {
  grid-auto-flow: column;
}

.grid-auto-flow-dense {
  grid-auto-flow: dense;
}

/* 自动轨道大小 */
.grid-auto-rows {
  grid-auto-rows: 100px;
}

.grid-auto-columns {
  grid-auto-columns: 100px;
}

/* 对齐方式 */
.justify-items-start {
  justify-items: start;
}

.justify-items-end {
  justify-items: end;
}

.justify-items-center {
  justify-items: center;
}

.justify-items-stretch {
  justify-items: stretch;
}

.align-items-start {
  align-items: start;
}

.align-items-end {
  align-items: end;
}

.align-items-center {
  align-items: center;
}

.align-items-stretch {
  align-items: stretch;
}

/* 简写形式 */
.place-items: center center;

/* Grid轨道对齐 */
.justify-content-start {
  justify-content: start;
}

.justify-content-end {
  justify-content: end;
}

.justify-content-center {
  justify-content: center;
}

.justify-content-stretch {
  justify-content: stretch;
}

.justify-content-space-between {
  justify-content: space-between;
}

.justify-content-space-around {
  justify-content: space-around;
}

.justify-content-space-evenly {
  justify-content: space-evenly;
}

/* 子网格（Subgrid） */
.subgrid-container {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  grid-template-rows: auto auto auto;
}

.subgrid-item {
  display: grid;
  grid-template-columns: subgrid;
  grid-template-rows: subgrid;
  grid-column: 1 / -1;
}
```

### 3.4 Grid实际应用

```css
/* 经典的12列网格系统 */
.grid-12 {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 1rem;
}

.col-12 { grid-column: span 12; }
.col-11 { grid-column: span 11; }
.col-10 { grid-column: span 10; }
.col-9 { grid-column: span 9; }
.col-8 { grid-column: span 8; }
.col-7 { grid-column: span 7; }
.col-6 { grid-column: span 6; }
.col-5 { grid-column: span 5; }
.col-4 { grid-column: span 4; }
.col-3 { grid-column: span 3; }
.col-2 { grid-column: span 2; }
.col-1 { grid-column: span 1; }

/* 响应式网格 */
.responsive-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

/* 复杂页面布局 */
.page-layout {
  display: grid;
  grid-template-areas:
    "header header header"
    "sidebar main aside"
    "footer footer footer";
  grid-template-columns: 250px 1fr 200px;
  grid-template-rows: auto 1fr auto;
  min-height: 100vh;
}

@media (max-width: 768px) {
  .page-layout {
    grid-template-areas:
      "header"
      "main"
      "sidebar"
      "aside"
      "footer";
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto auto;
  }
}

/* 图片画廊 */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  grid-auto-rows: 200px;
  gap: 1rem;
  grid-auto-flow: dense;
}

.gallery-item {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.gallery-item.wide {
  grid-column: span 2;
}

.gallery-item.tall {
  grid-row: span 2;
}

.gallery-item.large {
  grid-column: span 2;
  grid-row: span 2;
}

/* 表格式布局 */
.table-grid {
  display: grid;
  grid-template-columns: 100px 1fr 150px 100px;
  gap: 1px;
  background: #dee2e6;
}

.table-header {
  background: #f8f9fa;
  font-weight: bold;
  padding: 0.5rem;
}

.table-cell {
  background: white;
  padding: 0.5rem;
}
```

## 四、Flexbox与Grid的结合使用

### 4.1 组合布局策略

```css
/* Flexbox + Grid 组合 */
.flex-grid-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.flex-grid-header {
  background: #333;
  color: white;
  padding: 1rem;
}

.flex-grid-main {
  flex: 1;
  display: grid;
  grid-template-columns: 250px 1fr;
  grid-template-areas:
    "sidebar main";
}

.flex-grid-sidebar {
  grid-area: sidebar;
  background: #f8f9fa;
  padding: 1rem;
}

.flex-grid-content {
  grid-area: main;
  padding: 1rem;
}

.flex-grid-footer {
  background: #f8f9fa;
  padding: 1rem;
  border-top: 1px solid #dee2e6;
}

/* 卡片内部的Grid布局 */
.card-flex-grid {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  padding: 1rem;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.card-body {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  padding: 1rem;
}

.card-footer {
  padding: 1rem;
  background: #f8f9fa;
  border-top: 1px solid #dee2e6;
}
```

### 4.2 响应式设计模式

```css
/* 响应式布局模式 */
.responsive-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
}

@media (min-width: 768px) {
  .responsive-layout {
    grid-template-columns: 250px 1fr;
  }
}

@media (min-width: 1024px) {
  .responsive-layout {
    grid-template-columns: 250px 1fr 200px;
  }
}

/* 内容优先的响应式布局 */
.content-first-layout {
  display: grid;
  grid-template-areas:
    "header"
    "main"
    "sidebar"
    "aside"
    "footer";
  grid-template-columns: 1fr;
  gap: 1rem;
}

@media (min-width: 768px) {
  .content-first-layout {
    grid-template-areas:
      "header header"
      "main sidebar"
      "aside aside"
      "footer footer";
    grid-template-columns: 2fr 1fr;
  }
}

@media (min-width: 1024px) {
  .content-first-layout {
    grid-template-areas:
      "header header header"
      "sidebar main aside"
      "footer footer footer";
    grid-template-columns: 250px 1fr 200px;
  }
}

/* 导航栏响应式设计 */
.responsive-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #333;
  color: white;
}

.nav-menu {
  display: flex;
  gap: 1rem;
}

.nav-toggle {
  display: none;
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: #333;
    flex-direction: column;
    padding: 1rem;
  }

  .nav-menu.active {
    display: flex;
  }

  .nav-toggle {
    display: block;
  }
}
```

## 五、现代CSS技术

### 5.1 CSS自定义属性

```css
/* CSS变量定义 */
:root {
  /* 颜色系统 */
  --primary-color: #007bff;
  --secondary-color: #6c757d;
  --success-color: #28a745;
  --danger-color: #dc3545;
  --warning-color: #ffc107;
  --info-color: #17a2b8;
  --light-color: #f8f9fa;
  --dark-color: #343a40;

  /* 间距系统 */
  --spacing-unit: 8px;
  --spacing-sm: calc(var(--spacing-unit) * 2);
  --spacing-md: calc(var(--spacing-unit) * 3);
  --spacing-lg: calc(var(--spacing-unit) * 4);
  --spacing-xl: calc(var(--spacing-unit) * 6);

  /* 字体系统 */
  --font-size-base: 16px;
  --font-size-sm: 14px;
  --font-size-lg: 18px;
  --font-size-xl: 24px;
  --font-size-xxl: 32px;

  /* 圆角系统 */
  --border-radius-sm: 4px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
  --border-radius-xl: 16px;

  /* 阴影系统 */
  --shadow-sm: 0 2px 4px rgba(0,0,0,0.1);
  --shadow-md: 0 4px 6px rgba(0,0,0,0.1);
  --shadow-lg: 0 10px 15px rgba(0,0,0,0.1);
  --shadow-xl: 0 20px 25px rgba(0,0,0,0.1);
}

/* 使用CSS变量 */
.button {
  background: var(--primary-color);
  color: white;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius-md);
  font-size: var(--font-size-base);
  box-shadow: var(--shadow-sm);
  border: none;
  cursor: pointer;
}

.button:hover {
  background: var(--primary-color-dark);
  box-shadow: var(--shadow-md);
}

/* 动态主题 */
[data-theme="dark"] {
  --primary-color: #4dabf7;
  --secondary-color: #868e96;
  --light-color: #212529;
  --dark-color: #f8f9fa;
  --body-bg: #121212;
  --body-color: #e9ecef;
}

/* JavaScript动态修改主题 */
function toggleTheme() {
  const html = document.documentElement;
  const currentTheme = html.getAttribute('data-theme');
  const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-theme', newTheme);
  localStorage.setItem('theme', newTheme);
}
```

### 5.2 CSS计算函数

```css
/* calc()函数 */
.calculated-width {
  width: calc(100% - 2rem);
  height: calc(100vh - 100px);
  margin: calc(var(--spacing-lg) * -1);
}

/* 与CSS变量结合 */
.responsive-container {
  --container-width: 1200px;
  --container-padding: 2rem;

  width: min(calc(100% - calc(var(--container-padding) * 2)), var(--container-width));
  margin: 0 auto;
  padding: 0 var(--container-padding);
}

/* 复杂计算 */
.complex-layout {
  --sidebar-width: 250px;
  --content-width: calc(100% - var(--sidebar-width));
  --gutter: 1rem;

  display: grid;
  grid-template-columns: var(--sidebar-width) var(--content-width);
  gap: var(--gutter);
}

/* min()函数 */
.responsive-text {
  font-size: min(5vw, 2rem);
}

.responsive-padding {
  padding: min(5vw, 2rem);
}

/* max()函数 */
.min-width-container {
  width: max(50%, 300px);
}

.min-height-section {
  min-height: max(50vh, 400px);
}

/* clamp()函数 */
.clamped-text {
  font-size: clamp(1rem, 2.5vw, 2rem);
}

.clamped-width {
  width: clamp(300px, 80%, 1200px);
}

/* 实际应用案例 */
.typography-scale {
  font-size: clamp(1rem, 2.5vw, 1.25rem);
  line-height: 1.6;
}

.responsive-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 300px), 1fr));
  gap: 2rem;
}

.fluid-container {
  width: min(100% - 4rem, 1200px);
  margin: 0 auto;
  padding: 0 2rem;
}
```

### 5.3 CSS容器查询

```css
/* 容器查询基础 */
.card-container {
  container-type: inline-size;
  container-name: card;
}

@container card (min-width: 400px) {
  .card {
    display: flex;
    flex-direction: row;
  }

  .card-image {
    width: 200px;
    height: 150px;
  }

  .card-content {
    flex: 1;
    padding: 1rem;
  }
}

@container card (min-width: 600px) {
  .card {
    flex-direction: column;
  }

  .card-image {
    width: 100%;
    height: 200px;
  }
}

/* 复杂的容器查询 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.product-card {
  container-type: inline-size;
  container-name: product;
}

@container product (min-width: 300px) {
  .product-card {
    padding: 1.5rem;
  }

  .product-title {
    font-size: 1.25rem;
  }
}

@container product (min-width: 500px) {
  .product-card {
    display: flex;
    gap: 1rem;
  }

  .product-image {
    width: 150px;
    height: 150px;
  }

  .product-details {
    flex: 1;
  }
}

/* 容器查询单位 */
.responsive-component {
  container-type: inline-size;
}

@container (min-width: 300px) {
  .heading {
    font-size: 1.5cqw; /* 容器查询宽度单位 */
  }

  .description {
    font-size: 1cqw;
    line-height: 1.5cqh; /* 容器查询高度单位 */
  }
}
```

### 5.4 CSS层级与级联

```css
/* CSS层级 */
@layer reset, base, components, utilities;

/* 重置样式 */
@layer reset {
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    font-family: system-ui, -apple-system, sans-serif;
    line-height: 1.6;
  }
}

/* 基础样式 */
@layer base {
  body {
    background: var(--body-bg);
    color: var(--body-color);
  }

  h1 {
    font-size: 2rem;
    font-weight: bold;
    margin-bottom: 1rem;
  }

  a {
    color: var(--primary-color);
    text-decoration: none;
  }

  a:hover {
    text-decoration: underline;
  }
}

/* 组件样式 */
@layer components {
  .button {
    display: inline-block;
    padding: 0.75rem 1.5rem;
    background: var(--primary-color);
    color: white;
    border-radius: 0.25rem;
    border: none;
    cursor: pointer;
    transition: all 0.2s;
  }

  .button:hover {
    background: var(--primary-color-dark);
    transform: translateY(-1px);
  }

  .card {
    background: white;
    border-radius: 0.5rem;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    overflow: hidden;
  }

  .card-header {
    padding: 1rem;
    background: var(--light-color);
    border-bottom: 1px solid rgba(0,0,0,0.1);
  }

  .card-body {
    padding: 1rem;
  }
}

/* 工具类 */
@layer utilities {
  .text-center { text-align: center; }
  .text-left { text-align: left; }
  .text-right { text-align: right; }

  .mb-1 { margin-bottom: 0.25rem; }
  .mb-2 { margin-bottom: 0.5rem; }
  .mb-3 { margin-bottom: 1rem; }
  .mb-4 { margin-bottom: 1.5rem; }
  .mb-5 { margin-bottom: 3rem; }

  .p-1 { padding: 0.25rem; }
  .p-2 { padding: 0.5rem; }
  .p-3 { padding: 1rem; }
  .p-4 { padding: 1.5rem; }
  .p-5 { padding: 3rem; }
}
```

## 六、性能优化与最佳实践

### 6.1 CSS性能优化

```css
/* 避免过度嵌套 */
/* 不推荐 */
.container .header .nav .menu .item {
  /* 深度嵌套 */
}

/* 推荐 */
.nav-item {
  /* 扁平化选择器 */
}

/* 使用BEM命名规范 */
.card {
  /* Block */
}

.card__header {
  /* Element */
}

.card--featured {
  /* Modifier */
}

.card__title--large {
  /* Element with Modifier */
}

/* 避免通用选择器 */
/* 不推荐 */
* {
  margin: 0;
  padding: 0;
}

/* 推荐 */
body, h1, h2, h3, p, ul, ol {
  margin: 0;
  padding: 0;
}

/* 避免重复属性 */
.button {
  background: #007bff;
  color: white;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
}

.button-secondary {
  /* 继承共同属性 */
  composes: button;
  background: #6c757d;
}

/* 使用will-change优化动画 */
.animated-element {
  will-change: transform, opacity;
}

.animated-element:hover {
  transform: translateY(-2px);
  opacity: 0.8;
}

/* 使用transform代替位置属性 */
/* 不推荐 */
.moving-element {
  left: 100px;
  top: 100px;
  transition: left 0.3s, top 0.3s;
}

/* 推荐 */
.moving-element {
  transform: translate(100px, 100px);
  transition: transform 0.3s;
}
```

### 6.2 响应式最佳实践

```css
/* 移动优先设计 */
.container {
  width: 100%;
  padding: 1rem;
}

@media (min-width: 768px) {
  .container {
    max-width: 720px;
    margin: 0 auto;
  }
}

@media (min-width: 992px) {
  .container {
    max-width: 960px;
  }
}

@media (min-width: 1200px) {
  .container {
    max-width: 1140px;
  }
}

/* 使用相对单位 */
.responsive-typography {
  font-size: clamp(1rem, 2.5vw, 1.25rem);
  line-height: 1.6;
}

.responsive-spacing {
  padding: clamp(1rem, 5vw, 3rem);
  margin: clamp(1rem, 5vw, 2rem);
}

/* 避免固定宽度 */
.flexible-layout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

/* 使用CSS变量进行响应式设计 */
:root {
  --font-size-base: 16px;
  --spacing-unit: 8px;
}

@media (min-width: 768px) {
  :root {
    --font-size-base: 18px;
    --spacing-unit: 12px;
  }
}

@media (min-width: 1200px) {
  :root {
    --font-size-base: 20px;
    --spacing-unit: 16px;
  }
}
```

### 6.3 可访问性最佳实践

```css
/* 确保足够的对比度 */
.high-contrast {
  color: #333;
  background: #fff;
}

/* 避免仅使用颜色传达信息 */
.error-message {
  color: #dc3545;
  border-left: 4px solid #dc3545;
  padding-left: 1rem;
}

.success-message {
  color: #28a745;
  border-left: 4px solid #28a745;
  padding-left: 1rem;
}

/* 焦点样式 */
:focus {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.focus-visible {
  /* 仅在键盘导航时显示焦点 */
}

:focus:not(:focus-visible) {
  outline: none;
}

/* 减少动画 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .button {
    border: 2px solid currentColor;
    background: transparent;
    color: var(--primary-color);
  }

  .button:hover {
    background: var(--primary-color);
    color: white;
  }
}
```

## 七、总结

现代CSS技术栈为Web开发提供了强大的布局能力。从传统的盒模型到现代的Flexbox和Grid，开发者可以构建更加灵活、响应式的用户界面。

### 关键要点：

1. **Flexbox适合一维布局**：适合组件级别的布局，如导航栏、卡片等
2. **Grid适合二维布局**：适合页面级别的布局，如网格系统、复杂页面结构
3. **两者可以结合使用**：Flexbox用于组件内部，Grid用于页面结构
4. **CSS自定义属性**：提供动态样式和主题切换能力
5. **现代CSS函数**：calc()、min()、max()、clamp()等提供更灵活的计算能力
6. **容器查询**：基于容器尺寸而非视口尺寸的响应式设计

### 最佳实践：

1. **选择合适的布局技术**：根据具体需求选择Flexbox或Grid
2. **移动优先设计**：从小屏幕开始，逐步增强
3. **使用语义化HTML**：确保良好的可访问性
4. **性能优化**：避免过度复杂的CSS选择器
5. **维护性**：使用CSS预处理器和模块化方法

随着CSS技术的不断发展，开发者需要持续学习和实践这些新技术，以构建更加现代化、用户友好的Web应用。记住，**好的CSS不仅要能工作，还要易于维护和扩展**。