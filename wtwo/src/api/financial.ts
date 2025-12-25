import fetchForm from "./http-form";

// 查询财务期间列表
export function QueryDuration(params: any) {
    return fetchForm.post({
        url: '/api/ZWFinance/QueryDuration',
        data: params
    })
}

// 解锁/锁定
export function UpdateFinancialPeriodLock(params: any) {
    return fetchForm.post({
        url: '/api/ZWFinance/OperateData',
        data: params
    })
}

// 修改期间范围
export function PutDuration(params: any) {
    return fetchForm.post({
        url: '/api/ZWFinance/PutDuration',
        data: params
    })
}

// 一键解锁
export function AllUnlockData() {
    return fetchForm.post({
        url: '/api/ZWFinance/AllUnlockData',
        data: {}
    })
}

// 快速设置财务期间
export function PostDuration(params: any) {
    return fetchForm.post({
        url: '/api/ZWFinance/PostDuration',
        data: params
    })
}