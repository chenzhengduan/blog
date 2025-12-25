import { dayjs } from "element-plus";
import { useConfigs, useLoginInfo } from '@/store'
import { computed } from "vue";
import { transToConfigDescript } from "./filters/filters";
// 导出列同步工具函数
export { syncUserColumns, getColumnDiff, validateColumns } from './column-sync'
export type { ColumnItem } from './column-sync'

export function exportFile(fileBolb: File, fileName: string) {
    const _fileName = `${fileName}_${dayjs(new Date()).format('YYYYMMDDHHmm')}`;
    const link = document.createElement("a");
    let blob = new Blob([fileBolb], { type: "application/vnd.ms-excel" });
    link.style.display = "none";
    link.href = URL.createObjectURL(blob);
    link.setAttribute("download", _fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

export interface IPageModel {
    PageIndex: number,
    PageSize: number,
    PageCount: number,
    TotalCount: number
}
export function assignPage(source: IPageModel, dest: IPageModel) {
    source.PageIndex = dest.PageIndex;
    source.PageCount = dest.PageCount;
    source.TotalCount = dest.TotalCount;
    if (dest.PageSize) {
        source.PageSize = dest.PageSize;
    }
}
export function checkPageIndex(pageIndex:number,totalCount:number,pageSize:number){
    if(pageIndex>1&&pageIndex>Math.ceil(totalCount/pageSize)){
        return Math.ceil(totalCount/pageSize)
    }
    return pageIndex
}

export const weekDiff = (num = 0) => {
    const now = new Date();
    // 默认从周一-周日
    let weekDay = now.getDay();
    weekDay == 0 && (weekDay = 7);

    // 本周一
    let monday = new Date();
    monday.setDate(monday.getDate() - weekDay + 1);

    let start = new Date(monday),
        end = new Date(monday);
    start.setDate(start.getDate() + num * 7);
    end.setDate(end.getDate() + (num + 1) * 7 - 1);

    return [start,end];
}

export const monthDiff = (num = 0) => {
    const now = new Date();
    // 默认从周一-周日
    let weekDay = now.getDay();
    weekDay == 0 && (weekDay = 7);

    // 本周一
    let monday = new Date();
    monday.setDate(monday.getDate() - weekDay + 1);

    let start = new Date(now),
        end = new Date(now);
    start.setDate(1);
    start.setMonth(start.getMonth() + num );

    end.setDate(1);
    end.setMonth(end.getMonth() + num + 1);
    end.setDate(0);

    return [start,end];
}

export const dateShortcuts = [
    {
        text: '今天',
        value: () => {
            const end = new Date()
            const start = new Date()
            return [start, end]
        },
    }, {
        text: '昨天',
        value: () => {
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 1)
            return [start, start]
        },
    },{
        text:'最近3天',
        value: () => {
            const end = new Date()
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 2)
            return [start, end]
        },
    },{
        text:'最近7天',
        value: () => {
            const end = new Date()
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
            return [start, end]
        },
    },{
        text:'最近半年',
        value:()=>{
            const end = new Date()
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 179)
            return [start, end]
        }
    },{
        text: '本周',
        value: () => {
            return weekDiff(0);
        },
    }, {
        text: '上周',
        value: () => {
            return weekDiff(-1);
        },
    }, {
        text: '本月',
        value: () => {
            return monthDiff(0);
        },
    },
    {
        text: '上月',
        value: () => {
            return monthDiff(-1);
        },
    },
]



/**
 * 将扁平化数据转换为树形结构
 * @param flatData 扁平化数据数组，每个元素包含ParentID属性表示父节点的ID
 * @param ParentID 当前层级的父节点ID，用于查找扁平化数据中对应的子节点
 * @returns 返回以树形结构组织的数据数组
 */
export function flatToTree(flatData: any, ParentID: any) {
    const tree = [];
  
    // 遍历flatData，找到parentId对应的子节点
    for (const node of flatData) {
      if (node.ParentID === ParentID) {
        // 递归查找子节点
        const children = flatToTree(flatData, node.ID);
        // 如果有子节点，则加入children属性中
        if (children.length > 0) {
          node.children = children;
        }
        // 加入tree中
        tree.push(node);
      }
    }
  
    return tree;
  }

