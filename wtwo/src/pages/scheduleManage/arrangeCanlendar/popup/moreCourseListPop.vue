<template>
    <el-drawer v-model="visibleInner" size="480px" custom-class="more-course-drawer" destroy-on-close
        :append-to-body="true">
        <template #title>
            <div> {{ dayjs(selectedDate).format('YYYY-MM-DD dddd') + (courseList.length ? '（共' + courseList.length +
                '节课）':'') }}</div>
        </template>
        <div class="drawer-body" v-loading="loading">
            <template v-if="courseList.length">
                <div v-for="group in groupedCourseList" :key="group.key">
                    <template v-if="group.items.length">
                        <div class="wtwo-schedule-group-title">{{ group.label }}</div>
                        <template v-for="(item, idx) in group.items">
                            <!-- 排课卡片 -->
                            <div v-if="item.type === 'course'" :key="`course-${idx}`" class="wtwo-schedule-course-card"
                                :data-course-id="item.ID" :style="{
                                    borderTopColor: getStatusColor(item),
                                    backgroundColor: getTint(getStatusColor(item), 0.1)
                                }" :ref="el => setCourseItemRef(el, item)"
                                v-click-outside="(event: Event) => handleClickOutside(event, item)"
                                @click.native="handleCourseClick($event, item)">
                                <div class="wtwo-schedule-course-time">{{ dayjs(item.StartTime).format('HH:mm') }}-{{
                                    dayjs(item.EndTime).format('HH:mm') }}</div>
                                <div class="wtwo-schedule-course-meta">
                                    <div class="wtwo-schedule-meta-item ellipsis-single" v-for="field in enabledLeftFields"
                                        :key="String(field.FieldName)"
                                        :title="getFieldValue(item, String(field.FieldName))">
                                        {{ getFieldValue(item, String(field.FieldName)) }}
                                    </div>
                                </div>
                                <div class="wtwo-schedule-course-tags">
                                    <div class="wtwo-schedule-course-tags-item ellipsis-single"
                                        v-for="tag in enabledRightTags" :key="String(tag.FieldName)"
                                        v-show="!!getTagText(item, String(tag.FieldName))"
                                        :style="{ color: getTagColor(String(tag.FieldName)) }"
                                        :title="getTagText(item, String(tag.FieldName))">
                                        {{ getTagText(item, String(tag.FieldName)) }}
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 日程卡片 -->
                            <div v-if="item.type === 'schedule'" :key="`schedule-${idx}`" class="wtwo-schedule-schedule-card"
                                :data-course-id="item.ID" :ref="el => setCourseItemRef(el, item)"
                                v-click-outside="(event: Event) => handleClickOutside(event, item)"
                                @click.native="handleCourseClick($event, item)">
                                <div class="fixed-type-label">日程</div>
                                <div class="wtwo-schedule-schedule-time">{{ dayjs(item.StartTime).format('HH:mm') }}-{{
                                    dayjs(item.EndTime).format('HH:mm') }}</div>
                                <div class="wtwo-schedule-schedule-meta">
                                    <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.Content">{{ item.Content }}</div>
                                    <div class="wtwo-schedule-meta-item ellipsis-single" :title="item.TeacherName">{{ item.TeacherName }}</div>
                                </div>
                            </div>
                        </template>
                        
                    </template>
                </div>
            </template>
            <el-empty v-else description="暂无数据" :image-size="100"/>
        </div>
        <CourseDetailPopover
            ref="courseDetailPopoverRef"
            :course-data="selectedCourseData"
            :virtual-ref="virtualRef"
            :need-api="false"
            @show="handlePopoverShow"
            @hide="handlePopoverHide"
            :timetable-type="props.timetableType || CourseTimetableTypeEnum.TimeTimetable"
        />
        <!-- 课程详情浮窗 -->
        <!-- <CourseDetailPopover ref="courseDetailPopoverRef" :course-data="selectedCourseData" :virtual-ref="virtualRef" :need-api="false"
            :timetable-type="props.timetableType ?? CourseTimetableTypeEnum.TimeTimetable" @roll-call="handleRollCall"
            @adjust-students="handleAdjustStudents" @schedule-details="handleScheduleDetails" @show="handlePopoverShow"
            @hide="handlePopoverHide" /> -->
    </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'
import { queryCalendarTeacher, queryCalendarClass, queryCalendarClassroom, queryCalendarStudent, queryCourseNew } from '@/api/arrange'
import { ClickOutside as vClickOutside } from 'element-plus'
import { getTint, getCourseStatusColor, getEnabledRightTagsFromPreference, getEnabledLeftFieldsFromPreference, getCourseFieldValue, getCourseTagText, getCourseTagColor, buildExportColumns, buildColorDetailMap, extractPreferenceColorIds } from '@/utils'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { CourseTimetableTypeEnum, CourseTimetableFieldTypeEnum, type CourseTimetableFieldSetting } from '@/types/model/timetable-preference'
import CourseDetailPopover from '@/components/CourseDetailPopover.vue'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
import useEvent from '@/config/event'

