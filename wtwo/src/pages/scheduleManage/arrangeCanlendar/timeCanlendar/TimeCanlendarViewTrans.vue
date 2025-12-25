<template>
  <div class="w100% h100% flex canlendar-container time-trans-view"
    :class="{ 'is-fullscreen': isFullscreen }"
    v-loading="loading" element-loading-target=".canlendar-container">
    
    <!-- 左侧：星期选择列 -->
    <div class="week-column-container">
      <!-- 表头行 -->
      <div class="header" style="border-top-left-radius: 8px;">
        <div class="week-column-header">
          <div class="flex-center">
            星期
            <WeekdaySelectPopover
              v-model="selectedWeekdays"
              v-model:visible="weekdaySelectVisible"
              @save="handleWeekdaySave"
            >
              <template #reference>
                <el-icon size="16px" color="var(--wtwo-color-primary)" class="ml-4px cursor-pointer setting-btn" title="日期设置">
                  <svg aria-hidden="true">
                    <use xlink:href="#w2-shaixuan"></use>
                  </svg>
                </el-icon>
              </template>
            </WeekdaySelectPopover>
          </div>
        </div>
      </div>
      
      <!-- 星期列表 -->
      <div class="time-schedule-content week-column-content">
        <div class="week-column-wrapper">
          <div class="week-list" ref="weekListRef">
            <div v-for="dayData in filteredWeekData" :key="dayData.originalIndex" 
                 class="week-item"
                 :class="{ 'selected': selectedDayIndex === dayData.originalIndex }" 
                 @click="handleWeekSelect(dayData.originalIndex)">
              <div class="week-info">
                <div class="week-name" :class="{ 'date-today': isToday(dayData.date) }" :title="dayData.dayName + ' ' + formatDate(dayData.date)">
                  {{ dayData.dayName }}
                  <div v-if="isToday(dayData.date)" class="today-tag">今</div>
                </div>
                <div class="week-date">{{ formatDate(dayData.date) }}</div>
                <div v-if="isHoliday(dayData.date)" class="holiday-tag" :title="getHolidayInfo(dayData.date)?.Name || '节假日'">假</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：时刻和对象 -->
    <div class="schedule-container" ref="scheduleContainerRef">
      <!-- 表头行 -->
      <div class="header">
        <!-- 时间列 -->
        <div class="time-column-simple">
          时刻
          <ViewModePopover
            :view-by="currentViewBy"
            :timetable-type="props.timetableType"
            :object-name="objectName"
            v-model:visible="viewModeVisible"
            @save="handleViewModeSave"
          >
            <template #reference>
              <el-icon size="16px" class="ml-2px cursor-pointer setting-btn" title="显示方式">
                <svg aria-hidden="true">
                  <use xlink:href="#w2-shezhichangyongtiaojian"></use>
                </svg>
              </el-icon>
            </template>
          </ViewModePopover>
        </div>
        
        <!-- 对象列（横向） -->
        <div v-for="object in visibleObjects" :key="object.ID" class="day-column">
          <div class="day-column-content">
            <div class="day-name" :title="object.Name">
              {{ object.Name }}
            </div>
            <!-- 更多课程标记 - 居中，挨着条数 -->
            <span v-if="objectsWithHiddenCourses.get(object.ID)" 
                  @click="openObjectMoreDrawer(object)" 
                  class="more-courses-mark"
                  title="该对象有更多排课未显示">
              更多
            </span>
            <div class="day-date">{{ getObjectDayCourseCount(object.ID) }}条</div>
            <!-- 学员信息按钮 - 仅在学员课表显示 -->
            <div
              v-if="props.timetableType === CourseTimetableTypeEnum.StudentTimetable"
              class="student-more-btn"
              @click.stop="openStudentPopover($event, object)"
              v-click-outside="(event) => handleStudentClickOutside(event, object)"
            >
              <el-icon class="default-icon" size="12px" color="#606266"><svg aria-hidden="true"><use xlink:href="#w2-xueyuanxinxi"></use></svg></el-icon>
              <el-icon class="active-icon" size="12px" color="#fff"><svg aria-hidden="true"><use xlink:href="#w2-xueyuanxinxi"></use></svg></el-icon>
            </div>
            <!-- 移除按钮 - 最右边，仅在指派模式显示 -->
            <el-dropdown trigger="click" v-if="props.assign==1" :show-timeout="0" :hide-timeout="0" @click.stop>
              <div class="more-btn" @click.stop>
                <el-icon class="default-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-xueyuanxinxikapianmoren"></use></svg></el-icon>
                <el-icon class="active-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-gengduohover"></use></svg></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleRemove(object)">移除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <!-- 更多对象按钮列 -->
        <div v-if="showMoreButton" class="weekday-select-column">
          <el-button type="primary" plain size="small" @click="handleShowMoreObjects">
            更多({{ remainingObjectsCount }})
          </el-button>
        </div>
        
        <!-- 添加对象按钮列 -->
        <div v-if="props.assign==1" class="add-select-column">
          <el-icon size="16px" class="cursor-pointer setting-btn" :title="'添加'+transToConfigDescript(objectName)" @click="handleObjectSelectClick">
            <svg aria-hidden="true">
              <use xlink:href="#w2-tianjia"></use>
            </svg>
          </el-icon>
        </div>
        
        <!-- 滚动条占位 -->
        <div class="scrollbar-placeholder"></div>
      </div>
      <div
        v-if="props.timetableType === CourseTimetableTypeEnum.StudentTimetable && (!props.assignList || props.assignList.length === 0)"
        class="empty-state">
        <el-empty :image="globalData.emptyPng" :image-size="100">
          <template #description>
            <div class="color-[#666]">仅支持选择“报读了1对1课程”的学员，请在左上角选择。</div>
          </template>
        </el-empty>
      </div>
      <!-- 时间轴和课程内容 -->
      <div v-else class="time-schedule-content">

        <div class="time-list">
          <!-- 时间标签列 -->
          <div class="time-labels" :class="{ 'with-object-column': showObjectColumn }">
            <div v-for="slot in timeSlots" :key="slot.value" :style="{
              height: (slot.isLast ? 0 : defaultHeight / 2) + 'px',

            }">
              <div class="time-label" v-if="slot.label.endsWith('00')" :style="{ height: slot.isLast ? 0 :defaultHeight + 'px',transform: slot.isLast ? 'translateY(-16px)' : 'none' }">{{ slot.label }}</div>
              <div class="time-label-empty" v-else :style="{ height: slot.isLast ? 0 :(defaultHeight / 2) + 'px' }"></div>
            </div>

            <!-- 拖拽时显示当前时间指示器 -->
            <div v-if="dragState.isDragging && dragState.currentTime" class="drag-time-indicator" :style="{
              top: `${dragState.currentPosition ? dragState.currentPosition.y : 0}px`,
              zIndex: 200
            }">
              <div class="drag-time-label">{{ dragState.currentTime }}</div>
              <div class="drag-time-arrow"></div>
            </div>
          </div>

          <!-- 课程网格容器 -->
          <div class="schedule-grid-wrapper">
            <!-- 课程网格 -->
            <div class="schedule-grid">
              <div v-for="object in visibleObjects" :key="object.ID" class="day-column-container">
                <!-- 网格背景 -->
                <div class="grid-background" @click="handleContainerClick($event, object.ID)">
                <div v-for="slot in timeSlots" :key="slot.value" class="grid-cell" :class="{
                  'past-date': isBeforeToday(selectedDayDate)
                }" :style="{
                  height: (slot.isLast ? 0 : defaultHeight / 2) + 'px',
                }"></div>
                <!-- :class="{ 'half-hour': slot.label.endsWith('30') }" -->
              </div>

              <div class="courses-container" @click="handleContainerClick($event, object.ID)">
                <div v-for="course in getCoursesByObject(object.ID).courses"
                  :key="course.ID" class="course-item" :class="{
                    'resizing': dragState.isResizing && dragState.draggedCourse?.ID === course.ID,
                    'short-course': false && parseFloat(getCourseStyle(course, selectedDayIndex, getStatusColor(course)).height) < 90
                  }" :style="getCourseStyle(course, selectedDayIndex, getStatusColor(course))"
                  :title="`${course.ShiftName}\n${course.StartTime} - ${course.EndTime}`"
                  v-click-outside="(event) => handleClickOutside(event, course)"
                  @mouseenter="handleCourseMouseEnter(course)" @mouseleave="handleCourseMouseLeave"
                  @mousedown="handleCourseMouseDown($event, course)" @click.native="handleCourseClick($event, course)">

                  <!-- 课程信息区域 -->
                  <div class="course-content">
                    <div :class="field == 'StartTime' ? 'course-header' : 'course-field'"
                      v-for="field in courseFieldsList" :key="field">
                      {{ field == 'StartTime' ? (course.StartTime + '~' + course.EndTime) : getFieldValue(course, field)
                      }}
                    </div>
                    <div class="course-tag-fields">
                      <div v-for="field in courseTagFieldsList" :key="field" v-show="getCourseTagText(course, field)"
                        :style="{ color: getTagColor(field) }">
                        {{ getCourseTagText(course, field) }}
                      </div>
                    </div>
                  </div>
                  <!-- 新增：上下边缘拖拽手柄 -->
                  <div class="resize-handle resize-handle-top"
                    @mousedown.stop.prevent="handleResizeStart($event, course, 'top')" @click.stop.prevent
                    @mouseenter.stop @mouseleave.stop title="拖拽调整开始时间" :data-course-ID="course.ID"
                    :data-direction="'top'">
                  </div>
                  <div class="resize-handle resize-handle-bottom"
                    @mousedown.stop.prevent="handleResizeStart($event, course, 'bottom')" @click.stop.prevent
                    @mouseenter.stop @mouseleave.stop title="拖拽调整结束时间" :data-course-ID="course.ID"
                    :data-direction="'bottom'">
                  </div>
                </div>
                <div v-for="course in getCoursesByObject(object.ID).schedules"
                  :key="course.ID" class="course-item" :class="{
                    'resizing': dragState.isResizing && dragState.draggedCourse?.ID === course.ID
                  }" :style="getCourseStyle(course, selectedDayIndex, '#2877FF')"
                  :title="`${course.StartTime} - ${course.EndTime}`"
                  v-click-outside="(event) => handleClickOutside(event, course)"
                  @mouseenter="handleCourseMouseEnter(course)" @mouseleave="handleCourseMouseLeave"
                  @mousedown="handleCourseMouseDown($event, course)" @click.native="handleCourseClick($event, course)">

                  <!-- 课程信息区域 -->
                  <div class="course-content">

                    <div class="course-header">
                      {{ (course.StartTime + '~' + course.EndTime) }}
                      <div class="fixed-type-label">日程</div>
                    </div>
                    <div :class="'course-field'">
                      {{ course.Title }}
                    </div>
                    <div class="course-field" :title="course.TeacherList.map((i) => i.Name).join(',')">
                      {{course.TeacherList.map((i) => i.Name).join(',')}}
                    </div>
                    <div :class="'course-field'">
                      {{ course.CampusName }}
                    </div>
                    <div :class="'course-field'">
                      {{ course.Content }}
                    </div>
                    <div :class="'course-field'" v-if="course.Remark">
                      {{ course.Remark }}
                    </div>
                  </div>
                  <!-- 新增：上下边缘拖拽手柄 -->
                  <div class="resize-handle resize-handle-top"
                    @mousedown.stop.prevent="handleResizeStart($event, course, 'top')" @click.stop.prevent
                    @mouseenter.stop @mouseleave.stop title="拖拽调整开始时间" :data-course-ID="course.ID"
                    :data-direction="'top'">
                  </div>
                  <div class="resize-handle resize-handle-bottom"
                    @mousedown.stop.prevent="handleResizeStart($event, course, 'bottom')" @click.stop.prevent
                    @mouseenter.stop @mouseleave.stop title="拖拽调整结束时间" :data-course-ID="course.ID"
                    :data-direction="'bottom'">
                  </div>
                </div>

                <!-- 临时占位块 - 只在对应的对象显示 -->
                <div v-if="temporaryBlock && temporaryBlock.objectId === object.ID" class="temporary-block"
                  :class="{ 'passive-block': temporaryBlock.isPassive, 'large-block': isTemporaryBlockLarge }"
                  :style="getTemporaryBlockStyle()" @dblclick.stop="addSchedule">
                  <!-- 只有主动创建的临时块才显示文字内容 -->
                  <div v-if="!temporaryBlock.isPassive" class="temporary-block-content">
                    <div class="temporary-block-time">{{ temporaryBlock.StartTime }} - {{ temporaryBlock.EndTime }}
                    </div>
                    <div class="temporary-block-label">双击新增排课</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 拖拽预览块 - 独立的拖拽预览层 -->
            <div
              v-if="(dragState.isDragging || dragState.isResizing) && dragState.draggedCourse && dragState.draggedCourse.type === 'course'"
              class="course-item dragging-course-preview" :style="getDragPreviewStyle()">

              <!-- 课程信息区域 -->
              <div class="course-content">
                <div :class="field == 'StartTime' ? 'course-header' : 'course-field'" v-for="field in courseFieldsList"
                  :key="field">
                  {{ field == 'StartTime' ? (dragState.draggedCourse.StartTime + '~' + dragState.draggedCourse.EndTime)
                    :
                    getFieldValue(dragState.draggedCourse, field) }}
                </div>
                <div class="course-tag-fields">
                  <div v-for="field in courseTagFieldsList" :key="field"
                    v-show="getCourseTagText(dragState.draggedCourse, field)" :style="{ color: getTagColor(field) }">
                    {{ getCourseTagText(dragState.draggedCourse, field) }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 拖拽预览块 - 独立的拖拽预览层 -->
            <div
              v-if="(dragState.isDragging || dragState.isResizing) && dragState.draggedCourse && dragState.draggedCourse.type === 'schedule'"
              class="course-item dragging-course-preview" :style="getDragPreviewStyle('#2877FF')">
              <!-- 课程信息区域 -->
              <div class="course-content">
                <div class="course-header">
                  {{ (dragState.draggedCourse.StartTime + '~' + dragState.draggedCourse.EndTime) }}
                  <div class="fixed-type-label">日程</div>
                </div>
                <div :class="'course-field'">
                  {{ dragState.draggedCourse.Title }}
                </div>
                <div class="course-field" :title="dragState.draggedCourse.TeacherList.map((i) => i.Name).join(',')">
                  {{dragState.draggedCourse.TeacherList.map((i) => i.Name).join(',')}}
                </div>
                <div :class="'course-field'">
                  {{ dragState.draggedCourse.CampusName }}
                </div>
                <div :class="'course-field'">
                  {{ dragState.draggedCourse.Content }}
                </div>
                <div :class="'course-field'" v-if="dragState.draggedCourse.Remark">
                  {{ dragState.draggedCourse.Remark }}
                </div>
              </div>
            </div>

            <!-- 拖拽预览块的时间指示器 -->
            <div v-if="(dragState.isDragging || dragState.isResizing) && dragState.draggedCourse"
              class="drag-preview-time-indicator" :style="getDragPreviewTimeStyle()">
              <div class="drag-preview-time-label" :style="{ color: getStatusColor(dragState.draggedCourse) }">
                {{ dragState.isDragging ? `${dragState.currentTime} ~
                ${hourToTimeString(parseTime(dragState.currentTime)
                  + (parseTime(dragState.draggedCourse.EndTime) - parseTime(dragState.draggedCourse.StartTime)))}` :
                  `${dragState.draggedCourse.StartTime} ~ ${dragState.draggedCourse.EndTime}` }}
              </div>
            </div>
          </div>
            
            <!-- 更多按钮占位列 -->
            <div v-if="showMoreButton" class="placeholder-column">
              <div class="placeholder-content" v-for="slot in timeSlots" :key="slot.value" :style="{
                height: (slot.isLast ? 0 : defaultHeight / 2) + 'px',
              }"></div>
            </div>
            
            <!-- 添加对象按钮占位列 -->
            <div v-if="props.assign==1" class="add-column">
              <div class="add-content" v-for="slot in timeSlots" :key="slot.value" :style="{
                height: (slot.isLast ? 0 : defaultHeight / 2) + 'px',
              }"></div>
            </div>
            
            <!-- 滚动条占位列 -->
            <div class="scrollbar-placeholder-content"></div>
          </div>

          <!-- 当前时间线 -->
          <div v-if="showCurrentTimeLine && currentDay >= 0 && currentTimePosition >= 0" class="current-time-line"
            :style="{
              top: `${currentTimePosition}px`,
              left: `${currentDay * (100 / 7)}%`,
              width: `${100 / 7}%`
            }">
            <div class="current-time-dot"></div>
            <div class="current-time-label">{{ formatTime(currentTimePosition / defaultHeight + startHour.value) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 课程拖拽确认弹框组件 -->
      <CourseDragConfirmDialog ref="courseDragConfirmDialogRef" />
      <ScheduleDragConfirmDialog ref="scheduleDragConfirmDialogRef" />
      <!-- 更多课程抽屉 -->
      <MoreCourseListPop v-model="moreDrawerVisible" :selected-date="moreDrawerDate"
        :search-params="{ ...props.searchParams, ...moreParams }"
        :timetable-type="props.timetableType||CourseTimetableTypeEnum.TimeTimetable" :preference-color-resolver="preferenceColorResolver" />
      <!-- 课程详情浮窗 -->
      <CourseDetailPopover ref="courseDetailPopoverRef" :course-data="selectedCourseData" :virtual-ref="virtualRef"
        :prevent-auto-hide="preventAutoHide" @roll-call="handleRollCall" @adjust-students="handleAdjustStudents"
        @schedule-details="handleScheduleDetails" @show="handlePopoverShow" @hide="handlePopoverHide"
        :timetable-type="props.timetableType||CourseTimetableTypeEnum.TimeTimetable" :needApi="false" />
      <!-- 学员详情浮窗 -->
      <StudentDetailPopover ref="studentPopoverRef" :student="selectedStudent" :virtual-ref="studentVirtualRef"
        :campus-id="selectedStudent?.CampusID || selectedStudent?.campusId" />
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, computed, onUnmounted, nextTick, watch, getCurrentInstance } from 'vue'
import { queryCourseNew, queryCalendarTeacher, queryCalendarClass, queryCalendarClassroom, queryCalendarStudent } from '@/api/arrange'
import { queryHoliday } from '@/api/comm'
import { ClickOutside as vClickOutside, ElMessage, ElLoading } from 'element-plus'
import { getCourseStatusColor, getCourseTagText, getCourseFieldValue, getBackgroundColorByPreference, buildExportColumns, getNameInitial } from '@/utils'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
import dayjs from 'dayjs'
import CourseDragConfirmDialog from '../popup/courseDragConfirmDialog.vue'
import ScheduleDragConfirmDialog from '../popup/scheduleDragConfirmDialog.vue'
import StudentDetailPopover from '@/pages/scheduleManage/components/studentDetailPopover.vue'
import MoreCourseListPop from '../popup/moreCourseListPop.vue'
import CourseDetailPopover from '@/components/CourseDetailPopover.vue'
import ViewModePopover from '@/pages/scheduleManage/components/ViewModePopover.vue'
import WeekdaySelectPopover from '@/pages/scheduleManage/components/WeekdaySelectPopover.vue'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { CourseTimetableTypeEnum } from '@/types/model/timetable-preference'
import { transToConfigDescript } from '@/utils/filters/filters'
const preventAutoHide = ref(true)
const timetablePrefStore = useTimetablePreferences()
const timeTimetablePref = computed(() => timetablePrefStore.preferenceByType(props.timetableType))
const courseFieldsList = computed(() => {
  const cs = timeTimetablePref.value && Array.isArray(timeTimetablePref.value.CardShowInformationSettings)
    ? timeTimetablePref.value.CardShowInformationSettings.filter((s) => s.FieldType === 'Main')
    : []
  return cs.filter((s) => s.IsEnabled).map((s) => s.FieldName)
})
const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
// 111
// 导出列（公用工具）
const exportColumns = computed(() => buildExportColumns(ALL_COLUMNS))
// 为抽屉提供与当前视图一致的 ByTeacher/ByCourse/ByClassroom 颜色解析
function preferenceColorResolver(
  type,
  ids,
  timetableType) {
  return getBackgroundColorByPreference(type, ids, timetableType || CourseTimetableTypeEnum.TimeTimetable)
}
const courseTagFieldsList = computed(() => {
  const cs = timeTimetablePref.value && Array.isArray(timeTimetablePref.value.CardShowInformationSettings)
    ? timeTimetablePref.value.CardShowInformationSettings.filter((s) => s.FieldType === 'Tag')
    : []
  return cs.filter((s) => s.IsEnabled).map((s) => s.FieldName)
})
const enabledColorSetting = computed(() => {
  const cs = timeTimetablePref.value && Array.isArray(timeTimetablePref.value.ColorSettings)
    ? timeTimetablePref.value.ColorSettings
    : []
  if (!cs || cs.length === 0) return null
  return cs.find((s) => s && s.IsEnabled) || cs[0]
})
const colorDetailMap = computed(() => {
  const details = enabledColorSetting.value && Array.isArray(enabledColorSetting.value.ColorDetails)
    ? enabledColorSetting.value.ColorDetails
    : []
  const map = {}
  details.forEach((d) => {
    if (d && d.EnumValue != null && d.ColorValue) {
      map[String(d.EnumValue)] = d.ColorValue
    }
  })
  return map
})
// 标签颜色映射：优先使用偏好里的 TagColorSettings，其次使用默认
const tagColorMap = computed(() => {
  const list = (timeTimetablePref.value?.TagColorSettings || [])
  const map = {}
  if (Array.isArray(list)) {
    list.forEach((t) => {
      if (t && t.TagFieldName && t.ColorValue) map[t.TagFieldName] = t.ColorValue
    })
  }
  return map
})
// function hexToRgba(hex, alpha) {
//   const r = parseInt(hex.slice(1, 3), 16);
//   const g = parseInt(hex.slice(3, 5), 16);
//   const b = parseInt(hex.slice(5, 7), 16);
//   return `rgba(${r}, ${g}, ${b}, ${alpha})`;
// }
function hexToRgb(hex) {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return [r, g, b]
}
const DEFAULT_TAG_COLORS = {
  Finished: '#909399',          // 上课状态
  IsSubscribeCourse: '#909399', // 约课
  IsOneToOne: '#909399',        // 教学形式
  TeacherName: '#909399',       // 任课老师
  ClassroomName: '#909399',     // 上课教室
  ClassPlanNum_ClassPlanCount: '#909399',       // 上课进度
  CourseType: '#909399',         // 上课方式
  CourseStatus: '#909399'         // 开课状态
}
function getTagColor(fieldName) {
  return tagColorMap.value[fieldName] || DEFAULT_TAG_COLORS[fieldName] || '#409EFF'
}
// 学员详情浮窗
const studentPopoverRef = ref()
const studentVirtualRef = ref()
const selectedStudent = ref({})
// 打开学员浮窗（学员视图侧边“更多”按钮）
async function openStudentPopover(evt, student) {
  const target = evt.currentTarget
  // 适配字段
  selectedStudent.value = {
    ID: student.ID,
    Name: student.Name,
    Photo: student.Avatar || student.Photo,
    CampusID: student.CampusID,
  }
  studentVirtualRef.value = target
  await nextTick()
  studentPopoverRef.value && studentPopoverRef.value.show()
}
// 处理点击外部区域隐藏popover
const handleStudentClickOutside = (event, item) => {
  if ((selectedStudent.value?.ID) === (item.ID)) return
  const target = event.target
  const popoverElement = document.querySelector('.wtwo-student-detail-popover')
  if (popoverElement && popoverElement.contains(target)) return
  hideStudentPopover()
}

function hideStudentPopover() {
  if (studentPopoverRef.value) {
    studentPopoverRef.value.hide()
  }
}

// 根据偏好或完成状态返回颜色
function getStatusColor(item) {
  if (item.type === 'schedule') {
    return '#2877FF'
  }
  return getCourseStatusColor(
    item,
    enabledColorSetting.value?.SettingType,
    colorDetailMap.value,
    props.timetableType
  )
}
function getFieldValue(item, fieldName) {
  return getCourseFieldValue(item, fieldName)
}
// === 组件属性定义 ===
const props = defineProps({
  // 是否显示当前时间线
  showCurrentTimeLine: {
    type: Boolean,
    default: true
  },
  // 是否启用拖拽功能
  enableDrag: {
    type: Boolean,
    default: true
  },
  // 周几的显示文本
  weekDays: {
    type: Array,
    default: () => ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  // 当前周的基准日期
  currentWeek: {
    type: Date,
    default: () => new Date()
  },
  searchParams: {
    type: Object,
    default: () => { }
  },
  // 课表类型
  timetableType: {
    type: String,
    default: CourseTimetableTypeEnum.TimeTimetable
  },
  // 分配列表（用于指定对象）
  assignList: {
    type: Array,
    default: () => []
  },
  // 分配模式：0-全部，1-指定的xxx
  assign: {
    type: Number,
    default: 0
  },
  // 视图模式（byTime 或 byPeriod）
  viewBy: {
    type: String,
    default: 'byTime'
  },
  // 选择的星期（1-7，7代表星期日）
  Weekdays: {
    type: Array,
    default: () => [1, 2, 3, 4, 5, 6, 7]
  }
})

// 显示方式设置相关
// 显示方式弹窗控制
const viewModeVisible = ref(false)
const currentViewBy = ref(props.viewBy || 'byTime')

// 日期选择相关
const weekdaySelectVisible = ref(false)
const selectedWeekdays = ref(props.Weekdays || [1, 2, 3, 4, 5, 6, 7]) // 默认全选，周日用7表示

// 初始化时默认选择今天
const getTodayDayIndex = () => {
  const today = new Date()
  const dayOfWeek = today.getDay()
  // 周日是0，转换为6（周一是0，周日是6）
  return dayOfWeek === 0 ? 6 : dayOfWeek - 1
}

const selectedDayIndex = ref(getTodayDayIndex())

// 监听 props.Weekdays 的变化，同步到 selectedWeekdays
watch(() => props.Weekdays, (newWeekdays) => {
  if (newWeekdays && newWeekdays.length > 0) {
    selectedWeekdays.value = newWeekdays
    
    // 检查初始化时选中的今天是否在weekdays中
    const currentDayWeekday = selectedDayIndex.value === 6 ? 7 : selectedDayIndex.value + 1
    if (!newWeekdays.includes(currentDayWeekday)) {
      // 如果今天不在选中的weekdays中，选择剩余日期中最小的那个（按周顺序）
      const sortedWeekdays = [...newWeekdays].sort((a, b) => a - b)
      const firstWeekday = sortedWeekdays[0]
      const firstDayIndex = firstWeekday === 7 ? 6 : firstWeekday - 1
      selectedDayIndex.value = firstDayIndex
    }
  } else {
    // 如果传入的 Weekdays 为空，默认选择周一
    selectedWeekdays.value = [1]
    emit('update:Weekdays', [1])
  }
}, { immediate: true })

// 监听 selectedWeekdays 的变化，确保至少有一个被选中
watch(selectedWeekdays, (newVal) => {
  if (!newVal || newVal.length === 0) {
    selectedWeekdays.value = [1]
    emit('update:Weekdays', [1])
  }
})

// 监听 viewBy prop 变化
watch(() => props.viewBy, (newVal) => {
  if (newVal) {
    currentViewBy.value = newVal
  }
})

// 处理显示方式保存
const handleViewModeSave = (viewBy) => {
  currentViewBy.value = viewBy
  emit('update:view-by', viewBy)
}

// 处理日期选择保存
const handleWeekdaySave = (weekdays) => {
  // 如果weekdays为空，默认选择第一个（周一）
  if (!weekdays || weekdays.length === 0) {
    weekdays = [1]
  }
  
  selectedWeekdays.value = weekdays
  console.log('选择的日期:', weekdays)
  
  // 检查当前选中的日期是否还在新的weekdays中
  // selectedDayIndex是基于0的索引(0=周一...6=周日)
  // weekdays数组中存储的是1-7(1=周一...7=周日)
  const currentDayWeekday = selectedDayIndex.value === 6 ? 7 : selectedDayIndex.value + 1
  
  if (!weekdays.includes(currentDayWeekday)) {
    // 如果当前选中的日期被取消了，选择剩余日期中最小的那个（按周顺序）
    // 对weekdays数组排序，找到最小值
    const sortedWeekdays = [...weekdays].sort((a, b) => a - b)
    const firstWeekday = sortedWeekdays[0]
    // 转换回索引：1=0(周一), 2=1(周二)...7=6(周日)
    const firstDayIndex = firstWeekday === 7 ? 6 : firstWeekday - 1
    selectedDayIndex.value = firstDayIndex
    console.log(`当前选中日期被取消，自动切换到: ${firstWeekday}(索引${firstDayIndex})`)
  }
  
  // 通知父组件更新 Weekdays
  emit('update:Weekdays', weekdays)
}

const minWidth = 60

const defaultHeight = computed(() => {
  return (timeTimetablePref?.value?.RowHeight || 30) * 2;
});

// 时间格式转换函数 - 提前定义，因为在计算属性中会用到
const parseTime = (timeStr) => {
  const [hours, minutes] = timeStr ? timeStr.split(':').map(Number) : [0, 0]
  return hours + minutes / 60
}

const formatTime = (timeNum) => {
  const hours = Math.floor(timeNum)
  const minutes = Math.round((timeNum - hours) * 60)
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
}


// 时间范围控制
const timeRangeStart = computed(() => timeTimetablePref.value ? timeTimetablePref.value.TimeViewStart || '00:00' : '00:00');
const timeRangeEnd = computed(() => timeTimetablePref.value ? timeTimetablePref.value.TimeViewEnd == '00:00' ? '24:00' : timeTimetablePref.value.TimeViewEnd : '24:00');

// 计算时间范围对应的小时数值
const startHour = computed(() => parseTime(timeRangeStart.value));
const endHour = computed(() => parseTime(timeRangeEnd.value));

// 计算时间范围内的总小时数
const visibleHoursCount = computed(() => endHour.value - startHour.value);

// 计算可见区域的总高度
const visibleHeight = computed(() => visibleHoursCount.value * defaultHeight.value);


// === 组件事件定义 ===
const emit = defineEmits([
  'course-click',       // 课程点击事件
  'fullscreen-change',  // 全屏状态改变事件
  'canlendar-dbclick-add-course',        // 添加课程事件
  'create-temporary-block',        // 创建临时占位块事件
  'course-count-update',        // 课程数量更新事件
  'object-selected', // 选中对象事件（用于父组件感知选中的老师/对象）
  'remove-student',  // 移除学生事件
  'remove-teacher',  // 移除老师事件
  'update:view-by',   // 视图模式切换事件
  'object-select-click', // 对象选择点击事件（添加对象）
  'remove-object', // 移除对象事件
  'update:Weekdays'  // 星期选择变化事件
])

// === 内部响应式数据 ===
const internalCourses = ref([])
const holidays = ref([]) // 节假日数据（由父组件传入）
const daysWithHiddenCourses = ref(Array(7).fill(false)) // 记录每天是否有隐藏的课程

// 性能优化：按天预分组课程数据，避免模板中重复 filter
const coursesByDay = computed(() => {
  const grouped = Array.from({ length: 7 }, () => ({
    courses: [],
    schedules: []
  }))
  
  console.log('📅 coursesByDay 计算:', {
    internalCoursesTotal: internalCourses.value.length,
    sampleCourses: internalCourses.value.slice(0, 3).map(c => ({
      ID: c.ID,
      day: c.day,
      type: c.type,
      StartTime: c.StartTime,
      Date: c.Date
    }))
  })
  
  internalCourses.value.forEach(course => {
    if (course.day >= 0 && course.day < 7) {
      if (course.type === 'course') {
        grouped[course.day].courses.push(course)
      } else if (course.type === 'schedule') {
        grouped[course.day].schedules.push(course)
      }
    } else {
      console.warn('⚠️ 课程 day 值异常:', course.day, course)
    }
  })
  
  console.log('📅 coursesByDay 结果:', grouped.map((day, idx) => ({
    day: idx,
    courses: day.courses.length,
    schedules: day.schedules.length
  })))
  
  return grouped
})

// 转置视图新增：计算当前选中日期
const selectedDayDate = computed(() => {
  const weekStart = dayjs(props.currentWeek).startOf('week')
  return weekStart.add(selectedDayIndex.value, 'day').toDate()
})

// 转置视图新增：根据对象ID获取该对象在选中日期的课程和日程
const getCoursesByObject = (objectId) => {
  const dayData = coursesByDay.value[selectedDayIndex.value]
  
  console.log('🔍 getCoursesByObject 调试:', {
    objectId,
    selectedDayIndex: selectedDayIndex.value,
    dayData,
    coursesByDayLength: coursesByDay.value.length,
    internalCoursesLength: internalCourses.value.length,
    timetableType: props.timetableType
  })
  
  if (!dayData) {
    console.warn('⚠️ dayData 为空!')
    return { courses: [], schedules: [] }
  }
  
  const fieldNames = getFieldNames(props.timetableType)
  
  const filteredCourses = dayData.courses.filter(course => {
    // 时间视图没有对象概念，直接返回所有课程
    if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
      return true
    }
    // 其他视图根据对象ID筛选
    const matches = course[fieldNames.idField] === objectId
    console.log('  课程筛选:', {
      courseId: course.ID,
      courseFieldValue: course[fieldNames.idField],
      objectId,
      fieldName: fieldNames.idField,
      matches
    })
    return matches
  })
  
  const filteredSchedules = dayData.schedules.filter(schedule => {
    if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
      return true
    }
    return schedule[fieldNames.idField] === objectId
  })
  
  console.log('✅ 筛选结果:', {
    courses: filteredCourses.length,
    schedules: filteredSchedules.length
  })
  
  return {
    courses: filteredCourses,
    schedules: filteredSchedules
  }
}

