<template>
  <div ref="viewContainerRef" class="period-calendar-view is-compare" :class="{'period-loading':loading}" :data-timetable-type="props.timetableType">
    <!-- 🆕 对比模式头部信息（学员/老师） -->
    <div v-if="props.assignList?.[0]"
      :class="[props.timetableType === CourseTimetableTypeEnum.StudentTimetable ? 'student-header' : 'teacher-header']">
      <div
        :class="[props.timetableType === CourseTimetableTypeEnum.StudentTimetable ? 'student-info' : 'teacher-info']">
        <!-- 🆕 刷新按钮（名字左边） -->
        <div class="refresh-btn" @click.stop="handleRefreshSingle" :title="`刷新${props.assignList[0].Name}的数据`">
          <el-icon :size="16"><Refresh /></el-icon>
        </div>
        <div
          :class="[props.timetableType === CourseTimetableTypeEnum.StudentTimetable ? 'student-details' : 'teacher-details']">
          <span
            :class="[props.timetableType === CourseTimetableTypeEnum.StudentTimetable ? 'student-name' : 'teacher-name']">
            {{ props.assignList[0].Name }}
          </span>
          <span v-if="props.timetableType === CourseTimetableTypeEnum.StudentTimetable" class="student-id">
            ({{ props.assignList[0].Serial }})
          </span>
          <template v-if="props.timetableType === CourseTimetableTypeEnum.StudentTimetable">
            <div class="more-btn ml-4px mt-1px" @click.stop="openStudentPopover($event, props.assignList[0])"
              v-click-outside="(event: any) => handleStudentClickOutside(event, props.assignList?.[0])">
                    <el-icon class="default-icon" size="16px"><svg aria-hidden="true">
                        <use xlink:href="#w2-xueyuanxinxi"></use>
                      </svg></el-icon>
            </div>
          </template>
          <div class="object-count" v-if="getCurrentObjectCourseCount">
            {{ getCurrentObjectCourseCount }}条
          </div>
        </div>
      </div>
      <!-- 🆕 拖到老师课表按钮（学员头部右边） -->
      <div v-if="props.timetableType === CourseTimetableTypeEnum.StudentTimetable"
        class="drag-to-teacher-btn"
        draggable="true"
        @dragstart="handleDragScheduleStart"
        @dragend="handleDragScheduleEnd"
        title="拖到老师课表">
                  <!-- 🆕 拖拽图标提示 -->
          <el-icon v-if="props.timetableType === CourseTimetableTypeEnum.StudentTimetable" 
                   class="drag-icon" size="14px">
            <svg aria-hidden="true">
              <use xlink:href="#w2-paixu"></use>
            </svg>
          </el-icon>
        <span>拖到老师课表</span>
      </div>
    </div>
    
    <div class="calendar-container" 
         :style="{ '--week-days-count': weekDays.length }" 
         v-loading="loading || refreshing" 
         element-loading-target=".calendar-container"
         @dragover="handleTableDragOver"
         @dragleave="handleTableDragLeave"
         @drop="handleTableDrop">
      <!-- 表格 -->
      <table class="period-calendar-table">
        <!-- 表格头部 -->
        <thead>
          <tr>
            <!-- 🆕 对比模式：不显示对象列 -->
            
            <!-- 时段列 -->
            <th class="period-header sticky">
              <div class="flex-center">
                 {{ props.viewBy=='byRange'?'范围':'时段' }}
              </div>
            </th>
            <!-- 日期列 -->
            <th
              v-for="day in weekDays"
              :key="day.key"
              class="header-cell sticky"
              :class="{ today: day.isToday }"
            >
              <div class="header-cell-box">
                <div class="header-week">{{ day.weekText }}</div>
                <div class="header-date" :class="{ circle: day.isToday }">
                  {{ day.isToday ? '今' : day.dateText }}
                </div>
                <span v-if="day.isHoliday" class="holiday-flag">假</span>
              </div>
            </th>
          </tr>
        </thead>
        <!-- 表格主体 -->
        <tbody>
          <!-- 学员视图且assignList为空时显示空状态 -->
          <tr v-if="timetableType === CourseTimetableTypeEnum.StudentTimetable && (!assignList || assignList.length === 0)">
            <td :colspan="1 + weekDays.length" class="empty-state">
              <el-empty :image="globalData.emptyPng" :image-size="100">
                <template #description>
                  <div class="color-[#666]">
                    仅支持选择“报读了1对1{{ transToConfigDescript('课程') }}”的学员，请在左上角选择。
                  </div>
                </template>
              </el-empty>
            </td>
          </tr>
          <tr v-else-if="(!assignList || assignList.length === 0)||(!visibleTeachers) || visibleTeachers.length === 0">
            <td :colspan="1 + weekDays.length" class="empty-state">
              <el-empty :image="globalData.emptyPng" :image-size="100">
                <template #description>
                  <div class="color-[#666]">
                    暂无排课
                  </div>
                </template>
              </el-empty>
            </td>
          </tr>
          <!-- 正常内容行 -->
          <template v-else>
            <!-- 每个教师 -->
            <template v-for="(t, idx) in visibleTeachers" :key="t.id">
              <!-- 每个时段 -->
              <tr v-for="(period, periodIdx) in timePeriods" :key="`${t.id}-${period.key}`">
                <!-- 🆕 对比模式：不显示对象列 -->
                
                <!-- 时段名称 -->
                <td class="period-label-cell" :class="{'text-12px!':props.viewBy=='byRange'}" v-html="period.label"></td>
                <!-- 每天的课程单元格 -->
                <td
                  v-for="(day, dayIndex) in weekDays"
                  :key="day.key"
                  class="grid-day-column"
                  :data-teacher-id="t.id"
                  :data-date-key="day.key"
                  :data-day-index="dayIndex"
                  :data-period-key="period.key"
                  :data-period-start="period.startTime"
                  :data-period-end="period.endTime"
                >
                  <div
                    class="grid-cell"
                    :class="{ 'is-past': isPastDay(day.key), 'drag-over': isDragOver && dragTargetCell?.day === dayIndex && dragTargetCell?.periodKey === period.key }"
                  >
                    <template v-for="item in getCellItemsByPeriod(t.id, day.key, period.key)">
                        <!-- 排课卡片 -->
                        <div
                          v-if="item.type === 'course'"
                          :key="`course-${item.CourseID || item.ID || Math.random()}`"
                          class="wtwo-schedule-course-card"
                          :style="{ borderTopColor: getStatusColor(item), backgroundColor: getTint(getStatusColor(item), 0.1) }"
                          v-click-outside="(event: Event) => handleClickOutside(event as MouseEvent, item)"
                          @click="handleCourseClick($event, item)"
                        >
                        <div class="wtwo-schedule-course-time whitespace-nowrap overflow-hidden text-ellipsis" >
                          {{ safeFormatTime(item.StartTime) }}-{{ safeFormatTime(item.EndTime) }}
                        </div>
                        <div class="wtwo-schedule-course-meta">
                          <div
                            class="wtwo-schedule-meta-item ellipsis-single"
                            v-for="field in enabledLeftFields"
                            :key="String(field.FieldName)"
                            :title="getFieldValue(item, String(field.FieldName))"
                          >
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
                            :title="getTagText(item, String(tag.FieldName))"
                          >
                            {{ getTagText(item, String(tag.FieldName)) }}
                          </div>
                        </div>
                      </div>
                      <!-- 日程卡片 -->
                        <div
                          v-if="item.type === 'schedule'"
                          :key="`schedule-${item.ID || Math.random()}`"
                          class="wtwo-schedule-schedule-card"
                          v-click-outside="(event: Event) => handleClickOutside(event as MouseEvent, item)"
                          @click="handleCourseClick($event, item)"
                        >
                        <div class="fixed-type-label">日程</div>
                        <div class="wtwo-schedule-schedule-time">
                          {{ safeFormatTime(item.StartTime) }}-{{ safeFormatTime(item.EndTime) }}
                        </div>
                        <div class="wtwo-schedule-schedule-meta">
                          <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.Title">{{ item.Title }}</div>
                          <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.TeacherList.map((i: any) => i.Name).join(',')">
                            {{ item.TeacherList.map((i: any) => i.Name).join(',') }}
                          </div>
                          <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.CampusName">{{ item.CampusName }}</div>
                          <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.Content">{{ item.Content }}</div>
                          <div v-if="item.Remark" class="wtwo-schedule-meta-item ellipsis-single" :title="item.Remark">{{ item.Remark }}</div>
                        </div>
                      </div>
                    </template>
                    <!-- 🆕 拖拽占位块 -->
                    <div 
                      v-if="dragTargetCell && dragTargetCell.day === dayIndex && dragTargetCell.periodKey === period.key" 
                      class="drag-target-cell"
                    >
                      <div class="drag-target-cell-content">
                        <div class="drag-target-time" v-if="props.viewBy === 'byRange'">
                          {{ dragTargetCell.period.startTime }} - {{ dragTargetCell.period.endTime }}
                        </div>
                        <div class="drag-target-label">松开添加排课</div>
                      </div>
                    </div>
                    <!-- 双击添加排课区域 -->
                    <div
                      class="double-click-add-course"
                      @dblclick="handleDoubleClickAddCourse(t, day.key, period)"
                    >
                      双击新增排课
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </template>
        </tbody>
      </table>
      <!-- 查看更多按钮 -->
      <!-- <div class="view-more mt-12px">
        <el-button
          type="primary"
          plain
          v-if="showViewMore&&visibleTeachers.length>0"
          @click="handleViewMoreClick"
        >
          查看更多{{ transToConfigDescript(objectName) }}（剩余{{ remainingCount }}）
        </el-button>
      </div> -->
      <!-- 超出数量提示对话框 -->
      <el-dialog v-model="overLimitTipVisible" title="温馨提示" width="420px">
        <div class="py-16px line-height-22px">
          当前课表显示的{{ transToConfigDescript(objectName) }}数量过多，可能会影响体验，建议切换到“{{ transToConfigDescript(objectName) }}与时刻”视图
        </div>
        <template #footer>
          <el-button type="primary" @click="overLimitTipVisible = false">我知道了</el-button>
        </template>
      </el-dialog>
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
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ClickOutside as vClickOutside, ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { queryCalendarTeacher, queryCalendarClass, queryCalendarClassroom, queryCalendarStudent } from '@/api/arrange'
import { useUserSettings } from '@/store'
import { getTint, getCourseStatusColor, buildColorDetailMap, buildExportColumns, getEnabledRightTagsFromPreference, getEnabledLeftFieldsFromPreference, getCourseFieldValue, getCourseTagText, getCourseTagColor, getNameInitial } from '@/utils'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { CourseTimetableTypeEnum, CourseTimetableFieldTypeEnum } from '@/types/model/timetable-preference'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
import { getCurrentInstance } from 'vue'
import { queryHoliday } from '@/api/comm'
import CourseDetailPopover from '@/components/CourseDetailPopover.vue'
import StudentDetailPopover from '@/pages/scheduleManage/components/studentDetailPopover.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
import ViewModePopover from '@/pages/scheduleManage/components/ViewModePopover.vue'
// 导出列（公用工具）
const exportColumns = computed<string[]>(() => buildExportColumns(ALL_COLUMNS as unknown as any[]))
// 类型定义
interface Teacher {
  id: string | number
  name: string
  avatar?: string
  desc?: string
  campusId?: string
}

