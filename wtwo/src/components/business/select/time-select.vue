<template>
  <BaseSelect
    :id="id"
    ref="selectRef"
    v-model="selectedValue"
    :options="optionsValues"
    :initial-data="initialDataComputed"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :select-style="selectStyle"
    :show-search="!isCustomView&&!!campusId"
    :search-placeholder="'搜索常用时间'"
    :filterable="true"
    :loading="loading"
    :cell-mode="!isCustomView&&cellMode&&!!campusId"
    value-key="value"
    label-key="label"
    :get-option-desc="getTimeDesc"
    empty-text="暂无匹配的时间段"
    @change="handleChange"
    @clear="handleClear"
    @search="handleSearch"
    @visible-change="handleVisibleChange"
  >
    <!-- 自定义头部 -->
    <template #header>
      <div class="custom-time-header">
        <el-button text bg @click.stop="isCustomView = false" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <span class="custom-time-title">自定义时间</span>
      </div>
    </template>

    <!-- 主体内容：常用时间列表 -->
    <template v-if="!isCustomView" #option="{ item }">
      <div class="time-option">
        <span class="time-name" :title="item.Name">{{ item.Name }}</span>
        <span class="time-range">{{ item.startTime }}~{{ item.endTime }}</span>
        <span v-if="item.duration" class="time-duration">{{ item.duration }}分钟</span>
      </div>
    </template>

    <!-- 空状态提示 -->
    <template #empty>
      <div v-if="!isCustomView" class="time-empty">
        <el-empty 
        :image="globalData.emptyPng2"
        :image-size="80"
        style="padding: 0px 0px;"
        :description="loading ? '加载中...' :!campusId?transToConfigDescript('请先选“上课校区”') : searchKeyword ? '暂无匹配的时间段' : '暂未设置常用时间'" 
        />
        <div v-if="campusId&&!hasCommonTimeData && !loading" class="time-empty-tip">
          请前往 <span class="time-empty-tip-link">"管理后台-字段管理"</span> 设置常用时间
          <br>
          <el-button type="primary" link size="small" @click="goToSettings">
            去设置
          </el-button>
        </div>
      </div>
      
      <!-- 自定义时间选择器 -->
      <div v-else class="custom-time-picker-area" @click.stop>
        <!-- 返回箭头 -->
        <div class="custom-time-back-header">
          <!-- <div @click.stop="isCustomView = false" class="back-button">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </div> -->
          <span class="custom-time-title">自定义时间</span>
        </div>
        
        <div class="time-picker-row">
          <span class="time-label">开始时间：</span>
          <el-time-picker
            v-model="customStartTime"
            placeholder="选择开始时间"
            format="HH:mm"
            value-format="HH:mm"
            :clearable="false"
            :teleported="false"
          />
        </div>
        <div class="time-picker-row">
          <span class="time-label">结束时间：</span>
          <el-time-picker
            v-model="customEndTime"
            placeholder="选择结束时间"
            format="HH:mm"
            value-format="HH:mm"
            :clearable="false"
            :teleported="false"
          />
        </div>
        <div v-if="timeError" class="time-error">
          {{ timeError }}
        </div>
      </div>
    </template>

    <!-- 下拉框底部 -->
    <template #footer  v-if="campusId">
      <div v-if="!isCustomView" class="select-footer">
        <el-button 
          type="primary" 
          text
          size="small" 
          class="w100%"
          @click.stop="switchToCustomView"
        >
        <el-icon size="16px">
              <svg aria-hidden="true">
                <use xlink:href="#w2-shijian"></use>
              </svg>
            </el-icon>
          <span class="text-14px ml-8px!">自定义时间</span>
        </el-button>
      </div>
      <div v-else class="custom-time-footer-confirm">
        <el-button 
          size="small" 
          @click="cancelCustomTime" 
        >
          取消
        </el-button>
        <el-button 
          type="primary" 
          size="small" 
          @click="confirmCustomTime" 
          :disabled="!canConfirmCustomTime"
        >
          确定
        </el-button>
      </div>
    </template>
  </BaseSelect>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick, getCurrentInstance } from 'vue'
import { Search, InfoFilled, ArrowLeft } from '@element-plus/icons-vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  campusId: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择时间'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  selectStyle: {
    type: Object,
    default: () => ({ width: '100%' })
  },
  apiConfig: {
    type: Object,
    default: () => ({})
  },
  autoOpen: {
    type: Boolean,
    default: false
  },
  id: {
    type: String,
    default: null
  },
  initialData: { // 新增：用于即时回显的初始数据
    type: Object,
    default: null
  },
  cellMode: { // 新增：单元格模式
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'goToSettings',
  'visible-change' // 🆕 暴露 visible-change 事件
])

