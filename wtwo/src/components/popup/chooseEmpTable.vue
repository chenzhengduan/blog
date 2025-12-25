<template>
	<!-- 选员工 -->
	<el-dialog
		v-model="dialogVisible"
		:title="transToConfigDescript(popTitle)"
		:width="multi ? '1180px' : '870px'"
		:close-on-click-modal="false"
		:append-to-body="true"
		:destroy-on-close="true"
		:align-center="true"
		class="chooseEmpTable"
		draggable
		@opened="handleOpened"
		@close="close"
	>
		<div
			class="box-wrapper-table"
			:class="{ 'multi-choosed-box-wrapper': multi }"
		>
			<div class="fixed-table-box">
				<div class="modal-filter multi-conditional">
					<div class="filter-item mr-20px w-262px">
						<el-input
							v-model.lazy.trim="condition.Query"
							placeholder="姓名/工号"
							ref="empInputRef"
							@keyup.native.enter="getFirstPage"
						></el-input>
					</div>
                    <div class="filter-item mr-20px w-262px">
                        <el-tree-select
                            v-model="condition.CampusIDList"
                            :data="treeData"
                            :props="{ label: 'label', value: 'value', children: 'Children' }"
                            multiple
                            collapse-tags
							collapse-tags-tooltip
                            filterable
                            clearable
                            default-expand-all
                            show-checkbox
                            check-strictly
                            placeholder="所有部门"
                        >
                            <template #prefix><p class="search-input-label">部门</p></template>
                        </el-tree-select>
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
                        <div class="filter-item">
                            <el-select v-model="condition.StatusList" placeholder="不限"  multiple clearable :disabled="disabledCondition.indexOf('StatusList')!=-1" @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">员工状态</p></template>
                                <el-option :value="1" label="在职"></el-option>
                                <el-option :value="0" label="离职"></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
                            <el-select v-model="condition.IsClassTeacherList" placeholder="不限"  multiple clearable :disabled="disabledCondition.indexOf('IsClassTeacherList')!=-1" @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">{{transToConfigDescript('是否为任课老师')}}</p></template>
                                <el-option :value="1" label="是"></el-option>
                                <el-option :value="0" label="否"></el-option>
                            </el-select>
						</div>
                        <div class="filter-item">
							<el-select
								v-model="condition.PositionIDList"
								placeholder="不限"
								filterable
                                multiple 
                                clearable 
                                collapse-tags
                                collapse-tags-tooltip
								@change="getFirstPage"
							>
								<template #prefix><p class="search-input-label">职位</p></template>
								<el-option
									v-for="item in EMP_POSITION"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
							</el-select>
						</div>
                        <div class="filter-item">
							<el-select v-model="condition.IsEmployeeList" placeholder="不限" multiple clearable collapse-tags
                                collapse-tags-tooltip @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">员工类型</p></template>
                                <el-option :value="1" label="正式"></el-option>
                                <el-option :value="0" label="实习/外包/其他"></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-select v-model="condition.PositionTypeList" placeholder="不限" multiple clearable collapse-tags
                                collapse-tags-tooltip @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">在职类型</p></template>
                                <el-option :value="0" label="全职"></el-option>
                                <el-option :value="1" label="兼职"></el-option>
                                <el-option :value="2" label="专职"></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-select
								v-model="condition.SubjectIDList"
								:disabled="disabledCondition.indexOf('SubjectIDList')!=-1"
								placeholder="不限"
								filterable
                                multiple 
                                clearable 
                                collapse-tags
                                collapse-tags-tooltip
								@change="getFirstPage"
							>
								<template #prefix><p class="search-input-label">授课科目</p></template>
								<el-option
									v-for="item in SUBJECT"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
							</el-select>
						</div>
                        <div class="filter-item">
							<el-select
								v-model="condition.GradeIDList"
								placeholder="不限"
								filterable
                                multiple 
                                clearable 
                                collapse-tags
                                collapse-tags-tooltip
								@change="getFirstPage"
							>
								<template #prefix><p class="search-input-label">授课年级</p></template>
								<el-option
									v-for="item in SHIFT_GRADE"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
							</el-select>
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
						:row-key="'ID'"
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
							key="Name"
							label="姓名"
							show-overflow-tooltip
							width="120px"
						></el-table-column>
						<el-table-column
							prop="Serial"
							key="Serial"
							label="工号"
							show-overflow-tooltip
							width="80"
						></el-table-column>
						<el-table-column
							prop="DepartPositionList"
							key="DepartPositionList"
							label="所属部门与职位"
							width="120"
							show-overflow-tooltip
						>
                            <template #default="scope">
								{{ scope.row.DepartPositionText }}
							</template>
                        </el-table-column>
						<el-table-column
                            prop="IsClassTeacher"
							key="IsClassTeacher"
							:label="transToConfigDescript('是否为任课老师')"
							show-overflow-tooltip
                            align="center"
							width="130px"
						>
                            <template #default="scope">
                                {{ scope.row.IsClassTeacher==1?'是':'否' }}
                            </template>
                        </el-table-column>
                        <el-table-column
                            prop="CampusList"
							key="CampusList"
							:label="transToConfigDescript('上课校区')"
							show-overflow-tooltip
							v-if="EnableAssignTeacherCourseCampus"
							width="160px"
						></el-table-column>
						<el-table-column
							prop="IsEmployee"
							label="员工类型"
							width="80"
							show-overflow-tooltip
						>
                            <template #default="scope">
                                {{ scope.row.IsEmployee==1?'正式':scope.row.IsEmployee==0?'实习/外包/其他':'-' }}
                            </template>
                        </el-table-column>
						<el-table-column
							prop="PositionType"
							key="PositionType"
							label="在职类型"
							width="80"
							show-overflow-tooltip
						>
                            <template #default="scope">
                                {{ scope.row.PositionType==1?'兼职':scope.row.PositionType==2?'专职': scope.row.PositionType==0?'全职':'-'}}
                            </template>
                        </el-table-column>
						<el-table-column
							prop="SubjectList"
							key="SubjectList"
							label="授课科目"
							width="140"
                            show-overflow-tooltip
						>
                            <template #default="scope">
								{{ scope.row.SubjectText }}
							</template>
                        </el-table-column>
						<el-table-column
							prop="GradeList"
							key="GradeList"
							label="授课年级"
							width="140"
							show-overflow-tooltip
						>
                            <template #default="scope">
								{{ scope.row.GradeText }}
							</template>
                        </el-table-column>
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
			<div class="choosed-result-wrapper" v-if="multi">
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
			<div :class="multi&&!(EnableClassCommission&&showTeacherType) ? 'flex-end' : 'flex-between'">
				<div class="single-choosed-wrapper" v-if="!multi">
					<span class="single-choosed-name">已选择：{{ oneChoosed ? oneChoosed.Name : '' }}</span>
					<el-select v-if="EnableClassCommission&&showTeacherType" v-model="teacherType" :placeholder="transToConfigDescript('老师类型')" class="w-120px!">
                        <el-option v-for="item in teacherTypeList" :key="item.ID" :label="item.Name" :value="item.ID"></el-option>
                    </el-select>
				</div>
				<el-select v-if="multi&&EnableClassCommission&&showTeacherType" v-model="teacherType" :placeholder="transToConfigDescript('老师类型')" class="w-120px!">
					<el-option v-for="item in teacherTypeList" :key="item.ID" :label="item.Name" :value="item.ID"></el-option>
				</el-select>
				<div>
					<el-button plain @click="close">取消</el-button>
					<el-button type="primary" @click="submit">确定</el-button>
				</div>
			</div>
		</template>
	</el-dialog>
