import { apiUrl } from '../store';
import fetch from './http';
import fetchForm from './http-form';
import fetchBlob from "./http-blob";
import { IResponse } from '@common/tool/http/fetch';
import {
  SaveTimetablePreference_ReqFormModel,
  TimetablePreference_ViewModel,
  SaveTimetableColorPreference_ReqFormModel,
  TimetableColorPreference_ViewModel,
  SaveTimetableTimeRangePreference_ReqFormModel,
  SaveTimetableRowHeightPreference_ReqFormModel,
  CourseTimetableTypeEnum
} from '@/types/model/timetable-preference';
import { CourseDraftPublishDraft_ReqFormModel } from '@/types/model/table-course-class';
// 根据类型查询自定义字段
export function queryCourseNew(data: any){
    return fetch.post({
        url: apiUrl+'/api/course/Course/QueryNew', //新接口会要带上全地址
        data,
    })
}

// 根据类型查询自定义字段
export function queryCourseExport(data: any){
    return fetchBlob.post({
        url: apiUrl+'/api/course/Course/QueryExport', //新接口会要带上全地址
        data,
    })
}

// 按月查询日历数据
export function queryCalendarMonth(data: any){
    return fetch.post({
        url: apiUrl + '/api/course/Course/QueryCalendarMonth',
        data,
    })
}

//检查是否有查看权限
export function chekSeeCourseware(data: any){
    return fetchForm.post({
        url: '/api/Courseware/ChekSeeCourseware',
        data,
    })
}

//获取课件地址
export function getFilesUrl(data: any){
    return fetchForm.get({
        url: '/api/courseware/getfilesurl', 
        data
    })
}

//查询多个校区下的教室
export function queryClassroom(data: any){
    return fetchForm.post({
        url: '/api/Classroom/QueryClassroom', 
        data,
    })
}
// 课表偏好设置相关API
//保存颜色和偏好设置的接口
export function saveTimetablePreference(data: any) {
    return fetch.post({
        url: apiUrl + '/api/course/TimetablePreference/SaveAll',
        data,
    })
}

// // 保存课表颜色偏好设置
// export function saveTimetableColorPreference(data: SaveTimetableColorPreference_ReqFormModel): Promise<IResponse<any>> {
//     return fetch.post({
//         url: apiUrl + '/api/course/TimetablePreference/SaveColor',
//         data,
//     })
// }


// // 课表偏好设置相关API
// // 保存课表偏好设置
// export function saveTimetablePreference(data: SaveTimetablePreference_ReqFormModel): Promise<IResponse<any>> {
//     return fetch.post({
//         url: apiUrl + '/api/course/TimetablePreference/Save',
//         data,
//     })
// }

// 获取课表偏好设置
export function getAllTimetablePreference(): Promise<IResponse<TimetablePreference_ViewModel[]>> {
    return fetch.get({
        url: apiUrl + '/api/course/TimetablePreference/GetAll',
    })
}

//保存课表时间范围偏好设置
export function saveTimetableTimeRangePreference(data: SaveTimetableTimeRangePreference_ReqFormModel): Promise<IResponse<any>> {
    return fetch.post({
        url: apiUrl + '/api/course/TimetablePreference/SaveTimeRange',
        data,
    })
}

//保存课表行高偏好设置
export function saveTimetableRowHeightPreference(data: SaveTimetableRowHeightPreference_ReqFormModel): Promise<IResponse<any>> {
    return fetch.post({
        url: apiUrl + '/api/course/TimetablePreference/SaveRowHeight',
        data,
    })
}

//获取班级排课信息
export function getClass(data: any){
    return fetchForm.post({
        url: '/api/class/get', 
        data,
    })
}

//新增排课
export function addCourse(data: any){
    return fetch.post({
        url: apiUrl+'/api/course/CoursePlan/AddCourse',
        data,
        code:[409]
    })
}

// 排课详情
export function getCourseDetailByID(id: String){
    return fetch.get({
        url: `${apiUrl}/api/course/Course/CourseDetailByID?courseID=${id}`
    })
}

// 批量保存草稿
export function BatchSaveCourseDraft(list: Array<any>){
    return fetch.post({
        url: `${apiUrl}/api/course/CourseDraft/BatchSaveCourseDraft`,
        data:list
    })
}

