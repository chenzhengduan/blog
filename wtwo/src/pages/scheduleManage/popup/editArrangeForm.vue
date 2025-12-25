<template>
	<el-drawer
		v-model="drawer"
		title="编辑排课"
		direction="rtl"
		size="1150px"
		class="editArrangeForm"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
		<div class="drawer-body-wrap bg-[#F7F8FA]" v-loading="arrangeDataLoading">	
			<el-scrollbar class="px-16px!">
				<el-form :model="form" ref="formRef" label-position="top">
					<!-- 基本信息 -->
					<div class="form-section">
						<div class="section-title mb-16px">
							<span class="text-14px font-600 color-#303133">基础信息</span>
						</div>
						<div class="flex two-column-wrap">
							<el-form-item
								prop="courseName"
								:label="transToConfigDescript('上课课程')"
								class="half-width"
								v-if="form.ShiftName!='' &&IsOpen_OneClassMuitShift"
							>
								<el-input
									v-model="form.ShiftName"
									placeholder=""
									class="w-100%!"
									disabled
								></el-input>
							</el-form-item>
							<el-form-item
								prop="className"
								:label="transToConfigDescript('上课学员/班级')"
								class="half-width"
							>
								<el-input
									v-model="form.ClassName"
									placeholder=""
									class="w-100%!"
									disabled
								></el-input>
							</el-form-item>
							<el-form-item
								prop="CampusName"
								:label="transToConfigDescript('上课校区')"
								class="half-width"
							>
								<el-input
									v-model="form.CampusName"
									placeholder=""
									class="w-100%!"
									disabled
								></el-input>
							</el-form-item>
							<el-form-item
								prop="StartTime"
								:label="transToConfigDescript('上课时间')"
								class="half-width"
								:rules="[
									{
										required: !IsMonthCourse || EnableMonthShiftCourse,
										message: transToConfigDescript('请选择上课时间'),
										validator: validateTimeRange,
										trigger: 'change'
									},
								]"
								v-if="!IsMonthCourse || EnableMonthShiftCourse"
							>
								<div class="flex items-center gap-[6px] w-100%">
									<el-date-picker 
										v-model="timeStartDate" 
										type="date" 
										placeholder="请选择日期" 
										value-format="YYYY-MM-DD" 
										class="w-122px!"
										@change="handleDateChange"
									></el-date-picker>
									<course-time-range
										v-model="timeRange"
										class="flex-1"
										:min-course-time="MinCourseTime"
										:max-course-time="MaxCourseTime"
										:campus-id="form.CampusID"
										@change="handleTimeRangeChange"
									/>
								</div>
							</el-form-item>
							<el-form-item
								prop="Duration"
								:label="transToConfigDescript('上课时长')"
								class="half-width"
								v-if="!IsMonthCourse || EnableMonthShiftCourse"
							>
								<el-input
									:value="form.Duration ? form.Duration + '分钟' : '-'"
									placeholder="-"
									class="w-100%!"
									disabled
								></el-input>
							</el-form-item>
							<el-form-item
								prop="FinishedName"
								:label="transToConfigDescript('上课状态')"
								class="half-width"
							>
								<el-input
									v-model="form.FinishedName"
									placeholder=""
									class="w-100%!"
									disabled
								></el-input>
							</el-form-item>
						</div>
					</div>

					<!-- 上课内容 -->
					<div class="form-section">
						<div class="section-title mb-16px">
							<span class="text-14px font-600 color-#303133">{{transToConfigDescript('上课内容')}}</span>
						</div>
						<div class="flex two-column-wrap">
							<template v-if="shiftScheduleList.length > 0">
							<!-- <el-form-item
								prop="classProgress"
								class="half-width progress-form-item"
							>
									<template #label>
										<div class="flex items-center justify-between custom-checkbox-height !w-100%">
											<div class="flex-center">
												<span>上课进度</span>
											</div>
											<el-checkbox
												v-model="schedulePostpone"
												v-if="EnableAddCourse_ShiftSchedule"
												:disabled="!ClassChapterEdit"
												class="!absolute top-3px right-0"
											>
												<div class="flex-center">
													<span>后续排课进度顺延</span>
													<el-icon class="ml-4px" color="#909399" size="14px">
														<svg aria-hidden="true">
															<use xlink:href="#w2-xinxitishi"></use>
														</svg>
													</el-icon>
												</div>
											</el-checkbox>
											<el-checkbox
												v-model="scheduleCover"
												v-if="!EnableAddCourse_ShiftSchedule"
												:disabled="!ClassChapterEdit"
											>
												<div class="flex-center">
													<span>上课进度覆盖所有排课</span>
													<el-tooltip
														class="box-item"
														effect="dark"
														placement="top"
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
														<template #content>勾选复选框后，该班所有未上课排课记录的上课进度都为所选的进度。</template>
													</el-tooltip>
												</div>
											</el-checkbox>
										</div>
									</template>
									<div 
										ref="progressTriggerRef"
										class="custom-select-trigger"
										:class="{'disabled': !ClassChapterEdit}"
										@click.stop="openPopover('progress', progressTriggerRef)"
										v-click-outside="onClickOutside"
									>
										<span class="trigger-text mx-11px">{{ form.ShiftAmount }} / {{ form.ShiftAmountTotal }}</span>
										<div class="h-100% px-12px flex-center">
											<el-icon 
												class="trigger-arrow" 
												:class="{ 'is-reverse': currentPopoverType === 'progress' && currentPopoverVisible }">
												<ArrowDown />
											</el-icon>
										</div>
									</div>
								</el-form-item> -->
								<el-form-item
									prop="ChapterContent"
									label="章节内容"
									class="half-width progress-form-item"
								>
									<template #label>
										<div class="flex items-center justify-between custom-checkbox-height !w-100%">
											<div class="flex-center mr-5px">
												<span>章节内容</span>
											</div>
											<el-checkbox
												v-model="schedulePostpone"
												v-if="EnableAddCourse_ShiftSchedule"
												:disabled="!NewCourse_ClassChapterEdit"
											>
												<div class="flex-center">
													<span>后续排课进度顺延</span>
													<el-tooltip
                                                        class="box-item"
                                                        effect="dark"
                                                        placement="top"
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
                                                        <template #content>勾选复选框，后续排课记录的{{transToConfigDescript('上课进度')}}从“{{form.ShiftAmount}}”依次顺延。</template>
                                                    </el-tooltip>
												</div>
											</el-checkbox>
											<el-checkbox
												v-model="scheduleCover"
												v-if="!EnableAddCourse_ShiftSchedule"
												:disabled="!NewCourse_ClassChapterEdit"
											>
												<div class="flex-center">
													<span>{{transToConfigDescript('上课进度覆盖所有排课')}}</span>
													<el-tooltip
														class="box-item"
														effect="dark"
														placement="top"
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
														<template #content>{{transToConfigDescript('勾选复选框后，该班所有未上课排课记录的上课进度都为所选的进度。')}}</template>
													</el-tooltip>
												</div>
											</el-checkbox>
										</div>
									</template>
									<div 
										ref="chapterTriggerRef"
										class="custom-select-trigger"
										:class="{'disabled': !NewCourse_ClassChapterEdit}"
										@click.stop="openPopover('chapter', chapterTriggerRef)"
										v-click-outside="onClickOutside"
									>
										<span class="trigger-text mx-11px">{{ form.ChapterContent || '请选择' }}</span>
										<div class="h-100% px-12px flex-center">
											<el-icon 
												class="trigger-arrow" 
												:class="{ 'is-reverse': currentPopoverType === 'chapter' && currentPopoverVisible }">
												<ArrowDown />
											</el-icon>
										</div>
									</div>
								</el-form-item>
							</template>
							<el-form-item
								prop="CourseContent"
								:label="transToConfigDescript('上课内容')"
								class="half-width"
							>
								<el-input
									v-model="form.CourseContent"
									:placeholder="NewCourse_ClassContentEdit ? '请输入' : ''"
									type="textarea"
									:rows="2"
									maxlength="2000"
									class="w-100%!"
									:disabled="!NewCourse_ClassContentEdit"
								></el-input>
							</el-form-item>
							<el-form-item
								prop="SubjectName"
								:label="transToConfigDescript('上课科目')"
								class="half-width"
								v-if="form.SubjectName"
							>
								<template #label>
									<div class="flex-center">
										<span>{{transToConfigDescript('上课科目')}}</span>
										<el-tooltip
											effect="dark"
											placement="top"
										>
											<el-icon
												size="16px"
												class="ml-4px cursor-pointer vertical-middle"
												color="#909399"
											>
												<svg aria-hidden="true">
													<use
														xlink:href="#w2-xinxitishi"
													></use>
												</svg>
											</el-icon>
											<template #content>{{transToConfigDescript('全科课程的上课科目')}}</template>
										</el-tooltip>
									</div>
								</template>
								<el-select v-model="form.SubjectID" filterable @change="changeSubject">
									<el-option v-for="item in subjectList" :value="item.ID" :label="item.Name" :key="item.ID"></el-option>
								</el-select>
							</el-form-item>
						</div>
					</div>

					<!-- 教室与老师 -->
					<div class="form-section">
						<div class="section-title mb-16px">
							<span class="text-14px font-600 color-#303133">{{transToConfigDescript('教室与老师')}}</span>
						</div>
						<div class="flex two-column-wrap">
							<el-form-item
								prop="CourseType"
								:label="transToConfigDescript('上课方式')"
								class="half-width"
							>
								<template #label>
									<div class="flex-center">
										<span>{{transToConfigDescript('上课方式')}}</span>
										<el-tooltip
											class="box-item"
											effect="dark"
											placement="top"
											v-if="IsOpenLiveStream"
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
								<el-select
									v-model="form.CourseType"
									placeholder="请选择"
									class="w-100%!"
									:disabled="!(EnableStore && EnableCourseOnline)"
								>
									<el-option label="线下课" :value="1"></el-option>
									<el-option label="线上课" :value="2"></el-option>
								</el-select>
							</el-form-item>
							<el-form-item
								prop="ClassroomID"
								:label="transToConfigDescript('上课教室')"
								class="half-width"
								:rules="[
									{
										required: form.CourseType === 1,
										message: transToConfigDescript('请选择上课教室'),
										trigger: 'change'
									}
								]"
							>
								<classroom-select
									v-model="form.ClassroomID"
									:campus-id="campusID"
									:initial-data="form.ClassroomID ? { ID: form.ClassroomID, Name: form.ClassroomName } : undefined"
									:start-time="timeStartDate&&timeRange.length==2?(timeStartDate+' '+timeRange[0]):''"
									:end-time="timeStartDate&&timeRange.length==2?(timeStartDate+' '+timeRange[1]):''"
									:SubjectIDList="EnableSubject&&form.SubjectID ? [form.SubjectID] : []"
									:shiftSubjectId="shiftSubjectId"
									:EnableSubject="EnableSubject"
									:ShiftID="form.ShiftID"
									:course-type="form.CourseType"
									placeholder="请选择"
									:clearable="form.CourseType === 1 ? false : true"
									@change="(val:string,obj:any)=>{handleClassroomChange(obj)}"
									class="w-100%!"
								></classroom-select>
							</el-form-item>
						<el-form-item
							prop="TeacherName"
							:label="transToConfigDescript('任课老师')"
							class="half-width"
							:rules="[
								{
									required: CourseTeacherRequired,
									message: transToConfigDescript('请选择任课老师'),
									validator: (rule: any, value: any, callback: any) => {
										if (CourseTeacherRequired && mainTeacherList.length === 0) {
											callback(new Error(transToConfigDescript('请选择任课老师')));
										} else {
											callback();
										}
									},
									trigger: 'change'
								},
							]"
						>
							<input-tag
								v-if="EnableClassCommission"
								placeholder="请选择"
								:selected="mainTeacherList"
								:isLine="true"
								:multiple="false"
								@click="selectTableTeacher()"
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
                        <TeacherSelect
                            v-else
                            :model-value="mainTeacherList.length>0 ? mainTeacherList[0].ID : ''"
                            :initial-data="{ ID: mainTeacherList?.[0]?.ID, Name: mainTeacherList?.[0]?.Name }"
                            :start-time="timeStartDate&&timeRange.length==2?(timeStartDate+' '+timeRange[0]):''"
							:end-time="timeStartDate&&timeRange.length==2?(timeStartDate+' '+timeRange[1]):''"
							:SubjectIDList="EnableSubject&&form.SubjectID ? [form.SubjectID] : []"
							:shiftSubjectId="shiftSubjectId"
							:EnableSubject="EnableSubject"
							:ShiftID="form.ShiftID"
                            placeholder="请选择"
                            :disabled-ids="assistantTeacherList ? assistantTeacherList.map((item: any) => item.ID) : []"
							:cell-mode="true"
                            @change="onMainTeacherSelectChange"
                            @clear="() => onMainTeacherSelectChange('', null)"
                            class="w-100%!"
                        />
							<div v-if="teacherScheduleTime&&mainTeacherList.length>0" class="text-12px color-[#EBB722] line-height-20px">{{ teacherScheduleTime }}</div>
						</el-form-item>
						<el-form-item
							prop="AssistantTeacherName"
							label="助教"
							class="half-width"
						>
							<input-tag
								v-if="EnableClassCommission"
								placeholder="请选择"
								:selected="assistantTeacherList"
								:isLine="true"
								:multiple="true"
								@click="selectTableAssistant()"
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
								:model-value="assistantTeacherList ? assistantTeacherList.map((item: any) => item.ID) : []"
								:initial-data="assistantTeacherList"
								:start-time="timeStartDate&&timeRange.length==2?(timeStartDate+' '+timeRange[0]):''"
								:end-time="timeStartDate&&timeRange.length==2?(timeStartDate+' '+timeRange[1]):''"
								:SubjectIDList="EnableSubject&&form.SubjectID ? [form.SubjectID] : []"
								:shiftSubjectId="shiftSubjectId"
								:EnableSubject="EnableSubject"
								:ShiftID="form.ShiftID"
								placeholder="请选择助教"
								:disabled-ids="mainTeacherList.length>0 ? [mainTeacherList[0].ID] : []"
								:cell-mode="true"
								@change="onAssistantSelectChange"
								@clear="() => onAssistantSelectChange([], [])"
								class="w-100%!"
							/>
						</el-form-item>
							<template v-if="EnableCourseRole&&form.IsOneToOne==0">
								<el-form-item prop="" label="替课老师" class="half-width">
									<input-tag
										placeholder="请选择"
										:selected="replaceTeacherList"
										:isLine="true"
										@choose="selectTeacher('replace')"
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
										@choose="selectTeacher('follow')"
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
										@choose="selectTeacher('office')"
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
										@choose="selectTeacher('consultant')"
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
							</template>
						</div>
					</div>

					<!-- 约课信息：按班级排课 = 10，按学员排课 = 20，预约排课 = 30，日程 = 40 -->
					<div class="form-section" v-if="form.CourseMethod == 10 || form.CourseMethod == 30">
						<div class="section-title mb-16px">
							<span class="text-14px font-600 color-#303133">约课信息</span>
						</div>
						<div class="flex two-column-wrap">
							<el-form-item
								prop="IsSubscribeCourse"
								class="half-width"
								v-if="form.CourseMethod == 10 || form.CourseMethod == 30"
								:class="{'half-width':form.CourseMethod == 30,'w-[90px]!':form.CourseMethod == 10}"
							>
								<template #label>
									<div>开放预约</div>
								</template>
								<div>
									<el-switch
										v-model="form.IsSubscribeCourse"
										:active-value="1"
										:inactive-value="0"
										:disabled="!NewCourse_SelectCoursePlan&&form.CourseMethod == 10"
									></el-switch>
									<div v-if="form.CourseMethod == 30" class="appointment-control">开放预约后,学员可以自助在师生信-约课模块预约排课</div>
								</div>
							</el-form-item>
							<el-form-item v-if="form.CourseMethod == 10&&form.IsSubscribeCourse==1">
								<div  class="mt-5px text-12px color-[#909399] w-[100%] line-height-16px!">
									1、开放预约后，学员可以自助在师生信-约课模块预约排课；<br/>
									{{transToConfigDescript('2、可约人数=班级预招人数-在课人数；')}}<br/>
									{{transToConfigDescript('3、临近上课时间，将不允许预约（临近的时间长度，在学员约课规则中设置）')}}
								</div>
							</el-form-item>
							<template v-if="form.CourseMethod == 30">
								<el-form-item
									prop="MaxStudentCount"
									label="可约人数"
									class="half-width"
                                    :show-message="false"
									:rules="[
										{
											required: form.CourseMethod === 30 && form.IsSubscribeCourse === 1,
											message: '请输入可约人数',
											trigger: 'blur'
										},
										{
											validator: validateMaxStudentCount,
											trigger: 'blur'
										}
									]"
								>
                                        <el-input
                                            v-model="form.MaxStudentCount"
                                            placeholder="请输入"
                                            class="w-100%!"
                                            :formatter="inputIntFormat"
                                            :disabled="form.IsOneToOne==1"
                                        >
                                            <template #suffix>人</template>
                                        </el-input>
                                        <div class="description-text">
                                            约课人数达到指定的人数后,学员不能再预约
                                        </div>
								</el-form-item>
								<el-form-item
									prop="StartStudentCount"
									label="开课人数"
									class="half-width"
                                    :show-message="false"
									:rules="[
										{
											required: form.CourseMethod === 30 && form.IsSubscribeCourse === 1,
											message: '请输入开课人数',
											trigger: 'blur'
										},
										{
											validator: validateStartStudentCount,
											trigger: 'blur'
										}
									]"
								>
									<el-input
										v-model="form.StartStudentCount"
										placeholder="请输入"
										class="w-100%!"
										:formatter="inputIntFormat"
										:disabled="form.IsOneToOne==1"
									>
										<template #suffix>人</template>
									</el-input>
									<div class="description-text">
										{{transToConfigDescript('若预约人数未达到开课人数,需要老师手动取消上课。')}}
									</div>
								</el-form-item>
							</template>
						</div>
					</div>

					<!-- 其他信息 -->
					<div class="form-section">
						<div class="section-title mb-16px">
							<span class="text-14px font-600 color-#303133">其他信息</span>
						</div>
						<div class="flex two-column-wrap">
							<el-form-item
								prop="InternalRemark"
								label="对内备注"
								class="half-width"
							>
								<el-input
									v-model="form.InternalRemark"
									type="textarea"
									:rows="2"
									maxlength="3000"
									placeholder="请输入"
									class="w-100%!"
									:disabled="!NewCourse_ClassContentEdit"
								></el-input>
							</el-form-item>
							<!-- <el-form-item
								prop="Describe"
								label="对外备注"
								class="half-width"
							>
								<el-input
									v-model="form.Describe"
									type="textarea"
									:rows="2"
									maxlength="3000"
									placeholder="请输入"
									class="w-100%!"
								></el-input>
							</el-form-item> -->
						</div>
					</div>
				</el-form>
			</el-scrollbar>
		</div>
		<template #footer>
			<div class="flex-between">
				<div class="flex-center">
					<el-checkbox :model-value="checkConflict" :disabled="submitting||(!IsMonthCourse&&!NewCourse_IngoreCourseConflict)" @click="changeCheckConflict" :true-value="1" :false-value="0">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
				</div>
				<div class="flex-center">
					<el-button @click="close">取消</el-button>
					<el-button type="primary" @click="submitForm" :loading="submitting" v-if="NewCourse_CourseEdit && form.Finished !== 1">保存</el-button>
				</div>
			</div>
		</template>
	</el-drawer>

	<!-- 统一的上课进度选择弹窗 -->
	<el-popover
		:visible="currentPopoverVisible"
		placement="bottom-start"
		:width="600"
		:virtual-ref="currentTriggerRef"
		popper-class="edit-arrange-form-class-content-popover"
		virtual-triggering
		trigger="click"
	>
		<template #default>
			<div class="class-content-table-component">
				<el-table 
					:data="shiftScheduleList" 
					:max-height="300"
					@row-click="handleSelect"
					class="class-content-table w-100% cursor-pointer"
                    border
				>
					<el-table-column prop="ShiftAmount" :label="transToConfigDescript('课次')" width="60" align="center"/>
					<el-table-column prop="Title" label="章节内容" min-width="80" show-overflow-tooltip/>
					<el-table-column prop="Content" label="上课内容" show-overflow-tooltip min-width="100" />
					<el-table-column label="课件" show-overflow-tooltip min-width="100">
						<template #default="{ row }">
							{{ row.CoursewareList.map((item: any) => item.FileName).join('、') }}
						</template>
					</el-table-column>
				</el-table>
			</div>
		</template>
	</el-popover>

	<chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
	<ConflictPrompt ref="conflictPromptRef" />
