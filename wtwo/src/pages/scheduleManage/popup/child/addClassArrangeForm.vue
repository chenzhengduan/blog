<template>
    <el-scrollbar class="px-16px! addClassArrangeForm">
        <el-form :model="form" ref="formRef" label-position="top" :scroll-to-error="true">
            <div class="flex two-column-wrap">
                <el-form-item
                    prop="ClassID"
                    :label="transToConfigDescript('上课班级')"
                    class="half-width"
                    :rules="[
                        {
                            required: true,
                            message: transToConfigDescript('请选择班级')
                        },
                    ]"
                >
					<template #label>{{transToConfigDescript('上课班级')}}<span class="text-12px color-[#909399]">{{transToConfigDescript('（为集体班，一对多班级排课）')}}</span></template>
					<el-input v-if="props.isEdit" :model-value="form.className" disabled></el-input>
                    <input-tag
						v-else
                        v-model:value="className"
                        :selected="classSelected"
                        :multiple="false"
                        :searchable="true"
                        :api-config="classApiConfig"
                        placeholder="请选择或搜索"
                        @change="handleClassChange"
                        @choose="selectClass"
                    >
                        <template #btn-icon>
                            <el-icon size="20px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanban"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                        <template #option="{ option, isSelected }">
                            <input-tag-option
                                :option="option"
                                :isSelected="isSelected"
                                :label="option.Name"
                                iconColor="#FF8F1F"
                                iconShape="square"
                                iconText="班"
                            >
                                <template #content="{ option, label }">
                                    <div class="option-content">
                                        <span class="option-title ellipsis-single" :title="label">{{
                                            label
                                        }}</span>
                                        <el-tag
                                            v-if="
                                                option.IsFinished === 1
                                            "
                                            type="info"
                                            size="small"
                                            class="ml-8px"
                                            >已结业</el-tag
                                        >
                                    </div>
                                </template>
                            </input-tag-option>
                        </template>
                        <!-- 自定义 footer -->
                        <template #dropdown-footer>
                            <span class="switch-wrap">
                                <el-switch
                                    v-model="finished"
                                    :active-value="-1"
                                    :inactive-value="0"
                                    size="small"
                                ></el-switch>
                                <span class="switch-title"
                                    >包含结业{{transToConfigDescript('班级')}}</span
                                >
                            </span>
                        </template>
                    </input-tag>
                </el-form-item>
                <!-- IsOpen_OneClassMuitShift:是否开启一对多的班课关系。0或1 ，默认0。设置为1后，一个班级可以设置多个可上课课程，排课时可以统计这些上课课程。 -->
                <el-form-item
					v-if="IsOpen_OneClassMuitShift"
                    prop="ShiftID"
                    :label="transToConfigDescript('上课课程')"
                    class="half-width"
                    filterable
                    :rules="[
                        {
                            required: true,
                            message: transToConfigDescript('请选择课程'),
                        },
                    ]"
                >
					<div class="text-12px color-[#909399]" style="position: absolute;top: -35px;right: 0;">
                        <span v-if="ConsumeAmount*1 !== 1">该{{transToConfigDescript('课程')}}每排1次课，计{{ConsumeAmount}}{{transToConfigDescript("次")}}课消。</span>
                    </div>
					<el-input v-if="props.isEdit" disabled :model-value="shiftName"></el-input>
                    <el-select v-else v-model="form.ShiftID" :disabled="!form.ClassID" :placeholder="transToConfigDescript(!form.ClassID?'请先选“上课班级”':'请选择')" @change="changeShift">
                        <el-option v-for="item in shiftList" :value="item.ID" :label="item.Name"></el-option>
                    </el-select>
                </el-form-item>
				<!-- 全科课程展示 -->
				<el-form-item prop="SubjectID" :label="transToConfigDescript('上课科目')" v-if="EnableSubject" :rules="[{required: true,message: '请选择科目'}]" class="half-width">
					<el-select v-model="form.SubjectID" placeholder="请选择" filterable @change="changeSubject">
						<el-option v-for="item in subjectList" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
					</el-select>
				</el-form-item>
            </div>
            <div class="flex two-column-wrap">
                <el-form-item label="排课规则" class="half-width" prop="CourseDateList" :rules="[{required: planRule==0,message: transToConfigDescript('请选择上课日期')}]">
					<!-- 这个仅前端用 后端没得用 -->
					<div class="text-12px color-[#909399]" style="position: absolute;top: -35px;right: 0;">
                        <el-radio-group v-model="planRule" @change="changePlanRule">
							<el-radio :value="1">重复排课</el-radio>
							<el-radio :value="0">自由排课</el-radio>
						</el-radio-group>
                    </div>
					<el-select v-if="planRule==1" v-model="form.CourseMode" placeholder="请选择" filterable> 
						<el-option value="Weekly" label="每周重复"></el-option>
						<el-option value="Biweekly" label="隔周重复"></el-option>
						<el-option value="Daily" label="每天重复"></el-option>
						<el-option value="AlternateDay" label="隔天重复"></el-option>
					</el-select>
					<!-- 自由排课 -->
					<multiple-dates-picker
						v-if="planRule==0"
                        v-model="form.CourseDateList"
                        @cleared="onCourseDateListCleared"
						placeholder="请选择上课日期"
                    />
                </el-form-item>
            </div>
            <div class="flex two-column-wrap">
                <!-- 重复排课 -->
                <el-form-item
                    label="重复范围"
                    class="half-width"
                    :rules="[{required: true,message: '请选择开始日期'}]"
                    v-if="planRule==1"
                >
                    <div class="flex-center" style="position: absolute;top: -35px;right: 0;">
                        <el-checkbox v-model="form.IsHoliday" :true-value="1" :false-value="0">跳过节假日</el-checkbox>
                        <el-popover
                            class="box-item"
                            placement="top"
							width="300"
                        >
                            <template #reference>
                                <el-link class="ml-10px" type="primary" underline="never">查看</el-link>
                            </template>
                            <div style="max-height: 200px;overflow-y: auto;">
                                <span class="text-12px color-[#909399]" v-if="!form.StartDate||!form.EndDate">请选择开始、结束日期</span>
								<template v-if="form.StartDate&&form.EndDate">
									<span class="text-12px color-[#909399]" v-if="holidays.length==0">此时段内无节假日</span>
									<div v-for="item in holidays" class="popover-holiday-item py-3px">
										<span>{{item.Date}}</span>
										<span class="ml-10px text-12px color-[#909399]">{{item.Name}}</span>
									</div>
								</template>
                            </div>
                        </el-popover>
                        
                    </div>
                    <el-form-item prop="StartDate" :rules="[{required: true,message: '请选择开始日期'}]" style="flex: 1;">
                        <el-date-picker v-model="form.StartDate" type="date" :disabled="!props.isEdit&&EnableSynchronizeClassPlan && CreatedCourseTimes === 0"
                            placeholder="请选择" value-format="YYYY-MM-DD" @change="changeStartDate" class="w-100%!"></el-date-picker>
                    </el-form-item>
                    <span class="mx-10px">至</span>
                    <el-form-item prop="EndDate" :rules="[{ required: true, message: '请选择结束日期' }, { validator: validateEndDate }]" style="flex: 1;">
                        <el-date-picker v-model="form.EndDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" 
                            :disabled="!props.isEdit&&EnableSynchronizeClassPlan && CreatedCourseTimes === 0" class="w-100%!"></el-date-picker>
                    </el-form-item>
                </el-form-item>
            </div>
			<div class="flex-center color-[var(--wtwo-text-color-regular)] mb-8px">
				{{transToConfigDescript('上课时间')}}
				<PageAttentionTips v-if="!props.isEdit&&((classSelected.length&&classPlanList.length)||lastPlanList.length)" class="ml-8px">
					你可能需要复用的排课信息
					<el-popover
						placement="right"
						:width="520"
						trigger="click"
						v-model:visible="reusablePlanPopoverVisible"
						content="this is content, this is content, this is content"
					>
						<template #reference>
							<el-link type="primary" underline="never" class="ml-6px" style="vertical-align: inherit;">查看</el-link>
						</template>
						<div>
							<template v-if="classSelected.length&&classPlanList.length">
								<div class="flex-between mb-8px">
									<div class="flex-center">班级预设
										<el-tag v-if="classPlanInfo.EnableSubject" type="info" effect="light" round class="ml-8px">全科课程</el-tag>
									</div>
									<el-button type="primary" plain size="small" @click="useClassPlan">使用</el-button>
								</div>
								<el-table :data="classPlanList" class="mb-16px" max-height="160px" :scrollbar-always-on="true">
									<el-table-column label="星期" width="70px" prop="Weekday">
										<template #default="scope">
											{{weekList.find(i=>i.ID==scope.row.Weekday)?.Name}}
										</template>
									</el-table-column>
									<el-table-column :label="transToConfigDescript('上课时间')" prop="StartTime" width="100px">
										<template #default="scope">
											{{scope.row.TimeRange[0]}}-{{scope.row.TimeRange[1]}}
										</template>
									</el-table-column>
									<el-table-column label="教室" prop="ClassroomName" min-width="100px" show-overflow-tooltip></el-table-column>
									<template v-if="!classPlanInfo.EnableSubject">
										<el-table-column :label="transToConfigDescript('任课老师')" min-width="100px" show-overflow-tooltip prop="Teachers">
											<template #default="scope">
												{{scope.row.Teachers&&scope.row.Teachers.length>0?scope.row.Teachers[0].LabelName:''}}
											</template>
										</el-table-column>
										<el-table-column label="助教" min-width="100px" show-overflow-tooltip prop="Assistants">
											<template #default="scope">
												{{scope.row.Assistants&&scope.row.Assistants.length>0?scope.row.Assistants.map((i:any)=>i.LabelName).join(', '):''}}
											</template>
										</el-table-column>
									</template>
								</el-table>
							</template>
							<template v-if="lastPlanList.length">
								<div class="flex-between mb-8px">
									<div class="flex-center">最近一次排课
										<el-tag v-if="lastInfo.EnableSubject" type="info" effect="light" round class="ml-8px">全科课程</el-tag>
										<el-tag v-if="lastInfo.SubjectID" type="info" effect="light" round class="ml-8px">{{ lastInfo.SubjectName }}</el-tag>
									</div>
									<el-button type="primary" plain size="small" @click="useLastPlan">使用</el-button>
								</div>
								<el-table :data="lastPlanList" max-height="160px" :scrollbar-always-on="true">
									<el-table-column v-if="lastInfo.weekVisible" label="星期" prop="WeekName" width="70px"></el-table-column>
									<el-table-column :label="transToConfigDescript('上课时间')" prop="StartTime" width="100px">
										<template #default="scope">
											{{scope.row.TimeRange[0]}}-{{scope.row.TimeRange[1]}}
										</template>
									</el-table-column>
									<el-table-column label="教室" prop="ClassroomName" min-width="100px" show-overflow-tooltip></el-table-column>
									<el-table-column :label="transToConfigDescript('任课老师')" min-width="100px" show-overflow-tooltip prop="Teachers">
										<template #default="scope">
											{{scope.row.Teachers&&scope.row.Teachers.length>0?scope.row.Teachers[0].LabelName:''}}
										</template>
									</el-table-column>
									<el-table-column label="助教" min-width="100px" show-overflow-tooltip prop="Assistants">
										<template #default="scope">
											{{scope.row.Assistants&&scope.row.Assistants.length>0?scope.row.Assistants.map((i:any)=>i.LabelName).join(', '):''}}
										</template>
									</el-table-column>
								</el-table>
							</template>
						</div>
					</el-popover>
				</PageAttentionTips>
			</div>
            <el-form-item prop="PlanList">
				<div class="text-12px color-[#909399] flex-center" style="position: absolute;top: -35px;right: 0;">
					<div class="text-12px color-[var(--wtwo-color-warning)] mr-10px">
						<span v-if="CreatedCourseTimes>0">
							当前{{transToConfigDescript('班级')}}已排课{{CreatedCourseTimes }}{{Unit==1?"小时":"次"}}
							<span v-if="EnableConsumeMinutesToTimes&&isMinutesToTimes">，每次{{StandardMinutes}}分钟</span>
						</span>
						<span v-if="CreatedCourseTimes<=0&&EnableConsumeMinutesToTimes&&isMinutesToTimes">每次{{StandardMinutes}}分钟</span>
					</div>
					<el-form-item
						prop="CourseTimes"
						class="w-300px"
						:show-message="false"
						:rules="{validator:validCourseTimes,trigger:'change'}"
					>
						<el-input v-model="form.CourseTimes" :placeholder="planRule==1?'“不填”则按重复范围排满':'“不填”则按所选日期排满'" style="width: 100%!important;line-height: 28px!important;height: 28px;" :formatter="inputFloatFormat">
							<template #prepend>最多排</template>
							<template #append>{{Unit==1?'小时':'次'}}</template>
						</el-input>
					</el-form-item>
				</div>
                <el-table :data="form.PlanList">
					<template #empty>
						<div></div>
					</template>
                    <el-table-column prop="op" width="24px" column-key="op">
                        <template #default="scope">
                            <div class="cursor-pointer flex-center" title="移除">
                                <el-icon size="16px" @click="delPlan(scope.$index)" color="#F56C6C"><RemoveFilled /></el-icon>
                            </div>
                        </template>
                    </el-table-column>
					<!-- 冲突提示列 -->
                    <el-table-column width="24px" align="center" column-key="conflict">
                        <template #default="scope">
							<div v-if="planListConflicts[scope.$index]" class="flex-center" :title="transToConfigDescript('该行与列表中的其他行上课时间有冲突！')">
                                <el-icon size="16px" color="#E6A23C" ><Warning /></el-icon>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column v-if="(form.CourseMode=='Weekly'||form.CourseMode=='Biweekly')&&planRule==1" :label="transToConfigDescript('上课星期')" width="100px" column-key="Weekday">
						<template #header>
							{{transToConfigDescript('上课星期')}}<span class="color-[#f56c6c] ml-4px">*</span>
						</template>
                        <template #default="scope">
                            <el-form-item :prop="'PlanList.'+scope.$index+'.Weekday'" :rules="[{required:true,message:'请选择'}]" label-width="0" class="mb-0!">
                                <el-select v-model="scope.row.Weekday" @change="handleWeekdayChange(scope.row)">
                                    <el-option v-for="item in weekList" :value="item.ID" :label="item.Name"></el-option>
                                </el-select>
                            </el-form-item>
                        </template>
                    </el-table-column>
                    <el-table-column :label="transToConfigDescript('上课时间')" width="230px" column-key="TimeRange">
						<template #header>
							{{transToConfigDescript('上课时间')}}<span class="color-[#f56c6c] ml-4px">*</span>
						</template>
                        <template #default="scope">
                            <el-form-item :prop="'PlanList.'+scope.$index+'.TimeRange'" :rules="[{required:true,message:'请选择'},{ validator: validateRowTimeRange }]" label-width="0" class="mb-0!">
                                <course-time-range
                                v-model="scope.row.TimeRange"
                                :campus-id="selectedCampusId||currentCampus"
                                @change="handleTimeRangeChange(scope.row)"
								:min-course-time="MinCourseTime"
								:max-course-time="MaxCourseTime"
                            />
                            </el-form-item>
                        </template>
                    </el-table-column>
                    <el-table-column :label="transToConfigDescript('上课教室')" width="200px" column-key="ClassroomID">
						<template #header>
							<div class="flex-center">
								<el-dropdown class="batch-dropdown" trigger="click">
									<div class="flex-center w-[100%]! color-[#909399]" style="line-height: 23px;">
										<div>{{transToConfigDescript('上课教室')}}</div>
										<el-icon class="wtwo-icon--right"><CaretBottom /></el-icon>
									</div>
									<template #dropdown>
										<el-dropdown-menu>
											<el-dropdown-item @click.native="batchEditClassroom">批量修改</el-dropdown-item>
										</el-dropdown-menu>
									</template>
								</el-dropdown>
								<span class="color-[#f56c6c] ml-4px">*</span>
								<el-tooltip
									class="box-item"
									effect="dark"
									placement="top"
									content="如果是线上课，非必填！"
								>
									<el-icon
										size="16px"
										class="ml-4px"
										color="#909399"
									>
										<svg aria-hidden="true">
											<use
												xlink:href="#w2-xinxitishi"
											></use>
										</svg>
									</el-icon>
								</el-tooltip>
							</div>
                        </template>
                        <template #default="scope">
                            <el-form-item :prop="'PlanList.'+scope.$index+'.ClassroomID'" :rules="[{required:scope.row.CourseType==1,message:'请选择'}]" label-width="0" class="mb-0!">
								<classroom-select
                                    v-model="scope.row.ClassroomID"
                                    :campus-id="currentCampus"
                                    :initial-data="scope.row.ClassroomID ? { ID: scope.row.ClassroomID, Name: scope.row.ClassroomName } : undefined"
                                    :customParams="getFreeParams('classroom',scope.row)"
									:start-time="scope.row.TimeRange?.[0] || ''"
                                    :end-time="scope.row.TimeRange?.[1] || ''"
                                    :course-type="scope.row.CourseType"
                                    placeholder="请选择"
                                    @change="(val:string,obj:any)=>{handleClassroomChange(obj,scope.row)}"
									class="w-100%"
                                ></classroom-select>
                            </el-form-item>
                        </template>
                    </el-table-column>
                    <el-table-column :label="transToConfigDescript('任课老师')" column-key="Teachers">
						<template #header>
                            <el-dropdown class="batch-dropdown" trigger="click">
                                <div class="flex-center w-[100%]! color-[#909399]" style="line-height: 23px;">
                                    <div>{{transToConfigDescript('任课老师')}}</div>
									<el-icon class="wtwo-icon--right"><CaretBottom /></el-icon>
                                </div>
                                <template #dropdown>
                                    <el-dropdown-menu>
                                        <el-dropdown-item @click.native="batchEditTableTeacher">批量修改</el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
							<span v-if="courseTeacherRequired" class="color-[#f56c6c] ml-4px">*</span>
                        </template>
                        <template #default="scope">
                            <el-form-item :prop="'PlanList.'+scope.$index+'.Teachers'" :rules="[{required:courseTeacherRequired,message:'请选择'}]" label-width="0" class="mb-0!">
                                <input-tag
									v-if="EnableClassCommission"
                                    placeholder="请选择"
                                    :selected="scope.row.Teachers"
                                    :isLine="true"
                                    :multiple="false"
                                    @click="selectTableTeacher(scope.row)"
									:fieldMapping="{
										label: 'LabelName',
										value: 'ID',
										key: 'ID'
									}"
									class="flex-1"
                                >
                                    <template #btn-icon>
                                        <el-icon size="18px">
                                            <svg aria-hidden="true">
                                                <use
                                                    xlink:href="#w2-xuanren"
                                                ></use>
                                            </svg>
                                        </el-icon>
                                    </template>
                                </input-tag>
								
                                <TeacherSelect
                                    v-else
                                    :model-value="(scope.row.Teachers && scope.row.Teachers.length)>0 ? scope.row.Teachers[0].ID : ''"
                                    :initial-data="{ ID: scope.row.Teachers?.[0]?.ID, Name: scope.row.Teachers?.[0]?.Name }"
									:customParams="getFreeParams('teacher',scope.row)"
                                    placeholder="请选择"
                                    :start-time="scope.row.TimeRange?.[0] || ''"
                                    :end-time="scope.row.TimeRange?.[1] || ''"
                                    :SubjectIDList="EnableSubject&&form.SubjectID ? [form.SubjectID] : []"
                                    :shiftSubjectId="shiftSubjectId"
									:EnableSubject="EnableSubject"
									:ShiftID="form.ShiftID"
									:cell-mode="true"
                                    :disabled-ids="scope.row.Assistants ? scope.row.Assistants.map((item: any) => item.ID) : []"
                                    @change="(val: any, teacher: any) => onTeacherChange(scope.row, val, teacher)"
                                    @clear="() => onTeacherChange(scope.row, '', null)"
                                    class="flex-1"
                                />
								<el-tooltip
									class="box-item"
									effect="dark"
									placement="top-start"
									v-if="EnableAssignTeacherCourseCampus&&scope.row.Teachers.length&&scope.row.teacherScheduleTime"
								>
									<el-icon  size="16px" class="ml-4px" color="#909399">
										<svg aria-hidden="true">
											<use xlink:href="#w2-xinxitishi"></use>
										</svg>
									</el-icon>
									<template #content>{{ scope.row.teacherScheduleTime }}</template>
								</el-tooltip>
                            </el-form-item>
                        </template>
                    </el-table-column>
                    <el-table-column label="助教" column-key="Assistants">
						<template #header>
                            <el-dropdown class="batch-dropdown" trigger="click">
                                <div class="flex-center w-[100%]! color-[#909399]" style="line-height: 23px;">
                                    <div>助教</div>
									<el-icon class="wtwo-icon--right"><CaretBottom /></el-icon>
                                </div>
                                <template #dropdown>
                                    <el-dropdown-menu>
                                        <el-dropdown-item @click.native="batchEditTableAssistant">批量修改</el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
                        </template>
                        <template #default="scope">
                            <input-tag
                                v-if="EnableClassCommission"
                                placeholder="请选择"
                                :selected="scope.row.Assistants"
                                :isLine="true"
                                :multiple="true"
                                @click="selectTableAssistant(scope.row)"
								:fieldMapping="{
									label: 'LabelName',
									value: 'ID',
									key: 'ID'
								}"
                            >
                                <template #btn-icon>
                                    <el-icon size="18px">
                                        <svg aria-hidden="true">
                                            <use
                                                xlink:href="#w2-xuanren"
                                            ></use>
                                        </svg>
                                    </el-icon>
                                </template>
                            </input-tag>
                            <AssistantSelect
                                v-else
                                :model-value="scope.row.Assistants ? scope.row.Assistants.map((item: any) => item.ID) : []"
                                :initial-data="scope.row.Assistants"
								:customParams="getFreeParams('assistant',scope.row)"
								:start-time="scope.row.TimeRange?.[0] || ''"
                                :end-time="scope.row.TimeRange?.[1] || ''"
                                placeholder="请选择助教"
								:disabled-ids="scope.row.Teachers ? scope.row.Teachers.map((item: any) => item.ID) : []"
								:cell-mode="true"
                                @change="(val: any, assistants: any) => onAssistantChange(scope.row, val, assistants)"
                                @clear="() => onAssistantChange(scope.row, [], [])"
                                class="w-[100%]"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column v-if="EnableStore&&EnableCourseOnline" label="线上课" width="80px" align="center" column-key="CourseType">
                        <template #header>
                            <div class="flex-center impower-col-title" style="justify-content: center;">
                                线上课
                                <el-tooltip
                                    class="box-item"
                                    effect="dark"
                                    placement="top"
                                    v-if="IsOpenLiveStream==1"
                                >
                                    <template #content>
                                        <div class="flex-center">
                                            <span>{{transToConfigDescript('设为在线课后，老师可以直接进入直播间，进行远程教学。')}}</span>
                                            <el-link type="primary" underline="never" class="text-12px!" href="https://helpcenter.xiaogj.com/help-list/detail-638.html" target="_blank">详细介绍</el-link>
                                        </div>
                                    </template>
                                    <el-icon
                                        size="16px"
                                        class="ml-4px"
                                        color="#909399"
                                    >
                                        <svg aria-hidden="true">
                                            <use
                                                xlink:href="#w2-xinxitishi"
                                            ></use>
                                        </svg>
                                    </el-icon>
                                </el-tooltip>
                            </div>
                        </template>
                        <template #default="scope">
                            <el-checkbox v-model="scope.row.CourseType" :true-value="2" :false-value="1" @change="changeCourseType(scope.row)"></el-checkbox>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="flex-center pt-5px pb-5px" style="background-color: #F9FAFC;width: 100%;">
                    <el-link type="primary" underline="never" @click="addPlan" class="ml-6px">
                        <div class="flex-center">
                            <el-icon size="16px" class="mr-8px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-tianjia"
                                    ></use>
                                </svg>
                            </el-icon>
                            <span>添加时段</span>
                        </div>
                    </el-link>
                </div>
                

            </el-form-item>
            <!-- 小银星定制：增加了几种老师角色 替课老师 跟课老师 钢伴老师 坐班老师 课程顾问-->
            <div class="flex two-column-wrap" v-if="EnableCourseRole&&isOneToOne==0">
                <el-form-item prop="" label="替课老师" class="half-width">
                    <input-tag
                        placeholder="请选择"
                        :selected="replaceTeacherList"
                        :isLine="true"
                        @click="selectTeacher('replace')"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </el-form-item>
                <el-form-item prop="" label="跟课老师" class="half-width">
                    <input-tag
                        placeholder="请选择"
                        :selected="followTeacherList"
                        :isLine="true"
                        :multiple="true"
                        @click="selectTeacher('follow')"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </el-form-item>
                <el-form-item prop="" label="钢伴老师" class="half-width">
                    <input-tag
                        placeholder="请选择"
                        :selected="pianoTeacherList"
                        :isLine="true"
                        @click="selectTeacher('piano')"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </el-form-item>
                <el-form-item prop="" label="坐班老师" class="half-width">
                    <input-tag
                        placeholder="请选择"
                        :selected="officeTeacherList"
                        :isLine="true"
                        :multiple="true"
                        @click="selectTeacher('office')"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </el-form-item>
                <el-form-item prop="" label="课程顾问" class="half-width">
                    <input-tag
                        placeholder="请选择"
                        :selected="consultantTeacherList"
                        :isLine="true"
                        :multiple="true"
                        @click="selectTeacher('consultant')"
                    >
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-xuanren"
                                    ></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </el-form-item>
            </div>
            <el-form-item v-if="NewCourse_SelectCoursePlan" prop="" label="开放预约" label-position="left">
				<div class="flex w-[100%]">
					<el-switch v-model="form.IsSubscribeCourse" :active-value="1" :inactive-value="0"></el-switch>
				</div>
				<div v-if="form.IsSubscribeCourse==1" class="mt-5px text-12px color-[#909399] w-[100%] line-height-16px!">
					1、开放预约后，学员可以自助在师生信-约课模块预约排课；<br/>
					{{transToConfigDescript('2、可约人数=班级预招人数-在课人数；')}}<br/>
					{{transToConfigDescript('3、临近上课时间，将不允许预约（临近的时间长度，在学员约课规则中设置）')}}
				</div>
            </el-form-item>
			
            <div class="flex two-column-wrap">
                <el-form-item
                    prop=""
                    label="对内备注"
                    class="half-width"
                >
                    <el-input v-model="form.InternalRemark" type="textarea" :rows="2" placeholder="请输入" maxlength="3000"></el-input>
                </el-form-item>
                <!-- <el-form-item
                    prop=""
                    label="对外备注"
                    class="half-width"
                >
                    <el-input v-model="form.Describe" type="textarea" :rows="2" placeholder="请输入" maxlength="3000"></el-input>
                </el-form-item> -->
            </div>
        </el-form>
        <chooseClass ref="chooseClassRef"></chooseClass>
		<chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
		<BatchEditClassroom ref="batchEditClassroomRef"></BatchEditClassroom>
		<BatchEditTeacher ref="batchEditTeacherRef"></BatchEditTeacher>
		<BatchEditAssistant ref="batchEditAssistantRef"></BatchEditAssistant>
    </el-scrollbar>
