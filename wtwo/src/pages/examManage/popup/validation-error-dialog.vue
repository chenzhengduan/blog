<template>
    <el-dialog v-model="dialogVisible" title="成绩录入校验失败" width="600px" :close-on-click-modal="false">
        <div class="validation-error-content">
            <div class="error-header">
                <el-icon class="error-icon" color="#F56C6C" size="20">
                    <WarningFilled />
                </el-icon>
                <div class="error-title-section">
                    <span class="error-title">请完善以下必填信息后再进行成绩录入：</span>
                    <span class="error-count">共 {{ errors.length }} 行数据存在问题</span>
                </div>
            </div>
            
            <div class="error-list">
                <div v-for="(error, index) in errors" :key="index" class="error-item">
                    <div class="error-row-header">
                        <div class="error-row-info">
                            <span class="row-number">{{ error.rowDisplay || `第${error.rowIndex}行` }}</span>
                            <span class="student-name" v-if="error.studentName">{{ error.studentName }}</span>
                            <span class="class-name" v-if="error.className">{{ error.className }}</span>
                        </div>
                        <el-button 
                            type="primary" 
                            size="small" 
                            @click="handleGoToRow(error)"
                            class="goto-button"
                        >
                            前往
                        </el-button>
                    </div>
                    <div class="error-fields">
                        <el-tag 
                            v-for="(errorMsg, errorIndex) in error.errors" 
                            :key="errorIndex" 
                            type="danger" 
                            size="small"
                            class="error-tag"
                        >
                            {{ errorMsg }}
                        </el-tag>
                    </div>
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
import { WarningFilled } from '@element-plus/icons-vue'

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
    // 可选：点击前往后关闭弹窗
    // dialogVisible.value = false
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
        margin-bottom: 20px;
        
        .error-icon {
            margin-right: 8px;
        }
        
        .error-title-section {
            display: flex;
            flex-direction: column;
            
            .error-title {
                font-size: 14px;
                color: #606266;
                font-weight: 500;
                margin-bottom: 4px;
            }
            
            .error-count {
                font-size: 12px;
                color: #F56C6C;
                font-weight: 600;
                background: #FEF0F0;
                padding: 2px 8px;
                border-radius: 12px;
                align-self: flex-start;
            }
        }
    }
    
    .error-list {
        max-height: 400px;
        overflow-y: auto;
        
        .error-item {
            margin-bottom: 16px;
            padding: 12px;
            background: #FEF0F0;
            border: 1px solid #FDE2E2;
            border-radius: 6px;
            
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
                        font-weight: 600;
                        color: #F56C6C;
                        margin-right: 8px;
                    }
                    
                    .student-name {
                        color: #303133;
                        margin-right: 8px;
                        font-weight: 500;
                    }
                    
                    .class-name {
                        color: #909399;
                        font-size: 12px;
                    }
                }
                
                .goto-button {
                    margin-left: 12px;
                    height: 28px;
                    padding: 0 12px;
                    font-size: 12px;
                }
            }
            
            .error-fields {
                display: flex;
                flex-wrap: wrap;
                gap: 6px;
                
                .error-tag {
                    margin: 0;
                }
            }
        }
    }
}
</style>
