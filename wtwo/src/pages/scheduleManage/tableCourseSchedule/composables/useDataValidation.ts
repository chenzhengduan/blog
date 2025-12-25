import { reactive } from 'vue'
import { isUUID } from '@/utils/uuid/uuid'
// 组件类型配置 - 用于统一处理焦点获取
export const fieldMap:any = reactive({
    'CampusID': 'CampusName',
    'ClassID': 'ClassName',
    'StudentUserID': 'StudentUserName',
    'ShiftID': 'ShiftName',
    'SubjectID': 'SubjectName',
    'Date': 'Date',
    'StartTime': 'StartTime',
    'EndTime': 'EndTime',
    'ClassRoomID': 'ClassRoomName',
    'MainTeacherID': 'MainTeacherName',
    'AssistantTeacherID': 'AssistantTeacherName',
    'CourseType': 'CourseType',
    'IsSubscribeCourse': 'IsSubscribeCourse',
    'InternalRemark': 'InternalRemark',
    'Describe': 'Describe',
    'MaxStudentCount': 'MaxStudentCount',
    'StartStudentCount': 'StartStudentCount',
})

// 各个字段的数据格式校验对象函数
export const fieldFormatCheck = {
    'CampusID': (value:any) => {
        // 判断是否string类型
        return typeof value === 'string' && value
    },
    'ClassID': (value:any) => {
        return typeof value === 'string' && value
    },
    'StudentUserID': (value:any) => {
        return typeof value === 'string' && value
    },
    'ShiftID': (value:any) => {
        return typeof value === 'string' && value
    },
    'SubjectID': (value:any) => {
        return typeof value === 'string' && value
    },
    'ClassRoomID': (value:any) => {
        return typeof value === 'string' && value
    },
    'MainTeacherID': (value:any) => {
        return typeof value === 'string' && value
    },
    'MainTeacherIDList': (value:any) => {
        return typeof value === 'string' && value
    },
    'CourseType': (value:any) => {
        if ([2, '2', '是'].includes(value)) {
            return 2
        }
        if ([1, '1', '否'].includes(value)) {
            return 1
        }
        return false
    },
    'IsSubscribeCourse': (value:any) => {
        if ([1, '1', '是'].includes(value)) {
            return 1
        }
        if ([0, '0', '否'].includes(value)) {
            return 0
        }
        return false
    },
    'InternalRemark': (value:any) => {
        return typeof value === 'string' && value
    },
    'Describe': (value:any) => {
        return typeof value === 'string' && value
    },
    'Date': (value:any) => {
        // 判断值是否符合时间格式
        return /^\d{4}-\d{2}-\d{2}$/.test(value) && value
    },
    'StartTime': (value:any) => {
        return /^\d{2}:\d{2}:\d{2}$/.test(value) && value
    },
    'EndTime': (value:any) => {
        return /^\d{2}:\d{2}:\d{2}$/.test(value) && value
    },
    'timeRange': (value:any) => {
        // 时间范围字段，允许空值或包含~的格式
        return !value || (typeof value === 'string' && value.includes('~'))
    },
    'AssistantTeacherID': (value:any) => {
        return typeof value === 'string' && value
    },
    // 名称字段的验证（允许空值）
    'CampusName': (value:any) => {
        return typeof value === 'string'
    },
    'ClassName': (value:any) => {
        return typeof value === 'string'
    },
    'StudentUserName': (value:any) => {
        return typeof value === 'string'
    },
    'ShiftName': (value:any) => {
        return typeof value === 'string'
    },
    'SubjectName': (value:any) => {
        return typeof value === 'string'
    },
    'ClassRoomName': (value:any) => {
        return typeof value === 'string'
    },
    'MainTeacherName': (value:any) => {
        return typeof value === 'string'
    },
    'AssistantTeacherName': (value:any) => {
        return typeof value === 'string'
    },
    'MaxStudentCount': (value:any) => {
        // 允许空值或大于等于0的数字/数字字符串
        return value === '' || (!isNaN(Number(value)) && Number(value) >= 0)
    },
    'StartStudentCount': (value:any) => {
        // 允许空值或大于等于0的数字/数字字符串
        return value === '' || (!isNaN(Number(value)) && Number(value) >= 0)
    }
}

export /**
* 验证ID字段值是否符合UUID规则
* @param {string} fieldName - 字段名
* @param {any} value - 字段值
* @returns {any} 验证后的值
*/
const validateIDField = (fieldName:any, value:any) => {
   // 定义ID字段列表
   const idFields = ['CampusID', 'ClassID', 'ShiftID', 'SubjectID', 'ClassRoomID', 'MainTeacherID', 'AssistantTeacherID', 'StudentUserID']

   // 如果不是ID字段，直接返回原值
   if (!idFields.includes(fieldName)) {
       return value
   }

   // 如果没有值，返回原值
   if (!value) {
       return value
   }

   // 助教字段：逗号分隔的字符串
   if (fieldName === 'AssistantTeacherID') {
       if (typeof value === 'string') {
           // 分割字符串，验证每个ID，返回有效ID的逗号分隔字符串
           const ids = value.split(',').map(id => id.trim()).filter(Boolean)
           const validUUIDs = ids.filter(id => isUUID(id))
           return validUUIDs.join(',')
       } else {
           return ''
       }
   } else {
       // 单个ID字段
       if (!isUUID(value)) {
           return null
       }
       return value
   }
}

/**
 * 检测草稿数据中的非法字段（只有ID没有Name的字段）
 * @param {object} draft - 草稿数据
 * @returns {Array} 非法字段名数组
 */
export const detectInvalidFields = (draft:any) => {
    const errorFields:any[] = []

    // 定义需要检测的ID字段和对应的Name字段映射
    const idNameMapping = {
        'CampusID': 'CampusName',
        'ClassID': 'ClassName',
        'StudentUserID': 'StudentUserName',
        'ShiftID': 'ShiftName',
        'SubjectID': 'SubjectName',
        'ClassRoomID': 'ClassRoomName',
        'MainTeacherID': 'MainTeacherName',
        'AssistantTeacherID': 'AssistantTeacherName',
    }

    // 遍历每个ID字段，检查是否有对应的Name值
    Object.entries(idNameMapping).forEach(([idField, nameField]) => {
        const idValue = draft[idField]
        const nameValue = draft[nameField]

        // 如果ID字段有值，但Name字段为空或无效，则标记为非法
        if (idValue && idValue !== '') {
            // 对于助教字段（逗号分隔字符串）
            if (idField === 'AssistantTeacherID') {
                if (typeof idValue === 'string' && idValue.trim() !== '' && (!nameValue || nameValue.trim() === '')) {
                    errorFields.push(idField)
                }
            } else {
                // 对于单个ID字段：检测字段不存在、值为空字符串、或为默认UUID
                if (nameValue === undefined || nameValue === null || nameValue === '') {
                    errorFields.push(idField)
                }
            }
        }
    })

    return errorFields
}