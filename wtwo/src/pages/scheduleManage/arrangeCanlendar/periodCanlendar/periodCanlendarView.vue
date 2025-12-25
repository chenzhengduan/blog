<!-- 暂未使用 -->
<template>
  <div class="w100% h100% flex canlendar-container" v-loading="loading" element-loading-target=".canlendar-container">
    <div v-if="showObjectColumn" class="object-column-container">
      <!-- 左上角：对象列列头（固定"对象"） -->
      <div class="corner sticky object-corner" v-if="showObjectColumn">{{ transToConfigDescript(objectTypeName) }}</div>
      <!-- 对象列 -->
      <div class="sidebar object-column" ref="objectColumnRef" v-if="showObjectColumn">
        <!-- 其他课表显示对象列表 -->
        <div class="object-list">
          <div 
            v-for="(obj, index) in objectList" 
            :key="obj.id || index"
            class="object-item"
            :class="{ active: selectedObjectId === obj.id }"
            @click="selectObject(obj.id)"
          >
            <div class="avatar" v-if="obj.avatar && shouldShowAvatar">
              <img :src="obj.avatar" alt="" />
            </div>
            <div class="avatar placeholder" v-else-if="shouldShowAvatar">
              {{ getNameInitial(obj.name) }}
            </div>
            <div class="object-info" :class="{'width-avatar':shouldShowAvatar}">
              <div class="object-name ellipsis-single" :title="obj.name">{{ obj.name }}</div>
              <div class="flex-center mt-3px">
                <div class="teacher-course-num">{{ getObjectCourseCount(obj.id) }}条</div>
                <div v-if="props.timetableType == CourseTimetableTypeEnum.StudentTimetable" class="more-btn ml-4px mt-1px" @click.stop="openStudentPopover($event, obj)">
                    <el-icon class="default-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-xueyuanxinxikapianmoren"></use></svg></el-icon>
                    <el-icon class="active-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-gengduohover"></use></svg></el-icon>
                    <el-icon class="current-active-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-xueyuanxinxikapianxuanzhong"></use></svg></el-icon>
                </div>
              </div>
            </div>
            
          </div>
        </div>
      </div>
    </div>
    <div class="period-calendar-view">
      <div class="calendar-container" :class="gridLayoutClass" :key="viewKey">
      
      <!-- 时段列列头 -->
      <div class="corner sticky period-corner">时段</div>
      <!-- 顶部一周日期头 -->
      <div class="header sticky">
        <div v-for="day in weekDays" :key="day.key" class="header-cell" :class="{ today: day.isToday }">
          <div class="header-week">{{ day.weekText }}</div>
          <div class="header-date" :class="{ circle: day.isToday }">{{ day.isToday ? '今' : day.dateText }}</div>
          <span v-if="day.isHoliday" class="holiday-flag">假</span>
        </div>
      </div>

      

      <!-- 时段列 -->
      <div class="sidebar period-column" ref="periodColumnRef" v-if="!(timetableType === CourseTimetableTypeEnum.StudentTimetable && (!assignList || assignList.length === 0))">
        <div v-for="(p, idx) in periods" :key="p.key" class="period-cell">
          <div class="period-name">{{ p.name }}</div>
        </div>
      </div>

      <!-- 内容区：周 × 时段 -->
      <div class="grid scrollable" ref="gridRef">
        <!-- 学员视图且assignList为空时显示空状态 -->
        <div 
          v-if="timetableType === CourseTimetableTypeEnum.StudentTimetable && (!assignList || assignList.length === 0)"
          class="empty-state"
        >
            <el-empty :image="globalData.emptyPng" :image-size="100">
              <template #description>
                  <div class="color-[#666]">仅支持选择“报读了1对1{{transToConfigDescript('课程')}}”的学员，请在左上角“<span class="color-[#E6A23C]">显示设置</span>”中选择。</div>
              </template>
            </el-empty>
        </div>
        <div v-else v-for="(p, idx) in periods" :key="p.key" class="grid-row" :ref="el => setGridRowRef(el, idx)">
          <div v-for="day in weekDays" :key="day.key" class="grid-cell" :class="{ 'is-past': isPastDay(day.key) }">
            <template v-for="(item, itemIdx) in getCellItems(day.key, p.key)">
              <!-- 排课卡片 -->
              <div 
                v-if="item.type === 'course'"
                :key="`course-${itemIdx}`" 
                class="wtwo-schedule-course-card"
                :style="{ borderTopColor: getStatusColor(item), backgroundColor: getTint(getStatusColor(item), 0.1) }"
                :ref="el => setCourseItemRef(el, item)"
                v-click-outside="(event: Event) => handleClickOutside(event, item)"
                @click="handleCourseClick($event, item)"
              >
                <div class="wtwo-schedule-course-time">{{ dayjs(item.StartTime).format('HH:mm') }}-{{ dayjs(item.EndTime).format('HH:mm') }}</div>
                <div class="wtwo-schedule-course-meta">
                  <div class="wtwo-schedule-meta-item ellipsis-single" v-for="field in enabledLeftFields" :key="String(field.FieldName)" :title="getFieldValue(item, String(field.FieldName))">
                    {{ getFieldValue(item, String(field.FieldName)) }}
                  </div>
                </div>
                <div class="wtwo-schedule-course-tags">
                  <div 
                    class="wtwo-schedule-course-tags-item ellipsis-single" 
                    v-for="tag in enabledRightTags" 
                    :key="String(tag.FieldName)" 
                    v-show="!!getTagText(item, String(tag.FieldName))"
                    :style="{ color: getTagColor(String(tag.FieldName)) }"
                  >
                    {{ getTagText(item, String(tag.FieldName)) }}
                  </div>
                </div>
              </div>
              
              <!-- 日程卡片 -->
              <div 
                v-if="item.type === 'schedule'"
                :key="`schedule-${itemIdx}`" 
                class="wtwo-schedule-schedule-card"
                :ref="el => setCourseItemRef(el, item)"
                v-click-outside="(event: Event) => handleClickOutside(event, item)"
                @click="handleCourseClick($event, item)"
              >
                <div class="fixed-type-label">日程</div>
                <div class="wtwo-schedule-schedule-time">{{ dayjs(item.StartTime).format('HH:mm') }}-{{ dayjs(item.EndTime).format('HH:mm') }}</div>
                <div class="wtwo-schedule-schedule-meta">
                  <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.Title">{{ item.Title }}</div>
                  <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.TeacherList.map((i:any)=>i.Name).join(',')">{{ item.TeacherList.map((i:any)=>i.Name).join(',') }}</div>
                  <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.CampusName">{{ item.CampusName }}</div>
                  <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.Content">{{ item.Content }}</div>
                  <div v-if="item.Remark" class="wtwo-schedule-meta-item ellipsis-single" :title="item.Remark">{{ item.Remark }}</div>
                </div>
              </div>
            </template>
            
            <div 
              class="double-click-add-course"
              @dblclick="handleDoubleClickAddCourse(day.key, p.key)"
            >
              双击新增排课
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 课程详情浮窗 -->
    <CourseDetailPopover
        ref="courseDetailPopoverRef"
        :course-data="selectedCourseData"
        :virtual-ref="virtualRef"
        :need-api="false"
        @roll-call="handleRollCall"
        :timetable-type="props.timetableType"
    />

    <!-- 学员详情浮窗 -->
    <StudentDetailPopover
        ref="studentPopoverRef"
        :student="selectedStudent"
        :virtual-ref="studentVirtualRef"
        :campus-id="selectedStudent?.CampusID || selectedStudent?.campusId"
    />

    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, onUnmounted, getCurrentInstance } from 'vue'
