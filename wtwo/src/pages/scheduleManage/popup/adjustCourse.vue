<!-- 临时调课 -->
<template>
    <el-drawer
		v-model="drawer"
		direction="rtl"
		size="1280px"
		class="adjustCourse"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
        <template #header>
            <div class="flex-center title-bar">
                <div>临时调课</div>
                <!-- <div class="flex-center step-bar">
                    <div v-for="(item,index) in stepList" :key="index" class="step-bar-item" :class="{active:index+1==step,finished:index+1<step}">
                        <div class="step-num">{{ index+1 }}</div>
                        <div class="step-main">
                            <div class="step-title">{{ item.title }}</div>
                        </div>
                    </div>
                </div> -->
            </div>
        </template>
        <div class="drawer-body-wrap" v-loading="loading">
            <div class="adjust-student-box" :class="{'active':step==1}">
                <div class="adjust-box-mask"></div>
                <div class="adjust-box-article" style="height: calc(100% - 50px);">
                    <el-scrollbar class="px-16px!">
                        <div class="adjust-box-article-header">
                            <div v-if="info.Unit!=3||IsOpenShiftForDay" class="time-info">{{ dayjs(info.StartTime).format('YYYY-MM-DD HH:mm') }}-{{ dayjs(info.EndTime).format('HH:mm') }}[{{ dayjs(info.StartTime).format('dddd') }}]</div>
                            <div v-if="info.Unit==3&&!IsOpenShiftForDay" class="time-info">{{ dayjs(info.StartTime).format('YYYY-MM-DD') }}[{{ dayjs(info.StartTime).format('dddd') }}]</div>
                            <div class="other-info-item my-12px flex-center">
                                <span class="info-label">{{transToConfigDescript('上课课程：')}}</span>
                                <span class="ellipsis-single" :title="info.ShiftName">{{ info.ShiftName }}</span>
                            </div>
                            <div class="other-info-item flex-center">
                                <span class="info-label">{{transToConfigDescript('上课班级/学员：')}}</span>
                                <span class="ellipsis-single" :title="info.ClassName">{{ info.ClassName }}</span>
                            </div>
                        </div>
                        <div class="flex-end pb-10px" v-if="outStudentList.length>0">
                            <el-link type="primary" underline="never" @click="showOutStudentList">查看已调出学员</el-link>
                        </div>
                        <div class="table-wrap p-0!" v-loading="step==1&&studentLoading">
                            <el-table :data="studentList" style="width: 100%" :max-height="`calc(-230px + 100vh)`" @selection-change="handleSelectionChange">
                                <template #empty>
                                    <el-empty
                                        :image="globalData.emptyPng"
                                        description="暂无数据"
                                        :image-size="100"
                                    ></el-empty>
                                </template>
                                <el-table-column
                                    fixed="left"
                                    type="selection"
                                    width="30"
                                    key="selection"
                                    :selectable="selectable"
                                ></el-table-column>
                                <el-table-column prop="Name" label="姓名" width="220">
                                    <template #default="scope">
                                        <div class="flex-center">
                                            <div class="avatar" v-if="scope.row.Photo">
                                                <img :src="scope.row.Photo" alt="" />
                                            </div>
                                            <div class="avatar placeholder" v-else>
                                                {{ getNameInitial(scope.row.Name) }}
                                            </div>
                                            <div class="ellipsis-single" :title="scope.row.Name">{{scope.row.Name}}</div>
                                            <el-tag
                                                v-if="(scope.row.AdjustFlag == 2) || (scope.row.AdjustFlag == 3 && scope.row.IsSubscribeCourse == 0)"
                                                class="ml-4px px-2px! h-18px!"
                                                type="warning"
                                                size="small"
                                                :hit="true"
                                                >{{ scope.row.AdjustFlag == 2 ? "临调" : "临加" }}</el-tag
                                            >
                                            <el-tag
                                                v-if="scope.row.IsSubscribeCourse == 1"
                                                class="ml-4px px-2px! h-18px!"
                                                type="warning"
                                                size="small"
                                                :hit="true"
                                                >约课</el-tag
                                            >
                                            <el-tag
                                                v-if="scope.row.IsMend==1"
                                                class="ml-4px px-2px! h-18px!"
                                                type="warning"
                                                size="small"
                                                :hit="true"
                                                >补课</el-tag
                                            >
                                            <span v-if="scope.row.IsMend==1" class="flex-center" title="补课学员不允许调课，请前往补课管理撤销补课后，重新安排补课">
                                                <el-icon size="16px" color="#E6A23C" ><Warning /></el-icon>
                                            </span>
                                        </div>
                                        
                                    </template>
                                </el-table-column>
                                <el-table-column prop="SMSTel" label="联系电话" width="110" />
                                <el-table-column prop="AbsentCauseName" label="缺勤原因" show-overflow-tooltip/>
                            </el-table>
                        </div>
                    </el-scrollbar>
                </div>
                <!-- <div class="adjust-box-footer flex-end">
                    <el-button type="primary" @click="nextStep" :disabled="multipleSelection.length==0">已完成选择，下一步</el-button>
                </div> -->
            </div>
            <div class="adjust-course-box" :class="{'active':step==2}">
                <!-- <div class="adjust-box-mask"></div> -->
                <div class="adjust-box-article">
                    <el-scrollbar>
                        <div class="drawer-filter-box mb-16px">
                            <div class="drawer-filter-box-item">
                                <el-select v-model="campusID" :placeholder="transToConfigDescript('请选择校区')" filterable>
                                    <template #prefix>
                                        <p class="search-input-label">{{transToConfigDescript('上课校区')}}</p>
                                    </template>
                                    <el-option v-for="item in campusList" :key="item.ID" :label="item.Name" :value="item.ID" />
                                </el-select>
                            </div>
                            <div class="drawer-filter-box-item">
                                <el-select v-model="condition.ShiftID" :placeholder="transToConfigDescript('请选择上课课程')" filterable>
                                    <template #prefix>
                                        <p class="search-input-label">{{transToConfigDescript('上课课程')}}</p>
                                    </template>
                                    <el-option v-for="item in shiftList" :key="item.ID" :label="item.Name" :value="item.ID">
                                        {{ item.Name }}<el-tag v-if="item.Type != 1" type="info" size="small" class="ml-4px!">{{transToConfigDescript('关联课程')}}</el-tag>
                                    </el-option>
                                </el-select>
                            </div>
                            <div class="drawer-filter-box-item">
                                <el-select v-model="condition.ClassID" :placeholder="transToConfigDescript('请选择上课班级')" filterable>
                                    <template #prefix>
                                        <p class="search-input-label">{{transToConfigDescript('上课班级')}}</p>
                                    </template>
                                    <el-option v-for="item in classList" :key="item.ID" :label="item.Name" :value="item.ID" />
                                </el-select>
                            </div>
                            <template v-if="expandedMore">
                                <div class="drawer-filter-box-item">
                                    <el-date-picker
                                        v-model="condition.dateRange"
                                        type="daterange"
                                        unlink-panels
                                        range-separator="到"
                                        start-placeholder="开始日期"
                                        end-placeholder="结束日期"
                                        :prefix-icon="customPrefix"
                                        :shortcuts="dateShortcuts"
                                        value-format="YYYY-MM-DD"
                                        format="YYYY-MM-DD"
                                        class="search-date-wrap w-100%!"
                                    >
                                    </el-date-picker>
                                </div>
                                <div class="drawer-filter-box-item">
                                    <input-tag
                                        :label="transToConfigDescript('任课老师')"
                                        placeholder="请选择"
                                        :isLine="true"
                                        :multiple="true"
                                        :selected="condition.teacherList"
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
                                <div class="drawer-filter-box-item">
                                    <el-select
                                        v-model="condition.timeType"
                                        placeholder="不限"
                                        clearable
                                        collapse-tags
                                        collapse-tags-tooltip
                                        filterable
                                        multiple
                                    >
                                        <template #prefix>
                                            <p class="search-input-label">{{transToConfigDescript('上课时段')}}</p>
                                        </template>
                                        <el-option :value="0" label="上午"></el-option>
                                        <el-option :value="1" label="下午"></el-option>
                                        <el-option :value="2" label="晚上"></el-option>
                                    </el-select>
                                </div>
                            </template>
                            <div class="drawer-filter-box-item flex-end" :class="{'w-[100%]! mr-16px':expandedMore}">
                                <el-link
                                    type="primary"
                                    underline="never"
                                    @click.stop="showMore"
                                    class="mr-12px"
                                    >{{ expandedMore ? '收起' : '展开' }}&nbsp;
                                    <el-icon
                                        ><ArrowUp v-if="expandedMore" /><ArrowDown
                                            v-else
                                    /></el-icon>
                                </el-link>
                                <el-button type="primary" @click="getFirstPage">查询</el-button>
                                <el-button @click="handleReset">重置</el-button>
                            </div>
                        </div>
                        <div class="table-wrap" v-loading="step==2&&courseLoading">
                            <el-table :data="courseList" style="width: 100%" :max-height="`calc(${expandedMore?'-346px':'-250px'} + 100vh)`" 
                                ref="customTable" @row-click="chooseIt">
                                <template #empty>
                                    <el-empty
                                        :image="globalData.emptyPng"
                                        description="暂无数据"
                                        :image-size="100"
                                    ></el-empty>
                                </template>
                                <el-table-column prop="index" width="30" fixed="left" class-name="table-radio">
                                    <template #default="scope">
                                        <el-radio @click.native.prevent :model-value="oneChoosed&&oneChoosed.ID" :value="scope.row.ID"/>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="StartTime" :label="transToConfigDescript('上课时间')" width="240">
                                    <template #default="scope">
                                        <span v-if="scope.row.Unit!=3||IsOpenShiftForDay">{{ dayjs(scope.row.StartTime).format('YYYY-MM-DD HH:mm') }}-{{ dayjs(scope.row.EndTime).format('HH:mm') }}[{{dayjs(scope.row.StartTime).format('dddd') }}]</span>
                                        <span v-if="scope.row.Unit==3&&!IsOpenShiftForDay">{{ dayjs(scope.row.StartTime).format('YYYY-MM-DD') }}&nbsp;[{{dayjs(scope.row.StartTime).format('dddd') }}]</span>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="ClassName" :label="transToConfigDescript('上课班级/学员')" width="200" show-overflow-tooltip></el-table-column>
                                <el-table-column prop="TeacherName" :label="transToConfigDescript('任课老师')" width="120" show-overflow-tooltip></el-table-column>
                                <el-table-column prop="ClassroomName" :label="transToConfigDescript('上课教室')" width="120" show-overflow-tooltip></el-table-column>
                                <el-table-column prop="StudentCount" label="在班/预招" width="140">
                                    <template #default="scope">
                                        {{scope.row.StudentCount}}/{{scope.row.MaxStudentsAmount}}
                                    </template>
                                </el-table-column>
                            </el-table>
                            <div
                                class="pageination-wrapper flex-end m-t-10px"
                                v-if="courseList.length > 0"
                            >
                                <el-pagination
                                    @size-change="handleSizeChange"
                                    @current-change="handleCurrentChange"
                                    :current-page="page.PageIndex"
                                    :page-sizes="[10, 20, 50, 100, 200]"
                                    :page-size="page.PageSize"
                                    layout="total, sizes, prev, jumper ,next"
                                    :total="page.TotalCount"
                                    size="small"
                                >
                                </el-pagination>
                            </div>
                        </div>
                    </el-scrollbar>
                </div>
                <div class="adjust-box-footer flex-between">
                    <div class="flex-center">
                        <el-checkbox v-model="checkConflict" :true-value="1" :false-value="0" :disabled="loading||!NewCourse_IngoreCourseConflict">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
                    </div>
                    <div class="flex-center">
                        <!-- <el-button @click="prevStep">上一步</el-button> -->
                        <el-button type="primary" :disabled="multipleSelection.length==0||!(oneChoosed&&oneChoosed.ID)||loading" @click="submit">确认调课</el-button>
                    </div>
                </div>
            </div>
        </div>
        <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
        <adjusted-students ref="adjustedStudentsRef"></adjusted-students>
        <ConflictPrompt ref="conflictPromptRef" />
    </el-drawer>
