<template>
	<!-- 选学员 -->
	<el-dialog 
        v-model="dialogVisible" 
        :title="transToConfigDescript(popTitle)" 
        :width="multi?'900px':'800px'" 
        :close-on-click-modal="false" 
        :append-to-body="true"
        :destroy-on-close="true" 
        :align-center="true" 
        class="chooseStudentExam" 
        draggable
        @close="close" 
    >
		<div class="box-wrapper-table" :class="{ 'multi-choosed-box-wrapper':multi}">
            <div class="fixed-table-box">
                <div class="modal-filter multi-conditional">
                    <div class="filter-item mr-20px w-534px">
                        <el-input v-model.trim="condition.Query" placeholder="请输入学员名称" class="class-input" @keyup.native.enter="getFirstPage"></el-input>
                    </div>
                    <div class="filter-item">
                        <el-button type="primary" @click="getFirstPage" :disabled="loading">查询</el-button>
                        <el-button plain @click="reset" :disabled="loading">重置</el-button>
                    </div>
                    <div class="filter-item flex-center" @click.stop="showMore">
                        <el-link type="primary" underline="never">{{expandedMore?'收起':'展开'}}筛选&nbsp;
                            <el-icon><ArrowUp v-if="expandedMore"/><ArrowDown v-else/></el-icon>
                        </el-link>
                    </div>
                    <div v-show="expandedMore" class="advanced-filter-wrap">
                        <div class="filter-item" v-if="showCampus">
                            <el-select v-model="condition.CampusIds" :multiple="campusMultiple"
                            collapse-tags placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">{{ transToConfigDescript('所属校区') }}</p></template>
                                <el-option v-for="item in (isAllCampus?campusList:userCampuses)" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class=filter-item>
                            <input-tag :label="transToConfigDescript('班级名称')" placeholder="请选择" :isLine="true"
                                :multiple="true" :selected="condition.classSelected" 
                                @change="(selected: any) => handleClassChange(selected)" @click="() => selectClass()">
                                <template #btn-icon>
                                    <el-icon size="18px">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-xuanban"></use>
                                        </svg>
                                    </el-icon>
                                </template>
                            </input-tag>
                        </div>
                        <div class="filter-item" v-if="fixedCourse && fixedCourse.length > 0">
                            <el-select v-model="condition.ShiftIdList" :multiple="true"
                            collapse-tags placeholder="请选择" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">{{ transToConfigDescript('课程名称') }}</p></template>
                                <el-option v-for="item in fixedCourse" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item" v-else>
                            <input-tag :label="transToConfigDescript('课程名称')" placeholder="请选择" :isLine="true"
                                :multiple="true" :selected="condition.courseSelected" 
                                @change="(selected: any) => handleCourseChange(selected)" @click="() => selectCourse()">
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
                                v-model="condition.grades"
                                placeholder="不限"
                                filterable
                                multiple
                                collapse-tags
                                clearable
                                @change="getFirstPage"
                            >
                                <template #prefix><p class="search-input-label">年级</p></template>
                                <el-option
                                    v-for="item in gradeList"
                                    :value="item.ID"
                                    :label="item.Name"
                                    :key="item.ID"
                                ></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.status" placeholder="不限" clearable @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">学员状态</p></template>
                                <el-option value="0" label="试听"></el-option>
                                <el-option value="1" label="在读"></el-option>
                                <el-option value="2" label="停课"></el-option>
                                <el-option v-if="!hideQuit" value="3" label="休学"></el-option>
                                <el-option v-if="!hideDrop" value="99" label="已退学"></el-option>
                            </el-select>
                        </div>
                        
                       
                    </div>
                </div>
                <div class="table-wrap" v-loading="loading" ref="tableContainerRef">
                    <el-table ref="customTable" 
                        :data='list'
                        :highlight-current-row="!multi"
                        style="width: 100%;"  
                        @row-dblclick="confirmChoose" 
                        @row-click="chooseIt"
                        :height="tableHeight">
                        <template #empty>
                            <el-empty description="暂无数据" :image="globalData.emptyPng" :image-size="100"/>
                        </template>
                        <el-table-column v-if="multi" prop="ID" key="ID" width="30px" align="center" fixed>
                            <template #header="scope">
                                <el-checkbox v-model="allChecked"></el-checkbox>
                            </template>
                            <template #default="scope">
                                <el-checkbox @click.native.prevent :model-value="!!checkedList.find((el:any)=>{return el==scope.row.ID})"></el-checkbox>
                            </template>
                        </el-table-column>
                        <el-table-column prop="Name" label="学员名称" show-overflow-tooltip min-width="130"></el-table-column>
                        <el-table-column prop="Serial" label="学号" width="120px"></el-table-column>
                        <el-table-column prop="Sex" label="性别" width="70" show-overflow-tooltip>
                            <template #default="scope">
                                {{ scope.row.Sex == 1 ? '男' : scope.row.Sex == 2 ? '女' : '未知' }}
                            </template>
                        </el-table-column>
                        <el-table-column prop="SmsTel" label="手机号" width="120" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="CampusName" :label="transToConfigDescript('所属校区')" min-width="140" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="Grade" :label="transToConfigDescript('年级')" min-width="100"></el-table-column>
                        <!-- <el-table-column prop="Age" label="年龄" show-overflow-tooltip width="120px"></el-table-column> -->
                    </el-table>
                </div>
                <div class="pageination-wrapper flex-end" v-if="list.length">
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        :current-page.sync="page.PageIndex"
                        :page-sizes="[10,20,50,100,200]"
                        :page-size="page.PageSize"
                        layout="total, sizes, prev, jumper ,next"
                        :total="page.TotalCount" size="small">
                    </el-pagination>
                </div>
            </div>
			<div class="choosed-result-wrapper" v-if="multi && false">
            	<div class="choosed-result">
            		<div class="choosed-result-title">
            			<span>已选择：{{selected.length}}</span>
            			<el-link type="primary" underline="never" @click="removeAll">清空</el-link>
            		</div>
            		<div class="choosed-result-content">
            			<ul>
            				<li v-for="(item,index) in selected" :key="item.ID">
            					<span class="choosed-item-name" :title="item.Name">{{item.Name}}</span>
                                <a class="del-btn" @click="remove(item.ID,index)"><el-icon><deleteSVG></deleteSVG></el-icon></a>
            				</li>
            			</ul>
            		</div>
            	</div>
            </div>
		</div>
        <template #footer>
            <div :class="multi?'flex-end':'flex-between'">
                <div class="single-choosed-wrapper" v-if="!multi">
                    <span class="single-choosed-name">已选择：{{oneChoosed?oneChoosed.Name:''}}</span>
                </div>
                <div>
                    <el-button plain @click="close">取消</el-button>
                    <el-button type="primary" @click="submit">确定</el-button>
                </div>
            </div>
        </template>
	</el-dialog>
    
    <!-- 班级选择组件 -->
    <chooseClass ref="chooseClassRef" />
    <!-- 课程选择组件 -->
    <chooseCourse ref="chooseCourseRef" />
