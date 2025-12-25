<template>
	<el-drawer
		v-model="drawer"
		direction="rtl"
		size="1150px"
		class="addArrangeForm"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
		<template #header>
			<div class="flex-center">
				<el-select
					v-if="!isEdit"
					:model-value="CampusID" 
                    :placeholder="transToConfigDescript('请选择1个校区')" 
                    @update:model-value="handleCampusChange"
                    class="header-campus-select w-280px!"
                    popper-class="campus-select-dropdown"
                    filterable
				>
					<template #prefix>
						<p class="search-input-label">
							{{ transToConfigDescript('上课校区') }}
						</p>
					</template>
                    <el-option-group
                        v-for="group in options"
                        :key="group.label"
                        :label="group.label"
                    >
                        <el-option
                            v-for="item in group.options"
                            :key="item.ID"
                            :label="item.Name"
                            :value="item.ID"
                        />
                    </el-option-group>
				</el-select>
				<div v-else>修改这批排课</div>
			</div>
		</template>
		<div class="disabled-mask" v-if="!CampusID">
			<div class="ml-140px" style="position: relative;">
				<img src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/guide.png" width="184px"/>
				<div style="position: absolute;bottom: 6px;left:8px;height: 46px;line-height:18px;color: #fff;width: 100px;word-break: break-all;display: flex;align-items: center;">{{ transToConfigDescript('请选择上课校区') }}</div>
			</div>
		</div>
		<div class="drawer-body-wrap" v-loading="loading" element-loading-text="正在排课中...">
			<div class="flex-center mb-16px!" v-if="!isEdit">
				<div class="arrange-type-tabs">
					<div
						v-if="NewCourse_ClassCourse"
						class="arrange-type-tab"
						:class="{ active: arrangeType === 0 }"
						@click="handleArrangeTypeTabClick(0)"
					>
						{{transToConfigDescript('给班级排课')}}
					</div>
					<div
						v-if="NewCourse_StudentCourse"
						class="arrange-type-tab"
						:class="{ active: arrangeType === 1 }"
						@click="handleArrangeTypeTabClick(1)"
					>
						给学员排课
					</div>
					<div
						v-if="NewCourse_SubscribeCourse"
						class="arrange-type-tab"
						:class="{ active: arrangeType === 2 }"
						@click="handleArrangeTypeTabClick(2)"
					>
						排预约课
					</div>
				</div>
			</div>
			
			
            <!-- 给班级排课 -->
            <addClassArrangeForm 
                v-show="arrangeType === 0" 
                ref="addClassArrangeFormRef"
				:is-edit="isEdit"
				:selectedCampusId="CampusID"
                :activated="activatedTabs.classTab"
				:disabledAutoFillPlan="disabledAutoFillPlan"
                @submit="handleClassArrangeSubmit"
            />
			
            <!-- 给学员排课 -->
            <addStudentArrangeForm 
                v-show="arrangeType === 1" 
                ref="addStudentArrangeFormRef"
                :is-edit="isEdit"
				:selectedCampusId="CampusID"
                :activated="activatedTabs.student"
				:disabledAutoFillPlan="disabledAutoFillPlan"
                @submit="handleStudentArrangeSubmit"
            />
            <!-- 排预约课 -->
            <addShiftArrangeForm 
                v-show="arrangeType === 2" 
                ref="addShiftArrangeFormRef"
				:is-edit="isEdit"
				:selectedCampusId="CampusID"
                :activated="activatedTabs.shift"
                @submit="handleShiftArrangeSubmit"
            />
		</div>
		<template #footer>
			<div class="wtwo-drawer-footer flex-between">
				<div class="flex-center">
					<el-checkbox :model-value="checkConflict" :true-value="1" :false-value="0" :disabled="loading||!NewCourse_IngoreCourseConflict" @click="changeCheckConflict">{{transToConfigDescript('检查上课冲突')}}</el-checkbox>
				</div>
				<div class="flex-center">
					<el-button @click="close" :disabled="loading">取消</el-button>
					<!-- <el-button :disabled="loading">预览排课</el-button> -->
					<el-button type="primary" @click="submit" :disabled="loading"
						>确认排课</el-button
					>
				</div>
			</div>
		</template>
		<ConflictPrompt ref="conflictPromptRef" />
	</el-drawer>
