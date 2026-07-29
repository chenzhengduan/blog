# 单元 2：SDIE 体系概览与工具链标准导入

> 定位：可参考 · 可操作 · 可落地

---

## 一、SDIE 是什么

**SDIE**（Software Development with Intelligence Engineering）是一套将 AI 能力系统性融入软件开发全流程的工程体系。它不是单一工具，而是一套**方法论 + 工具链 + 规范**的组合。

### 核心理念

```
传统开发：人写代码 → 人审查 → 人测试 → 人部署
SDIE 开发：人定方向 → AI 执行 → 人审查关键节点 → AI 辅助测试/部署
```

SDIE 的目标是让开发者从**执行者**转变为**决策者和审查者**，把重复性、机械性的工作交给 AI。

### SDIE 四个阶段

```
┌─────────────────────────────────────────────────────┐
│  Phase 1: Spec（规约）  →  需求 → 设计文档 → 验收标准  │
│  Phase 2: Design（设计）→  架构 → 接口定义 → 数据模型  │
│  Phase 3: Implement（实现）→  编码 → 测试 → 代码审查  │
│  Phase 4: Evolve（演进）→  重构 → 优化 → 知识沉淀     │
└─────────────────────────────────────────────────────┘
```

---

## 二、工具链全景

### 2.1 标准工具链组成

| 层次 | 工具 | 职责 |
|------|------|------|
| AI 核心 | Claude Code | 代码生成、理解、重构 |
| 版本控制 | Git + GitHub/GitLab | 代码管理、协作 |
| 规约管理 | Markdown + CLAUDE.md | 项目上下文、规范 |
| 任务追踪 | GitHub Issues / Linear | 需求和任务管理 |
| CI/CD | GitHub Actions | 自动化测试和部署 |
| 代码质量 | ESLint / Prettier / TypeScript | 静态检查 |
| 测试 | Jest / Vitest / Playwright | 单元/集成/E2E 测试 |

### 2.2 工具链导入顺序（推荐）

```
第1步：建立 Git 仓库 + 分支策略
第2步：配置 CLAUDE.md（项目记忆）
第3步：配置 .claude/settings.json（权限规则）
第4步：接入 CI（至少跑 lint + test）
第5步：建立 PR 模板和 Review 流程
第6步：逐步引入 AI 辅助各阶段
```

---

## 三、CLAUDE.md 标准模板

这是 SDIE 体系中最重要的单个文件，是 AI 理解项目的"说明书"。

```markdown
# 项目名称

## 项目概述
[一句话描述项目是什么、给谁用的]

## 技术栈
- 前端：[框架、版本]
- 后端：[语言、框架]
- 数据库：[类型、版本]
- 部署：[平台]

## 目录结构
src/
  api/        # 接口层，所有 HTTP 请求
  components/ # 公共组件
  pages/      # 页面组件
  stores/     # 状态管理
  utils/      # 工具函数

## 开发规范
- [规范1]
- [规范2]

## 禁止事项
- 不要修改 xxx 文件（原因）
- 不要使用 xxx 库（原因）

## 常用命令
- 开发：npm run dev
- 测试：npm run test
- 构建：npm run build

## 当前重点
[当前阶段在做什么，有什么特殊注意事项]
```

---

## 四、.claude/settings.json 标准配置

```json
{
  "permissions": {
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Bash(git status)",
      "Bash(git diff *)",
      "Bash(git log *)",
      "Bash(git add *)",
      "Bash(git commit *)",
      "Bash(npm run dev)",
      "Bash(npm run build)",
      "Bash(npm run test)",
      "Bash(npm run lint)"
    ],
    "deny": [
      "Bash(git push --force*)",
      "Bash(rm -rf *)",
      "Bash(DROP *)",
      "Bash(git reset --hard *)"
    ]
  }
}
```

**原则：**
- `allow` 列表：日常开发中频繁使用、低风险的操作
- `deny` 列表：不可逆、影响范围大的危险操作
- 未在列表中的操作：每次执行前询问用户

---

## 五、Git 分支策略（SDIE 推荐）

```
main          ← 生产环境，只接受 PR 合并
  └── develop ← 开发主线
        ├── feature/xxx  ← 新功能（AI 在这里工作）
        ├── fix/xxx      ← Bug 修复
        └── refactor/xxx ← 重构
```

### AI 工作的标准流程

```bash
# 1. 从 develop 创建任务分支
git checkout develop
git checkout -b feature/user-auth

# 2. 在分支上让 Claude 工作
claude "实现用户登录功能，包括 JWT token 管理"

# 3. 审查变更
git diff develop
claude /review

# 4. 运行测试
npm run test

# 5. 提交并创建 PR
git add src/
git commit -m "feat: 实现用户登录功能"
git push origin feature/user-auth
# 然后在 GitHub 创建 PR
```

---

## 六、CI 配置标准（GitHub Actions）

```yaml
# .github/workflows/ci.yml
name: CI

on:
  pull_request:
    branches: [main, develop]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Lint
        run: npm run lint
      
      - name: Type check
        run: npm run type-check
      
      - name: Test
        run: npm run test -- --coverage
      
      - name: Build
        run: npm run build
```

**为什么 CI 是 SDIE 的必要组件：**
AI 生成的代码可能通过语法检查但有逻辑问题。CI 是最后一道自动化防线，确保 AI 的输出不会破坏已有功能。

---

## 七、PR 模板标准

创建 `.github/pull_request_template.md`：

```markdown
## 变更说明
[描述这个 PR 做了什么]

## 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 重构
- [ ] 文档更新

## 测试情况
- [ ] 已添加/更新单元测试
- [ ] 已在本地测试通过
- [ ] CI 全部通过

## AI 辅助说明
- [ ] 此 PR 包含 AI 生成的代码
- 已审查的关键逻辑：[列出]
- 需要重点关注的地方：[列出]

## 截图（如有 UI 变更）
```

---

## 八、工具链健康度检查

定期（每周/每迭代）检查以下指标：

| 指标 | 健康标准 | 检查方式 |
|------|---------|---------|
| CI 通过率 | > 95% | GitHub Actions 统计 |
| 测试覆盖率 | > 70% | Coverage 报告 |
| PR 平均审查时间 | < 24h | GitHub Insights |
| CLAUDE.md 更新频率 | 每次架构变更后 | git log CLAUDE.md |
| 未解决的 lint 警告 | 0 | npm run lint |

---

## 九、团队协作规范

### AI 使用透明度
- PR 描述中注明哪些代码由 AI 生成
- 关键业务逻辑必须人工审查，不能直接合并 AI 输出
- 安全相关代码（认证、权限、加密）必须专人审查

### 知识沉淀
- AI 解决的有价值的问题，记录到 CLAUDE.md 或 Wiki
- 发现 AI 的错误模式，更新 CLAUDE.md 的"禁止事项"
- 定期回顾 AI 辅助效果，调整工作流

---

## 十、快速导入 Checklist

- [ ] 创建 `CLAUDE.md`（用 `claude /init` 生成草稿后人工完善）
- [ ] 创建 `.claude/settings.json`（复制上方模板，按项目调整）
- [ ] 配置 Git 分支保护规则（main 分支禁止直接 push）
- [ ] 添加 CI 配置（至少包含 lint + test）
- [ ] 创建 PR 模板
- [ ] 团队对齐 AI 使用规范（哪些可以 AI 做，哪些必须人工）
- [ ] 建立 CLAUDE.md 的更新机制（谁负责、什么时候更新）
