<template>
	<el-drawer v-model="drawer"
		title="列表字段显示设置"
		direction="rtl"
		size="260px"
		class="setTableColumns"
		:close-on-click-modal="false"
		:append-to-body="true"
		@close="close"
		:destroy-on-close="true">
        <div class="drawer-body-wrap" v-loading="loading">
            <div class="wtwo-table-column-item">
                <el-checkbox v-model="allChecked">全部</el-checkbox>
            </div>
            <el-scrollbar class="wtwo-table-column-list">
                <!-- 分组顺序模式：禁用列在前，可拖拽列在后 -->
                <template v-if="orderMode === 'grouped'">
                    <div v-for="item in columns.filter((item:any)=>item.Disabled)" :key="item.Key" class="wtwo-table-column-item flex-between columns-item-disabled">
                        <el-checkbox :label="transToConfigDescript(item.Label)" :model-value="item.Visible" :true-value="1" :false-value="0" :disabled="true"></el-checkbox>
                        <div></div>
                    </div>
                    <div class="wtwo-table-column-list-dragable" ref="dragableRef">
                        <div v-for="item in dragableColumns" :key="item.Key" class="wtwo-table-column-item flex-between"  
                            @click="handleColumnClick(item)">
                            <div class="flex-center">
                                <el-checkbox :label="transToConfigDescript(item.Label)" :model-value="item.Visible" :true-value="1" :false-value="0"></el-checkbox>
                                <el-tooltip v-if="item.Tips" placement="top" class="wtwo-table-header-brief-tooltip">
                                    <el-icon size="16px" class="ml-4px mt-1px" color="#909399">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-xinxitishi"></use>
                                        </svg>
                                    </el-icon>
                                    <template #content>
                                        <div v-html="transToConfigDescript(item.Tips)"></div>
                                    </template>
                                </el-tooltip>
                            </div>
                            
                            <el-icon class="columns-item-drag-icon">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-tuodongpaixu"
                                    ></use>
                                </svg>
                            </el-icon>
                        </div>
                    </div>
                </template>
                
                <!-- 原始顺序模式：按照原始顺序显示所有列，所有列都可以拖动 -->
                <template v-else>
                    <!-- 所有列（包括禁用列）都在一个可拖拽容器中 -->
                    <div class="wtwo-table-column-list-dragable" ref="dragableRef">
                        <div v-for="item in allColumnsForDrag" :key="item.Key" 
                            :class="['wtwo-table-column-item', 'flex-between', item.Disabled ? 'columns-item-disabled' : '']"
                            @click="!item.Disabled && handleColumnClick(item)">
                            <div class="flex-center">
                                <el-checkbox 
                                    :label="transToConfigDescript(item.Label)" 
                                    :model-value="item.Visible" 
                                    :true-value="1" 
                                    :false-value="0"
                                    :disabled="item.Disabled">
                                </el-checkbox>
                                <el-tooltip v-if="item.Tips" placement="top" class="wtwo-table-header-brief-tooltip">
                                    <el-icon size="16px" class="ml-4px mt-1px" color="#909399">
                                        <svg aria-hidden="true">
                                            <use xlink:href="#w2-xinxitishi"></use>
                                        </svg>
                                    </el-icon>
                                    <template #content>
                                        <div v-html="transToConfigDescript(item.Tips)"></div>
                                    </template>
                                </el-tooltip>
                            </div>
                            
                            <el-icon class="columns-item-drag-icon">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-tuodongpaixu"
                                    ></use>
                                </svg>
                            </el-icon>
                        </div>
                    </div>
                </template>
            </el-scrollbar>
        </div>
        <template #footer>
			<div class="wtwo-drawer-footer flex-end">
				<div class="flex-center">
					<el-button @click="close" :disabled="loading">取消</el-button>
					<el-button type="primary" @click="submit" :disabled="loading"
						>保存</el-button
					>
				</div>
			</div>
		</template>
	</el-drawer>
</template>

<script setup lang="ts">
import { postUserColumns } from '@/api';
import { transToConfigDescript } from '@/utils/filters/filters';
import { cloneDeep } from 'lodash';
import { computed, ref, nextTick, onMounted } from 'vue';
import { useDraggable } from 'vue-draggable-plus'

interface IColumn {
    Key: string,//字段名
    Label: string,//字段名称
    Visible: number,//是否显示
    Disabled?: boolean,//是否禁用
    ColumnKey: string,//字段key
    Tips?: string,//提示文案
}

const drawer = ref(false)
const loading = ref(false)
const columns = ref<IColumn[]>([])
const dragableColumns = ref<IColumn[]>([])
const allColumnsForDrag = ref<IColumn[]>([]) // 原始顺序模式下，所有列（包括禁用列）都可以拖动
const moduleId = ref('') //需要是唯一的id，用于标识是哪个页面  建议叫XXX_table
const orderMode = ref<'original' | 'grouped'>('original') // 顺序模式：'original'=原始顺序，'grouped'=分组顺序

const allChecked = computed({
    get: () => {
		let length = columns.value.filter((item:any)=>item.Visible).length
		return length === 0
			? false
			: length === columns.value.length
	},
    set(val:boolean){
        if (val) {
			columns.value.forEach((item: any) => {
                if(!item.Disabled){
                    item.Visible=1
                }
			})
            // 同步更新dragableColumns的可见性状态
            dragableColumns.value.forEach(item => {
                item.Visible = 1
            })
            // 同步更新allColumnsForDrag的可见性状态
            allColumnsForDrag.value.forEach(item => {
                if(!item.Disabled){
                    item.Visible = 1
                }
            })
		} else {
            columns.value.forEach((item: any) => {
                if(!item.Disabled){
                    item.Visible=0
                }
			})
            // 同步更新dragableColumns的可见性状态
            dragableColumns.value.forEach(item => {
                item.Visible = 0
            })
            // 同步更新allColumnsForDrag的可见性状态
            allColumnsForDrag.value.forEach(item => {
                if(!item.Disabled){
                    item.Visible = 0
                }
            })
		}
    }
})