</template>

<script lang="ts" setup>
import { addCourse, addStudentCourse, addSubscribeCourse, editCoursePlan, getSchedulePlan } from '@/api/arrange'
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddClassArrangeForm from './child/addClassArrangeForm.vue'
import AddStudentArrangeForm from './child/addStudentArrangeForm.vue'
import AddShiftArrangeForm from './child/addShiftArrangeForm.vue'
import ConflictPrompt from './conflictPrompt.vue'
import { cloneDeep } from 'lodash'
import { useCurrentCampuses, useUserCampuses } from '@/store'
import { transToConfigDescript } from '@/utils/filters/filters'
import { CourseTimetableTypeEnum } from '@/types/model/timetable-preference'

//权限
const NewCourse_ClassCourse = window.$xgj.op('NewCourse_ClassCourse') //给班级排课
const NewCourse_StudentCourse = window.$xgj.op('NewCourse_StudentCourse') //给学员排课
const NewCourse_SubscribeCourse = window.$xgj.op('NewCourse_SubscribeCourse') //排预约课
const NewCourse_IngoreCourseConflict = window.$xgj.op('NewCourse_IngoreCourseConflict') //跳过冲突检查的权限

const LAST_ARRANGE_TYPE_KEY = 'addArrangeForm:lastArrangeType'

const drawer = ref(false)
const loading = ref(false)
const arrangeType = ref(0)
const checkConflict = ref(1)

const addClassArrangeFormRef = ref()
const addStudentArrangeFormRef = ref()
const addShiftArrangeFormRef = ref()
const conflictPromptRef = ref()
const isEdit=ref(false)
const disabledAutoFillPlan=ref(false)
const CampusID=ref('')
const currentCampus = computed(() => {
    return useCurrentCampuses().campusList
})
// 从store获取数据
const userCampusesStore = useUserCampuses()
const currentCampusesStore = useCurrentCampuses()

// 计算属性
const allCampusList = computed(() => {
	return userCampusesStore.userCampuses || []
})

const recentCampusList = computed(() => {
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	
	return allCampusList.value.filter((campus: any) => 
		currentCampusIds.includes(campus.ID.toString())
	)
})

const otherCampusList = computed(() => {
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	return allCampusList.value.filter((campus: any) => !currentCampusIds.includes(campus.ID.toString()))
})

const options = computed(() => {
    // 基础列表
    let recent = recentCampusList.value
    let others = otherCampusList.value

    return [
        {
            label:'最近使用',
            options: recent
        },{
            label: recent.length>0?transToConfigDescript('其他校区'):transToConfigDescript('全部校区'),
            options: others
        }
    ]
})  
// 首次激活标记（用于通知子组件进行一次性初始化）；保持 v-show，不做销毁/挂载
const activatedTabs = ref({
    classTab: false,
    student: false,
    shift: false,
})

function handleArrangeTypeTabClick(type: number) {
    arrangeType.value = type
    if (type === 0) {
        activatedTabs.value.classTab = true
    } else if (type === 1) {
        activatedTabs.value.student = true
    } else if (type === 2) {
        activatedTabs.value.shift = true
    }
    try {
        localStorage.setItem(LAST_ARRANGE_TYPE_KEY, String(type))
    } catch (e) {
        // ignore storage errors
    }
}

