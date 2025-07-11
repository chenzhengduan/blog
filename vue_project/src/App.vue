<template>
  <el-config-provider namespace="sub">
  <div>
    <router-view  />
  </div>
</el-config-provider>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

// 获取micro-app名称
const microAppName = ref('');

onMounted(() => {
  // 在组件挂载后获取micro-app名称
  if (window.__MICRO_APP_NAME__) {
    microAppName.value = window.__MICRO_APP_NAME__;
    console.log('当前micro-app名称:', microAppName.value);
  }
});
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
