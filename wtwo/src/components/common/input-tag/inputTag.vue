<template>
	<div class="input-tag-box" ref="inputTagBoxRef">
		<el-popover
			:visible="visible"
			placement="bottom-start"
			:width="popoverWidth"
			:disabled="disabled"
			:popper-options="{
				strategy: 'fixed',
			}"
			popper-class="input-tag-popover"
			@hide="handlePopoverHide"
		>
			<template #reference>
				<div
				class="input-tag"
				:class="{ 'is-focused': isFocused, disabled: props.disabled }"
			>
					<!-- 容器区域 @click="handleBtnClick"-->
					<div
						class="input-tag-container"
						:class="{ 'is-line': isLine !== false }"
						:tabindex="props.disabled ? undefined : tabindex"
						@click="handleContainerClick"
					>
						<div
							v-if="label"
							class="input-tag-pre-container search-input-label"
						>
							{{ label }}
						</div>

						<div class="input-tag-content">
							<span
								v-if="
									selected.length === 0 &&
									placeholder &&
									inputValue == '' &&
									!isComposing
								"
								class="input-tag-placeholder"
								>{{ placeholder }}</span
							>
							<div class="input-tag-item-container">
								<template v-if="!isLine && multiple">
									<div
										class="input-tag-item"
										v-for="(item, index) in selected"
										:key="index"
										:style="{
											'max-width': itemMaxWidth,
										}"
									>
										<div
											class="input-tag-content-define"
											:data="item"
										>	
										   <el-icon v-if="item.status ===0"><Warning /></el-icon>
											{{
												fieldMapping.label
													? (item as any)[fieldMapping.label]
													: ''
											}}
										</div>
										<div
											class="input-tag-close"
											@click.stop="
												!disabled && handleDelete(index)
											"
											v-if="!disabled && showDelete"
										>
											<el-icon class="wtwo-close"
												><Close
											/></el-icon>
										</div>
									</div>
								</template>
								<template
									v-if="isLine && selected.length && multiple"
								>
									<div class="input-tag-line-container">
										<div
											class="input-tag-item"
										:style="{
											'max-width': itemMaxWidth,
										}"
									>
										<div
											class="input-tag-content-define"
											:data="selected[0]"
											:title="fieldMapping.label ? (selected[0] as any)[fieldMapping.label] : ''"
										>
										   <el-icon v-if="selected[0].status ===0"><Warning /></el-icon>
											{{
												fieldMapping.label
													? (selected[0] as any)[
														fieldMapping.label
													  ]
													: ''
											}}
										</div>
										<div
											class="input-tag-close"
											@click.stop="
												!disabled && handleDelete(0)
											"
											v-if="!disabled && showDelete"
										>
											<el-icon class="wtwo-close"
												><Close
											/></el-icon>
										</div>
									</div>
									<el-popover
										v-if="selected.length > 1"
										placement="bottom"
										:trigger="visible ? undefined : 'hover'"
										:visible="visible ? false : undefined"
										popper-class="hidden-items-popover"
										:popper-options="{ strategy: 'fixed' }"
									>
										<template #reference>
											<div class="count-num">
												+ {{ selected.length - 1 }}
											</div>
										</template>
										<template #default>
											<div class="hidden-items-list">
												<div
													v-for="(
														item, index
													) in selected.slice(1)"
													:key="index + 1"
													class="hidden-item"
												>
													<span
														class="hidden-item-name"
													>
														<el-icon v-if="item.status ===0"><Warning /></el-icon>
														{{
															fieldMapping.label
																? (item as any)[
																	fieldMapping.label
																  ]
																: ''
														}}
													</span>
													<div
														class="input-tag-close"
														@click="
															!disabled &&
																handleDelete(
																	index + 1
																)
														"
														v-if="!disabled && showDelete"
													>
														<el-icon
															class="wtwo-close"
															><Close
														/></el-icon>
													</div>
												</div>
											</div>
										</template>
									</el-popover>
									</div>
								</template>
								<el-input
									v-if="searchable"
									v-model="inputValue"
									type="text"
									class="input-tag-search"
									:readonly="!searchable || disabled"
									@input="handleInput"
									@compositionstart="handleCompositionStart"
									@compositionend="handleCompositionEnd"
									@focus="handleFocus"
									@blur="handleBlur"
									@click.stop
								></el-input>
								<el-input
									v-if="!searchable&&!multiple"
									:value="props.selected.length>0?props.selected[0][props.fieldMapping?.label||'Name']:''"
									type="text"
									class="input-tag-search"
									readonly
								></el-input>
							</div>
						</div>
					</div>
					<div
						v-if="showDelete && (multiple ? selected.length != 0 : (searchable ? !!inputValue : selected.length != 0))"
						class="input-tag-clear"
						:class="{
									'is-hidden': multiple
										? selected.length === 0 || props.disabled
										: (searchable ? (selected.length === 0 && !inputValue) : selected.length === 0) ||
										  props.disabled,
								}"
						@click.stop.prevent="handleClearAll"
						@mousedown.stop.prevent="handleClearAll"
					>
						<el-icon class="wtwo-close"><CircleClose /></el-icon>
					</div>
					<!-- 触发按钮 -->
					<div
						v-if="$slots.text"
						class="input-tag-btn input-tag-text"
						@click.stop="handleBtnClick"
					>
						<slot name="text"><div>选择</div></slot>
					</div>
					<!-- 触发按钮 -->
					<div
						v-else-if="!$slots.btn"
						class="input-tag-btn"
						@click.stop="handleBtnClick"
					>
						<slot name="btn-icon">
							<el-icon
								class="mr-5px"
							>
								<ArrowDown />
							</el-icon>
						</slot>
					</div>
				</div>
			</template>
			<template #default>
				<slot
					name="popover-content"
					:inputValue="inputValue"
					:visible="visible"
					:searchResults="searchState.searchResults"
					:isLoading="searchState.isLoading"
					:searchError="searchState.searchError"
					:onOptionSelect="handleOptionSelect"
				>
					<!-- 增强的默认内容 -->
					<div
						class="input-tag-dropdown"
						v-if="props.apiConfig && props.searchable"
					>
						<!-- Header 插槽 -->
						<div
							v-if="$slots['dropdown-header']"
							class="input-tag-dropdown__header"
						>
							<slot name="dropdown-header"></slot>
						</div>

						<!-- 内容区域 -->
						<div
							class="input-tag-dropdown__content"
							:style="{ maxHeight: props.optionConfig.maxHeight }"
						>
							<!-- 加载状态 -->
							<div
								v-if="searchState.isLoading"
								class="input-tag-dropdown__loading"
							>
								<slot name="loading">
									{{ props.optionConfig.loadingText }}
								</slot>
							</div>

							<!-- 错误状态 -->
							<div
								v-else-if="searchState.searchError"
								class="input-tag-dropdown__error"
							>
								{{ searchState.searchError }}
							</div>

							<!-- 搜索结果 -->
							<div
								v-else-if="searchState.searchResults.length > 0"
								class="input-tag-dropdown__options"
							>
								<div
									v-for="(
										option, index
									) in searchState.searchResults"
									:key="option.key || option.value || index"
									class="input-tag-dropdown__option"
									:class="{
										'input-tag-dropdown__option--highlighted':
											index ===
											searchState.highlightedIndex,
										'input-tag-dropdown__option--selected':
											isOptionSelected(option),
									}"
									@click="handleOptionSelect(option)"
									@mouseenter="
										searchState.highlightedIndex = index
									"
								>
									<slot
										name="option"
										:option="option"
										:index="index"
										:isSelected="isOptionSelected(option)"
									>
										<div
											class="input-tag-dropdown__option-content"
										>
											<span
												class="input-tag-dropdown__option-text"
											>
												{{
													option.label ||
													option[
														props.fieldMapping
															.label || 'Name'
													] ||
													''
												}}
											</span>
											<span
												v-if="
													props.multiple &&
													isOptionSelected(option)
												"
												class="input-tag-dropdown__option-check"
											>
												<el-icon><Check /></el-icon>
											</span>
										</div>
									</slot>
								</div>
							</div>

							<!-- 空状态 -->
							<div
								v-else-if="inputValue.trim()"
								class="input-tag-dropdown__empty"
							>
								<slot name="empty">
									<el-empty
										:image="globalData.emptyPng"
										:description="props.optionConfig.emptyText"
										:image-size="80"
										class="py-0!"
									></el-empty>
								</slot>
							</div>
						</div>

						<!-- Footer 插槽 -->
						<div
							v-if="$slots['dropdown-footer']"
							class="input-tag-dropdown__footer"
						>
							<slot name="dropdown-footer"></slot>
						</div>
					</div>

					<!-- 向后兼容：如果没有配置 API，显示原有内容 -->
					<div v-else>未正确配置</div>
				</slot>
			</template>
		</el-popover>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ArrowDown, Close, CircleClose, Check, Warning } from '@element-plus/icons-vue'