// 删除草稿
export function DeleteCourseDraft(IDList: Array<String>){
    return fetch.post({
        url: `${apiUrl}/api/course/CourseDraft/DeleteCourseDraft`,
        data: {
            IDList
        }
    })
}

/**
 * 获取草稿列表
 * @param data 
 * @returns 
 */
export function GetCourseDraftList(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CourseDraft/GetCourseDraftList`,
        data
    })
}

//获取学员在校区的课程剩余课时
export function getStudentRemaindAmount(data: any){
    return fetchForm.post({
        url: `/api/Course/GetStudentRemaindAmount`,
        data
    })
}


// 添加排课学员
export function addCourseStudent(data: any){
    return fetch.post({
        url: `/api/Course/AddStudents`,
        data
    })
}

// 删除排课学员
export function reduceCourseStudent(data: any){
    return fetch.post({
        url: `/api/course/ReduceStudents`,
        data
    })
}
export function RemoveCourseAdjustOrTryStudents(data: any){
    return fetch.post({
        url: `/api/course/RemoveAdjustOrTryStudents`,
        data
    })
}
export function removeCourseStudent(data: any){
    return fetch.post({
        url: `/api/Course/RemoveStudent`,
        data
    })
}

// 修改学员备注
export function updateDescribeByCourseStudent(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/Course/UpdateDescribeByCourseStudent`,
        data
    })
}

//取消预约
export function cancelCourseSubscribeCourseRecords(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/SubscribeCourse/CancelSubscribeCourseRecordsByTeacher`,
        data
    })
}

//取消排队
export function cancelSubscribeCourseQueue(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/SubscribeCourse/CancelSubscribeCourseQueueByTeacher`,
        data
    })
}

//获取学员已购买的课程信息
export function getStudentBuyAndCourseAmount(data: any){
    return fetchForm.post({
        url: `/api/Course/GetStudentBuyAndCourseAmount`,
        data
    })
}

// 获取课程的上课进度
export function getCourseShiftSchedule(data: any){
    return fetch.post({
        url: `/api/Shift/GetShiftSchedule`,
        data
    })
}

// 修改排课
export function editCourseInfoByID(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/EditCourseByID`,
        data,
        code:[409]
    })
}

// 获取老师校区可排课时间
export function getTeachersCourseTimes(data: any){
    return fetch.get({
        url: `/api/Employee/GetTeacherCourseTimes`,
        data
    })
}

//获取学员报读的一对一课程对应的班级
export function getClassOfOne(data: any){
    return fetchForm.post({
        url: `/api/class/GetClassOfOne`,
        data
    })
}

//获取学员指定课程的上课信息
export function getStudentShiftAmount(data: any){
    return fetchForm.post({
        url: `/api/class/GetStudentShiftAmount`,
        data
    })
}

//获取学员相关信息
export function getStudent(data: any){
    return fetchForm.post({
        url: `/api/Student/Get`,
        data
    })
}

//按学员新增排课
export function addStudentCourse(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/AddStudentCourse`,
        data,
        code:[409]
    })
}

// 检查是否有约课学员
export function checkSubscribeCourseStudent(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/Course/CheckPutData`,
        data
    })
}

// 获取教室可用状态
export function GetClassRoomAvailabilityStatus(data: any){
    return fetch.post({
        url: apiUrl+`/api/course/ClassRoom/GetClassRoomAvailabilityStatus`,
        data
    })
}

// 获取教室可用状态
export function GetClassRoomAvailabilityStatusByPlan(data: any){
    return fetch.post({
        url: apiUrl+`/api/course/ClassRoom/GetClassRoomAvailabilityStatusByPlan`,
        data
    })
}


//新增排预约课
export function addSubscribeCourse(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/AddSubscribeCourse`,
        data,
        code:[409]
    })
}

//获取班级课程科目信息
export function getClassWithShiftAndSubject(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Class/GetClassWithShiftAndSubject`,
        data:data
    })
}


// 检查草稿 冲突和限制
export function CheckCourseDraft(data: {IDList:Array<String>}){
    return fetch.post({
        url: `${apiUrl}/api/course/CourseDraft/CheckCourseDraft`,
        data
    })
}

export function getClassSubject(data: any){
    return fetch.post({
        url: `/api/Class/GetClassSubject`,
        data
    })
}

//排课列表合计
export function queryCourseTotal(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryTotal`,
        data
    })
}

