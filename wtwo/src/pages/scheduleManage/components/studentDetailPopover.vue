<template>
    <el-popover
        ref="popoverRef"
        v-model:visible="visible"
        :virtual-ref="virtualRef"
        virtual-triggering
        placement="right"
        :width="408"
        :trigger="[]"
        append-to="body"
        popper-class="wtwo-student-detail-popover"
        :offset="8"
        :hide-after="0"
        :popper-options="popperOptions"
        @show="handleShow"
        @hide="handleHide"
    >
        <div class="wtwo-student-popover" @mousedown.stop @click.stop v-loading="loading">
            <div class="wtwo-student-popover__header">
                <div class="wtwo-student-popover__user">
                    <div class="wtwo-student-popover__avatar" v-if="student?.Photo">
                        <img :src="student.Photo" alt="" />
                    </div>
                    <div class="wtwo-student-popover__avatar placeholder" v-else>{{ getNameInitial(student?.Name) }}</div>
                    <div class="wtwo-student-popover__meta">
                        <div class="wtwo-student-popover__name ellipsis-single" :title="student?.Name">{{ student?.Name || '-' }}</div>
                        <div class="wtwo-student-popover__tags ellipsis-single" :class="{ 'is-overflow': tagsOverflow }" ref="tagsRef">
                            <span class="wtwo-student-popover__tag" v-for="t in computedTags" :key="t.ID">{{ t.Name }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="wtwo-student-popover__info">
                <div class="flex-center">
                    <div class="wtwo-student-popover__row w-50%"><span class="wtwo-student-popover__label">学号：</span><span class="wtwo-student-popover__value ellipsis-single" :title="student?.StudentSerial">{{student?.StudentSerial||'-'}}</span></div>
                    <div class="wtwo-student-popover__row w-50%"><span class="wtwo-student-popover__label">{{transToConfigDescript('所属校区：')}}</span><span class="wtwo-student-popover__value ellipsis-single" :title="student?.CampusName">{{ student?.CampusName || '-' }}</span></div>
                </div>
                <div class="flex-center">
                    <div class="wtwo-student-popover__row w-50%"><span class="wtwo-student-popover__label">学员类型：</span><span class="wtwo-student-popover__value ellipsis-single" :title="student?.StudentType">{{ student?.StudentType || '-' }}</span></div>
                    <div class="wtwo-student-popover__row w-50%"><span class="wtwo-student-popover__label">{{transToConfigDescript('学管师：')}}</span><span class="wtwo-student-popover__value ellipsis-single" :title="student?.StudentMaster">{{ student?.StudentMaster || '-' }}</span></div>
                </div>
            </div>
            <PageAttentionTips class="ml-16px mb-6px">下方仅显示学员报读的一对一{{transToConfigDescript('课程')}}</PageAttentionTips>
            <div class="wtwo-student-popover__course-list">
                <div v-if="!loading && courses.length === 0" class="wtwo-student-popover__empty">暂无{{transToConfigDescript('课程')}}</div>
                <div v-else class="wtwo-student-popover__item" v-for="(c, idx) in courses" :key="idx">
                    <div class="flex-center mb-6px">
                        <div class="wtwo-student-popover__title ellipsis-single" :title="c.ShiftName">{{ c.ShiftName }}</div>
                        <div class="wtwo-student-popover__item-tag">{{ c.IsOneToOne }}</div>
                    </div>
                    <div class="wtwo-student-popover__sub">
                        <div class="wtwo-student-popover__sub-item">
                            <div class="wtwo-student-popover__strong">{{ c.NeedCourseCount ?? '-' }}
                                <el-tooltip
                                    effect="dark"
                                    placement="top"
                                >
                                    <el-icon color="#909399" class="ml-4px" size="14px">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-xinxitishi"></use>
                                        </svg>
                                    </el-icon>
                                    <template #content>
                                        <div class="w-200px">可排数量=剩余数量-已排未上数量（注意！此数量仅供参考，若后续排课有“已出勤未计费”的情况，则可造成实际可排数量大于此处显示的数量，均为正常情况）</div>
                                    </template>
                                </el-tooltip>
                            </div>
                            <div class="wtwo-student-popover__label">可排数量（{{c.ShiftUnit}}）</div>
                        </div>
                        <div class="wtwo-student-popover__split">｜</div>
                        <div class="wtwo-student-popover__sub-item">
                            <div class="wtwo-student-popover__strong">{{ c.RemainAmount }}
                                <el-tooltip
                                    effect="dark"
                                    placement="top"
                                >
                                    <el-icon color="#909399" class="ml-4px" size="14px">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-xinxitishi"></use>
                                        </svg>
                                    </el-icon>
                                    <template #content>
                                        <div>剩余数量=购买数量-课消数量-结转数量-退费数量-过期数量</div>
                                    </template>
                                </el-tooltip>
                            </div>
                            <div class="wtwo-student-popover__label">剩余数量（{{c.ShiftUnit}}）</div>
                        </div>
                        <div class="wtwo-student-popover__split">｜</div>
                        <div class="wtwo-student-popover__sub-item">
                            <div class="wtwo-student-popover__strong">{{ c.OutDate || '-' }}</div>
                            <div class="wtwo-student-popover__label">有效期</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getStudentCardDetail } from '@/api/arrange'