import type {
	ApiConfig,
	FieldMapping,
	OptionConfig,
	OptionItem,
	SearchState,
} from '@/components/common/input-tag/input-tag.d.ts'
import { SearchApiService } from '@/components/common/input-tag/search-api-service'

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const props = defineProps({
	selected: {
		type: Array as any,
		default: () => [],
	},
	value: {
		type: String,
		default: '',
	},
	label: {
		type: String,
		default: '',
	},
	placeholder: {
		type: String,
		default: '',
	},
	tabindex: {
		type: String,
		default: '',
	},
	isLine: {
		type: [Boolean, String],
		default: true,
	},
	disabled: {
		type: Boolean,
		default: false,
	},
	searchable: {
		//多选模式的棵树可选在框比较短的时候是换行的，功能已实现了一部分，不过暂时先不要使用，体验不太好
		type: Boolean,
		default: false,
	},
	multiple: {
		type: Boolean,
		default: false,
	},
	// 是否展示清空和删除按钮
	showDelete: {
		type: Boolean,
		default: true,
	},
	// 新增的 API 配置
	apiConfig: {
		type: Object as () => ApiConfig | undefined,
		default: undefined,
		validator: (value: ApiConfig | undefined) => {
			if (!value) return true
			return typeof value.apiFunction === 'function'
		},
	},
	// 单选模式下是否允许未选中状态保留输入内容
	allowUnselectedInput: {
		type: Boolean,
		default: false,
	},
	// 字段映射配置
	fieldMapping: {
		type: Object as () => FieldMapping,
		default: () => ({
			label: 'Name',
			value: 'ID',
			key: 'ID',
		}),
	},
	// 选项配置
	optionConfig: {
		type: Object as () => OptionConfig,
		default: () => ({
			emptyText: '暂无数据',
			loadingText: '加载中...',
			maxHeight: '200px',
		}),
	},
})

