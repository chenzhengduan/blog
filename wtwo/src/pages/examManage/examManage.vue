<template>
	<div class="wtwo-exam-manage">
		<div class="breadcrumb">
			<span class="breadcrumb-txt">教务管理</span>
			<span class="breadcrumb-split">/</span>
			<span class="breadcrumb-main-txt">考试管理</span>
		</div>
		<div class="page-area with-breadcrumb">
			<el-tabs v-model="activeName">
				<el-tab-pane v-for="(item, index) in tabs" :key="index" :label="item.Name" :name="item.PageName"
					:lazy="false">
					<template #label>
						<div class="tab-bar">{{ item.Name }}</div>
					</template>
				</el-tab-pane>
			</el-tabs>
			<!-- 考试管理和成绩查询公用筛选条件 -->
			<div class="search-result-box" v-if="
				activeName == 'examManageList' ||
				activeName == 'scoreQueryList'
			">
				<div class="search-area">
					<div class="search-fixed-area">
						<!-- 常用筛选显示区域 -->
						<exam-filter v-for="filter in visibleCommonFilters" :key="filter.key" :filter="filter"
							:condition="condition" :show-remove-button="visibleCommonFilters.length > 1"
							:classroomList="classroomList" @remove="removeCommonFilter" @select-course="selectCourse"
							@select-class="selectClass" @select-exam-item="selectExamItem" @handle-shift-change="handleShiftChange"
							@handle-course-change="handleCourseChange" @handle-class-change="handleClassChange" @handle-exam-item-change="handleExamItemChange" @handle-student-change="handleStudentChange"
							@handle-head-master-change="handleHeadMasterChange" @select-head-master="selectHeadMaster"
							@select-teacher="selectTeacher" @select-assistant="selectAssistant"
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
						<el-button type="primary" @click="handleSearch">查询</el-button>
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
								常用筛选条件（{{ commonFilters.length }}/4）
							</div>
							<div class="search-area common-filters-area">
								<exam-filter v-for="filter in visibleCommonFilters" :key="filter.key"
									:filter="filter" :condition="condition" :disabled="true" :is-common-filter="true"
									:show-remove-button="visibleCommonFilters.length > 1" 									:classroomList="classroomList"
									@remove="removeCommonFilter" @select-course="selectCourse"
									@select-class="selectClass" @select-exam-item="selectExamItem" @handle-shift-change="handleShiftChange"
									@handle-course-change="handleCourseChange" @handle-class-change="handleClassChange" @handle-exam-item-change="handleExamItemChange"
									@handle-student-change="handleStudentChange"
									@handle-head-master-change="handleHeadMasterChange"
									@select-head-master="selectHeadMaster" @select-teacher="selectTeacher"
									@select-assistant="selectAssistant" @select-student="selectStudent"
									@update:condition="(newCondition: any) => condition = newCondition" />
							</div>
							<div class="common-edit-title">更多筛选条件</div>
						</template>
						<div class="all-filter-area" :class="{ 'is-edit': isEditCommon }">
							<!-- 所有筛选条件直接显示，不分类 -->
							<div class="search-area">
								<exam-filter v-for="filter in filterGroup" :key="filter.key" :filter="filter"
									:condition="condition" :disabled="isEditCommon" :show-common-flag="isEditCommon"
									:is-common-filter-flag="isCommonFilter(filter.key)"
									:classroomList="classroomList" @select-course="selectCourse"
									@select-class="selectClass" @select-exam-item="selectExamItem" @handle-shift-change="handleShiftChange"
									@handle-course-change="handleCourseChange" @handle-class-change="handleClassChange" @handle-exam-item-change="handleExamItemChange"
									@handle-student-change="handleStudentChange"
									@handle-head-master-change="handleHeadMasterChange"
									@select-head-master="selectHeadMaster" @select-teacher="selectTeacher"
									@select-assistant="selectAssistant" @select-student="selectStudent"
									@select-master="selectMaster" @toggle-common="toggleCommonFilter"
									@update:condition="(newCondition: any) => condition = newCondition" />
							</div>
						</div>
					</div>
					<div class="all-filter-panel-footer" v-if="!isEditCommon">
						<div class="footer-right">
							<el-link type="primary" underline="never" @click="showMore"
								class="mr-12px">收起&nbsp;<el-icon>
									<ArrowUp />
								</el-icon></el-link>
							<el-button type="primary" @click="handleSearch">查询</el-button>
							<el-button @click="handleReset">重置</el-button>
						</div>
					</div>
				</div>
			</div>
			<component v-if="activeName" :is="comp[activeName]" ref="activeComponentRef" :key="activeName" @refreshExamList="handleSearch"></component>
		</div>
		<chooseCourse ref="chooseCourseRef"></chooseCourse>
		<chooseClass ref="chooseClassRef"></chooseClass>
		<chooseExamItem ref="chooseExamItemRef"></chooseExamItem>
		<chooseEmpAsync ref="chooseEmpAsyncRef"></chooseEmpAsync>
		<chooseStudent ref="chooseStudentRef"></chooseStudent>
		
	</div>
