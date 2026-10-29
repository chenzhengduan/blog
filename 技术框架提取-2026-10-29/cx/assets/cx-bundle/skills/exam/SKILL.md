---
name: exam
description: 为教师生成、查询和整卷变式试卷，并查询已有题目。当用户要出卷、查看试卷、从已有试卷或试卷图片生成变式卷时使用。成功后展示查看链接，不复述整卷题目。
---

# 出卷、查询与整卷变式（exam）

## 场景

本技能覆盖三类场景：

1. **生成试卷**：用户要用自然语言生成一份试卷（测评），至少明确学科与年级。
2. **查询已有数据**：用户想查看已有试卷列表、查询某份试卷的详情、或查询已有题目列表。
3. **整卷变式**：用户想基于已有试卷或按页上传的试卷图片生成一份新变式卷。

## 工具总览

| 工具 | 用途 | 类型 |
|------|------|------|
| `generate_exam` | 生成试卷 | 流式（SSE） |
| `list_exams` | 分页查询试卷列表 | 同步 JSON |
| `get_exam_detail` | 查询试卷详情摘要 | 同步 JSON |
| `list_questions` | 分页查询题目列表 | 同步 JSON |
| `generate_variant_exam_from_exam` | 从已有试卷生成整卷变式 | 流式（SSE） |
| `generate_variant_exam_from_images` | 从试卷图片 URL 生成整卷变式 | 流式（SSE） |

---

## 一、生成试卷

### 必收集

| 项 | 说明 |
|----|------|
| 学科 | 映射为工具枚举：`math`（数学）、`chinese`（语文）、`english`（英语） |
| 年级 | 1–9（小学一年级至初三） |

未收齐前先追问，不要猜测年级或学科后直接调用工具。

### 可选

- 知识点 / 考察范围 → `topic`
- 试卷标题 → `title`
- 补充要求（如侧重应用题）→ `extraRequirements`
- 难度：简单/中等/困难 → `easy` / `medium` / `hard`
- 满分 → `totalScore`（默认由服务端处理）
- 题型分布 → `questionDistribution`（不传则后端按学科使用默认分布）

### 成功后

向用户展示：标题、学科年级、试卷 ID、**查看试卷链接**。**不要**把完整题目正文复述进对话。

### A2UI 卡片交互（必须使用 CX 自定义卡片，禁止用 basic 拼）

生成试卷过程**必须**使用 `cx-exam-cards` catalog 的自定义卡片让教师确认参数，**禁止**用 basic catalog（TextField/Button/Text 等）自行拼装，**也禁止用纯文本追问**。CX 卡片提供学科选择器、年级选择器、题型分布编辑器等专用交互，体验远优于 basic 文本输入。

**关键规则：当学科或年级信息不足时，不要用纯文本追问，直接调用 `render_a2ui` 下发 `ExamScopeCard`（catalogId: `cx-exam-cards`）让教师选择。**

三种卡片及其下发方式：

1. **范围确认卡（ExamScopeCard）**——信息不足（缺学科或年级）时下发：
   - `createSurface({ surfaceId: "exam-scope", catalogId: "cx-exam-cards" })`
   - 组件名：`ExamScopeCard`
   - dataModel 绑定 `subject`（math/chinese/english）、`grade`（整数 1-9）
   - action.event.name = `confirm_scope`
   - 教师通过学科/年级选择器确认后恢复运行，进入蓝图卡流程

2. **出卷蓝图卡（ExamBlueprintCard）**——出卷前下发，预填建议参数供教师调整：
   - `createSurface({ surfaceId: "exam-blueprint", catalogId: "cx-exam-cards" })`
   - 组件名：`ExamBlueprintCard`
   - dataModel 绑定：`subject`、`grade`（只读）、`difficulty`、`totalScore`、`topic`、`title`、`extraRequirements`、`questionDistribution`（可空，走默认分布）
   - action.event.name = `generate_exam`；action.event.context 与 `generate_exam` 工具入参同构，恢复后直接透传调用工具
   - 教师通过难度按钮、总分步进器、题型分布编辑器调整后提交，恢复运行以提交参数调用 `generate_exam` 工具

