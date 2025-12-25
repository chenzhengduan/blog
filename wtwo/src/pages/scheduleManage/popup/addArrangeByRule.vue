<template>
    <el-drawer v-model="drawer" title="按规则批量新增" direction="rtl" size="1200px" class="addArrangeByRule"
        :close-on-click-modal="false" :append-to-body="true" @close="close" :destroy-on-close="true">
        <template #header>
			<div class="flex-center">
				<el-select
					:model-value="selectedCampusId" 
                    :placeholder="transToConfigDescript('请选择1个校区')" 
                    @update:model-value="handleCampusChange"
                    class="header-campus-select w-280px!"
                    popper-class="campus-select-dropdown"
                    filterable
				>
					<template #prefix>
						<p class="search-input-label">
							{{ transToConfigDescript('上课校区') }}
						</p>
					</template>
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
		</template>
        <div class="drawer-body-wrap pt-10px" v-loading="loading">
            

            <!-- 给班级排课 -->
            <addClassArrangeForm v-if="selectedCampusId && selectedType === 10" ref="addClassArrangeFormRef"
                @submit="handleClassArrangeSubmit" :selected-campus-id="selectedCampusId" :only-emit-data="true" :activated="true" />

            <!-- 给学员排课 -->
            <addStudentArrangeForm v-if="selectedCampusId && selectedType === 20" ref="addStudentArrangeFormRef"
                @submit="handleStudentArrangeSubmit" :selected-campus-id="selectedCampusId" :only-emit-data="true" :activated="true" />
            <!-- 排预约课 -->
            <addShiftArrangeForm v-if="selectedCampusId && selectedType === 30" ref="addShiftArrangeFormRef"
                @submit="handleShiftArrangeSubmit" :selected-campus-id="selectedCampusId" :only-emit-data="true" :activated="true" />
        </div>
        <template #footer>
            <div class="wtwo-drawer-footer flex-right">
                <div class="flex-right">
                    <el-button @click="close" :disabled="loading">取消</el-button>
                    <el-button type="primary" @click="submit" :disabled="loading">确认新增</el-button>
                </div>
            </div>
        </template>
    </el-drawer>
</template>

<script lang="ts" setup>
import { CoursePlanGetCoursePlanPreview,GetStudentCoursePlanPreview,
    GetSubscribeCoursePlanPreview } from '@/api/arrange'
import { ref, computed, defineAsyncComponent, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserCampuses, useCurrentCampuses } from '@/store'
import { transToConfigDescript } from '@/utils/filters/filters'
// 使用懒加载优化性能
const AddClassArrangeForm = defineAsyncComponent({
    loader: () => import('./child/addClassArrangeForm.vue'),
    loadingComponent: undefined,
    errorComponent: undefined,
    delay: 200,
    timeout: 3000
})
const AddStudentArrangeForm = defineAsyncComponent({
    loader: () => import('./child/addStudentArrangeForm.vue'),
    loadingComponent: undefined,
    errorComponent: undefined,
    delay: 200,
    timeout: 3000
})
const AddShiftArrangeForm = defineAsyncComponent({
    loader: () => import('./child/addShiftArrangeForm.vue'),
    loadingComponent: undefined,
    errorComponent: undefined,
    delay: 200,
    timeout: 3000
})

const drawer = ref(false)
const loading = ref(false)
const selectedCampusId = ref('')
const selectedType = ref(10) // 默认给班级排课
const checkConflict = ref(0)

const addClassArrangeFormRef = ref()
const addStudentArrangeFormRef = ref()
const addShiftArrangeFormRef = ref()

// 从store获取数据
const userCampusesStore = useUserCampuses()
const currentCampusesStore = useCurrentCampuses()

// 获取所有校区列表
const allCampusList = computed(() => {
    return userCampusesStore.userCampuses || []
})

// 计算最近使用的校区列表
const recentCampusList = computed(() => {
    const currentCampusIds = currentCampusesStore.campusList
        ? currentCampusesStore.campusList.split(',').filter(id => id.trim())
        : []
    
    return allCampusList.value.filter((campus: any) => 
        currentCampusIds.includes(campus.ID.toString())
    )
})

// 计算其他校区列表
const otherCampusList = computed(() => {
    const currentCampusIds = currentCampusesStore.campusList
        ? currentCampusesStore.campusList.split(',').filter(id => id.trim())
        : []
    
    return allCampusList.value.filter((campus: any) => 
        !currentCampusIds.includes(campus.ID.toString())
    )
})

// 分组选项
const options = computed(() => {
    const recent = recentCampusList.value
    const others = otherCampusList.value

    return [
        {
            label: '最近使用',
            options: recent
        },
        {
            label: recent.length > 0 ? transToConfigDescript('其他校区') : transToConfigDescript('全部校区'),
            options: others
        }
    ]
})

// 处理班级排课提交
function handleClassArrangeSubmit(params: any) {
    loading.value = true
    params.CheckConflict = checkConflict.value
    CoursePlanGetCoursePlanPreview(params).then((res: any) => {
        if (res.Data && res.Data.length) {
            _resolve(res.Data)
            drawer.value = false

        } else {
            ElMessage.warning(res.Data.ErrorMsg || '没有生成任何排课记录，请检查规则。')
        }
    }).catch((error: any) => {
        // ElMessage.error('排课失败：' + (error.ErrorMsg || '未知错误'))
    }).finally(() => {
        loading.value = false
    })
}