// 转置视图新增：获取对象在选中日期的课程数量
const getObjectDayCourseCount = (objectId) => {
  const result = getCoursesByObject(objectId)
  return result.courses.length + result.schedules.length
}

// 转置视图新增：处理星期选择
const handleWeekSelect = (dayIndex) => {
  selectedDayIndex.value = dayIndex
}

// 转置视图：打开对象的更多课程抽屉
const openObjectMoreDrawer = (object) => {
  // 保留原始 searchParams 中的参数（如 StartDate、EndDate 等），然后添加或覆盖特定参数
  moreParams.value = {
    ...props.searchParams, // 保留原始参数
    timetableType: props.timetableType,
  }

  // 根据课表类型设置对应的对象ID参数
  switch (props.timetableType) {
    case CourseTimetableTypeEnum.TeacherTimetable:
      moreParams.value.IDList = [object.ID]
      moreParams.value.TeacherType = -1
      break
    case CourseTimetableTypeEnum.StudentTimetable:
      moreParams.value.StudentID = object.ID
      break
    case CourseTimetableTypeEnum.ClassTimetable:
      moreParams.value.ClassID = object.ID
      break
    case CourseTimetableTypeEnum.ClassroomTimetable:
      moreParams.value.ClassroomIDList = [object.ID]
      break
  }

  // 使用选中日期作为抽屉日期（这会在 MoreCourseListPop 的 buildParams 中被用来设置 StartDate 和 EndDate）
  const selectedDate = dayjs(props.currentWeek).startOf('week').add(selectedDayIndex.value, 'day')
  moreDrawerDate.value = selectedDate.format('YYYY-MM-DD')
  moreDrawerVisible.value = true
}

// 对象相关数据
const objects = ref([]) // 对象列表
const selectedObject = ref(null) // 当前选中的对象
const objectListVisible = ref(false) // 是否显示对象列表
const allObjectsData = ref([]) // 存储所有对象的完整数据（包括CourseList）
const objectsWithHiddenCourses = ref(new Map()) // 记录每个对象在选中日期是否有隐藏课程

// 根据课表类型获取对应的对象名称
const objectName = computed(() => {
  const map = {
    [CourseTimetableTypeEnum.TeacherTimetable]: '任课老师',
    [CourseTimetableTypeEnum.StudentTimetable]: '学员',
    [CourseTimetableTypeEnum.ClassTimetable]: '班级',
    [CourseTimetableTypeEnum.ClassroomTimetable]: '教室',
    [CourseTimetableTypeEnum.TimeTimetable]: '时间',
  }
  return map[props.timetableType] || '任课老师'
})

// 判断是否显示对象列
const showObjectColumn = computed(() => {
  return props.timetableType !== CourseTimetableTypeEnum.TimeTimetable
})