</template>

<script lang="ts" setup>
import $ from 'jquery'
import { storeToRefs } from 'pinia';
import { useConfigs,useCurrentCampuses,useUserCampuses,useYears } from '@/store';
import { useDictFieldsStore } from '@/store/dict';

const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);

import { queryStudentBreifExam, getGradeList } from '@/api';
import { transToConfigDescript,formatNumber } from '@/utils/filters/filters';
import { ArrowUp,ArrowDown } from '@element-plus/icons-vue';
import { cloneDeep } from 'lodash';

import deleteSVG from '@/assets/svg/delete.svg'
import { computed, getCurrentInstance, h, nextTick, reactive, ref, shallowRef } from 'vue';
import { TableInstance } from 'element-plus';
import { useOrganizationStore } from '@/store/organization';
import { checkPageIndex } from '@/utils';
import InputTag from '@/components/common/input-tag/inputTag.vue';
import chooseClass from '@/components/popup/chooseClass.vue';
import chooseCourse from '@/components/popup/chooseCourse.vue';

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const configs=computed(()=>{
    return useConfigs().configs
})
const organizationStore=useOrganizationStore()
const { campusList }=storeToRefs(organizationStore)

// 获取适用校区
const fixedCampusIds = ref<string[]>([]);
// 根据适用校区获取固定校区列表
const userCampuses=computed(()=>{
    // 根据适用校区 CampusIds 过滤出对应校区列表
    let userCampuses:any = useUserCampuses().userCampuses
    if(fixedCampusIds.value.length > 0){
        userCampuses = userCampuses.filter((item: any) => fixedCampusIds.value.includes(item.ID))
    }
    return userCampuses
})
// 获取适用课程
const fixedCourse = ref<any[]>([]);


