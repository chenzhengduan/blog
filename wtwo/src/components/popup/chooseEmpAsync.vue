<template>
    <el-dialog
        v-model="isShow"
        class="chooseEmpAsync"
        model-class="comm-drawer-wrapper"
        :title="popTitle"
        :width="multi ? '992px' : '668px'"
		:destroy-on-close="true"
        :align-center="true"
        :close-on-click-modal="false" draggable
        :append-to-body="true"
        @opened="handleOpened"
        @close="close"
    >

        <div class="box-wrapper" :class="{'multi-choosed-box-wrapper': multi}">
            <div class="fixed-table-box">
                <div class="modal-filter">
					<div class="filter-item tree-node-type">
						<span
							class="btn-item"
							@click="changeView('部门')"
							:class="{ active: treeType == '部门' ,'is-disabled': loading}"
							>部门</span
						>
						<span
							class="btn-item"
							@click="changeView('年级')"
							:class="{ active: treeType == '年级' ,'is-disabled': loading}"
							>{{ transToConfigDescript('年级') }}</span
						>
						<span
							class="btn-item"
							@click="changeView('科目')"
							:class="{ active: treeType == '科目' ,'is-disabled': loading}"
							>科目</span
						>
					</div>
                    <div class="filter-item w-266px">
                        <el-input 
                            v-model.trim="query" 
                            class="class-input" 
                            maxlength="50"
                            :placeholder="'输入员工或' + treeType + '名称'" 
                            @keyup.native.enter="searchEmp"
                            ref="searchInputRef"
                        ></el-input>
                    </div>
                    <div class="filter-item">
                        <el-button type="primary" @click="searchEmp" :disabled="loading">查询</el-button>
                    </div>
					<div class="filter-item" v-if="showContainOut">
						<el-checkbox v-model="isContainOut" @change="changeContainOutStatus">已离职</el-checkbox>
					</div>
				</div>

				<pageAttentionTips class="ml-16px! mb-4px!">{{ treeType == '部门' ? transToConfigDescript("按校区或部门选择员工") : treeType == "年级" ? "按授课年级选择员工" : treeType == "科目" ? "按授课科目选择员工" : "" }}</pageAttentionTips>

                <el-scrollbar ref="scrollbarRef" class="table-wrap" v-loading="loading">
                    <el-tree
                        ref="treeRef"
                        :lazy="true"
                        :load="loadNode"
                        :props="treeProps"
                        :show-checkbox="multi"
						:default-expanded-keys="expandNodes"
                        :highlight-current="true"
                        :check-on-click-leaf="multi"
						node-key="nodeId"
                        @node-click="handleNodeClick"
                        @check="handleCheck"
                    >

                        <template #default="{ node, data }">
                            <span class="depart-tree-node flex-center">
                                <el-icon class="fe-icon" v-if="data.isCompany">
                                    <companySVG></companySVG>
                                </el-icon>
                                <el-icon class="fe-icon" v-if="treeType == '部门' && !data.isCompany && !data.isEmp">
                                    <campusSVG v-if="!data.isCompany"></campusSVG>
                                </el-icon>
                                <el-icon class="fe-icon" v-if="treeType!='部门' && !data.isCompany && !data.isEmp">
                                    <fileOpenSVG v-if="!data.isCompany&&node.expended"></fileOpenSVG>
                                    <fileCloseSVG v-if="!data.isCompany&&!node.expended"></fileCloseSVG>
                                </el-icon>
                                <span>{{ data.isEmp ? getSerialAndPosition(data) : node.label }}</span>
                            </span>
                        </template>

                    </el-tree>
                </el-scrollbar>
            </div>

            <div class="choosed-result-wrapper" v-if="multi">
				<div class="choosed-result">
					<div class="choosed-result-title">
						<span>已选择：{{ multiChoosed.length }}</span>
                        <el-link type="primary" underline="never" @click="removeAll">清空</el-link>
					</div>
					<div class="choosed-result-content">
						<ul>
							<li v-for="(item, index) in multiChoosed" :key="item.ID">
								<span class="choosed-item-name" :title="item.Name">{{ getSerialAndPosition(item) }}</span>
                                <a class="del-btn" @click="remove(item,index)"><el-icon><deleteSVG></deleteSVG></el-icon></a>
							</li>
						</ul>
					</div>
				</div>
			</div>
        </div>

        <template #footer>
            <div class="flex-between">
                <div class="flex-center" style="width: calc(100% - 150px);">
                    <div class="single-choosed-wrapper mr-10px!" v-if="!multi">
                        <span class="single-choosed-name">已选择：{{oneChoosed.ID ? getSerialAndPosition(oneChoosed) : ''}}</span>
                    </div>
                    <el-select v-if="EnableClassCommission&&showTeacherType" v-model="teacherType" :placeholder="transToConfigDescript('老师类型')" class="w-120px!">
                        <el-option v-for="item in teacherTypeList" :key="item.ID" :label="item.Name" :value="item.ID"></el-option>
                    </el-select>
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
import { computed, nextTick, ref, getCurrentInstance } from 'vue';
import { CheckboxValueType, ScrollbarInstance, TreeInstance, InputInstance } from 'element-plus';
import { storeToRefs } from 'pinia';
import Node from 'element-plus/es/components/tree/src/model/node';
import { transToConfigDescript } from '@/utils/filters/filters';

