<template>
  <ve-table 
    style="height: 100%;width: 100%;"
    ref="tableRef" 
    v-bind="tableProps"
    @on-custom-comp="handleCustomComp"
    @on-header-cell-click="handleHeaderCellClick"
    @on-body-cell-click="handleBodyCellClick"
    @on-body-cell-double-click="handleBodyCellDoubleClick"
    @on-body-cell-context-menu="handleBodyCellContextMenu"
    @on-body-row-click="handleBodyRowClick"
    @on-body-row-double-click="handleBodyRowDoubleClick"
    @on-body-row-context-menu="handleBodyRowContextMenu"
    @on-header-row-menu="handleHeaderRowMenu"
    @on-body-row-menu="handleBodyRowMenu"
    @on-cell-selection-change="handleCellSelectionChange"
    @on-sort-change="handleSortChange"
    @on-filter-change="handleFilterChange"
    @on-expand-row-change="handleExpandRowChange"
    @on-scroll="handleScroll"
    @on-radio-change="handleRadioChange"
    @on-checkbox-change="handleCheckboxChange"
    @on-checkbox-all-change="handleCheckboxAllChange"
    @on-clipboard-change="handleClipboardChange"
    @on-cell-edit="handleCellEdit"
    @on-cell-edit-before="handleCellEditBefore"
    @on-cell-edit-after="handleCellEditAfter"
    @on-cell-autofill="handleCellAutofill"
    @on-cell-autofill-before="handleCellAutofillBefore"
    @on-cell-autofill-after="handleCellAutofillAfter"
    @on-cell-merge-change="handleCellMergeChange"
    @on-operation-cell-click="handleOperationCellClick"
    @on-footer-cell-click="handleFooterCellClick"
    @on-pagination-change="handlePaginationChange"
    @on-pagination-page-change="handlePaginationPageChange"
    @on-pagination-page-size-change="handlePaginationPageSizeChange"
    @on-loading-change="handleLoadingChange"
    @on-theme-change="handleThemeChange"
    @on-locale-change="handleLocaleChange"
  />
</template>

