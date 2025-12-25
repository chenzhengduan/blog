<template>
	<div class="wtwo-schedule-manage">
		<div id="wtwo-breadcrumb" class="breadcrumb">
			<span class="breadcrumb-txt">教务管理</span>
			<span class="breadcrumb-split">/</span>
			<span class="breadcrumb-main-txt">排课管理</span>
		</div>
		<div class="page-area with-breadcrumb">
			<div class="tab-op-box flex-center">
				<el-link type="primary" :underline="false" class="mr-12px text-13px! line-height-24px!" target="_blank" @click="upgradeIntro">老用户须知</el-link>
                <el-link type="primary" :underline="false" class="mr-12px text-13px! line-height-24px!" target="_blank" @click="getHelpVideo">帮助视频</el-link>
                <el-link type="primary" :underline="false" :href="activeName=='arrangeCanlendar'?'https://wiki.xiaogj.com/docs/ke-biao-ri-li':activeName=='arrangeTableList'?'https://wiki.xiaogj.com/docs/pai-ke-lie-biao':'https://wiki.xiaogj.com/docs/biao-ge-pai-ke'" class="text-13px! line-height-24px!" target="_blank">帮助文档</el-link>
				<!-- 等确认正式上线再展示 -->
				<!-- <el-dropdown trigger="click" class="ml-6px">
					<el-button class="px-5px!" size="small" text>
						<el-icon size="20px">
						<svg aria-hidden="true">
							<use xlink:href="#w2-gengduocaozuo"></use>
						</svg>
						</el-icon>
					</el-button>
					<template #dropdown>
						<el-dropdown-menu>
							<el-dropdown-item @click.native="backToW1">返回旧版排课</el-dropdown-item>
						</el-dropdown-menu>
					</template>
				</el-dropdown> -->
            </div>
			<el-tabs v-model="activeName">
				<el-tab-pane v-for="(item, index) in tabs" :key="index" :label="item.Name" :name="item.PageName"
					:lazy="false">
					<template #label>
						<div class="tab-bar">{{ item.Name }}</div>
					</template>
				</el-tab-pane>
			</el-tabs>
			<!-- 组件容器 - 使用相对定位 -->
			<div 
				class="component-wrapper"
				:class="{ 
					'drag-arrange-fullscreen': isDragArrangeMode
				}"
			>
				<!-- 日历和课表公用筛选条件 -->
				<div 
					class="search-result-box" 
					:class="{ 
						'filter-hidden': isDragArrangeMode && !isFilterExpanded
					}"
					v-if="
						activeName == 'arrangeTableList' ||
						activeName == 'arrangeCanlendar'
					">
				<div v-if="quickFilterList.length" class="flex-center pl-12px!">
					<div class="quick-area-label flex-center">快捷筛选：</div>
					<div class="quick-filter-list">
						<div class="quick-filter-item" v-for="item in quickFilterList" :key="item.ID">
							<div class="quick-filter-item-name" :title="item.Menu" @click="applyQuickFilter(item)">{{
								item.Menu }}
							</div>
							<el-dropdown trigger="click">
								<el-icon size="12px" class="more-op-icon">
									<MoreFilled />
								</el-icon>
								<template #dropdown>
									<el-dropdown-menu>
										<el-dropdown-item @click.native="removeQuickFilter(item)">删除</el-dropdown-item>
									</el-dropdown-menu>
								</template>
							</el-dropdown>
						</div>
					</div>
				</div>
				<div class="search-area">
					<div class="search-fixed-area">
						<!-- 常用筛选显示区域 -->
						<schedule-filter v-for="filter in visibleCommonFilters" :key="filter.key" :filter="filter"
				:condition="condition" :show-remove-button="visibleCommonFilters.length > 1"
				:disabled="isEditCommon || (activeName === 'arrangeCanlendar' && filter.key === 'dateRange') || isFilterDisabled(filter.key)"
				:disabledTooltip="getDisabledTooltip(filter.key)"
				:classroomList="classroomList" @remove="removeCommonFilter" @select-course="selectCourse"
				@select-class="selectClass" @handle-shift-change="handleShiftChange"
				@handle-class-change="handleClassChange" @handle-student-change="handleStudentChange"
				@handle-head-master-change="handleHeadMasterChange" @select-head-master="selectHeadMaster" @select-teacher="selectTeacher" @select-assistant="selectAssistant"
				@select-master="selectMaster"
				@select-student="selectStudent"
				@update:condition="(newCondition: any) => condition = newCondition" />
					</div>
					<div class="search-item">
						<el-link type="primary" underline="never" @click.stop="showMore" class="mr-12px">{{ expandedMore
							? '收起' :
							'展开' }}&nbsp;
							<el-icon>
								<ArrowUp v-if="expandedMore" />
								<ArrowDown v-else />
							</el-icon>
						</el-link>
						<div class="mr-12px filter-badge-box">
							<el-tooltip effect="dark" content="已设置的筛选条件数量" placement="top" v-if="setFilterCount > 0">
								<div class="badge-content">{{ setFilterCount }}</div>
							</el-tooltip>
							<el-button type="primary" @click="handleSearch">查询</el-button>
						</div>
						<el-button @click="handleReset">重置</el-button>
					</div>
				</div>
				<div class="all-filter-panel" :class="{ expanded: expandedMore }">
					<div class="all-filter-panel-header flex-end">
						<div v-if="isEditCommon" class="flex-center">
							<el-button @click="cancelCommonSet">取消</el-button>
							<el-button type="primary" @click="saveCommonSet">保存</el-button>
						</div>
						<el-link v-else type="primary" underline="never" @click="setCommonCondition">
							<el-icon size="16px" class="mr-4px">
								<svg aria-hidden="true">
									<use xlink:href="#w2-shezhichangyongtiaojian"></use>
								</svg> </el-icon>设置常用条件
						</el-link>
					</div>
					<div class="expanded-content">
						<template v-if="isEditCommon">
							<div class="common-edit-title">
								常用筛选条件（{{ commonFilters.length }}/7）
							</div>
							<div class="search-area common-filters-area">
								<schedule-filter v-for="filter in visibleCommonFilters" :key="filter.key"
					:filter="filter" :condition="condition" 
					:disabled="isEditCommon || (activeName === 'arrangeCanlendar' && filter.key === 'dateRange') || isFilterDisabled(filter.key)" 
					:disabledTooltip="getDisabledTooltip(filter.key)"
					:is-common-filter="true"
					:show-remove-button="visibleCommonFilters.length > 1" :classroomList="classroomList"
					@remove="removeCommonFilter" @select-course="selectCourse"
					@select-class="selectClass" @handle-shift-change="handleShiftChange"
					@handle-class-change="handleClassChange"
					@handle-student-change="handleStudentChange"
					@handle-head-master-change="handleHeadMasterChange"
					@select-head-master="selectHeadMaster" @select-teacher="selectTeacher"
					@select-assistant="selectAssistant" @select-student="selectStudent"
					@update:condition="(newCondition: any) => condition = newCondition" />
							</div>
							<div class="common-edit-title">更多筛选条件</div>
						</template>
						<div class="all-filter-area" :class="{ 'is-edit': isEditCommon }">
							<!-- 这里可以添加更多筛选条件 -->
							<template v-for="group in filterGroup" :key="group.label">
								<div class="search-area-title">{{ transToConfigDescript(group.label) }}</div>
								<div class="search-area">
									<schedule-filter v-for="filter in group.filters" :key="filter.key" :filter="filter"
					:condition="condition" 
					:disabled="isEditCommon || (activeName === 'arrangeCanlendar' && filter.key === 'dateRange') || isFilterDisabled(filter.key)" 
					:disabledTooltip="getDisabledTooltip(filter.key)"
					:show-common-flag="isEditCommon"
					:is-common-filter-flag="isCommonFilter(filter.key)"
					:classroomList="classroomList" @select-course="selectCourse"
					@select-class="selectClass" @handle-shift-change="handleShiftChange"
					@handle-class-change="handleClassChange"
					@handle-student-change="handleStudentChange"
					@handle-head-master-change="handleHeadMasterChange"
					@select-head-master="selectHeadMaster" @select-teacher="selectTeacher"
					@select-assistant="selectAssistant" @select-student="selectStudent"
					@select-master="selectMaster" @toggle-common="toggleCommonFilter"
					@update:condition="(newCondition: any) => condition = newCondition" />
								</div>
							</template>
						</div>
					</div>
					<div class="all-filter-panel-footer" v-if="!isEditCommon">
						<div class="footer-left">
							<el-link type="primary" underline="never" @click="openAddQuickFilter">
								<el-icon size="16px" class="mr-4px">
									<svg aria-hidden="true">
										<use xlink:href="#w2-baocunkuaijieshaixuan"></use>
									</svg>
								</el-icon>
								另存为个人快捷筛选
							</el-link>
						</div>
						<div class="footer-right">
							<el-link type="primary" underline="never" @click="showMore"
								class="mr-12px">收起&nbsp;<el-icon>
									<ArrowUp />
								</el-icon></el-link>
							<div class="mr-12px filter-badge-box">
								<el-tooltip effect="dark" content="已设置的筛选条件数量" placement="top" v-if="setFilterCount > 0">
									<div class="badge-content">{{ setFilterCount }}</div>
								</el-tooltip>
								<el-button type="primary" @click="onClickSearch">查询</el-button>
							</div>

							<el-button @click="handleReset">重置</el-button>
						</div>
					</div>
				</div>
			</div>
				
				<!-- 骨架屏 - 绝对定位覆盖 -->
				<div v-show="componentLoading" class="component-skeleton">
					<el-skeleton :rows="8" animated />
				</div>
				<!-- 实际组件 - 始终存在但可能被骨架屏覆盖 -->
				<keep-alive>
					<component v-if="activeName" :is="comp[activeName]" ref="activeComponentRef" :key="activeName"
					:class="{ 'component-hidden': componentLoading }"
					:classroomList="classroomList" 
					:is-drag-arrange-mode="isDragArrangeMode"
					:is-filter-expanded="isFilterExpanded"
					@update-date-range="handleDateRangeUpdate" 
					@component-ready="handleComponentReady"
					@filter-control="handleFilterControl"></component>
				</keep-alive>
			</div>
		</div>
		<chooseCourse ref="chooseCourseRef"></chooseCourse>
		<chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
		<chooseStudent ref="chooseStudentRef"></chooseStudent>
		<chooseClass ref="chooseClassRef"></chooseClass>
		<add-quick-filter ref="addQuickFilterRef" />
		<backToW1Form ref="backToW1FormRef"></backToW1Form>
		<scheduleUpgradeIntro ref="scheduleUpgradeIntroRef"></scheduleUpgradeIntro>
		<!-- 统一处理的事件弹窗 -->
		<addScheduleForm ref="addScheduleFormRef"></addScheduleForm>
		<editArrangeForm ref="editArrangeFormRef"></editArrangeForm>
		<el-dialog v-model="showHelpVideo" title="帮助视频" width="900" @close="closeVideo" :destroy-on-close="true">
            <div class="pt-16px pb-16px">
                <video controls :src='videoUrl' width="100%" autoplay poster="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/schedule-help-video.mp4?x-oss-process=video/snapshot,t_2000,f_jpg,ar_auto"></video>
            </div>
        </el-dialog>
	</div>