interface TimePeriod {
  key: string
  label: string
  startTime: number | string
  endTime: number | string
  startHour?: number
  endHour?: number
}

// 时段定义
const DEFAULT_TIME_PERIODS = [
  { key: 'morning', label: '上午', startTime: 0, endTime: 12 },
  { key: 'afternoon', label: '下午', startTime: 12, endTime: 18 },
  { key: 'evening', label: '晚上', startTime: 18, endTime: 24 }
] as const;
const timePeriods = ref<TimePeriod[]>([...DEFAULT_TIME_PERIODS])

// 🆕 时段配置是否已加载的标志（防止重复加载）
const periodConfigLoaded = ref(false)

// Props定义
const props = defineProps<{
  currentWeek?: Date
  timetableType?: any
  assignList?: any[]
  viewBy?: string
  externalData?: any  // 🆕 对比模式：接收外部批量加载的数据
  holidays?: any[]    // 🆕 对比模式：接收节假日数据
  refreshing?: boolean  // 🆕 单个刷新时的 loading 状态
}>()

// Emits定义
const emit = defineEmits<{
  'canlendar-dbclick-add-course': [data: { object: any; date: string; timetableType: any; timeRange?: { startTime: string; endTime: string } }]
  'update:view-by': [viewBy: string]
  'object-select-click': []
  'remove-object': [item: any]
  'refresh-single': [data: { object: any; timetableType: any }]  // 🆕 刷新单个对象的数据
}>()

// 显示方式设置相关
// 显示方式弹窗控制
const viewModeVisible = ref(false)

// 🆕 刷新单个对象的数据
const handleRefreshSingle = () => {
  if (props.assignList && props.assignList[0]) {
    emit('refresh-single', {
      object: props.assignList[0],
      timetableType: props.timetableType
    })
  }
}

// 🆕 拖拽排课相关状态
const isDragOver = ref(false) // 是否有学员被拖拽到当前老师课表上方
const dragTargetCell = ref<any>(null) // 当前拖拽悬停的目标格子 { day, periodKey, period }

// 🚀 性能优化：拖拽位置计算优化
const dragOptimization = {
  rafId: null as number | null,    // requestAnimationFrame ID
  pendingUpdate: null as any,       // 待更新的位置数据
  lastUpdateTime: 0,                // 上次更新时间
  minUpdateInterval: 16             // 最小更新间隔（约60fps）
}

// 🆕 拖拽排课 - 开始拖拽学员
const handleDragScheduleStart = (event: DragEvent) => {
  if (props.timetableType !== CourseTimetableTypeEnum.StudentTimetable) return
  
  const studentInfo = props.assignList?.[0]
  if (!studentInfo) return
  
  // 设置拖拽数据
  const dragData = {
    type: 'schedule-student',
    student: {
      ID: studentInfo.ID,
      Name: studentInfo.Name,
      Serial: studentInfo.Serial,
      Avatar: studentInfo.Avatar || studentInfo.Photo,
      CampusID: studentInfo.CampusID,
      CampusName: studentInfo.CampusName
    }
  }
  
  event.dataTransfer!.setData('application/json', JSON.stringify(dragData))
  event.dataTransfer!.effectAllowed = 'copy'
  
  // 🎨 灰色背景的拖拽图像 - 只显示学员姓名（居中）
  const dragImage = document.createElement('div')
  dragImage.innerHTML = `
    <div style="
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 8px 16px;
      background: #f0f2f5;
      color: #606266;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      font-size: 14px;
      font-weight: 500;
      white-space: nowrap;
      border: 1px solid #dcdfe6;
    ">
      <span>${studentInfo.Name}</span>
    </div>
  `
  
  dragImage.style.cssText = `
    position: absolute;
    top: -9999px;
    left: -9999px;
    pointer-events: none;
    z-index: 9999;
  `
  
  document.body.appendChild(dragImage)
  event.dataTransfer!.setDragImage(dragImage, 50, 20)
  
  // 清理临时元素
  setTimeout(() => {
    if (document.body.contains(dragImage)) {
      document.body.removeChild(dragImage)
    }
  }, 0)
}

// 🆕 拖拽排课 - 拖拽结束
const handleDragScheduleEnd = (event: DragEvent) => {
  // 拖拽结束后的清理工作
  // 🆕 恢复鼠标样式
  document.body.style.cursor = ''
  dragTargetCell.value = null
  isDragOver.value = false
  
  // 🚀 性能优化：清理 RAF
  if (dragOptimization.rafId) {
    cancelAnimationFrame(dragOptimization.rafId)
    dragOptimization.rafId = null
  }
  dragOptimization.pendingUpdate = null
}

// 移除对象功能
const handleRemove = (item: any) => {
  emit('remove-object', item)
}

// 获取全局实例
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

// 响应式数据
const internalLoading = ref(false)
// 🆕 合并内部 loading 和外部 refreshing 状态
const loading = computed(() => internalLoading.value || props.refreshing || false)

// 课程详情浮窗相关
const courseDetailPopoverRef = ref()
const selectedCourseData = ref<any>({})
const virtualRef = ref()

// 学员详情浮窗
const studentPopoverRef = ref()
const studentVirtualRef = ref()
const selectedStudent = ref<any>({})

