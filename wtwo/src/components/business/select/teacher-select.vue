<!-- 用于选择任课老师 -->
<template>
  <div>
    <BaseSelect v-model="selectedValue" :options="filteredTeacherList" :initial-data="initialDataList"
      :placeholder="transToConfigDescript(dynamicPlaceholder)" :clearable="clearable" :disabled="disabled" :select-style="selectStyle"
      :show-search="true" :search-placeholder="transToConfigDescript('搜索任课老师')" :filterable="true" :loading="loading" value-key="id"
      label-key="name" :get-option-desc="getTeacherDesc" :empty-text="transToConfigDescript('暂无匹配的任课老师')" :show-attention-tips="true"
      :attention-tips="transToConfigDescript('显示设置为任课老师的员工，并基于排课时间计算忙闲')" @change="handleChange" @clear="handleClear" @search="handleSearch"
      @visible-change="visibleChange" @scroll-bottom="handleScrollBottom"
      :cell-mode="!!(cellModeVal && EnableMustSameSubjectTeacherCourse && props.ShiftID)">
      <template #option="{ item }">
        <div class="teacher-option">
          <div class="teacher-info">
            <el-avatar :size="32" :src="item.avatar" class="teacher-avatar">
              {{ item.name?.charAt(0) || 'T' }}
            </el-avatar>
            <span class="teacher-name" :title="item.name">{{ item.name }}</span>
          </div>
          <span v-if="hasTimeProps" :class="['option-status', item.status === '闲' ? 'status-free' : item.status === '忙' ? 'status-busy' : 'status-loading']">
            {{ item.status || '...' }}
          </span>
        </div>
      </template>
      <template #empty>
        <div class="teacher-empty">
          <el-empty :image="globalData.emptyPng2" :image-size="80" style="padding: 0px 0px;"
            :description="transToConfigDescript(emptyDescription)" />
          <div v-if="!hasTeacherData && !loading && !searchKeyword" class="teacher-empty-tip">
            请前往 <span class="teacher-empty-tip-link">"管理后台-人员管理"</span> 为员工设置
            <span class="teacher-label">{{transToConfigDescript('任课老师')}}</span>
            <br>
            <el-button type="primary" link size="small" @click="goToSettings">
              去设置
            </el-button>
          </div>
        </div>
      </template>
      <template #footer v-if="!EnableMustSameSubjectTeacherCourse || props.ShiftID">
        <!-- 加载提示 - 统一容器 -->
        <transition name="fade-slide">
          <div v-if="loadMoreLoading || (!hasMore && teacherList.length > 0)" class="teacher-status-tip">
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
        <div class="select-footer" :style="(loadMoreLoading || (!hasMore && teacherList.length > 0)) ? 'border-top: 1px solid #EBEEF5; margin-top: 0; padding-top: 8px;' : ''">
          <el-button type="primary" size="small" text @click="handleAllPersonnel" style="width: 100%">
            全部人员
          </el-button>
        </div>
      </template>
    </BaseSelect>

    <!-- 选择员工弹框 -->
    <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { getEmployeeAvailabilityStatus, GetEmployeeAvailabilityStatusByPlan } from '@/api/comm'
