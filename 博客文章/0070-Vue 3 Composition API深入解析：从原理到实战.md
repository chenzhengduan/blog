# Vue 3 Composition API深入解析：从原理到实战

> Composition API是Vue 3最重要的特性之一，它提供了一种更灵活、更强大的组件逻辑组织方式。本文将深入探讨其核心原理、使用方法和最佳实践。

## 引言

Vue 3的Composition API是对Options API的重大改进，它解决了大型组件中逻辑复用和代码组织的问题。通过使用函数式的方式组织代码，Composition API提供了更好的类型推断、更灵活的逻辑组合和更清晰的代码结构。

## 一、Composition API基础

### 1.1 setup()函数

```javascript
import { ref, reactive } from 'vue'

export default {
  setup() {
    // 响应式数据
    const count = ref(0)
    const user = reactive({
      name: 'John',
      age: 30
    })

    // 方法
    const increment = () => {
      count.value++
    }

    const updateUser = (newName) => {
      user.name = newName
    }

    // 生命周期钩子
    onMounted(() => {
      console.log('Component mounted')
    })

    // 返回模板中需要使用的内容
    return {
      count,
      user,
      increment,
      updateUser
    }
  }
}
```

### 1.2 响应式系统

```javascript
// ref() - 基本类型响应式
const message = ref('Hello Vue 3')
console.log(message.value) // 访问值
message.value = 'New message' // 修改值

// reactive() - 对象响应式
const state = reactive({
  count: 0,
  user: {
    name: 'John',
    profile: {
      email: 'john@example.com'
    }
  }
})

// toRefs() - 将reactive对象转换为ref
const { count, user } = toRefs(state)

// computed() - 计算属性
const doubleCount = computed(() => state.count * 2)

// watch() - 监听器
watch(
  () => state.count,
  (newCount, oldCount) => {
    console.log(`Count changed from ${oldCount} to ${newCount}`)
  }
)

// watchEffect() - 自动追踪依赖
watchEffect(() => {
  console.log(`Current count: ${state.count}`)
})
```

### 1.3 生命周期钩子

```javascript
import {
  onMounted,
  onUpdated,
  onUnmounted,
  onBeforeMount,
  onBeforeUpdate,
  onBeforeUnmount,
  onErrorCaptured,
  onRenderTracked,
  onRenderTriggered
} from 'vue'

export default {
  setup() {
    onMounted(() => {
      console.log('Component is mounted')
    })

    onUpdated(() => {
      console.log('Component is updated')
    })

    onUnmounted(() => {
      console.log('Component is unmounted')
    })

    onBeforeMount(() => {
      console.log('Component will be mounted')
    })

    onBeforeUpdate(() => {
      console.log('Component will be updated')
    })

    onBeforeUnmount(() => {
      console.log('Component will be unmounted')
    })

    onErrorCaptured((error, instance, info) => {
      console.error('Error captured:', error)
      return false // 阻止错误继续向上传播
    })

    onRenderTracked((e) => {
      console.log('Render tracked:', e)
    })

    onRenderTriggered((e) => {
      console.log('Render triggered:', e)
    })
  }
}
```

## 二、高级Composition API特性

### 2.1 自定义组合函数（Composables）

```javascript
// useCounter.js
import { ref, computed, watch } from 'vue'

export function useCounter(initialValue = 0) {
  const count = ref(initialValue)
  const doubleCount = computed(() => count.value * 2)

  const increment = () => {
    count.value++
  }

  const decrement = () => {
    count.value--
  }

  const reset = () => {
    count.value = initialValue
  }

  const setValue = (newValue) => {
    count.value = newValue
  }

  watch(count, (newValue, oldValue) => {
    console.log(`Count changed from ${oldValue} to ${newValue}`)
  })

  return {
    count,
    doubleCount,
    increment,
    decrement,
    reset,
    setValue
  }
}

// useAsyncData.js
import { ref, watchEffect } from 'vue'

export function useAsyncData(asyncFunction, options = {}) {
  const {
    immediate = true,
    initialData = null,
    onError = null
  } = options

  const data = ref(initialData)
  const loading = ref(false)
  const error = ref(null)

  const execute = async () => {
    loading.value = true
    error.value = null

    try {
      const result = await asyncFunction()
      data.value = result
    } catch (err) {
      error.value = err
      if (onError) {
        onError(err)
      }
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    execute()
  }

  return {
    data,
    loading,
    error,
    execute
  }
}

// 使用示例
import { useCounter, useAsyncData } from './composables'

export default {
  setup() {
    const { count, increment } = useCounter(10)

    const { data: userData, loading, error } = useAsyncData(
      () => fetch('/api/user').then(res => res.json()),
      {
        onError: (error) => {
          console.error('Failed to fetch user:', error)
        }
      }
    )

    return {
      count,
      increment,
      userData,
      loading,
      error
    }
  }
}
```

