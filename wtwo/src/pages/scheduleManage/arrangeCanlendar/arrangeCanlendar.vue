<template>
  <div 
    class="schedule-example-page" 
    :class="{ 
      'fullscreen-mode': isFullscreen,
      'drag-arrange-mode': props.isDragArrangeMode,
      'mt-0!':props.isDragArrangeMode&&!isFilterExpanded
    }"
  >
  
    <!-- <div 
      v-if="selectedComponentType === CourseTimetableTypeEnum.StudentTimetable|| selectedComponentType === CourseTimetableTypeEnum.TeacherTimetable"
      v-show="!props.isDragArrangeMode"
      ref="draggableBtnBoxRef"
      class="arrange-draggable-btn-box"
      :style="draggableBtnBoxStyle"
      @mousedown="handleDragStart"
    >
      <img 
        src="https://cdn01.xiaogj.com/uploads/fe/wtwo/img/compare-arrange-btn.png" 
        width="60px"
        @click.stop="openCompareCanlendar"
        v-if="selectedComponentType === CourseTimetableTypeEnum.StudentTimetable|| selectedComponentType === CourseTimetableTypeEnum.TeacherTimetable"
        title="对比排课"
      />
      <img 
        class="mt-20px" 
        src="https://cdn01.xiaogj.com/uploads/fe/wtwo/img/drag-arrange-btn.png" 
        width="60px"
        @click.stop="handleDragArrangeClick"
        v-if="selectedComponentType === CourseTimetableTypeEnum.TeacherTimetable"
        title="拖拽排课"
      />
    </div> -->
    <div class="schedule-example-page-left">
      <div class="drag-panel-header flex-between">
        <div class="flex-center">
          <h3 class="drag-panel-title">拖拽排课</h3>
          <div class="text-12px color-[#606266] ml-6px">仅支持“{{ transToConfigDescript('老师课表') }}”</div>
          <el-tooltip effect="dark" :content="transToConfigDescript('此模式下老师会显示在“全机构”的排课/日程')" placement="top">
              <el-icon color="#909399" size="14px">
                <svg aria-hidden="true">
                  <use xlink:href="#w2-xinxitishi"></use>
                </svg>
              </el-icon>
            </el-tooltip>
        </div>
        <el-button type="info" 
              class="px-6px! color-#606266!" 
              color="#F3F4F4" size="small" @click="exitDragArrangeMode">退出</el-button>
      </div>
      <el-select
        v-model="arrangeCampusID" 
        :placeholder="transToConfigDescript('请选择1个校区')"
        class="mt-8px px-12px"
        filterable
      >
        <template #prefix>
          <p class="search-input-label">
            {{ transToConfigDescript('排课校区') }}
            
          </p>
        </template>
        <el-option-group
            v-for="group in options"
            :key="group.label"
            :label="group.label"
        >
            <el-option
                v-for="item in group.options"
                :key="item.ID"
                :label="item.Name"
                :value="item.ID"
            />
        </el-option-group>
      </el-select>
      <div class="mt-8px" style="position: relative;height: calc(100% - 68px);overflow: auto;padding: 0 12px;">
        <div 
          v-if="props.isDragArrangeMode && arrangeCampusID==''" 
          class="drag-arrange-mask"
        >
          <div style="position: relative;">
            <img src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/guide.png" width="184px"/>
            <div style="position: absolute;bottom: 6px;left:8px;height: 46px;line-height:18px;color: #fff;width: 100px;word-break: break-all;display: flex;align-items: center;">{{ transToConfigDescript('请选择排课校区') }}</div>
          </div>
        </div>
        <PageAttentionTips class="mb-4px">{{transToConfigDescript('可将下方学员、班级拖拽至老师课表排课；')}}</PageAttentionTips>
        <DragObjectView :campus-id="arrangeCampusID"/>
      </div>
    </div>
    <div class="schedule-example-page-right">
      <!-- 蒙层：拖拽模式下且非单个校区时显示 -->
      <div 
        v-if="props.isDragArrangeMode && arrangeCampusID==''" 
        class="drag-arrange-mask"
      ></div>
      
      <!-- Toolbar 工具栏 -->
      <div class="toolbar">
        <!-- 左侧：组件类型切换 -->
        <div class="toolbar-left">
          <el-select v-model="selectedComponentType" :disabled="props.isDragArrangeMode" :placeholder="transToConfigDescript('校区课表')" class="schedule-type-select"
            @change="handleComponentTypeChange">
            <template #prefix>
              <img :src="`https://cdn01.xiaogj.com/uploads/wtwo-pc/00${getIconNumber()}.png`" style="width: 18px; height: 18px;" />
            </template>
            <el-option :label="transToConfigDescript('老师课表')" :value="CourseTimetableTypeEnum.TeacherTimetable">
              <template #default>
                <div style="display: flex; align-items: center;">
                  <img src="https://cdn01.xiaogj.com/uploads/wtwo-pc/005.png" style="width: 18px; height: 18px; margin-right: 8px;" />
                  <span class="color-#303133 text-14px">{{transToConfigDescript('老师课表')}}</span>
                </div>
              </template>
            </el-option>
            <el-option label="1对1学员课表" :value="CourseTimetableTypeEnum.StudentTimetable">
              <template #default>
                <div style="display: flex; align-items: center;">
                  <img src="https://cdn01.xiaogj.com/uploads/wtwo-pc/007.png" style="width: 18px; height: 18px; margin-right: 8px;" />
                  <span class="color-#303133 text-14px">1对1学员课表</span>
                </div>
              </template>
            </el-option>
            <el-option :label="transToConfigDescript('班级课表')" :value="CourseTimetableTypeEnum.ClassTimetable">
              <template #default>
                <div style="display: flex; align-items: center;">
                  <img src="https://cdn01.xiaogj.com/uploads/wtwo-pc/003.png" style="width: 18px; height: 18px; margin-right: 8px;" />
                  <span class="color-#303133 text-14px">{{transToConfigDescript('班级课表')}}</span>
                </div>
              </template>
            </el-option>
            <el-option label="教室课表" :value="CourseTimetableTypeEnum.ClassroomTimetable">
              <template #default>
                <div style="display: flex; align-items: center;">
                  <img src="https://cdn01.xiaogj.com/uploads/wtwo-pc/004.png" style="width: 18px; height: 18px; margin-right: 8px;" />
                  <span class="color-#303133 text-14px">教室课表</span>
                </div>
              </template>
            </el-option>
            <el-option :label="transToConfigDescript('校区课表')" :value="CourseTimetableTypeEnum.TimeTimetable">
              <template #default>
                <div style="display: flex; align-items: center;">
                  <img src="https://cdn01.xiaogj.com/uploads/wtwo-pc/006.png" style="width: 18px; height: 18px; margin-right: 8px;" />
                  <span class="color-#303133 text-14px">{{transToConfigDescript('校区课表')}}</span>
                </div>
              </template>
            </el-option>
          </el-select>
          <div v-if="selectedComponentType!=CourseTimetableTypeEnum.StudentTimetable&&selectedComponentType!=CourseTimetableTypeEnum.TimeTimetable" class="schedule-view-mode-tabs ml-12px">
            <div
              class="schedule-view-mode-tab"
              :class="{ active: subViewFilter[selectedComponentType].Assign==0}"
              @click="changeSubViewFilter(0)"
            >
              有课{{ transToConfigDescript(objectName) }}
            </div>
            <div
              class="schedule-view-mode-tab"
              :class="{ active: subViewFilter[selectedComponentType].Assign==1}"
              @click="changeSubViewFilter(1)"
            >
              指定{{ transToConfigDescript(objectName) }}
            </div>
          </div>
          <el-button class="ml-12px" @click="openCompareCanlendar"
            v-if="selectedComponentType === CourseTimetableTypeEnum.StudentTimetable">
            对比排课
            <el-tooltip class="box-item" effect="dark" :content="transToConfigDescript('可同时查看多位学员、多位老师、多天的空闲时间。')" placement="top">
              <el-icon size="16px" class="ml-4px">
                <svg aria-hidden="true">
                  <use xlink:href="#w2-xinxitishi"></use>
                </svg>
              </el-icon>
            </el-tooltip>
          </el-button>
          <el-dropdown v-if="selectedComponentType!=CourseTimetableTypeEnum.TimeTimetable" class="ml-12px" trigger="click" @command="handleCoordinateSwap">
            <el-button class="px-6px!">
              <el-icon size="18px">
                <svg aria-hidden="true">
                  <use xlink:href="#w2-qiehuan"></use>
                </svg>
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="Y" :class="{'dropdown-swap-active': subViewFilter[selectedComponentType].Swap !== 'X'}">{{ transToConfigDescript(objectName) }}显示在左</el-dropdown-item>
                <el-dropdown-item command="X" :class="{'dropdown-swap-active': subViewFilter[selectedComponentType].Swap === 'X'}">{{ transToConfigDescript(objectName) }}显示在顶</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 中间：周/月切换和日期控制 -->
        <div class="toolbar-center">
          <!-- 周/月切换 - 只有校区课表才显示 -->
          <div class="schedule-view-mode-tabs" v-if="selectedComponentType === CourseTimetableTypeEnum.TimeTimetable">
            <div class="schedule-view-mode-tab" :class="{ active: viewMode === 'week' }" @click="changeViewMode('week')">
              周
            </div>
            <div class="schedule-view-mode-tab" :class="{ active: viewMode === 'month' }"
              @click="changeViewMode('month')">
              月
            </div>
          </div>

          <!-- 日期导航 -->
          <div class="date-navigation">
            <el-button :icon="ArrowLeft" plain @click="previousDateSelect" size="small" class="arrow-button" />

            <el-date-picker v-model="currentWeek" type="week"
              :format="`YYYY.MM.DD[-][${dayjs(currentWeek).add(6, 'day').format('MM.DD')}]`"
              value-format="YYYY-MM-DD" placeholder="请选择" :clearable="false"
              class="date-picker-choose-box" v-if="viewMode === 'week'" @change="handleWeekChange">
              <template #default="cell">
                <div class="wtwo-date-table-cell" :class="{ current: cell.isCurrent }">
                  <span class="wtwo-date-table-cell__text">{{ cell.renderText||cell.text }}</span>
                </div>
                <div v-if="selectedComponentType==CourseTimetableTypeEnum.TeacherTimetable&&subViewFilter[selectedComponentType].ViewBy=='byTime'" class="w-[100%] flex-center justify-center h-5px! mt-6px">
                  <div v-if="getCellCount(cell)" class="date-picker-course-count">{{getCellCount(cell)}}</div>
                </div>
              </template>
            </el-date-picker>
            <el-date-picker v-model="month" type="month" :format="`YYYY.MM`" value-format="YYYY-MM-DD" placeholder="请选择"
              :clearable="false" :editable="false" class="date-picker-choose-box w-[100px]!" v-if="viewMode === 'month'"
              @change="handleMonthChange" />
            <el-button :icon="ArrowRight" plain @click="nextDateSelect" size="small" class="arrow-button" />

            <el-button @click="goToToday" type="info" class="ml-6px! today-button px-6px!" color="#F3F4F4">
              今天
            </el-button>
            
            <!-- 展开/隐藏筛选条件按钮（拖拽排课模式下显示） -->
            <el-button 
              v-if="props.isDragArrangeMode"
              @click="toggleFilterPanel" 
              type="info" 
              class="ml-0! filter-toggle-button px-6px! color-#606266!" 
              color="#F3F4F4"
              :title="props.isFilterExpanded ? '隐藏筛选条件' : '展开筛选条件'"
            >
              筛选
              <el-icon class="ml-4px">
                <ArrowUp v-if="props.isFilterExpanded"/>
                <ArrowDown v-else/>
              </el-icon>
            </el-button>
          </div>
        </div>

        <!-- 右侧：操作按钮 -->
        <div class="toolbar-right">
          <el-popover placement="bottom" trigger="click" width="256px" popper-style="padding:12px 0">
            <template #reference>
              <el-button class="icon-button">
                <el-icon size="20px">
                  <svg aria-hidden="true">
                    <use xlink:href="#w2-a-Plattetiaosepan"></use>
                  </svg>
                </el-icon>
              </el-button>
            </template>
            <div>
              <div class="flex-between px-16px">
                <span class="font-bold color-[#303133]">背景色图例</span>
                <el-link type="primary" underline="never" @click="openColorSettings">
                  <el-icon class="mr-4px">
                    <svg aria-hidden="true">
                      <use xlink:href="#w2-shezhichangyongtiaojian"></use>
                    </svg>
                  </el-icon>
                  <span class="text-12px">自定义颜色</span>
                </el-link>
              </div>
              <template v-if="legendPureText">
                <div class="color-[#909399] line-height-[18px] text-[13px] mt-6px px-16px">
                  系统会给“同个{{ transToConfigDescript(legendPureTextTarget) }}”随机分配相同颜色，以便达到“用颜色”来区分{{ transToConfigDescript(legendPureTextTarget === '任课老师' ? '任课老师' : legendPureTextTarget === '上课课程' ? '课程' : '教室'
                  )}}的目的。
                </div>
              </template>
              <div v-else class="flex schedule-legend-box">
                <div class="flex-center" v-for="item in legendItems" :key="item.key">
                  <div class="figure-square" :style="{ backgroundColor: getTint(item.color, 0.2) }"></div>
                  <div>{{ transToConfigDescript(item.label) }}</div>
                </div>
              </div>
            </div>
          </el-popover>

          <el-button 
            v-if="!props.isDragArrangeMode"
            class="icon-button" 
            @click="toggleFullscreen" 
            :class="{ 'fullscreen-active': isFullscreen }"
          >
            <el-icon size="20px">
              <svg aria-hidden="true">
                <use :xlink:href="isFullscreen ? '#w2-quanjusuoxiao' : '#w2-quanjufangda'"></use>
              </svg>
            </el-icon>
          </el-button>
          <el-dropdown @command="handleMoreActions" class="ml-12px" trigger="click">
            <el-button class="px-5px!">
              <el-icon size="20px">
                <svg aria-hidden="true">
                  <use xlink:href="#w2-gengduocaozuo"></use>
                </svg>
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">课表偏好设置</el-dropdown-item>
                <el-dropdown-item command="export" v-if="NewCourse_CourseExport&&selectedComponentType!=CourseTimetableTypeEnum.TimeTimetable">导出为表格</el-dropdown-item>
                <el-dropdown-item command="exportImage">导出为图片</el-dropdown-item>
                <el-dropdown-item command="showPlanCourse" divided>排课进度</el-dropdown-item>
                <el-dropdown-item command="checkBusy" >查忙闲</el-dropdown-item>
                <template v-if="NewCourse_CoursePlanViaCopy">
                  <el-dropdown-item v-if="!EnableCourseApplication" command="moveCourse" divided>移动排课</el-dropdown-item>
                  <el-dropdown-item command="copyCourse">复制排课</el-dropdown-item>
                </template>
                <el-dropdown-item v-if="NewCourse_ScheduleAdd&&selectedComponentType==CourseTimetableTypeEnum.TeacherTimetable" @click="addNewSchedule" divided >新增日程</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button v-if="NewCourse_ClassCourse||NewCourse_StudentCourse||NewCourse_SubscribeCourse" type="primary" @click="addNewArrange" style="margin-left: 12px">
            新增排课
          </el-button>
        </div>
      </div>

      <!-- 骨架屏 - 仅在初次加载时显示 -->
      <div v-if="initialLoading" class="calendar-skeleton-wrapper">
        <!-- 日历表头骨架 -->
        <div class="skeleton-calendar-header">
          <el-skeleton-item variant="text" style="width: 14%; height: 43px;" v-for="i in 7" :key="i" />
        </div>
        
        <!-- 日历内容骨架 -->
        <div class="skeleton-calendar-body">
          <div class="skeleton-time-column">
            <el-skeleton-item variant="text" style="width: 60px; height: 40px; margin-bottom: 8px;" v-for="i in 12" :key="i" />
          </div>
          <div class="skeleton-calendar-grid">
            <div class="skeleton-grid-column" v-for="day in 7" :key="day">
              <div class="skeleton-course-block" v-for="block in 3" :key="block" 
                :style="{ height: block === 1 ? '80px' : block === 2 ? '120px' : '80px', marginBottom: '8px' }">
                <el-skeleton animated :throttle="100">
                  <template #template>
                    <el-skeleton-item variant="text" style="width: 80%; height: 16px; margin-bottom: 4px;" />
                    <el-skeleton-item variant="text" style="width: 60%; height: 14px; margin-bottom: 4px;" />
                    <el-skeleton-item variant="text" style="width: 50%; height: 12px;" />
                  </template>
                </el-skeleton>
              </div>
            </div>
          </div>
        </div>
      </div>

    <!-- 课程安排组件 -->
    <div v-else class="schedule-content"
      :class="subViewFilter[selectedComponentType].ViewBy === 'byTime' && viewMode === 'week' ? 'overflow-y-hidden' : 'overflow-auto'">
      <ArrangeMonthView v-if="viewMode === 'month'" :courses="courseData" :month="month"
        :search-params="monthSearchParams" @more-action="handleCourseMoreAction" />
      <TimeCanlendarView v-if="subViewFilter[selectedComponentType].ViewBy === 'byTime' && viewMode === 'week' && subViewFilter[selectedComponentType].Swap === 'Y'"
        :courses="courseData" :current-week="currentWeekDate" :search-params="monthSearchParams"
        :timetable-type="selectedComponentType" :assign-list="subViewFilter[selectedComponentType].AssignList"
        :assign="subViewFilter[selectedComponentType].Assign"
        :view-by="subViewFilter[selectedComponentType].ViewBy"
        :Weekdays="subViewFilter[selectedComponentType].Weekdays"
        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
        @object-selected="handleChildObjectSelected"
        @object-select-click="handleObjectSelectClick"
        @remove-object="handleRemoveObject"
        @update:view-by="handleViewByChange"
        @update:Weekdays="handleWeekdaysChange" />
      <!-- 转置时刻视图：对象在顶部横向排列 -->
      <TimeCanlendarViewTrans v-if="subViewFilter[selectedComponentType].ViewBy === 'byTime' && viewMode === 'week' && subViewFilter[selectedComponentType].Swap === 'X'"
        :courses="courseData" :current-week="currentWeekDate" :search-params="monthSearchParams"
        :timetable-type="selectedComponentType" :assign-list="subViewFilter[selectedComponentType].AssignList"
        :assign="subViewFilter[selectedComponentType].Assign"
        :view-by="subViewFilter[selectedComponentType].ViewBy"
        :Weekdays="subViewFilter[selectedComponentType].Weekdays"
        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
        @object-selected="handleChildObjectSelected"
        @object-select-click="handleObjectSelectClick"
        @remove-object="handleRemoveObject"
        @update:view-by="handleViewByChange"
        @update:Weekdays="handleWeekdaysChange" />
      <!-- <ObjectCanlendarView v-if="subViewFilter[selectedComponentType].ViewBy === 'onlyObject' && viewMode === 'week'"
        :courses="courseData" :current-week="currentWeekDate" :search-params="monthSearchParams"
        :timetable-type="selectedComponentType" :assign-list="subViewFilter[selectedComponentType].AssignList"
        :assign="subViewFilter[selectedComponentType].Assign"
        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse" /> -->
      <PeriodCanlendarView v-if="(subViewFilter[selectedComponentType].ViewBy === 'byPeriod'||subViewFilter[selectedComponentType].ViewBy === 'byRange') && viewMode === 'week' && subViewFilter[selectedComponentType].Swap === 'Y'"
        :courses="courseData" :current-week="currentWeekDate" :search-params="monthSearchParams"
        :timetable-type="selectedComponentType" :assign-list="subViewFilter[selectedComponentType].AssignList"
        :assign="subViewFilter[selectedComponentType].Assign"
        :view-by="subViewFilter[selectedComponentType].ViewBy"
        :Weekdays="subViewFilter[selectedComponentType].Weekdays"
        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
        @object-selected="handleChildObjectSelected"
        @object-select-click="handleObjectSelectClick"
        @update:view-by="handleViewByChange"
        @update:Weekdays="handleWeekdaysChange"
        @remove-object="handleRemoveObject" />
      <PeriodCanlendarViewTrans v-if="(subViewFilter[selectedComponentType].ViewBy === 'byPeriod'||subViewFilter[selectedComponentType].ViewBy === 'byRange') && viewMode === 'week' && subViewFilter[selectedComponentType].Swap === 'X'"
        :courses="courseData" :current-week="currentWeekDate" :search-params="monthSearchParams"
        :timetable-type="selectedComponentType" :assign-list="subViewFilter[selectedComponentType].AssignList"
        :assign="subViewFilter[selectedComponentType].Assign"
        :view-by="subViewFilter[selectedComponentType].ViewBy"
        :Weekdays="subViewFilter[selectedComponentType].Weekdays"
        @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse"
        @object-selected="handleChildObjectSelected"
        @object-select-click="handleObjectSelectClick"
        @update:view-by="handleViewByChange"
        @update:Weekdays="handleWeekdaysChange"
        @remove-object="handleRemoveObject" />
      <!-- 错误提示 -->
      <div v-if="loadingError" class="error-message">
        <el-alert :title="loadingError" type="error" :closable="false" show-icon>
          <template #default>
            <p>{{ loadingError }}</p>
            <el-button type="primary" size="small" @click="refreshCurrentWeek" style="margin-top: 8px">
              重新加载
            </el-button>
          </template>
        </el-alert>
      </div>
    </div>
  </div>
    <!-- 课表偏好设置弹窗 -->
    <TimetablePreferenceSettings ref="timetableSettingsRef" />
    <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
    <chooseStudent ref="chooseStudentRef"></chooseStudent>
    <chooseClass ref="chooseClassRef"></chooseClass>
    <chooseClassroom ref="chooseClassroomRef"></chooseClassroom>

    <!-- 弹窗组件 -->
    <cancelCourseForm ref="cancelCourseFormRef"></cancelCourseForm>
    <editArrangeContent ref="editArrangeContentRef"></editArrangeContent>
    <printArrangeStudent ref="printArrangeStudentRef"></printArrangeStudent>
    <qrSignCodePop ref="qrSignCodePopRef"></qrSignCodePop>
    <disCourseConfirm ref="disCourseConfirmRef"></disCourseConfirm>
    <classinLiveLink ref="classinLiveLinkRef"></classinLiveLink>
    <editArrangeForm ref="editArrangeFormRef"></editArrangeForm>
    <adjustCourse ref="adjustCourseRef"></adjustCourse>
    <arrangeInfo ref="arrangeInfoRef"></arrangeInfo>
    <addArrangeForm ref="addArrangeFormRef"></addArrangeForm>
    <chooseSingleCampus ref="chooseSingleCampusRef"></chooseSingleCampus>
    <copyArrageCourse ref="copyArrageCourseRef"></copyArrageCourse>
    <!-- 对比排课弹框 -->
    <compareCanlendar ref="compareCalendarRef" v-model:visible="compareCalendarVisible" :search-params="monthSearchParams" @canlendar-dbclick-add-course="handleCanlendarDbclickAddCourse" @refresh="triggerViewRefresh"/>
    <ObjectFreeTime ref="objectFreeTimeRef"></ObjectFreeTime>
    <addScheduleForm ref="addScheduleFormRef"></addScheduleForm>
    <batchArrangeInfo ref="batchArrangeInfoRef"></batchArrangeInfo>
    <periodRangeSetting ref="periodRangeSettingRef" @settings-updated="handlePeriodRangeSettingsUpdated"></periodRangeSetting>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ArrowLeft, ArrowRight, WarningFilled, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import TimeCanlendarView from './timeCanlendar/TimeCanlendarView.vue'
