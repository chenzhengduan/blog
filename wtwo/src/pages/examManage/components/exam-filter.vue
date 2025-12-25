<template>
	<div class="search-item exam-search-item" :class="{ 'common-filter-display': isCommonFilter }">
		<!-- 考试年级 -->
		<el-select v-if="filter.key === 'gradeIds'" :model-value="condition.gradeIds"
			@update:model-value="(value) => emit('update:condition', { ...condition, gradeIds: value })"
			placeholder="不限" filterable clearable multiple collapse-tags collapse-tags-tooltip :disabled="disabled">
			<template #prefix>
				<p class="search-input-label">
					{{ filter.label }}
				</p>
			</template>
			<el-option v-for="item in SHIFT_GRADE" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
		</el-select>

		<!-- 考试科目 -->
		<el-select v-else-if="filter.key === 'subjectIds'" :model-value="condition.subjectIds"
			@update:model-value="(value) => emit('update:condition', { ...condition, subjectIds: value })"
			placeholder="不限" filterable clearable multiple collapse-tags collapse-tags-tooltip :disabled="disabled">
			<template #prefix>
				<p class="search-input-label">
					{{ filter.label }}
				</p>
			</template>
			<el-option v-for="item in SUBJECT" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
		</el-select>

		<!-- 考试类型 -->
		<el-select v-else-if="filter.key === 'examTypes'" :model-value="condition.examTypes"
			@update:model-value="(value) => emit('update:condition', { ...condition, examTypes: value })"
			placeholder="不限" filterable clearable multiple collapse-tags collapse-tags-tooltip :disabled="disabled">
			<template #prefix>
				<p class="search-input-label">
					{{ filter.label }}
				</p>
			</template>
			<el-option v-for="item in EXAM_TYPE" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
		</el-select>

		<!-- 考试模式 -->
		<el-select v-else-if="filter.key === 'examModes'" :model-value="condition.examModes"
			@update:model-value="(value) => emit('update:condition', { ...condition, examModes: value })"
			placeholder="不限" filterable clearable :disabled="disabled" :empty-values="[0]" :value-on-clear="0">
			<template #prefix>
				<p class="search-input-label">
					{{ filter.label }}
				</p>
			</template>
			<el-option :value="0" label="全部"></el-option>
			<el-option :value="1" label="线下考试"></el-option>
			<!-- 一期先不显示线上选项 -->
			<!-- <el-option :value="2" label="线上考试"></el-option> -->
		</el-select>

		<!-- 适用课程 -->
		<input-tag v-else-if="filter.key === 'courseIds'" :label="filter.label" placeholder="请选择" :isLine="true"
			:multiple="true" :selected="condition.courseSelected" :disabled="disabled"
			@change="(selected: any) => emit('handleCourseChange', selected)" @click="() => emit('selectCourse')">
			<template #btn-icon>
				<el-icon size="18px">
					<svg aria-hidden="true">
						<use xlink:href="#w2-xuanke"></use>
					</svg>
				</el-icon>
			</template>
		</input-tag>
		<!-- 班级 -->
		<input-tag v-else-if="filter.key === 'classIds'" :label="filter.label" placeholder="请选择" :isLine="true"
			:multiple="true" :selected="condition.classSelected" :disabled="disabled"
			@change="(selected: any) => emit('handleClassChange', selected)" @click="() => emit('selectClass')">
			<template #btn-icon>
				<el-icon size="18px">
					<svg aria-hidden="true">
						<use xlink:href="#w2-xuanban"></use>
					</svg>
				</el-icon>
			</template>
		</input-tag>
		<!-- 学员 -->
		<input-tag v-else-if="filter.key === 'studentIds'" :label="filter.label" placeholder="请选择" :isLine="true"
			:multiple="true" :selected="condition.studentSelected" :disabled="disabled"
			@change="(selected: any) => emit('handleStudentChange', selected)" @click="() => emit('selectStudent')">
			<template #btn-icon>
				<el-icon size="18px">
					<svg aria-hidden="true">
						<use xlink:href="#w2-xuanren"></use>
					</svg>
				</el-icon>
			</template>
		</input-tag>

		<!-- 考试项目 -->
		<input-tag v-else-if="filter.key === 'examSubjectIds'" :label="filter.label" placeholder="请选择" :isLine="true"
			:multiple="true" :selected="condition.examItemSelected" :disabled="disabled"
			@change="(selected: any) => emit('handleExamItemChange', selected)" @click="() => emit('selectExamItem')">
			<template #text>
				<div>选择</div>
			</template>
		</input-tag>

		<!-- 考试日期 -->
		<el-date-picker v-else-if="filter.key === 'examDate'"  type="datetimerange" unlink-panels range-separator="到"
			start-placeholder="开始时间" end-placeholder="结束时间" :prefix-icon="customPrefix" :shortcuts="dateShortcuts"
			:model-value="condition.examDate"
			@update:model-value="(value) => emit('update:condition', { ...condition, examDate: value })"
			value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" class="search-date-wrap twice-width common-filter-width-wide" :disabled="disabled"
			placeholder="选择日期">
		</el-date-picker>

		<!-- 成绩公布时间 -->
		<el-date-picker v-else-if="filter.key === 'publicTime'" type="datetimerange" unlink-panels range-separator="到"
			start-placeholder="开始时间" end-placeholder="结束时间" :prefix-icon="customPrefix" :shortcuts="dateShortcuts"
			:model-value="condition.publicTime"
			@update:model-value="(value) => emit('update:condition', { ...condition, publicTime: value })"
			value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss"
			class="search-date-wrap twice-width common-filter-width-wide" :disabled="disabled">
		</el-date-picker>


		<!-- 学员名称 -->
		<el-input v-else-if="filter.key === 'studentName' && filter.type === 'input'" type="text"
			:model-value="condition.studentName"
			@update:model-value="(value) => emit('update:condition', { ...condition, studentName: value })"
			:placeholder="`搜索${filter.label}`" :disabled="disabled">
			<template #prefix>
				<el-icon>
					<Search />
				</el-icon>
			</template>
		</el-input>
		<!-- 考试名称 -->
		<el-input v-else-if="filter.key === 'examName' && filter.type === 'input'" type="text"
			:model-value="condition.examName"
			@update:model-value="(value) => emit('update:condition', { ...condition, examName: value })"
			:placeholder="`搜索${filter.label}`" :disabled="disabled">
			<template #prefix>
				<el-icon>
					<Search />
				</el-icon>
			</template>
		</el-input>

		<!-- 删除按钮 -->
		<el-tooltip v-if="isCommonFilter && showRemoveButton" effect="dark" content="取消常用条件" placement="top">
			<el-icon class="remove-common-icon" @click="$emit('remove', filter.key)" size="18px">
				<svg aria-hidden="true">
					<use xlink:href="#w2-yichu"></use>
				</svg>
			</el-icon>
		</el-tooltip>

		<!-- 常用筛选标志 -->
		<div v-if="showCommonFlag" class="common-flag" :class="{
			'is-common': isCommonFilterFlag,
		}">
			<el-tooltip effect="dark" content="设为常用条件，常用条件会显示在外面，最多可设置4个" placement="top">
				<el-icon size="18px" :color="isCommonFilterFlag ? '#e6a23c' : '#999'"
					@click="$emit('toggleCommon', filter.key)">
					<StarFilled />
				</el-icon>
			</el-tooltip>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { Search, StarFilled } from '@element-plus/icons-vue'
