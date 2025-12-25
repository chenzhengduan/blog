<template>
	<div
		class="input-tag-option"
		:class="[{ 'is-selected': isSelected }, customClass]"
	>
		<!-- 左侧图标区域 -->
		<div
			v-if="showIcon"
			class="input-tag-option__icon"
			:class="{
				'input-tag-option__icon--round': iconShape === 'round',
				'input-tag-option__icon--square': iconShape === 'square',
			}"
			:style="iconStyle"
		>
			<slot
				name="icon"
				:option="option"
				:iconText="displayIconText"
				:iconImage="iconImage"
			>
				<!-- 图片图标 -->
				<img
					v-if="iconImage"
					:src="iconImage"
					:alt="displayLabel"
					class="input-tag-option__icon-image"
				/>
				<!-- 文字图标 -->
				<span v-else class="input-tag-option__icon-text">{{
					displayIconText
				}}</span>
			</slot>
		</div>

		<!-- 内容区域 - 完全自定义 -->
		<div class="input-tag-option__content">
			<slot
				name="content"
				:option="option"
				:label="displayLabel"
				:subtitle="displaySubtitle"
			>
				<!-- 默认内容布局 -->
				<div class="input-tag-option__title">{{ displayLabel }}</div>
				<div class="input-tag-option__subtitle" v-if="displaySubtitle">
					{{ displaySubtitle }}
				</div>
			</slot>
		</div>

		<!-- 右侧勾选图标 -->
		<div class="input-tag-option__check" v-if="isSelected">
			<slot name="check" :option="option" :isSelected="isSelected">
				<el-icon><Check /></el-icon>
			</slot>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Check } from '@element-plus/icons-vue'

interface Props {
	option: any
	isSelected?: boolean
	label?: string
	subtitle?: string
	iconColor?: string
	iconImage?: string
	iconShape?: 'round' | 'square'
	showIcon?: boolean
	iconText?: string
	iconSize?: string
	colorPalette?: string[]
	customClass?: string
}

const props = withDefaults(defineProps<Props>(), {
	isSelected: false,
	label: '',
	subtitle: '',
	iconColor: '',
	iconImage: '',
	iconShape: 'square',
	showIcon: true,
	iconText: '',
	iconSize: '20px',
	colorPalette: () => [
		'#409eff',
		'#67c23a',
		'#e6a23c',
		'#f56c6c',
		'#909399',
		'#c71585',
		'#ff6347',
		'#32cd32',
		'#1e90ff',
		'#ff69b4',
		'#ffd700',
		'#8a2be2',
	],
	customClass: '',
})

// 计算显示标签
const displayLabel = computed(() => {
	return props.label || props.option.label || props.option.Name || ''
})

// 计算显示副标题
const displaySubtitle = computed(() => {
	return props.subtitle || ''
})

// 计算图标文本
const displayIconText = computed(() => {
	// 如果外部传入了固定的图标文本，直接使用
	if (props.iconText && props.iconText.trim()) {
		return props.iconText.trim()
	}

	// 否则从显示标签中提取首字母
	const text = displayLabel.value
	return text.charAt(0).toUpperCase()
})

// 计算图标样式
const iconStyle = computed(() => {
	const style: any = {
		width: props.iconSize,
		height: props.iconSize,
	}

	// 如果没有图片，设置背景色
	if (!props.iconImage) {
		if (props.iconColor) {
			style.background = props.iconColor
		} else {
			// 根据文本生成颜色，使用自定义颜色调色板
			const colors = props.colorPalette
			const text = displayLabel.value || ''
			const index = text.charCodeAt(0) % colors.length
			style.background = colors[index]
		}
	}

	return style
})
</script>

<style lang="scss" scoped>
.input-tag-option {
	display: flex;
	align-items: center;
	cursor: pointer;
	transition: background-color 0.2s;

	&:hover {
		background-color: #f5f7fa;
	}

	&.is-selected {
		background-color: #ecf5ff;
	}

	&__icon {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 8px;
		flex-shrink: 0;
		overflow: hidden;
		&--round {
			border-radius: 50%;
		}

		&--square {
			border-radius: 4px;
		}
	}

	&__icon-image {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	&__icon-text {
		color: white;
		font-size: 12px;
		font-weight: 500;
	}

	&__content {
		flex: 1;
		min-width: 0; // 防止文本溢出
	}

	&__title {
		font-size: 14px;
		color: #303133;
		line-height: 20px;

		// 文本溢出处理
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__subtitle {
		font-size: 12px;
		color: #909399;
		line-height: 16px;

		// 文本溢出处理
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__check {
		color: var(--wtwo-color-primary, #409eff);
		margin-left: 8px;
		flex-shrink: 0;
		display: flex;
		align-items: center;
	}
}
</style>