// 根据课表类型获取对应的对象名称
const objectName = computed(() => {
  const map: Record<string, string> = {
    [CourseTimetableTypeEnum.TeacherTimetable]: '任课老师',
    [CourseTimetableTypeEnum.StudentTimetable]: '学员',
    [CourseTimetableTypeEnum.ClassTimetable]: '班级',
    [CourseTimetableTypeEnum.ClassroomTimetable]: '教室',
    [CourseTimetableTypeEnum.TimeTimetable]: '时间'
  }
  return map[props.timetableType] || '老师'
})

// 🆕 对比模式：获取当前对象的课程数量
const getCurrentObjectCourseCount = computed(() => {
  // 统计所有时段的课程数量
  let count = 0
  Object.values(dataMap.value).forEach((teacherData: any) => {
    Object.values(teacherData.periodDataMap || {}).forEach((periods: any) => {
      if (Array.isArray(periods)) {
        count += periods.filter((c: any) => c.type !== 'schedule').length
      }
    })
  })
  return count
})

// 处理显示方式保存
const handleViewModeSave = (viewBy: string) => {
  emit('update:view-by', viewBy)
}

// 处理对象选择点击
const handleObjectSelectClick = () => {
  emit('object-select-click')
}

// 是否显示头像（仅老师和学员视图）
// const shouldShowAvatar = computed(() => {
//   return [CourseTimetableTypeEnum.TeacherTimetable, CourseTimetableTypeEnum.StudentTimetable].includes(props.timetableType)
// })

// 课表偏好设置
const timetablePreferencesStore = useTimetablePreferences()

// 用户设置store
const userSettingsStore = useUserSettings()

// 启用的左侧字段列表
const enabledLeftFields = computed(() => {
  return getEnabledLeftFieldsFromPreference(timetablePreferencesStore.preferenceByType(props.timetableType))
})

// 启用的右侧标签列表
const enabledRightTags = computed(() => {
  return getEnabledRightTagsFromPreference(timetablePreferencesStore.preferenceByType(props.timetableType))
})

// 当前周的起始日期
const currentWeekStart = computed(() => {
  if (props.currentWeek) return dayjs(props.currentWeek).startOf('week')
  return dayjs().startOf('week')
})

// 本周的日期列表
const weekDays = computed(() => {
  const days = []
  for (let i = 0; i < 7; i++) {
    const date = currentWeekStart.value.add(i, 'day')
    const dateStr = date.format('YYYY-MM-DD')
    days.push({
      key: dateStr,
      date: date,
      dateText: date.format('DD'),
      weekText: '周' + ['日', '一', '二', '三', '四', '五', '六'][date.day()],
      isToday: date.isSame(dayjs(), 'day'),
      isHoliday: false // 后续会根据节假日信息更新
    })
  }
  return days
})

// 最大显示数量
const MAX_DISPLAY_COUNT = 100

// 默认显示数量
const DEFAULT_DISPLAY_COUNT = 5

// 每次增加数量
const INCREMENT_COUNT = 5

// 显示的教师列表
const visibleTeachers = ref<Teacher[]>([])

// 当前显示数量
const currentDisplayCount = ref(DEFAULT_DISPLAY_COUNT)

// 剩余的教师数量
const remainingCount = ref(0)

// 是否显示查看更多按钮
const showViewMore = ref(false)

// 教师ID集合（保存教师头像等信息）
const teacherIdSet = ref<Map<string | number, Teacher>>(new Map())

// 数据映射
const dataMap = ref<Record<string, Record<string, Record<string, any[]>>>>({})

// 节假日映射
const holidayMap = ref<Record<string, boolean>>({})

// 超出数量提示对话框
const overLimitTipVisible = ref(false)

// 偏好设置颜色
const enabledColorSetting = computed(() => {
  const preference:any = timetablePreferencesStore.preferenceByType(props.timetableType)
  const cs = preference && Array.isArray(preference.ColorSettings)
    ? preference.ColorSettings as any[]
    : []
  if (!cs || cs.length === 0) return null
  return cs.find((s:any) => s && s.IsEnabled) || cs[0]
})
const colorDetailMap = computed(() => buildColorDetailMap((enabledColorSetting.value as any)?.ColorDetails))

// 获取状态颜色
const getStatusColor = (item: any) => {
  return getCourseStatusColor(
    item,
    enabledColorSetting.value?.SettingType as any,
    colorDetailMap.value as any,
    props.timetableType
  )
}

// 获取字段值
const getFieldValue = (item: any, fieldName: string) => {
  return getCourseFieldValue(item, fieldName)
}

// 获取标签文本
const getTagText = (item: any, tagName: string) => {
  return getCourseTagText(item, tagName)
}

// 获取标签颜色
const getTagColor = (tagName: string) => {
  return getCourseTagColor(tagName, timetablePreferencesStore.preferenceByType(props.timetableType))
}

// 🆕 对比模式：处理外部传入的数据
// 🆕 对比模式：处理外部数据
async function processExternalData() {
  internalLoading.value = true
  try {
    // 🆕 加载时段配置（根据viewBy决定使用默认时段还是自定义时段）
    await loadPeriodConfig()
    
    // 处理外部传入的节假日数据
    if (props.holidays && Array.isArray(props.holidays)) {
      const holidayMapTemp: Record<string, boolean> = {}
      props.holidays.forEach((h: any) => {
        const k = dayjs(h.Date).format('YYYY-MM-DD')
        if (k && k !== 'Invalid Date') {
          holidayMapTemp[k] = true
        }
      })
      holidayMap.value = holidayMapTemp
    }

    // 如果 externalData 存在，处理课程数据
    if (props.externalData) {
      processData([props.externalData])
    } else {
      // 即使没有 externalData，也要从 assignList 构建 visibleTeachers
      dataMap.value = {}
      teacherIdSet.value = new Map()
      
      // 调用 processData 传入空数组，让它从 assignList 构建
      processData([])
    }
  } catch (error) {
    console.error('处理外部数据失败:', error)
    dataMap.value = {}
    visibleTeachers.value = []
  } finally {
    internalLoading.value = false
  }
}

// 🆕 加载时段配置（提取出来，供processExternalData和loadData共用）
async function loadPeriodConfig() {
  // 处理时段数据
  if (props.viewBy === 'byRange') {
    // 获取用户自定义时段（使用缓存，避免重复请求）
    try {
      const settings = await userSettingsStore.fetchUserSettings('TeacherTimeRange', '老师课表时段范围', 1, 1, false)
      
      if (settings && settings.length > 0) {
        // 处理用户自定义时段
        const customPeriods: TimePeriod[] = []
        settings.forEach((item: any, index: number) => {
          if (item.UserSettingsDetailList && item.UserSettingsDetailList.length > 0) {
            item.UserSettingsDetailList.forEach((detail: any) => {
              if (detail.Field1 && detail.Field2) {
                customPeriods.push({
                  key: `custom_${index}_${detail.ID}`,
                  label: `${detail.Field1}<br/>${detail.Field2}`,
                  startTime: detail.Field1, // 存储完整的时间字符串，如 "07:00"
                  endTime: detail.Field2,   // 存储完整的时间字符串，如 "09:00"
                  startHour: parseInt(detail.Field1.split(':')[0]), // 保留小时数用于其他可能的计算
                  endHour: parseInt(detail.Field2.split(':')[0])     // 保留小时数用于其他可能的计算
                })
              }
            })
          }
        })
        // 按startTime升序排序时段
        customPeriods.sort((a, b) => {
          const aTime = typeof a.startTime === 'string' ? a.startTime : String(a.startTime)
          const bTime = typeof b.startTime === 'string' ? b.startTime : String(b.startTime)
          return aTime.localeCompare(bTime)
        })
        timePeriods.value = customPeriods
      } else {
        // 如果没有自定义时段，则使用默认时段
        timePeriods.value = [...DEFAULT_TIME_PERIODS]
      }
    } catch (error) {
      console.error('获取自定义时段失败:', error)
      // 出错时使用默认时段
      timePeriods.value = [...DEFAULT_TIME_PERIODS]
    }
  } else {
    // 使用默认时段配置（上午/下午/晚上）
    timePeriods.value = [...DEFAULT_TIME_PERIODS]
  }
}

// 加载数据
let loadDataRequestCounter = 0
let loadDataTimer: any = null
let pendingRequestId: number | null = null