### 2.2 依赖注入

```javascript
// 父组件
import { provide, ref } from 'vue'

export default {
  setup() {
    const theme = ref('light')
    const user = ref({ name: 'John' })

    // 提供响应式数据
    provide('theme', theme)
    provide('user', user)

    // 提供方法
    const updateTheme = (newTheme) => {
      theme.value = newTheme
    }

    provide('updateTheme', updateTheme)

    return {
      theme,
      updateTheme
    }
  }
}

// 子组件
import { inject } from 'vue'

export default {
  setup() {
    // 注入数据
    const theme = inject('theme')
    const user = inject('user')

    // 注入方法
    const updateTheme = inject('updateTheme')

    // 提供默认值
    const defaultValue = inject('optional-key', 'default value')

    return {
      theme,
      user,
      updateTheme
    }
  }
}
```

### 2.3 模板引用

```javascript
import { ref, onMounted } from 'vue'

export default {
  setup() {
    const inputRef = ref(null)
    const divRef = ref(null)

    onMounted(() => {
      // 访问DOM元素
      console.log(inputRef.value) // <input>元素
      console.log(divRef.value)   // <div>元素

      // 聚焦输入框
      inputRef.value.focus()
    })

    const focusInput = () => {
      inputRef.value.focus()
    }

    return {
      inputRef,
      divRef,
      focusInput
    }
  }
}
```

## 三、响应式原理深入

### 3.1 响应式系统的工作原理

```javascript
// 简化的响应式系统实现
class Dep {
  constructor() {
    this.subscribers = new Set()
  }

  depend() {
    if (activeEffect) {
      this.subscribers.add(activeEffect)
    }
  }

  notify() {
    this.subscribers.forEach(effect => effect())
  }
}

let activeEffect = null

function watchEffect(effect) {
  activeEffect = effect
  effect()
  activeEffect = null
}

function reactive(obj) {
  const depsMap = new Map()

  return new Proxy(obj, {
    get(target, key) {
      const dep = depsMap.get(key) || new Dep()
      dep.depend()
      return target[key]
    },
    set(target, key, value) {
      target[key] = value
      const dep = depsMap.get(key) || new Dep()
      dep.notify()
      return true
    }
  })
}

// 使用示例
const state = reactive({
  count: 0,
  name: 'Vue'
})

watchEffect(() => {
  console.log(`Count: ${state.count}`)
})

state.count = 1 // 触发effect
```

### 3.2 深度响应式与浅响应式

```javascript
import { reactive, shallowReactive, readonly, shallowReadonly } from 'vue'

// 深度响应式 - 嵌套对象也是响应式的
const deepReactive = reactive({
  user: {
    name: 'John',
    profile: {
      email: 'john@example.com'
    }
  }
})

// 浅响应式 - 只有根层是响应式的
const shallowReactive = shallowReactive({
  user: {
    name: 'John',
    profile: {
      email: 'john@example.com'
    }
  }
})

// 只读响应式
const readonlyObj = readonly({
  count: 0,
  user: {
    name: 'John'
  }
})

// 浅只读响应式
const shallowReadonly = shallowReadonly({
  count: 0,
  user: {
    name: 'John'
  }
})
```

### 3.3 toRaw和markRaw

```javascript
import { reactive, toRaw, markRaw } from 'vue'

// toRaw - 获取原始对象
const original = { count: 0 }
const reactiveObj = reactive(original)
const rawObj = toRaw(reactiveObj)

console.log(rawObj === original) // true

// markRaw - 标记对象为不可响应式
const staticObject = markRaw({
  id: 1,
  data: 'static data'
})

const state = reactive({
  items: [staticObject]
})

// 修改staticObject不会触发响应式更新
staticObject.data = 'new data' // 不会触发更新
```

## 四、性能优化技巧

### 4.1 computed的高级用法

```javascript
import { computed } from 'vue'

// 带缓存的计算属性
const expensiveValue = computed(() => {
  // 复杂计算
  let result = 0
  for (let i = 0; i < 1000000; i++) {
    result += Math.random()
  }
  return result
})

// 可写的计算属性
const writableComputed = computed({
  get() {
    return state.count * 2
  },
  set(value) {
    state.count = value / 2
  }
})

// 计算属性的调试
const debugComputed = computed(() => {
  console.log('Computing value...')
  return state.count * 2
}, {
  onTrack(e) {
    console.log('Tracking:', e)
  },
  onTrigger(e) {
    console.log('Trigger:', e)
  }
})
```

