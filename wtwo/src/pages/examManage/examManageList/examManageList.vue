<template>
    <div class="wtwo-exam-manage-list page-box">
        <!-- 考试列表区域 -->
        <div class="wtwo-flex-card-box">
            <div class="tool-bar">
                <div>共{{ page.TotalCount || 0 }}个考试</div>
                <div class="btn-wraper">
                    <el-dropdown trigger="click">
                        <el-button>
                            <el-icon size="20px">
                                <svg aria-hidden="true">
                                    <use xlink:href="#w2-gengduocaozuo"></use>
                                </svg>
                            </el-icon>
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item
                                    @click.native="setTableColumnsOpen"
                                    :disabled="userDefinedColumns.length == 0"
                                >
                                    列表字段显示设置
                                </el-dropdown-item>
                                <el-dropdown-item @click.native="exportExamManageList">
                                    导出
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template> 
                    </el-dropdown>
                    <el-button type="primary" @click="handleAdd">新增考试</el-button>
                </div>
            </div>
            <div
                class="table-wrap scroll-box"
                ref="tableContainerRef"
                v-loading="loading"
                element-loading-target=".table-wrap"
            >
            <!-- :max-height="`calc(-180px + 100vh)`" -->
                <el-table
                    :data="examList"
                    ref="customTable"
                    width="100%"
                    height="calc(100% - 40px)"
                    resizable
                    border
                    @selection-change="handleSelectionChange"
                >
                    <template #empty>
                        <el-empty
                            :image="globalData.emptyPng"
                            description="暂无数据"
                            :image-size="100"
                        ></el-empty>
                    </template>
                    <!-- 根据 visibleColumns 循环渲染，保持初始化顺序 -->
                    <template v-for="item in visibleColumns" :key="item.Key">
                        <!-- 考试名称（禁用列，固定左侧） -->
                        <el-table-column
                            v-if="item.Key == 'examName'"
                            fixed="left"
                            label="考试名称"
                            prop="examName"
                            key="examName"
                            min-width="160"
                        >
                            <template #default="scope">
                                <el-popover placement="top" :width="200" trigger="hover">
                                    <template #reference>
                                        <el-text
                                            type="primary"
                                            class="cursor-pointer exam-name-display"
                                            @click="showExamDetail(scope.row)"
                                        >
                                            {{ scope.row.examName }}
                                        </el-text>
                                    </template>
                                    <div class="popover-content">
                                        {{ scope.row.examName }}
                                    </div>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <el-table-column
                            v-if="item.Key == 'examType'"
                            label="考试类型"
                            prop="examType"
                            key="examType"
                            width="120"
                        >
                            <template #default="scope">
                                {{ (scope.row.examType && scope.row.examType.name) || '-' }}
                            </template>
                        </el-table-column>
                        
                        <!-- <el-table-column
                            v-if="item.Key == 'examModeText'"
                            label="考试模式"
                            prop="examModeText"
                            key="examModeText"
                            width="100"
                        >
                            <template #default="scope">
                                {{ scope.row.examModeText }}
                            </template>
                        </el-table-column> -->
                        
                        <el-table-column
                            v-if="item.Key == 'subjects'"
                            label="考试科目"
                            prop="subjects"
                            key="subjects"
                            width="120"
                        >
                            <template #default="scope">
                                {{ formatArrayItem(scope.row.subjects) }}
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'grades'"
                            label="考试年级"
                            prop="grades"
                            key="grades"
                            width="120"
                        >
                            <template #default="scope">
                                {{ formatArrayItem(scope.row.grades) }}
                            </template>
                        </el-table-column>
                        
                        <!-- 考试项目（禁用列） -->
                        <el-table-column
                            v-if="item.Key == 'examItems'"
                            label="考试项目"
                            prop="examItems"
                            key="examItems"
                            width="180"
                        >
                            <template #default="scope">
                                <el-popover placement="bottom" popper-class="popover-self-content" :width="150" trigger="hover" v-if="scope.row.examItems && scope.row.examItems.length > 0">
                                    <template #reference>
                                        <div class="exam-items-display">
                                            {{ formatExamItems(scope.row.examItems) }}
                                        </div>
                                    </template>
                                    <div class="popover-content-scroll">
                                        <div v-for="(item, index) in scope.row.examItems" :key="index" class="popover-item">
                                            {{ item.subjectName }}
                                            <span v-if="item.status ===0" class="deleted-tag">(已删除)</span>
                                        </div>
                                    </div>
                                </el-popover>
                                <div v-else class="exam-items-display">
                                    {{ formatExamItems(scope.row.examItems) }}
                                </div>
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'statistics'"
                            label="考试统计"
                            prop="statistics"
                            key="statistics"
                            width="440"
                            class-name="exam-statistics-table-column"
                        >
                            <template #default="scope">
                                <div class="exam-statistics-table" v-if="scope.row.statistics && scope.row.statistics.length > 0">
                                    <el-table :data="scope.row.statistics" :stripe="false" :header-row-style="headerRowStyle" cell-class-name="exam-statistics-table-cell" max-height="250" style="width: 100%">
                                        <el-table-column prop="subjectName" label="考试项目" width="80" show-overflow-tooltip />
                                        <el-table-column prop="scoredCount" label="已有成绩人数" width="100" >
                                            <template #default="{row}">
                                                {{ row.scoredCount || '-' }}
                                            </template>
                                        </el-table-column>
                                        <el-table-column prop="maxScore" label="最高分" width="80" >
                                            <template #default="{row}">
                                                {{ row.maxScore || '-' }}
                                            </template>
                                        </el-table-column>
                                        <el-table-column prop="minScore" label="最低分" width="80" >
                                            <template #default="{row}">
                                                {{ row.minScore || '-' }}
                                            </template>
                                        </el-table-column>
                                        <el-table-column prop="averageScore" label="平均分" width="80" >
                                            <template #default="{row}">
                                                {{ row.averageScore || '-' }}
                                            </template>
                                        </el-table-column>
                                    </el-table>
                                </div>
                                <div v-else>-</div>
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'examStudentCount'"
                            label="考试人数"
                            prop="examStudentCount"
                            key="examStudentCount"
                            width="120"
                        >
                            <template #default="scope">
                                {{ scope.row.examStudentCount || '-' }}
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'scoredStudentCount'"
                            label="已有成绩人数"
                            prop="scoredStudentCount"
                            key="scoredStudentCount"
                            width="120"
                        >
                            <template #default="scope">
                                {{ scope.row.scoredStudentCount || '-' }}
                            </template>
                        </el-table-column>
                        
                        <!-- 适用校区（禁用列） -->
                        <el-table-column
                            v-if="item.Key == 'campuses'"
                            label="适用校区"
                            prop="campuses"
                            key="campuses"
                            width="120"
                        >
                            <template #default="scope">
                                <el-popover placement="bottom" popper-class="popover-self-content" :width="150" trigger="click" v-if="scope.row.campuses && scope.row.campuses.length > 0">
                                    <template #reference>
                                        <span class="cursor-pointer color-#2878e8">
                                            {{scope.row.campuses && scope.row.campuses.length > 0 ? scope.row.campuses.length+'个校区' : '-'}}
                                        </span>
                                    </template>
                                    <div class="popover-content-scroll">
                                        <div v-for="(campus, index) in scope.row.campuses" :key="index" class="popover-item">
                                            {{ campus.name }}
                                        </div>
                                    </div>
                                </el-popover>
                                <span v-else>{{ formatArrayItem(scope.row.campuses) }}</span>
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'courses'"
                            label="适用课程"
                            prop="courses"
                            key="courses"
                            width="120"
                        >
                            <template #default="scope">
                                <el-popover placement="bottom" popper-class="popover-self-content" :width="150" trigger="click" v-if="scope.row.courses && scope.row.courses.length > 0">
                                    <template #reference>
                                        <span class="cursor-pointer color-#2878e8">
                                            {{scope.row.courses && scope.row.courses.length > 0 ? scope.row.courses.length+'个课程' : '-'}}
                                        </span>
                                    </template>
                                    <div class="popover-content-scroll">
                                        <div v-for="(course, index) in scope.row.courses" :key="index" class="popover-item">
                                            {{ course.name }}
                                        </div>
                                    </div>
                                </el-popover>
                                <span v-else>{{ formatArrayItem(scope.row.courses) }}</span>
                            </template>
                        </el-table-column>
                        
                        <!-- 考试日期（禁用列） -->
                        <el-table-column
                            v-if="item.Key == 'examDate'"
                            label="考试日期"
                            prop="examDate"
                            key="examDate"
                            width="180"
                        >
                            <template #default="scope">
                                {{ scope.row.examDate ? scope.row.examDate?.substring(0, 10) : '-' }}
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'publicTime'"
                            label="成绩公布时间"
                            prop="publicTime"
                            key="publicTime"
                            width="160"
                        >
                            <template #default="scope">
                                {{ scope.row.publicTime}}
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'createTime'"
                            label="创建时间"
                            prop="createTime"
                            key="createTime"
                            width="160"
                        >
                            <template #default="scope">
                                {{scope.row.createTime }}
                            </template>
                        </el-table-column>
                        
                        <el-table-column
                            v-if="item.Key == 'memo'"
                            label="备注"
                            prop="memo"
                            key="memo"
                            width="120"
                        >
                            <template #default="scope">
                                <el-popover placement="top" :width="200" trigger="hover">
                                    <template #reference>
                                        <el-text type="" class="cursor-pointer exam-name-display" >
                                            {{ scope.row.memo }}
                                        </el-text>
                                    </template>
                                    <div class="popover-content">
                                        {{ scope.row.memo }}
                                    </div>
                                </el-popover>
                            </template>
                        </el-table-column>
                    </template>
                    <el-table-column
                        label="操作"
                        prop="op"
                        key="op"
                        min-width="180"
                        fixed="right"
                    >
                        <template #default="scope">
                            <div class="table-action-btn flex-center">
                                <el-button
                                    type="primary"
                                    size="small"
                                    link
                                    @click="handleEdit(scope.row)"
                                >
                                    修改
                                </el-button>
                                <div class="colline"></div>
                                <el-button
                                    type="primary"
                                    size="small"
                                    link
                                    @click="handleInputScore(scope.row)"
                                >
                                    录入成绩
                                </el-button>
                                <div class="colline"></div>
                                <el-dropdown trigger="click">
                                    <el-button size="small" link>
                                        <el-icon size="20px" color="#2878e8">
                                            <svg aria-hidden="true">
                                                <use xlink:href="#w2-gengduo1"></use>
                                            </svg>
                                        </el-icon>
                                    </el-button>
                                    <template #dropdown>
                                        <el-dropdown-menu>
                                            <el-dropdown-item @click="handleCopy(scope.row)">
                                                <el-icon><CopyDocument /></el-icon>
                                                复制
                                            </el-dropdown-item>
                                            <el-dropdown-item @click="handleDelete(scope.row)" divided>
                                                <el-icon><Delete /></el-icon>
                                                删除
                                            </el-dropdown-item>
                                        </el-dropdown-menu>
                                    </template>
                                </el-dropdown>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
                <div
                    class="pageination-wrapper flex-end m-t-10px"
                    v-if="examList.length > 0"
                >
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        :current-page="page.PageIndex"
                        :page-sizes="[10, 20, 50, 100, 200]"
                        :page-size="page.PageSize"
                        layout="total, sizes, prev, jumper ,next"
                        :total="page.TotalCount"
                        size="small"
                    >
                    </el-pagination>
                </div>
            </div>
        </div>
        
        <!-- 新增考试抽屉 -->
        <AddExamForm ref="addExamFormRef" />

        <!-- 编辑考试抽屉 -->
        <EditExamForm ref="editExamFormRef" />

        <!-- 录入成绩抽屉 -->
        <InputScoreForm ref="inputScoreFormRef" />
        
        <!-- 考试详情弹窗 -->
        <ExamDetailDialog ref="examDetailDialogRef" />
        
        <!-- 列表字段显示设置 -->
        <setExamTableColumns ref="setTableColumnsRef" />
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, CopyDocument, Delete } from '@element-plus/icons-vue'
import { useCurrentCampuses, apiUrl } from '@/store'
import {
    queryExamList,
    deleteExam
} from '@/api/exam'
import { getUserColumns } from '@/api'
import { syncUserColumns, downloadFile } from '@/utils'
import { cloneDeep } from 'lodash'
import AddExamForm from '../popup/addExamForm.vue'
import EditExamForm from '../popup/editExamForm.vue'
import InputScoreForm from '../popup/inputScoreForm.vue'
import ExamDetailDialog from '../popup/examDetailDialog.vue'
import setExamTableColumns from '../popup/setExamTableColumns.vue'

