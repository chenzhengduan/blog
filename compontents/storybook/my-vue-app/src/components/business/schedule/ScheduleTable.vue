<!--
  排课表格业务组件
  职责：封装排课业务逻辑、数据管理、操作按钮
  使用基础 BaseTable 组件
-->
<script setup lang="ts">
import { ref } from 'vue'
import BaseTable from '../../base/table/BaseTable.vue'
// 示例自定义组件（业务改造过的下拉）
import CustomSelect from '../../common/CustomSelect.vue'
import { scheduleTableColumns, mockTableData } from './schedule-config'

// 表格配置
// 将示例组件注入到部分列的 editorConfig.component（运行时通过引用生效）
scheduleTableColumns.forEach(col => {
  if (col.field === 'CampusID' || col.field === 'ClassID') {
    // 使用 any 避免类型冲突，仅示例注入自定义组件
    const cfg: any = Object.assign({}, col.editorConfig || {})
    cfg.component = CustomSelect
    col.editorConfig = cfg
  }
})

const tableData = ref([...mockTableData])
const loading = ref(false)
const baseTableRef = ref()

// 行选择配置
const rowSelection = ref({
  enabled: true,
  type: 'checkbox' as const,
  showSelectAll: true
})

// 处理行选择变化
const handleSelectionChange = (data: any) => {
  console.log('选中的行：', data)
}

// 处理排序变化
const handleSortChange = (data: any) => {
  console.log('排序变化：', data)
  // TODO: 这里可以调用后端API进行排序
}

// 处理单元格点击
const handleCellClick = (data: any) => {
  console.log('单元格点击：', data)
}

// 处理单元格变化
const handleCellChange = (data: any) => {
  console.log('单元格变化：', data)
  // TODO: 可以在这里添加业务验证逻辑
  // 例如：校验课程时间冲突、教室占用情况等
}

// 新增排课
const addRow = () => {
  const newRow = {
    id: String(Date.now()),
    CampusID: '',
    ClassID: '',
    StudentUserID: '',
    ShiftID: '',
    SubjectID: '',
    Date: '',
    timeRange: '',
    ClassRoomID: '',
    MainTeacherID: '',
    AssistantTeacherID: [],
    CourseType: '1',
    IsSubscribeCourse: '0',
    MaxStudentCount: '',
    StartStudentCount: '',
    InternalRemark: ''
  }
  tableData.value.push(newRow)
  console.log('新增排课：', newRow)
}

// 删除选中行
const deleteSelected = () => {
  const selectedRows = baseTableRef.value?.getSelectedRows()
  if (!selectedRows || selectedRows.length === 0) {
    alert('请先选择要删除的行')
    return
  }
  
  if (confirm(`确定要删除选中的 ${selectedRows.length} 条记录吗？`)) {
    const selectedIds = selectedRows.map((row: any) => row.id)
    tableData.value = tableData.value.filter(row => !selectedIds.includes(row.id))
    baseTableRef.value?.clearSelection()
    alert('删除成功')
  }
}

// 保存排课数据
const saveData = async () => {
  console.log('保存排课数据：', tableData.value)
  loading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    loading.value = false
    alert('数据已保存（查看控制台）')
  }, 1000)
}

// 自动填充（仅支持往下填充）
const autoFillDown = () => {
  // 仅支持选中一行后，将该行内容往下填充到所有空行
  // 这里假设选中行为第一个非空行（可根据实际选中逻辑调整）
  const selectedIndex = tableData.value.findIndex(row => {
    // 选中条件：有内容的行（可自定义）
    return row.CampusID || row.ClassID || row.ShiftID || row.SubjectID || row.Date
  })
  
  if (selectedIndex === -1) {
    alert('请先选中要填充的行（有内容的行）')
    return
  }
  
  const sourceRow = tableData.value[selectedIndex]
  if (!sourceRow) return
  
  // 往下填充到所有空行
  for (let i = selectedIndex + 1; i < tableData.value.length; i++) {
    const targetRow = tableData.value[i]
    if (!targetRow) continue
    
    // 仅填充空行
    const isEmpty = !(targetRow.CampusID || targetRow.ClassID || targetRow.ShiftID || targetRow.SubjectID || targetRow.Date)
    if (isEmpty) {
      // 使用类型断言处理动态属性赋值
      const source = sourceRow as Record<string, any>
      const target = targetRow as Record<string, any>
      
      Object.keys(source).forEach(key => {
        // 不覆盖 id、InternalRemark、AssistantTeacherID
        if (key !== 'id' && key !== 'InternalRemark' && key !== 'AssistantTeacherID') {
          target[key] = source[key]
        }
      })
    }
  }
  
  alert('自动填充完成！')
}

// 导出数据（供父组件访问）
defineExpose({
  tableData,
  addRow,
  deleteSelected,
  saveData,
  autoFillDown
})
</script>

<template>
  <div class="schedule-table">
    <BaseTable
      ref="baseTableRef"
      :columns="scheduleTableColumns"
      :data="tableData"
      :loading="loading"
      :row-selection="rowSelection"
      row-key="id"
      :max-height="600"
      @cell-click="handleCellClick"
      @cell-change="handleCellChange"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <template #toolbar>
        <div class="toolbar">
          <h3>排课表格</h3>
          
          <div class="toolbar-actions">
            <button @click="addRow" class="btn-primary">新增排课</button>
            <button @click="deleteSelected" class="btn-danger">删除选中</button>
            <button @click="saveData" class="btn-success">保存</button>
            <button @click="autoFillDown" class="btn-fill">自动填充</button>
          </div>
        </div>
      </template>
    </BaseTable>
  </div>
</template>

<style scoped>
.schedule-table {
  width: 100%;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.toolbar h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-actions button {
  padding: 8px 16px;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.toolbar-actions button:hover {
  opacity: 0.85;
  transform: translateY(-1px);
}

.toolbar-actions button:active {
  transform: translateY(0);
}

.btn-primary {
  background: #409eff;
}

.btn-primary:hover {
  background: #66b1ff;
}

.btn-danger {
  background: #f56c6c;
}

.btn-danger:hover {
  background: #f78989;
}

.btn-success {
  background: #67c23a;
}

.btn-success:hover {
  background: #85ce61;
}

.btn-fill {
  background: #e6a23c;
}

.btn-fill:hover {
  background: #f3d19e;
}

.btn-warning {
  background: #e6a23c;
}

.btn-warning:hover {
  background: #ebb563;
}

.btn-secondary {
  background: #909399;
}

.btn-secondary:hover {
  background: #a6a9ad;
}
</style>
