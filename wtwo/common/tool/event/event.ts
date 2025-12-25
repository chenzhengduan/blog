import Subcribe, { IEventNameMap } from "../subcribe";
import { onUnmounted } from "vue";
import { getCurrentInstance } from "vue";


export default function createEvent<T extends IEventNameMap>(config: T) {
  type IKeys = keyof T;
  const subcribe = new Subcribe(config);

  function useEvent() {
    if (!getCurrentInstance()) {
      console.error("useEvent必须在setup或生命周期中同步调用");
      return {
        on: subcribe.on.bind(subcribe),
        once: subcribe.once.bind(subcribe),
        emit: subcribe.emit.bind(subcribe),
        off: subcribe.off.bind(subcribe),
      };
    }
    const offList: Array<Function> = [];

    function on(type: IKeys, fn: (...args: any[]) => any | void) {
      const offFn = subcribe.on(type, fn);
      offList.push(offFn);
      return offFn;
    }
    function once(type: IKeys, fn: (...args: any[]) => any | void) {
      const offFn = subcribe.once(type, fn);
      offList.push(offFn);
      return offFn;
    }
    const emit = subcribe.emit.bind(subcribe);
    const off = subcribe.off.bind(subcribe);

    onUnmounted(() => {
      offList.forEach((off) => off());
    });

    return {
      /**
       * 订阅(在setup中生成的on方法,订阅后会在页面销毁时自动off)
       */
      on,
      once,
      emit,
      off,
    };
  }

  return useEvent
}
