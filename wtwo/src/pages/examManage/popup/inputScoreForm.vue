<template>
    <el-dialog v-model="dialogVisible" :width="isFullscreen ? '100vw' : '80%'" 
        :class="['inputScoreForm', { 'is-fullscreen': isFullscreen }]"
        :close-on-click-modal="false" :append-to-body="true" @close="close" :destroy-on-close="true"
        :show-close="!isFullscreen" :header="isFullscreen ? '' : '录入成绩'" style="min-width: 960px;">
        <template #header v-if="!isFullscreen">
            <div class="dialog-header">
                <span class="dialog-title">录入成绩</span>
            </div>
        </template>
        <div class="dialog-body-wrap" v-loading="loading">
            <!-- 提示信息 -->
            <div class="info-banner" v-if="!isFullscreen">
                <el-icon class="info-icon"><InfoFilled /></el-icon>
                <span>支持2种成绩录入方式：手动选择学员、导入成绩表格；需注意只有考试适用校区、课程（如有选择）下的学员支持录入成绩。</span>
            </div>
            
            <!-- 考试信息 -->
            <div class="exam-info" v-if="!isFullscreen">
                <div class="info-grid">
                    <div class="info-item">
                        <span class="label">考试名称：</span>
                        <el-tooltip placement="top-start" effect="light">
                            <template #content> 
                                <div class="max-w-500px">
                                    {{examInfo.ExamName}}
                                </div>
                            </template>
                            <span class="value">{{ examInfo.ExamName }}</span>
                        </el-tooltip>
                        <!-- <el-ellipsis style="max-width: 216px" :tooltip="true">
                            <span class="value">{{ examInfo.ExamName }}</span>
                        </el-ellipsis> -->
                    </div>
                    <div class="info-item">
                        <span class="label">考试类型：</span>
                        <span class="value">{{ examInfo.ExamTypeName || '-' }}</span>
                    </div>
                    <!-- ExamMode: 0, // 考试模式：0=全部，1=线下考试，2=线上考试 -->
                    <!-- <div class="info-item">
                        <span class="label">考试模式：</span>
                        <span class="value">{{ examInfo.ExamMode === 0 ? '全部' : examInfo.ExamMode === 1 ? '线下考试' : '线上考试' }}</span>
                    </div> -->
                    <div class="info-item">
                        <span class="label">考试科目：</span>
                        <span class="value">{{formatArrayItem(examInfo.Subjects, '、')  }}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">考试年级：</span>
                        <span class="value">{{ formatArrayItem(examInfo.Grades, '、')   }}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">考试项目：</span>
                        <el-tooltip placement="top-start" effect="light">
                            <template #content> 
                                <div class="max-w-500px">
                                    {{formatExamItems(examInfo.ExamItems)}}
                                </div>
                            </template>
                            <span class="value">{{ formatExamItems(examInfo.ExamItems)}}</span>
                        </el-tooltip>
                    </div>
                    <!-- <div class="info-item info-item-full"> -->
                    <div class="info-item">
                        <span class="label">考试日期：</span>
                        <span class="value">{{ examInfo.ExamDate || '-'}}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">适用校区：</span>
                        <el-tooltip placement="bottom-start" effect="light">
                            <template #content> 
                                <div class="max-w-500px">
                                    {{formatArrayItem(examInfo.Campuses, '、')}}
                                </div>
                            </template>
                            <span class="value">{{ formatArrayItem(examInfo.Campuses, '、')}}</span>
                        </el-tooltip>
                    </div>
                    <div class="info-item">
                        <span class="label">适用课程：</span>
                        <el-tooltip placement="bottom-start" effect="light">
                            <template #content> 
                                <div class="max-w-500px">
                                    {{formatArrayItem(examInfo.Courses, '、')}}
                                </div>
                            </template>
                            <span class="value">{{formatArrayItem(examInfo.Courses, '、')}}</span>
                        </el-tooltip>
                    </div>
                    
                </div>
            </div>
            
            <!-- 学员搜索和操作栏 -->
            <div class="search-section">
                <div class="action-buttons">
                    <el-popover placement="bottom" :width="120" trigger="click">
                        <template #reference>
                            <el-button plain class="w32px h32px">
                                <template #icon>
                                    <el-icon size="20px">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-gengduocaozuo"></use>
                                        </svg>
                                    </el-icon>
                                </template>
                            </el-button>
                        </template>
                        <div class="popover-menu">
                            <!-- <div class="menu-item" @click="handleImportLocalFile">
                                <span>导入本地表格</span>
                            </div> -->
                            <div class="menu-item" @click="handleImportLocalFileOSS">
                                <span>导入本地表格</span>
                            </div>
                            <div class="menu-item" @click="handleDownloadTemplate">
                                <span>下载表格模板</span>
                            </div>
                            <div class="menu-item" @click="handleDelete">
                                <span>删除</span>
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
                    <!-- <el-button type="primary" @click="handleConfirmInput">确认录入</el-button> -->
                </div>
            </div>

            <!-- 表格 -->
            <base-table 
                id="loading-container" 
                v-loading="isDraftLoading" 
                ref="tableRef" 
                :columns="columns" 
                :style="{width: '100%', maxHeight: isFullscreen ? 'calc(100% - 32px)' : 'calc(100% - 220px)'}"
                :class="{
                    'click-group-row': clickGroupRow
                }"
                :table-data="displayTableData" 
                :footer-data="footerData" 
                :edit-option="editOption"
                :virtual-scroll-option="virtualScrollOption" 
                :row-style-option="rowStyleOption"
                :cell-autofill-option="cellAutofillOption" 
                :event-custom-option="eventCustomOption"
                :cell-selection-option="cellSelectionOption" 
                :loading-option="loadingOption"
                :cell-style-option="cellStyleOption" 
                :cell-span-option="cellSpanOption" 
                :sort-option="sortOption"
                :clipboard-option="clipboardOption" 
                :column-hidden-option="columnHiddenOption" 
                fixed-header
                :scroll-width="750" 
                :column-width-resize-option="columnWidthResizeOption"
                :max-height="isFullscreen ? 'calc(100%)' : '100%'" 
                border-y 
                border-x 
                row-key-field-name="ID"
                @scrolling="scrolling"
            />

        </div>
        
        <template #footer>
            <div class="flex-end">
                <el-button @click="close" :disabled="loading">取消</el-button>
                <el-button type="primary" @click="handleSaveAll" :disabled="loading">确认录入</el-button>
            </div>
        </template>
    </el-dialog>
    
    <!-- 学员选择弹窗 -->
    <chooseStudent ref="chooseStudentRef" />
    
    <!-- 班级选择弹窗 -->
    <chooseClass ref="chooseClassRef" />
    
    <!-- 验证错误弹窗 -->
    <ValidationErrorDialog 
        v-model="validationErrorDialogVisible" 
        :errors="validationErrors"
        @goToRow="handleGoToRowFromDialog" 
        @update:modelValue="handleValidationDialogVisibilityChange" 
    />
    <el-dialog
        v-model="importResultDialogVisible"
        
        title="导入结果"
        width="500"
        append-to-body
        class="import-result-dialog"
    >
        <div class="import-result-content">
            <div class="import-result-success">导入成功：
                <span class="count">{{parsedDataInfo.successCount}}</span>条成绩数据
            </div>
            <div class="import-result-fail">导入失败：
                <span class="count">{{parsedDataInfo.failCount}}</span>条成绩数据 
                <span class="view-reason" v-show="parsedDataInfo.failCount > 0" @click="importResultFailReasonVisible = !importResultFailReasonVisible">查看原因 
                    <el-icon><ArrowDown v-if="!importResultFailReasonVisible" /><ArrowUp v-else /></el-icon>
                </span>
            </div>
             <div class="import-result-fail-reason" v-show="importResultFailReasonVisible && parsedDataInfo.failCount > 0">
                 <el-table :data="importResultTableData" style="width: 100%" height="250">
                     <el-table-column prop="failReason" label="失败原因" width="120" />
                     <el-table-column prop="failCount" label="失败数量" width="80" />
                     <el-table-column prop="rowNumbers" label="导入表格行号" />
                 </el-table>
             </div>
        </div>
        <template #footer>
        <span class="dialog-footer">
            <el-button type="primary" @click="importResultDialogVisible = false"
            >我知道了</el-button
            >
        </span>
        </template>
    </el-dialog>
    <el-dialog
        v-model="failedStudentsDialogVisible"
        title="录入结果"
        :show-close="false"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        width="500"
        append-to-body
        class="import-result-dialog"
    >
        <div class="import-result-content">
            <div class="import-result-success">录入成功：
                <span class="count">{{ inputResultInfo.totalCount - (inputResultInfo.failCount || 0) }}</span>条成绩数据
            </div>
            <div class="import-result-fail">录入失败：
                <span class="count">{{ inputResultInfo.failCount }}</span>条成绩数据
                <span class="view-reason" v-show="inputResultInfo.failCount > 0" @click="failedStudentsTableDataVisible = !failedStudentsTableDataVisible">查看原因 
                    <el-icon><ArrowDown v-if="!failedStudentsTableDataVisible" /><ArrowUp v-else /></el-icon>
                </span>
            </div>
            <el-table :data="failedStudentsTableData" style="width: 100%" height="250" v-if="inputResultInfo.failCount > 0 && failedStudentsTableDataVisible">
                <el-table-column prop="studentName" label="学员姓名" width="120" show-overflow-tooltip />
                <el-table-column prop="failReason" label="失败原因" min-width="300" show-overflow-tooltip />
            </el-table>
        </div>
        <template #footer>
            <span class="dialog-footer">
                <el-button type="primary" @click="handleCloseFailedStudentsDialog">我知道了</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, watch, h, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElPopover, ElInput, ElInputNumber, ElSelect, ElOption, ElLoading, ElCheckbox, ElIcon } from 'element-plus'
