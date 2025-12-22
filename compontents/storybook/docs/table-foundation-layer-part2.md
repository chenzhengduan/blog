# 表格组件底层基础能力设计（第二部分）

## 📊 数据结构优化

### 设计目标

- 高性能数据索引
- 快速查询和更新
- 支持多字段索引
- 内存效率优化

### 核心实现

```typescript
// core/dataIndex.ts

/**
 * 高性能数据索引
 * 支持主键索引和多个次级索引
 */
class DataIndex<T = any> {
  private primaryIndex = new Map<string, T>()
  private secondaryIndexes = new Map<string, Map<any, Set<string>>>()
  private keyField: string
  private stats = {
    insertCount: 0,
    updateCount: 0,
    deleteCount: 0,
    queryCount: 0
  }
  
  constructor(keyField = 'rowKey') {
    this.keyField = keyField
  }
  
  /**
   * 创建次级索引
   */
  createIndex(field: string): void {
    if (!this.secondaryIndexes.has(field)) {
      this.secondaryIndexes.set(field, new Map())
      console.log(`[DataIndex] Created index for field: ${field}`)
    }
  }
  
  /**
   * 删除索引
   */
  dropIndex(field: string): void {
    if (this.secondaryIndexes.has(field)) {
      this.secondaryIndexes.delete(field)
      console.log(`[DataIndex] Dropped index for field: ${field}`)
    }
  }
  
  /**
   * 插入数据
   */
  insert(item: T): void {
    const key = (item as any)[this.keyField]
    
    if (!key) {
      throw new Error(`Key field "${this.keyField}" is missing in item`)
    }
    
    // 更新主索引
    this.primaryIndex.set(key, item)
    
    // 更新次级索引
    this.updateSecondaryIndexes(key, item, 'insert')
    
    this.stats.insertCount++
  }
  
  /**
   * 批量插入
   */
  insertBatch(items: T[]): void {
    items.forEach(item => this.insert(item))
  }
  
  /**
   * 根据主键查询
   */
  get(key: string): T | undefined {
    this.stats.queryCount++
    return this.primaryIndex.get(key)
  }
  
  /**
   * 根据字段查询
   */
  query(field: string, value: any): T[] {
    this.stats.queryCount++
    
    const index = this.secondaryIndexes.get(field)
    
    if (!index) {
      throw new Error(`Index for field "${field}" not found. Call createIndex("${field}") first.`)
    }
    
    const keys = index.get(value)
    
    if (!keys) {
      return []
    }
    
    return Array.from(keys)
      .map(key => this.primaryIndex.get(key))
      .filter((item): item is T => item !== undefined)
  }
  
  /**
   * 复杂查询（支持多个条件）
   */
  queryMultiple(conditions: Record<string, any>): T[] {
    const fields = Object.keys(conditions)
    
    if (fields.length === 0) {
      return this.getAll()
    }
    
    // 使用第一个条件获取初始结果集
    let results = this.query(fields[0], conditions[fields[0]])
    
    // 对剩余条件进行过滤
    for (let i = 1; i < fields.length; i++) {
      const field = fields[i]
      const value = conditions[field]
      
      results = results.filter(item => (item as any)[field] === value)
    }
    
    return results
  }
  
  /**
   * 范围查询
   */
  queryRange(field: string, min: any, max: any): T[] {
    this.stats.queryCount++
    
    const index = this.secondaryIndexes.get(field)
    
    if (!index) {
      throw new Error(`Index for field "${field}" not found`)
    }
    
    const results: T[] = []
    
    index.forEach((keys, value) => {
      if (value >= min && value <= max) {
        keys.forEach(key => {
          const item = this.primaryIndex.get(key)
          if (item) {
            results.push(item)
          }
        })
      }
    })
    
    return results
  }
  
  /**
   * 更新数据
   */
  update(key: string, updates: Partial<T>): void {
    const item = this.primaryIndex.get(key)
    
    if (!item) {
      throw new Error(`Item with key "${key}" not found`)
    }
    
    // 保存旧值（用于更新索引）
    const oldItem = { ...item }
    
    // 更新主索引
    Object.assign(item, updates)
    
    // 更新次级索引
    this.updateSecondaryIndexes(key, item, 'update', oldItem)
    
    this.stats.updateCount++
  }
  
  /**
   * 批量更新
   */
  updateBatch(updates: Array<{ key: string; data: Partial<T> }>): void {
    updates.forEach(({ key, data }) => this.update(key, data))
  }
  
  /**
   * 删除数据
   */
  delete(key: string): void {
    const item = this.primaryIndex.get(key)
    
    if (!item) {
      return
    }
    
    // 从主索引删除
    this.primaryIndex.delete(key)
    
    // 从次级索引删除
    this.updateSecondaryIndexes(key, item, 'delete')
    
    this.stats.deleteCount++
  }
  
  /**
   * 批量删除
   */
  deleteBatch(keys: string[]): void {
    keys.forEach(key => this.delete(key))
  }
  
  /**
   * 清空索引
   */
  clear(): void {
    this.primaryIndex.clear()
    this.secondaryIndexes.forEach(index => index.clear())
  }
  
  /**
   * 获取所有数据
   */
  getAll(): T[] {
    return Array.from(this.primaryIndex.values())
  }
  
  /**
   * 获取数据数量
   */
  size(): number {
    return this.primaryIndex.size
  }
  
  /**
   * 检查是否存在
   */
  has(key: string): boolean {
    return this.primaryIndex.has(key)
  }
  
  /**
   * 更新次级索引
   */
  private updateSecondaryIndexes(
    key: string,
    item: T,
    operation: 'insert' | 'update' | 'delete',
    oldItem?: T
  ): void {
    this.secondaryIndexes.forEach((index, field) => {
      const value = (item as any)[field]
      const oldValue = oldItem ? (oldItem as any)[field] : undefined
      
      if (operation === 'delete' || (operation === 'update' && oldValue !== undefined)) {
        // 从旧值的集合中移除
        const oldKeys = index.get(oldValue)
        if (oldKeys) {
          oldKeys.delete(key)
          if (oldKeys.size === 0) {
            index.delete(oldValue)
          }
        }
      }
      
      if (operation === 'insert' || operation === 'update') {
        // 添加到新值的集合
        if (!index.has(value)) {
          index.set(value, new Set())
        }
        index.get(value)!.add(key)
      }
    })
  }
  
  /**
   * 获取索引统计
   */
  getStats() {
    return {
      ...this.stats,
      totalRecords: this.primaryIndex.size,
      indexes: Array.from(this.secondaryIndexes.entries()).map(([field, index]) => ({
        field,
        uniqueValues: index.size,
        totalKeys: Array.from(index.values()).reduce((sum, keys) => sum + keys.size, 0)
      }))
    }
  }
  
  /**
   * 重建索引
   */
  rebuild(): void {
    console.log('[DataIndex] Rebuilding indexes...')
    
    // 清空次级索引
    this.secondaryIndexes.forEach(index => index.clear())
    
    // 重新构建
    this.primaryIndex.forEach((item, key) => {
      this.updateSecondaryIndexes(key, item, 'insert')
    })
    
    console.log('[DataIndex] Indexes rebuilt')
  }
}

export { DataIndex }
```

