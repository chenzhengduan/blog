<template>
	<div class="wtwo-arrange-table-list page-box">
		<!-- 骨架屏 - 仅首次加载时显示 -->
		<div v-if="isInitialLoad && loading" class="skeleton-wrapper">
			<!-- 工具栏骨架 -->
			<div class="skeleton-toolbar">
				<el-skeleton-item variant="text" style="width: 180px; height: 20px;" />
				<div class="skeleton-toolbar-right">
					<el-skeleton-item variant="button" style="width: 80px; height: 32px; margin-right: 8px;" />
					<el-skeleton-item variant="button" style="width: 80px; height: 32px; margin-right: 8px;" />
					<el-skeleton-item variant="button" style="width: 90px; height: 32px;" />
				</div>
			</div>
			
			<!-- 表格骨架 -->
			<div class="skeleton-table">
				<el-skeleton animated :throttle="100">
					<template #template>
						<div class="skeleton-table-row" v-for="i in 10" :key="i">
							<el-skeleton-item variant="text" style="width: 12%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 15%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 8%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 18%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 12%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 10%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 10%; height: 16px;" />
							<el-skeleton-item variant="text" style="width: 8%; height: 16px;" />
						</div>
					</template>
				</el-skeleton>
			</div>
			
			<!-- 分页骨架 -->
			<div class="skeleton-pagination">
				<el-skeleton-item variant="text" style="width: 280px; height: 28px;" />
			</div>
		</div>
		
		<!-- 实际内容 -->
		<div v-else class="wtwo-flex-card-box">
			<div class="tool-bar flex-center justify-between">
				<div>共{{ page.TotalCount||0 }}节排课<el-divider direction="vertical" />
					<span class="color-[#909399] text-13px">{{totalInfo.NoFinishCount}}{{transToConfigDescript('未上课')}}，{{totalInfo.FinishCount}}{{transToConfigDescript('已上课')}}，{{ totalInfo.CancelCount }}已取消</span>
				</div>
				<div class="btn-wraper">
					<el-dropdown trigger="click" class="mr-12px">
						<el-button class="px-5px!">
							<el-icon size="20px">
							<svg aria-hidden="true">
								<use xlink:href="#w2-gengduocaozuo"></use>
							</svg>
							</el-icon>
						</el-button>
						<template #dropdown>
							<el-dropdown-menu style="max-height: 60vh;">
								<el-dropdown-item v-if="NewCourse_CourseExport" @click="exportData">导出排课</el-dropdown-item>
								<el-dropdown-item v-if="NewCourse_StudentCourse" @click="excelImportCourse">导入一对一</el-dropdown-item>
								<el-dropdown-item @click="showPlanCourse">排课进度</el-dropdown-item>
								<el-divider />
								<el-dropdown-item @click="editBatchContent()">{{transToConfigDescript('修改上课内容')}}</el-dropdown-item>
								<el-dropdown-item v-if="ClassAutoCharge<2&&CourseBeginPower" @click="openBatchCheckin">一对一批量点名
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
										<template #content>
											排课符合下列要求，才支持批量点名<br/>
											{{transToConfigDescript('1.排课属于同一上课校区')}}<br/>
											{{transToConfigDescript('2.排课状态为“未上课”状态')}}<br/>
										</template>
									</el-tooltip>
								</el-dropdown-item>
								<el-dropdown-item @click.native="printCourseStudentList()">打印点名表</el-dropdown-item>
								<el-dropdown-item v-if="NewCourse_CourseEdit" @click.native="openBatchEditCourse">批量修改</el-dropdown-item>
								<template v-if="NewCourse_CoursePlanViaCopy">
									<el-dropdown-item v-if="!EnableCourseApplication" @click.native="moveOrCopyCourse(2)">移动排课</el-dropdown-item>
									<el-dropdown-item @click.native="moveOrCopyCourse(1)">复制排课</el-dropdown-item>
								</template>
								<el-dropdown-item v-if="NewCourse_CourseCancel" @click.native="removeOrCancelBatch(1)">取消排课</el-dropdown-item>
								<el-dropdown-item v-if="NewCourse_CourseDelete" @click.native="removeOrCancelBatch(0)">删除排课</el-dropdown-item>
							</el-dropdown-menu>
						</template>
					</el-dropdown>
					<el-button class="px-5px! mr-12px" @click.native="setTableColumnsOpen" :disabled="userDefinedColumns.length==0" title="列表字段显示设置">
						<el-icon size="20px">
							<svg aria-hidden="true">
								<use xlink:href="#w2-xuanzelie"></use>
							</svg>
						</el-icon>
					</el-button>
					<el-dropdown trigger="click" class="mr-12px">
						<el-button class="pr-10px!">约课管理<el-icon class="ml-4px"><arrow-down /></el-icon></el-button>
						<template #dropdown>
							<el-dropdown-menu style="max-height: 60vh;">
								<el-dropdown-item @click="openSubscribeRecord">约课记录</el-dropdown-item>
								<el-dropdown-item v-if="!EnablePreSubscribeCourse&&IsOpenNewMallDecoration" @click="openAppointQrcode">约课二维码</el-dropdown-item>
								<el-dropdown-item v-if="!EnablePreSubscribeCourse&&NewCourse_SubscribeCourseRuleQuery" @click="openAppointRule">学员约课规则</el-dropdown-item>
								<el-dropdown-item v-if="!EnablePreSubscribeCourse&&NewCourse_SettingSubscribeCourseTeacherDesc" @click="openAppointTeacher">{{transToConfigDescript('约课老师介绍')}}</el-dropdown-item>
							</el-dropdown-menu>
						</template>
					</el-dropdown>
					<el-button @click="openObjectFreeTime">查忙闲</el-button>
					<el-button v-if="NewCourse_ClassCourse||NewCourse_StudentCourse||NewCourse_SubscribeCourse" type="primary" @click="openAdd"
						>新增排课</el-button
					>
				</div>
			</div>
			<div
				class="table-wrap scroll-box"
				ref="tableContainerRef"
				v-loading="loading"
				element-loading-target=".table-wrap"
			>
				<el-table
					:data="list"
					ref="customTable"
					width="100%"
					show-summary
					:summary-method="getSummaries"
					:max-height="`calc(-335px + 100vh)`"
					resizable
					border
					@sort-change="sortChange"
					:default-sort="{ prop: sort.sort, order: sort.desc==1?'descending':'ascending' }"
					@selection-change="handleSelectionChange"
				>
					<template #empty>
						<el-empty
							:image="globalData.emptyPng"
							description="暂无数据"
							:image-size="100"
						></el-empty>
					</template>
					<el-table-column
						fixed="left"
						type="selection"
						width="30"
						key="selection"
						:selectable="selectable"
					></el-table-column>
					<el-table-column
						fixed="left"
						:label="transToConfigDescript('上课班级/学员')"
						prop="ClassName"
						key="ClassName"
						min-width="220">
						<template #default="scope">
							<div class="flex-center" @click="showShiftDetail(scope.row)">
								<el-text
									type="primary"
									class="cursor-pointer ellipsis-single block"
									:title="scope.row.ClassName"
									:class="{'color-[#a8abb2]!':scope.row.IsApprovaling,'cursor-pointer':!scope.row.IsApprovaling,'cursor-not-allowed':scope.row.IsApprovaling}"
									>{{ scope.row.ClassName || '-'}}
									{{ scope.row.IsApprovaling?scope.row.IsApprovaling==1?'【修改申请中】':'【删除申请中】':'' }}
								</el-text>
								<el-tag
									v-if="scope.row.CourseMethod==30||(scope.row.CourseMethod==10&&scope.row.IsSubscribeCourse)"
									class="ml-4px px-2px! h-18px!"
									type="warning"
									size="small"
									:hit="true"
									>约</el-tag
								>
								<el-tag
									v-if="scope.row.ClassType==2"
									class="ml-4px px-2px! h-18px!"
									type="warning"
									size="small"
									:hit="true"
									>补</el-tag
								>
								<el-tag
									v-if="scope.row.ClassType==3"
									class="ml-4px px-2px! h-18px!"
									type="warning"
									size="small"
									:hit="true"
									>试</el-tag
								>
							</div>
						</template>
					</el-table-column>
					<el-table-column
						:label="transToConfigDescript('上课课程')"
						prop="ShiftName"
						key="ShiftName"
						min-width="180"
						sortable="custom"
						show-overflow-tooltip
					></el-table-column>
					<template v-for="item in definedColumns" :key="item.Key">
						<el-table-column
							v-if="item.Key=='IsOneToOne'"
							label="教学形式"
							prop="IsOneToOne"
							key="IsOneToOne"
							width="120"
							sortable="custom"
						>
							<template #header>
								<div class="flex-center" style="display: inline-flex;">
									教学形式
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
										<template #content>
											<div v-html="transToConfigDescript(item.Tips)"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
							<template #default="scope">
								{{
									scope.row.IsOneToOne == 0
										? '集体班'
										: scope.row.IsOneToOne == 1
										? '一对一'
										: '一对多'
								}}
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='CampusName'"
							:label="transToConfigDescript('上课校区')"
							prop="CampusName"
							key="CampusName"
							width="140"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='StartTime'"
							:label="transToConfigDescript('上课时间')"
							prop="StartTime"
							key="StartTime"
							width="240"
							sortable="custom"
						>
							<template #default="scope">
								<span v-if="scope.row.Unit!=3||IsOpenShiftForDay">{{ dayjs(scope.row.StartTime).format('YYYY-MM-DD HH:mm') }}-{{ dayjs(scope.row.EndTime).format('HH:mm') }}[{{dayjs(scope.row.StartTime).format('dddd') }}]</span>
								<span v-if="scope.row.Unit==3&&!IsOpenShiftForDay">{{ dayjs(scope.row.StartTime).format('YYYY-MM-DD') }}&nbsp;[{{dayjs(scope.row.StartTime).format('dddd') }}]</span>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='Duration'"
							:label="transToConfigDescript('上课时长')"
							prop="Duration"
							key="Duration"
							width="140"
							sortable="custom"
						>
							<template #default="scope">
								{{ formatDuration(scope.row.Duration) }}
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='Finished'"
							:label="transToConfigDescript('上课状态')"
							prop="Finished"
							key="Finished"
							width="100"
							sortable="custom"
						>
							<template #default="scope">
								<el-tag
									effect="light"
									:type="
										scope.row.Finished == 1
											? 'success'
											: scope.row.Finished == 0
											? 'primary'
											: 'info'
									"
									size="small"
									>{{ scope.row.FinishedName }}</el-tag
								>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='ConfirmUserName'"
							label="点名人"
							prop="ConfirmUserName"
							key="ConfirmUserName"
							width="100"
							show-overflow-tooltip
						>
							<template #default="scope">
								<el-text type="primary" class="cursor-pointer" @click.native="showShiftDetail(scope.row,'records')">{{
									scope.row.ConfirmUserName
								}}</el-text>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='IsClassTeacher'"
							label="点名人是否为老师"
							prop="IsClassTeacher"
							key="IsClassTeacher"
							width="140"
							align="center"
						></el-table-column>
						<!-- sortable="custom" -->
						<el-table-column
							v-if="item.Key=='StudentAttendanceCount'"
							label="实到/应到"
							prop="StudentAttendanceCount"
							key="StudentAttendanceCount"
							width="120"
							align="center"
						>
							<template #default="scope">
								<el-text type="primary" class="cursor-pointer" @click="showShiftDetail(scope.row)"
									>{{
										scope.row.Finished == 1
											? scope.row.StudentAttendanceCount
											: '-'
									}}/{{ scope.row.StudentCount }}</el-text
								>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='CostCount'"
							label="计费人数"
							prop="CostCount"
							key="CostCount"
							width="100"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='CostNum'"
							label="计费数量"
							prop="CostNum"
							key="CostNum"
							width="120"
						>
							<template #header>
								<div class="flex-center">
									计费数量
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
										<template #content>
											<div v-html="item.Tips"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
						</el-table-column>
						
						<el-table-column
							v-if="item.Key=='ChapterTitle'"
							label="章节内容"
							prop="ChapterTitle"
							key="ChapterTitle"
							width="120"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							v-if="item.Key=='CourseContent'"
							:label="transToConfigDescript('上课内容')"
							prop="CourseContent"
							key="CourseContent"
							width="120"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							v-if="item.Key=='CoursewareSettingList'"
							label="课件"
							prop="CoursewareSettingList"
							key="CoursewareSettingList"
							width="120"
							show-overflow-tooltip
						>
							<template #default="scope">
								<el-text
									type="primary"
									class="cursor-pointer"
									v-for="item in scope.row.CoursewareSettingList"
									@click="showWareDetail(scope.row, item.FileID)"
								>
									{{ item.FileName }}&nbsp;&nbsp;</el-text
								>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='ImgAddress'"
							:label="transToConfigDescript('上课照片')"
							prop="ImgAddress"
							key="ImgAddress"
							width="120"
						>
							<template #default="scope">
								<el-image
									v-if="scope.row.ImgAddress"
									:preview-teleported="true"
									:preview-src-list="[scope.row.ImgAddress]"
									:src="scope.row.ImgAddress"
									style="
										width: 40px;
										height: 40px;
										border-radius: 100px;
									"
									alt=""
								/>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='SubjectName'"
							:label="transToConfigDescript('上课科目')"
							prop="SubjectName"
							key="SubjectName"
							width="120px"
							show-overflow-tooltip
							sortable="custom"
						>
							<template #header>
								<div class="flex-center inline-flex!">
									{{transToConfigDescript('上课科目')}}
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
										<template #content>
											<div v-html="transToConfigDescript(item.Tips)"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='ClassPlanNum_ClassPlanCount'"
							:label="transToConfigDescript('上课进度')"
							prop="ClassPlanNum_ClassPlanCount"
							key="ClassPlanNum_ClassPlanCount"
							width="120"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='CourseType'"
							:label="transToConfigDescript('上课方式')"
							prop="CourseType"
							key="CourseType"
							width="100px"
							sortable="custom"
						>
							<template #default="scope">{{
								scope.row.CourseType == 2 ? '线上课' : '线下课'
							}}</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='ClassroomName'"
							:label="transToConfigDescript('上课教室')"
							prop="ClassroomName"
							key="ClassroomName"
							width="120"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='TeacherName'"
							:label="transToConfigDescript('任课老师')"
							prop="TeacherName"
							key="TeacherName"
							width="100"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='TeacherPostionTypeName'"
							:label="transToConfigDescript('任课老师类型')"
							prop="TeacherPostionTypeName"
							key="TeacherPostionTypeName"
							width="120"
							show-overflow-tooltip
							align="center"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='TeacherPositionName'"
							:label="transToConfigDescript('任课老师职位')"
							prop="TeacherPositionName"
							key="TeacherPositionName"
							width="120"
							show-overflow-tooltip
							align="center"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='AssistantTeacherName'"
							label="助教"
							prop="AssistantTeacherName"
							key="AssistantTeacherName"
							min-width="120"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							v-if="item.Key=='MasterName'"
							:label="transToConfigDescript('学管师')"
							prop="MasterName"
							key="MasterName"
							width="100"
							show-overflow-tooltip
						></el-table-column>
						<el-table-column
							v-if="item.Key=='HeadMasterUserName'"
							:label="transToConfigDescript('班主任')"
							prop="HeadMasterUserName"
							key="HeadMasterUserName"
							width="100"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='ShiftTermName'"
							:label="transToConfigDescript('课程期段')"
							prop="ShiftTermName"
							key="ShiftTermName"
							width="120"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='ShiftGradeName'"
							:label="transToConfigDescript('课程年级')"
							prop="ShiftGradeName"
							key="ShiftGradeName"
							width="120"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='ShiftSubjectName'"
							:label="transToConfigDescript('课程科目')"
							prop="ShiftSubjectName"
							key="ShiftSubjectName"
							width="120"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='ShiftCategoryName'"
							:label="transToConfigDescript('课程类型')"
							prop="ShiftCategoryName"
							key="ShiftCategoryName"
							width="120"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='ShiftClassTypeName'"
							:label="transToConfigDescript('课程班型')"
							prop="ShiftClassTypeName"
							key="ShiftClassTypeName"
							width="120"
							show-overflow-tooltip
							sortable="custom"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='IsSubscribeCourse'"
							label="开放预约"
							prop="IsSubscribeCourse"
							key="IsSubscribeCourse"
							width="100"
						>
							<template #header>
								<div class="flex-center">
									开放预约
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
										<template #content>
											<div v-html="item.Tips"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
							<template #default="scope">
								{{ scope.row.CourseMethod==20?'-':(scope.row.IsSubscribeCourse?'已开放':'不开放') }}
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='SubscribeStudentCount'"
							label="已约"
							prop="SubscribeStudentCount"
							key="SubscribeStudentCount"
							width="100"
						>
							<template #header>
								<div class="flex-center">
									已约
									<el-tooltip
										class="box-item"
										effect="dark"
										placement="top"
										popper-class="arrange-brief-tooltip"
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
										<template #content>
											<div v-html="transToConfigDescript(item.Tips)"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
							<template #default="scope">
								<el-link v-if="scope.row.CourseMethod==30||(scope.row.CourseMethod==10&&(scope.row.IsSubscribeCourse==1||scope.row.SubscribeStudentCount))"
									type="primary" underline="never" @click="showShiftDetail(scope.row,'reserved')"
								>{{ scope.row.SubscribeStudentCount}}</el-link>
								<span v-else>-</span>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='MaxStudentCount'"
							label="在课/最大可约"
							prop="MaxStudentCount"
							key="MaxStudentCount"
							width="140"
						>
							<template #header>
								<div class="flex-center">
									在课/最大可约
									<el-tooltip
										class="box-item"
										effect="dark"
										placement="top"
										popper-class="arrange-brief-tooltip"
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
										<template #content>
											<div v-html="transToConfigDescript(item.Tips)"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
							<template #default="scope">
								<el-link v-if="scope.row.CourseMethod==30||(scope.row.CourseMethod==10&&scope.row.IsSubscribeCourse==1)"
									type="primary" underline="never" @click="showShiftDetail(scope.row,'reserved')"
								>{{scope.row.StudentCount}}/{{ scope.row.CourseMethod==10?(scope.row.MaxStudentsAmount||'-'):(scope.row.MaxStudentCount||'-')}}</el-link>
								<span v-else>-</span>
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='StartStudentCount'"
							label="开课人数"
							prop="StartStudentCount"
							key="StartStudentCount"
							width="100"
						>
							<template #header>
								<div class="flex-center">
									开课人数
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
										<template #content>
											<div v-html="item.Tips"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
							<template #default="scope">{{scope.row.CourseMethod==30?scope.row.StartStudentCount:'-' }}</template>
						</el-table-column>
						<el-table-column 
							v-if="item.Key=='CourseStatus'"
							label="开课状态" 
							prop="CourseStatus" 
							key="CourseStatus"
							width="100"
						>
							<template #header>
								<div class="flex-center">
									开课状态
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
										<template #content>
											<div v-html="item.Tips"></div>
										</template>
									</el-tooltip>
								</div>
							</template>
							<template #default="scope">
								{{ scope.row.CourseMethod==30?(scope.row.CourseStatus?'已开课':'未开课'):'-' }}
							</template>
						</el-table-column>
						<el-table-column
							v-if="item.Key=='InternalRemark'"
							label="对内备注"
							width="120"
							prop="InternalRemark"
							key="InternalRemark"
							show-overflow-tooltip
						></el-table-column>
						<!-- <el-table-column
							v-if="item.Key=='Describe'"
							label="对外备注"
							width="120"
							prop="Describe"
							key="Describe"
							show-overflow-tooltip
						></el-table-column> -->
						<el-table-column
							v-if="item.Key=='CreateRule'"
							label="创建规则"
							width="100"
							prop="CreateRule"
							key="CreateRule"
						></el-table-column>
						<el-table-column
							v-if="item.Key=='CreateTime'"
							label="创建时间"
							prop="CreateTime"
							key="CreateTime"
							width="160"
							sortable="custom"
						>
							<template #default="scope">
								{{
									dayjs(scope.row.CreateTime).format(
										'YYYY-MM-DD HH:mm:ss'
									)
								}}
							</template>
						</el-table-column>
					</template>
					<el-table-column
						label="操作"
						prop="op"
						key="op"
						min-width="60"
						fixed="right"
					>
						<template #default="scope">
							<div class="flex-center">
								<el-dropdown trigger="click" v-if="scope.row.IsClassFinished!=1 && !scope.row.IsApprovaling">
									<el-link type="primary" underline="never"
										>更多</el-link
									>
									<template #dropdown>
										<el-dropdown-menu style="max-height: 60vh;">
											<el-dropdown-item @click="rollCall(scope.row)" v-if="scope.row.Finished!=2&&CourseBeginPower">{{transToConfigDescript('点名上课')}}</el-dropdown-item>
											<el-dropdown-item @click.native="showShiftDetail(scope.row)">{{transToConfigDescript('上课学员')}}</el-dropdown-item>
											<el-dropdown-item v-if="scope.row.Finished==0" @click.native="getScanCode(scope.row)">签到二维码</el-dropdown-item>
											<el-divider/>
											<el-dropdown-item v-if="NewCourse_CourseStudentAdjustLesson&&scope.row.Finished!=2" @click.native="openAdjustCourse(scope.row)">临时调课</el-dropdown-item>
											<el-dropdown-item v-if="NewCourse_CourseBeginCancel&&scope.row.Finished!=0&&scope.row.Finished!=2" @click.native="disCourse(scope.row)">{{transToConfigDescript('撤销上课')}}</el-dropdown-item>
											<el-dropdown-item v-if="EnablePrintOneToOneCourseRecord&&NewCourse_PrintCourseRecord
												&&scope.row.Finished==1&&scope.row.IsOneToOne==1" @click="printOneToOne(scope.row)">{{transToConfigDescript('打印上课凭证')}}</el-dropdown-item>
											<el-dropdown-item v-if="scope.row.Finished!=2" @click.native="printCourseStudentList(scope.row)">打印点名表</el-dropdown-item>
											<el-dropdown-item @click.native="copyMainInfo(scope.row)">复制主要信息</el-dropdown-item>
											<el-dropdown-item v-if="NewCourse_LookLiveStreamingLink&&IsOpenLiveStream==2&&scope.row.LiveStreamingLink" @click.native="openLiveLink(scope.row)">查看直播链接</el-dropdown-item>
											<el-divider/>
                                            <el-dropdown-item v-if="scope.row.PlanID && scope.row.PlanID !== globalData.EMPTYGUID" @click.native="openBatchScheduleDialog(scope.row)">管理本批次排课</el-dropdown-item>
											<el-dropdown-item v-if="scope.row.Finished!=2 && scope.row.Unit!=3" @click.native="editContent(scope.row)">{{transToConfigDescript('修改上课内容')}}</el-dropdown-item>
											<el-dropdown-item v-if="NewCourse_CourseEdit&&scope.row.Finished === 0" @click.native="showShiftEdit(scope.row)">编辑排课</el-dropdown-item>
											<el-dropdown-item v-if="NewCourse_CourseCancel&&scope.row.Finished!=1&&scope.row.Finished!=2" @click.native="cancel(scope.row)">取消排课</el-dropdown-item>
											<el-dropdown-item v-if="NewCourse_CourseDelete&&scope.row.Finished!=1" @click.native="delCourse(scope.row)">删除排课</el-dropdown-item>
										</el-dropdown-menu>
									</template>
								</el-dropdown>
							</div>
						</template>
					</el-table-column>
				</el-table>
				<div
					class="pageination-wrapper flex-end m-t-10px"
					v-if="list.length > 0"
				>
					<el-pagination
						@size-change="handleSizeChange"
						@current-change="handleCurrentChange"
						:current-page="page.PageIndex"
						:page-sizes="[10, 20, 50, 100, 200]"
						:page-size="page.PageSize"
						layout="total, sizes, prev, jumper ,next"
						:total="page.TotalCount"
						size="small"
					>
					</el-pagination>
				</div>
			</div>
		</div>
		<addArrangeForm ref="addArrangeFormRef"></addArrangeForm>
		<editArrangeForm ref="editArrangeFormRef"></editArrangeForm>
		<arrangeInfo ref="arrangeInfoRef"></arrangeInfo>
		<chooseSingleCampus ref="chooseSingleCampusRef"></chooseSingleCampus>
		<setTableColumns ref="setTableColumnsRef"></setTableColumns>
		<qrCodePop ref="qrCodePopRef"></qrCodePop>
		<workFlowPopup ref="workFlowPopupRef"></workFlowPopup>
		<cancelCourseForm ref="cancelCourseFormRef"></cancelCourseForm>
		<batchOneToOneCourse ref="batchOneToOneCourseRef"></batchOneToOneCourse>
		<copyArrageCourse ref="copyArrageCourseRef"></copyArrageCourse>
		<arrangeSubscribeRecord ref="arrangeSubscribeRecordRef"></arrangeSubscribeRecord>
		<editArrangeContent ref="editArrangeContentRef"></editArrangeContent>
		<printArrangeStudent ref="printArrangeStudentRef"></printArrangeStudent>
		<qrSignCodePop ref="qrSignCodePopRef"></qrSignCodePop>
		<batchEditCourse ref="batchEditCourseRef"></batchEditCourse>
		<disCourseConfirm ref="disCourseConfirmRef"></disCourseConfirm>
		<appointTeacherIntro ref="appointTeacherIntroRef"></appointTeacherIntro>
		<classinLiveLink ref="classinLiveLinkRef"></classinLiveLink>
		<adjustCourse ref="adjustCourseRef"></adjustCourse>
		<ObjectFreeTime ref="objectFreeTimeRef"></ObjectFreeTime>
		<addScheduleForm ref="addScheduleFormRef"></addScheduleForm>
		<subscribeRuleDrawer ref="subscribeRuleDrawerRef"></subscribeRuleDrawer>
        <batchArrangeInfo ref="batchArrangeInfoRef"></batchArrangeInfo>
	</div>
