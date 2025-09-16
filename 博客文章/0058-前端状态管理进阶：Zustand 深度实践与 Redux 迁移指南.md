# 前端状态管理进阶：Zustand 深度实践与 Redux 迁移指南

> 在完成《0054-前端状态管理深度实践：从Redux到Zustand的现代化状态管理方案》后，本篇聚焦 Zustand 的高阶用法与可落地的迁移实践。

## 0. 引言
Zustand 以极小核心、极低心智负担和优秀性能著称，适合中小型到中大型 React 应用的“应用状态 + 视图状态”管理。本篇从工程实战视角给出可直接复用的模式与代码骨架。

## 1. 为什么选择 Zustand
- 轻量：无样板代码，学习曲线低
- 性能：细粒度订阅 + 选择器 + shallow 比较
- 生态：持久化、Devtools、Immer 等中间件开箱即用
- 渐进式：与 React Query、Redux 共存或平滑迁移

## 2. Store 设计：按领域分片（slices）
```ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'

// 用户分片
interface UserSlice {
  profile: { id: string; name: string } | null
  setProfile: (p: UserSlice['profile']) => void
  logout: () => void
}
const createUserSlice = (): UserSlice => ({
  profile: null,
  setProfile: (p) => set({ user: { ...get().user, profile: p } }),
  logout: () => set({ user: { ...get().user, profile: null } })
}) as any

// UI 分片
interface UISlice {
  theme: 'light' | 'dark'
  toggleTheme: () => void
}
const createUISlice = (): UISlice => ({
  theme: 'light',
  toggleTheme: () => set({ ui: { ...get().ui, theme: get().ui.theme === 'light' ? 'dark' : 'light' } })
}) as any

// 组合 Store
type RootState = { user: UserSlice; ui: UISlice }
const useStore = create<RootState>()(
  devtools(
    persist((set, get) => {
      // 为了能在 slice 中访问 set/get，将其提升到闭包
      // 实际项目可使用工厂：createUserSlice(set, get)
      (global as any).set = set; (global as any).get = get
      return {
        user: createUserSlice(),
        ui: createUISlice()
      }
    }, { name: 'app-store' })
  )
)
export default useStore
```

要点：
- 将 set/get 提升为闭包或通过工厂函数注入，保证 slice 之间可组合
- 使用 persist 将关键状态持久化；结合版本号做迁移

## 3. 中间件组合与工程化
- devtools：时间旅行与状态可视化
- persist：细粒度持久化（只持久化 user.profile 等）
- immer：在复杂嵌套更新时提升可读性

```ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'
import { immer } from 'zustand/middleware/immer'

type CartState = {
  items: { id: string; qty: number }[]
  add: (id: string, qty?: number) => void
  remove: (id: string) => void
}

export const useCart = create<CartState>()(
  devtools(
    persist(
      immer((set) => ({
        items: [],
        add: (id, qty = 1) => set((s) => {
          const i = s.items.find((x) => x.id === id)
          if (i) i.qty += qty
          else s.items.push({ id, qty })
        }),
        remove: (id) => set((s) => { s.items = s.items.filter((x) => x.id !== id) })
      })),
      { name: 'cart', partialize: (s) => ({ items: s.items }) }
    )
  )
)
```

## 4. 性能优化与选择器
- 只订阅需要的片段：useStore((s) => s.ui.theme)
- 使用 shallow 避免对象引用变化导致重渲染
- 大列表/频繁更新场景优先使用原子字段或拆分 slice

```tsx
import { shallow } from 'zustand/shallow'
const themeAndName = useStore((s) => ({ theme: s.ui.theme, name: s.user.profile?.name }), shallow)
```

## 5. 异步与服务端数据的边界
- UI/本地状态用 Zustand；服务端数据用 React Query/SWR
- 在 Zustand 中仅存放“视图层面的衍生与控制”

```ts
// 轻量异步：在 action 内执行
const useAuth = create<{ loading: boolean; login: (u: string,p: string) => Promise<void> }>((set) => ({
  loading: false,
  async login(u,p){
    set({ loading: true })
    try { /* 调用 API */ } finally { set({ loading: false }) }
  }
}))
```

## 6. SSR 与 Hydration
- 持久化在 SSR 时关闭（根据运行环境判断）
- 将初始数据通过服务端注入，再在客户端 hydrate

## 7. 测试实践
- 用 jest + @testing-library/react；通过自定义 Hook 暴露 store
- 对 actions 测试副作用；对组件测试渲染与订阅

```ts
import useStore from './store'

test('toggle theme', () => {
  const { result } = { result: { current: useStore.getState() } } // 简化演示
  const prev = result.current.ui.theme
  useStore.getState().ui.toggleTheme()
  expect(useStore.getState().ui.theme).not.toBe(prev)
})
```

## 8. 从 Redux 迁移指南（实践清单）
- 切分领域：按 slice 迁移，先迁 UI/局部状态
- Action -> 函数式 action；Reducer -> 直接 set/immer
- Selector -> 组件内 useStore(selector) + shallow
- 中间件 -> 对应 devtools/persist/immer；异步交给 React Query
- 双栈过渡：Redux 与 Zustand 并存，逐步削减 Redux slice

## 9. 常见陷阱与建议
- 大对象状态更新导致重渲染：拆分字段或使用 shallow
- 滥用全局状态：组件内局部状态优先 useState
- 持久化数据的兼容：使用版本化与迁移函数
- 服务端数据不要塞进 Zustand，避免失真与缓存不一致

## 10. 结语与延伸阅读
- 搭配阅读：0054（体系全览）、0056（测试体系）、0057（微前端集成）
- 实战建议：先小范围落地（UI 状态、偏本地业务状态），再逐步替换

本篇给出了一套开箱即用的 Zustand 工程化骨架与迁移清单，适合在现有项目中“低风险、可回滚”地引入与验证。