</template>
<script lang="ts" setup>
import { useConfigs } from '@/store';
import dayjs from 'dayjs';
import { computed, nextTick, ref, watch } from 'vue';
import { Warning } from '@element-plus/icons-vue';
import { useOrganizationStore } from '@/store/organization';
import { assignPage, checkPageIndex, dateShortcuts, IPageModel, weekDiff, getNameInitial } from '@/utils';
import { cloneDeep } from 'lodash';
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue';
import { adjustCourseGetShifts, adjustForStudents, getStudentsForAdjustCourse, queryCourseNew } from '@/api/arrange';
import { queryClass } from '@/api';
import { TableInstance } from 'element-plus';
import AdjustedStudents from './adjustedStudents.vue';
import useEvent from '@/config/event';
import { getCurrentInstance } from 'vue';
import { transToConfigDescript } from '@/utils/filters/filters';
import ConflictPrompt from './conflictPrompt.vue';

const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const configs=computed(()=>{
    return useConfigs().configs
})

const IsOpenShiftForDay=computed(()=>{ // 0按月 1按天
	return configs.value.IsOpenShiftForDay==1
})
const campusList=computed(()=>{
    return useOrganizationStore().campusList
})

const event = useEvent()

const drawer = ref(false)
const loading=ref(false)
const studentLoading=ref(false)
const courseLoading=ref(false)
const step=ref(1)
const info=ref({} as any)
let stepList=[{
    title:'选择学员'
},{
    title:'调入排课'
}]
// 日期相关
const customPrefix = {
    render() {
        return transToConfigDescript('上课时间')
    },
}
const checkConflict=ref(1)
const studentList=ref([])
const outStudentList=ref([])
const courseList=ref([])
const expandedMore=ref(false)
const defaultCondition: any = {
	Query: '', // 查询条件，支持模糊查询
	ShiftID: '', // 课程ID
	ShiftName: '', // 课程名称
	ClassID: '', // 班级ID
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
	FinishType: "0", // 上课状态：-1不限，0未上课，1已上课，2已取消，数据库中的状态为：0未上课，1已上课，2已取消，多个以,分割
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
	DialogType: 2, // 查询场景，0其他；1跟班补课查询排课，2调课查询排课
	TryStatus: 0, // 1=处理不需要试听班级的排课
	UsePlatform: 0, // 判断是否微信端调用该接口 微信调用需加上课程手机端不显示的过滤条件,1=需要，0=不需要
	ExportColumn: ['ClassName','TeacherName','ClassroomName','StudentCount'], // 选择列，逗号分隔
	Download: 0, // 0=查询，1=导出
	IsTotal: 0, // 是否合计,0=不是，1=是
	IsSubscribeCourse: -1, // 开放预约状态：-1不限，0未开放，1已开放
	// 筛选参数
	teacherList: [], // 任课老师列表
	timeType: [], // 时段类型
	dateRange: [], // 日期范围
}
const condition=ref(cloneDeep(defaultCondition))
const campusID=ref('')
const page = ref({
	TotalCount: 0, //总条数
	PageSize: 10, //每页条数
	PageIndex: 1, //第几页
	PageCount: 1, //总页数
} as IPageModel)
const shiftList=ref([] as any[])
const classList=ref([] as any[])
const oneChoosed=ref(null as any)
const selectable = (row: any) => row.AdjustFlag!=1&&!row.IsMend

