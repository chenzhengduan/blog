<template>
  <el-popover
    :placement="placement"
    :width="width"
    :trigger="trigger"
    :hide-after="0"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    v-model:visible="popoverVisible"
    popper-class="weekday-select-popover-popper"
  >
    <template #reference>
      <slot name="reference"></slot>
    </template>

    <div class="weekday-select-popover">
      <div class="weekday-select-content">
        <el-checkbox-group v-model="localSelectedWeekdays">
          <div 
            v-for="day in weekdays" 
            :key="day.value" 
            class="weekday-checkbox-item"
          >
            <el-checkbox :value="day.value">
              {{ day.label }}
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <div class="flex-end px-5px!">
        <el-button size="small" @click="handleCancel">取消</el-button>
        <el-button type="primary" size="small" @click="handleSave">保存</el-button>
      </div>
    </div>
    
  </el-popover>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

// 星期选项
const weekdays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

// Props
const props = defineProps({
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
    default: 120
  },
  modelValue: {
    type: Array as () => number[],
    default: () => [1, 2, 3, 4, 5, 6, 7] // 默认全选
  },
  visible: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits({
  'save': (weekdays: number[]) => true,
  'cancel': () => true,
  'update:visible': (visible: boolean) => true,
  'update:modelValue': (weekdays: number[]) => true
})

// Local state
const localSelectedWeekdays = ref<number[]>([...props.modelValue])
const popoverVisible = ref(props.visible)

// Watchers
watch(() => props.modelValue, (newVal) => {
  localSelectedWeekdays.value = [...newVal]
}, { deep: true })

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
const handleCancel = () => {
  localSelectedWeekdays.value = [...props.modelValue]
  emit('cancel')
  popoverVisible.value = false
}

const handleSave = () => {
  if (localSelectedWeekdays.value.length === 0) {
    ElMessage.warning('请至少选择一个日期')
    return
  }
  
  emit('save', localSelectedWeekdays.value)
  emit('update:modelValue', localSelectedWeekdays.value)
  popoverVisible.value = false
}
</script>

<style scoped lang="scss">
.weekday-select-popover {
  padding: 4px 0 8px;
}

.weekday-select-content {
  padding: 0 16px;
}

.weekday-checkbox-item {
  
  &:last-child {
    margin-bottom: 0;
  }
}

.flex-end {
  display: flex;
  justify-content: flex-end;
  padding: 8px 4px 0;
}
</style>
<style lang="scss">
.weekday-select-popover-popper{
    padding: 0!important;
    min-width: 120px!important;
}
</style>