</template>

<script lang="ts" setup>
import { ref, computed, getCurrentInstance, watch } from 'vue';
import { ElMessage, ElMessageBox, formMetaProps, ClickOutside as vClickOutside } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { dayjs } from 'element-plus'
import { 
	getCourseDetailByID,
	queryClassroom,
	getCourseShiftSchedule,
	editCourseInfoByID,
	getTeachersCourseTimes,
	checkSubscribeCourseStudent,
	getClassSubject,
} from '@/api/arrange';
import { useConfigs } from '@/store';
import { storeToRefs } from 'pinia';
import { useDictFieldsStore } from '@/store/dict';
import { Class_classifyTeachersBySubject } from '@/utils/index';
import { querySysConfig } from '@/api';
import { checkTeacherType } from '@/utils/scheduleUtils';
import { cloneDeep } from 'lodash';
import useEvent from '@/config/event';
import { inputIntFormat, transToConfigDescript } from '@/utils/filters/filters';
import ConflictPrompt from './conflictPrompt.vue';
import TeacherSelect from '@/components/business/select/teacher-select.vue'
import AssistantSelect from '@/components/business/select/assistant-select.vue'
import classroomSelect from '@/components/business/select/classroom-select.vue';
import courseTimeRange from '../components/course-time-range.vue';
import { calculateDuration } from '@/utils/timeUtils';