</template>
<script lang="ts" setup>
import {
	chekSeeCourseware,
	deleteCourseList,
	deleteCourseAsk,
	getFilesUrl,
	queryCourseNew,
	queryCourseTotal,
	checkStudentAvailabilityStatus,
	queryCourseExport,
} from '@/api/arrange'
import { assignPage, checkPageIndex, exportFile, IPageModel, syncUserColumns } from '@/utils'
import { dayjs, TableColumnCtx } from 'element-plus'
import useEvent from '@/config/event'
import {
	getCurrentInstance,
	ref,
	VNode,
	watch,
	onMounted,
	reactive,
	computed,
	onUnmounted,
} from 'vue'

// 定义 emit
const emit = defineEmits<{
  'component-ready': []
}>()
import addArrangeForm from '../popup/addArrangeForm.vue'
import arrangeInfo from '../popup/arrangeInfo.vue';
import { useConfigs, useCurrentCampuses } from '@/store'
import { checkJingBeiFinanceLock, getUserColumns } from '@/api'
import { cloneDeep } from 'lodash'
import setTableColumns from '@/components/popup/setTableColumns.vue'
import qrCodePop from '../popup/qrCodePop.vue'
import { nextTick } from 'vue'
import { formatDuration } from '@/utils/timeUtils'
import workFlowPopup from '../popup/workFlowPopup.vue'
import editArrangeForm from '../popup/editArrangeForm.vue'
import batchOneToOneCourse from '../popup/batchOneToOneCourse.vue'
import copyArrageCourse from '../popup/copyArrageCourse.vue'
import arrangeSubscribeRecord from '../popup/arrangeSubscribeRecord.vue'
import cancelCourseForm from '../popup/cancelCourseForm.vue'
import editArrangeContent from '../popup/editArrangeContent.vue'
import printArrangeStudent from '../popup/printArrangeStudent.vue'
import qrSignCodePop from '../popup/qrSignCodePop.vue'
import useClipboard from 'vue-clipboard3';
import batchEditCourse from '../popup/batchEditCourse.vue'
import disCourseConfirm from '../popup/disCourseConfirm.vue'
import appointTeacherIntro from '../popup/appointTeacherIntro.vue'
import classinLiveLink from '../popup/classinLiveLink.vue'
import adjustCourse from '../popup/adjustCourse.vue'
import ObjectFreeTime from '../popup/objectFreeTime.vue'
import addScheduleForm from '../popup/addScheduleForm.vue'
import subscribeRuleDrawer from '../popup/subscribeRuleDrawer.vue'
import { transToConfigDescript } from '@/utils/filters/filters'
import batchArrangeInfo from '../popup/batchArrangeInfo.vue'
import { ArrowDown, Operation } from '@element-plus/icons-vue'