import { useCurrentCampuses } from '@/store'
import { useDictFieldsStore } from '@/store/dict'
import { storeToRefs } from 'pinia'
import { dateShortcuts } from '@/utils'

interface FilterItem {
	key: string
	label: string
	type: string
}

interface Props {
	filter: FilterItem
	condition: any
	disabled?: boolean
	isCommonFilter?: boolean
	showRemoveButton?: boolean
	showCommonFlag?: boolean
	isCommonFilterFlag?: boolean
	// 基础数据 - 从store获取
	classroomList?: any[]
	// 其他数据 - shiftSelected 已移动到 condition 中
}

const props = withDefaults(defineProps<Props>(), {
	disabled: false,
	isCommonFilter: false,
	showRemoveButton: false,
	showCommonFlag: false,
	isCommonFilterFlag: false
})

const emit = defineEmits<{
	remove: [key: string]
	selectCourse: [course?: any]
	handleCourseChange: [value: any]
	selectClass: [classItem?: any]
	handleClassChange: [value: any]
	selectStudent: [student?: any]
	handleStudentChange: [value: any]
	selectExamItem: [examItem?: any]
	handleExamItemChange: [value: any]
	toggleCommon: [key: string]
	'update:condition': [condition: any]
}>()

const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})

const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)

const SHIFT_GRADE = computed(() => {
	return dictFields.value('SHIFT_GRADE')
})

const SUBJECT = computed(() => {
	return dictFields.value('SUBJECT')
})

const EXAM_TYPE = computed(() => {
	return dictFields.value('EXAM_TYPE')
})

const EMPTYGUID = '00000000-0000-0000-0000-000000000000'


// 日期相关
const customPrefix = {
	render() {
		return props.filter.label
	},
}

</script>

<style scoped lang="scss">
.exam-search-item {
	:deep(.wtwo-date-editor .wtwo-range-input) {
		width: 36%;
	}
}
</style>