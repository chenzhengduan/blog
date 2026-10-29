请使用 question-variant skill 加载单题变式规范，然后根据以下原题生成一道变式题。

【元数据】
- 学科：{subject}
- 年级：{grade}
- 题型：{questionType}
- 自定义题型要求：{customTypeDefinition}
- 目标难度：{difficulty}

【原题信息】
原题题干：
{originalContent}

{originalOptionsBlock}
{originalAnswerBlock}
{originalExplanationBlock}
{imagesBlock}

【补充要求】
{extraRequirements}

要求：
1. 新题必须与原题的知识点、考查方向、题型一致
2. 题干、选项必须完全不同，不能与原题雷同，不能仅修改数字或人名/地名
3. 如果原题答案未提供，请先自行理解原题考点，再生成变式题
4. 新题必须输出标准答案（answer）和解析（explanation）
5. 配图规则（按题内图决策流程执行）：
   - 几何配图：题干需引用图片时，diagrams 输出 imgType="BASE64" + tikzCode
   - 非几何必要图：需引用图片时，调用 image_generate 生图，diagrams 输出 imgType="URL" + url
   - 精确数据图/计数量化题：用文字或表格列出数据/数量，不调用生图，不输出 diagrams
   - 装饰图或可文字化图：不配图，题干禁止出现"如图""下图""见图""观察图片""看图""图片中"等表达
   - 禁止复用原题图片、禁止编造图片 URL、禁止生图 prompt 含答案/解题线索
6. 如果元数据中题型为"待推断"，请根据原题信息推断题型；不属于标准题型时使用 custom，名称不明时留空
7. 生成变式题后调用 submit_variant_question 工具提交，参数含 questionType/type/content/options/answer/explanation/diagrams 等
