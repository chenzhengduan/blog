<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="qrCodePop" 
        width="400px" 
        top="5%" 
        :title="`${campusName}-约课二维码`"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <div class="qr-code-box">
                <img :src="preUrl" class="ewCode" />
			    <p>微信扫描二维码可进入“师生信-约课”页面进行预约排课</p>
            </div>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">关闭</el-button>
                <el-button @click="download">下载二维码</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { useUserCampuses } from '@/store';
import { downloadFile } from '@/utils';
import { computed, ref } from 'vue';

const dialogVisible=ref(false)
const loading=ref(false)
const preUrl=ref('')

const campusList=computed(()=>{
    return useUserCampuses().userCampuses
})
let campusId=""
const campusName=ref('')
function download(){
    downloadFile(preUrl.value, campusName.value+'-约课二维码.png')
}

function close(){
    dialogVisible.value=false
    campusName.value=''
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    campusId=params.campusId||''
    preUrl.value='/api/SubscribeCoursePlan/GetSubscribeQRCode?campusId='+params.campusId
    campusList.value.forEach((item:any)=>{
        if(item.ID==campusId){
            campusName.value=item.Name
        }
    })
    
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
        margin-top: 10px;
    }
    > p {
        text-align: center;
        color: #bdbdbd ;
        margin-top: 20px;
    }
}
</style>
