<template>
  <el-drawer v-model="drawer" title="课表偏好设置" direction="rtl" size="820px" class="timetable-preference-settings"
    :close-on-click-modal="false" :append-to-body="true" :before-close="beforeClose" :destroy-on-close="true">
    <div class="drawer-body-wrap" v-loading="loading">
      <!-- 左侧导航 -->
      <div class="settings-nav">
        <!-- 导航菜单 -->
        <div class="nav-menu">
          <div v-for="nav in navItems" :key="nav.key" :class="['nav-item', { active: activeNav === nav.key }]"
            @click="switchNav(nav.key)">
            <span class="nav-text">{{ nav.label }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧内容区域 -->
      <div class="settings-content">
        <!-- 信息与颜色 -->
        <div v-if="activeNav === 'info'" class="info-settings">
          <!-- 示例效果 -->
          <div class="info-settings-content">
            <div class="example-effect">
              <div style="position: absolute;left:10px;top:10px">效果预览</div>
              <div class="example-card">
                <div class="course-card has-annotation" 
                  :style="{ 
                    borderTopColor: currentCardColor, 
                    backgroundColor: getTint(currentCardColor, 0.1),
                    marginLeft: '246px'
                  }"
                >
                  <div class="annotation-right-box">【颜色设置】背景颜色</div>
                  <div class="course-time">09:00-09:30</div>                  
                  <div v-if="leftFields.length > 0" class="course-meta has-annotation">
                      <div class="annotation-left-box">【信息设置】基础信息</div>
                      <div
                        class="meta-item ellipsis-single"
                        v-for="f in leftFields"
                        :key="String(f.FieldName ?? f.DisplayName ?? '')"
                      >
                        {{ getLeftFieldText(f.FieldName ?? undefined) || f.DisplayName }}
                      </div>
                  </div>
                  <div v-if="enabledRightTags.length > 0" class="course-tags has-annotation">
                    <div class="annotation-left-box">【信息设置】标签信息</div>
                    <div class="annotation-right-box">【颜色设置】标签颜色</div>
                    <span
                      v-for="tag in enabledRightTags"
                      :key="String(tag.FieldName ?? tag.DisplayName ?? tag.SortOrder)"
                      class="course-tags-item ellipsis-single"
                      :style="{ maxWidth: `calc(100% / ${calWidthNum})`, color: getTagColor(tag) }"
                      :title="getTagPreviewText(tag)"
                    >
                      {{ getTagPreviewText(tag) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div class="schedule-color-content-setting">
              <!-- 课表类型切换 -->
              <div class="timetable-type-tabs">
                <el-tabs v-model="currentTimetableType" @tab-change="handleTimetableTypeChange">
                  <el-tab-pane :name="CourseTimetableTypeEnum.TeacherTimetable">
                    <template #label>
                      <span class="tab-label">
                        {{transToConfigDescript('老师课表')}}
                        <span v-if="hasTabChanges(CourseTimetableTypeEnum.TeacherTimetable)" class="tab-dot"></span>
                      </span>
                    </template>
                  </el-tab-pane>
                  <el-tab-pane :name="CourseTimetableTypeEnum.StudentTimetable">
                    <template #label>
                      <span class="tab-label">
                        1对1学员课表
                        <span v-if="hasTabChanges(CourseTimetableTypeEnum.StudentTimetable)" class="tab-dot"></span>
                      </span>
                    </template>
                  </el-tab-pane>
                  <el-tab-pane :name="CourseTimetableTypeEnum.ClassTimetable">
                    <template #label>
                      <span class="tab-label">
                        {{transToConfigDescript('班级课表')}}
                        <span v-if="hasTabChanges(CourseTimetableTypeEnum.ClassTimetable)" class="tab-dot"></span>
                      </span>
                    </template>
                  </el-tab-pane>
                  <el-tab-pane :name="CourseTimetableTypeEnum.ClassroomTimetable">
                    <template #label>
                      <span class="tab-label">
                        教室课表
                        <span v-if="hasTabChanges(CourseTimetableTypeEnum.ClassroomTimetable)" class="tab-dot"></span>
                      </span>
                    </template>
                  </el-tab-pane>
                  <el-tab-pane :name="CourseTimetableTypeEnum.TimeTimetable">
                    <template #label>
                      <span class="tab-label">
                        {{transToConfigDescript('校区课表')}}
                        <span v-if="hasTabChanges(CourseTimetableTypeEnum.TimeTimetable)" class="tab-dot"></span>
                      </span>
                    </template>
                  </el-tab-pane>
                </el-tabs>
              </div>
              <PageAttentionTips class="ml-16px">设置的效果仅会对你生效，不会影响其他人，请放心修改！</PageAttentionTips>
              <div class="schedule-view-mode-tabs">
                <div
                  class="schedule-view-mode-tab"
                  :class="{ active: viewMode === 'info' }"
                  @click="changeViewMode('info')"
                >
                  信息设置
                </div>
                <div
                  class="schedule-view-mode-tab"
                  :class="{ active: viewMode === 'color' }"
                  @click="changeViewMode('color')"
                >
                  颜色设置
                </div>
              </div>
              

              <!-- 信息设置配置 -->
              <div class="info-config">

                <div class="config-grid" v-if="viewMode === 'info'">
                  <!-- 左边信息配置 -->
                  <div class="config-section">
                    <div class="section-header">
                      <h5>基础信息<el-divider direction="vertical" /><span class="hint">最多设置7个</span></h5>
                    </div>
                    <div class="field-list">
                      <!-- 可拖拽字段 -->
                      <VueDraggable v-model="leftFields" item-key="FieldName" handle=".drag-handle" @update="onLeftFieldDragUpdate" class="draggable-list" v-if="leftFields.length > 0">
                        <div v-for="field in leftFields" :key="field.FieldName || ''" class="field-item">
                          <el-icon title="移除" @click="removeLeftField(field)" size="16px" class="mr-8px cursor-pointer">
                            <svg aria-hidden="true"><use xlink:href="#w2-yichu"></use></svg>
                          </el-icon>
                          <span class="field-name">{{ transToConfigDescript(field.DisplayName||'') }}</span>
                          <el-icon class="drag-handle columns-item-drag-icon" title="拖拽排序">
                            <svg aria-hidden="true"><use xlink:href="#w2-tuodongpaixu"></use></svg>
                          </el-icon>
                        </div>
                      </VueDraggable>
                    </div>
                    <el-popover placement="left-start" :width="200" trigger="click" :popper-style="{padding:'8px 10px'}" :hide-after="0" @before-enter="searchKeyword = ''">
                      <template #reference>
                        <el-link type="primary" underline="never" class="add-btn">
                          <el-icon class="mr-8px" size="16px">
                            <svg aria-hidden="true">
                                <use
                                    xlink:href="#w2-tianjia"
                                ></use>
                            </svg>
                          </el-icon>
                          添加信息（{{leftFields.length}}/7）
                      </el-link>
                      </template>
                      <div>
                        <div class="mb-10px">
                          <el-input v-model="searchKeyword" placeholder="搜索字段" clearable>
                            <template #prefix>
                              <el-icon class="el-input__icon"><Search /></el-icon>
                            </template>
                          </el-input>
                        </div>
                        <div class="left-field-list" style="max-height: 300px;overflow-y: auto;">
                          <div class="left-field-item cursor-pointer" v-for="field in filteredLeftInfoFields" :key="field.FieldName" @click="toggleFieldSelection(field)">
                            <el-checkbox :model-value="isFieldSelected(field)" >
                              {{ transToConfigDescript(field.DisplayName) }}
                            </el-checkbox>
                          </div>
                        </div>
                        <div class="flex-end mt-10px">
                          <el-button @click="cancelAddFields" size="small">取消</el-button>
                          <el-button type="primary" @click="confirmAddFields" size="small">确认</el-button>
                        </div>
                      </div>
                    </el-popover>
                    
                  </div>

                  <!-- 右边标签配置 -->
                  <div class="config-section">
                    <div class="section-header">
                      <h5>标签信息<el-divider direction="vertical" /><span class="hint">最多设置3个</span></h5>
                    </div>
                    <VueDraggable v-model="rightTags" item-key="FieldName" handle=".drag-handle" @update="onRightTagDragUpdate"
                      class="draggable-list" :disabled="false" :fallback-class="'sortable-fallback'">
                      <div v-for="tag in rightTags" :key="tag.FieldName || ''" class="tag-item">
                        <el-checkbox :model-value="tag.IsEnabled" @click="updateRightTags(tag)" @click.stop>{{tag.FieldName==='IsSubscribeCourse'?'约课':transToConfigDescript(tag.DisplayName||'')}}</el-checkbox>
                        <el-icon class="drag-handle columns-item-drag-icon" title="拖拽排序">
                          <svg aria-hidden="true"><use xlink:href="#w2-tuodongpaixu"></use></svg>
                        </el-icon>
                        
                      </div>
                    </VueDraggable>
                  </div>
                </div>
                <div class="color-config" v-if="viewMode === 'color'" ref="colorConfigRef">
                  <div class="color-config-section">
                    <div class="color-config-section-header">
                      <div>背景颜色</div>
                    </div>
                    <div class="color-config-section-content">
                      <el-select v-model="backgroundColorByType" placeholder="请选择背景颜色" class="mb-8px">
                        <el-option v-for="item in backgroundColorByTypeOptions" :key="item.value" :label="transToConfigDescript(item.label)" :value="item.value" />
                      </el-select>
                      <template v-if="backgroundColorItems.length==0">
                        <div class="color-[#909399] line-height-[18px] text-[13px] mt-6px">
                          系统会给“同个{{transToConfigDescript(backgroundColorByType=='ByTeacher'?'任课老师':backgroundColorByType=='ByCourse'?'上课课程':backgroundColorByType=='ByClassroom'?'上课教室':'-')}}”随机分配同种颜色，以便达到
                          “用颜色”来区分{{transToConfigDescript(backgroundColorByType=='ByTeacher'?'任课老师':backgroundColorByType=='ByCourse'?'课程':backgroundColorByType=='ByClassroom'?'教室':'-')}}的目的。
                        </div>
                      </template>
                      <ColorSelector 
                        v-else
                        :items="backgroundColorItems" 
                        :color-type="'light'"
                        v-model="backgroundColors" 
                        @change="handleBackgroundColorChange"
                        @pick="handleBackgroundItemPick"
                      />
                    </div>
                  </div>
                  <div class="color-config-section">
                    <div class="color-config-section-header">
                      <div>标签颜色</div>
                    </div>
                    <div class="color-config-section-content">
                      <ColorSelector 
                        :items="tagColorItems" 
                        :color-type="'dark'"
                        v-model="tagColors" 
                        @change="handleTagColorChange" 
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="flex-between operation-btn-box">
            <el-checkbox v-model="setAllSame">同时将上方设置，应用到其他课表</el-checkbox>
            <el-button type="primary" @click="saveSettings">应用</el-button>
          </div>
        </div>

        <!-- 时刻范围 -->
        <div v-if="activeNav === 'time'" class="time-settings">
          <div class="time-settings-content-wrap">
            <div class="time-settings-content">
              <div class="mb-16px">设置所有课表时刻视图“开始至结束”的时间范围</div>
              <el-form ref="formRef" :model="form" label-position="top">
                <el-form-item label="开始时间">
                  <el-time-select
                    v-model="form.TimeViewStart"
                    style="width: 240px"
                    :max-time="form.TimeViewEnd=='00:00'?'23:59':form.TimeViewEnd"
                    start="00:00"
                    step="00:30"
                    end="24:00"
                    placeholder="请选择"
                    :clearable="false"
                  />
                </el-form-item>
                <el-form-item label="结束时间">
                  <el-time-select
                    v-model="form.TimeViewEnd"
                    style="width: 240px"
                    :min-time="form.TimeViewStart"
                    start="00:00"
                    step="00:30"
                    end="24:00"
                    placeholder="请选择"
                    :clearable="false"
                  />
                </el-form-item>
              </el-form>
              <PageAttentionTips class="mt-30px">
                <div class="text-12px">特别提示：</div>
              </PageAttentionTips>
              <div class="text-12px mt-6px color-[#909399] line-height-[20px]">1、若课节的时间超出了上方所设置的时间范围，则不会显示在课表，设置范围时须注意机构最早与最晚的{{transToConfigDescript('上课时间')}}。</div>
              <div class="text-12px mt-6px color-[#909399] line-height-[20px]">2、设置的效果仅会对你生效，不会影响其他人，请放心修改！</div>
            </div>
            <div class="flex-end operation-btn-box">
              <el-button type="primary" @click="saveSettings">应用</el-button>
            </div>
          </div>
          
        </div>
        <!-- 时刻高度 -->
        <div v-if="activeNav === 'height'" class="height-settings">
          <div class="height-settings-content-wrap">
            <div class="height-settings-content">
              <el-tabs v-model="currentTableTypeHeight" @tab-change="handlTableTypeHieghtChange">
                <el-tab-pane :name="CourseTimetableTypeEnum.TeacherTimetable">
                  <template #label>
                    <span class="tab-label">
                      {{transToConfigDescript('老师课表')}}
                      <span v-if="hasHeightTabChanges(CourseTimetableTypeEnum.TeacherTimetable)" class="tab-dot"></span>
                    </span>
                  </template>
                </el-tab-pane>
                <el-tab-pane :name="CourseTimetableTypeEnum.StudentTimetable">
                  <template #label>
                    <span class="tab-label">
                      1对1学员课表
                      <span v-if="hasHeightTabChanges(CourseTimetableTypeEnum.StudentTimetable)" class="tab-dot"></span>
                    </span>
                  </template>
                </el-tab-pane>
                <el-tab-pane :name="CourseTimetableTypeEnum.ClassTimetable">
                  <template #label>
                    <span class="tab-label">
                      {{transToConfigDescript('班级课表')}}
                      <span v-if="hasHeightTabChanges(CourseTimetableTypeEnum.ClassTimetable)" class="tab-dot"></span>
                    </span>
                  </template>
                </el-tab-pane>
                <el-tab-pane :name="CourseTimetableTypeEnum.ClassroomTimetable">
                  <template #label>
                    <span class="tab-label">
                      教室课表
                      <span v-if="hasHeightTabChanges(CourseTimetableTypeEnum.ClassroomTimetable)" class="tab-dot"></span>
                    </span>
                  </template>
                </el-tab-pane>
                <el-tab-pane :name="CourseTimetableTypeEnum.TimeTimetable">
                  <template #label>
                    <span class="tab-label">
                      {{transToConfigDescript('校区课表')}}
                      <span v-if="hasHeightTabChanges(CourseTimetableTypeEnum.TimeTimetable)" class="tab-dot"></span>
                    </span>
                  </template>
                </el-tab-pane>
              </el-tabs>
              <div class="schedule-view-height-set-tabs">
                <PageAttentionTips>设置“{{ transToConfigDescript(getHeightTabLabel(currentTableTypeHeight)) }}”时刻视图下，30分钟的刻度高度。设置的效果仅会对你生效，不会影响其他人，请放心修改！</PageAttentionTips>
                <el-form label-position="top" class="mt-10px">
                  <el-form-item label="30分钟刻度高度" class="mb-4px!">
                    <el-select v-model="heightType" placeholder="请选择">
                      <el-option label="低" :value="30"></el-option>
                      <el-option label="中" :value="90"></el-option>
                      <el-option label="高" :value="180"></el-option>
                    </el-select>
                  </el-form-item>
                  <div class="text-12px color-[#909399] line-height-[20px]">
                    {{heightType==30?'课表可以显示更多的课节数量，侧重于查看整体的排课情况。':
                      heightType==90?'课表显示的课节数量与课节详细信息适中，即可查看整体的排课情况，又兼顾具体课节的详细信息。':
                      '课表可以显示更多的详细信息，侧重于查看具体课节的详情。'}}
                  </div>
                </el-form>
                <div class="font-bold my-30px">高度示意</div>
                <div class="time-height-example-wrap">
                  <div class="time-top-line"><div class="time-top-line-text">09:00</div></div>
                  <div class="course-card" 
                    :style="{ 
                      borderTopColor: '#137DFF', 
                      backgroundColor: getTint('#137DFF', 0.1),
                      marginLeft: '30px',
                      height:`calc(${heightType}px ${heightType==90?'- 8px':heightType==30?'- 4px':'- 20px'})`,
                      overflow:'hidden'
                    }"
                  >
                    <div class="course-time">09:00-09:30</div>                  
                    <div class="course-meta">
                        <div class="meta-item ellipsis-single">阅读理解</div>
                        <div class="meta-item ellipsis-single">三年级语文提高班</div>
                        <div class="meta-item ellipsis-single">李四</div>
                        <div class="meta-item ellipsis-single">201</div>
                    </div>
                    <div class="course-tags">
                      <span class="course-tags-item ellipsis-single color-[#FF615C]!">{{transToConfigDescript('未上课')}}</span>
                      <span class="course-tags-item ellipsis-single color-[#FF7D00]!">约课</span>
                      <span class="course-tags-item ellipsis-single color-[#137DFF]!">集体班</span>
                    </div>
                  </div>
                  <div class="time-bottom-line"><div class="time-bottom-line-text">09:30</div></div>
                </div>
              </div>
            </div>
          </div>
          <div class="flex-between operation-btn-box">
            <el-checkbox v-model="setAllSameHeight">同时将上方设置，应用到其他课表</el-checkbox>
            <el-button type="primary" @click="saveSettings">应用</el-button>
          </div>
        </div>
        
      </div>
    </div>
  </el-drawer>

</template>

<script setup lang="ts">
import { ref, nextTick, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { VueDraggable } from 'vue-draggable-plus'
import { Search } from '@element-plus/icons-vue'
import ColorSelector from '../components/color-selector.vue'
import {
  saveTimetablePreference,
  getAllTimetablePreference,
  saveTimetableTimeRangePreference,
  saveTimetableRowHeightPreference
} from '@/api/arrange'
import {
  CourseTimetableTypeEnum,
  CourseTimetableFieldTypeEnum,
  CourseTimetableFieldSetting,
  SaveTimetablePreference_ReqFormModel,
  SaveTimetableColorPreference_ReqFormModel,
  CourseTimetableColorSetting,
  CourseTimetableColorSettingTypeEnum,
  CourseTimetableTagColorSetting
} from '@/types/model/timetable-preference'
import { getTint } from '@/utils'
import { cloneDeep } from 'lodash'
import { useTimetablePreferences } from '@/store/timetablePreferences'
import {
  DEFAULT_TIME_RANGE_START,
  DEFAULT_TIME_RANGE_END,
  DEFAULT_LEFT_FIELD_KEYS,
  DEFAULT_RIGHT_TAG_CANDIDATES,
  DEFAULT_ENABLED_TAG_KEYS,
  ALL_COLUMNS
} from '@/constants/timetablePreferencesDefaults'
import PageAttentionTips from '@/components/common/page-attention-tips/pageAttentionTips.vue'
import { useConfigs } from '@/store'
import { transToConfigDescript } from '@/utils/filters/filters'

const configs = computed(() => {
	return useConfigs().configs
})
const ShowClassProgressBar=computed(()=>{//在课表上是否显示动态计算出来的上课进度（第几次课）：1显示，0不显示（默认）2小银星定制（在补课及请假管理界面，给学员安排跟班补课或请假时，如果开启了此配置，则可以显示目标补课班级和请假班级的上课进度。）
	return configs.value.ShowClassProgressBar==1||configs.value.ShowClassProgressBar==2
})

// 抽屉控制
const drawer = ref(false)
const loading = ref(false)
// 初始化阶段标记，避免初始化赋值被标记为未保存更改
const isInitializing = ref(false)
const colorConfigRef = ref<HTMLElement | null>(null)
// 切换课表类型阶段标记，避免切换时同步数据被标记为未保存更改
const isSyncingTab = ref(false)
const timetablePrefStore = useTimetablePreferences()
const allColumns=[{
	FieldName:'ShiftName',
	DisplayName:'上课课程',
	ColumnKey:'ShiftName',
},{
	FieldName:'ClassName',
	DisplayName:'上课班级/学员',
	ColumnKey:'ClassName,StudentName',
},{
	FieldName:'IsOneToOne',
	DisplayName:'教学形式',
	ColumnKey:'IsOneToOne',
  Tips:'课程管理中的“课程类型”'
},{
	FieldName:'CampusName',
	DisplayName:'上课校区',
	ColumnKey:'CampusName'
},
// {
// 	FieldName:'StartTime',
// 	DisplayName:'上课时间',
// 	ColumnKey:'StartTime,EndTime'
// },
{
	FieldName:'Duration',
	DisplayName:'上课时长',
	ColumnKey:'Duration'
},{
	FieldName:'Finished',
	DisplayName:'上课状态',
	ColumnKey:'FinishedName'
},{
	FieldName:'StudentAttendanceCount',
	DisplayName:'实到/应到',
	ColumnKey:'StudentAttendanceCount,StudentCount'
},{
	FieldName:'ClassPlanNum_ClassPlanCount',
	DisplayName:'上课进度',
	ColumnKey:'ClassPlanNum_ClassPlanCount',
	Tips:``
},{
	FieldName:'ChapterTitle',
	DisplayName:'章节内容',
	ColumnKey:'ChapterTitle'
},{
	FieldName:'CourseContent',
	DisplayName:'上课内容',
	ColumnKey:'CourseContent'
},{
	FieldName:'SubjectName',
	DisplayName:'上课科目',
	ColumnKey:'SubjectName',
	Tips:transToConfigDescript(`全科课程的上课科目`)
},{
	FieldName:'CourseType',
	DisplayName:'上课方式',
	ColumnKey:'CourseType'
},{
	FieldName:'ClassroomName',
	DisplayName:'上课教室',
	ColumnKey:'ClassroomName'
},{
	FieldName:'TeacherName',
	DisplayName:'任课老师',
	ColumnKey:'TeacherName'
},{
	FieldName:'AssistantTeacherName',
	DisplayName:'助教',
	ColumnKey:'AssistantTeacherName'
},{
	FieldName:'MasterName',
	DisplayName:'学管师',
	ColumnKey:'MasterName'
},{
	FieldName:'HeadMasterUserName',
	DisplayName:'班主任',
	ColumnKey:'HeadMasterUserName'
},{
	FieldName:'IsSubscribeCourse',
	DisplayName:'开放预约',
	ColumnKey:'IsSubscribeCourse',
	Tips:`已开放的排课，支持学员在手机端预约。`
},{
	FieldName:'StartStudentCount',
	DisplayName:'开课人数',
	ColumnKey:'StartStudentCount',
	Tips:`1、只有预约课，才会有开课人数；<br/>2、已约人数达到“开课人数”后，状态变为“已开课”`
},{
	FieldName:'CourseStatus',
	DisplayName:'开课状态',
	ColumnKey:'CourseStatus',
	Tips:`1、预约课的“已约人数”需要达到“开课人数”，会转为“已开课”状态；<br/>2、若设置了自动取消排课，则“未开课”的排课会根据设置的时间，自动取消排课；`
},{
	FieldName:'InternalRemark',
	DisplayName:'对内备注',
	ColumnKey:'InternalRemark'
}
// ,{
// 	FieldName:'Describe',
// 	DisplayName:'对外备注',
// 	ColumnKey:'Describe'
// }
]
// 如果关闭上课进度显示，则从列定义中移除“上课进度”字段
if(!ShowClassProgressBar.value){
  const progressIdx = (allColumns as any[]).findIndex(c=>c.FieldName==='ClassPlanNum_ClassPlanCount')
  if(progressIdx>-1){
    ;(allColumns as any[]).splice(progressIdx,1)
  }
}
const leftInfoFields=ref(cloneDeep(allColumns))
// 示例卡片左侧基础信息的默认预览文案
const defaultLeftPreviewMap: Record<string, string> = {
  ShiftName: '阅读理解',
  ClassName: '三年级语文提高班',
  StudentName: '韩梅梅',
  IsOneToOne: '集体班',
  CampusName: '一号校区',
  Duration: '30分钟',
  Finished: transToConfigDescript('未上课'),
  StudentAttendanceCount: '28/30',
  ClassPlanNum_ClassPlanCount: '10/20',
  ChapterTitle: '第二单元·阅读策略',
  CourseContent: '课文精讲与练习',
  SubjectName: '语文',
  CourseType: '线下课',
  ClassroomName: '201',
  TeacherName: '李四',
  AssistantTeacherName: '王五',
  MasterName: '赵六',
  HeadMasterUserName: '张三',
  IsSubscribeCourse: '不开放预约',
  StartStudentCount: '8人',
  CourseStatus: '已开课',
  InternalRemark: '内部复盘关注互动',
  Describe: '欢迎家长旁听'
}

function getLeftFieldText(fieldName?: string | null) {
  if (!fieldName) return ''
  
  // 动态响应颜色面板点选的字段
  if (fieldName === 'Finished') {
    // 上课状态
    let label = '未上课'
    if (backgroundColorByType.value === 'ByCourseFinished') {
      const pickedKey = lastChangedKeyByType.value['ByCourseFinished']
      const items = backgroundColorItems.value
      const found = items.find(i => String(i.key) === String(pickedKey))
      label = found?.label || '未上课'
    }
    return transToConfigDescript(label)
  }
  
  if (fieldName === 'IsOneToOne') {
    // 教学形式
    let label = '集体班'
    if (backgroundColorByType.value === 'ByTeachingMethod') {
      const pickedKey = lastChangedKeyByType.value['ByTeachingMethod']
      const items = backgroundColorItems.value
      const found = items.find(i => String(i.key) === String(pickedKey))
      label = found?.label || '集体班'
    }
    return label
  }
  
  if (fieldName === 'CourseStatus') {
    // 开课状态
    let label = '已开课'
    if (backgroundColorByType.value === 'ByOpeningStatus') {
      const pickedKey = lastChangedKeyByType.value['ByOpeningStatus']
      const items = backgroundColorItems.value
      const found = items.find(i => String(i.key) === String(pickedKey))
      label = found?.label || '已开课'
    }
    return label
  }
  
  // 组合字段特殊处理
  if (fieldName === 'StudentAttendanceCount') return defaultLeftPreviewMap['StudentAttendanceCount']
  if (fieldName === 'ClassPlanNum_ClassPlanCount') return defaultLeftPreviewMap['ClassPlanNum_ClassPlanCount']
  return defaultLeftPreviewMap[fieldName] || ''
}
// 右侧标签候选项固定顺序
const RIGHT_TAG_ORDER = [
  'Finished',          // 上课状态
  'IsSubscribeCourse', // 约课
  'IsOneToOne',        // 教学形式
  'TeacherName',       // 任课老师
  'ClassroomName',     // 上课教室
  'ClassPlanNum_ClassPlanCount',       // 上课进度
  'CourseType',        // 上课方式
  'CourseStatus',    // 开课状态
]
const rightTagCandidates = computed(() => {
  return RIGHT_TAG_ORDER
    .map((fieldName:string) => (allColumns as any[]).find(c => c.FieldName === fieldName))
    .filter(Boolean)
    .map((c:any, idx:number) => ({
      FieldName: c.FieldName,
      DisplayName: c.DisplayName,
      IsEnabled: false,
      IsDefault: false,
      SortOrder: idx + 1,
      FieldType: CourseTimetableFieldTypeEnum.Tag,
    }))
})
const calWidthNum=computed(()=>{
  return rightTags.value.filter((c:any)=>c.IsEnabled).length
})

const backgroundColorByTypeOptions=ref([{
  value: 'ByCourseFinished',
  label: '按上课状态'
},{
  value: 'ByTeachingMethod',
  label: '按教学形式'
},{
  value: 'ByTeacher',
  label: '按任课老师'
},{
  value: 'ByCourse',
  label: '按上课课程'
},{
  value: 'ByOpeningStatus',
  label: '按开课状态'
},{
  value: 'ByClassroom',
  label: '按上课教室'
}])
const DEFAULT_BACKGROUND_COLORS = [{
  SettingType: 'ByCourseFinished',
  IsEnabled: true,
  ColorDetails: [{
    ColorValue: '#4893FF',
    EnumValue: 0,
    DisplayName: '未上课'
  },{
    ColorValue: '#67C23A',
    EnumValue: 1,
    DisplayName: '已上课'
  },{
    ColorValue: '#909399',
    EnumValue: 2,
    DisplayName: '已取消'
  }]
},{
  SettingType: 'ByTeachingMethod',
  IsEnabled: false,
  ColorDetails: [{
    ColorValue: '#4893FF',
    EnumValue: 0,
    DisplayName: '集体班'
  },{
    ColorValue: '#67C23A',
    EnumValue: 1,
    DisplayName: '一对一'
  },{
    ColorValue: '#909399',
    EnumValue: 2,
    DisplayName: '一对多'
  }]
},{
  SettingType: 'ByTeacher',
  IsEnabled: false,
  ColorDetails: []
},{
  SettingType: 'ByCourse',
  IsEnabled: false,
  ColorDetails: []
},{
  SettingType: 'ByOpeningStatus',
  IsEnabled: false,
  ColorDetails: [{
    ColorValue: '#4893FF',
    EnumValue: 1,
    DisplayName: '已开课'
  },{
    ColorValue: '#FFC01E',
    EnumValue: 0,
    DisplayName: '未开课'
  }]
},{
  SettingType: 'ByClassroom',
  IsEnabled: false,
  ColorDetails: []
}]
// 标签默认颜色，key 与 RIGHT_TAG_ORDER 的字段名一致
const DEFAULT_TAG_COLORS: Record<string, string> = {
  Finished: '#909399',          // 上课状态
  IsSubscribeCourse: '#909399', // 约课
  IsOneToOne: '#909399',        // 教学形式
  TeacherName: '#909399',       // 任课老师
  ClassroomName: '#909399',     // 上课教室
  ClassPlanNum_ClassPlanCount: '#909399',       // 上课进度
  CourseType: '#909399',         // 上课方式
  CourseStatus: '#909399'         // 开课状态
}
// 颜色选择器数据 - 这些变量现在通过computed属性管理

// 背景颜色项配置 - 优先使用服务端 ColorDetails，其次用默认
const backgroundColorItems = computed(() => {
  const currentType = backgroundColorByType.value
  const serverDetails = (colorDetailsByType.value || {})[currentType]
  if (Array.isArray(serverDetails) && serverDetails.length > 0) {
    return serverDetails.map((detail:any) => ({
      key: detail.EnumValue != null ? detail.EnumValue.toString() : '',
      label: detail.DisplayName,
      color: detail.ColorValue
    }))
  }
  const defaultConfig = DEFAULT_BACKGROUND_COLORS.find(config => config.SettingType === currentType)
  
  if (defaultConfig && defaultConfig.ColorDetails) {
    return defaultConfig.ColorDetails.map(detail => ({
      key: detail.EnumValue.toString(),
      label: detail.DisplayName,
      color: detail.ColorValue
    }))
  }
  
  // 如果找不到对应配置，返回默认的按上课状态配置
  return [
    { key: '0', label: '未上课', color: '#4893FF' },
    { key: '1', label: '已上课', color: '#67C23A' },
    { key: '2', label: '已取消', color: '#909399' }
  ]
})

// 标签颜色项配置：来源于已启用的 rightTags
const tagColorItems = computed(() => {
  const enabledTags = (rightTags.value || []).filter((t:any) => t.IsEnabled)
  return enabledTags.map((t:any) => ({
    key: t.FieldName,
    label: t.FieldName === 'IsSubscribeCourse' ? '约课' : t.DisplayName,
    // 优先使用服务端/当前会话的颜色；没有则回退到默认
    color: (tagColors.value || {})[t.FieldName] || DEFAULT_TAG_COLORS[t.FieldName] || '#409EFF'
  }))
})
// 右侧实际展示的标签（最多3个）
const enabledRightTags = computed(() => {
  return (rightTags.value || []).filter((t:any) => t.IsEnabled).slice(0, 3)
})
// 左侧字段搜索
const filteredLeftInfoFields = computed(() => {
  const keyword = (searchKeyword.value || '').trim().toLowerCase()
  const leftSet = new Set((leftFields.value || []).map((f:any) => transToConfigDescript(f.FieldName)))
  const candidates = (leftInfoFields.value || []).filter((f:any) => !leftSet.has(f.FieldName))
  if (!keyword) return candidates
  return candidates.filter((f:any) => {
    const nameMatch = (transToConfigDescript(f.DisplayName || '')).toLowerCase().includes(keyword)
    const selectedInPopover = selectedFields.value.some((s:any) => s.DisplayName === f.DisplayName)
    return nameMatch || selectedInPopover
  })
})
// 导航相关
const activeNav = ref('info')
const navItems = [
  { key: 'info', label: '信息与颜色'},
  { key: 'time', label: '时刻开始/结束'},
  { key: 'height', label: '时刻高度'}
]
const viewMode = ref('info')
const changeViewMode = (mode: string) => {
  viewMode.value = mode
}
// 课表类型/*  */
const currentTimetableType = ref<CourseTimetableTypeEnum>(CourseTimetableTypeEnum.TeacherTimetable)

// 每个tab的独立数据存储
interface TabData {
  leftFields: CourseTimetableFieldSetting[]
  rightTags: CourseTimetableFieldSetting[]
  backgroundColorByType: string
  backgroundColors: Record<string, string>
  tagColors: Record<string, string>
  colorDetailsByType: Record<string, any[]>
  lastChangedKeyByType: Record<string, string | undefined>
  hasChanges: boolean // 是否有未保存的变更
  isLoaded: boolean // 是否已从后台加载过数据
}

const tabDataMap = ref<Record<string, TabData>>({})

// 当前tab的数据
const currentTabData = computed(() => {
  const tabKey = currentTimetableType.value.toString()
  if (!tabDataMap.value[tabKey]) {
    tabDataMap.value[tabKey] = {
      leftFields: [],
      rightTags: [],
      backgroundColorByType: 'ByCourseFinished',
      backgroundColors: {},
      tagColors: {},
      colorDetailsByType: {},
      lastChangedKeyByType: {},
      hasChanges: false,
      isLoaded: false
    }
  }
  return tabDataMap.value[tabKey]
})

// 左边信息字段 - 动态加载
const leftFields = computed({
  get: () => currentTabData.value.leftFields,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].leftFields = value
      if (!isInitializing.value && !isSyncingTab.value) {
        tabDataMap.value[tabKey].hasChanges = true
      }
    }
  }
})

