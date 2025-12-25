<template>
  <div>
    <el-select ref="baseSelectRef" v-model="computedValue" :placeholder="placeholder" :clearable="clearable"
      :value-on-clear="''" :disabled="disabled" :style="selectStyle" :multiple="multiple" :collapse-tags="multiple"
      :collapse-tags-tooltip="multiple" :loading="loading" :filterable="false" :remote="remote"
      :remote-method="remoteMethod" :class="{ 'cell-mode': cellMode, customSelect: true }"
      :popper-class="defaultNotshowEmpty ? ' custom-select-dropdown custom-select-dropdown-empty' : 'custom-select-dropdown'"
      autocomplete="off" @change="handleChange" @clear="handleClear" @focus="handleFocus"
      @visible-change="handleVisibleChange" @click="handleControlClick">
      <!-- 搜索头部 -->
      <template #header v-if="showSearch || cellMode">
        <div class="search-container">
          <el-input ref="searchInputRef" v-model="searchKeyword" :placeholder="searchPlaceholder" size="small"
            class="search-input" autocomplete="new-password" name="ignore-autofill" id="ignore-autofill"
            autocorrect="off" autocapitalize="off" spellcheck="false" @input="handleSearch" @clear="handleSearchClear"
            @keydown.stop="handleSearchKeydown" @blur="handleSearchBlur">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
        </div>
      </template>
      <pageAttentionTips v-if="showAttentionTips && attentionTips" class="ml-16px! mb-8px! mt-4px!">{{ attentionTips }}
      </pageAttentionTips>
      <!-- 选项列表 -->
      <el-option v-for="item in displayOptions" :key="getOptionKey(item)" :label="getOptionLabel(item)"
        :value="getOptionValue(item)" :disabled="getOptionDisabled(item)" v-show="item._displayInOptions !== false">
        <slot name="option" :item="item">
          <div class="option-line-1">{{ getOptionLabel(item) }}</div>
          <div v-if="getOptionDesc(item)" class="option-line-2">{{ getOptionDesc(item) }}</div>
        </slot>
      </el-option>

      <!-- 空状态 -->
      <template #empty>
        <slot name="empty">
          <el-empty :image="globalData.emptyPng2" :image-size="80" description="暂无数据" />
        </slot>
      </template>

      <!-- 底部操作 -->
      <template #footer v-if="$slots.footer">
        <slot name="footer" />
      </template>
    </el-select>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted, getCurrentInstance } from 'vue'
import { Search, InfoFilled } from '@element-plus/icons-vue'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
// ==================== Props 定义 ====================
const props = defineProps({
  // 基础属性
  modelValue: {
    type: [String, Number, Array],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  defaultNotshowEmpty: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  selectStyle: {
    type: Object,
    default: () => ({ width: '100%' })
  },
  loading: {
    type: Boolean,
    default: false
  },

  // 多选相关
  multiple: {
    type: Boolean,
    default: false
  },

  // 搜索相关
  showSearch: {
    type: Boolean,
    default: false
  },
  searchPlaceholder: {
    type: String,
    default: '搜索'
  },
  filterable: {
    type: Boolean,
    default: false
  },
  remote: {
    type: Boolean,
    default: false
  },
  remoteMethod: {
    type: Function,
    default: null
  },

  // 数据相关
  options: {
    type: Array,
    default: () => []
  },
  initialData: { // 新增：用于即时回显的初始数据
    type: Object,
    default: null
  },
  valueKey: {
    type: String,
    default: 'value'
  },
  labelKey: {
    type: String,
    default: 'label'
  },
  descKey: {
    type: String,
    default: 'desc'
  },
  disabledKey: {
    type: String,
    default: 'disabled'
  },

  // 自定义函数
  getOptionKey: {
    type: Function,
    default: null
  },
  getOptionValue: {
    type: Function,
    default: null
  },
  getOptionLabel: {
    type: Function,
    default: null
  },
  getOptionDesc: {
    type: Function,
    default: null
  },
  getOptionDisabled: {
    type: Function,
    default: null
  },

  // 空状态
  emptyText: {
    type: String,
    default: '暂无数据'
  },

  // 单元格模式
  cellMode: {
    type: Boolean,
    default: false
  },
  showAttentionTips: {
    type: Boolean,
    default: false
  },
  attentionTips: {
    type: String,
    default: ''
  }
})

// ==================== Emits 定义 ====================
const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'focus',
  'visible-change',
  'scroll-bottom'  // 🆕 滚动到底部事件
])