// 根据课表类型获取对应的字段名
const getFieldNames = (timetableType) => {
  const fieldMap = {
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

// 判断是否应该显示头像（班级和教室视图不显示头像）
const shouldShowAvatar = computed(() => {
  return props.timetableType !== CourseTimetableTypeEnum.ClassTimetable &&
    props.timetableType !== CourseTimetableTypeEnum.ClassroomTimetable
})

// 转置视图新增：可见对象列表（默认显示前5个，可展开更多）
const DEFAULT_DISPLAY_COUNT = 5
const currentDisplayCount = ref(DEFAULT_DISPLAY_COUNT)

const visibleObjects = computed(() => {
  const displayCount = currentDisplayCount.value
  return objects.value.slice(0, displayCount)
})

const showMoreButton = computed(() => {
  return objects.value.length > currentDisplayCount.value
})

const remainingObjectsCount = computed(() => {
  return Math.max(0, objects.value.length - currentDisplayCount.value)
})

const handleShowMoreObjects = () => {
  const INCREMENT_COUNT = 5
  const MAX_DISPLAY_COUNT = 100
  currentDisplayCount.value = Math.min(
    currentDisplayCount.value + INCREMENT_COUNT,
    MAX_DISPLAY_COUNT,
    objects.value.length
  )
}

// （初始化默认选中逻辑已移至数据加载完成处，避免使用watch）

// 对象选择处理
const handleObjectSelect = (object) => {
  selectedObject.value = object
  
  // 使用 Loading Service 显示加载效果（只在课程表格区域）
  const loadingInstance = ElLoading.service({
    target: '.schedule-grid',
    text: '加载中...',
    background: 'rgba(255, 255, 255, 0.8)'
  })
  
  // 使用 nextTick 确保 UI 更新，然后更新数据
  nextTick(() => {
    updateCoursesFromSelectedObject()
    
    // 短暂延迟后隐藏加载效果，让用户能看到反馈
    setTimeout(() => {
      loadingInstance.close()
    }, 200) // 200ms 足够显示加载状态但不会感觉慢
  })
  
  // 向父级上报选中的对象（用于老师与时间/时段视图联动）
  emit('object-selected', { object, timetableType: props.timetableType })
}

// 处理对象选择点击（添加对象）
const handleObjectSelectClick = () => {
  emit('object-select-click')
}

// 移除对象功能
const handleRemove = (item) => {
  emit('remove-object', item)
}

// 根据选中的对象更新课程数据（从内存中获取，不调用API）
const updateCoursesFromSelectedObject = () => {
  // 切换对象时，重置所有天的“更多”标记
  daysWithHiddenCourses.value = Array(7).fill(false)
  if (!selectedObject.value || props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
    internalCourses.value = []
    return
  }

  const fieldNames = getFieldNames(props.timetableType)

  // 从存储的所有对象数据中找到选中对象的数据
  let selectedObjectData = allObjectsData.value.find(
    item => item[fieldNames.idField] === selectedObject.value.ID
  )

  // 如果在allObjectsData中找不到，但assignList存在，尝试从assignList中获取对象信息
  if (!selectedObjectData && props.assignList && props.assignList.length > 0) {
    const assignItem = props.assignList.find(
      item => item[fieldNames.idField] === selectedObject.value.ID
    )

    if (assignItem) {
      // 创建一个模拟的对象数据结构
      selectedObjectData = {
        [fieldNames.idField]: assignItem[fieldNames.idField],
        [fieldNames.nameField]: assignItem[fieldNames.nameField] || assignItem.Name,
        CourseList: [], // 默认为空课程列表
        ...assignItem // 包含其他可能的属性
      }
    }
  }

  if (selectedObjectData && (selectedObjectData.CourseList || selectedObjectData.ScheduleList)) {
    // 合并 CourseList 与 ScheduleList，并标记类型
    const normalizeItem = (item, type) => ({
      ID: item.ID || item.CourseID || `${type}_${dayjs(item.StartTime).valueOf()}_${dayjs(item.EndTime).valueOf()}`,
      type,
      ...item,
      [fieldNames.idField]: selectedObject.value.ID,
      [fieldNames.nameField]: selectedObject.value.Name,
      day: getDayOfWeek(dayjs(item.StartTime).format('YYYY-MM-DD'), dayjs(props.currentWeek).startOf('week').format('YYYY-MM-DD')),
      StartTime: dayjs(item.StartTime).format('HH:mm'),
      EndTime: dayjs(item.EndTime).format('HH:mm'),
      Date: dayjs(item.StartTime).format('YYYY-MM-DD'),
    })

    const courseList = Array.isArray(selectedObjectData.CourseList)
      ? selectedObjectData.CourseList.map(it => normalizeItem(it, 'course'))
      : []
    const scheduleList = Array.isArray(selectedObjectData.ScheduleList)
      ? selectedObjectData.ScheduleList.map(it => normalizeItem(it, 'schedule'))
      : []

    internalCourses.value = [...courseList, ...scheduleList]

    // console.log('从内存更新课程数据:', {
    //   对象: selectedObject.value.Name,
    //   课程数量: internalCourses.value.length,
    //   排课数量: courseList.length,
    //   日程数量: scheduleList.length
    // })
  } else {
    internalCourses.value = []
  }
}

// 转置视图专用：加载所有对象的课程数据
const updateCoursesForTransView = () => {
  // 重置所有天的"更多"标记
  daysWithHiddenCourses.value = Array(7).fill(false)
  
  if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
    // 时间视图保持原有逻辑
    internalCourses.value = []
    return
  }

  const fieldNames = getFieldNames(props.timetableType)
  const allCourses = []
  const allSchedules = []

  // 遍历所有对象数据，收集所有课程和日程
  allObjectsData.value.forEach(objectData => {
    const objectId = objectData[fieldNames.idField]
    const objectName = objectData[fieldNames.nameField]
    
    const normalizeItem = (item, type) => ({
      ID: item.ID || item.CourseID || `${type}_${dayjs(item.StartTime).valueOf()}_${dayjs(item.EndTime).valueOf()}`,
      type,
      ...item,
      [fieldNames.idField]: objectId,
      [fieldNames.nameField]: objectName,
      day: getDayOfWeek(dayjs(item.StartTime).format('YYYY-MM-DD'), dayjs(props.currentWeek).startOf('week').format('YYYY-MM-DD')),
      StartTime: dayjs(item.StartTime).format('HH:mm'),
      EndTime: dayjs(item.EndTime).format('HH:mm'),
      Date: dayjs(item.StartTime).format('YYYY-MM-DD'),
    })

    // 收集该对象的课程
    if (Array.isArray(objectData.CourseList)) {
      objectData.CourseList.forEach(course => {
        allCourses.push(normalizeItem(course, 'course'))
      })
    }

    // 收集该对象的日程
    if (Array.isArray(objectData.ScheduleList)) {
      objectData.ScheduleList.forEach(schedule => {
        allSchedules.push(normalizeItem(schedule, 'schedule'))
      })
    }
  })

  internalCourses.value = [...allCourses, ...allSchedules]

  console.log('✨ 转置视图加载所有对象课程数据:', {
    对象数量: allObjectsData.value.length,
    课程总数: internalCourses.value.length,
    排课数量: allCourses.length,
    日程数量: allSchedules.length,
    按天分布: internalCourses.value.reduce((acc, c) => {
      acc[c.day] = (acc[c.day] || 0) + 1
      return acc
    }, {})
  })
}

// 滚动同步处理
const setupScrollSync = () => {
  if (!objectListRef.value) return

  const objectColumn = objectListRef.value.parentElement
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid')

  if (!scheduleGrid) return

  let isObjectScrolling = false
  let isGridScrolling = false

  // 对象列滚动同步到网格
  objectColumn.addEventListener('scroll', () => {
    if (isGridScrolling) return
    isObjectScrolling = true

    scheduleGrid.scrollTop = objectColumn.scrollTop

    setTimeout(() => {
      isObjectScrolling = false
    }, 50)
  })

  // 网格滚动同步到对象列
  scheduleGrid.addEventListener('scroll', () => {
    if (isObjectScrolling) return
    isGridScrolling = true

    objectColumn.scrollTop = scheduleGrid.scrollTop

    setTimeout(() => {
      isGridScrolling = false
    }, 50)
  })
}

// === 日期计算相关 ===
// 获取一周的周一日期
const getMonday = (date) => {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1) // 调整周日为0的情况
  return new Date(d.setDate(diff))
}

// 计算当前周每一天的具体日期
const weekDates = computed(() => {
  const monday = getMonday(props.currentWeek)
  const dates = []

  for (let i = 0; i < 7; i++) {
    const date = new Date(monday)
    date.setDate(monday.getDate() + i)
    dates.push(date)
  }

  return dates
})

// 过滤后的周日期（根据选择的星期过滤）
const filteredWeekData = computed(() => {
  const filtered = []
  weekDates.value.forEach((date, index) => {
    const dayOfWeek = date.getDay() === 0 ? 7 : date.getDay()
    if (selectedWeekdays.value.includes(dayOfWeek)) {
      filtered.push({
        originalIndex: index,
        date: date,
        dayName: props.weekDays[index],
        dayOfWeek: dayOfWeek
      })
    }
  })
  return filtered
})

// 格式化日期显示
const formatDate = (date) => {
  // const month = date.getMonth() + 1
  // const day = date.getDate()
  // return `${month}月${day}日`
  return dayjs(date).format('MM.DD')
  // return dayjs(date).format('DD')
}

// 判断是否是今天
const isToday = (date) => {
  const today = new Date()
  return date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate()
}

// 判断日期是否在今天之前
const isBeforeToday = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0) // 设置为今天0点
  const compareDate = new Date(date)
  compareDate.setHours(0, 0, 0, 0)
  return compareDate < today
}

// 判断是否是节假日
const isHoliday = (date) => {
  const dateStr = dayjs(date).format('YYYY-MM-DD')
  return holidays.value.some(holiday => holiday.Date === dateStr)
}

// 获取节假日信息
const getHolidayInfo = (date) => {
  const dateStr = dayjs(date).format('YYYY-MM-DD')
  return holidays.value.find(holiday => holiday.Date === dateStr)
}
// 当前时间线位置
const currentTimePosition = ref(0)
const currentDay = ref(new Date().getDay() - 1) // 0表示周一，-1表示周日

// 更新当前时间线位置
const updateCurrentTimePosition = () => {
  const now = new Date()
  const hours = now.getHours()
  const minutes = now.getMinutes()

  // 计算当前时间的小时数值
  const totalHours = hours + minutes / 60

  // 检查当前时间是否在显示范围内
  if (totalHours >= startHour.value && totalHours <= endHour.value) {
    // 调整位置计算，减去时间范围开始偏移
    currentTimePosition.value = (totalHours - startHour.value) * defaultHeight.value
  } else {
    // 当前时间不在显示范围内，隐藏时间线
    currentTimePosition.value = -1
  }

  // 计算当前是本周的第几天
  const today = new Date()
  const mondayOfCurrentWeek = getMonday(props.currentWeek)
  const mondayOfToday = getMonday(today)

  // 只有在当前显示的周包含今天时，才显示时间线
  if (mondayOfCurrentWeek.getTime() === mondayOfToday.getTime() && currentTimePosition.value >= 0) {
    currentDay.value = today.getDay() === 0 ? 6 : today.getDay() - 1 // 调整为0-6，0表示周一
  } else {
    currentDay.value = -1 // 不在当前周或时间范围外，不显示时间线
  }
}
// 计算课程是周几（相对于周一）
function getDayOfWeek(courseDate, weekStart) {
  const date = new Date(courseDate)
  const startDate = new Date(weekStart)
  const diffTime = date.getTime() - startDate.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(0, Math.min(6, diffDays)) // 确保在0-6范围内
}
const loading = ref(false)
let loadCourseDataRequestCounter = 0
let loadCourseDataTimer = null
let pendingRequestId = null

async function loadCourseData(forceRefresh = false) {
  // 取消之前的防抖定时器
  if (loadCourseDataTimer) {
    clearTimeout(loadCourseDataTimer)
    loadCourseDataTimer = null
  }

  // 生成新的请求ID
  const currentRequestId = ++loadCourseDataRequestCounter
  pendingRequestId = currentRequestId

  // 设置防抖延迟
  return new Promise((resolve, reject) => {
    loadCourseDataTimer = setTimeout(async () => {
      // 只有当前请求是最新的才执行
      if (currentRequestId !== pendingRequestId) {
        resolve()
        return
      }

      loading.value = true
      try {
        const startOfWeek = dayjs(props.currentWeek).startOf('week').format('YYYY-MM-DD')
        const endOfWeek = dayjs(props.currentWeek).endOf('week').format('YYYY-MM-DD')
        
        // 先请求节假日（所有视图都需要）
        const holidayRes = await queryHoliday({ sdate: startOfWeek, edate: endOfWeek })
        const hs = []
        const holidayList = (holidayRes && (holidayRes.Data?.Data || [])) || []
        holidayList.forEach((h) => {
          const k = dayjs(h.Date).format('YYYY-MM-DD')
          if (k && k !== 'Invalid Date') hs.push(h)
        })
        holidays.value = hs
        
        // 根据课表类型调用不同的API
        let res
        // const fieldNames = getFieldNames(props.timetableType)
        if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
          // 时间视图使用原有的API
          const params = {
            ...props.searchParams,
            ExportColumn: exportColumns.value,
            StartDate: startOfWeek,
            EndDate: endOfWeek,
            PageSize: 99999,
            PageIndex: 1,
          }
          res = await queryCourseNew(params)
        } else {

          // 如果是指定对象视图（assign==1）且assignList为空，不发起课程数据请求，但保留节假日显示
          if (props.assign === 1 && 
              (!props.assignList || props.assignList.length === 0)) {
            objects.value = []
            allObjectsData.value = []
            selectedObject.value = null
            internalCourses.value = []
            loading.value = false
            resolve()
            return
          }
          // 对象视图使用对应的日历API
          const calendarParams = {
            ...(props.searchParams || {}),
            PageSize: 99999,
            PageIndex: 1,
            StartDate: startOfWeek,
            EndDate: endOfWeek,
            sort: 'StartTime',
            desc: 0,
            ExportColumn: exportColumns.value,
            IDList: props.assign==1 && props.assignList && props.assignList.length ? 
              props.assignList.map((i)=>i.ID || i.Id) : []
          }

          switch (props.timetableType) {
            case CourseTimetableTypeEnum.TeacherTimetable:
              res = await queryCalendarTeacher(calendarParams)
              break
            case CourseTimetableTypeEnum.StudentTimetable:
              res = await queryCalendarStudent(calendarParams)
              break
            case CourseTimetableTypeEnum.ClassTimetable:
              res = await queryCalendarClass(calendarParams)
              break
            case CourseTimetableTypeEnum.ClassroomTimetable:
              res = await queryCalendarClassroom(calendarParams)
              break
            default:
              res = await queryCourseNew(calendarParams)
          }
        }



        // 只有当前请求是最新的才更新数据
        if (currentRequestId === pendingRequestId) {
          if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
            // 时间视图数据处理
            internalCourses.value = (res.Data?.List || []).map(item => ({
              ...item,
              day: getDayOfWeek(dayjs(item.StartTime).format('YYYY-MM-DD'), dayjs(props.currentWeek).startOf('week').format('YYYY-MM-DD')),
              StartTime: dayjs(item.StartTime).format('HH:mm'),
              EndTime: dayjs(item.EndTime).format('HH:mm'),
              Date: dayjs(item.StartTime).format('YYYY-MM-DD'),
              type: 'course'
            }))
          } else {
            // 对象视图数据处理
            const fieldNames = getFieldNames(props.timetableType)
            // 存储所有对象的完整数据（包括CourseList）
            allObjectsData.value = res.Data || []
            // 根据assign值判断是否使用assignList
            if (props.assign==1 && props.assignList && props.assignList.length > 0) {
              // 情况1：指定对象模式且assignList有值，以assignList为基准
              const objectMap = new Map()

              props.assignList.forEach((assignItem) => {
                const id = assignItem[fieldNames.idField] || assignItem.ID || ''
                const name = assignItem[fieldNames.nameField] || assignItem.Name || '未知'
                const avatar = assignItem[fieldNames.nameField] || assignItem.Photo || ''
                const campusId = assignItem.CampusID // 保存CampusID
                // 先设置对象信息
                objectMap.set(String(id), {
                  ID: id,
                  Name: name,
                  Avatar: avatar,
                  CourseCount: 0, // 初始课程数为0
                  CampusID: campusId,
                  CampusList: props.timetableType === CourseTimetableTypeEnum.ClassroomTimetable ? assignItem.CampusList : []
                })
              })
                // 从API返回的数据中查找对应的排课信息并更新课程数量
                ; (res.Data || []).forEach((apiItem) => {
                  const id = String(apiItem[fieldNames.idField])
                  const objectItem = objectMap.get(id)

                  if (objectItem && Array.isArray(apiItem.CourseList)) {
                    // 更新课程数量
                    objectItem.CourseCount = apiItem.CourseCount
                    // 确保对象数据中包含完整的课程信息
                    if (!allObjectsData.value.find(item =>
                      String(item[fieldNames.idField]) === id
                    )) {
                      allObjectsData.value.push({
                        ...apiItem,
                        Avatar: apiItem[fieldNames.avatarField] || '',
                        [fieldNames.idField]: id,
                        [fieldNames.nameField]: objectItem.Name
                      })
                    }
                  }
                })

              objects.value = Array.from(objectMap.values())
            } else {
              // 情况2：有课对象模式或assignList没有值，按原有逻辑处理API返回的数据
              // 存储所有对象的完整数据（包括CourseList）
              allObjectsData.value = res.Data || []

              // 从API响应中提取对象数据（根据课表类型动态处理字段）
              objects.value = (res.Data || []).map(item => ({
                ID: item[fieldNames.idField],
                Name: item[fieldNames.nameField],
                Avatar: fieldNames.avatarField ? item[fieldNames.avatarField] : '',
                CourseCount: item.CourseCount,
                CampusID: item.CampusID,
                CampusList: props.timetableType === CourseTimetableTypeEnum.ClassroomTimetable ? item.CampusList : []
              }))
            }
            // 如果没有选中的对象，默认选中第一个，并通知父级	h
            let selectObjectFlag=!!selectedObject.value&&objects.value.some(obj=>obj.ID===selectedObject.value.ID)
            if (!selectObjectFlag && objects.value.length > 0) {
              selectedObject.value = objects.value[0]
              emit('object-selected', { object: selectedObject.value, timetableType: props.timetableType })
            }else if(selectObjectFlag){
              // 保持原有选中对象
              selectedObject.value = objects.value.find(obj=>obj.ID===selectedObject.value.ID)
              emit('object-selected', { object: selectedObject.value, timetableType: props.timetableType })
            }else{
              // 无对象可选
              selectedObject.value = null
            }

            // 转置视图：加载所有对象的课程数据
            // 原始视图：只加载选中对象的课程数据
            updateCoursesForTransView()
          }


          // 设置滚动同步（当对象列显示时）
          if (showObjectColumn.value) {
            nextTick(() => {
              setupScrollSync()
            })
          }

          // console.log('数据加载完成:', {
          //   课表类型: props.timetableType,
          //   对象数量: objects.value.length,
          //   选中对象: selectedObject.value?.Name,
          //   节假日数量: holidays.value.length,
          //   显示对象列: showObjectColumn.value
          // })
        }
        resolve()
      } catch (error) {
        console.error('加载课程数据失败:', error)
        // 只有当前请求是最新的才更新数据
        if (currentRequestId === pendingRequestId) {
          internalCourses.value = []
          reject(error)
        } else {
          resolve()
        }
      } finally {
        // 只有当前请求是最新的才关闭loading
        if (currentRequestId === pendingRequestId) {
          loading.value = false
          // 通知父组件数据加载完成
          emit('data-loaded')
        }
      }
    }, 200) // 0.2秒防抖延迟
  })
}


// 监听当前周变化，更新时间线显示
watch(() => [props.currentWeek, props.searchParams], (newVal, oldVal) => {
  const forceRefresh = oldVal && newVal[1] !== oldVal[1]
  loadCourseData(forceRefresh).catch(() => {
    // 静默处理错误，避免控制台报错
  })
  updateCurrentTimePosition()
}, { immediate: true, deep: true })

// 监听课表类型和分配列表变化，确保兼容性
watch(() => [props.timetableType, props.assignList], (newVal, oldVal) => {
  // 如果课表类型改变，重置对象选择状态
  if (oldVal && newVal[0] !== oldVal[0]) {
    selectedObject.value = null
    objects.value = []
  }

  // 重新加载数据
  loadCourseData(true).catch(() => {
    // 静默处理错误，避免控制台报错
  })
}, { immediate: false, deep: true })

// 更新课程位置时通知父组件
const updateCoursePosition = (course, newDay, newStartTime, newEndTime) => {
  // 找到课程在数组中的索引
  const courseIndex = internalCourses.value.findIndex(c => c.ID === course.ID)
  if (courseIndex === -1) return

  // 更新内部数据
  const updatedCourse = {
    ...course,
    day: newDay,
    StartTime: newStartTime,
    EndTime: newEndTime,
    Date: dayjs(weekDates.value[newDay]).format('YYYY-MM-DD') // 转换为YYYY-MM-DD格式的字符串
  }
  internalCourses.value[courseIndex] = updatedCourse
  // console.log('课程位置已更新:', {
  //   课程: course.ShiftName,
  //   新天: props.weekDays[newDay],
  //   新时间: `${newStartTime} - ${newEndTime}`
  // })
}

// === 拖拽功能状态管理 ===
const dragState = ref({
  isDragging: false,        // 是否正在拖拽
  isPreparing: false,       // 🆕 是否处于拖拽准备状态（鼠标按下但未超过阈值）
  draggedCourse: null,      // 被拖拽的课程对象
  originalCourse: null,     // 原始课程数据备份
  preventClick: false,      // 🆕 是否阻止点击事件（拖拽后阻止点击）
  startPosition: {          // 拖拽开始位置
    mouseX: 0,
    mouseY: 0,
    courseX: 0,
    courseY: 0
  },
  currentPosition: {        // 当前拖拽位置
    x: 0,
    y: 0
  },
  previewPosition: {        // 预览位置（对齐后）
    day: 0,
    hour: 0
  },
  dragThreshold: 5,        // 🆕 拖拽阈值（像素），超过此距离才进入真正的拖拽模式
  timeSnap: 1,             // 时间对齐精度（分钟）
  animationFrameId: null,  // 动画帧ID，用于性能优化
  currentTime: '',         // 当前拖拽对应的时间字符串
  targetDay: -1,           // 当前拖拽目标的天（0-6，-1表示无效）
  // 高度调整及尺寸状态
  isResizing: false,       // 是否正在调整高度
  resizeDirection: '',     // 调整方向：'top' | 'bottom'
  originalWidth: 0,        // 拖拽开始时，课程块的原始宽度
  originalHeight: 0,       // 拖拽开始时，课程块的原始高度
  originalStartTime: '',   // 原始开始时间
  originalEndTime: '',     // 原始结束时间
  minDuration: 0.5,        // 最小持续时间（小时）
  maxDuration: 8           // 最大持续时间（小时）
})