</template>
<script lang="ts" setup>
import { getShiftInfo, queryClass, queryHoliday, querySysConfig } from '@/api';
import { getClass, getCoursePlanTimeReusableInfot, getTeachersCourseTimes } from '@/api/arrange';
import { useConfigs, useCurrentCampuses } from '@/store';
import { useDictFieldsStore } from '@/store/dict';
import { CourseTimetableTypeEnum } from '@/types/model/timetable-preference';
import type { ApiConfig } from '@/components/common/input-tag/input-tag.d.ts';
import { FormInstance } from 'element-plus';
import { cloneDeep } from 'lodash';
import { storeToRefs } from 'pinia';
import { computed, onMounted, ref, watch, nextTick } from 'vue';
import { calculateDuration } from '@/utils/timeUtils';
import { calculateScheduleCount, calculateRepeatedScheduleInfo, generateScheduleConfirmMessage, checkTeacherType, checkPlanListConflicts } from '@/utils/scheduleUtils';
import { RemoveFilled, CaretBottom, Warning } from '@element-plus/icons-vue';
import { dayjs } from 'element-plus'

import CourseTimeRange from '../../components/course-time-range.vue';
import MultipleDatesPicker from '../../components/multiple-dates-picker.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { transToConfigDescript, inputFloatFormat } from '@/utils/filters/filters';
import { Class_classifyTeachersBySubject } from '@/utils/index';
import BatchEditClassroom from '../batchEditClassroom.vue';
import BatchEditTeacher from '../batchEditTeacher.vue';
import BatchEditAssistant from '../batchEditAssistant.vue';
import TeacherSelect from '@/components/business/select/teacher-select.vue'
import AssistantSelect from '@/components/business/select/assistant-select.vue'
import classroomSelect from '@/components/business/select/classroom-select.vue';
import { i } from 'vite/dist/node/types.d-aGj9QkWt';
// props 接收一个校区id 与 激活标记
const props = defineProps({
	selectedCampusId: {
		type: String,
		default: ''
	},
	activated: {
		type: Boolean,
		default: false
	},
	isEdit: {
        type: [Boolean, Number, String],
        default: false
    },
	onlyEmitData:{
		type:Boolean,
		default:false
	},
	disabledAutoFillPlan:{//双击新增排课，不要自动填充排课计划
		type:Boolean,
		default:false
	}
})
const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);
const currentCampus = computed(() => {
	// if(props.selectedCampusId){
		// 如果有选中的校区ID，直接返回该ID
		return props.selectedCampusId
	// }	
	// return useCurrentCampuses().campusList
})

