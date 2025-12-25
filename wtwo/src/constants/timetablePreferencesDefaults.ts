// 统一的课表偏好默认配置，供各处复用

export const DEFAULT_TIME_RANGE_START = '07:00'
export const DEFAULT_TIME_RANGE_END = '23:30'
export const DEFAULT_ROW_HEIGHT = 30

// 默认左侧基础信息字段顺序（不含内置的 TimeViewStart）
export const DEFAULT_LEFT_FIELD_KEYS = ['ShiftName', 'ClassName', 'ClassroomName'] as const

// 左侧字段的默认显示名（作为兜底，组件内优先用列配置 allColumns 匹配）
export const DEFAULT_LEFT_FIELD_DISPLAY_NAME: Record<string, string> = {
  ClassName: '上课班级/学员',
  ShiftName: '上课课程',
  ClassroomName: '上课教室',
}

// 默认右侧标签候选（若服务端无返回，用此生成默认）
export const DEFAULT_RIGHT_TAG_CANDIDATES = [
  { FieldName: 'Finished', DisplayName: '上课状态' },
  { FieldName: 'IsSubscribeCourse', DisplayName: '约课' },
  { FieldName: 'IsOneToOne', DisplayName: '教学形式' },
  { FieldName: 'TeacherName', DisplayName: '任课老师'},
  { FieldName: 'ClassroomName', DisplayName: '上课教室'},
  { FieldName: 'ClassPlanNum_ClassPlanCount', DisplayName: '上课进度'},
  { FieldName: 'CourseType', DisplayName: '上课方式'},
  { FieldName: 'CourseStatus', DisplayName: '开课状态'},
] as const

// 默认启用的标签键
export const DEFAULT_ENABLED_TAG_KEYS = ['Finished', 'IsSubscribeCourse', 'IsOneToOne'] as const



// 统一列配置（从 timetablePreferenceSettings.vue 抽取）
export const ALL_COLUMNS = [{
	FieldName:'ShiftName',
	DisplayName:'上课课程',
	ColumnKey:'ShiftName',
},{
	FieldName:'ClassName',
	DisplayName:'上课班级/学员',
	ColumnKey:'ClassName,StudentName',
},{
	FieldName:'IsOneToOne',
	DisplayName:'教学形式',
	ColumnKey:'IsOneToOne',
  Tips:'课程管理中的“课程类型”'
},{
	FieldName:'CampusName',
	DisplayName:'上课校区',
	ColumnKey:'CampusName'
},
// {
// 	FieldName:'StartTime',
// 	DisplayName:'上课时间',
// 	ColumnKey:'StartTime,EndTime'
// },
{
	FieldName:'Duration',
	DisplayName:'上课时长',
	ColumnKey:'Duration'
},{
	FieldName:'Finished',
	DisplayName:'上课状态',
	ColumnKey:'FinishedName'
},{
	FieldName:'StudentAttendanceCount',
	DisplayName:'实到/应到',
	ColumnKey:'StudentAttendanceCount,StudentCount'
},{
	FieldName:'ClassPlanNum_ClassPlanCount',
	DisplayName:'上课进度',
	ColumnKey:'ClassPlanNum_ClassPlanCount',
	Tips:``
},{
	FieldName:'ChapterTitle',
	DisplayName:'章节内容',
	ColumnKey:'ChapterTitle'
},{
	FieldName:'CourseContent',
	DisplayName:'上课内容',
	ColumnKey:'CourseContent'
},{
	FieldName:'SubjectName',
	DisplayName:'上课科目',
	ColumnKey:'SubjectName',
	Tips:`全科课程的上课科目`
},{
	FieldName:'CourseType',
	DisplayName:'上课方式',
	ColumnKey:'CourseType'
},{
	FieldName:'ClassroomName',
	DisplayName:'上课教室',
	ColumnKey:'ClassroomName'
},{
	FieldName:'TeacherName',
	DisplayName:'任课老师',
	ColumnKey:'TeacherName'
},{
	FieldName:'AssistantTeacherName',
	DisplayName:'助教',
	ColumnKey:'AssistantTeacherName'
},{
	FieldName:'MasterName',
	DisplayName:'学管师',
	ColumnKey:'MasterName'
},{
	FieldName:'HeadMasterUserName',
	DisplayName:'班主任',
	ColumnKey:'HeadMasterUserName'
},{
	FieldName:'IsSubscribeCourse',
	DisplayName:'开放预约',
	ColumnKey:'IsSubscribeCourse',
	Tips:`已开放的排课，支持学员在手机端预约。`
},{
	FieldName:'StartStudentCount',
	DisplayName:'开课人数',
	ColumnKey:'StartStudentCount',
	Tips:`1、只有预约课，才会有开课人数；<br/>2、已约人数达到“开课人数”后，状态变为“已开课”`
},{
	FieldName:'CourseStatus',
	DisplayName:'开课状态',
	ColumnKey:'CourseStatus',
	Tips:`1、预约课的“已约人数”需要达到“开课人数”，会转为“已开课”状态；<br/>2、若设置了自动取消排课，则“未开课”的排课会根据设置的时间，自动取消排课；`
},{
	FieldName:'InternalRemark',
	DisplayName:'对内备注',
	ColumnKey:'InternalRemark'
}
// ,{
// 	FieldName:'Describe',
// 	DisplayName:'对外备注',
// 	ColumnKey:'Describe'
// }
] as const