//删除排课
export function deleteCourseList(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/DeleteCourseList`,
        data
    })
}

//删除排课申请
export function deleteCourseAsk(data: any){
    return fetch.post({
        url: `/api/Course/Delete_Ask`,
        data
    })
}

// 排课-本批次排课列表
export function getCourseListInfoByPlanID(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/CourseListByPlanID`,
        data
    })
}

//取消排课
export function cancelCourse(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/CancelCourse`,
        data
    })
}

//修改已取消的排课
export function cancelCoursePutContent(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/PutContent`,
        data
    })
}

// 修改上课内容
export function editCoursesSchedule(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/EditCourseSchedule`,
        data
    })
}

//批量一对一点名
export function setAttendanceOneToOneBatch(data: any){
    return fetchForm.post({
        url: `/api/course/SetAttendanceOneToOneBatch`,
        data,
        code:[400]
    })
}

// 获取点名表
export function getAttendancePrint(data: any){
    return fetch.post({
        url: `/api/Course/GetAttendanceForPrint`,
        data
    })
}

// 获取上课内容详情
export function getCoursesSchedule(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/Course/GetCourseSchedule`,
        data
    })
}

// 查询预约记录
export function querySubscribeRecords(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/course/SubscribeCourse/QuerySubscribeRecords`,
        data,
    })
}

// 预约记录-导出
export function exportSubscribeRecords(data: any) {
    return fetchBlob.post({
        url: `${apiUrl}/api/course/SubscribeCourse/QuerySubscribeRecordsExport`,
        data,
    })
}

// 查询排队记录
export function querySubscribeCourseQueue(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/course/SubscribeCourse/QuerySubscribeCourseQueue`,
        data,
    })
}

// 排队记录-导出
export function exportSubscribeCourseQueue(data: any) {
    return fetchBlob.post({
        url: `${apiUrl}/api/course/SubscribeCourse/QuerySubscribeCourseQueueExport`,
        data,
    })
}

//复制或移动排课
export function copyOrMoveCourse(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/CopyOrMoveCourse`,
        data,
        code:[409]
    })
}

// 复制或移动 日程
export function CopyOrMoveSchedule(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Schedule/CopyOrMoveSchedule`,
        data,
        code:[409]
    })
}

//批量修改排课
export function putCourseList(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/Puts`,
        data,
        code:[409]
    })
}

// 刷新排队
export function RefreshSubscribeCourseQueue(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/SubscribeCourse/RefreshQueue`,
        data
    })
}

// 查询老师列表
export function queryTeacherListProfile(params:any) {
    return fetch.get({
        url: '/api/onlinesetting/QueryTeacherProfile',
        data: params
    });
}

// 删除老师
export function deleteTeacherProfile(params:any) {
    return fetch.get({
        url: '/api/onlinesetting/DeleteTeacherInfo',
        data: params
    });
}

// 修改老师
export function updateTeacherProfile(params:any) {
    return fetch.post({
        url: '/api/onlineproduct/PutTeacherInfo',
        data: params
    });
}

// 批量修改排课申请
export function putCourseAsk(data: any){
    return fetch.post({
        url: `/api/Course/Put_Ask`,
        data
    })
}

//点名、撤销点名
export function setAttendance(data: any){
    return fetchForm.post({
        url: `/api/Course/SetAttendance`,
        data
    })
}

// 获取排课计划预览-使用场景：按规则新增班级排课数据
export function CoursePlanGetCoursePlanPreview(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/GetCoursePlanPreview`,
        data
    })
}
// 获取排课计划预览-使用场景：按规则新增学员排课数据
export function GetStudentCoursePlanPreview(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/GetStudentCoursePlanPreview`,
        data
    })
}
// 获取排课计划预览-使用场景：按规则新增预约课数据
export function GetSubscribeCoursePlanPreview(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/GetSubscribeCoursePlanPreview`,
        data
    })
}

// 草稿发布为正式排课
export function CourseDraftPublishDraft(data: CourseDraftPublishDraft_ReqFormModel){
    return fetch.post({
        url: `${apiUrl}/api/course/CourseDraft/PublishDraft`,
        data
    })
}

//获取调课可选课程
export function adjustCourseGetShifts(data: any){
    return fetchForm.post({
        url: `/api/Shift/AdjustCourseGetShifts`,
        data
    })
}

//获取排课里的学员-调课使用
export function getStudentsForAdjustCourse(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/Course/GetStudentsForAdjustCourse`,
        data
    })
}