// 配置项和权限项
const configs = computed(() => {
	return useConfigs().configs
})
const NewCourse_ClassContentEdit = window.$xgj.op("NewCourse_ClassContentEdit"); // 修改上课内容权限
const NewCourse_ClassChapterEdit = window.$xgj.op("NewCourse_ClassChapterEdit"); // 修改上课章节
const NewCourse_SelectCoursePlan = window.$xgj.op("NewCourse_SelectCoursePlan") //班级排课开放预约
const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限
const NewCourse_CourseEdit = window.$xgj.op("NewCourse_CourseEdit"); // 修改排课权限

const EnableAddCourse_ShiftSchedule=computed(()=>{ //在排课时，是否根据课程上设置的上课进度自动生成每一节课的上课进度：1是，0否(默认)
	return configs.value.EnableAddCourse_ShiftSchedule==1
})
const CourseIsClassSubject=computed(()=>{ //全科课程排课，是否只能选班级上设置的科目 0：否（默认），1：是
	return configs.value.CourseIsClassSubject==1
})
const EnableAssignTeacherCourseCampus = computed(()=>{ //是否开启指派老师上课校区功能，0否（默认），1（是，丽音艺术定制）
	return configs.value.EnableAssignTeacherCourseCampus==1
})
const EnableCourseRole=computed(()=>{ //是否开启排课角色。0：不开启（默认），1：开启
	return configs.value.EnableCourseRole==1
})
const EnableClassCommission=computed(()=>{ // 是否开启老师类别（老的选择器）
	return configs.value.EnableClassCommission==1
})
const EnableStore=computed(()=>{ //是否开启更多功能菜单,1:显示更多功能的菜单
	return configs.value.EnableStore==1
})
const EnableCourseOnline=computed(()=>{ //是否开启在线课堂菜单，1：开通（开通后，才显示在线课堂菜单）
	return configs.value.EnableCourseOnline==1
})
const IsOpenShiftForDay=computed(()=>{ //开启按天计费或者按月计费功能：0按月计费（默认），1按天计费。
	return configs.value.IsOpenShiftForDay==1
})
const EnableMonthShiftCourse=computed(()=>{ //按月计费课程是否支持手动排课出勤不计费：0否，1是（白塔岭定制）。
	return configs.value.EnableMonthShiftCourse==1
})
const EnableMustSameSubjectTeacherCourse=computed(()=>{ //是否开启限制跨科目选择老师 0允许（默认） 1不允许
	return configs.value.EnableMustSameSubjectTeacherCourse==1
})
const IsOpenLiveStream=computed(()=>{ //声网直播
	return configs.value.IsOpenLiveStream==1
})
const IsOpen_OneClassMuitShift=computed(()=>{ //是否开启一对多的班课关系。0或1 ，默认0。设置为1后，一个班级可以设置多个可上课课程，排课时可以统计这些上课课程。
	return configs.value.IsOpen_OneClassMuitShift==1
})
const EnbaleEmpIsClassTeacher=computed(()=>{
    return configs.value.EnbaleEmpIsClassTeacher
})
const Check_Shift_Teacher_Subject=ref(false) //排课时的课程科目与老师可授课科目不一致时，是否显示提示  业务规则
const CourseTeacherRequired=ref(false) //排课时任课老师是否必填  业务规则
const MinCourseTime=ref('') //上课的“开始时间”不能早于“下方时间” 业务规则
const MaxCourseTime=ref('') //上课的“结束时间”不能晚于“下方时间” 业务规则