// 处理学员排课提交
function handleStudentArrangeSubmit(params: any) {
    loading.value = true
    params.CheckConflict = checkConflict.value
    GetStudentCoursePlanPreview(params).then((res: any) => {
        if (res.Data && res.Data.length) {
            _resolve(res.Data)
            drawer.value = false
        } else {
            ElMessage.warning(res.Data.ErrorMsg || '没有生成任何排课记录，请检查规则。')
        }
    }).catch((error: any) => {
        // ElMessage.error('排课失败：' + (error.ErrorMsg || '未知错误'))
    }).finally(() => {
        loading.value = false
    })
}

// 处理预约排课提交
function handleShiftArrangeSubmit(params: any) {
    loading.value = true
    params.CheckConflict = checkConflict.value
    GetSubscribeCoursePlanPreview(params).then((res: any) => {
        if (res.Data && res.Data.length) {
            _resolve(res.Data)
            drawer.value = false
        } else {
            ElMessage.warning(res.Data.ErrorMsg || '没有生成任何排课记录，请检查规则。')
        }
    }).catch((error: any) => {
        // ElMessage.error('排课失败：' + (error.ErrorMsg || '未知错误'))
    }).finally(() => {
        loading.value = false
    })
}

// 处理校区变化
async function handleCampusChange(newCampusId: string) {
    // 保存旧值用于回滚
    const oldCampusId = selectedCampusId.value;
    
    // 如果新值和旧值相同，不需要处理
    if (newCampusId === oldCampusId) {
        return;
    }

    try {
        if(oldCampusId!==''){
			// 获取当前激活的表单引用
			let currentFormRef = null
			if (selectedType.value === 0) {
				currentFormRef = addClassArrangeFormRef.value
			} else if (selectedType.value === 1) {
				currentFormRef = addStudentArrangeFormRef.value
			} else if (selectedType.value === 2) {
				currentFormRef = addShiftArrangeFormRef.value
			}

			// 检查当前表单是否有关键字段变化
			const hasChanges = currentFormRef?.hasKeyFieldsChanged ? currentFormRef.hasKeyFieldsChanged() : false

			// 只有当关键字段有变化时才弹出确认框
			if (hasChanges) {
				await ElMessageBox.confirm(
					transToConfigDescript('切换"上课校区"仅保留设置的时间等信息，确认切换吗?'),
					'提示',
					{
						confirmButtonText: '确认',
						cancelButtonText: '取消',
						type: 'warning',
						closeOnClickModal: false,
						closeOnPressEscape: false
					}
				)
			}
		}

		// 用户确认切换，更新 CampusID 并重置表单
		selectedCampusId.value = newCampusId;

		// 重置当前表单
		if (selectedType.value === 0) {
			addClassArrangeFormRef.value?.resetForm()
		} else if (selectedType.value === 1) {
			addStudentArrangeFormRef.value?.resetForm()
		} else if (selectedType.value === 2) {
			addShiftArrangeFormRef.value?.resetForm()
		}
    } catch (error) {
        // 用户取消切换，不需要回滚（因为使用 :model-value，值还没变）
    }
}

function submit() {
    // 根据当前选中的类型调用对应的表单
    if (selectedType.value === 10) {
        // 班级排课
        addClassArrangeFormRef.value?.submit()
    } else if (selectedType.value === 20) {
        // 学员排课
        addStudentArrangeFormRef.value?.submit()
    } else if (selectedType.value === 30) {
        // 预约排课
        addShiftArrangeFormRef.value?.submit()
    }
}

let _resolve: any = null,
    _reject: any = null

/** 对外暴露一个open方法 */
function open(params: any) {
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		// 接收类型参数
		if (params) {
			if (params.selectedTableType) {
				selectedType.value = params.selectedTableType
			}
		}
		// 默认选择第一个最近使用的校区，如果没有则选择第一个校区
		if (recentCampusList.value.length > 0) {
			selectedCampusId.value = recentCampusList.value[0].ID
		} else if (allCampusList.value.length > 0) {
			selectedCampusId.value = allCampusList.value[0].ID
		}
		drawer.value = true
	})
}

function close() {
    drawer.value = false
    selectedCampusId.value = ''
    selectedType.value = 10
    addClassArrangeFormRef.value?.resetAllForm()
    addStudentArrangeFormRef.value?.resetAllForm()
    addShiftArrangeFormRef.value?.resetAllForm()
    _reject && _reject()
}

defineExpose({
    open,
})
</script>

<style lang="scss" scope>
.addArrangeByRule {
    .drawer-body-wrap {
        min-width: 1150px;

        .campus-select-container {
            margin: 10px 16px;
            display: flex;
            align-items: center;
            gap: 8px;
            
            .campus-label {
                font-size: 14px;
                color: #303133;
                font-weight: 500;
                white-space: nowrap;
            }
        }

        .two-column-wrap {
            flex-wrap: wrap;

            .half-width {
                width: calc(50% - 30px);

                &:nth-child(2n) {
                    margin-left: 60px;
                }
            }
        }

        .half-width {
            width: 50%;
        }
    }
}

.option-content {
    display: flex;
    align-items: center;

    .option-title {
        font-size: 14px;
        color: #303133;
        line-height: 20px;
    }
}

.header-campus-select{
	.wtwo-select__wrapper{
		background-color: #F2F3F5;
        box-shadow:none;
	}
}
</style>