const props = defineProps<{
    modelValue: boolean
    selectedDate?: string
    searchParams?: any
    timetableType?: any
    // 当 SettingType 为 ByTeacher/ByCourse/ByClassroom 时，允许外部注入颜色解析，确保与外部视图严格一致
    preferenceColorResolver?: (type: 'ByTeacher' | 'ByCourse' | 'ByClassroom', ids: { TeacherID?: string | number; ShiftID?: string | number; ClassroomID?: string | number }, timetableType?: any) => string
}>()

const emits = defineEmits<{
    (e: 'update:modelValue', v: boolean): void
}>()

const visibleInner = computed({
    get: () => props.modelValue,
    set: (v: boolean) => emits('update:modelValue', v)
})

const selectedDate = ref<string>(props.selectedDate || '')
const loading = ref(false)
const courseList = ref<any[]>([])

// 根据开始时间分组：上午(00:00-12:00)、下午(12:00-18:00)、晚上(18:00-24:00)
const groupedCourseList = computed(() => {
    const groups = [
        { key: 'morning', label: '上午', items: [] as any[] },
        { key: 'afternoon', label: '下午', items: [] as any[] },
        { key: 'evening', label: '晚上', items: [] as any[] },
    ]
    const list = Array.isArray(courseList.value) ? courseList.value : []
    list.forEach((item: any) => {
        const start = dayjs(item?.StartTime)
        const hour = start.isValid() ? start.hour() : -1
        if (hour >= 0 && hour < 12) {
            groups[0].items.push(item)
        } else if (hour >= 12 && hour < 18) {
            groups[1].items.push(item)
        } else if (hour >= 18 && hour < 24) {
            groups[2].items.push(item)
        } else {
            // 无效时间，默认丢到上午，避免遗漏
            groups[0].items.push(item)
        }
    })
    return groups
})

// 课程详情浮窗相关
const courseDetailPopoverRef = ref()
const selectedCourseData = ref<any>({})
const courseItemRefs = new Map()
const virtualRef = ref()
const isPopoverVisible = ref(false)
const event = useEvent()

// 与 CourseDetailPopover.vue 保持一致的导出列（公用工具）
const exportColumns = computed<string[]>(() => buildExportColumns(ALL_COLUMNS as unknown as any[]))

function buildParams() {
    const base = props.searchParams || {}
    const date = selectedDate.value
    return {
        ...base,
        StartDate: date,
        EndDate: date,
        PageIndex: 1,
        PageSize: 999,
        sort: 'StartTime',
        desc: 0,
        ExportColumn: exportColumns.value
    }
}

// 偏好设置：根据传入课表类型使用对应的颜色与字段/标签设置
const timetablePrefStore = useTimetablePreferences()
const preferenceVm = computed(() => timetablePrefStore.preferenceByType(props.timetableType ?? CourseTimetableTypeEnum.TimeTimetable) as any)
const enabledColorSetting = computed(() => {
    const cs = (preferenceVm.value?.ColorSettings || []) as any[]
    return Array.isArray(cs) && cs.length > 0 ? (cs.find(s => s && s.IsEnabled) || cs[0]) : null
})
const colorDetailMap = computed<Record<string, string>>(() => buildColorDetailMap((enabledColorSetting.value as any)?.ColorDetails))
function getStatusColor(item: any) {
    const st = (enabledColorSetting.value as any)?.SettingType as string | undefined
    if (st === 'ByTeacher' || st === 'ByCourse' || st === 'ByClassroom') {
        if (typeof props.preferenceColorResolver === 'function') {
            const ids = extractPreferenceColorIds(item)
            const tt = (props.timetableType as any) ?? 'TimeTimetable'
            const color = props.preferenceColorResolver(st as any, ids, tt)
            if (color) return color
        }
    }
    return getCourseStatusColor(
        item,
        st,
        colorDetailMap.value as any,
        (props.timetableType as any) ?? 'TimeTimetable'
    )
}

// 信息与标签：按偏好设置渲染
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

function getTagText(item: any, fieldName: string): string {
    return getCourseTagText(item, fieldName)
}

function getFieldValue(item: any, fieldName: string): string {
    return getCourseFieldValue(item, fieldName)
}

// 标签颜色映射：优先使用偏好里的 TagColorSettings，其次使用默认
function getTagColor(fieldName: string): string {
    return getCourseTagColor(fieldName, { TagColorSettings: (preferenceVm.value as any)?.TagColorSettings })
}

// 设置课程项引用
const setCourseItemRef = (el: any, item: any) => {
    if (el) {
        const itemId = item.ID
        courseItemRefs.set(itemId, el)
    }
}

// 处理课程点击
const handleCourseClick = async (event: Event, item: any) => {
    // 阻止事件冒泡
    event.stopPropagation()

    // 如果点击的是同一个课程且popover已显示，则保持显示状态，不做任何操作
    if (isPopoverVisible.value && selectedCourseData.value?.ID === item.ID) {
        return
    }

    // 更新课程数据和虚拟引用
    
    selectedCourseData.value=item
    console.log(selectedCourseData.value)
    const targetElement = event.currentTarget as HTMLElement
    virtualRef.value = targetElement

    await nextTick()
    if (courseDetailPopoverRef.value) {
        courseDetailPopoverRef.value.show()
        isPopoverVisible.value = true
    }
}

