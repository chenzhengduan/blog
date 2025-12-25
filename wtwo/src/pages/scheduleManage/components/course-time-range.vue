<template>
	<div class="flex-center course-time-range-picker-container">
		<div class="course-time-range-picker-container-box flex-center">
			<!-- <el-time-picker
				v-model="timeRange"
				is-range
				range-separator="-"
				start-placeholder="开始"
				end-placeholder="结束"
				value-format="HH:mm"
				format="HH:mm"
				class="w-100%! course-time-range-picker"
				:class="{'with-title': !!props.title}"
				:disabled-hours="disabledHours"
				:disabled-minutes="disabledMinutes"
				@change="handleTimeRangeChange"
				:prefix-icon="customPrefix"
				:clearable="props.clearable"
				:unlink-panels="true"
			/> -->
			<span v-if="props.title" class="ml-11px mr-10px">{{ props.title }}</span>
			<el-time-picker 
				v-model="startTime" 
				placeholder="开始" 
				value-format="HH:mm"
				format="HH:mm" 
				:disabled-hours="disabledHours"
				:disabled-minutes="disabledMinutes"
				:clearable="false"
				class="course-time-range-picker ml-5px"
				:style="{width: props.pickerWidth}"
				:disabled="props.disabled"
			/>
			<span class="mx-3px">-</span>
			<el-time-picker 
				v-model="endTime" 
				placeholder="结束" 
				value-format="HH:mm"
				format="HH:mm" 
				:disabled-hours="disabledHours"
				:disabled-minutes="disabledMinutes"
				:clearable="false"
				class="course-time-range-picker inner-padding-0"
				:style="{width: props.pickerWidth}"
				:disabled="props.disabled"
			/>
			<el-icon v-if="props.clearable&&(startTime||endTime)&&!props.disabled" class="ml-2px cursor-pointer" color="#909399" @click="clearTime"><CircleClose /></el-icon>
			<div class="fixed-duration">{{ startTime && endTime && isEndAfterStartSameDay && duration ? formatDuration(duration, true) : '-分钟' }}</div>
		</div>
		<el-popover ref="popoverRef" v-if="showCommonTime&&props.campusId&&!props.disabled" placement="bottom" :width="280" trigger="click" popper-class="wtwo-course-time-popover" transition="none" :hide-after="0" prefix-icon="''">
			<template #reference>
				<el-icon class="ml-4px cursor-pointer" size="18px">
					<svg aria-hidden="true">
						<use xlink:href="#w2-changyongshiduan"></use>
					</svg>
				</el-icon>
			</template>
			<div class="wtwo-course-time-popover-content">
				<div class="wtwo-course-time-popover-content-title">选择常用时段</div>
				<div class="wtwo-course-time-popover-content-list">
					<div v-for="item in courseTimeList" class="wtwo-course-time-popover-content-list-item" @click="selectCourseTime(item)">
						<div class="wtwo-course-time-popover-content-list-item-name" :title="item.Name">{{ item.Name }}</div>
						<div class="wtwo-course-time-popover-content-list-item-time">{{ item.StartTime }}-{{ item.EndTime }}</div>
						<div class="wtwo-course-time-popover-content-list-item-minutes">{{ item.Duration ? formatDuration(item.Duration, true) : '-' }}</div>
					</div>
				</div>
			</div>
		</el-popover>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed, shallowRef, h } from 'vue'
import { calculateDuration, formatDuration } from '@/utils/timeUtils'
import { useCourseTime } from '@/store/courseTime'
import { CircleClose } from '@element-plus/icons-vue'

interface Props {
	modelValue: [string, string]|[]
	campusId?: string
	minCourseTime?: string
	maxCourseTime?: string
	showCommonTime?: boolean
	title?: string
	clearable?: boolean
	pickerWidth?:string
	disabled?: boolean
}
const props = withDefaults(defineProps<Props>(), {
    showCommonTime: true,
    clearable: true,
	pickerWidth:'50px',
	disabled: false
})
interface Emits {
	(e: 'update:modelValue', value: [string, string]|[]): void
	(e: 'change', row: any): void
}
const customPrefix = shallowRef({
  render() {
    return h('p', props.title);
  },
})


