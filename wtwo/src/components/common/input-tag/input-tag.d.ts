// Input Tag 组件相关类型定义

export interface ApiConfig {
    apiFunction: (params: any) => Promise<any>;  // API 函数，如 queryShift
    params?: Record<string, any>;                // 固定参数
    searchParam?: string;                        // 搜索参数名，默认 'keyword'
    debounceTime?: number;                       // 防抖时间，默认 300ms
  }
  
  export interface FieldMapping {
    label?: string;     // 显示字段名，默认 'Name'
    value?: string;     // 值字段名，默认 'ID'
    key?: string;       // 唯一标识字段，默认使用 value
  }
  
  export interface OptionConfig {
    emptyText?: string;           // 无数据提示，默认 '暂无数据'
    loadingText?: string;         // 加载提示，默认 '加载中...'
    maxHeight?: string;           // 最大高度，默认 '200px'
  }
  
  export interface OptionItem {
    [key: string]: any;  // 灵活的数据结构
    // 常见字段示例：
    ID?: string;
    Name?: string;
    Value?: string;
  }
  
  export interface ApiResponse {
    Data: OptionItem[];  // 根据您的描述，数据在 res.Data 中
    [key: string]: any;  // 其他可能的响应字段
  }
  
  export interface SearchState {
    searchResults: OptionItem[];
    isLoading: boolean;
    searchError: string | null;
    highlightedIndex: number;
  }