</template>
<script lang="ts" setup>
import { computed, h, nextTick, onMounted, onUnmounted, ref, shallowRef, watch, defineAsyncComponent } from 'vue'
import ScheduleFilter from './components/schedule-filter.vue'
import {
	ArrowDown,
	ArrowUp,
	Close,
	WarningFilled
} from '@element-plus/icons-vue'
import { useConfigs, useCurrentCampuses, useUser } from '@/store'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import { cloneDeep, isEqual } from 'lodash'
import { queryClassroom } from '@/api/arrange'
import { getUserLabel, deleteUserLabel, queryShift, putSysInfoByName } from '@/api/comm'
import { dayjs } from 'element-plus'
import { weekDiff } from '@/utils'
import { MoreFilled } from '@element-plus/icons-vue'
import BackToW1Form from './popup/backToW1Form.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
import ScheduleUpgradeIntro from './popup/scheduleUpgradeIntro.vue'
import ls from '@common/tool/ls/ls';
import useEvent from '@/config/event'
import addScheduleForm from './popup/addScheduleForm.vue'
import editArrangeForm from './popup/editArrangeForm.vue'

interface IKeyValueMap {
	[key: string]: any
}
const comp: IKeyValueMap = {
	arrangeCanlendar: defineAsyncComponent(() => import('./arrangeCanlendar/arrangeCanlendar.vue')),
	arrangeTableList: defineAsyncComponent(() => import('./arrangeTableList/arrangeTableList.vue')),
	tableCourseSchedule: defineAsyncComponent(() => import('./tableCourseSchedule/tableCourseSchedule.vue')),
	// aiCourse: defineAsyncComponent(() => import('./aiCourse/aiCourse.vue'))
}
const ClassCourse = window.$xgj.op('NewCourse_ClassCourse')
const StudentCourse = window.$xgj.op('NewCourse_StudentCourse')
const SubscribeCourse = window.$xgj.op('NewCourse_SubscribeCourse')
const TableCourse = ClassCourse || StudentCourse || SubscribeCourse;
const activeName = ref('arrangeTableList')
const timetablePrefStore = useTimetablePreferences()
const tabs = computed(() => {
	let tabsArray = [
		{ Name: '排课列表', PageName: 'arrangeTableList' },
		{ Name: '课表日历', PageName: 'arrangeCanlendar' },
	]
	if (TableCourse) {
		tabsArray.push({ Name: '表格排课', PageName: 'tableCourseSchedule' })
	}
	return tabsArray
}
)


