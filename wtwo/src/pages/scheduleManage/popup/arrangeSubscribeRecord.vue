<!-- 预约记录 | 排队记录 -->
<template>
    <el-drawer 
        v-model="drawer" 
        direction="rtl" 
        size="1150px" 
        class="arrangeSubscribeRecord"
        :close-on-click-modal="false" 
        :append-to-body="true" 
        @close="close" 
        :destroy-on-close="true">
        <template #header>
            <el-tabs v-model="activeName" class="demo-tabs">
                <el-tab-pane label="预约记录" name="appointRecord"></el-tab-pane>
                <el-tab-pane label="排队记录" name="appointQueue"></el-tab-pane>
            </el-tabs>
        </template>
        <div class="min-w-[1150px]" v-loading="loading">
            <!-- 筛选条件 -->
            <div class="drawer-filter-box mb-[12px]">
                <div class="drawer-filter-box-item" v-if="activeName === 'appointRecord'">
                    <el-select
                        v-model="condition.Status"
                        placeholder="不限"
                        clearable
                        collapse-tags
                        collapse-tags-tooltip
                        filterable
                    >
                        <template #prefix>
                            <p class="search-input-label">预约状态</p>
                        </template>
                        <el-option :value="0" label="不限"></el-option>
                        <el-option :value="1" label="已预约"></el-option>
                        <el-option :value="2" label="已取消"></el-option>
                    </el-select>
                </div>
                <div class="drawer-filter-box-item" v-if="activeName === 'appointQueue'">
                    <el-select
                        v-model="condition.Status"
                        placeholder="不限"
                        clearable
                        collapse-tags
                        collapse-tags-tooltip
                        filterable
                    >
                        <template #prefix>
                            <p class="search-input-label">排队状态</p>
                        </template>
                        <el-option :value="-1" label="不限"></el-option>
                        <el-option :value="0" label="排队中"></el-option>
                        <el-option :value="1" label="已预约"></el-option>
                        <el-option :value="2" label="已取消"></el-option>
                    </el-select>
                </div>
                <div class="drawer-filter-box-item">
                    <input-tag
                        :label="transToConfigDescript('上课课程')"
                        placeholder="请选择"
                        :isLine="true"
                        v-model:value="condition.ShiftName"
                        :multiple="false"
                        :searchable="true"
                        :api-config="shiftApiConfig"
                        :selected="condition.shiftSelected"
                        @choose="selectCourse"
                        @change="handleShiftChange"
                    >
                        <template #btn-icon>
                            <el-icon size="20px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanke"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                        <template #option="{ option, isSelected }">
                            <input-tag-option
                                :option="option"
                                :isSelected="isSelected"
                                :label="option.Name"
                                iconColor="#1890FF"
                                iconShape="square"
                                iconText="课"
                            >
                                <template #content="{ option, label }">
                                    <div class="option-content">
                                        <span class="option-title ellipsis-single" :title="label">{{ label }}</span>
                                        <el-tag v-if="option.Status === 0" type="info" size="small" class="ml-8px">已停用</el-tag>
                                    </div>
                                </template>
                            </input-tag-option>
                        </template>
                        <template #dropdown-footer>
                            <span class="switch-wrap">
                                <el-switch
                                    v-model="finished"
                                    :active-value="5"
                                    :inactive-value="1"
                                    size="small"
                                ></el-switch>
                                <span class="switch-title"
                                    >包含停用{{transToConfigDescript('课程')}}</span
                                >
                            </span>
                        </template>
                    </input-tag>
                </div>
                <div class="drawer-filter-box-item">
                    <input-tag
                        :label="transToConfigDescript('任课老师')"
                        placeholder="请选择"
                        :isLine="true"
                        :multiple="true"
                        :selected="condition.teacherList"
                        @click="selectTeacher"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </div>
                <div class="drawer-filter-box-item">
                    <input-tag
                        :label="transToConfigDescript('上课学员')"
                        placeholder="请选择"
                        :isLine="true"
                        v-model:value="condition.studentName"
                        :selected="condition.studentSelected"
                        :searchable="true"
                        :api-config="studentApiConfig"
                        @choose="selectStudent"
                        @change="handleStudentChange"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                        <template #option="{ option, isSelected }">
                            <input-tag-option
                                :option="option"
                                :isSelected="isSelected"
                                :label="option.Name"
                                :iconImage="option.Photo"
                                iconColor="linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%)"
                                iconShape="square"
                                :iconText="option.Name?.slice(0,1)"
                            >
                                <template #content="{ option, label }">
                                    <div class="option-content">
                                        <span class="option-title ellipsis-single" :title="label">{{ label }}</span>
                                    </div>
                                </template>
                            </input-tag-option>
                        </template>
                    </input-tag>
                </div>
                <div class="drawer-filter-box-item !w-[548px]">
                    <el-date-picker
                        type="daterange"
                        unlink-panels
                        range-separator="到"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        :prefix-icon="customPrefix"
                        :shortcuts="dateShortcuts"
                        v-model="condition.dateRange"
                        value-format="YYYY-MM-DD"
                        format="YYYY-MM-DD"
                        class="!w-full"
                    >
                    </el-date-picker>
                </div>
                <div class="drawer-filter-box-item !w-[548px] flex-end">
                    <el-button type="primary" @click="getFirstPage">查询</el-button>
                    <el-button @click="handleReset">重置</el-button>
                </div>
            </div>

            <!-- 操作头部 -->
            <div class="flex justify-end items-center mb-[12px] mr-[14px]" v-if="activeName === 'appointQueue' || NewCourse_ExportSubscribeCourseHistory">
                <el-button type="primary" link @click="refreshQueue" :loading="refreshLoading" v-if="activeName === 'appointQueue'">
                    <el-icon class="mr-1">
                        <RefreshRight />
                    </el-icon>
                    刷新排队记录
                </el-button>
                <el-button type="primary" link @click="handleExport" :loading="exportLoading" v-if="NewCourse_ExportSubscribeCourseHistory">
                    <el-icon class="mr-1">
                        <Upload />
                    </el-icon>
                    导出记录
                </el-button>
            </div>

            <!-- 表格 -->
            <div class="px-[16px]">
                <el-table 
                    :data="list" 
                    class="w-100%"
                    v-loading="loading"
                    border>
                    <template #empty>
                        <el-empty
                            :image="globalData.emptyPng"
                            description="暂无数据"
                            :image-size="100"
                        ></el-empty>
                    </template>
                    <el-table-column prop="ShiftName" :label="transToConfigDescript('上课课程')" min-width="200"/>
                    <el-table-column prop="StudentUserName" label="预约学员" min-width="120"/>
                    <el-table-column prop="StudentSerial" label="学号" width="100" />
                    <el-table-column prop="StudentSmsTel" label="手机号" width="120" />
                    <el-table-column prop="CampusName" :label="transToConfigDescript('上课校区')" min-width="120" />
                    <el-table-column prop="" :label="transToConfigDescript('上课时间')" min-width="250">
                        <template #default="scope">
                            {{ formatStartEndTime(scope.row.StartTime, scope.row.EndTime) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="EmployeeName" :label="transToConfigDescript('任课老师')" min-width="150" />
                    <template v-if="activeName === 'appointRecord'">
                        <el-table-column prop="StatusName" label="预约状态" width="100">
                            <template #default="scope">
                                <div class="flex items-center">
                                    <div class="tag-status" :class="scope.row.StatusCode === 1 ? 'success' : scope.row.StatusCode === 2 ? 'cancel' : ''"></div>
                                    {{ scope.row.StatusCode === 1 ? '已预约' : scope.row.StatusCode === 2 ? '已取消' : '' }}
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="" label="预约/取消时间" width="150">
                            <template #default="scope">
                                <span v-if="scope.row.StatusCode === 1">{{ scope.row.ApplyTime }}</span>
                                <span v-if="scope.row.StatusCode === 2">{{ dayjs(scope.row.CancelTime).format('YYYY-MM-DD')=='1900-01-01'?'':dayjs(scope.row.CancelTime).format('YYYY-MM-DD HH:mm') }}</span>
                            </template>
                        </el-table-column>
                        <!-- <el-table-column prop="CancelTimeInterval" label="取消时间间隔" width="120">
                            <template #header>
								<div class="flex-center">
									取消时间间隔
									<el-tooltip
										class="box-item"
										effect="dark"
										placement="top"
									>
										<el-icon
											size="16px"
											class="ml-4px"
											color="#909399"
										>
											<svg aria-hidden="true">
												<use
													xlink:href="#w2-xinxitishi"
												></use>
											</svg>
										</el-icon>
										<template #content>
											上课开始时间和操作取消时间中间的间隔，从而得出学员在上课开始前多久取消预约。
										</template>
									</el-tooltip>
								</div>
							</template>
                        </el-table-column> -->
                    </template>
                    <template v-if="activeName === 'appointQueue'">
                        <el-table-column prop="StatusName" label="排队状态" width="100">
                            <template #default="scope">
                                <div class="flex items-center">
                                    <div class="tag-status" :class="scope.row.Status === 0 ? '' : scope.row.Status === 1 ? 'success' :  scope.row.Status === 2 ? 'cancel' : ''"></div>
                                    {{ scope.row.Status === 0 ? '排队中' : scope.row.Status === 1 ? '已预约' : scope.row.Status === 2 ? '已取消' : '' }}
                                </div>
                            </template>
                        </el-table-column>
                    </template>
                    <el-table-column label="操作" width="90" fixed="right" align="center">
                        <template #default="scope">
                            <el-button
                                link 
                                v-if="activeName === 'appointRecord' && NewCourse_CancelSubscribeCourseRecords && scope.row.StatusCode === 1" 
                                type="primary"
                                @click="handleCancelSubscribe(scope.row)">
                                取消预约
                            </el-button>
                            <el-button 
                                link
                                v-if="activeName === 'appointQueue' && NewCourse_CancelSubscribeCourseRecords && scope.row.Status === 0" 
                                type="primary"
                                @click="handleCancelSubscribe(scope.row)">
                                取消排队
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="flex justify-end mt-4" v-if="list.length">
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        :current-page="page.PageIndex"
                        :page-sizes="[10, 20, 50, 100, 200]"
                        :page-size="page.PageSize"
                        layout="total, sizes, prev, jumper ,next"
                        :total="page.TotalCount"
                        size="small"
                    >
                    </el-pagination>
                </div>
            </div>
        </div>


        <!-- 取消预约弹窗 -->
        <el-dialog v-model="cancelSubscribeDialog" :title="`取消${activeName=='appointRecord'?'预约':'排队'}`" width="600px" :close-on-click-modal="false"
            @close="closeCancelSubscribeDialog">
            <div class="pb-5">
                <div class="my-5 my-2">
                    <label class="block mb-3 text-sm text-gray-800 font-medium">取消原因</label>
                    <el-input v-model="cancelReason" type="textarea" :rows="4" placeholder="请输入取消原因(限100字)"
                        maxlength="100" show-word-limit class="w-full" />
                </div>
                <el-checkbox v-model="sendNotification" :true-label="1" :false-label="0">
                    以微信/短信形式给家长发送取消预约信息(推送微信需要家长关注并登录"师生信"公众号)
                </el-checkbox>
            </div>
            <template #footer>
                <el-button @click="closeCancelSubscribeDialog">取消</el-button>
                <el-button type="primary" @click="confirmCancelSubscribe">确定</el-button>
            </template>
        </el-dialog>
    </el-drawer>
    <choose-student ref="chooseStudentRef"></choose-student>
    <choose-course ref="chooseCourseRef"></choose-course>
    <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
</template>

<script setup lang="ts">
import { ref, nextTick, computed, getCurrentInstance, watch } from 'vue';
import { getStudentDic, queryShift } from '@/api';
import { ElMessage } from 'element-plus';
import { Upload, RefreshRight } from '@element-plus/icons-vue';
import { querySubscribeRecords, 
        cancelCourseSubscribeCourseRecords,
        cancelSubscribeCourseQueue, 
        exportSubscribeRecords, 
        querySubscribeCourseQueue, 
        exportSubscribeCourseQueue,
        RefreshSubscribeCourseQueue
    } from '@/api/arrange';
import { weekDiff, assignPage, IPageModel, checkPageIndex, exportFile } from '@/utils';
import { cloneDeep } from 'lodash';
import { dayjs } from 'element-plus'
import { ApiConfig } from '@/components/common/input-tag/input-tag';
import { dateShortcuts } from '@/utils'
import { useConfigs, useCurrentCampuses } from '@/store';
import { transToConfigDescript } from '@/utils/filters/filters';

// 权限项和权限项
const NewCourse_CancelSubscribeCourseRecords = window.$xgj.op("NewCourse_CancelSubscribeCourseRecords"); // 取消预约、排队
const NewCourse_ExportSubscribeCourseHistory = window.$xgj.op("NewCourse_ExportSubscribeCourseHistory"); // 导出约课计划/预约记录/排队记录
const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})
const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
// 响应式数据
const drawer = ref(false);
const activeName = ref('appointRecord');
const loading = ref(false);
const exportLoading = ref(false);
const refreshLoading = ref(false);

