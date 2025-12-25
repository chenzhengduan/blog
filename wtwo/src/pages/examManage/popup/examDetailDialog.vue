<template>
    <el-dialog v-model="dialogVisible" title="考试详情" width="880px" :close-on-click-modal="false" :append-to-body="true"
        :destroy-on-close="true" :align-center="true" class="exam-detail-dialog" @close="handleClose">
        <div class="exam-detail-content">
            <div class="exam-detail-content-head">
                <div class="info-row">
                    <div class="info-item">
                        <span class="label">考试日期：</span>
                        <span class="value">{{ examData && examData.ExamDate || '-' }}</span>
                    </div>
                    <!-- <div class="info-item">
                        <span class="label">考试模式：</span>
                        <span class="value">{{ examData && (examData.ExamMode === 0 ? '全部' : examData.ExamMode === 1 ?
                            '线下考试' : '线上考试') || '-'}}</span>
                    </div> -->
                    <div class="info-item">
                        <span class="label">科目：</span>
                        <span class="value">{{examData && examData.Subjects && examData.Subjects.length > 0 ?
                            examData.Subjects.map((item: any) => item.Name).join('、') : '-'}}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">年级：</span>
                        <span class="value">{{examData && examData.Grades && examData.Grades.length > 0 ?
                            examData.Grades.map((item: any) => item.Name).join('、') : '-'}}</span>
                    </div>
                </div>
            </div>
            <div class="exam-detail-content-tabs">
                <el-tabs v-model="activeTab" class="exam-tabs" @tab-change="handleTabChange">
                    <el-tab-pane label="考试分析" name="analysis">
                        <div class="tab-content">
                            <!-- 分析页面头部按钮组 -->
                            <div class="analysis-header">
                                <div class="button-group">
                                    <el-button :type="analysisFilter === 'all' ? 'primary' : 'default'"
                                        @click="handleAnalysisFilterChange('all')" size="small">
                                        全部
                                    </el-button>
                                    <el-button :type="analysisFilter === 'class' ? 'primary' : 'default'"
                                        @click="handleAnalysisFilterChange('class')" size="small">
                                        按班级
                                    </el-button>
                                    <el-button :type="analysisFilter === 'campus' ? 'primary' : 'default'"
                                        @click="handleAnalysisFilterChange('campus')" size="small">
                                        按校区
                                    </el-button>
                                </div>
                                <div class="analysis-filter">
                                    <el-select v-model="selectedCampusIds" v-if="analysisFilter === 'campus'"
                                        :multiple="true" collapse-tags placeholder="不限" filterable clearable
                                        @change="handleCampusChange" style="width: 280px;">
                                        <template #prefix>
                                            <p class="search-input-label">所属校区</p>
                                        </template>
                                        <el-option v-for="item in campusList" :value="item.ID" :label="item.Name"
                                            :key="item.ID"></el-option>
                                    </el-select>
                                    <!-- 班级的下拉框 -->
                                    <el-select v-model="selectedClassIds" v-if="analysisFilter === 'class'"
                                        :multiple="true" collapse-tags placeholder="不限" filterable clearable
                                        @change="handleClassChange" style="width: 280px;">
                                        <template #prefix>
                                            <p class="search-input-label">班级名称</p>
                                        </template>
                                        <el-option v-for="item in classList" :value="item.classId" :label="item.className"
                                            :key="item.classId"></el-option>
                                        <template #empty>
                                            <div v-if="classListLoading" class="loading-text">加载中...</div>
                                            <div v-else-if="classList.length === 0" class="empty-text">暂无数据</div>
                                        </template>
                                        <template #footer>
                                            <div v-if="hasMoreClasses && dialogVisible" v-infinite-scroll="loadMoreClasses" 
                                                :infinite-scroll-disabled="classListLoading || !dialogVisible" 
                                                :infinite-scroll-distance="10"
                                                class="infinite-scroll-footer">
                                                <div v-if="classListLoading" class="loading-more">加载中...</div>
                                                <div v-else class="load-more-text">
                                                    已加载 {{ classList.length }}/{{ classPageInfo.TotalCount }} 条，滚动加载更多
                                                </div>
                                            </div>
                                            <div v-else-if="classList.length > 0" class="load-complete">
                                                已加载全部 {{ classList.length }} 条数据
                                            </div>
                                        </template>
                                    </el-select>
                                </div>
                            </div>
                            <!-- 考试项单选框组 -->
                            <div class="analysis-item-group">
                                <el-radio-group v-model="selectedExamItem" @change="handleExamItemChange">
                                    <el-radio v-for="item in examItems" :key="item.Id" :label="item.Id" size="small">
                                        <span v-if="item.Name.length <= 10">{{ item.Name }}<span v-if="item.status ===0" class="deleted-tag">(已删除)</span></span>
                                        <el-tooltip
                                            v-else
                                            class="box-item"
                                            effect="dark"
                                            :content="item.Name"
                                            placement="top"
                                        >
                                            <template #content>
                                                <span>{{ item.Name }}</span>
                                            </template>
                                            <div class="flex items-center">
                                                <div class="inline-block text-left " style="width: fit-content;overflow: hidden;
                                                    white-space: nowrap;
                                                    text-overflow: ellipsis;">{{ item.Name }}</div>
                                                <span v-if="item.status ===0" class="deleted-tag">(已删除)</span>
                                            </div>
                                        </el-tooltip>
                                        
                                    </el-radio>
                                </el-radio-group>
                            </div>
                            <!-- 成绩分析 -->
                            <div class="analysis-score">
                                <div class="analysis-score-title">
                                    <span>成绩分析</span>
                                </div>
                                <div class=" analysis-score-content">
                                    <div class="analysis-score-content-item">
                                        <span class="label flex items-center">
                                            <span>已有成绩人次</span>
                                            <el-tooltip class="box-item" effect="dark" content="" placement="top">
                                                <template #content> 
                                                    <div>人次含义说明：</div>
                                                    <div class="flex items-center ">
                                                        1名学员有1条成绩算1次
                                                    </div>
                                                    <div class="flex items-center ">
                                                        举例：学员A在班级1和班级2各有一次考试<br />项目A的成绩记录，算2人次。
                                                    </div>
                                                </template>
                                                <el-icon size="18px">
                                                    <svg aria-hidden="true">
                                                        <use xlink:href="#w2-xinxitishi"></use>
                                                    </svg>
                                                </el-icon>
                                            </el-tooltip>
                                        </span>
                                        <span class="value" v-if="analysisLoading">{{
                                            analysisData.scoredCount}}</span>
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
                                        <span class="value" v-if="analysisLoading">{{ analysisData.averageScore }}</span>
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
                                        <span class="value" v-if="analysisLoading">{{ analysisData.maxScore }}</span>
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
                                        <span class="value" v-if="analysisLoading">{{ analysisData.minScore }}</span>
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
                            <!-- 成绩统计 -->
                            <div class="analysis-statistics">
                                <div class="analysis-statistics-title">
                                    <span>成绩统计</span>
                                    <el-button type="primary" @click="showAddRangeDialog" size="small"
                                        :disabled="scoreRangeData.length >= 10 || loadingAnalysis">
                                        添加统计区间
                                    </el-button>
                                </div>
                                <div class="analysis-statistics-content" v-loading="loadingAnalysis">
                                    <div class="analysis-statistics-content-left">
                                        <div ref="pieChartRef" class="pie-chart" v-if="scoreRangeData.length > 0"></div>
                                        <el-empty description="暂无数据" v-else/>
                                    </div>
                                    <div class="analysis-statistics-content-right">
                                        <el-table :data="scoreRangeData" style="width: 100%" height="280px">
                                            <el-table-column prop="range" label="成绩区间" width="120">
                                                <template #header>
                                                    <div class="flex items-center">
                                                        <span class="mr-4px">成绩区间</span>
                                                        <el-tooltip class="box-item" effect="dark" content="" placement="top">
                                                            <template #content> 
                                                                <div>区间统计公式举例说明：</div>
                                                                <div class="flex items-center ">
                                                                    <span class="w-50px inline-block text-right">0-60：</span>
                                                                    <span class="w-100px inline-block text-left">0<=成绩<60</span>
                                                                </div>
                                                                <div class="flex items-center ">
                                                                    <span class="w-50px inline-block text-right">60-90：</span>
                                                                    <span class="w-100px inline-block text-left">60<=成绩<90</span>
                                                                </div>
                                                                <div class="flex items-center ">
                                                                    <span class="w-50px inline-block text-right">90-100：</span>
                                                                    <span class="w-100px inline-block text-left">90<=成绩<=100</span>
                                                                </div>
                                                                
                                                            </template>
                                                            <el-icon size="18px">
                                                                <svg aria-hidden="true">
                                                                    <use xlink:href="#w2-xinxitishi"></use>
                                                                </svg>
                                                            </el-icon>
                                                        </el-tooltip>
                                                    </div>
                                                </template>
                                                <template #default="{ row }">
                                                    <span v-if="row.scoreTypeCode =='TYPE_GRADE'">{{ row.scoreListValue || '' }}</span>
                                                    <span v-else>{{ row.scoreStart + '-' + row.scoreEnd }}</span>
                                                    
                                                </template>
                                            </el-table-column>
                                            <el-table-column prop="scoredCount" label="学员人次" width="100" />
                                            <el-table-column prop="percentage" label="占比" width="100">
                                                <template #default="{ row }">
                                                    <span v-if="row.percentage || row.percentage==0">{{ row.percentage }}%</span>
                                                    <span v-else>-</span>
                                                </template>
                                            </el-table-column>
                                            <el-table-column label="操作" width="120">
                                                <template #default="{ row, $index }">
                                                    <div class="table-action-btn">
                                                        <el-button type="primary" link size="small"
                                                            @click="handleEditRange(row, $index)">
                                                            修改
                                                        </el-button>
                                                        <div class="colline"></div>
                                                        <el-button type="primary" link size="small"
                                                            @click="handleDeleteRange(row, $index)"
                                                            style="margin-left: 8px;">
                                                            删除
                                                        </el-button>

                                                    </div>
                                                </template>
                                            </el-table-column>
                                        </el-table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-tab-pane>
                    <el-tab-pane label="成绩明细" name="scores">
                        <div class="tab-content">
                            <div class="filter-area">
                                <div class="filter-item">
                                    <el-input v-model="scoresCondition.studentName" placeholder="请输入学员名称"
                                        class="class-input" @keyup.native.enter=""></el-input>
                                </div>
                                <div class="filter-item">
                                    <el-input v-model="scoresCondition.className" placeholder="请输入班级名称"
                                        class="class-input" @keyup.native.enter=""></el-input>
                                </div>
                                <div class="filter-item">
                                    <el-select v-model="scoresCondition.examSubjectIds" collapse-tags collapse-tags-tooltip :multiple="true"
                                        placeholder="请选择考试项" style="width: 100%">
                                        <el-option v-for="item in examItems" :key="item.Id" :value="item.Id"
                                            :label="item.Name">
                                            <span style="float: left">{{ item.Name }}</span>
                                            <span  style=" float: right; color: #f56c6c; font-size: 10px; " v-if="item.status == 0">(已删除)</span>
                                        </el-option>
                                    </el-select>
                                </div>
                                <div class="filter-item">
                                    <el-button type="primary" @click="handleSearch"
                                        :disabled="loadingTable">查询</el-button>
                                    <el-button style="margin-left: 0px;" plain @click="handleReset"
                                        :disabled="loadingTable">重置</el-button>
                                </div>
                            </div>
                            <!-- 操作列 -->
                            <div class="operation-area">
                                <el-button type="primary" size="small" @click="handleExport"
                                    :disabled="loadingTable">导出</el-button>
                            </div>
                            <!-- 成绩明细表格 -->
                            <div class="score-detail-table">
                                <el-table :data="scoreDetailData" style="width: 100%" height="445px"
                                    v-loading="loadingTable">
                                    <el-table-column prop="studentName" label="学员名称" width="100" fixed="left"
                                        show-overflow-tooltip />
                                    <el-table-column prop="studentCampusName" label="学员所属校区" width="120"
                                        show-overflow-tooltip />
                                    <el-table-column prop="examName" label="考试名称" width="120" show-overflow-tooltip />
                                    <el-table-column prop="examType" label="考试类型" width="100">
                                        <template #default="{ row }">
                                            {{ row.examType && row.examType.name || '-' }}
                                        </template>
                                    </el-table-column>
                                    <!-- <el-table-column prop="examModeText" label="考试模式" width="100" show-overflow-tooltip>
                                        <template #default="{ row }">
                                            {{ row.examModeText || '-' }}
                                        </template>
                                    </el-table-column> -->
                                    <!-- 暂时不使用学员年级筛选 -->
                                    <!-- <el-table-column prop="grade" label="年级" width="80" show-overflow-tooltip>
                                        <template #default="{ row }">
                                            {{ row.grade && row.grade.name }}
                                        </template>
                                    </el-table-column> -->
                                    <el-table-column prop="fullTimeSchool" label="公立学校" width="120" show-overflow-tooltip>
                                        <template #default="{ row }">
                                            {{ row.fullTimeSchool || '-' }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="classInfo" label="班级" width="120" show-overflow-tooltip>
                                        <template #default="{ row }">
                                            {{ row.classInfo && row.classInfo.name || '-' }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="examSubjectItem" label="考试项目" width="100"
                                        show-overflow-tooltip>
                                        <template #default="{ row }">
                                            {{ row.examSubjectItem && row.examSubjectItem.name || '-' }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="scoreShowInfo" label="成绩" width="80">
                                        <template #default="{ row, $index }">
                                            <div class="score-cell" @mouseenter="handleScoreHover(row)"
                                                @mouseleave="handleScoreLeave(row)">
                                                <span class="score-text">{{ row.scoreShowInfo }}</span>
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
                                    <el-table-column prop="classRank" label="班级排名" width="100" >
                                        <template #default="{ row }">
                                            {{ row.scoreType && row.scoreType.code == 'TYPE_SCORE' ? row.classRank : '-' }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="examDate" label="考试日期" width="120" show-overflow-tooltip>
                                        <template #default="{ row }">
                                            {{ row.examDate || '-' }}
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
                                    <el-table-column label="操作" width="80" fixed="right" align="center">
                                        <template #default="{ row, $index }">
                                            <div class="table-action-btn">
                                                <el-button type="primary" link size="small"
                                                    @click="handleDeleteScore(row, $index)">删除</el-button>
                                            </div>
                                        </template>
                                    </el-table-column>
                                </el-table>

                                <!-- 分页组件 -->
                                <div class="pagination-wrapper" v-if="scoreDetailData.length > 0">
                                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                                        :current-page="page.PageIndex" :page-sizes="[10, 20, 50, 100, 200]"
                                        :page-size="page.PageSize" layout="total, sizes, prev, jumper, next"
                                        :total="page.TotalCount" size="small" />
                                </div>
                            </div>
                        </div>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
    </el-dialog>

    <!-- 添加/修改统计区间弹窗 -->
    <el-dialog v-model="addRangeDialogVisible" :show-close="false" :show-header="false" width="320px"
        :close-on-click-modal="false" :append-to-body="true" :destroy-on-close="true" :align-center="true"
        class="add-range-dialog">

        <el-form :model="addRangeForm" :rules="addRangeRules" ref="addRangeFormRef" label-width="100px"
            label-position="top">
            <el-form-item label="考试项目" prop="subjectId">
                <el-select v-model="addRangeForm.subjectId" :disabled="!!addRangeForm.id" placeholder="请选择考试项"
                    style="width: 100%" @change="handleExamItemChangeForm">
                    <el-option v-for="item in examItems" :key="item.Id" :value="item.Id" :label="item.Name">
                        <span style="float: left">{{ item.Name }}</span>
                        <span  style=" float: right; color: #f56c6c; font-size: 10px; " v-if="item.status == 0">(已删除)</span>
                    </el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="" prop=""
                v-if="currentExamItem && (currentExamItem.UnitTypeID || currentExamItem.ScoreTypeName == '等级') && currentExamItem.status != 0">
                <label slot="label">
                    <div class="flex items-center">
                        <span class="mr-4px">成绩区间</span>
                        <el-tooltip class="box-item" effect="dark" content="" placement="top">
                            <template #content> 
                                <div>区间统计公式举例说明：</div>
                                <div class="flex items-center ">
                                    <span class="w-50px inline-block text-right">0-60：</span>
                                    <span class="w-100px inline-block text-left">0<=成绩<60</span>
                                </div>
                                <div class="flex items-center ">
                                    <span class="w-50px inline-block text-right">60-90：</span>
                                    <span class="w-100px inline-block text-left">60<=成绩<90</span>
                                </div>
                                <div class="flex items-center ">
                                    <span class="w-50px inline-block text-right">90-100：</span>
                                    <span class="w-100px inline-block text-left">90<=成绩<=100</span>
                                </div>
                                
                            </template>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use xlink:href="#w2-xinxitishi"></use>
                                </svg>
                            </el-icon>
                        </el-tooltip>
                    </div>
                </label>
                <template v-if="currentExamItem.ScoreTypeName == '等级'">
                    <el-select v-model="addRangeForm.scoreList" multiple placeholder="请选择成绩区间" style="width: 100%">
                        <el-option v-for="item in examScoreGradeList" :key="item.ID" :value="item.ID" :label="item.Value"></el-option>
                    </el-select>
                </template>
                <template v-else>
                    <div class="score-range-input flex justify-between">
                        <el-form-item label="" prop="scoreStart" style="margin-right: 20px;">
                            <el-input v-model="addRangeForm.scoreStart" placeholder="请输入" @input="handleScoreStartInput"
                                @keypress="handleNumberKeypress">
                                <template #suffix>
                                    <span>{{ currentExamItem.UnitType }}</span>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item label="" prop="scoreEnd">
                            <el-input v-model="addRangeForm.scoreEnd" placeholder="请输入" @input="handleScoreEndInput"
                                @keypress="handleNumberKeypress">
                                <template #suffix>
                                    <span>{{ currentExamItem.UnitType }}</span>
                                </template>
                            </el-input>
                        </el-form-item>

                    </div>
                </template>

            </el-form-item>
            <div v-else-if="currentExamItem && currentExamItem.status == 0" class="score-range-text">
                当前考试项目已被删除不支持统计。
            </div>
            <div v-else class="score-range-text">
                文本类型的考试项目暂不支持统计。
            </div>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="cancelAddRange" size="small">取消</el-button>
                <el-button style="margin-left: 0px;" type="primary" @click="confirmAddRange" size="small" 
                    :disabled="isTextTypeDisabled">{{ isEditMode ? '保存' : '确定' }}</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import * as echarts from 'echarts'
import { getExamDetail, examScoreAnalysis, queryScoreDetailsList, queryExamClassesList, examScoreStatisticsList, exportScoreDetails, examScoreStatisticsAdd, examScoreStatisticsUpdate, examScoreStatisticsDelete, updateScore, deleteScoreById, queryDictionaryList } from '@/api/exam'
import { ElMessage, ElMessageBox } from 'element-plus'
import { downloadFile, assignPage, checkPageIndex, IPageModel } from '@/utils'
import { apiUrl, useUserCampuses } from '@/store'
import { Edit } from '@element-plus/icons-vue';

// 弹窗显示状态
const dialogVisible = ref(false)

// 加载状态
const loading = ref(false)

// 当前激活的标签页
const activeTab = ref('analysis')

// 分析筛选类型
const analysisFilter = ref('all')

// 选中的考试项
const selectedExamItem = ref('')
watch(selectedExamItem, (newVal: string) => {
    console.log('选中的考试项ID:', newVal)

})

// 考试项列表
const examItems = ref<any[]>([])
// 添加统计区间弹窗中选中的考试项
const currentExamItem = ref<any>(null)

// 考试数据
const examId = ref('')
const examData = ref<any>({

})
// 成绩分析
const analysisLoading = ref(false)
const analysisData = ref<any>({
    scoredStudentCount: 0,
    scoredCount: 0,
    averageScore: 0,
    maxScore: 0,
    minScore: 0
})

// 成绩区间统计数据
const scoreRangeData = ref<any[]>([])
const loadingAnalysis = ref(false)

// 图表相关
const pieChartRef = ref<HTMLElement>()
let pieChart: echarts.ECharts | null = null

// 校区相关
const selectedCampusIds = ref<string[]>([])
const campusList = ref<any[]>([])

// 班级相关
const selectedClassIds = ref<string[]>([])
const classList = ref<any[]>([])
const classListLoading = ref(false)
const classPageInfo = ref({
    PageIndex: 1,
    PageSize: 20,
    TotalCount: 0,
    PageCount: 1
})
const hasMoreClasses = ref(true)

// 字典值相关
const examScoreGradeList = ref<any[]>([])

const ExamScoreExport = window.$xgj.op('ExamScoreExport'); // 是否允许导出成绩

// 成绩明细筛选条件
const scoresCondition = ref({
    studentName: '',
    className: '',
    examSubjectIds: []
})

// 成绩明细表格数据
const scoreDetailData = ref([])
const loadingTable = ref(false)
// 分页相关
const page = ref({
    TotalCount: 0,    // 总条数
    PageSize: 10,     // 每页条数
    PageIndex: 1,     // 第几页
    PageCount: 1      // 总页数
} as IPageModel)
// 添加统计区间弹窗相关
const addRangeDialogVisible = ref(false)
const addRangeFormRef = ref()
const addRangeForm = ref({
    id: '',
    subjectId: '',
    scoreList: [],
    scoreStart: '',
    scoreEnd: ''
})

// 编辑模式相关
const isEditMode = ref(false)
const currentEditRange = ref<any>(null)

// 判断是否为文本类型，文本类型不能保存 或者 考试项目已被删除
const isTextTypeDisabled = computed(() => {
    if (!currentExamItem.value) return false
    // 考试项目已被删除
    if (currentExamItem.value.status == 0) return true
    // 检查是否为文本类型（没有UnitTypeID且ScoreTypeName不是'等级'）
    const isTextType = currentExamItem.value.ScoreTypeCode == "TYPE_TEXT"
    
    console.log('检查文本类型:', {
        currentExamItem: currentExamItem.value,
        UnitTypeID: currentExamItem.value.UnitTypeID,
        ScoreTypeName: currentExamItem.value.ScoreTypeName,
        Type: currentExamItem.value.Type,
        isTextType: isTextType
    })
    
    return isTextType
})
const addRangeRules = ref({
    subjectId: [
        { required: true, message: '请选择考试项目', trigger: 'change' }
    ],
    scoreList: [
        { required: true, message: '请选择成绩区间', trigger: 'change' }
    ],
    scoreStart: [
        { required: true, message: '请输入成绩开始值', trigger: 'blur' },
        {
            validator: (rule: any, value: any, callback: any) => {
                if (!value) {
                    callback()
                    return
                }
                // 检查是否为数字
                if (isNaN(Number(value))) {
                    callback(new Error('请输入有效的数字'))
                    return
                }
                // 检查小数位数
                const num = Number(value)
                if (num < 0) {
                    callback(new Error('成绩不能为负数'))
                    return
                }
                // 检查小数位数
                const decimalPlaces = (value.toString().split('.')[1] || '').length
                if (decimalPlaces > 2) {
                    callback(new Error('小数位数最多两位'))
                    return
                }
                callback()
            },
            trigger: 'blur'
        }
    ],
    scoreEnd: [
        { required: true, message: '请输入成绩结束值', trigger: 'blur' },
        {
            validator: (rule: any, value: any, callback: any) => {
                if (!value) {
                    callback()
                    return
                }
                // 检查是否为数字
                if (isNaN(Number(value))) {
                    callback(new Error('请输入有效的数字'))
                    return
                }
                // 检查小数位数
                const num = Number(value)
                if (num < 0) {
                    callback(new Error('成绩不能为负数'))
                    return
                }
                // 检查小数位数
                const decimalPlaces = (value.toString().split('.')[1] || '').length
                if (decimalPlaces > 2) {
                    callback(new Error('小数位数最多两位'))
                    return
                }
                // 检查结束值是否大于开始值
                const startValue = addRangeForm.value.scoreStart
                if (startValue && Number(value) <= Number(startValue)) {
                    callback(new Error('结束值必须大于开始值'))
                    return
                }
                callback()
            },
            trigger: 'blur'
        }
    ]
})





// 处理分析筛选变化
const handleAnalysisFilterChange = (filter: string) => {
    analysisFilter.value = filter
    console.log('分析筛选类型:', filter)
    // TODO: 根据筛选类型加载对应的分析数据
    selectedClassIds.value = []
    selectedCampusIds.value = []
    // 加载成绩分析数据
    loadScoreAnalysis()

    // 加载成绩区间数据
    loadScoreRangeData()
}

// 处理考试项变化
const handleExamItemChange = (examItemId: string | number | boolean | undefined) => {
    console.log('选中的考试项ID:', examItemId)
    // TODO: 根据选中的考试项加载对应的分析数据
    // 加载成绩分析数据
    loadScoreAnalysis()

    // 加载成绩区间数据
    loadScoreRangeData()
}

// 处理添加统计区间弹窗中的考试项变化
const handleExamItemChangeForm = (examItemId: string | number | boolean | undefined) => {
    console.log('添加统计区间弹窗中选中的考试项ID:', examItemId)
    currentExamItem.value = examItems.value.find((item: any) => item.Id === examItemId)
    console.log('currentExamItem:', currentExamItem.value)
    
    // 调试信息：显示考试项的详细结构
    if (currentExamItem.value) {
        console.log('考试项详细信息:', {
            Id: currentExamItem.value.Id,
            Name: currentExamItem.value.Name,
            Type: currentExamItem.value.Type,
            UnitTypeID: currentExamItem.value.UnitTypeID,
            UnitType: currentExamItem.value.UnitType,
            ScoreTypeName: currentExamItem.value.ScoreTypeName
        })
    }
    
    // TODO: 根据选中的考试项更新成绩区间选项或其他相关逻辑
}

// 处理数字输入 - 只允许数字和小数点
const handleNumberKeypress = (event: KeyboardEvent) => {
    const char = String.fromCharCode(event.which)
    // 允许数字、小数点、退格、删除、方向键等
    if (!/[0-9.]/.test(char) && !['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab'].includes(event.key)) {
        event.preventDefault()
    }
}

// 处理成绩开始值输入
const handleScoreStartInput = (value: string) => {
    // 限制小数位数为2位
    const parts = value.split('.')
    if (parts[1] && parts[1].length > 2) {
        addRangeForm.value.scoreStart = parts[0] + '.' + parts[1].substring(0, 2)
    }
}

// 处理成绩结束值输入
const handleScoreEndInput = (value: string) => {
    // 限制小数位数为2位
    const parts = value.split('.')
    if (parts[1] && parts[1].length > 2) {
        addRangeForm.value.scoreEnd = parts[0] + '.' + parts[1].substring(0, 2)
    }
}

// 处理校区变化
const handleCampusChange = (campusIds: string[]) => {
    console.log('选中的校区IDs:', campusIds)
    selectedCampusIds.value = campusIds
    // TODO: 根据选中的校区加载对应的分析数据
    // 加载成绩分析数据
    loadScoreAnalysis()

    // 加载成绩区间数据
    loadScoreRangeData()
}

// 处理班级变化
const handleClassChange = (classIds: string[]) => {
    console.log('选中的班级IDs:', classIds)
    selectedClassIds.value = classIds
    // TODO: 根据选中的班级加载对应的分析数据
    // 加载成绩分析数据
    loadScoreAnalysis()

    // 加载成绩区间数据
    loadScoreRangeData()
}

// 初始化班级数据加载
const loadInitialClasses = async () => {
    try {
        classListLoading.value = true
        
        // 重置分页信息
        classPageInfo.value = {
            PageIndex: 1,
            PageSize: 20,
            TotalCount: 0,
            PageCount: 1
        }
        hasMoreClasses.value = true

        const res = await queryExamClassesList({
            examId: examId.value,
            PageIndex: classPageInfo.value.PageIndex,
            PageSize: classPageInfo.value.PageSize
        })

        console.log('初始化班级数据:', res)

        if (res.ErrorCode === 200 && res.Data) {
            const initialClasses = res.Data.List || []
            classList.value = initialClasses
            
            // 更新分页信息
            classPageInfo.value.TotalCount = res.Data.TotalCount || 0
            classPageInfo.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / classPageInfo.value.PageSize)
            
            // 判断是否还有更多数据
            hasMoreClasses.value = classPageInfo.value.PageIndex < classPageInfo.value.PageCount
            
            console.log('初始化班级数据加载完成:')
            console.log(`- 第1页数据: ${initialClasses.length}条`)
            console.log(`- 总数据量: ${classPageInfo.value.TotalCount}条`)
            console.log(`- 总页数: ${classPageInfo.value.PageCount}页`)
            console.log(`- 是否还有更多: ${hasMoreClasses.value}`)
            console.log('班级分页信息:', classPageInfo.value)
        } else {
            console.log(res.ErrorMsg || '加载班级数据失败')
            classList.value = []
        }
    } catch (error) {
        console.error('加载班级数据失败:', error)
        classList.value = []
    } finally {
        classListLoading.value = false
    }
}

// 获取考试分数等级字典值
const loadExamScoreGradeDict = async () => {
    try {
        console.log('开始获取EXAM_SCORE_GRADE字典值')
        
        const res = await queryDictionaryList({
            PageIndex: 1,
            PageSize: 1000,
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

// 加载更多班级数据
const loadMoreClasses = async () => {
    console.log('loadMoreClasses 被调用')
    
    // 检查弹窗是否打开
    if (!dialogVisible.value) {
        console.log('弹窗已关闭，取消加载')
        return
    }
    
    if (classListLoading.value || !hasMoreClasses.value) {
        console.log('加载条件不满足:', { 
            loading: classListLoading.value, 
            hasMore: hasMoreClasses.value 
        })
        return
    }

    try {
        classListLoading.value = true
        classPageInfo.value.PageIndex += 1

        const res = await queryExamClassesList({
            examId: examId.value,
            PageIndex: classPageInfo.value.PageIndex,
            PageSize: classPageInfo.value.PageSize
        })

        console.log('加载更多班级数据:', res)

        if (res.ErrorCode === 200 && res.Data) {
            const newClasses = res.Data.List || []
            const oldCount = classList.value.length
            
            // 数据累加：将新数据追加到现有数据后面
            classList.value = [...classList.value, ...newClasses]
            
            // 更新分页信息
            classPageInfo.value.TotalCount = res.Data.TotalCount || 0
            classPageInfo.value.PageCount = Math.ceil((res.Data.TotalCount || 0) / classPageInfo.value.PageSize)
            
            // 判断是否还有更多数据
            hasMoreClasses.value = classPageInfo.value.PageIndex < classPageInfo.value.PageCount
            
            console.log(`第${classPageInfo.value.PageIndex}页加载完成:`)
            console.log(`- 新增数据: ${newClasses.length}条`)
            console.log(`- 累计数据: ${oldCount} + ${newClasses.length} = ${classList.value.length}条`)
            console.log(`- 总数据量: ${classPageInfo.value.TotalCount}条`)
            console.log(`- 是否还有更多: ${hasMoreClasses.value}`)
            console.log('班级分页信息:', classPageInfo.value)
        } else {
            console.log(res.ErrorMsg || '加载班级数据失败')
            // 如果加载失败，回退页码
            classPageInfo.value.PageIndex -= 1
        }
    } catch (error) {
        console.error('加载班级数据失败:', error)
        // 如果加载失败，回退页码
        classPageInfo.value.PageIndex -= 1
    } finally {
        classListLoading.value = false
    }
}

// 处理标签页切换
const handleTabChange = (tabName: string | number) => {
    console.log('切换到标签页:', tabName)
    activeTab.value = tabName as string

    if (tabName === 'analysis') {
        loadExamDetail(examId.value)
    }
    if (tabName === 'scores') {
        // 切换到成绩明细标签页时，加载成绩明细数据
        loadScoreDetails()
    }
}

// 加载考试详情
function loadExamDetail(examId: string) {
    if (!examId) return
    analysisLoading.value = true;
    loadingTable.value = true;
    try {
        getExamDetail({ id: examId }).then((res: any) => {
            if (res.ErrorCode === 200 && res.Data) {
                const detail = res.Data
                console.log('考试详情:', detail)
                examData.value = detail

                // 初始化考试项数据
                if (detail.ExamItems && Array.isArray(detail.ExamItems)) {
                    examItems.value = (detail.ExamItems || [])
                    // 默认选中第一个考试项
                    if (examItems.value.length > 0) {
                        selectedExamItem.value = examItems.value[0].Id;
                        if(selectedExamItem.value){
                            // 加载成绩分析数据
                            loadScoreAnalysis()
    
                            // 加载成绩区间数据
                            loadScoreRangeData()
                        }
                    }
                }
                // 计算“当前人可操作校区 ∩ 考试适用校区”的交集，作为校区下拉数据
                try {
                    // 可操作校区来自全局 store
                    const userCampusesStore = useUserCampuses()
                    const userCampuses = userCampusesStore.userCampuses || []
                    const userCampusIds = userCampuses.map((c: any) => c.ID)

                    // 考试适用校区ID集合（兼容两种结构）
                    const examCampusIds: string[] = Array.isArray((detail as any).Campuses)
                        ? (detail as any).Campuses.map((c: any) => c.Id || c.ID)
                        : Array.isArray((detail as any).CampusIds)
                            ? (detail as any).CampusIds
                            : []

                    const examCampusIdSet = new Set(examCampusIds.filter(Boolean))
                    // 交集 ID 列表
                    const intersectIds = userCampusIds.filter((id: any) => examCampusIdSet.has(id))
                    const intersectIdSet = new Set(intersectIds)
                    // 用可操作校区列表中过滤出交集，确保包含名称等信息
                    campusList.value = userCampuses.filter((c: any) => intersectIdSet.has(c.ID))
                    console.log('campusList:', campusList.value)
                } catch (e) {
                    console.warn('计算校区交集失败，回退为空列表:', e)
                    campusList.value = []
                }

                


            } else {
                ElMessage.error(res.ErrorMsg || '加载考试详情失败')
                handleClose()
            }
        })


    } catch (error) {
        console.error('加载考试详情失败:', error)
        ElMessage.error('加载考试详情失败，请重试')
        handleClose()
    }
}
// 加载成绩分析数据
const loadScoreAnalysis = () => {
    if(!selectedExamItem.value) return;
    analysisLoading.value = false;
    examScoreAnalysis({
        examId: examId.value,
        subjectId: selectedExamItem.value,
        classIds: selectedClassIds.value,
        campusIds: selectedCampusIds.value
    }).then((res2: any) => {
        console.log('成绩分析数据:', res2)
        if (res2.ErrorCode === 200 && res2.Data) {
            analysisData.value.scoredStudentCount = (res2.Data.scoredStudentCount || res2.Data.scoredStudentCount == 0 ? res2.Data.scoredStudentCount : '-')
            analysisData.value.scoredCount = (res2.Data.scoredCount || res2.Data.scoredCount == 0 ? res2.Data.scoredCount : '-')
            analysisData.value.averageScore = (res2.Data.averageScore || res2.Data.averageScore == 0 ? res2.Data.averageScore : '-')
            analysisData.value.maxScore = (res2.Data.maxScore || res2.Data.maxScore == 0 ? res2.Data.maxScore : '-')
            analysisData.value.minScore = (res2.Data.minScore || res2.Data.minScore == 0 ? res2.Data.minScore : '-')
        }
    }).catch((err: any) => {
        console.error('成绩分析失败:', err)
        analysisLoading.value = false
    }).finally(() => {
        analysisLoading.value = true
    })
}

// 加载成绩明细数据
const loadScoreDetails = async () => {
    if (!examData.value || !examData.value.Id) {
        console.log('考试信息不完整，无法加载成绩明细')
        return
    }

    try {
        loadingTable.value = true

        // 调用成绩明细查询API，包含分页参数
        const res = await queryScoreDetailsList({
            examIds: [examData.value.Id],
            PageIndex: page.value.PageIndex,
            PageSize: page.value.PageSize,
            ...scoresCondition.value
        })

        console.log('成绩明细数据:', res)

        if (res.ErrorCode === 200 && res.Data) {
            // 处理返回的数据，转换为表格需要的格式
            const items = res.Data.List || []
            // 为每行数据添加编辑相关的属性
            scoreDetailData.value = items.map((item: any) => ({
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


            console.log('成绩明细加载成功:', scoreDetailData.value)
            console.log('分页信息:', page.value)
        } else {
            console.log(res.ErrorMsg || '加载成绩明细失败')
            ElMessage.error(res.ErrorMsg || '加载成绩明细失败')
            scoreDetailData.value = []
        }
    } catch (error) {
        console.error('加载成绩明细失败:', error)
        ElMessage.error('加载成绩明细失败，请重试')
        scoreDetailData.value = []
    } finally {
        loadingTable.value = false
    }
}

// 加载成绩区间数据
const loadScoreRangeData = () => {
    if(!selectedExamItem.value) return;
    loadingAnalysis.value = true
    examScoreStatisticsList({
        examId: examId.value,
        subjectId: selectedExamItem.value,
        classIds: selectedClassIds.value,
        campusIds: selectedCampusIds.value
    }).then((res3: any) => {
        console.log('成绩统计数据:', res3)
        if (res3.ErrorCode === 200 && res3.Data) {
            let List = res3.Data?.List?.map((item: any) => {
                if(item.scoreTypeCode == 'TYPE_GRADE'){
                    const scoreListValue = examScoreGradeList.value.filter((item2: any) => item.scoreList.includes(item2.ID))?.map((item2: any) => item2.Value)?.join(',') || ''
                    return {
                        ...item,
                        scoreListValue: scoreListValue || ''
                    }
                }
                return item
            }) || [];
            
            scoreRangeData.value = List || []
            console.log('scoreRangeData:', scoreRangeData.value)
            // 延迟初始化图表，确保DOM已渲染
            nextTick(() => {
                setTimeout(() => {
                    initPieChart()
                }, 100)
            })
        }
    }).catch((err: any) => {
        console.error('成绩统计失败:', err)
    }).finally(() => {
        loadingAnalysis.value = false
    })
}

// 处理查询
const handleSearch = () => {
    console.log('查询条件:', scoresCondition.value)
    loadScoreDetails()
}

// 处理重置
const handleReset = () => {
    scoresCondition.value = {
        studentName: '',
        className: '',
        examSubjectIds: []
    }

    // 重置分页到第一页
    page.value.PageIndex = 1

    // 重新加载数据
    loadScoreDetails()

    console.log('重置查询条件和表格数据')
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

// 处理导出
const handleExport = () => {
    if (!ExamScoreExport) {
        ElMessage.warning('暂无导出权限')
        return
    }

    if (!examData.value || !examData.value.Id) {
        ElMessage.warning('考试信息不完整，无法导出')
        return
    }

    // 构建导出URL，参考 inputScoreForm.vue 的 handleDownloadTemplate
    const downloadUrl = `${apiUrl}/api/exam/scores/detailsExport`

    // 获取年月日时分
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    const day = now.getDate()
    const hour = now.getHours()
    const minute = now.getMinutes()
    const fileName = `成绩明细表_${year}${month}${day}${hour}${minute}.xlsx`
    // 使用 downloadFile 工具函数下载文件
    downloadFile(downloadUrl, fileName,'POST',{
        examIds: [examData.value.Id],
        PageIndex: page.value.PageIndex,
        PageSize: page.value.PageSize,
        ...scoresCondition.value
    })
    // exportScoreDetails({
    //     examIds: [examData.value.Id],
    //     PageIndex: page.value.PageIndex,
    //     PageSize: page.value.PageSize,
    //     ...scoresCondition.value
    // }).then((res: any) => {
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

// 处理删除成绩
const handleDeleteScore = (row: any, index: number) => {
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
                handleSearch()
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
    scoreDetailData.value.forEach((item: any) => {
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
            handleSearch()
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

// 显示添加统计区间弹窗
const showAddRangeDialog = () => {
    addRangeDialogVisible.value = true
    isEditMode.value = false
    currentEditRange.value = null

    // 重置表单
    addRangeForm.value = {
        id: '',
        subjectId: selectedExamItem.value,
        scoreList: [],
        scoreStart: '',
        scoreEnd: ''
    }
    // 初始化手动调用
    handleExamItemChangeForm(selectedExamItem.value)
}

// 取消添加/修改统计区间
const cancelAddRange = () => {
    addRangeDialogVisible.value = false
    isEditMode.value = false
    currentEditRange.value = null

    // 重置表单
    addRangeForm.value = {
        id: '',
        subjectId: '',
        scoreList: [],
        scoreStart: '',
        scoreEnd: ''
    }
}

// 确认添加/修改统计区间
const confirmAddRange = async () => {
    // 检查是否为考试项目已被删除
    if (currentExamItem.value.status == 0) {
        ElMessage.warning('当前考试项目已被删除不支持统计区间设置')
        return
    }
    // 检查是否为文本类型，文本类型不能保存
    if (isTextTypeDisabled.value) {
        ElMessage.warning('文本类型的考试项目暂不支持统计区间设置')
        return
    }
    // 检查是否为文本类型，文本类型不能保存
    if (isTextTypeDisabled.value) {
        ElMessage.warning('文本类型的考试项目暂不支持统计区间设置')
        return
    }
    
    try {
        await addRangeFormRef.value.validate()

        // 检查是否已达到最大限制（仅添加时检查）
        if (!isEditMode.value && scoreRangeData.value.length >= 10) {
            ElMessage.warning('最多只能添加10个统计区间')
            return
        }

        // rangeType: 1, // 区间类型， 1：成绩值范围；2：成绩值集合
        let params: any = {
            examId: examId.value,
            subjectId: currentExamItem.value.Id,
        }
        // 除了文本和等级类型，其他类型都为成绩值集合
        if(currentExamItem.value.UnitTypeID){
            params.rangeType = 1
            params.scoreStart= addRangeForm.value.scoreStart
            params.scoreEnd= addRangeForm.value.scoreEnd
        }else{
            if (currentExamItem.value.ScoreTypeCode == "TYPE_GRADE") {
                params.rangeType = 2
                params.scoreList = addRangeForm.value.scoreList
            }

        }
        if (isEditMode.value) {
            params.id = currentEditRange.value.id;
            // 修改统计区间
            examScoreStatisticsUpdate(params).then((res: any) => {
                console.log('修改统计区间数据:', res)
                if (res.ErrorCode === 200) {
                    // 重新加载数据
                    loadScoreRangeData()
                    addRangeDialogVisible.value = false
                    ElMessage.success('修改统计区间成功')
                }
            }).catch((error: any) => {
                console.error('修改统计区间失败:', error)
                ElMessage.error('修改统计区间失败')
            })
        } else {
            console.log('添加统计区间请求参数:', params)
            examScoreStatisticsAdd(params).then((res: any) => {
                console.log('添加统计区间数据:', res)
                if (res.ErrorCode === 200) {
                    // 重新加载数据
                    loadScoreRangeData()
                    addRangeDialogVisible.value = false
                    ElMessage.success('添加统计区间成功')
                }
            }).catch((error: any) => {
                console.error('添加统计区间失败:', error)
                ElMessage.error('添加统计区间失败')
            })
        }

    } catch (error) {
        console.log('表单验证失败:', error)
    }
}

// 处理修改成绩区间
const handleEditRange = (row: any, index: number) => {
    console.log('修改成绩区间:', row, index)

    // 设置当前编辑的行数据
    currentEditRange.value = {
        ...row,
        index: index
    }
    handleExamItemChangeForm(row.subjectId)

    // 填充表单数据
    addRangeForm.value = {
        id: row.id,
        subjectId: row.subjectId,
        scoreList: row.scoreList || [],
        scoreStart: row.scoreStart,
        scoreEnd: row.scoreEnd
    }


    // 显示修改弹窗
    addRangeDialogVisible.value = true
    isEditMode.value = true
}

// 处理删除成绩区间
const handleDeleteRange = (row: any, index: number) => {
    console.log('删除成绩区间:', row, index)

    // 二次确认删除操作
    ElMessageBox.confirm(
        `确定要删除成绩区间 "${row.scoreStart}-${row.scoreEnd}" 吗？删除后无法恢复。`,
        '删除确认',
        {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        // 调用删除API
        console.log('调用删除成绩区间API，区间ID:', row.id)

        examScoreStatisticsDelete({ id: row.id }).then((res: any) => {
            console.log('删除成绩区间API响应:', res)
            if (res.ErrorCode === 200) {
                // 删除成功后重新加载数据
                loadScoreRangeData()
                ElMessage.success('已删除成绩区间')
            } else {
                ElMessage.error(res.ErrorMsg || '删除失败')
            }
        }).catch((error: any) => {
            console.error('删除成绩区间失败:', error)
            ElMessage.error('删除成绩区间失败，请重试')
        })

    }).catch(() => {
        // 取消删除
        console.log('取消删除成绩区间操作')
    })
}

// 初始化环形图
const initPieChart = () => {
    if (!pieChartRef.value) return

    // 如果已存在图表实例，先销毁
    if (pieChart) {
        pieChart.dispose()
        pieChart = null
    }

    pieChart = echarts.init(pieChartRef.value)
    updatePieChart()
}

// 更新环形图数据
const updatePieChart = () => {
    if (!pieChart) return

    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9C27B0', '#FF9800', '#4CAF50', '#2196F3', '#FF5722']

    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}： {c} ({d}%)'
        },
        legend: {
            orient: 'horizontal',
            bottom: '10px',
            left: 'center',
            align: 'left',
            textStyle: {
                fontSize: 12,
                color: '#606266',
                fontFamily: 'monospace' // 使用等宽字体
            },
            itemWidth: 12,
            itemHeight: 8,
            itemGap: 15,
            formatter: function (name: string) {
                // 使用等宽字体，通过固定字符数来实现对齐
                const maxLength = 8; // 最大字符数
                if (name.length < maxLength) {
                    return name.padEnd(maxLength, ' ');
                }
                return name;
            }
        },
        series: [
            {
                name: '成绩区间',
                type: 'pie',
                radius: ['40%', '50%'],
                center: ['50%', '30%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderColor: '#fff',
                    borderWidth: 0
                },
                label: {
                    show: true,
                    position: 'center',
                    formatter: function () {
                        // 计算总人数
                        const totalCount = scoreRangeData.value.reduce((sum, item) => sum + (item.scoredCount || 0), 0)
                        return `共计（人）\n\n${totalCount}`
                    },
                    fontSize: 14,
                    fontWeight: 'bold',
                    color: '#303133',
                },
                emphasis: {
                    // label: {
                    //     show: true,
                    //     fontSize: '16',
                    //     fontWeight: 'bold',
                    //     formatter: '{b}\n{c}人'
                    // }
                },
                labelLine: {
                    show: false
                },
                data: scoreRangeData.value.map((item, index) => {
                    return {
                        value: item.scoredCount,
                        name: item.scoreTypeCode == 'TYPE_GRADE' ? item.scoreListValue : `${item.scoreStart}-${item.scoreEnd}`,
                        itemStyle: {
                            color: colors[index % colors.length]
                        }
                    }
                })
            }
        ]
    }

    pieChart.setOption(option)
}


// 打开弹窗
const open = (data?: any) => {
    console.log('data:', data)
    examId.value = data.id
    dialogVisible.value = true

    // 初始化班级数据（示例数据，实际应该从API获取）
    classList.value = []

    handleTabChange('analysis')
    
    // 初始化班级数据加载
    loadInitialClasses()
    
    // 获取考试分数等级字典值
    loadExamScoreGradeDict()


}

// 关闭弹窗
const handleClose = () => {
    console.log('关闭弹窗，重置所有状态')
    
    // 先关闭弹窗，防止无限滚动继续触发
    dialogVisible.value = false
    analysisFilter.value = 'all'
    selectedExamItem.value = ''
    selectedCampusIds.value = []
    scoresCondition.value = {
        studentName: '',
        className: '',
        examSubjectIds: []
    }
    // 停止所有加载操作
    analysisLoading.value = false;
    loadingTable.value = false;
    classListLoading.value = false;
    
    // 重置数据
    examData.value = {};
    examId.value = '';
    scoreDetailData.value = [];
    scoreRangeData.value = [];
    
    // 重置班级相关状态
    classList.value = [];
    classPageInfo.value = {
        PageIndex: 1,
        PageSize: 20,
        TotalCount: 0,
        PageCount: 1
    };
    hasMoreClasses.value = true;
    selectedClassIds.value = [];
    
    // 重置字典值
    examScoreGradeList.value = [];
    
    // 销毁图表
    if (pieChart) {
        pieChart.dispose()
        pieChart = null
    }
    
    console.log('弹窗关闭完成')
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

// 监听数据变化
watch(scoreRangeData, () => {
    updatePieChart()
}, { deep: true })

// 组件销毁时清理
onUnmounted(() => {
    if (pieChart) {
        pieChart.dispose()
        pieChart = null
    }
})

// 暴露方法
defineExpose({
    open
})
</script>

<style lang="scss" scoped>
.exam-detail-dialog {
    .exam-detail-content {
        padding: 12px 0;

        .exam-detail-content-head {
            margin-bottom: 16px;

            .info-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 8px 16px;
                background-color: #F9FAFC;
                border-radius: 8px;

                .info-item {
                    display: flex;
                    align-items: center;
                    flex: 1;
                    min-width: 0;

                    .label {
                        font-weight: 400;
                        font-size: 14px;
                        color: #909399;
                        white-space: nowrap;

                    }

                    .value {
                        font-weight: 400;
                        font-size: 14px;
                        color: #303133;
                    }
                }
            }
        }

        .exam-detail-content-tabs {
            .exam-tabs {
                :deep(.wtwo-tabs__header) {
                    margin-bottom: 16px;
                }

                :deep(.wtwo-tabs__nav-wrap) {
                    padding: 0;
                }

                :deep(.wtwo-tabs__nav) {
                    border-bottom: 1px solid #E4E7ED;
                }

                :deep(.wtwo-tabs__item) {
                    font-size: 14px;
                    font-weight: 500;
                    color: #606266;
                    padding: 0 20px;
                    height: 40px;
                    line-height: 40px;

                    &.is-active {
                        color: #409EFF;
                    }
                }

                :deep(.wtwo-tabs__active-bar) {
                    background-color: #409EFF;
                }

                .tab-content {
                    min-height: 590px;

                    .filter-area {
                        width: 100%;
                        display: grid;
                        grid-template-columns: 1fr 1fr 1fr 1fr;
                        gap: 16px;
                        align-items: center;
                        padding: 12px;
                        background: #F9FAFC;
                        border-radius: 8px;
                        margin: 0px;
                        margin-bottom: 16px;

                        .filter-item {
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            margin: 0px;

                            .class-input {
                                width: 100%;
                            }
                        }
                    }

                    .operation-area {
                        display: flex;
                        justify-content: flex-end;
                        margin-bottom: 10px;
                    }

                    .score-detail-table {
                        width: 100%;

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
                            justify-content: center;
                        }

                        .colline {
                            margin: 0 12px;
                            width: 1px;
                            height: 12px;
                            background-color: #D8D8D8;
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

                        .pagination-wrapper {
                            display: flex;
                            justify-content: flex-end;
                            margin-top: 16px;
                            padding: 0 16px;

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

                    .analysis-header {
                        display: flex;
                        align-items: center;
                        margin-bottom: 16px;

                        .button-group {
                            display: flex;
                            background-color: #F2F3F5;
                            padding: 2px;
                            width: fit-content;
                            border-radius: 6px;
                            margin-right: 16px;

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
                        }

                        .analysis-filter {
                            display: flex;
                            align-items: center;
                            gap: 16px;

                            .search-input-label {
                                font-size: 14px;
                                color: #606266;
                                margin: 0;
                                padding-right: 8px;
                                white-space: nowrap;
                            }

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
                    }

                    .analysis-item-group {
                        margin-bottom: 16px;
                        min-height: 24px;

                        :deep(.wtwo-radio-group) {
                            display: flex;
                            gap: 16px;
                            flex-wrap: wrap;
                        }

                        :deep(.wtwo-radio) {
                            margin-right: 0;

                            .wtwo-radio__label {
                                font-size: 14px;
                                color: #606266;
                                font-weight: 400;
                                max-width: 165px;
                                overflow: hidden;
                                white-space: nowrap;
                                text-overflow: ellipsis;
                            }

                            &.is-checked {
                                .wtwo-radio__label {
                                    color: #409EFF;
                                    font-weight: 500;
                                }
                            }
                            .deleted-tag{
                                color: #f56c6c;
                                font-size: 10px;
                                margin-left: 4px;
                            }
                        }
                    }

                    .analysis-score {
                        display: flex;
                        padding: 16px;
                        flex-direction: column;
                        align-items: flex-start;
                        border-radius: 8px;
                        border: 1px solid #E4E7ED;
                        margin-bottom: 16px;

                        .analysis-score-title {
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
                                    display: inline-flex;
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

                        .analysis-content {
                            padding: 16px 0;

                            p {
                                text-align: center;
                                color: #909399;
                                font-size: 14px;
                                margin: 0;
                            }
                        }

                        p {
                            text-align: center;
                            color: #909399;
                            font-size: 14px;
                            margin: 0;
                        }
                    }

                    .analysis-statistics {
                        display: flex;
                        padding: 16px;
                        flex-direction: column;
                        align-items: flex-start;
                        border-radius: 8px;
                        border: 1px solid #E4E7ED;

                        .analysis-statistics-title {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            width: 100%;
                            font-size: 16px;
                            font-style: normal;
                            font-weight: 500;
                            color: #303133;
                            margin-bottom: 16px;
                        }

                        .analysis-statistics-content {
                            display: flex;
                            // align-items: center;
                            justify-content: space-between;
                            width: 100%;

                            .analysis-statistics-content-left {
                                width: 350px;
                                height: 280px;
                                margin-right: 16px;

                                .pie-chart {
                                    width: 100%;
                                    height: 100%;
                            }
                            }

                            .analysis-statistics-content-right {
                                width: calc(100% - 350px - 16px);

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
                            }
                        }




                    }
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
.exam-detail-dialog {
    .wtwo-dialog__header {
        background: #fff !important;
        display: flex;
        align-items: center;
        height: 48px;
        padding: 0 20px;
    }

    .wtwo-dialog__title {
        color: #303133 !important;
        font-size: 16px;
        font-weight: 500;
    }

    .wtwo-dialog__close {
        color: #606266 !important;
    }
}

.add-range-dialog {
    .score-range-text {
        font-weight: 400;
        font-size: 12px;
        color: #FF7D00;
        line-height: 20px;
    }

    .wtwo-dialog__header {
        background: #fff !important;
    }

    .wtwo-dialog__footer {
        background: #fff !important;
    }

    .dialog-footer {
        background: #fff !important;
        display: flex;
        justify-content: flex-end;
        gap: 12px;
    }
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
