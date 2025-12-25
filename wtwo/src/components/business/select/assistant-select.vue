<template>
  <div>
    <BaseSelect v-model="selectedValue" :options="filteredAssistantList" :initial-data="initialDataList"
      :placeholder="placeholderComp" :clearable="clearable" :disabled="disabled" :select-style="selectStyle"
      :show-search="true" :search-placeholder="'搜索助教'" :filterable="true" :show-attention-tips="hasTimeProps"
      :loading="loading" :multiple="true" value-key="id" label-key="name" :get-option-desc="getAssistantDesc"
      empty-text="暂无匹配的助教" @change="handleChange" @clear="handleClear" @visible-change="visibleChange"
      @search="handleSearch" @scroll-bottom="handleScrollBottom" :cell-mode="cellMode">
      <template #option="{ item }">
        <div class="assistant-option">
          <div class="assistant-info">
            <el-avatar :size="20" :src="item.avatar" class="assistant-avatar">
              {{ item.name?.charAt(0) || 'A' }}
            </el-avatar>
            <span class="assistant-name">{{ item.name }}</span>
          </div>
          <span v-if="hasTimeProps" :class="['option-status', item.status === '闲' ? 'status-free' : item.status === '忙' ? 'status-busy' : 'status-loading']">
            {{ item.status || '...' }}
          </span>
        </div>
      </template>
      <template #empty>
        <div class="assistant-empty">
          <el-empty :image="globalData.emptyPng2" :image-size="80" style="padding: 0px 0px;"
            :description="loading ? '加载中...' : searchKeyword ? '暂无匹配的助教' : '请输入名称搜索助教'" />
        </div>
      </template>
      <template #footer>
        <!-- 加载提示 - 统一容器 -->
        <transition name="fade-slide">
          <div v-if="loadMoreLoading || (!hasMore && assistantList.length > 0)" class="assistant-status-tip">
            <template v-if="loadMoreLoading">
              <el-icon class="is-loading status-icon" :size="14">
                <Loading />
              </el-icon>
              <span class="status-text">加载中...</span>
            </template>
            <template v-else>
              <el-icon :size="14" class="status-icon">
                <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path fill="currentColor" d="M512 64a448 448 0 1 1 0 896 448 448 0 0 1 0-896zm-55.808 536.384-99.52-99.584a38.4 38.4 0 1 0-54.336 54.336l126.72 126.72a38.272 38.272 0 0 0 54.336 0l262.4-262.464a38.4 38.4 0 1 0-54.272-54.336L456.192 600.384z"/>
                </svg>
              </el-icon>
              <span class="status-text">已加载全部</span>
            </template>
          </div>
        </transition>
        <!-- 全部人员按钮 -->
        <div class="select-footer" :style="(loadMoreLoading || (!hasMore && assistantList.length > 0)) ? 'border-top: 1px solid #EBEEF5; margin-top: 0; padding-top: 8px;' : ''">
          <el-button type="primary" size="small" text @click="handleAllPersonnel" style="width: 100%">
            全部人员
          </el-button>
        </div>
      </template>
    </BaseSelect>
    <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
  </div>
  <!-- 选择员工弹框 -->
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { getEmployeeAvailabilityStatus, GetEmployeeAvailabilityStatusByPlan } from '@/api/comm'
import { useConfigs } from '@/store'
import { checkTeacherType } from '@/utils/scheduleUtils'
import { transToConfigDescript } from '@/utils/filters/filters'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const props = defineProps({
  modelValue: {
    type: [String, Number, Array],
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择助教'
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
  },
  disabledIds: { // 🆕 阻止选择的ID列表
    type: Array,
    default: () => []
  },
  startTime: { // 排课开始时间
    type: String,
    default: ''
  },
  endTime: { // 排课结束时间
    type: String,
    default: ''
  },
  customParams: { // 自定义参数，预留扩展
    type: Object,
    default: null
  }
})
const configs = computed(() => {
  return useConfigs().configs
})
const EnbaleEmpIsClassTeacher=computed(()=>{
    return configs.value.EnbaleEmpIsClassTeacher
})
const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'goToSettings',
  'allPersonnel',
  'dialogOpen',   // 🆕 UI交互锁：弹框打开
  'dialogClose',  // 🆕 UI交互锁：弹框关闭
  'visible-change' // 🆕 暴露 visible-change 事件，让父组件能够监听
])