// 筛选表单
const defaultCondition: any = {
    ShiftID: globalData.EMPTYGUID, // 上课课程ID
    ShiftName: '', // 上课课程名称
	shiftSelected: [], // 上课课程选中项数组
    StudentID: globalData.EMPTYGUID, // 学员ID
    studentName: '', // 学员名称
	studentSelected: [], // 学员选中项数组
    teacherList: [], // 任课老师列表
    dateRange: [dayjs(weekDiff(0)[0]).format('YYYY-MM-DD'),dayjs(weekDiff(0)[1]).format('YYYY-MM-DD')],
    Status: 0, // 预约状态：0不限，1已预约，2已取消
    Download: 0,
};
const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition));
// 表格数据
const list = ref([]);
const page = ref({
    TotalCount: 0,//总条数
    PageSize: 10,//每页条数
    PageIndex: 1,//第几页
    PageCount: 1 //总页数
} as IPageModel)

// 上课课程相关
const finished = ref(1)
const shiftApiConfig = computed(
    (): ApiConfig => ({
        apiFunction: queryShift,
        params: {
            pageSize: 10,
            pageIndex: 1,
            status: finished.value,
        },
        searchParam: 'query',
        debounceTime: 300,
    })
)
function handleShiftChange(val:any){
    if (val.length) {
		condition.value.ShiftID = val[0].ID
		condition.value.ShiftName = val[0].Name
		condition.value.shiftSelected = val
	} else {
		condition.value.ShiftID = globalData.EMPTYGUID
		condition.value.shiftSelected = []
	}
}