import companySVG from '@/assets/svg/company.svg'
import campusSVG from '@/assets/svg/campus.svg'
import fileOpenSVG from '@/assets/svg/file-open.svg'
import fileCloseSVG from '@/assets/svg/file-close.svg'
import deleteSVG from '@/assets/svg/delete.svg'

import { useConfigs, useUser } from '@/store';
import { useDictFieldsStore } from '@/store/dict';

import { queryDepartEmployee, modalQueryEmployee, multiSelEmployee } from '@/api/dept';
import { TreeOptionProps } from 'element-plus/es/components/tree/src/tree.type';
import { cloneDeep } from 'lodash';
import { queryTeacherCommissionSetting } from '@/api';

const configs=computed(()=>{
    return useConfigs().configs
})

const EnableClassCommission=computed(()=>{
    return configs.value.EnableClassCommission==1
})

const popTitle=ref('选择员工')
const multi=ref(false)
const disabledIds = ref<string[]>([]);
const isLoginUser=ref(0 as number)
const showContainOut=ref(false)
const showTeacherType=ref(false)
interface IDictFields {
    ID: string,
    Name: string,
}
interface IDepartment {
    ID: string,
    Name: string,
    PID: string,
    IsCampus: number,
    Type: number,
    Children: (IDepartment | IEmployee)[],
    isCompany?: boolean,
    nodeId: string,
}
interface IEmployee {
    ID: string,
    Name: string,
    DepartID: string,
    DepartName: string,
    GradeID: string,
    GradeName: string,
    GradeList: IDictFields[],
    SubjectID: string,
    SubjectName: string,
    SubjectList: IDictFields[],
    PositionID: string,
    PositionName: string,
    Serial: string,
    
    leaf: boolean,
    isEmp: boolean,
    nodeId: string,
}
interface ITreeData {
    DepartList: IDepartment[],
    EmployeeList: IEmployee[],
}

type ResolveFunction = (data: (IDepartment | IEmployee)[]) => void;
interface ILoadNode {
    node: Node,
    resolve: ResolveFunction,
}

const instance = getCurrentInstance();
const globalData = instance?.appContext.config.globalProperties.$global;
const users = computed(() => useUser().user);
const dictStore = useDictFieldsStore();
const { dictFields } = storeToRefs(dictStore);

const subjectList = computed(() => {
    return dictFields.value('SUBJECT');
})
const gradeList = computed(() => {
    return dictFields.value('SHIFT_GRADE');
})

const teacherTypeList=ref([] as any)
const teacherType=ref('')

const isShow = ref(false);
const loading = ref(false);
const query = ref('');
const treeType = ref('部门');
const isContainOut = ref(false);
const condition=ref({} as any)
const expandNodes = ref([] as string[]);
const oneChoosed = ref({} as IEmployee);
const multiChoosed = ref([] as IEmployee[]);
const treeRef = ref<TreeInstance>();
const scrollbarRef = ref<ScrollbarInstance>()
const searchInputRef = ref<InputInstance>();
const savedData: ITreeData = {
    DepartList: [],
    EmployeeList: [],
}
const treeTypeCode = computed(() => {
    return treeType.value === '部门' ? 'Depart' : treeType.value === '年级' ? 'Grade' : 'Subject';
})
const firstNode:ILoadNode = {
    node: {} as Node,
    resolve: () => {}
}
const customNodeClass = (data: (IDepartment | IEmployee), node: Node) => {
    if (query.value !== '' && data.Name.indexOf(query.value) !== -1) {
        return 'is-highlight';
    }
    return null;
}
const setDisabledNode = (data: IEmployee, node: Node) => {
    if (data.isEmp && disabledIds.value.includes(data.ID)) {
        return true;
    }
    return false;
}
const treeProps = ref({
    label: 'Name',
    isLeaf: 'leaf',
    children: 'Children',
    class: customNodeClass,
    disabled: setDisabledNode,
} as TreeOptionProps)

const changeContainOutStatus = (val: CheckboxValueType) => {
    const rootNode = treeRef.value!.getNode(users.value.CompanyID);
    rootNode.loaded = false;
    rootNode.expand();
}

