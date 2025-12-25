<template>
    <div class="month-view" v-loading="loading" element-loading-target=".month-view">
        <div class="calendar-container">
            <!-- 顶部周表头 -->
            <div class="header sticky">
                <div v-for="w in weekdays" :key="w" class="header-cell">{{ w }}</div>
            </div>
            
            <!-- 可滚动内容区 -->
            <div class="grid scrollable">
                <div class="month-grid">
                    <div v-for="day in days" :key="day.dateStr" class="month-cell"
                        :class="{ 'is-other-month': !day.isCurrentMonth, 'is-today': day.isToday, 'is-past': day.isPast }">
                        <div class="cell-header">
                            <span class="date-num" :class="{ 'today-circle': day.isToday }">{{ dayjs(day.dateStr).date()}}</span>
                            <span v-if="day.isHoliday" class="holiday-flag">假</span>
                        </div>
                        <div class="cell-body">
                            <div v-for="(item, idx) in day.items.slice(0, 2)" :key="idx" 
                                 class="month-view-course-item" 
                                 :title="item.title" 
                                 :style="{ backgroundColor: getTint(getStatusColor(item), 0.1), '--course-status-color': getStatusColor(item) }"
                                 :ref="el => setCourseItemRef(el, item)"
                                 v-click-outside="(event: Event) => handleClickOutside(event, item)"
                                 @click.native="handleCourseClick($event, item)">
                                <span class="course-item-line" :style="{ backgroundColor: getStatusColor(item) }"></span>
                                <span class="course-item-time" :style="{ color: getStatusColor(item) }">{{ dayjs(item.StartTime).format('HH:mm') }}</span>
                                <span class="course-item-text ellipsis-single">{{ item.title }}</span>
                            </div>
                            <div v-if="day.items.length > 2" class="more-text" @click="openMoreDrawer(day.dateStr)">共{{ day.items.length }}节排课</div>
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
            @roll-call="handleRollCall"
            :timetable-type="CourseTimetableTypeEnum.TimeTimetable"
        />

        <!-- 更多课程抽屉 -->
        <MoreCourseListPop
            v-model="moreDrawerVisible"
            :selected-date="moreDrawerDate"
            :search-params="props.searchParams"
            :timetable-type="CourseTimetableTypeEnum.TimeTimetable"
            :preference-color-resolver="preferenceColorResolver"
        />
    </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { ClickOutside as vClickOutside } from 'element-plus'
import dayjs from 'dayjs'
import { queryCalendarMonth } from '@/api/arrange'
import { queryHoliday } from '@/api/comm'
import CourseDetailPopover from '@/components/CourseDetailPopover.vue'
import { getTint, getCourseStatusColor, buildColorDetailMap, getBackgroundColorByPreference } from '@/utils'
import MoreCourseListPop from './popup/moreCourseListPop.vue'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { CourseTimetableTypeEnum } from '@/types/model/timetable-preference'

const props = defineProps<{ month: string; searchParams?: any }>()

const weekdays = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日']
const days = ref<Array<{ dateStr: string; isCurrentMonth: boolean; isToday: boolean; isPast: boolean; isHoliday: boolean; items: any[] }>>([])
const loading = ref(false)

// 课程详情浮窗相关
const courseDetailPopoverRef = ref()
const selectedCourseData = ref<any>({})
const virtualRef = ref()
const courseItemRefs = new Map()

// 更多课程抽屉
const moreDrawerVisible = ref(false)
const moreDrawerDate = ref('')
const openMoreDrawer = (dateStr: string) => {
    moreDrawerDate.value = dateStr
    moreDrawerVisible.value = true
}

// 为抽屉提供与当前视图一致的 ByTeacher/ByCourse/ByClassroom 颜色解析
function preferenceColorResolver(
    type: 'ByTeacher'|'ByCourse'|'ByClassroom',
    ids: { TeacherID?: string|number; ShiftID?: string|number; ClassroomID?: string|number },
    timetableType?: any
): string {
    return getBackgroundColorByPreference(type, ids, timetableType || CourseTimetableTypeEnum.TimeTimetable)
}

function buildMonthDays(targetMonth: string, dataMap: Record<string, any[]>, holidaySet: Set<string>) {
    const first = dayjs(targetMonth).startOf('month')
    const weekdayIndex = (first.day() + 6) % 7 // 将周一设为0
    const start = first.subtract(weekdayIndex, 'day')
    const grid: Array<{ dateStr: string; isCurrentMonth: boolean; isToday: boolean; isPast: boolean; isHoliday: boolean; items: any[] }> = []
    for (let i = 0; i < 42; i++) {
        const d = start.add(i, 'day')
        const key = d.format('YYYY-MM-DD')
        grid.push({
            dateStr: key,
            isCurrentMonth: d.month() === first.month(),
            isToday: d.isSame(dayjs(), 'day'),
            isPast: d.isBefore(dayjs(), 'day'),
            isHoliday: holidaySet.has(key),
            items: dataMap[key] || []
        })
    }
    days.value = grid
}

