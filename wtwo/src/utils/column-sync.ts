/**
 * 列同步工具函数
 * 用于处理用户自定义列与默认列的同步，包括新增列和删除列的处理
 */

export interface ColumnItem {
  Key: string
  [key: string]: any
}

/**
 * 同步用户自定义列与默认列
 * @param userColumns 用户自定义的列配置
 * @param defaultColumns 默认的列配置
 * @returns 同步后的列配置
 */
export function syncUserColumns(
  userColumns: ColumnItem[],
  defaultColumns: ColumnItem[]
): ColumnItem[] {
  if (!userColumns || !defaultColumns) {
    return defaultColumns || []
  }

  const result: ColumnItem[] = []
  const defaultColumnMap = new Map<string, ColumnItem>()
  
  // 创建默认列的映射，方便查找
  defaultColumns.forEach(col => {
    defaultColumnMap.set(col.Key, col)
  })

  // 按照 userColumns 的顺序处理列，只保留在 defaultColumns 中存在的列
  userColumns.forEach(userCol => {
    const defaultCol = defaultColumnMap.get(userCol.Key)
    if (defaultCol) {
      // 如果默认列中存在该列，合并默认配置和用户配置
      const syncedCol = { ...defaultCol, ...userCol }
      result.push(syncedCol)
    }
    // 如果默认列中不存在该列，则跳过（过滤掉已被移除的列）
  })

  // 添加用户没有配置但在默认列中存在的列（新增的列）
  defaultColumns.forEach(defaultCol => {
    const hasUserConfig = userColumns.some(userCol => userCol.Key === defaultCol.Key)
    if (!hasUserConfig) {
      // 用户没有配置的列，使用默认配置
      result.push({ ...defaultCol })
    }
  })

  return result
}

/**
 * 获取列差异信息
 * @param userColumns 用户自定义的列配置
 * @param defaultColumns 默认的列配置
 * @returns 列差异信息
 */
export function getColumnDiff(
  userColumns: ColumnItem[],
  defaultColumns: ColumnItem[]
): {
  added: ColumnItem[]
  removed: ColumnItem[]
  modified: ColumnItem[]
} {
  const userColumnMap = new Map<string, ColumnItem>()
  const defaultColumnMap = new Map<string, ColumnItem>()
  
  userColumns.forEach(col => userColumnMap.set(col.Key, col))
  defaultColumns.forEach(col => defaultColumnMap.set(col.Key, col))

  const added: ColumnItem[] = []
  const removed: ColumnItem[] = []
  const modified: ColumnItem[] = []

  // 检查新增的列
  userColumns.forEach(userCol => {
    if (!defaultColumnMap.has(userCol.Key)) {
      added.push(userCol)
    }
  })

  // 检查删除的列
  defaultColumns.forEach(defaultCol => {
    if (!userColumnMap.has(defaultCol.Key)) {
      removed.push(defaultCol)
    }
  })

  // 检查修改的列
  userColumns.forEach(userCol => {
    const defaultCol = defaultColumnMap.get(userCol.Key)
    if (defaultCol && JSON.stringify(userCol) !== JSON.stringify(defaultCol)) {
      modified.push(userCol)
    }
  })

  return { added, removed, modified }
}

/**
 * 验证列配置的完整性
 * @param columns 列配置
 * @param requiredFields 必需字段数组
 * @returns 验证结果
 */
export function validateColumns(
  columns: ColumnItem[],
  requiredFields: string[] = ['Key']
): { isValid: boolean; errors: string[] } {
  const errors: string[] = []
  
  if (!Array.isArray(columns)) {
    return { isValid: false, errors: ['列配置必须是数组'] }
  }

  columns.forEach((col, index) => {
    requiredFields.forEach(field => {
      if (!col[field]) {
        errors.push(`第${index + 1}列缺少必需字段: ${field}`)
      }
    })
  })

  return {
    isValid: errors.length === 0,
    errors
  }
}