const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);
const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
let teacherSubjectObj:any={} //班级上设置的科目及对应的老师和助教
let teacherScheduleTime=ref('') //老师上课校区时间
let mainTeacherSubjects = ref(''); //任课老师科目
const event = useEvent();

interface IArrangeData {
	// 基本信息
	ID?: string; // 排课ID
	ShiftName?: string; // 课程名称
	ClassName?: string; // 班级名称
	ClassID?: string; // 班级ID
	CourseTypeName?: string; // 课程类型名称
	CampusName?: string; // 校区名称
	CampusID?: string; // 校区ID
	StartTime?: string; // 排课开始时间
	EndTime?: string; // 排课结束时间
	Duration?: number; // 排课开始时间 与排课结束时间 的组合
	Finished?: number; // 上课状态：-1不限，0未上课，1已上课，2已取消
	FinishedName?: string; // 上课状态枚举
	
	// 点名与出勤
	ConfirmUserName?: string; // 排课点名确认人
	IsClassTeacher?: string; // 是否班级老师
	StudentAttendanceCount?: number; // 实到人数
	StudentCount?: number; // 排课中学员人数
	CostCount?: number; // 计费人数
	CostNum?: number; // 计费数量
	
	// 上课内容
	ShiftAmount?: number; // 课次，关联[tShiftSchedule].cShiftAmount
	ShiftAmountTotal?: number; // 总进度课次，关联[tShiftSchedule]
	ShiftScheduleID?: string; // 课次ID
	ChapterContent?: string; // 章节内容
	ChapterTitle?: string; // 章节标题
	CourseContent?: string; // 排课-上课内容
	CourseImgList?: string[]; // 排课照片
	CourseImgs?: string; // 排课照片
	CoursewareSettingList?: ICourseware[]; // 课件
	SubjectName?: string; // 课程科目
	SubjectID?: string; // 课程科目ID
	
	// 教室与老师
	CourseType?: number; // 上课方式，1=线下课，2=在线课
	HeadMasterUserName?: string; // 班主任姓名
	ClassroomName?: string; // 教室名称
	ClassroomID?: string; // 教室ID
	MasterNameList?: string[]; // 学管师名称
	
	// 课程信息
	ShiftTermName?: string; // 课程所属期段
	ShiftSubjectName?: string; // 课程所属科目
	ShiftClassTypeName?: string; // 课程所属班型
	ShiftGradeName?: string; // 课程所属年级
	ShiftID?: string; // 课程ID
	ShiftCategoryName?: string; // 课程所属类型
	IsOneToOne?: number; // 0：集体班 1：一对一 2：一对N
	IsOneToOneName?: string; // 班级类型
	Unit?: number; // 课程单位：1小时、2次、3按月
	CourseMethod?: number; // 排课方式
	
	// 约课信息
	IsFromSubscribeCourse?: number; // 1来源于约课，0否
	StartStudentCount?: number; // 开课人数
	MaxStudentCount?: number; // 可约人数
	CourseStatus?: number; // 开课状态
	SubscribedCount?: number; // 已约人数
	MaxStudentsAmount?: number; // 班级最大上课人数
	IsSubscribeCourse?:number;//是否开放约课
	
	// 其他信息
	Describe?: string; // 对外备注
	InternalRemark?: string; // 对内备注
	CreateRule?: string; // 排课创建规则
	CreateRuleText?: string; // 排课创建规则文本
	CreateTime?: string; // 排课创建时间
	UpdateTime?: string; // 排课修改时间
	PlanID?: string; // 排课计划ID
	EnableSubject?: number; // 是否是全科课程，0否，1是
	LiveStreamingLink?: string; // 直播链接，用于ClassIn
	ImgAddress?: string; // 上课照片
	StudentName?: string; // 上课学员
	CourseTimeWeek?: string; // 上课时间
	CourseImgs1?: string; // 开放预约
	ClassCourseIndex?: number; // 排课进度索引
	ClassCourseCount?: number; // 排课进度总数
	IsPostpone?: boolean; // 是否延期
	IsCover?: number; // 是否覆盖
	ClassShiftAmount?: number; // 班级上的当前上课进度

	// 学员列表
	CourseStuentList?: any[];
	// 学员汇总数据
	CourseStuentTotal?: any[];
	// 点名记录
	CourseRollCallList?: any[];
}

interface ICourseware {
	CourseID?: string; // 排课ID
	EnableSee?: number; // 学员是否可见
	FileID?: string; // 课件ID
	FileName?: string; // 课件名称
	IsSyncLiveRoom?: number; // 是否同步到直播课堂
	ShiftID?: string; // 课程ID
	ShiftScheduleID?: string; // 课次ID
}

