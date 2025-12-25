<template>
	<!-- 选班级 -->
	<el-dialog
		v-model="dialogVisible"
		:title="transToConfigDescript(popTitle)"
		:width="multi ?'900px':'800px'"
		:close-on-click-modal="false"
		:append-to-body="true"
		:destroy-on-close="true"
		:align-center="true"
		class="chooseClassExam"
		draggable
		@close="close"
	>
		<div
			class="box-wrapper-table"
			:class="{ 'multi-choosed-box-wrapper': multi }"
		>
			<div class="fixed-table-box">
				<div class="modal-filter multi-conditional">
					<div class="filter-item mr-20px w-534px">
						<el-input
							v-model.trim="condition.Query"
							:placeholder="
								transToConfigDescript('请输入班级名称')
							"
							class="class-input"
							ref="classInputRef"
							@keyup.native.enter="getFirstPage"
						></el-input>
					</div>
					<div class="filter-item">
						<el-button type="primary" @click="getFirstPage" :disabled="loading"
							>查询</el-button
						>
						<el-button plain @click="reset" :disabled="loading">重置</el-button>
					</div>
					<div class="filter-item flex-center" @click.stop="showMore">
						<el-link type="primary" underline="never"
							>{{ expandedMore ? '收起' : '展开' }}筛选&nbsp;
							<el-icon
								><ArrowUp v-if="expandedMore" /><ArrowDown
									v-else
							/></el-icon>
						</el-link>
					</div>
					<div v-show="expandedMore" class="advanced-filter-wrap">
						<div class="filter-item" v-if="showCampus">
							<el-select
								v-model="condition.campus"
								placeholder="不限"
								filterable
								clearable
								:multiple="campusMultiple"
								collapse-tags
								@change="getFirstPage"
							>
								<template #prefix
									><p class="search-input-label">
										{{ transToConfigDescript('所属校区') }}
									</p></template
								>
								<el-option
									v-for="item in isAllCampus
										? campusList
										: userCampuses"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
							</el-select>
						</div>
						<div class="filter-item">
							<input-tag
								label="课程名称"
								placeholder="请选择"
								:selected="condition.courseSelected"
								:isLine="true"
								:multiple="true"
								@choose="selectCourse"
								@change="getFirstPage"
							>
								<template #btn-icon>
									<el-icon size="18px">
										<svg aria-hidden="true">
											<use xlink:href="#w2-xuanke"></use>
										</svg>
									</el-icon>
								</template>
							</input-tag>
						</div>
						<div class="filter-item">
							<el-select
								v-model="condition.Grade"
								placeholder="不限"
								filterable
								clearable
								@change="getFirstPage"
							>
								<template #prefix
									><p class="search-input-label">
										年级
									</p></template
								>
								<el-option
									v-for="item in SHIFT_GRADE"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
							</el-select>
						</div>
						<div class="filter-item">
							<input-tag
								label="班主任"
								placeholder="请选择"
								:selected="condition.headMasterId"
								:isLine="true"
                                :multiple="false"
								@choose="selectTeacher"
								@change="getFirstPage"
							>
								<template #btn-icon>
									<el-icon size="18px">
										<svg aria-hidden="true">
											<use
												xlink:href="#w2-xuanren"
											></use>
										</svg>
									</el-icon>
								</template>
							</input-tag>
						</div>
					</div>
				</div>
				<div
					class="table-wrap"
					v-loading="loading"
					ref="tableContainerRef"
				>
					<el-table
						ref="customTable"
						:data="list"
						:highlight-current-row="!multi"
						style="width: 100%"
						@row-dblclick="confirmChoose"
						@row-click="chooseIt"
						:height="tableHeight"
					>
						<template #empty>
							<el-empty
								description="暂无数据"
								:image="globalData.emptyPng"
								:image-size="100"
							/>
						</template>
						<el-table-column
							v-if="multi"
							prop="ID"
							key="ID"
							width="30px"
							align="center"
							fixed
						>
							<template #header="scope">
								<el-checkbox v-model="allChecked"></el-checkbox>
							</template>
							<template #default="scope">
								<el-checkbox
									@click.native.prevent
									:model-value="!!checkedList.find((el:any)=>{return el==scope.row.ID})"
								></el-checkbox>
							</template>
						</el-table-column>
						<el-table-column
							fixed
							prop="Name"
							:label="transToConfigDescript('班级名称')"
							show-overflow-tooltip
							width="200px"
						></el-table-column>
						<el-table-column
							prop="CampusName"
							:label="transToConfigDescript('所属校区')"
							width="120"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							prop="ShiftName"
							:label="transToConfigDescript('课程名称')"
							show-overflow-tooltip
							width="160"
						></el-table-column>
						
						
						<el-table-column
							prop="HeadMasterUserName"
							label="班主任"
							width="100"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							prop="TeacherName"
							:label="transToConfigDescript('任课老师')"
							width="100"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							prop="AssistantTeacherName"
							label="助教"
							width="100"
							show-overflow-tooltip
						></el-table-column>
						
						
						<el-table-column
							prop="CourseTime"
							label="上课时间"
							width="160"
						></el-table-column>
					</el-table>
				</div>
				<div class="pageination-wrapper flex-end" v-if="list.length">
					<el-pagination
						@size-change="handleSizeChange"
						@current-change="handleCurrentChange"
						:current-page.sync="page.PageIndex"
						:page-sizes="[10, 20, 50, 100, 200]"
						:page-size="page.PageSize"
						layout="total, sizes, prev, jumper ,next"
						:total="page.TotalCount"
						size="small"
					>
					</el-pagination>
				</div>
			</div>
			<div class="choosed-result-wrapper" v-if="multi && false">
				<div class="choosed-result">
					<div class="choosed-result-title">
						<span>已选择：{{ selected.length }}</span>
						<el-link
							type="primary"
							underline="never"
							@click="removeAll"
							>清空</el-link
						>
					</div>
					<div class="choosed-result-content">
						<ul>
							<li
								v-for="(item, index) in selected"
								:key="item.ID"
							>
								<span
									class="choosed-item-name"
									:title="item.Name"
									>{{ item.Name }}</span
								>
								<a
									class="del-btn"
									@click="remove(item.ID, index)"
									><el-icon><deleteSVG></deleteSVG></el-icon
								></a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</div>
		<template #footer>
			<div :class="multi ? 'flex-end' : 'flex-between'">
				<div class="single-choosed-wrapper" v-if="!multi">
					<span class="single-choosed-name"
						>已选择：{{ oneChoosed ? oneChoosed.Name : '' }}</span
					>
				</div>
				<div>
					<el-button plain @click="close">取消</el-button>
					<el-button type="primary" @click="submit">确定</el-button>
				</div>
			</div>
		</template>
		<chooseEmpAsync ref="chooseEmpAsyncRef"></chooseEmpAsync>
		<chooseCourse ref="chooseCourseRef"></chooseCourse>
	</el-dialog>
