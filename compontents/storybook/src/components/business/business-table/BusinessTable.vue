<template>
  <div class="business-table-container">
    <div class="business-table-header" v-if="$slots.toolbar">
      <slot name="toolbar"></slot>
    </div>
    
    <div class="business-table-body">
      <table class="business-table">
        <thead>
          <tr>
            <th v-for="col in visibleColumns" :key="col.field" :style="getColumnStyle(col)">
              {{ col.title }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, rowIndex) in tableData"
            :key="getRowKey(row, rowIndex)"
            :class="{ 'is-editing': editingCell?.rowIndex === rowIndex }"
          >
            <td
              v-for="col in visibleColumns"
              :key="col.field"
              :style="getColumnStyle(col)"
              @click="handleCellClick(row, col, rowIndex)"
            >
              <!-- 编辑状态 -->
              <component
                v-if="isEditing(rowIndex, col.field)"
                :is="getEditorComponent(col)"
                v-model="row[col.field]"
                :config="col.editorConfig || {}"
                :row-data="row"
                :field="col.field"
                @blur="handleEditorBlur"
                @change="handleEditorChange(row, col)"
              />
              
              <!-- 显示状态 -->
              <div v-else class="cell-content">
                <component
                  v-if="col.customRender"
                  :is="col.customRender"
                  :value="row[col.field]"
                  :row="row"
                />
                <span v-else>
                  {{ formatCellValue(row[col.field], row, col) }}
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="business-table-footer" v-if="$slots.pagination">
      <slot name="pagination"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TableConfig, ColumnConfig, CellType } from './types/column'
import { SelectEditor, InputEditor, DateEditor } from './editors'

interface Props {
  config: TableConfig
  data: any[]
  rowKey?: string
}

interface Emits {
  (e: 'cell-click', data: { row: any; column: ColumnConfig; rowIndex: number }): void
  (e: 'cell-change', data: { row: any; column: ColumnConfig; value: any }): void
  (e: 'update:data', data: any[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表格数据
const tableData = ref([...props.data])

// 监听外部数据变化
watch(() => props.data, (newData) => {
  tableData.value = [...newData]
}, { deep: true })

// 可见列
const visibleColumns = computed(() => {
  return props.config.columns.filter(col => col.visible !== false)
})

// 当前编辑单元格
interface EditingCell {
  rowIndex: number
  field: string
}
const editingCell = ref<EditingCell | null>(null)

// 获取行键值
const getRowKey = (row: any, index: number) => {
  return props.rowKey ? row[props.rowKey] : index
}

// 获取列样式
const getColumnStyle = (col: ColumnConfig) => {
  const style: any = {}
  if (col.width) {
    style.width = typeof col.width === 'number' ? `${col.width}px` : col.width
  }
  if (col.minWidth) {
    style.minWidth = `${col.minWidth}px`
  }
  return style
}

// 判断是否处于编辑状态
const isEditing = (rowIndex: number, field: string) => {
  return editingCell.value?.rowIndex === rowIndex && editingCell.value?.field === field
}

// 获取编辑器组件
const getEditorComponent = (col: ColumnConfig) => {
  switch (col.cellType) {
    case 'select':
      return SelectEditor
    case 'input':
      return InputEditor
    case 'date':
      return DateEditor
    case 'custom':
      return col.editorConfig?.component
    default:
      return InputEditor
  }
}

// 格式化单元格值
const formatCellValue = (value: any, row: any, col: ColumnConfig) => {
  if (col.formatter) {
    return col.formatter(value, row)
  }
  
  // 对于 select 类型，显示 label
  if (col.cellType === 'select' && col.editorConfig?.options) {
    const option = col.editorConfig.options.find(opt => opt.value === value)
    return option ? option.label : value
  }
  
  return value ?? ''
}

// 处理单元格点击
const handleCellClick = (row: any, col: ColumnConfig, rowIndex: number) => {
  // 如果列配置了编辑器，进入编辑模式
  if (col.cellType && col.cellType !== 'text') {
    editingCell.value = {
      rowIndex,
      field: col.field
    }
  }
  
  emit('cell-click', { row, column: col, rowIndex })
}

// 处理编辑器失焦
const handleEditorBlur = () => {
  // 延迟关闭编辑模式，允许change事件先触发
  setTimeout(() => {
    editingCell.value = null
  }, 200)
}

// 处理编辑器值变化
const handleEditorChange = (row: any, col: ColumnConfig) => {
  emit('cell-change', { row, column: col, value: row[col.field] })
  emit('update:data', tableData.value)
}
</script>

<style scoped>
.business-table-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.business-table-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.business-table-body {
  flex: 1;
  overflow: auto;
}

.business-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.business-table thead {
  background-color: #f5f7fa;
  position: sticky;
  top: 0;
  z-index: 10;
}

.business-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
}

.business-table td {
  padding: 8px 16px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  transition: background-color 0.2s;
  cursor: pointer;
}

.business-table tbody tr:hover td {
  background-color: #f5f7fa;
}

.business-table tbody tr.is-editing td {
  background-color: #ecf5ff;
}

.cell-content {
  min-height: 20px;
}

.business-table-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}
</style>