// ==================== 响应式数据 ====================
const selectedValue = ref('')
const searchKeyword = ref('')
const searchInputRef = ref(null)
const searchDebounceTimer = ref(null)
const baseSelectRef = ref(null)
const dropdownPanelRef = ref(null)  // 🆕 下拉面板 DOM 引用
let scrollListener = null  // 🆕 滚动监听器
let mutationObserver = null  // 🆕 DOM 变化观察器
let scrollDebounceTimer = null  // 🆕 滚动防抖定时器
let isScrollBottomEmitting = false  // 🆕 防止重复触发标记
// ==================== 计算属性 ====================
// 计算当前应该显示的值，优先使用modelValue，其次使用initialData
const computedValue = computed({
  get() {
    if (props.modelValue !== undefined && props.modelValue !== null) {
      // 多选模式下，空数组也是有效值
      if (props.multiple) {
        return Array.isArray(props.modelValue) ? props.modelValue : []
      } else {
        // 单选模式下，如果modelValue不为空，则使用它
        if (props.modelValue !== '') {
          return props.modelValue
        }
        // 如果modelValue为空字符串，则尝试使用initialData
      }
    }

    // 处理initialData
    if (props.initialData) {
      if (props.multiple && Array.isArray(props.initialData)) {
        // 多选模式：从initialData数组中提取ID数组
        return props.initialData.map(item => getOptionValue(item)).filter(Boolean)
      } else if (!props.multiple && typeof props.initialData === 'object') {
        // 单选模式：从单个initialData对象中提取ID
        return getOptionValue(props.initialData) || ''
      }
    }

    // 根据multiple属性返回正确的默认值
    return props.multiple ? [] : ''
  },
  set(value) {
    selectedValue.value = value
  }
})

// 显示的选项列表
const displayOptions = computed(() => {
  const options = props.options || []

  // 🆕 只有在没有搜索关键词时才显示 initialData
  if (props.initialData && !searchKeyword.value) {
    if (props.multiple && Array.isArray(props.initialData)) {
      // 多选模式：处理initialData数组
      const initialDataArray = props.initialData.filter(item => {
        const value = getOptionValue(item)
        return value && !options.some(option => getOptionValue(option) === value)
      })

      if (initialDataArray.length > 0) {
        // 给 initialData 添加标识，用于回显但不显示在下拉选项中
        const markedInitialData = initialDataArray.map(item => ({
          ...item,
          _isInitialData: true, // 标识这是初始数据
          _displayInOptions: false // 控制是否在下拉选项中显示
        }))
        return [...markedInitialData, ...options]
      }
    } else if (!props.multiple && typeof props.initialData === 'object') {
      // 单选模式：处理单个initialData对象
      const value = getOptionValue(props.initialData)
      const label = getOptionLabel(props.initialData)

      if (value) {
        // 检查初始数据是否已存在于选项列表中
        const exists = options.some(option => getOptionValue(option) === value)

        // 如果不存在，则将其添加到列表顶部，但标记为不显示
        if (!exists) {
          // 如果label为空，使用value作为label
          const initialData = {
            ...props.initialData,
            [props.labelKey]: label || value,
            _isInitialData: true, // 标识这是初始数据
            _displayInOptions: false // 控制是否在下拉选项中显示
          }
          return [initialData, ...options]
        }
      }
    }
  }

  return options
})

// ==================== 方法定义 ====================
// 获取选项键值
const getOptionKey = (item) => {
  if (props.getOptionKey) {
    return props.getOptionKey(item)
  }
  return item[props.valueKey] || item.id || item.key
}

// 获取选项值
const getOptionValue = (item) => {
  if (props.getOptionValue) {
    return props.getOptionValue(item)
  }
  return item[props.valueKey] || item.value || item.id
}

// 获取选项标签
const getOptionLabel = (item) => {
  if (props.getOptionLabel) {
    return props.getOptionLabel(item)
  }
  return item[props.labelKey] || item.label || item.name || item.title
}

// 获取选项描述
const getOptionDesc = (item) => {
  if (props.getOptionDesc) {
    return props.getOptionDesc(item)
  }
  return item[props.descKey] || item.description || item.desc
}

// 获取选项禁用状态
const getOptionDisabled = (item) => {
  if (props.getOptionDisabled) {
    return props.getOptionDisabled(item)
  }
  return item[props.disabledKey] || item.disabled || false
}

// 处理选择变化
const handleChange = (value) => {
  // 立即更新selectedValue
  selectedValue.value = value
  emit('update:modelValue', value)
  emit('change', value)
}

// 处理清空
const handleClear = () => {
  searchKeyword.value = ''
  selectedValue.value = props.multiple ? [] : ''
  emit('update:modelValue', props.multiple ? [] : '')
  emit('clear')
}

