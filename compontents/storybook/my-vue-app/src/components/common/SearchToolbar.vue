<!--
  表格搜索工具栏组件
  支持多列模糊搜索和精确筛选
-->
<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElInput, ElSelect, ElOption, ElButton } from 'element-plus'

interface Props {
  columns: any[] // 可搜索的列配置
  placeholder?: string
}

interface Emits {
  (e: 'search', data: { keyword: string; field: string }): void
  (e: 'clear'): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入关键词搜索...'
})

const emit = defineEmits<Emits>()

// 搜索数据
const searchKeyword = ref('')
const searchField = ref('all') // 搜索字段，'all' 表示全部列

// 可搜索的列选项
const searchableColumns = computed(() => {
  const columns = [
    { label: '全部列', value: 'all' }
  ]
  
  props.columns.forEach(col => {
    if (col.field !== 'index' && col.title) {
      columns.push({
        label: col.title,
        value: col.field
      })
    }
  })
  
  return columns
})

// 执行搜索
const handleSearch = () => {
  emit('search', {
    keyword: searchKeyword.value.trim(),
    field: searchField.value
  })
}

// 清空搜索
const handleClear = () => {
  searchKeyword.value = ''
  searchField.value = 'all'
  emit('clear')
}

// 回车搜索
const handleKeydown = (e: Event) => {
  const keyEvent = e as KeyboardEvent
  if (keyEvent.key === 'Enter') {
    handleSearch()
  }
}
</script>

<template>
  <div class="search-toolbar">
    <div class="search-input-wrapper">
      <ElSelect
        v-model="searchField"
        placeholder="选择搜索列"
        style="width: 150px; margin-right: 10px"
      >
        <ElOption
          v-for="col in searchableColumns"
          :key="col.value"
          :label="col.label"
          :value="col.value"
        />
      </ElSelect>
      
      <ElInput
        v-model="searchKeyword"
        :placeholder="placeholder"
        clearable
        style="width: 300px; margin-right: 10px"
        @keydown="handleKeydown"
        @clear="handleClear"
      >
        <template #prefix>
          <span class="search-icon">🔍</span>
        </template>
      </ElInput>
      
      <ElButton type="primary" @click="handleSearch">搜索</ElButton>
      <ElButton @click="handleClear">重置</ElButton>
    </div>
    
    <div v-if="searchKeyword" class="search-tip">
      搜索: "{{ searchKeyword }}" 
      <span v-if="searchField !== 'all'">
        (在 {{ searchableColumns.find(c => c.value === searchField)?.label }})
      </span>
    </div>
  </div>
</template>

<style scoped>
.search-toolbar {
  margin-bottom: 12px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
}

.search-icon {
  font-size: 14px;
}

.search-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
  color: #0050b3;
  font-size: 13px;
  border-radius: 2px;
}
</style>
