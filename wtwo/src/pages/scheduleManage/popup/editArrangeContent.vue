<!-- 修改上课内容 -->
<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="editArrangeContent" 
        width="800px" 
        top="5%" 
        :title="transToConfigDescript('修改上课内容')"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <el-alert type="primary" :closable="false" class="!mt-12px" show-icon v-if="isBatch">
            <template #title>
                <div class="text-#606266">已选择“<span class="font-bold">{{allList[0].ShiftName}}</span>”{{transToConfigDescript('课程')}}的<span class="font-bold">“{{allList.length}}”</span> 节排课</div>
            </template>
        </el-alert>
        <el-form 
            :model="form" 
            ref="formRef" 
            label-position="top" 
            class="editArrangeContent"
            v-loading="loading">
            <div class="form-section">
                <div class="flex two-column-wrap">
                    <template v-if="scheduleData?.ShiftScheduleList?.length > 0">
                        <!-- <el-form-item
                            prop=""
                            label="上课进度"
                            class="half-width"
                        >
                            <template #label>
                                <div class="flex-between custom-checkbox-height">
                                    <div class="flex-center">
                                        <span>上课进度</span>
                                        <el-icon class="ml-4px" color="#909399" size="14px">
                                            <svg aria-hidden="true">
                                                <use xlink:href="#w2-xinxitishi"></use>
                                            </svg>
                                        </el-icon>
                                    </div>
                                    <el-checkbox
                                        v-model="form.IsPostpone"
                                        v-if="!isBatch && EnableAddCourse_ShiftSchedule == 1"
                                        :disabled="!ClassChapterEdit"
                                    >
                                        <div class="flex-center">
                                            <span>后续排课进度顺延</span>
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
                                                <template #content>勾选复选框，后续排课记录的上课进度从“{{form.ShiftAmount}}”依次顺延。</template>
                                            </el-tooltip>
                                        </div>
                                    </el-checkbox>
                                    <el-checkbox
                                        v-model="form.IsCover"
                                        v-if="!isBatch && EnableAddCourse_ShiftSchedule !== 1"
                                        :disabled="!ClassChapterEdit"
                                        class="ml-5px"
                                    >
                                        <div class="flex-center">
                                            <span>上课进度覆盖所有排课</span>
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
                                                <template #content>勾选复选框后，该班所有未上课排课记录的上课进度都为所选的进度。</template>
                                            </el-tooltip>
                                        </div>
                                    </el-checkbox>
                                </div>
                            </template>
                            <div 
                                ref="progressTriggerRef"
                                class="custom-select-trigger"
                                :class="{'disabled': !ClassChapterEdit}"
                                @click.stop="openPopover('progress', progressTriggerRef)"
                                v-click-outside="onClickOutside"
                            >
                                <span class="trigger-text mx-11px" v-if="form.ShiftAmount">{{ form.ShiftAmount }} / {{ scheduleData?.ShiftScheduleList?.length }}</span>
                                <span class="trigger-text mx-11px !text-#a8abb2" v-else>请选择</span>
                                <div class="h-100% px-12px flex-center">
                                    <el-icon 
                                        class="trigger-arrow" 
                                        :class="{ 'is-reverse': currentPopoverType === 'progress' && currentPopoverVisible }">
                                        <ArrowDown />
                                    </el-icon>
                                    <el-icon 
                                        class="trigger-delete" 
                                        @click.stop="clearSchedule">
                                        <CircleClose />
                                    </el-icon>
                                </div>
                            </div>
                        </el-form-item> -->
                        <el-form-item
                            prop=""
                            label="章节内容"
                            class="half-width progress-form-item"
                        >
                            <template #label>
                                <div class="flex-between custom-checkbox-height">
                                    <div class="flex-center">
                                        <span>章节内容</span>
                                    </div>
                                    <el-checkbox
                                        v-model="form.IsPostpone"
                                        v-if="!isBatch && EnableAddCourse_ShiftSchedule == 1"
                                        :disabled="!NewCourse_ClassChapterEdit"
                                    >
                                        <div class="flex-center">
                                            <span>后续排课进度顺延</span>
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
                                                <template #content>勾选复选框，后续排课记录的{{transToConfigDescript('上课进度')}}从“{{form.ShiftAmount}}”依次顺延。</template>
                                            </el-tooltip>
                                        </div>
                                    </el-checkbox>
                                    <el-checkbox
                                        v-model="form.IsCover"
                                        v-if="!isBatch && EnableAddCourse_ShiftSchedule != 1"
                                        :disabled="!NewCourse_ClassChapterEdit"
                                        class="ml-5px"
                                    >
                                        <div class="flex-center">
                                            <span>{{transToConfigDescript('上课进度覆盖所有排课')}}</span>
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
                                                <template #content>{{transToConfigDescript('勾选复选框后，该班所有未上课排课记录的上课进度都为所选的进度。')}}</template>
                                            </el-tooltip>
                                        </div>
                                    </el-checkbox>
                                </div>
                            </template>
                            <div 
                                ref="chapterTriggerRef"
                                class="custom-select-trigger"
                                :class="{'disabled': !NewCourse_ClassChapterEdit}"
                                @click.stop="openPopover('chapter', chapterTriggerRef)"
                                v-click-outside="onClickOutside"
                            >
                                <span class="trigger-text mx-11px" :class="{'!text-#a8abb2': !chapterContent}">{{ chapterContent || '请选择' }}</span>
                                <div class="h-100% px-12px flex-center">
                                    <el-icon 
                                        class="trigger-arrow" 
                                        :class="{ 'is-reverse': currentPopoverType === 'chapter' && currentPopoverVisible }">
                                        <ArrowDown />
                                    </el-icon>
                                    <el-icon 
                                        class="trigger-delete" 
                                        @click.stop="clearSchedule">
                                        <CircleClose />
                                    </el-icon>
                                </div>
                            </div>
                        </el-form-item>
                    </template>
                    <el-form-item
                        prop="Content"
                        :label="transToConfigDescript('上课内容')"
                        class="half-width"
                    >
                        <el-input
                            v-model="form.Content"
                            :placeholder="NewCourse_ClassContentEdit ? '请输入' : ''"
                            type="textarea"
                            :rows="2"
                            maxlength="2000"
                            class="w-100%!"
                            :disabled="!NewCourse_ClassContentEdit"
                        ></el-input>
                    </el-form-item>
                    <el-form-item
                        prop="courseware"
                        label="课件"
                        class="half-width"
                    >
                        <div class="custom-select-trigger disabled">
                            <div class="courseware-content">                                
                                <el-popover
                                    v-model:visible="coursewarePopoverVisible"
                                    placement="bottom-start"
                                    :width="350"
                                    v-if="coursewareSettingList.length > 0"
                                >
                                    <template #reference>
                                        <span class="courseware-text">{{ coursewareSettingList.map(item => item.FileName).join('、') }}</span>
                                    </template>
                                    <div class="max-h-300px overflow-y-auto">
                                        <div 
                                            v-for="(item, index) in coursewareSettingList" 
                                            :key="index"
                                            class="text-#409eff py-3px cursor-pointer overflow-hidden text-ellipsis whitespace-nowrap"
                                            @click="showWareDetail(item, item.FileID || '')"
                                        >
                                            {{ item.FileName }}
                                        </div>
                                    </div>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item
                        prop="InternalRemark"
                        label="对内备注"
                        class="half-width"
                    >
                        <el-input
                            v-model="form.InternalRemark"
                            type="textarea"
                            :rows="2"
                            maxlength="3000"
                            placeholder="请输入"
                            class="w-100%!"
                            :disabled="!NewCourse_ClassContentEdit"
                        ></el-input>
                    </el-form-item>
                    <!-- <el-form-item
                        prop="Describe"
                        label="对外备注"
                        class="half-width"
                    >
                        <el-input
                            v-model="form.Describe"
                            type="textarea"
                            :rows="2"
                            maxlength="3000"
                            placeholder="请输入"
                            class="w-100%!"
                        ></el-input>
                    </el-form-item> -->
                </div>
            </div>
        </el-form>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button type="primary" @click="submitForm" v-if="NewCourse_ClassContentEdit || NewCourse_ClassChapterEdit">保存</el-button>
            </div>
        </template>
    </el-dialog>

    <!-- 统一的上课进度选择弹窗 -->
	<el-popover
		:visible="currentPopoverVisible"
		placement="bottom-start"
		:width="600"
		:virtual-ref="currentTriggerRef"
		popper-class="edit-arrange-form-class-content-popover"
		virtual-triggering
		trigger="click"
	>
		<template #default>
			<div class="class-content-table-component">
				<el-table 
					:data="scheduleData?.ShiftScheduleList || []" 
					:max-height="300"
					@row-click="handleSelect"
					class="class-content-table w-100% cursor-pointer"
                    border
				>
					<el-table-column prop="ShiftAmount" :label="transToConfigDescript('课次')" width="60" align="center"/>
					<el-table-column prop="Title" label="章节内容" min-width="80" show-overflow-tooltip/>
					<el-table-column prop="Content" :label="transToConfigDescript('上课内容')" show-overflow-tooltip min-width="100" />
					<el-table-column label="课件" show-overflow-tooltip min-width="100">
						<template #default="{ row }">
							{{ row.CoursewareList.map((item: any) => item.FileName).join('、') }}
						</template>
					</el-table-column>
				</el-table>
			</div>
		</template>
	</el-popover>
