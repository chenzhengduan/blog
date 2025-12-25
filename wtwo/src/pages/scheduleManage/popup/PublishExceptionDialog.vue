<template>
  <el-dialog
    v-model="dialogVisible"
    title="检查提示"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    width="600px"
    class="publish-exception-dialog"
  >
    <div class="exception-content">
      <!-- 被限制部分 -->
      <div v-if="restrictionCount > 0" class="restriction-section">
        <div class="section-header">
          <svg class="warning-icon" aria-hidden="true">
            <use xlink:href="#w2-xianzhi"></use>
          </svg>
          <span class="section-title">被限制: <span class="color-#F53F3F">{{ restrictionCount }}</span> 节排课</span>
        </div>
        <div class="section-content">
          <div 
            v-for="(item, index) in restrictionDetails" 
            :key="`restriction-${index}`"
            class="detail-item"
          >
            {{ item }}
          </div>
        </div>
      </div>

      <!-- 有冲突部分 -->
      <div v-if="conflictCount > 0" class="conflict-section">
        <div class="section-header">
          <svg class="conflict-icon" aria-hidden="true">
            <use xlink:href="#w2-chongtu"></use>
          </svg>
          <span class="section-title">有冲突: {{ conflictCount }}节排课</span>
        </div>
        <div class="section-content">
          <div 
            v-for="(item, index) in conflictDetails" 
            :key="`conflict-${index}`"
            class="detail-item"
          >
            {{ item }}
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleContinuePartial">
          仅排通过检查的排课
        </el-button>
        <el-button type="primary" @click="handleReturnModify">
          暂不排课，返回修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  restrictionCount: {
    type: Number,
    default: 0
  },
  restrictionDetails: {
    type: Array,
    default: () => []
  },
  conflictCount: {
    type: Number,
    default: 0
  },
  conflictDetails: {
    type: Array,
    default: () => []
  }
})
// Emits
const emit = defineEmits([
  'update:visible',
  'continue-partial',
  'return-modify'
])

// 响应式数据
const dialogVisible = ref(false)

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

// 监听 dialogVisible 变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 方法
const handleContinuePartial = () => {
  emit('continue-partial')
  dialogVisible.value = false
}

const handleReturnModify = () => {
  emit('return-modify')
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
.publish-exception-dialog {
  .exception-content {
    padding: 20px 0;
  }

  .restriction-section,
  .conflict-section {
    margin-bottom: 25px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 15px;
    
    .warning-icon {
      color: #f56c6c;
      width: 14px;
      height: 14px;
    }
    
    .conflict-icon {
      color: #e6a23c;
      width: 14px;
      height: 14px;
    }
    
    .section-title {
      font-size: 16px;
      
      .restriction-section & {
        color: #f56c6c;
      }
      
      .conflict-section & {
        color: #e6a23c;
      }
    }
  }

  .section-content {
    .detail-item {
      background-color: #f7f8fa;
      padding: 6px 16px;
      margin-bottom: 8px; 
      border-radius: 4px;
      font-size: 14px;
      color: #606266;
      line-height: 1.5;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
  }
}
</style> 