import { ClickOutside as vClickOutside } from 'element-plus'
import dayjs from 'dayjs'
import { queryHoliday } from '@/api/comm'
import { queryCourseNew, queryCalendarTeacher, queryCalendarClass, queryCalendarClassroom, queryCalendarStudent } from '@/api/arrange'
import { CourseTimetableTypeEnum, CourseTimetableFieldTypeEnum } from '@/types/model/timetable-preference'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
import { buildExportColumns, getTint, getCourseStatusColor, buildColorDetailMap, getEnabledRightTagsFromPreference, getEnabledLeftFieldsFromPreference, getCourseFieldValue, getCourseTagText, getCourseTagColor, getNameInitial } from '@/utils'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import CourseDetailPopover from '@/components/CourseDetailPopover.vue'
import StudentDetailPopover from '@/pages/scheduleManage/components/studentDetailPopover.vue'
import { transToConfigDescript } from '@/utils/filters/filters'

const props = defineProps<{ currentWeek?: Date; searchParams?: any; timetableType?: any; assignList?: any[]; objectName?: string }>()

const emit = defineEmits<{
  'canlendar-dbclick-add-course': [data: { object: any; date: string; period: string; timetableType: any }]
  'object-selected': [data: { objectId: string; timetableType: any }]
}>()

const loading = ref(false)
const switching = ref(false)
const viewKey = ref(0)
const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;

// 对象列表和选中状态
const objectList = ref<any[]>([])
const selectedObjectId = ref<string>('')


// 根据课表类型获取对应的对象名称
const objectTypeName = computed(() => {
  const map: Record<string, string> = {
    [CourseTimetableTypeEnum.TeacherTimetable]: '老师',
    [CourseTimetableTypeEnum.StudentTimetable]: '学员',
    [CourseTimetableTypeEnum.ClassTimetable]: '班级',
    [CourseTimetableTypeEnum.ClassroomTimetable]: '教室',
    [CourseTimetableTypeEnum.TimeTimetable]: '时间',
  }
  return map[props.timetableType] || '老师'
})

// 当前选中的对象
const selectedObject = computed(() => {
  return objectList.value.find(obj => obj.id === selectedObjectId.value) || objectList.value[0]
})

// 是否显示对象列
const showObjectColumn = computed(() => {
  // 时间课表不显示对象列
  if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
    return false
  }
  // 学员课表且assignList为空时不显示对象列
  if (props.timetableType === CourseTimetableTypeEnum.StudentTimetable && (!props.assignList || props.assignList.length === 0)) {
    return false
  }
  return true
})

// 动态grid布局类
const gridLayoutClass = computed(() => {
  // 学员课表且assignList为空时，显示时段列+内容区（两列布局）
  if (props.timetableType === CourseTimetableTypeEnum.StudentTimetable && (!props.assignList || props.assignList.length === 0)) {
    return 'two-columns'
  }
  return showObjectColumn.value ? 'three-columns' : 'two-columns'
})

// 同步滚动相关
const objectColumnRef = ref<HTMLElement>()
const periodColumnRef = ref<HTMLElement>()
const gridRef = ref<HTMLElement>()
let isScrolling = false

// 行高同步相关
const rowHeights = ref<number[]>([])
const gridRowRefs: Array<Element | null> = []
let ro: ResizeObserver | null = null
let isSyncing = false // 防止同步过程中的死循环

// 课程详情浮窗相关
const courseDetailPopoverRef = ref()
const selectedCourseData = ref<any>({})
const virtualRef = ref()
const courseItemRefs = new Map()

// 学员详情浮窗
const studentPopoverRef = ref()
const studentVirtualRef = ref()
const selectedStudent = ref<any>({})

// 偏好设置
const timetablePreferencesStore = useTimetablePreferences()
const preferenceVm = computed(() => timetablePreferencesStore.preferenceByType(props.timetableType))

// 偏好设置颜色
const enabledColorSetting = computed(() => {
  const cs = (preferenceVm.value as any) && Array.isArray((preferenceVm.value as any).ColorSettings)
    ? (preferenceVm.value as any).ColorSettings as any[]
    : []
  if (!cs || cs.length === 0) return null
  return cs.find((s:any) => s && s.IsEnabled) || cs[0]
})
const colorDetailMap = computed(() => buildColorDetailMap((enabledColorSetting.value as any)?.ColorDetails))

