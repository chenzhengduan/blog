<template>
  <BaseSelect
    v-model="selectedValue"
    :options="filteredCampusList"
    :initial-data="props.initialData"
    :placeholder="transToConfigDescript(dynamicPlaceholder)"
    :clearable="clearable"
    :disabled="disabled"
    :select-style="selectStyle"
    :show-search="true"
    :search-placeholder="transToConfigDescript('搜索校区')"
    :filterable="true"
    :loading="loading"
    :cell-mode="cellMode"
    value-key="ID"
    label-key="Name"
    :get-option-desc="getCampusDesc"
    :empty-text="transToConfigDescript('暂无匹配的校区')"
    @change="handleChange"
    @clear="handleClear"
    @search="handleSearch"
  >
    <template #option="{ item }">
      <div class="campus-option">
        <span class="campus-name">{{ item.Name }}</span>
        <span v-if="item.Description" class="campus-description">{{ item.Description }}</span>
      </div>
    </template>
    <template #empty>
      <div class="campus-empty">
        <el-icon><InfoFilled /></el-icon>
        <span v-if="loading">加载中...</span>
        <span v-else>{{transToConfigDescript('暂无匹配的校区')}}</span>
      </div>
    </template>
  </BaseSelect>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Search, InfoFilled } from '@element-plus/icons-vue'
import { useUserCampuses } from '@/store'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { transToConfigDescript } from '@/utils/filters/filters'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: transToConfigDescript('请选择校区')
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
  }
})
const dynamicPlaceholder = computed(() => {
  // if (!props.campusId) {
  //   return '请选择上课校区'
  // }
  return props.placeholder
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
const userCampusesStore = useUserCampuses()
const campusList = computed(() => userCampusesStore.userCampuses || [])

const filteredCampusList = computed(() => {
  if (!searchKeyword.value) return campusList.value
  return campusList.value.filter(campus => {
    const keyword = searchKeyword.value.toLowerCase()
    const name = campus.Name?.toLowerCase() || ''
    const description = campus.Description?.toLowerCase() || ''
    return name.includes(keyword) || description.includes(keyword)
  })
})

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value, getSelectedCampus(value))
}
const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}
const handleSearch = (value) => {
  searchKeyword.value = value
  emit('search', value, filteredCampusList.value)
}
const getCampusDesc = (item) => {
  return item.Description || ''
}
const getSelectedCampus = (value) => {
  return campusList.value.find(campus => campus.ID === value) || null
}
watch(() => props.modelValue, (newValue) => {
  selectedValue.value = newValue
}, { immediate: true })
watch(campusList, (newList) => {
  if (selectedValue.value && !newList.find(campus => campus.ID === selectedValue.value)) {
    selectedValue.value = ''
    emit('update:modelValue', '')
  }
  
  // 如果有校区数据，停止加载状态
  if (newList.length > 0) {
    loading.value = false
  }
}, { deep: true })
onMounted(() => {
  // 组件挂载后的初始化逻辑
  console.log('CampusSelect 组件已挂载')
  
  // 如果校区数据还没有加载，显示加载状态
  if (campusList.value.length === 0) {
    loading.value = true
    // 监听校区数据变化
    const unwatch = watch(campusList, (newList) => {
      if (newList.length > 0) {
        loading.value = false
        unwatch()
      }
    }, { immediate: true })
    
    // 设置超时，避免loading状态一直显示
    setTimeout(() => {
      if (loading.value) {
        loading.value = false
        console.warn('CampusSelect: 加载超时，停止loading状态')
      }
    }, 10000) // 10秒超时
  }
})
</script>

<style scoped>
.campus-option {
  display: flex;
  flex-direction: column;
  width: 100%;
}
.campus-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.campus-description {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.campus-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}
.campus-empty .wtwo-icon {
  margin-right: 8px;
  font-size: 16px;
}
</style>