</template>

<script lang="ts" setup>
import $ from 'jquery'
import { storeToRefs } from 'pinia'
import {
	useConfigs,
	useCurrentCampuses,
} from '@/store'
import { useDictFieldsStore } from '@/store/dict'

const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)

import { getEmployeeList, queryTeacherCommissionSetting } from '@/api'
import { transToConfigDescript, formatNumber } from '@/utils/filters/filters'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { cloneDeep } from 'lodash'

import deleteSVG from '@/assets/svg/delete.svg'
import { computed, getCurrentInstance, h, nextTick, ref, shallowRef, watch } from 'vue'
import { TableInstance, InputInstance } from 'element-plus'
import { useOrganizationStore } from '@/store/organization'
import { checkPageIndex } from '@/utils'

const organizationStore = useOrganizationStore()
const treeData = computed(() => {
	return ((organizationStore as any).departTree || []) as any[]
})
// 单次加载控制，避免首次打开触发多次“查部门”接口
const departTreeLoaded = ref(false)
const isFetchingDepartTree = ref(false)
async function ensureDepartTreeOnce() {
	// 若已有数据或已标记加载完成，则不再请求
	if (departTreeLoaded.value || (treeData.value && treeData.value.length > 0)) {
		departTreeLoaded.value = true
		return
	}
	// 防抖/并发保护
	if (isFetchingDepartTree.value) return
	if (typeof (organizationStore as any).fetchDepartTree !== 'function') return
	try {
		isFetchingDepartTree.value = true
		await (organizationStore as any).fetchDepartTree()
		departTreeLoaded.value = true
	} finally {
		isFetchingDepartTree.value = false
	}
}
// 如果外部异步把部门树塞进来，这里也会感知并标记已加载
watch(
	() => treeData.value?.length,
	(len) => {
		if (len && len > 0) {
			departTreeLoaded.value = true
		}
	}
)

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const configs = computed(() => {
	return useConfigs().configs
})
const EnableAssignTeacherCourseCampus = computed(()=>{ //是否开启指派老师上课校区功能，0否（默认），1（是，丽音艺术定制）
	return configs.value.EnableAssignTeacherCourseCampus==1
})
const EnableClassCommission=computed(()=>{
    return configs.value.EnableClassCommission==1
})

const EMP_POSITION = computed(() => {
	return dictFields.value('EMP_POSITION')
})
const SUBJECT = computed(() => {
	return dictFields.value('SUBJECT')
})
const SHIFT_GRADE = computed(() => {
	return dictFields.value('SHIFT_GRADE')
})


