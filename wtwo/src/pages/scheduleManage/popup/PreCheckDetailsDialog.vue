<template>
  <el-drawer v-model="dialogVisible" title="预检查详情" size="800px" :close-on-click-modal="false" @close="handleClose">
    <div class="h100%">
      <el-tabs v-model="activeTab" class="h40px">
        <!-- 动态渲染 tabs -->
        <el-tab-pane v-for="tab in visibleTabs" :key="tab.name" :label="tab.label" :name="tab.name">
        </el-tab-pane>
      </el-tabs>
      <div class="precheck-drawer-content">
        <div class="restriction-details" v-show="activeTab === 'restriction'">

          <!-- 当前数据展示 -->
          <div class="current-data-section">
            <div class="current-data-section-header">
              <div class="section-title">当前数据</div>
              <!-- 前往修改按钮 -->
              <el-button type="primary" style="margin-bottom: 6px;" text @click="handleGoToModify">
                前往修改
              </el-button>
            </div>
            <el-table :data="[currentRow]" border size="small" v-auto-title>
              <el-table-column prop="CampusName" :label="transToConfigDescript('上课校区')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('CampusName') }]">{{ row.CampusName
                    }}</span>
                    <el-tooltip v-if="shouldShowIcon('CampusName')" :content="getFieldMessage('CampusName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showClassColumn" prop="ClassName" :label="transToConfigDescript('上课班级')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('ClassName') }]">{{ row.ClassName
                    }}</span>
                    <el-tooltip v-if="shouldShowIcon('ClassName')" :content="getFieldMessage('ClassName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showStudentColumn" prop="StudentUserName" :label="transToConfigDescript('上课学员')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('StudentUserID') }]">{{
                      row.StudentUserName
                      }}</span>
                    <el-tooltip v-if="shouldShowIcon('StudentUserID')" :content="getFieldMessage('StudentUserID')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ShiftName" :label="transToConfigDescript('上课课程')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('ShiftName') || isWarnField('ShiftID') }]">{{
                        row.ShiftName }}</span>
                    <el-tooltip v-if="shouldShowIcon('ShiftName') || shouldShowIcon('ShiftID')"
                      :content="getFieldMessage('ShiftName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="Date" :label="transToConfigDescript('上课日期')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('Date') }]">{{ row.Date }}</span>
                    <el-tooltip v-if="shouldShowIcon('Date')" :content="getFieldMessage('Date')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="timeRange" :label="transToConfigDescript('上课时间')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('timeRange') || isWarnField('StartTime') || isWarnField('EndTime') }]">{{
                        row.timeRange }}</span>
                    <el-tooltip
                      v-if="shouldShowIcon('timeRange') || shouldShowIcon('StartTime') || shouldShowIcon('EndTime')"
                      :content="getFieldMessage('timeRange')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ClassRoomName" :label="transToConfigDescript('上课教室')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('ClassRoomName') || isWarnField('ClassRoomID') }]">{{
                        row.ClassRoomName }}</span>
                    <el-tooltip v-if="shouldShowIcon('ClassRoomName') || shouldShowIcon('ClassRoomID')"
                      :content="getFieldMessage('ClassRoomName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="MainTeacherName" :label="transToConfigDescript('任课老师')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('MainTeacherName') || isWarnField('MainTeacherID') }]">{{
                        row.MainTeacherList && row.MainTeacherList.length > 0 ? row.MainTeacherList[0].Name :
                          row.MainTeacherName || '-' }}</span>
                    <el-tooltip v-if="shouldShowIcon('MainTeacherName') || shouldShowIcon('MainTeacherID')"
                      :content="getFieldMessage('MainTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="AssistantTeacherName" label="助教">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('AssistantTeacherName') || isWarnField('AssistantTeacherID') }]">{{
                        row.AssistantTeacherList && row.AssistantTeacherList.length > 0 ?
                          row.AssistantTeacherList.map(item => item.Name).join('，') :
                          row.AssistantTeacherName || '-'}}</span>
                    <el-tooltip v-if="shouldShowIcon('AssistantTeacherName') || shouldShowIcon('AssistantTeacherID')"
                      :content="getFieldMessage('AssistantTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <!-- 信息提示 -->
            <pageAttentionTips class="ml-0px! mb-8px! mt-4px!">上方为“当前数据”触发了下方排课限制的规则</pageAttentionTips>
          </div>


          <!-- 下半区（灰底容器） -->
          <div class="restriction-bottom">
            <div class="bg-#fff w100% h100% p-12px rounded-8px" style="display: block;">
              <!-- 限制内容 -->
              <div class="restriction-content">
                <div class="section-title">限制内容</div>
                <div v-for="(checkField, index) in preCheckData?.CheckFieldList || []" :key="index"
                  class="restriction-item">
                  <div class="restriction-number">{{ index + 1 }}.</div>
                  <div class="restriction-message">
                    {{ checkField.ErrorMessage }}
                  </div>
                </div>
              </div>

              <!-- 查看触发的限制规则 -->
              <!-- <div class="view-rules-link" @click="toggleDetailedRules">
                <span>查看触发的限制规则</span>
                <el-icon class="caret-icon" :class="{ 'is-collapsed': !showDetailedRules }">
                  <ArrowUp />
                </el-icon>
              </div> -->

              <!-- 详细规则内容（动态渲染） -->
              <!-- <div v-show="showDetailedRules" class="detailed-rules-content">
                <div class="restriction-rules" v-for="(group, gIdx) in displayRuleGroups" :key="gIdx">
                  <div class="section-title">{{ group.groupTitle }}</div>
                  <div class="rule-item" v-for="item in group.items" :key="item.key">
                    <el-checkbox :model-value="true" disabled checked class="rule-checkbox" />
                    <div class="rule-content">
                      <div class="rule-title">{{ item.text }}</div>
                      <div class="rule-description sub-title">{{ item.desc }}</div>
                      <div v-if="item.isTime" class="time-input-row">
                        <el-time-picker :model-value="item.value" format="HH:mm" value-format="HH:mm" placeholder="请设置"
                          disabled class="time-input" />
                      </div>
                    </div>
                  </div>
                </div>
              </div> -->
            </div>
          </div>
        </div>
        <div class="conflict-details" v-show="activeTab === 'conflict'">
          <!-- 当前数据展示（白底） -->
          <div class="current-data-section">
            <div class="current-data-section-header">
              <div class="section-title">当前数据</div>
              <el-button type="primary" text @click="handleGoToModify">前往修改</el-button>
            </div>
            <el-table :data="[currentRow]" border size="small" v-auto-title>
              <el-table-column prop="CampusName" :label="transToConfigDescript('上课校区')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('CampusName') }]">{{ row.CampusName
                    }}</span>
                    <el-tooltip v-if="shouldShowIcon('CampusName')" :content="getFieldMessage('CampusName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showClassColumn" prop="ClassName" :label="transToConfigDescript('上课班级')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('ClassName') }]">{{ row.ClassName
                    }}</span>
                    <el-tooltip v-if="shouldShowIcon('ClassName')" :content="getFieldMessage('ClassName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showStudentColumn" prop="StudentUserName" :label="transToConfigDescript('上课学员')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('StudentUserID') }]">{{
                      row.StudentUserName
                      }}</span>
                    <el-tooltip v-if="shouldShowIcon('StudentUserID')" :content="getFieldMessage('StudentUserID')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ShiftName" :label="transToConfigDescript('上课课程')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('ShiftName') || isWarnField('ShiftID') }]">{{
                        row.ShiftName }}</span>
                    <el-tooltip v-if="shouldShowIcon('ShiftName') || shouldShowIcon('ShiftID')"
                      :content="getFieldMessage('ShiftName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="Date" :label="transToConfigDescript('上课日期')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isWarnField('Date') }]">{{ row.Date }}</span>
                    <el-tooltip v-if="shouldShowIcon('Date')" :content="getFieldMessage('Date')" placement="top">
                      <el-icon class="warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="timeRange" :label="transToConfigDescript('上课时间')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('timeRange') || isWarnField('StartTime') || isWarnField('EndTime') }]">{{
                        row.timeRange }}</span>
                    <el-tooltip
                      v-if="shouldShowIcon('timeRange') || shouldShowIcon('StartTime') || shouldShowIcon('EndTime')"
                      :content="getFieldMessage('timeRange')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ClassRoomName" :label="transToConfigDescript('上课教室')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('ClassRoomName') || isWarnField('ClassRoomID') }]">{{
                        row.ClassRoomName }}</span>
                    <el-tooltip v-if="shouldShowIcon('ClassRoomName') || shouldShowIcon('ClassRoomID')"
                      :content="getFieldMessage('ClassRoomName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="MainTeacherName" :label="transToConfigDescript('任课老师')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('MainTeacherName') || isWarnField('MainTeacherID') }]">{{
                        row.MainTeacherList && row.MainTeacherList.length > 0 ? row.MainTeacherList[0].Name :
                          row.MainTeacherName || '-' }}</span>
                    <el-tooltip v-if="shouldShowIcon('MainTeacherName') || shouldShowIcon('MainTeacherID')"
                      :content="getFieldMessage('MainTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="AssistantTeacherName" label="助教">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isWarnField('AssistantTeacherName') || isWarnField('AssistantTeacherID') }]">{{
                        row.AssistantTeacherList && row.AssistantTeacherList.length > 0 ?
                          row.AssistantTeacherList.map(item => item.Name).join('，') :
                          row.AssistantTeacherName || '-'}}</span>
                    <el-tooltip v-if="shouldShowIcon('AssistantTeacherName') || shouldShowIcon('AssistantTeacherID')"
                      :content="getFieldMessage('AssistantTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 下半区（灰底容器） -->
          <div class="conflict-bottom">

            <!-- 表格中其他数据 (冲突) -->
          </div>
          <div v-if="preCheckData?.ConflictFieldList?.ConflictingDraftList?.length > 0" class="conflict-section">
            <div class="section-title">表格中其他数据 (冲突)</div>
            <el-table :data="preCheckData.ConflictFieldList.ConflictingDraftList" border size="small" v-auto-title>
              <el-table-column prop="CampusName" :label="transToConfigDescript('上课校区')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'CampusName') }]">{{ row.CampusName
                    }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'CampusName')" :content="getConflictFieldMessage('CampusName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showClassColumn" prop="ClassName" :label="transToConfigDescript('上课班级')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'ClassName') }]">{{ row.ClassName
                    }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'ClassName')" :content="getConflictFieldMessage('ClassName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showStudentColumn" prop="StudentUserName" :label="transToConfigDescript('上课学员')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'StudentUserID') }]">{{
                      row.StudentUserName
                      }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'StudentUserID')" :content="getConflictFieldMessage('StudentUserID')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ShiftName" :label="transToConfigDescript('上课课程')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'ShiftName') || isConflictFieldInRow(row, 'ShiftID') }]">{{
                        row.ShiftName }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'ShiftName') || isConflictFieldInRow(row, 'ShiftID')"
                      :content="getConflictFieldMessage('ShiftName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="Date" :label="transToConfigDescript('上课日期')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'Date') }]">{{ row.Date }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'Date')" :content="getConflictFieldMessage('Date')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="timeRange" :label="transToConfigDescript('上课时间')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'timeRange') || isConflictFieldInRow(row, 'StartTime') || isConflictFieldInRow(row, 'EndTime') }]">{{
                        row.StartTime + '~' + row.EndTime }}</span>
                    <el-tooltip
                      v-if="isConflictFieldInRow(row, 'timeRange') || isConflictFieldInRow(row, 'StartTime') || isConflictFieldInRow(row, 'EndTime')"
                      :content="getConflictFieldMessage('timeRange')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ClassRoomName" :label="transToConfigDescript('上课教室')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'ClassRoomName') || isConflictFieldInRow(row, 'ClassRoomID') }]">{{
                        row.ClassRoomName }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'ClassRoomName') || isConflictFieldInRow(row, 'ClassRoomID')"
                      :content="getConflictFieldMessage('ClassRoomName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="MainTeacherName" :label="transToConfigDescript('任课老师')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'MainTeacherName') || isConflictFieldInRow(row, 'MainTeacherID') || isConflictFieldInRow(row, 'MainTeacherList') }]">{{
                        row.MainTeacherList && row.MainTeacherList.length > 0 ? row.MainTeacherList[0].Name :
                          row.MainTeacherName || '-' }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'MainTeacherName') || isConflictFieldInRow(row, 'MainTeacherID') || isConflictFieldInRow(row, 'MainTeacherList')"
                      :content="getConflictFieldMessage('MainTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="AssistantTeacherName" label="助教">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'AssistantTeacherName') || isConflictFieldInRow(row, 'AssistantTeacherID') || isConflictFieldInRow(row, 'AssistantTeacherList') }]">{{
                        row.AssistantTeacherList && row.AssistantTeacherList.length > 0 ?
                          row.AssistantTeacherList.map(item => item.Name).join('，') :
                          row.AssistantTeacherName || '-'}}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'AssistantTeacherName') || isConflictFieldInRow(row, 'AssistantTeacherID') || isConflictFieldInRow(row, 'AssistantTeacherList')"
                      :content="getConflictFieldMessage('AssistantTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 已有排课数据 (冲突) -->
          <div v-if="preCheckData?.ConflictFieldList?.ConflictingCourseList?.length > 0" class="conflict-section">
            <div class="section-title">已有排课数据 (冲突)</div>
            <el-table :data="preCheckData.ConflictFieldList.ConflictingCourseList" border size="small" v-auto-title>
              <el-table-column prop="CampusName" :label="transToConfigDescript('上课校区')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'CampusName') }]">{{ row.CampusName
                    }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'CampusName')" :content="getConflictFieldMessage('CampusName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showClassColumn" prop="ClassName" :label="transToConfigDescript('上课班级')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'ClassName') }]">{{ row.ClassName
                    }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'ClassName')" :content="getConflictFieldMessage('ClassName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-if="showStudentColumn" prop="StudentUserName" :label="transToConfigDescript('上课学员')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'StudentUserID') }]">{{
                      row.StudentUserName
                      }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'StudentUserID')" :content="getConflictFieldMessage('StudentUserID')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ShiftName" :label="transToConfigDescript('上课课程')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'ShiftName') || isConflictFieldInRow(row, 'ShiftID') }]">{{
                        row.ShiftName }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'ShiftName') || isConflictFieldInRow(row, 'ShiftID')"
                      :content="getConflictFieldMessage('ShiftName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="Date" :label="transToConfigDescript('上课日期')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'Date') }]">{{ row.Date }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'Date')" :content="getConflictFieldMessage('Date')" placement="top">
                      <el-icon class="warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="timeRange" :label="transToConfigDescript('上课时间')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'timeRange') || isConflictFieldInRow(row, 'StartTime') || isConflictFieldInRow(row, 'EndTime') }]">{{
                        row.StartTime + '~' + row.EndTime }}</span>
                    <el-tooltip
                      v-if="isConflictFieldInRow(row, 'timeRange') || isConflictFieldInRow(row, 'StartTime') || isConflictFieldInRow(row, 'EndTime')"
                      :content="getConflictFieldMessage('timeRange')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="ClassRoomName" :label="transToConfigDescript('上课教室')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'ClassRoomName') || isConflictFieldInRow(row, 'ClassRoomID') }]">{{
                        row.ClassRoomName }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'ClassRoomName') || isConflictFieldInRow(row, 'ClassRoomID')"
                      :content="getConflictFieldMessage('ClassRoomName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="MainTeacherName" :label="transToConfigDescript('任课老师')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'MainTeacherName') || isConflictFieldInRow(row, 'MainTeacherID') || isConflictFieldInRow(row, 'MainTeacherList') }]">{{
                        row.MainTeacherList && row.MainTeacherList.length > 0 ? row.MainTeacherList[0].Name :
                          row.MainTeacherName || '-' }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'MainTeacherName') || isConflictFieldInRow(row, 'MainTeacherID') || isConflictFieldInRow(row, 'MainTeacherList')"
                      :content="getConflictFieldMessage('MainTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="AssistantTeacherName" label="助教">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'AssistantTeacherName') || isConflictFieldInRow(row, 'AssistantTeacherID') || isConflictFieldInRow(row, 'AssistantTeacherList') }]">{{
                        row.AssistantTeacherList && row.AssistantTeacherList.length > 0 ?
                          row.AssistantTeacherList.map(item => item.Name).join('，') :
                          row.AssistantTeacherName || '-'}}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'AssistantTeacherName') || isConflictFieldInRow(row, 'AssistantTeacherID') || isConflictFieldInRow(row, 'AssistantTeacherList')"
                      :content="getConflictFieldMessage('AssistantTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 已有日程数据 (冲突) -->
          <div v-if="preCheckData?.ConflictFieldList?.ConflictingScheduleList?.length > 0" class="conflict-section">
            <div class="section-title">已有日程数据 (冲突)</div>
            <el-table :data="preCheckData.ConflictFieldList.ConflictingScheduleList" border size="small" v-auto-title>
              <el-table-column prop="CampusName" :label="transToConfigDescript('上课校区')">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'CampusName') }]">{{ row.CampusName
                    }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'CampusName')" :content="getConflictFieldMessage('CampusName')"
                      placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="Date" label="日程日期">
                <template #default="{ row }">
                  <div class="field-content">
                    <span :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'Date') }]">{{ row.Date }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'Date')" :content="getConflictFieldMessage('Date')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="timeRange" label="日程时间">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'timeRange') || isConflictFieldInRow(row, 'StartTime') || isConflictFieldInRow(row, 'EndTime') }]">{{
                        row.StartTime + '~' + row.EndTime }}</span>
                    <el-tooltip
                      v-if="isConflictFieldInRow(row, 'timeRange') || isConflictFieldInRow(row, 'StartTime') || isConflictFieldInRow(row, 'EndTime')"
                      :content="getConflictFieldMessage('timeRange')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="MainTeacherName" label="参与人员">
                <template #default="{ row }">
                  <div class="field-content">
                    <span
                      :class="['field-text', { 'highlight-cell': isConflictFieldInRow(row, 'MainTeacherName') || isConflictFieldInRow(row, 'MainTeacherID') || isConflictFieldInRow(row, 'MainTeacherList') }]">{{
                        row.MainTeacherList && row.MainTeacherList.length > 0 ? row.MainTeacherList[0].Name :
                          row.MainTeacherName || '-' }}</span>
                    <el-tooltip v-if="isConflictFieldInRow(row, 'MainTeacherName') || isConflictFieldInRow(row, 'MainTeacherID') || isConflictFieldInRow(row, 'MainTeacherList')"
                      :content="getConflictFieldMessage('MainTeacherName')" placement="top">
                      <el-icon class="field-icon warning-icon"><svg aria-hidden="true">
                          <use xlink:href="#w2-xianzhi"></use>
                        </svg></el-icon>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, nextTick, onBeforeUnmount, watch } from 'vue'
