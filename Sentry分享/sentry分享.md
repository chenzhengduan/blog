# Sentry 使用与配置说明

本文档介绍前端项目中 Sentry 的基本配置及其作用，方便团队统一理解和使用。
123123
---
123
## 1. 基本初始化示例
<!--123-->
```ts
import * as Sentry from "@sentry/browser";
import { BrowserTracing } from "@sentry/tracing";

const APP_VERSION = import.meta.env.VITE_APP_VERSION || "0.0.1";
const ENV = import.meta.env.VITE_APP_ENV || import.meta.env.MODE || "development";

Sentry.init({
  dsn: import.meta.env.VITE_SENTRY_DSN || "",
  environment: ENV,
  release: `my-app@${APP_VERSION}`,

  tracesSampleRate: ENV === "prod" ? 0.3 : 0.8,
  sampleRate: 1.0,

  maxValueLength: 10000,
  normalizeDepth: 10,

  allowUrls: [/https?:\/\/((cdn|www)\.)?example\.com/],
  ignoreErrors: [
    "ResizeObserver loop limit exceeded",
    "Script error.",
    /Loading chunk \d+ failed/,
  ],

  beforeSend: (event) => {
    if (event.user) {
      delete event.user.email;
      delete event.user.ip_address;
    }
    if (event.request?.url?.includes("token")) {
      event.request.url = "[Filtered]";
    }
    return event;
  },

  integrations: [new BrowserTracing()],
});
```

## 2. 配置项说明
| 配置项                  | 作用                      | 建议值 / 说明                  |
| -------------------- | ----------------------- | ------------------------- |
| **dsn**              | 项目的数据源地址，Sentry 后台提供    | 必填，生产和测试环境可用不同 DSN        |
| **environment**      | 当前运行环境，用于后台区分日志来源       | 建议：`prod`、`test`、`dev`    |
| **release**          | 应用版本号，用于日志与版本关联         | 建议：`my-app@版本号`           |
| **tracesSampleRate** | 性能监控采样率（APM）            | 生产：0.1\~0.3，开发/测试：1.0     |
| **sampleRate**       | 错误日志采样率                 | 一般 1.0（100%）              |
| **maxValueLength**   | 单个字段最大字符长度              | 默认 250，可调大避免数据被截断         |
| **normalizeDepth**   | 对象/数组序列化深度              | 默认 3，调大会增加数据体积            |
| **allowUrls**        | 只采集指定 URL 来源的错误         | 可过滤掉第三方脚本报错               |
| **ignoreErrors**     | 忽略指定错误信息                | 减少无意义的噪音                  |
| **beforeSend**       | 发送前处理事件，可做脱敏            | 建议删除敏感字段，如 token、email、IP |
| **integrations**     | 插件集成，如 `BrowserTracing` | 性能监控需要开启此项                |

---

## 3. 上下文信息设置

在初始化后，可以为事件附加用户、标签、额外信息：

```ts
// 用户信息（会随每条事件一起发送）
Sentry.setUser({ id: "123", username: "Tom" });

// 单个标签
Sentry.setTag("module", "order");

// 多个标签
Sentry.setTags({ feature: "checkout", page: "OrderPage" });

// 额外信息
Sentry.setExtras({ courseId: "C123", orderId: "O456" });

```

> 注意：setUser 一旦设置，会在当前会话中所有事件自动附带，直到被修改或清空。

---

## 4. 全局错误捕获

> 除了框架自带的异常捕获，建议额外监听以下全局事件：

```ts
// 捕捉未处理的 Promise 错误
window.addEventListener("unhandledrejection", (event) => {
  Sentry.captureException(event.reason);
});
```

> 如果需要采集接口错误，可在 Axios 等 HTTP 客户端拦截器中调用：

```ts
axios.interceptors.response.use(
  res => res,
  err => {
    Sentry.captureException(err, {
      tags: { type: "API_ERROR", url: err.config?.url }
    });
    return Promise.reject(err);
  }
);

```

---

## 5. 性能监控与会话回放（可选）

> 如果需要性能分析与用户操作回放，可额外配置：

```ts
replaysSessionSampleRate: 0.1, // 会话回放采样率
replaysOnErrorSampleRate: 1.0, // 报错时 100% 回放
```

> 需要配合 @sentry/replay 集成使用。

---

## 6. 最佳实践建议

1. 错误日志采样率：生产环境保持 100%（sampleRate: 1.0）

2. 性能采样率：生产 0.1~0.3 即可，避免成本过高

3. 敏感数据脱敏：beforeSend 必须过滤 token、密码、隐私信息

4. 版本管理：使用 release 与源码映射（Source Map）配合，能直接还原出错代码行

5. 标签与上下文：为关键日志添加 setTag、setExtras 方便后台过滤

## 7. 工具类封装

1. 封装一个`Logger`工具类统一调用 Sentry:
    - 自动带环境、版本
    - 自动附加用户、站点、额外信息
2. 避免各页面直接调用 `Sentry.captureException` ，保证日志统一和可管理性。


## 8. 参考文档

[Sentry SDK 官方文档](https://docs.sentry.io/platforms/javascript/)
