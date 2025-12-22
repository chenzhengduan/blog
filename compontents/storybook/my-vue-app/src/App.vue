<script setup lang="ts">
</script>

<template>
  <div class="app">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="navbar-content">
        <h1 class="logo">组件演示系统</h1>
        <nav class="nav-menu">
          <router-link to="/schedule" class="nav-link">
            排课管理
          </router-link>
          <router-link to="/component-builder" class="nav-link">
            动态组件配置器
          </router-link>
        </nav>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  background: #f5f5f5;
}

.navbar {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-content {
  max-width: 1800px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.nav-menu {
  display: flex;
  gap: 24px;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-link:hover {
  color: #409eff;
  background: #f0f7ff;
}

.nav-link.router-link-active {
  color: #409eff;
  background: #f0f7ff;
  font-weight: 500;
}

.main-content {
  min-height: calc(100vh - 64px);
}
</style>
