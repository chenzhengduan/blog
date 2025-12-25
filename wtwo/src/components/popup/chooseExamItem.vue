<template>
    <!-- 选考试项 -->
    <el-dialog v-model="dialogVisible" :title="transToConfigDescript(popTitle)" :width="multi ? '1180px' : '870px'"
        :close-on-click-modal="false" :append-to-body="true" :destroy-on-close="true" :align-center="true"
        class="chooseCourse choose-exam-item" draggable @close="close">
        <div class="box-wrapper-table" :class="{ 'multi-choosed-box-wrapper': multi }">
            <div class="fixed-table-box">
                <div class="modal-filter multi-conditional">
                    <div class="filter-item mr-20px" :class="showShiftType ? 'w-262px' : 'w-534px'">
                        <el-input v-model.trim="condition.Keyword" :placeholder="transToConfigDescript('请输入考试项名称')"
                            class="class-input" @keyup.native.enter="getFirstPage"></el-input>
                    </div>

                    <div class="filter-item">
                        <el-button type="primary" @click="getFirstPage" :disabled="loading">查询</el-button>
                        <el-button plain @click="reset" :disabled="loading">重置</el-button>
                    </div>
                </div>
                <div class="table-wrap" v-loading="loading" ref="tableContainerRef">
                    <el-table ref="customTable" :data='list' :highlight-current-row="!multi" style="width: 100%;"
                        @row-dblclick="confirmChoose" @row-click="chooseIt" :height="tableHeight">
                        <template #empty>
                            <el-empty description="暂无数据" :image="globalData.emptyPng" :image-size="100" />
                        </template>
                        <el-table-column fixed v-if="multi" prop="ID" key="ID" width="40px" align="center">
                            <template #header="scope">
                                <el-checkbox v-model="allChecked"></el-checkbox>
                            </template>
                            <template #default="scope">
                                <el-checkbox @click.native.prevent
                                    :model-value="!!checkedList.find((el: any) => { return el == scope.row.ID })"></el-checkbox>
                            </template>
                        </el-table-column>
                        <el-table-column fixed prop="Name" width="300px" :label="transToConfigDescript('名称')"
                            show-overflow-tooltip></el-table-column>
                        <el-table-column prop="ScoreTypeName" label="类型" width="100px"
                            show-overflow-tooltip></el-table-column>
                        <el-table-column prop="Describe" label="备注" min-width="200px"
                            show-overflow-tooltip></el-table-column>
                    </el-table>
                </div>
                <div class="pageination-wrapper flex-end" v-if="list.length">
                    <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                        :current-page.sync="page.PageIndex" :page-sizes="[10, 20, 50, 100, 200]" :page-size="page.PageSize"
                        layout="total, sizes, prev, jumper ,next" :total="page.TotalCount" size="small">
                    </el-pagination>
                </div>
            </div>
            <div class="choosed-result-wrapper" v-if="multi">
                <div class="choosed-result">
                    <div class="choosed-result-title">
                        <span>已选择：{{ selected.length }}</span>
                        <el-link type="primary" underline="never" @click="removeAll">清空</el-link>
                    </div>
                    <div class="choosed-result-content">
                        <ul>
                            <li v-for="(item, index) in selected" :key="item.ID">
                                <span class="choosed-item-name" :title="item.Name">
                                    {{ item.Name }}
                                </span>
                                <span class="choosed-item-status">
                                    <span v-if="item.status === 0" class="deleted-tag">(已删除)</span>
                                    <a class="del-btn" @click="remove(item.ID, index)"><el-icon>
                                            <deleteSVG></deleteSVG>
                                        </el-icon></a>
                                </span>

                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <div :class="multi ? 'flex-end' : 'flex-between'">
                <div class="single-choosed-wrapper" v-if="!multi">
                    <span class="single-choosed-name">已选择：{{ oneChoosed ? oneChoosed.Name : '' }}</span>
                </div>
                <div>
                    <el-button plain @click="close">取消</el-button>
                    <el-button type="primary" @click="submit">确定</el-button>
                </div>
            </div>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import $ from 'jquery'
import { storeToRefs } from 'pinia';
import { useConfigs, useCurrentCampuses, useUserCampuses, useYears } from '@/store';
import { useDictFieldsStore } from '@/store/dict';

const fieldsStore = useDictFieldsStore();
const { dictFields } = storeToRefs(fieldsStore);

import { queryDictionaryList, queryExamSubjectsList } from '@/api';
import { transToConfigDescript } from '@/utils/filters/filters';
import { cloneDeep } from 'lodash';

