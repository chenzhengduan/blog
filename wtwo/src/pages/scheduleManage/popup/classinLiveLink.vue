<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="classinLiveLink" 
        width="550px" 
        top="5%" 
        title="查看直播链接"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <div class="qr-code-box">
                <img :src="preUrl" class="ewCode" />
			    <p>微信扫码后直接打开微信小程序观看直播，其他APP扫码打开H5网页观看直播</p>
                <div class="flex-center">
                    <el-input v-model="link" class="mt-10px w-[478px]!" placeholder="直播链接" readonly>
                        <template #append>
                            <el-link type="primary" underline="never" @click="copyLink">复制链接</el-link>
                        </template>
                    </el-input>
                </div>
                <p style="text-align: left;width: 100%;margin-left: 34px;">复制上方链接，可通过微信、QQ发送给家长</p>
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
const link=ref('')

const className=ref('')
function download(){
    downloadFile(preUrl.value, className.value+'-直播二维码.png')
}

function copyLink(){
    navigator.clipboard.writeText(link.value)
    ElMessage.success('复制成功')
}

function close(){
    dialogVisible.value=false
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    className.value=params.name||''
    link.value=params.link||''
    preUrl.value='/api/SSX/GetQRCode?url='+params.link
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
        margin-top: 10px;
    }
}
</style>