const multipleSelection = ref([] as any[]);
function handleSelectionChange(val: any) {
    multipleSelection.value = val;
}

function showMore(){
    expandedMore.value = !expandedMore.value
}

const chooseEmpTableRef = ref()
function selectTeacher(){
    chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: condition.value.teacherList
		})
		.then((res: any) => {
			condition.value.teacherList = res.data || []
		})
}

//翻页查询
function handleSizeChange(val: number) {
	page.value.PageSize = val
    page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
	funcQuery()
}
function handleCurrentChange(val: number) {
	page.value.PageIndex = val
	funcQuery()
}
function getFirstPage() {
	page.value.PageIndex = 1
	funcQuery()
}
function handleReset(){
    // 重置所有筛选条件到默认值
	Object.assign(condition.value, cloneDeep(defaultCondition))
    campusID.value=info.value.CampusID
    condition.value.ShiftID=info.value.ShiftID
	// 重置后自动查询
	getFirstPage()
}
const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
function funcQuery(){
    courseList.value = []
    let params: any = {}
	
	// 从 condition 中排除需要特殊处理的参数
	const { teacherList, timeType, dateRange, ...conditionParams } = condition.value
	
	Object.assign(params, conditionParams,page.value,{
        desc:1,
        sort:'StartTime'
    })
	params.ClassID=params.ClassID||EMPTYGUID
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
	params.ShiftTypeList = condition.value.ShiftTypeList||[]
	params.CampusIDList = [campusID.value]
    courseLoading.value=true
    queryCourseNew(params).then((res:any)=>{
        courseList.value=res.Data.List||[]
        if(oneChoosed.value&&!courseList.value.find((item:any)=>item.ID==oneChoosed.value.ID)){
            oneChoosed.value=null
        }
        assignPage(page.value, res.Data)
    }).finally(()=>{
        courseLoading.value=false
    })
}