const selectedValue = ref([])
const searchKeyword = ref('')
const loading = ref(false)
const loadMoreLoading = ref(false) // 分页加载更多时的 loading
const hasSearched = ref(false)

// 🚀 优化：组件挂载状态标记
const isMounted = ref(false)
// 🆕 用于存储上次的时间参数，用于比较变化
const lastTimeParams = ref({ startTime: '', endTime: '' })

// 🆕 分页相关状态
const currentPage = ref(1)
const pageSize = ref(10) // 每页10条
const hasMore = ref(true) // 是否还有更多数据
const totalCount = ref(0) // 总数据量

// 🆕 忙闲状态查询队列（防止重复查询）
const busyStatusFetching = ref(false)
const busyStatusQueue = ref([]) // 待查询的助教ID队列

const initialDataList = computed(() => {
  let list = []
  if (props.initialData && Array.isArray(props.initialData)) {
    list = props.initialData.map(item => ({
      id: item.ID || item.Id || item.id,
      name: item.Name || item.name,
      TeacherCommissionList: item.TeacherCommissionList || [],
    }))
  }
  return list
})

// 防抖相关
let searchTimer = null
const DEBOUNCE_DELAY = 300 // 300ms防抖延迟

// 内部管理的助教数据
const assistantList = ref([])

// 组件引用
const chooseEmpTableRef = ref()



const filteredAssistantList = computed(() => {
  return assistantList.value
})
const placeholderComp = computed(() => {
  return props.placeholder || ''
})

const hasAssistantData = computed(() => {
  return assistantList.value && assistantList.value.length > 0
})