### 4.2 watch的高级用法

```javascript
import { watch, watchEffect, watchPostEffect } from 'vue'

// 监听多个源
watch(
  [() => state.count, () => state.name],
  ([newCount, newName], [oldCount, oldName]) => {
    console.log(`Count: ${oldCount} -> ${newCount}`)
    console.log(`Name: ${oldName} -> ${newName}`)
  }
)

// 深度监听
watch(
  () => state.user,
  (newUser, oldUser) => {
    console.log('User changed:', newUser)
  },
  { deep: true }
)

// 立即执行
watch(
  () => state.count,
  (newCount) => {
    console.log('Count:', newCount)
  },
  { immediate: true }
)

// watchPostEffect - 在DOM更新后执行
watchPostEffect(() => {
  console.log('DOM updated, count:', state.count)
})

// 清理副作用
watch(
  () => state.searchTerm,
  (newTerm, oldTerm, onCleanup) => {
    const debounceTimer = setTimeout(() => {
      // 执行搜索
    }, 300)

    onCleanup(() => {
      clearTimeout(debounceTimer)
    })
  }
)
```

### 4.3 性能优化技巧

```javascript
import { shallowRef, triggerRef, markRaw } from 'vue'

// shallowRef - 只有.value的访问是响应式的
const largeData = shallowRef({
  items: Array(10000).fill(0).map((_, i) => ({ id: i }))
})

// 手动触发更新
function updateLargeData() {
  largeData.value.items.push({ id: largeData.value.items.length })
  triggerRef(largeData) // 手动触发更新
}

// markRaw - 避免大型对象的响应式开销
const staticConfig = markRaw({
  apiKey: '12345',
  endpoints: ['api1', 'api2', 'api3']
})

// 使用v-once减少不必要的重新渲染
// <div v-once>{{ staticContent }}</div>

// 使用shallowReactive优化大型对象
const largeObject = shallowReactive({
  // 只有根层属性是响应式的
  data: Array(10000).fill(0)
})
```

## 五、类型Script集成

### 5.1 基本类型支持

```typescript
import { ref, reactive, computed } from 'vue'

// 基本类型
const count = ref<number>(0)
const message = ref<string>('Hello')

// 对象类型
interface User {
  id: number
  name: string
  email: string
}

const user = reactive<User>({
  id: 1,
  name: 'John',
  email: 'john@example.com'
})

// 计算属性
const doubleCount = computed<number>(() => count.value * 2)

// 自定义组合函数的类型
function useCounter(initialValue: number = 0) {
  const count = ref<number>(initialValue)
  const increment = () => {
    count.value++
  }

  return {
    count,
    increment
  }
}

// 使用组合函数
const { count: counter, increment } = useCounter(10)
```

### 5.2 高级类型定义

```typescript
import { PropType } from 'vue'

// 组件Props类型
interface UserProps {
  id: number
  name: string
  email?: string
  isAdmin?: boolean
}

export default defineComponent({
  props: {
    user: {
      type: Object as PropType<UserProps>,
      required: true
    },
    status: {
      type: String as PropType<'active' | 'inactive' | 'pending'>,
      default: 'active'
    }
  }
})

// 自定义组合函数的复杂类型
interface UseAsyncOptions<T> {
  immediate?: boolean
  initialData?: T
  onError?: (error: Error) => void
}

interface UseAsyncResult<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<Error | null>
  execute: () => Promise<void>
}

function useAsyncData<T>(
  asyncFunction: () => Promise<T>,
  options: UseAsyncOptions<T> = {}
): UseAsyncResult<T> {
  const { immediate = true, initialData = null, onError } = options

  const data = ref<T | null>(initialData)
  const loading = ref<boolean>(false)
  const error = ref<Error | null>(null)

  const execute = async () => {
    loading.value = true
    error.value = null

    try {
      const result = await asyncFunction()
      data.value = result
    } catch (err) {
      error.value = err as Error
      if (onError) {
        onError(err as Error)
      }
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    execute()
  }

  return {
    data,
    loading,
    error,
    execute
  }
}
```

### 5.3 与Vue 3的setup语法糖

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'

interface Todo {
  id: number
  text: string
  completed: boolean
}

const props = defineProps<{
  todos: Todo[]
}>()

const emit = defineEmits<{
  'update-todo': [todo: Todo]
  'delete-todo': [id: number]
}>()

const newTodoText = ref('')