const chooseCourseRef = ref()
function selectCourse() {
	chooseCourseRef.value
		?.open({
			multi: false,
			required: true,
		})
		.then((res:any) => {
			if (res.data) {
				condition.value.ShiftID = res.data.ID
				condition.value.ShiftName = res.data.Name
				// 同时更新 shiftSelected 数组
				condition.value.shiftSelected = [res.data]

                console.log(res)
                console.log(condition.value)
			}
		})
}

const studentApiConfig = computed(
    (): ApiConfig => ({
        apiFunction: getStudentDic,
        params: {
            pageSize: 10,
            pageIndex: 1,
            Status:1
        },
        searchParam: 'query',
        debounceTime: 300,
    })
)
function handleStudentChange(val:any){
    if (val.length) {
		condition.value.StudentID = val[0].ID
		condition.value.studentName = val[0].Name
		condition.value.studentSelected = val
	} else {
		condition.value.StudentID = globalData.EMPTYGUID
		condition.value.studentSelected = []
	}
}
const chooseStudentRef = ref()
function selectStudent() {
	chooseStudentRef.value
		?.open({
			multi: false,
		})
		.then((res: any) => {
			if (res.data) {
				condition.value.StudentID = res.data.ID
				condition.value.studentName = res.data.Name
			}
		})
}

