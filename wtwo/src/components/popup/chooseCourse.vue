<template>
	<!-- 选课程 -->
	<el-dialog 
        v-model="dialogVisible" 
        :title="transToConfigDescript(popTitle)" 
        :width="multi?'1180px':'870px'"
        :close-on-click-modal="false" 
        :append-to-body="true"
        :destroy-on-close="true" 
        :align-center="true" 
        class="chooseCourse" 
        draggable
        @opened="handleOpened"
        @close="close" 
    >
		<div class="box-wrapper-table" :class="{ 'multi-choosed-box-wrapper':multi}">
            <div class="fixed-table-box">
                <div class="modal-filter multi-conditional">
                    <div class="filter-item mr-20px" :class="showShiftType?'w-262px':'w-534px'">
                        <el-input v-model.trim="condition.Query" :placeholder="transToConfigDescript('请输入课程名称')" class="class-input" @keyup.native.enter="getFirstPage" ref="courseInputRef"></el-input>
                    </div>
                    <div class="filter-item mr-20px w-295px" v-if="showShiftType">
                        <el-select v-model="shiftType" placeholder="不限" collapse-tags collapse-tags-tooltip multiple clearable @change="getFirstPage"> 
                            <template #prefix><p class="search-input-label">教学形式</p></template>
                            <el-option :value="4" label="集体班"></el-option>
                            <el-option :value="1" label="一对一"></el-option>
                            <el-option :value="2" label="一对多"></el-option>
                        </el-select>
                    </div>
                    <div class="filter-item">
                        <el-button type="primary" @click="getFirstPage" :disabled="loading">查询</el-button>
                        <el-button plain @click="reset" :disabled="loading">重置</el-button>
                    </div>
                    <div class="filter-item flex-center" @click="showMore">
                        <el-link type="primary" underline="never">{{expandedMore?'收起':'展开'}}筛选&nbsp;
                            <el-icon><ArrowUp v-if="expandedMore"/><ArrowDown v-else/></el-icon>
                        </el-link>
                    </div>
                    <div v-show="expandedMore" class="advanced-filter-wrap">
                        <div class="filter-item" v-if="showCampus">
                            <el-select v-model="condition.campus" :placeholder="transToConfigDescript('校区')" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">{{ transToConfigDescript('校区') }}</p></template>
                                <el-option v-for="item in campusList" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.Year" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">年份</p></template>
                                <el-option v-for="item in years" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.Term" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">期段</p></template>
                                <el-option v-for="item in CLASS_TERM" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.Grade" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">年级</p></template>
                                <el-option v-for="item in SHIFT_GRADE" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.Subject" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">科目</p></template>
                                <el-option v-for="item in SUBJECT" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.Category" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">类型</p></template>
                                <el-option v-for="item in SHIFT_CAT" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="condition.ClassType" placeholder="不限" filterable clearable @change="getFirstPage">
                                <template #prefix><p class="search-input-label">班型</p></template>
                                <el-option v-for="item in CLASS_TYPE" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                            </el-select>
                        </div>
                        <div class="filter-item">
                            <el-select v-model="status" placeholder="不限" multiple clearable @change="getFirstPage"> 
                                <template #prefix><p class="search-input-label">状态</p></template>
                                <el-option :value="1" label="启用"></el-option>
                                <el-option :value="0" label="停用"></el-option>
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
                        <el-table-column fixed v-if="multi" prop="ID" key="ID" width="40px" align="center">
                            <template #header="scope">
                                <el-checkbox v-model="allChecked"></el-checkbox>
                            </template>
                            <template #default="scope">
                                <el-checkbox @click.native.prevent :model-value="!!checkedList.find((el:any)=>{return el==scope.row.ID})"></el-checkbox>
                            </template>
                        </el-table-column>
                        <el-table-column fixed prop="Name" min-width="200px" :label="transToConfigDescript('课程名称')" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="IsOneToOne" :label="transToConfigDescript('教学形式')" width="100px">
                            <template #default="scope">
                                {{scope.row.IsOneToOne==0?'集体班':scope.row.IsOneToOne==1?'一对一':'一对多'}}
                            </template>
                        </el-table-column>
                        <el-table-column prop="Year" label="年份" width="100px" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="GradeName" label="年级" width="100px" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="SubjectName" label="科目" width="100px" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="CategoryName" label="类型" width="100px" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="ClassTypeName" label="班型" width="100px" show-overflow-tooltip></el-table-column>
                        <el-table-column prop="Status" label="状态" width="70px" show-overflow-tooltip>
                            <template #default="scope">
                                {{scope.row.Status==1?'已启用':'已停用'}}
                            </template>
                        </el-table-column>
                        <el-table-column prop="OriginalUnit" label="计费形式" width="80">
                            <template #default="scope">
                                {{ configs.IsOpenShiftForDay==1?scope.row.OriginalUnit: scope.row.UnitCode==3?'月':scope.row.Unit}}
                            </template>
                        </el-table-column>
                        <el-table-column v-if="courseShiftPrice" prop="UnitPrice" label="价格（元）" align="right" width="120">
                            <template #default="scope">{{ formatNumber(scope.row.UnitPrice,2,'') }}</template>
                        </el-table-column>
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

import { queryShift } from '@/api';
import { transToConfigDescript,formatNumber } from '@/utils/filters/filters';
import { ArrowUp,ArrowDown } from '@element-plus/icons-vue';
import { cloneDeep, sum } from 'lodash';

import deleteSVG from '@/assets/svg/delete.svg'
import { computed, getCurrentInstance, nextTick, ref } from 'vue';
import { TableInstance, InputInstance } from 'element-plus';
import { checkPageIndex } from '@/utils';

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const courseShiftPrice=ref(window.$xgj.op('courseShiftPrice')) //查看价格的权限

