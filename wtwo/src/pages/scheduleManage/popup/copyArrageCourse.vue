<template>
    <el-drawer
		v-model="drawer"
		direction="rtl"
		size="1190px"
		class="copyArrageCourse"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
        <template #header>
            <el-tabs v-model="activeName" class="demo-tabs">
                <el-tab-pane :label="`按天${CopyOrMove==1?'复制':'移动'}`" name="daily"></el-tab-pane>
                <el-tab-pane :label="`按周${CopyOrMove==1?'复制':'移动'}`" name="weekly"></el-tab-pane>
            </el-tabs>
        </template>
        <div class="drawer-body-wrap" v-loading="loading">
            <div class="pop-copy-course-box">
                <div class="pop-arrange-course">
                    <el-scrollbar>
                        <div class="drawer-filter-box">
                            <div class="drawer-filter-box-item" v-if="activeName=='daily'">
                                <el-date-picker v-model="condition.StartDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" :clearable="false"
                                    class="w-100%! search-date-wrap" :prefix-icon="customPrefix"></el-date-picker>
                            </div>
                            <div class="drawer-filter-box-item" v-else>
                                <!-- [第]ww[周]  -->
                                <el-date-picker
                                    v-model="condition.weekRange"
                                    type="week"
                                    :format="`YYYY-MM-DD [至] [${dayjs(condition.weekRange).add(6, 'day').format('MM-DD')}]`"
                                    value-format="YYYY-MM-DD"
                                    placeholder="请选择"
                                    :clearable="false"
                                    class="w-100%! search-date-wrap"
                                    :prefix-icon="customPrefix"
                                ></el-date-picker>
                            </div>
                            <div class="drawer-filter-box-item">
                                <input-tag
                                    :label="transToConfigDescript('上课班级')"
                                    placeholder="请选择或搜索"
                                    :isLine="true"
                                    v-model:value="condition.ClassName"
                                    :multiple="false"
                                    :searchable="true"
                                    :api-config="classApiConfig"
                                    :selected="condition.classSelected"
                                    @choose="selectClass"
                                    @change="handleClassChange"
                                >
                                    <template #btn-icon>
                                        <el-icon size="20px">
                                            <svg aria-hidden="true">
                                                <use
                                                    xlink:href="#w2-xuanban"
                                                ></use>
                                            </svg>
                                        </el-icon>
                                    </template>
                                    <template #option="{ option, isSelected }">
                                        <input-tag-option
                                            :option="option"
                                            :isSelected="isSelected"
                                            :label="option.Name"
                                            iconColor="#FF8F1F"
                                            iconShape="square"
                                            iconText="班"
                                        >
                                            <template #content="{ option, label }">
                                                <div class="option-content">
                                                    <span class="option-title ellipsis-single" :title="label">{{ label }}</span>
                                                    <el-tag v-if="option.IsFinished === 1" type="info" size="small" class="ml-8px">已结业</el-tag>
                                                </div>
                                            </template>
                                        </input-tag-option>
                                    </template>
                                    <!-- 自定义 footer -->
                                    <template #dropdown-footer>
                                        <span class="switch-wrap">
                                            <el-switch
                                                v-model="finishedClass"
                                                :active-value="-1"
                                                :inactive-value="0"
                                                size="small"
                                            ></el-switch>
                                            <span class="switch-title"
                                                >包含结业{{transToConfigDescript('班级')}}</span
                                            >
                                        </span>
                                    </template>
                                </input-tag>
                            </div>
                            <div class="drawer-filter-box-item">
                                <!-- 学员 -->
                                <input-tag
                                    :label="transToConfigDescript('上课学员')"
                                    placeholder="请选择或搜索"
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
                            <div class="drawer-filter-box-item">
                                <input-tag
                                    :label="transToConfigDescript('上课课程')"
                                    placeholder="请选择或搜索"
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
                                    <!-- 自定义 footer -->
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
                            <template v-if="expandedMore">
                                <div class="drawer-filter-box-item">
                                    <el-select
                                        v-model="condition.timeType"
                                        placeholder="不限"
                                        clearable
                                        collapse-tags
                                        collapse-tags-tooltip
                                        filterable
                                        multiple
                                    >
                                    <template #prefix>
                                            <p class="search-input-label">{{transToConfigDescript('上课时段')}}</p>
                                        </template>
                                        <el-option :value="0" label="上午"></el-option>
                                        <el-option :value="1" label="下午"></el-option>
                                        <el-option :value="2" label="晚上"></el-option>
                                    </el-select>
                                </div>
                                <div class="drawer-filter-box-item" v-if="CopyOrMove!=1">
                                    <el-select
                                        v-model="condition.FinishType"
                                        placeholder="不限"
                                        clearable
                                        collapse-tags
                                        collapse-tags-tooltip
                                        filterable
                                        multiple
                                    >
                                        <template #prefix>
                                            <p class="search-input-label">{{transToConfigDescript('上课状态')}}</p>
                                        </template>
                                        <el-option
                                            :value="1"
                                            :label="transToConfigDescript('已上课')"
                                        ></el-option>
                                        <el-option
                                            :value="0"
                                            :label="transToConfigDescript('未上课')"
                                        ></el-option>
                                    </el-select>
                                </div>
                                <div class="drawer-filter-box-item">
                                    <el-select
                                        v-model="condition.ShiftTypeList"
                                        placeholder="不限"
                                        collapse-tags
                                        collapse-tags-tooltip
                                        multiple
                                        clearable
                                    >
                                        <template #prefix>
                                            <p class="search-input-label">教学形式</p>
                                        </template>
                                        <el-option
                                            :value="0"
                                            label="集体班"
                                        ></el-option>
                                        <el-option
                                            :value="1"
                                            label="一对一"
                                        ></el-option>
                                        <el-option
                                            :value="2"
                                            label="一对多"
                                        ></el-option>
                                    </el-select>
                                </div>
                                <div class="drawer-filter-box-item">
                                    <el-select
                                        v-model="condition.GradeID"
                                        placeholder="不限"
                                        filterable
                                        clearable
                                        :empty-values="[EMPTYGUID]"
                                        :value-on-clear="EMPTYGUID"
                                    >
                                        <template #prefix>
                                            <p class="search-input-label">{{transToConfigDescript('课程年级')}}</p>
                                        </template>
                                        <el-option
                                            v-for="item in SHIFT_GRADE"
                                            :value="item.ID"
                                            :label="item.Name"
                                            :key="item.ID"
                                        ></el-option>
                                    </el-select>
                                </div>
                                <div class="drawer-filter-box-item">
                                    <el-select
                                        v-model="condition.SubjectID"
                                        placeholder="不限"
                                        filterable
                                        clearable
                                        :empty-values="[EMPTYGUID]"
                                        :value-on-clear="EMPTYGUID"
                                    >
                                        <template #prefix>
                                            <p class="search-input-label">{{transToConfigDescript('课程科目')}}</p>
                                        </template>
                                        <el-option
                                            v-for="item in SUBJECT"
                                            :value="item.ID"
                                            :label="item.Name"
                                            :key="item.ID"
                                        ></el-option>
                                    </el-select>
                                </div>
                                <div class="drawer-filter-box-item">
                                    <el-select
                                        v-model="condition.CategoryID"
                                        placeholder="不限"
                                        filterable
                                        clearable
                                        :empty-values="[EMPTYGUID]"
                                        :value-on-clear="EMPTYGUID"
                                    >
                                        <template #prefix>
                                            <p class="search-input-label">{{transToConfigDescript('课程类型')}}</p>
                                        </template>
                                        <el-option
                                            v-for="item in SHIFT_CAT"
                                            :value="item.ID"
                                            :label="item.Name"
                                            :key="item.ID"
                                        ></el-option>
                                    </el-select>
                                </div>
                            </template>
                            
                            <div class="drawer-filter-box-item flex-end" :class="{'w-[100%]!':expandedMore&&CopyOrMove==1}">
                                <el-link
                                    type="primary"
                                    underline="never"
                                    @click.stop="showMore"
                                    class="mr-12px"
                                    >{{ expandedMore ? '收起' : '展开' }}&nbsp;
                                    <el-icon
                                        ><ArrowUp v-if="expandedMore" /><ArrowDown
                                            v-else
                                    /></el-icon>
                                </el-link>
                                <el-button type="primary" @click="getFirstPage">查询</el-button>
                                <el-button @click="handleReset">重置</el-button>
                            </div>
                        </div>
                        <div class="table-wrap scroll-box mt-12px" ref="tableContainerRef" v-loading="loading" element-loading-target=".table-wrap">
                            <el-table
                                :data="list"
                                ref="customTable"
                                width="100%"
                                :max-height="`calc(-180px + 100vh)`"
                                @selection-change="handleSelectionChange"
                            >
                                <template #empty>
                                    <el-empty
                                        :image="globalData.emptyPng"
                                        description="暂无数据"
                                        :image-size="100"
                                    ></el-empty>
                                </template>
                                <el-table-column
                                    fixed="left"
                                    type="selection"
                                    width="30"
                                    key="selection"
                                    :selectable="selectable"
                                ></el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课日期')"
                                    prop="StartTime"
                                    key="StartTime1"
                                    width="150"
                                >
                                    <template #default="scope">
                                        {{ dayjs(scope.row.StartTime).format('YYYY-MM-DD') }}[{{ dayjs(scope.row.StartTime).format('dddd') }}]
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课时间')"
                                    prop="StartTime"
                                    key="StartTime2"
                                    width="100"
                                >
                                    <template #default="scope">
                                        <span v-if="scope.row.Unit!=3||IsOpenShiftForDay">{{ dayjs(scope.row.StartTime).format('HH:mm') }}-{{ dayjs(scope.row.EndTime).format('HH:mm') }}</span>
                                        <span v-if="scope.row.Unit==3&&!IsOpenShiftForDay"></span>
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课班级/学员')"
                                    prop="ClassName"
                                    key="ClassName"
                                    min-width="200"
                                    show-overflow-tooltip
                                >
                                    <template #default="scope">
                                        <div class="flex-center" >
                                            <div class="ellipsis-single block" :title="scope.row.ClassName">
                                                <span>{{ scope.row.ClassName }}</span>
                                                <span v-if="scope.row.IsApprovaling">【{{ scope.row.IsApprovaling==1?'修改申请中':'删除申请中' }}】</span>
                                            </div>
                                            <el-tag
                                                v-if="scope.row.CourseMethod==30||(scope.row.CourseMethod==10&&scope.row.IsSubscribeCourse)"
                                                class="ml-4px px-2px! h-18px!"
                                                type="warning"
                                                size="small"
                                                :hit="true"
                                                >约</el-tag
                                            >
                                            <el-tag
                                                v-if="scope.row.ClassType==2"
                                                class="ml-4px px-2px! h-18px!"
                                                type="warning"
                                                size="small"
                                                :hit="true"
                                                >补</el-tag
                                            >
                                            <el-tag
                                                v-if="scope.row.ClassType==3"
                                                class="ml-4px px-2px! h-18px!"
                                                type="warning"
                                                size="small"
                                                :hit="true"
                                                >试</el-tag
                                            >
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课课程')"
                                    prop="ShiftName"
                                    key="ShiftName"
                                    min-width="140"
                                >
                                    <template #default="scope">
                                        <div class="flex-center" >
                                            <span
                                                class="ellipsis-single block"
                                                :title="scope.row.ShiftName"
                                                :class="{'color-[#a8abb2]!':scope.row.IsApprovaling,'cursor-not-allowed':scope.row.IsApprovaling}"
                                                >{{ scope.row.ShiftName }}</span
                                            >
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('任课老师')"
                                    prop="TeacherName"
                                    key="TeacherName"
                                    width="100"
                                    show-overflow-tooltip
                                ></el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课教室')"
                                    prop="ClassroomName"
                                    key="ClassroomName"
                                    width="100"
                                    show-overflow-tooltip
                                ></el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课状态')"
                                    prop="Finished"
                                    key="Finished"
                                    width="80"
                                >
                                    <template #default="scope">
                                        <el-tag
                                            :type="
                                                scope.row.Finished == 1
                                                    ? 'success'
                                                    : scope.row.Finished == 0
                                                    ? 'primary'
                                                    : 'info'
                                            "
                                            effect="light"
                                            size="small"
                                            >{{ scope.row.FinishedName }}</el-tag
                                        >
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    :label="transToConfigDescript('上课方式')"
                                    prop="CourseType"
                                    key="CourseType"
                                    width="80px"
                                >
                                    <template #default="scope">{{
                                        scope.row.CourseType == 2 ? '线上课' : '线下课'
                                    }}</template>
                                </el-table-column>
                            </el-table>
                            <div
                                class="pageination-wrapper flex-end m-t-10px"
                                v-if="list.length > 0"
                            >
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
                    </el-scrollbar>
                </div>
                <div class="pop-copy-list">
                    <div class="pop-copy-list-header">
                        <div class="pop-copy-list-title">{{CopyOrMove==1?'复制':'移动'}}到{{ activeName=='daily'?'天':'周' }}</div>
                        <div class="flex-center">
                            <multiple-dates-picker
                                v-if="activeName=='daily'"
                                v-model="dateList"
                            />
                            <el-date-picker
                                v-else
                                v-model="weekRange"
                                type="week"
                                :format="`YYYY-MM-DD [至] [${dayjs(weekRange).add(6, 'day').format('MM-DD')}]`"
                                value-format="YYYY-MM-DD"
                                placeholder="请选择"
                                :clearable="false"
                                class="w-100%!"
                            ></el-date-picker>
                        </div>
                        <div class="text-12px color-[#999] pt-6px line-height-18px">
                            <template v-if="activeName=='weekly'">
                                1.左侧已选的排课“{{CopyOrMove==1?'复制':'移动'}}到”目标日期中，原日期的排课{{CopyOrMove==1?'会继续保留':'不再保留'}}。2.周与周之间，只能按星期{{CopyOrMove==1?'平行复制':'平移。'}}
                            </template>
                            <template v-else>
                                左侧已选的排课{{CopyOrMove==1?'复制':'移动'}}到目标日期中，原日期的排课{{CopyOrMove==1?'会继续保留':'不再保留'}}
                            </template>
                        </div>
                    </div>
                    <div class="pop-copy-list-preview">
                        <el-scrollbar>
                            <el-collapse expand-icon-position="left">
                                <el-collapse-item v-for="(item) in previewList" :name="item.Date" :icon="CaretRight">
                                    <template #title>
                                        <div class="flex-between mr-10px">
                                            <div>{{ `${dayjs(item.Date).format('YYYY-MM-DD')} [${dayjs(item.Date).format('dddd')}]` }}</div>
                                            <div class="color-#999 text-12px">{{CopyOrMove==1?'复制':'移入'}}{{ item.CourseList.length }}</div>
                                        </div>
                                    </template>
                                    <div v-for="course in item.CourseList" class="flex course-preview-item">
                                        <div class="w-[90px] ">{{ dayjs(course.StartTime).format('YYYY-MM-DD') }}</div>
                                        <div class="w-[90px] " v-if="course.Unit!=3||IsOpenShiftForDay">{{ dayjs(course.StartTime).format('HH:mm') }}-{{ dayjs(course.EndTime).format('HH:mm') }}</div>
                                        <div class="w-[90px] " v-if="course.Unit==3&&!IsOpenShiftForDay">-</div>
                                        <div class="w-[170px] ellipsis-single block" :title="course.ClassName">{{ course.ClassName }}</div>
                                    </div>
                                    <div v-if="item.CourseList.length==0" class="ml-8px color-#999 py-8px">没有{{CopyOrMove==1?'复制':'移入'}}的排课！</div>
                                </el-collapse-item>
                            </el-collapse>
                        </el-scrollbar>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
			<div class="wtwo-drawer-footer flex-between">
				<div class="flex-center">
					<el-checkbox :model-value="checkConflict" :true-value="1" :false-value="0" :disabled="loading||!NewCourse_IngoreCourseConflict" @click="changeCheckConflict">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
				</div>
				<div class="flex-center">
					<el-button @click="close" :disabled="loading">取消</el-button>
					<el-button type="primary" @click="submit" :disabled="loading">确认{{CopyOrMove==1?'复制':'移动'}}</el-button>
				</div>
			</div>
		</template>
        <choose-class ref="chooseClassRef"></choose-class>
        <choose-student ref="chooseStudentRef"></choose-student>
        <choose-course ref="chooseCourseRef"></choose-course>
        <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
        <ConflictPrompt ref="conflictPromptRef" />
    </el-drawer>
