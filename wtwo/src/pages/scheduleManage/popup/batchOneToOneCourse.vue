<template>
    <!-- 取消排课弹窗 -->
    <el-dialog 
        v-model="dialogVisible" 
        class="batchOneToOneCourse" 
        width="740px" 
        top="5%" 
        :title="popTitle"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <template v-if="!isShowFinished && !isSuccess">
                <div class="mb-[10px] font-bold">确定要进行批量点名{{transToConfigDescript('上课')}}吗？</div>
                <div class="mb-[10px] color-[#999] text-[13px]">操作批量点名后，已选排课将成为{{transToConfigDescript('已上课')}}状态。排课中的学员将全部标记为已出勤已计费状态。</div>
                <el-checkbox v-model="form.weixin" :true-value="1" :false-value="0">以微信/短信形式给家长发送{{transToConfigDescript('取消上课')}}信息（推送微信需要家长关注并登录"师生信"公众号）</el-checkbox>
            </template>
            
            <div v-if="isShowFinished && !isSuccess&&(finishedList.length||cancelList.length||emptyTeacherList.length)">
                <div class="form-group form-title" style="width: 100%;">
                    共{{sum([list.length,finishedList.length,cancelList.length,emptyTeacherList.length])}}节排课，以下<span class="color-[#F56C6C]">{{sum([finishedList.length,cancelList.length,emptyTeacherList.length])}}</span>节“{{transToConfigDescript('已上课')}}、已取消、无{{transToConfigDescript('任课老师')}}”不能点名。
                </div>
                <div class="wtwo-check-tips-box mt-[10px]">
                    <div class="wtwo-check-tips-item" v-for="item in finishedList">
                        <div class="w-[160px] ellipsis-single" :title="item.ShiftName">{{item.ShiftName}}</div>
                        <div class="w-[160px] ellipsis-single" :title="item.ClassName">{{item.ClassName}}</div>
                        <div class="w-[220px]" v-if="item.Unit !=3 || EnableMonthShiftCourse||IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )}} - {{dayjs(item.EndTime).format("HH:mm" )}}[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[220px]" v-if="item.Unit == 3 && !EnableMonthShiftCourse&&!IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD" )}}&nbsp;[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[100px]">{{transToConfigDescript('已上课')}}</div>
                    </div>
                    <div class="wtwo-check-tips-item" v-for="item in cancelList">
                        <div class="w-[160px] ellipsis-single" :title="item.ShiftName">{{item.ShiftName}}</div>
                        <div class="w-[160px] ellipsis-single" :title="item.ClassName">{{item.ClassName}}</div>
                        <div class="w-[220px]" v-if="item.Unit !=3 || EnableMonthShiftCourse||IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )}} - {{dayjs(item.EndTime).format("HH:mm" )}}[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[220px]" v-if="item.Unit == 3 && !EnableMonthShiftCourse&&!IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD" )}}&nbsp;[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[100px]">已取消</div>
                    </div>
                    <div class="wtwo-check-tips-item" v-for="item in emptyTeacherList">
                        <div class="w-[160px] ellipsis-single" :title="item.ShiftName">{{item.ShiftName}}</div>
                        <div class="w-[160px] ellipsis-single" :title="item.ClassName">{{item.ClassName}}</div>
                        <div class="w-[220px]" v-if="item.Unit !=3 || EnableMonthShiftCourse||IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )}} - {{dayjs(item.EndTime).format("HH:mm" )}}[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[220px]" v-if="item.Unit == 3 && !EnableMonthShiftCourse&&!IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD" )}}&nbsp;[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[100px]">无{{transToConfigDescript('任课老师')}}</div>
                    </div>
                </div>
            </div>
            <div v-if="isSuccess">
                共{{list.length}}排课
                <span v-if="successList.length">，有<span class="color-[#67C23A]">{{successList.length}}</span>节点名成功</span>
                <span v-if="errorList.length">，<span class="color-[#F56C6C]">{{errorList.length}}</span>节点名失败</span>
                <div class="wtwo-check-tips-box mt-[10px]" v-if="exceptionList.length||approvalList.length||errorList.length">
                    <div class="wtwo-check-tips-item" v-for="item in exceptionList">
                        <div class="w-[160px] ellipsis-single" :title="item.ClassName">{{item.ClassName}}</div>
                        <div class="w-[120px] ellipsis-single" :title="item.StudentName">{{ item.StudentName }}</div>
                        <div v-if="EnableCourseConfirmWhenLess!=1" class="w-[180px]">学员欠费</div>
                    </div>
                    <div class="wtwo-check-tips-item" v-for="item in approvalList">
                        <div class="w-[160px] ellipsis-single" :title="item.ClassName">{{item.ClassName}}</div>
                        <div class="w-[160px] ellipsis-single" :title="item.StudentName">{{item.StudentName}}</div>
                        <div class="w-[180px]">学员单据审批中</div>
                    </div>
                    <div class="wtwo-check-tips-item" v-for="item in errorList">
                        <div class="w-[160px] ellipsis-single" :title="item.ClassName">{{item.ClassName}}</div>
                        <div class="w-[220px]" v-if="item.Unit !=3 || EnableMonthShiftCourse||IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )}} - {{dayjs(item.EndTime).format("HH:mm" )}}[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[220px]" v-if="item.Unit == 3 && !EnableMonthShiftCourse&&!IsOpenShiftForDay">{{dayjs(item.StartTime).format("YYYY-MM-DD" )}}&nbsp;[{{dayjs(item.StartTime).format('dddd') }}]</div>
                        <div class="w-[180px]">失败原因：{{item.ErrorMsg||'未知错误'}}</div>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <div class="flex-between">
                <div>
                    <el-checkbox v-if="isSuccess&&EnablePrintOneToOneCourseRecord&&NewCourse_PrintCourseRecord" v-model="printCourseCard" :true-value="1" :false-value="0">{{transToConfigDescript('打印上课凭证')}}</el-checkbox>
                </div>
                <div class="flex-center" v-if="isShowFinished && !isSuccess">
                    <el-button plain @click="submit" v-if="sum([list.length,finishedList.length,cancelList.length,emptyTeacherList.length])>sum([finishedList.length,cancelList.length,emptyTeacherList.length])">继续点名剩下的排课</el-button>
                    <el-button type="primary" @click="close" :loading="loading">返回</el-button>
                </div>
                <div class="flex-center" v-else>
                    <el-button plain @click="close">取消</el-button>
                    <el-button type="primary" @click="submit" :loading="loading">确定</el-button>
                </div>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { useConfigs } from '@/store';
import { computed, ref } from 'vue';
import { dayjs } from 'element-plus'
import { setAttendanceOneToOneBatch } from '@/api/arrange';
import { nextTick } from 'vue';
import { sum } from 'lodash';
import { transToConfigDescript } from '@/utils/filters/filters';

const configs=computed(()=>{
    return useConfigs().configs
})

const EnableMonthShiftCourse=computed(()=>{ //按月计费课程是否支持手动排课出勤不计费：0否，1是（白塔岭定制）。
    return configs.value.EnableMonthShiftCourse==1
})      

const IsOpenShiftForDay=computed(()=>{ //是否支持按天排课：0否，1是（白塔岭定制）。
    return configs.value.IsOpenShiftForDay==1
})
//学员欠费时，是否可以点名上课：1是（默认），0：可以出勤但不能计费，2：不可出勤也不可计费，3：不可出勤也不可计费，根据学员收据的实缴费用来计算的可上课点名课时，只能上到已缴费用的课时，与学生的课程费用是否交清有关,4：在3的基础上新增：点名时，勾选了“试听”，支持勾选出勤。
const EnableCourseConfirmWhenLess=computed(()=>{
    return configs.value.EnableCourseConfirmWhenLess
})
const EnablePrintOneToOneCourseRecord=computed(()=>{ //是否开启打印一对一课程上课凭证：0不开启（默认），1开启
    return configs.value.EnablePrintOneToOneCourseRecord==1
})

const NewCourse_PrintCourseRecord=window.$xgj.op("NewCourse_PrintCourseRecord")//打印上课凭证

const dialogVisible = ref(false)
const loading=ref(false)
const popTitle=ref('提示')
const form=ref({
    weixin:1
})
const printCourseCard=ref(1)
const list=ref([] as any[])
const finishedList=ref([] as any[])
const cancelList=ref([] as any[])
const emptyTeacherList=ref([] as any[])
const isSuccess=ref(false)
const isShowFinished=ref(false)
const successList=ref([] as any[])
const failList=ref([] as any[])
const exceptionList=ref([] as any[])
const approvalList=ref([] as any[])
const errorList=ref([] as any[])


function submit(){
    if(isSuccess.value){
        if(successList.value.length==0){
            close()
            return
        }
        
        if(form.value.weixin==1&&EnablePrintOneToOneCourseRecord.value){
            let ids:any=[],cams:any=[]
            successList.value.forEach((item:any)=>{
                ids.push(item.ID)
                cams.push(item.CampusID)
            })
            //TODO:唤起原系统的打印上课凭证
            if (window.microApp&&ids.length&&cams.length) {
                window.microApp.dispatch({type:'scheduleManage:printCourseCard',courseCardData:{courseid:ids.join(','),campusid:cams[0]}})
            }
        }
        close()
    }else{
        if(isShowFinished.value){
            schedule()
        }else{
            if(finishedList.value.length > 0 || cancelList.value.length || emptyTeacherList.value.length) {
                isShowFinished.value = true;
            } else {
                schedule()
            }
        }
    }
}

let count=0
function schedule(){
    if(list.value.length==0){
        isSuccess.value=true
        return
    }
    loading.value=true
    setAttendanceOneToOneBatch({
        courseid: list.value[count].ID, 
        Weixin: form.value.weixin
    }).then((resData:any)=>{
        let exceptionList = resData.Data.ExceptionList || [] ,
            approvalList = resData.Data.ApprovalList || [] ,
            exceptionName:any[] = [],  approvalName:any[] = [];
        if ( exceptionList.length ) {
            exceptionList.forEach((item:any)=>{
                exceptionName.push(item.Name) ;
            });
            exceptionList.value.push({ClassName: list.value[count].ClassName, studentName: exceptionName.join('，')});
        } else if ( approvalList.length ) {
            approvalList.forEach((item:any)=>{
                approvalName.push(item.Name) ;
            });
            approvalList.value.push({ClassName: list.value[count].ClassName, studentName: approvalName.join('，')});
        }
        successList.value.push(list.value[count])
        count++;
        if(count != list.value.length) {
            schedule()
        } else {
            isSuccess.value = true;
        }
        
    }).catch((err:any)=>{
        if(err.ErrorCode==400){
            let params = {};
            Object.assign(params, list.value[count],{
                ErrorMsg: err.ErrorMsg.replace('<br/>', '')
            })
            errorList.value.push(params)
            count++;
            if(count != list.value.length) {
                schedule()
            } else {
                isSuccess.value = true;
            }
        }
    }).finally(()=>{
        loading.value=false
    })
}

function close() {
    dialogVisible.value = false;
    if(!isShowFinished.value && !isSuccess.value){
        _reject();
    }else{
        _resolve()
    }
    nextTick(()=>{  
        isSuccess.value=false
        isShowFinished.value=false
        successList.value=[]
        failList.value=[]
        exceptionList.value=[]
        approvalList.value=[]
        errorList.value=[]
        printCourseCard.value=1
        count=0
        
        
    })
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    popTitle.value=params.popTitle||'提示'
    list.value = params.list;
    finishedList.value = params.finishedList || [];
    cancelList.value = params.cancelList || [];
    emptyTeacherList.value = params.emptyTeacherList || [];
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