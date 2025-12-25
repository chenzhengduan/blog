// 和web端共用的域名,要对本地存储做前缀隔离

const Prefix = "_WTwo_";

export const _getItem = localStorage.getItem;
export const _setItem = localStorage.setItem;
export const _removeItem = localStorage.removeItem;
export const _lsDontClear_ = localStorage.clear;

export default {
  getItem<T>(key: string) {
    const key2 = Prefix + key;
    return _getItem.call(localStorage, key2) as T;
  },
  setItem(key: string, data: any) {
    const key2 = Prefix + key;
    if (typeof data === "object") {
      const val = JSON.stringify(data);
      _setItem.call(localStorage, key2, val);
    } else {
      _setItem.call(localStorage, key2, data as string);
    }
  },
  removeItem(key: string) {
    const key2 = Prefix + key;
    _removeItem.call(localStorage, key2);
  },
};

// 覆写
// localStorage.getItem = function () {
//   console.error("请使用封装的getItem方法");
//   return "";
// };

// localStorage.setItem = function () {
//   console.error("请使用封装的getItem方法");
// };

// localStorage.removeItem = function () {
//   console.error("请使用封装的removeItem方法");
// };
// localStorage.clear = function () {
//   console.error("不允许使用clear方法");
// };
