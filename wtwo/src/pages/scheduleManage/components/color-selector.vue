<template>
  <div class="color-selector">
    <div 
      v-for="item in colorItems" 
      :key="item.key"
      class="color-item"
      @click="emit('pick', item.key)"
    >
      <!-- 左侧：类型名称 -->
      <div class="item-label">
        {{ transToConfigDescript(item.label) }}
      </div>
      
      <!-- 右侧：颜色选择器 -->
      <el-popover
        placement="bottom"
        :width="280"
        trigger="click"
        :popper-style="{ padding: '12px' }"
        :show-arrow="false"
        :popper-options="{ strategy: 'fixed' }"
        :teleported="true"
      >
        <template #reference>
          <div 
            class="color-picker-trigger"
            :style="{ backgroundColor: getDisplayColor(item.color) || 'transparent' }"
          >
            <el-icon class="dropdown-icon">
              <ArrowDown />
            </el-icon>
          </div>
        </template>
        
        <!-- 颜色选择面板 -->
        <div class="color-picker-panel">
          <div class="color-palette-grid">
            <div 
              v-for="(color, index) in colorPalette" 
              :key="`${item.key}-${index}`"
              class="color-option"
              :class="{ active: item.color === color }"
              :style="{ backgroundColor: getDisplayColor(color) }"
              @click="selectColor(item, color)"
            ></div>
          </div>
        </div>
      </el-popover>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { transToConfigDescript } from '@/utils/filters/filters'

// 定义颜色项接口
interface ColorItem {
  key: string
  label: string
  color: string
}

// 定义组件属性
interface Props {
  items: Array<{
    key: string
    label: string
    color?: string
  }>
  modelValue?: Record<string, string>
  colorType?: 'light' | 'dark' // 颜色类型：是否应用透明度（light为带透明度，dark为不透明）
}

// 定义组件事件
interface Emits {
  (e: 'update:modelValue', value: Record<string, string>): void
  (e: 'change', value: Record<string, string>): void
  (e: 'pick', key: string): void
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
  modelValue: () => ({}),
  colorType: 'dark'
})

const emit = defineEmits<Emits>()

// 统一调色板
const baseColors = [
  '#137DFF', '#73DF3E', '#FF7D00', '#FF615C',
  '#FFC01E', '#F5319D', '#D91AD9', '#00B42A',
  '#FADC19', '#722ED1', '#20CBFF', '#909399',
  '#4E5969', '#1D2129'
]

// 使用统一调色板
const colorPalette = computed(() => baseColors)

// 颜色项数据
const colorItems = ref<ColorItem[]>([])

// 初始化颜色项
const initColorItems = () => {
  colorItems.value = props.items.map(item => ({
    key: item.key,
    label: item.label,
    color: item.color || props.modelValue[item.key] || ''
  }))
}



// 选择颜色
const selectColor = (item: ColorItem, color: string) => {
  // 选中的颜色保持原色不变
  item.color = color
  emitChange()
}

// 添加透明度到颜色
const addOpacity = (color: string, opacity: number) => {
  // 如果是十六进制颜色，转换为rgba
  if (color.startsWith('#')) {
    const hex = color.replace('#', '')
    const r = parseInt(hex.substr(0, 2), 16)
    const g = parseInt(hex.substr(2, 2), 16)
    const b = parseInt(hex.substr(4, 2), 16)
    return `rgba(${r}, ${g}, ${b}, ${opacity})`
  }
  // 如果已经是rgba格式，替换透明度
  if (color.startsWith('rgba')) {
    return color.replace(/,\s*[\d.]+\)$/, `, ${opacity})`)
  }
  // 如果是rgb格式，添加透明度
  if (color.startsWith('rgb')) {
    return color.replace('rgb', 'rgba').replace(')', `, ${opacity})`)
  }
  return color
}

// 获取浮层中显示的颜色（light应用透明度，dark不透明）
const getDisplayColor = (color: string) => {
  if (props.colorType === 'light') {
    return addOpacity(color, 0.1)
  }
  return color
}

// 发出变化事件
const emitChange = () => {
  const value: Record<string, string> = {}
  colorItems.value.forEach(item => {
    if (item.color) {
      value[item.key] = item.color
    }
  })
  emit('update:modelValue', value)
  emit('change', value)
}

// 监听props变化
watch(() => props.items, initColorItems, { immediate: true, deep: true })
watch(() => props.modelValue, () => {
  colorItems.value.forEach(item => {
    if (props.modelValue[item.key]) {
      item.color = props.modelValue[item.key]
    }
  })
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
.color-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 8px 8px 12px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  background: #F7F8FA;
  transition: all 0.2s ease;
  &:hover {
    border-color: #409EFF;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
  }
}

.item-label {
  font-size: 14px;
  color: #606266;
  flex: 1;
}

.color-picker-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  border: 1px solid #DCDFE6;
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  .dropdown-icon {
    color: #fff;
    font-size: 12px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  }
}

.color-picker-panel {
  .color-palette-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
  }
}

.color-option {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
  position: relative;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  
  &.active {
    box-shadow: 0px 0px 0px 2px rgba(24,144,255,0.2);
  }
  
  
}
</style>