const emit = defineEmits<{
	(e: 'choose'): void
	(e: 'click'):void
	(e: 'update:value', value: string): void
	(e: 'change', selected: any[]): void
	(e: 'search', keyword: string): void
	(e: 'api-success', data: any[]): void
	(e: 'api-error', error: any): void
	(e: 'option-select', option: any): void
}>()

const itemMaxWidth = ref('auto')
const visible = ref(false)
const popoverWidth = ref(200)
const isFocused = ref(false)
// 双向绑定的内部值
const inputValue = ref(props.value || '')

// 标记是否刚刚完成选择操作（用于防止单选模式下重新打开下拉面板）
const justSelected = ref(false)

// 标记是否是程序化更新（用于区分程序设置和用户输入）
const programmaticUpdate = ref(false)

// 标记输入法是否正在输入
const isComposing = ref(false)

// 搜索相关状态
const searchState = ref<SearchState>({
	searchResults: [],
	isLoading: false,
	searchError: null,
	highlightedIndex: -1,
})

// API 搜索服务实例
let searchApiService: SearchApiService | null = null

// 点击外部隐藏 popover
const handleClickOutside = (event: MouseEvent) => {
	// 如果下拉面板本身就是关闭状态，不需要处理
	if (!visible.value) {
		return
	}

	const target = event.target as Element
	if (!target) return

	// 特殊处理：如果点击的是清空按钮，不要阻止事件
	if (target.closest('.input-tag-clear')) {
		// console.log('点击了清空按钮，不阻止事件')
		return
	}

	// 检查是否点击在当前组件内部
	if (inputTagBoxRef.value && inputTagBoxRef.value.contains(target)) {
		return
	}

	// 检查是否点击在任何 popover 内部
	// 使用更宽泛的选择器来捕获所有可能的 popover 元素
	const popoverSelectors = [
		'.el-popover',
		'.input-tag-popover',
		'.hidden-items-popover',
		'.el-popper',
		'[role="tooltip"]',
		'[data-popper-placement]',
	]

	for (const selector of popoverSelectors) {
		const elements = document.querySelectorAll(selector)
		for (const element of elements) {
			if (element.contains(target)) {
				return
			}
		}
	}

	// 如果都不是，则关闭下拉面板
	visible.value = false
}

// 监听 props.value 变化，同步到内部值
watch(
	() => props.value,
	(newValue) => {
		programmaticUpdate.value = true
		inputValue.value = newValue || ''
		// 延迟重置标志，确保 handleInput 能检测到
		nextTick(() => {
			setTimeout(() => {
				programmaticUpdate.value = false
			}, 0)
		})
	}
)

const inputTagBoxRef = ref()

// 参考 el-select 的宽度计算策略
const calculatePopoverWidth = () => {
	if (!inputTagBoxRef.value) return

	// 直接获取宽度，就像 el-select 一样
	const width = inputTagBoxRef.value.offsetWidth
	if (width > 0) {
		popoverWidth.value = width
	}
}