const hasInited = ref(false)

watch(() => props.selectedCampusId, (newVal) => {
	// if(newVal){
		form.value.CampusID=newVal
	// }else{
	// 	form.value.CampusID=currentCampus.value
	// }
})

const SUBJECT=computed(()=>{
    return dictFields.value('SUBJECT').filter((item:any)=>item.Status==1);
})

const configs = computed(() => {
	return useConfigs().configs
})

// 冲突校验相关的计算属性
const planListConflicts = computed(() => {
	return checkPlanListConflicts(
		form.value.PlanList,
		form.value.CourseMode,
		planRule.value,
		CheckAssistantConflict.value
	);
});

//权限
const NewCourse_SelectCoursePlan = window.$xgj.op('NewCourse_SelectCoursePlan') //班级排课开放预约

//配置项
const EnableConsumeMinutesToTimes=computed(()=>{ //是否启用按次收费，按时长计费的功能（将时长转换为次数）：1是，0否（默认）
	return configs.value.EnableConsumeMinutesToTimes==1
})
// const EnableMonthShiftCourse=computed(()=>{ //按月计费课程是否支持手动排课出勤不计费：0否，1是（白塔岭定制）
// 	return configs.value.EnableMonthShiftCourse==1
// })
const IsOpenShiftForDay=computed(()=>{ //开启按天计费或者按月计费功能：0按月计费（默认），1按天计费。注意:不能来回切换配置项，只能从按月计费切到按天计费，不能从按天计费切换到按月计费（会产生错误数据，无法修复）。
	return configs.value.IsOpenShiftForDay==1
})
const EnableMustSameSubjectTeacherCourse=computed(()=>{ //是否开启限制跨科目选择老师 0允许（默认） 1不允许
	return configs.value.EnableMustSameSubjectTeacherCourse==1
})
const CheckClassSetWhenAddCourse=computed(()=>{ //【启橙】排课之前是否检查班级的老师、教室、预招人数设置：1是（未设置完整则不可以排课），0否（默认）
	return configs.value.CheckClassSetWhenAddCourse==1
})
const IsOpen_OneClassMuitShift=computed(()=>{ //是否开启一对多的班课关系。0或1 ，默认0。设置为1后，一个班级可以设置多个可上课课程，排课时可以统计这些上课课程。
	return configs.value.IsOpen_OneClassMuitShift==1
})
const EnableSynchronizeClassPlan=computed(()=>{ //班级计划开班时间是否同步至排课开始日期中，且不能修改;计划结业时间是否同步至排课结束日期，且可修改，0表示否（默认），1 表示是（昂立）
	return configs.value.EnableSynchronizeClassPlan==1
})
const EnableCourseRole=computed(()=>{ //是否开启排课角色。0：不开启（默认），1：开启
	return configs.value.EnableCourseRole==1
})
const CourseIsClassSubject=computed(()=>{ //全科课程排课，是否只能选班级上设置的科目 0：否（默认），1：是
	return configs.value.CourseIsClassSubject==1
})
const EnableStore=computed(()=>{ //是否开启更多功能菜单,1:显示更多功能的菜单
	return configs.value.EnableStore==1
})
const EnableCourseOnline=computed(()=>{ //是否开启在线课堂菜单，1：开通（开通后，才显示在线课堂菜单）
	return configs.value.EnableCourseOnline==1
})
const CheckClassSizeWhenArranging=computed(()=>{ //是否开启排课前检查班级人数的功能：0不开启（默认）；1开启，开启后排课前会检查班级人数，若人数不达标则不能排课。班级可排课的最低人数标准是否可以修改由权限控制
	return configs.value.CheckClassSizeWhenArranging==1
})
const IsOpenLiveStream=computed(()=>{ //开启直播教学服务。0:不开启（默认），1：声网。2：classin（自动同步方式）。3：classin(手动同步方式,通过zb.xiaogj.com进行同步)。
	return configs.value.IsOpenLiveStream
})
const EnableClassOpeningStatus=computed(()=>{ //是否开启设置班级开班状态的功能：0否（默认）；1是，开启后班级信息中会增加班级状态的属性，状态包括已开班和待定班，只有已开班状态的班级可以进行排课。并且系统会增加修改班级开班状态的权限，拥有此权限的人才可以对班级状态进行更改。
	return configs.value.EnableClassOpeningStatus==1
})

const EnableAssignTeacherCourseCampus = computed(()=>{ //是否开启指派老师上课校区功能，0否（默认），1（是，丽音艺术定制）
	return configs.value.EnableAssignTeacherCourseCampus==1
})

const EnbaleEmpIsClassTeacher=computed(()=>{
    return configs.value.EnbaleEmpIsClassTeacher
})

const EnableConsumeSelf=computed(()=>{ //上课点名是否只能课消本校区的课程费用 0否(默认)，1是
	return configs.value.EnableConsumeSelf==1
})
const EnableCourseConfirmWhenLess=computed(()=>{ //学员欠费时，是否可以点名上课：1是（默认），0：可以出勤但不能计费，2：不可出勤也不可计费，3：不可出勤也不可计费，根据学员收据的实缴费用来计算的可上课点名课时，只能上到已缴费用的课时，与学生的课程费用是否交清有关,4：在3的基础上新增：点名时，勾选了“试听”，支持勾选出勤。
	return configs.value.EnableCourseConfirmWhenLess
})

const EnableClassCommission=computed(()=>{
    return configs.value.EnableClassCommission==1
})
const Check_Shift_Teacher_Subject=ref(false) //排课时的课程科目与老师可授课科目不一致时，是否显示提示  业务规则
const courseTeacherRequired=ref(false) //排课时任课老师是否必填  业务规则
const CheckAssistantConflict=ref(false) //是否检查助教冲突  业务规则

const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
const finished = ref(0)
const defaultForm: any = {
	ShiftID: '', //课程ID
	CampusID: '', //校区ID
	IsHoliday: 0, //跳过节假日：0-不跳过，1-跳过
	PlanID: EMPTYGUID, //排课计划ID
	SubjectID: '', //科目ID uuid
	CourseMode: 'Weekly', //重复规则 Free Weekly Biweekly Daily AlternateDay
	StartDate: '', //排课开始日期
	EndDate: '', //排课结束日期
	CourseTimes: '', //课时数/课次数
	CourseDateList: [], //自由排课的日期列表
	PlanList: [], //排课计划
	ClassID: '', //班级ID
	IsSubscribeCourse: 0, //是否为开放预约
	InternalRemark: '', //对内备注
	Describe: '', //对外备注
}
const form = ref<typeof defaultForm>(cloneDeep(defaultForm))
const className = ref('')
const classSelected = ref([] as any)
const shiftList=ref([] as any)
const EnableSubject=ref(false) //是否为全科课程
const subjectList=ref([] as any) //科目
const planRule=ref(1) //排课规则 1重复排课 0自由排课
const replaceTeacherList=ref([] as any)//替课老师 role:3
const followTeacherList=ref([] as any)//跟课老师 role:4
const pianoTeacherList=ref([] as any)//钢伴老师 role:5
const officeTeacherList=ref([] as any)//坐班老师 role:6
const consultantTeacherList=ref([] as any)//课程顾问 role:7
const isOneToOne=ref(0)

// 编辑态下用于展示的课程名称
const shiftNameEdit = ref('')

// 计算属性：课程名称
const shiftName = computed(() => {
    if (props.isEdit) {
        return shiftNameEdit.value || ''
    }
    if (!form.value.ShiftID || !shiftList.value.length) {
        return ''
    }
    const shift = shiftList.value.find((item: any) => item.ID === form.value.ShiftID)
    return shift ? shift.Name : ''
})

// const showHoliday=computed(()=>{
// 	// return !EnableMonthShiftCourse.value&&!(!EnableMonthShiftCourse.value&&!IsOpenShiftForDay.value&&Unit.value==3)
// 	return Unit.value!=3
// })
	
const holidays=ref([] as any)
const CreatedCourseTimes=ref(0)
const Unit=ref(0)
const isMinutesToTimes=ref(false)//是否为按时长计费
const StandardMinutes=ref(0)//标准时长
const ConsumeAmount=ref(1)//课消

let teacherSubjectObj:any={}//班级上设置的科目及对应的老师和助教
let classTeachers:any=[] //班级上的老师和助教
let tableDefaultTeahcers:any={ //默认带上的老师：全科课程：科目对应的老师  非全科课程：班级上的老师和助教
	teachers:[],
	assistants:[]
}

