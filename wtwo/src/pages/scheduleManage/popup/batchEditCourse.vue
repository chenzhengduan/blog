<!-- 批量修改排课 -->
<template>
    <el-drawer v-model="drawer" title="批量修改" direction="rtl" size="650px" class="batchEditCourse" :close-on-click-modal="false" @close="close">
        <div class="drawer-body-wrap" v-loading="loading">
            <div class="mx-16px mt-12px">
                <pageAttentionTips>
                    <div>已选择 <span class="text-red">{{allList.length}}</span> 节排课
                        <span v-if="unabledList.length>0">，其中 <span class="text-red">{{unabledList.length}}</span> 节是“{{transToConfigDescript('已上课')}}、已取消”状态，不支持修改，修改时会为您跳过这类课</span>
                    </div>
                </pageAttentionTips>
            </div>
            <div class="drawer-content">
                <el-scrollbar ref="scrollbarRef">
                    <div class="drawer-content-wrap">
                        <el-form :model="form" ref="formRef" :validate-on-rule-change="false" :scroll-to-error="true">
                            <div class="batch-item-wrap">
                                <div class="flex-center">
                                    <el-checkbox v-model="checkConfig.IsCheckDate" :disabled="!isShowClassDateTime">{{transToConfigDescript('上课日期')}}</el-checkbox>
                                    <div v-if="!isShowClassDateTime" class="text-12px ml-12px color-[#999]">选中排课中包含了按月/按天计费{{transToConfigDescript('课程')}}，不支持修改{{transToConfigDescript('上课')}}日期。</div>
                                </div>
                                <div class="batch-item-content" v-if="checkConfig.IsCheckDate">  
                                    <el-form-item prop="Date" :rules="[{ required: true, message: transToConfigDescript('请选择上课日期') }]">
                                        <el-date-picker v-model="form.Date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="w-[300px]!"/>
                                    </el-form-item>
                                </div>
                            </div>
                            <div class="batch-item-wrap">
                                <div class="flex-center">
                                    <el-checkbox v-model="checkConfig.IsCheckTime" :disabled="!isShowClassDateTime">{{transToConfigDescript('上课时间')}}</el-checkbox>
                                    <div v-if="!isShowClassDateTime" class="text-12px ml-12px color-[#999]">{{transToConfigDescript('选中排课中包含了按月/按天计费课程，不支持修改上课时间。')}}</div>
                                </div>
                                <div class="batch-item-content" v-if="checkConfig.IsCheckTime">  
                                    <el-form-item prop="TimeRange" :rules="[{ required: true, message: transToConfigDescript('请选择上课时间') }, { validator: validateTimeRange }]">
                                        <course-time-range
                                            v-model="form.TimeRange"
                                            :campus-id="campusList.length==1?campusList[0]:''"
                                            :showCommonTime="campusList.length==1?true:false"
                                            @change="handleTimeRangeChange"
                                            :min-course-time="MinCourseTime"
                                            :max-course-time="MaxCourseTime"
                                            :class="campusList.length==1?'w-[323px]':'w-[300px]'"
                                            pickerWidth="200px"
                                        />
                                    </el-form-item>
                                </div>
                            </div>
                            <div class="batch-item-wrap" v-if="EnableCourseOnline">
                                <el-checkbox v-model="checkConfig.IsUpdateOnlineCourse" @change="handleUpdateOnlineCourseChange">{{transToConfigDescript('修改为线上课')}}</el-checkbox>
                                <div class="batch-item-content" v-if="checkConfig.IsUpdateOnlineCourse">  
                                    <el-checkbox :disabled="checkConfig.IsUpdateClassroomID==1" v-model="form.KeepOriginClassroom" :true-value="1" :false-value="0" @change="handleKeepOriginClassroomChange">保留排课的{{transToConfigDescript('上课')}}教室</el-checkbox>
                                </div>
                            </div>
                            <div class="batch-item-wrap" v-if="campusList.length==1">
                                <el-checkbox :disabled="form.KeepOriginClassroom==1" v-model="checkConfig.IsUpdateClassroomID">{{transToConfigDescript('上课教室')}}</el-checkbox>
                                <div class="batch-item-content" v-if="checkConfig.IsUpdateClassroomID">  
                                    <el-form-item prop="ClassroomID" :rules="[{ required: true, message: transToConfigDescript('请选择上课教室') }]">
                                        <el-select
                                            v-model="form.ClassroomID"
                                            placeholder="请选择"
                                            clearable
                                            filterable
                                        >
                                            <el-option
                                                v-for="item in classroomList"
                                                :key="item.ID"
                                                :value="item.ID"
                                                :label="item.Name"
                                                >{{ item.Name }}</el-option
                                            >
                                        </el-select>
                                    </el-form-item>
                                </div>
                            </div>
                            <div class="batch-item-wrap">
                                <el-checkbox v-model="checkConfig.IsUpdateMainTeacher">{{transToConfigDescript('任课老师')}}</el-checkbox>
                                <div class="batch-item-content" v-if="checkConfig.IsUpdateMainTeacher">
                                    <el-form-item prop="Teachers" :rules="[{ required: true, message: transToConfigDescript('请选择任课老师') }]">
                                        <input-tag
                                            placeholder="请选择"
                                            :selected="form.Teachers"
                                            :isLine="true"
                                            :multiple="false"
                                            @click="selectTableTeacher"
                                            :fieldMapping="{
                                                label: 'LabelName',
                                                value: 'ID',
                                                key: 'ID'
                                            }"
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
                                </div>
                            </div>
                            <div class="batch-item-wrap">
                                <el-checkbox v-model="checkConfig.IsUpdateAssistantTeacher">助教</el-checkbox>
                                <div class="batch-item-content" v-if="checkConfig.IsUpdateAssistantTeacher">  
                                    <el-form-item prop="Assistants" :rules="[{ required: true, message: '请选择助教' }]">
                                        <input-tag
                                            placeholder="请选择"
                                            :selected="form.Assistants"
                                            :isLine="true"
                                            :multiple="true"
                                            @click="selectTableAssistant"
                                            :fieldMapping="{
                                                label: 'LabelName',
                                                value: 'ID',
                                                key: 'ID'
                                            }"
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
                                </div>
                            </div>
                            <div class="batch-item-wrap">
                                <div class="flex-center">
                                    <el-checkbox v-model="checkConfig.IsUpdateSubjectID" :disabled="!canUpdateSubject">{{transToConfigDescript('上课科目')}}</el-checkbox>
                                    <div v-if="!canUpdateSubject" class="text-12px ml-12px color-[#999]">
                                        选中的排课需为全科{{transToConfigDescript('课程')}}，<span v-if="CourseIsClassSubject">{{transToConfigDescript('并且排课所属班级都为相同班级时，')}}</span>才支持修改科目。
                                    </div>
                                </div>
                                <div class="batch-item-content" v-if="checkConfig.IsUpdateSubjectID">
                                    <el-form-item prop="SubjectID" :rules="[{ required: true, message: '请选择上课科目' }]">
                                        <el-select v-model="form.SubjectID" placeholder="请选择" filterable :disabled="!canUpdateSubject">
                                            <el-option v-for="item in subjectList" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
                                        </el-select>
                                    </el-form-item>
                                </div>
                            </div>
                        </el-form>
                    </div>
                </el-scrollbar>
            </div>
            <div class="text-12px color-[#999] line-height-16px h-[30px] flex-center pl-5px" style="background-color: var(--wtwo-color-warning-light-9);">{{transToConfigDescript('注意！若选择的排课中存在多节“同班级、学员或课程”的排课，同时修改“上课日期、上课时间”会被判定冲突')}}</div>
        </div>
        <template #footer>
			<div class="wtwo-drawer-footer flex-between">
				<div class="flex-center">
					<el-checkbox :model-value="checkConflict" :true-value="1" :false-value="0" :disabled="loading||!NewCourse_IngoreCourseConflict" @click="changeCheckConflict">检查上课冲突</el-checkbox>
				</div>
				<div class="flex-center">
					<el-button @click="close" :disabled="loading">取消</el-button>
					<el-button type="primary" @click="submit" :disabled="loading">确认修改</el-button>
				</div>
			</div>
		</template>
        <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
        <workFlowPopup ref="workFlowPopupRef"></workFlowPopup>
        <ConflictPrompt ref="conflictPromptRef" />
    </el-drawer>