</template>
<script lang="ts" setup>
import { computed, ref, getCurrentInstance } from 'vue';
import { dayjs, ElMessage, ClickOutside as vClickOutside } from 'element-plus';
import { ArrowDown, CircleClose } from '@element-plus/icons-vue';
import { 
    chekSeeCourseware, 
    getFilesUrl,
    getCoursesSchedule,
    editCoursesSchedule
} from '@/api/arrange';
import { useConfigs } from '@/store';
import { cloneDeep } from 'lodash';
import { transToConfigDescript } from '@/utils/filters/filters';

// 权限项和权限项
const configs = computed(() => {
	return useConfigs().configs
})
const NewCourse_ClassContentEdit = window.$xgj.op("NewCourse_ClassContentEdit"); // 修改上课内容权限
const NewCourse_ClassChapterEdit = window.$xgj.op("NewCourse_ClassChapterEdit"); // 修改上课章节
const NewCourse_CourseWareQuery = window.$xgj.op("NewCourse_CourseWareQuery"); // 电脑端排课管理随时查看课件。没有改权限时，需要根据课件规则决定是否能查看

const EnableAddCourse_ShiftSchedule=computed(()=>{ //在排课时，是否根据课程上设置的上课进度自动生成每一节课的上课进度：1是，0否(默认)
	return configs.value.EnableAddCourse_ShiftSchedule
})

