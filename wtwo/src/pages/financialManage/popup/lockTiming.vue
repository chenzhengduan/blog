<template>
    <el-drawer 
        v-model="isShow" 
        title="定时锁定设置"
        class="lockTiming"
        :size="800"
        :close-on-click-modal="false"
        :append-to-body="true"
        :destroy-on-close="true"
        @close="close"
    >
        <div class="drawer-body-wrap scroll-box" v-loading="loading">
            <div class="intro-area">
                <div class="intro-main-desc">
                    <el-icon size="16px" color="#409EFF"><InfoFilled /></el-icon>
                    <span>开启定时锁定后，系统会在指定日期自动执行上月财务期间的锁定操作；同时，也支持用户随时手动执行财务期间锁定操作。</span>
                </div>
            </div>

            <!-- 定时锁定开关 -->
            <div class="config-section">
                <div class="section-row">
                    <span class="section-label">定时锁定：</span>
                    <el-switch 
                        v-model="formData.AutoLockEnabled" 
                        :active-value="1"
                        :inactive-value="0"
                    />
                </div>
            </div>

            <!-- 锁定方式配置区域（仅开关开启时显示） -->
            <div class="lock-config-area" v-show="formData.AutoLockEnabled === 1">
                <!-- 固定日期自动锁定 -->
                <div class="lock-type-section">
                    <div class="type-header">
                        <el-radio 
                            v-model="lockType" 
                            :value="1"
                            @change="handleLockTypeChange(1)"
                        >
                            固定日期自动锁定
                        </el-radio>
                    </div>
                    <div class="config-item">
                        <span class="config-text">可设每月固定日期(如每月1日)，系统当日零点自动锁上一财务期间，适用于财务期间按自然月或固定规律设定的机构。</span>
                    </div>
                    <div class="type-content" v-show="lockType === 1">
                        <div class="config-item input-row">
                            <span class="config-label">每月</span>
                            <el-input-number 
                                v-model="formData.FixedDateLockDay" 
                                :min="1" 
                                :max="31" 
                                :controls="false"
                                :disabled="lockType !== 1"
                                class="day-input"
                                placeholder="例：1"
                            />
                            <span class="config-label">日0点锁定</span>
                        </div>
                    </div>
                </div>

                <!-- 财务期间结束后锁定 -->
                <div class="lock-type-section">
                    <div class="type-header">
                        <el-radio 
                            v-model="lockType" 
                            :value="2"
                            @change="handleLockTypeChange(2)"
                        >
                            财务期间结束后锁定
                        </el-radio>
                    </div>
                    <div class="config-item">
                        <span class="config-text">上一财务期间结束后，支持立即锁定或自定义延迟 N 天（如：3天）锁定，预留数据核对调整时间。
                        </span>
                    </div>
                    <div class="type-content" v-show="lockType === 2">
                        <div class="config-item radio-row">
                            <el-radio-group 
                                v-model="formData.EndPeriodLockMode"
                                :disabled="lockType !== 2"
                            >
                                <el-radio :value="0">立即锁定</el-radio>
                                <el-radio :value="1" class="delay-radio">
                                    <el-input-number 
                                        v-model="formData.EndPeriodLockDelayDays" 
                                        :min="1" 
                                        :max="999"
                                        :controls="false"
                                        :disabled="lockType !== 2 || formData.EndPeriodLockMode !== 1"
                                        class="delay-input"
                                        placeholder="例：3"
                                        @click.stop
                                    />
                                    <span class="config-label">天后锁定</span>
                                </el-radio>
                            </el-radio-group>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <template #footer>
            <div class="wtwo-drawer-footer flex-end">
                <el-button @click="close">取消</el-button>
                <el-button @click="submit" type="primary" :loading="loading">保存</el-button>
            </div>
        </template>
    </el-drawer>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getSysConfig, PutOtherParameter } from '@/api/comm';

// 表单数据
const formData = reactive({
    AutoLockEnabled: 0, // 总开关：是否启用自动锁定功能 0 / 1
    FixedDateLockEnabled: 0, // 是否启用固定日期自动锁定 0 / 1  
    FixedDateLockDay: 1, // 每月几号锁定
    EndPeriodLockEnabled: 0, // 是否启用"财务期间结束后自动锁定" 0 / 1
    EndPeriodLockMode: 0, // 0=立即锁定，1=延迟锁定
    EndPeriodLockDelayDays: 3, // 延迟锁定天数（仅 Mode=1 时有效）
});

// 锁定类型：1=固定日期，2=财务期间结束后
const lockType = ref(1);

const isShow = ref(false);
const loading = ref(false);
let configId = '';
let _resolve = null as any;
let _reject = null;

// 处理锁定方式切换
function handleLockTypeChange(type: number) {
    lockType.value = type;
    if (type === 1) {
        // 选择固定日期自动锁定
        formData.FixedDateLockEnabled = 1;
        formData.EndPeriodLockEnabled = 0;
    } else {
        // 选择财务期间结束后锁定
        formData.FixedDateLockEnabled = 0;
        formData.EndPeriodLockEnabled = 1;
    }
}