const SHIFT_GRADE = computed(() => {
    return dictFields.value('SHIFT_GRADE')
})

const gradeList = ref([] as any)


const currentCampus=computed(()=>{
    return useCurrentCampuses().campusList
})

const dialogVisible=ref(false)
// 弹框参数 start
const multi=ref(false)
const popTitle=ref('选择学员')
const choosed=ref([] as any)
const showCampus=ref(false)
const hideQuit=ref(false)
const hideDrop=ref(false)
const required=ref(false)
const courseID=ref('')
const isAllCampus=ref(false)    //默认展示可操作校区
const campusMultiple=ref(false)  // 控制校区选择器是否多选
// 弹框参数 end

const selected=ref([] as any)
const selectedID=ref([] as any)
const expandedMore=ref(false)
const loading=ref(false)
const list=ref([] as any)
const checkedList=ref([] as any)    
const oneChoosed=ref(null as any)
const page=ref({
    PageIndex:1,
    PageSize:20,
    TotalCount:0
})
const sort = reactive({
	sort: 'Name',
	desc: 0,
})
const defaultCondition:any={
    Query: "",
    CampusIds: [],
    // ShiftId: '',
    // ShiftName: '',
    // classId: '',
    // className: '',
    classSelected: [], // 班级选择
    courseSelected: [], // 课程选择
    ShiftIdList: [],
    classIds: [],
    inDateStart: '',
    inDateEnd:'',
    flag: 1,
    grades: [], // 课程年级
    types: [], // types
    status: -1, // 学员状态
    isCrossCampus: 0, // 是否跨校区
    isFilterDropOut: 0, // 是否查询休学和退学的学员
    isSimplifyQuery: 0, // 是否使用简写版学员查询
    masterUserId: '', // 学管师
    salePersonId: '', // 主责任人ID
    school: '', // 公立学校
    // grade:0
    studentLabelIds: [], // 学员标签
    sort: "name",
    desc: 0,
    isSingle:0,
    campusFlag:0,
    queryNoLimit:0,
    masterIdFlag:0,
    isStudentShiftStop:0,
    hasAmount:0,
    isOnlyShowOneToOneStudent:0,

}
const condition=ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const newCondition=ref<typeof defaultCondition>(cloneDeep(defaultCondition))
function showMore(){
    expandedMore.value=!expandedMore.value
    setHeight()
}
const allChecked=computed({
    get:()=>{
        let length=checkedList.value.length;
	    return length===0?false:checkedList.value.length===list.value.length;
    },
    set:(val:boolean)=>{
        if(val){
            let ids:any=[];
            list.value.forEach((item:any)=>{
                ids.push(item.ID);
                if(selectedID.value.indexOf(item.ID)===-1){
                    selectedID.value.push(item.ID);
                    selected.value.push(item);
                }
            })
            checkedList.value=ids;
        }else{
            list.value.forEach((item:any)=>{
                let index=selectedID.value.indexOf(item.ID);
                if(index!==-1){
                    selectedID.value.splice(index,1);
                    selected.value.splice(index,1);
                }
            })
            checkedList.value=[];
        }
    }
})

// 获取考试分数等级字典值
const loadGradeList = async () => {
    try {
        console.log('开始获取年级列表')
        
        const res = await getGradeList({
            PageIndex: 1,
            PageSize: 1000,
            campus: currentCampus.value
        })
        
        console.log('年级列表响应:', res)
        
        if (res.ErrorCode === 200 && res.Data) {
            // 将对象形式的数据转换为数组形式 [{ID: '', Name: ''}]
            // 对象且有值时，转换为数组形式
            if (typeof res.Data === 'object' && Object.keys(res.Data).length > 0) {
                gradeList.value = Object.entries(res.Data)?.map(([id, name]) => ({
                    ID: id,
                    Name: name
                }))
            } 
            console.log('年级列表加载成功:', gradeList.value)
        } else {
            console.log(res.ErrorMsg || '获取年级列表失败')
            gradeList.value = []
        }
    } catch (error) {
        console.error('获取年级列表失败:', error)
        gradeList.value = []
    }
}