const EnableCourseware = computed(()=>{ //是否开启课件管理功能(EnableCourseware)，1：是，0：否(默认)
	return configs.value.EnableCourseware == 1;
})

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
const dialogVisible=ref(false);
const loading=ref(false);
// 触发元素引用
const progressTriggerRef = ref();
const chapterTriggerRef = ref();
// 当前弹窗状态
const currentPopoverVisible = ref(false);
const currentTriggerRef = ref();
const currentPopoverType = ref<'progress' | 'chapter' | 'content'>('progress'); 
const defaultForm=ref<any>({
    CourseIDList: [] as string[], //排课ID
    ShiftScheduleID: globalData.EMPTYGUID, //上课进度计划ID，关联[tShiftSchedule].cID
    ShiftAmount: 0, //课次，关联[tShiftSchedule].cShiftAmount
    Content: '', //上课内容/进度信息
    InternalRemark: '', //对内备注
    Describe: '', //对外备注
    IsPostpone: false, //是否上课进度依次顺延
    IsCover: false, //开启上课进度覆盖所有排课
})
const form=ref(cloneDeep(defaultForm.value))
const coursewareSettingList = ref<any[]>([]); //课件列表
const chapterContent = ref(''); //章节内容TODO: 需要修改


// 处理popover弹窗打开
function openPopover(type: 'progress' | 'chapter' | 'content', triggerRef: any) {
    if (!NewCourse_ClassContentEdit) {
        return;
    }

    // 设置当前弹窗状态
    currentPopoverType.value = type;
    currentTriggerRef.value = triggerRef;
    currentPopoverVisible.value = true;
}

// 处理popover弹窗选择
function handleSelect(item: any) {
    form.value.ShiftAmount = item.ShiftAmount;
    form.value.Content = item.Content;
    form.value.ShiftScheduleID = item.ID;
    coursewareSettingList.value = item.CoursewareList;
    chapterContent.value = item.Title;

    // 关闭弹窗
    currentPopoverVisible.value = false;
    // 清理触发元素引用
    currentTriggerRef.value = null;
}

// 处理popover弹窗点击外部关闭
function onClickOutside(e: any) {
    if(currentPopoverVisible.value&&(currentTriggerRef.value&&!currentTriggerRef.value.contains(e.target))){
        currentPopoverVisible.value=false
        currentTriggerRef.value = null;
    }
}