</template>
<script lang="ts" setup>
import { computed, h, nextTick, onMounted, ref, watch, defineAsyncComponent } from 'vue'
import { useCurrentCampuses } from '@/store'
import ExamFilter from '../examManage/components/exam-filter.vue'
import chooseCourse from '@/components/popup/chooseCourse.vue'
import chooseClass from '@/components/popup/chooseClass.vue'
import chooseExamItem from '@/components/popup/chooseExamItem.vue'
import chooseEmpAsync from '@/components/popup/chooseEmpAsync.vue'
import chooseStudent from '@/components/popup/chooseStudent.vue'

import {
	ArrowDown,
	ArrowUp,
	Close
} from '@element-plus/icons-vue'
import { cloneDeep } from 'lodash'
import {  } from '@/api/exam'
import { dayjs } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { weekDiff } from '@/utils'

interface IKeyValueMap {
	[key: string]: any
}
	const comp: IKeyValueMap = {
	examManageList: defineAsyncComponent(() => import('./examManageList/examManageList.vue')),
	scoreQueryList: defineAsyncComponent(() => import('./scoreQueryList/scoreQueryList.vue')),
}
const activeName = ref('')

const ExamQuery = window.$xgj.op('ExamQuery'); // 考试管理
const ExamScoreQueryNew = window.$xgj.op('ExamScoreQueryNew'); // 成绩查询

const tabs = computed(() => {
	const list: Array<{ Name: string; PageName: string }> = []
	if (ExamQuery) {
		list.push({ Name: '考试管理', PageName: 'examManageList' })
	}
	if (ExamScoreQueryNew) {
		list.push({ Name: '成绩查询', PageName: 'scoreQueryList' })
	}
	return list
})

// 当权限变更或初始化时，确保选中首个可见页签
watch(tabs, (newTabs) => {
	const allowed = newTabs.map(t => t.PageName)
	if (!allowed.includes(activeName.value)) {
		activeName.value = newTabs[0]?.PageName || ''
	}
}, { immediate: true })
watch(activeName, (newVal) => {
	if (window.microApp) {
		console.log('newVal', newVal)
		// let isSingleCampus = newVal=='examManageList';
		console.log('发送消息：isSingleCampus', false)
		window.microApp.dispatch({type:'examTab: isSingleCampus',isSingleCampus: false})
	}
}, { immediate: true })
// 上方已定义 configs

const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})

watch(
	currentCampus,
	(newVal, oldVal) => {
		condition.value.ClassID = EMPTYGUID
		condition.value.ShiftID = EMPTYGUID
		classList.value = []
		courseList.value = []
		classroomList.value = []
		handleSearch()
	},
	{
		deep: true,
	}
)

