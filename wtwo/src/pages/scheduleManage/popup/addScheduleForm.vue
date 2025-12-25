<template>
    <el-drawer
		v-model="drawer"
		:title="opType === 1 ? '新增日程' : (!byPlan?'仅编辑本次日程':'编辑全部日程')"
		direction="rtl"
		size="550px"
		class="addScheduleForm"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
        <div class="drawer-body-wrap" v-loading="loading">
            <pageAttentionTips v-if="opType==2" class="ml-16px mb-6px">
                <div v-if="!byPlan">仅调整“当前日程”，如同批次创建了多个日程，则其他日程不会更改！</div>
                <div v-else>本次调整“所有日程”，如同批次创建了多个日程，则其他日程会一起更改！</div>
            </pageAttentionTips>
            <el-scrollbar class="px-16px!">
                <el-form :model="form" ref="formRef" label-position="top" :scroll-to-error="true">
                    <el-form-item label="日程名称" prop="Title" :rules="[{required: true,message: '请输入日程名称'}]">
                        <el-input v-model="form.Title" placeholder="请输入" />
                    </el-form-item>
                    <el-form-item label="参与员工" prop="TeacherList" :rules="[{required: true,message: '请选择参与员工',trigger:'change'}]">
                        <input-tag
                            placeholder="请选择"
                            :selected="form.TeacherList"
                            :isLine="true"
                            :multiple="true"
                            @click="selectTeacher"
                            @change="() => formRef?.validateField('TeacherList')"
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
                    </el-form-item>
                    <el-form-item v-if="!byPlan&&opType==2" label="日期" :rules="[{required: true,message: '请选择日期'}]">
                        <el-date-picker v-model="form.editSingleDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" class="w-100%!"/>
                    </el-form-item>
                    <template v-if="byPlan||opType==1">
                        <el-form-item label="日程规则" prop="PlanRule">
                            <div class="flex-center w-[100%]">
                                <el-select v-model="form.PlanRule" placeholder="请选择" :class="form.PlanRule==1?'w-110px!':'w-[100%]!'">
                                    <el-option :value="1" label="重复日程"></el-option>
                                    <el-option :value="0" label="自由日程"></el-option>
                                </el-select>
                                <el-select v-model="form.CourseMode" placeholder="请选择" class="ml-12px flex-1" v-if="form.PlanRule === 1">
                                    <el-option value="Weekly" label="每周重复"></el-option>
                                    <el-option value="Biweekly" label="隔周重复"></el-option>
                                    <el-option value="Daily" label="每天重复"></el-option>
                                    <el-option value="AlternateDay" label="隔天重复"></el-option>
                                </el-select>
                            </div>
                        </el-form-item>
                        <!-- 重复排课 -->
                        <el-form-item
                            label="重复范围"
                            class="half-width"
                            :rules="[{required: true,message: '请选择开始日期'}]"
                            v-if="form.PlanRule==1"
                        >
                            <el-form-item prop="StartDate" :rules="[{required: true,message: '请选择开始日期'}]" style="flex: 1;">
                                <el-date-picker v-model="form.StartDate" type="date"
                                    placeholder="请选择" value-format="YYYY-MM-DD" @change="changeStartDate" class="w-100%!"></el-date-picker>
                            </el-form-item>
                            <span class="mx-10px">至</span>
                            <el-form-item prop="EndDate" :rules="[{ required: true, message: '请选择结束日期' }, { validator: validateEndDate }]" style="flex: 1;">
                                <el-date-picker v-model="form.EndDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" 
                                    class="w-100%!"></el-date-picker>
                            </el-form-item>
                        </el-form-item>
                        <!-- 自由排课 -->
                        <el-form-item prop="DateList" label="日程日期" class="half-width" v-if="form.PlanRule==0" :rules="[{required: true,message: '请选择日程日期'}]">
                            <multiple-dates-picker
                                v-model="form.DateList"
                                @cleared="onDateListCleared"
                            />
                        </el-form-item>
                        <el-form-item v-if="form.PlanRule==1&&(form.CourseMode=='Weekly'||form.CourseMode=='Biweekly')" label="星期" prop="Weekdays" :rules="[{required: true,message: '请选择星期'}]">
                            <el-select v-model="form.Weekdays" multiple>
                                <el-option v-for="item in weekList" :value="item.ID" :label="item.Name"></el-option>
                            </el-select>
                        </el-form-item>
                    </template>
                    <el-form-item label="时长" prop="TimeRange" :rules="[{required: true,message: '请选择时长'},{ validator: validateTimeRange }]">
                        <course-time-range
                            v-model="form.TimeRange"
                            pickerWidth="200px"
                        />
                    </el-form-item>
                    <el-form-item :label="transToConfigDescript('所属校区')" prop="CampusID" :rules="[{required: true,message: transToConfigDescript('请选择所属校区')}]">
                        <el-select v-model="form.CampusID" placeholder="请选择" filterable clearable>
                            <el-option v-for="item in campusList" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="日程类型" prop="ContentID">
                        <el-select v-model="form.ContentID" placeholder="请选择">
                            <el-option v-for="item in SCHEDULE_TYPE" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="日程备注" prop="Remark">
                        <el-input v-model="form.Remark" type="textarea" :rows="2" placeholder="请输入" maxlength="3000"></el-input>
                    </el-form-item>
                </el-form>
            </el-scrollbar>
        </div>
        <template #footer>
			<div class="wtwo-drawer-footer flex-between">
				<div class="flex-center">
					<el-checkbox v-model="checkConflict" :true-value="1" :false-value="0" :disabled="loading||!NewCourse_IngoreCourseConflict">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
				</div>
				<div class="flex-center">
					<el-button @click="close" :disabled="loading">取消</el-button>
					<el-button type="primary" @click="submit" :disabled="loading"
						>确定</el-button
					>
				</div>
			</div>
		</template>
        <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
        <ConflictPrompt ref="conflictPromptRef" />
    </el-drawer>