import TimeCanlendarViewTrans from './timeCanlendar/TimeCanlendarViewTrans.vue'
import compareCanlendar from './compareCanlendar/compareCanlendar.vue'
import ArrangeMonthView from './arrangeMonthView.vue'
import addArrangeForm from '../popup/addArrangeForm.vue'
// import ObjectCanlendarView from './objectCanlendar/objectCanlendarView.vue'
import TimetablePreferenceSettings from '../popup/timetablePreferenceSettings.vue'
import PeriodCanlendarView from './periodCanlendar/periodCanlendarView3.vue'
import PeriodCanlendarViewTrans from './periodCanlendar/periodCanlendarViewTrans.vue'
import dayjs from 'dayjs';
import { exportFile, getTint, weekDiff } from '@/utils';
import { useTimetablePreferences } from '@/store/timetablePreferences'
import html2canvas from 'html2canvas'
import { ElLoading } from 'element-plus'
import { CourseTimetableTypeEnum } from '@/types/model/timetable-preference'
import periodRangeSetting from './popup/periodRangeSetting.vue'

// 定义 emit
const emit = defineEmits<{
  'update-date-range': [dateRange: [string, string]]
  'component-ready': []
  'filter-control': [data: { disable: boolean; filterKey: string }]
}>()

// 获取选中项的图标编号
const getIconNumber = () => {
  switch (selectedComponentType.value) {
    case CourseTimetableTypeEnum.TeacherTimetable:
      return '5'
    case CourseTimetableTypeEnum.StudentTimetable:
      return '7'
    case CourseTimetableTypeEnum.ClassTimetable:
      return '3'
    case CourseTimetableTypeEnum.ClassroomTimetable:
      return '4'
    case CourseTimetableTypeEnum.TimeTimetable:
      return '6'
    default:
      return '6'
  }
}

import { getUserLabel, postUserLabel } from '@/api';
import { cloneDeep } from 'lodash'
import useEvent from '@/config/event'
// 弹窗组件
import cancelCourseForm from '../popup/cancelCourseForm.vue'

// 时段范围设置弹窗ref
const periodRangeSettingRef = ref()
import editArrangeContent from '../popup/editArrangeContent.vue'
import printArrangeStudent from '../popup/printArrangeStudent.vue'
import qrSignCodePop from '../popup/qrSignCodePop.vue'
import disCourseConfirm from '../popup/disCourseConfirm.vue'
import classinLiveLink from '../popup/classinLiveLink.vue'
import editArrangeForm from '../popup/editArrangeForm.vue'
import adjustCourse from '../popup/adjustCourse.vue'
import arrangeInfo from '../popup/arrangeInfo.vue'
import copyArrageCourse from '../popup/copyArrageCourse.vue'
import { deleteCourseList, queryTeacherCourseCountByDate, getClassRoomCampus, deleteSchedule, deleteSchedulePlan, queryCalendarTeacherExport, queryCalendarClassExport, queryCalendarStudentExport, queryCalendarClassroomExport } from '@/api/arrange'
import { checkJingBeiFinanceLock } from '@/api/comm'
import { ElMessageBox, ElMessage } from 'element-plus'
import useClipboard from 'vue-clipboard3'
import { useConfigs, useCurrentCampuses, useUserCampuses } from '@/store'
import ObjectFreeTime from '../popup/objectFreeTime.vue'
import addScheduleForm from '../popup/addScheduleForm.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
import batchArrangeInfo from '../popup/batchArrangeInfo.vue'
import DragObjectView from './child/dragObjectView.vue'

//权限
const NewCourse_ClassCourse = window.$xgj.op('NewCourse_ClassCourse') //给班级排课
const NewCourse_StudentCourse = window.$xgj.op('NewCourse_StudentCourse') //给学员排课
const NewCourse_SubscribeCourse = window.$xgj.op('NewCourse_SubscribeCourse') //排预约课
const NewCourse_CoursePlanViaCopy=window.$xgj.op("NewCourse_CoursePlanViaCopy")//复制/移动排课
const NewCourse_ScheduleAdd=window.$xgj.op("NewCourse_ScheduleAdd")//新增日程
const NewCourse_CourseExport=window.$xgj.op('NewCourse_CourseExport') //导出排课


const currentCampus = computed(() => {
  return useCurrentCampuses().campusList
})

const arrangeCampusID=ref('')
// 从store获取数据
const userCampusesStore = useUserCampuses()
const currentCampusesStore = useCurrentCampuses()

// 计算属性
const allCampusList = computed(() => {
	return userCampusesStore.userCampuses || []
})

const recentCampusList = computed(() => {
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	
	return allCampusList.value.filter((campus: any) => 
		currentCampusIds.includes(campus.ID.toString())
	)
})

const otherCampusList = computed(() => {
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	return allCampusList.value.filter((campus: any) => !currentCampusIds.includes(campus.ID.toString()))
})
// 本地选中校区变化时同步到全局
watch(arrangeCampusID, (val) => {
  if (val !== currentCampus.value) {
    setCampus(val)
  }
})

// 全局校区变化时同步到本地（仅当只有一个校区）
watch(currentCampus, (val) => {
  const arr = val ? val.split(',').filter(Boolean) : []
  if (arr.length === 1 && arrangeCampusID.value !== val) {
    arrangeCampusID.value = val
  }
})

// 处理校区变化
function setCampus(newCampusId: string) {
  const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	if(currentCampusIds.length===1&&currentCampusIds[0]===newCampusId){
		//校区没变化不要更新
	}else{
		currentCampusesStore.$state={
			campusList:newCampusId,
			multi:true
		}
		if (window.microApp) {
			window.microApp.dispatch({type:'global:setCampus',campus: newCampusId,time:Date.now()})
		}
	}
   
}

const options = computed(() => {
    // 基础列表
    let recent = recentCampusList.value
    let others = otherCampusList.value

    return [
        {
            label:'最近使用',
            options: recent
        },{
            label: recent.length>0?transToConfigDescript('其他校区'):transToConfigDescript('全部校区'),
            options: others
        }
    ]
}) 

const configs = computed(() => {
  return useConfigs().configs
})
//配置项
const ShowAllStudentsWhenCoursePlan = computed(() => { //一对一排课时，是否可以跨校区选择学员：1是（默认），0否（只能选择当前校区的学员）。
  return configs.value.ShowAllStudentsWhenCoursePlan == 1
})
const EnableCourseApplication=computed(()=>{ //是否开启更多功能菜单,1:显示更多功能的菜单
	return configs.value.EnableStore==1
})
const IsOpenShiftForDay=computed(()=>{ // 0按月 1按天
	return configs.value.IsOpenShiftForDay==1
})
const EnableMonthShiftCourse=computed(()=>{ //按月计费课程是否支持手动排课出勤不计费：0否，1是（白塔岭定制）。
	return configs.value.EnableMonthShiftCourse==1
})


// === 数据加载相关状态 ===
const loadingError = ref(null) // 加载错误信息


// === Toolbar 相关状态 ===
const selectedComponentType = ref<CourseTimetableTypeEnum>(CourseTimetableTypeEnum.TeacherTimetable)
const viewMode = ref('week')
const currentWeek = ref(dayjs(weekDiff(0)[0]).format('YYYY-MM-DD')) // 当前周的基准日期
const month = ref(dayjs(new Date()).format('YYYY-MM-DD')) // 当前月的基准日期
// const showDebugPanel = ref(false) // 控制调试面板显示
const isFullscreen = ref(false) // 全屏状态
const compareCalendarVisible = ref(false) // 对比排课弹框显示状态
// 接收外部传入的教室列表和拖拽排课模式状态
const props = defineProps<{ 
  classroomList: any[]
  isDragArrangeMode?: boolean
  isFilterExpanded?: boolean
}>()

const timetableSettingsRef = ref()
// 初始加载状态
const initialLoading = ref(true)
// 月视图查询参数（来自父级 scheduleManage 的 handleSearch 结果）
const monthSearchParams = ref({})

// 子视图当前选中的老师ID（用于周视图日历上的课程数量统计）
const currentSelectedTeacherId = ref<string>('')
// 子视图当前选中的对象信息（用于导出文件名）
const currentSelectedObject = ref<any>(null)

