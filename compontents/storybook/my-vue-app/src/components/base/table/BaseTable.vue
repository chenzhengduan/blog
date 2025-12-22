<template>
  <div class="business-table-container">
    <!-- 工具栏 -->
    <div class="business-table-header" v-if="$slots.toolbar">
      <slot name="toolbar"></slot>
    </div>
    
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
    
    <!-- vue-fantable 表格 -->
    <ve-table
      ref="tableRef"
      :columns="tableColumns"
      :table-data="filteredTableData"
      :border-y="true"
      :border-x="true"
      :max-height="maxHeight"
      :cell-style-option="cellStyleOption"
      :edit-option="editOption"
      :cell-autofill-option="cellAutofillOption"
      :row-style-option="rowStyleOption"
      :row-key-field-name="rowKey || 'id'"
      :sort-option="sortOption"
      @on-cell-click="handleCellClick"
      @on-sort-change="handleSortChange"
    />
    
    <!-- 分页 -->
    <div class="business-table-footer" v-if="$slots.pagination">
      <slot name="pagination"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, h, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { VeTable } from 'vue-fantable'
import 'vue-fantable/libs/theme-default.css'
import { ElSelect, ElOption, ElInput, ElDatePicker } from 'element-plus'
import 'element-plus/dist/index.css'

interface Props {
  columns: any[]
  data: any[]
  rowKey?: string
  maxHeight?: string | number
  loading?: boolean // 加载状态
  // 行选择配置
  rowSelection?: {
    enabled: boolean // 是否启用行选择
    type?: 'checkbox' | 'radio' // 选择类型
    showSelectAll?: boolean // 是否显示全选
    selectedRowKeys?: any[] // 受控模式：选中的行keys
  }
}

interface Emits {
  (e: 'cell-click', data: { row: any; column: any; rowIndex: number }): void
  (e: 'cell-change', data: { row: any; column: any; value: any }): void
  (e: 'update:data', data: any[]): void
  (e: 'selection-change', data: { selectedRowKeys: any[]; selectedRows: any[] }): void
  (e: 'sort-change', data: { field: string; order: 'asc' | 'desc' | null }): void
}

const props = withDefaults(defineProps<Props>(), {
  rowKey: 'id',
  maxHeight: 600,
  loading: false
})

const emit = defineEmits<Emits>()

// 表格引用
const tableRef = ref()
// 行选择状态
const selectedRowKeys = ref<any[]>([])

// 监听受控模式的选中状态
watch(() => props.rowSelection?.selectedRowKeys, (newKeys) => {
  if (newKeys !== undefined) {
    selectedRowKeys.value = [...newKeys]
  }
}, { immediate: true })

// 全选/取消全选
const handleSelectAll = (checked: boolean) => {
  if (checked) {
    selectedRowKeys.value = tableData.value.map(row => row[props.rowKey!])
  } else {
    selectedRowKeys.value = []
  }
  emitSelectionChange()
}

// 单选行
const handleSelectRow = (rowKey: any, checked: boolean) => {
  if (props.rowSelection?.type === 'radio') {
    // 单选模式：只能选中一行
    selectedRowKeys.value = checked ? [rowKey] : []
  } else {
    // 多选模式
    if (checked) {
      if (!selectedRowKeys.value.includes(rowKey)) {
        selectedRowKeys.value.push(rowKey)
      }
    } else {
      const index = selectedRowKeys.value.indexOf(rowKey)
      if (index > -1) {
        selectedRowKeys.value.splice(index, 1)
      }
    }
  }
  emitSelectionChange()
}

// 判断行是否被选中
const isRowSelected = (rowKey: any) => {
  return selectedRowKeys.value.includes(rowKey)
}

// 判断是否全选
const isAllSelected = computed(() => {
  return tableData.value.length > 0 && selectedRowKeys.value.length === tableData.value.length
})

// 判断是否部分选中（用于显示半选状态）
const isIndeterminate = computed(() => {
  return selectedRowKeys.value.length > 0 && selectedRowKeys.value.length < tableData.value.length
})