const scheduleContainerRef = ref(null)
const objectListRef = ref(null)
const isFullscreen = ref(false)
const courseDragConfirmDialogRef = ref(null)
const scheduleDragConfirmDialogRef = ref(null)
const courseDetailPopoverRef = ref(null)

// 课程详情弹窗相关数据
const selectedCourseData = ref({})
const virtualRef = ref()
const isPopoverVisible = ref(false)

// 添加悬浮状态管理
const hoveredCourse = ref(null)
const hoverTimer = ref(null)

// 临时占位块状态管理
const temporaryBlock = ref(null)

// 处理课程容器的点击事件（空白区域点击）
const handleContainerClick = (event, objectId) => {
  // 检查点击是否在课程块上
  const clickedOnCourse = event.target.closest('.course-item')
  if (clickedOnCourse) {
    return // 如果点击在课程块上，不处理
  }
  preventAutoHide.value = false
  //
  hidePopover()

  // 阻止事件冒泡
  event.stopPropagation()

  // 获取点击位置相对于当天容器的坐标
  const dayContainer = event.currentTarget
  const rect = dayContainer.getBoundingClientRect()
  const relativeY = event.clientY - rect.top

  // 将Y坐标转换为时间，确定点击在哪个30分钟时间段内
  const hourDecimal = relativeY / defaultHeight.value + startHour.value  // 加上时间范围开始偏移

  // 计算点击位置所在的30分钟时间段
  // 每小时分为2个30分钟段：0-0.5, 0.5-1, 1-1.5, 1.5-2, ...
  const halfHourIndex = Math.floor(hourDecimal * 2) // 获取是第几个30分钟段
  const blockStartHour = halfHourIndex / 2 // 该30分钟段的开始时间
  const blockEndHour = blockStartHour + 0.5 // 该30分钟段的结束时间

  // 边界限制：确保在时间范围内
  const clampedStartHour = Math.max(startHour.value, Math.min(endHour.value - 0.5, blockStartHour))
  const clampedEndHour = Math.max(startHour.value + 0.5, Math.min(endHour.value, blockEndHour))

  // 确保结束时间不会小于开始时间
  const finalStartHour = clampedStartHour
  const finalEndHour = Math.max(clampedEndHour, clampedStartHour + 0.5)

  // 计算开始和结束时间
  const StartTime = hourToTimeString(finalStartHour)
  const EndTime = hourToTimeString(finalEndHour)

  // 创建临时占位块（转置视图：记录objectId而不是day）
  temporaryBlock.value = {
    ID: 'temp_' + Date.now(),
    objectId: objectId, // 转置视图：保存对象ID
    day: selectedDayIndex.value, // 转置视图：使用选中的星期
    StartTime: StartTime,
    EndTime: EndTime,
    type: 'temporary',
    isPassive: false // 主动点击创建，不是被动同步
  }
}

// 隐藏临时占位块
const hideTemporaryBlock = () => {
  temporaryBlock.value = null
}

// 点击添加排课
const addSchedule = () => {
  if (!temporaryBlock.value) return

  const block = temporaryBlock.value
  const selectedDate = dayjs(weekDates.value[block.day]).format('YYYY-MM-DD')

  // 准备基础的日期时间信息
  const baseScheduleData = {
    date: selectedDate,
    timeRange: {
      startTime: block.StartTime,
      endTime: block.EndTime,
    },
    timetableType: props.timetableType
  }

  let scheduleData = { ...baseScheduleData }

  // 根据课表类型准备不同的数据
  if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
    // 时间课表：只需要日期和时间信息
    // console.log('准备添加时间课程:', scheduleData)
  } else {
    // 转置视图：根据点击的列找到对应的对象
    const clickedObject = objects.value.find(obj => obj.ID === block.objectId)
    
    if (clickedObject) {
      const campusId = (clickedObject.CampusID || clickedObject.CampusList) || ''
      scheduleData = {
        ...scheduleData,
        object: {
          id: clickedObject.ID,
          name: clickedObject.Name,
          avatar: clickedObject.Avatar,
          campusId: campusId
        }
      }
      // 除了老师课表，其他的都有校区id
      if (props.timetableType !== CourseTimetableTypeEnum.TeacherTimetable && campusId) {
        scheduleData.campusId = campusId
      }
    } else {
      // 没有找到对象，当做空排课处理
      console.warn('未找到点击列对应的对象，objectId:', block.objectId)
    }
  }

  // ✅ 清除临时占位块，避免遮挡新创建的课程
  temporaryBlock.value = null

  // 触发添加课程事件，传递准备好的数据给父组件
  emit('canlendar-dbclick-add-course', scheduleData)

  // console.log('最终准备的课程数据:', scheduleData)
}

// 获取临时占位块的样式
const getTemporaryBlockStyle = () => {
  if (!temporaryBlock.value) {
    return { display: 'none' }
  }

  const block = temporaryBlock.value
  let blockStartHour = parseTime(block.StartTime)
  let blockEndHour = parseTime(block.EndTime)

  // 特殊处理跨天情况：当结束时间是00:00时，表示第二天的00:00，应该当作24:00计算
  if (block.EndTime === '00:00' && blockStartHour > blockEndHour) {
    blockEndHour = 24
  }

  const duration = blockEndHour - blockStartHour
  return {
    position: 'absolute',
    top: `${(blockStartHour - startHour.value) * defaultHeight.value + 2}px`,  // 调整top位置，减去时间范围开始偏移
    left: '4px',
    width: 'calc(100% - 8px)', // 减去左右间距
    height: `${duration * defaultHeight.value - 4}px`,
    borderRadius: '8px',
    boxSizing: 'border-box',
    border: '1px dashed #C0C4CC',
    zIndex: 10,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  }
}

// 计算临时占位块是否为大尺寸（高度大于30px）
const isTemporaryBlockLarge = computed(() => {
  if (!temporaryBlock.value) return false

  const block = temporaryBlock.value
  let blockStartHour = parseTime(block.StartTime)
  let blockEndHour = parseTime(block.EndTime)

  // 特殊处理跨天情况
  if (block.EndTime === '00:00' && blockStartHour > blockEndHour) {
    blockEndHour = 24
  }

  const duration = blockEndHour - blockStartHour
  const height = duration * defaultHeight.value - 4

  return height > 30
})

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  emit('fullscreen-change', isFullscreen.value)
}

// 鼠标悬浮处理函数
const handleCourseMouseEnter = (course) => {
  // 拖拽或调整高度时不处理悬浮效果
  if (dragState.value.isDragging || dragState.value.isResizing) return

  // 清除之前的定时器
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
  }

  // // 延迟0.5秒后显示毛玻璃效果
  // hoverTimer.value = setTimeout(() => {
  //   // 再次检查是否在拖拽或调整状态（防止定时器执行时状态改变）
  //   if (!dragState.value.isDragging && !dragState.value.isResizing) {
  //     hoveredCourse.value = course
  //   }
  // }, 500)
}

const handleCourseMouseLeave = () => {
  // 清除定时器
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }

  // 立即清除毛玻璃效果
  hoveredCourse.value = null
}

// 隐藏popover
const hidePopover = () => {
  if (courseDetailPopoverRef.value) {
    courseDetailPopoverRef.value.hide()
    isPopoverVisible.value = false
  }
}
// 处理点击外部区域隐藏popover
const handleClickOutside = (event, item = {}) => {
  if (!isPopoverVisible.value) {
    return
  }
  preventAutoHide.value = false


  // 如果点击的是当前显示popover的课程项，不隐藏popover
  if (selectedCourseData.value?.ID === item.ID) {
    return
  }

  // 检查点击是否在popover内部
  const target = event.target
  const popoverElement = document.querySelector('.course-detail-popover')
  if (popoverElement && popoverElement.contains(target)) {
    return
  }

  // 点击在其他区域，隐藏popover
  hidePopover()
}

// 处理课程点击事件
const handleCourseClick = async (event, course) => {
  // 🆕 如果刚刚发生过拖拽准备，阻止点击事件
  if (dragState.value.preventClick) {
    dragState.value.preventClick = false
    event.preventDefault()
    event.stopPropagation()
    // console.log('阻止拖拽后的点击事件')
    return
  }

  if (!dragState.value.isDragging && !dragState.value.isResizing) {
    // 阻止事件冒泡，避免触发容器点击
    event.stopPropagation()
    preventAutoHide.value = true

    // 如果点击的是同一个课程且popover已显示，则保持显示状态，不做任何操作
    if (isPopoverVisible.value && selectedCourseData.value?.ID === course.ID) {
      return
    }


    // 如果是切换到不同的课程，先保存新的课程数据和目标元素
    let courseData = {
      ...course,
      EndTime: course.Date + ' ' + course.EndTime,
      StartTime: course.Date + ' ' + course.StartTime,
      // 确保包含 Finished 属性
      Finished: course.Finished,
      CourseStatus: course.CourseStatus
    }

    // 等待 DOM 更新完成
    await nextTick()

    // 更新目标元素和课程数据
    virtualRef.value = event.currentTarget
    selectedCourseData.value = courseData

    // 如果当前已有显示的弹窗，先将其隐藏
    if (isPopoverVisible.value) {
      isPopoverVisible.value = false
      await nextTick()
    }

    // 显示新的课程详情弹窗
    if (courseDetailPopoverRef.value) {
      isPopoverVisible.value = true
      courseDetailPopoverRef.value.show()
    }

    // emit('course-click', { course, event })
  }
}

// 判断课程是否应该显示毛玻璃效果
const shouldShowBlurEffect = (course) => {
  if (!hoveredCourse.value) {
    return false
  }

  // 拖拽或调整高度时不显示毛玻璃效果，避免视觉干扰
  if (dragState.value.isDragging || dragState.value.isResizing) {
    return false
  }

  // 如果是同名课程（包括带序号的），不显示毛玻璃效果
  const hoveredCourseName = hoveredCourse.value.ShiftName.replace(/\(\d+\)$/, '') // 去掉序号
  const currentCourseName = course.ShiftName.replace(/\(\d+\)$/, '') // 去掉序号

  return hoveredCourseName !== currentCourseName
}

// === 拖拽功能核心实现 ===

// 获取动态列宽度 - 修正版本：获取整个天的宽度
const getDayColumnWidth = () => {
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  if (!scheduleGrid) return minWidth; // 默认值

  const dayColumn = scheduleGrid.querySelector('.day-column-container');
  if (!dayColumn) return minWidth; // 默认值

  const rect = dayColumn.getBoundingClientRect();
  return rect.width;
};

// 新增：获取课程在天内的实际位置信息
const getCoursePositionInDay = (course, courseElement) => {
  if (!courseElement) return null;

  // 获取课程元素的位置
  const courseRect = courseElement.getBoundingClientRect();

  // 获取该天容器的位置
  const dayContainer = courseElement.closest('.day-column-container');
  if (!dayContainer) return null;

  const dayRect = dayContainer.getBoundingClientRect();

  // 计算课程相对于天容器的位置
  const relativeX = courseRect.left - dayRect.left;
  const relativeY = courseRect.top - dayRect.top;

  // 获取schedule-grid的位置，计算课程相对于整个网格的位置
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  if (!scheduleGrid) return null;

  const gridRect = scheduleGrid.getBoundingClientRect();
  
  // ✅ 修复：加上滚动偏移量，确保拖拽与滚动同步
  const scrollContainer = scheduleContainerRef.value?.querySelector('.time-schedule-content');
  const scrollTop = scrollContainer?.scrollTop || 0;
  const scrollLeft = scrollContainer?.scrollLeft || 0;
  
  // 计算绝对位置时需要加上滚动偏移
  const absoluteX = courseRect.left - gridRect.left + scrollLeft;
  const absoluteY = courseRect.top - gridRect.top + scrollTop;

  return {
    relativeX,      // 相对于天容器的X位置
    relativeY,      // 相对于天容器的Y位置
    absoluteX,      // 相对于整个网格的X位置（包含滚动）
    absoluteY,      // 相对于整个网格的Y位置（包含滚动）
    dayWidth: dayRect.width,  // 该天的总宽度
    courseWidth: courseRect.width,  // 课程的实际宽度
    courseHeight: courseRect.height // 课程的实际高度
  };
};

// 像素坐标转换为时间和日期 (5分钟精度) - 以课块中心为准
const pixelToTime = (x, y) => {
  const dayWidth = getDayColumnWidth(); // 动态获取列宽

  // 计算课块中心点位置
  let centerX;
  if (dragState.value.isDragging && dragState.value.originalWidth) {
    // 拖拽时使用记录的原始宽度计算中心点
    centerX = x + dragState.value.originalWidth / 2;
  } else {
    // 其他情况使用默认宽度估算（占列宽的80%）
    const estimatedWidth = dayWidth * 0.8;
    centerX = x + estimatedWidth / 2;
  }

  // 🔥 关键修改：基于 filteredWeekData 计算天数
  // 计算在过滤后的列中的索引（0 到 filteredWeekData.length - 1）
  let filteredDayIndex = Math.floor(centerX / dayWidth)
  // 限制在有效范围内
  filteredDayIndex = Math.max(0, Math.min(filteredWeekData.value.length - 1, filteredDayIndex))
  
  // 将过滤后的索引映射回原始的天数索引（0-6）
  let dayIndex = filteredWeekData.value[filteredDayIndex]?.originalIndex ?? 0

  // 简化的边界处理：只在微小移动时保持原天
  if (dragState.value.isDragging && dragState.value.draggedCourse) {
    const originalDay = dragState.value.draggedCourse.day
    
    // 找到原始天在过滤后列表中的位置
    const originalFilteredIndex = filteredWeekData.value.findIndex(d => d.originalIndex === originalDay)
    
    if (originalFilteredIndex !== -1) {
      const originalDayStart = originalFilteredIndex * dayWidth
      const originalCenterX = originalDayStart + (dragState.value.originalWidth || dayWidth * 0.8) / 2

      // 只有在同一天内的小范围移动时才保持原天（范围缩小到30px）
      if (Math.abs(centerX - originalCenterX) < 30 && filteredDayIndex === originalFilteredIndex) {
        dayIndex = originalDay

        if (Math.random() < 0.1) { // 降低调试输出频率
          // console.log('保持原天（基于中心点）:', {
          //   中心点X: Math.round(centerX),
          //   原始中心点X: Math.round(originalCenterX),
          //   原始天: originalDay,
          //   距离原始中心点: Math.round(centerX - originalCenterX),
          //   保持天: dayIndex,
          //   动态列宽: dayWidth
          // })
        }
      }
    }

    // 添加详细的天数计算调试信息
    if (Math.random() < 0.05) { // 偶尔输出调试信息
      // console.log('天数计算详情（基于中心点）:', {
      //   输入X: Math.round(x),
      //   课块宽度: dragState.value.originalWidth || dayWidth * 0.8,
      //   计算中心点X: Math.round(centerX),
      //   动态列宽: dayWidth,
      //   除法结果: centerX / dayWidth,
      //   向下取整: Math.floor(centerX / dayWidth),
      //   最终dayIndex: dayIndex,
      //   范围限制前: dayIndex,
      //   范围限制后: Math.max(0, Math.min(6, dayIndex)),
      //   对应天名: props.weekDays[Math.max(0, Math.min(6, dayIndex))] || '超出范围'
      // })
    }
  }

  const hourDecimal = y / defaultHeight.value + startHour.value  // 加上时间范围开始偏移
  const actualHour = hourDecimal             // 调整后的实际小时

  // 时间对齐到1分钟网格 (每小时60个1分钟间隔)
  const alignedHour = Math.round(actualHour * 60) / 60

  return {
    day: Math.max(0, Math.min(6, dayIndex)),
    hour: Math.max(startHour.value, Math.min(endHour.value, alignedHour)) // 限制在时间范围内
  }
}