const chooseEmpTableRef = ref()
function selectTeacher() {
	chooseEmpTableRef.value
		?.open({
			multi: true,
			choosed: condition.value.teacherList
		})
		.then((res: any) => {
			condition.value.teacherList = res.data || []
		})
}

// 查询数据
const funcQuery = async () => {
    list.value = [];
    loading.value = true;
    try {
        let params: any = {}
	
        // 从 condition 中排除需要特殊处理的参数
        const { shiftSelected, StudentID, studentName, studentSelected, teacherList, dateRange, ...conditionParams } = condition.value
        Object.assign(params, conditionParams, page.value);

        if (condition.value.dateRange && condition.value.dateRange.length === 2) {
            params.StartTime = condition.value.dateRange[0]
            params.EndTime = condition.value.dateRange[1]
        }
        params.StudentUserID = condition.value.StudentID;
        // 处理老师ID列表
        params.EmployeeIDList = condition.value.teacherList.map((item: any) => item.ID)
        params.CampusIDList = currentCampus.value
            ? currentCampus.value.split(',')
            : []

        const _api = activeName.value === 'appointRecord' ? querySubscribeRecords : querySubscribeCourseQueue;
        const res = await _api(params);
        list.value = res.Data.List || [];
        assignPage(page.value, res.Data)
    } catch (error) {
    } finally {
        loading.value = false;
    }
};