const defaultCondition: any = {
	examName: '', // 考试名称
	examTypes: [], // 考试类型ID列表
	// examModes: 0, // 考试模式：0=全部，1=线下考试，2=线上考试
	subjectIds: [], // 考试科目ID列表
	gradeIds: [], // 考试年级ID列表
	courseIds: [], // 适用课程ID列表
	examDate: [dayjs().subtract(3, 'month').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
	dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss')], // 考试日期 ：默认近3个月
	publicTime: [], // 成绩公布时间
	// 新增字段
	courseSelected: [], // 课程选中项数组
}

const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))

// 添加数据列表作为独立的 ref
const classList = ref<any[]>([])
const courseList = ref<any[]>([])
const classroomList = ref<any[]>([])

const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
const isEditCommon = ref(false)

// 常用筛选相关
const commonFilters = ref<any[]>([])
const originalCondition = ref<any>({})
const originalCommonFilters = ref<any[]>([])

// 可见的常用筛选条件（根据当前 tab 过滤）
const visibleCommonFilters = computed(() => {
	if (activeName.value === 'scoreQueryList') {
		// 在成绩查询 tab 中，过滤掉 dateRange 筛选条件
		return commonFilters.value.filter(filter => filter.key !== 'dateRange')
	}
	return commonFilters.value
})

// 筛选项配置
const filterConfigs = computed(() => {
	const baseConfigs = [
		{ key: 'examName', label: '考试名称', type: 'input' },
		{ key: 'examTypes', label: '考试类型', type: 'select' },
		// { key: 'examModes', label: '考试模式', type: 'select' },
		{ key: 'subjectIds', label: '考试科目', type: 'select' },
		{ key: 'gradeIds', label: '考试年级', type: 'select' },
		{ key: 'courseIds', label: '适用课程', type: 'input-tag' },
		{ key: 'examDate', label: '考试日期', type: 'date' },
		{ key: 'publicTime', label: '成绩公布时间', type: 'date-range' },
	]
	const baseConfigs2 = [
		// { key: 'studentName', label: '学员名称', type: 'input' },
		{ key: 'studentIds', label: '学员', type: 'input-tag' },
		{ key: 'examName', label: '考试名称', type: 'input' },
		{ key: 'examTypes', label: '考试类型', type: 'select' },
		// { key: 'examModes', label: '考试模式', type: 'select' },
		{ key: 'subjectIds', label: '考试科目', type: 'select' },
		// { key: 'gradeIds', label: '学员年级', type: 'select' },  // 暂时不使用学员年级筛选
		{ key: 'classIds', label: '考试班级', type: 'input-tag' },
		{ key: 'examSubjectIds', label: '考试项目', type: 'input-tag' },
		{ key: 'examDate', label: '考试日期', type: 'date' },
	]

	return activeName.value === 'examManageList' ? baseConfigs : baseConfigs2
})

const filterGroup = computed(() => {
	// 直接返回所有筛选条件，不分类
	const allFilters = filterConfigs.value.map(config => ({
		key: config.key,
		label: config.label,
		type: config.type,

	}))
	
	// 在成绩查询中过滤掉 dateRange
	// if (activeName.value === 'scoreQueryList') {
	// 	return allFilters.filter(filter => filter.key !== 'dateRange')
	// }
	
	return allFilters
})

const chooseCourseRef = ref()
const chooseClassRef = ref()
const chooseExamItemRef = ref()
const chooseGradeRef = ref()
const chooseSubjectRef = ref()
const chooseEmpAsyncRef = ref()
const chooseStudentRef = ref()

// 数据选择器方法
function selectCourse() {
	chooseCourseRef.value
		?.open({
			multi: true,
			choosed: condition.value.courseSelected,
			showCampus: true,
			showShiftType: true
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.courseIds = res.data.map((item: any) => item.ID)
				condition.value.courseSelected = res.data
			}
		})
}