</template>
<script lang="ts" setup>
import { cloneDeep } from 'lodash'
import { storeToRefs } from 'pinia';
import { ref, computed, watch, nextTick } from 'vue'
import { useDictFieldsStore } from '@/store/dict';
import { useUserCampuses } from '@/store';
import CourseTimeRange from '../components/course-time-range.vue';
import MultipleDatesPicker from '../components/multiple-dates-picker.vue';
import dayjs from 'dayjs';
import { addSchedulePlan, editSchedule, editSchedulePlan, getScheduleDetail, getSchedulePlan } from '@/api/arrange';
import ConflictPrompt from './conflictPrompt.vue';
import { transToConfigDescript } from '@/utils/filters/filters';

//权限
const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限

const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);
const SCHEDULE_TYPE=computed(()=>{
    return dictFields.value('SCHEDULE_TYPE');
})
const campusList=computed(()=>{
    return useUserCampuses().userCampuses
})
const weekList=ref([{
	ID:1,
	Name:'星期一'
},{
	ID:2,
	Name:'星期二'
},{
	ID:3,
	Name:'星期三'
},{
	ID:4,
	Name:'星期四'
},{
	ID:5,
	Name:'星期五'
},{
	ID:6,
	Name:'星期六'
},{
	ID:7,
	Name:'星期日'
}])
// 根据 weekList 的先后顺序对选择的星期进行排序
const weekdayOrderMap = computed(() => {
    const orderMap = new Map<number, number>()
    weekList.value.forEach((item, index) => {
        orderMap.set(item.ID, index)
    })
    return orderMap
})