// 右边标签字段 - 动态加载
const rightTags = computed({
  get: () => currentTabData.value.rightTags,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].rightTags = value
      if (!isInitializing.value && !isSyncingTab.value) {
        tabDataMap.value[tabKey].hasChanges = true
      }
    }
  }
})

// 背景颜色类型
const backgroundColorByType = computed({
  get: () => currentTabData.value.backgroundColorByType,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].backgroundColorByType = value
      if (!isInitializing.value && !isSyncingTab.value) {
        tabDataMap.value[tabKey].hasChanges = true
      }
    }
  }
})

// 背景颜色
const backgroundColors = computed({
  get: () => currentTabData.value.backgroundColors,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].backgroundColors = value
      if (!isInitializing.value && !isSyncingTab.value) {
        tabDataMap.value[tabKey].hasChanges = true
      }
    }
  }
})

// 标签颜色
const tagColors = computed({
  get: () => currentTabData.value.tagColors,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].tagColors = value
      if (!isInitializing.value && !isSyncingTab.value) {
        tabDataMap.value[tabKey].hasChanges = true
      }
    }
  }
})

// 颜色详情缓存
const colorDetailsByType = computed({
  get: () => currentTabData.value.colorDetailsByType,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].colorDetailsByType = value
    }
  }
})