import { querySysConfig } from '@/api/comm'
import { ArrowUp } from '@element-plus/icons-vue'
import { transToConfigDescript } from '@/utils/filters/filters'

// 自动为 field-text 添加原文 title 的指令
const vAutoTitle = {
  mounted(el) {
    const apply = () => {
      const nodes = el.querySelectorAll?.('.field-text') || []
      nodes.forEach((n) => {
        const text = (n.textContent || '').trim()
        if (text) n.setAttribute('title', text)
      })
    }
    apply()
    el.__autoTitleObserver__ = new MutationObserver(() => apply())
    el.__autoTitleObserver__.observe(el, { childList: true, subtree: true, characterData: true })
  },
  updated(el) {
    const nodes = el.querySelectorAll?.('.field-text') || []
    nodes.forEach((n) => {
      const text = (n.textContent || '').trim()
      if (text) n.setAttribute('title', text)
    })
  },
  unmounted(el) {
    if (el.__autoTitleObserver__) {
      el.__autoTitleObserver__.disconnect()
      delete el.__autoTitleObserver__
    }
  }
}

// Props
const props = defineProps({
  currentRow: {
    type: Object,
    default: () => ({})
  },
  preCheckData: {
    type: Object,
    default: () => ({})
  },
  tableType: {
    type: Number,
    default: 10 // 10=班级排课；20=学员排课；30=预约课
  }
})