// 组件本地状态
const drawer = ref(false)
const loading = ref(false)
const opType = ref(1)
const defaultForm: any = {
	Title:'',
    TeacherList:[],
    PlanRule:1,
    StartDate:'',
    EndDate:'',
    DateList:[],
    CourseMode:'Weekly',
    TimeRange:[],
    Weekdays:[],
    CampusID:'',
    ContentID:'',
    Remark:'',
    editSingleDate:''
}
const checkConflict=ref(1)
const byPlan=ref(false)
const form = ref<typeof defaultForm>(cloneDeep(defaultForm))
const formRef = ref()
const chooseEmpTableRef = ref()
function selectTeacher() {
    chooseEmpTableRef.value?.open({
        multi: true,
        choosed: form.value.TeacherList,
        disabledCondition:['StatusList']
    }).then((res: any) => {
        console.log(res)
        form.value.TeacherList = res.data
        // 手动触发验证
        formRef.value?.validateField('TeacherList')
    })
}

function onDateListCleared() {
    // 与原 clearDateList 的行为一致，保持校验体验
    formRef.value?.clearValidate(`DateList`)
    setTimeout(() => {
        formRef.value?.validateField(`DateList`)
    }, 0)
}

function validateEndDate(rule: any, value: any, callback: any) {
    // 仅在重复排课时校验
    if (form.value.PlanRule === 1) {
        const start = form.value.StartDate
        const end = form.value.EndDate
        if (start && end && dayjs(end).isBefore(dayjs(start))) {
            callback(new Error('结束日期不能早于开始日期'))
            return
        }
    }
    callback()
}

function validateTimeRange(rule: any, value: any, callback: any) {
    if (!Array.isArray(value) || value.length < 2) {
        callback(new Error('请选择时长'))
        return
    }
    const [start, end] = value
    if (!start || !end) {
        callback(new Error('请选择时长'))
        return
    }
    const toMinutes = (time: string) => {
        const parts = time.split(':')
        const hours = Number(parts[0])
        const minutes = Number(parts[1])
        return hours * 60 + minutes
    }
    if (toMinutes(end) <= toMinutes(start)) {
        callback(new Error('结束时间必须大于开始时间'))
        return
    }
    callback()
}

function changeStartDate(){
	checkEndDate()
}
function checkEndDate(){
	if(form.value.EndDate){
		// 清除之前的校验结果
		formRef.value?.clearValidate(`EndDate`)
		// 重新触发校验
		setTimeout(() => {
			formRef.value?.validateField(`EndDate`)
		}, 0)
	}
}
// 根据星期顺序自动排序选择
watch(() => form.value?.Weekdays, (newVal) => {
    if (!Array.isArray(newVal)) return
    const sorted = [...newVal].sort((a, b) => {
        const ai = weekdayOrderMap.value.get(a as number) ?? 999
        const bi = weekdayOrderMap.value.get(b as number) ?? 999
        return ai - bi
    })
    const isSame = newVal.length === sorted.length && newVal.every((v, i) => v === sorted[i])
    if (!isSame) {
        form.value.Weekdays = sorted as any
    }
}, { deep: false })

const conflictPromptRef = ref()
function submit() {
    formRef.value!.validate((valid:any) => {
        if (valid) {
            if(opType.value==2){
                if(byPlan.value){
                    sendPlanEdit()
                }else{
                    sendSingleEidt()
                }
            }else{
                sendAdd()
            }
            
        }
    })
}

function sendSingleEidt(){
    loading.value=true
    let params:any={
        CheckConflict:checkConflict.value,
        ID:form.value.ID,
        CampusID:form.value.CampusID,
        StartTime:form.value.editSingleDate+' '+form.value.TimeRange[0],
        EndTime:form.value.editSingleDate+' '+form.value.TimeRange[1],
        Title:form.value.Title,
        ContentID:form.value.ContentID,
        Remark:form.value.Remark,
        TeacherIDList:form.value.TeacherList.map((item:any)=>{return item.ID})
    }
    // 根据 ContentID 获取对应的名称
    const typeList = SCHEDULE_TYPE.value || []
    const matchedType = typeList.find((t:any) => String(t.ID) === String(form.value.ContentID))
    params.Content = matchedType ? matchedType.Name : ''
    editSchedule(params).then((res:any)=>{
        ElMessage.success('已修改日程')
        _resolve()
        drawer.value = false
    }).catch((error)=>{
      if(error.ErrorCode==409){
        conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{}).catch((back:any)=>{
            if(back&&back.closeCurrent){
                drawer.value=false
            }
        })
      }
    }).finally(()=>{
        loading.value=false
    })
}

