import { computed } from 'vue'
import { useConfigs } from '@/store'
import { transToConfigDescript } from '@/utils/filters/filters'

/**
 * 必填字段校验相关逻辑
 */
export function useValidation() {
    // 获取配置项
    const configs = computed(() => {
        return useConfigs().configs
    })

    // 是否开启限制跨科目选择老师 0允许（默认） 1不允许
    const EnableMustSameSubjectTeacherCourse = computed(() => {
        return configs.value.EnableMustSameSubjectTeacherCourse == 1
    })

    // shouldValidateRow 已被 shouldValidateRowWithType 取代

    // 新增：根据表格类型决定参与校验的关键字段（不导出，内部使用）
    function shouldValidateRowWithType(row: any, tableType?: number) {
        const baseKeyFields = [
            'CampusID', 'ShiftID', 'Date',
            'StartTime', 'EndTime', 'ClassRoomID', 
            'MainTeacherID', 'AssistantTeacherID', 'SubjectID'
        ]
        // 10=班级排课；20=学员排课；30=预约课（不包含班级/学员对象字段）
        if (tableType === 10) {
            baseKeyFields.push('ClassID')
        } else if (tableType === 20) {
            baseKeyFields.push('StudentUserID')
        } else {
            // 30 或其他：不添加对象字段
        }
        return baseKeyFields.some(field => {
            const value = row[field]
            return value !== null && value !== undefined && value !== ''
        })
    }

    /**
     * 校验必填字段
     * @param {Array} rows - 需要校验的行数据 (tableData 原始数据)
     * @param {Array} displayRows - 当前显示的数据 (displayTableData，可能包含分组)
     * @param {boolean} isGrouped - 是否为分组模式
     * @param {string} groupByField - 分组字段名
     * @param {number} tableType - 表格类型，20表示给学员排课，其它表示给班级排课
     * @param {boolean} courseTeacherRequired - 新增：任课老师是否必填的配置项
     * @returns {Object} 校验结果 { isValid: boolean, errors: Array, totalRows: number }
     */
    function validateRequiredFields(rows: any[], displayRows: any[] = [], isGrouped: boolean = false, groupByField: string = '', tableType?: number, courseTeacherRequired: boolean = false) {
        const errors: any[] = []
        
        // 过滤出需要参与校验的行
        const validationRows = rows.filter(row => 
            !row.isGroupRow && !row.isGroupFooter && shouldValidateRowWithType(row, tableType)
        )
        
        console.log(`总行数: ${rows.length}, 参与校验行数: ${validationRows.length}`)
        
        // 遍历所有数据行（非分组行）获取实际行号
        const dataRows = rows.filter(row => !row.isGroupRow && !row.isGroupFooter)
        
        validationRows.forEach((row) => {
            const rowErrors: string[] = []
            const errorFields: string[] = [] // 记录错误字段名
            
            // 计算行号显示信息
            let rowDisplay: string
            let actualRowIndex: number
            
            if (isGrouped && groupByField && displayRows.length > 0) {
                // 分组模式：计算在显示视图中的行号
                let groupValue
                
                // 对于特定字段，需要特殊处理
                if (groupByField === 'CourseType' || groupByField === 'IsSubscribeCourse') {
                    // 对于数值型字段，只有 null 和 undefined 才归类为未分组
                    groupValue = (row[groupByField] === null || row[groupByField] === undefined) ? '未分组' : String(row[groupByField])
                } else {
                    // 对于其他字段，空值、null、undefined 都归类为"未分组"
                    groupValue = row[groupByField] || '未分组'
                }
                
                // 从显示数据中找到对应的分组行，获取已经格式化好的分组名称
                const groupRow = displayRows.find(displayRow => 
                    displayRow.isGroupRow && displayRow.groupKey === groupValue
                )
                
                const groupDisplayName = groupRow && groupRow.groupName ? groupRow.groupName : (groupValue === '未分组' ? '未分组' : groupValue)
                
                // 在显示数据中找到该行的实际位置（排除分组行和footer行）
                const visibleDataRows = displayRows.filter(r => !r.isGroupRow && !r.isGroupFooter)
                const displayRowIndex = visibleDataRows.findIndex(r => r.ID === row.ID) + 1
                
                if (displayRowIndex > 0) {
                    // 计算在分组内的序号（用于显示）
                    const sameGroupRows = rows.filter(r => {
                        if (!r.isGroupRow && !r.isGroupFooter && shouldValidateRowWithType(r, tableType)) {
                            let itemGroupValue
                            if (groupByField === 'CourseType' || groupByField === 'IsSubscribeCourse') {
                                itemGroupValue = (r[groupByField] === null || r[groupByField] === undefined) ? '未分组' : String(r[groupByField])
                            } else {
                                itemGroupValue = r[groupByField] || '未分组'
                            }
                            return itemGroupValue === groupValue
                        }
                        return false
                    })
                    const groupRowIndex = sameGroupRows.findIndex(r => r.ID === row.ID) + 1
                    
                    // 显示格式：分组名 + 分组内序号
                    rowDisplay = `${groupDisplayName} 第${groupRowIndex}行`
                    actualRowIndex = groupRowIndex
                } else {
                    // 降级处理：使用原始数据中的位置
                    const globalRowIndex = dataRows.findIndex(dataRow => dataRow.ID === row.ID) + 1
                    rowDisplay = `第${globalRowIndex}行`
                    actualRowIndex = globalRowIndex
                }
            } else {
                // 非分组模式：使用全局行号
                if (displayRows.length > 0) {
                    const visibleDataRows = displayRows.filter(row => !row.isGroupRow && !row.isGroupFooter)
                    actualRowIndex = visibleDataRows.findIndex(dataRow => dataRow.ID === row.ID) + 1
                } else {
                    actualRowIndex = dataRows.findIndex(dataRow => dataRow.ID === row.ID) + 1
                }
                rowDisplay = `第${actualRowIndex}行`
            }
            
            // 基础必填字段（教室为条件必填）
            const requiredFields = [
                { field: 'CampusID', name: '上课校区' },
                { field: 'ShiftID', name: '上课课程' },
                { field: 'Date', name: '上课日期' },
            ]

            // 10=班级排课；20=学员排课；30=预约课（无对象字段）
            if (tableType === 10) {
                requiredFields.splice(1, 0, { field: 'ClassID', name: transToConfigDescript('上课班级') })
            } else if (tableType === 20) {
                requiredFields.splice(1, 0, { field: 'StudentUserID', name: '上课学员' })
            } else {
                // 30：不插入对象字段
            }
            
            // 线下课需要教室；线上课不需要
            if (row.CourseType != '2') {
                requiredFields.push({ field: 'ClassRoomID', name: '上课教室' })
            }
            
            // 检查基础必填字段
            requiredFields.forEach(({ field, name }) => {
                const value = row[field]
                if (!value || value === '') {
                    rowErrors.push(`${name}不能为空`)
                    errorFields.push(field) // 记录错误字段
                }
            })
            
            // 特殊处理：时间字段（StartTime + EndTime = timeRange）
            const startTimeEmpty = !row.StartTime || row.StartTime === ''
            const endTimeEmpty = !row.EndTime || row.EndTime === ''
            if (startTimeEmpty || endTimeEmpty) {
                rowErrors.push('上课时间不能为空')
                errorFields.push('timeRange') // 错误标识到timeRange列
            }
            
            // 条件必填：上课科目（根据 EnableSubject 判断）
            if (row.EnableSubject === '1' || row.EnableSubject === 1) {
                if (!row.SubjectID || row.SubjectID === '') {
                    rowErrors.push('上课科目不能为空')
                    errorFields.push('SubjectID') // 记录错误字段
                }
            } else {
                console.log(`第${actualRowIndex}行 EnableSubject=${row.EnableSubject}，科目非必填`)
            }
            
            // 条件必填：任课老师（根据配置项判断）
            if (courseTeacherRequired) {
                if (!row.MainTeacherID || row.MainTeacherID === '') {
                    rowErrors.push(transToConfigDescript('任课老师')+'不能为空')
                    errorFields.push('MainTeacherID') // 记录错误字段
                }
            }
            
            // 🆕 条件必填：可约人数和开课人数（预约课 + 开放预约为是）
            if (tableType === 30 && row.IsSubscribeCourse === '1') {
                if (!row.MaxStudentCount || row.MaxStudentCount === '' || row.MaxStudentCount === '0') {
                    rowErrors.push('可约人数不能为空')
                    errorFields.push('MaxStudentCount')
                }
                if (!row.StartStudentCount || row.StartStudentCount === '' || row.StartStudentCount === '0') {
                    rowErrors.push('开课人数不能为空')
                    errorFields.push('StartStudentCount')
                }
            }
            
            // 如果有错误，记录到总错误列表
            if (rowErrors.length > 0) {
                errors.push({
                    rowIndex: actualRowIndex,
                    rowDisplay: rowDisplay, // 新增：格式化的行号显示
                    rowId: row.ID,
                    campusName: row.CampusName || '未知校区',
                    className: row.ClassName || transToConfigDescript('未知班级'),
                    date: row.Date || '未知日期',
                    errors: rowErrors,
                    errorFields: errorFields // 添加错误字段列表
                })
            }
        })
        
        return {
            isValid: errors.length === 0,
            errors,
            totalRows: validationRows.length,
            errorRowIds: errors.map(error => error.rowId) // 返回错误行的ID列表
        }
    }

    /**
     * 重新计算现有验证错误的显示信息（行号、分组名等）
     * 用于分组状态变化时更新错误信息显示
     */
    function recalculateValidationErrors(existingErrors: any[], rows: any[], displayRows: any[] = [], isGrouped: boolean = false, groupByField: string = '', tableType?: number) {
        if (!existingErrors || existingErrors.length === 0) {
            return []
        }

        return existingErrors.map(error => {
            // 找到对应的数据行
            const targetRow = rows.find(row => row.ID === error.rowId)
            if (!targetRow) {
                console.warn('重新计算验证错误时未找到对应行:', error.rowId)
                return error // 保持原错误信息
            }

            // 重新计算行号显示信息
            let rowDisplay: string
            let actualRowIndex: number

            if (isGrouped && groupByField && displayRows.length > 0) {
                // 分组模式：重新计算在显示视图中的行号
                let groupValue
                
                // 对于特定字段，需要特殊处理
                if (groupByField === 'CourseType' || groupByField === 'IsSubscribeCourse') {
                    groupValue = (targetRow[groupByField] === null || targetRow[groupByField] === undefined) ? '未分组' : String(targetRow[groupByField])
                } else {
                    groupValue = targetRow[groupByField] || '未分组'
                }
                
                // 从显示数据中找到对应的分组行，获取已经格式化好的分组名称
                const groupRow = displayRows.find(displayRow => 
                    displayRow.isGroupRow && displayRow.groupKey === groupValue
                )
                
                const groupDisplayName = groupRow && groupRow.groupName ? groupRow.groupName : (groupValue === '未分组' ? '未分组' : groupValue)
                
                // 计算在当前分组内的行号
                // 1. 找到同一分组的所有数据行（从原始数据中过滤）
                const sameGroupRows = rows.filter(r => {
                    if (!r.isGroupRow && !r.isGroupFooter && shouldValidateRowWithType(r, tableType)) {
                        let itemGroupValue
                        if (groupByField === 'CourseType' || groupByField === 'IsSubscribeCourse') {
                            itemGroupValue = (r[groupByField] === null || r[groupByField] === undefined) ? '未分组' : String(r[groupByField])
                        } else {
                            itemGroupValue = r[groupByField] || '未分组'
                        }
                        return itemGroupValue === groupValue
                    }
                    return false
                })
                
                // 2. 计算该行在分组内的序号
                const indexInGroup = sameGroupRows.findIndex(r => r.ID === targetRow.ID) + 1
                
                if (indexInGroup > 0) {
                    rowDisplay = `${groupDisplayName} 第${indexInGroup}行`
                    actualRowIndex = indexInGroup
                } else {
                    // 如果在分组中找不到，使用原始索引作为后备
                    const originalIndex = rows.findIndex(r => r.ID === targetRow.ID) + 1
                    rowDisplay = `${groupDisplayName} 第${originalIndex}行`
                    actualRowIndex = originalIndex
                }
            } else {
                // 非分组模式：使用原始表格的行号
                actualRowIndex = rows.findIndex(r => r.ID === targetRow.ID) + 1
                rowDisplay = `第${actualRowIndex}行`
            }

            // 返回更新后的错误信息
            return {
                ...error,
                rowIndex: actualRowIndex,
                rowDisplay: rowDisplay
            }
        })
    }

    return {
        EnableMustSameSubjectTeacherCourse,
        shouldValidateRowWithType,
        validateRequiredFields,
        recalculateValidationErrors
    }
} 