// 最后变更的键
const lastChangedKeyByType = computed({
  get: () => currentTabData.value.lastChangedKeyByType,
  set: (value) => {
    const tabKey = currentTimetableType.value.toString()
    if (tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey].lastChangedKeyByType = value
    }
  }
})

// 搜索关键词
const searchKeyword = ref('')

// 选中的字段
const selectedFields = ref<CourseTimetableFieldSetting[]>([])

// 标记tab有变更
const markTabAsChanged = () => {
  currentTabData.value.hasChanges = true
}

// 清除tab变更标记
const clearTabChanges = () => {
  const tabKey = currentTimetableType.value.toString()
  if (tabDataMap.value[tabKey]) {
    tabDataMap.value[tabKey].hasChanges = false
  }
}

// 检查tab是否有变更
const hasTabChanges = (tabType: CourseTimetableTypeEnum) => {
  const tabKey = tabType.toString()
  const hasChanges = tabDataMap.value[tabKey]?.hasChanges || false
  return hasChanges
}

// 切换导航
const switchNav = (navKey: string) => {
  activeNav.value = navKey
}

// 切换课表类型
const handleTimetableTypeChange = (type: string | number) => {
  // 切换tab时暂时屏蔽更改标记
  isSyncingTab.value = true
  currentTimetableType.value = type as CourseTimetableTypeEnum
  // 使用微任务/下一拍恢复，避免同步触发的setter打点
  nextTick(() => {
    isSyncingTab.value = false
  })
  // 所有数据已经一次性加载完成，不需要再次加载
}

