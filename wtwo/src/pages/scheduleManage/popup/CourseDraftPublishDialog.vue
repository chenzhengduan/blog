<template>
  <el-dialog
    v-model="dialogVisible"
    :title="currentTitle"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="currentState !== 'processing'"
    width="500px"
    class="course-draft-publish-dialog"
  >
    <!-- 提示态 -->
    <div v-if="currentState === 'confirm'" class="confirm-content">
      <div class="main-message">
        本次共 <span class="highlight">{{ totalCount }}</span> 节排课，确认排课吗？
      </div>
      
      <!-- 预检查结果 -->
      <div v-if="preCheckEnabled" class="pre-check-results">
        <div class="pre-check-label">预检查结果:</div>
        <div class="result-items">
          <div class="result-item passed">
            <el-icon class="success-icon">
              <svg aria-hidden="true">
                <use xlink:href="#w2-tongguo"></use>
              </svg>
            </el-icon>
            通过({{ passedCount }}节)
          </div>
          <div class="result-item failed">
            <el-icon class="warning-icon">
              <svg aria-hidden="true">
                <use xlink:href="#w2-xianzhi"></use>
              </svg>
            </el-icon>
            不通过({{ failedCount }}节)
          </div>
        </div>
      </div>
    </div>

    <!-- 进行中态 -->
    <div v-if="currentState === 'processing'" class="processing-content">
      <div class="processing-center">
        <div class="loading-wrap" v-loading="true">
        </div>
          <div class="loading-text">排课中</div>
      </div>
    </div>

    <!-- 完成态 -->
    <div v-if="currentState === 'completed'" class="completed-content">
      <div class="success-section">
        <div class="success-icon">
          <img src="https://cdn01.xiaogj.com/uploads/wtwo-pc/002.png" alt="">
        </div>
      </div>
      <div class="completion-message">
        已完成 <span class="color-#2878E8">{{ completedCount }}</span> 节排课
      </div>
      
      <div class="summary-info">
        本次共 {{ totalCount }} 节排课，冲突/限制 {{ failedCount }} 节
      </div>
      
      <!-- 清空表格数据选项 -->

    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <!-- 提示态按钮 -->
        <div v-if="currentState === 'confirm'" class="confirm-buttons flex-between w-100%">
          <div class="left-area">
            <el-checkbox v-model="checkConflict" :disabled="!NewCourse_IngoreCourseConflict">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
          </div>
          <div class="right-area">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="handleConfirm" :loading="false">
              确认排课
            </el-button>
          </div>
        </div>
        
        <!-- 完成态按钮 -->
        <div v-if="currentState === 'completed'" class="completed-buttons flex-between w-100%">
          <div class="left-area">
            <el-switch v-model="clearTableData" active-text="清空表格中的数据" inactive-text="" />
          </div>
          <div class="right-area">
            <el-button type="primary" @click="handleComplete">
              确认
            </el-button>
          </div>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Check, Warning } from '@element-plus/icons-vue'
import { transToConfigDescript } from '@/utils/filters/filters'

// 权限配置
const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict')

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  preCheckEnabled: {
    type: Boolean,
    default: false
  },
  totalCount: {
    type: Number,
    default: 0
  },
  passedCount: {
    type: Number,
    default: 0
  },
  failedCount: {
    type: Number,
    default: 0
  }
})

// Emits
const emit = defineEmits([
  'update:visible',
  'confirm',
  'cancel',
  'complete'
])

// 响应式数据
const dialogVisible = ref(false)
const currentState = ref('confirm') // confirm | processing | completed
const checkConflict = ref(true)
const clearTableData = ref(true)
const progressPercent = ref(0)
const completedCount = ref(0)

// 计算属性
const currentTitle = computed(() => {
  switch (currentState.value) {
    case 'confirm': return '排课提示'
    case 'processing': return '排课中'
    case 'completed': return '排课完成'
    default: return '排课提示'
  }
})

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    // 重置状态
    currentState.value = 'confirm'
    // checkConflict.value = props.preCheckEnabled // 预检查开启时默认勾选
    clearTableData.value = true
    progressPercent.value = 0
    completedCount.value = 0
  }
})

