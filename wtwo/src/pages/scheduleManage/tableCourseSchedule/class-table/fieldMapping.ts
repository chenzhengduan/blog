/**
 * 表格排课字段映射配置
 * 完全按照 TableCourseClass 接口的字段名
 */

import { transToConfigDescript } from "@/utils/filters/filters"
import { ref } from "vue"

// 字段类型定义（基于 TableCourseClass 接口）
export const FIELD_TYPES = {
  CampusID: 'select',
  ClassID: 'select', 
  ShiftID: 'select',
  SubjectID: 'select',
  Date: 'date',
  StartTime: 'time',
  EndTime: 'time',
  MainTeacherID: 'select',
  AssistantTeacherID: 'select',
  ClassRoomID: 'select',
  CourseMethod: 'number',
  CourseType: 'select', // 1=线上课，2=线下课
  IsSubscribeCourse: 'select', // 0=否，1=是
  InternalRemark: 'string',
  Describe: 'string',
  StudentUserID: 'string'
} as const

// 字段显示名称（中文描述）
export const FIELD_DISPLAY_NAMES = {
  CampusID: '上课校区',
  ClassID: transToConfigDescript('上课班级'),
  ShiftID: '上课课程',
  SubjectID: '上课科目',
  Date: '上课日期',
  StartTime: '开始时间',
  EndTime: '结束时间',
  MainTeacherID: transToConfigDescript('任课老师'),
  AssistantTeacherID: '助教',
  ClassRoomID: '上课教室',
  CourseMethod: '排课方式',
  CourseType: '线上课',
  IsSubscribeCourse: '开放预约',
  InternalRemark: '对内备注',
  Describe: '对外备注',
  StudentUserID: '上课学员'
} as const

// 按表格类型返回分组选项
export function getGroupFieldOptionsByTableType(tableType?: number) {
  const base = [
    { label: getFieldDisplayName('CampusID'), value: 'CampusID' },
    { label: getFieldDisplayName('ShiftID'), value: 'ShiftID' },
    { label: getFieldDisplayName('Date') , value: 'Date' },
    { label: getFieldDisplayName('ClassRoomID'), value: 'ClassRoomID' },
    { label: getFieldDisplayName('MainTeacherID'), value: 'MainTeacherID' },
    { label: getFieldDisplayName('CourseType'), value: 'CourseType' },
    { label: getFieldDisplayName('IsSubscribeCourse'), value: 'IsSubscribeCourse' }
  ]
  if (tableType === 10) {
    return [
      { label: getFieldDisplayName('CampusID'), value: 'CampusID' },
      { label: getFieldDisplayName('ClassID'), value: 'ClassID' },
      ...base.filter(o => o.value !== 'CampusID')
    ]
  }
  if (tableType === 20) {
    // 学员排课：不包含开放预约字段
    return [
      { label: getFieldDisplayName('CampusID'), value: 'CampusID' },
      { label: getFieldDisplayName('StudentUserID'), value: 'StudentUserID' },
      ...base.filter(o => o.value !== 'CampusID' && o.value !== 'IsSubscribeCourse')
    ]
  }
  // 30 或其它：不包含班级/学员
  return [
    { label: getFieldDisplayName('CampusID'), value: 'CampusID' },
    ...base.filter(o => o.value !== 'CampusID')
  ]
}
// 获取字段类型
export function getFieldType(field: string): string | undefined {
  return FIELD_TYPES[field as keyof typeof FIELD_TYPES]
}

// 获取字段显示名称
export function getFieldDisplayName(field: string): string | undefined {
  return FIELD_DISPLAY_NAMES[field as keyof typeof FIELD_DISPLAY_NAMES]=='线上课'?FIELD_DISPLAY_NAMES[field as keyof typeof FIELD_DISPLAY_NAMES]:transToConfigDescript(FIELD_DISPLAY_NAMES[field as keyof typeof FIELD_DISPLAY_NAMES])
}