// 移除左边字段（不区分默认项）
const removeLeftField = (field: CourseTimetableFieldSetting) => {
  const index = leftFields.value.findIndex(f => f.FieldName === field.FieldName)
  if (index > -1) {
    const newFields = [...leftFields.value]
    newFields.splice(index, 1)
    leftFields.value = newFields
  }
}

// 左边字段拖拽结束（在完整列表上排序）
const onLeftFieldDragUpdate = (evt: any) => {
  console.log('onLeftFieldDragUpdate', evt);
  console.log('拖拽前的 leftFields:', leftFields.value);

  // 更新排序并触发computed setter
  const newFields = leftFields.value.map((field, index) => ({
    ...field,
    SortOrder: index + 1
  }))
  leftFields.value = newFields
}

// 右边标签拖拽结束
const onRightTagDragUpdate = (evt: any) => {
  console.log('onRightTagDragUpdate', evt);
  // 更新排序并触发computed setter
  const newTags = rightTags.value.map((tag, index) => ({
    ...tag,
    SortOrder: leftFields.value.length + index + 1
  }))
  rightTags.value = newTags
}

// 更新右边标签
const updateRightTags = (item:any) => {
  // 检查是否超过3个选中的标签
  const enabledCount = rightTags.value.filter(tag => tag.IsEnabled).length
  if (enabledCount == 3&&!item.IsEnabled) {
    ElMessage.warning('最多只能选择3个标签')
  }else{
    // 创建新的数组来触发computed setter
    const newTags = rightTags.value.map(tag => 
      tag.FieldName === item.FieldName 
        ? { ...tag, IsEnabled: !tag.IsEnabled }
        : tag
    )
    rightTags.value = newTags
  }
}