</template>
<script setup lang="ts">
import { querySysConfig } from '@/api';
import { useConfigs } from '@/store';
import { calculateDuration } from '@/utils/timeUtils';
import { computed, ref } from 'vue';
import CourseTimeRange from '../components/course-time-range.vue';
import { getClassSubject, putCourseAsk, putCourseList, queryClassroom } from '@/api/arrange';
import { storeToRefs } from 'pinia';
import { useDictFieldsStore } from '@/store/dict';
import { cloneDeep } from 'lodash';
import { checkTeacherType } from '@/utils/scheduleUtils';
import { dayjs, FormInstance } from 'element-plus';
import workFlowPopup from './workFlowPopup.vue';
import ConflictPrompt from './conflictPrompt.vue';
import PageAttentionTips from '@/components/common/page-attention-tips/pageAttentionTips.vue';
import { nextTick } from 'vue';
import { transToConfigDescript } from '@/utils/filters/filters';

const configs = computed(() => {
	return useConfigs().configs
})
const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);
const SUBJECT=computed(()=>{
    return dictFields.value('SUBJECT').filter((item:any)=>item.Status==1);
})
const CourseIsClassSubject=computed(()=>{//全科课程排课，是否只能选班级上设置的科目 0：否（默认），1：是
    return configs.value.CourseIsClassSubject==1
})
const EnableCourseOnline=computed(()=>{//在线课配置项 是否开启在线课堂菜单，1：开通（开通后，才显示在线课堂菜单）
    return configs.value.EnableCourseOnline==1
})
const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限
const drawer = ref(false)
const loading = ref(false)
const checkConflict = ref(1)
const allList=ref([] as any)
const editList=ref([] as any)
const unabledList=computed(()=>{
    return allList.value.filter((item:any)=>item.Finished!=0)
})
const isShowClassDateTime=computed(()=>{
    return !(editList.value.some((item:any)=>item.Unit==3))
})
const sameClassSelected=computed(()=>{
    return editList.value.length>0 && editList.value.every((item:any)=>item.ClassID===editList.value[0].ClassID)
})
const allEnableSubject=computed(()=>{
    return editList.value.every((item:any)=>item.EnableSubject==1)
})
const canUpdateSubject=computed(()=>{
    return (CourseIsClassSubject.value && sameClassSelected.value && allEnableSubject.value) || (!CourseIsClassSubject.value && allEnableSubject.value)
})
const EnbaleEmpIsClassTeacher=computed(()=>{
    return configs.value.EnbaleEmpIsClassTeacher
})
const EMPTYGUID='00000000-0000-0000-0000-000000000000'
const MinCourseTime=ref('')
const MaxCourseTime=ref('')
const campusList=ref([] as any)
const classroomList=ref([] as any)
const subjectList=ref([] as any)
const defaultForm:any={
    Date:'',
    TimeRange:[],
    Duration:0,
    KeepOriginClassroom:0,
    ClassroomID:'',
    SubjectID:'',
    Teachers:[],
    Assistants:[]
}
const form=ref(cloneDeep(defaultForm))
const defaultCheckConfig:any={
    IsUpdateMainTeacher:false,//是否更新任课老师
    IsUpdateAssistantTeacher:false,//是否更新助教老师
    IsUpdateClassroomID:false,//是否更新教室
    IsUpdateSubjectID: false,//是否更新上课科目

    IsCheckDate:false,//是否检查上课日期
    IsCheckTime:false,//是否检查上课时间
    IsUpdateOnlineCourse:false,//是否修改为线上课
}
const checkConfig=ref(cloneDeep(defaultCheckConfig))