function selectClass() {
	chooseClassRef.value
		?.open({
			multi: true,
			choosed: condition.value.classSelected,
			showCampus: true,
			showShiftType: true
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.classIds = res.data.map((item: any) => item.ID)
				condition.value.classSelected = res.data
			}
		})
}

function selectExamItem() {
	chooseExamItemRef.value
		?.open({
			multi: true,
			choosed: condition.value.examItemSelected,
			showCampus: false,
			showShiftType: false,
			// maxNum: 10
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.examSubjectIds = res.data.map((item: any) => item.ID)
				condition.value.examItemSelected = res.data
			}
		})
}

// 添加排课管理中的方法
function handleShiftChange(val: any) {
	console.log('上课课程 change 事件:', val)
	if (val.length) {
		condition.value.ShiftID = val[0].ID
		condition.value.ShiftName = val[0].Name
		condition.value.courseSelected = val
	} else {
		condition.value.ShiftID = EMPTYGUID
		condition.value.courseSelected = []
	}
}


function handleCourseChange(val: any) {
	console.log('适用课程 change 事件:', val)
	if (val.length) {
		condition.value.courseIds = val.map((item: any) => item.ID)
		condition.value.courseSelected = val
	} else {
		condition.value.courseIds = []
		condition.value.courseSelected = []
	}
}

function handleClassChange(val: any) {
	console.log('适用班级 change 事件:', val)
	if (val.length) {
		condition.value.classIds = val.map((item: any) => item.ID)
		condition.value.classSelected = val
	} else {
		condition.value.classIds = []
		condition.value.classSelected = []
	}
}

function handleExamItemChange(val: any) {
	console.log('考试项目 change 事件:', val)
	if (val.length) {
		condition.value.examSubjectIds = val.map((item: any) => item.ID)
		condition.value.examItemSelected = val
	} else {
		condition.value.examSubjectIds = []
		condition.value.examItemSelected = []
	}
}

function handleStudentChange(val: any) {
	console.log('学员 change 事件:', val)
	if (val.length) {
		condition.value.studentIds = val.map((item: any) => item.ID)
		condition.value.studentSelected = val

	} else {
		condition.value.studentSelected = []
		condition.value.studentIds = []
	}
}

function handleHeadMasterChange(val: any) {
	console.log('班主任 change 事件:', val)
	if (val.length) {
		condition.value.headerMasterName = val[0].Name
		condition.value.headerMasterSelected = val
	} else {
		condition.value.headerMasterSelected = []
	}
}

function selectHeadMaster() {
	chooseEmpAsyncRef.value
		?.open({
			multi: false,
			required: true,
			showContainOut: true,
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.headerMasterName = res.data.Name
				condition.value.headerMasterSelected = [res.data]
			}
		})
}

function selectTeacher() {
	chooseEmpAsyncRef.value
		?.open({
			multi: true,
			selected: condition.value.teacherList,
			showContainOut: true,
		})
		.then((res: any) => {
			condition.value.teacherList = res.data || []
		})
}

function selectAssistant() {
	chooseEmpAsyncRef.value
		?.open({
			multi: true,
			selected: condition.value.assistantList,
			showContainOut: true,
		})
		.then((res: any) => {
			condition.value.assistantList = res.data || []
		})
}

function selectMaster() {
	chooseEmpAsyncRef.value
		?.open({
			multi: true,
			selected: condition.value.masterList,
			showContainOut: true,
		})
		.then((res: any) => {
			condition.value.masterList = res.data || []
		})
}

function selectStudent() {
	chooseStudentRef.value
		?.open({
			multi: true,
			choosed: condition.value.studentSelected,
		})
		.then((res: any) => {
			console.log('selectStudent:', res)
			if (res.data) {
				condition.value.studentIds = res.data.map((item: any) => item.ID)
				condition.value.studentSelected = res.data
			}
		})
}


const expandedMore = ref(false)
const activeComponentRef = ref()

// 标记是否已经执行过查询，用于避免重复查询
const hasSearchedOnce = ref(false)