const emit = defineEmits<Emits>()

const startTime=ref('')
const endTime=ref('')
const timeRange = ref(props.modelValue)
const duration = ref(0)
const courseTimeList = ref([] as any)
const popoverRef = ref()

// 结束时间是否在同一天大于开始时间（用于模板判断显示）
const isEndAfterStartSameDay = computed(() => {
    if (!startTime.value || !endTime.value) return false
    const parseToMinutes = (t: string) => {
        const [h, m] = t.split(':').map(Number)
        return h * 60 + m
    }
    const s = parseToMinutes(startTime.value)
    const e = parseToMinutes(endTime.value)
    return e > s
})

// 使用store管理上课时间数据
const courseTimeStore = useCourseTime()

// 监听外部传入的值变化
watch(() => props.modelValue, (newValue) => {
	if (newValue && Array.isArray(newValue)) {
		startTime.value = newValue[0] || ''
		endTime.value = newValue[1] || ''
		if (startTime.value && endTime.value) {
			duration.value = calculateDuration([startTime.value, endTime.value])
		} else {
			duration.value = 0
		}
	} else {
		startTime.value = ''
		endTime.value = ''
		duration.value = 0
	}
}, { immediate: true })

// 监听内部 startTime / endTime 变化，向外部发送更新
watch([startTime, endTime], ([newStart, newEnd]) => {
	if (newStart && newEnd) {
		const range: [string, string] = [newStart, newEnd]
		duration.value = calculateDuration(range)
		emit('update:modelValue', range)
		emit('change', { TimeRange: range, Duration: duration.value })
	}
})

// 获取上课时间数据（从store获取，自动缓存）
const queryClassTimes = async () => {
	if(!props.campusId) return
	try {
		const data = await courseTimeStore.getCourseTimes(props.campusId)
		courseTimeList.value = data
	} catch (error) {
		console.error('获取上课时间失败:', error)
		courseTimeList.value = []
	}
}
    
// 选择常用时段
const selectCourseTime = (item: any) => {
	if (props.minCourseTime && new Date(`1970-01-01 ${item.StartTime}`).getTime() < new Date(`1970-01-01 ${props.minCourseTime}`).getTime()) {
		ElMessage.error(`开始时间不能早于${props.minCourseTime}`)
		return
	}
	if (props.maxCourseTime && new Date(`1970-01-01 ${item.EndTime}`).getTime() > new Date(`1970-01-01 ${props.maxCourseTime}`).getTime()) {
		ElMessage.error(`结束时间不能晚于${props.maxCourseTime}`)
		return
	}
	startTime.value = item.StartTime
	endTime.value = item.EndTime
	if (startTime.value && endTime.value) {
		duration.value = calculateDuration([startTime.value, endTime.value])
		emit('update:modelValue', [startTime.value, endTime.value])
		emit('change', { TimeRange: [startTime.value, endTime.value], Duration: duration.value })
	}
	// 关闭popover
	popoverRef.value?.hide()
}

function clearTime(){
	startTime.value = ''
	endTime.value = ''
	emit('update:modelValue', [])
	emit('change', { TimeRange: [], Duration: 0 })
}

// 处理时间范围变化
const handleTimeRangeChange = (value: []) => {
	if (value && Array.isArray(value)) {
		duration.value = calculateDuration(value)
		emit('change', { TimeRange: value, Duration: duration.value })
	}
}

// 禁用小时
const disabledHours = () => {
	const disabled = []
	
	if (props.minCourseTime || props.maxCourseTime) {
		const minHour = props.minCourseTime ? new Date(`1970-01-01 ${props.minCourseTime}`).getHours() : 0
		const maxHour = props.maxCourseTime ? new Date(`1970-01-01 ${props.maxCourseTime}`).getHours() : 23
		
		for (let i = 0; i < 24; i++) {
			if (i < minHour || i > maxHour) {
				disabled.push(i)
			}
		}
	}
	
	return disabled
}