// 初始化所有tab的数据
const initializeAllTabs = () => {
  // 获取所有课表类型
  const allTimetableTypes = [
    CourseTimetableTypeEnum.TeacherTimetable,
    CourseTimetableTypeEnum.StudentTimetable,
    CourseTimetableTypeEnum.ClassTimetable,
    CourseTimetableTypeEnum.ClassroomTimetable,
    CourseTimetableTypeEnum.TimeTimetable
  ]

  // 为每个课表类型初始化默认数据
  allTimetableTypes.forEach(type => {
    const tabKey = type.toString()
    if (!tabDataMap.value[tabKey]) {
      tabDataMap.value[tabKey] = {
        leftFields: [],
        rightTags: [],
        backgroundColorByType: 'ByCourseFinished',
        backgroundColors: {},
        tagColors: {},
        colorDetailsByType: {},
        lastChangedKeyByType: {},
        hasChanges: false,
        isLoaded: false
      }
    }
  })
}
// 并行加载所有课表类型的颜色设置
const allTimetableTypes = [
  CourseTimetableTypeEnum.TeacherTimetable,
  CourseTimetableTypeEnum.StudentTimetable,
  CourseTimetableTypeEnum.ClassTimetable,
  CourseTimetableTypeEnum.ClassroomTimetable,
  CourseTimetableTypeEnum.TimeTimetable
]
// 加载设置
const loadSettings = async () => {
  try {
    loading.value = true

    // 先初始化所有tab的数据
    initializeAllTabs()

    // 加载信息设置
    const infoResponse = await getAllTimetablePreference()
    if (infoResponse.ErrorCode === 200 && infoResponse.Data) {
      const list = infoResponse.Data
      if(list.length > 0){
        form.value.TimeViewStart = list[0]?.TimeViewStart || DEFAULT_TIME_RANGE_START
        form.value.TimeViewEnd = list[0]?.TimeViewEnd || DEFAULT_TIME_RANGE_END
      } else {
        form.value.TimeViewStart = DEFAULT_TIME_RANGE_START
        form.value.TimeViewEnd = DEFAULT_TIME_RANGE_END
      }
      list.forEach((data:any) => {
        const tabKey = data.TimetableType.toString()
        const settings = data.CardShowInformationSettings
        if (Array.isArray(settings) && settings.length > 0) {

          // 分离左边信息和右边标签，只显示已启用的字段
          let mainFields = settings.filter((f:any) =>
            f.FieldType === CourseTimetableFieldTypeEnum.Main && f.IsEnabled && !f.IsDefault
          )
          let tagFields = settings.filter((f:any) =>
            f.FieldType === CourseTimetableFieldTypeEnum.Tag
          )
          // 如果后端未返回标签或为空，则使用内建候选生成默认标签（均为未启用）
          if (!tagFields || tagFields.length === 0) {
            tagFields = rightTagCandidates.value
          } else {
            // 容错：对比 DEFAULT_RIGHT_TAG_CANDIDATES，补充缺失的字段，保留后端返回的字段
            const defaultFieldNames = new Set(DEFAULT_RIGHT_TAG_CANDIDATES.map((t:any) => t.FieldName))
            const existingFieldNames = new Set(tagFields.map((f:any) => f.FieldName))
            
            // 找出缺失的字段（在DEFAULT中但不在后端返回中的）
            const missingFields = DEFAULT_RIGHT_TAG_CANDIDATES.filter((t:any) => !existingFieldNames.has(t.FieldName))
            
            // 找出多出来的字段（在后端返回中但不在DEFAULT中的）
            const extraFields = tagFields.filter((f:any) => !defaultFieldNames.has(f.FieldName))
            
            // 如果有缺失或多余的字段，进行合并处理
            if (missingFields.length > 0 || extraFields.length > 0) {
              // 从 allColumns 中查找缺失字段的完整信息，生成默认标签对象
              const missingTagFields = missingFields.map((t:any) => {
                const column = (allColumns as any[]).find(c => c.FieldName === t.FieldName)
                return {
                  FieldName: t.FieldName,
                  DisplayName: column?.DisplayName || t.DisplayName,
                  IsEnabled: false,
                  IsDefault: false,
                  SortOrder: tagFields.length + missingFields.indexOf(t) + 1,
                  FieldType: CourseTimetableFieldTypeEnum.Tag,
                }
              })
              
              // 合并：保留后端返回的字段（包括多出来的）+ 补充缺失的字段
              tagFields = [...tagFields, ...missingTagFields]
            }
          }
          // ShowClassProgressBar 为 false 时，过滤掉“上课进度”字段
          if (!ShowClassProgressBar.value) {
            mainFields = mainFields.filter((f:any) => f.FieldName !== 'ClassPlanNum_ClassPlanCount')
            tagFields = tagFields.filter((f:any) => f.FieldName !== 'ClassPlanNum_ClassPlanCount')
          }
          // 按排序字段排序
          tabDataMap.value[tabKey].leftFields = mainFields.sort((a:any, b:any) => (a.SortOrder || 0) - (b.SortOrder || 0))
          tabDataMap.value[tabKey].rightTags = tagFields.sort((a:any, b:any) => (a.SortOrder || 0) - (b.SortOrder || 0))
        } else {
          // CardShowInformationSettings 为空时的默认处理
          let defaultLeftFields = (DEFAULT_LEFT_FIELD_KEYS as readonly string[])
            .map((k, index) => (allColumns as any[]).find(c => c.FieldName === k))
            .filter(Boolean)
            .map((c:any, index:number) => ({
              FieldName: c.FieldName,
              DisplayName: c.DisplayName,
              SortOrder: index + 1,
            })) as any

          const enabledTagKeys = new Set(DEFAULT_ENABLED_TAG_KEYS as readonly string[])
          let defaultRightTags = (DEFAULT_RIGHT_TAG_CANDIDATES as readonly any[]).map((t:any, index:number) => ({
            ...t,
            IsEnabled: enabledTagKeys.has(t.FieldName),
            SortOrder: defaultLeftFields.length + index + 1,
          }))
          // ShowClassProgressBar 为 false 时，过滤掉“上课进度”字段
          if (!ShowClassProgressBar.value) {
            defaultLeftFields = defaultLeftFields.filter((c:any) => c.FieldName !== 'ClassPlanNum_ClassPlanCount')
            defaultRightTags = defaultRightTags.filter((t:any) => t.FieldName !== 'ClassPlanNum_ClassPlanCount')
          }
          
          tabDataMap.value[tabKey].leftFields = defaultLeftFields
          tabDataMap.value[tabKey].rightTags = defaultRightTags
        }


        const serverColorSettings = Array.isArray(data.ColorSettings) ? data.ColorSettings : []
        const serverTagColorSettings = Array.isArray(data.TagColorSettings) ? data.TagColorSettings : []

        // 背景颜色设置：优先使用启用的配置，否则取第一条；为空则使用默认值
        if (serverColorSettings.length > 0) {
          const enabledSetting = serverColorSettings.find((s:any) => s && s.IsEnabled)
          const pickedSetting = enabledSetting || serverColorSettings[0]
          if (pickedSetting && pickedSetting.SettingType) {
            tabDataMap.value[tabKey].backgroundColorByType = pickedSetting.SettingType
            if (Array.isArray(pickedSetting.ColorDetails) && pickedSetting.ColorDetails.length > 0) {
              // 缓存该类型下的服务端明细
              tabDataMap.value[tabKey].colorDetailsByType[pickedSetting.SettingType] = pickedSetting.ColorDetails
              const map: Record<string, string> = {}
              pickedSetting.ColorDetails.forEach((d:any) => {
                if (d && d.EnumValue !== null && d.EnumValue !== undefined && d.ColorValue) {
                  map[String(d.EnumValue)] = d.ColorValue
                }
              })
              tabDataMap.value[tabKey].backgroundColors = map
            } else {
              // 确保无明细时清空缓存，回退到默认
              tabDataMap.value[tabKey].colorDetailsByType[pickedSetting.SettingType] = []
              // 对于 ByTeacher/ByCourse/ByClassroom 等无明细的情况，使用默认项
              const defaultConfig = DEFAULT_BACKGROUND_COLORS.find(config => config.SettingType === pickedSetting.SettingType)
              if (defaultConfig && defaultConfig.ColorDetails) {
                const map: Record<string, string> = {}
                defaultConfig.ColorDetails.forEach(detail => {
                  map[detail.EnumValue.toString()] = detail.ColorValue
                })
                tabDataMap.value[tabKey].backgroundColors = map
              }
            }
          }
        } else {
          // 服务端为空，用默认按上课状态
          tabDataMap.value[tabKey].backgroundColorByType = 'ByCourseFinished'
          tabDataMap.value[tabKey].colorDetailsByType = {}
          const defaultConfig = DEFAULT_BACKGROUND_COLORS.find(config => config.SettingType === 'ByCourseFinished')
          if (defaultConfig && defaultConfig.ColorDetails) {
            const map: Record<string, string> = {}
            defaultConfig.ColorDetails.forEach(detail => {
              map[detail.EnumValue.toString()] = detail.ColorValue
            })
            tabDataMap.value[tabKey].backgroundColors = map
          }
        }

        // 标签颜色设置：为空则使用默认值
        if (serverTagColorSettings.length > 0) {
          const tagMap: Record<string, string> = {}
          serverTagColorSettings.forEach((t:any) => {
            if (t && t.TagFieldName && t.ColorValue) {
              tagMap[t.TagFieldName] = t.ColorValue
            }
          })
          tabDataMap.value[tabKey].tagColors = tagMap
          // 自动启用接口返回的标签
          const enabledTagNames = new Set(Object.keys(tagMap))
          if (tabDataMap.value[tabKey].rightTags && tabDataMap.value[tabKey].rightTags.length > 0) {
            tabDataMap.value[tabKey].rightTags = tabDataMap.value[tabKey].rightTags.map((tag:any) => ({
              ...tag,
              IsEnabled: enabledTagNames.has(tag.FieldName) ? true : tag.IsEnabled
            }))
          }
        } else {
          // 直接使用内置默认
          tabDataMap.value[tabKey].tagColors = { ...DEFAULT_TAG_COLORS }
        }

        // 行高设置：从后端数据初始化各tab的行高
        try {
          const serverRowHeight = Number((data as any).RowHeight ?? (data as any).TimeViewRowHeight ?? DEFAULT_ROW_HEIGHT)
          const rowHeight = isNaN(serverRowHeight) ? DEFAULT_ROW_HEIGHT : serverRowHeight
          if (!heightStateMap.value[tabKey]) {
            heightStateMap.value[tabKey] = {
              value: rowHeight,
              savedValue: rowHeight,
              hasChanges: false,
              isLoaded: true
            }
          } else {
            heightStateMap.value[tabKey].value = rowHeight
            heightStateMap.value[tabKey].savedValue = rowHeight
            heightStateMap.value[tabKey].hasChanges = false
            heightStateMap.value[tabKey].isLoaded = true
          }
          // 同步当前展示tab的高度
          if (currentTableTypeHeight.value.toString() === tabKey) {
            heightType.value = rowHeight
          }
        } catch {}
      })
      
    }

    // 标记所有tab已加载
    Object.keys(tabDataMap.value).forEach(tabKey => {
      tabDataMap.value[tabKey].isLoaded = true
    })

  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.error('加载设置失败')

  } finally {
    loading.value = false
  }
}
const setAllSame = ref(false)
const setAllSameHeight=ref(false)
// 保存设置
const saveSettings = async () => {
  if(activeNav.value === 'info') {
    try {
      loading.value = true
      const applyAllInfo = setAllSame.value === true
      if (applyAllInfo) {
        // 保存到所有课表类型（并行）
        const saveInfoPromises = saveInfoSettingsForType()
        
        const [infoResults] = await Promise.all([
          Promise.allSettled([saveInfoPromises])
        ])
        const infoAllOk = infoResults.every(r => r.status === 'fulfilled')
        if (!infoAllOk) {
          const parts: string[] = []
          if (!infoAllOk) parts.push('信息设置、颜色设置')
          throw new Error(`${parts.join('、')}应用失败`)
        }
        // 同步本地所有tab为当前tab的设置
        const currentTabKey = currentTimetableType.value.toString()
        const currentTabSnapshot = cloneDeep(tabDataMap.value[currentTabKey])
        allTimetableTypes.forEach(type => {
          const key = type.toString()
          if (!tabDataMap.value[key]) return
          tabDataMap.value[key].leftFields = cloneDeep(currentTabSnapshot.leftFields)
          tabDataMap.value[key].rightTags = cloneDeep(currentTabSnapshot.rightTags)
          tabDataMap.value[key].backgroundColorByType = currentTabSnapshot.backgroundColorByType
          tabDataMap.value[key].backgroundColors = cloneDeep(currentTabSnapshot.backgroundColors)
          tabDataMap.value[key].tagColors = cloneDeep(currentTabSnapshot.tagColors)
          tabDataMap.value[key].colorDetailsByType = cloneDeep(currentTabSnapshot.colorDetailsByType)
          tabDataMap.value[key].lastChangedKeyByType = cloneDeep(currentTabSnapshot.lastChangedKeyByType)
          tabDataMap.value[key].hasChanges = false
        })
        ElMessage.success('应用成功')
        // 更新全局偏好缓存
        await timetablePrefStore.refreshAll()
      } else {
        // 并行执行两个保存函数（仅当前tab）
        const [infoResult] = await Promise.allSettled([
          saveInfoSettings()
        ])
        const infoSuccess = infoResult.status === 'fulfilled'
        if (infoSuccess) {
          ElMessage.success('应用成功')
          clearTabChanges()
          // 更新全局偏好缓存（当前类型）
          await timetablePrefStore.refreshAll()
        } else {
          const errors = []
          if (!infoSuccess) errors.push('信息设置、颜色设置')
          ElMessage.error(`${errors.join('、')}应用失败`)
        }
      }
      drawer.value=false
    } catch (error) {
      console.error('保存设置失败:', error)
      ElMessage.error('保存设置失败')
    } finally {
      loading.value = false
    }
  }else if(activeNav.value === 'time'){
    try {
      loading.value = true
      await saveTimetableTimeRangePreference({
        TimeViewStart: form.value.TimeViewStart,
        TimeViewEnd: form.value.TimeViewEnd
      })
      // 时间范围影响 getAllTimetablePreference 的 TimeViewStart/End，刷新缓存
      await timetablePrefStore.refreshAll()
      ElMessage.success('应用成功')
      drawer.value=false
    } catch (error) {
      console.error('保存设置失败:', error)
      ElMessage.error('保存设置失败')
    } finally {
      loading.value = false
    }
  } else if (activeNav.value === 'height') {
    try {
      loading.value = true
      const applyAll = setAllSameHeight.value === true
      if (applyAll) {
        // 仅调用一次后端接口，设置为应用到所有课表类型
        const resp = await saveTimetableRowHeightPreference({
          RowHeight: heightType.value,
          TimetableType: currentTableTypeHeight.value,
          ApplyToAllTypes: true
        })
        if (resp.ErrorCode !== 200) throw new Error(resp.ErrorMsg || '行高保存失败')
        // 本地同步所有tab的行高，保持一致
        for (const type of allHeightTimetableTypes) {
          const key = type.toString()
          if (!heightStateMap.value[key]) continue
          heightStateMap.value[key].savedValue = heightType.value
          heightStateMap.value[key].value = heightType.value
          heightStateMap.value[key].hasChanges = false
        }
      } else {
        const resp = await saveTimetableRowHeightPreference({
          RowHeight: heightType.value,
          TimetableType: currentTableTypeHeight.value,
          ApplyToAllTypes: false
        })
        if (resp.ErrorCode !== 200) throw new Error(resp.ErrorMsg || '行高保存失败')
        const key = currentTableTypeHeight.value.toString()
        if (heightStateMap.value[key]) {
          heightStateMap.value[key].savedValue = heightType.value
          heightStateMap.value[key].value = heightType.value
          heightStateMap.value[key].hasChanges = false
        }
      }
      // 更新全局偏好缓存
      await timetablePrefStore.refreshAll()
      ElMessage.success('应用成功')
      drawer.value=false
    } catch (error) {
      console.error('保存设置失败:', error)
      ElMessage.error('保存设置失败')
    } finally {
      loading.value = false
    }
  }
}