// 处理输入事件，向父组件发送更新
const handleInput = (value: string) => {
	if (props.disabled) return
	emit('update:value', value)
	emit('search', value)

	// 检查是否刚刚完成选择或程序化更新
	const justCompletedSelection = !props.multiple && justSelected.value
	const isProgrammaticUpdate = programmaticUpdate.value

	if (justCompletedSelection) {
		justSelected.value = false
	}

	// 在单选模式下，如果用户修改输入框内容，处理选中项
	// 但如果刚刚完成选择或是程序化更新，不要清空
	if (
		!props.multiple &&
		props.selected.length > 0 &&
		!justCompletedSelection &&
		!isProgrammaticUpdate
	) {
		// 如果开启了 allowUnselectedInput，检查输入值是否与选中项匹配
		if (props.allowUnselectedInput) {
			// 获取当前选中项的显示值
			const selectedItem:any = props.selected[0]
			const selectedDisplayValue = selectedItem[props.fieldMapping?.label || 'Name'] || ''
			
			// 如果输入值与选中项不匹配，清空选中项
			if (value !== selectedDisplayValue) {
				props.selected.splice(0, props.selected.length)
				emit('change', props.selected)
			}
		} else {
			// 如果没有开启 allowUnselectedInput，直接清空选择的值（原有逻辑）
			props.selected.splice(0, props.selected.length)
			emit('change', props.selected)
		}
	}

	// 当有输入内容时显示 popover，没有内容时隐藏（除非刚刚完成选择或程序化更新）
	// 注意：在输入法输入过程中，即使有内容也不显示 popover
	if (!justCompletedSelection && !isProgrammaticUpdate && !isComposing.value) {
		visible.value = value.length > 0
	}

	// 如果配置了 API 且开启了搜索功能，执行搜索（除非是程序化更新）
	if (!isProgrammaticUpdate) {
		if (props.apiConfig && props.searchable) {
			if (value.trim()) {
				// 有搜索内容时执行搜索
				performApiSearch(value.trim())
			} else {
				// 没有搜索内容时清空搜索结果并隐藏下拉面板
				searchState.value.searchResults = []
				searchState.value.isLoading = false
				searchState.value.searchError = null
				// 在单选模式下，清空输入时隐藏下拉面板
				if (!props.multiple) {
					visible.value = false
				}
			}
		} else {
			// 没有配置 API 时清空搜索结果
			searchState.value.searchResults = []
			searchState.value.isLoading = false
			searchState.value.searchError = null
			// 在单选模式下，清空输入时隐藏下拉面板
			if (!props.multiple) {
				visible.value = false
			}
		}
	}
}

function handleFocus() {
	if (props.disabled) return
	// 设置焦点状态
	isFocused.value = true

	// 在单选模式下，如果刚刚完成选择，不要重新打开下拉面板
	if (!props.multiple && justSelected.value) {
		justSelected.value = false
		return
	}

	// 可搜索的情况下，当前下拉面板是关闭，再次点输入框，此时输入框如果有值，需要打开下拉面板
	if (props.searchable && !visible.value && inputValue.value.length > 0) {
		visible.value = true
		// 如果配置了 API，重新搜索当前输入的内容
		if (props.apiConfig && inputValue.value.trim()) {
			performApiSearch(inputValue.value.trim())
		}
	}
}

function handleBlur() {
	// 失去焦点时重置状态
	isFocused.value = false
}

// 处理输入法开始输入事件
function handleCompositionStart() {
	isComposing.value = true
}

// 处理输入法结束输入事件
function handleCompositionEnd() {
	isComposing.value = false
	// 输入法结束后，确保状态同步
	if (inputValue.value) {
		handleInput(inputValue.value)
	}
}



// 处理搜索结果的字段映射
const mapSearchResults = (results: OptionItem[]): OptionItem[] => {
	if (!results || !Array.isArray(results)) return []

	return results.map((item) => {
		// 如果已经有正确的字段，直接返回
		if (item.label && item.value) return item

		// 根据 fieldMapping 映射字段
		const mappedItem = {
			...item,
			label: item[props.fieldMapping.label || 'Name'] || '',
			value: item[props.fieldMapping.value || 'ID'] || '',
			key:
				item[
					props.fieldMapping.key || props.fieldMapping.value || 'ID'
				] || '',
		}

		return mappedItem
	})
}

// 执行 API 搜索
const performApiSearch = (keyword: string) => {
	if (!props.apiConfig || !searchApiService) return

	// 设置加载状态
	searchState.value.isLoading = true
	searchState.value.searchError = null

	// 执行搜索
	searchApiService.searchWithDebounce(
		props.apiConfig,
		keyword,
		(results: OptionItem[], error?: any) => {
			searchState.value.isLoading = false

			if (error) {
				searchState.value.searchError = error.message || '搜索失败'
				searchState.value.searchResults = []
				emit('api-error', error)
			} else {
				// 映射搜索结果字段
				const mappedResults = mapSearchResults(results)
				searchState.value.searchResults = mappedResults
				searchState.value.searchError = null
				emit('api-success', mappedResults)
			}
		}
	)
}