const shiftSubjectId = ref('') ; //	当前课程对应科目 

const MinCourseTime=ref('')
const MaxCourseTime=ref('')

//班级上设置的排课信息
const classPlanList=ref([] as any[])
const classPlanInfo=ref({
	EnableSubject:0
})

//最近一次排课信息
const lastPlanList=ref([] as any[])
const lastInfo=ref({
	weekVisible:true,
	EnableSubject:0,
	SubjectID:'',
	SubjectName:''
})

// 控制复用排课信息的 popover 显示状态
const reusablePlanPopoverVisible = ref(false)

const weekList=ref([{
	ID:1,
	Name:'星期一'
},{
	ID:2,
	Name:'星期二'
},{
	ID:3,
	Name:'星期三'
},{
	ID:4,
	Name:'星期四'
},{
	ID:5,
	Name:'星期五'
},{
	ID:6,
	Name:'星期六'
},{
	ID:7,
	Name:'星期日'
}])

const classApiConfig = computed(
	(): ApiConfig => ({
		apiFunction: queryClass,
		params: {
			pageSize: 10,
			pageIndex: 1,
			status: 1,
			campus: currentCampus.value,
			finished: finished.value,
			shiftType: 6, //集体班、一对多
			isFilterByRight: 1, // 是否按权限过滤：0否，1只查询自己名下学员的班级，2只查询自己名下的班级;如果有查看所有班级权限，这个参数可以忽略
			countStudents: 1,
			classStatusSelect:EnableClassOpeningStatus.value?1:-1//开班状态
		},
		searchParam: 'query',
		debounceTime: 300,
	})
)

function handleClassChange(val: any) {
	
	if (val.length) {
		// className.value = val[0].Name
		form.value.ClassID = val[0].ID
		// 【启橙】排课之前是否检查班级的老师、教室、预招人数设置：1是（未设置完整则不可以排课），0否（默认）
		if ( CheckClassSetWhenAddCourse.value ) {
			if ( val[0].ClassroomName=="" || val[0].MaxStudentsAmount<=0 || val[0].StudentCount<val[0].MaxStudentsAmount || val[0].TeacherName=="" ) {
				ElMessageBox.alert(`“${val[0].Name}”需同时满足下面条件，才允许排课！<br/>1、${transToConfigDescript('班级详情中需完善上课教室、任课老师、预招人数等信息。')}<br/>2、当前在班人数必须“满员”。`, '提示', {
					confirmButtonText: '知道了',
					dangerouslyUseHTMLString: true,
				})
				form.value.ClassID=''
				className.value=''
				classSelected.value=[]
				return ;
			}
		}
		if (CheckClassSizeWhenArranging.value &&  val[0].MinStudentsAmount !== 0 && val[0].MinStudentsAmount > val[0].FactStudentCount && val[0].IsOneToOne != 1) {
			ElMessageBox.alert(`${transToConfigDescript('班级人数未达到排课标准')}，不能进行排课（最低开班人数：` + val[0].MinStudentsAmount + '人，实际入班人数：' + val[0].FactStudentCount +'人）。', '提示', {
				confirmButtonText: '知道了',
			})
			form.value.ClassID=''
			className.value=''
			classSelected.value=[]
			return ;
		}
	} else {
		// 不清空 className.value，保持用户输入的内容
		// className.value = ''
		form.value.ClassID = ''
		clearAboutClass()
		if(EnableMustSameSubjectTeacherCourse.value){
			form.value.PlanList.forEach((item:any)=>{
				item.Teachers=[]
				item.teacherScheduleTime=''
			})
		}
	}

	// 同步更新 classSelected
	classSelected.value = val
    fucnQueryClass()
}

function fucnQueryClass(filterDate?: string) {
    shiftList.value=[]
    form.value.ShiftID=''
	if (classSelected.value.length) {
		getClass({ id: classSelected.value[0].ID }).then((res: any) => {
            let cls=res.Data
			classTeachers=cls.Teachers//一班多课会要用
			Unit.value=cls.Unit
			isMinutesToTimes.value=cls.MinutesToTimes==1
			StandardMinutes.value=cls.StandardMinutes
			shiftList.value=IsOpen_OneClassMuitShift.value?(cls.ClassRelShift||[]):[]
			if(cls.ShiftID && cls.IsOneToOne != 1){
				shiftList.value.unshift({ID: cls.ShiftID, Name: cls.ShiftName})
				form.value.ShiftID = cls.ShiftID;
			}
			if(cls.Plan&&cls.Plan.length>0){
				let planList = cls.Plan.map((item:any)=>{
					return{
						Weekday:item.Weekday,
						TimeRange:[item.StartTime,item.EndTime],
						Duration: calculateDuration([item.StartTime,item.EndTime]),
						ClassroomID:item.ClassroomID==EMPTYGUID?'':item.ClassroomID,
						ClassroomName:item.ClassroomName||'',
						Teachers:tableDefaultTeahcers.teachers.length>0?cloneDeep(tableDefaultTeahcers.teachers):[],
						Assistants:tableDefaultTeahcers.assistants.length>0?cloneDeep(tableDefaultTeahcers.assistants):[],
						CourseType:item.CourseType?item.CourseType:1,
					}
				})
				classPlanList.value = planList
				classPlanInfo.value.EnableSubject=cls.EnableSubject
				if(!props.disabledAutoFillPlan){
					// 如果是自由排课且有传入日期，过滤PlanList只保留与传入日期星期几一致的排课
					if (planRule.value === 0 && filterDate && planList.length > 0) {
						const targetDate = dayjs(filterDate)
						const targetWeekday = targetDate.day() // 获取星期几 (0=周日, 1=周一, ..., 6=周六)
						
						const filteredPlanList = planList.filter((plan: any) => {
							// 检查排课的星期几是否与传入日期的星期几一致
							return plan.Weekday === targetWeekday
						})
						
						// 如果过滤后有结果，使用过滤后的PlanList
						if (filteredPlanList.length > 0) {
							form.value.PlanList = filteredPlanList
						}
					} else {
						// 没有过滤条件，使用原始PlanList
						form.value.PlanList = planList
					}
				}
			}
			shiftSubjectId.value=cls.SubjectID
            EnableSubject.value=cls.EnableSubject == 1
            if(EnableSubject.value){
                let obj=Class_classifyTeachersBySubject(cls.Teachers,SUBJECT.value)
                if(!CourseIsClassSubject.value){
                    subjectList.value=obj.concatSubjectList
                }else{
                    subjectList.value=obj.SubjectArr
                }
				teacherSubjectObj=obj.Fa
                getSubjectAndTeacher()
            }else{
				funcGetTeachers(cls.Teachers)
			}
			if(!cls.CreatedCourseTimes){//第一次排课
				form.value.StartDate=cls.OpenDate||''
				form.value.EndDate=cls.CloseDate||''
				CreatedCourseTimes.value=0
				form.value.CourseTimes=cls.CourseTimes||''; //赋值计划排课数
			}else{
				let count = (Math.round((cls.CourseTimes * 1 - cls.CreatedCourseTimes) * 100)) / 100; //计算剩余排课数
				if (count > 0) {
					form.value.CourseTimes = count;
				}else{
					form.value.CourseTimes=''
				}
				CreatedCourseTimes.value=cls.CreatedCourseTimes
			}
			
			
			if(cls.Unit==3){
				form.value.IsHoliday=0
			}
			isOneToOne.value=cls.IsOneToOne
        })
	}
}

const chooseClassRef = ref()

function selectClass() {
	chooseClassRef.value
		?.open({
			multi: false,
			required: true,
			campusID:props.selectedCampusId||'',
			condition: {
				shiftType: 6,
				isFilterByRight: 1,
				isNoFinished:1,
				classStatusSelect:EnableClassOpeningStatus.value?1:-1
			},
			checkClassStudentCount:CheckClassSizeWhenArranging.value//检查班级人数是否达排课标准
		})
		.then((res: any) => {
			let obj=res.data
			// 【启橙】排课之前是否检查班级的老师、教室、预招人数设置：1是（未设置完整则不可以排课），0否（默认）
			if ( CheckClassSetWhenAddCourse.value ) {
				if ( obj.ClassroomName=="" || obj.MaxStudentsAmount<=0 || obj.StudentCount<obj.MaxStudentsAmount || obj.TeacherName=="" ) {
					ElMessageBox.alert(`“${obj.Name}”需同时满足下面条件，才允许排课！<br/>1、${transToConfigDescript('班级详情中需完善上课教室、任课老师、预招人数等信息。')}<br/>2、当前在班人数必须“满员”。`, '提示', {
						confirmButtonText: '知道了',
						dangerouslyUseHTMLString: true,
					})
					return ;
				}
			}

			clearAboutClass()
			// 同时更新所有相关状态，避免触发中间状态的处理
			className.value = res.data.Name
			form.value.ClassID = res.data.ID
			classSelected.value = [res.data]
			
            fucnQueryClass()
			// 触发验证更新
			nextTick(() => {
				formRef.value?.clearValidate('ClassID')
				setTimeout(() => {
					formRef.value?.validateField('ClassID')
				}, 0)
			})
		})
}

watch([()=>form.value.StartDate, ()=>form.value.EndDate],()=>{
	if(form.value.StartDate&&form.value.EndDate){
		getHolidays()
	}
})
function getHolidays(){
	holidays.value=[]
	queryHoliday({
        sdate:form.value.StartDate,
        edate:form.value.EndDate
    }).then((res:any)=>{
		holidays.value=res.Data.Data||[]
	})
}

function funcGetTeachers(arr:any) {//分离任课老师和助教
	if (!arr || arr.length == 0) {
		return;
	}
	tableDefaultTeahcers={	
		teachers:[],
		assistants:[]
	}
	arr.forEach((item:any)=>{
		if(item.Status==1){
			if(item.Role==1){
				item.LabelName=item.Name+(item.TeacherCommissionName?'('+item.TeacherCommissionName+')':'')
				tableDefaultTeahcers.teachers.push(item)
			}else if(item.Role==2){
				item.LabelName=item.Name+(item.TeacherCommissionName?'('+item.TeacherCommissionName+')':'')
				tableDefaultTeahcers.assistants.push(item)
			}
		}
	})
	form.value.PlanList.forEach((j:any)=>{
		j.Teachers=cloneDeep(tableDefaultTeahcers.teachers)
		j.Assistants=cloneDeep(tableDefaultTeahcers.assistants)
		getTeacherTime(j)
	})
	
}

function getSubjectAndTeacher(){
	let teacherSubjectIds: any[] = []
	form.value.PlanList.forEach((item:any)=>{
		item.Teachers.forEach((teacher:any)=>{
			teacherSubjectIds.push(teacher.SubjectID)
		})
		getTeacherTime(item)
	})
	
	// 判断所有教师的SubjectID是否相同
	let sameSubjectId: any = null;
	if (teacherSubjectIds.length > 0) {
		// 使用Set去重，如果只有一个值说明所有SubjectID都相同
		let uniqueSubjectIds = [...new Set(teacherSubjectIds)];
		if (uniqueSubjectIds.length === 1) {
			sameSubjectId = uniqueSubjectIds[0];
			// console.log('所有教师的SubjectID都相同:', sameSubjectId);
		} else {
			// console.log('教师的SubjectID不相同:', uniqueSubjectIds);
		}
	}
	
	if(sameSubjectId&&sameSubjectId!=EMPTYGUID){
		let s = subjectList.value.filter((item:any)=>{
			return item.ID == sameSubjectId
		})
		if(s.length > 0){
			form.value.SubjectID = s[0].ID;
		}
	}
	
}

function changePlanRule(){
	if(planRule.value==0){
		form.value.IsHoliday=0
	}
}

function changeSubject(){
	
	let re = teacherSubjectObj[form.value.SubjectID];
	if (!re || re.Status === 0) {
		form.value.PlanList.forEach((item:any)=>{
			item.Teachers=[]
			item.Assistants=[]
		})
		tableDefaultTeahcers={
			teachers:[],
			assistants:[]
		}
	}else{
		re.$AssiTeacher.forEach((item:any)=>{
			item.LabelName=item.Name+(item.TeacherCommissionName?'('+item.TeacherCommissionName+')':'')
		})
		tableDefaultTeahcers={
			teachers:re.TeacherID?[{
				ID:re.TeacherID,
				Name:re.Teacher,
				TeacherCommissionIDs:re.TeacherCommissionIDs,
				TeacherCommissionName:re.TeacherCommissionName,
				LabelName:re.Teacher+(re.TeacherCommissionName?'('+re.TeacherCommissionName+')':'')
			}]:[],
			assistants:re.$AssiTeacher||[]
		}
		form.value.PlanList.forEach((item:any)=>{
			item.Teachers=cloneDeep(tableDefaultTeahcers.teachers)
			item.Assistants=cloneDeep(tableDefaultTeahcers.assistants)
		})
	}
	form.value.PlanList.forEach((i:any)=>{
		getTeacherTime(i)
	})
	shiftSubjectId.value=form.value.SubjectID
}