// 判断是否应该显示头像（班级和教室视图不显示头像）
const shouldShowAvatar = computed(() => {
  return props.timetableType !== CourseTimetableTypeEnum.ClassTimetable && 
         props.timetableType !== CourseTimetableTypeEnum.ClassroomTimetable
})

// 启用的字段和标签
const enabledRightTags = computed<any[]>(() => {
  return getEnabledRightTagsFromPreference({
    CardShowInformationSettings: (preferenceVm.value as any)?.CardShowInformationSettings,
    FieldTypeEnum: { Tag: CourseTimetableFieldTypeEnum.Tag, Main: CourseTimetableFieldTypeEnum.Main }
  })
})

const enabledLeftFields = computed<any[]>(() => {
  return getEnabledLeftFieldsFromPreference({
    CardShowInformationSettings: (preferenceVm.value as any)?.CardShowInformationSettings,
    FieldTypeEnum: { Tag: CourseTimetableFieldTypeEnum.Tag, Main: CourseTimetableFieldTypeEnum.Main }
  })
})

// 工具函数
function getFieldValue(item: any, fieldName: string): string {
  return getCourseFieldValue(item, fieldName)
}
function getTagText(item: any, fieldName: string): string {
  return getCourseTagText(item, fieldName)
}
function getTagColor(fieldName: string): string {
  return getCourseTagColor(fieldName, { TagColorSettings: (preferenceVm.value as any)?.TagColorSettings })
}
function getStatusColor(item: any): string {
  return getCourseStatusColor(
    item,
    enabledColorSetting.value?.SettingType as any,
    colorDetailMap.value as any,
    props.timetableType
  )
}

// 计算周一到周日
const startOfWeek = computed(() => {
  const base = props.currentWeek ? dayjs(props.currentWeek) : dayjs()
  const mondayIndex = (base.day() + 6) % 7
  return base.subtract(mondayIndex, 'day').startOf('day')
})
const endOfWeek = computed(() => startOfWeek.value.add(6, 'day'))

const holidaySet = ref<Set<string>>(new Set())
const weekDays = computed(() => {
  const arr: Array<{ key: string; weekText: string; dateText: string; isToday: boolean; isHoliday: boolean }> = []
  const weekdays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']
  for (let i = 0; i < 7; i++) {
    const d = startOfWeek.value.add(i, 'day')
    arr.push({ key: d.format('YYYY-MM-DD'), weekText: weekdays[i], dateText: d.format('DD'), isToday: d.isSame(dayjs(), 'day'), isHoliday: holidaySet.value.has(d.format('YYYY-MM-DD')) })
  }
  return arr
})

// 时段定义
const periods = ref([
  { key: 'morning', name: '上午', start: '00:00', end: '12:00', rangeText: '00:00-12:00' },
  { key: 'afternoon', name: '下午', start: '12:00', end: '18:00', rangeText: '12:00-18:00' },
  { key: 'evening', name: '晚上', start: '18:00', end: '24:00', rangeText: '18:00-24:00' },
])

const minRowHeight = 120

// 数据：按天、时段和对象存储课程
const dataMap = ref<{[key: string]: {[key: string]: {[key: string]: any[]}}}>({})
// 当前显示的数据：按天和时段存储课程（过滤后的数据）
const displayDataMap = ref<{[key: string]: {[key: string]: any[]}}>({})
// 对象课节计数
const objectCourseCounts = ref<Record<string, number>>({})

function isPastDay(dateKey: string){ return dayjs(dateKey).isBefore(dayjs(), 'day') }
function getCellItems(dayKey: string, periodKey: string){ 
  const items = displayDataMap.value[dayKey]?.[periodKey] || []
  return items
}

// 获取对象课节计数
function getObjectCourseCount(objectId: string | number): number {
  const oid = String(objectId)
  return objectCourseCounts.value[oid] || 0
}

function setGridRowRef(el: any, index: number){
  gridRowRefs[index] = el as Element | null
  // 移除ResizeObserver自动监听，改为手动控制
}

function syncRowHeights(){
  // 如果正在同步，直接返回，防止死循环
  if (isSyncing) return
  
  // 确保所有行引用都已更新
  const validRefs = gridRowRefs.filter(el => el !== null)
  if (validRefs.length === 0) return
  
  const heights: number[] = validRefs.map(el => {
    if (!el) return minRowHeight
    const rect = (el as Element).getBoundingClientRect()
    return Math.max(rect.height, minRowHeight)
  })
  
  // 当对象列表有值时，如果三行总高度 + 横向滚动条高度 小于 老师列内容高度，则将差值均分到三行
  try {
    const gridEl = gridRef.value as HTMLElement | undefined
    const objectColEl = objectColumnRef.value as HTMLElement | undefined
    const rowsTotalHeight = heights.reduce((sum, h) => sum + (h || 0), 0)
    const hasHorizontalScroll = !!(gridEl && gridEl.scrollWidth > gridEl.clientWidth)
    const horizontalScrollbarHeight = hasHorizontalScroll && gridEl ? Math.max(0, gridEl.offsetHeight - gridEl.clientHeight) : 0
    // 使用对象列的可见高度（展示高度），而不是滚动高度
    const teacherColumnContentHeight = objectColEl ? objectColEl.clientHeight : 0
    
    if (objectList.value && objectList.value.length > 0 && teacherColumnContentHeight > 0) {
      // 仅当三行总高度本身小于对象列展示高度时才进行均分
      if (rowsTotalHeight < teacherColumnContentHeight && heights.length > 0) {
        const rawDeficit = teacherColumnContentHeight - rowsTotalHeight
        const deficit = Math.max(0, rawDeficit - horizontalScrollbarHeight)
        if (deficit > 0) {
          // 固定均分到3行
          const rowsCount = 3
          const baseAdd = Math.floor(deficit / rowsCount)
          const remainder = deficit % rowsCount
          for (let i = 0; i < heights.length; i++) {
            const extra = baseAdd + (i < remainder ? 1 : 0)
            heights[i] = Math.max(minRowHeight, heights[i] + extra)
          }
        }
      }
    }
  } catch (e) {
    // 忽略测量异常，继续使用原高度
  }
  
  // 如果高度没有变化，不进行同步
  if (heights.length === rowHeights.value.length && heights.every((h, i) => Math.abs(h - (rowHeights.value[i] || 0)) < 1)) {
    return
  }
  
  isSyncing = true
  rowHeights.value = heights
  
  // 使用 requestAnimationFrame 确保DOM完全更新后再同步
  requestAnimationFrame(() => {
    const periodCells = document.querySelectorAll('.period-cell')
    const currentGridRows = gridRowRefs.slice(0, heights.length)
    
    // 同步时段列高度（仅设置最小高度，避免压缩内容）
    if (periodCells.length === heights.length) {
      periodCells.forEach((cell, index) => {
        const cellElement = cell as HTMLElement
        if (cellElement && heights[index]) {
          cellElement.style.height = 'auto'
          cellElement.style.minHeight = heights[index] + 'px'
        }
      })
    }
    
    // 同步内容区每个grid-row的高度（仅设置最小高度，避免压缩内容）
    currentGridRows.forEach((rowEl, index) => {
      const rowElement = rowEl as HTMLElement | null
      if (rowElement && heights[index]) {
        rowElement.style.height = 'auto'
        rowElement.style.minHeight = heights[index] + 'px'
      }
    })
    
    // 同步完成后重置状态
    setTimeout(() => {
      isSyncing = false
    }, 50) // 减少延迟时间
  })
}

