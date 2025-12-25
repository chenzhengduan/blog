<template>
  <BaseSelect
    v-model="selectedValue"
    :options="filteredCourseList"
    :defaultNotshowEmpty="true"
    :initial-data="props.initialData"
    :placeholder="dynamicPlaceholder"
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
    :select-style="selectStyle"
    :id="id"
    :show-search="!!classId"
    :search-placeholder="transToConfigDescript('搜索课程')"
    :filterable="true"
    :cell-mode="cellMode&&!!classId"
    value-key="ID"
    label-key="Name"
    :get-option-desc="getCourseDesc"
    :empty-text="transToConfigDescript('暂无匹配的课程')"
    @change="handleChange"
    @clear="handleClear"
    @search="handleSearch"
    @visible-change="handleVisibleChange"
  >
    <template #option="{ item }">
      <div class="course-option">
        <el-icon size="20px">
            <svg aria-hidden="true">
              <use xlink:href="#w2-xuanke1"></use>
            </svg>
          </el-icon>
        <span class="course-name">{{ item.Name }}</span>
      </div>
    </template>
    <template #empty>
      <div class="course-empty">
        <el-empty 
          v-show="loading||!classId||!courseList.length" 
          :image="globalData.emptyPng2"
          :image-size="80"
          :description="transToConfigDescript(loading ? '加载中...' : (!classId ? '请先选“上课班级”' : searchKeyword ? '暂无匹配的课程' : '暂无课程'))" 
          />
      </div>
    </template>
  </BaseSelect>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick, getCurrentInstance } from 'vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { useConfigs } from '@/store'
import { getClassWithShiftAndSubject } from '@/api/arrange'
import { transToConfigDescript } from '@/utils/filters/filters'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const configs = computed(() => useConfigs().configs)
// const IsOpen_OneClassMuitShift = computed(() => configs.value.IsOpen_OneClassMuitShift == 1)
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: transToConfigDescript('请选择课程')
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
  id: {
    type: String,
    default: ''
  },
  classId: {
    type: [String, Number],
    default: ''
  },
  showDescription: {
    type: Boolean,
    default: false
  },
  initialData: { // 新增：用于即时回显的初始数据
    type: Object,
    default: null
  },
  cellMode: { // 新增：单元格模式
    type: Boolean,
    default: false
  },
  // 班级信息
  ClassData:{
    type:Object,
    default:null
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'visible-change' // 🆕 暴露 visible-change 事件
])

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const courseList = ref([])

const lastClassId = ref('') // 记录上次的班级ID

// 🚀 优化：懒加载标记
const hasLoadedData = ref(false)
const isMounted = ref(false)

const filteredCourseList = computed(() => {
  if (!searchKeyword.value) {
    return courseList.value
  }
  return courseList.value.filter(courseItem => {
    const keyword = searchKeyword.value.toLowerCase()
    const name = courseItem.Name?.toLowerCase() || ''
    return name.includes(keyword)
  })
})

const dynamicPlaceholder = computed(() => {
  if (!props.classId) {
    return ''
  }
  return props.placeholder
})

const handleChange = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  emit('update:modelValue', value)
  emit('change', value, getSelectedCourse(value))
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
  emit('search', value, filteredCourseList.value)
}

// 🚀 优化：下拉框显示/隐藏回调
const handleVisibleChange = (visible) => {
  // 🆕 先抛出事件给父组件，让父组件的 UI 锁逻辑能够执行
  emit('visible-change', visible)
  
  if (!isMounted.value) return
  
  if (visible && !hasLoadedData.value && props.classId) {
    fetchCourseList()
    hasLoadedData.value = true
  }
}

const getCourseDesc = (item) => {
  return item.GradeName || ''
}

const getSelectedCourse = (value) => {
  return courseList.value.find(courseItem => courseItem.ID === value) || null
}

const fetchCourseList = async () => {
  
  if (!props.classId) {
    courseList.value = []
    return
  }

  // 是否已有当前选中值（包含父传入的 modelValue 或内部已设置的 selectedValue）
  const hasCurrentValue = !!(props.modelValue || selectedValue.value)

  
  getClassWithShiftAndSubject(props.classId).then(res=>{
    if(res.ErrorCode==200&&res.Data){
      courseList.value = res.Data.ShiftList
      // 仅当唯一项且当前无值时自动选中
      if(courseList.value.length===1 && !hasCurrentValue){
        const only = courseList.value[0]
        selectedValue.value = only.ID
        emit('update:modelValue', only.ID)
        emit('change', only.ID, only)
      }
    }
  })
}

watch(() => props.modelValue, (newValue) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  console.log('CourseSelect: modelValue变化', { newValue, courseListLength: courseList.value.length, classId: props.classId })
  
  if (newValue) {
    // 如果courseList还没有加载完成，等待加载完成后再设置
    if (courseList.value.length === 0 && props.classId) {
      console.log('CourseSelect: courseList为空，等待加载...')
      // 等待courseList加载完成，最多等待3秒
      let retryCount = 0
      const maxRetries = 30 // 30 * 100ms = 3秒
      
      const checkCourseList = () => {
        if (courseList.value.length > 0) {
          console.log('CourseSelect: courseList加载完成，设置selectedValue', newValue)
          selectedValue.value = newValue
        } else if (retryCount < maxRetries) {
          retryCount++
          setTimeout(checkCourseList, 100)
        } else {
          // 超时后直接设置，即使没有找到对应的选项
          console.warn('CourseSelect: courseList加载超时，直接设置selectedValue')
          selectedValue.value = newValue
        }
      }
      checkCourseList()
    } else {
      console.log('CourseSelect: 直接设置selectedValue', newValue)
      selectedValue.value = newValue
    }
  } else {
    selectedValue.value = newValue
  }
}, { immediate: true })

watch(() => props.classId, (newClassId, oldClassId) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  // 🚀 优化：班级变化时重置加载标记，不立即请求
  if (newClassId !== oldClassId) {
    hasLoadedData.value = false
    if (!newClassId) {
      courseList.value = []
    }
    
    // 如果当前选中值不在新班级的课程中，清空选择
    if (selectedValue.value && newClassId && courseList.value) {
      const currentCourse = courseList.value.find(courseItem => courseItem.ID === selectedValue.value)
      if (!currentCourse) {
        selectedValue.value = ''
        emit('update:modelValue', '')
      }
    }
  }
}, { immediate: false })



watch(searchKeyword, (newKeyword) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) return
  
  if (props.classId) {
    // 搜索时，如果班级ID没有变化，直接使用现有列表进行过滤
    // 只有在班级ID变化时才重新请求
    if (props.classId !== lastClassId.value) {
      fetchCourseList()
    } else {
      console.log('CourseSelect: 搜索时使用现有课程列表进行过滤')
    }
  }
}, { debounce: 300 })

onMounted(() => {
  isMounted.value = true
  console.log('✅ CourseSelect 组件已挂载')
  // 🚀 优化：不再在组件挂载时请求数据，完全改为懒加载模式
  // 只在下拉框展开时才请求，减少虚拟滚动时的重复请求和性能开销
})

onUnmounted(() => {
  isMounted.value = false
  console.log('❌ CourseSelect 组件销毁')
})
</script>

<style scoped>
.course-option {
  display: flex;
  width: 100%;
  align-items: center;
}
.course-name {
  font-size: 14px;
  color: #606266;
  font-weight: 400;
  margin-left: 8px;
}


.course-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}
.course-empty .wtwo-icon {
  margin-right: 8px;
  font-size: 16px;
}
</style> 