// 🆕 第一步：分页获取助教列表（不传时间参数，不含忙闲状态）
const fetchAssistantList = async (searchQuery = '', isLoadMore = false) => {
  // 助教必须输入关键词才能搜索
  if (!searchQuery.trim()) {
    assistantList.value = []
    hasSearched.value = false
    hasMore.value = false
    return
  }

  // 如果是加载更多且没有更多数据，直接返回
  if (isLoadMore && !hasMore.value) {
    return
  }

  // 如果是新查询（非加载更多），重置页码和列表
  if (!isLoadMore) {
    currentPage.value = 1
    assistantList.value = []
    hasMore.value = true
    loading.value = true
  } else {
    loadMoreLoading.value = true
  }

  try {
    hasSearched.value = true

    // 🆕 统一构建基础参数（分页）
    const baseParams = {
      PageIndex: currentPage.value,
      PageSize: pageSize.value,
      Desc: 1,
      Query: searchQuery,
      TeacherRole: 2,
    }

    // 🆕 根据是否有自定义参数决定最终参数和API
    let response = null
    if (props.customParams) {
      // 有自定义参数：合并自定义参数，但保留基础参数的优先级
      const params = {
        ...baseParams,
        ...props.customParams,
        SubjectIDList: [],
        IsIncludeStatus: 0 // 🔑 第一步不查忙闲
      }

      response = await GetEmployeeAvailabilityStatusByPlan(params)
    } else {
      // 无自定义参数：使用基础参数
      const params = {
        ...baseParams,
        SubjectIDList: [],
        IsIncludeStatus: 0 // 🔑 第一步不查忙闲
      }
      response = await getEmployeeAvailabilityStatus(params)
    }

    if (response && response.IsSuccess && response.Data && response.Data.List) {
      const list = response.Data.List.map(item => ({
        id: item.ID,
        name: item.Name,
        nickname: item.Serial || '',
        department: item.Department || '',
        avatar: '',
        // 🔑 如果有时间参数，强制设为空字符串，等第二步查询；否则使用 API 返回的状态
        // 注意：即使 API 返回了 Status，如果有时间参数，也要忽略第一步的状态
        status: hasTimeProps.value ? '' : (item.Status === 1 ? '闲' : '忙'),
        BusyStatusLoading: hasTimeProps.value // 如果需要查询忙闲，标记为加载中
      }))
      
      const total = response.Data.TotalCount || 0
      
      // 合并数据（加载更多时追加）
      if (isLoadMore) {
        assistantList.value = [...assistantList.value, ...list]
      } else {
        assistantList.value = list
      }
      
      totalCount.value = total
      hasMore.value = assistantList.value.length < total
      
      // 🆕 第二步：如果有时间参数，异步查询忙闲状态
      if (hasTimeProps.value && list.length > 0) {
        fetchBusyStatus(list.map(item => item.id))
      }
    } else {
      if (!isLoadMore) {
        assistantList.value = []
      }
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取助教数据失败:', error)
    if (!isLoadMore) {
      assistantList.value = []
    }
    hasMore.value = false
  } finally {
    if (isLoadMore) {
      loadMoreLoading.value = false
    } else {
      loading.value = false
    }
  }
}

// 🆕 第二步：异步批量查询助教忙闲状态（传时间参数和IDList）
const fetchBusyStatus = async (assistantIds) => {
  if (!assistantIds || assistantIds.length === 0) {
    return
  }
  
  // 🔑 判断是否有足够的时间参数来查询忙闲状态
  if (props.customParams) {
    // 场景1：按计划查询，必须同时满足 时间(StartTime+EndTime) 和 日期(StartDate+EndDate 或 CourseDateList)
    const hasTime = props.startTime && props.endTime
    const hasDate = (
      (props.customParams.StartDate && props.customParams.StartDate !== '' && 
       props.customParams.EndDate && props.customParams.EndDate !== '') ||  // 重复排课（不能是空字符串）
      (props.customParams.CourseDateList && Array.isArray(props.customParams.CourseDateList) && 
       props.customParams.CourseDateList.length > 0)  // 自由排课
    )
    
    // 必须同时有时间和日期才能查询忙闲
    if (!hasTime || !hasDate) {
      return
    }
  } else {
    // 场景2：普通查询，必须有 startTime 和 endTime
    if (!props.startTime || !props.endTime) {
      return
    }
  }
  
  // 防止重复查询
  if (busyStatusFetching.value) {
    busyStatusQueue.value.push(...assistantIds)
    return
  }
  
  busyStatusFetching.value = true
  
  // 为当前批次的助教设置加载状态
  assistantList.value.forEach(item => {
    if (assistantIds.includes(item.id)) {
      item.BusyStatusLoading = true
    }
  })
  
  try {
    let response = null
    if (props.customParams) {
      const params = {
        ...props.customParams,
        // 🔑 强制覆盖 IDList 和 TeacherRole，保留其他业务参数
        IDList: assistantIds,
        TeacherRole: 2,
        SubjectIDList: [],
        IsIncludeStatus: 1 // 🔑 第二步查忙闲
      }
      
      // 🔑 时间参数优先级：props.startTime/endTime > customParams 中的时间参数
      if (props.startTime && props.endTime) {
        params.StartTime = props.startTime
        params.EndTime = props.endTime
      }
      // 如果 props 没有时间参数，customParams 中的时间参数会保留（已经通过 ...props.customParams 展开）
      
      response = await GetEmployeeAvailabilityStatusByPlan(params)
    } else {
      const params = {
        IDList: assistantIds,
        TeacherRole: 2,
        SubjectIDList: [],
        IsIncludeStatus: 1 // 🔑 第二步查忙闲
      }
      
      // 🔑 时间参数：只有在 props 中有值时才设置
      if (props.startTime && props.endTime) {
        params.StartTime = props.startTime
        params.EndTime = props.endTime
      }
      
      response = await getEmployeeAvailabilityStatus(params)
    }
    
    if (response && response.IsSuccess && response.Data && response.Data.List) {
      const busyStatusMap = {}
      response.Data.List.forEach(item => {
        // 🔑 只有当 Status 字段存在时才设置状态，否则保持空字符串
        if (item.Status !== undefined && item.Status !== null) {
          busyStatusMap[item.ID] = item.Status === 1 ? '闲' : '忙'
        } else {
          busyStatusMap[item.ID] = '' // 后端没返回状态，保持空字符串
        }
      })
      
      // 更新助教的忙闲状态
      assistantList.value.forEach(item => {
        if (assistantIds.includes(item.id)) {
          item.BusyStatusLoading = false
          if (busyStatusMap[item.id] !== undefined) {
            item.status = busyStatusMap[item.id]
          }
          // 🔑 如果后端没返回该助教的数据，也保持空字符串
          else {
            item.status = ''
          }
        }
      })
    } else {
      // 清除加载状态
      assistantList.value.forEach(item => {
        if (assistantIds.includes(item.id)) {
          item.BusyStatusLoading = false
        }
      })
    }
  } catch (error) {
    console.error('获取忙闲状态出错:', error)
    // 清除加载状态
    assistantList.value.forEach(item => {
      if (assistantIds.includes(item.id)) {
        item.BusyStatusLoading = false
      }
    })
  } finally {
    busyStatusFetching.value = false
    
    // 如果队列中还有待查询的ID，继续查询
    if (busyStatusQueue.value.length > 0) {
      const nextBatch = busyStatusQueue.value.splice(0, busyStatusQueue.value.length)
      fetchBusyStatus(nextBatch)
    }
  }
}

// 🆕 滚动到底部时加载更多
const handleScrollBottom = () => {
  if (!isMounted.value || loading.value || loadMoreLoading.value || !hasMore.value) {
    return
  }
  
  // 助教必须有搜索关键词才能加载
  if (!searchKeyword.value || !searchKeyword.value.trim()) {
    return
  }
  
  currentPage.value++
  fetchAssistantList(searchKeyword.value, true) // 加载更多
}

// 防抖搜索函数
const debouncedSearch = (value) => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }

  // 设置新的定时器
  searchTimer = setTimeout(() => {
    // 重置分页
    currentPage.value = 1
    hasMore.value = true
    if (value && value.trim()) {
      fetchAssistantList(value, false)
    } else {
      assistantList.value = []
      hasSearched.value = false
      hasMore.value = false
    }
  }, DEBOUNCE_DELAY)
}
const EnableClassCommission = computed(() => {
  return configs.value.EnableClassCommission == 1
})
const visibleChange = (show) => {
  // 🚀 组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  // 🆕 emit 给父组件，让父组件的 UI 锁逻辑能够执行
  emit('visible-change', show)
  
  if (show) {
    // 🆕 检查时间参数是否发生变化
    const currentTimeParams = { startTime: props.startTime, endTime: props.endTime }
    const timeParamsChanged = (
      lastTimeParams.value.startTime !== currentTimeParams.startTime ||
      lastTimeParams.value.endTime !== currentTimeParams.endTime
    )
    
    if (timeParamsChanged) {
      // 时间参数变化，清空列表，需要用户重新搜索
      lastTimeParams.value = { ...currentTimeParams }
      assistantList.value = []
      hasSearched.value = false
      currentPage.value = 1
      hasMore.value = true
    }
    
    if (EnableClassCommission.value) {
      handleAllPersonnel();
    }
  }
}