const configs = computed(() => {
	return useConfigs().configs
})

// 组件加载状态
const componentLoading = ref(false)
// 标记是否已经加载过任何组件（页面级别的标记，切换tab不重置）
const hasLoadedOnce = ref(false)

// 拖拽排课模式状态
const isDragArrangeMode = ref(false)
// 筛选条件展开状态（拖拽模式下使用）
const isFilterExpanded = ref(false)

const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})
watch(
	currentCampus,
	(newVal, oldVal) => {
		condition.value.ClassroomIDList = []
		classroomList.value = []
		getClassroomList()
		handleSearch()
	},
	{
		deep: true,
	}
)


// 进入“课表日历”时，确保已加载偏好设置（若已有缓存则不重复请求）
watch(
	activeName,
	async (val) => {
		if (val === 'arrangeCanlendar') {
			await timetablePrefStore.ensureLoaded()
		}
	},
	{ immediate: true }
)

// 从 arrangeTableList 切换到 arrangeCanlendar时，如果当前视图是"指定xx"，清空禁用的筛选条件（上课时间除外）
watch(
	() => activeName.value,
	(val, oldVal) => {
		// 只在从 arrangeTableList 切换到 arrangeCanlendar 时执行
		if (val === 'arrangeCanlendar' && oldVal === 'arrangeTableList') {
			setTimeout(() => {
				// 调用子组件的方法，让子组件自己判断是否需要清空筛选条件
				if (activeComponentRef.value?.checkAndClearDisabledFilters) {
					activeComponentRef.value.checkAndClearDisabledFilters()
				}
			}, 150)
		}
	}
)

//定义什么样的值算空值
const emptyCondition: any = {
    Query:'',
	ShiftTypeList: [],
	Year: 0,
	TermID: '00000000-0000-0000-0000-000000000000',
	GradeID: '00000000-0000-0000-0000-000000000000',
	SubjectID: '00000000-0000-0000-0000-000000000000',
	CategoryID: '00000000-0000-0000-0000-000000000000',
	ClassTypeID: '00000000-0000-0000-0000-000000000000',
	shiftSelected: [],
	IsContainFinished: -1,
	headerMasterSelected: [],
	teacherList: [],
	assistantList: [],
	studentSelected: [],
	timeType: [],
	dateRange: [],
	IsSubscribeCourse: -1,
	classSelected: [],
	ClassLabelIDList: [],
	TeacherType: -1,
	CourseType: 0,
	ClassroomIDList: [],
	FinishType: [],
	masterList: []
}

const defaultCondition: any = {
	Query: '', // 查询条件，支持模糊查询
	ShiftID: '00000000-0000-0000-0000-000000000000', // 课程ID
	ShiftName: '', // 课程名称
	ClassID: '00000000-0000-0000-0000-000000000000', // 班级ID
	ClassName: '', // 班级名称
	MasterIDList: [], // 学管师ID
	CourseFlag: '', //
	StudentID: '00000000-0000-0000-0000-000000000000', // 学员ID
	HeadMasterUserID: '00000000-0000-0000-0000-000000000000', // 班主任
	IsContainFinished: 0, // 0:未结业；1:结业
	ShiftTypeList: [], // 不传=不限,0集体班课程，1一对一课程，2一对多课程，多个逗号分隔
	CourseNumbers: 0, // 补课课次
	TeacherType: -1, // 老师类型,0=全职，1=兼职，2=专职,-1=不限
	CourseType: 0, // 上课方式，1=线下课，2=在线课
	FinishType: [], // 上课状态：-1不限，0未上课，1已上课，2已取消，数据库中的状态为：0未上课，1已上课，2已取消，多个以,分割
	ClassLabelIDList: [], // 班级标签ID
	ShowField: '', // 周、月视图列表显示字段 班级,老师,学员...
	CampusIDList: [], // 校区ID,多个用逗号分隔
	GradeID: '00000000-0000-0000-0000-000000000000', // 课程年级筛选
	SubjectID: '00000000-0000-0000-0000-000000000000', // 课程科目筛选
	CategoryID: '00000000-0000-0000-0000-000000000000', // 课程类型筛选
	ClassroomIDList: [], // 教室ID,多个用逗号分隔
	StartDate: '', // 排课开始时间
	EndDate: '', // 排课结束时间
	TeacherIDList: [], // 任课老师筛选
	AssistantTeacherIDList: [], // 助教老师筛选
	Forenoon: 1, // 上午
	Afternoon: 1, // 下午
	Nightnoon: 1, // 晚上
	Year: 0, // 年份
	TermID: '00000000-0000-0000-0000-000000000000', // 期段ID
	ClassTypeID: '00000000-0000-0000-0000-000000000000', // 班型ID
	IncludeFull: 0, // 包含人数已满的排课（0包含,-1不包含）
	Weekdays: '', // 按星期筛选
	DialogType: 0, // 查询场景，0其他；1跟班补课查询排课，2调课查询排课
	TryStatus: 0, // 1=处理不需要试听班级的排课
	UsePlatform: 0, // 判断是否微信端调用该接口 微信调用需加上课程手机端不显示的过滤条件,1=需要，0=不需要
	ExportColumn: [], // 选择列，逗号分隔
	Download: 0, // 0=查询，1=导出
	IsTotal: 0, // 是否合计,0=不是，1=是
	IsSubscribeCourse: -1, // 开放预约状态：-1不限，0未开放，1已开放
	// 筛选参数
	headerMasterName: '', // 班主任名称
	teacherList: [], // 任课老师列表
	assistantList: [], // 助教列表
	studentName: '', // 学员名称
	masterList: [], // 学管师列表
	timeType: [], // 时段类型
	dateRange: [dayjs(weekDiff(0)[0]).format('YYYY-MM-DD'), dayjs(weekDiff(0)[1]).format('YYYY-MM-DD')], // 日期范围
	shiftSelected: [], // 上课课程选中项数组
	studentSelected: [], // 学员选中项数组
	headerMasterSelected: [], // 班主任选中项数组
	classSelected: [], // 班级选中项数组
}