//临时调课
export function adjustForStudents(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/AdjustForStudents`,
        data,
        code:[409]
    })
}

//撤销临时调课
export function unAdjustForStudents(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/UnAdjustForStudents`,
        data,
        code:[409]
    })
}

//学员排课冲突信息
export function checkStudentAvailabilityStatus(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/CheckStudentAvailabilityStatus`,
        data
    })
}

//课表日历-班级视图查询
export function queryCalendarClass(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarClass`,
        data
    })
}

//课表日历-教室视图查询
export function queryCalendarClassroom(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarClassroom`,
        data
    })
}

//课表日历-学员视图查询
export function queryCalendarStudent(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarStudent`,
        data
    })
}

//课表日历-老师视图查询
export function queryCalendarTeacher(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarTeacher`,
        data
    })
}

//选择时间：展示对象每天的排课数
export function queryTeacherCourseCountByDate(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryTeacherCourseCountByDate`,
        data
    })
}

//学员信息卡片详情
export function getStudentCardDetail(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/Student/GetStudentCourseInfo`,
        data
    })
}

//查询教室授权校区
export function getClassRoomCampus(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/ClassRoom/GetClassRoomCampus`,
        data
    })
}

//查询对象空闲时间
export function queryFree(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Course/QueryFree`,
        data
    })
}

//按计划新增日程
export function addSchedulePlan(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Schedule/AddSchedulePlan`,
        data,
        code:[409]
    })
}

//删除日程
export function deleteSchedule(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Schedule/DeleteSchedule`,
        data
    })
}

//按计划删除日程
export function deleteSchedulePlan(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Schedule/DeleteSchedulePlan`,
        data
    })
}

//获取日程详情
export function getScheduleDetail(data: any){
    return fetch.get({
        url: `${apiUrl}/api/course/Schedule/GetScheduleDetail`,
        data
    })
}

//修改日程
export function editSchedule(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Schedule/EditSchedule`,
        data,
        code:[409]
    })
}

//按计划修改日程
export function editSchedulePlan(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/Schedule/EditSchedulePlan`,
        data,
        code:[409]
    })
}

//获取排课、日程计划详情
export function getSchedulePlan(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/GetPlan`,
        data
    })
}

//课表日历-班级视图导出
export function queryCalendarClassExport(data: any){
    return fetchBlob.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarClassExport`,
        data
    })
}

//课表日历-教室视图导出
export function queryCalendarClassroomExport(data: any){
    return fetchBlob.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarClassroomExport`,
        data
    })
}

//课表日历-学员视图导出
export function queryCalendarStudentExport(data: any){
    return fetchBlob.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarStudentExport`,
        data
    })
}

//课表日历-老师视图导出
export function queryCalendarTeacherExport(data: any){
    return fetchBlob.post({
        url: `${apiUrl}/api/course/Course/QueryCalendarTeacherExport`,
        data
    })
}

//按计划修改排课
export function editCoursePlan(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/EditCoursePlan`,
        data,
        code:[409]
    })
}

// 获取学员约课规则
export function getSubscribeCourseRule(){
    return fetch.get({
        url: `/api/SubscribeCoursePlan/GetSubscribeCourseRule`
    })
}


//查询用户设置列表
export function queryUserSettingsPage(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/User/QueryUserSettingsPage`,
        data
    })
}

//批量保存用户设置
export function saveUserSettingsList(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/User/SaveUserSettingsList`,
        data
    })
}

//获取排课复用信息
export function getCoursePlanTimeReusableInfot(data: any){
    return fetch.post({
        url: `${apiUrl}/api/course/CoursePlan/GetCoursePlanTimeReusableInfo`,
        data
    })
}
