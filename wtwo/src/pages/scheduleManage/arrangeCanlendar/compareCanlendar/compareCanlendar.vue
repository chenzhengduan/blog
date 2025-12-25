<template>
    <el-dialog v-model="dialogVisible" :width="'100%'" :fullscreen="true" :close-on-click-modal="false"
        :append-to-body="true" :destroy-on-close="true" :show-close="false" class="compare-canlendar-dialog"
        @close="handleClose">
        <template #header="{ close, titleId, titleClass }">
            <div class="dialog-header">
                <div class="header-left flex-center">
                    <span class="campus-label">对比排课</span>
                    <pageAttentionTips class="ml-12px!">此模式下学员与老师显示在“全机构”的排课/日程
                    </pageAttentionTips>
                    <!-- 校区选择 -->
                    <!-- <span class="campus-label">按校区筛选排课/日程：</span>
                    <CampusMultiSelect v-model="selectedCampusIds" @change="handleCampusChange" /><el-tooltip
                        effect="dark" :content="transToConfigDescript('建议全选校区，下方才会展示最全的排课情况')" placement="top">
                        <el-icon size="16px" class=" campus-tip-icon">
                            <svg aria-hidden="true">
                                <use xlink:href="#w2-xinxitishi"></use>
                            </svg>
                        </el-icon>
                    </el-tooltip> -->
                </div>
                <div class="date-navigation">
                    <el-button :icon="ArrowLeft" plain @click="previousWeek" size="small" class="arrow-button" />
                    <el-date-picker v-model="currentWeekStr" type="week"
                        :format="`YYYY.MM.DD[-][${dayjs(currentWeekStr).add(6, 'day').format('YYYY.MM.DD')}]`"
                        value-format="YYYY-MM-DD" placeholder="请选择" :clearable="false" :editable="false"
                        class="date-picker-choose-box" @change="handleWeekChange">
                        <template #default="cell">
                            <div class="wtwo-date-table-cell" :class="{ current: cell.isCurrent }">
                                <span class="wtwo-date-table-cell__text">{{ cell.text }}</span>
                            </div>
                        </template>
                    </el-date-picker>

                    <el-button :icon="ArrowRight" plain @click="nextWeek" size="small" class="arrow-button" />

                    <el-button @click="goToday" type="info" class="ml-6px! today-button" color="#F3F4F4">
                        今天
                    </el-button>
                </div>
                <div class="header-right">
                    <div @click="handleRefresh" class="header-right-item header-right-icon-only">
                        <el-icon size="16px">
                            <svg aria-hidden="true">
                                <use xlink:href="#w2-a-Refreshshuaxin"></use>
                            </svg>
                        </el-icon>
                    </div>
                    <div @click="toggleFullscreen" class="header-right-item header-right-icon-only">
                        <el-icon size="16px">
                            <svg aria-hidden="true">
                                <use :xlink:href="'#w2-quanjufangda'"></use>
                            </svg>
                        </el-icon>
                    </div>

                    <!-- 🆕 视图切换按钮 -->
                    <ViewModePopover
                        v-model:visible="viewModePopoverVisible"
                        :view-by="viewBy"
                        :timetable-type="CourseTimetableTypeEnum.StudentTimetable"
                        object-name="学员"
                        @save="handleViewModeChange"
                    >
                        <template #reference>
                            <div class="header-right-item header-right-icon-only">
                                <el-icon size="16px">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-shezhi"></use>
                                    </svg>
                                </el-icon>
                            </div>
                        </template>
                    </ViewModePopover>
                    
                    <div @click="close" class="header-right-item">
                        退出
                    </div>
                </div>
            </div>
        </template>

        <!-- 主要内容区域 -->
        <div class="main-content">
            <!-- 左侧学员区域 -->
            <div class="student-section">
                <div class="section-header">
                    <div class="header-title">
                        <h3>选择学员</h3>
                        <!-- 显示选中的学员 -->
                        <div class="selected-items" v-if="selectedStudents.length > 0">
                            <div v-for="(student, index) in displayedStudents" :key="student.ID"
                                class="selected-item">
                                <span class="item-name">{{ student.Name }}</span>
                                <el-icon class="item-close" @click.stop="removeStudent(index)">
                                    <Close />
                                </el-icon>
                            </div>
                            <el-popover v-if="hiddenStudentCount > 0" placement="bottom" :width="240" trigger="hover">
                                <template #reference>
                                    <div class="more-count">+{{ hiddenStudentCount }}</div>
                                </template>
                                <div class="more-list">
                                    <div v-for="(student, index) in hiddenStudents" :key="student.ID"
                                        class="more-item-tag">
                                        <span class="item-name">{{ student.Name }}</span>
                                        <el-icon class="item-close" @click.stop="removeStudent(index + 6)">
                                            <Close />
                                        </el-icon>
                                    </div>
                                </div>
                            </el-popover>
                        </div>
                    </div>
                    <div class="header-actions">
                        <el-popover placement="bottom" :width="300" trigger="click" v-model="studentSortVisible"
                            v-if="selectedStudents.length > 0">
                            <template #reference>
                                <el-button type="text" size="small" class="sort-btn">
                                    排序
                                </el-button>
                            </template>
                            <div class="sort-panel">
                                <div class="sort-header">
                                    <span>学员列表 ({{ selectedStudents.length }})</span>
                                    <span class="sort-tip">课表将按以下排序显示</span>
                                </div>
                                <div class="sort-list" ref="studentSortListRef">
                                    <div v-for="(student, index) in selectedStudents" :key="student.ID"
                                        :data-index="index" class="sort-item" draggable="true"
                                        @dragstart="handleDragStart($event, index, 'student')"
                                        @dragover="handleDragOver($event, index)" @dragenter="handleDragEnter"
                                        @drop="handleDrop($event, index, 'student')" @dragend="handleDragEnd">
                                        <svg class="drag-handle" aria-hidden="true">
                                            <use xlink:href="#w2-paixu"></use>
                                        </svg>
                                        <div class="item-avatar">
                                            <img v-if="student.Avatar || student.Photo"
                                                :src="student.Avatar || student.Photo" :alt="student.Name" />
                                            <span v-else>{{ getNameInitial(student.Name) }}</span>
                                        </div>
                                        <div class="item-info">
                                            <div class="item-name">{{ student.Name }}</div>
                                            <div class="item-serial">{{ student.Serial }}</div>
                                        </div>
                                        <el-button @click="removeStudentFromSort(index)" type="text" class="remove-btn">
                                            <el-icon>
                                                <Close />
                                            </el-icon>
                                        </el-button>
                                    </div>
                                </div>
                            </div>
                        </el-popover>
                        <el-button v-else type="text" size="small" class="sort-btn" disabled>
                            排序
                        </el-button>
                        <div class="header-divider">|</div>
                        <el-button @click="handleAddStudent" type="text" size="small" class="add-btn">
                            选择 ({{ selectedStudents.length }}/20)
                        </el-button>
                    </div>
                </div>

                <!-- 学员列表和课表 -->
                <div class="section-content">
                    <div v-if="selectedStudents.length === 0" class="empty-state">
                        <el-empty style="padding:12px 0" :image="globalData.emptyPng"
                            :description="transToConfigDescript('暂无学员，请先添加“1对1课程”学员')" :image-size="100" />
                        <el-button @click="handleAddStudent" type="primary">
                            <i class="wtwo-icon-plus" />
                            选择学员
                        </el-button>
                    </div>
                    <div v-else class="students-container">
                        <div v-for="student in selectedStudents" :key="student.ID" class="student-item-wrapper">
                            <div class="student-item">
                                <!-- 🆕 根据 viewBy 切换不同视图 -->
                                <!-- 🆕 使用 v-show 保持组件实例，避免临时块联动失效 -->
                                <div class="timetable-container" v-show="studentsDataReady">
                                    <!-- 时刻视图 -->
                                    <CompareTimeView
                                        v-if="viewBy === 'byTime'"
                                        :timetable-type="CourseTimetableTypeEnum.StudentTimetable"
                                        :assign-list="getStudentProps(student).assignList"
                                        :current-week="currentWeek"
                                        :external-data="studentsDataMap.get(student.ID)"
                                        :holidays="weekHolidays"
                                        :refreshing="studentRefreshingMap.get(student.ID)"
                                        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
                                        @create-temporary-block="(data) => handleCreateTemporaryBlockStudent(data, student.ID)"
                                        @course-count-update="(count) => handleStudentCourseCountUpdate(student.ID, count)"
                                        @refresh-single="handleRefreshSingle"
                                        :ref="(el) => setStudentTimetableRef(el, student.ID)" />
                                    
                                    <!-- 时段/范围视图 -->
                                    <ComparePeriodView
                                        v-else-if="viewBy === 'byPeriod' || viewBy === 'byRange'"
                                        :timetable-type="CourseTimetableTypeEnum.StudentTimetable"
                                        :assign-list="getStudentProps(student).assignList"
                                        :current-week="currentWeek"
                                        :external-data="studentsDataMap.get(student.ID)"
                                        :holidays="weekHolidays"
                                        :view-by="viewBy"
                                        :refreshing="studentRefreshingMap.get(student.ID)"
                                        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
                                        @refresh-single="handleRefreshSingle"
                                        :ref="(el) => setStudentTimetableRef(el, student.ID)" />
                                </div>
                                <!-- 🆕 加载骨架屏，数据未就绪时显示 -->
                                <div class="timetable-skeleton" v-show="!studentsDataReady">
                                    <div class="skeleton-week-header">
                                        <div class="skeleton-week-day" v-for="i in 7" :key="i">
                                            <div class="skeleton-day-name"></div>
                                            <div class="skeleton-day-date"></div>
                                        </div>
                                    </div>
                                    <div class="skeleton-grid">
                                        <div class="skeleton-grid-row" v-for="i in 12" :key="i">
                                            <div class="skeleton-time-label"></div>
                                            <div class="skeleton-grid-cells">
                                                <div class="skeleton-cell" v-for="j in 7" :key="j"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 右侧老师区域 -->
            <div class="teacher-section">
                <div class="section-header">
                    <div class="header-title">
                        <h3>选择老师</h3>
                        <!-- 显示选中的老师 -->
                        <div class="selected-items" v-if="selectedTeachers.length > 0">
                            <div v-for="(teacher, index) in displayedTeachers" :key="teacher.ID"
                                class="selected-item">
                                <span class="item-name">{{ teacher.Name }}</span>
                                <el-icon class="item-close" @click.stop="removeTeacher(index)">
                                    <Close />
                                </el-icon>
                            </div>
                            <el-popover v-if="hiddenTeacherCount > 0" placement="bottom" :width="240" trigger="hover">
                                <template #reference>
                                    <div class="more-count">+{{ hiddenTeacherCount }}</div>
                                </template>
                                <div class="more-list">
                                    <div v-for="(teacher, index) in hiddenTeachers" :key="teacher.ID"
                                        class="more-item-tag">
                                        <span class="item-name">{{ teacher.Name }}</span>
                                        <el-icon class="item-close" @click.stop="removeTeacher(index + 6)">
                                            <Close />
                                        </el-icon>
                                    </div>
                                </div>
                            </el-popover>
                        </div>
                    </div>
                    <div class="header-actions">
                        <el-popover placement="bottom" :width="300" trigger="click" v-model="teacherSortVisible"
                            v-if="selectedTeachers.length > 0">
                            <template #reference>
                                <el-button type="text" size="small" class="sort-btn">
                                    排序
                                </el-button>
                            </template>
                            <div class="sort-panel">
                                <div class="sort-header">
                                    <span>{{ transToConfigDescript('老师列表') }} ({{ selectedTeachers.length }})</span>
                                    <span class="sort-tip">课表将按以下排序显示</span>
                                </div>
                                <div class="sort-list" ref="teacherSortListRef">
                                    <div v-for="(teacher, index) in selectedTeachers" :key="teacher.ID"
                                        :data-index="index" class="sort-item" draggable="true"
                                        @dragstart="handleDragStart($event, index, 'teacher')"
                                        @dragover="handleDragOver($event, index)" @dragenter="handleDragEnter"
                                        @drop="handleDrop($event, index, 'teacher')" @dragend="handleDragEnd">
                                        <svg class="drag-handle" aria-hidden="true">
                                            <use xlink:href="#w2-paixu"></use>
                                        </svg>
                                        <div class="item-avatar">
                                            <img v-if="teacher.Image || teacher.Photo"
                                                :src="teacher.Image || teacher.Photo" :alt="teacher.Name" />
                                            <span v-else>{{ getNameInitial(teacher.Name) }}</span>
                                        </div>
                                        <div class="item-info">
                                            <div class="item-name">{{ teacher.Name }}</div>
                                            <div class="item-serial">{{ teacher.Serial }}</div>
                                        </div>
                                        <el-button @click="removeTeacherFromSort(index)" type="text" class="remove-btn">
                                            <el-icon>
                                                <Close />
                                            </el-icon>
                                        </el-button>
                                    </div>
                                </div>
                            </div>
                        </el-popover>
                        <el-button v-else type="text" size="small" class="sort-btn" disabled>
                            排序
                        </el-button>
                        <div class="header-divider">|</div>
                        <el-button @click="handleAddTeacher" type="text" size="small" class="add-btn">
                            选择 ({{ selectedTeachers.length }}/20)
                        </el-button>
                    </div>
                </div>

                <!-- 老师列表和课表 -->
                <div class="section-content">
                    <div v-if="selectedTeachers.length === 0" class="empty-state">
                        <el-empty style="padding:12px 0" :image="globalData.emptyPng"
                            :description="transToConfigDescript('暂无老师，请先选择老师')" :image-size="100" />
                        <el-button @click="handleAddTeacher" type="primary">
                            <i class="wtwo-icon-plus" />
                            选择老师
                        </el-button>
                    </div>
                    <div v-else class="teachers-container">
                        <div v-for="teacher in selectedTeachers" :key="teacher.ID" class="teacher-item-wrapper">
                            <div class="teacher-item">
                                <!-- 🆕 根据 viewBy 切换不同视图 -->
                                <!-- 🆕 使用 v-show 保持组件实例，避免临时块联动失效 -->
                                <div class="timetable-container" v-show="teachersDataReady">
                                    <!-- 时刻视图 -->
                                    <CompareTimeView
                                        v-if="viewBy === 'byTime'"
                                        :timetable-type="CourseTimetableTypeEnum.TeacherTimetable"
                                        :assign-list="getTeacherProps(teacher).assignList"
                                        :current-week="currentWeek"
                                        :external-data="teachersDataMap.get(teacher.ID)"
                                        :holidays="weekHolidays"
                                        :refreshing="teacherRefreshingMap.get(teacher.ID)"
                                        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
                                        @create-temporary-block="(data) => handleCreateTemporaryBlockTeacher(data, teacher.ID)"
                                        @course-count-update="(count) => handleTeacherCourseCountUpdate(teacher.ID, count)"
                                        @refresh-single="handleRefreshSingle"
                                        :ref="(el) => setTeacherTimetableRef(el, teacher.ID)" />
                                    
                                    <!-- 时段/范围视图 -->
                                    <ComparePeriodView
                                        v-else-if="viewBy === 'byPeriod' || viewBy === 'byRange'"
                                        :timetable-type="CourseTimetableTypeEnum.TeacherTimetable"
                                        :assign-list="getTeacherProps(teacher).assignList"
                                        :current-week="currentWeek"
                                        :external-data="teachersDataMap.get(teacher.ID)"
                                        :holidays="weekHolidays"
                                        :view-by="viewBy"
                                        :refreshing="teacherRefreshingMap.get(teacher.ID)"
                                        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
                                        @refresh-single="handleRefreshSingle"
                                        :ref="(el) => setTeacherTimetableRef(el, teacher.ID)" />
                                </div>
                                <!-- 🆕 加载骨架屏，数据未就绪时显示 -->
                                <div class="timetable-skeleton" v-show="!teachersDataReady">
                                    <div class="skeleton-week-header">
                                        <div class="skeleton-week-day" v-for="i in 7" :key="i">
                                            <div class="skeleton-day-name"></div>
                                            <div class="skeleton-day-date"></div>
                                        </div>
                                    </div>
                                    <div class="skeleton-grid">
                                        <div class="skeleton-grid-row" v-for="i in 12" :key="i">
                                            <div class="skeleton-time-label"></div>
                                            <div class="skeleton-grid-cells">
                                                <div class="skeleton-cell" v-for="j in 7" :key="j"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 学员选择弹框 -->
        <ChooseStudent ref="studentSelectorRef" :multi="true" pop-title="选择学员" :only-one-to-one="true" />

        <!-- 老师选择弹框 -->
        <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>

        <!-- 学员详情浮窗 -->
        <StudentDetailPopover ref="studentPopoverRef" />
    </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import ChooseStudent from '@/components/popup/chooseStudent.vue'
