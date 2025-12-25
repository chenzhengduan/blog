<template>
  <div>
    <BaseSelect v-model="selectedValue" :defaultNotshowEmpty="!!searchKeyword.value || true" :options="filteredStudentList"
      :initial-data="props.initialData" :placeholder="dynamicPlaceholder" :clearable="clearable" :disabled="disabled"
      :select-style="selectStyle" :id="id" :show-search="!!campusId" :search-placeholder="'搜索学员'" :filterable="true"
      :loading="loading" :cell-mode="cellMode&&!!campusId" value-key="ID" label-key="Name" :get-option-desc="getStudentDesc"
      empty-text="暂无匹配的学员" @change="handleChange" @clear="handleClear" @search="handleSearch">
      <template #option="{ item }">
        <div class="student-option">
          <el-avatar :size="20" :src="item.Photo" class="student-avatar">
            {{ item.Name?.charAt(0) || 'S' }}
          </el-avatar>
          <span class="student-name">{{ item.Name }}</span>
          <span class="student-serial" v-if="item.Serial">({{ item.Serial }})</span>
        </div>
      </template>
      <template #empty>
        <el-empty v-show="searchKeyword || loading || !campusId" :image="globalData.emptyPng2" :image-size="80"
          :description="loading ? '加载中...' : (!campusId ? transToConfigDescript('请先选“上课校区”') : searchKeyword ? '暂无匹配的学员' : ' ')" />
      </template>
      <template #footer v-if="props.campusId">
        <div class="select-footer">
          <el-button type="primary" size="small" text :disabled="!props.campusId" @click="handleViewAllStudents"
            style="width: 100%">
            <span class="select-footer-text">全部学员</span>
          </el-button>
        </div>
      </template>
    </BaseSelect>

    <!-- 全部学员选择对话框 -->
    <chooseStudent ref="chooseStudentRef" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, getCurrentInstance } from 'vue'
import { Search, InfoFilled, Grid } from '@element-plus/icons-vue'
import { queryStudentBreif } from '@/api'
import chooseStudent from '@/components/popup/chooseStudent.vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
import { useConfigs } from '@/store';
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择学员'
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
  },
})
const configs = computed(() => {
	return useConfigs().configs
})
//配置项
const ShowAllStudentsWhenCoursePlan=computed(()=>{ //一对一排课时，是否可以跨校区选择学员：1是（默认），0否（只能选择当前校区的学员）。
	return configs.value.ShowAllStudentsWhenCoursePlan==1
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'exitEdit',
  'dialogOpen',   // 🆕 弹框打开事件
  'dialogClose'   // 🆕 弹框关闭事件
])

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const studentList = ref([])
const chooseStudentRef = ref()

const filteredStudentList = computed(() => {
  // true ，不可以跨校区选择学员
  // 如果这一行没有校区id，且配置项要求只能选择当前校区学员，则返回空列表
  if (!props.campusId && !ShowAllStudentsWhenCoursePlan.value) {
    return []
  }

  return studentList.value.filter(studentItem => {
    const keyword = searchKeyword.value.toLowerCase()
    const name = studentItem.Name?.toLowerCase() || ''
    const serial = studentItem.Serial?.toLowerCase() || ''

    return name.includes(keyword) || serial.includes(keyword)
  })
})

const dynamicPlaceholder = computed(() => {
  return props.placeholder
})

const handleChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value, getSelectedStudent(value))
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}

const handleSearch = (value) => {
  searchKeyword.value = value
  emit('search', value, filteredStudentList.value)
}

const getStudentDesc = (item) => {
  return item.Serial || ''
}

