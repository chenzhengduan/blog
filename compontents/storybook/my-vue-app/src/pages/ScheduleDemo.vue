<template>
  <div class="schedule-demo">
    <div class="app-container">
      <div class="header">
        <h1>排课管理系统</h1>
        <p class="subtitle">基于 BaseTable 基础组件构建的业务系统</p>
      </div>

      <!-- 排课表格业务组件 -->
      <div class="table-wrapper">
        <ScheduleTable ref="scheduleTableRef" />
      </div>

      <!-- 数据展示 -->
      <div class="data-display">
        <h3>表格数据：</h3>
        <pre v-if="scheduleTableRef">{{ JSON.stringify(scheduleTableRef.tableData, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ScheduleTable from '../components/business/schedule/ScheduleTable.vue'

// 获取排课表格组件引用
const scheduleTableRef = ref<InstanceType<typeof ScheduleTable> | null>(null)
</script>

<style scoped>
.schedule-demo {
  min-height: 100vh;
}

.app-container {
  padding: 20px;
  max-width: 1800px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
}

.header {
  margin-bottom: 20px;
  text-align: center;
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 28px;
}

.subtitle {
  color: #666;
  margin-bottom: 20px;
  font-size: 14px;
}

.table-wrapper {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.data-display {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.data-display h3 {
  margin-top: 0;
  color: #2c3e50;
  font-size: 16px;
}

.data-display pre {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: #666;
  overflow-x: auto;
}
</style>
