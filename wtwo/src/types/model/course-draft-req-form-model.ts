/**
 * WTwo.Course.Model.Request.CourseDraft_ReqFormModel，排课草稿请求模型
 *
 * 字段说明：
 * - null值：表示不修改该字段（仅更新时有效）
 * - 空值（空字符串、空GUID）：表示清空该字段
 * - 有值：表示设置该字段的值
 */
export interface Request {
    /**
     * 助教ID列表
     * 新增时可选，更新时null表示不修改
     */
    AssistantTeacherID?: string | null;
    /**
     * 校区ID
     * 新增时必填，更新时null表示不修改
     */
    CampusID?: null | string;
    /**
     * 班级ID
     * 新增时必填，更新时null表示不修改
     */
    ClassID?: null | string;
    /**
     * 教室ID
     * 新增时必填，更新时null表示不修改
     */
    ClassRoomID?: null | string;
    CourseMethod?: WTwoCourseModelEnumsCreateCourseTypeEnum;
    CourseType?: WTwoBusCommonModelDBEnumsCourseTypeEnum;
    /**
     * 日期
     * 新增时必填，更新时null表示不修改
     */
    Date?: Date | null;
    /**
     * 外部备注
     * 新增时可选，更新时null表示不修改，空字符串表示清空
     */
    Describe?: null | string;
    /**
     * 排课结束时间
     * 新增时必填，更新时null表示不修改
     */
    EndTime?: Date | null;
    /**
     * 草稿ID（新增时为空）
     */
    ID?: null | string;
    /**
     * 内部备注
     * 新增时可选，更新时null表示不修改，空字符串表示清空
     */
    InternalRemark?: null | string;
    /**
     * 是否开放预约（0=否，1=是）
     * 新增时必填，更新时null表示不修改
     */
    IsSubscribeCourse?: number | null;
    /**
     * 任课老师ID列表
     * 新增时可选，更新时null表示不修改
     */
    MainTeacherIDList?: string[] | null;
    /**
     * 课程ID
     * 新增时必填，更新时null表示不修改
     */
    ShiftID?: null | string;
    /**
     * 排课开始时间
     * 新增时必填，更新时null表示不修改
     */
    StartTime?: Date | null;
    /**
     * 学员ID，如果不是学员排课则为空Guid
     * 新增时必填，更新时null表示不修改
     */
    StudentUserID?: null | string;
    /**
     * 科目ID
     * 新增时必填，更新时null表示不修改
     */
    SubjectID?: null | string;
}

/**
 * WTwo.Course.Model.Enums.CreateCourseTypeEnum，排课类型： 按班级排课\按学员排课\预约课\日程
 */
export type WTwoCourseModelEnumsCreateCourseTypeEnum = "按班级排课" | "按学员排课" | "预约课" | "日程";

/**
 * WTwo.Bus.Common.Model.DBEnums.CourseTypeEnum，上课方式，1=线下课，2=在线课
 */
export type WTwoBusCommonModelDBEnumsCourseTypeEnum = "其他" | "线下课" | "在线课";


/**
 *
 * WTwo.Course.Model.Common.CallResult`1[[WTwo.Course.Model.Response.BatchSaveCourseDraft_ViewModel,
 * WTwo.Course.Model, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null]]
 */
export interface Response {
    Data?: WTwoCourseModelResponseBatchSaveCourseDraftViewModel;
    ErrorCode?: number;
    ErrorMsg?: null | string;
    IsSuccess?: boolean;
}

/**
 * WTwo.Course.Model.Response.BatchSaveCourseDraft_ViewModel，批量保存草稿响应模型
 */
export interface WTwoCourseModelResponseBatchSaveCourseDraftViewModel {
    /**
     * 保存成功的草稿数据列表
     */
    SavedDraftList?: WTwoCourseModelResponseCourseDraftViewModel[] | null;
    /**
     * 保存过程中的错误提示列表
     */
    SaveErrorList?: WTwoCourseModelResponseBatchSaveCourseDraftViewModelCourseDraftSaveError[] | null;
}

/**
 * WTwo.Course.Model.Response.BatchSaveCourseDraft_ViewModel+CourseDraft_SaveError，草稿保存错误信息
 */
export interface WTwoCourseModelResponseBatchSaveCourseDraftViewModelCourseDraftSaveError {
    /**
     * 草稿ID（新增时为空）
     */
    DraftId?: null | string;
    /**
     * 错误字段列表
     */
    ErrorFieldList?: string[] | null;
    /**
     * 错误原因
     */
    ErrorMessage?: null | string;
}

/**
 * WTwo.Course.Model.Response.CourseDraft_ViewModel，排课草稿响应模型
 */
export interface WTwoCourseModelResponseCourseDraftViewModel {
    /**
     * 助教列表
     */
    AssistantTeacherList?: WTwoBusCommonModelCommonCommonDataModelIDNameModel[] | null;
    /**
     * 校区ID
     */
    CampusID?: string;
    /**
     * 校区名称
     */
    CampusName?: null | string;
    /**
     * 班级ID
     */
    ClassID?: string;
    /**
     * 班级名称
     */
    ClassName?: null | string;
    /**
     * 教室ID
     */
    ClassRoomID?: string;
    /**
     * 教室名称
     */
    ClassRoomName?: null | string;
    CourseMethod?: WTwoCourseModelEnumsCreateCourseTypeEnum;
    CourseType?: WTwoBusCommonModelDBEnumsCourseTypeEnum;
    /**
     * 排课类型名称
     */
    CourseTypeName?: null | string;
    /**
     * 创建时间
     */
    CreateTime?: Date;
    /**
     * 日期
     */
    Date?: Date | null;
    /**
     * 外部备注
     */
    Describe?: null | string;
    /**
     * 排课结束时间
     */
    EndTime?: Date | null;
    /**
     * 草稿ID
     */
    ID?: string;
    /**
     * 内部备注
     */
    InternalRemark?: null | string;
    IsSubscribeCourse?: WTwoBusCommonModelPublicEnumsYesOrNoEnum;
    /**
     * 是否开放预约名称
     */
    IsSubscribeCourseName?: null | string;
    /**
     * 任课老师列表
     */
    MainTeacherList?: WTwoBusCommonModelCommonCommonDataModelIDNameModel[] | null;
    /**
     * 课程ID
     */
    ShiftID?: string;
    /**
     * 课程名称
     */
    ShiftName?: null | string;
    /**
     * 排课开始时间
     */
    StartTime?: Date | null;
    /**
     * 学员姓名
     */
    StudentName?: null | string;
    /**
     * 学员ID，如果不是学员排课则为空Guid
     */
    StudentUserID?: string;
    /**
     * 科目ID
     */
    SubjectID?: string;
    /**
     * 科目名称
     */
    SubjectName?: null | string;
    /**
     * 更新时间
     */
    UpdateTime?: Date;
}

/**
 * WTwo.Bus.Common.Model.Common.CommonDataModel.IDNameModel
 */
export interface WTwoBusCommonModelCommonCommonDataModelIDNameModel {
    ID?: string;
    Name?: null | string;
}

/**
 * WTwo.Bus.Common.Model.PublicEnums.YesOrNoEnum，是否 通用枚举
 */
export type WTwoBusCommonModelPublicEnumsYesOrNoEnum = "No" | "Yes";