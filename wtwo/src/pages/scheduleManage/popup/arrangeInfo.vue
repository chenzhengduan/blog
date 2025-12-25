<template>
	<el-drawer
		v-model="drawer"
		title="详情"
		direction="rtl"
		size="1150px"
		class="arrangeInfo"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
		<div class="bg-[#F7F8FA] min-w-[1150px]" v-loading="arrangeDataLoading">
			<!-- 头部信息 -->
			<div class="p-16px" :class="arrangeData.Finished == 1 ? 'bg-gradient-to-r from-white to-[#E5E5E5]' : 'bg-gradient-to-r from-white to-[#DAE8FF]'">
				<div class="flex justify-between items-center mb-8px">
					<div class="text-18px font-bold color-[#303133]">{{ arrangeData.ClassName || '-' }}</div>
					<div>
						<el-dropdown trigger="hover">
							<el-button class="w-32px!"><el-icon><MoreFilled /></el-icon></el-button>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item @click.native="getScanCode" v-if="arrangeData.Finished === 0">
										<span>签到二维码</span>
									</el-dropdown-item>
									<el-dropdown-item @click.native="openAdjustCourse" :divided="arrangeData.Finished === 0" v-if="NewCourse_CourseStudentAdjustLesson&&arrangeData.Finished!=2">
										<span>临时调课</span>
									</el-dropdown-item>
									<el-dropdown-item v-if="EnablePrintOneToOneCourseRecord&&NewCourse_PrintCourseRecord
										&&arrangeData.Finished==1&&arrangeData.IsOneToOne==1" @click="printOneToOne(arrangeData)">{{transToConfigDescript('打印上课凭证')}}</el-dropdown-item>
									<el-dropdown-item @click.native="printCourseStudentList" v-if="arrangeData.Finished !== 2">
										<span>打印点名表</span>
									</el-dropdown-item>
									<el-dropdown-item @click.native="copyMainInfo">
										<span>复制主要信息</span>
									</el-dropdown-item>
									<el-dropdown-item @click.native="editContent" divided v-if="arrangeData.Finished != 2  && arrangeData.Unit != 3">
										<span>{{transToConfigDescript('修改上课内容')}}</span>
									</el-dropdown-item>
									<el-dropdown-item @click.native="cancelArrange" v-if="NewCourse_CourseCancel && arrangeData.Finished != 1 && arrangeData.Finished != 2">
										<span>取消排课</span>
									</el-dropdown-item>
									<el-dropdown-item @click.native="delArrange" v-if="NewCourse_CourseDelete && arrangeData.Finished !== 1">
										<span>删除排课</span>
									</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
						<el-button class="ml-12px" @click="openEditArrangeForm" v-if="NewCourse_CourseEdit&&arrangeData.Finished === 0">编辑</el-button>
						<el-button class="ml-12px" type="primary" @click="rollCall" v-if="arrangeData.Finished !== 2 && CourseBeginPower">{{transToConfigDescript('点名上课')}}</el-button>
					</div>
				</div>

				<div class="flex items-center gap-8px flex-wrap">
					<span class="text-14px color-[#666]">{{ arrangeData.CourseTimeWeek || '-' }}</span>
					<span class="text-14px color-[#666]">{{ arrangeData.Duration || '-' }}分钟</span>
					<div class="flex gap-8px">
						<el-tag effect="light" v-if="arrangeData.Finished !== undefined" :type="arrangeData.Finished === 0 ? 'primary' : arrangeData.Finished === 1 ? 'success' : 'info'">{{ arrangeData.FinishedName }}</el-tag>
					</div>
				</div>
			</div>
			<div class="flex justify-between bg-white text-12px color-[#909399] h-26px">
				<div class="flex items-center gap-16px ml-14px">
					<div v-if="arrangeData.ClassroomName">上课教室：{{ arrangeData.ClassroomName || '-' }}</div>
					<div v-if="teacherDisplayNames.main">{{transToConfigDescript('任课老师：')}}{{ teacherDisplayNames.main || '-' }}</div>
				</div>
				<div class="flex items-center">
					<span v-if="arrangeData.CreateRuleText" class="mr-14px">{{ arrangeData.CreateRuleText }}</span>
					<el-button 
						v-if="arrangeData.PlanID && arrangeData.PlanID !== globalData.EMPTYGUID" 
						type="primary" 
						link 
						size="small" 
						class="mr-14px" 
						@click="openBatchScheduleDialog"
					>
						管理本批次排课 <el-icon><ArrowRight /></el-icon>
					</el-button>
				</div>
			</div>
			
			<!-- 详细信息动态布局 -->
			<div class="mx-16px mt-12px px-20px pt-16px rounded-8px bg-white">
				<div class="info-grid">
					<div class="info-item">
						<label class="info-label">{{transToConfigDescript('上课课程')}}</label>
						<span class="info-value">{{ arrangeData.ShiftName || '-' }}</span>
					</div>
					<div class="info-item">
						<label class="info-label">{{transToConfigDescript('上课学员/班级')}}</label>
						<span class="info-value">{{ arrangeData.ClassName || '-' }}</span>
					</div>
					<div class="info-item">
						<label class="info-label">教学形式</label>
						<span class="info-value">{{ arrangeData.IsOneToOneName || '-' }}</span>
					</div>
					<div class="info-item" v-show="showCollapseSections">
						<label class="info-label">{{transToConfigDescript('上课校区')}}</label>
						<span class="info-value">{{ arrangeData.CampusName || '-' }}</span>
					</div>
					<div class="info-item" v-show="showCollapseSections">
						<label class="info-label">{{transToConfigDescript('上课时间')}}</label>
						<span class="info-value">{{ arrangeData.CourseTimeWeek || '-' }}</span>
					</div>
					<div class="info-item" v-show="showCollapseSections">
						<label class="info-label">{{transToConfigDescript('上课时长')}}</label>
						<span class="info-value">{{ arrangeData.Duration || '-' }}分钟</span>
					</div>
					<div class="info-item" v-show="showCollapseSections">
						<label class="info-label">{{transToConfigDescript('上课状态')}}</label>
						<span class="info-value">
							<el-tag v-if="arrangeData.Finished !== undefined" :type="arrangeData.Finished === 0 ? 'primary' : arrangeData.Finished === 1 ? 'success' : 'info'">{{ arrangeData.FinishedName }}</el-tag>
						</span>
					</div>
				</div>
				<div class="flex justify-center py-10px" v-if="!showCollapseSections">
					<el-button link size="small" class="w-[128px] !h-[22px] flex items-center !color-[#2878E8] text-12px hover:!bg-[#EAF3FF]" @click="toggleAllCollapse">
						<span>展开全部</span>
						<el-icon class="ml-4px">
							<ArrowDown />
						</el-icon>
					</el-button>
				</div>
			</div>
			
			<!-- 可折叠区块容器 -->
			<transition name="collapse-fade" mode="out-in">
				<div v-if="showCollapseSections">
					<!-- 点名与出勤 -->
					<el-collapse v-model="activeNames" class="info-collapse" expand-icon-position="left">
						<el-collapse-item name="attendance" :icon="CaretRight">
							<template #title>
								<span>点名与出勤</span>
							</template>
							<div class="info-grid">
								<div class="info-item">
									<label class="info-label">点名人</label>
									<span class="info-value">{{ arrangeData.ConfirmUserName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('点名人是否为老师')}}</label>
									<span class="info-value">{{ arrangeData.IsClassTeacher || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">实到/应到</label>
									<span class="info-value">{{ arrangeData.StudentAttendanceCount || '-' }}/{{ arrangeData.StudentCount || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">计费人数</label>
									<span class="info-value">{{ arrangeData.CostCount || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label flex-center">计费数量<el-tooltip
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
										<template #content>取本节课每个学员扣费数量的合计</template>
									</el-tooltip></label>
									<span class="info-value">
										{{ arrangeData.CostNum || '-' }}
									</span>
								</div>
							</div>
						</el-collapse-item>

						<el-collapse-item name="content" :title="transToConfigDescript('上课内容')" :icon="CaretRight">
							<div class="info-grid">
								<div class="info-item">
									<label class="info-label">章节内容</label>
									<span class="info-value">
										<el-tooltip
											class="box-item"
											effect="dark"
											:content="arrangeData.ChapterTitle || '-'"
											placement="top-start"
											popper-class="custom-tooltip"
										>
											{{ arrangeData.ChapterTitle || '-' }}
										</el-tooltip>
									</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('上课内容')}}</label>
									<span class="info-value">
										<el-tooltip
											class="box-item"
											effect="dark"
											:content="arrangeData.CourseContent || '-'"
											placement="top-start"
											popper-class="custom-tooltip"
										>
											{{ arrangeData.CourseContent || '-' }}
										</el-tooltip>
									</span>
								</div>
								<div class="info-item">
									<label class="info-label">课件</label>
									<span class="info-value">
										<div v-if="arrangeData.CoursewareSettingList && arrangeData.CoursewareSettingList.length > 0" class="flex items-center gap-8px w-100%">
											<!-- 课件内容展示 -->
											<div class="flex items-center gap-8px flex-1 min-w-0 max-w-100%">
												<!-- <span class="flex-0-1-auto overflow-hidden text-ellipsis whitespace-nowrap color-[#333] block">{{ (arrangeData.CoursewareSettingList || []).map(item => item.FileName).join('、') }}</span> -->
												
												<!-- Popover 展示所有课件 -->
												<el-popover
													v-model:visible="coursewarePopoverVisible"
													placement="bottom-start"
													:width="350"
												>
													<template #reference>
														<span class="flex-0-1-auto overflow-hidden text-ellipsis whitespace-nowrap color-[#333] cursor-pointer">{{ (arrangeData.CoursewareSettingList || []).map(item => item.FileName).join('、') }}</span>
														<!-- <span class="flex-shrink-0 color-[#409eff] whitespace-nowrap cursor-pointer transition-color duration-300 ease-in-out hover:color-[#66b1ff]">共{{ (arrangeData.CoursewareSettingList || []).length }}个课件</span> -->
													</template>
													<div class="max-h-300px overflow-y-auto">
														<div 
															v-for="(item, index) in (arrangeData.CoursewareSettingList || [])" 
															:key="index"
															class="text-#409eff py-3px cursor-pointer overflow-hidden text-ellipsis whitespace-nowrap"
															@click="showWareDetail(item, item.FileID || '')"
														>
															{{ item.FileName }}
														</div>
													</div>
												</el-popover>
											</div>
										</div>
										<span v-else>-</span>
									</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('上课照片')}}</label>
									<span class="info-value">
										<div v-if="arrangeData.CourseImgList && arrangeData.CourseImgList.length > 0">
											<div class="photo-container">
												<div 
													v-for="(photo, index) in arrangeData.CourseImgList.slice(0, MaxCourseImgCount)" 
													:key="index"
													class="photo-item"
													:class="{ 'has-overlay': index === MaxCourseImgCount-1 && arrangeData.CourseImgList.length > MaxCourseImgCount }"
												>
													<el-image 
														:src="photo" 
														fit="cover"
														class="photo-thumbnail"
													/>
													<div v-if="index === MaxCourseImgCount-1 && arrangeData.CourseImgList.length > MaxCourseImgCount" class="photo-overlay">
														<span class="overlay-text">+{{ arrangeData.CourseImgList.length - MaxCourseImgCount }}</span>
													</div>
												</div>
											</div>
										</div>
										<span v-else>-</span>
									</span>
								</div>
								<div class="info-item">
									<label class="info-label flex-center">{{transToConfigDescript('上课科目')}}
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
									</label>
									<span class="info-value">
										{{ arrangeData.SubjectName || '-' }}
									</span>
								</div>
							</div>
						</el-collapse-item>

						<el-collapse-item name="teacher" :title="transToConfigDescript('教室与老师')" :icon="CaretRight">
							<div class="info-grid">
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('上课方式')}}</label>
									<span class="info-value">{{ arrangeData.CourseTypeName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('上课教室')}}</label>
									<span class="info-value">{{ arrangeData.ClassroomName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('任课老师')}}</label>
									<span class="info-value">{{ teacherDisplayNames.main || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('任课老师类型')}}</label>
									<span class="info-value">{{ teacherDisplayNames.teacherPostionTypeName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">助教</label>
									<span class="info-value">{{ teacherDisplayNames.assistant || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('学管师')}}</label>
									<span class="info-value">{{ arrangeData.MasterNameList?.join(', ') || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('班主任')}}</label>
									<span class="info-value">{{ arrangeData.HeadMasterUserName || '-' }}</span>
								</div>
								<!-- 小银星定制：增加了几种老师角色 替课老师 跟课老师 钢伴老师 坐班老师 课程顾问-->
								<template v-if="EnableCourseRole && arrangeData.IsOneToOne==0">
									<div class="info-item">
										<label class="info-label">替课老师</label>
										<span class="info-value">{{ teacherDisplayNames.replace }}</span>
									</div>
									<div class="info-item">
										<label class="info-label">跟课老师</label>
										<span class="info-value">{{ teacherDisplayNames.follow }}</span>
									</div>
									<div class="info-item">
										<label class="info-label">钢伴老师</label>
										<span class="info-value">{{ teacherDisplayNames.piano }}</span>
									</div>
									<div class="info-item">
										<label class="info-label">坐班老师</label>
										<span class="info-value">{{ teacherDisplayNames.office }}</span>
									</div>
									<div class="info-item">
										<label class="info-label">课程顾问</label>
										<span class="info-value">{{ teacherDisplayNames.consultant }}</span>
									</div>
								</template>
							</div>
						</el-collapse-item>

						<el-collapse-item name="course" :title="transToConfigDescript('课程信息')" :icon="CaretRight">
							<div class="info-grid">
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('课程期段')}}</label>
									<span class="info-value">{{ arrangeData.ShiftTermName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('课程年级')}}</label>
									<span class="info-value">{{ arrangeData.ShiftGradeName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('课程科目')}}</label>
									<span class="info-value">{{ arrangeData.ShiftSubjectName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('课程类型')}}</label>
									<span class="info-value">{{ arrangeData.ShiftCategoryName || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">{{transToConfigDescript('课程班型')}}</label>
									<span class="info-value">{{ arrangeData.ShiftClassTypeName || '-' }}</span>
								</div>
							</div>
						</el-collapse-item>
						<!-- 约课信息：按班级排课 = 10，按学员排课 = 20，预约排课 = 30，日程 = 40 -->
						<el-collapse-item name="appointment" title="约课信息" :icon="CaretRight" v-if="arrangeData.CourseMethod == 10 || arrangeData.CourseMethod == 30">
							<div class="info-grid">
								<div class="info-item">
									<label class="info-label">开放预约</label>
									<span class="info-value">{{ arrangeData.IsSubscribeCourse === 1 ? '是' : '否' }}</span>
								</div>
								
								<div class="info-item" v-if="(arrangeData.CourseMethod == 30||(arrangeData.CourseMethod == 10&&(arrangeData.IsSubscribeCourse == 1 || (arrangeData.SubscribedCount || 0)> 0)))">
									<label class="info-label flex-center">已约
										<el-tooltip
											effect="dark"
											placement="top"
										>
											<el-icon
												size="16px"
												class="ml-4px cursor-pointer"
												color="#909399"
											>
												<svg aria-hidden="true">
													<use
														xlink:href="#w2-xinxitishi"
													></use>
												</svg>
											</el-icon>
											<template #content>预约的人数</template>
										</el-tooltip>
									</label>
									<span class="info-value">{{ arrangeData.SubscribedCount }}</span>
								</div>
								<div class="info-item" v-if="(arrangeData.CourseMethod == 30||(arrangeData.CourseMethod == 10&&arrangeData.IsSubscribeCourse == 1))">
									<label class="info-label flex-center">在课/最大可约
										<el-tooltip
											effect="dark"
											placement="top"
										>
											<el-icon
												size="16px"
												class="ml-4px cursor-pointer"
												color="#909399"
											>
												<svg aria-hidden="true">
													<use
														xlink:href="#w2-xinxitishi"
													></use>
												</svg>
											</el-icon>
											<template #content>
												在课：当前在排课中的人数（包含约课、临加、在班学员，不含停课学员）<br>
												{{transToConfigDescript('最大可约：1、预约课：排课时所填写的可约人数。2、班级开放预约：班级详情中的预招人数。')}}
											</template>
										</el-tooltip>
									</label>
									<span class="info-value">{{ arrangeData.StudentCount }}/{{ arrangeData.CourseMethod == 10?(arrangeData.MaxStudentsAmount || '-'):(arrangeData.MaxStudentCount || '-') }}</span>
								</div>
								<template v-if="arrangeData.CourseMethod == 30">
									<div class="info-item" v-if="(arrangeData.IsSubscribeCourse == 1 || (arrangeData.SubscribedCount || 0) > 0)">
										<label class="info-label">开课人数</label>
										<span class="info-value">{{ arrangeData.StartStudentCount || '-' }}</span>
									</div>
									<div class="info-item" v-if="(arrangeData.IsSubscribeCourse == 1 || (arrangeData.SubscribedCount || 0) > 0)">
										<label class="info-label">开课状态</label>
										<span class="info-value">{{ arrangeData.CourseMethod==30?(arrangeData.CourseStatus==1?'已开课':'未开课'):'-' }}</span>
									</div>
								</template>
							</div>
						</el-collapse-item>

						<el-collapse-item name="other" title="其他信息" :icon="CaretRight">
							<div class="info-grid">
								<div class="info-item">
									<label class="info-label">对内备注</label>
									<span class="info-value">
										<el-tooltip
											class="box-item"
											effect="dark"
											:content="arrangeData.InternalRemark || '-'"
											placement="top-start"
											popper-class="custom-tooltip"
										>
											{{ arrangeData.InternalRemark || '-' }}
										</el-tooltip>
									</span>
								</div>
								<!-- <div class="info-item">
									<label class="info-label">对外备注</label>
									<span class="info-value">
										<el-tooltip
											class="box-item"
											effect="dark"
											:content="arrangeData.ExternalRemark || '-'"
											placement="top-start"
											popper-class="custom-tooltip"
										>
											{{ arrangeData.ExternalRemark || '-' }}
										</el-tooltip>
									</span>
								</div> -->
								<div class="info-item">
									<label class="info-label">创建规则</label>
									<span class="info-value">{{ arrangeData.CreateRule || '-' }}</span>
								</div>
								<div class="info-item">
									<label class="info-label">创建时间</label>
									<span class="info-value">{{ arrangeData.CreateTime ? dayjs(arrangeData.CreateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</span>
								</div>
							</div>
						</el-collapse-item>
					</el-collapse>
				</div>
			</transition>
			
			<!-- 展开收起控制按钮 -->
			<div class="flex justify-center my-10px" v-if="showCollapseSections">
				<el-button link size="small" class="w-[128px] !h-[22px] flex items-center !color-[#2878E8] text-12px hover:!bg-[#EAF3FF]" @click="toggleAllCollapse">
					<span>收起信息</span>
					<el-icon class="ml-4px">
						<ArrowUp />
					</el-icon>
				</el-button>
			</div>

			<!-- 学员管理区域 -->
			<div class="student-management-section bg-#fff px-16px" :class="{ 'mt-12px': !showCollapseSections }">
				<!-- 选项卡 -->
				<el-tabs v-model="activeTab" class="mb-20px">
					<el-tab-pane name="students">
						<template #label>
							<span>{{transToConfigDescript('上课学员')}}</span>
						</template>
						
						<!-- 上课学员内容 -->
						<div v-loading="arrangeTabLoading">
							<!-- 搜索和操作区域 -->
							<div class="flex justify-between items-center my-16px my-12px">
                                <div class="p-0">
									<el-input
										v-model="searchKeyword"
										placeholder="学员姓名"
										class="!w-240px"
										clearable
										@keyup.enter="handleSearch"
									>
										<template #append>
											<el-button type="primary" @click="handleSearch">搜索</el-button>
										</template>
									</el-input>
								</div>
								<el-button @click="handleAddStudent" v-if="NewCourse_CourseStudentEdit && arrangeData.ID && (arrangeData.Unit != 3 || IsCPD)">
									添加学员
								</el-button>
							</div>
							<!-- 学员表格 -->
							<div class="table-container mb-20px">
								<el-table 
									:data="arrangeData.CourseStudentList || []" 
									class="w-100%"
									border
									show-summary
									:summary-method="getSummaries"
								>
									<template #empty>
										<el-empty
											:image="globalData.emptyPng"
											description="暂无数据"
											:image-size="100"
										></el-empty>
									</template>
									<el-table-column prop="StudentName" label="姓名" width="210">
										<template #default="{ row }">
											<div class="student-name">
												<div class="avatar">
													<div class="avatar" v-if="row.StudentPhotoPath">
														<img :src="row.StudentPhotoPath" alt="" />
													</div>
													<div class="avatar placeholder" v-else>
														{{ getNameInitial(row.StudentName) }}
													</div>
												</div>
												<div class="student-info">
													<span v-if="row.highlightedName" v-html="row.highlightedName"></span>
													<span v-else>{{ row.StudentName }}</span>
													<span v-if="row.IsMend == 1">_补课</span>
													<span v-if="(row.AdjustFlag == 2) || (row.AdjustFlag == 3 && row.IsSubscribecourse == 0)" 
														  :class="['stamp-default', { 'stamp-default': row.AdjustFlag == 3 }]">
														{{ row.AdjustFlag == 2 ? "临调" : "临加" }}
													</span>
													<span v-if="row.IsAttendStauts == 1" class="stamp-default">请假</span>
													<span v-if="row.IsSubscribecourse == 1" class="stamp-default">约课</span>
													<span v-if="row.IsTry == 1 && EnableNewTryCourse" class="stamp-default">试听</span>
													<span v-if="row.StudentStatus == -1" class="stamp-default">[已删除]</span>
												</div>
											</div>
										</template>
									</el-table-column>
									<el-table-column prop="StudentSmsTel" label="联系电话" width="150" />
									<el-table-column label="出勤" width="100">
										<template #default="{ row }">
											<el-icon v-if="row.IsAttend" color="#00B42A" size="16">
												<Check />
											</el-icon>
											<span v-else>-</span>
										</template>
									</el-table-column>
									<el-table-column label="计费 (次)" width="120">
										<template #default="{ row }">
											<el-icon v-if="row.IsCost" color="#00B42A" size="16">
												<Check />
											</el-icon>
											<span v-else>-</span>
										</template>
									</el-table-column>
									<el-table-column label="试听" width="100" v-if="arrangeData.Unit != 3 && !EnableNewTryCourse">
										<template #default="{ row }">
											<el-icon v-if="row.IsTry" color="#00B42A" size="16">
												<Check />
											</el-icon>
											<span v-else>-</span>
										</template>
									</el-table-column>
									<el-table-column prop="AbsentReason" label="缺勤原因" width="120" show-overflow-tooltip>
										<template #default="{ row }">
											<el-tooltip
												class="box-item"
												effect="dark"
												:content="row.AbsentReason || '-'"
												placement="top-start"
												popper-class="custom-tooltip"
											>
												{{ row.AbsentReason || '-' }}
											</el-tooltip>
										</template>
									</el-table-column>
									<el-table-column prop="Remark" label="备注" min-width="120" show-overflow-tooltip>
										<template #default="{ row }">
											<div class="remark-cell" @mouseenter="handleRemarkHover(row)" @mouseleave="handleRemarkLeave(row)">
												<div class="remark-text">{{ row.Remark || '-' }}</div>
												<el-popover
													v-if="row.CourseStudentID&&row.CourseStudentID!=globalData.EMPTYGUID"
													v-model:visible="row.showPopover"
													placement="bottom-start"
													:width="320"
													trigger="click"
													@show="handlePopoverShow(row)"
													@hide="handlePopoverHide(row)"
												>
													<template #reference>
														<el-icon class="edit-icon cursor-pointer" :class="{ 'visible': row.showEditIcon }" size="16"><Edit /></el-icon>
													</template>
													<div v-loading="row.isSaving">
														<div class="mb-10px">编辑备注</div>
														<el-input 
															v-model="row.editRemarkText" 
															placeholder="请输入备注"
															type="textarea"
															:rows="3"
															class="w-100%!"
															maxlength="200"
															show-word-limit
														/>
														<div class="flex justify-end mt-16px">
															<el-button @click="cancelRemarkEdit(row)" :disabled="row.isSaving">取消</el-button>
															<el-button type="primary" @click="saveRemarkEdit(row)" :loading="row.isSaving" :disabled="row.isSaving">
																{{ row.isSaving ? '保存中...' : '保存' }}
															</el-button>
														</div>
													</div>
												</el-popover>
											</div>
										</template>
									</el-table-column>
									<el-table-column v-if="NewCourse_CourseStudentRemove" label="操作" width="100" fixed="right" align="center">
										<template #default="{ row }">
											<el-button class="!p-0 !h-24px !leading-24px" type="danger" link @click="handleRemoveStudent(row, 'remove')" v-if="arrangeData.ID && row.AdjustFlag!=3 && !row.AwawType">移除</el-button>
											<el-button class="!p-0 !h-24px !leading-24px" type="danger" link @click="handleRemoveStudent(row, 'delete')" v-if="arrangeData.ID && row.AdjustFlag==3">移除</el-button>
										</template>
									</el-table-column>
								</el-table>
							</div>
						</div>
					</el-tab-pane>

					<el-tab-pane name="reserved" v-if="(arrangeData.IsSubscribeCourse == 1 || (arrangeData.SubscribeCourseList&&arrangeData.SubscribeCourseList.length)||arrangeData.CourseMethod == 30)">
						<template #label>
							<span>预约学员</span>
						</template>
						
						<!-- 预约学员内容 -->
						<div v-loading="arrangeTabLoading">
							<!-- 搜索和操作区域 -->
							<div class="flex items-center my-16px my-12px">
								<div class="p-0">
									<el-input
										v-model="reservedSearchKeyword"
										placeholder="学员姓名"
										class="!w-240px"
										clearable
										@keyup.enter="handleSubscribedSearch"
									>
										<template #append>
											<el-button type="primary" @click="handleSubscribedSearch">搜索</el-button>
										</template>
									</el-input>
								</div>
							</div>

							<!-- 预约学员表格 -->
							<div class="table-container mb-20px">
								<el-table 
									:data="arrangeData.SubscribeCourseList || []" 
									class="w-100%"
									border
								>
									<template #empty>
										<el-empty
											:image="globalData.emptyPng"
											description="暂无数据"
											:image-size="100"
										></el-empty>
									</template>
									<el-table-column prop="StudentName" label="姓名" width="210">
										<template #default="{ row }">
											<div class="student-name">
												<div class="avatar">
													<div class="avatar" v-if="row.StudentPhotoPath">
														<img :src="row.StudentPhotoPath" alt="" />
													</div>
													<div class="avatar placeholder" v-else>
														{{ getNameInitial(row.StudentName) }}
													</div>
												</div>
												<span v-if="row.highlightedName" v-html="row.highlightedName"></span>
												<span v-else>{{ row.StudentName || '' }}</span>
											</div>
										</template>
									</el-table-column>
									<el-table-column prop="StudentSerial" label="学号" width="120" />
									<el-table-column prop="StudentSmsTel" label="联系电话" width="150" />
									<el-table-column prop="SubscribeType" label="预约状态" min-width="120">
										<template #default="{ row }">
											<el-tag v-if="row.SubscribeType === '已预约'" type="success" size="small" effect="light">{{ row.SubscribeType }}</el-tag>
											<el-tag v-else type="info" size="small" effect="light">{{ row.SubscribeType || '' }}</el-tag>
										</template>
									</el-table-column>
									<el-table-column label="操作" width="120" fixed="right" align="center">
										<template #default="{ row }">
											<el-button type="danger" size="small" link @click="handleCancelSubscribe(row)" v-if="NewCourse_CancelSubscribeCourseRecords && row.SubscribeType == '已预约'">取消预约</el-button>
										</template>
									</el-table-column>
								</el-table>
							</div>
						</div>
					</el-tab-pane>

					<el-tab-pane name="records" v-if="arrangeData.Finished == 1">
						<template #label>
							<span>点名记录</span>
						</template>
						
						<!-- 点名记录内容 -->
						<div v-loading="arrangeTabLoading">
							<!-- 记录头部信息 -->
							<div class="mt-[16px] mb-12px flex justify-between items-center" v-if="arrangeData.Finished == 1">
								<div class="flex items-center">
									<template v-if="arrangeData.ConfirmUserName">
										<div class="avatar">
											<div class="avatar" v-if="arrangeData.ConfirmUserPhoto">
												<img :src="arrangeData.ConfirmUserPhoto" alt="" />
											</div>
											<div class="avatar placeholder" v-else>
												{{ getNameInitial(arrangeData.ConfirmUserName) }}
											</div>
										</div>
										<span class="ml-16px color-[#303133] font-500">{{ arrangeData.ConfirmUserName }}</span>
										<span class="ml-32px color-[#606266]">{{transToConfigDescript('点名上课')}}</span>
									</template>
								</div>
								<div class="color-[#606266] text-12px">{{ dayjs(arrangeData.ConfirmTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
							</div>

							<!-- 点名记录表格 -->
							<div class="table-container mb-20px ml-40px" :class="{ '!ml-0': !arrangeData.ConfirmUserName }">
								<el-table :data="arrangeData.CourseRollCallList || []" class="w-100%" border>
									<template #empty>
										<el-empty
											:image="globalData.emptyPng"
											description="暂无数据"
											:image-size="100"
										></el-empty>
									</template>
									<el-table-column prop="CreateTime" label="点名时间" width="160" />
									<el-table-column prop="FullName" label="点名人" width="210"></el-table-column>
									<el-table-column prop="Action" label="操作类型" width="140"/>
									<el-table-column prop="Content" label="内容"/>
								</el-table>
							</div>
						</div>
					</el-tab-pane>
				</el-tabs>
			</div>
		</div>
    </el-drawer>
	


	<batchArrangeInfo ref="batchArrangeInfoRef"></batchArrangeInfo>
	<editArrangeForm ref="editArrangeFormRef"></editArrangeForm>
	<chooseStudent ref="chooseStudentRef"></chooseStudent>
	<workFlowPopup ref="workFlowPopupRef"></workFlowPopup>
	<qrSignCodePop ref="qrSignCodePopRef"></qrSignCodePop>
	<editArrangeContent ref="editArrangeContentRef"></editArrangeContent>
	<printArrangeStudent ref="printArrangeStudentRef"></printArrangeStudent>
	<cancelCourseForm ref="cancelCourseFormRef"></cancelCourseForm>
	<adjustCourse ref="adjustCourseRef"></adjustCourse>
	<!-- 取消预约弹窗 -->
	<el-dialog
		v-model="cancelSubscribeDialog"
		title="取消预约"
		width="600px"
		:close-on-click-modal="false"
		@close="closeCancelSubscribeDialog"
	>
		<div class="pb-20px">
			<div class="my-20px my-8px">
				<label class="block mb-12px text-14px color-[#333] font-500">取消原因</label>
				<el-input
					v-model="cancelReason"
					type="textarea"
					:rows="4"
					placeholder="请输入取消原因(限100字)"
					maxlength="100"
					show-word-limit
					class="w-100%"
				/>
			</div>
			<el-checkbox v-model="sendNotification" :true-label="1" :false-label="0">
				以微信/短信形式给家长发送取消预约信息(推送微信需要家长关注并登录"师生信"公众号)
			</el-checkbox>
		</div>
		<template #footer>
			<el-button @click="closeCancelSubscribeDialog">取消</el-button>
			<el-button type="primary" @click="confirmCancelSubscribe">确定</el-button>
		</template>
	</el-dialog>
</template>

<script lang="ts" setup>
import useClipboard from 'vue-clipboard3';
import { computed, ref, watch, getCurrentInstance, nextTick } from 'vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { dayjs } from 'element-plus'
import { ArrowUp, ArrowDown, ArrowRight, Edit, Check, MoreFilled, CaretRight } from '@element-plus/icons-vue';
import chooseStudent from '@/components/popup/chooseStudent.vue';
import batchArrangeInfo from './batchArrangeInfo.vue';
import editArrangeForm from './editArrangeForm.vue';
import workFlowPopup from '../popup/workFlowPopup.vue'
import qrSignCodePop from './qrSignCodePop.vue'
import editArrangeContent from './editArrangeContent.vue';
import printArrangeStudent from './printArrangeStudent.vue';
import cancelCourseForm from './cancelCourseForm.vue';
import adjustCourse from './adjustCourse.vue';
import { useConfigs } from '@/store';
import useEvent from '@/config/event';
import { cloneDeep } from 'lodash'
import { 
	getCourseDetailByID,
	chekSeeCourseware,
	getFilesUrl,
	addCourseStudent,
	reduceCourseStudent,
	removeCourseStudent,
	RemoveCourseAdjustOrTryStudents,
	updateDescribeByCourseStudent,
	cancelCourseSubscribeCourseRecords,
	getCourseShiftSchedule,
	deleteCourseList,
	deleteCourseAsk
} from '@/api/arrange';
import { transToConfigDescript } from '@/utils/filters/filters';
import { getNameInitial } from '@/utils';

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
// 权限项和权限项
const configs = computed(() => {
	return useConfigs().configs
})

//权限
const NewCourse_CourseEdit = window.$xgj.op('NewCourse_CourseEdit') //编辑排课
const NewCourse_CourseDelete=window.$xgj.op("NewCourse_CourseDelete") //删除上课记录
const NewCourse_CourseCancel=window.$xgj.op("NewCourse_CourseCancel")//取消排课
const NewCourse_CourseStudentRemove = window.$xgj.op("NewCourse_CourseStudentRemove") // 从排课中移除学员
const NewCourse_CourseStudentEdit = window.$xgj.op("NewCourse_CourseStudentEdit") // 往排课添加学员
const NewCourse_CourseStudentAdjustLesson = window.$xgj.op('NewCourse_CourseStudentAdjustLesson'); // 临时调课
const NewCourse_CancelSubscribeCourseRecords = window.$xgj.op("NewCourse_CancelSubscribeCourseRecords"); // 取消预约
const CourseBeginPower = window.$xgj.op("NewCourse_CourseBegin") || window.$xgj.op("NewCourse_CourseBeginLimit"); //点名上课
const NewCourse_CourseWareQuery = window.$xgj.op("NewCourse_CourseWareQuery"); // 电脑端排课管理随时查看课件。没有改权限时，需要根据课件规则决定是否能查看
const NewCourse_PrintCourseRecord=window.$xgj.op("NewCourse_PrintCourseRecord")//打印上课凭证

const EnableCourseware = computed(()=>{ //是否开启课件管理功能(EnableCourseware)，1：是，0：否(默认)
	return configs.value.EnableCourseware == 1;
})
const EnableCourseRole = computed(()=>{ //是否开启排课角色。0：不开启（默认），1：开启
	return configs.value.EnableCourseRole==1;
})
const IsOpenShiftForDay=computed(()=>{ //开启按天计费或者按月计费功能：0按月计费（默认），1按天计费。
	return configs.value.IsOpenShiftForDay==1;
})
const EnableMonthShiftCourse=computed(()=>{ //按月计费课程是否支持手动排课出勤不计费：0否，1是（白塔岭定制）。
	return configs.value.EnableMonthShiftCourse==1
})
const EnableNewTryCourse = computed(()=>{ //是否启用新版试听管理:0否（只能对试听学员安排试听），1是（支持意向学员和正式学员试听）
	return configs.value.EnableNewTryCourse == 1;
})
const EnableAddCourse_ShiftSchedule=computed(()=>{ //在排课时，是否根据课程上设置的上课进度自动生成每一节课的上课进度：1是，0否(默认)
	return configs.value.EnableAddCourse_ShiftSchedule==1
})
const EnablePrintOneToOneCourseRecord=computed(()=>{ //是否开启打印一对一课程上课凭证：0不开启（默认），1开启
	return configs.value.EnablePrintOneToOneCourseRecord==1
})

const event = useEvent();
const IsCPD = ref(false); // 是否为按天计费
const MaxCourseImgCount = ref(9); // 最大照片数量
const shiftScheduleList = ref<any[]>([]); // 上课内容数据
const drawer = ref(false);
const arrangeData = ref<IArrangeData>({});

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
	ConfirmTime?: string; // 排课点名确认时间
	ConfirmUserPhoto?: string; // 排课点名确认人头像
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
	IsSubscribeCourse?:number;//是否开放预约
	IsFromSubscribeCourse?: number; // 1来源于约课，0否
	StartStudentCount?: number; // 开课人数
	MaxStudentCount?: number; // 可约人数
	CourseStatus?: number; // 开课状态
	SubscribedCount?: number; // 已约人数
	MaxStudentsAmount?: number; // 班级最大上课人数
	
	// 其他信息
	Describe?: string; // 对外备注
	InternalRemark?: string; // 对内备注
	ExternalRemark?: string; // 对外备注
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
	IsCover?: number; // 是否覆盖
	ClassShiftAmount?: number; // 班级上的当前上课进度

	// 学员列表
	CourseStudentList?: IStudent[];
	// 学员汇总数据
	CourseStuentTotal?: ICourseStuentTotal[];
	// 点名记录
	CourseRollCallList?: ICourseRollCall[];
	// 预约学员列表
	SubscribeCourseList?: ISubscribeStudent[];
	// 老师列表
	TeacherList?: any[];
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

// 上课学员相关接口
interface IStudent {
	StudentUserID: string; // 学员ID
	CourseStudentID: string; // 排课学员ID
	StudentPhotoPath: string; // 学员头像
	StudentName: string; // 学员姓名
	StudentSmsTel: string; // 学员手机号
	IsAttend: number; // 是否出勤
	IsCost: number; // 是否计费
	IsTry: number; // 是否试听
	AbsentReason: string; // 缺勤原因
	Remark: string; // 备注
	IsMend?: number; // 是否补课
	AdjustFlag?: number; // 调整标志：2临调，3临加
	IsAttendStauts?: number; // 出勤状态：1请假
	IsSubscribecourse?: number; // 是否约课：1约课
	StudentStatus?: number; // 学员状态：-1已删除
	showEditIcon?: boolean; // 控制编辑图标显示
	showPopover?: boolean; // 控制 popover 显示
	editRemarkText?: string; // 编辑时的临时文本
	highlightedName?: string; // 高亮后的姓名（包含HTML标签）
	isSaving?: boolean; // 保存备注时的loading状态
}

// 学员汇总数据接口
interface ICourseStuentTotal {
	Total?: number; // 人员数量
	IsAttend?: number; // 是否出勤
	IsCost?: number; // 是否计费
	IsTry?: number; // 是否试听
}

// 预约学员相关接口
interface ISubscribeStudent {
	SubscribeCourseRecordsID: string | null; // ID
	StudentPhotoPath: string | null; // 学员头像
	StudentName: string | null; // 学员姓名
	StudentSerial: string | null; // 学员学号
	StudentSmsTel: string | null; // 学员手机号
	SubscribeType: string | null; // 预约状态
	highlightedName?: string; // 高亮后的姓名（包含HTML标签）
}

// 点名记录接口
interface ICourseRollCall {
	Type?: string; // 状态，出勤，缺勤，计费，试听
	Count?: number; // 人数
	StudentNames?: string; // 学员姓名
}

// 展开收起状态管理
const activeNames = ref(['attendance', 'content', 'teacher', 'course', 'appointment', 'other']);
const showCollapseSections = ref(false); // 控制所有collapse区块的显示/隐藏

// 切换所有collapse区块的显示/隐藏
function toggleAllCollapse() {
	showCollapseSections.value = !showCollapseSections.value;
}

// 获取签到二维码
const qrSignCodePopRef=ref<InstanceType<typeof qrSignCodePop> | null>(null)
function getScanCode(){
	qrSignCodePopRef.value?.open({
		ID:arrangeData.value.ID,
		ClassName:arrangeData.value.ClassName
	})
}

const adjustCourseRef=ref<InstanceType<typeof adjustCourse> | null>(null)
//临时调课
function openAdjustCourse(item:any){
	adjustCourseRef.value?.open({
		data:arrangeData.value
	}).then((res:any)=>{
		event.emit("arrange-table-list-refresh",{refreshTotal:true})
	})
}

// 打印点名表
const printArrangeStudentRef=ref<InstanceType<typeof printArrangeStudent> | null>(null)
function printCourseStudentList(){
	printArrangeStudentRef.value?.open({
		ids: arrangeData.value.ID
	})
}

function printOneToOne(row:any){
	if (window.microApp) {
		window.microApp.dispatch({type:'scheduleManage:printCourseCard',courseCardData:{courseid:row.ID,campusid:row.CampusID}})
	}
}

// 复制主要信息
async function copyMainInfo() {
	try {
		const { toClipboard } = useClipboard();
		const data = arrangeData.value;
		
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
			{ label: transToConfigDescript('任课老师'), value: teacherDisplayNames.value.main },
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

// 修改上课内容
const editArrangeContentRef=ref<InstanceType<typeof editArrangeContent> | null>(null)
function editContent() {
	editArrangeContentRef.value?.open({
		selected:[arrangeData.value]
	}).then(()=>{
		getArrangeData();
		event.emit("arrange-table-list-refresh")
	})
}

// 取消排课
const cancelCourseFormRef=ref<InstanceType<typeof cancelCourseForm> | null>(null)
function cancelArrange(){
	cancelCourseFormRef.value?.open({
		popTitle:'取消排课',
		isBatch:false,
		isEdit:false,
		data:arrangeData.value,
		gparams:{
			courseIDList:[arrangeData.value.ID]
		}
	}).then((res:any)=>{
		close();
		event.emit("arrange-table-list-refresh",{refreshTotal:true})
	})
}

// 删除排课
const workFlowPopupRef=ref<InstanceType<typeof workFlowPopup> | null>(null)
function delArrange(){
	ElMessageBox.confirm(`警告！删除的排课不可恢复，请谨慎操作。`, '提示', {
		confirmButtonText: '确认删除',
		cancelButtonText: '取消'
	}).then((res:any)=>{
		doDelete(()=> {
			close();
			event.emit("arrange-table-list-refresh",{refreshTotal:true})
		})
	})
}
function doDelete(callback:Function){
	arrangeDataLoading.value=true
	deleteCourseList({
		planID:globalData.EMPTYGUID,
		courseIDList:[arrangeData.value.ID]
	}).then((res:any)=>{
		if(res.Data.IsMatchWorkflow){
			workFlowPopupRef.value?.open({
				popTitle:'删除排课',
				api:deleteCourseAsk,
				data:{
					data:arrangeData.value.ID,
					WorkflowId:res.Data.WorkflowId
				}
			}).then((res:any)=>{
				callback&&callback()
			})
		}else{
			ElMessage.success('已删除')
			callback&&callback()
		}
	}).finally(()=>{
		arrangeDataLoading.value=false
	})
}

// 显示批次排课弹窗
const batchArrangeInfoRef = ref<InstanceType<typeof batchArrangeInfo> | null>(null)
function openBatchScheduleDialog() {
	batchArrangeInfoRef.value?.open({
		ID: arrangeData.value.ID,
		PlanID: arrangeData.value.PlanID,
		CourseMethod:arrangeData.value.CourseMethod,
		CampusID:arrangeData.value.CampusID
	})
}

// 监听批次删除成功事件
event.on('arrange-info-refresh', () => {
	// 如果当前课程是未上课状态（Finished === 0），则关闭当前弹窗
	if (arrangeData.value.Finished === 0) {
		close();
	}
	// 如果当前课程是已上课状态（Finished === 1），则保持弹窗打开，刷新数据
	else if (arrangeData.value.Finished === 1) {
		getArrangeData();
	}
})

// 显示编辑排课弹窗
const editArrangeFormRef = ref<InstanceType<typeof editArrangeForm> | null>(null)
function openEditArrangeForm() {
	editArrangeFormRef.value?.open({
		CampusID: arrangeData.value.CampusID,
		ID: arrangeData.value.ID,
		ShiftID: arrangeData.value.ShiftID,
	}).then(() => {
		// event.emit("arrange-table-list-refresh",{refreshTotal:true})
		getArrangeData();
	})
}

// 显示点名上课弹窗
function rollCall() {
	if (window.microApp) {
		let info=cloneDeep(arrangeData.value)
		// 将Duration从分钟转换为xx:yy格式
		let formattedDuration: string | number | undefined = info.Duration
        if(info.Duration){
            const hours = Math.floor(info.Duration / 60)
            const minutes = info.Duration % 60
            formattedDuration = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
        }
		window.microApp.dispatch({type:'scheduleManage:rollCall',courseData:{...info, Duration: formattedDuration as any}},(res:any)=>{
			if(res){
				res[0].then(()=>{
					event.emit("arrange-table-list-refresh",{refreshTotal:true})
					getArrangeData();
				})
			}
		})
	}
}

// 学员管理相关数据
const activeTab = ref<'students' | 'reserved' | 'records'>('students');
const searchKeyword = ref('');
const reservedSearchKeyword = ref('');
const originalStudentList = ref<IStudent[]>([]); // 保存原始学员顺序
const originalSubscribeList = ref<ISubscribeStudent[]>([]); // 保存原始预约学员顺序

// 监听搜索关键词变化，自动清除高亮并还原顺序
watch(searchKeyword, (newValue) => {
	if (!newValue.trim()) {
		// 当搜索关键词为空时，清除所有高亮并还原原始顺序
		if (arrangeData.value.CourseStudentList && originalStudentList.value.length > 0) {
			arrangeData.value.CourseStudentList.forEach(student => {
				student.highlightedName = undefined;
			});
			// 还原原始顺序
			arrangeData.value.CourseStudentList = [...originalStudentList.value];
		}
	}
});

// 监听预约学员搜索关键词变化，自动清除高亮并还原顺序
watch(reservedSearchKeyword, (newValue) => {
	if (!newValue.trim()) {
		// 当搜索关键词为空时，清除所有高亮并还原原始顺序
		if (arrangeData.value.SubscribeCourseList && originalSubscribeList.value.length > 0) {
			arrangeData.value.SubscribeCourseList.forEach(student => {
				student.highlightedName = undefined;
			});
			// 还原原始顺序
			arrangeData.value.SubscribeCourseList = [...originalSubscribeList.value];
		}
	}
});

// 课件相关数据
const coursewarePopoverVisible = ref(false);

// 上课学员搜索
function handleSearch() {
	const keyword = searchKeyword.value.trim();
	
	// 如果没有搜索关键词，清除所有高亮并还原原始顺序
	if (!keyword) {
		if (arrangeData.value.CourseStudentList && originalStudentList.value.length > 0) {
			arrangeData.value.CourseStudentList.forEach(student => {
				student.highlightedName = undefined;
			});
			// 还原原始顺序
			arrangeData.value.CourseStudentList = [...originalStudentList.value];
		}
		return;
	}
	
	// 遍历学员列表，设置高亮状态并标记匹配状态
	if (arrangeData.value.CourseStudentList) {
		// 第一次搜索时保存原始顺序
		if (originalStudentList.value.length === 0) {
			originalStudentList.value = [...arrangeData.value.CourseStudentList];
		}
		
		const matchedStudents: IStudent[] = [];
		const unmatchedStudents: IStudent[] = [];
		
		arrangeData.value.CourseStudentList.forEach(student => {
			// 使用toLowerCase()进行忽略大小写的比较
			if (student.StudentName.toLowerCase().includes(keyword.toLowerCase())) {
				// 将匹配的关键字用span标签包裹，使用gi标志忽略大小写
				const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
				student.highlightedName = student.StudentName.replace(regex, '<span class="highlighted-keyword">$1</span>');
				matchedStudents.push(student);
			} else {
				student.highlightedName = undefined;
				unmatchedStudents.push(student);
			}
		});
		
		// 将匹配的学员放在前面，未匹配的学员放在后面
		arrangeData.value.CourseStudentList = [...matchedStudents, ...unmatchedStudents];
	}
}

function handleSubscribedSearch() {
	const keyword = reservedSearchKeyword.value.trim();
	
	// 如果没有搜索关键词，清除所有高亮并还原原始顺序
	if (!keyword) {
		if (arrangeData.value.SubscribeCourseList && originalSubscribeList.value.length > 0) {
			arrangeData.value.SubscribeCourseList.forEach(student => {
				student.highlightedName = undefined;
			});
			// 还原原始顺序
			arrangeData.value.SubscribeCourseList = [...originalSubscribeList.value];
		}
		return;
	}
	
	// 遍历预约学员列表，设置高亮状态并标记匹配状态
	if (arrangeData.value.SubscribeCourseList) {
		// 第一次搜索时保存原始顺序
		if (originalSubscribeList.value.length === 0) {
			originalSubscribeList.value = [...arrangeData.value.SubscribeCourseList];
		}
		
		const matchedStudents: ISubscribeStudent[] = [];
		const unmatchedStudents: ISubscribeStudent[] = [];
		
		arrangeData.value.SubscribeCourseList.forEach(student => {
			// 使用toLowerCase()进行忽略大小写的比较
			if (student.StudentName && student.StudentName.toLowerCase().includes(keyword.toLowerCase())) {
				// 将匹配的关键字用span标签包裹，使用gi标志忽略大小写
				const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
				student.highlightedName = student.StudentName.replace(regex, '<span class="highlighted-keyword">$1</span>');
				matchedStudents.push(student);
			} else {
				student.highlightedName = undefined;
				unmatchedStudents.push(student);
			}
		});
		
		// 将匹配的学员放在前面，未匹配的学员放在后面
		arrangeData.value.SubscribeCourseList = [...matchedStudents, ...unmatchedStudents];
	}
}

// 取消预约弹窗相关数据
const cancelSubscribeDialog = ref(false);
const cancelReason = ref('');
const sendNotification = ref(1);
const currentCancelSubscribe = ref<ISubscribeStudent | null>(null);

// 预约学员取消
function handleCancelSubscribe(row: ISubscribeStudent) {
	currentCancelSubscribe.value = row;
	cancelReason.value = '';
	sendNotification.value = 1;
	cancelSubscribeDialog.value = true;
}

// 确认取消预约
let arrangeTabLoading = ref(false);
function confirmCancelSubscribe() {
	if (!currentCancelSubscribe.value) return;
	arrangeTabLoading.value = true;
	
	cancelCourseSubscribeCourseRecords({
		SubscribeCourseRecordsID: currentCancelSubscribe.value.SubscribeCourseRecordsID,
		CancelReason: cancelReason.value,
		IsSendMsg: sendNotification.value,
	}).then((res: any) => {
		ElMessage.success('已取消预约');
		// 关闭弹窗
		cancelSubscribeDialog.value = false;
		currentCancelSubscribe.value = null;
		// 刷新排课数据
		getArrangeData();
	}).catch((error: any) => {
		// ElMessage.error('预约取消失败');
	}).finally(() => {
		arrangeTabLoading.value = false;
	})
}

// 关闭取消预约弹窗
function closeCancelSubscribeDialog() {
	cancelSubscribeDialog.value = false;
	currentCancelSubscribe.value = null;
	cancelReason.value = '';
}


// 备注编辑相关方法
function handleRemarkHover(student: IStudent) {
	student.showEditIcon = true;
}

function handleRemarkLeave(student: IStudent) {
	// 如果 popover 正在显示，不要隐藏编辑图标
	if (!student.showPopover) {
		student.showEditIcon = false;
	}
}

function handlePopoverShow(student: IStudent) {
	student.editRemarkText = student.Remark || '';
}

function handlePopoverHide(student: IStudent) {
	student.editRemarkText = student.Remark || '';
}

function saveRemarkEdit(student: IStudent) {
	// 添加loading状态
	student.isSaving = true;

	updateDescribeByCourseStudent({
		courseStudentID: student.CourseStudentID,
		describe: student.editRemarkText || '',
	}).then((res: any) => {
		student.Remark = student.editRemarkText || '';
		student.showPopover = false;
		student.editRemarkText = '';
		ElMessage.success('备注已保存');
	}).catch((error: any) => {
		// ElMessage.error('备注保存失败');
	}).finally(() => {
		// 清除loading状态
		student.isSaving = false;
	});
}

function cancelRemarkEdit(student: IStudent) {
	student.showPopover = false;
	student.editRemarkText = '';
}

// 表格汇总方法
function getSummaries(param: { columns: any[], data: any[] }) {
	const { columns, data } = param;
	const sums: string[] = [];
	
	// 获取后端返回的汇总数据
	const totalData = arrangeData.value.CourseStuentTotal;
	
	columns.forEach((column, index) => {
		if (index === 0) {
			// 使用后端返回的总人数
			const totalCount = totalData?.find(item => item.Total !== undefined)?.Total || data.length;
			sums[index] = `合计：共${totalCount}人`;
			return;
		}
		
		if (index === 2) { // 出勤列
			// 使用后端返回的出勤人数
			const attendanceCount = totalData?.find(item => item.IsAttend !== undefined)?.IsAttend || data.filter(item => item.IsAttend).length;
			sums[index] = `${attendanceCount}人`;
		} else if (index === 3) { // 计费列
			// 使用后端返回的计费人数
			const billingCount = totalData?.find(item => item.IsCost !== undefined)?.IsCost || data.filter(item => item.IsCost).length;
			sums[index] = `${billingCount}人`;
		} else if (index === 4) { // 试听列
			// 使用后端返回的试听人数
			const trialCount = totalData?.find(item => item.IsTry !== undefined)?.IsTry || data.filter(item => item.IsTry).length;
			sums[index] = `${trialCount}人`;
		} else {
			sums[index] = '';
		}
	});
	
	return sums;
}

// 上课学员 - 添加学员
const chooseStudentRef = ref()
function handleAddStudent() {
	chooseStudentRef.value?.open({
		multi: true,
		condition: {
			shiftName: arrangeData.value.ShiftName,
			campusid: arrangeData.value.CampusID,
			status: ["1"]
		}
	}).then((res: any) => {
		if (res.data) {
			let newStudentIds = res.data.map((item: any) => item.ID).join();
			arrangeTabLoading.value = true;
			addCourseStudent({
				id: arrangeData.value.ID,
				students: newStudentIds
			}).then((res: any) => {
				ElMessage.success('学员已添加');
				getArrangeData();
			}).catch((error: any) => {
				// ElMessage.error('添加学员失败');
			}).finally(() => {
				arrangeTabLoading.value = false;
			})
		}
	})
}

// 上课学员 - 移除学员
function handleRemoveStudent(row: any, type: string) {
	let _msg = type == 'remove' ? `是否将学员"${row.StudentName}"从该堂课中移除（可在临时调课功能中撤销）？` : `确定将"${row.StudentName}"从该排课中移除吗？`;
	ElMessageBox.confirm(
		_msg,
		'确认',
		{
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning',
		}
	)
	.then(() => {
		arrangeTabLoading.value = true;
		if (type == 'remove') {
			let _url = reduceCourseStudent,
				_params: any = {
					students: row.StudentUserID,
					id: arrangeData.value.ID,
					updateTime: arrangeData.value.UpdateTime
				};

			if(row.AdjustFlag == 2 || row.IsTry == 1) {
				_url = RemoveCourseAdjustOrTryStudents;
				_params.type = row.AdjustFlag == 2 ? 0 : 1;
			}

			_url(_params).then((res: any) => {
				ElMessage.success('学员已移除');
				getArrangeData();
			}).catch((error: any) => {
				// ElMessage.error('学员移除失败');
			}).finally(() => {
				arrangeTabLoading.value = false;
			})
		} else if(type == 'delete') {
			removeCourseStudent({
				student: row.StudentUserID,
				id: arrangeData.value.ID,
				updateTime: arrangeData.value.UpdateTime
			}).then((res: any) => {
				ElMessage.success('学员已移除');
				getArrangeData();
			})
			.catch((error: any) => {
				// ElMessage.error('学员移除失败');
			})
			.finally(() => {
				arrangeTabLoading.value = false;
			})
		}
	})
}

// 处理课件点击
function showWareDetail(item: any, fileId: string) {
	if (!NewCourse_CourseWareQuery) {
		arrangeDataLoading.value = true
		chekSeeCourseware({
			terminal: 0,
			shiftId: item.ShiftID,
			courseId: courseID.value,
		})
			.then((res: any) => {
				if (res.Data == 1) {
					queryWareDetail(fileId)
				} else {
					arrangeDataLoading.value = false
					ElMessage.warning('无查看权限')
				}
			})
			.catch(() => {
				arrangeDataLoading.value = false
			})
	} else {
		queryWareDetail(fileId)
	}
}
function queryWareDetail(fileId: string) {
	arrangeDataLoading.value = true;
	getFilesUrl({ pagetype: 1 }).then((res: any) => {
		var previewUrl = res.Data + '?preview=true&id=' + fileId
		window.open(previewUrl)
	}).finally(() => {
		arrangeDataLoading.value = false;
	})
}

function resetData() {
	arrangeData.value = {};
	activeNames.value = ['attendance', 'content', 'teacher', 'course', 'appointment', 'other'];
	showCollapseSections.value = false; // 控制所有collapse区块的显示/隐藏
	activeTab.value = 'students';
	searchKeyword.value = '';
	reservedSearchKeyword.value = '';
	originalStudentList.value = []; // 清空原始学员列表
	originalSubscribeList.value = []; // 清空原始预约学员列表
}

// 老师相关变量
const mainTeacherList=ref([] as any)//老师 role:1
const assistantTeacherList=ref([] as any)//助教 role:2
const replaceTeacherList=ref([] as any)//替课老师 role:3
const followTeacherList=ref([] as any)//跟课老师 role:4
const pianoTeacherList=ref([] as any)//钢伴老师 role:5
const officeTeacherList=ref([] as any)//坐班老师 role:6
const consultantTeacherList=ref([] as any)//课程顾问 role:7

// 老师名称显示的计算属性
const teacherDisplayNames = computed(() => ({
	main: mainTeacherList.value.length > 0 ? mainTeacherList.value.map((t: any) => t.LabelName).join(', ') : '-',
	assistant: assistantTeacherList.value.length > 0 ? assistantTeacherList.value.map((t: any) => t.LabelName).join(', ') : '-',
	replace: replaceTeacherList.value.length > 0 ? replaceTeacherList.value.map((t: any) => t.Name).join(', ') : '-',
	follow: followTeacherList.value.length > 0 ? followTeacherList.value.map((t: any) => t.Name).join(', ') : '-',
	piano: pianoTeacherList.value.length > 0 ? pianoTeacherList.value.map((t: any) => t.Name).join(', ') : '-',
	office: officeTeacherList.value.length > 0 ? officeTeacherList.value.map((t: any) => t.Name).join(', ') : '-',
	consultant: consultantTeacherList.value.length > 0 ? consultantTeacherList.value.map((t: any) => t.Name).join(', ') : '-',
	teacherPostionTypeName: mainTeacherList.value.length > 0 ? mainTeacherList.value.map((t: any) => t.TeacherPostionType).join(', ') : '-'
}))

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

// 获取课程的上课进度
async function getShiftSchedule(){
	try {
		arrangeDataLoading.value = true;
		const res = await getCourseShiftSchedule({
			shiftId: shiftID.value
		});
		shiftScheduleList.value = res.Data;
	} catch (error) {
		console.error('获取上课进度失败:', error);
	} finally {
		arrangeDataLoading.value = false;
	}
}

function processArrangeSchedule() {
	let schList = shiftScheduleList.value || [],
		info:any = {},
		shiftAmount = 0;

	info.ShiftAmount = shiftAmount = (arrangeData.value.ShiftAmount || 0) * 1;
	info.ShiftScheduleID = globalData.EMPTYGUID;
	info.ChapterContent = "";
	info.CourseContent = arrangeData.value.CourseContent || "";
	info.CoursewareSettingList = arrangeData.value.CoursewareSettingList || [];
	info.Describe = arrangeData.value.Describe || "";
	info.InternalRemark = arrangeData.value.InternalRemark || "";
	if(schList.length > 0) { //有上课进度
		if(arrangeData.value.Finished == 0 && arrangeData.value.IsCover == 0) { // 1.未进行上课点名
			if(!EnableAddCourse_ShiftSchedule.value && !info.ShiftAmount) {
				//排课时，没确定每次课的上课进度，此时要从班级上拿到当前的进度+1
				var classShiftAmount = (arrangeData.value.ClassShiftAmount || 0) * 1; //班级上的当前上课进度
				info.ShiftAmount =shiftAmount= (classShiftAmount >= schList.length) ? schList.length : (classShiftAmount + 1);
			}
		}
		schList.forEach(function(item) {
			if(item.ShiftAmount == shiftAmount) {
				info.ShiftScheduleID = item.ID;
				info.ChapterContent = item.Title;
				info.CoursewareSettingList = item.CoursewareList;
				if(!arrangeData.value.CourseContent) {
					info.CourseContent = item.Content;
				}
			}
		});
	};

	// 将处理后的信息合
	Object.assign(arrangeData.value, info);
}


// 获取排课数据
async function getArrangeData() {
	arrangeDataLoading.value = true;
	arrangeData.value = {};
	originalStudentList.value = []; // 清空原始学员列表，重新加载时重置
	originalSubscribeList.value = []; // 清空原始预约学员列表，重新加载时重置
	try {
		const res = await getCourseDetailByID(courseID.value);
		arrangeData.value = res.Data;

		const { Unit, TeacherList} = arrangeData.value;
		IsCPD.value = Unit == 3 && IsOpenShiftForDay.value;
		// 处理TeacherList数组
		if (TeacherList) {
			processTeacherList(TeacherList);
		}
		// 处理上课进度
		// processArrangeSchedule();
		// 如果有搜索关键词，重新应用高亮
		if (searchKeyword.value.trim()) {
			handleSearch();
		}
		if (reservedSearchKeyword.value.trim()) {
			handleSubscribedSearch();
		}
	} catch (error) {
		console.error('获取排课数据失败:', error);
	} finally {
		arrangeDataLoading.value = false;
	}
}

let _resolve: any = null,
	_reject: any = null,
	courseID = ref(''),
	shiftID = ref(''),
	arrangeDataLoading = ref(false);

async function open(params: any) {
	courseID.value = params.ID;
	shiftID.value = params.ShiftID;
	drawer.value = true;
	activeTab.value=params.activeTab||'students'
	try {
		// await getShiftSchedule();
		await getArrangeData();
	} catch (error) {
		console.error('数据加载失败:', error);
	}

	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
	})
}

function close() {
	drawer.value = false
	nextTick(()=>{
		resetData();
	})
	_reject && _reject()
}

defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
.arrangeInfo {
	.info-collapse {
		border: none !important;
		
		:deep(.wtwo-collapse-item) {
			margin: 10px 16px 0 16px;
			border: none !important;
			border-radius: 8px;
			overflow: hidden;
			
			.wtwo-collapse-item__header {
				border: none !important;
				padding: 0 8px;
				font-weight: 600;
				font-size: 14px;
			}
			
			.wtwo-collapse-item__wrap {
				padding: 0 30px;
				border: none !important;
				.wtwo-collapse-item__content {
					padding: 0;
				}
			}
		}
	}
	
	// 统一的信息网格样式
	.info-grid {
		display: grid;
		grid-template-columns: 1fr 1fr 1fr;
		gap: 0 24px;
		
		.info-item {
			font-size: 14px;
			line-height: 22px;
			display: flex;
			flex-direction: column;
			min-width: 0;
			
			.info-label {
				margin-bottom: 4px;
				color: #909399;
			}
			
			.info-value {
				margin-bottom: 16px;
				display: block;
				color: #303133;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
				max-width: 100%;
				min-width: 0;
			}
		}
	}
	
	// 过渡动画样式
	.collapse-fade-enter-active,
	.collapse-fade-leave-active {
		transition: all 0.2s ease;
	}
	
	.collapse-fade-enter-from {
		opacity: 0;
	}
	
	.collapse-fade-leave-to {
		opacity: 0;
	}
	.avatar {
		width: 24px;
		height: 24px;
		border-radius: 4px;
		overflow: hidden;
		background: #f2f3f5;
		flex-shrink: 0;
	}
	.avatar img {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}

	.avatar.placeholder {
		display: flex;
		align-items: center;
		justify-content: center;
		background: linear-gradient(135deg, #2DEEFF 0%, #4593FF 100%);
      	color: #fff;
		font-weight: 500;
		font-size: 14px;
	}
	
	.student-info {
		// 状态标签样式
		.stamp-default {
			height: 20px;
			line-height: 20px;
			display: inline-block;
			padding: 0 8px;
			font-size: 12px;
			border-radius: 4px;
			background-color: #FFF2E5;
			color: #FF7D00;
			margin-left: 8px;
		}
	}
	// 学员管理区域样式
	.student-management-section {
		.table-container {

			.student-name {
				display: flex;
				align-items: center;
				gap: 8px;
				// color: #2878e8;
				
			}

			.remark-cell {
				display: flex;
				align-items: center;
				gap: 8px;
				position: relative;
				
				.remark-text {
					flex: 0 1 auto;
					min-width: 0;
					overflow: hidden;
					text-overflow: ellipsis;
					white-space: nowrap;
					word-break: break-all;
				}

				.remark-text.empty {
					color: #909399;
				}

				.edit-icon {
					color: #409eff;
					cursor: pointer;
					font-size: 14px;
					flex-shrink: 0;
					opacity: 0;
					transition: opacity 0.3s ease;
				}
				
				.edit-icon.visible {
					opacity: 1;
				}
				
				&:hover .edit-icon {
					opacity: 1;
				}
			}
			
			// 高亮样式 - 使用深度选择器确保v-html内容也能应用样式
			:deep(.highlighted-keyword) {
				color: #409eff !important;
			}
		}


	}
	
	.photo-container {
		display: flex;
		align-items: center;
		gap: 8px;
		
		.photo-item {
			position: relative;
			width: 28px;
			height: 28px;
			border-radius: 4px;
			overflow: hidden;
			flex-shrink: 0;
			
			.photo-thumbnail {
				width: 100%;
				height: 100%;
				border-radius: 4px;
			}
			
			.photo-overlay {
				position: absolute;
				top: 0;
				left: 0;
				right: 0;
				bottom: 0;
				background-color: rgba(0, 0, 0, 0.6);
				display: flex;
				align-items: center;
				justify-content: center;
				border-radius: 4px;
				
				.overlay-text {
					color: white;
					font-size: 10px;
					font-weight: 500;
				}
			}
		}
	}
}

// 全局tooltip样式，控制宽度
:global(.custom-tooltip) {
	max-width: 400px !important;
	word-wrap: break-word !important;
	word-break: break-all !important;
}
</style>