const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))

// 添加 classroomList 作为独立的 ref
const classroomList = ref<any[]>([])

const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
const isEditCommon = ref(false)

// 常用筛选相关
const commonFilters = ref<any[]>([])
const originalCondition = ref<any>({})
const originalCommonFilters = ref<any[]>([])

// 可见的常用筛选条件（根据当前 tab 过滤）
const visibleCommonFilters = computed(() => {
	// 不再过滤 dateRange，让它显示但不可编辑
	return commonFilters.value
})

// 筛选项配置
const filterConfigs = computed(() => {
	const baseConfigs = [
		{ key: 'ShiftTypeList', label: '教学形式', type: 'select' },
		{ key: 'Year', label: '课程年份', type: 'select' },
		{ key: 'TermID', label: '课程期段', type: 'select' },
		{ key: 'GradeID', label: '课程年级', type: 'select' },
		{ key: 'SubjectID', label: '课程科目', type: 'select' },
		{ key: 'CategoryID', label: '课程类型', type: 'select' },
		{ key: 'ClassTypeID', label: '课程班型', type: 'select' },
		{ key: 'ShiftName', label: '上课课程', type: 'input-tag' },
		{ key: 'IsContainFinished', label: '结业状态', type: 'select' },
		{ key: 'headerMasterName', label: '班主任', type: 'input-tag' },
		{ key: 'teacherList', label: '任课老师', type: 'input-tag' },
		{ key: 'assistantList', label: '助教', type: 'input-tag' },
		{ key: 'ClassLabelIDList', label: '班级标签', type: 'select' },
		{ key: 'TeacherType', label: '老师类型', type: 'select' },
		{ key: 'CourseType', label: '上课方式', type: 'select' },
		{ key: 'ClassroomIDList', label: '上课教室', type: 'select' },
		{ key: 'studentName', label: '学员', type: 'input-tag' },
		{ key: 'masterList', label: '学管师', type: 'input-tag' },
		{ key: 'FinishType', label: '上课状态', type: 'select' },
		{ key: 'timeType', label: '上课时段', type: 'select' },
		{ key: 'dateRange', label: '上课时间', type: 'date-range' },
		{ key: 'Query', label: '搜索', type: 'input' },
		{ key: 'ClassName', label: '上课班级', type: 'input-tag' },
		{ key: 'IsSubscribeCourse', label: '开放预约', type: 'select' },
	]

	return baseConfigs
})

const filterGroup = computed(() => {
	const baseFilterGroup = [
		{
			label: '上课条件',
			filters: [
				{ key: 'dateRange', label: '上课时间' },
				{ key: 'studentName', label: '学员' },
				{ key: 'masterList', label: '学管师' },
				{ key: 'CourseType', label: '上课方式' },
				{ key: 'ClassroomIDList', label: '上课教室' },
				{ key: 'FinishType', label: '上课状态' },
				{ key: 'timeType', label: '上课时段' },
				{ key: 'IsSubscribeCourse', label: '开放预约' },
				{ key: 'Query',label:'搜索'}
			],
		}, {
			label: '班级条件',
			filters: [
				{ key: 'IsContainFinished', label: '结业状态' },
				{ key: 'headerMasterName', label: '班主任' },
				{ key: 'teacherList', label: '任课老师' },
				{ key: 'assistantList', label: '助教' },
				{ key: 'ClassLabelIDList', label: '班级标签' },
				{ key: 'TeacherType', label: '任课老师类型' },
				{ key: 'ClassName', label: '上课班级' },
			],
		}, {
			label: '课程条件',
			filters: [
				{ key: 'ShiftTypeList', label: '教学形式' },
				{ key: 'Year', label: '课程年份' },
				{ key: 'TermID', label: '课程期段' },
				{ key: 'GradeID', label: '课程年级' },
				{ key: 'SubjectID', label: '课程科目' },
				{ key: 'CategoryID', label: '课程类型' },
				{ key: 'ClassTypeID', label: '课程班型' },
				{ key: 'ShiftName', label: '上课课程' },
			],
		}
	]

	return baseFilterGroup
})


const chooseCourseRef = ref()

// 上课课程选中项数组 - 已移动到 condition 中


function handleShiftChange(val: any) {
	if (val.length) {
		// 有选中项时，设置 ShiftID 和 ShiftName
		condition.value.ShiftID = val[0].ID
		condition.value.ShiftName = val[0].Name
		// 更新 shiftSelected 数组
		condition.value.shiftSelected = val
	} else {
		// 没有选中项时，清空 ShiftID，但保持 ShiftName（用户输入的内容）
		condition.value.ShiftID = EMPTYGUID
		// 注意：不清空 condition.value.ShiftName，因为用户可能输入了自定义内容
		// 清空 shiftSelected 数组
		condition.value.shiftSelected = []
	}
}
function selectCourse() {
	chooseCourseRef.value
		?.open({
			multi: false,
			required: true,
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.ShiftID = res.data.ID
				condition.value.ShiftName = res.data.Name
				// 同时更新 shiftSelected 数组
				condition.value.shiftSelected = [res.data]
			}
		})
}