// 获取全局数据
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

// 当前校区
const currentCampus = computed(() => {
    let campusList = useCurrentCampuses().campusList;
    return campusList ? campusList?.split(',') : []
})

// 表格头部样式
const headerRowStyle = {
    backgroundColor: '#fff',
}

// 默认列配置
const allColumns = [
    {
        Key: 'examName',
        Label: '考试名称',
        Visible: 1,
        ColumnKey: 'examName',
        Disabled: true  // 禁用列始终显示，不可修改
    },
    {
        Key: 'examType',
        Label: '考试类型',
        Visible: 1,
        ColumnKey: 'examType'
    },
    // {
    //     Key: 'examModeText',
    //     Label: '考试模式',
    //     Visible: 0,
    //     ColumnKey: 'examModeText'
    // },
    {
        Key: 'subjects',
        Label: '考试科目',
        Visible: 1,
        ColumnKey: 'subjects'
    },
    {
        Key: 'grades',
        Label: '考试年级',
        Visible: 1,
        ColumnKey: 'grades'
    },
    {
        Key: 'examItems',
        Label: '考试项目',
        Visible: 1,
        ColumnKey: 'examItems',
        Disabled: true  // 禁用列始终显示，不可修改
    },
    {
        Key: 'statistics',
        Label: '考试统计',
        Visible: 0,
        ColumnKey: 'statistics'
    },
    {
        Key: 'examStudentCount',
        Label: '考试人数',
        Visible: 1,
        ColumnKey: 'examStudentCount'
    },
    {
        Key: 'scoredStudentCount',
        Label: '已有成绩人数',
        Visible: 1,
        ColumnKey: 'scoredStudentCount'
    },
    {
        Key: 'campuses',
        Label: '适用校区',
        Visible: 1,
        ColumnKey: 'campuses',
        Disabled: true  // 禁用列始终显示，不可修改
    },
    {
        Key: 'courses',
        Label: '适用课程',
        Visible: 1,
        ColumnKey: 'courses'
    },
    {
        Key: 'examDate',
        Label: '考试日期',
        Visible: 1,
        ColumnKey: 'examDate',
        Disabled: true  // 禁用列始终显示，不可修改
    },
    {
        Key: 'publicTime',
        Label: '成绩公布时间',
        Visible: 1,
        ColumnKey: 'publicTime'
    },
    {
        Key: 'createTime',
        Label: '创建时间',
        Visible: 1,
        ColumnKey: 'createTime'
    },
    {
        Key: 'memo',
        Label: '备注',
        Visible: 1,
        ColumnKey: 'memo'
    }
]