const event = useEvent()
const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global
const configs = computed(() => {
	return useConfigs().configs
})

const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})
const EnablePreSubscribeCourse=computed(()=>{ //是否启用约课管理中老师电脑端代预约功能（预约时支持选择意向学员且学员预约后需要老师手动生成排课），0否（默认），1是（子叶定制）。
	return configs.value.EnablePreSubscribeCourse==1
})
const IsOpenNewMallDecoration=computed(()=>{ //是否开启新的商城装修方案。 0否 ，1是。(默认)
	return configs.value.IsOpenNewMallDecoration==1
})
// 是否开启自动计费：0不开启（默认）；1只控制集体班手动点名时， 自动勾选计费和出勤（计费不可以改，出勤可以改）；2开启[昂立定制]，只要是集体班已排的课，都能够定时执行自动计费操作，且点名时自动勾选计费和出勤（计费不可以改，出勤可以改）；3开启[悦动部落]，请假通过的学员自动点名时不勾选计费，其他规则与2一致。4手动点名时补课和试听学员不默认计费，学员正常出勤、请假、临调、临加等情况默认计费，且有权限的老师允许修改计费状态[艾克瑞特]。5手动点名时请假、临加学员默认不出勤不计费，老师可以手动修改出勤，勾选出勤后默认计费，且计费不能修改；试听学员默认不出勤不计费，只能修改出勤状态，不能修改计费；正常在课学员默认出勤计费，允许修改出勤状态，不支持修改计费状态。
const ClassAutoCharge=computed(()=>{ 
	return configs.value.ClassAutoCharge
})
const EnableCourseApplication=computed(()=>{ //是否启用排课审批流程：0不启用（默认）；1启用，修改、删除排课都需要审批
	return configs.value.EnableCourseApplication==1
})
const EnablePrintOneToOneCourseRecord=computed(()=>{ //是否开启打印一对一课程上课凭证：0不开启（默认），1开启
	return configs.value.EnablePrintOneToOneCourseRecord==1
})
const IsOpenLiveStream=computed(()=>{ //开启直播教学服务。0:不开启（默认），1：声网。2：classin（自动同步方式）。3：classin(手动同步方式,通过zb.xiaogj.com进行同步)。
	return configs.value.IsOpenLiveStream
})
const IsOpenShiftForDay=computed(()=>{ // 0按月 1按天
	return configs.value.IsOpenShiftForDay==1
})
const EnableMonthShiftCourse=computed(()=>{ //按月计费课程是否支持手动排课出勤不计费：0否，1是（白塔岭定制）。
	return configs.value.EnableMonthShiftCourse==1
})
const EnableStore=computed(()=>{ //是否开启更多功能菜单,1:显示更多功能的菜单
	return configs.value.EnableStore==1
})
const EnableCourseOnline=computed(()=>{ //在线课配置项 是否开启在线课堂菜单，1：开通（开通后，才显示在线课堂菜单）
	return configs.value.EnableCourseOnline==1
})
const CheckStudentAttendanceConflict=computed(()=>{ //上课点名学生冲突时是否可以继续：0不能继续（默认），1可以继续
	return configs.value.CheckStudentAttendanceConflict==1
})
const ShowClassProgressBar=computed(()=>{//在课表上是否显示动态计算出来的上课进度（第几次课）：1显示，0不显示（默认）2小银星定制（在补课及请假管理界面，给学员安排跟班补课或请假时，如果开启了此配置，则可以显示目标补课班级和请假班级的上课进度。）
	return configs.value.ShowClassProgressBar==1||configs.value.ShowClassProgressBar==2
})