function handleClassChange(val: any) {
	if (val.length) {
		condition.value.ClassID = val[0].ID
		condition.value.ClassName = val[0].Name
		condition.value.classSelected = val
	} else {
		condition.value.ClassID = EMPTYGUID
		condition.value.classSelected = []
	}
}

const chooseClassRef = ref()
function selectClass() {
	chooseClassRef.value
		?.open({
			multi: false,
			required: true,
			showClassStatusFilter: true
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.ClassID = res.data.ID
				condition.value.ClassName = res.data.Name
				condition.value.classSelected = [res.data]
			}
		})
}

function handleHeadMasterChange(val: any) {
	if (val.length) {
		condition.value.HeadMasterUserID = val[0].ID
		condition.value.headerMasterName = val[0].Name
		condition.value.headerMasterSelected = val
	} else {
		condition.value.HeadMasterUserID = EMPTYGUID
		condition.value.headerMasterSelected = []
	}
}

const chooseEmpTableRef=ref()
function selectHeadMaster() {
	chooseEmpTableRef.value
		?.open({
			multi: false,
			required: true
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.HeadMasterUserID = res.data.ID
				condition.value.headerMasterName = res.data.Name
                condition.value.headerMasterSelected = [res.data]
			}
		})
}

function selectTeacher() {
	chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: condition.value.teacherList
		})
		.then((res: any) => {
			condition.value.teacherList = res.data || []
		})
}

function selectAssistant() {
	chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: condition.value.assistantList
		})
		.then((res: any) => {
			condition.value.assistantList = res.data || []
		})
}

function selectMaster() {
	chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: condition.value.masterList
		})
		.then((res: any) => {
			condition.value.masterList = res.data || []
		})
}

// 处理筛选条件控制事件
function handleFilterControl(data: { disable: boolean; filterKey: string }) {
	const { disable, filterKey } = data
	
	// 更新禁用筛选条件列表
	if (disable && !disabledFilterKeys.value.includes(filterKey)) {
		disabledFilterKeys.value.push(filterKey)
	} else if (!disable) {
		disabledFilterKeys.value = disabledFilterKeys.value.filter(key => key !== filterKey)
	}
	
	// 根据筛选条件key清空对应的值
	switch (filterKey) {
		case 'teacherList':
			if (disable) {
				condition.value.teacherList = []
			}
			break
		case 'studentSelected':
		case 'studentName':
			if (disable) {
				condition.value.StudentID = EMPTYGUID
				condition.value.studentName = ''
				condition.value.studentSelected = []
			}
			break
		case 'classSelected':
		case 'ClassName':
			if (disable) {
				condition.value.classSelected = []
				condition.value.ClassID = EMPTYGUID
				condition.value.ClassName = ''
			}
			break
		case 'ClassroomIDList':
			if (disable) {
				condition.value.ClassroomIDList = []
			}
			break
		default:
			break
	}
	
	// 触发查询更新
	handleSearch()
}

function handleStudentChange(val: any) {
	if (val.length) {
		condition.value.StudentID = val[0].ID
		condition.value.studentName = val[0].Name
		condition.value.studentSelected = val
	} else {
		condition.value.StudentID = EMPTYGUID
		condition.value.studentSelected = []
	}
}
const chooseStudentRef = ref()
function selectStudent() {
	chooseStudentRef.value
		?.open({
			multi: false,
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.StudentID = res.data.ID
				condition.value.studentName = res.data.Name
                condition.value.studentSelected=[res.data]
			}
		})
}

function getClassroomList() {
	// '4fa703e4-12b0-4406-9651-e2844a570736'
	queryClassroom({ campusIds: currentCampus.value || EMPTYGUID }).then(
		(res: any) => {
			classroomList.value = res.Data || []
		}
	)
}

const expandedMore = ref(false)
const activeComponentRef = ref()

// 存储当前需要禁用的筛选条件key列表
const disabledFilterKeys = ref<string[]>([])

// 判断筛选条件是否应该被禁用
function isFilterDisabled(filterKey: string): boolean {
	return activeName.value === 'arrangeCanlendar'?disabledFilterKeys.value.includes(filterKey):false
}

// 标记是否已经执行过查询，用于避免重复查询
const hasSearchedOnce = ref(false)

// 获取禁用提示文案
function getDisabledTooltip(filterKey: string): string {
	// 课表日历视图下的日期范围筛选禁用提示
	if (activeName.value === 'arrangeCanlendar' && filterKey === 'dateRange') {
		return '【课表日历】因自带日期范围，所以此筛选不可用！'
	}
	
	// 其他视图下的筛选禁用提示
	const viewNames: Record<string, string> = {
		'arrangeTableList': '排课列表',
		'arrangeCanlendar': '课表日历'
	}
	
	const viewName = viewNames[activeName.value] || '当前视图'
	
	// 根据筛选条件key返回对应的提示文案
	if (disabledFilterKeys.value.includes(filterKey)) {
		// 课表日历视图下的特定筛选条件禁用提示
		if (activeName.value === 'arrangeCanlendar' && activeComponentRef.value) {
			// 获取arrangeCanlendar组件实例
			const arrangeCanlendarInstance = activeComponentRef.value as any
			
			// 检查组件是否有selectedComponentType和filterMap属性
			if (arrangeCanlendarInstance.selectedComponentType && arrangeCanlendarInstance.filterMap) {
				const { selectedComponentType, filterMap } = arrangeCanlendarInstance
				
				// 检查当前筛选条件是否是对应课表类型的禁用筛选条件
				if (filterMap[selectedComponentType] === filterKey) {
					// 根据课表类型返回特定的提示文案
					switch (selectedComponentType) {
						case 'ClassTimetable':
							return transToConfigDescript('【班级课表】"指定班级"的模式下，此筛选不可用！')
						case 'TeacherTimetable':
							return transToConfigDescript('【老师课表】"指定老师"的模式下，此筛选不可用！')
						case 'ClassroomTimetable':
							return '【教室课表】"指定教室"的模式下，此筛选不可用！'
						case 'StudentTimetable':
							return '【学员课表】"指定学员"的模式下，此筛选不可用！'
					}
				}
			}
		}
		
		// 默认提示文案
		return `【${viewName}】当前模式下，此筛选不可用！`
	}
	
	return ''
}