// 事件总线
const event = useEvent()

// 防重复调用标记（用于 keep-alive 场景下避免两个组件重复处理同一事件）
// 使用全局对象存储，确保两个组件共享同一个标记
declare global {
  interface Window {
    __scheduleEventDebounce?: {
      lastEventCall: { type: string; params: any; timestamp: number } | null
    }
  }
}
if (!window.__scheduleEventDebounce) {
  window.__scheduleEventDebounce = { lastEventCall: null }
}

// 弹窗组件引用
const cancelCourseFormRef = ref()
const editArrangeContentRef = ref()
const printArrangeStudentRef = ref()
const qrSignCodePopRef = ref()
const disCourseConfirmRef = ref()
const classinLiveLinkRef = ref()
const editArrangeFormRef = ref()
const adjustCourseRef = ref()
const arrangeInfoRef = ref<InstanceType<typeof arrangeInfo> | null>(null)
const copyArrageCourseRef = ref<InstanceType<typeof copyArrageCourse> | null>(null)
const compareCalendarRef = ref<InstanceType<typeof compareCanlendar> | null>(null)

// === 拖放按钮相关状态 ===
const draggableBtnBoxRef = ref<HTMLElement | null>(null)
const DRAGGABLE_BTN_POSITION_KEY = 'arrangeCalendar:draggableBtnPosition'
const draggableBtnPosition = ref({
  bottom: 36,
  right: 12
})
const draggableBtnBoxStyle = computed(() => ({
  bottom: `${draggableBtnPosition.value.bottom}px`,
  right: `${draggableBtnPosition.value.right}px`
}))

// 防止拖动后触发点击的标记
let isDragging = false
let dragEndTimestamp = 0
const CLICK_DELAY = 200 // 拖动结束后的点击延迟时间（毫秒）

// 从 localStorage 恢复位置
try {
  const savedPosition = localStorage.getItem(DRAGGABLE_BTN_POSITION_KEY)
  if (savedPosition) {
    draggableBtnPosition.value = JSON.parse(savedPosition)
  }
} catch (e) {
  console.warn('Failed to restore draggable button position', e)
}