// 设置课程项引用
const setCourseItemRef = (el: any, item: any) => {
  if (el) {
    const itemId = item.CourseID || item.ID
    courseItemRefs.set(itemId, el)
  }
}

// 处理课程点击
const handleCourseClick = async (event: Event, item: any) => {
  event.stopPropagation()
  selectedCourseData.value = item
  const targetElement = event.currentTarget as HTMLElement
  virtualRef.value = targetElement
  await nextTick()
  if (courseDetailPopoverRef.value) {
    courseDetailPopoverRef.value.show()
  }
}

// 隐藏popover
const hidePopover = () => {
  if (courseDetailPopoverRef.value) {
    // selectedCourseData.value = {}
    courseDetailPopoverRef.value.hide()
  }
}

// 处理点击外部区域隐藏popover
const handleClickOutside = (event: Event, item: any) => {
  if ((selectedCourseData.value?.CourseID || selectedCourseData.value?.ID) === (item.CourseID || item.ID)) return
  const target = event.target as Element
  const popoverElement = document.querySelector('.course-detail-popover')
  if (popoverElement && popoverElement.contains(target)) return
  hidePopover()
}


// 选择对象
const selectObject = (objectId: string) => {
  switching.value = true
  selectedObjectId.value = objectId
  // 切换对象时清理行高与测量状态，避免沿用上一个对象的 inline 高度
  resetColumnHeights()
  // 强制视图重新挂载，确保重新测量
  viewKey.value++
  // 切换显示数据，不重新请求
  updateDisplayData()
  // 向父级上报选中的对象（用于联动老师ID）
  emit('object-selected', { objectId, timetableType: props.timetableType })
  
  // 延迟同步高度，确保DOM更新完成
  nextTick(() => {
    setTimeout(() => {
      syncRowHeights()
      // 再稍后移除切换 loading，避免闪烁
      setTimeout(() => { switching.value = false }, 80)
    }, 100)
  })
}

// （初始化默认选中对象的通知已移至 loadData 完成后直接触发，避免使用watch）

// 更新显示数据
const updateDisplayData = () => {
  if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
    // 时间课表需要转换数据结构：从 [date][object][period] 转换为 [date][period]
    const convertedData: {[key: string]: {[key: string]: any[]}} = {}
    Object.keys(dataMap.value).forEach(dateKey => {
      const dateData = dataMap.value[dateKey]
      if (dateData.time) { // 时间课表使用 'time' 作为对象键
        convertedData[dateKey] = dateData.time
      }
    })
    displayDataMap.value = convertedData
    return
  }

  if (!selectedObjectId.value) {
    displayDataMap.value = {}
    return
  }

  // 根据选中的对象过滤数据
  const filteredData: {[key: string]: {[key: string]: any[]}} = {}
  Object.keys(dataMap.value).forEach(dateKey => {
    const dateData = dataMap.value[dateKey]
    if (dateData[selectedObjectId.value]) {
      filteredData[dateKey] = dateData[selectedObjectId.value]
    }
  })
  displayDataMap.value = filteredData
  
  // 数据更新后，延迟同步高度
  nextTick(() => {
    setTimeout(() => {
      syncRowHeights()
    }, 50)
  })
}

// 重置列高度
const resetColumnHeights = () => {
  // 设置同步状态，防止重置过程中触发同步
  isSyncing = true
  
  // 重置行高数组
  rowHeights.value = []
  // 清空行引用，避免使用旧的 DOM 引用参与后续测量
  while (gridRowRefs.length) { gridRowRefs.pop() }
  
  // 重置对象列高度
  nextTick(() => {
    const objectCells = document.querySelectorAll('.object-cell')
    const objectItems = document.querySelectorAll('.object-item')
    
    // 重置对象单元格高度
    objectCells.forEach((cell) => {
      const cellElement = cell as HTMLElement
      if (cellElement) {
        cellElement.style.height = 'auto'
        cellElement.style.minHeight = 'auto'
      }
    })
    
    // 重置对象项为固定高度
    objectItems.forEach((item) => {
      const itemElement = item as HTMLElement
      if (itemElement) {
        itemElement.style.height = '52px'
        itemElement.style.minHeight = '52px'
      }
    })
    
    // 重置时段列高度（固定基础最小高度，避免视觉坍塌闪烁）
    const periodCells = document.querySelectorAll('.period-cell')
    periodCells.forEach((cell) => {
      const cellElement = cell as HTMLElement
      if (cellElement) {
        cellElement.style.height = 'auto'
        cellElement.style.minHeight = minRowHeight + 'px'
      }
    })

    // 重置内容区每一行（grid-row）高度（固定基础最小高度）
    const gridRows = document.querySelectorAll('.grid-row')
    gridRows.forEach((row) => {
      const rowElement = row as HTMLElement
      if (rowElement) {
        rowElement.style.height = 'auto'
        rowElement.style.minHeight = minRowHeight + 'px'
      }
    })

    // 清除列跟随滚动的 transform，以免切换视图后出现错位
    if (objectColumnRef.value) {
      (objectColumnRef.value as HTMLElement).style.transform = ''
    }
    if (periodColumnRef.value) {
      (periodColumnRef.value as HTMLElement).style.transform = ''
    }
    
    // 滚动位置复位，避免旧滚动影响测量
    if (gridRef.value) {
      try { (gridRef.value as HTMLElement).scrollTo({ top: 0, behavior: 'auto' }) } catch(_) {
        (gridRef.value as HTMLElement).scrollTop = 0
      }
    }

    // 重置完成后仅恢复同步状态，具体同步交由数据更新后的钩子统一触发
    setTimeout(() => {
      isSyncing = false
    }, 50)
  })
}