</template>

<script lang="ts" setup>
import $ from 'jquery'
import { storeToRefs } from 'pinia'
import {
	useConfigs,
	useCurrentCampuses,
	useUserCampuses,
	useYears,
} from '@/store'
import { useDictFieldsStore } from '@/store/dict'

const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)

import { queryClass } from '@/api'
import { transToConfigDescript, formatNumber } from '@/utils/filters/filters'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { cloneDeep, multiply } from 'lodash'

import deleteSVG from '@/assets/svg/delete.svg'
import { computed, getCurrentInstance, h, nextTick, ref, shallowRef } from 'vue'
import { TableInstance } from 'element-plus'
import { useOrganizationStore } from '@/store/organization'
import { checkPageIndex } from '@/utils'
import InputTag from '@/components/common/input-tag/inputTag.vue'
import chooseCourse from '@/components/popup/chooseCourse.vue'

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const configs = computed(() => {
	return useConfigs().configs
})
const organizationStore = useOrganizationStore()
const { campusList } = storeToRefs(organizationStore)

// 获取适用校区
const fixedCampusIds = ref<string[]>([]);
// 根据适用校区获取固定校区列表
const userCampuses = computed(() => {
    // 根据适用校区 CampusIds 过滤出对应校区列表
    let userCampuses: any = useUserCampuses().userCampuses
    if (fixedCampusIds.value.length > 0) {
        userCampuses = userCampuses.filter((item: any) => fixedCampusIds.value.includes(item.ID))
    }
    return userCampuses
})