### 使用示例

```typescript
import { DataIndex } from './core/dataIndex'

interface User {
  rowKey: string
  name: string
  age: number
  department: string
  isActive: boolean
}

// 创建索引
const userIndex = new DataIndex<User>('rowKey')

// 创建次级索引
userIndex.createIndex('department')
userIndex.createIndex('age')
userIndex.createIndex('isActive')

// 插入数据
userIndex.insertBatch([
  { rowKey: '1', name: 'Alice', age: 25, department: 'IT', isActive: true },
  { rowKey: '2', name: 'Bob', age: 30, department: 'HR', isActive: true },
  { rowKey: '3', name: 'Charlie', age: 25, department: 'IT', isActive: false }
])

// 主键查询
const user = userIndex.get('1')

// 字段查询
const itUsers = userIndex.query('department', 'IT')

// 多条件查询
const activeItUsers = userIndex.queryMultiple({
  department: 'IT',
  isActive: true
})

// 范围查询
const youngUsers = userIndex.queryRange('age', 20, 30)

// 更新
userIndex.update('1', { age: 26 })

// 删除
userIndex.delete('2')

// 统计
console.log(userIndex.getStats())
```

---

## 🛠️ 核心工具库

### 通用工具函数

```typescript
// utils/index.ts

/**
 * 深度克隆
 * 支持Date、Array、Object等常见类型
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }
  
  if (obj instanceof Date) {
    return new Date(obj.getTime()) as any
  }
  
  if (obj instanceof Array) {
    return obj.map(item => deepClone(item)) as any
  }
  
  if (obj instanceof Object) {
    const clonedObj = {} as T
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  
  return obj
}

/**
 * 防抖
 * @param func 要防抖的函数
 * @param wait 等待时间（毫秒）
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  
  return function (this: any, ...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout)
    
    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

/**
 * 节流
 * @param func 要节流的函数
 * @param limit 时间限制（毫秒）
 * @returns 节流后的函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean
  
  return function (this: any, ...args: Parameters<T>) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      
      setTimeout(() => {
        inThrottle = false
      }, limit)
    }
  }
}

/**
 * 生成唯一 ID
 */
export function generateId(prefix = 'id'): string {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 根据路径获取对象属性值
 */
export function getValueByPath(obj: any, path: string | string[]): any {
  const pathArray = typeof path === 'string' ? path.split('.') : path
  
  return pathArray.reduce((current, key) => {
    return current?.[key]
  }, obj)
}

/**
 * 根据路径设置对象属性值
 */
export function setValueByPath(obj: any, path: string | string[], value: any): void {
  const pathArray = typeof path === 'string' ? path.split('.') : path
  const lastKey = pathArray.pop()!
  
  const target = pathArray.reduce((current, key) => {
    if (!current[key]) {
      current[key] = {}
    }
    return current[key]
  }, obj)
  
  target[lastKey] = value
}

/**
 * 对象浅比较
 */
export function shallowEqual(obj1: any, obj2: any): boolean {
  if (obj1 === obj2) return true
  
  if (typeof obj1 !== 'object' || typeof obj2 !== 'object' || obj1 == null || obj2 == null) {
    return false
  }
  
  const keys1 = Object.keys(obj1)
  const keys2 = Object.keys(obj2)
  
  if (keys1.length !== keys2.length) return false
  
  return keys1.every(key => obj1[key] === obj2[key])
}

/**
 * 深度比较
 */
export function deepEqual(obj1: any, obj2: any): boolean {
  if (obj1 === obj2) return true
  
  if (typeof obj1 !== 'object' || typeof obj2 !== 'object' || obj1 == null || obj2 == null) {
    return false
  }
  
  const keys1 = Object.keys(obj1)
  const keys2 = Object.keys(obj2)
  
  if (keys1.length !== keys2.length) return false
  
  return keys1.every(key => deepEqual(obj1[key], obj2[key]))
}

/**
 * 数组分块
 */
export function chunk<T>(array: T[], size: number): T[][] {
  const result: T[][] = []
  
  for (let i = 0; i < array.length; i += size) {
    result.push(array.slice(i, i + size))
  }
  
  return result
}

/**
 * 数组去重
 */
export function unique<T>(array: T[], key?: keyof T): T[] {
  if (!key) {
    return Array.from(new Set(array))
  }
  
  const seen = new Set()
  return array.filter(item => {
    const value = item[key]
    if (seen.has(value)) {
      return false
    }
    seen.add(value)
    return true
  })
}

/**
 * 数组分组
 */
export function groupBy<T>(array: T[], key: keyof T): Record<string, T[]> {
  return array.reduce((result, item) => {
    const groupKey = String(item[key])
    if (!result[groupKey]) {
      result[groupKey] = []
    }
    result[groupKey].push(item)
    return result
  }, {} as Record<string, T[]>)
}

/**
 * 延迟执行
 */
export function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 重试机制
 */
export async function retry<T>(
  fn: () => Promise<T>,
  options: {
    maxRetries?: number
    delay?: number
    backoff?: 'linear' | 'exponential'
    onRetry?: (error: Error, attempt: number) => void
  } = {}
): Promise<T> {
  const {
    maxRetries = 3,
    delay = 1000,
    backoff = 'exponential',
    onRetry
  } = options
  
  let lastError: Error
  
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error as Error
      
      if (attempt < maxRetries) {
        onRetry?.(lastError, attempt)
        
        const waitTime = backoff === 'exponential'
          ? delay * Math.pow(2, attempt - 1)
          : delay * attempt
        
        await sleep(waitTime)
      }
    }
  }
  
  throw lastError!
}

/**
 * 批处理
 */
export function batchProcess<T, R>(
  items: T[],
  processor: (item: T) => R,
  batchSize = 100
): R[] {
  const results: R[] = []
  
  for (let i = 0; i < items.length; i += batchSize) {
    const batch = items.slice(i, i + batchSize)
    results.push(...batch.map(processor))
  }
  
  return results
}

/**
 * 异步批处理
 */
export async function batchProcessAsync<T, R>(
  items: T[],
  processor: (item: T) => Promise<R>,
  batchSize = 100,
  concurrent = 5
): Promise<R[]> {
  const results: R[] = []
  const batches = chunk(items, batchSize)
  
  for (const batch of batches) {
    const batchResults = await Promise.all(
      batch.map(item => processor(item))
    )
    results.push(...batchResults)
  }
  
  return results
}

/**
 * 函数缓存（Memoization）
 */
export function memoize<T extends (...args: any[]) => any>(
  func: T,
  resolver?: (...args: Parameters<T>) => string
): T {
  const cache = new Map<string, ReturnType<T>>()
  
  return ((...args: Parameters<T>) => {
    const key = resolver ? resolver(...args) : JSON.stringify(args)
    
    if (cache.has(key)) {
      return cache.get(key)!
    }
    
    const result = func(...args)
    cache.set(key, result)
    
    return result
  }) as T
}

/**
 * 限制并发数
 */
export async function limit<T>(
  tasks: Array<() => Promise<T>>,
  concurrency: number
): Promise<T[]> {
  const results: T[] = []
  const executing: Promise<void>[] = []
  
  for (const task of tasks) {
    const promise = task().then(result => {
      results.push(result)
      executing.splice(executing.indexOf(promise), 1)
    })
    
    executing.push(promise)
    
    if (executing.length >= concurrency) {
      await Promise.race(executing)
    }
  }
  
  await Promise.all(executing)
  return results
}

/**
 * 格式化文件大小
 */
export function formatBytes(bytes: number, decimals = 2): string {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i]
}

/**
 * 格式化数字
 */
export function formatNumber(num: number, decimals = 0): string {
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })
}

/**
 * 安全的 JSON.parse
 */
export function safeJsonParse<T = any>(json: string, defaultValue?: T): T | undefined {
  try {
    return JSON.parse(json)
  } catch {
    return defaultValue
  }
}

/**
 * 判断是否为空
 */
export function isEmpty(value: any): boolean {
  if (value == null) return true
  if (typeof value === 'string') return value.trim() === ''
  if (Array.isArray(value)) return value.length === 0
  if (typeof value === 'object') return Object.keys(value).length === 0
  return false
}

/**
 * 随机字符串
 */
export function randomString(length = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 安全地获取嵌套属性
 */
export function safeGet<T = any>(obj: any, path: string, defaultValue?: T): T | undefined {
  try {
    const value = getValueByPath(obj, path)
    return value !== undefined ? value : defaultValue
  } catch {
    return defaultValue
  }
}
```