//权限
const NewCourse_ClassCourse = window.$xgj.op('NewCourse_ClassCourse') //给班级排课
const NewCourse_StudentCourse = window.$xgj.op('NewCourse_StudentCourse') //给学员排课
const NewCourse_SubscribeCourse = window.$xgj.op('NewCourse_SubscribeCourse') //排预约课
const NewCourse_CourseEdit = window.$xgj.op('NewCourse_CourseEdit') //编辑排课
const NewCourse_CourseDelete=window.$xgj.op("NewCourse_CourseDelete")	//删除排课
const NewCourse_CourseCancel=window.$xgj.op("NewCourse_CourseCancel") //取消排课记录
const NewCourse_CoursePlanViaCopy=window.$xgj.op("NewCourse_CoursePlanViaCopy")//复制/移动排课
const NewCourse_CourseStudentAdjustLesson=window.$xgj.op("NewCourse_CourseStudentAdjustLesson")//临时调课
const NewCourse_SubscribeCourseRuleQuery= window.$xgj.op("NewCourse_SubscribeCourseRuleQuery");// 查看约课规则
const NewCourse_SettingSubscribeCourseTeacherDesc= window.$xgj.op("NewCourse_SettingSubscribeCourseTeacherDesc");// 设置约课老师介绍
const NewCourse_CourseExport=window.$xgj.op('NewCourse_CourseExport') //导出排课
const CourseBeginPower= window.$xgj.op("NewCourse_CourseBegin") || window.$xgj.op("NewCourse_CourseBeginLimit");// 点名上课
const NewCourse_CourseBeginCancel=window.$xgj.op("NewCourse_CourseBeginCancel")//撤销上课
const NewCourse_LookLiveStreamingLink=window.$xgj.op("NewCourse_LookLiveStreamingLink")//查看直播链接
const NewCourse_PrintCourseRecord=window.$xgj.op("NewCourse_PrintCourseRecord")//打印上课凭证

