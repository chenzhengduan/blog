# API-CN

## API

### 表格配置

#### Table Props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| tableData | 表格数据 | Array | - | - |
| footerData | 表格footer 汇总数据，数据结构和 tableData 一致 | Array | - | - |
| columns | 列配置，具体项见下表 columns 配置 | Array | - | - |
| showHeader | 是否展示表头 | Boolean | - | true |
| fixedHeader | 是否固定表头，默认启用。需要和 `maxHeight`结合使用 | Boolean | - | true |
| fixedFooter | 是否固定footer 汇总，默认启用。需要和 `maxHeight`结合使用 | Boolean | - | true |
| scrollWidth | 表格滚动区域的宽（开始出滚动条的宽度）。Number指定像素；String指定百分比 | Number、String | - | - |
| maxHeight | 表格的最大高度。Number指定像素；String指定百分比。用于"固定头"或"虚拟滚动"功能 | Number、String | - | - |
| rowKeyFieldName | 指定 row key 的字段名称。用于行展开、行单选、行多选、行点击高亮、虚拟滚动 | String | - | - |
| borderAround | 是否展示表格外边框 | Boolean | - | true |
| borderX | 是否展示列横向边框 | Boolean | - | true |
| borderY | 是否展示列纵向边框 | Boolean | - | false |
| cellSpanOption | 单元格合并配置，具体见下表 cellSpanOption 配置 | Object | - | - |
| columnHiddenOption | 列隐藏配置，具体见下表 columnHiddenOption 配置 | Object | - | - |
| cellStyleOption | 单元格样式配置，具体见下表 cellStyleOption 配置 | Object | - | - |
| rowStyleOption | 行样式配置，具体见下表 rowStyleOption 配置 | Object | - | - |
| expandOption | 行展开配置，具体见下表 expandOption 配置 | Object | - | - |
| checkboxOption | 行多选配置，具体见下表 checkboxOption 配置 | Object | - | - |
| radioOption | 行单选配置，具体见下表 radioOption 配置 | Object | - | - |
| virtualScrollOption | 虚拟滚动配置，建议需要一次性展示1000条以上使用。具体见下表 virtualScrollOption 配置。 | Object | - | - |
| sortOption | 排序配置，具体见下表 sortOption 配置 | Object | - | - |
| cellSelectionOption | 单元格选择配置，具体见下表 cellSelectionOption 配置 | Object | - | - |
| editOption | 单元格编辑配置，具体见下表 editOption 配置 | Object | - | - |
| contextmenuHeaderOption | 表格 header 右键菜单配置，具体见下表 contextmenuHeaderOption 配置 | Object | - | - |
| contextmenuBodyOption | 表格 body 右键菜单配置，具体见下表 contextmenuBodyOption 配置 | Object | - | - |
| eventCustomOption | 自定义事件配置，具体见下表 eventCustomOption 配置 | Object | - | - |
| cellAutofillOption | 单元格自动填充配置，具体见下表 cellAutofillOption 配置 | Object | - | - |
| clipboardOption | 单元格剪贴板配置，具体见下表 clipboardOption 配置 | Object | - | - |

### 列配置

#### Columns

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| field | 对应列的字段 | String | - | - |
| key | 每个列的唯一key值 | String | - | - |
| type | 当前列类型。 "expand"：行展开；"checkbox"：行多选；"radio"：行单选 | String | "expand"、"checkbox"、"radio" | - |
| title | 列标题 | String | - | - |
| width | Number指定像素；String指定百分比（指定了也不生效？） | String、Number | - | - |
| align | 单元格对齐方式 | String | "left"、"center"、"right" | "center" |
| operationColumn | 是否是操作列 | Boolean | - | false |
| edit | 是否开启列编辑 | Boolean | - | false |
| sortBy | 排序规则。<br>1、sortBy=""：允许排序但无排序规则；<br>2、sortBy="asc"：默认当前列升序；<br>3、sortBy="desc"：默认当前列降序； | String | ""、"desc"、"asc" | "" |
| renderBodyCell | 1、表体自定义单元格渲染函数。jsx 语法,书写和模板语法接近。<br>2、参数信息。row:当前行数据、column:当前列配置、rowIndex:行索引、h：createElement 函数的别名<br>3、更多 jsx 知识请参考Vue.js 官方文档 | Function({row,column,rowIndex},h):VNode | - | - |
| renderHeaderCell | 1、表头自定义单元格渲染函数。用法同renderBodyCell。<br>2、参数信息。column:当前列配置、h：createElement 函数的别名 | Function({ column },h):VNode | - | - |
| renderFooterCell | 1、footer汇总 自定义单元格渲染函数。<br>2、参数信息。row:当前行数据、column:当前列配置、rowIndex:行索引、h：createElement 函数的别名<br>3、更多 jsx 知识请参考Vue.js 官方文档 | Function({row,column,rowIndex},h):VNode | - | - |
| disableResizing | Disable resizing for this column. Only effective if columnWidthResizeOption is enabled | Boolean | - | false |
| ellipsis | 单元格省略配置。 | Object | - | - |
| filter | 筛选配置。 | Object | - | - |
| filterCustom | 筛选自定义配置。 | Object | - | - |