// 处理点击外部区域隐藏popover
const handleClickOutside = (event: Event, item: any) => {
    if (!isPopoverVisible.value) {
        return
    }

    // 如果点击的是当前显示popover的课程项，不隐藏popover
    if (selectedCourseData.value?.ID === item.ID) {
        return
    }

    // 检查点击是否在popover内部
    const target = event.target as Element
    const popoverElement = document.querySelector('.course-detail-popover')
    if (popoverElement && popoverElement.contains(target)) {
        return
    }

    // 点击在其他区域，隐藏popover
    hidePopover()
}

// 隐藏popover
const hidePopover = () => {
    if (courseDetailPopoverRef.value) {
        // selectedCourseData.value = {}
        courseDetailPopoverRef.value.hide()
        isPopoverVisible.value = false
    }
}

// 处理浮窗事件
const handleRollCall = (courseData: any) => {
    console.log('上课点名:', courseData)
    // 这里可以添加具体的业务逻辑
}

const handleAdjustStudents = (courseData: any) => {
    console.log('调整学员:', courseData)
    // 这里可以添加具体的业务逻辑
}

const handleScheduleDetails = (courseData: any) => {
    console.log('排课详情:', courseData)
    // 这里可以添加具体的业务逻辑
}

// 监听popover的显示/隐藏状态
const handlePopoverShow = () => {
    isPopoverVisible.value = true
}

const handlePopoverHide = () => {
    isPopoverVisible.value = false
}

async function fetchList() {
    if (!selectedDate.value) {
        courseList.value = []
        return
    }
    loading.value = true
    try {
        const params = buildParams()
        let res: any
        // 根据课表类型调用不同的API
        switch (props.timetableType) {
            case CourseTimetableTypeEnum.ClassTimetable:
                res = await queryCalendarClass(params)
                break
            case CourseTimetableTypeEnum.ClassroomTimetable:
                res = await queryCalendarClassroom(params)
                break
            case CourseTimetableTypeEnum.StudentTimetable:
                res = await queryCalendarStudent(params)
                break
            case CourseTimetableTypeEnum.TeacherTimetable:
                res = await queryCalendarTeacher(params)
                break;
            default:
                res = await queryCourseNew(params)
                break;
        }
        
        const raw = res && (res.Data || res.data || {})
        const list = Array.isArray(raw?.Data) ? raw.Data : (Array.isArray(raw) ? raw : (raw?.List || []))
        
        // 处理课程和日程数据
        const processedList: any[] = []
        
        if (Array.isArray(list)) {
            list.forEach((item: any) => {
                if(props.timetableType==CourseTimetableTypeEnum.TimeTimetable){
                    processedList.push({
                        ...item,
                        type: 'course' // 标记为排课
                    })
                }else{
                    // 处理排课数据
                    if (Array.isArray(item.CourseList)) {
                        item.CourseList.forEach((course: any) => {
                            processedList.push({
                                ...course,
                                type: 'course' // 标记为排课
                            })
                        })
                    }
                    
                    // 处理老师日程数据（仅老师课表）
                    if (props.timetableType === CourseTimetableTypeEnum.TeacherTimetable && Array.isArray(item.ScheduleList)) {
                        item.ScheduleList.forEach((schedule: any) => {
                            processedList.push({
                                ...schedule,
                                type: 'schedule' // 标记为日程
                            })
                        })
                    }
                }
                
            })
        }
        
        // 按StartTime时间从早到晚排序
        courseList.value = processedList.sort((a, b) => {
            const timeA = new Date(a.StartTime).getTime()
            const timeB = new Date(b.StartTime).getTime()
            return timeA - timeB
        })
    } finally {
        loading.value = false
    }
}

watch(
    () => [props.modelValue, props.selectedDate, props.searchParams],
    (nv) => {
        selectedDate.value = (props.selectedDate as string) || ''
        if (props.modelValue) {
            courseList.value = [] // 优化：弹框打开时先清空，避免显示旧数据
            fetchList()
        }
    },
    { immediate: true, deep: true }
)

// 监听课程详情相关操作完成后的全局刷新事件
onMounted(() => {
    try {
        event.on('arrange-course-detail-updated', handleCourseDetailUpdated)
    } catch (e) {}
})

onUnmounted(() => {
    try {
        event.off && event.off('arrange-course-detail-updated')
    } catch (e) {}
})

function handleCourseDetailUpdated(_payload?: { refreshTotal?: boolean }) {
    // 简单起见，直接刷新当前日期的课程列表
    fetchList()
}

</script>

<style scoped lang="scss">
.drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
}

.header-date {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.drawer-body {
    padding: 0 16px 20px 16px;
    overflow: auto;
    height: 100%;
}

/* 注意：覆盖样式时使用 wtwo- 前缀，而不是 el- 前缀 */
.wtwo-drawer__body {
    padding: 0;
    background-color: #F9FAFC;
}
</style>