// 拖放处理函数
const handleDragStart = (e: MouseEvent) => {
  e.preventDefault()
  
  const btnBox = draggableBtnBoxRef.value
  if (!btnBox) return
  
  // 记录初始位置
  const startX = e.clientX
  const startY = e.clientY
  const startBottom = draggableBtnPosition.value.bottom
  const startRight = draggableBtnPosition.value.right
  
  let hasMoved = false // 标记是否发生了移动
  const moveThreshold = 3 // 移动阈值（像素），小于此值认为是点击而非拖动
  
  // 设置拖动状态
  isDragging = false
  
  // 改变光标样式
  document.body.style.cursor = 'grabbing'
  btnBox.style.cursor = 'grabbing'
  
  const handleMouseMove = (moveEvent: MouseEvent) => {
    // 计算移动距离
    const deltaX = startX - moveEvent.clientX
    const deltaY = moveEvent.clientY - startY
    
    // 如果移动超过阈值，标记为已移动
    if (!hasMoved && (Math.abs(deltaX) > moveThreshold || Math.abs(deltaY) > moveThreshold)) {
      hasMoved = true
      isDragging = true // 设置为正在拖动
    }
    
    // 只有移动超过阈值才更新位置
    if (hasMoved) {
      const newRight = startRight + deltaX
      const newBottom = startBottom - deltaY
      
      // 限制在窗口范围内
      const minDistance = 0
      const maxRight = window.innerWidth - btnBox.offsetWidth - minDistance
      const maxBottom = window.innerHeight - btnBox.offsetHeight - minDistance
      
      draggableBtnPosition.value = {
        right: Math.max(minDistance, Math.min(maxRight, newRight)),
        bottom: Math.max(minDistance, Math.min(maxBottom, newBottom))
      }
    }
  }
  
  const handleMouseUp = (upEvent: MouseEvent) => {
    // 恢复光标样式
    document.body.style.cursor = ''
    if (btnBox) {
      btnBox.style.cursor = 'grab'
    }
    
    // 保存位置到 localStorage（只有发生移动时才保存）
    if (hasMoved) {
      // try {
      //   localStorage.setItem(DRAGGABLE_BTN_POSITION_KEY, JSON.stringify(draggableBtnPosition.value))
      // } catch (e) {
      //   console.warn('Failed to save draggable button position', e)
      // }
      // 记录拖动结束的时间戳
      dragEndTimestamp = Date.now()
    }
    
    // 延迟重置拖动状态，防止立即触发点击
    setTimeout(() => {
      isDragging = false
    }, 50)
    
    // 移除事件监听
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  // 添加事件监听
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}


// 将字符串型 currentWeek 转换为 Date 以供子组件使用
const currentWeekDate = computed(() => new Date(currentWeek.value))

// 已撤销注入 CalendarTeacher_TeacherIDList 的逻辑

// === 计算属性 ===
// 根据组件类型获取对应的对象名称
const objectName = computed(() => {
  const map: Record<string, string> = {
    [CourseTimetableTypeEnum.TeacherTimetable]: '老师',
    [CourseTimetableTypeEnum.StudentTimetable]: '学员',
    [CourseTimetableTypeEnum.ClassTimetable]: '班级',
    [CourseTimetableTypeEnum.ClassroomTimetable]: '教室',
    [CourseTimetableTypeEnum.TimeTimetable]: '',
  }
  return map[selectedComponentType.value] || ''
})

const defaultSubViewFilter = {
  [CourseTimetableTypeEnum.TeacherTimetable]: {
    Assign: 0,
    AssignList: [],
    ViewBy: 'byTime',
    Swap:'Y',
    Weekdays: [1, 2, 3, 4, 5, 6, 7]
  },
  [CourseTimetableTypeEnum.StudentTimetable]: {
    Assign: 1,
    AssignList: [],
    ViewBy: 'byTime',
    Swap:'Y',
    Weekdays: [1, 2, 3, 4, 5, 6, 7]
  },
  [CourseTimetableTypeEnum.ClassTimetable]: {
    Assign: 0,
    AssignList: [],
    ViewBy: 'byTime',
    Swap:'Y',
    Weekdays: [1, 2, 3, 4, 5, 6, 7]
  },
  [CourseTimetableTypeEnum.ClassroomTimetable]: {
    Assign: 0,
    AssignList: [],
    ViewBy: 'byTime',
    Swap:'Y',
    Weekdays: [1, 2, 3, 4, 5, 6, 7]
  },
  [CourseTimetableTypeEnum.TimeTimetable]: {
    Assign: 0,
    AssignList: [],
    ViewBy: 'byTime',
    Swap:'Y',
    Weekdays: [1, 2, 3, 4, 5, 6, 7]
  }
}
// 已保存（生效中）的设置
const subViewFilter = ref({} as any)
// 初始化默认值
subViewFilter.value = cloneDeep(defaultSubViewFilter)



// 当 Assign 切换到非“指定的X”(值!==1) 时，清空对应 AssignList，避免旧数据残留
// function watchAssignAndClear(timetableType: CourseTimetableTypeEnum) {
//   watch(
//     () => subViewFilter.value[timetableType]?.Assign,
//     (newVal) => {
//       if (Number(newVal) !== 1 && subViewFilter.value[timetableType]) {
//         subViewFilter.value[timetableType].AssignList = []
//       }
//     }
//   )
// }

// watchAssignAndClear(CourseTimetableTypeEnum.TeacherTimetable)
// watchAssignAndClear(CourseTimetableTypeEnum.ClassTimetable)
// watchAssignAndClear(CourseTimetableTypeEnum.ClassroomTimetable)

// 本地持久化 - 键名
const LOCAL_VIEW_KEY = 'arrangeCanlendar_currentView'

function persistCurrentView() {
  try {
    const currentKey = selectedComponentType.value as unknown as string
    const rawViewBy = subViewFilter.value?.[currentKey]?.ViewBy || 'byTime'
    // 保存到本地存储前，将"仅对象"模式转换为"时段视图"
    const viewBy = rawViewBy === 'onlyObject' ? 'byPeriod' : rawViewBy
    const payload = { selectedComponentType: currentKey, viewBy, viewMode: viewMode.value }
    localStorage.setItem(LOCAL_VIEW_KEY, JSON.stringify(payload))
  } catch (e) { }
}

function loadPersistedView() {
  try {
    const raw = localStorage.getItem(LOCAL_VIEW_KEY)
    if (!raw) return
    const saved = JSON.parse(raw)
    if (saved && saved.selectedComponentType !== undefined) {
      // 恢复为字符串枚举
      selectedComponentType.value = saved.selectedComponentType as CourseTimetableTypeEnum
      // 非校区课表强制为周视图
      if (selectedComponentType.value !== CourseTimetableTypeEnum.TimeTimetable) {
        viewMode.value = 'week'
      } else if (saved.viewMode === 'week' || saved.viewMode === 'month') {
        viewMode.value = saved.viewMode
      }
      const key = selectedComponentType.value as unknown as string
      // 确保 subViewFilter 存在
      if (!subViewFilter.value || !subViewFilter.value[key]) {
        subViewFilter.value = cloneDeep(defaultSubViewFilter)
      }
      if (saved.viewBy && subViewFilter.value[key]) {
        // 从本地存储加载时，将"仅对象"模式转换为"时段视图"
        subViewFilter.value[key].ViewBy = saved.viewBy === 'onlyObject' ? 'byPeriod' : saved.viewBy
      }
      // 确保 Swap 属性存在，兼容旧数据，默认为 'Y'
      if (subViewFilter.value[key] && !subViewFilter.value[key].Swap) {
        subViewFilter.value[key].Swap = 'Y'
      }

    }
  } catch (e) { }
}

// 打开对比排课弹框
const openCompareCanlendar = () => {
  // 检查是否刚结束拖动
  if (isDragging) {
    return
  }
  const timeSinceLastDrag = Date.now() - dragEndTimestamp
  if (timeSinceLastDrag < CLICK_DELAY) {
    return
  }
  compareCalendarVisible.value = true
}

// 打开拖动排课（新增排课）
const handleDragArrangeClick = () => {
  // 检查是否刚结束拖动
  if (isDragging) {
    return
  }
  const timeSinceLastDrag = Date.now() - dragEndTimestamp
  if (timeSinceLastDrag < CLICK_DELAY) {
    return
  }
  // 全局校区变化时同步到本地（仅当只有一个校区）
  const arr = currentCampus.value ? currentCampus.value.split(',').filter(Boolean) : []
  if (arr.length === 1 && arrangeCampusID.value !== currentCampus.value) {
    arrangeCampusID.value = currentCampus.value
  }
  // 触发进入拖拽排课模式事件
  event.emit('enter-drag-arrange-mode')
}

// 切换筛选条件展开状态
const toggleFilterPanel = () => {
  event.emit('toggle-filter-expanded')
}

// 退出拖拽排课模式
const exitDragArrangeMode = () => {
  event.emit('exit-drag-arrange-mode')
}

// 定义课表类型与筛选条件key的映射关系
const filterMap: Record<CourseTimetableTypeEnum, string | undefined> = {
  [CourseTimetableTypeEnum.TeacherTimetable]: 'teacherList', // 老师课表 -> 任课老师
  [CourseTimetableTypeEnum.StudentTimetable]: 'studentName', // 学员课表 -> 学员
  [CourseTimetableTypeEnum.ClassTimetable]: 'ClassName', // 班级课表 -> 上课班级
  [CourseTimetableTypeEnum.ClassroomTimetable]: 'ClassroomIDList', // 教室课表 -> 上课教室
  [CourseTimetableTypeEnum.TimeTimetable]: undefined, // 时间课表 -> 无对应筛选条件
}

// 监听subViewFilter变化，控制筛选条件的禁用状态
watch(
  () => [selectedComponentType.value, subViewFilter.value],
  (newValues, oldValues) => {
    const newComponentType = newValues[0] as CourseTimetableTypeEnum;
    const newSubViewFilter = newValues[1];
    const oldComponentType = oldValues?.[0] as CourseTimetableTypeEnum | undefined;
    
    // 旧类型：如果之前是1（指定对象），现在不是1，需要启用对应的筛选条件
    if (oldComponentType) {
      const oldFilterKey = filterMap[oldComponentType];
      const oldAssign = oldValues?.[1]?.[oldComponentType]?.Assign;
      if (oldFilterKey && oldAssign === 1) {
        emit('filter-control', { disable: false, filterKey: oldFilterKey });
      }
    }

    // 新类型：如果现在是1（指定对象），需要禁用对应的筛选条件
    if (newComponentType) {
      const newFilterKey = filterMap[newComponentType];
      const newAssign = newSubViewFilter?.[newComponentType]?.Assign;
      if (newFilterKey) {
        emit('filter-control', { disable: newAssign === 1, filterKey: newFilterKey });
      }
    }
  },
  { immediate: true, deep: true }
)

// 图例项来自偏好设置：按当前背景颜色类型映射
const timetablePrefStore = useTimetablePreferences()
const enabledColorSetting = computed(() => {
  const pref: any = timetablePrefStore.preferenceByType(selectedComponentType.value)
  const cs = pref && Array.isArray(pref.ColorSettings) ? pref.ColorSettings : []
  return cs.find((s: any) => s && s.IsEnabled) || cs[0] || null
})
const legendItems = computed(() => {
  const pref = timetablePrefStore.preferenceByType(selectedComponentType.value)
  // 无偏好时使用默认上课状态三种
  const backgroundMap = pref && (pref as any).ColorSettings && Array.isArray((pref as any).ColorSettings)
    ? (() => {
      const enabled = (pref as any).ColorSettings.find((s: any) => s && s.IsEnabled) || (pref as any).ColorSettings[0]
      const details = enabled && enabled.ColorDetails
      if (Array.isArray(details) && details.length > 0) {
        return details.map((d: any) => ({ key: String(d.EnumValue), label: d.DisplayName, color: d.ColorValue }))
      }
      return null
    })()
    : null

  if (backgroundMap && backgroundMap.length > 0) return backgroundMap
  return [
    { key: '0', label: '未上课', color: '#4893FF' },
    { key: '1', label: '已上课', color: '#67C23A' },
    { key: '2', label: '已取消', color: '#909399' },
  ]
})

// 当按 老师/课程/教室 着色时，不显示图例，仅提示纯文案
const legendPureText = computed(() => {
  const t = enabledColorSetting.value && enabledColorSetting.value.SettingType
  return t === 'ByTeacher' || t === 'ByCourse' || t === 'ByClassroom'
})
const legendPureTextTarget = computed(() => {
  const t = enabledColorSetting.value && enabledColorSetting.value.SettingType
  if (t === 'ByTeacher') return '任课老师'
  if (t === 'ByCourse') return '上课课程'
  if (t === 'ByClassroom') return '上课教室'
  return '-'
})

// 计算并发送日期范围到父组件
function updateDateRangeToParent() {
  let dateRange: [string, string] = ['', '']
  
  if (viewMode.value === 'week') {
    // 周视图：计算当前周的周一和周日
    const startOfWeek = dayjs(currentWeek.value).startOf('week').format('YYYY-MM-DD')
    const endOfWeek = dayjs(currentWeek.value).endOf('week').format('YYYY-MM-DD')
    dateRange = [startOfWeek, endOfWeek]
  } else if (viewMode.value === 'month') {
    // 月视图：按照7*6的日历网格计算日期范围（包括上个月末尾和下个月开头）
    // 获取目标月份的第一天
    const first = dayjs(month.value).startOf('month')
    // 计算第一天是星期几，将周一设为0
    const weekdayIndex = (first.day() + 6) % 7
    // 从第一天往前减去这个天数，得到网格的开始日期
    const gridStart = first.subtract(weekdayIndex, 'day')
    // 网格结束日期：开始日期 + 41天（6周*7天-1）
    const gridEnd = gridStart.add(41, 'day')
    dateRange = [gridStart.format('YYYY-MM-DD'), gridEnd.format('YYYY-MM-DD')]
  }
  
  emit('update-date-range', dateRange)
}


// 刷新当前周数据
const refreshCurrentWeek = () => {
}

// 触发视图重新查询
const triggerViewRefresh = () => {
  const bumpParams = () => {
    monthSearchParams.value = { ...(monthSearchParams.value || {}), _refresh: Date.now() }
  }
  if (viewMode.value === 'week') {
    bumpParams()
    // if (viewMode.value === 'week' && selectedComponentType.value == CourseTimetableTypeEnum.TeacherTimetable&&subViewFilter.value[selectedComponentType.value].ViewBy!='onlyObjects') {
    //   handleCurrentMonthCourseCount()
    // }
  } else if (viewMode.value === 'month') {
    // 仅改引用，确保 ArrangeMonthView 的 watch 生效
    bumpParams()
  }
  
  // 🆕 如果对比排课弹框打开，同时刷新对比排课数据
  if (compareCalendarRef.value && compareCalendarRef.value.isVisible()) {
    compareCalendarRef.value.handleRefresh()
  }
}

// === Toolbar 事件处理 ===
const handleComponentTypeChange = (value: CourseTimetableTypeEnum) => {
  // 切换课表类型时清空选中的对象
  currentSelectedObject.value = null
  currentSelectedTeacherId.value = ''

  // 如果不是校区课表，强制设置为周视图
  if (value !== CourseTimetableTypeEnum.TimeTimetable && viewMode.value == 'month') {
    viewMode.value = 'week'
  }

  // 切换课表类型，立即持久化当前视图
  persistCurrentView()

  // 触发视图重新查询
  triggerViewRefresh()
}

function changeViewMode(mode: string) {
  // 切换视图模式时清空选中的对象
  currentSelectedObject.value = null
  currentSelectedTeacherId.value = ''
  
  viewMode.value = mode
  if (mode == 'week') {
    // loadCourseData(currentWeek.value, true)
    // 切换到周视图不改变当前二级视图，仅在确认设置或切换课表类型时更新本地持久化
  } else if (mode == 'month') {
    // 触发月视图子组件的 immediate 查询：需要确保有 searchParams
    const hasParams = monthSearchParams.value && Object.keys(monthSearchParams.value).length > 0
    if (hasParams) {
      // 通过引用变更触发 ArrangeMonthView 的 watch
      monthSearchParams.value = { ...(monthSearchParams.value as any) }
    }
  }
  // 保存周/月视图到本地
  persistCurrentView()
  updateDateRangeToParent()
}

const previousDateSelect = () => {
  if (viewMode.value === 'week') {
    const newDate = new Date(currentWeek.value)
    newDate.setDate(newDate.getDate() - 7)
    currentWeek.value = dayjs(newDate).format('YYYY-MM-DD')
    // loadCourseData(newDate)
  } else {
    const newMonth = dayjs(month.value).subtract(1, 'month').format('YYYY-MM-DD')
    month.value = newMonth
    // ArrangeMonthView 通过 props.month 的变化自行发起查询
  }
  updateDateRangeToParent()
}

const nextDateSelect = () => {
  if (viewMode.value === 'week') {
    const newDate = new Date(currentWeek.value)
    newDate.setDate(newDate.getDate() + 7)
    currentWeek.value = dayjs(newDate).format('YYYY-MM-DD')
    // loadCourseData(newDate)
  } else {
    const newMonth = dayjs(month.value).add(1, 'month').format('YYYY-MM-DD')
    month.value = newMonth
    // ArrangeMonthView 通过 props.month 的变化自行发起查询
  }
  updateDateRangeToParent()
}

const goToToday = () => {
  if (viewMode.value === 'week') {
    const today = dayjs(weekDiff(0)[0]).format('YYYY-MM-DD')
    currentWeek.value = dayjs(today).format('YYYY-MM-DD')
    // if (viewMode.value === 'week' && selectedComponentType.value == CourseTimetableTypeEnum.TeacherTimetable&&subViewFilter.value[selectedComponentType.value].ViewBy!='onlyObjects') {
    //   handleCurrentMonthCourseCount()
    // }
  } else {
    const thisMonth = dayjs().startOf('month').format('YYYY-MM-DD')
    month.value = thisMonth
    // ArrangeMonthView 通过 props.month 的变化自行发起查询
  }
  updateDateRangeToParent()
}

// 处理周选择器变化
const handleWeekChange = (value: string) => {
  if (value) {
    currentWeek.value = value
    // if (viewMode.value === 'week' && selectedComponentType.value == CourseTimetableTypeEnum.TeacherTimetable&&subViewFilter.value[selectedComponentType.value].ViewBy!='onlyObjects') {
    //   handleCurrentMonthCourseCount()
    // }
    updateDateRangeToParent()
  }
}

// 处理月选择器变化
const handleMonthChange = (value: string) => {
  if (value) {
    month.value = value
    // ArrangeMonthView 通过 props.month 的变化自行发起查询
    updateDateRangeToParent()
  }
}

// 处理坐标转换切换
const handleCoordinateSwap = (command: string) => {
  if (command === 'X' || command === 'Y') {
    const currentKey = selectedComponentType.value
    if(subViewFilter.value[currentKey].Swap===command||(!subViewFilter.value[currentKey].Swap&&command==='Y')){
      return
    }
    // 确保 Swap 属性存在，兼容旧数据
    // if (!subViewFilter.value[currentKey]) {
    //   subViewFilter.value[currentKey] = cloneDeep(defaultSubViewFilter[currentKey])
    // }
    subViewFilter.value[currentKey].Swap = command
    saveViewModeSettings(true)
  }
}

const handleMoreActions = (command: string) => {
  switch (command) {
    case 'export':
      exportExcel()
      break
    case 'exportImage':
      exportCalendarAsImage()
      break
    case 'moveCourse':
      moveOrCopyCourse(2)
      break
    case 'settings':
      openTimetableSettings()
      break
    case 'copyCourse':
      moveOrCopyCourse(1)
      break
    case 'checkBusy':
      openObjectFreeTime()
      break
    case 'showPlanCourse':
      showPlanCourse()
      break
  }
}

function showPlanCourse(){
	if (window.microApp) {
		window.microApp.dispatch({type:'scheduleManage:showPlanCourse',data:Math.random()})
	}
}


function exportExcel(){
  // 获取显示的周几列表
  const displayWeekdays = subViewFilter.value[selectedComponentType.value].Weekdays || [1, 2, 3, 4, 5, 6, 7]
  
  // 计算隐藏的日期列表
  let hiddenDateList: string[] = []
  if(viewMode.value === 'week') {
    // 所有可能的周几
    const allWeekdays = [1, 2, 3, 4, 5, 6, 7]
    // 找出隐藏的周几
    const hiddenWeekdays = allWeekdays.filter(day => !displayWeekdays.includes(day))
    
    // 将隐藏的周几转换为实际日期
    const startDate = dayjs(currentWeek.value)
    hiddenWeekdays.forEach(weekday => {
      // weekday: 1=周一, 2=周二, ..., 7=周日
      // dayjs的day(): 0=周日, 1=周一, ..., 6=周六
      const targetDayOfWeek = weekday === 7 ? 0 : weekday
      const currentDayOfWeek = startDate.day()
      
      // 计算目标日期相对于起始日期的偏移
      let offset = targetDayOfWeek - currentDayOfWeek
      if (offset < 0) offset += 7
      
      const hiddenDate = startDate.add(offset, 'day').format('YYYY-MM-DD')
      hiddenDateList.push(hiddenDate)
    })
  }
  
  const params:any={
    ...monthSearchParams.value,
    IDList: subViewFilter.value[selectedComponentType.value].Assign == 1 && subViewFilter.value[selectedComponentType.value].AssignList && subViewFilter.value[selectedComponentType.value].AssignList.length ? 
          subViewFilter.value[selectedComponentType.value].AssignList.map((i: any) => i.ID ) : [],
    PageIndex:1,
    PageSize:99999,
    LayoutOrientation:subViewFilter.value[selectedComponentType.value].Swap == 'X' ? 1 : 0,
    HiddenDateList: hiddenDateList
  }
  if(viewMode.value=='week'){
    params.StartDate=currentWeek.value
    params.EndDate=dayjs(currentWeek.value).add(6, 'day').format('YYYY-MM-DD')
  }
  const exportConfig:Partial<Record<CourseTimetableTypeEnum,{
    api:(params:any)=>Promise<any>,
    label:string
  }>>={
    [CourseTimetableTypeEnum.TeacherTimetable]:{
      api:queryCalendarTeacherExport,
      label:'老师'
    },
    [CourseTimetableTypeEnum.StudentTimetable]:{
      api:queryCalendarStudentExport,
      label:'学员'
    },
    [CourseTimetableTypeEnum.ClassTimetable]:{
      api:queryCalendarClassExport,
      label:'班级'
    },
    [CourseTimetableTypeEnum.ClassroomTimetable]:{
      api:queryCalendarClassroomExport,
      label:'教室'
    }
  }
  const currentExport=exportConfig[selectedComponentType.value as CourseTimetableTypeEnum]
  if(!currentExport){
    ElMessage.warning('当前视图不支持导出')
    return
  }
  currentExport.api(params).then((res:any)=>{
    exportFile(res, `${transToConfigDescript(currentExport.label)}课表日历_${dayjs(params.StartDate).format('MM月DD日')}到${dayjs(params.EndDate).format('MM月DD日')}`)
  })
}

/**
 * 导出日历视图为图片
 */
/**
 * 导出日历视图为图片
 * 性能优化要点：
 * 1. 立即显示 loading，避免用户等待时无反馈
 * 2. 使用 setTimeout 将耗时操作推迟到下一个事件循环
 * 3. 动态调整 scale，平衡清晰度和性能
 * 4. 优化克隆文档的样式处理
 * 5. 限制最大导出尺寸，避免浏览器崩溃
 */
const exportCalendarAsImage = async () => {
  // 立即显示 loading（同步执行，确保用户立即看到）
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在准备导出...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  // 使用 setTimeout 将所有耗时操作推迟到下一个宏任务
  // 这样可以确保 loading 先渲染出来
  setTimeout(async () => {
    try {
      // 更新 loading 文案
      loadingInstance.setText('正在分析内容...')
      
      // 等待一帧
      await new Promise(resolve => requestAnimationFrame(resolve))

      // 获取要导出的容器元素
      const scheduleContent = document.querySelector('.schedule-content') as HTMLElement
      
      if (!scheduleContent) {
        throw new Error('未找到日历内容')
      }

    // 根据视图模式选择不同的截图元素
    let element: HTMLElement
    let actualWidth: number
    let actualHeight: number
    
    // 周视图 - 需要区分不同的组件类型
    if (viewMode.value === 'week') {
      // 时间课表（TimeCanlendarView）- 使用 .schedule-container
      const timeViewContainer = scheduleContent.querySelector('.schedule-container') as HTMLElement
      
      // 对象课表（objectCanlendarView）- 使用 .calendar-container
      // const objectViewContainer = scheduleContent.querySelector('.object-calendar-view .calendar-container') as HTMLElement
      
      // 时段课表（periodCanlendarView）- 使用 .calendar-container
      const periodViewContainer = scheduleContent.querySelector('.period-calendar-view .calendar-container') as HTMLElement
      
      if (timeViewContainer && !periodViewContainer) {
        // TimeCanlendarView: 使用 .schedule-container
        element = timeViewContainer
        actualWidth = Math.max(element.scrollWidth, element.clientWidth)
        actualHeight = Math.max(element.scrollHeight, element.clientHeight)
        // console.log('导出元素: TimeCanlendarView (.schedule-container)')
      } 
      // else if (objectViewContainer) {
      //   // objectCanlendarView: 截图整个 .calendar-container，包括左侧对象列
      //   element = objectViewContainer
      //   // 使用 .grid 的滚动尺寸，确保横向和纵向都完整
      //   const gridElement = objectViewContainer.querySelector('.grid') as HTMLElement
      //   if (gridElement) {
      //     // 横向：使用 scrollWidth 确保所有列都显示
      //     actualWidth = Math.max(objectViewContainer.scrollWidth, objectViewContainer.clientWidth, gridElement.scrollWidth)
      //     // 纵向：使用 scrollHeight + header 高度
      //     actualHeight = Math.max(gridElement.scrollHeight + 44, objectViewContainer.clientHeight)
      //   } else {
      //     actualWidth = Math.max(element.scrollWidth, element.clientWidth)
      //     actualHeight = Math.max(element.scrollHeight, element.clientHeight)
      //   }
      //   console.log('导出元素: objectCanlendarView (.calendar-container)')
      // } 
      else if (periodViewContainer) {
        // periodCanlendarView: 截图整个 .calendar-container
        element = periodViewContainer
        // 使用 .grid 的滚动尺寸，确保横向和纵向都完整
        const gridElement = periodViewContainer.querySelector('.grid') as HTMLElement
        if (gridElement) {
          // 横向：使用 scrollWidth 确保所有列都显示
          actualWidth = Math.max(periodViewContainer.scrollWidth, periodViewContainer.clientWidth, gridElement.scrollWidth)
          // 纵向：使用 scrollHeight + header 高度
          actualHeight = Math.max(gridElement.scrollHeight + 44, periodViewContainer.clientHeight)
        } else {
          actualWidth = Math.max(element.scrollWidth, element.clientWidth)
          actualHeight = Math.max(element.scrollHeight, element.clientHeight)
        }
        // console.log('导出元素: periodCanlendarView (.calendar-container)')
      } else {
        // 兜底方案
        element = scheduleContent
        actualWidth = Math.max(element.scrollWidth, element.clientWidth)
        actualHeight = Math.max(element.scrollHeight, element.clientHeight)
        // console.log('导出元素: 兜底方案 (.schedule-content)')
      }
    } else {
      // 月视图 - 直接使用 scheduleContent
      element = scheduleContent
      actualWidth = Math.max(element.scrollWidth, element.clientWidth)
      actualHeight = Math.max(element.scrollHeight, element.clientHeight)
      // console.log('导出元素: 月视图 (.schedule-content)')
    }
    
    // console.log('尺寸信息:', { 
    //   actualWidth,
    //   actualHeight,
    //   scrollWidth: element.scrollWidth,
    //   scrollHeight: element.scrollHeight,
    //   clientWidth: element.clientWidth,
    //   clientHeight: element.clientHeight
    // })

    // 性能优化：限制最大尺寸，避免浏览器崩溃或过慢
    const MAX_DIMENSION = 8000 // 单边最大像素
    const MAX_AREA = 20000000 // 最大面积（约 4472 x 4472）
    
    let finalWidth = actualWidth
    let finalHeight = actualHeight
    const area = actualWidth * actualHeight
    
    if (actualWidth > MAX_DIMENSION || actualHeight > MAX_DIMENSION || area > MAX_AREA) {
      const scaleRatio = Math.min(
        MAX_DIMENSION / actualWidth,
        MAX_DIMENSION / actualHeight,
        Math.sqrt(MAX_AREA / area)
      )
      finalWidth = Math.floor(actualWidth * scaleRatio)
      finalHeight = Math.floor(actualHeight * scaleRatio)
      console.warn(`尺寸过大，已缩放至 ${finalWidth} x ${finalHeight}`)
    }

    // 动态调整 scale：根据最终尺寸智能调整
    let scale = 2
    const totalPixels = finalWidth * finalHeight
    
    // 根据总像素数调整 scale，避免过大导致卡顿
    if (totalPixels > 5000000) { // > 500万像素
      scale = 1.5
      // console.log(`⚠️ 尺寸较大 (${(totalPixels/1000000).toFixed(1)}M 像素)，降低 scale 至 1.5`)
    } else if (totalPixels > 3000000) { // > 300万像素
      scale = 1.8
      // console.log(`⚠️ 尺寸较大 (${(totalPixels/1000000).toFixed(1)}M 像素)，降低 scale 至 1.8`)
    }
    
    if (finalWidth > 5000 || finalHeight > 5000) {
      scale = 1 // 超大尺寸只用 1 倍
      // console.log(`⚠️ 超大尺寸，强制使用 scale = 1`)
    }
    
    const actualRenderWidth = Math.floor(finalWidth * scale)
    const actualRenderHeight = Math.floor(finalHeight * scale)
    const renderPixels = actualRenderWidth * actualRenderHeight
    
    // console.log(`使用 scale: ${scale}`)
    // console.log(`最终导出尺寸: ${finalWidth} x ${finalHeight}`)
    // console.log(`实际渲染尺寸: ${actualRenderWidth} x ${actualRenderHeight} (${(renderPixels/1000000).toFixed(1)}M 像素)`)
    // console.log('导出目标元素:', element.className, element.id)
    // console.log('元素层级:', element.tagName, '>', element.parentElement?.tagName, '>', element.parentElement?.parentElement?.tagName)
    
    // 初始化性能统计
    // window.__exportStats = { 
    //   total: 0, 
    //   ignored: 0, 
    //   kept: 0,
    //   svgIgnored: 0,
    //   popupIgnored: 0,
    //   outsideIgnored: 0, // 外部元素
    //   samples: [], // 采样一些被过滤的元素
    //   filterTime: 0 // 过滤耗时
    // }
    // const startTime = performance.now()
    // let filterStartTime = 0
    
    // 更新 loading 文案
    if (loadingInstance) {
      loadingInstance.setText(`正在生成图片 (${finalWidth}x${finalHeight})...`)
    }
    
    // 再次等待一帧，确保 loading 文案更新
    await new Promise(resolve => requestAnimationFrame(resolve))

    // 生成 canvas
    // console.log('🚀 开始 html2canvas 渲染...')
    // console.log('目标元素:', element)
    
    const canvas = await html2canvas(element, {
        useCORS: true,              // 允许跨域图片
        allowTaint: false,          // 不允许跨域图片污染画布
        backgroundColor: '#ffffff', // 背景色
        scale: scale,               // 动态调整分辨率
        logging: false,
        width: actualWidth,
        height: actualHeight,
        windowWidth: finalWidth,    // 使用限制后的尺寸
        windowHeight: finalHeight,
        x: 0,
        y: 0,
        scrollX: 0,
        scrollY: 0,
        foreignObjectRendering: false, // 禁用外部对象渲染（性能优化）
        ignoreElements: (el) => {
        // if (filterStartTime === 0) {
        //   filterStartTime = performance.now()
        // }
        
        // const stats = window.__exportStats
        // stats.total++
        
        // 性能优化：缓存属性访问，避免多次读取 DOM
        const tagName = el.tagName
        const lowerTag = tagName ? tagName.toLowerCase() : ''
        const className = el.className
        const classStr = typeof className === 'string' ? className : ''
        const elementId = el.id || ''
        
        // 采样前 20 个元素用于分析
        // if (stats.samples.length < 20) {
        //   stats.samples.push({ 
        //     tag: lowerTag, 
        //     id: elementId || '无', 
        //     class: classStr.substring(0, 40) || '无' 
        //   })
        // }
        // if (lowerTag === 'head' && el && el.nextElementSibling && el.nextElementSibling.id === 'woneBody') {
        //   console.log("过滤的外部容器元素示例:", )
        //   stats.ignored++
        //   stats.outsideIgnored++
        //   return true
        // }

        // 策略1: 忽略明确的外部容器 ID（包括动态生成的 ID 前缀）
        if (elementId) {
          // 动态生成的弹窗容器
          if (elementId.indexOf('wtwo-popper-container-') === 0) {
            // stats.ignored++
            // stats.outsideIgnored++
            return true
          }
          
          // WonePC和Wtwo固定 ID
          const ignoreIds = [
            'header', 'leftContent', 'homepage', 'contentScrollbar', 'moduleLocation',
            'guanzaiAI', 'guanzaiChat', 'typingIndicator', 'notFindSchool',
            'schoolList_masker', 'updateSystem', 'toast', 'main_tips', 'main_tipsS',
            'importConditionSetLoad', 'contentHelpIFrame', 'js-drop', 'middleScroller',
            'schoolWrapper', 'header-user-center', 'wtwo-breadcrumb'
          ]
          
          if (ignoreIds.indexOf(elementId) !== -1) {
            // stats.ignored++
            // stats.outsideIgnored++
            return true
          }
        }
        
        // 策略2: 忽略明确的外部容器 class（使用 indexOf 优化性能）
        if (classStr && (
          classStr.indexOf('tips-trace-id') !== -1 ||
          classStr.indexOf('chat-message') !== -1 ||
          classStr.indexOf('message-avatar') !== -1 ||
          classStr.indexOf('message-bubble') !== -1 ||
          classStr.indexOf('typing-indicator') !== -1 ||
          classStr.indexOf('user-center-item') !== -1 ||
          classStr.indexOf('homepage-') !== -1 ||
          classStr.indexOf('leftContent') !== -1 ||
          classStr.indexOf('guanzai') !== -1 ||
          classStr.indexOf('system-tips') !== -1 ||
          classStr.indexOf('header-kefu-wrap') !== -1 ||
          classStr.indexOf('tooltip-container') !== -1 ||
          classStr.indexOf('help-iframe-collspan') !== -1 ||
          classStr.indexOf('tab-op-box') !== -1 ||
          classStr.indexOf('wtwo-tabs') !== -1 ||
          classStr.indexOf('search-result-box') !== -1 ||
          classStr.indexOf('component-skeleton') !== -1 ||
          classStr.indexOf('toolbar') !== -1 ||
          classStr.indexOf('view-more') !== -1 ||
          classStr.indexOf('wtwo-button') !== -1 ||
          classStr.indexOf('wtwo-loading-mask') !== -1 ||
          classStr.indexOf('wtwo-loading-spinner') !== -1 ||
          classStr.indexOf('wtwo-loading-text') !== -1 ||
          classStr.indexOf('temporary-block') !== -1
        )) {
          // stats.ignored++
          // stats.outsideIgnored++
          return true
        }
        
        // 策略3: 过滤SVG和脚本元素（使用精确匹配优化性能）
        if ( lowerTag === 'avalon' || lowerTag === 'iframe' || 
            lowerTag === 'svg' || lowerTag === 'path' || lowerTag === 'use' || 
            lowerTag === 'symbol' || lowerTag === 'link' || lowerTag === 'meta' || 
            lowerTag === 'script') {
          // stats.ignored++
          // stats.svgIgnored++
          return true
        }

        // 策略4: 过滤弹出层和下拉菜单（使用 indexOf 优化性能）
        if (classStr && (
            classStr.indexOf('wtwo-overlay') !== -1 || 
            classStr.indexOf('wtwo-popper') !== -1 ||
            classStr.indexOf('wtwo-dropdown-menu') !== -1 || 
            classStr.indexOf('wtwo-tooltip') !== -1
        )) {
          // stats.ignored++
          // stats.popupIgnored++
          return true
        }
        
        // 策略5: 过滤iconfont图标
        if (lowerTag === 'i' && classStr && classStr.indexOf('iconfont') !== -1) {
          // stats.ignored++
          // stats.svgIgnored++
          return true
        }

        // stats.kept++
        return false
      },
      onclone: (clonedDoc) => {
        // console.log("进了几次",Date.now())
        // 性能优化：简化克隆文档处理，只添加必要的样式
        const style = clonedDoc.createElement('style')
        style.textContent = `
          /* 容器可见性 */
          .schedule-content,.schedule-container,.calendar-container,.grid,.scrollable,.courses-wrapper,.week-view,.days-container{overflow:visible!important;overflow-y:visible!important;overflow-x:visible!important;height:auto!important;max-height:none!important;min-height:auto!important}
          .calendar-container{width:${actualWidth}px!important;height:${actualHeight}px!important}
          .grid{width:100%!important;height:auto!important;min-height:auto!important}
          .day-column,.time-column{display:block!important;visibility:visible!important;opacity:1!important}
          
          /* 修复 sticky 定位：将所有 sticky 改为 relative */
          .sticky,.header,.object-column-header,.day-column-header,.time-column-header,[style*="position: sticky"],[style*="position:sticky"]{position:relative!important;top:auto!important;left:auto!important;z-index:auto!important}
          
          /* 强制重置容器和表头的位置相关属性 */
          .schedule-container{margin:0!important;padding:0!important;transform:none!important}
          .header{margin:0!important;padding-top:0!important;transform:none!important}
          
          /* 修复表头内部元素的垂直对齐（保持原有的水平对齐方式）*/
          .day-column{display:flex!important;align-items:center!important;height:43px!important;padding:8px 6px!important}
          .day-column-content{display:flex!important;align-items:center!important;gap:2px!important}
          
          /* 移除"今天"特殊样式（保留"今"字的显示）*/
          .date-today{background:none!important;background-color:transparent!important;color:#303133!important;font-weight:400!important;-webkit-background-clip:unset!important;-webkit-text-fill-color:unset!important;background-clip:unset!important}
          .header-date.circle{background:#2878E8!important;color:#fff!important}
          .date-today::before,.date-today::after,.day-column::before,.day-column::after{display:none!important;content:none!important;background:none!important}
          
          /* 课程卡片完整显示 */
          .course-card,.course-item,.schedule-item{page-break-inside:avoid!important;break-inside:avoid!important}
        `
        
        // 安全地添加样式：如果没有 head，则创建一个
        if (!clonedDoc.head) {
          const head = clonedDoc.createElement('head')
          if (clonedDoc.documentElement) {
            clonedDoc.documentElement.insertBefore(head, clonedDoc.documentElement.firstChild)
          } else {
            // 极端情况：连 documentElement 都没有，直接 appendChild
            clonedDoc.appendChild(head)
          }
        }
        clonedDoc.head.appendChild(style)
        
        // 强制设置克隆元素的尺寸
        const clonedTarget = clonedDoc.querySelector('.schedule-container, .calendar-container, .schedule-content') as HTMLElement
        if (clonedTarget) {
          clonedTarget.style.width = `${actualWidth}px`
          clonedTarget.style.height = `${actualHeight}px`
          clonedTarget.style.overflow = 'visible'
        }
      }
    })
    
    // 记录过滤结束时间
    // if (filterStartTime > 0) {
    //   window.__exportStats.filterTime = performance.now() - filterStartTime
    // }
    
    // 输出性能统计
    // const endTime = performance.now()
    // const duration = ((endTime - startTime) / 1000).toFixed(2)
    // const stats = window.__exportStats
    // const ignoreRate = ((stats.ignored / stats.total) * 100).toFixed(1)
    // const filterDuration = (stats.filterTime / 1000).toFixed(2)
    // const renderDuration = ((endTime - startTime - stats.filterTime) / 1000).toFixed(2)
    
    // console.log('✅ html2canvas 渲染完成！')
    // console.log(`⏱️  总耗时: ${duration} 秒`)
    // console.log(`   - 过滤阶段: ${filterDuration} 秒 (${((stats.filterTime / (endTime - startTime)) * 100).toFixed(1)}%)`)
    // console.log(`   - 渲染阶段: ${renderDuration} 秒 (${(((endTime - startTime - stats.filterTime) / (endTime - startTime)) * 100).toFixed(1)}%)`)
    // console.log(`📊 元素统计:`)
    // console.log(`   - 总计: ${stats.total} 个元素`)
    // console.log(`   - 已忽略: ${stats.ignored} 个 (${ignoreRate}%)`)
    // console.log(`     * 外部元素 (wonePC等): ${stats.outsideIgnored} 个`)
    // console.log(`     * SVG 图标: ${stats.svgIgnored} 个`)
    // console.log(`     * 弹窗/提示: ${stats.popupIgnored} 个`)
    // console.log(`   - 已渲染: ${stats.kept} 个`)
    // console.log(`🎯 过滤效率: 减少了 ${ignoreRate}% 的渲染工作量`)
    // console.log(`🔍 元素采样 (前20个):`)
    // console.table(stats.samples)

    // 生成文件名
    // 获取当前视图类型
    const currentViewBy = subViewFilter.value[selectedComponentType.value]?.ViewBy || 'byTime'
    
    // 课表类型名称映射
    const typeNames: any = {
      [CourseTimetableTypeEnum.TeacherTimetable]: transToConfigDescript('老师课表'),
      [CourseTimetableTypeEnum.StudentTimetable]: '学员课表',
      [CourseTimetableTypeEnum.ClassTimetable]: transToConfigDescript('班级课表'),
      [CourseTimetableTypeEnum.ClassroomTimetable]: '教室课表',
      [CourseTimetableTypeEnum.TimeTimetable]: transToConfigDescript('校区课表')
    }
    
    // 根据视图模式生成周区间或月份
    let dateRangeStr = ''
    if (viewMode.value === 'week') {
      // 周视图：显示周区间（例如：2025.12.01-2025.12.07）
      const startOfWeek = dayjs(currentWeek.value).format('YYYY.MM.DD')
      const endOfWeek = dayjs(currentWeek.value).add(6, 'day').format('YYYY.MM.DD')
      dateRangeStr = `${startOfWeek}-${endOfWeek}`
    } else {
      // 月视图：显示月份（例如：2025.12）
      dateRangeStr = dayjs(month.value).format('YYYY.MM')
    }
    
    // 根据视图类型生成文件名
    let filename = ''
    const timestamp = dayjs().format('HHmmss')
    
    // 检查是否是转置视图（Swap === 'X'）
    const isTransposeView = subViewFilter.value[selectedComponentType.value]?.Swap === 'X'
    
    if (currentViewBy === 'byTime' && !isTransposeView) {
      // 时刻视图（非转置）：检查是否有选中的对象
      if (currentSelectedObject.value && currentSelectedObject.value.name) {
        // 如果有选中对象，使用对象名称：张三_周区间_时间戳
        filename = `${currentSelectedObject.value.name}_${dateRangeStr}_${timestamp}`
      } else {
        // 如果没有选中对象，使用课表类型：老师课表_周区间_时间戳
        const typeName = typeNames[selectedComponentType.value] || '课表'
        filename = `${typeName}_${dateRangeStr}_${timestamp}`
      }
    } else {
      // 其他视图（转置视图、仅对象、时段）：周区间_时间戳
      filename = `${dateRangeStr}_${timestamp}`
    }

    // 更新 loading 文案
    loadingInstance.setText('正在转换图片格式...')
    
    // 等待一帧，让用户知道进度
    await new Promise(resolve => requestAnimationFrame(resolve))

    // 转换为图片并下载（使用 0.95 质量以加快速度）
    const dataUrl = canvas.toDataURL('image/png', 0.95)
    
    const link = document.createElement('a')
    link.download = `${filename}.png`
    link.href = dataUrl
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    ElMessage.success('图片导出成功！')
    } catch (error) {
      console.error('导出图片失败:', error)
      ElMessage.error('导出图片失败，请重试')
    } finally {
      loadingInstance.close()
    }
  }, 0) // setTimeout 结束
}

// 处理课表双击新增排课事件
const handleCanlendarDbclickAddCourse = async (data: { object: any; date: string; period?: string; timeRange?: { startTime: string; endTime: string }; campusId?: string; timetableType: any, teacher?: any }) => {
  if(!NewCourse_ClassCourse&&selectedComponentType.value==CourseTimetableTypeEnum.ClassroomTimetable){
    ElMessage.warning(transToConfigDescript('暂无“给班级排课”的权限，请联系管理员！'))
    return
  }
  if(!NewCourse_StudentCourse&&selectedComponentType.value==CourseTimetableTypeEnum.StudentTimetable){
    ElMessage.warning('暂无“给学员排课”的权限，请联系管理员！')
    return
  }
  if(!NewCourse_ClassCourse&&!NewCourse_StudentCourse&&!NewCourse_SubscribeCourse){
    ElMessage.warning('暂无“排课”权限，请联系管理员！')
    return
  }
  try {
    // 检查是否需要选择校区
    let needCampusSelection = false

    const campusList = currentCampus.value ? currentCampus.value.split(',') : []
    if (campusList.length !== 1) {
      needCampusSelection = true
    }

    // 根据课表类型设置默认的排课类型
    let arrangeType = 0 // 默认给班级排课
    if (data.timetableType === CourseTimetableTypeEnum.StudentTimetable) {
      arrangeType = 1 // 学员课表默认给学员排课
      if (data.campusId && campusList.indexOf(data.campusId) == -1) {
        needCampusSelection = true
      }
    }else if(data.timetableType === CourseTimetableTypeEnum.ClassTimetable){
      if (data.campusId && campusList.indexOf(data.campusId) == -1) {
        needCampusSelection = true
      }

    }else if(data.timetableType === CourseTimetableTypeEnum.ClassroomTimetable){
      
      if (data.campusId&&data.campusId.length==1) {
        needCampusSelection = true
      }
    } else if (data.timetableType === CourseTimetableTypeEnum.TeacherTimetable) {
      // 🆕 老师课表默认给学员排课
      arrangeType = 1
    }

    // 如果需要选择校区，先显示校区选择对话框
    if (needCampusSelection) {
      await showCampusSelectionDialog(data, arrangeType)
      return
    }

    // 准备参数
    const params: any = {
      arrangeType,
      date: data.date,
      object: data.object,
      period: data.period,
      timetableType: data.timetableType,
      disabledAutoFillPlan:true  //双击新增排课，不要自动填充排课计划
    }

    // 有时间范围就传过去填充
    if (data.timeRange) {
      params.timeRange = data.timeRange
    }

    // 有老师就传老师过去填充
    if(data.teacher){
      params.teacher=data.teacher
    }

    // 如果是班级课表，添加campusId参数
    // if (data.timetableType === CourseTimetableTypeEnum.ClassTimetable && data.campusId) {
    //     params.campusId = data.campusId
    // }

    // 调用addArrangeForm
    if (addArrangeFormRef.value) {
      await addArrangeFormRef.value.open(params).then(() => {
        // 如果对比排课弹窗已挂载，只刷新对比弹窗
        if (compareCalendarRef.value&&compareCalendarRef.value.isVisible()) {
          compareCalendarRef.value.handleRefresh()
        } else {
          // 否则刷新主课表
          triggerViewRefresh()
        }
      })
    }
  } catch (error) {
    console.error('打开新增排课失败:', error)
  }
}

// 显示校区选择对话框
const showCampusSelectionDialog = async (data: { object: any; teacher?:any; date: string; period?: string; timeRange?: { startTime: string; endTime: string }; campusId?: string; timetableType: any }, arrangeType: number) => {
  try {
    let campusList: any[] = []
    if ((data.timetableType === CourseTimetableTypeEnum.StudentTimetable && !ShowAllStudentsWhenCoursePlan.value) ||
      data.timetableType === CourseTimetableTypeEnum.ClassTimetable ||
      data.timetableType === CourseTimetableTypeEnum.ClassroomTimetable) {
        
      campusList = data.campusId ? (Array.isArray(data.campusId) ? data.campusId : [data.campusId]) : []
    }
    // const campusId = await chooseSingleCampusRef.value?.open({
    //   optionTextFull: data.timetableType === CourseTimetableTypeEnum.TimeTimetable||!data.object ? transToConfigDescript('请选择1个校区新增排课') : transToConfigDescript('请选择1个校区为“') + data.object.name + '”新增排课',
    //   allowedCampusIds: campusList,
    //   subTextHtml: data.timetableType === CourseTimetableTypeEnum.StudentTimetable&&!ShowAllStudentsWhenCoursePlan.value ? (transToConfigDescript('由于开启了一对一学员“不能跨校区排课”，所以只能选择“')+`${data.object.name}”`+transToConfigDescript('的所属校区排课；<br/>如有疑问请联系系统管理员。')) : ''
    // })
    // if (campusId) {
      // 准备参数
      const params: any = {
        arrangeType,
        date: data.date,
        object: data.object,
        period: data.period,
        timetableType: data.timetableType,
        CampusID: campusList.length ==1 ? campusList[0] : '',
        disabledAutoFillPlan:true  //双击新增排课，不要自动填充排课计划
      }

      // 有时间范围就传过去填充
      if (data.timeRange) {
        params.timeRange = data.timeRange
      }
      // 有老师就传老师过去填充
      if (data.teacher){
        params.teacher=data.teacher
      }

      // 调用addArrangeForm
      if (addArrangeFormRef.value) {
        await addArrangeFormRef.value.open(params).then(() => {
          // 如果对比排课弹窗已挂载，只刷新对比弹窗
          if (compareCalendarRef.value&&compareCalendarRef.value.isVisible()) {
            compareCalendarRef.value.handleRefresh()
          } else {
            // 否则刷新主课表
            triggerViewRefresh()
          }
        })
      }
    // }
  } catch (error) {
    console.error('校区选择失败:', error)
  }
}

// 处理显示方式变化
const handleViewByChange = (viewBy: string) => {
  // 更新当前课表类型的显示方式
  subViewFilter.value[selectedComponentType.value].ViewBy = viewBy
  // 保存到后端
  saveViewModeSettings()
}

const handleWeekdaysChange = (weekdays: number[]) => {
  // 更新当前课表类型的 Weekdays
  subViewFilter.value[selectedComponentType.value].Weekdays = weekdays
  // 保存到后端
  saveViewModeSettings(true) // 使用快速保存，不触发刷新和提示
}

const addScheduleFormRef = ref<InstanceType<typeof addScheduleForm> | null>(null)
function addNewSchedule() {
  addScheduleFormRef.value?.open({
    opType: 1
  }).then(() => {
    if(selectedComponentType.value == CourseTimetableTypeEnum.TeacherTimetable) {
      triggerViewRefresh()
    }
  })
}

const addArrangeFormRef = ref<InstanceType<typeof addArrangeForm> | null>(null)
const chooseSingleCampusRef = ref()

const addNewArrange = () => {
  // const campusList = currentCampus.value.length > 0 ? currentCampus.value.split(',') : []
  // if (campusList.length == 1) {
    addArrangeFormRef.value?.open({

    }).then(() => {
      triggerViewRefresh()
    })
  // } else {
  //   chooseSingleCampusRef.value?.open({
  //     optionText: '新增排课'
  //   }).then((res: any) => {
  //     addArrangeFormRef.value?.open({}).then(() => {
  //       triggerViewRefresh()
  //     })
  //   })
  // }
}

// 移动/复制排课
function moveOrCopyCourse(type: number) {
  const campusList = currentCampus.value.length > 0 ? currentCampus.value.split(',') : []
  if (campusList.length == 1) {
    openCopyArrageCourse(type)
  } else {
    chooseSingleCampusRef.value?.open({
      optionText: `${type == 1 ? '复制' : '移动'}排课`
    }).then(() => {
      openCopyArrageCourse(type)
    })
  }
  function openCopyArrageCourse(type: number) {
    nextTick(() => {
      copyArrageCourseRef.value?.open({
        CopyOrMove: type
      }).then(() => {
        // 操作完成后刷新视图
        triggerViewRefresh()
      })
    })
  }
}

// === 全屏功能 ===
const toggleFullscreen = async () => {
  // 使用浏览器全屏API
  try {
    if (!document.fullscreenElement) {
      // 进入全屏 - 让页面布满浏览器窗口
      await document.documentElement.requestFullscreen()
      isFullscreen.value = true
    } else {
      // 退出全屏
      await document.exitFullscreen()
      isFullscreen.value = false
    }
  } catch (error) {
    console.error('全屏操作失败:', error)
  }
}

// 监听全屏状态变化
const handleDocumentFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 处理ESC键退出全屏
const handleKeydown = (event: any) => {
  if (event.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen()
  }
}

// 打开课表偏好设置
const openTimetableSettings = () => {
  timetableSettingsRef.value?.open({
    tabType: selectedComponentType.value
  }).then((result: any) => {
    if (result) {
      // 可以在这里刷新课表数据
      refreshCurrentWeek()
    }
  }).catch((error: any) => {
  })
}

// 从图例的“自定义颜色”入口打开，并跳转到对应课表类型的tab
const openColorSettings = () => {
  timetableSettingsRef.value?.open({
    tabType: selectedComponentType.value,
    nav: 'info',
    viewMode: 'color'
  }).then((result: any) => {
    if (result) {
      refreshCurrentWeek()
    }
  }).catch(() => { })
}

const chooseEmpTableRef = ref()
function selectTeacher() {
  chooseEmpTableRef.value
    ?.open({
      multi: true,
      choosed: subViewFilter.value[CourseTimetableTypeEnum.TeacherTimetable].AssignList
    })
    .then((res: any) => {
      subViewFilter.value[CourseTimetableTypeEnum.TeacherTimetable].AssignList = res.data || []
      saveViewModeSettings()
    })
}

const chooseStudentRef = ref()
function selectStudent() {
  chooseStudentRef.value
    ?.open({
      multi: true,
      choosed: subViewFilter.value[CourseTimetableTypeEnum.StudentTimetable].AssignList,
      showOneToOneStudent: true,
      disabledOneToOneStudent: true,
      condition: {
        IsOnlyShowOneToOneStudent: 1
      }
    })
    .then((res: any) => {
      if (res.data) {
        subViewFilter.value[CourseTimetableTypeEnum.StudentTimetable].AssignList = res.data || []
        saveViewModeSettings()
      }
    })
}

const chooseClassRef = ref()
function selectClass() {
  chooseClassRef.value
    ?.open({
      multi: true,
      choosed: subViewFilter.value[CourseTimetableTypeEnum.ClassTimetable].AssignList,
      showClassStatusFilter: true,
      condition: {
        shiftType: 6
      }
    })
    .then((res: any) => {
      // console.log(res.data)
      if (res.data) {
        subViewFilter.value[CourseTimetableTypeEnum.ClassTimetable].AssignList = res.data || []
        saveViewModeSettings()
      }
    })
}

const chooseClassroomRef = ref()
function selectClassroom() {
  chooseClassroomRef.value
    ?.open({
      multi: true,
      choosed: subViewFilter.value[CourseTimetableTypeEnum.ClassroomTimetable].AssignList
    })
    .then((res: any) => {
      if (res.data) {
        subViewFilter.value[CourseTimetableTypeEnum.ClassroomTimetable].AssignList = res.data || []
        saveViewModeSettings()
      }
    })
}

// 处理点击添加对象图标事件
function handleObjectSelectClick() {
  const componentType = selectedComponentType.value
  
  // 确保 subViewFilter.value[componentType] 存在
  // if (subViewFilter.value && subViewFilter.value[componentType]) {
  //   // 如果当前不是指定对象视图，先切换到指定对象视图
  //   if (subViewFilter.value[componentType].Assign !== 1) {
  //     changeSubViewFilter(1)
  //   }
  // } else {
  //   // 如果不存在，先切换到指定对象视图
  //   changeSubViewFilter(1)
  // }
  
  // 直接根据当前组件类型调用相应的对象选择方法，无需通过显示设置弹窗中转
  setTimeout(() => {
    switch (componentType) {
      case CourseTimetableTypeEnum.TeacherTimetable:
        selectTeacher()
        break
      case CourseTimetableTypeEnum.StudentTimetable:
        selectStudent()
        break
      case CourseTimetableTypeEnum.ClassTimetable:
        selectClass()
        break
      case CourseTimetableTypeEnum.ClassroomTimetable:
        selectClassroom()
        break
    }
  }, 100)
}

// 处理移除对象事件
function handleRemoveObject(item: any) {
  const componentType = selectedComponentType.value
  if (!subViewFilter.value[componentType]) return
  
  // 从AssignList中过滤掉要移除的对象
  // 兼容处理：periodCanlendarView3使用item.id(小写)，TimeCanlendarView使用item.ID(大写)
  const itemId = item.id || item.ID
  subViewFilter.value[componentType].AssignList = subViewFilter.value[componentType].AssignList.filter(
    (obj: any) => obj.ID !== itemId
  )
  
  // 保存设置到PostUserLabel
  saveViewModeSettings()
}

function changeSubViewFilter(flag:number){
  const currentKey = selectedComponentType.value||''
  if(subViewFilter.value[currentKey].Assign==flag){
    return
  }
  subViewFilter.value[currentKey].Assign=flag
  // 保存设置，但不触发视图刷新和关闭弹层
  saveViewModeSettings()
}

async function saveViewModeSettings(isQuickSave = false){
  try {
  // 组装所有课表类型的保存数据
    const formData = await Promise.all(Object.keys(subViewFilter.value).map(async (key) => {
      const filterData = subViewFilter.value[key]
      
      // 教室课表需要获取CampusList后再保存
      let assignList = filterData.AssignList || []
      if (selectedComponentType.value === CourseTimetableTypeEnum.ClassroomTimetable&&key==CourseTimetableTypeEnum.ClassroomTimetable && Array.isArray(assignList) && assignList.length > 0) {
        // 检查第一个元素是否是对象格式
        if (typeof assignList[0] === 'object' && assignList[0].ID) {
          // 提取所有教室ID
          const classroomIds = assignList.map((item: any) => item.ID)
          
          // 获取所有教室的校区信息
          let campusDataMap = new Map()
          try {
            const campusRes = await getClassRoomCampus({ ClassRoomIDlist: classroomIds })
            if (campusRes.Data && Array.isArray(campusRes.Data)) {
              campusRes.Data.forEach((item: any) => {
                campusDataMap.set(item.ID, item.CampusList || [])
              })
            }
          } catch (error) {
            console.warn('获取教室校区信息失败:', error)
          }
          
          // 更新对象数组，使用最新的CampusList
          assignList = assignList.map((item: any) => {
            const campusList = campusDataMap.get(item.ID) || []
            return {
              ...item,
              CampusList: campusList
            }
          })
          
          // 同时更新subViewFilter中的CampusList，确保数据一致性
          subViewFilter.value[key].AssignList = assignList
          isQuickSave=true
        }
      }
      
      // 处理ViewBy为"仅对象"模式的情况，自动替换为"时段视图"
      const viewBy = filterData.ViewBy === 'onlyObject' ? 'byPeriod' : filterData.ViewBy
      return {
        TimetableType: key,
        Assign: filterData.Assign,
        AssignList: assignList,
        ViewBy: viewBy,
        Swap: filterData.Swap || 'Y', // 保存坐标转换设置，默认为 'Y'
        Weekdays: filterData.Weekdays || [1, 2, 3, 4, 5, 6, 7] // 保存选中的日期，默认全选
      }
    }))
    postUserLabel({
      IsPublic: 0,
      PageName: 'arrangeCanlendar',
      Menu: '课表日历二级视图',
      ParentPageName: '',
      Type: 5,
      FormData: JSON.stringify(formData)
    }).then((res: any) => {
      if (!isQuickSave) {
        ElMessage.success('保存设置成功')
      }
      
      persistCurrentView()
      // 如果不是快速保存，则关闭弹层
      if (!isQuickSave) {
        // 触发视图重新查询
        triggerViewRefresh()
      }
    }).catch((error: any) => {
      console.error('保存设置失败:', error.message)
    })
    
  } catch (error: any) {
    console.error('保存设置失败:', error.message)
    // 这里可以添加用户提示，比如使用Element Plus的Message组件
    // ElMessage.error(error.message)
  }
}

async function getViewModeSetting() {
  try {
    const res = await getUserLabel({
      TypeList: [5],
      PageName: 'arrangeCanlendar'
    })
    if (res && Array.isArray(res.Data) && res.Data.length) {
      // 取当前页面下保存的视图配置（优先匹配 Menu='课表日历二级视图'），否则取第一条
      const target = res.Data.find((x: any) => x.Menu === '课表日历二级视图' || x.PageName === 'arrangeCanlendar') || res.Data[0]
      let parsed: any[] = []
      try {
        parsed = JSON.parse(target.FormData || target.formData || '[]')
      } catch (e) {
        parsed = []
      }

      // 基于默认值创建一份拷贝
      const next: any = cloneDeep(defaultSubViewFilter)

      // 允许的 ViewBy 取值，移除'onlyObject'
      const allowedViewBy = new Set(['byTime', 'byPeriod','byRange'])

      if (Array.isArray(parsed)) {
        for (const item of parsed) {
          const key = item?.TimetableType
          if (key === undefined || key === null) continue
          const base = cloneDeep(defaultSubViewFilter as any)[key] || { Assign: [CourseTimetableTypeEnum.TeacherTimetable, CourseTimetableTypeEnum.ClassTimetable, CourseTimetableTypeEnum.ClassroomTimetable, CourseTimetableTypeEnum.StudentTimetable, CourseTimetableTypeEnum.TimeTimetable].includes(key) ? (key === CourseTimetableTypeEnum.StudentTimetable ? 1 : 0) : 0, AssignList: [], ViewBy: 'byTime' }

          // 合法性兜底
          // 兼容历史脏数据：若 Assign 非法（数组/undefined/null/非0或1），按默认值兜底
          let assign: any = item?.Assign
          if (Array.isArray(assign) || assign === undefined || assign === null || (assign !== 0 && assign !== 1)) {
            assign = base.Assign
          }
          let assignList = Array.isArray(item?.AssignList) ? item.AssignList : base.AssignList
          
          // 对于教室课表，需要获取最新的CampusList
          if (key === CourseTimetableTypeEnum.ClassroomTimetable && Array.isArray(assignList) && assignList.length > 0) {
            // 检查第一个元素是否是对象格式
            if (typeof assignList[0] === 'object' && assignList[0].ID) {
              // 提取所有教室ID
              const classroomIds = assignList.map((item: any) => item.ID)
              
              // 获取所有教室的校区信息
              let campusDataMap = new Map()
              try {
                const campusRes = await getClassRoomCampus({ ClassRoomIDlist: classroomIds })
                if (campusRes.Data && Array.isArray(campusRes.Data)) {
                  campusRes.Data.forEach((item: any) => {
                    campusDataMap.set(item.ID, item.CampusList || [])
                  })
                }
              } catch (error) {
                console.warn('获取教室校区信息失败:', error)
              }
              
              // 更新对象数组，使用最新的CampusList
              assignList = assignList.map((item: any) => {
                const campusList = campusDataMap.get(item.ID) || []
                return {
                  ...item,
                  CampusList: campusList
                }
              })
            }
          }
          // 处理从后端加载的'onlyObject'值，自动替换为'byPeriod'
          const rawViewBy = item?.ViewBy === 'onlyObject' ? 'byPeriod' : item?.ViewBy
          const viewBy = allowedViewBy.has(rawViewBy) ? rawViewBy : base.ViewBy
          
          // 恢复 Swap 设置，兼容旧数据默认为 'Y'
          const swap = item?.Swap || 'Y'
          
          // 恢复 Weekdays 设置，兼容旧数据默认为全选
          const weekdays = Array.isArray(item?.Weekdays) && item.Weekdays.length > 0 ? item.Weekdays : (base.Weekdays || [1, 2, 3, 4, 5, 6, 7])

          next[key] = { Assign: assign, AssignList: assignList, ViewBy: viewBy, Swap: swap, Weekdays: weekdays }
        }
      }

      // 回填到状态
      subViewFilter.value = next
      // 合并后加载本地的当前视图（如有）以覆盖本次的默认选择
      // loadPersistedView()
    } else {
      subViewFilter.value = cloneDeep(defaultSubViewFilter)
      // 没有远端数据也尝试加载本地视图
      // loadPersistedView()
    }
  } catch (error) {
    console.error('获取视图设置失败:', error)
    // 出错时使用默认设置
    subViewFilter.value = cloneDeep(defaultSubViewFilter)
    // loadPersistedView()
  }
}

// 监听视图模式变化，切换时显示骨架屏
watch([viewMode, selectedComponentType, () => subViewFilter.value[selectedComponentType.value]?.ViewBy], () => {
  // 显示骨架屏
  initialLoading.value = true
  // 延迟隐藏，等待新组件加载完成
  setTimeout(() => {
    initialLoading.value = false
  }, 1000) // 增加到 1000ms，确保接口有足够时间完成
})

// 处理时间范围设置更新事件
function handlePeriodRangeSettingsUpdated() {
  // 如果当前viewBy不等于'byRange'，需要改为'byRange'并保存到接口
  const currentViewBy = subViewFilter.value[selectedComponentType.value]?.ViewBy
  if(compareCalendarRef.value&&compareCalendarRef.value.isVisible()){
    compareCalendarRef.value.handleRefresh()
  }else{
    if (currentViewBy !== 'byRange') {
      handleViewByChange('byRange')
    } else {
      // 刷新当前视图
      triggerViewRefresh()
    }
  }
}

// === 生命周期 ===
onMounted(async () => {
  
  // 添加全屏相关事件监听器
  document.addEventListener('fullscreenchange', handleDocumentFullscreenChange)
  document.addEventListener('keydown', handleKeydown)
  
  // 先从本地恢复（保证UI初始为上次视图），再拉远端并二次兜底
  // loadPersistedView()
  getViewModeSetting()
  
  // 初始化时发送日期范围到父组件
  updateDateRangeToParent()
  
  // 先隐藏自己的骨架屏
  initialLoading.value = false
  
  // 等待 DOM 更新，确保内容已经渲染到页面上
  await nextTick()
  
  // 再等待一帧，确保浏览器完成绘制
  requestAnimationFrame(() => {
    // 通知父组件骨架屏可以隐藏
    emit('component-ready')
  })

  // 监听来自 CourseDetailPopover 的全局动作事件
  try {
    event.on('arrange-course-detail-action', (payload: any) => {
      if (!payload || !payload.type) return
      handleCourseMoreAction(payload)
    })

    // 监听排课详情中的操作刷新事件
    event.on('arrange-table-list-refresh', (params: any) => {

      // 触发视图刷新
      triggerViewRefresh()
    })

    // 监听时段范围设置弹窗事件
    event.on('request-open-period-range-setting', () => {
      periodRangeSettingRef.value?.open({})
    })

    
  } catch (e) {
    console.error('事件监听器注册失败:', e)
  }
})

// 组件卸载时清理事件监听器
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleDocumentFullscreenChange)
  document.removeEventListener('keydown', handleKeydown)
  try {
    event.off && event.off('arrange-course-detail-action')
    event.off && event.off('arrange-table-list-refresh')
    event.off && event.off('request-open-period-range-setting')
  } catch (_) { }
})

