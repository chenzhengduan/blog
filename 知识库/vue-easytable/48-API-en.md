# API - English

## Table Props

### Table Options

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| tableData | Table data | Array | - | - |
| footerData | Table footer summary data, The data structure is consistent with tableData | Array | - | - |
| columns | Column option. See the following table columns option for specific items | Array | - | - |
| showHeader | Show header | Boolean | - | true |
| fixedHeader | Is the header fixed, Enabled by default. It needs to be used in combination with `maxHeight` | Boolean | - | true |
| fixedFooter | Is the footer fixed, Enabled by default. It needs to be used in combination with `maxHeight` | Boolean | - | true |
| scrollWidth | The width of the table's scroll area (the width of the start scroll bar). Number: Specify pixels; String: Specified percentage | Number、String | - | - |
| maxHeight | The maximum height of the table. Number: Specify pixels; String: Specified percentage | Number、String | - | - |
| rowKeyFieldName | Specifies the field name of the row key. Used for row expand、row radio、row checkbox、row highlight、virtual scrolling | String | - | - |
| borderAround | Show table outer border | Boolean | - | true |
| borderX | Show column horizontal border | Boolean | - | true |
| borderY | Show column vertical border | Boolean | - | false |
| cellSpanOption | Cell merge option, Refer to cellSpanOption option for details | Object | - | - |
| cellStyleOption | Cell style option, Refer to cellStyleOption option for details | Object | - | - |
| rowStyleOption | For row style option, Refer to rowStyleOption option for details | Object | - | - |
| expandOption | Row expand option, Refer to expandOption option for details | Object | - | - |
| checkboxOption | Row multiple selection option, Refer to checkboxOption option for details | Object | - | - |
| radioOption | Row radio option, Refer to radioOption option for details | Object | - | - |
| virtualScrollOption | Virtual scroll option, it is recommended to display more than 1000 rows at a time. Refer to virtualScrollOption option for details | Object | - | - |
| sortOption | Sort option, Refer to sortOption option for details | Object | - | - |
| cellSelectionOption | Cell selection option, Refer to cellSelectionOption option for details | Object | - | - |
| editOption | Cell edit option, Refer to editOption for details | Object | - | - |
| contextmenuHeaderOption | table header contextmenu option, Refer to contextmenuHeaderOption | Object | - | - |
| contextmenuBodyOption | table body contextmenu option, Refer to contextmenuBodyOption | Object | - | - |
| eventCustomOption | Custom event option, Refer to eventCustomOption option for details | Object | - | - |
| cellAutofillOption | Cell autofill option, Refer to cellAutofillOption option for details | Object | - | - |
| clipboardOption | Clipboard Option, Refer to clipboardOption option for details | Object | - | - |

### Column Options

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| field | The field of the column | String | - | - |
| key | Unique key value for each column | String | - | - |
| type | Column type. "expand": row expand; "checkbox": row checkbox; "radio": row radio | String | "expand"、"checkbox"、"radio" | - |
| title | Column header text | String | - | - |
| width | Number: width pixel value; String: width percentage | String、Number | - | - |
| align | Cell alignment | String | "left"、"center"、"right" | "center" |
| operationColumn | is operation column | Boolean | - | false |
| edit | Enable cell edit | Boolean | - | false |
| sortBy | Sort rules. 1、sortBy="": Sorting allowed without collation; 2、sortBy="asc": Default current column ascending; 3、sortBy="desc": Default current column descending | String | ""、"desc"、"asc" | "" |
| renderBodyCell | 1、Custom cell rendering function in the table body. jsx, Writing is close to template syntax 2、Parameter information. row: Current row data、column: Current column option、rowIndex: Row index、h: createElement 3、For more JSX knowledge, please refer to Vue.js Official doc | Function({row,column,rowIndex},h):VNode | - | - |
| renderHeaderCell | 1、Header custom cell rendering function. The usage is the same as renderBodyCell. 2、Parameter information. column: Current column option、h: createElement | Function({ column },h):VNode | - | - |
| renderFooterCell | 1、footer custom cell rendering function. 2、Parameter information. row: Current row data、column: Current column option、rowIndex: Row index、h: createElement | Function({row,column,rowIndex},h):VNode | - | - |
| disableResizing | Disable resizing for this column. Only effective if columnWidthResizeOption is enabled | Boolean | - | false |
| ellipsis | Cell ellipsis option | Object | - | - |
| filter | Filter option | Object | - | - |
| filterCustom | Filter custom option | Object | - | - |