const dialogVisible = ref(false)
// 弹框参数 start
const multi = ref(false)
const popTitle = ref('选择员工')
const choosed = ref([] as any)
const showTeacherType=ref(false)
const disabledCondition=ref([] as any)
// 弹框参数 end

const teacherTypeList=ref([] as any)
const teacherType=ref('')

const selected = ref([] as any)
const selectedID = ref([] as any)
const expandedMore = ref(true)
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
	IsClassTeacherList:[],
	StatusList:[1],
	PositionIDList:[],
	SubjectIDList:[],
	PositionTypeList:[],
	CampusIDList:[],
	GradeIDList:[],
	IsEmployeeList:[]
}
const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const newCondition=ref<typeof defaultCondition>(cloneDeep(defaultCondition))
function showMore() {
	expandedMore.value = !expandedMore.value
	setHeight()
}

const empInputRef = ref<InputInstance>()
async function handleOpened(){
    // 确保部门树数据只加载一次
    await ensureDepartTreeOnce()
    nextTick(()=>{
        if(empInputRef.value && typeof empInputRef.value.focus==='function'){
            empInputRef.value.focus();
        }else{
            (empInputRef.value as any)?.input?.focus?.();
        }
    })
}
// 防御：当对话框显示时也拉取一次，避免个别场景 @opened 未触发
watch(dialogVisible, async (val) => {
	if (val) {
		await ensureDepartTreeOnce()
	}
})

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
		if (selected.value.length === 0) {
			ElMessage({
				message: '请至少选择一个员工。',
				type: 'warning',
			})
			return
		}
		let obj:any={data: selected.value}
        if(EnableClassCommission.value&&showTeacherType.value){
            obj.other={
                ID:teacherType.value,
                Name:teacherTypeList.value.find((item:any)=>item.ID===teacherType.value)?.Name||''
            }
        }else{
            obj.other={
                ID:'',
                Name:''
            }
        }
		_resolve(obj)
	} else {
		if (!oneChoosed.value) {
			ElMessage({
				message: '请先选择员工。',
				type: 'warning',
			})
			return
		}
		let obj:any={data: oneChoosed.value}
		if(EnableClassCommission.value&&showTeacherType.value){
			obj.other={
				ID:teacherType.value,
				Name:teacherTypeList.value.find((item:any)=>item.ID===teacherType.value)?.Name||''
			}
		}else{
			obj.other={
				ID:'',
				Name:''
			}
		}
		params.data = oneChoosed.value
		_resolve(obj)
	}
	
	
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
	expandedMore.value=true
	_reject()
}

//查询
function funcQuery() {
	loading.value = true
	list.value = []
	checkedList.value = []
	let params: any = {
		desc: 1,
		sort: 'Name',
		// Shift: courseID.value || '',
	}
	Object.assign(params, {
		...page.value,
		...condition.value,
	})
	getEmployeeList(params)
		.then((res) => {
            let data=res.Data||{}
			list.value = (data.List || []).map((item:any)=>{
				// 统一 ID
				item.ID = item.UserID
				// 预计算重渲染时会用到的展示文本，避免每次 render 做 join/遍历
				const departList = item.DepartPositionList || []
				item.DepartPositionText = departList
					.map((dp:any)=>`${dp.DepartName}${dp.PositionName?('['+dp.PositionName+']'):''}`)
					.join(',')
				const subjectList = item.SubjectList || []
				item.SubjectText = subjectList.map((s:any)=>s.Name).join(',')
				const gradeList = item.GradeList || []
				item.GradeText = gradeList.map((g:any)=>g.Name).join(',')
				return item
			})
			let checkedArr: any = []
			list.value.forEach((item: any) => {
				if (selectedID.value.indexOf(item.ID) !== -1) {
					checkedArr.push(item.ID)
				}
			})
			checkedList.value = checkedArr
			Object.assign(page.value, {
				PageIndex: data.PageIndex,
				PageCount: data.PageCount,
				PageSize: data.PageSize,
				TotalCount: data.TotalCount,
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

function getTeacherType(){
    queryTeacherCommissionSetting().then((res:any)=>{
        teacherTypeList.value = res.Data || [];
    })
}

let _resolve = null as any
let _reject = null as any
function open(params: any) {
	let promise = new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
	})
	showTeacherType.value=params.showTeacherType||false;
    if(showTeacherType.value){
        getTeacherType()
    }
	disabledCondition.value=params.disabledCondition||[]
	dialogVisible.value = true
	multi.value = params.multi || false
	popTitle.value = params.popTitle || '选择员工'
	choosed.value = params.choosed ? cloneDeep(params.choosed) : []
	if(params.condition){
		Object.assign(condition.value, params.condition)
	}
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
	getFirstPage()
	
	// 在弹窗打开后自动聚焦到搜索输入框
	nextTick(() => {
		setTimeout(() => {
			empInputRef.value?.focus?.()
		}, 100)
	})
	
	return promise
}

defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
.chooseEmpTable {
	.box-wrapper-table {
		display: flex;
		height: 506px !important;
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
		display: flex;
		align-items: center;
	}
	.single-choosed-name {
		margin-right:10px;
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
