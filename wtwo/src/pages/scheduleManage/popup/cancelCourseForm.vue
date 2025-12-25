<template>
    <!-- 取消排课弹窗 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="cancelCourseForm" 
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
			<div class="warning-message">
				<span class="warning-text">{{isEdit?'排课已取消，无法进入详情':'警告！已取消的排课将无法恢复，请谨慎操作。'}}</span>
			</div>
            <div class="mb-[16px]!" v-if="isBatch">
                <div>本次操作取消共<span class="color-[#F56C6C]">{{ list.length }}</span>节排课</div>
            </div>
            <el-form :model="form" label-position="top" ref="formRef" :validate-on-rule-change="false">
                <template v-if="!isBatch">
                    <div class="flex">
                        <el-form-item :label="transToConfigDescript('上课课程')" class="w-[47%] mr-[40px]">
                            <el-input :model-value="courseInfo.ShiftName" disabled></el-input>
                        </el-form-item>
                        <el-form-item :label="transToConfigDescript('上课班级/学员')" class="w-[47%]">
                            <el-input :model-value="courseInfo.ClassName" disabled></el-input>
                        </el-form-item>
                    </div>
                    <div class="flex">
                        <el-form-item :label="transToConfigDescript('上课时间')" class="w-[47%] mr-[40px]">
                            <el-input :model-value="dayjs(courseInfo.StartTime).format('YYYY-MM-DD HH:mm')+'-'+dayjs(courseInfo.EndTime).format('HH:mm')+'['+dayjs(courseInfo.StartTime).format('ddd')+']'" disabled></el-input>
                        </el-form-item>
                        <el-form-item :label="transToConfigDescript('上课状态')" class="w-[47%]">
                            <el-input :model-value="transToConfigDescript(courseInfo.Finished==0?'未上课':courseInfo.Finished==1?'已上课':'已取消')" disabled></el-input>
                        </el-form-item>
                    </div>
                </template>
                <el-form-item label="取消原因" prop="describe" :rules="[{ required: true, message: '请输入取消原因' }]">
                    <el-input
                        v-model="form.describe"
                        maxlength="200"
                        placeholder="请输入取消原因"
                        show-word-limit
                        type="textarea"
                    />
                </el-form-item>
                <el-form-item label="" prop="ssxSend">
                    <el-checkbox v-model="form.weixin" :true-label="1" :false-label="0">以微信/短信形式给家长发送取消{{transToConfigDescript('上课')}}信息（推送微信需要家长关注并登录"师生信"公众号）</el-checkbox>
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
import { dayjs } from 'element-plus'
import { transToConfigDescript } from '@/utils/filters/filters';
import { cancelCourse, cancelCoursePutContent } from '@/api/arrange';

const dialogVisible = ref(false)
const loading=ref(false)
const popTitle=ref('提示')
const isBatch=ref(false)//是否为批量取消
const isEdit=ref(false)//是否为编辑
const list=ref([] as any)
const courseInfo=ref({} as any)
const form=ref({
    describe:'',
    weixin:1
})
let gparams:any
const formRef = ref<FormInstance>()
function submit(){
    let params:any={}
    Object.assign(params,form.value)
    formRef.value?.validate((valid:boolean)=>{
        if(valid){
            loading.value=true
            let api=isEdit.value?cancelCoursePutContent:cancelCourse
            if(isEdit.value){
                params.CourseID=gparams.courseIDList[0]
            }else{
                Object.assign(params,gparams)
            }
            api(params).then((res:any)=>{
                ElMessage.success(isEdit.value?'已修改':'已取消排课')
                _resolve()
                dialogVisible.value = false;
            }).catch((res:any)=>{
                ElMessage.error(res.Message)
            }).finally(()=>{
                loading.value=false
            })
        }
    })  
}
function close() {
    dialogVisible.value = false;
    form.value.describe=''
    form.value.weixin=1
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    popTitle.value=params.popTitle||'提示'
    gparams=params.gparams||{}
    isBatch.value=params.isBatch||false
    isEdit.value=params.isEdit||false
    list.value=params.isBatch?(params.data||[]):[]
    courseInfo.value=!params.isBatch?params.data:{}
    form.value.describe=courseInfo.value.Describe||''
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