// 用户自定义列配置
const userDefinedColumns = ref([] as any)

// 计算可见列（包含禁用列和动态列，保持原始顺序）
const visibleColumns = computed(() => {
    return userDefinedColumns.value.filter((item: any) => {
        // 禁用列始终显示，动态列根据 Visible 状态显示
        return item.Disabled || item.Visible
    })
})

// 列表字段显示设置组件引用
const setTableColumnsRef = ref<InstanceType<typeof setExamTableColumns> | null>(null)

// 打开列表字段显示设置
function setTableColumnsOpen() {
    let columns = cloneDeep(userDefinedColumns.value)
    setTableColumnsRef.value?.open({
        columns: columns,
        moduleId: 'examManage_table',
        orderMode: 'original'  // 使用原始顺序模式，保持初始化时的顺序
    }).then((res: any) => {
        userDefinedColumns.value = res.data ? cloneDeep(res.data) : []
        // 刷新列表
        emit('refreshExamList')
    })
}

// 加载状态
const loading = ref(false)


// 考试列表数据
const examList = ref([])

// 分页数据
const page = ref({
    TotalCount: 0,
    PageSize: 10,
    PageIndex: 1,
    PageCount: 1,
})

// 选中的行
const multipleSelection = ref([])

// 当前搜索条件
const currentSearchCondition = ref<any>({})

