<script setup>
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import CourseSchedule from './components/CourseSchedule.vue'
</script>

<template>
  <div class="app-container">
    <div class="app-header">
      <h1>课程表</h1>
      <p>基于 Vue 3 的响应式课程表组件</p>
    </div>
    <div class="schedule-wrapper">
      <CourseSchedule />
    </div>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
  color: #303133;
}

.app-container {
  width: 100%;
  min-height: 100vh;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.app-header {
  text-align: center;
  margin-bottom: 20px;
}

.app-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.app-header p {
  font-size: 14px;
  color: #909399;
}

.schedule-wrapper {
  width: 100%;
}
</style>
