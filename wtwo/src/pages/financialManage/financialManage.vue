<template>
    <div class="wtwo-financial-manage">
        <div class="breadcrumb">
            <span class="breadcrumb-txt">财务管理</span>
            <span class="breadcrumb-split">/</span>
            <span class="breadcrumb-main-txt">财务安全管理</span>
        </div>
        <div class="page-area with-breadcrumb">
            <!-- 功能介绍区域 -->
            <div class="function-intro-box">
                <div class="intro-title">财务安全功能介绍</div>
                <div class="intro-cards">
                    <div class="intro-card" v-for="(item, index) in introCards" :key="index">
                        <div class="card-icon">
                            <el-icon size="16px" color="#606266">
                                <svg aria-hidden="true">
                                    <use :xlink:href="item.icon"></use>
                                </svg>
                            </el-icon>
                        </div>
                        <div class="card-content">
                            <div class="card-title">
                                <span>{{ item.title }}</span>
                                <el-link v-if="item.link && item.showAction" type="primary" :underline="false" class="card-link"
                                    @click="handleCardClick(item.action)">
                                    {{ item.linkText }}
                                </el-link>
                            </div>
                            <div class="card-desc">{{ item.desc }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 主体内容区域 -->
            <div class="main-content-box">
                <!-- 左侧年份选择 -->
                <div class="year-detail-box">
                    <el-button v-if="FiscalPeriodEdit" type="primary" class="mx-16px mb-16px" @click="handleAddYear">快速设置</el-button>
                    <div class="year-list-box">
                        <div v-for="year in yearList" class="year-item" :class="{ active: year == selectedYear }"
                            :key="year" @click="selectYear(year)">
                            <span class="year-text">{{ year }}</span>
                        </div>
                    </div>
                </div>

                <!-- 右侧详情内容 -->
                <div class="period-detail-box">
                    <div class="detail-header">
                        <div class="header-left">
                            <span class="header-title">{{ selectedYear }}年财务期间详情</span>
                            <el-select v-model="lockStatus" placeholder="请选择锁定状态" @change="handleLockStatusChange">
                                <template #label="{label, value}">
                                    <span>锁定状态：</span>
                                    <span>{{ label }}</span>
                                </template>
                                <el-option v-for="item in lockStatusOptions" :key="item.value" :label="item.label"
                                    :value="item.value" />
                            </el-select>
                        </div>
                        <div class="header-right">
                            <el-link v-if="FinanceLockScopeRule" type="primary" :underline="false" @click="handleCardClick('lockRange')">
                                <el-icon size="16px" class="mr-4px">
                                    <Setting />
                                </el-icon>
                                操作范围设置
                            </el-link>
                            <el-link v-if="FinanceAutoLockRule" class="ml-20px" type="primary" :underline="false" @click="handleCardClick('scheduleLock')">定时锁定设置</el-link>

                            <el-button class="ml-12px" @click="handleBatchUnlock">一键解锁
                                <el-tooltip effect="dark" content="可以解锁所有财务区间，仅限管理员操作。">
                                    <el-icon size="16px"
                                        class="ml-4px">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-xinxitishi"></use>
                                        </svg>
                                    </el-icon>
                                </el-tooltip>
                            </el-button>
                        </div>
                    </div>

                    <!-- 表格区域 -->
                    <div class="table-wrap scroll-box" v-loading="loading">
                        <el-table :data="periodList" :style="{ width: '100%' }" border :max-height="tableHeight">
                            <template #empty>
                                <el-empty description="暂无数据" :image-size="100"></el-empty>
                            </template>

                            <el-table-column label="期间名称" prop="periodName" width="160">
                                <template #default="{ row }">
                                    <span>{{ row.Month }}月</span>
                                </template>
                            </el-table-column>

                            <el-table-column label="期间范围" width="300">
                                <template #default="{ row }">
                                    <div class="period-range-cell" v-if="row.isEditing">
                                        <el-date-picker 
                                            v-model="row.editDateRange"
                                            type="daterange" 
                                            value-format="YYYY-MM-DD"
                                            range-separator="至" 
                                            start-placeholder="开始日期" 
                                            end-placeholder="结束日期" 
                                        />
                                    </div>
                                    <div v-else class="period-range">
                                        <span>{{ row.StartDate }}</span>
                                        <span class="range-separator">至</span>
                                        <span>{{ row.EndDate }}</span>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column label="锁定状态" width="280">
                                <template #default="{ row }">
                                    <div class="lock-status-cell">
                                        <el-icon :size="16" :color="row.Pass == 1 ? '#C0C4CC' : '#2878E8'">
                                            <svg v-if="row.Pass == 1" aria-hidden="true">
                                                <use xlink:href="#w2-suoding"></use>
                                            </svg>
                                            <svg v-else aria-hidden="true">
                                                <use xlink:href="#w2-weisuoding"></use>
                                            </svg>
                                        </el-icon>
                                        <span :class="['status-text', row.Pass == 1 ? 'locked' : 'unlocked']">
                                            {{ row.Pass == 1 ? '已锁定' : '未锁定' }}
                                        </span>
                                        <el-tooltip v-if="row.Pass == 1" :content="row.AutoLockTime" placement="top">
                                            <span class="lock-time">{{ row.AutoLockTime }}</span>
                                        </el-tooltip>
                                    </div>
                                    <div v-if="row.Pass == 0 && row.AutoLockTime" class="schedule-lock-info">
                                        <el-icon size="14px" color="#FF7D00">
                                            <svg aria-hidden="true">
                                                <use xlink:href="#w2-dingshi"></use>
                                            </svg>
                                        </el-icon>
                                        <span class="schedule-text">定时 {{ row.AutoLockTime }}</span>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column label="操作" min-width="140" fixed="right">
                                <template #default="{ row }">
                                    <div v-if="row.isEditing" class="action-btns" style="gap: 0;">
                                        <el-button type="default" size="small" @click="handleCancelEdit(row)">取消</el-button>
                                        <el-button type="primary" size="small" @click="handleSaveEdit(row)">保存</el-button>
                                    </div>
                                    <div v-else class="action-btns">
                                        <el-tooltip effect="dark" raw-content placement="left-start" 
                                            :content="row.Pass == 0 ? '为防止以往的财务记录被随意更改，您可以对财务期间进行锁定。<br />锁定后的该财务期间范围及之前的时间，其收费记录、上课记录<br />都无法再进行任何操作（如上课点名、作废收据、退费等）。' : '财务期间已锁定，该财务期间范围内，其收费记录、上课记录<br />都无法再进行任何操作（如上课点名、作废收据、退费等）。'">
                                            <el-link v-if="LockFiscalPeriod" type="primary" size="small" :underline="false" :disabled="hasEditing && !row.isEditing" @click="handleToggleLock(row)">{{ row.Pass == 1 ? '解锁' : '锁定' }}</el-link>
                                        </el-tooltip>
                                        <el-link v-if="FiscalPeriodEdit && row.Pass == 0" type="primary" size="small" :underline="false" :disabled="hasEditing && !row.isEditing" @click="handleEdit(row)">修改</el-link>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </div>
            </div>
        </div>

        <!-- 设置锁定范围 -->
        <lock-range ref="lockRangeRef"></lock-range>
        <!-- 定时锁定设置 -->
        <lock-timing ref="lockTimingRef"></lock-timing>
        <!-- 快速设置 -->
        <add-financial-range ref="addFinancialRangeRef"></add-financial-range>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import lockRange from './popup/lockRange.vue'
import lockTiming from './popup/lockTiming.vue'
import addFinancialRange from './popup/addFinancialRange.vue'
import { 
    QueryDuration,
    UpdateFinancialPeriodLock,
    PutDuration,
    AllUnlockData,
} from '@/api/financial'

import { useUser } from '@/store'

const user = computed(() => {
    return useUser().user;
})
const LockFiscalPeriod = window.$xgj.op('LockFiscalPeriod');
const FiscalPeriodEdit = window.$xgj.op('FiscalPeriodEdit');
const FinanceAutoLockRule = window.$xgj.op('FinanceAutoLockRule');  // 定时锁设置
const FinanceLockScopeRule = window.$xgj.op('FinanceLockScopeRule');  // 锁定范围设置

// 功能介绍卡片数据
const introCards = ref([
    {
        icon: '#w2-quanxian',
        title: '设置财务期间',
        desc: '定义会计年度内的各个财务周期，如月度、季度等，确保账务处理的有序进行',
        linkText: '',
        link: false,
        action: '',
    },
    {
        icon: '#w2-caiwusuodingfanwei',
        title: '财务期间锁定范围',
        desc: '设定期间锁定后禁止修改的业务范围，保障财务数据的完整性和准确性',
        linkText: '设置',
        link: true,
        action: 'lockRange',
        showAction: FinanceLockScopeRule,
    },
    {
        icon: '#w2-shezhi',
        title: '快速设置/一键解锁',
        desc: '支持批量设置一整年财务期间及批量解锁某年度所有财务期间，提升管理效率',
        linkText: '',
        link: false,
        action: ''
    },
    {
        icon: '#w2-dingshi',
        title: '定时锁定',
        desc: '设置自动锁定规则，在指定时间自动完成期间锁定，避免人为疏漏',
        linkText: '设置',
        link: true,
        action: 'scheduleLock',
        showAction: FinanceAutoLockRule,
    },
    {
        icon: '#w2-caiwuhongchong',
        title: '财务红冲',
        desc: '针对已锁定期间的特殊业务，提供合规的红字冲销处理机制',
        linkText: '了解更多',
        link: true,
        action: 'redFlush',
        showAction: true,
    }
])
const lockStatus = ref(-1)
const lockStatusOptions = ref([
    {
        value: -1,
        label: '全部'
    },
    {
        value: 1,
        label: '已锁定'
    },
    {
        value: 0,
        label: '未锁定'
    }
])
function handleLockStatusChange() {
    // 从备份数据中根据筛选状态过滤，不修改原始数据
    if (lockStatus.value == -1) {
        // 显示全部，直接使用备份数据的深拷贝
        periodList.value = JSON.parse(JSON.stringify(originalPeriodList.value))
    } else if (lockStatus.value == 1) {
        // 显示已锁定
        periodList.value = originalPeriodList.value.filter(item => item.Pass == 1)
    } else if (lockStatus.value == 0) {
        // 显示未锁定
        periodList.value = originalPeriodList.value.filter(item => item.Pass == 0)
    }
}

// 年份列表
const currentYear = new Date().getFullYear()
const yearList = ref<number[]>([])
// 初始化年份列表（2020-2026）
const initYearList = () => {
    const startYear = 2018,
        endYear = currentYear + 5;

    let years: number[] = [];
    for(let _year = startYear; _year <= endYear; _year++) {
        years.push(_year);
    }
    yearList.value = years;
}
// 当前选中的年份
const selectedYear = ref(currentYear)


// 期间数据
const periodList = ref<any[]>([])
const originalPeriodList = ref<any[]>([]) // 备份原始数据，用于筛选还原
const loading = ref(false)
// 锁定状态相关计算属性
const hasEditing = computed(() => {
    return periodList.value.length > 0 && periodList.value.some(item => item.isEditing)
})

// 表格高度
const tableHeight = ref(500)

// 计算表格高度
const calculateTableHeight = () => {
    nextTick(() => {
        const windowHeight = window.innerHeight
        tableHeight.value = windowHeight - 268
    })
}

// 选择年份
const selectYear = (year: number) => {
    selectedYear.value = year
    loadPeriodData(year)
}

// 加载期间数据
const loadPeriodData = async (year: number) => {
    loading.value = true
    QueryDuration({ year }).then((res) => {
        const data = res.Data || []
        data.forEach((item: any) => {
            item.isEditing = false
            item.editDateRange = [item.StartDate, item.EndDate]
        })
        originalPeriodList.value = JSON.parse(JSON.stringify(data)) // 深拷贝备份原始数据
        periodList.value = data
        // 重新应用当前筛选条件
        handleLockStatusChange()
    }).finally(() => {
        loading.value = false
    })
}

const lockRangeRef = ref<InstanceType<typeof lockRange> | null>(null);
const lockTimingRef = ref<InstanceType<typeof lockTiming> | null>(null);
const addFinancialRangeRef = ref<InstanceType<typeof addFinancialRange> | null>(null);

// 快速设置
const handleAddYear = () => {
    addFinancialRangeRef.value?.open(selectedYear.value).then((year) => {
        // 设置成功后，切换到该年份并刷新数据
        const targetYear = year as number;
        selectedYear.value = targetYear;
        loadPeriodData(targetYear);
    }).catch(() => {
        // 用户取消，不做任何操作
    });
};

// 卡片点击事件
const handleCardClick = (action: string) => {
    switch (action) {
        case 'lockRange':
            lockRangeRef.value?.open().then(() => {

            })
            break
        case 'scheduleLock':
            lockTimingRef.value?.open().then(() => {
                loadPeriodData(selectedYear.value);
            })
            break
        case 'redFlush':
            window.open('https://helpcenter.xiaogj.com/help-list/detail-728.html');
            break
    }
}

// 一键解锁
const handleBatchUnlock = () => {
    if (!user.value.IsAdmin) {
        ElMessage.warning('仅限管理员操作此功能，请联系管理员操作');
        return;
    }
    ElMessageBox.confirm(
        '确定要解锁<strong>所有期间</strong>吗？<br/>财务期间解锁后，其对应范围内的收费、课消信息有可能会被再次修改，从而导致以往的财务数据发生变化。',
        '提示',{
            dangerouslyUseHTMLString: true,
        }
    ).then(() => {
        loading.value = true;
        AllUnlockData().then(res => {
            loadPeriodData(selectedYear.value);
        }).finally(() => {
            loading.value = false;
        })
    }).catch(() => {})
}

// 切换锁定状态
const handleToggleLock = async (row: any) => {
   const operate = row.Pass == 1 ? 0 : 1;
   const tip = row.Pass == 1 
            ? '确定要解锁吗？财务期间解锁后，其对应范围内的收费、课消信息有可能会被再次修改，从而导致以往的财务数据发生变化。'
            : '确定要锁定？锁定后该时间范围及之前的时间都不能进行收费、点名、撤销上课、作废收据、退费、结转等操作。';

    ElMessageBox.confirm(tip, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
    }).then(async () => {
        loading.value = true
        UpdateFinancialPeriodLock({
            id: row.ID,
            operation: operate,
            sdate: row.StartDate,
            edate: row.EndDate,
        }).then(() => {
            ElMessage.success('操作成功')
            loadPeriodData(selectedYear.value);
        }).finally(() => {
            loading.value = false
        })
    })

}

