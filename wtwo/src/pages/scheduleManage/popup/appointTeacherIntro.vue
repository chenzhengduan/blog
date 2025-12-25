<template>
	<el-drawer
		v-model="drawer"
		:title="transToConfigDescript('约课老师介绍')"
		direction="rtl"
		size="1150px"
		class="appointTeacherIntro"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true"
	>
	    <div class="drawer-body-wrap min-w-[1150px] p-[16px]" v-loading="loading">
			<div class="mb-[16px]">
				<div class="flex items-center justify-between mb-[8px]">
					<div class="text-[16px] font-bold">{{transToConfigDescript('老师列表')}}</div>
					<div>
						<el-input
							v-model="query"
							class="mr-12px"
							@keyup.enter="getFirstPage"
							style="width: 240px"
							:placeholder="transToConfigDescript('输入老师姓名')"
							clearable
						/>
						<el-button type="primary" :disabled="loading" @click="getFirstPage">查询</el-button>
						<el-button type="primary" :disabled="loading" plain @click="editItemData('add')">{{transToConfigDescript('添加老师')}}</el-button>
					</div>
				</div>
				<pageAttentionTips>
					<div class="flex items-center">
						{{transToConfigDescript('1、此页面的“约课老师介绍”与网上商城中的“名师阵容”信息会相互同步。2、老师介绍内容会在师生信-约课模块中显示。')}}
						<el-popover :width="326" trigger="click" placement="right-start">
							<template #reference>
								<el-link type="primary" :underline="false" class="text-12px! ml-2px">查看示例</el-link>
							</template>
							<div class="text-12px">
								<img src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/schedule-example13.png" width="302px"/>
							</div>
						</el-popover>
					</div>
				</pageAttentionTips>
			</div>
			<el-table
				v-loading="loading"
				:data="list"
				border
			>
				<template #empty>
				<el-empty
					:image="globalData.emptyPng"
					description="暂无数据"
					:image-size="100"
				></el-empty>
				</template>
				<el-table-column prop="Name" :label="transToConfigDescript('老师姓名')" width="280">
					<template #default="scope">
						<div class="flex-center">
							<div class="avatar">
								<div class="avatar" v-if="scope.row.Image">
									<img :src="scope.row.Image" alt="" />
								</div>
								<div class="avatar placeholder" v-else>
									{{ getNameInitial(scope.row.Name) }}
								</div>
							</div>{{ scope.row.Name }}
						</div>
						
					</template>
				</el-table-column>
				<el-table-column prop="NickName" label="昵称" width="180" />
				<el-table-column prop="SubjectList" label="授课科目" show-overflow-tooltip min-width="340">
					<template #default="scope">
						<span v-for="(item, index) in scope.row.SubjectList" :key="index"
						>{{ item.Name
						}}<i v-if="index !== scope.row.SubjectList.length - 1"
							>、</i
						></span
						>
					</template>
				</el-table-column>
				<el-table-column prop="address" label="操作" width="140px">
					<template #default="scope">
						<el-button link type="primary" plain @click="editItemData('edit',scope.row)">编辑介绍</el-button>
						<el-button link type="danger" @click="handleDelete(scope.row)" plain>删除</el-button>
					</template>
				</el-table-column>
			</el-table>
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
	</el-drawer>
	<appointTeacherForm ref="appointTeacherFormRef"></appointTeacherForm>
</template>

<script lang="ts" setup>
import { ref, nextTick, getCurrentInstance } from 'vue';
import { queryTeacherListProfile, deleteTeacherProfile } from '@/api/arrange';
import { assignPage, checkPageIndex, IPageModel, getNameInitial } from '@/utils';
import appointTeacherForm from './appointTeacherForm.vue';
import { transToConfigDescript } from '@/utils/filters/filters';

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
const drawer = ref(false);
const loading = ref(false);
const query = ref('');
const list = ref([]);
const page = ref({
    TotalCount: 0,//总条数
    PageSize: 10,//每页条数
    PageIndex: 1,//第几页
    PageCount: 1 //总页数
} as IPageModel)
const defaultImage = ref("https://cdn01.xiaogj.com/file/mall/default/teacher.png")

async function handleDelete(row:any){
  try {
    await ElMessageBox.confirm(
      transToConfigDescript('是否删除该老师？'),
      '删除提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    const res = await deleteTeacherProfile({id: row.ID});
    if(res.ErrorCode==200){
      ElMessage.success("删除成功");
      getFirstPage();
    }
  } catch (error) {
  }
}

const appointTeacherFormRef = ref<InstanceType<typeof appointTeacherForm> | null>(null)
function editItemData(type:string,row:any = null){
  appointTeacherFormRef.value?.open({type,...row}).then((res:any)=>{
    getFirstPage()
  })
}

async function funcQuery() {
	list.value = [];
    loading.value = true;
    try {
        const res = await queryTeacherListProfile({
            Query: query.value,
            PageIndex: page.value.PageIndex,
            PageSize: page.value.PageSize,
        });
        
        let data = res.Data || [];
        list.value = data;
        assignPage(page.value, {
            PageIndex: res.PageIndex || 1,
            PageSize: res.PageSize || page.value.PageSize,
            PageCount: res.PageCount || 1,
            TotalCount: res.TotalCount || 0
        });
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

let _resolve: any = null,
	_reject: any = null;

async function open() {
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

defineExpose({
	open,
})
</script>

<style lang="scss" scoped>
.appointTeacherIntro {
	.avatar {
		width: 24px;
		height: 24px;
		border-radius: 4px;
		overflow: hidden;
		background: #f2f3f5;
		flex-shrink: 0;
		margin-right: 6px;
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
	}
}
</style>