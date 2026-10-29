---
name: chuti
description: 当需要为中小学语文、数学、英语课程出题时，使用此 Skill。支持 1–9 年级人教版知识范围，可生成单题或整卷结构。
---

# 出题 Skill（chuti）

## 功能说明

本 Skill 为中小学 AI 出题助手提供知识边界与出题规则约束，覆盖语文、数学、英语三门学科，1–9 年级人教版课程标准。

**当前仅支持语文、数学、英语三门学科。** 接收到其他学科请求时，拒绝并说明支持范围。

---

## 学科与年级范围

| 学科 | 年级范围 | 知识参考文件 |
|------|----------|------------|
| 数学 | 1–9 年级 | `references/math.md` |
| 语文 | 1–9 年级 | `references/chinese.md` |
| 英语 | 1–9 年级 | `references/english.md` |

| 年级 | 学段 |
|------|------|
| 1–2 | 小学低年级 |
| 3–4 | 小学中年级 |
| 5–6 | 小学高年级 |
| 7–9 | 初中 |

---

## 出题工作流程（按顺序执行，不可跳步）

### 第一步：加载学科知识

根据题目学科和年级加载对应的 references 文件，查阅知识点范围、典型题型、难度边界，以及公式写法规范：

**数学**（根据年级选择对应文件，**只加载一个**，含知识点 + LaTeX 公式规范）：

| 年级 | 加载文件 |
|------|---------|
| 1 年级 | `references/math-grade-1.md` |
| 2 年级 | `references/math-grade-2.md` |
| 3 年级 | `references/math-grade-3.md` |
| 4 年级 | `references/math-grade-4.md` |
| 5 年级 | `references/math-grade-5.md` |
| 6 年级 | `references/math-grade-6.md` |
| 7 年级 | `references/math-grade-7.md` |
| 8 年级 | `references/math-grade-8.md` |
| 9 年级 | `references/math-grade-9.md` |

**语文**（根据年级选择对应文件，**只加载一个**）：

| 年级 | 加载文件 |
|------|---------|
| 1 年级 | `references/chinese-grade-1.md` |
| 2 年级 | `references/chinese-grade-2.md` |
| 3 年级 | `references/chinese-grade-3.md` |
| 4 年级 | `references/chinese-grade-4.md` |
| 5 年级 | `references/chinese-grade-5.md` |
| 6 年级 | `references/chinese-grade-6.md` |
| 7 年级 | `references/chinese-grade-7.md` |
| 8 年级 | `references/chinese-grade-8.md` |
| 9 年级 | `references/chinese-grade-9.md` |

**英语**（根据年级选择对应文件，**只加载一个**）：

| 年级 | 加载文件 |
|------|---------|
| 1 年级 | `references/english-grade-1.md` |
| 2 年级 | `references/english-grade-2.md` |
| 3 年级 | `references/english-grade-3.md` |
| 4 年级 | `references/english-grade-4.md` |
| 5 年级 | `references/english-grade-5.md` |
| 6 年级 | `references/english-grade-6.md` |
| 7 年级 | `references/english-grade-7.md` |
| 8 年级 | `references/english-grade-8.md` |
| 9 年级 | `references/english-grade-9.md` |

### 第二步：配图决策（仅数学题，写题干之前必须完成）

语文、英语题跳过此步骤，`diagrams` 字段始终为 null。

数学题在动笔写题干之前，先做一次二选一判断：

**→ 选择「配图模式」的条件（满足任一即选此模式）**：
- 多点空间位置关系（需辅助线，如高线、中线、角平分线）
- 图形变换（折叠、旋转、翻折）
- 多边形与圆的组合（切线、内接多边形）
- 需要学生在图上作图

**→ 选择「纯文字模式」的条件（以下情况均用纯文字）**：
- 边长、角度已用数字明确给出（如 ∠C=90°，AB=3，AD⊥BC 垂足为 D，BD=5）
- 平行线与截线的角度关系
- 简单位置关系（M 是 AB 中点、点 D 在 BC 上）

**两种模式的写法完全不同，决策做好了再写 content**：

| 模式 | content 写法 | diagrams 字段 | 额外操作 |
|------|------------|---------------|---------|
| **配图模式** | 以"如图，…"起笔，引用图中标注 | 必须输出 TikZ 代码 | **写 tikzCode 之前**（不是在整卷开始时）加载 `references/geometry-tikz.md` |
| **纯文字模式** | 直接用文字描述几何关系，**禁止出现"如图""下图"等词** | null | 无 |