const selectedValue = ref('')
const searchKeyword = ref('')
const isCustomView = ref(false) // 控制是否显示自定义时间视图
const customStartTime = ref('')
const customEndTime = ref('')
const timeError = ref('')

// 内部管理的常用时间段数据
const timeList = ref([])
const loading = ref(false)
const lastCampusId = ref('')

// 选择器引用
const selectRef = ref(null)

// 🚀 优化：懒加载标记
const hasLoadedData = ref(false)
const isMounted = ref(false)
const optionsValues = computed(() => {
  return isCustomView.value ? [] : filteredTimeList.value
})
// 处理 initialData，用于即时回显
const initialDataComputed = computed(() => {
  // 如果 modelValue 是时间范围格式（如 "09:00~10:00"），转换为 BaseSelect 需要的格式
  if(isCustomView.value){
    return {}
  }
  if (props.modelValue && props.modelValue.includes('~')) {
    return {
      value: props.modelValue,
      label: props.modelValue
    }
  }
  return props.initialData
})

const filteredTimeList = computed(() => {
  if (!searchKeyword.value) return timeList.value
  
  return timeList.value.filter(timeSlot => {
    const keyword = searchKeyword.value.toLowerCase()
    const timeRange = timeSlot.label?.toLowerCase() || ''
    const startTime = timeSlot.startTime?.toLowerCase() || ''
    const endTime = timeSlot.endTime?.toLowerCase() || ''
    const duration = timeSlot.duration ? `${timeSlot.duration}分钟` : ''
    const name = timeSlot.Name?.toLowerCase() || ''
    // 搜索时间范围、开始时间、结束时间、时长
    return timeRange.includes(keyword) || 
           startTime.includes(keyword) || 
           endTime.includes(keyword) || 
           duration.includes(keyword)||
           name.includes(keyword)
  })
})

const hasCommonTimeData = computed(() => {
  return timeList.value && timeList.value.length > 0
})

const canConfirmCustomTime = computed(() => {
  return customStartTime.value && customEndTime.value && !timeError.value
})

// 获取课程时间数据
const fetchCourseTimeList = async () => {
  if (!props.campusId) {
    timeList.value = []
    return
  }
  
  // 如果校区ID没有变化，且已有数据，则不重复请求
  if (props.campusId === lastCampusId.value && timeList.value.length > 0) {
    return
  }
  
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('campusId', props.campusId)
    
    const response = await fetch('/api/Course/QueryCourseTime', {
      method: 'POST',
      body: formData
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    
    if (result.ErrorCode === 200 && result.Data) {
      // 转换数据格式
      timeList.value = result.Data.map(item => ({
        id: item.ID,
        label: item.StartTime + '~' + item.EndTime,
        value: item.StartTime + '~' + item.EndTime,
        startTime: item.StartTime,
        endTime: item.EndTime,
        duration: calculateDuration(item.StartTime, item.EndTime),
        Name: item.Name
      }))
      lastCampusId.value = props.campusId
    } else {
      console.error('获取课程时间数据失败:', result.ErrorMsg)
      timeList.value = []
    }
  } catch (error) {
    console.error('获取课程时间数据出错:', error)
    timeList.value = []
  } finally {
    loading.value = false
  }
}

// 计算时长
const calculateDuration = (start, end) => {
  if (!start || !end) return null
  const startTime = new Date(`2000-01-01 ${start}`)
  const endTime = new Date(`2000-01-01 ${end}`)
  if (endTime <= startTime) return null
  return (endTime - startTime) / (1000 * 60)
}

const handleChange = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  emit('update:modelValue', value)
  emit('change', value, getSelectedTimeSlot(value))
}

const handleClear = () => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  emit('update:modelValue', '')
  emit('clear')
}

const handleSearch = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  searchKeyword.value = value
  emit('search', value, filteredTimeList.value)
}

const handleSearchClear = () => {
  searchKeyword.value = ''
  emit('search', '', timeList.value)
}

const getTimeDesc = (item) => {
  return item.duration ? `${item.duration}分钟` : ''
}

const getSelectedTimeSlot = (value) => {
  return timeList.value.find(timeSlot => timeSlot.value === value) || null
}

const goToSettings = () => {
  window.open('/back_end/#/fieldManage?menu1=fieldSetting&menu2=setting_teach&menu3=setting_teach_lesson&searchKey=常用时间段', '_blank')
  emit('goToSettings')
}

// 确认自定义时间
const confirmCustomTime = () => {
  if (!canConfirmCustomTime.value) return
  
  const customValue = `${customStartTime.value}~${customEndTime.value}`
  handleChange(customValue)
  // 关闭下拉面板
  selectRef.value?.blur()
}
const cancelCustomTime = () => {
  isCustomView.value = false
  searchKeyword.value = ''
  customStartTime.value = ''
  customEndTime.value = ''
  selectRef.value?.blur()
}