import { InfoFilled, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import BaseTable from '@/components/common/base-table/baseTable.vue'
import { generateUUID, isUUID } from '@/utils/uuid/uuid'
import { useTableData } from '@/pages/scheduleManage/tableCourseSchedule/composables/useTableData'
import { useValidation } from '@/pages/scheduleManage/tableCourseSchedule/composables/useValidation'
import { useTableScroll } from '@/pages/scheduleManage/tableCourseSchedule/composables/useTableScroll'
import { renderHeaderWithStar } from '@/pages/scheduleManage/tableCourseSchedule/composables/useTableUtils'
import chooseStudent from '@/components/popup/chooseStudentExam.vue'
import chooseClass from '@/components/popup/chooseClassExam.vue'
import ValidationErrorDialog from './validation-error-dialog.vue'
import {
    getClassesByStudents,
    getStudentsByClasses,
    batchSaveScore,
    getTemplate,
    batchCheckScoreStatus,
    parseExcelScoreFile,
    parseExcelScoreFileOSS,
    getExamDetail,
    queryDictionaryList
} from '@/api/exam'
import { downloadFile } from '@/utils'
import { useCurrentCampuses, useLoginInfo, useUserCampuses } from '@/store'
import { apiUrl } from '@/store'
import { uploadFile } from '@/services/oss'

// 当前校区
const currentCampus = computed(() => {
    console.log('currentCampus', useCurrentCampuses().campusList)
    let campusList = useCurrentCampuses().campusList;
    return campusList ? campusList?.split(',') : []
})
// 获取用户可操作校区列表
const userCampusesStore = useUserCampuses();
const userCampuses = userCampusesStore.userCampuses; // 这是一个数组，包含用户可操作的所有校区
console.log('userCampuses', userCampuses)
// 其他状态和引用 ================================start
const dialogVisible = ref(false)
const loading = ref(false)
const isFullscreen = ref(false)
const importResultDialogVisible = ref(false)
// 考试信息（保留用于模板显示）
const examInfo = ref<any>({})
const importResultFailReasonVisible = ref(false)
const importResultTableData = ref<any[]>([])
const parsedDataInfo = ref<any>({})

// 等级选项数据
const gradeOptions:any = ref([])
// 失败学员表格数据
const failedStudentsTableData = ref<any[]>([])
// 失败学员弹窗显示状态
const failedStudentsDialogVisible = ref(false)
// 失败学员表格数据显示状态
const failedStudentsTableDataVisible = ref(false)
// 录入结果信息
const inputResultInfo = ref({
    totalCount: 0,
    successCount: 0,
    failCount: 0,
    failedStudents: []
})
// 其他状态和引用 ================================end

// 表格状态和引用 ================================start

// ==================== 表格核心状态 ====================
const isGrouped = ref(false)                    // 是否启用分组（默认关闭）
const groupByField = ref('')                    // 按哪个字段分组（默认不选择）
const expandedGroups = ref(new Set())           // 展开的分组集合（默认不展开任何分组）
const groupedDataCache = ref<{data: any[], isValid: boolean, timestamp: number} | null>(null)     // 分组数据缓存

// 表格类型数据
const selectedTableType = ref(Number(localStorage.getItem('wtwo_selectedTableType') || 10))

// ==================== 响应式数据 ====================
const { tableData, originalTableData, cellDataChange, handleAddTenRows, sortOption, initTableData } = useTableData({
    isGrouped: isGrouped,
    groupedDataCache: groupedDataCache,
    selectedTableType: selectedTableType,
})


// 表格引用
const tableRef = ref<any>(null)
// 弹出框引用
const addMenuPopoverRef = ref<any>(null)
const addMenuPopoverFooterRef = ref<any>(null)
const chooseStudentRef = ref<any>(null)
const chooseClassRef = ref<any>(null)
// 组件引用映射
const componentRefs = ref(new Map())
// 双击单元格标识 - 存储当前编辑的单元格信息
const cellDbClickKey = ref<{rowId: string, field: string} | null>(null)
// 加载实例
const loadingInstance = ref(null)
// 开始行索引
const startRowIndex = ref(0)
const autoSaveTime = ref('')
// 草稿列表加载状态
const isDraftLoading = ref(false)
// 分组popover相关状态
const groupPopoverVisible = ref(false) // 分组popover显示状态

// 预检查开关状态（保留用于表格功能）
const preCheckEnabled = ref(false)

// 统一数据量上限控制
const MAX_TABLE_ROWS = 3000

// 其他状态变量
const clickGroupRow = ref(false)

// 验证错误状态管理
const validationErrorRowIds = ref<string[]>([])
const validationErrorFields = ref<Map<string, string[]>>(new Map())
const validationErrorDialogVisible = ref(false)
const validationErrors = ref<any[]>([])

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


// 表格状态和引用 ================================end

// ==================== 计算属性 ====================
// 容量检查函数
function getDataRowCount() {
    if (!tableData.value) return 0
    return tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter).length
}

function getRemainingCapacity() {
    return Math.max(0, MAX_TABLE_ROWS - getDataRowCount())
}


// 显示数据
const displayTableData = computed(() => {
    if (!isGrouped.value) {
        return tableData.value
    }
    
    // 检查缓存是否有效
    if (groupedDataCache.value && groupedDataCache.value.isValid) {
        return groupedDataCache.value.data
    }
    
    // 重新计算分组
    const grouped = calculateGroupedData()
    
    // 更新缓存
    groupedDataCache.value = {
        data: grouped,
        isValid: true,
        timestamp: Date.now()
    }
    
    return grouped
})

// 计算分组数据
const calculateGroupedData = (): any[] => {
    const groups = new Map()
    const result: any[] = []
    
    // 按分组字段分组
    tableData.value.forEach(row => {
        const groupKey = row[groupByField.value]
        if (!groups.has(groupKey)) {
            groups.set(groupKey, [])
        }
        groups.get(groupKey).push(row)
    })
    
    // 生成分组数据
    groups.forEach((rows, groupKey) => {
        // 添加分组行
        result.push({
            ID: `group_${groupKey}`,
            isGroupRow: true,
            groupKey,
            groupTitle: `${groupByField.value}: ${groupKey}`,
            groupCount: rows.length
        })
        
        // 添加数据行
        result.push(...rows)
        
        // 添加分组底部行
        result.push({
            ID: `group_footer_${groupKey}`,
            isGroupFooter: true,
            groupKey,
            groupCount: rows.length
        })
    })
    
    return result
}


// ==================== 表格排课核心功能 ====================

// 创建可编辑单元格 - 与 class-table-course.vue 完全一致
const createEditableCell = (Component: any, options: any = {}) => {
    return ({ row, column }: any) => {
        const isEditing = cellDbClickKey.value?.rowId === row.ID &&
            cellDbClickKey.value?.field === column.field

        // 非编辑状态：显示值
        if (!isEditing) {
            const rawValue = options.getDisplayValue ? 
                options.getDisplayValue(row, column) : 
                (row[options.displayField || column.field] || '')
            
            // 确保显示值是字符串，避免 [Object,Object] 问题
            const displayValue = rawValue !== null && rawValue !== undefined ? String(rawValue) : ''
            
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

        // 编辑状态：渲染编辑组件
        const displayValue = options.getDisplayValue ? 
            options.getDisplayValue(row, column) : 
            (row[column.field] !== null && row[column.field] !== undefined ? String(row[column.field]) : '')
        
        const baseProps = {
            key: `${row.ID}_${row[column.field]}`, // 强制销毁和重建组件，避免状态污染
            modelValue: row[column.field] !== null && row[column.field] !== undefined ? row[column.field] : '',
            placeholder: options.placeholder || '',
            id: `${row.ID}_${column.field}`,
            style: { width: '100%', height: '100%' },
            cellMode: true,
            title: displayValue, // 确保 title 是字符串
            ...options
        }

        // 处理 getProps
        if (options.getProps) {
            Object.assign(baseProps, options.getProps(row, column))
        }

        // 为 ElSelect 组件添加选项
        if (Component === ElSelect && options.getProps) {
            const selectProps = options.getProps(row, column)
            if (selectProps && selectProps.options) {
                baseProps.options = selectProps.options
            }
        }

        const baseEvents = {
            'onUpdate:modelValue': (val: any) => {
                const oldValue = row[column.field]
                
                // 更新数据
                row[column.field] = val
                
                // 处理 onUpdateModelValue
                if (options.onUpdateModelValue) {
                    options.onUpdateModelValue(row, column, val)
                }
                
                // 处理 getEvents
                if (options.getEvents) {
                    const events = options.getEvents(row, column)
                    if (events.onChange) {
                        events.onChange(val, row)
                    }
                }
                
                // 记录变更
                recordFieldChange(row.ID, column.field, val, 'edit')
                
                // 清除该字段的错误状态
                if (row.errorField && Array.isArray(row.errorField)) {
                    const fieldIndex = row.errorField.indexOf(column.field)
                    if (fieldIndex > -1) {
                        row.errorField.splice(fieldIndex, 1)
                    }
                }
                
                // 清除验证错误状态
                if (validationErrorRowIds.value.includes(row.ID)) {
                    const rowErrorFields = validationErrorFields.value.get(row.ID)
                    if (rowErrorFields && rowErrorFields.includes(column.field)) {
                        // 从错误字段列表中移除该字段
                        const updatedErrorFields = rowErrorFields.filter((field: string) => field !== column.field)
                        if (updatedErrorFields.length === 0) {
                            // 如果没有错误字段了，从错误行列表中移除
                            validationErrorRowIds.value = validationErrorRowIds.value.filter(id => id !== row.ID)
                            validationErrorFields.value.delete(row.ID)
                        } else {
                            // 更新错误字段列表
                            validationErrorFields.value.set(row.ID, updatedErrorFields)
                        }
                    }
                }
            },
            onBlur: () => {
                cellDbClickKey.value = null
                if (options.getEvents) {
                    const events = options.getEvents(row, column)
                    if (events.onExitEdit) {
                        events.onExitEdit()
                    }
                }
            },
            onEnter: () => {
                cellDbClickKey.value = null
                if (options.getEvents) {
                    const events = options.getEvents(row, column)
                    if (events.onExitEdit) {
                        events.onExitEdit()
                    }
                }
            }
        }

        return h(Component, {
            ...baseProps,
            ...baseEvents
        }, {
            default: Component === ElSelect && baseProps.options ? 
                () => baseProps.options.map((option: any) => 
                    h(ElOption, {
                        key: option.value,
                        label: option.label,
                        value: option.value
                    })
                ) : undefined
        })
    }
}

// 序号列
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
    renderHeaderCell: ({ column }: any, _h: any) => {
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
    renderBodyCell: ({ row, column, rowIndex }: any, _h: any) => {
        // 分组行渲染
        if (row.isGroupRow) {
            const isExpanded = expandedGroups.value.has(row.groupKey)
            return h('div', {
                class: 'group-header-cell',
                onClick: () => toggleGroupExpand(row.groupKey)
            }, [
                h('span', {
                    class: 'group-toggle-icon',
                }, isExpanded ? '▼' : '▶'),
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
                                handleSelectStudentsToAdd(row.groupKey)
                                
                                // 隐藏弹出框
                                if (addMenuPopoverRef.value && typeof addMenuPopoverRef.value.hide === 'function') {
                                    addMenuPopoverRef.value.hide()
                                }
                            }
                        }, [
                            h('span', '选择学员新增')
                        ]),
                        
                        h('div', {
                            class: 'add-menu-item',
                            onClick: () => {
                                handleSelectClassesToAdd()
                                // 隐藏弹出框
                                if (addMenuPopoverRef.value && typeof addMenuPopoverRef.value.hide === 'function') {
                                    addMenuPopoverRef.value.hide()
                                }
                            }
                        }, [
                            h('span', '选择班级新增')
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
            (rowIndex + 1 + startRowIndex.value)
        )
    },
    renderFooterCell: ({ row }: any, _h: any) => {
        // 分组时不显示底部添加行
        if (isGrouped.value) {
            return null
        }
        
        // 底部添加行渲染
        if (row.isFooterRow) {
            return h('div', {
                class: 'footer-row-cell',
                style: {
                    backgroundColor: '#fafafa',
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
                                handleSelectStudentsToAdd('')
                                
                                // 隐藏弹出框
                                if (addMenuPopoverFooterRef.value && typeof addMenuPopoverFooterRef.value.hide === 'function') {
                                    addMenuPopoverFooterRef.value.hide()
                                }
                            }
                        }, [
                            h('span', '选择学员新增')
                        ]),
                        
                        h('div', {
                            class: 'add-menu-item',
                            onClick: () => {
                                handleSelectClassesToAdd()
                                // 隐藏弹出框
                                if (addMenuPopoverFooterRef.value && typeof addMenuPopoverFooterRef.value.hide === 'function') {
                                    addMenuPopoverFooterRef.value.hide()
                                }
                            }
                        }, [
                            h('span', '选择班级新增')
                        ])
                    ])
                })
            ])
        }
        
        return null
    }
}