export function initUuid(): string {
    const s: string[] = [];
    const hexDigits: string = "0123456789abcdef";

    for (let i = 0; i < 36; i++) {
        if (i === 14) {
            s[i] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
        } else if (i === 19) {
            s[i] = hexDigits.substr((Number(s[19]) & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
        } else {
            s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
        }
    }

    s[8] = s[13] = s[18] = s[23] = "-";

    return s.join("");
}

export function Form_valueAssignObj(dest:any,source:any){
	for(var i in source){
		if(dest.hasOwnProperty(i)){
			dest[i]=source[i];
		}
	}
}

export function downloadFile(url:string, filename:string, method: 'GET' | 'POST' = 'GET', data?: any) {
	getBlob(url, method, data).then(blob => {
		saveAs(blob, filename);
	});
	/**
	 * 获取 blob
	 * @param  {String} url 目标文件地址
	 * @param  {String} method 请求方法
	 * @param  {any} data 请求数据
	 * @return {Promise} 
	 */
	function getBlob(url:string, method: 'GET' | 'POST' = 'GET', data?: any) {
		return new Promise(resolve => {
			const xhr = new XMLHttpRequest();
			xhr.open(method, url, true);
			xhr.responseType = 'blob';
            // 添加必要的请求头
            xhr.setRequestHeader('wtwo-companyid', useLoginInfo().loginInfo.WTwo_CompanyID)
            xhr.setRequestHeader('wtwo-authtoken', useLoginInfo().loginInfo.WTwo_AuthToken)
            // 如果是POST请求，设置Content-Type
            if (method === 'POST') {
                xhr.setRequestHeader('Content-Type', 'application/json')
            }
			xhr.onload = () => {
				if (xhr.status === 200) {
					resolve(xhr.response);
				}
			};
			// 发送数据
			if (method === 'POST' && data) {
				xhr.send(JSON.stringify(data));
			} else {
				xhr.send();
			}
		});
	}
	/**
	 * 保存
	 * @param  {Blob} blob     
	 * @param  {String} filename 想要保存的文件名称
	 */
	function saveAs(blob:any, filename:string) {
		
			const link = document.createElement('a');
			const body = document.querySelector('body');

			link.href = window.URL.createObjectURL(blob);
			link.download = filename;

			// fix Firefox
			link.style.display = 'none';
			body?.appendChild(link);
			
			link.click();
			body?.removeChild(link);

			window.URL.revokeObjectURL(link.href);
		
	}
}

/*解决排课时，老师和课程的科目不一致的问题*/
export function Class_classifyTeachersBySubject(arr:any,allSubject?:any){
	let Fa:any={};
	/*emptyTeacher = {
			SubjectID: "",
			Teacher: "",
			TeacherID: "",
			$AssiTeacher: []
		}*/
        arr.forEach((item:any)=> {
		    let subID=item.SubjectID;
		    let sub=Fa[subID];
		    if(!sub){
			sub=Fa[subID]={
				SubjectID: subID,
				Teacher: "",
				TeacherID: "",
				TeacherCommissionIDs:"",
				TeacherCommissionName:"",
				$AssiTeacher: [],
				AssiTeacherHTML:"",
                Status: item.Status,
			}
		};
		if(item.Role==1){
			sub.TeacherID=item.ID;
			let tcN=item.TeacherCommissionName;	//老师类别名
			sub.Teacher=item.Name+(tcN?"（"+tcN+"）":"");
			sub.TeacherCommissionIDs=item.TeacherCommissionIDs;	//老师类别ID
			sub.TeacherCommissionName=tcN;
		}else{
			sub.$AssiTeacher.push(item);
			sub.AssiTeacherHTML+='<span><i class="icon-delete-red" sujectTeacher="delAssi" ></i><span sujectTeacher="signAssi">'
											+item.Name
			if(item.TeacherCommissionName!=""){
				sub.AssiTeacherHTML+='（'+item.TeacherCommissionName+'）';
			}
			sub.AssiTeacherHTML+='</span></span>';
		}
	});
	let SubjectArr:any=[],
		Teachers:any=[];
	for(let i in Fa){
		SubjectArr.push(i);
		Teachers.push(Fa[i]);
	};
	let subjectList:any=[], concatSubjectList:any = [];
	if(allSubject){
		subjectList=[];
		allSubject.forEach((item:any)=> {
			if(SubjectArr.indexOf(item.ID)!=-1){
				subjectList.push(item);
			} else {
                concatSubjectList.push(item);
            }
		});
        concatSubjectList = subjectList.concat(concatSubjectList);
	}
	return {
		SubjectArr:subjectList||SubjectArr,
		Teachers:Teachers,
		Fa:Fa,
        concatSubjectList: concatSubjectList
	}
}

//色值转换
export function getTint(hexColor: string, alpha = 0.3){
    try{
        if(!hexColor) return `rgba(72,147,255,${alpha})`
        let c = hexColor.trim()
        if(c.startsWith('#')) c = c.slice(1)
        if(c.length===3){ c = c.split('').map(ch=>ch+ch).join('') }
        const r = parseInt(c.substring(0,2),16)
        const g = parseInt(c.substring(2,4),16)
        const b = parseInt(c.substring(4,6),16)
        if([r,g,b].some(n=>Number.isNaN(n))) return `rgba(72,147,255,${alpha})`
        return `rgba(${r},${g},${b},${alpha})`
    }catch{
        return `rgba(72,147,255,${alpha})`
    }
}

/**
 * 生成 HSB 色板（H: 0-360 步长5；S: 75-95 步长1；B: 80-95 步长1）
 * 返回十六进制颜色列表（不含透明度）
 */
export function buildHSBColorPalette(): string[] {
	const colors: string[] = []
	for (let h = 0; h <= 360; h += 5) {
		for (let s = 75; s <= 95; s += 1) {
			for (let b = 80; b <= 95; b += 1) {
				colors.push(hsbToHex(h, s / 100, b / 100))
			}
		}
	}
	return colors
}

/** 将 HSB/HSV 转为 HEX */
function hsbToHex(h: number, s: number, v: number): string {
	const c = v * s
	const hh = (h % 360) / 60
	const x = c * (1 - Math.abs((hh % 2) - 1))
	let r1 = 0, g1 = 0, b1 = 0
	if (hh >= 0 && hh < 1) { r1 = c; g1 = x; b1 = 0 }
	else if (hh >= 1 && hh < 2) { r1 = x; g1 = c; b1 = 0 }
	else if (hh >= 2 && hh < 3) { r1 = 0; g1 = c; b1 = x }
	else if (hh >= 3 && hh < 4) { r1 = 0; g1 = x; b1 = c }
	else if (hh >= 4 && hh < 5) { r1 = x; g1 = 0; b1 = c }
	else { r1 = c; g1 = 0; b1 = x }
	const m = v - c
	const r = Math.round((r1 + m) * 255)
	const g = Math.round((g1 + m) * 255)
	const b = Math.round((b1 + m) * 255)
	return '#' + [r, g, b].map(n => n.toString(16).padStart(2, '0')).join('')
}

/**
 * 稳定哈希：将任意字符串映射为非负整数
 */
export function stableStringHash(input: string): number {
	let hash = 2166136261 >>> 0 // FNV-1a 起始值
	for (let i = 0; i < input.length; i++) {
		hash ^= input.charCodeAt(i)
		hash = Math.imul(hash, 16777619) >>> 0
	}
	return hash >>> 0
}

/**
 * 将 key 映射到色板中的某个颜色（稳定、可复现）
 */
export function mapKeyToColorHex(key: string, palette?: string[]): string {
	const colors = palette && palette.length > 0 ? palette : defaultHSBPalette
	const idx = stableStringHash(key) % colors.length
	return colors[idx]
}

/**
 * 根据偏好类型返回背景色
 * 同一任课老师/课程/教室将映射到同一背景色
 * @param type ByTeacher | ByCourse | ByClassroom
 * @param ids { teacherId?, courseId?, classroomId? }
 * @param extraKey 可选的额外区分维度（例如课表类型）
 */
export function getBackgroundColorByPreference(
	type: 'ByTeacher' | 'ByCourse' | 'ByClassroom',
	ids: { TeacherID?: string | number; ShiftID?: string | number; ClassroomID?: string | number },
	extraKey: string = ''
): string {
	let key = ''
	switch (type) {
		case 'ByTeacher':
			key = `teacher:${ids.TeacherID ?? ''}`
			break
		case 'ByCourse':
			key = `course:${ids.ShiftID ?? ''}`
			break
		case 'ByClassroom':
			key = `classroom:${ids.ClassroomID ?? ''}`
			break
		default:
			key = 'default'
	}
	const context = extraKey ? `${extraKey}|${type}` : type
	return pickUniqueColorHex(context, key)
}

// 默认全局色板（懒加载一次）
let defaultHSBPalette: string[] = []
function ensureDefaultPaletteReady() {
	if (defaultHSBPalette.length === 0) {
		defaultHSBPalette = buildHSBColorPalette()
	}
}
ensureDefaultPaletteReady()

// 在相同上下文内尽量避免不同对象出现相同颜色
type UniqueColorContext = {
	keyToIndex: Map<string, number>
	usedIndices: Set<number>
}
const uniqueColorRegistry: Map<string, UniqueColorContext> = new Map()

function getOrCreateUniqueContext(context: string): UniqueColorContext {
	let ctx = uniqueColorRegistry.get(context)
	if (!ctx) {
		ctx = { keyToIndex: new Map(), usedIndices: new Set() }
		uniqueColorRegistry.set(context, ctx)
	}
	return ctx
}

function pickUniqueColorHex(context: string, key: string): string {
	ensureDefaultPaletteReady()
	const colors = defaultHSBPalette
	const ctx = getOrCreateUniqueContext(context)
	// 已分配过的 key 保持稳定
	const existed = ctx.keyToIndex.get(key)
	if (existed != null) return colors[existed]
	// 基准索引
	let idx = stableStringHash(`${context}|${key}`) % colors.length
	if (!ctx.usedIndices.has(idx)) {
		ctx.usedIndices.add(idx)
		ctx.keyToIndex.set(key, idx)
		return colors[idx]
	}
	// 线性探测（使用质数步长，分散碰撞）
	const step = 97 // 质数步长，需小于色板长度
	const maxProbe = Math.min(colors.length, 1024)
	for (let i = 1; i < maxProbe; i++) {
		const probeIdx = (idx + i * step) % colors.length
		if (!ctx.usedIndices.has(probeIdx)) {
			ctx.usedIndices.add(probeIdx)
			ctx.keyToIndex.set(key, probeIdx)
			return colors[probeIdx]
		}
	}
	// 若色板被占满或未找到空位，退回基准颜色
	ctx.keyToIndex.set(key, idx)
	return colors[idx]
}

/**
 * 从课程项中提取与配色相关的标识
 */
export function extractPreferenceColorIds(item: any): { TeacherID?: string | number; ShiftID?: string | number; ClassroomID?: string | number } {
    return {
        TeacherID: item?.TeacherID,
        ShiftID: item?.ShiftID,
        ClassroomID: item?.ClassroomID,
    }
}

/**
 * 根据偏好设置类型解析用于明细色值映射的枚举键
 */
export function resolveEnumKeyByPreference(settingType: string | undefined, item: any): string | null {
    if (!settingType) return null
    if (settingType === 'ByCourseFinished') {
        const v = Number(item?.Finished)
        return Number.isNaN(v) ? '0' : String(v)
    }
    if (settingType === 'ByOpeningStatus') {
        const v = Number(item?.CourseStatus)
        return Number.isNaN(v) ? '1' : String(v)
    }
    if (settingType === 'ByTeachingMethod') {
        const oneToOne = Number(item?.IsOneToOne)
        if (oneToOne === 1) return '1'
        const oneToMany = Number(item?.IsOneToMany)
        if (oneToMany === 1) return '2'
        return '0'
    }
    return null
}

/**
 * 通用：根据偏好或完成状态返回颜色（可复用）
 * @param item 课程数据项
 * @param settingType 偏好设置类型，如 ByTeacher/ByCourse/ByClassroom/ByCourseFinished 等
 * @param colorDetailMap 明细枚举到颜色的映射表
 * @param timetableType 课表类型区分键，默认 TimeTimetable
 */
export function getCourseStatusColor(
    item: any,
    settingType?: string,
    colorDetailMap?: Record<string, string>,
    timetableType: string = 'TimeTimetable'
): string {
    if (settingType) {
        if (settingType === 'ByTeacher' || settingType === 'ByCourse' || settingType === 'ByClassroom') {
            return getBackgroundColorByPreference(settingType as any, extractPreferenceColorIds(item), timetableType)
        }
        const key = resolveEnumKeyByPreference(settingType, item)
        if (key != null && colorDetailMap && colorDetailMap[key]) return colorDetailMap[key]
    }
    const status = Number(item?.Finished)
    if (status === 1) return '#67C23A'
    if (status === 2) return '#909399'
    return item?.Color || item?.color || '#4893FF'
}

/**
 * 从偏好中获取启用的右侧标签（最多3个）
 */
export function getEnabledRightTagsFromPreference(preferenceVm: any): any[] {
    const settings = (preferenceVm?.CardShowInformationSettings || []) as any[]
    return settings
        .filter(s => s && s.FieldType === (preferenceVm?.FieldTypeEnum?.Tag ?? 'Tag') && s.IsEnabled)
        .sort((a, b) => (a.SortOrder || 0) - (b.SortOrder || 0))
        .slice(0, 3)
}

/**
 * 从偏好中获取启用的左侧主信息字段
 */
export function getEnabledLeftFieldsFromPreference(preferenceVm: any): any[] {
    const settings = (preferenceVm?.CardShowInformationSettings || []) as any[]
    // FieldTypeEnum 在运行时不可用时，按业务固定值 'Main'
    const mainEnum = preferenceVm?.FieldTypeEnum?.Main ?? 'Main'
    return settings
        .filter(s => s && s.FieldType === (mainEnum as any) && s.IsEnabled && !s.IsDefault)
        .sort((a, b) => (a.SortOrder || 0) - (b.SortOrder || 0))
}

/**
 * 统一的课程字段取值
 */
const configs = computed(() => {
	return useConfigs().configs
})
const ShowClassProgressBar=computed(()=>{//在课表上是否显示动态计算出来的上课进度（第几次课）：1显示，0不显示（默认）2小银星定制（在补课及请假管理界面，给学员安排跟班补课或请假时，如果开启了此配置，则可以显示目标补课班级和请假班级的上课进度。）
	return configs.value.ShowClassProgressBar==1||configs.value.ShowClassProgressBar==2
})
export function getCourseFieldValue(item: any, fieldName: string): string {
    switch (fieldName) {
        case 'ShiftName':
            return item?.ShiftName || ''
        case 'ClassName':
        case 'StudentName':
            return item?.ClassName || item?.StudentName || ''
        case 'IsOneToOne': {
            const v = Number(item?.IsOneToOne)
            return v === 1 ? '一对一' : v === 2 ? '一对多' : '集体班'
        }
        case 'CampusName':
            return item?.CampusName || '-'
        case 'Duration':
            return item?.Duration ? `${item.Duration}` : ''
        case 'Finished':
        case 'FinishedName': {
            const v = Number(item?.Finished)
            return transToConfigDescript(v === 1 ? '已上课' : v === 2 ? '已取消' : '未上课')
        }
        case 'ClassPlanNum_ClassPlanCount':
            return ShowClassProgressBar.value&&item.ClassPlanNum_ClassPlanCount&&item.ClassPlanNum_ClassPlanCount!='0'?item.ClassPlanNum_ClassPlanCount:''
        case 'ChapterTitle':
            return item?.ChapterTitle || ''
        case 'CourseContent':
            return item?.CourseContent || ''
        case 'SubjectName':
            return item?.SubjectName || ''
        case 'CourseType':
            return item.CourseType==2?'线上课':'线下课'
        case 'ClassroomName':
            return item?.ClassroomName || ''
        case 'TeacherName':
            return item?.TeacherName || ''
        case 'AssistantTeacherName':
            return item?.AssistantTeacherName || ''
        case 'MasterName':
            return item?.MasterName || ''
        case 'HeadMasterUserName':
            return item?.HeadMasterUserName || ''
        case 'IsSubscribeCourse': {
            const v = Number(item?.IsSubscribeCourse)
            return item.CourseMethod==20?'':(v === 1 ? '已开放预约' : '不开放预约')
        }
        case 'StudentAttendanceCount':
            return (item.Finished&&item.Finished==1 ? `${item.StudentAttendanceCount}` : '-')+'/'+item.StudentCount
        case 'CourseStatus':
            return item.CourseMethod==30?(item.CourseStatus==1?'已开课':'未开课'):''
        case 'StartStudentCount':
            return item.CourseMethod==30?item.StartStudentCount:'-'
        case 'InternalRemark':
            return item?.InternalRemark || ''
        case 'Describe':
            return item?.Describe || ''
        default:
            return item?.[fieldName] ?? ''
    }
}

/**
 * 统一的课程标签文案
 */
export function getCourseTagText(item: any, fieldName: string): string {
    switch (fieldName) {
        case 'Finished': {
            const v = Number(item?.Finished)
            return transToConfigDescript(v === 1 ? '已上课' : v === 2 ? '已取消' : '未上课')
        }
        case 'IsSubscribeCourse': {
            const v = Number(item?.IsSubscribeCourse)
            return v === 1 ? '约课' : ''
        }
        case 'IsOneToOne': {
            const v = Number(item?.IsOneToOne)
            return v === 1 ? '一对一' : v === 2 ? '一对多' : '集体班'
        }
        case 'TeacherName':
            return item?.TeacherName || item?.Teacher || ''
        case 'ClassroomName':
            return item?.ClassroomName || ''
        case 'ClassPlanNum_ClassPlanCount':
            return ShowClassProgressBar.value&&item.ClassPlanNum_ClassPlanCount&&item.ClassPlanNum_ClassPlanCount!=0?item.ClassPlanNum_ClassPlanCount:''
        case 'CourseType':
            return item.CourseType==2?'线上课':'线下课'
        case 'CourseStatus':
            return item.CourseMethod==30?(item.CourseStatus==1?'已开课':'未开课'):''
        default:
            return item?.[fieldName] ?? ''
    }
}

/**
 * 从偏好中提取 Tag 颜色映射
 */
export function getTagColorMapFromPreference(preferenceVm: any): Record<string, string> {
    const list = (preferenceVm?.TagColorSettings || []) as any[]
    const map: Record<string, string> = {}
    if (Array.isArray(list)) {
        list.forEach((t: any) => {
            if (t && t.TagFieldName && t.ColorValue) map[t.TagFieldName] = t.ColorValue
        })
    }
    return map
}

/** 默认标签颜色表 */
export const DEFAULT_COURSE_TAG_COLORS: Record<string, string> = {
    Finished: '#909399',
    IsSubscribeCourse: '#909399',
    IsOneToOne: '#909399',
    TeacherName: '#909399',
    ClassroomName: '#909399',
    ClassPlanNum_ClassPlanCount: '#909399',
    CourseType: '#909399',
    CourseStatus: '#909399',
}

/**
 * 统一获取课程标签颜色，优先偏好映射，其次默认颜色，最后退回主色
 */
export function getCourseTagColor(
    fieldName: string,
    preferenceVm: any,
    fallbackMap: Record<string, string> = DEFAULT_COURSE_TAG_COLORS,
    defaultColor: string = '#409EFF'
): string {
    const map = getTagColorMapFromPreference(preferenceVm)
    return map[fieldName] || fallbackMap[fieldName] || defaultColor
}

/**
 * 统一从 ALL_COLUMNS 计算导出列（去重后的 ColumnKey 列表）
 */
export function buildExportColumns(allColumns: any[]): string[] {
    const keys = (allColumns as unknown as any[])
        .map(c => String((c as any)?.ColumnKey || ''))
        .flatMap(s => s.split(',').map(it => it.trim()))
        .filter(Boolean)
    return Array.from(new Set(keys))
}

/**
 * 从 ColorDetails 数组构建 enumValue -> colorValue 的映射
 */
export function buildColorDetailMap(details: any[] | undefined | null): Record<string, string> {
    const map: Record<string, string> = {}
    const list = Array.isArray(details) ? details : []
    list.forEach((d: any) => {
        if (d && d.EnumValue != null && d.ColorValue) map[String(d.EnumValue)] = d.ColorValue
    })
    return map
}

/**
 * 获取姓名（或字符串）的首字母大写
 */
export function getNameInitial(name: string | undefined): string {
    const n = (name || '').trim();
    if (!n) return '';
    return n.charAt(0).toUpperCase();
}