const handleChange = (value) => {
  // 🚀 组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  // 🆕 检查新选择的值是否在阻止列表中

  if (Array.isArray(value) && props.disabledIds && props.disabledIds.length > 0) {
    const invalidIds = value.filter(id => props.disabledIds.includes(id))
    if (invalidIds.length > 0) {
      // 获取被阻止的助教信息
      const selectedObjects = getSelectedAssistants(value)
      const blockedAssistants = selectedObjects.filter(obj => invalidIds.includes(obj.ID || obj.id))
      const assistantNames = blockedAssistants.map(obj => obj.Name || obj.name).join('、') || '该助教'
      ElMessageBox.alert(
        `"${assistantNames}"已被设置为${transToConfigDescript('任课老师')}，不能继续设为助教。`,
        '提示',
        {
          confirmButtonText: '知道了',
          type: 'warning'
        }
      )
      // 过滤掉不允许的ID，恢复到有效值
      const validValue = value.filter(id => !props.disabledIds.includes(id))
      selectedValue.value = validValue
      emit('update:modelValue', validValue)
      return
    }
  }

  // 获取完整的对象数组
  const selectedObjects = getSelectedAssistants(value)
  emit('update:modelValue', value) // 传递ID数组
  emit('change', value, selectedObjects)
}

const handleClear = () => {
  // 🚀 组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  emit('update:modelValue', [])
  emit('clear')
}