// 验证时间
const validateTime = () => {
  if (!customStartTime.value || !customEndTime.value) {
    timeError.value = ''
    return
  }
  
  const start = new Date(`2000-01-01 ${customStartTime.value}`)
  const end = new Date(`2000-01-01 ${customEndTime.value}`)
  
  if (start >= end) {
    timeError.value = '结束时间必须晚于开始时间'
  } else {
    timeError.value = ''
  }
}

// 解析当前时间值到自定义时间选择器
const parseCurrentTimeToCustom = () => {
  if (props.modelValue && props.modelValue.includes('~')) {
    const [start, end] = props.modelValue.split('~')
    if (start && end) {
      customStartTime.value = start.trim()
      customEndTime.value = end.trim()
    }
  }
}

// 切换到自定义时间视图
const switchToCustomView = () => {
  parseCurrentTimeToCustom()
  isCustomView.value = true
}

// 处理下拉框显示/隐藏
const handleVisibleChange = (visible) => {
  emit('visible-change', visible) // 🆕 先抛出事件给父组件
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  if (!visible) {
    isCustomView.value = false
    searchKeyword.value = ''
    customStartTime.value = ''
    customEndTime.value = ''
    timeError.value = ''
  } else if (visible && isCustomView.value) {
    // 当切换到自定义时间视图时，解析当前时间值并回显
    parseCurrentTimeToCustom()
  } else if (visible && !hasLoadedData.value && props.campusId) {
    // 🚀 优化：下拉框打开时才加载数据（懒加载）
    fetchCourseTimeList()
    hasLoadedData.value = true
  }
}

watch(() => props.modelValue, (newValue) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  selectedValue.value = newValue
}, { immediate: true })

watch(() => props.campusId, (newCampusId, oldCampusId) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  // 🚀 优化：校区变化时重置加载标记，不立即请求
  if (newCampusId !== oldCampusId) {
    hasLoadedData.value = false
    if (!newCampusId) {
      timeList.value = []
    }
  }
}, { immediate: false })

watch([customStartTime, customEndTime], () => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  validateTime()
})

onMounted(() => {
  isMounted.value = true
  console.log('✅ TimeSelect 组件已挂载')
  // 🚀 优化：不再在组件挂载时请求数据，完全改为懒加载模式
  // 只在下拉框展开时才请求，减少虚拟滚动时的重复请求和性能开销
  
  if (props.autoOpen) {
    nextTick(() => {
      if (selectRef.value) {
        selectRef.value.focus()
      }
    })
  }
})

onUnmounted(() => {
  isMounted.value = false
  console.log('❌ TimeSelect 组件销毁')
})

defineExpose({
  search: handleSearch,
  clearSearch: handleSearchClear,
  openMenu: () => {
    if (selectRef.value) {
      const selectElement = selectRef.value.$el
      if (selectElement) {
        selectElement.click()
      }
    }
  }
})
</script>

<style scoped>
/* 自定义时间视图-头部 */
.custom-time-header {
  display: flex;
  align-items: center;
  padding: 5px 12px;
  border-bottom: 1px solid #ebeef5;
}
.back-button {
  margin-right: 8px;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
}
.custom-time-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 时间选项 */
.time-option {
  display: flex;
  align-items: center;
  width: 100%;
}
.time-name{
  font-size: 14px;
  font-weight: 500;
  width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.time-range {
  font-weight: 500;
  margin-left: 20px;
  width: 84px;
}
.time-duration {
  font-size: 12px;
  color: #909399;
  margin-left: 20px;
}

/* 自定义时间选择区域 */
.custom-time-picker-area {
  padding-left: 12px;
  padding-right: 12px;
}

/* 自定义时间返回头部 */
.custom-time-back-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f4f4f4;
  margin-bottom: 16px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  padding-bottom: 12px;
}
.time-picker-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.time-picker-row:last-child {
  margin-bottom: 0;
}
.time-label {
  font-weight: 500;
  color: #303133;
  flex-shrink: 0;
  font-size: 14px;
}
.time-error {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 8px;
  text-align: center;
}

/* 空状态 */
.time-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #909399;
}
.time-empty .wtwo-icon {
  font-size: 24px;
  margin-bottom: 8px;
}
.time-empty-tip {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  line-height: 1.4;
  .time-empty-tip-link{
    color: #303133;
    cursor: pointer;
  }
}
.custom-time-footer-confirm{
  display: flex;
  justify-content: flex-end;
}
</style>