async function saveInfoSettings() {
  let leftList=[{
    FieldName:'StartTime',
    DisplayName:'上课时间',
    IsEnabled:true,
    SortOrder:0,
    IsDefault:true,
    FieldType:CourseTimetableFieldTypeEnum.Main
  }],
  rightList:any=[]
  leftFields.value.forEach((item:any,index:number)=>{
    leftList.push({
      FieldName: item.FieldName,
      DisplayName: item.DisplayName,
      IsEnabled: true,
      SortOrder: index+1,
      IsDefault:false,
      FieldType:CourseTimetableFieldTypeEnum.Main
    })
  })
  rightTags.value.forEach((item:any,index:number)=>{
    rightList.push({
      FieldName: item.FieldName,
      DisplayName: item.DisplayName,
      IsEnabled: item.IsEnabled,
      SortOrder: index,
      IsDefault:true,
      FieldType:CourseTimetableFieldTypeEnum.Tag
    })
  })
  // 准备信息设置数据
  const cardShowInformationSettings: CourseTimetableFieldSetting[] = [
    ...leftList,
    ...rightList
  ]
  let colorSettings: CourseTimetableColorSetting[] = []
  if(backgroundColorByType.value=='ByTeacher'||backgroundColorByType.value=='ByCourse'||backgroundColorByType.value=='ByClassroom'){
    colorSettings=[{
      SettingType: backgroundColorByType.value as CourseTimetableColorSettingTypeEnum,
      IsEnabled: true
    }]
  }else{
    colorSettings=[{
      SettingType: backgroundColorByType.value as CourseTimetableColorSettingTypeEnum,
      IsEnabled: true,
      ColorDetails: []
    }]
    backgroundColorItems.value.forEach((item:any) => {
      colorSettings[0].ColorDetails!.push({
        DisplayName: item.label,
        EnumValue: item.key*1,
        ColorValue: (backgroundColors.value || {})[item.key] || item.color,
      })
    })
  }
  let tagColorSettings: CourseTimetableTagColorSetting[] = []
  tagColorItems.value.forEach((item:any) => {
    tagColorSettings.push({
      TagFieldName: item.key,
      DisplayName: item.label,
      ColorValue: (tagColors.value || {})[item.key] || item.color
    })
  })
  // 保存信息设置
  const saveData: any = {
    TimetableType: currentTimetableType.value,
    CardShowInformationSettings: cardShowInformationSettings,
    ColorSettings: colorSettings,
    TagColorSettings: tagColorSettings,
    IsEnabled:true
  }

  const response = await saveTimetablePreference(saveData)
  if (response.ErrorCode !== 200) {
    throw new Error(response.ErrorMsg || '信息设置应用失败')
  }
}

// 将当前“信息设置”应用到指定课表类型
async function saveInfoSettingsForType() {
  const currentTabKey = currentTimetableType.value.toString()
  const snapshot = tabDataMap.value[currentTabKey]
  const leftList:any[] = [{
    FieldName:'StartTime',
    DisplayName:'上课时间',
    IsEnabled:true,
    SortOrder:0,
    IsDefault:true,
    FieldType:CourseTimetableFieldTypeEnum.Main
  }]
  const rightList:any[] = []
  ;(snapshot.leftFields || []).forEach((item:any,index:number)=>{
    leftList.push({
      FieldName: item.FieldName,
      DisplayName: item.DisplayName,
      IsEnabled: true,
      SortOrder: index+1,
      IsDefault:false,
      FieldType:CourseTimetableFieldTypeEnum.Main
    })
  })
  ;(snapshot.rightTags || []).forEach((item:any,index:number)=>{
    rightList.push({
      FieldName: item.FieldName,
      DisplayName: item.DisplayName,
      IsEnabled: item.IsEnabled,
      SortOrder: index,
      IsDefault:true,
      FieldType:CourseTimetableFieldTypeEnum.Tag
    })
  })
  const cardShowInformationSettings: CourseTimetableFieldSetting[] = [
    ...leftList,
    ...rightList
  ]
  let colorSettings: CourseTimetableColorSetting[] = []
  if(snapshot.backgroundColorByType=='ByTeacher'||snapshot.backgroundColorByType=='ByCourse'||snapshot.backgroundColorByType=='ByClassroom'){
    colorSettings=[{
      SettingType: snapshot.backgroundColorByType as CourseTimetableColorSettingTypeEnum,
      IsEnabled: true
    }]
  }else{
    colorSettings=[{
      SettingType: snapshot.backgroundColorByType as CourseTimetableColorSettingTypeEnum,
      IsEnabled: true,
      ColorDetails: []
    }]
    const items = backgroundColorItems.value // 基于当前tab类型生成的枚举项
    items.forEach((item:any) => {
      colorSettings[0].ColorDetails!.push({
        DisplayName: item.label,
        EnumValue: item.key*1,
        ColorValue: (snapshot.backgroundColors || {})[item.key] || item.color,
      })
    })
  }
  let tagColorSettings: CourseTimetableTagColorSetting[] = []
  const enabledTagItems = (snapshot.rightTags || []).filter((t:any)=>t.IsEnabled).slice(0,3).map((t:any)=>({
    key: t.FieldName,
    label: t.FieldName === 'IsSubscribeCourse' ? '约课' : t.DisplayName,
    color: (snapshot.tagColors || {})[t.FieldName] || DEFAULT_TAG_COLORS[t.FieldName] || '#409EFF'
  }))
  enabledTagItems.forEach((item:any) => {
    tagColorSettings.push({
      TagFieldName: item.key,
      DisplayName: item.label,
      ColorValue: item.color
    })
  })
  const saveData: any = {
    TimetableType: currentTimetableType.value,
    CardShowInformationSettings: cardShowInformationSettings,
    ColorSettings: colorSettings,
    TagColorSettings: tagColorSettings,
    IsEnabled: true,
    ApplyToAllTypes:true
  }
  const response = await saveTimetablePreference(saveData)
  if (response.ErrorCode !== 200) {
    throw new Error(response.ErrorMsg || '信息设置应用失败')
  }
}