> **配图模式**下，`diagrams` 必须包含完整 TikZ 代码（见下方 diagrams 格式）。  
> **纯文字模式**下，content 里出现"如图"类词语即视为违规，必须改写。  
> 禁止在两种模式之间"打补丁"：不能在 content 里写"如图"的同时把 diagrams 设为 null，无论加什么括号注释都不合法。

> ⚠️ **geometry-tikz.md 的加载时机**：只在确认某道题要进入配图模式、即将动手写 tikzCode 时才加载一次。不要在整卷/整题开始阶段提前加载——对于大多数题目来说它完全用不到，提前加载会无谓占满 context，拖慢所有后续推理。

**diagrams 字段输出格式**（配图模式使用）：
```json
"diagrams": [
  {
    "sortOrder": 1,
    "tikzCode": "\\tkzDefPoint(0,0){A}\n\\tkzDefPoint(4,0){B}\n..."
  }
]
```
- `sortOrder` 从 1 开始，多张图按顺序编号（如折叠题：sortOrder 1=折叠前，2=折叠后）
- `tikzCode` 只含 `tikzpicture` 环境内的内容，后端自动包裹外层模板
- 禁止使用的命令：`\input`、`\include`、`\write18`、`\immediate`、`\openout`、`\closeout`
- TikZ 命令写法详见 `references/geometry-tikz.md`（配图模式下必须加载）

### 第三步：生成题目

遵守以下约束逐题生成，生成完成后对照「生成后自查清单」检查：

**1. 标准题型与年级合法组合**

| 学科 | 年级 | 可用标准题型 |
|------|------|---------|
| 数学 | 1–4 年级 | 选择题、填空题、判断题、计算题、应用题 |
| 数学 | 5–6 年级 | 选择题、填空题、判断题、计算题、应用题 |
| 数学 | 7–9 年级 | 选择题、填空题、计算题、解答题、应用题 |
| 语文 | 1–6 年级 | 选择题、填空题、判断题、解答题、阅读理解题、写作题 |
| 语文 | 7–9 年级 | 选择题、填空题、解答题、阅读理解题、写作题 |
| 英语 | 1–6 年级 | 选择题、填空题、判断题、解答题、阅读理解题、写作题 |
| 英语 | 7–9 年级 | 选择题、填空题、解答题、阅读理解题、写作题 |

custom 按给定定义出题，知识范围仍须符合学科与年级。

**2. 内容安全**：不出政治敏感、歧视、暴力等内容；素材积极健康。

**3. 知识边界**：知识点必须在请求年级对应范围内，不超纲、不低龄化。

**4. 答案质量**：答案准确唯一，解析清晰；应用题须分步骤说明解题过程。

**5. 填空题填空线**（全学科通用）：题干用 `$\underline{\hspace{2cm}}$` 表示填空位置，**禁止 ASCII 下划线 `___`**，也禁止用 `\quad` 堆叠方式。`\hspace` 宽度按答案长度调整。

| 答案 | 字符数 | 填空线写法 |
|------|--------|----------|
| `["5"]` | 1-2 | `$\underline{\hspace{1cm}}$` |
| `["心理"]` | 3-4 | `$\underline{\hspace{1.5cm}}$` |
| `["100"]` | 5-8 | `$\underline{\hspace{2cm}}$` |
| 长答案 | 8+ | `$\underline{\hspace{3cm}}$` |

多空题：每个空独立包裹，`\quad` 数量按各空答案长度分别调整。

**6. 数学公式写法**：一律用 LaTeX 行内公式 `$...$`，禁止 Unicode 数学符号（√ ² ÷ π ° ∠ △ ≥ ≤ ±）。详细对照表见 `references/math.md` 末尾的「LaTeX 公式写法规范」章节。

⚠️ 填空题 LaTeX 边界：闭合 `$` 前不能有空格，否则渲染器原样显示源码。

---

## 结构化输出格式

### 单题（Question）

```json
{
  "type": "题型英文 key",
  "content": "题干文本",
  "options": [{"label": "A", "content": "选项一"}, ...],
  "answer": ["标准答案"],
  "score": 3,
  "explanation": "解析说明",
  "source": "AI",
  "difficulty": "medium"
}
```

**教学元数据字段**（每道题均需输出）：
- `source`：来源，AI 生成场景固定输出 `"AI"`。
- `difficulty`：难度，根据题目内容判断，取值 `"easy"`（基础）/ `"medium"`（中等）/ `"hard"`（拔高）。子题独立判断，可与父题不同。

**题型 key 对照表**（`type` 字段必须用英文 key）：

