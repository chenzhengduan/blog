<template>
    <div class="stuScoreManage">
        <div class="header flex items-center">
        <div class="button-group">
            <el-button :type="tabsType === 'record' ? 'primary' : 'default'" @click="handleFilterChange('record')"
                size="small">
                成绩记录
            </el-button>
            <el-button :type="tabsType === 'analysis' ? 'primary' : 'default'" @click="handleFilterChange('analysis')"
                size="small">
                成绩分析
            </el-button>
        </div>
            <el-select v-if="tabsType === 'analysis'" v-model="subjectId" placeholder="请选择考试项目"
                style="width: 280px;margin-bottom: 20px;" @change="handleExamItemChangeForm">
                <el-option v-for="item in examItems" :key="item.Id" :value="item.Id" :label="item.Name"></el-option>
                <template #empty>
                    <div v-if="examItemsLoading" class="loading-text">加载中...</div>
                    <div v-else-if="examItems.length === 0" class="empty-text">暂无数据</div>
                </template>
                <template #footer>
                    <div v-if="hasMoreExamItems" v-infinite-scroll="loadMoreExamItems" 
                        :infinite-scroll-disabled="examItemsLoading" 
                        :infinite-scroll-distance="10"
                        class="infinite-scroll-footer">
                        <div v-if="examItemsLoading" class="loading-more">加载中...</div>
                        <div v-else class="load-more-text">
                            已加载 {{ examItems.length }}/{{ examItemsPageInfo.TotalCount }} 条，滚动加载更多
                        </div>
                    </div>
                    <div v-else-if="examItems.length > 0" class="load-complete">
                        已加载历史参考的 {{ examItems.length }} 个考试项目
                    </div>
                </template>
            </el-select>

        </div>
        <div class="tabsType-content" v-loading="loading">
            <div class="score-detail-table record" v-show="tabsType === 'record'">
                <el-table :data="scoreDetailData" style="width: 100%" height="600px">
                    <el-table-column prop="examName" label="考试名称" width="220" fixed="left" show-overflow-tooltip />
                    <el-table-column prop="examType" label="考试类型" width="150" >
                        <template #default="{ row }">
                            {{ row.examType && row.examType.name || '-' }}
                        </template>
                    </el-table-column>
                    <!-- <el-table-column prop="examMode" label="考试模式" width="120" >
                        <template #default="{ row }">
                            {{ row.examModeText }}
                        </template>
                    </el-table-column> -->
                    <el-table-column prop="examSubject" label="考试科目" width="120" show-overflow-tooltip>
                        <template #default="{ row }">
                            {{ row.examSubject && row.examSubject.name || '-' }}
                        </template>
                    </el-table-column>
                    <!-- 暂时不使用学员年级筛选 -->
                    <!-- <el-table-column prop="grade" label="年级" width="120" >
                        <template #default="{ row }">
                            {{ row.grade && row.grade.name || '-' }}
                        </template>
                    </el-table-column> -->
                    <el-table-column prop="examSubjectItem" label="考试项目" show-overflow-tooltip >
                        <template #default="{ row }">
                            {{ row.examSubjectItem && row.examSubjectItem.name || '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="scoreShowInfo" label="成绩" width="120" >
                        <template #default="{ row }">
                            {{ row.scoreShowInfo || '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="classRank" label="班级排名" width="120" >
                        <template #default="{ row }">
                            {{ row.scoreType && row.scoreType.code == 'TYPE_SCORE' ? row.classRank : '-' }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="examDate" label="考试日期" width="120" show-overflow-tooltip >
                        <template #default="{ row }">
                            {{ row.examDate || '-'}}
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
                    <!-- <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row, $index }">
                        <div class="table-action-btn">
                            <el-button type="primary" link size="small"
                                @click="handleEditScore(row, $index)">修改</el-button>
                            <div class="colline"></div>
                            <el-button type="primary" link size="small"
                                @click="handleDeleteScore(row, $index)">删除</el-button>
                        </div>
                    </template>
                </el-table-column> -->
            </el-table>

            <!-- 分页组件 -->
            <div class="pagination-wrapper" v-if="scoreDetailData.length > 0">
                <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    :current-page="page.PageIndex" :page-sizes="[10, 20, 50, 100, 200]" :page-size="page.PageSize"
                    layout="total, sizes, prev, jumper, next" :total="page.TotalCount" size="small" />
            </div>
            </div>
            <div class="analysis" v-show="tabsType === 'analysis'">
                <div class="analysis-total">
                    <div class="analysis-total-title">成绩总览</div>
                    <div class="analysis-score-content" style="height: 55px;" v-show="currentExamItem && (currentExamItem.scoreTypeCode =='TYPE_TEXT' || currentExamItem.scoreTypeCode =='TYPE_GRADE')">
                        该考试项目成绩类型暂不支持显示成绩总览。
                    </div>
                    <div class=" analysis-score-content" v-show="(currentExamItem && (currentExamItem.scoreTypeCode !='TYPE_TEXT' && currentExamItem.scoreTypeCode !='TYPE_GRADE')) || !currentExamItem">
                        <div class="analysis-score-content-item">
                            <span class="label">考试次数</span>
                            <span class="value" v-if="analysisLoading">{{ (analysisData.examCount || analysisData.examCount==0) ? (analysisData.examCount !='-'?analysisData.examCount+'次':'-') :'-'}}</span>
                            <el-skeleton :rows="1" animated v-else>
                                <template #template>
                                    <div class="flex items-center justify-center h18px">
                                        <el-skeleton-item variant="text" style="width:30px;" />
                                    </div>
                                </template>
                            </el-skeleton>
                        </div>
                        <div class="analysis-score-content-item">
                            <span class="label">平均成绩</span>
                            <span class="value" v-if="analysisLoading">{{ (analysisData.averageScore || analysisData.averageScore==0) ? analysisData.averageScore :'-'}}</span>
                            <el-skeleton :rows="1" animated v-else>
                                <template #template>
                                    <div class="flex items-center justify-center h18px">
                                        <el-skeleton-item variant="text" style="width:30px;" />
                                    </div>
                                </template>
                            </el-skeleton>
                        </div>
                        <div class="analysis-score-content-item">
                            <span class="label">最高成绩</span>
                            <span class="value" v-if="analysisLoading">{{ (analysisData.highestScore || analysisData.highestScore==0) ? analysisData.highestScore :'-'}}</span>
                            <el-skeleton :rows="1" animated v-else>
                                <template #template>
                                    <div class="flex items-center justify-center h18px">
                                        <el-skeleton-item variant="text" style="width:30px;" />
                                    </div>
                                </template>
                            </el-skeleton>
                        </div>
                        <div class="analysis-score-content-item">
                            <span class="label">最低成绩</span>
                            <span class="value" v-if="analysisLoading">{{ (analysisData.lowestScore || analysisData.lowestScore==0) ? analysisData.lowestScore :'-'}}</span>
                            <el-skeleton :rows="1" animated v-else>
                                <template #template>
                                    <div class="flex items-center justify-center h18px">
                                        <el-skeleton-item variant="text" style="width:30px;" />
                                    </div>
                                </template>
                            </el-skeleton>
                        </div>

                    </div>
                    
                </div>
                <!-- 平滑折线图 -->
                <div class="line-smooth">
                    <div class="line-chart-title">成绩曲线</div>
                    <div style="width: 100%;height: 444px;" class="flex items-center justify-center" v-loading="lineChartLoading" v-show="!currentExamItem">
                        <el-empty description="请选择需要分析的考试项目。" />
                    </div>
                    <div style="width: 100%;height: 444px;" class="flex items-center justify-center" v-loading="lineChartLoading" v-show="currentExamItem && (currentExamItem.scoreTypeCode =='TYPE_TEXT' || currentExamItem.scoreTypeCode =='TYPE_GRADE')">
                        该考试项目成绩类型暂不支持显示成绩曲线。
                    </div>
                    <div ref="lineChartRef" style="width: 100%;height: 444px;" class="line-chart" v-loading="lineChartLoading" v-show="(currentExamItem && (currentExamItem.scoreTypeCode !='TYPE_TEXT' && currentExamItem.scoreTypeCode !='TYPE_GRADE'))"></div>
                </div>
            </div>

        </div>
    </div>
</template>
<script setup lang="ts">
import { onMounted, ref, nextTick, onUnmounted } from 'vue';
import { queryStudentScoreRecord, queryStudentScoreOverview, queryStudentExamSubjects , queryStudentScoreCurve, queryStudentClassAverageScoreCurve } from '@/api/exam';
import { ElMessage, ElMessageBox } from 'element-plus';
import { assignPage, checkPageIndex, IPageModel } from '@/utils';
import * as echarts from 'echarts';

const studentInfo = ref<any>(null);

// 分析筛选类型
const tabsType = ref('record')

// 考试项相关
const subjectId = ref('')
const currentExamItem = ref<any>(null)
const examItems = ref<any[]>([])
const examItemsLoading = ref(false)
const examItemsPageInfo = ref({
    PageIndex: 1,
    PageSize: 20,
    TotalCount: 0,
    PageCount: 1
})
const hasMoreExamItems = ref(true)

// 成绩明细表格数据
const scoreDetailData = ref<any[]>([])

// 分页相关
const page = ref({
    TotalCount: 0,    // 总条数
    PageSize: 10,     // 每页条数
    PageIndex: 1,     // 第几页
    PageCount: 1      // 总页数
} as IPageModel)

// 加载状态
const loading = ref(false)
const lineChartLoading = ref(false)

// 成绩分析
const analysisLoading = ref(false)
const analysisData = ref<any>({
    examCount: '-',
    averageScore: '-',
    highestScore: '-',
    lowestScore: '-'
})

// 折线图数据
const lineChartData = ref({
    studentScores: [],
    classAverages: []
})

// 折线图相关
const lineChartRef = ref<HTMLElement>()
let lineChart: echarts.ECharts | null = null

// 初始化考试项数据加载
const loadInitialExamItems = async () => {
    if (!studentInfo.value || !studentInfo.value.ID) {
        console.log('学员信息不完整，无法加载考试项目')
        return
    }

    try {
        examItemsLoading.value = true
        
        // 重置分页信息
        examItemsPageInfo.value = {
            PageIndex: 1,
            PageSize: 20,
            TotalCount: 0,
            PageCount: 1
        }
        hasMoreExamItems.value = true

        const res = await queryStudentExamSubjects({
            studentId: studentInfo.value.ID,
            PageIndex: examItemsPageInfo.value.PageIndex,
            PageSize: examItemsPageInfo.value.PageSize
        })


        if (res.ErrorCode === 200 && res.Data) {
            const initialItems = res.Data.List || []
            examItems.value = initialItems.map((item: any) => ({
                ...item,
                Id: item.subjectId,
                Name: item.subjectName
            }))
            
            // 更新分页信息
            examItemsPageInfo.value.TotalCount = res.Data.TotalCount || 0
            examItemsPageInfo.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / examItemsPageInfo.value.PageSize)
            
            // 判断是否还有更多数据
            hasMoreExamItems.value = examItemsPageInfo.value.PageIndex < examItemsPageInfo.value.PageCount
            
        } else {
            examItems.value = []
        }
    } catch (error) {
        examItems.value = []
    } finally {
        examItemsLoading.value = false
    }
}

// 加载更多考试项数据
const loadMoreExamItems = async () => {
    if (examItemsLoading.value || !hasMoreExamItems.value) {
        return
    }

    try {
        examItemsLoading.value = true
        examItemsPageInfo.value.PageIndex += 1

        const res = await queryStudentExamSubjects({
            studentId: studentInfo.value.ID,
            PageIndex: examItemsPageInfo.value.PageIndex,
            PageSize: examItemsPageInfo.value.PageSize
        })

        if (res.ErrorCode === 200 && res.Data) {
            const newItems = res.Data.List || []
            const oldCount = examItems.value.length
            
            // 数据累加：将新数据追加到现有数据后面
            const mappedNewItems = newItems.map((item: any) => ({
                ...item,
                Id: item.Id,
                Name: item.subjectName
            }))
            examItems.value = [...examItems.value, ...mappedNewItems]
            
            // 更新分页信息
            examItemsPageInfo.value.TotalCount = res.Data.TotalCount || 0
            examItemsPageInfo.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / examItemsPageInfo.value.PageSize)
            
            // 判断是否还有更多数据
            hasMoreExamItems.value = examItemsPageInfo.value.PageIndex < examItemsPageInfo.value.PageCount
            
        } else {
            // 如果加载失败，回退页码
            examItemsPageInfo.value.PageIndex -= 1
        }
    } catch (error) {
        // 如果加载失败，回退页码
        examItemsPageInfo.value.PageIndex -= 1
    } finally {
        examItemsLoading.value = false
    }
}

// 加载图表数据并初始化折线图
const loadChartDataAndInit = async () => {
    if (!studentInfo.value || !studentInfo.value.ID || !subjectId.value) {
        console.log('学员信息或考试项目不完整，无法加载图表数据')
        // 即使出错也要初始化图表，避免空白
        nextTick(() => {
            setTimeout(() => {
                initLineChart()
            }, 100)
        })
        return
    }
    
    try {
        lineChartLoading.value = true
        // 并行加载两个曲线数据
        const [scoreCurveRes, classAverageRes] = await Promise.all([
            queryStudentScoreCurve({
                PageIndex: 1,
                PageSize: 1000,
                studentId: studentInfo.value.ID,
                subjectId: subjectId.value
            }),
            queryStudentClassAverageScoreCurve({
                studentId: studentInfo.value.ID,
                subjectId: subjectId.value
            })
        ])
        
        console.log('学员成绩曲线数据:', scoreCurveRes)
        console.log('班级平均分曲线数据:', classAverageRes)
        
        // 处理学员成绩曲线数据
        if (scoreCurveRes.ErrorCode === 200 && scoreCurveRes.Data && scoreCurveRes.Data.List) {
            lineChartData.value.studentScores = scoreCurveRes.Data.List.map((item: any) => ({
                name: item.examName,
                examDate: item.examDate || '',
                value: item.score
            }))
            console.log('lineChartData.value.studentScores:', lineChartData.value.studentScores)
        } else {
            lineChartData.value.studentScores = []
        }
        
        // 处理班级平均分曲线数据
        if (classAverageRes.ErrorCode === 200 && classAverageRes.Data && classAverageRes.Data.List) {
            lineChartData.value.classAverages = classAverageRes.Data.List.map((item: any) => ({
                name: item.examName,
                examDate: item.examDate || '',
                value: item.averageScore
            }))
        } else {
            lineChartData.value.classAverages = []
        }
        
        // 两个请求都完成后，初始化折线图
        nextTick(() => {
            lineChartLoading.value = false
            setTimeout(() => {
                initLineChart()
            }, 100)
        })
        
    } catch (error) {
        // 即使出错也要初始化图表，避免空白
        nextTick(() => {
            setTimeout(() => {
                initLineChart()
            }, 100)
        })
    }
}

// 处理考试项变化
const handleExamItemChangeForm = (examItemId: string) => {
    console.log('选中的考试项ID:', examItemId)
    let currentExamItemFind = examItems.value?.filter((item: any) => item.Id === examItemId)
    console.log('currentExamItemFind:', currentExamItemFind)
    currentExamItem.value = currentExamItemFind[0]
    subjectId.value = examItemId
    // 根据选中的考试项加载对应的分析数据和曲线数据
    if(tabsType.value === 'analysis'){
        loadScoreAnalysis()
        loadChartDataAndInit()
    }
}

// 处理分析筛选变化
const handleFilterChange = (filter: string) => {
    tabsType.value = filter
    console.log('分析筛选类型:', filter)

    if (filter === 'record') {
        // 切换到成绩记录时，加载成绩数据
        loadScoreDetails()
    } else if (filter === 'analysis') {
        // 切换到成绩分析时，加载分析数据和考试项
        if(examItems.value.length == 0){
            loadInitialExamItems()
        }else{
            loadScoreAnalysis()
            loadChartDataAndInit()
        }
    }
}

// 加载成绩明细数据
const loadScoreDetails = async () => {
    console.log('loadScoreDetails', studentInfo.value)
    if (!studentInfo.value || !studentInfo.value.ID) {
        console.log('学员信息不完整，无法加载成绩明细')
        return
    }

    try {
        loading.value = true

        // 调用成绩明细查询API
        const res = await queryStudentScoreRecord({
            studentId: studentInfo.value.ID,
            PageIndex: page.value.PageIndex,
            PageSize: page.value.PageSize
        })

        if (res.ErrorCode === 200 && res.Data) {
            // 处理返回的数据，转换为表格需要的格式
            scoreDetailData.value = res.Data?.List || []

            page.value.PageSize = res.Data.PageSize || 0
            page.value.PageIndex = res.Data.PageIndex || 0
            page.value.TotalCount = res.Data.TotalCount || 0
            page.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / page.value.PageSize)

         
        } else {
            ElMessage.error(res.ErrorMsg || '加载成绩明细失败')
            scoreDetailData.value = []
        }
    } catch (error) {
        ElMessage.error('加载成绩明细失败，请重试')
        scoreDetailData.value = []
    } finally {
        loading.value = false
    }
}

// 加载成绩分析数据
const loadScoreAnalysis = async () => {
    if (!studentInfo.value || !studentInfo.value.ID || !subjectId.value) {
        console.log('学员信息不完整，无法加载成绩分析')
        analysisLoading.value = true

        return
    }
    
    try {
        analysisLoading.value = false
        
        // 调用成绩分析API，参考 examDetailDialog.vue 的实现
        const res = await queryStudentScoreOverview({
            studentId: studentInfo.value.ID,
            subjectId: subjectId.value
        })
        
        
        if (res.ErrorCode === 200 && res.Data) {
            analysisData.value.examCount = res.Data.examCount
            analysisData.value.averageScore = res.Data.averageScoreText 
            analysisData.value.highestScore = res.Data.highestScoreText
            analysisData.value.lowestScore = res.Data.lowestScoreText
            analysisLoading.value = true

        } 
    } catch (error) {
        analysisData.value.examCount = 0
        analysisData.value.averageScore = 0
        analysisData.value.highestScore = 0
        analysisData.value.lowestScore = 0
    } finally {
        analysisLoading.value = true
        
        
    }
}

// 初始化折线图
const initLineChart = () => {
    
    if (!lineChartRef.value) {
        return
    }
    
    try {
        // 如果已存在图表实例，先销毁
        if (lineChart) {
            lineChart.dispose()
            lineChart = null
        }
        
        // 确保容器有正确的尺寸
        const container = lineChartRef.value
        
        lineChart = echarts.init(lineChartRef.value, null, {
            renderer: 'canvas',
            useDirtyRect: false
        })
        
        // 监听窗口大小变化
        window.addEventListener('resize', () => {
            if (lineChart) {
                lineChart.resize()
            }
        })
        
        updateLineChart()
    } catch (error) {
        console.error('初始化折线图失败:', error)
    }
}

// 更新折线图数据
const updateLineChart = () => {
    if (!lineChart) {
        return
    }
    
    // 强制重新计算图表尺寸
    lineChart.resize()
    
    const dataCount = lineChartData.value.studentScores.length
    
    const option = {
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(50, 50, 50, 0.9)',
            borderColor: '#333',
            textStyle: {
                color: '#fff'
            },
            formatter: function(params: any) {
                if (!params || !params.length) return ''
                
                // 获取第一个数据点
                const firstData = params[0]
                const name = firstData.name
                const dataIndex = firstData.dataIndex
                
                // 检查是否有考试日期数据
                let examDate = ''
                const studentScore = lineChartData.value.studentScores[dataIndex] as any
                if (studentScore?.examDate) {
                    examDate = studentScore.examDate
                }
                
                // 构建tooltip内容
                let result = ''
                if (examDate) {
                    result = `${name}<br/>考试日期：${examDate}<br/>`
                } else {
                    result = `${name}<br/>`
                }
                
                params.forEach((param: any) => {
                    const color = param.color
                    const seriesName = param.seriesName
                    const value = param.value
                    result += `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${color};"></span>${seriesName}: ${value}${currentExamItem.value?.unitType || ''}<br/>`
                })
                return result
            }
        },
        legend: {
            data: ['学员成绩', '班级平均分'],
            selected: {
                '学员成绩': true,    // 默认显示
                '班级平均分': false   // 默认隐藏
            },
            bottom: dataCount > 10 ? '1%' : '2%', // 当有滚动条时，调整图例位置
            textStyle: {
                color: '#606266',
                fontSize: 12
            },
            itemWidth: 12,
            itemHeight: 8,
            itemGap: 20
        },
        grid: {
            left: '2%',
            right: '4%',
            bottom: dataCount > 10 ? '20%' : '15%', // 当有滚动条时，增加底部空间
            top: '10%',
            containLabel: true
        },
        // 🚀 添加横向滚动配置：当数据点超过10个时启用滚动
        dataZoom: dataCount > 10 ? [
            {
                type: 'slider',
                show: true,
                xAxisIndex: [0],
                start: 0,
                end: Math.min(100, (10 / dataCount) * 100), // 初始显示前10个数据点
                height: 20,
                bottom: '10%',
                handleIcon: 'path://M30.9,53.2C16.8,53.2,5.3,41.7,5.3,27.6S16.8,2,30.9,2C45,2,56.4,13.5,56.4,27.6S45,53.2,30.9,53.2z M30.9,3.5C17.6,3.5,6.8,14.4,6.8,27.6c0,13.3,10.8,24.1,24.1,24.1C44.2,51.7,55,40.9,55,27.6C54.9,14.4,44.1,3.5,30.9,3.5z M36.9,35.8h5.7v-8.3h-5.7V35.8z M23.9,35.8h5.7v-8.3h-5.7V35.8z M36.9,27.6V19.2h-10.8v8.3H36.9z M23.9,19.2v8.3H13.2V19.2H23.9z',
                handleSize: '80%',
                handleStyle: {
                    color: '#409EFF'
                },
                textStyle: {
                    color: '#606266',
                    fontSize: 12
                },
                filterMode: 'none' // 🚀 不对数据过滤，只影响显示范围，配合scale使用
            }
        ] : [],
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: lineChartData.value.studentScores.map((item: any) => item.name),
            axisLabel: {
                color: '#606266',
                fontSize: 12,
                rotate: 0, // 数据多时旋转标签
                interval: 'auto', // 数据多时自动间隔
                formatter: function(value: string) {
                    // 超过十个字时省略显示
                    if (value && value.length > 8) {
                        return value.substring(0, 8) + '...'
                    }
                    return value
                }
            },
            axisLine: {
                lineStyle: {
                    color: '#E4E7ED'
                }
            },
            axisTick: {
                alignWithLabel: true
            }
        },
        yAxis: {
            type: 'value',
            name: currentExamItem.value?.scoreTypeName || '',
            scale: true, // 🚀 允许根据可见数据范围自动缩放Y轴，配合dataZoom使用
            nameTextStyle: {
                color: '#606266',
                fontSize: 12
            },
            axisLabel: {
                color: '#606266',
                fontSize: 12,
                formatter: function(value: any) {
                    // 根据考试项类型决定是否显示单位
                    if (currentExamItem.value?.scoreTypeCode === 'TYPE_GRADE') {
                        return value
                    } else if (currentExamItem.value?.scoreTypeCode === 'TYPE_TEXT') {
                        return value
                    }
                    return value + (currentExamItem.value?.unitType || '')
                }
            },
            axisLine: {
                lineStyle: {
                    color: '#E4E7ED'
                }
            },
            splitLine: {
                lineStyle: {
                    color: '#F5F7FA',
                    type: 'dashed'
                }
            },
            min: function(value: any) {
                return Math.max(0, value.min - 10) // 确保最小值不会太低
            }
        },
        series: [
            {
                name: '学员成绩',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 8,
                lineStyle: {
                    color: '#409EFF',
                    width: 3
                },
                itemStyle: {
                    color: '#409EFF',
                    borderColor: '#fff',
                    borderWidth: 2
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [
                            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
                        ]
                    }
                },
                emphasis: {
                    focus: 'series',
                    itemStyle: {
                        borderColor: '#409EFF',
                        borderWidth: 3
                    }
                },
                data: lineChartData.value.studentScores.map((item: any) => item.value)
            },
            {
                name: '班级平均分',
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: {
                    color: '#67C23A',
                    width: 2,
                    type: 'dashed' // 虚线表示班级平均分
                },
                itemStyle: {
                    color: '#67C23A',
                    borderColor: '#fff',
                    borderWidth: 2
                },
                emphasis: {
                    focus: 'series',
                    itemStyle: {
                        borderColor: '#67C23A',
                        borderWidth: 3
                    }
                },
                data: lineChartData.value.classAverages.map((item: any) => item.value)
            }
        ],
        animation: true,
        animationDuration: 1000,
        animationEasing: 'cubicOut' as const
    }
    
    lineChart.setOption(option, true) // 第二个参数 true 表示不合并，完全替换
    
    // 再次确保图表尺寸正确
    setTimeout(() => {
        lineChart?.resize()
    }, 100)
}