const SHIFT_GRADE = computed(() => {
	return dictFields.value('SHIFT_GRADE')
})

const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})

const dialogVisible = ref(false)
// 弹框参数 start
const multi = ref(false)
const popTitle = ref('选择班级')
const choosed = ref([] as any)
const showCampus = ref(false)
const required = ref(false)
// const courseID = ref('')
const isAllCampus = ref(false) //默认展示可操作校区
const checkClassStudentCount = ref(false) //检查班级人数是否达排课标准
const campusMultiple=ref(false)  // 控制校区选择器是否多选

// 弹框参数 end

const selected = ref([] as any)
const selectedID = ref([] as any)
const expandedMore = ref(false)
const loading = ref(false)
const list = ref([] as any)
const checkedList = ref([] as any)
const oneChoosed = ref(null as any)
const page = ref({
	PageIndex: 1,
	PageSize: 20,
	TotalCount: 0,
})
const defaultCondition: any = {
	campus: [],
	Grade: '',
	Query: '',
	shiftType: 7, 
	countStudents: 1,
	teacherId: '',
	Teachers: [],
	finished:0,
	isFilterByRight: 0, // 是否按权限过滤：0否，1只查询自己名下学员的班级，2只查询自己名下的班级
	classStatusSelect: -1, // 开班状态
	isFee:0,
	isFilterDrainageClass:0,
	isNoFinished:0,
	headMasterId:[],
	courseSelected: [] // 课程选择
}
const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const newCondition=ref<typeof defaultCondition>(cloneDeep(defaultCondition))
function showMore() {
	expandedMore.value = !expandedMore.value
	setHeight()
}

const chooseEmpAsyncRef = ref()
const chooseCourseRef = ref()
const classInputRef = ref()
function selectTeacher() {
	chooseEmpAsyncRef.value
		?.open({
			multi: false,
			selected: condition.value.headMasterId.length > 0 ? condition.value.headMasterId[0] : null,
		})
		.then((res: any) => {
			console.log('res', res)
			
			// 单选模式，将单个对象包装成数组
			condition.value.headMasterId = res.data ? [res.data] : []
			getFirstPage()
		})
}

// 课程选择相关函数
function selectCourse() {
	chooseCourseRef.value?.open({
		multi: true,
		choosed: condition.value.courseSelected || [],
		showCampus: true,
		showShiftType: true,
		campusID: condition.value.campus?.join(',') || '',
		showClassStatusFilter: true,
		condition: {
			finished: 0
		}
	}).then((res: any) => {
		if (res && res.data) {
			condition.value.courseSelected = res.data;
			getFirstPage();
		}
	}).catch((err: any) => {
		console.error('选择课程失败:', err);
	});
}

const allChecked = computed({
	get: () => {
		let length = checkedList.value.length
		return length === 0
			? false
			: checkedList.value.length === list.value.length
	},
	set: (val: boolean) => {
		if (val) {
			let ids: any = []
			list.value.forEach((item: any) => {
				ids.push(item.ID)
				if (selectedID.value.indexOf(item.ID) === -1) {
					selectedID.value.push(item.ID)
					selected.value.push(item)
				}
			})
			checkedList.value = ids
		} else {
			list.value.forEach((item: any) => {
				let index = selectedID.value.indexOf(item.ID)
				if (index !== -1) {
					selectedID.value.splice(index, 1)
					selected.value.splice(index, 1)
				}
			})
			checkedList.value = []
		}
	},
})

