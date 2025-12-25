<template>
  <el-dialog 
    v-model="dialogVisible" 
    title="预检查冲突与限制" 
    width="400px" 
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :destroy-on-close="true"
    @close="close"
  >
    <div class="pre-check-dialog-content">
      <div class="pre-check-message">
        <el-icon class="info-icon"><InfoFilled /></el-icon>
        <span class="message-text">预检查排课比较消耗性能，建议排课数据在"已经整理完成"后，再开启预检查。</span>
      </div>
      <div class="pre-check-checkbox">
        <el-checkbox v-model="preCheckConfirmed" @change="handlePreCheckConfirmChange">
          确认已经整理完成
        </el-checkbox>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelPreCheck">取消</el-button>
        <el-button type="primary" :disabled="!preCheckConfirmed" @click="startPreCheck">
          开始检查
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { ElLoading } from 'element-plus'
import { CheckCourseDraft } from '@/api/arrange'
import Logger from '@/utils/sentry/sentry'
interface IPreCheckParams {
  // 可以添加需要的参数
  title?: string
  message?: string
  IDList?: string[] // 添加 IDList 参数
}

const dialogVisible = ref(false)
const preCheckConfirmed = ref(false)
const currentParams = ref<IPreCheckParams>({}) // 保存当前参数

let _resolve: any = null
let _reject: any = null

/**
 * 对外暴露的 open 方法
 * @param params 弹框参数
 * @returns Promise
 */
function open(params?: IPreCheckParams) {
  // 重置状态
  preCheckConfirmed.value = false
  currentParams.value = params || {} // 保存参数
  dialogVisible.value = true
  
  return new Promise((resolve, reject) => {
    _resolve = resolve
    _reject = reject
  })
}

/**
 * 关闭弹框
 */
function close() {
  dialogVisible.value = false
  _reject && _reject()
}

/**
 * 取消预检查
 */
function cancelPreCheck() {
  close()
}

/**
 * 处理预检查确认状态变化
 */
function handlePreCheckConfirmChange(value: any) {
  console.log('预检查确认状态变化:', value)
}

/**
 * 开始预检查
 */
function startPreCheck() {
  if (!preCheckConfirmed.value) {
    return
  }

  // 开启全局 Loading
  const loading = ElLoading.service({
    lock: true,
    text: '正在检查...',
    background: 'rgba(0, 0, 0, 0.3)'
  })

  // 使用传入的 IDList 参数
  const idList = currentParams.value.IDList || []
  
  CheckCourseDraft({ IDList: idList }).then(res => {
    if (res.ErrorCode == 200 && res.Data) {
      // 预检查成功
      console.log('预检查完成', res.Data)
      
      // 检查完成后，resolve Promise 并关闭弹框
      _resolve && _resolve({
        success: true,
        message: '预检查完成',
        data: res.Data
      })
      dialogVisible.value = false
    } else {
      // 预检查失败
      console.error('预检查失败:', res.ErrorMsg)
      _resolve && _resolve({
        success: false,
        message: res.ErrorMsg || '预检查失败'
      })
      dialogVisible.value = false
    }
  }).catch(error => {
    console.error('预检查异常:', error)
    // 在预检查失败时
    Logger.error('预检查失败', {
        error: error,
        checkedIds: currentParams.value.IDList,
        dataCount: currentParams.value.IDList?.length
    })
    _resolve && _resolve({
      success: false,
      message: '预检查过程中发生异常'
    })
    dialogVisible.value = false
  }).finally(() => {
    // 关闭全局 Loading
    loading.close()
  })
}

defineExpose({
  open
})
</script>

<style scoped lang="scss">
.pre-check-dialog-content {
    padding-top: 12px;
    padding-bottom: 12px;
  .pre-check-message {
    margin-bottom: 20px;
    color: #606266;
    line-height: 1.6;
    font-weight: 400;
    line-height: 21px;
    display: flex;
    align-items: flex-start;
    gap: 6px;
    
    .info-icon {
      color: #2878E8;
      font-size: 16px;
      margin-top: 2px;
      flex-shrink: 0;
    }
    
    .message-text {
      flex: 1;
      line-height: 21px;
    }
  }
  
  .pre-check-checkbox {
    display: flex;
    align-items: center;
    padding-left: 22px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>