// 新增考试组件引用
const addExamFormRef = ref()

// 编辑考试组件引用
const editExamFormRef = ref()

// 录入成绩组件引用
const inputScoreFormRef = ref()

// 考试详情弹窗组件引用
const examDetailDialogRef = ref()

// 搜索方法，供父组件调用
function search(searchCondition: any) {
    console.log('接收到的搜索条件:', searchCondition)
    
    // 保存当前搜索条件
    currentSearchCondition.value = cloneDeep(searchCondition)
    
    // 如果列配置已初始化，直接查询
    if (userDefinedColumns.value.length > 0) {
        loadExamList(searchCondition)
    } else {
        // 首次加载，获取用户列配置
        loading.value = true
        getUserColumns({
            moduleId: 'examManage_table'
        }).then((res: any) => {
            let data = res.Data
            if (data && data.AllColumns) {
                let obj = JSON.parse(data.AllColumns)
                // 使用列同步工具函数处理列的同步
                userDefinedColumns.value = syncUserColumns(obj, allColumns)
            } else {
                // 如果没有保存的配置，使用默认配置
                userDefinedColumns.value = cloneDeep(allColumns)
            }
            loadExamList(searchCondition)
        }).catch(() => {
            loading.value = false
        })
    }
}

// 加载考试列表
const loadExamList = async (searchCondition: any = {}) => {
    console.log('searchCondition-----', searchCondition)
    loading.value = true
    try {
        const params = {
            ...searchCondition,
            PageIndex: page.value.PageIndex,
            PageSize: page.value.PageSize,
            // campusIds: currentCampus.value
        }
        delete params.PublicTime
        delete params.examDate
        
        console.log('发送的请求参数:', params)
        const res = await queryExamList(params)
        if (res.ErrorCode === 200) {
            examList.value = res.Data.List || []
            page.value.TotalCount = res.Data.TotalCount || 0
            page.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / page.value.PageSize)
        } else {
            ElMessage.error(res.ErrorMsg || '查询失败')
        }
    } catch (error) {
        console.error('加载考试列表失败:', error)
        ElMessage.error('查询失败')
    } finally {
        loading.value = false
    }
}

