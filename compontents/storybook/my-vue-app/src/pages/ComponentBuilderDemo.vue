<template>
  <div class="component-builder-demo">
    <ComponentBuilder />
  </div>
</template>

<script setup lang="ts">
import ComponentBuilder from '../components/dynamic/ComponentBuilder.vue'
</script>

<style scoped lang="scss">
.component-builder-demo {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}
</style>