const drawer = ref(false); //弹窗是否显示
const arrangeDataLoading = ref(false); //加载中
const submitting = ref(false); //提交中
const checkConflict = ref(1); //开启检查上课冲突
const schedulePostpone = ref(false); //开启后续排课进度顺延
const scheduleCover = ref(false); //开启上课进度覆盖所有排课
const timeStartDate = ref(''); //上课日期
const timeRange = ref<[string, string] | []>([]);
// 触发元素引用
const progressTriggerRef = ref();
const chapterTriggerRef = ref();
// 当前弹窗状态
const currentPopoverVisible = ref(false);
const currentTriggerRef = ref();
const currentPopoverType = ref<'progress' | 'chapter' | 'content'>('progress');
// 上课内容数据
const shiftScheduleList = ref<any[]>([]);
const form = ref<IArrangeData>({});
const formRef = ref();
const EnableSubject=ref(false) //是否为全科课程
const subjectList=ref([] as any); //科目
const IsMonthCourse=ref(false); //是否为月课
const shiftSubjectId=ref('')//课程科目

function handleTimeRangeChange() {
	if (timeRange.value && Array.isArray(timeRange.value)) {
		form.value.Duration = calculateDuration(timeRange.value)
	}
	// 同步更新表单时间并触发校验
	updateFormTime();
	setTimeout(() => {
		formRef.value?.validateField('StartTime');
	}, 0);
}

function changeCheckConflict(){
	if(checkConflict.value){
		ElMessageBox.confirm(transToConfigDescript("不检查上课冲突，可能会生成相同时间的排课，确认不检查吗？"), '提示', {
			confirmButtonText: '确认不检查',
			cancelButtonText: '取消',
		}).then(()=>{
			checkConflict.value=0
		})
	}else{
		checkConflict.value=1
	}
}

function handleClassroomChange(obj:any) {
	form.value.ClassroomName=obj.Name
}

// 验证时间范围
function validateTimeRange(rule: any, value: any, callback: any) {
	// 如果不是月课或者启用了月课手动排课，则不需要验证
	if (!(!IsMonthCourse.value || EnableMonthShiftCourse.value)) {
		callback();
		return;
	}
	
	// 检查日期和时间是否都已选择
	if (!timeStartDate.value) {
		callback(new Error(transToConfigDescript('请选择上课日期')));
		return;
	}
	
	if (!timeRange.value || timeRange.value.length !== 2) {
		callback(new Error(transToConfigDescript('请选择上课时间')));
		return;
	}
	
	// 检查时间范围限制
	if (MinCourseTime.value && MaxCourseTime.value) {
		const startTime = timeRange.value[0];
		const endTime = timeRange.value[1];
		
		if (startTime < MinCourseTime.value) {
			callback(new Error(`开始时间不能早于 ${MinCourseTime.value}`));
			return;
		}
		
		if (endTime > MaxCourseTime.value) {
			callback(new Error(`结束时间不能晚于 ${MaxCourseTime.value}`));
			return;
		}
	}
	
	callback();
}

// 处理日期变化
function handleDateChange() {
	updateFormTime();
	setTimeout(() => {
		formRef.value?.validateField('StartTime');
	}, 0);
}


// 更新表单时间字段
function updateFormTime() {
	if (timeStartDate.value && timeRange.value && timeRange.value.length === 2) {
		const startDateTime = dayjs(`${timeStartDate.value} ${timeRange.value[0]}`);
		const endDateTime = dayjs(`${timeStartDate.value} ${timeRange.value[1]}`);
		
		if (startDateTime.isValid() && endDateTime.isValid()) {
			form.value.StartTime = startDateTime.format('YYYY-MM-DD HH:mm:ss');
			form.value.EndTime = endDateTime.format('YYYY-MM-DD HH:mm:ss');
		} else {
			form.value.StartTime = undefined;
			form.value.EndTime = undefined;
		}
	} else {
		form.value.StartTime = undefined;
		form.value.EndTime = undefined;
	}
}

// 初始化时间范围
function initTimeRange() {
	if (form.value.StartTime && form.value.EndTime) {
		const startDate = dayjs(form.value.StartTime);
		const endDate = dayjs(form.value.EndTime);
		
		if (startDate.isValid() && endDate.isValid()) {
			timeStartDate.value = startDate.format('YYYY-MM-DD');
			timeRange.value = [
				startDate.format('HH:mm'),
				endDate.format('HH:mm')
			];
		} else {
			timeStartDate.value = '';
			timeRange.value = [];
		}
	} else {
		timeStartDate.value = '';
		timeRange.value = [];
	}
}

// 处理popover弹窗打开
function openPopover(type: 'progress' | 'chapter' | 'content', triggerRef: any) {
	if (!NewCourse_ClassChapterEdit) {
		return;
	}
	// 设置当前弹窗状态
	currentPopoverType.value = type;
	currentTriggerRef.value = triggerRef;
	currentPopoverVisible.value = true;
}

// 处理popover弹窗选择
function handleSelect(item: any) {
	form.value.ShiftAmount = item.ShiftAmount;
	form.value.ChapterContent = item.Title;
	form.value.CourseContent = item.Content;
	form.value.CoursewareSettingList = item.CoursewareList;
	form.value.ShiftScheduleID = item.ID;

	// 关闭弹窗
	currentPopoverVisible.value = false;
	// 清理触发元素引用
	currentTriggerRef.value = null;
}

// 处理popover弹窗点击外部关闭
function onClickOutside(e: any) {
	if(currentPopoverVisible.value&&(currentTriggerRef.value&&!currentTriggerRef.value.contains(e.target))){
		currentPopoverVisible.value=false
		currentTriggerRef.value = null;
	}
}

// 切换科目
function changeSubject() {
	if (Check_Shift_Teacher_Subject && form.value.SubjectID !== '' && form.value.SubjectID != globalData.EMPTYGUID && mainTeacherSubjects.value !== '' ) {
		if ( mainTeacherSubjects.value?.indexOf(form.value.SubjectID || '') === -1 ) {
			ElMessageBox.confirm(transToConfigDescript('任课老师和课程的科目不一致，是否继续？'), '提示', {
				confirmButtonText: '继续',
				cancelButtonText: '取消',
			}).then(()=>{
			}).catch(()=>{
				form.value.SubjectID = '';
			})
		}
	}else { // 教师信息自动匹配
		var re = teacherSubjectObj[form.value.SubjectID || ''];
		if (!re || re.Status === 0) {
			return;
		}
		if (mainTeacherList.value.length == 0 && re.TeacherID) {
			mainTeacherList.value = [{
				ID:re.TeacherID,
				Name:re.Teacher,
				TeacherCommissionIDs:re.TeacherCommissionIDs,
				TeacherCommissionName:re.TeacherCommissionName,
				LabelName:re.Teacher+(re.TeacherCommissionName?'('+re.TeacherCommissionName+')':'')
			}]
		}
		if (assistantTeacherList.value.length == 0 && re.$AssiTeacher) {
			assistantTeacherList.value = cloneDeep(re.$AssiTeacher)
		}
	}
}

// 选择任课老师
const chooseEmpTableRef = ref();
function selectTableTeacher() {
	if (form.value.StartTime == '') {
		return ElMessage.warning(transToConfigDescript('请选择上课日期'));
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
		console.log(res);
		let d=res.data,
			other:any=res.other;
		// 配置项启用 并且 课程和老师都有所属科目，才需要验证
		if ( Check_Shift_Teacher_Subject.value && form.value.SubjectID !== '' && form.value.SubjectID != globalData.EMPTYGUID && res.data.SubjectList.length ) {
			let teacherSubjects = res.data.SubjectList.map((i:any)=>i.ID).join(',') 
			if ( teacherSubjects.indexOf(form.value.SubjectID) === -1 ) {
				ElMessageBox.confirm(transToConfigDescript('任课老师和课程的科目不一致，是否继续？'), '提示', {
					confirmButtonText: '继续',
                    cancelButtonText: '取消',
				}).then(()=>{
					handlerMainTeacherData(d, other) ;
				})
			} else {
				handlerMainTeacherData(d, other) ;
			}
		} else {
			handlerMainTeacherData(d, other) ;
		}
	})
}

