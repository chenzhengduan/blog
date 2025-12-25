import { reactive, Ref, ref } from 'vue'
import { generateUUID, isUUID } from '@/utils/uuid/uuid'
import { fieldMap } from './useDataValidation'

export function useTableData(options:{
    isGrouped:Ref<boolean>,
    groupedDataCache:Ref<any>,
    selectedTableType:Ref<number>
}) {
    // 表格数据
    const tableData = ref<any[]>([])
    // 原始数据备份（用于无排序时恢复）
    const originalTableData = ref<any[]>([])
    // 提交数据
    const submitData = ref<any[]>([])

    const getNewRow = (overrides: any = {}) => {
        const baseRow:any = {
            ID: generateUUID(),
            CampusID: null,
            ShiftID: null,
            SubjectID: null,
            Date: null,
            timeRange: '',
            StartTime: null,
            EndTime: null,
            ClassRoomID: null,
            MainTeacherID: '',
            MainTeacherName: '',
            MainTeacherList: [],
            AssistantTeacherID: '',
            AssistantTeacherName: '',
            AssistantTeacherList: [],
            CourseType: '1',
            IsSubscribeCourse: '0',
            InternalRemark: '',
            Describe: '',
            IsOneToOne: null, // 新增：默认为0
            CampusName: '',
            ShiftName: '',
            SubjectName: '',
            ClassRoomName: '',
            errorField: [],
            errorMessages: {},
            IsNew: true, // 标记为新行
        }

        const tableType = options.selectedTableType.value

        if (tableType === 10) { // 给班级排课
            baseRow.ClassID = null
            baseRow.ClassName = ''
        } else if (tableType === 20) { // 给学员排课
            baseRow.StudentUserID = null
            baseRow.StudentUserName = ''
        } else if (tableType === 30) { // 排预约课
            baseRow.IsSubscribeCourse = '1'
            baseRow.MaxStudentCount = null
            baseRow.StartStudentCount = null
        }

        return {
            ...baseRow,
            ...overrides
        }
    }


    /**
     * 初始化表格数据
     * @param recordNewRow 记录数据改变
     */
    const initTableData = (recordNewRow: any) => {
        // 创建一条默认的空数据行
        const defaultRow = getNewRow()

        tableData.value = [defaultRow]
        // 保存原始数据备份
        originalTableData.value = JSON.parse(JSON.stringify([defaultRow]))

        // 🆕 记录初始化的空行到变更收集器
        recordNewRow(defaultRow.ID, {...defaultRow,IsNew:true}, 'init')
    }

    /**
     * 单元格数据变化处理
     */
    const cellDataChange = (row: any, column: any, cellValue: any, recordFieldChange: any) => {
        // 1. 更新 tableData 中对应的数据
        const rowIndex = tableData.value.findIndex(item => item.ID === row.ID)
        if (rowIndex !== -1) {
            // 更新字段值
            tableData.value[rowIndex][column.field] = cellValue
            // 添加 isEdit 字段标记该行已被编辑
            tableData.value[rowIndex].isEdit = true

            // 同时更新原始数据备份
            const originalRowIndex = originalTableData.value.findIndex(item => item.ID === row.ID)
            if (originalRowIndex !== -1) {
                originalTableData.value[originalRowIndex][column.field] = cellValue

                // 同时更新名称快照字段
                const nameField = fieldMap[column.field]
                if (nameField && row[nameField]) {
                    originalTableData.value[originalRowIndex][nameField] = row[nameField]
                }
            }
        }

        // 2. 更新 submitData（保持原有逻辑）
        const currentCell = submitData.value.find(
            (x) => x.ID === row.ID && x.field === column.field,
        )

        if (currentCell) {
            currentCell.value = cellValue
        } else {
            const newCell = {
                ID: row.ID,
                field: column.field,
                value: cellValue,
            }
            submitData.value.push(newCell)
        }

        // 3. 记录变更到草稿保存队列
        if (column.field && column.field !== '') {
            recordFieldChange(row.ID, column.field, cellValue, 'edit')
        }
    }


    /**
 * 处理新增10行
 * @param {string} groupKey - 可选参数，如果提供则表示在指定分组中新增
 */
    const handleAddTenRows = (groupKey: any = null, {
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
        studentUserDataMap
    }: any) => {
        if (groupKey) {
            // 添加10行新数据到表格末尾
            for (let i = 0; i < 10; i++) {
                const newRow = getNewRow({
                    [groupByField.value]: groupKey, // 自动设置分组字段
                })
                // 根据分组字段设置默认值和显示名称
                if (groupByField.value === 'CampusID') {
                    newRow.CampusID = groupKey
                    newRow.CampusName = getCampusName(groupKey) || groupKey
                } else if (groupByField.value === 'ClassID') {
                    newRow.ClassID = groupKey
                    // 需要从缓存中获取班级名称
                    const classData = classDataMap.value.get(groupKey)
                    newRow.ClassName = classData ? classData.Name : groupKey
                }else if (groupByField.value === 'StudentUserID') {
                    newRow.StudentUserID = groupKey
                    newRow.StudentUserName = studentUserDataMap.value.get(groupKey)?.Name || groupKey
                } else if (groupByField.value === 'ShiftID') {
                    newRow.ShiftID = groupKey
                    // 需要从缓存中获取课程名称
                    const courseData = courseDataMap.value.get(groupKey)
                    newRow.ShiftName = courseData ? courseData.Name : groupKey
                } else if (groupByField.value === 'SubjectID') {
                    newRow.SubjectID = groupKey
                    newRow.SubjectName = getSubjectName(groupKey) || groupKey
                } else if (groupByField.value === 'Date') {
                    newRow.Date = groupKey
                } else if (groupByField.value === 'StartTime') {
                    newRow.StartTime = groupKey
                } else if (groupByField.value === 'ClassRoomID') {
                    newRow.ClassRoomID = groupKey
                    // 需要从缓存中获取教室名称
                    const classroomData = classroomDataMap.value.get(groupKey)
                    newRow.ClassRoomName = classroomData ? classroomData.Name : groupKey
                } else if (groupByField.value === 'MainTeacherID') {
                    newRow.MainTeacherID = groupKey
                    // 需要从缓存中获取教师名称
                    const teacherData = teacherDataMap.value.get(groupKey)
                    newRow.MainTeacherName = teacherData ? teacherData.name : groupKey

                    // 同步更新传输字段
                    if (groupKey) {
                        newRow.MainTeacherList = [{ ID: groupKey, Name: newRow.MainTeacherName,TeacherCommissionList:newRow.TeacherCommissionList }]
                    } else {
                        newRow.MainTeacherList = []
                    }
                } else if (groupByField.value === 'AssistantTeacherID') {
                    // 处理助教分组：支持逗号分隔的字符串
                    if (Array.isArray(groupKey)) {
                        // 如果是数组，转换为逗号分隔字符串
                        newRow.AssistantTeacherID = groupKey.join(',')
                        const assistantNames = groupKey.map(id => {
                            const assistantData = assistantDataMap.value.get(id)
                            return assistantData ? assistantData.name : id
                        })
                        newRow.AssistantTeacherName = assistantNames.join(', ')
                        
                        // 同步更新传输字段
                        newRow.AssistantTeacherList = groupKey.map(id => {
                            const assistantData = assistantDataMap.value.get(id)
                            return {
                                ID: id,
                                Name: assistantData ? assistantData.name : id
                            }
                        })
                    } else {
                        // 单个助教
                        newRow.AssistantTeacherID = groupKey
                        const assistantData = assistantDataMap.value.get(groupKey)
                        newRow.AssistantTeacherName = assistantData ? assistantData.name : groupKey
                        
                        // 同步更新传输字段
                        newRow.AssistantTeacherList = [{
                            ID: groupKey,
                            Name: assistantData ? assistantData.name : groupKey
                        }]
                    }
                } else if (groupByField.value === 'CourseType') {
                    newRow.CourseType = groupKey === '2' ? '2' : '1'
                } else if (groupByField.value === 'IsSubscribeCourse') {
                    newRow.IsSubscribeCourse = groupKey === '1' ? '1' : '0'
                }
                newRow.IsNew=true;
                tableData.value.push(newRow)
                // 同时更新原始数据备份
                originalTableData.value.push(JSON.parse(JSON.stringify(newRow)))

                // 记录新增行到变更收集器
                recordNewRow(newRow.ID, {...newRow,IsNew:true}, 'addTen')
            }

            // 如果分组未展开，自动展开
            if (!expandedGroups.value.has(groupKey)) {
                expandedGroups.value.add(groupKey)
            }

            // 重新计算分组（按分组规则归纳到分组下面）
            if (groupedDataCache.value) {
                groupedDataCache.value.isValid = false
            }
        } else {
            // 添加10行新数据到表格末尾
            for (let i = 0; i < 10; i++) {
                const newRow = getNewRow()
                tableData.value.push(newRow)
                // 同时更新原始数据备份
                originalTableData.value.push(JSON.parse(JSON.stringify(newRow)))
                // 记录新增行到变更收集器
                recordNewRow(newRow.ID, {...newRow,IsNew:true}, 'addTen')
            }
            // 如果分组模式下，需要重新计算分组
            if (isGrouped.value && groupedDataCache.value) {
                groupedDataCache.value.isValid = false
            }
        }
    }

    // 排序配置
const sortOption = reactive({
    // 关闭sortAlways，支持三种状态：无排序 → 升序 → 降序 → 无排序
    sortAlways: false,
    sortChange: (params:any) => {

        // 如果分组模式下进行排序，需要重新计算分组
        if (options.isGrouped.value) {
            console.log('分组模式下进行排序，重新计算分组')

            // 先对原始数据进行排序
            sortChangeCustom(params)

            // 然后重新计算分组
            if (options.groupedDataCache.value) {
                options.groupedDataCache.value.isValid = false
            }

            console.log('分组重新计算完成')
        } else {
            // 非分组模式下正常排序
            sortChangeCustom(params)
        }
    },
})

/**
 * 排序改变事件
 */
const sortChangeCustom =(params:any)=> {
    console.log('排序参数:', params)

    // 根据知识库文档，params 是一个对象，包含每个字段的排序规则
    // 例如：{ ClassName: 'asc', course: 'desc' }
    // 当某个字段没有排序规则时，params中不会包含该字段

    // 检查是否有任何排序规则
    const hasSortRules = Object.keys(params).length > 0 &&
        Object.values(params).some(order => order === 'asc' || order === 'desc')

    if (!hasSortRules) {
        // 恢复到原始数据状态，但保留所有用户编辑的字段
        const currentData = JSON.parse(JSON.stringify(tableData.value))
        tableData.value = JSON.parse(JSON.stringify(originalTableData.value))

        // 将当前数据的所有字段合并到恢复的数据中
        currentData.forEach((currentRow:any) => {
            const originalRow = tableData.value.find(row => row.ID === currentRow.ID)
            if (originalRow) {
                // 合并所有字段（除了 ID）
                Object.keys(currentRow).forEach(key => {
                    if (key !== 'ID') {
                        originalRow[key] = currentRow[key]
                    }
                })
            }
        })

        return
    }

    // 对原始数据进行排序
    tableData.value.sort((a, b) => {
        // 遍历所有排序字段
        for (const [field, order] of Object.entries(params)) {
            if (!order || (order !== 'asc' && order !== 'desc')) continue // 跳过没有排序规则的字段

            let aValue = a[field]
            let bValue = b[field]

            // 处理空值
            if (aValue === null || aValue === undefined) aValue = ''
            if (bValue === null || bValue === undefined) bValue = ''

            // 字符串比较
            if (typeof aValue === 'string' && typeof bValue === 'string') {
                if (order === 'asc') {
                    const result = aValue.localeCompare(bValue)
                    if (result !== 0) return result
                } else if (order === 'desc') {
                    const result = bValue.localeCompare(aValue)
                    if (result !== 0) return result
                }
            }

            // 数字比较
            if (typeof aValue === 'number' && typeof bValue === 'number') {
                if (order === 'asc') {
                    const result = aValue - bValue
                    if (result !== 0) return result
                } else if (order === 'desc') {
                    const result = bValue - aValue
                    if (result !== 0) return result
                }
            }

            // 日期比较（字符串格式的日期）
            if (typeof aValue === 'string' && typeof bValue === 'string' &&
                /^\d{4}-\d{2}-\d{2}$/.test(aValue) && /^\d{4}-\d{2}-\d{2}$/.test(bValue)) {
                const aDate = new Date(aValue)
                const bDate = new Date(bValue)
                if (order === 'asc') {
                    const result = aDate.getTime() - bDate.getTime()
                    if (result !== 0) return result
                } else if (order === 'desc') {
                    const result = bDate.getTime() - aDate.getTime()
                    if (result !== 0) return result
                }
            }

            // 布尔值比较
            if (typeof aValue === 'boolean' && typeof bValue === 'boolean') {
                if (order === 'asc') {
                    const result = (aValue === bValue) ? 0 : (aValue ? 1 : -1)
                    if (result !== 0) return result
                } else if (order === 'desc') {
                    const result = (aValue === bValue) ? 0 : (bValue ? 1 : -1)
                    if (result !== 0) return result
                }
            }

            // 默认字符串比较
            const aStr = String(aValue)
            const bStr = String(bValue)
            if (order === 'asc') {
                const result = aStr.localeCompare(bStr)
                if (result !== 0) return result
            } else if (order === 'desc') {
                const result = bStr.localeCompare(aStr)
                if (result !== 0) return result
            }
        }

        return 0 // 如果所有字段都相等，返回0
    })

    console.log(`排序完成，排序规则:`, params)
}

    return {
        tableData,
        originalTableData,
        submitData,
        getNewRow,
        initTableData,
        cellDataChange,
        handleAddTenRows,
        sortOption,
    }
}