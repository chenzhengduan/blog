<template>
    <div class="object-calendar-view" v-loading="loading" element-loading-target=".object-calendar-view">
        <div class="calendar-container">
            <div class="corner sticky">{{ transToConfigDescript(objectName) }}</div>

            <!-- 顶部日期头 -->
            <div class="header sticky">
                <div
                    v-for="day in weekDays"
                    :key="day.key"
                    class="header-cell"
                    :class="{ today: day.isToday }"
                >
                    <div class="header-week">{{ day.weekText }}</div>
                    <div class="header-date" :class="{ circle: day.isToday }">{{ day.isToday?'今':day.dateText }}</div>
                    <span v-if="day.isHoliday" class="holiday-flag">假</span>
                </div>
            </div>

            <!-- 左侧老师列 -->
            <div class="sidebar" v-if="visibleTeachers.length&&!(props.assign==1 && (!assignList || assignList.length === 0))">
                <div
                    v-for="(t, idx) in visibleTeachers"
                    :key="t.id"
                    class="teacher-cell"
                    :style="{ height: rowHeights[idx] ? rowHeights[idx] + 'px' : 'auto' }"
                    :ref="el => setTeacherRowRef(el, idx)"
                >
                    <div class="avatar" v-if="t.avatar && shouldShowAvatar">
                        <img :src="t.avatar" alt="" />
                    </div>
                    <div class="avatar placeholder" v-else-if="shouldShowAvatar">
                        {{ getNameInitial(t.name) }}
                    </div>
                    <div class="teacher-info">
                        <div class="teacher-name">{{ t.name }}</div>
                    </div>
                    <div class="flex-center mt-8px">
                        <div class="teacher-course-num">{{ getTeacherCourseCount(t.id) }}条</div>
                        <div v-if="props.timetableType == CourseTimetableTypeEnum.StudentTimetable" class="more-btn ml-4px mt-1px" 
                            @click.stop="openStudentPopover($event, t)"
                            v-click-outside="(event: Event) => handleStudentClickOutside(event, t)">
                            <el-icon class="default-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-xueyuanxinxikapianmoren"></use></svg></el-icon>
                            <el-icon class="active-icon" size="16px"><svg aria-hidden="true"><use xlink:href="#w2-gengduohover"></use></svg></el-icon>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 可滚动内容区 -->
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
                <div 
                    v-else-if="(props.assign==1 && (!assignList || assignList.length === 0))||!visibleTeachers || visibleTeachers.length === 0"
                    class="empty-state"
                >
                    <el-empty :image="globalData.emptyPng" :image-size="100">
                        <template #description>
                            <div class="color-[#666]">{{props.assign==1?('请添加指定的'+transToConfigDescript(objectName)):'暂无排课'}}</div>
                        </template>
                    </el-empty>
                </div>
                <!-- 正常内容区域 -->
                <div
                    v-else
                    v-for="(t, idx) in visibleTeachers"
                    :key="t.id"
                    class="grid-row"
                    :ref="el => setGridRowRef(el, idx)"
                >
                    <div v-for="day in weekDays" :key="day.key" class="grid-cell" :class="{ 'is-past': isPastDay(day.key) }">
                        
                        <template v-for="(item, idx) in getCellItems(t.id, day.key)">
                            <!-- 排课卡片 -->
                            <div
                                v-if="item.type === 'course'"
                                :key="`course-${idx}`"
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
                                :key="`schedule-${idx}`"
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
                            @dblclick="handleDoubleClickAddCourse(t, day.key)"
                        >
                            双击新增排课
                        </div>
                    </div>
                    
                </div>
            </div>
        </div>
        <div class="view-more mt-12px">
            <el-button 
                type="primary" 
                plain 
                v-if="showViewMore&&visibleTeachers.length>0&&!(props.assign==1 && (!assignList || assignList.length === 0))"
                @click="handleViewMoreClick"
            >
                查看更多{{ transToConfigDescript(objectName) }}（剩余{{ remainingCount }}）
            </el-button>
        </div>
        <el-dialog v-model="overLimitTipVisible" title="温馨提示" width="420px">
            <div class="py-16px line-height-22px">当前课表显示的{{transToConfigDescript(objectName)}}数量过多，可能会影响体验，建议切换到“{{transToConfigDescript(objectName)}}与时刻”视图</div>
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