const customTable = ref<TableInstance>()
function chooseIt(row:any){
    let item=row
    customTable.value?.setCurrentRow(row);
    oneChoosed.value=item;
}

function nextStep(){
    if(multipleSelection.value.length==0){
        ElMessage.warning("请选择需要调课的学员")
        return
    }
    step.value++
}
function getShiftList(){
    adjustCourseGetShifts({
        ShiftID: condition.value.ShiftID,
        campus: campusID.value,
        PageSize: 1000
    }).then((res:any)=>{
        shiftList.value=res.Data||[]
    })
}
function getClassList(){
    queryClass({
        campus: campusID.value,
        shift:condition.value.ShiftID,
        PageSize: 99999
    }).then((res:any)=>{
        classList.value=res.Data||[]
        if(classList.value.length){
            condition.value.ClassID=classList.value[0].ID
        }else{
            condition.value.ClassID=''
        }
    })
}
watch(()=>condition.value.ShiftID,()=>{
    if(campusID.value&&condition.value.ShiftID){
        getClassList()
    }
})
watch(()=>campusID.value,()=>{
    if(campusID.value&&condition.value.ShiftID){
        getClassList()
    }
})
watch(()=>condition.value.ClassID,()=>{
    if(campusID.value&&condition.value.ShiftID){
        getFirstPage()
    }
})
function prevStep(){
    step.value--
}

