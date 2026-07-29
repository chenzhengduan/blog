# 单元 1：Agentic Engineering 基础概念与 Claude Code 最佳实践

> 定位：可参考 · 可操作 · 可落地

---

## 一、什么是 Agentic Engineering

### 核心概念

**Agentic Engineering** 是指构建能够自主规划、执行多步骤任务的 AI 系统的工程方法。与传统"一问一答"的 LLM 使用方式不同，Agentic 系统具备：

- **自主决策**：根据目标自行拆解任务、选择工具
- **多步执行**：完成需要多轮操作才能达成的复杂目标
- **环境感知**：读取文件、调用 API、执行代码、浏览网页
- **自我纠错**：观察执行结果，调整后续行动

### Agent 的基本结构

```
目标输入
   ↓
[规划层] → 任务分解 → 子任务列表
   ↓
[执行层] → 工具调用（文件/命令/API）
   ↓
[观察层] → 结果评估 → 是否达成目标？
   ↓（未达成则循环）
输出结果
```

### Agent vs 普通 LLM 调用

| 维度 | 普通 LLM | Agentic |
|------|----------|---------|
| 交互模式 | 单轮问答 | 多轮自主执行 |
| 工具使用 | 无或有限 | 丰富工具集 |
| 任务复杂度 | 简单 | 复杂、多步骤 |
| 人工干预 | 每步都需要 | 最小化干预 |
| 适用场景 | 问答、生成 | 编码、研究、自动化 |

---

## 二、Claude Code 是什么

Claude Code 是 Anthropic 官方的 AI 编程助手 CLI 工具，运行在终端中，能够：

- 读写项目文件
- 执行 shell 命令
- 理解整个代码库上下文
- 自主完成编码任务（修 bug、加功能、重构、写测试）

### 安装与启动

```bash
# 安装（需要 Node.js 18+）
npm install -g @anthropic-ai/claude-code

# 启动（在项目目录下）
claude

# 或直接执行一次性任务
claude "帮我找出所有未处理的 Promise rejection"
```

---

## 三、Claude Code 核心最佳实践

### 3.1 CLAUDE.md —— 项目记忆文件

在项目根目录创建 `CLAUDE.md`，Claude Code 每次启动都会自动读取。

**写什么进去：**

```markdown
# 项目说明
这是一个 Vue3 + TypeScript 的后台管理系统。

## 技术栈
- 前端：Vue 3, Vite, Element Plus
- 后端：Node.js, Express, MySQL

## 开发规范
- 组件用 Composition API，不用 Options API
- 所有接口调用放在 src/api/ 目录
- 禁止直接修改 node_modules

## 常用命令
- 启动开发：npm run dev
- 构建：npm run build
- 测试：npm run test
```

**效果：** 不用每次重复交代背景，Claude 直接按项目规范工作。

### 3.2 任务描述要具体

| 模糊（效果差） | 具体（效果好） |
|--------------|--------------|
| "优化一下代码" | "优化 src/utils/request.ts 中的错误处理，统一用 toast 提示，不要用 alert" |
| "加个功能" | "在用户列表页加分页，每页20条，用 Element Plus 的 el-pagination 组件" |
| "修复 bug" | "修复登录后跳转到 /dashboard 时偶发白屏的问题，报错在 console 里是 Cannot read properties of undefined" |

### 3.3 权限控制（--allowedTools）

Claude Code 默认会询问每个操作的权限。可以预先配置允许的操作：

```bash
# 允许读文件和运行测试，不允许写文件
claude --allowedTools "Read,Bash(npm test)"

# 完全自主模式（谨慎使用）
claude --dangerouslySkipPermissions
```

**项目级权限配置**（`.claude/settings.json`）：

```json
{
  "permissions": {
    "allow": [
      "Bash(npm run *)",
      "Bash(git status)",
      "Bash(git diff *)",
      "Read",
      "Edit",
      "Write"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(git push *)"
    ]
  }
}
```

### 3.4 使用 /commands 斜杠命令

```bash
/help          # 查看所有命令
/clear         # 清空对话上下文
/compact       # 压缩上下文（节省 token）
/cost          # 查看本次会话消耗
/review        # 代码审查当前改动
/init          # 生成 CLAUDE.md
```

### 3.5 多文件任务的正确姿势

对于跨多个文件的任务，先让 Claude 制定计划再执行：

```
你：先告诉我你打算怎么实现用户权限系统，不要写代码，只说方案
Claude：[给出方案]
你：方案可以，开始实现
Claude：[开始执行]
```

这样避免 Claude 走错方向后大量返工。

### 3.6 Git 工作流集成

```bash
# 让 Claude 在独立分支上工作
git checkout -b feature/ai-task
claude "实现购物车功能"

# 完成后审查
git diff main
claude /review

# 确认无误再合并
git checkout main && git merge feature/ai-task
```

---

## 四、Agentic 系统的关键设计原则

### 4.1 最小权限原则

Agent 只应拥有完成任务所需的最小权限。不要给 Agent 生产数据库的写权限去做只需要读的任务。

### 4.2 人在回路（Human-in-the-Loop）

```
低风险操作 → Agent 自主执行
中风险操作 → 执行前展示计划，人工确认
高风险操作 → 必须人工审批（删除数据、推送代码、发送消息）
```

### 4.3 可观测性

每个 Agent 操作都应该有日志：
- 做了什么决策
- 调用了哪些工具
- 输入输出是什么
- 耗时和 token 消耗

### 4.4 幂等性设计

Agent 可能因为网络、超时等原因重试。设计工具时要考虑重复调用的安全性：

```python
# 不好：重复调用会创建多条记录
def create_user(name):
    db.insert("users", {"name": name})

# 好：重复调用安全
def create_user(name):
    db.upsert("users", {"name": name}, conflict_key="name")
```

---

## 五、常见陷阱与规避

| 陷阱 | 现象 | 规避方法 |
|------|------|---------|
| 上下文丢失 | Claude 忘记之前的约定 | 写入 CLAUDE.md，用 /compact |
| 幻觉执行 | Claude 假装执行了但没有 | 验证文件实际变化，运行测试 |
| 权限过大 | Agent 误删重要文件 | 配置 deny 规则，重要操作前备份 |
| 任务漂移 | Claude 做了额外"优化" | 明确说"只做X，不要改其他地方" |
| 循环失败 | Agent 反复尝试同一错误方案 | 设置最大重试次数，失败后人工介入 |

---

## 六、快速上手 Checklist

- [ ] 安装 Claude Code：`npm install -g @anthropic-ai/claude-code`
- [ ] 在项目根目录创建 `CLAUDE.md`，写入技术栈和规范
- [ ] 创建 `.claude/settings.json`，配置允许/禁止的操作
- [ ] 第一次使用先用 `/init` 让 Claude 自动生成 CLAUDE.md 草稿
- [ ] 复杂任务先要方案，确认后再执行
- [ ] 重要操作前先 `git commit`，保留回退点
- [ ] 用 `/review` 在提交前做代码审查

---

## 七、参考资源

- [Claude Code 官方文档](https://docs.anthropic.com/claude-code)
- [Anthropic Cookbook - Agents](https://github.com/anthropics/anthropic-cookbook)
- [Claude Code GitHub Issues](https://github.com/anthropics/claude-code/issues)