// 选择变化
const handleSelectionChange = (val: any) => {
    multipleSelection.value = val
}

// 新增考试
const handleAdd = () => {
    addExamFormRef.value?.open({}).then(() => {
        // 新增成功后刷新列表
        emit('refreshExamList')
    })
}

// 编辑考试
const handleEdit = (row: any) => {
    if (!row.id) {
        ElMessage.error('考试Id不存在')
        return
    }
    editExamFormRef.value?.open({ ExamID: row.id }).then(() => {
        // 编辑成功后触发父组件的 handleSearch 函数刷新考试列表
        emit('refreshExamList')
    }).catch((err: any) => {
        console.error('编辑考试失败:', err)
    })
}

// 录入成绩
const handleInputScore = (row: any) => {
    if (row.examItems && row.examItems.length > 0) {
        inputScoreFormRef.value?.open(row).then(() => {
            // 录入成绩成功后触发父组件的 handleSearch 函数刷新考试列表
            emit('refreshExamList')
        }).catch((err: any) => {
            console.error('录入成绩失败:', err)
        })
    }else{
        ElMessage.warning('本场考试还没有考试项暂不支持录入成绩，快去添加吧。')
    }
}

// 复制考试
const handleCopy = async (row: any) => {
    if (!row.id) {
        ElMessage.error('考试Id不存在')
        return
    }
    addExamFormRef.value?.open({ ExamID: row.id }).then(() => {
        // 复制成功后刷新列表
        emit('refreshExamList')
    }).catch((err: any) => {
        console.error('复制考试失败:', err)
    })
}