function changeShift(){
	form.value.SubjectID=''
	subjectList.value=[]
	getShiftInfo({id:form.value.ShiftID}).then((res:any)=>{
		let cou = res.Data;
		ConsumeAmount.value=cou.ConsumeAmount||1;
		Unit.value = cou.Unit;
		isMinutesToTimes.value = cou.MinutesToTimes == 1 ;
		StandardMinutes.value = cou.StandardMinutes ;
		if(cou.Unit==3){
			form.value.IsHoliday=0
		}
		isOneToOne.value=cou.IsOneToOne
		shiftSubjectId.value=cou.SubjectID
		EnableSubject.value=cou.EnableSubject == 1
		if(EnableSubject.value){
			let obj=Class_classifyTeachersBySubject(classTeachers,SUBJECT.value)
			if(!CourseIsClassSubject.value){
				subjectList.value=obj.concatSubjectList
			}else{
				subjectList.value=obj.SubjectArr
			}
			teacherSubjectObj=obj.Fa
			getSubjectAndTeacher()
		}else{
			funcGetTeachers(classTeachers)
		}
	})
}

const chooseEmpTableRef=ref()
function selectTeacher(type: string) {
	chooseEmpTableRef.value
		?.open({
			multi: type == 'replace' || type == 'piano' ? false : true,
			choosed: type == 'follow' ? followTeacherList.value : type == 'office' ? officeTeacherList.value : type == 'consultant' ? consultantTeacherList.value : [],
			disabledCondition:['StatusList']
		})
		.then((res: any) => {
			const selectedTeachers = Array.isArray(res.data) ? res.data : [res.data];
			const teacherCategory = res.other;
			
			// 根据类型确认角色名称和目标列表
			let roleName = '';
			let targetList: any[] = [];
			
			switch (type) {
				case 'replace':
					roleName = '替课老师';
					targetList = replaceTeacherList.value;
					break;
				case 'follow':
					roleName = '跟课老师';
					targetList = followTeacherList.value;
					break;
				case 'piano':
					roleName = '钢伴老师';
					targetList = pianoTeacherList.value;
					break;
				case 'office':
					roleName = '坐班老师';
					targetList = officeTeacherList.value;
					break;
				case 'consultant':
					roleName = '课程顾问';
					targetList = consultantTeacherList.value;
					break;
			}
			
			// 检查每个选中的教师是否与其他角色重复
			for (const teacher of selectedTeachers) {
				const duplicationCheck = checkTeacherDuplication(teacher.ID, teacher.Name, roleName);
				if (duplicationCheck.duplicate) {
					return; // 如果重复，直接返回
				}
			}
			
			// 清空目标列表并添加新选中的教师
			targetList.length = 0;
			
			// 处理每个选中的教师
			for (const teacher of selectedTeachers) {
				const success = handleTeacherSelection(teacher, teacherCategory, targetList, roleName);
				if (!success) {
					return; // 如果处理失败，直接返回
				}
			}
		});
}

// 通用的表单校验触发函数
function triggerFormValidation(row: any, fieldName: string) {
	nextTick(() => {
		const rowIndex = form.value.PlanList.indexOf(row)
		if (rowIndex !== -1) {
			// 清除之前的校验结果
			formRef.value?.clearValidate(`PlanList.${rowIndex}.${fieldName}`)
			// 重新触发校验
			setTimeout(() => {
				formRef.value?.validateField(`PlanList.${rowIndex}.${fieldName}`)
			}, 0)
		}
	})
}

function selectTableTeacher(row:any){
	if (EnableMustSameSubjectTeacherCourse.value) {
		if(!form.value.ClassID){
			ElMessageBox.alert(transToConfigDescript('请选择班级。'), '提示', {
				confirmButtonText: '确认',
			})
			return;
		}
	}
	let teacherCondition:any={
		StatusList:[1],
		IsClassTeacherList: [1]
	}
	let subjectId=EnableMustSameSubjectTeacherCourse.value?(EnableSubject.value?form.value.SubjectID:shiftSubjectId.value):'';
	let disabledCondition:any=['StatusList']
    if(EnableMustSameSubjectTeacherCourse.value){
        disabledCondition.push('SubjectIDList')
    }
	if(EnbaleEmpIsClassTeacher.value==1){
        disabledCondition.push('IsClassTeacherList')
    }
	if(subjectId&&subjectId!=EMPTYGUID){
		teacherCondition.SubjectIDList=[subjectId]
	}
    
	chooseEmpTableRef.value?.open({
		multi: false,
		disabledCondition:disabledCondition,
		condition:teacherCondition,
		showTeacherType:true
	}).then((res:any)=>{
		let d=res.data,other:any=res.other
		// 配置项启用 并且 课程和老师都有所属科目，才需要验证
		if ( Check_Shift_Teacher_Subject.value && shiftSubjectId.value !== '' && shiftSubjectId.value != EMPTYGUID && res.data.SubjectList.length ) {
			let teacherSubjects = res.data.SubjectList.map((i:any)=>i.ID).join(',') 
			if ( teacherSubjects.indexOf(shiftSubjectId.value) === -1 ) {
				ElMessageBox.confirm(transToConfigDescript('任课老师和课程的科目不一致，是否继续？'), '提示', {
					confirmButtonText: '继续',
                    cancelButtonText: '取消',
				}).then(()=>{
					handlerMainTeacherData(row, d, other) ;
				})
			} else {
				handlerMainTeacherData(row, d, other) ;
			}
		}else {
			handlerMainTeacherData(row, d, other) ;
		}
		
	})
}

// 处理 assistant-select 组件的助教选择变化
function onAssistantChange(row: any, selectedIds: any[], selectedAssistants: any[]) {
	// 更新助教数据
	row.Assistants = selectedAssistants.map(assistant => ({
		ID: assistant.id,
		Name: assistant.name,
		LabelName: assistant.name,
		Department: assistant.department || '',
		Photo: assistant.avatar || '',
		Status: assistant.status === '闲' ? 1 : 0,
		TeacherCommissionIDs: '',
		TeacherCommissionName: ''
	}));
	
	// 触发冲突校验的重新计算
	nextTick(() => {
		// 强制重新计算 planListConflicts
		planListConflicts.value;
	});
}

function selectTableAssistant(row: any) {
	chooseEmpTableRef.value?.open({
		multi: true,
		showTeacherType:true,
		disabledCondition:['StatusList']
	}).then((res: any) => {
		const selectedTeachers = res.data;
		const teacherCategory = res.other;
		
		// 检查每个选中的教师是否与其他角色重复
		for (const teacher of selectedTeachers) {
			const duplicationCheck = checkTeacherDuplication(teacher.ID, teacher.Name, '助教');
			if (duplicationCheck.duplicate) {
				return; // 如果重复，直接返回
			}
		}
		
		// 处理每个选中的教师
		for (const teacher of selectedTeachers) {
			// 检查同一类别是否只对应一个老师
			const pass = checkTeacherType(teacher, teacherCategory, row.Assistants, {
				ID: row.Teachers.length > 0 ? row.Teachers[0].ID : '',
				TeacherCommissionIDs: row.Teachers.length > 0 ? row.Teachers[0].TeacherCommissionIDs : ''
			});
			
			if (!pass) {
				continue; // 如果类别检查不通过，跳过这个教师
			}
			
			// 查找教师是否已存在
			const existingIndex = row.Assistants.findIndex((item: any) => item.ID === teacher.ID);
			
			if (existingIndex === -1) {
				// 新增助教
				const newAssistant = cloneDeep(teacher);
				newAssistant.TeacherCommissionIDs = teacherCategory ? teacherCategory.ID : '';
				newAssistant.TeacherCommissionName = teacherCategory ? teacherCategory.Name : '';
				newAssistant.LabelName = teacher.Name + (teacherCategory&&teacherCategory.Name ? `（${teacherCategory.Name}）` : '');
				row.Assistants.push(newAssistant);
			} else {
				// 更新现有助教的类别
				const existingAssistant = row.Assistants[existingIndex];
				const hasType = existingAssistant.TeacherCommissionIDs && 
					teacherCategory?.ID && 
					existingAssistant.TeacherCommissionIDs.indexOf(teacherCategory.ID) === -1;
				
				if (hasType) {
					existingAssistant.TeacherCommissionIDs += `,${teacherCategory.ID}`;
					existingAssistant.TeacherCommissionName += `,${teacherCategory.Name}`;
				} else {
					existingAssistant.TeacherCommissionIDs = teacherCategory?.ID || '';
					existingAssistant.TeacherCommissionName = teacherCategory?.Name || '';
				}
				
				existingAssistant.LabelName = teacher.Name + 
					(existingAssistant.TeacherCommissionName ? `（${existingAssistant.TeacherCommissionName}）` : '');
			}
		}
		
		// 触发冲突校验的重新计算
		nextTick(() => {
			// 强制重新计算 planListConflicts
			planListConflicts.value;
		});
	});
}

//选择任课老师的处理
function handlerMainTeacherData(row: any, d: any, other: any) {
	// 检查任课老师是否与其他角色重复
	const duplicationCheck = checkTeacherDuplication(d.ID, d.Name, '任课老师');
	if (duplicationCheck.duplicate) {
		return;
	}
	
	// 检查是否已被设置为助教
	const isAlreadyAssistant = row.Assistants.some((item: any) => item.ID === d.ID);
	if (isAlreadyAssistant) {
		ElMessageBox.alert(transToConfigDescript('该老师已被设置为助教。'), '提示', {
			confirmButtonText: '确认',
		});
		return;
	}
	
	// 检查同一类别是否只对应一个老师
	const pass = checkTeacherType(false, other, row.Assistants);
	if (!pass) {
		return;
	}
	
	// 处理任课老师的设置
	if (row.Teachers.length > 0 && row.Teachers[0].ID === d.ID) {
		// 更新现有任课老师的类别
		const existingTeacher = row.Teachers[0];
		const hasType = existingTeacher.TeacherCommissionIDs && 
			other?.ID && 
			existingTeacher.TeacherCommissionIDs.indexOf(other.ID) === -1;
		
		if (hasType) {
			existingTeacher.TeacherCommissionIDs += `,${other.ID}`;
			existingTeacher.TeacherCommissionName += `,${other.Name}`;
		} else {
			existingTeacher.TeacherCommissionIDs = other?.ID || '';
			existingTeacher.TeacherCommissionName = other?.Name || '';
		}
		
		existingTeacher.LabelName = existingTeacher.Name + (other?.ID ? `（${existingTeacher.TeacherCommissionName}）` : '');
	} else {
		// 设置新的任课老师
		const newTeacher = cloneDeep(d);
		newTeacher.TeacherCommissionIDs = other?.ID || '';
		newTeacher.TeacherCommissionName = other?.Name || '';
		newTeacher.LabelName = newTeacher.Name + (other?.Name ? `（${other.Name}）` : '');
		row.Teachers = [newTeacher];
	}
	// 更新科目和教师信息
	getSubjectAndTeacher();
	
	// 触发冲突校验的重新计算
	nextTick(() => {
		// 强制重新计算 planListConflicts
		planListConflicts.value;
	});
}

function onTeacherChange(row:any, value:any, teacher:any){
    // 将 TeacherSelect 选择结果同步到表格行的 Teachers 数组
    if(!value){
        row.Teachers = []
        return
    }
    const selectedId = typeof value === 'object' ? value.id : value
    const teacherName = teacher?.name || teacher?.Name || ''
    const newTeacher:any = {
        ID: selectedId,
        Name: teacherName,
        LabelName: teacherName,
        TeacherCommissionIDs: teacher?.TeacherCommissionIDs || '',
        TeacherCommissionName: teacher?.TeacherCommissionName || ''
    }
    if (row.Teachers && row.Teachers.length > 0) {
        const existing = row.Teachers[0]
        if (existing && existing.TeacherCommissionIDs) {
            newTeacher.TeacherCommissionIDs = existing.TeacherCommissionIDs
            newTeacher.TeacherCommissionName = existing.TeacherCommissionName
        }
    }
    row.Teachers = [newTeacher]
    // 更新科目和教师信息保持一致
    getSubjectAndTeacher()
}



