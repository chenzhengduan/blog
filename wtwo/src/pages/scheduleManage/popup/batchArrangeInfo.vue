<!-- 管理本批次排课 -->
<template>
	<el-drawer
		v-model="drawer"
		title="管理本批次排课"
		direction="rtl"
		size="1150px"
		class="batchArrangeInfo"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
		<div class="px-16px pt-8px pb-16px min-w-[1150px]">
			<pageAttentionTips class="mb-[6px]">下列排课是“同一批次”的规则创建，你可批量修改这批排课。已上课的排课，不支持修改与删除！</pageAttentionTips>
			<div class="flex justify-between items-center mb-[12px]">
				<div>共{{ page.TotalCount }}节排课</div>
				<div>
					<el-button v-if="NewCourse_CourseDelete" type="default" @click="handleDeleteBatch">删除这批排课</el-button>
					<el-button v-if="NewCourse_CourseEdit" type="primary" @click="handleEditBatch">修改这批排课</el-button>
				</div>
			</div>
			<el-table 
				:data="list" 
				style="width: 100%"
				v-loading="loading"
				border>
				<template #empty>
					<el-empty
						:image="globalData.emptyPng"
						description="暂无数据"
						:image-size="100"
					></el-empty>
				</template>
				<el-table-column prop="StartTime" :label="transToConfigDescript('上课时间')" width="250">
					<template #default="{ row }">
						<div>{{ formatClassTime(row) }}</div>
					</template>
				</el-table-column>
				<el-table-column prop="ShiftName" :label="transToConfigDescript('上课课程')" min-width="150" show-overflow-tooltip/>
				<el-table-column prop="ClassNameOrStudentName" :label="transToConfigDescript('上课班级/学员')" min-width="150" show-overflow-tooltip/>
				<el-table-column prop="IsOneToOneName" label="教学形式" min-width="100" show-overflow-tooltip/>
				<el-table-column prop="FinishedName" :label="transToConfigDescript('上课状态')" min-width="100">
					<template #default="scope">
						<el-tag
							effect="light"
							:type="
								scope.row.FinishedName == '已上课'
									? 'success'
									: scope.row.FinishedName == '未上课'
									? 'primary'
									: 'info'
							"
							size="small"
							>{{ transToConfigDescript(scope.row.FinishedName) }}</el-tag
						>
					</template>
				</el-table-column>
			</el-table>
			
			<!-- 分页组件 -->
			<div class="flex justify-end mt-[16px]" v-if="list.length">
				<el-pagination
					:current-page="page.PageIndex"
					:page-size="page.PageSize"
					:page-sizes="[10,20,50,100,200]"
					layout="total, sizes, prev, jumper, next"
					:total="page.TotalCount"
					size="small"
					@size-change="handleSizeChange"
					@current-change="handleCurrentChange"
				/>
			</div>
		</div>
		<addArrangeForm ref="addArrangeFormRef"></addArrangeForm>
	</el-drawer>
</template>

<script lang="ts" setup>
import { getCurrentInstance, ref, nextTick } from 'vue';
import { ElMessageBox } from 'element-plus';
import { 
	getCourseListInfoByPlanID,
	deleteCourseList,
 } from '@/api/arrange';
import { assignPage, checkPageIndex, IPageModel } from '@/utils';
import { dayjs } from 'element-plus'
import useEvent from '@/config/event';
import addArrangeForm from '../popup/addArrangeForm.vue'
import { transToConfigDescript } from '@/utils/filters/filters';
const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;

//权限
const NewCourse_CourseEdit = window.$xgj.op('NewCourse_CourseEdit') //编辑排课
const NewCourse_CourseDelete=window.$xgj.op("NewCourse_CourseDelete") //删除上课记录

const event = useEvent();
const drawer = ref(false);
const loading = ref(false);
const list = ref([]);
const page = ref({
    TotalCount: 0,//总条数
    PageSize: 10,//每页条数
    PageIndex: 1,//第几页
    PageCount: 1 //总页数
} as IPageModel)


async function funcQuery() {
	list.value = [];
    loading.value = true;
    try {
        const res = await getCourseListInfoByPlanID({
            CourseID: courseID.value,
            PlanID: planID.value,
            PageIndex: page.value.PageIndex,
            PageSize: page.value.PageSize,
            Sort: '',
            Desc: 1,
        });
        
        let data = res.Data?.List || [];
        list.value = data;
        assignPage(page.value, res.Data);
    } catch (error) {
        console.error('获取数据失败:', error);
    } finally {
        loading.value = false;
    }
}

