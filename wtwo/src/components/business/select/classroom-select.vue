<template>
  <BaseSelect v-model="selectedValue" :options="filteredClassroomList" :initial-data="props.initialData"
    :placeholder="dynamicPlaceholder" :clearable="clearable" :disabled="disabled" :select-style="selectStyle"
    :show-search="!!campusId" :search-placeholder="'搜索教室'" :filterable="true" :loading="loading"
    :cell-mode="cellMode && !!campusId" :show-attention-tips="showBusyStatus" :attention-tips="'基于排课时间计算忙闲'"
    value-key="ID" label-key="Name" :get-option-desc="getClassroomDesc" empty-text="暂无匹配的教室" @change="handleChange"
    @clear="handleClear" @search="handleSearch" @visible-change="handleVisibleChange" @scroll-bottom="handleScrollBottom">
    <template #option="{ item }">
      <div class="classroom-option">
        <el-icon size="20px">
          <svg aria-hidden="true">
            <use xlink:href="#w2-jiaoshi"></use>
          </svg>
        </el-icon>
        <span class="classroom-name" :title="item.Name">{{ item.Name || '-' }}</span>
        <span class="classroom-capacity" v-if="item.PersonCount">可容纳 {{ item.PersonCount }} 人</span>
        <span class="classroom-capacity" v-else>不限人数</span>
        <span v-if="showBusyStatus" :class="['option-status', item.status === '闲' ? 'status-free' : item.status === '忙' ? 'status-busy' : 'status-loading']">
          {{ item.status || '...' }}
        </span>
      </div>
    </template>
    <template #empty>
      <div class="classroom-empty">
        <el-empty :image="globalData.emptyPng2" :image-size="80" style="padding: 0px 0px;" :description="loading ? '加载中...' :
          ((!campusId ? transToConfigDescript('请先选“上课校区”') : (searchKeyword ? '暂无匹配的教室' : '暂无授权教室')))" />
        <div v-if="campusId && !hasClassroomData && !loading && !searchKeyword" class="classroom-empty-tip">
          请前往 <span class="time-empty-tip-link">"管理后台-教室管理"</span> 为校区设置授权教室
          <br>
          <el-button type="primary" link size="small" @click="goToSettings">
            去设置
          </el-button>
        </div>
      </div>
    </template>
    <template #footer>
      <!-- 加载提示 - 统一容器 -->
      <transition name="fade-slide">
        <div v-if="loadMoreLoading || (!hasMore && classroomList.length > 0)" class="classroom-status-tip">
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
    </template>
  </BaseSelect>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
import { GetClassRoomAvailabilityStatus, GetClassRoomAvailabilityStatusByPlan } from '@/api/arrange'
import { transToConfigDescript } from '@/utils/filters/filters'
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  placeholder: {
    type: String,
    default: '请选择教室'
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
  campusId: {
    type: [String, Number],
    default: ''
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
  courseType: { // 课程类型：'2' 线上, '1'或其他 线下
    type: [String, Number],
    default: '1'
  },
  lazy: { // 是否懒加载：true 打开面板时才加载，false 组件挂载时就加载
    type: Boolean,
    default: true
  },
  customParams: { // 自定义参数，预留扩展
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'clear',
  'search',
  'goToSettings',
  'visible-change'  // 🆕 暴露 visible-change 事件，让父组件能够监听
])

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const loadMoreLoading = ref(false) // 分页加载更多时的 loading
const classroomList = ref([])
const classroomCache = new Map() // 缓存教室数据（按 campusId + 搜索关键词 作为 key）
const busyStatusCache = new Map() // 缓存忙闲状态数据

// 🆕 分页相关状态
const currentPage = ref(1)
const pageSize = ref(10) // 每页10条
const hasMore = ref(true) // 是否还有更多数据
const totalCount = ref(0) // 总数据量

// 🚀 优化：懒加载标记
const hasLoadedData = ref(false)
const isMounted = ref(false)
// 🆕 用于存储上次的 customParams 和时间参数，用于比较变化
const lastCustomParams = ref(null)
const lastTimeParams = ref({ startTime: '', endTime: '' })

// 🆕 忙闲状态查询队列（防止重复查询）
const busyStatusFetching = ref(false)
const busyStatusQueue = ref([]) // 待查询的教室ID队列

const filteredClassroomList = computed(() => {
  return classroomList.value
})

const dynamicPlaceholder = computed(() => {
  return props.placeholder
})

const hasClassroomData = computed(() => {
  return classroomList.value && classroomList.value.length > 0
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

// 根据是否有时间参数来决定是否显示忙闲状态
const showBusyStatus = computed(() => {
  return hasValidTimeParams()
})

const handleChange = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  emit('update:modelValue', value)
  const selectedObject = getSelectedClassroom(value)
  emit('change', value, selectedObject)
}

const handleClear = () => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  emit('update:modelValue', '')
  // 触发 change 让父组件同步清空名称
  emit('change', '', null)
  emit('clear')
}

const handleSearch = (value) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  searchKeyword.value = value
  emit('search', value, filteredClassroomList.value)
}