// 判断选项是否已被选中（检查 selected 数组）
const isOptionSelected = (option: OptionItem): boolean => {
	if (!props.multiple) {
		return false // 单选模式下不显示选中状态
	}

	const optionValue = option.value || option[props.fieldMapping.value || 'ID']
	return props.selected.some((item: any) => {
		const itemValue = item[props.fieldMapping.value || 'ID']
		return itemValue === optionValue
	})
}

// 处理选项选择
const handleOptionSelect = (option: OptionItem) => {
	if (props.disabled) return

	emit('option-select', option)

	if (props.multiple) {
		// 多选模式：直接同步添加/移除到 selected 数组
		const optionValue =
			option.value || option[props.fieldMapping.value || 'ID']
		const isAlreadySelected = props.selected.some((item: any) => {
			const itemValue = item[props.fieldMapping.value || 'ID']
			return itemValue === optionValue
		})

		if (!isAlreadySelected) {
			// 添加到 selected 数组（去重）
			props.selected.push(option)
			emit('change', props.selected)
		} else {
			// 从 selected 数组中移除
			const index = props.selected.findIndex((item: any) => {
				const itemValue = item[props.fieldMapping.value || 'ID']
				return itemValue === optionValue
			})
			if (index > -1) {
				props.selected.splice(index, 1)
				emit('change', props.selected)
			}
		}
		// 多选模式下保持下拉面板打开，不清空搜索输入
	} else {
		// 单选模式：设置 inputValue 并关闭弹窗
		const displayValue =
			option.label || option[props.fieldMapping.label || 'Name'] || ''

		// 设置标志，防止输入事件重新打开下拉面板
		justSelected.value = true

		inputValue.value = displayValue
		emit('update:value', displayValue)

		// 更新 selected 数组
		props.selected.splice(0, props.selected.length, option)
		emit('change', props.selected)

		// 关闭弹窗
		visible.value = false

		// 使用 nextTick 确保 DOM 更新后再重置标志
		nextTick(() => {
			setTimeout(() => {
				justSelected.value = false
			}, 100)
		})
	}
}

// 处理容器点击事件
const handleContainerClick = () => {
	// 如果禁用状态，直接返回
	if (props.disabled) return
	
	// 触发外部click事件
	emit('click')
}

// 处理 popover 隐藏事件
const handlePopoverHide = () => {
	visible.value = false
}

// 处理按钮点击事件
function handleBtnClick() {
	// 如果禁用状态，直接返回
	if (props.disabled) return

	// 在单选模式下，如果刚刚完成选择，不要重新打开下拉面板
	if (!props.multiple && justSelected.value) {
		justSelected.value = false
		return
	}
	if(props.searchable){
		// 触发@choose事件
		emit('choose')
	}else{
		emit('click')
	}
}

// 删除项目的处理函数
const handleDelete = (index: number) => {
	if (props.disabled) return
	props.selected.splice(index, 1)
	emit('change', props.selected)
}

// 清空所有项目的处理函数
const handleClearAll = () => {
	// console.log('清空')
	if (props.disabled) return

	// 设置程序化更新标志，防止 handleInput 重新打开下拉面板
	programmaticUpdate.value = true

	// 清空输入框的值和选中项
	inputValue.value = ''
	emit('update:value', '')
	props.selected.splice(0, props.selected.length)
	emit('change', props.selected)

	// 清空搜索结果和状态
	searchState.value.searchResults = []
	searchState.value.isLoading = false
	searchState.value.searchError = null
	// 强制隐藏下拉面板
	visible.value = false

	// 延迟重置程序化更新标志
	nextTick(() => {
		setTimeout(() => {
			programmaticUpdate.value = false
		}, 0)
	})
}

