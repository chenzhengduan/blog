
<template>
    <div class="search-item" :class="{ 'common-filter-display': isCommonFilter }">
        <div ref="tooltipRef" class="tooltip-wrapper">
        <!-- 教学形式 -->
        <el-select
            v-if="filter.key === 'ShiftTypeList'"
            :model-value="condition.ShiftTypeList"
            @update:model-value="(value) => emit('update:condition', { ...condition, ShiftTypeList: value })"
            placeholder="不限"
            collapse-tags
            collapse-tags-tooltip
            multiple
            clearable
            :disabled="disabled"
            :class="{ 'common-filter-width': isCommonFilter }"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
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

        <!-- 课程年份 -->
        <el-select
            v-else-if="filter.key === 'Year'"
            :model-value="condition.Year"
            @update:model-value="(value) => emit('update:condition', { ...condition, Year: value })"
            placeholder="不限"
            filterable
            clearable
            :disabled="disabled"
            :empty-values="[0]"
            :value-on-clear="0"
            :class="{ 'common-filter-width': isCommonFilter }"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in years"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 课程期段 -->
        <el-select
            v-else-if="filter.key === 'TermID'"
            :model-value="condition.TermID"
            @update:model-value="(value) => emit('update:condition', { ...condition, TermID: value })"
            placeholder="不限"
            filterable
            clearable
            :disabled="disabled"
            :empty-values="[EMPTYGUID]"
            :value-on-clear="EMPTYGUID"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in CLASS_TERM"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 课程年级 -->
        <el-select
            v-else-if="filter.key === 'GradeID'"
            :model-value="condition.GradeID"
            @update:model-value="(value) => emit('update:condition', { ...condition, GradeID: value })"
            placeholder="不限"
            filterable
            clearable
            :disabled="disabled"
            :empty-values="[EMPTYGUID]"
            :value-on-clear="EMPTYGUID"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in SHIFT_GRADE"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 课程科目 -->
        <el-select
            v-else-if="filter.key === 'SubjectID'"
            :model-value="condition.SubjectID"
            @update:model-value="(value) => emit('update:condition', { ...condition, SubjectID: value })"
            placeholder="不限"
            filterable
            clearable
            :disabled="disabled"
            :empty-values="[EMPTYGUID]"
            :value-on-clear="EMPTYGUID"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in SUBJECT"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 课程类型 -->
        <el-select
            v-else-if="filter.key === 'CategoryID'"
            :model-value="condition.CategoryID"
            @update:model-value="(value) => emit('update:condition', { ...condition, CategoryID: value })"
            placeholder="不限"
            filterable
            clearable
            :disabled="disabled"
            :empty-values="[EMPTYGUID]"
            :value-on-clear="EMPTYGUID"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in SHIFT_CAT"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 课程班型 -->
        <el-select
            v-else-if="filter.key === 'ClassTypeID'"
            :model-value="condition.ClassTypeID"
            @update:model-value="(value) => emit('update:condition', { ...condition, ClassTypeID: value })"
            placeholder="不限"
            filterable
            clearable
            :disabled="disabled"
            :empty-values="[EMPTYGUID]"
            :value-on-clear="EMPTYGUID"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in CLASS_TYPE"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 上课课程 -->
        <input-tag
            v-else-if="filter.key === 'ShiftName'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择或搜索"
            :isLine="true"
            v-model:value="condition.ShiftName"
            :multiple="false"
            :searchable="true"
            :api-config="shiftApiConfig"
            :selected="condition.shiftSelected"
            :disabled="disabled"
            @choose="()=> emit('selectCourse')"
            @change="(value: any) => emit('handleShiftChange', value)"
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

        <!-- 结业状态 -->
        <el-select
            v-else-if="filter.key === 'IsContainFinished'"
            :model-value="condition.IsContainFinished"
            @update:model-value="(value) => emit('update:condition', { ...condition, IsContainFinished: value })"
            placeholder="不限"
            clearable
            :disabled="disabled"
            :empty-values="[-1]"
            :value-on-clear="-1"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                :value="0"
                label="未结业"
            ></el-option>
            <el-option
                :value="1"
                label="已结业"
            ></el-option>
        </el-select>

        <!-- 班主任 -->
        <input-tag
            v-else-if="filter.key === 'headerMasterName'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择或搜索"
            :isLine="true"
            v-model:value="condition.headerMasterName"
            :selected="condition.headerMasterSelected"
            :searchable="true"
            :disabled="disabled"
            :api-config="headMasterApiConfig"
            @choose="() => emit('selectHeadMaster')"
            @change="(value: any) => emit('handleHeadMasterChange', value)"
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

        <!-- 任课老师 -->
        <input-tag
            v-else-if="filter.key === 'teacherList'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择"
            :isLine="true"
            :multiple="true"
            :selected="condition.teacherList"
            @change="(selected:any) => {
                emit('update:condition', { ...condition, teacherList: selected })
            }"
            :disabled="disabled"
            @click="() => emit('selectTeacher')"
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

        <!-- 助教 -->
        <input-tag
            v-else-if="filter.key === 'assistantList'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择"
            :isLine="true"
            :multiple="true"
            :selected="condition.assistantList"
            @change="(selected:any) => {
                emit('update:condition', { ...condition, assistantList: selected })
            }"
            :disabled="disabled"
            @click="() => emit('selectAssistant')"
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

        <!-- 学员 -->
        <input-tag
            v-else-if="filter.key === 'studentName'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择或搜索"
            :isLine="true"
            v-model:value="condition.studentName"
            :selected="condition.studentSelected"
            :searchable="true"
            :disabled="disabled"
            :api-config="studentApiConfig"
            @choose="() => emit('selectStudent')"
            @change="(value: any) => emit('handleStudentChange', value)"
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

        <!-- 上课时段 -->
        <el-select
            v-else-if="filter.key === 'timeType'"
            :model-value="condition.timeType"
            @update:model-value="(value) => emit('update:condition', { ...condition, timeType: value })"
            placeholder="不限"
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            multiple
            :disabled="disabled"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option :value="0" label="上午"></el-option>
            <el-option :value="1" label="下午"></el-option>
            <el-option :value="2" label="晚上"></el-option>
        </el-select>

        <!-- 上课时间 -->
        <el-date-picker
            v-else-if="filter.key === 'dateRange'"
            type="daterange"
            unlink-panels
            range-separator="到"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :prefix-icon="customPrefix"
            :shortcuts="dateShortcuts"
            :model-value="condition.dateRange"
            @update:model-value="(value) => emit('update:condition', { ...condition, dateRange: value })"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            class="search-date-wrap twice-width common-filter-width-wide"
            :disabled="disabled"
        >
        </el-date-picker>

        <!-- 开放预约 -->
        <el-select
            v-else-if="filter.key === 'IsSubscribeCourse'"
            :model-value="condition.IsSubscribeCourse"
            @update:model-value="(value) => emit('update:condition', { ...condition, IsSubscribeCourse: value })"
            placeholder="不限"
            clearable
            :disabled="disabled"
            :empty-values="[-1]"
            :value-on-clear="-1"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                :value="0"
                label="不开放"
            ></el-option>
            <el-option
                :value="1"
                label="已开放"
            ></el-option>
        </el-select>

        <!-- 班级 -->
        <input-tag
            v-else-if="filter.key === 'ClassName'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择或搜索"
            :isLine="true"
            v-model:value="condition.ClassName"
            :multiple="false"
            :searchable="true"
            :api-config="classApiConfig"
            :selected="condition.classSelected"
            :disabled="disabled"
            @choose="()=> emit('selectClass')"
            @change="(value: any) => emit('handleClassChange', value)"
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
        <el-input
            v-else-if="filter.key === 'Query'"
            type="text"
            :model-value="condition.Query"
            @update:model-value="(value) => emit('update:condition', { ...condition, Query: value })"
            :placeholder="`请输入${transToConfigDescript('上课班级')}`"
            :disabled="disabled"
        >
            <template #prefix>
                <el-icon>
                    <Search />
                </el-icon>
            </template>
        </el-input>

        <!-- 班级标签 -->
        <el-select
            v-else-if="filter.key === 'ClassLabelIDList'"
            :model-value="condition.ClassLabelIDList"
            @update:model-value="(value) => emit('update:condition', { ...condition, ClassLabelIDList: value })"
            placeholder="不限"
            collapse-tags
            collapse-tags-tooltip
            filterable
            multiple
            clearable
            :disabled="disabled"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in CLASS_LABLE"
                :value="item.ID"
                :label="item.Name"
                :key="item.ID"
            ></el-option>
        </el-select>

        <!-- 任课老师类型 -->
        <el-select
            v-else-if="filter.key === 'TeacherType'"
            :model-value="condition.TeacherType"
            @update:model-value="(value) => emit('update:condition', { ...condition, TeacherType: value })"
            placeholder="不限"
            clearable
            :empty-values="[-1]"
            :value-on-clear="-1"
            :disabled="disabled"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                :value="0"
                label="全职"
            ></el-option>
            <el-option
                :value="1"
                label="兼职"
            ></el-option>
            <el-option
                :value="2"
                label="专职"
            ></el-option>
        </el-select>

        <!-- 上课方式 -->
        <el-select
            v-else-if="filter.key === 'CourseType'"
            :model-value="condition.CourseType"
            @update:model-value="(value) => emit('update:condition', { ...condition, CourseType: value })"
            placeholder="不限"
            clearable
            :empty-values="[0]"
            :value-on-clear="0"
            :disabled="disabled"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                :value="2"
                label="线上课"
            ></el-option>
            <el-option
                :value="1"
                label="线下课"
            ></el-option>
        </el-select>

        <!-- 上课教室 -->
        <el-select
            v-else-if="filter.key === 'ClassroomIDList'"
            :model-value="condition.ClassroomIDList"
            @update:model-value="(value) => emit('update:condition', { ...condition, ClassroomIDList: value })"
            placeholder="不限"
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            multiple
            :disabled="disabled"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                v-for="item in classroomListComputed"
                :key="item.ID"
                :value="item.ID"
                :label="item.Name"
            >{{ item.Name }}</el-option>
        </el-select>

        <!-- 上课状态 -->
        <el-select
            v-else-if="filter.key === 'FinishType'"
            :model-value="condition.FinishType"
            @update:model-value="(value) => emit('update:condition', { ...condition, FinishType: value })"
            placeholder="不限"
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            multiple
            :disabled="disabled"
        >
            <template #prefix>
                <p class="search-input-label">
                    {{ transToConfigDescript(filter.label) }}
                </p>
            </template>
            <el-option
                :value="1"
                :label="transToConfigDescript('已上课')"
            ></el-option>
            <el-option
                :value="0"
                :label="transToConfigDescript('未上课')"
            ></el-option>
            <el-option
                :value="2"
                label="已取消"
            ></el-option>
        </el-select>

        <!-- 学管师 -->
        <input-tag
            v-else-if="filter.key === 'masterList'"
            :label="transToConfigDescript(filter.label)"
            placeholder="请选择"
            :isLine="true"
            :multiple="true"
            :selected="condition.masterList"
            @change="(selected:any) => {
                emit('update:condition', { ...condition, masterList: selected })
            }"
            :disabled="disabled"
            @click="() => emit('selectMaster')"
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

        <!-- 删除按钮 -->
        <el-tooltip
            v-if="isCommonFilter && showRemoveButton"
            effect="dark"
            content="取消常用条件"
            placement="top"
            
        >
            <el-icon
                class="remove-common-icon"
                @click="$emit('remove', filter.key)"
                size="18px"
                :style="{visibility: filter.key == 'Query'?'hidden':'visible'}"
            >
                <svg aria-hidden="true">
                    <use xlink:href="#w2-yichu"></use>
                </svg>
            </el-icon>
        </el-tooltip>

                <!-- 常用筛选标志 -->
                <div
                    v-if="showCommonFlag"
                    class="common-flag"
                    :class="{
                        'is-common': isCommonFilterFlag || filter.key === 'Query',
                    }"
                >
                    <el-tooltip
                        effect="dark"
                        :content="filter.key === 'Query' ? '此条件始终为常用条件' : '设为常用条件，常用条件会显示在外面，最多可设置7个'"
                        placement="top"
                    >
                        <el-icon
                            size="18px"
                            :color="(isCommonFilterFlag || filter.key === 'Query') ? '#e6a23c' : '#999'"
                            :class="{ 'cursor-not-allowed': filter.key === 'Query' }"
                            @click="filter.key === 'Query' ? undefined : $emit('toggleCommon', filter.key)"
                        >
                            <StarFilled />
                        </el-icon>
                    </el-tooltip>
                </div>
            </div>
        <el-tooltip
            v-if="disabled && disabledTooltip"
            trigger="hover"
            virtual-triggering
            :virtual-ref="tooltipRef"
            effect="dark"
            :content="disabledTooltip"
            placement="top"
        ></el-tooltip>
    </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch, onMounted } from 'vue'
