<template>
  <el-popover
    v-model:visible="popoverVisible"
    placement="bottom-start"
    :width="360"
    trigger="click"
    popper-class="campus-multi-select-popover"
  >
    <template #reference>
      <div class="campus-select-trigger" :class="{ 'has-selection': selectedCampuses.length > 0 }">
        <span class="trigger-text">{{ displayText }}</span>
        <el-icon class="trigger-icon" :class="{ 'is-reverse': popoverVisible }">
          <ArrowDown />
        </el-icon>
      </div>
    </template>

    <div class="campus-select-panel">
      <!-- 搜索框 -->
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          :placeholder="transToConfigDescript('搜索校区')"
          clearable
          size="small"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 全选 -->
      <div class="select-all">
        <el-checkbox
          v-model="isAllSelected"
          :indeterminate="isIndeterminate"
          @change="handleSelectAll"
        >
          全选
        </el-checkbox>
      </div>

      <!-- 校区列表 -->
      <div class="campus-list">
        <el-checkbox-group v-model="selectedCampusIds" class="campus-grid">
          <el-checkbox
            v-for="campus in filteredCampusList"
            :key="campus.ID"
            :label="campus.ID"
            @change="handleCampusChange"
          >
            {{ campus.Name }}
          </el-checkbox>
        </el-checkbox-group>
        <div v-if="filteredCampusList.length === 0" class="empty-state">
          暂无匹配的校区
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="footer-actions">
        <el-button size="small" @click="handleCancel">取消</el-button>
        <el-button size="small" type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ArrowDown, Search } from '@element-plus/icons-vue'
import { useUserCampuses } from '@/store'
import { transToConfigDescript } from '@/utils/filters/filters'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const userCampusesStore = useUserCampuses()
const campusList = computed(() => userCampusesStore.userCampuses || [])

const popoverVisible = ref(false)
const searchKeyword = ref('')
const selectedCampusIds = ref([...props.modelValue])
const tempSelectedIds = ref([...props.modelValue]) // 临时存储，用于取消时恢复

// 过滤后的校区列表
const filteredCampusList = computed(() => {
  if (!searchKeyword.value) return campusList.value
  const keyword = searchKeyword.value.toLowerCase()
  return campusList.value.filter(campus => 
    campus.Name?.toLowerCase().includes(keyword)
  )
})

// 已选择的校区对象数组
const selectedCampuses = computed(() => {
  return campusList.value.filter(campus => 
    selectedCampusIds.value.includes(campus.ID)
  )
})

// 显示文本
const displayText = computed(() => {
  const count = selectedCampuses.value.length
  if (count === 0) {
    return transToConfigDescript('请选择校区')
  } else if (count === 1) {
    return selectedCampuses.value[0].Name
  } else {
    return transToConfigDescript(`已选择${count}个校区`)
  }
})

// 是否全选
const isAllSelected = computed({
  get() {
    return filteredCampusList.value.length > 0 && 
           filteredCampusList.value.every(campus => 
             selectedCampusIds.value.includes(campus.ID)
           )
  },
  set(val) {
    // setter 用于 v-model
  }
})

// 是否部分选中
const isIndeterminate = computed(() => {
  const selectedCount = filteredCampusList.value.filter(campus => 
    selectedCampusIds.value.includes(campus.ID)
  ).length
  return selectedCount > 0 && selectedCount < filteredCampusList.value.length
})

// 处理全选
const handleSelectAll = (checked) => {
  if (checked) {
    // 全选：将当前过滤列表中的所有校区ID添加到选中列表
    const newIds = filteredCampusList.value.map(campus => campus.ID)
    selectedCampusIds.value = [...new Set([...selectedCampusIds.value, ...newIds])]
  } else {
    // 取消全选：移除当前过滤列表中的所有校区ID
    const filterIds = new Set(filteredCampusList.value.map(campus => campus.ID))
    selectedCampusIds.value = selectedCampusIds.value.filter(id => !filterIds.has(id))
  }
}

// 处理单个校区选择变化
const handleCampusChange = () => {
  // checkbox 的 v-model 会自动更新 selectedCampusIds
}

// 取消
const handleCancel = () => {
  selectedCampusIds.value = [...tempSelectedIds.value]
  popoverVisible.value = false
  searchKeyword.value = ''
}

// 确定
const handleConfirm = () => {
  tempSelectedIds.value = [...selectedCampusIds.value]
  emit('update:modelValue', selectedCampusIds.value)
  emit('change', selectedCampuses.value)
  popoverVisible.value = false
  searchKeyword.value = ''
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedCampusIds.value = [...newVal]
  tempSelectedIds.value = [...newVal]
}, { deep: true })

// 监听弹窗打开，保存临时状态
watch(popoverVisible, (newVal) => {
  if (newVal) {
    tempSelectedIds.value = [...selectedCampusIds.value]
  } else {
    searchKeyword.value = ''
  }
})
</script>

<style scoped>
.campus-select-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #F3F4F4;
  border-radius: 4px;
  cursor: pointer;
  user-select: none;
  min-width: 120px;
  height: 32px;

  &.has-selection {
    background: #E6F4FF;
    color: #2878E8;
  }

  .trigger-text {
    font-size: 14px;
    color: #606266;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &.has-selection .trigger-text {
    color: #2878E8;
    font-weight: 500;
  }

  .trigger-icon {
    font-size: 12px;
    color: #909399;
    flex-shrink: 0;

    &.is-reverse {
      transform: rotate(180deg);
    }
  }

  &.has-selection .trigger-icon {
    color: #2878E8;
  }
}

.campus-select-panel {
  display: flex;
  flex-direction: column;
  max-height: 600px;

  .search-container {
    width: 100%;
    padding: 8px;
  }

  .search-input {
    width: 100%;
    height: 32px;
    border-radius: 4px;
  }

  .select-all {
    padding: 0px 12px;
    border-bottom: 1px solid #F0F0F0;

  }

  .campus-list {
    flex: 1;
    overflow-y: auto;
    padding:0px 12px;
    max-height: 480px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f1f1f1;
    }

    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
    }

    .campus-grid {
      display: flex;
      flex-direction: column;
      gap: 0;
    }

    :deep(.wtwo-checkbox) {
      display: flex;
      align-items: center;
      width: 100%;
      margin: 0;
      height: 40px;
      border-radius: 4px;

      .wtwo-checkbox__input {
        flex-shrink: 0;

        &.is-checked {
          .wtwo-checkbox__inner {
            background-color: #2878E8;
            border-color: #2878E8;
          }
        }

        &:hover .wtwo-checkbox__inner {
          border-color: #2878E8;
        }
      }

      .wtwo-checkbox__label {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-left: 8px;
        font-size: 14px;
        line-height: normal;
        color: #303133;
      }

      &:hover .wtwo-checkbox__label {
        color: #2878E8;
      }
    }

    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: #909399;
      font-size: 14px;
    }
  }

  .footer-actions {
    display: flex;
    justify-content: flex-end;
    padding: 12px;
    border-top: 1px solid #F0F0F0;
  }
}
</style>

<style>
.campus-multi-select-popover {
  padding: 0 !important;
}
</style>