// 课程数据 - 初始为空，通过API加载
const courseData = ref([])

// 暴露给父组件的查询方法：用于接收已处理的查询参数
function search(params: any) {
  monthSearchParams.value = params || {}
  // if (viewMode.value === 'week' && selectedComponentType.value == CourseTimetableTypeEnum.TeacherTimetable&&subViewFilter.value[selectedComponentType.value].ViewBy!='onlyObjects') {
  //   handleCurrentMonthCourseCount()
  // }
}

// 处理课程详情弹窗的更多操作
function handleCourseMoreAction({ type, data }: { type: string; data: any }) {
  switch (type) {
    case 'rollCall':
      rollCall(data)
      break
    case 'getScanCode':
      getScanCode(data);
      break
    case 'openAdjustCourse':
      openAdjustCourse(data)
      break
    case 'disCourse':
      disCourse(data)
      break
    case 'printCourseStudentList':
      printCourseStudentList(data)
      break
    case 'printOneToOne':
      printOneToOne(data)
      break;
    case 'copyMainInfo':
      copyMainInfo(data)
      break
    case 'openLiveLink':
      openLiveLink(data)
      break;
    case 'openBatchScheduleDialog':
      openBatchScheduleDialog(data)
      break
    case 'editContent':
      editContent(data)
      break
    case 'showShiftEdit':
      showShiftEdit(data)
      break
    case 'cancel':
      cancel(data)
      break
    case 'delCourse':
      delCourse(data)
      break
    case 'adjustStudents':
      adjustStudents(data)
      break
    case 'scheduleDetails':
      scheduleDetails(data)
      break
    case 'editScheduleOnce':
      editScheduleOnce(data)
      break
    case 'editScheduleAll':
      editScheduleAll(data)
      break
    case 'deleteScheduleOnce':
      deleteScheduleOnce(data)
      break
    case 'deleteScheduleAll':
      deleteScheduleAll(data)
      break
    default:
      console.warn('未知的操作类型:', type)
  }
}