//选择任课老师的处理
function handlerMainTeacherData(d: any, other: any) {
	// 检查任课老师是否与其他角色重复
	const duplicationCheck = checkTeacherDuplication(d.ID, d.Name, '任课老师');
	if (duplicationCheck.duplicate) {
		return;
	}
	
	// 检查是否已被设置为助教
	const isAlreadyAssistant = assistantTeacherList.value.some((item: any) => item.ID === d.ID);
	if (isAlreadyAssistant) {
		ElMessageBox.alert(transToConfigDescript('该老师已被设置为助教。'), '提示', {
			confirmButtonText: '确认',
		});
		return;
	}
	
	// 检查同一类别是否只对应一个老师
	const pass = checkTeacherType(false, other, assistantTeacherList.value);
	if (!pass) {
		return;
	}
	
	// 处理任课老师的设置
	if (mainTeacherList.value.length > 0 && mainTeacherList.value[0].ID === d.ID) {
		// 更新现有任课老师的类别
		const existingTeacher = mainTeacherList.value[0];
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
		mainTeacherList.value = [newTeacher];
	}
	// 设置任课老师相关信息
	mainTeacherSubjects.value = mainTeacherList.value[0]?.SubjectList.map((item:any)=>item.ID).join(',');

	if (EnableAssignTeacherCourseCampus.value) {
		getTeachersCourseTimes({
			teacherUserID: mainTeacherList.value[0].ID,
			campusID: form.value.CampusID
		}).then((data:any)=>{
			teacherScheduleTime.value = data.Data || '';
		})
	}
}