// 计算最大宽度的函数
const calculateItemMaxWidth = () => {
	if (!inputTagBoxRef.value || props.selected.length === 0) {
		itemMaxWidth.value = 'auto'
		return
	}

	nextTick(() => {
		const inputTagElement = inputTagBoxRef.value
		const totalWidth = inputTagElement.offsetWidth

		// 获取除 input-tag-item-container 以外的所有元素宽度
		let otherElementsWidth = 0

		// label 宽度
		const labelElement = inputTagElement.querySelector(
			'.input-tag-pre-container'
		)
		if (labelElement) {
			otherElementsWidth += labelElement.offsetWidth
		}

		// 按钮宽度
		const btnElement = inputTagElement.querySelector('.input-tag-btn')
		if (btnElement) {
			otherElementsWidth += btnElement.offsetWidth
		}

		// clear 按钮宽度（仅在可能显示时预留）
		const shouldReserveClear =
			props.showDelete &&
			!props.disabled &&
			(
				props.multiple
					? props.selected.length > 0
					: (props.searchable ? !!inputValue.value : props.selected.length > 0)
			)
		if (shouldReserveClear) {
			const clearElement = inputTagElement.querySelector('.input-tag-clear') as HTMLElement | null
			if (clearElement) {
				otherElementsWidth += clearElement.offsetWidth
			} else {
				// 兜底：预留 16px 图标宽度 + 4px 间距
				otherElementsWidth += 20
			}
		}

		// container 的 padding 和 border
		const containerElement = inputTagElement.querySelector(
			'.input-tag-container'
		)
		if (containerElement) {
			const containerStyle = getComputedStyle(containerElement)
			otherElementsWidth +=
				parseFloat(containerStyle.paddingLeft) +
				parseFloat(containerStyle.paddingRight)
			otherElementsWidth +=
				parseFloat(containerStyle.borderLeftWidth) +
				parseFloat(containerStyle.borderRightWidth)
		}

		// count-num 宽度 - 在 isLine 模式下特别重要
		let countNumWidth = 0
		if (props.isLine && props.selected.length > 1) {
			const countNumElement = inputTagElement.querySelector('.count-num')
			if (countNumElement) {
				// 获取实际渲染的 count-num 元素宽度
				countNumWidth = countNumElement.offsetWidth
				// 加上 margin-right (5px)
				countNumWidth += 5
			} else {
				// 如果还没有渲染，根据选中数量估算宽度
				const countText = `+${props.selected.length - 1}`
				// 估算：字符宽度(12px) + padding(8px) + margin-right(5px)
				countNumWidth = countText.length * 12 + 8 + 5
			}
		}

		// 额外的间距预留
		const extraSpacing = 5

		// 计算最大宽度
		const maxWidth = totalWidth - otherElementsWidth - countNumWidth - extraSpacing
		
		// 调试信息
		// console.log('itemMaxWidth 计算详情:', {
		// 	totalWidth,
		// 	otherElementsWidth,
		// 	countNumWidth,
		// 	extraSpacing,
		// 	maxWidth,
		// 	isLine: props.isLine,
		// 	selectedCount: props.selected.length
		// })
		
		// 确保最小宽度不为负数
		if (maxWidth > 0) {
			itemMaxWidth.value = `${maxWidth}px`
		} else {
			// 如果计算出的宽度太小，设置一个合理的最小值
			itemMaxWidth.value = '100px'
		}
	})
}

watch(
	() => props.selected,
	() => {
		calculateItemMaxWidth()
	},
	{ immediate: true }
)

// 参考 el-select：监听 popover 显示状态，在显示时计算宽度
watch(
	() => visible.value,
	(newVisible) => {
		if (newVisible) {
			// popover 显示时立即计算宽度
			nextTick(() => {
				calculatePopoverWidth()
			})
		} else {
			// popover 关闭时，单选模式下检查是否需要清空输入框
			// 如果开启了 allowUnselectedInput，则不清空输入框
			if (
				!props.multiple &&
				!props.allowUnselectedInput &&
				props.selected.length === 0 &&
				inputValue.value.trim()
			) {
				inputValue.value = ''
				emit('update:value', '')
			}
		}
	}
)

// 监听 apiConfig 变化，重新初始化搜索服务
watch(
	() => props.apiConfig,
	(newConfig, oldConfig) => {
		// 如果配置发生变化，重新初始化搜索服务
		if (newConfig !== oldConfig) {
			// 保存当前的搜索关键词
			const currentSearchKeyword = inputValue.value.trim()

			if (searchApiService) {
				searchApiService.destroy()
				searchApiService = null
			}

			if (newConfig) {
				searchApiService = new SearchApiService()
			}

			// 清空之前的搜索结果
			searchState.value.searchResults = []
			searchState.value.isLoading = false
			searchState.value.searchError = null

			// 如果当前有搜索内容且配置了新的API，重新执行搜索
			if (currentSearchKeyword && newConfig && props.searchable) {
				performApiSearch(currentSearchKeyword)
			}
		}
	},
	{ deep: true }
)