</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ClickOutside as vClickOutside } from 'element-plus'
import dayjs from 'dayjs'
import { queryCalendarTeacher, queryCalendarClass, queryCalendarClassroom, queryCalendarStudent } from '@/api/arrange'
import { getTint, getCourseStatusColor, buildColorDetailMap, buildExportColumns, getEnabledRightTagsFromPreference, getEnabledLeftFieldsFromPreference, getCourseFieldValue, getCourseTagText, getCourseTagColor, getNameInitial } from '@/utils'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { CourseTimetableTypeEnum, CourseTimetableFieldTypeEnum, type CourseTimetableFieldSetting } from '@/types/model/timetable-preference'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
import { getCurrentInstance } from 'vue'
import { queryHoliday } from '@/api/comm'
import CourseDetailPopover from '@/components/CourseDetailPopover.vue'
import StudentDetailPopover from '@/pages/scheduleManage/components/studentDetailPopover.vue'
import { transToConfigDescript } from '@/utils/filters/filters'

type Teacher = { id: string | number; name: string; avatar?: string; desc?: string; campusId?: string }

const props = defineProps<{
    startDate?: string
    endDate?: string
    currentWeek?: Date
    searchParams?: any
    timetableType?: any
    assignList?: any[]
    assign:number
}>()

const emit = defineEmits<{
  'canlendar-dbclick-add-course': [data: { object: any; date: string; timetableType: any }]
}>()

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;

const loading = ref(false)
const gridRef = ref<HTMLElement | null>(null)
const rowHeights = ref<number[]>([])
const gridRowRefs: Array<Element | null> = []
const teacherRowRefs: Array<Element | null> = []
let ro: ResizeObserver | null = null

// 课程详情浮窗相关
const courseDetailPopoverRef = ref()
const selectedCourseData = ref<any>({})
const virtualRef = ref()
const courseItemRefs = new Map()

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
    [CourseTimetableTypeEnum.TimeTimetable]: '时间',
  }
  return map[props.timetableType] || '任课老师'
})

// 判断是否应该显示头像（班级和教室视图不显示头像）
const shouldShowAvatar = computed(() => {
  return props.timetableType !== CourseTimetableTypeEnum.ClassTimetable && 
         props.timetableType !== CourseTimetableTypeEnum.ClassroomTimetable
})

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

// 计算周起止（周一到周日）
const startOfWeek = computed(() => {
    const base = props.currentWeek ? dayjs(props.currentWeek) : (props.startDate ? dayjs(props.startDate) : dayjs())
    const mondayIndex = (base.day() + 6) % 7
    return base.subtract(mondayIndex, 'day').startOf('day')
})
const endOfWeek = computed(() => {
    return props.endDate ? dayjs(props.endDate) : startOfWeek.value.add(6, 'day')
})

const holidaySet = ref<Set<string>>(new Set())

const weekDays = computed(() => {
    const arr: Array<{ key: string; weekText: string; dateText: string; isToday: boolean; isHoliday: boolean }> = []
    const weekdays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']
    for (let i = 0; i < 7; i++) {
        const d = startOfWeek.value.add(i, 'day')
        arr.push({
            key: d.format('YYYY-MM-DD'),
            weekText: weekdays[i],
            dateText: d.format('DD'),
            isToday: d.isSame(dayjs(), 'day'),
            isHoliday: holidaySet.value.has(d.format('YYYY-MM-DD'))
        })
    }
    return arr
})