function showMore() {
	expandedMore.value = !expandedMore.value
}

// 监听 tab 切换，确保组件挂载后能正确触发查询
watch(activeName, (newVal, oldVal) => {
	
	// 每次 tab 切换都重新设置 hasSearchedOnce 为 false
	hasSearchedOnce.value = false
	expandedMore.value = false
	
	// 只在首次加载时显示骨架屏
	if (!hasLoadedOnce.value) {
		componentLoading.value = true
	}

	// 如果切换到表格排课标签页，发送通信
	if (newVal === 'tableCourseSchedule' && window.microApp) {
		window.microApp.dispatch({
			type: 'scheduleManage:hideCampus',
		})
	} else if (oldVal === 'tableCourseSchedule' && window.microApp) {
		window.microApp.dispatch({
			type: 'scheduleManage:showCampus',
		})
	}

	// 等待下一个 tick，确保组件已经挂载
	nextTick(() => {
		if (activeComponentRef.value && typeof activeComponentRef.value.search === 'function') {
			handleSearch()
			hasSearchedOnce.value = true
		}
		// 如果切换到课表日历，同步日期范围到condition
		if (newVal === 'arrangeCanlendar' && activeComponentRef.value && typeof activeComponentRef.value.updateDateRange === 'function') {
			// 延迟一点确保组件完全初始化
			setTimeout(() => {
				if (activeComponentRef.value && typeof activeComponentRef.value.updateDateRange === 'function') {
					activeComponentRef.value.updateDateRange()
				}
			}, 100)
		}
	})
})

// 监听组件引用变化，确保组件挂载后能触发查询
watch(activeComponentRef, (val, oldVal) => {
	// 只有在组件引用变化且还没有搜索过的情况下才触发搜索
	// 避免与 activeName 的 watch 重复触发
	if (val && oldVal !== val && !hasSearchedOnce.value && typeof val.search === 'function') {
		// 延迟执行，避免与 activeName 的 watch 同时触发
		setTimeout(() => {
			if (!hasSearchedOnce.value) {
				handleSearch()
				hasSearchedOnce.value = true
			}
		}, 0)
	}
	// 如果切换到课表日历，同步日期范围到condition
	if (val && activeName.value === 'arrangeCanlendar' && typeof val.updateDateRange === 'function') {
		setTimeout(() => {
			if (activeComponentRef.value && typeof activeComponentRef.value.updateDateRange === 'function') {
				activeComponentRef.value.updateDateRange()
			}
		}, 100)
	}
	
	// 注意：不在这里调用 handleSearch，避免与 onMounted 中的 watch 重复
	// 查询逻辑统一由 onMounted 中的 watch 处理
})

// 处理组件加载完成
function handleComponentReady() {
	// 隐藏骨架屏并标记已加载过
	componentLoading.value = false
	hasLoadedOnce.value = true
}

// 处理重置按钮点击事件
function handleReset() {
	// 重置所有筛选条件到默认值
	Object.assign(condition.value, cloneDeep(defaultCondition))
	// 重置后自动查询
	handleSearch()
	expandedMore.value = false
}

// 处理查询按钮点击事件
function onClickSearch() {
	handleSearch()
	expandedMore.value = false
}

//发请求
function handleSearch() {
	let params: any = {}

	// 从 condition 中排除需要特殊处理的参数
	const { headerMasterName, teacherList, assistantList, studentName, masterList, timeType, dateRange, shiftSelected, studentSelected, headerMasterSelected, classSelected, ...conditionParams } = condition.value

	Object.assign(params, conditionParams)

	// 处理时间范围
	if (condition.value.dateRange && condition.value.dateRange.length === 2) {
		params.StartDate = condition.value.dateRange[0]
		params.EndDate = condition.value.dateRange[1]
	}

	// 处理时段筛选
	params.Forenoon =
		condition.value.timeType.length === 0 || condition.value.timeType.includes(0) ? 1 : 0
	params.Afternoon =
		condition.value.timeType.length === 0 || condition.value.timeType.includes(1) ? 1 : 0
	params.Nightnoon =
		condition.value.timeType.length === 0 || condition.value.timeType.includes(2) ? 1 : 0

	// 处理老师ID列表
	params.TeacherIDList = condition.value.teacherList.map((item: any) => item.ID)
	params.AssistantTeacherIDList = condition.value.assistantList.map((item: any) => item.ID)
	params.MasterIDList = condition.value.masterList.map((item: any) => item.ID)
	params.FinishType = condition.value.FinishType.length
		? condition.value.FinishType.join(',')
		: '-1'
	params.ShiftTypeList = condition.value.ShiftTypeList || []
	params.CampusIDList = currentCampus.value
		? currentCampus.value.split(',')
		: []
	// 调用当前激活组件的查询方法
	if (
		activeComponentRef.value &&
		typeof activeComponentRef.value.search === 'function'
	) {
		activeComponentRef.value.search(params)
	}
}
// 常用筛选相关方法
function setCommonCondition() {
	// 保存当前条件状态
	originalCondition.value = cloneDeep(condition.value)
	// 保存当前常用筛选状态
	originalCommonFilters.value = cloneDeep(commonFilters.value)
	isEditCommon.value = true
}

function cancelCommonSet() {
	// 恢复到设置前的状态
	if (originalCondition.value) {
		Object.assign(condition.value, cloneDeep(originalCondition.value))
	}
	// 恢复常用筛选状态
	commonFilters.value = cloneDeep(originalCommonFilters.value)
	isEditCommon.value = false
}

function saveCommonSet() {
	// 保存常用筛选到本地存储
	saveCommonFiltersToStorage()
	isEditCommon.value = false
	expandedMore.value = false
}

// 切换常用筛选状态
function toggleCommonFilter(filterKey: string) {
	const index = commonFilters.value.findIndex(
		(item) => item.key === filterKey
	)
	if (index > -1) {
		// 移除常用筛选
		commonFilters.value.splice(index, 1)
		saveCommonFiltersToStorage()
	} else {
		// 添加常用筛选（最多4个）
		if (commonFilters.value.length < 7) {
			const config = filterConfigs.value.find((item: any) => item.key === filterKey)
			if (config) {
				commonFilters.value.push({
					key: filterKey,
					label: config.label,
					type: config.type,
				})
				// 保存到本地存储
				saveCommonFiltersToStorage()
			}
		}
	}
}

