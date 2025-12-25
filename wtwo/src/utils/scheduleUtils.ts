/**
 * 排课工具函数
 */

import { cloneDeep } from 'lodash'
import { calculateDuration } from './timeUtils'

/**
 * 计算排课次数
 * @param planRule 排课规则：1-重复排课，0-自由排课
 * @param courseMode 重复规则：Weekly-每周重复，Biweekly-隔周重复，Daily-每天重复，AlternateDay-隔天重复
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @param courseDateList 自由排课的日期列表
 * @param courseTimes 最多排多少（小时/次）
 * @param isHoliday 是否跳过节假日：0-不跳过，1-跳过
 * @param holidays 节假日列表
 * @param planList 上课时段列表，包含Weekday信息
 * @returns 排课次数
 */
export function calculateScheduleCount(
	planRule: number,
	courseMode: string,
	startDate: string,
	endDate: string,
	courseDateList: string[],
	courseTimes: number | string,
	isHoliday: number,
	holidays: any[] = [],
	planList: any[] = [],
	unit: number = 1
): number {
	// 自由排课：根据选择的日期数量和实际时段来排算
	if (planRule === 0) {
		const dateCount = courseDateList?.length || 0
		const planCount = planList?.length || 0
		// 总次数 = 选择的日期数量 × 每个日期的时段数量
		let totalCount = dateCount * planCount
		
		// 如果指定了最多排课次数，需要考虑排课上限
		if (courseTimes && Number(courseTimes) > 0) {
			const maxCourseTimes = Number(courseTimes)
			
			if (unit === 1) {
				// 按小时排课：计算总时长，然后按顺序排课直到达到时长上限
				const totalMinutes = planList.reduce((total: number, session: any) => {
					return total + calculateDuration([session.StartTime, session.EndTime])
				}, 0) * dateCount
				
				const maxMinutes = maxCourseTimes * 60
				if (totalMinutes <= maxMinutes) {
					// 总时长不超过上限，可以排完所有课
					return totalCount
				} else {
					// 需要按顺序排课，直到达到时长上限
					let currentMinutes = 0
					let currentCount = 0
					
					for (let dateIndex = 0; dateIndex < dateCount; dateIndex++) {
						for (let planIndex = 0; planIndex < planCount; planIndex++) {
							const session = planList[planIndex]
							const sessionMinutes = calculateDuration([session.StartTime, session.EndTime])
							
							if (currentMinutes + sessionMinutes <= maxMinutes) {
								currentMinutes += sessionMinutes
								currentCount++
							} else {
								// 超出时长上限，停止排课
								return currentCount
							}
						}
					}
					return currentCount
				}
			} else if (unit === 2) {
				// 按次排课：直接限制次数
				return Math.min(totalCount, maxCourseTimes)
			} else {
				// 按月排课或其他情况
				return Math.min(totalCount, maxCourseTimes)
			}
		}
		
		return totalCount
	}

	// 重复排课：根据日期范围和重复规则动态计算
	if (planRule === 1 && startDate && endDate) {
		// 使用通用函数计算重复排课信息
		const scheduleInfo = calculateRepeatedScheduleInfo(
			startDate,
			endDate,
			courseMode,
			planList,
			isHoliday,
			holidays,
			unit,
			courseTimes
		)

		let scheduleCount = scheduleInfo.count

		// 调试信息
		console.log(`calculateScheduleCount调试 - courseTimes: ${courseTimes}, scheduleInfo.count: ${scheduleInfo.count}, scheduleCount: ${scheduleCount}`)

		// 如果指定了最多排课次数，取较小值
		if (courseTimes && Number(courseTimes) > 0) {
			scheduleCount = Math.min(scheduleCount, Number(courseTimes))
			console.log(`应用courseTimes限制后: ${scheduleCount}`)
		}

		return scheduleCount
	}

	return 0
}