## Table Instance Methods

| Methods Name | Description | Parameters |
|--------------|-------------|------------|
| scrollTo | Scrolls the table to the specified position | Refer to MDN scrollTo |
| scrollToRowKey | Scroll the table to the column position | {rowKey} |
| setHighlightRow | Set highlight row | {rowKey} |
| startEditingCell | Start cell editing | {rowKey,colKey,defaultValue} |
| stopEditingCell | Stop cell editing | - |
| hideColumnsByKeys | Hide columns | keys |
| showColumnsByKeys | Show columns | keys |
| setCellSelection | Set single cell selection | { rowKey, colKey } |
| setAllCellSelection | Set all cell selection | - |
| setRangeCellSelection | Set range cell selection | { startRowKey,startColKey,endRowKey,endColKey,isScrollToStartCell } |
| getRangeCellSelection | Get the information of the range selection. Returns the indexes and key information of the selected region | {selectionRangeKeys,selectionRangeIndexes} |

## Cell Merge Option

### cellSpanOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| bodyCellSpan | 1、Body cell merge function 2、Parameter information. row: Current row data、column: Current column option、rowIndex: Row index | Function({row,column,rowIndex}) | - | - |
| footerCellSpan | 1、footer cell merge function 2、Parameter information. row: Current row data、column: Current column option、rowIndex: Row index | Function({row,column,rowIndex}) | - | - |

## Column Hidden Option

### columnHiddenOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| defaultHiddenColumnKeys v2.11.0 | Set default hidden columns | Array | - | - |

## Cell Style Option

### cellStyleOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| bodyCellClass | 1、Table body cell style 2、Received 3 parameters, row: Current row data、column: Current column option、rowIndex: Row index | Function({row,column,rowIndex}) | - | - |
| headerCellClass | 1、Header cell style 2、Received 2 parameters, column: Current column option、rowIndex: Row index | Function({column,rowIndex}) | - | - |
| footerCellClass | 1、Footer cell style 2、Received 3 parameters, row: Current row data、column: Current column option、rowIndex: Row index | Function({row,column,rowIndex}) | - | - |

## Row Style Option

### rowStyleOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| hoverHighlight | row hover background highlight | Boolean | - | true |
| clickHighlight | row click background highlight | Boolean | - | true |
| stripe | row stripe | Boolean | - | false |

## Row Expand Option

### expandOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| expandable | 1、Whether the row rendering function is allowed to expand. Returns a Boolean value 2、Receive 3 parameter, row: Current row data、column: Column option、rowIndex: rowIndex | Function({row,column,rowIndex}) | - | - |
| render | 1、Render functions 2、The parameters received by the render function. row: Current row data、column: Column option、rowIndex: rowIndex、h: createElement | Function({row,column,rowIndex},h):VNode | - | - |
| defaultExpandAllRows | is expand all row | Boolean | - | false |
| defaultExpandedRowKeys | The default expanded row key. When parameter defaultExpandAllRows and parameter defaultExpandedRowKeys exist at the same time, priority of use defaultExpandAllRows | String[]、Number[] | - | - |
| expandedRowKeys | Controllable attributes of expand row, After setting the property, defaultExpandAllRows and defaultExpandedRowKeys will be invalid. refer to examples for details | String[]、Number[] | - | - |
| beforeExpandRowChange | 1、Expand functions before switching, If false is returned, execution is interrupted. 2、Receive 3 parameter, beforeExpandedRowKeys: All expanded keys before the change, row: The current row data, rowIndex: row index | Function({beforeExpandedRowKeys,row,rowIndex}) | - | - |
| afterExpandRowChange | 1、Expand the function after switching. 2、Receive 3 parameter, afterExpandedRowKeys: All expanded keys after change, row: Current row data, rowIndex: row index | Function({afterExpandedRowKeys,row,rowIndex}) | - | - |
| trigger | Expand the row event trigger type. icon: expand by click the icon; cell: expand by click cell; row: expand by click row | String | "icon"、"cell"、"row" | "icon" |