const loadNode = (node: Node, resolve: ResolveFunction) => {
    if (node.level === 1) {
        firstNode.node = node;
        firstNode.resolve = resolve;
    }
    switch(treeType.value) {
        case '部门':
            loadDepartTree(node, resolve);
            break;
        case '年级':
            loadOtherTree(1, node, resolve);
            break;
        case '科目':
            loadOtherTree(2, node, resolve);
            break;
        default:
            break;
    }

    if (node.level === 0) {
        expandNodes.value = [users.value.CompanyID];
    }
}
// 加载部门员工树
async function loadDepartTree(node: Node, resolve: (data: (IDepartment | IEmployee)[]) => void) {
    if (node.level === 0) {
        return resolve([{
            ID: users.value.CompanyID,
            Name: users.value.CompanyName,
            PID: '',
            Type: 1,
            IsCampus: 0,
            Children: [],
            isCompany: true,
            nodeId: users.value.CompanyID,
        }])
    }else if(node.level === 1) {
        const params={
            isuser: isLoginUser.value,
            isContainOut: +isContainOut.value 
        }
        Object.assign(params,condition.value)
        const res = await queryDepartEmployee(params)
        const data = res.Data;
        const treeNode = buildTreeNode(data);
        removeDuplicateData(data);
        treeRef.value!.updateKeyChildren(node.data.ID, treeNode[0]?.Children);
        return resolve(treeNode[0]?.Children);
    }else {
        const params={
            isuser: isLoginUser.value,
            isContainOut: +isContainOut.value ,
            departId: node.data.ID, 
        }
        Object.assign(params,condition.value)
        const res = await queryDepartEmployee(params)
        const data = res.Data;
        const treeNode = buildTreeNode(data);
        removeDuplicateData(data);
        treeRef.value!.updateKeyChildren(node.data.ID, treeNode[0]?.Children);
        return resolve(treeNode[0]?.Children);
    }
}
/**
 * 加载年级员工树
 * @param type 1年级 2科目
 * @param node 
 * @param resolve 
 */
async function loadOtherTree(type: number, node: Node, resolve: (data: (IDepartment | IEmployee)[]) => void) {
    if (node.level === 0) {
        return resolve([{
            ID: users.value.CompanyID,
            Name: users.value.CompanyName,
            PID: '',
            Type: 1,
            IsCampus: 0,
            Children: [],
            isCompany: true,
            nodeId: users.value.CompanyID,
        }])
    }else if(node.level === 1) {
        const _list = buildOtherTreeNode(type);
        treeRef.value!.updateKeyChildren(node.data.nodeId, _list);
        return resolve(_list);
    }else {
        const params={
            isuser: isLoginUser.value,
            isContainOut: +isContainOut.value ,
            queryItemId: node.data.ID,
            queryType: type === 1 ? 'Grade' : 'Subject',
        }
        Object.assign(params,condition.value)
        const res = await modalQueryEmployee(params)
        const empList = res.Data.EmployeeList.map((item:any) => {
            return {
                ...item,
                leaf: true,
                isEmp: true,
            }
        })
        treeRef.value!.updateKeyChildren(node.data.nodeId, empList);
        return resolve(empList);
    }
}

function buildTreeNode(data: ITreeData): IDepartment[] {
    const departmentMap = new Map<string, IDepartment>();

    // 将部门数组转换为 Map，key 为部门 ID
    data.DepartList.forEach(department => {
        departmentMap.set(department.ID, { ...department, Children: [], nodeId: department.ID });
    });

    // 将员工分配到对应的部门
    data.EmployeeList.forEach(employee => {
        employee.leaf = true;
        employee.isEmp = true;
        employee.nodeId = employee.DepartID + '|' + employee.ID;
        if (departmentMap.has(employee.DepartID)) {
            departmentMap.get(employee.DepartID)!.Children!.push(employee);
        }
    });

    // 创建树形结构
    const tree: IDepartment[] = [];
    departmentMap.forEach(department => {
        if (!departmentMap.has(department.PID)) {
            tree.push(department);
        } else {
            const parent = departmentMap.get(department.PID);
            if (parent) {
                if (!parent.Children) {
                    parent.Children = [];
                }
                parent.Children.push(department);
            }
        }
    });

    return tree;
}

function buildOtherTreeNode(type: number): IDepartment[] {
    let _list = [] as IDepartment[],
        _sourceList = type == 1 ? gradeList.value : subjectList.value;

    _sourceList.forEach((item: IDictFields) => {
        _list.push({
            ID: item.ID,
            Name: item.Name,
            PID: users.value.CompanyID,
            Type: 0,
            IsCampus: 0,
            Children: [],
            isCompany: false,
            nodeId: item.ID,
        })
    })
    _list.push({
        ID: globalData.EMPTYGUID,
        Name: type == 1 ? '<未指定年级>' : '<未指定科目>',
        PID: users.value.CompanyID,
        Type: 0,
        IsCampus: 0,
        Children: [],
        isCompany: false,
        nodeId: globalData.EMPTYGUID,
    })
    return _list;
}

function changeView(type: string) {
    if (loading.value) {
        return;
    }
    loading.value = true;
    treeType.value = type;
    const rootNode = treeRef.value!.getNode(users.value.CompanyID);
    rootNode.loaded = false;
    rootNode.expand();
    loading.value = false;

    oneChoosed.value = {} as IEmployee;
    multiChoosed.value = [];
    const nodes = treeRef.value!.getCheckedNodes();
    nodes.forEach(node => {
        treeRef.value!.setChecked(node.nodeId, false, false);
    })
}

