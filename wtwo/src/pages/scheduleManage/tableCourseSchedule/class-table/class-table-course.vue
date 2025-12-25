<template>
    <div :class="{ 'fullscreen-container': isFullscreen, 'h-100%': true }">
        <!-- 操作栏 - 始终显示 -->
        <div class="operation-bar">
            <div class="left-section">
                <el-select id="class-table-course-select" class="class-table-course-select" v-model="selectedTableType"
                    :placeholder="isDraftLoading ? '加载中...' : '请选择'" style="width: 140px; margin-right: 20px;"
                    :loading="isDraftLoading">
                    <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value">
                        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                            <span>{{ item.label }}</span>
                            <el-tooltip :content="item.tooltip" placement="top">
                                <el-icon style="font-size: 16px; color: #909399; margin-left: 4px;">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-xinxitishi"></use>
                                    </svg>
                                </el-icon>
                            </el-tooltip>
                        </div>
                    </el-option>
                </el-select>
                <div class="pre-check-switch-container">
                    <el-switch v-model="preCheckEnabled" size="small" active-text="预检查冲突与限制" inactive-text=""
                        :disabled="isDraftLoading" @change="handlePreCheckChange" />
                    <div v-if="preCheckEnabled && preCheckTotalCount > 0" class="pre-check-stats">
                        <span class="total-count">（{{ preCheckTotalCount }}）</span>
                        <span class="passed-count">通过 ({{ preCheckPassedCount }})</span>
                        <span class="failed-count">不通过 ({{ preCheckFailedCount }})</span>
                    </div>
                    <el-checkbox v-if="preCheckEnabled" v-model="showOnlyAbnormal" label="仅显示异常项"
                        style="margin-left: 20px;" />
                </div>
            </div>
            <div class="right-section">
                <div class="auto-save-status" :class="saveStatus">
                    <el-icon v-if="saveStatus === 'idle' && autoSaveTime" class="success-icon">
                        <svg aria-hidden="true">
                            <use xlink:href="#w2-tongguo"></use>
                        </svg>
                    </el-icon>
                    <el-icon v-else-if="saveStatus === 'saving'" class="loading-icon">
                        <svg aria-hidden="true">
                            <use xlink:href="#w2-caogaobaocunzhong"></use>
                        </svg>
                    </el-icon>
                    <el-icon v-else-if="saveStatus === 'success'" class="success-icon">
                        <svg aria-hidden="true">
                            <use xlink:href="#w2-tongguo"></use>
                        </svg>
                    </el-icon>
                    <span v-if="saveStatus === 'idle' && autoSaveTime" >已自动保存 {{ autoSaveTime }}</span>
                    <span v-else-if="saveStatus === 'saving'">草稿保存中...</span>
                    <span v-else-if="saveStatus === 'success'">已自动保存 {{ autoSaveTime }}</span>
                </div>
                <el-popover v-model:visible="groupPopoverVisible" placement="bottom" :width="200" trigger="click"
                    popper-class="custom-group-popover">
                    <template #reference>
                        <el-button text :type="isGrouped ? 'primary' : ''" class="w60px">
                            <template #icon>
                                <el-icon size="16px">
                                    <svg aria-hidden="true" :style="{ color: isGrouped ? '#2878E8' : '#606266' }">
                                        <use xlink:href="#w2-fenzu"></use>
                                    </svg>
                                </el-icon>
                            </template>
                            {{ groupButtonText }}
                        </el-button>
                    </template>

                    <div class="group-popover-content">
                        <div class="group-header">
                            <div class="group-title">选择分组条件</div>
                            <a class="cancel-group" v-if="groupByField" @click="handleGroupClear">取消分组</a>
                        </div>
                        <el-divider />
                        <ul class="group-list">
                            <li v-for="field in computedGroupFieldOptions" :key="field.value" class="group-item"
                                :class="{ active: groupByField === field.value }"
                                @click="handleGroupFieldChange(field.value)">
                                <span>{{ field.label }}</span>
                                <el-icon v-if="groupByField === field.value" class="selected-icon">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-xuanzhong"></use>
                                    </svg>
                                </el-icon>
                            </li>
                        </ul>
                    </div>
                </el-popover>
                <!-- 删除确认 Popover -->
                <el-popover placement="bottom" :width="300" trigger="click" popper-class="delete-confirm-popover"
                    ref="deletePopoverRef">
                    <template #reference>
                        <el-button text class="w60px flex-center" @click="onDeleteButtonClick">
                            <template #icon>
                                <el-icon size="18px" class="table-delete">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-shanchu"></use>
                                    </svg>
                                </el-icon>
                            </template>
                            <span class="table-delete">删除</span>
                        </el-button>
                    </template>
                    <div class="delete-popover-content">
                        <div class="delete-message">
                            <el-icon class="color-#2878E8! text-14px! scale-114.285714">
                                <InfoFilled />
                            </el-icon>
                            <span>已选择 <span class="delete-count">{{ checkedRowsCount }}</span> 条排课，确认要删除吗？</span>
                        </div>
                        <div class="delete-actions">
                            <el-button size="small" @click="cancelDelete">取消</el-button>
                            <el-button size="small" type="primary" @click="confirmDelete">确认删除</el-button>
                        </div>
                    </div>
                </el-popover>

                <el-tooltip :content="isFullscreen ? '取消全屏' : '全屏'" placement="top">
                    <el-button plain class="w32px h32px" @click="toggleFullscreen">
                        <template #icon>
                            <el-icon size="20px">
                                <svg aria-hidden="true">
                                    <use v-if="!isFullscreen" xlink:href="#w2-quanjufangda"></use>
                                    <use v-else="isFullscreen" xlink:href="#w2-quanjusuoxiao"></use>
                                </svg>
                            </el-icon>
                        </template>
                    </el-button>
                </el-tooltip>

                <el-button type="primary" @click="startPublishing">
                    开始排课
                </el-button>
            </div>
        </div>

        <!-- 骨架屏 - 只显示表格部分 -->
        <div v-if="isDraftLoading" class="skeleton-table-wrapper">
            <!-- 表格头部骨架 -->
            <div class="skeleton-table-header">
                <el-skeleton-item variant="text" style="width: 8%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 15%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 12%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 10%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 15%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 12%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 10%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 10%; height: 40px;" />
                <el-skeleton-item variant="text" style="width: 8%; height: 40px;" />
            </div>
            
            <!-- 表格内容骨架 -->
            <div class="skeleton-table-body">
                <el-skeleton animated :throttle="100">
                    <template #template>
                        <div class="skeleton-table-row" v-for="i in 10" :key="i">
                            <el-skeleton-item variant="text" style="width: 8%;" />
                            <el-skeleton-item variant="text" style="width: 15%;" />
                            <el-skeleton-item variant="text" style="width: 12%;" />
                            <el-skeleton-item variant="text" style="width: 10%;" />
                            <el-skeleton-item variant="text" style="width: 15%;" />
                            <el-skeleton-item variant="text" style="width: 12%;" />
                            <el-skeleton-item variant="text" style="width: 10%;" />
                            <el-skeleton-item variant="text" style="width: 10%;" />
                            <el-skeleton-item variant="text" style="width: 8%;" />
                        </div>
                    </template>
                </el-skeleton>
            </div>
        </div>

        <base-table v-else id="loading-container" :class="{
            'click-group-row': clickGroupRow
        }" ref="tableRef" :columns="columns" style="width: 100%;max-height: calc(100% - 65px);"
            :table-data="displayTableData" :footer-data="footerData" :edit-option="editOption"
            :virtual-scroll-option="virtualScrollOption" :row-style-option="rowStyleOption"
            :cell-autofill-option="cellAutofillOption" :event-custom-option="eventCustomOption"
            :cell-selection-option="cellSelectionOption" :loading-option="loadingOption"
            :cell-style-option="cellStyleOption" :cell-span-option="cellSpanOption" :sort-option="sortOption"
            :clipboard-option="clipboardOption" :column-hidden-option="columnHiddenOption" fixed-header
            :scroll-width="1600" :column-width-resize-option="columnWidthResizeOption"
            :max-height="isFullscreen ? 'calc(100vh - 80px)' : '100%'" border-y border-x row-key-field-name="ID" />

        <!-- 预检查对话框 -->
        <CheckCourseDraftDialog ref="checkCourseDraftDialogRef" />

        <!-- 按规则批量新增弹框 -->
        <AddArrangeByRule ref="addArrangeByRuleRef" />

        <!-- 校验错误弹窗 -->
        <ValidationErrorDialog v-model="validationErrorDialogVisible" :errors="validationErrors"
            @goToRow="handleGoToRowFromDialog" @update:modelValue="handleValidationDialogVisibilityChange" />

        <!-- 预检查详情弹框 -->
        <PreCheckDetailsDialog ref="preCheckDetailsDialogRef" :current-row="currentPreCheckRow"
            :pre-check-data="currentPreCheckData" :table-type="selectedTableType"
            @go-to-modify="handleGoToModifyFromDialog" />

        <!-- 开始排课主弹框 -->
        <CourseDraftPublishDialog ref="courseDraftPublishDialogRef" v-model:visible="courseDraftPublishVisible"
            :pre-check-enabled="preCheckEnabled" :total-count="publishTotalCount" :passed-count="publishPassedCount"
            :failed-count="publishFailedCount" @confirm="handlePublishConfirm" @cancel="handlePublishCancel"
            @complete="handlePublishComplete" />

        <!-- 排课异常处理弹框 -->
        <PublishExceptionDialog ref="publishExceptionDialogRef" v-model:visible="publishExceptionVisible"
            :restriction-count="publishRestrictionCount" :restriction-details="publishRestrictionDetails"
            :conflict-count="publishConflictCount" :conflict-details="publishConflictDetails"
            @continue-partial="handleContinuePartialPublish" @return-modify="handleReturnModifyFromPublish" />

        <!-- 实时显示tableData -->
        <!-- <div v-if="!isFullscreen && tableData.length > 0"
            style="margin-top: 20px; padding: 15px; background-color: #f5f5f5; border-radius: 4px;">
            <h3 style="margin: 0 0 10px 0; color: #333;">tableData 实时数据：</h3>
            <pre style="margin: 0; font-size: 12px; color: #666; white-space: pre-wrap; word-break: break-all;">{{
                JSON.stringify(tableData, null, 2) }}</pre>
        </div> -->
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'

// 定义 emit
const emit = defineEmits(['component-ready'])

// 安全的JSON转换方法
const safeJsonClone = (context = 'unknown') => {
    try {
        if (!tableData.value) {
            return tableData.value
        }

        // 检查循环引用
        const seen = new WeakSet()
        const checkCircular = (item) => {
            if (item && typeof item === 'object') {
                if (seen.has(item)) {
                    throw new Error(`Circular reference detected in ${context}`)
                }
                seen.add(item)
            }
        }
        checkCircular(tableData.value)

        return JSON.parse(JSON.stringify(tableData.value))
    } catch (error) {
        console.error(`JSON转换失败 [${context}]:`, tableData.value, error)
        // 使用Logger.error上报错误到Sentry
        Logger.error("JSON转换失败", {
            error: error,
            context: context,
            dataType: typeof tableData.value,
            isArray: Array.isArray(tableData.value),
            dataLength: tableData.value?.length,
            component: 'class-table-course',
            operation: 'json-clone'
        })

        // 降级处理
        if (Array.isArray(tableData.value)) {
            console.warn(`JSON转换失败，返回空数组 [${context}]`)
            return []
        } else if (typeof tableData.value === 'object' && tableData.value !== null) {
            console.warn(`JSON转换失败，返回空对象 [${context}]`)
            return {}
        } else {
            console.warn(`JSON转换失败，返回原值 [${context}]`)
            return tableData.value
        }
    }
}
import { h } from 'vue'
import {
    ElDatePicker,
    ElSelect,
    ElOption,
    ElButton,
    ElIcon,
    ElPopover,
    ElSwitch,
    ElCheckbox,
    ElMessage,
    ElLoading,
    ElInput,
} from 'element-plus'
import {
    InfoFilled,
} from '@element-plus/icons-vue'
import BaseTable from '@/components/common/base-table/baseTable.vue'
import ValidationErrorDialog from '../components/validation-error/validation-error-dialog.vue'
import CampusSelect from '@/components/business/select/campus-select.vue'
import ClassSelect from '@/components/business/select/class-select.vue'
import CourseSelect from '@/components/business/select/course-select.vue'
import StudentCourseSelect from '@/components/business/select/student-course-select.vue'
import SubjectSelect from '@/components/business/select/subject-select.vue'
import TimeSelect from '@/components/business/select/time-select.vue'
import ClassroomSelect from '@/components/business/select/classroom-select.vue'
import TeacherSelect from '@/components/business/select/teacher-select.vue'
import AssistantSelect from '@/components/business/select/assistant-select.vue'
import StudentSelect from '@/components/business/select/student-select.vue'
import { useConfigs, useUserCampuses } from '@/store'
import { useDictFieldsStore } from '@/store/dict'
import { getFieldDisplayName, getFieldType, getGroupFieldOptionsByTableType } from './fieldMapping'
import { BatchSaveCourseDraft, GetCourseDraftList, DeleteCourseDraft, CheckCourseDraft, CourseDraftPublishDraft } from '@/api/arrange'
import Logger from '@/utils/sentry/sentry'
import { useTableData } from '../composables/useTableData'
import { fieldFormatCheck, fieldMap, validateIDField } from '../composables/useDataValidation'
import { useValidation } from '../composables/useValidation'
import { useTableScroll } from '../composables/useTableScroll'
import { isUUID, generateUUID } from '@/utils/uuid/uuid'
import { querySysConfig } from '@/api'
import { renderHeaderWithStar, clearRelatedFields, getSubjectName, calculateCellCount, getClipboardVisibleColumns, applySubjectChange } from '../composables/useTableUtils'
import CheckCourseDraftDialog from '../components/check-course-draft/check-course-draft-dialog.vue'
import AddArrangeByRule from '../../popup/addArrangeByRule.vue'
import PreCheckDetailsDialog from '../../popup/PreCheckDetailsDialog.vue'
import CourseDraftPublishDialog from '../../popup/CourseDraftPublishDialog.vue'
import PublishExceptionDialog from '../../popup/PublishExceptionDialog.vue'
import SubscribeCourseSelect from '@/components/business/select/subscribe-course-select.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
// ==================== 分组相关状态 ====================
const isGrouped = ref(false)                    // 是否启用分组（默认关闭）
const groupByField = ref('')                    // 按哪个字段分组（默认不选择）
const expandedGroups = ref(new Set())           // 展开的分组集合（默认不展开任何分组）
const groupedDataCache = ref(null)     // 分组数据缓存

const ClassCourse = window.$xgj.op('NewCourse_ClassCourse')
const StudentCourse = window.$xgj.op('NewCourse_StudentCourse')
const SubscribeCourse = window.$xgj.op('NewCourse_SubscribeCourse')
const customTrips=transToConfigDescript('任课老师和助教不能重复');
// 班级选项
const classOptions = ref([])
if(ClassCourse){
    classOptions.value.push({ label: transToConfigDescript('给班级排课'), value: 10, tooltip: transToConfigDescript('为集体班，一对多班级排课') })
}
if(StudentCourse){
    classOptions.value.push({ label: '给学员排课', value: 20, tooltip: '为一对一学员排课' })
}
if(SubscribeCourse){
    classOptions.value.push({ label: '排预约课', value: 30, tooltip: '可将排课，开放至学员端预约' })
}
// 操作栏数据
const selectedTableType = ref(Number(localStorage.getItem('wtwo_selectedTableType') || classOptions.value[0]?.value))
const { tableData, originalTableData, cellDataChange, handleAddTenRows, sortOption, initTableData } = useTableData({
    isGrouped: isGrouped,
    groupedDataCache: groupedDataCache,
    selectedTableType: selectedTableType,
})
// ==================== 响应式数据 ====================
const configs = computed(() => {
    return useConfigs().configs
})
const deletePopoverRef = ref(null)

// 使用校验相关的 composable
const { validateRequiredFields, recalculateValidationErrors } = useValidation()

// 使用表格滚动功能
const { scrollToRow, scrollToFirstErrorRow } = useTableScroll()

// ==================== 🆕 任课老师和助教重复校验 ====================
/**
 * 校验任课老师和助教是否重复
 * @param {Object} row - 表格行数据
 * @returns {Object} { isValid: boolean, errorFields: string[], errorMessage: string }
 */
const validateTeacherAssistantDuplicate = (row) => {
    // 1. 跳过特殊行
    if (row.isGroupRow || row.isGroupFooter) {
        return { isValid: true, errorFields: [], errorMessage: '' }
    }

    // 2. 获取任课老师ID
    const mainTeacherId = row.MainTeacherID?.trim()
    if (!mainTeacherId) {
        return { isValid: true, errorFields: [], errorMessage: '' }
    }

    // 3. 获取助教ID列表
    const assistantIds = row.AssistantTeacherID
        ? row.AssistantTeacherID.split(',')
            .map(id => id.trim())  // 去除空格
            .filter(Boolean)        // 过滤空字符串
        : []
    if (assistantIds.length === 0) {
        return { isValid: true, errorFields: [], errorMessage: '' }
    }

    // 4. 检查是否有重复
    const isDuplicate = assistantIds.includes(mainTeacherId)

    if (isDuplicate) {
        return {
            isValid: false,
            errorFields: ['MainTeacherID', 'AssistantTeacherID'],
            errorMessage: customTrips // '任课老师和助教不能重复'
        }
    }

    return { isValid: true, errorFields: [], errorMessage: '' }
}

/**
 * 🆕 批量触发任课老师与助教重复校验
 * @param {Array} rows - 要校验的行数组
 * @param {boolean} useNextTick - 是否使用 nextTick 延迟执行（默认 true）
 */
const batchValidateTeacherAssistant = (rows, useNextTick = true) => {
    if (!rows || rows.length === 0) return

    const executeValidation = () => {
        rows.forEach(row => {
            if (row && !row.isGroupRow && !row.isGroupFooter) {
                const validationResult = validateTeacherAssistantDuplicate(row)
                applyValidationResult(row, validationResult)
            }
        })
    }

    if (useNextTick) {
        nextTick(executeValidation)
    } else {
        executeValidation()
    }
}

/**
 * 🆕 检查指定列范围是否包含任课老师或助教字段
 * @param {number} startColIndex - 起始列索引
 * @param {number} endColIndex - 结束列索引
 * @param {Array} visibleColumns - 可见列数组
 * @returns {boolean} 是否包含教师字段
 */
const hasTeacherFieldsInColumns = (startColIndex, endColIndex, visibleColumns) => {
    for (let colIndex = startColIndex; colIndex <= endColIndex; colIndex++) {
        const column = visibleColumns[colIndex]
        if (column && (column.field === 'MainTeacherID' || column.field === 'AssistantTeacherID')) {
            return true
        }
    }
    return false
}

/**
 * 应用校验结果到行数据
 * @param {Object} row - 表格行数据
 * @param {Object} validationResult - 校验结果
 */
const applyValidationResult = (row, validationResult) => {
    if (!row.errorField) {
        row.errorField = []
    }
    if (!row.errorMessages) {
        row.errorMessages = {}
    }

    if (!validationResult.isValid) {
        // 添加错误字段（去重，与现有错误合并）
        const existingErrors = new Set(row.errorField)
        validationResult.errorFields.forEach(field => {
            existingErrors.add(field)
        })
        row.errorField = Array.from(existingErrors)

        // 添加错误信息
        validationResult.errorFields.forEach(field => {
            row.errorMessages[field] = validationResult.errorMessage
        })
    } else {
        // ✅ 重要：当校验通过时，精确清除任课老师和助教的重复错误标记
        // 只清除"任课老师和助教不能重复"相关的错误，保留其他错误（如必填校验等）

        if (row.errorField && row.errorField.length > 0) {
            // 检查是否有"任课老师和助教不能重复"的错误
            const hasDuplicateError =
                row.errorMessages['MainTeacherID'] === customTrips ||
                row.errorMessages['AssistantTeacherID'] === customTrips

            if (hasDuplicateError) {
                // 清除重复错误的错误信息
                if (row.errorMessages['MainTeacherID'] === customTrips) {
                    delete row.errorMessages['MainTeacherID']
                }
                if (row.errorMessages['AssistantTeacherID'] === customTrips) {
                    delete row.errorMessages['AssistantTeacherID']
                }

                // 从 errorField 中移除字段（只有当该字段没有其他错误时）
                // ✅ 使用新数组引用强制触发 Vue 响应式更新
                const newErrorField = row.errorField.filter(field => {
                    if (field === 'MainTeacherID' || field === 'AssistantTeacherID') {
                        // 检查该字段是否还有其他错误信息
                        return row.errorMessages[field] !== undefined
                    }
                    return true
                })
                row.errorField = [...newErrorField]

                // ✅ 同步更新 validationErrors 数组
                const errorIndex = validationErrors.value.findIndex(error => error.rowId === row.ID)
                if (errorIndex !== -1) {
                    // 移除已清除的字段（MainTeacherID 和 AssistantTeacherID）
                    const currentError = validationErrors.value[errorIndex]
                    if (currentError.errorFields) {
                        currentError.errorFields = currentError.errorFields.filter(
                            field => field !== 'MainTeacherID' && field !== 'AssistantTeacherID'
                        )
                    }

                    // 如果该行没有其他错误字段了，从 validationErrors 中删除这一行
                    if (!currentError.errorFields || currentError.errorFields.length === 0) {
                        validationErrors.value.splice(errorIndex, 1)
                        validationErrorFields.value.delete(row.ID)

                        // ✅ 同时从 validationErrorRowIds 中移除该行ID
                        const rowIdIndex = validationErrorRowIds.value.indexOf(row.ID)
                        if (rowIdIndex !== -1) {
                            validationErrorRowIds.value.splice(rowIdIndex, 1)
                        }
                    } else {
                        // 否则更新错误字段映射
                        validationErrorFields.value.set(row.ID, currentError.errorFields)
                    }
                }

                // 触发显示更新
                updateValidationErrorsDisplay()
            }
        }
    }
}

// 计算属性：判断是否显示上课科目列
const shouldShowSubjectColumn = computed(() => {
    // 过滤掉分组行和footer行，只检查真实数据行
    const dataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter)
    return dataRows.some(row => row.EnableSubject == '1')
})

// 🆕 列隐藏配置 - 根据表格类型和状态动态隐藏字段
const columnHiddenOption = computed(() => {
    const hiddenKeys = []

    // 科目列：根据业务规则决定是否显示
    if (!shouldShowSubjectColumn.value) {
        hiddenKeys.push('SubjectID')
    }

    // 预检查列：关闭预检查时隐藏
    if (!preCheckEnabled.value) {
        hiddenKeys.push('preCheckStatus')
    }

    // 🆕 根据表格类型隐藏不相关的字段
    const tableType = selectedTableType.value

    if (tableType === 10) {
        // 给班级排课：隐藏学员相关字段和预约课字段
        hiddenKeys.push('StudentUserID', 'MaxStudentCount', 'StartStudentCount')
    } else if (tableType === 20) {
        // 给学员排课：隐藏班级相关字段和预约课字段
        hiddenKeys.push('ClassID', 'MaxStudentCount', 'StartStudentCount')
    } else if (tableType === 30) {
        // 排预约课：隐藏班级和学员字段
        hiddenKeys.push('ClassID', 'StudentUserID')
    }

    // console.log(`📊 表格类型 ${tableType} 的隐藏字段:`, hiddenKeys)

    return {
        defaultHiddenColumnKeys: hiddenKeys
    }
})



// 表格引用
const tableRef = ref(null)
// 弹出框引用
const addMenuPopoverRef = ref(null)
const addMenuPopoverFooterRef = ref(null)
// 组件引用映射
const componentRefs = ref(new Map())
// 双击单元格标识 - 存储当前编辑的单元格信息
const cellDbClickKey = ref(null)
// 加载实例
const loadingInstance = ref(null)
// 开始行索引
const startRowIndex = ref(0)
const autoSaveTime = ref('')
// 草稿列表加载状态
const isDraftLoading = ref(true)
// 全屏状态
const isFullscreen = ref(false)
// 分组popover相关状态
const groupPopoverVisible = ref(false) // 分组popover显示状态
// 预检查弹框引用
const checkCourseDraftDialogRef = ref()
// 按规则批量新增弹框引用
const addArrangeByRuleRef = ref()
// 预检查详情弹框引用（Promise 风格 open）
const preCheckDetailsDialogRef = ref()
// 预检查开关状态
const preCheckEnabled = ref(false)

// 开始排课相关状态
const courseDraftPublishDialogRef = ref()
const publishExceptionDialogRef = ref()
const courseDraftPublishVisible = ref(false)
const publishExceptionVisible = ref(false)
// 新增：仅显示异常项的复选框状态
const showOnlyAbnormal = ref(false)
// 新增：预检查统计
const preCheckTotalCount = ref(0)
const preCheckPassedCount = ref(0)
const preCheckFailedCount = ref(0)

// 排课数据统计
const publishTotalCount = ref(0)
const publishPassedCount = ref(0)
const publishFailedCount = ref(0)

// 排课异常详情
const publishRestrictionCount = ref(0)
const publishRestrictionDetails = ref([])
const publishConflictCount = ref(0)
const publishConflictDetails = ref([])

// 排课流程状态
const publishInProgress = ref(false)
const lastSubmittedIds = ref([])
const failedIdsFromPublish = ref(new Set())
const isRetryForPassedOnly = ref(false)

