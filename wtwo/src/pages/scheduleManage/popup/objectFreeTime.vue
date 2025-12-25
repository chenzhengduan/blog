<template>
    <!-- 查忙闲 -->
    <el-dialog 
        v-model="dialogVisible" 
        title="查忙闲" 
        width="1160px"
        :close-on-click-modal="false" 
        :append-to-body="true"
        :destroy-on-close="true" 
        :align-center="true" 
        class="objectFreeTime" 
        draggable
        @close="close" 
    >
        <div class="box-wrapper-table">
            <div class="fixed-table-box">
                <div class="modal-filter">
                    <div class="filter-item w-450px!">
                        <!-- 最多选30天 -->
                        <multiple-dates-picker
                            v-model="dateList"
                            :clearable="false"
                            :title="transToConfigDescript('上课日期')"
                            :max-days="30"
                        />
                    </div>
                    <div class="filter-item">
                        <CourseTimeRange
                            v-model="timeRange"
                            :clearable="false"
                            :campusId="currentCampusesIds.length==1?currentCampusesIds[0]:''"
                            :title="transToConfigDescript('上课时间')"
                            class="w-360px!"
                            pickerWidth="95px"
                        />
                    </div>
                    <div class="filter-item">
                        <el-button type="primary" @click="funcQuery" :disabled="loading">查询</el-button>
                    </div>
                </div>
            </div>
            <div class="flex-between pb-10px">
                <div class="flex-center">
                    <div class="text-15px font-weight-500 color-#303133">查询结果</div>
                    <el-divider direction="vertical"/>
                    <page-attention-tips>
                        <!-- → ④ 选择信息进行快捷排课 -->
                        <div class="color-#999"> {{transToConfigDescript('① 选择上课日期与上课时间。 → ② 添加查询内容。 → ③ 点击查询')}}</div>
                    </page-attention-tips>
                </div>
                
                <span class="switch-wrap">
                    <el-switch
                        v-model="showDetail"
                        :active-value="1"
                        :inactive-value="0"
                        size="small"
                    ></el-switch>
                    <span class="switch-title">显示明细</span>
                </span>
            </div>
            <div class="table-wrap" v-loading="loading" ref="tableContainerRef">
                <el-table ref="customTable" 
                    :data='list'
                    style="width: 100%;"  
                    :max-height="400">
                    <template #empty>
                        <el-empty :image="globalData.emptyPng" :image-size="100" :description="transToConfigDescript('请添加老师、教室、班级、学员内容查询忙闲')">
                            <el-dropdown trigger="click">
                                <el-button type="primary" :icon="CirclePlus">添加查询内容</el-button>
                                <template #dropdown>
                                    <el-dropdown-menu class="w-136px!" placement="bottom-start">
                                        <el-dropdown-item @click.native="selectTeacher">{{transToConfigDescript('老师')}}</el-dropdown-item>
                                        <el-dropdown-item @click.native="selectClassroom">教室</el-dropdown-item>
                                        <el-dropdown-item @click.native="selectStudent">学员</el-dropdown-item>
                                        <el-dropdown-item @click.native="selectClass">{{transToConfigDescript('班级')}}</el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
                        </el-empty>
                    </template>
                    <el-table-column fixed="left" prop="Name" label="名称" width="130px" show-overflow-tooltip></el-table-column>
                    <el-table-column fixed="left" prop="Type" label="类型" width="60">
                        <template #default="scope">{{ transToConfigDescript(scope.row.Type) }}</template>
                    </el-table-column>
                    <el-table-column 
                        v-for="date in dateList" 
                        :key="date"
                        align="center"
                        width="120">
                        <template #header>
                            <div class="date-header">
                                <span class="date-label">{{ formatDateLabel(date) }}</span>
                                <span class="week-label">{{ formatWeek(date) }}</span>
                            </div>
                        </template>
                        <template #default="{ row }">
                            <template v-if="!row.__queried">
                                <div class="arrange-status-label empty-label">-</div>
                            </template>
                            <template v-else>
                                <template v-if="showDetail===0">
                                    <div :class="['arrange-status-label', row.__busy && row.__busy[date] ? 'busy' : 'free']">
                                        {{ row.__busy && row.__busy[date] ? '忙' : '闲' }}
                                    </div>
                                </template>
                                <template v-else>
                                    <div :class="['arrange-status-label', row.__busy && row.__busy[date] ? 'busy' : 'free']">
                                        {{ row.__busy && row.__busy[date] ? '忙' : '闲' }}
                                    </div>
                                    <div v-if="row.__slots && row.__slots[date] && row.__slots[date].length" class="text-center mt-4px!">
                                        <div 
                                            v-for="(timeSlot, idx) in row.__slots[date]" 
                                            :key="timeSlot + '_' + idx" 
                                            :class="['detail-slot', row.__slotOverlaps && row.__slotOverlaps[date] && row.__slotOverlaps[date][idx] ? 'busy' : '']"
                                        >
                                            {{ timeSlot }}
                                        </div>
                                    </div>
                                </template>
                            </template>
                        </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" min-width="60">
                        <template #default="{ row }">
                            <el-link type="primary" underline="never" @click="handleRemove(row)">移除</el-link>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div v-if="list.length" class="mt-16px">
                <el-dropdown trigger="click">
                    <el-button type="primary" plain :icon="CirclePlus">添加查询内容</el-button>
                    <template #dropdown>
                        <el-dropdown-menu class="w-136px!" placement="bottom-start">
                            <el-dropdown-item @click.native="selectTeacher">{{transToConfigDescript('老师')}}</el-dropdown-item>
                            <el-dropdown-item @click.native="selectClassroom">教室</el-dropdown-item>
                            <el-dropdown-item @click.native="selectStudent">学员</el-dropdown-item>
                            <el-dropdown-item @click.native="selectClass">{{transToConfigDescript('班级')}}</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </div>
        <template #footer>
            <div class="flex-end">
                <div>
                    <el-button plain @click="close">关闭</el-button>
                </div>
            </div>
        </template>
    </el-dialog>
    <chooseEmpTable ref="chooseEmpTableRef"></chooseEmpTable>
    <chooseClassroom ref="chooseClassroomRef"></chooseClassroom>
    <chooseStudent ref="chooseStudentRef"></chooseStudent>
    <chooseClass ref="chooseClassRef"></chooseClass>
