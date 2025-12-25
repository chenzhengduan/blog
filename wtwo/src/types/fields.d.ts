export interface IFieldsParams {
    status: 0 | 1,
    type: number,
    table: string
}

export interface IFieldsModel {
    Fields: string,
    FieldType: number,
    ID: string,
    Name: string,
    Required: boolean,
    SelectItem: string,
    Status: number
}

export interface IDictFieldsModel {
    ID: string,
    Name: string,
    Value: string,
    Describe: string,
    Status: number,
    IsSysDefine: 0 | 1,
}