// 小时数转换为时间字符串 (1分钟精度)
const hourToTimeString = (hour) => {
  // 边界处理：确保输入在 [0, 24] 范围内
  const clampedHour = Math.max(0, Math.min(24, hour));

  // 1. 将小时转换为总分钟数
  const totalMinutes = clampedHour * 60;

  // 2. 对齐到最近的1分钟网格
  const alignedMinutes = Math.round(totalMinutes);

  // 3. 计算小时和分钟
  let h = Math.floor(alignedMinutes / 60);
  let m = alignedMinutes % 60;

  // 4. 特殊处理24:00的情况
  if (h >= 24) {
    return '24:00';
  }

  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`
}

// 获取容器的边界信息
const getContainerBounds = () => {
  const container = scheduleContainerRef.value
  if (!container) return null

  const scheduleGrid = container.querySelector('.schedule-grid')
  if (!scheduleGrid) return null

  const rect = scheduleGrid.getBoundingClientRect()
  return {
    left: rect.left,
    top: rect.top,
    width: rect.width,
    height: rect.height
  }
}

// 开始拖拽
const handleCourseMouseDown = (event, course) => {
  // 检查是否启用拖拽功能
  if (!props.enableDrag) return

  // 只有按下鼠标左键才开始拖拽
  if (event.button !== 0) return

  // 检查点击位置，避免与调整高度手柄冲突
  const target = event.target

  // 如果点击的是调整手柄，不进行拖拽
  if (target.classList.contains('resize-handle') ||
    target.classList.contains('resize-handle-top') ||
    target.classList.contains('resize-handle-bottom')) {
    return
  }

  // 阻止默认行为和事件冒泡
  event.preventDefault()
  event.stopPropagation()

  // 🆕 只设置准备状态，不立即进入拖拽模式
  dragState.value.isPreparing = true
  dragState.value.draggedCourse = { ...course } // 创建副本
  dragState.value.originalCourse = { ...course }

  // 获取课程元素并记录其精确尺寸
  const courseElement = event.currentTarget
  const rect = courseElement.getBoundingClientRect()
  dragState.value.originalWidth = rect.width
  dragState.value.originalHeight = rect.height

  // 记录鼠标的初始位置
  dragState.value.startPosition.mouseX = event.clientX
  dragState.value.startPosition.mouseY = event.clientY
  
  // 获取滚动容器和网格
  const scrollContainer = scheduleContainerRef.value?.querySelector('.time-schedule-content')
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid')
  
  if (scrollContainer && scheduleGrid) {
    const gridRect = scheduleGrid.getBoundingClientRect()
    const currentScrollTop = scrollContainer.scrollTop
    const currentScrollLeft = scrollContainer.scrollLeft
    
    // 计算鼠标相对于网格内容的位置
    const mouseXInGrid = event.clientX - gridRect.left + currentScrollLeft
    const mouseYInGrid = event.clientY - gridRect.top + currentScrollTop
    
    // 稍后会用 getCoursePositionInDay 获取课程位置，这里先预留
    // 我们需要记录鼠标点击位置相对于课程左上角的偏移
    dragState.value.startPosition.mouseXInGrid = mouseXInGrid
    dragState.value.startPosition.mouseYInGrid = mouseYInGrid
  }

  // 动态获取列宽度
  const dayWidth = getDayColumnWidth()

  // 获取课程元素
  const courseElementForPosition = event.target.closest('.course-item')

  // 优先使用精确的DOM位置信息
  const positionInfo = getCoursePositionInDay(course, courseElementForPosition)

  let finalStartX, finalStartY
  let useActualDOMPosition = false

  if (positionInfo) {
    // 使用DOM获取的精确位置
    finalStartX = positionInfo.absoluteX
    finalStartY = positionInfo.absoluteY
    useActualDOMPosition = true

    // console.log('使用精确DOM位置作为起始位置:', {
    //   课程: course.ShiftName,
    //   相对天位置: { x: Math.round(positionInfo.relativeX), y: Math.round(positionInfo.relativeY) },
    //   相对网格位置: { x: Math.round(positionInfo.absoluteX), y: Math.round(positionInfo.absoluteY) },
    //   天宽度: positionInfo.dayWidth,
    //   课程尺寸: { width: positionInfo.courseWidth, height: positionInfo.courseHeight },
    //   动态列宽: dayWidth
    // })
  } else {
    // 降级使用理论位置计算
    const courseStart = parseTime(course.StartTime)
    finalStartX = course.day * dayWidth  // 使用动态列宽
    finalStartY = courseStart * defaultHeight.value  // 不再需要减1偏移，现在从00:00开始

    console.warn('DOM位置获取失败，使用理论位置:', {
      课程: course.ShiftName,
      理论位置: { x: finalStartX, y: finalStartY },
      原因: '无法获取精确位置信息',
      课程开始时间: course.StartTime,
      解析后时间: courseStart,
      网格偏移: courseStart - 1
    })
  }

  // 设置起始位置（用于鼠标移动计算）
  dragState.value.startPosition.courseX = finalStartX
  dragState.value.startPosition.courseY = finalStartY
  
  // 计算鼠标相对于课程块左上角的偏移量
  if (dragState.value.startPosition.mouseXInGrid !== undefined) {
    dragState.value.startPosition.mouseOffsetX = dragState.value.startPosition.mouseXInGrid - finalStartX
    dragState.value.startPosition.mouseOffsetY = dragState.value.startPosition.mouseYInGrid - finalStartY
  } else {
    // 降级方案：使用鼠标相对视口的位置
    dragState.value.startPosition.mouseOffsetX = 0
    dragState.value.startPosition.mouseOffsetY = 0
  }

  // 初始化当前位置，让预览块从正确位置开始
  dragState.value.currentPosition.x = finalStartX
  dragState.value.currentPosition.y = finalStartY

  // 🆕 初始化拖拽指示器信息 - 使用原始时间，不进行对齐
  // 这样点击时不会触发对齐，只有开始拖动时才对齐
  dragState.value.currentTime = course.StartTime
  dragState.value.targetDay = course.day

  // 添加全局事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)

  // 🆕 不再立即设置光标样式，等真正拖拽时再设置
  // document.body.style.cursor = 'grabbing'

  // console.log('鼠标按下，进入拖拽准备状态:', {
  //   课程: course.ShiftName,
  //   原始天: props.weekDays[course.day],
  //   起始位置: { x: Math.round(finalStartX), y: Math.round(finalStartY) },
  //   使用DOM位置: useActualDOMPosition,
  //   动态列宽: dayWidth,
  //   拖拽基准: `鼠标从(${event.clientX}, ${event.clientY})开始，课程从(${Math.round(finalStartX)}, ${Math.round(finalStartY)})开始`
  // })
}

// 拖拽中 - 优化性能版本
const handleMouseMove = (event) => {
  // 🆕 只有在准备状态或已拖拽状态下才处理
  if (!dragState.value.isPreparing && !dragState.value.isDragging) return

  event.preventDefault()

  // 🆕 计算鼠标移动距离
  const deltaX = event.clientX - dragState.value.startPosition.mouseX
  const deltaY = event.clientY - dragState.value.startPosition.mouseY
  const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY)

  // 🆕 如果还在准备状态，检查是否超过阈值
  if (dragState.value.isPreparing && !dragState.value.isDragging) {
    if (distance < dragState.value.dragThreshold) {
      // 移动距离不足，还不算真正的拖拽，直接返回
      return
    }
    // 超过阈值，进入真正的拖拽模式
    dragState.value.isDragging = true
    dragState.value.isPreparing = false
    dragState.value.preventClick = true  // 🆕 只在这里设置 preventClick，表示发生了拖拽
    document.body.style.cursor = 'grabbing'

    // console.log('超过拖拽阈值，开始拖拽:', {
    //   移动距离: Math.round(distance),
    //   阈值: dragState.value.dragThreshold,
    //   课程: dragState.value.draggedCourse.ShiftName
    // })
  }

  // 使用requestAnimationFrame节流，避免过度更新
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
  }

  dragState.value.animationFrameId = requestAnimationFrame(() => {
    // 获取容器边界信息
    const bounds = getContainerBounds()
    if (!bounds) return

    // 动态获取列宽度
    const dayWidth = getDayColumnWidth()
    
    // 获取当前滚动容器的位置（用于修正鼠标位置）
    const scrollContainer = scheduleContainerRef.value?.querySelector('.time-schedule-content')
    const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid')
    if (!scrollContainer || !scheduleGrid) return
    
    const gridRect = scheduleGrid.getBoundingClientRect()
    const currentScrollTop = scrollContainer.scrollTop
    const currentScrollLeft = scrollContainer.scrollLeft
    
    // 计算鼠标相对于网格内容的实际位置（视口位置 - 网格视口位置 + 滚动偏移）
    const mouseXInGrid = event.clientX - gridRect.left + currentScrollLeft
    const mouseYInGrid = event.clientY - gridRect.top + currentScrollTop
    
    // 计算课程应该在的位置（鼠标在网格中的位置 - 鼠标相对课程的偏移）
    let newX = mouseXInGrid - dragState.value.startPosition.mouseOffsetX
    let newY = mouseYInGrid - dragState.value.startPosition.mouseOffsetY

    // 转置视图：锁定X轴，不允许横向拖动到其他对象列
    // 保持课程在原始列中
    newX = dragState.value.startPosition.courseX

    // Y轴边界：限制在时间范围内
    const visibleGridHeight = visibleHoursCount.value * defaultHeight.value  // 可见时间范围的高度
    const courseHeight = parseFloat(getCourseStyle(dragState.value.draggedCourse, dragState.value.draggedCourse.day, getStatusColor(dragState.value.draggedCourse)).height) || 60

    newY = Math.max(0, Math.min(visibleGridHeight - courseHeight, newY))

    // 更新当前位置
    dragState.value.currentPosition.x = newX
    dragState.value.currentPosition.y = newY

    // 计算预览位置（对齐后的位置）
    // 转置视图：day保持原值，只计算hour
    const day = dragState.value.draggedCourse.day
    const { hour } = pixelToTime(newX, newY)
    dragState.value.previewPosition.day = day
    dragState.value.previewPosition.hour = hour

    // 更新拖拽指示器信息
    dragState.value.currentTime = hourToTimeString(hour)
    dragState.value.targetDay = day

    // 减少控制台输出频率，提高性能
    // if (import.meta.env.DEV && Math.random() < 0.05) {
    //   // console.log('拖拽实时位置:', {
    //   //   当前像素: { x: Math.round(newX), y: Math.round(newY) },
    //   //   预览位置: { day: props.weekDays[day], time: hourToTimeString(hour) },
    //   //   动态列宽: dayWidth
    //   // })
    // }

    // 添加时间指示器调试信息
    // if (import.meta.env.DEV && Math.random() < 0.1) {
    //   console.log('时间指示器调试:', {
    //     isDragging: dragState.value.isDragging,
    //     currentTime: dragState.value.currentTime,
    //     hour: hour,
    //     startHour: startHour.value,
    //     endHour: endHour.value,
    //     计算位置: (parseTime(dragState.value.currentTime) - startHour.value) * defaultHeight.value,
    //     可见高度: visibleHeight.value
    //   })
    // }
  })
}

// 结束拖拽
const handleMouseUp = (event) => {
  // 🆕 只要是准备状态或拖拽状态都需要处理
  if (!dragState.value.isPreparing && !dragState.value.isDragging) return

  event.preventDefault()

  // 🆕 如果只是准备状态但未真正拖拽，说明用户只是点击，清理状态即可
  if (dragState.value.isPreparing && !dragState.value.isDragging) {
    // console.log('鼠标松开，但未超过拖拽阈值，视为正常点击')
    
    dragState.value.isPreparing = false
    dragState.value.draggedCourse = null
    dragState.value.originalCourse = null

    // 移除全局事件监听
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)

    // 恢复文档光标样式
    document.body.style.cursor = ''
    
    // 🆕 不设置 preventClick，允许点击事件正常触发
    return
  }

  // 清理动画帧
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
    dragState.value.animationFrameId = null
  }

  // 移除全局事件监听
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)

  // 恢复文档光标样式
  document.body.style.cursor = ''

  // 计算最终位置（对齐到1分钟网格）
  // 转置视图：day保持不变（因为不允许横向拖动），只计算时间变化
  const day = dragState.value.draggedCourse.day // 保持原来的day（星期几）
  const { hour } = pixelToTime(
    dragState.value.currentPosition.x,
    dragState.value.currentPosition.y
  )

  // 计算新的开始和结束时间
  const course = dragState.value.draggedCourse
  const originalStart = parseTime(course.StartTime)
  const originalEnd = parseTime(course.EndTime)
  const courseDuration = originalEnd - originalStart

  const newStartTime = hourToTimeString(hour)
  const newEndTime = hourToTimeString(hour + courseDuration)

  // 检查是否发生了实际移动（转置视图中只检查时间变化，不检查day变化）
  const hasPositionChanged = course.StartTime !== newStartTime
  if (hasPositionChanged) {
    // 构建确认信息
    const confirmInfo = {
      course: { ...course },
      originalPosition: {
        day: course.day,
        dayName: props.weekDays[course.day],
        StartTime: course.StartTime,
        EndTime: course.EndTime,
        Date: course.Date
      },
      newPosition: {
        day: day,
        dayName: props.weekDays[day],
        StartTime: newStartTime,
        EndTime: newEndTime,
        Date: dayjs(weekDates.value[day]).format('YYYY-MM-DD') // 转换为YYYY-MM-DD格式的字符串
      },
      changeDetails: {
        dayChanged: course.day !== day,
        timeChanged: course.StartTime !== newStartTime,
        dayChange: course.day !== day ? `${props.weekDays[course.day]} → ${props.weekDays[day]}` : null,
        timeChange: course.StartTime !== newStartTime ? `${course.StartTime}-${course.EndTime} → ${newStartTime}-${newEndTime}` : null
      }
    }

    // 使用弹框组件显示确认对话框
    showDragConfirmDialog(confirmInfo)
  }

  // 立即重置拖拽状态
  dragState.value.isDragging = false
  dragState.value.isPreparing = false  // 🆕 同时清理准备状态
  dragState.value.draggedCourse = null
  dragState.value.originalCourse = null
  
  // 🆕 延迟清除 preventClick，确保本次点击事件被阻止
  setTimeout(() => {
    dragState.value.preventClick = false
  }, 100)

  // 清除拖拽指示器信息
  dragState.value.currentTime = ''
  dragState.value.targetDay = -1

  // 清除悬浮状态
  hoveredCourse.value = null
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }

  // 触发布局重新计算
  nextTick(() => {
    recalculateAllLayouts()
  })
}

// 重新计算所有布局
const recalculateAllLayouts = () => {
  // 由于使用了computed，布局会自动重新计算
  // 这里只需要确保UI更新
  // console.log('重新计算所有布局...')
}
const CoursePlanViaCopy = window.$xgj.op("CoursePlanViaCopy") // 复制移动排课
const NewCourse_ScheduleEdit = window.$xgj.op("NewCourse_ScheduleEdit")//编辑日程
// === 课程拖拽确认处理 ===
// 显示拖拽确认弹框
const showDragConfirmDialog = async (confirmInfo) => {
  if (confirmInfo.course.type === 'schedule') {
    if (!scheduleDragConfirmDialogRef.value) return
  } else {
    if (!courseDragConfirmDialogRef.value) return
  }
  if(confirmInfo.course.type === 'course' && ! CoursePlanViaCopy ){
    ElMessage.warning('暂无“复制/移动排课”的权限，请联系管理员！')
    return
  }
  if(confirmInfo.course.type === 'schedule' && ! NewCourse_ScheduleEdit ){
    ElMessage.warning('暂无“编辑日程”的权限，请联系管理员！')
    return
  }
  // ✅ 只对排课进行限制：已上课的排课不允许打开弹框（日程不受限制）
  if (confirmInfo.course.type === 'course' && (confirmInfo.course.Finished === 1 || confirmInfo.course.Finished === '1')) {
    ElMessage.warning(transToConfigDescript('"已上课"的排课，不允许修改！'))
    return
  }
  // 已取消的排课不许修改
  if (confirmInfo.course.type === 'course' && (confirmInfo.course.Finished === 2|| confirmInfo.course.Finished === '2')) {
    ElMessage.warning('"已取消"的排课，不允许修改！')
    return
  }
  

  try {
    let result
    if (confirmInfo.course.type === 'schedule') {
      result = await scheduleDragConfirmDialogRef.value.open(confirmInfo)
    } else {
      result = await courseDragConfirmDialogRef.value.open(confirmInfo)
    }

    if (result.confirmed) {
      // // 用户确认了变更
      // const { course, newPosition } = result.data
      // updateCoursePosition(
      //   course,
      //   newPosition.day,
      //   newPosition.StartTime,
      //   newPosition.EndTime
      // )
      handleRefreshData()
    } else {
      // 用户取消了变更，重新布局计算，确保课程回到原位置
      nextTick(() => {
        recalculateAllLayouts()
      })
    }
  } catch (error) {
    console.error('拖拽确认弹框处理失败:', error)
    // 发生错误时也重新布局计算
    nextTick(() => {
      recalculateAllLayouts()
    })
  }
}

// 处理刷新数据事件
const handleRefreshData = () => {
  // 重新加载课程数据
  loadCourseData(true).catch((error) => {
    console.error('刷新课程数据失败:', error)
  })
}

function rgbaToHexWithBg(r, g, b, a = 1, bg = [255, 255, 255]) {
  // 把透明色叠加到背景色上
  const blend = (fg, bg) => Math.round(fg * a + bg * (1 - a));
  const toHex = (n) => n.toString(16).padStart(2, "0");

  const R = blend(r, bg[0]);
  const G = blend(g, bg[1]);
  const B = blend(b, bg[2]);

  return `#${toHex(R)}${toHex(G)}${toHex(B)}`;
}

// 重新设计getCourseStyle函数，使用绝对定位
const getCourseStyle = (course, day, color) => {
  // 转置视图中，使用对象维度的布局计算
  const fieldNames = getFieldNames(props.timetableType)
  let objectId
  
  if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
    // 时间视图没有对象ID，使用简单布局
    objectId = 'time'
  } else {
    objectId = course[fieldNames.idField]
  }
  
  // 获取该对象在选中日期的布局
  const layoutResult = getCachedObjectDayLayout(objectId, selectedDayIndex.value)
  const layout = layoutResult.layouts.get(course.ID)
  
  // 如果课程被标记为隐藏（超过3列），直接返回隐藏样式
  if (!layout) {
    // 未找到布局意味着被隐藏了
    return {
      display: 'none'
    }
  }
  
  // 如果这个课程正在被拖拽，原位置显示毛玻璃效果
  const isDraggedCourse = dragState.value.isDragging && dragState.value.draggedCourse?.ID === course.ID;

  // 如果这个课程正在被调整高度，原位置显示半透明效果，不隐藏
  const isResizedCourse = dragState.value.isResizing && dragState.value.draggedCourse?.ID === course.ID;

  // 检查是否应该显示悬浮毛玻璃效果（但不与拖拽效果冲突）
  const shouldShowHoverBlur = shouldShowBlurEffect(course);

  // 优先级：拖拽毛玻璃 > 调整高度半透明 > 悬浮毛玻璃 > 无效果
  let filterValue = 'none';
  let opacityValue = 1;
  let transformValue = 'scale(1)';

  if (isDraggedCourse) {
    // 拖拽时的毛玻璃效果
    filterValue = 'blur(2px)';
    opacityValue = 0.3;
    transformValue = 'scale(0.98)';
  } else if (isResizedCourse) {
    // 调整高度时的半透明效果，保持可见但不干扰预览
    opacityValue = 0.2;
    filterValue = 'blur(1px)';
  } else if (shouldShowHoverBlur) {
    // 悬浮毛玻璃效果
    filterValue = 'blur(2px)';
    opacityValue = 0.3;
    transformValue = 'scale(0.98)';
  }

  return {
    position: 'absolute',
    top: layout.top,
    left: layout.left,
    width: layout.width,
    height: layout.height,
    borderTop: `3px solid ${color}`,
    backgroundColor: rgbaToHexWithBg(...hexToRgb(color), 0.1, [255, 255, 255]),
    // borderRadius: '4px',
    boxSizing: 'border-box',
    zIndex: isDraggedCourse || isResizedCourse ? 1 : 2, // 被拖拽或调整的课程降低层级
    cursor: props.enableDrag ? 'pointer' : 'default',
    transition: 'all 0.2s ease',
    // 统一的视觉效果处理
    filter: filterValue,
    opacity: opacityValue,
    transform: transformValue,
    overflow: 'hidden' // 确保渐变效果不会溢出
  };
};

// 生成时间刻度数据 - 根据指定的时间范围生成刻度
const timeSlots = computed(() => {
  // 计算开始和结束的半小时索引
  const startIndex = Math.floor(startHour.value * 2); // 每小时2个半小时刻度
  const endIndex = Math.ceil(endHour.value * 2);      // 向上取整确保包含结束时间刻度

  // 计算需要生成的刻度数量，+1确保显示结束时间刻度
  const slotsCount = endIndex - startIndex + 1;

  return Array.from({ length: slotsCount }, (_, i) => {
    // 当前半小时索引
    const halfHourIndex = startIndex + i;

    // 计算小时和分钟
    const hour = Math.floor(halfHourIndex / 2);
    const minute = (halfHourIndex % 2) === 0 ? '00' : '30';

    // 生成时间标签
    const timeLabel = `${hour.toString().padStart(2, '0')}:${minute}`;

    return {
      value: hour + (minute === '30' ? 0.5 : 0),
      label: timeLabel,
      isLast: i === slotsCount - 1  // 标记最后一个格子
    };
  });
});

// 网格高度验证调试信息
// watch(timeSlots, (newTimeSlots) => {
  // console.log('=== 时间范围更新 ===', {
  //   时间段数量: newTimeSlots.length,
  //   每个网格单元格高度: `${defaultHeight.value / 2}px (使用box-sizing: border-box)`,
  //   每个时间行高度: `${defaultHeight.value / 2}px (time-row)`,
  //   整点标签高度: `${defaultHeight.value}px (time-label, 跨越2个time-row)`,
  //   半点标签高度: `${defaultHeight.value / 2}px (time-label-empty)`,
  //   总网格高度: `${newTimeSlots.length} × ${defaultHeight.value / 2}px = ${newTimeSlots.length * defaultHeight.value / 2}px`,
  //   课程定位基准: `defaultHeight.value = ${defaultHeight.value}px (每小时)`,
  //   时间范围: `${newTimeSlots[0]?.label} - ${newTimeSlots[newTimeSlots.length - 1]?.label}`,
  //   可见小时数: `${visibleHoursCount.value}小时`,
  //   时间范围设置: `${timeRangeStart.value} - ${timeRangeEnd.value}`
  // })
// }, { immediate: true })