## Row Multiple Selection Option

### checkboxOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| defaultSelectedAllRows | Is selected all by default | Boolean | - | false |
| defaultSelectedRowKeys | Default selected row keys | String[]、Number[] | - | - |
| disableSelectedRowKeys | Disable selected row keys | String[]、Number[] | - | - |
| selectedRowKeys | The controllable properties. After setting the property, defaultSelectedAllRows and defaultSelectedRowKeys will be invalid. Refer to example | String[]、Number[] | - | - |
| selectedRowChange | Change event for the selected row. Receive 3 parameter, row: Current row data, isSelected: Whether the current row is selected, selectedRowKeys: All selected rowKey information | Function({row, isSelected, selectedRowKeys}) | - | - |
| selectedAllChange | Select all change events. The event receives 2 parameters, isSelected: Select all or not. selectedRowKeys: All selected rowKey information | Function({isSelected, selectedRowKeys}) | - | - |
| hideSelectAll | Is hide select all button | Boolean | - | false |

## Row Radio Option

### radioOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| defaultSelectedRowKey | Default selected row key | String、Number | - | - |
| disableSelectedRowKeys | Disable selected row keys | String[]、Number[] | - | - |
| selectedRowKey | The controllable properties of the selected row, After setting the property, defaultSelectedRowKey will be invalid. Refer to example | String、Number | - | - |
| selectedRowChange | Change event for the selected row. Method receives 1 parameter, row: Current row data | Function({row}) | - | - |

## Virtual Scroll Option

### virtualScrollOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| enable | Enable virtual scrolling | Boolean | - | false |
| minRowHeight | The min row height (PX). The smaller the value is, the more row is rendered in the table visualization range. It can be set according to the actual minimum height | Number | - | 40 |
| scrolling | Scrolling callback events. startRowIndex is the line number currently starting rendering, visibleStartIndex is the starting line number of the currently visible area, visibleEndIndex is the end line number of the currently visible area, visibleAboveCount is the number of renderings above the currently visible area, visibleBelowCount is the number of renderings below the currently visible area | Function({startRowIndex,visibleStartIndex,visibleEndIndex,visibleAboveCount,visibleBelowCount}) | - | - |
| bufferScale | Buffer scale. 1 buffer scale is the number of rows within the current table height | Number | - | 1 |

## Sort Option

### sortOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| multipleSort | Enable multi field sorting | Boolean | - | false |
| sortAlways | Whether to turn on sorting is only switched between ascending and descending | Boolean | - | false |
| sortChange | Sort change events. Event receives 1 Parameter object: Sort rules for columns | Function({row}) | - | - |

## Cell Selection Option

### cellSelectionOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| enable | enable cell selection | Boolean | - | true |

## Cell Edit Option

### editOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| beforeStartCellEditing | before start editing cell callback method. row: Current row data, column: Current column, cellValue: Current cell value. If false is returned, Will prevent the cell from starting the editing state | Function({ row, column,cellValue }) | - | - |
| beforeCellValueChange | before cell value change callback method. row: Current row data, column: Current column, changeValue: change value. If false is returned, cell editing will be blocked, the cell will back to the state before editing | Function({ row, column,changeValue }) | - | - |
| afterCellValueChange | after cell value change callback method. row: Current row data, column: Current column, changeValue: change value | Function({ row, column,changeValue }) | - | - |
| cellValueChange | Will be removed | Function({ row, column }) | - | - |