// 清除上课进度和章节内容
function clearSchedule() {
    form.value.ShiftAmount = 0;
    form.value.Content = '';
    form.value.ShiftScheduleID = globalData.EMPTYGUID;
    coursewareSettingList.value = scheduleData.value.CoursewareList;
    chapterContent.value = '';
}

// 课件弹窗状态
const coursewarePopoverVisible = ref(false);

// 处理课件点击
function showWareDetail(item: any, fileId: string) {
	if (!isBatch.value && !NewCourse_CourseWareQuery) {
		loading.value = true
		chekSeeCourseware({
			terminal: 0,
			shiftId: item.ShiftID,
			courseId:allList.value[0].ID,
		})
			.then((res: any) => {
				if (res.Data == 1) {
					queryWareDetail(fileId)
				} else {
                    loading.value = false
					ElMessage.warning('无查看权限')
				}
			})
			.catch(() => {
				loading.value = false
			})
	} else {
		queryWareDetail(fileId)
	}
}
function queryWareDetail(fileId: string) {
    loading.value = true;
	getFilesUrl({ pagetype: 1 }).then((res: any) => {
		var previewUrl = res.Data + '?preview=true&id=' + fileId
		window.open(previewUrl)
	}).finally(() => {
		loading.value = false;
	})
}

const formRef=ref();
function submitForm(){
    loading.value = true;
    let params = cloneDeep(form.value);
    params.UpdateTime=dayjs().format('YYYY-MM-DD HH:mm:ss') // 修改时间
    editCoursesSchedule(params).then((res: any) => {
        if(res.ErrorCode==200){
            ElMessage.success('修改成功');
            dialogVisible.value=false;
            _resolve();
        }
    }).finally(() => {
        loading.value = false;
    })
}

const defaultSchedule=ref<any>({
    ClassShiftAmount: 0,
    Content: '',
    CourseID: '',
    CoursewareList: [],
    Describe: '',
    IsCover: 0,
    ShiftAmount: 0,
    ShiftScheduleID: globalData.EMPTYGUID,
    ShiftScheduleList: [] as any[]
});
const scheduleData=ref(cloneDeep(defaultSchedule.value))
function getData(){
    loading.value = true;
    getCoursesSchedule({
        courseID: allList.value[0].ID
    }).then((res: any) => {
        scheduleData.value = cloneDeep(res.Data);

        if (isBatch.value) { // 批量操作
            form.value.CourseIDList = allList.value.map((item: any) => item.ID);
        } else { // 单个操作
            let ret = cloneDeep(res.Data);
            const { IsCover, ClassShiftAmount, ShiftScheduleList } = ret;
            // if(EnableAddCourse_ShiftSchedule.value == 0) {
            //     if(allList.value[0].Finished == 0 && IsCover == 0) { // 1.未进行上课点名
            //         if(ClassShiftAmount == "") { // 该班级下面的所有排课记录都没有进行过点名操作。
            //             ret.ShiftAmount = 1;
            //         } else {
            //             if(parseInt(ret.ClassShiftAmount) >= ShiftScheduleList.length) {
            //                 ret.ShiftAmount = ShiftScheduleList.length;
            //             } else {
            //                 ret.ShiftAmount = parseInt(ret.ClassShiftAmount) + 1;
            //             }
            //         }
            //         ShiftScheduleList.forEach((item: any) => {
            //             if(item.ShiftAmount == ret.ShiftAmount) {
            //                 ret.Content = item.Content;
            //                 ret.ShiftScheduleID = item.ID;
            //             }
            //         });
            //     } else if(allList.value[0].Finished == 1 && Number(ret.ShiftAmount) == 0) { // 2. 已进行上课点名、未设置上课进度
            //         ret.ShiftAmount = 0;
            //         ret.ShiftScheduleID = globalData.EMPTYGUID;
            //     } else { // 3. 已进行上课点名、有设置上课进度
            //         ShiftScheduleList.forEach((item: any) => {
            //             if(item.ShiftAmount == ret.ShiftAmount) {
            //                 ret.ShiftScheduleID = item.ID;
            //             }
            //         })
            //     }
            // }
            form.value.CourseIDList = [allList.value[0].ID];
            form.value.ShiftScheduleID = ret.ShiftScheduleID;
            form.value.ShiftAmount = ret.ShiftAmount;
            form.value.Content = ret.Content;
            form.value.InternalRemark = ret.InternalRemark;
            form.value.Describe = ret.Describe;
            form.value.IsPostpone = !!ret.IsPostpone;
            form.value.IsCover = !!ret.IsCover;
            chapterContent.value = scheduleData.value.ShiftScheduleList.find((item: any) => item.ID == ret.ShiftScheduleID)?.Title || '';
        }

        // 当有上课进度时，才需要去匹配当前上课进度对应的课件
        if (form.value.ShiftScheduleID && form.value.ShiftScheduleID != globalData.EMPTYGUID) {
			if (res.Data.CourseWareList && res.Data.CourseWareList.length) { // 后端有返回当前上课进度的场景
                coursewareSettingList.value = res.Data.CoursewareList;
			}else { // 兼容班级-快速排课点名的处理
				scheduleData.value.ShiftScheduleList.forEach(function(item:any){
					if(item.ID == form.value.ShiftScheduleID){
						coursewareSettingList.value = cloneDeep(item.CoursewareList);
					}
				})
			}
		}
    }).finally(() => {
        loading.value = false;
    })
}

