# 几何配图 TikZ 参考（geometry-tikz）

本文件规定几何题配图使用 TikZ / tkz-euclide 的写法约束，适用于 `diagrams[].tikzCode` 字段。

---

## `.tex` 文件模板（后端自动包裹，无需在 tikzCode 中声明）

```latex
\documentclass[border=5pt]{standalone}
\usepackage{tkz-euclide}
\usepackage{amsmath}
\begin{document}
\begin{tikzpicture}
{你的 tikzCode 放在这里}
\end{tikzpicture}
\end{document}
```

**因此 tikzCode 只需提供 tikzpicture 环境内的内容，不要包含 documentclass 或 begin{document}。**

---

## 允许的命令白名单

### 点定义与绘制
| 命令 | 说明 | 示例 |
|------|------|------|
| `\tkzDefPoint(x,y){Label}` | 定义点（坐标系） | `\tkzDefPoint(0,0){A}` |
| `\tkzDefMidPoint(A,B)` | 定义中点，结果为 `tkzPointResult` | `\tkzDefMidPoint(A,B) \tkzGetPoint{M}` |
| `\tkzGetPoint{Label}` | 获取计算结果点 | 见上 |
| `\tkzDrawPoint(A)` | 绘制实心点 | `\tkzDrawPoint(A)` |
| `\tkzDrawPoints(A,B,C)` | 批量绘制点 | `\tkzDrawPoints(A,B,C)` |

### 线段与直线
| 命令 | 说明 | 示例 |
|------|------|------|
| `\tkzDrawSegment(A,B)` | 绘制线段 | `\tkzDrawSegment(A,B)` |
| `\tkzDrawSegments(A,B C,D)` | 批量绘制线段 | `\tkzDrawSegments(A,B B,C C,A)` |
| `\tkzDrawLine(A,B)` | 绘制直线（两端延伸） | `\tkzDrawLine(A,B)` |

### 多边形
| 命令 | 说明 | 示例 |
|------|------|------|
| `\tkzDrawPolygon(A,B,C)` | 绘制多边形 | `\tkzDrawPolygon(A,B,C,D)` |

### 圆
| 命令 | 说明 | 示例 |
|------|------|------|
| `\tkzDrawCircle(O,A)` | 以 O 为圆心过 A 画圆 | `\tkzDrawCircle(O,A)` |
| `\tkzDrawCircle[R](O,3cm)` | 指定半径画圆 | `\tkzDrawCircle[R](O,3cm)` |

### 角度标记
| 命令 | 说明 | 示例 |
|------|------|------|
| `\tkzMarkAngle(B,A,C)` | 标记∠BAC（顶点为 A） | `\tkzMarkAngle(B,A,C)` |
| `\tkzMarkRightAngle(A,B,C)` | 标记直角（顶点为 B） | `\tkzMarkRightAngle(B,A,C)` |

### 标注
| 命令 | 说明 | 示例 |
|------|------|------|
| `\tkzLabelPoint[位置](A){文字}` | 标注点名 | `\tkzLabelPoint[below](A){$A$}` |
| `\tkzLabelPoints[below](A,B)` | 批量标注（同方向） | `\tkzLabelPoints[below](A,B)` |
| `\tkzLabelSegment(A,B){文字}` | 标注边长 | `\tkzLabelSegment[above](A,B){$5$}` |
| `\tkzLabelAngle(B,A,C){文字}` | 标注角度 | `\tkzLabelAngle(B,A,C){$60^\circ$}` |

**位置参数**：`above`、`below`、`left`、`right`、`above left`、`above right`、`below left`、`below right`

---

## 禁止使用的命令

以下命令可能执行外部程序或读写文件，**绝对禁止**：

- `\input`、`\include`
- `\write18`、`\immediate`
- `\openout`、`\closeout`
- `\special`（部分形式）

---

## 典型几何图示例

### 示例 1：直角三角形（含角标和边长标注）

```
\tkzDefPoint(0,0){A}
\tkzDefPoint(4,0){B}
\tkzDefPoint(0,3){C}
\tkzDrawPolygon(A,B,C)
\tkzMarkRightAngle(B,A,C)
\tkzLabelPoints[below](A,B)
\tkzLabelPoints[left](C)
\tkzLabelSegment[below](A,B){$4$}
\tkzLabelSegment[left](A,C){$3$}
\tkzLabelSegment[above right](B,C){$5$}
```

**对应题干**：在直角三角形 ABC 中，∠A = 90°，AB = 4，AC = 3，BC = 5。

---

### 示例 2：三角形的高线（辅助线 + 垂足）

```
\tkzDefPoint(0,0){B}
\tkzDefPoint(5,0){C}
\tkzDefPoint(2,4){A}
\tkzDrawPolygon(A,B,C)
\tkzDefPointBy[projection=onto B--C](A) \tkzGetPoint{D}
\tkzDrawSegment[dashed](A,D)
\tkzMarkRightAngle(A,D,C)
\tkzLabelPoints[below](B,C,D)
\tkzLabelPoints[above](A)
\tkzLabelSegment[right](A,D){$h$}
```

