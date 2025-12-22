<!--
  列显示/隐藏配置组件
  用户可以自定义显示哪些列
-->
<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElPopover, ElCheckbox, ElCheckboxGroup, ElButton } from 'element-plus'

interface Props {
  columns: any[] // 所有列配置
  visible?: any[] // 当前显示的列字段数组
}

interface Emits {
  (e: 'update:visible', fields: string[]): void
  (e: 'change', fields: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 可见列字段数组
const visibleFields = ref<string[]>([])

// 初始化可见列
watch(() => props.columns, (newColumns) => {
  if (newColumns && newColumns.length > 0) {
    if (props.visible && props.visible.length > 0) {
      visibleFields.value = [...props.visible]
    } else {
      // 默认显示所有列
      visibleFields.value = newColumns.map(col => col.field)
    }
  }
}, { immediate: true })

// 可配置的列选项（排除索引列）
const configurableColumns = props.columns.filter(col => col.field !== 'index')

// 应用配置
const handleApply = () => {
  emit('update:visible', visibleFields.value)
  emit('change', visibleFields.value)
}

// 全选/取消全选
const handleSelectAll = () => {
  if (visibleFields.value.length === configurableColumns.length) {
    // 取消全选（保留索引列）
    visibleFields.value = props.columns
      .filter(col => col.field === 'index')
      .map(col => col.field)
  } else {
    // 全选
    visibleFields.value = props.columns.map(col => col.field)
  }
}

// 重置为默认
const handleReset = () => {
  visibleFields.value = props.columns.map(col => col.field)
  handleApply()
}
</script>

<template>
  <ElPopover
    placement="bottom-end"
    :width="300"
    trigger="click"
  >
    <template #reference>
      <ElButton>
        <span style="margin-right: 6px">⚙️</span>
        列设置
      </ElButton>
    </template>
    
    <div class="column-config">
      <div class="config-header">
        <span class="title">显示列配置</span>
        <div class="actions">
          <ElButton link size="small" @click="handleSelectAll">
            {{ visibleFields.length === columns.length ? '取消全选' : '全选' }}
          </ElButton>
          <ElButton link size="small" @click="handleReset">重置</ElButton>
        </div>
      </div>
      
      <ElCheckboxGroup v-model="visibleFields" class="checkbox-list">
        <ElCheckbox
          v-for="col in configurableColumns"
          :key="col.field"
          :label="col.field"
          :disabled="col.field === 'index'"
        >
          {{ col.title }}
        </ElCheckbox>
      </ElCheckboxGroup>
      
      <div class="config-footer">
        <ElButton size="small" @click="handleApply" type="primary">应用</ElButton>
        <span class="info">已选择 {{ visibleFields.length }} 列</span>
      </div>
    </div>
  </ElPopover>
</template>

<style scoped>
.column-config {
  padding: 8px 0;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #ebeef5;
}

.title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 8px;
}

.checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.checkbox-list :deep(.el-checkbox) {
  margin-right: 0;
}

.config-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-top: 1px solid #ebeef5;
}

.info {
  font-size: 12px;
  color: #909399;
}
</style>