// 禁用分钟
const disabledMinutes = (hour: number) => {
	const disabled = []
	
	if (props.minCourseTime || props.maxCourseTime) {
		const minHour = props.minCourseTime ? new Date(`1970-01-01 ${props.minCourseTime}`).getHours() : 0
		const minMinute = props.minCourseTime ? new Date(`1970-01-01 ${props.minCourseTime}`).getMinutes() : 0
		const maxHour = props.maxCourseTime ? new Date(`1970-01-01 ${props.maxCourseTime}`).getHours() : 23
		const maxMinute = props.maxCourseTime ? new Date(`1970-01-01 ${props.maxCourseTime}`).getMinutes() : 59
		
		for (let i = 0; i < 60; i++) {
			// 如果是最小时间的小时，禁用小于最小分钟的时间
			if (hour === minHour && i < minMinute) {
				disabled.push(i)
			}
			// 如果是最大时间的小时，禁用大于最大分钟的时间
			if (hour === maxHour && i > maxMinute) {
				disabled.push(i)
			}
		}
	}
	
	return disabled
}

onMounted(() => {
	queryClassTimes()
})

// 当父组件异步传入或切换 campusId 时，触发重新获取
watch(
    () => props.campusId,
    (newCampusId, oldCampusId) => {
        if (!newCampusId) {
            courseTimeList.value = []
            return
        }
        if (newCampusId !== oldCampusId) {
            queryClassTimes()
        }
    }
)
</script>

<style lang="scss">
.course-time-range-picker-container{
	position: relative;
	line-height: normal;
	width: 100%;
	.course-time-range-picker-container-box{
		position: relative;
		flex: 1;
		background: #fff;
		box-shadow: 0 0 0 1px var(--wtwo-border-color) inset;
		border-radius: 4px;
			&:focus,
			&:focus-within{
				box-shadow: 0 0 0 1px var(--wtwo-color-primary) inset;
				outline: none;
			}
		.fixed-duration{
			position: absolute;
			right: 6px;
			top:0;
			font-size: 12px;
			color: #909399;
			line-height: 32px;
		}
	}
	.course-time-range-picker{
		//padding-right: 60px;

		.wtwo-input__wrapper{
			box-shadow: none!important;
			background-color: transparent!important;
			padding: 1px 0;
			.wtwo-input__inner{
				text-align: center;
				background-color: rgb(244, 244, 245);
				height: 24px;
				border-radius: 4px;
			}
		}
		&.inner-padding-0{
			.wtwo-input__wrapper{
				padding-right: 0;
			}
		}
		.wtwo-input__prefix-inner{
			display: none;
		}
		
		&.with-title{
			.wtwo-input__prefix-inner{
				display: block;
				width: auto;
    			flex-shrink: 0;
				line-height: 32px;
				font-style: normal;
				color: #606266;
			}
		}
		
	}
}
.is-error{
	.course-time-range-picker-container-box{
		box-shadow: 0 0 0 1px var(--wtwo-color-danger) inset;
	}
}

.wtwo-course-time-popover{
	padding: 8px!important;
	.wtwo-course-time-popover-content{
		.wtwo-course-time-popover-content-title{
			color: #606266;
			border-bottom: 1px solid #E4E7ED;
			font-weight: 500;
			padding: 2px 6px 4px 6px;
			margin-bottom: 4px;
		}
		.wtwo-course-time-popover-content-list{
			max-height: 280px;
			overflow-y: auto;
			.wtwo-course-time-popover-content-list-item{
				display: flex;
				align-items: center;
				padding: 6px;
				cursor: pointer;
				border-radius: 3px;
				&:hover{
					background-color: rgba(0,0,0,0.04);
				}
				.wtwo-course-time-popover-content-list-item-name{
					overflow: hidden;
					text-overflow: ellipsis;
					white-space: nowrap;
					margin-right: 4px;
					width: 90px;
				}
				.wtwo-course-time-popover-content-list-item-minutes{	
					margin-left: 10px;
					font-size: 12px;
					color: #909399;
				}
			}
		}
	}
}
</style>