// 触发选择变化事件
const emitSelectionChange = () => {
  const selectedRows = tableData.value.filter(row => selectedRowKeys.value.includes(row[props.rowKey!]))
  emit('selection-change', { selectedRowKeys: selectedRowKeys.value, selectedRows })
}

// 获取选中的行（公开方法）
const getSelectedRows = () => {
  return tableData.value.filter(row => selectedRowKeys.value.includes(row[props.rowKey!]))
}

// 清空选择（公开方法）
const clearSelection = () => {
  selectedRowKeys.value = []
  emitSelectionChange()
}

// ============ 复制粘贴功能 ============
// 复制选中的单元格/行数据
const handleCopy = async () => {
  const selectedRows = getSelectedRows()
  if (selectedRows.length === 0) {
    console.warn('没有选中的行')
    return
  }
  
  // 将选中的行转换为 TSV 格式（Excel 兼容）
  const headers = props.columns.map(col => col.title).join('\t')
  const rows = selectedRows.map(row => {
    return props.columns.map(col => {
      const value = row[col.field]
      return value ?? ''
    }).join('\t')
  }).join('\n')
  
  const tsvData = headers + '\n' + rows
  
  try {
    await navigator.clipboard.writeText(tsvData)
    console.log('已复制到剪贴板')
  } catch (e) {
    console.error('复制失败:', e)
  }
}

// 粘贴数据到选中的行
const handlePaste = async () => {
  try {
    const text = await navigator.clipboard.readText()
    if (!text) return
    
    const selectedRows = getSelectedRows()
    if (selectedRows.length === 0) {
      console.warn('请先选中要粘贴到的行')
      return
    }
    
    // 解析 TSV 数据
    const lines = text.split('\n').filter(line => line.trim())
    if (lines.length === 0) return
    
    // 跳过表头（如果有）
    const firstLine = lines[0]
    const hasHeader = firstLine ? firstLine.split('\t').every(cell => isNaN(Number(cell))) : false
    const dataLines = hasHeader ? lines.slice(1) : lines
    
    // 粘贴到选中的行
    dataLines.forEach((line, index) => {
      if (index >= selectedRows.length) return
      const values = line.split('\t')
      const row = selectedRows[index]
      props.columns.forEach((col, colIndex) => {
        if (colIndex < values.length && col.field !== props.rowKey) {
          row[col.field] = values[colIndex]
        }
      })
    })
    
    emit('update:data', tableData.value)
    console.log('粘贴成功')
  } catch (e) {
    console.error('粘贴失败:', e)
  }
}

// 键盘快捷键处理
const handleKeydown = (e: KeyboardEvent) => {
  // Ctrl+C 复制
  if ((e.ctrlKey || e.metaKey) && e.key === 'c') {
    if (selectedRowKeys.value.length > 0) {
      e.preventDefault()
      handleCopy()
    }
  }
  // Ctrl+V 粘贴
  else if ((e.ctrlKey || e.metaKey) && e.key === 'v') {
    if (selectedRowKeys.value.length > 0) {
      e.preventDefault()
      handlePaste()
    }
  }
  // Ctrl+Z 撤销
  else if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
    e.preventDefault()
    undo()
  }
  // Ctrl+Y 或 Ctrl+Shift+Z 重做
  else if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || (e.key === 'z' && e.shiftKey))) {
    e.preventDefault()
    redo()
  }
}

// 挂载/卸载键盘事件监听
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// ============ 撤销/重做功能 ============
interface HistoryState {
  data: any[]
  timestamp: number
}

const undoStack = ref<HistoryState[]>([])
const redoStack = ref<HistoryState[]>([])
const maxHistorySize = 50 // 最多保存50条历史记录

// 保存当前状态到历史栈
const saveHistory = () => {
  const currentState: HistoryState = {
    data: JSON.parse(JSON.stringify(tableData.value)),
    timestamp: Date.now()
  }
  
  undoStack.value.push(currentState)
  
  // 限制历史栈大小
  if (undoStack.value.length > maxHistorySize) {
    undoStack.value.shift()
  }
  
  // 新的修改会清空重做栈
  redoStack.value = []
}

