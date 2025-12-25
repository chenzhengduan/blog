import { queryDictByType } from '../api';
import { defineStore } from 'pinia';
import { IDictFieldsModel } from '@/types/fields';

interface ICustomFields {
    [key: string]: IDictFieldsModel[]
}

export const useDictFieldsStore = defineStore('dictFields', {
    state: () => {
        return {
            _dictMap: {} as ICustomFields,
        }
    },
    getters: {
        dictFields(state) {
            return (type: string, status?: number) => {
                if (!state._dictMap[type]) {
                    // @ts-ignore
                    this.fetchFieldsByType(type);
                }
                let list=(state._dictMap[type] || []) as IDictFieldsModel[];
                if (typeof status === 'undefined') {
                    return list;
                }else{
                    return list.filter((i:any)=>i.Status==status);
                }
                
            }
        },
    },
    actions: {
        async fetchFieldsByType(type: string) {
            const res = await queryDictByType(type);
            if (!this._dictMap[type]) {
                this._dictMap[type] = [];
            }
            let data = res.Data;
            data.forEach(item => {
                if (!item.Name) {
                    item.Name = item.Value;
                }
            })
            this._dictMap[type] = data;
        }
    },
})



/**
 * 用来课程期段 过滤了已删除的、停用的
 */
export const useShiftSpec = defineStore('shiftSpec', {
    state: () => {
        return {
            _shiftSpec:null as any
        }
    },
    getters: {
        shiftSpec(state){
            if (!state._shiftSpec) {
                // @ts-ignore
                this.fetchShiftSpec();
            } 
            return state._shiftSpec || {};
        },
    },
    actions: {
        async fetchShiftSpec() {
            const res = await queryDictByType('SHIFT_SPEC',1);
            let data = res.Data;
            data.forEach(item => {
                if (!item.Name) {
                    item.Name = item.Value+(item.Status == 2 ? '[已停用]' : '');
                }
            })
            data.sort(function(a,b){
                return a.Status - b.Status;
            })
            this._shiftSpec = data;
        }
    }
})