// 新增：统一的教师重复检查函数
function checkTeacherDuplication(teacherId: string, teacherName: string, excludeRole?: string) {
	const allRoles = [
		{ name: '任课老师', list: form.value.PlanList.flatMap((plan: any) => plan.Teachers || []) },
		{ name: '助教', list: form.value.PlanList.flatMap((plan: any) => plan.Assistants || []) },
		{ name: '替课老师', list: replaceTeacherList.value },
		{ name: '跟课老师', list: followTeacherList.value },
		{ name: '钢伴老师', list: pianoTeacherList.value },
		{ name: '坐班老师', list: officeTeacherList.value },
		{ name: '课程顾问', list: consultantTeacherList.value }
	];
	
	// 检查教师是否已在其他角色中存在
	for (const role of allRoles) {
		if (excludeRole && role.name === excludeRole) {
			continue; // 跳过当前角色
		}
		
		const existingTeacher = role.list.find((teacher: any) => teacher.ID === teacherId);
		if (existingTeacher) {
			ElMessageBox.alert(`${teacherName}已被设置为${role.name=='任课老师'?transToConfigDescript('任课老师'):role.name},不能继续设为${excludeRole=='任课老师'?transToConfigDescript('任课老师'):excludeRole}。`, '提示', {
				confirmButtonText: '知道了',
			});
			return { duplicate: true, role: role.name, teacher: existingTeacher };
		}
	}
	
	return { duplicate: false };
}

// 新增：批量修改任课老师/助教的校验函数
function validateBatchEditTeachers(teachers: any[], teacherRole: string) {
	// 1. 检查选择的老师是否与其他行的老师有冲突
	for (const teacher of teachers) {
		const conflictCheck = checkTeacherDuplication(teacher.ID, teacher.Name, teacherRole);
		if (conflictCheck.duplicate) {
			return { success: false };
		}
	}
	
	// 2. 检查选择的老师类别是否与其他行已选类别重复
	for (const teacher of teachers) {
		if (teacher.TeacherCommissionIDs) {
			const categoryIds = teacher.TeacherCommissionIDs.split(',');
			for (const categoryId of categoryIds) {
				// 检查其他行是否已有相同类别
				for (const plan of form.value.PlanList) {
					const otherTeachers = teacherRole === '任课老师' ? plan.Assistants || [] : plan.Teachers || [];
					for (const otherTeacher of otherTeachers) {
						if (otherTeacher.TeacherCommissionIDs) {
							const otherCategoryIds = otherTeacher.TeacherCommissionIDs.split(',');
							if (otherCategoryIds.includes(categoryId)) {
								ElMessageBox.alert(transToConfigDescript('同一类别只能设置给一个老师。'), '提示', {
									confirmButtonText: '确认',
								});
								return { success: false };
							}
						}
					}
				}
			}
		}
	}
	
	return { success: true };
}

// 新增：检查任课老师和课程科目一致性的函数
function validateTeacherSubjectConsistency(teachers: any[]) {
	for (const teacher of teachers) {
		if (teacher.SubjectList && teacher.SubjectList.length > 0) {
			const teacherSubjects = teacher.SubjectList.map((subject: any) => subject.ID);
			if (!teacherSubjects.includes(shiftSubjectId.value)) {
				return ElMessageBox.confirm(
					transToConfigDescript('任课老师和课程的科目不一致，是否继续？'),
					'提示',
					{
						confirmButtonText: '继续',
						cancelButtonText: '取消',
						type: 'warning',
					}
				).then(() => {
					return { success: true };
				}).catch(() => {
					return { success: false };
				});
			}
		}
	}
	return Promise.resolve({ success: true });
}

// 新增：通用的教师选择处理函数
function handleTeacherSelection(teacher: any, teacherCategory: any, targetList: any[], roleName: string) {
	// 检查同一类别是否只对应一个老师
	const pass = checkTeacherType(teacher, teacherCategory, targetList);
	if (!pass) {
		return false;
	}
	
	// 查找教师是否已存在
	const existingIndex = targetList.findIndex((item: any) => item.ID === teacher.ID);
	
	if (existingIndex === -1) {
		// 新增教师
		const newTeacher = cloneDeep(teacher);
		newTeacher.TeacherCommissionIDs = teacherCategory ? teacherCategory.ID : '';
		newTeacher.TeacherCommissionName = teacherCategory ? teacherCategory.Name : '';
		newTeacher.LabelName = teacher.Name + (teacherCategory ? `（${teacherCategory.Name}）` : '');
		targetList.push(newTeacher);
	} else {
		// 更新现有教师的类别
		const existingTeacher = targetList[existingIndex];
		const hasType = existingTeacher.TeacherCommissionIDs && 
			teacherCategory?.ID && 
			existingTeacher.TeacherCommissionIDs.indexOf(teacherCategory.ID) === -1;
		
		if (hasType) {
			existingTeacher.TeacherCommissionIDs += `,${teacherCategory.ID}`;
			existingTeacher.TeacherCommissionName += `,${teacherCategory.Name}`;
		} else {
			existingTeacher.TeacherCommissionIDs = teacherCategory?.ID || '';
			existingTeacher.TeacherCommissionName = teacherCategory?.Name || '';
		}
		
		existingTeacher.LabelName = teacher.Name + 
			(existingTeacher.TeacherCommissionName ? `（${existingTeacher.TeacherCommissionName}）` : '');
	}
	
	return true;
}



function changeCourseType(row:any){
	triggerFormValidation(row, 'ClassroomID')
}

function validateEndDate(rule: any, value: any, callback: any) {
    // 仅在重复排课时校验
    if (planRule.value === 1) {
        const start = form.value.StartDate
        const end = form.value.EndDate
        if (start && end && dayjs(end).isBefore(dayjs(start))) {
            callback(new Error('结束日期不能早于开始日期'))
            return
        }
        if (start && end) {
            const daysDiff = dayjs(end).diff(dayjs(start), 'day')
            if (daysDiff > 180) {
                callback(new Error('起止时间间隔不能超过180天'))
                return
            }
        }
    }
    callback()
}

function validateRowTimeRange(rule: any, value: any, callback: any) {
    if (!Array.isArray(value) || value.length < 2) {
        callback(new Error(transToConfigDescript('请选择上课时间')))
        return
    }
    const [start, end] = value
    if (!start || !end) {
        callback(new Error(transToConfigDescript('请选择上课时间')))
        return
    }
    const toMinutes = (time: string) => {
        const parts = time.split(':')
        const hours = Number(parts[0])
        const minutes = Number(parts[1])
        return hours * 60 + minutes
    }
    if (toMinutes(end) <= toMinutes(start)) {
        callback(new Error('结束时间必须大于开始时间'))
        return
    }
    callback()
}

function changeStartDate(){
	checkEndDate()
}
function checkEndDate(){
	if(form.value.EndDate){
		// 清除之前的校验结果
		formRef.value?.clearValidate(`EndDate`)
		// 重新触发校验
		setTimeout(() => {
			formRef.value?.validateField(`EndDate`)
		}, 0)
	}
}

const batchEditClassroomRef=ref()
function batchEditClassroom(){
	if(form.value.PlanList.length==0){
		ElMessage.warning(transToConfigDescript('请添加上课时间'))
		return
	}
	batchEditClassroomRef.value.open({
		campusId:currentCampus.value
	}).then((res:any)=>{
		form.value.PlanList.forEach((item:any)=>{
			if(res.data){
				item.ClassroomID=res.data.ID
				item.ClassroomName=res.data.Name
			}else{
				item.ClassroomID=''
				item.ClassroomName=''
			}
			
		})
	})
}

const batchEditTeacherRef=ref()
function batchEditTableTeacher(){
	if(form.value.PlanList.length==0){
		ElMessage.warning(transToConfigDescript('请添加上课时间'))
		return
	}
	if (EnableMustSameSubjectTeacherCourse.value) {
		if(!form.value.ClassID){
			ElMessageBox.alert(transToConfigDescript('请选择班级。'), '提示', {
				confirmButtonText: '确认',
			})
			return;
		}
	}
	batchEditTeacherRef.value.open({
		subjectId:EnableMustSameSubjectTeacherCourse.value?(EnableSubject.value?form.value.SubjectID:shiftSubjectId.value):''
	}).then((res:any)=>{
		// 校验选择的任课老师
		const validationResult = validateBatchEditTeachers(res.data, '任课老师');
		if (!validationResult.success) {
			return;
		}
		
		// 如果开启了Check_Shift_Teacher_Subject配置项且shiftSubjectId有值，检查任课老师和课程科目是否一致
		if (Check_Shift_Teacher_Subject.value && shiftSubjectId.value && shiftSubjectId.value !== EMPTYGUID) {
			validateTeacherSubjectConsistency(res.data).then((subjectValidationResult: any) => {
				if (subjectValidationResult.success) {
					form.value.PlanList.forEach((item:any)=>{
						item.Teachers=res.data
						getTeacherTime(item)
					})
				}
			}).catch(() => {
				// 用户取消操作
			});
			return;
		}
		
		form.value.PlanList.forEach((item:any)=>{
			item.Teachers=res.data
			getTeacherTime(item)
		})
	})
}

const batchEditAssistantRef=ref()
function batchEditTableAssistant(){
	if(form.value.PlanList.length==0){
		ElMessage.warning(transToConfigDescript('请添加上课时间'))
		return
	}
	batchEditAssistantRef.value.open().then((res:any)=>{
		// 校验选择的助教
		const validationResult = validateBatchEditTeachers(res.data, '助教');
		if (!validationResult.success) {
			return;
		}
		
		form.value.PlanList.forEach((item:any)=>{
			item.Assistants = cloneDeep(res.data)
		})
	})
}


function addPlan(){
	let defaultWeekday=1,
		defaultClassroomID='',
		defaultClassroomName='',
		teachers=[],
		assistants=[],
		defaultCourseType=1
	if(classSelected.value&&classSelected.value.length>0){
		let obj=classSelected.value[0]
		defaultClassroomID=obj.ClassroomID&&obj.ClassroomID!=EMPTYGUID?obj.ClassroomID:''
		defaultClassroomName=obj.ClassroomName||''
		teachers=tableDefaultTeahcers.teachers.length>0?cloneDeep(tableDefaultTeahcers.teachers):[]
		assistants=tableDefaultTeahcers.assistants.length>0?cloneDeep(tableDefaultTeahcers.assistants):[]
		defaultCourseType=1
	}
	if(form.value.PlanList.length>0){
		defaultWeekday=form.value.PlanList[form.value.PlanList.length-1].Weekday
		defaultClassroomID=form.value.PlanList[form.value.PlanList.length-1].ClassroomID
		defaultClassroomName=form.value.PlanList[form.value.PlanList.length-1].ClassroomName
		teachers=form.value.PlanList[form.value.PlanList.length-1].Teachers
		assistants=form.value.PlanList[form.value.PlanList.length-1].Assistants
		defaultCourseType=form.value.PlanList[form.value.PlanList.length-1].CourseType
	}
	form.value.PlanList.push({
		Weekday:defaultWeekday,
		TimeRange:[],
		Duration: 0,
		ClassroomID:defaultClassroomID!=EMPTYGUID?defaultClassroomID:'',
		ClassroomName:defaultClassroomName,
		Teachers:teachers,
		Assistants:cloneDeep(assistants),
		CourseType:defaultCourseType,
	})
	console.log(form.value.PlanList)
}

function delPlan(index:number){	
	form.value.PlanList.splice(index,1)
}
watch(() => form.value.CourseDateList, (val:any) => {
	if(val.length){
		val.sort((a:any, b:any) => new Date(a).getTime() - new Date(b).getTime());
	}
	form.value.CourseDateList=val
})


function onCourseDateListCleared() {
    // 与原 clearCourseDateList 的行为一致，保持校验体验
    formRef.value?.clearValidate(`CourseDateList`)
    setTimeout(() => {
        formRef.value?.validateField(`CourseDateList`)
    }, 0)
}

function getAdvanceConfig(){
	querySysConfig({
		campusID: '',
		type: 0,
		configNames: 'Check_Shift_Teacher_Subject,CourseTeacherRequired,CheckAssistantConflict,CourseRuleSetting' //配置项名称（多个时用逗号分隔）
	}).then((res:any) => {
		var data = res.Data;
		data.forEach((item:any) => {
			if (item.Name == "Check_Shift_Teacher_Subject" && item.Value == 1) {
				Check_Shift_Teacher_Subject.value = true;
			}
			if (item.Name == 'CourseTeacherRequired' && item.Value == 1) {
				courseTeacherRequired.value = true;
			}
			if (item.Name == 'CheckAssistantConflict' && item.Value == 1) {
				CheckAssistantConflict.value = true;
			}
			if (item.Name == 'CourseRuleSetting') {
				let obj=JSON.parse(item.OtherParameter)
				MinCourseTime.value=obj.MinCourseTime
				MaxCourseTime.value=obj.MaxCourseTime
			}
		})
	})
}