### 实例方法

#### Table instance methods

| 方法名 | 说明 | 参数 |
|--------|------|------|
| scrollTo | 使表格滚动到指定的位置 参考示例 | 参考 MDN scrollTo |
| scrollToColKey | 将指定的列显示在可视区域 参考示例 | `{ rowKey, colKey }` |
| scrollToRowKey | 将表格滚动到行为rowKey的位置 参考示例 | `{rowKey}` |
| setHighlightRow | 设置高亮的行 参考示例 | `{rowKey}` |
| startEditingCell | 开始单元格编辑 参考示例 | `{rowKey,colKey,defaultValue}` |
| stopEditingCell | 停止单元格编辑 | - |
| hideColumnsByKeys | 隐藏列 参考示例 | `keys` |
| showColumnsByKeys | 显示列 参考示例 | `keys` |
| setCellSelection | 单元格选中 参考示例 | `{ rowKey, colKey }` |
| setAllCellSelection | 单元格全选 参考示例 | - |
| setRangeCellSelection | 区域单元格选中 参考示例 | `{ startRowKey,startColKey,endRowKey,endColKey,isScrollToStartCell }` |
| getRangeCellSelection | 获取当前选择区域的信息。返回所选区域索引和key信息 | `{selectionRangeKeys,selectionRangeIndexes}` |

### 单元格合并配置

#### cellSpanOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| bodyCellSpan | 1、body单元格合并函数<br>2、参数信息。row:当前行数据、column:当前列配置、rowIndex:行索引 | Function({row,column,rowIndex}) | - | - |
| footerCellSpan | 1、footer单元格合并函数<br>2、参数信息。row:当前行数据、column:当前列配置、rowIndex:行索引 | Function({row,column,rowIndex}) | - | - |

### 列隐藏配置

#### columnHiddenOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultHiddenColumnKeys v2.11.0 | 设置默认隐藏的列 | Array | - | - |

### 单元格样式配置

#### cellStyleOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| bodyCellClass | 1、表体单元格样式<br>2、接收3个参数，row:当前行数据、column:当前列配置、rowIndex:行索引 | Function({row,column,rowIndex}) | - | - |
| headerCellClass | 1、表头单元格样式<br>2、接收2个参数，column:当前列配置、rowIndex:行索引 | Function({column,rowIndex}) | - | - |
| footerCellClass | 1、footer汇总 单元格样式<br>2、接收3个参数，row:当前行数据、column:当前列配置、rowIndex:行索引 | Function({row,column,rowIndex}) | - | - |

### 行样式配置

#### rowStyleOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| hoverHighlight | 是否开启行hover 背景高亮 | Boolean | - | true |
| clickHighlight | 是否开启行click 背景高亮 | Boolean | - | true |
| stripe | 是否开启斑马纹 | Boolean | - | false |

### 行展开配置

#### expandOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| expandable | 1、是否允许展开行渲染函数。返回布尔值。<br>2、渲染函数接收三个参数，row:当前行数据、column:可展开列配置、rowIndex:行索引。 | Function({row,column,rowIndex}) | - | - |
| render | 1、渲染函数。<br>2、渲染函数接收的参数，row:当前行数据、column:可展开列配置、rowIndex:行索引、h：createElement 函数的别名 | Function({row,column,rowIndex},h):VNode | - | - |
| defaultExpandAllRows | 是否默认展开全部行 | Boolean | - | false |
| defaultExpandedRowKeys | 默认展开的行key。defaultExpandAllRows 参数和defaultExpandedRowKeys 参数同时存在时，优先使用defaultExpandAllRows | String[]、Number[] | - | - |
| expandedRowKeys | 展开行的可控属性，设置后属性后 defaultExpandAllRows和defaultExpandedRowKeys属性将会失效。具体见相关实例 | String[]、Number[] | - | - |
| beforeExpandRowChange | 1、展开切换前的函数，如果返回false 则中断执行。<br>2、函数接收三个参数，beforeExpandedRowKeys:改变前所有展开的key，row:当前的行数据，rowIndex行号 | Function({beforeExpandedRowKeys,row,rowIndex}) | - | - |
| afterExpandRowChange | 1、展开切换后的函数。<br>2、函数接收三个参数，afterExpandedRowKeys:改变后所有展开的key，row:当前的行数据，rowIndex行号 | Function({afterExpandedRowKeys,row,rowIndex}) | - | - |
| trigger | 展开行事件触发类型。<br>icon：点击展开小图标；cell：点击单元格;row:点击行 | String | "icon"、"cell"、"row" | "icon" |

