<template>
  <div class="component-builder">
    <div class="builder-header">
      <h2>动态组件配置器</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加组件
      </el-button>
    </div>

    <!-- 组件列表 -->
    <div class="component-list">
      <el-empty v-if="components.length === 0" description="暂无组件，点击上方按钮添加" />
      
      <div v-else class="component-items">
        <div 
          v-for="(comp, index) in components" 
          :key="comp.id"
          class="component-item"
        >
          <div class="component-label">
            {{ comp.label }}
          </div>
          <div class="component-control">
            <!-- 动态渲染不同类型的组件 -->
            <component 
              :is="getComponentType(comp.type)"
              v-model="comp.value"
              v-bind="getComponentProps(comp)"
              :placeholder="comp.placeholder || `请${getPlaceholderPrefix(comp.type)}${comp.label}`"
              @change="handleComponentChange(comp)"
            >
              <!-- 下拉选择的选项 -->
              <template v-if="comp.type === 'select'">
                <el-option
                  v-for="option in comp.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </template>
            </component>
          </div>
          <div class="component-actions">
            <el-button 
              text 
              type="primary" 
              @click="editComponent(comp, index)"
            >
              编辑
            </el-button>
            <el-button 
              text 
              type="danger" 
              @click="deleteComponent(index)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览数据 -->
    <div class="data-preview">
      <h3>组件数据预览</h3>
      <el-button size="small" @click="exportData">导出配置</el-button>
      <el-button size="small" @click="exportValues">导出值</el-button>
      <pre>{{ JSON.stringify(getComponentValues(), null, 2) }}</pre>
    </div>

    <!-- 添加/编辑组件对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑组件' : '添加组件'"
      width="600px"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="组件类型" required>
          <el-select 
            v-model="formData.type" 
            placeholder="请选择组件类型"
            @change="handleTypeChange"
          >
            <el-option label="输入框" value="input" />
            <el-option label="数字输入框" value="input-number" />
            <el-option label="下拉选择" value="select" />
            <el-option label="日期选择" value="date" />
            <el-option label="日期时间" value="datetime" />
            <el-option label="时间选择" value="time" />
            <el-option label="开关" value="switch" />
            <el-option label="滑块" value="slider" />
            <el-option label="文本域" value="textarea" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签名称" required>
          <el-input 
            v-model="formData.label" 
            placeholder="请输入标签名称"
          />
        </el-form-item>

        <el-form-item label="字段名" required>
          <el-input 
            v-model="formData.field" 
            placeholder="请输入字段名（英文）"
          />
        </el-form-item>

        <el-form-item label="占位符">
          <el-input 
            v-model="formData.placeholder" 
            placeholder="请输入占位符文本"
          />
        </el-form-item>

        <el-form-item label="默认值">
          <el-input 
            v-model="formData.defaultValue" 
            placeholder="请输入默认值"
          />
        </el-form-item>

        <!-- 下拉选择特有配置 -->
        <template v-if="formData.type === 'select'">
          <el-form-item label="下拉选项" required>
            <div class="options-config">
              <div 
                v-for="(option, index) in formData.options" 
                :key="index"
                class="option-item"
              >
                <el-input 
                  v-model="option.label" 
                  placeholder="选项名称"
                  size="small"
                />
                <el-input 
                  v-model="option.value" 
                  placeholder="选项值"
                  size="small"
                />
                <el-button 
                  text 
                  type="danger" 
                  size="small"
                  @click="removeOption(index)"
                >
                  删除
                </el-button>
              </div>
              <el-button 
                size="small" 
                @click="addOption"
              >
                添加选项
              </el-button>
            </div>
          </el-form-item>
        </template>

        <!-- 数字输入框特有配置 -->
        <template v-if="formData.type === 'input-number'">
          <el-form-item label="最小值">
            <el-input-number 
              v-model="formData.min" 
              :controls="false"
            />
          </el-form-item>
          <el-form-item label="最大值">
            <el-input-number 
              v-model="formData.max" 
              :controls="false"
            />
          </el-form-item>
          <el-form-item label="步长">
            <el-input-number 
              v-model="formData.step" 
              :controls="false"
            />
          </el-form-item>
        </template>

        <!-- 滑块特有配置 -->
        <template v-if="formData.type === 'slider'">
          <el-form-item label="最小值">
            <el-input-number 
              v-model="formData.min" 
              :controls="false"
            />
          </el-form-item>
          <el-form-item label="最大值">
            <el-input-number 
              v-model="formData.max" 
              :controls="false"
            />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