| 中文题型名 | type 值 |
|-----------|--------|
| 单选题 | `choice` |
| 多选题 | `multi` |
| 判断题 | `judge` |
| 填空题 | `fill` |
| 简答题 | `short` |
| 应用题 | `apply` |
| 计算题 | `calculation` |
| 解答题 | `solution` |
| 阅读理解题 | `reading` |
| 写作题 | `writing` |
| 自定义题型 | `custom` |

**自定义题型字段**：
- type/questionType 使用 custom，名称填 customQuestionTypeName，定义填 customQuestionTypeDescription；已给出的名称、定义原文保留，缺失时留空，标准题型两字段为空。
- 父题、子题分别填写自身题型、名称和定义，不继承父题。
- 用当前输出结构中的题干、选项、子题等字段表达定义，不增加字段或答题控件。

**各题型 answer 格式**：
- `choice`：`["A"]`
- `multi`：`["A", "C"]`（字母顺序）
- `judge`：`["TRUE"]` 或 `["FALSE"]`；必须提供 options；题干末尾加 `（　　）`（两个全角空格）
- `fill` 单空：`["答案"]`；多空：`["答案1", "答案2"]`
- `apply`：`["解题步骤...\n最终答案"]`（分步说明）
- `calculation`：`["分步计算过程...", "最终答案"]`
- `solution`：`["分步解答过程...", "结论"]`
- `reading`：content 仅放阅读材料，各小题进 `subQuestions`（见下方复合题结构）
- `writing`：answer 为 `["参考范文或写作提纲"]`
- `short`：answer 为 `["要点一", "要点二"]`（按要点拆分）

**options 字段规则**：
- `choice`/`multi`：4 个选项，`{"label":"A","content":"选项内容"}`；label 唯一；字母不拼进 content
- `judge`：`[{"label":"TRUE","content":"正确"},{"label":"FALSE","content":"错误"}]`
- 其他标准题型：`options` 为 null；custom 按定义填写选项，无需选项时为 null

### 整卷（Exam）

```json
{
  "title": "卷名",
  "subject": "学科",
  "grade": 5,
  "totalScore": 100,
  "durationMinutes": 80,
  "sections": [
    {
      "name": "一、选择题",
      "questionType": "choice",
      "scoreNote": "每题 3 分，共 15 分",
      "questions": [...]
    }
  ]
}
```

**整卷要求**：
- sections 数量按题型分布决定，每个 section 至少 2 道题
- 所有题目 score 之和必须等于 totalScore
- 分值合理：选择题 2–4 分，填空题每空 1–3 分，应用题 5–15 分
- 知识点覆盖面广，不集中在单一知识点
- 每个 section 的 scoreNote 格式：「每题 X 分，共 Y 分」

---

## 复合题结构：多空填空与题组

### 多空填空题 —— `blanks` 字段

填空题有多个空时必须输出 `blanks` 数组：
- `blanks[i].order` 从 1 开始，与 `answer[order-1]` 一一对应
- `blanks[i].score` 各空分值，之和应等于整题 `score`
- 单空填空题可不输出 `blanks`；**多空填空题必须输出**

### 题组 —— `subQuestions` 字段

`reading` 和含 (1)(2)(3) 小问的 `solution` 必须使用 `subQuestions`：
- 父题 `content` 只放共享材料/情境，**禁止**把小题塞进父题 content
- `subQuestions[i].index` 从 1 开始，渲染为 (1)(2)(3)
- 各子题按自身题型提供 `options`/`answer`/`explanation`/`score`
- **子题分值之和必须等于父题 `score`**
- 父题 `answer` 字段留空（`[]`），答案在各子题内
- 子题各自独立输出 `source`/`difficulty`（不继承父题，允许为空）

---

## 生成后自查清单

每道题生成完成后必查：

- [ ] **配图（数学）**：决策是配图模式 → content 有"如图"？diagrams 有 TikZ 代码？
- [ ] **纯文字（数学）**：决策是纯文字模式 → content 里没有"如图""下图"等词？diagrams = null？
- [ ] **填空线**：填空位置用的是 `$\underline{\hspace{...}}$`，不是 `___` 也不是 `\quad` 堆叠？
- [ ] **LaTeX**：数学符号全用 LaTeX？没有 Unicode 符号（×、÷、√、π 等）？
- [ ] **多空填空**：多个空 → `blanks` 已输出？`order` 与 `answer` 下标对齐？
- [ ] **题组**：reading / 含(1)(2)(3)的 solution → 用了 `subQuestions`？小题没有塞进父题 content？
- [ ] **分值**：整卷时所有题目 score 之和 = totalScore？
- [ ] **教学元数据**：每道题（含子题）是否输出了 `source`/`difficulty`？难度取值是否为 easy/medium/hard 之一？
