<template>
	<!-- 选学员 -->
    <el-dialog 
        v-model="dialogVisible" 
        :title="transToConfigDescript(popTitle)" 
        :width="multi?'1180px':'870px'" 
        :close-on-click-modal="false" 
        :append-to-body="true"
        :destroy-on-close="true" 
        :align-center="true" 
        class="chooseStudent" 
        draggable
        @opened="handleOpened"
        @close="close" 
    >
		<div class="box-wrapper-table" :class="{ 'multi-choosed-box-wrapper':multi}">
            <div class="fixed-table-box">
                <div class="modal-filter multi-conditional">
                    <div class="filter-item mr-20px w-534px">
                        <el-input v-model.trim="condition.Query" placeholder="请输入学员姓名、学号、手机号" class="class-input" @keyup.native.enter="getFirstPage" ref="studentInputRef"></el-input>
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
                        <div class="filter-item">
                            <el-select v-model="condition.campusid" placeholder="不限" :disabled="disabledCondition.indexOf('campusid')!=-1" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">{{ transToConfigDescript('校区') }}</p></template>
                                <el-option v-for="item in (isAllCampus?campusList:userCampuses)" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.status" placeholder="不限" clearable @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">学员状态</p></template>
                                <el-option value="0" label="试听"></el-option>
                                <el-option value="1" :label="transToConfigDescript('在读')"></el-option>
                                <el-option value="2" label="停课"></el-option>
                                <el-option v-if="!hideQuit" value="3" label="休学"></el-option>
                                <el-option v-if="!hideDrop" value="99" label="已退学"></el-option>
                            </el-select>
                        </div>
                        <div class=filter-item>
                            <el-input typepe="text" v-model.trim="condition.shiftName" placeholder="请输入" class="class-input" @keyup.native.enter="getFirstPage" clearable>
                                <template #prefix><p class="search-input-label">{{ transToConfigDescript('报读课程') }}</p></template>
                            </el-input>
                        </div>
                        <div class=filter-item>
                            <el-input typepe="text" v-model.trim="condition.className" placeholder="请输入" class="class-input" @keyup.native.enter="getFirstPage" clearable>
                                <template #prefix><p class="search-input-label">{{ transToConfigDescript('所在班级') }}</p></template>
                            </el-input>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.Term" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">学员类别</p></template>
                                <el-option v-for="item in STUDENT_TYPE" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item" style="width: 534px;">
                            <el-date-picker 
                                type="daterange" 
                                unlink-panels 
                                range-separator="到" 
                                start-placeholder="开始日期"
                                end-placeholder="结束日期" 
                                style="width: 534px;"
                                :prefix-icon="customPrefix"
                                :shortcuts="dateShortcuts"
                                v-model="dateRange"
                                @change="getFirstPage"
                                @clear="getFirstPage"
                                value-format="YYYY-MM-DD"
                                format="YYYY-MM-DD"
                            >
                            </el-date-picker>
                        </div>
                    </div>
                    <div class="filter-item block" v-if="showOneToOneStudent">
                        <span class="switch-wrap">
                            <el-switch
                                v-model="condition.IsOnlyShowOneToOneStudent"
                                :active-value="1"
                                :inactive-value="0"
                                size="small"
                                @change="getFirstPage"
                                :disabled="loading||disabledOneToOneStudent"
                            ></el-switch>
                            <span class="switch-title"
                                >仅显示报读了1对1{{transToConfigDescript('课程')}}的学员</span
                            >
                        </span>
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
                            <el-empty :description="EnableStudentQueryEmpty?'暂无数据':'请输入需要查询学员的姓名/学号/电话'" :image="globalData.emptyPng" :image-size="100"/>
                        </template>
                        <el-table-column v-if="multi" prop="ID" key="ID" width="30px" align="center" fixed>
                            <template #header="scope">
                                <el-checkbox v-model="allChecked"></el-checkbox>
                            </template>
                            <template #default="scope">
                                <el-checkbox @click.native.prevent :model-value="!!checkedList.find((el:any)=>{return el==scope.row.ID})"></el-checkbox>
                            </template>
                        </el-table-column>
                        <el-table-column prop="Serial" label="学号" width="120px"></el-table-column>
                        <el-table-column prop="Name" label="姓名" show-overflow-tooltip min-width="130"></el-table-column>
                        <el-table-column prop="SMSTel" label="手机号" width="120" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="Age" label="年龄" show-overflow-tooltip width="120px"></el-table-column>
                        <el-table-column prop="Sex" label="性别" width="70" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="Grade" :label="transToConfigDescript('就读年级')" min-width="100"></el-table-column>
                        <el-table-column prop="CampusName" :label="transToConfigDescript('所属校区')" min-width="140" show-overflow-tooltip></el-table-column>
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
			<div class="choosed-result-wrapper" v-if="multi">
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
</template>

<script lang="ts" setup>
import $ from 'jquery'
import { storeToRefs } from 'pinia';
import { useConfigs,useCurrentCampuses,useUserCampuses,useYears } from '@/store';
import { useDictFieldsStore } from '@/store/dict';

const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);

import { queryStudentBreif } from '@/api';
import { transToConfigDescript,formatNumber } from '@/utils/filters/filters';
import { ArrowUp,ArrowDown } from '@element-plus/icons-vue';
import { cloneDeep } from 'lodash';