---

## 📈 性能监控

### 设计目标

- 实时性能监控
- 性能指标收集
- 性能瓶颈识别
- 性能报告生成

### 核心实现

```typescript
// core/performanceMonitor.ts

interface PerformanceMetric {
  name: string
  startTime: number
  endTime?: number
  duration?: number
  metadata?: Record<string, any>
}

interface PerformanceReport {
  metrics: Record<string, {
    count: number
    total: number
    average: number
    min: number
    max: number
    p50: number
    p95: number
    p99: number
  }>
  startTime: number
  endTime: number
  duration: number
}

/**
 * 性能监控器
 */
class PerformanceMonitor {
  private metrics: PerformanceMetric[] = []
  private maxMetrics = 1000
  private enabled = true
  
  /**
   * 开始测量
   */
  start(name: string, metadata?: Record<string, any>): () => void {
    if (!this.enabled) return () => {}
    
    const metric: PerformanceMetric = {
      name,
      startTime: performance.now(),
      metadata
    }
    
    return () => this.end(metric)
  }
  
  /**
   * 结束测量
   */
  private end(metric: PerformanceMetric): void {
    metric.endTime = performance.now()
    metric.duration = metric.endTime - metric.startTime
    
    this.metrics.push(metric)
    
    // 限制存储的指标数量
    if (this.metrics.length > this.maxMetrics) {
      this.metrics.shift()
    }
    
    // 警告慢操作
    if (metric.duration > 100) {
      console.warn(`[Performance] Slow operation: ${metric.name} took ${metric.duration.toFixed(2)}ms`)
    }
  }
  
  /**
   * 测量同步函数
   */
  measure<T>(name: string, fn: () => T, metadata?: Record<string, any>): T {
    if (!this.enabled) return fn()
    
    const end = this.start(name, metadata)
    try {
      return fn()
    } finally {
      end()
    }
  }
  
  /**
   * 测量异步函数
   */
  async measureAsync<T>(
    name: string,
    fn: () => Promise<T>,
    metadata?: Record<string, any>
  ): Promise<T> {
    if (!this.enabled) return fn()
    
    const end = this.start(name, metadata)
    try {
      return await fn()
    } finally {
      end()
    }
  }
  
  /**
   * 记录自定义指标
   */
  record(name: string, duration: number, metadata?: Record<string, any>): void {
    if (!this.enabled) return
    
    this.metrics.push({
      name,
      startTime: performance.now() - duration,
      endTime: performance.now(),
      duration,
      metadata
    })
  }
  
  /**
   * 获取指定名称的指标
   */
  getMetrics(name: string): PerformanceMetric[] {
    return this.metrics.filter(m => m.name === name)
  }
  
  /**
   * 获取所有指标
   */
  getAllMetrics(): PerformanceMetric[] {
    return [...this.metrics]
  }
  
  /**
   * 生成性能报告
   */
  generateReport(): PerformanceReport {
    const grouped = this.groupByName()
    const report: PerformanceReport = {
      metrics: {},
      startTime: this.metrics[0]?.startTime || 0,
      endTime: this.metrics[this.metrics.length - 1]?.endTime || 0,
      duration: 0
    }
    
    report.duration = report.endTime - report.startTime
    
    for (const [name, metrics] of Object.entries(grouped)) {
      const durations = metrics
        .map(m => m.duration!)
        .filter(d => d !== undefined)
        .sort((a, b) => a - b)
      
      if (durations.length === 0) continue
      
      report.metrics[name] = {
        count: durations.length,
        total: durations.reduce((a, b) => a + b, 0),
        average: durations.reduce((a, b) => a + b, 0) / durations.length,
        min: Math.min(...durations),
        max: Math.max(...durations),
        p50: this.percentile(durations, 0.5),
        p95: this.percentile(durations, 0.95),
        p99: this.percentile(durations, 0.99)
      }
    }
    
    return report
  }
  
  /**
   * 按名称分组
   */
  private groupByName(): Record<string, PerformanceMetric[]> {
    return this.metrics.reduce((acc, metric) => {
      if (!acc[metric.name]) {
        acc[metric.name] = []
      }
      acc[metric.name].push(metric)
      return acc
    }, {} as Record<string, PerformanceMetric[]>)
  }
  
  /**
   * 计算百分位数
   */
  private percentile(values: number[], p: number): number {
    const index = Math.ceil(values.length * p) - 1
    return values[Math.max(0, index)]
  }
  
  /**
   * 清除指标
   */
  clear(): void {
    this.metrics = []
  }
  
  /**
   * 启用/禁用监控
   */
  setEnabled(enabled: boolean): void {
    this.enabled = enabled
  }
  
  /**
   * 设置最大指标数量
   */
  setMaxMetrics(max: number): void {
    this.maxMetrics = max
    
    if (this.metrics.length > max) {
      this.metrics = this.metrics.slice(-max)
    }
  }
  
  /**
   * 导出为 JSON
   */
  export(): string {
    return JSON.stringify({
      report: this.generateReport(),
      metrics: this.metrics
    }, null, 2)
  }
  
  /**
   * 打印报告到控制台
   */
  printReport(): void {
    const report = this.generateReport()
    
    console.group('📊 Performance Report')
    console.log(`Duration: ${report.duration.toFixed(2)}ms`)
    console.log(`Metrics collected: ${this.metrics.length}`)
    console.log('\nDetailed metrics:')
    console.table(report.metrics)
    console.groupEnd()
  }
}

export const perfMonitor = new PerformanceMonitor()
export { PerformanceMonitor, type PerformanceMetric, type PerformanceReport }
```