// 修改handleClassArrangeSubmit方法，传递更多信息
function handleClassArrangeSubmit(params: any,ClassName:string) {
	loading.value = true
	if(checkConflict.value==1){
		params.CheckConflict=1
	}else{
		params.CheckConflict=0
	}
	let promise=isEdit.value?editCoursePlan:addCourse
	promise(params).then((res: any) => {
		ElMessage.success('已排课')
		_resolve({
			...params,
			arrangeType: 0,
			ClassName: ClassName
		})
		drawer.value = false
	}).catch((error: any) => {
		if(error.ErrorCode==409){
			conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{
				// addClassArrangeFormRef.value?.submit()
			}).catch((back:any)=>{
				if(back&&back.closeCurrent){
					drawer.value=false
				}
			})
		}else{
			// ElMessage.error('排课失败：' + (error.ErrorMsg || '未知错误'))
		}
	}).finally(() => {
		loading.value = false
	})
}

// 修改handleStudentArrangeSubmit方法，传递更多信息
function handleStudentArrangeSubmit(params: any,StudentName:string) {
	loading.value = true
	if(checkConflict.value==1){
		params.CheckConflict=1
	}else{
		params.CheckConflict=0
	}
	let promise=isEdit.value?editCoursePlan:addStudentCourse
	promise(params).then((res: any) => {
		ElMessage.success('已排课')
		_resolve({
			...params,
			arrangeType: 1,
			StudentName: StudentName
		})
		drawer.value = false
	}).catch((error: any) => {
		if(error.ErrorCode==409){
			conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{
				// addStudentArrangeFormRef.value?.submit()
			}).catch((back:any)=>{
				if(back&&back.closeCurrent){
					drawer.value=false
				}
			})
		}else{
			// ElMessage.error('排课失败：' + (error.ErrorMsg || '未知错误'))
		}
	}).finally(() => {
		loading.value = false
	})
}

// 修改handleShiftArrangeSubmit方法，传递更多信息
function handleShiftArrangeSubmit(params: any,ShiftName:string) {
	loading.value = true
	if(checkConflict.value==1){
		params.CheckConflict=1
	}else{
		params.CheckConflict=0
	}
	let promise=isEdit.value?editCoursePlan:addSubscribeCourse
	promise(params).then((res: any) => {
		ElMessage.success('已排课')
		_resolve({
			...params,
			arrangeType: 2,
			ShiftName: ShiftName
		})
		drawer.value = false
	}).catch((error: any) => {
		if(error.ErrorCode==409){
			conflictPromptRef.value?.open({info:error.Data,enablePreview:true}).then(()=>{
				// addShiftArrangeFormRef.value?.submit()
			}).catch((back:any)=>{
				if(back&&back.closeCurrent){
					drawer.value=false
				}
			})
		}else{
			// ElMessage.error('排课失败：' + (error.ErrorMsg || '未知错误'))
		}
	}).finally(() => {
		loading.value = false
	})
}

function submit() {
	if (arrangeType.value === 0) {
		// 班级排课
		addClassArrangeFormRef.value?.submit()
	} else if (arrangeType.value === 1) {
		// 学员排课
		addStudentArrangeFormRef.value?.submit()
	} else if (arrangeType.value === 2) {
		// 预约排课
		addShiftArrangeFormRef.value?.submit()
	}
}

function changeCheckConflict() {
	if(checkConflict.value==1){
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

let _resolve: any = null,
	_reject: any = null

/** 对外暴露一个open方法 */
function open(params: any) {
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
        // 处理传入的参数
		if (params&&Object.values(params).some(value => value !== undefined && value !== null)) {
			isEdit.value = params.isEdit||false
			disabledAutoFillPlan.value=!!params.disabledAutoFillPlan
			// 仅在传入的 CampusID 在 allCampusList 中存在时才赋值
			const providedCampus = params.CampusID || ''
			if(providedCampus==''){
				if(params.timetableType!=CourseTimetableTypeEnum.ClassroomTimetable){
					// 教室课表不指定校区时不处理
					const campusList = currentCampus.value.length > 0 ? currentCampus.value.split(',') : []
					if (campusList.length == 1) {
						const candidate = campusList[0]
						validateAndAssignCampus(candidate, true, isEdit.value)
					}
				}
				
			}else{
				validateAndAssignCampus(providedCampus, true, isEdit.value)
			}
			
			// 设置排课类型
            if (params.arrangeType !== undefined) {
                arrangeType.value = params.arrangeType
            } else {
				setTabBylocalStorage()
            }
			
			checkTab()
			// 设置其他参数到子组件
			setupFormWithParams(params)
		} else {
			const campusList = currentCampus.value.length > 0 ? currentCampus.value.split(',') : []
			if (campusList.length == 1) {
				const candidate = campusList[0]
				validateAndAssignCampus(candidate, true, isEdit.value)
			}
            setTabBylocalStorage()
            checkTab()
        }
		drawer.value = true
	})
}

