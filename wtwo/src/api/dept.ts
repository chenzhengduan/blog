import fetch from './http';
import fetchForm from "./http-form";

//获取区域列表
export function getAreaList(params:any) {
    return fetch.get({
        url: '/api/Area/ReportAreaList',
        data: params
    })
}


//获取可操作校区的员工部门树
export function queryWithEmployeeRight(params:any){
    return fetch.get({
        url: '/api/depart/QueryWithEmployeeRight',
        data: params
    })
}

// 异步加载员工部门树
export function queryDepartEmployee(params: any) {
    return fetch.post({
        url: '/api/depart/QueryDepartEmployee',
        data: params
    })
}

// 异步加载某个节点的员工
export function multiSelEmployee(params: any) {
    return fetch.post({
        url: '/api/Employee/MuitSelEmployee',
        data: params
    })
}

// 异步加载年级/科目员工树
export function modalQueryEmployee(params: any) {
    return fetchForm.post({
        url: '/api/Employee/ModalQueryEmployee',
        data: params
    })
}

//查询所有部门/校区
export function queryDepart(params: any | undefined) {
    return fetch.post({
        url: '/api/depart/query',
        data: params || {},
    })
}