import CompareTimeView from './views/CompareTimeView.vue'
import ComparePeriodView from './views/ComparePeriodView.vue'  // 🆕 导入时段视图
import ViewModePopover from '@/pages/scheduleManage/components/ViewModePopover.vue'  // 🆕 导入视图切换组件
import StudentDetailPopover from '@/pages/scheduleManage/components/studentDetailPopover.vue'
// import CampusMultiSelect from '@/components/business/select/campus-multi-select.vue'
import chooseEmpTable from '@/components/popup/chooseEmpTable.vue'
import { CourseTimetableTypeEnum } from '@/types/model/timetable-preference'
import { queryHoliday } from '@/api/comm'
import { queryCalendarStudent, queryCalendarTeacher } from '@/api/arrange'
import { getUserLabel, postUserLabel } from '@/api'  // 🆕 导入用户设置API
import { useConfigs, useCurrentCampuses } from '@/store'
import { useUserSettings } from '@/store/userSettings'  // 🆕 导入用户设置store
import { getNameInitial, buildExportColumns } from '@/utils';
import { transToConfigDescript } from '@/utils/filters/filters'
import { ALL_COLUMNS } from '@/constants/timetablePreferencesDefaults'
const configs = computed(() => {
    return useConfigs().configs
})
//权限
const NewCourse_StudentCourse = window.$xgj.op('NewCourse_StudentCourse') //给学员排课

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
// 组件属性
const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    searchParams: {
        type: Object,
        default: () => ({})
    }
})