interface ComponentOption {
  label: string
  value: string | number
}

interface ComponentConfig {
  id: string
  type: string
  label: string
  field: string
  placeholder?: string
  value: any
  defaultValue?: any
  options?: ComponentOption[]
  min?: number
  max?: number
  step?: number
}

interface FormData {
  type: string
  label: string
  field: string
  placeholder: string
  defaultValue: string
  options: ComponentOption[]
  min?: number
  max?: number
  step?: number
}

// 组件列表
const components = ref<ComponentConfig[]>([])

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const editIndex = ref(-1)

// 表单数据
const formData = ref<FormData>({
  type: 'input',
  label: '',
  field: '',
  placeholder: '',
  defaultValue: '',
  options: [],
  min: 0,
  max: 100,
  step: 1
})

// 根据组件类型获取对应的 Element Plus 组件
const getComponentType = (type: string) => {
  const typeMap: Record<string, string> = {
    'input': 'el-input',
    'input-number': 'el-input-number',
    'select': 'el-select',
    'date': 'el-date-picker',
    'datetime': 'el-date-picker',
    'time': 'el-time-picker',
    'switch': 'el-switch',
    'slider': 'el-slider',
    'textarea': 'el-input'
  }
  return typeMap[type] || 'el-input'
}

// 获取组件属性
const getComponentProps = (comp: ComponentConfig) => {
  const baseProps: any = {}
  
  if (comp.type === 'datetime') {
    baseProps.type = 'datetime'
  }
  
  if (comp.type === 'textarea') {
    baseProps.type = 'textarea'
    baseProps.rows = 3
  }
  
  if (comp.type === 'input-number') {
    baseProps.min = comp.min
    baseProps.max = comp.max
    baseProps.step = comp.step
    baseProps.controls = true
  }
  
  if (comp.type === 'slider') {
    baseProps.min = comp.min || 0
    baseProps.max = comp.max || 100
  }
  
  return baseProps
}

// 获取占位符前缀
const getPlaceholderPrefix = (type: string) => {
  const prefixMap: Record<string, string> = {
    'input': '输入',
    'input-number': '输入',
    'select': '选择',
    'date': '选择',
    'datetime': '选择',
    'time': '选择',
    'textarea': '输入'
  }
  return prefixMap[type] || '输入'
}

// 打开添加对话框
const openAddDialog = () => {
  console.log('openAddDialog called')
  isEdit.value = false
  editIndex.value = -1
  resetForm()
  dialogVisible.value = true
  console.log('dialogVisible:', dialogVisible.value)
}

// 编辑组件
const editComponent = (comp: ComponentConfig, index: number) => {
  isEdit.value = true
  editIndex.value = index
  
  formData.value = {
    type: comp.type,
    label: comp.label,
    field: comp.field,
    placeholder: comp.placeholder || '',
    defaultValue: comp.defaultValue || '',
    options: comp.options ? [...comp.options] : [],
    min: comp.min,
    max: comp.max,
    step: comp.step
  }
  
  dialogVisible.value = true
}

// 删除组件
const deleteComponent = (index: number) => {
  components.value.splice(index, 1)
  ElMessage.success('删除成功')
}

// 重置表单
const resetForm = () => {
  formData.value = {
    type: 'input',
    label: '',
    field: '',
    placeholder: '',
    defaultValue: '',
    options: [],
    min: 0,
    max: 100,
    step: 1
  }
}

// 类型改变时重置相关配置
const handleTypeChange = (type: string) => {
  if (type === 'select' && formData.value.options.length === 0) {
    formData.value.options = [
      { label: '选项1', value: '1' },
      { label: '选项2', value: '2' }
    ]
  }
}