### 行多选配置

#### checkboxOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultSelectedAllRows | 是否默认全部选中 | Boolean | - | false |
| defaultSelectedRowKeys | 默认选中的行key | String[]、Number[] | - | - |
| disableSelectedRowKeys | 禁止勾选或者禁止取消勾选的行key | String[]、Number[] | - | - |
| selectedRowKeys | 选中行的可控属性，设置后 defaultSelectedAllRows 和 defaultSelectedRowKeys 属性将会失效。具体参考示例 | String[]、Number[] | - | - |
| selectedRowChange | 行选中的改变事件。事件接收 3 个参数，row:当前行数据，isSelected当前行是否选中，selectedRowKeys所有选中的 rowKey 信息 | Function({row, isSelected, selectedRowKeys}) | - | - |
| selectedAllChange | 全选改变事件。事件接收 2 个参数，isSelected 是否全选。selectedRowKeys所有选中的 rowKey 信息 | Function({isSelected, selectedRowKeys}) | - | - |
| hideSelectAll | 是否隐藏全选按钮 | Boolean | - | false |

### 行单选配置

#### radioOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| defaultSelectedRowKey | 默认选中的行key | String、Number | - | - |
| disableSelectedRowKeys | 禁止勾选或者禁止取消勾选的行key | String[]、Number[] | - | - |
| selectedRowKey | 选中行的可控属性，设置后 defaultSelectedRowKey属性将会失效。具体参考示例 | String、Number | - | - |
| selectedRowChange | 行选中的改变事件。事件接收 1 个参数，row:当前行数据 | Function({row}) | - | - |

### 虚拟滚动配置

#### virtualScrollOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| enable | 是否开启虚拟滚动 | Boolean | - | false |
| minRowHeight | 最小行高（px）。值越小，可视化范围渲染的数据越多，具体根据实际最小高度设置即可 | Number | - | 40 |
| scrolling | 滚动回调事件。<br>startRowIndex为当前开始渲染的行号，visibleStartIndex为当前可见区域的开始行号，visibleEndIndex为当前可见区域的结束行号，visibleAboveCount为当前可见区域上方渲染的数量，visibleBelowCount为当前可见区域下方渲染的数量 | Function({startRowIndex,visibleStartIndex,visibleEndIndex,visibleAboveCount,visibleBelowCount}) | - | - |
| bufferScale | 缓冲倍数。1个缓冲倍数为当前表格高度内的行数量 | Number | - | 1 |

### 排序配置

#### sortOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| multipleSort | 是否开启多字段排序 | Boolean | - | false |
| sortAlways | 是否开启排序只在升序和降序切换 | Boolean | - | false |
| sortChange | 排序改变事件。事件接收 1 个参数对象，列的排序规则 | Function({row}) | - | - |

### 单元格选择配置

#### cellSelectionOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| enable | 是否开启单元格选择 | Boolean | - | true |

### 单元格编辑配置

#### editOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| beforeStartCellEditing | 单元格进入编辑状态前的回调方法。row当前行数据，column当前列信息，cellValue当前单元格的值。如果返回false，将则会阻止单元格进入编辑状态 | Function({ row, column,cellValue }) | - | - |
| beforeCellValueChange | 单元格内容改变前的回调方法。row当前行数据，column当前列信息，changeValue单元格改变的值。如果返回false，将会阻止编辑，单元格还原为编辑前状态 | Function({ row, column,changeValue }) | - | - |
| afterCellValueChange | 单元格内容改变后的回调方法。row当前行数据，column当前列信息，changeValue单元格改变的值 | Function({ row, column,changeValue }) | - | - |
| cellValueChange | 即将废弃的方法 | Function({ row, column }) | - | - |

### header 右键菜单配置

#### contextmenuHeaderOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| beforeShow | 菜单显示之前的回调事件，你可以在这个阶段改变菜单项信息。<br>isWholeColSelection是否整列选中，<br>selectionRangeKeys 当前选中的单元格key信息，<br>selectionRangeIndexes 当前选中的单元格索引信息 | Function({ isWholeColSelection, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| afterMenuClick | 菜单项被点击的回调，返回 false 将阻止当前右键操作。<br>type菜单类型，<br>selectionRangeKeys 当前选中的单元格key信息，<br>selectionRangeIndexes 当前选中的单元格索引信息 | Function({ type, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| contextmenus | 右键菜单配置项。 右键菜单组件 | Array | - | - |

### body 右键菜单配置

#### contextmenuBodyOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| beforeShow | 菜单显示之前的回调事件，你可以在这个阶段改变菜单项信息。<br>isWholeRowSelection是否整行选中，<br>selectionRangeKeys 当前选中的单元格key信息，<br>selectionRangeIndexes 当前选中的单元格索引信息 | Function({ isWholeRowSelection, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| afterMenuClick | 菜单项被点击的回调，返回 false 将阻止当前右键操作。<br>type菜单类型，<br>selectionRangeKeys 当前选中的单元格key信息，<br>selectionRangeIndexes 当前选中的单元格索引信息 | Function({ type, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| contextmenus | 右键菜单配置项。 右键菜单组件 | Array | - | - |

### 事件自定义配置

#### eventCustomOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| bodyRowEvents | 1、body 行自定义事件，返回需要自定义的事件。<br>2、接收2个参数。row当前行数据、rowIndex:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,rowIndex}) | - | - |
| bodyCellEvents | 1、body 列自定义事件，返回需要自定义的事件。<br>2、接收3个参数。row当前行数据、column:当前列配置、rowIndex:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,column,rowIndex}) | - | - |
| headerRowEvents | 1、header 行自定义事件，返回需要自定义的事件。<br>2、接收1个参数。rowIndex：表头行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | Function({rowIndex}) | - | - |
| headerCellEvents | 1、header 列自定义事件，返回需要自定义的事件。<br>2、接收2个参数。column:当前列配置、rowIndex:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | Function({column,rowIndex}) | - | - |
| footerRowEvents | 1、footer 行自定义事件，返回需要自定义的事件。<br>2、接收2个参数。row当前行数据、rowIndex:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,rowIndex}) | - | - |
| footerCellEvents | 1、footer 列自定义事件，返回需要自定义的事件。<br>2、接收3个参数。row当前行数据、column:当前列配置、rowIndex:行索引<br>3、支持自定义事件有 click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,column,rowIndex}) | - | - |

### 单元格自动填充配置

#### cellAutofillOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| directionX | 是否开启横向填充 | Boolean | - | true |
| directionY | 是否开启纵向填充 | Boolean | - | true |
| beforeAutofill | 单元格自动填充前的回调方法,返回false 则取消自动填充。参数说明：<br>1、direction自动填充的方向<br>2、sourceSelectionRangeIndexes自动填充来源的行和列索引<br>3、targetSelectionRangeIndexes自动填充目标的行和列索引<br>4、sourceSelectionData自动填充来源的数据，超出会自动去除<br>5、targetSelectionData自动填充目标的数据 | Function({<br>direction,<br>sourceSelectionRangeIndexes,<br>targetSelectionRangeIndexes,<br>sourceSelectionData,<br>targetSelectionData,<br>}) | - | - |
| afterAutofill | 单元格自动填充后的回调方法。参数说明：<br>1、direction自动填充的方向<br>2、sourceSelectionRangeIndexes自动填充来源的行和列索引<br>3、targetSelectionRangeIndexes自动填充目标的行和列索引<br>4、sourceSelectionData自动填充来源的数据，超出会自动去除<br>5、targetSelectionData自动填充目标的数据 | Function({<br>direction,<br>sourceSelectionRangeIndexes,<br>targetSelectionRangeIndexes,<br>sourceSelectionData,<br>targetSelectionData,<br>}) | - | - |

### 剪贴板配置

#### clipboardOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| copy | 是否开启单元格复制 | Boolean | - | true |
| paste | 是否开启单元格粘贴 | Boolean | - | true |
| cut | 是否开启单元格剪切 | Boolean | - | true |
| delete | 是否开启单元格删除 | Boolean | - | true |
| beforeCopy | 单元格拷贝前的回调方法,返回false 则取消拷贝。参数说明：<br>1、data拷贝的数据<br>2、selectionRangeIndexes拷贝区域的索引信息<br>3、selectionRangeKeys拷贝区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| afterCopy | 单元格拷贝后回调方法。参数说明：<br>1、data拷贝的数据<br>2、selectionRangeIndexes拷贝区域的索引信息<br>3、selectionRangeKeys拷贝区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| beforePaste | 单元格粘贴前的回调方法,返回false 则取消粘贴。参数说明：<br>1、data粘贴的数据<br>2、selectionRangeIndexes粘贴区域的索引信息<br>3、selectionRangeKeys粘贴区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| afterPaste | 单元格粘贴后回调方法。参数说明：<br>1、data粘贴的数据<br>2、selectionRangeIndexes粘贴区域的索引信息<br>3、selectionRangeKeys粘贴区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| beforeCut | 单元格剪切前的回调方法,返回false 则取消剪切。参数说明：<br>1、data粘贴的数据<br>2、selectionRangeIndexes粘贴区域的索引信息<br>3、selectionRangeKeys粘贴区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| afterCut | 单元格剪切后回调方法。参数说明：<br>1、data剪切的数据<br>2、selectionRangeIndexes剪切区域的索引信息<br>3、selectionRangeKeys剪切区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| beforeDelete | 单元格删除前的回调方法,返回false 则取消删除。参数说明：<br>1、data粘贴的数据<br>2、selectionRangeIndexes粘贴区域的索引信息<br>3、selectionRangeKeys粘贴区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |
| afterDelete | 单元格删除后回调方法。参数说明：<br>1、data删除的数据<br>2、selectionRangeIndexes删除区域的索引信息<br>3、selectionRangeKeys删除区域的key信息 | Function({<br>data, selectionRangeIndexes, selectionRangeKeys<br>}) | - | - |

### 列宽改变配置

#### columnWidthResizeOption

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|------|------|------|--------|--------|
| enable | 是否开启列宽可变 | Boolean | - | false |
| minWidth | 可改变列的最小宽度 | Number | - | 30px |
| sizeChange | 列宽改变后的回调函数。参数说明：<br>1、column宽度改变的列信息<br>2、differWidth列宽改变后差异的宽度<br>3、columnWidth列宽改变后的宽度 | Function({<br>column,<br>differWidth,<br>columnWidth,<br>}) | - | - |

## 功能特性

### 1. 基础功能
- **数据展示**: 支持复杂数据结构的表格展示
- **列配置**: 灵活的列配置选项，支持自定义渲染
- **边框样式**: 可配置的边框显示选项
- **表头固定**: 支持表头和表尾的固定显示

### 2. 交互功能
- **行选择**: 支持单选和多选模式
- **行展开**: 支持行展开功能，可自定义展开内容
- **单元格编辑**: 支持单元格内联编辑
- **排序筛选**: 支持多字段排序和筛选功能

### 3. 高级功能
- **虚拟滚动**: 支持大数据量的虚拟滚动
- **单元格合并**: 支持复杂的单元格合并逻辑
- **右键菜单**: 支持自定义右键菜单
- **剪贴板**: 支持复制、粘贴、剪切、删除操作

### 4. 样式定制
- **单元格样式**: 支持动态单元格样式配置
- **行样式**: 支持行级别的样式定制
- **列隐藏**: 支持列的动态显示和隐藏
- **列宽调整**: 支持列宽的动态调整

## 使用指南

### 1. 基础用法

```vue
<template>
  <fan-table 
    :columns="columns" 
    :table-data="tableData"
    row-key-field-name="id"
  />
</template>
```

### 2. 高级配置

```vue
<template>
  <fan-table 
    :columns="columns" 
    :table-data="tableData"
    :virtual-scroll-option="virtualScrollOption"
    :checkbox-option="checkboxOption"
    :sort-option="sortOption"
    row-key-field-name="id"
  />
</template>
```

### 3. 事件处理

```javascript
export default {
  data() {
    return {
      checkboxOption: {
        selectedRowChange: ({ row, isSelected, selectedRowKeys }) => {
          console.log('选中行变化:', row, isSelected, selectedRowKeys)
        }
      }
    }
  }
}
```

## 注意事项

1. **rowKeyFieldName**: 必须设置正确的行键字段名，用于行操作功能
2. **虚拟滚动**: 大数据量时建议开启虚拟滚动以提升性能
3. **列配置**: 确保每列都有唯一的 key 值
4. **事件回调**: 注意事件回调函数的参数格式和返回值
5. **样式兼容**: 自定义样式时注意与组件默认样式的兼容性 