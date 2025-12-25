import { h, Ref } from "vue"
import { ElTooltip, ElIcon } from 'element-plus'
import { useDictFieldsStore } from '@/store/dict'
/**
 * 渲染表头单元格（可选带红色星号）
 * @param {string} title - 列标题
 * @param {boolean} [required=true] - 是否必填（显示红色星号）
 * @param {number} [marginLeft=2] - 星号左边距
 * @returns {VNode} 渲染后的表头
 */
export const renderHeaderWithStar = (title: string, required: boolean = true, marginLeft: number = 6, tooltip?: string) => {
    const elements = []
    let width = 0;

    if (required) {
        width += 20
        elements.push(h('span', {
            style: `color: red; display: inline-block;margin-left: ${marginLeft}px;`
        }, '*'))
    }


    if (tooltip) {
        width += 20

        elements.push(h(ElTooltip, {
            content: tooltip,
            placement: 'top',
        }, {
            default: () => h(ElIcon, {
                style: {
                    fontSize: '16px',
                    color: '#909399',
                    marginLeft: '4px'
                }
            }, () => h('svg', {
                'aria-hidden': 'true'
            }, [h('use', {
                'xlink:href': '#w2-xinxitishi'
            })]))
        }))
    }
    elements.unshift(h('span', {
        style: {
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
            maxWidth:`calc(100% - ${width}px)`,
        }
    }, title))

    return h('div', {
        class: 'wtwotable-header-cell',
        style: {
            display: 'flex',
            alignItems: 'center'
        }
    }, elements)
}


/**
 * 清理关联字段的通用函数
 * @param {object} row - 行数据
 * @param {string} field - 被清空的字段名
 * @param {number} tableType - 表格类型
 */
export const clearRelatedFields = (row:any, field:any, tableType:any) => {
    switch (field) {
        case 'CampusID':
            // 清空校区相关字段
            row.CampusName = ''
            break
        case 'ClassID':
            // 清空班级相关字段
            row.ClassName = ''
            // 班级变化会影响课程、科目等
            break
        case 'StudentUserID':
            // 清空学员相关字段
            row.StudentUserName = ''
            break
        case 'ShiftID':
            // 清空课程相关字段
            row.ShiftName = ''
            row.IsOneToOne = 0
            // 只有预约课才需要清空人数字段
            if (tableType === 30) {
                row.MaxStudentCount = ''
                row.StartStudentCount = ''
            }
            break
        case 'SubjectID':
            // 清空科目相关字段
            row.SubjectName = ''
            break
        case 'ClassRoomID':
            // 清空教室相关字段
            row.ClassRoomName = ''
            break
        case 'MainTeacherID':
            // 清空教师相关字段
            row.MainTeacherName = ''
            row.MainTeacherList = []
            break
        case 'AssistantTeacherID':
            // 清空助教相关字段
            row.AssistantTeacherName = ''
            row.AssistantTeacherList = []
            break
    }
}

// 科目数据管理
const dictFieldsStore = useDictFieldsStore()

// 根据科目值获取科目名称
export const getSubjectName = (subjectValue:any) => {
    if (!subjectValue) return ''
    const subjectList = dictFieldsStore.dictFields('SUBJECT', 1)
    const subject = subjectList.find(item => (item.Value === subjectValue||item.ID === subjectValue))
    return subject ? subject.Name : subjectValue
}

/**
 * 计算选择范围内的单元格数量
 * @param {Object} selectionRangeIndexes - 选择范围索引
 * @returns {number} 单元格数量
 */
export const calculateCellCount = (selectionRangeIndexes: any) => {
    if (!selectionRangeIndexes) return 0
    
    const { startRowIndex, endRowIndex, startColIndex, endColIndex } = selectionRangeIndexes
    
    // 计算行数和列数
    const rowCount = endRowIndex - startRowIndex + 1
    const colCount = endColIndex - startColIndex + 1
    
    // 总单元格数量
    const totalCells = rowCount * colCount
    
    return totalCells
}

/**
 * 统一获取可见列（用于复制/剪切/删除/填充等需要与表格可见列索引对齐的场景）
 * - 会根据 hiddenKeys 过滤被隐藏列
 * - 支持额外排除某些列（如预检查提示列）
 */
export function getVisibleColumns(
    allColumns: Array<{ field: string }>,
    hiddenKeys: string[] = [],
    exclude: string[] = []
) {
    const excludeSet = new Set(exclude)
    const hiddenSet = new Set(hiddenKeys)
    // 保留序号列 field === '' 以保证与实际可见列索引一致
    return (allColumns || []).filter(col => !hiddenSet.has(col.field) && !excludeSet.has(col.field))
}

/**
 * 获取用于剪贴板相关操作的可见列（默认排除预检查提示列）
 */
export function getClipboardVisibleColumns(
    allColumns: Ref<Array<{ field: string }>>,
    hiddenKeys: string[] = []
) {
    return getVisibleColumns(allColumns.value, hiddenKeys, ['preCheckStatus'])
}

/**
 * 统一处理科目字段变更（支持粘贴/自动填充等场景）
 * - 非全科课程行（EnableSubject != '1'）清空 SubjectID/SubjectName
 * - 全科课程行（EnableSubject == '1'）按入参写入 ID/Name（传了哪个写哪个）
 * - 每次变更都会调用 record 记录来源
 */
export function applySubjectChange(
    row: any,
    payload: { id?: any; name?: any },
    isEnableSubject: boolean,
    record: (rowId: string, fieldName: string, value: any, source: string) => void,
    source: string
) {
    if (!row) return
    if (!isEnableSubject) {
        if (row.SubjectID || row.SubjectName) {
            row.SubjectID = ''
            row.SubjectName = ''
            record(row.ID, 'SubjectID', '', source || 'auto_clear')
            record(row.ID, 'SubjectName', '', source || 'auto_clear')
        }
        return
    }
    if (payload && Object.prototype.hasOwnProperty.call(payload, 'id')) {
        row.SubjectID = payload.id
        record(row.ID, 'SubjectID', payload.id, source || 'edit')
    }
    if (payload && Object.prototype.hasOwnProperty.call(payload, 'name')) {
        row.SubjectName = payload.name
        record(row.ID, 'SubjectName', payload.name, source || 'edit')
    }
}