// 获取签到二维码
function getScanCode(data: any) {
  qrSignCodePopRef.value?.open({
    ID: data.ID,
    ClassName: data.ClassName
  })
}

// 临时调课
function openAdjustCourse(data: any) {
  adjustCourseRef.value?.open({
    data: data
  }).then(() => {
    // 刷新视图
    triggerViewRefresh()
  })
}

// 撤销上课
function disCourse(data: any) {
  checkJingBeiFinanceLock({
    campusId: data.CampusID,
    date: dayjs(data.StartTime).format('YYYY-MM-DD')
  }).then((res: any) => {
    if (res.Data == 1) {
      ElMessageBox.confirm(res.ErrorMsg, '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then(() => {
        handleDis()
      })
    } else {
      handleDis()
    }
  })

  function handleDis() {
    const tipsStr = (data.CourseType == 2) ? `该线上课已经${transToConfigDescript('上课')}，撤销${transToConfigDescript('上课')}仅改变校管家系统内状态，无法撤销第三方记录，可能造成数据不吻合，请谨慎操作。` : transToConfigDescript('撤销上课后，该堂课将恢复成未上课的状态。确定撤销上课吗？')
    disCourseConfirmRef.value?.open({
      tips: tipsStr,
      data: data
    }).then(() => {
      triggerViewRefresh()
    })
  }
}

