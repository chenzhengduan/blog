import { apiUrl } from '../store';
import fetch from './http';

// 获取考试项列表
export function queryExamSubjectsList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/exams/subjects`,
        data,
    });
}

// 获取字典列表 （支持分页和搜索）
export function queryDictionaryList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/dictionary/list`,
        data,
    });
}
// 查询考试列表
export function queryExamList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/exams/list`,
        // url: '/exam/exams/list',
        data,
    });
}

// 创建考试
export function createExam(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/exams/create`,
        data,
    });
}

// 获取考试详情
export function getExamDetail(data: any) {
    const { id } = data
    return fetch.get({
        url: `${apiUrl}/api/exam/exams/detail?id=${id}`,
    });
}

// 更新考试
export function updateExam(data: any) {
    const { id, ...bodyData } = data
    return fetch.put({
        url: `${apiUrl}/api/exam/exams/update?id=${id}`,
        data: bodyData,
    });
}

// 删除考试
export function deleteExam(data: any) {
    const { id } = data
    return fetch.delete({
        url: `${apiUrl}/api/exam/exams/delete?id=${id}`,
    });
}

// 根据学员获取班级
export function getClassesByStudents(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/students/classes`,
        data,
    });
}

// 根据班级获取学员
export function getStudentsByClasses(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/classes/students`,
        data,
    });
}

// 批量录入/修改成绩
export function batchSaveScore(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/batch`,
        data,
    });
}

// 学员简要查询（分页）
export function queryStudentBreifExam(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/students/brief-query`,
        data,
    });
}

//获取年级列表
export function getGradeList(params:any) {
    return fetch.get({
        url: '/api/Student/GetGrade',
        data: params
    })
}

// 下载导入模板
export function getTemplate(params: any) {
    return fetch.get({
        url: `${apiUrl}/api/exam/scores/template`,
        data: params
    });
}

// 批量查询学员成绩录入状态
export function batchCheckScoreStatus(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/check-status`,
        data,
    });
}
// 解析Excel成绩文件
export function parseExcelScoreFile(data: FormData) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/parse-excel`,
        data,
    });
}
// 解析Excel成绩文件 oss
export function parseExcelScoreFileOSS(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/parse-excel`,
        data,
    });
}

// 查询成绩明细列表
export function queryScoreDetailsList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/details`,
        data,
    });
}

// 考试成绩分析
export function examScoreAnalysis(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/analysis`,
        data,
    });
}

// 考试成绩统计列表
export function examScoreStatisticsList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/statistics`,
        data,
    });
}

// 新增成绩统计区间
export function examScoreStatisticsAdd(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/score-ranges/create`,
        data,
    });
}
// 修改成绩统计区间
export function examScoreStatisticsUpdate(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/score-ranges/update`,
        data,
    });
}
// 删除成绩统计区间
export function examScoreStatisticsDelete(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/score-ranges/delete`,
        data,
    });
}
// 成绩明细-修改成绩
export function updateScore(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/update`,
        data,
    });
}
// 按成绩ID删除成绩
export function deleteScoreById(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/delete-by-id`,
        data,
    });
}
// 成绩明细列表导出
export function exportScoreDetails(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/detailsExport`,
        data,
    });
}
// 成绩查询列表导出
export function queryScoreDetailsListExport(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/scores/queryListExport`,
        data,
    });
}
// 查询考试关联的班级列表
export function queryExamClassesList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/exams/classes`,
        data,
    });
}
// 查询学员成绩记录
export function queryStudentScoreRecord(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/studentScore/scoreRecord`,
        data,
    });
}
// 查询学员成绩总览
export function queryStudentScoreOverview(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/studentScore/scoreOverview`,
        data,
    });
}
// 查询学员考试项目列表
export function queryStudentExamSubjects(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/studentScore/examSubjects`,
        data,
    });
}
// 查询学员成绩曲线
export function queryStudentScoreCurve(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/studentScore/scoreCurve`,
        data,
    });
}
// 查询班级平均分曲线
export function queryStudentClassAverageScoreCurve(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/studentScore/classAverageScoreCurve`,
        data,
    });
}
// 导出考试列表
export function exportExamManageList(data: any) {
    return fetch.post({
        url: `${apiUrl}/api/exam/exams/export`,
        data,
    });
}