// Emits
const emit = defineEmits(['update:visible', 'goToModify'])

// 响应式数据
const activeTab = ref('restriction')

// 计算可见的 tabs
const visibleTabs = computed(() => {
  const tabs = []
  if (getRestrictionCount() > 0) {
    tabs.push({
      name: 'restriction',
      label: `限制详情 (${getRestrictionCount()})`
    })
  }
  if (getConflictCount() > 0) {
    tabs.push({
      name: 'conflict',
      label: `冲突详情 (${getConflictCount()})`
    })
  }
  return tabs
})

// 根据排课类型判断是否显示班级列
const showClassColumn = computed(() => {
  return props.tableType === 10 // 只有班级排课显示班级列
})

// 根据排课类型判断是否显示学员列
const showStudentColumn = computed(() => {
  return props.tableType === 20 // 只有学员排课显示学员列
})

// 监听 tabs 变化，自动切换到第一个可用的 tab
watch(visibleTabs, (newTabs) => {
  if (newTabs.length > 0 && !newTabs.some(tab => tab.name === activeTab.value)) {
    activeTab.value = newTabs[0].name
  } else if (newTabs.length === 0) {
    activeTab.value = ''
  }
}, { immediate: true })


const showDetailedRules = ref(false) // 控制详细规则折叠状态
let _resolve = null
let _reject = null