3. **出卷结果卡（ExamResultCard）**——出卷成功后下发：
   - `createSurface({ surfaceId: "exam-result-{examId}", catalogId: "cx-exam-cards" })`
   - 组件名：`ExamResultCard`
   - dataModel 绑定：`examId`、`title`、`subject`、`grade`、`totalScore`、`examUrl`
   - 「查看试卷」按钮纯前端跳转 `examUrl`，不上行
   - 「生成变式卷」按钮 action.event.name = `variant_exam`，context 含 `examId`，恢复后调用变式工具
   - **不含题目正文**

卡片下发后流会挂起，等待教师提交；提交后恢复运行，不要新发一轮普通问答。

---

## 二、查询试卷列表（list_exams）

### 场景

用户想查看已有试卷、按条件筛选试卷。

### 可选过滤条件

| 项 | 说明 |
|----|------|
| 学科 | `math` / `chinese` / `english` |
| 年级 | 1–9 |
| 试卷 ID | 精确查询某份试卷 |
| 操作人 | 按操作人标识过滤 |
| 分页 | `page` 默认 1，`size` 默认 5，最大 20 |

### 返回

每条试卷含标题/学科/年级/满分/题量/难度/作答时长和**试卷查看链接**。如有更多结果，告知用户可翻页（增大 page 值）。

---

## 三、查询试卷详情（get_exam_detail）

### 场景

用户通过试卷链接或试卷 ID 想了解某份试卷的概况。

### 必收集

| 项 | 说明 |
|----|------|
| 试卷链接 | `examUrl`，格式同生成试卷返回的查看链接 |

如果用户只给了试卷 ID，可用 `list_exams` 的 `examId` 参数查。

### 返回

试卷摘要（标题/学科/年级/满分/难度/题量/作答时长/操作人）和**试卷查看链接**。**不含题目内容**——如用户想看题目，引导其点击链接在前端查看。

---

## 四、查询题目列表（list_questions）

### 场景

用户想查看已有题目、按条件筛选题目。

### 可选过滤条件

| 项 | 说明 |
|----|------|
| 题干关键词 | `keyword`，模糊匹配 |
| 题型 | `type`：choice / multi / judge / fill / short / apply / calculation / solution / reading / writing / custom |
| 学科 | `subject`：math / chinese / english |
| 年级 | `grade`：1-9 |
| 知识点 | `knowledgePoint`，知识点名称 |
| 难度 | `difficulty`：easy / medium / hard |
| 来源 | `source`：AI / PHOTO / VARIANT_QUESTION |
| 创建时间 | `createdFrom` / `createdTo`，格式 yyyy-MM-dd |
| 分页 | `page` 默认 1，`size` 默认 5，最大 20 |

### 返回

每条题目含题干摘要/题型/学科/年级/难度/知识点/来源，**不含完整题干和答案**。如有更多结果，告知用户可翻页。

---

## 能力边界（必须遵守）

## 五、整卷变式

### 从已有试卷生成

- 用户必须明确源试卷，优先使用查询结果中的试卷 ID。
- 难度、标题和补充要求可选；不要擅自改变学科或年级。
- 这是写操作，会生成并保存一份新试卷；用户明确提出生成变式卷即可视为授权。

### 从图片生成

- 至少需要 1 张试卷图片 URL，最多 10 张。
- 多张图片必须按原试卷页码顺序传递，均视为同一份试卷。
- 图片 URL 必须来自当前会话附件或用户明确提供的可信 HTTPS 地址。

### 成功后

只展示新试卷摘要和查看链接，不在对话中复述整卷题目。

---

本技能**不支持**以下能力，须明确说明边界：

- 单题变式（应使用单题变式技能）
- 试卷内单题重新生成或替换
- 批改 / 阅卷
- 导出 Word/PDF
- 在线答题 / 免登分享链

禁止假装已执行，禁止调用未绑定的其他工具冒充完成。

## 成功回复示例要点

### 生成试卷

1. 简要确认已生成
2. 给出查看链接（来自工具结果中的 URL）
3. 提醒需在 CX 前端登录后打开详情（若适用）

### 查询列表

1. 简要说明查询条件与结果数量
2. 列出当前页每条摘要（标题/学科年级等关键字段）
3. 如有更多结果，提示可翻页

### 查询试卷详情

1. 简要展示试卷概况
2. 给出查看链接