// 撤销操作
const undo = () => {
  if (undoStack.value.length === 0) {
    console.warn('没有可撤销的操作')
    return
  }
  
  // 将当前状态保存到重做栈
  const currentState: HistoryState = {
    data: JSON.parse(JSON.stringify(tableData.value)),
    timestamp: Date.now()
  }
  redoStack.value.push(currentState)
  
  // 从撤销栈恢复上一个状态
  const previousState = undoStack.value.pop()
  if (previousState) {
    tableData.value = JSON.parse(JSON.stringify(previousState.data))
    emit('update:data', tableData.value)
    console.log('已撤销')
  }
}

// 重做操作
const redo = () => {
  if (redoStack.value.length === 0) {
    console.warn('没有可重做的操作')
    return
  }
  
  // 将当前状态保存到撤销栈
  const currentState: HistoryState = {
    data: JSON.parse(JSON.stringify(tableData.value)),
    timestamp: Date.now()
  }
  undoStack.value.push(currentState)
  
  // 从重做栈恢复下一个状态
  const nextState = redoStack.value.pop()
  if (nextState) {
    tableData.value = JSON.parse(JSON.stringify(nextState.data))
    emit('update:data', tableData.value)
    console.log('已重做')
  }
}

// ============ 批量编辑功能 ============
// 批量更新选中行的指定字段
const batchUpdateField = (field: string, value: any) => {
  const selectedRows = getSelectedRows()
  if (selectedRows.length === 0) {
    console.warn('请先选择要批量编辑的行')
    return false
  }
  
  // 保存历史状态
  saveHistory()
  
  // 批量更新
  selectedRows.forEach(row => {
    row[field] = value
  })
  
  emit('update:data', tableData.value)
  console.log(`已批量更新 ${selectedRows.length} 行的 ${field} 字段`)
  return true
}

// 批量更新多个字段
const batchUpdateFields = (updates: Record<string, any>) => {
  const selectedRows = getSelectedRows()
  if (selectedRows.length === 0) {
    console.warn('请先选择要批量编辑的行')
    return false
  }
  
  // 保存历史状态
  saveHistory()
  
  // 批量更新
  selectedRows.forEach(row => {
    Object.keys(updates).forEach(field => {
      row[field] = updates[field]
    })
  })
  
  emit('update:data', tableData.value)
  console.log(`已批量更新 ${selectedRows.length} 行`)
  return true
}

// ============ 搜索/筛选功能 ============
const searchKeyword = ref('')
const searchField = ref('all')

// 过滤后的表格数据
const filteredTableData = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tableData.value
  }
  
  const keyword = searchKeyword.value.trim().toLowerCase()
  
  return tableData.value.filter(row => {
    if (searchField.value === 'all') {
      // 搜索所有列
      return props.columns.some(col => {
        const value = row[col.field]
        return value && String(value).toLowerCase().includes(keyword)
      })
    } else {
      // 搜索指定列
      const value = row[searchField.value]
      return value && String(value).toLowerCase().includes(keyword)
    }
  })
})

// 执行搜索
const handleSearch = (data: { keyword: string; field: string }) => {
  searchKeyword.value = data.keyword
  searchField.value = data.field
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchField.value = 'all'
}

// 当前激活编辑的单元格 { rowIndex, field }
const editingCell = ref<{ rowIndex: number; field: string } | null>(null)
// 编辑前原始值（用于取消回退）
const editingOriginal = ref<{ rowIndex: number; field: string; value: any } | null>(null)

// 开始编辑指定单元格（公开方法）
const startEdit = async (rowIndex: number, field: string) => {
  editingCell.value = { rowIndex, field }
  const row = tableData.value[rowIndex]
  editingOriginal.value = { rowIndex, field, value: row ? row[field] : undefined }
  await nextTick()
  // focus input inside the activated cell
  try {
    const selector = `[data-row-index="${rowIndex}"][data-field="${field}"] input, [data-row-index="${rowIndex}"][data-field="${field}"] textarea`;
    const el: any = document.querySelector(selector)
    if (el && typeof el.focus === 'function') el.focus()
  } catch (e) {
    // ignore
  }
}