import deleteSVG from '@/assets/svg/delete.svg'
import { computed, getCurrentInstance, nextTick, ref } from 'vue';
import { TableInstance } from 'element-plus';
import { checkPageIndex } from '@/utils';

const instance = getCurrentInstance()
const globalData = instance?.appContext.config.globalProperties.$global


const currentCampus = computed(() => {
    return useCurrentCampuses().campusList
})

const dialogVisible = ref(false)
// 弹框参数 start
const multi = ref(false)
const popTitle = ref('选择考试项')
const choosed = ref([] as any)
const showCampus = ref(true)
const showShiftType = ref(true)
const required = ref(false)
const maxNum = ref(20)//考试项最大数量
// 弹框参数 end

const selected = ref([] as any)
const selectedID = ref([] as any)
const loading = ref(false)
const list = ref([] as any)
const checkedList = ref([] as any)
const oneChoosed = ref(null as any)
const page = ref({
    PageIndex: 1,
    PageSize: 10,
    TotalCount: 0
})
const defaultCondition: any = {
    Keyword: "",
}
const condition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))
const newCondition = ref<typeof defaultCondition>(cloneDeep(defaultCondition))

const allChecked = computed({
    get: () => {
        let length = checkedList.value.length;
        return length === 0 ? false : checkedList.value.length === list.value.length;
    },
    set: (val: boolean) => {
        if (val) {
            let ids: any = [];
            list.value.forEach((item: any) => {
                ids.push(item.ID);
                if (selectedID.value.indexOf(item.ID) === -1) {
                    selectedID.value.push(item.ID);
                    selected.value.push(item);
                }
            })
            checkedList.value = ids;
        } else {
            list.value.forEach((item: any) => {
                let index = selectedID.value.indexOf(item.ID);
                if (index !== -1) {
                    selectedID.value.splice(index, 1);
                    selected.value.splice(index, 1);
                }
            })
            checkedList.value = [];
        }
    }
})

//多选选择
function chooseIt(row: any, column: any) {
    let item = row, index = row.$index;
    if (multi.value) {
        let checkedIndex = checkedList.value.indexOf(item.ID);
        if (checkedIndex === -1) {
            checkedList.value.push(item.ID);
            selectedID.value.push(item.ID);
            selected.value.push(item);
        } else {
            checkedList.value.splice(checkedIndex, 1);
            let selectedIndex = selectedID.value.indexOf(item.ID);
            selected.value.splice(selectedIndex, 1);
            selectedID.value.splice(selectedIndex, 1);
        }
    } else {
        customTable.value?.setCurrentRow(row);
        oneChoosed.value = item;
    }
}

//单选双击选择
function confirmChoose(item: any) {
    if (!multi.value) {
        oneChoosed.value = item;
        submit();
    }
}

//移除选中项
function remove(id: string, index: number) {
    // 根据ID找到在selectedID数组中的索引
    let selectedIndex = selectedID.value.indexOf(id);
    if (selectedIndex !== -1) {
        selectedID.value.splice(selectedIndex, 1);
        selected.value.splice(selectedIndex, 1);
    }

    // 根据ID找到在checkedList数组中的索引
    let checkedIndex = checkedList.value.indexOf(id);
    if (checkedIndex !== -1) {
        checkedList.value.splice(checkedIndex, 1);
    }
}

//清空选中项
function removeAll() {
    checkedList.value = [];
    selected.value = [];
    selectedID.value = [];
}

//确认
function submit() {
    let params: any = {}
    if (multi.value) {
        if (required.value && selected.value.length === 0) {
            ElMessage({
                message: '请至少选择一个考试项。',
                type: 'warning',
            })
            return;
        }
        if (maxNum.value && selected.value.length > maxNum.value) {
            ElMessage({
                message: '单场考试最多添加' + maxNum.value + '个考试项，请删减现有考试项再添加。',
                type: 'warning',
            })
            return;
        }
        params.data = selected.value
    } else {
        if (required.value && !oneChoosed.value) {
            ElMessage({
                message: '请先选择考试项。',
                type: 'warning',
            })
            return;
        }
        params.data = oneChoosed.value
    }

    _resolve(params)
    close()
}

//查第一页
function getFirstPage() {
    page.value.PageIndex = 1;
    funcQuery();
}
function handleSizeChange(val: any) {
    page.value.PageSize = val;
    page.value.PageIndex = checkPageIndex(page.value.PageIndex, page.value.TotalCount, page.value.PageSize)
    funcQuery();
}
function handleCurrentChange(val: any) {
    page.value.PageIndex = val
    funcQuery()
}

function reset() {
    restCondition()
    getFirstPage();
}

function restCondition() {
    Object.assign(condition.value, cloneDeep(newCondition.value))

}