// 统一数据量上限控制
const MAX_TABLE_ROWS = 3000
function getDataRowCount() {
    if (!tableData.value) return 0;
    return tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter).length
}
function enforceMaxCapacity(sourceLabel = '') {
    const dataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter)
    if (dataRows.length <= MAX_TABLE_ROWS) return
    const allowedIds = new Set(dataRows.slice(0, MAX_TABLE_ROWS).map(r => r.ID))
    tableData.value = tableData.value.filter(row => row.isGroupRow || row.isGroupFooter || allowedIds.has(row.ID))
    originalTableData.value = safeJsonClone('enforce-max-capacity')
    if (groupedDataCache.value) groupedDataCache.value.isValid = false
    const trimmed = dataRows.length - MAX_TABLE_ROWS
    if (trimmed > 0) {
        ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，本次超出${trimmed}条，已自动截断`)
    }
}
function getRemainingCapacity() {
    return Math.max(0, MAX_TABLE_ROWS - getDataRowCount())
}
// 预检查结果数据 - 存储每行的检查结果
const preCheckResults = ref(new Map()) // { rowId: PreCheckResultData }
// 本轮参与预检查的草稿ID集合（用于区分未参与检测的行）
const preCheckedIds = ref(new Set())
// 正在检查中的行ID集合（用于显示"检查中..."状态）
const checkingIds = ref(new Set())
// 预检查详情弹框状态
// 已改为通过 ref.open() 控制显隐，无需本地可见性状态
const currentPreCheckData = ref(null)
const currentPreCheckRow = ref(null)
// 校验错误弹窗状态
const validationErrorDialogVisible = ref(false)
const validationErrors = ref([])
// 校验错误行ID列表
const validationErrorRowIds = ref([])
// 校验错误字段映射 - 存储每行的错误字段信息 { rowId: [fieldName1, fieldName2] }
const validationErrorFields = ref(new Map())
// ==================== 草稿保存相关状态 ====================
// 🆕 优化后的双缓冲变更收集器
// 当前正在收集变更的收集器 - 存储所有字段变更 { rowId: { fieldName: { value: newValue, timestamp: Date.now() } } }
const activeChangeCollector = ref(new Map())
// 待保存的变更收集器（保存期间的缓冲区）
const pendingChangeCollector = ref(new Map())

// 🆕 并发控制
// 保存并发锁 - 防止重复保存
const isSaving = ref(false)
// 保存队列 - 确保保存任务串行执行
const saveQueue = ref([])

// ==================== 编辑状态保护相关 ====================
// 正在编辑的单元格集合 - 存储格式 "rowId_fieldName"
const editingCells = ref(new Set())
// 当前聚焦的单元格 - 存储格式 "rowId_fieldName"
const activeFocusedCell = ref(null)
// 编辑单元格时间戳 - 记录编辑开始时间 { "rowId_fieldName": timestamp }
const editingCellTimestamps = ref(new Map())
// 🆕 UI 交互锁 - 当有弹框/下拉框打开时，暂停数据更新
const uiInteractionLock = ref(false)
// 🆕 待处理的保存响应队列 - 存储被锁住时到达的保存响应
const pendingSaveResponses = ref([])
// 🆕 锁超时定时器 - 防止锁泄漏（用户按ESC、点击遮罩等情况）
const lockTimeout = ref(null)

// 防抖计时器 - 控制保存时机
const saveDebounceTimer = ref(null)

// 保存状态 - 'idle' | 'saving' | 'success' | 'error'
const saveStatus = ref('idle')

// 兜底检查定时器 - 定期检查是否有未保存的变更
const backupCheckTimer = ref(null)

// 🆕 保存统计信息
const saveStats = ref({
    totalSaves: 0,
    successSaves: 0,
    failedSaves: 0,
    lastSaveTime: null,
    averageSaveTime: 0
})

// 🆕 虚拟滚动优化
const lastScrollRange = ref({ start: -1, end: -1 }) // 记录上次滚动的范围


// 监听selectedTableType变化，自动保存到localStorage并重新加载草稿
watch(selectedTableType, async (newValue) => {
    if (newValue) {
        localStorage.setItem('wtwo_selectedTableType', newValue)

        // 检查当前分组字段是否在新表格类型中有效
        if (isGrouped.value && groupByField.value) {
            const validGroupFields = getGroupFieldOptionsByTableType(newValue)
            const isCurrentGroupFieldValid = validGroupFields.some(field => field.value === groupByField.value)

            if (!isCurrentGroupFieldValid) {
                // console.log(`⚠️ 分组字段 "${groupByField.value}" 在当前表格类型中无效，自动清除分组`)
                // 清除分组状态
                isGrouped.value = false
                groupByField.value = ''
                expandedGroups.value.clear()

                // 使分组缓存失效
                if (groupedDataCache.value) {
                    groupedDataCache.value.isValid = false
                }

                // 显示提示信息
                ElMessage.warning('表格类型已切换，原分组条件不适用，已自动清除分组')
            }
        }

        // 清空预检查相关数据并关闭预检查开关
        preCheckEnabled.value = false
        preCheckTotalCount.value = 0
        preCheckPassedCount.value = 0
        preCheckFailedCount.value = 0
        currentPreCheckRow.value = {}
        currentPreCheckData.value = {}
        preCheckResults.value.clear()
        preCheckedIds.value.clear()

        // 重新加载对应类型的草稿数据
        await loadCourseDraftList()
    }
})

/**
 * 🆕 通用的剪贴板数据收集函数
 * @param {Object} selectionRangeIndexes - 选择范围索引
 * @param {boolean} isCut - 是否为剪切操作
 */
const collectDataForClipboard = (selectionRangeIndexes, isCut) => {
    const hiddenKeys = columnHiddenOption.value?.defaultHiddenColumnKeys || []
    const visibleColumns = getClipboardVisibleColumns(columns, hiddenKeys)

    const columnFields = visibleColumns.map(v => v.field).filter((_, index) => {
        return index >= selectionRangeIndexes.startColIndex && index <= selectionRangeIndexes.endColIndex
    });

    // 根据操作类型选择数据源
    const dataSource = isCut ? displayTableData.value : displayTableData.value

    copyData.value = dataSource.filter((_, index) => {
        return index >= selectionRangeIndexes.startRowIndex && index <= selectionRangeIndexes.endRowIndex
    }).map(row => {
        const clipboardRow = { ID: row.ID }
        columnFields.filter(v => v).forEach(field => {
            // 收集ID字段和对应的Name字段
            clipboardRow[field] = row[field]
            clipboardRow[fieldMap[field]] = row[fieldMap[field]]
            if (field === 'MainTeacherID') {
                clipboardRow['MainTeacherList'] = [{
                    ID: row.MainTeacherID,
                    Name: row.MainTeacherName,
                    TeacherCommissionList: row.MainTeacherList && row.MainTeacherList.length > 0 ?
                        row.MainTeacherList[0].TeacherCommissionList : []
                }]
            }
            //  特殊处理：如果复制的是上课时间，额外收集 StartTime 和 EndTime
            if (field === 'timeRange') {
                clipboardRow['StartTime'] = row['StartTime']
                clipboardRow['EndTime'] = row['EndTime']
            }

        })
        return clipboardRow
    })

    // console.log(`📋 ${isCut ? '剪切' : '复制'}数据已收集:`, copyData.value)
}

/**
 * 记录字段变更到变更收集器
 * @param {string} rowId - 行ID
 * @param {string} fieldName - 字段名
 * @param {any} newValue - 新值
 * @param {string} source - 变更来源 ('edit', 'paste', 'autofill', 'batch', 'other')
 */
const recordFieldChange = (rowId, fieldName, newValue, source = 'other') => {
    // 🆕 使用当前活跃的收集器
    // 如果该行还没有变更记录，创建一个新的Map
    if (!activeChangeCollector.value.has(rowId)) {
        activeChangeCollector.value.set(rowId, new Map())
    }

    // 记录字段变更，包含值和时间戳
    const rowChanges = activeChangeCollector.value.get(rowId)
    rowChanges.set(fieldName, {
        value: newValue,
        timestamp: Date.now(),
        source: source
    })

    // 🆕 智能防抖：根据操作类型调整防抖时间
    resetSaveTimer(source)
}

/**
 * 记录新增行到变更收集器
 * @param {string} rowId - 新行的UUID
 * @param {object} rowData - 行数据
 * @param {string} source - 新增来源 ('add', 'addTen', 'batch')
 */
const recordNewRow = (rowId, rowData, source = 'add') => {
    // 🆕 使用当前活跃的收集器
    // 为新增行创建变更记录
    if (!activeChangeCollector.value.has(rowId)) {
        activeChangeCollector.value.set(rowId, new Map())
    }

    // 记录所有字段的初始值
    const rowChanges = activeChangeCollector.value.get(rowId)
    Object.keys(fieldMap).forEach(fieldName => {
        if (rowData[fieldName] !== undefined) {
            rowChanges.set(fieldName, {
                value: rowData[fieldName],
                timestamp: Date.now(),
                source: source,
            })
        }
    })
    rowChanges.set('IsNew', {
        value: true,
        timestamp: Date.now(),
        source: source,
    })

    // 🆕 智能防抖：根据操作类型调整防抖时间
    resetSaveTimer(source)
}

/**
 * 🆕 清除开放预约相关字段的校验错误
 * 当开放预约为"否"时，可约人数和开课人数不再是必填字段，需要清除它们的错误标记
 * @param {string} rowId - 行ID
 */
const clearSubscribeCourseRelatedErrors = (rowId) => {
    const rowErrorFields = validationErrorFields.value.get(rowId)
    if (rowErrorFields) {
        const fieldsToRemove = ['MaxStudentCount', 'StartStudentCount']
        const updatedErrorFields = rowErrorFields.filter(field => !fieldsToRemove.includes(field))
        
        if (updatedErrorFields.length === 0) {
            // 如果该行没有错误字段了，完全移除
            validationErrorFields.value.delete(rowId)
            // 同时从错误行ID列表中移除
            const errorRowIndex = validationErrorRowIds.value.indexOf(rowId)
            if (errorRowIndex > -1) {
                validationErrorRowIds.value.splice(errorRowIndex, 1)
            }
        } else {
            // 更新错误字段列表
            validationErrorFields.value.set(rowId, updatedErrorFields)
        }
    }
}


/**
 * 🆕 智能防抖保存计时器
 * @param {string} operationType - 操作类型 ('edit', 'paste', 'batch', 'add', 'addTen', 'other')
 */
const resetSaveTimer = (operationType = 'edit') => {
    // 清除之前的计时器
    if (saveDebounceTimer.value) {
        clearTimeout(saveDebounceTimer.value)
    }

    // 🆕 立即显示"保存中"状态，提升用户体验
    if (activeChangeCollector.value.size > 0) {
        saveStatus.value = 'saving'
    }

    // 🆕 智能防抖策略：根据操作类型调整防抖时间
    let debounceTime = 200 // 默认200ms

    // 批量操作使用更长的防抖时间，避免频繁保存
    if (operationType === 'batch' || operationType === 'paste' || operationType === 'addTen') {
        debounceTime = 500
    }
    // 单个编辑操作使用较短的防抖时间，提升响应速度
    else if (operationType === 'edit') {
        debounceTime = 200
    }
    // 新增操作使用中等防抖时间
    else if (operationType === 'add') {
        debounceTime = 300
    }

    // 设置新的防抖计时器
    saveDebounceTimer.value = setTimeout(() => {
        // 如果有待保存的变更，触发保存
        if (activeChangeCollector.value.size > 0) {
            enqueueSave()
        }
    }, debounceTime)
}

/**
 * 🆕 保存任务入队，确保串行执行
 */
const enqueueSave = () => {
    // 如果已经在保存中，直接返回
    if (isSaving.value) {
        // console.log('🔄 保存正在进行中，跳过本次保存请求')
        return
    }

    // 执行保存
    executeSave()
}

/**
 * 🆕 执行保存操作（优化后的版本）
 */
const executeSave = async () => {
    // 检查并发锁
    if (isSaving.value) {
        // console.log('🔒 保存被并发锁阻止')
        return
    }

    // 检查是否有待保存的变更
    if (activeChangeCollector.value.size === 0) {
        // console.log('📝 没有待保存的变更，跳过保存')
        saveStatus.value = 'idle'
        return
    }

    // 🆕 原子操作：交换收集器，防止保存期间丢失新变更
    const toSaveCollector = activeChangeCollector.value
    activeChangeCollector.value = new Map()

    // 设置并发锁和保存状态
    isSaving.value = true
    saveStatus.value = 'saving'

    const saveStartTime = Date.now()

    try {
        // 收集要保存的变更数据
        const changes = collectChangesFromCollector(toSaveCollector)
        if (changes.length === 0) {
            // console.log('📝 收集的变更数据为空，跳过保存')
            saveStatus.value = 'idle'
            return
        }

        // console.log(`💾 开始保存草稿，共 ${changes.length} 条变更`)

        // 🆕 检查本次保存是否包含新增行
        const hasNewRows = changes.some(change => change.IsNew === true)

        // 更新保存统计
        saveStats.value.totalSaves++

        // 调用后台保存接口
        const response = await BatchSaveCourseDraft(changes)

        // 计算保存耗时
        const saveTime = Date.now() - saveStartTime
        saveStats.value.averageSaveTime = Math.round(
            (saveStats.value.averageSaveTime * (saveStats.value.totalSaves - 1) + saveTime) / saveStats.value.totalSaves
        )
        // 处理保存结果
        if (response.IsSuccess) {
            saveStatus.value = 'success'
            saveStats.value.successSaves++
            saveStats.value.lastSaveTime = new Date()

            // console.log(`✅ 草稿保存成功，耗时 ${saveTime}ms`)

            // 处理保存成功的逻辑（传递是否有新增行的标记）
            await handleSaveSuccess(response, hasNewRows)

        } else {
            console.error('❌ 草稿保存失败:', response.ErrorMsg)
            saveStatus.value = 'error'
            saveStats.value.failedSaves++

            // 🆕 判断是否为验证错误（400 错误不应重试）
            const isValidationError = response.ErrorCode === 400 ||
                response.ErrorMsg?.includes('validation') ||
                response.ErrorMsg?.includes('验证')

            if (isValidationError) {
                console.warn('⚠️ 检测到验证错误，不重新放回收集器，避免无限重试')
                // 验证错误：不重新放回收集器，直接处理错误
                handleSaveError(response, toSaveCollector, false)
            } else {
                // 其他错误：重新放回收集器以便重试
                // console.log('🔄 非验证错误，将变更重新放回收集器')
                mergeCollectorBack(toSaveCollector)
                handleSaveError(response, toSaveCollector, true)
            }
        }

    } catch (error) {
        console.error('💥 保存草稿时发生异常:', error)
        saveStatus.value = 'error'
        saveStats.value.failedSaves++

        // 🆕 判断是否为网络错误或其他可重试的错误
        const isRetryableError = error.message?.includes('network') ||
            error.message?.includes('timeout') ||
            error.message?.includes('abort')

        if (isRetryableError) {
            // console.log('🔄 检测到可重试错误，将变更重新放回收集器')
            mergeCollectorBack(toSaveCollector)
            handleSaveException(error, toSaveCollector, true)
        } else {
            // console.warn('⚠️ 检测到不可重试错误，不重新放回收集器')
            handleSaveException(error, toSaveCollector, false)
        }

    } finally {
        // 释放并发锁
        isSaving.value = false

        // 🆕 检查是否有新的变更需要保存
        if (activeChangeCollector.value.size > 0) {
            // console.log('🔄 检测到新的变更，准备下一轮保存')
            resetSaveTimer('batch') // 使用批量防抖时间
        }

        // 重置保存状态（延迟）
        setTimeout(() => {
            if (saveStatus.value !== 'saving') {
                saveStatus.value = 'idle'
            }
        }, 3000)
    }
}

/**
 * 🆕 从指定收集器收集变更数据
 * @param {Map} collector - 变更收集器
 * @returns {Array} 变更数据数组
 */
const collectChangesFromCollector = (collector) => {
    const changes = []
    // 🆕 根据表格类型定义需要过滤的字段
    const getExcludedFields = () => {
        const tableType = selectedTableType.value
        const excluded = []
        
        if (tableType === 10) {
            // 给班级排课：排除学员字段和预约课字段
            excluded.push('StudentUserID', 'StudentUserName', 'MaxStudentCount', 'StartStudentCount')
        } else if (tableType === 20) {
            // 给学员排课：排除班级字段和预约课字段
            excluded.push('ClassID', 'ClassName', 'MaxStudentCount', 'StartStudentCount')
        } else if (tableType === 30) {
            // 排预约课：排除班级和学员字段
            excluded.push('ClassID', 'ClassName', 'StudentUserID', 'StudentUserName')
        }
        
        return new Set(excluded)
    }
    
    const excludedFields = getExcludedFields()
    
    collector.forEach((rowChanges, rowId) => {
        // 找到对应的表格行数据
        const tableRow = tableData.value.find(row => row.ID === rowId)
        if (!tableRow) {
            return
        }
        // 检查是否包含新增行
        const hasNewRow = Array.from(rowChanges.values()).some(changeInfo => changeInfo.isNewRow)
        // 构建该行的变更数据
        const rowData = {
            ID: rowId,
            CourseMethod: selectedTableType.value // 必传字段
        }

        // 对于 StartTime 和 EndTime
        const hasTimeRangeChange = rowChanges.has('timeRange')
        // 特殊处理：如果有 timeRange 字段，需要解析出 StartTime 和 EndTime
        if (hasTimeRangeChange && tableRow.timeRange && tableRow.timeRange.includes('~')) {
            const [start, end] = tableRow.timeRange.split('~')
            rowData.StartTime = start || ''
            rowData.EndTime = end || ''
        }
        const hasMainTeacherChange = rowChanges.has('MainTeacherID') || rowChanges.has('MainTeacherName')
        // 特殊处理：构建主教师传输字段 - 使用验证后的ID字段
        const validatedMainTeacherID = validateIDField('MainTeacherID', tableRow.MainTeacherID)
        // 🆕 只要有主教师变更，就传递 MainTeacherList（包括清空的情况）
        if (hasMainTeacherChange) {
            // 如果有有效的ID或名称，构建完整对象；否则传递空数组表示清空
            if (validatedMainTeacherID || tableRow.MainTeacherName) {
                rowData.MainTeacherList = [{
                    ID: validatedMainTeacherID || '',
                    Name: tableRow.MainTeacherName || '',
                    TeacherCommissionList: tableRow.MainTeacherList && tableRow.MainTeacherList.length > 0 && tableRow.MainTeacherList[0].TeacherCommissionList && tableRow.MainTeacherList[0].TeacherCommissionList.length > 0
                        ? tableRow.MainTeacherList[0].TeacherCommissionList
                        : tableRow.MainTeacherCommissionList || [],
                }]
            } else {
                // 清空任课老师时传递空数组
                rowData.MainTeacherList = []
            }
        }
        const hasAssistantTeacherChange = rowChanges.has('AssistantTeacherID') || rowChanges.has('AssistantTeacherName')
        // 特殊处理：构建助教传输字段 - 保留名称但验证ID
        // 🆕 只要有助教变更，就传递 AssistantTeacherList（包括清空的情况）
        if (hasAssistantTeacherChange) {
            if (tableRow.AssistantTeacherID || tableRow.AssistantTeacherName) {
                const originalAssistantIds = tableRow.AssistantTeacherID ? tableRow.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean) : []
                const assistantNames = tableRow.AssistantTeacherName ? tableRow.AssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean) : []

                // 构建助教列表，保留所有名称，但验证ID
                const maxLength = Math.max(originalAssistantIds.length, assistantNames.length)
                rowData.AssistantTeacherList = []

                for (let i = 0; i < maxLength; i++) {
                    const originalId = originalAssistantIds[i] || ''
                    const name = assistantNames[i] || originalId || ''

                    // 验证ID是否为有效UUID，无效则设为空字符串
                    const validatedId = originalId && isUUID(originalId) ? originalId : ''
                    const assistantData = assistantDataMap.value.get(validatedId)
                    if (name) { // 只要有名称就保留这个助教
                        rowData.AssistantTeacherList.push({
                            ID: validatedId, // 无效UUID时为空字符串
                            Name: name,
                            TeacherCommissionList: assistantData ? assistantData.TeacherCommissionList : [],
                        })
                    }
                }
            } else {
                // 清空助教时传递空数组
                rowData.AssistantTeacherList = []
            }
        }

        if (hasNewRow) {
            // 🆕 新增行：传递所有字段（包括空值）

            // 遍历所有业务字段，确保都有值
            Object.keys(fieldMap).forEach(fieldName => {
                // 🆕 过滤掉当前表格类型不应该提交的字段
                if (excludedFields.has(fieldName)) {
                    return
                }
                
                let fieldValue = tableRow[fieldName]

                // 对ID字段进行UUID验证，不符合规则则传null或过滤数组
                fieldValue = validateIDField(fieldName, fieldValue)
                rowData[fieldName] = fieldValue !== undefined ? fieldValue : ''

                // 如果是ID字段，同时收集对应的名称字段
                if (fieldName !== fieldMap[fieldName]) {
                    const nameField = fieldMap[fieldName]
                    // 🆕 也要检查名称字段是否被排除
                    if (excludedFields.has(nameField)) {
                        return
                    }
                    const nameValue = tableRow[nameField]
                    rowData[nameField] = nameValue !== undefined ? nameValue : ''
                }
            })

            // 添加其他必要字段（包括纯文本字段）
            rowData.CourseType = tableRow.CourseType !== undefined ? tableRow.CourseType : '1'
            // 学员排课时，开放预约字段固定为'0'（否）
            if (selectedTableType.value === 20) {
                rowData.IsSubscribeCourse = '0'
            } else {
                rowData.IsSubscribeCourse = tableRow.IsSubscribeCourse !== undefined ? tableRow.IsSubscribeCourse : '0'
            }
            rowData.InternalRemark = tableRow.InternalRemark !== undefined ? tableRow.InternalRemark : ''
            rowData.Describe = tableRow.Describe !== undefined ? tableRow.Describe : ''
            // 只有预约课才需要人数字段
            if (selectedTableType.value === 30) {
                // 保持原始值，如果是 null 或 undefined 就不处理
                if (tableRow.MaxStudentCount !== null && tableRow.MaxStudentCount !== undefined) {
                    rowData.MaxStudentCount = tableRow.MaxStudentCount
                }
                if (tableRow.StartStudentCount !== null && tableRow.StartStudentCount !== undefined) {
                    rowData.StartStudentCount = tableRow.StartStudentCount
                }
            }

        } else {
            // 收集所有变更的字段，包括ID字段和名称字段
            rowChanges.forEach((changeInfo, fieldName) => {
                // 根据表格类型过滤不应提交的字段
                if (excludedFields.has(fieldName)) {
                    return
                }

                let { value, timestamp } = changeInfo

                // 日期被清空时，保证传递空字符串
                if (fieldName === 'Date' && (value === null || value === undefined)) {
                    value = ''
                }

                // 空值清空处理：教室/日期
                if ((fieldName === 'ClassRoomID' || fieldName === 'Date') && (value === null || value === undefined)) {
                    value = ''
                }

                // 对ID字段进行UUID验证，不符合规则则传null或过滤数组
                value = validateIDField(fieldName, value)

                // 使用验证后的值
                rowData[fieldName] = value

                // 如果是ID字段，同时收集对应的名称字段
                if (fieldMap[fieldName] && fieldName !== fieldMap[fieldName]) {
                    const nameField = fieldMap[fieldName]
                    // 检查名称字段是否也应被排除
                    if (excludedFields.has(nameField)) {
                        return
                    }
                    const nameValue = tableRow[nameField]
                    if (nameValue !== undefined) {
                        rowData[nameField] = nameValue
                    }
                }

                // 如果是名称字段，同时收集对应的ID字段
                const idField = Object.keys(fieldMap).find(key => fieldMap[key] === fieldName)
                if (idField && tableRow[idField] !== undefined) {
                    // 检查ID字段是否应被排除
                    if (excludedFields.has(idField)) {
                        return
                    }
                    let idValue = tableRow[idField]
                    // 对ID字段进行UUID验证
                    idValue = validateIDField(idField, idValue)
                    rowData[idField] = idValue
                }
            })
            // 📝 修改行：如果涉及教师字段变更，需要重新构建传输字段
            // const hasMainTeacherChange = rowChanges.has('MainTeacherID') || rowChanges.has('MainTeacherName')
            // if (hasMainTeacherChange) {
            //     const validatedMainTeacherID = validateIDField('MainTeacherID', rowData.MainTeacherID || tableRow.MainTeacherID)
            //     if (validatedMainTeacherID || rowData.MainTeacherName || tableRow.MainTeacherName) {
            //         rowData.MainTeacherList = [{
            //             ID: validatedMainTeacherID || '',
            //             Name: rowData.MainTeacherName || tableRow.MainTeacherName || '',
            //             TeacherCommissionList: tableRow.MainTeacherList && tableRow.MainTeacherList.length > 0
            //                 ? tableRow.MainTeacherList[0].TeacherCommissionList
            //                 : tableRow.MainTeacherCommissionList || tableRow.TeacherCommissionList || [],
            //         }]
            //     } else {
            //         rowData.MainTeacherList = []
            //     }
            // }

            const hasAssistantTeacherChange = rowChanges.has('AssistantTeacherID') || rowChanges.has('AssistantTeacherName')
            if (hasAssistantTeacherChange) {
                const currentAssistantTeacherID = rowData.AssistantTeacherID || tableRow.AssistantTeacherID || ''
                const currentAssistantTeacherName = rowData.AssistantTeacherName || tableRow.AssistantTeacherName || ''

                if (currentAssistantTeacherID || currentAssistantTeacherName) {
                    const originalAssistantIds = currentAssistantTeacherID ? currentAssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean) : []
                    const assistantNames = currentAssistantTeacherName ? currentAssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean) : []

                    // 构建助教列表，保留所有名称，但验证ID
                    const maxLength = Math.max(originalAssistantIds.length, assistantNames.length)
                    rowData.AssistantTeacherList = []

                    for (let i = 0; i < maxLength; i++) {
                        const originalId = originalAssistantIds[i] || ''
                        const name = assistantNames[i] || originalId || ''

                        // 验证ID是否为有效UUID，无效则设为空字符串
                        const validatedId = originalId && isUUID(originalId) ? originalId : ''
                        const assistantData = assistantDataMap.value.get(validatedId)
                        if (name) { // 只要有名称就保留这个助教
                            rowData.AssistantTeacherList.push({
                                ID: validatedId, // 无效UUID时为空字符串
                                Name: name,
                                TeacherCommissionList: assistantData ? assistantData.TeacherCommissionList : [],
                            })
                        }
                    }

                } else {
                    rowData.AssistantTeacherList = []
                }
            }
            // 🆕 追加逻辑：把该行标记为错误的字段一并携带到本次提交
            const errorFieldsForRow = Array.isArray(tableRow.errorField) ? tableRow.errorField : []
            if (errorFieldsForRow.length > 0) {
                const includeExtraFields = new Set(errorFieldsForRow)

                includeExtraFields.forEach((fieldName) => {
                    // 根据表格类型过滤不应提交的字段
                    if (excludedFields.has(fieldName)) {
                        return
                    }

                    // 附带该字段当前值（若未在本次变更中）
                    if (rowData[fieldName] === undefined) {
                        let value = tableRow[fieldName]
                        value = validateIDField(fieldName, value)
                        rowData[fieldName] = value
                    }

                    // 附带对应的名称快照
                    const nameField = fieldMap[fieldName]
                    if (nameField && rowData[nameField] === undefined) {
                        // 检查名称字段是否也应被排除
                        if (excludedFields.has(nameField)) {
                            return
                        }
                        rowData[nameField] = tableRow[nameField]
                    }
                })

                // 如果错误字段包含 MainTeacherID，但本次未构建 List，则补构建
                if (includeExtraFields.has('MainTeacherID') && rowData.MainTeacherList === undefined) {
                    const validatedMainTeacherID = validateIDField('MainTeacherID', tableRow.MainTeacherID)
                    if (validatedMainTeacherID || tableRow.MainTeacherName) {
                        rowData.MainTeacherList = [{
                            ID: validatedMainTeacherID || '',
                            Name: tableRow.MainTeacherName || '',
                            TeacherCommissionList: tableRow.TeacherCommissionList,
                        }]
                    } else {
                        rowData.MainTeacherList = []
                    }
                }

                // 如果错误字段包含 AssistantTeacherID，但本次未构建 List，则补构建
                if (includeExtraFields.has('AssistantTeacherID') && rowData.AssistantTeacherList === undefined) {
                    const currentAssistantTeacherID = tableRow.AssistantTeacherID || ''
                    const currentAssistantTeacherName = tableRow.AssistantTeacherName || ''
                    if (currentAssistantTeacherID || currentAssistantTeacherName) {
                        const originalAssistantIds = currentAssistantTeacherID ? currentAssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean) : []
                        const assistantNames = currentAssistantTeacherName ? currentAssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean) : []
                        const maxLength = Math.max(originalAssistantIds.length, assistantNames.length)
                        rowData.AssistantTeacherList = []
                        for (let i = 0; i < maxLength; i++) {
                            const originalId = originalAssistantIds[i] || ''
                            const name = assistantNames[i] || originalId || ''
                            const validatedId = originalId && isUUID(originalId) ? originalId : ''
                            const assistantData = assistantDataMap.value.get(validatedId)
                            if (name) {
                                rowData.AssistantTeacherList.push({ ID: validatedId, Name: name, TeacherCommissionList: assistantData ? assistantData.TeacherCommissionList : [] })
                            }
                        }
                    } else {
                        rowData.AssistantTeacherList = []
                    }
                }
            }
        }

        changes.push(rowData)
    })

    return changes
}

/**
 * 🆕 将收集器合并回活跃收集器（保存失败时使用）
 * @param {Map} collectorToMerge - 要合并的收集器
 */
const mergeCollectorBack = (collectorToMerge) => {
    collectorToMerge.forEach((rowChanges, rowId) => {
        if (!activeChangeCollector.value.has(rowId)) {
            activeChangeCollector.value.set(rowId, rowChanges)
        } else {
            // 如果已存在，合并变更（保留最新的）
            const existingChanges = activeChangeCollector.value.get(rowId)
            rowChanges.forEach((changeInfo, fieldName) => {
                // 只有当现有变更的时间戳更早时才覆盖
                const existing = existingChanges.get(fieldName)
                if (!existing || existing.timestamp < changeInfo.timestamp) {
                    existingChanges.set(fieldName, changeInfo)
                }
            })
        }
    })
}

/**
 * 🆕 处理保存成功
 * @param {Boolean} skipConflictCheck - 是否跳过冲突检查（队列处理时使用）
 */
const handleSaveSuccess = async (response, hasNewRows = false, skipConflictCheck = false) => {
    // 更新自动保存时间
    const now = new Date()
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    autoSaveTime.value = `${hours}:${minutes}:${seconds}`

    // ==================== 🆕 UI 交互锁检查：如果有弹框/下拉框打开，暂停更新 ====================
    if (uiInteractionLock.value) {
        // console.log('🔒 UI交互锁已锁定，保存响应暂存到队列', {
        //     queueLength: pendingSaveResponses.value.length,
        //     editingCells: Array.from(editingCells.value),
        //     activeFocusedCell: activeFocusedCell.value
        // })
        pendingSaveResponses.value.push({ response, hasNewRows, timestamp: Date.now() })
        
        // ✅ 方案1：冲突检查不受 UI 锁影响，立即执行
        // 原因：冲突检查不涉及 UI 更新，不会中断用户操作，可以立即给用户反馈
        if (preCheckEnabled.value && !hasNewRows) {
            try {
                const savedIds = (response.Data?.SavedDraftList || []).map(item => item?.ID).filter(Boolean)
                if (savedIds.length > 0) {
                    // console.log('🔍 [UI锁定期间] 立即触发冲突检查，ID数量:', savedIds.length)
                    triggerCheckByIds(savedIds, { force: true })
                }
            } catch (e) {
                console.warn('UI锁定期间触发冲突检查失败:', e)
            }
        }
        
        return
    }

    // 🆕 如果包含新增行，重新加载草稿列表以保持顺序一致
    if (hasNewRows) {
        // console.log('🔄 检测到新增行，重新加载草稿列表以同步后端顺序')
        await loadCourseDraftList()
        return
    }

    // 处理后台返回的已保存数据（用于数据回填）
    if (response.Data && response.Data.SavedDraftList) {
        // ==================== 🆕 字段别名映射：前端编辑字段 → 后台返回字段 ====================
        // 用于编辑状态保护：当用户编辑某个前端字段时，需要同时保护后台返回的相关字段
        const fieldAliasMap = {
            'MainTeacherID': ['MainTeacherList', 'MainTeacherName'],
            'MainTeacherName': ['MainTeacherList', 'MainTeacherID'],
            'AssistantTeacherID': ['AssistantTeacherList', 'AssistantTeacherName'],
            'AssistantTeacherName': ['AssistantTeacherList', 'AssistantTeacherID'],
        }
        
        // 用后台返回的正确数据覆盖前端表格数据
        response.Data.SavedDraftList.forEach(savedDraft => {
            const rowIndex = tableData.value.findIndex(row => row.ID === savedDraft.ID)
            if (rowIndex !== -1) {
                // ==================== 🆕 编辑状态保护：获取该行正在编辑的字段 ====================
                const editingFieldsInRow = Array.from(editingCells.value)
                    .filter(key => key.startsWith(`${savedDraft.ID}_`))
                    .map(key => key.split('_')[1])  // 提取字段名
                
                // console.log(`🔍 Row ${savedDraft.ID} - Editing fields:`, editingFieldsInRow)
                
                // 检查字段是否受保护（直接匹配 或 别名匹配）
                const isFieldProtected = (field) => {
                    // 1. 直接匹配：字段本身正在编辑
                    if (editingFieldsInRow.includes(field)) {
                        return true
                    }
                    
                    // 2. 别名匹配：正在编辑的字段的别名包含当前字段
                    return editingFieldsInRow.some(editingField => {
                        const aliases = fieldAliasMap[editingField] || []
                        return aliases.includes(field)
                    })
                }
                
                // 用后台数据覆盖，同时保留前端特有字段
                const updatedRow = {
                    ...tableData.value[rowIndex],
                    ClassID: savedDraft.ClassID === null ? '' : savedDraft.ClassID,
                    ShiftID: savedDraft.ShiftID === null ? '' : savedDraft.ShiftID,
                    ClassRoomID: savedDraft.ClassRoomID === null ? '' : savedDraft.ClassRoomID,
                    StudentUserID: savedDraft.StudentUserID === null ? '' : savedDraft.StudentUserID,
                }
                
                // ==================== 🆕 有保护地更新字段（跳过正在编辑的字段）====================
                Object.keys(savedDraft).forEach(field => {
                    // 跳过前端特有字段和已经处理的特殊字段
                    if (['ClassID', 'ShiftID', 'ClassRoomID', 'StudentUserID'].includes(field)) {
                        return
                    }
                    
                    // 🛡️ 编辑状态保护：跳过正在编辑的字段及其别名
                    if (isFieldProtected(field)) {
                        // console.log(`⏭️ Skip protected field: ${field} (row: ${savedDraft.ID})`)
                        return
                    }
                    
                    // 安全更新：不受保护的字段正常回填
                    updatedRow[field] = savedDraft[field]
                })

                // 处理主教师数据：从 MainTeacherList 中提取 MainTeacherID 和 MainTeacherName
                // 🆕 修复：后台可能返回 null 或 []，都需要清空字段
                // 🛡️ 编辑状态保护：仅在 MainTeacherList/MainTeacherID/MainTeacherName 都不在编辑时才更新
                if (savedDraft.MainTeacherList !== undefined && !isFieldProtected('MainTeacherList')) {
                    if (Array.isArray(savedDraft.MainTeacherList) && savedDraft.MainTeacherList.length > 0) {
                        // 有数据：正常回填
                        const firstTeacher = savedDraft.MainTeacherList[0]
                        updatedRow.MainTeacherID = firstTeacher.ID || ''
                        if (firstTeacher.TeacherCommissionList && Array.isArray(firstTeacher.TeacherCommissionList) && firstTeacher.TeacherCommissionList.length > 0 && firstTeacher.TeacherCommissionList[0].Name) {
                            updatedRow.MainTeacherName = firstTeacher.Name + '（' + firstTeacher.TeacherCommissionList[0].Name + '）'
                        } else {
                            updatedRow.MainTeacherName = firstTeacher.Name || ''
                        }
                        updatedRow.MainTeacherList = savedDraft.MainTeacherList
                    } else {
                        // null 或 空数组：清空操作
                        updatedRow.MainTeacherID = ''
                        updatedRow.MainTeacherName = ''
                        updatedRow.MainTeacherList = []
                    }
                }

                // 处理助教数据：从 AssistantTeacherList 中提取 AssistantTeacherID 和 AssistantTeacherName
                // 🆕 修复：后台可能返回 null 或 []，都需要清空字段
                // 🛡️ 编辑状态保护：仅在 AssistantTeacherList/AssistantTeacherID/AssistantTeacherName 都不在编辑时才更新
                if (savedDraft.AssistantTeacherList !== undefined && !isFieldProtected('AssistantTeacherList')) {
                    if (Array.isArray(savedDraft.AssistantTeacherList) && savedDraft.AssistantTeacherList.length > 0) {
                        // 有数据：正常回填
                        const assistantIds = savedDraft.AssistantTeacherList.map(assistant => assistant.ID || '').filter(Boolean)
                        const assistantNames = savedDraft.AssistantTeacherList.map(assistant => {
                            if (assistant.TeacherCommissionList && Array.isArray(assistant.TeacherCommissionList) && assistant.TeacherCommissionList.length > 0 && assistant.TeacherCommissionList[0].Name) {
                                return assistant.Name + '（' + assistant.TeacherCommissionList[0].Name + '）'
                            } else {
                                return assistant.Name || ''
                            }
                        }).filter(Boolean)
                        updatedRow.AssistantTeacherID = assistantIds.join(',')
                        updatedRow.AssistantTeacherName = assistantNames.join(', ')
                        updatedRow.AssistantTeacherList = savedDraft.AssistantTeacherList
                    } else {
                        // null 或 空数组：清空操作
                        updatedRow.AssistantTeacherID = ''
                        updatedRow.AssistantTeacherName = ''
                        updatedRow.AssistantTeacherList = []
                    }
                }
                
                // ==================== 🆕 真正的精细更新：逐字段更新，只更新变化的字段 ====================
                // 避免 Object.assign 触发整行重新渲染导致弹框/下拉框关闭
                const currentRow = tableData.value[rowIndex]
                Object.keys(updatedRow).forEach(key => {
                    // 只有当值真正变化时才更新（避免触发不必要的响应式更新）
                    if (currentRow[key] !== updatedRow[key]) {
                        currentRow[key] = updatedRow[key]
                    }
                })
            }
        })
        // 同时更新原始数据备份
        originalTableData.value = safeJsonClone('save-draft-success')
    }

    // 处理错误字段
    if (response.Data) {
        const saveErrorList = response.Data.SaveErrorList || []
        // DraftId -> 映射后的错误字段和错误信息
        const errorMapByDraftId = new Map()
        const messageMapByDraftId = new Map()

        saveErrorList.forEach(err => {
            const mappedErrors = []
            const errorMessages = {}

                ; (err.ErrorFieldList || []).forEach(errorItem => {
                    if (errorItem && errorItem.FieldName) {
                        // 字段名映射
                        let mappedFieldName = errorItem.FieldName
                        if (errorItem.FieldName === 'MainTeacherList') {
                            mappedFieldName = 'MainTeacherID'
                        } else if (errorItem.FieldName === 'AssistantTeacherList') {
                            mappedFieldName = 'AssistantTeacherID'
                        }

                        mappedErrors.push(mappedFieldName)

                        // 存储错误信息
                        if (errorItem.ErrorMessage) {
                            errorMessages[mappedFieldName] = errorItem.ErrorMessage
                        }
                    }
                })

            errorMapByDraftId.set(err.DraftId, Array.from(new Set(mappedErrors)))
            messageMapByDraftId.set(err.DraftId, errorMessages)
        })

        // 仅覆盖本次提交的这些行
        const changedRowIds = new Set(response.Data?.SavedDraftList?.map(item => item.ID) || [])
        tableData.value.forEach(row => {
            if (changedRowIds.has(row.ID)) {
                row.errorField = errorMapByDraftId.get(row.ID) || []
                row.errorMessages = messageMapByDraftId.get(row.ID) || {}
            }
        })

        // 同步原始备份
        originalTableData.value = safeJsonClone('save-draft-error-handling')
    }
    // console.log("保存成功后")
    // 🆕 保存成功后：若预检查开启，则对本次保存成功的行触发单行复检（仅这些ID，强制跳过去重）
    // 注意：如果 skipConflictCheck=true，说明在 UI 锁定期间已经检查过了，这里不需要重复检查
    if (preCheckEnabled.value && !skipConflictCheck) {
        try {
            const savedIds = (response.Data?.SavedDraftList || []).map(item => item?.ID).filter(Boolean)
            if (savedIds.length > 0) {
                // console.log('🔍 [正常流程] 保存后触发冲突检查，ID数量:', savedIds.length)
                triggerCheckByIds(savedIds, { force: true })
            }
        } catch (e) {
            console.warn('保存后复检触发失败:', e)
        }
    }
}

/**
 * 🆕 处理待处理的保存响应队列
 * 当 UI 交互锁释放时调用
 */
const processPendingSaveResponses = async () => {
    // console.log('📤 [processPendingSaveResponses] 被调用，队列长度:', pendingSaveResponses.value.length, '当前锁状态:', uiInteractionLock.value)
    
    if (pendingSaveResponses.value.length === 0) {
        // console.log('📤 队列为空，无需处理')
        return
    }
    
    // console.log(`🔓 开始处理 ${pendingSaveResponses.value.length} 个待处理的保存响应`)
    
    // 取出所有待处理响应
    const responses = [...pendingSaveResponses.value]
    pendingSaveResponses.value = []
    
    // 逐个处理（按时间顺序）
    // skipConflictCheck=true 因为在 UI 锁定期间已经触发过冲突检查了
    for (const item of responses) {
        // console.log('📤 处理队列项:', { hasNewRows: item.hasNewRows, timestamp: item.timestamp })
        await handleSaveSuccess(item.response, item.hasNewRows, true)  // skipConflictCheck=true
    }
    
    // console.log('✅ 所有待处理响应已处理完毕')
}

/**
 * 🆕 处理保存失败
 * @param {Object} response - 响应对象
 * @param {Map} failedCollector - 失败的变更收集器
 * @param {Boolean} shouldRetry - 是否应该重试
 */
const handleSaveError = (response, failedCollector, shouldRetry = false) => {
    // 显示错误提示
    ElMessage.error({
        message: `保存失败: ${response.ErrorMsg || '未知错误'}`,
        duration: 5000,
        showClose: true
    })

    // 保存失败时，清空本次新增的行
    const newRowsToRemove = tableData.value.filter(row => {
        const rowChanges = failedCollector.get(row.ID)
        return rowChanges && Array.from(rowChanges.values()).some(change => change.isNewRow === true)
    })

    if (newRowsToRemove.length > 0) {
        // console.log(`🗑️ 清除 ${newRowsToRemove.length} 个新增行（保存失败）`)
        newRowsToRemove.forEach(rowToRemove => {
            const index = tableData.value.findIndex(row => row.ID === rowToRemove.ID)
            if (index !== -1) {
                tableData.value.splice(index, 1)
            }
        })
        // 同步更新原始数据备份
        originalTableData.value = safeJsonClone('save-failed-remove-new-rows')
    }

    // 如果不应该重试，清空收集器中对应的变更
    if (!shouldRetry) {
        failedCollector.forEach((rowChanges, rowId) => {
            // 从活跃收集器中移除这些失败的变更
            if (activeChangeCollector.value.has(rowId)) {
                const activeChanges = activeChangeCollector.value.get(rowId)
                rowChanges.forEach((changeInfo, fieldName) => {
                    activeChanges.delete(fieldName)
                })
                // 如果该行没有任何变更了，移除整行
                if (activeChanges.size === 0) {
                    activeChangeCollector.value.delete(rowId)
                }
            }
        })
        // console.log('🚫 已清除失败的变更，不会重试')
    }
}

/**
 * 🆕 处理保存异常
 * @param {Error} error - 错误对象
 * @param {Map} failedCollector - 失败的变更收集器
 * @param {Boolean} shouldRetry - 是否应该重试
 */
const handleSaveException = (error, failedCollector, shouldRetry = false) => {
    Logger.error("草稿保存失败", {
        error: error,
        changesCount: failedCollector.size,
        selectedTableType: selectedTableType.value,
    })

    // 显示错误提示
    ElMessage.error({
        message: `保存异常: ${error.message || '未知错误'}`,
        duration: 5000,
        showClose: true
    })

    // 异常时，同样清空本次新增的行
    const newRowsToRemove = tableData.value.filter(row => {
        const rowChanges = failedCollector.get(row.ID)
        return rowChanges && Array.from(rowChanges.values()).some(change => change.isNewRow === true)
    })

    if (newRowsToRemove.length > 0) {
        // console.log(`🗑️ 清除 ${newRowsToRemove.length} 个新增行（保存异常）`)
        newRowsToRemove.forEach(rowToRemove => {
            const index = tableData.value.findIndex(row => row.ID === rowToRemove.ID)
            if (index !== -1) {
                tableData.value.splice(index, 1)
            }
        })
        // 同步更新原始数据备份
        originalTableData.value = safeJsonClone('save-exception-remove-new-rows')
    }

    // 如果不应该重试，清空收集器中对应的变更
    if (!shouldRetry) {
        failedCollector.forEach((rowChanges, rowId) => {
            // 从活跃收集器中移除这些失败的变更
            if (activeChangeCollector.value.has(rowId)) {
                const activeChanges = activeChangeCollector.value.get(rowId)
                rowChanges.forEach((changeInfo, fieldName) => {
                    activeChanges.delete(fieldName)
                })
                // 如果该行没有任何变更了，移除整行
                if (activeChanges.size === 0) {
                    activeChangeCollector.value.delete(rowId)
                }
            }
        })
        // console.log('🚫 已清除失败的变更，不会重试')
    }
}

/**
 * 🆕 兼容性：保留原有的 saveDraft 函数名，重定向到新的执行函数
 */
const saveDraft = async () => {
    // 🆕 使用新的优化保存逻辑
    await executeSave()
}



/**
 * 收集所有未保存的变更数据（准备请求前的工作）
 * @returns {Array} 变更数据数组，包含所有字段（ID和名称）
 * 🆕 兼容性函数：现在使用 collectChangesFromCollector 处理 activeChangeCollector
 */
const collectAllChanges = () => {
    // 🆕 使用新的收集器处理函数
    return collectChangesFromCollector(activeChangeCollector.value)
}

/**
 * 加载草稿列表数据
 */
const loadCourseDraftList = async () => {
    try {
        // 设置loading状态
        isDraftLoading.value = true

        const response = await GetCourseDraftList({
            CourseMethod: selectedTableType.value
        })
        if (response.IsSuccess) {
            let savedList = response.Data?.SavedDraftList || []
            const saveErrorList = response.Data?.SaveErrorList || []
            // 上限控制：仅当本次返回条数将导致超出时才提示并截断
            const capacity = getRemainingCapacity()
            if (savedList.length > capacity) {
                ElMessage.warning(`本次返回 ${savedList.length} 条，超出 ${savedList.length - capacity} 条，已仅载入 ${capacity} 条；总上限 ${MAX_TABLE_ROWS}`)
                savedList = savedList.slice(0, capacity)
            }

            // 后端已给出错误字段，构建 DraftId -> 错误字段和错误信息映射
            const errorMap = new Map()
            const messageMap = new Map()

            saveErrorList.forEach(err => {
                const mappedErrors = []
                const errorMessages = {}

                    ; (err.ErrorFieldList || []).forEach(errorItem => {
                        if (errorItem && errorItem.FieldName) {
                            // 字段名映射
                            let mappedFieldName = errorItem.FieldName
                            if (errorItem.FieldName === 'MainTeacherList') {
                                mappedFieldName = 'MainTeacherID'
                            } else if (errorItem.FieldName === 'AssistantTeacherList') {
                                mappedFieldName = 'AssistantTeacherID'
                            }

                            mappedErrors.push(mappedFieldName)

                            // 存储错误信息
                            if (errorItem.ErrorMessage) {
                                errorMessages[mappedFieldName] = errorItem.ErrorMessage
                            }
                        }
                    })

                errorMap.set(err.DraftId, Array.from(new Set(mappedErrors)))
                messageMap.set(err.DraftId, errorMessages)
            })

            // 将草稿数据复制给 tableData
            tableData.value = (savedList || []).slice(0, MAX_TABLE_ROWS).map(draft => {
                const processedDraft = { ...draft }

                // 优先合成 timeRange
                if (draft.StartTime && draft.EndTime) {
                    processedDraft.timeRange = `${draft.StartTime}~${draft.EndTime}`
                } else {
                    processedDraft.timeRange = ''
                }

                // 主教师：从 List 提取便于显示的快照
                // 🆕 修复：后台可能返回 null，也需要清空字段
                if (draft.MainTeacherList !== undefined && draft.MainTeacherList !== null) {
                    if (Array.isArray(draft.MainTeacherList) && draft.MainTeacherList.length > 0) {
                        // 有数据：正常处理
                        const firstTeacher = draft.MainTeacherList[0]
                        if (firstTeacher.TeacherCommissionList && Array.isArray(firstTeacher.TeacherCommissionList) && firstTeacher.TeacherCommissionList.length > 0 && firstTeacher.TeacherCommissionList[0].Name) {
                            processedDraft.MainTeacherName = firstTeacher.Name + '（' + firstTeacher.TeacherCommissionList[0].Name + '）'
                        } else {
                            processedDraft.MainTeacherName = firstTeacher.Name || ''
                        }
                        processedDraft.MainTeacherID = firstTeacher.ID || ''
                    } else {
                        // null 或 空数组：清空操作
                        processedDraft.MainTeacherID = ''
                        processedDraft.MainTeacherName = ''
                        processedDraft.MainTeacherList = []
                    }
                } else {
                    // 字段不存在：使用原有字段值
                    processedDraft.MainTeacherID = processedDraft.MainTeacherID || ''
                    processedDraft.MainTeacherName = processedDraft.MainTeacherName || ''
                    processedDraft.MainTeacherList = []
                }
                // 助教：从 List 提取便于显示的快照
                // 🆕 修复：后台可能返回 null，也需要清空字段
                if (draft.AssistantTeacherList !== undefined && draft.AssistantTeacherList !== null) {
                    if (Array.isArray(draft.AssistantTeacherList) && draft.AssistantTeacherList.length > 0) {
                        // 有数据：正常处理
                        const assistantIds = draft.AssistantTeacherList.map(assistant => assistant.ID || '').filter(Boolean)
                        const assistantNames = draft.AssistantTeacherList.map(assistant => {
                            if (assistant.TeacherCommissionList && Array.isArray(assistant.TeacherCommissionList) && assistant.TeacherCommissionList.length > 0 && assistant.TeacherCommissionList[0].Name) {
                                return assistant.Name + '（' + assistant.TeacherCommissionList[0].Name + '）'
                            } else {
                                return assistant.Name || ''
                            }
                        }).filter(Boolean)
                        processedDraft.AssistantTeacherID = assistantIds.join(',')
                        processedDraft.AssistantTeacherName = assistantNames.join(', ')
                    } else {
                        // null 或 空数组：清空操作
                        processedDraft.AssistantTeacherID = ''
                        processedDraft.AssistantTeacherName = ''
                        processedDraft.AssistantTeacherList = []
                    }
                } else {
                    // 字段不存在：使用原有字段值
                    processedDraft.AssistantTeacherID = processedDraft.AssistantTeacherID || ''
                    processedDraft.AssistantTeacherName = processedDraft.AssistantTeacherName || ''
                    processedDraft.AssistantTeacherList = []
                }

                // 为错误字段补充默认名称
                const errorFieldsForRow = errorMap.get(processedDraft.ID) || []
                errorFieldsForRow.forEach(idField => {
                    const nameField = fieldMap[idField]
                    if (nameField && (!processedDraft[nameField] || processedDraft[nameField] === '')) {
                        processedDraft[nameField] = '数据不存在'
                    }
                })

                return {
                    ...processedDraft,
                    isEdit: false,
                    isUpdatedFromDraft: true,
                    errorField: errorFieldsForRow,
                    errorMessages: messageMap.get(processedDraft.ID) || {}
                }
            })

            // 同时更新原始数据备份
            originalTableData.value = safeJsonClone('pre-check-error-handling')

            // 填充数据映射，用于分组显示
            tableData.value.forEach(row => {
                // 填充校区映射（校区通过store获取，不需要额外填充）

                // 填充班级映射
                if (row.ClassID && row.ClassName) {
                    updateClassDataMap(row.ClassID, {
                        ID: row.ClassID,
                        Name: row.ClassName,
                        EnableSubject: row.EnableSubject
                    })
                }
                // 填充学员映射
                if (row.StudentUserID && row.StudentUserName) {
                    updateStudentUserDataMap(row.StudentUserID, {
                        ID: row.StudentUserID,
                        Name: row.StudentUserName
                    })
                }

                // 填充课程映射
                if (row.ShiftID && row.ShiftName) {
                    updateCourseDataMap(row.ShiftID, {
                        ID: row.ShiftID,
                        Name: row.ShiftName
                    })
                }

                // 填充教室映射
                if (row.ClassRoomID && row.ClassRoomName) {
                    updateClassroomDataMap(row.ClassRoomID, {
                        ID: row.ClassRoomID,
                        Name: row.ClassRoomName
                    })
                }

                // 填充主教师映射（包含完整的 TeacherCommissionList）
                if (row.MainTeacherID && row.MainTeacherName) {
                    updateTeacherDataMap(row.MainTeacherID, {
                        ID: row.MainTeacherID,
                        Name: row.MainTeacherName,
                        TeacherCommissionList: row.MainTeacherList && row.MainTeacherList[0] ? row.MainTeacherList[0].TeacherCommissionList : []
                    })
                }

                // 填充助教映射
                if (row.AssistantTeacherID && row.AssistantTeacherName) {
                    if (typeof row.AssistantTeacherID === 'string' && typeof row.AssistantTeacherName === 'string') {
                        const assistantIds = row.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean)
                        const assistantNames = row.AssistantTeacherName.split(',').map(name => name.trim()).filter(Boolean)

                        assistantIds.forEach((id, index) => {
                            let assistantData = row.AssistantTeacherList.find(assistant => assistant.ID === id)
                            if (assistantNames[index]) {
                                // 助教是小写id，name
                                updateAssistantDataMap(id, {
                                    id: id,
                                    name: assistantNames[index],
                                    TeacherCommissionList: assistantData ? assistantData.TeacherCommissionList : []
                                })
                            }
                        })
                    }
                }
            })

            enforceMaxCapacity('loadCourseDraftList')

            // 检查是否有非法数据
            const invalidRecords = tableData.value.filter(record => record.errorField && record.errorField.length > 0)
            if (invalidRecords.length > 0) {
                console.warn('⚠️ 发现非法数据记录:', invalidRecords.map(record => ({
                    ID: record.ID,
                    errorFields: record.errorField
                })))
            }

            // 如果分组模式下有数据，重新计算分组
            if (isGrouped.value && tableData.value.length > 0) {
                groupedDataCache.value.isValid = false
            }

            // 🆕 加载草稿后，对所有行进行任课老师与助教重复校验
            // nextTick(() => {
            //     tableData.value.forEach(row => {
            //         if (row && !row.isGroupRow && !row.isGroupFooter) {
            //             const validationResult = validateTeacherAssistantDuplicate(row)
            //             applyValidationResult(row, validationResult)
            //         }
            //     })
            // })

            // 初始化科目列的显示状态
            setTimeout(() => {
                if (tableRef.value && tableRef.value.$refs && tableRef.value.$refs.tableRef) {
                    const veTableInstance = tableRef.value.$refs.tableRef
                    if (shouldShowSubjectColumn.value) {
                        veTableInstance.showColumnsByKeys(['SubjectID'])
                    } else {
                        veTableInstance.hideColumnsByKeys(['SubjectID'])
                    }
                }
            }, 100)
        } else {
            // 如果没有草稿数据，使用默认的空行
            initTableData(recordNewRow)
        }
    } catch (error) {
        console.error('❌ 加载草稿列表出错:', error)
        // 出错时使用默认的空行
        initTableData(recordNewRow)
    } finally {
        // 无论成功失败都要重置loading状态
        isDraftLoading.value = false
    }
}

/**
 * 启动兜底检查机制
 */
const startBackupCheck = () => {
    // 每30秒检查一次是否有未保存的变更
    // backupCheckTimer.value = setInterval(() => {
    //     if (activeChangeCollector.value.size > 0 && saveStatus.value === 'idle') {
    //      // console.log('🔄 兜底检查：发现未保存的变更，触发保存')
    //      // console.log('📊 当前变更收集器状态:', activeChangeCollector.value)
    //      // console.log('📋 收集的变更数据:', collectAllChanges())
    //     }
    // }, 30000) // 30秒
}

/**
 * 停止兜底检查机制
 */
const stopBackupCheck = () => {
    if (backupCheckTimer.value) {
        clearInterval(backupCheckTimer.value)
        backupCheckTimer.value = null
    }
}



/**
 * 创建完整的可编辑单元格渲染器
 * @param {Component} Component - 要渲染的组件
 * @param {Object} options - 配置选项
 * @param {Function} options.getDisplayValue - 获取显示值的函数
 * @param {string} options.displayField - 显示字段名（默认使用column.field）
 * @param {Function} options.getProps - 获取组件props的函数
 * @param {Function} options.getEvents - 获取组件事件的函数
 * @param {Function} options.onUpdateModelValue - 值变化时的回调
 * @param {Function} options.getRef - 获取ref回调的函数
 * @param {Function} options.getDefaultSlot - 获取ElSelect的default插槽的函数
 */
const createEditableCell = (Component, options = {}) => {
    return ({ row, column }) => {
        const isEditing = cellDbClickKey.value?.rowId === row.ID &&
            cellDbClickKey.value?.field === column.field

        // 非编辑状态：显示值
        if (!isEditing) {
            const displayValue = options.getDisplayValue?.(row, column) ||
                row[options.displayField || column.field] || ''
            // 检查当前字段是否有错误
            const hasError = row.errorField && row.errorField.includes(column.field)
            // 预检查命中（限制/冲突/错误）也显示同样的提示标签
            let hasPreCheckHit = false
            if (preCheckEnabled.value && !row.isGroupRow && !row.isGroupFooter) {
                const preCheckData = preCheckResults.value.get(row.ID)
                if (preCheckData) {
                    const getAliases = (field) => {
                        const aliases = new Set([field])
                        const mapped = fieldMap[field]
                        if (mapped) aliases.add(mapped)
                        for (const k in fieldMap) { if (fieldMap[k] === field) aliases.add(k) }
                        // 任课老师特殊：后端可能返回 MainTeacherList
                        if (field === 'MainTeacherName' || field === 'MainTeacherID') aliases.add('MainTeacherList')
                        // 🆕 助教特殊：后端可能返回 AssistantTeacherList
                        if (field === 'AssistantTeacherName' || field === 'AssistantTeacherID') aliases.add('AssistantTeacherList')
                        return Array.from(aliases)
                    }
                    const aliases = getAliases(column.field)
                    const matchList = (list = []) => aliases.some(a => list.includes(a))
                    const inError = matchList(preCheckData.ErrorFieldList || [])
                    const inCheck = (preCheckData.CheckFieldList || []).some(c => matchList(c.FieldNameList || []))
                    const inConflict = matchList((preCheckData.ConflictFieldList && preCheckData.ConflictFieldList.FieldNameList) || [])
                    hasPreCheckHit = inError || inCheck || inConflict

                    // 🆕 如果预检查命中但不是错误，需要将字段添加到 errorField 和 errorMessages 中
                    if (hasPreCheckHit && !hasError) {
                        if (!row.errorField) {
                            row.errorField = []
                        }
                        if (!row.errorMessages) {
                            row.errorMessages = {}
                        }

                        // 确保字段不在 errorField 中
                        if (!row.errorField.includes(column.field)) {
                            row.errorField.push(column.field)
                        }

                        // 设置预检查相关的错误消息
                        if (!row.errorMessages[column.field]) {
                            let message = '有限制/冲突！请查看预检查提示'
                            if (inError) {
                                message = '预检查错误！请查看预检查提示'
                            } else if (inCheck) {
                                message = '有限制！请查看预检查提示'
                            } else if (inConflict) {
                                message = '有冲突！请查看预检查提示'
                            }
                            row.errorMessages[column.field] = message
                        }
                    }
                }
            }

            if (hasError || hasPreCheckHit) {
                // 如果有错误或预检查命中，显示带错误/限制提示的内容
                return h('div', {
                    title: displayValue,
                    class: 'error-field-tooltip',
                    style: {
                        position: 'relative',
                        width: '100%',
                        height: '100%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        paddingRight: '12px'
                    }
                }, [
                    h('span', {
                        style: {
                            flex: '1',
                            lineHeight: 'normal',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap'
                        }
                    }, displayValue),
                    h(ElTooltip, {
                        content: row.errorMessages?.[column.field] || '有限制或冲突！请查看预检查提示',
                        placement: 'top',
                        effect: 'dark',
                        popperClass: 'error-field-tooltip'
                    }, {
                        default: () => h('el-icon', {}, [
                            h('svg', { 'aria-hidden': 'true', }, [
                                h('use', { 'xlink:href': '#w2-xianzhi' })
                            ])
                        ])
                    })
                ])
            }

            return h('div', {
                title: displayValue,
                style: {
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                    lineHeight: 'normal',
                }
            }, displayValue)
        }

        // 编辑状态：渲染组件
        const baseProps = {
            // ✅ 使用稳定的key，避免输入时重新创建组件导致失焦
            key: Component === ElInput 
                ? `${row.ID}_${column.field}_stable`  // ElInput使用稳定key
                : `${row.ID}_${row[column.field]}`,   // 其他组件保持动态key
            modelValue: row[column.field],
            placeholder: options.placeholder || '',
            id: `${row.ID}_${column.field}`,
            style: { width: '100%', height: '100%' },
            cellMode: true,
            title: options.getProps?.(row, column)?.title || row[options.displayField],
            ...options.getProps?.(row, column)
        }

        const baseEvents = {
            'onUpdate:modelValue': (val) => {
                const oldValue = row[column.field]

                // 特殊处理：助教字段需要将数组转换为字符串
                let finalValue = val
                if (column.field === 'AssistantTeacherID' && Array.isArray(val)) {
                    finalValue = val.join(',')
                }
                // 日期清空：null -> ''
                if (column.field === 'Date' && (val === null || val === undefined)) {
                    finalValue = ''
                }
                // 上课教室清空：null -> ''
                if (column.field === 'ClassRoomID' && (val === null || val === undefined)) {
                    finalValue = ''
                }

                row[column.field] = finalValue

                // 通用字段清理逻辑：当值被清空时，清理相关字段
                if (!finalValue && oldValue) {
                    clearRelatedFields(row, column.field, selectedTableType.value)
                }
                // 值变化时，若该字段在错误列表中，立即移除错误标记
                // 🔧 但要保留任课老师和助教重复校验的错误，需要重新校验后再决定
                if (Array.isArray(row.errorField)) {
                    const idx = row.errorField.indexOf(column.field)
                    if (idx > -1) {
                        // 检查是否是任课老师与助教重复的错误
                        const isDuplicateError =
                            (column.field === 'MainTeacherID' || column.field === 'AssistantTeacherID') &&
                            row.errorMessages[column.field] === customTrips

                        if (!isDuplicateError) {
                            // 非重复校验错误，直接清除
                            row.errorField.splice(idx, 1)
                            // 同时清理错误信息
                            if (row.errorMessages && row.errorMessages[column.field]) {
                                delete row.errorMessages[column.field]
                            }
                        }
                        // 如果是重复校验错误，留给后续的校验逻辑处理
                    }
                }

                // 处理必填字段校验错误的实时清除
                const rowErrorFields = validationErrorFields.value.get(row.ID)
                if (rowErrorFields && rowErrorFields.includes(column.field)) {
                    // 当字段有值时，清除该字段的必填校验错误
                    let isValueValid = finalValue && finalValue !== ''
                    
                    // 🆕 特殊处理：可约人数和开课人数，'0'也视为无效值
                    if ((column.field === 'MaxStudentCount' || column.field === 'StartStudentCount') && finalValue === '0') {
                        isValueValid = false
                    }
                    
                    if (isValueValid) {
                        const updatedErrorFields = rowErrorFields.filter(field => field !== column.field)
                        if (updatedErrorFields.length === 0) {
                            // 如果该行没有错误字段了，完全移除
                            validationErrorFields.value.delete(row.ID)
                            // 同时从错误行ID列表中移除
                            const errorRowIndex = validationErrorRowIds.value.indexOf(row.ID)
                            if (errorRowIndex > -1) {
                                validationErrorRowIds.value.splice(errorRowIndex, 1)
                            }
                        } else {
                            // 更新错误字段列表
                            validationErrorFields.value.set(row.ID, updatedErrorFields)
                        }
                    }
                }

                // 🆕 特殊处理：开放预约改为"否"时，清除可约人数和开课人数的校验错误（仅预约课排课）
                if (column.field === 'IsSubscribeCourse' && finalValue === '0' && selectedTableType.value === 30) {
                    clearSubscribeCourseRelatedErrors(row.ID)
                }

                options.onUpdateModelValue?.(row, column, val)
                cellDataChange(row, column, finalValue, recordFieldChange)
            },
            ...options.getEvents?.(row, column)
        }

        // ==================== 🆕 编辑状态保护：记录编辑状态 ====================
        const cellKey = `${row.ID}_${column.field}`
        
        // 添加编辑状态追踪事件
        const editStateEvents = {
            // 输入框聚焦时 - 标记为正在编辑
            onFocus: () => {
                editingCells.value.add(cellKey)
                activeFocusedCell.value = cellKey
                editingCellTimestamps.value.set(cellKey, Date.now())
                // console.log(`📝 Start editing: ${cellKey}`)
            },
            // 输入框失焦时 - 延迟移除编辑标记（给保存响应时间）
            onBlur: () => {
                setTimeout(() => {
                    editingCells.value.delete(cellKey)
                    editingCellTimestamps.value.delete(cellKey)
                    if (activeFocusedCell.value === cellKey) {
                        activeFocusedCell.value = null
                    }
                    // console.log(`✅ End editing: ${cellKey}`)
                }, 100)  // 100ms延迟，确保保存响应能看到编辑状态
            },
            // 下拉框展开/收起时 - ElSelect等组件使用
            'onVisible-change': (visible) => {
                if (visible) {
                    editingCells.value.add(cellKey)
                    activeFocusedCell.value = cellKey
                    editingCellTimestamps.value.set(cellKey, Date.now())
                    // 🔒 锁定 UI - 防止保存响应更新数据
                    uiInteractionLock.value = true
                    // console.log(`� Dropdown opened: ${cellKey}, UI锁定`)
                } else {
                    setTimeout(() => {
                        editingCells.value.delete(cellKey)
                        editingCellTimestamps.value.delete(cellKey)
                        if (activeFocusedCell.value === cellKey) {
                            activeFocusedCell.value = null
                        }
                        // 🔓 释放 UI 锁 - 允许处理待处理的保存响应
                        uiInteractionLock.value = false
                        // console.log(`✅ Dropdown closed: ${cellKey}, UI解锁`)
                        // 处理队列中的保存响应
                        processPendingSaveResponses()
                    }, 100)
                }
            }
        }

        // 合并编辑状态事件到baseEvents（避免覆盖已有事件）
        const mergedEvents = { ...baseEvents }
        Object.keys(editStateEvents).forEach(eventName => {
            const existingHandler = mergedEvents[eventName]
            const newHandler = editStateEvents[eventName]
            
            if (existingHandler) {
                // 如果已有事件处理器，合并调用
                mergedEvents[eventName] = (...args) => {
                    existingHandler(...args)
                    newHandler(...args)
                }
            } else {
                // 如果没有现有处理器，直接使用新处理器
                mergedEvents[eventName] = newHandler
            }
        })

        // 特殊处理：ref回调（用于TimeSelect等需要存储引用的组件）
        if (options.getRef) {
            baseProps.ref = options.getRef(row, column)
        } else if (Component === ElInput && options.autoFocus !== false) {
            // 🆕 ElInput 默认自动聚焦（可通过 autoFocus: false 禁用）
            baseProps.ref = (el) => {
                if (el && el.$el) {
                    // 使用 nextTick 确保 DOM 已渲染
                    nextTick(() => {
                        const inputElement = el.$el.querySelector('input')
                        if (inputElement) {
                            inputElement.focus()
                            // 可选：选中所有文本（方便快速替换）
                            if (options.selectAllOnFocus) {
                                inputElement.select()
                            }
                        }
                    })
                }
            }
        }

        // 特殊处理：ElSelect的default插槽（用于CourseType、IsSubscribeCourse等）
        if (options.getDefaultSlot) {
            return h(Component, {
                ...baseProps,
                ...mergedEvents
            }, {
                default: options.getDefaultSlot(row, column, h)
            })
        }

        return h(Component, {
            ...baseProps,
            ...mergedEvents
        })
    }
}

// 组件依赖数据状态
const campusId = ref('')
const classId = ref('')
const studentUserId = ref('')

// 校区数据管理
const userCampusesStore = useUserCampuses()

// 校区ID到名称的映射
const campusNameMap = computed(() => {
    const map = {}
    userCampusesStore.userCampuses.forEach(campus => {
        map[campus.ID] = campus.Name
    })
    return map
})

// 根据校区ID获取校区名称
const getCampusName = (campusId) => {
    return campusNameMap.value[campusId] || campusId || ''
}

// 班级数据管理
const classDataMap = ref(new Map()) // 存储班级ID到班级对象的映射

// 学员数据管理
const studentUserDataMap = ref(new Map()) // 存储学员ID到学员对象的映射



// 更新班级数据映射
const updateClassDataMap = (classId, classData) => {
    if (classId && classData) {
        classDataMap.value.set(classId, classData)
    }
}

// 更新学员数据映射
const updateStudentUserDataMap = (studentUserId, studentUserData) => {
    if (studentUserId && studentUserData) {
        studentUserDataMap.value.set(studentUserId, studentUserData)
    }
}

// 课程数据管理
const courseDataMap = ref(new Map()) // 存储课程ID到课程对象的映射



// 更新课程数据映射
const updateCourseDataMap = (courseId, courseData) => {
    if (courseId && courseData) {
        courseDataMap.value.set(courseId, courseData)
    } else {
    }
}

// 教室数据管理
const classroomDataMap = ref(new Map()) // 存储教室ID到教室对象的映射



// 更新教室数据映射
const updateClassroomDataMap = (classroomId, classroomData) => {
    if (classroomId && classroomData) {
        classroomDataMap.value.set(classroomId, classroomData)
    }
}

// 教师数据管理
const teacherDataMap = ref(new Map()) // 存储教师ID到教师对象的映射



// 更新教师数据映射
const updateTeacherDataMap = (teacherId, teacherData) => {
    if (teacherId && teacherData) {
        teacherDataMap.value.set(teacherId, teacherData)
    }
}

// 助教数据管理
const assistantDataMap = ref(new Map()) // 存储助教ID到助教对象的映射



// 更新助教数据映射
const updateAssistantDataMap = (assistantId, assistantData) => {
    if (assistantId && assistantData) {
        assistantDataMap.value.set(assistantId, assistantData)
    }
}

// 鼠标悬浮状态管理
const hoveredRowKey = ref(null) // 当前悬浮的行key
const checkedRows = ref(new Set()) // 已勾选的行key集合

// 获取已勾选的行数据
const getCheckedRows = () => {
    return tableData.value.filter(row => checkedRows.value.has(row.ID))
}

// 清空所有勾选
const clearCheckedRows = () => {
    checkedRows.value.clear()
}

// 勾选所有行
const checkAllRows = () => {
    tableData.value.forEach(row => {
        if (!row.isGroupRow && !row.isGroupFooter) {
            checkedRows.value.add(row.ID)
        }
    })
}

// 获取勾选状态
const isRowChecked = (rowId) => {
    return checkedRows.value.has(rowId)
}


// 获取勾选行的数量
const checkedRowsCount = computed(() => checkedRows.value.size)

// 全选状态计算
const isAllChecked = computed(() => {
    const dataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter)
    return dataRows.length > 0 && dataRows.every(row => checkedRows.value.has(row.ID))
})

// 半选状态计算（部分选中）
const isIndeterminate = computed(() => {
    const dataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter)
    const checkedCount = checkedRows.value.size
    return checkedCount > 0 && checkedCount < dataRows.length
})


/**
 * 根据分组字段和分组值，获取用于显示的分组名称
 * @param {string} groupField - 分组字段
 * @param {string} groupKey - 分组值
 * @returns {string}
 */
const getGroupDisplayName = (groupField, groupKey) => {
    switch (groupField) {
        case 'ClassID':
            const classData = classDataMap.value.get(groupKey)
            return classData ? classData.Name : groupKey
        case 'StudentUserID':
            const studentUserData = studentUserDataMap.value.get(groupKey)
            return studentUserData ? studentUserData.Name : groupKey
        case 'ShiftID':
            const courseData = courseDataMap.value.get(groupKey)
            return courseData ? courseData.Name : groupKey
        case 'ClassRoomID':
            const classroomData = classroomDataMap.value.get(groupKey)
            return classroomData ? classroomData.Name : groupKey
        case 'SubjectID':
            return getSubjectName(groupKey)
        case 'CampusID':
            return getCampusName(groupKey)
        case 'MainTeacherID':
            const teacherData = teacherDataMap.value.get(groupKey)
            return teacherData ? teacherData.Name : groupKey
        case 'AssistantTeacherID':
            // 助教可能是逗号分隔的多个ID
            if (groupKey && groupKey.includes(',')) {
                const assistantIds = groupKey.split(',').map(id => id.trim()).filter(Boolean)
                const assistantNames = assistantIds.map(id => {
                    const assistantData = assistantDataMap.value.get(id)
                    return assistantData ? assistantData.Name : id
                })
                return assistantNames.join(', ')
            } else {
                const assistantData = assistantDataMap.value.get(groupKey)
                return assistantData ? assistantData.Name : groupKey
            }
        case 'IsSubscribeCourse':
            // 数值型字段：0=不开放预约，1=开放预约
            if (groupKey === '0') return '不开放预约'
            if (groupKey === '1') return '开放预约'
            return `预约状态${groupKey}`
        case 'CourseType':
            // 根据表单定义：1=线下课，2=线上课
            if (groupKey === '1') return '线下课'
            if (groupKey === '2') return '线上课'
            return transToConfigDescript(`课程类型${groupKey}`)
        default:
            return groupKey
    }
}

const copyData = ref([])
const copystr = ref('')
const isShowWarningMessage = ref(false);
const isShowSuccessMessage = ref(false);
function showPasteWarning() {
    if (!isShowWarningMessage.value) {
        ElMessage.warning("粘贴失败，粘贴内容与当前字段类型或数值范围不符")
        isShowWarningMessage.value = true;
    }
}
// 剪贴板配置
const clipboardOption = reactive({
    copy: true,
    paste: true,
    cut: true,
    delete: true,
    beforeCopy: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
        // 分组不支持复制
        if (selectionRangeKeys.startRowKey.startsWith('group_')) {
            // 写入""给copy
            navigator.clipboard.writeText('')
            return false
        }

        // 🆕 立即显示复制提示
        const cellCount = calculateCellCount(selectionRangeIndexes)
        ElMessage.success(`已复制 ${cellCount} 个单元格`)

        // 🆕 复用通用的数据收集逻辑
        collectDataForClipboard(selectionRangeIndexes, false) // false表示复制操作
        return true
    },
    afterCopy: (e) => {
        navigator.clipboard.readText().then(res => {
            copystr.value = res
        })
    },
    beforePaste: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
        isShowWarningMessage.value = false;
        isShowSuccessMessage.value = false;
        // 分组不支持粘贴
        if (selectionRangeKeys.startRowKey.startsWith('group_')) {
            navigator.clipboard.writeText('')
            return false
        }
        // 如果没有粘贴数据就取消操作
        if (!data || !data.length) {
            return false
        }

        // 用和beforeCopy同样的方式，找到对应的几行
        const targetRows = displayTableData.value.filter((_, index) => {
            return index >= selectionRangeIndexes.startRowIndex && index <= selectionRangeIndexes.endRowIndex
        }).filter(row => !row.isGroupRow && !row.isGroupFooter)
        function pasteData() {
            // 外部粘贴
            if (copyData.value.length === 0) {
                // 如果没有copyData，则说明是外部粘贴
                // 根据fieldMap规则，直接赋值对应的名称字段
                data.forEach((dataRow, dataIndex) => {
                    if (targetRows[dataIndex]) {
                        const targetRow = targetRows[dataIndex]
                        // 遍历data中的每个字段
                        Object.keys(dataRow).forEach(field => {
                            if (field !== 'ID' && fieldMap[field]) {
                                // 科目字段统一处理
                                if (field === 'SubjectID' || field === 'SubjectName') {
                                    // 校验仍保留：当格式错误时提示并中断
                                    const idVal = dataRow['SubjectID']
                                    const nameVal = dataRow['SubjectName']
                                    if ((idVal !== undefined && fieldFormatCheck['SubjectID'](idVal) === false) ||
                                        (nameVal !== undefined && fieldFormatCheck['SubjectName'](nameVal) === false)) {
                                        showPasteWarning()
                                        return
                                    }

                                    applySubjectChange(
                                        targetRow,
                                        { id: idVal, name: nameVal },
                                        targetRow.EnableSubject == '1' || targetRow.EnableSubject == 1,
                                        recordFieldChange,
                                        'paste'
                                    )
                                    return
                                }

                                // 检验顺便格式化
                                let result = fieldFormatCheck[field](dataRow[field])
                                if (result === false) {
                                    showPasteWarning()
                                    return
                                } else {
                                    targetRow[field] = result
                                    // 直接记录字段变更
                                    recordFieldChange(targetRow.ID, field, result, 'paste')
                                }
                                // 如果字段有映射，则赋值映射字段，异常数据
                                if (fieldMap[field] !== field) {
                                    targetRow[fieldMap[field]] = dataRow[field]
                                    // 直接记录映射字段变更
                                    recordFieldChange(targetRow.ID, fieldMap[field], dataRow[field], 'paste')
                                }
                            }
                        })
                        // 同步更新到tableData.value中
                        const tableDataIndex = tableData.value.findIndex(row => row.ID === targetRow.ID)
                        if (tableDataIndex !== -1) {
                            // 使用精细更新，避免整行替换导致组件重新渲染失去焦点
                            Object.assign(tableData.value[tableDataIndex], targetRow)
                            // 同步更新主教师传输字段
                            if (targetRow.MainTeacherID && targetRow.MainTeacherName) {
                                // 什么情况下会走到这里？
                                targetRow.MainTeacherList = [{
                                    ID: targetRow.MainTeacherID,
                                    Name: targetRow.MainTeacherName,
                                }]
                                // 记录 MainTeacherList 的变更
                                recordFieldChange(targetRow.ID, 'MainTeacherList', targetRow.MainTeacherList, 'paste')
                            } else {
                                targetRow.MainTeacherList = []
                                // 记录 MainTeacherList 的变更
                                recordFieldChange(targetRow.ID, 'MainTeacherList', [], 'paste')
                            }

                            // 同步更新助教传输字段
                            if (targetRow.AssistantTeacherID && targetRow.AssistantTeacherName) {
                                const assistantIds = targetRow.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean)
                                const assistantNames = targetRow.AssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean)
                                targetRow.AssistantTeacherList = assistantIds.map((id, idx) => ({
                                    ID: id,
                                    Name: assistantNames[idx] || id,
                                }))
                                // 记录 AssistantTeacherList 的变更
                                recordFieldChange(targetRow.ID, 'AssistantTeacherList', targetRow.AssistantTeacherList, 'paste')
                            } else {
                                targetRow.AssistantTeacherList = []
                                // 记录 AssistantTeacherList 的变更
                                recordFieldChange(targetRow.ID, 'AssistantTeacherList', [], 'paste')
                            }
                        }
                    }
                })

                // 🆕 外部粘贴完成后，对涉及教师字段的行进行重复校验
                const hasTeacherFields = data.some(dataRow =>
                    Object.keys(dataRow).some(field =>
                        field === 'MainTeacherID' || field === 'MainTeacherName' ||
                        field === 'AssistantTeacherID' || field === 'AssistantTeacherName'
                    )
                )
                if (hasTeacherFields) {
                    batchValidateTeacherAssistant(targetRows)
                }

                return false
            }

            // 获取要粘贴的列字段 - 按照copyData里面实际复制的字段来
            const columnFields = copyData.value.length > 0 ? Object.keys(copyData.value[0]).filter(key => key !== 'ID') : []
            // 按照数据顺序，合并data里面的字段数据到targetRows
            copyData.value.forEach((dataRow, dataIndex) => {
                if (targetRows[dataIndex]) {
                    const targetRow = targetRows[dataIndex]

                    // 遍历要粘贴的字段，合并数据
                    columnFields.forEach(field => {
                        if (!dataRow.hasOwnProperty(field)) return

                        // 科目字段统一处理
                        if (field === 'SubjectID' || field === 'SubjectName') {
                            applySubjectChange(
                                targetRow,
                                { id: dataRow['SubjectID'], name: dataRow['SubjectName'] },
                                targetRow.EnableSubject === '1' || targetRow.EnableSubject === 1,
                                recordFieldChange,
                                'paste'
                            )
                            return
                        }

                        // 普通字段11
                        targetRow[field] = dataRow[field]
                        recordFieldChange(targetRow.ID, field, dataRow[field], 'paste')
                        // 如果复制的是ID字段，同时设置对应的名称字段
                        if (fieldMap[field] && dataRow[fieldMap[field]]) {
                            targetRow[fieldMap[field]] = dataRow[fieldMap[field]]
                            recordFieldChange(targetRow.ID, fieldMap[field], dataRow[fieldMap[field]], 'paste')
                        }
                    })

                    // 同步更新到tableData.value中
                    const tableDataIndex = tableData.value.findIndex(row => row.ID === targetRow.ID)
                    if (tableDataIndex !== -1) {
                        // 使用精细更新，避免整行替换导致组件重新渲染失去焦点
                        Object.assign(tableData.value[tableDataIndex], targetRow)
                        // 同步更新主教师传输字段
                        if (targetRow.MainTeacherID && targetRow.MainTeacherName) {
                            // targetRow.MainTeacherList = [{
                            //     ID: targetRow.MainTeacherID,
                            //     Name: targetRow.MainTeacherName,
                            //     TeacherCommissionList: dataRow.MainTeacherList && dataRow.MainTeacherList.length > 0 ?
                            //         dataRow.MainTeacherList[0].TeacherCommissionList : [],
                            // }]
                            // 记录 MainTeacherList 的变更
                            recordFieldChange(targetRow.ID, 'MainTeacherList', targetRow.MainTeacherList, 'paste')
                        } else {
                            targetRow.MainTeacherList = []
                            // 记录 MainTeacherList 的变更
                            recordFieldChange(targetRow.ID, 'MainTeacherList', [], 'paste')
                        }

                        // 同步更新助教传输字段
                        if (targetRow.AssistantTeacherID && targetRow.AssistantTeacherName) {
                            const assistantIds = targetRow.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean)
                            const assistantNames = targetRow.AssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean)
                            targetRow.AssistantTeacherList = assistantIds.map((id, idx) => ({
                                ID: id,
                                Name: assistantNames[idx] || id,
                                TeacherCommissionList: assistantDataMap.value.get(id) ? assistantDataMap.value.get(id).TeacherCommissionList : [],
                            }))
                            // 记录 AssistantTeacherList 的变更
                            recordFieldChange(targetRow.ID, 'AssistantTeacherList', targetRow.AssistantTeacherList, 'paste')
                        } else {
                            targetRow.AssistantTeacherList = []
                            // 记录 AssistantTeacherList 的变更
                            recordFieldChange(targetRow.ID, 'AssistantTeacherList', [], 'paste')
                        }

                        if (!isShowSuccessMessage.value) {
                            // ElMessage.success('已为你自动粘贴到对应的列');
                            isShowSuccessMessage.value = true;
                        }
                        // console.log('已更新tableData中的粘贴行:', tableDataIndex)
                    }
                }
            })

            // 🆕 粘贴完成后，对涉及教师字段的行进行重复校验
            const hasTeacherFields = columnFields.some(field =>
                field === 'MainTeacherID' || field === 'MainTeacherName' ||
                field === 'AssistantTeacherID' || field === 'AssistantTeacherName'
            )
            if (hasTeacherFields) {
                batchValidateTeacherAssistant(targetRows)
            }

            // 🆕 特殊处理：粘贴"开放预约"为"否"时，清除可约人数和开课人数的校验错误（仅预约课排课）
            const hasIsSubscribeCourse = columnFields.includes('IsSubscribeCourse')
            if (hasIsSubscribeCourse && selectedTableType.value === 30) {
                targetRows.forEach(targetRow => {
                    if (targetRow.IsSubscribeCourse === '0' || !targetRow.IsSubscribeCourse) {
                        // 清除可约人数和开课人数的校验错误
                        clearSubscribeCourseRelatedErrors(targetRow.ID)
                    }
                })
            }
        }
        navigator.clipboard.readText().then(res => {
            if (res !== copystr.value) {
                // console.log(res, copystr.value)
                copyData.value = []
                copystr.value = ''
                // console.log('粘贴数据与复制数据不一致，清空copyData')
            }
            pasteData()
        }).catch(error => {
            console.warn("粘贴失败，请检查浏览器权限")
            pasteData()
        })

        return false
    },
    beforeCut: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
        // console.log('=== 剪切前回调 ===')
        // console.log('剪切的数据:', data)

        // 🆕 立即显示剪切提示
        const cellCount = calculateCellCount(selectionRangeIndexes)
        ElMessage.success(`已剪切 ${cellCount} 个单元格`)

        // 分组不支持复制
        if (selectionRangeKeys.startRowKey.startsWith('group_')) {
            navigator.clipboard.writeText('')
            return false
        }
        // 🆕 收集剪切数据（复用通用的数据收集逻辑）
        collectDataForClipboard(selectionRangeIndexes, true) // true表示剪切操作
        return true
    },
    afterCut: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
        // console.log('=== 剪切后回调 ===')
        // console.log('剪切完成:', data)

        // 🆕 清理剪切后的原字段值，并记录变更
        if (selectionRangeIndexes && displayTableData.value) {
            // 获取剪切的行范围
            const startRowIndex = selectionRangeIndexes.startRowIndex
            const endRowIndex = selectionRangeIndexes.endRowIndex

            // 获取剪切的列范围
            const startColIndex = selectionRangeIndexes.startColIndex
            const endColIndex = selectionRangeIndexes.endColIndex

            // 遍历剪切的区域
            for (let rowIndex = startRowIndex; rowIndex <= endRowIndex; rowIndex++) {
                const targetRow = displayTableData.value[rowIndex]
                if (targetRow && !targetRow.isGroupRow && !targetRow.isGroupFooter) {

                    // 遍历剪切的列
                    for (let colIndex = startColIndex; colIndex <= endColIndex; colIndex++) {
                        // 使用可见列索引，过滤隐藏列及预检查列
                        const hiddenKeys = columnHiddenOption.value?.defaultHiddenColumnKeys || []
                        const visibleColumns = getClipboardVisibleColumns(columns, hiddenKeys)
                        const column = visibleColumns[colIndex]
                        if (column && column.field) {
                            // 清理被剪切字段的相关名称
                            clearRelatedFields(targetRow, column.field, selectedTableType.value)

                            // 🆕 记录字段变更到变更收集器
                            recordFieldChange(targetRow.ID, column.field, '', 'cut')

                            // 🆕 同时记录相关名称字段的变更
                            const nameField = fieldMap[column.field]
                            if (nameField) {
                                recordFieldChange(targetRow.ID, nameField, '', 'cut')
                            }

                            // timeRange 被清空时，同步清空 StartTime/EndTime
                            if (column.field === 'timeRange') {
                                targetRow.timeRange = ''
                                targetRow.StartTime = ''
                                targetRow.EndTime = ''
                                recordFieldChange(targetRow.ID, 'timeRange', '', 'cut')
                                recordFieldChange(targetRow.ID, 'StartTime', '', 'cut')
                                recordFieldChange(targetRow.ID, 'EndTime', '', 'cut')
                            }

                            // 🆕 特殊处理：如果是主教师字段，记录 MainTeacherList 的变更
                            if (column.field === 'MainTeacherID') {
                                recordFieldChange(targetRow.ID, 'MainTeacherList', [], 'cut')
                            }

                            // 🆕 特殊处理：如果是助教字段，记录 AssistantTeacherList 的变更
                            if (column.field === 'AssistantTeacherID') {
                                recordFieldChange(targetRow.ID, 'AssistantTeacherList', [], 'cut')
                            }
                        }
                    }

                    // 🆕 同步到 tableData.value
                    const tableDataIndex = tableData.value.findIndex(row => row.ID === targetRow.ID)
                    if (tableDataIndex !== -1) {
                        // 直接引用 displayTableData 中已清理的行数据
                        // 使用精细更新，避免整行替换导致组件重新渲染失去焦点
                        Object.assign(tableData.value[tableDataIndex], targetRow)
                        // console.log(`✅ 剪切后，已同步到 tableData 行 ${targetRow.ID}`)
                    }
                }
            }
        }

        // 🆕 剪切后触发任课老师与助教重复校验
        if (selectionRangeIndexes && displayTableData.value) {
            const startRowIndex = selectionRangeIndexes.startRowIndex
            const endRowIndex = selectionRangeIndexes.endRowIndex
            const startColIndex = selectionRangeIndexes.startColIndex
            const endColIndex = selectionRangeIndexes.endColIndex
            const hiddenKeys = columnHiddenOption.value?.defaultHiddenColumnKeys || []
            const visibleColumns = getClipboardVisibleColumns(columns, hiddenKeys)

            // 检查是否剪切了任课老师或助教字段，如果是则触发重复校验
            if (hasTeacherFieldsInColumns(startColIndex, endColIndex, visibleColumns)) {
                const affectedRows = displayTableData.value
                    .slice(startRowIndex, endRowIndex + 1)
                    .filter(row => row && !row.isGroupRow && !row.isGroupFooter)
                batchValidateTeacherAssistant(affectedRows)
            }
        }

        // 读取剪贴板
        navigator.clipboard.readText().then(res => {
            copystr.value = res
        })

        // 如果分组模式下剪切了数据，需要重新计算分组
        if (isGrouped.value && groupedDataCache.value) {
            // console.log('分组模式下剪切数据，重新计算分组')
            groupedDataCache.value.isValid = false
        }
    },
    beforeDelete: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {
        // console.log('=== 删除前回调 ===')
        // console.log('删除的数据:', data)
        // 不允许删除分组行和footer行
        if (data && Array.isArray(data)) {
            const hasGroupRows = data.some(row => row.isGroupRow || row.isGroupFooter)
            if (hasGroupRows) {
                // console.log('不能删除分组行或footer行')
                return false
            }
        }
        return true
    },
    afterDelete: ({ data, selectionRangeIndexes, selectionRangeKeys }) => {

        // 当删除操作完成后，清理相关字段的显示名称
        if (selectionRangeIndexes && displayTableData.value) {
            // 获取删除的行范围
            const startRowIndex = selectionRangeIndexes.startRowIndex
            const endRowIndex = selectionRangeIndexes.endRowIndex

            // 🆕 获取删除的列范围（支持多列删除）
            const startColIndex = selectionRangeIndexes.startColIndex
            const endColIndex = selectionRangeIndexes.endColIndex
            const hiddenKeys = columnHiddenOption.value?.defaultHiddenColumnKeys || []
            const visibleColumns = getClipboardVisibleColumns(columns, hiddenKeys)

            // console.log(`🗑️ 删除操作 - 行范围: ${startRowIndex}-${endRowIndex}, 列范围: ${startColIndex}-${endColIndex}`)
            // console.log(`📊 当前表格类型: ${selectedTableType.value}, 隐藏字段:`, hiddenKeys)
            // console.log(`📋 可见列总数: ${visibleColumns.length}, 全部列数: ${columns.value.length}`)

            // 🆕 遍历所有被删除的列
            for (let colIndex = startColIndex; colIndex <= endColIndex; colIndex++) {
                const column = visibleColumns[colIndex]
                if (column && column.field) {
                    // console.log(`🗑️ 处理删除列: ${column.field}`)

                    // 遍历删除的行，清理相关字段
                    for (let rowIndex = startRowIndex; rowIndex <= endRowIndex; rowIndex++) {
                        const targetRow = displayTableData.value[rowIndex]
                        if (targetRow && !targetRow.isGroupRow && !targetRow.isGroupFooter) {
                            // 清理被删除字段的相关名称
                            clearRelatedFields(targetRow, column.field, selectedTableType.value)

                            // 🆕 记录字段变更到变更收集器
                            recordFieldChange(targetRow.ID, column.field, '', 'delete')

                            // 🆕 同时记录相关名称字段的变更
                            const nameField = fieldMap[column.field]
                            if (nameField) {
                                recordFieldChange(targetRow.ID, nameField, '', 'delete')
                            }

                            // timeRange 删除时，同步清空 StartTime/EndTime
                            if (column.field === 'timeRange') {
                                targetRow.timeRange = ''
                                targetRow.StartTime = ''
                                targetRow.EndTime = ''
                                recordFieldChange(targetRow.ID, 'timeRange', '', 'delete')
                                recordFieldChange(targetRow.ID, 'StartTime', '', 'delete')
                                recordFieldChange(targetRow.ID, 'EndTime', '', 'delete')
                            }

                            // 🆕 特殊处理：如果是主教师字段，记录 MainTeacherList 的变更
                            if (column.field === 'MainTeacherID') {
                                recordFieldChange(targetRow.ID, 'MainTeacherList', [], 'delete')
                            }

                            // 🆕 特殊处理：如果是助教字段，记录 AssistantTeacherList 的变更
                            if (column.field === 'AssistantTeacherID') {
                                recordFieldChange(targetRow.ID, 'AssistantTeacherList', [], 'delete')
                            }

                            // console.log(`✅ 已清理行 ${targetRow.ID} 的字段 ${column.field}`)
                        }
                    }
                }
            }

            // 🆕 批量同步到 tableData.value（在所有列处理完成后统一同步）
            const affectedRows = [] // 记录受影响的行，用于后续校验
            for (let rowIndex = startRowIndex; rowIndex <= endRowIndex; rowIndex++) {
                const targetRow = displayTableData.value[rowIndex]
                if (targetRow && !targetRow.isGroupRow && !targetRow.isGroupFooter) {
                    const tableDataIndex = tableData.value.findIndex(row => row.ID === targetRow.ID)
                    if (tableDataIndex !== -1) {
                        // 使用精细更新，避免整行替换导致组件重新渲染失去焦点
                        Object.assign(tableData.value[tableDataIndex], targetRow)
                        affectedRows.push(targetRow)
                        // console.log(`✅ Delete键删除后，已同步到 tableData 行 ${targetRow.ID}`)
                    }
                }
            }

            // 🆕 检查是否删除了任课老师或助教字段，如果是则触发重复校验
            if (hasTeacherFieldsInColumns(startColIndex, endColIndex, visibleColumns)) {
                batchValidateTeacherAssistant(affectedRows)
            }

            // 🆕 特殊处理：删除"开放预约"字段时（变为默认值"否"），清除可约人数和开课人数的校验错误（仅预约课排课）
            if (selectedTableType.value === 30) {
                for (let colIndex = startColIndex; colIndex <= endColIndex; colIndex++) {
                    const column = visibleColumns[colIndex]
                    if (column && column.field === 'IsSubscribeCourse') {
                        // 遍历删除的行，清除相关错误
                        for (let rowIndex = startRowIndex; rowIndex <= endRowIndex; rowIndex++) {
                            const targetRow = displayTableData.value[rowIndex]
                            if (targetRow && !targetRow.isGroupRow && !targetRow.isGroupFooter) {
                                // 删除后IsSubscribeCourse变为空或'0'（否），需要清除可约人数和开课人数的错误
                                clearSubscribeCourseRelatedErrors(targetRow.ID)
                            }
                        }
                        break // 只需要处理一次
                    }
                }
            }
        }

        // 如果分组模式下删除了数据，需要重新计算分组
        if (isGrouped.value && groupedDataCache.value) {
            // console.log('分组模式下删除数据，重新计算分组')
            groupedDataCache.value.isValid = false
        }
    },
})

const clickGroupRow = ref(true)
// 事件自定义配置
const eventCustomOption = reactive({
    // body 列事件自定义
    bodyCellEvents: ({ row, column, rowIndx }) => {
        return {
            click: (event) => {
                if (column.field == '') {
                    clickGroupRow.value = true;
                    return
                }
                clickGroupRow.value = false
                // 设置编辑状态
                cellDbClickKey.value = { rowId: row.ID, field: column.field };
            },
            contextmenu: (event) => { },
            mouseenter: (event) => {
                // 设置当前悬浮的行key
                hoveredRowKey.value = row.ID
            },
            mouseleave: (event) => {
                // 清除悬浮状态，但保持已勾选的状态
                hoveredRowKey.value = null
            },
        };
    },
    // footer 列事件自定义
    footerCellEvents: ({ row, column, rowIndex }) => {
        return {
            click: (event) => {
                clickGroupRow.value = true
                // footer点击事件处理
            },
        };
    },

})

// 单元格自动填充配置
const cellAutofillOption = reactive({
    directionX: false,
    directionY: true,
    beforeAutofill: ({
        direction,
        sourceSelectionRangeIndexes,
        targetSelectionRangeIndexes,
        sourceSelectionData,
        targetSelectionData,
    }) => {
        // 检查源数据是否包含分组行或footer行
        if (sourceSelectionData && Array.isArray(sourceSelectionData)) {
            const hasGroupRowsInSource = sourceSelectionData.some(row => row.isGroupRow || row.isGroupFooter)
            if (hasGroupRowsInSource) {
                return false
            }
        }
        // 检查目标区域是否包含分组行或footer行
        if (targetSelectionRangeIndexes && Array.isArray(targetSelectionRangeIndexes) && displayTableData.value) {
            const targetRows = targetSelectionRangeIndexes.map(index => displayTableData.value[index]).filter(Boolean)
            const hasGroupRowsInTarget = targetRows.some(row => row.isGroupRow || row.isGroupFooter)
            if (hasGroupRowsInTarget) {
                return false
            }
        }
        // 类型合理性快速校验：仅对起始列做一次
        if (sourceSelectionData && Array.isArray(sourceSelectionData) && targetSelectionRangeIndexes && Array.isArray(targetSelectionRangeIndexes)) {
            const firstSource = sourceSelectionData[0]
            const firstTargetIndex = targetSelectionRangeIndexes[0]
            const firstTargetRow = displayTableData.value[firstTargetIndex]
            if (firstSource && firstTargetRow) {
                // 自动填充前，基于可见列重新对齐列索引，避免包含预检查提示列
                const hiddenKeys = columnHiddenOption.value?.defaultHiddenColumnKeys || []
                const visibleColumns = getClipboardVisibleColumns(columns, hiddenKeys)
                // 类型合理性检查沿用原逻辑
                Object.keys(firstSource).forEach(field => {
                    const t = getFieldType(field)
                    const v = firstSource[field]
                    if (t === 'date' && v && !/^\d{4}-\d{2}-\d{2}$/.test(String(v))) {
                        return false
                    }
                    if (t === 'time' && v && !/^\d{2}:\d{2}$/.test(String(v))) {
                        // 自动填充仅接受单个时间，范围仅用于粘贴
                        return false
                    }
                    if (t === 'number' && v !== '' && isNaN(Number(v))) {
                        return false
                    }
                })
            }
        }
        return true
    },
    afterAutofill: ({
        direction,
        sourceSelectionRangeIndexes,
        targetSelectionRangeIndexes,
        sourceSelectionData,
        targetSelectionData,
    }) => {
        // 处理名称快照的复制
        if (sourceSelectionData && Array.isArray(sourceSelectionData) && sourceSelectionData.length > 0 &&
            targetSelectionData && Array.isArray(targetSelectionData) && targetSelectionData.length > 0) {
            
            // 🔧 修复：按索引一一对应，source循环控制
            // 自动填充逻辑：源数据会循环填充到目标区域
            // 例如：2个源行可以填充4个目标行 (S1→T1, S2→T2, S1→T3, S2→T4)
            targetSelectionData.forEach((targetRowData, targetIndex) => {
                // 计算对应的源行索引（循环使用源数据）
                const sourceIndex = targetIndex % sourceSelectionData.length
                const sourceRowData = sourceSelectionData[sourceIndex]
                
                // 根据ID找到tableData中的源行和目标行
                const sourceRow = tableData.value.find(row => row.ID === sourceRowData.ID)
                const targetRow = tableData.value.find(row => row.ID === targetRowData.ID)
                
                if (!sourceRow || !targetRow) {
                    return
                }
                
                // 获取需要复制的字段（排除ID）
                const fieldsToCopy = Object.keys(sourceRowData).filter(key => key !== 'ID')

                // 遍历需要复制的字段
                fieldsToCopy.forEach(field => {
                    const fieldId = sourceRowData[field] // 源行的字段ID
                    const nameField = fieldMap[field] // 对应的名称字段

                    if (fieldId && nameField) {
                        // 科目：统一调用
                        if (field === 'SubjectID' || nameField === 'SubjectName') {
                            applySubjectChange(
                                targetRow,
                                { id: sourceRowData['SubjectID'], name: sourceRow[nameField] },
                                targetRow.EnableSubject === '1' || targetRow.EnableSubject === 1,
                                recordFieldChange,
                                'autofill'
                            )
                            return
                        }

                        // 从源行获取名称快照
                        const sourceName = sourceRow[nameField]
                        if (sourceName !== undefined) {
                            // 复制到目标行
                            targetRow[nameField] = sourceName
                        }
                    }
                })

                // 强制更新tableData中的对应行
                const tableDataIndex = tableData.value.findIndex(row => row.ID === targetRow.ID)
                if (tableDataIndex !== -1) {
                    // 使用精细更新，避免整行替换导致组件重新渲染失去焦点
                    Object.assign(tableData.value[tableDataIndex], targetRow)

                    // 同步更新主教师传输字段
                    if (targetRow.MainTeacherID && targetRow.MainTeacherName) {
                        // console.log(JSON.parse(JSON.stringify(sourceRow)),99999999)
                        targetRow.MainTeacherList = sourceRow.MainTeacherList;
                        // 记录 MainTeacherList 的变更
                        recordFieldChange(targetRow.ID, 'MainTeacherList', targetRow.MainTeacherList, 'autofill')
                    } else {
                        // ✅ 清空所有教师相关字段
                        targetRow.MainTeacherID = ''
                        targetRow.MainTeacherName = ''
                        targetRow.MainTeacherList = []
                        // 只记录 MainTeacherList 变更即可（后台只需要这个字段）
                        recordFieldChange(targetRow.ID, 'MainTeacherList', [], 'autofill')
                    }

                    // 同步更新助教传输字段
                    if (targetRow.AssistantTeacherID && targetRow.AssistantTeacherName) {
                        const assistantIds = targetRow.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean)
                        const assistantNames = targetRow.AssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean)
                        targetRow.AssistantTeacherList = assistantIds.map((id, idx) => ({
                            ID: id,
                            Name: assistantNames[idx] || id,
                            TeacherCommissionList: assistantDataMap.value.get(id) ? assistantDataMap.value.get(id).TeacherCommissionList : [],
                        }))
                        // 记录 AssistantTeacherList 的变更
                        recordFieldChange(targetRow.ID, 'AssistantTeacherList', targetRow.AssistantTeacherList, 'autofill')
                    } else {
                        // ✅ 清空所有助教相关字段
                        targetRow.AssistantTeacherID = ''
                        targetRow.AssistantTeacherName = ''
                        targetRow.AssistantTeacherList = []
                        // 记录所有字段的变更
                        recordFieldChange(targetRow.ID, 'AssistantTeacherID', '', 'autofill')
                        recordFieldChange(targetRow.ID, 'AssistantTeacherName', '', 'autofill')
                        recordFieldChange(targetRow.ID, 'AssistantTeacherList', [], 'autofill')
                    }

                    // 🆕 复用粘贴逻辑记录字段变更
                    fieldsToCopy.forEach(field => {
                        if (field !== 'ID') {
                            // 科目字段特殊处理：非全科课程行跳过科目字段
                            if ((field === 'SubjectID' || field === 'SubjectName') && targetRow.EnableSubject !== '1' && targetRow.EnableSubject !== 1) {
                                // 确保非全科课程行的科目字段为空
                                if (targetRow[field]) {
                                    targetRow[field] = ''
                                    recordFieldChange(targetRow.ID, field, '', 'auto_clear')
                                }
                                return // 跳过记录
                            }
                            // 记录当前字段的变更
                            recordFieldChange(targetRow.ID, field, sourceRowData[field], 'autofill')

                            // 如果字段有映射，同时记录映射字段
                            if (fieldMap[field] && fieldMap[field] !== field) {
                                const mappedValue = targetRow[fieldMap[field]]
                                if (mappedValue !== undefined) {
                                    recordFieldChange(targetRow.ID, fieldMap[field], mappedValue, 'autofill')
                                }
                            }
                        }
                    })
                }
            })
        }

        // 如果分组模式下进行了自动填充，可能需要重新计算分组
        if (isGrouped.value && groupedDataCache.value) {
            groupedDataCache.value.isValid = false
        }

        // 🆕 自动填充完成后，对涉及教师字段的行进行重复校验
        if (sourceSelectionData && Array.isArray(sourceSelectionData) && sourceSelectionData.length > 0) {
            const fieldsToCopy = Object.keys(sourceSelectionData[0]).filter(key => key !== 'ID')
            const hasTeacherFields = fieldsToCopy.some(field =>
                field === 'MainTeacherID' || field === 'MainTeacherName' ||
                field === 'AssistantTeacherID' || field === 'AssistantTeacherName'
            )

            if (hasTeacherFields && targetSelectionData && Array.isArray(targetSelectionData)) {
                const affectedRows = targetSelectionData
                    .map(targetRowData => tableData.value.find(row => row.ID === targetRowData.ID))
                    .filter(row => row && !row.isGroupRow && !row.isGroupFooter)
                batchValidateTeacherAssistant(affectedRows)
            }
        }

        // 🆕 特殊处理：填充"开放预约"为"否"时，清除可约人数和开课人数的校验错误（仅预约课排课）
        if (sourceSelectionData && Array.isArray(sourceSelectionData) && sourceSelectionData.length > 0 && selectedTableType.value === 30) {
            const fieldsToCopy = Object.keys(sourceSelectionData[0]).filter(key => key !== 'ID')
            const hasIsSubscribeCourse = fieldsToCopy.includes('IsSubscribeCourse')
            
            if (hasIsSubscribeCourse && targetSelectionData && Array.isArray(targetSelectionData)) {
                targetSelectionData.forEach(targetRowData => {
                    const targetRow = tableData.value.find(row => row.ID === targetRowData.ID)
                    if (targetRow && targetRow.IsSubscribeCourse === '0') {
                        // 清除可约人数和开课人数的校验错误
                        clearSubscribeCourseRelatedErrors(targetRow.ID)
                    }
                })
            }
        }
    }
})

// 虚拟滚动配置
const virtualScrollOption = reactive({
    // 是否开启
    enable: true,
    scrolling: scrolling,
    bufferScale: 1, // 缓冲区大小，数值越大，滚动时加载越多数据，性能越差
})

// 行样式配置
const rowStyleOption = reactive({
    clickHighlight: false,
    hoverHighlight: true, // 启用悬浮高亮

})

// 单元格选择配置11
const cellSelectionOption = reactive({
    enable: true,
})

// 加载配置
const loadingOption = reactive({
    target: '#loading-container',
    name: 'wave',
})

// 单元格样式配置
const cellStyleOption = reactive({
    bodyCellClass: ({ row, column, rowIndex }) => {
        const classes = []

        // 为操作列设置自定义样式
        if (column.operationColumn) {
            classes.push('operation-column-cell')
        }

        // 为校验错误的行添加黄色背景
        if (validationErrorRowIds.value.includes(row.ID) && !row.isGroupRow && !row.isGroupFooter) {
            classes.push('validation-error-cell')
        }

        // 为特定错误字段的单元格添加样式 - 使用校验错误字段映射
        const rowErrorFields = validationErrorFields.value.get(row.ID)
        if (rowErrorFields && rowErrorFields.includes(column.field) && !row.isGroupRow && !row.isGroupFooter) {
            classes.push('validation-error-field-cell')
        }

        // 预检查开启时，非法字段也标红边框
        if (preCheckEnabled.value && Array.isArray(row.errorField) && row.errorField.includes(column.field) && !row.isGroupRow && !row.isGroupFooter) {
            classes.push('precheck-invalid-field')
        }

        // 预检查字段标记
        if (preCheckEnabled.value && !row.isGroupRow && !row.isGroupFooter) {
            const preCheckData = preCheckResults.value.get(row.ID)
            // 字段别名匹配：支持 Name/ID 双字段与特殊聚合字段（如 MainTeacherList）
            const getFieldAliases = (field) => {
                const aliases = new Set([field])
                const mapped = fieldMap[field]
                if (mapped) aliases.add(mapped)
                for (const key in fieldMap) {
                    if (fieldMap[key] === field) aliases.add(key)
                }
                // 任课老师特殊：后端可能返回 MainTeacherList
                if (field === 'MainTeacherName' || field === 'MainTeacherID') {
                    aliases.add('MainTeacherList')
                }
                // 🆕 助教特殊：后端可能返回 AssistantTeacherList
                if (field === 'AssistantTeacherName' || field === 'AssistantTeacherID') {
                    aliases.add('AssistantTeacherList')
                }
                return Array.from(aliases)
            }
            const matchByAliases = (list = []) => {
                const aliases = getFieldAliases(column.field)
                return aliases.some(a => list.includes(a))
            }
            if (preCheckData) {
                // 错误字段标记
                if (preCheckData.ErrorFieldList && matchByAliases(preCheckData.ErrorFieldList)) {
                    classes.push('precheck-error-field')
                }

                // 限制字段标记
                if (preCheckData.CheckFieldList && preCheckData.CheckFieldList.some(
                    check => check.FieldNameList && matchByAliases(check.FieldNameList)
                )) {
                    classes.push('precheck-restriction-field')
                }

                // 冲突字段标记
                if (preCheckData.ConflictFieldList && preCheckData.ConflictFieldList.FieldNameList &&
                    matchByAliases(preCheckData.ConflictFieldList.FieldNameList)) {
                    classes.push('precheck-conflict-field')
                }
            }
        }

        return classes.join(' ')
    },
    headerCellClass: ({ column, rowIndex }) => {
        // 为操作列表头设置自定义样式
        if (column.operationColumn) {
            return 'operation-column-header'
        }
        // 为其他表头设置统一样式
        return 'table-header-cell'
    },
    footerCellClass: ({ row, column, rowIndex }) => {
        // 为footer行设置特殊样式
        return 'footer-row-cell'
    },
})

// 列宽调整配置
const columnWidthResizeOption = reactive({
    enable: true, // 启用列宽调整
    minWidth: 60, // 最小列宽
    maxWidth: 500, // 最大列宽
    handleWidth: 2, // 拖拽手柄宽度
    handleColor: '#e4e7ed', // 拖拽手柄颜色
    handleHoverColor: '#409eff', // 拖拽手柄悬浮颜色
    onBeforeResize: ({ column, newWidth }) => {
        return true // 返回 true 允许调整，false 阻止调整
    },
    onAfterResize: ({ column, oldWidth, newWidth }) => {
    }
})

// 计算实际显示的列数量（排除隐藏列）
const getVisibleColumnsCount = () => {
    const hiddenKeys = columnHiddenOption.value.defaultHiddenColumnKeys || []
    const visibleColumns = columns.value.filter(col => !hiddenKeys.includes(col.field))

    // 调试信息
    // console.log('📊 列显示计算:', {
    //     totalColumns: columns.value.length,
    //     hiddenKeys,
    //     visibleColumns: visibleColumns.length,
    //     preCheckEnabled: preCheckEnabled.value,
    //     preCheckColumnHidden: hiddenKeys.includes('preCheckStatus')
    // })

    return visibleColumns.length
}

// 单元格合并配置
const cellSpanOption = reactive({
    bodyCellSpan: ({ row, column, rowIndex }) => {
        // 分组行：第一列跨所有显示的列
        if (row.isGroupRow) {
            if (column.field === '') {
                return { rowspan: 1, colspan: getVisibleColumnsCount() }
            } else {
                return { rowspan: 0, colspan: 0 } // 隐藏其他列
            }
        }
        // 分组footer：第一列跨所有显示的列
        if (row.isGroupFooter) {
            if (column.field === '') {
                return { rowspan: 1, colspan: getVisibleColumnsCount() }
            } else {
                return { rowspan: 0, colspan: 0 } // 隐藏其他列
            }
        }

        // 普通行：正常显示
        return { rowspan: 1, colspan: 1 }
    }
})

// edit option 可控单元格编辑
const editOption = reactive({
    beforeStartCellEditing: ({ row, column, cellValue }) => {
        // 分组行和footer行不允许编辑
        if (row.isGroupRow || row.isGroupFooter) {
            return false
        }

        // 序号列不允许编辑
        if (column.field === '') {
            return false
        }

        // 备注列允许编辑（使用表格原生编辑）
        if (column.field === 'InternalRemark' || column.field === 'Describe') {
            return true
        }

        if (row.ID === 0 && column.field === 'ClassName') {
            // alert("You can't edit this cell.")
            return false
        }

        return true
    },
    beforeCellValueChange: ({ row, column, changeValue }) => {
        // 分组行和footer行不允许修改
        if (row.isGroupRow || row.isGroupFooter) {
            return false
        }
        if (column.field === 'InternalRemark' || column.field === 'Describe') {
            if (changeValue && changeValue.length > 3000) {
                ElMessage.warning('备注内容不能超过3000个字符');
                // 自动截断
                row[column.field] = changeValue.slice(0, 3000);
                // 阻止原始的超长值，但允许我们截断后的值
                return false;
            }
        }
        return true
    },
    afterCellValueChange: ({ row, column, changeValue }) => {
        // 如果修改了分组字段，需要重新计算分组
        if (column.field === groupByField.value && isGrouped.value) {
            if (groupedDataCache.value) {
                groupedDataCache.value.isValid = false
            }
        }

        // ==================== 🆕 编辑状态保护：备注字段特殊处理 ====================
        // InternalRemark 不使用 createEditableCell，所以需要在这里手动管理编辑状态
        if (column.field === 'InternalRemark') {
            const cellKey = `${row.ID}_${column.field}`
            editingCells.value.add(cellKey)
            editingCellTimestamps.value.set(cellKey, Date.now())
            // console.log(`📝 Start editing InternalRemark: ${cellKey}`)
            
            // 延迟清除编辑状态（给保存响应时间）
            setTimeout(() => {
                editingCells.value.delete(cellKey)
                editingCellTimestamps.value.delete(cellKey)
                // console.log(`✅ End editing InternalRemark: ${cellKey}`)
            }, 500)  // 备注字段使用更长的延迟
        }

        // 记录字段变更到草稿保存队列
        if (column.field && column.field !== '') {
            // console.log(`📝 editOption 触发变更记录: ${column.field} = ${changeValue}`)
            recordFieldChange(row.ID, column.field, changeValue, 'edit')
        }
    },
})
// ============班级排课
// 班级
const ClassColumn = {
    field: 'ClassID',
    key: 'ClassID',
    title: getFieldDisplayName('ClassID') || transToConfigDescript('上课班级'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(ClassSelect, {
        displayField: 'ClassName',
        getProps: (row, column) => ({
            campusId: row.CampusID,
            initialData: { ID: row.ClassID, Name: row.ClassName },
            defaultNoApiCall: true,
            placeholder: '',
        }),
        getEvents: (row, column) => ({
            onChange: (val, classData) => {
                if (val && classData) {
                    updateClassDataMap(val, classData)
                    row.ClassName = classData.Name
                    // 更新 EnableSubject 状态
                    row.EnableSubject = classData.EnableSubject
                    // 如果切换到非全科课程，清空科目数据
                    if (classData.EnableSubject !== '1' && classData.EnableSubject !== 1) {
                        if (row.SubjectID || row.SubjectName) {
                            // console.log(`清空行 ${row.ID} 的科目数据，因为切换到非全科课程班级`)
                            row.SubjectID = ''
                            row.SubjectName = ''
                            // 记录变更
                            recordFieldChange(row.ID, 'SubjectID', '', 'class_change')
                            recordFieldChange(row.ID, 'SubjectName', '', 'class_change')
                        }
                        //begin 这里是为了因为科目导致的错误消失
                        // 查找该行在 validationErrors 中的错误信息
                        const errorInfo = validationErrors.value.find(error => error.rowId === row.ID)
                        if (errorInfo && Array.isArray(errorInfo.errorFields)) {
                            // 只有当错误字段只包含 SubjectID 时才移除错误标记
                            if (errorInfo.errorFields.length === 1 && errorInfo.errorFields.includes('SubjectID')) {
                                const index = validationErrorRowIds.value.indexOf(row.ID)
                                if (index > -1) {
                                    validationErrorRowIds.value.splice(index, 1)
                                    // 同时从 validationErrors 中移除该条错误记录
                                    const errorIndex = validationErrors.value.findIndex(error => error.rowId === row.ID)
                                    if (errorIndex > -1) {
                                        validationErrors.value.splice(errorIndex, 1)
                                    }
                                }
                            }
                        }
                        // end
                    }

                    // 获取全局配置：是否开启一对多的班课关系
                    const IsOpen_OneClassMuitShift = configs.value.IsOpen_OneClassMuitShift == 1

                    if (!IsOpen_OneClassMuitShift) {
                        // 配置为0：使用班级对象的 ClassRelShift
                        const rel = classData.ClassRelShift
                        if (Array.isArray(rel) && rel.length > 0) {
                            // 唯一项自动选中
                            if (rel.length === 1) {
                                const only = rel[0]
                                row.ShiftID = only.ID
                                row.ShiftName = only.Name
                                recordFieldChange(row.ID, 'ShiftID', row.ShiftID, 'class_auto_fill')
                                recordFieldChange(row.ID, 'ShiftName', row.ShiftName, 'class_auto_fill')
                            }
                        }
                    } else {
                        // 配置为1：使用 ClassCourseContent
                        const cc = classData.ClassCourseContent
                        if (Array.isArray(cc) && cc.length > 0) {
                            // 唯一项自动选中
                            if (cc.length === 1) {
                                const only = cc[0]
                                row.ShiftID = only.ShiftID
                                row.ShiftName = only.ShiftName
                                recordFieldChange(row.ID, 'ShiftID', row.ShiftID, 'class_auto_fill')
                                recordFieldChange(row.ID, 'ShiftName', row.ShiftName, 'class_auto_fill')
                            }
                        }
                    }
                } else {
                    // 清空班级时不清空科目
                    row.ClassName = ''
                    row.ShiftID = ''
                    row.ShiftName = ''
                    row.EnableSubject = ''
                    recordFieldChange(row.ID, 'ShiftID', '', 'class_auto_fill')
                    recordFieldChange(row.ID, 'ShiftName', '', 'class_auto_fill')
                }
            },
            onExitEdit: () => {
                cellDbClickKey.value = null
            },
            // 🆕 UI交互锁：弹框打开时锁定
            onDialogOpen: () => {
                uiInteractionLock.value = true
                // console.log('🔒 班级选择弹框打开，UI锁定')
                
                lockTimeout.value = setTimeout(() => {
                    if (uiInteractionLock.value) {
                        console.warn('⚠️ UI锁超时（30秒），强制释放')
                        uiInteractionLock.value = false
                        processPendingSaveResponses()
                    }
                }, 30000)
            },
            // 🆕 UI交互锁：弹框关闭时解锁
            onDialogClose: () => {
                if (lockTimeout.value) {
                    clearTimeout(lockTimeout.value)
                    lockTimeout.value = null
                }
                
                uiInteractionLock.value = false
                // console.log('🔓 班级选择弹框关闭，UI解锁')
                processPendingSaveResponses()
            }
        }),
        onUpdateModelValue: (row, column, val) => {
            classId.value = val
        }
    })
}
// ============给学员排课
// 给学员排课
const StudentUserColumn = {
    field: 'StudentUserID',
    key: 'StudentUserID',
    title: getFieldDisplayName('StudentUserID') || transToConfigDescript('上课学员'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(StudentSelect, {
        displayField: 'StudentUserName',
        getProps: (row, column) => ({
            campusId: row.CampusID,
            initialData: { ID: row.StudentUserID, Name: row.StudentUserName },
            defaultNoApiCall: true,
            showAllStudentsWhenCoursePlan: showAllStudentsWhenCoursePlan.value,
            placeholder: '',
        }),
        getEvents: (row, column) => ({
            onChange: (val, studentUserData) => {
                if (val && studentUserData) {
                    updateStudentUserDataMap(val, studentUserData)
                    row.StudentUserName = studentUserData.Name
                } else {
                    row.StudentUserName = ''
                }
            },
            onExitEdit: () => {
                cellDbClickKey.value = null
            },
            // 🆕 弹框打开时锁定 UI
            onDialogOpen: () => {
                uiInteractionLock.value = true
                // console.log('🔒 [Parent] 学员选择弹框打开，UI锁定，当前锁状态:', uiInteractionLock.value)
                
                // 🛡️ 超时保护：30秒后强制释放锁（防止用户按ESC、点击遮罩等情况导致锁泄漏）
                lockTimeout.value = setTimeout(() => {
                    if (uiInteractionLock.value) {
                        console.warn('⚠️ UI锁超时（30秒），强制释放并处理待处理的保存响应')
                        uiInteractionLock.value = false
                        processPendingSaveResponses()
                    }
                }, 30000)
            },
            // 🆕 弹框关闭时解锁 UI 并处理待处理的保存响应
            onDialogClose: () => {
                // console.log('🔓 [Parent] 收到 dialogClose 事件，准备解锁')
                
                // 清除超时定时器
                if (lockTimeout.value) {
                    clearTimeout(lockTimeout.value)
                    lockTimeout.value = null
                    // console.log('⏰ 清除超时定时器')
                }
                
                uiInteractionLock.value = false
                // console.log('🔓 [Parent] 学员选择弹框关闭，UI解锁，当前队列长度:', pendingSaveResponses.value.length)
                processPendingSaveResponses()
            }
        }),
        onUpdateModelValue: (row, column, val) => {
            studentUserId.value = val
        }
    })
}
// 上课课程-学员排课专用
const StudentShiftColumn = {
    field: 'ShiftID',
    key: 'ShiftID',
    title: getFieldDisplayName('ShiftID') || transToConfigDescript('上课课程'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(StudentCourseSelect, {
        displayField: 'ShiftName',
        getProps: (row, column) => ({
            studentId: row.StudentUserID,
            studentName: row.StudentUserName,
            campusId: row.CampusID,
            initialData: { ID: row.ShiftID, Name: row.ShiftName },
        }),
        getEvents: (row, column) => ({
            onChange: (val, courseData) => {
                if (val && courseData) {
                    updateCourseDataMap(val, courseData)
                    row.ShiftName = courseData.ShiftName || courseData.Name
                } else {
                    row.ShiftName = ''
                }
            },
            // 🆕 UI交互锁：弹框打开时锁定
            onDialogOpen: () => {
                uiInteractionLock.value = true
                // console.log('🔒 学员课程选择弹框打开，UI锁定')
                
                lockTimeout.value = setTimeout(() => {
                    if (uiInteractionLock.value) {
                        console.warn('⚠️ UI锁超时（30秒），强制释放')
                        uiInteractionLock.value = false
                        processPendingSaveResponses()
                    }
                }, 30000)
            },
            // 🆕 UI交互锁：弹框关闭时解锁
            onDialogClose: () => {
                if (lockTimeout.value) {
                    clearTimeout(lockTimeout.value)
                    lockTimeout.value = null
                }
                
                uiInteractionLock.value = false
                // console.log('🔓 学员课程选择弹框关闭，UI解锁')
                processPendingSaveResponses()
            }
        })
    })
}
// ============预约排课

// 上课课程-预约排课专用
const SubscribeShiftColumn = {
    field: 'ShiftID',
    key: 'ShiftID',
    title: getFieldDisplayName('ShiftID') || transToConfigDescript('上课课程'),
    width: 200,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(SubscribeCourseSelect, {
        displayField: 'ShiftName',
        getProps: (row, column) => ({
            campusId: row.CampusID,
            class: 'w-100%!',
            initialData: { ID: row.ShiftID, Name: row.ShiftName },
        }),
        getEvents: (row, column) => ({
            onChange: (val, courseData) => {
                if (val && courseData) {
                    updateCourseDataMap(val, courseData)
                    row.ShiftName = courseData.ShiftName || courseData.Name
                    // 联动是否一对一、可约人数、开课人数，并记录变更
                    const newIsOneToOne = courseData.IsOneToOne || 0
                    if (row.IsOneToOne != newIsOneToOne) {
                        row.IsOneToOne = newIsOneToOne
                        // recordFieldChange(row.ID, 'IsOneToOne', newIsOneToOne)
                    }

                    if (row.IsOneToOne == 1) {
                        if (row.MaxStudentCount != 1) {
                            row.MaxStudentCount = '1'
                            recordFieldChange(row.ID, 'MaxStudentCount', '1')
                        }
                        if (row.StartStudentCount != 1) {
                            row.StartStudentCount = '1'
                            recordFieldChange(row.ID, 'StartStudentCount', '1')
                        }
                    } else {
                        if (row.MaxStudentCount != '') {
                            row.MaxStudentCount = ''
                            recordFieldChange(row.ID, 'MaxStudentCount', '')
                        }
                        if (row.StartStudentCount != '') {
                            row.StartStudentCount = ''
                            recordFieldChange(row.ID, 'StartStudentCount', '')
                        }
                    }
                } else {
                    row.ShiftName = ''
                }
            },
        })
    }),
}

// 可约人数
const MaxStudentCountColumn = {
    field: 'MaxStudentCount',
    key: 'MaxStudentCount',
    title: '可约人数',
    width: 100,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
    renderBodyCell: createEditableCell(ElInput, {
        getDisplayValue: (row, column) => {
            const value = row[column.field]
            return (value === '0' || value === 0) ? '' : value
        },
        getProps: (row, column) => ({
            class: 'w-100%!',
            disabled: row.IsOneToOne == 1,
            placeholder: row.IsOneToOne == 1 ? transToConfigDescript('一对一课程') : '请输入数字',
            // � 手动处理删除键
            onKeydown: (e) => {
                
                // 手动处理 Backspace 和 Delete 键
                if (e.key === 'Backspace' || e.keyCode === 8) {
                    // Backspace：删除光标前的字符
                    const input = e.target
                    const start = input.selectionStart
                    const end = input.selectionEnd
                    const value = input.value
                    
                    if (start === end && start > 0) {
                        // 没有选中文本，删除光标前一个字符
                        const newValue = value.slice(0, start - 1) + value.slice(start)
                        input.value = newValue
                        row.MaxStudentCount = newValue
                        // 手动触发 input 事件
                        input.dispatchEvent(new Event('input', { bubbles: true }))
                        e.preventDefault()
                    } else if (start !== end) {
                        // 有选中文本，删除选中部分
                        const newValue = value.slice(0, start) + value.slice(end)
                        input.value = newValue
                        row.MaxStudentCount = newValue
                        input.dispatchEvent(new Event('input', { bubbles: true }))
                        e.preventDefault()
                    }
                }
            },
            onInput: (e) => {
             // console.log('🔍 input事件:', e.target?.value || e)
                // 过滤非数字字符
                const value = e.target?.value || e
                const filtered = String(value).replace(/[^\d]/g, '')
                if (filtered !== value && e.target) {
                    e.target.value = filtered
                }
                
                // 数字超限检查
                if (filtered && parseInt(filtered, 10) > 999) {
                    ElMessage.warning('最大可约人数不能超过999')
                    row.MaxStudentCount = '999'
                    if (e.target) e.target.value = '999'
                } else {
                    row.MaxStudentCount = filtered
                }
                
                // ❌ 移除：不在输入时立即记录
                // recordFieldChange(row.ID, 'MaxStudentCount', row.MaxStudentCount, 'edit')
                
                // 清除验证错误
                if (row._showValidationError && row._showValidationError['MaxStudentCount']) {
                    row._showValidationError['MaxStudentCount'] = false
                    checkAndUpdatePaperClipIcon(row.ID)
                }
            }
        }),
        getEvents: (row) => ({
            // ⚠️ 覆盖baseEvents的onUpdate:modelValue，阻止每次输入都调用cellDataChange
            'onUpdate:modelValue': (value) => {
                // 只更新row数据，不调用recordFieldChange（由onBlur处理）
                row.MaxStudentCount = value
            },
            onBlur: (e) => {
                // ✅ 失焦时记录变更
                recordFieldChange(row.ID, 'MaxStudentCount', row.MaxStudentCount, 'edit')
                
                // 处理开课人数联动
                const startCount = parseInt(row.StartStudentCount, 10)
                const maxCount = parseInt(row.MaxStudentCount, 10)
                if (row.StartStudentCount && !isNaN(startCount) && !isNaN(maxCount) && startCount > maxCount) {
                    ElMessage.warning('开课人数已自动调整为不大于可约人数')
                    nextTick(() => {
                        row.StartStudentCount = row.MaxStudentCount
                        recordFieldChange(row.ID, 'StartStudentCount', row.MaxStudentCount, 'edit')
                    })
                }
            }
        })
    }),
}

// 开课人数
const StartStudentCountColumn = {
    field: 'StartStudentCount',
    key: 'StartStudentCount',
    title: '开课人数',
    width: 100,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
    renderBodyCell: createEditableCell(ElInput, {
        getDisplayValue: (row, column) => {
            const value = row[column.field]
            return (value === '0' || value === 0) ? '' : value
        },
        getProps: (row, column) => ({
            class: 'w-100%!',
            disabled: row.IsOneToOne == 1,
            placeholder: row.IsOneToOne == 1 ? transToConfigDescript('一对一课程') : '请输入数字',
            onKeydown: (e) => {
             // console.log('🔍 [开课人数] 键盘按下:', e.key)
                // 手动处理 Backspace 删除
                if (e.key === 'Backspace' || e.keyCode === 8) {
                    const input = e.target
                    const start = input.selectionStart
                    const end = input.selectionEnd
                    const value = input.value

                    if (start === end && start > 0) {
                        // 单字符删除
                        const newValue = value.slice(0, start - 1) + value.slice(start)
                        input.value = newValue
                        row.StartStudentCount = newValue
                        const cursorPos = start - 1
                        input.setSelectionRange(cursorPos, cursorPos)
                        // 手动触发 input 事件
                        input.dispatchEvent(new Event('input', { bubbles: true }))
                        e.preventDefault()
                    } else if (start !== end) {
                        // 选中内容删除
                        const newValue = value.slice(0, start) + value.slice(end)
                        input.value = newValue
                        row.StartStudentCount = newValue
                        input.setSelectionRange(start, start)
                        input.dispatchEvent(new Event('input', { bubbles: true }))
                        e.preventDefault()
                    }
                }
                // 手动处理 Delete 键
                else if (e.key === 'Delete' || e.keyCode === 46) {
                    const input = e.target
                    const start = input.selectionStart
                    const end = input.selectionEnd
                    const value = input.value

                    if (start === end && start < value.length) {
                        const newValue = value.slice(0, start) + value.slice(start + 1)
                        input.value = newValue
                        row.StartStudentCount = newValue
                        input.setSelectionRange(start, start)
                        input.dispatchEvent(new Event('input', { bubbles: true }))
                        e.preventDefault()
                    } else if (start !== end) {
                        const newValue = value.slice(0, start) + value.slice(end)
                        input.value = newValue
                        row.StartStudentCount = newValue
                        input.setSelectionRange(start, start)
                        input.dispatchEvent(new Event('input', { bubbles: true }))
                        e.preventDefault()
                    }
                }
            },
            onInput: (e) => {
             // console.log('🔍 [开课人数] input事件:', e.target?.value || e)
                // 过滤非数字字符
                const value = e.target?.value || e
                const filtered = String(value).replace(/[^\d]/g, '')
                if (filtered !== value && e.target) {
                    e.target.value = filtered
                }
                
                // 数字超限检查
                if (filtered && parseInt(filtered, 10) > 999) {
                    ElMessage.warning('最大开课人数不能超过999')
                    row.StartStudentCount = '999'
                    if (e.target) e.target.value = '999'
                } else {
                    row.StartStudentCount = filtered
                }
                
                // 检查是否大于可约人数
                const startCount = parseInt(filtered, 10)
                const maxCount = parseInt(row.MaxStudentCount, 10)
                if (row.MaxStudentCount && !isNaN(startCount) && !isNaN(maxCount) && startCount > maxCount) {
                    ElMessage.warning('开课人数不能大于可约人数')
                    row.StartStudentCount = String(row.MaxStudentCount)
                    if (e.target) e.target.value = String(row.MaxStudentCount)
                }
                
                // ❌ 移除：不在输入时立即记录
                // recordFieldChange(row.ID, 'StartStudentCount', row.StartStudentCount, 'edit')
                
                // 清除验证错误
                if (row._showValidationError && row._showValidationError['StartStudentCount']) {
                    row._showValidationError['StartStudentCount'] = false
                    checkAndUpdatePaperClipIcon(row.ID)
                }
            }
        }),
        getEvents: (row) => ({
            // ⚠️ 覆盖baseEvents的onUpdate:modelValue，阻止每次输入都调用cellDataChange
            'onUpdate:modelValue': (value) => {
                // 只更新row数据，不调用recordFieldChange（由onBlur处理）
                row.StartStudentCount = value
            },
            onBlur: (e) => {
                // ✅ 失焦时记录变更
                recordFieldChange(row.ID, 'StartStudentCount', row.StartStudentCount, 'edit')
            }
        })
    }),
}

// ============正常字段
// 序号
const indexColumn = {
    field: '',
    key: 'index',
    title: '序号',
    width: 40,
    align: 'center',
    fixed: 'left',
    ellipsis: {
        showTitle: false,
        lineClamp: 1,
    },
    renderHeaderCell: ({ column }, _h) => {
        // 渲染表头复选框（全选功能）
        return h('div', {
            style: {
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100%'
            }
        }, [
            h(ElCheckbox, {
                modelValue: isAllChecked.value,
                indeterminate: isIndeterminate.value,
                onChange: (checked) => {
                    if (checked) {
                        checkAllRows()
                    } else {
                        clearCheckedRows()
                    }
                }
            })
        ])
    },
    renderBodyCell: ({ row, column, rowIndex }, _h) => {
        // 分组行渲染
        if (row.isGroupRow) {
            const isExpanded = expandedGroups.value.has(row.groupKey)
            return h('div', {
                class: 'group-header-cell',
                onClick: () => toggleGroupExpand(row.groupKey)
            }, [
                h('span', {
                    class: 'group-toggle-icon',
                }, [
                    h('svg', { 'aria-hidden': 'true' }, [
                        h('use', { 'xlink:href': isExpanded ? '#w2-zhankai' : '#w2-shouqi' })
                    ])
                ]),
                h('span', {
                    class: 'group-name',
                }, `${row.groupKey === '未分组' ? '未分组' : row.groupName}`),
                h('span', {
                    class: 'group-count',
                }, `${row.groupCount} 条数据`)
            ])
        }

        // 分组footer渲染
        if (row.isGroupFooter) {
            return h('div', {
                class: 'group-footer-cell',
                style: {
                    backgroundColor: '#fafafa',
                    padding: '8px 16px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'flex-start',
                    width: '100%',
                    height: '100%'
                }
            }, [
                h(ElPopover, {
                    ref: addMenuPopoverRef,
                    placement: 'right',
                    width: 120,
                    trigger: 'click',
                    popperClass: 'custom-menu-popover'
                }, {
                    reference: () => h('div', {
                        class: 'add-menu-icon-wrapper',
                    }, [
                        h('el-icon', {}, [
                            h('svg', { 'aria-hidden': 'true', }, [
                                h('use', { 'xlink:href': '#w2-xinzenghang' })
                            ])
                        ])
                    ]),
                    default: () => h('div', {
                        class: 'add-menu-content'
                    }, [
                        h('div', {
                            class: 'add-menu-item',
                            onClick: () => {
                                const remaining = getRemainingCapacity()
                                if (remaining <= 0) {
                                    ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，已达上限`)
                                    return
                                }
                                if (remaining < 10) {
                                    ElMessage.warning(`本次拟新增 10 行，超出 ${10 - remaining} 行，已仅新增 ${remaining} 行`)
                                }
                                handleAddTenRows(row.groupKey, {
                                    groupByField,
                                    recordNewRow,
                                    getCampusName,
                                    classDataMap,
                                    courseDataMap,
                                    getSubjectName,
                                    assistantDataMap,
                                    classroomDataMap,
                                    teacherDataMap,
                                    expandedGroups,
                                    isGrouped,
                                    groupedDataCache,
                                    capacityLimit: {
                                        max: MAX_TABLE_ROWS,
                                        remaining: getRemainingCapacity(),
                                    },
                                    studentUserDataMap
                                })
                                // 隐藏弹出框
                                if (addMenuPopoverRef.value) {
                                    addMenuPopoverRef.value.hide()
                                }
                            }
                        }, [
                            h('span', '新增10行'),
                        ]),
                        h('div', {
                            class: 'add-menu-item',
                            onClick: () => {
                                handleBatchAddByRules()
                                // 隐藏弹出框
                                if (addMenuPopoverRef.value) {
                                    addMenuPopoverRef.value.hide()
                                }
                            }
                        }, [
                            h('span', '按规则批量新增'),
                        ])
                    ])
                })
            ])
        }

        // 普通数据行：显示序号或勾选框
        const isHovered = hoveredRowKey.value === row.ID
        const isChecked = checkedRows.value.has(row.ID)

        // 如果鼠标悬浮或已勾选，只显示勾选框
        if (isHovered || isChecked) {
            return h(ElCheckbox, {
                modelValue: isChecked,
                onChange: (checked) => {
                    if (checked) {
                        checkedRows.value.add(row.ID)
                    } else {
                        checkedRows.value.delete(row.ID)
                    }
                }
            })
        }

        // 否则只显示序号
        return h('span', {
            style: {
                fontSize: '12px',
                color: '#909399',
                textAlign: 'center'
            }
        }, isGrouped.value ?
            (() => {
                // 分组模式下，每个分组内的序号从1开始
                const groupKey = row[groupByField.value]

                // 找到当前分组内的所有数据行（包括未分组的情况）
                const groupRows = tableData.value.filter(item => {
                    const itemGroupValue = item[groupByField.value]
                    let itemGroupKey

                    // 对于特定字段，需要特殊处理
                    if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
                        itemGroupKey = (itemGroupValue === null || itemGroupValue === undefined) ? '未分组' : String(itemGroupValue)
                    } else {
                        itemGroupKey = itemGroupValue || '未分组'
                    }

                    // 确保与分组逻辑一致
                    const currentGroupValue = row[groupByField.value]
                    let currentGroupKey

                    if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
                        currentGroupKey = (currentGroupValue === null || currentGroupValue === undefined) ? '未分组' : String(currentGroupValue)
                    } else {
                        currentGroupKey = currentGroupValue || '未分组'
                    }

                    return itemGroupKey === currentGroupKey && !item.isGroupRow && !item.isGroupFooter
                })

                // 计算当前行在分组内的序号
                const groupIndex = groupRows.findIndex(item => item.ID === row.ID)
                return groupIndex >= 0 ? groupIndex + 1 : 1
            })() :
            (++rowIndex + startRowIndex.value)
        )
    },
    // 添加数据
    renderFooterCell: ({ row, column, rowIndex }, _h) => {
        // 分组时不显示底部添加行
        if (isGrouped.value) {
            return null
        }
        // 在footer的第一列渲染圆形加号按钮（与分组模式保持一致）
        return h('div', {
            style: {
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: '100%',
                height: '100%'
            }
        }, [
            h(ElPopover, {
                ref: addMenuPopoverFooterRef,
                placement: 'right',
                width: 120,
                trigger: 'click',
                popperClass: 'custom-menu-popover'
            }, {
                reference: () => h('div', {
                    class: 'add-menu-icon-wrapper',
                }, [
                    h('el-icon', {}, [
                        h('svg', { 'aria-hidden': 'true', }, [
                            h('use', {
                                'xlink:href': '#w2-xinzenghang'
                            })
                        ])
                    ])
                ]),
                default: () => h('div', {
                    class: 'add-menu-content'
                }, [
                    h('div', {
                        class: 'add-menu-item',
                        onClick: () => {
                            const remaining = getRemainingCapacity()
                            if (remaining <= 0) {
                                ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，已达上限`)
                                return
                            }
                            if (remaining < 10) {
                                ElMessage.warning(`本次拟新增 10 行，超出 ${10 - remaining} 行，已仅新增 ${remaining} 行`)
                            }
                            handleAddTenRows(null, {
                                groupByField,
                                recordNewRow,
                                getCampusName,
                                classDataMap,
                                courseDataMap,
                                getSubjectName,
                                assistantDataMap,
                                classroomDataMap,
                                teacherDataMap,
                                expandedGroups,
                                isGrouped,
                                groupedDataCache,
                                capacityLimit: {
                                    max: MAX_TABLE_ROWS,
                                    remaining: getRemainingCapacity(),
                                },
                                studentUserDataMap
                            })
                            // 隐藏弹出框
                            if (addMenuPopoverFooterRef.value) {
                                addMenuPopoverFooterRef.value.hide()
                            }
                        }
                    }, [
                        h('span', '新增10行'),
                    ]),
                    h('div', {
                        class: 'add-menu-item',
                        onClick: () => {
                            handleBatchAddByRules()
                            // 隐藏弹出框
                            if (addMenuPopoverFooterRef.value) {
                                addMenuPopoverFooterRef.value.hide()
                            }
                        }
                    }, [
                        h('span', '按规则批量新增'),

                    ])
                ])
            })
        ])
    }
};
// 校区
const CampusIDColumn = {
    field: 'CampusID',
    key: 'CampusID',
    title: transToConfigDescript(getFieldDisplayName('CampusID')) || transToConfigDescript('上课校区'),
    width: 130,
    sortBy: '',
    align: 'left',
    ellipsis: {
        showTitle: false,
        lineClamp: 1,
    },
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(CampusSelect, {
        displayField: 'CampusName',
        getProps: (row, column) => ({
            title: row.CampusName,
            placeholder: '',
            initialData: { ID: row.CampusID, Name: getCampusName(row.CampusID) }
        }),
        getEvents: (row, column) => ({
            onChange: (val, campusData) => {
                if (val && campusData) {
                    row.CampusName = campusData.Name
                } else {
                    row.CampusName = ''
                }
            },
            onGoToSettings: () => {
             // console.log('前往校区设置')
            }
        }),
        onUpdateModelValue: (row, column, val) => {
            campusId.value = val
        }
    })
}
// 课程
const ShiftColumn = {
    field: 'ShiftID',
    key: 'ShiftID',
    title: getFieldDisplayName('ShiftID') || transToConfigDescript('上课课程'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(CourseSelect, {
        displayField: 'ShiftName',
        getProps: (row, column) => {
            return {
                classId: row.ClassID,
                initialData: { ID: row.ShiftID, Name: row.ShiftName },
                ClassData: classDataMap.value.get(row.ClassID) || null
            }
        },
        getEvents: (row, column) => ({
            onChange: (val, courseData) => {
                if (val && courseData) {
                    updateCourseDataMap(val, courseData)
                    row.ShiftName = courseData.Name
                } else {
                    row.ShiftName = ''
                }
            }
        })
    })
}
// 科目
const SubjectColumn = {
    field: 'SubjectID',
    key: 'SubjectID',
    title: getFieldDisplayName('SubjectID') || transToConfigDescript('上课科目'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(SubjectSelect, {
        displayField: 'SubjectName',
        getProps: (row, column) => {
            return {
                classId: row.ClassID,
                initialData: { ID: row.SubjectID, Name: getSubjectName(row.SubjectID) },
                classData: row.ClassID ? { ...classDataMap.value.get(row.ClassID), EnableSubject: row.EnableSubject } : null,
                EnableSubject: row.EnableSubject,
                courseIsClassSubject: courseIsClassSubject.value,
                selectedTableType: selectedTableType.value,
                ShiftID: row.ShiftID,
            }
        },
        getEvents: (row, column) => ({
            onChange: (val, subjectData) => {
                if (val && subjectData) {
                    row.SubjectName = subjectData.Name
                } else {
                    row.SubjectName = ''
                }
            }
        }),
        getDisplayValue: (row, column) => {
            // 如果是非全科课程，显示"无需填写"
            if (row.ShiftID && row.EnableSubject !== '1' && row.EnableSubject !== 1) {
                row.SubjectName = '无需填写'
                row.SubjectID = ''
                return '无需填写'
            }
            return row.SubjectName || getSubjectName(row.SubjectID) || ''
        }
    })
}
// 上课日期
const DateColumn = {
    field: 'Date',
    key: 'Date',
    title: getFieldDisplayName('Date') || transToConfigDescript('上课日期'),
    width: 150,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(ElDatePicker, {
        getProps: (row, column) => ({
            size: 'default',
            type: 'date',
            clearable: true,
            format: "YYYY-MM-DD",
            'value-format': 'YYYY-MM-DD',
            // 🔧 修复：使用当前行的日期作为 default-value，如果没有则用当前日期
            // 这样日历面板会定位到原有日期的年月，而不是总是跳到当前年月
            "default-value": row.Date ? new Date(row.Date) : new Date(),
            name: "ignore-autofill",
        }),
        getDisplayValue: (row, column) => {
            const getWeekDay = (dateStr) => {
                if (!dateStr) return '';
                const date = new Date(dateStr);
                const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
                return weekDays[date.getDay()];
            };
            const formatDateDisplay = (dateStr) => {
                if (!dateStr) return '';
                const weekDay = getWeekDay(dateStr);
                return `${dateStr}(${weekDay})`;
            };
            return formatDateDisplay(row.Date);
        }
    })
}
// 上课时间
const timeColumn = {
    field: 'timeRange',
    key: 'timeRange',
    title: transToConfigDescript('上课时间'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, true),
    renderBodyCell: createEditableCell(TimeSelect, {
        getProps: (row, column) => ({
            campusId: row.CampusID,
            // 使用 timeRange 字段，如果没有则从 StartTime 和 EndTime 组合
            modelValue: row.timeRange || (row.StartTime && row.EndTime ? `${row.StartTime}~${row.EndTime}` : '')
        }),
        getRef: (row, column) => (el) => {
            if (el) {
                componentRefs.value.set(`${row.ID}_${column.field}`, el)
            }
        },
        onUpdateModelValue: (row, column, val) => {
            // 更新 timeRange 字段
            row.timeRange = val

            // 同时更新 StartTime 和 EndTime 字段（用于兼容）
            if (val && val.includes('~')) {
                const [start, end] = val.split('~')
                row.StartTime = start || ''
                row.EndTime = end || ''
            } else {
                row.StartTime = val || ''
                row.EndTime = ''
            }

            // 记录变更到草稿保存队列
            recordFieldChange(row.ID, 'StartTime', row.StartTime, 'edit')
            recordFieldChange(row.ID, 'EndTime', row.EndTime, 'edit')
        },
        getEvents: (row, column) => ({
            onCustomTime: () => {
             // console.log('自定义时间')
            }
        }),
        getDisplayValue: (row, column) => {
            // 优先显示 timeRange，如果没有则组合 StartTime 和 EndTime
            return row.timeRange || (row.StartTime && row.EndTime ? `${row.StartTime}~${row.EndTime}` : '')
        }
    })
}
// 上课教室
const ClassRoomColumn = {
    field: 'ClassRoomID',
    key: 'ClassRoomID',
    title: getFieldDisplayName('ClassRoomID') || transToConfigDescript('上课教室'),
    width: 145,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => {
        const hasOfflineCourse = tableData.value.some(r => !r.isGroupRow && !r.isGroupFooter && r.CourseType != '2')
        return renderHeaderWithStar(column.title, hasOfflineCourse, 6, '如果是线上课，非必填！')
    },
    renderBodyCell: createEditableCell(ClassroomSelect, {
        displayField: 'ClassRoomName',
        getDisplayValue: (row, column) => {
            return row.ClassRoomName || row.ClassRoomID || ''
        },
        getProps: (row, column) => {
            // 优先从缓存获取教室数据
            const classroomId = row.ClassRoomID || ''
            let initialData = null

            if (classroomId) {
                const cachedClassroom = classroomDataMap.value.get(classroomId)
                if (cachedClassroom) {
                    initialData = {
                        ID: cachedClassroom.ID,
                        Name: cachedClassroom.Name
                    }
                } else {
                    // 备用方案：使用行数据
                    initialData = {
                        ID: classroomId,
                        Name: row.ClassRoomName || classroomId
                    }
                }
            }
            let startTime = ''
            let endTime = ''
            if (row.Date && row.StartTime && row.EndTime) {
                startTime = row.Date + 'T' + row.StartTime
                endTime = row.Date + 'T' + row.EndTime
            }
            return {
                campusId: row.CampusID,
                startTime: startTime,
                endTime: endTime,
                courseType: row.CourseType,
                initialData,
                lazy: true
            }
        },
        getEvents: (row, column) => ({
            onChange: (val, classroomData) => {
                if (val && classroomData) {
                    updateClassroomDataMap(val, classroomData)
                    row.ClassRoomName = classroomData.Name
                } else {
                    row.ClassRoomName = ''
                }
            },
            onGoToSettings: () => {
             // console.log('前往教室设置')
            }
            // 🆕 注意：不需要在这里添加 onVisible-change
            // classroom-select 内部会 emit('visible-change')
            // createEditableCell 的 editStateEvents 会自动添加 UI 锁逻辑
            // 两者会通过合并逻辑正确组合
        }),
        onUpdateModelValue: (row, column, val) => {
            // 确保教室数据在缓存中
            if (val && !classroomDataMap.value.has(val)) {
                // 如果缓存中没有数据，但有名称，则构建缓存数据
                if (row.ClassRoomName) {
                    updateClassroomDataMap(val, {
                        ID: val,
                        Name: row.ClassRoomName
                    })
                }
            }
        }
    })
}
// 任课老师
const MainTeacherColumn = {
    field: 'MainTeacherID',
    key: 'MainTeacherID',
    title: getFieldDisplayName('MainTeacherID') || transToConfigDescript('任课老师'),
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, courseTeacherRequired.value),
    renderBodyCell: createEditableCell(TeacherSelect, {
        getProps: (row, column) => {
            const teacherId = row.MainTeacherID || ''
            let startTime = ''
            let endTime = ''
            if (row.Date && row.StartTime && row.EndTime) {
                startTime = row.Date + 'T' + row.StartTime
                endTime = row.Date + 'T' + row.EndTime
            }

            // 🆕 获取当前行的助教ID列表，作为任课老师的阻止列表
            const assistantIds = row.AssistantTeacherID
                ? row.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean)
                : []

            return {
                title: row.MainTeacherName || '',
                modelValue: teacherId,
                initialData: teacherId ? { id: teacherId, name: row.MainTeacherName || '' } : null,
                startTime: startTime,
                endTime: endTime,
                ShiftID: row.ShiftID,
                SubjectIDList: row.SubjectID ? [row.SubjectID] : [],
                EnableSubject: row.EnableSubject=='1',
                disabledIds: assistantIds,
                Check_Shift_Teacher_Subject:Check_Shift_Teacher_Subject.value,
            }
        },
        getEvents: (row, column) => ({
            onChange: (val, teacherData) => {
                if (val && teacherData) {
                    updateTeacherDataMap(val, {
                        ID: val,
                        Name: teacherData.name || '',
                    })
                    row.MainTeacherName = teacherData.name || ''
                    row.MainTeacherCommissionList = teacherData.TeacherCommissionList || []
                } else {
                    row.MainTeacherName = ''
                    row.MainTeacherCommissionList = []
                }
                // 🆕 触发任课老师与助教重复校验
                const validationResult = validateTeacherAssistantDuplicate(row)
                applyValidationResult(row, validationResult)
            },
            onGoToSettings: () => {
             // console.log('前往教师设置')
            },
            // 🆕 UI交互锁：弹框打开时锁定
            onDialogOpen: () => {
                uiInteractionLock.value = true
             // console.log('🔒 任课老师弹框打开，UI锁定')
                
                lockTimeout.value = setTimeout(() => {
                    if (uiInteractionLock.value) {
                        console.warn('⚠️ UI锁超时（30秒），强制释放')
                        uiInteractionLock.value = false
                        processPendingSaveResponses()
                    }
                }, 30000)
            },
            // 🆕 UI交互锁：弹框关闭时解锁
            onDialogClose: () => {
                if (lockTimeout.value) {
                    clearTimeout(lockTimeout.value)
                    lockTimeout.value = null
                }
                
                uiInteractionLock.value = false
             // console.log('🔓 任课老师弹框关闭，UI解锁')
                processPendingSaveResponses()
            }
        }),
        onUpdateModelValue: (row, column, val) => {
            // 更新操作字段
            row.MainTeacherID = val || ''
            // 同步更新传输字段
            if (val) {
                row.MainTeacherList = [{ ID: val, Name: row.MainTeacherName || '' }]
            } else {
                row.MainTeacherList = []
            }

            // 🆕 在数据更新后触发校验（确保 MainTeacherID 已更新）
            nextTick(() => {
                const validationResult = validateTeacherAssistantDuplicate(row)
                applyValidationResult(row, validationResult)
            })
        },
        getDisplayValue: (row, column) => {
            return row.MainTeacherName || row.MainTeacherID || ''
        }
    })
}
// 助教
const AssistantTeacherColumn = {
    field: 'AssistantTeacherID',
    key: 'AssistantTeacherID',
    title: getFieldDisplayName('AssistantTeacherID') || '助教',
    width: 115,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
    renderBodyCell: createEditableCell(AssistantSelect, {
        getProps: (row, column) => {
            let assistantIds = []
            let initialData = []
              let startTime = ''
            let endTime = ''
            if (row.Date && row.StartTime && row.EndTime) {
                startTime = row.Date + 'T' + row.StartTime
                endTime = row.Date + 'T' + row.EndTime
            }
            // 优先使用 AssistantTeacherList 构建 initialData（最准确的数据源）
            if (row.AssistantTeacherList && Array.isArray(row.AssistantTeacherList) && row.AssistantTeacherList.length > 0) {
                initialData = row.AssistantTeacherList.map(assistant => {
                    // 从 assistantDataMap 获取完整的助教数据
                    const fullAssistantData = assistantDataMap.value.get(assistant.ID)
                    if (fullAssistantData) {
                        return fullAssistantData // 返回完整的助教数据，包含状态等所有字段
                    } else {
                        return {
                            id: assistant.ID,
                            name: assistant.Name,
                            TeacherCommissionList: assistant ? assistant.TeacherCommissionList : []
                        }
                    }
                })
                assistantIds = row.AssistantTeacherList.map(assistant => assistant.ID)
            } else {
                // 备用方案：从字符串字段构建，优先从缓存获取完整数据
                assistantIds = row.AssistantTeacherID ? row.AssistantTeacherID.split(',').map(id => id.trim()).filter(Boolean) : []

                initialData = assistantIds.map(id => {
                    // 优先从 assistantDataMap 获取完整的助教数据
                    const fullAssistantData = assistantDataMap.value.get(id)
                    if (fullAssistantData) {
                        return fullAssistantData // 返回完整的助教数据，包含状态等所有字段
                    } else {
                        // 如果缓存中没有，则使用名称字段
                        const assistantNames = row.AssistantTeacherName ? row.AssistantTeacherName.split(', ').map(name => name.trim()).filter(Boolean) : []
                        const idx = assistantIds.indexOf(id)
                        return {
                            id,
                            name: assistantNames[idx] || id
                        }
                    }
                })
            }
            // 🆕 获取当前行的任课老师ID，作为助教的阻止列表
            const mainTeacherIds = row.MainTeacherID ? [row.MainTeacherID] : []

            return {
                title: row.AssistantTeacherName || '',
                modelValue: assistantIds,  // 覆盖baseProps中的modelValue，传递数组而不是字符串
                initialData,
                startTime: startTime,
                endTime: endTime,
                disabledIds: mainTeacherIds,
            }
        },
        getEvents: (row, column) => ({
            onChange: (val, assistantDataList) => {
                if (val && Array.isArray(val) && val.length > 0) {
                    // 更新助教数据映射
                    assistantDataList.forEach(assistantData => {
                        if (assistantData && assistantData.id) {
                            updateAssistantDataMap(assistantData.id, assistantData)
                        }
                    })
                    // 从 assistantDataMap 获取最准确的名称数据
                    row.AssistantTeacherList = val.map(id => {
                        const assistantData = assistantDataMap.value.get(id)
                        return {
                            ID: id,
                            Name: assistantData ? assistantData.name : (id || ''),
                            TeacherCommissionList: assistantData ? (assistantData.TeacherCommissionList || []) : []
                        }
                    })
                    // 更新显示名称字段
                    row.AssistantTeacherName = assistantDataList.map(item => item.name).join(', ')
                } else {
                    row.AssistantTeacherName = ''
                    row.AssistantTeacherList = []

                }
                // 🆕 触发任课老师与助教重复校验
                const validationResult = validateTeacherAssistantDuplicate(row)
                applyValidationResult(row, validationResult)
            },
            onGoToSettings: () => {
             // console.log('前往助教设置')
            },
            // 🆕 UI交互锁：弹框打开时锁定
            onDialogOpen: () => {
                uiInteractionLock.value = true
             // console.log('🔒 助教弹框打开，UI锁定')
                
                lockTimeout.value = setTimeout(() => {
                    if (uiInteractionLock.value) {
                        console.warn('⚠️ UI锁超时（30秒），强制释放')
                        uiInteractionLock.value = false
                        processPendingSaveResponses()
                    }
                }, 30000)
            },
            // 🆕 UI交互锁：弹框关闭时解锁
            onDialogClose: () => {
                if (lockTimeout.value) {
                    clearTimeout(lockTimeout.value)
                    lockTimeout.value = null
                }
                
                uiInteractionLock.value = false
             // console.log('🔓 助教弹框关闭，UI解锁')
                processPendingSaveResponses()
            }
        }),
        onUpdateModelValue: (row, column, val) => {
            // 同步更新传输字段，使用最新的名称数据
            if (val && Array.isArray(val) && val.length > 0) {
                // 从 assistantDataMap 获取最准确的名称数据
                row.AssistantTeacherList = val.map(id => {
                    const assistantData = assistantDataMap.value.get(id)
                    return {
                        ID: id,
                        Name: assistantData ? assistantData.name : (id || ''),
                        TeacherCommissionList: assistantData ? (assistantData.TeacherCommissionList || []) : []
                    }
                })

                // 确保 AssistantTeacherName 与 AssistantTeacherList 保持一致
                row.AssistantTeacherName = row.AssistantTeacherList.map(assistant => assistant.Name).join(', ')
            } else {
                row.AssistantTeacherList = []
                row.AssistantTeacherName = ''
            }

            // 🆕 在数据更新后触发校验（确保 AssistantTeacherID 已更新）
            nextTick(() => {
                const validationResult = validateTeacherAssistantDuplicate(row)
                applyValidationResult(row, validationResult)
            })

            // 通用逻辑已经处理了字段赋值和cellDataChange调用
        },
        getDisplayValue: (row, column) => {
            return row.AssistantTeacherName || row.AssistantTeacherID || ''
        }
    })
}
// 线上课
const CourseTypeColumn = {
    field: 'CourseType',
    key: 'CourseType',
    title: getFieldDisplayName('CourseType') || '线上课',
    width: 90,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
    renderBodyCell: createEditableCell(ElSelect, {
        getProps: (row, column) => ({
            modelValue: row.CourseType == '2' ? '2' : '1',
        }),
        getDefaultSlot: (row, column, h) => {
            const onlineOptions = [
                { label: '是', value: '2' },
                { label: '否', value: '1' }
            ];
            return () => onlineOptions.map(option =>
                h(ElOption, {
                    key: option.value,
                    label: option.label,
                    value: option.value
                })
            )
        },
        getDisplayValue: (row, column) => {
            return row.CourseType == '2' ? '是' : '否'
        },
        onUpdateModelValue: (row, column, val) => {
        }
    })
}
// 开放预约
const IsSubscribeCourseColumn = {
    field: 'IsSubscribeCourse',
    key: 'IsSubscribeCourse',
    title: getFieldDisplayName('IsSubscribeCourse') || '开放预约',
    width: 95,
    align: 'left',
    sortBy: '',
    renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
    renderBodyCell: createEditableCell(ElSelect, {
        getProps: (row, column) => ({
            clearValue: null,
            modelValue: row.IsSubscribeCourse == '1' ? '1' : '0',
        }),
        getDefaultSlot: (row, column, h) => {
            const reservationOptions = [
                { label: '是', value: '1' },
                { label: '否', value: '0' }
            ];
            return () => reservationOptions.map(option =>
                h(ElOption, {
                    key: option.value,
                    label: option.label,
                    value: option.value
                })
            )
        },
        getDisplayValue: (row, column) => {
            return row.IsSubscribeCourse == '1' ? '是' : '否'
        }
    })
}
// 获取当前表格类型对应的列字段
const getCurrentTableColumnKey = () => {
    if (selectedTableType.value == 10) {
        return 'ClassID'
    } else if (selectedTableType.value == 20) {
        return 'StudentUserID'
    }
    return ''
}
// 获取当前表格列配置
const getCurrentTableColumns = () => {
    if (selectedTableType.value == 10) {
        return [
            CampusIDColumn,
            ClassColumn,
            ShiftColumn,
            SubjectColumn,
            DateColumn,
            timeColumn,
            ClassRoomColumn,
            MainTeacherColumn,
            AssistantTeacherColumn,
            CourseTypeColumn,
            IsSubscribeCourseColumn
        ]
    } else if (selectedTableType.value == 20) {
        // 学员排课：不包含开放预约字段
        return [
            CampusIDColumn,
            StudentUserColumn,
            StudentShiftColumn,
            SubjectColumn,
            DateColumn,
            timeColumn,
            ClassRoomColumn,
            MainTeacherColumn,
            AssistantTeacherColumn,
            CourseTypeColumn
            // IsSubscribeCourseColumn - 学员排课不显示此字段
        ]
    } else if (selectedTableType.value == 30) {
        return [
            CampusIDColumn,
            SubscribeShiftColumn,
            SubjectColumn,
            DateColumn,
            timeColumn,
            ClassRoomColumn,
            MainTeacherColumn,
            AssistantTeacherColumn,
            CourseTypeColumn,
            IsSubscribeCourseColumn,
            MaxStudentCountColumn,
            StartStudentCountColumn
        ]
    }
    // 默认返回班级排课的列
    return [
        CampusIDColumn,
        ClassColumn,
        ShiftColumn,
        SubjectColumn,
        DateColumn,
        timeColumn,
        ClassRoomColumn,
        MainTeacherColumn,
        AssistantTeacherColumn,
        CourseTypeColumn,
        IsSubscribeCourseColumn
    ]
}
// 列配置
const columns = computed(() => [
    indexColumn,
    ...getCurrentTableColumns(),
    {
        field: 'InternalRemark',
        key: 'InternalRemark',
        title: getFieldDisplayName('InternalRemark') || '对内备注',
        renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
        width: 150,
        sortBy: '',
        edit:true,
        align: 'left',
        ellipsis: {
            showTitle: true,
            lineClamp: 1,
        },
    },
    // {
    //     field: 'Describe',
    //     key: 'n',
    //     title: getFieldDisplayName('Describe') || '对外备注',
    //     renderHeaderCell: ({ column }) => renderHeaderWithStar(column.title, false),
    //     width: 150,
    //     align: 'left',
    //     edit: true,
    //     sortBy: '',
    //     ellipsis: {
    //         showTitle: true,
    //         lineClamp: 1,
    //     },
    // },
    {
        field: 'preCheckStatus',
        key: 'preCheckStatus',
        title: '预检查提示',
        width: 190,
        align: 'left',
        edit: false,
        fixed: 'right',
        sortBy: '',
        ellipsis: {
            showTitle: false,
            lineClamp: 1,
        },
        renderBodyCell: ({ row, column }, _h) => {
            // 如果不是预检查模式，不显示此列
            if (!preCheckEnabled.value) {
                return null
            }

            // 如果是分组行或footer行，不显示
            if (row.isGroupRow || row.isGroupFooter) {
                return null
            }
            // 新增：未满足必填要求的行，显示提示而不参与检查
            const isReady = isRowReadyForPrecheck(row)
            if (!isReady) {
                // 统计必填字段是否至少填写了一个
                const needTeacher = courseTeacherRequired.value === true
                const requiredList = [
                    row.CampusID,
                    row[getCurrentTableColumnKey()],
                    row.ShiftID,
                    row.Date,
                    ((row.StartTime && row.EndTime) || (row.timeRange && row.timeRange.includes('~'))) ? '1' : '',
                    row.ClassRoomID
                ]
                if (needTeacher) {
                    requiredList.push(row.MainTeacherID)
                }
                const filledCount = requiredList.filter(v => !!v).length
                // 若一个必填都未填写，视为无效行：不显示任何提示
                if (filledCount === 0) {
                    return null
                }
                // 否则显示"未满足检查要求"提示
                return h('div', {
                    style: {
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'flex-start',
                        padding: '0 8px',
                        color: '#909399',
                        fontSize: '12px'
                    }
                }, '请先完善必填项！')
            }

            // ✅ 检查中状态：接口调用中，显示加载提示
            if (checkingIds.value.has(row.ID)) {
                return h('div', {
                    style: {
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'flex-start',
                        padding: '0 8px',
                        color: '#909399',
                        fontSize: '14px'
                    }
                }, '检查中...')
            }

            const checkResult = preCheckResults.value.get(row.ID)

            // 行级非法字段（后端返回的 errorField 或必填校验等）也视为限制
            const rowHasInvalid = (Array.isArray(row.errorField) && row.errorField.length > 0)
                || (validationErrorFields.value.get(row.ID)?.length > 0)

            // 未参与本轮检测：不显示该单元格（严格以本次参与列表为准）
            const isInThisCheck = preCheckedIds.value.has(row.ID)
            if (!isInThisCheck) {
             // console.log(`❌ 行ID ${row.ID} 未参与本轮检测，隐藏预检查提示`)
             // console.log('📋 当前参与检测的ID列表:', Array.from(preCheckedIds.value))
                return null
            }

            // 没有检查结果且无非法字段：本行参与了检测，判定为"通过"
            if (!checkResult && !rowHasInvalid) {
             // console.log('✅ 本行参与了检测且无问题，显示通过')
            }

         // console.log('✅ 开始渲染状态，checkResult:', checkResult, 'rowHasInvalid:', rowHasInvalid)

            // 根据预检查结果和非法字段判断状态
            const hasConflict = !!(checkResult?.ConflictFieldList &&
                (checkResult.ConflictFieldList.ConflictingDraftList?.length > 0 ||
                    checkResult.ConflictFieldList.ConflictingCourseList?.length > 0 ||
                    checkResult.ConflictFieldList.ConflictingScheduleList?.length > 0))

            // 修改这里：只有CheckFieldList不为空时才算作有限制，不再考虑rowHasInvalid
            const hasRestriction = !!(checkResult?.CheckFieldList?.length > 0)

            let statusText = ''
            let statusColor = ''

            if (hasConflict && hasRestriction) {
                statusText = '有冲突、被限制'
                statusColor = '#f56c6c' // 红色
            } else if (hasConflict) {
                statusText = '有冲突'
                statusColor = '#f56c6c' // 红色
            } else if (hasRestriction) {
                statusText = '被限制'
                statusColor = '#f56c6c' // 红色
            } else {
                statusText = '通过'
                statusColor = '#67c23a' // 绿色
            }

            return h('div', {
                style: {
                    display: 'flex',
                    alignItems: 'center',
                    padding: '0 8px'
                }
            }, [
                h('span', {
                    style: {
                        color: statusColor,
                        fontSize: '14px',
                        fontWeight: '400',
                    }
                }, statusText),
                // 如果有冲突或限制，显示查看按钮
                (hasConflict || hasRestriction) ? h('el-button', {
                    type: 'primary',
                    size: 'small',
                    text: true,
                    style: {
                        cursor: 'pointer',
                        fontSize: '14px',
                        paddingLeft: '16px',
                        minHeight: 'auto',
                        color: '#2878E8'
                    },
                    onClick: () => {
                        handleViewPreCheckDetails(row.ID, checkResult)
                    }
                }, '查看') : null
            ])
        }
    },
])

// footer汇总数据
const footerData = ref([
    {
        ID: 'footer',
    }
])

// ==================== 分组功能状态管理 ====================

// 分组按钮文字计算属性
const groupButtonText = computed(() => {
    if (!isGrouped.value || !groupByField.value) {
        return '分组'
    }
    return '已分组'
})

// 分组数据计算属性
const displayTableData = computed(() => {
    if (!isGrouped.value) {
        // 不分组时直接返回过滤后的数据，性能最优
        return filteredTableData.value
    }
    // 分组时返回分组后的数据
    return getGroupedData()
})

// 智能分组数据计算
const getGroupedData = () => {
    // 生成更完整的缓存键，包含数据和分组配置
    const cacheKey = {
        dataHash: JSON.stringify(filteredTableData.value),  // 使用过滤后的数据
        groupByField: groupByField.value,
        expandedGroups: Array.from(expandedGroups.value),
        isGrouped: isGrouped.value
    }
    const currentHash = JSON.stringify(cacheKey)

    // 检查缓存是否有效
    if (groupedDataCache.value &&
        groupedDataCache.value.isValid &&
        groupedDataCache.value.hash === currentHash) {
        return groupedDataCache.value.data
    }

    // 重新计算分组数据（使用过滤后的数据）
    const result = calculateGroupedData(filteredTableData.value)

    // 更新缓存
    groupedDataCache.value = {
        data: result,
        isValid: true,
        hash: currentHash,
        timestamp: Date.now(),
        groupByField: groupByField.value,
        expandedGroups: Array.from(expandedGroups.value)
    }
    return result
}

// 分组数据计算函数
const calculateGroupedData = (dataSource = tableData.value) => {
    const result = []

    // 如果没有选择分组字段，返回原始数据
    if (!groupByField.value) {
        return dataSource
    }

    // 按指定字段分组
    const groups = {}
    dataSource.forEach(row => {
        // 获取分组值，特殊处理不同字段的空值情况
        const groupValue = row[groupByField.value]
        let groupKey

        // 对于特定字段，需要特殊处理
        if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
            // 对于数值型字段，只有 null 和 undefined 才归类为未分组
            // 数字 0 是有效值，不应该归类为未分组
            groupKey = (groupValue === null || groupValue === undefined) ? '未分组' : String(groupValue)
        } else {
            // 对于其他字段，空值、null、undefined 都归类为"未分组"
            groupKey = groupValue || '未分组'
        }
        if (!groups[groupKey]) {
            groups[groupKey] = []
        }
        groups[groupKey].push(row)
    })

    // 生成分组后的数据
    Object.keys(groups).forEach(groupKey => {
        // 获取分组显示名称
        const groupName = groupKey === '未分组' ? '未分组' : getGroupDisplayName(groupByField.value, groupKey)

        // 添加分组行
        result.push({
            ID: `group_${groupKey}`,
            isGroupRow: true,
            groupKey: groupKey,
            groupName: groupName,
            groupCount: groups[groupKey].length,
            groupField: groupByField.value // 记录分组字段
        })

        // 如果分组展开，添加数据行和footer
        if (expandedGroups.value.has(groupKey)) {
            result.push(...groups[groupKey])

            // 添加分组footer（只在展开时显示）
            result.push({
                ID: `footer_${groupKey}`,
                isGroupFooter: true,
                groupKey: groupKey,
                groupName: groupName,
                groupField: groupByField.value // 记录分组字段
            })
        }
    })

    return result
}

// ==================== 生命周期 ====================

// 监听原始数据变化，使缓存失效
watch(tableData, (newData, oldData) => {
    if (groupedDataCache.value) {
        groupedDataCache.value.isValid = false
    }

    // 如果数据行数发生变化，清理验证错误信息（因为行号可能已经过时）
    if (oldData && newData.length !== oldData.length) {
     // console.log('📊 表格数据行数变化，清理验证错误信息')
        validationErrors.value = []
        validationErrorRowIds.value = []
        validationErrorFields.value.clear()
    }
}, { deep: true })

// 监听分组字段变化
watch(groupByField, () => {
    if (groupedDataCache.value) {
        groupedDataCache.value.isValid = false
    }

    // 如果存在验证错误，重新计算错误信息的显示
    updateValidationErrorsDisplay()
})

// 监听分组状态变化（开启/关闭分组）
watch(isGrouped, () => {
    if (groupedDataCache.value) {
        groupedDataCache.value.isValid = false
    }

    // 如果存在验证错误，重新计算错误信息的显示
    updateValidationErrorsDisplay()
})

// 监听展开状态变化
watch(expandedGroups, () => {
    if (groupedDataCache.value) {
        groupedDataCache.value.isValid = false
    }

    // 如果存在验证错误，重新计算错误信息的显示
    updateValidationErrorsDisplay()
}, { deep: true })

// 监听上课科目列显示状态，动态控制列的显示隐藏
watch(shouldShowSubjectColumn, (show, oldShow) => {
 // console.log('shouldShowSubjectColumn 变化:', { show, oldShow, tableRef: !!tableRef.value })

    // 使用实例方法动态控制列的显示隐藏
    if (tableRef.value && tableRef.value.$refs && tableRef.value.$refs.tableRef) {
        const veTableInstance = tableRef.value.$refs.tableRef

        if (show) {
            // 显示科目列
         // console.log('显示科目列')
            veTableInstance.showColumnsByKeys(['SubjectID'])
        } else {
            // 隐藏科目列
         // console.log('隐藏科目列')
            veTableInstance.hideColumnsByKeys(['SubjectID'])

            // 当从显示变为隐藏时，清空科目数据
            if (oldShow === true) {
             // console.log('科目列从显示变为隐藏，清空所有科目数据')
                tableData.value.forEach(row => {
                    if (!row.isGroupRow && !row.isGroupFooter) {
                        if (row.SubjectID || row.SubjectName) {
                         // console.log(`清空行 ${row.ID} 的科目数据，因为无全科课程行`)
                            row.SubjectID = ''
                            row.SubjectName = ''
                            // 记录变更
                            recordFieldChange(row.ID, 'SubjectID', '', 'auto_clear')
                            recordFieldChange(row.ID, 'SubjectName', '', 'auto_clear')
                        }
                    }
                })

                // 同步更新原始数据备份
                originalTableData.value = safeJsonClone('subject-column-toggle')
            }
        }
    } else {
     // console.log('ve-table 实例未准备好')
    }
})

// 🆕 监听预检查开关状态，动态控制预检查列的显示隐藏
watch(preCheckEnabled, (enabled) => {
 // console.log('预检查开关状态变化:', enabled)

    // 使用实例方法动态控制列的显示隐藏
    if (tableRef.value && tableRef.value.$refs && tableRef.value.$refs.tableRef) {
        const veTableInstance = tableRef.value.$refs.tableRef

        if (enabled) {
            // 显示预检查列
         // console.log('显示预检查列')
            veTableInstance.showColumnsByKeys(['preCheckStatus'])
        } else {
            // 隐藏预检查列
         // console.log('隐藏预检查列')
            veTableInstance.hideColumnsByKeys(['preCheckStatus'])
        }
    } else {
     // console.log('ve-table 实例未准备好')
    }
})

const courseTeacherRequired = ref(false)
const courseIsClassSubject = ref(false) // 全科课程排课，是否只能排班级上设置的科目 业务规则
const showAllStudentsWhenCoursePlan = ref(false) // 1对1排课时，是否可跨校区选择学员  1:不可以跨校区选择学员  0:可以跨校区选择学员
const Check_Shift_Teacher_Subject = ref(false) // 是否开启限制跨科目选择老师
function getAdvanceConfig() {
    querySysConfig({
        campusID: '',
        type: 0,
        configNames: 'Check_Shift_Teacher_Subject,CourseTeacherRequired,CourseIsClassSubject,ShowAllStudentsWhenCoursePlan' //配置项名称（多个时用逗号分隔）
    }).then((res) => {
        var data = res.Data;
        data.forEach((item) => {
            if (item.Name == "CourseTeacherRequired" && item.Value == 1) {
                courseTeacherRequired.value = true
            }
            if (item.Name == "CourseIsClassSubject" && item.Value == 1) {
                courseIsClassSubject.value = true
            }
            if (item.Name == "ShowAllStudentsWhenCoursePlan" && item.Value == 1) {
                showAllStudentsWhenCoursePlan.value = true
            }
            if (item.Name == "Check_Shift_Teacher_Subject" && item.Value == 1) {
				Check_Shift_Teacher_Subject.value = true;
			}
        })
    })
}

// 组件挂载后初始化加载实例
onMounted(() => {
    loadingInstance.value = window.$veLoading({
        target: document.querySelector('#loading-container'),
        name: 'wave',
    })

    // 预加载科目数据，确保 getSubjectName 函数能正常工作
    const dictFieldsStore = useDictFieldsStore()
    dictFieldsStore.fetchFieldsByType('SUBJECT')
    
    // 通知父组件骨架屏可以隐藏
    emit('component-ready')

    // 等待校区数据加载完成后加载草稿数据
    if (userCampusesStore.userCampuses.length > 0) {
        loadCourseDraftList()
    } else {
        // 如果校区数据还没加载，监听数据变化
        const unwatch = watch(() => userCampusesStore.userCampuses, (newCampuses) => {
            if (newCampuses.length > 0) {
                loadCourseDraftList()
                unwatch() // 停止监听
            }
        }, { immediate: true })
    }

    // 启动兜底检查机制
    startBackupCheck()
    getAdvanceConfig()
    // 监听ESC键退出全屏
    document.addEventListener('keydown', handleKeyDown)
})

// 组件卸载时清理
onUnmounted(() => {
    document.removeEventListener('keydown', handleKeyDown)
    // 停止兜底检查机制
    stopBackupCheck()
})

// ==================== 方法定义 ====================


/**
 * 切换全屏状态
 */
function toggleFullscreen() {
    isFullscreen.value = !isFullscreen.value
}

/**
 * 处理键盘事件
 */
function handleKeyDown(event) {
    if (event.key === 'Escape' && isFullscreen.value) {
        isFullscreen.value = false
    }
}

/**
 * 更新预检查统计数据
 * 🔑 统一使用 calculatePreCheckStats() 来保证统计逻辑一致
 */
function updatePreCheckStats() {
    calculatePreCheckStats()
}

/**
 * 查看预检查详情
 * @param {string} rowId - 行ID
 * @param {Object} checkResult - 检查结果
 */
async function handleViewPreCheckDetails(rowId, checkResult) {
 // console.log('🔍 handleViewPreCheckDetails 被调用:', { rowId, checkResult })

    // 获取当前行数据
    const targetRow = tableData.value.find(r => r.ID === rowId)

    if (!targetRow) {
        console.warn('❌ 未找到目标行:', rowId)
        return
    }
 // console.log('✅ 找到目标行:', targetRow)

    // 获取预检查结果数据
    const preCheckData = preCheckResults.value.get(rowId)
    if (!preCheckData) {
        console.warn('❌ 未找到预检查数据:', rowId)
     // console.log('📊 当前预检查结果:', preCheckResults.value)
        return
    }
 // console.log('✅ 找到预检查数据:', preCheckData)

    // 设置弹框数据
    currentPreCheckRow.value = targetRow
    currentPreCheckData.value = preCheckData
    try {
        const res = await preCheckDetailsDialogRef.value?.open()
     // console.log('预检查详情已打开并返回:', res)
    } catch (e) {
     // console.log('预检查详情关闭或失败:', e)
    }
}

/**
 * 处理来自校验错误弹窗的前往请求
 * 关闭弹窗并滚动到指定的错误行，支持分组模式下的精确定位
 * @param {Object} error - 错误信息对象，包含rowId等信息
 */
function handleGoToRowFromDialog(error) {
    if (!error.rowId) {
        console.warn('错误信息中缺少rowId:', error)
        return
    }

    // 🆕 将当前行ID标记为错误行（添加黄色背景）
    if (!validationErrorRowIds.value.includes(error.rowId)) {
        validationErrorRowIds.value.push(error.rowId)
    }

    // 先关闭弹窗
    validationErrorDialogVisible.value = false

    // 等待弹窗关闭动画完成后再滚动
    setTimeout(async () => {

        if (isGrouped.value && groupByField.value) {
            // 分组模式：确保目标分组已展开
            const targetRow = tableData.value.find(row => row.ID === error.rowId)
            if (targetRow) {
                let groupValue

                // 对于特定字段，需要特殊处理
                if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
                    groupValue = (targetRow[groupByField.value] === null || targetRow[groupByField.value] === undefined) ? '未分组' : String(targetRow[groupByField.value])
                } else {
                    groupValue = targetRow[groupByField.value] || '未分组'
                }
                // 自动展开目标分组（如果未展开）
                let needWaitForExpansion = false
                if (!expandedGroups.value.has(groupValue)) {
                    expandedGroups.value.add(groupValue)
                    needWaitForExpansion = true

                    // 强制重新计算分组数据
                    if (groupedDataCache.value) {
                        groupedDataCache.value.isValid = false
                    }
                }

                // 如果需要展开分组，等待DOM和虚拟滚动稳定
                if (needWaitForExpansion) {
                    // 等待分组数据重新计算
                    await nextTick()
                    // 等待DOM完全渲染和虚拟滚动稳定
                    await new Promise(resolve => setTimeout(resolve, 500))
                    // 再次确保数据已更新
                    await nextTick()
                }

                // 在最新的displayTableData中找到目标行的索引
                const currentDisplayData = displayTableData.value
                const targetIndex = currentDisplayData.findIndex(row => row.ID === error.rowId)

                // 在分组数据中查找目标行位置

                if (targetIndex !== -1) {
                    // 验证目标索引处确实是目标行
                    const rowAtIndex = currentDisplayData[targetIndex]
                    if (rowAtIndex && rowAtIndex.ID === error.rowId) {
                        // 使用精确的 scrollToRowKey 方法滚动
                        scrollToRow(tableRef, targetIndex, error.rowId, currentDisplayData)
                    } else {
                        // 索引验证失败，使用rowId搜索
                        scrollToRow(tableRef, 0, error.rowId, currentDisplayData)
                    }
                } else {
                    // 未找到目标行，使用rowId精确搜索
                    scrollToRow(tableRef, 0, error.rowId, currentDisplayData)
                }
            }
        } else {
            // 非分组模式：直接使用rowId滚动到目标行
            scrollToRow(tableRef, 0, error.rowId, displayTableData.value)
        }
    }, 300)
}

/**
 * 更新验证错误信息的显示（当分组状态变化时）
 * 重新计算错误信息的行号和分组名称，确保弹窗显示与当前表格状态一致
 */
function updateValidationErrorsDisplay() {
    // 如果当前没有验证错误，直接返回
    if (!validationErrors.value || validationErrors.value.length === 0) {
        return
    }

    // 使用 nextTick 确保 displayTableData 已经更新
    nextTick(() => {
        // 重新计算错误信息（行号、分组名等）
        const updatedErrors = recalculateValidationErrors(
            validationErrors.value,
            tableData.value,
            displayTableData.value,
            isGrouped.value,
            groupByField.value,
            selectedTableType.value
        )

        // 更新错误信息
        validationErrors.value = updatedErrors

        // 重新构建错误字段映射
        const errorFieldsMap = new Map()
        updatedErrors.forEach(error => {
            if (error.errorFields && error.errorFields.length > 0) {
                errorFieldsMap.set(error.rowId, error.errorFields)
            }
        })
        validationErrorFields.value = errorFieldsMap
    })
}

/**
 * 处理验证错误弹窗显示状态变化
 * 弹窗关闭时保留错误信息，以便用户可以继续查看错误单元格
 * 
 */
function handleValidationDialogVisibilityChange(visible) {
    if (!visible) {

    }
}

/**
 * 显示校验错误弹窗
 */
function showValidationErrorDialog(errors) {
    validationErrors.value = errors
    // 提取错误行的ID列表
    validationErrorRowIds.value = errors.map(error => error.rowId)

    // 构建错误字段映射
    const errorFieldsMap = new Map()
    errors.forEach(error => {
        if (error.errorFields && error.errorFields.length > 0) {
            errorFieldsMap.set(error.rowId, error.errorFields)
        }
    })
    validationErrorFields.value = errorFieldsMap

    // 如果是分组模式，自动展开包含错误的分组
    if (isGrouped.value && groupByField.value) {
        const errorRowIds = errors.map(error => error.rowId)
        const groupsToExpand = new Set()

        errorRowIds.forEach(rowId => {
            const targetRow = tableData.value.find(row => row.ID === rowId)
            if (targetRow) {
                let groupValue

                // 对于特定字段，需要特殊处理
                if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
                    groupValue = (targetRow[groupByField.value] === null || targetRow[groupByField.value] === undefined) ? '未分组' : String(targetRow[groupByField.value])
                } else {
                    groupValue = targetRow[groupByField.value] || '未分组'
                }

                groupsToExpand.add(groupValue)
            }
        })

        // 展开包含错误的分组
        let hasNewExpansion = false
        groupsToExpand.forEach(groupValue => {
            if (!expandedGroups.value.has(groupValue)) {
                expandedGroups.value.add(groupValue)
                hasNewExpansion = true
            }
        })

        // 如果有新展开的分组，重新计算数据
        if (hasNewExpansion && groupedDataCache.value) {
            groupedDataCache.value.isValid = false
         // console.log(`📂 自动展开包含错误的分组: ${Array.from(groupsToExpand)}`)
        }
    }

    validationErrorDialogVisible.value = true
}

/**
 * 处理预检查开关变化
 */
function handlePreCheckChange(value) {
    Logger.info('预检查开关变化', {
        action: value ? '开启' : '关闭',
        dataCount: tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter).length,
        selectedTableType: selectedTableType.value
    })
    if (value) {
        // 在用户确认前，不真实打开预检查
        preCheckEnabled.value = false
        // 使用 nextTick 确保 displayTableData 是最新的（特别是在分组状态刚刚变化后）
        nextTick(() => {
            // 先进行必填字段校验，传入当前显示的数据用于正确计算行号
            const validationResult = validateRequiredFields(
                tableData.value,
                displayTableData.value,
                isGrouped.value,
                groupByField.value,
                selectedTableType.value
            )

            // 非法字段收集
            const illegalRows = tableData.value
                .filter(row => !row.isGroupRow && !row.isGroupFooter)
                .filter(row => Array.isArray(row.errorField) && row.errorField.length > 0)

            // 组合两类错误（必填未填 + 非法字段），按 rowId 合并
            const combinedMap = new Map()

            if (!validationResult.isValid && Array.isArray(validationResult.errors)) {
                validationResult.errors.forEach(err => {
                    combinedMap.set(err.rowId, { ...err, errorFields: Array.from(new Set(err.errorFields || [])) })
                })
            }

            if (illegalRows.length > 0) {
                const baseErrors = illegalRows.map(row => ({
                    rowId: row.ID,
                    errorFields: Array.from(new Set(row.errorField)),
                    errors: ['存在非法字段，请修正后再开启预检查']
                }))
                const enrichedErrors = recalculateValidationErrors(
                    baseErrors,
                    tableData.value,
                    displayTableData.value,
                    isGrouped.value,
                    groupByField.value,
                    selectedTableType.value
                )
                enrichedErrors.forEach(err => {
                    if (combinedMap.has(err.rowId)) {
                        const existed = combinedMap.get(err.rowId)
                        const mergedFields = Array.from(new Set([...(existed.errorFields || []), ...(err.errorFields || [])]))
                        const mergedErrors = [...(existed.errors || []), ...(err.errors || [])]
                        combinedMap.set(err.rowId, { ...existed, errorFields: mergedFields, errors: mergedErrors })
                    } else {
                        combinedMap.set(err.rowId, err)
                    }
                })
            }

            if (combinedMap.size > 0) {
                // 显示合并后的错误并中断
                showValidationErrorDialog(Array.from(combinedMap.values()))
                preCheckEnabled.value = false
                return
            }

         // console.log(`必填字段与非法字段校验通过，共校验 ${validationResult.totalRows} 行数据`)

            // 开启预检查时，仅对当前可视区+缓冲区的ID先弹窗确认
            // 开始前先清空上一次的参与集合
            preCheckedIds.value = new Set()
            participatedIds.clear()
            checkedIds.clear()
            pendingIds.clear()
            
            // 🔧 修复：兼容分组折叠的数据收集逻辑
         // console.log('🔍 分组模式:', isGrouped.value, '分组字段:', groupByField.value)
            
            // 🆕 先统计总行数，用于区分"没有数据"和"数据不符合条件"
            const totalDataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter).length
            
            let firstBatchIds
            
            // � 简化逻辑：有分组就检查所有数据，无分组就检查可视区数据
            if (isGrouped.value && groupByField.value) {
                // � 分组模式：检查所有数据（不受折叠影响）
             // console.log('🔍 分组模式：检查所有数据')
                
                firstBatchIds = tableData.value
                    .filter(row => {
                        // 过滤掉分组行和footer行
                        if (row.isGroupRow || row.isGroupFooter) return false
                        
                        // 过滤掉空行：必须有ID且至少有一个关键字段有值
                        if (!row || !row.ID) return false
                        
                        // 检查关键字段：校区、班级/学员、课程、日期、时间、教室、任课老师
                        const hasKeyData = row.CampusID || row[getCurrentTableColumnKey()] || row.ShiftID || row.Date ||
                            row.timeRange || row.ClassRoomID || row.MainTeacherID
                        
                        return hasKeyData
                    })
                    .map(r => r.ID)
                    .map(normalizeId)
                    .filter(Boolean)
            } else {
                // 📋 非分组模式：检查可视区数据
             // console.log('🔍 非分组模式：检查可视区数据')
                
                const { start, end } = calcCurrentRangeWithBuffer()
                const visibleDisplayRows = displayTableData.value.slice(start, end + 1)
                
                firstBatchIds = visibleDisplayRows
                    .filter(row => {
                        // 过滤掉分组行和footer行
                        if (row.isGroupRow || row.isGroupFooter) return false
                        
                        // 过滤掉空行：必须有ID且至少有一个关键字段有值
                        if (!row || !row.ID) return false
                        
                        // 检查关键字段
                        const hasKeyData = row.CampusID || row[getCurrentTableColumnKey()] || row.ShiftID || row.Date ||
                            row.timeRange || row.ClassRoomID || row.MainTeacherID
                        
                        return hasKeyData
                    })
                    .map(r => r.ID)
                    .map(normalizeId)
                    .filter(Boolean)
            }
            
         // console.log('🔍 收集到的预检查ID数量:', firstBatchIds.length, '总数据行数:', totalDataRows)

            // 🆕 优化提示：区分"没有数据"和"数据不符合检查条件"
            if (firstBatchIds.length === 0) {
                if (totalDataRows === 0) {
                    // 真的没有任何数据行
                    ElMessage.warning('当前没有任何排课数据，请先添加排课内容后再开启预检查')
                } else {
                    // 有数据，但都不符合检查条件（缺少必要字段）
                    ElMessage.warning('当前排课数据缺少必要字段（如校区、课程、日期等），请补充完整后再开启预检查')
                }
                preCheckEnabled.value = false
                return
            }

            checkCourseDraftDialogRef.value?.open({
                IDList: firstBatchIds
            }).then((result) => {
                if (result?.success) {
                    // 用户确认后，正式打开预检查
                    preCheckEnabled.value = true
                 // console.log('预检查完成:', result.message)
                    if (result.data) {
                     // console.log('预检查结果数据:', result.data)

                        // 🆕 保存"本次参与检测"的 ID（应该包含所有提交检查的ID，不仅仅是有结果的）
                        // 使用初始提交的 firstBatchIds，确保所有参与检查的行都被记录
                        preCheckedIds.value = new Set(firstBatchIds)

                        // ✅ 将所有参与检查的ID都标记为已检查（避免重复检查）
                        firstBatchIds.forEach(id => {
                            checkedIds.add(id)
                            participatedIds.add(id)
                        })

                        // 保存预检查结果数据到 preCheckResults
                        preCheckResults.value = new Map()
                        if (Array.isArray(result.data)) {
                            result.data.forEach(item => {
                                const draftId = normalizeId(item.DraftId)
                                if (draftId) {
                                    preCheckResults.value.set(draftId, item)
                                    // 已经在上面统一添加过了，这里不需要重复添加
                                }
                            })
                        }
                     // console.log('✅ 本次参与ID:', Array.from(preCheckedIds.value))
                     // console.log('✅ 预检查结果Map:', preCheckResults.value)
                     // console.log('🔍 初始提交的ID数量:', firstBatchIds.length, '返回结果数量:', result.data?.length || 0)

                        // 更新统计数据
                        updatePreCheckStats()

                        // 🆕 预检查完成后自动展开所有分组以显示结果
                        if (isGrouped.value && groupByField.value) {
                            // 🔧 修复：从实际数据中计算所有可能的分组 groupKey
                            const allGroupKeys = new Set()
                            
                            // 遍历原始数据，按照分组逻辑计算 groupKey
                            tableData.value.forEach(row => {
                                if (row.isGroupRow || row.isGroupFooter) return
                                
                                const groupValue = row[groupByField.value]
                                let groupKey
                                
                                // 与 calculateGroupedData 中的逻辑保持一致
                                if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
                                    groupKey = (groupValue === null || groupValue === undefined) ? '未分组' : String(groupValue)
                                } else {
                                    groupKey = groupValue || '未分组'
                                }
                                
                                allGroupKeys.add(groupKey)
                            })
                            
                         // console.log(`🔓 准备展开所有分组，分组数量: ${allGroupKeys.size}`)
                         // console.log(`🔓 分组keys:`, Array.from(allGroupKeys))
                         // console.log(`🔓 当前展开状态: ${expandedGroups.value.size}`)
                            
                            // 🔧 强制触发响应式更新：创建新的 Set 实例
                            expandedGroups.value = new Set(allGroupKeys)
                            
                            // 🔧 强制使缓存失效，确保视图重新计算
                            if (groupedDataCache.value) {
                                groupedDataCache.value.isValid = false
                            }
                            
                         // console.log(`🔓 预检查完成，已自动展开所有 ${allGroupKeys.size} 个分组以显示检查结果`)
                         // console.log(`🔓 展开后状态: ${expandedGroups.value.size}`)
                            
                            // 🔧 使用 nextTick 确保 DOM 更新
                            nextTick(() => {
                             // console.log('🔓 DOM 更新完成，displayTableData 长度:', displayTableData.value.length)
                            })
                        }

                        // 初次完成后，立刻触发一次滚动增量检查以覆盖缓冲区之外的可见扩展
                        const range = calcCurrentRangeWithBuffer()
                     // console.log("第一个地方")
                        if (range.start == 0 && range.end == 0) {
                            return
                        }
                        triggerCheckByRange(range.start, range.end)
                    }
                    // 初次确认后，保持对话框关闭但开关保持开启，滚动将继续触发增量检查
                } else {
                    console.warn('预检查失败:', result?.message)
                    // 失败也不自动关闭开关，允许继续滚动触发增量检查或重试
                }
            }).catch(() => {
             // console.log('预检查对话框已关闭或被取消，保持预检查开关开启以支持滚动增量检查')
            })
        }) // 闭合 nextTick 回调
    } else {
        // 关闭预检查开关时的处理逻辑
     // console.log('预检查已关闭')

        // 🆕 关闭预检查时，清理由预检查添加的 errorField 和 errorMessages
        tableData.value.forEach(row => {
            if (row.errorField && Array.isArray(row.errorField)) {
                // 获取预检查结果，判断哪些字段是由预检查添加的
                const preCheckData = preCheckResults.value.get(row.ID)
                if (preCheckData) {
                    const getAliases = (field) => {
                        const aliases = new Set([field])
                        const mapped = fieldMap[field]
                        if (mapped) aliases.add(mapped)
                        for (const k in fieldMap) { if (fieldMap[k] === field) aliases.add(k) }
                        // 任课老师特殊：后端可能返回 MainTeacherList
                        if (field === 'MainTeacherName' || field === 'MainTeacherID') aliases.add('MainTeacherList')
                        // 🆕 助教特殊：后端可能返回 AssistantTeacherList
                        if (field === 'AssistantTeacherName' || field === 'AssistantTeacherID') aliases.add('AssistantTeacherList')
                        return Array.from(aliases)
                    }

                    // 检查哪些字段是预检查命中的
                    const preCheckHitFields = []

                    if (preCheckData.ErrorFieldList) {
                        row.errorField.forEach(field => {
                            const aliases = getAliases(field)
                            const matchList = (list = []) => aliases.some(a => list.includes(a))
                            if (matchList(preCheckData.ErrorFieldList)) {
                                preCheckHitFields.push(field)
                            }
                        })
                    }

                    if (preCheckData.CheckFieldList) {
                        row.errorField.forEach(field => {
                            const aliases = getAliases(field)
                            const matchList = (list = []) => aliases.some(a => list.includes(a))
                            if (preCheckData.CheckFieldList.some(c => matchList(c.FieldNameList || []))) {
                                preCheckHitFields.push(field)
                            }
                        })
                    }

                    if (preCheckData.ConflictFieldList && preCheckData.ConflictFieldList.FieldNameList) {
                        row.errorField.forEach(field => {
                            const aliases = getAliases(field)
                            const matchList = (list = []) => aliases.some(a => list.includes(a))
                            if (matchList(preCheckData.ConflictFieldList.FieldNameList)) {
                                preCheckHitFields.push(field)
                            }
                        })
                    }

                    // 从 errorField 中移除预检查命中的字段
                    row.errorField = row.errorField.filter(field => !preCheckHitFields.includes(field))

                    // 从 errorMessages 中移除预检查命中的字段消息
                    if (row.errorMessages) {
                        preCheckHitFields.forEach(field => {
                            delete row.errorMessages[field]
                        })
                    }

                    // 如果 errorField 为空，则删除该属性
                    if (row.errorField.length === 0) {
                        delete row.errorField
                    }

                    // 如果 errorMessages 为空，则删除该属性
                    if (Object.keys(row.errorMessages || {}).length === 0) {
                        delete row.errorMessages
                    }
                }
            }
        })

        // 清空预检查结果
        preCheckResults.value.clear()
        // 清空预检查统计数据
        preCheckTotalCount.value = 0
        preCheckPassedCount.value = 0
        preCheckFailedCount.value = 0
        preCheckedIds.value.clear()
    }
}

// 删除引用按钮点击：未选中阻止弹出
function onDeleteButtonClick(event) {
    if (checkedRows.value.size === 0) {
        event?.stopImmediatePropagation?.()
        event?.stopPropagation?.()
        event?.preventDefault?.()
        ElMessage.warning('请先勾选要删除的草稿数据')
        return
    }
}

/**
 * 取消删除
 */
function cancelDelete() {
    // Popover 会自动关闭，不需要手动控制
    deletePopoverRef.value?.hide()
}

/**
 * 确认删除
 */
async function confirmDelete() {
    // 开启全局 Loading
    const loading = ElLoading.service({
        lock: true,
        text: '正在删除...',
        background: 'rgba(0, 0, 0, 0.3)'
    })

    try {
        const checkedRowKeys = Array.from(checkedRows.value)

        // 调用删除草稿接口
        const response = await DeleteCourseDraft(checkedRowKeys)

        if (response.IsSuccess) {
            cancelDelete()
            // 成功后前端直接删除对应数据
            tableData.value = tableData.value.filter(row => !checkedRowKeys.includes(row.ID))
            originalTableData.value = safeJsonClone('batch-delete-success')
            // 清空勾选状态
            checkedRows.value.clear()

            // 更新预检查结果
            if (preCheckEnabled.value) {
                // 从预检查结果中移除被删除的行
                checkedRowKeys.forEach(id => {
                    preCheckResults.value.delete(id)
                    preCheckedIds.value.delete(id)
                })

                // 重新计算统计数据
                updatePreCheckStats()

                // 如果删除后没有数据了，自动关闭预检查开关
                const remainingDataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter)
                if (remainingDataRows.length === 0) {
                    preCheckEnabled.value = false
                    preCheckResults.value.clear()
                    preCheckedIds.value.clear()
                    preCheckTotalCount.value = 0
                    preCheckPassedCount.value = 0
                    preCheckFailedCount.value = 0
                }
            }

            // 显示成功提示
            ElMessage.success(`已成功删除 ${checkedRowKeys.length} 条草稿`)

            // 如果分组模式下删除了数据，需要重新计算分组
            if (isGrouped.value && groupedDataCache.value) {
                groupedDataCache.value.isValid = false
            }

            // 🆕 如果预检查冲突与限制开启，重新检查剩余数据的冲突
            if (preCheckEnabled.value) {
             // console.log('🔄 删除操作后，重新检查剩余数据的冲突')
                // 使用 nextTick 确保数据同步完成后再检查
                nextTick(() => {
                    // 获取当前表格中所有有效的行ID
                    const remainingIds = tableData.value
                        .filter(row => !row.isGroupRow && !row.isGroupFooter)
                        .map(row => row.ID)
                        .filter(Boolean)

                    if (remainingIds.length > 0) {
                     // console.log(99999999999999)
                        triggerCheckByIds(remainingIds, { force: true })
                    }
                })
            }

        } else {
            ElMessage.error(`删除失败: ${response.ErrorMsg}`)
        }

    } catch (error) {
        console.error('💥 删除草稿时发生异常:', error)
        ElMessage.error('删除草稿时发生异常，请重试')
    } finally {
        // 关闭全局 Loading
        loading.close()
    }
}

/**
 * 确认排课
 */
function confirmScheduling() {
 // console.log('确认排课')
}

/**
 * 虚拟滚动回调
 * 优化：通过缓冲区和防抖减少频繁触发
 */
let lastLoggedIndex = -1
function scrolling({ startRowIndex: newStartRowIndex }) {
    startRowIndex.value = newStartRowIndex

    // 优化日志输出：只在索引变化超过10行时才打印，避免频繁输出
    if (Math.abs(newStartRowIndex - lastLoggedIndex) > 10) {
     // console.log("🔄 虚拟滚动", newStartRowIndex)
        lastLoggedIndex = newStartRowIndex
    }

    onVirtualScrollDebounced()
}

// ==================== 分组功能方法 ====================

/**
 * 处理按规则批量新增
 */
function handleBatchAddByRules() {
    // 获取校区数据
    const campusOptions = userCampusesStore.userCampuses.map(campus => ({
        ID: campus.ID,
        Name: campus.Name
    }))

    // 打开按规则批量新增弹框，传递校区数据和当前选中的类型
    addArrangeByRuleRef.value?.open({
        campusOptions: campusOptions,
        selectedTableType: selectedTableType.value
    }).then(async (result) => {
     // console.log('按规则批量新增完成:', result)
        if (Array.isArray(result) && result.length > 0) {
            const addedCount = []
            const remaining = getRemainingCapacity()
            if (remaining <= 0) {
                ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，已达上限`)
                return
            }
            if (result.length > remaining) {
                ElMessage.warning(`本次拟新增 ${result.length} 行，超出 ${result.length - remaining} 行，已仅新增 ${remaining} 行`)
            }
            result.slice(0, remaining).forEach((item) => {
                const newRow = { ...(item || {}) }
                // 确保ID存在且为UUID
                if (!newRow.ID || !isUUID(String(newRow.ID))) {
                    newRow.ID = generateUUID()
                }
                // 处理任课老师数据
                if (newRow.MainTeacherList && newRow.MainTeacherList.length > 0) {
                    const mainTeacher = newRow.MainTeacherList[0]
                    newRow.MainTeacherID = mainTeacher.ID
                    newRow.MainTeacherName = mainTeacher.Name + (mainTeacher.TeacherCommissionList && mainTeacher.TeacherCommissionList.length > 0 && mainTeacher.TeacherCommissionList[0].Name ? `（${mainTeacher.TeacherCommissionList[0].Name}）` : '')
                    newRow.MainTeacherCommissionList = mainTeacher.TeacherCommissionList || []
                    // 🆕 更新教师数据映射，包含完整的 TeacherCommissionList
                    updateTeacherDataMap(mainTeacher.ID, {
                        id: mainTeacher.ID,
                        name: mainTeacher.Name,
                        campusId: newRow.CampusID
                    })
                }
                // 处理助教数据
                if (newRow.AssistantTeacherList && newRow.AssistantTeacherList.length > 0) {
                    // 处理助教 ID 和 Name 字段
                    const assistantIds = []
                    const assistantNames = []

                    newRow.AssistantTeacherList.forEach(assistant => {
                        assistantIds.push(assistant.ID)
                        assistantNames.push(assistant.Name + (assistant.TeacherCommissionList && assistant.TeacherCommissionList.length > 0 && assistant.TeacherCommissionList[0].Name ? `（${assistant.TeacherCommissionList[0].Name}）` : ''))
                        // 更新助教数据映射
                        updateAssistantDataMap(assistant.ID, {
                            id: assistant.ID,
                            name: assistant.Name + (assistant.TeacherCommissionList && assistant.TeacherCommissionList.length > 0 && assistant.TeacherCommissionList[0].Name ? `（${assistant.TeacherCommissionList[0].Name}）` : ''),
                            campusId: newRow.CampusID,
                            TeacherCommissionList: assistant.TeacherCommissionList || []
                        })
                    })

                    // 设置助教相关字段
                    newRow.AssistantTeacherID = assistantIds.join(',')
                    newRow.AssistantTeacherName = assistantNames.join(', ')
                }
                // 标记必要的运行时属性
                newRow.isGroupRow = false
                newRow.isGroupFooter = false

                // 合并到当前草稿数据
                tableData.value.push(newRow)
                // 记录为新增行，来源为按规则批量新增
                recordNewRow(newRow.ID, newRow, 'batchRuleAdd')
                addedCount.push(newRow.ID)

                // 如果处于分组模式，则确保新行所在分组展开
                if (isGrouped.value && groupByField.value) {
                    let groupValue
                    if (groupByField.value === 'CourseType' || groupByField.value === 'IsSubscribeCourse') {
                        groupValue = (newRow[groupByField.value] === null || newRow[groupByField.value] === undefined) ? '未分组' : String(newRow[groupByField.value])
                    } else {
                        groupValue = newRow[groupByField.value] || '未分组'
                    }
                    if (groupValue !== undefined && !expandedGroups.value.has(groupValue)) {
                        expandedGroups.value.add(groupValue)
                        if (groupedDataCache.value) {
                            groupedDataCache.value.isValid = false
                        }
                    }
                }
            })

            // 等待分组数据刷新后滚动定位到第一条新增记录
            if (isGrouped.value) {
                await nextTick()
                await new Promise(resolve => setTimeout(resolve, 200))
            }

            ElMessage.success(`按规则批量新增成功，共新增 ${addedCount.length} 行并已保存草稿；最多支持${MAX_TABLE_ROWS}条`)
        } else {
         // console.log('按规则批量新增返回空数组或无效结果，忽略')
        }
    }).catch((error) => {
     // console.log('按规则批量新增被取消或出错:', error)
        // 用户取消或出错时不需要特殊处理
    })
}

/**
 * 处理来自预检查详情弹框的前往修改请求
 * @param {Object} params - 包含rowId和firstProblemField
 */
function handleGoToModifyFromDialog({ rowId, firstProblemField }) {
    // 复用现有的滚动功能
    handleGoToRowFromDialog({
        rowId,
        field: firstProblemField
    })
}

/**
 * 收集有效行数据（与预检查逻辑一致）
 */
function collectValidRows() {
    return tableData.value.filter(row => {
        if (row.isGroupRow || row.isGroupFooter) return false

        // 检查关键字段：任一有值即视为有效行
        const hasKeyData = row.CampusID || row[getCurrentTableColumnKey()] || row.ShiftID || row.Date ||
            row.timeRange || row.ClassRoomID || row.MainTeacherID

        return hasKeyData
    })
}

/**
 * 开始排课
 */
async function startPublishing() {
    // 收集有效行
    const validRows = collectValidRows()

    if (validRows.length === 0) {
        ElMessage.warning('当前没有任何排课数据，请先添加排课内容后再开始排课')
        return
    }

    // 必填字段校验
    const validationResult = validateRequiredFields(
        tableData.value,
        displayTableData.value,
        isGrouped.value,
        groupByField.value,
        selectedTableType.value
    )

    if (!validationResult.isValid) {
        // 显示校验错误弹窗
        showValidationErrorDialog(validationResult.errors)
        return
    }

    // 设置统计数据
    publishTotalCount.value = validRows.length
    publishPassedCount.value = 0
    publishFailedCount.value = 0

    // 如果开启了预检查，计算通过和失败数量
    if (preCheckEnabled.value) {
        let passed = 0
        let failed = 0

        validRows.forEach(row => {
            const preCheckData = preCheckResults.value.get(row.ID)
            if (preCheckData) {
                const hasConflict = !!(preCheckData.ConflictFieldList &&
                    (preCheckData.ConflictFieldList.ConflictingDraftList?.length > 0 ||
                        preCheckData.ConflictFieldList.ConflictingCourseList?.length > 0 ||
                        preCheckData.ConflictFieldList.ConflictingScheduleList?.length > 0))

                const hasRestriction = !!(preCheckData.CheckFieldList?.length > 0 ||
                    (Array.isArray(row.errorField) && row.errorField.length > 0))

                if (hasConflict || hasRestriction) {
                    failed++
                } else {
                    passed++
                }
            } else {
                // 没有预检查数据，检查是否有错误字段
                if (Array.isArray(row.errorField) && row.errorField.length > 0) {
                    failed++
                } else {
                    passed++
                }
            }
        })

        publishPassedCount.value = passed
        publishFailedCount.value = failed
    } else {
        // 未开启预检查，所有行都算通过
        publishPassedCount.value = validRows.length
        publishFailedCount.value = 0
    }

    // 显示排课确认弹框
    courseDraftPublishVisible.value = true
}

/**
 * 处理排课确认
 */
async function handlePublishConfirm({ checkConflict }) {
    try {
        // 收集有效行ID
        const validRows = collectValidRows()
        const draftIds = validRows.map(row => row.ID)

        // 检查是否有可排的数据
        if (draftIds.length === 0) {
            ElMessage.warning('当前没有任何可排课的排课数据')
            return
        }

        // 如果开启了预检查，进一步检查是否有通过的数据
        if (preCheckEnabled.value) {
            let hasPassableData = false

            validRows.forEach(row => {
                const preCheckData = preCheckResults.value.get(row.ID)
                if (preCheckData) {
                    const hasConflict = !!(preCheckData.ConflictFieldList &&
                        (preCheckData.ConflictFieldList.ConflictingDraftList?.length > 0 ||
                            preCheckData.ConflictFieldList.ConflictingCourseList?.length > 0 ||
                            preCheckData.ConflictFieldList.ConflictingScheduleList?.length > 0))

                    const hasRestriction = !!(preCheckData.CheckFieldList?.length > 0 ||
                        (Array.isArray(row.errorField) && row.errorField.length > 0))

                    if (!hasConflict && !hasRestriction) {
                        hasPassableData = true
                    }
                } else {
                    // 没有预检查数据，检查是否有错误字段
                    if (!(Array.isArray(row.errorField) && row.errorField.length > 0)) {
                        hasPassableData = true
                    }
                }
            })
            if (!hasPassableData) {
                ElMessage.warning('没有可排课的数据，请调整！')
                return
            }
        }
        // 设置状态
        publishInProgress.value = true
        lastSubmittedIds.value = [...draftIds]
        isRetryForPassedOnly.value = false

        // 切换到进行中状态
        courseDraftPublishDialogRef.value?.setProcessing()
        // 调用排课接口
        const response = await CourseDraftPublishDraft({
            DraftIDList: draftIds,
            CheckConflict: checkConflict ? 1 : 0
        })

        // 处理响应（显示弹框）
        await handlePublishResponse(response, { showDialog: true })

    } catch (error) {
        console.error('排课失败:', error)
        ElMessage.error('排课失败，请重试')
        Logger.error("排课发布失败", {
            error: error,
            totalCount: publishTotalCount.value,
            passedCount: publishPassedCount.value,
            failedCount: publishFailedCount.value
        })
        // 关闭弹框，让用户重新开始
        courseDraftPublishVisible.value = false
        publishInProgress.value = false
    }
}

/**
 * 处理排课响应
 */
async function handlePublishResponse(response, { showDialog = true } = {}) {
    // 检查是否有数据返回，即使 IsSuccess 为 true
    const hasData = response.Data && response.Data.length > 0

    // 当 Data 存在但所有项均无冲突/限制/非法字段时，也应视为完全成功
    const dataHasIssues = Array.isArray(response.Data) && response.Data.some(item => {
        const hasErrorFields = Array.isArray(item.ErrorFieldList) && item.ErrorFieldList.length > 0
        const hasRestrictions = Array.isArray(item.CheckFieldList) && item.CheckFieldList.length > 0
        const conflict = item.ConflictFieldList || {}
        const hasConflicts = (Array.isArray(conflict.ConflictingDraftList) && conflict.ConflictingDraftList.length > 0)
            || (Array.isArray(conflict.ConflictingCourseList) && conflict.ConflictingCourseList.length > 0)
            || (Array.isArray(conflict.ConflictingScheduleList) && conflict.ConflictingScheduleList.length > 0)
            || !!conflict.ErrorMessage
        return hasErrorFields || hasRestrictions || hasConflicts
    })

    // 后端明确返回失败：提示消息并结束流程，不弹异常弹框
    if (!response.IsSuccess) {
        ElMessage.warning(response.ErrorMsg || '排课失败，请检查数据后重试')
        if (showDialog) {
            courseDraftPublishVisible.value = false
            publishExceptionVisible.value = false
        }
        publishInProgress.value = false
        return { success: false, showDialog: false }
    }
    if (response.IsSuccess && (!hasData || (hasData && !dataHasIssues))) {
        // 排课完全成功，没有任何限制或冲突
        const completedCount = publishPassedCount.value
        if (showDialog) {
            courseDraftPublishDialogRef.value?.setCompleted(completedCount, publishTotalCount.value, 0)
        }
        return { success: true, showDialog: true, completedCount, totalCount: publishTotalCount.value, failedCount: 0 }
    }

    // 存在限制/冲突/非法，即使 IsSuccess 为 true
    const errorData = response.Data || []

    // 解析错误数据
    parsePublishErrors(errorData)
    if (showDialog) {
        // 显示异常弹框
        publishExceptionVisible.value = true
        // 隐藏主弹框
        courseDraftPublishVisible.value = false
    }

    return { success: true, showDialog: false, hasErrors: true }
}

/**
 * 解析排课错误数据
 */
function parsePublishErrors(errorData) {
    const restrictionDetails = []
    const conflictDetails = []
    const failedIds = new Set()

    errorData.forEach(item => {
        if (item.DraftId) {
            failedIds.add(item.DraftId)
        }

        // 处理限制字段 (CheckFieldList)
        if (item.CheckFieldList && item.CheckFieldList.length > 0) {
            item.CheckFieldList.forEach(checkField => {
                if (checkField.ErrorMessage) {
                    restrictionDetails.push(checkField.ErrorMessage)
                }
            })
        }

        // 处理冲突字段 (ConflictFieldList)
        if (item.ConflictFieldList) {
            // 检查是否有冲突信息
            const hasConflict = item.ConflictFieldList.ConflictingDraftList?.length > 0 ||
                item.ConflictFieldList.ConflictingCourseList?.length > 0 ||
                item.ConflictFieldList.ConflictingScheduleList?.length > 0 ||
                item.ConflictFieldList.ErrorMessage

            if (hasConflict) {
                if (item.ConflictFieldList.ErrorMessage) {
                    conflictDetails.push(item.ConflictFieldList.ErrorMessage)
                } else {
                    // 如果没有具体错误信息，生成通用冲突描述
                    conflictDetails.push(`行 ${item.DraftId} 存在时间冲突`)
                }
            }
        }

        // 处理非法字段 (ErrorFieldList)
        if (item.ErrorFieldList && item.ErrorFieldList.length > 0) {
            item.ErrorFieldList.forEach(errorField => {
                if (errorField.ErrorMessage) {
                    restrictionDetails.push(errorField.ErrorMessage)
                }
            })
        }
    })

    // 更新状态
    failedIdsFromPublish.value = failedIds
    publishRestrictionCount.value = restrictionDetails.length
    publishRestrictionDetails.value = [...new Set(restrictionDetails)] // 去重
    publishConflictCount.value = conflictDetails.length
    publishConflictDetails.value = [...new Set(conflictDetails)] // 去重

    // 更新表格标注
    updateTableErrorMarking(errorData)
}

/**
 * 更新表格错误标注
 */
function updateTableErrorMarking(errorData) {
    errorData.forEach(item => {
        const row = tableData.value.find(r => r.ID === item.DraftId)
        if (row) {
            // 设置错误字段
            const errorFields = []
            if (item.ErrorFieldList) {
                errorFields.push(...item.ErrorFieldList.map(f => f.FieldName))
            }
            if (item.CheckFieldList) {
                item.CheckFieldList.forEach(checkField => {
                    if (checkField.FieldNameList) {
                        errorFields.push(...checkField.FieldNameList)
                    }
                })
            }
            if (item.ConflictFieldList && item.ConflictFieldList.FieldNameList) {
                errorFields.push(...item.ConflictFieldList.FieldNameList)
            }

            row.errorField = [...new Set(errorFields)]

            // 设置错误信息
            row.errorMessages = {}
            if (item.ErrorFieldList) {
                item.ErrorFieldList.forEach(errorField => {
                    if (errorField.FieldName && errorField.ErrorMessage) {
                        row.errorMessages[errorField.FieldName] = errorField.ErrorMessage
                    }
                })
            }
        }
    })
}

/**
 * 处理排课取消
 */
function handlePublishCancel() {
    courseDraftPublishVisible.value = false
    publishInProgress.value = false
}

/**
 * 处理排课完成
 */
async function handlePublishComplete({ clearTableData }) {
    if (clearTableData) {
        // 清空表格数据
        const allDraftIds = tableData.value
            .filter(row => !row.isGroupRow && !row.isGroupFooter)
            .map(row => row.ID)

        if (allDraftIds.length > 0) {
            try {
                await DeleteCourseDraft(allDraftIds)
                ElMessage.success('草稿数据已清空')

                // 重新加载草稿列表
                await loadCourseDraftList()
            } catch (error) {
                console.error('清空草稿失败:', error)
                ElMessage.error('清空草稿失败')
            }
        }
    }

    courseDraftPublishVisible.value = false
    publishInProgress.value = false
}

/**
 * 继续排通过检查的排课
 */
async function handleContinuePartialPublish() {
    // 显示全局loading
    const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在排课中...',
        background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
        // 保存原始总数和失败数（用于最终显示）
        const originalTotalCount = publishTotalCount.value
        const originalFailedCount = failedIdsFromPublish.value.size

        // 过滤掉失败的ID
        const remainingIds = lastSubmittedIds.value.filter(id => !failedIdsFromPublish.value.has(id))

        if (remainingIds.length === 0) {
            loadingInstance.close()
            ElMessage.warning(transToConfigDescript('没有可排的课程'))

            // 自动开启预检查开关，让用户能看到具体的限制信息
            if (!preCheckEnabled.value) {
                preCheckEnabled.value = true
                // ElMessage.info('已自动开启预检查，请查看具体的限制信息')

                // 触发预检查以显示最新的限制信息
                nextTick(() => {
                    handlePreCheckChange()
                })
            }

            return
        }

        // 更新统计数据（用于进度显示和最终结果）
        publishTotalCount.value = originalTotalCount  // 保持原始总数
        publishPassedCount.value = remainingIds.length  // 可以排的数量
        publishFailedCount.value = originalTotalCount - remainingIds.length  // 失败的数量

        // 设置重试状态
        isRetryForPassedOnly.value = true
        lastSubmittedIds.value = remainingIds

        // 调用排课接口
        const response = await CourseDraftPublishDraft({
            DraftIDList: remainingIds,
            CheckConflict: 0 // 重试时不检查冲突
        })

        // 关闭loading
        loadingInstance.close()

        // 隐藏可能存在的异常弹框
        publishExceptionVisible.value = false

        // 直接处理响应，不自动显示弹框
        const result = await handlePublishResponse(response, { showDialog: false })

        // 根据处理结果显示相应的弹框
        if (result.success && result.showDialog) {
            // 完全成功，显示成功弹框
            courseDraftPublishVisible.value = true
            // 等待弹框渲染完成，然后设置为完成状态
            nextTick(() => {
                courseDraftPublishDialogRef.value?.setProcessing()
                setTimeout(() => {
                    // 使用原始总数和失败数显示最终结果
                    courseDraftPublishDialogRef.value?.setCompleted(result.completedCount, originalTotalCount, originalFailedCount)
                }, 100)
            })
        } else if (result.success && result.hasErrors) {
            // 有异常数据，显示异常弹框
            publishExceptionVisible.value = true
        } else if (!result.success) {
            // 完全失败，已经显示过消息，不需要额外操作
        }

    } catch (error) {
        loadingInstance.close()
        console.error('重试排课失败:', error)
        ElMessage.error('重试排课失败，请重试')
    }
}

/**
 * 返回修改
 * 从发布异常弹窗返回，自动开启预检查并触发冲突检查
 */
function handleReturnModifyFromPublish() {
    publishExceptionVisible.value = false

    // 自动开启预检查开关，让用户能看到具体的限制信息
    if (!preCheckEnabled.value) {
        preCheckEnabled.value = true
    }

    // 触发预检查以显示最新的限制信息
    nextTick(() => {
        // 获取当前表格中所有有效的行ID
        const remainingIds = tableData.value
            .filter(row => !row.isGroupRow && !row.isGroupFooter)
            .map(row => row.ID)
            .filter(Boolean)

        if (remainingIds.length === 0) {
            return
        }

        // ✅ 步骤1：触发完整的预检查流程
        handlePreCheckChange()

        // ✅ 步骤2：强制触发冲突检查（force: true 跳过缓存）
        // triggerCheckByIds 内部会自动设置 checkingIds 显示"检查中..."状态
        triggerCheckByIds(remainingIds, { force: true })
    })
}

/**
 * 处理分组字段变化
 */
function handleGroupFieldChange(field) {
    if (field) {
        // 启用分组
        isGrouped.value = true
        groupByField.value = field
        // 清空展开状态，重新计算分组
        expandedGroups.value.clear()

        // 使缓存失效，触发重新计算
        if (groupedDataCache.value) {
            groupedDataCache.value.isValid = false
        }

        // 关闭popover
        groupPopoverVisible.value = false
        // 记录分组功能使用
        Logger.info('数据分组操作', {
            action: '启用分组',
            groupBy: field,
            dataCount: tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter).length,
            selectedTableType: selectedTableType.value
        })
    } else {
        // 如果清空了分组字段，取消分组
        handleGroupClear()
    }
}

/**
 * 取消分组
 */
function handleGroupClear() {
    isGrouped.value = false
    groupByField.value = ""
    expandedGroups.value.clear()

    if (groupedDataCache.value) {
        groupedDataCache.value.isValid = false
    }
    // 关闭popover
    groupPopoverVisible.value = false
    // 记录分组功能使用
    Logger.info('数据分组操作', {
        action: '取消分组',
        dataCount: tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter).length,
        selectedTableType: selectedTableType.value
    })
}

/**
 * 切换分组展开状态
 */
function toggleGroupExpand(groupKey) {
    clickGroupRow.value = true
    // 防止事件冒泡
    event?.stopPropagation()

    if (expandedGroups.value.has(groupKey)) {
        // 收起分组
        expandedGroups.value.delete(groupKey)
    } else {
        // 展开分组
        expandedGroups.value.add(groupKey)
    }

    // 使缓存失效，触发重新计算
    if (groupedDataCache.value) {
        groupedDataCache.value.isValid = false
    }
}

// 暴露组件方法供父组件调用
defineExpose({
    scrollToRow: (rowIndex, rowId = null) => scrollToRow(tableRef, rowIndex, rowId, tableData.value),
    scrollToFirstErrorRow: () => scrollToFirstErrorRow(tableRef, validationErrorRowIds.value, tableData.value),
    // 其他可能需要的方法
    saveDraft,
    loadCourseDraftList,
    handlePreCheckChange,
    handleGoToModifyFromDialog,
    // 调试用：暴露状态信息
    getDebugInfo: () => ({
        isGrouped: isGrouped.value,
        groupByField: groupByField.value,
        tableDataLength: tableData.value.length,
        displayDataLength: displayTableData.value.length,
        hasValidationErrors: validationErrors.value.length > 0,
        validationErrorCount: validationErrors.value.length
    })
})

// 渐进式预检查集合
const checkedIds = new Set()
const pendingIds = new Set()
const participatedIds = new Set() // 本轮已参与过检查的ID（用于滚动时彻底去重）

// 预检查就绪判定：必填字段任意为空则不参与预检查
function isRowReadyForPrecheck(row) {
    if (!row || row.isGroupRow || row.isGroupFooter) return false

    // 基础必填字段：校区、课程、日期、时间
    const hasCampus = !!row.CampusID
    const hasShift = !!row.ShiftID
    const hasDate = !!row.Date
    const hasTime = (!!row.StartTime && !!row.EndTime) || (row.timeRange && row.timeRange.includes('~'))

    // 根据表格类型判断班级/学员字段
    let hasClassOrStudent = true
    if (selectedTableType.value === 10) {
        // 给班级排课：需要班级
        hasClassOrStudent = !!row.ClassID
    } else if (selectedTableType.value === 20) {
        // 给学员排课：需要学员
        hasClassOrStudent = !!row.StudentUserID
    } else if (selectedTableType.value === 30) {
        // 排预约课：不需要班级或学员
        hasClassOrStudent = true
    }

    // 教室仅在线下课必填
    const needClassroom = row.CourseType != '2'
    const hasClassroom = !!row.ClassRoomID

    // 任课老师是否必填取决于配置
    const needTeacher = courseTeacherRequired.value === true
    const hasTeacher = !!row.MainTeacherID

    // 检查所有必填字段
    if (!(hasCampus && hasClassOrStudent && hasShift && hasDate && hasTime && (needClassroom ? hasClassroom : true))) return false
    if (needTeacher && !hasTeacher) return false
    return true
}

function normalizeId(id) {
    return (id ?? '').toString()
}

// 可视区+缓冲区计算
function getVisibleBodyRowCount() {
    const inst = tableRef.value?.getTableInstance?.()
    const rows = inst?.$el?.querySelectorAll?.('.fan-table-body-tr') || []
    return rows.length || 0
}

function calcCurrentRangeWithBuffer() {
    const total = displayTableData.value?.length || 0
    const visible = getVisibleBodyRowCount()
    const PRE_BUFFER = 10
    const POST_BUFFER = 30
    const start = Math.max(0, startRowIndex.value - PRE_BUFFER)
    const end = Math.min(total - 1, startRowIndex.value + visible + POST_BUFFER)
    return { start, end }
}

function triggerCheckByIds(idList, { force = false } = {}) {
 // console.log("检查冲突")
    const raw = Array.isArray(idList) ? idList : []
    const norm = raw.map(normalizeId).filter(Boolean)
    // 过滤出就绪的 ID（防止外部调用时传入未就绪行）
    const readySet = new Set(
        tableData.value.filter(isRowReadyForPrecheck).map(r => r.ID)
    )
    const toConsider = norm.filter(id => readySet.has(id))
    const toCheck = toConsider.filter(id => {
        if (force) return !pendingIds.has(id)
        return !checkedIds.has(id) && !pendingIds.has(id) && !participatedIds.has(id)
    })
    if (!toCheck.length) return
    
    // ✅ 在接口调用前，标记这些行为"检查中"状态
    toCheck.forEach(id => {
        pendingIds.add(id)
        checkingIds.value.add(id) // 显示"检查中..."
    })
 // console.log('🔄 [触发检查] 标记检查中的行:', toCheck.length, '条')
    CheckCourseDraft({ IDList: toCheck }).then(res => {
        if (res?.ErrorCode === 200 && Array.isArray(res.Data)) {
            const returnedIds = new Set()
            res.Data.forEach(item => {
                const draftId = normalizeId(item?.DraftId)
                if (draftId) {
                    returnedIds.add(draftId)
                    preCheckedIds.value.add(draftId)
                    preCheckResults.value.set(draftId, item)
                    checkedIds.add(draftId)
                    participatedIds.add(draftId)

                    // 🆕 精确更新错误标记：根据最新的检查结果构建 errorField 和 errorMessages
                    const row = tableData.value.find(r => r.ID === draftId)
                    if (row) {
                        const errorFields = []
                        const errorMessages = {}
                        
                        // 收集错误字段
                        if (item.ErrorFieldList && Array.isArray(item.ErrorFieldList)) {
                            item.ErrorFieldList.forEach(errorField => {
                                if (errorField.FieldName) {
                                    errorFields.push(errorField.FieldName)
                                    if (errorField.ErrorMessage) {
                                        errorMessages[errorField.FieldName] = errorField.ErrorMessage
                                    }
                                }
                            })
                        }
                        
                        // 收集限制字段
                        if (item.CheckFieldList && Array.isArray(item.CheckFieldList)) {
                            item.CheckFieldList.forEach(checkField => {
                                if (checkField.FieldNameList && Array.isArray(checkField.FieldNameList)) {
                                    errorFields.push(...checkField.FieldNameList)
                                }
                            })
                        }
                        
                        // 收集冲突字段
                        if (item.ConflictFieldList && item.ConflictFieldList.FieldNameList) {
                            errorFields.push(...item.ConflictFieldList.FieldNameList)
                        }
                        
                        // 更新行的错误标记（去重）
                        row.errorField = [...new Set(errorFields)]
                        row.errorMessages = errorMessages
                    }
                }
            })

            // 对于本次提交但未在返回数据中的 ID，视为无问题，清理错误并记录参与
            toCheck.forEach(id => {
                if (!returnedIds.has(id)) {
                    preCheckedIds.value.add(id)
                    checkedIds.add(id)
                    participatedIds.add(id)
                    // 清理错误标记
                    const row = tableData.value.find(r => r.ID === id)
                    if (row) {
                        row.errorField = []
                        row.errorMessages = {}
                    }
                    // 确保之前可能遗留的结果被移除
                    preCheckResults.value.delete(id)
                }
            })
        } else {
            ElMessage.error(res?.ErrorMessage || '预检查失败，请重试')
        }
    }).catch(error => {
        console.error('预检查请求失败:', error)
        ElMessage.error('预检查请求失败，请重试')
    }).finally(() => {
        // ✅ 清理状态：移除检查队列和检查中标记
        toCheck.forEach(id => {
            pendingIds.delete(id)
            checkingIds.value.delete(id) // 清除"检查中..."状态，显示检查结果
        })
        // 重新计算预检查统计数据
        calculatePreCheckStats()
    })
}

function triggerCheckByRange(start, end) {
    const slice = displayTableData.value.slice(start, end + 1)
    const ids = slice
        .filter(row => {
            if (!row || !row.ID) return false
            // 仅预检查就绪的行才参与检查
            return isRowReadyForPrecheck(row)
        })
        .map(r => r.ID)
        .map(normalizeId)
        .filter(Boolean)
 // console.log('triggerCheckByRange', ids)
    triggerCheckByIds(ids)
}

// 轻量防抖实现，避免外部依赖
function debounce(fn, wait = 300) {
    let timeout
    const debounced = function (...args) {
        if (timeout) clearTimeout(timeout)
        timeout = setTimeout(() => fn.apply(this, args), wait)
    }
    debounced.cancel = () => {
        if (timeout) clearTimeout(timeout)
        timeout = null
    }
    return debounced
}

const onVirtualScrollDebounced = debounce(() => {
    if (!preCheckEnabled.value) return
    const { start, end } = calcCurrentRangeWithBuffer()

    // 🆕 检查滚动范围是否真正发生变化
    const lastRange = lastScrollRange.value
    if (lastRange.start === start && lastRange.end === end) {
     // console.log("📍 滚动范围未变化，跳过预检查", { start, end })
        return
    }

    // 🆕 更新滚动范围记录
    lastScrollRange.value = { start, end }

    if (start == 0 && end == 0) {
        return
    }
 // console.log("📍 虚拟滚动预检查", { start, end, lastRange })
    triggerCheckByRange(start, end)
}, 300)

// 新增：按表格类型返回分组选项
const computedGroupFieldOptions = computed(() => {
    return getGroupFieldOptionsByTableType(selectedTableType.value)
})

// 根据"仅显示异常项"过滤表格数据
const filteredTableData = computed(() => {
    if (!preCheckEnabled.value || !showOnlyAbnormal.value) {
        return tableData.value
    }
    return tableData.value.filter(row => {
        if (row.isGroupRow || row.isGroupFooter) {
            return true
        }
        const result = preCheckResults.value.get(row.ID)
        return result && result.status !== 'passed' && result.status !== 'checking'
    })
})

/**
 * 🆕 计算预检查统计数据
 */
const calculatePreCheckStats = () => {
    // 仅统计参与了本轮检查的草稿
    const checkedIds = Array.from(preCheckedIds.value)
    if (checkedIds.length === 0) {
        preCheckTotalCount.value = 0
        preCheckPassedCount.value = 0
        preCheckFailedCount.value = 0
        return
    }

    preCheckTotalCount.value = checkedIds.length
    let passed = 0
    let failed = 0

    checkedIds.forEach(id => {
        const result = preCheckResults.value.get(id)
        if (result) {
            // 检查是否有任何冲突或限制
            const hasError = result.ErrorFieldList?.length > 0
            const hasConflict = result.ConflictFieldList?.ConflictingCourseList?.length > 0 ||
                result.ConflictFieldList?.ConflictingScheduleList?.length > 0 ||
                result.ConflictFieldList?.ConflictingDraftList?.length > 0 ||
                result.ConflictFieldList?.ErrorMessage
            const hasRestriction = result.CheckFieldList?.length > 0

            if (hasError || hasConflict || hasRestriction) {
                failed++
            } else {
                passed++
            }
        } else {
            // 如果在 preCheckedIds 中但没有结果，说明该记录没有冲突和限制，算作通过
            passed++
        }
    })

    preCheckPassedCount.value = passed
    preCheckFailedCount.value = failed
}

</script>
<style lang="scss" scoped>
@use './class-table-course.scss' as *;

// 骨架屏样式 - 只显示表格部分
.skeleton-table-wrapper {
    background: #fff;
    border-radius: 8px;
    height: calc(100% - 65px); // 减去操作栏的高度
    min-height: 500px;
    
    .skeleton-table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0px 16px;
        background: linear-gradient(90deg, #f0f2f5 25%, #e0e4e8 50%, #f0f2f5 75%);
        background-size: 200% 100%;
        animation: shimmer 2s infinite linear;
        border-radius: 8px 8px 0 0;
        margin-bottom: 2px;
        font-weight: 500;
    }
    
    .skeleton-table-body {
        border-radius: 0 0 8px 8px;
        overflow: hidden;
        
        .skeleton-table-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px;
            gap: 8px;
            background: linear-gradient(90deg, #fafbfc 25%, #f0f2f5 50%, #fafbfc 75%);
            background-size: 200% 100%;
            animation: shimmer 2s infinite linear;
            border-bottom: 1px solid #e8e8e8;
            transition: all 0.3s;
            
            &:hover {
                background: #f9fafb;
                transform: translateX(2px);
            }
            
            &:last-child {
                border-bottom: none;
                border-radius: 0 0 8px 8px;
            }
        }
    }
}

@keyframes shimmer {
    0% {
        background-position: 200% 0;
    }
    100% {
        background-position: -200% 0;
    }
}
</style>