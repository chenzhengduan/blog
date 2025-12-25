<template>
    <BaseSelect v-model="selectedValue" :options="filteredCourseList" :defaultNotshowEmpty="true"
        :initial-data="props.initialData" :placeholder="transToConfigDescript(dynamicPlaceholder)" :clearable="clearable" :disabled="disabled"
        :loading="loading" :select-style="selectStyle" :id="id" :show-search="!!campusId" :search-placeholder="transToConfigDescript('搜索课程')"
        :filterable="true" :cell-mode="cellMode && !!campusId" value-key="ID" label-key="Name"
        :get-option-desc="getCourseDesc" :empty-text="transToConfigDescript('暂无匹配的课程')" @change="handleChange" @clear="handleClear"
        @search="handleSearch" @visible-change="handleVisibleChange">
        <template #option="{ item }">
            <div class="course-option">
                <el-icon size="20px">
                    <svg aria-hidden="true">
                        <use xlink:href="#w2-xuanke1"></use>
                    </svg>
                </el-icon>
                <span class="course-name">{{ item.Name }}</span>
            </div>
        </template>
        <template #empty>
            <div class="course-empty">
                <el-empty v-show="loading || !campusId || !courseList.length" :image="globalData.emptyPng2" :image-size="80"
                    :description="transToConfigDescript(loading ? '加载中...' : (!campusId ? '请先选“上课校区”' : searchKeyword ? '暂无匹配的课程' : '暂无课程'))" />
            </div>
        </template>
    </BaseSelect>
</template>

<script setup>
import { ref, computed, watch, onMounted, getCurrentInstance } from 'vue'
import BaseSelect from '@/components/common/base-select/base-select.vue'
import { queryShift } from '@/api/comm'
import { transToConfigDescript } from '@/utils/filters/filters'

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

const props = defineProps({
    modelValue: {
        type: [String, Number],
        default: ''
    },
    placeholder: {
        type: String,
        default: transToConfigDescript('请选择课程')
    },
    clearable: {
        type: Boolean,
        default: true
    },
    disabled: {
        type: Boolean,
        default: false
    },
    selectStyle: {
        type: Object,
        default: () => ({ width: '100%' })
    },
    id: {
        type: String,
        default: ''
    },
    campusId: { // 依赖校区ID
        type: [String, Number],
        default: ''
    },
    initialData: {
        type: Object,
        default: null
    },
    cellMode: {
        type: Boolean,
        default: false
    },
})

const emit = defineEmits([
    'update:modelValue',
    'change',
    'clear',
    'search',
    'visible-change' // 🆕 暴露 visible-change 事件
])

const selectedValue = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const courseList = ref([])

const filteredCourseList = computed(() => {
    // 后端已过滤，前端不再需要过滤
    return courseList.value
})

const dynamicPlaceholder = computed(() => {
    if (!props.campusId) {
        return '请先选择校区'
    }
    return props.placeholder
})

const handleChange = (value) => {
    emit('update:modelValue', value)
    emit('change', value, getSelectedCourse(value))
}

const handleClear = () => {
    emit('update:modelValue', '')
    emit('clear')
}

const handleSearch = (value) => {
    searchKeyword.value = value
    emit('search', value, filteredCourseList.value)
}

// 🆕 处理下拉框显示/隐藏
const handleVisibleChange = (visible) => {
    emit('visible-change', visible)
}

const getCourseDesc = (item) => {
    return item.GradeName || ''
}

const getSelectedCourse = (value) => {
    return courseList.value.find(courseItem => courseItem.ID === value) || null
}

const fetchCourseList = async (isSearch = false) => {
    if (!props.campusId) {
        courseList.value = []
        return
    }

    // 只有在非搜索触发的情况下，才清空课程列表
    if (!isSearch) {
        courseList.value = []
    }

    loading.value = true
    try {
        const params = {
            PageIndex: 1,
            PageSize: 20,
            status: 1,
            shiftType: 7,
            campus: props.campusId,
            Query: searchKeyword.value // 将搜索关键词传给后端
        }
        const res = await queryShift(params)
        if (res.ErrorCode === 200 && res.Data && res.Data) {
            courseList.value = res.Data
        } else {
            courseList.value = []
        }
    } catch (error) {
        console.error('获取课程列表失败:', error)
        courseList.value = []
    } finally {
        loading.value = false
    }
}

watch(() => props.modelValue, (newValue) => {
    if (newValue) {
        if (courseList.value.length === 0 && props.campusId) {
            // 列表为空但有campusId，说明可能正在加载
            const checkCourseList = () => {
                if (courseList.value.length > 0) {
                    selectedValue.value = newValue
                } else if (!loading.value) { // 如果没在加载了，说明加载完了还是没有
                    selectedValue.value = newValue
                }
            }
            checkCourseList()
        } else {
            selectedValue.value = newValue
        }
    } else {
        selectedValue.value = ''
    }
})

watch(() => props.campusId, (newCampusId, oldCampusId) => {
    if (newCampusId) {
        // 校区变化，清空搜索词和列表
        searchKeyword.value = '' // 同时清空搜索词
        fetchCourseList()
        // 如果当前选中值不在新校区的课程中，清空选择
        if (selectedValue.value && newCampusId && courseList.value) {
            const currentCourse = courseList.value.find(courseItem => courseItem.ID === selectedValue.value)
            if (!currentCourse) {
                selectedValue.value = ''
                emit('update:modelValue', '')
                emit('clear')
            }
        }
    } else {
        courseList.value = []
    }
}, { immediate: true })


// 监听搜索关键词变化，带防抖
watch(searchKeyword, (newValue, oldValue) => {
    // 确保有校区ID时才进行搜索
    if (props.campusId && newValue !== oldValue) {
        fetchCourseList(true)
    }
})

onMounted(() => {
    // onMounted中的调用已由 watch immediate: true 替代
    // if (props.campusId) {
    //   fetchCourseList()
    // }
})
</script>

<style scoped>
.course-option {
    display: flex;
    width: 100%;
    align-items: center;
}

.course-name {
    font-size: 14px;
    color: #606266;
    font-weight: 400;
    margin-left: 8px;
}

.course-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    color: #909399;
    font-size: 14px;
}

.course-empty .wtwo-icon {
    margin-right: 8px;
    font-size: 16px;
}
</style>