import { Search, StarFilled } from '@element-plus/icons-vue'
import { useConfigs, useCurrentCampuses, useYears } from '@/store'
import { useDictFieldsStore } from '@/store/dict'
import { storeToRefs } from 'pinia'
import { getEmployeeDic, getStudentDic, queryClass, queryShift } from '@/api/comm'
import type { ApiConfig } from '@/components/common/input-tag/input-tag.d.ts'
import { dateShortcuts } from '@/utils'
import { transToConfigDescript } from '@/utils/filters/filters'

const tooltipRef = ref()

onMounted(() => {
})

interface FilterItem {
    key: string
    label: string
}

interface Props {
    filter: FilterItem
    condition: any
    disabled?: boolean
    isCommonFilter?: boolean
    showRemoveButton?: boolean
    showCommonFlag?: boolean
    isCommonFilterFlag?: boolean
    disabledTooltip?: string
    // 基础数据 - 从store获取
    classroomList?: any[]
    // 其他数据 - shiftSelected 已移动到 condition 中
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false,
    isCommonFilter: false,
    showRemoveButton: false,
    showCommonFlag: false,
    isCommonFilterFlag: false,
    disabledTooltip: ''
})

const emit = defineEmits<{
    remove: [key: string]
    selectCourse: [course?: any]
    handleShiftChange: [value: any]
    handleStudentChange: [value: any]
    handleHeadMasterChange: [value: any]
    selectHeadMaster: [master?: any]
    selectTeacher: [teacher?: any]
    selectAssistant: [assistant?: any]
    selectStudent: [student?: any]
    selectMaster: [master?: any]
    selectClass: [value?: any]
    handleClassChange: [value: any]
    toggleCommon: [key: string]
    'update:condition': [condition: any]
}>()