// 本地可见性
const dialogVisible = ref(false)


// 规则配置与映射
const courseRuleSetting = ref(null)
const RULE_DEFS = [
  { key: 'EnableAssignTeacherCourseCampus', group: '任课老师规则', text: '任课老师指定"上课校区与时段"', desc: '勾选后需在“员工管理”指定上课校区与时段，在排课“选任课老师”时只能排指定的校区与时段。' },
  { key: 'CheckClassSetWhenAddCourse', group: '课程与班级规则', text: '排课前需完善班级的"老师、教室、预招人数"信息', desc: '勾选后在“给班级排课”时，需要先完善班级的老师、教室、预招人数信息，否则不允许排课。' },
  { key: 'CheckClassSizeWhenArranging', group: '课程与班级规则', text: '班级未达到“最低开班人数”不允许排课', desc: '勾选后在“给班级排课”时，需要班级中的人数达到最低开班人数，否则不允许排课。' },
  { key: 'CourseTimeLimit', group: '课程与班级规则', text: '排课的“上课日期”不允许早于“开班日期”', desc: '勾选后在“给班级排课”时，排课的“上课日期”不允许早于班级的“开班日期”。' },
  { key: 'CheckCourseTimesOfShift', group: '课程与班级规则', text: '排课时不能超过课程的“计划排课数”', desc: '勾选后需先在课程详情设置“计划排课数”，排课时的数量将不允许超过“计划排课数”。' },
  { key: 'EnableOverCoursePost', group: '数量与费用规则', text: '1对1排课不允许超过购买数量', desc: '勾选后在“给1对1学员排课”时，排课的数量不允许超过购买数量。' },
  { key: 'EnableLessMoneyCourse', group: '数量与费用规则', text: '1对1排课不允许欠交排课', desc: '勾选后在“给1对1学员排课”时，如果学员在课程上有欠交的情况，则不允许排课。' },
  { key: 'MinCourseTime', group: '时间规则', text: '上课的“开始时间”不能早于“下方时间”', desc: '', isTime: true },
  { key: 'MaxCourseTime', group: '时间规则', text: '上课的“结束时间”不能晚于“下方时间”', desc: '', isTime: true },
]