const getClassroomDesc = (item) => {
  return `可容纳 ${item.PersonCount} 人`
}

const getSelectedClassroom = (value) => {
  return classroomList.value.find(classroom => classroom.ID === value) || null
}

const goToSettings = () => {
  window.open('/back_end/#/classroomManage', '_blank')
  emit('goToSettings')
}

// 🚀 优化：下拉框显示/隐藏回调
const handleVisibleChange = (visible) => {
  // 🚀 组件销毁时不处理任何逻辑
  if (!isMounted.value) {
    return
  }

  // 🆕 emit 给父组件，让父组件的 UI 锁逻辑能够执行
  emit('visible-change', visible)

  if (visible) {
    // 🆕 检查 customParams 是否发生变化
    const currentCustomParams = props.customParams
    const customParamsChanged = JSON.stringify(currentCustomParams) !== JSON.stringify(lastCustomParams.value)
    
    // 🆕 检查时间参数是否发生变化
    const currentTimeParams = { startTime: props.startTime, endTime: props.endTime }
    const timeParamsChanged = (
      lastTimeParams.value.startTime !== currentTimeParams.startTime ||
      lastTimeParams.value.endTime !== currentTimeParams.endTime
    )
    
    // 任何参数变化都清空所有缓存并强制刷新
    if (customParamsChanged || timeParamsChanged) {
      classroomCache.clear()
      busyStatusCache.clear()
      hasLoadedData.value = false
      lastCustomParams.value = currentCustomParams ? JSON.parse(JSON.stringify(currentCustomParams)) : null
      lastTimeParams.value = { ...currentTimeParams }
    }

    // 懒加载模式：下拉框打开时才加载数据，或者参数变化时强制刷新
    if (props.lazy && (!hasLoadedData.value || customParamsChanged || timeParamsChanged) && props.campusId) {
      currentPage.value = 1
      hasMore.value = true
      fetchClassroomList(false) // 重新查询第一页
      hasLoadedData.value = true
    }
  }
}