async function searchEmp() {
    const _val = query.value;

    if (_val === '') {
        ElMessage.warning(`请输入员工或${treeType.value}名称`);
        return;
    }
    loading.value=true
    const params={
        isuser: isLoginUser.value,
        isContainOut: +isContainOut.value ,
        query: _val,
        queryType: treeTypeCode.value,
    }
    Object.assign(params,condition.value)
    const res = await modalQueryEmployee(params)
    loading.value=false
    const data = res.Data;

    switch(treeType.value) {
        case '部门':
            searchEmpByDepart(data);
            break;
        case '年级':
            searchEmpByOther(data, 1);
            break;
        case '科目':
            searchEmpByOther(data, 2);
            break;
        default:
            break;
    }
}

function searchEmpByDepart(data: ITreeData) {
    const conbineList = [...data.DepartList, ...data.EmployeeList];
    const sameNode = conbineList.some(item => item.Name.indexOf(query.value) !== -1);
    if (!sameNode) {
        ElMessage.warning(`未找到该员工或${treeType.value}`);
        return;
    }

    // 方法1: 赋值默认需展开的部门ID，弊端是会调很多次接口，并且层级比较深的话搜不出
    // const departIds = result.EmployeeList.filter(emp => {
    //     return emp.Name.indexOf(_val) !== -1;
    // }).map(item => item.DepartID);
    // expandNodes.value = [users.value.CompanyID, ...departIds];

    // 方法2： 使用组件的filter方法，没办法自动展开节点

    // 方法3：手动遍历树执行节点搜索，节点展开，节点定位
    const result: ITreeData = removeDuplicateData(data);
    const treeNode = buildTreeNode(result);
    
    const childList = treeNode[0]?.Children || [];
    // // firstNode.resolve(childList);
    // treeRef.value!.updateKeyChildren(users.value.CompanyID, childList);
    treeNode.forEach(_node => {
        updateTreeNodeChildren(_node);
    })
    // 搜索后反选
    if (multi.value) {
        setTreeNodeCheckedByChoosed();
    }

    let departIds = [] as string[];
    childList.forEach(item => {
        departIds = departIds.concat(findNodeId(item));
    })
    
    // 去重并排序，确保父级节点先展开
    const uniqueDepartIds = [...new Set(departIds)];
    const sortedDepartIds = sortNodesByLevel(uniqueDepartIds);
    
    nextTick(async () => {
        // 按层级顺序展开所有节点
        for (const item of sortedDepartIds) {
            const departNode = treeRef.value!.getNode(item);
            if (departNode) {
                if (departNode.level > 1 && departNode.childNodes.length === 0) {
                    // 查找并加载子节点
                    childList.forEach(_node => {
                        const _result = findNodeChildren(_node, departNode.data.ID);
                        if(!!_result) {
                            treeRef.value!.updateKeyChildren(departNode.data.nodeId, _result.Children);
                        }
                    })
                }
                departNode.loaded = true;
                departNode.expanded = true;
                
                // 等待当前节点完全渲染
                await new Promise(resolve => setTimeout(resolve, 50));
            }
        }
        
        // 使用更长的延迟确保所有节点都已渲染，特别是深层级节点
        setTimeout(() => {
            posHighlightNode();
        }, 200);
    })
}

function searchEmpByOther(data: ITreeData, type: number) {
    if (data.DepartList.length === 0 && data.EmployeeList.length === 0) {
        ElMessage.warning(`未找到该员工或${treeType.value}`);
        return;
    }
    // const _list = buildOtherTreeNode(type);
    // treeRef.value!.updateKeyChildren(users.value.CompanyID, _list);

    nextTick(() => {
        let resultMap = {} as { [key: string]: any };
        data.EmployeeList.forEach(emp => {
            emp.leaf = true;
            emp.isEmp = true;
            
            const sourceList = type === 1 ? emp.GradeList : emp.SubjectList;
            const dictIds = type === 1 ? gradeList.value.map(item => item.ID) : subjectList.value.map(item => item.ID);
            sourceList.forEach(item => {
                const _emp = cloneDeep(emp);
                _emp.nodeId = item.ID + '|' + emp.ID;

                if(dictIds.indexOf(item.ID) !== -1 || item.ID === globalData.EMPTYGUID) {
                    if (!resultMap[item.ID]) {
                        resultMap[item.ID] = [];
                    }
                    resultMap[item.ID].push(_emp);
                }
            })
        })
    
        for(let key in resultMap) {
            const departNode = treeRef.value!.getNode(key);
            treeRef.value!.updateKeyChildren(key, resultMap[key]);
            departNode.loaded = true;
            departNode.expanded = true;
        }
        
        // 在所有节点更新完成后执行滚动
        setTimeout(() => {
            posHighlightNode();
        }, 100);

        // 搜索后反选
        if (multi.value) {
            setTreeNodeCheckedByChoosed();
        }
    })
}

