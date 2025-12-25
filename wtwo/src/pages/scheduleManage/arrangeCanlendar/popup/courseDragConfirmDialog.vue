<template>
  <div>
    <el-dialog v-model="dialogVisible" :show-close="true" width="640px" :close-on-click-modal="false"
      :close-on-press-escape="false" @close="handleClose" class="course-adjust-dialog" title="调整提示">
      <div v-if="confirmInfo" class="dialog-content">
        <div class="course-info">
          <div class="course-title">{{ confirmInfo.course.ClassName }}</div>
          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">{{transToConfigDescript('上课校区：')}}</span>
              <span class="info-value" :title="confirmInfo.course.CampusName">{{ confirmInfo.course.CampusName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">{{transToConfigDescript('上课课程：')}}</span>
              <span class="info-value" :title="confirmInfo.course.ShiftName">{{ confirmInfo.course.ShiftName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">{{transToConfigDescript('上课老师：')}}</span>
              <span class="info-value" :title="confirmInfo.course.TeacherName">{{ confirmInfo.course.TeacherName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">{{transToConfigDescript('上课教室：')}}</span>
              <span class="info-value" :title="confirmInfo.course.ClassroomName">{{ confirmInfo.course.ClassroomName || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="time-divider">本次拖动，将要调整以下信息</div>
        <div class="time-adjust">
          <!-- 调整前 -->
          <div class="time-section original">
            <div class="time-tag ">调整前</div>
            <div class="time-header"></div>
            <div class="time-row">
              <span class="time-label">{{transToConfigDescript('上课日期：')}}</span>
              <span class="time-value">{{ confirmInfo.course.Date }}({{ confirmInfo.originalPosition.dayName }})</span>
            </div>
            <div class="time-row">
              <span class="time-label">{{transToConfigDescript('上课时间：')}}</span>
              <span class="time-value">{{ confirmInfo.originalPosition.StartTime }} - {{
                confirmInfo.originalPosition.EndTime }}</span>
            </div>
          </div>
          <div class="arrow-icon">
            <el-icon>
              <svg aria-hidden="true">
                <use xlink:href="#w2-a-Arrow-circle-rightyou-jiantou"></use>
              </svg>
            </el-icon>
          </div>
          <!-- 调整后 -->
          <div class="time-section new">
            <div class="time-tag">调整后</div>
            <div class="time-header"></div>
            <div class="time-row">
              <span class="time-label" :class="{'value-changed': isDateChanged}">{{transToConfigDescript('上课日期：')}}</span>
              <el-date-picker :clearable="false" v-model="confirmInfo.newPosition.Date" type="date" size="small"
                :placeholder="confirmInfo.newPosition.Date" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
                :class="['custom-date-picker', { 'value-changed': isDateChanged }]" @change="handleDateChange" />
              <span :class="['day-name', { 'value-changed-name': isDateChanged }]">({{ getNewDayName }})</span>
              <!-- <el-icon class="edit-icon">
                <Edit />
              </el-icon> -->
            </div>
            <div class="time-row">
              <span class="time-label" :class="{'value-changed': isTimeChanged}">{{transToConfigDescript('上课时间：')}}</span>
              <div class="time-range-container">
                <el-time-picker :clearable="false" v-model="confirmInfo.newPosition.StartTime" size="small"
                  :placeholder="confirmInfo.newPosition.StartTime" format="HH:mm" value-format="HH:mm"
                  :class="['custom-time-picker', { 'value-changed': isTimeChanged }]" 
                  @change="handleStartTimeChange" />
                <span class="time-separator">-</span>
                <el-time-picker :clearable="false" v-model="confirmInfo.newPosition.EndTime" size="small"
                  :placeholder="confirmInfo.newPosition.EndTime" format="HH:mm" value-format="HH:mm"
                  :class="['custom-time-picker', { 'value-changed': isTimeChanged }]" 
                  @change="handleEndTimeChange" />
              </div>
              <!-- <el-icon class="edit-icon">
                <Edit />
              </el-icon> -->
            </div>
          </div>
        </div>

        <!-- 调整类型选择 -->
        <!-- <div class="adjust-types">
          <div>调整类型</div>
          <div class="type-item">
            <el-radio v-model="adjustType" label="2">移动排课</el-radio>
          </div>
          <div class="type-item">
            <el-radio v-model="adjustType" label="1">复制排课</el-radio>
          </div>
        </div> -->

        <!-- 提示信息 -->
        <!-- <div class="warning-message">
        <el-icon>
          <Warning />
        </el-icon>
        <span>点击"确认调整"将保存新的课程时间，点击"取消"将恢复到原来的位置</span>
      </div> -->
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-checkbox :disabled="!NewCourse_IngoreCourseConflict" v-model="checkConflict">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
          <div><el-button @click="handleCancel" :disabled="loading">放弃调整</el-button>
            <el-button type="primary" @click="handleConfirm" :loading="loading">确认调整</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
    <ConflictPrompt ref="conflictPromptRef" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Edit, Warning } from '@element-plus/icons-vue'
import { copyOrMoveCourse } from '@/api/arrange';
import ConflictPrompt from '../../popup/conflictPrompt.vue';
import { transToConfigDescript } from '@/utils/filters/filters';

//权限
const NewCourse_IngoreCourseConflict=window.$xgj.op('NewCourse_IngoreCourseConflict')

// 新增响应式变量
const adjustType = ref('2')
const checkConflict = ref(true)

const dialogVisible = ref(false)
const loading = ref(false)
const confirmInfo = ref(null)

let _resolve = null
let _reject = null

// 计算日期是否改变
const isDateChanged = computed(() => {
  if (!confirmInfo.value) return false
  return confirmInfo.value.course.Date !== confirmInfo.value.newPosition.Date
})

// 计算时间是否改变
const isTimeChanged = computed(() => {
  if (!confirmInfo.value) return false
  const originalStart = confirmInfo.value.originalPosition.StartTime
  const originalEnd = confirmInfo.value.originalPosition.EndTime
  const newStart = confirmInfo.value.newPosition.StartTime
  const newEnd = confirmInfo.value.newPosition.EndTime
  return originalStart !== newStart || originalEnd !== newEnd
})// 获取新日期对应的周几名称
const getNewDayName = computed(() => {
  if (!confirmInfo.value || !confirmInfo.value.newPosition.Date) return ''

  const date = new Date(confirmInfo.value.newPosition.Date)
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return dayNames[date.getDay()]
})

// 获取变更摘要文本
const getChangeSummary = computed(() => {
  if (!confirmInfo.value) return ''

  const details = confirmInfo.value.changeDetails
  const changes = []

  if (details.dayChanged) {
    changes.push(`日期: ${details.dayChange}`)
  }

  if (details.timeChanged) {
    changes.push(`时间: ${details.timeChange}`)
  }

  if (details.teacherChanged) {
    changes.push(`${transToConfigDescript('老师')}: ${details.teacherChange}`)
  }

  if (details.classroomChanged) {
    changes.push(`教室: ${details.classroomChange}`)
  }

  return changes.join('，') || '无变更'
})

// 打开弹框
function open(params) {
  return new Promise((resolve, reject) => {
    _resolve = resolve
    _reject = reject
    checkConflict.value = true
    confirmInfo.value = params
    // 不需要初始化 time 数组，直接使用 StartTime 和 EndTime
    dialogVisible.value = true
  })
}
const conflictPromptRef = ref(null)
// 确认变更
function handleConfirm() {
  if (!confirmInfo.value || loading.value) return

  // 验证时间：结束时间必须大于开始时间
  const startTime = confirmInfo.value.newPosition.StartTime
  const endTime = confirmInfo.value.newPosition.EndTime
  
  if (startTime && endTime) {
    // 将时间字符串转换为可比较的值
    const [startHour, startMin] = startTime.split(':').map(Number)
    const [endHour, endMin] = endTime.split(':').map(Number)
    const startMinutes = startHour * 60 + startMin
    const endMinutes = endHour * 60 + endMin
    
    if (endMinutes <= startMinutes) {
      ElMessage.warning('结束时间必须大于开始时间')
      return
    }
  }

  loading.value = true
  
  // 在确认信息中添加调整类型
  confirmInfo.value.adjustType = adjustType.value
  confirmInfo.value.checkConflict = checkConflict.value
  let handlerName = adjustType.value == 1 ? "复制" : "移动";
  
  copyOrMoveCourse({
    CourseList: [{
      CourseID: confirmInfo.value.course.ID,
      DateList: [{
        StartTime: confirmInfo.value.newPosition.Date + ' ' + confirmInfo.value.newPosition.StartTime,
        EndTime: confirmInfo.value.newPosition.Date + ' ' + confirmInfo.value.newPosition.EndTime
      }]
    }],
    CheckConflict: checkConflict.value ? 1 : 0,
    CopyOrMove: adjustType.value
  }).then((res) => {
    ElMessage.success("排课" + handlerName + "成功。");
    _resolve({ confirmed: true, data: confirmInfo.value })
    dialogVisible.value = false
  }).catch((error) => {
    if (error.ErrorCode == 409) {
      conflictPromptRef.value?.open({ info: error.Data }).then(() => { }).catch((back)=>{
				if(back&&back.closeCurrent){
					dialogVisible.value=false
				}
			})
    } else {
      _reject(error)
    }
  }).finally(() => {
    loading.value = false
  })
}

// 取消变更
function handleCancel() {
  if (!confirmInfo.value) return

  loading.value = true
  try {
    // 返回取消结果
    _resolve({ confirmed: false, data: confirmInfo.value })
    dialogVisible.value = false
  } catch (error) {
    _reject(error)
  } finally {
    loading.value = false
  }
}

// 关闭弹框
function handleClose() {
  if (!confirmInfo.value) return

  // 返回取消结果
  _resolve({ confirmed: false, data: confirmInfo.value })
  dialogVisible.value = false
}

// 日期时间处理方法
const handleDateChange = (date) => {
  if (confirmInfo.value) {
    confirmInfo.value.newPosition.date = date
  }
}

const handleStartTimeChange = (time) => {
  if (confirmInfo.value && time) {
    confirmInfo.value.newPosition.StartTime = time
  }
}

const handleEndTimeChange = (time) => {
  if (confirmInfo.value && time) {
    confirmInfo.value.newPosition.EndTime = time
  }
}

// 对外暴露方法
defineExpose({
  open
})
</script>

<style lang="scss" scoped>
.course-adjust-dialog {
  :deep(.el-dialog) {
    border-radius: 8px;
    overflow: hidden;

    .el-dialog__header {
      margin: 0;
      padding: 16px 20px;
      background-color: #2878E8;
      border-bottom: none;

      .dialog-header {
        color: #fff;
        font-size: 16px;
        font-weight: normal;
      }
    }

    .el-dialog__headerbtn {
      top: 16px;

      .el-dialog__close {
        color: #fff;
      }
    }

    .el-dialog__body {
      padding: 20px;
    }
  }
}

.dialog-content {
  padding-top: 20px;

  .course-info {
    margin-bottom: 30px;
    background: #F9FAFC;
    border-radius: 8px 8px 8px 8px;
    padding: 16px;

    .course-title {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 16px;
      color: #303133;
    }

    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;

      .info-row {
        display: flex;
        font-size: 14px;

        .info-label {
          color: #909399;
          width: 80px;
          flex-shrink: 0;
        }

        .info-value {
          color: #303133;
          flex: 1;
              white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 195px;
        }
      }
    }
  }

  .time-divider {
    margin: 20px 0;
    color: #909399;
    font-size: 12px;
    position: relative;
    text-align: center;

    &::before,
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      width: 30%;
      height: 1px;
    }

    &::before {
      left: 0;
      background: linear-gradient(90deg, rgba(217, 217, 217, 0) 0%, #D9D9D9 100%);
    }

    &::after {
      right: 0;
      background: linear-gradient(-90deg, rgba(217, 217, 217, 0) 0%, #D9D9D9 100%);
    }
  }

  .time-adjust {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    align-items: center;

    .time-section {
      flex: 1;
      padding: 16px;
      border-radius: 8px;
      background-color: #F8F9FA;
      position: relative;

      &.original {
        border: 1px solid #E4E7ED;

        .time-tag {
          background: #B5B9C1;
        }
      }

      &.new {
        border: 1px solid #2878E8;

        .time-tag {
          background: #2878E8;
        }
      }

      .time-tag {
        position: absolute;
        top: 0;
        left: 0;
        width: 68px;
        height: 22px;
        border-radius: 6px 4px 4px 0px;
        font-weight: 400;
        font-size: 12px;
        color: #FFFFFF;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .time-header {
        color: #606266;
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 12px;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .time-row {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        font-size: 14px;
        height: 24px;
        position: relative;

        &:last-child {
          margin-bottom: 0;
        }

        .time-label {
          color: #909399;
          width: 80px;
          flex-shrink: 0;
        }
        .value-changed{
          color: #2878E8 !important;
        }

        .time-value {
          color: #909399;
          margin-right: 8px;
          flex: 1;
        }

        .edit-icon {
          color: #2878E8;
          position: absolute;
          right: 0;
          pointer-events: none;
        }

        .day-name {
          color: #303133;
          font-size: 14px;
          margin-left: 4px;
          white-space: nowrap;
          position: absolute;
          right: 42px;
          pointer-events: none;
        }

        .time-range-container {
          display: flex;
          align-items: center;
          gap: 4px;
          flex: 1;
          margin-right: 30px;
                :deep(.wtwo-input__inner){
          width:40px
        }
        :deep(.wtwo-input__wrapper){
          width:40px
        }
        :deep(.custom-time-picker){
          width:40px
        }
        
          .time-separator {
            color: #909399;
            font-size: 14px;
            flex-shrink: 0;
            padding: 0 2px;
          }
          
          .custom-time-picker {
            flex: 1;
            min-width: 0;
          }
        }
  
        :deep(.custom-date-picker),
        :deep(.custom-time-picker) {
          :deep(.el-input__wrapper) {
            padding: 0 8px;
          }

          :deep(.el-input__inner) {
            color: #303133;
            font-size: 14px;
          }
        }

        :deep(.custom-date-picker) {
          width: calc(100% - 80px);
          
          .wtwo-input__prefix {
            display: none;
          }

          .wtwo-input__icon {
            display: none;
          }

          .wtwo-input__wrapper {
            // box-shadow: none !important;
            // padding: 0;
            // background: none !important;

            .wtwo-input__inner {
              cursor: pointer !important;
              text-align: center;
              padding: 0px;
              border-radius: 4px;
              color: #303133;
              text-align: left;
              font-size: 14px;
            }
          }

        }

        :deep(.custom-time-picker) {
          .wtwo-input__prefix {
            display: none;
          }

          .wtwo-input__icon {
            display: none;
          }

          .wtwo-input__wrapper {
            // box-shadow: none !important;
            // padding: 0 4px;
            // background: none !important;

            .wtwo-input__inner {
              cursor: pointer !important;
              padding: 0px;
              border-radius: 4px;
              color: #303133;
              text-align: center;
              font-size: 14px;
              min-width: 40px;
            }
          }

        }
      }
    }

    .arrow-icon {
      width: 20px;
      height: 20px;
      font-size: 20px;
      color: #909399;
    }
  }

  .adjust-types {
    margin: 20px 0;
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .warning-message {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #E6A23C;
    font-size: 12px;

    .el-icon {
      font-size: 14px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;

  :deep(.el-button) {
    min-width: 90px;
  }
}
</style>