// 取消编辑并回退值
const cancelEdit = () => {
  if (!editingCell.value) return
  const { rowIndex, field } = editingCell.value
  const original = editingOriginal.value
  if (original && original.rowIndex === rowIndex && original.field === field) {
    const row = tableData.value[rowIndex]
    if (row) row[field] = original.value
    // 清理错误
    row.errorFields = row.errorFields?.filter((f: string) => f !== field) || []
  }
  editingCell.value = null
  editingOriginal.value = null
}

// 确认编辑（会执行 validator）
const confirmEdit = async (rowIndex: number, col: any) => {
  const row = tableData.value[rowIndex]
  if (!row) return true
  const field = col.field
  const validator = col.editorConfig?.validator
  const value = row[field]
  if (validator) {
    try {
      const res = await Promise.resolve(validator(value, row))
      if (!res) {
        // 标记错误
        row.errorFields = Array.from(new Set([...(row.errorFields || []), field]))
        return false
      }
    } catch (e) {
      row.errorFields = Array.from(new Set([...(row.errorFields || []), field]))
      return false
    }
  }
  // 验证通过，清除错误
  row.errorFields = (row.errorFields || []).filter((f: string) => f !== field)
  editingCell.value = null
  editingOriginal.value = null
  emit('update:data', tableData.value)
  return true
}

// 键盘事件处理（Enter 提交、Esc 取消、上下箭头切换）
const handleEditorKeydown = async (e: KeyboardEvent, rowIndex: number, col: any) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    await confirmEdit(rowIndex, col)
  } else if (e.key === 'Escape') {
    e.preventDefault()
    cancelEdit()
  } else if (e.key === 'ArrowDown') {
    e.preventDefault()
    const next = Math.min(tableData.value.length - 1, rowIndex + 1)
    editingCell.value = { rowIndex: next, field: col.field }
    await nextTick()
    startEdit(next, col.field)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    const prev = Math.max(0, rowIndex - 1)
    editingCell.value = { rowIndex: prev, field: col.field }
    await nextTick()
    startEdit(prev, col.field)
  } else if (e.key === 'Tab') {
    e.preventDefault()
    // 获取所有可编辑列
    const editableColumns = props.columns.filter(c => c.cellType)
    const currentColIndex = editableColumns.findIndex(c => c.field === col.field)
    
    if (e.shiftKey) {
      // Shift+Tab 向左移动
      if (currentColIndex > 0) {
        // 同一行，上一列
        const prevCol = editableColumns[currentColIndex - 1]
        await confirmEdit(rowIndex, col)
        startEdit(rowIndex, prevCol.field)
      } else if (rowIndex > 0) {
        // 上一行，最后一列
        const lastCol = editableColumns[editableColumns.length - 1]
        await confirmEdit(rowIndex, col)
        startEdit(rowIndex - 1, lastCol.field)
      }
    } else {
      // Tab 向右移动
      if (currentColIndex < editableColumns.length - 1) {
        // 同一行，下一列
        const nextCol = editableColumns[currentColIndex + 1]
        await confirmEdit(rowIndex, col)
        startEdit(rowIndex, nextCol.field)
      } else if (rowIndex < tableData.value.length - 1) {
        // 下一行，第一列
        const firstCol = editableColumns[0]
        await confirmEdit(rowIndex, col)
        startEdit(rowIndex + 1, firstCol.field)
      }
    }
  }
}

// 表格数据
const tableData = ref([...props.data])

// 监听外部数据变化
watch(() => props.data, (newData) => {
  tableData.value = [...newData]
}, { deep: true })

// 单元格自动填充配置
const cellAutofillOption = ref({
  directionX: true,  // 开启横向填充
  directionY: true,  // 开启纵向填充
  beforeAutofill: ({ direction, sourceSelectionData, targetSelectionData }: any) => {
    console.log('自动填充方向:', direction)
    console.log('源数据:', sourceSelectionData)
    console.log('目标数据:', targetSelectionData)
    return true  // 返回 true 允许填充
  },
  afterAutofill: () => {
    console.log('自动填充完成')
    // 触发数据变化事件
    emit('update:data', tableData.value)
  }
})