const handleSearch = (value) => {
  // 🚀 组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  searchKeyword.value = value
  // 使用防抖处理搜索
  debouncedSearch(value)
  emit('search', value, filteredAssistantList.value)
}

const getAssistantDesc = (item) => {
  return item.department || ''
}

const getSelectedAssistants = (value) => {
  if (Array.isArray(value)) {
    return value.map(id => {
      // 首先从assistantList中查找
      let assistant = assistantList.value.find(assistant => assistant.id === id)

      // 如果没找到，尝试从initialData中查找
      if (!assistant && initialDataList.value && Array.isArray(initialDataList.value)) {
        const initialDataItem = initialDataList.value.find(item => item.id === id)
        if (initialDataItem) {
          assistant = {
            id: initialDataItem.id,
            name: initialDataItem.name,
            department: initialDataItem.department || '',
            avatar: initialDataItem.avatar || '',
            status: initialDataItem.status || '闲'
          }
        }
      }

      return assistant
    }).filter(Boolean)
  }
  return []
}

const goToSettings = () => {
  emit('goToSettings')
}

// 🔑 判断是否有有效的时间参数（支持 props 和 customParams 两种场景）
const hasValidTimeParams = () => {
  // 场景1：按计划查询（customParams），必须同时满足 时间 和 日期
  if (props.customParams) {
    const hasTime = props.startTime && props.endTime
    const { StartDate, EndDate, CourseDateList } = props.customParams
    const hasDate = (
      (StartDate && StartDate !== '' && EndDate && EndDate !== '') ||  // 重复排课
      (CourseDateList && Array.isArray(CourseDateList) && CourseDateList.length > 0)  // 自由排课
    )
    // 必须同时有时间和日期
    return hasTime && hasDate
  }
  
  // 场景2：普通排课，只需要 startTime 和 endTime
  return !!(props.startTime && props.endTime)
}

// 判断是否有时间参数
const hasTimeProps = computed(() => {
  return hasValidTimeParams()
})