// 检查是否有未保存的修改（信息/颜色 或 行高）
const hasUnsavedChanges = () => {
  const infoChanged = Object.values(tabDataMap.value).some(tabData => tabData.hasChanges)
  const heightChanged = Object.values(heightStateMap.value).some(state => state.hasChanges)
  return infoChanged || heightChanged
}

// 显示关闭确认对话框
const showCloseConfirm = async () => {
  try {
    await ElMessageBox.confirm(
      '还有修改未保存，确认关闭？',
      '提示',
      {
        confirmButtonText: '确认关闭',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    return true // 用户确认关闭
  } catch {
    return false // 用户取消关闭
  }
}

// 执行关闭收尾（不直接改drawer，交给before-close的done控制）
const afterCloseCleanup = () => {
  // 关闭时恢复默认值
  activeNav.value = 'info'
  viewMode.value = 'info'
  
  // 先清空变更标记，避免小红点残留
  Object.keys(tabDataMap.value || {}).forEach(key => {
    try { tabDataMap.value[key].hasChanges = false } catch {}
  })
  Object.keys(heightStateMap.value || {}).forEach(key => {
    try { heightStateMap.value[key].hasChanges = false } catch {}
  })
  // 清空所有tab数据
  tabDataMap.value = {}
  
  form.value = {
    TimeViewStart: '00:00',
    TimeViewEnd: '23:30'
  }
  heightType.value = 30
  // 清理行高各tab缓存
  heightStateMap.value = {}
  // 额外清理信息与颜色未保存的临时选择与搜索
  selectedFields.value = []
  searchKeyword.value = ''
  setAllSame.value = false
  setAllSameHeight.value = false
  
  if (_reject) {
    _reject()
    _reject = null
    _resolve = null
  }
}

// before-close 钩子（Element Plus）
const beforeClose = async (done: () => void) => {
  if (hasUnsavedChanges()) {
    const confirmed = await showCloseConfirm()
    if (!confirmed) {
      return // 阻止关闭
    }
  }
  // 允许关闭
  afterCloseCleanup()
  done()
}


// 切换字段选中状态
function toggleFieldSelection(field: any){
  const index = selectedFields.value.findIndex((f:any) => f.FieldName === field.FieldName)
  if (index > -1) {
    selectedFields.value.splice(index, 1)
  } else {
    if((leftFields.value.length + selectedFields.value.length) < 7){
      selectedFields.value.push(field)
    }else{
      ElMessage.warning('最多只能设置7个基础信息')
    }
  }
}

// 检查字段是否已选中
const isFieldSelected = (field: any) => {
  return selectedFields.value.some((f:any) => f.FieldName === field.FieldName)
}

// 取消添加字段
const cancelAddFields = () => {
  document.querySelector('body')?.click()
  selectedFields.value = []
}

// 确认添加字段
const confirmAddFields = () => {
  const selectedCount = selectedFields.value.length
  // 添加所有选中的字段
  const newFields = [...leftFields.value]
  selectedFields.value.forEach((field:any) => {
    const exists = newFields.some((f:any) => f.FieldName === field.FieldName)
    if (!exists) {
      newFields.push(field)
    }
  })
  
  leftFields.value = newFields

  document.querySelector('body')?.click()
  selectedFields.value = []
}

// 处理背景颜色变化
const handleBackgroundColorChange = (colors: Record<string, string>) => {
  console.log('背景颜色变化:', colors)
  // 记录最后变更的枚举项，用于示例文案
  try {
    const prev = { ...(backgroundColors.value || {}) }
    backgroundColors.value = colors
    const changedKey = Object.keys(colors).find(k => prev[k] !== colors[k])
    if (changedKey) {
      lastChangedKeyByType.value[backgroundColorByType.value] = changedKey
    }
  } catch (e) {
    backgroundColors.value = colors
  }
  // 这里可以添加保存背景颜色的逻辑
}

// 监听backgroundColorByType变化，更新backgroundColors
watch(backgroundColorByType, (newType) => {
  const newItems = backgroundColorItems.value
  const newColors: Record<string, string> = {}
  
  newItems.forEach(item => {
    newColors[item.key] = item.color
  })
  
  backgroundColors.value = newColors
}, { immediate: true })

// 处理标签颜色变化
const handleTagColorChange = (colors: Record<string, string>) => {
  console.log('标签颜色变化:', colors)
  tagColors.value = colors
  // 这里可以添加保存标签颜色的逻辑
}

// 记录行点击，更新对应类型下的“当前选择键”，用于示例卡片颜色
function handleBackgroundItemPick(key: string) {
  if (!key) return
  lastChangedKeyByType.value[backgroundColorByType.value] = String(key)
}

// Promise 控制
let _resolve: any = null
let _reject: any = null

// 对外暴露的open方法
function open(params?: any) {
  return new Promise((resolve, reject) => {
    _resolve = resolve
    _reject = reject
    drawer.value = true
    // 初始tab类型与导航
    const initTab = params && params.tabType ? params.tabType : CourseTimetableTypeEnum.TeacherTimetable
    const initNav = params && params.nav ? params.nav : 'info'
    const initViewMode = params && params.viewMode ? params.viewMode : 'info'
    currentTimetableType.value = initTab as CourseTimetableTypeEnum
    activeNav.value = initNav
    viewMode.value = initViewMode

    // 清空所有tab数据，重新开始
    tabDataMap.value = {}

    // 一次性加载所有课表类型的设置
    nextTick(() => {
      isInitializing.value = true
      // 初始化高度各tab默认值
      initializeHeightTabs()
      loadSettings().finally(() => {
        // 加载完成，解除初始化保护
        isInitializing.value = false
        // 确保初始进入不显示小红点
        Object.keys(tabDataMap.value || {}).forEach(key => {
          try { tabDataMap.value[key].hasChanges = false } catch {}
        })
      })
    })
    // 下一拍滚到颜色设置
    if (initViewMode === 'color') {
      nextTick(() => {
        colorConfigRef.value?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
      })
    }
  })
}

defineExpose({
  open
})

// ========== 示例标签文案 ===========
// 记录不同背景分组下，用户最后一次修改颜色的枚举键 - 现在通过computed属性管理

// 当前示例卡片所用的颜色键（优先使用当前类型最后一次点击的项，否则取第一个）
const currentBackgroundSelectedKey = computed(() => {
  const type = backgroundColorByType.value
  const picked = lastChangedKeyByType.value[type]
  if (picked != null && picked !== '') return String(picked)
  const first = backgroundColorItems.value?.[0]?.key
  return first != null ? String(first) : ''
})

// 当前示例卡片的实际颜色：来自 v-model 的 backgroundColors 映射，回退到 items 的默认色
const currentCardColor = computed(() => {
  const key = String(currentBackgroundSelectedKey.value || '')
  const map = backgroundColors.value || {}
  if (key && map[key]) return map[key]
  const items = backgroundColorItems.value || []
  const found = items.find(i => String(i.key) === key) || items[0]
  return found?.color || '#4893FF'
})

function getTagPreviewText(tag: any): string {
  const field = tag?.FieldName
  if (!field) return ''

  if (field === 'IsSubscribeCourse') return '约课'

  if (field === 'IsOneToOne') return '集体班'

  if (field === 'Finished') {
    // 上课状态
    let label = '未上课'
    if (backgroundColorByType.value === 'ByCourseFinished') {
      const pickedKey = lastChangedKeyByType.value['ByCourseFinished']
      const items = backgroundColorItems.value
      const found = items.find(i => String(i.key) === String(pickedKey))
      label = found?.label || '未上课'
    }
    return `${transToConfigDescript(label)}`
  }

  if (field === 'CourseStatus') {
    // 开课状态
    let label = '已开课'
    if (backgroundColorByType.value === 'ByOpeningStatus') {
      const pickedKey = lastChangedKeyByType.value['ByOpeningStatus']
      const items = backgroundColorItems.value
      const found = items.find(i => String(i.key) === String(pickedKey))
      label = found?.label || '已开课'
    }
    return `${label}`
  }

  if (field === 'ClassPlanNum_ClassPlanCount') return '10/20'
  if (field === 'CourseType') return '线上课'
  if (field === 'TeacherName') return '李四'
  if (field === 'ClassroomName') return '201'

  return tag.DisplayName || ''
}

function getTagColor(tag: any): string {
  const field = tag?.FieldName as string | undefined
  if (!field) return '#909399'
  return (tagColors.value || {})[field] || DEFAULT_TAG_COLORS[field] || '#409EFF'
}
const formRef = ref<FormInstance>()
const form = ref({
  TimeViewStart: DEFAULT_TIME_RANGE_START,
  TimeViewEnd: DEFAULT_TIME_RANGE_END
})
const currentTableTypeHeight = ref<CourseTimetableTypeEnum>(CourseTimetableTypeEnum.TeacherTimetable)
// 切换课表类型--时刻高度
const handlTableTypeHieghtChange = (type: string | number) => {
  currentTableTypeHeight.value = type as CourseTimetableTypeEnum
  
  // 如果当前tab还没有加载过数据，则从后台加载
  // if (!currentTabData.value.isLoaded) {
  //   loadSettings()
  // }
}

// 行高：每个tab独立状态
interface HeightTabState {
  value: number
  savedValue: number
  hasChanges: boolean
  isLoaded: boolean
}
const DEFAULT_ROW_HEIGHT = 30
const heightStateMap = ref<Record<string, HeightTabState>>({})
const allHeightTimetableTypes = [
  CourseTimetableTypeEnum.TeacherTimetable,
  CourseTimetableTypeEnum.StudentTimetable,
  CourseTimetableTypeEnum.ClassTimetable,
  CourseTimetableTypeEnum.ClassroomTimetable,
  CourseTimetableTypeEnum.TimeTimetable
]
function initializeHeightTabs() {
  allHeightTimetableTypes.forEach(type => {
    const key = type.toString()
    if (!heightStateMap.value[key]) {
      heightStateMap.value[key] = {
        value: DEFAULT_ROW_HEIGHT,
        savedValue: DEFAULT_ROW_HEIGHT,
        hasChanges: false,
        isLoaded: true
      }
    }
  })
}

// UI 绑定的当前高度值
const heightType = ref<number>(DEFAULT_ROW_HEIGHT)

// 切换tab时，同步当前展示值
watch(currentTableTypeHeight, (newType) => {
  const key = (newType as CourseTimetableTypeEnum).toString()
  const state = heightStateMap.value[key]
  heightType.value = state ? state.value : DEFAULT_ROW_HEIGHT
}, { immediate: true })

// 高度值修改时，标记未保存
watch(heightType, (newVal) => {
  const key = currentTableTypeHeight.value.toString()
  const state = heightStateMap.value[key]
  if (state) {
    state.value = newVal
    state.hasChanges = state.value !== state.savedValue
  }
})

function hasHeightTabChanges(type: CourseTimetableTypeEnum) {
  const tabKey = type.toString()
  return !!heightStateMap.value[tabKey]?.hasChanges
}

function getHeightTabLabel(type: CourseTimetableTypeEnum) {
  switch (type) {
    case CourseTimetableTypeEnum.TeacherTimetable: return '老师课表'
    case CourseTimetableTypeEnum.StudentTimetable: return '1对1学员课表'
    case CourseTimetableTypeEnum.ClassTimetable: return '班级课表'
    case CourseTimetableTypeEnum.ClassroomTimetable: return '教室课表'
    case CourseTimetableTypeEnum.TimeTimetable: return '校区课表'
    default: return '课表'
  }
}
</script>

<style lang="scss" scoped>
.timetable-preference-settings {
  display: flex;
  height: 100%;
}

.drawer-body-wrap {
  display: flex;
  flex-direction: row;
  height: 100%;
  padding: 0;
  min-height: 0;
}

/* 左侧导航 */
.settings-nav {
  width: 140px;
  position: relative;
  z-index: 10;
  height: 100%;
  display: flex;
  flex-direction: column;
}


.nav-badge {
  margin-left: 8px;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  padding: 10px 8px;
}

.nav-item {
  padding: 0 12px;
  height: 40px;
  line-height: 40px;
  cursor: pointer;
  color: #606266;
  border-radius: 4px;
}

.nav-item.active {
  background: rgba(0,0,0,0.04);
}

.nav-icon {
  margin-right: 8px;
  font-size: 16px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

/* 右侧内容区域 */
.settings-content {
  flex: 1;
  overflow-y: auto;
  height: 100%;
  padding: 0 12px;
  background: #F7F8FA;
}

/* 信息设置样式 */
.info-settings {
  max-width: 660px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

}
.info-settings-content{
  display: flex;
  flex-direction: column;
  flex: 1;
  height:calc(100% - 60px);
  overflow-y: auto;
}

.timetable-type-tabs {
  margin-bottom: 8px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-dot {
  width: 6px;
  height: 6px;
  background-color: #F56C6C;
  border-radius: 50%;
  display: inline-block;
}

.example-effect {
  margin-bottom: 12px;
  position: relative;
}
.schedule-color-content-setting{
  background:#fff;
  border-radius: 8px;
  flex: 1;
}

.example-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.example-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.example-card {
  display: flex;
  background:#fff;
  border-radius: 8px;
  padding: 12px 20px;
}

.course-card-wrap{
  display: flex;
  justify-content: center;
}
.course-card {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 0 4px 0;
    border-top: 3px solid #4893FF;
    transition: all 0.2s ease;
    width: 157px;
    & + .course-card {
        margin-top: 12px;
    }
}
.course-time {
    font-weight: 600;
    color: #303133;
    line-height: 18px;
}
.course-title {
    margin-top: 6px;
    color: #303133;
}
.course-meta {
    font-size: 12px;
    color: #606266;
    line-height: 18px;
    .meta-item { margin-bottom: 2px; }
}
.course-tags {
  display: flex;
  gap: 6px;
  width: 100%;
  .course-tags-item {
      display: inline-block;
      font-size: 10px;
      line-height: 20px;
      padding: 0 5px;
      border-radius: 4px;
      background: #fff;
      color: #909399;
      text-align: center;
      flex-shrink: 0;
      margin-bottom: 4px;
  }
}
.course-meta,.course-tags{
  position: relative;
  &.has-annotation{
    &::before{
      content: '';
      position: absolute;
      left: -6px;
      top: 0;
      width: calc(100% + 4px);
      height: 100%;
      background: rgba(205,208,214,0.3);
      border-radius: 6px 6px 6px 6px;
      border: 1px dashed #CDD0D6;
    }
  }
  .annotation-left-box{
    position: absolute;
    left: -210px;
    top: 50%;
    font-size: 13px;
    transform: translateY(-50%);
    color: #666;
    &::before{
      content: '';
      position: absolute;
      left: 202px;
      top: 8px;
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: #CDD0D6;
    }
    &::after{
      content: '';
      position: absolute;
      left: 136px;
      top: 10px;
      width: 69px;
      height: 1px;
      background: #CDD0D6;
    }
  }
}
.course-tags,.course-card{
  position: relative;
  .annotation-right-box{
    position: absolute;
    right: -200px;
    top: 50%;
    font-size: 13px;
    color: #666;
    transform: translateY(-50%);
    &::before{
      content: '';
      position: absolute;
      right: 200px;
      top: 8px;
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: #CDD0D6;
    }
    &::after{
      content: '';
      position: absolute;
      right: 132px;
      top: 10px;
      width: 69px;
      height: 1px;
      background: #CDD0D6;
    }
  }
}
.schedule-view-mode-tabs {
	display: flex;
  margin: 10px 20px 0;
	background-color: #F3F4F4;
	border-radius: 6px;
	padding: 2px;
	gap: 4px;
	width: fit-content;
}

.schedule-view-mode-tab {
	padding: 7px 16px;
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
.info-config {
  margin:0 20px;
  padding: 16px 0;
}

.config-grid,.color-config {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.color-config{
  gap: 90px;
}

.section-header {
  display: flex;
  align-items: center;
  background-color: #F5F7FA;
  border-bottom: 1px solid #EBEEF5;
  padding: 8px;
  line-height:23px;
}

.section-header h5 {
  margin: 0;
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
}
.color-config-section-header {
  display: flex;
  align-items: center;
  padding: 8px 0;
  line-height:23px;
}

.section-badge {
  margin-left: 8px;
}

.add-btn {
  margin:10px 12px;
}

.hint {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.field-list,
.tag-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.draggable-list {
  display: flex;
  flex-direction: column;
}

.field-item,
.tag-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid #EBEEF5;
  // cursor: grab;
  transition: all 0.3s;
  user-select: none;
  position: relative;
  &:hover{
    background: #f5f7fa;
  }
}

.default-field {
  cursor: default;
  background: #f0f9ff;
  border-color: #91d5ff;
}

.default-badge {
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
}

.drag-handle {
  margin-left: 10px;
  color: #999;
  cursor: grab;
  font-size: 16px;
  padding: 6px;
  border-radius: 4px;
  transition: all 0.3s;
  min-width: 28px;
  min-height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.field-name {
  font-size: 14px;
  line-height: 1.4;
  color:#606266;
  flex: 1;
}
.tag-item .wtwo-checkbox{
  flex: 1;
}

.info-icon {
  margin-left: 8px;
  color: #999;
  font-size: 14px;
  cursor: help;
}

.empty-state {
  padding: 20px;
  text-align: center;
}

/* 颜色设置样式 */
.color-settings-content,
.resource-settings-content {
  max-width: 800px;
}

.color-settings-content h4,
.resource-settings-content h4 {
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}

.color-hint,
.resource-hint {
  color: #666;
  margin-bottom: 20px;
  font-size: 14px;
}

.color-placeholder,
.resource-placeholder {
  margin-top: 40px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}


/* 添加字段对话框样式 */
.field-search-container {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;

  .el-input {
    .el-input__wrapper {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      border-radius: 6px;

      &:hover {
        box-shadow: 0 0 0 1px #c0c4cc inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px #409eff inset;
      }
    }
  }
}

.field-description {
  padding: 12px 20px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.field-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  max-height: 400px;
}

.field-option-item {
  margin: 0 12px 8px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #f0f9ff;
    border-color: #409eff;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.field-option-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}

.field-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}




.field-action {
  color: #409eff;
  font-size: 16px;
  opacity: 0;
  transition: opacity 0.3s;
}

.field-option-item:hover .field-action {
  opacity: 1;
}

.field-empty {
  padding: 40px 20px;
  text-align: center;
}
.operation-btn-box{
  background-color: #fff;
  padding: 10px 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.time-settings,.height-settings{
  height: 100%;
  overflow: hidden;
}
.time-settings-content-wrap,.height-settings-content-wrap{
  display: flex;
  flex-direction: column;
  height: 100%;
}
.time-settings-content,.height-settings-content{
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  flex: 1;
  overflow-y: auto;
}
.height-settings-content-wrap{
  height: calc(100% - 60px);
  .height-settings-content{
    padding: 0 0 16px 0;
  }
}
.schedule-view-height-set-tabs{
  padding: 12px 16px;
}
.time-height-example-wrap{
  padding-left: 40px;
  .time-top-line,.time-bottom-line{
    width: 100%;
    height: 1px;
    background: #E5E5E5;
    position: relative;
    font-size: 12px;
  }
  .time-top-line-text,.time-bottom-line-text{
    position: absolute;
    left: -36px;
    top: -6px;
    width: 100%;
    height: 100%;
  }
}
</style>