// 从store获取基础数据
const configs = computed(() => {
    return useConfigs().configs
})

const currentCampus = computed(() => {
    return useCurrentCampuses().campusList
})

// 添加 computed 属性确保响应性
const classroomListComputed = computed(() => {
    return props.classroomList || []
})

const years = computed(() => {
    return useYears().years
})

const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)

const CLASS_TERM = computed(() => {
    return dictFields.value('CLASS_TERM')
})

const SHIFT_GRADE = computed(() => {
    return dictFields.value('SHIFT_GRADE')
})

const SUBJECT = computed(() => {
    return dictFields.value('SUBJECT')
})

const SHIFT_CAT = computed(() => {
    return dictFields.value('SHIFT_CAT')
})

const CLASS_TYPE = computed(() => {
    return dictFields.value('CLASS_TYPE')
})

const CLASS_LABLE = computed(() => {
    return dictFields.value('CLASS_LABLE')
})

const EMPTYGUID = '00000000-0000-0000-0000-000000000000'


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

const headMasterApiConfig = computed(
    (): ApiConfig => ({
        apiFunction: getEmployeeDic,
        params: {
            pageSize: 10,
            pageIndex: 1,
            Status:1
        },
        searchParam: 'query',
        debounceTime: 300,
    })
)

// 日期相关
const customPrefix = {
    render() {
        return '上课时间'
    },
}

</script>

<style scoped lang="scss">
.tooltip-wrapper{
    display: inline-flex;
    align-items: center;
    min-height: 32px;
}
</style>