// 新增：统一的教师重复检查函数
function checkTeacherDuplication(teacherId: string, teacherName: string, excludeRole?: string) {
	const allRoles = [
		{ name: '任课老师', list: mainTeacherList.value },
		{ name: '助教', list: assistantTeacherList.value },
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

// 选择助教
function selectTableAssistant() {
	if (form.value.StartTime == '') {
		return ElMessage.warning(transToConfigDescript('请选择上课日期'));
	}
	chooseEmpTableRef.value?.open({
		multi: true,
		showTeacherType:true,
		disabledCondition:['StatusList']
	}).then((res: any) => {
		console.log(res);
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
			const pass = checkTeacherType(teacher, teacherCategory, assistantTeacherList.value, {
				ID: mainTeacherList.value.length > 0 ? mainTeacherList.value[0].ID : '',
				TeacherCommissionIDs: mainTeacherList.value.length > 0 ? mainTeacherList.value[0].TeacherCommissionIDs : ''
			});
			
			if (!pass) {
				continue; // 如果类别检查不通过，跳过这个教师
			}
			
			// 查找教师是否已存在
			const existingIndex = assistantTeacherList.value.findIndex((item: any) => item.ID === teacher.ID);
			
			if (existingIndex === -1) {
				// 新增助教
				const newAssistant = cloneDeep(teacher);
				newAssistant.TeacherCommissionIDs = teacherCategory ? teacherCategory.ID : '';
				newAssistant.TeacherCommissionName = teacherCategory ? teacherCategory.Name : '';
				newAssistant.LabelName = teacher.Name + (teacherCategory&&teacherCategory.Name ? `（${teacherCategory.Name}）` : '');
				assistantTeacherList.value.push(newAssistant);
			} else {
				// 更新现有助教的类别
				const existingAssistant = assistantTeacherList.value[existingIndex];
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
	});
}

// 老师相关变量
const mainTeacherList=ref([] as any)//老师 role:1
const assistantTeacherList=ref([] as any)//助教 role:2
const replaceTeacherList=ref([] as any)//替课老师 role:3
const followTeacherList=ref([] as any)//跟课老师 role:4
const pianoTeacherList=ref([] as any)//钢伴老师 role:5
const officeTeacherList=ref([] as any)//坐班老师 role:6
const consultantTeacherList=ref([] as any)//课程顾问 role:7

// 监听任课老师列表变化，触发表单验证
watch(() => mainTeacherList.value.length, () => {
	// 延迟触发验证，确保数据已更新
	setTimeout(() => {
		formRef.value?.validateField('TeacherName');
	}, 100);
});

// 处理TeacherList数组，根据Role分类到不同的数组中
function processTeacherList(teacherList: any[]) {
	// 清空所有数组
	mainTeacherList.value = [];
	assistantTeacherList.value = [];
	replaceTeacherList.value = [];
	followTeacherList.value = [];
	pianoTeacherList.value = [];
	officeTeacherList.value = [];
	consultantTeacherList.value = [];
	mainTeacherSubjects.value = '';
	
	if (!teacherList || !Array.isArray(teacherList)) {
		return;
	}
	
	teacherList.forEach((teacher: any) => {
		const teacherData = {
			ID: teacher.TeacherID,
			Name: teacher.TeacherName,
			LabelName: teacher.TeacherName + (teacher.TeacherCommissionName ? `（${teacher.TeacherCommissionName}）` : ''),
			...teacher
			// TeacherCommissionIDs: teacher.TeacherCommissionIDs,
			// TeacherCommissionName: teacher.TeacherCommissionName,
			// TeacherRole: teacher.TeacherRole,
			// TeacherPostionType: teacher.TeacherPostionType
		};
		
		switch (teacher.TeacherRole) {
			case 1: // 主教
				mainTeacherList.value.push(teacherData);
				// mainTeacherPositionTypeName.value = mainTeacherList.value[0].TeacherPostionType;
				mainTeacherSubjects.value = mainTeacherList.value[0].SubjectIDList.join(',');
				break;
			case 2: // 助教
				assistantTeacherList.value.push(teacherData);
				break;
			case 3: // 替课老师
				replaceTeacherList.value.push(teacherData);
				break;
			case 4: // 跟课老师
				followTeacherList.value.push(teacherData);
				break;
			case 5: // 钢伴老师
				pianoTeacherList.value.push(teacherData);
				break;
			case 6: // 坐班老师
				officeTeacherList.value.push(teacherData);
				break;
			case 7: // 课程顾问
				consultantTeacherList.value.push(teacherData);
				break;
		}
	});
}
function selectTeacher(type: string) {
	chooseEmpTableRef.value?.open({
		multi: type == 'replace' || type == 'piano' ? false : true,
		choosed: type == 'follow' ? followTeacherList.value : type == 'office' ? officeTeacherList.value : type == 'consultant' ? consultantTeacherList.value : [],
		disabledCondition:['StatusList']
	}).then((res: any) => {
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

function processArrangeSchedule() {
	let schList = shiftScheduleList.value || [],
		info:any = {},
		shiftAmount = 0;

	info.ShiftAmount = shiftAmount = (form.value.ShiftAmount || 0) * 1;
	info.ShiftScheduleID = globalData.EMPTYGUID;
	info.ChapterContent = "";
	info.CourseContent = form.value.CourseContent || "";
	info.CoursewareSettingList = form.value.CoursewareSettingList || [];
	info.Describe = form.value.Describe || "";
	info.InternalRemark = form.value.InternalRemark || "";
	if(schList.length > 0) { //有上课进度
		if(form.value.Finished == 0 && form.value.IsCover == 0) { // 1.未进行上课点名
			if(!EnableAddCourse_ShiftSchedule.value && !info.ShiftAmount) {
				//排课时，没确定每次课的上课进度，此时要从班级上拿到当前的进度+1
				var classShiftAmount = (form.value.ClassShiftAmount || 0) * 1; //班级上的当前上课进度
				info.ShiftAmount =shiftAmount= (classShiftAmount >= schList.length) ? schList.length : (classShiftAmount + 1);
			}
		}
		schList.forEach(function(item) {
			if(item.ShiftAmount == shiftAmount) {
				info.ShiftScheduleID = item.ID;
				info.ChapterContent = item.Title;
				info.CoursewareSettingList = item.CoursewareList;
				if(!form.value.CourseContent) {
					info.CourseContent = item.Content;
				}
			}
		});
	};

	// 将处理后的信息合并
	Object.assign(form.value, info);
}

// 使用 TeacherSelect 组件的变更处理（当未启用 EnableClassCommission 时）
function onMainTeacherSelectChange(val: any, teacher: any) {
	if (!val || !teacher) {
		mainTeacherList.value = []
		mainTeacherSubjects.value = ''
		teacherScheduleTime.value = ''
		return
	}
	// 构造任课老师数据
	const id = teacher.ID || teacher.id
	const name = teacher.Name || teacher.name
	const newTeacher: any = {
		ID: id,
		Name: name,
		LabelName: name
	}
	mainTeacherList.value = [newTeacher]

	if (EnableAssignTeacherCourseCampus.value) {
		getTeachersCourseTimes({
			teacherUserID: id,
			campusID: form.value.CampusID
		}).then((data:any)=>{
			teacherScheduleTime.value = data.Data || ''
		})
	}
}

function onAssistantSelectChange(val: any, assistants: any[]) {
	if (!assistants || assistants.length === 0) {
		assistantTeacherList.value = []
		return
	}
	assistantTeacherList.value = assistants.map((item:any)=>{
		const id = item.ID || item.id
		const name = item.Name || item.name
		return { ID: id, Name: name, LabelName: name }
	})
}

// 验证可约人数
function validateMaxStudentCount(rule: any, value: any, callback: any) {
	// 如果不是预约课或者没有开启开放预约，则不验证
	if (!(form.value.CourseMethod === 30 && form.value.IsSubscribeCourse === 1)) {
		callback();
		return;
	}
	
	if (!value) {
		callback(new Error('请输入可约人数'));
		return;
	}
	
	// 检查是否为正整数
	const numValue = parseInt(value);
	if (isNaN(numValue) || numValue <= 0 || numValue !== parseFloat(value)) {
		callback(new Error('可约人数必须为正整数'));
		return;
	}
	
	callback();
}

// 验证开课人数
function validateStartStudentCount(rule: any, value: any, callback: any) {
	// 如果不是预约课或者没有开启开放预约，则不验证
	if (!(form.value.CourseMethod === 30 && form.value.IsSubscribeCourse === 1)) {
		callback();
		return;
	}
	
	if (!value) {
		callback(new Error('请输入开课人数'));
		return;
	}
	
	// 检查是否为正整数
	const numValue = parseInt(value);
	if (isNaN(numValue) || numValue <= 0 || numValue !== parseFloat(value)) {
		callback(new Error('开课人数必须为正整数'));
		return;
	}
	
	callback();
}

// 提交表单
function submitForm() {
	formRef.value?.validate((valid: boolean) => {
		if (valid) {
			// 设置提交状态
			submitting.value = true;
			arrangeDataLoading.value = true;
			
			const _params = {
				CourseID: form.value.ID, // 排课ID
				ClassID: form.value.ClassID, // 班级ID
				CampusID: form.value.CampusID, // 校区ID
				StartTime: timeStartDate.value+' '+timeRange.value[0]+':00', // 排课开始时间
				EndTime: timeStartDate.value+' '+timeRange.value[1]+':00', // 排课结束时间
				CourseType: form.value.CourseType, // 上课方式，1=线下课，2=在线课
				ClassroomID: form.value.ClassroomID==''?EMPTYGUID:form.value.ClassroomID, // 教室ID
				Describe: form.value.Describe, // 对外备注
				ShiftAmount: form.value.ShiftAmount, // 课次，关联[tShiftSchedule].cShiftAmount
				ShiftScheduleID: form.value.ShiftScheduleID, // 上课进度计划ID，关联[tShiftSchedule].cID
				Content: form.value.CourseContent, // 上课内容/进度信息
				SubjectID: !EnableSubject.value ? globalData.EMPTYGUID : (form.value.SubjectID || globalData.EMPTYGUID), // 科目ID
				ShiftID: form.value.ShiftID, // 课程ID（关联tShift表）
				PlanID: form.value.PlanID, // 计划ID
				TeacherList: [
					// 遍历所有老师列表，构建TeacherList数组
					...mainTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 1, // 主教
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					})),
					...assistantTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 2, // 助教
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					})),
					...replaceTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 3, // 替课老师
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					})),
					...followTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 4, // 跟课老师
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					})),
					...pianoTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 5, // 钢伴老师
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					})),
					...officeTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 6, // 坐班老师
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					})),
					...consultantTeacherList.value.map((teacher: any) => ({
						TeacherID: teacher.ID,
						TeacherName: teacher.Name,
						TeacherRole: 7, // 课程顾问
						TeacherCommissionIDs: teacher.TeacherCommissionIDs || '',
					}))
				],
				CourseExSettings: {
					IsSubscribeCourse: form.value.IsSubscribeCourse, // 开放预约
					CourseStatus: form.value.CourseStatus, // 开课状态 0未开课1已开课
					MaxStudentCount: form.value.CourseMethod == 10?form.value.MaxStudentsAmount:form.value.MaxStudentCount, // 人数限制
					StartStudentCount: form.value.StartStudentCount, // 开课人数
					InternalRemark: form.value.InternalRemark, // 内部备注
					ExternalRemark: form.value.Describe, // 对外备注
				},
				CheckConflict: checkConflict.value,
				IsPostpone: schedulePostpone.value,
				IsCover: scheduleCover.value,
			}
			
			// 先检查是否有学员预约
			checkSubscribeCourseStudent({
				id: form.value.ID
			}).then((checkRes: any) => {
				console.log(checkRes)
				if (checkRes.ErrorMsg == 1) {
					// 有学员预约，弹窗确认
					ElMessageBox.confirm(
						transToConfigDescript('当前排课记录已有学员预约，确定修改？<br/>如确定修改，请人工通知所有预约了此课程的学员。'),
						'确认',
						{
							confirmButtonText: '确定',
							cancelButtonText: '取消',
							type: 'warning',
							dangerouslyUseHTMLString: true,
						}
					).then(() => {
						performSubmit(_params);
					}).catch(() => {
						// 用户取消，重置状态
						submitting.value = false;
						arrangeDataLoading.value = false;
					});
				} else {
					performSubmit(_params);
				}
			}).catch((error: any) => {
				// 检查学员预约失败，重置状态
				console.error('检查学员预约失败:', error);
				submitting.value = false;
				arrangeDataLoading.value = false;
			});
		}
	});
}
const conflictPromptRef = ref()
// 执行真实提交的函数
function performSubmit(_params: any) {
	editCourseInfoByID(_params).then((res:any) => {
		ElMessage.success('修改成功');
		_resolve && _resolve();
		drawer.value = false;
		submitting.value = false;
		arrangeDataLoading.value = false;
		event.emit("arrange-table-list-refresh",{refreshTotal:true});
	}).catch((error: any) => {
		if(error.ErrorCode==409){
			conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{}).catch((back:any)=>{
				if(back&&back.closeCurrent){
					drawer.value=false
				}
			})
		}
		submitting.value = false;
		arrangeDataLoading.value = false;
	});
}