// 计算：本次表格检查命中的规则码
const triggeredRuleKeys = computed(() => {
  const list = props.preCheckData?.CheckFieldList || []
  const codes = new Set()
  list.forEach(item => {
    ; (item.CheckerConfigList || []).forEach(code => codes.add(code))
  })
  return Array.from(codes)
})

// 计算：根据配置 + 命中规则，生成展示分组
const displayRuleGroups = computed(() => {
  const groupsMap = new Map()
  if (!courseRuleSetting.value) return []
  const cfg = courseRuleSetting.value
  RULE_DEFS.forEach(def => {
    let enabled = false
    let value = ''
    if (def.isTime) {
      value = cfg[def.key] || ''
      enabled = !!value
    } else {
      enabled = cfg[def.key] === 1 || cfg[def.key] === '1'
    }
    const isTriggered = triggeredRuleKeys.value.includes(def.key)
    if (enabled && isTriggered) {
      if (!groupsMap.has(def.group)) {
        groupsMap.set(def.group, [])
      }
      groupsMap.get(def.group).push({ key: def.key, text: def.text, desc: def.desc, isTime: !!def.isTime, value })
    }
  })
  return Array.from(groupsMap.entries()).map(([groupTitle, items]) => ({ groupTitle, items }))
})

// 加载排课限制规则配置：CourseRuleSetting
function loadCourseRuleSetting() {
  return querySysConfig({
    campusID: '',
    type: 0,
    configNames: 'CourseRuleSetting'
  }).then(res => {
    const list = res?.Data || []
    if (list.length > 0) {
      try {
        if (list[0].OtherParameter) {
          const parsed = JSON.parse(list[0].OtherParameter)
          courseRuleSetting.value = parsed || {}
        } else {
          courseRuleSetting.value = {}
        }
      } catch (e) {
        courseRuleSetting.value = {}
      }
    } else {
      courseRuleSetting.value = {}
    }
    return res
  })
}