function handleColumnClick(item:any){
    if(item.Disabled){
        return
    }
    item.Visible=item.Visible==1?0:1
    
    // 同步更新dragableColumns中对应项的可见性状态
    const dragItem = dragableColumns.value.find(dragCol => dragCol.Key === item.Key)
    if (dragItem) {
        dragItem.Visible = item.Visible
    }
    
    // 同步更新allColumnsForDrag中对应项的可见性状态
    const allDragItem = allColumnsForDrag.value.find(col => col.Key === item.Key)
    if (allDragItem) {
        allDragItem.Visible = item.Visible
    }
}

function submit(){
    if(moduleId.value==''){
        ElMessage.error('moduleId不能为空')
        return
    }
    
    let sortedColumns: IColumn[] = []
    
    if (orderMode.value === 'grouped') {
        // 分组顺序模式：先添加disabled的字段（保持原有顺序），再添加拖拽排序后的字段
        columns.value.forEach(item => {
            if (item.Disabled) {
                sortedColumns.push(item)
            }
        })
        
        dragableColumns.value.forEach(dragItem => {
            const originalItem = columns.value.find(col => col.Key === dragItem.Key)
            if (originalItem) {
                sortedColumns.push({
                    ...originalItem,
                    Visible: dragItem.Visible
                })
            }
        })
    } else {
        // 原始顺序模式：直接使用 allColumnsForDrag 的顺序（已经包含拖拽后的顺序）
        sortedColumns = allColumnsForDrag.value.map(item => ({
            ...item,
            Visible: item.Visible
        }))
    }
    
    let allColumns=sortedColumns.map((item:any)=>{
        return {
            Key:item.Key,
            Visible:item.Visible
        }
    })
    
    postUserColumns({
        moduleId:moduleId.value,
        allColumns:JSON.stringify(allColumns),
        hideColumns:'' //新版这个值没得什么用
    }).then((res:any)=>{
        _resolve({
            data:sortedColumns // 返回排序后的结果
        })
        drawer.value=false
    })
}

const dragableRef = ref<HTMLElement | null>(null);

// 初始化拖拽功能
const initDraggable = () => {
    if (dragableRef.value && orderMode.value === 'original') {
        // 原始顺序模式：所有列（包括禁用列）都可以拖动
        useDraggable(dragableRef, allColumnsForDrag, {
            animation: 150,
            onStart() {
                // console.log('start')
            },
            onUpdate() {
                // 拖拽更新后，同步更新columns的顺序
                columns.value = [...allColumnsForDrag.value]
                // 同步更新dragableColumns
                dragableColumns.value = columns.value.filter((item:any)=>!item.Disabled)
            }
        })
    } else if (dragableRef.value && orderMode.value === 'grouped') {
        // 分组顺序模式：只有非禁用列可以拖动
        useDraggable(dragableRef, dragableColumns, {
            animation: 150,
            onStart() {
                // console.log('start')
            },
            onUpdate() {
                // 拖拽更新后，同步更新columns中对应项的可见性状态
                dragableColumns.value.forEach(dragItem => {
                    const originalItem = columns.value.find(col => col.Key === dragItem.Key)
                    if (originalItem) {
                        originalItem.Visible = dragItem.Visible
                    }
                })
            }
        })
    }
}

// 在组件挂载后初始化拖拽
onMounted(() => {
    nextTick(() => {
        initDraggable()
    })
})

let _resolve: any = null,
	_reject: any = null
/** 对外暴露一个open方法 */
function open(params: any) {
    columns.value=params.columns?cloneDeep(params.columns):[]
    dragableColumns.value=columns.value.filter((item:any)=>!item.Disabled)
    // 原始顺序模式下，所有列（包括禁用列）都可以拖动
    allColumnsForDrag.value = cloneDeep(columns.value)
    moduleId.value=params.moduleId
    // 支持传入 orderMode 参数，默认为 'original'
    orderMode.value = params.orderMode || 'original'
	return new Promise((resolve, reject) => {
		_resolve = resolve
		_reject = reject
		drawer.value = true
		// 在抽屉打开后重新初始化拖拽功能
		nextTick(() => {
			initDraggable()
		})
	})
}

function close() {
    columns.value=[]
    dragableColumns.value=[]
    allColumnsForDrag.value=[]
	drawer.value = false
	_reject && _reject()
}

defineExpose({
	open,
})   
</script>

<style scoped lang="scss">
.setTableColumns{
    .wtwo-checkbox-group{
        display: flex;
        flex-wrap: wrap;
    }
    .wtwo-table-column-item{
        padding: 4px 12px;
        border-radius: 3px;
        cursor: pointer;
        &:hover{
            background-color: rgba(0,0,0,0.04);
        }
        &.columns-item-disabled{
            cursor: grab;
            &:active{
                cursor: grabbing;
            }
        }
        .columns-item-drag-icon{
            visibility: hidden;
            font-size: 18px;
            &:hover{
                cursor: grab;
            }
        }
        &:hover{
            .columns-item-drag-icon{
                visibility: visible;
            }
        }
    }
    .wtwo-table-column-divider{
        display: flex;
        align-items: center;
        margin: 8px 0;
        .divider-line{
            flex: 1;
            height: 1px;
            background-color: #e4e7ed;
        }
        .divider-text{
            padding: 0 12px;
            font-size: 12px;
            color: #909399;
            white-space: nowrap;
        }
    }
}
</style>
<style lang="scss">
.wtwo-table-header-brief-tooltip {
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