// 分页处理 - 每页条数变化
const handleSizeChange = (val: number) => {
    page.value.PageSize = val
    page.value.PageIndex = checkPageIndex(page.value.PageIndex, page.value.TotalCount, page.value.PageSize)
    loadScoreDetails()
}

// 分页处理 - 当前页变化
const handleCurrentChange = (val: number) => {
    page.value.PageIndex = val
    loadScoreDetails()
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

onMounted(() => {
    console.log('window.microApp', window.microApp)
    if (window.microApp) {
        const data = window.microApp.getData()
        console.log('data', data)
        studentInfo.value = data.studentInfo;

        // 初始化完成后加载成绩数据和考试项
        setTimeout(() => {
            loadScoreDetails()
            loadInitialExamItems()
        }, 100)
    } else {
        studentInfo.value = {
            name: '未知',
            age: 0,
            score: 0
        };
    }
    
    
});

// 组件销毁时清理
onUnmounted(() => {
    if (lineChart) {
        lineChart.dispose()
        lineChart = null
    }
    
    // 移除窗口大小变化监听器
    window.removeEventListener('resize', () => {
        if (lineChart) {
            lineChart.resize()
        }
    })
});
</script>

<style lang="scss" scoped>
.stuScoreManage {
    padding: 10px 0px;
    background-color: #fff;
    height: fit-content;
    display: flex;
    flex-direction: column;

    .button-group {
        display: flex;
        background-color: #F2F3F5;
        padding: 2px;
        width: fit-content;
        border-radius: 6px;
        margin-right: 16px;
        margin-bottom: 20px;
        flex-shrink: 0;
        align-items: center;
        gap: 8px;

        .wtwo-button {
            border-radius: 4px;
            font-size: 14px;
            font-weight: 400;
            padding: 8px 16px;
            height: 32px;
            line-height: 1;

            &.wtwo-button--primary {
                background-color: #2878e8;
                border: none;
                color: #fff;
            }

            &.wtwo-button--default {
                background: #F2F3F5;
                border: none;
                color: #606266;

            }
        }
        
        // 考试项下拉框样式
        :deep(.wtwo-select) {
            .wtwo-input__wrapper {
                border-radius: 6px;
            }
        }
        
        // 无限滚动相关样式
        .loading-text, .empty-text {
            text-align: center;
            color: #909399;
            font-size: 12px;
            padding: 8px 0;
        }
        
        .infinite-scroll-footer {
            text-align: center;
            padding: 8px 0;
            border-top: 1px solid #E4E7ED;
            margin-top: 4px;
            
            .loading-more, .load-more-text {
                color: #909399;
                font-size: 12px;
            }
            
            .loading-more {
                color: #409EFF;
            }
        }
        
        .load-complete {
            text-align: center;
            padding: 8px 0;
            border-top: 1px solid #E4E7ED;
            margin-top: 4px;
            color: #67C23A;
            font-size: 12px;
        }
    }

    .tabsType-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;
    }


    .score-detail-table {
        width: 100%;
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;


        :deep(.wtwo-table) {
            .wtwo-table__header {
                background-color: #F5F7FA;
            }

            .wtwo-table__header th {
                background-color: #F5F7FA;
                color: #606266;
                font-weight: 500;
            }

            .wtwo-table__body tr:hover {
                background-color: #F5F7FA;
            }
        }

        .table-action-btn {
            display: flex;
            align-items: center;
        }

        .colline {
            margin: 0 12px;
            width: 1px;
            height: 12px;
            background-color: #D8D8D8;
        }

        .pagination-wrapper {
            display: flex;
            justify-content: flex-end;
            margin-top: 16px;
            padding: 0 16px;
            flex-shrink: 0;

            :deep(.wtwo-pagination) {
                .wtwo-pagination__total {
                    color: #606266;
                    font-size: 14px;
                }

                .wtwo-pagination__sizes {
                    .wtwo-select {
                        .wtwo-input__wrapper {
                            border-radius: 4px;
                        }
                    }
                }

                .wtwo-pagination__jump {
                    color: #606266;
                    font-size: 14px;

                    .wtwo-input__wrapper {
                        border-radius: 4px;
                    }
                }

                .wtwo-pagination__btn {
                    border-radius: 4px;

                    &.is-disabled {
                        color: #C0C4CC;
                    }
                }
            }
        }
    }

    .analysis {
        width: 100%;

        .analysis-total {
            padding: 16px;
            border: 1px solid #E4E7ED;
            border-radius: 8px;

            .analysis-total-title {
                font-size: 16px;
                font-style: normal;
                font-weight: 500;
                color: #303133;
                margin-bottom: 16px;
            }

            .analysis-score-content {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: space-around;

                .analysis-score-content-item {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    height: 52px;
                    justify-content: space-between;

                    .label {
                        color: #606266;
                        font-size: 14px;
                        font-style: normal;
                        font-weight: 400;
                        display: inline-block;
                        line-height: 20px;
                        margin-bottom: 4px;
                    }

                    .value {
                        font-size: 18px;
                        font-weight: 600;
                        color: #303133;
                    }
                }
            }
        }
        .line-smooth {
            width: 100%;
            margin-top: 16px;
            padding: 16px;
            border: 1px solid #E4E7ED;
            border-radius: 8px;
            display: flex;
            flex-direction: column;
    
            .line-chart-title {
                font-size: 16px;
                font-style: normal;
                font-weight: 500;
                color: #303133;
                margin-bottom: 16px;
                flex-shrink: 0;
            }
    
            .line-chart {
                flex: 1;
                position: relative;
                
                // 确保容器有正确的尺寸
                &::before {
                    content: '';
                    display: block;
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    top: 0;
                    left: 0;
                    z-index: -1;
                }
            }
        }
    }

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

#wtwo-app {
    padding: 0 !important;
    background-color: #fff !important;
}

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