function sendPlanEdit(){
    loading.value=true
    let params:any={
        CampusID:form.value.CampusID,
        Remark:form.value.Remark,
        Title:form.value.Title,
        ContentID:form.value.ContentID,
        CourseMode:form.value.PlanRule==0?'Free':form.value.CourseMode,
        IsHoliday:0,
        CheckConflict:checkConflict.value,
        SchedulePlanID:form.value.SchedulePlanID,
    }
    // 根据 ContentID 获取对应的名称
    const typeList = SCHEDULE_TYPE.value || []
    const matchedType = typeList.find((t:any) => String(t.ID) === String(form.value.ContentID))
    params.Content = matchedType ? matchedType.Name : ''
    if(form.value.PlanRule==0){
        params.StartDate=form.value.DateList[0]
        params.DateList=form.value.DateList
    }else{
        params.StartDate=form.value.StartDate
        params.EndDate=form.value.EndDate
    }
    let planList:any=[]
    if(form.value.PlanRule==0||(form.value.PlanRule==1&&(form.value.CourseMode=='Daily'||form.value.CourseMode=='AlternateDay'))){
        planList=[{
            Weekday:1,
            WeekName:'星期一',
            StartTime:form.value.TimeRange[0],
            EndTime:form.value.TimeRange[1],
            TeacherList:form.value.TeacherList.map((item:any)=>{return {ID:item.ID,Name:item.Name}}),
        }]
    }else{
        form.value.Weekdays.forEach((item:any)=>{
            planList.push({
                Weekday:item,
                WeekName:weekList.value.find((t:any)=>t.ID==item)?.Name,
                StartTime:form.value.TimeRange[0],
                EndTime:form.value.TimeRange[1],
                TeacherList:form.value.TeacherList.map((item:any)=>{return {ID:item.ID,Name:item.Name}}),
            })
        })
    }
    params.PlanList=planList
    editSchedulePlan(params).then((res:any)=>{
        ElMessage.success('已修改日程')
        _resolve()
        drawer.value = false
    }).catch((error)=>{
      if(error.ErrorCode==409){
        conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{}).catch((back:any)=>{
            if(back&&back.closeCurrent){
                drawer.value=false
            }
        })
      }
    }).finally(()=>{
        loading.value=false
    })
}

function sendAdd(){
    loading.value = true
    let params:any={
        Title:form.value.Title,
        Remark:form.value.Remark,
        IsHoliday:0,
        CheckConflict:checkConflict.value,
        CourseMode:form.value.PlanRule==0?'Free':form.value.CourseMode,
        CampusID:form.value.CampusID,
        ContentID:form.value.ContentID
    }
    
    // 根据 ContentID 获取对应的名称
    const typeList = SCHEDULE_TYPE.value || []
    const matchedType = typeList.find((t:any) => String(t.ID) === String(form.value.ContentID))
    params.Content = matchedType ? matchedType.Name : ''
    if(form.value.PlanRule==0){
        params.StartDate=form.value.DateList[0]
        params.DateList=form.value.DateList
    }else{
        params.StartDate=form.value.StartDate
        params.EndDate=form.value.EndDate
    }
    let planList:any=[]
    if(form.value.PlanRule==0||(form.value.PlanRule==1&&(form.value.CourseMode=='Daily'||form.value.CourseMode=='AlternateDay'))){
        planList=[{
            Weekday:1,
            WeekName:'星期一',
            StartTime:form.value.TimeRange[0],
            EndTime:form.value.TimeRange[1],
            TeacherList:form.value.TeacherList.map((item:any)=>{return {ID:item.ID,Name:item.Name}}),
        }]
    }else{
        form.value.Weekdays.forEach((item:any)=>{
            planList.push({
                Weekday:item,
                WeekName:weekList.value.find((t:any)=>t.ID==item)?.Name,
                StartTime:form.value.TimeRange[0],
                EndTime:form.value.TimeRange[1],
                TeacherList:form.value.TeacherList.map((item:any)=>{return {ID:item.ID,Name:item.Name}}),
            })
        })
    }
    params.PlanList=planList
    addSchedulePlan(params).then((res: any) => {
        ElMessage.success('已新增日程')
        _resolve()
        drawer.value = false
    }).catch((error)=>{
      if(error.ErrorCode==409){
        conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{}).catch((back:any)=>{
            if(back&&back.closeCurrent){
                drawer.value=false
            }
        })
      }
    }).finally(() => {
        loading.value = false
    })
}