//多选选择
function chooseIt(row: any, column: any) {
	let item = row,
		index = row.$index
	if (multi.value) {
		let checkedIndex = checkedList.value.indexOf(item.ID)
		if (checkedIndex === -1) {
			checkedList.value.push(item.ID)
			selectedID.value.push(item.ID)
			selected.value.push(item)
		} else {
			checkedList.value.splice(checkedIndex, 1)
			let selectedIndex = selectedID.value.indexOf(item.ID)
			selected.value.splice(selectedIndex, 1)
			selectedID.value.splice(selectedIndex, 1)
		}
	} else {
		customTable.value?.setCurrentRow(row)
		oneChoosed.value = item
	}
}

//单选双击选择
function confirmChoose(item: any) {
	if (!multi.value) {
		oneChoosed.value = item
		submit()
	}
}

//移除选中项
function remove(id: string, index: number) {
	selectedID.value.splice(index, 1)
	selected.value.splice(index, 1)
	let index1 = checkedList.value.indexOf(id)
	if (index1 != -1) {
		checkedList.value.splice(index, 1)
	}
}

//清空选中项
function removeAll() {
	checkedList.value = []
	selected.value = []
	selectedID.value = []
}

//确认
function submit() {
	let params: any = {}
	if (multi.value) {
		if (required.value && selected.value.length === 0) {
			ElMessage({
				message: '请至少选择一个班级。',
				type: 'warning',
			})
			return
		}
		params.data = selected.value
	} else {
		if (required.value && !oneChoosed.value) {
			ElMessage({
				message: '请先选择班级。',
				type: 'warning',
			})
			return
		}
		if(checkClassStudentCount.value){//检查班级人数是否达排课标准
			if(oneChoosed.value.MinStudentsAmount !== 0 && oneChoosed.value.MinStudentsAmount > oneChoosed.value.FactStudentCount && oneChoosed.value.IsOneToOne != 1){
				ElMessage({
					message: '班级人数未达到排课标准，不能进行排课（最低开班人数：' + oneChoosed.value.MinStudentsAmount + '人，实际入班人数：' + oneChoosed.value.FactStudentCount +'人）。',
					type: 'warning',
				})
				return
			}
		}
		params.data = oneChoosed.value
	}
	
	_resolve(params)
	close()
}

//查第一页
function getFirstPage() {
	page.value.PageIndex = 1
	funcQuery()
}
function handleSizeChange(val: any) {
	page.value.PageSize = val
	page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
	funcQuery()
}
function handleCurrentChange(val: any) {
	page.value.PageIndex = val
	funcQuery()
}

function reset() {
	restCondition()
	getFirstPage()
}
function restCondition(){
	Object.assign(condition.value,cloneDeep(newCondition.value))
}
function resetDefaultCondition(){
    condition.value = cloneDeep(defaultCondition)
}

//取消
function close() {
	resetDefaultCondition()
	removeAll()
	oneChoosed.value = null
	dialogVisible.value = false
	_reject()
}

//查询
function funcQuery() {
	loading.value = true
	list.value = []
	checkedList.value = []
	let params: any = {
		desc: 0,
		sort: 'Name',
		// Shift: courseID.value || '',
	}
	Object.assign(params, {
		...page.value,
		...condition.value,
	})
	if (!showCampus.value) {
		// 处理校区参数，支持多选
		if (Array.isArray(condition.value.campus) && condition.value.campus.length > 0) {
			params.campus = condition.value.campus.join(',')
		} else if (condition.value.campus) {
			params.campus = condition.value.campus
		} else {
			params.campus = currentCampus.value
		}
	}
	
	// 处理班主任参数，将数组转换为字符串
	if (Array.isArray(params.headMasterId) && params.headMasterId.length > 0) {
		params.headMasterId = params.headMasterId.map((item: any) => item.ID).join(',')
	} else if (params.headMasterId) {
		params.headMasterId = params.headMasterId
	} else {
		params.headMasterId = ''
	}

	// 处理课程选择数据
	if (params.courseSelected && params.courseSelected.length > 0) {
		params.Shift = params.courseSelected.map((item: any) => item.ID);
	}
	delete params.courseSelected
	queryClass(params)
		.then((res) => {
			list.value = res.Data || []
			let checkedArr: any = []
			list.value.forEach((item: any) => {
				if (selectedID.value.indexOf(item.ID) !== -1) {
					checkedArr.push(item.ID)
				}
			})
			checkedList.value = checkedArr
			Object.assign(page.value, {
				PageIndex: res.PageIndex,
				PageCount: res.PageCount,
				PageSize: res.PageSize,
				TotalCount: res.TotalCount,
			})

			setHeight()
		})
		.finally(() => {
			loading.value = false
		})
}

