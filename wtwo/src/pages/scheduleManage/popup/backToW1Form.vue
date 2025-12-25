<template>
    <el-dialog
        v-model="isShow"
        width="453"
        modal-class="backToW1Form"
        destroy-on-close
        :close-on-click-modal="false"
        @close="close"
    >
        <div class="text-16px color-[#303133] font-semibold">很抱歉让您的体验不好<img 
            class="ml-4px" src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/small-people.png" width="16px" />
        </div>
        <div class="color-[#303133] mt-16px">是什么原因让您回到旧版【排课管理】呢？</div>
        <el-checkbox-group v-model="checked">
            <el-checkbox v-for="item in reason" :label="item"></el-checkbox>
        </el-checkbox-group>
        <div class="txt-area">
            <el-form :model="form" ref="formRef">
                <el-form-item prop="remark" :rules="[{required:true,message:'请输入内容'}]">
                    <div class="w-[100%]">
                        <el-input 
                            v-model="form.remark"
                            type="textarea" 
                            resize="none" 
                            maxlength="500"
                            placeholder="每条反馈我们都会查阅并积极改进，请尽可能详细描述~" 
                        />
                        <img class="guanzai" src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/guanzai-kuqi.png" width="82px" height="73px" />
                    </div>
                </el-form-item>
            </el-form>
            
        </div>
        <el-checkbox v-model="interviews" label="我愿意接受回访，帮助系统做的更好" size="large" />
        <el-input
            v-if="interviews"
            v-model="contact"
            style="margin-bottom: 10px;"
            :autosize="{ minRows: 2, maxRows: 4 }"
            type="textarea"
            placeholder="请留下联系方式，方便我们联系您"
        />
        <div class="flex justify-end">
            <el-button plain @click="close">留在新版</el-button>
            <el-button type="primary" @click="backTo">回到旧版</el-button>
        </div>
    </el-dialog>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
// import { BackVesionFeedback } from '@main/api/comm';
import { FormInstance } from 'element-plus';
import { BackVesionFeedback } from '@/api';

const isShow = ref(false);

const checked = ref([]);
const interviews = ref<boolean>(true)
const contact = ref<string>('')
const form= ref({
    remark: ''
}); 
const reason = ['不会用', '找不到想要的功能', '习惯旧版'];
const formRef = ref<FormInstance>()
function backTo() {
    formRef.value!.validate((valid)=>{
        if(valid){
            BackVesionFeedback({
                checkboxValue: checked.value.join(','),
                feedbackContent:form.value.remark,
                enableInterviews: interviews.value ? 1:0,
                contact: interviews.value?contact.value:'',
                Module:1,
            }).then(() => {
                _resolve();
            }).finally(() => {
                isShow.value = false;
            })
        }
    })
    
}
function close() {
    isShow.value = false;
    checked.value = [];
    interviews.value = true;
    contact.value = ''
    form.value.remark = '';
}

let _resolve = null as any, 
    _reject = null as any;
function open() {
    isShow.value = true;

    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
}
defineExpose({
    open
})
</script>

<style lang="scss">
.backToW1Form {
    .wtwo-dialog {
        padding: 20px 0 24px;
        background-image: url('https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/gradient-bg.png');
        background-repeat: no-repeat;
        background-size: contain;
        .wtwo-dialog__header {
            padding: 0 20px;
            background-color: #fff;
            .wtwo-dialog__headerbtn {
                .wtwo-dialog__close {
                    color: #4E5969;
                }
            }
        }
        .wtwo-checkbox-group {
            display: flex;
            flex-direction: column;
            margin-top: 20px;
        }
        .txt-area {
            position: relative;
            margin-top: 20px;
            margin-bottom: 12px;
            .guanzai {
                position: absolute;
                right: 0;
                top: -62px;
            }
        }
        .wtwo-textarea__inner {
            height: 160px;
            padding: 10px 12px;
            background-color: #F9FAFC;
            border-color: #E4E7ED;
            border-radius: 10px;
        }
    }
}
</style>