function getStudentList(){
    studentLoading.value=true
    getStudentsForAdjustCourse({
        courseID:info.value.ID
    }).then((res:any)=>{
        let data=res.Data||{}
        studentList.value=data.StudentAttendanceList||[]
        outStudentList.value=data.StudentAdjustList||[]
    }).finally(()=>{
        studentLoading.value=false
    })
}
const adjustedStudentsRef=ref()
function showOutStudentList(){
    adjustedStudentsRef.value?.open({
        studentList:outStudentList.value,
        courseID:info.value.ID
    }).then((res:any)=>{
    })
}

const conflictPromptRef = ref()
function submit(){
    if(oneChoosed.value==null){
        ElMessage.warning("请选择调整的目标排课")
        return
    }
    ElMessageBox.confirm('确定调课吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(()=>{
        loading.value=true
        adjustForStudents({
            FromCourseID:info.value.ID,
            ToCourseID:oneChoosed.value.ID,
            StudentIDList:multipleSelection.value.map((item:any)=>item.ID),
            IsCheckConflict:checkConflict.value
        }).then((res:any)=>{
            ElMessage.success("已调课")
            getStudentList()
            step.value=1
            oneChoosed.value=null
            event.emit("arrange-table-list-refresh",{refreshTotal:true})
        }).catch((error: any) => {
            if(error.ErrorCode==409){
                conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{
                }).catch((back:any)=>{
                    if(back&&back.closeCurrent){
                        drawer.value=false
                    }
                })
            }
        }).finally(()=>{
            loading.value=false
        })
    })
}
let _resolve: any = null,
    _reject: any = null

