<template>
	<el-dialog
		v-model="dialogVisible"
		:title="dialogTitle"
		width="560px"
		:close-on-click-modal="false"
		:append-to-body="true"
		:destroy-on-close="true"
		:align-center="true"
		class="chooseClassroom"
		draggable
		@close="close"
	>
		<div class="classroom-selection-container">
			<div class="classroom-select-wrapper mt-16px">
				<el-form label-position="top">
					<el-form-item label="教室">
						<el-select
							v-model="selectModel"
							placeholder="请选择"
							class="classroom-select"
							filterable
							:multiple="multi"
							clearable
							@change="handleSelectChange"
						>
							<el-option
								v-for="item in classroomList"
								:key="item.ID"
								:label="item.Name"
								:value="item.ID"
							/>
						</el-select>
					</el-form-item>
				</el-form>
				
			</div>
		</div>

		<template #footer>
			<div class="dialog-footer">
				<el-button @click="close">取消</el-button>
				<el-button type="primary" @click="confirm" :disabled="!selectedIds.length">确定</el-button>
			</div>
		</template>
	</el-dialog>
</template>
<script lang="ts" setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { queryClassroom } from '@/api/arrange'
import { useCurrentCampuses } from '@/store'

type Classroom = { ID: string | number; Name: string; [key: string]: any }

const dialogVisible = ref(false)
const dialogTitle = ref('选择教室')
const classroomList = ref<Classroom[]>([])
const multi = ref<boolean>(false)
const selectedIds = ref<Array<string | number>>([])
const selectedId = ref<string | number | ''>('')
const selectedObjects = ref<Classroom[]>([])
const selectModel = computed({
	get() {
		return multi.value ? selectedIds.value : selectedId.value
	},
	set(val: any) {
		if (multi.value) {
			selectedIds.value = Array.isArray(val) ? val : []
		} else {
			selectedId.value = val ?? ''
		}
	}
})

let _resolve: any = null
let _reject: any = null

function handleSelectChange(val: any) {
	if (multi.value) {
		const ids = Array.isArray(val) ? val : []
		selectedObjects.value = ids
			.map(id => classroomList.value.find(it => it.ID === id))
			.filter(Boolean) as Classroom[]
	} else {
		const found = classroomList.value.find(it => it.ID === val)
		selectedObjects.value = found ? [found] : []
	}
}

function confirm() {
	_resolve?.({data:selectedObjects.value})
	close()
}

function close() {
	dialogVisible.value = false
	selectedIds.value = []
	selectedId.value = ''
	selectedObjects.value = []
	classroomList.value = []
	_reject?.()
}

function normalizeInitialSelected(initial: any): { ids: Array<string | number>, objId: string | number | '' , objs: Classroom[] } {
    if (!initial) return { ids: [], objId: '', objs: [] }
	if (Array.isArray(initial)) {
        if (initial.length === 0) return { ids: [], objId: '', objs: [] }
		if (typeof initial[0] === 'object') {
			const objs = initial as Classroom[]
			const ids = objs.map(it => it.ID)
			return { ids, objId: '', objs }
		}
		return { ids: initial as Array<string | number>, objId: '', objs: [] }
	}
	// 单个对象或ID
	if (typeof initial === 'object') return { ids: [], objId: (initial as Classroom).ID, objs: [initial as Classroom] }
	return { ids: [], objId: (initial as string | number), objs: [] }
}

function open(params: {
	campusIds?: string
	choosed?: Array<string | number> | Classroom[] | string | number
	multi?: boolean
	title?: string
}) {
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		dialogTitle.value = params?.title || '选择教室'
		multi.value = !!params?.multi
		fetchClassrooms(params).then(() => {
			const { ids, objId, objs } = normalizeInitialSelected(params?.choosed)
			if (multi.value) {
				selectedIds.value = ids.length ? ids : (objId !== '' ? [objId] : [])
				selectedObjects.value = (objs && objs.length > 0)
					? objs
					: selectedIds.value
						.map(id => classroomList.value.find(it => it.ID === id))
						.filter(Boolean) as Classroom[]
			} else {
				selectedId.value = objId !== '' ? objId : (ids[0] ?? '')
				const found = classroomList.value.find(it => it.ID === selectedId.value)
				selectedObjects.value = found ? [found] : (objs && objs.length ? [objs[0]] : [])
			}
			dialogVisible.value = true
		}).catch(() => {
			classroomList.value = []
			dialogVisible.value = true
		})
	})
}

async function fetchClassrooms(params: { campusIds?: string }) {
	const store = useCurrentCampuses()
	const campusIds = params?.campusIds || store.campusList || ''
	if (!campusIds) {
		classroomList.value = []
		return
	}
	try {
		const result: any = await queryClassroom({ campusIds })
		if (result?.ErrorCode === 200 && result?.Data) {
			classroomList.value = result.Data
		} else {
			classroomList.value = []
			if (result?.ErrorMsg) ElMessage.error(result.ErrorMsg)
		}
	} catch (err) {
		classroomList.value = []
		// ElMessage.error('获取教室列表失败')
	}
}

defineExpose({ open })
</script>
<style lang="scss" scoped>
.chooseClassroom {
	.classroom-selection-container {
		position: relative;
	}

	.classroom-select-wrapper {
		margin-bottom: 16px;

		.classroom-select {
			width: 100%;
		}
	}

	.dialog-footer {
		text-align: right;
	}
}
</style>