const currentCampus = computed(() => {
    return useCurrentCampuses().campusList
})

// 🚀 导出列配置（用于API查询）
const exportColumns = computed(() => buildExportColumns(ALL_COLUMNS))

// 组件事件
const emit = defineEmits(['update:visible', 'close', 'refresh'])

// 响应式数据
const currentWeek = ref(new Date())
const currentWeekStr = ref(dayjs().startOf('week').format('YYYY-MM-DD'))
const selectedCampusIds = ref([]) // 选中的校区ID列表
const selectedStudents = ref([])
const selectedTeachers = ref([])

// 🆕 视图切换相关
const viewBy = ref('byTime')  // 默认时刻视图 'byTime' | 'byPeriod' | 'byRange'
const viewModePopoverVisible = ref(false)

// 🆕 全屏状态
const isFullscreen = ref(false)

// 🆕 保存所有设置（显示方式 + 学员教师选择）
const saveAllSettings = async () => {
    try {
        const formData = {
            viewBy: viewBy.value,
            students: selectedStudents.value.map(s => ({ ID: s.ID,CampusID:s.CampusID,CampusName:s.CampusName, Name: s.Name, Serial: s.Serial, Avatar: s.Avatar, Photo: s.Photo })),
            teachers: selectedTeachers.value.map(t => ({ ID: t.ID, Name: t.Name, Image: t.Image, Photo: t.Photo }))
        }
        await postUserLabel({
            IsPublic: 0,
            PageName: 'compareCalendar',
            Menu: '对比排课-全部设置',
            ParentPageName: '',
            Type: 5,
            FormData: JSON.stringify(formData)
        })
    } catch (error) {
        console.error('❌ 保存对比排课设置失败:', error)
    }
}

// 🆕 读取用户设置
const loadUserSettings = async () => {
    try {
        const res = await getUserLabel({
            PageName: 'compareCalendar',
            TypeList: [5]
        })
        
        // ⚠️ getUserLabel 返回的是 { Data: [...] }，找到对应 Menu 的设置
        if (res && res.Data && Array.isArray(res.Data) && res.Data.length > 0) {
            // 🔍 查找 Menu 为 '对比排课-全部设置' 的数据
            const setting = res.Data.find(item => item.Menu === '对比排课-全部设置')
            
            if (setting && setting.FormData) {
                const data = JSON.parse(setting.FormData)
                
                // 恢复显示方式
                if (data.viewBy) {
                    viewBy.value = data.viewBy
                }
                
                // 恢复学员选择
                if (data.students && Array.isArray(data.students)) {
                    selectedStudents.value = data.students
                }
                
                // 恢复教师选择
                if (data.teachers && Array.isArray(data.teachers)) {
                    selectedTeachers.value = data.teachers
                }
                
            } else {
                // console.log('📖 未找到"对比排课-全部设置"的保存数据')
            }
        } else {
            // console.log('📖 未找到保存的对比排课设置')
        }
    } catch (error) {
        console.error('❌ 读取用户设置失败:', error)
    }
}

// 🚀 优化：使用 Map 存储 ref，以 ID 为键，避免数组索引导致的重渲染
const studentTimetableRefs = ref(new Map())
const teacherTimetableRefs = ref(new Map())

// 独立存储课程数量，避免修改原始对象触发重渲染
const studentCourseCounts = ref(new Map())
const teacherCourseCounts = ref(new Map())

// 🚀 性能优化：使用 computed 缓存 slice 结果，避免每次渲染都创建新数组
const displayedStudents = computed(() => selectedStudents.value.slice(0, 6))
const hiddenStudents = computed(() => selectedStudents.value.slice(6))
const displayedTeachers = computed(() => selectedTeachers.value.slice(0, 6))
const hiddenTeachers = computed(() => selectedTeachers.value.slice(6))

// 🚀 性能优化：使用 computed 自动计算隐藏数量，确保响应式更新
const hiddenStudentCount = computed(() => Math.max(0, selectedStudents.value.length - 6))
const hiddenTeacherCount = computed(() => Math.max(0, selectedTeachers.value.length - 6))

const resetParam = {
    CategoryID: "00000000-0000-0000-0000-000000000000",
    ClassID: "00000000-0000-0000-0000-000000000000",
    ClassLabelIDList: [],
    ClassName: "",
    ClassTypeID: "00000000-0000-0000-0000-000000000000",
    ClassroomIDList: [],
    CourseFlag: "",
    CourseNumbers: 0,
    CourseType: 0,
    DialogType: 0,
    Download: 0,
    FinishType: "-1",
    Forenoon: 1,
    GradeID: "00000000-0000-0000-0000-000000000000",
    HeadMasterUserID: "00000000-0000-0000-0000-000000000000",
    IncludeFull: 0, IsContainFinished: 0, IsSubscribeCourse: -1, IsTotal: 0, MasterIDList: [],
    Nightnoon: 1, Query: "", ShiftID: "00000000-0000-0000-0000-000000000000", ShiftName: "", ShiftTypeList: [],
    ShowField: '', SubjectID: "00000000-0000-0000-0000-000000000000", TeacherIDList: [], TeacherType: -1, TermID: "00000000-0000-0000-0000-000000000000",
    TryStatus: 0, UsePlatform: 0, Weekdays: "", Year: 0, desc: 0,
}
// 🚀 创建内部响应式的 searchParams，避免直接修改 prop
const internalSearchParams = ref({
    ...props.searchParams,
    ...resetParam,
    CampusIDList:[],
})

// 监听 props.searchParams 变化，同步到内部状态
watch(() => props.searchParams, (newVal) => {
    internalSearchParams.value = {
        ...newVal,
        ...resetParam,
        CampusIDList:[],
    }
}, { deep: true, immediate: true })

// 🚀 优化：为每个学员/老师缓存 props 对象，避免重新创建导致的重渲染
const studentPropsCache = ref(new Map())
const teacherPropsCache = ref(new Map())

// 生成稳定的学员 props
const getStudentProps = (student) => {
    const studentId = student.ID
    if (!studentPropsCache.value.has(studentId)) {
        studentPropsCache.value.set(studentId, {
            assignList: [student],
            searchParams: { ...internalSearchParams.value, StudentID: studentId }
        })
    }
    return studentPropsCache.value.get(studentId)
}

// 生成稳定的老师 props
const getTeacherProps = (teacher) => {
    const teacherId = teacher.ID
    if (!teacherPropsCache.value.has(teacherId)) {
        teacherPropsCache.value.set(teacherId, {
            assignList: [teacher],
            searchParams: { ...internalSearchParams.value }
        })
    }
    return teacherPropsCache.value.get(teacherId)
}

// 🚀 批量加载优化：数据存储
const studentsDataMap = ref(new Map())
const teachersDataMap = ref(new Map())
const batchLoadingStudents = ref(false)
const batchLoadingTeachers = ref(false)

// 🆕 单个刷新的 loading 状态（按 ID 存储）
const studentRefreshingMap = ref(new Map())
const teacherRefreshingMap = ref(new Map())

// 🆕 控制是否允许自动加载数据的标志
const allowAutoLoad = ref(true)

// 🆕 数据是否已就绪的标志
const studentsDataReady = ref(false)
const teachersDataReady = ref(false)

// 🚀 批量加载学员数据（1次API调用获取所有学员数据）
const loadAllStudentsData = async (showSkeleton = false) => {
    if (selectedStudents.value.length === 0) {
        studentsDataMap.value.clear()
        studentsDataReady.value = true  // 🆕 即使没有数据，也标记为就绪
        return
    }

    batchLoadingStudents.value = true
    
    // 🆕 如果需要显示骨架屏（刷新、选择学员等场景），则设置为 false
    if (showSkeleton) {
        studentsDataReady.value = false
    }
    
    try {
        const startDate = dayjs(currentWeek.value).startOf('week').format('YYYY-MM-DD')
        const endDate = dayjs(currentWeek.value).endOf('week').format('YYYY-MM-DD')

        const res = await queryCalendarStudent({
            ...internalSearchParams.value,
            IDList: selectedStudents.value.map(s => s.ID),
            StartDate: startDate,
            EndDate: endDate,
            PageSize: 99999,
            PageIndex: 1,
            sort: 'StartTime',
            desc: 0,
            ExportColumn: exportColumns.value
        })
        // 🆕 先准备新数据，再一次性更新，减少闪烁
        const newDataMap = new Map()

        // 按 StudentID 分组数据（res.Data 是学员数组，每个学员有 CourseList）
        const dataList = res?.Data || []
        if (Array.isArray(dataList)) {
            dataList.forEach(item => {
                const studentId = item.StudentID
                if (studentId) {
                    // 直接存储整个学员对象，包含 CourseList
                    newDataMap.set(studentId, item)
                }
            })
        }
        
        // ✅ 修复：为所有选中的学员设置数据，即使没有返回数据也设为空
        // 这样可以确保界面正确显示"无数据"状态，而不是保留旧数据
        selectedStudents.value.forEach(student => {
            if (!newDataMap.has(student.ID)) {
                newDataMap.set(student.ID, {
                    StudentID: student.ID,
                    CourseList: [],
                    ScheduleList: []
                })
            }
        })

        // 🆕 一次性更新数据
        studentsDataMap.value = newDataMap
        studentsDataReady.value = true  // 🆕 加载完成后标记为就绪
    } catch (error) {
        console.error('❌ 批量加载学员数据失败:', error)
        ElMessage.error('加载学员数据失败')
        studentsDataReady.value = true  // 🆕 即使失败，也标记为就绪，避免卡住
    } finally {
        batchLoadingStudents.value = false
    }
}

