// 课表类型枚举
export enum CourseTimetableTypeEnum {
    TeacherTimetable = 'TeacherTimetable',
    StudentTimetable = 'StudentTimetable',
    ClassTimetable = 'ClassTimetable',
    ClassroomTimetable = 'ClassroomTimetable',
    TimeTimetable = 'TimeTimetable'
  }
  
  // 课表字段类型枚举
  export enum CourseTimetableFieldTypeEnum {
    Main = 'Main',
    Tag = 'Tag'
  }
  
  // 课表颜色设置类型枚举
  export enum CourseTimetableColorSettingTypeEnum {
    ByCourseFinished = 'ByCourseFinished',
    ByTeachingMethod = 'ByTeachingMethod',
    ByTeacher = 'ByTeacher',
    ByCourse = 'ByCourse',
    ByOpeningStatus = 'ByOpeningStatus',
    ByClassroom = 'ByClassroom'
  }
  
  // 课表字段设置
  export interface CourseTimetableFieldSetting {
    FieldName: string | null
    DisplayName: string | null
    IsEnabled: boolean
    IsDefault: boolean
    SortOrder: number
    FieldType: CourseTimetableFieldTypeEnum
  }
  
  // 课表颜色设置详情
  export interface CourseTimetableColorDetail {
    DisplayName: string | null
    EnumValue: number | null
    ColorValue: string | null
  }
  
  // 课表颜色设置
  export interface CourseTimetableColorSetting {
    SettingType: CourseTimetableColorSettingTypeEnum
    IsEnabled: boolean
    ColorDetails?: CourseTimetableColorDetail[] | null
  }
  
  // 标签颜色设置
  export interface CourseTimetableTagColorSetting {
    TagFieldName: string | null
    DisplayName: string | null
    ColorValue: string | null
  }
  
  // 保存课表偏好设置请求模型
  export interface SaveTimetablePreference_ReqFormModel {
    TimetableType: CourseTimetableTypeEnum
    CardShowInformationSettings: CourseTimetableFieldSetting[] | null
  }
  
  // 课表偏好设置视图模型
  export interface TimetablePreference_ViewModel {
    ID: string
    UserID: string
    TimetableType: CourseTimetableTypeEnum
    TimetableTypeName: string | null
    CardShowInformationSettings: CourseTimetableFieldSetting[] | null
    IsEnabled: boolean
    CreateTime: string
    UpdateTime: string
    TimeViewStart: string
    TimeViewEnd: string
    RowHeight?: number
  }
  
  // 保存课表颜色偏好设置请求模型
  export interface SaveTimetableColorPreference_ReqFormModel {
    TimetableType: CourseTimetableTypeEnum
    ColorSettings: CourseTimetableColorSetting[] | null
    TagColorSettings: CourseTimetableTagColorSetting[] | null
    IsEnabled: boolean
  }
  
  // 课表颜色偏好设置响应模型
  export interface TimetableColorPreference_ViewModel {
    ID: string
    UserID: string
    TimetableType: CourseTimetableTypeEnum
    TimetableTypeName: string | null
    ColorSettings: CourseTimetableColorSetting[] | null
    TagColorSettings: CourseTimetableTagColorSetting[] | null
    IsEnabled: boolean
    CreateTime: string
    UpdateTime: string
  }

  // 保存课表时间范围偏好设置请求模型
  export interface SaveTimetableTimeRangePreference_ReqFormModel {
    TimeViewStart: string
    TimeViewEnd: string
  }

  export interface SaveTimetableRowHeightPreference_ReqFormModel {
    RowHeight: number,
    TimetableType: CourseTimetableTypeEnum,
    ApplyToAllTypes: boolean
  }
  