// 偏好设置颜色
const timetablePrefStore = useTimetablePreferences()
const currentTimetablePref = computed(() => timetablePrefStore.preferenceByType(props.timetableType) as any)
const enabledColorSetting = computed(() => {
    const cs = currentTimetablePref.value && Array.isArray(currentTimetablePref.value.ColorSettings)
        ? currentTimetablePref.value.ColorSettings as any[]
        : []
    if (!cs || cs.length === 0) return null
    return cs.find((s:any) => s && s.IsEnabled) || cs[0]
})
const colorDetailMap = computed(() => buildColorDetailMap((enabledColorSetting.value as any)?.ColorDetails))
function getStatusColor(item:any){
    return getCourseStatusColor(
        item,
        enabledColorSetting.value?.SettingType as any,
        colorDetailMap.value as any,
        props.timetableType
    )
}

// 偏好设置：字段与标签（与抽屉一致）
const preferenceVm = computed(() => timetablePrefStore.preferenceByType(props.timetableType) as any)
const enabledRightTags = computed<any[]>(() => {
    return getEnabledRightTagsFromPreference({
        CardShowInformationSettings: (preferenceVm.value as any)?.CardShowInformationSettings,
        FieldTypeEnum: { Tag: CourseTimetableFieldTypeEnum.Tag, Main: CourseTimetableFieldTypeEnum.Main }
    }) as unknown as any[]
})
const enabledLeftFields = computed<any[]>(() => {
    return getEnabledLeftFieldsFromPreference({
        CardShowInformationSettings: (preferenceVm.value as any)?.CardShowInformationSettings,
        FieldTypeEnum: { Tag: CourseTimetableFieldTypeEnum.Tag, Main: CourseTimetableFieldTypeEnum.Main }
    }) as unknown as any[]
})

function getFieldValue(item: any, fieldName: string): string {
    return getCourseFieldValue(item, fieldName)
}
function getTagText(item: any, fieldName: string): string {
    return getCourseTagText(item, fieldName)
}
function getTagColor(fieldName: string): string {
    return getCourseTagColor(fieldName, { TagColorSettings: (preferenceVm.value as any)?.TagColorSettings })
}

// 数据
const teachers = ref<Teacher[]>([])
const dataMap = ref<Record<string, Record<string, any[]>>>({})
const teacherCourseCounts = ref<Record<string, number>>({})

// 可见对象数量控制（除“时间”视图外均生效）
const visibleCount = ref(5)
const overLimitTipVisible = ref(false)

const isPaginatedView = computed(() => props.timetableType !== CourseTimetableTypeEnum.TimeTimetable)
const visibleTeachers = computed<Teacher[]>(() => {
    if (!isPaginatedView.value) return teachers.value
    return teachers.value.slice(0, Math.max(0, visibleCount.value))
})
const remainingCount = computed(() => {
    if (!isPaginatedView.value) return 0
    return Math.max(teachers.value.length - visibleCount.value, 0)
})
const showViewMore = computed(() => isPaginatedView.value && remainingCount.value > 0)

function handleViewMoreClick(){
    if (!isPaginatedView.value) return
    if (visibleCount.value >= 25) {
        overLimitTipVisible.value = true
        return
    }
    visibleCount.value = Math.min(visibleCount.value + 5, teachers.value.length)
}

// 当对象列表或类型变化时，复位或适配可见数量
watch(() => [teachers.value.length, props.timetableType], () => {
    if (isPaginatedView.value) {
        // 进入可分页视图或对象数量变化时，确保默认至少显示5个，且不超过总数
        const base = 5
        visibleCount.value = Math.min(Math.max(visibleCount.value || base, base), teachers.value.length)
    } else {
        // 非可分页视图（时间视图）展示全部
        visibleCount.value = teachers.value.length
    }
}, { immediate: true })

function getTeacherCourseCount(teacherId: string | number): number {
    const tid = String(teacherId)
    return teacherCourseCounts.value[tid] || 0
}

