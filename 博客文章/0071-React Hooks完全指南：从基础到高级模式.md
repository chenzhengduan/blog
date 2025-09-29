# React Hooks完全指南：从基础到高级模式

> React Hooks是React 16.8引入的革命性特性，它让函数组件拥有了状态管理和生命周期的能力。本文将深入探讨Hooks的原理、用法和最佳实践。

## 引言

React Hooks改变了我们编写React组件的方式，使得函数组件能够拥有类组件的所有功能，同时提供了更简洁、更直观的API。通过Hooks，我们可以将组件逻辑提取到可重用的函数中，大大提高了代码的复用性和可测试性。

## 一、基础Hooks

### 1.1 useState - 状态管理

```jsx
import React, { useState } from 'react';

function Counter() {
  // 基本用法
  const [count, setCount] = useState(0);
  const [name, setName] = useState('John');

  // 函数式更新
  const increment = () => {
    setCount(prevCount => prevCount + 1);
  };

  // 对象状态
  const [user, setUser] = useState({
    id: 1,
    name: 'John',
    age: 30
  });

  const updateAge = (newAge) => {
    setUser(prevUser => ({
      ...prevUser,
      age: newAge
    }));
  };

  // 懒初始化
  const [expensiveValue, setExpensiveValue] = useState(() => {
    return computeExpensiveValue();
  });

  return (
    <div>
      <h1>Count: {count}</h1>
      <button onClick={increment}>Increment</button>

      <h2>Name: {name}</h2>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
      />

      <h3>User: {user.name}, Age: {user.age}</h3>
      <button onClick={() => updateAge(31)}>Update Age</button>
    </div>
  );
}

function computeExpensiveValue() {
  // 模拟昂贵的计算
  return Math.random() * 100;
}
```

### 1.2 useEffect - 副作用处理

```jsx
import React, { useState, useEffect } from 'react';

function UserProfile({ userId }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 基本用法 - 组件挂载时执行
  useEffect(() => {
    console.log('Component mounted');
    return () => {
      console.log('Component will unmount');
    };
  }, []);

  // 依赖数组变化时执行
  useEffect(() => {
    const fetchUser = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch(`/api/users/${userId}`);
        const userData = await response.json();
        setUser(userData);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [userId]);

  // 清理函数
  useEffect(() => {
    const timer = setInterval(() => {
      console.log('Timer tick');
    }, 1000);

    return () => {
      clearInterval(timer);
    };
  }, []);

  // 多个依赖
  useEffect(() => {
    if (user) {
      document.title = `User: ${user.name}`;
    }
  }, [user]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div>
      <h1>{user?.name}</h1>
      <p>Email: {user?.email}</p>
    </div>
  );
}
```

### 1.3 useContext - 上下文消费

```jsx
import React, { createContext, useContext, useState } from 'react';

// 创建上下文
const ThemeContext = createContext();
const UserContext = createContext();

// 主题提供者
function ThemeProvider({ children }) {
  const [theme, setTheme] = useState('light');

  const toggleTheme = () => {
    setTheme(prevTheme => prevTheme === 'light' ? 'dark' : 'light');
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

// 用户提供者
function UserProvider({ children }) {
  const [user, setUser] = useState(null);

  const login = (userData) => {
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <UserContext.Provider value={{ user, login, logout }}>
      {children}
    </UserContext.Provider>
  );
}

// 消费上下文
function ThemeButton() {
  const { theme, toggleTheme } = useContext(ThemeContext);

  return (
    <button
      onClick={toggleTheme}
      style={{ background: theme === 'light' ? '#fff' : '#333' }}
    >
      Toggle Theme
    </button>
  );
}

function UserInfo() {
  const { user, logout } = useContext(UserContext);

  if (!user) return null;

  return (
    <div>
      <h2>Welcome, {user.name}</h2>
      <button onClick={logout}>Logout</button>
    </div>
  );
}

// 组合使用
function App() {
  return (
    <ThemeProvider>
      <UserProvider>
        <div>
          <ThemeButton />
          <UserInfo />
        </div>
      </UserProvider>
    </ThemeProvider>
  );
}
```