function posHighlightNode() {
    // 使用多重检查确保节点完全渲染
    const attemptScroll = (attempts = 0) => {
        if (attempts > 10) {
            console.warn('无法定位到高亮节点，已达到最大重试次数');
            return;
        }
        
        nextTick(() => {
            requestAnimationFrame(() => {
                const highlightElem = document.querySelectorAll('.chooseEmpAsync .wtwo-tree .is-highlight');
                if (highlightElem.length) {
                    const firstElement = highlightElem[0] as HTMLElement | null;
                    if (firstElement && firstElement.offsetParent !== null) {
                        // 元素已完全渲染，执行滚动
                        const scrollContainer = scrollbarRef.value?.$el?.querySelector('.wtwo-scrollbar__wrap');
                        if (scrollContainer) {
                            // 计算元素相对于滚动容器的位置
                            const containerRect = scrollContainer.getBoundingClientRect();
                            const elementRect = firstElement.getBoundingClientRect();
                            const relativeTop = elementRect.top - containerRect.top + scrollContainer.scrollTop;
                            
                            // 滚动到目标位置，留一些边距
                            const scrollTop = Math.max(0, relativeTop - 50);
                            scrollbarRef.value!.setScrollTop(scrollTop);
                        } else {
                            // 降级方案：使用 offsetTop
                            const _offsetTop = firstElement.offsetTop || 0;
                            scrollbarRef.value!.setScrollTop(Math.max(0, _offsetTop - 50));
                        }
                    } else {
                        // 元素还未完全渲染，延迟重试
                        setTimeout(() => attemptScroll(attempts + 1), 50);
                    }
                } else {
                    // 没有找到高亮元素，延迟重试
                    setTimeout(() => attemptScroll(attempts + 1), 50);
                }
            });
        });
    };
    
    attemptScroll();
}

function findNodeId(item: (IDepartment | IEmployee)) {
    let _ids = [] as string[];
    if (item.Name.indexOf(query.value) !== -1) {
        if ('isEmp' in item && item.isEmp) {
            _ids = _ids.concat(findNodeParentId(item.DepartID));
            _ids.push(item.DepartID);
        }else {
            _ids = _ids.concat(findNodeParentId(item.ID));
            _ids.push(item.ID);
        }
    }
    if ('Children' in item && item.Children.length) {
        item.Children.forEach(childItem => {
            _ids = _ids.concat(findNodeId(childItem));
        })
    }
    return _ids;
}
// 查找节点的父级ID
function findNodeParentId(departId: string) {
    let _parentIds = [] as string[];
    const departList = savedData.DepartList;
    const departNode = departList.find(item => item.ID === departId);
    if (departNode && departNode.PID !== globalData.EMPTYGUID && departNode.PID !== users.value.CompanyID) {
        _parentIds = _parentIds.concat(findNodeParentId(departNode.PID));
        _parentIds.push(departNode.PID);
    }
    return _parentIds;
}

// 按层级排序节点，确保父级节点先展开
function sortNodesByLevel(nodeIds: string[]): string[] {
    const sortedIds: string[] = [];
    const processedIds = new Set<string>();
    
    // 递归函数：获取节点的所有父级ID
    const getAllParentIds = (nodeId: string): string[] => {
        const parentIds: string[] = [];
        const departList = savedData.DepartList;
        const departNode = departList.find(item => item.ID === nodeId);
        
        if (departNode && departNode.PID !== globalData.EMPTYGUID && departNode.PID !== users.value.CompanyID) {
            const grandParentIds = getAllParentIds(departNode.PID);
            parentIds.push(...grandParentIds);
            parentIds.push(departNode.PID);
        }
        return parentIds;
    };
    
    // 为每个节点收集所有父级ID
    const nodeWithParents = nodeIds.map(nodeId => ({
        nodeId,
        parentIds: getAllParentIds(nodeId)
    }));
    
    // 按父级数量排序（父级少的先处理）
    nodeWithParents.sort((a, b) => a.parentIds.length - b.parentIds.length);
    
    // 按顺序添加节点，确保父级先被处理
    nodeWithParents.forEach(({ nodeId, parentIds }) => {
        // 先添加所有父级节点
        parentIds.forEach(parentId => {
            if (!processedIds.has(parentId)) {
                sortedIds.push(parentId);
                processedIds.add(parentId);
            }
        });
        // 再添加当前节点
        if (!processedIds.has(nodeId)) {
            sortedIds.push(nodeId);
            processedIds.add(nodeId);
        }
    });
    
    return sortedIds;
}
// 查找节点的子节点
function findNodeChildren(node: IDepartment | IEmployee, id: string): IDepartment | null {
    if ('Children' in node && node.ID === id) {
        return node;
    }
    if ('Children' in node && node.Children.length) {
        for(const child of node.Children) {
            const foundNode = findNodeChildren(child, id);
            if (foundNode) {
                return foundNode;
            }
        }
    }
    return null;
}