function setTabBylocalStorage(){
	// 无参数时，也尝试读取本地缓存并处理权限与激活状态
	try {
		const saved = localStorage.getItem(LAST_ARRANGE_TYPE_KEY)
		if (saved !== null && saved !== '') {
			const parsed = Number(saved)
			if (!Number.isNaN(parsed)) {
				arrangeType.value = parsed
			}
		}
	} catch (e) {
		// ignore storage errors
	}
}

// 验证候选校区是否存在于 allCampusList 中，存在则赋值。
// 参数: clearIfInvalid - 若候选无效则是否清空 CampusID；skipValidation - 若为 true 则直接赋值不校验
function validateAndAssignCampus(candidate: string | undefined | null, clearIfInvalid = true, skipValidation = false) {
	if (skipValidation) {
		CampusID.value = candidate || ''
		if(CampusID.value!=''){
			setCurrentCampus(CampusID.value);
		}
		return !!candidate
	}

	if (candidate && allCampusList.value.some((c: any) => String(c.ID) === String(candidate))) {
		CampusID.value = candidate
		if(CampusID.value!=''){
			setCurrentCampus(CampusID.value);
		}
		return true
	}
	if (clearIfInvalid) {
		CampusID.value = ''
	}
	return false
}

function checkTab(){
	if(!isEdit.value){
		if(!NewCourse_ClassCourse&&arrangeType.value==0){
			arrangeType.value=NewCourse_StudentCourse?1:2
		}else if(!NewCourse_StudentCourse&&arrangeType.value==1){
			arrangeType.value=NewCourse_ClassCourse?0:2
		}else if(!NewCourse_SubscribeCourse&&arrangeType.value==2){
			arrangeType.value=NewCourse_ClassCourse?0:1
		}
	}
	localStorage.setItem(LAST_ARRANGE_TYPE_KEY, String(arrangeType.value))
	if (arrangeType.value === 0) activatedTabs.value.classTab = true
	if (arrangeType.value === 1) activatedTabs.value.student = true
	if (arrangeType.value === 2) activatedTabs.value.shift = true
}

// 根据参数设置表单
function setupFormWithParams(params: any) {
	// 延迟执行，确保子组件已经渲染
	nextTick(() => {
		// 使用setTimeout确保子组件完全渲染
		setTimeout(() => {
			if(params.isEdit){
				loading.value=true
				getSchedulePlan({
					PlanID:params.PlanID
				}).then((res)=>{
					let data=res.Data||{}
					let para:any={
						...cloneDeep(params),
						...data
					}
					handleInit(para)
				}).finally(()=>{
					loading.value=false
				})
			}else{
				handleInit(params)
			}
			
            
		}, 100) // 100ms延迟确保组件完全渲染
	})
}