function getCellItems(teacherId: string | number, dayKey: string){
    const tid = String(teacherId)
    return dataMap.value[dayKey]?.[tid] || []
}
function isPastDay(dateKey: string){
    const d = dayjs(dateKey)
    return d.isBefore(dayjs(), 'day')
}
// 导出列（公用工具）
const exportColumns = computed<string[]>(() => buildExportColumns(ALL_COLUMNS as unknown as any[]))
let loadDataRequestCounter = 0
let loadDataTimer: any = null
let pendingRequestId: number | null = null
async function loadData(force = false){
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

            const s = startOfWeek.value.format('YYYY-MM-DD')
            const e = endOfWeek.value.format('YYYY-MM-DD')
            // 根据课表类型设置不同的ID列表参数
            const fieldNames = getFieldNames(props.timetableType)

            const mergedParams = {
                ...(props.searchParams || {}),
                PageSize: 99999,
                PageIndex: 1,
                StartDate: s,
                EndDate: e,
                sort: 'StartTime',
                desc: 0,
                ExportColumn: exportColumns.value,
                IDList:props.assignList&&props.assignList.length?props.assignList.map((i)=>i.ID):[]
            }
            loading.value = true
            try{
                // 先请求节假日（所有视图都需要）
                const holidayRes: any = await queryHoliday({ sdate: s, edate: e })
                const hs = new Set<string>()
                const holidays: any[] = (holidayRes && (holidayRes.Data?.Data || [])) || []
                holidays.forEach((h: any) => {
                    const k = dayjs(h.Date).format('YYYY-MM-DD')
                    if (k && k !== 'Invalid Date') hs.add(k)
                })
                holidaySet.value = hs

                // 如果是指定对象视图且assignList为空，不发起课程数据请求，但保留节假日显示
                if (props.assign==1 && 
                    (!props.assignList || props.assignList.length === 0)) {
                    teachers.value = []
                    dataMap.value = {}
                    teacherCourseCounts.value = {}
                    resolve()
                    return
                }

                // 根据课表类型调用不同的API
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

                const res: any = await apiPromise
        const list: any[] = (res?.Data ?? []) as any[]
        const teacherIdSet = new Map<string | number, Teacher>()
        const map: Record<string, Record<string, any[]>> = {}
        const counts: Record<string, number> = {}

        const pushItem = (dateKey: string, objectKey: string | number, it: any) => {
            const tkey = String(objectKey)
            if (!map[dateKey]) map[dateKey] = {}
            if (!map[dateKey][tkey]) map[dateKey][tkey] = []
            map[dateKey][tkey].push({
                ...it
            })
            // 按StartTime时间从早到晚排序
            map[dateKey][tkey].sort((a: any, b: any) => {
                const timeA = new Date(a.StartTime).getTime()
                const timeB = new Date(b.StartTime).getTime()
                return timeA - timeB
            })
        }

        // 根据assignList是否有值来决定处理方式
        if (props.assignList && props.assignList.length > 0) {
            // 情况1：assignList有值，以assignList为基准
            props.assignList.forEach((assignItem: any) => {
                const tid = String(assignItem.ID)
                const tname = assignItem[fieldNames.nameField] || assignItem.Name || '未知'
                const avatar = fieldNames.avatarField?assignItem[fieldNames.avatarField] :''
                
                // 先设置对象信息
                teacherIdSet.set(tid, { id: tid, name: tname, avatar:avatar||assignItem['Photo'],campusId:assignItem.CampusID||assignItem.CampusList||'' })
                counts[tid] = 0 // 初始课程数为0
                
                // 从API返回的数据中查找对应的排课信息
                const matchedItem = list.find((item: any) => String(item[fieldNames.idField]) === tid)
                
                if (matchedItem) {
                    // 处理排课数据
                    if (Array.isArray(matchedItem.CourseList)) {
                        counts[tid] = matchedItem.CourseList.length
                        matchedItem.CourseList.forEach((course: any) => {
                            const dk = dayjs(course.StartTime).format('YYYY-MM-DD')
                            // 保留对象信息，便于卡片展示需要
                            const payload = { 
                                ...course, 
                                [fieldNames.idField]: tid, 
                                [fieldNames.nameField]: tname,
                                type: 'course' // 标记为排课
                            }
                            pushItem(dk, tid, payload)
                        })
                    }
                    
                    // 处理老师日程数据
                    if (Array.isArray(matchedItem.ScheduleList)) {
                        matchedItem.ScheduleList.forEach((schedule: any) => {
                            const dk = dayjs(schedule.StartTime).format('YYYY-MM-DD')
                            // 保留对象信息，便于卡片展示需要
                            const payload = { 
                                ...schedule, 
                                [fieldNames.idField]: tid, 
                                [fieldNames.nameField]: tname,
                                type: 'schedule' // 标记为日程
                            }
                            pushItem(dk, tid, payload)
                        })
                    }
                }
            })
        } else {
            // 情况2：assignList没有值，维持现状（按原有逻辑处理API返回的数据）
            list.forEach((item:any)=>{
                const tid = String(item[fieldNames.idField])
                const tname = item[fieldNames.nameField]
                const avatar = fieldNames.avatarField?item[fieldNames.avatarField] :''
                const campusId = item.CampusID||item.CampusList||'' // 保存CampusID
                teacherIdSet.set(tid, { id: tid, name: tname, avatar, campusId })

                const courses = Array.isArray(item.CourseList) ? item.CourseList : []
                counts[tid] = courses.length
                courses.forEach((course:any) => {
                    const dk = dayjs(course.StartTime).format('YYYY-MM-DD')
                    // 保留对象信息，便于卡片展示需要
                    const payload = { 
                        ...course, 
                        [fieldNames.idField]: tid, 
                        [fieldNames.nameField]: tname,
                        type: 'course' // 标记为排课
                    }
                    pushItem(dk, tid, payload)
                })
                
                // 处理老师日程数据
                if (Array.isArray(item.ScheduleList)) {
                    item.ScheduleList.forEach((schedule: any) => {
                        const dk = dayjs(schedule.StartTime).format('YYYY-MM-DD')
                        // 保留对象信息，便于卡片展示需要
                        const payload = { 
                            ...schedule, 
                            [fieldNames.idField]: tid, 
                            [fieldNames.nameField]: tname,
                            type: 'schedule' // 标记为日程
                        }
                        pushItem(dk, tid, payload)
                    })
                }
            })
        }

                // 只有当前请求是最新的才更新数据
                if (currentRequestId === pendingRequestId) {
                    teachers.value = Array.from(teacherIdSet.values())
                    dataMap.value = map
                    teacherCourseCounts.value = counts
                }
                resolve()
            } catch (err) {
                // 只有当前请求是最新的才处理失败态
                if (currentRequestId === pendingRequestId) {
                    reject(err)
                } else {
                    resolve()
                }
            } finally {
                // 只有当前请求是最新的才关闭loading
                if (currentRequestId === pendingRequestId) {
                    loading.value = false
                }
            }
        }, 200)
    })
}