const configs=computed(()=>{
    return useConfigs().configs
})
const years=computed(()=>{
    return useYears().years;
})
const campusList=computed(()=>{
    return useUserCampuses().userCampuses
})

const CLASS_TERM=computed(()=>{
    return dictFields.value('CLASS_TERM');
})
const SHIFT_GRADE=computed(()=>{
    return dictFields.value('SHIFT_GRADE');
})
const SUBJECT=computed(()=>{
    return dictFields.value('SUBJECT');
})
const SHIFT_CAT=computed(()=>{
    return dictFields.value('SHIFT_CAT');
})
const CLASS_TYPE=computed(()=>{ 
    return dictFields.value('CLASS_TYPE');
})

const currentCampus=computed(()=>{
    return useCurrentCampuses().campusList
})

const dialogVisible=ref(false)
const courseInputRef = ref<InputInstance>();
// 弹框参数 start
const multi=ref(false)
const popTitle=ref('选择课程')
const choosed=ref([] as any)
const showCampus=ref(true)
const showAllCourse=ref(false)
const showShiftType=ref(true)
const required=ref(false)
const maxNum=ref(0)//课程最大数量
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
const defaultCondition:any={
    Grade: "",
    Subject: "",
    Category: "",
    ClassType: "",
    Query: "",
    campus: "",
    shiftType: 7,
    Term: "",
    Year: "",
    shiftCode: 0,
    status: 1,
    fromCharge:0,// 1(收费入口)，0(其他入口)
    IsPayAfterStudyShift:0, // 是否查询先学后付课程
    EnableSubject:0, // 全科课程
    isFilterDuration: 0,	// 是否过滤按次按时长计费的课程 0否 1是
    ShiftPriceIsHide:courseShiftPrice.value?0:1,
    isFilterDrainageClass:0, // 是否过滤引流课
    canRefund:0, // 是否只查询允许结转或退费的课程，默认查询所有课程（包括不允许结转或退费的）
    // isMonthUnit 默认查全部课程（0非按月计费课程，1是按月计费课程，2全部）
    isMonthUnit:2 // 是否查询按月计费的课程，默认查询所有课程（包括按次计费的）
}
const shiftType=ref([4,1,2])
const status=ref([1])
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
    // 根据 id 查找索引
    const idIndex = selectedID.value.indexOf(id);
    if (idIndex !== -1) {
        selectedID.value.splice(idIndex, 1);
        selected.value.splice(idIndex, 1);
    }
    
    // 从 checkedList 中移除
    let checkedIndex = checkedList.value.indexOf(id);
    if(checkedIndex !== -1){
        checkedList.value.splice(checkedIndex, 1);
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
                message: transToConfigDescript('请至少选择一个课程。'),
                type: 'warning',
            })
            return ;
        }
        if(maxNum.value&&selected.value.length>maxNum.value){
            ElMessage({
                message: transToConfigDescript('您选择的课程数量过多，一次最多支持'+maxNum.value+'个。'),
                type: 'warning',
            })
            return ;
        }
        params.data=selected.value
    }else {
        if( required.value&&!oneChoosed.value ){
            ElMessage({
                message: transToConfigDescript('请先选择课程。'),
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

function handleOpened(){
    nextTick(()=>{
        if(courseInputRef.value && typeof courseInputRef.value.focus==='function'){
            courseInputRef.value.focus();
        }else{
            (courseInputRef.value as any)?.input?.focus?.();
        }
    })
}

function reset(){
    restCondition()
    getFirstPage();
}

function restCondition(){
    Object.assign(condition.value,cloneDeep(newCondition.value))
    if(condition.value.shiftType==7){
        shiftType.value=[4,1,2]
    }else if(condition.value.shiftType==6){
        shiftType.value=[4,2]
    }else if(condition.value.shiftType==5){
        shiftType.value=[4,1]
    }else if(condition.value.shiftType==3){
        shiftType.value=[1,2]
    }else {
        shiftType.value=[condition.value.shiftType]
    }

    if(condition.value.status==5){
        status.value=[1,0]
    }else{
        status.value=[condition.value.status]
    }
}

function resetDefaultCondition(){
    condition.value=cloneDeep(defaultCondition)
    shiftType.value=[4,1,2]
    status.value=[1]
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
    let params:any={
        desc: 0,
        sort: "Name"
    }
    Object.assign(params, {
        ...page.value,
        ...condition.value
    })
    params.shiftType=sum(shiftType.value)||7;
    params.status=status.value.length==1?status.value[0]:5;
    if(!showCampus.value){
        params.campus=params.campus||currentCampus.value;
    }
    queryShift(params).then((res)=> {
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
    let campusIDs = currentCampus.value.split(',')||[]
    if(showCampus.value&&campusIDs.length===1){
        condition.value.campus=campusIDs[0];
    }
    dialogVisible.value=true
    multi.value=params.multi||false
    popTitle.value=params.popTitle||'选择课程'
    choosed.value=params.choosed?cloneDeep(params.choosed):[];
    showCampus.value=params.showCampus||false
    showAllCourse.value=params.showAllCourse||false
    showShiftType.value=params.showShiftType||false
    required.value=params.required||false
    maxNum.value=params.maxNum||0
    if(params.condition){
		Object.assign(condition.value, params.condition)
        shiftType.value=params.condition.shiftType||[4,1,2]
	}
    condition.value.shiftType=sum(shiftType.value)||7;
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
    getFirstPage();
    return promise;
}


defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
	.chooseCourse {
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