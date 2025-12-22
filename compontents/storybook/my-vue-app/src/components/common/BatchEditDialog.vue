<!--
  批量编辑对话框
  用于批量修改选中行的字段值
-->
<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElDatePicker, ElButton } from 'element-plus'

interface Props {
  visible: boolean
  columns: any[] // 可编辑的列配置
  selectedCount: number // 选中的行数
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', data: { field: string; value: any }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单数据
const formData = ref({
  field: '',
  value: ''
})

// 可编辑的列选项
const editableColumns = computed(() => {
  return props.columns.filter(col => col.cellType && col.field !== 'index')
})

// 当前选中列的配置
const selectedColumn = computed(() => {
  return editableColumns.value.find(col => col.field === formData.value.field)
})

// 关闭对话框
const handleClose = () => {
  emit('update:visible', false)
  // 重置表单
  formData.value = {
    field: '',
    value: ''
  }
}

// 确认批量编辑
const handleConfirm = () => {
  if (!formData.value.field) {
    alert('请选择要编辑的字段')
    return
  }
  
  emit('confirm', {
    field: formData.value.field,
    value: formData.value.value
  })
  
  handleClose()
}
</script>

<template>
  <ElDialog
    :model-value="visible"
    title="批量编辑"
    width="500px"
    @close="handleClose"
  >
    <div class="batch-edit-content">
      <div class="info-text">
        已选中 <span class="highlight">{{ selectedCount }}</span> 行，将批量修改以下字段：
      </div>
      
      <ElForm :model="formData" label-width="100px">
        <ElFormItem label="选择字段">
          <ElSelect
            v-model="formData.field"
            placeholder="请选择要编辑的字段"
            style="width: 100%"
          >
            <ElOption
              v-for="col in editableColumns"
              :key="col.field"
              :label="col.title"
              :value="col.field"
            />
          </ElSelect>
        </ElFormItem>
        
        <ElFormItem label="新值" v-if="formData.field">
          <!-- 根据列类型渲染不同的输入控件 -->
          
          <!-- 下拉选择 -->
          <ElSelect
            v-if="selectedColumn?.cellType === 'select'"
            v-model="formData.value"
            :placeholder="selectedColumn?.editorConfig?.placeholder || '请选择'"
            :filterable="selectedColumn?.editorConfig?.filterable"
            :clearable="selectedColumn?.editorConfig?.clearable"
            style="width: 100%"
          >
            <ElOption
              v-for="option in selectedColumn?.editorConfig?.options || []"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </ElSelect>
          
          <!-- 日期选择 -->
          <ElDatePicker
            v-else-if="selectedColumn?.cellType === 'date'"
            v-model="formData.value"
            :type="selectedColumn?.editorConfig?.type || 'date'"
            :format="selectedColumn?.editorConfig?.format"
            :value-format="selectedColumn?.editorConfig?.valueFormat"
            :placeholder="selectedColumn?.editorConfig?.placeholder || '请选择日期'"
            style="width: 100%"
          />
          
          <!-- 文本输入 -->
          <ElInput
            v-else
            v-model="formData.value"
            :type="selectedColumn?.editorConfig?.type || 'text'"
            :placeholder="selectedColumn?.editorConfig?.placeholder || '请输入新值'"
            :maxlength="selectedColumn?.editorConfig?.maxlength"
          />
        </ElFormItem>
      </ElForm>
    </div>
    
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" @click="handleConfirm">确定</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
.batch-edit-content {
  padding: 10px 0;
}

.info-text {
  margin-bottom: 20px;
  padding: 12px;
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  color: #606266;
  font-size: 14px;
}

.highlight {
  color: #409eff;
  font-weight: bold;
  font-size: 16px;
}
</style>