function removeDuplicateData(newData: ITreeData) {
    // 去除部门数据中的重复项
    const uniqueDeparts = newData.DepartList.filter(newDept => {
        return !savedData.DepartList.some(savedDept => newDept.ID === savedDept.ID);
    })

    // 去除员工数据中的重复项
    const uniqueEmps = newData.EmployeeList.filter(newEmp => {
        return !savedData.EmployeeList.some(savedEmp => newEmp.ID === savedEmp.ID && newEmp.DepartID === savedEmp.DepartID);
    })

    // 更新保存的数据
    savedData.DepartList.push(...uniqueDeparts);
    savedData.EmployeeList.push(...uniqueEmps);

    return {
        DepartList: savedData.DepartList,
        EmployeeList: savedData.EmployeeList
    }
}

function getSerialAndPosition(item: IEmployee) {
    const serial = item.Serial ? `[${item.Serial}]` : '';
    const position = item.PositionName ? `[${item.PositionName}]` : '';
    return `${item.Name}${serial}${position}`;
}

// // 节点点击
function handleNodeClick(data: IEmployee) {
    if (!multi.value && data.isEmp && disabledIds.value.indexOf(data.ID) === -1 ) {
        oneChoosed.value = data;
    } 
    // else {
    //     let checkedList = treeRef.value!.getCheckedNodes()
    //     let obj = checkedList.find(emp => emp.ID === data.ID)
    //     if(!!data.isEmp) {
    //         if (obj) {
    //             treeRef.value!.setChecked(data, false, false);
    //         } else {
    //             treeRef.value!.setChecked(data, true, false);
    //         }
    //     }
    //     handleCheck(data, { checkedKeys: treeRef.value!.getCheckedKeys() }, true);
        
    // }
    if (!multi.value) {
        nextTick(() => {
            if (oneChoosed.value) {
                treeRef.value!.setCurrentKey(oneChoosed.value.ID);
            } else {
                treeRef.value!.setCurrentKey();
            }
        })
    }
}


// 节点选中
function handleCheck(data: (IEmployee | IDepartment), checked: any, isParentNode: boolean = false) {
    if (!multi.value) {
        return;
    }
    switch(treeType.value) {
        case '部门':
            handleCheckByDepart(data, checked, isParentNode);
            break;
        case '年级':
            handleCheckByOther(data, checked, 1, isParentNode);
            break;
        case '科目':
            handleCheckByOther(data, checked, 2, isParentNode);
            break;
        default:
            break;
    }
}

async function handleCheckByDepart(data: (IEmployee | IDepartment), checked: any, isParentNode: boolean) {
   
    // 先加载数据
    if (!('isEmp' in data)) {
        const departNode = treeRef.value!.getNode(data.ID);
        if(!departNode.loaded || (!!data.isCompany && departNode.checked)) {
            const res = await multiSelEmployee({
                isuser: isLoginUser.value,
                isContainOut: +isContainOut.value,
                departId: data.ID,
            })
            const treeNode = buildTreeNode(res.Data);
            removeDuplicateData(res.Data);
            // console.log(departNode)
            // 更新一级节点
            treeNode.forEach(_node => {
                updateTreeNodeChildren(_node);
            })
            nextTick(() => {
                !isParentNode && setCheckedStatus(data, true);
                // setChoosedList(res.Data.EmployeeList)
            })
        }else {
            const res = await multiSelEmployee({
                isuser: isLoginUser.value,
                isContainOut: +isContainOut.value,
                departId: data.ID,
            })
            res.Data.EmployeeList.forEach((i:any)=>{
                let existIndex=multiChoosed.value.findIndex((j:any)=>i.ID==j.ID)
                if(existIndex!=-1){
                    multiChoosed.value.splice(existIndex,1)
                }
            })
            !isParentNode && setCheckedStatus(data, checked.checkedKeys.includes(data.nodeId));
        }
        return;
    }
    let existIndex=multiChoosed.value.findIndex((j:any)=>data.ID==j.ID)
    if(existIndex!=-1){
        multiChoosed.value.splice(existIndex,1)
    }
    setCheckedStatus(data, checked.checkedKeys.includes(data.nodeId));
}