</template>

<script setup lang="ts">
import { getStudentDic, queryClass, queryShift } from '@/api';
import { ApiConfig } from '@/components/common/input-tag/input-tag';
import { useConfigs, useCurrentCampuses } from '@/store';
import { useDictFieldsStore } from '@/store/dict';
import { assignPage, checkPageIndex, dateShortcuts, IPageModel, weekDiff } from '@/utils';
import dayjs from 'dayjs';
import { cloneDeep } from 'lodash';
import { storeToRefs } from 'pinia';
import { computed, nextTick, ref, watch } from 'vue';
import { ArrowUp, ArrowDown, CaretRight } from '@element-plus/icons-vue';
import { getCurrentInstance } from 'vue';
import { copyOrMoveCourse, queryCourseNew } from '@/api/arrange';
import MultipleDatesPicker from '../components/multiple-dates-picker.vue';
import ConflictPrompt from './conflictPrompt.vue';      
import { transToConfigDescript } from '@/utils/filters/filters';

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})
const configs = computed(() => {
	return useConfigs().configs
})
const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)
const SHIFT_GRADE = computed(() => {
    return dictFields.value('SHIFT_GRADE')
})
const SUBJECT = computed(() => {
    return dictFields.value('SUBJECT')
})
const SHIFT_CAT = computed(() => {
    return dictFields.value('SHIFT_CAT')
})
const EMPTYGUID = '00000000-0000-0000-0000-000000000000'