async function loadData(force = false) {
  // 🆕 对比模式：完全依赖外部数据，不调用 API
  // 如果在对比排课中使用，externalData 应该始终存在
  if (props.externalData) {
    await processExternalData()
    return
  }

  // 以下是非对比模式的逻辑
  console.error('❌❌❌ ComparePeriodView loadData 被调用了！这不应该发生！', {
    externalData: props.externalData,
    stack: new Error().stack
  })
  
  // 若已有加载且非强制，则等待已有流程
  if (loading.value && !force) return

  // 取消之前的防抖定时器
  if (loadDataTimer) {
    clearTimeout(loadDataTimer)
    loadDataTimer = null
  }

  // 生成新的请求ID并标记为最新
  const currentRequestId = ++loadDataRequestCounter
  pendingRequestId = currentRequestId

  return new Promise<void>((resolve, reject) => {
    loadDataTimer = setTimeout(async () => {
      // 只有当前请求是最新的才执行
      if (currentRequestId !== pendingRequestId) {
        resolve()
        return
      }

      const s = currentWeekStart.value.format('YYYY-MM-DD')
      const e = currentWeekStart.value.add(6, 'day').format('YYYY-MM-DD')
      
      // 🆕 加载时段配置
      await loadPeriodConfig()
      
      // 先请求节假日（所有视图都需要）
      try {
        const holidayRes: any = await queryHoliday({ sdate: s, edate: e })
        const holidayMapTemp: Record<string, boolean> = {}
        const holidays: any[] = (holidayRes && (holidayRes.Data?.Data || [])) || []
        holidays.forEach((h: any) => {
          const k = dayjs(h.Date).format('YYYY-MM-DD')
          if (k && k !== 'Invalid Date') {
            holidayMapTemp[k] = true
          }
        })
        holidayMap.value = holidayMapTemp
      } catch (error) {
        console.error('加载节假日数据失败:', error)
      }

      // 如果assignList为空，不发起课程数据请求，但保留节假日显示
      if (!props.assignList || props.assignList.length === 0) {
        visibleTeachers.value = []
        currentDisplayCount.value = DEFAULT_DISPLAY_COUNT
        remainingCount.value = 0
        showViewMore.value = false
        dataMap.value = {}
        // 清理教师信息集合，避免切换视图时残留旧数据
        teacherIdSet.value = new Map()
        internalLoading.value = false
        resolve()
        return
      }

      // 根据不同的课表类型调用不同的API
      const fieldNames = getFieldNames(props.timetableType)
      const mergedParams = {
        PageSize: 99999,
        PageIndex: 1,
        StartDate: s,
        EndDate: e,
        sort: 'StartTime',
        desc: 0,
        ExportColumn: exportColumns.value,
        IDList: props.assignList && props.assignList.length ? 
          props.assignList.map((i: any) => i[fieldNames.idField] || i.ID || i.Id) : []
      }

      let apiPromise: Promise<any>
      switch (props.timetableType) {
        case CourseTimetableTypeEnum.ClassTimetable:
          apiPromise = queryCalendarClass(mergedParams)
          break
        case CourseTimetableTypeEnum.ClassroomTimetable:
          apiPromise = queryCalendarClassroom(mergedParams)
          break
        case CourseTimetableTypeEnum.StudentTimetable:
          apiPromise = queryCalendarStudent(mergedParams)
          break
        case CourseTimetableTypeEnum.TeacherTimetable:
        default:
          apiPromise = queryCalendarTeacher(mergedParams)
      }

      internalLoading.value = true
      try {
        const result: any = await apiPromise
        if (result && result.Data) {
          processData(result.Data)
        }
        resolve()
      } catch (error) {
        console.error('加载数据失败:', error)
        reject(error)
      } finally {
        internalLoading.value = false
      }
    }, 300)
  })
}

// 添加项目到数据映射
const addItemToDataMap = (objectId: string, item: any) => {
  // 生产环境专用数据有效性检查 - 防止异常数据导致崩溃
  if (!objectId || !item) return
  if (!item.StartTime) return
  
  // 检查日期有效性
  const startTime = dayjs(item.StartTime)
  if (!startTime.isValid()) return
  
  // 获取日期
  const date = startTime.format('YYYY-MM-DD')
  
  // 获取时段
  let periodKey = 'morning'
  
  // 如果是byRange模式，根据自定义时段确定归属
  if (props.viewBy === 'byRange') {
    // 将开始时间格式化为HH:mm字符串
    const timeStr = startTime.format('HH:mm')
    // 将开始时间转换为分钟数，用于与默认时段的比较
    const startTimeMinutes = startTime.hour() * 60 + startTime.minute()
    
    // 查找开始时间所在的时段
    let matchingPeriod = null
    
    // 先按当前逻辑查找匹配的时段
    for (let i = 0; i < timePeriods.value.length; i++) {
      const period = timePeriods.value[i]
      
      // 根据时段的startTime类型选择不同的比较方式
      if (typeof period.startTime === 'string' && typeof period.endTime === 'string') {
        // 自定义时段：使用字符串比较
        if (timeStr >= period.startTime && timeStr < period.endTime) {
          matchingPeriod = period
          break
        }
      } else {
        // 默认时段：使用分钟数比较
        const periodStart = Number(period.startTime)
        const periodEnd = Number(period.endTime)
        const periodStartMinutes = periodStart * 60
        const periodEndMinutes = periodEnd * 60
        
        if (startTimeMinutes >= periodStartMinutes && startTimeMinutes < periodEndMinutes) {
          matchingPeriod = period
          break
        }
      }
    }
    
    // 优化处理：检查是否是边界时间，根据区间连续性决定归属
    if (!matchingPeriod && timePeriods.value.length > 0) {
      // 先检查是否是某个时段的开始时间
      for (let i = 0; i < timePeriods.value.length; i++) {
        const period = timePeriods.value[i]
        let periodStartTime: string | number = period.startTime
        
        if (typeof periodStartTime === 'string') {
          if (timeStr === periodStartTime) {
            // 开始时间边界
            if (i > 0) {
              // 检查前一个时段的结束时间是否与当前时段的开始时间连续
              const previousPeriod = timePeriods.value[i - 1]
              let previousPeriodEndTime: string | number = previousPeriod.endTime
              
              if (typeof previousPeriodEndTime === 'string') {
                // 字符串比较
                if (previousPeriodEndTime === periodStartTime) {
                  // 连续区间：归属到当前时段（下一个区间）
                  matchingPeriod = period
                } else {
                  // 非连续区间：归属到前一个时段
                  matchingPeriod = previousPeriod
                }
              } else {
                // 数字转换为分钟数比较
                const previousEndMinutes = Number(previousPeriodEndTime) * 60
                const currentStartMinutes = Number(periodStartTime) * 60
                if (previousEndMinutes === currentStartMinutes) {
                  // 连续区间：归属到当前时段（下一个区间）
                  matchingPeriod = period
                } else {
                  // 非连续区间：归属到前一个时段
                  matchingPeriod = previousPeriod
                }
              }
            } else {
              // 第一个时段的开始时间边界，直接归属
              matchingPeriod = period
            }
            break
          }
        } else {
          // 数字类型的时段开始时间
          const periodStartMinutes = Number(periodStartTime) * 60
          if (startTimeMinutes === periodStartMinutes) {
            // 开始时间边界
            if (i > 0) {
              // 检查前一个时段的结束时间是否与当前时段的开始时间连续
              const previousPeriod = timePeriods.value[i - 1]
              const previousPeriodEndTime = Number(previousPeriod.endTime) * 60
              if (previousPeriodEndTime === periodStartMinutes) {
                // 连续区间：归属到当前时段（下一个区间）
                matchingPeriod = period
              } else {
                // 非连续区间：归属到前一个时段
                matchingPeriod = previousPeriod
              }
            } else {
              // 第一个时段的开始时间边界，直接归属
              matchingPeriod = period
            }
            break
          }
        }
      }
      
      // 如果还没找到匹配，检查是否是某个时段的结束时间
      if (!matchingPeriod) {
        for (let i = 0; i < timePeriods.value.length; i++) {
          const period = timePeriods.value[i]
          let periodEndTime: string | number = period.endTime
          
          if (typeof periodEndTime === 'string') {
            if (timeStr === periodEndTime) {
              // 结束时间边界 - 直接归属于当前时段
              matchingPeriod = period
              break
            }
          } else {
            const periodEndMinutes = Number(periodEndTime) * 60
            if (startTimeMinutes === periodEndMinutes) {
              // 结束时间边界 - 直接归属于当前时段
              matchingPeriod = period
              break
            }
          }
        }
      }
    }
    
    // 如果找到匹配的时段，使用其key；否则直接返回，不将该课程添加到任何时段中
    if (matchingPeriod) {
      periodKey = matchingPeriod.key
    } else {
      return // 没有匹配的时段，不添加该课程
    }
  } else {
    // 其他模式使用固定时段划分
    const hour = startTime.hour()
    if (hour >= 12 && hour < 18) periodKey = 'afternoon'
    else if (hour >= 18) periodKey = 'evening'
  }
  
  // 初始化数据结构
  if (!dataMap.value[objectId]) {
    dataMap.value[objectId] = {}
  }
  if (!dataMap.value[objectId][date]) {
    dataMap.value[objectId][date] = {}
  }
  if (!dataMap.value[objectId][date][periodKey]) {
    dataMap.value[objectId][date][periodKey] = []
  }
  
  // 添加数据
  dataMap.value[objectId][date][periodKey].push(item)
}

