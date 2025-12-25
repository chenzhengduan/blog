import { queryCustomForm } from '../api';
import { defineStore } from 'pinia';
import { IFieldsParams } from '@/types/fields';
import { IFieldsModel } from '@/types/fields';

interface ICustomFields {
    [key: string]: IFieldsModel[]
}

export const useCustomFieldsStore = defineStore('customFields', {
    state: () => {
        return {
            _customFields: {} as ICustomFields,
        }
    },
    getters: {
        customFields(state) {
            return (params: IFieldsParams) => {
                if (!state._customFields[params.table]) {
                    // @ts-ignore
                    this.fetchFieldsByType(params);
                }
                return (state._customFields[params.table] || []) as IFieldsModel[];
            }
        },
    },
    actions: {
        async fetchFieldsByType(params: IFieldsParams) {
            const res = await queryCustomForm(params);
            if (!this._customFields[params.table]) {
                this._customFields[params.table] = [];
            }
            this._customFields[params.table] = res.Data;
        },
        resetAll(){
            this.$reset()
        }
    },
})
