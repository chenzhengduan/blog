<template>
  <BaseSelect v-model="selectedValue" :options="filteredSubjectList" :defaultNotshowEmpty="true"
    :initial-data="props.initialData" :placeholder="transToConfigDescript(dynamicPlaceholder)" :clearable="clearable" :disabled="disabled"
    :select-style="selectStyle" :show-search="EnableSubject == 1" :search-placeholder="'搜索科目'" :filterable="true"
    :loading="loading" :cell-mode="cellMode && EnableSubject == 1" value-key="ID" label-key="Name"
    :get-option-desc="getSubjectDesc" empty-text="暂无匹配的科目" @change="handleChange" @clear="handleClear"
    @search="handleSearch">
    <template #option="{ item }">
      <div class="subject-option">
        <span class="subject-name">{{ item.Name }}</span>
      </div>
    </template>
    <template #empty>
      <div class="subject-empty">
        <el-empty v-show="shouldShowEmpty" :image="globalData.emptyPng2" :image-size="80" :description="transToConfigDescript(emptyText)" />
      </div>
    </template>
  </BaseSelect>
</template>

<script setup>
import { ref, computed, watch, onMounted, getCurrentInstance } from 'vue'
import { useDictFieldsStore } from '@/store/dict'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { isUUID } from '@/utils/uuid/uuid'
import { getClassWithShiftAndSubject } from '@/api/arrange'
import { transToConfigDescript } from '@/utils/filters/filters'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
// Store
const dictFieldsStore = useDictFieldsStore()

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择科目'
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
  classData: {
    type: Object,
    default: null
  },
  EnableSubject: {
    type: String,
    default: null
  },
  courseIsClassSubject: {
    type: Boolean,
    default: false
  },
  selectedTableType: {
    type: Number,
    default: 0
  },
  ShiftID: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search'
])

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
// 用于存储从 API 获取的科目列表
const apiSubjectList = ref([])
// 用于防止重复调用 API
const lastClassId = ref('')
const isFetching = ref(false)
// 科目列表数据
const subjectList = ref([])

const isClassSchedule = computed(() => props.selectedTableType == 10)

const shouldShowEmpty = computed(() => {
  return true // 总是由 emptyText 的内容来决定是否显示
})

const emptyText = computed(() => {
  if (loading.value) return '加载中...'
  if (searchKeyword.value) return '暂无匹配的科目'
  // 根据不同排课类型，判断前置条件
  if (isClassSchedule.value) {
    if (!props.classId) return '请先选“上课班级”'
    // 接口加载完成前，不应显示最终状态
    if (!loading.value && props.courseIsClassSubject) return '请在上课班级的详情中，设置上课科目'
  } else {
    // 学员和预约排课，依赖课程。
    if (!props.ShiftID) {
      return '请先选择“上课课程”'
    }
  }
  // 只有在明确选择了课程，且该课程为非全科时，才显示此提示
  if (props.EnableSubject === '0') {
    return '所选课程非全科课程，无需填写'
  }
  // 各种前置条件都满足了，但列表依然为空
  return '暂无科目数据'
})

const filteredSubjectList = computed(() => {
  // 如果是班级排课且没有选择班级，也返回空列表
  if (isClassSchedule.value && !props.classId) {
    return []
  }

  // 如果非班级排课，但未选择课程（EnableSubject 为空），也返回空列表
  if (!isClassSchedule.value && !props.ShiftID) {
    return []
  }

  let list = subjectList.value

  if (!searchKeyword.value) {
    return list
  }

  return list.filter(subjectItem => {
    const keyword = searchKeyword.value.toLowerCase()
    const name = subjectItem.Name?.toLowerCase() || ''
    const value = subjectItem.Value?.toLowerCase() || ''
    const describe = subjectItem.Describe?.toLowerCase() || ''

    return name.includes(keyword) || value.includes(keyword) || describe.includes(keyword)
  })
})

const dynamicPlaceholder = computed(() => {
  // 明确选择了非全科课程
  if (props.EnableSubject === '0') {
    return '无需填写'
  }

  if (isClassSchedule.value) {
    if (!props.classId) return '请先选择班级'
  } else {
    if (!props.ShiftID) {
      return '请先选择课程'
    }
  }
  return props.placeholder
})

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value, getSelectedSubject(value))
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}

const handleSearch = (value) => {
  searchKeyword.value = value
  emit('search', value, filteredSubjectList.value)
}

const getSubjectDesc = (item) => {
  return item.Describe || ''
}

const getSelectedSubject = (value) => {
  return subjectList.value.find(subjectItem => subjectItem.ID === value) || null
}

// 更新科目列表数据
const updateSubjectList = () => {
  if (props.EnableSubject !== '1') {
    subjectList.value = []
    return
  }

  if (isClassSchedule.value) {
    // 班级排课：使用API获取的科目列表
    subjectList.value = apiSubjectList.value
  } else {
    // 学员/预约排课：使用字典中的全量科目
    subjectList.value = dictFieldsStore.dictFields('SUBJECT', 1)
  }
}

// 获取科目列表的函数
const fetchSubjectList = () => {
  // 防止重复调用
  if (isFetching.value) {
    return
  }

  // 只有班级排课模式才需要通过接口获取科目
  if (!isClassSchedule.value) {
    return
  }

  if (!props.classId) {
    apiSubjectList.value = []
    lastClassId.value = ''
    return
  }

  // 如果 classId 没有变化，不重复调用
  if (lastClassId.value === props.classId) {
    return
  }



  // 当没有 classData 但有 classId 且 EnableSubject 为 1 时，调用 API
  if (props.EnableSubject === '1') {
    loading.value = true
    isFetching.value = true
    lastClassId.value = props.classId
    getClassWithShiftAndSubject(props.classId)
      .then(res => {
        if (res.ErrorCode === 200 && res.Data && res.Data.SubjectList) {
          apiSubjectList.value = res.Data.SubjectList
        } else {
          apiSubjectList.value = []
        }
      })
      .catch(() => {
        apiSubjectList.value = []
      })
      .finally(() => {
        // 确保无论成功或失败，加载状态和请求状态都被重置
        loading.value = false
        isFetching.value = false
        // 更新科目列表
        updateSubjectList()
      })
  }
}

watch(() => props.modelValue, (newValue) => {
  selectedValue.value = newValue
}, { immediate: true })

// 监听核心依赖的变化
watch(
  [() => props.classId, () => props.classData, () => props.EnableSubject, () => props.courseIsClassSubject, () => props.selectedTableType],
  () => {
    // 仅在班级排课模式下，才需要调用API
    if (isClassSchedule.value) {
      fetchSubjectList()
    }
    updateSubjectList()
  },
  { immediate: true, deep: true }
)

watch(() => subjectList.value, (newList) => {
  // 移除自动清空选中值的逻辑，避免主动 emit
  // if (selectedValue.value && !newList.find(subjectItem => subjectItem.ID === selectedValue.value)) {
  //   selectedValue.value = ''
  //   emit('update:modelValue', '')
  // }

  // 如果有科目数据，停止加载状态
  if (newList.length > 0) {
    loading.value = false
  }
}, { deep: true })

onMounted(() => {
  // 组件挂载后的初始化逻辑
  console.log('SubjectSelect 组件已挂载')
  // onMounted中的加载状态管理逻辑已被移除，完全交由 fetchSubjectList 控制
})
</script>

<style scoped>
.subject-option {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.subject-name {
  font-size: 14px;
  color: #606266;
  font-weight: 400;
}

.subject-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.subject-empty .wtwo-icon {
  margin-right: 8px;
  font-size: 16px;
}
</style>