const incompleteTodos = computed(() => {
  return props.todos.filter(todo => !todo.completed)
})

function addTodo() {
  if (newTodoText.value.trim()) {
    const newTodo: Todo = {
      id: Date.now(),
      text: newTodoText.value,
      completed: false
    }
    emit('update-todo', newTodo)
    newTodoText.value = ''
  }
}

function deleteTodo(id: number) {
  emit('delete-todo', id)
}
</script>
```

## 六、测试Composition API

### 6.1 单元测试组合函数

```javascript
// useCounter.test.js
import { useCounter } from './useCounter'
import { renderHook, act } from '@testing-library/vue'

describe('useCounter', () => {
  it('should initialize with default value', () => {
    const { result } = renderHook(() => useCounter())

    expect(result.current.count.value).toBe(0)
    expect(result.current.doubleCount.value).toBe(0)
  })

  it('should increment counter', () => {
    const { result } = renderHook(() => useCounter())

    act(() => {
      result.current.increment()
    })

    expect(result.current.count.value).toBe(1)
    expect(result.current.doubleCount.value).toBe(2)
  })

  it('should decrement counter', () => {
    const { result } = renderHook(() => useCounter(5))

    act(() => {
      result.current.decrement()
    })

    expect(result.current.count.value).toBe(4)
  })

  it('should reset counter', () => {
    const { result } = renderHook(() => useCounter(10))

    act(() => {
      result.current.increment()
      result.current.increment()
      result.current.reset()
    })

    expect(result.current.count.value).toBe(10)
  })
})
```

### 6.2 组件测试

```javascript
// CounterComponent.test.js
import { mount } from '@vue/test-utils'
import CounterComponent from './CounterComponent.vue'

describe('CounterComponent', () => {
  it('should render counter value', () => {
    const wrapper = mount(CounterComponent, {
      props: {
        initialValue: 5
      }
    })

    expect(wrapper.find('.counter').text()).toBe('5')
  })

  it('should increment counter when button is clicked', async () => {
    const wrapper = mount(CounterComponent)

    await wrapper.find('.increment-btn').trigger('click')

    expect(wrapper.find('.counter').text()).toBe('1')
  })

  it('should emit event when counter changes', async () => {
    const wrapper = mount(CounterComponent)

    await wrapper.find('.increment-btn').trigger('click')

    expect(wrapper.emitted('counter-change')).toBeTruthy()
    expect(wrapper.emitted('counter-change')[0]).toEqual([1])
  })
})
```

## 七、实际应用案例

### 7.1 表单处理

```javascript
// useForm.js
import { ref, reactive, computed } from 'vue'

export function useForm(initialValues, validationRules = {}) {
  const form = reactive({ ...initialValues })
  const errors = reactive({})
  const touched = reactive({})

  const validate = (fieldName) => {
    const value = form[fieldName]
    const rules = validationRules[fieldName] || []

    errors[fieldName] = rules
      .map(rule => rule(value))
      .filter(error => error !== null)

    return errors[fieldName].length === 0
  }

  const validateAll = () => {
    Object.keys(validationRules).forEach(fieldName => {
      validate(fieldName)
    })

    return Object.keys(errors).every(fieldName => errors[fieldName].length === 0)
  }

  const reset = () => {
    Object.keys(form).forEach(key => {
      form[key] = initialValues[key]
    })
    Object.keys(errors).forEach(key => {
      delete errors[key]
    })
    Object.keys(touched).forEach(key => {
      delete touched[key]
    })
  }

  const submit = async (onSubmit) => {
    if (validateAll()) {
      await onSubmit(form)
    }
  }

  return {
    form,
    errors,
    touched,
    validate,
    validateAll,
    reset,
    submit
  }
}

// 使用示例
import { useForm } from './useForm'

const validationRules = {
  email: [
    (value) => !value ? 'Email is required' : null,
    (value) => !/\S+@\S+\.\S+/.test(value) ? 'Invalid email format' : null
  ],
  password: [
    (value) => !value ? 'Password is required' : null,
    (value) => value.length < 6 ? 'Password must be at least 6 characters' : null
  ]
}

const { form, errors, validate, submit } = useForm(
  {
    email: '',
    password: ''
  },
  validationRules
)
```

### 7.2 权限管理

```javascript
// useAuth.js
import { ref, computed, provide, inject } from 'vue'

const AUTH_KEY = Symbol('auth')