<script>
export default {
  name: 'BaseTable',
  props: {
    // ==================== 基础配置 ====================
    /**
     * 表格列配置
     * @type {Array}
     * @description 定义表格的列结构，包含字段名、标题、宽度、对齐方式等
     */
    columns: {
      type: Array,
      default: () => []
    },
    
    /**
     * 表格数据
     * @type {Array}
     * @description 表格要显示的数据数组
     */
    tableData: {
      type: Array,
      default: () => []
    },
    
    // ==================== 表格尺寸配置 ====================
    /**
     * 表格宽度
     * @type {Number|String}
     * @description 表格的宽度，可以是数字(px)或字符串(百分比/auto)
     */
    width: {
      type: [Number, String],
      default: 'auto'
    },
    
    /**
     * 表格高度
     * @type {Number|String}
     * @description 表格的高度，可以是数字(px)或字符串(百分比/auto)
     */
    height: {
      type: [Number, String],
      default: 'auto'
    },
    
    /**
     * 表格最大高度
     * @type {Number|String}
     * @description 表格的最大高度，超出时显示滚动条
     */
    maxHeight: {
      type: [Number, String],
      default: 'auto'
    },
    
    /**
     * 表格最小宽度
     * @type {Number|String}
     * @description 表格的最小宽度
     */
    minWidth: {
      type: [Number, String],
      default: 'auto'
    },
    
    /**
     * 滚动宽度
     * @type {Number|String}
     * @description 表格内容区域的滚动宽度
     */
    scrollWidth: {
      type: [Number, String],
      default: 'auto'
    },
    
    // ==================== 表格样式配置 ====================
    /**
     * 是否显示垂直边框
     * @type {Boolean}
     * @description 控制表格是否显示垂直边框线
     */
    borderX: {
      type: Boolean,
      default: false
    },
    
    /**
     * 是否显示水平边框
     * @type {Boolean}
     * @description 控制表格是否显示水平边框线
     */
    borderY: {
      type: Boolean,
      default: false
    },
    
    /**
     * 是否显示斑马纹
     * @type {Boolean}
     * @description 控制表格是否显示斑马纹效果
     */
    stripe: {
      type: Boolean,
      default: false
    },
    
    // ==================== 表头配置 ====================
    /**
     * 是否固定表头
     * @type {Boolean}
     * @description 控制表头是否固定在顶部
     */
    fixedHeader: {
      type: Boolean,
      default: false
    },
    
    /**
     * 表头行高度
     * @type {Number}
     * @description 表头行的高度，单位px
     */
    headerRowHeight: {
      type: Number,
      default: 40
    },
    
    /**
     * 表头分组配置
     * @type {Object}
     * @description 配置表头分组功能，支持多级表头
     */
    headerGroupOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 表头隐藏配置
     * @type {Object}
     * @description 控制表头的显示和隐藏
     */
    headerHideOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 表体配置 ====================
    /**
     * 表体行高度
     * @type {Number}
     * @description 表体行的高度，单位px
     */
    bodyRowHeight: {
      type: Number,
      default: 40
    },
    
    /**
     * 行键字段名
     * @type {String}
     * @description 用于标识每行数据的唯一字段名
     */
    rowKeyFieldName: {
      type: String,
      default: 'rowKey'
    },
    
    // ==================== 列配置 ====================
    /**
     * 列固定配置
     * @type {Object}
     * @description 配置列的固定功能，支持左右固定
     */
    fixedColumnOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 列隐藏配置
     * @type {Object}
     * @description 控制列的显示和隐藏
     */
    columnHideOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 列宽调整配置
     * @type {Object}
     * @description 配置列宽拖拽调整功能
     */
    columnWidthResizeOption: {
      type: Object,
      default: () => ({
        enable: false
      })
    },
    
    /**
     * 操作列配置
     * @type {Object}
     * @description 配置操作列，包含编辑、删除等操作按钮
     */
    operationColumnOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 虚拟滚动配置 ====================
    /**
     * 虚拟滚动配置
     * @type {Object}
     * @description 配置虚拟滚动功能，用于大数据量性能优化
     */
    virtualScrollOption: {
      type: Object,
      default: () => ({
        enable: false
      })
    },
    
    // ==================== 数据处理配置 ====================
    /**
     * 排序配置
     * @type {Object}
     * @description 配置表格排序功能
     */
    sortOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 筛选配置
     * @type {Object}
     * @description 配置表格筛选功能
     */
    filterOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 单元格功能配置 ====================
    /**
     * 单元格选择配置
     * @type {Object}
     * @description 配置单元格选择功能，支持键盘操作
     */
    cellSelectionOption: {
      type: Object,
      default: () => ({
        enable: false
      })
    },
    
    /**
     * 单元格自动填充配置
     * @type {Object}
     * @description 配置Excel风格的单元格自动填充功能
     */
    cellAutofillOption: {
      type: Object,
      default: () => ({
        directionX: false,
        directionY: false
      })
    },
    
    /**
     * 单元格编辑配置
     * @type {Object}
     * @description 配置单元格编辑功能
     */
    editOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 单元格合并配置
     * @type {Object}
     * @description 配置单元格合并功能，支持跨行跨列合并
     */
    cellMergeOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 单元格省略配置
     * @type {Object}
     * @description 配置长文本省略显示功能
     */
    ellipsisOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 单元格样式配置
     * @type {Object}
     * @description 配置单元格的样式和外观
     */
    cellStyleOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 剪贴板配置
     * @type {Object}
     * @description 配置复制、粘贴、剪切、删除等剪贴板操作
     */
    clipboardOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 行功能配置 ====================
    /**
     * 行单选配置
     * @type {Object}
     * @description 配置表格行单选功能
     */
    radioOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 行多选配置
     * @type {Object}
     * @description 配置表格行多选功能
     */
    checkboxOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 行展开配置
     * @type {Object}
     * @description 配置行展开功能，显示详细信息
     */
    expandOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 行样式配置
     * @type {Object}
     * @description 配置行的样式，包括点击高亮、悬浮高亮等
     */
    rowStyleOption: {
      type: Object,
      default: () => ({
        clickHighlight: true,
        hoverHighlight: true
      })
    },
    
    // ==================== 分页配置 ====================
    /**
     * 分页配置
     * @type {Object}
     * @description 配置分页功能，包括页码、每页条数等
     */
    paginationOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== Footer配置 ====================
    /**
     * Footer汇总配置
     * @type {Object}
     * @description 配置表格底部的汇总信息
     */
    footerOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 右键菜单配置 ====================
    /**
     * 表头右键菜单配置
     * @type {Object}
     * @description 配置表头右键菜单功能
     */
    contextmenuHeaderOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 表体右键菜单配置
     * @type {Object}
     * @description 配置表体右键菜单功能
     */
    contextmenuBodyOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 状态配置 ====================
    /**
     * 加载配置
     * @type {Object}
     * @description 配置加载状态显示
     */
    loadingOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 空数据配置
     * @type {Object}
     * @description 配置空数据状态的显示
     */
    emptyOption: {
      type: Object,
      default: () => ({})
    },
    
    // ==================== 主题和国际化配置 ====================
    /**
     * 主题配置
     * @type {Object}
     * @description 配置表格的主题样式
     */
    themeOption: {
      type: Object,
      default: () => ({})
    },
    
    /**
     * 国际化配置
     * @type {String}
     * @description 配置表格的语言，如 'zh-CN', 'en-US'
     */
    locale: {
      type: String,
      default: 'zh-CN'
    },
    
    // ==================== 事件自定义配置 ====================
    /**
     * 事件自定义配置
     * @type {Object}
     * @description 配置自定义事件处理
     */
    eventCustomOption: {
      type: Object,
      default: () => ({})
    }
  },
  
  computed: {
    /**
     * 合并所有 props 传递给 ve-table
     * @returns {Object} 完整的表格配置对象
     */
    tableProps() {
      return {
        // 基础配置
        columns: this.columns,
        tableData: this.tableData,
        
        // 尺寸配置
        width: this.width,
        height: this.height,
        maxHeight: this.maxHeight,
        minWidth: this.minWidth,
        scrollWidth: this.scrollWidth,
        
        // 样式配置
        borderX: this.borderX,
        borderY: this.borderY,
        stripe: this.stripe,
        
        // 表头配置
        fixedHeader: this.fixedHeader,
        headerRowHeight: this.headerRowHeight,
        headerGroupOption: this.headerGroupOption,
        headerHideOption: this.headerHideOption,
        
        // 表体配置
        bodyRowHeight: this.bodyRowHeight,
        rowKeyFieldName: this.rowKeyFieldName,
        
        // 列配置
        fixedColumnOption: this.fixedColumnOption,
        columnHideOption: this.columnHideOption,
        columnWidthResizeOption: this.columnWidthResizeOption,
        operationColumnOption: this.operationColumnOption,
        
        // 功能配置
        virtualScrollOption: this.virtualScrollOption,
        sortOption: this.sortOption,
        filterOption: this.filterOption,
        cellSelectionOption: this.cellSelectionOption,
        cellAutofillOption: this.cellAutofillOption,
        editOption: this.editOption,
        cellMergeOption: this.cellMergeOption,
        ellipsisOption: this.ellipsisOption,
        cellStyleOption: this.cellStyleOption,
        clipboardOption: this.clipboardOption,
        radioOption: this.radioOption,
        checkboxOption: this.checkboxOption,
        expandOption: this.expandOption,
        rowStyleOption: this.rowStyleOption,
        paginationOption: this.paginationOption,
        footerOption: this.footerOption,
        contextmenuHeaderOption: this.contextmenuHeaderOption,
        contextmenuBodyOption: this.contextmenuBodyOption,
        loadingOption: this.loadingOption,
        emptyOption: this.emptyOption,
        themeOption: this.themeOption,
        locale: this.locale,
        eventCustomOption: this.eventCustomOption
      }
    }
  },
  
  methods: {
    // ==================== 事件处理方法 ====================
    
    /**
     * 自定义组件事件
     */
    handleCustomComp(params) {
      this.$emit('on-custom-comp', params)
    },
    
    /**
     * 表头单元格点击事件
     */
    handleHeaderCellClick(params) {
      this.$emit('on-header-cell-click', params)
    },
    
    /**
     * 表体单元格点击事件
     */
    handleBodyCellClick(params) {
      this.$emit('on-body-cell-click', params)
    },
    
    /**
     * 表体单元格双击事件
     */
    handleBodyCellDoubleClick(params) {
      this.$emit('on-body-cell-double-click', params)
    },
    
    /**
     * 表体单元格右键菜单事件
     */
    handleBodyCellContextMenu(params) {
      this.$emit('on-body-cell-context-menu', params)
    },
    
    /**
     * 表体行点击事件
     */
    handleBodyRowClick(params) {
      this.$emit('on-body-row-click', params)
    },
    
    /**
     * 表体行双击事件
     */
    handleBodyRowDoubleClick(params) {
      this.$emit('on-body-row-double-click', params)
    },
    
    /**
     * 表体行右键菜单事件
     */
    handleBodyRowContextMenu(params) {
      this.$emit('on-body-row-context-menu', params)
    },
    
    /**
     * 表头行菜单事件
     */
    handleHeaderRowMenu(params) {
      this.$emit('on-header-row-menu', params)
    },
    
    /**
     * 表体行菜单事件
     */
    handleBodyRowMenu(params) {
      this.$emit('on-body-row-menu', params)
    },
    
    /**
     * 单元格选择变化事件
     */
    handleCellSelectionChange(params) {
      this.$emit('on-cell-selection-change', params)
    },
    
    /**
     * 排序变化事件
     */
    handleSortChange(params) {
      this.$emit('on-sort-change', params)
    },
    
    /**
     * 筛选变化事件
     */
    handleFilterChange(params) {
      this.$emit('on-filter-change', params)
    },
    
    /**
     * 展开行变化事件
     */
    handleExpandRowChange(params) {
      this.$emit('on-expand-row-change', params)
    },
    
    /**
     * 滚动事件
     */
    handleScroll(params) {
      this.$emit('on-scroll', params)
    },
    
    /**
     * 单选变化事件
     */
    handleRadioChange(params) {
      this.$emit('on-radio-change', params)
    },
    
    /**
     * 多选变化事件
     */
    handleCheckboxChange(params) {
      this.$emit('on-checkbox-change', params)
    },
    
    /**
     * 全选变化事件
     */
    handleCheckboxAllChange(params) {
      this.$emit('on-checkbox-all-change', params)
    },
    
    /**
     * 剪贴板变化事件
     */
    handleClipboardChange(params) {
      this.$emit('on-clipboard-change', params)
    },
    
    /**
     * 单元格编辑事件
     */
    handleCellEdit(params) {
      this.$emit('on-cell-edit', params)
    },
    
    /**
     * 单元格编辑前事件
     */
    handleCellEditBefore(params) {
      this.$emit('on-cell-edit-before', params)
    },
    
    /**
     * 单元格编辑后事件
     */
    handleCellEditAfter(params) {
      this.$emit('on-cell-edit-after', params)
    },
    
    /**
     * 单元格自动填充事件
     */
    handleCellAutofill(params) {
      this.$emit('on-cell-autofill', params)
    },
    
    /**
     * 单元格自动填充前事件
     */
    handleCellAutofillBefore(params) {
      this.$emit('on-cell-autofill-before', params)
    },
    
    /**
     * 单元格自动填充后事件
     */
    handleCellAutofillAfter(params) {
      this.$emit('on-cell-autofill-after', params)
    },
    
    /**
     * 单元格合并变化事件
     */
    handleCellMergeChange(params) {
      this.$emit('on-cell-merge-change', params)
    },
    
    /**
     * 操作列点击事件
     */
    handleOperationCellClick(params) {
      this.$emit('on-operation-cell-click', params)
    },
    
    /**
     * Footer单元格点击事件
     */
    handleFooterCellClick(params) {
      this.$emit('on-footer-cell-click', params)
    },
    
    /**
     * 分页变化事件
     */
    handlePaginationChange(params) {
      this.$emit('on-pagination-change', params)
    },
    
    /**
     * 分页页码变化事件
     */
    handlePaginationPageChange(params) {
      this.$emit('on-pagination-page-change', params)
    },
    
    /**
     * 分页每页条数变化事件
     */
    handlePaginationPageSizeChange(params) {
      this.$emit('on-pagination-page-size-change', params)
    },
    
    /**
     * 加载状态变化事件
     */
    handleLoadingChange(params) {
      this.$emit('on-loading-change', params)
    },
    
    /**
     * 主题变化事件
     */
    handleThemeChange(params) {
      this.$emit('on-theme-change', params)
    },
    
    /**
     * 国际化变化事件
     */
    handleLocaleChange(params) {
      this.$emit('on-locale-change', params)
    },
    
    // ==================== 表格方法代理 ====================
    
    /**
     * 设置所有单元格选择
     */
    setAllCellSelection() {
      return this.$refs.tableRef?.setAllCellSelection()
    },
    
    /**
     * 设置范围单元格选择
     * @param {Object} params 选择参数
     */
    setRangeCellSelection(params) {
      return this.$refs.tableRef?.setRangeCellSelection(params)
    },
    
    /**
     * 设置单元格选择
     * @param {Object} params 选择参数
     */
    setCellSelection(params) {
      return this.$refs.tableRef?.setCellSelection(params)
    },
    
    /**
     * 开始单元格编辑
     * @param {Object} params 编辑参数
     */
    startCellEditing(params) {
      return this.$refs.tableRef?.startCellEditing(params)
    },
    
    /**
     * 停止单元格编辑
     */
    stopCellEditing() {
      return this.$refs.tableRef?.stopCellEditing()
    },
    
    /**
     * 滚动到指定行键
     * @param {Object} params 滚动参数
     */
    scrollToRowKey(params) {
      return this.$refs.tableRef?.scrollToRowKey(params)
    },
    
    /**
     * 滚动到指定位置
     * @param {Object} params 滚动参数
     */
    scrollTo(params) {
      return this.$refs.tableRef?.scrollTo(params)
    },
    
    /**
     * 获取表格实例
     * @returns {Object} 表格实例
     */
    getTableInstance() {
      return this.$refs.tableRef
    },
    
    /**
     * 设置排序
     * @param {Object} params 排序参数
     */
    setSort(params) {
      return this.$refs.tableRef?.setSort(params)
    },
    
    /**
     * 设置筛选
     * @param {Object} params 筛选参数
     */
    setFilter(params) {
      return this.$refs.tableRef?.setFilter(params)
    },
    
    /**
     * 设置行选择
     * @param {Object} params 选择参数
     */
    setRowSelection(params) {
      return this.$refs.tableRef?.setRowSelection(params)
    },
    
    /**
     * 设置行展开
     * @param {Object} params 展开参数
     */
    setRowExpand(params) {
      return this.$refs.tableRef?.setRowExpand(params)
    },
    
    /**
     * 设置列显示
     * @param {Object} params 显示参数
     */
    setColumnVisible(params) {
      return this.$refs.tableRef?.setColumnVisible(params)
    },
    
    /**
     * 设置列固定
     * @param {Object} params 固定参数
     */
    setColumnFixed(params) {
      return this.$refs.tableRef?.setColumnFixed(params)
    },
    
    /**
     * 设置单元格合并
     * @param {Object} params 合并参数
     */
    setCellMerge(params) {
      return this.$refs.tableRef?.setCellMerge(params)
    },
    
    /**
     * 设置加载状态
     * @param {Boolean} loading 加载状态
     */
    setLoading(loading) {
      return this.$refs.tableRef?.setLoading(loading)
    },
    
    /**
     * 刷新表格
     */
    refresh() {
      return this.$refs.tableRef?.refresh()
    },
    
    /**
     * 重新计算布局
     */
    recalculateLayout() {
      return this.$refs.tableRef?.recalculateLayout()
    }
  }
}
</script>

<style lang="scss" scoped>
// 基础表格样式
// 可以根据需要添加自定义样式
</style>
