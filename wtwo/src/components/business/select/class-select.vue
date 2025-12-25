<template>
  <div>
    <BaseSelect v-model="selectedValue" :defaultNotshowEmpty="!!searchKeyword.value || true"
      :options="filteredClassList" :initial-data="props.initialData" :placeholder="dynamicPlaceholder"
      :clearable="clearable" :disabled="disabled" :select-style="selectStyle" :id="id" :show-search="!!campusId"
      :search-placeholder="transToConfigDescript('搜索班级')" :filterable="true" :loading="loading" :cell-mode="cellMode && !!campusId"
      value-key="ID" label-key="Name" :get-option-desc="getClassDesc" empty-text="暂无匹配的班级" @change="handleChange"
      @clear="handleClear" @search="handleSearch">
      <template #option="{ item }">
        <div class="class-option">
          <el-icon size="20px">
            <svg aria-hidden="true">
              <use xlink:href="#w2-xuanban1"></use>
            </svg>
          </el-icon>
          <span class="class-name">{{ item.Name }}</span>
        </div>
      </template>
      <template #empty>
        <el-empty v-show="searchKeyword || loading || !campusId" :image="globalData.emptyPng2" :image-size="80"
          :description="loading ? '加载中...' : transToConfigDescript(!campusId ? '请先选“上课校区”' : searchKeyword ? '暂无匹配的班级' : ' ')" />
      </template>
      <template #footer v-if="props.campusId">
        <div class="select-footer">
          <el-button type="primary" size="small" text :disabled="!props.campusId" @click="handleViewAllClasses"
            style="width: 100%">
            <el-icon size="20px">
              <svg aria-hidden="true">
                <use xlink:href="#w2-xuanban"></use>
              </svg>
            </el-icon>
            <span class="select-footer-text">{{transToConfigDescript('全部班级')}}</span>
          </el-button>
        </div>
      </template>
    </BaseSelect>

    <!-- 全部班级选择对话框 -->
    <chooseClass ref="chooseClassRef" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, getCurrentInstance } from 'vue'
import { queryClass } from '@/api'
import { useConfigs, useUserCampuses } from '@/store'
import chooseClass from '@/components/popup/chooseClass.vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const configs = computed(() => {
  return useConfigs().configs
})
const EnableClassOpeningStatus = computed(() => { //是否开启设置班级开班状态的功能：0否（默认）；1是，开启后班级信息中会增加班级状态的属性，状态包括已开班和待定班，只有已开班状态的班级可以进行排课。并且系统会增加修改班级开班状态的权限，拥有此权限的人才可以对班级状态进行更改。
  return configs.value.EnableClassOpeningStatus == 1
})
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择班级'
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
  campusId: {
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
  defaultNoApiCall: { // 新增：默认不调用接口，只有搜索时才调用
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'exitEdit',
  'dialogOpen',   // 🆕 UI交互锁：弹框打开
  'dialogClose'   // 🆕 UI交互锁：弹框关闭
])

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const classList = ref([])
const chooseClassRef = ref()

const filteredClassList = computed(() => {
  if (!props.campusId) {
    return []
  }


  return classList.value.filter(classItem => {
    const keyword = searchKeyword.value.toLowerCase()
    const name = classItem.Name?.toLowerCase() || ''
    const shiftName = classItem.ShiftName?.toLowerCase() || ''

    return name.includes(keyword) || shiftName.includes(keyword)
  })
})

const dynamicPlaceholder = computed(() => {
  // if (!props.campusId) {
  //   return '请选择上课校区'
  // }
  return props.placeholder
})

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value, getSelectedClass(value))
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}

const handleSearch = (value) => {
  searchKeyword.value = value
  emit('search', value, filteredClassList.value)
}

const getClassDesc = (item) => {
  return item.ShiftName || ''
}