/**
 * 将分钟数转换为小时分钟格式
 * @param minutes 分钟数
 * @returns 小时分钟字符串，如 "2小时30分钟"
 */
export function formatMinutesToHours(minutes: number): string {
	if (minutes <= 0) {
		return '0分钟'
	}
	
	const hours = Math.floor(minutes / 60)
	const remainingMinutes = minutes % 60
	
	if (hours === 0) {
		return `${remainingMinutes}分钟`
	} else if (remainingMinutes === 0) {
		return `${hours}小时`
	} else {
		return `${hours}小时${remainingMinutes}分钟`
	}
}

/**
 * 生成排课确认提示信息
 * @param unit 课程单位：1-按小时，2-按次，3-按月
 * @param scheduleCount 排课次数
 * @param consumeAmount 课消数量
 * @param totalMinutes 总分钟数（用于Unit=2时计算时长）
 * @returns 确认提示信息
 */
export function generateScheduleConfirmMessage(
	unit: number,
	scheduleCount: number,
	consumeAmount: number = 1,
	totalMinutes: number = 0
): string {
	let message = `即将排课${scheduleCount}次`
	
	if (unit === 3) {
		// Unit为3，提示：即将排课（x次）。是否确定？
		message = `即将排课（${scheduleCount}次）。确认排课吗？`
	} else if (unit === 1) {
		// Unit为1（按小时），提示：即将排课y（x次）。是否确定？
		const durationText = formatMinutesToHours(totalMinutes)
		message = `即将排课${durationText}（${scheduleCount}次）。确认排课吗？`
	} else {
		// Unit为2（按次）或其他，y可能不存在
		let extraInfo = ''
		if (consumeAmount > 1) {
			extraInfo = `，即将产生${consumeAmount * scheduleCount}次课消`
		}
		message = `即将排课${scheduleCount}次${extraInfo}。确认排课吗？`
	}
	
	return message
}

/**
 * 获取日期所在周的第一天（周一）
 * @param date 日期
 * @returns 该周周一的日期
 */
function getWeekStart(date: Date): Date {
	const d = new Date(date)
	const day = d.getDay() // 0=周日, 1=周一, ..., 6=周六
	const diff = day === 0 ? -6 : 1 - day // 如果是周日，往前6天；否则往前(day-1)天到周一
	d.setDate(d.getDate() + diff)
	d.setHours(0, 0, 0, 0)
	return d
}

/**
 * 计算从起始日期所在周开始，当前日期是第几周（从1开始）
 * @param currentDate 当前日期
 * @param startDate 起始日期
 * @returns 周数（1, 2, 3, ...）
 */
function getWeekNumber(currentDate: Date, startDate: Date): number {
	const startWeekStart = getWeekStart(startDate)
	const currentWeekStart = getWeekStart(currentDate)
	const daysDiff = Math.floor((currentWeekStart.getTime() - startWeekStart.getTime()) / (1000 * 60 * 60 * 24))
	const weekDiff = Math.floor(daysDiff / 7)
	return weekDiff + 1 // 从1开始计数
}

/**
 * 计算指定日期的排课信息（次数和时长）
 * @param currentDate 当前日期
 * @param startDate 开始日期（用于计算间隔）
 * @param courseMode 重复规则
 * @param planList 上课时段列表
 * @param isHoliday 是否跳过节假日
 * @param holidays 节假日列表
 * @returns { count: number, minutes: number } 排课次数和时长（分钟）
 */