// 🚀 批量加载老师数据（1次API调用获取所有老师数据）
const loadAllTeachersData = async (showSkeleton = false) => {
    if (selectedTeachers.value.length === 0) {
        teachersDataMap.value.clear()
        teachersDataReady.value = true  // 🆕 即使没有数据，也标记为就绪
        return
    }

    batchLoadingTeachers.value = true
    
    // 🆕 如果需要显示骨架屏（刷新、添加老师等场景），则设置为 false
    if (showSkeleton) {
        teachersDataReady.value = false
    }
    
    try {
        const startDate = dayjs(currentWeek.value).startOf('week').format('YYYY-MM-DD')
        const endDate = dayjs(currentWeek.value).endOf('week').format('YYYY-MM-DD')

        const res = await queryCalendarTeacher({
            ...internalSearchParams.value,
            IDList: selectedTeachers.value.map(t => t.ID),
            StartDate: startDate,
            EndDate: endDate,
            PageSize: 99999,
            PageIndex: 1,
            sort: 'StartTime',
            desc: 0,
            ExportColumn: exportColumns.value
        })

        // 🆕 先准备新数据，再一次性更新，减少闪烁
        const newDataMap = new Map()

        // 按 TeacherID 分组数据（res.Data 是老师数组，每个老师有 CourseList）
        const dataList = res?.Data || []
        if (Array.isArray(dataList)) {
            dataList.forEach(item => {
                const teacherId = item.TeacherID
                if (teacherId) {
                    // 直接存储整个老师对象，包含 CourseList
                    newDataMap.set(teacherId, item)
                }
            })
        }
        
        // ✅ 修复：为所有选中的老师设置数据，即使没有返回数据也设为空
        // 这样可以确保界面正确显示"无数据"状态，而不是保留旧数据
        selectedTeachers.value.forEach(teacher => {
            if (!newDataMap.has(teacher.ID)) {
                newDataMap.set(teacher.ID, {
                    TeacherID: teacher.ID,
                    CourseList: [],
                    ScheduleList: []
                })
            }
        })

        // 🆕 一次性更新数据
        teachersDataMap.value = newDataMap
        teachersDataReady.value = true  // 🆕 加载完成后标记为就绪
    } catch (error) {
        console.error('❌ 批量加载老师数据失败:', error)
        ElMessage.error('加载老师数据失败')
        teachersDataReady.value = true  // 🆕 即使失败，也标记为就绪，避免卡住
    } finally {
        batchLoadingTeachers.value = false
    }
}

// 弹框控制
const dialogVisible = ref(false)
const studentSelectorRef = ref(null)
const chooseEmpTableRef = ref(null)
const studentSortVisible = ref(false)
const teacherSortVisible = ref(false)
const studentSortListRef = ref(null)
const teacherSortListRef = ref(null)

// 计算单行显示时隐藏的项数
const calculateHiddenCount = () => {
    // 已使用 computed 自动计算，此函数保留用于兼容旧代码调用
}

// 拖拽相关 - 使用ref对象统一管理状态
const dragState = ref({
    draggedIndex: -1,
    draggedType: '',
    draggedOverIndex: -1,
    isDragging: false,
    tempStudents: [],
    tempTeachers: []
})

// 拖拽防抖定时器
let dragOverTimer = null
// 当前周节假日
const weekHolidays = ref([])


// 格式化日期范围显示（保留用于调试）
const formatDateRange = (date) => {
    const monday = getMonday(date)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)

    return `${dayjs(monday).format('YYYY.MM.DD')}-${dayjs(sunday).format('MM.DD')}`
}

// 获取周一日期
const getMonday = (date) => {
    const d = new Date(date)
    const day = d.getDay()
    const diff = d.getDate() - day + (day === 0 ? -6 : 1)
    return new Date(d.setDate(diff))
}

// 日期导航方法
const previousWeek = () => {
    const newDate = dayjs(currentWeekStr.value).subtract(7, 'day')
    currentWeekStr.value = newDate.format('YYYY-MM-DD')
    currentWeek.value = newDate.toDate()
    // fetchWeekHolidays() 已移到 watch([currentWeek, currentWeekStr]) 中
}

const nextWeek = () => {
    const newDate = dayjs(currentWeekStr.value).add(7, 'day')
    currentWeekStr.value = newDate.format('YYYY-MM-DD')
    currentWeek.value = newDate.toDate()
    // fetchWeekHolidays() 已移到 watch([currentWeek, currentWeekStr]) 中
}

const goToday = () => {
    const today = dayjs().startOf('week')
    const todayStr = today.format('YYYY-MM-DD')

    // 如果已经是当前周，不需要重新加载
    if (currentWeekStr.value === todayStr) {
        ElMessage.info('当前已经是本周了')
        return
    }

    currentWeekStr.value = todayStr
    currentWeek.value = today.toDate()
    // fetchWeekHolidays() 已移到 watch([currentWeek, currentWeekStr]) 中
}

// 处理周选择器变化
const handleWeekChange = (value) => {
    currentWeekStr.value = value
    currentWeek.value = dayjs(value).toDate()
    // fetchWeekHolidays() 已移到 watch([currentWeek, currentWeekStr]) 中
}

// 处理校区选择变化
// const handleCampusChange = async (selectedCampuses) => {
//     console.log('选中的校区:', selectedCampuses)

//     // 更新内部 searchParams 中的 CampusIDList
//     const campusIds = selectedCampuses.map(campus => campus.ID)
//     internalSearchParams.value = {
//         ...internalSearchParams.value,
//         CampusIDList: campusIds
//     }

//     // 🚀 校区变化时重新批量加载数据，显示骨架屏
//     await Promise.all([
//         loadAllStudentsData(true),
//         loadAllTeachersData(true)
//     ])
// }

const handleCreateTemporaryBlockStudent = (data, sourceId) => {
    // 学员触发：
    // 1) 先清空所有其他学员的 temporaryBlock
    studentTimetableRefs.value.forEach((refItem, id) => {
        if (refItem && typeof refItem.clearTemporaryBlock === 'function') {
            if (id !== sourceId) refItem.clearTemporaryBlock()
        }
    })
    // 2) 给所有老师设置相同的 temporaryBlock
    teacherTimetableRefs.value.forEach((refItem) => {
        if (refItem && typeof refItem.setTemporaryBlock === 'function') {
            refItem.setTemporaryBlock(data)
        }
    })
}

const handleCreateTemporaryBlockTeacher = (data, sourceId) => {
    // 老师触发：
    // 1) 先清空所有其他老师的 temporaryBlock
    teacherTimetableRefs.value.forEach((refItem, id) => {
        if (refItem && typeof refItem.clearTemporaryBlock === 'function') {
            if (id !== sourceId) refItem.clearTemporaryBlock()
        }
    })
    // 2) 给所有学员设置相同的 temporaryBlock
    studentTimetableRefs.value.forEach((refItem) => {
        if (refItem && typeof refItem.setTemporaryBlock === 'function') {
            refItem.setTemporaryBlock(data)
        }
    })
}