// 监听 dialogVisible 变化
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 方法
const handleCancel = () => {
  emit('cancel')
  dialogVisible.value = false
}

const handleConfirm = () => {
  emit('confirm', {
    checkConflict: checkConflict.value
  })
}

const handleComplete = () => {
  emit('complete', {
    clearTableData: clearTableData.value
  })
  dialogVisible.value = false
}

// 公开方法
const setProcessing = () => {
  currentState.value = 'processing'
}

const setCompleted = (completed, total, failed) => {
  currentState.value = 'completed'
  completedCount.value = completed
  progressPercent.value = 100
}

const updateProgress = (percent, completed) => {
  progressPercent.value = percent
  completedCount.value = completed
}

const resetToConfirm = () => {
  currentState.value = 'confirm'
  progressPercent.value = 0
  completedCount.value = 0
}

// 暴露方法
defineExpose({
  setProcessing,
  setCompleted,
  updateProgress,
  resetToConfirm
})
</script>

<style lang="scss" scoped>
.course-draft-publish-dialog {
  .confirm-content,
  .processing-content,
  .completed-content {
    padding: 20px 0;
  }

  .main-message {
    font-size: 14px;
    margin-bottom: 20px;
    font-weight: 500;
    font-size: 14px;
    color: #303133;
    .highlight {
      color: #2878E8;
      font-weight: bold;
    }
  }

  .pre-check-results {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    .pre-check-label {
      font-size: 14px;
      color: #606266;
    }
    
    .result-items {
      display: flex;
      gap: 20px;
      padding-left: 20px;
      .result-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 14px;
        background: #F7F8FA;
        border-radius: 4px 4px 4px 4px;
        padding: 4px 6px;
        
        &.passed {
          color: #67c23a;
        }
        
        &.failed {
          color: #f56c6c;
        }
        
        .success-icon {
          color: #67c23a;
        }
        
        .warning-icon {
          color: #f56c6c;
        }
      }
    }
  }

  .conflict-check-option {
    margin-bottom: 20px;
    
    .conflict-checkbox {
      font-size: 14px;
    }
  }

  .processing-content {
    text-align: center;
    
    .processing-center {
      height: 180px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
      .loading-wrap {
        width: 100%;
        min-height: 120px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

      }
              
        .loading-text {
          margin-top: 20px;
          font-size: 14px;
          color: #606266;
        }
    }

    .progress-section {
      margin-bottom: 20px;
      
      .progress-text {
        font-size: 14px;
        color: #2878E8;
        font-weight: bold;
      }
    }
    
    .processing-message {
      font-size: 16px;
      color: #67c23a;
      margin-bottom: 15px;
      font-weight: bold;
    }
    
    .summary-info {
      font-size: 14px;
      color: #606266;
    }
  }

  .completed-content {
    text-align: center;
    
    .success-section {
      margin-bottom: 20px;
      
      .success-icon {
        width: 56px;
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 15px;
        img{
            width: 100%;
            height: 100%;
        }
      }
      
      .progress-text {
        font-size: 14px;
        color: #2878E8;
        font-weight: bold;
      }
    }
    
    .completion-message {
      font-size: 14px;
      color: #303133;
      margin-bottom: 15px;
    }
    
    .summary-info {
      font-size: 14px;
      color: #606266;
      margin-bottom: 20px;
    }
    
    .clear-data-option {
      text-align: left;
      
      .clear-data-checkbox {
        font-size: 14px;
      }
    }
  }

  .dialog-footer {
    .confirm-buttons,
    .completed-buttons {
      display: flex;
      gap: 10px;
    }

    .confirm-buttons {
      justify-content: space-between;
      align-items: center;
    }

    .left-area {
      :deep(.el-checkbox__label) {
        color: #606266;
        font-size: 14px;
        font-weight: 400;
      }
    }
  }
}
</style> 