function getSingleDetail(ID:string){
    loading.value = true
    getScheduleDetail({
        ID: ID
    }).then((res: any) => {
        form.value = res.Data
        // 处理 TimeRange 字段，从 StartTime 和 EndTime 构建
        if (res.Data && res.Data.StartTime && res.Data.EndTime) {
            // 提取时间部分（HH:mm），去掉日期部分
            const startTime = dayjs(res.Data.StartTime).format('HH:mm')
            const endTime = dayjs(res.Data.EndTime).format('HH:mm')
            form.value.TimeRange = [startTime, endTime]
            form.value.editSingleDate=dayjs(res.Data.StartTime).format('YYYY-MM-DD')
        }
    }).finally(() => {
        loading.value = false
    })
}

function getPlanDetail(ID:string){
    loading.value=true
    getSchedulePlan({
        PlanID:ID
    }).then((res:any)=>{
        let data=res.Data||{},
            info=data.ScheduleInfo||{}
        if(data.DetailList&&data.DetailList.length){
            form.value.TimeRange=[data.DetailList[0].StartTime,data.DetailList[0].EndTime]
            // 提取所有Weekday并排序
            const weekdays = data.DetailList.map((item:any) => item.Weekday)
            form.value.Weekdays = weekdays.sort((a:any, b:any) => a - b)
            form.value.TeacherList=data.DetailList[0].TeacherList
        }
        form.value.Title=info.Title||''
        form.value.PlanRule=data.CourseMode=='Free'?0:1
        form.value.CourseMode=data.CourseMode=='Free'?'Weekly':data.CourseMode
        form.value.CampusID=info.CampusID
        form.value.ContentID=info.ContentID
        form.value.Remark=info.Remark
        if(form.value.PlanRule==0){
            form.value.DateList=data.DateList
        }else{
            form.value.StartDate = dayjs(data.StartDate).format('YYYY-MM-DD')
            form.value.EndDate = dayjs(data.EndDate).format('YYYY-MM-DD')
        }
        
    }).finally(()=>{
        loading.value=false
    })
}

let _resolve: any = null,
	_reject: any = null 
const open = (params: any) => {
    opType.value = params.opType
    if(opType.value == 2){
        if(params.byPlan){
            form.value.SchedulePlanID=params.SchedulePlanID
            getPlanDetail(params.SchedulePlanID)
        }else{
            form.value.ID = params.ID
            getSingleDetail(params.ID)
        }
    }
    byPlan.value = params.byPlan||false
    
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		
		drawer.value = true
	})
}

function close() {
	drawer.value = false
    nextTick(()=>{
        form.value=cloneDeep(defaultForm)
        checkConflict.value=1
    })
    _reject && _reject()
}

defineExpose({
	open,
})
</script>
<style lang="scss" scoped>
.addScheduleForm{
    .drawer-body-wrap{
        padding: 10px 0!important;
    }
}
</style>