const handleViewAllStudents = () => {
  if (!props.campusId) {
    return
  }
  
  // 🆕 通知父组件弹框即将打开
  emit('dialogOpen')
  let disabledCondition=[]
  if(!ShowAllStudentsWhenCoursePlan.value){
    disabledCondition=['campusid']
  }
  // 打开全部学员选择对话框
  chooseStudentRef.value?.open({
    multi: false, // 单选模式
    popTitle: '选择学员',
    required: false,
    showOneToOneStudent:true,
    disabledCondition:disabledCondition,
    condition: {
      status: "1", // 在读学员
      campusid: props.campusId,
      IsOnlyShowOneToOneStudent:1
    }
  }).then((res) => {
    if (res && res.data) {
      const selectedStudent = res.data

      // 如果学员不在当前列表中，添加到列表中
      const existingStudent = studentList.value.find(item => item.ID === selectedStudent.ID)
      if (!existingStudent) {
        studentList.value.push(selectedStudent)
      }

      // 先触发change事件，让父组件更新值
      handleChange(selectedStudent.ID)

      // 使用nextTick确保DOM更新后再设置selectedValue
      nextTick(() => {
        selectedValue.value = selectedStudent.ID

        // 🆕 立即通知父组件弹框已关闭（在延迟之前）
        emit('dialogClose')
        console.log('🔓 [StudentSelect] emit dialogClose')

        // 延迟一下再退出编辑状态，让用户看到选中的值
        setTimeout(() => {
          emit('exitEdit')
        }, 500)
      })

      console.log('从全部学员中选择:', selectedStudent)
    }
  }).catch((error) => {
    // 🆕 取消选择时也要通知弹框关闭
    emit('dialogClose')
    console.log('🔓 [StudentSelect] emit dialogClose (cancelled)')
    console.log('取消选择学员')
  })
}

const getSelectedStudent = (value) => {
  return studentList.value.find(studentItem => studentItem.ID === value) || null
}

const fetchStudentList = async () => {
  if (!props.campusId) {
    studentList.value = []
    return
  }

  // 如果设置了默认不调用接口，且没有搜索关键词，则不调用接口
  if (props.defaultNoApiCall && !searchKeyword.value.trim()) {
    studentList.value = []
    return
  }

  loading.value = true
  try {
    const params = {
      // 不可以跨校区选择学员则传校区id
      campusid: !ShowAllStudentsWhenCoursePlan.value ? props.campusId : '',
      PageIndex: 1,
      PageSize: 20, // 每页20条数据
      desc: 0,
      sort: 'Name',
      status: "1", // 在读学员
      Query: searchKeyword.value
    }

    const res = await queryStudentBreif(params)
    studentList.value = res.Data || []
  } catch (error) {
    console.error('获取学员列表失败:', error)
    studentList.value = []
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (newValue) => {
  console.log('StudentSelect: modelValue变化', { newValue, studentListLength: studentList.value.length, campusId: props.campusId })

  if (newValue) {
    // 如果studentList还没有加载完成，等待加载完成后再设置
    if (studentList.value.length === 0 && props.campusId) {
      console.log('StudentSelect: studentList为空，等待加载...')
      // 等待studentList加载完成，最多等待3秒
      let retryCount = 0
      const maxRetries = 30 // 30 * 100ms = 3秒

      const checkStudentList = () => {
        if (studentList.value.length > 0) {
          console.log('StudentSelect: studentList加载完成，设置selectedValue', newValue)
          selectedValue.value = newValue
        } else if (retryCount < maxRetries) {
          retryCount++
          setTimeout(checkStudentList, 100)
        } else {
          // 超时后直接设置，即使没有找到对应的选项
          console.warn('StudentSelect: studentList加载超时，直接设置selectedValue')
          selectedValue.value = newValue
        }
      }
      checkStudentList()
    } else {
      console.log('StudentSelect: 直接设置selectedValue', newValue)
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
      fetchStudentList()
    }
  } else {
    studentList.value = []
  }

  // 如果当前选中值不在新校区的学员中，清空选择
  if (selectedValue.value && newCampusId && studentList.value) {
    const currentStudent = studentList.value.find(studentItem => studentItem.ID === selectedValue.value)
    if (!currentStudent || currentStudent.CampusID !== newCampusId) {
      selectedValue.value = ''
      emit('update:modelValue', '')
    }
  }
}, { immediate: true })

watch(searchKeyword, (newKeyword) => {
  if (props.campusId) {
    fetchStudentList()
  }
}, { debounce: 300 })

onMounted(() => {
  // 组件挂载后的初始化逻辑
  console.log('StudentSelect 组件已挂载')
})
</script>

<style scoped>
.student-option {
  display: flex;
  width: 100%;
  align-items: center;
}

.student-avatar {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
  border-radius: 4px;
  font-weight: 400;
  font-size: 13px;
  color: #FFFFFF;
  margin-right: 2px;
}

.student-name {
  font-size: 14px;
  color: #606266;
  font-weight: 400;
  margin-left: 8px;
}

.student-serial {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.student-empty {
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

.student-empty .wtwo-icon {
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