function showMore() {
	expandedMore.value = !expandedMore.value
}

// 监听 tab 切换，确保组件挂载后能正确触发查询
watch(activeName, (newVal, oldVal) => {
	// 每次 tab 切换都重新设置 hasSearchedOnce 为 false
	hasSearchedOnce.value = false
	expandedMore.value = false
	// 等待下一个 tick，确保组件已经挂载
	nextTick(() => {
		if (activeComponentRef.value && typeof activeComponentRef.value.search === 'function') {
			handleSearch()
			hasSearchedOnce.value = true
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
})

// 处理重置按钮点击事件
function handleReset() {
	// 重置所有筛选条件到默认值
	Object.assign(condition.value, cloneDeep(defaultCondition))
	// 重置后自动查询
	handleSearch()
}

// 处理查询按钮点击事件
function handleSearch() {
	let params: any = {}

	// 直接使用考试相关的筛选条件，排除 courseSelected
	const { courseSelected, ...searchCondition } = condition.value
	Object.assign(params, searchCondition)

	// 处理考试模式参数
	if (condition.value.examModes === 0) {
		params.examModes = [] // 全部：传空数组
	} else if (condition.value.examModes === 1) {
		params.examModes = [1] // 线下：传[1]
	} else if (condition.value.examModes === 2) {
		params.examModes = [2] // 线上：传[2]
	}

	// 处理课程ID列表
	params.courseIds = condition.value.courseIds || []

	// 处理成绩公布时间参数
	if (condition.value.publicTime && Array.isArray(condition.value.publicTime) && condition.value.publicTime.length === 2) {
		params.publicTimeStart = condition.value.publicTime[0]
		params.publicTimeEnd = condition.value.publicTime[1]
	} else {
		params.publicTimeStart = ''
		params.publicTimeEnd = ''
	}
	// 处理考试日期参数
	if (condition.value.examDate && Array.isArray(condition.value.examDate) && condition.value.examDate.length === 2) {
		params.examDateStart = condition.value.examDate[0]
		params.examDateEnd = condition.value.examDate[1]
	} else {
		params.examDateStart = ''
		params.examDateEnd = ''
	}

	// 处理校区ID列表
	params.campusIds = currentCampus.value
		? currentCampus.value.split(',')
		: []

	// 关闭展开的条件搜索框
	expandedMore.value = false

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
		if (commonFilters.value.length < 4) {
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
		'examManage_commonFilters',
		JSON.stringify(commonData)
	)
}

// 从本地存储加载常用筛选
function loadCommonFiltersFromStorage() {
	try {
		const stored = localStorage.getItem('examManage_commonFilters')
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
	const defaultKeys = ['examName', 'examDate']
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


onMounted(() => {
	loadCommonFiltersFromStorage()
	// 延迟执行初始查询，确保默认 tab 的组件已经挂载完成
	nextTick(() => {
		// handleSearch()
	})
})

</script>
<style lang="scss" scoped>
.wtwo-exam-manage {
	flex: 1 1 auto;
	display: flex;
	flex-direction: column;
	// overflow: hidden;
	height: 100%;

	.page-area {
		display: flex;
		flex-direction: column;

		.all-filter-panel {
			.all-filter-panel-footer {
				justify-content: flex-end;
			}
		}
	}
}

.common-edit-title {
	font-size: 14px;
	font-weight: 500;
	color: #606266;
	margin-bottom: 12px;
	padding-bottom: 8px;
	border-bottom: 1px solid #e4e7ed;
}

.common-filters-area {
	margin-bottom: 20px;
}

.common-filters-area .search-item {
	margin-bottom: 12px;
}

.common-filters-area .search-item .el-button {
	margin-left: 8px;
}

.ml-16px {
	margin-left: 16px;
}

/* 考试管理页面特殊处理考试日期选择器样式 */
:deep(.search-date-wrap.wtwo-date-editor:not(.twice-width)) {
	padding-left: 0px !important;
}
</style>