function clearAboutClass(){
	form.value.SubjectID=''
	subjectList.value=[]
	shiftSubjectId.value=''
	teacherSubjectObj={}
	classTeachers=[]
	tableDefaultTeahcers={
		teachers:[],
		assistants:[]
	}
	CreatedCourseTimes.value=0
	Unit.value=0
	isMinutesToTimes.value=false
	StandardMinutes.value=0
	ConsumeAmount.value=1
	isOneToOne.value=0
	EnableSubject.value=false
	classPlanList.value=[]
	classPlanInfo.value.EnableSubject=0
}

function resetAllForm(){ 
	form.value=cloneDeep(defaultForm)
	className.value=''
	classSelected.value=[]
	planRule.value=1
	replaceTeacherList.value=[]
	followTeacherList.value=[]
	pianoTeacherList.value=[]
	officeTeacherList.value=[]
	consultantTeacherList.value=[]
	holidays.value=[]
	MinCourseTime.value=''
	MaxCourseTime.value=''
	lastPlanList.value=[]
	lastInfo.value.weekVisible=true
	lastInfo.value.EnableSubject=0
	lastInfo.value.SubjectID=''
	lastInfo.value.SubjectName=''
	clearAboutClass()
}

function resetForm(){ //目前外部切换校区使用，要保留时间相关信息
	// 先清除表单验证状态
	formRef.value?.clearValidate()
	
	// 保存需要保留的字段
	const preservedStartDate = form.value.StartDate
	const preservedEndDate = form.value.EndDate
	const preservedCourseDateList = form.value.CourseDateList
	const preservedCourseTimes = form.value.CourseTimes
	const preservedPlanListFields = form.value.PlanList.map((plan: any) => ({
		Weekday: plan.Weekday,
		TimeRange: plan.TimeRange,
		Duration: plan.Duration,
		CourseType: plan.CourseType,
		Teachers: plan.Teachers,
		Assistants: plan.Assistants
	}))
	const preservedDescribe = form.value.Describe
	const preservedInternalRemark = form.value.InternalRemark
	// 重置表单
	form.value=cloneDeep(defaultForm)
	
	// 恢复需要保留的字段
	form.value.StartDate = preservedStartDate
	form.value.EndDate = preservedEndDate
	form.value.CourseDateList = preservedCourseDateList
	form.value.CourseTimes = preservedCourseTimes
	form.value.Describe = preservedDescribe
	form.value.InternalRemark = preservedInternalRemark
	
	// 恢复 PlanList 中需要保留的字段
	form.value.PlanList = preservedPlanListFields.map((preserved: any) => ({
		...cloneDeep(defaultForm.PlanList[0] || {}),
		Weekday: preserved.Weekday,
		TimeRange: preserved.TimeRange,
		Duration: preserved.Duration,
		CourseType: preserved.CourseType,
		Teachers: preserved.Teachers,
		Assistants: preserved.Assistants
	}))
	// 如果原来没有 PlanList，则保持为空数组
	if (preservedPlanListFields.length === 0) {
		form.value.PlanList = []
	}
	
	className.value=''
	classSelected.value=[]
	// planRule.value=1
	replaceTeacherList.value=[]
	followTeacherList.value=[]
	pianoTeacherList.value=[]
	officeTeacherList.value=[]
	consultantTeacherList.value=[]
	// holidays.value=[]
	// MinCourseTime.value=''
	// MaxCourseTime.value=''
	clearAboutClass()
	
	// 再次清除表单验证状态（确保清除）
	nextTick(() => {
		formRef.value?.clearValidate()
	})
}

// 检查关键字段是否发生变化（排除时间相关字段）
function hasKeyFieldsChanged(): boolean {
	const current = form.value
	const initial = defaultForm

	// 检查基础字段（排除时间相关字段）
	if (
		current.ShiftID !== initial.ShiftID ||
		current.IsHoliday !== initial.IsHoliday ||
		current.PlanID !== initial.PlanID ||
		current.SubjectID !== initial.SubjectID ||
		current.ClassID !== initial.ClassID ||
		current.IsSubscribeCourse !== initial.IsSubscribeCourse ||
		current.InternalRemark !== initial.InternalRemark ||
		current.Describe !== initial.Describe
	) {
		return true
	}

	// 检查 PlanList：如果数量大于0，说明有排课计划（需要检查是否有非时间字段）
	if (current.PlanList.length > 0) {
		for (let i = 0; i < current.PlanList.length; i++) {
			const plan = current.PlanList[i]
			
			// 检查是否有老师、助教、教室等关键字段
			if (
				(plan.Teachers && plan.Teachers.length > 0) ||
				(plan.Assistants && plan.Assistants.length > 0) ||
				plan.ClassroomID ||
				(plan.ReplaceTeachers && plan.ReplaceTeachers.length > 0) ||
				(plan.FollowTeachers && plan.FollowTeachers.length > 0) ||
				(plan.PianoTeachers && plan.PianoTeachers.length > 0) ||
				(plan.OfficeTeachers && plan.OfficeTeachers.length > 0) ||
				(plan.ConsultantTeachers && plan.ConsultantTeachers.length > 0)
			) {
				return true
			}
		}
	}

	return false
}

const formRef = ref<FormInstance>()

// 新增：对外暴露的方法和属性
const emit = defineEmits(['submit'])

// 新增：监听TimeRange变化，自动计算Duration
function handleTimeRangeChange(row: any) {
	if (row.TimeRange && Array.isArray(row.TimeRange)) {
		row.Duration = calculateDuration(row.TimeRange)
	}
	// 变更时间段后，手动触发表单该行 TimeRange 的重新校验
	triggerFormValidation(row, 'TimeRange')
	// 触发冲突校验的重新计算
	nextTick(() => {
		// 强制重新计算 planListConflicts
		planListConflicts.value;
	});
}

// 新增：监听教室选择变化，触发冲突校验
function handleClassroomChange(obj:any,row: any) {
	row.ClassroomName=obj.Name
	// 触发冲突校验的重新计算
	nextTick(() => {
		// 强制重新计算 planListConflicts
		planListConflicts.value;
	});
}

// 新增：监听星期选择变化，触发冲突校验
function handleWeekdayChange(row: any) {
	// 触发冲突校验的重新计算
	nextTick(() => {
		// 强制重新计算 planListConflicts
		planListConflicts.value;
	});
}

// 新增：监听PlanList变化，自动计算所有时段的Duration
watch(() => form.value.PlanList, (newPlanList) => {
	if (newPlanList && Array.isArray(newPlanList)) {
		newPlanList.forEach((item: any) => {
			if (item.TimeRange && Array.isArray(item.TimeRange) && item.TimeRange.length === 2) {
				item.Duration = calculateDuration(item.TimeRange)
			}
		})
	}
}, { deep: true })

function getFreeParams(type:string,item:any){
	let params:any={
		CampusID:form.value.CampusID||currentCampus.value,
		IsHoliday:form.value.IsHoliday,
		CourseMode:form.value.CourseMode,
		CourseTimes:form.value.CourseTimes||0,
		PlanList:[{
			Weekday:item.Weekday,
			StartTime:item.TimeRange.length==2?item.TimeRange[0]:'',
			EndTime:item.TimeRange.length==2?item.TimeRange[1]:''
		}]
	}
	if(planRule.value==0){
		params.CourseMode='Free'
		params.IsHoliday=0
		params.CourseDateList=form.value.CourseDateList
	}else{
		params.StartDate=form.value.StartDate
		params.EndDate=form.value.EndDate
	}
	if(type=='teacher'){
		let subjectid=EnableMustSameSubjectTeacherCourse.value?(EnableSubject.value?form.value.SubjectID:shiftSubjectId.value):''
		params.IsClassTeacher=1
		params.SubjectIDList=subjectid?[subjectid]:[]
	}
	return params
}

// 新增：提交方法
function submit() {
	formRef.value!.validate((valid) => {
        if (valid) {
			if(form.value.PlanList.length==0){
				ElMessage.error(transToConfigDescript('请添加上课时间'))
				return
			}
			let params:any={}
			Object.assign(params,form.value)
			params.CampusID=params.CampusID||currentCampus.value
			params.SubjectID=params.SubjectID||EMPTYGUID
			if(planRule.value==0){
				params.CourseMode='Free'
				params.StartDate=params.CourseDateList[0]
				params.IsHoliday=0
			}else{
				params.CourseDateList=[]
			}
			params.CourseTimes=params.CourseTimes||0
			let planList=params.PlanList.map((item:any)=>{
				let teachers:any=[]
				item.Teachers.forEach((teacher:any)=>{
					teachers.push({
						ID:teacher.ID,
						Role:1,
						TeacherCommissionIDList:teacher.TeacherCommissionIDs?teacher.TeacherCommissionIDs.split(','):[],
					})
				})
				item.Assistants.forEach((assistant:any)=>{
					teachers.push({
						ID:assistant.ID,
						Role:2,
						TeacherCommissionIDList:assistant.TeacherCommissionIDs?assistant.TeacherCommissionIDs.split(','):[],
					})
				})
				replaceTeacherList.value.forEach((item:any)=>{
					teachers.push({
						ID:item.ID,
						Role:3,
						TeacherCommissionIDList:[],
					})
				})
				followTeacherList.value.forEach((item:any)=>{
					teachers.push({
						ID:item.ID,
						Role:4,
						TeacherCommissionIDList:[],
					})
				})
				pianoTeacherList.value.forEach((item:any)=>{
					teachers.push({
						ID:item.ID,
						Role:5,
						TeacherCommissionIDList:[],
					})
				})
				officeTeacherList.value.forEach((item:any)=>{
					teachers.push({
						ID:item.ID,
						Role:6,
						TeacherCommissionIDList:[],
					})
				})
				consultantTeacherList.value.forEach((item:any)=>{
					teachers.push({
						ID:item.ID,
						Role:7,
						TeacherCommissionIDList:[],
					})
				})
				let obj:any={
					Weekday:(form.value.CourseMode=='Weekly'||form.value.CourseMode=='Biweekly')&&planRule.value==1?item.Weekday:0,
					StartTime:item.TimeRange[0],
					EndTime:item.TimeRange[1],
					ClassroomID:item.ClassroomID||EMPTYGUID,
					TeacherList:teachers,
					CourseType:EnableStore.value&&EnableCourseOnline.value?item.CourseType:1,
				}
				return obj
			})
			delete params.PlanList
			
			params.PlanList=planList
			if(planListConflicts.value.some((item:any)=>item)){
				ElMessageBox.alert('当前“弹窗中的排课”存在冲突，请调整！', '提示', {
					confirmButtonText: '知道了',
				})
				return
			}
			// 这块暂时没用，新增1对1课程排课时，EnableConsumeSelf 开启，EnableCourseConfirmWhenLess = 2 | 3| 0 时， 并且剩余课时小于等于0，给出提示“学员在校区的课程上没有剩余费用，是否继续排课。”。
			// if (isOneToOne.value == 1 && '023'.indexOf(EnableCourseConfirmWhenLess.value)!=-1 && EnableConsumeSelf.value) {
			// 	getStudentRemaindAmount({
			// 		campusid: currentCampus.value,
			// 		id: form.value.ClassID,
			// 		searchType: 2
			// 	}).then((res:any)=>{
			// 		let list = res.Data instanceof Array ? res.Data : [],
			// 		studentList = list.filter((item:any)=>item.RestOfClass <= 0);
			// 		if (studentList.length > 0) {
			// 			let tipsMsg = studentList.map((item:any)=>item.Name).join('、');
			// 			ElMessageBox.confirm('以下学员在校区的课程上没有剩余费用，是否继续排课：<br>' + tipsMsg, '提示', {
			// 				confirmButtonText: '是',
			// 				cancelButtonText: '否',
			// 			}).then(()=>{
			// 				next();
			// 			})
			// 		} else {
			// 			next();
			// 		}
			// 	})
			// } else {
				next();
			// }
			function next(){
				// 计算排课次数
				const scheduleCount = calculateScheduleCount(
					planRule.value,
					form.value.CourseMode,
					form.value.StartDate,
					form.value.EndDate,
					form.value.CourseDateList,
					form.value.CourseTimes,
					form.value.IsHoliday,
					holidays.value,
					planList,
					Unit.value
				)
				// 计算总分钟数（用于Unit=1时显示时长）
				let totalMinutes = 0
				if (Unit.value === 1) {
					if (planRule.value === 1) {
						// 重复排课：使用通用函数计算总时长
						const scheduleInfo = calculateRepeatedScheduleInfo(
							form.value.StartDate,
							form.value.EndDate,
							form.value.CourseMode,
							planList,
							form.value.IsHoliday,
							holidays.value,
							Unit.value,
							form.value.CourseTimes
						)
						totalMinutes = scheduleInfo.minutes
					} else {
						// 自由排课：按选择的日期数量计算
						totalMinutes = planList.reduce((total: number, item: any) => {
							return total + calculateDuration([item.StartTime, item.EndTime])
						}, 0) * form.value.CourseDateList.length
					}
				}
				// 只需要抛出数据
				if(props.onlyEmitData){
					emit('submit', params,className.value)
					return
				}

				// 生成确认提示信息
				const confirmMessage = generateScheduleConfirmMessage(
					Unit.value,
					scheduleCount,
					ConsumeAmount.value,
					totalMinutes
				)

				// 用户二次确认
				ElMessageBox.confirm(confirmMessage, '排课确认', {
					confirmButtonText: '确认排课',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					// 用户确认后，触发提交事件，让父组件处理
					emit('submit', params,className.value)
				}).catch(() => {
					// 用户取消，不执行任何操作
				})
			}
		}
	})
}

