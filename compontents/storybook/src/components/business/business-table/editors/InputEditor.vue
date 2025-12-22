<template>
  <input
    v-if="config.type !== 'textarea'"
    v-model="localValue"
    :type="config.type || 'text'"
    :placeholder="config.placeholder"
    :maxlength="config.maxLength"
    class="input-editor"
    @input="handleInput"
    @blur="handleBlur"
  />
  <textarea
    v-else
    v-model="localValue"
    :placeholder="config.placeholder"
    :maxlength="config.maxLength"
    class="input-editor textarea-editor"
    @input="handleInput"
    @blur="handleBlur"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { InputEditorConfig } from '../types/column'

interface Props {
  modelValue: any
  config: InputEditorConfig
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

watch(() => props.modelValue, (newVal) => {
  localValue.value = newVal || ''
})

const handleInput = () => {
  emit('update:modelValue', localValue.value)
  emit('change', localValue.value)
}

const handleBlur = () => {
  emit('blur')
}
</script>

<style scoped>
.input-editor {
  width: 100%;
  height: 32px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.input-editor:focus {
  border-color: #409eff;
}

.textarea-editor {
  height: 60px;
  resize: vertical;
}
</style>
