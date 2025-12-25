import fetch from './http';
import fetchForm from "./http-form";
import { IFieldsParams, IFieldsModel, IDictFieldsModel } from '@/types/fields';
import { apiUrl, testUrl } from "../store";
// 根据类型查询自定义字段
export function queryCustomForm(data: IFieldsParams){
    return fetch.post<IFieldsModel[]>({
        url: '/api/CustomerForm/QueryCustomerForm',
        data,
    })
}

// 根据类型查询字典数据
export function queryDictByType(type: string, status?: number){
    let params:any={
        type
    }
    if(type=='SHIFT_SPEC'){
        params.isFeeCustType=status||0
    }else{
        params.status=status||0
    }
    return fetchForm.post<IDictFieldsModel[]>({
        url: '/api/Dictionary/Get',
        data: params,
    })
}

//修改配置项（目前用到的：修改校区超过最大容量后允不允许继续录入学员）
export function putSysInfoByName(params:any){
    return fetchForm.post({
        url: '/api/SysInfo/PutByName',
        data: params,
    })
}

//获取课程
export function queryShift(params:any){
    return fetchForm.post({
        url: '/api/Shift/Query',
        data: params
    })
}

//查询所有校区
export function queryAllCampus(params:any){
    return fetchForm.post({
        url: '/api/depart/queryAllCampus',
        data: params
    })
}


//获取班级
export function queryClass(params:any){
    return fetchForm.post({
        url: '/api/Class/Query',
        data: params
    })
}

//获取老师
export function queryTeacherProfile(params:any){
    return fetch.post({
        url: '/api/onlinesetting/QueryTeacherProfile',
        data: params
    })
}

//获取学员
export function queryStudentBreif(params:any){
    return fetchForm.post({
        url: '/api/Student/QueryStudentBrief',
        data: params
    })
}

// 新手指引 - 记录用户操作
export function LogUserSettingStep(step: number){
    return fetchForm.post({
        url: '/api/user/LogUserSettingStep',
        data: {
            step
        }
    })
}

// 新手指引 - 获取用户是否阅读 99为看过课程新手指引了
export function GetUserSettingStep(){
    return fetchForm.post({
        url: '/api/User/GetUserSettingStep',
        data: {}
    })
}

//存为快捷筛选 type传4，存为课表日历视图模式 type传5
export function postUserLabel(params:any){
    return fetch.post({
        url: '/api/user/PostUserLabel',
        data: params
    })
}

//获取用户快捷筛选、获取用户存储的课表日历视图模式 
export function getUserLabel(params:any){
    return fetch.post({
        url: '/api/user/GetUserLabel',
        data: params
    })
}

//删除用户快捷筛选
export function deleteUserLabel(params:any){
    return fetch.post({
        url: '/api/user/DeleteUserLabel',
        data: params
    })
}

//获取课程相关信息
export function getShiftInfo(params:any){
    return fetch.post({
        url: '/api/Shift/Get',
        data: params
    })
}

//获取业务规则
export function querySysConfig(params:any) {
    return fetchForm.post({
        url: '/api/sysConfig/query',
        data: params
    });
}

//查询员工 忙闲
export function getEmployeeAvailabilityStatus(params:any){
    return fetch.post({
        url: apiUrl+'/api/course/Employee/GetEmployeeAvailabilityStatus',
        data: params
    })
}

//根据计划时间 查询员工 忙闲
export function GetEmployeeAvailabilityStatusByPlan(params:any){
    return fetch.post({
        url: apiUrl+'/api/course/Employee/GetEmployeeAvailabilityStatusByPlan',
        data: params
    })
}


//查询常用时段
export function queryCourseTime(params:any){
    return fetchForm.post({
        url: '/api/Course/QueryCourseTime',
        data: params
    })
}

//查询节假日
export function queryHoliday(params:any){
    return fetchForm.post({
        url: '/api/Holiday/Query',
        data: params
    })
}

//查询老师类型--排课选老师、助教可以选老师类型
export function queryTeacherCommissionSetting(){
    return fetchForm.post({
        url: '/api/Class/QueryTeacherCommissionSetting',
        data: {}
    })
}

//快捷查询学员
export function getStudentDic(params:any){
    return fetch.post({
        url: apiUrl+'/api/course/Student/GetStudentDic',
        data: params
    })
}

//快捷查询员工
export function getEmployeeDic(params:any){
    return fetch.post({
        url: apiUrl+'/api/course/Employee/GetEmployeeDic',
        data: params
    })
}

//获取用户指定页面选择列
export function getUserColumns(params:any){
    return fetchForm.post({
        url: '/api/user/GetUserColumns',
        data: params
    })
}

//保存用户指定页面选择列
export function postUserColumns(params:any){
    return fetchForm.post({
        url: '/api/user/PostUserColumns',
        data: params
    })
}

//检查指定日期在经贝是否结账
export function checkJingBeiFinanceLock(params:any){
    return fetchForm.post({
        url: '/api/ZWFinance/CheckJingBeiFinanceLock',
        data: params
    })
}


// 查询配置项详情
export function SysConfigGet(params:any){
    return fetchForm.post({
        url: '/api/SysConfig/Get',
        data: params
    })
}

//返回旧版
export function BackVesionFeedback(params: {
    checkboxValue: string,
    feedbackContent: string,
    enableInterviews: Number,
    contact: String,
    Module:Number
}) {
    return fetchForm.post({
        url: '/api/User/BackVesionFeedback',
        data: params
    })
}

//获取员工（table版）
export function getEmployeeList(params:any){
    return fetch.post({
        url: `${apiUrl}/api/course/Employee/GetEmployeeList`,
        data: params
    })
}

//获取配置详情
export function getSysConfig(params: any) {
    return fetchForm.post({
        url: '/api/SysConfig/Get',
        data: params
    })
}

// 修改配置
export function PutOtherParameter(params: {
    id: string,
    otherParameter: string,
}) {
    return fetch.post({
        url: '/api/sysconfig/PutOtherParameter',
        data: params
    })
}