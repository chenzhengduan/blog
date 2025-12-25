export interface TableCourseClass {
    /**
     * 助教
     */
    AssistantTeacherID: string;
    /**
     * 校区ID
     */
    CampusID: string;
    /**
     * 班级ID
     */
    ClassID: string;
    /**
     * 教师ID
     */
    ClassRoomID: string;
    /**
     * 排课方式，10=按班
     */
    CourseMethod: number;
    /**
     * 排课类型，（1=线上课，2=线下课）
     */
    CourseType: number;
    /**
     * 日期
     */
    Date: string;
    /**
     * 外部备注
     */
    Describe: string;
    /**
     * 排课结束时间
     */
    EndTime: string;
    ID: string;
    /**
     * 内部备注
     */
    InternalRemark: string;
    /**
     * 是否开放预约
     */
    IsSubscribeCourse: number;
    /**
     * 任课老师
     */
    MainTeacherIDList: string[];
    /**
     * 课程ID
     */
    ShiftID: string;
    /**
     * 排课开始时间
     */
    StartTime: string;
    /**
     * 学员ID，按学员排课专用
     */
    StudentUserID: string;
    /**
     * 科目ID
     */
    SubjectID: string;
    [property: string]: any;
}

// 预检查结果数据结构
export interface PreCheckResultData {
  DraftId: string
  ErrorFieldList: string[]
  CheckFieldList: CheckField[]
  ConflictFieldList: ConflictField
}

// 限制字段信息
export interface CheckField {
  FieldNameList: string[]
  ErrorMessage: string
  CheckerConfigList: string[]
}

// 冲突字段信息
export interface ConflictField {
  FieldNameList: string[]
  ErrorMessage: string
  ConflictingCourseList: ConflictingCourse[]
  ConflictingDraftList: any[]
  ConflictingScheduleList: any[]
}

// 冲突的课程数据
export interface ConflictingCourse {
  ConflictingID: string
  CourseMethod: number
  CampusID: string
  CampusName: string
  ClassID: string
  ClassName: string
  ShiftID: string
  ShiftName: string
  SubjectID: string
  SubjectName: string
  ClassRoomID: string
  ClassRoomName: string
  StartTime: string
  EndTime: string
}

// 草稿发布为正式排课
export interface CourseDraftPublishDraft_ReqFormModel {
  DraftIDList: string[]
  CheckConflict:number // 0:不检查，1:检查
} 

export interface CourseDraftPublishDraft_ResFormModel {
  Data?: WTwoCourseModelDTOCheckConflictCourseDraftSaveErrorDTO[] | null;
  ErrorCode?: number;
  ErrorMsg?: null | string;
  IsSuccess?: boolean;
}

/**
* WTwo.Course.Model.DTO.CheckConflict.CourseDraft_SaveErrorDTO，草稿保存错误信息
*/
export interface WTwoCourseModelDTOCheckConflictCourseDraftSaveErrorDTO {
  /**
   * 限制字段列表
   */
  CheckFieldList?: WTwoCourseModelDTOCheckConflictCheckFieldModel[] | null;
  ConflictFieldList?: WTwoCourseModelDTOCheckConflictConflictFieldModel;
  /**
   * 草稿ID（新增时为空）
   */
  DraftId?: null | string;
  /**
   * 非法字段列表
   */
  ErrorFieldList?: WTwoCourseModelDTOCheckConflictErrorFieldModel[] | null;
}

/**
* WTwo.Course.Model.DTO.CheckConflict.CheckFieldModel，错误字段模型
*/
export interface WTwoCourseModelDTOCheckConflictCheckFieldModel {
  CheckerConfigList?: string[] | null;
  ErrorMessage?: null | string;
  FieldNameList?: string[] | null;
}

/**
* WTwo.Course.Model.DTO.CheckConflict.ConflictFieldModel，错误字段模型
*/
export interface WTwoCourseModelDTOCheckConflictConflictFieldModel {
  /**
   * 冲突的数据
   */
  ConflictingCourseList?: WTwoCourseModelDTOCheckConflictConflictingCourseItemDTO[] | null;
  /**
   * 草稿冲突的数据
   */
  ConflictingDraftList?: WTwoCourseModelDTOCheckConflictConflictingCourseItemDTO[] | null;
  /**
   * 日程冲突的数据
   */
  ConflictingScheduleList?: WTwoCourseModelDTOCheckConflictConflictingCourseItemDTO[] | null;
  ErrorMessage?: null | string;
  FieldNameList?: string[] | null;
}

/**
* WTwo.Course.Model.DTO.CheckConflict.ConflictingCourseItemDTO，冲突详情
*/
export interface WTwoCourseModelDTOCheckConflictConflictingCourseItemDTO {
  /**
   * 助教列表
   */
  AssistantTeacherList?: WTwoBusCommonModelCommonCommonDataModelIDNameStringModel[] | null;
  /**
   * 校区ID
   */
  CampusID?: null | string;
  /**
   * 校区名称
   */
  CampusName?: null | string;
  /**
   * 班级ID
   */
  ClassID?: null | string;
  /**
   * 班级名称
   */
  ClassName?: null | string;
  /**
   * 教室ID
   */
  ClassRoomID?: null | string;
  /**
   * 教室名称
   */
  ClassRoomName?: null | string;
  /**
   * 排课ID、日程ID、草稿ID
   */
  ConflictingID?: string;
  ConflictingName?: null | string;
  /**
   * 冲突数据类型，排课、草稿、日程
   */
  CourseMethod?: number;
  EndTime?: null | string;
  /**
   * 任课老师列表
   */
  MainTeacherList?: WTwoBusCommonModelCommonCommonDataModelIDNameStringModel[] | null;
  /**
   * 课程ID
   */
  ShiftID?: null | string;
  /**
   * 课程名称
   */
  ShiftName?: null | string;
  StartTime?: null | string;
  /**
   * 学员姓名
   */
  StudentName?: null | string;
  /**
   * 学员ID，如果不是学员排课则为空
   */
  StudentUserID?: null | string;
  /**
   * 科目ID
   */
  SubjectID?: null | string;
  /**
   * 科目名称
   */
  SubjectName?: null | string;
}

/**
* WTwo.Bus.Common.Model.Common.CommonDataModel.IDNameStringModel
*/
export interface WTwoBusCommonModelCommonCommonDataModelIDNameStringModel {
  ID?: null | string;
  Name?: null | string;
}

/**
* WTwo.Course.Model.DTO.CheckConflict.ErrorFieldModel，错误字段模型
*/
export interface WTwoCourseModelDTOCheckConflictErrorFieldModel {
  ErrorMessage?: null | string;
  FieldName?: null | string;
}