import { useSyncCancel } from '@common/tool/http/fetch'
import { cloneDeep } from 'lodash'
import PageAttentionTips from '@/components/common/page-attention-tips/pageAttentionTips.vue'
import { getNameInitial } from '@/utils';
import { transToConfigDescript } from '@/utils/filters/filters'

interface StudentInfo {
    ID?: string | number
    Name?: string
    Photo?: string
    Code?: string
    CampusName?: string
    StudentLabel?: string[]
    StudentSerial?: string
    StudentType?: string
    StudentMaster?: string
}

const props = withDefaults(defineProps<{
    student?: StudentInfo
    virtualRef?: any
    campusId?: string | number
    preventAutoHide?: boolean
}>(), {
    preventAutoHide: false
})

const emit = defineEmits<{ show: []; hide: [] }>()

const visible = ref(false)
const popoverRef = ref()

// 本地合并后的学员信息（props + 接口返回）
const studentInfo = ref<StudentInfo>({ ...(props.student || {}) })
const student = computed(() => studentInfo.value || {})
const courses = ref([] as any)
const loading = ref(false)
// 允许通过 props 传入或通过方法注入虚拟锚点
const internalVirtualRef = ref<any>(null)
const virtualRef = computed(() => props.virtualRef ?? internalVirtualRef.value)
const tagsRef = ref<HTMLElement | null>(null)
const tagsOverflow = ref(false)
const lastLoadedStudentId = ref<string>('')
const cancelCurrentRequest = ref<null | (() => void)>(null)
const requestSequence = ref(0)

const popperOptions = computed(() => ({
    modifiers: [{
        name: 'eventListeners',
        enabled: !props.preventAutoHide
    }]
}))

async function loadingStudentDetail(){
    if (!student.value?.ID) { courses.value = []; return }

    const mySeq = ++requestSequence.value
    const myStudentId = String(student.value.ID)

    loading.value = true
    try{
        // 取消上一个未完成请求
        if (cancelCurrentRequest.value) {
            cancelCurrentRequest.value()
            cancelCurrentRequest.value = null
        }
        cancelCurrentRequest.value = useSyncCancel()

        const res: any = await getStudentCardDetail({ StudentID: student.value.ID })
        // 仅当仍然是最新请求且学员ID未被切换时才写入
        if (mySeq === requestSequence.value && myStudentId === lastLoadedStudentId.value) {
            const data = res?.Data ||{}
            const list = data.ShiftMoney || []
            courses.value = cloneDeep(list)
            studentInfo.value = { ...studentInfo.value, ...(data as any) }
        }
    } catch (error) {
        // 请求被取消时不回写数据
        if ((error as any)?.name !== 'AbortError') {
            console.error('加载学员详情失败:', error)
            if (mySeq === requestSequence.value && myStudentId === lastLoadedStudentId.value) {
                // 保持原有数据，不重置
            }
        }
    } finally {
        if (mySeq === requestSequence.value) {
            loading.value = false
            cancelCurrentRequest.value = null
        }
    }
}

function show(){
    visible.value = true
}

function showWithStudent(studentData: StudentInfo, virtualEl?: any){
    // 直接设置学员信息，不触发响应式更新
    studentInfo.value = { ...studentInfo.value, ...studentData }
    // 若传入了锚点，则使用该锚点定位
    if (virtualEl) {
        internalVirtualRef.value = virtualEl
    }
    visible.value = true
}
function hide(){
    if (!props.preventAutoHide) {
        visible.value = false
    } else {
        // 阻止自动隐藏
        visible.value = true
    }
}

async function handleShow(){
    // 优先使用通过 showWithStudent 传入并已写入的 studentInfo；若未提供则回退到 props
    const effectiveStudent = (studentInfo.value && Object.keys(studentInfo.value).length > 0)
        ? studentInfo.value
        : (props.student || {})

    const currentStudentId = String((effectiveStudent as any)?.ID || '')

    // 如果当前学员ID与上次加载的相同，且已有数据，则不重新请求
    if (
        currentStudentId &&
        lastLoadedStudentId.value === currentStudentId &&
        courses.value.length > 0
    ) {
        emit('show')
        return
    }

    // 确保本地 studentInfo 有效数据
    studentInfo.value = { ...studentInfo.value, ...(effectiveStudent as any) }
    courses.value = []

    // 如果有学员ID，则加载课程详情
    if (currentStudentId) {
        lastLoadedStudentId.value = currentStudentId
        await loadingStudentDetail()
    }

    emit('show')
}
function handleHide(){
    if (!props.preventAutoHide) {
        // 隐藏时中断当前请求，避免状态回写
        if (cancelCurrentRequest.value) {
            cancelCurrentRequest.value()
            cancelCurrentRequest.value = null
        }
    }
    emit('hide')
}

