<template>
    <el-dialog
		v-model="dialogVisible"
		:title="transToConfigDescript('请选择1个校区')"
		width="500px"
		:close-on-click-modal="false"
		:append-to-body="true"
		:destroy-on-close="true"
		:align-center="true"
		class="chooseSingleCampus"
		draggable
		@close="close"
	>
		<div class="campus-selection-container">
			<!-- 提示信息 -->
			<pageAttentionTips class="py-12px">
				<span class="warning-text" v-if="optionTextFull">{{ optionTextFull }}</span>
				<span class="warning-text" v-else>"{{ optionText }}"{{transToConfigDescript('只能在单校区操作,请选择1个校区')}}</span>
			</pageAttentionTips>

			<!-- 校区选择器 -->
			<div class="campus-select-wrapper">
				<el-select
					v-model="selectedCampus"
					:placeholder="transToConfigDescript('请选择1个校区')"
					class="campus-select"
					popper-class="campus-select-dropdown"
                    filterable
				>
                    <el-option-group
                        v-for="group in options"
                        :key="group.label"
                        :label="group.label"
                    >
                        <el-option
                            v-for="item in group.options"
                            :key="item.ID"
                            :label="item.Name"
                            :value="item.ID"
                        />
                    </el-option-group>
					
				</el-select>
			</div>
			<div v-if="subTextHtml" class="text-12px line-height-18px color-#909399 mb-20px" v-html="subTextHtml"></div>
		</div>

		<!-- 底部按钮 -->
		<template #footer>
			<div class="dialog-footer">
				<el-button @click="close">取消</el-button>
				<el-button type="primary" @click="confirm" :disabled="!selectedCampus">
					确定
				</el-button>
			</div>
		</template>
	</el-dialog>
</template>
<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { useUserCampuses, useCurrentCampuses } from '@/store'
import PageAttentionTips from '../common/page-attention-tips/pageAttentionTips.vue'
import { transToConfigDescript } from '@/utils/filters/filters'

const optionText=ref('当前操作')
const optionTextFull=ref('')
const subTextHtml=ref('')
// 响应式数据
const dialogVisible = ref(false)
const selectedCampus = ref<any>(null)
// 允许选择的校区ID集合（可选）。传入为仅包含ID的Array。
const allowedCampusIds = ref<string[] | null>(null)

// 从store获取数据
const userCampusesStore = useUserCampuses()
const currentCampusesStore = useCurrentCampuses()

// 计算属性
const allCampusList = computed(() => {
	return userCampusesStore.userCampuses || []
})

const recentCampusList = computed(() => {
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	
	return allCampusList.value.filter((campus: any) => 
		currentCampusIds.includes(campus.ID.toString())
	)
})

const otherCampusList = computed(() => {
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	return allCampusList.value.filter((campus: any) => !currentCampusIds.includes(campus.ID.toString()))
})

const options = computed(() => {
    // 基础列表
    let recent = recentCampusList.value
    let others = otherCampusList.value

    // 如果传入了允许的校区ID，则与现有选项取交集
    if (allowedCampusIds.value && Array.isArray(allowedCampusIds.value) && allowedCampusIds.value.length > 0) {
        const allowSet = new Set(allowedCampusIds.value.map(id => id?.toString?.() || id))
        recent = recent.filter((c:any) => allowSet.has(c.ID?.toString?.() || c.ID))
        others = others.filter((c:any) => allowSet.has(c.ID?.toString?.() || c.ID))
    }

    return [
        {
            label:'最近使用',
            options: recent
        },{
            label: recent.length>0?transToConfigDescript('其他校区'):transToConfigDescript('全部校区'),
            options: others
        }
    ]
})  

// 扁平化后的可选校区，用于判断是否只有一个可选项
const flatOptions = computed(() => {
    const groups = options.value || []
    const list = [] as any[]
    groups.forEach((g:any) => {
        if (Array.isArray(g?.options)) list.push(...g.options)
    })
    return list
})



const confirm = () => {
	if (selectedCampus.value) {
        const currentCampuses=useCurrentCampuses()
        currentCampuses.$state={
            campusList:selectedCampus.value,
            multi:true
        }
		if (window.microApp) {
			window.microApp.dispatch({type:'global:setCampus',campus: selectedCampus.value,time:Date.now()})
		}
		_resolve(selectedCampus.value)
		close()
	}
}

const close = () => {
	dialogVisible.value = false
	selectedCampus.value = null
    allowedCampusIds.value = null

	_reject?.()
}

// Promise 处理
let _resolve: any = null,
	_reject: any = null

function open(params: any) {
    optionText.value = params?.optionText || ''
	optionTextFull.value = params?.optionTextFull || ''
	subTextHtml.value = params?.subTextHtml || ''
    // 接收可选的允许校区ID列表（仅ID数组）
    allowedCampusIds.value = Array.isArray(params?.allowedCampusIds) ? params.allowedCampusIds : null
    return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		dialogVisible.value = true
	})
}

// 当弹窗打开且仅有一个可选校区时，默认选中该校区
watch(
    () => [dialogVisible.value, flatOptions.value],
    () => {
        if (dialogVisible.value) {
            const list = flatOptions.value
            if (Array.isArray(list) && list.length === 1) {
                selectedCampus.value = list[0]?.ID ?? null
            }
        }
    },
    { immediate: true }
)

// 暴露方法
defineExpose({
	open
})
</script>
<style lang="scss" scoped>
.chooseSingleCampus {
	.campus-selection-container {
		position: relative;
	}

	.warning-message {
		display: flex;
		align-items: center;
		margin-bottom: 16px;
        margin-top: 16px;
		padding: 12px;
		background-color: #fff7e6;
		border: 1px solid #ffd591;
		border-radius: 4px;

		.warning-tag {
			margin-right: 8px;
			background-color: #faad14;
			border-color: #faad14;
			color: #fff;
		}

		.warning-text {
			color: #d46b08;
			font-size: 14px;
		}
	}

	.campus-select-wrapper {
		margin-bottom: 16px;

		.campus-select {
			width: 100%;
		}
	}

	.dialog-footer {
		text-align: right;
	}
}
.search-container {
			padding: 12px;

			.el-input {
				width: 100%;
			}
		}

		.tabs-container {

			.tabs-wrapper {
				display: flex;
				align-items: center;
				position: relative;

				.tab-item {
					flex: 1;
					padding: 12px 16px;
					text-align: center;
					cursor: pointer;
					position: relative;
					color: #606266;
					font-size: 14px;
					transition: all 0.3s;

					&:hover {
						color: #409eff;
					}

					&.active {
						color: #409eff;
						border-bottom: 2px solid #409eff;
					}

					
				}

				
			}
		}

</style>