// 其他方法 ================================start

// 获取校区合集（可操作校区和适用校区的交集）
const campusIdsStr = computed(() => {
    const userCampusIds = userCampuses ? userCampuses.map((campus: any) => campus.ID) : []
    const examCampusIds = examInfo.value.Campuses ? examInfo.value.Campuses.map((campus: any) => campus.Id) : []
    const allCampusIds = userCampusIds.filter((campusId: any) => examCampusIds.includes(campusId))
    console.log('校区合集:', {
        userCampusIds,
        examCampusIds,
        allCampusIds,
        campusIdsStr
    })
    return allCampusIds.join(',')
})
// ==================== 表格功能函数 ====================

// 记录字段变更
const recordFieldChange = async (rowId: string, field: string, newValue: any, changeType: string) => {
    const changeRecord = {
        rowId,
        field,
        oldValue: getOldValue(rowId, field),
        newValue,
        changeType,
        timestamp: Date.now()
    }
    console.log('字段变更记录:', changeRecord)
    console.log('tableData.value', tableData.value)
    const currentRowIndex = tableData.value.findIndex(row => row.ID === rowId)
    const currentRow = currentRowIndex > -1 ? tableData.value[currentRowIndex] : null
    if(newValue && newValue != '' && currentRow){
        console.log('currentRow', currentRow)
        console.log('currentRowIndex', currentRowIndex)
        let checkStatusRes = null
        if(currentRow.StudentID){
            try {
                checkStatusRes = await batchCheckScoreStatus({
                    examId: examInfo.value.Id,
                    campusId: campusIdsStr.value,
                    students:[
                        {
                            studentId: currentRow.StudentID,
                            classId: newValue
                        }
                    ]
                })
                console.log('checkStatusRes-------', checkStatusRes)
                let studentStatuses = []
                if (checkStatusRes && checkStatusRes.ErrorCode == 200) {
                    const checkStatusData = checkStatusRes.Data
                    console.log('checkStatusData-------', checkStatusData)
                    studentStatuses = checkStatusData.studentStatuses;
                }
                // 查找学员的成绩状态
                let hasScore = false
                if (studentStatuses && Array.isArray(studentStatuses)) {
                    const studentStatus = studentStatuses.find((status: any) => status.studentId === currentRow.StudentID)
                    if (studentStatus) {
                        hasScore = studentStatus.hasScore || false
                    }
                }
                currentRow.HasScore = hasScore
                // 更新当前行成绩状态
                tableData.value.splice(currentRowIndex, 1, currentRow)
                console.log('tableData.value更新后-------', tableData.value)
            } catch (error) {
                // 抛出异常，不影响后续程序执行
                console.log('error-------', error)
            }

        }
        
    }
    
    // 如果切换的是班级字段，需要更新当前行的重复标记
    if (field === 'ClassID' && currentRow) {
        // 获取所有数据行（排除分组行、footer行等）
        const dataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter && !row.isFooterRow && row.StudentID)
        const isRepeat = dataRows.some((row: any) => row.ID !== currentRow.ID && isDuplicateStudent(currentRow, row))
        tableData.value[currentRowIndex].isRepeat = isRepeat
    }
    
    if (cellDataChange && typeof cellDataChange === 'object' && 'value' in cellDataChange) {
        (cellDataChange as any).value.push(changeRecord)
    }
}

// 获取旧值
const getOldValue = (rowId: string, field: string) => {
    const originalRow = originalTableData.value.find(row => row.ID === rowId)
    console.log('originalRow', originalRow)
    console.log('field', field)

    return originalRow ? originalRow[field] : null
}

// 记录新增行
const recordNewRow = (rowId: string, rowData: any, addType: string) => {
    const newRowRecord = {
        rowId,
        rowData,
        addType,
        timestamp: Date.now()
    }
    
}

// 分组展开/收起
const toggleGroupExpand = (groupKey: any) => {
    clickGroupRow.value = true
    if (expandedGroups.value.has(groupKey)) {
        expandedGroups.value.delete(groupKey)
    } else {
        expandedGroups.value.add(groupKey)
    }
    // 清除缓存，强制重新计算
    groupedDataCache.value = null
}

// 判断两个学员是否重复（考虑班级ID）
// 规则：学员ID一致时要判断班级ID，班级ID一致是重复学员，班级ID不一致则不是重复学员，班级ID为空时只根据学员ID判断
const isDuplicateStudent = (student1: any, student2: any): boolean => {
    // StudentID 必须一致
    if (student1.StudentID !== student2.StudentID) {
        return false
    }
    
    // 统一字段名：将 ClassId 转换为 ClassID
    const classId1 = (student1.ClassID || student1.ClassId || '').toString()
    const classId2 = (student2.ClassID || student2.ClassId || '').toString()
    
    // 如果两个学员都没有 ClassID（都为空），只需要 StudentID 一致就算重复
    if (!classId1 && !classId2) {
        return true
    }
    
    // 如果两个学员都有 ClassID，需要 ClassID 也一致才算重复
    if (classId1 && classId2) {
        return classId1 === classId2
    }
    
    // 如果一个有 ClassID，一个没有 ClassID，不算重复
    return false
}

// 根据学员获取班级选项
const getClassOptionsForStudents = async (students: any[]) => {
    let classOptionsRes = null
    try {
        let res = await getClassesByStudents({
            studentIds: students.map((student: any) => student.StudentID),
        })
        if (res && res.ErrorCode == 200) {
            classOptionsRes = res.Data
        }
    } catch (error) {
        console.warn('获取班级选项失败，使用默认选项:', error)
        // 接口报错不影响后续程序执行，继续使用默认班级选项
    }
    return classOptionsRes
}

// 表格数据排序函数
const sortTableData = () => {
    // tableData.value排序 规则：既是重复学员又是重复录入>重复学员>重复录入>其他学员
    tableData.value.sort((a: any, b: any) => {
        // 跳过非数据行（分组行、footer行等）
        if (a.isGroupRow || a.isGroupFooter || a.isFooterRow || b.isGroupRow || b.isGroupFooter || b.isFooterRow) {
            return 0
        }
        
        // 计算优先级：数字越小优先级越高
        const getPriority = (row: any) => {
            const isRepeat = row.isRepeat || false
            const hasScore = row.HasScore || false
            
            if (isRepeat && hasScore) {
                return 1 // 最高优先级：既是重复学员又是重复录入
            } else if (isRepeat) {
                return 2 // 第二优先级：重复学员
            } else if (hasScore) {
                return 3 // 第三优先级：重复录入（已录入成绩）
            } else {
                return 4 // 最低优先级：其他学员
            }
        }
        
        const priorityA = getPriority(a)
        const priorityB = getPriority(b)
        
        return priorityA - priorityB
    })
}


// 为学员创建表格行
const createStudentRows = (students: any[], classOptionsRes: any, groupKey?: string, studentStatuses?: any[]) => {
    console.log('createStudentRows', students, classOptionsRes, groupKey, studentStatuses)
    const newRows = []
    for (let i = 0; i < students.length; i++) {
        const student = students[i]
        
        // 根据API返回的班级数据设置班级选项
        let classOptions: any[] = [] // 默认班级选项
        
        // 如果API返回了班级数据，使用对应学员的班级
        if (classOptionsRes && Array.isArray(classOptionsRes)) {
            const studentClasses = classOptionsRes.find((item: any) => item.StudentID === student.StudentID)
            if (studentClasses && studentClasses.Classes && Array.isArray(studentClasses.Classes)) {
                classOptions = studentClasses.Classes.map((cls: any) => ({
                    ...cls,
                    value: cls.ClassID,
                    label: cls.ClassName
                }))
            }
        }
        
        // 查找学员的成绩状态
        let hasScore = false
        if (studentStatuses && Array.isArray(studentStatuses)) {
            const studentStatus = studentStatuses.find((status: any) => status.studentId === student.StudentID)
            if (studentStatus) {
                hasScore = studentStatus.hasScore || false
            }
        }
        
        const newRow = {
            ID: generateUUID(),
            StudentID: student.StudentID,
            StudentName: student.StudentName,
            StudentSerial: student.Serial,
            StudentPhone: student.SmsTel,
            ClassID: '',
            ClassName: '',
            classOptions: classOptions,
            Memo: '',
            HasScore: hasScore,
            isRepeat: student.isRepeat || false, // 添加重复标记
            errorField: []
        }
        // 根据考试项动态添加成绩字段
        // 修改时间：2024-12-19 | 修改原因：添加考试项状态判断，避免为无效考试项创建字段，防止后续验证和提交问题
        if (examInfo.value.ExamItems && Array.isArray(examInfo.value.ExamItems)) {
            examInfo.value.ExamItems.forEach((examItem: any, index: number) => {
                if(examItem.status) {  // 只处理状态有效的考试项
                    const fieldName = `Score${index + 1}`;
                    (newRow as any)[fieldName] = ''
                }
            })
        }
            
        // 判断 student.ClassId 是否存在（xlsx导入）且在classOptions中存在
        if (student.ClassId && classOptions.some((option: any) => option.value === student.ClassId)) {
            newRow.ClassID = student.ClassId
            newRow.ClassName = classOptions.find((option: any) => option.value === student.ClassId).label
        } else if(student.ClassId && !classOptions.some((option: any) => option.value === student.ClassId)){
            newRow.ClassID = ''
            newRow.ClassName = ''
        }else{
            // 如果班级选项只有一个，自动选中
            if (classOptions.length === 1) {
                newRow.ClassID = classOptions[0].value
                newRow.ClassName = classOptions[0].label
            }
        }
        
        // 如果分组，设置分组字段值
        if (groupKey && groupByField.value) {
            (newRow as any)[groupByField.value] = groupKey
        }
        
        newRows.push(newRow)
        recordNewRow(newRow.ID, newRow, 'add')
    }
    return newRows
}