const allColumns=[{
	Key:'ClassName',
	Label:'上课班级/学员',
	Visible:1,
	ColumnKey:'ClassName,StudentName',
	Disabled:true
},{
	Key:'ShiftName',
	Label:'上课课程',
	Visible:1,
	ColumnKey:'ShiftName',
	Disabled:true
},{
	Key:'IsOneToOne',
	Label:'教学形式',
	Visible:1,
	ColumnKey:'IsOneToOne',
	Tips:'课程管理中的“课程类型”'
},{
	Key:'CampusName',
	Label:'上课校区',
	Visible:1,
	ColumnKey:'CampusName'
},{
	Key:'StartTime',
	Label:'上课时间',
	Visible:1,
	ColumnKey:'StartTime,EndTime'
},{
	Key:'Duration',
	Label:'上课时长',
	Visible:1,
	ColumnKey:'Duration'
},{
	Key:'Finished',
	Label:'上课状态',
	Visible:1,
	ColumnKey:'FinishedName'
},{
	Key:'ConfirmUserName',
	Label:'点名人',
	Visible:0,
	ColumnKey:'ConfirmUserName'
},{
	Key:'IsClassTeacher',
	Label:'点名人是否为老师',
	Visible:0,
	ColumnKey:'IsClassTeacher'
},{
	Key:'StudentAttendanceCount',
	Label:'实到/应到',
	Visible:1,
	ColumnKey:'StudentAttendanceCount,StudentCount'
},{
	Key:'CostCount',
	Label:'计费人数',
	Visible:0,
	ColumnKey:'CostCount'
},{
	Key:'CostNum',
	Label:'计费数量',
	Visible:0,
	ColumnKey:'CostNum',
	Tips:'取本节课每个学员扣费数量的合计'
},{
	Key:'ChapterTitle',
	Label:'章节内容',
	Visible:0,
	ColumnKey:'ChapterTitle'
},{
	Key:'CourseContent',
	Label:'上课内容',
	Visible:0,
	ColumnKey:'CourseContent'
},{
	Key:'CoursewareSettingList',
	Label:'课件',
	Visible:0,
	ColumnKey:'CoursewareSettingList'
},{
	Key:'ImgAddress',
	Label:'上课照片',
	Visible:0,
	ColumnKey:'ImgAddress'
},{
	Key:'SubjectName',
	Label:'上课科目',
	Visible:0,
	ColumnKey:'SubjectName',
	Tips:`全科课程的上课科目`
},{
	Key:'ClassPlanNum_ClassPlanCount',
	Label:'上课进度',
	Visible:0,
	ColumnKey:'ClassPlanNum_ClassPlanCount',
	Tips:``
},{
	Key:'CourseType',
	Label:'上课方式',
	Visible:1,
	ColumnKey:'CourseType'
},{
	Key:'ClassroomName',
	Label:'上课教室',
	Visible:1,
	ColumnKey:'ClassroomName'
},{
	Key:'TeacherName',
	Label:'任课老师',
	Visible:1,
	ColumnKey:'TeacherName'
},{
	Key:'TeacherPostionTypeName',
	Label:'任课老师类型',
	Visible:0,
	ColumnKey:'TeacherPostionTypeName'
},{
	Key:'TeacherPositionName',
	Label:'任课老师职位',
	Visible:0,
	ColumnKey:'TeacherPositionName'
},{
	Key:'AssistantTeacherName',
	Label:'助教',
	Visible:1,
	ColumnKey:'AssistantTeacherName'
},{
	Key:'MasterName',
	Label:'学管师',
	Visible:1,
	ColumnKey:'MasterName'
},{
	Key:'HeadMasterUserName',
	Label:'班主任',
	Visible:0,
	ColumnKey:'HeadMasterUserName'
},{
	Key:'ShiftTermName',
	Label:'课程期段',
	Visible:0,
	ColumnKey:'ShiftTermName'
},{
	Key:'ShiftGradeName',
	Label:'课程年级',
	Visible:0,
	ColumnKey:'ShiftGradeName'
},{
	Key:'ShiftSubjectName',
	Label:'课程科目',
	Visible:0,
	ColumnKey:'ShiftSubjectName'
},{
	Key:'ShiftCategoryName',
	Label:'课程类型',
	Visible:0,
	ColumnKey:'ShiftCategoryName'
},{
	Key:'ShiftClassTypeName',
	Label:'课程班型',
	Visible:0,
	ColumnKey:'ShiftClassTypeName'
},{
	Key:'IsSubscribeCourse',
	Label:'开放预约',
	Visible:1,
	ColumnKey:'IsSubscribeCourse',
	Tips:`已开放的排课，支持学员在手机端预约。`
},{
	Key:'SubscribeStudentCount',	
	Label:'已约',
	Visible:1,
	ColumnKey:'SubscribeStudentCount',
	Tips:`预约的人数`
},{
	Key:'MaxStudentCount',	
	Label:'在课/最大可约',
	Visible:1,
	ColumnKey:'MaxStudentCount',
	Tips:`<div>
		<div class="desc-txt">
			<span class="font-bold"
				>在课：</span
			>当前在排课中的人数（包含约课、临加、在班学员，不含停课学员）
		</div>
		<div class="desc-txt">
	<span class="font-bold"
				>最大可约：</span
			>1、预约课：排课时所填写的可约人数。2、班级开放预约：班级详情中的预招人数。
		</div>
	</div>`
},{
	Key:'StartStudentCount',
	Label:'开课人数',
	Visible:1,
	ColumnKey:'StartStudentCount',
	Tips:`1、只有预约课，才会有开课人数；<br/>2、已约人数达到“开课人数”后，状态变为“已开课”`
},{
	Key:'CourseStatus',
	Label:'开课状态',
	Visible:1,
	ColumnKey:'CourseStatus',
	Tips:transToConfigDescript(`1、预约课的“已约人数”需要达到“开课人数”，会转为“已开课”状态；<br/>2、若设置了自动取消排课，则“未开课”的排课会根据设置的时间，自动取消排课；`)
},{
	Key:'InternalRemark',
	Label:'对内备注',
	Visible:0,
	ColumnKey:'InternalRemark'
}
// ,{
// 	Key:'Describe',
// 	Label:'对外备注',
// 	Visible:0,
// 	ColumnKey:'Describe'
// }
,{
	Key:'CreateRule',
	Label:'创建规则',
	Visible:1,
	ColumnKey:'CreateRule'
},{
	Key:'CreateTime',
	Label:'创建时间',
	Visible:1,
	ColumnKey:'CreateTime'
}]
const userDefinedColumns = ref([] as any)
const definedColumns=computed(()=>{
	let columns:any=userDefinedColumns.value.filter((item:any)=>item.Visible&&!item.Disabled)
	if(!ShowClassProgressBar.value){
		columns=columns.filter((item:any)=>item.Key!='ClassPlanNum_ClassPlanCount')
	}
	return columns
})

const EMPTYGUID='00000000-0000-0000-0000-000000000000'

const loading = ref(false)
const isInitialLoad = ref(true) // 标记是否是首次加载

const list = ref([] as any)
const condition = reactive({} as any)
const totalInfo=ref({
	CancelCount:0,
	CostCount:0,
	CostNum:0,
	Duration:0,
	FinishCount:0,
	NoFinishCount:0,
	StudentAttendanceCount:0,
	StudentCount:0,
	SubscribeStudentCount:0,
	MaxStudentCount:0
})

const sort = reactive({
	sort: 'StartTime',
	desc: 0,
})

const page = ref({
	TotalCount: 0, //总条数
	PageSize: 10, //每页条数
	PageIndex: 1, //第几页
	PageCount: 1, //总页数
} as IPageModel)

onMounted(() => {
	// 通知父组件骨架屏可以隐藏
	emit('component-ready')
})

const selectable = (row: any) => row.IsApprovaling!=1&&row.IsApprovaling!=2
const multipleSelection = ref([] as any[]);
function handleSelectionChange(val: any) {
    multipleSelection.value = val;
}

interface SummaryMethodProps {
	columns: TableColumnCtx[]
	data: any[]
}
const getSummaries = (param: SummaryMethodProps) => {
	const { columns, data } = param
	const sums: (string | VNode)[] = []
	columns.forEach((column, index) => {
		if (index === 0) {
			sums[index] = ''
			return
		}
		if (index === 1) {
			sums[index] = '合计'
			return
		}
		if (column.property=='Duration'){
			sums[index]=formatDuration(totalInfo.value.Duration)
		}else if(column.property=='StudentAttendanceCount'){
			sums[index]=totalInfo.value.StudentAttendanceCount+'/'+totalInfo.value.StudentCount
		}else if(column.property=='CostCount'){
			sums[index]=totalInfo.value.CostCount+''
		}else if(column.property=='CostNum'){
			sums[index]=totalInfo.value.CostNum+''
		}else if(column.property=='SubscribeStudentCount') {
			sums[index] = totalInfo.value.SubscribeStudentCount+''
		}else if(column.property=='MaxStudentCount'){
			sums[index] = totalInfo.value.StudentCount+'/'+(totalInfo.value.MaxStudentCount||'-')
		} else {
			sums[index] = ''
		}
	})

	return sums
}

const addArrangeFormRef = ref<InstanceType<typeof addArrangeForm> | null>(null)

const chooseSingleCampusRef = ref()
function openAdd() {
	// const campusList=currentCampus.value.length>0?currentCampus.value.split(','):[]
	// if(campusList.length==1){
		dealWithAdd()
	// }else{
	// 	chooseSingleCampusRef.value?.open({
	// 		optionText:'新增排课'
	// 	}).then((res: any) => {
	// 		dealWithAdd()
	// 	})
	// }	
}