// 处理全部人员选择
const handleAllPersonnel = () => {
  // 🚀 组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  // 🆕 UI交互锁：弹框打开时通知父组件
  emit('dialogOpen')
  
  chooseEmpTableRef.value?.open({
    popTitle: '选择助教',
    multi: true, // 多选
    disabledCondition:['StatusList'],
    showTeacherType: true,
    choosed: !EnableClassCommission.value ? initialDataList.value.map(item => ({ ID: item.id, Name: item.name })) : [], // 传入当前已选的ID数组，用于回显
    condition: {
      StatusList:[1]
    }
  }).then((result) => {
    const teacherCategory = result.other;
    if (result && result.data && Array.isArray(result.data)) {
      const selectedAssistants = result.data

      // 🆕 1. 过滤掉阻止列表中的ID（任课老师）
      const blockedAssistants = []
      const filteredAssistants = selectedAssistants.filter(assistant => {
        if (props.disabledIds && props.disabledIds.includes(assistant.ID)) {
          blockedAssistants.push(assistant)
          return false
        }
        return true
      })

      // 如果有被过滤的，提示用户
      if (blockedAssistants.length > 0) {
        const assistantNames = blockedAssistants.map(a => a.Name).join('、')
        ElMessageBox.alert(
          `"${assistantNames}"已被设置为${transToConfigDescript('任课老师')}，不能继续设为助教。`,
          '提示',
          {
            confirmButtonText: '知道了',
            type: 'warning'
          }
        )
      }

      // 如果全部被过滤了，直接返回
      if (filteredAssistants.length === 0) {
        return
      }

      // 🆕 2. 如果启用了教师类别功能，使用 checkTeacherType 检查类别唯一性
      if (EnableClassCommission.value && teacherCategory && teacherCategory.ID) {
        // 检查每个选中的助教
        for (const assistant of filteredAssistants) {
          // 构建已有助教列表：
          // 1. 包含 initialDataList 中的原有助教（排除当前助教）
          // 2. 包含本次选择的其他助教（result.data 中除了当前助教的其他人）
          const existingAssistantsFromInitial = initialDataList.value
            .filter(item => item.id !== assistant.ID)
            .map(item => {
              // 将 TeacherCommissionList 转换为 TeacherCommissionIDs 字符串
              const commissionIDs = item.TeacherCommissionList && Array.isArray(item.TeacherCommissionList)
                ? item.TeacherCommissionList.map(c => c.ID || c.Id || c.id).filter(Boolean).join(',')
                : ''

              return {
                ID: item.id,
                Name: item.name,
                TeacherCommissionIDs: commissionIDs
              }
            })

          const otherSelectedAssistants = filteredAssistants
            .filter(item => item.ID !== assistant.ID)
            .map(a => ({
              ID: a.ID,
              Name: a.Name,
              TeacherCommissionIDs: teacherCategory?.ID || ''
            }))

          // 合并两部分助教
          const existingAssistants = [
            ...existingAssistantsFromInitial,
            ...otherSelectedAssistants
          ]

          // 使用 checkTeacherType 检查类别冲突
          const pass = checkTeacherType(assistant, teacherCategory, existingAssistants)

          if (!pass) {
            // checkTeacherType 内部已经显示了 ElMessageBox.alert
            return
          }
        }
      }

      // 🆕 3. 增量合并：将新选择的助教与原有助教合并（去重）
      // 创建一个 Map 用于去重，key 为助教ID
      const assistantMap = new Map()

      // 先添加原有的助教（保留原有数据）
      if (initialDataList.value && initialDataList.value.length > 0) {
        initialDataList.value.forEach(item => {
          // 如果该助教已经存在于新选择中，跳过（让新选择的覆盖）
          const isInNewSelection = filteredAssistants.some(a => a.ID === item.id)
          if (!isInNewSelection) {
            assistantMap.set(item.id, {
              id: item.id,
              name: item.name,
              TeacherCommissionList: item.TeacherCommissionList || [],
              nickname: '',
              department: '',
              avatar: '',
              status: '闲'
            })
          }
        })
      }

      // 再添加/更新新选择的助教
      filteredAssistants.forEach(assistant => {
        const assistantData = {
          id: assistant.ID,
          TeacherCommissionList: teacherCategory.ID ? [teacherCategory] : [],
          name: assistant.Name + (teacherCategory && teacherCategory.Name ? `（${teacherCategory.Name}）` : ''),
          nickname: assistant.Serial || '',
          department: assistant.DepartName || '',
          avatar: assistant.Photo || '',
          status: assistant.Status === 1 ? '闲' : '忙'
        }

        // 如果该助教已存在，则更新；否则新增
        assistantMap.set(assistant.ID, assistantData)
      })

      // 将 Map 转换为数组
      const assistantDataList = Array.from(assistantMap.values())

      // 更新选中值 - 传递ID数组而不是对象数组
      const selectedIds = assistantDataList.map(item => item.id)
      selectedValue.value = selectedIds
      emit('update:modelValue', selectedIds) // 传递ID数组
      emit('change', selectedIds, assistantDataList)

      // 将选中的员工添加到列表中（如果不存在）
      assistantDataList.forEach(assistantData => {
        const existingIndex = assistantList.value.findIndex(item => item.id === assistantData.id)
        if (existingIndex === -1) {
          assistantList.value.unshift(assistantData)
        }
      })
      
      // 🆕 UI交互锁：弹框关闭时通知父组件
      emit('dialogClose')
      console.log('🔓 [AssistantSelect] emit dialogClose')
    }
  }).catch((error) => {
    console.error('选择助教失败:', error)
    // 🆕 UI交互锁：即使失败也要释放锁
    emit('dialogClose')
    console.log('🔓 [AssistantSelect] emit dialogClose (error)')
  })
}