const computedTags = computed(() => (student.value as any)?.StudentLabel || [])

// 同步外部传入的学员信息到本地合并对象
watch(() => props.student, (nv) => {
    if (nv && typeof nv === 'object') {
        studentInfo.value = { ...(nv as any) }
        courses.value = []
    }
}, { deep: true, immediate: true })

// 监测标签是否溢出，决定是否显示省略效果
watch([() => computedTags.value, () => visible.value], () => {
    try {
        const el = tagsRef.value as HTMLElement | null
        if (!el) { tagsOverflow.value = false; return }
        // 下一帧测量，避免初次渲染尺寸不准
        requestAnimationFrame(() => {
            try {
                tagsOverflow.value = el.scrollWidth > el.clientWidth + 1
            } catch (_) { tagsOverflow.value = false }
        })
    } catch (_) { tagsOverflow.value = false }
}, { deep: true })

defineExpose({ show, hide, showWithStudent, popoverRef })

// 防止在需要阻止自动隐藏时因外部点击被隐藏
watch(visible, (newVal, oldVal) => {
    if (!newVal && oldVal && props.preventAutoHide) {
        visible.value = true
    }
}, { flush: 'sync' })
</script>

<style scoped lang="scss">
.wtwo-student-popover {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    min-width: 360px;
}

.wtwo-student-popover__header {
    padding: 12px 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #EAF3FF;
}

.wtwo-student-popover__user {
    display: flex;
    align-items: center;
    min-width: 0;
}

.wtwo-student-popover__avatar {
    width: 42px;
    height: 42px;
    border-radius: 4px;
    overflow: hidden;
    background: #f2f3f5;
    flex-shrink: 0;
}

.wtwo-student-popover__avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.wtwo-student-popover__avatar.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
    color: #fff;
    font-weight: 500;
}

.wtwo-student-popover__meta {
    margin-left: 10px;
    min-width: 0;
}

.wtwo-student-popover__name {
    font-size: 16px;
    font-weight: 600;
    line-height: 22px;
    color: #303133;
}

.wtwo-student-popover__tags {
    margin-top: 6px;
    display: flex;
    gap: 6px;
    flex-wrap: nowrap;
    overflow: hidden;
    position: relative;
    padding-right: 16px; /* 给省略区域留出空间 */
}
.wtwo-student-popover__tags::after {
    content: ' ';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    padding-left: 30px;
    background: linear-gradient(90deg, rgba(234,243,255,0), #EAF3FF 65%);
}

/* 未溢出时不显示省略效果 */
.wtwo-student-popover__tags:not(.is-overflow)::after {
    display: none;
}

.wtwo-student-popover__tag {
    background: #FFFFFF;
    flex-shrink: 0;
    color: #606266;
    padding: 1px 8px;
    border-radius: 4px;
    font-size: 12px;
    line-height: 20px;
}

.wtwo-student-popover__info {
    padding: 12px 16px 0;
}

.wtwo-student-popover__row {
    display: flex;
    margin-bottom: 8px;
}

.wtwo-student-popover__label {
    color: #909399;
    flex-shrink: 0;
}

.wtwo-student-popover__value {
    color: #303133;
    flex: 1;
    min-width: 0;
}

.wtwo-student-popover__course-list {
    padding: 0 16px 12px;
    max-height: 220px;
    overflow: auto;
}

.wtwo-student-popover__item {
    background: #F6F6F7;
    border-radius: 8px;
    padding: 10px 12px;
    margin-bottom: 8px;
    .wtwo-student-popover__item-tag{
        background: #fff;
        border-radius: 4px;
        border: 1px solid #F4F4F5;
        padding: 2px 8px;
        font-size: 12px;
        color: #606266;
        margin-left: 6px;
        flex-shrink: 0;
    }
}

.wtwo-student-popover__title {
    font-weight: 400;
    color: #303133;
    max-width: 220px;
}

.wtwo-student-popover__sub {
    color: #606266;
    font-size: 12px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    .wtwo-student-popover__sub-item{
        width: calc(100% / 3 - 20px);
        flex-shrink: 0;
        line-height:20px;
    }
}

.wtwo-student-popover__strong {
    color: #303133;
    display: flex;
    align-items: center;
    justify-content: center;
}
.wtwo-student-popover__label{
    text-align: center;
}

.wtwo-student-popover__split {
    color: #E5E6EB;
    margin: 0 2px;
}

.wtwo-student-popover__empty {
    color: #909399;
    text-align: center;
    padding: 12px 0;
}
</style>

<style lang="scss">
.wtwo-student-detail-popover {
    padding: 0 !important;
    border: none !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;

    .el-popper__arrow {
        display: none !important;
    }
}
</style>


