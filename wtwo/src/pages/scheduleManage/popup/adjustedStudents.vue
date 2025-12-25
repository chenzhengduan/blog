<!-- 临时调课已调出学员 -->
<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="adjustedStudents" 
        width="720px" 
        top="5%" 
        title="查看已调出学员"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <div class="flex-between mb-8px!">
                <span class="text-14px color-[#999]">下列显示历史调出的学员</span>
                <el-checkbox v-model="checkConflict" :true-value="1" :false-value="0" :disabled="loading||!NewCourse_IngoreCourseConflict">{{transToConfigDescript('撤销调出时，检查上课冲突')}}</el-checkbox>
            </div>
            <el-table :data="studentList" style="width: 100%" :max-height="`calc(-180px + 100vh)`">
                <el-table-column prop="Name" label="姓名" width="220">
                    <template #default="scope">
                        <div class="flex-center">
                            <div class="avatar" v-if="scope.row.Photo">
                                <img :src="scope.row.Photo" alt="" />
                            </div>
                            <div class="avatar placeholder" v-else>
                                {{ getNameInitial(scope.row.Name) }}
                            </div>
                            <div class="ellipsis-single" :title="scope.row.Name">{{scope.row.Name}}</div>
                            <el-tag
                                v-if="(scope.row.AdjustFlag == 2) || (scope.row.AdjustFlag == 3 && scope.row.IsSubscribeCourse == 0)"
                                class="ml-4px px-2px! h-18px!"
                                type="warning"
                                size="small"
                                :hit="true"
                                >{{ scope.row.AdjustFlag == 2 ? "临调" : "临加" }}</el-tag
                            >
                            <el-tag
                                v-if="scope.row.IsSubscribeCourse == 1"
                                class="ml-4px px-2px! h-18px!"
                                type="warning"
                                size="small"
                                :hit="true"
                                >约课</el-tag
                            >
                        </div>
                    </template>
                </el-table-column>
                <el-table-column prop="SMSTel" label="联系电话" width="110" />
                <el-table-column prop="AbsentCauseName" label="缺勤原因" show-overflow-tooltip/>
                <el-table-column prop="AdjustTime" label="调出时间" width="150"></el-table-column>
                <el-table-column prop="op" label="操作" width="70">
                    <template #default="scope">
                        <el-link type="primary" underline="never" @click="handleAdjustOut(scope.row)">撤销调出</el-link>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <ConflictPrompt ref="conflictPromptRef" />
    </el-dialog>
</template>
<script lang="ts" setup>
import { unAdjustForStudents } from '@/api/arrange';
import useEvent from '@/config/event';
import { cloneDeep } from 'lodash';
import { ref, nextTick } from 'vue';
import { getNameInitial } from '@/utils';
import { transToConfigDescript } from '@/utils/filters/filters';
import ConflictPrompt from './conflictPrompt.vue';

const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限

const dialogVisible=ref(false)
const loading=ref(false)
const studentList=ref([])
const checkConflict=ref(1)
const event = useEvent()

let courseID=""
const conflictPromptRef = ref()
function handleAdjustOut(row:any){
    ElMessageBox.confirm('确定撤销该学员的调课吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(()=>{
        loading.value=true
        unAdjustForStudents({
            CourseID:courseID,
            StudentID:row.ID,
            IsCheckConflict:checkConflict.value
        }).then((res:any)=>{
            studentList.value=studentList.value.filter((item:any)=>item.ID!=row.ID)
            ElMessage.success("已撤销调课")
            event.emit("adjust-course-student-refresh")
            event.emit("arrange-table-list-refresh",{refreshTotal:true})
            
        }).catch((error: any) => {
            if(error.ErrorCode==409){
                conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{
                }).catch((back:any)=>{
                    if(back&&back.closeCurrent){
                        dialogVisible.value=false
                    }
                })
            }
        }).finally(()=>{
            loading.value=false
        })
    })
    
}


function close(){
    dialogVisible.value=false
    checkConflict.value=1
    _reject();
}

let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    dialogVisible.value = true;
    courseID=params.courseID||''

    studentList.value=params.studentList?cloneDeep(params.studentList):[]
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
.adjustedStudents{
    .box-wrapper{
        padding-top: 8px;
    }
    .avatar {
        width: 24px;
        height: 24px;
        border-radius: 4px;
        overflow: hidden;
        background: #f2f3f5;
        flex-shrink: 0;
        margin-right: 4px;
    }

    .avatar img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .avatar.placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
        color: #fff;
        font-weight: 500;
        font-size: 14px;
    }
}
</style>
