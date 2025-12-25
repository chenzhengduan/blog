<template>
  <div class="wtwo-multiple-date-picker">
	<div v-if="props.title" ref="titleRef" class="wtwo-multiple-date-picker-title">{{ props.title }}</div>
    <el-date-picker
      v-model="innerValue"
      type="dates"
      style="opacity: 0;position: relative;z-index: 1;"
      placeholder="请选择"
      value-format="YYYY-MM-DD"
      class="w-[100%]!"
	  :clearable="props.clearable"
    />
    <div class="wtwo-dates-input-wrap" :style="{paddingLeft: titlePaddingLeft}">
		<el-icon v-if="!props.title" color="var(--wtwo-text-color-placeholder)" class="mr-10px"><Calendar /></el-icon>
		<div v-if="innerValue && innerValue.length" class="flex-center">
			<div class="mr-10px">{{ dayjs(innerValue[0]).format('YYYY-MM-DD') }}</div>
			<div class="mr-10px">至</div>
			<div class="mr-12px">{{ dayjs(innerValue[innerValue.length - 1]).format('YYYY-MM-DD') }}</div>
			<div class="text-12px" :class="props.maxDays > 0 && innerValue.length >= props.maxDays ? 'color-[#f56c6c]' : 'color-[#909399]'">
				已选{{ innerValue.length }}天
				<span v-if="props.maxDays > 0 && innerValue.length >= props.maxDays" class="ml-5px">(已达上限)</span>
			</div>
		</div>
		<div v-else class="dates-input-placeholder" style="color: #a8abb2;line-height: 30px;">请选择</div>
    </div>
    <el-icon v-if="props.clearable && innerValue && innerValue.length" @click="handleClear" class="wtwo-close">
      <CircleClose />
    </el-icon>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, nextTick, watch, onMounted, onUpdated } from 'vue'
import { dayjs, ElMessage } from 'element-plus'
import { CircleClose, Calendar } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  modelValue: string[],
  clearable?: boolean,
  title?: string,
  maxDays?: number
}>(), {
  clearable: true,
  maxDays: 0
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
  (e: 'cleared'): void
}>()

const titleRef = ref<HTMLElement>()
const titleWidth = ref(0)

// 计算标题宽度
const calculateTitleWidth = () => {
  if (titleRef.value && props.title) {
    titleWidth.value = titleRef.value.offsetWidth
    console.log('标题宽度:', titleWidth.value)
  } else {
    titleWidth.value = 0
  }
}

// 监听 title 变化
watch(() => props.title, () => {
  nextTick(() => {
    calculateTitleWidth()
  })
}, { immediate: true })

// 组件挂载后计算
onMounted(() => {
  nextTick(() => {
    calculateTitleWidth()
  })
})

// 组件更新后重新计算
onUpdated(() => {
  nextTick(() => {
    calculateTitleWidth()
  })
})

// 计算 paddingLeft
const titlePaddingLeft = computed(() => {
  return props.title ? `${titleWidth.value + 15 + 12}px` : '10px'
})

const innerValue = computed<string[]>({
  get: () => props.modelValue || [],
  set: (val) => {
    const newValue = Array.isArray(val) ? val : []
    // 检查是否超过最大天数限制（maxDays > 0 时才进行限制）
    if (props.maxDays > 0 && newValue.length > props.maxDays) {
      // 如果超过限制，只保留前 maxDays 个日期
      const limitedValue = newValue.slice(0, props.maxDays)
      emit('update:modelValue', limitedValue)
      // 显示提示信息
      ElMessage.warning(`最多只能选择${props.maxDays}天`)
    } else {
      emit('update:modelValue', newValue)
    }
  }
})

function handleClear() {
  emit('update:modelValue', [])
  emit('cleared')
}
</script>

<style lang="scss">

.wtwo-multiple-date-picker{
	width: 100%;
	position: relative;
	.wtwo-multiple-date-picker-title{
		line-height: 32px;
		position: absolute;
		left: 12px;
		top: 0;
		color: #606266;
		font-size: 14px;
	}
	.wtwo-date-editor-picker,
	.wtwo-input__inner{
		cursor: pointer!important;
	}
	.wtwo-dates-input-wrap{
		z-index: 0;
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		border: 1px solid #dcdfe6;
		border-radius: 4px;
		height: 32px;
		padding: 0 12px 0 10px;
		line-height: 30px;
		color: var(--wtwo-text-color-regular);
		display: flex;
		align-items: center;
	}
	.wtwo-close{
		visibility: hidden;
		position: absolute;
		z-index: 2;
		right: 10px;
		top: 50%;
		transform: translateY(-50%);
		cursor: pointer;
		color: #909399;
	}
	&:hover{
		.wtwo-close{
			visibility: visible;
		}
		
	}
}
.wtwo-form-item.is-error{
	.wtwo-dates-input-wrap{
		border-color: var(--wtwo-color-danger);
	}
}
</style>