// 检查是否为常用筛选
function isCommonFilter(filterKey: string) {
	return commonFilters.value.some((item) => item.key === filterKey)
}

// 保存常用筛选到本地存储
function saveCommonFiltersToStorage() {
	// 只保存筛选项配置，不保存具体值
	const commonData = commonFilters.value.map((item) => ({
		key: item.key,
		label: item.label,
		type: item.type,
	}))
	localStorage.setItem(
		'scheduleManage_commonFilters',
		JSON.stringify(commonData)
	)
}

// 从本地存储加载常用筛选
function loadCommonFiltersFromStorage() {
	try {
		const stored = localStorage.getItem('scheduleManage_commonFilters')
		if (stored) {
			const commonData = JSON.parse(stored)
			commonFilters.value = commonData.map((item: any) => ({
				key: item.key,
				label: item.label,
				type: item.type,
			}))
		} else {
			// 如果没有存储的数据，设置默认的常用筛选
			setDefaultCommonFilters()
		}
	} catch (error) {
		console.error('加载常用筛选失败:', error)
		// 出错时也设置默认值
		setDefaultCommonFilters()
	}
}

// 设置默认的常用筛选
function setDefaultCommonFilters() {
	const defaultKeys = ['Query', 'dateRange', 'studentName']
	commonFilters.value = defaultKeys
		.map((key) => {
			const config = filterConfigs.value.find((item: any) => item.key === key)
			return config
				? {
					key: config.key,
					label: config.label,
					type: config.type,
				}
				: null
		})
		.filter(Boolean) as any[]
}

// 删除常用筛选项
function removeCommonFilter(filterKey: string) {
	const index = commonFilters.value.findIndex(
		(item) => item.key === filterKey
	)
	if (index > -1) {
		commonFilters.value.splice(index, 1)
		saveCommonFiltersToStorage()
	}
}

const quickFilterList = ref<any[]>([])

// 获取快捷筛选项
function fetchQuickFilters() {
	getUserLabel({ TypeList: [4] }).then(res => {
		quickFilterList.value = res.Data || []
	})
}



onMounted(() => {
	const mountId = Date.now() // 添加挂载ID用于追踪
	
	// 初始化时显示骨架屏（首次加载）
	componentLoading.value = true
	
	getClassroomList()
	loadCommonFiltersFromStorage()
	fetchQuickFilters()
	
	// 等待异步组件加载后再执行查询
	// 使用 watch 监听 activeComponentRef，确保组件真正加载完成
	// const unwatchComponent = watch(
	// 	() => activeComponentRef.value,
	// 	(component) => {
	// 		if (component) {
	// 			console.log(`[父组件-${mountId}] 异步组件已加载，准备执行查询`)
	// 			nextTick(() => {
	// 				console.log(`[父组件-${mountId}] nextTick 后执行 handleSearch`)
	// 				handleSearch()
	// 			})
	// 			// 只执行一次，然后停止监听
	// 			unwatchComponent()
	// 		}
	// 	},
	// 	{ immediate: true }
	// )
	
	// 延迟预加载其他异步组件，避免影响首屏加载
	// 等待首屏组件加载完成后再预加载
	setTimeout(() => {
		try {
			// 优先预加载排课列表
			import('./arrangeCanlendar/arrangeCanlendar.vue')
			
			// 只在有权限时才预加载表格排课
			if (TableCourse) {
				import('./tableCourseSchedule/tableCourseSchedule.vue').catch(() => {})
			}
		} catch (_) { }
	}, 1500) // 延迟1.5秒后再开始预加载，确保不影响首屏
	const localStorage=ls.getItem('hide_schedule_intro');
    if (!localStorage) {
		nextTick(()=>{
			upgradeIntro()
		})
        
    }
})

// 在<script setup>中添加保存快捷筛选的方法
function openAddQuickFilter() {
	if (quickFilterList.value.length >= 10) {
		ElMessage.warning('最多保存10个快捷筛选标签')
		return
	}
	// 组装筛选条件数组
	const filterDataArr = filterConfigs.value.map((item: any, idx: number) => ({
		Name: item.label,
		Key: item.key,
		Value: condition.value[item.key],
		Index: idx
	}))
	const params = {
		FormData: JSON.stringify(filterDataArr),
		Type: 4,
		PageName: 'scheduleManage',
		ParentPageName: ''
	}
	addQuickFilterRef.value.open(params).then(() => {
		expandedMore.value = false
		fetchQuickFilters()
	})
}

function applyQuickFilter(item: any) {
	// 解析FormData字段
	let filterArr = []
	try {
		filterArr = JSON.parse(item.FormData || item.formData || '[]')
	} catch (e) {
		ElMessage.warning('筛选条件解析失败');
		return
	}
	// 回填到condition.value
	filterArr.forEach((f: any) => {
		if (f.Key && condition.value.hasOwnProperty(f.Key) && !isFilterDisabled(f.Key)) {
			condition.value[f.Key] = f.Value
		}
	})
	handleSearch()
}

function removeQuickFilter(item: any) {
	ElMessageBox.confirm(
		`确定删除"${item.Menu}"吗？`,
		'提示',
		{
			type: 'warning'
		}
	).then(() => {
		deleteUserLabel({ ID: item.ID }).then(() => {
			ElMessage.success('快捷筛选已删除');
			fetchQuickFilters()
		})
	})

}

// 处理日期范围更新
function handleDateRangeUpdate(dateRange: [string, string]) {
	if (dateRange && dateRange.length === 2 && dateRange[0] && dateRange[1]) {
		condition.value.dateRange = dateRange
	}
}

// 2. 定义ref
const addQuickFilterRef = ref()

