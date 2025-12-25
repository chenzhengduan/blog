<template>
    <!-- 批量修改列 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="batchEditClassroom" 
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
                <el-form-item :label="transToConfigDescript('上课教室')" prop="val">
                    <el-select
                        v-model="form.val"
                        placeholder="请选择"
                        clearable
                        filterable
                        @change="setClassroomName"
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
            </el-form>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button type="primary" @click="submit">确定</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { queryClassroom } from '@/api/arrange';
import { setConfig } from '@/services/oss';
import { transToConfigDescript } from '@/utils/filters/filters';
import { FormInstance } from 'element-plus';
import { ref } from 'vue';

const dialogVisible = ref(false)
const loading=ref(false)
const form=ref({
    val:''
})
const classroomName=ref('')
const classroomList=ref([] as any)	
const campusId=ref('')
function getClassroomList() {
    loading.value=true
	queryClassroom({ campusIds: campusId.value }).then(
		(res: any) => {
			classroomList.value = res.Data || []
		}
	).finally(()=>{
		loading.value=false
	})
}
function setClassroomName(val:string){
    let name=classroomList.value.find((i:any)=>i.ID==val)?.Name
    classroomName.value=name||''
}
const formRef = ref<FormInstance>()
function submit(){
    formRef.value!.validate((valid) => {
        if (valid) {
            _resolve({data:form.value.val?{ID:form.value.val,Name:classroomName.value}:null})
            dialogVisible.value = false;
        }
    })
}
function close() {
    dialogVisible.value = false;
    form.value.val=''
    classroomName.value=''
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    campusId.value=params.campusId
    getClassroomList()
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