// 打印点名表
function printCourseStudentList(data: any) {
  printArrangeStudentRef.value?.open({
    ids: data.ID
  })
}

function printOneToOne(row:any){
	if (window.microApp) {
		window.microApp.dispatch({type:'scheduleManage:printCourseCard',courseCardData:{courseid:row.ID,campusid:row.CampusID}})
	}
}

// 复制主要信息
async function copyMainInfo(data: any) {
  try {
		const { toClipboard } = useClipboard();
		
		// 格式化时间函数
		const formatTime = (time: string, format: string) => {
			return time ? dayjs(time).format(format) : '';
		};
		
		// 获取星期几的中文名称
		const getWeekday = (time: string) => {
			if (!time) return '';
			const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
			const day = dayjs(time).day();
			return weekdays[day];
		};
		
		// 构建时间字符串
		const buildTimeString = () => {
			if (!data.StartTime) return '';
			
			const weekday = getWeekday(data.StartTime);
			
			if (data.Unit === 3 && !EnableMonthShiftCourse.value && !IsOpenShiftForDay.value) {
				// 按月课程：只显示日期和星期
				return formatTime(data.StartTime, 'YYYY-MM-DD') + '[' + weekday + ']';
			} else {
				// 按次或按小时课程：显示完整时间范围
				const startTime = formatTime(data.StartTime, 'YYYY-MM-DD HH:mm');
				const endTime = data.EndTime ? formatTime(data.EndTime, 'HH:mm') : '';
				return startTime + (endTime ? '~' + endTime : '') + '[' + weekday + ']';
			}
		};
		
		// 构建教室信息
		const buildClassroomInfo = () => {
			if (!data.ClassroomName && data.CourseType !== 2) return '';
			
			let classroom = data.ClassroomName || '';
				classroom = `${classroom}${data.CourseType === 2?'【在线课】':''}`;
			return classroom;
		};
		
		// 构建班级名称（包含科目）
		const buildClassName = () => {
			let className = data.ClassName || '';
			if (data.SubjectName) {
				className += `(${data.SubjectName})`;
			}
			return className;
		};
		
		// 定义要复制的字段配置
		const fields = [
			{ label: transToConfigDescript('上课课程'), value: data.ShiftName },
			{ label: transToConfigDescript('上课班级/学员'), value: buildClassName() },
			{ label: transToConfigDescript('上课校区'), value: data.CampusName },
			{ label: transToConfigDescript('任课老师'), value: data.TeacherName },
			{ label: transToConfigDescript('上课教室'), value: buildClassroomInfo() },
			{ label: transToConfigDescript('上课时间'), value: buildTimeString() },
			{ label: transToConfigDescript('上课时长'), value: data.Duration ? `${data.Duration}分钟` : '' },
			{ label: transToConfigDescript('上课状态'), value: data.FinishedName },
			{ label: '实到/应到', value: `${data.StudentAttendanceCount || 0}/${data.StudentCount || 0}` },
		];
		
		// 构建复制内容
		const mainInfo = fields
			.filter(field => field.value) // 只包含有值的字段
			.map(field => `${field.label}：${field.value}`)
			.join('\n');
		
		await toClipboard(mainInfo);
		ElMessage.success('已复制至剪贴板');
		
	} catch (error) {
		ElMessage.error('复制失败，请重试');
	}
}

// 查看直播链接
function openLiveLink(data: any) {
  classinLiveLinkRef.value?.open({
    link: data.LiveStreamingLink,
    name: data.ClassName
  })
}

//管理本批次排课
const batchArrangeInfoRef = ref<InstanceType<typeof batchArrangeInfo> | null>(null)
function openBatchScheduleDialog(row:any){
    batchArrangeInfoRef.value?.open({
		ID: row.ID,
		PlanID: row.PlanID,
		CourseMethod:row.CourseMethod,
		CampusID:row.CampusID
	})
}

// 修改上课内容
function editContent(data: any) {
  editArrangeContentRef.value?.open({
    selected: [data]
  }).then(() => {
    triggerViewRefresh()
  })
}

// 编辑排课
function showShiftEdit(data: any) {
  editArrangeFormRef.value?.open({
    CampusID: data.CampusID,
    ID: data.ID,
    ShiftID: data.ShiftID,
  }).then(() => {
    // triggerViewRefresh()
  })
}

// 取消排课
function cancel(data: any) {
  cancelCourseFormRef.value?.open({
    popTitle: '取消排课',
    isBatch: false,
    isEdit: false,
    data: data,
    gparams: {
      courseIDList: [data.ID]
    }
  }).then(() => {
    triggerViewRefresh()
  })
}

// 删除排课
function delCourse(data: any) {
  ElMessageBox.confirm(`确认删除该节课？`, '提示', {
    confirmButtonText: '是',
    cancelButtonText: '否'
  }).then(() => {
    const loading = ElLoading.service({
      lock: true,
      text: '删除中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    deleteCourseList({
      courseIDList: [data.ID]
    }).then(() => {
      ElMessage.success('删除成功')
      triggerViewRefresh()
    }).catch((error: any) => {
      ElMessage.error(error.message || '删除失败')
    }).finally(() => {
      loading.close()
    })
  })
}

// 上课学员
function adjustStudents(data: any) {
  // 打开排课详情弹窗，显示学员信息
  arrangeInfoRef.value?.open({
    ID: data.ID,
    ShiftID: data.ShiftID,
  })
}

// 排课详情
function scheduleDetails(data: any) {
  if(data.IsApprovaling){
		return
	}
	if(data.Finished==2){
		cancelCourseFormRef.value?.open({
			popTitle:'提示',
			isBatch:false,
			isEdit:true,
			data:data,
			gparams:{
				courseIDList:[data.ID]
			}
		}).then((res:any)=>{
			triggerViewRefresh()
		})
	}else{
		arrangeInfoRef.value?.open({
			ID: data.ID,
			ShiftID: data.ShiftID
		})
	}
}