// 编辑期间范围
const handleEdit = (row: any) => {
    row.isEditing = true
    row.editDateRange = [row.StartDate, row.EndDate]
}

// 保存编辑
const handleSaveEdit = async (row: any) => {
    if (!row.editDateRange || row.editDateRange.length !== 2) {
        ElMessage.warning('请选择完整的期间范围')
        return
    }
    PutDuration({
        id: row.ID,
        sdate: row.editDateRange[0],
        edate: row.editDateRange[1],
    }).then(() => {
        ElMessage.success('保存成功')
        loadPeriodData(selectedYear.value);
    }).finally(() => {
        loading.value = false
    })
}

// 取消编辑
const handleCancelEdit = (row: any) => {
    row.isEditing = false
    row.editDateRange = [row.StartDate, row.EndDate]
}

// 初始化
onMounted(() => {
    if (window.microApp) {
        const data = window.microApp.getData();
        if (data && data.curYear) {
            selectedYear.value = data.curYear;
        }
    }
    initYearList()
    selectYear(selectedYear.value)
    calculateTableHeight()
    
    window.addEventListener('resize', calculateTableHeight)
})
</script>

<style scoped lang="scss">
.wtwo-financial-manage {
    flex: 1 1 auto;
    display: flex;
    flex-direction: column;
    height: 100%;

    .page-area {
        display: flex;
        flex-direction: column;
        overflow: auto;
    }

    // 功能介绍区域
    .function-intro-box {
        background: #fff;
        border-radius: 8px;
        padding: 16px;
        margin-bottom: 12px;

        .intro-title {
            display: flex;
            align-items: center;
            font-size: 14px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 12px;
        }

        .intro-cards {
            display: flex;
            gap: 12px;

            .intro-card {
                flex: 1;
                display: flex;
                padding: 16px;
                background: #F9FAFC;
                border-radius: 6px;

                .card-icon {
                    flex-shrink: 0;
                    width: 16px;
                    height: 16px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-right: 8px;
                }

                .card-content {
                    flex: 1;
                    min-width: 0;

                    .card-title {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        font-size: 14px;
                        font-weight: 600;
                        color: #606266;
                        margin-bottom: 6px;
                        .card-link {
                            font-size: 12px;
                        }
                    }

                    .card-desc {
                        font-size: 12px;
                        color: #909399;
                        line-height: 1.5;
                    }

                }
            }
        }
    }

    // 主体内容区域
    .main-content-box {
        display: flex;
        flex: 1;
        min-height: 0;

        // 左侧年份列表
        .year-detail-box {
            display: flex;
            flex-direction: column;
            width: 120px;
            padding: 16px 4px;
            background: #fff;
            border-right: 1px solid #E4E7ED;
            border-radius: 8px 0 0 8px;

            .year-list-box {
                display: flex;
                flex-direction: column;
                flex: 1;
                overflow: auto;
    
                .year-item {
                    display: flex;
                    align-items: center;
                    height: 40px;
                    padding: 0 12px 0 16px;
                    color: #606266;
                    font-size: 14px;
                    border-radius: 4px;
                    cursor: pointer;
    
                    &:hover {
                        background: #f5f7fa;
                    }
    
                    &.active {
                        background: #eaf3ff;
                        color: var(--wtwo-color-primary);
                    }
                }
            }
        }

        // 右侧详情内容
        .period-detail-box {
            flex: 1;
            background: #fff;
            border-radius: 0 8px 8px 0;
            display: flex;
            flex-direction: column;
            min-width: 0;

            .detail-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 20px 16px 12px;

                .header-left {
                    display: flex;
                    align-items: center;
                    gap: 12px;

                    .header-title {
                        font-size: 16px;
                        font-weight: 600;
                        color: #303133;
                    }
                    .wtwo-select {
                        width: 180px;
                    }
                }

                .header-right {
                    display: flex;
                    align-items: center;
                }
            }

            .table-wrap {
                flex: 1;
                padding: 0 16px 16px;
                overflow: auto;

                .period-range-cell {
                    display: flex;
                    align-items: center;
                    width: 260px;

                    .range-separator {
                        color: #909399;
                    }
                }

                .period-range {
                    display: flex;
                    align-items: center;
                    gap: 8px;

                    .range-separator {
                        color: #909399;
                    }
                }

                .lock-status-cell {
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;

                    .status-text {
                        font-size: 14px;

                        &.locked {
                            color: #606266;
                        }

                        &.unlocked {
                            color: #2878E8;
                        }
                    }

                    .lock-time {
                        color: #909399;
                        font-size: 12px;
                    }
                }

                .schedule-lock-info {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    height: 22px;
                    gap: 4px;
                    padding: 0 8px;
                    margin-top: 4px;
                    margin-left: 10px;
                    background-color: #FFF7E8;
                    border-radius: 64px;

                    .schedule-text {
                        color: #FF7D00;
                        font-size: 12px;
                        line-height: 1;
                    }
                }

                .action-btns {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }
            }
        }
    }
}
</style>