## 二、额外Hooks

### 2.1 useReducer - 复杂状态管理

```jsx
import React, { useReducer } from 'react';

// 初始状态
const initialState = {
  count: 0,
  isLoading: false,
  error: null,
  data: null
};

// 动作类型
const ACTION_TYPES = {
  INCREMENT: 'INCREMENT',
  DECREMENT: 'DECREMENT',
  RESET: 'RESET',
  FETCH_START: 'FETCH_START',
  FETCH_SUCCESS: 'FETCH_SUCCESS',
  FETCH_ERROR: 'FETCH_ERROR'
};

// Reducer函数
function counterReducer(state, action) {
  switch (action.type) {
    case ACTION_TYPES.INCREMENT:
      return { ...state, count: state.count + 1 };

    case ACTION_TYPES.DECREMENT:
      return { ...state, count: state.count - 1 };

    case ACTION_TYPES.RESET:
      return { ...state, count: 0 };

    case ACTION_TYPES.FETCH_START:
      return { ...state, isLoading: true, error: null };

    case ACTION_TYPES.FETCH_SUCCESS:
      return {
        ...state,
        isLoading: false,
        data: action.payload,
        error: null
      };

    case ACTION_TYPES.FETCH_ERROR:
      return {
        ...state,
        isLoading: false,
        error: action.payload,
        data: null
      };

    default:
      return state;
  }
}

function ComplexComponent() {
  const [state, dispatch] = useReducer(counterReducer, initialState);

  const fetchData = async () => {
    dispatch({ type: ACTION_TYPES.FETCH_START });

    try {
      const response = await fetch('/api/data');
      const data = await response.json();
      dispatch({ type: ACTION_TYPES.FETCH_SUCCESS, payload: data });
    } catch (error) {
      dispatch({ type: ACTION_TYPES.FETCH_ERROR, payload: error });
    }
  };

  return (
    <div>
      <h1>Count: {state.count}</h1>
      <button onClick={() => dispatch({ type: ACTION_TYPES.INCREMENT })}>
        Increment
      </button>
      <button onClick={() => dispatch({ type: ACTION_TYPES.DECREMENT })}>
        Decrement
      </button>
      <button onClick={() => dispatch({ type: ACTION_TYPES.RESET })}>
        Reset
      </button>

      <button onClick={fetchData} disabled={state.isLoading}>
        {state.isLoading ? 'Loading...' : 'Fetch Data'}
      </button>

      {state.error && <div>Error: {state.error.message}</div>}
      {state.data && <div>Data: {JSON.stringify(state.data)}</div>}
    </div>
  );
}
```

### 2.2 useCallback - 函数记忆化

```jsx
import React, { useState, useCallback, useMemo } from 'react';

function ParentComponent() {
  const [count, setCount] = useState(0);

  // 普通函数 - 每次渲染都会重新创建
  const handleClick = () => {
    console.log('Button clicked');
  };

  // 使用useCallback - 依赖不变时函数不会重新创建
  const memoizedHandleClick = useCallback(() => {
    console.log('Memoized button clicked');
  }, []);

  // 带依赖的useCallback
  const handleCountUpdate = useCallback(() => {
    console.log('Count updated:', count);
  }, [count]);

  return (
    <div>
      <h1>Count: {count}</h1>
      <button onClick={() => setCount(count + 1)}>Increment</button>

      <ChildComponent onClick={memoizedHandleClick} />
      <CountDisplay count={count} onUpdate={handleCountUpdate} />
    </div>
  );
}

function ChildComponent({ onClick }) {
  // 只有当onClick函数变化时才会重新渲染
  console.log('ChildComponent rendered');

  return (
    <button onClick={onClick}>
      Child Button
    </button>
  );
}

function CountDisplay({ count, onUpdate }) {
  // 只有当count或onUpdate变化时才会重新渲染
  console.log('CountDisplay rendered');

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={onUpdate}>Update Count</button>
    </div>
  );
}
```

