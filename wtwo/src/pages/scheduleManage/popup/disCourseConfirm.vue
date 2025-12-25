<template>
    <!-- 撤销上课弹窗 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="disCourseConfirm" 
        width="620px" 
        top="5%" 
        :title="popTitle"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <!-- 提示信息 -->
			<div class="mb-[16px]! line-height-[22px]">{{tips}}</div>
            <el-checkbox v-if="isShowSsxSent" v-model="form.weixin" :true-label="1" :false-label="0">以微信/短信形式给家长发送取消{{transToConfigDescript('上课')}}信息（推送微信需要家长关注并登录"师生信"公众号）</el-checkbox>
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
import { dayjs } from 'element-plus'
import { querySysConfig } from '@/api';
import { setAttendance } from '@/api/arrange';
import { transToConfigDescript } from '@/utils/filters/filters';

const dialogVisible = ref(false)
const loading=ref(false)
const popTitle=ref('提示')
const tips=ref('')
const info=ref({} as any)
const form=ref({
    weixin:1
})
const isShowSsxSent=ref(false)
function getAdvanceConfig(){
	querySysConfig({
		campusID: '',
		type: 1,
		configNames: 'MSG_COURSE_RESET' //配置项名称（多个时用逗号分隔）
	}).then((res:any) => {
		var data = res.Data;
		data.forEach((item:any) => {
			if (item.Name == "MSG_COURSE_RESET") {
				isShowSsxSent.value = !item.Status ? false : true ;
                form.value.weixin = isShowSsxSent.value ? 1 : 0 ;
			}
		})
	})
}
function submit(){
    loading.value=true
    let params={
        Students: [],
		Action: -1,
        IsSendResetMsg:form.value.weixin,
        CourseID:info.value.ID,
        lastClassTime:dayjs(info.value.StartTime).format('YYYY-MM-DD HH:mm')+'-'+dayjs(info.value.EndTime).format('HH:mm')+'['+dayjs(info.value.StartTime).format('dddd')+']'
    }
    setAttendance({data: JSON.stringify(params)}).then((res:any)=>{
        ElMessage.success(transToConfigDescript('已撤销上课'))
        _resolve()
        dialogVisible.value = false;
    }).finally(()=>{
        loading.value=false
    })
}
function close() {
    dialogVisible.value = false;
    form.value.weixin=1
    tips.value=''
    info.value={}
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    popTitle.value=params.popTitle||'提示'
    tips.value=params.tips||''
    info.value=params.data||{}
    getAdvanceConfig()
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
.cancelCourseForm{
    .warning-message {
		display: flex;
		align-items: center;
		margin-bottom: 16px;
		padding: 12px;
		background-color: #fff7e6;
		border: 1px solid #ffd591;
		border-radius: 4px;

		.warning-tag {
			margin-right: 8px;
			background-color: #faad14;
			border-color: #faad14;
			color: #fff;
		}

		.warning-text {
			color: #d46b08;
			font-size: 14px;
		}
	}
}
</style>