export function calculateDayScheduleInfo(
	currentDate: Date,
	startDate: Date,
	courseMode: string,
	planList: any[],
	isHoliday: number,
	holidays: any[] = []
): { count: number; minutes: number } {
	// 检查是否为节假日
	let isHolidayDate = false
	if (isHoliday === 1 && holidays.length > 0) {
		isHolidayDate = holidays.some((holiday: any) => {
			const holidayDate = new Date(holiday.Date)
			return holidayDate.toDateString() === currentDate.toDateString()
		})
	}

	// 如果是节假日，返回0
	if (isHolidayDate) {
		return { count: 0, minutes: 0 }
	}

	let dayScheduleCount = 0
	let dayTotalMinutes = 0

	// 根据重复规则计算当天的排课信息
	switch (courseMode) {
		case 'Daily': // 每天重复
			// 每天重复：不考虑星期几，根据实际时段来算
			dayScheduleCount = planList.length
			dayTotalMinutes = planList.reduce((total: number, session: any) => {
				return total + calculateDuration([session.StartTime, session.EndTime])
			}, 0)
			break

		case 'AlternateDay': // 隔天重复
			const daysDiff = Math.floor((currentDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24))
			if (daysDiff % 2 === 0) {
				// 隔天重复：不考虑星期几，根据实际时段来算
				dayScheduleCount = planList.length
				dayTotalMinutes = planList.reduce((total: number, session: any) => {
					return total + calculateDuration([session.StartTime, session.EndTime])
				}, 0)
			}
			break

		case 'Weekly': // 每周重复
			const currentWeekday = currentDate.getDay() || 7
			const matchingSessions = planList.filter((session: any) => session.Weekday === currentWeekday)
			dayScheduleCount = matchingSessions.length
			dayTotalMinutes = matchingSessions.reduce((total: number, session: any) => {
				return total + calculateDuration([session.StartTime, session.EndTime])
			}, 0)
			break

		case 'Biweekly': // 隔周重复
			// 计算当前日期是第几周（从起始日期所在周开始，第1周、第2周...）
			// 第1周、第3周、第5周...排课（奇数周）
			// 第2周、第4周、第6周...跳过（偶数周）
			const weekNumber = getWeekNumber(currentDate, startDate)
			if (weekNumber % 2 === 1) {
				const currentWeekday = currentDate.getDay() || 7
				const matchingSessions = planList.filter((session: any) => session.Weekday === currentWeekday)
				dayScheduleCount = matchingSessions.length
				dayTotalMinutes = matchingSessions.reduce((total: number, session: any) => {
					return total + calculateDuration([session.StartTime, session.EndTime])
				}, 0)
			}
			break

		default:
			// 默认情况：检查星期几匹配
			if (planList.some((session: any) => session.Weekday)) {
				const currentWeekday = currentDate.getDay() || 7
				const matchingSessions = planList.filter((session: any) => session.Weekday === currentWeekday)
				dayScheduleCount = matchingSessions.length
				dayTotalMinutes = matchingSessions.reduce((total: number, session: any) => {
					return total + calculateDuration([session.StartTime, session.EndTime])
				}, 0)
			} else {
				dayScheduleCount = planList.length
				dayTotalMinutes = planList.reduce((total: number, session: any) => {
					return total + calculateDuration([session.StartTime, session.EndTime])
				}, 0)
			}
	}

	return { count: dayScheduleCount, minutes: dayTotalMinutes }
}

/**
 * 计算重复排课的总信息（次数和时长）
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @param courseMode 重复规则
 * @param planList 上课时段列表
 * @param isHoliday 是否跳过节假日
 * @param holidays 节假日列表
 * @param unit 排课单位：1-按小时，2-按次，3-按月
 * @param courseTimes 最多排多少（小时/次）
 * @returns { count: number, minutes: number } 总排课次数和时长（分钟）
 */