// 根据课表类型获取对应的字段名
const getFieldNames = (timetableType: string) => {
  const fieldMap: Record<string, { idField: string, nameField: string, avatarField?: string }> = {
    [CourseTimetableTypeEnum.TeacherTimetable]: { 
      idField: 'TeacherID', 
      nameField: 'TeacherName', 
      avatarField: 'TeacherPhoto' 
    },
    [CourseTimetableTypeEnum.StudentTimetable]: { 
      idField: 'StudentID', 
      nameField: 'StudentName', 
      avatarField: 'StudentPhoto' 
    },
    [CourseTimetableTypeEnum.ClassTimetable]: { 
      idField: 'ClassID', 
      nameField: 'ClassName'
    },
    [CourseTimetableTypeEnum.ClassroomTimetable]: { 
      idField: 'ClassroomID', 
      nameField: 'ClassroomName'
    },
  }
  return fieldMap[timetableType] || fieldMap[CourseTimetableTypeEnum.TeacherTimetable]
}

// 浮窗事件回调
const handleRollCall = (courseData: any) => { console.log('上课点名:', courseData) }

// 处理双击新增排课
const handleDoubleClickAddCourse = (dateKey: string, periodKey: string) => {
    // 准备事件数据
    const eventData: any = {
        object: selectedObject.value, // 使用选中的对象
        date: dateKey,
        period: periodKey,
        timetableType: props.timetableType
    }
    
    // 如果是班级课表，添加CampusID
    if (selectedObject.value?.campusId) {
        eventData.campusId = selectedObject.value.campusId
    }
    // 抛出事件给父组件处理
    emit('canlendar-dbclick-add-course', eventData)
}