// 处理焦点变化
const handleFocusChange = (event: FocusEvent) => {
	if (!visible.value) return

	const target = event.target as Element
	if (!target) return

	// 如果焦点移动到组件外部，关闭下拉面板
	if (inputTagBoxRef.value && !inputTagBoxRef.value.contains(target)) {
		// 检查是否焦点移动到了 popover 内部
		const popoverSelectors = [
			'.el-popover',
			'.input-tag-popover',
			'.hidden-items-popover',
			'.el-popper',
			'[role="tooltip"]',
			'[data-popper-placement]',
		]

		let isInPopover = false
		for (const selector of popoverSelectors) {
			const elements = document.querySelectorAll(selector)
			for (const element of elements) {
				if (element.contains(target)) {
					isInPopover = true
					break
				}
			}
			if (isInPopover) break
		}

		if (!isInPopover) {
			visible.value = false
		}
	}
}

// 组件挂载时初始化
onMounted(() => {
	// 添加全局点击事件监听器，使用 capture 阶段确保优先处理
	document.addEventListener('click', handleClickOutside, true)
	// 添加焦点变化监听器
	document.addEventListener('focusin', handleFocusChange)

	// 初始化搜索服务
	if (props.apiConfig) {
		searchApiService = new SearchApiService()
	}

	// 组件挂载后计算初始宽度
	nextTick(() => {
		calculateItemMaxWidth()
	})
})

// 组件卸载时清理资源
onUnmounted(() => {
	document.removeEventListener('click', handleClickOutside, true)
	document.removeEventListener('focusin', handleFocusChange)

	// 销毁搜索服务
	if (searchApiService) {
		searchApiService.destroy()
		searchApiService = null
	}
})
</script>

<style lang="scss" scoped>
.input-tag-box {
	width: 100%;
}
.input-tag {
	display: flex;
	color: #909399;
	width: 100%;
	background-color: #fff;
	border: 1px solid #dcdfe6;
	border-radius: 4px;
	transition: border-color 0.2s ease;

	&.is-focused {
		border-color: var(--wtwo-color-primary);
	}
	&.disabled {
		background-color: var(--wtwo-fill-color-light);
		border: 1px solid #dcdfe6;
		cursor: not-allowed !important;
		.input-tag-container,
		.input-tag-item,
		.input-tag-btn,
		.count-num,
		.input-tag-clear {
			cursor: not-allowed !important;
		}
	}
	.input-tag-container {
		flex: 1 1 auto;
		overflow: hidden;
		line-height: 14px;
		min-height: 30px;
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		padding: 0px 5px 0px 12px;
		cursor: pointer;

		.input-tag-placeholder {
			color: #a8abb2;
			word-break: keep-all;
			position: absolute;
			top: 50%;
			transform: translateY(-50%);
		}

		.input-tag-pre-container {
			align-self: flex-start;
			line-height: 30px;
			padding-right: 15px;
		}
		.input-tag-content {
			display: flex;
			flex: 1;
			position: relative;
			min-height: 20px;
		}
		.input-tag-item-container {
			display: flex;
			flex-wrap: wrap;
			align-items: center;
			font-size: 12px;
			.input-tag-item {
				display: flex;
				flex: 0 0 auto;
				justify-content: space-between;
				align-items: center;
				padding: 4px 5px 4px 10px;
				background: #f4f4f5;
				border-radius: 4px;
				margin-top: 3px;
				margin-right: 5px;
				margin-bottom: 3px;
				cursor: pointer;
				/* 移除 max-width: 100%，让动态设置的 itemMaxWidth 生效 */
				.wtwo-close {
					width: 14px;
					height: 14px;
				}
				.input-tag-close {
					margin-left: 5px;
					display: flex;
					align-items: center;
					justify-content: center;
					line-height: 1;
					width: 16px;
					height: 16px;
					border-radius: 50%;
					flex-shrink: 0;
					&:hover {
						background-color: #909399;
						color: #fff;
						.wtwo-close {
							width: 12px;
							height: 12px;
						}
					}
				}
			}
		}
	}

	.input-tag-search {
		font-size: 14px;
		flex: 1;
		min-width: 11px;
		width: auto !important;
		:deep(.wtwo-input__wrapper) {
			box-shadow: none !important;
			padding: 0 !important;
			background: transparent;
			.wtwo-input__inner {
				min-width: 11px;
				padding-left: 0!important;
			}
		}
	}
	.input-tag-btn {
		border-radius: 0 4px 4px 0;
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 32px;
		text-align: center;
		border-left: transparent;
		color: #909399;
		padding: 0 7px 0 7px;
		flex: 0 0 auto;
		height: 30px;
		cursor: pointer;
		.wtwo-icon{
			color:var(--wtwo-select-input-color);
		}
		&:hover{
			color: var(--wtwo-color-primary);
		}
	}
	.input-tag-text {
		padding: 0px;
		
		:deep(div) {
			border-left: 1px solid #dcdfe6;
			border-radius: 0px 4px 4px 0px;
			font-size: 14px;
			width: 44px;
			color: #606266;
			background: #F7F8FA;
			height: 30px;
			cursor: pointer;
			display: flex;
			justify-content: center;
			align-items: center;
			padding-right: 2px;
			margin-left: 4px;
		}
	}

	.input-tag-container.is-line {
		// 在 isLine 模式下，确保不换行
		.input-tag-content {
			flex-wrap: nowrap;
			overflow: hidden;
		}
		.input-tag-item-container {
			flex-wrap: nowrap !important;
			flex: 1;
			overflow: hidden;
			.input-tag-content-define {
				text-overflow: ellipsis;
				white-space: nowrap;
				overflow: hidden;
				display: flex;
				align-items: center;
				.wtwo-icon{
					margin-right: 4px;
					color: #f56c6c;
				}
			}
		}
		.count-num {
			padding: 5px 8px;
			background: #f4f4f5;
			border-radius: 4px;
			flex-shrink: 0;
			margin-right: 5px;
			cursor: pointer;
			&:hover {
				background: #e6e8eb;
			}
		}
	}

	// 新增：专门用于 isLine 模式的容器样式
	.input-tag-line-container {
		display: flex;
		align-items: center;
		flex-wrap: nowrap;
		overflow: hidden;
		width: 100%;
		
		.input-tag-item {
			flex-shrink: 0;
		}
		
		.count-num {
			flex-shrink: 0;
		}
	}
}
.input-tag-clear {
	display: flex;
	align-items: center;
	justify-content: center;
	line-height: 1;
	width: 16px;
	height: 16px;
	border-radius: 50%;
	flex-shrink: 0;
	cursor: pointer;
	margin-top: 8px;
	visibility: hidden;
	position: relative;
	z-index: 10;
	&:hover {
		// background-color: #909399;
		color: #909399;
	}
	&.is-hidden {
		visibility: hidden;
		cursor: default;
	}
}
.input-tag:hover {
	.input-tag-clear {
		visibility: visible;
		&.is-hidden {
			visibility: hidden;
			cursor: default;
		}
	}
}
// 隐藏项目列表样式
.hidden-items-list {
	.hidden-item {
		display: inline-flex;
		align-items: center;
		padding: 4px 8px;
		margin-bottom: 5px;
		margin-right: 8px;
		background: #f8f9fa;
		border-radius: 4px;
		font-size: 12px;
		width: auto;

		&:last-child {
			margin-right: 0;
		}

		&:hover {
			background: #f0f2f5;
		}

		.hidden-item-name {
			color: #606266;
			white-space: nowrap;
			margin-right: 4px;
			display: flex;
			align-items: center;
			.wtwo-icon{
				margin-right: 4px;
				color: #f56c6c;
			}
		}

		.hidden-item-close {
			margin-left: 8px;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 16px;
			height: 16px;
			border-radius: 50%;
			cursor: pointer;
			flex-shrink: 0;

			.wtwo-close {
				width: 12px;
				height: 12px;
				color: #909399;
			}

			&:hover {
				background-color: #909399;
				.wtwo-close {
					color: #fff;
				}
			}
		}
	}
}
</style>

