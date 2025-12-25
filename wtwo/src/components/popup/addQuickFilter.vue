<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="addQuickFilter" 
        width="408px" 
        top="5%" 
        title="另存为快捷筛选"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <el-form :model="form" label-width="52px" ref="formRef" :validate-on-rule-change="false">
                <el-form-item label="名称" prop="Menu" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
                    <el-input v-model="form.Menu" type="text" maxlength="10" placeholder="请输入"></el-input>
                </el-form-item>
            </el-form>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button v-loading="loading" type="primary" @click="submit">保存</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { postUserLabel } from '@/api';
import { ElForm, ElMessage } from 'element-plus';
import { ref, computed, reactive } from 'vue';

const dialogVisible = ref(false)

const loading = ref(false);
const form = reactive({
    Menu: '',
})
let info={}

const formRef = ref<InstanceType<typeof ElForm>>()
function submit() {
    formRef.value!.validate((valid) => {
        if (valid) {
            let params: any = {}

            Object.assign(params,info, form);
            loading.value = true;
            postUserLabel(params).then(res => {
                ElMessage({
                    message:'快捷筛选已新增',
                    type: 'success'
                })
                loading.value = false;
                dialogVisible.value = false;
                resetForm();
                _resolve();
            }).finally(() => {
                loading.value = false;
            })
        }
    })
}
function resetForm() {
    form.Menu = '';
}
function close() {
    dialogVisible.value = false;
    resetForm();
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    info=params
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
}
defineExpose({
    open
})

</script>
<style lang="scss" scoped></style>