// 打开学员浮窗（学员视图对象列“更多”）
async function openStudentPopover(evt: Event, student: any){
    const target = evt.currentTarget as HTMLElement
    // 适配字段
    selectedStudent.value = {
        ID: student.id,
        Name: student.name,
        Photo: student.avatar,
        CampusID: student.campusId,
    }
    studentVirtualRef.value = target
    await nextTick()
    studentPopoverRef.value && studentPopoverRef.value.show()
}
// 导出列（公用工具）
const exportColumns = computed<string[]>(() => buildExportColumns(ALL_COLUMNS as unknown as any[]))
let loadDataRequestCounter = 0
let loadDataTimer: any = null
let pendingRequestId: number | null = null
async function loadData(force=false){
  if (loading.value && !force) return

  // 取消上一次的防抖
  if (loadDataTimer) {
    clearTimeout(loadDataTimer)
    loadDataTimer = null
  }

  const currentRequestId = ++loadDataRequestCounter
  pendingRequestId = currentRequestId

  return new Promise<void>((resolve, reject) => {
    loadDataTimer = setTimeout(async () => {
      // 只有最新的请求才继续
      if (currentRequestId !== pendingRequestId) {
        resolve()
        return
      }

      const s = startOfWeek.value.format('YYYY-MM-DD')
      const e = endOfWeek.value.format('YYYY-MM-DD')
      loading.value = true
      try{
        // 节假日所有视图都请求，用于标记头部"假"
        const holidayRes: any = await queryHoliday({ sdate: s, edate: e })
        const hs = new Set<string>()
        const holidays: any[] = (holidayRes && (holidayRes.Data?.Data || [])) || []
        holidays.forEach((h:any)=>{ const k = dayjs(h.Date).format('YYYY-MM-DD'); if (k && k !== 'Invalid Date') hs.add(k) })
        holidaySet.value = hs

        // 学员课表特殊处理：如果assignList为空则不调用接口
        if (props.timetableType === CourseTimetableTypeEnum.StudentTimetable && (!props.assignList || props.assignList.length === 0)) {
          if (currentRequestId === pendingRequestId) {
            dataMap.value = {}
        displayDataMap.value = {}
          }
          resolve()
          return
        }
        const fieldNames = getFieldNames(props.timetableType)
        const params = {
          ...(props.searchParams || {}),
          ExportColumn: exportColumns.value,
          StartDate: s,
          EndDate: e,
          PageSize: 99999,
          PageIndex: 1,
          IDList:props.assignList&&props.assignList.length?props.assignList.map((i)=>i.ID):[]
        }

        // 根据课表类型调用不同的API
        let apiPromise
        switch (props.timetableType) {
          case CourseTimetableTypeEnum.TimeTimetable:
            // 时间课表使用原来的接口
            apiPromise = queryCourseNew(params)
            break
          case CourseTimetableTypeEnum.ClassTimetable:
            apiPromise = queryCalendarClass(params)
            break
          case CourseTimetableTypeEnum.ClassroomTimetable:
            apiPromise = queryCalendarClassroom(params)
            break
          case CourseTimetableTypeEnum.StudentTimetable:
            apiPromise = queryCalendarStudent(params)
            break
          case CourseTimetableTypeEnum.TeacherTimetable:
          default:
            apiPromise = queryCalendarTeacher(params)
        }

        const res: any = await apiPromise
        const list = props.timetableType === CourseTimetableTypeEnum.TimeTimetable ? res?.Data?.List : res?.Data || []
        
        // 提取对象列表（去重）
        const uniqueObjects = new Map()
        const allCourses: any[] = []
        const counts: Record<string, number> = {}
        
        if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
          // 时间课表直接处理课程列表
          const courseList = (list || []).map((item:any) => ({
            ...item,
            Date: dayjs(item.StartTime).format('YYYY-MM-DD'),
            StartTime: item.StartTime,
            EndTime: item.EndTime,
            type: 'course' // 标记为排课
          }))
          allCourses.push(...courseList)
          
          // 时间课表不需要对象列表，但需要设置空的对象列表和计数
          counts['time'] = courseList.length
          if (currentRequestId === pendingRequestId) {
            objectList.value = []
          }
        } else {
          // 根据assignList是否有值来决定处理方式
          if (props.assignList && props.assignList.length > 0) {
            // 情况1：assignList有值，以assignList为基准
            props.assignList.forEach((assignItem: any) => {
              const objId = String(assignItem.ID)
              const objName = assignItem.TeacherName || assignItem.StudentName || assignItem.ClassName || assignItem.ClassroomName || assignItem.Name || '未知'
              const avatar = fieldNames.avatarField?assignItem[fieldNames.avatarField] :''
              // 先设置对象信息
              uniqueObjects.set(objId, {
                id: objId,
                name: objName,
                campusId:assignItem.CampusID||assignItem.CampusList||'',
                avatar:avatar||assignItem['Photo']
              })
              counts[objId] = 0 // 初始课程数为0
              
              // 从API返回的数据中查找对应的排课信息
              const matchedItem = list.find((item: any) => {
                const itemId = item.TeacherID || item.StudentID || item.ClassID || item.ClassroomID
                return String(itemId) === objId
              })
              
              if (matchedItem) {
                // 处理排课数据
                if (Array.isArray(matchedItem.CourseList)) {
                  counts[objId] = matchedItem.CourseList.length
                  matchedItem.CourseList.forEach((course: any) => {
                    allCourses.push({
                      ...course,
                      Date: dayjs(course.StartTime).format('YYYY-MM-DD'),
                      StartTime: course.StartTime,
                      EndTime: course.EndTime,
                      ObjectID: objId,
                      ObjectName: objName,
                      type: 'course' // 标记为排课
                    })
                  })
                }
                
                // 处理老师日程数据
                if (Array.isArray(matchedItem.ScheduleList)) {
                  matchedItem.ScheduleList.forEach((schedule: any) => {
                    allCourses.push({
                      ...schedule,
                      Date: dayjs(schedule.StartTime).format('YYYY-MM-DD'),
                      StartTime: schedule.StartTime,
                      EndTime: schedule.EndTime,
                      ObjectID: objId,
                      ObjectName: objName,
                      type: 'schedule' // 标记为日程
                    })
                  })
                }
              }
            })
          } else {
            // 情况2：assignList没有值，维持现状（按原有逻辑处理API返回的数据）
            list.forEach((item: any) => {
              const objId = item.TeacherID || item.StudentID || item.ClassID || item.ClassroomID
              const objName = item.TeacherName || item.StudentName || item.ClassName || item.ClassroomName
              const avatar = fieldNames.avatarField?item[fieldNames.avatarField] :''
              if (objId && objName && !uniqueObjects.has(objId)) {
                uniqueObjects.set(objId, {
                  id: objId,
                  name: objName,
                  avatar,
                  campusId: item.CampusID||item.CampusList||'' // 保存CampusID
                })
                counts[objId] = 0 // 初始课程数为0
              }
              
              // 处理排课列表
              if (Array.isArray(item.CourseList)) {
                counts[objId] = item.CourseList.length // 设置课程数量
                item.CourseList.forEach((course: any) => {
                  allCourses.push({
                    ...course,
                    Date: dayjs(course.StartTime).format('YYYY-MM-DD'),
                    StartTime: course.StartTime,
                    EndTime: course.EndTime,
                    ObjectID: objId,
                    ObjectName: objName,
                    type: 'course' // 标记为排课
                  })
                })
              }
              
              // 处理老师日程数据
              if (Array.isArray(item.ScheduleList)) {
                item.ScheduleList.forEach((schedule: any) => {
                  allCourses.push({
                    ...schedule,
                    Date: dayjs(schedule.StartTime).format('YYYY-MM-DD'),
                    StartTime: schedule.StartTime,
                    EndTime: schedule.EndTime,
                    ObjectID: objId,
                    ObjectName: objName,
                    type: 'schedule' // 标记为日程
                  })
                })
              }
            })
          }
          
          if (currentRequestId === pendingRequestId) {
            objectList.value = Array.from(uniqueObjects.values())
            
            // 选中对象逻辑：如果当前选中的对象不在新列表中，或者没有选中对象，则选中第一个
            if (objectList.value.length > 0) {
              const currentSelectedExists = selectedObjectId.value && objectList.value.some(obj => obj.id === selectedObjectId.value)
              if (!currentSelectedExists) {
                selectedObjectId.value = objectList.value[0].id
              // 初始化默认选中时通知父组件（仅老师课表）
              if (props.timetableType === CourseTimetableTypeEnum.TeacherTimetable) {
                emit('object-selected', { objectId: selectedObjectId.value, timetableType: props.timetableType })
              }
              }
            } else {
              selectedObjectId.value = ''
            }
          }
        }
        
        // 按日期、时段和对象分组存储数据
        const map: {[key: string]: {[key: string]: {[key: string]: any[]}}} = {}
        allCourses.forEach((course: any) => {
          const dk = course.Date
          const startTime = dayjs(course.StartTime).format('HH:mm')
          let periodKey = ''
          
          // 判断时段
          if (startTime >= '00:00' && startTime < '12:00') {
            periodKey = 'morning'
          } else if (startTime >= '12:00' && startTime < '18:00') {
            periodKey = 'afternoon'
          } else if (startTime >= '18:00' && startTime < '24:00') {
            periodKey = 'evening'
          }
          
          if (periodKey) {
            const objectKey = props.timetableType === CourseTimetableTypeEnum.TimeTimetable ? 'time' : course.ObjectID
            
            if (!map[dk]) map[dk] = {}
            if (!map[dk][objectKey]) map[dk][objectKey] = {}
            if (!map[dk][objectKey][periodKey]) map[dk][objectKey][periodKey] = []
            map[dk][objectKey][periodKey].push(course)
            // 按StartTime时间从早到晚排序
            map[dk][objectKey][periodKey].sort((a: any, b: any) => {
                const timeA = new Date(a.StartTime).getTime()
                const timeB = new Date(b.StartTime).getTime()
                return timeA - timeB
            })
          }
        })
        
        if (currentRequestId === pendingRequestId) {
          dataMap.value = map
          
          // 更新课节计数
          objectCourseCounts.value = counts
          
          // 更新显示数据
          updateDisplayData()

          // 延迟同步高度，确保DOM完全更新
          nextTick(() => {
            setTimeout(() => {
              syncRowHeights()
            }, 150)
          })
        }

        resolve()
      } catch (err) {
        if (currentRequestId === pendingRequestId) {
          reject(err)
        } else {
          resolve()
        }
      } finally {
        if (currentRequestId === pendingRequestId) {
          loading.value = false
        }
      }
    }, 200)
  })
}