// 🆕 第一步：分页获取教室列表（不传时间参数，不含忙闲状态）
const fetchClassroomList = async (isLoadMore = false) => {
  if (!props.campusId) {
    classroomList.value = []
    hasMore.value = false
    return
  }

  // 如果是加载更多且没有更多数据，直接返回
  if (isLoadMore && !hasMore.value) {
    return
  }

  //如果是新查询（非加载更多），重置页码和列表
  if (!isLoadMore) {
    currentPage.value = 1
    classroomList.value = []
    hasMore.value = true
    loading.value = true
  } else {
    loadMoreLoading.value = true
  }

  // 检查缓存：按照 campusId + 搜索关键词 + 页码 作为 key
  const cacheKey = `${props.campusId}__${searchKeyword.value || ''}__${currentPage.value}`
  if (classroomCache.has(cacheKey)) {
    const cachedData = classroomCache.get(cacheKey)
    
    // 🔑 深拷贝缓存数据，避免污染原缓存
    const list = cachedData.list.map(item => ({ ...item }))
    
    // 🔑 如果需要查询忙闲状态，标记为加载中
    if (showBusyStatus.value) {
      list.forEach(item => {
        item.status = '' // 清空初始状态
        item.BusyStatusLoading = true // 标记为加载中
      })
    }
    
    if (isLoadMore) {
      classroomList.value = [...classroomList.value, ...list]
    } else {
      classroomList.value = list
    }
    hasMore.value = cachedData.hasMore
    totalCount.value = cachedData.total
    
    // 如果需要查询忙闲状态，异步查询
    if (showBusyStatus.value && list.length > 0) {
      fetchBusyStatus(list.map(item => item.ID))
    }
    
    // 缓存命中，结束 loading
    if (isLoadMore) {
      loadMoreLoading.value = false
    } else {
      loading.value = false
    }
    return
  }

  try {
    // 🔑 第一步：获取教室列表（不查忙闲）
    const params = {
      CampusID: props.campusId,
      PageIndex: currentPage.value,
      PageSize: pageSize.value,
      Desc: 0,
      Sort: 'Name',
      Query: searchKeyword.value || '',
      IsIncludeStatus: 0, // 🔑 第一步不查忙闲
      // 如果有 customParams，合并进来
      ...(props.customParams || {})
    }
    
    // 🔑 根据是否有 customParams 选择调用哪个接口
    const response = props.customParams 
      ? await GetClassRoomAvailabilityStatusByPlan(params)
      : await GetClassRoomAvailabilityStatus(params)

    if (response.ErrorCode === 200 && response.Data) {
      const list = response.Data.List || []
      const total = response.Data.TotalCount || 0
      
      // 🔑 如果需要查询忙闲状态，先清空 status 字段并标记加载中
      if (showBusyStatus.value) {
        list.forEach(item => {
          item.status = '' // 清空初始状态，避免显示错误的忙闲
          item.BusyStatusLoading = true // 标记为加载中
        })
      }
      
      // 合并数据（加载更多时追加）
      if (isLoadMore) {
        classroomList.value = [...classroomList.value, ...list]
      } else {
        classroomList.value = list
      }
      
      totalCount.value = total
      hasMore.value = classroomList.value.length < total
      
      // 缓存结果
      classroomCache.set(cacheKey, {
        list,
        hasMore: hasMore.value,
        total
      })
      
      // 🆕 第二步：如果有时间参数，异步查询忙闲状态
      if (showBusyStatus.value && list.length > 0) {
        fetchBusyStatus(list.map(item => item.ID))
      }
    } else {
      console.error('获取教室列表失败:', response.ErrorMsg)
      if (!isLoadMore) {
        ElMessage.error(response.ErrorMsg || '获取教室列表失败')
      }
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取教室列表出错:', error)
    hasMore.value = false
  } finally {
    if (isLoadMore) {
      loadMoreLoading.value = false
    } else {
      loading.value = false
    }
  }
}

// 🆕 第二步：异步批量查询教室忙闲状态（传时间参数和IDList）
const fetchBusyStatus = async (classroomIds) => {
  if (!classroomIds || classroomIds.length === 0) {
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
  
  // 检查缓存
  const statusCacheKey = `${props.startTime}__${props.endTime}__${classroomIds.sort().join(',')}`
  if (busyStatusCache.has(statusCacheKey)) {
    const busyStatusMap = busyStatusCache.get(statusCacheKey)
    classroomList.value.forEach(item => {
      if (classroomIds.includes(item.ID) && busyStatusMap[item.ID] !== undefined) {
        item.status = busyStatusMap[item.ID]
        item.BusyStatusLoading = false
      }
    })
    
    return
  }
  
  // 防止重复查询
  if (busyStatusFetching.value) {
    busyStatusQueue.value.push(...classroomIds)
    return
  }
  
  busyStatusFetching.value = true
  
  // 为当前批次的教室设置加载状态
  classroomList.value.forEach(item => {
    if (classroomIds.includes(item.ID)) {
      item.BusyStatusLoading = true
    }
  })
  
  try {
    // 🔑 第二步：传 IDList 和时间参数，获取忙闲状态
    let response = null
    
    if (props.customParams) {
      // 场景1：按计划查询，使用 GetClassRoomAvailabilityStatusByPlan 接口
      const params = {
        ...props.customParams,
        // 🔑 强制覆盖这些关键参数，保留 customParams 中的其他业务参数
        CampusID: props.campusId,
        IDList: classroomIds,
        IsIncludeStatus: 1 // 🔑 第二步查忙闲
      }
      
      // 🔑 时间参数优先级：props.startTime/endTime > customParams 中的时间参数
      if (props.startTime && props.endTime) {
        params.StartTime = props.startTime
        params.EndTime = props.endTime
      }
      // 如果 props 没有时间参数，customParams 中的时间参数会保留（已经通过 ...props.customParams 展开）
      
      response = await GetClassRoomAvailabilityStatusByPlan(params)
    } else {
      // 场景2：普通查询，使用 GetClassRoomAvailabilityStatus 接口
      const params = {
        CampusID: props.campusId,
        IDList: classroomIds,
        IsIncludeStatus: 1 // 🔑 第二步查忙闲
      }
      
      // 🔑 时间参数：只有在 props 中有值时才设置
      if (props.startTime && props.endTime) {
        params.StartTime = props.startTime
        params.EndTime = props.endTime
      }
      
      response = await GetClassRoomAvailabilityStatus(params)
    }
    
    if (response.ErrorCode === 200 && response.Data) {
      const busyStatusMap = {}
      const list = response.Data.List || response.Data || []
      
      if (Array.isArray(list)) {
        list.forEach(item => {
          // 🔑 只有当 Status 字段存在时才设置状态，否则保持空字符串
          if (item.Status !== undefined && item.Status !== null) {
            busyStatusMap[item.ID] = item.Status === 1 ? '闲' : '忙'
          } else {
            busyStatusMap[item.ID] = '' // 后端没返回状态，保持空字符串
          }
        })
      }
      
      // 更新教室的忙闲状态
      classroomList.value.forEach(item => {
        if (classroomIds.includes(item.ID)) {
          item.BusyStatusLoading = false
          if (busyStatusMap[item.ID] !== undefined) {
            item.status = busyStatusMap[item.ID]
          }
          // 🔑 如果后端没返回该教室的数据，也保持空字符串
          else {
            item.status = ''
          }
        }
      })
      
      // 缓存忙闲状态
      busyStatusCache.set(statusCacheKey, busyStatusMap)
    } else {
      console.error('获取忙闲状态失败:', response.ErrorMsg)
      // 清除加载状态
      classroomList.value.forEach(item => {
        if (classroomIds.includes(item.ID)) {
          item.BusyStatusLoading = false
        }
      })
    }
  } catch (error) {
    console.error('获取忙闲状态出错:', error)
    // 清除加载状态
    classroomList.value.forEach(item => {
      if (classroomIds.includes(item.ID)) {
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
  fetchClassroomList(true) // 加载更多
}

watch(() => props.modelValue, (newValue) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  selectedValue.value = newValue
}, { immediate: true })

watch(() => props.campusId, (newCampusId, oldCampusId) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  // 🚀 优化：校区变化时重置加载标记和分页，清空列表
  if (newCampusId !== oldCampusId) {
    hasLoadedData.value = false
    currentPage.value = 1
    hasMore.value = true
    classroomList.value = []
  }
}, { immediate: false })

watch(searchKeyword, (newKeyword, oldKeyword) => {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  // 搜索关键词变化时，重置分页并重新查询
  if (newKeyword !== oldKeyword && props.campusId) {
    currentPage.value = 1
    hasMore.value = true
    fetchClassroomList(false)
  }
}, { debounce: 300 })

onMounted(() => {
  isMounted.value = true
  console.log('✅ ClassroomSelect 组件已挂载')
  // 🆕 初始化 lastCustomParams 和 lastTimeParams
  lastCustomParams.value = props.customParams ? JSON.parse(JSON.stringify(props.customParams)) : null
  lastTimeParams.value = { startTime: props.startTime, endTime: props.endTime }
  // 🚀 根据 lazy 参数决定加载时机
  if (!props.lazy && props.campusId) {
    // 非懒加载模式：组件挂载时立即请求第一页数据
    currentPage.value = 1
    hasMore.value = true
    fetchClassroomList(false)
    hasLoadedData.value = true
  }
  // 懒加载模式：只在下拉框展开时才请求，减少虚拟滚动时的重复请求和性能开销
  // 即使有初始值，也通过 initialData 属性回显，无需立即请求列表
})

onUnmounted(() => {
  isMounted.value = false
  lastCustomParams.value = null
  lastTimeParams.value = { startTime: '', endTime: '' }
  console.log('❌ ClassroomSelect 组件销毁')
})

defineExpose({
  search: handleSearch,
  clearSearch: () => {
    searchKeyword.value = ''
    emit('search', '', classroomList.value)
  }
})
</script>

<style lang="scss" scoped>
.classroom-option {
  display: flex;
  justify-content: left;
  align-items: center;
  width: 100%;
}

.classroom-name {
  font-weight: 500;
  margin-left: 8px;
  color: #303133;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-status {
  position: absolute;
  right: 14px;
}

.classroom-capacity {
  font-size: 12px;
  margin-left: 8px;
  color: #909399;
  padding-right: 8px;
  font-weight: 400;
}

.classroom-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #909399;
}

.classroom-empty .wtwo-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.classroom-empty-tip {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  line-height: 1.4;
}

.classroom-status-tip {
  padding: 8px 12px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: default;
}

.classroom-status-tip .status-icon {
  flex-shrink: 0;
  color: #C0C4CC;
  font-size: 14px;
}

.classroom-status-tip .status-text {
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