// 删除考试
const handleDelete = async (row: any) => {
    try {
        await ElMessageBox.confirm('确定删除该考试吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })

        const res = await deleteExam({ id: row.id })    
        if (res.ErrorCode === 200) {
            ElMessage.success('删除成功')
            emit('refreshExamList')
        } else {
            ElMessage.error(res.ErrorMsg || '删除失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除考试失败:', error)
            ElMessage.error('删除失败')
        }
    }
}

// 显示考试详情
const showExamDetail = (row: any) => {
    examDetailDialogRef.value?.open(row)
}

// 导出考试列表
const exportExamManageList = () => {
    try {
        // 构建导出参数，参考查询列表接口的参数结构
        const params: any = {
            ...currentSearchCondition.value,
        }
        
        // 删除分页参数
        delete params.PageIndex
        delete params.PageSize
        
        // 处理 examDate 和 publicTime 参数（如果存在）
        if (params.examDate) {
            delete params.examDate
        }
        if (params.PublicTime) {
            delete params.PublicTime
        }
        
        // 构建 exportColumn 字段
        // 根据可见列构建，如果未传则使用默认字段列表
        const exportColumns: string[] = []
        
        // 默认字段列表（参考需求表格格式）
        const defaultExportColumns = [
            'examName',
            'examType',
            'subjects',
            'grades',
            'examItems',
            'statistics',
            'examStudentCount',
            'scoredStudentCount',
            'campuses',
            'courses',
            'examDate',
            'publicTime',
            'createTime',
            'memo'
        ]
        
        // 根据可见列构建导出字段列表
        if (visibleColumns.value && visibleColumns.value.length > 0) {
            // 获取可见列的 ColumnKey
            visibleColumns.value.forEach((col: any) => {
                if (col.ColumnKey && defaultExportColumns.includes(col.ColumnKey)) {
                    exportColumns.push(col.ColumnKey)
                }
            })
        }
        
        // 如果根据可见列没有找到任何字段，使用默认字段列表
        if (exportColumns.length === 0) {
            exportColumns.push(...defaultExportColumns)
        }
        
        // 添加 exportColumn 参数
        params.exportColumn = exportColumns
        
        // 构建导出URL
        const downloadUrl = `${apiUrl}/api/exam/exams/export`
        
        // 生成文件名
        const now = new Date()
        const year = now.getFullYear()
        const month = String(now.getMonth() + 1).padStart(2, '0')
        const day = String(now.getDate()).padStart(2, '0')
        const hour = String(now.getHours()).padStart(2, '0')
        const minute = String(now.getMinutes()).padStart(2, '0')
        const fileName = `考试管理列表_${year}${month}${day}${hour}${minute}.xlsx`
        
        // 使用 downloadFile 工具函数下载文件
        downloadFile(downloadUrl, fileName, 'POST', params)
        
        ElMessage.success('导出任务已开始，请稍候...')
    } catch (error) {
        console.error('导出考试列表失败:', error)
        ElMessage.error('导出失败，请重试')
    }
}

// 格式化
const formatArrayItem = (items: string[], joinType = '，') => {
    if (!items || !Array.isArray(items) || items.length === 0) return '-';
    let itemNames = items.map((item: any) => item.name)
    // 这里需要根据ID获取对应的名称，暂时显示ID
    return itemNames.join(joinType)
}

// 格式化考试项
const formatExamItems = (items: string[]) => {
    if (!items || !Array.isArray(items) || items.length === 0) return '-';
    let itemNames = items.map((item: any) => item.subjectName)
    // 这里需要根据ID获取对应的名称，暂时显示ID
    return itemNames.join('、')
}

// 分页大小变化
const handleSizeChange = (val: number) => {
    page.value.PageSize = val
    page.value.PageIndex = 1
    emit('refreshExamList')
}

// 当前页变化
const handleCurrentChange = (val: number) => {
    page.value.PageIndex = val
    emit('refreshExamList')
}

// 组件挂载
onMounted(() => {
    // 初始化时触发一次搜索，会自动加载列配置
    // 这里不需要手动调用，因为父组件会调用 search 方法
})

// 定义组件事件
const emit = defineEmits(['refreshExamList'])

// 暴露方法给父组件
defineExpose({
    search,
})
</script>

<style lang="scss" scoped>
.wtwo-exam-manage-list {
    .wtwo-flex-card-box {
        overflow: hidden;
        height: calc(-193px - 10px + 100vh);
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
            flex: none;
            height: calc(100% - 60px);
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
.table-action-btn {
    .colline {
        margin: 0 12px;
        width: 1px;
        height: 12px;
        background-color: #D8D8D8;
    }
}

.exam-items-display {
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

.exam-name-display {
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

.popover-content-scroll {
    max-height: 300px;
    overflow: hidden;
    overflow-y: auto;
    
    .popover-item {
        padding: 10px 0px;
        margin: 0px 10px;

        line-height: 1.5;
        word-break: break-all;
        
        &:not(:last-child) {
            border-bottom: 1px solid #f0f0f0;
            padding: 10px 0px;
            margin: 0px 10px;
        }
        .deleted-tag{
            color: #f56c6c;
            font-size: 10px;
            margin-left: 4px;
        }
    }
}

// 嵌套表格头部背景色
.exam-statistics-table {
    :deep(.wtwo-table) {
        .wtwo-table__header-wrapper {
            .wtwo-table__header {
                th {
                    background-color: #fff !important;
                    padding-top: 4px;
                    padding-bottom: 4px;
                }
            }
        }
    }
    :deep(.wtwo-table--fit) {
        .wtwo-table__inner-wrapper::before {
            height: 0px !important;
        }
    }
}

// 考试统计列样式
:deep(.exam-statistics-table-column) {
    padding-top: 0!important;
    padding-bottom: 0px!important;
}

:deep(.exam-statistics-table-cell) {
    padding-top: 4px!important;
    padding-bottom: 4px!important;
}
</style>

<style lang="scss">
.popover-self-content {
    padding: 0 !important;
}
</style>