### 2.3 useMemo - 值记忆化

```jsx
import React, { useState, useMemo } from 'react';

function ExpensiveCalculation({ numbers }) {
  const [filter, setFilter] = useState('');

  // 昂贵的计算 - 只有当numbers变化时才重新计算
  const processedNumbers = useMemo(() => {
    console.log('Expensive calculation running...');
    return numbers
      .map(n => n * 2)
      .filter(n => n > 10)
      .sort((a, b) => a - b);
  }, [numbers]);

  // 过滤后的结果 - 只有当processedNumbers或filter变化时才重新计算
  const filteredNumbers = useMemo(() => {
    console.log('Filtering numbers...');
    return processedNumbers.filter(num =>
      num.toString().includes(filter)
    );
  }, [processedNumbers, filter]);

  // 复杂对象记忆化
  const expensiveObject = useMemo(() => {
    return {
      sum: numbers.reduce((a, b) => a + b, 0),
      average: numbers.reduce((a, b) => a + b, 0) / numbers.length,
      min: Math.min(...numbers),
      max: Math.max(...numbers)
    };
  }, [numbers]);

  return (
    <div>
      <input
        type="text"
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
        placeholder="Filter numbers..."
      />

      <h3>Filtered Numbers:</h3>
      <ul>
        {filteredNumbers.map((num, index) => (
          <li key={index}>{num}</li>
        ))}
      </ul>

      <h3>Statistics:</h3>
      <p>Sum: {expensiveObject.sum}</p>
      <p>Average: {expensiveObject.average}</p>
      <p>Min: {expensiveObject.min}</p>
      <p>Max: {expensiveObject.max}</p>
    </div>
  );
}
```

### 2.4 useRef - 引用操作

```jsx
import React, { useRef, useEffect, useState } from 'react';

function RefExample() {
  // DOM引用
  const inputRef = useRef(null);
  const buttonRef = useRef(null);

  // 普通值引用（不会触发重新渲染）
  const renderCount = useRef(0);
  const timerRef = useRef(null);

  const [count, setCount] = useState(0);

  // 聚焦输入框
  const focusInput = () => {
    inputRef.current.focus();
  };

  // 获取按钮位置
  const getButtonPosition = () => {
    const rect = buttonRef.current.getBoundingClientRect();
    console.log('Button position:', rect);
  };

  // 使用ref存储值
  useEffect(() => {
    renderCount.current += 1;
    console.log('Component rendered:', renderCount.current, 'times');
  });

  // 使用ref存储定时器
  useEffect(() => {
    timerRef.current = setInterval(() => {
      console.log('Timer tick');
    }, 1000);

    return () => {
      clearInterval(timerRef.current);
    };
  }, []);

  // 修改ref值（不会触发重新渲染）
  const updateRefValue = () => {
    renderCount.current += 100;
    console.log('Updated ref value:', renderCount.current);
  };

  return (
    <div>
      <input
        ref={inputRef}
        type="text"
        placeholder="Focus me"
      />

      <button
        ref={buttonRef}
        onClick={focusInput}
      >
        Focus Input
      </button>

      <button onClick={getButtonPosition}>
        Get Button Position
      </button>

      <button onClick={updateRefValue}>
        Update Ref Value
      </button>

      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>
        Increment
      </button>
    </div>
  );
}

// 可变ref示例
function MutableRefExample() {
  const [count, setCount] = useState(0);
  const latestCount = useRef(count);

  // 每次count更新时更新ref
  useEffect(() => {
    latestCount.current = count;
  }, [count]);

  const showAlert = () => {
    setTimeout(() => {
      alert(`Latest count: ${latestCount.current}`);
    }, 3000);
  };

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>
        Increment
      </button>
      <button onClick={showAlert}>
        Show Alert
      </button>
    </div>
  );
}
```

## 三、自定义Hooks

### 3.1 基础自定义Hook

