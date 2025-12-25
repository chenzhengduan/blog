const config = {
    "chartClick":Symbol("图表点击"),
    "hash-change": Symbol("路由发生变化"),

    'arrange-table-list-refresh':Symbol('排课列表刷新'),
    'arrange-info-refresh':Symbol('排课信息刷新'),
    'adjust-course-student-refresh':Symbol('调课学员列表刷新'),
    // 课程详情 Popover 统一动作与刷新事件（命名空间化，避免冲突）
    'arrange-course-detail-action': Symbol('课程详情动作分发'),
    'arrange-course-detail-updated': Symbol('课程详情完成后刷新'),

    //多层重复嵌套会引起互相污染穿透得放到外面去唤起了
    'request-edit-schedule':Symbol('唤起修改日程弹窗'),
    'request-edit-arrange':Symbol('唤起修改排课弹窗'),
    'request-open-period-range-setting':Symbol('唤起时段范围设置弹窗'),

    // 更新排课查询条件
    'update-schedule-query': Symbol('更新排课查询条件'),
    
    // 拖拽排课模式相关
    'enter-drag-arrange-mode': Symbol('进入拖拽排课模式'),
    'exit-drag-arrange-mode': Symbol('退出拖拽排课模式'),
    'toggle-filter-expanded': Symbol('切换筛选条件展开状态')
};

import createEvent from "@common/tool/event/event";
const useEvent = createEvent(config);
export default useEvent;