async function handleCheckByOther(data: (IEmployee | IDepartment), checked: any, type: number, isParentNode: boolean) {
    if (!('isEmp' in data)) {
        const departNode = treeRef.value!.getNode(data.ID);
        if (!departNode.loaded || (!!data.isCompany && departNode.checked)) {
            const params={
                isuser: isLoginUser.value,
                isContainOut: +isContainOut.value ,
                queryItemId: data.ID,
                queryType: type === 1 ? 'Grade' : 'Subject',
                isSelectAll: !!data.isCompany ? 1 : 0,
            }
            Object.assign(params,condition.value)
            const res = await modalQueryEmployee(params)
            const empList: IEmployee[] = res.Data.EmployeeList;
            
            if (!!data.isCompany && departNode.checked) {
                const _empList = cloneDeep(empList);
                let resultMap = {} as { [key: string]: any };

                _empList.forEach(emp => {
                    emp.isEmp = true;
                    emp.leaf = true;

                    const sourceList = type === 1 ? emp.GradeList : emp.SubjectList;
                    const dictIds = type === 1 ? gradeList.value.map(item => item.ID) : subjectList.value.map(item => item.ID);
                    sourceList.forEach(item => {
                        const _emp = cloneDeep(emp);
                        _emp.nodeId = item.ID + '|' + emp.ID;

                        if(dictIds.indexOf(item.ID) !== -1 || item.ID === globalData.EMPTYGUID) {
                            if (!resultMap[item.ID]) {
                                resultMap[item.ID] = [];
                            }
                            resultMap[item.ID].push(_emp);
                        }
                    })
                    console.log(resultMap)
                })
                for(let key in resultMap) {
                    const treeNode = treeRef.value!.getNode(key);
                    treeRef.value!.updateKeyChildren(key, resultMap[key]);
                    treeNode.loaded = true;
                }
            }else {
                const _empList = cloneDeep(empList);
                _empList.forEach((item: IEmployee) => {
                    item.isEmp = true;
                    item.leaf = true;
                    item.nodeId = data.ID + '|' + item.ID;
                });
                treeRef.value!.updateKeyChildren(data.nodeId, _empList);
                departNode.loaded = true;
            }

            nextTick(() => {
                !isParentNode && setCheckedStatus(data, true);
            })
        }else {
            !isParentNode && setCheckedStatus(data, checked.checkedKeys.includes(data.nodeId));
        }
        return;
    }
    setCheckedStatus(data, checked.checkedKeys.includes(data.nodeId));
}

function updateTreeNodeChildren(asyncTreeData: IDepartment){
    treeRef.value!.updateKeyChildren(asyncTreeData.ID, asyncTreeData.Children);
    const treeNode = treeRef.value!.getNode(asyncTreeData.ID);
    if(treeNode){
        treeNode.loaded = true;
    }
    

    if(asyncTreeData.Children.length) {
        asyncTreeData.Children.forEach(child => {
            if('Children' in child) {
                updateTreeNodeChildren(child);
            }
        })  
    }
}

function setTreeNodeCheckedByChoosed() {
    multiChoosed.value.forEach(emp => {
        treeRef.value!.setChecked(emp.nodeId, true, false);
    })
}


function setCheckedStatus(data: (IEmployee | IDepartment), checked: boolean) {
    checkRepeat(data, checked);
    
    let checkedList = treeRef.value!.getCheckedNodes(),
        arr = [] as IEmployee[];

    checkedList.forEach((item: any) => {
        if (item.isEmp) {
            const exist = arr.find((emp) => emp.ID === item.ID);
            const chooseExist = multiChoosed.value.find(emp => emp.ID === item.ID);

            if (!exist && !chooseExist) {
                arr.push(item)
            }
        }
    })
    multiChoosed.value.push(...arr);
}

function checkRepeat(data: (IEmployee | IDepartment), checked: boolean) {
    if (!('isEmp' in data)) {
        data.Children.forEach(function (child) {
            checkRepeat(child, checked);
        })
    } else {
        if (treeType.value === '部门') {
            savedData.EmployeeList.forEach(emp => {
                if (emp.ID === data.ID) {
                    const _nodeId = emp.DepartID + '|' + emp.ID;
                    treeRef.value!.setChecked(_nodeId, checked, false);
                }
            })
            return;
        }
        const sourceList = treeType.value === '年级' ? data.GradeList : data.SubjectList;
        const dictIds = treeType.value === '年级' ? gradeList.value.map(item => item.ID) : subjectList.value.map(item => item.ID);
        sourceList.forEach(item => {
            if (dictIds.indexOf(item.ID) !== -1 || item.ID === globalData.EMPTYGUID) {
                const _nodeId = item.ID + '|' + data.ID;
                treeRef.value!.setChecked(_nodeId, checked, false);
            }
        })
    }
}

function removeAll() {
    multiChoosed.value = [];
    const nodes = treeRef.value!.getCheckedNodes();
    nodes.forEach(node => {
        treeRef.value!.setChecked(node.nodeId, false, false);
    })
}

function remove(item: IEmployee, index: number) {
    multiChoosed.value.splice(index, 1);
    const nodes = treeRef.value!.getCheckedNodes();
    nodes.forEach(node => {
        if (node.ID == item.ID) {
            treeRef.value!.setChecked(node.nodeId, false, false);
        }
    })
}