// 🚀 优化：绑定子组件 ref，使用 ID 作为键而不是索引，避免重渲染
function setStudentTimetableRef(el, studentId) {
    if (el) {
        studentTimetableRefs.value.set(studentId, el)
    } else {
        // 组件卸载时清理 ref
        studentTimetableRefs.value.delete(studentId)
    }
}
function setTeacherTimetableRef(el, teacherId) {
    if (el) {
        teacherTimetableRefs.value.set(teacherId, el)
    } else {
        // 组件卸载时清理 ref
        teacherTimetableRefs.value.delete(teacherId)
    }
}
//配置项
const ShowAllStudentsWhenCoursePlan = computed(() => { //一对一排课时，是否可以跨校区选择学员：1是（默认），0否（只能选择当前校区的学员）。
    return configs.value.ShowAllStudentsWhenCoursePlan == 1
})
// 学员相关方法
const handleAddStudent = () => {
    studentSelectorRef.value?.open({
        multi: true, // 多选模式
        popTitle: '选择学员',
        required: false,
        showOneToOneStudent: true,
        disabledOneToOneStudent: true,
        choosed: selectedStudents.value, // 添加回显数据
        condition: {
            status: "1", // 在读学员
            campusid: '',
            IsOnlyShowOneToOneStudent: 1
        }
    }).then((res) => {
        if (res && res.data) {
            console.log('选择学员返回数据:', res.data)

            // 处理返回的学员数据
            let students = []
            if (Array.isArray(res.data)) {
                students = res.data
            } else {
                students = [res.data]
            }

            // 🆕 覆盖模式：直接替换，检查数量限制（最多20个）
            if (students.length > 20) {
                ElMessage.warning(`最多只能选择20个学员，已为您自动选择前20个`)
                selectedStudents.value = students.slice(0, 20)
            } else {
                selectedStudents.value = students
            }

            console.log('当前选择的学员:', selectedStudents.value)
            calculateHiddenCount()
            
            // 🆕 保存所有设置
            saveAllSettings()
            
            // ⚠️ 不需要手动调用 loadAllStudentsData()
            // 因为 watch(() => selectedStudents.value) 会自动触发加载
        }
    }).catch(() => {
        console.log('取消选择学员')
    })
}



const removeStudent = (index) => {
    const student = selectedStudents.value[index]
    // 🚀 优化：移除时清理对应的 ref 和 props 缓存，避免内存泄漏
    if (student && student.ID) {
        studentTimetableRefs.value.delete(student.ID)
        studentPropsCache.value.delete(student.ID)
        studentsDataMap.value.delete(student.ID)  // 🆕 也删除数据缓存
    }
    selectedStudents.value.splice(index, 1)
    calculateHiddenCount()
    
    // 🆕 保存所有设置
    saveAllSettings()
}

const EnbaleEmpIsClassTeacher = computed(() => {
    return configs.value.EnbaleEmpIsClassTeacher
})
// 老师相关方法
const handleAddTeacher = () => {
    let disabledCondition = ['StatusList']
    if (EnbaleEmpIsClassTeacher.value == 1) {
        disabledCondition.push('IsClassTeacherList')
    }
    chooseEmpTableRef.value?.open({
        popTitle: '选择老师',
        multi: true, // 多选
        choosed: selectedTeachers.value, // 添加回显数据
        condition: {
            IsClassTeacherList: [1], // 只显示任课老师
            StatusList: [1]
        }
    }).then((result) => {
        if (result && result.data) {
            console.log('选择老师返回数据:', result.data)

            // 处理返回的老师数据
            let teachers = []
            if (Array.isArray(result.data)) {
                teachers = result.data
            } else {
                teachers = [result.data]
            }

            // 🆕 覆盖模式：直接替换，检查数量限制（最多20个）
            if (teachers.length > 20) {
                ElMessage.warning(`最多只能选择20个${transToConfigDescript('老师')}，已为您自动选择前20个`)
                selectedTeachers.value = teachers.slice(0, 20)
            } else {
                selectedTeachers.value = teachers
            }

            console.log('当前选择的老师:', selectedTeachers.value)
            calculateHiddenCount()
            
            // 🆕 保存所有设置
            saveAllSettings()
            
            // ⚠️ 不需要手动调用 loadAllTeachersData()
            // 因为 watch(() => selectedTeachers.value) 会自动触发加载
        }
    }).catch(() => {
        console.log('取消选择老师')
    })
}


