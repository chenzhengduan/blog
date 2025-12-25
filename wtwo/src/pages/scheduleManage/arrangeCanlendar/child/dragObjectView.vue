<template>
	<div class="drag-object-view-box">
		<div class="schedule-view-mode-tabs">
			<div
				class="schedule-view-mode-tab"
				:class="{ active: dragObjectViewType === 'student' }"
				@click="changedragObjectViewType('student')"
			>
				选择学员
			</div>
			<div
				class="schedule-view-mode-tab"
				:class="{ active: dragObjectViewType === 'class' }"
				@click="changedragObjectViewType('class')"
			>
				选择{{ transToConfigDescript('班级') }}
			</div>
		</div>
		<div class="drag-panel-content">
			<div v-show="dragObjectViewType === 'student'">
				<div class="drag-panel-filter-box">
					<div class="filter-item">
						<el-input placeholder="学员姓名/学号/电话"></el-input>
					</div>
					<div class="filter-item">
						<el-select placeholder="科目">
                            <el-option
                                v-for="item in SUBJECT"
                                :value="item.ID"
                                :label="item.Name"
                                :key="item.ID"
                                collapse-tags
                                collapse-tags-tooltip
                                filterable
                                multiple
                                clearable
                            ></el-option>
                        </el-select>
					</div>
					<div class="filter-item">
						<input-tag
							placeholder="课程"
							:isLine="true"
							:multiple="true"
							:selected="studentCondition.shiftSelected"
							@click="selectCourse"
						>
							<template #btn-icon>
								<el-icon size="20px">
									<svg aria-hidden="true">
										<use
											xlink:href="#w2-xuanke"
										></use>
									</svg>
								</el-icon>
							</template>
						</input-tag>
					</div>
					<div class="filter-item">
						<input-tag
							placeholder="学管师"
							:isLine="true"
							:multiple="true"
							:selected="studentCondition.masterList"
							@click="selectMaster"
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
					<div v-show="studentExpandedMore">
						<div class="filter-item">
							<el-select placeholder="不限" class="mr-4px w-[220px]!">
								<template #prefix>
									<p class="search-input-label">剩余</p>
								</template>
							</el-select>
							<el-input placeholder="请输入"></el-input>
						</div>
						<div class="filter-item">
							<el-select placeholder="报名校区">
                                <el-option
                                    v-for="item in userCampuses"
                                    :value="item.ID"
                                    :label="item.Name"
                                    :key="item.ID"
                                    collapse-tags
                                    collapse-tags-tooltip
                                    filterable
                                    multiple
                                    clearable
                                ></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-date-picker 
                                type="daterange" 
                                unlink-panels 
                                range-separator="到" 
                                start-placeholder="入学开始日期"
								end-placeholder="结束日期"
                                style="width: 534px;"
                                :shortcuts="dateShortcuts"
                                v-model="dateRange"
                                value-format="YYYY-MM-DD"
                                format="YYYY-MM-DD"
                            >
                            </el-date-picker>
						</div>
					</div>
					<div class="filter-item flex-end">
						<el-link type="primary" underline="never" @click="showMoreStudentFilter" class="mr-12px">
							{{ studentExpandedMore ? '收起' : '展开' }}筛选&nbsp;
							<el-icon>
								<ArrowUp v-if="studentExpandedMore" /><ArrowDown v-else />
							</el-icon>
						</el-link>
						<el-button type="primary">查询</el-button>
						<el-button type="default">重置</el-button>
					</div>
				</div>
				<el-table size="small">
					<el-table-column prop="StudentName" label="姓名" width="100px" fixed></el-table-column>
					<el-table-column prop="ShiftName" label="课程" width="100px"></el-table-column>
					<el-table-column prop="RemainAmount" label="可排课数量" width="100px"></el-table-column>
					<el-table-column prop="ShiftRemainAmount" label="剩余数量" width="100px"></el-table-column>
					<el-table-column prop="SubjectName" label="科目" width="100px"></el-table-column>
					<el-table-column prop="Serial" label="学号" width="100px"></el-table-column>
					<el-table-column prop="SMSTel" label="电话" width="100px"></el-table-column>
					<el-table-column prop="Grade" label="年级" width="100px"></el-table-column>
					<el-table-column prop="CampusName" label="报名校区" width="100px"></el-table-column>
				</el-table>
			</div>
			<div v-show="dragObjectViewType === 'class'">
				<div class="drag-panel-filter-box">
					<div class="filter-item">
						<el-input v-model.trim="classCondition.Query" placeholder="班级名称"></el-input>
					</div>
					<div class="filter-item">
						<el-select 
							v-model="classCondition.ShiftTypes"
                            placeholder="教学形式"
                            collapse-tags
                            collapse-tags-tooltip
                            filterable
                            multiple
                            clearable
                        >
                            <el-option
                                :value="4"
                                label="集体班"
                            ></el-option>
                            <el-option
                                :value="2"
                                label="一对多"
                            ></el-option>
                        </el-select>
					</div>
					<div class="filter-item">
						<el-select 
							v-model="classCondition.classType"
                            placeholder="班级类型"
                            collapse-tags
                            collapse-tags-tooltip
                            filterable
                            multiple
                            clearable
                        >
                            <el-option
                                value="2"
                                :label="transToConfigDescript('补课班级')"
                            ></el-option>
                            <el-option
                                value="3"
                                :label="transToConfigDescript('试听班级')"
                            ></el-option>
                            <el-option
                                value="0,1"
                                :label="transToConfigDescript('普通班级')"
                            ></el-option>
                        </el-select>
					</div>
					<div class="filter-item">
						<input-tag
							:placeholder="transToConfigDescript('任课老师')"
							:selected="classCondition.Teachers"
							:isLine="true"
							:multiple="true"
							@click="selectTeacher"
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
					<div v-show="classExpandedMore">
						<div class="filter-item">
							<el-select v-model="classCondition.Year" placeholder="年份" filterable clearable>
                                <el-option
									v-for="item in years"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-select v-model="classCondition.Term" placeholder="期段" filterable clearable>
                                <el-option
									v-for="item in CLASS_TERM"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-select v-model="classCondition.Grade" placeholder="年级" filterable clearable>
                                <el-option
									v-for="item in SHIFT_GRADE"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-select v-model="classCondition.Subject" placeholder="科目" filterable clearable>
                                <el-option
									v-for="item in SUBJECT"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
                            </el-select>
						</div>
						<div class="filter-item">
							<el-select v-model="classCondition.Category" placeholder="课程类型" filterable clearable>
                                <el-option
									v-for="item in SHIFT_CAT"
									:value="item.ID"
									:label="item.Name"
									:key="item.ID"
								></el-option>
                            </el-select>
						</div>
					</div>
					<div class="filter-item flex-end">
						<el-link type="primary" underline="never" @click="showMoreClassFilter" class="mr-12px">
							{{ classExpandedMore ? '收起' : '展开' }}筛选&nbsp;
							<el-icon>
								<ArrowUp v-if="classExpandedMore" /><ArrowDown v-else />
							</el-icon>
						</el-link>
						<el-button type="primary" @click="getClassFirstPage">查询</el-button>
						<el-button type="default" @click="resetClassCondition">重置</el-button>
					</div>
				</div>
				<el-table size="small" :data="classList" v-loading="classLoading">
					<el-table-column prop="Name" label="班级名称" width="100px" fixed></el-table-column>
					<el-table-column prop="ShiftName" label="课程" width="100px"></el-table-column>
					<el-table-column prop="TeacherName" label="任课老师" width="100px"></el-table-column>
					<el-table-column prop="StudentCount" label="人数" width="100px">
						<template #default="scope">
							{{ scope.row.StudentCount + '/' + scope.row.MaxStudentsAmount }}
						</template>
					</el-table-column>
					<el-table-column prop="OpenDate" label="开班日期" width="100px"></el-table-column>
				</el-table>
				<div class="pageination-wrapper flex-end mt-8px" v-if="classList.length">
					<el-pagination
						@size-change="handleClassPageSizeChange"
						@current-change="handleClassPageCurrentChange"
						:current-page.sync="classPage.PageIndex"
						:page-sizes="[10, 20, 50, 100, 200]"
						:page-size="classPage.PageSize"
						layout="total, sizes, prev, jumper ,next"
						:total="classPage.TotalCount"
						size="small"
					>
					</el-pagination>
				</div>
			</div>
		</div>
		<chooseCourse ref="chooseCourseRef"></chooseCourse>
		<chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
	</div>
