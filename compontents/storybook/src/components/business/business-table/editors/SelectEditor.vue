<template>
  <select
    v-model="localValue"
    :class="['select-editor', { 'is-multiple': config.multiple }]"
    :multiple="config.multiple"
    @change="handleChange"
    @blur="handleBlur"
  >
    <option v-if="config.placeholder && !localValue" value="" disabled>
      {{ config.placeholder }}
    </option>
    <option
      v-for="option in config.options"
      :key="option.value"
      :value="option.value"
    >
      {{ option.label }}
    </option>
  </select>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import type { SelectEditorConfig } from '../types/column'

interface Props {
  modelValue: any
  config: SelectEditorConfig
  rowData?: any
  field?: string
}

interface Emits {
  (e: 'update:modelValue', value: any): void
  (e: 'change', value: any): void
  (e: 'blur'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const localValue = ref(props.modelValue)

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  localValue.value = newVal
})

// 处理值变化
const handleChange = () => {
  emit('update:modelValue', localValue.value)
  emit('change', localValue.value)
}

// 处理失焦
const handleBlur = () => {
  emit('blur')
}

// 自动聚焦
onMounted(() => {
  // 可选：自动聚焦到编辑器
})
</script>

<style scoped>
.select-editor {
  width: 100%;
  height: 32px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.select-editor:focus {
  border-color: #409eff;
}

.select-editor.is-multiple {
  height: auto;
  min-height: 32px;
}

.select-editor option {
  padding: 8px;
}
</style>