// 学员详情浮窗
const studentPopoverRef = ref()
// 打开学员浮窗（学员视图侧边"更多"按钮）
async function openStudentPopover(evt, index) {
    const student = selectedStudents.value[index]
    const target = evt.currentTarget

    // 直接传递学员对象，避免修改响应式变量
    const studentData = {
        ID: student.ID,
        Name: student.Name,
        Photo: student.Avatar,
        CampusID: student.CampusID,
    }

    await nextTick()

    // 直接调用组件的 show 方法，并传递学员数据
    if (studentPopoverRef.value) {
        studentPopoverRef.value.showWithStudent(studentData, target)
    }
}
// 处理点击外部区域隐藏popover
const handleStudentClickOutside = (event) => {
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


const removeTeacher = (index) => {
    const teacher = selectedTeachers.value[index]
    // 🚀 优化：移除时清理对应的 ref 和 props 缓存，避免内存泄漏
    if (teacher && teacher.ID) {
        teacherTimetableRefs.value.delete(teacher.ID)
        teacherPropsCache.value.delete(teacher.ID)
        teachersDataMap.value.delete(teacher.ID)  // 🆕 也删除数据缓存
    }
    selectedTeachers.value.splice(index, 1)
    calculateHiddenCount()
    
    // 🆕 保存所有设置
    saveAllSettings()
}

// 刷新数据
const handleRefresh = async () => {
    // 🆕 清除时段范围配置缓存
    const userSettingsStore = useUserSettings()
    userSettingsStore.clearUserSettings('TeacherTimeRange')
    
    // 🆕 如果是范围视图，预加载时段配置到缓存（只调用一次接口）
    // 这样后续所有子组件都从缓存读取，不会重复调用接口
    if (viewBy.value === 'byRange') {
        await userSettingsStore.fetchUserSettings('TeacherTimeRange', '老师课表时段范围', 1, 1, false)
    }
    
    // 🆕 触发子组件 watch 重新加载配置（从缓存读取）
    // 通过创建新的 Date 对象来改变引用，触发所有子组件的 watch
    const temp = new Date(currentWeek.value)
    currentWeek.value = new Date(temp.getTime())
    
    // 🚀 使用批量加载刷新数据，显示骨架屏
    // ⚠️ loadAllStudentsData 和 loadAllTeachersData 内部会更新数据
    // 子组件通过 watch studentsDataMap/teachersDataMap 自动刷新，无需改变 currentWeek
    // await Promise.all([
    //     loadAllStudentsData(true),
    //     loadAllTeachersData(true)
    // ])
    ElMessage.success('刷新成功')
}

// 🆕 刷新单个对象的数据
const handleRefreshSingle = async ({ object, timetableType }) => {
    // 设置单个对象的 loading 状态
    if (timetableType === CourseTimetableTypeEnum.StudentTimetable) {
        studentRefreshingMap.value.set(object.ID, true)
    } else if (timetableType === CourseTimetableTypeEnum.TeacherTimetable) {
        teacherRefreshingMap.value.set(object.ID, true)
    }
    
    try {
        // 预加载时段配置（如果是范围视图）
        if (viewBy.value === 'byRange') {
            const userSettingsStore = useUserSettings()
            await userSettingsStore.fetchUserSettings('TeacherTimeRange', '老师课表时段范围', 1, 1, false)
        }
        
        // 根据课表类型刷新对应的数据
        if (timetableType === CourseTimetableTypeEnum.StudentTimetable) {
            // 刷新学员数据
            await loadStudentData(object.ID)
            ElMessage.success(`已刷新 ${object.Name} 的数据`)
        } else if (timetableType === CourseTimetableTypeEnum.TeacherTimetable) {
            // 刷新老师数据
            await loadTeacherData(object.ID)
            ElMessage.success(`已刷新 ${object.Name} 的数据`)
        }
    } finally {
        // 清除 loading 状态
        if (timetableType === CourseTimetableTypeEnum.StudentTimetable) {
            studentRefreshingMap.value.delete(object.ID)
        } else if (timetableType === CourseTimetableTypeEnum.TeacherTimetable) {
            teacherRefreshingMap.value.delete(object.ID)
        }
    }
}

// 🆕 加载单个学员数据
const loadStudentData = async (studentId) => {
    const startDate = dayjs(currentWeek.value).startOf('week').format('YYYY-MM-DD')
    const endDate = dayjs(currentWeek.value).endOf('week').format('YYYY-MM-DD')
    
    try {
        const res = await queryCalendarStudent({
            ...internalSearchParams.value,
            IDList: [studentId],
            StartDate: startDate,
            EndDate: endDate,
            PageSize: 99999,
            PageIndex: 1,
            sort: 'StartTime',
            desc: 0,
            ExportColumn: exportColumns.value
        })
        
        // 更新单个学员的数据
        const dataList = res?.Data || []
        if (Array.isArray(dataList) && dataList.length > 0) {
            const studentData = dataList[0]
            if (studentData && studentData.StudentID) {
                studentsDataMap.value.set(studentData.StudentID, studentData)
            }
        } else {
            // ✅ 修复：当返回空数组时，也要更新 studentsDataMap（设为空数据）
            // 这样子组件才能正确清空课块
            studentsDataMap.value.set(studentId, {
                StudentID: studentId,
                CourseList: [],
                ScheduleList: []
            })
        }
    } catch (error) {
        console.error('❌ 加载学员数据失败:', error)
        ElMessage.error('刷新学员数据失败')
    }
}

// 🆕 加载单个老师数据
const loadTeacherData = async (teacherId) => {
    const startDate = dayjs(currentWeek.value).startOf('week').format('YYYY-MM-DD')
    const endDate = dayjs(currentWeek.value).endOf('week').format('YYYY-MM-DD')
    
    try {
        const res = await queryCalendarTeacher({
            ...internalSearchParams.value,
            IDList: [teacherId],
            StartDate: startDate,
            EndDate: endDate,
            PageSize: 99999,
            PageIndex: 1,
            sort: 'StartTime',
            desc: 0,
            ExportColumn: exportColumns.value
        })
        
        // 更新单个老师的数据
        const dataList = res?.Data || []
        if (Array.isArray(dataList) && dataList.length > 0) {
            const teacherData = dataList[0]
            if (teacherData && teacherData.TeacherID) {
                teachersDataMap.value.set(teacherData.TeacherID, teacherData)
            }
        } else {
            // ✅ 修复：当返回空数组时，也要更新 teachersDataMap（设为空数据）
            // 这样子组件才能正确清空课块
            teachersDataMap.value.set(teacherId, {
                TeacherID: teacherId,
                CourseList: [],
                ScheduleList: []
            })
        }
    } catch (error) {
        console.error('❌ 加载老师数据失败:', error)
        ElMessage.error('刷新老师数据失败')
    }
}

// 🆕 切换全屏
const toggleFullscreen = async () => {
    try {
        if (!isFullscreen.value) {
            // 进入全屏
            const elem = document.documentElement
            if (elem.requestFullscreen) {
                await elem.requestFullscreen()
            } else if (elem.webkitRequestFullscreen) {
                await elem.webkitRequestFullscreen()
            } else if (elem.mozRequestFullScreen) {
                await elem.mozRequestFullScreen()
            } else if (elem.msRequestFullscreen) {
                await elem.msRequestFullscreen()
            }
            isFullscreen.value = true
        } else {
            // 退出全屏
            if (document.exitFullscreen) {
                await document.exitFullscreen()
            } else if (document.webkitExitFullscreen) {
                await document.webkitExitFullscreen()
            } else if (document.mozCancelFullScreen) {
                await document.mozCancelFullScreen()
            } else if (document.msExitFullscreen) {
                await document.msExitFullscreen()
            }
            isFullscreen.value = false
        }
    } catch (error) {
        console.warn('全屏切换失败:', error)
    }
}

// 🆕 处理视图切换
const handleViewModeChange = (newViewBy) => {
    viewBy.value = newViewBy
    viewModePopoverVisible.value = false
    console.log('视图切换为:', newViewBy)
    // 🆕 保存所有设置
    saveAllSettings()
}

// 关闭弹框
const handleClose = async () => {
    // 🆕 如果处于全屏模式，先退出全屏
    if (isFullscreen.value) {
        try {
            if (document.exitFullscreen) {
                await document.exitFullscreen()
            } else if (document.webkitExitFullscreen) {
                await document.webkitExitFullscreen()
            } else if (document.mozCancelFullScreen) {
                await document.mozCancelFullScreen()
            } else if (document.msExitFullscreen) {
                await document.msExitFullscreen()
            }
            isFullscreen.value = false
        } catch (error) {
            console.warn('退出全屏失败:', error)
        }
    }
    
    emit('update:visible', false)
    emit('close')
}

// 处理课表双击新增排课事件，交给父组件去搞
const handleCanlendarDbclickAddCourse = (data) => {
    if (!NewCourse_StudentCourse) {
        ElMessage.warning('暂无“给学员排课”的权限，请联系管理员！')
        return
    }
    emit('canlendar-dbclick-add-course', data)
}

// 监听周变化
watch([currentWeek, currentWeekStr], () => {
    if (!allowAutoLoad.value) return
    
    // 🚀 周次变化时重新加载节假日和数据，显示骨架屏
    fetchWeekHolidays()
    
    if (selectedStudents.value.length > 0) {
        loadAllStudentsData(true)
    }
    if (selectedTeachers.value.length > 0) {
        loadAllTeachersData(true)
    }
}, { deep: true })

// 🚀 监听学员列表变化，触发批量加载
watch(() => selectedStudents.value, () => {
    if (allowAutoLoad.value) {
        loadAllStudentsData(true)  // 添加/删除学员时显示骨架屏
    }
}, { deep: true })

// 🚀 监听老师列表变化，触发批量加载
watch(() => selectedTeachers.value, () => {
    if (allowAutoLoad.value) {
        loadAllTeachersData(true)  // 添加/删除老师时显示骨架屏
    }
}, { deep: true })

// 监听visible变化
watch(() => props.visible, async (newVal) => {
    if (newVal) {
        // 🚀 第一步：立即打开弹框，显示空状态
        dialogVisible.value = true
        
        // 🚀 第二步：清空旧数据，避免渲染大量旧组件
        allowAutoLoad.value = false
        selectedStudents.value = []
        selectedTeachers.value = []
        studentsDataReady.value = false
        teachersDataReady.value = false
        
        // 第三步：重置基础数据
        const today = dayjs().startOf('week')
        currentWeekStr.value = today.format('YYYY-MM-DD')
        currentWeek.value = today.toDate()

        // 从 searchParams 中初始化选中的校区
        // if (props.searchParams.CampusIDList && Array.isArray(props.searchParams.CampusIDList)) {
        //     selectedCampusIds.value = [...props.searchParams.CampusIDList]
        // } else {
        //     selectedCampusIds.value = []
        // }
        
        // 🚀 第四步：使用 setTimeout 让弹框先渲染出来，再异步加载数据
        setTimeout(async () => {
            try {
                // 获取节假日
                await fetchWeekHolidays()
                
                // 🆕 加载用户保存的设置（这会恢复学员/老师选择）
                await loadUserSettings()
                
                // 设置恢复完成后，等待 DOM 更新
                await nextTick()
                
                // 分批加载数据，避免同时加载导致卡顿
                if (selectedStudents.value.length > 0) {
                    loadAllStudentsData()  // 不 await，让它异步执行
                }
                if (selectedTeachers.value.length > 0) {
                    // 延迟一点加载老师数据，错开加载时机
                    setTimeout(() => {
                        loadAllTeachersData()
                    }, 100)
                }
            } finally {
                // 重新启用自动加载
                allowAutoLoad.value = true
            }
        }, 50)  // 延迟 50ms，让弹框先完成渲染
    } else {
        dialogVisible.value = false
    }
})

// 监听dialogVisible变化，同步到父组件
watch(dialogVisible, (newVal) => {
    if (!newVal) {
        emit("refresh")
    }
    if (newVal !== props.visible) {
        emit('update:visible', newVal)
    }
})

// 拖拽排序方法 - 优化版本
const handleDragStart = (event, index, type) => {
    dragState.value.draggedIndex = index
    dragState.value.draggedType = type
    dragState.value.isDragging = true
    event.dataTransfer.effectAllowed = 'move'

    // 创建自定义拖拽镜像效果
    const target = event.target.closest('.sort-item')
    if (target) {
        target.classList.add('dragging')

        // 创建自定义拖拽预览
        const dragImage = document.createElement('div')
        dragImage.className = 'drag-ghost'
        dragImage.style.cssText = `
            position: absolute;
            top: -9999px;
            left: -9999px;
            width: ${target.offsetWidth}px;
            padding: 8px;
            background: #fff;
            border-radius: 4px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            opacity: 0.9;
            display: flex;
            align-items: center;
            gap: 12px;
            pointer-events: none;
        `

        // 只显示姓名，不显示其他内容
        const list = type === 'student' ? selectedStudents.value : selectedTeachers.value
        const item = list[index]
        const avatarUrl = item.Avatar || item.Image || item.Photo
        dragImage.innerHTML = `
            <svg style="width: 14px; height: 14px; color: #c0c4cc;" aria-hidden="true">
                <use xlink:href="#w2-paixu"></use>
            </svg>
            <div style="
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
            ">
                ${avatarUrl ?
                `<img src="${avatarUrl}" alt="${item.Name}" style="width: 100%; height: 100%; object-fit: cover;" />` :
                `<span style="color: white;">${getNameInitial(item.Name)}</span>`
            }
            </div>
            <div style="font-size: 14px; font-weight: 500; color: #303133;">${item.Name}</div>
        `

        document.body.appendChild(dragImage)
        event.dataTransfer.setDragImage(dragImage, event.offsetX, event.offsetY)

        // 清理临时元素
        setTimeout(() => {
            if (document.body.contains(dragImage)) {
                document.body.removeChild(dragImage)
            }
        }, 0)
    }
}

const handleDragOver = (event, targetIndex) => {
    event.preventDefault()
    event.dataTransfer.dropEffect = 'move'

    if (!dragState.value.isDragging) return

    const { draggedIndex, draggedType } = dragState.value

    // 如果是同一个位置，不做处理
    if (draggedIndex === targetIndex) return

    const target = event.target.closest('.sort-item')
    if (!target || target.classList.contains('dragging')) return

    // 更新悬停索引
    if (dragState.value.draggedOverIndex !== targetIndex) {
        dragState.value.draggedOverIndex = targetIndex

        // 使用CSS Transform实现平滑移动 - 不触发重渲染
        const listRef = draggedType === 'student' ? studentSortListRef.value : teacherSortListRef.value
        if (!listRef) return

        const items = Array.from(listRef.querySelectorAll('.sort-item'))
        const draggedElement = items[draggedIndex]
        const targetElement = items[targetIndex]

        if (draggedElement && targetElement) {
            const draggedHeight = draggedElement.offsetHeight + 4 // 加上margin和padding
            const isMovingDown = draggedIndex < targetIndex

            items.forEach((item, idx) => {
                if (idx === draggedIndex) {
                    // 被拖拽的元素移动到目标位置
                    const offset = (targetIndex - draggedIndex) * draggedHeight
                    item.style.transform = `translateY(${offset}px)`
                    item.style.transition = 'transform 0.2s ease'
                } else if (isMovingDown && idx > draggedIndex && idx <= targetIndex) {
                    // 向下拖拽：中间的元素向上移动
                    item.style.transform = `translateY(-${draggedHeight}px)`
                    item.style.transition = 'transform 0.2s ease'
                } else if (!isMovingDown && idx < draggedIndex && idx >= targetIndex) {
                    // 向上拖拽：中间的元素向下移动
                    item.style.transform = `translateY(${draggedHeight}px)`
                    item.style.transition = 'transform 0.2s ease'
                } else {
                    // 其他元素保持原位
                    item.style.transform = ''
                    item.style.transition = 'transform 0.2s ease'
                }
            })
        }
    }
}

const handleDragEnter = (event) => {
    event.preventDefault()
}

const handleDrop = (event, targetIndex, type) => {
    event.preventDefault()

    const { draggedIndex, draggedType, draggedOverIndex } = dragState.value

    // 使用最后记录的悬停索引作为目标位置
    const finalTargetIndex = draggedOverIndex !== -1 ? draggedOverIndex : targetIndex

    if (draggedIndex === -1 || draggedIndex === finalTargetIndex) {
        clearDragStyles()
        resetDragState()
        return
    }

    // 更新实际数据顺序
    if (draggedType === 'student') {
        const newStudents = [...selectedStudents.value]
        const [draggedItem] = newStudents.splice(draggedIndex, 1)
        newStudents.splice(finalTargetIndex, 0, draggedItem)
        selectedStudents.value = newStudents
    } else if (draggedType === 'teacher') {
        const newTeachers = [...selectedTeachers.value]
        const [draggedItem] = newTeachers.splice(draggedIndex, 1)
        newTeachers.splice(finalTargetIndex, 0, draggedItem)
        selectedTeachers.value = newTeachers
    }

    // 清理拖拽状态
    nextTick(() => {
        clearDragStyles()
        resetDragState()
        calculateHiddenCount()
    })
}

const handleDragEnd = (event) => {
    // 拖拽结束，清理所有样式和状态
    clearDragStyles()
    resetDragState()
}

// 清理拖拽样式的辅助函数
const clearDragStyles = () => {
    document.querySelectorAll('.sort-item').forEach(item => {
        item.classList.remove('dragging')
        item.style.transform = ''
        item.style.transition = ''
    })
}

// 重置拖拽状态的辅助函数
const resetDragState = () => {
    dragState.value = {
        draggedIndex: -1,
        draggedType: '',
        draggedOverIndex: -1,
        isDragging: false,
        tempStudents: [],
        tempTeachers: []
    }

    // 清理防抖定时器
    if (dragOverTimer) {
        clearTimeout(dragOverTimer)
        dragOverTimer = null
    }
}

// 从排序面板移除学员
const removeStudentFromSort = (index) => {
    const student = selectedStudents.value[index]
    // 🚀 优化：移除时清理对应的 ref 和 props 缓存
    if (student && student.ID) {
        studentTimetableRefs.value.delete(student.ID)
        studentPropsCache.value.delete(student.ID)
    }
    selectedStudents.value.splice(index, 1)
    if (selectedStudents.value.length === 0) {
        studentSortVisible.value = false
    }
    calculateHiddenCount()
}

// 处理学员课程数量更新
const handleStudentCourseCountUpdate = (studentId, count) => {
    studentCourseCounts.value.set(studentId, count)
}

// 处理老师课程数量更新
const handleTeacherCourseCountUpdate = (teacherId, count) => {
    teacherCourseCounts.value.set(teacherId, count)
}

// 从排序面板移除老师
const removeTeacherFromSort = (index) => {
    const teacher = selectedTeachers.value[index]
    // 🚀 优化：移除时清理对应的 ref 和 props 缓存
    if (teacher && teacher.ID) {
        teacherTimetableRefs.value.delete(teacher.ID)
        teacherPropsCache.value.delete(teacher.ID)
    }
    selectedTeachers.value.splice(index, 1)
    if (selectedTeachers.value.length === 0) {
        teacherSortVisible.value = false
    }
    calculateHiddenCount()
}

// 组件生命周期（保留用于其他可能的初始化）
onMounted(() => {
    // 组件挂载时的初始化逻辑
    calculateHiddenCount()

    // 从 searchParams 中初始化选中的校区
    // if (props.searchParams.CampusIDList && Array.isArray(props.searchParams.CampusIDList)) {
    //     selectedCampusIds.value = [...props.searchParams.CampusIDList]
    // }
    
    // 🆕 监听全屏状态变化（用户按 ESC 或 F11 退出全屏时同步状态）
    const handleFullscreenChange = () => {
        isFullscreen.value = !!(
            document.fullscreenElement ||
            document.webkitFullscreenElement ||
            document.mozFullScreenElement ||
            document.msFullscreenElement
        )
    }
    document.addEventListener('fullscreenchange', handleFullscreenChange)
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
    document.addEventListener('mozfullscreenchange', handleFullscreenChange)
    document.addEventListener('MSFullscreenChange', handleFullscreenChange)
    
    // ⚠️ 注意：所有数据加载逻辑都在 watch(() => props.visible) 中处理
    // 因为对话框设置了 :destroy-on-close="true"，每次打开都会重新挂载
})

onUnmounted(() => {
    // 🆕 移除全屏状态监听
    const handleFullscreenChange = () => {
        isFullscreen.value = !!(
            document.fullscreenElement ||
            document.webkitFullscreenElement ||
            document.mozFullScreenElement ||
            document.msFullscreenElement
        )
    }
    document.removeEventListener('fullscreenchange', handleFullscreenChange)
    document.removeEventListener('webkitfullscreenchange', handleFullscreenChange)
    document.removeEventListener('mozfullscreenchange', handleFullscreenChange)
    document.removeEventListener('MSFullscreenChange', handleFullscreenChange)
})

async function fetchWeekHolidays() {
    try {
        const startOfWeek = dayjs(currentWeek.value).startOf('week').format('YYYY-MM-DD')
        const endOfWeek = dayjs(currentWeek.value).endOf('week').format('YYYY-MM-DD')
        const res = await queryHoliday({ sdate: startOfWeek, edate: endOfWeek })
        weekHolidays.value = res?.Data?.Data || []
    } catch (e) {
        weekHolidays.value = []
    }
}

// 暴露方法给父组件调用
defineExpose({
    handleRefresh,
    isVisible: () => dialogVisible.value
})
</script>

<style lang="scss" scoped>
.compare-canlendar-dialog {
    background: #F0F2F5;

    .el-dialog {
        margin: 0;
        height: 100vh;
        max-height: none;
        border-radius: 0;
    }

    .el-dialog__header {
        padding: 0;
        margin: 0;
    }

    .el-dialog__body {
        padding: 0;
        height: calc(100vh - 60px);
        overflow: hidden;
    }

    .el-dialog__headerbtn {
        display: none;
    }
}

.dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-shrink: 0;

    .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .campus-label {
            font-size: 14px;
            color: #606266;
            font-weight: 600;
        }

        .title {
            margin: 0;
            font-weight: 500;
            font-size: 16px;
            color: #303133;
        }

        .campus-tip-icon {
            color: #909399;
            cursor: pointer;
            transition: color 0.3s;

            &:hover {
                color: #409eff;
            }
        }
    }

    .date-navigation {
        display: flex;
        align-items: center;
        gap: 8px;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);

        .arrow-button {
            padding: 8px;
            border-color: #dcdfe6;
            color: #606266;

            &:hover {
                color: #409eff;
                border-color: #c6e2ff;
                background-color: #ecf5ff;
            }
        }

        :deep(.date-picker-choose-box) {
            width: 170px;

            .wtwo-input__prefix {
                display: none;
            }

            .wtwo-input__wrapper {
                box-shadow: none !important;
                padding: 0;

                .wtwo-input__inner {
                    cursor: pointer !important;
                    font-weight: 600;
                    text-align: center;
                    padding: 0 6px;
                    border-radius: 4px;
                    color: #303133;

                    &:hover {
                        background-color: #F3F4F4;
                    }
                }
            }
        }

        .today-button {
            color: #606266;
        }
    }

    .header-right {
        display: flex;
        align-items: center;
        gap: 8px;

        .header-right-item {
            cursor: pointer;
            font-weight: 400;
            font-size: 14px;
            width: 74px;
            height: 32px;
            background: rgba(0, 0, 0, 0.04);
            border-radius: 4px 4px 4px 4px;
            color: #606266;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 4px;

            &.header-right-icon-only {
                width: 32px;
            }
        }
    }
}