onMounted(()=>{
    // 监听网格行高度变化并同步到老师行
    if ('ResizeObserver' in window) {
        ro = new ResizeObserver(() => {
            syncRowHeights()
        })
    }
})
function setGridRowRef(el: any, index: number){
    gridRowRefs[index] = el as Element | null
    if (el && ro) ro.observe(el as Element)
}

function setTeacherRowRef(el: any, index: number){
    teacherRowRefs[index] = el as Element | null
}

function syncRowHeights(){
    const heights: number[] = gridRowRefs.map(el => (el ? (el as Element).getBoundingClientRect().height : 0))
    rowHeights.value = heights
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


// 浮窗事件回调
const handleRollCall = (courseData: any) => { console.log('上课点名:', courseData) }

// 处理双击新增排课
const handleDoubleClickAddCourse = (object: Teacher, dateKey: string) => {
    // 准备事件数据
    const eventData: any = {
        object,
        date: dateKey,
        timetableType: props.timetableType
    }
    
    // 如果是班级课表，添加CampusID
    if (object.campusId) {
        eventData.campusId = object.campusId
    }

    // 抛出事件给父组件处理
    emit('canlendar-dbclick-add-course', eventData)
}

// 打开学员浮窗（学员视图侧边“更多”按钮）
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

// 处理点击外部区域隐藏popover
const handleStudentClickOutside = (event: Event, item: any) => {
    if ((selectedStudent.value?.ID) === (item.ID)) return
    const target = event.target as Element
    const popoverElement = document.querySelector('.wtwo-student-detail-popover')
    if (popoverElement && popoverElement.contains(target)) return
    hideStudentPopover()
}

function hideStudentPopover(){
    if (studentPopoverRef.value) {
        studentPopoverRef.value.hide()
    }
}


watch(() => [props.currentWeek, props.searchParams, props.timetableType], (nv, ov) => {
    const weekChanged = ov ? nv[0] !== ov[0] : false
    const paramsChanged = ov ? nv[1] !== ov[1] : false
    const typeChanged = ov ? nv[2] !== ov[2] : true // 第一次渲染时认为类型已改变
    const hasParams = props.searchParams && Object.keys(props.searchParams || {}).length > 0
    if (!hasParams) return
    // 视图切换或日期变更时，重置可见数量
    if (typeChanged || weekChanged) {
        if (isPaginatedView.value) {
            visibleCount.value = Math.min(5, teachers.value.length)
        }
    }
    if (weekChanged || typeChanged ) {
        loadData(true)
        return
    }
    if (paramsChanged) {
        loadData(true)
    }
}, { deep: true, immediate: true })

onUnmounted(() => {
    courseItemRefs.clear()
})

// 监听assignList变化，确保数据更新后同步行高
watch(() => props.assignList, async (nv, ov) => {
    if (nv !== ov) {
        await loadData(true)
        await nextTick()
        setTimeout(() => {
            syncRowHeights()
        }, 100)
    }
}, { deep: true })

</script>

<style scoped lang="scss">
.object-calendar-view {
    width: 100%;
}

.calendar-container {
    display: grid;
    grid-template-columns: 100px 1fr;
    grid-template-rows: 40px 1fr;
    position: relative;
}

.corner {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
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
    }
}