// 选择班级新增
const handleSelectClassesToAdd = () => {
        const remaining = getRemainingCapacity()
    if (remaining <= 0) {
        ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，已达上限`)
            return
        }
        
    // 获取校区合集（可操作校区和适用校区的交集）
    const userCampusIds = userCampuses ? userCampuses.map((campus: any) => campus.ID) : []
    const examCampusIds = examInfo.value.Campuses ? examInfo.value.Campuses.map((campus: any) => campus.Id) : []
    const campusIds = userCampusIds.filter((campusId: any) => examCampusIds.includes(campusId)).join(',')
    console.log('campusIds', campusIds)
    // 打开班级选择弹窗，参考 examManage.vue 的 selectClass 方法
    chooseClassRef.value?.open({
        multi: true, // 多选模式
        choosed: [], // 初始没有选中任何班级
        campusMultiple: true, // 校区选择器支持多选
        showCampus: true, // 显示校区筛选
        showShiftType: true, // 显示班次类型筛选
        campusID: campusIds, // 限制校区范围
        showClassStatusFilter: true, // 显示班级状态筛选
        condition: {
            finished: 0, // 只显示未结业的班级
        }
    }).then(async (res: any) => {
        if (res && res.data) {
            console.log('选择班级返回数据:', res.data)
            isDraftLoading.value = true;
            // 处理返回的班级数据
            let classes = []
            if (Array.isArray(res.data)) {
                classes = res.data
            } else {
                classes = [res.data]
            }
            
            if (classes.length === 0) {
                ElMessage.info('未选择任何班级')
                return
            }
            
            // 检查新增数量限制
            const maxAvailableSlots = Math.min(remaining, classes.length)
            if (classes.length > remaining) {
                ElMessage.warning(`最多只能新增${remaining}行，已为您自动选择前${maxAvailableSlots}个班级`)
            }
            
            
            // 根据班级获取学员（接口报错不影响后续执行）
            let classStudentsRes = null
            try {
                let res = await getStudentsByClasses({
                    classIds: classes.map((cls: any) => cls.ID),
                })
                if (res && res.ErrorCode ==200) {
                    classStudentsRes = res.Data
                }
                console.log('班级选项:', classStudentsRes)
            } catch (error) {
                console.warn('获取班级选项失败，使用默认选项:', error)
                // 接口报错不影响后续程序执行，继续使用默认班级选项
                isDraftLoading.value = false
            }
            
            console.log('班级学员:', classStudentsRes)
            
            // 获取选中班级中学员集合
            let students: any[] = []
            if (classStudentsRes && Array.isArray(classStudentsRes)) {
                // 遍历每个班级的学员数据
                classStudentsRes.forEach((classData: any) => {
                    if (classData.Students && Array.isArray(classData.Students)) {
                        students.push(...classData.Students)
                    }
                })
            }
            
            if (students.length === 0) {
                ElMessage.info('所选班级中没有学员')
                isDraftLoading.value = false
                return
            }
            console.log('students=======', students)
            // 班级中的学员去重
            students = students.filter((student: any, index: number, self: any) =>
                index === self.findIndex((t: any) => t.StudentID === student.StudentID)
            )
            console.log('students=======去重后', students)
            
            // 获取当前表格中已存在的学员数据
            const existingStudents = tableData.value.filter((row: any) => !row.isFooterRow && row.StudentID)
            const existingStudentIds = new Set(existingStudents.map((row: any) => row.StudentID))
            const selectedStudentIds = new Set(students.map((student: any) => student.StudentID))
            
            console.log('已存在的学员ID:', Array.from(existingStudentIds))
            console.log('班级中的学员ID:', Array.from(selectedStudentIds))
            
            // 1. 找出需要覆盖的学员（班级中有且表格中已存在的）
            const studentsToUpdate = students.filter((student: any) => existingStudentIds.has(student.StudentID))
            
            // 2. 找出需要新增的学员（班级中有但表格中没有的）
            let studentsToAdd = students.filter((student: any) => !existingStudentIds.has(student.StudentID));
            // 将学生手机号改为SmsTel
            studentsToAdd = studentsToAdd.map((student: any) => ({
                ...student,
                SmsTel: student.Phone,
            }))
            console.log('需要覆盖的学员数量:', studentsToUpdate.length)
            console.log('需要新增的学员数量:', studentsToAdd.length)
            
            // 检查新增数量限制
            const studentAvailableSlots = Math.min(remaining, studentsToAdd.length)
            if (studentsToAdd.length > remaining) {
                ElMessage.warning(`最多只能新增${remaining}行，已为您自动选择前${studentAvailableSlots}个新学员`)
            }
            
            // 执行新增操作
            if (studentsToAdd.length > 0) {
                // 根据选择的学员获取班级选项
                const classOptionsRes = await getClassOptionsForStudents(studentsToAdd.slice(0, studentAvailableSlots))
                console.log('classOptionsRes-------', classOptionsRes)
                
                // 检查学员成绩状态
                let checkStatusRes = null
                try {
                    checkStatusRes = await batchCheckScoreStatus({
                        examId: examInfo.value.Id,
                        campusId: campusIds,
                        students: classOptionsRes.map((co: any) => ({
                            studentId: co.StudentID,
                            classId: co.Classes && co.Classes.length ===1 ? co.Classes[0].ClassID : '' // 如果班级只有一个，则选择第一个班级, 多个时不选
                        }))
                    })
                } catch (error) {
                    // 抛出异常，不影响后续程序执行
                    console.log('error-------', error)
                }
                console.log('checkStatusRes-------', checkStatusRes)
                let studentStatuses = []
                if (checkStatusRes && checkStatusRes.ErrorCode == 200) {
                    const checkStatusData = checkStatusRes.Data
                    console.log('checkStatusData-------', checkStatusData)
                    studentStatuses = checkStatusData.studentStatuses;
                }

                // 为选中的学员创建表格行
                const newRows = createStudentRows(studentsToAdd.slice(0, studentAvailableSlots), classOptionsRes, undefined, studentStatuses)
            
                // 添加到表格数据
                tableData.value.push(...newRows)
                console.log(`已新增 ${studentAvailableSlots} 个学员`)
                
                // 排序表格数据
                sortTableData()
            }
            
            isDraftLoading.value = false

            // 显示确认消息框
            let message = ''
            if (studentAvailableSlots > 0) {
                message += `成功添加<span style="color: #67C23A; font-weight: bold;">${studentAvailableSlots}</span>名学员。<br/>`
            }
            if (studentsToUpdate.length > 0) {
            if (studentAvailableSlots > 0) {
                    message += `<span style="color: #F56C6C; font-weight: bold;">${studentsToUpdate.length}</span>名学员未添加。<br/>原因：学员已经在录入成绩列表啦。`
                } else {
                    message += `<span style="color: #F56C6C; font-weight: bold;">${studentsToUpdate.length}</span>名学员未添加。<br/>原因：学员已经在录入成绩列表啦。`
                }
            }
            if (studentAvailableSlots === 0 && studentsToUpdate.length === 0) {
                message = '没有可添加的学员'
            }
            
            ElMessageBox.alert(message, '录入结果', {
                confirmButtonText: '我知道了',
                customStyle: {
                    width: '300px'
                },
                dangerouslyUseHTMLString: true
            })
        }
    }).catch((error: any) => {
        console.log('取消选择班级')
    })
}

// 初始化表格数据
function initTableDataLocal() {
    // 使用 useTableData 的 initTableData
    initTableData({})
}

console.log('userCampuses', userCampuses)
    

// 处理学员选择新增
function handleSelectStudentsToAdd(groupKey: string) {
    const remaining = getRemainingCapacity()
    if (remaining <= 0) {
        ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，已达上限`)
        return
    }
    
    // 获取当前表格中已选中的学员数据（用于回显）
    const alreadySelectedStudents = tableData.value
        .filter((row: any) => !row.isFooterRow && row.StudentName) // 排除footer行和空行
        .map((row: any) => ({
            ID: row.StudentID, // 使用学员ID
            Name: row.StudentName,
        }))
    
    console.log('已选中的学员:', alreadySelectedStudents)

    // 打开学员选择弹窗
    chooseStudentRef.value?.open({
        multi: true, // 多选模式
        popTitle: '选择学员',
        showCampus: true, // 显示校区筛选
        isAllCampus: false, // 不显示所有校区，只显示适用校区
        campusMultiple: true, // 校区选择器支持多选
        required: false,
        showOneToOneStudent: true,
        // choosed: alreadySelectedStudents, // 回显已选中的学员
        condition: {
            status: "1", // 在读学员
            campusFlag: 1,
            CampusIds: campusIdsStr.value ? campusIdsStr.value.split(',') : '', // 多选时传入数组
            courseSelected: examInfo.value.Courses.map((course: any) => {
                return {
                    ID: course.Id,
                    Name: course.Name
                }
            }) || [],
            IsOnlyShowOneToOneStudent: 1
        }
    }).then(async (res: any) => {
        if (res && res.data) {
            console.log('选择学员返回数据:', res.data)
            isDraftLoading.value = true
            // 处理返回的学员数据
            let students = []
            if (Array.isArray(res.data)) {
                students = res.data
            } else {
                students = [res.data]
            }
            students = students.map((student: any) => ({
                ...student,
                StudentID: student.ID,
                StudentName: student.Name
            }))
            
            // 获取当前表格中已存在的学员数据
            const existingStudents = tableData.value.filter((row: any) => !row.isFooterRow && row.StudentID)
            const existingStudentIds = new Set(existingStudents.map((row: any) => row.StudentID))
            const selectedStudentIds = new Set(students.map((student: any) => student.StudentID))
            
            console.log('已存在的学员ID:', Array.from(existingStudentIds))
            console.log('选择的学员ID:', Array.from(selectedStudentIds))
            
            // 1. 找出需要覆盖的学员（选择中有且表格中已存在的）
            const studentsToUpdate = students.filter((student: any) => existingStudentIds.has(student.StudentID))
            
            // 2. 找出需要新增的学员（选择中有但表格中没有的）
            const studentsToAdd = students.filter((student: any) => !existingStudentIds.has(student.StudentID))
            
            console.log('需要覆盖的学员数量:', studentsToUpdate.length)
            console.log('需要新增的学员数量:', studentsToAdd.length)
            
            // 检查新增数量限制
            const studentAvailableSlots = Math.min(remaining, studentsToAdd.length)
            if (studentsToAdd.length > remaining) {
                ElMessage.warning(`最多只能新增${remaining}行，已为您自动选择前${studentAvailableSlots}个新学员`)
            }
            
            // 执行新增操作
            if (studentsToAdd.length > 0) {
                // 根据选择的学员获取班级选项
                const classOptionsRes = await getClassOptionsForStudents(studentsToAdd.slice(0, studentAvailableSlots))
                console.log('classOptionsRes-------', classOptionsRes)
                let checkStatusRes = null
                try {
                    checkStatusRes = await batchCheckScoreStatus({
                        examId: examInfo.value.Id,
                        campusId: campusIdsStr.value,
                        students: classOptionsRes.map((co: any) => ({
                            studentId: co.StudentID,
                            classId: co.Classes && co.Classes.length ===1 ? co.Classes[0].ClassID : '' // 如果班级只有一个，则选择第一个班级, 多个时不选
                        }))
                    })
                } catch (error) {
                    // 抛出异常，不影响后续程序执行
                    console.log('error-------', error)
                }
                console.log('checkStatusRes-------', checkStatusRes)
                let studentStatuses = []
                if (checkStatusRes && checkStatusRes.ErrorCode == 200) {
                    const checkStatusData = checkStatusRes.Data
                    console.log('checkStatusData-------', checkStatusData)
                    studentStatuses = checkStatusData.studentStatuses;
                }
                // 为选中的学员创建表格行
                const newRows = createStudentRows(studentsToAdd.slice(0, studentAvailableSlots), classOptionsRes, groupKey, studentStatuses)
                console.log('newRows-------', newRows)
                // 添加到表格数据
                tableData.value.push(...newRows)
                console.log(`已新增 ${studentAvailableSlots} 个学员`)
                // 排序表格数据
                sortTableData()
            }
            
            isDraftLoading.value = false
            
            // 显示确认消息框
            let message = ''
            if (studentAvailableSlots > 0) {
                message += `成功添加<span style="color: #67C23A; font-weight: bold;">${studentAvailableSlots}</span>名学员。<br/>`
            }
            if (studentsToUpdate.length > 0) {
            if (studentAvailableSlots > 0) {
                    message += `<span style="color: #F56C6C; font-weight: bold;">${studentsToUpdate.length}</span>名学员未添加。<br/>原因：学员已经在录入成绩列表啦。`
                } else {
                    message += `<span style="color: #F56C6C; font-weight: bold;">${studentsToUpdate.length}</span>名学员未添加。<br/>原因：学员已经在录入成绩列表啦。`
                }
            }
            if (studentAvailableSlots === 0 && studentsToUpdate.length === 0) {
                message = '没有可添加的学员'
            }
            
            ElMessageBox.alert(message, '录入结果', {
                confirmButtonText: '我知道了',
                customStyle: {
                    width: '300px'
                },
                dangerouslyUseHTMLString: true
            })
        }
    }).catch((error: any) => {
        console.log('取消选择学员')
    })
}

