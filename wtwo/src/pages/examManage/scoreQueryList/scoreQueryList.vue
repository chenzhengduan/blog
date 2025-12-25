<template>
    <div class="wtwo-score-query-list page-box">
        <!-- 成绩查询列表区域 -->
        <div class="wtwo-flex-card-box">
            <div class="tool-bar">
                <div>共{{ page.TotalCount || 0 }}条成绩记录</div>
                <div class="btn-wraper">
                    <el-button type="primary" @click="handleExport">导出成绩</el-button>
                </div>
            </div>
            <div class="table-wrap scroll-box" ref="tableContainerRef" v-loading="loading"
                element-loading-target=".table-wrap">
                <el-table :data="scoreList" ref="customTable" width="100%" :max-height="`calc(-180px + 100vh)`"
                    resizable border @selection-change="handleSelectionChange">
                    <template #empty>
                        <el-empty :image="globalData.emptyPng" description="暂无数据"></el-empty>
                    </template>
                    <!-- <el-table-column fixed="left" type="selection" width="30" key="selection"></el-table-column> -->
                    <el-table-column fixed="left" label="学员名称" prop="studentName" key="studentName" min-width="120"
                        show-overflow-tooltip></el-table-column>
                    <el-table-column label="学员所属校区" prop="studentCampusName" key="studentCampusName" width="120"
                        show-overflow-tooltip></el-table-column>
                    <el-table-column label="考试名称" prop="examName" key="examName" min-width="160" show-overflow-tooltip></el-table-column>
                    <el-table-column label="考试类型" prop="examType" key="examType" width="120" show-overflow-tooltip>
                        <template #default="scope">
                            {{ (scope.row.examType && scope.row.examType.name) || '-' }}
                        </template>
                    </el-table-column>
                    <!-- <el-table-column label="考试模式" prop="examModeText" key="examModeText" width="120"></el-table-column> -->
                    <el-table-column label="考试科目" prop="examSubject" key="examSubject" width="120"
                        show-overflow-tooltip>
                        <template #default="scope">
                            {{ (scope.row.examSubject && scope.row.examSubject.name) || '-' }}
                        </template>
                    </el-table-column>
                    <!-- 暂时不使用学员年级筛选 -->
                    <!-- <el-table-column label="年级" prop="grade" key="grade" width="100">
                        <template #default="scope">
                            {{ (scope.row.grade && scope.row.grade.name) || '-' }}
                        </template>
                    </el-table-column> -->
                    <el-table-column prop="fullTimeSchool" label="公立学校" width="120" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ row.fullTimeSchool || '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column label="班级" prop="classInfo" key="classInfo" width="120" show-overflow-tooltip>
                        <template #default="scope">
                            {{ (scope.row.classInfo && scope.row.classInfo.name) || '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column label="考试项目" prop="examSubjectItem" key="examSubjectItem" width="120"
                        show-overflow-tooltip>
                        <template #default="scope">
                            {{ (scope.row.examSubjectItem && scope.row.examSubjectItem.name) || '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column label="成绩" prop="scoreShowInfo" key="scoreText" width="100">
                        <template #default="{ row, $index }">
                            <div class="score-cell" @mouseenter="handleScoreHover(row)" @mouseleave="handleScoreLeave(row)">
                                <span class="score-text">{{ row.scoreShowInfo || '-' }}</span>
                                <el-popover :visible="row.showPopover" placement="bottom-start"
                                    :width="320" @show="handlePopoverShow(row)"
                                    @hide="handlePopoverHide(row)">
                                    <template #reference>
                                        <el-icon @click="handleEditClick(row)" class="edit-icon edit-score-btn"
                                            :class="{ 'visible': row.showEditIcon }" size="16">
                                            <Edit />
                                        </el-icon>
                                    </template>
                                    <div v-loading="row.isSaving">
                                        <div class="mb-10px">修改成绩</div>
                                        <el-select v-if="row.scoreType && row.scoreType.code == 'TYPE_GRADE'" v-model="row.editScoreText" placeholder="请选择" style="width: 100%">
                                            <el-option v-for="item in examScoreGradeList" :key="item.ID" :value="item.ID" :label="item.Value"></el-option>
                                        </el-select>
                                        <el-input v-else v-model="row.editScoreText" placeholder="请输入"
                                            type="text" class="w-100%!" />
                                        <div class="flex justify-end mt-16px">
                                            <el-button @click="cancelScoreEdit(row)"
                                                :disabled="row.isSaving">取消</el-button>
                                            <el-button type="primary" @click="saveScoreEdit(row)"
                                                :loading="row.isSaving" :disabled="row.isSaving">
                                                {{ row.isSaving ? '保存中...' : '保存' }}
                                            </el-button>
                                        </div>
                                    </div>
                                </el-popover>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column label="班级排名" prop="classRank" key="classRank" width="100">
                        <template #default="scope">
                            {{ scope.row.scoreType && scope.row.scoreType.code == 'TYPE_SCORE' ? (scope.row.classRank?scope.row.classRank:'-') : '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column label="考试日期" prop="examDate" key="examDate" width="120">
                        <template #default="scope">
                            {{ formatExamDate(scope.row.examDate) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="memo" label="备注" width="120">
                        <template #default="{ row }">
                            <el-popover v-if="isTextOverflow(row.memo)" placement="top" :width="300" trigger="hover" popper-class="memo-popover">
                                <template #reference>
                                    <div class="memo-display">
                                        {{ row.memo || '-' }}
                                    </div>
                                </template>
                                <div class="popover-content">
                                    {{ row.memo || '-' }}
                                </div>
                            </el-popover>
                            <div v-else class="memo-display">
                                {{ row.memo || '-' }}
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" prop="op" key="op" min-width="120" fixed="right">
                        <template #default="{ row, $index }">
                            <div class="flex-center">
                                <el-button type="primary" link size="small"
                                    @click="handleDelete(row, $index)">删除</el-button>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="pageination-wrapper flex-end m-t-10px" v-if="scoreList.length > 0">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page="page.PageIndex" :page-sizes="[10, 20, 50, 100, 200]" :page-size="page.PageSize"
                        layout="total, sizes, prev, jumper ,next" :total="page.TotalCount" size="small">
                    </el-pagination>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCurrentCampuses, apiUrl } from '@/store'

import {
    queryScoreDetailsListExport,
    queryScoreDetailsList,
    updateScore,
    deleteScoreById,
    queryDictionaryList
} from '@/api/exam'
import { Edit } from '@element-plus/icons-vue'
import { downloadFile } from '@/utils'

const ExamScoreExport = window.$xgj.op('ExamScoreExport'); // 是否允许导出成绩

// 获取全局数据
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

// 当前校区
const currentCampus = computed(() => {
    return useCurrentCampuses().campusList
})

// 加载状态
const loading = ref(false)

// 成绩列表数据
const scoreList = ref([])

// 分页数据
const page = ref({
    TotalCount: 0,
    PageSize: 10,
    PageIndex: 1,
    PageCount: 1,
})

// 选中的行
const multipleSelection = ref([])
const searchCondition = ref({})

// 字典值相关
const examScoreGradeList = ref<any[]>([])

// 获取考试分数等级字典值
const loadExamScoreGradeDict = async () => {
    try {
        console.log('开始获取EXAM_SCORE_GRADE字典值')
        
        const res = await queryDictionaryList({
            Type: 'EXAM_SCORE_GRADE'
        })
        
        console.log('EXAM_SCORE_GRADE字典值响应:', res)
        
        if (res.ErrorCode === 200 && res.Data) {
            examScoreGradeList.value = res.Data.List || []
            console.log('EXAM_SCORE_GRADE字典值加载成功:', examScoreGradeList.value)
        } else {
            console.log(res.ErrorMsg || '获取EXAM_SCORE_GRADE字典值失败')
            examScoreGradeList.value = []
        }
    } catch (error) {
        console.error('获取EXAM_SCORE_GRADE字典值失败:', error)
        examScoreGradeList.value = []
    }
}

// 搜索方法，供父组件调用
function search(condition: any) {
    console.log('接收到的搜索条件:', condition)
    searchCondition.value = condition
    // 这里可以根据搜索条件进行查询
    loadScoreList()
}

// 加载成绩列表
const loadScoreList = async () => {
    loading.value = true
    try {
        const params = {
            ...searchCondition.value,
            PageIndex: page.value.PageIndex,
            PageSize: page.value.PageSize,
        }

        const res = await queryScoreDetailsList(params)
        if (res.ErrorCode === 200) {
            const items = res.Data.List || []
            // 为每行数据添加编辑相关的属性
            scoreList.value = items.map((item: any) => ({
                ...item,
                showEditIcon: false,
                showPopover: false,
                editScoreText: '',
                isSaving: false
            }))
            page.value.PageSize = res.Data.PageSize || 0
            page.value.PageIndex = res.Data.PageIndex || 0
            page.value.TotalCount = res.Data.TotalCount || 0
            page.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / page.value.PageSize)
        } else {
            ElMessage.error(res.ErrorMsg || '查询失败')
        }
    } catch (error) {
        console.error('加载成绩列表失败:', error)
        ElMessage.error('查询失败')
    } finally {
        loading.value = false
    }
}

// 选择变化
const handleSelectionChange = (val: any) => {
    multipleSelection.value = val
}

// 导出成绩
const handleExport = () => {
    if(!ExamScoreExport){
        ElMessage.warning('暂无导出权限')
        return
    }
    const params = {
        ...searchCondition.value,
        PageIndex: page.value.PageIndex,
        PageSize: page.value.PageSize,
    }
    // 构建导出URL，参考 inputScoreForm.vue 的 handleDownloadTemplate
    const downloadUrl = `${apiUrl}/api/exam/scores/queryListExport`

    // 获取年月日时分
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    const day = now.getDate()
    const hour = now.getHours()
    const minute = now.getMinutes()
    const fileName = `成绩明细表_${year}${month}${day}${hour}${minute}.xlsx`
    // 使用 downloadFile 工具函数下载文件
    downloadFile(downloadUrl, fileName,'POST',params)
    // queryScoreDetailsListExport(params).then((res: any) => {
    //     console.log('导出成绩明细响应:', res)
    //     if (res.ErrorCode === 200) {
    //         ElMessage.success('导出成功')
    //     } else {
    //         ElMessage.error(res.ErrorMsg || '导出失败')
    //     }
    // }).catch((error: any) => {
    //     console.error('导出成绩明细失败:', error)
    //     ElMessage.error('导出失败，请重试')
    // })
}


// 成绩编辑相关方法
function handleScoreHover(row: any) {
    row.showEditIcon = true
}

function handleScoreLeave(row: any) {
    console.log('handleScoreLeave', row)
    // 如果 popover 正在显示，不要隐藏编辑图标
    if (!row.showPopover) {
        row.showEditIcon = false
    }
}

function handlePopoverShow(row: any) {
    if (row.scoreType && row.scoreType.code == 'TYPE_GRADE') {
        row.editScoreText = row.scoreGradeId || ''
        return
    }
    if (row.scoreType && row.scoreType.code == 'TYPE_TEXT') {
        row.editScoreText = row.scoreText || ''
        return
    }
    row.editScoreText = row.score || ''
}

function handlePopoverHide(row: any) {
    console.log('handlePopoverHide', row)
    // 重置编辑数据
    row.editScoreText = row.score || ''
    // 隐藏编辑图标
    row.showEditIcon = false
}

// 处理编辑图标点击事件
function handleEditClick(row: any) {
    console.log('点击编辑图标:', row)
    
    // 关闭其他行的编辑状态，确保同一时间只有一个编辑框打开
    scoreList.value.forEach((item: any) => {
        if (item.scoreId !== row.scoreId) {
            item.showPopover = false
            item.showEditIcon = false
        }
    })
    
    // 切换当前行的编辑状态
    row.showPopover = !row.showPopover
    
    // 如果打开编辑框，初始化编辑数据
    if (row.showPopover) {
        if (row.scoreType && row.scoreType.code === 'TYPE_GRADE') {
            row.editScoreText = row.scoreGradeId || ''
        } else if(row.scoreType && row.scoreType.code === "TYPE_TEXT"){
            row.editScoreText = row.scoreText || ''
        }else{
            row.editScoreText = row.score || ''
        }
        console.log('打开编辑框，初始化数据:', row.editScoreText)
    } else {
        console.log('关闭编辑框')
    }
}

function saveScoreEdit(row: any) {
    console.log('saveScoreEdit', row)
    // 添加loading状态
    row.isSaving = true

    // 验证成绩格式
    const scoreStr = String(row.editScoreText || '').trim()
    const scoreRegex = /(?:^\d{1,8}\.\d{0,2}$)|(?:^\d{0,8}$)/
    // 等级类型和文本类型不进行格式验证
    if (row.scoreType && row.scoreType.code !== 'TYPE_GRADE' && row.scoreType.code !== 'TYPE_TEXT') {
        if (scoreStr && !scoreRegex.test(scoreStr)) {
            ElMessage.error('成绩格式不正确，请输入数字，最多8位整数和2位小数')
            row.isSaving = false
            return
        }
    }

    const requestData = {
        scoreId: row.scoreId,
        score: scoreStr
    }

    console.log('保存成绩请求参数:', requestData)

    updateScore(requestData).then((res: any) => {
        console.log('保存成绩响应:', res)
        if (res.ErrorCode === 200) {
            row.showPopover = false
            row.showEditIcon = false
            row.editScoreText = ''
            loadScoreList()
            ElMessage.success('成绩已保存')
        } else {
            ElMessage.error(res.ErrorMsg || '保存失败')
        }
    }).catch((error: any) => {
        console.error('保存成绩失败:', error)
        ElMessage.error('保存失败，请重试')
    }).finally(() => {
        row.isSaving = false
    })
}

function cancelScoreEdit(row: any) {
    console.log('取消成绩编辑:', row)
    row.showPopover = false
    row.showEditIcon = false
    row.editScoreText = ''
}

// 删除成绩
const handleDelete = (row: any, index: number) => {
    console.log('删除成绩:', row, index)

    // 确认删除操作
    ElMessageBox.confirm(
        `确定要删除学员 ${row.studentName} 的成绩记录吗？删除后无法恢复。`,
        '删除确认',
        {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        // 执行删除操作
        deleteScoreById({ scoreId: row.scoreId }).then((res: any) => {
            console.log('删除成绩响应:', res)
            if (res.ErrorCode === 200) {
                loadScoreList()
                ElMessage.success('删除成功')
            } else {
                ElMessage.error(res.ErrorMsg || '删除失败')
            }
        }).catch((error: any) => {
            console.error('删除成绩失败:', error)
            ElMessage.error('删除失败，请重试')
        })

    }).catch(() => {
        // 取消删除
        console.log('取消删除操作')
    })
}

// 判断文本是否会溢出（基于列宽和字体大小的估算）
const isTextOverflow = (text: string) => {
    if (!text || text === '-') return false
    // 备注列宽度为 120px，考虑 padding（约 16px），实际可用宽度约 104px
    // 字体大小约 14px，中文字符宽度约 14px，英文字符约 7px
    // 单行大约可以显示：104 / 14 ≈ 7-8 个中文字符
    // 两行（line-clamp: 2）大约可以显示：7 * 2 = 14 个中文字符
    // 考虑到可能有英文、数字等，保守估算为 30 个字符
    const maxChars = 30
    return text.length > maxChars
}

// 格式化考试日期
const formatExamDate = (date: string) => {
    if (!date) return '-'
    return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化创建时间
const formatCreateTime = (time: string) => {
    if (!time) return '-'
    return new Date(time).toLocaleString('zh-CN')
}

// 分页大小变化
const handleSizeChange = (val: number) => {
    page.value.PageSize = val
    page.value.PageIndex = 1
    loadScoreList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
    page.value.PageIndex = val
    loadScoreList()
}

// 组件挂载
onMounted(() => {
    // 获取考试分数等级字典值
    loadExamScoreGradeDict()
})

// 暴露方法给父组件
defineExpose({
    search,
})
</script>

<style lang="scss" scoped>
.wtwo-score-query-list {
    .wtwo-flex-card-box {
        .tool-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            color: #606266;

            .btn-wraper {
                display: flex;
                gap: 8px;
            }
        }

        .table-wrap {
            position: relative;
            isolation: isolate;
        }
    }
}

.pageination-wrapper {
    margin-top: 16px;
}

.flex-center {
    display: flex;
    align-items: center;
}

.flex-end {
    display: flex;
    justify-content: flex-end;
}

.cursor-pointer {
    cursor: pointer;
}

.m-t-10px {
    margin-top: 10px;
}

// 成绩样式
.score-excellent {
    color: #67c23a;
    font-weight: bold;
}

.score-good {
    color: #409eff;
    font-weight: bold;
}

.score-pass {
    color: #e6a23c;
    font-weight: bold;
}

.score-fail {
    color: #f56c6c;
    font-weight: bold;
}

// 成绩单元格样式
.score-cell {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;

    .score-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .edit-icon {
        color: #409eff;
        cursor: pointer;
        font-size: 14px;
        flex-shrink: 0;
        opacity: 0;
        transition: opacity 0.3s ease;
    }

    .edit-score-btn {
        opacity: 0 !important;
        transition: opacity 0.2s ease;
        margin-left: 4px;
        flex-shrink: 0;
    }

    .edit-icon.visible {
        opacity: 1 !important;
    }

    &:hover .edit-icon {
        opacity: 1 !important;
    }
}

// 使用 CSS hover 实现悬浮效果
:deep(.wtwo-table__body tr:hover) {
    .score-cell {
        .edit-score-btn {
            opacity: 1 !important;
        }
    }
}

// 备用选择器
:deep(.wtwo-table tbody tr:hover) {
    .score-cell {
        .edit-score-btn {
            opacity: 1 !important;
        }
    }
}

// 操作按钮样式
.colline {
    margin: 0 12px;
    width: 1px;
    height: 12px;
    background-color: #D8D8D8;
}

// 备注显示样式
.memo-display {
    max-height: 2.4em; /* 最多显示两行 */
    line-height: 1.2em;
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2; /* 限制显示两行 */
    line-clamp: 2; /* 标准属性 */
    word-break: break-all;
    text-overflow: ellipsis;
}

.popover-content {
    word-break: break-all;
    line-height: 1.5;
}
</style>

<style lang="scss">
// 备注 popover 样式（全局样式，因为 popover 插入到 body 中）
.memo-popover {
    background-color: #303133 !important;
    color: #fff !important;
    border-color: #303133 !important;
    
    .popover-content {
        color: #fff !important;
    }
    // 箭头样式 - 使用更高优先级的选择器，直接选择所有可能的箭头元素
    .wtwo-popper__arrow::before,
    .el-popper__arrow::before,
    .wtwo-popover__arrow::before,
    .el-popover__arrow::before,
    .wtwo-popper__arrow::after,
    .el-popper__arrow::after,
    .wtwo-popover__arrow::after,
    .el-popover__arrow::after {
        border-color: #303133 !important;
        background-color: #303133 !important;
    }
}

</style>