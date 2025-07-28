# Locale 国际化

## 使用方法

```javascript
import Vue from "vue";
import { VeLocale } from "vue-fantable";
// 引入英文语言包
import enUS from "vue-fantable/libs/locale/lang/en-US.js";

VeLocale.use(enUS);
```

## 全局使用

将 veLocale 组件挂载到 Vue 的 prototype 原型上，便于全局调用

```javascript
import Vue from "vue";
import { VeLocale } from "vue-fantable";

Vue.prototype.$veLocale = VeLocale;
```

调用

```javascript
import enUS from "vue-fantable/libs/locale/lang/en-US.js";
this.$veLocale.use(enUS);
```

## 语言切换

你可以通过 VeLocal 组件实现多语言支持，使用 VeLocal.use 方法可以切换当前使用的语言

## 覆盖语言包

通过 VeLocale.update 方法可以实现文案的修改和扩展

## API

### methods

| 方法名称 | 说明 | 参数 |
|----------|------|------|
| use | 使用语言包 | 语言包 |
| update | 修改或者扩展语言包 | 语言包 |
``` 