//多选选择
function chooseIt(row:any,column:any){
    console.log('row', row)
    let item=row,index=row.$index;
    if(multi.value){
        let checkedIndex=checkedList.value.indexOf(item.ID);
        if(checkedIndex===-1){
            checkedList.value.push(item.ID);
            selectedID.value.push(item.ID);
            selected.value.push(item);
        }else{
            checkedList.value.splice(checkedIndex,1);
            let selectedIndex=selectedID.value.indexOf(item.ID);
            selected.value.splice(selectedIndex,1);
            selectedID.value.splice(selectedIndex,1);
        }
    }else{
        customTable.value?.setCurrentRow(row);
        oneChoosed.value=item;
    }
}

//单选双击选择
function confirmChoose(item:any){
    if(!multi.value){
        oneChoosed.value=item;
        submit();
    }
}

//移除选中项
function remove(id:string,index:number){
    selectedID.value.splice(index,1);
    selected.value.splice(index,1);
    let index1=checkedList.value.indexOf(id);
    if(index1!=-1){
        checkedList.value.splice(index,1);
    }
}

//清空选中项
function removeAll(){
    checkedList.value=[];
    selected.value=[];
    selectedID.value=[];
}

//确认
function submit(){
    let params:any={}
    if( multi.value ){
        if( required.value&&selected.value.length === 0 ){
            ElMessage({
                message: '请至少选择一个班级。',
                type: 'warning',
            })
            return ;
        }
        params.data=selected.value
    }else {
        if( required.value&&!oneChoosed.value ){
            ElMessage({
                message: '请先选择班级。',
                type: 'warning',
            })
            return;
        }
        params.data=oneChoosed.value
    }
    _resolve(params)
    close()
}

//查第一页
function getFirstPage(){
    page.value.PageIndex=1;
    funcQuery();
}
function handleSizeChange(val:any) {
    page.value.PageSize = val;
    page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
    funcQuery();
}
function handleCurrentChange(val:any){
    page.value.PageIndex=val
    funcQuery()
}

function reset(){
    restCondition()
    getFirstPage();
}

function restCondition(){
	Object.assign(condition.value,cloneDeep(newCondition.value))
}

function resetDefaultCondition(){
    condition.value=cloneDeep(defaultCondition)
}

// 班级选择相关函数
function selectClass() {
    chooseClassRef.value?.open({
        multi: true,
        choosed: condition.value.classSelected || [],
        showCampus: true,
        showShiftType: true,
        campusID: condition.value.CampusIds?.join(',') || '',
        showClassStatusFilter: true,
        condition: {
            finished: 0
        }
    }).then((res: any) => {
        if (res && res.data) {
            condition.value.classSelected = res.data;
            getFirstPage();
        }
    }).catch((err: any) => {
        console.error('选择班级失败:', err);
    });
}

function handleClassChange(selected: any) {
    condition.value.classSelected = selected;
    getFirstPage();
}