const drawer = ref(false)
const activeName = ref('daily')
const CopyOrMove=ref(1)//1为复制 2为移动
const checkConflict=ref(1)
const loading=ref(false)
const selectable = (row: any) => row.IsApprovaling!=1&&row.IsApprovaling!=2

const page = ref({
	TotalCount: 0, //总条数
	PageSize: 10, //每页条数
	PageIndex: 1, //第几页
	PageCount: 1, //总页数
} as IPageModel)
const defaultCondition: any = {
	Query: '', // 查询条件，支持模糊查询
	ShiftID: '00000000-0000-0000-0000-000000000000', // 课程ID
	ShiftName: '', // 课程名称
	ClassID: '00000000-0000-0000-0000-000000000000', // 班级ID
	ClassName: '', // 班级名称
	MasterIDList: [], // 学管师ID
	CourseFlag: '', //
	StudentID: '00000000-0000-0000-0000-000000000000', // 学员ID
	HeadMasterUserID: '00000000-0000-0000-0000-000000000000', // 班主任
	IsContainFinished: 0, // 0:未结业；1:结业
	ShiftTypeList: [], // 不传=不限,0集体班课程，1一对一课程，2一对多课程，多个逗号分隔
	CourseNumbers: 0, // 补课课次
	TeacherType: -1, // 老师类型,0=全职，1=兼职，2=专职,-1=不限
	CourseType: 0, // 上课方式，1=线下课，2=在线课
	FinishType: [], // 上课状态：-1不限，0未上课，1已上课，2已取消，数据库中的状态为：0未上课，1已上课，2已取消，多个以,分割
	ClassLabelIDList: [], // 班级标签ID
	ShowField: '', // 周、月视图列表显示字段 班级,老师,学员...
	CampusIDList: [], // 校区ID,多个用逗号分隔
	GradeID: '00000000-0000-0000-0000-000000000000', // 课程年级筛选
	SubjectID: '00000000-0000-0000-0000-000000000000', // 课程科目筛选
	CategoryID: '00000000-0000-0000-0000-000000000000', // 课程类型筛选
	ClassroomIDList: [], // 教室ID,多个用逗号分隔
	StartDate: dayjs(new Date()).format('YYYY-MM-DD'), // 排课开始时间
	EndDate: '', // 排课结束时间
	TeacherIDList: [], // 任课老师筛选
	AssistantTeacherIDList: [], // 助教老师筛选
	Forenoon: 1, // 上午
	Afternoon: 1, // 下午
	Nightnoon: 1, // 晚上
	Year: 0, // 年份
	TermID: '00000000-0000-0000-0000-000000000000', // 期段ID
	ClassTypeID: '00000000-0000-0000-0000-000000000000', // 班型ID
	IncludeFull: 0, // 包含人数已满的排课（0包含,-1不包含）
	Weekdays: '', // 按星期筛选
	DialogType: 0, // 查询场景，0其他；1跟班补课查询排课，2调课查询排课
	TryStatus: 0, // 1=处理不需要试听班级的排课
	UsePlatform: 0, // 判断是否微信端调用该接口 微信调用需加上课程手机端不显示的过滤条件,1=需要，0=不需要
	ExportColumn: ['ShiftName','ClassName','TeacherName','ClassroomName'], // 选择列，逗号分隔
	Download: 0, // 0=查询，1=导出
	IsTotal: 0, // 是否合计,0=不是，1=是
	IsSubscribeCourse: -1, // 开放预约状态：-1不限，0未开放，1已开放
	// 筛选参数
	teacherList: [], // 任课老师列表
	studentName: '', // 学员名称
    timeType: [], // 时段类型
	shiftSelected: [], // 上课课程选中项数组
	studentSelected: [], // 学员选中项数组
	classSelected: [], // 班级选中项数组
    weekRange: dayjs(weekDiff(0)[0]).format('YYYY-MM-DD'), // 周区间
}
const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const expandedMore = ref(false)
// 日期相关
const customPrefix = {
    render() {
        return activeName.value=='daily'?transToConfigDescript('上课日期'):'周区间'
    },
}
const list = ref([] as any)
const dateList = ref([])
const weekRange = ref('')
const previewList = ref([] as any)