function dealWithAdd(){
    addArrangeFormRef.value?.open({}).then((result: any)=>{
        // 使用event.emit替代window.parent.postMessage反写Query筛选条件
        if (result) {
            console.log('发送事件 update-schedule-query:', result);
            // 给班级排课 - 填充班级名称
            if (result.arrangeType === 0 && result.ClassName) {
                event.emit('update-schedule-query', result.ClassName);
            } 
            // 给学员排课 - 填充学员名称
            else if (result.arrangeType === 1 && result.StudentList && result.StudentList.length > 0) {
                event.emit('update-schedule-query', result.StudentName);
            }
            // 排预约课 - 填充课程名称
            else if (result.arrangeType === 2 && result.ShiftName) {
                event.emit('update-schedule-query', result.ShiftName);
            }
        }
        
        nextTick(() => {
            getFirstPage()
            getTotal()
        })
    })
}

const arrangeSubscribeRecordRef=ref<InstanceType<typeof arrangeSubscribeRecord> | null>(null)
function openSubscribeRecord(){
	arrangeSubscribeRecordRef.value?.open({
		
	}).then(()=>{
		getFirstPage()
	})
}

function sortChange(data: any) {
	if (data.order == null) {
		sort.sort = 'StartTime'
		sort.desc = 0
	} else {
		sort.sort = data.prop
		sort.desc = data.order == 'descending' ? 1 : 0
	}
	getFirstPage()
}

function showWareDetail(item: any, fileId: string) {
	if (!window.$xgj.op('NewCourse_CourseWareQuery')) {
		loading.value = true
		chekSeeCourseware({
			terminal: 0,
			shiftId: item.ShiftID,
			courseId: item.ID,
		})
			.then((res: any) => {
				if (res.Data == 1) {
					queryWareDetail(fileId)
				} else {
					ElMessage.warning('无查看权限')
				}
			})
			.finally(() => {
				loading.value = false
			})
	} else {
		queryWareDetail(fileId)
	}
}
function queryWareDetail(fileId: string) {
	getFilesUrl({ pagetype: 1 }).then((res: any) => {
		var previewUrl = res.Data + '?preview=true&id=' + fileId
		window.open(previewUrl)
	})
}

const setTableColumnsRef = ref<InstanceType<typeof setTableColumns> | null>(null)
function setTableColumnsOpen(){
	let columns:any=cloneDeep(userDefinedColumns.value)
	if(!ShowClassProgressBar.value){
		columns=columns.filter((item:any)=>item.Key!='ClassPlanNum_ClassPlanCount')
	}
	setTableColumnsRef.value?.open({
		columns:columns,
		moduleId:'scheduleManage_table'
	}).then((res:any)=>{
		userDefinedColumns.value=res.data?cloneDeep(res.data):[]
		funcQuery()
		getTotal()
	})
}

const qrCodePopRef=ref<InstanceType<typeof qrCodePop> | null>(null)

function openAppointQrcode(){
	const campusList=currentCampus.value.length>0?currentCampus.value.split(','):[]
	if(campusList.length==1){
		let campusId=campusList[0]
		toQrcode(campusId)
	}else{
		chooseSingleCampusRef.value?.open({
			optionTextFull:transToConfigDescript('请选择1个校区来生成约课二维码。')
		}).then((res: any) => {
			let campusId=res
			toQrcode(campusId)
		})
	}
	function toQrcode(campusId:string){
		nextTick(()=>{
			qrCodePopRef.value?.open({
				campusId:campusId
			}).then(()=>{})
		})
	}
}

// 约课规则
const subscribeRuleDrawerRef = ref<InstanceType<typeof subscribeRuleDrawer> | null>(null)
function openAppointRule(){
	subscribeRuleDrawerRef.value?.open()
}

// 约课老师介绍
const appointTeacherIntroRef=ref<InstanceType<typeof appointTeacherIntro> | null>(null)
function openAppointTeacher(){
	appointTeacherIntroRef.value?.open()
}

const batchOneToOneCourseRef=ref<InstanceType<typeof batchOneToOneCourse> | null>(null)
function openBatchCheckin(){
	if(multipleSelection.value.length==0){
		ElMessage.warning('请选择要批量点名的排课记录')
		return
	}
	if(multipleSelection.value.length>100){
		ElMessage.warning('最多支持100条排课记录批量点名')
		return
	}
	//仅支持对单个校区未结业的一对一排课操作批量点名上课
	let campusIdList:string[]=[],
		notOneToOneCount=0,
		classFinishCount=0,
		courseStatusZeroCount=0,
		finishedList:any=[],
		cancelList:any=[],
		newList:any=[],
		emptyTeacherList:any=[]
	multipleSelection.value.forEach((item:any)=>{
		if(campusIdList.indexOf(item.CampusID)==-1){
			campusIdList.push(item.CampusID)
		}
		if(item.IsOneToOne!=1){
			notOneToOneCount++
		}else if(item.IsClassFinished!=0){
			classFinishCount++
		}else if(item.Finished==1){//已点名
			finishedList.push(item)
		}else if(item.Finished==2){//已取消
			cancelList.push(item)
		}else if(!item.TeacherID||item.TeacherID==EMPTYGUID){
			emptyTeacherList.push(item)
		}else{
			newList.push(item)
		}
	})
	if(campusIdList.length>1||notOneToOneCount>0||classFinishCount>0||courseStatusZeroCount>0){
		ElMessage.warning(transToConfigDescript('仅支持对单个校区已开课未结业的一对一排课操作批量点名上课'))
		return
	}
	let _batchParams = {
		list: newList,
		finishedList: finishedList,
		cancelList: cancelList,
		emptyTeacherList: emptyTeacherList,
	}
	// loading.value=true
	checkStudentAvailabilityStatus({
		CourseIDList:newList.map((item:any)=>item.ID)
	}).then((res:any)=>{
		if(res.Data&&res.Data.length>0){
			let tips=`共${newList.length}节排课，以下<span class="color-[#F56C6C]">${res.Data.length}</span>节课，有学生排课冲突<br/>`
				tips+='<div class="wtwo-check-tips-box mt-[10px]">'
			res.Data.forEach((item:any)=>{
				tips+=`<div class="wtwo-check-tips-item">
					<div class="w-[160px] ellipsis-single" title="${item.ShiftName}">${item.ShiftName}</div>
					<div class="w-[160px] ellipsis-single" title="${item.ClassName}">${item.ClassName}</div>
					<div class="w-[180px]"">${dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )} - ${dayjs(item.EndTime).format("HH:mm" )}</div>
				</div>`
			})
			tips+='</div>'
			if(CheckStudentAttendanceConflict.value){
				ElMessageBox.confirm(tips, '提示', {
					confirmButtonText: newList.length>res.Data.length?'仅点名无冲突排课':'返回',
					cancelButtonText:`继续点名`,
					customStyle:{
						'max-width':'550px'
					},
					dangerouslyUseHTMLString:true,
					distinguishCancelAndClose:true
				}).then((res1:any)=>{
					if(newList.length>res.Data.length){
						_batchParams.list=newList.filter((item:any)=>res.Data.map((item:any)=>item.CourseID).indexOf(item.ID)==-1)
						doNext()
					}
				}).catch((action:any)=>{
					if(action=='cancel'){
						doNext()
					}
				})
			}else{
				ElMessageBox[newList.length>res.Data.length?'confirm':'alert'](tips, '提示', {
					confirmButtonText: '返回',
					cancelButtonText:`继续点名无冲突排课`,
					customStyle:{
						'max-width':'550px'
					},
					dangerouslyUseHTMLString:true,
					distinguishCancelAndClose:true
				}).then((res1:any)=>{
					
				}).catch((action:any)=>{
					if(action=='cancel'){
						_batchParams.list=newList.filter((item:any)=>res.Data.map((item:any)=>item.CourseID).indexOf(item.ID)==-1)
						doNext()
					}
				})
			}
		}else{
			doNext()	
		}
		function doNext(){
			batchOneToOneCourseRef.value?.open(_batchParams).then(res=>{
				getTotal()
				funcQuery()
			})
		}
	}).finally(()=>{
		// loading.value=false
	})
}