// 更多课程抽屉
const moreParams = ref({})
const moreDrawerVisible = ref(false)
const moreDrawerDate = ref('')
const openMoreDrawer = (dateStr) => {
  // 重置参数
  moreParams.value = {
    timetableType: props.timetableType,
  }

  // 根据课表类型设置对应的对象ID参数
  if (props.timetableType !== CourseTimetableTypeEnum.TimeTimetable && selectedObject.value) {
    switch (props.timetableType) {
      case CourseTimetableTypeEnum.TeacherTimetable:
        moreParams.value.IDList = [selectedObject.value.ID]
        moreParams.value.TeacherType = -1
        break
      case CourseTimetableTypeEnum.StudentTimetable:
        moreParams.value.StudentID = selectedObject.value.ID
        break
      case CourseTimetableTypeEnum.ClassTimetable:
        moreParams.value.ClassID = selectedObject.value.ID
        break
      case CourseTimetableTypeEnum.ClassroomTimetable:
        moreParams.value.ClassroomIDList = [selectedObject.value.ID]
        break
    }
  }

  moreDrawerDate.value = dateStr
  moreDrawerVisible.value = true
}

// 课程详情弹窗事件处理
const handleRollCall = (courseData) => {
  // console.log('点名功能:', courseData)
  // 这里可以添加点名功能的具体实现
}

const handleAdjustStudents = (courseData) => {
  // console.log('调整学员功能:', courseData)
  // 这里可以添加调整学员功能的具体实现
}

const handleScheduleDetails = (courseData) => {
  // console.log('排课详情功能:', courseData)
  // 这里可以添加排课详情功能的具体实现
}

const handlePopoverShow = () => {
  // console.log('课程详情弹窗显示')
}

const handlePopoverHide = () => {
  // console.log('课程详情弹窗隐藏')
  isPopoverVisible.value = false
}



// 定时更新当前时间线
let timeUpdateInterval = null

onMounted(() => {
  console.log(window.microApp, 'window.microApp')

  // 清理可能存在的旧监听器
  // if (window.microApp && window.microApp.clearDataListener) {
  //   // window.microApp.clearDataListener();
  // }

  // // 注册新的监听器
  // if (window.microApp && window.microApp.addDataListener) {
  //   window.microApp.addDataListener((data) => {
  //     console.log('接收到数据:', data)
  //   });
  // }

  updateCurrentTimePosition()
  timeUpdateInterval = setInterval(updateCurrentTimePosition, 60000) // 每分钟更新一次

  // 添加全局点击监听，用于隐藏临时占位块
  // document.addEventListener('click', hideTemporaryBlock)

  // 设置滚动同步
  nextTick(() => {
    setupScrollSync()

    // 调试：检查课程块和拖拽手柄的渲染
    console.log('=== 组件挂载完成，检查渲染 ===')
    const courseItems = document.querySelectorAll('.course-item')
    // console.log('找到课程块数量:', courseItems.length)

    courseItems.forEach((item, index) => {
      const resizeHandles = item.querySelectorAll('.resize-handle')
      // console.log(`课程块 ${index}:`, {
      //   元素: item,
      //   拖拽手柄数量: resizeHandles.length,
      //   手柄: Array.from(resizeHandles).map(handle => ({
      //     类名: handle.ClassName,
      //     文本: handle.textContent,
      //     样式: handle.style.cssText
      //   }))
      // })
    })
  })
})

onUnmounted(() => {
  // 清理定时器
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }

  // 清理防抖定时器
  if (loadCourseDataTimer) {
    clearTimeout(loadCourseDataTimer)
    loadCourseDataTimer = null
  }

  // 清理全局点击监听器
  // document.removeEventListener('click', hideTemporaryBlock)

  // 清理数据监听器
  if (window.microApp && window.microApp.clearDataListener) {
    // window.microApp.clearDataListener();
    // console.log('已清理数据监听器');
  }
})

// 布局计算缓存
const layoutCache = new Map()

// 生成课程数据签名（用于检测数据是否变化）
const getCoursesSignature = (dayIndex) => {
  const coursesInDay = internalCourses.value.filter(c => c.day === dayIndex)
  return `${dayIndex}_${coursesInDay.length}_${coursesInDay.map(c => 
    `${c.ID}_${c.StartTime}_${c.EndTime}`
  ).join(',')}`
}

// 清除布局缓存
const clearLayoutCache = () => {
  layoutCache.clear()
  // console.log('布局缓存已清除')
}

// 获取缓存的布局（如果存在）
const getCachedDayLayout = (dayIndex) => {
  const signature = getCoursesSignature(dayIndex)
  const cacheKey = `day_${dayIndex}_${signature}`
  
  if (layoutCache.has(cacheKey)) {
    // console.log(`使用缓存的布局: day ${dayIndex}`)
    return layoutCache.get(cacheKey)
  }
  
  // console.log(`计算新布局: day ${dayIndex}`)
  const layout = calculateDayLayout(dayIndex)
  layoutCache.set(cacheKey, layout)
  
  // 限制缓存大小，保留最近 30 个
  if (layoutCache.size > 30) {
    const firstKey = layoutCache.keys().next().value
    layoutCache.delete(firstKey)
  }
  
  return layout
}

// 监听数据变化，自动清除缓存
watch(internalCourses, () => {
  clearLayoutCache()
}, { deep: true })

// 转置视图：计算单个对象在选中日期的课程布局
const calculateObjectDayLayout = (objectId, dayIndex) => {
  const dayData = coursesByDay.value[dayIndex]
  if (!dayData) {
    return new Map()
  }
  
  const fieldNames = getFieldNames(props.timetableType)
  
  // 获取该对象在该天的所有课程
  const objectCourses = [...dayData.courses, ...dayData.schedules].filter(course => {
    if (props.timetableType === CourseTimetableTypeEnum.TimeTimetable) {
      return true
    }
    return course[fieldNames.idField] === objectId
  })
  
  if (objectCourses.length === 0) {
    return new Map()
  }
  
  // 按开始时间排序
  const sortedCourses = [...objectCourses].sort((a, b) => {
    const aStart = parseTime(a.StartTime)
    const bStart = parseTime(b.StartTime)
    if (aStart !== bStart) return aStart - bStart
    return parseTime(a.EndTime) - parseTime(b.EndTime)
  })
  
  // 建立冲突关系
  const conflicts = new Map()
  
  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.StartTime)
    let courseEnd = parseTime(course.EndTime)
    
    if (courseEnd === 0) courseEnd = 24
    
    const conflictSet = new Set()
    
    sortedCourses.forEach(other => {
      if (other.ID === course.ID) return
      
      const otherStart = parseTime(other.StartTime)
      let otherEnd = parseTime(other.EndTime)
      if (otherEnd === 0) otherEnd = 24
      
      // 检查时间是否重叠
      if (courseStart < otherEnd && courseEnd > otherStart) {
        conflictSet.add(other.ID)
      }
    })
    
    conflicts.set(course.ID, conflictSet)
  })
  
  // 计算最大并发数，限制最多3列
  let maxColumns = 1
  const timePoints = new Set()
  
  sortedCourses.forEach(course => {
    const startMinutes = Math.round(parseTime(course.StartTime) * 60)
    const endMinutes = Math.round(parseTime(course.EndTime) * 60)
    timePoints.add(startMinutes)
    timePoints.add(endMinutes)
  })
  
  let maxConcurrentCourses = 0
  Array.from(timePoints).sort((a, b) => a - b).forEach(timePoint => {
    let concurrentCourses = 0
    
    sortedCourses.forEach(course => {
      const startMinutes = Math.round(parseTime(course.StartTime) * 60)
      const endMinutes = Math.round(parseTime(course.EndTime) * 60)
      
      if (startMinutes <= timePoint && timePoint < endMinutes) {
        concurrentCourses++
      }
    })
    
    maxConcurrentCourses = Math.max(maxConcurrentCourses, concurrentCourses)
    maxColumns = Math.min(Math.max(maxColumns, concurrentCourses), 3)
  })
  
  // 记录是否有隐藏课程（超过3列）
  const hasHiddenCourses = maxConcurrentCourses > 3
  
  // 计算每个课程的列位置（限制在maxColumns内）
  const columnOccupancy = Array.from({ length: maxColumns }, () => [])
  const positions = new Map()
  const hiddenCourses = [] // 记录被隐藏的课程
  
  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.StartTime)
    let courseEnd = parseTime(course.EndTime)
    if (courseEnd === 0) courseEnd = 24
    
    // 找到第一个可用的列
    let columnIndex = -1
    for (let col = 0; col < maxColumns; col++) {
      const occupied = columnOccupancy[col]
      let canPlace = true
      
      // 检查该列在这个时间段是否被占用
      for (const segment of occupied) {
        if (courseStart < segment.end && courseEnd > segment.start) {
          canPlace = false
          break
        }
      }
      
      if (canPlace) {
        columnIndex = col
        break
      }
    }
    
    // 如果所有列都被占用（超过3列），标记为隐藏
    if (columnIndex === -1) {
      hiddenCourses.push(course.ID)
      positions.set(course.ID, {
        column: -1,
        maxColumns: maxColumns,
        hidden: true
      })
    } else {
      // 记录该列的占用时间段
      columnOccupancy[columnIndex].push({
        start: courseStart,
        end: courseEnd
      })
      
      positions.set(course.ID, {
        column: columnIndex,
        maxColumns: maxColumns,
        hidden: false
      })
    }
  })
  
  // 生成最终布局
  const layouts = new Map()
  const gap = 4 // 列之间的间距
  
  sortedCourses.forEach(course => {
    const pos = positions.get(course.ID)
    
    // 隐藏的课程不生成布局
    if (pos.hidden) {
      return
    }
    
    const courseStart = parseTime(course.StartTime)
    let courseEnd = parseTime(course.EndTime)
    if (courseEnd === 0) courseEnd = 24
    
    const top = `${(courseStart - startHour.value) * defaultHeight.value}px`
    const height = `${(courseEnd - courseStart) * defaultHeight.value}px`
    
    // 计算宽度和left，考虑间距
    const totalGap = gap * 2 // 左右边距
    const middleGap = gap * (maxColumns - 1) // 中间间距总数
    const columnWidth = `calc((100% - ${totalGap + middleGap}px) / ${maxColumns})`
    const left = `calc(${gap}px + (${columnWidth} + ${gap}px) * ${pos.column})`
    
    layouts.set(course.ID, {
      top,
      left,
      width: columnWidth,
      height,
      hidden: false
    })
  })
  
  return {
    layouts,
    hasHiddenCourses
  }
}

// 转置视图：布局缓存
const transLayoutCache = new Map()

const getCachedObjectDayLayout = (objectId, dayIndex) => {
  const cacheKey = `obj_${objectId}_day_${dayIndex}`
  
  if (transLayoutCache.has(cacheKey)) {
    return transLayoutCache.get(cacheKey)
  }
  
  const result = calculateObjectDayLayout(objectId, dayIndex)
  transLayoutCache.set(cacheKey, result)
  
  // 更新该对象是否有隐藏课程的状态
  objectsWithHiddenCourses.value.set(objectId, result.hasHiddenCourses)
  
  // 限制缓存大小
  if (transLayoutCache.size > 100) {
    const firstKey = transLayoutCache.keys().next().value
    transLayoutCache.delete(firstKey)
  }
  
  return result
}

// 监听选中日期和课程数据变化，清除转置布局缓存
watch([selectedDayIndex, internalCourses], () => {
  transLayoutCache.clear()
  objectsWithHiddenCourses.value.clear()
}, { deep: true })

// 计算某一天所有课程的布局 - 重新设计的冲突处理算法
const calculateDayLayout = (dayIndex) => {
  // 获取当天所有课程并按开始时间排序
  const allCoursesInDay = internalCourses.value.filter(c => c.day === dayIndex)

  if (allCoursesInDay.length === 0) {
    // 重置该天的隐藏课程状态
    daysWithHiddenCourses.value[dayIndex] = false
    return new Map()
  }

  const sortedCourses = [...allCoursesInDay].sort((a, b) => {
    const aStart = parseTime(a.StartTime)
    const bStart = parseTime(b.StartTime)
    if (aStart !== bStart) return aStart - bStart
    return parseTime(a.EndTime) - parseTime(b.EndTime)
  })

  if (dayIndex === 0) {
    // console.log('\n=== 新的课程冲突处理算法 ===')
    // console.log('处理课程顺序:', sortedCourses.map(c => `${c.ShiftName}(${c.StartTime}-${c.EndTime})`))
  }

  // 第一步：建立所有课程的时间冲突关系
  const conflicts = new Map() // 存储每门课程与其他课程的冲突关系

  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.StartTime)
    let courseEnd = parseTime(course.EndTime)

    // 特殊处理跨天情况：当结束时间是00:00时，表示第二天的00:00，应该当作24:00计算
    if (course.EndTime === '00:00' && courseStart > courseEnd) {
      courseEnd = 24
    }

    const conflictingCourses = new Set()

    // 找出与当前课程时间有重叠的所有课程
    sortedCourses.forEach(otherCourse => {
      if (course.ID === otherCourse.ID) return

      const otherStart = parseTime(otherCourse.StartTime)
      let otherEnd = parseTime(otherCourse.EndTime)

      // 特殊处理跨天情况：当结束时间是00:00时，表示第二天的00:00，应该当作24:00计算
      if (otherCourse.EndTime === '00:00' && otherStart > otherEnd) {
        otherEnd = 24
      }

      // 检查时间是否重叠：两个课程有重叠时间
      if (courseStart < otherEnd && courseEnd > otherStart) {
        conflictingCourses.add(otherCourse.ID)
      }
    })

    conflicts.set(course.ID, conflictingCourses)
  })

  if (dayIndex === 0) {
    console.log('冲突关系分析:')
    conflicts.forEach((conflictSet, courseId) => {
      const course = sortedCourses.find(c => c.ID === courseId)
      const conflictNames = Array.from(conflictSet).map(ID =>
        sortedCourses.find(c => c.ID === ID)?.ShiftName
      )
      console.log(`${course.ShiftName}: 与 [${conflictNames.join(', ')}] 冲突`)
    })
  }

  // 第二步：计算全天最大同时冲突数（决定列数）最大列
  let maxColumns = 1

  // 为了准确计算，我们需要找出任意时间点的最大并发课程数
  const timePoints = new Set()

  sortedCourses.forEach(course => {
    const startMinutes = Math.round(parseTime(course.StartTime) * 60)
    const endMinutes = Math.round(parseTime(course.EndTime) * 60)
    timePoints.add(startMinutes)
    timePoints.add(endMinutes)
  })

  let maxConcurrentCourses = 0
  Array.from(timePoints).sort((a, b) => a - b).forEach(timePoint => {
    let concurrentCourses = 0

    sortedCourses.forEach(course => {
      const startMinutes = Math.round(parseTime(course.StartTime) * 60)
      const endMinutes = Math.round(parseTime(course.EndTime) * 60)

      if (startMinutes <= timePoint && timePoint < endMinutes) {
        concurrentCourses++
      }
    })

    maxConcurrentCourses = Math.max(maxConcurrentCourses, concurrentCourses)
    maxColumns = Math.min(Math.max(maxColumns, concurrentCourses), 3)
  })

  // 更新该天是否有隐藏课程的状态
  daysWithHiddenCourses.value[dayIndex] = maxConcurrentCourses > 3

  if (dayIndex === 0) {
    // console.log(`全天最大并发课程数: ${maxConcurrentCourses}, 显示列数: ${maxColumns}, 是否有隐藏课程: ${daysWithHiddenCourses.value[dayIndex]}`)
  }

  // 第三步：使用贪心算法为每门课程分配列位置
  const coursePositions = new Map()
  const columnOccupancy = Array.from({ length: maxColumns }, () => []) // 每列的占用时间段

  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.StartTime)
    let courseEnd = parseTime(course.EndTime)

    // 特殊处理跨天情况：当结束时间是00:00时，表示第二天的00:00，应该当作24:00计算
    if (course.EndTime === '00:00' && courseStart > courseEnd) {
      courseEnd = 24
    }

    // 找到第一个可用的列
    let assignedColumn = -1

    for (let col = 0; col < maxColumns; col++) {
      let canUseColumn = true

      // 检查这一列是否在当前课程时间段内有冲突
      for (const occupiedPeriod of columnOccupancy[col]) {
        const occupiedStart = occupiedPeriod.start
        const occupiedEnd = occupiedPeriod.end

        // 检查时间是否重叠
        if (courseStart < occupiedEnd && courseEnd > occupiedStart) {
          canUseColumn = false
          break
        }
      }

      if (canUseColumn) {
        assignedColumn = col
        // 将当前课程的时间段标记为占用
        columnOccupancy[col].push({
          start: courseStart,
          end: courseEnd,
          courseId: course.ID
        })
        break
      }
    }

    coursePositions.set(course.ID, assignedColumn)

    if (dayIndex === 0) {
      // console.log(`${course.ShiftName} (${course.StartTime}-${course.EndTime}) 分配到列 ${assignedColumn}`)
    }
  })

  // 第四步：计算最终布局样式
  const layouts = new Map()
  const gap = 6 // 间距大小6px
  const leftGap = gap // 左边间距6px
  const rightGap = gap // 右边间距6px
  const middleGap = gap * (maxColumns - 1) // 中间间距总数
  const totalGap = leftGap + rightGap + middleGap // 总间距 = 左间距 + 右间距 + 中间间距
  const columnWidth = `calc((100% - ${totalGap}px) / ${maxColumns})` // 每列宽度（扣除总间距）

  sortedCourses.forEach(course => {
    const courseStart = parseTime(course.StartTime)
    let courseEnd = parseTime(course.EndTime)

    // 特殊处理跨天情况：当结束时间是00:00时，表示第二天的00:00，应该当作24:00计算
    if (course.EndTime === '00:00' && courseStart > courseEnd) {
      courseEnd = 24
    }

    const courseDuration = courseEnd - courseStart
    const column = coursePositions.get(course.ID)

    if (column !== -1) {
      layouts.set(course.ID, {
        left: `calc(${gap}px + ${column} * (${columnWidth} + ${gap}px ) - 1px)`, // 左边间距 + 前面列的宽度+间距
        width: columnWidth, // 扣掉间距后的列宽
        top: `${(courseStart - startHour.value) * defaultHeight.value}px`, // 调整top位置，减去时间范围开始偏移
        height: `${(courseDuration * defaultHeight.value) - 6}px`
      })
    }

    if (dayIndex === 0) {
      // console.log(`${course.ShiftName} 布局计算:`, {
      //   时间: `${course.StartTime} - ${course.EndTime}`,
      //   courseStart: courseStart,
      //   courseDuration: courseDuration,
      //   top位置: `${(courseStart - startHour.value) * defaultHeight.value}px`,
      //   height: `${(courseDuration * defaultHeight.value) - 6}px`,
      //   网格对齐验证: {
      //     每小时高度: `${defaultHeight.value}px`,
      //     每30分钟高度: `${defaultHeight.value / 2}px`,
      //     网格单元格高度: `30px (使用box-sizing: border-box)`,
      //     理论top位置: `课程开始时间${courseStart}小时 × ${defaultHeight.value}px = ${courseStart * defaultHeight.value}px`,
      //     完美匹配: `每30分钟30px = ${defaultHeight.value / 2}px ✓`
      //   }
      // })
    }
  })

  if (dayIndex === 0) {
    // console.log('\n=== 列占用情况 ===')
    columnOccupancy.forEach((periods, colIndex) => {
      // console.log(`列${colIndex}:`, periods.map(p => {
      //   const courseName = sortedCourses.find(c => c.ID === p.courseId)?.ShiftName
      //   return `${courseName}(${formatTime(p.start)}-${formatTime(p.end)})`
      // }).join(', '))
    })
    // console.log('\n=== 课程布局计算完成 ===\n')
  }

  return layouts
}

// 使用computed缓存布局计算结果
const mondayLayouts = computed(() => calculateDayLayout(0))