<style lang="scss">
// 全局样式，用于控制隐藏项目 popover 的宽度
.hidden-items-popover {
	min-width: 50px !important;
	max-width: 500px !important;
	width: auto !important;
	padding: 5px 5px 0 5px !important;

	.wtwo-popper__arrow {
		display: block;
	}
}

// 下拉选项样式
.input-tag-dropdown {
	&__header {
		padding: 8px 12px;
		border-bottom: 1px solid #e4e7ed;
		background-color: #f8f9fa;
	}

	&__content {
		max-height: 200px;
		overflow-y: auto;
	}

	&__option {
		margin: 4px;
		padding: 4px 12px;
		border-radius: 4px;
		cursor: pointer;
		transition: background-color 0.2s;
		color: #606266;
		font-size: 14px;

		&:hover,
		&--highlighted {
			background-color: #f5f7fa;
		}

		&--selected {
			color: var(--wtwo-color-primary);
			font-weight: 500;
		}

		&-content {
			display: flex;
			justify-content: space-between;
			align-items: center;
		}

		&-text {
			flex: 1;
		}

		&-check {
			color: var(--wtwo-color-primary);
			margin-left: 8px;
			display: flex;
			align-items: center;
		}
	}

	&__footer {
		padding: 8px 12px;
		border-top: 1px solid #e4e7ed;
	}

	&__empty,
	&__loading,
	&__error {
		padding: 20px;
		text-align: center;
		color: #909399;
		font-size: 14px;
	}

	&__error {
		color: #f56c6c;
	}
}
.input-tag-popover {
	padding: 0 !important;
}
</style>