// 获取课程的上课进度
function getShiftSchedule(){
	getCourseShiftSchedule({
		shiftId: shiftID.value
	}).then((res:any) => {
		shiftScheduleList.value = res.Data;
	})
}

// 获取业务规则配置
function getAdvanceConfig(){
	querySysConfig({
		campusID: '',
		type: 0,
		configNames: 'Check_Shift_Teacher_Subject,CourseTeacherRequired,CourseRuleSetting' //配置项名称（多个时用逗号分隔）
	}).then((res:any) => {
		var data = res.Data;
		data.forEach((item:any) => {
			if (item.Name == "Check_Shift_Teacher_Subject" && item.Value == 1) {
				Check_Shift_Teacher_Subject.value = true;
			}
			if (item.Name == 'CourseTeacherRequired' && item.Value == 1) {
				CourseTeacherRequired.value = true;
			}
			if (item.Name == 'CourseRuleSetting') {
				let obj=JSON.parse(item.OtherParameter)
				MinCourseTime.value=obj.MinCourseTime
				MaxCourseTime.value=obj.MaxCourseTime
				console.log(MinCourseTime.value, MaxCourseTime.value)
			}
		})
	})
}


async function getClassesSubject(){
	const res = await getClassSubject({
		classId: form.value.ClassID
	});
	console.log(res);
	return res;
}

// 获取排课数据
const SUBJECT=computed(()=>{
    return dictFields.value('SUBJECT').filter((item:any)=>item.Status==1);
})
const EMPTYGUID = '00000000-0000-0000-0000-000000000000'
async function getArrangeData() {
	arrangeDataLoading.value = true;
	form.value = {};
	try {
		const res = await getCourseDetailByID(courseID.value);
		form.value = res.Data;
		form.value.ClassroomID = form.value.ClassroomID==EMPTYGUID?'':form.value.ClassroomID;
		form.value.ClassroomName=form.value.ClassroomName||''
		shiftSubjectId.value=res.Data.ShiftSubjectID
		initTimeRange();
		IsMonthCourse.value=res.Data.Unit == 3 && !IsOpenShiftForDay.value; // 课程单位：1小时、2次、3按月;isOpenShiftForDay：0按月计费（默认），1按天计费。
		EnableSubject.value=res.Data.EnableSubject == 1;
		if(EnableSubject.value){ // 全科课程处理
			let subjectData = await getClassesSubject();
			let obj=Class_classifyTeachersBySubject(subjectData.Data,SUBJECT.value)
			if(!CourseIsClassSubject.value){
				subjectList.value=obj.concatSubjectList
			}else{
				subjectList.value=obj.SubjectArr
			}
			teacherSubjectObj=obj.Fa
		}else{
			form.value.SubjectID = res.Data.ShiftSubjectID;
		}

		if (IsMonthCourse.value && !EnableMonthShiftCourse) {
			checkConflict.value = 0;
		}

		// 处理TeacherList数组
		if (res.Data.TeacherList) {
			processTeacherList(res.Data.TeacherList);
		}
		// 处理上课进度
		processArrangeSchedule();
		// 处理对外备注
		form.value.Describe = res.Data.ExternalRemark || '';
	} finally {
		arrangeDataLoading.value = false;
	}
}

let _resolve: any = null,
	_reject: any = null,
	courseID = ref(''),
	campusID = ref(''),
	shiftID = ref('');

function open(params: any) {
	campusID.value = params.CampusID;
	courseID.value = params.ID;
	shiftID.value = params.ShiftID;
	getArrangeData();
	getAdvanceConfig();
	getShiftSchedule();
	
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		drawer.value = true
	})
}

function close() {
	drawer.value = false
	// 清理弹窗状态
	currentPopoverVisible.value = false;
	currentTriggerRef.value = null;
	checkConflict.value=1
	_reject && _reject()
}

defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
.editArrangeForm {
	.drawer-body-wrap {
		min-width: 1150px;
		height: 100%;
		display: flex;
		flex-direction: column;
		
		.two-column-wrap {
			display: flex;
			gap: 0 40px;
			flex-wrap: wrap;
			.half-width {
				width: calc(33.333% - 27px);
				min-width: 0;
			}
		}
	}

	.form-section {
		background-color: #fff;
		margin: 16px 0;
		border-radius: 8px;
		padding: 16px 20px 0 16px;
	}

	.appointment-control {
		font-size: 12px;
		color: #909399;
	}

	.description-text {
		position: absolute;
		top: 42px;
		font-size: 12px;
		color: #909399;
		line-height: 1;
	}

	.custom-checkbox-height {
		:deep(.wtwo-checkbox) {
			min-height: auto !important;
			height: auto !important;
		}
	}

	// 设置上课进度 form-item label 宽度为 100%
	.progress-form-item {
		:deep(.wtwo-form-item__label) {
			width: 100% !important;
			padding-right: 0;
		}
	}
	
	.custom-select-trigger {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 32px;
		border: 1px solid #dcdfe6;
		border-radius: 4px;
		background-color: #fff;
		cursor: pointer;
		transition: border-color 0.2s;
	
		&.disabled {
			color: #a8abb2;
			background-color: #f5f7fa;
			cursor: not-allowed;
			&:hover {
				border-color: #dcdfe6;
			}
		}
		
		&:hover {
			border-color: #c0c4cc;
		}
		
		.trigger-text {
			display: block;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			color: #606266;
			font-size: 14px;
			flex: 1;
			min-width: 0;
		}
		
		.trigger-arrow {
			transition: transform 0.2s;
			color: #c0c4cc;
			
			&.is-reverse {
				transform: rotate(180deg);
			}
		}
	
		// 去掉 el-input 的默认样式
		:deep(.wtwo-input) {
			border: none;
			background: transparent;
			box-shadow: none;
			padding: 0 11px;
			height: auto;
			flex: 1;
			min-width: 0;
			display: flex;
			
			.wtwo-input__wrapper {
				border: none;
				background: transparent;
				box-shadow: none;
				padding: 0;
				flex: 1;
				min-width: 0;
			}
			
			.wtwo-input__inner {
				border: none;
				box-shadow: none;
				padding: 0;
				height: auto;
				color: #606266;
				font-size: 14px;
				flex: 1;
				min-width: 0;
			}
		}
	}

	// 设置 el-input disabled 状态的字体颜色
	:deep(.wtwo-input.is-disabled) {
		.wtwo-input__inner {
			// color: #a8abb2 !important;
			// -webkit-text-fill-color: #a8abb2 !important;
			color: #333 !important;
		}
	}
}
</style>

<style>
.edit-arrange-form-class-content-popover {
	padding: 0 !important;
	.class-content-table-component {
		padding: 8px;
		
		.class-content-table {
			:deep(.wtwo-table) {
				border: none;
				
				.wtwo-table__header {
					background-color: #f5f7fa;
					
					.wtwo-table__cell {
						background-color: #f5f7fa;
						border-bottom: 1px solid #ebeef5;
						font-size: 12px;
						color: #909399;
						font-weight: 500;
						padding: 8px 0;
					}
				}
				
				.wtwo-table__body {
					.wtwo-table__row {
						cursor: pointer;
						transition: background-color 0.2s;
						
						&:hover {
							background-color: #f5f7fa;
						}
						
						.wtwo-table__cell {
							font-size: 13px;
							color: #606266;
							line-height: 1.4;
							padding: 8px 0;
							border-bottom: 1px solid #f0f0f0;
						}
					}
				}
			}
		}
	}
}
</style>