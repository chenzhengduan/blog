import { Ref } from 'vue'

/**
 * 虚拟滚动表格滚动功能 composable
 */
export function useTableScroll() {
    /**
     * 滚动到指定行
     * @param {Ref} tableRef - 表格组件的引用
     * @param {number} rowIndex - 行索引（从0开始）
     * @param {string} rowId - 行ID（可选，如果提供ID会先查找对应的索引）
     * @param {any[]} tableData - 表格数据数组
     */
    function scrollToRow(tableRef: Ref, rowIndex: number, rowId: string | null = null, tableData: any[] = []) {
        // 优先使用精确的 scrollToRowKey 方法（如果有rowId）
        if (rowId && tableRef.value) {
            try {
                // 使用 base-table 的 scrollToRowKey 方法，这对虚拟滚动更准确
                const result = tableRef.value.scrollToRowKey({
                    rowKey: rowId,
                    verticalPosition: 'top' // 滚动到顶部位置
                })
                
                if (result !== false) {
                    return
                } else {
                    console.warn(`scrollToRowKey 未找到目标行，降级到索引滚动`)
                }
            } catch (error) {
                console.warn(`scrollToRowKey 执行失败，降级到索引滚动:`, error)
            }
        }
        
        // 降级逻辑：如果没有rowId或scrollToRowKey失败，使用索引滚动
        if (rowId && tableData.length > 0) {
            const foundIndex = tableData.findIndex(row => row.ID === rowId)
            if (foundIndex !== -1) {
                rowIndex = foundIndex
            } else {
                console.warn(`未找到ID为 ${rowId} 的行`)
                return
            }
        }
        
        // 检查索引是否有效
        if (rowIndex < 0 || (tableData.length > 0 && rowIndex >= tableData.length)) {
            console.warn(`行索引 ${rowIndex} 超出范围 [0, ${tableData.length - 1}]`)
            return
        }
        
        // 获取ve-table实例
        if (!tableRef.value || !tableRef.value.$refs || !tableRef.value.$refs.tableRef) {
            console.warn('未找到表格引用')
            return
        }
        
        const veTableInstance = tableRef.value.$refs.tableRef
        if (!veTableInstance) {
            console.warn('未找到ve-table实例')
            return
        }
        
        try {
            // 获取滚动容器
            const scrollContainer = veTableInstance.$el.querySelector('.fan-table-container')
            if (!scrollContainer) {
                console.error('未找到滚动容器 .fan-table-container')
                return
            }
            
            // 获取行高
            const virtualScrollOption = veTableInstance.virtualScrollOption
            let itemSize = 40 // 默认行高
            
            if (virtualScrollOption && virtualScrollOption.itemSize) {
                itemSize = virtualScrollOption.itemSize
            } else {
                const firstRow = scrollContainer.querySelector('.fan-table-body-tr')
                if (firstRow) {
                    itemSize = firstRow.offsetHeight
                }
            }
            
            // 计算目标滚动位置
            let targetScrollTop = rowIndex * itemSize
            
            // 分组模式下的特殊处理：考虑分组行和数据行可能有不同高度
            if (tableData.length > 0 && rowIndex < tableData.length) {
                // 添加额外偏移，确保目标行可见
                const SCROLL_OFFSET = 50
                targetScrollTop = Math.max(0, targetScrollTop - SCROLL_OFFSET)
            }
            
                        // 使用纯原生滚动方案（最稳定）
            scrollContainer.scrollTop = targetScrollTop
            
            // 触发原生滚动事件，让虚拟滚动组件自然响应
            const scrollEvent = new Event('scroll', { bubbles: true })
            scrollContainer.dispatchEvent(scrollEvent)
            
            // 使用更稳定的方式确保虚拟滚动更新：
            // 1. 先等待一帧让滚动位置生效
            // 2. 再次触发滚动事件确保虚拟滚动组件响应
            requestAnimationFrame(() => {
                // 确认滚动位置已设置
                if (Math.abs(scrollContainer.scrollTop - targetScrollTop) < 5) {
                    // 再次触发滚动事件，确保虚拟滚动完全更新
                    const confirmEvent = new Event('scroll', { bubbles: true })
                    scrollContainer.dispatchEvent(confirmEvent)
                } else {
                    // 如果滚动位置不对，重新设置
                    scrollContainer.scrollTop = targetScrollTop
                    const retryEvent = new Event('scroll', { bubbles: true })
                    scrollContainer.dispatchEvent(retryEvent)
                }
            })
            
        } catch (error: any) {
            console.error('滚动失败:', error?.message || error)
        }
    }
    
    /**
     * 滚动到校验错误的第一行
     * @param {Ref} tableRef - 表格组件的引用
     * @param {string[]} errorRowIds - 错误行ID列表
     * @param {any[]} tableData - 表格数据数组
     */
    function scrollToFirstErrorRow(tableRef: Ref, errorRowIds: string[], tableData: any[] = []) {
        if (errorRowIds.length > 0) {
            const firstErrorRowId = errorRowIds[0]
            scrollToRow(tableRef, 0, firstErrorRowId, tableData)
        } else {
            console.log('没有校验错误的行')
        }
    }
    
    return {
        scrollToRow,
        scrollToFirstErrorRow
    }
} 