// 获取拖拽预览块的样式
const getDragPreviewStyle = (color) => {
  if (!dragState.value.isDragging && !dragState.value.isResizing) {
    return { display: 'none' };
  }

  if (!dragState.value.draggedCourse) {
    return { display: 'none' };
  }

  const course = dragState.value.draggedCourse;

  // 根据操作类型确定预览块的尺寸
  let previewWidth = dragState.value.originalWidth; // 始终使用拖拽开始时的宽度
  let previewHeight;

  if (dragState.value.isResizing) {
    // 调整高度时：重新计算高度
    const courseStart = parseTime(course.StartTime);
    const courseEnd = parseTime(course.EndTime);
    const courseDuration = courseEnd - courseStart;
    previewHeight = courseDuration * defaultHeight.value;
  } else {
    // 拖拽移动时：使用拖拽开始时的高度
    previewHeight = dragState.value.originalHeight;
  }

  // 获取容器边界信息用于右边界保护
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  if (!scheduleGrid) return { display: 'none' };
  const gridRect = scheduleGrid.getBoundingClientRect();
  const containerWidth = gridRect.width;

  // 根据操作类型确定预览块位置
  let currentLeft, currentTop;

  if (dragState.value.isDragging) {
    // 拖拽移动：使用当前拖拽位置
    currentLeft = dragState.value.currentPosition.x;
    currentTop = dragState.value.currentPosition.y;
  } else if (dragState.value.isResizing) {
    // 调整高度：直接使用记录的精确位置（已经是相对于 schedule-grid 的绝对位置）
    currentLeft = dragState.value.startPosition.courseX; // 直接使用记录的绝对left，不需要再加dayLeft

    const courseStart = parseTime(course.StartTime);
    currentTop = (courseStart - startHour.value) * defaultHeight.value;  // 减去时间范围偏移
  }

  // 添加右边界保护：确保预览块不会超出容器右边界
  const maxLeft = containerWidth - previewWidth;
  currentLeft = Math.min(currentLeft, maxLeft);

  // 同时确保不会超出左边界
  currentLeft = Math.max(0, currentLeft);
  let relColor = color || getStatusColor(course)

  // 拖拽预览块完全模拟原课程块的样式
  return {
    position: 'absolute',
    top: `${currentTop}px`,
    left: `${currentLeft}px`,
    width: `${previewWidth}px`,
    height: `${previewHeight}px`,
    borderTop: `3px solid ${relColor}`,
    backgroundColor: rgbaToHexWithBg(...hexToRgb(relColor), 0.1, [255, 255, 255]),
    boxSizing: 'border-box',
    zIndex: 1000,
    cursor: dragState.value.isDragging ? 'grabbing' : 'ns-resize',
    transition: 'none',
    pointerEvents: 'none',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
    opacity: 0.9,
    transform: 'none'
  };
};

// 新增：获取拖拽预览块时间指示器的样式
const getDragPreviewTimeStyle = () => {
  if (!dragState.value.isDragging && !dragState.value.isResizing) {
    return { display: 'none' };
  }

  if (!dragState.value.draggedCourse) {
    return { display: 'none' };
  }

  // 计算时间指示器位置：预览块的上方，居中对齐
  let currentLeft, currentTop, currentWidth;

  if (dragState.value.isDragging) {
    // 拖拽移动：使用当前拖拽位置
    currentLeft = dragState.value.currentPosition.x;
    currentTop = dragState.value.currentPosition.y - 25; // 在预览块上方25px
    currentWidth = dragState.value.originalWidth; // 使用课程块的宽度
  } else if (dragState.value.isResizing) {
    // 调整高度：使用原始位置
    const dayWidth = getDayColumnWidth();
    
    // 🔥 关键修改：基于 filteredWeekData 计算位置
    // 找到原始天在过滤后列表中的索引
    const filteredDayIndex = filteredWeekData.value.findIndex(d => d.originalIndex === dragState.value.draggedCourse.day)
    const displayDayIndex = filteredDayIndex !== -1 ? filteredDayIndex : 0
    
    currentLeft = displayDayIndex * dayWidth + dragState.value.startPosition.courseX;
    const courseStart = parseTime(dragState.value.draggedCourse.StartTime);
    currentTop = (courseStart - startHour.value) * defaultHeight.value - 25; // 在预览块上方25px
    currentWidth = dragState.value.originalWidth; // 使用课程块的宽度
  }

  // 获取容器边界信息用于右边界保护
  const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid');
  let containerWidth = 7 * minWidth; // 默认宽度（7天 * 150px）

  if (scheduleGrid) {
    const rect = scheduleGrid.getBoundingClientRect();
    containerWidth = rect.width;
  }

  // 边界保护：确保时间指示器不会超出容器边界
  const maxLeft = containerWidth - currentWidth;
  currentLeft = Math.min(currentLeft, maxLeft);
  currentLeft = Math.max(0, currentLeft);

  return {
    position: 'absolute',
    top: `${Math.max(0, currentTop)}px`,
    left: `${currentLeft}px`,
    width: `${currentWidth}px`,
    height: '20px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    pointerEvents: 'none',
    zIndex: 1001
  };
};

// === 对外暴露的方法 ===
defineExpose({
  refresh: (forceRefresh = true) => loadCourseData(forceRefresh),
  // 手动触发全屏切换
  toggleFullscreen,
  // 获取当前课程数据
  getCourses: () => internalCourses.value,
  // 设置课程数据（可选的手动更新方法）
  setCourses: (newCourses) => {
    internalCourses.value = [...newCourses]
  },
  // 重新计算布局
  recalculateLayout: recalculateAllLayouts,
  // 隐藏临时占位块
  hideTemporaryBlock,
  // 确认修改课程位置
  confirmCourseUpdate: (confirmInfo) => {
    // console.log('确认修改课程位置:', confirmInfo)

    const { course, newPosition } = confirmInfo

    // 执行实际的课程位置更新
    updateCoursePosition(
      course,
      newPosition.day,
      newPosition.StartTime,
      newPosition.EndTime
    )

    return true
  },
  // 取消课程修改（恢复原位置）
  cancelCourseUpdate: (confirmInfo) => {
    // console.log('取消课程修改，恢复原位置:', confirmInfo)

    // 触发重新布局计算，确保课程回到原位置
    nextTick(() => {
      recalculateAllLayouts()
    })

    return true
  },
  // 对比排课联动：设置/清空临时占位块
  setTemporaryBlock: (block) => {
    temporaryBlock.value = {
      ...block,
      isPassive: true // 被动同步，标记为passive
    }
  },
  clearTemporaryBlock: () => {
    temporaryBlock.value = null
  }
})

// 新增：开始调整高度
const handleResizeStart = (event, course, direction) => {
  // 检查是否启用拖拽功能
  if (!props.enableDrag) {
    return
  }

  // 只有按下鼠标左键才开始调整
  if (event.button !== 0) {
    return
  }

  // 阻止默认行为和事件冒泡
  event.preventDefault()
  event.stopPropagation()

  // 设置调整状态
  dragState.value.isResizing = true
  dragState.value.resizeDirection = direction
  dragState.value.draggedCourse = { ...course } // 创建副本
  dragState.value.originalCourse = { ...course }

  // 获取课程元素并记录其精确尺寸和位置
  const courseElement = event.currentTarget.parentElement // 获取父元素，即.course-item
  if (courseElement) {
    const rect = courseElement.getBoundingClientRect()
    
    // 🔥 修复：记录相对于 schedule-grid 的绝对位置，而不是相对父元素的位置
    const scheduleGrid = scheduleContainerRef.value?.querySelector('.schedule-grid')
    if (scheduleGrid) {
      const gridRect = scheduleGrid.getBoundingClientRect()
      dragState.value.originalWidth = rect.width
      dragState.value.originalHeight = rect.height
      dragState.value.startPosition.courseX = rect.left - gridRect.left // 相对于 grid 的绝对 left
    } else {
      // 降级方案
      const parentRect = courseElement.parentElement.getBoundingClientRect()
      dragState.value.originalWidth = rect.width
      dragState.value.originalHeight = rect.height
      dragState.value.startPosition.courseX = rect.left - parentRect.left
    }
  }

  // 记录原始数据
  dragState.value.originalStartTime = course.StartTime
  dragState.value.originalEndTime = course.EndTime

  // 记录鼠标的初始位置
  dragState.value.startPosition.mouseX = event.clientX
  dragState.value.startPosition.mouseY = event.clientY

  // 添加全局事件监听
  document.addEventListener('mousemove', handleResizeMove)
  document.addEventListener('mouseup', handleResizeEnd)

  // 设置文档光标样式
  document.body.style.cursor = 'ns-resize'
}

// 新增：调整高度过程中
const handleResizeMove = (event) => {
  if (!dragState.value.isResizing) return

  event.preventDefault()

  // 使用requestAnimationFrame节流
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
  }

  dragState.value.animationFrameId = requestAnimationFrame(() => {
    const course = dragState.value.draggedCourse
    const direction = dragState.value.resizeDirection

    // 计算鼠标移动的距离
    const deltaY = event.clientY - dragState.value.startPosition.mouseY

    // 将像素距离转换为时间变化（每小时60px），添加阻尼效果使拖拽更稳定
    const timeDelta = (deltaY / defaultHeight.value) * 0.8 // 添加0.8的阻尼系数

    // 根据调整方向计算新的开始和结束时间
    let newStartTime, newEndTime

    if (direction === 'top') {
      // 调整开始时间（向上拖拽开始时间提前，向下拖拽开始时间延后）
      const originalStartHour = parseTime(dragState.value.originalStartTime)
      const originalEndHour = parseTime(dragState.value.originalEndTime)
      const newStartHour = originalStartHour + timeDelta

      // 限制最小持续时间和时间范围
      const minDuration = dragState.value.minDuration
      const maxStartHour = originalEndHour - minDuration

      if (newStartHour >= 0 && newStartHour <= maxStartHour) {
        newStartTime = hourToTimeString(newStartHour)
        newEndTime = dragState.value.originalEndTime // 保持原始结束时间
      } else {
        // 如果超出范围，保持原始开始时间
        newStartTime = dragState.value.originalStartTime
        newEndTime = dragState.value.originalEndTime
      }
    } else {
      // 调整结束时间（向上拖拽结束时间提前，向下拖拽结束时间延后）
      const originalStartHour = parseTime(dragState.value.originalStartTime)
      const originalEndHour = parseTime(dragState.value.originalEndTime)
      const newEndHour = originalEndHour + timeDelta

      // 限制最小和最大持续时间
      const minDuration = dragState.value.minDuration
      const maxDuration = dragState.value.maxDuration
      const minEndHour = originalStartHour + minDuration
      const maxEndHour = Math.min(24, originalStartHour + maxDuration)

      if (newEndHour >= minEndHour && newEndHour <= maxEndHour) {
        newStartTime = dragState.value.originalStartTime // 保持原始开始时间
        newEndTime = hourToTimeString(newEndHour)
      } else {
        // 如果超出范围，保持原始结束时间
        newStartTime = dragState.value.originalStartTime
        newEndTime = dragState.value.originalEndTime
      }
    }

    // 更新拖拽状态中的课程数据（用于预览）
    dragState.value.draggedCourse = {
      ...course,
      StartTime: newStartTime,
      EndTime: newEndTime,
      Date: dayjs(weekDates.value[course.day]).format('YYYY-MM-DD') // 转换为YYYY-MM-DD格式的字符串
    }

    // 减少控制台输出频率
    // if (import.meta.env.DEV && Math.random() < 0.05) {
      // console.log('调整高度实时预览:', {
      //   课程: course.ShiftName,
      //   方向: direction,
      //   原始时间: `${dragState.value.originalStartTime} - ${dragState.value.originalEndTime}`,
      //   新时间: `${newStartTime} - ${newEndTime}`,
      //   持续时间变化: `${parseTime(newEndTime) - parseTime(newStartTime)}小时`,
      //   鼠标移动: `${Math.round(deltaY)}px`,
      //   时间变化: `${timeDelta.toFixed(2)}小时`
      // })
    // }
  })
}

// 新增：结束调整高度
const handleResizeEnd = (event) => {
  if (!dragState.value.isResizing) return

  event.preventDefault()

  // 清理动画帧
  if (dragState.value.animationFrameId) {
    cancelAnimationFrame(dragState.value.animationFrameId)
    dragState.value.animationFrameId = null
  }

  // 移除全局事件监听
  document.removeEventListener('mousemove', handleResizeMove)
  document.removeEventListener('mouseup', handleResizeEnd)

  // 恢复文档光标样式
  document.body.style.cursor = ''

  const course = dragState.value.draggedCourse
  const originalCourse = dragState.value.originalCourse

  // 检查是否发生了实际变化
  const hasTimeChanged = course.StartTime !== originalCourse.StartTime || course.EndTime !== originalCourse.EndTime

  if (hasTimeChanged) {
    // 构建确认信息
    const confirmInfo = {
      course: { ...course },
      originalPosition: {
        day: originalCourse.day,
        dayName: props.weekDays[originalCourse.day],
        StartTime: originalCourse.StartTime,
        EndTime: originalCourse.EndTime,
        Date: originalCourse.Date
      },
      newPosition: {
        day: course.day,
        dayName: props.weekDays[course.day],
        StartTime: course.StartTime,
        EndTime: course.EndTime,
        Date: dayjs(weekDates.value[course.day]).format('YYYY-MM-DD') // 转换为YYYY-MM-DD格式的字符串
      },
      changeDetails: {
        dayChanged: false, // 高度调整不改变天数
        timeChanged: true,
        dayChange: null,
        timeChange: `${originalCourse.StartTime}-${originalCourse.EndTime} → ${course.StartTime}-${course.EndTime}`,
        resizeDirection: dragState.value.resizeDirection,
        durationChange: `${parseTime(originalCourse.EndTime) - parseTime(originalCourse.StartTime)}小时 → ${parseTime(course.EndTime) - parseTime(course.StartTime)}小时`
      }
    }

    // console.log('课程时间发生改变，等待用户确认:', confirmInfo)

    // 使用弹框组件显示确认对话框
    showDragConfirmDialog(confirmInfo)
  } else {
    // console.log('课程时间未发生改变，无需确认')
  }

  // 重置调整状态
  dragState.value.isResizing = false
  dragState.value.resizeDirection = ''
  dragState.value.draggedCourse = null
  dragState.value.originalCourse = null

  // 清除悬浮状态
  hoveredCourse.value = null
  if (hoverTimer.value) {
    clearTimeout(hoverTimer.value)
    hoverTimer.value = null
  }

  // 触发布局重新计算
  nextTick(() => {
    recalculateAllLayouts()
  })
}
</script>
<style lang="scss" scoped>
.canlendar-container {
  // border: 1px solid #e8e8e8;
  border-radius: 8px;

  .object-column-container {
    .object-column-content {
      height: calc(100% - 50px);
      border-bottom-left-radius: 8px;
      overflow: auto;
    }
  }
}

.schedule-container {
  width: 100%;
  height: 100%;
  // min-height: 600px;

  background-color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: auto;

  &.is-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    margin: 0;
    border-radius: 0;
    max-height: 100%;
    z-index: 2000;
    border: none;
    box-shadow: none;
  }

  &.is-compare {
    // 对比模式：不创建滚动上下文，让 sticky 向上查找外部滚动容器
    overflow: visible;
    height: auto;
    box-shadow: none;
  }

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

.header {
  display: flex;
  height: 43px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #f5f7fa;
  font-weight: 500;
  width: max-content;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  min-width: 100%;
  overflow-x: visible;
}


.time-column {
  width: 70px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background-color: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  color: #666;
  flex-direction: column;

  &.object-column {
    width: 120px;
  }

  .wtwo-button {
    font-size: 10px;
    padding: 4px 8px;
  }
}

.is-compare {
  --day-column-min-width: 60px;
  
  .time-column,
  .time-labels {
    width: 45px;
  }
}

.day-column {
  width: 180px;
  min-width: 180px;
  max-width: 180px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #333;
  border-right: 1px solid #e8e8e8;
  position: relative;
  padding: 8px 6px;
  height: 43px;
  box-sizing: border-box;

  &-content {
    display: flex;
    align-items: center;
    gap: 4px;
    width: 100%;
    position: relative;
    overflow: hidden;
  }
  
  .more-courses-mark {
    font-weight: 500;
    font-size: 11px;
    color: #2878E8;
    cursor: pointer;
    white-space: nowrap;
    flex-shrink: 0;
    padding: 0 2px;
    
  }

  &-name-row {
    display: flex;
    align-items: center;
    gap: 4px;
    width: 100%;
    overflow: hidden;
  }

  &-highlight {
    background-color: #e6f7ff;
    /* 高亮背景色 */
    border: 1px solid #91d5ff;
    /* 高亮边框 */
    box-shadow: 0 0 8px rgba(0, 136, 255, 0.3);
    /* 高亮阴影 */
    transform: scale(1.02);
    /* 轻微放大效果 */
    color: #2878E8;
    /* 高亮文字颜色 */
    font-weight: 700;
    /* 加粗字体 */
  }

  &-today {
    background-color: #e6f7ff;
    border: 1px solid #91d5ff;
    box-shadow: 0 0 8px rgba(24, 144, 255, 0.2);

    .day-name {
      color: #2878E8;
      font-weight: 700;
    }
  }
}

@media screen and (min-width: 1280px) {
  .day-column {
    justify-content: center;
  }

  .is-compare {
    .day-column {
      justify-content: left;
    }
  }
}

.day-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: normal;

  &.date-today {
    color: #2878E8 !important;
    font-weight: 600 !important;
    background: linear-gradient(45deg, #2878E8, #40a9ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.day-date {
  font-weight: 400;
  font-size: 11px;
  color: #606266;
  flex-shrink: 0;
  white-space: nowrap;

  &-today {
    width: 20px;
    height: 20px;
    background: #2878E8;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 12px;
    color: #FFFFFF;
    margin-left: 12px;
  }
}

// 移除按钮样式
.more-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .default-icon {
    display: block;
  }

  .active-icon {
    display: none;
  }

  &:hover {
    .default-icon {
      display: none;
    }

    .active-icon {
      display: block;
    }
  }
}

// 学员信息按钮样式
.student-more-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  background-color: #F0F0F0;
  border-radius: 50%;

  .default-icon {
    display: block;
  }

  .active-icon {
    display: none;
  }

  &:hover {
    background-color: var(--wtwo-color-primary);

    .default-icon {
      display: none;
    }

    .active-icon {
      display: block;
    }
  }
}

.holiday-mark {
  width: 20px;
  height: 20px;
  background: #FFECE8;
  border-radius: 4px;
  font-weight: 400;
  font-size: 12px;
  color: #F53F3F;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 12px;
  cursor: default;
  user-select: none;
}