const lastQueryKey = ref('')

// 偏好设置：时间课表的颜色配置
const timetablePrefStore = useTimetablePreferences()
const timeTimetablePref = computed(() => timetablePrefStore.preferenceByType(CourseTimetableTypeEnum.TimeTimetable) as any)
const enabledColorSetting = computed(() => {
    const cs = timeTimetablePref.value && Array.isArray(timeTimetablePref.value.ColorSettings)
        ? timeTimetablePref.value.ColorSettings as any[]
        : []
    if (!cs || cs.length === 0) return null
    return cs.find((s:any) => s && s.IsEnabled) || cs[0]
})
const colorDetailMap = computed(() => buildColorDetailMap((enabledColorSetting.value as any)?.ColorDetails))

// 根据偏好或完成状态返回颜色
function getStatusColor(item:any){
    return getCourseStatusColor(
        item,
        enabledColorSetting.value?.SettingType as any,
        colorDetailMap.value as any,
        'TimeTimetable'
    )
}

async function loadMonth(forceRefresh = false) {
    const startOfMonth = dayjs(props.month).startOf('month').format('YYYY-MM-DD')
    loading.value = true
    try {
        // 计算6*7网格的起止日期，用于节假日查询
        const first = dayjs(props.month).startOf('month')
        const weekdayIndex = (first.day() + 6) % 7
        const gridStart = first.subtract(weekdayIndex, 'day').format('YYYY-MM-DD')
        const gridEnd = dayjs(gridStart).add(41, 'day').format('YYYY-MM-DD')
        const mergedParams = {
            ...(props.searchParams || {}),
            PageSize: 99999,
            PageIndex: 1,
            StartDate: gridStart,
            EndDate: gridEnd,
            sort: 'StartTime',
            desc:0
        }
        const queryKey = `${startOfMonth}|${gridStart}|${gridEnd}|${JSON.stringify(mergedParams)}`
        if (!forceRefresh && queryKey === lastQueryKey.value) {
            return
        }
        lastQueryKey.value = queryKey

        const [res, holidayRes]: any = await Promise.all([
            queryCalendarMonth(mergedParams),
            queryHoliday({ sdate: gridStart, edate: gridEnd })
        ])
        const raw: any = res && (res.Data || res.data || [])
        const list: any[] = Array.isArray(raw) ? raw : raw?.Data || []
        const map: Record<string, any[]> = {}

        const ensureArray = (val: any) => Array.isArray(val) ? val : (val ? [val] : [])

        const pushItem = (dateKey: string, item: any) => {
            if (!dateKey || dateKey === 'Invalid Date') return
            if (!map[dateKey]) map[dateKey] = []
            
            // 确保title字段正确设置，优先使用ClassName，其次使用title，最后使用'-'
            const courseTitle = item.ClassName || '-'
            
            map[dateKey].push({
                ...item,
                title: courseTitle,
                color: item?.color || item?.Color,
            })
        }

        if (Array.isArray(list)) {
            list.forEach((it: any) => {
                const dk = dayjs(it.StartTime).format('YYYY-MM-DD')
                pushItem(dk, it)
            })
        } else if (list && typeof list === 'object') {
            // 兼容返回为 { '2025-09-01': [...], ... }
            Object.keys(list).forEach(key => {
                ensureArray(list[key]).forEach((it: any) => {
                    const dk = dayjs(it.StartTime || key).format('YYYY-MM-DD')
                    pushItem(dk, it)
                })
            })
        }
        const holidaySet = new Set<string>()
        const holidays: any[] = (holidayRes && (holidayRes.Data?.Data || [])) || []
        holidays.forEach((h: any) => {
            const k = dayjs(h.Date).format('YYYY-MM-DD')
            if (k && k !== 'Invalid Date') holidaySet.add(k)
        })
        buildMonthDays(props.month, map, holidaySet)
    } catch (e) {
        // 失败时也要至少渲染空网格
        buildMonthDays(props.month, {}, new Set())
    } finally {
        loading.value = false
    }
}

// 设置课程项引用
const setCourseItemRef = (el: any, item: any) => {
    if (el) {
        const itemId = item.CourseID
        courseItemRefs.set(itemId, el)
    }
}

