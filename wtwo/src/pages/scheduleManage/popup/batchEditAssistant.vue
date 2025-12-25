<template>
    <!-- 批量修改列 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="batchEditAssistant" 
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
                <el-form-item label="助教" prop="val">
                    <input-tag
                        placeholder="请选择"
                        :selected="form.val"
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
import { checkTeacherType } from '@/utils/scheduleUtils';
import { FormInstance } from 'element-plus';
import { cloneDeep } from 'lodash';
import { ref } from 'vue';

const dialogVisible = ref(false)
const loading=ref(false)
const form=ref({
    val:[] as any,
    other:''
})
const chooseEmpTableRef = ref()
function selectTableAssistant(){
	chooseEmpTableRef.value?.open({
		multi: true,
        showTeacherType:true,
        disabledCondition:['StatusList']
	}).then((res:any)=>{
		const selectedTeachers = res.data;
		const teacherCategory = res.other;
		// 处理每个选中的教师
		for (const teacher of selectedTeachers) {
			// 检查同一类别是否只对应一个老师
			const pass = checkTeacherType(teacher, teacherCategory, form.value.val, {
				ID: '',
				TeacherCommissionIDs: ''
			});
			
			if (!pass) {
				continue; // 如果类别检查不通过，跳过这个教师
			}
			
			// 查找教师是否已存在
			const existingIndex = form.value.val.findIndex((item: any) => item.ID === teacher.ID);
			
			if (existingIndex === -1) {
				// 新增助教
				const newAssistant = cloneDeep(teacher);
				newAssistant.TeacherCommissionIDs = teacherCategory ? teacherCategory.ID : '';
				newAssistant.TeacherCommissionName = teacherCategory ? teacherCategory.Name : '';
				newAssistant.LabelName = teacher.Name + (teacherCategory&&teacherCategory.Name ? `（${teacherCategory.Name}）` : '');
				form.value.val.push(newAssistant);
			} else {
				// 更新现有助教的类别
				const existingAssistant = form.value.val[existingIndex];
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
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
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