```jsx
import { useState, useEffect } from 'react';

// useCounter Hook
function useCounter(initialValue = 0) {
  const [count, setCount] = useState(initialValue);

  const increment = () => {
    setCount(prevCount => prevCount + 1);
  };

  const decrement = () => {
    setCount(prevCount => prevCount - 1);
  };

  const reset = () => {
    setCount(initialValue);
  };

  return {
    count,
    increment,
    decrement,
    reset
  };
}

// 使用示例
function CounterComponent() {
  const { count, increment, decrement, reset } = useCounter(0);

  return (
    <div>
      <h1>Count: {count}</h1>
      <button onClick={increment}>Increment</button>
      <button onClick={decrement}>Decrement</button>
      <button onClick={reset}>Reset</button>
    </div>
  );
}
```

### 3.2 数据获取Hook

```jsx
import { useState, useEffect } from 'react';

function useFetch(url, options = {}) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch(url, options);
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const result = await response.json();
        setData(result);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [url, JSON.stringify(options)]);

  return { data, loading, error };
}

// 使用示例
function UserProfile({ userId }) {
  const { data: user, loading, error } = useFetch(
    `https://jsonplaceholder.typicode.com/users/${userId}`
  );

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div>
      <h1>{user?.name}</h1>
      <p>Email: {user?.email}</p>
      <p>Phone: {user?.phone}</p>
    </div>
  );
}
```

### 3.3 表单处理Hook

```jsx
import { useState } from 'react';

function useForm(initialValues, validate) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setValues(prevValues => ({
      ...prevValues,
      [name]: value
    }));

    if (touched[name] && validate) {
      const fieldErrors = validate({ ...values, [name]: value });
      setErrors(prevErrors => ({
        ...prevErrors,
        [name]: fieldErrors[name]
      }));
    }
  };

  const handleBlur = (e) => {
    const { name } = e.target;
    setTouched(prevTouched => ({
      ...prevTouched,
      [name]: true
    }));

    if (validate) {
      const fieldErrors = validate(values);
      setErrors(prevErrors => ({
        ...prevErrors,
        [name]: fieldErrors[name]
      }));
    }
  };

  const handleSubmit = (onSubmit) => (e) => {
    e.preventDefault();

    setTouched(
      Object.keys(values).reduce((acc, key) => {
        acc[key] = true;
        return acc;
      }, {})
    );

    if (validate) {
      const validationErrors = validate(values);
      setErrors(validationErrors);

      if (Object.keys(validationErrors).length === 0) {
        onSubmit(values);
      }
    } else {
      onSubmit(values);
    }
  };

  const reset = () => {
    setValues(initialValues);
    setErrors({});
    setTouched({});
  };

  return {
    values,
    errors,
    touched,
    handleChange,
    handleBlur,
    handleSubmit,
    reset
  };
}

// 使用示例
const validateForm = (values) => {
  const errors = {};

  if (!values.email) {
    errors.email = 'Email is required';
  } else if (!/\S+@\S+\.\S+/.test(values.email)) {
    errors.email = 'Email is invalid';
  }

  if (!values.password) {
    errors.password = 'Password is required';
  } else if (values.password.length < 6) {
    errors.password = 'Password must be at least 6 characters';
  }

  return errors;
};

function LoginForm() {
  const { values, errors, touched, handleChange, handleBlur, handleSubmit } = useForm(
    {
      email: '',
      password: ''
    },
    validateForm
  );

  const onSubmit = (values) => {
    console.log('Form submitted:', values);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label>Email:</label>
        <input
          type="email"
          name="email"
          value={values.email}
          onChange={handleChange}
          onBlur={handleBlur}
        />
        {touched.email && errors.email && (
          <div style={{ color: 'red' }}>{errors.email}</div>
        )}
      </div>

      <div>
        <label>Password:</label>
        <input
          type="password"
          name="password"
          value={values.password}
          onChange={handleChange}
          onBlur={handleBlur}
        />
        {touched.password && errors.password && (
          <div style={{ color: 'red' }}>{errors.password}</div>
        )}
      </div>

      <button type="submit">Login</button>
    </form>
  );
}
```

### 3.4 本地存储Hook

```jsx
import { useState, useEffect } from 'react';