**对应题干**：在三角形 ABC 中，AD 是 BC 边上的高，D 在 BC 上，AD ⊥ BC。

---

### 示例 2b：线段内部按比例取点（DE∥BC，平行线截三角形）

**这是 K12 最高频的几何场景之一，D 在 AB 上、E 在 AC 上，满足 AD:AB = AE:AC。**
**绝对不能把 D、E 都定在同一条边上，必须用 homothety（位似变换）精确构造。**

```
\tkzDefPoint(0,0){A}
\tkzDefPoint(6,0){B}
\tkzDefPoint(2,4){C}
\tkzDrawPolygon(A,B,C)
% D 在 AB 上，AD:AB = 1:3，即 AD=2, DB=4
\tkzDefPointBy[homothety=center A ratio 0.333](B) \tkzGetPoint{D}
% E 在 AC 上，AE:AC = 1:3（与 AD:AB 相同，保证 DE∥BC）
\tkzDefPointBy[homothety=center A ratio 0.333](C) \tkzGetPoint{E}
\tkzDrawSegment(D,E)
\tkzLabelPoints[below](A,D,B)
\tkzLabelPoints[left](C,E)
\tkzLabelSegment[below](A,D){$2$}
\tkzLabelSegment[below](D,B){$4$}
```

**对应题干**：在 △ABC 中，DE∥BC，AD=2，DB=4，DE=3，求 BC。
**比例计算**：AD:AB = 2:(2+4) = 1:3，所以 ratio = 1/3 ≈ 0.333。

---

### 示例 2c：角平分线与对边的交点

**BD 是 ∠ABC 的角平分线，交 AC 于点 D —— D 在 AC 上，不在 AB 上。**
**必须用 `\tkzInterLL` 求角平分线方向与对边的交点，不能手算 D 的坐标。**

```
\tkzDefPoint(0,0){A}
\tkzDefPoint(4,0){B}
\tkzDefPoint(1,3){C}
\tkzDrawPolygon(A,B,C)
% 求 ∠ABC 的角平分线方向点：BA 方向单位向量 + BC 方向单位向量
\tkzDefPointBy[homothety=center B ratio 0.5](A) \tkzGetPoint{Ba}
\tkzDefPointBy[homothety=center B ratio 0.5](C) \tkzGetPoint{Bc}
% 角平分线方向点（用于确定角平分线所在直线）
\tkzDefMidPoint(Ba,Bc) \tkzGetPoint{Bm}
% 用两线交点命令求 BD 与 AC 的交点 D
\tkzInterLL(B,Bm)(A,C) \tkzGetPoint{D}
\tkzDrawSegment(B,D)
\tkzLabelPoints[below](A,B)
\tkzLabelPoints[above](C)
\tkzLabelPoint[right](D){$D$}
```

**对应题干**：在 △ABC 中，BD 是 ∠ABC 的角平分线，交 AC 于点 D。

---

### 示例 3：圆与切线

```
\tkzDefPoint(0,0){O}
\tkzDefPoint(3,0){A}
\tkzDrawCircle(O,A)
\tkzDefPoint(5,0){T}
\tkzDefPointBy[projection=onto O--T](A) \tkzGetPoint{P}
\tkzDrawSegment(O,T)
\tkzDrawSegment(T,A)
\tkzMarkRightAngle(O,A,T)
\tkzDrawPoint(O)
\tkzLabelPoints[below](O,T)
\tkzLabelPoints[above](A)
```

---

### 示例 4：平行线与截线

```
\tkzDefPoint(0,0){A}
\tkzDefPoint(5,0){B}
\tkzDefPoint(0,2){C}
\tkzDefPoint(5,2){D}
\tkzDefPoint(1.5,3.5){E}
\tkzDefPoint(3.5,-1.5){F}
\tkzDrawSegments(A,B C,D E,F)
\tkzMarkAngle[size=0.5](F,A,C)
\tkzLabelPoints[below](A,B)
\tkzLabelPoints[above](C,D)
\tkzLabelPoints[above](E)
\tkzLabelPoints[below](F)
```

---

### 示例 5：多图场景（折叠题，sortOrder 1 和 2）

**图1（折叠前）**的 tikzCode：
```
\tkzDefPoint(0,0){A}
\tkzDefPoint(4,0){B}
\tkzDefPoint(4,3){C}
\tkzDefPoint(0,3){D}
\tkzDrawPolygon(A,B,C,D)
\tkzLabelPoints[below](A,B)
\tkzLabelPoints[above](D,C)
```

**图2（折叠后）**的 tikzCode：
```
\tkzDefPoint(0,0){A}
\tkzDefPoint(4,0){B}
\tkzDefPoint(4,3){C}
\tkzDefPoint(0,3){D}
\tkzDefPoint(2,3){E}
\tkzDefPoint(0,1.5){F}
\tkzDrawPolygon(A,B,C,E,F)
\tkzDrawSegment[dashed](F,E)
\tkzLabelPoints[below](A,B)
\tkzLabelPoints[above](E,C)
\tkzLabelPoints[left](D,F)
```