export function provideAuth() {
  const user = ref(null)
  const permissions = ref([])

  const isAuthenticated = computed(() => !!user.value)
  const hasPermission = (permission) => {
    return permissions.value.includes(permission)
  }

  const login = async (credentials) => {
    // 登录逻辑
    const response = await fetch('/api/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    })

    const data = await response.json()
    user.value = data.user
    permissions.value = data.permissions
  }

  const logout = () => {
    user.value = null
    permissions.value = []
  }

  const auth = {
    user,
    permissions,
    isAuthenticated,
    hasPermission,
    login,
    logout
  }

  provide(AUTH_KEY, auth)

  return auth
}

export function useAuth() {
  const auth = inject(AUTH_KEY)

  if (!auth) {
    throw new Error('Auth not provided')
  }

  return auth
}

// 使用示例
import { useAuth } from './useAuth'

const { isAuthenticated, hasPermission } = useAuth()

// 在模板中使用
// <div v-if="isAuthenticated">Welcome back!</div>
// <button v-if="hasPermission('admin')">Admin Panel</button>
```

### 7.3 国际化

```javascript
// useI18n.js
import { ref, reactive, provide, inject } from 'vue'

const I18N_KEY = Symbol('i18n')

export function provideI18n(options) {
  const { locale, messages } = options
  const currentLocale = ref(locale)

  const t = (key, params = {}) => {
    let message = messages[currentLocale.value][key] || key

    Object.entries(params).forEach(([param, value]) => {
      message = message.replace(`{${param}}`, value)
    })

    return message
  }

  const setLocale = (newLocale) => {
    currentLocale.value = newLocale
  }

  const i18n = {
    locale: currentLocale,
    t,
    setLocale
  }

  provide(I18N_KEY, i18n)

  return i18n
}

export function useI18n() {
  const i18n = inject(I18N_KEY)

  if (!i18n) {
    throw new Error('I18n not provided')
  }

  return i18n
}

// 使用示例
const { t, setLocale } = useI18n()

// 在模板中使用
// <h1>{{ t('welcome.title') }}</h1>
// <p>{{ t('welcome.message', { name: 'John' }) }}</p>
```

## 八、最佳实践

### 8.1 代码组织

```javascript
// 按功能组织代码
export default {
  setup() {
    // 状态
    const state = reactive({
      items: [],
      loading: false,
      error: null
    })

    // 数据获取
    const fetchData = async () => {
      state.loading = true
      state.error = null

      try {
        const response = await fetch('/api/items')
        state.items = await response.json()
      } catch (error) {
        state.error = error
      } finally {
        state.loading = false
      }
    }

    // 生命周期
    onMounted(() => {
      fetchData()
    })

    // 方法
    const addItem = (item) => {
      state.items.push(item)
    }

    const removeItem = (index) => {
      state.items.splice(index, 1)
    }

    // 计算属性
    const itemCount = computed(() => state.items.length)

    // 返回
    return {
      ...toRefs(state),
      addItem,
      removeItem,
      itemCount
    }
  }
}
```

### 8.2 性能优化

```javascript
// 避免不必要的响应式
const staticConfig = markRaw({
  apiUrl: 'https://api.example.com',
  timeout: 5000
})

// 使用shallowRef优化大型对象
const largeDataset = shallowRef([])

// 使用computed缓存计算结果
const filteredItems = computed(() => {
  return state.items.filter(item => item.active)
})

// 使用watchEffect的清理函数
watchEffect((onCleanup) => {
  const timer = setInterval(() => {
    // 定时任务
  }, 1000)

  onCleanup(() => {
    clearInterval(timer)
  })
})
```

### 8.3 错误处理

```javascript
// 全局错误处理
export default {
  setup() {
    const error = ref(null)

    const handleError = (err) => {
      error.value = err
      console.error('Error:', err)
    }

    const safeExecute = async (fn) => {
      try {
        return await fn()
      } catch (err) {
        handleError(err)
        throw err
      }
    }

    return {
      error,
      handleError,
      safeExecute
    }
  }
}
```

## 九、总结

Vue 3的Composition API提供了更灵活、更强大的组件逻辑组织方式。通过合理使用组合函数、响应式系统和依赖注入，可以构建出更易维护、更易测试的组件。

### 关键要点：

1. **组合函数**：提取可复用的逻辑，提高代码复用性
2. **响应式系统**：理解ref和reactive的区别，合理使用
3. **性能优化**：使用shallowRef、markRaw等优化技巧
4. **类型支持**：充分利用TypeScript的类型系统
5. **测试策略**：为组合函数和组件编写单元测试

Composition API不仅是语法糖，而是一种新的编程范式。掌握它能够帮助你构建更优雅、更高效的Vue应用。记住，**好的代码不仅要能工作，还要易于理解和维护**。