// 全屏切换
function toggleFullscreen() {
    isFullscreen.value = !isFullscreen.value
    console.log('全屏状态:', isFullscreen.value)
}

/**
 * 虚拟滚动回调
 * 优化：通过缓冲区和防抖减少频繁触发
 */
let lastLoggedIndex = -1
function scrolling({ startRowIndex: newStartRowIndex }: any) {
    startRowIndex.value = newStartRowIndex

    // 优化日志输出：只在索引变化超过10行时才打印，避免频繁输出
    if (Math.abs(newStartRowIndex - lastLoggedIndex) > 10) {
        console.log("🔄 虚拟滚动", newStartRowIndex)
        lastLoggedIndex = newStartRowIndex
    }

    onVirtualScrollDebounced()
}

// 轻量防抖实现，避免外部依赖
function debounce(fn: Function, wait = 300) {
    let timeout: ReturnType<typeof setTimeout> | null = null
    const debounced = function (this: any, ...args: any[]) {
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
    // 这里可以添加滚动时的额外逻辑，比如预加载数据等
    // 当前暂时不需要额外的处理
}, 300)

// ==================== 列定义 ====================

// 获取当前表格列
const getCurrentTableColumns = () => {
    const baseColumns = [
        {
            field: 'StudentName',
            key: 'StudentName',
            title: '学员姓名',
            renderHeaderCell: ({ column }: any) => renderHeaderWithStar(column.title, false),
            width: 180,
            edit: false,
            sortBy: '',
            align: 'left',
            ellipsis: {
                showTitle: true,
                lineClamp: 1,
            },
            renderBodyCell: ({ row }: any) => {
                const displayValue = row.StudentName ? String(row.StudentName) : ''
                const hasScore = row.HasScore || false
                
                // 构建提示文案
                let tooltipContent = ''
                if (row.isRepeat && hasScore) {
                    tooltipContent = '本次录入该学员有多条成绩数据，请检查。<br/>继续录入本次成绩将会覆盖原有成绩。'
                } else if (row.isRepeat) {
                    tooltipContent = '本次录入该学员有多条成绩数据，请检查。'
                } else if (hasScore) {
                    tooltipContent = '继续录入本次成绩将会覆盖原有成绩。'
                }
                
                return h('div', {
                    title: displayValue,
                    style: {
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        lineHeight: 'normal',
                        display: 'flex',
                        alignItems: 'center',
                        marginRight: '4px',
                        gap: '4px'
                    }
                }, [
                    h('span', {
                        style: {
                            minWidth: 0,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap'
                        }
                    }, displayValue),
                    (row.isRepeat || hasScore) ? h(
                        ElTooltip,
                        {
                            effect: 'light',
                            content: tooltipContent,
                            placement: 'top',
                            rawContent: true
                        },
                        {
                            default: () => h('span', {
                                style: {
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '18px',
                                    height: '18px',
                                    color: row.isRepeat ? '#F53F3F' : ''
                                }
                            }, [
                                h(ElIcon, { size: 14 }, () => [
                                    h('svg', { 'aria-hidden': 'true' }, [
                                        h('use', { 'xlink:href': '#w2-chongtu' })
                                    ])
                                ])
                            ])
                        }
                    ) : null
                ])
            }
        },
        {
            field: 'StudentSerial',
            key: 'StudentSerial',
            title: '学号',
            renderHeaderCell: ({ column }: any) => renderHeaderWithStar(column.title, false),
            width: 120,
            edit: false,
            sortBy: '',
            align: 'left',
            ellipsis: {
                showTitle: true,
                lineClamp: 1,
            },
            renderBodyCell: ({ row }: any) => {
                const displayValue = row.StudentSerial ? String(row.StudentSerial) : ''
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
        },
        {
            field: 'StudentPhone',
            key: 'StudentPhone',
            title: '手机号',
            renderHeaderCell: ({ column }: any) => renderHeaderWithStar(column.title, false),
            width: 120,
            edit: false,
            sortBy: '',
            align: 'left',
            ellipsis: {
                showTitle: true,
                lineClamp: 1,
            },
            renderBodyCell: ({ row }: any) => {
                const displayValue = row.StudentPhone ? String(row.StudentPhone) : ''
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
        },
        {
            field: 'ClassID',
            key: 'ClassID',
            title: '班级',
            renderHeaderCell: ({ column }: any) => renderHeaderWithStar(column.title, false, 6, '学员参与本次考试的所属班级，可用于分析学员本场考试的班级排名、平均分、最高分、最低分。'),
            width: 120,
            edit: true,
            sortBy: '',
            align: 'left',
            ellipsis: {
                showTitle: true,
                lineClamp: 1,
            },
            renderBodyCell: ({ row, column }: any) => {
                const displayValue = row.ClassName ? String(row.ClassName) : ''
                // 始终渲染选择器，不进入编辑状态
                return h(ElSelect, {
                    key: `${row.ID}_${row[column.field]}`,
                    modelValue: row.ClassID,
                    placeholder: '请选择班级',
                    clearable: true,
                    style: { width: 'calc(100% - 4px)', height: 'calc(100% - 4px)', margin: '2px' },
                    cellMode: true,
                    title: displayValue, // 设置 title 属性用于鼠标悬停显示
                    'onUpdate:modelValue': (val: any) => {
                        if (val) {
                            const selectedClass = row.classOptions.find((option: any) => option.value === val)
                            if (selectedClass) {
                                row.ClassID = val
                                row.ClassName = selectedClass.label
                            }
                        } else {
                            // 清空班级数据
                            row.ClassID = ''
                            row.ClassName = ''
                        }
                        recordFieldChange(row.ID, column.field, val, 'edit')
                    }
                }, {
                    default: () => (row.classOptions || []).map((option: any) => 
                        h(ElOption, {
                            key: option.value,
                            label: option.label,
                            value: option.value
                        })
                    )
                })
            }
        }
    ]

    // 根据考试项动态生成成绩列
    const scoreColumns: any[] = []
    if (examInfo.value.ExamItems && Array.isArray(examInfo.value.ExamItems)) {
        examInfo.value.ExamItems.forEach((examItem: any, index: number) => {
            const fieldName = `Score${index + 1}`
            
            // 根据考试项类型选择不同的渲染组件
            const isGradeType = examItem.ScoreTypeCode === 'TYPE_GRADE'
            const Component = isGradeType ? ElSelect : ElInput
            // 判断考试项的状态
            if(examItem.status){
                scoreColumns.push({
                    field: fieldName,
                    key: fieldName,
                    title: `${examItem.Name}`+ (examItem.UnitType ? `(${examItem.UnitType})` : ''),
                    renderHeaderCell: ({ column }: any) => renderHeaderWithStar(column.title, false),
                    width: 120,
                    edit: true,
                    sortBy: '',
                    align: 'left',
                    ellipsis: {
                        showTitle: true,
                        lineClamp: 1,
                    },
                    renderBodyCell: isGradeType ? ({ row, column }: any) => {
                        // 等级类型始终显示选择器，参考班级下拉的实现
                        const currentValue = row[fieldName]
                        const selectedOption = gradeOptions.value.find((option: any) => option.value === currentValue)
                        const displayValue = selectedOption ? selectedOption.label : ''
                        
                        return h(ElSelect, {
                            key: `${row.ID}_${row[fieldName]}`,
                            modelValue: row[fieldName],
                            placeholder: '请选择',
                            clearable: true,
                            style: { width: 'calc(100% - 4px)', height: 'calc(100% - 4px)', margin: '2px' },
                            cellMode: true,
                            title: displayValue, // 设置 title 属性用于鼠标悬停显示
                            'onUpdate:modelValue': (val: any) => {
                                row[fieldName] = val
                                recordFieldChange(row.ID, fieldName, val, 'edit')
                            }
                        }, {
                            default: () => gradeOptions.value.map((option: any) => 
                                h(ElOption, {
                                    key: option.value,
                                    label: option.label,
                                    value: option.value
                                })
                            )
                        })
                    } : createEditableCell(Component, {
                        placeholder: '请输入',
                        getDisplayValue: (row: any) => {
                            const value = row[fieldName]
                            return value !== null && value !== undefined ? String(value) : ''
                        },
                        onUpdateModelValue: (row: any, column: any, val: any) => {
                            row[fieldName] = val
                        }
                    })
                })
            }
        })
    }

    return [...baseColumns, ...scoreColumns]
}

// 列定义
const columns = computed(() => [
    indexColumn,
    ...getCurrentTableColumns(),
    {
        field: 'Memo',
        key: 'Memo',
        title: '备注',
        renderHeaderCell: ({ column }: any) => renderHeaderWithStar(column.title, false),
        width: 150,
        edit: true,
        sortBy: '',
        align: 'left',
        ellipsis: {
            showTitle: true,
            lineClamp: 1,
        },
        renderBodyCell: createEditableCell(ElInput, {
            placeholder: '请输入备注',
            maxlength: 200,
            showWordLimit: false,
            getDisplayValue: (row: any) => {
                const value = row.Memo
                return value !== null && value !== undefined ? String(value) : ''
            },
            onUpdateModelValue: (row: any, column: any, val: any) => {
                row.Memo = val
            }
        })
    }
])

// ==================== 表格配置选项 ====================

// 底部数据
const footerData = computed(() => {
    if (!isGrouped.value) {
        return [{
            ID: 'footer_row',
            isFooterRow: true
        }]
    }
    return []
})

// 编辑配置
const editOption = reactive({
    beforeStartCellEditing: ({ row, column }: any) => {
        // 分组行和footer行不允许编辑
        if (row.isGroupRow || row.isGroupFooter || row.isFooterRow) {
            return false
        }
        // 班级列不允许双击编辑（始终显示选择器）
        if (column.field === 'ClassID') {
            return false
        }
        // 等级类型考试项成绩列不允许双击编辑（始终显示选择器）
        if (column.field && column.field.startsWith('Score')) {
            const scoreIndex = parseInt(column.field.replace('Score', '')) - 1
            if (examInfo.value.ExamItems && examInfo.value.ExamItems[scoreIndex]) {
                const examItem = examInfo.value.ExamItems[scoreIndex]
                // 修改时间：2024-12-19 | 修改原因：添加考试项状态判断，提高代码健壮性，确保只处理有效的考试项
                if(examItem.status && examItem.ScoreTypeCode === 'TYPE_GRADE') {
                    return false
                }
            }
        }
        return true
    },
    beforeCellValueChange: ({ row, column, newValue }: any) => {
        // 可以在这里添加值变更前的验证
        return true
    },
    afterCellValueChange: ({ row, column, newValue, oldValue }: any) => {
        // 值变更后的处理
        console.log('单元格值变更:', { row: row.ID, column: column.field, newValue, oldValue })
    }
})

// 虚拟滚动配置
const virtualScrollOption = reactive({
    enable: false,  // 不开启虚拟滚动
    scrolling: scrolling,
    bufferScale: 1, // 缓冲区大小，数值越大，滚动时加载越多数据，性能越差
})

// 行样式配置
const rowStyleOption = reactive({
    clickHighlight: false,
    hoverHighlight: true // 启用悬浮高亮
})

// 单元格选择配置
const cellSelectionOption = reactive({
    enable: true
})

// 加载配置
const loadingOption = reactive({
    target: '#loading-container',
    name: 'wave'
})

// 单元格自动填充配置
const cellAutofillOption = reactive({
    directionX: false,
    directionY: false
})

// 事件自定义配置
const eventCustomOption = reactive({
    // body 列事件自定义
    bodyCellEvents: ({ row, column, rowIndx }: any) => {
        return {
            click: (event: any) => {
                if (column.field === '') {
                    clickGroupRow.value = true
                    return
                }
                clickGroupRow.value = false
                // 设置编辑状态
                cellDbClickKey.value = { rowId: row.ID, field: column.field }
            },
            contextmenu: (event: any) => { },
            mouseenter: (event: any) => {
                // 设置当前悬浮的行key
                hoveredRowKey.value = row.ID
            },
            mouseleave: (event: any) => {
                // 清除悬浮状态，但保持已勾选的状态
                hoveredRowKey.value = null
            },
        }
    },
    // footer 列事件自定义
    footerCellEvents: ({ row, column, rowIndex }: any) => {
        return {
            click: (event: any) => {
                clickGroupRow.value = true
                // footer点击事件处理
            },
        }
    }
})

// 单元格样式配置
const cellStyleOption = reactive({
    bodyCellClass: ({ row, column }: any) => {
        const classes = []
        
        // 操作列样式
        if (column.field === '') {
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
        
        return classes.join(' ')
    },
    headerCellClass: ({ column }: any) => {
        const classes = []
        
        // 表头样式
        classes.push('table-header-cell')
        
        // 操作列表头样式
        if (column.field === '') {
            classes.push('operation-column-header')
        }
        
        return classes.join(' ')
    },
    footerCellClass: ({ row }: any) => {
        const classes = []
        
        // 底部行样式
        if (row.isFooterRow) {
            classes.push('footer-row-cell')
        }
        
        return classes.join(' ')
    }
})

// 单元格合并配置
const cellSpanOption = reactive({
    enable: true,
    spanMethod: ({ row, column }: any) => {
        return { rowspan: 1, colspan: 1 }
    }
})

// 列隐藏配置
const columnHiddenOption = reactive({
    enable: true,
    defaultHiddenColumnKeys: []
})

// 列宽调整配置
const columnWidthResizeOption = reactive({
    enable: true,
    minWidth: 40,  // 调整为40px，允许序号列使用40px宽度
    maxWidth: 500,
    handleWidth: 2,
    handleColor: '#e4e7ed', // 拖拽手柄颜色
    handleHoverColor: '#409eff',
    onBeforeResize: ({ column, width }: any) => {
        return true
    },
    onAfterResize: ({ column, width }: any) => {
    }
})

// 剪贴板配置
const clipboardOption = reactive({
    enable: true,
    copyHeaders: true
})

// 使用OSS上传方式的文件导入函数
function handleImportLocalFileOSS() {
    let campusId = currentCampus.value.join(',')
    console.log('currentCampus.value', campusId)
    
    // 创建文件输入元素
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = '.xlsx,.xls,.csv'
    input.onchange = async (e: any) => {
        const file = e.target.files[0]
        console.log('file', file)
        if (file) {
            try {
                // 显示加载状态
                isDraftLoading.value = true
                // 重命名文件：考试名称_时间戳.扩展名（去除非法字符，限制长度）
                const ext = file.name && file.name.includes('.') ? file.name.substring(file.name.lastIndexOf('.')) : ''
                const safeExamName = String(examInfo.value.ExamName || 'score')
                    .replace(/[\\/:*?"<>|\s]+/g, '_')
                    .substring(0, 40)
                const newFileName = `${safeExamName}_${Date.now()}${ext}`
                const renamedFile = new File([file], newFileName, { type: file.type })

                // 使用OSS上传文件
                const uploadResponse = await uploadFile(renamedFile, { fileType: 1 }).catch((err:any)=>{
                    isDraftLoading.value = false
                    console.error('文件上传失败:', err)
                    return 
                })
                console.log('uploadResponse=====================', uploadResponse)
                if (uploadResponse && uploadResponse.url) {
                    // 文件上传成功后，调用解析接口
                    const parseResponse = await parseExcelScoreFileOSS({
                        examId: examInfo.value.Id,
                        campusId: campusId || '',
                        fileUrl: uploadResponse.url
                    } as any)

                    
                    console.log('解析结果:', parseResponse)
                    
                    if (parseResponse && parseResponse.ErrorCode === 200) {
                        // 处理解析结果，将数据添加到表格中
                        const parsedData = parseResponse.Data
                        parsedDataInfo.value = parsedData
                        
                        if (parsedData && parsedData.successRecords && Array.isArray(parsedData.successRecords)) {
                            // 将返回的驼峰形式的参数处理成首字母大写
                            const successRecords = parsedData.successRecords.map((pd: any) => {
                                return {
                                    ClassId: pd.classId,
                                    ClassList: pd.classList?.map((cls: any) => ({
                                        Status: cls.status,
                                        ClassID: cls.classId,
                                        ClassName: cls.className
                                    })),
                                    ClassName: pd.className,
                                    Memo: pd.memo,
                                    Scores: pd.scores?.map((score: any) => ({
                                        Score: score.score,
                                        ScoreGradeId: score.scoreGradeId,
                                        ScoreText: score.scoreText,
                                        SubjectID: score.subjectId,
                                        SubjectName: score.subjectName,
                                    })),
                                    Serial: pd.serial,
                                    SmsTel: pd.smsTel,
                                    StudentID: pd.studentId,
                                    StudentName: pd.studentName
                                }
                            })
                            console.log('成功导入的学员数据:', successRecords)
                            
                            // 检查新增数量限制
                            const remaining = getRemainingCapacity()
                            if (remaining <= 0) {
                                ElMessage.warning(`最多支持${MAX_TABLE_ROWS}条，已达上限`)
                                return
                            }
                            
                            // 检查新增数量限制
                            const studentAvailableSlots = Math.min(remaining, successRecords.length)
                            if (successRecords.length > remaining) {
                                ElMessage.warning(`最多只能新增${remaining}行，已为您自动选择前${studentAvailableSlots}个新学员`)
                            }
                            
                            // 执行新增操作
                            if (successRecords.length > 0) {
                                // 根据选择的学员获取班级选项
                                const classOptionsRes = await getClassOptionsForStudents(successRecords.slice(0, studentAvailableSlots))
                                
                                // 检查学员成绩状态
                                let checkStatusRes = null
                                try {
                                    checkStatusRes = await batchCheckScoreStatus({
                                        examId: examInfo.value.Id,
                                        campusId: campusId,
                                        students: classOptionsRes.map((co: any) => ({
                                            studentId: co.StudentID,
                                            classId: co.Classes && co.Classes.length ===1 ? co.Classes[0].ClassID : '' // 如果班级只有一个，则选择第一个班级, 多个时不选
                                        })),
                                    })
                                } catch (error) {
                                    // 抛出异常，不影响后续程序执行
                                    console.log('error-------', error)
                                }

                                let studentStatuses = []
                                if (checkStatusRes && checkStatusRes.ErrorCode == 200) {
                                    const checkStatusData = checkStatusRes.Data
                                    studentStatuses = checkStatusData.studentStatuses;
                                }
                                // 检查导入的学员与表格中已有学员是否有重复
                                const existingStudents = tableData.value
                                    .filter(row => !row.isGroupRow && !row.isGroupFooter && !row.isFooterRow && row.StudentID)
                                
                                // 为每个导入的学员添加重复标记
                                const studentsWithRepeatFlag = successRecords.slice(0, studentAvailableSlots).map((student: any, index: number) => {
                                    let isRepeat = false
                                    
                                    // 统一字段名：将 ClassId 转换为 ClassID
                                    const studentForCheck = {
                                        ...student,
                                        ClassID: student.ClassList && student.ClassList.length == 1 ? student.ClassList[0].ClassID : ''
                                    }
                                    
                                    // 检查是否与表格中已有学员重复（使用新的重复判断逻辑）
                                    const isExistingDuplicate = existingStudents.some((existingRow: any) => 
                                        isDuplicateStudent(studentForCheck, existingRow)
                                    )
                                    
                                    if (isExistingDuplicate) {
                                        // 如果与表格中已有学员有重复，标记为重复
                                        isRepeat = true
                                    } else {
                                        // 检查导入的 successRecords 本身是否有学员重复
                                        // 检查当前学员是否与之前已处理的学员重复
                                        for (let i = 0; i < index; i++) {
                                            const otherStudentForCheck = {
                                                ...successRecords[i],
                                                ClassID: successRecords[i].ClassId || successRecords[i].ClassID || ''
                                            }
                                            if (isDuplicateStudent(studentForCheck, otherStudentForCheck)) {
                                                isRepeat = true
                                                break
                                            }
                                        }
                                    }
                                    
                                    return {
                                        ...student,
                                        ClassID: studentForCheck.ClassID, // 统一字段名
                                        isRepeat: isRepeat
                                    }
                                })
                                
                                // 为选中的学员创建表格行
                                const newRows = createStudentRows(studentsWithRepeatFlag, classOptionsRes, undefined, studentStatuses)
                                // 处理导入的成绩数据
                                newRows.forEach((newRow: any, index: number) => {
                                    const originalStudent = studentsWithRepeatFlag[index]
                                    if (originalStudent && originalStudent.Scores) {
                                        // 将导入的成绩数据设置到对应的字段
                                        originalStudent.Scores.forEach((scoreData: any) => {
                                            // 根据SubjectID找到对应的考试项索引
                                            const examItemIndex = examInfo.value.ExamItems.findIndex((examItem: any) => examItem.Id === scoreData.SubjectID)
                                            if (examItemIndex !== -1) {
                                                const examItem = examInfo.value.ExamItems[examItemIndex]
                                                // 修改时间：2024-12-19 | 修改原因：添加考试项状态判断，避免导入无效考试项的数据
                                                if(examItem.status) {  // 只处理状态有效的考试项
                                                    const fieldName = `Score${examItemIndex + 1}`
                                                    // 根据考试项类型设置不同的值
                                                    if (examItem.ScoreTypeCode === 'TYPE_GRADE') {
                                                        newRow[fieldName] = scoreData.ScoreGradeId || ''
                                                    } else if (examItem.ScoreTypeCode === 'TYPE_TEXT') {
                                                        newRow[fieldName] = scoreData.ScoreText || ''
                                                    } else {
                                                        newRow[fieldName] = scoreData.Score || ''
                                                    }
                                                }
                                            }
                                        })
                                    }
                                    // 设置备注
                                    if (originalStudent && originalStudent.Memo) {
                                        newRow.Memo = originalStudent.Memo
                                    }
                                })
                                
                                // 添加到表格数据
                                tableData.value.push(...newRows)
                                console.log(`已新增 ${studentAvailableSlots} 个学员`)

                                // 排序表格数据
                                sortTableData()
                                console.log('tableData.value=====================', tableData.value)
                                
                                
                                
                                
                            }
                            importResultDialogVisible.value = true
                            // 处理失败原因统计
                            const failReasonMap = new Map()
                            parsedData.failRecords.forEach((item: any) => {
                                const failReason = item.failReason || '未知错误'
                                if (failReasonMap.has(failReason)) {
                                    const existingData = failReasonMap.get(failReason)
                                    existingData.count += 1
                                    existingData.rowNumbers.push(item.rowNumber)
                                    failReasonMap.set(failReason, existingData)
                                } else {
                                    failReasonMap.set(failReason, {
                                        count: 1,
                                        rowNumbers: [item.rowNumber]
                                    })
                                }
                            })
                            console.log('failReasonMap-------', failReasonMap)
                            
                            // 转换为表格数据格式
                            importResultTableData.value = Array.from(failReasonMap.entries()).map(([reason, data]) => ({
                                failReason: reason,
                                failCount: data.count,
                                rowNumbers: data.rowNumbers.join('、')
                            }))
                            console.log('importResultTableData.value-------', importResultTableData.value)
                        } 
                        isDraftLoading.value = false

                    } else {
                        ElMessage.error(parseResponse?.ErrorMsg || '文件解析失败')
                        isDraftLoading.value = false

                    }
                } else {
                    ElMessage.error('文件上传失败');
                    isDraftLoading.value = false

                }
                
                
            } catch (error) {
                console.error('文件处理失败:', error)
                ElMessage.error('文件处理失败，请稍后重试')
                isDraftLoading.value = false
            } finally {
                isDraftLoading.value = false
            }
        }
    }
    input.click()
}

// 下载表格模板
async function handleDownloadTemplate() {
    const downloadUrl = `${apiUrl}/api/exam/scores/template?examId=${examInfo.value.Id}`
    downloadFile(downloadUrl, '成绩导入模版_'+examInfo.value.ExamName+'.xlsx')
    
}

// 删除
function handleDelete() {
    // 获取已勾选的行
    const checkedRowsList = getCheckedRows()
    
    // 如果没有选择任何行，提示用户
    if (checkedRowsList.length === 0) {
        ElMessage.warning('请先选择要删除的数据行')
        return
    }
    
    // 有选择的行，二次确认删除
    ElMessageBox.confirm(
        `确定要删除选中的 ${checkedRowsList.length} 行数据吗？删除后无法恢复。`, 
        '删除确认', 
        {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        // 执行删除操作
        checkedRowsList.forEach(row => {
            const index = tableData.value.findIndex(item => item.ID === row.ID)
            if (index > -1) {
                tableData.value.splice(index, 1)
            }
        })
        
        // 清空选择状态
        clearCheckedRows()
        
        ElMessage.success(`已删除 ${checkedRowsList.length} 行数据`)
    }).catch(() => {
        // 取消删除
    })
}

// 保存全部 - 确定录入
function handleSaveAll() {
    // 检查是否有数据
    const dataRows = tableData.value.filter(row => !row.isGroupRow && !row.isGroupFooter && !row.isFooterRow)
    if (dataRows.length === 0) {
        ElMessage.warning('当前没有任何成绩数据，请先添加学员后再录入成绩')
        return
    }

    // 检查是否有重复学员(要对比班级id,学员id完全一致的才是重复学员)
    // 规则：学员ID一致时要判断班级ID，班级ID一致是重复学员，班级ID不一致则不是重复学员，班级ID为空时只根据学员ID判断
    const duplicateRows: any[] = []
    
    // 检查每一行是否与其他行重复
    dataRows.forEach((row: any, index: number) => {
        // 检查是否与其他行重复
        for (let i = 0; i < dataRows.length; i++) {
            if (i !== index && isDuplicateStudent(row, dataRows[i])) {
                // 如果找到重复，检查是否是第一个出现的
                const firstIndex = dataRows.findIndex((r: any) => isDuplicateStudent(r, row))
                if (firstIndex !== index) {
                    // 记录重复的行（只记录一次）
                    if (!duplicateRows.some(dr => dr.ID === row.ID)) {
                        duplicateRows.push(row)
                    }
                    break
                }
            }
        }
    })
    
    if (duplicateRows.length > 0) {
        // 有重复学员
        ElMessageBox.alert(
            `本次录入${dataRows.length}名学员<br/>存在${duplicateRows.length}条重复成绩数据（班级和学员完全一致），请检查后再录入。`,
            '录入结果',
            {
                confirmButtonText: '我知道了',
                customStyle: {
                    width: '300px'
                },
                dangerouslyUseHTMLString: true
            }
        )
        return
    }

    // 验证必填字段
    const validationResult = validateScoreData(dataRows)
    
    if (!validationResult.isValid) {
        // 显示验证错误
        showValidationErrors(validationResult.errors)
        return
    }

    // 直接执行录入逻辑，不需要二次确认
    performScoreInput(dataRows)
}

// 验证成绩数据
function validateScoreData(rows: any[]) {
    const errors: any[] = []
    
    // 检查是否有考试项配置
    if (!examInfo.value.ExamItems || !Array.isArray(examInfo.value.ExamItems) || examInfo.value.ExamItems.length === 0) {
        ElMessage.error('考试项目配置异常，无法进行成绩录入')
        return {
            isValid: false,
            errors: [{
                rowIndex: 0,
                rowDisplay: '系统错误',
                rowId: '',
                studentName: '',
                className: '',
                errors: ['考试项目配置异常'],
                errorFields: []
            }],
            totalRows: 0
        }
    }
    
    // 先清除所有行的错误状态
    rows.forEach(row => {
        row.errorField = []
    })
    
    rows.forEach((row, index) => {
        const rowErrors: string[] = []
        const errorFields: string[] = []
        
        // 验证必填字段
        const requiredFields = [
            { field: 'StudentName', name: '学员姓名' },
            // { field: 'ClassID', name: '班级' }  // 班级必填注释掉
        ]
        
        // 检查基础必填字段
        requiredFields.forEach(({ field, name }) => {
            const value = row[field]
            if (!value || value === '') {
                rowErrors.push(`${name}不能为空`)
                errorFields.push(field)
            }
        })
        
        // 成绩字段非必填，只进行格式校验
        
        // 验证成绩格式（如果输入了成绩）
        // 修改时间：2024-12-19 | 修改原因：添加考试项状态判断，避免验证无效考试项的字段，防止误报错
        if (examInfo.value.ExamItems && Array.isArray(examInfo.value.ExamItems)) {
            examInfo.value.ExamItems.forEach((examItem: any, index: number) => {
                if(examItem.status) {  // 只验证状态有效的考试项
                    const fieldName = `Score${index + 1}`
                    const score = row[fieldName]
                    
                    if (score !== null && score !== undefined && score !== '') {
                        // 等级类型不进行格式验证
                        if (examItem.ScoreTypeCode !== 'TYPE_GRADE' && examItem.ScoreTypeCode !== 'TYPE_TEXT') {
                            // 验证成绩格式（使用正则表达式）
                            const scoreStr = String(score).trim()
                            const scoreRegex = /(?:^\d{1,8}\.\d{0,2}$)|(?:^\d{0,8}$)/
                            
                            if (!scoreRegex.test(scoreStr)) {
                                rowErrors.push(`${examItem.Name || `考试项目${index + 1}`}成绩格式不正确`)
                                errorFields.push(fieldName)
                            }
                        }
                    }
                }
            })
        }
        
        // 设置错误字段到行数据中
        if (errorFields.length > 0) {
            row.errorField = [...new Set(errorFields)] // 去重
        } else {
            // 如果没有错误字段，确保清除 errorField
            row.errorField = []
        }
        
        // 如果有错误，记录到总错误列表
        if (rowErrors.length > 0) {
            errors.push({
                rowIndex: index + 1,
                rowDisplay: `第${index + 1}行`,
                rowId: row.ID,
                studentName: row.StudentName || '未知学员',
                className: row.ClassName || '未知班级',
                errors: rowErrors,
                errorFields: errorFields
            })
        }
    })
    
    return {
        isValid: errors.length === 0,
        errors,
        totalRows: rows.length
    }
}

// 显示验证错误
function showValidationErrors(errors: any[]) {
    // 设置验证错误状态
    validationErrors.value = errors
    validationErrorRowIds.value = errors.map((error: any) => error.rowId)
    
    // 构建错误字段映射
    const errorFieldsMap = new Map()
    errors.forEach(error => {
        if (error.errorFields && error.errorFields.length > 0) {
            errorFieldsMap.set(error.rowId, error.errorFields)
        }
    })
    validationErrorFields.value = errorFieldsMap
    
    // 显示验证错误弹窗
    validationErrorDialogVisible.value = true
}

// 处理验证错误弹窗可见性变化
function handleValidationDialogVisibilityChange(visible: boolean) {
    validationErrorDialogVisible.value = visible
}

// 处理从验证错误弹窗前往错误行
function handleGoToRowFromDialog(error: any) {
    if (!error || !error.rowId) {
        console.warn('错误信息中缺少rowId:', error)
        return
    }
    
    // 关闭弹窗
    validationErrorDialogVisible.value = false
    
    // 等待弹窗关闭动画完成后再滚动
    setTimeout(async () => {
        // 查找错误行
        const errorRowIndex = tableData.value.findIndex(row => row.ID === error.rowId)
        if (errorRowIndex > -1) {
            // 滚动到错误行
            if (tableRef.value && typeof tableRef.value.scrollToRowKey === 'function') {
                await tableRef.value.scrollToRowKey({
                    rowKey: error.rowId,
                    behavior: 'smooth'
                })
            }
            
            // 高亮错误行（可选）
            console.log(`已滚动到错误行: ${error.rowDisplay}`)
        } else {
            console.warn(`未找到错误行: ${error.rowId}`)
        }
    }, 300)
}

// 过滤掉已经成功的录入结果数据
const filterTableData = () => {
    // tableData.value排序 规则：既是重复学员又是重复录入>重复学员>重复录入>其他学员
    let failedStudentIds = inputResultInfo.value.failedStudents?.map((item: any) => item.studentId);

    tableData.value = tableData.value.filter((item: any) => failedStudentIds.includes(item.StudentID))
    if (tableData.value.length > 0) {
        sortTableData()
    }
}
// 关闭失败学员弹窗
function handleCloseFailedStudentsDialog() {
    // 全部录入成功
    if (inputResultInfo.value.failCount == 0) {
        failedStudentsDialogVisible.value = false
        failedStudentsTableDataVisible.value = false
        failedStudentsTableData.value = []
        close()
        _resolve()
        return
    }
    // 有失败的
    if (inputResultInfo.value.failCount > 0) {
        // 关闭失败学员弹窗
        failedStudentsDialogVisible.value = false;
        // 过滤掉已经成功的录入结果数据
        filterTableData()

        // 部分录入成功
        if ((inputResultInfo.value.totalCount - (inputResultInfo.value.failCount || 0)) > 0) {
            _resolve()
        }
    }
    
}
// 执行成绩录入
function performScoreInput(rows: any[]) {
    loading.value = true
    
    try {
        // 准备录入数据
        const students = rows.map(row => {
            const scores: any[] = []
            
            // 添加成绩字段
            // 修改时间：2024-12-19 | 修改原因：添加考试项状态判断，确保不提交无效考试项的数据，只处理状态有效的考试项
            if (examInfo.value.ExamItems && Array.isArray(examInfo.value.ExamItems)) {
                examInfo.value.ExamItems.forEach((examItem: any, index: number) => {
                    if(examItem.status) {  // 只提交状态有效的考试项
                        const fieldName = `Score${index + 1}`
                        const score = row[fieldName]
                        scores.push({
                            subjectId: examItem.Id,
                            score: String(score).trim(),
                            type: examItem.ScoreTypeCode
                        })
                    }
                })
            }
            
            return {
                studentId: row.StudentID,
                classId: row.ClassID,
                memo: row.Memo || '',
                scores: scores
            }
        })
        
        // 准备请求参数
        const requestData = {
            examId: examInfo.value.Id,
            campusId: currentCampus.value.join(','),
            students: students
        }
        
        console.log('准备录入的成绩数据:', requestData)
        
        // TODO: 调用API录入成绩
        // 这里需要根据实际的API接口来实现
        batchSaveScore(requestData).then((res: any) => {
            console.log('res', res)
            if(res.ErrorCode == 200){
                loading.value = false
                // 更新录入结果信息
                inputResultInfo.value = {
                    successCount: res.Data.successCount || 0,
                    failCount: res.Data.failCount || 0,
                    totalCount: res.Data.totalCount || 0,
                    failedStudents: res.Data.failedStudents || []
                }
                
                
                // 处理失败学员数据
                if(res.Data && res.Data.failedStudents && res.Data.failedStudents.length > 0){
                    // 填充失败学员表格数据
                    failedStudentsTableData.value = res.Data.failedStudents.map((student: any) => ({
                        studentName: student.studentName || '-',
                        failReason: student.failReason || '未知错误'
                    }))
                } 
                // else {
                //     ElMessage.success('录入成功')
                //     close()
                //     _resolve()
                // }
                // 显示失败学员弹窗
                failedStudentsDialogVisible.value = true
            }else{
                ElMessage.error(res.ErrorMsg || '录入失败')
            }
        }).finally(() => {
            loading.value = false
        })
        
      
        
    } catch (error) {
        loading.value = false
        console.error('成绩录入失败:', error)
        // ElMessage.error('成绩录入失败，请重试')
    }
}

// 格式化
const formatArrayItem = (items: string[], joinType = '，') => {
    if (!items || !Array.isArray(items) || items.length === 0) return '-';
    let itemNames = items.map((item: any) => item.Name)
    // 这里需要根据ID获取对应的名称，暂时显示ID
    return itemNames.join(joinType)
}
// 格式化考试项
const formatExamItems = (items: string[]) => {
    if (!items || !Array.isArray(items) || items.length === 0) return '-';
    let itemNames = items.map((item: any) => item.Name)
    // 这里需要根据ID获取对应的名称，暂时显示ID
    return itemNames.join('、')
}

// 关闭对话框
function close() {
    // 清除表格数据
    tableData.value = []
    
    // 清除选中状态
    clearCheckedRows()
    
    // 清除编辑状态
    cellDbClickKey.value = null
    
    // 清除悬浮状态
    hoveredRowKey.value = null
    
    // 清除分组状态
    expandedGroups.value.clear()
    groupedDataCache.value = null
    
    // 关闭对话框
    isDraftLoading.value = false
    failedStudentsDialogVisible.value = false
    failedStudentsTableData.value = []
    inputResultInfo.value = {
        totalCount: 0,
        successCount: 0,
        failCount: 0,
        failedStudents: []
    }
    loading.value = false
    dialogVisible.value = false
}

// 获取考试分数等级字典值
const loadExamScoreGradeDict = () => {
    
    queryDictionaryList({
        Type: 'EXAM_SCORE_GRADE'
    }).then((res: any) => {
        if (res.ErrorCode === 200 && res.Data) {
            gradeOptions.value = res.Data?.List?.map((item: any) => {
                return {
                    ...item,
                    label: item.Value,
                    value: item.ID
                }
            })
        } else {
            gradeOptions.value = []
        }
    })
   
}
// 加载考试详情
async function loadExamDetail(examId: string) {
    if (!examId) return

    loading.value = true
    try {
        const res = await getExamDetail({ id: examId })
        if (res.ErrorCode === 200 && res.Data) {
            const detail = res.Data
            examInfo.value = detail
        } else {
            ElMessage.error(res.ErrorMsg || '加载考试详情失败')
            close()
        }
    } catch (error) {
        ElMessage.error('加载考试详情失败，请重试')
        close()
    } finally {
        loading.value = false
    }
}

let _resolve: any = null, _reject: any = null

// 对外暴露的open方法
function open(examData: any) {
    return new Promise((resolve, reject) => {
        _resolve = resolve
        _reject = reject
       
        dialogVisible.value = true
        // 每次打开时重置全屏状态
        isFullscreen.value = false
        loadExamDetail(examData.id)
        // 获取等级固定字典值
        loadExamScoreGradeDict()
    })
}

onMounted(()=>{
    window.addEventListener('error', (err)=>{
        console.error('error', err)
    })
})

// 组件销毁时清理状态
onUnmounted(() => {
    // 清理加载实例（如果有的话）
    if (loadingInstance.value && typeof (loadingInstance.value as any).close === 'function') {
        (loadingInstance.value as any).close()
    }
    // 重置状态
    isFullscreen.value = false
    dialogVisible.value = false
})

// 其他方法 ================================end
defineExpose({
    open
})
</script>
   
<style lang="scss">
.inputScoreForm {
    height: 75vh !important;
    max-height: 75vh !important;
    
    .wtwo-dialog__header {
        background: #fff!important;
        display: flex;
        align-items: center;
        height: 48px;
        padding: 0 20px;

        .dialog-title {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
        }
    }

    .wtwo-dialog__headerbtn {
        color: #606266 !important;
    }

    .wtwo-dialog__close {
        color: #606266 !important;
    }
    
    .wtwo-dialog__body{
        padding: 12px 20px 20px;
        height: calc(100% - 100px);
        display: flex;
        flex-direction: column;
    }
    
    .dialog-body-wrap {
        height: 100%;
        overflow: hidden;
    }
    
    .dialog-body-wrap {
        .info-banner {
            width: 100%;
            background: #EAF3FF;
            border-radius: 2px;
            height: 40px;
            display: flex;
            align-items: center;
            color: #606266;
            font-size: 13px;
            padding: 0 16px;
            margin-bottom: 16px;
            
            .info-icon {
                width: 20px;
                height: 20px;
                color: #1890ff;
                margin-right: 8px;
                font-size: 16px;
            }
            
            span {
                color: #606266;
            }
        }
        
        .exam-info {
            margin-bottom: 16px;
            background-color: #F9FAFC;
            padding: 8px 16px;
            border-radius: 8px;
            
            .info-grid {
                display: grid;
                grid-template-columns: 1fr 1fr 1fr;
                
                .info-item {
                    height: 30px;
                    display: flex;
                    align-items: center;
                    
                    &.info-item-full {
                        grid-column: span 2;
                    }
                    
                    .label {
                        font-weight: 400;
                        font-size: 14px;
                        color: #909399;
                        min-width: 70px;
                        line-height: 22px;
                    }
                    
                    .value {
                        font-weight: 400;
                        font-size: 14px;
                        line-height: 22px;
                        color: #303133;
                        width: calc(296px - 80px);
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }
                }
            }
        }
        
        .search-section {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            margin-bottom: 10px;
            flex-shrink: 0;
            
            .action-buttons {
                display: flex;
            }
        }
        
    }
    .wtwo-dialog__footer{
       background: #fff!important;
    }
    // 全屏样式
    &.is-fullscreen {
        height: 100vh !important;
        max-height: 100vh !important;
        .wtwo-dialog {
            margin: 0 !important;
            max-height: 100vh;
            height: 100vh;
        }
        
        .wtwo-dialog__header {
            display: none !important;
            height: 0 !important;
            padding: 0 !important;
            margin: 0 !important;
            border: none !important;
        }
        
        .wtwo-dialog__headerbtn{
            display: none !important;
        }
        
        .wtwo-dialog__body {
            height: calc(100vh - 52px) !important;
            overflow-y: auto;
            padding: 0 !important;
            margin-top: 0 !important;
        }
        
        .dialog-body-wrap {
            height: 100%;
            padding: 12px 16px 20px;
        }
    }
}

// 添加菜单样式
.popover-menu {
    .menu-item {
        padding: 10px 8px;
        border-radius: 6px;
        cursor: pointer;
        font-weight: 400;
        font-size: 14px;
        color: #606266;
        
        &:hover {
            background: rgba(0, 0, 0, 0.04);
        }
        
        span {
            font-size: 14px;
            color: #303133;
        }
    }
}

.required-asterisk {
    color: #f56c6c;
    margin-left: 2px;
}
// 针对添加行的特殊样式
.inputScoreForm  {
    .fan-table-footer-td {
        border-bottom: 1px solid #EBEEF5 !important;
    }
    
    .fan-table.fan-table-border-around .fan-table-container {
       height: 100% !important;
       .fan-table-footer {
        .fan-table-footer-tr {
            &:hover {
                background-color: #fff !important;
                .fan-table-footer-td {
                    background-color: #fff !important;
                }
                
            }
        }
       }

    }
    
}
.wtwo-modal-dialog {
    .import-result-dialog {
        .wtwo-dialog__header {
            background-color: #fff;
            color: var(--1303133, #303133);
            .wtwo-dialog__title {
                color: var(--1303133, #303133);
                font-size: 16px;
                font-style: normal;
                font-weight: 500;
            }
            .wtwo-dialog__close {
                color: var(--1303133, #303133);
            }
        }
        .wtwo-dialog__body {
            padding: 20px;

            .import-result-content {
                max-height: 500px;
                font-size: 14px;
                font-style: normal;
                font-weight: 400;
                color: var(--2606266, #606266);
                .import-result-success {
                    margin-bottom: 10px;
                    .count {
                        display: inline-block;
                        width: 36px;
                        color: var(--67-c-23-a, #00B42A);
                    }
                }
                .import-result-fail {
                    margin-bottom: 10px;
                    .view-reason {
                        cursor: pointer;
                        display: inline-flex;
                        align-items: center;
                        color: var(--3909399, #909399);
                        font-size: 12px;
                        margin-left: 4px;

                        .wtwo-icon {
                            margin-left: 4px;
                        }
                    }
                    .count {
                        display: inline-block;
                        width: 36px;
                        color: var(--f-56-c-6-c, #F53F3F);

                    }
                }
            }
        }
    }
}
</style>
<style lang="scss" scoped>
@use './scss/inputScoreForm.scss' as *;
</style>