</template>
<script lang="ts" setup>
import { useCurrentCampuses } from '@/store'
import { computed, getCurrentInstance, ref, watch } from 'vue'
import { dayjs } from 'element-plus'
import { ElMessage } from 'element-plus'
import MultipleDatesPicker from '../components/multiple-dates-picker.vue'
import CourseTimeRange from '../components/course-time-range.vue'
import { queryFree } from '@/api/arrange'
import { CirclePlus } from '@element-plus/icons-vue'
import { transToConfigDescript } from '@/utils/filters/filters'

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;

const currentCampusesIds=computed(()=>{
    return useCurrentCampuses().campusList?useCurrentCampuses().campusList.split(','):[]
})

const loading = ref(false)

const dialogVisible = ref(false)
const list = ref<any[]>([])

const dateList = ref<string[]>([])
const timeRange = ref([] as any)
const showDetail = ref(0)
const queried = ref(false)

// 始终按日期升序排列（并去重），以便表头列按日期顺序显示
const sortDates = (arr: string[]) => {
	const unique = Array.from(new Set(Array.isArray(arr) ? arr : []))
	return unique.sort((a, b) => dayjs(a).valueOf() - dayjs(b).valueOf())
}
watch(dateList, (newVal) => {
	const sorted = sortDates(newVal || [])
	// 避免不必要的赋值导致的 watch 循环
	if (JSON.stringify(sorted) !== JSON.stringify(newVal)) {
		dateList.value = sorted
	}
}, { deep: false })

function getCurrentWeekDates(){
    const startOfWeek = dayjs().startOf('week')
    const dates:string[] = []
    for(let i=0;i<7;i++){
        dates.push(startOfWeek.add(i,'day').format('YYYY-MM-DD'))
    }
    return dates
}