const IsOpenShiftForDay=computed(()=>{ // 0按月 1按天
	return configs.value.IsOpenShiftForDay==1
})
const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限

function showMore(){
    expandedMore.value = !expandedMore.value
}

const multipleSelection = ref([]);
function handleSelectionChange(val: any) {
    multipleSelection.value = val;
}

// 上课课程相关
const finished = ref(1)
const finishedClass = ref(0)

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
		condition.value.ShiftID = EMPTYGUID
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
			}
		})
}

const classApiConfig = computed(
	(): ApiConfig => ({
		apiFunction: queryClass,
		params: {
			pageSize: 10,
			pageIndex: 1,
			campus: currentCampus.value,
			finished: finishedClass.value,
			isFlag:true
		},
		searchParam: 'query',
		debounceTime: 300,
	})
)

function handleClassChange(val: any) {
	if (val.length) {
		condition.value.ClassID = val[0].ID
		condition.value.ClassName = val[0].Name
		condition.value.classSelected = val
	} else {
		condition.value.ClassID = EMPTYGUID
		condition.value.classSelected = []
	}
}

const chooseClassRef = ref()
function selectClass() {
	chooseClassRef.value
		?.open({
			multi: false,
			required: true,
            showClassStatusFilter:true
		})
		.then((res:any) => {
			if (res.data) {
				condition.value.ClassID = res.data.ID
				condition.value.ClassName = res.data.Name
				condition.value.classSelected = [res.data]
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
		condition.value.StudentID = EMPTYGUID
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

function changeCheckConflict() {
	if(checkConflict.value==1){
		ElMessageBox.confirm(transToConfigDescript("不检查上课冲突，可能会生成相同时间的排课，确认不检查吗？"), '提示', {
			confirmButtonText: '确认不检查',
			cancelButtonText: '取消',
		}).then(()=>{
			checkConflict.value=0
		})
	}else{
		checkConflict.value=1
	}
}

function buildPreviewForDates(targetDates:any[], opts?:{ matchWeekday?: boolean }){
    const dateKeyToCourses:any = {}
    const selected = multipleSelection.value || []
    if(!selected.length || !targetDates.length){
        previewList.value = []
        return
    }
    targetDates.forEach((target:any)=>{
        const dateKey = dayjs(target).format('YYYY-MM-DD')
        const listForDate:any[] = []
        selected.forEach((src:any)=>{
            if(opts?.matchWeekday){
                const srcDow = dayjs(src.StartTime).day()
                const targetDow = dayjs(dateKey).day()
                if(srcDow !== targetDow){
                    return
                }
            }
            const start = dayjs(src.StartTime)
            const end = dayjs(src.EndTime)
            const newStart = dayjs(dateKey + ' ' + start.format('HH:mm:ss'))
            const newEnd = dayjs(dateKey + ' ' + end.format('HH:mm:ss'))
            const cloned = { ...src, StartTime: newStart.toISOString(), EndTime: newEnd.toISOString() }
            listForDate.push(cloned)
        })
        dateKeyToCourses[dateKey] = listForDate
    })
    const preview:any[] = Object.keys(dateKeyToCourses).sort().map((key)=>({
        Date: key,
        CourseList: dateKeyToCourses[key]
    }))
    previewList.value = preview
}

watch(()=>dateList.value, (val:any)=>{
    if(activeName.value==='daily'){
        buildPreviewForDates(val || [])
    }
},{deep:true})

watch(()=>weekRange.value, (nval:any)=>{
    if(activeName.value==='weekly'){
        let val=[nval,dayjs(nval).add(6, 'day').format('YYYY-MM-DD')]
        if(Array.isArray(val) && val.length===2){
            const start = dayjs(val[0]).startOf('day')
            const end = dayjs(val[1]).startOf('day')
            const days:number = end.diff(start, 'day')
            const allDates:string[] = []
            for(let i=0;i<=days;i++){
                allDates.push(start.add(i,'day').format('YYYY-MM-DD'))
            }
            buildPreviewForDates(allDates, { matchWeekday: true })
        }else{
            previewList.value = []
        }
    }
})

watch(()=>multipleSelection.value, ()=>{
    if(activeName.value==='daily'){
        buildPreviewForDates(dateList.value || [])
    }else{
        const val = [weekRange.value,dayjs(weekRange.value).add(6, 'day').format('YYYY-MM-DD')]
        if(Array.isArray(val) && val.length===2){
            const start = dayjs(val[0]).startOf('day')
            const end = dayjs(val[1]).startOf('day')
            const days:number = end.diff(start, 'day')
            const allDates:string[] = []
            for(let i=0;i<=days;i++){
                allDates.push(start.add(i,'day').format('YYYY-MM-DD'))
            }
            buildPreviewForDates(allDates, { matchWeekday: true })
        }else{
            previewList.value = []
        }
    }
},{deep:true})

watch(()=>activeName.value,()=>{
	nextTick(()=>{
		getFirstPage()
	})
})
//翻页查询
function handleSizeChange(val: number) {
	page.value.PageSize = val
	page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
	funcQuery()
}
function handleCurrentChange(val: number) {
	page.value.PageIndex = val
	funcQuery()
}
function getFirstPage() {
	page.value.PageIndex = 1
	funcQuery()
}

function handleReset() {
	Object.assign(condition.value, cloneDeep(defaultCondition))
	getFirstPage()
}
function funcQuery() {
    list.value = []
	let params: any = {}
	
	// 从 condition 中排除需要特殊处理的参数
	const { teacherList, studentName, timeType, shiftSelected,studentSelected,classSelected,weekRange, ...conditionParams } = condition.value
	
	Object.assign(params, conditionParams,page.value)
	
    if(activeName.value=='daily'){
        params.StartDate = dayjs(params.StartDate).format('YYYY-MM-DD')
	    params.EndDate = dayjs(params.StartDate).format('YYYY-MM-DD')
    }else if(condition.value.weekRange){
        params.StartDate = dayjs(condition.value.weekRange).format('YYYY-MM-DD')
        params.EndDate = dayjs(condition.value.weekRange).add(6, 'day').format('YYYY-MM-DD')
    }
	// 处理时段筛选
	params.Forenoon =
		condition.value.timeType.length === 0 || condition.value.timeType.includes(0) ? 1 : 0
	params.Afternoon =
		condition.value.timeType.length === 0 || condition.value.timeType.includes(1) ? 1 : 0
	params.Nightnoon =
		condition.value.timeType.length === 0 || condition.value.timeType.includes(2) ? 1 : 0

	// 处理老师ID列表
	params.TeacherIDList = condition.value.teacherList.map((item: any) => item.ID)
	params.FinishType = CopyOrMove.value==1?'0':(condition.value.FinishType.length
		? condition.value.FinishType.join(',')
		: '0,1')
	params.ShiftTypeList = condition.value.ShiftTypeList||[]
	params.CampusIDList = currentCampus.value
		? currentCampus.value.split(',')
		: []
	
	
	loading.value = true
	queryCourseNew(params)
		.then((res: any) => {
			list.value = res.Data.List || []
			assignPage(page.value, res.Data)
		})
		.finally(() => {
			loading.value = false
		})
}

const conflictPromptRef=ref<InstanceType<typeof ConflictPrompt> | null>(null)
function submit(){
    let handlerName=CopyOrMove.value==1?"复制":"移动";
    if(multipleSelection.value.length===0){
        ElMessage.warning("请选择要"+handlerName+transToConfigDescript("的上课记录。"));
        return;
    }
    if(activeName.value==='daily'){
        if(dateList.value.length==0){
            ElMessage.warning("请选择要"+handlerName+"到的目标日期。");
            return;
        }
    }else{
        if(weekRange.value===''){
            ElMessage.warning("请选择要"+handlerName+"到的目标周。");
            return;
        }
    }
    ElMessageBox.confirm(`确定将选择的${multipleSelection.value.length}节排课，${handlerName}到选中的${activeName.value=='daily'?'日期':'周'}吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
    }).then(()=>{
        let courseList:any=[]
        
        // 构建courseList数据
        if(activeName.value === 'daily') {
            // 按天模式：每个选中的课程对应每个目标日期
            multipleSelection.value.forEach((course: any) => {
                dateList.value.forEach((targetDate: string) => {
                    courseList.push({
                        CourseID: course.ID, // 原排课ID
                        DateList: [{
                            StartTime: targetDate+' '+dayjs(course.StartTime).format('HH:mm:ss'),
                            EndTime: targetDate+' '+dayjs(course.EndTime).format('HH:mm:ss')
                        }] // 目标日期列表
                    })
                })
            })
        } else {
            // 按周模式：每个选中的课程对应周内匹配的星期几
            const weekStart = dayjs(weekRange.value)
            const weekEnd = weekStart.add(6, 'day')
            const weekDates: string[] = []
            
            // 生成一周内的所有日期
            for(let i = 0; i <= 6; i++) {
                weekDates.push(weekStart.add(i, 'day').format('YYYY-MM-DD'))
            }
            
            multipleSelection.value.forEach((course: any) => {
                const courseWeekday = dayjs(course.StartTime).day() // 获取课程是星期几
                const targetDates: string[] = []
                
                // 找到周内对应星期几的日期
                weekDates.forEach((date: string) => {
                    if(dayjs(date).day() === courseWeekday) {
                        targetDates.push(date)
                    }
                })
                
                if(targetDates.length > 0) {
                    courseList.push({
                        CourseID: course.ID, // 原排课ID
                        DateList: targetDates.map((date: string) => ({
                            StartTime: date+' '+dayjs(course.StartTime).format('HH:mm:ss'),
                            EndTime: date+' '+dayjs(course.EndTime).format('HH:mm:ss')
                        })) // 目标日期列表
                    })
                }
            })
        }
        loading.value=true
        copyOrMoveCourse({
            CheckConflict:checkConflict.value,
            CopyOrMove:CopyOrMove.value,
            CourseList:courseList
        }).then((res:any)=>{
            ElMessage.success("排课"+handlerName+"成功。");
            _resolve && _resolve()
            close()
        }).catch((error)=>{
            if(error.ErrorCode==409){
                conflictPromptRef.value?.open({info:error.Data}).then(()=>{}).catch((back:any)=>{
                    if(back&&back.closeCurrent){
                        drawer.value=false
                    }
                })
            }
        }).finally(()=>{
            loading.value=false
        })
    })
}

let _resolve: any = null,
    _reject: any = null

/** 对外暴露一个open方法 */
function open(params: any) {
    CopyOrMove.value = params.CopyOrMove || 1
    getFirstPage()
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		drawer.value = true
	})
}

function close() {
    drawer.value = false
    nextTick(()=>{
        activeName.value = 'daily'
        CopyOrMove.value = 1
        condition.value=cloneDeep(defaultCondition)
        multipleSelection.value=[]
        previewList.value=[]
        weekRange.value=''
        dateList.value=[]
        list.value=[]
        loading.value=false
        checkConflict.value=1
    })
    

    _reject && _reject()
}

defineExpose({
    open,
})  
</script>

<style lang="scss">
.copyArrageCourse{
    .wtwo-drawer__header{
        padding-bottom: 0!important;
        padding-top: 0!important;
    }
    .drawer-body-wrap{
        overflow-y: auto;
        width: 1190px;
    }
    .wtwo-tabs__item{
        height: 46px;
        line-height: 46px;
        margin: 0;
    }
    .wtwo-tabs--top .wtwo-tabs__nav, .wtwo-tabs--bottom .wtwo-tabs__nav{
        height: 46px;
    }
    .pop-copy-course-box{
        display: flex;
        height: 100%;
        width: 100%;
        overflow: hidden;
        flex-shrink: 0;
    }
    .pop-arrange-course{
        border-right: 1px solid#E4E7ED;
        height: 100%;
        width: 789px;
        flex-shrink: 0;
        
    }
    .pop-copy-list{
        height: 100%;
        width: 400px;
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        .pop-copy-list-header{
            padding: 12px 16px;
        }
        .pop-copy-list-title{
            padding-bottom: 8px;
            font-weight: 600;
            color: #333;
        }
        .pop-copy-list-preview{
            padding: 0 16px;
            flex: 1;
            .wtwo-collapse{
                border: none;
                .wtwo-collapse-item__header{
                    height: 34px;
                    line-height: 34px;
                    min-height: 34px;
                    background-color: #F5F7FA;
                    border: 1px solid #EBEEF5;
                    padding-left: 8px;
                }
                .wtwo-collapse-item__content{
                    padding-bottom: 0;
                }
                .wtwo-collapse-item__wrap{
                    border-bottom: none;
                }
            }
            .course-preview-item{
                flex-shrink: 0;
                color: #606266;
                line-height: 40px;
                height: 40px;
                // padding-left: 8px;
                border-bottom: 1px solid #EBEEF5;
                font-size:13px;
                &:last-child{
                    border-bottom: none;
                }
                >div{
                    padding-left: 8px;
                }
            }
        }
    }
}
</style>