const workFlowPopupRef=ref<InstanceType<typeof workFlowPopup> | null>(null)
function removeOrCancelBatch(flag:number){//0:删除,1:取消
	const campusList=currentCampus.value.length>0?currentCampus.value.split(','):[]
	if(EnableCourseApplication.value&&campusList.length!=1){
		chooseSingleCampusRef.value?.open({
			optionText:'批量'+(flag==0?'删除':'取消')+'排课'
		}).then((res: any) => {})
		return
	}
	if(multipleSelection.value.length==0){
		ElMessage.warning('请选择要批量操作的排课')
		return
	}
	let notOperateArr:any=[],//不能操作的排课记录提示信息
		operateArr:any=[]//可以操作的排课记录提示信息
	multipleSelection.value.forEach((item:any)=>{
		// 已上课的记录不能删除   || 已上课或已取消排课的不能取消
		if((flag==0&&item.Finished==1)||(flag==1&&(item.Finished==1||item.Finished==2))){
				notOperateArr.push( item );
		}else{
			operateArr.push( item );
		}
	})
	if( notOperateArr.length>0 ){
		let tips=`共${multipleSelection.value.length}节排课，以下<span class="color-[#F56C6C]">${notOperateArr.length}</span>节是${transToConfigDescript(flag==0?'“已上课”状态，不能删除':'“已上课、已取消”状态，不能取消')}<br/>`
			tips+='<div class="wtwo-check-tips-box mt-[10px]">'
		notOperateArr.forEach((item:any)=>{
			tips+=`<div class="wtwo-check-tips-item">
				<div class="w-[160px] ellipsis-single" title="${item.ShiftName}">${item.ShiftName}</div>
				<div class="w-[160px] ellipsis-single" title="${item.ClassName}">${item.ClassName}</div>
				<div class="w-[180px]"">${dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )} - ${dayjs(item.EndTime).format("HH:mm" )}</div>
			</div>`
		})
		tips+='</div>'
		
		ElMessageBox[operateArr.length?'confirm':'alert'](tips, '提示', {
			confirmButtonText: '返回',
			cancelButtonText:`继续${flag==0?'删除':'取消'}剩下的排课`,
			customStyle:{
				'max-width':'550px'
			},
			dangerouslyUseHTMLString:true
		}).then((res:any)=>{
			
		}).catch(()=>{
			if(flag==0){
				doDelete(operateArr,getFirstPage)
			}else{
				doCancel(operateArr,getFirstPage)
			}
		})
	}else{
		if(flag==0){//删除排课
			ElMessageBox.confirm(`是否删除选中的${multipleSelection.value.length}条排课？`, '提示', {
				confirmButtonText: '是',
				cancelButtonText: '否'
			}).then((res:any)=>{
				doDelete(operateArr,getFirstPage)
			})
		}else{//取消排课
			doCancel(operateArr,getFirstPage)
		}
		
	}
	
}
function doDelete(operateArr:any,callback:Function){
	loading.value=true
	deleteCourseList({
		planID:EMPTYGUID,
		courseIDList:operateArr.map((item:any)=>item.ID)
	}).then((res:any)=>{
		if(res.Data.IsMatchWorkflow){
			workFlowPopupRef.value?.open({
				popTitle:'删除排课',
				api:deleteCourseAsk,
				data:{
					data:operateArr.map((item:any)=>item.ID).join(','),
					WorkflowId:res.Data.WorkflowId
				}
			}).then((res:any)=>{
				getTotal()
				callback&&callback()
			})
		}else{
			ElMessage.success('已删除')
			nextTick(()=>{
				getTotal()
				callback&&callback()
			})
		}
	}).finally(()=>{
		loading.value=false
	})
}
const cancelCourseFormRef=ref<InstanceType<typeof cancelCourseForm> | null>(null)
function doCancel(operateArr:any,callback:Function){
	cancelCourseFormRef.value?.open({
		popTitle:'取消排课',
		isBatch:true,
		isEdit:false,
		data:operateArr,
		gparams:{
			courseIDList:operateArr.map((item:any)=>item.ID)
		}
	}).then((res:any)=>{
		getTotal()
		callback&&callback()
	})
}
function delCourse(item:any){
	ElMessageBox.confirm(`警告！删除的排课不可恢复，请谨慎操作。`, '提示', {
		confirmButtonText: '确认删除',
		cancelButtonText: '取消'
	}).then((res:any)=>{
		doDelete([item],getFirstPage)
	})
}

function cancel(item:any){
	cancelCourseFormRef.value?.open({
		popTitle:'取消排课',
		isBatch:false,
		isEdit:false,
		data:item,
		gparams:{
			courseIDList:[item.ID]
		}
	}).then((res:any)=>{
		getTotal()
		funcQuery()
	})
}

const copyArrageCourseRef=ref<InstanceType<typeof copyArrageCourse> | null>(null)
function moveOrCopyCourse(type:number){
	const campusList=currentCampus.value.length>0?currentCampus.value.split(','):[]
	if(campusList.length==1){
		openCopyArrageCourse(type)
	}else{
		chooseSingleCampusRef.value?.open({
			optionText:`${type==1?'复制':'移动'}排课`
		}).then((res: any) => {
			openCopyArrageCourse(type)
		})
	}
	function openCopyArrageCourse(type:number){
		nextTick(()=>{
			copyArrageCourseRef.value?.open({
				CopyOrMove:type
			}).then((res:any)=>{
				getTotal()
				funcQuery()
			})
		})
	}
}

const batchEditCourseRef=ref<InstanceType<typeof batchEditCourse> | null>(null)
function openBatchEditCourse(){
	const campusList=currentCampus.value.length>0?currentCampus.value.split(','):[]
	if(EnableCourseApplication.value&&campusList.length!=1){
		chooseSingleCampusRef.value?.open({
			optionText:'批量修改'
		}).then((res: any) => {})
		return
	}
	if(multipleSelection.value.length==0){
		ElMessage.warning('请选择要批量修改的排课')
		return
	}
	let abledList=multipleSelection.value.filter((item:any)=>item.Finished==0)
	if(abledList.length==0){
		ElMessage.warning(transToConfigDescript('所选排课状态均为“已上课、已取消”，无法修改'))
		return
	}
	batchEditCourseRef.value?.open({
		selected:abledList
	}).then((res:any)=>{
		getTotal()
		funcQuery()
	})
}

const disCourseConfirmRef=ref<InstanceType<typeof disCourseConfirm> | null>(null)
//撤销上课
function disCourse(item:any){
	checkJingBeiFinanceLock({
		campusId:item.CampusID,
		date:dayjs(item.StartTime).format('YYYY-MM-DD')
	}).then((res:any)=>{
		if (res.Data == 1) {
			ElMessageBox.confirm(res.ErrorMsg, '提示', {
				confirmButtonText: '确认',
				cancelButtonText: '取消'
			}).then((res:any)=>{
				handleDis()
			})
        }else {
            handleDis();
        }
	})
	function handleDis(){
		let tipsStr = (item.CourseType == 2 && EnableStore.value&&EnableCourseOnline.value) ? `该线上课已经${transToConfigDescript('上课，撤销上课仅改变校管家系统内状态，无法撤销第三方记录，可能造成数据不吻合，请谨慎操作。')}` : transToConfigDescript('撤销上课后，该堂课将恢复成未上课的状态。确定撤销上课吗？')
		disCourseConfirmRef.value?.open({
			tips:tipsStr,
			data:item
		}).then((res:any)=>{
			funcQuery()
			getTotal()
		})
	}
}

const classinLiveLinkRef=ref<InstanceType<typeof classinLiveLink> | null>(null)
//查看直播链接
function openLiveLink(item:any){
	classinLiveLinkRef.value?.open({
		link:item.LiveStreamingLink,
		name:item.ClassName
	}).then((res:any)=>{
	})
}

const adjustCourseRef=ref<InstanceType<typeof adjustCourse> | null>(null)
//临时调课
function openAdjustCourse(item:any){
	adjustCourseRef.value?.open({
		data:item
	}).then((res:any)=>{
	})
}

//翻页查询
function handleSizeChange(val: number) {
	page.value.PageSize = val
	page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
	funcQuery()
}
function handleCurrentChange(val: number) {
	page.value.PageIndex = val
	funcQuery()
}
function getFirstPage() {
	page.value.PageIndex = 1
	funcQuery()
}

function funcQuery() {
	list.value = []
	let params: any = {}
	let columns=userDefinedColumns.value.filter((item:any)=>item.Visible)
	Object.assign(params, condition, sort, page.value,{
        ExportColumn:columns.map((item:any)=>item.ColumnKey).join(',').split(',')
    })
	
	loading.value = true
	queryCourseNew(params)
		.then((res: any) => {
			list.value = res.Data.List || []
			assignPage(page.value, res.Data)
		})
		.finally(() => {
			loading.value = false
			// 首次加载完成后，标记为非首次加载
			if (isInitialLoad.value) {
				isInitialLoad.value = false
			}
		})
}
function getTotal() {
	let params: any = {}

	Object.assign(params, condition)
	params.IsTotal = 1
	queryCourseTotal(params).then((res: any) => {
		let data=res.Data
		totalInfo.value.CancelCount=data.CancelCount
		totalInfo.value.CostCount=data.CostCount
		totalInfo.value.CostNum=data.CostNum
		totalInfo.value.Duration=data.Duration
		totalInfo.value.FinishCount=data.FinishCount
		totalInfo.value.NoFinishCount=data.NoFinishCount
		totalInfo.value.CancelCount=data.CancelCount
		totalInfo.value.StudentAttendanceCount=data.StudentAttendanceCount
		totalInfo.value.StudentCount=data.StudentCount
		totalInfo.value.MaxStudentCount=data.MaxStudentCount
		totalInfo.value.SubscribeStudentCount=data.SubscribeStudentCount
	})
}
// 搜索方法，供父组件调用
function search(searchCondition: any) {
	// 更新查询条件
	Object.assign(condition, searchCondition)
	if(userDefinedColumns.value.length>0){
		condition.ExportColumn=userDefinedColumns.value.map((item:any)=>item.ColumnKey).join(',').split(',')
		doSearch()
	}else{
		loading.value=true
		getUserColumns({
			moduleId: 'scheduleManage_table'
		}).then((res: any) => {
			let data=res.Data
			if(data.AllColumns){
				let obj=JSON.parse(data.AllColumns)
				// 使用列同步工具函数处理列的同步
				userDefinedColumns.value = syncUserColumns(obj, allColumns)
			}else{
				userDefinedColumns.value=cloneDeep(allColumns)
			}
			doSearch()
		}).catch(()=>{
			loading.value=false
		})
	}
}