watch(() => props.modelValue, (newValue) => {
  if (Array.isArray(newValue)) {
    // 如果是对象数组，提取ID
    if (newValue.length > 0 && typeof newValue[0] === 'object' && newValue[0].id) {
      selectedValue.value = newValue.map(item => item.id)
    } else {
      // 如果是ID数组
      selectedValue.value = newValue
    }
  } else {
    selectedValue.value = []
  }
}, { immediate: true })

watch(assistantList, (newList) => {
  if (Array.isArray(selectedValue.value)) {
    const validIds = selectedValue.value.filter(id =>
      newList.find(assistant => assistant.id === id)
    )
    if (validIds.length !== selectedValue.value.length) {
      // selectedValue.value = validIds
      // emit('update:modelValue', validIds) // 传递有效的ID数组
    }
  }
}, { deep: true })

onMounted(() => {
  isMounted.value = true
  console.log('✅ AssistantSelect 组件已挂载')
  // 🆕 初始化 lastTimeParams
  lastTimeParams.value = { startTime: props.startTime, endTime: props.endTime }
  // 默认不请求接口，保持空数据状态
})

// 组件卸载时清理定时器
onUnmounted(() => {
  isMounted.value = false
  lastTimeParams.value = { startTime: '', endTime: '' }
  console.log('❌ AssistantSelect 组件销毁')
  
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
})

defineExpose({
  search: handleSearch,
  clearSearch: () => {
    // 清除定时器
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
    searchKeyword.value = ''
    assistantList.value = []
    hasSearched.value = false
    currentPage.value = 1
    hasMore.value = true
    emit('search', '', assistantList.value)
  },
  refresh: () => {
    if (searchKeyword.value && searchKeyword.value.trim()) {
      currentPage.value = 1
      hasMore.value = true
      fetchAssistantList(searchKeyword.value, false)
    }
  }
})
</script>

<style scoped>
.assistant-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.assistant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assistant-avatar {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
  border-radius: 4px 4px 4px 4px;
  font-weight: 400;
  font-size: 13px;
  color: #FFFFFF;
}

.assistant-name {
  font-weight: 500;
  color: #303133;
}

.assistant-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #909399;
}

.assistant-empty .wtwo-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.assistant-empty-tip {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  line-height: 1.4;
}

.assistant-label {
  color: #409eff;
  font-weight: 500;
  margin: 0 4px;
}

.assistant-status-tip {
  padding: 8px 12px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: default;
}

.assistant-status-tip .status-icon {
  flex-shrink: 0;
  color: #C0C4CC;
  font-size: 14px;
}

.assistant-status-tip .status-text {
  font-size: 12px;
  color: #C0C4CC;
}

/* 淡入淡出 + 向下滑动动画 */
.fade-slide-enter-active {
  transition: all 0.3s ease-out;
}

.fade-slide-leave-active {
  transition: all 0.25s ease-in;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-4px);
  max-height: 0;
}

.fade-slide-enter-to {
  opacity: 1;
  transform: translateY(0);
  max-height: 32px;
}

.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 32px;
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-2px);
  max-height: 0;
}
</style>