## Header Contextmenu Option

### contextmenuHeaderOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| beforeShow | For the callback event before the menu is displayed, you can change the menu item information at this stage. isWholeColSelection: It's whole column selection, selectionRangeKeys: The currently cellSelection key information, selectionRangeIndexes: The currently cellSelection index information | Function({ isWholeColSelection, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| afterMenuClick | Callback when a menu item is clicked, returning false will prevent the current right-click operation. type: menu item, selectionRangeKeys: The currently cellSelection key information, selectionRangeIndexes: The currently cellSelection index information | Function({ type, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| contextmenus | contextmenu option. contextmenu component | Array | - | - |

## Body Contextmenu Option

### contextmenuBodyOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| beforeShow | For the callback event before the menu is displayed, you can change the menu item information at this stage. isWholeRowSelection: It's whole row selection, selectionRangeKeys: The currently cellSelection key information, selectionRangeIndexes: The currently cellSelection index information | Function({ isWholeRowSelection, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| afterMenuClick | Callback when a menu item is clicked, returning false will prevent the current right-click operation. type: menu item, selectionRangeKeys: The currently cellSelection key information, selectionRangeIndexes: The currently cellSelection index information | Function({ type, selectionRangeKeys, selectionRangeIndexes }) | - | - |
| contextmenus | contextmenu option. contextmenu component | Array | - | - |

## Event Custom Option

### eventCustomOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| bodyRowEvents | 1、body row custom events, Returns the event that needs to be customized. 2、Receive 2 parameters. row: Current row data、rowIndex: Row index 3、Support for custom events: click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,rowIndex}) | - | - |
| bodyCellEvents | 1、body column custom events, Returns the event that needs to be customized. 2、Receive 3 parameters. row: Current row data、column: Current column option、rowIndex: Row index 3、Support for custom events: click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,column,rowIndex}) | - | - |
| headerRowEvents | 1、header row custom events, Returns the event that needs to be customized. 2、Receive 1 parameters. rowIndex: header row index 3、Support for custom events: click、dblclick、contextmenu、mouseenter、mouseleave | Function({rowIndex}) | - | - |
| headerCellEvents | 1、header column custom events, Returns the event that needs to be customized. 2、Receive 2 parameters. column: Current column option、rowIndex: Row index 3、Support for custom events: click、dblclick、contextmenu、mouseenter、mouseleave | Function({column,rowIndex}) | - | - |
| footerRowEvents | 1、footer row custom events, Returns the event that needs to be customized. 2、Receive 2 parameters. row: Current row data、rowIndex: Row index 3、Support for custom events: click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,rowIndex}) | - | - |
| footerCellEvents | 1、footer column custom events, Returns the event that needs to be customized. 2、Receive 3 parameters. row: Current row data、column: Current column option、rowIndex: Row index 3、Support for custom events: click、dblclick、contextmenu、mouseenter、mouseleave | Function({row,column,rowIndex}) | - | - |

## Cell Autofill Option

### cellAutofillOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| directionX | enable horizontal autofill | Boolean | - | true |
| directionY | enable vertical autofill | Boolean | - | true |
| beforeAutofill | The callback method before cell autofilling. If false is returned, the autofilling will be cancelled. Parameter Description: 1、direction: autofill direction 2、sourceSelectionRangeIndexes: Autofilling the row and column indexes of the source 3、targetSelectionRangeIndexes: Autofilling the row and column indexes of the target 4、sourceSelectionData: autofilling source data, Excess will be removed automatically 5、targetSelectionData: autofilling target data | Function({direction,sourceSelectionRangeIndexes,targetSelectionRangeIndexes,sourceSelectionData,targetSelectionData}) | - | - |
| afterAutofill | The callback method after cell autofilling. Parameter Description: 1、direction: autofill direction 2、sourceSelectionRangeIndexes: Autofilling the row and column indexes of the source 3、targetSelectionRangeIndexes: Autofilling the row and column indexes of the target 4、sourceSelectionData: autofilling source data, Excess will be removed automatically 5、targetSelectionData: autofilling target data | Function({direction,sourceSelectionRangeIndexes,targetSelectionRangeIndexes,sourceSelectionData,targetSelectionData}) | - | - |