import { useConfigs } from '@/store'
import { transToConfigDescript } from '@/utils/filters/filters'
const configs = computed(() => {
  return useConfigs().configs
})
// 配置项
const EnableMustSameSubjectTeacherCourse = computed(() => { //是否开启限制跨科目选择老师 0允许（默认） 1不允许
  return configs.value.EnableMustSameSubjectTeacherCourse == 1
})
const EnbaleEmpIsClassTeacher=computed(()=>{
    return configs.value.EnbaleEmpIsClassTeacher
})
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择任课老师'
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
  showStatus: {
    type: Boolean,
    default: true
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
  startTime: { // 排课开始时间
    type: String,
    default: ''
  },
  endTime: { // 排课结束时间
    type: String,
    default: ''
  },
  ShiftID: { // 课程id
    type: String,
    default: ''
  },
  disabledIds: { // 🆕 阻止选择的ID列表
    type: Array,
    default: () => []
  },
  SubjectIDList: {
    type: Array,
    default: () => []
  },
  EnableSubject: {
    type: Boolean,
    default: false
  },
  Check_Shift_Teacher_Subject: { // 是否开启限制跨科目选择老师
    type: Boolean,
    default: false
  },
  shiftSubjectId: {
    type: String,
    default: ''
  },
  customParams: { // 额外的自定义参数
    type: Object,
    default: null
  }
})
const cellModeVal = computed(() => {
  return props.cellMode
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

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const loadMoreLoading = ref(false) // 分页加载更多时的 loading

// 防抖相关
let searchTimer = null
const DEBOUNCE_DELAY = 300 // 300ms防抖延迟

// 内部管理的任课老师数据
const teacherList = ref([])

// 🆕 分页相关状态
const currentPage = ref(1)
const pageSize = ref(10) // 每页10条
const hasMore = ref(true) // 是否还有更多数据
const totalCount = ref(0) // 总数据量

// 🆕 忙闲状态查询队列（防止重复查询）
const busyStatusFetching = ref(false)
const busyStatusQueue = ref([]) // 待查询的教师ID队列

// 组件引用
const chooseEmpTableRef = ref()

// 动态占位符
const dynamicPlaceholder = computed(() => {
  // if (loading.value) return '搜索中...'
  return props.placeholder
})

const initialDataList = computed(() => {
  let list = {}
  if (props.initialData) {
    list = {
      id: props.initialData.ID || props.initialData.Id || props.initialData.id,
      name: props.initialData.Name || props.initialData.name,
    }
  }
  return list
})

const filteredTeacherList = computed(() => {
  return teacherList.value
})

// 空状态描述
const emptyDescription = computed(() => {
  if (loading.value) return '加载中...'
  if (EnableMustSameSubjectTeacherCourse.value && !props.ShiftID) return '请先选“上课课程”'
  if (searchKeyword.value) return '暂无匹配的任课老师'
  return '暂未设置任课老师'
})

const hasTeacherData = computed(() => {
  return teacherList.value && teacherList.value.length > 0
})

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

// 🆕 第一步：分页获取教师列表（不传时间参数，不含忙闲状态）
const fetchTeacherList = async (searchQuery = '', isLoadMore = false) => {
  // 如果是加载更多且没有更多数据，直接返回
  if (isLoadMore && !hasMore.value) {
    return
  }

  // 如果是新查询（非加载更多），重置页码和列表
  if (!isLoadMore) {
    currentPage.value = 1
    teacherList.value = []
    hasMore.value = true
    loading.value = true
  } else {
    loadMoreLoading.value = true
  }

  try {
    let shiftSubjectId = props.shiftSubjectId || ''
    // 🆕 统一构建基础参数（分页）
    const baseParams = {
      PageIndex: currentPage.value,
      PageSize: pageSize.value,
      Desc: 1,
      IsClassTeacher: 1,
      Query: searchQuery,
    }
    // 🆕 根据是否有自定义参数决定最终参数和API
    let response = null
    if (props.customParams) {
      // 有自定义参数：合并自定义参数，但保留基础参数的优先级
      let subjectIds =
        EnableMustSameSubjectTeacherCourse.value ?
          (props.EnableSubject ?
            (props.customParams.SubjectIDList && props.customParams.SubjectIDList[0] === EMPTYGUID ?
              [] : props.customParams.SubjectIDList) : (shiftSubjectId && shiftSubjectId != EMPTYGUID ? [shiftSubjectId] : []))
          : []
      const params = {
        ...baseParams,
        ...props.customParams,
        SubjectIDList: subjectIds,
        IsIncludeStatus: 0 // 🔑 第一步不查忙闲
      }
      // 删除不需要的参数
      delete params.PlanList // 第一步不传 PlanList
      response = await GetEmployeeAvailabilityStatusByPlan(params)
    } else {
      let subjectIds =
        EnableMustSameSubjectTeacherCourse.value ?
          (props.EnableSubject ?
            (props.SubjectIDList && props.SubjectIDList[0] === EMPTYGUID ?
              [] : props.SubjectIDList) : (shiftSubjectId && shiftSubjectId != EMPTYGUID ? [shiftSubjectId] : []))
          : []
      const params = {
        ...baseParams,
        IsIncludeStatus: 0, // 🔑 第一步不查忙闲
        SubjectIDList: subjectIds || undefined
      }
      // 无自定义参数：直接使用基础参数
      response = await getEmployeeAvailabilityStatus(params)
    }

    if (response && response.IsSuccess && response.Data && response.Data.List) {
      const list = response.Data.List.map(item => ({
        id: item.ID,
        name: item.Name,
        nickname: item.Serial || '',
        department: item.Department || '',
        avatar: '',
        // 🔑 如果有时间参数，不设置初始状态，等第二步查询；否则使用 API 返回的状态
        status: hasTimeProps.value ? '' : (item.Status === 1 ? '闲' : '忙'),
        BusyStatusLoading: hasTimeProps.value // 如果需要查询忙闲，标记为加载中
      }))
      
      const total = response.Data.TotalCount || 0
      
      // 合并数据（加载更多时追加）
      if (isLoadMore) {
        teacherList.value = [...teacherList.value, ...list]
      } else {
        teacherList.value = list
      }
      
      totalCount.value = total
      hasMore.value = teacherList.value.length < total
      
      // 🆕 第二步：如果有时间参数，异步查询忙闲状态
      if (hasTimeProps.value && list.length > 0) {
        fetchBusyStatus(list.map(item => item.id))
      }
    } else {
      if (!isLoadMore) {
        teacherList.value = []
      }
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取任课老师数据失败:', error)
    if (!isLoadMore) {
      teacherList.value = []
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

// 🆕 第二步：异步批量查询教师忙闲状态（传时间参数和IDList）
const fetchBusyStatus = async (teacherIds) => {
  if (!teacherIds || teacherIds.length === 0) {
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
    busyStatusQueue.value.push(...teacherIds)
    return
  }
  
  busyStatusFetching.value = true
  
  // 为当前批次的教师设置加载状态
  teacherList.value.forEach(item => {
    if (teacherIds.includes(item.id)) {
      item.BusyStatusLoading = true
    }
  })
  
  try {
    let shiftSubjectId = props.shiftSubjectId || ''
    
    let response = null
    if (props.customParams) {
      let subjectIds =
        EnableMustSameSubjectTeacherCourse.value ?
          (props.EnableSubject ?
            (props.customParams.SubjectIDList && props.customParams.SubjectIDList[0] === EMPTYGUID ?
              [] : props.customParams.SubjectIDList) : (shiftSubjectId && shiftSubjectId != EMPTYGUID ? [shiftSubjectId] : []))
          : []
      const params = {
        ...props.customParams,
        // 🔑 强制覆盖 IDList 和 IsClassTeacher，保留其他业务参数
        IDList: teacherIds,
        IsClassTeacher: 1,
        SubjectIDList: subjectIds,
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
      let subjectIds =
        EnableMustSameSubjectTeacherCourse.value ?
          (props.EnableSubject ?
            (props.SubjectIDList && props.SubjectIDList[0] === EMPTYGUID ?
              [] : props.SubjectIDList) : (shiftSubjectId && shiftSubjectId != EMPTYGUID ? [shiftSubjectId] : []))
          : []
      const params = {
        IDList: teacherIds,
        IsClassTeacher: 1,
        SubjectIDList: subjectIds || undefined,
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
      
      // 更新教师的忙闲状态
      teacherList.value.forEach(item => {
        if (teacherIds.includes(item.id)) {
          item.BusyStatusLoading = false
          if (busyStatusMap[item.id] !== undefined) {
            item.status = busyStatusMap[item.id]
          }
          // 🔑 如果后端没返回该教师的数据，也保持空字符串
          else {
            item.status = ''
          }
        }
      })
    } else {
      // 清除加载状态
      teacherList.value.forEach(item => {
        if (teacherIds.includes(item.id)) {
          item.BusyStatusLoading = false
        }
      })
    }
  } catch (error) {
    console.error('获取忙闲状态出错:', error)
    // 清除加载状态
    teacherList.value.forEach(item => {
      if (teacherIds.includes(item.id)) {
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
  
  currentPage.value++
  fetchTeacherList(searchKeyword.value || '', true) // 加载更多
}

// 防抖搜索函数
const debouncedSearch = (value) => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }

  // 设置新的定时器
  searchTimer = setTimeout(() => {
    // 限制跨科且未选课程时，不发起搜索
    if (EnableMustSameSubjectTeacherCourse.value && !props.ShiftID) {
      teacherList.value = []
      return
    }
    // ✅ 修复：清空搜索框时也应该重新调用接口获取完整列表
    // 重置分页
    currentPage.value = 1
    hasMore.value = true
    if (value && value.trim()) {
      fetchTeacherList(value, false)
    } else {
      fetchTeacherList('', false) // 搜索框为空时，获取完整列表
    }
  }, DEBOUNCE_DELAY)
}

const handleChange = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  // 🆕 检查是否在阻止列表中
  if (props.disabledIds && props.disabledIds.includes(value)) {
    // 获取教师名称用于提示
    const selectedTeacher = getSelectedTeacher(value)
    const teacherName = selectedTeacher?.name || '该教师'
    ElMessageBox.alert(
      `"${teacherName}"已被设置为助教，不能继续设为${transToConfigDescript('任课老师')}。`,
      '提示',
      {
        confirmButtonText: '知道了',
        type: 'warning'
      }
    )
    // 恢复原值
    selectedValue.value = props.modelValue
    return
  }

  const selectedTeacher = getSelectedTeacher(value)
  emit('update:modelValue', value) // 传递ID字符串
  emit('change', value, selectedTeacher)
}

const handleClear = () => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  emit('update:modelValue', '')
  emit('clear')
}

const handleSearch = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  searchKeyword.value = value
  // 使用防抖处理搜索
  debouncedSearch(value)
  emit('search', value, filteredTeacherList.value)
}
const EnableClassCommission = computed(() => {
  return configs.value.EnableClassCommission == 1
})

// 🚀 优化：懒加载标记
const hasLoadedData = ref(false)
const isMounted = ref(false)
// 🆕 用于存储上次的 customParams 和时间参数，用于比较变化
const lastCustomParams = ref(null)
const lastTimeParams = ref({ startTime: '', endTime: '' })

const visibleChange = (show) => {
  // 🆕 先emit给父组件，让父组件的 UI 锁逻辑能够执行
  emit('visible-change', show)
  
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  if (show) {
    // 🆕 检查 customParams 是否发生变化
    const currentCustomParams = props.customParams
    const customParamsChanged = JSON.stringify(currentCustomParams) !== JSON.stringify(lastCustomParams.value)
    
    // 🆕 检查时间参数是否发生变化
    const currentTimeParams = { startTime: props.startTime, endTime: props.endTime }
    const timeParamsChanged = (
      lastTimeParams.value.startTime !== currentTimeParams.startTime ||
      lastTimeParams.value.endTime !== currentTimeParams.endTime
    )
    
    // 任何参数变化都强制刷新
    if (customParamsChanged || timeParamsChanged) {
      hasLoadedData.value = false
      lastCustomParams.value = currentCustomParams ? JSON.parse(JSON.stringify(currentCustomParams)) : null
      lastTimeParams.value = { ...currentTimeParams }
    }

    // 下拉框打开时才加载数据（懒加载优化），或者参数变化时强制刷新
    if (!hasLoadedData.value || customParamsChanged || timeParamsChanged) {
      if (EnableMustSameSubjectTeacherCourse.value && !props.ShiftID) {
        teacherList.value = []
      } else {
        currentPage.value = 1
        hasMore.value = true
        fetchTeacherList('', false)
      }
      hasLoadedData.value = true
    }

    if (EnableClassCommission.value) {
      handleAllPersonnel();
    }
  }
}
const getTeacherDesc = (item) => {
  return item.department || ''
}

const getSelectedTeacher = (value) => {
  return teacherList.value.find(teacher => teacher.id === value) || null
}

const goToSettings = () => {
  window.open('/back_end/#/empManage', '_blank')
  emit('goToSettings')
}

const Check_Shift_Teacher_Subject = computed(() => props.Check_Shift_Teacher_Subject) //排课时的课程科目与老师可授课科目不一致时，是否显示提示  业务规则
const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
// 处理全部人员选择
const handleAllPersonnel = () => {
  let teacherCondition={
		StatusList:[1],
		IsClassTeacherList: [1]
	}
  let shiftSubjectId = props.shiftSubjectId || ''
  let subjectId= EnableMustSameSubjectTeacherCourse.value ? (props.EnableSubject ? props.SubjectIDList[0] : shiftSubjectId) : ''
  let disabledCondition=['StatusList']
  if(EnableMustSameSubjectTeacherCourse.value){
      disabledCondition.push('SubjectIDList')
  }
	if(EnbaleEmpIsClassTeacher.value==1){
    disabledCondition.push('IsClassTeacherList')
  }
	if(subjectId&&subjectId!=EMPTYGUID){
		teacherCondition.SubjectIDList=[subjectId]
	}
  // 🆕 UI交互锁：弹框打开时通知父组件
  emit('dialogOpen')
  
  chooseEmpTableRef.value?.open({
    popTitle: '选择任课老师',
    multi: false, // 单选
    showTeacherType: true,
    disabledCondition:disabledCondition,
		condition:teacherCondition,
  }).then(async (result) => {
    const teacherCategory = result.other;
    if (result && result.data) {
      // ✅ 检查科目是否一致，如果不一致则需要用户确认
      if (Check_Shift_Teacher_Subject.value && shiftSubjectId !== '' && shiftSubjectId != EMPTYGUID && result.data.SubjectList.length) {
        let teacherSubjects = result.data.SubjectList.map((i) => i.ID).join(',')
        if (teacherSubjects.indexOf(shiftSubjectId) === -1) {
          try {
            // 等待用户确认，如果点击"取消"会抛出异常
            await ElMessageBox.confirm(transToConfigDescript('任课老师和课程的科目不一致，是否继续？'), '提示', {
              confirmButtonText: '继续',
              cancelButtonText: '取消',
            })
            // 用户点击"继续"，往下执行
          } catch (error) {
            // 用户点击"取消"，中断流程
            console.log('用户取消了科目不一致的确认')
            return
          }
        }
      }
      const selectedTeacher = result.data

      // 🆕 检查是否在阻止列表中
      if (props.disabledIds && props.disabledIds.includes(selectedTeacher.ID)) {
        const teacherName = selectedTeacher.Name || '该教师'
        ElMessageBox.alert(
          `"${teacherName}"已被设置为助教，不能继续设为${transToConfigDescript('任课老师')}。`,
          '提示',
          {
            confirmButtonText: '知道了',
            type: 'warning'
          }
        )
        return
      }

      // 将选中的员工数据转换为组件需要的格式
      const teacherData = {
        id: selectedTeacher.ID,
        TeacherCommissionList: teacherCategory.ID ? [teacherCategory] : [],
        name: selectedTeacher.Name + (teacherCategory && teacherCategory.Name ? `（${teacherCategory.Name}）` : ''),
        nickname: selectedTeacher.Serial || '',
        department: selectedTeacher.DepartName || '',
        avatar: '',
        status: '闲' // 默认状态
      }

      // 更新选中值 - 传递ID字符串而不是对象
      selectedValue.value = teacherData.id
      emit('update:modelValue', teacherData.id) // 传递ID字符串
      emit('change', teacherData.id, teacherData)

      // 将选中的员工添加到列表中（如果不存在）
      const existingIndex = teacherList.value.findIndex(item => item.id === teacherData.id)
      if (existingIndex === -1) {
        teacherList.value.unshift(teacherData)
      }
      
      // 🆕 UI交互锁：弹框关闭时通知父组件
      emit('dialogClose')
      console.log('🔓 [TeacherSelect] emit dialogClose')
    }
  }).catch((error) => {
    console.error('选择任课老师失败:', error)
    // 🆕 UI交互锁：即使失败也要释放锁
    emit('dialogClose')
    console.log('🔓 [TeacherSelect] emit dialogClose (error)')
  })
}

watch(() => props.modelValue, (newValue) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  if (newValue) {
    if (typeof newValue === 'object' && newValue.id) {
      selectedValue.value = newValue.id
    } else {
      selectedValue.value = newValue
    }
  } else {
    // 若开启限制且无课程，不主动清空已有回显，保持选中值，避免点击进入编辑导致清空
    if (!(EnableMustSameSubjectTeacherCourse.value && !props.ShiftID)) {
      selectedValue.value = ''
    }
  }
}, { immediate: true })

watch(teacherList, (newList) => {
  // 不在这里清空，改为在成功拉取列表后、且选择了课程时进行校验清空
  return
}, { deep: true })

// 监听课程ID变化：在限制开启下，ShiftID 有值时拉取老师；无值时不请求但保留已选
watch(() => props.ShiftID, (newShiftID) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  if (!EnableMustSameSubjectTeacherCourse.value) {
    // 未开启限制则忽略此逻辑
    return
  }
  if (newShiftID) {
    // 课程已选择，重置分页并拉取教师列表
    currentPage.value = 1
    hasMore.value = true
    fetchTeacherList(searchKeyword.value || '', false)
  } else {
    // 未选择课程：不清空已有选中，仅停止列表并展示空
    teacherList.value = []
    currentPage.value = 1
    hasMore.value = true
  }
}, { immediate: false })

onMounted(() => {
  isMounted.value = true
  console.log('✅ TeacherSelect 组件已挂载')
  // 🆕 初始化 lastCustomParams 和 lastTimeParams
  lastCustomParams.value = props.customParams ? JSON.parse(JSON.stringify(props.customParams)) : null
  lastTimeParams.value = { startTime: props.startTime, endTime: props.endTime }

  // 🚀 优化：完全懒加载，即使有初始值也不立即加载
  // 通过 initialData 属性即可回显，无需立即请求列表
  // 只在下拉框展开时才请求，减少虚拟滚动时的重复请求和性能开销
})

// 组件卸载时清理定时器
onUnmounted(() => {
  isMounted.value = false
  lastCustomParams.value = null
  lastTimeParams.value = { startTime: '', endTime: '' }
  console.log('❌ TeacherSelect 组件销毁')

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
    currentPage.value = 1
    hasMore.value = true
    fetchTeacherList('', false)
    emit('search', '', teacherList.value)
  },
  refresh: () => {
    currentPage.value = 1
    hasMore.value = true
    fetchTeacherList(searchKeyword.value, false)
  }
})
</script>

<style scoped>
.teacher-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.teacher-avatar {
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

.teacher-name {
  font-weight: 400;
  font-size: 14px;
  color: #606266;
  padding-right: 8px;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-status {
  position: absolute;
  right: 14px;
}

.teacher-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #909399;
}

.teacher-empty .wtwo-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.teacher-empty-tip {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  line-height: 1.4;
}

.teacher-label {
  color: #409eff;
  font-weight: 500;
  margin: 0 4px;
}

.teacher-status-tip {
  padding: 8px 12px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: default;
}

.teacher-status-tip .status-icon {
  flex-shrink: 0;
  color: #C0C4CC;
  font-size: 14px;
}

.teacher-status-tip .status-text {
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
