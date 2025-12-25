import { defineStore } from 'pinia'
import { getAllTimetablePreference } from '@/api/arrange'
import type { TimetablePreference_ViewModel, CourseTimetableTypeEnum, CourseTimetableFieldSetting } from '@/types/model/timetable-preference'
import {
  DEFAULT_TIME_RANGE_START,
  DEFAULT_TIME_RANGE_END,
  DEFAULT_ROW_HEIGHT,
  DEFAULT_LEFT_FIELD_KEYS,
  DEFAULT_LEFT_FIELD_DISPLAY_NAME,
  DEFAULT_RIGHT_TAG_CANDIDATES,
  DEFAULT_ENABLED_TAG_KEYS,
  ALL_COLUMNS,
} from '@/constants/timetablePreferencesDefaults'

type TimetableTypeKey = string

interface PreferencesState {
  // 缓存：不同 TimetableType 的偏好（使用普通对象确保更好地触发响应）
  byType: Record<TimetableTypeKey, TimetablePreference_ViewModel>
  // 是否已整体加载过（避免重复请求）
  hasLoadedOnce: boolean
}

// 构建默认视图模型的纯函数（可在 getter 和 action 中使用）
// 与 timetablePreferenceSettings.vue 的默认处理逻辑保持一致
function buildDefaultViewModelInternal(type: CourseTimetableTypeEnum | string): TimetablePreference_ViewModel {
  const key = String(type) as CourseTimetableTypeEnum
  
  // 左侧主字段：从 ALL_COLUMNS 查找字段信息（与 timetablePreferenceSettings.vue 一致）
  const leftFields: CourseTimetableFieldSetting[] = [
    {
      FieldName: 'StartTime',
      DisplayName: '上课时间',
      IsEnabled: true,
      SortOrder: 0,
      IsDefault: true,
      FieldType: 'Main' as any,
    },
    ...(DEFAULT_LEFT_FIELD_KEYS as readonly string[])
      .map((k, index) => (ALL_COLUMNS as unknown as any[]).find(c => c.FieldName === k))
      .filter(Boolean)
      .map((c: any, index: number) => ({
        FieldName: c.FieldName,
        DisplayName: c.DisplayName,
        IsEnabled: true,
        SortOrder: index + 1,
        IsDefault: false,
        FieldType: 'Main' as any,
      })),
  ]

  // 右侧标签：使用 DEFAULT_RIGHT_TAG_CANDIDATES，从 ALL_COLUMNS 查找 DisplayName
  const enabledTagKeys = new Set(DEFAULT_ENABLED_TAG_KEYS as readonly string[])
  const rightTags: CourseTimetableFieldSetting[] = (DEFAULT_RIGHT_TAG_CANDIDATES as readonly any[]).map((t, idx) => {
    const column = (ALL_COLUMNS as unknown as any[]).find(c => c.FieldName === t.FieldName)
    return {
      FieldName: t.FieldName,
      DisplayName: column?.DisplayName || t.DisplayName,
      IsEnabled: enabledTagKeys.has(t.FieldName),
      SortOrder: leftFields.length + idx + 1,
      IsDefault: true,
      FieldType: 'Tag' as any,
    }
  })

  return {
    ID: '',
    UserID: '',
    TimetableType: key,
    TimetableTypeName: null,
    CardShowInformationSettings: [...leftFields, ...rightTags],
    IsEnabled: true,
    CreateTime: '',
    UpdateTime: '',
    TimeViewStart: DEFAULT_TIME_RANGE_START,
    TimeViewEnd: DEFAULT_TIME_RANGE_END,
    RowHeight: DEFAULT_ROW_HEIGHT,
  }
}