// 处理校区变化
async function handleCampusChange(newCampusId: string) {
    // 保存旧值用于回滚
    const oldCampusId = CampusID.value;
    
    // 如果新值和旧值相同，不需要处理
    if (newCampusId === oldCampusId) {
        return;
    }

    try {
		if(oldCampusId!==''){
			// 获取当前激活的表单引用
			let currentFormRef = null
			if (arrangeType.value === 0) {
				currentFormRef = addClassArrangeFormRef.value
			} else if (arrangeType.value === 1) {
				currentFormRef = addStudentArrangeFormRef.value
			} else if (arrangeType.value === 2) {
				currentFormRef = addShiftArrangeFormRef.value
			}

			// 检查当前表单是否有关键字段变化
			const hasChanges = currentFormRef?.hasKeyFieldsChanged ? currentFormRef.hasKeyFieldsChanged() : false

			// 只有当关键字段有变化时才弹出确认框
			if (hasChanges) {
				await ElMessageBox.confirm(
					transToConfigDescript('切换"上课校区"仅保留设置的时间等信息，确认切换吗?'),
					'提示',
					{
						confirmButtonText: '确认',
						cancelButtonText: '取消',
						type: 'warning',
						closeOnClickModal: false,
						closeOnPressEscape: false
					}
				)
			}
		}

		// 用户确认切换，更新 CampusID 并重置表单
		CampusID.value = newCampusId;
        setCurrentCampus(newCampusId);
		// 重置当前表单
		addClassArrangeFormRef.value?.resetForm()
		addStudentArrangeFormRef.value?.resetForm()
		addShiftArrangeFormRef.value?.resetForm()
		
    } catch (error) {
        // 用户取消切换，不需要回滚（因为使用 :model-value，值还没变）
    }
}

function setCurrentCampus(campusId:string){
	const currentCampusIds = currentCampusesStore.campusList
		? currentCampusesStore.campusList.split(',').filter(id => id.trim())
		: []
	if(currentCampusIds.length===1&&currentCampusIds[0]===campusId){
		//校区没变化不要更新
	}else{
		currentCampusesStore.$state={
			campusList:campusId,
			multi:true
		}
		if (window.microApp) {
			window.microApp.dispatch({type:'global:setCampus',campus: campusId,time:Date.now()})
		}
	}
	
}

function handleInit(params:any){
	if (params.arrangeType === 0 && addClassArrangeFormRef.value) {
		// 班级排课
		addClassArrangeFormRef.value.setupWithParams(params)
	} else if (params.arrangeType === 1 && addStudentArrangeFormRef.value) {
		// 学员排课
		addStudentArrangeFormRef.value.setupWithParams(params)
	}else if((params.arrangeType === 2&&addShiftArrangeFormRef.value)){
		addShiftArrangeFormRef.value.setupWithParams(params)
	}
}

function close() {
	drawer.value = false
	nextTick(()=>{
		disabledAutoFillPlan.value=false
		CampusID.value=''
		arrangeType.value=0
		checkConflict.value=1
		activatedTabs.value = { classTab: false, student: false, shift: false }
		addClassArrangeFormRef.value?.resetAllForm()
		addStudentArrangeFormRef.value?.resetAllForm()
		addShiftArrangeFormRef.value?.resetAllForm()
	})
	_reject && _reject()
}

defineExpose({
	open,
})
</script>

<style lang="scss" scope>
.addArrangeForm {
	.drawer-body-wrap {
		min-width: 1150px;
		padding-top: 12px;
		.arrange-type-tabs{
			margin:0 16px 0 16px;
		}
		.two-column-wrap {
			flex-wrap: wrap;
			.half-width {
				width: calc(50% - 30px);
				&:nth-child(2n) {
					margin-left: 60px;
				}
			}
		}
		.half-width {
			width: 50%;
		}
	}
}
.arrange-type-tabs {
	display: flex;
	background-color: #f5f7fa;
	border-radius: 8px;
	padding: 4px;
	gap: 4px;
	width: fit-content;
}

.arrange-type-tab {
	padding: 10px 16px;
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
		background-color: var(--wtwo-color-primary);
		color: white;
		font-weight: 500;
	}
	
}
.header-campus-select{
	.wtwo-select__wrapper{
		background-color: #F2F3F5;
		box-shadow:none;
	}
}
.option-content {
	display: flex;
	align-items: center;

	.option-title {
		font-size: 14px;
		color: #303133;
		line-height: 20px;
	}
}
.disabled-mask{
	width: 100%;
	height: 100%;
	position: absolute;
	background-color: rgba(255,255,255,0.8);
	z-index: 20;
}
</style>

