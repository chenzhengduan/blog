/**
 * 业务表格示例配置
 * 参考 wtwo 项目的 class-table-course.vue 字段配置
 */

// 表格列配置示例 - 参考 class-table-course 的字段
export const scheduleTableColumns = [
  // 序号列
  {
    field: 'index',
    key: 'index',
    title: '序号',
    width: 60,
    align: 'center',
    fixed: 'left'
  },

  // 校区 - 下拉选择
  {
    field: 'CampusID',
    key: 'CampusID',
    title: '上课校区',
    width: 130,
    align: 'left',
    cellType: 'select',
    sortable: true, // 支持排序
    editorConfig: {
      // 使用业务自定义组件示例（在 ScheduleTable 中按需 import）
      // component: CustomSelect,
      placeholder: '请选择校区',
      filterable: true,
      clearable: true,
      options: [
        { label: '总部校区', value: '1' },
        { label: '分校区A', value: '2' },
        { label: '分校区B', value: '3' }
      ]
    }
  },

  // 班级 - 下拉选择
  {
    field: 'ClassID',
    key: 'ClassID',
    title: '上课班级',
    width: 115,
    align: 'left',
    cellType: 'select',
    sortable: true, // 支持排序
    editorConfig: {
      // component: CustomSelect,
      placeholder: '请选择班级',
      filterable: true,
      clearable: true,
      options: [
        { label: '一年级1班', value: 'c1' },
        { label: '一年级2班', value: 'c2' },
        { label: '二年级1班', value: 'c3' }
      ]
    }
  },

  // 学员 - 下拉选择（给学员排课时使用）
  {
    field: 'StudentUserID',
    key: 'StudentUserID',
    title: '上课学员',
    width: 115,
    align: 'left',
    cellType: 'select',
    editorConfig: {
      placeholder: '请选择学员',
      filterable: true,
      clearable: true,
      options: [
        { label: '张三', value: 's1' },
        { label: '李四', value: 's2' },
        { label: '王五', value: 's3' }
      ]
    }
  },

  // 课程 - 下拉选择
  {
    field: 'ShiftID',
    key: 'ShiftID',
    title: '上课课程',
    width: 115,
    align: 'left',
    cellType: 'select',
    sortable: true, // 支持排序
    editorConfig: {
      placeholder: '请选择课程',
      filterable: true,
      clearable: true,
      options: [
        { label: '数学基础班', value: 'shift1' },
        { label: '语文提高班', value: 'shift2' },
        { label: '英语强化班', value: 'shift3' }
      ]
    }
  },

  // 科目 - 下拉选择
  {
    field: 'SubjectID',
    key: 'SubjectID',
    title: '上课科目',
    width: 115,
    align: 'left',
    cellType: 'select',
    sortable: true, // 支持排序
    editorConfig: {
      placeholder: '请选择科目',
      filterable: true,
      clearable: true,
      options: [
        { label: '数学', value: 'sub1' },
        { label: '语文', value: 'sub2' },
        { label: '英语', value: 'sub3' },
        { label: '物理', value: 'sub4' },
        { label: '化学', value: 'sub5' }
      ]
    }
  },

  // 上课日期 - 日期选择器
  {
    field: 'Date',
    key: 'Date',
    title: '上课日期',
    width: 150,
    align: 'left',
    cellType: 'date',
    sortable: true, // 支持排序
    editorConfig: {
      type: 'date',
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
      placeholder: '请选择日期'
    }
  },

  // 上课时间 - 时间选择器（自定义）
  {
    field: 'timeRange',
    key: 'timeRange',
    title: '上课时间',
    width: 115,
    align: 'left',
    cellType: 'time',
    editorConfig: {
      placeholder: '请选择时间段'
    }
  },

  // 教室 - 下拉选择
  {
    field: 'ClassRoomID',
    key: 'ClassRoomID',
    title: '上课教室',
    width: 145,
    align: 'left',
    cellType: 'select',
    editorConfig: {
      placeholder: '请选择教室',
      filterable: true,
      clearable: true,
      options: [
        { label: '101教室', value: 'room1' },
        { label: '102教室', value: 'room2' },
        { label: '201教室', value: 'room3' },
        { label: '线上教室', value: 'room_online' }
      ]
    }
  },

  // 任课老师 - 下拉选择
  {
    field: 'MainTeacherID',
    key: 'MainTeacherID',
    title: '任课老师',
    width: 115,
    align: 'left',
    cellType: 'select',
    editorConfig: {
      placeholder: '请选择老师',
      filterable: true,
      clearable: true,
      options: [
        { label: '张老师', value: 't1' },
        { label: '李老师', value: 't2' },
        { label: '王老师', value: 't3' }
      ]
    }
  },

  // 助教 - 下拉选择（多选）
  {
    field: 'AssistantTeacherID',
    key: 'AssistantTeacherID',
    title: '助教',
    width: 115,
    align: 'left',
    cellType: 'select',
    editorConfig: {
      placeholder: '请选择助教',
      filterable: true,
      clearable: true,
      multiple: true,
      options: [
        { label: '赵助教', value: 'a1' },
        { label: '孙助教', value: 'a2' },
        { label: '周助教', value: 'a3' }
      ]
    }
  },

  // 线上课 - 下拉选择
  {
    field: 'CourseType',
    key: 'CourseType',
    title: '线上课',
    width: 90,
    align: 'left',
    cellType: 'select',
    editorConfig: {
      placeholder: '请选择',
      clearable: true,
      options: [
        { label: '是', value: '2' },
        { label: '否', value: '1' }
      ]
    }
  },

  // 开放预约 - 下拉选择
  {
    field: 'IsSubscribeCourse',
    key: 'IsSubscribeCourse',
    title: '开放预约',
    width: 95,
    align: 'left',
    cellType: 'select',
    editorConfig: {
      placeholder: '请选择',
      clearable: true,
      options: [
        { label: '是', value: '1' },
        { label: '否', value: '0' }
      ]
    }
  },

  // 可约人数 - 输入框
  {
    field: 'MaxStudentCount',
    key: 'MaxStudentCount',
    title: '可约人数',
    width: 100,
    align: 'left',
    cellType: 'input',
    editorConfig: {
      type: 'number',
      placeholder: '请输入数字',
      maxlength: 3
    }
  },

  // 开课人数 - 输入框
  {
    field: 'StartStudentCount',
    key: 'StartStudentCount',
    title: '开课人数',
    width: 100,
    align: 'left',
    cellType: 'input',
    editorConfig: {
      type: 'number',
      placeholder: '请输入数字',
      maxlength: 3
    }
  },

  // 对内备注 - 输入框
  {
    field: 'InternalRemark',
    key: 'InternalRemark',
    title: '对内备注',
    width: 150,
    align: 'left',
    cellType: 'input',
    editorConfig: {
      placeholder: '请输入备注',
      maxlength: 200
    }
  }
]