function close(){
    allList.value = [];
    isBatch.value = false;
    form.value = cloneDeep(defaultForm.value);
    scheduleData.value = cloneDeep(defaultSchedule.value);
    coursewareSettingList.value = [];
    chapterContent.value = '';
    loading.value = false;

    dialogVisible.value=false;
    _reject();
}

let _resolve: any = null,
    _reject: any = null,
    allList = ref<any[]>([]),
    isBatch = ref(false); //是否是批量操作

function open(params: any) {
    allList.value = params.selected;
    isBatch.value = params.isBatch || false;
    dialogVisible.value = true;
    getData();
    
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
.editArrangeContent {
    .two-column-wrap {
        padding: 20px 0;
        display: flex;
        gap: 0 20px;
        flex-wrap: wrap;
        .half-width {
            width: calc(50% - 10px);
            min-width: 0;
        }
    }
    
	.form-section {
		background-color: #fff;
		// margin: 20px 0;
	}

    // 自定义复选框高度样式
    .custom-checkbox-height {
        :deep(.wtwo-checkbox) {
            min-height: auto !important;
            height: auto !important;
        }
    }

    .custom-select-trigger {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 32px;
		border: 1px solid #dcdfe6;
		border-radius: 4px;
		background-color: #fff;
		cursor: pointer;
		transition: border-color 0.2s;
	
		&.disabled {
			color: #a8abb2;
			background-color: #f5f7fa;
			cursor: not-allowed;
			&:hover {
				border-color: #dcdfe6;
			}
		}
		
		&:hover {
			border-color: #c0c4cc;
		}
		
		.trigger-text {
			display: block;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			color: #333;
			font-size: 14px;
			flex: 1;
			min-width: 0;
		}
		
		.trigger-arrow {
			transition: transform 0.2s;
			color: #a8abb2;
			
			&.is-reverse {
				transform: rotate(180deg);
			}
		}
		
		.trigger-delete {
			display: none;
			color: #a8abb2;
			cursor: pointer;
			transition: color 0.2s;
		}
		
		&:hover {
			.trigger-arrow {
				display: none;
			}
			
			.trigger-delete {
				display: block;
			}
		}
	
		// 去掉 el-input 的默认样式
		:deep(.wtwo-input) {
			border: none;
			background: transparent;
			box-shadow: none;
			padding: 0 11px;
			height: auto;
			flex: 1;
			min-width: 0;
			display: flex;
			
			.wtwo-input__wrapper {
				border: none;
				background: transparent;
				box-shadow: none;
				padding: 0;
				flex: 1;
				min-width: 0;
			}
			
			.wtwo-input__inner {
				border: none;
				box-shadow: none;
				padding: 0;
				height: auto;
				color: #606266;
				font-size: 14px;
				flex: 1;
				min-width: 0;
			}
		}

        .courseware-content {
            padding: 0 11px;
            display: flex;
            align-items: center;
            gap: 8px;
            flex: 1;
            min-width: 0;
            max-width: 100%;
            
            .courseware-text {
                flex: 0 1 auto;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                display: block;
            }
            
            .courseware-count {
                flex-shrink: 0;
                color: #409eff;
                white-space: nowrap;
                cursor: pointer;
                transition: color 0.3s ease;
                
                &:hover {
                    color: #66b1ff;
                }
            }
        }

	}
    // 设置上课进度 form-item label 宽度为 100%
    .progress-form-item {
        :deep(.wtwo-form-item__label) {
            width: 100% !important;
            padding-right: 0;
        }
    }
}
</style>