## Clipboard Option

### clipboardOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| copy | enable cell copy | Boolean | - | true |
| paste | enable cell paste | Boolean | - | true |
| cut | enable cell cut | Boolean | - | true |
| delete | enable cell delete | Boolean | - | true |
| beforeCopy | The callback method before cell copy. If false is returned, the copy will be canceled. Parameter Description: 1、data: copy data 2、selectionRangeIndexes: The indexes information of copy area 3、selectionRangeKeys: The keys information of copy area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| afterCopy | The callback method after cell copy. Parameter Description: 1、data: copy data 2、selectionRangeIndexes: The indexes information of copy area 3、selectionRangeKeys: The keys information of copy area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| beforePaste | The callback method before cell paste. If false is returned, the copy will be canceled. Parameter Description: 1、data: paste data 2、selectionRangeIndexes: The indexes information of paste area 3、selectionRangeKeys: The keys information of paste area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| afterPaste | The callback method after cell paste. Parameter Description: 1、data: paste data 2、selectionRangeIndexes: The indexes information of paste area 3、selectionRangeKeys: The keys information of paste area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| beforeCut | The callback method before cell cut. If false is returned, the copy will be canceled. Parameter Description: 1、data: cut data 2、selectionRangeIndexes: The indexes information of cut area 3、selectionRangeKeys: The keys information of cut area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| afterCut | The callback method after cell cut. Parameter Description: 1、data: cut data 2、selectionRangeIndexes: The indexes information of cut area 3、selectionRangeKeys: The keys information of cut area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| beforeDelete | The callback method before cell delete. If false is returned, the copy will be canceled. Parameter Description: 1、data: delete data 2、selectionRangeIndexes: The indexes information of delete area 3、selectionRangeKeys: The keys information of delete area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |
| afterDelete | The callback method after cell delete. Parameter Description: 1、data: delete data 2、selectionRangeIndexes: The indexes information of delete area 3、selectionRangeKeys: The keys information of delete area | Function({data, selectionRangeIndexes, selectionRangeKeys}) | - | - |

## Column Resize Option

### columnWidthResizeOption

| Parameters | Description | Type | Optional | Default |
|------------|-------------|------|----------|---------|
| enable | enable column resize | Boolean | - | false |
| minWidth | min width of resize column | Number | - | 30px |
| sizeChange | The callback method after column resize. Parameter Description: 1、column: resize column 2、differWidth: Width of the difference after column resize 3、columnWidth: column width after column resize | Function({column,differWidth,columnWidth}) | - | - |

## Usage Notes

### Basic Configuration
- **tableData**: Required, array of table data
- **columns**: Required, array of column configurations
- **rowKeyFieldName**: Required for certain features like selection, expansion, virtual scrolling

### Performance Optimization
- Use **virtualScrollOption** for large datasets (>1000 rows)
- Set appropriate **maxHeight** for better scrolling experience
- Use **fixedHeader** and **fixedFooter** for better UX

### Event Handling
- Most options provide before/after callback methods
- Return `false` in before callbacks to prevent default behavior
- Use **eventCustomOption** for custom event handling

### Advanced Features
- **Cell Selection**: Enable with cellSelectionOption
- **Cell Editing**: Configure with editOption
- **Cell Autofill**: Excel-like autofill functionality
- **Clipboard**: Copy, paste, cut, delete operations
- **Contextmenu**: Right-click menu customization

### Styling
- Use **cellStyleOption** for cell-level styling
- Use **rowStyleOption** for row-level styling
- Configure borders with **borderAround**, **borderX**, **borderY**

This API documentation provides comprehensive information about all available options and methods for the Table component.