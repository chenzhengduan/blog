<template>
  <input
    v-model="localValue"
    :type="inputType"
    :placeholder="config.placeholder"
    class="date-editor"
    @change="handleChange"
    @blur="handleBlur"
  />
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { DateEditorConfig } from '../types/column'

interface Props {
  modelValue: any
  config: DateEditorConfig
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

const localValue = ref(props.modelValue || '')

const inputType = computed(() => {
  switch (props.config.type) {
    case 'datetime':
      return 'datetime-local'
    case 'daterange':
      return 'date' // 简化处理
    default:
      return 'date'
  }
})

watch(() => props.modelValue, (newVal) => {
  localValue.value = newVal || ''
})

const handleChange = () => {
  emit('update:modelValue', localValue.value)
  emit('change', localValue.value)
}

const handleBlur = () => {
  emit('blur')
}
</script>

<style scoped>
.date-editor {
  width: 100%;
  height: 32px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.date-editor:focus {
  border-color: #409eff;
}
</style>
