<template>
  <div style="padding: 20px;">
    <h2>Element Plus 测试</h2>
    
    <el-button type="primary" @click="handleClick">
      普通按钮测试
    </el-button>
    
    <el-button type="primary" @click="openDialog">
      <el-icon><Plus /></el-icon>
      带图标按钮测试
    </el-button>
    
    <p>点击次数: {{ count }}</p>
    
    <el-dialog v-model="visible" title="测试对话框">
      <p>对话框内容</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'

const count = ref(0)
const visible = ref(false)

const handleClick = () => {
  console.log('按钮被点击了')
  count.value++
}

const openDialog = () => {
  console.log('打开对话框')
  visible.value = true
}
</script>