const handleViewAllClasses = () => {
  if (!props.campusId) {
    return
  }
  
  // 🆕 UI交互锁：弹框打开时通知父组件
  emit('dialogOpen')
  
  // 打开全部班级选择对话框
  chooseClassRef.value?.open({
    multi: false, // 单选模式
    popTitle: '选择班级',
    showCampus: true, // 显示校区筛选
    isAllCampus: true, // 显示所有校区
    required: false,
    campusID: props.campusId, // 传入当前校区ID
    condition: {
      shiftType: 6,
      isFilterByRight: 1,
      isNoFinished: 1,
      classStatusSelect: EnableClassOpeningStatus.value ? 1 : -1
    }
  }).then((res) => {
    if (res && res.data) {
      const selectedClass = res.data

      // 如果班级不在当前列表中，添加到列表中
      const existingClass = classList.value.find(item => item.ID === selectedClass.ID)
      if (!existingClass) {
        classList.value.push(selectedClass)
      }

      // 先触发change事件，让父组件更新值
      handleChange(selectedClass.ID)

      // 使用nextTick确保DOM更新后再设置selectedValue
      nextTick(() => {
        selectedValue.value = selectedClass.ID

        // 延迟一下再退出编辑状态，让用户看到选中的值
        setTimeout(() => {
          emit('exitEdit')
        }, 500)
      })

      console.log('从全部班级中选择:', selectedClass)
      
      // 🆕 UI交互锁：弹框关闭时通知父组件
      emit('dialogClose')
      console.log('🔓 [ClassSelect] emit dialogClose')
    }
  }).catch((error) => {
    console.log('取消选择班级')
    // 🆕 UI交互锁：即使取消也要释放锁
    emit('dialogClose')
    console.log('🔓 [ClassSelect] emit dialogClose (cancelled)')
  })
}

const getSelectedClass = (value) => {
  return classList.value.find(classItem => classItem.ID === value) || null
}

const fetchClassList = async () => {
  if (!props.campusId) {
    classList.value = []
    return
  }

  // 如果设置了默认不调用接口，且没有搜索关键词，则不调用接口
  if (props.defaultNoApiCall && !searchKeyword.value.trim()) {
    classList.value = []
    return
  }

  loading.value = true
  try {
    const params = {
      campus: props.campusId,
      PageIndex: 1,
      PageSize: 20, // 每页20条数据
      desc: 0,
      sort: 'Name',
      shiftType: 6, // 集体班
      countStudents: 1,
      type: [], // 普通班级、试听班级、补课班级
      Query: searchKeyword.value,
      classStatusSelect: EnableClassOpeningStatus.value ? 1 : -1
    }

    const res = await queryClass(params)
    classList.value = res.Data || []
  } catch (error) {
    console.error('获取班级列表失败:', error)
    classList.value = []
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (newValue) => {
  console.log('ClassSelect: modelValue变化', { newValue, classListLength: classList.value.length, campusId: props.campusId })

  if (newValue) {
    // 如果classList还没有加载完成，等待加载完成后再设置
    if (classList.value.length === 0 && props.campusId) {
      console.log('ClassSelect: classList为空，等待加载...')
      // 等待classList加载完成，最多等待3秒
      let retryCount = 0
      const maxRetries = 30 // 30 * 100ms = 3秒

      const checkClassList = () => {
        if (classList.value.length > 0) {
          console.log('ClassSelect: classList加载完成，设置selectedValue', newValue)
          selectedValue.value = newValue
        } else if (retryCount < maxRetries) {
          retryCount++
          setTimeout(checkClassList, 100)
        } else {
          // 超时后直接设置，即使没有找到对应的选项
          console.warn('ClassSelect: classList加载超时，直接设置selectedValue')
          selectedValue.value = newValue
        }
      }
      checkClassList()
    } else {
      console.log('ClassSelect: 直接设置selectedValue', newValue)
      selectedValue.value = newValue
    }
  } else {
    selectedValue.value = newValue
  }
}, { immediate: true })

watch(() => props.campusId, (newCampusId) => {
  if (newCampusId) {
    // 如果设置了默认不调用接口，则不自动调用接口
    if (!props.defaultNoApiCall) {
      fetchClassList()
    }
  } else {
    classList.value = []
  }

  // 如果当前选中值不在新校区的班级中，清空选择
  if (selectedValue.value && newCampusId && classList.value) {
    const currentClass = classList.value.find(classItem => classItem.ID === selectedValue.value)
    if (!currentClass || currentClass.CampusID !== newCampusId) {
      selectedValue.value = ''
      emit('update:modelValue', '')
    }
  }
}, { immediate: true })

watch(searchKeyword, (newKeyword) => {
  if (props.campusId) {
    fetchClassList()
  }
}, { debounce: 300 })

onMounted(() => {
  // 组件挂载后的初始化逻辑
  console.log('ClassSelect 组件已挂载')
})
</script>

<style scoped>
.class-option {
  display: flex;
  width: 100%;
  align-items: center;
}

.class-name {
  font-size: 14px;
  color: #606266;
  font-weight: 400;
  margin-left: 8px;
}

.class-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 20px;
  color: #909399;
  font-size: 14px;

  img {
    width: 80px;
    height: 80px;
  }
}

.class-empty .wtwo-icon {
  margin-right: 8px;
  font-size: 16px;
}

.select-footer {
  .wtwo-button {
    font-size: 14px;
    color: #2878E8;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;

    .select-footer-text {
      margin-left: 6px;
    }
  }
}
</style>