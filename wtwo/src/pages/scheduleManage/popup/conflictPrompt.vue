<template>
    <el-dialog
        v-model="dialogVisible"
        title="检查提示"
        width="950px"
        :close-on-click-modal="false"
        :destroy-on-close="true"
        class="conflict-prompt"
        draggable
    >
        <div class="box-wrapper" v-loading="loading">
            <el-scrollbar style="max-height: 400px;">
                <template v-if="info.CheckSum>0">
                    <div class="flex-center conflict-msg-title">
                        <el-icon size="16px" class="mr-[4px]!">
                            <svg aria-hidden="true">
                                <use xlink:href="#w2-xianzhi"></use>
                            </svg>
                        </el-icon>
                        <div>被限制：<span class="color-[#F53F3F]">{{ info.CheckSum||0 }}</span>节排课被限制</div>
                    </div>
                    <div class="mb-[16px]!">
                        <div class="conflict-msg-item" v-for="item in info.CheckFieldList">{{ item }}</div>
                    </div>
                </template>
                <template v-if="info.ConflictSum>0">
                    <div class="flex-center conflict-msg-title">
                        <el-icon size="16px" class="mr-[4px]!">
                            <svg aria-hidden="true">
                                <use xlink:href="#w2-chongtu"></use>
                            </svg>
                        </el-icon>
                        <div>有冲突：系统中的下列数据，与当前排课/日程存在冲突</div>
                    </div>
                    <page-attention-tips class="mb-6px">注意:下列数据是系统已存在的数据，你可通过“修改已存在的数据”或“修改当前排课”来避免冲突;</page-attention-tips>
                    <div v-if="info.ConflictDataList.length">
                        <el-table :data="info.ConflictDataList" :max-height="200">
                            <el-table-column prop="CampusName" label="校区" width="100px">
                                <template #default="scope">
                                    <div class="column-text ellipsis-single" :title="scope.row.CampusName">
                                        {{ scope.row.CampusName }}
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column prop="Name" label="名称">
                                <template #default="scope">
                                    <div class="flex-center">
                                        <div class="column-text ellipsis-single" :title="scope.row.CourseMethod==10?scope.row.ClassName:
                                            scope.row.CourseMethod==20?(scope.row.StudentName||scope.row.ClassName):
                                            scope.row.CourseMethod==30?scope.row.ShiftName:
                                            scope.row.ConflictingName">
                                            {{ scope.row.CourseMethod==10?scope.row.ClassName:
                                            scope.row.CourseMethod==20?(scope.row.StudentName||scope.row.ClassName):
                                            scope.row.CourseMethod==30?scope.row.ShiftName:
                                            scope.row.ConflictingName }}
                                        </div>
                                        
                                        <el-tooltip
                                            v-if="scope.row.ConflictFieldList.indexOf('StudentUserID')!=-1||scope.row.ConflictFieldList.indexOf('ShiftID')!=-1||scope.row.ConflictFieldList.indexOf('ClassID')!=-1"
                                            effect="dark"
                                            content="此内容与当前排课/日程，在日期与时间上存在冲突！"
                                            placement="top"
                                        >
                                            <el-icon>
                                                <svg aria-hidden="true">
                                                    <use xlink:href="#w2-xianzhi"></use>
                                                </svg>
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                    
                                </template>
                            </el-table-column>
                            <el-table-column prop="CourseMethod" label="类型" width="60px">
                                <template #default="scope">
                                    {{ scope.row.CourseMethod==40?'日程':'排课' }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="Date" :label="transToConfigDescript('上课/日程日期')" width="115"></el-table-column>
                            <el-table-column prop="StartTime" :label="transToConfigDescript('上课/日程时间')" width="110">
                                <template #default="scope">
                                    {{ scope.row.StartTime }} - {{ scope.row.EndTime }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="ClassRoomName" :label="transToConfigDescript('上课教室')">
                                <template #default="scope">
                                    <div class="flex-center">
                                        <div class="column-text ellipsis-single" :title="scope.row.ClassRoomName">
                                            {{ scope.row.ClassRoomName }}
                                        </div>
                                        <el-tooltip
                                            v-if="scope.row.ConflictFieldList.indexOf('ClassRoomID')!=-1"
                                            effect="dark"
                                            content="此内容与当前排课/日程，在日期与时间上存在冲突！"
                                            placement="top"
                                        >
                                            <el-icon>
                                                <svg aria-hidden="true">
                                                    <use xlink:href="#w2-xianzhi"></use>
                                                </svg>
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column prop="MainTeacherList" :label="transToConfigDescript('任课/日程老师')" show-overflow-tooltip>
                                <template #default="scope">
                                    <div class="flex-center">
                                        <div class="column-text ellipsis-single" :title="scope.row.MainTeacherList?scope.row.MainTeacherList.map((item:any)=>item.Name).join(','):''">
                                            {{ scope.row.MainTeacherList?scope.row.MainTeacherList.map((item:any)=>item.Name).join(','):'' }}
                                        </div>
                                        <el-tooltip
                                            v-if="scope.row.ConflictFieldList.indexOf('MainTeacherList')!=-1"
                                            effect="dark"
                                            content="此内容与当前排课/日程，在日期与时间上存在冲突！"
                                            placement="top"
                                        >
                                            <el-icon>
                                                <svg aria-hidden="true">
                                                    <use xlink:href="#w2-xianzhi"></use>
                                                </svg>
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column prop="AssistantTeacherList" label="助教" show-overflow-tooltip>
                                <template #default="scope">
                                    <div class="flex-center">
                                        <div class="column-text ellipsis-single" :title="scope.row.AssistantTeacherList?scope.row.AssistantTeacherList.map((item:any)=>item.Name).join(','):''">
                                            {{ scope.row.AssistantTeacherList?scope.row.AssistantTeacherList.map((item:any)=>item.Name).join(','):'' }}
                                        </div>
                                        <el-tooltip
                                            v-if="scope.row.ConflictFieldList.indexOf('AssistantTeacherList')!=-1"
                                            effect="dark"
                                            content="此内容与当前排课/日程，在日期与时间上存在冲突！"
                                            placement="top"
                                        >
                                            <el-icon>
                                                <svg aria-hidden="true">
                                                    <use xlink:href="#w2-xianzhi"></use>
                                                </svg>
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column prop="op" label="操作" width="50">
                                <template #default="scope">
                                    <el-link type="primary" v-if="(scope.row.CourseMethod==40&&NewCourse_ScheduleEdit)||(scope.row.CourseMethod!=40&&NewCourse_CourseEdit&&scope.row.Finished==0)" underline="never" @click="editCurrent(scope.row)">修改</el-link>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </template>
            </el-scrollbar>
        </div>
        <template #footer>
            <div class="flex-end">
                <!-- <el-button @click="close" v-if="enablePreview">去预览页修改</el-button> -->
                <el-button type="primary" @click="close">返回修改</el-button>
            </div>
        </template>
    </el-dialog>    
</template>
<script setup lang="ts">
import useEvent from '@/config/event';
import { useUserCampuses } from '@/store';
import { transToConfigDescript } from '@/utils/filters/filters';
import { cloneDeep } from 'lodash';
import { ref, nextTick } from 'vue';
const event = useEvent()

const dialogVisible = ref(false);
const loading = ref(false);
const info = ref({} as any);
const enablePreview = ref(false);

const NewCourse_CourseEdit = window.$xgj.op('NewCourse_CourseEdit') //编辑排课
const NewCourse_ScheduleEdit = window.$xgj.op('NewCourse_ScheduleEdit') //编辑日程

function editCurrent(row:any){
    const userCampusesStore = useUserCampuses()
    const campusList = userCampusesStore.userCampuses || []
    if(campusList.findIndex((item:any)=>item.ID==row.CampusID)==-1){
        ElMessageBox.alert(`需要以下校区操作权限：${row.CampusName}`, '提示', {type: 'warning'})
        return
    }
    ElMessageBox.confirm(`注意！会打开该排课/日程的编辑界面，当前页面会被关闭，确认要打开吗？`, '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消'
    }).then(() => {
        dialogVisible.value=false 
        _reject && _reject({closeCurrent:true});
        nextTick(()=>{
            if(row.CourseMethod==40){
                let obj:any=cloneDeep(row)
                obj.ID=row.ConflictingID
                event.emit('request-edit-schedule', obj)
            }else{
                let obj:any=cloneDeep(row)
                obj.ID=row.ConflictingID
                event.emit('request-edit-arrange', obj)
            }
        
        })
    })
}

let _resolve: any = null,
    _reject: any = null

function open(params: any) {
    dialogVisible.value = true;
    info.value = params.info;
    enablePreview.value = params.enablePreview||false;
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
}

function close() {
    dialogVisible.value = false;
    info.value = {};
    _reject && _reject();
}

defineExpose({
    open,
    close
})
</script>
<style lang="scss" scoped>
.conflict-prompt{
    .conflict-msg-title{
        line-height: 22px;
        margin-bottom: 8px;
        font-weight: 500;
        color: #303133;
    }
    .conflict-msg-item{
        background-color: #F4F4F5;
        color: #606266;
        padding: 6px 16px;
        line-height: 22px;
        border-radius: 4px;
        &+ .conflict-msg-item{
            margin-top: 6px;
        }
    }
    .column-text{
        max-width: 100%;
    }
}
</style>