function useLocalStorage(key, initialValue) {
  const [storedValue, setStoredValue] = useState(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error('Error reading localStorage:', error);
      return initialValue;
    }
  });

  const setValue = (value) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      setStoredValue(valueToStore);
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error('Error setting localStorage:', error);
    }
  };

  return [storedValue, setValue];
}

// 使用示例
function LocalStorageExample() {
  const [name, setName] = useLocalStorage('username', '');
  const [preferences, setPreferences] = useLocalStorage('userPreferences', {
    theme: 'light',
    language: 'en'
  });

  return (
    <div>
      <h1>LocalStorage Example</h1>

      <div>
        <label>Name:</label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </div>

      <div>
        <label>Theme:</label>
        <select
          value={preferences.theme}
          onChange={(e) => setPreferences({
            ...preferences,
            theme: e.target.value
          })}
        >
          <option value="light">Light</option>
          <option value="dark">Dark</option>
        </select>
      </div>

      <div>
        <label>Language:</label>
        <select
          value={preferences.language}
          onChange={(e) => setPreferences({
            ...preferences,
            language: e.target.value
          })}
        >
          <option value="en">English</option>
          <option value="zh">Chinese</option>
        </select>
      </div>
    </div>
  );
}
```

## 四、高级Hook模式

### 4.1 Hook组合

```jsx
import { useState, useEffect, useCallback } from 'react';

// useDebounce Hook
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}

// usePrevious Hook
function usePrevious(value) {
  const ref = useRef();
  useEffect(() => {
    ref.current = value;
  });
  return ref.current;
}

// useClickOutside Hook
function useClickOutside(ref, handler) {
  useEffect(() => {
    const listener = (event) => {
      if (!ref.current || ref.current.contains(event.target)) {
        return;
      }
      handler(event);
    };

    document.addEventListener('mousedown', listener);
    document.addEventListener('touchstart', listener);

    return () => {
      document.removeEventListener('mousedown', listener);
      document.removeEventListener('touchstart', listener);
    };
  }, [ref, handler]);
}

