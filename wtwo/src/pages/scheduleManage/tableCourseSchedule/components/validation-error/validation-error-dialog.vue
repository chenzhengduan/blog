<template>
    <el-dialog v-model="dialogVisible" title="必填字段校验失败" width="600px" :close-on-click-modal="false">
        <div class="validation-error-content">
            <div class="error-header">
                <el-icon class="error-icon" color="#FF7D00" size="20">
                    <WarningFilled />
                </el-icon>
                <div class="error-title-section">
                    <span class="error-title">请完善以下必填信息后再进行预检查：共 <span class="error-count">{{ errors.length }}</span>
                        行数据存在问题</span>
                </div>
            </div>

            <div class="error-list">
                <div v-for="(error, index) in errors" :key="index" class="error-item">
                    <div class="error-row-header">
                        <div class="error-row-info">
                            <span class="row-number">{{ error.rowDisplay || `第${error.rowIndex}行` }}</span>
                            <span class="row-location">{{ error.campusName }}</span>
                            <span class="row-date" v-if="error.date">{{ error.date }}</span>
                        </div>

                    </div>
                    <div class="error-fields">
                        <el-tag v-for="(errorMsg, errorIndex) in error.errors" :key="errorIndex" type="info"
                            size="small" class="error-tag">
                            {{ errorMsg }}
                        </el-tag>
                    </div>
                    <el-button type="text" size="small" @click="handleGoToRow(error)" class="goto-button">
                        前往<el-icon>
                            <ArrowRightBold />
                        </el-icon>
                    </el-button>
                </div>
            </div>
        </div>

        <template #footer>
            <el-button @click="handleClose">知道了</el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { WarningFilled, ArrowRightBold } from '@element-plus/icons-vue'

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    errors: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['update:modelValue', 'close', 'goToRow'])

const dialogVisible = ref(false)

// 监听 modelValue 变化
watch(() => props.modelValue, (newValue) => {
    dialogVisible.value = newValue
}, { immediate: true })

// 监听弹窗显示状态变化
watch(dialogVisible, (newValue) => {
    emit('update:modelValue', newValue)
})

const handleClose = () => {
    dialogVisible.value = false
    emit('close')
}

// 处理前往按钮点击
const handleGoToRow = (error) => {
    emit('goToRow', error)
}
</script>

<style lang="scss" scoped>
/* 校验错误弹窗样式 */
.validation-error-content {
    padding-top: 12px;
    padding-bottom: 12px;

    .error-header {
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        height: 40px;
        background: #FFF7E8;
        border-radius: 2px;
        padding: 12px 16px;

        .error-icon {
            margin-right: 8px;
        }

        .error-title-section {
            display: flex;

            .error-title {
                font-size: 14px;
                color: #303133;
                font-weight: 400;
            }

            .error-count {
                color: #FF7D00;
            }
        }
    }

    .error-list {
        max-height: 400px;
        overflow-y: auto;

        // 隐藏滚动条
        &::-webkit-scrollbar {
            width: 0;
            height: 0;
        }

        // 滚动条细一点
        .error-item {
            margin-bottom: 8px;
            padding: 8px;
            background: #F4F4F5;
            border-radius: 4px;
            position: relative;
            &:last-child {
                margin-bottom: 0;
            }

            .error-row-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;

                .error-row-info {
                    display: flex;
                    align-items: center;

                    .row-number {
                        background: #FFFFFF;
                        font-weight: 500;
                        font-size: 13px;
                        color: #F53F3F;
                        margin-right: 16px;
                        padding: 2px 8px;
                        height: 24px;
                        display: flex;
                        align-items: center;
                        border-radius: 4px;
                    }

                    .row-location {
                        margin-right: 16px;
                        font-weight: 400;
                        font-size: 12px;
                        color: #606266;
                    }

                    .row-date {
                        font-weight: 400;
                        font-size: 12px;
                        color: #606266;
                    }
                }
            }

            .goto-button {
                margin-left: 12px;
                height: 28px;
                padding: 0 12px;
                font-size: 14px;
                position: absolute;
                right: 8px;
                top: 50%;
                transform: translateY(-50%);
            }

            .error-fields {
                display: flex;
                flex-wrap: wrap;
                gap: 6px;

                .error-tag {
                    margin: 0;
                    border: none;
                }
            }
        }
    }
}
</style>