// 获取字段名称映射
const getFieldNames = (timetableType: CourseTimetableTypeEnum) => {
  switch (timetableType) {
    case CourseTimetableTypeEnum.TeacherTimetable:
      return {
        idField: 'TeacherID',
        nameField: 'TeacherName',
        avatarField: 'TeacherPhoto'
      }
    case CourseTimetableTypeEnum.StudentTimetable:
      return {
        idField: 'StudentID',
        nameField: 'StudentName',
        avatarField: 'StudentPhoto'
      }
    case CourseTimetableTypeEnum.ClassTimetable:
      return {
        idField: 'ClassID',
        nameField: 'ClassName'
      }
    case CourseTimetableTypeEnum.ClassroomTimetable:
      return {
        idField: 'ClassroomID',
        nameField: 'ClassroomName'
      }
    default:
      return {
        idField: 'ID',
        nameField: 'Name'
      }
    }
  }

// 处理数据
const processData = (data: any[]) => {
  // 清空数据映射
  dataMap.value = {}
  // 重置显示数量
  currentDisplayCount.value = DEFAULT_DISPLAY_COUNT
  // 清空教师信息集合
  teacherIdSet.value = new Map()
  
  const fieldNames = getFieldNames(props.timetableType)
  
  // 根据assignList是否有值来决定处理方式
  if (props.assignList && props.assignList.length > 0) {
    // 情况1：assignList有值，以assignList为基准
    props.assignList.forEach((assignItem: any) => {
      const objectId = String(assignItem[fieldNames.idField] || assignItem.ID || assignItem.Id)
      const objectName = assignItem[fieldNames.nameField] || assignItem.Name || '未知'
      const avatar = fieldNames.avatarField ? assignItem[fieldNames.avatarField] : ''
      // 先设置对象信息
      teacherIdSet.value.set(objectId, {
        id: objectId,
        name: objectName,
        avatar: avatar || assignItem['Photo'],
        campusId: assignItem.CampusID || assignItem.CampusList || ''
      })
      // 从API返回的数据中查找对应的排课信息
      const matchedItem = data.find((item: any) => String(item[fieldNames.idField]) === objectId)
      
      if (matchedItem) {
        // 处理排课数据
        if (Array.isArray(matchedItem.CourseList)) {
          matchedItem.CourseList.forEach((course: any) => {
            // 保留对象信息，便于卡片展示需要
            const payload = {
              ...course,
              [fieldNames.idField]: objectId,
              [fieldNames.nameField]: objectName,
              type: 'course' // 标记为排课
            }
            addItemToDataMap(objectId, payload)
          })
        }
        
        // 处理老师日程数据
        if (Array.isArray(matchedItem.ScheduleList)) {
          matchedItem.ScheduleList.forEach((schedule: any) => {
            // 保留对象信息，便于卡片展示需要
            const payload = {
              ...schedule,
              [fieldNames.idField]: objectId,
              [fieldNames.nameField]: objectName,
              type: 'schedule' // 标记为日程
            }
            addItemToDataMap(objectId, payload)
          })
        }
      }
    })
  } else {
    // 情况2：assignList没有值，按原有逻辑处理API返回的数据
    data.forEach((item: any) => {
      const objectId = String(item[fieldNames.idField])
      const objectName = item[fieldNames.nameField]
      const avatar = fieldNames.avatarField ? item[fieldNames.avatarField] : ''
      const campusId = item.CampusID || item.CampusList || ''
      teacherIdSet.value.set(objectId, {
        id: objectId,
        name: objectName,
        avatar: avatar,
        campusId: campusId
      })
      
      // 处理排课数据
      if (Array.isArray(item.CourseList)) {
        item.CourseList.forEach((course: any) => {
          const payload = {
            ...course,
            [fieldNames.idField]: objectId,
            [fieldNames.nameField]: objectName,
            type: 'course' // 标记为排课
          }
          addItemToDataMap(objectId, payload)
        })
      }
      
      // 处理老师日程数据
      if (Array.isArray(item.ScheduleList)) {
        item.ScheduleList.forEach((schedule: any) => {
          const payload = {
            ...schedule,
            [fieldNames.idField]: objectId,
            [fieldNames.nameField]: objectName,
            type: 'schedule' // 标记为日程
          }
          addItemToDataMap(objectId, payload)
        })
      }
    })
  }
  
  // 构建可见的教师列表
  buildVisibleTeachers()
}

// 构建可见的教师列表
const buildVisibleTeachers = () => {
  let allTeachers: Teacher[] = []
  
  // 获取当前课表类型对应的字段名
  const fieldNames = getFieldNames(props.timetableType)
  
  // 对于学员视图，直接使用assignList构建教师列表
  if (props.timetableType === CourseTimetableTypeEnum.StudentTimetable && props.assignList) {
    allTeachers = props.assignList.map(item => ({
      // 优先使用getFieldNames返回的标准字段名，添加备选字段以提高兼容性
      id: item[fieldNames.idField]|| item.ID || '未知ID',
      name: item[fieldNames.nameField] || item.Name || '未知',
      avatar: fieldNames.avatarField ? item[fieldNames.avatarField] || item.Photo || '' : item.Photo || '',
      campusId: item.CampusID || item.CampusList || ''
    }))
  } else {
    // 对于其他视图，优先使用teacherIdSet中的信息
    const teachersFromData: Teacher[] = []
    
    // 如果teacherIdSet中有数据，优先使用它
    if (teacherIdSet.value.size > 0) {
      teacherIdSet.value.forEach(teacher => {
        teachersFromData.push(teacher)
      })
    } else {
      // 如果teacherIdSet为空，从数据映射中提取所有教师
      Object.keys(dataMap.value).forEach(id => {
        // 从数据中获取教师信息
        let name = '未知'
        let avatar = ''
        
        // 遍历数据找到教师信息
        Object.values(dataMap.value[id]).forEach(dateMap => {
          Object.values(dateMap).forEach(periodMap => {
            periodMap.forEach(item => {
              if (item.type === 'course') {
                // 使用统一的字段名获取教师信息
                name = item[fieldNames.nameField] || name
                if (fieldNames.avatarField) {
                  avatar = item[fieldNames.avatarField] || avatar
                }
              }
            })
          })
        })
        
        teachersFromData.push({
          id,
          name,
          avatar
        })
      })
    }
    
    allTeachers = teachersFromData
  }
  
  // 更新可见教师列表和剩余数量
  visibleTeachers.value = allTeachers.slice(0, currentDisplayCount.value)
  remainingCount.value = allTeachers.length - currentDisplayCount.value
  showViewMore.value = remainingCount.value > 0
  
  // 如果数据超过最大显示数量，显示提示
  if (visibleTeachers.value.length > MAX_DISPLAY_COUNT) {
    overLimitTipVisible.value = true
  }
}