.main-content {
    height: calc(100vh - 48px);
    display: flex;
    min-height: 0;
    overflow-x: auto;
}

.student-section,
.teacher-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    width: 50%; /* 🆕 固定各占50%宽度 */
    max-width: 50%; /* 🆕 限制最大宽度 */

    &:first-child {
        margin-right: 6px;
    }

    &:last-child {
        margin-left: 6px;
    }
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 7px 12px;
    margin: 8px 0;
    background-color: #fff;
    border-radius: 4px;
    flex-shrink: 0;

    .header-title {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;
        min-width: 0;

        h3 {
            margin: 0;
            font-weight: 400;
            font-size: 14px;
            color: #606266;
            flex-shrink: 0;
        }

        .selected-items {
            display: flex;
            align-items: center;
            gap: 4px;
            flex: 1;
            min-width: 0;
            overflow: hidden;
            position: relative;

            .selected-item {
                display: flex;
                align-items: center;
                gap: 4px;
                padding: 2px 10px;
                background: #F4F4F5;
                border-radius: 4px;
                font-size: 12px;
                flex-shrink: 0;
                font-weight: 400;
                height: 22px;

                .item-name {
                    color: #303133;
                    max-width: 80px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    line-height: 16px;
                }

                .item-close {
                    font-size: 12px;
                    color: #909399;
                    cursor: pointer;
                    line-height: normal;
                    flex-shrink: 0;

                    &:hover {
                        color: #f56c6c;
                    }
                }
            }

            .more-count {
                padding: 2px 10px;
                background: #F4F4F5;
                border-radius: 4px;
                font-weight: 400;
                font-size: 12px;
                color: #303133;
                flex-shrink: 0;
                height: 22px;
                display: flex;
                align-items: center;
                cursor: pointer;
                transition: background-color 0.2s ease;

                &:hover {
                    background: #E4E7ED;
                }
            }
        }
    }

    .header-actions {
        display: flex;
        align-items: center;
        gap: 8px;

        .header-divider {
            color: #E4E7ED;
            font-size: 14px;
        }

        .sort-btn {
            color: #2878E8;
            padding: 0;
            font-size: 14px;

            &:hover {
                color: #409eff;
            }

            &:disabled {
                color: #c0c4cc;
            }
        }

        .add-btn {
            color: #2878E8;
            padding: 0;
            font-size: 14px;

            &:hover {
                color: #409eff;
            }
        }
    }
}