function handleTimeRangeChange() {
	if (form.value.TimeRange && Array.isArray(form.value.TimeRange)) {
		form.value.Duration = calculateDuration(form.value.TimeRange)
	}
}

function validateTimeRange(rule: any, value: any, callback: any) {
    if (!Array.isArray(value) || value.length < 2) {
        callback(new Error(transToConfigDescript('请选择上课时间')))
        return
    }
    const [start, end] = value
    if (!start || !end) {
        callback(new Error(transToConfigDescript('请选择上课时间')))
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

function handleUpdateOnlineCourseChange(){
    if(!checkConfig.value.IsUpdateOnlineCourse){
        form.value.KeepOriginClassroom=0
    }
}

function handleKeepOriginClassroomChange(){
    if(form.value.KeepOriginClassroom){
        checkConfig.value.IsUpdateClassroomID=false
        form.value.ClassroomID=''
    }
}
function changeCheckConflict() {
	if(checkConflict.value){
		ElMessageBox.confirm(transToConfigDescript("不检查上课冲突，可能会生成相同时间的排课，确认不检查吗？"), '提示', {
			confirmButtonText: '确认不检查',
			cancelButtonText: '取消',
		}).then(()=>{
			checkConflict.value=0
		})
	}else{
		checkConflict.value=1
	}
}
const chooseEmpTableRef=ref()
function selectTableTeacher(row:any){
    let disabledCondition:any=['StatusList']
	if(EnbaleEmpIsClassTeacher.value==1){
        disabledCondition.push('IsClassTeacherList')
    }
	chooseEmpTableRef.value?.open({
		multi: false,
		showTeacherType:true,
        disabledCondition:disabledCondition,
        condition:{
            StatusList:[1],
		    IsClassTeacherList: [1]
        }
	}).then((res:any)=>{
		let d=res.data,other:any=res.other
		handlerMainTeacherData(d, other) ;
	})
}
//选择任课老师的处理
function handlerMainTeacherData(d: any, other: any) {
	// 检查任课老师是否与其他角色重复
	const duplicationCheck = checkTeacherDuplication(d.ID, d.Name, '任课老师');
	if (duplicationCheck.duplicate) {
		return;
	}
	
	// 检查是否已被设置为助教
	const isAlreadyAssistant = form.value.Assistants.some((item: any) => item.ID === d.ID);
	if (isAlreadyAssistant) {
		ElMessageBox.alert('该老师已被设置为助教。', '提示', {
			confirmButtonText: '确认',
		});
		return;
	}
	
	// 检查同一类别是否只对应一个老师
	const pass = checkTeacherType(false, other, form.value.Assistants);
	if (!pass) {
		return;
	}
	
	// 处理任课老师的设置
	if (form.value.Teachers.length > 0 && form.value.Teachers[0].ID === d.ID) {
		// 更新现有任课老师的类别
		const existingTeacher = form.value.Teachers[0];
		const hasType = existingTeacher.TeacherCommissionIDs && 
			other?.ID && 
			existingTeacher.TeacherCommissionIDs.indexOf(other.ID) === -1;
		
		if (hasType) {
			existingTeacher.TeacherCommissionIDs += `,${other.ID}`;
			existingTeacher.TeacherCommissionName += `,${other.Name}`;
		} else {
			existingTeacher.TeacherCommissionIDs = other?.ID || '';
			existingTeacher.TeacherCommissionName = other?.Name || '';
		}
		
		existingTeacher.LabelName = existingTeacher.Name + (other?.ID ? `（${existingTeacher.TeacherCommissionName}）` : '');
	} else {
		// 设置新的任课老师
		const newTeacher = cloneDeep(d);
		newTeacher.TeacherCommissionIDs = other?.ID || '';
		newTeacher.TeacherCommissionName = other?.Name || '';
		newTeacher.LabelName = newTeacher.Name + (other?.Name ? `（${other.Name}）` : '');
		form.value.Teachers = [newTeacher];
	}
}

// 新增：统一的教师重复检查函数
function checkTeacherDuplication(teacherId: string, teacherName: string, excludeRole?: string) {
	const allRoles = [
		{ name: '任课老师', list: form.value.Teachers },
		{ name: '助教', list: form.value.Assistants }
	];
	
	// 检查教师是否已在其他角色中存在
	for (const role of allRoles) {
		if (excludeRole && role.name === excludeRole) {
			continue; // 跳过当前角色
		}
		
		const existingTeacher = role.list.find((teacher: any) => teacher.ID === teacherId);
		if (existingTeacher) {
			ElMessageBox.alert(`${teacherName}已被设置为${transToConfigDescript(role.name||'')},不能继续设为${transToConfigDescript(excludeRole||'')}。`, '提示', {
				confirmButtonText: '知道了',
			});
			return { duplicate: true, role: role.name, teacher: existingTeacher };
		}
	}
	
	return { duplicate: false };
}

function selectTableAssistant() {
	chooseEmpTableRef.value?.open({
		multi: true,
		showTeacherType:true
	}).then((res: any) => {
		const selectedTeachers = res.data;
		const teacherCategory = res.other;
		
		// 检查每个选中的教师是否与其他角色重复
		for (const teacher of selectedTeachers) {
			const duplicationCheck = checkTeacherDuplication(teacher.ID, teacher.Name, '助教');
			if (duplicationCheck.duplicate) {
				return; // 如果重复，直接返回
			}
		}
		
		// 处理每个选中的教师
		for (const teacher of selectedTeachers) {
			// 检查同一类别是否只对应一个老师
			const pass = checkTeacherType(teacher, teacherCategory, form.value.Assistants, {
				ID: form.value.Teachers.length > 0 ? form.value.Teachers[0].ID : '',
				TeacherCommissionIDs: form.value.Teachers.length > 0 ? form.value.Teachers[0].TeacherCommissionIDs : ''
			});
			
			if (!pass) {
				continue; // 如果类别检查不通过，跳过这个教师
			}
			
			// 查找教师是否已存在
			const existingIndex = form.value.Assistants.findIndex((item: any) => item.ID === teacher.ID);
			
			if (existingIndex === -1) {
				// 新增助教
				const newAssistant = cloneDeep(teacher);
				newAssistant.TeacherCommissionIDs = teacherCategory ? teacherCategory.ID : '';
				newAssistant.TeacherCommissionName = teacherCategory ? teacherCategory.Name : '';
				newAssistant.LabelName = teacher.Name + (teacherCategory&&teacherCategory.Name ? `（${teacherCategory.Name}）` : '');
				form.value.Assistants.push(newAssistant);
			} else {
				// 更新现有助教的类别
				const existingAssistant = form.value.Assistants[existingIndex];
				const hasType = existingAssistant.TeacherCommissionIDs && 
					teacherCategory?.ID && 
					existingAssistant.TeacherCommissionIDs.indexOf(teacherCategory.ID) === -1;
				
				if (hasType) {
					existingAssistant.TeacherCommissionIDs += `,${teacherCategory.ID}`;
					existingAssistant.TeacherCommissionName += `,${teacherCategory.Name}`;
				} else {
					existingAssistant.TeacherCommissionIDs = teacherCategory?.ID || '';
					existingAssistant.TeacherCommissionName = teacherCategory?.Name || '';
				}
				
				existingAssistant.LabelName = teacher.Name + 
					(existingAssistant.TeacherCommissionName ? `（${existingAssistant.TeacherCommissionName}）` : '');
			}
		}
	});
}

const workFlowPopupRef=ref<InstanceType<typeof workFlowPopup> | null>(null)
const formRef=ref<FormInstance>()
const conflictPromptRef=ref<InstanceType<typeof ConflictPrompt> | null>(null)
function submit(){
    formRef.value!.validate((valid) => {
        if (valid) {
            let params:any={
                IsUpdateMainTeacher:checkConfig.value.IsUpdateMainTeacher,
                IsUpdateAssistantTeacher:checkConfig.value.IsUpdateAssistantTeacher,
                IsUpdateSubjectID:checkConfig.value.IsUpdateSubjectID,
                IsUpdateOnlineCourse:checkConfig.value.IsUpdateOnlineCourse,
                SubjectID:checkConfig.value.IsUpdateSubjectID?form.value.SubjectID:EMPTYGUID,
                CheckConflict:checkConflict.value,
            }
            let teachers:any=[]
            if(checkConfig.value.IsUpdateMainTeacher){
                form.value.Teachers.forEach((item:any)=>{
                    teachers.push({
                        TeacherID:item.ID,
                        TeacherName:item.Name,
                        TeacherRole:1,
                        TeacherCommissionIDs:item.TeacherCommissionIDs,
                        TeacherCommissionName:item.TeacherCommissionName,
                    })
                })
            }
            if(checkConfig.value.IsUpdateAssistantTeacher){
                form.value.Assistants.forEach((item:any)=>{
                    teachers.push({
                        TeacherID:item.ID,
                        TeacherName:item.Name,
                        TeacherRole:2,
                        TeacherCommissionIDs:item.TeacherCommissionIDs,
                        TeacherCommissionName:item.TeacherCommissionName
                    })
                })
            }
            params.TeacherList=teachers
            params.IsUpdateClassroomID=checkConfig.value.IsUpdateClassroomID||checkConfig.value.IsUpdateOnlineCourse
            let courseList:any=[]
            editList.value.forEach((item:any)=>{
                let classroomID='',date="",startTime='',endTime=''
                if(checkConfig.value.IsCheckDate){
                    date=form.value.Date
                }else{
                    date=dayjs(item.StartTime).format('YYYY-MM-DD')
                }
                if(checkConfig.value.IsCheckTime){
                    startTime=date+' '+form.value.TimeRange[0]+':00'
                    endTime=date+' '+form.value.TimeRange[1]+':59'
                }else{
                    startTime=date+' '+dayjs(item.StartTime).format('HH:mm:ss')
                    endTime=date+' '+dayjs(item.EndTime).format('HH:mm:ss')
                }
                if(checkConfig.value.IsUpdateClassroomID){
                    classroomID=form.value.ClassroomID
                }else if(checkConfig.value.IsUpdateOnlineCourse&&form.value.KeepOriginClassroom==1){
                    classroomID=item.ClassroomID||EMPTYGUID
                }
                courseList.push({
                    CourseID:item.ID,
                    ClassroomID:classroomID||EMPTYGUID,
                    StartTime:startTime,
                    EndTime:endTime
                })
            })
            params.CourseList=courseList
            params.IsUpdateDate=checkConfig.value.IsCheckDate||checkConfig.value.IsCheckTime
            loading.value=true
            putCourseList(params).then((res:any)=>{
                if(res.Data&&res.Data.IsMatchWorkflow==1){
                    // workFlowPopupRef.value?.open({
                    //     popTitle:'批量修改排课',
                    //     api:putCourseAsk,
                    //     data:{
                    //         data:operateArr.map((item:any)=>item.ID).join(','),
                    //         WorkflowId:res.Data.WorkflowId
                    //     }
                    // }).then((res:any)=>{
                    //     _resolve()
                    //     close()
                    // })
                }else{
                    ElMessage.success('已修改')
                    _resolve()
                    close()
                }
                
            }).catch((error)=>{
                if(error.ErrorCode==409){
                    conflictPromptRef.value?.open({info:error.Data}).then(()=>{}).catch((back:any)=>{
                        if(back&&back.closeCurrent){
                            drawer.value=false
                        }
                    })
                }
            }).finally(()=>{
                loading.value=false
            })
        }
    })
}
function getAdvanceConfig(){
	querySysConfig({
		campusID: '',
		type: 0,
		configNames: 'CourseRuleSetting' //配置项名称（多个时用逗号分隔）
	}).then((res:any) => {
		var data = res.Data;
		data.forEach((item:any) => {
			if (item.Name == 'CourseRuleSetting') {
				let obj=JSON.parse(item.OtherParameter)
				MinCourseTime.value=obj.MinCourseTime
				MaxCourseTime.value=obj.MaxCourseTime
			}
		})
	})
}
function getClassroomList() {
	queryClassroom({ campusIds:campusList.value[0] }).then(
		(res: any) => {
			classroomList.value = res.Data || []
		}
	)
}
function getSubjectList(classId:string){
    getClassSubject({ classId:classId }).then(
        (res: any) => {
            subjectList.value = res.Data?res.Data.map((item:any)=>
            {return {ID:item.SubjectID,Name:item.SubjectName}}) : []
        }
    )
}
function close(){
    drawer.value=false
    nextTick(()=>{
        form.value=cloneDeep(defaultForm)
        checkConfig.value=cloneDeep(defaultCheckConfig)
        checkConflict.value=1
        allList.value=[]
        editList.value=[]
        campusList.value=[]
        classroomList.value=[]
        subjectList.value=[]
    })
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    drawer.value = true;
    allList.value=params.selected||[]
    editList.value=allList.value.filter((item:any)=>item.Finished==0)
    // 根据 editList 统计去重后的 CampusID 列表（保留首次出现，去除重复）
    const seenCampusIds = new Set<any>()
    campusList.value = (editList.value || [])
        .map((item:any)=>item.CampusID)
        .filter((id:any)=>{
            if(seenCampusIds.has(id)) return false
            seenCampusIds.add(id)
            return true
        })
    getAdvanceConfig()
    if(campusList.value.length==1){
        getClassroomList()
    }
    if((CourseIsClassSubject.value && editList.value.length>0 && editList.value.every((item:any)=>item.ClassID===editList.value[0].ClassID)) && editList.value.every((item:any)=>item.EnableSubject==1)){
        getSubjectList(editList.value[0].ClassID)
    }else if((!CourseIsClassSubject.value&&editList.value.every((item:any)=>item.EnableSubject==1))){
        subjectList.value=SUBJECT.value
    }
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
}
defineExpose({
    open
})
</script>
<style scoped lang="scss">
.batchEditCourse{
    .batch-item-wrap{
        margin-bottom: 10px;
    }
    .batch-item-content{
        margin-left: 22px;
    }
    .drawer-content-wrap{
        padding:10px 16px;
    }
}
</style>