// 表头：日期与周几分离，便于样式控制
function formatDateLabel(date: string) {
    return dayjs(date).format('MM-DD')
}
function formatWeek(date: string) {
    const weekDays = ['日', '一', '二', '三', '四', '五', '六']
    return `周${weekDays[dayjs(date).day()]}`
}
// 合并选择结果：已存在的不动，不存在的添加，并标记类别
function mergeList(incomingList: any[], typeLabel: string) {
    const safeIncoming = Array.isArray(incomingList) ? incomingList : []
    const existingKeySet = new Set(
        (list.value as any[]).map((item: any) => `${item.Type}|${item.ID}`)
    )
    const additions = safeIncoming
        .map((item: any) => ({ ...item, Type: typeLabel }))
        .filter((item: any) => !existingKeySet.has(`${item.Type}|${item.ID}`))
    if (additions.length > 0) {
        additions.forEach((it:any)=>{
            it.__queried = false
        })
        list.value = (list.value as any[]).concat(additions)
    }
    console.log(list.value)
}
const chooseEmpTableRef = ref()
function selectTeacher(){
    chooseEmpTableRef.value.open({
        multi: true,
        choosed: []
    }).then((res:any)=>{
        mergeList(res?.data || [], '老师')
    })
}
const chooseClassroomRef = ref()
function selectClassroom(){
    chooseClassroomRef.value.open({
        multi: true,
        selected: [],
    }).then((res:any)=>{
        mergeList(res?.data || [], '教室')
    })
}
const chooseStudentRef = ref()
function selectStudent(){
    chooseStudentRef.value.open({
        multi: true,
        choosed: [],
        hideDrop:true,
        hideQuit:true,
    }).then((res:any)=>{
        mergeList(res?.data || [], '学员')
    })
}
const chooseClassRef = ref()
function selectClass(){
    chooseClassRef.value.open({
        multi: true,
        selected: [],
        condition:{
            shiftType:6
        }
    }).then((res:any)=>{
        mergeList(res?.data || [], '班级')
    })
}
function handleRemove(row:any){
    list.value = list.value.filter((item:any)=>item.ID !== row.ID)
}
function funcQuery(){
    if(!Array.isArray(timeRange.value) || timeRange.value.length!==2 || !timeRange.value[0] || !timeRange.value[1]){
        ElMessage.warning(transToConfigDescript('请先选择上课时间'))
        return
    }
    // 校验时间范围：结束时间必须大于开始时间
    const [rangeStart, rangeEnd] = timeRange.value as [string,string]
    const toMin = (hhmm:string)=>{
        const [h,m] = (hhmm||'0:0').split(':').map(n=>parseInt(n,10)||0)
        return h*60+m
    }
    if (toMin(rangeEnd) <= toMin(rangeStart)){
        ElMessage.warning('结束时间必须大于开始时间')
        return
    }
    if(!Array.isArray(dateList.value) || dateList.value.length===0){
        ElMessage.warning(transToConfigDescript('请先选择上课日期'))
        return
    }
    const teacherIds = (list.value || []).filter((x:any)=>x.Type==='老师').map((x:any)=>x.ID)
    const classroomIds = (list.value || []).filter((x:any)=>x.Type==='教室').map((x:any)=>x.ID)
    const studentIds = (list.value || []).filter((x:any)=>x.Type==='学员').map((x:any)=>x.ID)
    const classIds = (list.value || []).filter((x:any)=>x.Type==='班级').map((x:any)=>x.ID)

    if (!teacherIds.length && !classroomIds.length && !studentIds.length && !classIds.length) {
        ElMessage.warning(transToConfigDescript('请先添加查询对象（老师/教室/学员/班级）'))
        return
    }

    loading.value = true
    queryFree({
        DateList: dateList.value,
        TeacherIDList: teacherIds,
        ClassRoomIDList: classroomIds,
        StudentIDList: studentIds,
        ClassIDList: classIds
    }).then((res:any)=>{
        try{
            const data = res.Data || {}
            // 预先构建索引：按类型分类 API 列表
            const typeToKey = {
                '老师':'TeacherList',
                '教室':'ClassRoomList',
                '学员':'StudentList',
                '班级':'ClassList'
            } as Record<string,string>

            // 将每一行挂载容器
            const mapById: Record<string, any> = {}
            ;(list.value || []).forEach((row:any)=>{
                // 每次查询前重置（避免上一次结果残留）
                row.__busy = {}
                row.__slots = {}
                row.__slotOverlaps = {}
                row.__queried = false
                mapById[`${row.Type}|${row.ID}`] = row
            })

            // 工具：判断时间是否重叠，timeRange 为 [HH:mm, HH:mm]
            const hasOverlap = (slotStart:string, slotEnd:string, range:[string,string])=>{
                if(!range || !Array.isArray(range) || range.length!==2) return false
                const [rStart, rEnd] = range
                const toMin = (hhmm:string)=>{
                    const [h,m] = (hhmm||'0:0').split(':').map(n=>parseInt(n,10)||0)
                    return h*60+m
                }
                const s = toMin(slotStart)
                const e = toMin(slotEnd)
                const rs = toMin(rStart)
                const re = toMin(rEnd)
                return Math.max(s, rs) < Math.min(e, re)
            }

            const getDateStr = (dt:string)=>{
                if(!dt) return ''
                return dt.length>=10 ? dt.slice(0,10) : ''
            }
            const getTimeHHmm = (dt:string)=>{
                if(!dt) return ''
                if(dt.length>=16 && dt.includes(' ')) return dt.slice(11,16)
                // 若已是 HH:mm:ss 或 HH:mm
                if(dt.length>=5 && dt.includes(':')) return dt.slice(0,5)
                return ''
            }

            // 遍历四类对象
            Object.keys(typeToKey).forEach((typeLabel)=>{
                const listKey = typeToKey[typeLabel]
                const arr = data?.[listKey] || []
                ;(arr as any[]).forEach((item:any)=>{
                    const row = mapById[`${typeLabel}|${item.ID}`]
                    if(!row) return
                    const date = getDateStr(item.StartTime || item.EndTime || '')
                    if(!date || dateList.value.indexOf(date)===-1) return
                    const start = getTimeHHmm(item.StartTime)
                    const end = getTimeHHmm(item.EndTime)
                    if(!start || !end) return
                    if((row.__slots as any)[date] === undefined) (row.__slots as any)[date] = []
                    if((row.__slotOverlaps as any)[date] === undefined) (row.__slotOverlaps as any)[date] = []
                    if((row.__busy as any)[date] === undefined) (row.__busy as any)[date] = false
                    const idx = (row.__slots as any)[date].push(`${start}-${end}`) - 1
                    row.__queried = true
                    const overlap = hasOverlap(start,end,timeRange.value as [string,string])
                    ;(row.__slotOverlaps as any)[date][idx] = overlap
                    if(overlap){
                        ;(row.__busy as any)[date] = true
                    }
                })
            })

            // 补全未出现的日期：默认闲
            ;(list.value || []).forEach((row:any)=>{
                dateList.value.forEach((d)=>{
                    if((row.__busy as any)[d] === undefined) (row.__busy as any)[d] = false
                    if((row.__slots as any)[d] === undefined) (row.__slots as any)[d] = []
                    if((row.__slotOverlaps as any)[d] === undefined) (row.__slotOverlaps as any)[d] = []
                })
            })
            console.log(list.value)
        }catch(e){
            console.error(e)
        }
    }).finally(()=>{
        // 查询完成后，将所有现有行标记为已查询，以便没有数据的日期显示为“闲”
        ;(list.value || []).forEach((row:any)=>{ row.__queried = true })
        loading.value = false
    })
}