// 重置
function handleReset() {
	Object.assign(condition.value, cloneDeep(defaultCondition))
    if(activeName.value === 'appointQueue') { // 排队记录默认状态为不限
        condition.value.Status = -1;
    }
	getFirstPage()
}

// 导出记录
const handleExport = async () => {
    exportLoading.value = true;
    loading.value = true;
    try {
        let params: any = {}
	
        // 从 condition 中排除需要特殊处理的参数
        const { shiftSelected, StudentID, studentName, studentSelected, teacherList, dateRange, ...conditionParams } = condition.value
        Object.assign(params, conditionParams, page.value);

        if (condition.value.dateRange && condition.value.dateRange.length === 2) {
            params.StartTime = condition.value.dateRange[0]
            params.EndTime = condition.value.dateRange[1]
        }
        params.StudentUserID = condition.value.StudentID;
        // 处理老师ID列表
        params.EmployeeIDList = condition.value.teacherList.map((item: any) => item.ID)
        params.CampusIDList = currentCampus.value
            ? currentCampus.value.split(',')
            : []

        const _api = activeName.value === 'appointRecord' ? exportSubscribeRecords : exportSubscribeCourseQueue;
        _api(params).then((res:any)=>{
            exportFile(res, activeName.value === 'appointRecord'?'预约记录':'排队记录');
        });
        
    } finally {
        exportLoading.value = false;
        loading.value = false;
    }
};

// 刷新排队记录
async function refreshQueue() {
    refreshLoading.value = true;
    loading.value = true;
    try {
        await RefreshSubscribeCourseQueue({
            campusIDList: currentCampus.value
                ? currentCampus.value.split(',')
                : []
        });
        ElMessage.success('刷新成功');
        getFirstPage();
    } finally {
        refreshLoading.value = false;
        loading.value = false;
    }
}

// 分页处理
function handleSizeChange(val: number) {
    page.value.PageSize = val;
    page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
    funcQuery();
}
function handleCurrentChange(val: number) {
    page.value.PageIndex = val;
    funcQuery()
}
function getFirstPage() {
    page.value.PageIndex = 1;
    funcQuery();
};

