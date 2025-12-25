import { defineStore } from "pinia";
import { queryDepart,getAreaList,queryWithEmployeeRight,queryAllCampus } from '../api';

interface IDepartModel {
    Address: string,
    AreaId: string,
    AreaName: string,
    CategoryName: string,
    ClassRoomCount: number,
    EmployeeCount: number,
    ID: string,
    IsBindingDingTalk: 0,
    IsCampus: 0 | 1,
    IsEnable: number,
    Leader: string,
    LeaderList: string,
    LevelName: string,
    LevelString: string,
    Name: string,
    ShiftCount: 0,
    Tel: string,
    Type: string,
    TypeName: string,
    Visiable: number,
    parentId: string,
}

interface IAreaModel{
    ID:string,
    Name:string,
    CampusCount:number,
    CreateTime:string,
    Describe:string,
    ParentId:string
}

interface ICampusModel{
    ID:string,
    Name:string,
    Pinyin:string,
    IsEnable:number,
    AreaID?:string
}

interface IDepartmentTreeNode {
    ID: string,
    Name: string,
    PID: string,
    IsCampus: number,
    Type: number,
    Children: IDepartmentTreeNode[],
    value: string,
    label: string,
}

let _departListLoaded = false,
    _areaListLoaded = false,
    _campusListLoaded = false,
    _departTreeLoaded = false;

/**
 * 用来存组织架构下的 部门，区域，员工等
 */
export const useOrganizationStore = defineStore('companyOrganization', {
    state: () => {
        return {
            _departList: [] as IDepartModel[],
            _areaList: [] as IAreaModel[],
            _departEmps:null as any,
            _departEmpUser: null as any,
            _operateCampusEmps: null as any,
            _campusList:[] as ICampusModel[],
            _departTree: [] as IDepartmentTreeNode[],
        }
    },
    getters: {
        departList(state){
            if (!_departListLoaded) {
                // @ts-ignore
                this.fetchDepartsList();
            } 
            return (state._departList || []) as IDepartModel[];
        },
        areaList(state){
            if (!_areaListLoaded) {
                // @ts-ignore
                this.fetchAreaList();
            } 
            return (state._areaList || []) as IAreaModel[];
        },
        departEmps(state){
            if (!state._departEmps) {
                // @ts-ignore
                this.fetchDepartEmps();
            } 
            return state._departEmps || {};
        },
        departEmpUser(state){
            if (!state._departEmpUser) {
                // @ts-ignore
                this.fetchDepartEmpUser();
            } 
            return state._departEmpUser || {};
        },
        // 根据指定校区查询员工
        operateCampusEmps(state) {
            return (campusIds: string) => {
                if (!state._operateCampusEmps) {
                    // @ts-ignore
                    this.fetchOperateCampusEmps(campusIds);
                }
                return state._operateCampusEmps || {};
            }
        },
        campusList(state){
            if (!_campusListLoaded) {
                // @ts-ignore
                this.fetchCampusList();
            } 
            return (state._campusList || []) as ICampusModel[];
        },
        enabledCampuses(state){
            if (!_campusListLoaded) {
                // @ts-ignore
                this.fetchCampusList();
            } 
            return (state._campusList.filter(i=>{return i.IsEnable}) || []) as ICampusModel[];
        },
        departTree(state){
            if (!_departTreeLoaded) {
                // @ts-ignore
                this.fetchDepartTree();
            } 
            return (state._departTree || []) as IDepartmentTreeNode[];
        }
    },
    actions: {
        async fetchDepartsList() {
            const res = await queryDepart({});
            this._departList = res.Data;
            _departListLoaded = true;
        },
        async fetchAreaList() {
            const res = await getAreaList({
                query: '',
                PageSize: 999
            });
            this._areaList = res.Data;
            _areaListLoaded = true;
        },
        async fetchDepartEmps(){
            const res = await queryWithEmployeeRight({
                isuser: 0,
                // isContainOut: 1, // 现在默认都改成不包含离职员工，如果后续有需求再加
            })
            this._departEmps=res.Data
        },
        async fetchDepartEmpUser(){
            const res = await queryWithEmployeeRight({
                isuser: 1,
            })
            this._departEmpUser = res.Data;
        },
        async fetchOperateCampusEmps(campusIds: string) {

        },
        async fetchCampusList(){
            const res = await queryAllCampus({})
            this._campusList=res.Data
            _campusListLoaded = true;
        },
        /**
         * 构建部门树节点（使用 departList，只包含部门）
         */
        buildDepartmentTreeNodeFromDepartList(list: IDepartModel[]): IDepartmentTreeNode[] {
            const departmentMap = new Map<string, IDepartmentTreeNode>();

            (list || []).forEach((department:any) => {
                const node: IDepartmentTreeNode = {
                    ID: department.ID,
                    Name: department.Name,
                    PID: (department as any).PID ?? department.parentId ?? '',
                    IsCampus: department.IsCampus as any,
                    Type: (department as any).Type as any,
                    Children: [],
                    value: department.ID,
                    label: department.Name,
                }
                departmentMap.set(department.ID, node);
            });

            const tree: IDepartmentTreeNode[] = [];
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
        },
        /**
         * 获取部门树数据（基于 departList 构建）
         */
        async fetchDepartTree() {
            if (!_departListLoaded) {
                await this.fetchDepartsList();
            }
            this._departTree = this.buildDepartmentTreeNodeFromDepartList(this._departList);
            _departTreeLoaded = true;
            return this._departTree;
        }
    }
})