// 行样式配置（关闭高亮以获得更好的自动填充体验）
const rowStyleOption = ref({
  clickHighlight: false,  // 关闭点击高亮
  hoverHighlight: false   // 关闭悬停高亮
})

// ============ 排序功能 ============
const currentSort = ref<{ field: string; order: 'asc' | 'desc' | null }>({ field: '', order: null })

// 排序配置
const sortOption = computed(() => ({
  // 支持多列排序
  multipleSort: false,
  // 默认排序
  defaultSort: currentSort.value.field ? {
    key: currentSort.value.field,
    order: currentSort.value.order
  } : undefined
}))

// 处理排序变化
const handleSortChange = (params: any) => {
  const { field, order } = params
  currentSort.value = { field, order }
  emit('sort-change', { field, order })
}

// 转换列配置为 vue-fantable 格式
const tableColumns = computed(() => {
  const columns: any[] = []
  
  // 如果启用行选择，添加 checkbox/radio 列
  if (props.rowSelection?.enabled) {
    const selectionColumn: any = {
      field: '__selection',
      key: '__selection',
      title: '',
      width: 60,
      align: 'center',
      fixed: 'left',
      renderHeaderCell: () => {
        // 单选模式不显示全选框
        if (props.rowSelection?.type === 'radio') {
          return h('div', { style: 'text-align: center' }, '')
        }
        // 多选模式显示全选框
        if (props.rowSelection?.showSelectAll !== false) {
          return h('input', {
            type: 'checkbox',
            checked: isAllSelected.value,
            indeterminate: isIndeterminate.value,
            style: 'cursor: pointer',
            onChange: (e: any) => handleSelectAll(e.target.checked)
          })
        }
        return h('div', '')
      },
      renderBodyCell: ({ row }: any) => {
        const rowKey = row[props.rowKey!]
        const checked = isRowSelected(rowKey)
        const inputType = props.rowSelection?.type === 'radio' ? 'radio' : 'checkbox'
        return h('input', {
          type: inputType,
          name: props.rowSelection?.type === 'radio' ? '__radio_group' : undefined,
          checked,
          style: 'cursor: pointer',
          onChange: (e: any) => handleSelectRow(rowKey, e.target.checked)
        })
      }
    }
    columns.push(selectionColumn)
  }
  
  // 添加用户定义的列
  columns.push(...props.columns.map(col => {
    const column: any = {
      field: col.field,
      key: col.key || col.field,
      title: col.title,
      width: col.width || 120,
      align: col.align || 'left',
      fixed: col.fixed,
      // 支持排序
      sortBy: col.sortable ? col.field : undefined
    }

    // 根据 cellType 配置不同的渲染方式
    if (col.cellType) {
      // 自定义单元格渲染，传入 rowIndex 以支持点击激活编辑
      column.renderBodyCell = ({ row, rowIndex }: any) => {
        return renderEditableCell(row, col, rowIndex)
      }
    }

    // 自定义表头渲染
    if (col.renderHeaderCell) {
      column.renderHeaderCell = col.renderHeaderCell
    }

    return column
  }))

  return columns
})

