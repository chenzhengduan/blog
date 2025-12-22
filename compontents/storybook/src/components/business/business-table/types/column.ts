/**
 * 列配置类型定义
 * 支持不同的单元格类型和编辑器配置
 */

import { Component } from 'vue'

/**
 * 单元格类型枚举
 */
export enum CellType {
  TEXT = 'text',           // 普通文本
  SELECT = 'select',       // 下拉选择
  INPUT = 'input',         // 输入框
  DATE = 'date',          // 日期选择
  NUMBER = 'number',      // 数字输入
  CHECKBOX = 'checkbox',  // 复选框
  CUSTOM = 'custom'       // 自定义组件
}

/**
 * 下拉选择器配置
 */
export interface SelectEditorConfig {
  options: Array<{
    label: string
    value: any
  }>
  placeholder?: string
  clearable?: boolean
  filterable?: boolean
  multiple?: boolean
}

/**
 * 输入框配置
 */
export interface InputEditorConfig {
  placeholder?: string
  maxLength?: number
  type?: 'text' | 'textarea' | 'password'
}

/**
 * 日期选择器配置
 */
export interface DateEditorConfig {
  format?: string
  type?: 'date' | 'datetime' | 'daterange'
  placeholder?: string
}

/**
 * 数字输入配置
 */
export interface NumberEditorConfig {
  min?: number
  max?: number
  step?: number
  precision?: number
}

/**
 * 自定义编辑器配置
 */
export interface CustomEditorConfig {
  component: Component
  props?: Record<string, any>
}

/**
 * 编辑器配置联合类型
 */
export type EditorConfig =
  | SelectEditorConfig
  | InputEditorConfig
  | DateEditorConfig
  | NumberEditorConfig
  | CustomEditorConfig

/**
 * 列配置接口
 */
export interface ColumnConfig {
  // 基础配置
  field: string                    // 字段名
  title: string                    // 列标题
  width?: number | string          // 列宽度
  minWidth?: number               // 最小宽度
  fixed?: 'left' | 'right'        // 固定列

  // 单元格配置
  cellType: CellType              // 单元格类型
  editorConfig?: EditorConfig     // 编辑器配置

  // 显示配置
  visible?: boolean               // 是否可见
  sortable?: boolean              // 是否可排序
  resizable?: boolean             // 是否可调整大小

  // 格式化
  formatter?: (value: any, row: any) => string  // 值格式化函数

  // 校验
  validator?: (value: any) => boolean | string  // 值校验函数

  // 自定义渲染
  customRender?: Component        // 自定义渲染组件
}

/**
 * 表格配置接口
 */
export interface TableConfig {
  columns: ColumnConfig[]         // 列配置数组
  height?: number | string        // 表格高度
  maxHeight?: number | string     // 最大高度
  stripe?: boolean                // 斑马纹
  border?: boolean                // 边框
  showHeader?: boolean            // 显示表头
  highlightCurrentRow?: boolean   // 高亮当前行
  rowKey?: string                 // 行数据的 Key
}