function doSearch(){
	// 重新查询第一页
	getFirstPage()
	getTotal()
}

// 显示排课详情
const arrangeInfoRef = ref<InstanceType<typeof arrangeInfo> | null>(null)
function showShiftDetail(row: any,tab?:string) {
	if(row.IsApprovaling){
		return
	}
	if(row.Finished==2){
		cancelCourseFormRef.value?.open({
			popTitle:'提示',
			isBatch:false,
			isEdit:true,
			data:row,
			gparams:{
				courseIDList:[row.ID]
			}
		}).then((res:any)=>{
			funcQuery()
		})
	}else{
		arrangeInfoRef.value?.open({
			ID: row.ID,
			ShiftID: row.ShiftID,
			activeTab:tab
		})
	}
}

// 获取签到二维码
const qrSignCodePopRef=ref<InstanceType<typeof qrSignCodePop> | null>(null)
function getScanCode(row:any){
	qrSignCodePopRef.value?.open({
		ID:row.ID,
		ClassName:row.ClassName
	})
}

// 打印点名表
const printArrangeStudentRef=ref<InstanceType<typeof printArrangeStudent> | null>(null)
function printCourseStudentList(row?:any){
	if (row) {
		// 单个排课打印
		printArrangeStudentRef.value?.open({
			ids: row.ID
		})
	} else {
		// 批量打印
		if(multipleSelection.value.length == 0){
			ElMessage.warning('请选择要打印点名表的排课')
			return
		}
		
		// 过滤出可以打印的排课（排除已取消的）
		const printableItems = multipleSelection.value.filter((item: any) => item.Finished != 2)
		
		if(printableItems.length == 0){
			ElMessage.warning('选中的排课中没有可以打印点名表的记录（已取消的排课不能打印）')
			return
		}
		
		printArrangeStudentRef.value?.open({
			ids: printableItems.map((item: any) => item.ID).join(',')
		})
	}
}

// 复制主要信息
async function copyMainInfo(row:any) {
	try {
		const { toClipboard } = useClipboard();
		const data = row;
		
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

// 批量修改上课内容
function editBatchContent(){
    if(multipleSelection.value.length == 0){
        ElMessage.warning(transToConfigDescript('请选择要批量修改上课内容的排课'))
        return
    }
    
    const firstShiftID = multipleSelection.value[0].ShiftID
    const isSameCourse = multipleSelection.value.every((item: any) => item.ShiftID === firstShiftID)
    
    if (!isSameCourse) {
        ElMessage.warning(transToConfigDescript('请选择同一个课程下的排课记录'))
        return
    }
    
    editArrangeContentRef.value?.open({
        selected: multipleSelection.value,
        isBatch: true,
    }).then(() => {
        getTotal()
        funcQuery()
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
const editArrangeContentRef=ref<InstanceType<typeof editArrangeContent> | null>(null)
function editContent(row:any) {
	editArrangeContentRef.value?.open({
		selected: [row]
	}).then(()=>{
		// getTotal()
		funcQuery()
	})
}

// 编辑排课
const editArrangeFormRef = ref<InstanceType<typeof editArrangeForm> | null>(null)
function showShiftEdit(row:any){
	editArrangeFormRef.value?.open({
		CampusID: row.CampusID,
		ID: row.ID,
		ShiftID: row.ShiftID,
	}).then(() => {
		// getTotal()
		// funcQuery()
	})
}

event.on("arrange-table-list-refresh",(params:any)=>{
	if(params&&params.refreshTotal){
		getTotal()
	}
	funcQuery()
})

// 注意：request-edit-schedule 和 request-edit-arrange 事件已移至父组件 scheduleManage.vue 统一处理

const objectFreeTimeRef=ref<InstanceType<typeof ObjectFreeTime> | null>(null)
function openObjectFreeTime(){
	objectFreeTimeRef.value?.open({})
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
		window.microApp.dispatch({type:'scheduleManage:rollCall',courseData:info},(res:any)=>{
			if(res){
				res[0].then(()=>{
					getTotal()
					funcQuery()
				})
			}
		})
	}
}

function excelImportCourse(){
	if(window.microApp){
		window.microApp.dispatch({type:'scheduleManage:excelImportCourse',data:Math.random()},(res:any)=>{
			if(res){
				res[0].then(()=>{
					getTotal()
					funcQuery()
				})
			}
		})
	}
}

function showPlanCourse(row:any){
	if (window.microApp) {
		window.microApp.dispatch({type:'scheduleManage:showPlanCourse',data:Math.random()},(res:any)=>{
			// no callback
		})
	}
}

function exportData(){
	let params: any = {}
	let columns=userDefinedColumns.value.filter((item:any)=>item.Visible)
	Object.assign(params, condition, sort, page.value,{
        ExportColumn:columns.map((item:any)=>item.ColumnKey).join(',').split(','),
		download:1,
		PageSize:99999
    })
	queryCourseExport(params).then((res:any) => {
		exportFile(res, `排课列表_${dayjs(params.StartDate).format('MM月DD日')}到${dayjs(params.EndDate).format('MM月DD日')}`);
	})
}

function printOneToOne(row:any){
	if (window.microApp) {
		window.microApp.dispatch({type:'scheduleManage:printCourseCard',courseCardData:{courseid:row.ID,campusid:row.CampusID}})
	}
}

// 暴露方法给父组件
defineExpose({
	search,
})

</script>
<style lang="scss" scoped>
.wtwo-arrange-table-list {
	// flex: 1 1 auto;
	// display: flex;
	// flex-direction: column;
	// height: 100%;

	.skeleton-wrapper {
		padding: 20px;
		background: #fff;
		border-radius: 8px;
		min-height: 600px;
		
		.skeleton-toolbar {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 16px;
			padding: 0px 16px;
			background: linear-gradient(90deg, #f8f9fa 25%, #ebedef 50%, #f8f9fa 75%);
			background-size: 200% 100%;
			animation: shimmer 2.5s infinite linear;
			border-radius: 6px;
			height: 40px;
			
			.skeleton-toolbar-right {
				display: flex;
				align-items: center;
				gap: 8px;
			}
		}
		
		.skeleton-table {
			margin-bottom: 16px;
			border: 1px solid #e8e8e8;
			border-radius: 6px;
			overflow: hidden;
			
			.skeleton-table-row {
				display: flex;
				justify-content: space-between;
				align-items: center;
				padding: 12px 16px;
				gap: 10px;
				background: linear-gradient(90deg, #fcfcfd 25%, #f5f6f7 50%, #fcfcfd 75%);
				background-size: 200% 100%;
				animation: shimmer 2.5s infinite linear;
				border-bottom: 1px solid #eee;
				height: 40px;
				
				&:first-child {
					background: linear-gradient(90deg, #f5f6f7 25%, #e8eaec 50%, #f5f6f7 75%);
					background-size: 200% 100%;
					animation: shimmer 2.5s infinite linear;
					font-weight: 500;
					height: 40px;
				}
				
				&:last-child {
					border-bottom: none;
				}
			}
		}
		
		.skeleton-pagination {
			display: flex;
			justify-content: flex-end;
			align-items: center;
			padding: 12px 16px;
			background: linear-gradient(90deg, #f8f9fa 25%, #ebedef 50%, #f8f9fa 75%);
			background-size: 200% 100%;
			animation: shimmer 2.5s infinite linear;
			border-radius: 6px;
			height: 52px;
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

	.table-wrap {
		position: relative;
		isolation: isolate;
	}
}
</style>
<style lang="scss">
.arrange-brief-tooltip {
	width: 240px;
	line-height: 17px;
	.desc-title {
		position: relative;
		padding-left: 11px;
		margin-bottom: 8px;
		&::before {
			position: absolute;
			content: '';
			display: inline-block;
			width: 4px;
			height: 4px;
			border-radius: 50%;
			background: #2878e8;
			left: 0;
			top: 6px;
		}
	}
	.desc-txt {
		& + .desc-title {
			margin-top: 10px;
		}
		& + .desc-txt {
			margin-top: 5px;
		}
	}
}
</style>