// 暴露 open 方法：打开弹框并发起请求，返回 Promise（参考 chooseClass.vue）
function open(params) {
  const promise = new Promise((resolve, reject) => {
    _resolve = resolve
    _reject = reject
  })
  dialogVisible.value = true
  
  // 只有存在限制详情时才加载规则配置
  if (getRestrictionCount() > 0) {
    loadCourseRuleSetting()
      .then(() => {
        _resolve && _resolve({ config: courseRuleSetting.value, rules: triggeredRuleKeys.value })
      })
      .catch(err => {
        _reject && _reject(err)
      })
  } else {
    // 没有限制详情，直接resolve
    _resolve && _resolve({ config: null, rules: [] })
  }
  
  return promise
}

// 通过 <script setup> 暴露方法
// eslint-disable-next-line no-undef
defineExpose({ open })

// 方法
function handleClose() {
  dialogVisible.value = false
  _reject && _reject('closed')
}

function handleGoToModify() {
  // 关闭弹框
  dialogVisible.value = false

  // 触发前往修改事件
  emit('goToModify', {
    rowId: props.preCheckData.DraftId,
    // 可以传递第一个问题字段
    firstProblemField: getFirstProblemField()
  })
}

// 切换详细规则折叠状态
function toggleDetailedRules() {
  showDetailedRules.value = !showDetailedRules.value
}

function getFirstProblemField() {
  // 优先返回错误字段
  if (props.preCheckData?.ErrorFieldList?.length > 0) {
    return props.preCheckData.ErrorFieldList[0]
  }

  // 其次返回限制字段
  if (props.preCheckData?.CheckFieldList?.length > 0) {
    return props.preCheckData.CheckFieldList[0].FieldNameList[0]
  }

  // 最后返回冲突字段
  if (props.preCheckData?.ConflictFieldList?.FieldNameList?.length > 0) {
    return props.preCheckData.ConflictFieldList.FieldNameList[0]
  }

  return null
}

function getConflictCount() {
  const conflictData = props.preCheckData?.ConflictFieldList
  if (!conflictData) return 0

  let count = 0
  if (conflictData.ConflictingDraftList?.length > 0) count++
  if (conflictData.ConflictingCourseList?.length > 0) count++
  if (conflictData.ConflictingScheduleList?.length > 0) count++

  return count
}

function getRestrictionCount() {
  const checkData = props.preCheckData?.CheckFieldList
  if (!checkData) return 0

  return checkData.length
}

// 字段别名匹配（与主表一致）
function getFieldAliases(field) {
  const map = {
    MainTeacherName: ['MainTeacherID', 'MainTeacherList'],
    MainTeacherID: ['MainTeacherName', 'MainTeacherList'],
    ShiftName: ['ShiftID'],
    ClassRoomName: ['ClassRoomID'],
    timeRange: ['StartTime', 'EndTime'],
    // 新增别名映射，确保限制命中 ClassID 时高亮"上课班级"显示列
    ClassName: ['ClassID'],
    ClassID: ['ClassName'],
    // 完善校区双向别名，保证一致性
    CampusName: ['CampusID'],
    CampusID: ['CampusName'],
    // 添加助教字段的别名映射
    AssistantTeacherName: ['AssistantTeacherID', 'AssistantTeacherList'],
    AssistantTeacherID: ['AssistantTeacherName', 'AssistantTeacherList'],
    AssistantTeacherList: ['AssistantTeacherName', 'AssistantTeacherID'],
    // 添加学员字段的别名映射
    StudentUserName: ['StudentUserID'],
    StudentUserID: ['StudentUserName'],
  }
  const base = new Set([field])
  if (map[field]) map[field].forEach(f => base.add(f))
  return Array.from(base)
}