.header {
    grid-column: 2 / 3;
    grid-row: 1 / 2;
    position: sticky;
    top: 0;
    z-index: 2;
    display: grid;
    grid-template-columns: repeat(7, minmax(128px, 1fr));
    background: #F5F7FA;
    border-bottom: 1px solid #E4E7ED;
}

.header-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 7px 12px;
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

.sidebar {
    grid-column: 1 / 2;
    grid-row: 2 / 3;
    left: 0;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #E4E7ED;
}

.teacher-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 12px;
    box-sizing: border-box;
    border-bottom: 1px solid #E4E7ED;
    word-break: break-all;
    .more-btn{
        cursor: pointer;
        .default-icon{
            display: block;
        }
        .active-icon{
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
    margin-top: 10px;
}

.teacher-name {
    font-weight: 500;
    color: #303133;
    line-height: 20px;
    text-align: center;
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

.grid {
    grid-column: 2 / 3;
    grid-row: 2 / 3;
    overflow: auto;
}

.grid-row {
    display: grid;
    grid-template-columns: repeat(7, minmax(128px, 1fr));
    min-height: 120px;
}

.grid-cell {
    border-right: 1px solid #E4E7ED;
    border-bottom: 1px solid #E4E7ED;
    padding: 8px;
    background: #fff;
    display: flex;
    flex-direction: column;
}
.grid-cell.is-past {
    background: #F9FAFC;
}

.grid-row .grid-cell:last-child {
    border-right: none;
}
.double-click-add-course{
    display: flex;
    align-items: center;
    justify-content: center;
    flex:1;
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

.grid-cell:hover .double-click-add-course{
    visibility: visible;
}

.empty-state {
    grid-column: 1 / -1;
    grid-row: 1 / -1;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 200px;
    background: #fff;
}

.empty-state-content {
    text-align: center;
}

.empty-state-text {
    font-size: 14px;
    color: #909399;
    line-height: 1.5;
}
.holiday-flag{
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
.view-more{
    width: 100%;
    text-align: center;
}
</style>