### 使用示例

```typescript
import { perfMonitor } from './core/performanceMonitor'

// 测量同步函数
const result = perfMonitor.measure('processData', () => {
  // 数据处理逻辑
  return processData(data)
})

// 测量异步函数
const data = await perfMonitor.measureAsync('fetchData', async () => {
  return await fetch('/api/data').then(r => r.json())
})

// 手动开始/结束测量
const end = perfMonitor.start('customOperation', { userId: 123 })
// ... 执行操作
end()

// 记录自定义指标
perfMonitor.record('cacheHit', 5.2, { cacheKey: 'user-data' })

// 生成并查看报告
const report = perfMonitor.generateReport()
console.table(report.metrics)

// 打印报告
perfMonitor.printReport()

// 导出数据
const exportData = perfMonitor.export()
```

### Composition API Hook

```typescript
// hooks/usePerformance.ts
import { onUnmounted } from 'vue'
import { perfMonitor } from '../core/performanceMonitor'

export function usePerformance(componentName: string) {
  const componentStart = performance.now()
  
  function measure<T>(name: string, fn: () => T): T {
    return perfMonitor.measure(`${componentName}:${name}`, fn)
  }
  
  async function measureAsync<T>(name: string, fn: () => Promise<T>): Promise<T> {
    return perfMonitor.measureAsync(`${componentName}:${name}`, fn)
  }
  
  function start(name: string) {
    return perfMonitor.start(`${componentName}:${name}`)
  }
  
  // 记录组件生命周期
  onUnmounted(() => {
    const duration = performance.now() - componentStart
    perfMonitor.record(`${componentName}:lifetime`, duration)
  })
  
  return {
    measure,
    measureAsync,
    start,
    getReport: perfMonitor.generateReport.bind(perfMonitor),
    printReport: perfMonitor.printReport.bind(perfMonitor)
  }
}
```