function isWarnField(field) {
  // 根据当前tab来决定显示哪些字段的警告
  const d = props.preCheckData || {}
  const aliasList = getFieldAliases(field)

  if (activeTab.value === 'restriction') {
    // 限制详情tab：只显示CheckFieldList中的字段警告
    return (d.CheckFieldList || []).some(c => (c.FieldNameList || []).some(f => aliasList.includes(f)))
  } else if (activeTab.value === 'conflict') {
    // 冲突详情tab：只显示ConflictFieldList中的字段警告
    return ((d.ConflictFieldList && d.ConflictFieldList.FieldNameList) || []).some(f => aliasList.includes(f))
  }

  return false
}

function getFieldMessage(field) {
  const d = props.preCheckData || {}
  const aliasList = getFieldAliases(field)

  if (activeTab.value === 'restriction') {
    // 限制详情tab：只返回CheckFieldList中的错误信息
    const checkItem = (d.CheckFieldList || []).find(c => (c.FieldNameList || []).some(f => aliasList.includes(f)))
    if (checkItem && checkItem.ErrorMessage) return checkItem.ErrorMessage
    return ''
  } else if (activeTab.value === 'conflict') {
    // 冲突详情tab：前端写死错误信息
    if (d.ConflictFieldList && (d.ConflictFieldList.FieldNameList || []).some(f => aliasList.includes(f))) {
      // 字段名称映射
      const fieldLabelMap = {
        CampusName: '上课校区',
        CampusID: '上课校区',
        ClassName: '上课班级',
        ClassID: '上课班级',
        StudentUserName: '上课学员',
        StudentUserID: '上课学员',
        ShiftName: '上课课程',
        ShiftID: '上课课程',
        Date: '上课日期',
        timeRange: '上课时间',
        StartTime: '上课时间',
        EndTime: '上课时间',
        ClassRoomName: '上课教室',
        ClassRoomID: '上课教室',
        MainTeacherName: '任课老师',
        MainTeacherID: '任课老师',
        MainTeacherList: '任课老师',
        AssistantTeacherName: '助教',
        AssistantTeacherID: '助教',
        AssistantTeacherList: '助教'
      }
      
      // 获取字段的显示名称
      const fieldLabel = fieldLabelMap[field] || '该字段'
      return transToConfigDescript(`${fieldLabel}：跟其他排课或日程有冲突`)
    }
    return ''
  }

  return ''
}

function shouldShowIcon(field) {
  // 根据当前tab来决定是否显示警告图标
  if (activeTab.value === 'restriction') {
    // 限制详情tab：只显示CheckFieldList中的字段图标
    const d = props.preCheckData || {}
    const aliasList = getFieldAliases(field)
    const inCheck = (d.CheckFieldList || []).some(c => (c.FieldNameList || []).some(f => aliasList.includes(f)))
    return inCheck && !!getFieldMessage(field)
  } else if (activeTab.value === 'conflict') {
    // 冲突详情tab：只显示ConflictFieldList中的字段图标
    const d = props.preCheckData || {}
    const aliasList = getFieldAliases(field)
    const inConflict = ((d.ConflictFieldList && d.ConflictFieldList.FieldNameList) || []).some(f => aliasList.includes(f))
    return inConflict && !!getFieldMessage(field)
  }

  return false
}

// 🆕 判断冲突行中某个字段是否冲突（使用该行自己的 ConflictFieldList）
function isConflictFieldInRow(row, field) {
  if (!row || !row.ConflictFieldList) return false
  const aliasList = getFieldAliases(field)
  return row.ConflictFieldList.some(f => aliasList.includes(f))
}

// 🆕 获取冲突字段的提示信息
function getConflictFieldMessage(field) {
  const fieldLabelMap = {
    CampusName: '上课校区',
    CampusID: '上课校区',
    ClassName: '上课班级',
    ClassID: '上课班级',
    StudentUserName: '上课学员',
    StudentUserID: '上课学员',
    ShiftName: '上课课程',
    ShiftID: '上课课程',
    Date: '上课日期',
    timeRange: '上课时间',
    StartTime: '上课时间',
    EndTime: '上课时间',
    ClassRoomName: '上课教室',
    ClassRoomID: '上课教室',
    MainTeacherName: '任课老师',
    MainTeacherID: '任课老师',
    MainTeacherList: '任课老师',
    AssistantTeacherName: '助教',
    AssistantTeacherID: '助教',
    AssistantTeacherList: '助教'
  }
  
  const fieldLabel = fieldLabelMap[field] || '该字段'
  return transToConfigDescript(`${fieldLabel}：跟其他排课或日程有冲突`)
}
</script>

<style lang="scss" scoped>
.precheck-drawer-content {
  height: calc(100% - 40px);
}

.restriction-details {
  padding: 12px 20px;
  background: #fff;
  height: 100%;
}

.conflict-details {
  padding: 12px 20px;
  background: #fff;
  /* 顶部白底 */
}

/* 冲突页下半区：采用灰底容器 */
.conflict-bottom {
  background: #F7F8FA;
  padding: 0 20px 12px 20px;
  margin: 0 -20px;
}

