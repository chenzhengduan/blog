<template>
    <!-- 操作审批流弹窗 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="workFlowPopup" 
        width="500px" 
        top="5%" 
        :title="popTitle"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <div class="mb-[16px]!">该操作需要通过审批之后才能完成，是否继续提交申请？</div>
            <el-form :model="form" label-position="top" ref="formRef" :validate-on-rule-change="false">
                <el-form-item label="" prop="val">
                    <el-input
                        v-model="form.val"
                        maxlength="200"
                        placeholder="请输入申请原因"
                        show-word-limit
                        type="textarea"
                    />
                </el-form-item>
            </el-form>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button type="primary" @click="submit" :loading="loading">确定</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { FormInstance } from 'element-plus';
import { ref } from 'vue';

const dialogVisible = ref(false)
const loading=ref(false)
const popTitle=ref('提示')
const form=ref({
    val:''
})
const api=ref()
let gparams:any
const formRef = ref<FormInstance>()
function submit(){
    loading.value=true
    api.value({
        data: gparams.data,
        remark: form.value.val.trim(),
        workflowId: gparams.WorkflowId,
    }).then((res:any)=>{
        ElMessage.success('已提交申请')
        _resolve()
        dialogVisible.value = false;
    }).catch((res:any)=>{
        ElMessage.error(res.Message)
    }).finally(()=>{
        loading.value=false
    })
}
function close() {
    dialogVisible.value = false;
    form.value.val=''
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    popTitle.value=params.popTitle||'提示'
    api.value=params.api
    gparams=params.data
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