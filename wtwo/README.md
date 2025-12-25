# WTwo Enterprise Management System

[![Vue 3](https://img.shields.io/badge/vue-3.x-brightgreen.svg)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/vite-^5.0-blue.svg)](https://vitejs.dev/)
[![TypeScript](https://img.shields.io/badge/typescript-~5.4-blue.svg)](https://www.typescriptlang.org/)
[![Element Plus](https://img.shields.io/badge/element--plus-2.x-409EFF.svg)](https://element-plus.org/)
[![Pinia](https://img.shields.io/badge/pinia-2.x-yellow.svg)](https://pinia.vuejs.org/)

WTwo 是一款基于 Vue 3 和 Vite 构建的现代化企业级后台管理系统前端项目。
<!--sadas-->
---

## 1. 项目启动与部署

### 本地开发

1.  **安装依赖**
    ```bash
    npm install
    ```

2.  **启动开发服务器**
    ```bash
    npm run dev
    ```
    服务将运行在 `https://localhost:3011`。

### 构建与打包

1.  **生产环境构建**
    ```bash
    npm run build
    ```
    构建产物将生成在 `dist/` 目录中。

2.  **构建产物预览**
    ```bash
    npm run preview
    ```

### Docker 部署

本项目支持通过 Docker 进行容器化部署。

1.  **构建 Docker 镜像**
    ```bash
    docker build -t wtwo-frontend .
    ```

2.  **运行 Docker 容器**
    ```bash
    docker run -d -p 80:80 wtwo-frontend
    ```

---

## 2. 根目录文件/目录结构详解

这是项目的顶层结构，每个文件和目录都有其特定的职责。

### 核心目录

| 目录 | 作用 |
| :--- | :--- |
| **`src/`** | **[核心]** 存放项目的所有源代码。这是我们 99% 的开发工作的所在地，其内部结构遵循《项目目录结构规范》。 |
| **`public/`** | **[静态资源]** 存放不会被构建流程处理的静态资源。例如 `favicon.ico` 或一些必须保持原始路径的第三方库。 |
| **`dist/`** | **[构建产物]** 执行 `npm run build` 后生成的目录，用于生产环境部署。**此目录不应被提交到 Git**。 |
| **`node_modules/`** | **[项目依赖]** 存放 `package.json` 中定义的所有项目依赖包。**此目录不应被提交到 Git**。 |
| **`docs/`** | **[项目文档]** 存放项目的设计文档、需求文档、会议记录等。 |
| **`common/`** | **[通用模块]** (待审查) 存放一些项目早期的通用模块，未来应逐步重构整合进 `src/utils` 或 `src/services`。 |

### 配置文件

| 文件 | 作用 |
| :--- | :--- |
| **`vite.config.ts`** | **[核心]** Vite 的主配置文件。定义了构建策略、开发服务器、代理、路径别名 (`@/`) 等关键信息。 |
| **`package.json`** | **[核心]** Node.js 项目的清单文件。定义了项目名称、版本、依赖项 (`dependencies`)、开发依赖项 (`devDependencies`) 和脚本 (`scripts`)。 |
| **`tsconfig.json`** | **[核心]** TypeScript 的主配置文件。定义了编译选项、文件包含范围等。 |
| **`index.html`** | **[应用入口]** Vite 项目的 HTML 入口文件，Vue 应用会挂载到这个文件的 `#app` 元素上。 |
| `.gitignore` | **[Git配置]** 告诉 Git 哪些文件或目录（如 `node_modules`, `dist`, `.env.local`）不需要被版本控制。 |
| `pnpm-lock.yaml` | **[依赖锁定]** (如果使用 pnpm) 锁定项目依赖的精确版本，确保团队成员安装的依赖版本完全一致。 |
| `package-lock.json`| **[依赖锁定]** (如果使用 npm) 功能同上，确保依赖一致性。 |
| `uno.config.ts` | **[CSS配置]** UnoCSS 的配置文件，一个原子化的 CSS 引擎。 |

### 自动化与持续集成 (CI/CD)

| 文件 | 作用 |
| :--- | :--- |
| **`Dockerfile`** | **[容器化]** 用于构建项目 Docker 镜像的指令文件。 |
| **`nginx.conf`** | **[部署配置]** Nginx 服务器的配置文件，常用于 Docker 容器内，负责反向代理和静态资源服务。 |
| **`deployment.yaml`** | **[K8s部署]** Kubernetes 的部署描述文件，用于在 K8s 集群中部署应用。 |
| `.gitlab-ci.yml` | **[CI/CD]** GitLab CI/CD 的流水线配置文件，定义了自动化测试、构建和部署的流程。 |

### 自动生成类型定义 (Vite 插件)

| 文件 | 作用 |
| :--- | :--- |
| `auto-imports.d.ts` | 由 `unplugin-auto-import` 插件自动生成，用于声明全局导入的 API (如 Vue 的 `ref`, `computed`) 的 TypeScript 类型。**不应手动修改**。 |
| `components.d.ts` | 由 `unplugin-vue-components` 插件自动生成，用于声明自动导入的组件的 TypeScript 类型。**不应手动修改**。 |

---

## 3. 开发规范

1.  **代码风格**: 遵循项目配置的 ESLint 和 Prettier 规则。
2.  **目录结构**: 所有新功能开发，请严格遵循 `src` 目录下的《项目目录结构规范》。
3.  **Git 提交**: 遵循 `feat:`, `fix:`, `docs:`, `style:`, `refactor:`, `test:`, `chore:` 的提交信息格式约定。
4.  **环境变量**: 本地开发需要私有配置时，请创建 `.env.local` 文件，此文件不应提交到 Git。
5.  **命名规范**: 遵循以下统一的命名约定：
    -   **目录 (Folders)**: 全部使用小写 `kebab-case` (短横线连接)。
        -   示例: `src/pages/schedule-manage`, `src/components/base-table`
    -   **组件文件 (`.vue`)**: 全部使用 `PascalCase` (大驼峰命名法)。如果组件是目录下的核心文件，文件名应与目录名保持一致。
        -   示例: `schedule-manage/ScheduleManage.vue`, `base-table/BaseTable.vue`
    -   **路由命名 (Router Name)**: 与组件文件名保持一致，使用 `PascalCase`。
        -   示例: `{ name: 'ScheduleManage', ... }`
    -   **脚本/工具文件 (`.ts`, `.js`)**: 全部使用小写 `kebab-case` (短横线连接)。
        -   示例: `src/utils/date-formatter.ts`, `src/hooks/use-table-data.ts`

---

*该文档旨在提供清晰的项目指引，帮助团队高效协作。*
