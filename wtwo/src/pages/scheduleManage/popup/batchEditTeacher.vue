<template>
    <!-- 批量修改列 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="batchEditTeacher" 
        width="500px" 
        top="5%" 
        title="批量设置"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <pageAttentionTips class="mb-10px">
                注意！该列将会被修改为相同的值
            </pageAttentionTips>
            <el-form :model="form" label-position="top" ref="formRef" :validate-on-rule-change="false">
                <el-form-item :label="transToConfigDescript('任课老师')" prop="val">
                    <input-tag
                        placeholder="请选择"
                        :selected="form.val"
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
            </el-form>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button type="primary" @click="submit">确定</el-button>
            </div>
        </template>
        <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
    </el-dialog>
</template>
<script lang="ts" setup>
import { FormInstance } from 'element-plus';
import { cloneDeep } from 'lodash';
import { ref, computed } from 'vue';
import { useConfigs } from '@/store';
import { transToConfigDescript } from '@/utils/filters/filters';

const configs = computed(() => {
	return useConfigs().configs
})

const EnbaleEmpIsClassTeacher=computed(()=>{
    return configs.value.EnbaleEmpIsClassTeacher
})

const EnableMustSameSubjectTeacherCourse=computed(()=>{ //是否开启限制跨科目选择老师 0允许（默认） 1不允许
	return configs.value.EnableMustSameSubjectTeacherCourse==1
})

const dialogVisible = ref(false)
const loading=ref(false)
const form=ref({
    val:[] as any,
    other:''
})
const subjectId=ref('')
const chooseEmpTableRef = ref()
const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
function selectTableTeacher(){
    let teacherCondition:any={
		StatusList:[1],
		IsClassTeacherList: [1]
	}
	let subject:any=subjectId.value;
	let disabledCondition:any=['StatusList']
    if(EnableMustSameSubjectTeacherCourse.value){
        disabledCondition.push('SubjectIDList')
    }
	if(EnbaleEmpIsClassTeacher.value==1){
        disabledCondition.push('IsClassTeacherList')
    }
	if(subject&&subject!=EMPTYGUID){
		teacherCondition.SubjectIDList=[subject]
	}
	chooseEmpTableRef.value?.open({
		multi: false,
		disabledCondition:disabledCondition,
		condition:teacherCondition,
		showTeacherType:true
	}).then((res:any)=>{
        let d=res.data,other:any=res.other;
        // 处理任课老师的设置
        if (form.value.val.length > 0 && form.value.val[0].ID === d.ID) {
            // 更新现有任课老师的类别
            const existingTeacher = form.value.val[0];
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
            form.value.val = [newTeacher];
        }
	})
	
}
const formRef = ref<FormInstance>()
function submit(){
    formRef.value!.validate((valid) => {
        if (valid) {
            _resolve({data:form.value.val,other:form.value.other})
            dialogVisible.value = false;
        }
    })
}
function close() {
    dialogVisible.value = false;
    form.value.val=[]
    form.value.other=''
    subjectId.value=''
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    subjectId.value=params.subjectId||''
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
}
defineExpose({
    open
})
</script>
<style lang="scss" scoped>
</style>