// 已设置的筛选条件数量（与 emptyCondition 不相等即视为已设置）
const setFilterCount = computed(() => {
	// 仅依据 emptyCondition 的键进行判断，并按可见性剔除 dateRange
	const allKeys = Object.keys(emptyCondition)
	// const keysToCheck = allKeys.filter(k => !(activeName.value === 'arrangeCanlendar' && k === 'dateRange'))

	let count = 0
	for (const key of allKeys) {
		let currentValue = (condition.value as any)[key]
		const emptyValue = (emptyCondition as any)[key]

		// 特殊：dateRange 可能为 null 或 [null, null]，按“空”处理
		if (key === 'dateRange') {
			if (currentValue == null || (Array.isArray(currentValue) && currentValue.every((v: any) => !v))) {
				currentValue = []
			}
		}

		// 规范化：若空值是数组而当前值为 null/undefined，则按空数组处理
		if (Array.isArray(emptyValue) && (currentValue === null || currentValue === undefined)) {
			currentValue = []
		}
		// 规范化：若当前值为 undefined 且存在空值定义，则视为等同空值
		if (currentValue === undefined) {
			currentValue = emptyValue
		}
		if (!isEqual(currentValue, emptyValue)) {
			count++
		}
	}
	return count
})

const backToW1FormRef=ref()
const user=computed(()=>{
	return useUser().user
})
function backToW1(){
	backToW1FormRef.value?.open().then(()=>{
		var adminreg=/^admin@/,isadmin=false;
		if(adminreg.test(user.value.UserName)||user.value.UserName=='admin'){
			isadmin=true;
		}
		if(isadmin){
			putSysInfoByName({
				name: 'EnableNewCourse',
				value: 0
			}).then(() => {
				window.location.replace('/index.html');
			})
		}else{
			ElMessageBox.alert('请联系管理员admin切换','提示')
		}
	})
}

const scheduleUpgradeIntroRef=ref()
function upgradeIntro(){
	scheduleUpgradeIntroRef.value?.open()
}
const showHelpVideo=ref(false)
const videoUrl=ref('')
function getHelpVideo(){
    showHelpVideo.value=true
    videoUrl.value='https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/schedule-help-video.mp4'
}
function closeVideo(){
    showHelpVideo.value=false
    videoUrl.value=''
}

// 统一处理的事件弹窗引用
const addScheduleFormRef = ref<InstanceType<typeof addScheduleForm> | null>(null)
const editArrangeFormRef = ref<InstanceType<typeof editArrangeForm> | null>(null)

// 事件系统
const event = useEvent()

// 进入拖拽排课模式
const enterDragArrangeMode = () => {
	expandedMore.value = false
	isDragArrangeMode.value = true
	isFilterExpanded.value = false // 默认展开筛选条件
}

// 退出拖拽排课模式
const exitDragArrangeMode = () => {
	isDragArrangeMode.value = false
	isFilterExpanded.value = false
	// 如果处于全屏状态，退出全屏
	if (document.fullscreenElement) {
		document.exitFullscreen().catch(() => {})
	}
}

// 切换筛选条件展开状态
const toggleFilterExpanded = () => {
	isFilterExpanded.value = !isFilterExpanded.value
}

// 在父组件中统一处理事件，避免子组件重复监听
onMounted(() => {
	// 监听进入拖拽排课模式
	event.on('enter-drag-arrange-mode', enterDragArrangeMode)
	// 监听退出拖拽排课模式
	event.on('exit-drag-arrange-mode', exitDragArrangeMode)
	// 监听切换筛选条件展开状态
	event.on('toggle-filter-expanded', toggleFilterExpanded)
	
	// 监听编辑日程请求
	event.on('request-edit-schedule', (params: any) => {
		if (!addScheduleFormRef.value) return
		addScheduleFormRef.value.open({
			ID: params.ID,
			opType: 2,
			byPlan: false,
		}).then(() => {})
	})
	
	// 监听编辑排课请求
	event.on('request-edit-arrange', (params: any) => {
		if (!editArrangeFormRef.value) return
		editArrangeFormRef.value.open({
			CampusID: params.CampusID,
			ID: params.ID,
			ShiftID: params.ShiftID,
		}).then(() => {})
	})

	event.on('update-schedule-query', (params: any) => { 
        condition.value.Query = params
        handleSearch()
	})
})

// 组件卸载时清理事件监听器
onUnmounted(() => {
	try {
		event.off && event.off('enter-drag-arrange-mode')
		event.off && event.off('exit-drag-arrange-mode')
		event.off && event.off('toggle-filter-expanded')
		event.off && event.off('request-edit-schedule')
		event.off && event.off('request-edit-arrange')
		event.off && event.off('update-schedule-query')
	} catch (_) { }
})
</script>
<style lang="scss" scoped>
.wtwo-schedule-manage {
	flex: 1 1 auto;
	display: flex;
	flex-direction: column;
	// overflow: hidden;
	height: 100%;

	.page-area {
		display: flex;
		flex-direction: column;
		.tab-op-box{
			position: absolute;
			right: 12px;
			top: 8px;
			z-index: 1;
		}
	}
	
	// 组件容器 - 相对定位，让骨架屏和组件可以重叠
	.component-wrapper {
		position: relative;
		flex: 1;
		display: flex;
		flex-direction: column;
		overflow: hidden;
		min-height: 0; // 允许flex子项正确收缩
		
		// 拖拽排课全屏模式 - 使用 flex 布局自动占据剩余空间
		&.drag-arrange-fullscreen {
			position: fixed;
			top: 0;
			left: 0;
			right: 0;
			bottom: 0;
			z-index: 1000;
			background: #F0F2F5;
			display: flex;
			flex-direction: column;
			overflow: auto;
		}
	}
	
	// 骨架屏样式 - 绝对定位覆盖在组件上方
	.component-skeleton {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		padding: 20px;
		background: #fff;
		border-radius: 4px;
		z-index: 10;
		overflow: hidden;
	}
	
	// 组件隐藏状态 - 在骨架屏下方，避免闪烁
	.component-hidden {
		visibility: hidden;
		pointer-events: none;
	}
	
	// 拖拽排课模式样式
	.search-result-box {
		position: relative;
		
		// 拖拽排课全屏模式 - 保持正常文档流，直接撑开布局
		&.drag-arrange-fullscreen {
			background: #fff;
			box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
			transition: max-height 0.3s ease, opacity 0.3s ease;
			max-height: 1000px;
			opacity: 1;
		}
		
		// 隐藏筛选条件
		&.filter-hidden {
			max-height: 0;
			opacity: 0;
			overflow: hidden;
			padding: 0;
		}
	}
}
</style>