// 同步滚动函数
function setupScrollSync() {
  if (!periodColumnRef.value || !gridRef.value) return

  const objectColumn = objectColumnRef.value
  const periodColumn = periodColumnRef.value
  const grid = gridRef.value

  // 只监听内容区的滚动，让对象列和时段列跟随内容区滚动
  const handleGridScroll = () => {
    if (isScrolling) return
    isScrolling = true
    // 通过transform来同步对象列和时段列的位置
    if (objectColumn && showObjectColumn.value) {
      objectColumn.style.transform = `translateY(-${grid.scrollTop}px)`
    }
    periodColumn.style.transform = `translateY(-${grid.scrollTop}px)`
    nextTick(() => {
      isScrolling = false
    })
  }

  grid.addEventListener('scroll', handleGridScroll)

  // 返回清理函数
  return () => {
    grid.removeEventListener('scroll', handleGridScroll)
  }
}

let cleanupScrollSync: (() => void) | null = null

onMounted(() => {
  // 初始同步高度
  nextTick(() => {
    setTimeout(() => {
      syncRowHeights()
    }, 100)
    cleanupScrollSync = setupScrollSync() || null
  })
})

onUnmounted(() => {
  if (cleanupScrollSync) {
    cleanupScrollSync()
  }
  courseItemRefs.clear()
})

watch(() => [props.currentWeek, props.searchParams, props.timetableType], async (nv, ov) => {
  const hasParams = props.searchParams && Object.keys(props.searchParams || {}).length > 0
  if (!hasParams) return

  const weekChanged = ov ? nv[0] !== ov[0] : false
  const paramsChanged = ov ? nv[1] !== ov[1] : false
  const typeChanged = ov ? nv[2] !== ov[2] : true // 首次渲染认为类型已改变

  // 课表类型切换时重置状态
  if (typeChanged) {
    switching.value = true
    resetColumnHeights()
    objectList.value = []
    selectedObjectId.value = ''
    // 强制视图重新挂载，避免继承上一视图的 inline 样式与测量结果
    viewKey.value++
  }

  if (weekChanged || typeChanged) {
    await loadData(true)
    await nextTick()
    setTimeout(() => {
      syncRowHeights()
      setTimeout(() => { switching.value = false }, 80)
    }, 120)
    return
  }

  if (paramsChanged) {
    await loadData(true)
    await nextTick()
    setTimeout(() => {
      syncRowHeights()
    }, 120)
  }
}, { deep: true, immediate: true })

// 监听 assignList 变化，确保数据更新后选中第一个对象并同步行高
watch(() => props.assignList, async (nv, ov) => {
  const hasParams = props.searchParams && Object.keys(props.searchParams || {}).length > 0
  if (!hasParams) return
  if (nv !== ov) {
    await loadData(true)
    await nextTick()
    setTimeout(() => {
      syncRowHeights()
    }, 100)
  }
}, { deep: true })

// 监听显示数据变化，确保高度同步
watch(() => displayDataMap.value, () => {
  nextTick(() => {
    setTimeout(() => {
      syncRowHeights()
    }, 100)
  })
}, { deep: true })
</script>

<style scoped lang="scss">
.period-calendar-view {
    width: 100%;
    height: 100%;
    overflow: auto;
}

/* 基础grid布局 */
.calendar-container {
    display: grid;
    grid-template-rows: 44px 1fr;
    position: relative;
}

/* 三列布局：对象列 + 时段列 + 内容区 */
.calendar-container.three-columns {
    grid-template-columns: 0px 50px 1fr;
}

/* 两列布局：时段列 + 内容区 */
.calendar-container.two-columns {
    grid-template-columns: 50px 1fr;
}

/* 单列布局：仅内容区 */
.calendar-container.one-column {
    grid-template-columns: 1fr;
}

/* 顶部左上角列头 */
.corner {
    top: 0;
    left: 0;
    border-right: 1px solid #E4E7ED;
    border-bottom: 1px solid #E4E7ED;

    &.sticky {
        position: sticky;
        left: auto;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 2;
        background: #F5F7FA;
        color: #303133;
        font-weight: 600;
        height: 44px;
    }
}

.object-corner {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
    font-size: 13px;
}

.period-corner {
    grid-column: 2 / 3;
    grid-row: 1 / 2;
    font-size: 13px;
}