// 根据参数设置表单
function setupWithParams(params: any) {
	if(props.isEdit){
		initEditData(params)
	}else{
		initAddData(params);
	}	
}

function initEditData(params:any){
	let info=params.CourseInfo||{}
	form.value.PlanID=params.ID
	form.value.ClassID=info.ClassID
	form.value.className=info.ClassName
	isOneToOne.value=info.IsOneToOne||0
	Unit.value=info.Unit
	form.value.ShiftID=info.ShiftID
    shiftNameEdit.value = info.ShiftName || ''
	EnableSubject.value=info.EnableSubject||0
	shiftSubjectId.value=info.SubjectID==EMPTYGUID?'':info.SubjectID
	form.value.SubjectID=shiftSubjectId.value
	planRule.value=params.CourseMode=='Free'?0:1
	form.value.CourseMode=planRule.value?(params.CourseMode||'Weekly'):'Weekly'
	form.value.IsHoliday=info.IsHoliday||0
	form.value.CourseTimes=params.CourseTimes||''
	if(planRule.value==0){
		form.value.CourseDateList=params.DateList
	}else{
		form.value.StartDate=dayjs(params.StartDate).format('YYYY-MM-DD')
		form.value.EndDate=dayjs(params.EndDate).format('YYYY-MM-DD')
	}
	if(params.DetailList){
		let planList:any=[]
		let replaceTeacherList:any=[]//替课老师 role:3
		let followTeacherList:any=[]//跟课老师 role:4
		let pianoTeacherList:any=[]//钢伴老师 role:5
		let officeTeacherList:any=[]//坐班老师 role:6
		let consultantTeacherList:any=[]//课程顾问 role:7
		params.DetailList.forEach((item:any)=>{
			let teachers:any=[],assistents:any=[]
			if(item.TeacherList){
				item.TeacherList.forEach((t:any)=>{
					if(t.Role==1){
						teachers.push(t)
					}else if(t.Role==2){
						assistents.push(t)
					}else if(t.Role==3){
						replaceTeacherList.push(t)
					}else if(t.Role==4){
						followTeacherList.push(t)
					}else if(t.Role==5){
						pianoTeacherList.push(t)
					}else if(t.Role==6){
						officeTeacherList.push(t)
					}else if(t.Role==7){
						consultantTeacherList.push(t)
					}
				})
			}
			planList.push({
				Weekday:item.Weekday,
				TimeRange:[item.StartTime,item.EndTime],
				ClassroomID:item.ClassroomID==EMPTYGUID?'':item.ClassroomID,
				ClassroomName:item.ClassroomName,
				Teachers:teachers,
				Assistants:assistents,
				CourseType:item.CourseType||1
			})
		})
		replaceTeacherList.value=replaceTeacherList
		followTeacherList.value=followTeacherList
		pianoTeacherList.value=pianoTeacherList
		officeTeacherList.value=officeTeacherList
		consultantTeacherList.value=consultantTeacherList
		form.value.PlanList=planList
	}
	form.value.IsSubscribeCourse=info.IsSubscribeCourse
	form.value.InternalRemark=info.InternalRemark||''
	form.value.Describe=info.ExternalRemark||''
	form.value.PlanList.forEach((i:any)=>{
		getTeacherTime(i)
	})
	handleInitSubject()
}

function handleInitSubject(){//获取一些班级上的信息，处理全科课程和一班多课情况
	getClass({ id: form.value.ClassID }).then((res: any) => {
		let cls=res.Data
		Unit.value=cls.Unit
		isMinutesToTimes.value=cls.MinutesToTimes==1
		StandardMinutes.value=cls.StandardMinutes
		shiftSubjectId.value=cls.SubjectID
		isOneToOne.value=cls.IsOneToOne
		EnableSubject.value=cls.EnableSubject == 1
		if(EnableSubject.value){
			let obj=Class_classifyTeachersBySubject(cls.Teachers,SUBJECT.value)
			if(!CourseIsClassSubject.value){
				subjectList.value=obj.concatSubjectList
			}else{
				subjectList.value=obj.SubjectArr
			}
			teacherSubjectObj=obj.Fa
			getSubjectAndTeacher()
		}
		if(form.value.ShiftID!=cls.ShiftID){
			getShiftInfo({id:form.value.ShiftID}).then((res1:any)=>{
				let cou = res1.Data;
				ConsumeAmount.value=cou.ConsumeAmount||1;
				Unit.value = cou.Unit;
				isMinutesToTimes.value = cou.MinutesToTimes == 1 ;
				StandardMinutes.value = cou.StandardMinutes ;
				if(cou.Unit==3){
					form.value.IsHoliday=0
				}
				isOneToOne.value=cou.IsOneToOne
				shiftSubjectId.value=cou.SubjectID
				EnableSubject.value=cou.EnableSubject == 1
				if(EnableSubject.value){
					let obj=Class_classifyTeachersBySubject(classTeachers,SUBJECT.value)
					if(!CourseIsClassSubject.value){
						subjectList.value=obj.concatSubjectList
					}else{
						subjectList.value=obj.SubjectArr
					}
					teacherSubjectObj=obj.Fa
					getSubjectAndTeacher()
				}
			})
		}
	})
}

function initAddData(params:any){
	// 设置排课规则为自由排课
	planRule.value = 0
	
	// 设置上课日期
	if (params.date) {
		form.value.CourseDateList = [params.date]
	}
	
	// 根据课表类型设置不同的字段
	if (params.timetableType === CourseTimetableTypeEnum.ClassTimetable) {
		// 班级课表：设置班级
		if (params.object) {
			form.value.ClassID = params.object.id
			className.value = params.object.name
			classSelected.value = [{
				ID: params.object.id,
				Name: params.object.name
			}]
			// 触发班级选择后的后续处理，传递日期过滤标记
			fucnQueryClass(params.date)
		}
	}
	
	// 如果是老师或教室课表，需要添加时段
	if (params.timetableType === CourseTimetableTypeEnum.TeacherTimetable || 
		params.timetableType === CourseTimetableTypeEnum.ClassroomTimetable||
		params.timeRange) {
		// 添加一个默认时段
		const planItem: any = {
			Weekday:params.date?new Date(params.date).getDay():new Date().getDay(),
			TimeRange:[],
			Duration: 0,
			ClassroomID:'',
			ClassroomName:'',
			Teachers:[],
			Assistants:[],
			CourseType:1,
		}
		
		// 如果是老师课表，填充老师信息
		if (params.timetableType === CourseTimetableTypeEnum.TeacherTimetable && params.object) {
			planItem.Teachers = [{
				ID: params.object.id,
				Name: params.object.name,
				LabelName: params.object.name
			}]
		}
		
		// 如果是教室课表，填充教室信息
		if (params.timetableType === CourseTimetableTypeEnum.ClassroomTimetable && params.object) {
			planItem.ClassroomID = params.object.id
			planItem.ClassroomName = params.object.name
		}
		if (params.timeRange&&params.timeRange.startTime&&params.timeRange.endTime) {
			planItem.TimeRange = [params.timeRange.startTime,params.timeRange.endTime]
		}else{
			planItem.TimeRange = []
		}
		form.value.PlanList = [planItem]
		form.value.PlanList.forEach((i:any)=>{
			getTeacherTime(i)
		})
	}
}
function validCourseTimes(rule: any, value: any, callback: any){
    // 空值（null、undefined、空字符串）不校验
    if(value === null || value === undefined || value === ''){
        callback()
        return
    }
    // 校验数字0和字符串"0"
    if(value == 0){
        callback(new Error('需大于0或者不填'))
    }else{
        callback()
    }
}

function getTeacherTime(row:any){
	if (EnableAssignTeacherCourseCampus.value) {
		getTeachersCourseTimes({
			teacherUserID: row.Teachers[0].ID,
			campusID: form.value.CampusID||currentCampus.value
		}).then((data:any)=>{
			row.teacherScheduleTime = data.Data || '';
		})
	}
}
// 新增：对外暴露的方法
defineExpose({
	submit,
	resetForm,
	resetAllForm,
	setupWithParams,
	hasKeyFieldsChanged
})

// 懒初始化：首次激活时触发一次
watch(() => props.activated, (v) => {
    if (v && !hasInited.value) {
        hasInited.value = true
        getAdvanceConfig()
        if (!props.isEdit) {
            getLastArrangeInfo()
        }
    }
}, { immediate: true })

function useClassPlan(){
	let planList = classPlanList.value.map((item:any)=>{
		item.Teachers.forEach((j:any)=>{
			j.LabelName=j.Name+(j.TeacherCommissionName?'('+j.TeacherCommissionName+')':'')
		})
		item.Assistants.forEach((j:any)=>{
			j.LabelName=j.Name+(j.TeacherCommissionName?'('+j.TeacherCommissionName+')':'')
		})
		return{
			Weekday:item.Weekday||1,
			TimeRange:item.TimeRange,
			Duration: item.Duration,
			ClassroomID:item.ClassroomID==EMPTYGUID?'':item.ClassroomID,
			ClassroomName:item.ClassroomName||'',
			Teachers:item.Teachers||[],
			Assistants:item.Assistants||[],
			CourseType:item.CourseType?item.CourseType:1,
		}
	})
	form.value.PlanList = planList
	if(classPlanInfo.value.EnableSubject==1&&form.value.SubjectID&&form.value.SubjectID!=EMPTYGUID){
		changeSubject()
	}
	// 关闭 popover
	reusablePlanPopoverVisible.value = false
}

function useLastPlan(){
	if(lastInfo.value.EnableSubject==1&&lastInfo.value.SubjectID&&lastInfo.value.SubjectID!=EMPTYGUID){
		let obj=subjectList.value.find((item:any)=>item.ID==lastInfo.value.SubjectID)
		if(!obj){
			form.value.SubjectID=lastInfo.value.SubjectID
			changeSubject()
		}
	}
	let planList = lastPlanList.value.map((item:any)=>{
		item.Teachers.forEach((j:any)=>{
			j.LabelName=j.Name+(j.TeacherCommissionName?'('+j.TeacherCommissionName+')':'')
		})
		item.Assistants.forEach((j:any)=>{
			j.LabelName=j.Name+(j.TeacherCommissionName?'('+j.TeacherCommissionName+')':'')
		})
		return{
			Weekday:item.Weekday||1,
			TimeRange:item.TimeRange,
			Duration: item.Duration,
			ClassroomID:item.ClassroomID==EMPTYGUID?'':item.ClassroomID,
			ClassroomName:item.ClassroomName||'',
			Teachers:item.Teachers||[],
			Assistants:item.Assistants||[],
			CourseType:item.CourseType?item.CourseType:1,
		}
	})
	form.value.PlanList = planList
	// 关闭 popover
	reusablePlanPopoverVisible.value = false
}

function getLastArrangeInfo(){
	getCoursePlanTimeReusableInfot({}).then((res:any)=>{ 
		let data=res.Data||{}
		let planList=data.LastCourseTimeList||[],
			newPlanList:any[]=[]
		planList.forEach((item:any)=>{
			let teachers:any=[],assistants:any=[]
			item.TeacherList.forEach((j:any)=>{
				j.LabelName=j.Name+(j.TeacherCommissionName?'('+j.TeacherCommissionName+')':'')
				if(j.Role==1){
					teachers.push(j)
				}else if(j.Role==2){
					assistants.push(j)
				}
			})
			newPlanList.push({
				Weekday:item.Weekday,
				WeekName:item.WeekName,
				TimeRange:[item.StartTime,item.EndTime],
				Duration: calculateDuration([item.StartTime,item.EndTime]),
				ClassroomID:item.ClassroomID==EMPTYGUID?'':item.ClassroomID,
				ClassroomName:item.ClassroomName,
				Teachers:teachers,
				Assistants:assistants,
				CourseType:item.CourseType||1
			})
		})
		lastPlanList.value=newPlanList
		lastInfo.value.weekVisible=newPlanList[0].Weekday!=0
		lastInfo.value.EnableSubject=data.LastCourseEnableSubject||0
		lastInfo.value.SubjectID=data.LastCourseSubjectID&&data.LastCourseSubjectID!=EMPTYGUID?data.LastCourseSubjectID:''
		lastInfo.value.SubjectName=data.LastCourseSubjectName||''
	})
}
</script>