// 处理搜索
const handleSearch = (value) => {
  // 清除之前的防抖计时器
  if (searchDebounceTimer.value) {
    clearTimeout(searchDebounceTimer.value)
  }

  // 设置新的防抖计时器
  searchDebounceTimer.value = setTimeout(() => {
    emit('search', value, displayOptions.value)
  }, 300) // 300ms 防抖延迟
}

// 处理搜索清空
const handleSearchClear = () => {
  searchKeyword.value = ''
  emit('search', '', displayOptions.value)
}

// 处理搜索框键盘事件
const handleSearchKeydown = (event) => {
  // 在单元格模式下，强制阻止事件冒泡，确保搜索框能正常处理键盘事件
  if (props.cellMode) {
    event.stopPropagation()
  }

  // 确保删除键、退格键等正常工作
  if (event.key === 'Backspace' || event.key === 'Delete') {
    // 让事件正常传播，不阻止默认行为
    return true
  }

  // 处理回车键，关闭下拉框
  if (event.key === 'Enter' || event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault()
    // 可以在这里添加回车键的处理逻辑
  }
}

// 处理搜索框失焦
const handleSearchBlur = () => {
  // 在单元格模式下，失焦时发出特殊事件
  if (props.cellMode) {
    emit('focus', { isSearchFocus: false })
  }
}

// 处理聚焦
const handleFocus = () => {
  // 在单元格模式下，聚焦搜索框时发出特殊事件
  if (props.cellMode) {
    emit('focus', { isSearchFocus: true })
  } else {
    emit('focus')
  }
}

// 🆕 处理滚动到底部（优化版：防抖 + 防重复）
const handleScroll = (event) => {
  // 清除之前的防抖定时器
  if (scrollDebounceTimer) {
    clearTimeout(scrollDebounceTimer)
  }
  
  // 使用防抖，避免频繁触发
  scrollDebounceTimer = setTimeout(() => {
    const target = event.target
    const scrollTop = target.scrollTop
    const scrollHeight = target.scrollHeight
    const clientHeight = target.clientHeight
    const distanceToBottom = scrollHeight - scrollTop - clientHeight
    
    console.log('📜 [BaseSelect] 滚动中:', { 
      scrollTop: Math.round(scrollTop), 
      clientHeight, 
      scrollHeight, 
      距离底部: Math.round(distanceToBottom) 
    })
    
    // 滚动到距离底部 20px 以内时触发（减少阈值提高灵敏度）
    if (distanceToBottom <= 20 && !isScrollBottomEmitting) {
      isScrollBottomEmitting = true
      console.log('🔥 [BaseSelect] 触发 scroll-bottom 事件')
      emit('scroll-bottom')
      
      // 800ms 后重置标记，允许再次触发（优化：缩短冷却时间提高响应速度）
      setTimeout(() => {
        isScrollBottomEmitting = false
        console.log('🔓 [BaseSelect] scroll-bottom 标记已重置')
      }, 800)
    }
  }, 100) // 100ms 防抖延迟
}

// 🆕 添加滚动监听（优化版：多次尝试 + 多种选择器）
const addScrollListener = () => {
  nextTick(() => {
    let attempts = 0
    const maxAttempts = 10  // 增加尝试次数
    
    const tryFindScrollContainer = () => {
      attempts++
      console.log(`🔍 [BaseSelect] 第 ${attempts}/${maxAttempts} 次尝试查找滚动容器...`)
      
      let scrollContainer = null
      
      // 方法1：通过 ref 获取
      if (baseSelectRef.value) {
        const popperRef = baseSelectRef.value.$refs?.popper || baseSelectRef.value.popperRef
        if (popperRef) {
          const popperEl = popperRef.popperRef?.contentRef || popperRef.contentRef || popperRef
          if (popperEl) {
            scrollContainer = popperEl.querySelector('.wtwo-scrollbar__wrap') ||
                            popperEl.querySelector('.wtwo-scrollbar__view') ||
                            popperEl.querySelector('.wtwo-select-dropdown__list') ||
                            popperEl.querySelector('.el-scrollbar__wrap') ||
                            popperEl.querySelector('.el-select-dropdown__wrap')
          }
        }
      }
      
      // 方法2：全局查找（支持 wtwo- 和 el- 前缀）
      if (!scrollContainer) {
        const selectors = [
          '.custom-select-dropdown .wtwo-scrollbar__wrap',
          '.custom-select-dropdown .wtwo-scrollbar__view',
          '.custom-select-dropdown .wtwo-select-dropdown__list',
          '.custom-select-dropdown .el-scrollbar__wrap',
          '.custom-select-dropdown .el-select-dropdown__wrap',
          '.wtwo-select-dropdown .wtwo-scrollbar__wrap',
          '.wtwo-select-dropdown .wtwo-scrollbar__view'
        ]
        
        for (const selector of selectors) {
          scrollContainer = document.querySelector(selector)
          if (scrollContainer) {
            console.log(`✅ [BaseSelect] 通过选择器找到: ${selector}`)
            break
          }
        }
      }
      
      if (scrollContainer) {
        console.log('✅ [BaseSelect] 找到滚动容器:', scrollContainer)
        scrollListener = handleScroll
        scrollContainer.addEventListener('scroll', scrollListener, { passive: true })
        dropdownPanelRef.value = scrollContainer
        console.log('✅ [BaseSelect] 滚动监听已添加')
        return true
      } else if (attempts < maxAttempts) {
        // 继续尝试，逐渐增加延迟
        const delay = attempts < 3 ? 50 : (attempts < 6 ? 100 : 200)
        setTimeout(tryFindScrollContainer, delay)
        return false
      } else {
        console.warn('⚠️ [BaseSelect] 尝试', maxAttempts, '次后仍未找到滚动容器')
        console.warn('💡 [BaseSelect] 将依赖"加载更多"按钮进行分页')
        return false
      }
    }
    
    // 初始延迟 50ms 开始查找
    setTimeout(tryFindScrollContainer, 50)
  })
}