function close(){
    dialogVisible.value = false
    list.value = []
    dateList.value = []
    timeRange.value = []
    showDetail.value = 0
    queried.value = false
}
let _resolve = null as any
let _reject = null as any
const open = (params: any) => {
    dialogVisible.value = true
    // 默认当前一周日期（周日-周六）
    dateList.value = getCurrentWeekDates()
    queried.value = false
    return new Promise((resolve, reject) => {
        _resolve = resolve
        _reject = reject
    })
}

defineExpose({
    open,
})
</script>
<style lang="scss" scoped>
.objectFreeTime{
    .my-header{
        color: #fff;
    }
    .date-header{
        display: flex;
        justify-content: center;
        gap: 8px;
        align-items: center;
        .date-label{
            font-size: 14px;
            color: #606266;
        }
        .week-label{
            font-size: 12px;
            color: #C0C4CC;
        }
    }
    .arrange-status-label{
        width: 100%;
        height: 20px;
        line-height: 20px;
        border-radius: 3px;
        background: #F4F4F5;
        color: #909399;
        margin-top: 2px;
        font-size: 12px;
        .empty-label{
            color: #C0C4CC;
        }
        &.busy{
            background: #FFECE8;
            color: #F53F3F;
        }
        &.free{
            background: #E6F7EA;
            color: #00B42A;
        }
    }
    .detail-slot{
        width: 100%;
        height: 20px;
        line-height: 20px;
        border-radius: 3px;
        background: #F4F4F5;
        color: #909399;
        margin-top: 4px;
        font-size: 12px;
        &:first-child{
            margin-top: 10px;
        }
        &.busy{
            background: #fde2e2;
            color: #f56c6c;
        }
    }
}
:deep(.wtwo-table .wtwo-table__cell){
    vertical-align: top!important;
}
:deep(.wtwo-table .cell .wtwo-link){
    vertical-align: top!important;
}
</style>