<template>
  <el-popover
    ref="popoverRef"
    v-model:visible="visible"
    :virtual-ref="virtualRef"
    virtual-triggering
    placement="right-end"
    :width="362"
    :trigger="[]"
    :popper-options="popperOptions"
    popper-class="course-detail-popover"
    :offset="8"
    :fallback-placements="['right', 'right-start', 'left-end','left','left-start','top-start','bottom-start']"
    @show="handleShow"
    @hide="handleHide"
    append-to="body"
    :hide-after="0"
  >
    <!-- 排课类型卡片 -->
    <div v-if="courseData.type !== 'schedule'" class="course-detail-card" @mousedown.stop @click.stop>
      <!-- 头部区域 -->
      <div class="course-header" :style="{ background: `linear-gradient( 90deg, #FFFFFF 0%, ${getTint(headerColor, 0.2)} 85%)` }">
        <div class="course-title ellipsis-single" :title="courseData.ClassName">{{ courseData.ClassName}}</div>
        <div class="course-time">{{ dayjs(courseData.StartTime).format('YYYY-MM-DD') }}<span v-if="courseData.Unit!=3||IsOpenShiftForDay" class="ml-8px">{{ formatTime(courseData.StartTime) }}</span>[{{dayjs(courseData.StartTime).format('dddd') }}]</div>
        
        <div class="status-badge" :style="{ color: headerColor }">
          {{ transToConfigDescript(statusText) }}
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="course-content" v-loading="loading">
        <div class="course-tags">
          <span v-for="tag in enabledRightTags" :key="String(tag.FieldName)" class="tag" v-show="!!getTagText(String(tag.FieldName))">{{ getTagText(String(tag.FieldName)) }}</span>
        </div>
        <div class="content-body">
          <div class="info-row" v-for="field in enabledLeftFields" :key="String(field.FieldName)">
            <span class="label">{{ field.DisplayName }}:</span>
            <!-- 如果是"实到/应到"字段且包含进度信息，显示进度条 -->
              <div v-if="field.FieldName=='StudentAttendanceCount'" class="progress-container">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercent + '%', backgroundColor: '#409EFF' }"></div>
                </div>
                <span class="progress-text">{{ getAttendanceDisplayText() }}</span>
              </div>
            <!-- 普通字段显示 -->
            <span v-else class="value">{{ getFieldValue(String(field.FieldName)) }}</span>
          </div>
        </div>
      </div>

      <!-- 底部按钮区域 -->
      <div class="course-footer flex-between">
        <div class="flex-center main-btns" v-if="courseData.HasCampusPermission===true">
          <div class="course-footer-btn" v-if="courseData && courseData.Finished != 2&&CourseBeginPower" @click="handleRollCall">{{transToConfigDescript('点名上课')}}</div>
          <div class="course-footer-btn" @click="handleAdjustStudents">{{transToConfigDescript('上课学员')}}</div>
          <div class="course-footer-btn" @click="handleScheduleDetails">排课详情</div>
        </div>
        <div v-if="courseData.HasCampusPermission===false" class="text-12px flex-center">
          <el-icon size="14px">
            <svg aria-hidden="true">
              <use xlink:href="#w2-suoding"></use>
            </svg>
          </el-icon>
          <span class="ml-2px">非可操作校区的{{courseData?.type==='course'?'排课':'日程'}}，不可查看与操作！</span>
        </div>
        <div class="flex-center" v-if="courseData.HasCampusPermission===true">
          <el-divider direction="vertical" />
          <el-dropdown placement="top-start" trigger="click" :hide-timeout="0">
            <div class="course-footer-btn"><el-icon><MoreFilled /></el-icon></div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="courseData && courseData.Finished == 0" @click.native="getScanCode(courseData)">签到二维码</el-dropdown-item>
                <el-divider v-if="courseData && courseData.Finished == 0"/>
                <el-dropdown-item v-if="NewCourse_CourseStudentAdjustLesson&&courseData.Finished!=2" @click.native="openAdjustCourse(courseData)">临时调课</el-dropdown-item>
                <el-dropdown-item v-if="NewCourse_CourseBeginCancel&&courseData.Finished!=0&&courseData.Finished!=2" @click.native="disCourse(courseData)">{{transToConfigDescript('撤销上课')}}</el-dropdown-item>
                <el-dropdown-item v-if="EnablePrintOneToOneCourseRecord&&NewCourse_PrintCourseRecord
                  &&courseData.Finished==1&&courseData.IsOneToOne==1" @click="printOneToOne(courseData)">{{transToConfigDescript('打印上课凭证')}}</el-dropdown-item>
                <el-dropdown-item v-if="courseData.Finished!=2" @click.native="printCourseStudentList(courseData)">打印点名表</el-dropdown-item>
                <el-dropdown-item @click.native="copyMainInfo(courseData)">复制主要信息</el-dropdown-item>
                <el-dropdown-item v-if="NewCourse_LookLiveStreamingLink&&IsOpenLiveStream==2&&courseData.LiveStreamingLink" @click.native="openLiveLink(courseData)">查看直播链接</el-dropdown-item>
                <el-divider/>
                <el-dropdown-item v-if="courseData.PlanID && courseData.PlanID !== EMPTYGUID" @click.native="openBatchScheduleDialog(courseData)">管理本批次排课</el-dropdown-item>
                <el-dropdown-item v-if="courseData.Finished!=2 && courseData.Unit!=3" @click.native="editContent(courseData)">{{transToConfigDescript('修改上课内容')}}</el-dropdown-item>
                <el-dropdown-item v-if="NewCourse_CourseEdit&&courseData.Finished === 0" @click.native="showShiftEdit(courseData)">编辑排课</el-dropdown-item>
                <el-dropdown-item v-if="NewCourse_CourseCancel&&courseData.Finished!=1&&courseData.Finished!=2" @click.native="cancel(courseData)">取消排课</el-dropdown-item>
                <el-dropdown-item v-if="NewCourse_CourseDelete&&courseData.Finished!=1" @click.native="delCourse(courseData)">删除排课</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
        </div>
      </div>
    </div>

    <!-- 日程类型卡片 -->
    <div v-else class="schedule-detail-card" @mousedown.stop @click.stop>
      <!-- 日程头部区域 -->
      <div class="schedule-header">
        <div class="schedule-title">{{ courseData.Title || '日程' }}</div>
        <div class="schedule-time">{{ dayjs(courseData.StartTime).format('YYYY-MM-DD') }}<span class="ml-8px">{{ formatTime(courseData.StartTime) }}</span>[{{dayjs(courseData.StartTime).format('dddd') }}]</div>
      </div>
      
      <!-- 日程内容区域 -->
      <div class="schedule-content">
        <div class="course-tags">
          <div class="tag">日程</div>
        </div>
        <div class="schedule-info">
          <div class="info-row">
            <span class="label">参与员工:</span>
            <span class="value ellipsis-single" :title="courseData.TeacherList.map((i:any)=>i.Name).join(',')">{{ courseData.TeacherList.map((i:any)=>i.Name).join(',') || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">{{transToConfigDescript('所属校区:')}}</span>
            <span class="value ellipsis-single" :title="courseData.CampusName">{{ courseData.CampusName }}</span>
          </div>
          <div class="info-row">
            <span class="label">日程类型:</span>
            <span class="value ellipsis-single">{{ courseData.Content }}</span>
          </div>
          <div class="info-row">
            <span class="label">备注:</span>
            <span class="value ellipsis-single" :title="courseData.Remark">{{ courseData.Remark || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 日程底部按钮区域 -->
      <div class="schedule-footer">
        <div class="schedule-placeholder-btns">
          <el-dropdown v-if="NewCourse_ScheduleEdit" placement="bottom" trigger="click" :hide-timeout="0">
            <div class="schedule-btn">编辑</div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="editScheduleOnce">仅编辑本次日程</el-dropdown-item>
                <el-dropdown-item v-if="courseData&&courseData.SchedulePlanID&&courseData.SchedulePlanID!=EMPTYGUID" @click.native="editScheduleAll">编辑全部日程</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-divider v-if="NewCourse_ScheduleEdit&&NewCourse_ScheduleDelete" direction="vertical" />
          <el-dropdown v-if="NewCourse_ScheduleDelete" placement="bottom" trigger="click" :hide-timeout="0">
            <div class="schedule-btn">删除</div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="deleteScheduleOnce">仅删除本次日程</el-dropdown-item>
                <el-dropdown-item v-if="courseData&&courseData.SchedulePlanID&&courseData.SchedulePlanID!=EMPTYGUID" @click.native="deleteScheduleAll">删除全部日程</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElPopover, ElButton, ElIcon } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { queryCourseNew } from '@/api/arrange'
import useEvent from '@/config/event'
import { cloneDeep } from 'lodash'
import { getTint, getCourseStatusColor, getEnabledRightTagsFromPreference, getEnabledLeftFieldsFromPreference, getCourseFieldValue, getCourseTagText, buildExportColumns, buildColorDetailMap } from '@/utils'
import { useConfigs } from '@/store'
import { useSyncCancel } from '@common/tool/http/fetch'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { CourseTimetableTypeEnum, CourseTimetableFieldTypeEnum, type CourseTimetableFieldSetting } from '@/types/model/timetable-preference'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
import { transToConfigDescript } from '@/utils/filters/filters'

const EMPTYGUID = ref('00000000-0000-0000-0000-000000000000')

interface CourseData {
  title?: string
  StartTime?: string
  EndTime?: string
  Finished?: number
  ClassroomName?: string
  TeacherName?: string
  CampusName?: string
  ClassName?: string
  TeachingMethod?: string
  ChapterTitle?: string
  CourseContent?: string
  InternalNotes?: string
  Color?: string
  type?: 'course' | 'schedule' // 区分排课和日程类型
  Content?: string // 日程内容
  Remark?: string // 日程备注
  [key: string]: any
}

interface Props {
  courseData: CourseData
  virtualRef: any
  timetableType?: CourseTimetableTypeEnum | string
  preventAutoHide?: boolean
  needApi?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  courseData: () => ({}),
  virtualRef: null,
  timetableType: CourseTimetableTypeEnum.TeacherTimetable,
  preventAutoHide: false,
  needApi: true
})

// popper 配置
const popperOptions = computed(() => ({
  modifiers: [{
    name: 'eventListeners',
    enabled: !props.preventAutoHide
  }]
}))

const emit = defineEmits<{
  show: []
  hide: []
  // 操作完成后通知外层刷新
  updated: [payload?: { refreshTotal?: boolean }]
}>()
const globalEvent = useEvent()

const configs = computed(() => {
	return useConfigs().configs
})

const IsOpenLiveStream=computed(()=>{ //开启直播教学服务。0:不开启（默认），1：声网。2：classin（自动同步方式）。3：classin(手动同步方式,通过zb.xiaogj.com进行同步)。
	return configs.value.IsOpenLiveStream
})

const IsOpenShiftForDay=computed(()=>{ // 0按月 1按天
	return configs.value.IsOpenShiftForDay==1
})

const EnablePrintOneToOneCourseRecord=computed(()=>{ //打印上课凭证
	return configs.value.EnablePrintOneToOneCourseRecord==1
})


//权限
const NewCourse_CourseDelete=window.$xgj.op("NewCourse_CourseDelete")//删除排课
const NewCourse_CourseCancel=window.$xgj.op("NewCourse_CourseCancel")//取消排课
const NewCourse_CourseStudentAdjustLesson=window.$xgj.op("NewCourse_CourseStudentAdjustLesson")//临时调课
const NewCourse_CourseEdit = window.$xgj.op('NewCourse_CourseEdit') //编辑排课
const NewCourse_ScheduleEdit = window.$xgj.op('NewCourse_ScheduleEdit') //编辑日程
const NewCourse_ScheduleDelete = window.$xgj.op('NewCourse_ScheduleDelete') //删除日程
const CourseBeginPower= window.$xgj.op("NewCourse_CourseBegin") || window.$xgj.op("NewCourse_CourseBeginLimit");// 点名上课
const NewCourse_CourseBeginCancel=window.$xgj.op("NewCourse_CourseBeginCancel")//撤销上课
const NewCourse_LookLiveStreamingLink=window.$xgj.op("NewCourse_LookLiveStreamingLink")//查看直播链接
const NewCourse_PrintCourseRecord=window.$xgj.op("PrintCourseRecord")//打印上课凭证

const popoverRef = ref()
// 无需持有 dropdown 实例，提前隐藏 popover，再在 nextTick 打开对应弹窗，避免菜单在别处短暂闪现
function hidePopoverImmediately(evt?: Event) {
  try {
    evt?.preventDefault()
    evt?.stopPropagation()
  } catch (e) {}
  visible.value = false
}

const visible = ref(false)
const loading = ref(false)
const courseData = ref<CourseData>({})
const lastLoadedCourseId = ref<string>('')
const cancelCurrentRequest = ref<null | (() => void)>(null)
// 防并发：标记最近一次发起的请求序号，避免快切导致旧请求覆盖新数据
const requestSequence = ref(0)

// 状态配置
const statusConfig = {
  0: { text: '未上课', color: '#4893FF' },
  1: { text: '已上课', color: '#67C23A' },
  2: { text: '已取消', color: '#909399' }
}

// 计算属性
const status = computed(() => Number(courseData.value.Finished) || 0)
const statusInfo = computed(() => statusConfig[status.value as keyof typeof statusConfig] || statusConfig[0])
const statusText = computed(() => statusInfo.value.text)
// 偏好设置驱动的头部颜色
const timetablePrefStore = useTimetablePreferences()
const preferenceVm = computed(() => timetablePrefStore.preferenceByType(props.timetableType))
const enabledColorSetting = computed(() => {
  const cs = (preferenceVm.value as any)?.ColorSettings
  return Array.isArray(cs) && cs.length > 0 ? (cs.find((s:any)=>s && s.IsEnabled) || cs[0]) : null
})
const colorDetailMap = computed<Record<string, string>>(() => buildColorDetailMap((enabledColorSetting.value as any)?.ColorDetails))
const headerColor = computed(() => {
  const color = getCourseStatusColor(
    courseData.value || {},
    (enabledColorSetting.value as any)?.SettingType,
    colorDetailMap.value,
    String(props.timetableType || 'TimeTimetable')
  )
  return color
})

const progressPercent = computed(() => {
  // 优先使用 StudentAttendanceCount/StudentCount 计算进度
  const attendanceCount = Number(courseData.value.StudentAttendanceCount) || 0
  const totalCount = Number(courseData.value.StudentCount) || 0
  if (totalCount > 0) {
    return Math.round((attendanceCount / totalCount) * 100)
  }
  
  // 如果没有 StudentCount 数据，回退到 Progress 字段
  const progress = courseData.value.Progress || '0/0'
  const match = progress.match(/(\d+)\/(\d+)/)
  if (match) {
    const current = parseInt(match[1])
    const total = parseInt(match[2])
    return total > 0 ? Math.round((current / total) * 100) : 0
  }
  return 0
})

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '07:30-09:30'
  const start = dayjs(timeStr)
  const end = dayjs(courseData.value.EndTime)
  return `${start.format('HH:mm')}-${end.format('HH:mm')}`
}

// 处理显示
const handleShow = async () => {
  // 如果是日程类型，直接使用传入的数据，不发请求
  if (props.courseData.type === 'schedule') {
    courseData.value = cloneDeep(props.courseData)
    emit('show')
    return
  }
  
  if (props.needApi&&(props.courseData.CourseID||props.courseData.ID)) {
    
    const currentCourseId = String(props.courseData.CourseID || props.courseData.ID)
    
    // 如果当前课程ID与上次加载的相同，且已有数据，则不重新请求
    if (lastLoadedCourseId.value === currentCourseId && courseData.value && Object.keys(courseData.value).length > 0) {
      emit('show')
      return
    }
    courseData.value = props.courseData || {}
    lastLoadedCourseId.value = currentCourseId
    await loadCourseDetail()
    
  } else if (Object.keys(courseData.value).length==0) {
    courseData.value = cloneDeep(props.courseData)
  }
  emit('show')
}

// 处理隐藏
const handleHide = () => {
  // 只在未设置阻止自动隐藏时处理隐藏逻辑
  if (!props.preventAutoHide) {
    // 隐藏时中断当前请求，避免不必要的网络与状态回写
    if (cancelCurrentRequest.value) {
      cancelCurrentRequest.value()
      cancelCurrentRequest.value = null
    }
    emit('hide')
  }
}

// 加载课程详情
const loadCourseDetail = async () => {
  if (!props.courseData) return
  // 记录本次请求的序号和课程ID
  const mySeq = ++requestSequence.value
  const myCourseId = String(props.courseData.CourseID || props.courseData.ID || '')
  loading.value = true
  try {
    // 若存在未完成的请求，先取消
    if (cancelCurrentRequest.value) {
      cancelCurrentRequest.value()
      cancelCurrentRequest.value = null
    }
    
    // 为本次请求创建可取消句柄
    cancelCurrentRequest.value = useSyncCancel()
    const res = await queryCourseNew({
      CourseID: props.courseData.CourseID || props.courseData.ID,
      ExportColumn: exportColumns.value
    })
    // 仅当仍然是最新请求且课程ID未被切换时才写入
    if (mySeq === requestSequence.value && myCourseId === lastLoadedCourseId.value) {
      courseData.value = res.Data&&res.Data.List&&res.Data.List.length?res.Data.List[0] : {}
    }
    console.log(courseData.value)
  } catch (error) {
    // 请求被取消时不回写数据
    if ((error as any)?.name !== 'AbortError') {
      console.error('加载课程详情失败:', error)
      if (mySeq === requestSequence.value && myCourseId === lastLoadedCourseId.value) {
        courseData.value = props.courseData || {}
      }
    }
  } finally {
    if (mySeq === requestSequence.value) {
      loading.value = false
      // 本次请求已结束，清理取消句柄
      cancelCurrentRequest.value = null
    }
  }
}

// 事件处理
const handleRollCall = () => {
  hidePopoverImmediately()
  const payload = { type: 'rollCall', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

const handleAdjustStudents = () => {
  hidePopoverImmediately()
  const payload = { type: 'adjustStudents', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

const handleScheduleDetails = () => {
  hidePopoverImmediately()
  const payload = { type: 'scheduleDetails', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

// 监听props变化
watch(() => props.courseData, (newData) => {
  if (newData) {
    courseData.value = newData
  }
}, { deep: true, immediate: true })

function getScanCode(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'getScanCode', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function openAdjustCourse(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'openAdjustCourse', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function disCourse(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'disCourse', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function printCourseStudentList(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'printCourseStudentList', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function printOneToOne(data:any){
  hidePopoverImmediately()
  const payload = { type: 'printOneToOne', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function copyMainInfo(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'copyMainInfo', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function openLiveLink(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'openLiveLink', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function openBatchScheduleDialog(data: any){
    hidePopoverImmediately()
    const payload = { type: 'openBatchScheduleDialog', data: courseData.value }
    globalEvent.emit('arrange-course-detail-action', payload)
}

function editContent(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'editContent', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function showShiftEdit(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'showShiftEdit', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function cancel(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'cancel', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function delCourse(data: any) {
  hidePopoverImmediately()
  const payload = { type: 'delCourse', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

// 日程编辑和删除事件处理
function editScheduleOnce() {
  hidePopoverImmediately()
  const payload = { type: 'editScheduleOnce', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function editScheduleAll() {
  console.log(courseData.value)
  hidePopoverImmediately()
  const payload = { type: 'editScheduleAll', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function deleteScheduleOnce() {
  hidePopoverImmediately()
  const payload = { type: 'deleteScheduleOnce', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}

function deleteScheduleAll() {
  hidePopoverImmediately()
  const payload = { type: 'deleteScheduleAll', data: courseData.value }
  globalEvent.emit('arrange-course-detail-action', payload)
}
// 暴露方法
const show = () => {
  visible.value = true
}

const hide = () => {
  if (!props.preventAutoHide) {
    visible.value = false
  }
}

// 监听 visible 值的变化
watch(visible, (newVal, oldVal) => {
  // 如果是要隐藏弹窗，且设置了阻止自动隐藏，则保持显示状态
  if (!newVal && oldVal && props.preventAutoHide) {
    visible.value = true
  }
}, { flush: 'sync' }) // 使用 sync 确保在值改变的同时就进行处理

defineExpose({
  show,
  hide,
  popoverRef,
  notifyUpdated
})

// 提供一个方法给父组件在相关操作完成后调用，从而触发统一刷新通知
function notifyUpdated(params?: { refreshTotal?: boolean }) {
  emit('updated', params)
  try {
    globalEvent.emit && globalEvent.emit('arrange-course-detail-updated', params)
  } catch (e) {}
}

// 上方已定义 timetable 偏好与 preferenceVm

const enabledRightTags = computed<CourseTimetableFieldSetting[]>(() => {
  return getEnabledRightTagsFromPreference({
    CardShowInformationSettings: preferenceVm.value?.CardShowInformationSettings,
    FieldTypeEnum: { Tag: CourseTimetableFieldTypeEnum.Tag, Main: CourseTimetableFieldTypeEnum.Main }
  }) as unknown as CourseTimetableFieldSetting[]
})

const enabledLeftFields = computed<CourseTimetableFieldSetting[]>(() => {
  return getEnabledLeftFieldsFromPreference({
    CardShowInformationSettings: preferenceVm.value?.CardShowInformationSettings,
    FieldTypeEnum: { Tag: CourseTimetableFieldTypeEnum.Tag, Main: CourseTimetableFieldTypeEnum.Main }
  }) as unknown as CourseTimetableFieldSetting[]
})

// 导出列（公用工具）
const exportColumns = computed<string[]>(() => buildExportColumns(ALL_COLUMNS as unknown as any[]))

function getTagText(fieldName: string): string {
  if (fieldName === 'Finished') return statusText.value
  return getCourseTagText(courseData.value, fieldName)
}

function getFieldValue(fieldName: string): string {
  // 对于 Finished/FinsihedName，保持显示文案与 statusText 一致
  if (fieldName === 'Finished' || fieldName === 'FinishedName') return statusText.value
  return getCourseFieldValue(courseData.value, fieldName)
}

// 获取出勤显示文本
function getAttendanceDisplayText(): string {
  const attendanceCount = Number(courseData.value.StudentAttendanceCount) || 0
  const totalCount = Number(courseData.value.StudentCount) || 0
  return `${attendanceCount?attendanceCount:'-'}/${totalCount}`
  
}
</script>

<style scoped lang="scss">
.course-detail-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  position: relative;
  min-width: 362px;
}

.course-header {
  padding: 12px 16px 8px;
  position: relative;
  border-radius: 8px 8px 0 0;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 22px;
  color: #303133;
  width: calc(100% - 60px);
}

.course-time {
  font-size: 14px;
  color: #606266;
}

.course-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.tag {
  background: #F4F4F5;
  color: #909399;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

.status-badge {
  position: absolute;
  top: 26px;
  right: 16px;
  background: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.course-content {
  padding:8px 16px;
}

.content-body {
  min-height: 50px;
  // padding: 8px 0;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
  align-items: flex-start;
  line-height: 20px;
  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  color: #909399;
  font-size: 14px;
  margin-right: 8px;
  flex-shrink: 0;
}

.value {
  color: #303133;
  font-size: 14px;
  flex: 1;
  word-break: break-all;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  color: #606266;
  font-size: 14px;
  min-width: 40px;
  text-align: left;
}

.notes-content {
  color: #303133;
  font-size: 14px;
  border-radius: 4px;
  word-break: break-all;
}

.course-footer {
  padding: 6px 16px;
  background: #F6F6F7;
  gap: 5px;
  .main-btns{
    gap: 4px;
  }
  .course-footer-btn {
    color: #303133;
    font-size: 14px;
    line-height: 20px;
    padding: 6px 14px;
    border-radius: 4px;
    &:hover{
      cursor: pointer;
      background: rgba(0,0,0,0.06);
    }
    &.disabled {
      color: #C0C4CC;
      cursor: not-allowed;
      &:hover {
        background: transparent;
      }
    }
  }
}

/* 日程卡片样式 */
.schedule-detail-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  position: relative;
  min-width: 362px;
}

.schedule-header {
  padding: 12px 16px 8px;
  background: #F0F2F5;
  position: relative;
  border-radius: 8px 8px 0 0;
}

.schedule-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 22px;
  color: #303133;
  width: calc(100% - 60px);
}

.schedule-time {
  font-size: 14px;
  color: #606266;
}

.schedule-content {
  padding: 8px 16px;
  
  .schedule-info {
    margin-bottom: 16px;
  }
}

.schedule-footer {
  padding: 6px 16px;
  background: #F6F6F7;
  
  .schedule-placeholder-btns {
    display: flex;
    gap: 8px;
    justify-content: center;
    align-items: center;
  }
  
  .schedule-btn {
    color: #303133;
    font-size: 14px;
    border-radius: 4px;
    text-align: center;
    width: 150px;
    line-height: 20px;
    padding: 6px 14px;
    cursor: pointer;
    &:hover{
      background: rgba(0,0,0,0.06);
    }
  }
}

</style>

<style lang="scss">
.course-detail-popover {
  padding: 0 !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  
  .el-popper__arrow {
    display: none !important;
  }
  
  // 确保popover内容不会超出视口
  max-width: 362px !important;
  max-height: 80vh !important;
  overflow: hidden !important;
}
</style>