// 渲染可编辑单元格（只有当 cell 被激活时显示编辑器）
const renderEditableCell = (row: any, col: any, rowIndex: number) => {
  const value = row[col.field]

  // 点击单元格激活编辑
  const onClickCell = (e: Event) => {
    e.stopPropagation()
    editingCell.value = { rowIndex, field: col.field }
  }

  // 如果当前单元格不是激活状态，显示普通文本并绑定点击
  if (!editingCell.value || editingCell.value.rowIndex !== rowIndex || editingCell.value.field !== col.field) {
    return h('div', { class: 'editable-cell', onClick: onClickCell }, String(value ?? ''))
  }

  // 激活状态下渲染对应的编辑器，并将编辑器样式调整为无边框、与单元格一致
  const editorCommonProps = {
    modelValue: value,
    placeholder: (col.editorConfig && col.editorConfig.placeholder) || '',
    style: { width: '100%', height: '100%', border: 'none', background: 'transparent' },
    'onUpdate:modelValue': (val: any) => {
      row[col.field] = val
      handleCellChange(row, col, val)
      // 不在此处关闭编辑，改为在 blur/change 时关闭以支持连续输入
    }
    ,onKeydown: (e: KeyboardEvent) => handleEditorKeydown(e, rowIndex, col)
  }
  // 先支持自定义编辑器组件：editorConfig.component 可以是组件定义或异步组件
  const customComp = col.editorConfig?.component
  if (customComp) {
    // 如果组件提供 options 数据作为插槽，可以把它作为 default slot
    const slots: any = {}
    if (col.editorConfig?.options) {
      slots.default = () => (col.editorConfig.options || []).map((opt: any) => h(ElOption, { key: opt.value, label: opt.label, value: opt.value }))
    }
    return h(customComp as any, { ...editorCommonProps, ...(col.editorConfig?.props || {}) }, slots)
  }

  // 否则使用内置编辑器
  switch (col.cellType) {
    case 'select':
      return h(ElSelect as any, { ...editorCommonProps, clearable: col.editorConfig?.clearable !== false, filterable: col.editorConfig?.filterable, onChange: () => { editingCell.value = null } }, { default: () => (col.editorConfig?.options || []).map((opt: any) => h(ElOption as any, { key: opt.value, label: opt.label, value: opt.value })) })
    case 'input':
      return h(ElInput as any, { ...editorCommonProps, onBlur: () => { editingCell.value = null } })
    case 'date':
      return h(ElDatePicker as any, { ...editorCommonProps, type: col.editorConfig?.type || 'date', format: col.editorConfig?.format, valueFormat: col.editorConfig?.valueFormat, onChange: () => { editingCell.value = null } })
    case 'time':
      return h(ElInput as any, { ...editorCommonProps, onBlur: () => { editingCell.value = null } })
    default:
      return h('div', { class: 'editable-cell', onClick: onClickCell }, String(value ?? ''))
  }
}

// （编辑器相关逻辑已整合到 renderEditableCell）

// 单元格样式配置
const cellStyleOption = computed(() => ({
  bodyCellClass: ({ row, column }: any) => {
    const classes: string[] = []
    
    // 可编辑单元格添加样式
    const col = props.columns.find(c => c.field === column.field)
    if (col?.cellType) {
      classes.push('editable-cell')
    }
    
    // 错误单元格样式
    if (row.errorFields?.includes(column.field)) {
      classes.push('error-cell')
    }
    
    return classes.join(' ')
  }
}))

// 编辑配置
const editOption = computed(() => ({
  // 双击编辑
  cellEditOption: {
    beforeCellValueChange: () => {
      return true
    }
  }
}))

// 处理单元格点击
const handleCellClick = (params: any) => {
  const { row, column, rowIndex } = params
  emit('cell-click', { row, column, rowIndex })
}

// 处理单元格值变化
const handleCellChange = (row: any, col: any, value: any) => {
  // 保存历史状态（用于撤销/重做）
  saveHistory()
  
  emit('cell-change', { row, column: col, value })
  emit('update:data', tableData.value)
}

// 暴露表格实例方法
defineExpose({
  tableRef,
  startEdit,
  cancelEdit,
  confirmEdit,
  getSelectedRows,
  clearSelection,
  selectedRowKeys,
  handleCopy,
  handlePaste,
  undo,
  redo,
  batchUpdateField,
  batchUpdateFields,
  handleSearch,
  clearSearch
})
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
  position: relative;
}

.business-table-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.business-table-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}

/* 加载遮罩 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 12px;
  color: #606266;
  font-size: 14px;
}

/* 可编辑单元格样式 */
:deep(.editable-cell) {
  cursor: pointer;
  transition: background-color 0.2s;
}

:deep(.editable-cell:hover) {
  background-color: #f5f7fa;
}

/* 错误单元格样式 */
:deep(.error-cell) {
  background-color: #fef0f0;
  border-color: #f56c6c;
}
</style>