---

## 常见错误模式（禁止）

| ❌ 错误 | ✅ 正确 |
|--------|--------|
| 硬编码像素坐标（如 `\draw (100pt, 200pt)`） | 使用逻辑坐标（如 `\tkzDefPoint(3,4){A}`） |
| 重复定义同名点（如两个 `\tkzDefPoint(x,y){A}`） | 每个标签唯一，如 `A`、`B`、`C` |
| 在 tikzCode 中写 `\documentclass` 或 `\begin{document}` | 后端自动包裹模板，tikzCode 只写内容 |
| 使用 `\input`、`\write18` 等危险命令 | 严格遵守白名单 |
| 标注文字遮挡图形（随意选位置参数） | 根据点位选合适方向（上方点用 above，下方点用 below） |
| **⚠️ 圆上的点用手算坐标**（如圆心(0,0)半径5，却把点定在(2,4)，实际距圆心≈4.47≠5） | **必须用 `\tkzDefPointOnCircle` 或 `\tkzInterLC` 让 tkz-euclide 精确构造** |
| **⚠️ 垂足、中点、交点用估算坐标** | **必须用 `\tkzDefMidPoint`、`\tkzInterLL`、投影命令精确构造** |
| **⚠️ 使用白名单外的命令**（如 `\tkzMarkParallel`） | **只使用白名单和示例中出现的命令。平行线关系用方向相同的线段+相等角标注表达，无专门标记命令** |

---

## ⚠️ 涉及圆的几何图：坐标一致性铁律

**凡是题目中有圆，且需要在圆上取点或构造与圆相关的几何关系，必须用 tkz-euclide 命令精确构造，绝对不允许手算坐标。**

### 常见场景及正确写法

#### 场景 1：在圆上按指定角度取点

```
\tkzDefPoint(0,0){O}
\tkzDefPoint(5,0){A}          % 半径为5
\tkzDrawCircle(O,A)
% ✅ 在圆上取角度为53°的点C（\tkzGetPoint 获取计算结果）
\tkzDefPointBy[rotation=center O angle 53](A)  \tkzGetPoint{C}
\tkzDrawPoint(C)
\tkzLabelPoint[above right](C){$C$}
```

#### 场景 2：直径上作弦的垂足（垂径定理题型）

```
\tkzDefPoint(0,0){O}
\tkzDefPoint(-5,0){A}
\tkzDefPoint(5,0){B}
\tkzDrawCircle(O,A)
\tkzDrawSegment(A,B)
% ✅ 在圆上取点C，用旋转构造，保证C在圆上
\tkzDefPointBy[rotation=center O angle 60](B)  \tkzGetPoint{C}
% ✅ 用投影命令求C在AB上的垂足D，精确，不手算
\tkzDefPointBy[projection=onto A--B](C)  \tkzGetPoint{D}
\tkzDrawSegment(C,D)
\tkzMarkRightAngle(C,D,B)
\tkzLabelPoints[below](A,D,O,B)
\tkzLabelPoints[above](C)
```

#### 场景 3：圆与直线的交点

```
\tkzDefPoint(0,0){O}
\tkzDefPoint(5,0){A}
\tkzDrawCircle(O,A)
\tkzDefPoint(-3,2){P}
\tkzDefPoint(3,2){Q}
% ✅ 用 \tkzInterLC 求直线PQ与圆O的交点，精确
\tkzInterLC(P,Q)(O,A)  \tkzGetPoints{E}{F}
\tkzDrawPoints(E,F)
\tkzDrawSegment(P,Q)
```

#### 场景 4：两直线的交点

```
\tkzDefPoint(0,0){A}
\tkzDefPoint(4,0){B}
\tkzDefPoint(1,3){C}
\tkzDefPoint(3,3){D}
% ✅ 用 \tkzInterLL 求两直线交点，精确
\tkzInterLL(A,D)(B,C)  \tkzGetPoint{P}
\tkzDrawPoint(P)
```

#### 场景 5：中点

```
\tkzDefPoint(0,0){A}
\tkzDefPoint(6,0){B}
% ✅ 用 \tkzDefMidPoint 求中点，精确
\tkzDefMidPoint(A,B)  \tkzGetPoint{M}
\tkzDrawPoint(M)
\tkzLabelPoint[below](M){$M$}
```

---

### 自查清单：涉及圆的题目必查

- [ ] 圆上的每个点是否用命令构造（`\tkzDefPointOnCircle`、旋转等），而非手算坐标？
- [ ] 验证：`点到圆心的距离 = 圆的半径`（手算坐标时极易出错）
- [ ] 垂足是否用 `projection` 命令，而非目测坐标？
- [ ] 交点是否用 `\tkzInterLL` / `\tkzInterLC`，而非估算？
- [ ] 中点是否用 `\tkzDefMidPoint`，而非 `((x1+x2)/2, (y1+y2)/2)` 手算？