.section-content {
    flex: 1;
    overflow-y: auto; // 外部滚动，可以看到所有学员
    min-height: 0;
    border-radius: 4px;

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

.empty-state {
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    height: 100%;
    color: #909399;
}

.students-container,
.teachers-container {
    padding: 0;
}

.student-item-wrapper,
.teacher-item-wrapper {
    margin-bottom: 8px;
    // 不设置 overflow，让内部的 sticky 元素可以工作

    &:last-child {
        margin-bottom: 0;
    }
}

.student-item,
.teacher-item {
    border-radius: 4px;
    background-color: #fff;
    // 不设置固定高度，让内容自然撑开
    // 不设置 overflow，让 sticky 能够工作

    &:last-child {
        border-bottom: none;
    }
}

.student-header,
.teacher-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #fafafa;
}

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
    gap: 2px;

    .more-btn {
        cursor: pointer;

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

    .object-count {
        font-size: 12px;
        color: #909399;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        line-height: 1;
        background: #F0F0F0;
        border-radius: 40px;
        height: 18px;
        width: max-content;
        padding: 2px 8px;
    }
}

.student-name,
.teacher-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
}

.student-id,
.teacher-id {
    font-size: 12px;
    color: #909399;
}

.remove-btn {
    color: #909399;

    &:hover {
        color: #f56c6c;
    }
}

// 排序面板样式
.sort-panel {
    .sort-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        margin-bottom: 12px;

        span:first-child {
            font-weight: 500;
            color: #303133;
        }

        .sort-tip {
            font-size: 12px;
            color: #909399;
        }
    }

    .sort-list {
        max-height: 350px;
        overflow-y: auto;

        .sort-item {
            display: flex;
            align-items: center;
            padding: 8px;
            border-bottom: 1px solid #f5f5f5;
            cursor: move;
            border-radius: 4px;
            margin: 2px 0;
            background-color: #fff;
            position: relative;
            will-change: transform;

            &:last-child {
                border-bottom: none;
            }

            // 拖拽中的样式
            &.dragging {
                opacity: 0.5;
                z-index: 1000;
                background-color: #fff;
            }

            .drag-handle {
                fill: #909399;
                width: 16px;
                height: 16px;
                margin-right: 8px;
                cursor: grab;
                transition: color 0.2s ease;
                user-select: none;
                flex-shrink: 0;

                &:hover {
                    color: #2878E8;
                }

                &:active {
                    cursor: grabbing;
                }
            }

            // 拖拽时手柄样式
            &.dragging .drag-handle {
                color: #2878E8;
                cursor: grabbing;
            }

            .item-avatar {
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
                margin-right: 12px;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }

            .item-info {
                flex: 1;
                min-width: 0;

                .item-name {
                    font-size: 14px;
                    font-weight: 500;
                    color: #303133;
                    margin-bottom: 2px;
                    line-height: 16px;
                }

                .item-serial {
                    font-size: 12px;
                    color: #909399;
                }
            }

            .remove-btn {
                color: #909399;
                margin-left: 8px;
                line-height: normal;

                &:hover {
                    color: #f56c6c;
                }
            }
        }
    }
}

// 更多人员列表样式
.more-list {
    max-height: 300px;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 4px;

    .more-item-tag {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 2px 10px;
        background: #F4F4F5;
        border-radius: 4px;
        font-size: 12px;
        flex-shrink: 0;
        font-weight: 400;
        height: 22px;

        .item-name {
            color: #303133;
            max-width: 80px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            line-height: 16px;
        }

        .item-close {
            font-size: 12px;
            color: #909399;
            cursor: pointer;
            line-height: normal;
            flex-shrink: 0;

            &:hover {
                color: #f56c6c;
            }
        }
    }
}

// 🚀 Loading 样式优化
.students-container,
.teachers-container {
    position: relative;
    min-height: 200px;

    :deep(.el-loading-mask) {
        background-color: rgba(255, 255, 255, 0.8);
        backdrop-filter: blur(2px);
    }

    :deep(.el-loading-spinner) {
        .el-loading-text {
            color: #606266;
            font-size: 14px;
            margin-top: 12px;
        }

        .circular {
            width: 42px;
            height: 42px;
        }
    }
}

// 🆕 骨架屏样式 - 模拟真实课表结构
.timetable-skeleton {
    background: #fff;
    padding: 12px;
    min-height: 500px;

    .skeleton-week-header {
        display: flex;
        gap: 1px;
        margin-bottom: 1px;
        padding-left: 60px; // 为时间列留空间

        .skeleton-week-day {
            flex: 1;
            background: #f5f7fa;
            padding: 8px 4px;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 4px;

            .skeleton-day-name {
                width: 40%;
                height: 12px;
                background: #e4e7ed;
                border-radius: 2px;
            }

            .skeleton-day-date {
                width: 60%;
                height: 10px;
                background: #ebeef5;
                border-radius: 2px;
            }
        }
    }

    .skeleton-grid {
        display: flex;
        flex-direction: column;
        gap: 1px;

        .skeleton-grid-row {
            display: flex;
            gap: 1px;
            min-height: 60px;

            .skeleton-time-label {
                width: 60px;
                background: #f5f7fa;
                border-radius: 2px;
                flex-shrink: 0;
            }

            .skeleton-grid-cells {
                flex: 1;
                display: flex;
                gap: 1px;

                .skeleton-cell {
                    flex: 1;
                    background: #fafafa;
                    border: 1px solid #f0f0f0;
                    border-radius: 2px;
                    position: relative;
                    overflow: hidden;

                    &::after {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: -100%;
                        width: 100%;
                        height: 100%;
                        background: linear-gradient(
                            90deg,
                            transparent,
                            rgba(255, 255, 255, 0.6),
                            transparent
                        );
                        animation: shimmer 2s infinite;
                    }
                }
            }
        }
    }
}

@keyframes shimmer {
    0% {
        left: -100%;
    }
    100% {
        left: 100%;
    }
}

// 响应式适配
@media screen and (max-width: 1200px) {
    // .main-content {
    //     flex-direction: column;
    // }

    // .student-section,
    // .teacher-section {
    //     &:first-child {
    //         border-right: none;
    //         border-bottom: 1px solid #e8e8e8;
    //     }
    // }

    // .timetable-container {
    //     height: 300px;
    // }
}
</style>