// 获取单元格项目
const getCellItemsByPeriod = (objectId: string | number, date: string, period: string) => {
  const items = dataMap.value[objectId]?.[date]?.[period] || []
  // 按开始时间排序 - 添加时间有效性检查，避免无效日期导致排序错误
  return items.sort((a, b) => {
    const timeA = a.StartTime ? new Date(a.StartTime).getTime() : -Infinity
    const timeB = b.StartTime ? new Date(b.StartTime).getTime() : -Infinity
    return timeA - timeB
  })
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

// 处理点击外部区域隐藏popover
const handleClickOutside = (event: Event, item: any) => {
    if ((selectedCourseData.value?.CourseID || selectedCourseData.value?.ID) === (item.CourseID || item.ID)) return
    const target = event.target as Element
    const popoverElement = document.querySelector('.course-detail-popover')
    if (popoverElement && popoverElement.contains(target)) return
    hidePopover()
}

// 隐藏popover
const hidePopover = () => {
    if (courseDetailPopoverRef.value) {
        courseDetailPopoverRef.value.hide()
    }
}

// 安全格式化时间
const safeFormatTime = (timeStr: string, format: string = 'HH:mm') => {
    if (!timeStr) return ''
    const time = dayjs(timeStr)
    return time.isValid() ? time.format(format) : ''
}

// 双击添加排课
const handleDoubleClickAddCourse = (object: any, date: string, period: TimePeriod) => {
  const eventData:any = {
    object,
    date,
    timetableType: props.timetableType
  }
  
  // 当viewBy为'byRange'时，传递对应格子的timeRange
  if (props.viewBy === 'byRange') {
    eventData.timeRange = {
      startTime: String(period.startTime),
      endTime: String(period.endTime)
    }
  }
  if (object?.campusId) {
    eventData.campusId = object.campusId
  }
  emit('canlendar-dbclick-add-course', eventData)
}

// 查看更多
const handleViewMoreClick = () => {
  // 增加显示数量
  currentDisplayCount.value += INCREMENT_COUNT
  
  // 获取所有教师数据
  let allTeachers: Teacher[] = []
  const fieldNames = getFieldNames(props.timetableType)
  
  if (props.timetableType === CourseTimetableTypeEnum.StudentTimetable && props.assignList) {
    allTeachers = props.assignList.map(item => ({
      id: item[fieldNames.idField] || item.ID || '未知ID',
      name: item[fieldNames.nameField] || item.Name || '未知',
      avatar: fieldNames.avatarField ? item[fieldNames.avatarField] || item.Photo || '' : item.Photo || '',
      campusId: item.CampusID || item.CampusList || ''
    }))
  } else {
    // 优先使用teacherIdSet中的信息，与buildVisibleTeachers保持一致
    let teachersFromData: Teacher[] = []
    
    // 如果teacherIdSet中有数据，优先使用它
    if (teacherIdSet.value.size > 0) {
      teacherIdSet.value.forEach(teacher => {
        teachersFromData.push(teacher)
      })
    } else {
      // 如果teacherIdSet为空，从数据映射中提取所有教师
      teachersFromData = Object.keys(dataMap.value).map(id => {
        let name = '未知'
        let avatar = ''
        
        Object.values(dataMap.value[id]).forEach(dateMap => {
          Object.values(dateMap).forEach(periodMap => {
            periodMap.forEach(item => {
              if (item.type === 'course') {
                name = item[fieldNames.nameField] || name
                if (fieldNames.avatarField) {
                  avatar = item[fieldNames.avatarField] || avatar
                }
              }
            })
          })
        })
        
        return {
          id,
          name,
          avatar
        }
      })
    }
    
    allTeachers = teachersFromData
  }
  
  // 检查是否超过最大显示数量
  if (currentDisplayCount.value >= MAX_DISPLAY_COUNT) {
    currentDisplayCount.value = MAX_DISPLAY_COUNT
    overLimitTipVisible.value = true
  }
  
  // 更新可见教师列表和剩余数量
  visibleTeachers.value = allTeachers.slice(0, currentDisplayCount.value)
  remainingCount.value = allTeachers.length - currentDisplayCount.value
  showViewMore.value = remainingCount.value > 0
}

// 点击学员更多按钮
const openStudentPopover = async (event: MouseEvent, student: any) => {
  const target = event.currentTarget as HTMLElement
  // 适配字段以匹配StudentDetailPopover组件的预期
  selectedStudent.value = {
    ID: student.id || student.ID,
    Name: student.name || student.Name,
    Photo: student.avatar || student.Photo,
    CampusID: student.campusId || student.CampusID,
  }
  studentVirtualRef.value = target
  await nextTick() // 确保DOM更新后再显示弹窗
  studentPopoverRef.value?.show()
}

// 点击学员更多按钮外部
const handleStudentClickOutside = (event: MouseEvent, student: any) => {
  studentPopoverRef.value?.hide()
}

// 处理点名
const handleRollCall = (courseData: any) => {
  // 这里可以添加点名逻辑
}

// 🚀 性能优化：使用事件委托处理表格拖拽（减少事件监听器数量）
// 查找最近的格子单元格
const findGridCell = (target: HTMLElement): HTMLElement | null => {
  let current: HTMLElement | null = target
  while (current && current !== document.body) {
    if (current.classList.contains('grid-day-column')) {
      return current
    }
    current = current.parentElement
  }
  return null
}

// 从单元格元素提取数据
const extractCellData = (cell: HTMLElement) => {
  const teacherId = cell.dataset.teacherId
  const dateKey = cell.dataset.dateKey
  const dayIndex = parseInt(cell.dataset.dayIndex || '0', 10)
  const periodKey = cell.dataset.periodKey
  const periodStart = cell.dataset.periodStart
  const periodEnd = cell.dataset.periodEnd
  
  // 查找对应的教师和时段对象
  const teacher = visibleTeachers.value.find(t => String(t.id) === String(teacherId))
  const period = timePeriods.value.find(p => p.key === periodKey)
  
  return {
    teacher,
    dateKey,
    dayIndex,
    period
  }
}

// 🆕 表格拖拽悬停（事件委托）
const handleTableDragOver = (event: DragEvent) => {
  // 只有老师课表才接受拖拽
  if (props.timetableType !== CourseTimetableTypeEnum.TeacherTimetable) return
  
  // 查找最近的格子单元格
  const cell = findGridCell(event.target as HTMLElement)
  if (!cell) return
  
  event.preventDefault()
  event.stopPropagation()
  
  // 设置拖拽效果
  event.dataTransfer!.dropEffect = 'copy'
  document.body.style.cursor = 'grabbing'
  
  isDragOver.value = true
  
  // 提取格子数据
  const cellData = extractCellData(cell)
  if (!cellData.period) return
  
  // 🚀 性能优化：使用 RAF + 节流批处理更新
  dragOptimization.pendingUpdate = {
    day: cellData.dayIndex,
    periodKey: cellData.period.key,
    period: cellData.period,
    dateKey: cellData.dateKey
  }
  
  // 如果已经有待处理的 RAF，直接返回（节流）
  if (dragOptimization.rafId) return
  
  // 使用 RAF 批处理更新
  dragOptimization.rafId = requestAnimationFrame(() => {
    const now = performance.now()
    
    // 节流：如果距离上次更新时间太短，跳过本次更新
    if (now - dragOptimization.lastUpdateTime < dragOptimization.minUpdateInterval) {
      dragOptimization.rafId = null
      return
    }
    
    dragOptimization.lastUpdateTime = now
    
    if (!dragOptimization.pendingUpdate) {
      dragOptimization.rafId = null
      return
    }
    
    // 更新拖拽目标状态
    dragTargetCell.value = dragOptimization.pendingUpdate
    
    dragOptimization.pendingUpdate = null
    dragOptimization.rafId = null
  })
}

// 🆕 表格拖拽离开（事件委托）
const handleTableDragLeave = (event: DragEvent) => {
  if (props.timetableType !== CourseTimetableTypeEnum.TeacherTimetable) return
  
  // 只有当真正离开表格容器时才清理（避免在格子间移动时误触发）
  const relatedTarget = event.relatedTarget as HTMLElement
  const container = event.currentTarget as HTMLElement
  
  if (relatedTarget && container.contains(relatedTarget)) {
    return // 还在容器内，不清理
  }
  
  event.preventDefault()
  document.body.style.cursor = ''
  isDragOver.value = false
  dragTargetCell.value = null
  
  // 🚀 性能优化：清理 RAF
  if (dragOptimization.rafId) {
    cancelAnimationFrame(dragOptimization.rafId)
    dragOptimization.rafId = null
  }
  dragOptimization.pendingUpdate = null
}

// 🆕 表格拖拽放置（事件委托）
const handleTableDrop = (event: DragEvent) => {
  if (props.timetableType !== CourseTimetableTypeEnum.TeacherTimetable) return
  
  // 查找最近的格子单元格
  const cell = findGridCell(event.target as HTMLElement)
  if (!cell) return
  
  event.preventDefault()
  event.stopPropagation()
  
  document.body.style.cursor = ''
  isDragOver.value = false
  dragTargetCell.value = null
  
  // 🚀 性能优化：清理 RAF
  if (dragOptimization.rafId) {
    cancelAnimationFrame(dragOptimization.rafId)
    dragOptimization.rafId = null
  }
  dragOptimization.pendingUpdate = null
  
  // 提取格子数据
  const cellData = extractCellData(cell)
  if (!cellData.teacher || !cellData.period) return
  
  try {
    const data = event.dataTransfer!.getData('application/json')
    if (!data) return
    
    const dragData = JSON.parse(data)
    if (dragData.type !== 'schedule-student') return
    
    // 获取老师信息
    const teacherInfo = props.assignList?.[0]
    if (!teacherInfo) {
      ElMessage.warning('未找到老师信息')
      return
    }
    
    // 构建排课数据
    const scheduleData: any = {
      date: cellData.dateKey,
      timetableType: CourseTimetableTypeEnum.StudentTimetable,
      // 学员信息
      object: {
        id: dragData.student.ID,
        name: dragData.student.Name,
        avatar: dragData.student.Avatar,
        campusId: dragData.student.CampusID
      },
      campusId: dragData.student.CampusID,
      // 老师信息（用于自动填充）
      teacher: {
        id: teacherInfo.ID,
        name: teacherInfo.Name,
        avatar: teacherInfo.Image || teacherInfo.Photo
      }
    }
    
    // 🔑 关键区别：范围模式填充时间段，时段模式不填充时间
    if (props.viewBy === 'byRange') {
      scheduleData.timeRange = {
        startTime: String(cellData.period.startTime),
        endTime: String(cellData.period.endTime)
      }
    }
    // 时段模式(byPeriod)不设置 timeRange，让表单自己选择时间
    
    // 触发排课事件
    emit('canlendar-dbclick-add-course', scheduleData)
    
    // ElMessage.success(`已为 ${dragData.student.Name} 和 ${teacherInfo.Name} 创建排课`)
    
  } catch (error) {
    console.error('拖拽排课失败:', error)
    ElMessage.error('拖拽排课失败，请重试')
  }
}

// 检查是否是过去的日期
const isPastDay = (dateKey: string) => {
  return dayjs(dateKey).isBefore(dayjs(), 'day')
}

// 获取教师的课程数量
const getTeacherCourseCount = (teacherId: string | number) => {
  let count = 0
  Object.values(dataMap.value[teacherId] || {}).forEach(dateMap => {
    Object.values(dateMap).forEach(periodMap => {
      count += periodMap.length
    })
  })
  return count
}

// 监听属性变化
watch(
  [() => props.currentWeek, () => props.externalData, () => props.viewBy],
  async () => {
    // 🆕 对比模式：处理数据
    await processExternalData()
    // 数据加载完成后，等待DOM更新完成再调整列宽
    nextTick(() => {
      updateColumnWidth()
    })
  },
  { deep: true, immediate: true }
)

// 计算并设置合适的列宽
const updateColumnWidth = () => {
  // 🆕 对比模式下不需要动态计算宽度，使用CSS控制
  if (props.externalData) return
  
  // 最小列宽
  const minColumnWidth = 128
  // 🆕 对比模式：固定列总宽度只有时段列（70px）
  const fixedColumnsWidth = 70
  // 获取容器宽度
  if (!viewContainerRef.value) return
  const viewContainer = viewContainerRef.value
  
  // 使用clientWidth而不是offsetWidth，因为clientWidth不包含滚动条宽度
  const containerWidth = viewContainer.clientWidth
  // 计算可用宽度
  const availableWidth = containerWidth - fixedColumnsWidth
  // 计算每列宽度
  const calculatedWidth = availableWidth / weekDays.value.length
  // 确保不小于最小宽度
  const columnWidth = Math.max(calculatedWidth, minColumnWidth)
  
  // 只设置当前组件内的header-cell和grid-day-column的宽度
  const headerCells = viewContainer.querySelectorAll('.header-cell') as NodeListOf<HTMLElement>
  const gridCells = viewContainer.querySelectorAll('.grid-day-column') as NodeListOf<HTMLElement>
  
  headerCells.forEach(cell => {
    cell.style.width = `${columnWidth}px`
  })
  
  gridCells.forEach(cell => {
    cell.style.width = `${columnWidth}px`
  })
}

// 监听weekDays变化，重新计算列宽
watch(() => weekDays.value.length, () => {
  nextTick(() => {
    updateColumnWidth()
  })
})

// 组件容器ref
const viewContainerRef = ref<HTMLElement | null>(null)

// 组件挂载时设置列宽
onMounted(async () => {
  // 🆕 对比模式：不在 onMounted 中加载数据，完全依赖 watch 监听 externalData 变化
  // 因为组件挂载时 props.externalData 可能还没有传递过来
  // watch 会在 externalData 有值后自动触发
  nextTick(() => {
    updateColumnWidth()
  })
})

// 监听关键属性变化，重新加载数据
// watch(() => [props.currentWeek, props.searchParams, props.timetableType, props.assign, props.assignList], async (nv, ov) => {
//   // 当视图类型、周次、搜索参数、分配状态或分配列表变化时，重新加载数据
//   await loadData(true)
//   await nextTick()
//   updateColumnWidth()
// }, { deep: true, immediate: false })

// 窗口大小变化时重新计算列宽
window.addEventListener('resize', () => {
  nextTick(() => {
    updateColumnWidth()
  })
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', updateColumnWidth)
})

// 🆕 暴露刷新配置方法给父组件
defineExpose({
  refreshPeriodConfig: async () => {
    // 强制重新加载时段配置并处理数据
    await processExternalData()
  }
})
</script>

<style scoped lang="scss">
.period-calendar-view {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
  &.period-loading{
    overflow: hidden;
  }
  
  // 🆕 对比模式：移除内部滚动，让外层容器控制滚动
  &.is-compare {
    overflow: visible;
    height: auto;
    min-height: auto;
    
    // 确保表格容器充满宽度
    .calendar-container {
      width: 100%;
      overflow: visible;
    }
  }
}

.calendar-container {
  width: 100%;
  min-height: 100%;
  position: relative;
  padding-bottom: 16px;
}

.period-calendar-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

/* 🆕 对比模式下表格设置 */
.period-calendar-view.is-compare .period-calendar-table {
  table-layout: fixed; /* 固定布局，让列宽可控 */
  width: 100%; /* 确保表格充满容器 */
}

/* 🆕 对比模式下的列宽固定且均分 */
.period-calendar-view.is-compare {
  // 确保容器和表格都充满宽度
  .calendar-container {
    width: 100%;
    overflow: visible;
  }
  
  .period-calendar-table {
    width: 100% !important; // 强制表格充满宽度
    min-width: auto; // 移除最小宽度限制
  }
  
  .header-cell,
  .grid-day-column {
    /* 使用calc计算：(100% - 时段列70px) / 7天 */
    width: calc((100% - 70px) / var(--week-days-count, 7)) !important;
    min-width: 0; /* 允许缩小 */
    max-width: none; /* 移除最大宽度限制 */
  }
  
  .period-header {
    width: 70px !important;
    min-width: 70px;
    max-width: 70px;
  }
  
  .period-label-cell {
    width: 70px !important;
    min-width: 70px;
    max-width: 70px;
  }
}

// 表格头部
.thead {
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
}



.period-header {
  width:70px;
  min-width: 70px;
  max-width: 70px;
  background: #f5f7fa;
  padding: 4px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  position: relative;
  .flex-center{
    justify-content: center;
  }
  /* 使用伪元素实现底部边框 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left:69px;
    width: 100%;
    height: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
  &::before {
    content: "";
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
}

.object-header {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
  background: #f5f7fa;
  padding: 4px;
  text-align: center;
  font-weight: 600;
  font-size: 14px;
  position: relative;
  .flex-center{
    justify-content: center;
  }
  /* 使用伪元素实现底部边框 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 119px;
    width: 100%;
    height: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
  &::before {
    content: "";
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
}

.setting-btn{
  padding: 3px 3px;
  border-radius: 3px;
  width: 22px;
  height: 22px;
  &:hover{
    background:  rgba(0,0,0,0.06);
  }
}

.header-cell {
  background: #f5f7fa;
  padding: 4px;
  position: relative;
  /* 使用伪元素实现右侧边框 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    height: 100%;
    width: 1px;
    background-color: #e4e7ed;
    z-index: 12;
  }
  /* 使用伪元素实现底部边框 */
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
  .header-cell-box{
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 8px 4px;
    font-weight: 600;
    color: #303133;
  }
  .header-week {
    font-size: 14px;
  }
  .header-date {
    width: 20px;
    height: 20px;
    line-height: 20px;
    text-align: center;
    color: #C0C4CC;
    font-size:12px;
    font-weight: 400;
  }
  .header-date.circle {
    background: #2878E8;
    color: #fff;
    border-radius: 50%;
    font-weight: 400;
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
}









.sticky {
  position: sticky;
  top: 0;
  z-index: 10;
}

.object-header.sticky {
  left: 0;
  z-index: 13;
  /* 添加伪元素模拟右侧边框，确保滚动时可见 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 1px;
    height: 100%;
    background-color: #E4E7ED;
    z-index: 12;
  }
}

.period-header.sticky {
  left: 0;  /* 🆕 对比模式：没有对象列，固定在最左侧 */
  z-index: 13;
  /* 添加伪元素模拟右侧边框，确保滚动时可见 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 1px;
    height: 100%;
    background-color: #E4E7ED;
    z-index: 12;
  }
}

/* 校区课表时，时段列固定在最左侧 */
.period-calendar-view {
  &[data-timetable-type="TimeTimetable"] {
    .period-header.sticky {
      left: 0;
    }
    .period-label-cell {
      left: 0;
    }
  }
}

.header-cell.sticky {
  z-index: 10;
}

// 表格主体
.tbody {
  position: relative;
}

.loading-cell {
  padding: 40px;
  text-align: center;
  position: relative;
  /* 使用伪元素实现底部边框 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #e4e7ed;
    z-index: 12;
  }
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.teacher-cell-wrap{
  width: 120px;
  min-width: 120px;
  max-width: 120px;
  position: sticky;
  left: 0;
  background-color: #fff;
  z-index: 11;
  /* 使用伪元素实现底部边框 */
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
  /* 使用伪元素模拟右侧边框，确保滚动时可见 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
}

.teacher-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 4px;
    box-sizing: border-box;
    word-break: break-all;
    .more-btn,.student-more-btn{
        cursor: pointer;
        .default-icon{
            display: block;
        }
      
    }
    .student-more-btn{
      display: flex;
      align-items: center;
      justify-content: center;
      width: 18px;
      height: 18px;
      background-color: #F0F0F0;
      border-radius: 50%;
      &:hover{
        background-color: var(--wtwo-color-primary);
      }
    }
} 


.avatar {
    width: 36px;
    height: 36px;
    border-radius: 4px;
    overflow: hidden;
    background: #f2f3f5;
    flex-shrink: 0;
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

.teacher-info {
    min-width: 0;
    // margin-top: 10px;
}

.teacher-name {
    font-weight: 600;
    color: #303133;
    line-height: 20px;
    text-align: center;
    font-size: 13px;
    white-space: normal !important;
    word-break: break-all !important;
    overflow-wrap: break-word !important;
    word-wrap: break-word;
    hyphens: auto;
}

.teacher-course-num{
    background: #F0F0F0;
    border-radius: 40px;
    min-width: 41px;
    text-align: center;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    color: #606266;
}

.more-btn {
  margin-left: 4px;
  cursor: pointer;
  color: #909399;
}

.more-btn:hover {
  color: #409eff;
}

.period-label-cell {
  width: 70px;
  min-width: 70px;
  max-width: 70px;
  padding: 6px 4px;
  text-align: center;
  font-size: 14px;
  line-height: 16px;
  color: #606266;
  position: sticky;
  left: 0;  /* 🆕 对比模式：没有对象列，固定在最左侧 */
  background-color: #fff;
  z-index: 11;
  /* 使用伪元素实现底部边框 */
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #e4e7ed;
    z-index: 12;
  }
  /* 使用伪元素模拟右侧边框，确保滚动时可见 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 1px;
    background-color: #e4e7ed;
    z-index: 12;
  }
}

.grid-day-column {
  padding: 0;
  height: 34px;
  min-height: 0px;
  position: relative;
  /* 使用伪元素实现右侧边框 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    height: 100%;
    width: 1px;
    background-color: #e4e7ed;
    z-index: 1;
  }
  /* 使用伪元素实现底部边框 */
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background-color: #e4e7ed;
    z-index: 1;
  }
}

.grid-cell {
  width: 100%;
  height: 100%;
  padding: 4px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.grid-cell.is-past {
  background-color: #f9fafc;
}

.double-click-add-course {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  min-height: 24px;
  background: #F0F2F5;
  border-radius: 8px;
  font-size: 12px;
  color: #909399;
  font-weight: 600;
  cursor: pointer;
  visibility: hidden;
}

.grid-cell:hover .double-click-add-course {
  visibility: visible;
}

// 🆕 拖拽占位块样式
.drag-target-cell {
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  background-color: rgba(240, 242, 245, 0.9);
  border-radius: 4px;
  backdrop-filter: blur(2px);
  color: #909399;
  outline: 1px dashed #909399;
  outline-offset: -1px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  pointer-events: none;
  
  .drag-target-cell-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
  }
  
  .drag-target-time {
    font-size: 12px;
    font-weight: 500;
  }
  
  .drag-target-label {
    font-size: 12px;
    font-weight: 600;
  }
}

.grid-cell.drag-over {
  background-color: rgba(64, 158, 255, 0.05);
}

// 查看更多按钮
.view-more {
  text-align: center;
  margin-top: 16px;
}

// 超出数量提示对话框
.over-limit-tip {
  padding: 16px 0;
  line-height: 22px;
}

/* 🆕 对比模式样式 */
.period-calendar-view.is-compare {
  width: 100%;
  min-width: auto; /* 取消最小宽度限制，允许自适应 */
  overflow: visible; /* 允许吸顶元素溢出 */
  height: auto;

  .student-header,
  .teacher-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 6px;
    height: 32px;
    background-color: #fafafa;
    border-bottom: 1px solid #f0f0f0;
    // 吸顶设置：相对于组件本身
    position: sticky;
    top: 0;
    z-index: 100;
    flex-shrink: 0;

    .student-info,
    .teacher-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .student-details,
    .teacher-details {
      display: flex;
      align-items: center;
      gap: 4px;

      .student-name,
      .teacher-name {
        font-size: 14px;
        color: #333;
        font-weight: 600;
      }

      .student-id {
        font-size: 12px;
        color: #999;
      }

      .object-count {
        margin-left: 8px;
        padding: 0 6px;
        height: 20px;
        line-height: 20px;
        background: #F0F7FF;
        border-radius: 10px;
        font-size: 12px;
        color: #2878E8;
      }
    }
  }

  .more-btn {
    cursor: pointer;
    .default-icon {
      display: block;
    }
  }

  // 🆕 刷新按钮样式
  .refresh-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    cursor: pointer;
    color: #909399;
    transition: color 0.3s;
    
    &:hover {
      color: #409EFF;
    }
    
    &:active {
      color: #337ecc;
    }
  }

  // 🆕 拖到老师课表按钮样式
  .drag-to-teacher-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: #f0f2f5;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    cursor: grab;
    font-size: 13px;
    color: #606266;
    user-select: none;
    transition: all 0.2s;

    &:active {
      cursor: grabbing;
      background: #dcdfe6;
    }

    span {
      white-space: nowrap;
    }
  }

  // 表格头部也需要吸顶，但要在学员/老师头部下方
  .period-calendar-table thead tr {
    position: sticky;
    top: 32px; // 学员/老师头部的高度
    z-index: 99; // 比学员/老师头部低一层
  }
  
  .period-calendar-table thead th {
    background: white; // 确保有背景色覆盖下方内容
    position: sticky;
    top: 32px;
  }

  // 时段列的sticky定位 - 需要更高的z-index
  .period-header.sticky {
    position: sticky;
    left: 0;
    top: 32px; // 🆕 也要设置top
    z-index: 102; // 🆕 要比thead的th高，这样横向滚动时能覆盖日期列
    background: #f5f7fa; // 确保有背景色
  }
  
  // 日期列头部也需要sticky
  .header-cell.sticky {
    position: sticky;
    top: 32px;
    z-index: 99;
    background: #f5f7fa;
  }

  .period-label-cell {
    position: sticky;
    left: 0;
    z-index: 98; // 比thead低，但比普通单元格高
    background: white; // 确保有背景色
  }
  
  // 🆕 确保表格容器不影响sticky定位
  .calendar-container {
    overflow: visible; // 不设置overflow，让外层容器处理滚动
  }
}

</style>