export const useTimetablePreferences = defineStore('timetablePreferences', {
  state: (): PreferencesState => ({
    byType: {} as Record<TimetableTypeKey, TimetablePreference_ViewModel>,
    hasLoadedOnce: false,
  }),

  getters: {
    // 获取指定类型的偏好（如果没有则返回默认值）
    preferenceByType(state) {
      return (type: CourseTimetableTypeEnum | string): TimetablePreference_ViewModel | null => {
        const key = String(type)
        const existing = state.byType[key]
        if (existing) {
          // 如果存在但 CardShowInformationSettings 为空，使用默认值填充
          if (!existing.CardShowInformationSettings || existing.CardShowInformationSettings.length === 0) {
            const defaultVm = buildDefaultViewModelInternal(type)
            return {
              ...existing,
              CardShowInformationSettings: defaultVm.CardShowInformationSettings,
            }
          }
          return existing
        }
        // 如果没有数据，返回默认值
        return buildDefaultViewModelInternal(type)
      }
    },
  },

  actions: {
    // 修复 CardShowInformationSettings，确保标签字段完整
    fixCardShowInformationSettings(settings: CourseTimetableFieldSetting[]): CourseTimetableFieldSetting[] {
      if (!Array.isArray(settings) || settings.length === 0) {
        return []
      }

      // 分离主字段和标签字段
      const mainFields = settings.filter(f => f.FieldType === 'Main' as any)
      let tagFields = settings.filter(f => f.FieldType === 'Tag' as any)

      // 如果标签字段为空，使用默认标签候选生成
      if (!tagFields || tagFields.length === 0) {
        const enabledTagKeys = new Set(DEFAULT_ENABLED_TAG_KEYS as readonly string[])
        tagFields = (DEFAULT_RIGHT_TAG_CANDIDATES as readonly any[]).map((t: any, idx: number) => {
          const column = (ALL_COLUMNS as unknown as any[]).find(c => c.FieldName === t.FieldName)
          return {
            FieldName: t.FieldName,
            DisplayName: column?.DisplayName || t.DisplayName,
            IsEnabled: enabledTagKeys.has(t.FieldName),
            IsDefault: false,
            SortOrder: mainFields.length + idx + 1,
            FieldType: 'Tag' as any,
          }
        })
        return [...mainFields, ...tagFields]
      }

      // 对比 DEFAULT_RIGHT_TAG_CANDIDATES，补充缺失的字段，保留后端返回的字段
      const defaultFieldNames = new Set(DEFAULT_RIGHT_TAG_CANDIDATES.map((t: any) => t.FieldName))
      const existingFieldNames = new Set(tagFields.map(f => f.FieldName))

      // 找出缺失的字段（在DEFAULT中但不在后端返回中的）
      const missingFields = DEFAULT_RIGHT_TAG_CANDIDATES.filter(
        (t: any) => !existingFieldNames.has(t.FieldName)
      )

      // 找出多出来的字段（在后端返回中但不在DEFAULT中的）
      const extraFields = tagFields.filter(f => !defaultFieldNames.has(f.FieldName || ''))

      // 如果有缺失或多余的字段，进行合并处理
      if (missingFields.length > 0 || extraFields.length > 0) {
        // 从 ALL_COLUMNS 中查找缺失字段的完整信息，生成默认标签对象
        const missingTagFields: CourseTimetableFieldSetting[] = missingFields.map((t: any, idx: number) => {
          const column = (ALL_COLUMNS as unknown as any[]).find(c => c.FieldName === t.FieldName)
          return {
            FieldName: t.FieldName,
            DisplayName: column?.DisplayName || t.DisplayName,
            IsEnabled: false,
            IsDefault: false,
            SortOrder: tagFields.length + idx + 1,
            FieldType: 'Tag' as any,
          }
        })

        // 合并：保留后端返回的标签字段（包括多出来的）+ 补充缺失的字段
        const fixedTagFields = [...tagFields, ...missingTagFields]

        // 返回修复后的完整设置
        return [...mainFields, ...fixedTagFields]
      }

      // 如果没有缺失或多余的字段，直接返回原设置
      return settings
    },

    buildDefaultViewModel(type: CourseTimetableTypeEnum | string): TimetablePreference_ViewModel {
      return buildDefaultViewModelInternal(type)
    },

    // 进入日历前调用，若已加载则不再请求
    async ensureLoaded() {
      if (this.hasLoadedOnce && Object.keys(this.byType).length > 0) return
      await this.refreshAll()
    },

    // 强制刷新全部偏好
    async refreshAll() {
      const resp = await getAllTimetablePreference()
      this.byType = {}
      if (resp.ErrorCode === 200) {
        const list = (resp.Data || []) as TimetablePreference_ViewModel[]

        // 先为所有类型放入默认值
        const allTypes: (CourseTimetableTypeEnum | string)[] = [
          'TeacherTimetable',
          'StudentTimetable',
          'ClassTimetable',
          'ClassroomTimetable',
          'TimeTimetable',
        ]
        for (const t of allTypes) {
          this.byType[String(t)] = buildDefaultViewModelInternal(t)
        }

        // 用服务端返回覆盖默认值（若有）
        for (const item of list) {
          const key = String(item.TimetableType)
          // 确保服务端数据至少含有时间范围
          if (!item.TimeViewStart) item.TimeViewStart = DEFAULT_TIME_RANGE_START
          if (!item.TimeViewEnd) item.TimeViewEnd = DEFAULT_TIME_RANGE_END
          if (!item.RowHeight) item.RowHeight = DEFAULT_ROW_HEIGHT
          
          // 容错：修复 CardShowInformationSettings，确保标签字段完整
          if (item.CardShowInformationSettings) {
            item.CardShowInformationSettings = this.fixCardShowInformationSettings(item.CardShowInformationSettings)
          } else {
            // 如果为空，使用默认值
            const defVm = buildDefaultViewModelInternal(item.TimetableType)
            item.CardShowInformationSettings = defVm.CardShowInformationSettings
          }
          
          this.byType[key] = item
        }
      } else {
        // 接口错误也提供默认值，避免空引用
        const fallbacks: (CourseTimetableTypeEnum | string)[] = [
          'TeacherTimetable',
          'StudentTimetable',
          'ClassTimetable',
          'ClassroomTimetable',
          'TimeTimetable',
        ]
        for (const t of fallbacks) {
          this.byType[String(t)] = buildDefaultViewModelInternal(t)
        }
      }
      this.hasLoadedOnce = true
    },

    // 保存成功后更新单个类型的缓存
    updateOne(type: CourseTimetableTypeEnum | string, value: Partial<TimetablePreference_ViewModel>) {
      const key = String(type)
      const existing = this.byType[key]
      const merged = { ...(existing || buildDefaultViewModelInternal(type)), ...value } as TimetablePreference_ViewModel
      
      // 容错：修复 CardShowInformationSettings，确保标签字段完整
      if (merged.CardShowInformationSettings) {
        merged.CardShowInformationSettings = this.fixCardShowInformationSettings(merged.CardShowInformationSettings)
      } else {
        // 如果为空，使用默认值
        const defVm = buildDefaultViewModelInternal(type)
        merged.CardShowInformationSettings = defVm.CardShowInformationSettings
      }
      
      // 重新赋值以确保触发响应
      this.byType = { ...this.byType, [key]: merged }
    },

    // 批量覆盖某些类型
    updateMany(items: TimetablePreference_ViewModel[]) {
      const next: Record<string, TimetablePreference_ViewModel> = { ...this.byType }
      for (const item of items) {
        const key = String(item.TimetableType)
        
        // 确保服务端数据至少含有时间范围
        if (!item.TimeViewStart) item.TimeViewStart = DEFAULT_TIME_RANGE_START
        if (!item.TimeViewEnd) item.TimeViewEnd = DEFAULT_TIME_RANGE_END
        if (!item.RowHeight) item.RowHeight = DEFAULT_ROW_HEIGHT
        
        // 容错：修复 CardShowInformationSettings，确保标签字段完整
        if (item.CardShowInformationSettings) {
          item.CardShowInformationSettings = this.fixCardShowInformationSettings(item.CardShowInformationSettings)
        } else {
          // 如果为空，使用默认值
          const defVm = buildDefaultViewModelInternal(item.TimetableType)
          item.CardShowInformationSettings = defVm.CardShowInformationSettings
        }
        
        next[key] = item
      }
      this.byType = next
    },
  },
})