/** 对外暴露一个open方法 */
function open(params: any) {
    info.value=params.data||{}
    campusID.value=info.value.CampusID
    condition.value.ShiftID=info.value.ShiftID
    getStudentList()
    getShiftList()
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		drawer.value = true
	})
}

function close() {
    drawer.value = false
    nextTick(()=>{
        info.value={}
        shiftList.value=[]
        condition.value=cloneDeep(defaultCondition)
        courseList.value=[]
        oneChoosed.value=null
        studentList.value=[]
        step.value=1
    })
    
    _reject && _reject()
}

defineExpose({
    open,
})

event.on("adjust-course-student-refresh",()=>{
    if(!drawer.value||!info.value?.ID)return
    getStudentList()
})
</script>
<style lang="scss" scoped>
.adjustCourse{
    .drawer-body-wrap{
        overflow-x: auto;
        width: 1280px;
        flex-direction: row;
    }
    .step-bar{
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        flex-shrink: 0;
    }
    .step-bar-item{
        display: flex;
        align-items: center;
        position: relative;
        flex-shrink: 0;
        &+.step-bar-item{
            &:before{
                content: '';
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 130px;
                height: 1px;
                background: #E5E5E5;
                margin: 0 10px;
            }
        }
        .step-num{
            background: #F2F3F5;
            color: #A8ABB2;
            width: 28px;
            height: 28px;
            border-radius: 50%;
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            font-weight: 500;
            // cursor: pointer;
        }
        .step-main{
            padding-left: 12px;
            // cursor: pointer;
        }
        .step-title{
            font-size: 14px;
            color: #909399;
            line-height: 22px;
        }
        &.active{
            .step-num{
                background: #2878E8;
                color: #fff;
            }
            .step-title{
                color: #303133;
                font-weight: 600;
            }
        }
        &.finished{
            .step-num{
                background: #E8F3FF;
                color: #2878E8;
            }
            .step-title{
                color: #606266;
            }
        }
    }
    
    .adjust-student-box,.adjust-course-box{
        height: 100%;
        display: flex;
        flex-direction: column;
        border-right: 1px solid #E5E5E5;
        position: relative;
        .adjust-box-mask{
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255,255,255,0.5);
            z-index: 100;
        }
        &.active{
            .adjust-box-mask{
                display: none;
            }   
        }
        .adjust-box-article{
            flex: 1;
        }
        .adjust-box-footer{
            padding: 9px 20px;
        }
        
    }
    .adjust-student-box{
        width: 524px;
        .adjust-box-article-header{
            background: #F7F8FA;
            padding: 12px 16px;
            margin: 12px 0;
            line-height: 20px;
            .time-info{
                font-weight: 500;
            }
            .other-info-item{
                white-space: nowrap;
                .info-label{
                    color: #909399;
                }
            }
        }
    }
    .adjust-course-box{
        width: 756px;
        .drawer-filter-box {
            .drawer-filter-box-item {
                width: 353px;
            }
            :deep(.wtwo-range-input){
                width: 35%;
            }
        }
    }
    .avatar {
        width: 24px;
        height: 24px;
        border-radius: 4px;
        overflow: hidden;
        background: #f2f3f5;
        flex-shrink: 0;
        margin-right: 4px;
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
}
:deep(.wtwo-table .table-radio .cell){
    padding-right: 0!important;
}
</style>