import deleteSVG from '@/assets/svg/delete.svg'
import { computed, getCurrentInstance, h, nextTick, reactive, ref, shallowRef, watch } from 'vue';
import { TableInstance, InputInstance } from 'element-plus';
import { useOrganizationStore } from '@/store/organization';
import { checkPageIndex, dateShortcuts } from '@/utils';

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const configs=computed(()=>{
    return useConfigs().configs
})
const EnableStudentQueryEmpty=computed(()=>{//学员查询时，“学员姓名”输入框是否可以为空：0不能为空，1可以为空，2学员信息管理页面可以为空，但选择学员的页面不能为空（默认）
    return configs.value.EnableStudentQueryEmpty==1
})
const organizationStore=useOrganizationStore()
const { campusList }=storeToRefs(organizationStore)
const userCampuses=computed(()=>{
    return useUserCampuses().userCampuses
})


const STUDENT_TYPE=computed(()=>{
    return dictFields.value('STUDENT_TYPE');
})

const currentCampus=computed(()=>{
    return useCurrentCampuses().campusList
})
const customPrefix = shallowRef({
  render() {
    return h('p', '报名日期');
  },
})

const dialogVisible=ref(false)
const studentInputRef = ref<InputInstance>();
// 弹框参数 start
const multi=ref(false)
const popTitle=ref('选择学员')
const choosed=ref([] as any)
const hideQuit=ref(false)
const hideDrop=ref(false)
const required=ref(false)
const courseID=ref('')
const isAllCampus=ref(false)    //默认展示可操作校区
const showOneToOneStudent=ref(false)
const disabledOneToOneStudent=ref(false)
const disabledCondition=ref([] as any)
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
    shiftId: '',
    shiftName:'',
    classId: '',
    className:'',
    indate1: "",
    indate2: "",
    status: "1",
    Query: "",
    signStatus: "-1",
    campusid:"",
    masterIdFlag:0 ,
    masterUserId: '' , // 学管师 
    grade: '0' ,  // 年级
    school: '' , // 公立学校
    IsSimplifyQuery: 0,   /// 是否使用简写版学员查询（用于收费的时候查询学生信息，只根据模糊搜索条件和校区id搜索
    salePersonId: '' , // 主责任人ID
    types: '',
    campusFlag:1,
    IsStudentShiftStop: 0,
    IsFilterDropout:0,
    isSingle:0,
    IsOnlyShowOneToOneStudent:0
}
const condition=ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const newCondition=ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const dateRange=ref([] as any)
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

//多选选择
function chooseIt(row:any,column:any){
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
                message: '请至少选择一个学员。',
                type: 'warning',
            })
            return ;
        }
        params.data=selected.value
    }else {
        if( required.value&&!oneChoosed.value ){
            ElMessage({
                message: '请先选择学员。',
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
    if(condition.value.indate1&&condition.value.indate2){
        dateRange.value=[condition.value.indate1,condition.value.indate2]
    }
}

function resetDefaultCondition(){
    condition.value=cloneDeep(defaultCondition)
    dateRange.value=[]
}

//取消
function close(){
    resetDefaultCondition()
    disabledOneToOneStudent.value=false
    showOneToOneStudent.value=false
    removeAll()
    oneChoosed.value=null
    dialogVisible.value=false
    list.value=[]
    _reject()
}

//查询
function funcQuery() {
    if(!EnableStudentQueryEmpty.value){
        if(!condition.value.Query){
            ElMessage.warning("请输入需要查询学员的姓名/学号/电话")
            return
        }else if(condition.value.Query.length>=4||/\W+/.test(condition.value.Query)||/\d{4,}|[a-zA-Z]{2,}/.test(condition.value.Query)){
        }else{
            ElMessage.warning("查询关键字长度不够")
            return
        }
    }
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
    if(dateRange.value.length){
        params.indate1=dateRange.value[0]
        params.indate2=dateRange.value[1]
    }
    queryStudentBreif(params).then((res)=> {
        list.value =  res.Data||[];
        let checkedArr:any= [] ;
        list.value.forEach((item:any)=>{
            if(selectedID.value.indexOf(item.ID)!==-1){
                checkedArr.push(item.ID);
            }
        })
        checkedList.value=checkedArr;
        Object.assign(page.value, {
            PageIndex: res.PageIndex,
            PageCount: res.PageCount,
            PageSize: res.PageSize,
            TotalCount: res.TotalCount,
        })
        
        setHeight()
    }).finally(()=>{
        loading.value=false
    })
}

const tableHeight = ref(408);
const tableContainerRef = ref();
const customTable = ref<TableInstance>();
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
    isAllCampus.value=params.isAllCampus||false
    hideDrop.value=params.hideDrop||false
    hideQuit.value=params.hideQuit||false
    required.value=params.required||false
    courseID.value=params.courseID||''
    showOneToOneStudent.value=params.showOneToOneStudent||false
    disabledOneToOneStudent.value=params.disabledOneToOneStudent||false
    disabledCondition.value=params.disabledCondition||[]
    if(hideDrop.value&&hideQuit.value){
        condition.value.IsFilterDropout=1
    }
    if(params.condition){
		Object.assign(condition.value, params.condition)
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
    if(EnableStudentQueryEmpty.value||(!EnableStudentQueryEmpty.value&&condition.value.Query)){
        getFirstPage();
    }
    return promise;
}


defineExpose({
	open,
})

function handleOpened(){
    nextTick(()=>{
        if(studentInputRef.value && typeof studentInputRef.value.focus === 'function'){
            studentInputRef.value.focus();
        }else{
            (studentInputRef.value as any)?.input?.focus?.();
        }
    })
}
</script>

<style lang="scss" scoped>
    :deep(.wtwo-date-editor) {
        .wtwo-range__icon {
            width: 4em!important;
            font-style: normal;
            color:#606266;
        }
    }
	.chooseStudent {
        
        .box-wrapper-table{
            display: flex;
            height: 506px!important;
        }
        .fixed-table-box{
            flex: 1;
        }
        .multi-choosed-box-wrapper{
            .fixed-table-box{
                margin-right: 12px;
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