function resetDefaultCondition() {
    condition.value = cloneDeep(defaultCondition)
}

//取消
function close() {
    resetDefaultCondition()
    removeAll()
    oneChoosed.value = null
    dialogVisible.value = false
    _reject()
}

//查询
function funcQuery() {
    loading.value = true
    list.value = [];
    checkedList.value = [];
    let params: any = {
        Keyword: condition.value.Keyword || '',
        Type: 'EXAM_SUBJECT'
    }
    Object.assign(params, {
        ...page.value
    })

    queryExamSubjectsList(params).then((res) => {
        if (res.ErrorCode === 200) {
            // 转换数据格式，将 Value 字段映射为 Name 字段
            list.value = (res.Data.List || []).map((item: any) => ({
                ...item,
                Name: item.Value, // 将 Value 字段映射为 Name 字段
                ID: item.ID
            }));
            let checkedArr: any = [];
            list.value.forEach((item: any) => {
                if (selectedID.value.indexOf(item.ID) !== -1) {
                    checkedArr.push(item.ID);
                }
            })
            checkedList.value = checkedArr;
            Object.assign(page.value, {
                PageIndex: res.Data.PageIndex,
                PageCount: res.Data.TotalPages,
                PageSize: res.Data.PageSize,
                TotalCount: res.Data.TotalCount,
            })
        } else {
            ElMessage.error(res.ErrorMsg || '查询失败')
            list.value = []
        }

        setHeight()
    }).finally(() => {
        loading.value = false
    })
}

const tableHeight = ref(408);
const tableContainerRef = ref();
const customTable = ref<TableInstance>();
function setHeight() {
    if (tableContainerRef.value) {
        tableHeight.value = 3000 //撑开一下需要计算高度
        nextTick(() => {
            const h = tableHeight.value;
            let tableH = list.value.length ? ((list.value.length + 1) * 37) + 1 : 408
            tableHeight.value = tableH;

            // if(tableHeight.value>$(tableContainerRef.value)[0].clientHeight){
            tableHeight.value = $(tableContainerRef.value)[0].clientHeight
            // }
            if (h !== tableHeight.value && customTable.value) {
                customTable.value.doLayout();
            }
        })
    }
}

let _resolve = null as any
let _reject = null as any
function open(params: any) {
    let promise = new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
    let campusIDs = currentCampus.value.split(',') || []
    if (showCampus.value && campusIDs.length === 1) {
        condition.value.campus = campusIDs[0];
    }
    dialogVisible.value = true
    multi.value = params.multi || false
    popTitle.value = params.popTitle || '选择考试项'
    choosed.value = params.choosed ? cloneDeep(params.choosed) : [];
    showCampus.value = params.showCampus || false
    showShiftType.value = params.showShiftType || false
    required.value = params.required || false
    maxNum.value = params.maxNum || 0
    if (params.condition) {
        Object.assign(condition.value, params.condition)
    }
    //需要记录下来设置的condition，以免重置以后有问题
    Object.assign(newCondition.value, cloneDeep(condition.value))
    if (multi.value) {
        selected.value = choosed.value ? cloneDeep(choosed.value) : [];
        let ids: any = [];
        selected.value.forEach((item: any) => {
            ids.push(item.ID);
        })
        selectedID.value = ids;
    }
    funcQuery();
    return promise;
}


defineExpose({
    open,
})
</script>

<style lang="scss" scoped>
.chooseCourse {
    .box-wrapper-table {
        display: flex;
        height: 506px !important;
    }

    .fixed-table-box {
        flex: 1;
    }

    .multi-choosed-box-wrapper {
        .fixed-table-box {
            margin-right: 12px;
            width: calc(100% - 306px) !important;
        }
    }

    .single-choosed-wrapper {
        text-align: left;
        width: calc(100% - 150px) !important;
        height: 32px;
        display: table;
    }

    .single-choosed-name {
        vertical-align: middle;
        display: table-cell;
    }


    .advanced-filter-wrap {
        .filter-item {
            width: 262px;

            .el-select {
                width: 100%;
            }
        }
    }
}

.choose-exam-item {
    .choosed-result-wrapper {
        .choosed-result {

            .choosed-result-content {
                ul {
                    li {
                        span.choosed-item-name {
                            width: calc(100% - 70px);
                        }

                        span.choosed-item-status {
                            width: 70px;
                            display: flex;
                            align-items: center;
                            justify-content: flex-end;
                            span.deleted-tag {
                                color: #f56c6c;
                                font-size: 10px;
                                margin-left: 4px;
                                margin-right: 6px;
                            }
                        }
                    }
                }

                
            }
        }
    }

}
</style>