</template>

<script lang="ts" setup>
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { transToConfigDescript } from '@/utils/filters/filters'
import { computed, h, onMounted, ref, shallowRef, watch } from 'vue'
import { useDictFieldsStore } from '@/store/dict'
import { storeToRefs } from 'pinia'
import { useUserCampuses, useYears } from '@/store'
import { checkPageIndex, dateShortcuts } from '@/utils'
import { cloneDeep } from 'lodash'
import { queryClass } from '@/api'
const props = defineProps<{
  campusId: string
}>()

const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)

const SUBJECT = computed(() => {
	return dictFields.value('SUBJECT')
})
const years = computed(() => {
	return useYears().years
})
const CLASS_TERM = computed(() => {
	return dictFields.value('CLASS_TERM')
})
const SHIFT_GRADE = computed(() => {
	return dictFields.value('SHIFT_GRADE')
})
const SHIFT_CAT = computed(() => {
	return dictFields.value('SHIFT_CAT')
})
const userCampuses=computed(()=>{
    return useUserCampuses().userCampuses
})

const dragObjectViewType=ref('student') //student 学员  class班级
const studentCondition=ref({
	shiftSelected: [],
	masterList:[]
})
const defaultClassCondition: any = {
	Year: '',
	campus: '',
	Grade: '',
	Subject: '',
	Category: '',
	Query: '',
	shiftTypes: [2,4], 
	countStudents: 1,
	teacherId: '',
	Term: '',
	classType: ['0,1', '2', '3'],
	Teachers: [],
	finished:0,
	classStatusSelect: -1, // 开班状态
	Byauthorize:0,
	fromCharge:0
}
const classCondition = ref<typeof defaultClassCondition>(cloneDeep(defaultClassCondition))
const classLoading = ref(false)
const classList = ref([] as any)
const classPage = ref({
	PageIndex: 1,
	PageSize: 10,
	TotalCount: 0,
})
const dateRange = ref([] as any)
function changedragObjectViewType(type:string) {
  dragObjectViewType.value = type
}
const studentExpandedMore = ref(false)
function showMoreStudentFilter() {
  studentExpandedMore.value = !studentExpandedMore.value
}
const classExpandedMore = ref(false)
function showMoreClassFilter() {
  classExpandedMore.value = !classExpandedMore.value
}

