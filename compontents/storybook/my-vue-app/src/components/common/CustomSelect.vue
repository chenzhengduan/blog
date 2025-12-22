<template>
  <el-select v-model="modelValueLocal" :clearable="clearable" filterable style="width:100%">
    <slot />
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElSelect } from 'element-plus'

const props = defineProps({
  modelValue: null,
  clearable: { type: Boolean, default: true }
})
const emit = defineEmits(['update:modelValue'])

const modelValueLocal = ref(props.modelValue)

watch(() => props.modelValue, (v) => modelValueLocal.value = v)
watch(modelValueLocal, (v) => emit('update:modelValue', v))
</script>

<style scoped>
.el-select {
  border: none;
  background: transparent;
}
</style>
