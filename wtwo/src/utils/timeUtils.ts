/**
 * 时间工具函数
 */

/**
 * 计算上课时段的Duration（分钟数）
 * @param timeRange 时间范围数组 [startTime, endTime]
 * @returns 分钟数
 */
export function calculateDuration(timeRange: [string, string] |[]| null): number {
	if (!timeRange || !Array.isArray(timeRange) || timeRange.length !== 2) {
		return 0
	}
	
	const [startTime, endTime] = timeRange
	
	if (!startTime || !endTime) {
		return 0
	}
	
	// 解析时间字符串 HH:mm
	const parseTime = (timeStr: string): number => {
		const [hours, minutes] = timeStr.split(':').map(Number)
		return hours * 60 + minutes
	}
	
	const startMinutes = parseTime(startTime)
	const endMinutes = parseTime(endTime)
	
	// 如果结束时间小于开始时间，说明跨天，需要加上24小时
	if (endMinutes < startMinutes) {
		return (endMinutes + 24 * 60) - startMinutes
	}
	
	return endMinutes - startMinutes
}

/**
 * 格式化Duration为可读的字符串
 * @param duration 分钟数
 * @param alwaysShowMinutes 是否总是显示分钟，默认为false
 * @returns 格式化的字符串，如 "90分钟" 或 "1小时30分钟"
 */
export function formatDuration(duration: number, alwaysShowMinutes: boolean = false): string {
	if (duration <= 0) {
		return '0分钟'
	}
	
	// 如果配置为总是显示分钟，直接返回分钟数
	if (alwaysShowMinutes) {
		return `${duration}分钟`
	}
	
	const hours = Math.floor(duration / 60)
	const minutes = duration % 60
	
	if (hours === 0) {
		return `${minutes}分钟`
	} else if (minutes === 0) {
		return `${hours}小时`
	} else {
		return `${hours}小时${minutes}分钟`
	}
}

/**
 * 验证时间格式是否正确 (HH:mm)
 * @param timeStr 时间字符串
 * @returns 是否有效
 */
export function isValidTimeFormat(timeStr: string): boolean {
	const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/
	return timeRegex.test(timeStr)
}

/**
 * 检查时间范围是否有效
 * @param timeRange 时间范围数组 [startTime, endTime]
 * @returns 是否有效
 */
export function isValidTimeRange(timeRange: [string, string] | null): boolean {
	if (!timeRange || !Array.isArray(timeRange) || timeRange.length !== 2) {
		return false
	}
	
	const [startTime, endTime] = timeRange
	
	if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime)) {
		return false
	}
	
	// 检查开始时间是否小于结束时间（允许跨天）
	const startMinutes = parseTimeToMinutes(startTime)
	const endMinutes = parseTimeToMinutes(endTime)
	
	// 如果结束时间小于开始时间，说明跨天，这是允许的
	return true
}

/**
 * 将时间字符串转换为分钟数
 * @param timeStr 时间字符串 (HH:mm)
 * @returns 分钟数
 */
function parseTimeToMinutes(timeStr: string): number {
	const [hours, minutes] = timeStr.split(':').map(Number)
	return hours * 60 + minutes
} 