// 日程编辑和删除事件处理
function editScheduleOnce(data: any) {
  addScheduleFormRef.value?.open({
    ID: data.ID,
    opType: 2,
    byPlan: false,
  }).then(() => {
    triggerViewRefresh()
  })
}

function editScheduleAll(data: any) {
  addScheduleFormRef.value?.open({
    SchedulePlanID: data.SchedulePlanID,
    opType: 2,
    byPlan: true,
  }).then(() => {
    triggerViewRefresh()
  })
}

function deleteScheduleOnce(data: any) {
  ElMessageBox.confirm(`仅删除“当前日程”，若同批次创建了多个日程，其他日程不会删除！`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    const loading = ElLoading.service({
      lock: true,
      text: '删除中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    deleteSchedule({
      IDList: [data.ID]
    }).then(() => {
      ElMessage.success('删除成功')
      triggerViewRefresh()
    }).catch((error: any) => {
      ElMessage.error(error.message || '删除失败')
    }).finally(() => {
      loading.close()
    })
  })
}

function deleteScheduleAll(data: any) {
  ElMessageBox.confirm(`删除“所有日程”，若同批次创建了多个日程，其他日程会一起删除！`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    const loading = ElLoading.service({
      lock: true,
      text: '删除中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    deleteSchedulePlan({
      IDList: [data.SchedulePlanID]
    }).then(() => {
      ElMessage.success('删除成功')
      triggerViewRefresh()
    }).catch((error: any) => {
      ElMessage.error(error.message || '删除失败')
    }).finally(() => {
      loading.close()
    })
  })
}

//点名上课
function rollCall(row:any){
	if (window.microApp) {
		let info=cloneDeep(row)
		// 将Duration从分钟转换为xx:yy格式
        if(info.Duration){
            const hours = Math.floor(info.Duration / 60)
            const minutes = info.Duration % 60
            info.Duration = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
        }
        info.Status=info.FinishedName
      
		window.microApp.dispatch({type:'scheduleManage:rollCall',courseData:info},(res:any)=>{
			if(res){
				res[0].then(()=>{
          triggerViewRefresh()
				})
			}
		})
	}
}

const singleTeacherCount=ref([] as any)
function getCellCount(cell: any) {
  let obj=singleTeacherCount.value.find((i:any)=>dayjs(i.StartTime).format('YYYY-MM-DD')==dayjs(cell.date).format('YYYY-MM-DD'))
  return obj?obj.Count:''
}
function handleCurrentMonthCourseCount() {
  singleTeacherCount.value=[]
  let params = {
    ...monthSearchParams.value,
    StartDate: currentWeek.value,
    EndDate: dayjs(currentWeek.value).add(6, 'day').format('YYYY-MM-DD'),
    CalendarTeacherID: currentSelectedTeacherId.value || ''
  }
  queryTeacherCourseCountByDate(params).then((res: any) => {
    singleTeacherCount.value=res.Data||[]
  })
}

const objectFreeTimeRef=ref<InstanceType<typeof ObjectFreeTime> | null>(null)
function openObjectFreeTime(){
	objectFreeTimeRef.value?.open({})
}

// 当从排课列表切换过来时，检查并清空禁用的筛选条件
function checkAndClearDisabledFilters() {
  const currentType = selectedComponentType.value
  const currentFilter = subViewFilter.value[currentType]
  
  console.log('[arrangeCanlendar] 检查是否需要清空筛选条件')
  console.log('[arrangeCanlendar] currentType:', currentType)
  console.log('[arrangeCanlendar] currentFilter:', currentFilter)
  console.log('[arrangeCanlendar] Assign:', currentFilter?.Assign)
  
  // 如果当前是"指定xx"模式（Assign === 1），发送清空事件
  if (currentFilter && currentFilter.Assign === 1) {
    const filterKey = filterMap[currentType]
    console.log('[arrangeCanlendar] 当前是指定模式，需要清空筛选条件:', filterKey)
    
    if (filterKey) {
      // 发送事件通知父组件清空对应的筛选条件（除了上课时间）
      emit('filter-control', {
        disable: true,
        filterKey: filterKey
      })
    }
  } else {
    console.log('[arrangeCanlendar] 当前不是指定模式，无需清空')
  }
}

defineExpose({ 
  search,
  updateDateRange: updateDateRangeToParent,
  selectedComponentType,
  filterMap,
  subViewFilter,
  checkAndClearDisabledFilters
})

// 子组件选中对象回调：记录选中的对象信息（支持所有课表类型）
function handleChildObjectSelected(payload: any){
  // 清空之前的选中对象
  currentSelectedObject.value = null
  
  // 获取对象信息
  // Time 视图传入 { object }, Period 视图传入 { objectId }
  const object = payload?.object || null
  const objectId = object?.ID || payload?.objectId || ''
  
  // 存储完整对象信息用于导出文件名（支持所有课表类型）
  if (object && object.ID) {
    let objectName = ''
    
    // 根据课表类型获取对应的名称字段
    switch (selectedComponentType.value) {
      case CourseTimetableTypeEnum.TeacherTimetable:
        // 老师：Name 或 RealName
        objectName = object.Name || object.RealName || ''
        break
      case CourseTimetableTypeEnum.StudentTimetable:
        // 学员：Name 或 RealName
        objectName = object.Name || object.RealName || ''
        break
      case CourseTimetableTypeEnum.ClassTimetable:
        // 班级：Name 或 ClassName
        objectName = object.Name || object.ClassName || ''
        break
      case CourseTimetableTypeEnum.ClassroomTimetable:
        // 教室：Name 或 ClassRoomName
        objectName = object.Name || object.ClassRoomName || ''
        break
      default:
        objectName = object.Name || ''
    }
    
    if (objectName) {
      currentSelectedObject.value = {
        id: object.ID,
        name: objectName,
        type: selectedComponentType.value
      }
    }
  }
  
  // 特殊处理：仅老师课表需要记录 teacherId 用于课程数量统计
  if (selectedComponentType.value === CourseTimetableTypeEnum.TeacherTimetable) {
    currentSelectedTeacherId.value = objectId
    // 选中老师变化时，刷新周视图右上角日期选择器的课程数量
    if(viewMode.value === 'week' && subViewFilter.value[selectedComponentType.value].ViewBy!='onlyObjects'){
      handleCurrentMonthCourseCount()
    }
  } else {
    currentSelectedTeacherId.value = ''
  }
}
// 注意：request-edit-schedule 和 request-edit-arrange 事件已移至父组件 scheduleManage.vue 统一处理
// 子组件数据加载完成回调（暂时不使用，因为子组件数据处理未完成）
// function handleDataLoaded() {
//   if (initialLoading.value) {
//     initialLoading.value = false
//   }
// }

</script>

<style lang="scss" scoped>
html, body, #app, .wtwo-main-container {
  height: 100%;
}
.schedule-example-page {
  height: 100%;
  background-color: #fff;
  border-radius: 8px;
  margin-top: 10px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  /* 拖拽排课模式下的左右布局 */
  &.drag-arrange-mode {
    flex-direction: row;
  }
}

/* 左侧面板样式 */
.schedule-example-page-left {
  display: none; /* 非拖拽模式下隐藏 */
  position: relative;
  padding:10px 0;
  &::after{
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    right: 0;
    width: 1px;
    background-color: #e4e7ed;
  }
  .drag-arrange-mode & {
    display: flex;
    flex-direction: column;
    min-width: 320px;
    width: 22%;
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
    height: 100%;
  }

  /* 拖拽排课蒙层 */
  .drag-arrange-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(2px);
    z-index: 100;
    display: flex;
    align-items: start;
    justify-content: center;
  }
  
  .drag-panel-header {
    padding: 0 12px;
    .drag-panel-title {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

/* 右侧面板样式 */
.schedule-example-page-right {
  /* 非拖拽模式：占据全部空间 */
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
  min-height: 0;
  position: relative;
  padding:10px 12px;
  overflow-x: auto;
  
  .drag-arrange-mode & {
    /* 拖拽模式：自适应剩余空间 */
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    
    /* 当蒙层显示时，阻止右侧面板滚动 */
    &:has(.drag-arrange-mask) {
      overflow: hidden;
    }
  }
  
  /* 拖拽排课蒙层 */
  .drag-arrange-mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(2px);
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .calendar-skeleton-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 600px;
    
    .skeleton-date-nav {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
      padding: 16px;
      background: linear-gradient(90deg, #f5f7fa 25%, #e8ecf0 50%, #f5f7fa 75%);
      background-size: 200% 100%;
      animation: shimmer 2s infinite linear;
      border-radius: 8px;
    }
    
    .skeleton-calendar-header {
      display: flex;
      justify-content: space-around;
      align-items: center;
      padding: 0px 12px;
      background: linear-gradient(90deg, #f0f2f5 25%, #e0e4e8 50%, #f0f2f5 75%);
      background-size: 200% 100%;
      animation: shimmer 2s infinite linear;
      border-radius: 8px 8px 0 0;
      margin-bottom: 2px;
    }
    
    .skeleton-calendar-body {
      flex: 1;
      display: flex;
      gap: 12px;
      overflow: hidden;
      background: #fafbfc;
      padding: 12px;
      border-radius: 0 0 8px 8px;
      
      .skeleton-time-column {
        width: 60px;
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      
      .skeleton-calendar-grid {
        flex: 1;
        display: grid;
        grid-template-columns: repeat(7, 1fr);
        gap: 2px;
        
        .skeleton-grid-column {
          border-right: 1px solid #e8e8e8;
          padding: 0 6px;
          
          &:last-child {
            border-right: none;
          }
        }
        
        .skeleton-course-block {
          background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 50%, #ffffff 100%);
          background-size: 200% 100%;
          animation: shimmer 2.5s infinite linear;
          border-radius: 6px;
          padding: 10px;
          border: 1px solid #e8ecf0;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
          transition: transform 0.2s, box-shadow 0.2s;
          
          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
          }
        }
      }
    }
  }
  
  @keyframes shimmer {
    0% {
      background-position: 200% 0;
    }
    100% {
      background-position: -200% 0;
    }
  }
}

/* 全屏模式样式 */
.schedule-example-page.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
  margin: 0;
  border-radius: 0;
  background-color: #fff;
  overflow-y: auto;
  box-sizing: border-box;
}

.schedule-example-page h2 {
  margin-bottom: 20px;
  color: #333;
  text-align: center;
}

.schedule-content {
  flex: 1;
  min-height: 0;
  z-index: 65;
//   isolation: isolate;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  height: 32px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
}

.toolbar-center {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.toolbar-right {
  display: flex;
  align-items: center;
  flex: 0 0 auto;

  .icon-button {
    padding: 0 5px;
    transition: all 0.3s ease;
  }

  .icon-button.fullscreen-active {
    background-color: #e6f4ff;
    color: #1890ff;
    border-color: #91caff;
  }

  .icon-button.fullscreen-active:hover {
    background-color: #bae0ff;
    color: #0958d9;
  }
}

.schedule-type-select {
  width: 155px;
  :deep(.wtwo-select__selected-item){
    font-weight: bold;
  }
  :deep(.wtwo-select__wrapper) {
    background-color: #EAF3FF;
    box-shadow: 0 0 0 1px #EAF3FF inset;
    border-radius: 4px;

    .wtwo-select__selected-item,
    .wtwo-select__caret {
      color: var(--wtwo-color-primary);
    }
    &.is-disabled {
      opacity: 0.7;
    }
  }
}

.schedule-view-mode-tabs {
  display: flex;
  background-color: #F3F4F4;
  border-radius: 6px;
  padding: 2px;
  gap: 4px;
  width: fit-content;
}

.schedule-view-mode-tab {
  padding: 7px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: #606266;
  background-color: transparent;
  white-space: nowrap;

  &:hover {
    background-color: rgba(64, 158, 255, 0.1);
    color: var(--wtwo-color-primary);
  }

  &.active {
    background-color: #fff;
    color: var(--wtwo-color-primary);
    font-weight: 500;
  }
}

.date-navigation {
  display: flex;
  align-items: center;
  border-radius: 6px;
  padding: 0 20px;
  gap: 14px;

  .wtwo-button.arrow-button {
    background: transparent;
    color: #666;
    width: 18px;
    height: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
  }

  .wtwo-button.arrow-button:hover:not(:disabled) {
    background: #e6f4ff;
    color: #1890ff;
  }

  .wtwo-button.arrow-button:disabled {
    background: transparent;
    color: #d9d9d9;
    cursor: not-allowed;
  }

  .wtwo-button.arrow-button .wtwo-icon {
    font-size: 16px;
  }

  .wtwo-button.today-button {
    color: #606266;
    font-size: 13px;
    padding: 8px 11px;
  }

  :deep(.date-picker-choose-box) {
    width: 145px;

    .wtwo-input__prefix {
      display: none;
    }

    .wtwo-input__wrapper {
      box-shadow: none !important;
      padding: 0;

      .wtwo-input__inner {
        cursor: pointer !important;
        font-weight: 600;
        text-align: center;
        padding: 0 6px!important;
        border-radius: 4px;
        color: #303133;
        background-color: #F3F4F4;
      }
    }
  }
}

.current-week-display {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  min-width: 180px;
  text-align: center;
  white-space: nowrap;
}



.control-panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}





.toolbar .wtwo-radio-button__inner {
  border: none;
  background: transparent;
  color: #666;
  padding: 6px 12px;
  border-radius: 4px;
}

.toolbar .wtwo-radio-button__original-radio:checked+.wtwo-radio-button__inner {
  background: white;
  color: #1890ff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 错误提示样式 */
.error-message {
  margin-top: 16px;
  padding: 0 20px;
}

.error-message .wtwo-alert {
  border-radius: 8px;
}
</style>
<style lang="scss">
.schedule-legend-box {
  gap: 16px;
  padding: 0 16px;
  color: #606266;
  line-height: 20px;
  border-top: 1px solid #F4F4F4;
  margin-top: 10px;
  padding-top: 10px;

  .figure-square {
    width: 14px;
    height: 14px;
    border-radius: 2px;
    margin-right: 4px;
  }
}

.date-picker-course-count {
  width: 26px;
  height: 14px;
  border-radius: 72px;
  background: #FFECE8;
  color: #F53F3F;
  font-size: 10px;
  line-height: 12px;
  text-align: center;
}
.dropdown-swap-active{
  background-color: #EAF3FF;
  color: var(--wtwo-color-primary);
  &:hover,&:not(.is-disabled):hover,&:focus{
    background-color: #EAF3FF!important;
    color: var(--wtwo-color-primary)!important;
  }

  .view-checkbox-group-item {
    margin-bottom: 10px;

    .wtwo-checkbox {
      height: 22px;
    }

    .view-checkbox-group-item-content {
      margin-top: 4px;
      padding-left: 20px;
    }
  }
}

.date-picker-course-count {
  width: 26px;
  height: 14px;
  border-radius: 72px;
  background: #FFECE8;
  color: #F53F3F;
  font-size: 10px;
  line-height: 12px;
  text-align: center;
}
.arrange-draggable-btn-box{
  display: flex;
  flex-direction: column;
  position: fixed;
  z-index: 100;
  cursor: grab;
  user-select: none;
  -webkit-user-drag: none; /* Chrome, Safari, Edge, Opera */
  -ms-user-select: none; /* IE 10+ */
  -moz-user-select: none; /* Firefox */
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  }
  &:active {
    cursor: grabbing;
  }
  &>img{
    cursor: pointer;
    pointer-events: auto;
    user-select: none;
    -webkit-user-drag: none; /* Chrome, Safari, Edge, Opera */
    -ms-user-select: none; /* IE 10+ */
    -moz-user-select: none; /* Firefox */
  }

}
.drag-panel-filter-box{
  margin-top: 6px;
  .filter-item{
    display: flex;
    align-items: center;
    margin-bottom: 6px;
  }
}
</style>