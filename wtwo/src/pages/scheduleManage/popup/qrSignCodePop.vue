<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="qrCodePop" 
        width="400px" 
        top="5%" 
        :title="`签到二维码 - ${className}`"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <div class="qr-code-box">
			    <p>{{transToConfigDescript('学员用“师生信-扫码签到”功能扫一扫即可上课签到')}}</p>
                <img :src="preUrl" class="ewCode" />
                <div class="tip">学员当前不在排课中，扫码签到后系统自动将学员临加到这节排课。</div>
            </div>
        </div>
    </el-dialog>
</template>
<script lang="ts" setup>
import { transToConfigDescript } from '@/utils/filters/filters';
import { ref } from 'vue';

const dialogVisible=ref(false)
const loading=ref(false)
const preUrl=ref('')

let className="";

function close(){
    dialogVisible.value=false
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    className=params.ClassName||''
    preUrl.value=`/api/Course/GetCourseBySignQRCode?courseId=${params.ID}&_timer=${Math.random()}`
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
.qr-code-box{
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    >img{
        width: 200px;
		height: 200px;
        margin-top: 20px;
    }
    > p {
        text-align: center;
        color: #303133;
    }
    .tip {
        color: #909399;
        font-size: 12px;
        margin: 12px 0;
    }
}
</style>