---

## 📦 完整示例

### 在 BaseTable 中集成所有底层能力

```typescript
// baseTable.vue
import { defineComponent, ref, onMounted, onUnmounted } from 'vue'
import { EventBus, type TableEvents } from './core/eventBus'
import { renderer } from './core/renderer'
import { differ } from './core/differ'
import { memoryManager } from './core/memoryManager'
import { DataIndex } from './core/dataIndex'
import { perfMonitor } from './core/performanceMonitor'

export default defineComponent({
  name: 'BaseTable',
  setup(props, { expose }) {
    // 事件总线
    const eventBus = new EventBus<TableEvents>()
    
    // 数据索引
    const dataIndex = new DataIndex(props.rowKeyFieldName || 'rowKey')
    
    // 表格引用
    const tableRef = ref()
    
    // 初始化
    onMounted(() => {
      perfMonitor.measure('BaseTable:mount', () => {
        setupEventProxy()
        setupDataIndex()
        registerResources()
      })
    })
    
    // 清理
    onUnmounted(() => {
      memoryManager.cleanup()
      renderer.cancel()
      eventBus.clear()
      dataIndex.clear()
    })
    
    // 代理事件
    function setupEventProxy() {
      // 注册事件监听器并自动管理内存
      memoryManager.registerListener(
        'cell-click',
        tableRef.value.$el,
        'cell-click',
        (event: any) => {
          eventBus.emitSync('cell:click', event.detail)
        }
      )
    }
    
    // 设置数据索引
    function setupDataIndex() {
      if (props.columns) {
        // 为需要快速查询的字段创建索引
        props.columns.forEach(col => {
          if (col.indexed) {
            dataIndex.createIndex(col.field)
          }
        })
      }
    }
    
    // 注册资源
    function registerResources() {
      // 注册定时器等资源
      const timerId = setInterval(() => {
        // 定期清理缓存
      }, 60000)
      
      memoryManager.registerInterval('cleanup-timer', timerId)
    }
    
    // 暴露方法
    expose({
      // 事件相关
      on: eventBus.on.bind(eventBus),
      once: eventBus.once.bind(eventBus),
      off: eventBus.off.bind(eventBus),
      emit: eventBus.emit.bind(eventBus),
      
      // 数据相关
      query: dataIndex.query.bind(dataIndex),
      get: dataIndex.get.bind(dataIndex),
      
      // 性能相关
      getPerformanceReport: perfMonitor.generateReport.bind(perfMonitor),
      printPerformanceReport: perfMonitor.printReport.bind(perfMonitor),
      
      // 内存相关
      getMemoryStats: memoryManager.getStats.bind(memoryManager)
    })
    
    return { tableRef }
  }
})
```

---

## 📊 总结

### 底层能力清单

✅ **事件系统**：类型安全、支持优先级、一次性监听  
✅ **渲染引擎**：增量渲染、帧级调度、任务优先级  
✅ **Diff 算法**：高效数据对比、增量更新  
✅ **内存管理**：自动资源清理、LRU 缓存、防止泄漏  
✅ **数据索引**：多字段索引、快速查询、范围查询  
✅ **工具函数**：防抖节流、深度克隆、批处理等  
✅ **性能监控**：实时监控、性能报告、瓶颈识别  

### 特点

- **零依赖**：所有实现都是原生 TypeScript
- **高性能**：针对表格场景深度优化
- **类型安全**：完整的 TypeScript 类型定义
- **可测试**：每个模块独立可测试
- **可扩展**：预留扩展点，易于定制

---

<div align="center">

**Built with 🔧 by Frontend Architecture Team**

</div>