// 🆕 移除滚动监听
const removeScrollListener = () => {
  console.log('🧹 [BaseSelect] 清理滚动监听')
  
  // 清理防抖定时器
  if (scrollDebounceTimer) {
    clearTimeout(scrollDebounceTimer)
    scrollDebounceTimer = null
  }
  
  // 清理滚动监听器
  if (dropdownPanelRef.value && scrollListener) {
    dropdownPanelRef.value.removeEventListener('scroll', scrollListener)
    scrollListener = null
    dropdownPanelRef.value = null
  }
  
  // 清理观察器
  if (mutationObserver) {
    mutationObserver.disconnect()
    mutationObserver = null
  }
  
  // 重置标记
  isScrollBottomEmitting = false
}

// 处理下拉框显示/隐藏
const handleVisibleChange = (visible) => {
  if (visible) {
    // 单元格模式或显示搜索框时，面板打开时自动聚焦搜索框
    if (props.showSearch || props.cellMode) {
      nextTick(() => {
        setTimeout(() => {
          if (searchInputRef.value) {
            searchInputRef.value.focus()
          }
        }, 100)
      })
    }
    
    // 🆕 添加滚动监听
    addScrollListener()
  } else {
    // 🆕 移除滚动监听
    removeScrollListener()
  }
  
  emit('visible-change', visible)
}

// 处理el-select的点击事件
const handleControlClick = () => {
  if (props.cellMode) {
    // 单元格模式下，延迟聚焦到搜索框
    nextTick(() => {
      setTimeout(() => {
        if (searchInputRef.value && !searchInputRef.value.disabled) {
          searchInputRef.value.focus()
        }
      }, 200) // 增加延迟时间，确保面板完全打开
    })
  }
}

// ==================== 监听器 ====================
// 不再需要watch，因为使用计算属性来处理

// ==================== 生命周期 ====================
onUnmounted(() => {
  // 组件卸载时清理滚动监听器
  removeScrollListener()
})

// ==================== 暴露方法 ====================
defineExpose({
  search: handleSearch,
  clearSearch: handleSearchClear,
  blur: () => {
    baseSelectRef.value?.blur()
  },
  focus: () => {
    // 可以在这里添加聚焦逻辑
  }
})
</script>

<style scoped>
/* 单元格模式下的样式优化 */
:deep(.wtwo-select.cell-mode) {
  .wtwo-input__wrapper {
    cursor: pointer;
  }

  .wtwo-input__inner {
    cursor: pointer;
    user-select: none;
    pointer-events: none;
  }

  .wtwo-input__suffix {
    pointer-events: auto;
  }
}

/* 搜索框样式 */
.search-container {
  padding: 8px 8px;
  /* background: #F5F7FA; */
  border-bottom: 1px solid #ebeef5;
  :deep(.wtwo-input__wrapper){
    background: #F5F7FA;
  }
}

.search-input {
  width: 100%;
  height: 32px;
  border-radius: 4px;
}

.search-input :deep(.wtwo-input__inner) {
  pointer-events: auto !important;
  cursor: text !important;
  user-select: text !important;
}

.search-input :deep(.wtwo-input__wrapper) {
  pointer-events: auto !important;
  border-radius: 4px !important;
}

/* 选项样式 */
.option-line-1 {
  font-size: 14px;
  color: #303133;
}

.option-line-2 {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 空状态样式 */
.select-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.select-empty .wtwo-icon {
  margin-right: 8px;
  font-size: 16px;
}
</style>