const chooseCourseRef = ref()
function selectCourse() {
	chooseCourseRef.value
		?.open({
			multi: true,
			required: true,
			choosed:studentCondition.value.shiftSelected
		})
		.then((res: any) => {
			studentCondition.value.shiftSelected = res.data||[]
		})
}

const chooseEmpTableRef=ref()
function selectMaster() {
	chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: studentCondition.value.masterList
		})
		.then((res: any) => {
			studentCondition.value.masterList = res.data || []
		})
}

function selectTeacher() {
	chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: classCondition.value.Teachers,
		})
		.then((res: any) => {
			classCondition.value.Teachers = res.data || []
		})
}

//查第一页
function getClassFirstPage() {
	classPage.value.PageIndex = 1
	classFuncQuery()
}
function handleClassPageSizeChange(val: any) {
	classPage.value.PageSize = val
	classPage.value.PageIndex = checkPageIndex(classPage.value.PageIndex,classPage.value.TotalCount,classPage.value.PageSize)
	classFuncQuery()
}
function handleClassPageCurrentChange(val: any) {
	classPage.value.PageIndex = val
	classFuncQuery()
}

function resetClassCondition() {
	Object.assign(classCondition.value,cloneDeep(defaultClassCondition.value))
	getClassFirstPage()
}
//查询
function classFuncQuery() {
	if(!props.campusId){
		return
	}
	classLoading.value = true
	classList.value = []
	let params: any = {
		desc: 0,
		sort: 'Name'
	}
	Object.assign(params, {
		...classPage.value,
		...classCondition.value,
	})
	params.campus = props.campusId
	params.teacherId = params.Teachers.map((item: any) => item.ID).join(',')
	params.type =
		params.classType.length === 3 ? '' : params.classType.join(',')
	let shiftTypeNum=0
	if(params.shiftTypes.length){
		params.shiftTypes.forEach((item:any,index:number)=>{
			shiftTypeNum=shiftTypeNum+item*1
		})
	}
	params.shiftType = params.shiftTypes.length?shiftTypeNum:6
	delete params.classType
	delete params.Teachers
	delete params.shiftTypes
	queryClass(params)
		.then((res:any) => {
			classList.value = res.Data || []
			Object.assign(classPage.value, {
				PageIndex: res.PageIndex,
				PageCount: res.PageCount,
				PageSize: res.PageSize,
				TotalCount: res.TotalCount,
			})
		})
		.finally(() => {
			classLoading.value = false
		})
}

// watch(() => props.campusId, () => {
// 	getClassFirstPage()
// })
onMounted(() => {
	// getClassFirstPage()
})
</script>
<style lang="scss" scoped>
.drag-object-view-box {

	.schedule-view-mode-tabs {
		display: flex;
		background-color: #F3F4F4;
		border-radius: 6px;
		padding: 2px;
		gap: 4px;
		width: fit-content;
	}

	.schedule-view-mode-tab {
		padding: 7px 8px;
		border-radius: 4px;
		cursor: pointer;
		transition: all 0.2s ease;
		font-size: 14px;
		color: #606266;
		background-color: transparent;
		white-space: nowrap;

		&:hover {
			background-color: rgba(64, 158, 255, 0.1);
			color: var(--wtwo-color-primary);
		}

		&.active {
			background-color: #fff;
			color: var(--wtwo-color-primary);
			font-weight: 500;
		}
	}
}
</style>