.drag-target-indicator {
  position: absolute;
  bottom: 2px;
  /* 调整位置到底部 */
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(45deg, #2878E8, #40a9ff);
  /* 渐变背景 */
  color: white;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 9px;
  font-weight: 600;
  z-index: 10;
  opacity: 0.9;
  pointer-events: none;
  /* 确保指示器不会影响拖拽 */
  animation: targetIndicatorPulse 1.5s ease-in-out infinite;
  /* 添加脉冲动画 */
}

.time-labels {
  width: 70px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background-color: #fff;
  position: sticky;
  left: 0;
  z-index: 10;
}

.time-row {
  display: flex;
  align-items: flex-start;
  position: relative;
  border-top: 1px solid #fff;
}

.time-label {
  width: 100%;
  /* 跨越两个30px的time-row，匹配一小时的高度 */
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 4px 8px 0 4px;
  font-size: 11px;
  color: #666;
  font-weight: 500;
  background-color: #fff;
  position: relative;
  line-height: 1;

  &-empty {
    width: 100%;
    height: 30px;
    /* 匹配网格单元格高度：30px */
    background-color: #fff;
    position: relative;
  }
}

.course-item {
  cursor: pointer;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.2;
  margin: 1px;
  user-select: none;
  transition: all 0.2s ease;
  /* overflow: visible; */
  overflow: hidden;
  /* 改为visible，让拖拽手柄可以显示在外部 */
  display: flex;
  flex-direction: column;
  border: none;
  background: #2878E8;
  box-shadow: none;
  position: relative;
  /* 确保相对定位 */
  min-height: 20px;
  /* 确保最小高度 */
  /* 性能优化：启用硬件加速 */
  will-change: transform, top, left, height;
  transform: translateZ(0);
  backface-visibility: hidden;

  .resize-handle {
    cursor: ns-resize !important;

    &:hover {
      cursor: ns-resize !important;
    }
  }

  .course-header,
  .course-content {
    cursor: pointer;

    div {
      flex-shrink: 0;
    }
  }

  .course-content {
    padding: 8px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    // border-radius: 8px 8px 4px 4px;
    min-height: 0;
    overflow: hidden;
    height: calc(100% - 4px);
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding-top: 2px;

    .course-header {
      font-weight: 600;
      font-size: 14px;
      color: #303133;
      border-radius: 3px 3px 0 0;
      white-space: nowrap;
      overflow: hidden;
      // text-overflow: ellipsis;
      flex-shrink: 0;
      line-height: 14px;
      margin-bottom: 2px;

      .fixed-type-label {
        position: absolute;
        right: 0;
        top: 0px;
        background: #137DFF;
        color: #fff;
        width: 40px;
        padding: 2px 8px;
        font-size: 12px;
        line-height: 17px;
        border-radius: 4px 0px 0px 4px;
      }

    }

    .course-field {
      font-weight: 400;
      font-size: 12px;
      color: #606266;
      margin-bottom: 2px;
      white-space: nowrap;
      overflow: hidden;
      // text-overflow: ellipsis;
      line-height: normal;
    }

    .course-tag-fields {
      margin-top: 6px;
      display: flex;
      gap: 5px;
      flex-wrap: wrap;

      div {
        box-sizing: border-box;
        height: 20px;
        background-color: #FFFFFF;
        border-radius: 4px;
        font-weight: 400;
        font-size: 10px;
        padding: 0 6px;
        display: flex;
        align-items: center;

      }
    }
  }


  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
    z-index: 100 !important;

    .resize-handle {
      opacity: 1;
    }
  }

  &.resizing {
    .resize-handle {
      opacity: 1;
    }
  }

  /* 小课程块的特殊处理 */
  // &[style*="height: 60px"],
  // &[style*="height: 30px"] {}
}


.time-schedule-content {
  position: relative;
  width: max-content;
  min-width: 100%;
  display: flex;
}

.object-column-wrapper {
  width: 120px;
  flex-shrink: 0;
  // border-right: 1px solid #e8e8e8;
  background-color: #fff;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

.time-list {
  display: flex;
  flex-direction: row;
  position: relative;
  width: max-content;
  min-width: 100%;
  overflow: visible;
  /* 确保flex子元素可以收缩 */
  /* 允许时间列表滚动，时间标签和课程网格一起滚动 */
  // 确保最底部的时刻能看到
  /* 确保可以收缩 */
  
  /* 自定义滚动条样式 */
  &::-webkit-scrollbar {
    height: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

.schedule-grid-wrapper {
  display: flex;
  position: relative;
  flex-shrink: 0;
  width: max-content;
  min-width: calc(100% - 70px);
}

.schedule-grid {
  cursor: pointer;
  display: flex;
  flex-shrink: 0;
  position: relative;
}

.day-column-container {
  position: relative;
  border-right: 1px solid #e8e8e8;
  min-height: 100%;
  width: 180px;
  min-width: 180px;
  max-width: 180px;
  flex-shrink: 0;
  overflow: hidden;
  box-sizing: border-box;
}

.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  cursor: pointer;
  /* 鼠标变成手指状态 */
}

.courses-container {
  position: relative;
  z-index: 2;
  height: 100%;
  width: 100%;
}

.grid-cell {
  border-bottom: 1px solid #e1e1e1;
  /* 加深整点网格线颜色 */
  position: relative;
  background-color: #fff;
  box-sizing: border-box;
  /* 确保边框不影响总高度 */

  &.half-hour {
    border-bottom: 1px dashed #d1d1d1;
    /* 加深半点虚线颜色，提高对比度 */
    background-color: #fff;
    /* 略微加深半点背景色 */
  }

  &.past-date {
    background-color: #F9FAFC;
  }
}

.current-time-line {
  position: absolute;
  height: 2px;
  background-color: #ff4d4f;
  z-index: 200;
  display: flex;
  align-items: center;
  border-radius: 1px;
  pointer-events: none;
}

.current-time-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ff4d4f;
  margin-left: 4px;
  margin-right: 8px;
  box-shadow: 0 0 4px rgba(255, 77, 79, 0.4);
}

.current-time-label {
  background-color: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
}

/* 添加响应式样式 */
@media screen and (max-width: 1440px) {

  .day-column,
  .day-column-container {
    min-width: 120px;
  }
  .is-compare{
    .day-column,
    .day-column-container {
      min-width: 60px;
    }
  }

  .time-column,
  .time-labels {
    width: 70px;
  }

  .time-column .wtwo-button {
    font-size: 9px;
    padding: 2px 4px;
  }
}

/* 添加响应式样式 */
@media screen and (max-width: 1080px) {

  .day-column,
  .day-column-container {
    min-width: 104px;
  }

  .time-column,
  .time-labels {
    width: 70px;
  }

  .time-column .wtwo-button {
    font-size: 9px;
    padding: 2px 4px;
  }
}

/* 添加响应式样式 */
@media screen and (max-width: 768px) {

  .day-column,
  .day-column-container {
    min-width: 100px;
  }

  .time-column,
  .time-labels {
    width: 70px;
  }

  .time-column .wtwo-button {
    font-size: 9px;
    padding: 2px 4px;
  }
}

/* 毛玻璃效果样式 */
.course-item.blur-effect {
  filter: blur(2px);
  opacity: 0.3;
  transform: scale(0.98);
  transition: all 0.3s ease;

  &:hover {
    filter: blur(1px);
    opacity: 0.5;
    transform: scale(0.99);
  }
}

/* 拖拽预览块专用样式 */
.dragging-course-preview {
  pointer-events: none !important;
  z-index: 1000 !important;
  transition: none !important;
  cursor: grabbing !important;
  /* 移除特殊的视觉效果，保持与原课程块一致 */
}

.drag-time-indicator {
  position: absolute;
  left: 0;
  width: 70px;
  /* 与时间列宽度一致 */
  height: 2px;
  z-index: 200;
  /* 提高z-index确保可见 */
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
}

.drag-time-label {
  background-color: #ff6b35;
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
  animation: dragTimeFlash 1s ease-in-out infinite alternate;
}

.drag-time-arrow {
  width: 0;
  height: 0;
  border-left: 6px solid #ff6b35;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  margin-left: 2px;
}

/* 闪烁动画效果 */
@keyframes dragTimeFlash {
  0% {
    opacity: 0.7;
    transform: scale(1);
  }

  100% {
    opacity: 1;
    transform: scale(1.05);
  }
}

/* 脉冲动画效果 */
@keyframes targetIndicatorPulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

/* 拖拽预览块时间指示器样式 */
.drag-preview-time-indicator {
  pointer-events: none !important;
  z-index: 1001 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.drag-preview-time-label {
  color: white;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  // animation: previewTimeFlash 1.2s ease-in-out infinite alternate;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 预览时间指示器闪烁动画 */
@keyframes previewTimeFlash {
  0% {
    opacity: 0.8;
    transform: scale(1);
    box-shadow: 0 3px 12px rgba(82, 196, 26, 0.4);
  }

  100% {
    opacity: 1;
    transform: scale(1.08);
    box-shadow: 0 4px 16px rgba(82, 196, 26, 0.6);
  }
}

/* 临时占位块样式 */
.temporary-block {
  position: absolute !important;
  background-color: rgba(240, 242, 245,0.75) !important;
  border-radius: 4px !important;
  backdrop-filter: blur(2px) !important;
  box-sizing: border-box !important;
  cursor: pointer !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 14px !important;
  color: #909399 !important;
  font-weight: 500 !important;
  z-index: 10 !important;
  user-select: none !important;
  overflow: hidden;
  
  /* 被动同步的临时块：只显示背景色 */
  &.passive-block {
    background-color: #fef7e0 !important;
    cursor: default !important;
    /* 被动块不能双击添加，改为默认光标 */
    border: 1px dashed #FFBB1D !important;
    /* 虚线边框 */
  }

  &-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    line-height: 1.2;
  }

  &-time {
    font-size: 11px;
    font-weight: 500;
    margin-bottom: 1px;
  }

  &-label {
    font-size: 11px;
    font-weight: 500;
    opacity: 0.9;
  }

  /* 大尺寸临时块（高度大于30px）的字体样式 */
  &.large-block {
    .temporary-block-time {
      font-size: 14px;
      margin-bottom: 6px;
    }

    .temporary-block-label {
      font-size: 14px;
    }
  }
}

/* 新增：上下边缘拖拽手柄样式 */
.resize-handle {
  position: absolute;
  left: 0;
  width: 100%;
  height: 12px;
  /* 手柄的可点击区域高度 */
  cursor: ns-resize !important;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  /* 默认隐藏 */
  transition: opacity 0.2s ease;
  user-select: none;
  pointer-events: auto;
  background-color: transparent;
  /* 移除背景色 */
  border: none;
  /* 移除边框 */
  box-shadow: none;
  /* 移除阴影 */

  /* 手柄的视觉元素：一条线 */
  &::before {
    content: '';
    width: 30px;
    height: 4px;
    /* background-color: #ccc; */
    border-radius: 2px;
    transition: all 0.2s ease;
    /* box-shadow: 0 1px 2px rgba(0,0,0,0.1); */
  }

  &-top {
    top: 0;
    transform: translateY(-50%);
  }

  &-bottom {
    bottom: 0;
    transform: translateY(50%);
  }

  /* 强制设置手柄的光标样式 */
  &,
  & *,
  &:hover,
  &:hover * {
    cursor: ns-resize !important;
  }

  /* 确保手柄区域不会触发课程块的拖拽 */
  * {
    pointer-events: none;
  }
}

.more-courses-mark {
//   position: absolute;
//   top: 0;
//   right: 0px;
  font-weight: 500;
  font-size: 13px;
  color: #2878E8;
  padding: 2px 5px;
  z-index: 100;
  cursor: pointer;
//   top: 50%;
//   transform: translateY(-50%);
}

/* 短课程的渐变遮罩效果 */
.short-course {
  position: relative;
  overflow: hidden;
}

.short-course::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 10px;
  /* 渐变高度 */
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0.9));
  pointer-events: none;
  /* 确保不影响交互 */
  z-index: 1;
}

/* 确保内容在渐变遮罩之下清晰可见 */
.short-course .course-content {
  position: relative;
  z-index: 2;
  mask-image: linear-gradient(to bottom, black 70%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, black 70%, transparent 100%);
}

.object-list {
  width: 100%;
}

.object-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  // transition: all 0.2s ease;
  min-height: 52px;
  /* 与时间刻度的一半高度对齐 */
  margin: 4px;
  box-sizing: border-box;
  border-radius: 4px;
  /* 性能优化：启用硬件加速 */
  will-change: background-color;
  transform: translateZ(0);

  &:hover {
    background-color: #f5f7fa;
  }

  .object-info {
    flex: 1;
    min-width: 0;

    .more-btn {
      cursor: pointer;

      .default-icon {
        display: block;
      }

      .active-icon {
        display: none;
      }

      .current-active-icon {
        display: none;
      }

      &:hover {
        .default-icon {
          display: none;
        }

        .active-icon {
          display: block;
        }
      }
    }
  }

  &.selected {
    background-color: #2878E8;

    // border-left: 3px solid #2878E8;
    .object-name,
    .object-count {
      color: #fff;
    }
    .object-count{
      background-color: rgba(255, 255, 255, 0.2);
    }

    // 学员详情按钮在选中状态下保持白色背景，hover时也不变色
    .student-more-btn {
      background-color: #fff;
      
      .default-icon {
        display: block;
      }

      .active-icon {
        display: none;
      }

      &:hover {
        background-color: #fff;  // 保持白色背景，不变蓝
        
        .default-icon {
          display: block;  // 保持显示默认图标
        }

        .active-icon {
          display: none;
        }
      }
    }

    // 移除按钮在选中状态下也不变色
    .more-btn {
      .default-icon {
        display: block;
      }

      .active-icon {
        display: none;
      }

      .current-active-icon {
        display: none;
      }

      &:hover {
        .default-icon {
          display: block;  // hover时也保持默认图标
        }

        .active-icon {
          display: none;
        }

        .current-active-icon {
          display: none;
        }
      }
    }
  }

  .object-avatar {
    width: 28px;
    height: 28px;
    border-radius: 4px;
    background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    margin-right: 6px;
    flex-shrink: 0;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  

  .object-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    word-break: break-all;
    line-height: 1.4;
    /* 最多显示2行，超出显示省略号 */
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .object-count {
    margin-top: 2px;
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    border-radius: 40px;
    height: 18px;
    width: max-content;
    padding: 2px 8px;
    text-align: center;
    background: #f0f0f0;
    min-width: 41px;
    display: flex;
    align-items: center;
    justify-content: center;

  }
}

.student-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background-color: #F0F0F0;
  border-radius: 50%;
  cursor: pointer;

  .default-icon {
    display: block;
  }

  .active-icon {
    display: none;
  }

  &:hover {
    background-color: var(--wtwo-color-primary);
    
    .default-icon {
      display: none;
    }

    .active-icon {
      display: block;
    }
  }
}



/* 对比模式下的样式 */
.canlendar-container.is-compare {

  .student-header,
  .teacher-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #fafafa;
    border-bottom: 1px solid #f0f0f0;
    // 吸顶设置：相对于外部滚动容器
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

    .student-avatar,
    .teacher-avatar {
      width: 32px;
      height: 32px;
      border-radius: 4px;
      background-color: #2878E8;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 600;
      overflow: hidden;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
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
        font-weight: 500;
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

    .remove-btn {
      color: #999;

      &:hover {
        color: #666;
      }
    }

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

  // 周几表头也需要吸顶，但要在学员/老师头部下方
  .schedule-container .header {
    position: sticky;
    top: 57px; // 学员/老师头部的高度
    z-index: 99; // 比学员/老师头部低一层
  }

  @media screen and (max-width: 1780px) {
    .day-date-today {
      margin-left: 0;
    }

    .holiday-mark {
      margin-left: 0;
    }
  }
}

.object-column-header {
  width: 120px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  border-right: 1px solid #e8e8e8;
  font-weight: 600;
  color: #333;
  position: relative;
  min-width: 120px;
  justify-content: center;
  padding: 8px 6px;
}

/* 简化的时间列表表头样式（始终60px宽） */
.time-column-simple {
  width: 70px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #333;
  border-right: 1px solid #e8e8e8;
  position: sticky;
  left: 0;
  z-index: 11;
  min-width: 70px;
  padding: 8px 6px;
  justify-content: center;
  background-color: #f5f7fa;
  
  /* 对比模式下缩短宽度 */
  &.is-compare {
    width: 45px;
    min-width: 45px;
  }
}

/* 设置按钮 hover 效果 */
.setting-btn {
  padding: 3px 3px;
  border-radius: 3px;
  width: 22px;
  height: 22px;
  &:hover {
    background: rgba(0,0,0,0.06);
  }
}

/* 日期选择列样式 */
.weekday-select-column {
  width: 80px;
  min-width: 80px;
  max-width: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e8e8e8;
  background: #f5f7fa;
  padding: 0 8px;
  box-sizing: border-box;
  
  .setting-btn {
    padding: 3px;
    border-radius: 3px;
    &:hover {
      background: rgba(40, 120, 232, 0.1);
    }
  }
  
  .el-button {
    white-space: nowrap;
  }
}

/* 占位列样式 - 与表头按钮列对齐 */
.placeholder-column {
  width: 80px;
  min-width: 80px;
  max-width: 80px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  background: #fff;
  position: relative;
  box-sizing: border-box;
  
  .placeholder-content {
    border-bottom: 1px solid #e1e1e1;
  }
}

/* 占位列样式 - 与表头按钮列对齐 */
.add-column {
  min-width: 30px;
  flex-shrink: 0;
  background: #fff;
  position: sticky;
  right: 0;
  box-sizing: border-box;
  justify-content: left;
  flex: 1;
  z-index: 10;
  
  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: -1px;
    height: 100%;
    width: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
  
  .add-content {
    border-bottom: 1px solid #e1e1e1;
  }
}

/* 日期选择列样式 */
.add-select-column {
  min-width: 30px;
  flex-shrink: 0;
  justify-content: left;
  display: flex;
  align-items: center;
  background: #f5f7fa;
  box-sizing: border-box;
  padding: 8px 4px;
  position: sticky;
  right: 0;
  z-index: 11;
  
  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: -1px;
    height: 100%;
    width: 1px;
    background-color: #E4E7ED;
    z-index: 12;
  }
  
  .setting-btn {
    padding: 3px;
    border-radius: 3px;
    &:hover {
      background: rgba(40, 120, 232, 0.1);
    }
  }
  
  .el-button {
    white-space: nowrap;
  }
}

/* 滚动条占位区域 */
.scrollbar-placeholder,
.scrollbar-placeholder-content {
  width: 6px;
  flex-shrink: 0;
  background: transparent;
}

/* 时间标签列在有对象列时的样式 */
.time-labels {
  border-left: none;
}

/* 转置视图特定样式 - 星期列 */
.time-trans-view {
  .week-column-container {
    width: 120px;
    min-width: 120px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #e8e8e8;
    background: #fff;
    
    .header {
      height: 43px;
      border-bottom: 1px solid #e8e8e8;
      background-color: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .week-column-header {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 12px;
      height: 100%;
      font-weight: 600;
      font-size: 14px;
      color: #333;
      width: 100%;
    }
    
    .week-column-content {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      height: 100%;
      
      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: #f1f1f1;
      }

      &::-webkit-scrollbar-thumb {
        background: #c1c1c1;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb:hover {
        background: #a8a8a8;
      }
    }
    
    .week-column-wrapper {
      height: 100%;
      width: 100%;
    }
    
    .week-list {
      display: flex;
      flex-direction: column;
      width: 100%;
      height: 100%;
    }
    
    .week-item {
      padding: 8px;
      cursor: pointer;
      border-bottom: 1px solid #f0f0f0;
      margin: 4px;
      border-radius: 4px;
      min-height: 52px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        background: #f5f7fa;
      }
      
      &.selected {
        background-color: #2878E8;
        
        .week-name,
        .holiday-tag {
          color: #fff !important;
        }
        
        .today-tag {
          background-color: rgba(255, 255, 255, 0.2);
          color: #fff;
        }
        .week-date {
          background: rgba(255, 255, 255, 0.2)!important;
          color: #fff !important;
        }
      }
      
      .week-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        width: 100%;
        
        .week-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          display: flex;
          align-items: center;
          gap: 4px;

          
          &.date-today {
            color: var(--wtwo-color-primary);
            font-weight: 600;
          }
          
          .today-tag {
            width: 20px;
            height: 20px;
            line-height: 20px;
            text-align: center;
            color: #606266;
            font-size: 12px;
            border-radius: 50%;
            background: #2878E8;
            color: #fff;
            font-weight: 500;
            margin-left: 5px;
          }
        }
        
        .week-date {
            font-size: 12px;
            color: #999;
            background: #f0f0f0;
            border-radius: 40px;
            min-width: 44px;
            text-align: center;
            height: 20px;
            line-height: 20px;
            font-size: 12px;
            color: #606266;
            margin-top: 4px;
        }
        
        .holiday-tag {
          background: #ff4d4f;
          color: #fff;
          font-size: 12px;
          padding: 2px 4px;
          border-radius: 4px;
        }
      }
    }
  }
}
</style>