// 课程选择相关函数
function selectCourse() {
    chooseCourseRef.value?.open({
        multi: true,
        choosed: condition.value.courseSelected || [],
        showCampus: true,
        showShiftType: true,
        campusID: condition.value.CampusIds?.join(',') || '',
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

function handleCourseChange(selected: any) {
    condition.value.courseSelected = selected;
    getFirstPage();
}

//取消
function close(){
    resetDefaultCondition()
    removeAll()
    oneChoosed.value=null
    dialogVisible.value=false
    _reject()
}

//查询
function funcQuery() {
    loading.value=true
    list.value = [];
    checkedList.value = [];
    let params:any={}
    Object.assign(params, {
        ...page.value,
        ...condition.value,
        ...sort
    })
    params.status=params.status==0?0:!params.status?-1:params.status
    if(!showCampus.value){
        params.CampusIds=params.CampusIds||currentCampus.value;
    }
    
    // 处理班级选择数据
    if(params.classSelected && params.classSelected.length > 0) {
        params.classIds = params.classSelected.map((item: any) => item.ID);
    }
    
    // 处理课程选择数据
    if(fixedCourse.value && fixedCourse.value.length > 0) {
    }else{
        params.ShiftIdList = params.courseSelected.map((item: any) => item.ID);
    }

    console.log('params', params)
    queryStudentBreifExam(params).then((res)=> {
        list.value =  res.Data.List.map((item: any) => ({
            ...item,
            ID: item.Id
        }))||[];
        let checkedArr:any= [] ;
        list.value.forEach((item:any)=>{
            if(selectedID.value.indexOf(item.ID)!==-1){
                checkedArr.push(item.ID);
            }
        })
        checkedList.value=checkedArr;
        Object.assign(page.value, {
            PageIndex: res.Data.PageIndex,
            PageCount: res.Data.TotalPages,
            PageSize: res.Data.PageSize,
            TotalCount: res.Data.TotalCount,
        })
        
        setHeight()
    }).finally(()=>{
        loading.value=false
    })
}

const tableHeight = ref(408);
const tableContainerRef = ref();
const customTable = ref<TableInstance>();
const chooseClassRef = ref();
const chooseCourseRef = ref();
function setHeight() {
    if(tableContainerRef.value){
        tableHeight.value=3000 //撑开一下需要计算高度
        nextTick(()=>{
            const h =tableHeight.value;
            let tableH=list.value.length?((list.value.length+1)*37)+1:408
            tableHeight.value = tableH;
            
            // if(tableHeight.value>$(tableContainerRef.value)[0].clientHeight){
                tableHeight.value=$(tableContainerRef.value)[0].clientHeight
            // }
            if (h !==tableHeight.value && customTable.value) {
                customTable.value.doLayout();
            }
        })
    }
}

let _resolve = null as any
let _reject = null as any
function open(params: any) {
    let promise = new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
    dialogVisible.value=true
    multi.value=params.multi||false
    popTitle.value=params.popTitle||'选择学员'
    choosed.value=params.choosed?cloneDeep(params.choosed):[];
    showCampus.value=params.showCampus||false
    isAllCampus.value=params.isAllCampus||false
    hideDrop.value=params.hideDrop||false
    hideQuit.value=params.hideQuit||false
    required.value=params.required||false
    courseID.value=params.courseID||''
    campusMultiple.value=params.campusMultiple||false
    let campusIDs = currentCampus.value.split(',')||[]
    if(showCampus.value&&campusIDs.length===1){
        condition.value.CampusIds=campusIDs;
    }
    if(hideDrop.value&&hideQuit.value){
        condition.value.IsFilterDropout=1
    }
    if(params.condition){
		Object.assign(condition.value, params.condition)
	}
    // 记录下来适用校区
    fixedCampusIds.value=params.condition?.CampusIds||[];
    console.log('fixedCampusIds', fixedCampusIds.value)

    // 记录下来适用课程
    fixedCourse.value=params.condition?.courseSelected||[];
    console.log('fixedCourse', fixedCourse.value)
    // 处理课程选择数据
    if(fixedCourse.value && fixedCourse.value.length > 0) {
        
        condition.value.ShiftIdList = fixedCourse.value.map((item: any) => item.ID);
    }

	//需要记录下来设置的condition，以免重置以后有问题
    Object.assign(newCondition.value,cloneDeep(condition.value))
    if(multi.value){
        selected.value=choosed.value?cloneDeep(choosed.value):[];
        let ids:any=[];
        selected.value.forEach((item:any)=>{
            ids.push(item.ID);
        })
        selectedID.value=ids;
    }
    funcQuery();
    // 获取年级
    loadGradeList();
    return promise;
}


defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
    :deep(.wtwo-date-editor) {
        .wtwo-range__icon {
            width: 4em!important;
            font-style: normal;
            color:#606266;
        }
    }
	.chooseStudentExam {
        .box-wrapper-table{
            display: flex;
            height: 506px!important;
            padding: 0px;
            margin-bottom:10px;
        }
        .fixed-table-box{
            flex: 1;
        }
        .multi-choosed-box-wrapper{
            .fixed-table-box{
                // margin-right: 12px;
                width: calc(100% - 306px)!important;
            }
        }
		.single-choosed-wrapper{
            text-align: left;
			width: calc(100% - 150px)!important;
			height: 32px;
			display: table;
		}
		.single-choosed-name{
			vertical-align: middle;
			display: table-cell;
		}
        
        
        .advanced-filter-wrap{
            .filter-item{
                width: 262px;
                .el-select{
                    width: 100%;
                }
            }
        }
	}
</style>

<style lang="scss">
    .chooseStudentExam {
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