/* 两列布局时，时段列位置调整 */
.calendar-container.two-columns .period-corner {
    grid-column: 1 / 2;
}

/* 顶部日期头 */
.header {
    grid-column: 3 / 4;
    grid-row: 1 / 2;
    position: sticky;
    top: 0;
    z-index: 2;
    display: grid;
    grid-template-columns: repeat(7, minmax(128px, 1fr));
    background: #F5F7FA;
    border-bottom: 1px solid #E4E7ED;
}

/* 两列布局时，日期头位置调整 */
.calendar-container.two-columns .header {
    grid-column: 2 / 3;
}

.header-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 8px 12px;
    font-weight: 600;
    color: #303133;
    border-right: 1px solid #E4E7ED;
}

.header-cell:last-child {
    border-right: none;
}

.header-date {
    width: 22px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    color: #C0C4CC;
    
}

.header-date.circle {
    background: #2878E8;
    color: #fff;
    border-radius: 50%;
    font-size:12px;
}

/* 左侧对象列 */
.object-column {
    grid-column: 1 / 2;
    grid-row: 2 / 3;
    left: 0;
    border-right: 1px solid #E4E7ED;
    overflow: hidden;
}
.object-column-container {
  width: 120px;
    .object-column {
      height: calc(100% - 44px);
      overflow-y: auto;
    }
  }

.object-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 6px;
    box-sizing: border-box;
    border-bottom: 1px solid #E4E7ED;
    min-height: 360px; /* 上午、下午、晚上三行的高度之和 */
}

.object-name {
    color: #606266;
    font-size: 14px;
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 20px;
    flex:1;
}

/* 对象列表样式 */
.object-list {
    display: flex;
    flex-direction: column;
    margin-top: 4px;
}

.object-item {
    display: flex;
    align-items: center;
    padding: 8px 6px;
    box-sizing: border-box;
    cursor: pointer;
    transition: background-color 0.2s;
    height: 52px; /* 固定高度 */
    border-radius: 4px;
    margin: 0 4px 4px;
    width: calc(100% - 8px);
    .object-info{
      flex: 1;
      width: 100%;
      &.width-avatar{
        width: calc(100% - 30px);
      }
      
    }
    .avatar {
        width: 28px;
        height: 28px;
        border-radius: 4px;
        overflow: hidden;
        background: #f2f3f5;
        flex-shrink: 0;
        margin-right: 6px;
    }

    .avatar img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .avatar.placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
      color: #fff;
      font-weight: 500;
      font-size: 14px;
  }
    .teacher-course-num{
      background: #F0F0F0;
      border-radius: 40px;
      min-width: 41px;
      // flex: 1;
      max-width: 100%;
      text-align: center;
      height: 16px;
      line-height: 16px;
      font-size: 12px;
      color: #606266;
  }
  .more-btn{
      cursor: pointer;
      .default-icon{
          display: block;
      }
      .active-icon{
          display: none;
      }
      .current-active-icon{
        display: none;
      }
      &:hover{
          .default-icon{
              display: none;
          }
          .active-icon{
              display: block;
          }
      }
  }
}

.object-item:hover {
  background: rgba(0,0,0,0.04);
}

.object-item.active {
  background: #2878E8;
  .object-name{
    color: #fff;
  }
  .teacher-course-num{
    color: #fff;
    background: rgba(255,255,255,0.2);
  }
  .more-btn{
    .current-active-icon{
      display: block;
    }
    .active-icon{
      display: none;
    }
    .default-icon{
      display: none;
    }
    &:hover{
      .default-icon{
        display: block;
      }
      .active-icon{
        display: none;
      }
      .current-active-icon{
          display: none;
      }
    }
  }
}



/* 左侧时段列 */
.period-column {
    grid-column: 2 / 3;
    grid-row: 2 / 3;
    left: 0;
    border-right: 1px solid #E4E7ED;
    overflow: hidden;
}

/* 两列布局时，时段列位置调整 */
.calendar-container.two-columns .period-column {
    grid-column: 1 / 2;
}

.period-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 6px;
    box-sizing: border-box;
    border-bottom: 1px solid #E4E7ED;
    min-height: 120px;
    transition: min-height 120ms ease-in-out;
}

.period-name {
    color: #606266;
    font-size: 14px;
}

.period-time {
    margin-top: 4px;
    font-size: 12px;
    color: #A0A4AA;
}

/* 右侧内容区 */
.grid {
    grid-column: 3 / 4;
    grid-row: 2 / 3;
    overflow: auto;
}

/* 两列布局时，内容区位置调整 */
.calendar-container.two-columns .grid {
    grid-column: 2 / 3;
}

/* 单列布局时，内容区位置调整 */
.calendar-container.one-column .grid {
    grid-column: 1 / 2;
}

.grid-row {
    display: grid;
    grid-template-columns: repeat(7, minmax(128px, 1fr));
    min-height: 120px;
    transition: min-height 120ms ease-in-out;
}

.grid-cell {
    border-right: 1px solid #E4E7ED;
    border-bottom: 1px solid #E4E7ED;
    padding: 8px;
    background: #fff;
    display: flex;
    flex-direction: column;
    min-height: 120px;
    box-sizing: border-box;
}

.grid-cell.is-past {
    background: #F9FAFC;
}

.grid-row .grid-cell:last-child {
    border-right: none;
}

.double-click-add-course {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 1;
    height: 100%;
    min-height: 40px;
    background: #F0F2F5;
    border-radius: 8px;
    font-size: 14px;
    color: #909399;
    font-weight: 500;
    cursor: pointer;
    visibility: hidden;
}

.grid-cell:hover .double-click-add-course {
    visibility: visible;
}



.empty-state {
    grid-column: 1 / 3;
    grid-row: 1 / 3;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 200px;
    background: #fff;
}

.holiday-flag {
    width: 20px;
    height: 20px;
    line-height: 20px;
    text-align: center;
    background: #FFECE8;
    border-radius: 4px;
    font-size: 12px;
    color: #F53F3F;
    font-weight: 400;
}
</style>