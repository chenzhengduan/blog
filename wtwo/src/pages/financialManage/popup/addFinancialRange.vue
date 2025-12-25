<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="addFinancialRange" 
        width="600px" 
        top="5%" 
        title="快速设置"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper" v-loading="loading">
            <el-form :model="form" label-width="60px" ref="formRef" :validate-on-rule-change="false">
                <el-form-item label="年份" prop="year" :rules="[{ required: true, message: '请选择年份', trigger: 'change' }]">
                    <el-select v-model="form.year" placeholder="请选择年份" class="w-full">
                        <el-option v-for="year in yearList" :key="year" :label="year" :value="year" />
                    </el-select>
                </el-form-item>
                
                <el-form-item label="规则">
                    <el-radio-group v-model="form.type">
                        <div class="radio-item">
                            <el-radio :value="1">自然月</el-radio>
                            <div class="radio-desc">按每月1日~当月最后1日核算</div>
                        </div>
                        <div class="radio-item mt-12px">
                            <el-radio :value="2">非自然月</el-radio>
                            <div v-show="form.type === 2" class="date-config-row">
                                <span class="config-label">从：</span>
                                <span class="config-text">上月</span>
                                <el-select v-model="form.startDate" placeholder="请选择" class="day-select" @change="handleStartDateChange">
                                    <el-option v-for="day in dayList" :key="day" :label="day" :value="day" />
                                </el-select>
                                <span class="config-text">号</span>
                                <span class="config-label ml-12px">至：</span>
                                <span class="config-text">本月{{ form.endDate }}号</span>
                            </div>
                        </div>
                    </el-radio-group>
                </el-form-item>
            </el-form>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button v-loading="loading" type="primary" @click="submit">确定</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { ElForm, ElMessage, ElMessageBox } from 'element-plus';
import { PostDuration } from '@/api/financial';

const dialogVisible = ref(false);
const loading = ref(false);

// 构建年份列表（与主页面保持一致）
const initYearList = () => {
    const now = new Date();
    const currentYear = now.getFullYear();
    const startYear = 2018;
    const endYear = currentYear + 5;

    let years: number[] = [];
    for(let _year = startYear; _year <= endYear; _year++) {
        years.push(_year);
    }
    return years;
};

const yearList = ref<number[]>(initYearList());

// 构建日期列表（2-31号）
const dayList = ref<number[]>([]);
for (let i = 2; i <= 31; i++) {
    dayList.value.push(i);
}

// 表单数据
const form = reactive({
    year: new Date().getFullYear(),
    type: 1, // 1=自然月，2=非自然月
    startDate: 31, // 开始日期的号数
    endDate: 30 // 结束日期的号数
});

const formRef = ref<InstanceType<typeof ElForm>>();

// 处理开始日期变化，自动计算结束日期
const handleStartDateChange = () => {
    form.endDate = form.startDate === 1 ? 1 : form.startDate - 1;
};

// 提交保存
function submit() {
    formRef.value!.validate((valid) => {
        if (valid) {
            const params: any = form.type === 1 
                ? {
                    isNaturalMonth: true,
                    year: form.year
                }
                : {
                    year: form.year,
                    start: form.startDate,
                    isNaturalMonth: false
                };
            
            loading.value = true;
            PostDuration(params).then(res => {
                const data = res.Data;
                // 如果返回 Data > 0，说明该年份已设置，需要确认是否覆盖
                if (data > 0) {
                    loading.value = false;
                    ElMessageBox.confirm(
                        '当前年份已设置时间段，是否覆盖？',
                        '提示',
                        {
                            confirmButtonText: '确定',
                            cancelButtonText: '取消',
                            type: 'warning'
                        }
                    ).then(() => {
                        // 确认覆盖，添加 confirm 参数重新提交
                        const confirmParams = { ...params, confirm: 1 };
                        loading.value = true;
                        PostDuration(confirmParams).then(() => {
                            ElMessage.success('设置成功');
                            dialogVisible.value = false;
                            _resolve(form.year); // 先返回年份
                            resetForm(); // 再重置表单
                        }).finally(() => {
                            loading.value = false;
                        });
                    }).catch(() => {
                        // 取消覆盖，不做任何操作
                    });
                } else {
                    // 直接设置成功
                    ElMessage.success('设置成功');
                    loading.value = false;
                    dialogVisible.value = false;
                    _resolve(form.year); // 先返回年份
                    resetForm(); // 再重置表单
                }
            }).catch(() => {
                loading.value = false;
            });
        }
    });
}

// 重置表单
function resetForm() {
    form.year = new Date().getFullYear();
    form.type = 1;
    form.startDate = 31;
    form.endDate = 30;
}

// 关闭弹窗
function close() {
    dialogVisible.value = false;
    resetForm();
    _reject();
}

let _resolve: any = null;
let _reject: any = null;

// 打开弹窗，接收年份参数
function open(year?: number) {
    dialogVisible.value = true;
    if (year) {
        form.year = year;
    }
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    });
}

defineExpose({
    open
});
</script>

<style lang="scss" scoped>
.addFinancialRange {
    .box-wrapper {
        min-height: 268px;
        height: auto;
        padding: 16px 0;
        
        :deep(.wtwo-form-item) {
            margin-bottom: 16px;
        }
        
        .wtwo-radio-group {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            .radio-item {
                display: flex;
                flex-direction: column;
                .radio-desc {
                    line-height: 1;
                    font-size: 13px;
                    color: #909399;
                    margin-left: 24px;
                }
            }
        }
        .date-config-row {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            padding-left: 22px;
            
            .config-label {
                font-size: 14px;
                color: #606266;
            }
            
            .config-text {
                font-size: 14px;
                color: #606266;
                margin: 0 4px;
            }
            
            .day-select {
                width: 80px;
                margin: 0 4px;
            }
        }
    }
}
</style>