async function handleSizeChange(val: number) {
    page.value.PageSize = val;
	page.value.PageIndex = checkPageIndex(page.value.PageIndex,page.value.TotalCount,page.value.PageSize)
    await funcQuery();
}
async function handleCurrentChange(val: number) {
    page.value.PageIndex = val;
    await funcQuery();
}
async function getFirstPage() {
    page.value.PageIndex = 1;
    await funcQuery();
}

// 格式化上课时间
function formatClassTime(row: any) {
    const startTime = dayjs(row.StartTime).format('YYYY-MM-DD HH:mm');
    const endTime = dayjs(row.EndTime).format('HH:mm');
    const weekDay = row.CourseTimeWeek;
    return `${startTime}~${endTime} (${weekDay})`;
}

let _resolve: any = null,
	_reject: any = null,
	courseID = ref(''),
	planID = ref('');
const courseMethod=ref(10)
const CampusID=ref('')

function open(params: any) {
	courseID.value = params.ID;
	CampusID.value=params.CampusID||''
	planID.value = params.PlanID;
	courseMethod.value=params.CourseMethod||10
	drawer.value = true;
	
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		nextTick(() => {
			getFirstPage();
		});
	})
}

function close() {
	drawer.value = false
	_reject && _reject()
}

// 删除这批排课
async function handleDeleteBatch() {
	try {
		await ElMessageBox.confirm(
			transToConfigDescript('确定要删除这批排课吗？已上课的排课不受影响。'),
			'确认',
			{
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning',
			}
		);
		
		loading.value = true;
		await deleteCourseList({
			PlanID: planID.value,
			CourseIDList: [],
			IsPass: false //审批的字段? TODO
		});
		await funcQuery();
		
		// 删除成功后，发送事件通知父组件
		event.emit('arrange-info-refresh');
		event.emit('arrange-table-list-refresh',{refreshTotal:true});

	// 	if( notOperateArr.length>0 ){
	// 	let tips=`共${multipleSelection.value.length}节排课，以下<span class="color-[#F56C6C]">${notOperateArr.length}</span>节是${flag==0?'“已上课”状态，不能删除':'“已上课、已取消”状态，不能取消'}<br/>`
	// 		tips+='<div class="wtwo-check-tips-box mt-[10px]">'
	// 	notOperateArr.forEach((item:any)=>{
	// 		tips+=`<div class="wtwo-check-tips-item">
	// 			<div class="w-[160px] ellipsis-single" title="${item.ShiftName}">${item.ShiftName}</div>
	// 			<div class="w-[160px] ellipsis-single" title="${item.ClassName}">${item.ClassName}</div>
	// 			<div class="w-[180px]"">${dayjs(item.StartTime).format("YYYY-MM-DD HH:mm" )} ~ ${dayjs(item.EndTime).format("HH:mm" )}</div>
	// 		</div>`
	// 	})
	// 	tips+='</div>'
		
	// 	ElMessageBox[operateArr.length?'confirm':'alert'](tips, '提示', {
	// 		confirmButtonText: '返回',
	// 		cancelButtonText:`继续${flag==0?'删除':'取消'}剩下的排课`,
	// 		customStyle:{
	// 			'max-width':'550px'
	// 		},
	// 		dangerouslyUseHTMLString:true
	// 	}).then((res:any)=>{
			
	// 	}).catch(()=>{
	// 		if(flag==0){
	// 			doDelete(operateArr,getFirstPage)
	// 		}
	// 	})
	// }
	} catch (error) {
	} finally {
		loading.value = false;
	}
}
const addArrangeFormRef = ref<InstanceType<typeof addArrangeForm> | null>(null)
//修改这批排课
function handleEditBatch(){
	addArrangeFormRef.value?.open({
		isEdit:true,
		PlanID:planID.value,
		arrangeType:courseMethod.value==20?1:courseMethod.value==30?2:0,
		CampusID:CampusID.value
	}).then(()=>{
		getFirstPage()
		event.emit('arrange-info-refresh');
		event.emit('arrange-table-list-refresh',{refreshTotal:true});
	})
}

defineExpose({
	open,
})
</script>

<style lang="scss" scoped>


// UnoCSS 样式
:deep(.el-drawer) {
	.el-drawer__header {
		padding: 20px 20px 0;
	}
	
	.el-drawer__body {
		padding: 0;
	}
}
</style>