// 模拟数据
export const mockTableData = [
  {
    id: '1',
    CampusID: '1',
    ClassID: 'c1',
    ShiftID: 'shift1',
    SubjectID: 'sub1',
    Date: '2025-01-01',
    timeRange: '08:00~10:00',
    ClassRoomID: 'room1',
    MainTeacherID: 't1',
    AssistantTeacherID: ['a1'],
    CourseType: '1',
    IsSubscribeCourse: '0',
    MaxStudentCount: '',
    StartStudentCount: '',
    InternalRemark: '第一节课'
  },
  {
    id: '2',
    CampusID: '1',
    ClassID: 'c2',
    ShiftID: 'shift2',
    SubjectID: 'sub2',
    Date: '2025-01-02',
    timeRange: '10:00~12:00',
    ClassRoomID: 'room2',
    MainTeacherID: 't2',
    AssistantTeacherID: ['a2', 'a3'],
    CourseType: '2',
    IsSubscribeCourse: '1',
    MaxStudentCount: '30',
    StartStudentCount: '20',
    InternalRemark: '线上课程'
  },
  {
    id: '3',
    CampusID: '2',
    ClassID: 'c3',
    ShiftID: 'shift3',
    SubjectID: 'sub3',
    Date: '2025-01-03',
    timeRange: '14:00~16:00',
    ClassRoomID: 'room3',
    MainTeacherID: 't3',
    AssistantTeacherID: [],
    CourseType: '1',
    IsSubscribeCourse: '0',
    MaxStudentCount: '',
    StartStudentCount: '',
    InternalRemark: ''
  }
]
