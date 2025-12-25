<template>
  <div class="student-course-select">
    <el-popover
      placement="bottom"
      :width="popoverWidth"
      trigger="click"
      :hide-after="0"
      transition="none"
      v-model:visible="visible"
    >
      <template #reference>
        <el-input
          :model-value="displayName"
          readonly
          :placeholder="''"
          :suffix-icon="ArrowDown"
          @click.prevent
        />
      </template>

      <div v-if="!props.studentId"  class="flex-between mb-10px" style="flex-direction: column;">
        <div class="title w100% m-b20px">“学员” 报读的1对1课程</div>
        <el-empty :description="transToConfigDescript('请先选“上课学员”')" :image-size="80"  :image="globalData.emptyPng2" class="p-0px!"  />
      </div>

      <div v-else>
        <div class="flex-between mb-10px">
          <div class="title">“{{ studentName || '学员' }}” 报读的一对一{{transToConfigDescript('课程')}}</div>
          <el-link type="primary" underline="never" @click="handleChooseOther">选择其他{{transToConfigDescript('课程')}}</el-link>
        </div>

        <el-table :data="courseList" style="width: 100%;" @row-click="onRowClick" :height="320" v-loading="loading">
          <el-table-column prop="ShiftName" :label="transToConfigDescript('课程名称')">
            <template #default="scope">
              {{ scope.row.ShiftName }}
              <span v-if="(scope.row.ConsumeAmount||1)>1" class="text-12px color-#909399">
                排1次，计{{scope.row.ConsumeAmount}}次
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="Unit" label="单位" width="60" />
          <el-table-column prop="BuyAmount" label="购买" width="80" />
          <el-table-column prop="RemainAmount" label="剩余" width="80" />
          <el-table-column prop="OutAmount" label="过期" width="80" />
          <el-table-column prop="Amount" label="已上/已排" width="110">
            <template #default="scope">
              <span v-if="scope.row.Amount>0 || scope.row.PlanAmount>0">{{scope.row.Amount+'/'+scope.row.PlanAmount}}</span>
            </template>
          </el-table-column>
          <el-table-column prop="MorePlan" label="多排" width="80">
            <template #default="scope">
              <span v-if="scope.row.Amount>0">{{ calcOverPlan(scope.row) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-popover>
    <chooseCourse ref="chooseCourseRef" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import chooseCourse from '@/components/popup/chooseCourse.vue'
import { getStudentBuyAndCourseAmount } from '@/api/arrange'
import { transToConfigDescript } from '@/utils/filters/filters'
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global

// 🚀 优化：组件挂载状态标记
const isMounted = ref(false)
const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  studentId: { type: [String, Number], default: '' },
  campusId: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '请选择' },
  disabled: { type: Boolean, default: false },
  initialData: { type: Object, default: null },
  studentName: { type: String, default: '' },
})
const emit = defineEmits([
  'update:modelValue', 
  'change', 
  'exitEdit',
  'dialogOpen',   // 🆕 UI交互锁：弹框打开
  'dialogClose',  // 🆕 UI交互锁：弹框关闭
  'visible-change' // 🆕 UI交互锁：popover 显示/隐藏
])

const visible = ref(false)
const loading = ref(false)
const courseList = ref([])
const selectedId = ref('')
const displayName = computed(() => {
  if (selectedId.value) {
    const item = courseList.value.find(it => (it.ID||it.ShiftId||it.ShiftID) === selectedId.value)
    if (item) return item.ShiftName || item.Name
  }
  return props.initialData?.Name || ''
})

const popoverWidth = computed(() => {
  return props.studentId ? 720 : 340
})

function calcOverPlan(row) {
  const valid = (row.BuyAmount || 0) - (row.OutAmount || 0)
  const plan = row.PlanAmount || 0
  const diff = plan - valid
  return diff > 0 ? diff : 0
}

function onRowClick(row) {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  const id = row.ID || row.ShiftId || row.ShiftID
  selectedId.value = id
  emit('update:modelValue', id)
  emit('change', id, {
    ID: id,
    Name: row.ShiftName || row.Name,
    ShiftName: row.ShiftName || row.Name,
    ...row
  })
  visible.value = false
  setTimeout(() => emit('exitEdit'), 0)
}

function handleChooseOther() {
  // 🚀 优化：组件销毁时不处理
  if (!isMounted.value) {
    return
  }

  if (!props.studentId) return
  
  // 🆕 UI交互锁：弹框打开时通知父组件
  emit('dialogOpen')
  
  // 仅打开通用课程选择
  chooseCourseRef.value?.open({ multi: false, required: true, condition: { shiftType: [1] } }).then((res)=>{
    const c = res?.data
    if (!c) return
    const id = c.ShiftId || c.ID
    selectedId.value = id
    // 注入到列表首部，便于显示
    if (!courseList.value.find(it => (it.ID||it.ShiftId||it.ShiftID) === id)) {
      courseList.value.unshift({
        ID: id,
        ShiftName: c.ShiftName || c.Name,
        Unit: c.Unit,
        BuyAmount: c.BuyAmount,
        RemainAmount: c.RemainAmount,
        PlanAmount: c.PlanAmount,
        ConsumeAmount: c.ConsumeAmount,
      })
    }
    emit('update:modelValue', id)
    emit('change', id, { ID: id, Name: c.ShiftName || c.Name, ShiftName: c.ShiftName || c.Name, ...c })
    visible.value = false
    setTimeout(() => emit('exitEdit'), 0)
    
    // 🆕 UI交互锁：弹框关闭时通知父组件
    emit('dialogClose')
    console.log('🔓 [StudentCourseSelect] emit dialogClose')
  }).catch(()=>{
    // 🆕 UI交互锁：即使取消也要释放锁
    emit('dialogClose')
    console.log('🔓 [StudentCourseSelect] emit dialogClose (cancelled)')
  })
}

const chooseCourseRef = ref()

async function fetchCourses() {
  if (!props.studentId) { courseList.value = []; return }
  loading.value = true
  try {
    const res = await getStudentBuyAndCourseAmount({ id: props.studentId, ShiftId: '', CampusID: props.campusId })
    const list = res?.Data?.ShiftAmountList || []
    courseList.value = list
  } catch (e) {
    courseList.value = []
  } finally {
    loading.value = false
  }
}

watch(() => props.studentId, () => { fetchCourses() }, { immediate: true })
watch(() => props.modelValue, (v) => { selectedId.value = v || '' }, { immediate: true })

// 🚀 优化：监听 popover 显示/隐藏，emit 给父组件用于 UI 交互锁
watch(visible, (newVisible) => {
  // 🚀 组件销毁时不处理
  if (!isMounted.value) {
    return
  }
  
  emit('visible-change', newVisible)
  console.log(`🔄 [StudentCourseSelect] visible-change: ${newVisible}`)
})

onMounted(() => {
  isMounted.value = true
  console.log('✅ StudentCourseSelect 组件已挂载')
  
  if (props.initialData?.ID && !props.modelValue) {
    selectedId.value = props.initialData.ID
  }
})

onUnmounted(() => {
  isMounted.value = false
  console.log('❌ StudentCourseSelect 组件销毁')
})
</script>

<style scoped>
.student-course-select { width: 100%; }
.title { font-size: 14px; color: #606266;font-weight: bold; }
</style> 