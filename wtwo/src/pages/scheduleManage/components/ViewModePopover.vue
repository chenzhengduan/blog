<template>
  <el-popover
    :placement="placement"
    :width="width"
    :trigger="trigger"
    :hide-after="0"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    v-model:visible="popoverVisible"
  >
    <template #reference>
      <slot name="reference"></slot>
    </template>

    <div class="view-mode-popover">
      <div class="schedule-view-mode-setting-content-title">显示方式</div>
      <div class="view-radio-group-item">
        <el-radio value="byTime" v-model="localViewBy">
          时刻
        </el-radio>
        <div class="view-radio-group-item-desc">显示时间刻度，课节高度直观反映上课时长</div>
      </div>
      <div class="view-radio-group-item">
        <el-radio value="byPeriod" v-model="localViewBy">
          时段
        </el-radio>
        <div class="view-radio-group-item-desc">按示上午、下午、晚上的方式归类显示课节</div>
      </div>
      <div class="view-radio-group-item">
        <el-radio value="byRange" v-model="localViewBy">
          范围
        </el-radio>
        <div class="view-radio-group-item-desc">按设置的时间范围，归类显示课节</div>
        <div v-if="localViewBy=='byRange'" class="setting-range-result-box ml-24px mt-6px flex-between" @click="openSetting">
            <div class="flex-center">
              <el-icon>
                <svg aria-hidden="true">
                  <use xlink:href="#w2-dingshi"></use>
                </svg>
              </el-icon>
              <div class="ml-4px">设置范围</div>
            </div>
            <div class="flex-center">
              <div v-if="rangeCount==0">未设置</div>
              <div v-else>已设置{{ rangeCount }}个</div>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
      </div>
      <div class="flex-end">
        <el-button size="small" @click="handleCancel">取消</el-button>
        <el-button type="primary" size="small" @click="handleSave">保存</el-button>
      </div>
    </div>
    
  </el-popover>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import useEvent from '@/config/event'
import { useUserSettings } from '@/store'

// 获取事件对象
const event = useEvent()

// Props
const props = defineProps({
  // 改为click触发方式
  trigger: {
    type: String as any,
    default: 'click'
  },
  placement: {
    type: String as any,
    default: 'bottom'
  },
  width: {
    type: Number,
    default: 290
  },
  viewBy: {
    type: String,
    required: true
  },
  timetableType: {
    type: String,
    required: true
  },
  objectName: {
    type: String,
    required: true
  },
  visible: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits({
  'save': (viewBy: string) => true,
  'cancel': () => true,
  'update:visible': (visible: boolean) => true
})

// Local state
// 处理上次访问是"仅对象"模式的情况，自动替换为"时段视图"
const initialViewBy = props.viewBy === 'onlyObject' ? 'byPeriod' : props.viewBy
const localViewBy = ref(initialViewBy)
const popoverVisible = ref(props.visible)
const rangeCount = ref(0) // 存储已设置的时间范围数量
const loading = ref(false)

// 用户设置store
const userSettingsStore = useUserSettings()

// Watchers
watch(() => props.viewBy, (newVal) => {
  // 处理传入的viewBy是"仅对象"模式的情况，自动替换为"时段视图"
  localViewBy.value = newVal === 'onlyObject' ? 'byPeriod' : newVal
})

// 监听props.visible变化
watch(() => props.visible, (newVal) => {
  popoverVisible.value = newVal
})

// 监听popoverVisible变化，当变为false时执行取消逻辑
watch(popoverVisible, (newVal, oldVal) => {
  // 只有当从true变为false时才执行取消逻辑
  if (oldVal && !newVal) {
    handleCancel()
  }
  // 触发update:visible事件实现双向绑定
  emit('update:visible', newVal)
})

// Methods
// 获取已设置的时间范围数量
const fetchRangeCount = async () => {
  loading.value = true
  try {
    const settings = await userSettingsStore.fetchUserSettings('TeacherTimeRange', '老师课表时段范围', 1, 1)
    if (settings && settings.length > 0) {
      rangeCount.value = settings[0].UserSettingsDetailList?.length || 0
    } else {
      rangeCount.value = 0
    }
  } catch (error) {
    console.error('获取时间范围设置数量失败:', error)
    rangeCount.value = 0
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数量
onMounted(() => {
  fetchRangeCount()
})

// 监听popoverVisible变化，当显示时重新获取数量
watch(popoverVisible, (newVal) => {
  if (newVal) {
    fetchRangeCount()
  }
})

const handleCancel = () => {
  localViewBy.value = props.viewBy
  emit('cancel')
  popoverVisible.value = false
}

const handleSave = () => {
  if(localViewBy.value=='byRange'&&rangeCount.value==0){
    ElMessage.warning('未设置时间范围')
    return
  }
  // 处理props.viewBy的onlyObject情况，确保比较的是实际显示的视图模式
  const currentPropsViewBy = props.viewBy === 'onlyObject' ? 'byPeriod' : props.viewBy;
  
  // 只有当localViewBy与当前props.viewBy（处理后的）不同时，才触发保存
  if (localViewBy.value !== currentPropsViewBy) {
    emit('save', localViewBy.value)
  }
  
  // 无论是否保存，都关闭弹窗
  popoverVisible.value = false
}

function openSetting(){
  // 触发打开时段范围设置弹窗事件
  event.emit('request-open-period-range-setting', {})
}
</script>

<style scoped>
.view-mode-popover{
  .wtwo-radio{
    height: 26px;
  }
}
.schedule-view-mode-setting-content-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.view-radio-group-item {
  margin-bottom: 12px;
}

.view-radio-group-item-desc {
  font-size: 12px;
  color: #909399;
  margin-left: 24px;
}
.setting-range-result-box{
  border-radius:3px ;
  background: rgba(0,0,0,0.06);
  padding: 4px 8px;
  font-size: 13px;
  cursor: pointer;
}
</style>