export function calculateRepeatedScheduleInfo(
	startDate: string,
	endDate: string,
	courseMode: string,
	planList: any[],
	isHoliday: number,
	holidays: any[] = [],
	unit: number = 1,
	courseTimes: number | string = 0
): { count: number; minutes: number } {
	const start = new Date(startDate)
	const end = new Date(endDate)

	if (start > end) {
		return { count: 0, minutes: 0 }
	}

	let totalCount = 0
	let totalMinutes = 0
	const maxCourseTimes = Number(courseTimes) || 0
	
	// 调试信息
	console.log(`calculateRepeatedScheduleInfo调试 - courseTimes: ${courseTimes}, maxCourseTimes: ${maxCourseTimes}`)

	// 遍历每一天，计算排课信息
	const currentDate = new Date(start)
	console.log(`开始遍历日期范围: ${start.toDateString()} 到 ${end.toDateString()}`)
	while (currentDate <= end) {
		console.log(`处理日期: ${currentDate.toDateString()}, 星期: ${currentDate.getDay()}`)
		const dayInfo = calculateDayScheduleInfo(
			currentDate,
			start,
			courseMode,
			planList,
			isHoliday,
			holidays
		)
		console.log(`当天排课信息: count=${dayInfo.count}, minutes=${dayInfo.minutes}`)


		// 如果当天没有排课（节假日或不符合重复规则），继续下一天
		if (dayInfo.count === 0) {
			console.log(`当天无排课，跳过`)
			currentDate.setDate(currentDate.getDate() + 1)
			continue
		}

		// 检查排课上限
		let canAddAll = true
		let actualCount = 0
		let actualMinutes = 0

		// 如果没有设置上限，直接使用当天的排课信息
		if (maxCourseTimes <= 0) {
			actualCount = dayInfo.count
			actualMinutes = dayInfo.minutes
			console.log(`无上限模式 - 当天排课: ${actualCount}, 累计: ${totalCount + actualCount}`)
		} else if (maxCourseTimes > 0) {
			if (unit === 1) {
				// 按小时排课：同时检查时长上限和次数上限
				const remainingMinutes = maxCourseTimes * 60 - totalMinutes
				if (remainingMinutes <= 0) {
					// 已经达到时长上限，停止排课
					break
				}

				// 计算当天可以排多少课（仅针对当天匹配的时段）
				let tempCount = 0
				let tempMinutes = 0
				// 根据当前日期与重复规则，获取当天匹配的时段列表
				const currentWeekday = (currentDate.getDay() || 7)
				const daysDiff = Math.floor((currentDate.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
				let daySessions: any[] = []
				switch (courseMode) {
					case 'Weekly':
						daySessions = planList.filter((s: any) => s.Weekday === currentWeekday)
						break
					case 'Biweekly':
						// 使用正确的周数计算：奇数周排课，偶数周跳过
						const weekNumber1 = getWeekNumber(currentDate, start)
						daySessions = (weekNumber1 % 2 === 1) ? planList.filter((s: any) => s.Weekday === currentWeekday) : []
						break
					case 'AlternateDay':
						daySessions = (daysDiff % 2 === 0) ? planList : []
						break
					case 'Daily':
						daySessions = planList
						break
					default:
						if (planList.some((s: any) => s.Weekday)) {
							daySessions = planList.filter((s: any) => s.Weekday === currentWeekday)
						} else {
							daySessions = planList
						}
				}
				
				// 按小时排课时，需要将小时数转换为次数限制
				// 计算每个时段的小时数，然后按顺序排课直到达到小时上限
				for (let i = 0; i < daySessions.length; i++) {
					const session = daySessions[i]
					const sessionMinutes = calculateDuration([session.StartTime, session.EndTime])
					const sessionHours = sessionMinutes / 60
					
					// 检查是否超出剩余小时数
					if (tempMinutes + sessionMinutes <= remainingMinutes) {
						tempCount++
						tempMinutes += sessionMinutes
					} else {
						canAddAll = false
						break
					}
				}
				
				actualCount = tempCount
				actualMinutes = tempMinutes
			} else if (unit === 2) {
				// 按次排课：检查次数上限
				if (totalCount >= maxCourseTimes) {
					// 已经达到次数上限，停止排课
					break
				}

				// 计算当天可以排多少课
				const remainingCount = maxCourseTimes - totalCount
				actualCount = Math.min(dayInfo.count, remainingCount)
				
				// 计算实际排课的时长（仅针对当天匹配的时段）
				const currentWeekday = (currentDate.getDay() || 7)
				const daysDiff = Math.floor((currentDate.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
				let daySessions: any[] = []
				switch (courseMode) {
					case 'Weekly':
						daySessions = planList.filter((s: any) => s.Weekday === currentWeekday)
						break
					case 'Biweekly':
						// 使用正确的周数计算：奇数周排课，偶数周跳过
						const weekNumber2 = getWeekNumber(currentDate, start)
						daySessions = (weekNumber2 % 2 === 1) ? planList.filter((s: any) => s.Weekday === currentWeekday) : []
						break
					case 'AlternateDay':
						daySessions = (daysDiff % 2 === 0) ? planList : []
						break
					case 'Daily':
						daySessions = planList
						break
					default:
						if (planList.some((s: any) => s.Weekday)) {
							daySessions = planList.filter((s: any) => s.Weekday === currentWeekday)
						} else {
							daySessions = planList
						}
				}
				for (let i = 0; i < actualCount && i < daySessions.length; i++) {
					const session = daySessions[i]
					actualMinutes += calculateDuration([session.StartTime, session.EndTime])
				}
			} else {
				// 按月排课或其他情况，按原逻辑
				actualCount = dayInfo.count
				actualMinutes = dayInfo.minutes
			}
		}

		// 累加实际的排课信息
		totalCount += actualCount
		totalMinutes += actualMinutes

		// 如果当天没有排满课，说明达到了上限，停止排课
		console.log(`检查退出条件: canAddAll=${canAddAll}, unit=${unit}, totalCount=${totalCount}, maxCourseTimes=${maxCourseTimes}`)
		if (!canAddAll || (unit === 2 && maxCourseTimes > 0 && totalCount >= maxCourseTimes)) {
			console.log(`满足退出条件，停止循环`)
			break
		}

		// 移动到下一天
		currentDate.setDate(currentDate.getDate() + 1)
		console.log(`移动到下一天: ${currentDate.toDateString()}`)
	}
	
	console.log(`循环结束，总排课数: ${totalCount}`)

	return { count: totalCount, minutes: totalMinutes }
}

//老师类别是否重复
export function checkTeacherType(d: any, other: any, assi: any[], main?: any) {
	// 深拷贝助教列表
	const teacherList = cloneDeep(assi);
	
	// 如果有主教师，添加到检查列表
	if (main && main.TeacherCommissionIDs) {
		teacherList.push(main);
	}
	
	// 收集所有已使用的类别ID
	const usedTypeIds: string[] = [];
	
	// 检查每个教师的类别
	for (const teacher of teacherList) {
		// 跳过当前正在添加的教师（如果存在）
		if (d && d.ID === teacher.ID) {
			continue;
		}
		
		// 获取教师的类别ID数组
		const teacherTypeIds = teacher.TeacherCommissionIDs ? teacher.TeacherCommissionIDs.split(',') : [];
		
		// 检查是否有类别冲突
		for (const typeId of teacherTypeIds) {
			if (other.ID && typeId === other.ID) {
				ElMessageBox.alert("同一类别只能设置给一个老师。", '提示', {
					confirmButtonText: '确认',
				});
				return false;
			}
			usedTypeIds.push(typeId);
		}
	}
	
	return true;
}

/**
 * 将时间字符串转换为分钟数
 * @param timeStr 时间字符串，格式为 "HH:MM"
 * @returns 分钟数
 */
export function timeToMinutes(timeStr: string): number {
	if (!timeStr) return 0;
	const [hours, minutes] = timeStr.split(':').map(Number);
	return hours * 60 + minutes;
}

/**
 * 检查时段是否重叠
 * @param timeRange1 第一个时段 [startTime, endTime]
 * @param timeRange2 第二个时段 [startTime, endTime]
 * @returns 是否重叠
 */
export function isTimeRangeOverlap(timeRange1: any[], timeRange2: any[]): boolean {
	if (!timeRange1 || !timeRange2 || timeRange1.length !== 2 || timeRange2.length !== 2) {
		return false;
	}
	
	const [start1, end1] = timeRange1;
	const [start2, end2] = timeRange2;
	
	// 转换为分钟进行比较
	const start1Minutes = timeToMinutes(start1);
	const end1Minutes = timeToMinutes(end1);
	const start2Minutes = timeToMinutes(start2);
	const end2Minutes = timeToMinutes(end2);
	
	// 检查是否重叠：一个时段的开始时间小于另一个时段的结束时间，且结束时间大于另一个时段的开始时间
	return start1Minutes < end2Minutes && end1Minutes > start2Minutes;
}

/**
 * 检查同一时段是否存在冲突
 * @param plan1 第一个排课计划
 * @param plan2 第二个排课计划
 * @param checkAssistantConflict 是否检查助教冲突
 * @returns 是否存在冲突
 */
export function hasConflictInSameTimeSlot(plan1: any, plan2: any, checkAssistantConflict: boolean = false): boolean {
	// 检查教室冲突
	if (plan1.ClassroomID && plan2.ClassroomID && plan1.ClassroomID === plan2.ClassroomID) {
		return true;
	}
	
	// 检查任课老师冲突
	if (plan1.Teachers && plan2.Teachers && plan1.Teachers.length > 0 && plan2.Teachers.length > 0) {
		for (const teacher1 of plan1.Teachers) {
			for (const teacher2 of plan2.Teachers) {
				if (teacher1.ID === teacher2.ID) {
					return true;
				}
			}
		}
	}
	
	// 如果业务规则CheckAssistantConflict为true，则助教也不能相同
	if (checkAssistantConflict && plan1.Assistants && plan2.Assistants) {
		for (const assistant1 of plan1.Assistants) {
			for (const assistant2 of plan2.Assistants) {
				if (assistant1.ID === assistant2.ID) {
					return true;
				}
			}
		}
	}
	
	return false;
}

/**
 * 检查排课计划列表中的冲突
 * @param planList 排课计划列表
 * @param courseMode 课程模式：Weekly-每周重复，Biweekly-隔周重复，Daily-每天重复，AlternateDay-隔天重复
 * @param planRule 排课规则：1-重复排课，0-自由排课
 * @param checkAssistantConflict 是否检查助教冲突
 * @returns 冲突状态数组，true表示该索引的计划存在冲突
 */
export function checkPlanListConflicts(
	planList: any[],
	courseMode: string,
	planRule: number,
	checkAssistantConflict: boolean = false
): boolean[] {
	const conflicts: boolean[] = [];
	
	for (let i = 0; i < planList.length; i++) {
		const currentPlan = planList[i];
		let hasConflict = false;
		
		// 检查与其他行的冲突
		for (let j = 0; j < planList.length; j++) {
			if (i === j) continue;
			
			const otherPlan = planList[j];
			
			// 检查时段是否重叠
			if (isTimeRangeOverlap(currentPlan.TimeRange, otherPlan.TimeRange)) {
				// 如果展示上课星期，需要检查星期是否相同
				const showWeekday = (courseMode === 'Weekly' || courseMode === 'Biweekly') && planRule === 1;
				
				if (showWeekday) {
					// 同一上课星期、同一时段，上课教室、任课老师不能相同
					if (currentPlan.Weekday === otherPlan.Weekday) {
						if (hasConflictInSameTimeSlot(currentPlan, otherPlan, checkAssistantConflict)) {
							hasConflict = true;
						}
					}
				} else {
					// 不展示上课星期，同一时段，上课教室、任课老师不能相同
					if (hasConflictInSameTimeSlot(currentPlan, otherPlan, checkAssistantConflict)) {
						hasConflict = true;
					}
				}
			}
		}
		
		conflicts.push(hasConflict);
	}
	
	return conflicts;
}