// 处理课程点击
const handleCourseClick = async (event: Event, item: any) => {
    // 阻止事件冒泡
    event.stopPropagation()
    
    // 更新课程数据和虚拟引用
    selectedCourseData.value = item
    selectedCourseData.value.ID=item.CourseID
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
        // selectedCourseData.value={}
        courseDetailPopoverRef.value.hide()
    }
}

// 处理浮窗事件
const handleRollCall = (courseData: any) => {
    console.log('上课点名:', courseData)
    // 这里可以添加具体的业务逻辑
}

// 处理点击外部区域隐藏popover
const handleClickOutside = (event: Event, item: any) => {
    
    // 如果点击的是当前显示popover的课程项，不隐藏popover
    if (selectedCourseData.value?.CourseID === item.CourseID) {
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


onMounted(() => {
    // 初始化
})

onUnmounted(() => {
    courseItemRefs.clear()
})

watch(() => [props.month, props.searchParams], (newVal, oldVal) => {
    // 无搜索参数时不触发查询
    const hasParams = props.searchParams && Object.keys(props.searchParams || {}).length > 0
    if (!hasParams) return
    // 如果searchParams发生变化，强制刷新
    const forceRefresh = oldVal && newVal[1] !== oldVal[1]
    loadMonth(forceRefresh)
}, { deep: true, immediate: true })

defineExpose({ refresh: (forceRefresh = true) => loadMonth(forceRefresh) })
</script>

<style scoped lang="scss">
.month-view {
    width: 100%;
    min-height: 400px;
}

.calendar-container {
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: 40px 1fr;
    position: relative;
    height: 100%;
}

.header {
    grid-column: 1 / 2;
    grid-row: 1 / 2;
    top: 0;
    display: grid;
    grid-template-columns: repeat(7, minmax(128px, 1fr));
    background: #F5F7FA;
    border-bottom: 1px solid #E4E7ED;
    
    &.sticky {
        position: sticky;
        z-index: 2;
    }
}

.header-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10px 12px;
    font-weight: 600;
    color: #606266;
    border-right: 1px solid #E4E7ED;
    font-size: 14px;
    line-height: 20px;

    &:last-child {
        border-right: none;
    }
}

.grid {
    grid-column: 1 / 2;
    grid-row: 2 / 3;
    overflow: auto;
    
    // &.scrollable {
    //     max-height: calc(100vh - 200px);
    // }
}

.month-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(128px, 1fr));
    grid-auto-rows: 136px;
    gap: 0;
    border-top: none;
    border-bottom: 1px solid #E4E7ED;
    border-left: none;
    border-right: none;
    width: 100%;
    box-sizing: border-box;
}

.month-cell {
    border-right: 1px solid #E4E7ED;
    border-bottom: 1px solid #E4E7ED;
    padding: 6px;
    background-color: #fff;
    min-width: 0; /* 防止内容撑破导致列宽不等 */
    box-sizing: border-box;
}

.month-cell:nth-child(7n) {
    border-right: none;
}

.month-cell:nth-last-child(-n+7) {
    border-bottom: none;
}

.month-cell.is-other-month {
    color: #c0c4cc;
    
    .date-num {
        color: #C0C4CC;
    }
}

.month-cell.is-past {
    background-color: #fafafa;
}

.cell-header {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 8px;
    min-width: 0;
}

.date-num {
    font-weight: 600;
    font-size: 15px;
    color: #606266;
    width: 26px;
    height: 26px;
    line-height: 26px;
    text-align: center;
}

.today-circle {
    color: #fff;
    background: #2878E8;
    border-radius: 50%;
    
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.holiday-flag {
    background: #FFECE8;
    color: #F53F3F;
    width: 26px;
    height: 26px;
    line-height: 26px;
    text-align: center;
    font-size: 14px;
    border-radius:4px;
}

.cell-body {
    margin-top: 8px;
    overflow: hidden;
    min-width: 0;
}

.month-view-course-item {
    display: flex;
    align-items: center;
    font-size: 14px;
    height: 30px;
    line-height: 30px;
    color: #606266;
    border-radius: 2px;
    gap: 6px;
    width: 100%;
    box-sizing: border-box;
    cursor: pointer;
    transition: background-color 0.2s ease;
    .course-item-time{
        font-weight: 600;
        font-size: 15px;
    }
    .course-item-line {
        width: 2px;
        height: 100%;
        display: inline-block;
        border-radius: 2px 0 0 2px;
        flex-shrink: 0;
    }
    &+.month-view-course-item{
        margin-top: 5px;
    }
    &:hover {
        background-color: var(--course-status-color) !important;
        color: #fff;
        .course-item-time{
            color: #fff!important;
        }
    }
}


.more-text {
    font-size: 12px;
    line-height: 17px;
    color: var(--wtwo-color-primary, #409EFF);
    margin-top: 5px;
    cursor: pointer;
}
</style>