const tableHeight = ref(408)
const tableContainerRef = ref()
const customTable = ref<TableInstance>()
function setHeight() {
	if (tableContainerRef.value) {
		tableHeight.value = 3000 //撑开一下需要计算高度
		nextTick(() => {
			const h = tableHeight.value
			let tableH = list.value.length
				? (list.value.length + 1) * 37 + 1
				: 408
			tableHeight.value = tableH

			// if(tableHeight.value>$(tableContainerRef.value)[0].clientHeight){
			tableHeight.value = $(tableContainerRef.value)[0].clientHeight
			// }
			if (h !== tableHeight.value && customTable.value) {
				customTable.value.doLayout()
			}
		})
	}
}

let _resolve = null as any
let _reject = null as any
function open(params: any) {
	let promise = new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
	})
	dialogVisible.value = true
	multi.value = params.multi || false
	popTitle.value = params.popTitle || '选择班级'
	choosed.value = params.choosed ? cloneDeep(params.choosed) : []
	showCampus.value = params.showCampus || false
	isAllCampus.value = params.isAllCampus || false
	required.value = params.required || false
    campusMultiple.value=params.campusMultiple||false

	// courseID.value = params.courseID || ''
	checkClassStudentCount.value = params.checkClassStudentCount || false
	// 处理校区ID参数
	if (params.campusID) {
		// 支持多选校区
		if (campusMultiple.value && Array.isArray(params.campusID)) {
			condition.value.campus = params.campusID
		} else if (campusMultiple.value && typeof params.campusID === 'string') {
			condition.value.campus = params.campusID.split(',')
		} else {
			condition.value.campus = params.campusID
		}
	} 
	
	if(params.condition){
		Object.assign(condition.value, params.condition)
		
	}
	console.log('condition', condition.value)
	// 记录下来适用校区
	fixedCampusIds.value = condition.value.campus || [];	
	console.log('fixedCampusIds', fixedCampusIds.value)
	
	//需要记录下来设置的condition，以免重置以后有问题
    Object.assign(newCondition.value,cloneDeep(condition.value))
	if (multi.value) {
		selected.value = choosed.value ? cloneDeep(choosed.value) : []
		let ids: any = []
		selected.value.forEach((item: any) => {
			ids.push(item.ID)
		})
		selectedID.value = ids
	}
	funcQuery()
	
	// 在弹窗打开后自动聚焦到搜索输入框
	nextTick(() => {
		setTimeout(() => {
			classInputRef.value?.focus?.()
		}, 100)
	})
	
	return promise
}

defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
.chooseClassExam {
	.box-wrapper-table {
		display: flex;
		height: 506px !important;
		padding: 0px;
            margin-bottom:10px;
	}
	.fixed-table-box {
		flex: 1;
	}
	.multi-choosed-box-wrapper {
		.fixed-table-box {
			margin-right: 12px;
			width: calc(100% - 306px) !important;
		}
	}
	.single-choosed-wrapper {
		text-align: left;
		width: calc(100% - 150px) !important;
		height: 32px;
		display: table;
	}
	.single-choosed-name {
		vertical-align: middle;
		display: table-cell;
	}

	.advanced-filter-wrap {
		.filter-item {
			width: 262px;
			.el-select {
				width: 100%;
			}
		}
	}
}
</style>
<style lang="scss">
    .chooseClassExam {
        .wtwo-dialog__header {
            background-color: #fff!important;
            padding: 16px!important;
            .wtwo-dialog__title, .wtwo-dialog__close {
                color: #303133!important;
            }
            color: #303133!important;
        }
    }
</style>
