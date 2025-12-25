/**
 * 先定义,后使用,有一个统一的地方查看已定义的事件,拒绝重名
 * {
 *  studentManageListRefresh: Symbol('学员管理页面列表刷新')
 * }
 */

export interface IEventNameMap {
  [key: string]: symbol;
}
interface IListener {
  [key: symbol]: Array<Function>;
}

// T为所有的事件名集合: 'eventA | eventB | eventC'
export default class Subcribe<T extends IEventNameMap, K extends keyof T> {
  private config: T;
  private listeners: IListener = {};
  constructor(config: T) {
    this.config = config;
  }
  on(type: K, fn: (...args: any[]) => any | void) {
    const key = this.config[type];
    if (!this.listeners[key]) {
      this.listeners[key] = [];
    }
    this.listeners[key].push(fn);
    return () => {
      this.off(type, fn);
    };
  }

  once(type: K, fn: (...args: any[]) => any | void) {
    const key = this.config[type];
    if (!this.listeners[key]) {
      this.listeners[key] = [];
    }

    const wrapFn = (...args: any[]) => {
      fn(...args);
      this.off(type, wrapFn);
    };

    this.listeners[key].push(wrapFn);
    return () => {
      this.off(type, fn);
    };
  }
  off(type: K, fn?: (...args: any[]) => any | void) {
    const key = this.config[type];
    if (this.listeners[key]) {
      if (!fn) {
        console.warn("移除全部事件");
        this.listeners[key] = [];
      } else {
        const index = this.listeners[key].findIndex((item) => item === fn);
        if (index > -1) {
          this.listeners[key].splice(index, 1);
        }
      }
    }
  }
  emit(type: K, ...args: any[]) {
    const key = this.config[type];
    if (this.listeners[key]) {
      this.listeners[key].forEach((fn) => {
        fn(...args);
      });
    }
  }
}