/* 下半区：限制内容开始，采用灰底容器 */
.restriction-bottom {
  background: #F7F8FA;
  padding: 12px 20px 12px 20px;
  margin: 0 -20px;
  height: calc(100% - 146px);
  /* 扩展到容器边缘，与顶部保持对齐 */
}

/* 规则说明块 */
.rule-tips-wrap {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  margin: 8px 0 14px 0;
}

.rule-tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.tips-title {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.tips-desc {
  color: #606266;
  font-size: 12px;
  line-height: 1.6;
}

.current-data-section {
  height: 146px;
  margin-bottom: 12px;
  background: #fff;
  /* 白色卡片 */
  border-radius: 6px;
}

/* 提示条为独立块，不混在卡片内 */
:deep(.page-attention-tips) {
  background: #fff;
  border-radius: 6px;
  padding: 8px 12px;
}

.section-title {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.current-data-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 限制内容样式 */
.restriction-content {
  margin-bottom: 12px;
  background: #fff;
  /* 白色卡片 */
  border-radius: 6px;
}

.restriction-content .section-title {
  margin: 0 0 15px 0;
}

.restriction-item {
  display: flex;
  margin-bottom: 12px;
  background: #F7F8FA;
  align-items: center;
  /* 条目灰底 */
  border-radius: 4px;
  padding: 10px 12px;
}

.restriction-number {
  color: #909399;
  font-size: 14px;
  min-width: 20px;
}

.restriction-message {
  color: #606266;
  font-size: 14px;
}

/* 查看规则链接 */
.view-rules-link {
  display: flex;
  align-items: center;
  color: #2878E8;
  font-size: 14px;
  cursor: pointer;
  // margin-top: 16px;
  padding: 8px 0;
}

.view-rules-link .caret-icon {
  margin-left: 4px;
  font-size: 12px;
  transition: transform 0.2s;
}

.view-rules-link .caret-icon.is-collapsed {
  transform: rotate(180deg);
}

.detailed-rules-content {
  margin-top: 16px;
  background: #fff;
  /* 白色卡片 */
  border-radius: 6px;
  padding: 12px;
}

/* 限制规则样式 */
.restriction-rules {
  margin-bottom: 30px;
}

.restriction-rules .section-title {
  margin: 0 0 15px 0;
}

.rule-item {
  display: flex;
  align-items: baseline;
  margin-bottom: 16px;
}

.rule-checkbox {
  margin-right: 6px;
}

.rule-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rule-title {
  color: #606266;
  font-size: 14px;
  font-weight: 400;
  margin-right: 16px;
}

.rule-description {
  color: #606266;
  font-size: 12px;
  line-height: 1.5;
}

/* 参考业务样式：灰色说明、小时间输入框一行显示 */
.sub-title {
  color: #909399;
}

.time-input-row {
  margin: 6px 0 0 0;
}

.time-input {
  width: 264px !important;
}

/* 冲突部分样式 */
.conflict-section {
  margin-bottom: 30px;
  background: #fff;
  /* 白色卡片 */
  border-radius: 6px;
  // padding: 12px;
}

.conflict-section .section-title {
  margin: 0 0 15px 0;
}

/* 老师名称样式 */
.teacher-name {
  display: flex;
  align-items: center;
  gap: 4px;
}

.warning-icon {
  color: #f56c6c;
  font-size: 12px;
  flex-shrink: 0;
  /* 防止图标被压缩 */
}

/* 增加内联小图标样式 */
.inline-warning {
  margin-left: 6px;
  color: #f56c6c;
  vertical-align: middle;
}

/* 表格单元格内容样式优化 */
:deep(.wtwo-table td) {
  padding: 8px 0;
  white-space: nowrap;
  /* 防止换行 */
  font-size: 14px;
}

/* 字段内容容器样式 */
.field-content {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  min-width: 0;
  /* 允许flex子项收缩 */
}

.field-text {
  flex: 0 1 auto;
  /* 不强制拉伸，允许收缩 */
  min-width: 0;
  /* 允许文字收缩 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.field-icon {
  flex-shrink: 0;
  /* 图标不被压缩 */
}

/* 表格样式优化 */
:deep(.wtwo-table) {
  border-radius: 4px;
}

:deep(.wtwo-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
  font-size: 14px;
}

:deep(.wtwo-table td) {
  padding: 8px 0;
  font-size: 14px;
}

/* 抽屉整体样式 */
:deep(.wtwo-drawer) {
  border-radius: 8px 0 0 8px;
}

:deep(.wtwo-drawer__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 20px 16px;
  margin-bottom: 0;
}

:deep(.wtwo-drawer__body) {
  padding: 20px;
}

:deep(.wtwo-tabs__header) {
  margin-bottom: 20px;
}

:deep(.wtwo-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.wtwo-tabs__item.is-active) {
  color: #2878E8;
}

:deep(.wtwo-tabs__active-bar) {
  background-color: #2878E8;
}

.highlight-cell {
  color: #f56c6c;
}
</style>