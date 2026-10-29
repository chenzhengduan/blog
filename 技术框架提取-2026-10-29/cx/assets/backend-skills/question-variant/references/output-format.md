# 数学公式输出规范（output-format）

本文件规定数学题各字段的 LaTeX 写法，适用于 API JSON 响应中的
`content`、`options`、`answer`、`explanation` 字段。

---

## 核心规则：LaTeX 行内公式

数学题中所有公式、运算符、特殊符号**一律使用 LaTeX 行内公式**：

```
$公式内容$
```

**禁用** Unicode 数学符号：`× ÷ ² ³ √ π ° ∠ △ ≥ ≤ ± ≠ ≈ ∞ ∥ ⊥`

**填空题填空线**：用 LaTeX `$\underline{\hspace{2cm}}$` 表示，禁止 ASCII 下划线 `___`，也禁止用 `\quad` 堆叠方式。
`\quad` 数量按答案字符数 + 1 调整（最少 2 个），详见 SKILL.md 核心约束第 6 条。

---

## 常用符号 LaTeX 写法对照表

| 含义 | 禁用写法 | 正确 LaTeX | 示例 |
|---|---|---|---|
| 乘号 | × | `\times` | `$3 \times 4$` |
| 除号 | ÷ | `\div` | `$12 \div 4$` |
| 分数 | 3/4 | `\frac{3}{4}` | `$\frac{3}{4}$` |
| 平方 | x² | `x^2` | `$x^2$` |
| 立方 | x³ | `x^3` | `$x^3$` |
| 下标 | x₁ | `x_1` | `$x_1$` |
| 根号 | √2 | `\sqrt{2}` | `$\sqrt{2}$` |
| n次根 | ³√8 | `\sqrt[3]{8}` | `$\sqrt[3]{8}$` |
| 圆周率 | π | `\pi` | `$\pi r^2$` |
| 角度 | 30° | `30^\circ` | `$30^\circ$` |
| 角符号 | ∠AOB | `\angle AOB` | `$\angle AOB$` |
| 三角形 | △ABC | `\triangle ABC` | `$\triangle ABC$` |
| 大于等于 | ≥ | `\geq` | `$x \geq 0$` |
| 小于等于 | ≤ | `\leq` | `$x \leq 5$` |
| 不等于 | ≠ | `\neq` | `$a \neq b$` |
| 约等于 | ≈ | `\approx` | `$\pi \approx 3.14$` |
| 正负号 | ± | `\pm` | `$x = \pm 3$` |
| 绝对值 | \|x\| | `|x|` 或 `\|x\|` | `$|x - 1|$` |
| 因为 | ∵ | `\because` | `$\because x > 0$` |
| 所以 | ∴ | `\therefore` | `$\therefore x = 2$` |
| 平行 | ∥ | `\parallel` | `$AB \parallel CD$` |
| 垂直 | ⊥ | `\perp` | `$AB \perp CD$` |
| 判别式 | Δ | `\Delta` | `$\Delta = b^2 - 4ac$` |
| sin/cos/tan | sin30° | `\sin 30^\circ` | `$\sin 30^\circ = 0.5$` |

---

## 各字段写法示例

### content（题干）

```
计算 $\frac{3}{4} \div \frac{1}{2}$ 的结果是（  ）
```

```
解方程：$2x - 5 = 9$
```

```
已知 $\triangle ABC$ 中，$\angle A = 90^\circ$，$AB = 3$，$AC = 4$，求 $BC$。
```

### options（选择题选项）

```json
["A. $\\frac{3}{2}$", "B. $\\frac{1}{2}$", "C. $3$", "D. $\\frac{3}{8}$"]
```

注：JSON 字符串内反斜杠需转义为 `\\`，即 `\frac` 写成 `\\frac`。

### answer（答案）

```
$\frac{3}{2}$
```

```
$x = 7$
```

### explanation（解析）

```
$\frac{3}{4} \div \frac{1}{2} = \frac{3}{4} \times 2 = \frac{3}{2}$。
除以一个数等于乘以这个数的倒数，$\frac{1}{2}$ 的倒数是 $2$。
```

---

## ⚠️ 填空题边界陷阱

LaTeX 行内公式的结束 `$` 前**不能有空格**，否则渲染器会显示源码。

**错误写法**（结束 `$` 前有空格）：
```
$3x - 5 = \underline{\hspace{1cm}} $
```

**正确写法 A**（等号紧贴结束 `$`）：
```
$3x - 5 = \underline{\hspace{1cm}}$
```

**正确写法 B**（求值动作放公式外）：
```
计算 $3x - 5$（当 $x = 2$ 时），结果是 $\underline{\hspace{1cm}}$
```

> 填空线统一用 `$\underline{\hspace{...}}$`，`\hspace` 宽度按答案长度调整（1-2字符用1cm，3-4字符用1.5cm，5-8字符用2cm，8+字符用3cm），详见 SKILL.md 核心约束第 6 条。

---

## 自检清单（出完数学题必查）

- [ ] 题干中有没有 `× ÷ ² ³ √ π ° ∠ △ ≥ ≤ ±` 这些 Unicode 符号？有就改为 LaTeX
- [ ] 选项中的数学符号是否也用了 LaTeX？
- [ ] 解析中的计算步骤是否全部用 LaTeX？
- [ ] 填空题：结束 `$` 前有没有空格？（搜 `= $ ` 模式）