// 监听总开关变化
watch(() => formData.AutoLockEnabled, (newVal) => {
    if (newVal === 0) {
        // 关闭总开关时，清空子选项
        formData.FixedDateLockEnabled = 0;
        formData.EndPeriodLockEnabled = 0;
    } else {
        // 开启总开关时，默认选中当前锁定类型
        handleLockTypeChange(lockType.value);
    }
});

// 提交保存
function submit() {
    // 验证固定日期
    if (formData.AutoLockEnabled === 1 && lockType.value === 1) {
        if (formData.FixedDateLockDay < 1 || formData.FixedDateLockDay > 31) {
            ElMessage.warning('请输入1-31之间的日期');
            return;
        }
    }
    
    // 验证延迟天数
    if (formData.AutoLockEnabled === 1 && lockType.value === 2 && formData.EndPeriodLockMode === 1) {
        if (formData.EndPeriodLockDelayDays < 1) {
            ElMessage.warning('延迟天数必须大于0');
            return;
        }
    }

    loading.value = true;
    
    // 调用保存接口
    PutOtherParameter({
        id: configId,
        otherParameter: JSON.stringify(formData)
    }).then((res: any) => {
        ElMessage.success('保存成功');
        _resolve && _resolve(formData);
        close();
    }).finally(() => {
        loading.value = false;
    });
}

// 获取详情
function getDetail() {
    loading.value = true;
    
    getSysConfig({
        name: 'FinanceLockAutoSetting'
    }).then((res: any) => {
        const data = res.Data;
        if (data.length) {
            configId = data[0].ID;
            const otherParameter = data[0].OtherParameter;
            const configValue = JSON.parse(otherParameter);
            
            // 将后端数据赋值到 formData
            Object.assign(formData, configValue);
            
            // 根据数据设置锁定类型
            if (configValue.FixedDateLockEnabled === 1) {
                lockType.value = 1;
            } else if (configValue.EndPeriodLockEnabled === 1) {
                lockType.value = 2;
            }
        }
    }).finally(() => {
        loading.value = false;
    });
}

// 关闭抽屉
function close() {
    isShow.value = false;
}

// 打开抽屉
function open() {
    return new Promise((resolve, reject) => {
        isShow.value = true;
        getDetail();
        _resolve = resolve;
        _reject = reject;
    });
}

defineExpose({
    open
});
</script>

<style lang="scss" scoped>
.lockTiming {
    .drawer-body-wrap {
        padding: 16px 16px 0;
        overflow: auto;
        
        .intro-area {
            padding: 12px 16px;
            background-color: #EAF3FF;
            border-radius: 4px;
            
            .intro-main-desc {
                display: flex;
                align-items: flex-start;
                color: #606266;
                line-height: 22px;
                
                .wtwo-icon {
                    margin-right: 8px;
                    flex-shrink: 0;
                    margin-top: 3px;
                }
                
                span {
                    flex: 1;
                }
            }
        }
        
        .config-section {
            margin-top: 16px;
            
            .section-row {
                display: flex;
                align-items: center;
                
                .section-label {
                    font-size: 14px;
                    color: #303133;
                    margin-right: 12px;
                }
            }
        }
        
        .lock-config-area {
            margin-top: 12px;
            
            .lock-type-section {
                margin-bottom: 12px;
                padding: 16px;
                background-color: #F9FAFC;
                border: 1px solid #E4E7ED;
                border-radius: 8px;
                
                .type-header {
                    :deep(.wtwo-radio) {
                        .wtwo-radio__label {
                            color: #303133;
                        }
                    }
                }
                
                // 说明文字样式（在 type-content 外面）
                & > .config-item {
                    margin-bottom: 12px;
                    padding-left: 24px;
                    
                    .config-text {
                        font-size: 13px;
                        color: #909399;
                        line-height: 20px;
                        display: block;
                    }
                }
                
                .type-content {
                    padding-left: 24px;
                    
                    .config-item {
                        margin-bottom: 12px;
                        
                        .config-text {
                            font-size: 13px;
                            color: #909399;
                            line-height: 20px;
                            display: block;
                        }
                        
                        &.input-row {
                            display: flex;
                            align-items: center;
                            margin-top: 12px;
                            
                            .config-label {
                                font-size: 14px;
                                color: #606266;
                                margin: 0 8px;
                            }
                            
                            .day-input {
                                width: 104px;
                            }
                        }
                        
                        &.radio-row {
                            margin-top: 12px;
                            
                            :deep(.wtwo-radio-group) {
                                display: flex;
                                flex-direction: column;
                                align-items: flex-start;
                                gap: 12px;
                                
                                .wtwo-radio {
                                    margin-right: 0;
                                    
                                    &.delay-radio {
                                        .wtwo-radio__label {
                                            display: flex;
                                            align-items: center;
                                        }
                                    }
                                }
                            }
                            
                            .config-label {
                                font-size: 14px;
                                color: #606266;
                                margin: 0 8px;
                            }
                            
                            .delay-input {
                                width: 104px;
                                
                                :deep(.el-input__inner) {
                                    text-align: center;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
</style>

