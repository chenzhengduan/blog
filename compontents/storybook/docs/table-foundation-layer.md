# 表格组件底层基础能力设计

> 作者：前端架构师团队  
> 日期：2025-11-21  
> 版本：v1.0.0

## 📋 目录

1. [概述](#概述)
2. [事件系统](#事件系统)
3. [渲染引擎](#渲染引擎)
4. [内存管理](#内存管理)
5. [数据结构优化](#数据结构优化)
6. [核心工具库](#核心工具库)
7. [性能监控](#性能监控)

---

## 🎯 概述

### 设计原则

1. **零依赖**：底层工具库不依赖任何第三方库
2. **高性能**：所有实现都经过性能优化
3. **类型安全**：完整的 TypeScript 类型支持
4. **可测试**：每个模块都可独立测试
5. **可扩展**：预留扩展点，支持自定义

### 文件结构

```
src/components/common/base-table/
├── core/                          # 核心底层能力
│   ├── eventBus.ts                # 事件系统
│   ├── renderer.ts                # 渲染引擎
│   ├── differ.ts                  # Diff 算法
│   ├── memoryManager.ts           # 内存管理
│   ├── dataIndex.ts               # 数据索引
│   └── performanceMonitor.ts      # 性能监控
├── utils/                         # 工具函数库
│   ├── index.ts                   # 工具函数集合
│   ├── types.ts                   # 类型定义
│   └── constants.ts               # 常量定义
└── hooks/                         # Composition API
    ├── useEventBus.ts             # 事件总线 Hook
    ├── useRenderer.ts             # 渲染器 Hook
    └── useMemory.ts               # 内存管理 Hook
```

---

## 🚀 事件系统

### 设计目标

- 类型安全的发布订阅模式
- 支持事件优先级
- 支持一次性监听
- 异步事件处理
- 性能优化（事件去重、批处理）

### 核心实现

```typescript
// core/eventBus.ts

type EventCallback<T = any> = (data: T) => void | Promise<void>
type EventMap = Record<string, any>

interface EventListener {
  callback: EventCallback
  once: boolean
  priority: number
}

interface EventConfig {
  once?: boolean      // 是否只触发一次
  priority?: number   // 优先级（数字越大优先级越高）
}

/**
 * 类型安全的事件总线
 * @template Events 事件类型映射
 */
class EventBus<Events extends EventMap = EventMap> {
  private events = new Map<keyof Events, Set<EventListener>>()
  private eventCount = new Map<keyof Events, number>()
  private debug = false
  
  /**
   * 订阅事件
   * @param event 事件名
   * @param callback 回调函数
   * @param config 配置选项
   * @returns 取消订阅函数
   */
  on<K extends keyof Events>(
    event: K,
    callback: EventCallback<Events[K]>,
    config?: EventConfig
  ): () => void {
    if (!this.events.has(event)) {
      this.events.set(event, new Set())
    }
    
    const listener: EventListener = {
      callback,
      once: config?.once || false,
      priority: config?.priority || 0
    }
    
    this.events.get(event)!.add(listener)
    
    if (this.debug) {
      console.log(`[EventBus] Subscribed to "${String(event)}"`, { config })
    }
    
    // 返回取消订阅函数
    return () => this.off(event, callback)
  }
  
  /**
   * 一次性订阅
   */
  once<K extends keyof Events>(
    event: K,
    callback: EventCallback<Events[K]>
  ): () => void {
    return this.on(event, callback, { once: true })
  }
  
  /**
   * 取消订阅
   */
  off<K extends keyof Events>(
    event: K,
    callback?: EventCallback<Events[K]>
  ): void {
    if (!this.events.has(event)) return
    
    const listeners = this.events.get(event)!
    
    if (callback) {
      // 移除特定回调
      for (const listener of listeners) {
        if (listener.callback === callback) {
          listeners.delete(listener)
          break
        }
      }
    } else {
      // 移除所有回调
      listeners.clear()
    }
    
    if (this.debug) {
      console.log(`[EventBus] Unsubscribed from "${String(event)}"`)
    }
  }
  
  /**
   * 触发事件（异步）
   */
  async emit<K extends keyof Events>(
    event: K,
    data: Events[K]
  ): Promise<void> {
    if (!this.events.has(event)) return
    
    const listeners = Array.from(this.events.get(event)!)
      .sort((a, b) => b.priority - a.priority) // 按优先级排序
    
    // 统计触发次数
    this.eventCount.set(event, (this.eventCount.get(event) || 0) + 1)
    
    if (this.debug) {
      console.log(`[EventBus] Emit "${String(event)}"`, { 
        data, 
        listenerCount: listeners.length 
      })
    }
    
    for (const listener of listeners) {
      try {
        await listener.callback(data)
        
        // 一次性监听器触发后移除
        if (listener.once) {
          this.events.get(event)!.delete(listener)
        }
      } catch (error) {
        console.error(`[EventBus] Error in listener for "${String(event)}":`, error)
      }
    }
  }
  
  /**
   * 同步触发（不等待异步回调）
   */
  emitSync<K extends keyof Events>(event: K, data: Events[K]): void {
    if (!this.events.has(event)) return
    
    const listeners = Array.from(this.events.get(event)!)
      .sort((a, b) => b.priority - a.priority)
    
    this.eventCount.set(event, (this.eventCount.get(event) || 0) + 1)
    
    for (const listener of listeners) {
      try {
        listener.callback(data)
        
        if (listener.once) {
          this.events.get(event)!.delete(listener)
        }
      } catch (error) {
        console.error(`[EventBus] Error in listener for "${String(event)}":`, error)
      }
    }
  }
  
  /**
   * 批量触发事件
   */
  emitBatch<K extends keyof Events>(events: Array<{ event: K; data: Events[K] }>): void {
    events.forEach(({ event, data }) => this.emitSync(event, data))
  }
  
  /**
   * 获取事件统计
   */
  getStats() {
    const stats: Record<string, any> = {}
    
    this.events.forEach((listeners, event) => {
      stats[String(event)] = {
        listenerCount: listeners.size,
        triggerCount: this.eventCount.get(event) || 0
      }
    })
    
    return stats
  }
  
  /**
   * 清空所有事件
   */
  clear(): void {
    this.events.clear()
    this.eventCount.clear()
  }
  
  /**
   * 获取所有事件名
   */
  getEventNames(): (keyof Events)[] {
    return Array.from(this.events.keys())
  }
  
  /**
   * 启用/禁用调试模式
   */
  setDebug(enabled: boolean): void {
    this.debug = enabled
  }
  
  /**
   * 判断事件是否有监听器
   */
  hasListeners<K extends keyof Events>(event: K): boolean {
    const listeners = this.events.get(event)
    return !!listeners && listeners.size > 0
  }
}

// 表格事件类型定义
interface TableEvents {
  // 单元格事件
  'cell:click': { row: any; column: any; event: MouseEvent }
  'cell:dblclick': { row: any; column: any; event: MouseEvent }
  'cell:contextmenu': { row: any; column: any; event: MouseEvent }
  'cell:edit:start': { row: any; column: any }
  'cell:edit:end': { row: any; column: any; value: any }
  'cell:edit:cancel': { row: any; column: any }
  'cell:value:change': { row: any; column: any; oldValue: any; newValue: any }
  
  // 行事件
  'row:click': { row: any; event: MouseEvent }
  'row:dblclick': { row: any; event: MouseEvent }
  'row:select': { rows: any[] }
  'row:add': { row: any; index: number }
  'row:delete': { rows: any[] }
  'row:update': { row: any }
  
  // 表格事件
  'scroll': { scrollTop: number; scrollLeft: number }
  'sort': { column: any; order: 'asc' | 'desc' | null }
  'filter': { filters: Record<string, any> }
  
  // 渲染事件
  'render:before': { timestamp: number }
  'render:after': { timestamp: number; duration: number }
  
  // 数据事件
  'data:load': { data: any[] }
  'data:change': { data: any[] }
  'data:error': { error: Error }
}

export { EventBus, type TableEvents, type EventCallback, type EventConfig }

// 创建全局表格事件总线实例
export const tableEventBus = new EventBus<TableEvents>()
```

### 使用示例

```typescript
import { EventBus, type TableEvents } from './core/eventBus'

// 创建事件总线实例
const eventBus = new EventBus<TableEvents>()

// 订阅事件
const unsubscribe = eventBus.on('cell:value:change', ({ row, column, oldValue, newValue }) => {
  console.log('Cell changed:', { row, column, oldValue, newValue })
})

// 带优先级的订阅（优先级高的先执行）
eventBus.on('cell:edit:start', handleEdit, { priority: 10 })

// 一次性订阅
eventBus.once('data:load', ({ data }) => {
  console.log('Data loaded:', data.length)
})

// 触发事件
await eventBus.emit('cell:value:change', {
  row: { id: 1, name: 'John' },
  column: { field: 'name' },
  oldValue: 'John',
  newValue: 'Jane'
})

// 同步触发
eventBus.emitSync('cell:click', {
  row: { id: 1 },
  column: { field: 'name' },
  event: new MouseEvent('click')
})

// 取消订阅
unsubscribe()

// 查看统计
console.log(eventBus.getStats())
```

### Composition API Hook

```typescript
// hooks/useEventBus.ts
import { onUnmounted } from 'vue'
import { EventBus, type TableEvents } from '../core/eventBus'

export function useEventBus(eventBus: EventBus<TableEvents>) {
  const unsubscribers: Array<() => void> = []
  
  function on<K extends keyof TableEvents>(
    event: K,
    callback: (data: TableEvents[K]) => void,
    config?: { once?: boolean; priority?: number }
  ) {
    const unsubscribe = eventBus.on(event, callback, config)
    unsubscribers.push(unsubscribe)
    return unsubscribe
  }
  
  function once<K extends keyof TableEvents>(
    event: K,
    callback: (data: TableEvents[K]) => void
  ) {
    return on(event, callback, { once: true })
  }
  
  function emit<K extends keyof TableEvents>(event: K, data: TableEvents[K]) {
    return eventBus.emit(event, data)
  }
  
  // 组件卸载时自动清理
  onUnmounted(() => {
    unsubscribers.forEach(fn => fn())
  })
  
  return {
    on,
    once,
    emit,
    off: eventBus.off.bind(eventBus)
  }
}
```

---

## 🎨 渲染引擎

### 设计目标

- 增量渲染，最小化重绘
- 帧级别的渲染调度
- 任务优先级管理
- 性能监控

### 核心实现

```typescript
// core/renderer.ts

interface RenderTask {
  id: string
  type: 'cell' | 'row' | 'column' | 'full'
  target: any
  priority: number
  timestamp: number
  execute: () => void
}

/**
 * 增量渲染器
 * 使用 requestAnimationFrame 进行帧级别的渲染调度
 */
class IncrementalRenderer {
  private renderQueue: RenderTask[] = []
  private rendering = false
  private frameId: number | null = null
  private renderBudget = 16 // 每帧渲染预算（ms）
  private stats = {
    tasksExecuted: 0,
    totalDuration: 0,
    droppedFrames: 0,
    averageTaskDuration: 0
  }
  
  /**
   * 调度渲染任务
   */
  schedule(task: Omit<RenderTask, 'timestamp'>): void {
    // 去重：如果已存在相同任务，更新优先级
    const existingIndex = this.renderQueue.findIndex(t => 
      t.id === task.id && t.type === task.type
    )
    
    if (existingIndex > -1) {
      this.renderQueue[existingIndex].priority = Math.max(
        this.renderQueue[existingIndex].priority,
        task.priority
      )
      return
    }
    
    this.renderQueue.push({
      ...task,
      timestamp: performance.now()
    })
    
    this.sortQueue()
    
    if (!this.rendering) {
      this.startRendering()
    }
  }
  
  /**
   * 批量调度
   */
  scheduleBatch(tasks: Omit<RenderTask, 'timestamp'>[]): void {
    tasks.forEach(task => {
      this.renderQueue.push({
        ...task,
        timestamp: performance.now()
      })
    })
    
    this.sortQueue()
    
    if (!this.rendering) {
      this.startRendering()
    }
  }
  
  /**
   * 开始渲染循环
   */
  private startRendering(): void {
    this.rendering = true
    this.renderLoop()
  }
  
  /**
   * 渲染循环
   */
  private renderLoop(): void {
    if (this.renderQueue.length === 0) {
      this.rendering = false
      return
    }
    
    this.frameId = requestAnimationFrame(() => {
      const frameStart = performance.now()
      let tasksInFrame = 0
      
      // 在预算内尽可能多地渲染任务
      while (this.renderQueue.length > 0) {
        const elapsed = performance.now() - frameStart
        
        if (elapsed > this.renderBudget) {
          // 超出预算，记录掉帧
          if (this.renderQueue.length > 0) {
            this.stats.droppedFrames++
          }
          break
        }
        
        const task = this.renderQueue.shift()!
        this.executeTask(task)
        tasksInFrame++
      }
      
      // 继续下一帧
      this.renderLoop()
    })
  }
  
  /**
   * 执行渲染任务
   */
  private executeTask(task: RenderTask): void {
    const start = performance.now()
    
    try {
      task.execute()
      this.stats.tasksExecuted++
    } catch (error) {
      console.error('[Renderer] Task execution failed:', task, error)
    }
    
    const duration = performance.now() - start
    this.stats.totalDuration += duration
    this.stats.averageTaskDuration = this.stats.totalDuration / this.stats.tasksExecuted
    
    if (duration > 5) {
      console.warn(`[Renderer] Slow task: ${task.type}#${task.id} took ${duration.toFixed(2)}ms`)
    }
  }
  
  /**
   * 按优先级排序队列
   */
  private sortQueue(): void {
    this.renderQueue.sort((a, b) => {
      // 优先级高的先渲染
      if (a.priority !== b.priority) {
        return b.priority - a.priority
      }
      // 相同优先级，先提交的先渲染
      return a.timestamp - b.timestamp
    })
  }
  
  /**
   * 取消渲染
   */
  cancel(): void {
    if (this.frameId !== null) {
      cancelAnimationFrame(this.frameId)
      this.frameId = null
    }
    this.rendering = false
    this.renderQueue = []
  }
  
  /**
   * 立即渲染所有任务（跳过预算限制）
   */
  flush(): void {
    while (this.renderQueue.length > 0) {
      const task = this.renderQueue.shift()!
      this.executeTask(task)
    }
    this.rendering = false
  }
  
  /**
   * 获取队列状态
   */
  getQueueStatus() {
    return {
      queueLength: this.renderQueue.length,
      rendering: this.rendering,
      tasks: this.renderQueue.map(t => ({
        id: t.id,
        type: t.type,
        priority: t.priority
      }))
    }
  }
  
  /**
   * 获取性能统计
   */
  getStats() {
    return {
      ...this.stats,
      queueLength: this.renderQueue.length
    }
  }
  
  /**
   * 重置统计
   */
  resetStats(): void {
    this.stats = {
      tasksExecuted: 0,
      totalDuration: 0,
      droppedFrames: 0,
      averageTaskDuration: 0
    }
  }
  
  /**
   * 设置渲染预算
   */
  setRenderBudget(ms: number): void {
    this.renderBudget = ms
  }
}

export const renderer = new IncrementalRenderer()
export { IncrementalRenderer, type RenderTask }
```

### Diff 算法

```typescript
// core/differ.ts

interface DiffResult {
  type: 'add' | 'update' | 'delete' | 'move'
  path: string[]
  oldValue?: any
  newValue?: any
  oldIndex?: number
  newIndex?: number
}

/**
 * 数据 Diff 工具
 * 高效计算两个数据集的差异
 */
class DataDiffer {
  /**
   * 计算数组级别的 diff
   */
  diff(oldData: any[], newData: any[], keyField = 'rowKey'): DiffResult[] {
    const results: DiffResult[] = []
    
    const oldMap = new Map(oldData.map((item, index) => [item[keyField], { item, index }]))
    const newMap = new Map(newData.map((item, index) => [item[keyField], { item, index }]))
    
    // 检测删除
    oldMap.forEach((value, key) => {
      if (!newMap.has(key)) {
        results.push({
          type: 'delete',
          path: [String(key)],
          oldValue: value.item,
          oldIndex: value.index
        })
      }
    })
    
    // 检测新增和更新
    newMap.forEach((value, key) => {
      const oldValue = oldMap.get(key)
      
      if (!oldValue) {
        // 新增
        results.push({
          type: 'add',
          path: [String(key)],
          newValue: value.item,
          newIndex: value.index
        })
      } else {
        // 检测字段变更
        const fieldChanges = this.diffObject(oldValue.item, value.item)
        
        fieldChanges.forEach(change => {
          results.push({
            type: 'update',
            path: [String(key), ...change.path],
            oldValue: change.oldValue,
            newValue: change.newValue
          })
        })
        
        // 检测位置变更
        if (oldValue.index !== value.index) {
          results.push({
            type: 'move',
            path: [String(key)],
            oldIndex: oldValue.index,
            newIndex: value.index
          })
        }
      }
    })
    
    return results
  }
  
  /**
   * 对象级别的 diff
   */
  diffObject(oldObj: any, newObj: any): DiffResult[] {
    const results: DiffResult[] = []
    const allKeys = new Set([...Object.keys(oldObj), ...Object.keys(newObj)])
    
    allKeys.forEach(key => {
      const oldValue = oldObj[key]
      const newValue = newObj[key]
      
      if (!this.isEqual(oldValue, newValue)) {
        results.push({
          type: 'update',
          path: [key],
          oldValue,
          newValue
        })
      }
    })
    
    return results
  }
  
  /**
   * 深度相等比较
   */
  private isEqual(a: any, b: any): boolean {
    if (a === b) return true
    if (a == null || b == null) return false
    if (typeof a !== typeof b) return false
    
    if (typeof a === 'object') {
      const keysA = Object.keys(a)
      const keysB = Object.keys(b)
      
      if (keysA.length !== keysB.length) return false
      
      return keysA.every(key => this.isEqual(a[key], b[key]))
    }
    
    return false
  }
  
  /**
   * 应用 diff 结果（Patch）
   */
  patch(data: any[], diffs: DiffResult[], keyField = 'rowKey'): any[] {
    const result = [...data]
    const dataMap = new Map(result.map(item => [item[keyField], item]))
    
    diffs.forEach(diff => {
      const [key, ...fieldPath] = diff.path
      
      switch (diff.type) {
        case 'add':
          result.splice(diff.newIndex!, 0, diff.newValue)
          break
          
        case 'delete':
          const deleteIndex = result.findIndex(item => item[keyField] === key)
          if (deleteIndex > -1) {
            result.splice(deleteIndex, 1)
          }
          break
          
        case 'update':
          const item = dataMap.get(key)
          if (item && fieldPath.length > 0) {
            this.setValueByPath(item, fieldPath, diff.newValue)
          }
          break
          
        case 'move':
          const moveItem = dataMap.get(key)
          if (moveItem) {
            const oldIndex = result.indexOf(moveItem)
            result.splice(oldIndex, 1)
            result.splice(diff.newIndex!, 0, moveItem)
          }
          break
      }
    })
    
    return result
  }
  
  /**
   * 根据路径设置值
   */
  private setValueByPath(obj: any, path: string[], value: any): void {
    let current = obj
    
    for (let i = 0; i < path.length - 1; i++) {
      if (!current[path[i]]) {
        current[path[i]] = {}
      }
      current = current[path[i]]
    }
    
    current[path[path.length - 1]] = value
  }
}

export const differ = new DataDiffer()
export { DataDiffer, type DiffResult }
```

---

## 💾 内存管理

### 设计目标

- 防止内存泄漏
- 自动资源清理
- 缓存大小控制
- LRU 缓存策略

### 核心实现

```typescript
// core/memoryManager.ts

interface ResourceRef {
  type: 'timer' | 'listener' | 'observer' | 'cache' | 'custom'
  id: string
  cleanup: () => void
  createdAt: number
  size?: number
  metadata?: Record<string, any>
}

/**
 * 内存管理器
 * 统一管理各类资源的生命周期
 */
class MemoryManager {
  private resources = new Map<string, ResourceRef>()
  private cacheSize = 0
  private maxCacheSize = 50 * 1024 * 1024 // 50MB
  private stats = {
    totalRegistered: 0,
    totalCleaned: 0,
    cacheEvictions: 0
  }
  
  /**
   * 注册定时器
   */
  registerTimer(id: string, timerId: number): () => void {
    const resource: ResourceRef = {
      type: 'timer',
      id,
      cleanup: () => clearTimeout(timerId),
      createdAt: Date.now()
    }
    
    this.register(resource)
    return () => this.unregister(id)
  }
  
  /**
   * 注册间隔定时器
   */
  registerInterval(id: string, intervalId: number): () => void {
    const resource: ResourceRef = {
      type: 'timer',
      id,
      cleanup: () => clearInterval(intervalId),
      createdAt: Date.now()
    }
    
    this.register(resource)
    return () => this.unregister(id)
  }
  
  /**
   * 注册事件监听器
   */
  registerListener(
    id: string,
    element: EventTarget,
    event: string,
    handler: EventListener,
    options?: AddEventListenerOptions
  ): () => void {
    const resource: ResourceRef = {
      type: 'listener',
      id,
      cleanup: () => element.removeEventListener(event, handler, options),
      createdAt: Date.now(),
      metadata: { event, element: element.constructor.name }
    }
    
    this.register(resource)
    return () => this.unregister(id)
  }
  
  /**
   * 注册观察器
   */
  registerObserver(
    id: string,
    observer: IntersectionObserver | MutationObserver | ResizeObserver
  ): () => void {
    const resource: ResourceRef = {
      type: 'observer',
      id,
      cleanup: () => observer.disconnect(),
      createdAt: Date.now(),
      metadata: { observerType: observer.constructor.name }
    }
    
    this.register(resource)
    return () => this.unregister(id)
  }
  
  /**
   * 注册缓存
   */
  registerCache(id: string, data: any, cleanup: () => void): () => void {
    const size = this.estimateSize(data)
    
    const resource: ResourceRef = {
      type: 'cache',
      id,
      cleanup,
      createdAt: Date.now(),
      size
    }
    
    this.register(resource)
    this.cacheSize += size
    
    // 检查缓存大小
    if (this.cacheSize > this.maxCacheSize) {
      this.evictCache()
    }
    
    return () => this.unregister(id)
  }
  
  /**
   * 注册自定义资源
   */
  registerCustom(id: string, cleanup: () => void, metadata?: Record<string, any>): () => void {
    const resource: ResourceRef = {
      type: 'custom',
      id,
      cleanup,
      createdAt: Date.now(),
      metadata
    }
    
    this.register(resource)
    return () => this.unregister(id)
  }
  
  /**
   * 通用注册方法
   */
  private register(resource: ResourceRef): void {
    if (this.resources.has(resource.id)) {
      console.warn(`[MemoryManager] Resource "${resource.id}" already registered, replacing...`)
      this.unregister(resource.id)
    }
    
    this.resources.set(resource.id, resource)
    this.stats.totalRegistered++
  }
  
  /**
   * 注销资源
   */
  unregister(id: string): void {
    const resource = this.resources.get(id)
    
    if (resource) {
      try {
        resource.cleanup()
      } catch (error) {
        console.error(`[MemoryManager] Cleanup failed for "${id}":`, error)
      }
      
      if (resource.type === 'cache' && resource.size) {
        this.cacheSize -= resource.size
      }
      
      this.resources.delete(id)
      this.stats.totalCleaned++
    }
  }
  
  /**
   * 清理所有资源
   */
  cleanup(): void {
    this.resources.forEach((resource, id) => {
      try {
        resource.cleanup()
      } catch (error) {
        console.error(`[MemoryManager] Cleanup failed for "${id}":`, error)
      }
    })
    
    this.resources.clear()
    this.cacheSize = 0
  }
  
  /**
   * 清理特定类型的资源
   */
  cleanupByType(type: ResourceRef['type']): void {
    Array.from(this.resources.entries()).forEach(([id, resource]) => {
      if (resource.type === type) {
        this.unregister(id)
      }
    })
  }
  
  /**
   * 驱逐缓存（LRU策略）
   */
  private evictCache(): void {
    const caches = Array.from(this.resources.entries())
      .filter(([, resource]) => resource.type === 'cache')
      .sort((a, b) => a[1].createdAt - b[1].createdAt)
    
    // 移除最旧的缓存，直到低于阈值
    const threshold = this.maxCacheSize * 0.75
    
    for (const [id] of caches) {
      if (this.cacheSize <= threshold) {
        break
      }
      this.unregister(id)
      this.stats.cacheEvictions++
    }
    
    console.log(`[MemoryManager] Cache evicted, current size: ${(this.cacheSize / 1024 / 1024).toFixed(2)}MB`)
  }
  
  /**
   * 估算数据大小
   */
  private estimateSize(data: any): number {
    try {
      const json = JSON.stringify(data)
      return new Blob([json]).size
    } catch {
      return 0
    }
  }
  
  /**
   * 获取内存使用统计
   */
  getStats() {
    return {
      ...this.stats,
      currentResources: this.resources.size,
      byType: this.getResourcesByType(),
      cacheSize: this.cacheSize,
      cacheSizeMB: (this.cacheSize / 1024 / 1024).toFixed(2),
      maxCacheSizeMB: (this.maxCacheSize / 1024 / 1024).toFixed(2),
      cacheUsage: ((this.cacheSize / this.maxCacheSize) * 100).toFixed(2) + '%'
    }
  }
  
  /**
   * 按类型统计资源
   */
  private getResourcesByType(): Record<string, number> {
    const result: Record<string, number> = {}
    
    this.resources.forEach(resource => {
      result[resource.type] = (result[resource.type] || 0) + 1
    })
    
    return result
  }
  
  /**
   * 设置最大缓存大小
   */
  setMaxCacheSize(sizeInBytes: number): void {
    this.maxCacheSize = sizeInBytes
    
    if (this.cacheSize > this.maxCacheSize) {
      this.evictCache()
    }
  }
  
  /**
   * 获取资源详情
   */
  getResourceDetails(id: string): ResourceRef | undefined {
    return this.resources.get(id)
  }
  
  /**
   * 获取所有资源列表
   */
  getAllResources(): ResourceRef[] {
    return Array.from(this.resources.values())
  }
}

export const memoryManager = new MemoryManager()
export { MemoryManager, type ResourceRef }
```

### Composition API Hook

```typescript
// hooks/useMemory.ts
import { onUnmounted } from 'vue'
import { memoryManager } from '../core/memoryManager'

export function useMemory() {
  const registeredIds: string[] = []
  
  function registerTimer(id: string, timerId: number) {
    registeredIds.push(id)
    return memoryManager.registerTimer(id, timerId)
  }
  
  function registerListener(
    id: string,
    element: EventTarget,
    event: string,
    handler: EventListener
  ) {
    registeredIds.push(id)
    return memoryManager.registerListener(id, element, event, handler)
  }
  
  function registerObserver(
    id: string,
    observer: IntersectionObserver | MutationObserver | ResizeObserver
  ) {
    registeredIds.push(id)
    return memoryManager.registerObserver(id, observer)
  }
  
  // 组件卸载时自动清理
  onUnmounted(() => {
    registeredIds.forEach(id => {
      memoryManager.unregister(id)
    })
  })
  
  return {
    registerTimer,
    registerListener,
    registerObserver,
    registerCache: memoryManager.registerCache.bind(memoryManager),
    unregister: memoryManager.unregister.bind(memoryManager),
    getStats: memoryManager.getStats.bind(memoryManager)
  }
}
```

---

继续在下一条消息...