function submit() {
    if (multi.value) {
        if (multiChoosed.value.length === 0) {
            ElMessage.warning('请选择员工');
            return; 
        }
        let obj:any={data: multiChoosed.value}
        if(EnableClassCommission.value&&showTeacherType.value){
            obj.other={
                ID:teacherType.value,
                Name:teacherTypeList.value.find((item:any)=>item.ID===teacherType.value)?.Name||''
            }
        }else{
            obj.other={
                ID:'',
                Name:''
            }
        }
        _resolve(obj);
        isShow.value = false;
        return;
    }
    // 单选
    if (Object.keys(oneChoosed.value).length === 0) {
        ElMessage.warning('请选择员工');
        return;
    }
    let obj:any={data: oneChoosed.value}
    if(EnableClassCommission.value&&showTeacherType.value){
        obj.other={
            ID:teacherType.value,
            Name:teacherTypeList.value.find((item:any)=>item.ID===teacherType.value)?.Name||''
        }
    }else{
        obj.other={
            ID:'',
            Name:''
        }
    }
    _resolve(obj);
    isShow.value = false;
}

function getTeacherType(){
    queryTeacherCommissionSetting().then((res:any)=>{
        teacherTypeList.value = res.Data || [];
    })
}

function close() {
    treeType.value =  '部门';
    query.value = '';
    isContainOut.value = false;
    oneChoosed.value = {} as IEmployee;
    multiChoosed.value = [];
    condition.value={}
    savedData.DepartList = [];
    savedData.EmployeeList = [];
    teacherTypeList.value = [];
    teacherType.value = '';
    isShow.value = false;
    _reject();
}

let _resolve = null as any,
    _reject = null as any;
function open(params: any){
    popTitle.value=params.popTitle||'选择员工'
    disabledIds.value=params.disabledIds||[];
    multi.value = params.multi||false;
    isLoginUser.value = params.isLoginUser||0;
    isShow.value = true;
    // 聚焦放到对话框完全打开后处理（@opened 触发）
    isContainOut.value=params.isContainOut||false;
    showContainOut.value=params.showContainOut||false;  
    showTeacherType.value=params.showTeacherType||false;
    if(showTeacherType.value){
        getTeacherType()
    }
    if(params.condition){
        Object.assign(condition.value,params.condition)
    }
    let selected = params.selected||[];
    if (multi.value) {
        multiChoosed.value = cloneDeep(selected) as IEmployee[];
    }
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    })
}
function handleOpened() {
    nextTick(() => {
        if (searchInputRef.value && typeof searchInputRef.value.focus === 'function') {
            searchInputRef.value.focus();
        } else {
            (searchInputRef.value as any)?.input?.focus?.();
        }
    })
}
defineExpose({
    open
})

</script>

<style lang="scss" scoped>
.chooseEmpAsync {
	.modal-body {
		overflow: hidden !important;
	}
	.box-wrapper{
        height: 506px!important;
		.fixed-table-box {
			height: 100%;
		}
    }
    .table-wrap {
        width: 630px!important;
        max-height:484px;
        overflow-y: auto!important;;
    }
    .multi-choosed-box-wrapper{
        .fixed-table-box{
            margin-right: 12px;
            width: calc(100% - 306px)!important;
        }
        .modal-filter{
			padding: 12px 0 0 12px!important;
		}
    }
	.modal-filter {
		.filter-item {
			margin-bottom: 8px;
		}
	}
	

	.tree-node-type {
		display: flex;
		.btn-item {
			display: inline-block;
			width: 60px;
			border: 1px solid #dcdfe6;
			text-align: center;
			height: 32px;
			line-height: 30px;
			cursor: pointer;
            &.is-disabled{
                cursor: not-allowed;
            }
			&:first-child {
				border-radius: 4px 0 0 4px;
				border-right: none;
			}
			&:last-child {
				border-radius: 0 4px 4px 0;
				border-left: none;
			}
			&.active {
				border: 1px solid #2878e8;
				color: #2878e8;
			}
		}
	}
	.wtwo-tree {
		:deep(.wtwo-tree-node__content) {
			height: 40px;
			border-radius: 4px;
		}
		:deep(.wtwo-tree-node__content > .wtwo-tree-node__expand-icon) {
			margin-left: 10px;
		}
		:deep(.wtwo-tree-node__expand-icon) {
			color: #606266;
		}
		:deep(.wtwo-tree-node__expand-icon.is-leaf) {
			color: transparent;
		}
        :deep(.wtwo-tree-node) {
            &.is-highlight {
                &>.wtwo-tree-node__content {
                    color: #F56C6C;
                    font-weight: 500;
                }
            }
        }
        :deep(.wtwo-tree-node__children) {
            .wtwo-tree-node {
                &.is-highlight {
                    &>.wtwo-tree-node__content {
                        color: #F56C6C;
                        font-weight: 500;
                    }
                }
            }
        }
	}

	.depart-tree-node {
		.fe-icon {
			width: 14px;
			height: 14px;
			margin-right: 5px;
		}
		&.is-disabled {
			color: #d8d8d8;
			cursor: not-allowed;
		}
	}
	.single-choosed-wrapper {
		height: 32px;
		display: table;
        text-align: left;
        max-width: 100%;
	}
	.single-choosed-name {
		vertical-align: middle;
		display: table-cell;
	}
}
</style>