// 添加选项
const addOption = () => {
  formData.value.options.push({
    label: '',
    value: ''
  })
}

// 删除选项
const removeOption = (index: number) => {
  formData.value.options.splice(index, 1)
}

// 确认添加
const confirmAdd = () => {
  if (!formData.value.label) {
    ElMessage.warning('请输入标签名称')
    return
  }
  if (!formData.value.field) {
    ElMessage.warning('请输入字段名')
    return
  }
  
  if (formData.value.type === 'select' && formData.value.options.length === 0) {
    ElMessage.warning('下拉选择至少需要一个选项')
    return
  }
  
  const componentId = isEdit.value && editIndex.value >= 0 
    ? components.value[editIndex.value]?.id || `comp_${Date.now()}`
    : `comp_${Date.now()}`
  
  const newComponent: ComponentConfig = {
    id: componentId,
    type: formData.value.type,
    label: formData.value.label,
    field: formData.value.field,
    placeholder: formData.value.placeholder,
    value: getInitialValue(formData.value.type, formData.value.defaultValue),
    defaultValue: formData.value.defaultValue,
    options: formData.value.type === 'select' ? [...formData.value.options] : undefined,
    min: formData.value.min,
    max: formData.value.max,
    step: formData.value.step
  }
  
  if (isEdit.value) {
    components.value[editIndex.value] = newComponent
    ElMessage.success('编辑成功')
  } else {
    components.value.push(newComponent)
    ElMessage.success('添加成功')
  }
  
  dialogVisible.value = false
}

// 获取初始值
const getInitialValue = (type: string, defaultValue: any) => {
  if (defaultValue !== undefined && defaultValue !== '') {
    return defaultValue
  }
  
  const initialValueMap: Record<string, any> = {
    'input': '',
    'input-number': 0,
    'select': '',
    'date': '',
    'datetime': '',
    'time': '',
    'switch': false,
    'slider': 0,
    'textarea': ''
  }
  
  return initialValueMap[type]
}

// 组件值改变
const handleComponentChange = (comp: ComponentConfig) => {
  console.log(`组件 ${comp.label} 值改变:`, comp.value)
}

// 获取所有组件的值
const getComponentValues = () => {
  const values: Record<string, any> = {}
  components.value.forEach(comp => {
    values[comp.field] = comp.value
  })
  return values
}

// 导出配置
const exportData = () => {
  const config = components.value.map(comp => ({
    type: comp.type,
    label: comp.label,
    field: comp.field,
    placeholder: comp.placeholder,
    defaultValue: comp.defaultValue,
    options: comp.options,
    min: comp.min,
    max: comp.max,
    step: comp.step
  }))
  
  const dataStr = JSON.stringify(config, null, 2)
  navigator.clipboard.writeText(dataStr)
  ElMessage.success('配置已复制到剪贴板')
}

// 导出值
const exportValues = () => {
  const values = getComponentValues()
  const dataStr = JSON.stringify(values, null, 2)
  navigator.clipboard.writeText(dataStr)
  ElMessage.success('值已复制到剪贴板')
}
</script>

<style scoped lang="scss">
.component-builder {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.builder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;

  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
  }
}

.component-list {
  margin-bottom: 32px;
}

.component-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.component-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  gap: 16px;
  transition: all 0.3s;

  &:hover {
    background: #f0f0f0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.component-label {
  flex-shrink: 0;
  width: 120px;
  font-weight: 500;
  color: #333;
}

.component-control {
  flex: 1;
  min-width: 0;
}

.component-actions {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
}

.data-preview {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;

  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    font-weight: 600;
  }

  pre {
    margin: 12px 0 0 0;
    padding: 12px;
    background: #fff;
    border-radius: 4px;
    overflow-x: auto;
    font-size: 12px;
    line-height: 1.6;
  }
}

.options-config {
  width: 100%;
}

.option-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;

  .el-input {
    flex: 1;
  }
}
</style>