// 组合使用示例
function SearchComponent() {
  const [searchTerm, setSearchTerm] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  // 防抖搜索词
  const debouncedSearchTerm = useDebounce(searchTerm, 500);

  // 获取上一次的搜索词
  const prevSearchTerm = usePrevious(debouncedSearchTerm);

  // 搜索逻辑
  useEffect(() => {
    if (debouncedSearchTerm) {
      setLoading(true);
      fetchSearchResults(debouncedSearchTerm).then(data => {
        setResults(data);
        setLoading(false);
      });
    } else {
      setResults([]);
    }
  }, [debouncedSearchTerm]);

  return (
    <div>
      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder="Search..."
      />
      {loading && <div>Loading...</div>}
      {results.length > 0 && (
        <ul>
          {results.map((result, index) => (
            <li key={index}>{result.title}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

### 4.2 性能优化Hook

```jsx
import { useState, useEffect, useRef, useCallback, useMemo } from 'react';

// useIntersectionObserver Hook
function useIntersectionObserver(ref, options) {
  const [isIntersecting, setIsIntersecting] = useState(false);

  useEffect(() => {
    const element = ref.current;
    if (!element) return;

    const observer = new IntersectionObserver(([entry]) => {
      setIsIntersecting(entry.isIntersecting);
    }, options);

    observer.observe(element);

    return () => {
      observer.unobserve(element);
    };
  }, [ref, options]);

  return isIntersecting;
}

// useVirtualList Hook
function useVirtualList(items, containerHeight, itemHeight) {
  const [scrollTop, setScrollTop] = useState(0);
  const containerRef = useRef(null);

  const startIndex = Math.floor(scrollTop / itemHeight);
  const endIndex = Math.min(
    startIndex + Math.ceil(containerHeight / itemHeight) + 1,
    items.length - 1
  );

  const visibleItems = items.slice(startIndex, endIndex + 1);

  const offsetY = startIndex * itemHeight;
  const totalHeight = items.length * itemHeight;

  const handleScroll = useCallback(() => {
    if (containerRef.current) {
      setScrollTop(containerRef.current.scrollTop);
    }
  }, []);

  return {
    containerRef,
    visibleItems,
    offsetY,
    totalHeight,
    handleScroll
  };
}

// 使用示例
function VirtualList({ items, containerHeight, itemHeight }) {
  const {
    containerRef,
    visibleItems,
    offsetY,
    totalHeight,
    handleScroll
  } = useVirtualList(items, containerHeight, itemHeight);

  return (
    <div
      ref={containerRef}
      style={{ height: containerHeight, overflow: 'auto' }}
      onScroll={handleScroll}
    >
      <div style={{ height: totalHeight, position: 'relative' }}>
        <div style={{ position: 'absolute', top: offsetY, width: '100%' }}>
          {visibleItems.map((item, index) => (
            <div key={index} style={{ height: itemHeight }}>
              {item.content}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
```

### 4.3 状态管理Hook

```jsx
import { useState, useEffect, useContext, createContext, useCallback } from 'react';

// 全局状态上下文
const GlobalStateContext = createContext();

// useGlobalState Hook
function useGlobalState() {
  const context = useContext(GlobalStateContext);
  if (!context) {
    throw new Error('useGlobalState must be used within a GlobalStateProvider');
  }
  return context;
}

// 状态提供者
function GlobalStateProvider({ children }) {
  const [state, setState] = useState({
    user: null,
    theme: 'light',
    notifications: []
  });

  const updateUser = useCallback((userData) => {
    setState(prev => ({
      ...prev,
      user: userData
    }));
  }, []);

  const updateTheme = useCallback((theme) => {
    setState(prev => ({
      ...prev,
      theme
    }));
  }, []);

  const addNotification = useCallback((notification) => {
    setState(prev => ({
      ...prev,
      notifications: [...prev.notifications, notification]
    }));
  }, []);

  const removeNotification = useCallback((id) => {
    setState(prev => ({
      ...prev,
      notifications: prev.notifications.filter(n => n.id !== id)
    }));
  }, []);

  const value = {
    state,
    updateUser,
    updateTheme,
    addNotification,
    removeNotification
  };

  return (
    <GlobalStateContext.Provider value={value}>
      {children}
    </GlobalStateContext.Provider>
  );
}

// 使用示例
function UserProfile() {
  const { state, updateUser } = useGlobalState();

  return (
    <div>
      <h1>{state.user?.name}</h1>
      <button onClick={() => updateUser({ name: 'New Name' })}>
        Update Name
      </button>
    </div>
  );
}
```

## 五、Hook最佳实践

### 5.1 Hook规则

```jsx
// 1. 只在最顶层调用Hook
function BadComponent() {
  if (condition) {
    // 错误：不能在条件语句中调用Hook
    const [state, setState] = useState(0);
  }

  // 正确：总是在最顶层调用Hook
  const [state, setState] = useState(0);
}

// 2. 只在React函数中调用Hook
function regularFunction() {
  // 错误：不能在普通函数中调用Hook
  const [state, setState] = useState(0);
}

// 正确：在React组件或自定义Hook中调用Hook
function MyComponent() {
  const [state, setState] = useState(0);
  return <div>{state}</div>;
}

function useCustomHook() {
  const [state, setState] = useState(0);
  return state;
}
```

### 5.2 性能优化

```jsx
// 使用useCallback优化函数
function ParentComponent() {
  const [count, setCount] = useState(0);

  // 优化：依赖不变时函数不会重新创建
  const handleClick = useCallback(() => {
    console.log('Button clicked');
  }, []);

  // 优化：带依赖的useCallback
  const handleCountChange = useCallback(() => {
    console.log('Count:', count);
  }, [count]);

  return (
    <div>
      <ChildComponent onClick={handleClick} />
      <button onClick={() => setCount(count + 1)}>
        Increment
      </button>
    </div>
  );
}

// 使用useMemo优化计算
function ExpensiveComponent({ data }) {
  // 优化：只有data变化时才重新计算
  const processedData = useMemo(() => {
    return data.map(item => ({
      ...item,
      processed: true
    }));
  }, [data]);

  return (
    <div>
      {processedData.map(item => (
        <div key={item.id}>{item.name}</div>
      ))}
    </div>
  );
}
```

### 5.3 错误处理

```jsx
import { useState, useEffect } from 'react';

// useAsync Hook with error handling
function useAsync(asyncFunction, immediate = true) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const execute = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const result = await asyncFunction();
      setData(result);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [asyncFunction]);

  useEffect(() => {
    if (immediate) {
      execute();
    }
  }, [execute, immediate]);

  return { data, loading, error, execute };
}

// 使用示例
function DataLoader() {
  const { data, loading, error, execute } = useAsync(
    () => fetch('/api/data').then(res => res.json()),
    false // 不立即执行
  );

  const handleRetry = () => {
    execute();
  };

  if (loading) return <div>Loading...</div>;
  if (error) return (
    <div>
      Error: {error.message}
      <button onClick={handleRetry}>Retry</button>
    </div>
  );

  return (
    <div>
      <button onClick={execute}>Load Data</button>
      {data && <pre>{JSON.stringify(data, null, 2)}</pre>}
    </div>
  );
}
```

## 六、测试React Hooks

### 6.1 测试自定义Hook

```jsx
import { renderHook, act } from '@testing-library/react';
import { useCounter } from './useCounter';

describe('useCounter', () => {
  it('should initialize with default value', () => {
    const { result } = renderHook(() => useCounter());

    expect(result.current.count).toBe(0);
  });

  it('should increment counter', () => {
    const { result } = renderHook(() => useCounter());

    act(() => {
      result.current.increment();
    });

    expect(result.current.count).toBe(1);
  });

  it('should decrement counter', () => {
    const { result } = renderHook(() => useCounter(5));

    act(() => {
      result.current.decrement();
    });

    expect(result.current.count).toBe(4);
  });

  it('should reset counter', () => {
    const { result } = renderHook(() => useCounter(10));

    act(() => {
      result.current.increment();
      result.current.increment();
      result.current.reset();
    });

    expect(result.current.count).toBe(10);
  });
});
```

### 6.2 测试组件中的Hook

```jsx
import { render, screen, fireEvent } from '@testing-library/react';
import CounterComponent from './CounterComponent';

describe('CounterComponent', () => {
  it('should render initial count', () => {
    render(<CounterComponent />);
    expect(screen.getByText('Count: 0')).toBeInTheDocument();
  });

  it('should increment count when button is clicked', () => {
    render(<CounterComponent />);
    const button = screen.getByText('Increment');

    fireEvent.click(button);
    expect(screen.getByText('Count: 1')).toBeInTheDocument();
  });

  it('should decrement count when button is clicked', () => {
    render(<CounterComponent />);
    const button = screen.getByText('Decrement');

    fireEvent.click(button);
    expect(screen.getByText('Count: -1')).toBeInTheDocument();
  });
});
```

## 七、总结

React Hooks为函数组件提供了强大的状态管理和副作用处理能力。通过合理使用Hooks，我们可以：

1. **简化组件逻辑**：将相关逻辑组织在一起，提高代码可读性
2. **提高复用性**：通过自定义Hook提取可重用逻辑
3. **优化性能**：使用useCallback和useMemo避免不必要的重新渲染
4. **改善测试**：自定义Hook更容易进行单元测试

### 关键要点：

1. **Hook规则**：只在最顶层调用Hook，只在React函数中调用Hook
2. **依赖数组**：正确使用useEffect的依赖数组，避免无限循环
3. **性能优化**：合理使用useCallback和useMemo
4. **自定义Hook**：提取可重用逻辑，提高代码复用性
5. **错误处理**：在Hook中添加适当的错误处理机制

React Hooks不仅改变了我们编写React组件的方式，更重要的是改变了我们思考组件逻辑的方式。通过掌握Hooks，你可以构建出更简洁、更高效、更易维护的React应用。