// 取消预约弹窗相关数据
const cancelSubscribeDialog = ref(false);
const cancelReason = ref('');
const sendNotification = ref(1);
const currentCancelSubscribe = ref<any>(null);

// 预约学员取消
function handleCancelSubscribe(row: any) {
    currentCancelSubscribe.value = row;
    cancelReason.value = '';
    sendNotification.value = 1;
    cancelSubscribeDialog.value = true;
}

// 确认取消预约
let arrangeTabLoading = ref(false);
function confirmCancelSubscribe() {
    if (!currentCancelSubscribe.value) return;
    arrangeTabLoading.value = true;

    const _api = activeName.value === 'appointRecord' ? cancelCourseSubscribeCourseRecords : cancelSubscribeCourseQueue;

    let params:any={
        CancelReason: cancelReason.value,
        IsSendMsg: sendNotification.value,
    }
    if(activeName.value === 'appointRecord'){
        params.SubscribeCourseRecordsID=currentCancelSubscribe.value.ID
    }else{
        params.ID=currentCancelSubscribe.value.ID
    }
    _api(params).then((res: any) => {
        ElMessage.success(`已取消${activeName.value === 'appointRecord'?'预约':'排队'}`);
        // 关闭弹窗
        cancelSubscribeDialog.value = false;
        currentCancelSubscribe.value = null;
        // 刷新数据
        getFirstPage();
    }).catch((error: any) => {
        // ElMessage.error('预约取消失败');
    }).finally(() => {
        arrangeTabLoading.value = false;
    })
}

// 关闭取消预约弹窗
function closeCancelSubscribeDialog() {
    cancelSubscribeDialog.value = false;
    currentCancelSubscribe.value = null;
    cancelReason.value = '';
}

const formatStartEndTime = (startTime: string, endTime?: string): string => {
    const getWeekday = (time: string) => {
        if (!time) return '';
        const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
        const day = dayjs(time).day();
        return weekdays[day];
    };
    
    if (!startTime) return '';
    
    const weekday = getWeekday(startTime);
    
    const sTime = dayjs(startTime).format('YYYY-MM-DD HH:mm');
    const eTime = endTime ? dayjs(endTime).format('HH:mm') : '';
    return sTime + (eTime ? '~' + eTime : '') + '[' + weekday + ']';
}

const customPrefix = {
    render() {
        return '上课时间'
    },
}

watch(()=>activeName.value,()=>{
	nextTick(()=>{
		handleReset()
	})
})

// 对外暴露方法
let _resolve: any = null,
    _reject: any = null;

function open(params: any) {
    getFirstPage();
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
        drawer.value = true;
    });
}

function close() {
    drawer.value = false;
    activeName.value = 'appointRecord';
    condition.value=cloneDeep(defaultCondition)
    list.value=[]
    loading.value=false

    _reject && _reject();
}

defineExpose({
    open,
});
</script>

<style lang="scss">
.arrangeSubscribeRecord {
    .wtwo-drawer__header {
        padding-bottom: 0 !important;
        padding-top: 0 !important;
    }

    .wtwo-tabs__item {
        height: 46px;
        line-height: 46px;
        margin: 0;
    }

    .wtwo-tabs--top .wtwo-tabs__nav,
    .wtwo-tabs--bottom .wtwo-tabs__nav {
        height: 46px;
    }

    .drawer-filter-box {
        .drawer-filter-box-item {
            width: 266px;
        }
    }
    
    .tag-status {
        width: 8px;
        height: 8px;
        margin-right: 6px;
        border-radius: 50%;
        background: rgba(40, 120, 232 ,0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        &::before {
            content: '';
            display: block;
            width: 4px;
            height: 4px;
            border-radius: 50%;
            background: #2878E8;
        }
        &.success {
            background: rgba(103,194,58,0.2);
            &::before {
                background: #67C23A;
            }
        }
        &.cancel {
            background: rgba(245,108,108,0.2);
            &::before {
                background: #F56C6C;
            }
        }
    }
}
</style>