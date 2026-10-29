-- CX 出卷平台 —— 数据库全量 DDL（PostgreSQL 方言）
--
-- 维护约定：
--   每次 DDL 变更必须同时做两件事：
--   1. 在 db/migrations/ 新增增量脚本（命名：NNN_描述.sql，NNN 为三位序号，PG 方言）
--   2. 更新本文件，保持本文件始终是当前完整数据库结构的"真相来源"
--
-- 迁移说明（specs/050-mysql-to-postgres-migration）：
--   本文件已于 2026-08-18 从 MySQL 方言整体转换为 PostgreSQL 方言，MySQL 已下线。
--   db/migrations/ 下 2026-08-18 之前的脚本为 MySQL 历史增量，仅作演进记录保留，
--   不再在 PG 环境重放；PG 环境以本文件全量初始化。
--   注意：PG 索引名为 schema 级全局唯一，本文件索引统一命名为「表名_索引名」。
--
-- 规范：
--   - 主键：bigint，雪花 ID（应用侧生成，不使用数据库序列/自增）
--   - 公共字段：每表必须有 create_time、update_time、deleted
--
-- 建库参考（首次初始化执行）：
--   CREATE DATABASE cx;
--   （PG 字符集由库/集群统一指定，表级不声明；时区在实例参数设 timezone=Asia/Shanghai）
--
-- 最后更新：2026-08-20（知识点树形库与 pgvector 索引，specs/052）
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE question (
    id bigint        NOT NULL,
    app_id varchar(64)   DEFAULT NULL,
    external_org_id varchar(64) DEFAULT NULL,
    external_user_id varchar(64) DEFAULT NULL,
    external_user_name varchar(128) DEFAULT NULL,
    subject varchar(20)   DEFAULT NULL,
    grade smallint NOT NULL,
    type varchar(50)   NOT NULL,
    custom_question_type_name varchar(50) DEFAULT NULL,
    custom_question_type_description text DEFAULT NULL,
    content text          NOT NULL,
    options text          DEFAULT NULL,
    answer text          DEFAULT NULL,
    explanation text          DEFAULT NULL,
    source varchar(20)   DEFAULT NULL,
    difficulty varchar(10)   DEFAULT NULL,
    parent_id bigint        DEFAULT NULL,
    sub_order integer           DEFAULT NULL,
    blanks_json text          DEFAULT NULL,
    score integer DEFAULT NULL,
    create_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint    NOT NULL DEFAULT 0,
    CONSTRAINT pk_question PRIMARY KEY (id)
);
COMMENT ON COLUMN question.custom_question_type_name IS '自定义题型名称；标准题型为空';
COMMENT ON COLUMN question.custom_question_type_description IS '自定义题型定义及要求；最多2000字符，可为空';
COMMENT ON TABLE question IS '题库（题组子题复用本表，通过 parent_id 反向归属父题）';
COMMENT ON COLUMN question.id IS '主键（雪花 ID）';
COMMENT ON COLUMN question.app_id IS '接入方应用标识（系统从鉴权上下文填充；子题从父题继承）。补救 019 漏加项';
COMMENT ON COLUMN question.external_org_id IS '接入方机构标识（可选，仅记录不筛选）';
COMMENT ON COLUMN question.external_user_id IS '操作人标识（录题任务创建时由接入方传入）';
COMMENT ON COLUMN question.external_user_name IS '操作人姓名快照（创建时记录，不随后续改名更新）';
COMMENT ON COLUMN question.subject IS '学科编码（SubjectEnum value，共 12 个受控值）';
COMMENT ON COLUMN question.grade IS '年级 1–12（小学 1–6，初中 7–9，高中 10–12：高一、高二、高三）；0=未知年级（接入方确实无法提供时使用，主要见于不经过 AI 推断的开放入库接口）';
COMMENT ON COLUMN question.type IS '题型标准编码；自定义或未知分类为custom';
COMMENT ON COLUMN question.content IS '题干文本（含 LaTeX 行内公式）';
COMMENT ON COLUMN question.options IS '选项列表 JSON，仅 choice/multi 有值；格式：[{"label":"A","content":"..."},...]';
COMMENT ON COLUMN question.answer IS '标准答案，JSON 数组格式：["A"]、["A","C"]、["TRUE"]、["5"]、["答案1","答案2"]；AI 未返回时为 NULL';
COMMENT ON COLUMN question.explanation IS '解析说明';
COMMENT ON COLUMN question.source IS '来源枚举：AI=AI生成 / PHOTO=拍照录入；AI 输出非法值时应用层置 NULL';
COMMENT ON COLUMN question.difficulty IS '难度（easy/medium/hard，复用 exam.difficulty 枚举）；子题独立记录允许空，AI 输出非法值时应用层置 NULL';
COMMENT ON COLUMN question.parent_id IS '父题 ID；题组子题才有，普通题/父题为 NULL。子题通过此列反向归属父题';
COMMENT ON COLUMN question.sub_order IS '子题在父题下序号（1,2,3），渲染为 (1)(2)(3)；普通题/父题为 NULL';
COMMENT ON COLUMN question.blanks_json IS '多空填空空位结构 JSON：[{"order":1,"score":2},...]；与 answer 解耦，无答案题仍可有空位';
COMMENT ON COLUMN question.score IS '子题分值（题组子题专用，子题不挂关联表故分值存此列）；普通题/父题为 NULL，其分值在 exam_section_question 关联表';
COMMENT ON COLUMN question.create_time IS '创建时间';
COMMENT ON COLUMN question.update_time IS '更新时间';
COMMENT ON COLUMN question.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_question_subject_grade_type ON question (subject, grade, type);
CREATE INDEX idx_question_parent ON question (parent_id);
CREATE INDEX idx_question_app ON question (app_id);
CREATE INDEX idx_question_difficulty ON question (difficulty);


-- ============================================================
-- 知识点体系（app_id 为空表示平台公共体系）
-- ============================================================
CREATE TABLE knowledge_tree (
    id bigint NOT NULL,
    app_id varchar(64) DEFAULT NULL,
    name varchar(128) NOT NULL,
    subject varchar(32) NOT NULL,
    stage varchar(16) NOT NULL,
    status varchar(16) NOT NULL DEFAULT 'ACTIVE',
    content_version bigint NOT NULL DEFAULT 1,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_knowledge_tree PRIMARY KEY (id),
    CONSTRAINT uk_knowledge_tree_app_subject_stage UNIQUE (app_id, subject, stage),
    CONSTRAINT ck_knowledge_tree_stage CHECK (stage IN ('primary', 'junior', 'senior')),
    CONSTRAINT ck_knowledge_tree_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);
COMMENT ON TABLE knowledge_tree IS '知识点体系（app_id 为空表示平台公共体系）';
COMMENT ON COLUMN knowledge_tree.id IS '主键（雪花 ID）';
COMMENT ON COLUMN knowledge_tree.app_id IS '归属接入方应用标识；NULL 表示平台公共体系';
COMMENT ON COLUMN knowledge_tree.name IS '体系名称';
COMMENT ON COLUMN knowledge_tree.subject IS '学科编码（SubjectEnum value）';
COMMENT ON COLUMN knowledge_tree.stage IS '学段：primary / junior / senior';
COMMENT ON COLUMN knowledge_tree.status IS '状态：ACTIVE / DISABLED；禁用体系不参与召回';
COMMENT ON COLUMN knowledge_tree.content_version IS '整树内容版本；每次导入递增，供异步索引写入 fencing 校验';
COMMENT ON COLUMN knowledge_tree.create_time IS '创建时间（自动填充）';
COMMENT ON COLUMN knowledge_tree.update_time IS '更新时间（自动填充）';
COMMENT ON COLUMN knowledge_tree.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_knowledge_tree_subject ON knowledge_tree (subject);
CREATE UNIQUE INDEX uk_knowledge_tree_public_subject_stage
    ON knowledge_tree (subject, stage)
    WHERE app_id IS NULL AND deleted = 0;


-- ============================================================
-- 知识点树节点
-- ============================================================
CREATE TABLE knowledge_point (
    id bigint NOT NULL,
    tree_id bigint NOT NULL,
    no varchar(64) NOT NULL,
    name varchar(256) NOT NULL,
    parent_id bigint DEFAULT NULL,
    parent_no varchar(64) DEFAULT NULL,
    level integer NOT NULL,
    is_leaf boolean NOT NULL,
    sort_order integer NOT NULL DEFAULT 0,
    path_nos varchar(512) NOT NULL,
    path_text varchar(1024) NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_knowledge_point PRIMARY KEY (id)
);
COMMENT ON TABLE knowledge_point IS '知识点体系树节点；仅叶子节点可索引和挂接题目';
COMMENT ON COLUMN knowledge_point.id IS '主键（雪花 ID）';
COMMENT ON COLUMN knowledge_point.tree_id IS '所属知识点体系 ID';
COMMENT ON COLUMN knowledge_point.no IS '体系内稳定编号（字符串，可含字母）';
COMMENT ON COLUMN knowledge_point.name IS '知识点名称';
COMMENT ON COLUMN knowledge_point.parent_id IS '父节点 ID；根节点为空';
COMMENT ON COLUMN knowledge_point.parent_no IS '父节点体系内编号；根节点为空';
COMMENT ON COLUMN knowledge_point.level IS '节点层级，根节点为 1';
COMMENT ON COLUMN knowledge_point.is_leaf IS '是否叶子节点';
COMMENT ON COLUMN knowledge_point.sort_order IS '同级节点顺序';
COMMENT ON COLUMN knowledge_point.path_nos IS '完整编号路径，以 / 分隔';
COMMENT ON COLUMN knowledge_point.path_text IS '完整名称路径，以 > 分隔';
COMMENT ON COLUMN knowledge_point.create_time IS '创建时间（自动填充）';
COMMENT ON COLUMN knowledge_point.update_time IS '更新时间（自动填充）';
COMMENT ON COLUMN knowledge_point.deleted IS '逻辑删除（0=正常，1=已删）';
-- 覆盖导入按编号 upsert：仅活跃节点要求编号唯一，已删除节点允许同编号留存以支撑下线追溯与复活
CREATE UNIQUE INDEX uk_knowledge_point_tree_no ON knowledge_point (tree_id, no) WHERE deleted = 0;
CREATE INDEX idx_knowledge_point_tree_leaf ON knowledge_point (tree_id, is_leaf);
CREATE INDEX idx_knowledge_point_parent ON knowledge_point (parent_id);


-- ============================================================
-- 叶子知识点语义向量索引
-- ============================================================
CREATE TABLE knowledge_point_embedding (
    id bigint NOT NULL,
    knowledge_point_id bigint NOT NULL,
    embedding_model varchar(64) NOT NULL,
    embedding_text text NOT NULL,
    embedding vector(1024) NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_knowledge_point_embedding PRIMARY KEY (id),
    CONSTRAINT uk_kp_embedding_point_model UNIQUE (knowledge_point_id, embedding_model)
);
COMMENT ON TABLE knowledge_point_embedding IS '叶子知识点语义向量索引，可按模型独立重建';
COMMENT ON COLUMN knowledge_point_embedding.id IS '主键（雪花 ID）';
COMMENT ON COLUMN knowledge_point_embedding.knowledge_point_id IS '叶子知识点 ID';
COMMENT ON COLUMN knowledge_point_embedding.embedding_model IS '生成向量的模型名称';
COMMENT ON COLUMN knowledge_point_embedding.embedding_text IS '送入向量模型的可复现文本';
COMMENT ON COLUMN knowledge_point_embedding.embedding IS '1024 维向量；变更维度必须同步迁移 DDL 并重建数据';
COMMENT ON COLUMN knowledge_point_embedding.create_time IS '创建时间（自动填充）';
COMMENT ON COLUMN knowledge_point_embedding.update_time IS '更新时间（自动填充）';
COMMENT ON COLUMN knowledge_point_embedding.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_kp_embedding_vec
    ON knowledge_point_embedding USING hnsw (embedding vector_cosine_ops);


-- ============================================================
-- 题目-知识点关联表（多对多）
-- ============================================================
CREATE TABLE question_knowledge (
    id bigint      NOT NULL,
    question_id bigint      NOT NULL,
    knowledge_point_id bigint      NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_question_knowledge PRIMARY KEY (id),
    CONSTRAINT uk_question_knowledge_question_kp UNIQUE (question_id, knowledge_point_id)
);
COMMENT ON TABLE question_knowledge IS '题目-知识点关联（多对多，子题独立挂关联）';
COMMENT ON COLUMN question_knowledge.id IS '主键（雪花 ID）';
COMMENT ON COLUMN question_knowledge.question_id IS '题目 ID（含子题，子题直接挂关联，不继承父题）';
COMMENT ON COLUMN question_knowledge.knowledge_point_id IS '知识点 ID（关联 knowledge_point.id，仅允许叶子节点）';
COMMENT ON COLUMN question_knowledge.create_time IS '创建时间（自动填充）';
COMMENT ON COLUMN question_knowledge.update_time IS '更新时间（自动填充）';
COMMENT ON COLUMN question_knowledge.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_question_knowledge_kp ON question_knowledge (knowledge_point_id);


-- ============================================================
-- 知识点名称未命中记录（体系缺口聚合分析）
-- ============================================================
CREATE TABLE knowledge_point_unmatched_log (
    id bigint      NOT NULL,
    question_id bigint      NOT NULL,
    app_id varchar(64) NOT NULL,
    subject varchar(32) DEFAULT NULL,
    grade int DEFAULT NULL,
    name varchar(100) NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_knowledge_point_unmatched_log PRIMARY KEY (id)
);
COMMENT ON TABLE knowledge_point_unmatched_log IS '知识点名称未命中记录（确认入库时请求了但未挂载的名称，用于体系缺口聚合分析）';
COMMENT ON COLUMN knowledge_point_unmatched_log.id IS '主键（雪花 ID）';
COMMENT ON COLUMN knowledge_point_unmatched_log.question_id IS '正式题目 ID（含子题，子题独立记录）';
COMMENT ON COLUMN knowledge_point_unmatched_log.app_id IS '接入方应用标识（隔离键）';
COMMENT ON COLUMN knowledge_point_unmatched_log.subject IS '题目学科编码（可空，与确认请求一致）';
COMMENT ON COLUMN knowledge_point_unmatched_log.grade IS '题目年级 1-12（可空，与确认请求一致）';
COMMENT ON COLUMN knowledge_point_unmatched_log.name IS '未命中的知识点名称（trim 后原文）';
COMMENT ON COLUMN knowledge_point_unmatched_log.create_time IS '创建时间（自动填充）';
COMMENT ON COLUMN knowledge_point_unmatched_log.update_time IS '更新时间（自动填充）';
COMMENT ON COLUMN knowledge_point_unmatched_log.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_kp_unmatched_name ON knowledge_point_unmatched_log (subject, name);
CREATE INDEX idx_kp_unmatched_question ON knowledge_point_unmatched_log (question_id);


-- ============================================================
-- 试卷
-- ============================================================
CREATE TABLE exam (
    id bigint           NOT NULL,
    app_id varchar(64)      DEFAULT NULL,
    external_org_id varchar(64)      DEFAULT NULL,
    external_user_id varchar(64)      DEFAULT NULL,
    external_user_name varchar(128)     DEFAULT NULL,
    title varchar(200)     DEFAULT NULL,
    subject varchar(20)      DEFAULT NULL,
    grade smallint DEFAULT NULL,
    total_score integer NOT NULL,
    difficulty varchar(10)      NOT NULL,
    duration_minutes integer DEFAULT NULL,
    question_count integer NOT NULL DEFAULT 0,
    topic varchar(200)     DEFAULT NULL,
    extra_requirements varchar(500)     DEFAULT NULL,
    create_time timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam PRIMARY KEY (id)
);
COMMENT ON TABLE exam IS '试卷';
COMMENT ON COLUMN exam.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam.app_id IS '接入方应用标识（系统从鉴权上下文填充）';
COMMENT ON COLUMN exam.external_org_id IS '接入方机构标识（可选，仅记录不筛选）';
COMMENT ON COLUMN exam.external_user_id IS '操作人标识（接入方传入，出卷人/发起人/提交人统一字段）';
COMMENT ON COLUMN exam.external_user_name IS '操作人姓名快照（创建时由接入方传入，不随后续改名更新）';
COMMENT ON COLUMN exam.title IS '试卷标题（如"五年级数学期末模拟卷"）';
COMMENT ON COLUMN exam.subject IS '学科（math / chinese / english）；NULL=未知（变式生成请求未传学科时落库为空）';
COMMENT ON COLUMN exam.grade IS '年级 1–9；NULL=未知（变式生成请求未传年级时落库为空）';
COMMENT ON COLUMN exam.total_score IS '满分';
COMMENT ON COLUMN exam.difficulty IS '难度（easy / medium / hard）';
COMMENT ON COLUMN exam.duration_minutes IS '建议作答时长（分钟），AI 估算';
COMMENT ON COLUMN exam.question_count IS '总题数（冗余存储，写入时由 saveExam 填入）';
COMMENT ON COLUMN exam.topic IS '知识点 / 考察范围';
COMMENT ON COLUMN exam.extra_requirements IS '出卷时的补充要求';
COMMENT ON COLUMN exam.create_time IS '创建时间';
COMMENT ON COLUMN exam.update_time IS '更新时间';
COMMENT ON COLUMN exam.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_subject_grade ON exam (subject, grade);
CREATE INDEX idx_exam_subject_grade_time ON exam (subject, grade, create_time);
CREATE INDEX idx_exam_app ON exam (app_id);


-- ============================================================
-- 试卷大题
-- ============================================================
CREATE TABLE exam_section (
    id bigint           NOT NULL,
    app_id varchar(64)      DEFAULT NULL,
    exam_id bigint           NOT NULL,
    sort_order smallint NOT NULL,
    title varchar(100)     NOT NULL,
    question_type varchar(50)      NOT NULL,
    custom_question_type_name varchar(50) DEFAULT NULL,
    custom_question_type_description text DEFAULT NULL,
    score_note varchar(200)     DEFAULT NULL,
    create_time timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_section PRIMARY KEY (id)
);
COMMENT ON COLUMN exam_section.custom_question_type_name IS '自定义题型名称；标准题型为空';
COMMENT ON COLUMN exam_section.custom_question_type_description IS '自定义题型定义及要求；最多2000字符，可为空';
COMMENT ON TABLE exam_section IS '试卷大题';
COMMENT ON COLUMN exam_section.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam_section.app_id IS '接入方应用标识（从试卷继承）';
COMMENT ON COLUMN exam_section.exam_id IS '所属试卷 ID';
COMMENT ON COLUMN exam_section.sort_order IS '大题排序（从 1 开始）';
COMMENT ON COLUMN exam_section.title IS '大题标题（如"一、选择题"）';
COMMENT ON COLUMN exam_section.question_type IS '题型';
COMMENT ON COLUMN exam_section.score_note IS '分值说明（如"每题 3 分，共 15 分"）';
COMMENT ON COLUMN exam_section.create_time IS '创建时间';
COMMENT ON COLUMN exam_section.update_time IS '更新时间';
COMMENT ON COLUMN exam_section.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_section_exam ON exam_section (exam_id);


-- ============================================================
-- 试卷大题题目关联
-- ============================================================
CREATE TABLE exam_section_question (
    id bigint           NOT NULL,
    app_id varchar(64)      DEFAULT NULL,
    section_id bigint           NOT NULL,
    question_id bigint           NOT NULL,
    number integer NOT NULL,
    sort_order smallint  NOT NULL,
    score integer NOT NULL DEFAULT 0,
    create_time timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_section_question PRIMARY KEY (id),
    CONSTRAINT uk_exam_section_question_section_question UNIQUE (section_id, question_id)
);
COMMENT ON TABLE exam_section_question IS '试卷大题题目关联';
COMMENT ON COLUMN exam_section_question.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam_section_question.app_id IS '接入方应用标识（从试卷继承）';
COMMENT ON COLUMN exam_section_question.section_id IS '所属大题 ID';
COMMENT ON COLUMN exam_section_question.question_id IS '题目 ID（关联 question 表）';
COMMENT ON COLUMN exam_section_question.number IS '全卷连续题号（从 1 开始，后端计算填充）';
COMMENT ON COLUMN exam_section_question.sort_order IS '大题内排序（从 1 开始）';
COMMENT ON COLUMN exam_section_question.score IS '该题分值（整卷生成时由 AI 输出，0 表示未记录）';
COMMENT ON COLUMN exam_section_question.create_time IS '创建时间';
COMMENT ON COLUMN exam_section_question.update_time IS '更新时间';
COMMENT ON COLUMN exam_section_question.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_section_question_question ON exam_section_question (question_id);


-- ============================================================
-- 出题 / 出卷异步任务（002_add_generation_task.sql）
-- ============================================================
CREATE TABLE generation_task (
    id bigint           NOT NULL,
    app_id varchar(64)      DEFAULT NULL,
    external_org_id varchar(64)      DEFAULT NULL,
    external_user_id varchar(64)      DEFAULT NULL,
    external_user_name varchar(128)    DEFAULT NULL,
    type varchar(20)      NOT NULL,
    status varchar(20)      NOT NULL DEFAULT 'PENDING',
    subject varchar(20)      NOT NULL,
    grade smallint NOT NULL,
    result_id bigint           DEFAULT NULL,
    error_message text             DEFAULT NULL,
    create_time timestamp         NOT NULL,
    update_time timestamp         NOT NULL,
    deleted smallint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_generation_task PRIMARY KEY (id)
);
COMMENT ON TABLE generation_task IS '出题 / 出卷异步任务';
COMMENT ON COLUMN generation_task.id IS '主键（雪花 ID），即对外暴露的 taskId';
COMMENT ON COLUMN generation_task.app_id IS '接入方应用标识';
COMMENT ON COLUMN generation_task.external_org_id IS '接入方机构标识（可选，仅记录不筛选）';
COMMENT ON COLUMN generation_task.external_user_id IS '操作人标识（接入方传入，任务发起人）';
COMMENT ON COLUMN generation_task.external_user_name IS '操作人姓名快照（创建时记录，不随后续改名更新）';
COMMENT ON COLUMN generation_task.type IS '任务类型：EXAM / QUESTION';
COMMENT ON COLUMN generation_task.status IS '状态：PENDING / RUNNING / COMPLETED / FAILED / CANCELLED';
COMMENT ON COLUMN generation_task.subject IS '学科（math / chinese / english），冗余存储便于列表查询';
COMMENT ON COLUMN generation_task.grade IS '年级 1–9，冗余存储便于列表显示';
COMMENT ON COLUMN generation_task.result_id IS '完成后写入：examId 或 questionId';
COMMENT ON COLUMN generation_task.error_message IS '失败或取消时的原因';
COMMENT ON COLUMN generation_task.create_time IS '创建时间';
COMMENT ON COLUMN generation_task.update_time IS '更新时间';
COMMENT ON COLUMN generation_task.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_generation_task_status_type ON generation_task (status, type);
CREATE INDEX idx_generation_task_create_time ON generation_task (create_time);
CREATE INDEX idx_generation_task_app ON generation_task (app_id);


-- ============================================================
-- 答卷（exam_submission）（006_add_submission_tables.sql）
-- ============================================================
CREATE TABLE exam_submission (
    id bigint        NOT NULL,
    app_id varchar(64)   DEFAULT NULL,
    external_org_id varchar(64)   DEFAULT NULL,
    external_user_id varchar(64)   DEFAULT NULL,
    external_user_name varchar(128)  DEFAULT NULL,
    external_examinee_id varchar(64)   DEFAULT NULL,
    external_examinee_name varchar(128)  DEFAULT NULL,
    recognized_examinee_name varchar(128) DEFAULT NULL,
    exam_id bigint        NOT NULL,
    status varchar(20)   NOT NULL DEFAULT 'PENDING',
    submit_type varchar(32)   DEFAULT NULL,
    total_score smallint      DEFAULT NULL,
    create_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint    NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_submission PRIMARY KEY (id)
);
COMMENT ON TABLE exam_submission IS '答卷';
COMMENT ON COLUMN exam_submission.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam_submission.app_id IS '接入方应用标识';
COMMENT ON COLUMN exam_submission.external_org_id IS '接入方机构标识（可选，仅记录不筛选）';
COMMENT ON COLUMN exam_submission.external_user_id IS '操作人标识（执行提交动作的人）';
COMMENT ON COLUMN exam_submission.external_user_name IS '操作人姓名快照（提交时由接入方传入，不随后续改名更新）';
COMMENT ON COLUMN exam_submission.external_examinee_id IS '考生标识（真正做题的人）';
COMMENT ON COLUMN exam_submission.external_examinee_name IS '考生姓名快照（提交时由接入方传入，不随后续改名更新）';
COMMENT ON COLUMN exam_submission.recognized_examinee_name IS '照片识别的考生姓名；未填写、无法辨认或多页姓名冲突时为 NULL，与接入方传入姓名独立';
COMMENT ON COLUMN exam_submission.exam_id IS '关联试卷 ID';
COMMENT ON COLUMN exam_submission.status IS '状态：PENDING / GRADING / COMPLETED / FAILED / CANCELLED';
COMMENT ON COLUMN exam_submission.submit_type IS '提交方式：ONLINE=在线答题，PHOTO=拍照上传；存量历史数据为 NULL';
COMMENT ON COLUMN exam_submission.total_score IS '批改总得分，全部批改完成后写入';
COMMENT ON COLUMN exam_submission.create_time IS '创建时间';
COMMENT ON COLUMN exam_submission.update_time IS '更新时间';
COMMENT ON COLUMN exam_submission.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_submission_exam ON exam_submission (exam_id);
CREATE INDEX idx_exam_submission_app ON exam_submission (app_id);


-- ============================================================
-- 答卷原始图片（exam_submission_image，仅存 URL）
-- ============================================================
CREATE TABLE exam_submission_image (
    id bigint              NOT NULL,
    app_id varchar(64)         DEFAULT NULL,
    submission_id bigint              NOT NULL,
    image_url varchar(1000)       NOT NULL,
    sort_order smallint    NOT NULL DEFAULT 1,
    create_time timestamp            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_submission_image PRIMARY KEY (id)
);
COMMENT ON TABLE exam_submission_image IS '答卷原始图片（仅存 URL 类型，base64 不落库）';
COMMENT ON COLUMN exam_submission_image.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam_submission_image.app_id IS '接入方应用标识（从答卷继承）';
COMMENT ON COLUMN exam_submission_image.submission_id IS '关联答卷 ID';
COMMENT ON COLUMN exam_submission_image.image_url IS '整卷答题原图 URL（base64 图片不落库）';
COMMENT ON COLUMN exam_submission_image.sort_order IS '同一答卷多图排序（从 1 开始）';
COMMENT ON COLUMN exam_submission_image.create_time IS '创建时间';
COMMENT ON COLUMN exam_submission_image.update_time IS '更新时间';
COMMENT ON COLUMN exam_submission_image.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_submission_image_submission ON exam_submission_image (submission_id);


-- ============================================================
-- 每题作答与批改结果（exam_submission_answer）
-- ============================================================
CREATE TABLE exam_submission_answer (
    id bigint        NOT NULL,
    app_id varchar(64)   DEFAULT NULL,
    submission_id bigint        NOT NULL,
    question_id bigint        NOT NULL,
    text_answer text          DEFAULT NULL,
    is_correct boolean    DEFAULT NULL,
    score smallint      DEFAULT NULL,
    feedback text          DEFAULT NULL,
    create_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint    NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_submission_answer PRIMARY KEY (id)
);
COMMENT ON TABLE exam_submission_answer IS '答题记录与批改结果';
COMMENT ON COLUMN exam_submission_answer.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam_submission_answer.app_id IS '接入方应用标识（从答卷继承）';
COMMENT ON COLUMN exam_submission_answer.submission_id IS '关联答卷 ID';
COMMENT ON COLUMN exam_submission_answer.question_id IS '关联题目 ID';
COMMENT ON COLUMN exam_submission_answer.text_answer IS '学生文字答案，可为空';
COMMENT ON COLUMN exam_submission_answer.is_correct IS '客观题：1=正确 0=错误；主观题或未批改时为 NULL';
COMMENT ON COLUMN exam_submission_answer.score IS '实际得分，批改后写入';
COMMENT ON COLUMN exam_submission_answer.feedback IS '主观题批改评语（LLM 输出），客观题为 NULL';
COMMENT ON COLUMN exam_submission_answer.create_time IS '创建时间';
COMMENT ON COLUMN exam_submission_answer.update_time IS '更新时间';
COMMENT ON COLUMN exam_submission_answer.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_submission_answer_submission ON exam_submission_answer (submission_id);
CREATE INDEX idx_exam_submission_answer_question ON exam_submission_answer (question_id);


-- ============================================================
-- 答题图片（exam_submission_answer_image，仅存 URL）
-- ============================================================
CREATE TABLE exam_submission_answer_image (
    id bigint              NOT NULL,
    app_id varchar(64)         DEFAULT NULL,
    answer_id bigint              NOT NULL,
    image_url varchar(1000)       NOT NULL,
    sort_order smallint    NOT NULL DEFAULT 1,
    create_time timestamp            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_submission_answer_image PRIMARY KEY (id)
);
COMMENT ON TABLE exam_submission_answer_image IS '答题图片（仅存 URL 类型，base64 不落库）';
COMMENT ON COLUMN exam_submission_answer_image.id IS '主键（雪花 ID）';
COMMENT ON COLUMN exam_submission_answer_image.app_id IS '接入方应用标识（从答卷继承）';
COMMENT ON COLUMN exam_submission_answer_image.answer_id IS '关联答题记录 ID';
COMMENT ON COLUMN exam_submission_answer_image.image_url IS '图片 URL（base64 图片不落库）';
COMMENT ON COLUMN exam_submission_answer_image.sort_order IS '同一题多图排序（从 1 开始）';
COMMENT ON COLUMN exam_submission_answer_image.create_time IS '创建时间';
COMMENT ON COLUMN exam_submission_answer_image.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_submission_answer_image_answer ON exam_submission_answer_image (answer_id);


-- ============================================================
-- 批改任务（exam_grading_task）
-- ============================================================
CREATE TABLE exam_grading_task (
    id bigint        NOT NULL,
    app_id varchar(64)   DEFAULT NULL,
    submission_id bigint        NOT NULL,
    status varchar(20)   NOT NULL DEFAULT 'PENDING',
    error_message text          DEFAULT NULL,
    create_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint    NOT NULL DEFAULT 0,
    CONSTRAINT pk_exam_grading_task PRIMARY KEY (id)
);
COMMENT ON TABLE exam_grading_task IS '答卷批改任务';
COMMENT ON COLUMN exam_grading_task.id IS '主键（雪花 ID），即 SSE 首帧返回的 taskId';
COMMENT ON COLUMN exam_grading_task.app_id IS '接入方应用标识（从答卷继承）';
COMMENT ON COLUMN exam_grading_task.submission_id IS '关联答卷 ID（一对一）';
COMMENT ON COLUMN exam_grading_task.status IS '状态：PENDING / GRADING / COMPLETED / FAILED / CANCELLED';
COMMENT ON COLUMN exam_grading_task.error_message IS '失败或取消时的原因';
COMMENT ON COLUMN exam_grading_task.create_time IS '创建时间';
COMMENT ON COLUMN exam_grading_task.update_time IS '更新时间';
COMMENT ON COLUMN exam_grading_task.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_exam_grading_task_submission ON exam_grading_task (submission_id);
CREATE INDEX idx_exam_grading_task_status ON exam_grading_task (status);
CREATE INDEX idx_exam_grading_task_app ON exam_grading_task (app_id);


-- ============================================================
-- 变式试卷来源图片（variant_exam_source）
-- ============================================================
CREATE TABLE variant_exam_source (
    id bigint        NOT NULL,
    app_id varchar(64)   DEFAULT NULL,
    exam_id bigint        NOT NULL,
    image_url varchar(2000) NOT NULL,
    sort_order smallint NOT NULL,
    create_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint    NOT NULL DEFAULT 0,
    CONSTRAINT pk_variant_exam_source PRIMARY KEY (id)
);
COMMENT ON TABLE variant_exam_source IS '变式试卷来源图片';
COMMENT ON COLUMN variant_exam_source.id IS '主键（雪花 ID）';
COMMENT ON COLUMN variant_exam_source.app_id IS '接入方应用标识（从试卷继承）';
COMMENT ON COLUMN variant_exam_source.exam_id IS '生成的变式试卷 ID，关联 exam.id';
COMMENT ON COLUMN variant_exam_source.image_url IS '来源图片 URL（base64 图片不落库）';
COMMENT ON COLUMN variant_exam_source.sort_order IS '图片页码顺序（1 起）';
COMMENT ON COLUMN variant_exam_source.create_time IS '创建时间';
COMMENT ON COLUMN variant_exam_source.update_time IS '更新时间';
COMMENT ON COLUMN variant_exam_source.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_variant_exam_source_exam ON variant_exam_source (exam_id);


-- ============================================================
-- 几何题配图（question_diagram）
-- ============================================================
CREATE TABLE question_diagram (
    id bigint        NOT NULL,
    question_id bigint        NOT NULL,
    sort_order integer           NOT NULL DEFAULT 1,
    url varchar(2000) DEFAULT NULL,
    img_type varchar(20)   NOT NULL DEFAULT 'BASE64',
    tikz_code text          DEFAULT NULL,
    diagram_base64 text    DEFAULT NULL,
    create_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint    NOT NULL DEFAULT 0,
    CONSTRAINT pk_question_diagram PRIMARY KEY (id)
);
COMMENT ON TABLE question_diagram IS '题目配图表（一道题可对应多张图）：AI 生成的 TikZ 配图与拍照录入的题内图共用';
COMMENT ON COLUMN question_diagram.id IS '主键（雪花 ID）';
COMMENT ON COLUMN question_diagram.question_id IS '关联题目 ID（question.id）';
COMMENT ON COLUMN question_diagram.sort_order IS '同题多图时的显示顺序（从 1 开始）';
COMMENT ON COLUMN question_diagram.url IS '题内图 OSS URL；拍照录入场景使用，AI 生成配图场景为 NULL';
COMMENT ON COLUMN question_diagram.img_type IS '图片类型：URL=读 url 列，BASE64=读 diagram_base64 列';
COMMENT ON COLUMN question_diagram.tikz_code IS 'LLM 输出的 TikZ 源码（原始存储，便于重渲染）；拍照录入场景为 NULL';
COMMENT ON COLUMN question_diagram.diagram_base64 IS '渲染结果 base64 PNG；渲染失败时为 NULL';
COMMENT ON COLUMN question_diagram.create_time IS '创建时间';
COMMENT ON COLUMN question_diagram.update_time IS '更新时间';
COMMENT ON COLUMN question_diagram.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_question_diagram_question ON question_diagram (question_id);
CREATE INDEX idx_question_diagram_question_sort ON question_diagram (question_id, sort_order);


-- ============================================================
-- 开放平台
-- ============================================================
CREATE TABLE open_app (
    id bigint          NOT NULL,
    app_id varchar(64)     NOT NULL,
    app_secret varchar(128)    NOT NULL,
    app_name varchar(100)    NOT NULL,
    redirect_uri varchar(512)  DEFAULT NULL,
    remark varchar(500)    DEFAULT NULL,
    status smallint         NOT NULL DEFAULT 1,
    create_time timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint      NOT NULL DEFAULT 0,
    CONSTRAINT pk_open_app PRIMARY KEY (id),
    CONSTRAINT uk_open_app_app_id UNIQUE (app_id)
);
COMMENT ON TABLE open_app IS '开放平台应用凭证表（app_secret 存 BCrypt 哈希，明文仅在创建时返回一次）；redirect_uri 非空表示该应用同时是 OAuth 客户端';
COMMENT ON COLUMN open_app.id IS '主键（雪花 ID，不加 AUTO_INCREMENT）';
COMMENT ON COLUMN open_app.app_id IS '应用标识（公开，格式：app_{uuid}），唯一索引';
COMMENT ON COLUMN open_app.app_secret IS '应用密钥（BCrypt 哈希存储，不可逆，绝不存明文）';
COMMENT ON COLUMN open_app.app_name IS '应用名称（如"某某教育系统"）';
COMMENT ON COLUMN open_app.redirect_uri IS 'OAuth 客户端固定回调地址（非空即标识为 OAuth 客户端，严格逐字符匹配）';
COMMENT ON COLUMN open_app.remark IS '备注（平台内部使用，记录对接方用途/联系人/环境等，不对接方返回）';
COMMENT ON COLUMN open_app.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN open_app.create_time IS '创建时间（自动填充）';
COMMENT ON COLUMN open_app.update_time IS '更新时间（自动填充）';
COMMENT ON COLUMN open_app.deleted IS '逻辑删除（0=正常，1=已删）';


CREATE TABLE open_app_trusted_host (
    id bigint       NOT NULL,
    app_id varchar(64)  NOT NULL,
    host_pattern varchar(255) NOT NULL,
    match_type varchar(16)  NOT NULL,
    status smallint      NOT NULL DEFAULT 1,
    remark varchar(256) DEFAULT NULL,
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint      NOT NULL DEFAULT 0,
    CONSTRAINT pk_open_app_trusted_host PRIMARY KEY (id),
    CONSTRAINT uk_open_app_trusted_host_app_host_pattern UNIQUE (app_id, host_pattern)
);
COMMENT ON TABLE open_app_trusted_host IS '开放应用可信来源域名';
COMMENT ON COLUMN open_app_trusted_host.id IS '主键，雪花 ID';
COMMENT ON COLUMN open_app_trusted_host.app_id IS '开放应用标识';
COMMENT ON COLUMN open_app_trusted_host.host_pattern IS '域名模式：EXACT 为完整 host；SUFFIX 以.开头';
COMMENT ON COLUMN open_app_trusted_host.match_type IS 'EXACT / SUFFIX';
COMMENT ON COLUMN open_app_trusted_host.status IS '1启用 0禁用';
COMMENT ON COLUMN open_app_trusted_host.remark IS '备注';
COMMENT ON COLUMN open_app_trusted_host.create_time IS '创建时间';
COMMENT ON COLUMN open_app_trusted_host.update_time IS '更新时间';
COMMENT ON COLUMN open_app_trusted_host.deleted IS '逻辑删除';
CREATE INDEX idx_open_app_trusted_host_app_status ON open_app_trusted_host (app_id, status);


-- ============================================================
-- 录题异步任务（020_add_question_capture_task.sql）
-- ============================================================
CREATE TABLE question_capture_task (
    id bigint           NOT NULL,
    app_id varchar(64)      DEFAULT NULL,
    external_org_id varchar(64)      DEFAULT NULL,
    external_user_id varchar(64)      DEFAULT NULL,
    external_user_name varchar(128)    DEFAULT NULL,
    status varchar(20)      NOT NULL DEFAULT 'PENDING',
    run_version bigint           NOT NULL DEFAULT 1,
    subject varchar(20)      DEFAULT NULL,
    grade smallint DEFAULT NULL,
    source_images text             DEFAULT NULL,
    source_type varchar(16)      DEFAULT NULL,
    source_document_url text            DEFAULT NULL,
    source_document_name varchar(255)   DEFAULT NULL,
    source_document_mime_type varchar(128) DEFAULT NULL,
    source_document_position integer        DEFAULT NULL,
    source_page_count integer               DEFAULT NULL,
    extra_requirement varchar(500)     DEFAULT NULL,
    generate_answer boolean       NOT NULL DEFAULT false,
    error_message text             DEFAULT NULL,
    start_time timestamp         DEFAULT NULL,
    end_time timestamp         DEFAULT NULL,
    duration_ms bigint           DEFAULT NULL,
    create_time timestamp         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_question_capture_task PRIMARY KEY (id)
);
COMMENT ON TABLE question_capture_task IS '录题异步任务主表（一条记录 = 一次图片解析任务）';
COMMENT ON COLUMN question_capture_task.id IS '主键（雪花 ID），即对外暴露的 taskId';
COMMENT ON COLUMN question_capture_task.app_id IS '接入方应用标识（系统从鉴权上下文填充）';
COMMENT ON COLUMN question_capture_task.external_org_id IS '接入方机构标识（可选，仅记录不筛选）';
COMMENT ON COLUMN question_capture_task.external_user_id IS '操作人标识（接入方传入，任务发起人）';
COMMENT ON COLUMN question_capture_task.external_user_name IS '操作人姓名快照（创建时记录，不随后续改名更新）';
COMMENT ON COLUMN question_capture_task.status IS '状态：PENDING / RUNNING / COMPLETED / FAILED / CANCELLED';
COMMENT ON COLUMN question_capture_task.run_version IS '当前执行轮次，重跑时原子递增，用于隔离旧异步回调';
COMMENT ON COLUMN question_capture_task.subject IS '学科（math / chinese / english），冗余存储便于列表查询';
COMMENT ON COLUMN question_capture_task.grade IS '年级提示 1–12（小学 1–6，初中 7–9，高中 10–12：高一、高二、高三）；NULL=未指定，冗余存储便于列表显示';
COMMENT ON COLUMN question_capture_task.source_images IS '原图引用 JSON 字符串数组，按页码维度存储；格式见 data-model.md §2.2';
COMMENT ON COLUMN question_capture_task.source_type IS '原始来源类型：IMAGE / IMAGE_SINGLE（单题录题）/ DOCUMENT / MIXED';
COMMENT ON COLUMN question_capture_task.source_document_url IS '原始文档受控 URL，仅服务端使用';
COMMENT ON COLUMN question_capture_task.source_document_name IS '原始文档名称';
COMMENT ON COLUMN question_capture_task.source_document_mime_type IS '原始文档 MIME 类型';
COMMENT ON COLUMN question_capture_task.source_document_position IS '原始文档在上传列表中的位置';
COMMENT ON COLUMN question_capture_task.source_page_count IS '原始文档成功展开后的页数';
COMMENT ON COLUMN question_capture_task.extra_requirement IS '创建任务时的额外识别要求快照';
COMMENT ON COLUMN question_capture_task.generate_answer IS '是否在题目识别后生成 AI 答案与解析：0=不生成，1=生成';
COMMENT ON COLUMN question_capture_task.error_message IS '失败或取消时的原因';
COMMENT ON COLUMN question_capture_task.start_time IS '任务开始执行时间（PENDING → RUNNING 时写入）';
COMMENT ON COLUMN question_capture_task.end_time IS '任务终态时间（COMPLETED / FAILED / CANCELLED 时写入）';
COMMENT ON COLUMN question_capture_task.duration_ms IS '运行时长（毫秒，end_time - start_time；start_time 为 NULL 时为 NULL）';
COMMENT ON COLUMN question_capture_task.create_time IS '创建时间';
COMMENT ON COLUMN question_capture_task.update_time IS '更新时间';
COMMENT ON COLUMN question_capture_task.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_question_capture_task_app ON question_capture_task (app_id);
CREATE INDEX idx_question_capture_task_status ON question_capture_task (status);
CREATE INDEX idx_question_capture_task_create_time ON question_capture_task (create_time);


CREATE TABLE question_capture_task_item (
    id bigint       NOT NULL,
    app_id varchar(64)  DEFAULT NULL,
    external_org_id varchar(64)  DEFAULT NULL,
    external_user_id varchar(64)  DEFAULT NULL,
    external_user_name varchar(128) DEFAULT NULL,
    task_id bigint       NOT NULL,
    question_index integer          NOT NULL,
    page_index integer          NOT NULL DEFAULT 1,
    source_number varchar(32)  DEFAULT NULL,
    type varchar(50)  NOT NULL,
    custom_question_type_name varchar(50) DEFAULT NULL,
    custom_question_type_description text DEFAULT NULL,
    subject varchar(20)  DEFAULT NULL,
    content text         NOT NULL,
    options text         DEFAULT NULL,
    answer text         DEFAULT NULL,
    explanation text         DEFAULT NULL,
    knowledge_points text         DEFAULT NULL,
    knowledge_point_ids text         DEFAULT NULL,
    knowledge_point_details text         DEFAULT NULL,
    source varchar(20)  DEFAULT NULL,
    difficulty varchar(10)  DEFAULT NULL,
    question_region text         DEFAULT NULL,
    geometry_regions text         DEFAULT NULL,
    original_answer text         DEFAULT NULL,
    original_explanation text      DEFAULT NULL,
    ai_answer text         DEFAULT NULL,
    ai_explanation text         DEFAULT NULL,
    ai_answer_status varchar(20)  DEFAULT 'pending',
    answer_source varchar(20)  DEFAULT NULL,
    diagram_review_status varchar(32) DEFAULT NULL,
    diagram_review_note varchar(500) DEFAULT NULL,
    diagram_suggested_page_index integer DEFAULT NULL,
    blanks text         DEFAULT NULL,
    sub_questions text         DEFAULT NULL,
    confidence varchar(10)  DEFAULT NULL,
    note varchar(500) DEFAULT NULL,
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint   NOT NULL DEFAULT 0,
    CONSTRAINT pk_question_capture_task_item PRIMARY KEY (id),
    CONSTRAINT uk_question_capture_task_item_task_question_index UNIQUE (task_id, question_index)
);
COMMENT ON COLUMN question_capture_task_item.custom_question_type_name IS '自定义题型名称；标准题型为空';
COMMENT ON COLUMN question_capture_task_item.custom_question_type_description IS '自定义题型定义及要求；最多2000字符，可为空';
COMMENT ON TABLE question_capture_task_item IS '录题异步任务子表（一条记录 = 识别出的一道题草稿）';
COMMENT ON COLUMN question_capture_task_item.id IS '主键（雪花 ID）';
COMMENT ON COLUMN question_capture_task_item.app_id IS '接入方应用标识（从主表继承，便于按 appId 隔离子表查询）';
COMMENT ON COLUMN question_capture_task_item.external_org_id IS '接入方机构标识（从主表继承，仅记录不筛选）';
COMMENT ON COLUMN question_capture_task_item.external_user_id IS '操作人标识（从主表继承）';
COMMENT ON COLUMN question_capture_task_item.external_user_name IS '操作人姓名快照（从主表继承）';
COMMENT ON COLUMN question_capture_task_item.task_id IS '所属任务 ID（关联 question_capture_task.id）';
COMMENT ON COLUMN question_capture_task_item.question_index IS '题目序号（从 1 开始，同一任务内唯一）';
COMMENT ON COLUMN question_capture_task_item.page_index IS '题目文本所在标准页页码';
COMMENT ON COLUMN question_capture_task_item.source_number IS '原图题号（识别 Agent 返回，如 "1"、"2(1)"）';
COMMENT ON COLUMN question_capture_task_item.type IS '题型（choice / fill / multi / short_answer / custom 等）';
COMMENT ON COLUMN question_capture_task_item.subject IS '学科，从请求参数透传';
COMMENT ON COLUMN question_capture_task_item.content IS '题干文本（含 LaTeX 行内公式）';
COMMENT ON COLUMN question_capture_task_item.options IS '选项列表 JSON 字符串；格式见 data-model.md §3.2；仅 choice/multi 有值';
COMMENT ON COLUMN question_capture_task_item.answer IS '答案 JSON 数组字符串；格式 ["A"]、["A","C"]、["TRUE"]、["5"]';
COMMENT ON COLUMN question_capture_task_item.explanation IS '解析说明';
COMMENT ON COLUMN question_capture_task_item.knowledge_points IS '知识点名称 JSON 数组字符串；格式 ["一元二次方程","应用题"]';
COMMENT ON COLUMN question_capture_task_item.knowledge_point_ids IS '受控知识点 ID JSON 字符串数组，与 knowledge_points 顺序一致；格式 ["1900000000000000001"]';
COMMENT ON COLUMN question_capture_task_item.knowledge_point_details IS '知识点匹配详情快照 JSON 数组，含体系、路径与置信度；详情接口直接返回，不做回查兜底';
COMMENT ON COLUMN question_capture_task_item.source IS '题目来源；录题草稿固定 PHOTO';
COMMENT ON COLUMN question_capture_task_item.difficulty IS '难度：easy / medium / hard；无法判断时为 NULL';
COMMENT ON COLUMN question_capture_task_item.question_region IS '整题区域 JSON 字符串（原图 0-1 坐标）；含 pageIndex 引用主表 source_images 页码；格式见 data-model.md §3.3';
COMMENT ON COLUMN question_capture_task_item.geometry_regions IS '题内图区域 JSON 数组字符串（原图 0-1 坐标）；含 pageIndex 引用主表 source_images 页码';
COMMENT ON COLUMN question_capture_task_item.original_answer IS '原题图片识别答案 JSON';
COMMENT ON COLUMN question_capture_task_item.original_explanation IS '原题图片识别解析';
COMMENT ON COLUMN question_capture_task_item.ai_answer IS 'AI 生成答案 JSON';
COMMENT ON COLUMN question_capture_task_item.ai_explanation IS 'AI 生成解析';
COMMENT ON COLUMN question_capture_task_item.ai_answer_status IS 'AI 答案生成状态：pending/generating/done/failed/skipped（skipped 含任务取消后不再生成）';
COMMENT ON COLUMN question_capture_task_item.answer_source IS '答案来源建议：ORIGINAL/AI/CUSTOM';
COMMENT ON COLUMN question_capture_task_item.diagram_review_status IS '题内图二次确认状态';
COMMENT ON COLUMN question_capture_task_item.diagram_review_note IS '题内图二次确认说明';
COMMENT ON COLUMN question_capture_task_item.diagram_suggested_page_index IS '题内图复核建议优先查看的标准页页码';
COMMENT ON COLUMN question_capture_task_item.blanks IS '填空题空位结构 JSON';
COMMENT ON COLUMN question_capture_task_item.sub_questions IS '题组子题结构 JSON';
COMMENT ON COLUMN question_capture_task_item.confidence IS '整题置信度：high / medium / low';
COMMENT ON COLUMN question_capture_task_item.note IS '复核备注（如「题内图未定位，请在前端手动框选」）';
COMMENT ON COLUMN question_capture_task_item.create_time IS '创建时间';
COMMENT ON COLUMN question_capture_task_item.update_time IS '更新时间';
COMMENT ON COLUMN question_capture_task_item.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_question_capture_task_item_app ON question_capture_task_item (app_id);


-- ============================================================
-- 单题变式来源快照（variant_question_source）（025_add_variant_question_source.sql）
-- ============================================================
CREATE TABLE variant_question_source (
    id bigint           NOT NULL,
    task_id bigint           NOT NULL,
    question_id bigint           DEFAULT NULL,
    app_id varchar(64)      NOT NULL,
    external_user_id varchar(64)      DEFAULT NULL,
    external_org_id varchar(64)      DEFAULT NULL,
    subject varchar(20)      DEFAULT NULL,
    grade smallint DEFAULT NULL,
    question_type varchar(50)      DEFAULT NULL,
    custom_question_type_name varchar(50) DEFAULT NULL,
    custom_question_type_description text DEFAULT NULL,
    original_content text             DEFAULT NULL,
    original_options_json text             DEFAULT NULL,
    original_answer_json text             DEFAULT NULL,
    original_explanation text             DEFAULT NULL,
    original_diagrams_json text             DEFAULT NULL,
    image_sources_json text             DEFAULT NULL,
    question_image_json text             DEFAULT NULL,
    difficulty varchar(20)      DEFAULT NULL,
    extra_requirements text             DEFAULT NULL,
    create_time timestamp         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_variant_question_source PRIMARY KEY (id),
    CONSTRAINT uk_variant_question_source_task UNIQUE (task_id)
);
COMMENT ON COLUMN variant_question_source.custom_question_type_name IS '自定义题型名称；标准题型为空';
COMMENT ON COLUMN variant_question_source.custom_question_type_description IS '自定义题型定义及要求；最多2000字符，可为空';
COMMENT ON TABLE variant_question_source IS '单题变式来源快照';
COMMENT ON COLUMN variant_question_source.id IS '主键（雪花 ID）';
COMMENT ON COLUMN variant_question_source.task_id IS '生成任务 ID，关联 generation_task.id';
COMMENT ON COLUMN variant_question_source.question_id IS '生成成功后的变式题 ID，关联 question.id；失败时为空';
COMMENT ON COLUMN variant_question_source.app_id IS '接入方应用标识（数据隔离）';
COMMENT ON COLUMN variant_question_source.external_user_id IS '操作人标识';
COMMENT ON COLUMN variant_question_source.external_org_id IS '接入方机构标识';
COMMENT ON COLUMN variant_question_source.subject IS '最终使用的学科（接入方传入或 AI 推断）；推断失败时为空';
COMMENT ON COLUMN variant_question_source.grade IS '最终使用的年级（接入方传入或 AI 推断）；推断失败时为空';
COMMENT ON COLUMN variant_question_source.question_type IS '最终使用的题型（接入方传入或 AI 推断）；推断失败时为空';
COMMENT ON COLUMN variant_question_source.original_content IS '原题题干快照';
COMMENT ON COLUMN variant_question_source.original_options_json IS '原题选项 JSON 字符串';
COMMENT ON COLUMN variant_question_source.original_answer_json IS '原题答案 JSON 字符串';
COMMENT ON COLUMN variant_question_source.original_explanation IS '原题解析快照';
COMMENT ON COLUMN variant_question_source.original_diagrams_json IS '原题 TikZ 配图 JSON 字符串';
COMMENT ON COLUMN variant_question_source.image_sources_json IS '图片来源摘要 JSON 字符串（base64 不记录正文）';
COMMENT ON COLUMN variant_question_source.question_image_json IS '原题截图来源摘要 JSON（URL 类型记录 URL，base64 仅记录类型；文本模式为 null）';
COMMENT ON COLUMN variant_question_source.difficulty IS '目标难度';
COMMENT ON COLUMN variant_question_source.extra_requirements IS '补充变式要求';
COMMENT ON COLUMN variant_question_source.create_time IS '创建时间';
COMMENT ON COLUMN variant_question_source.update_time IS '更新时间';
COMMENT ON COLUMN variant_question_source.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_variant_question_source_question ON variant_question_source (question_id);
CREATE INDEX idx_variant_question_source_app_question ON variant_question_source (app_id, question_id);
CREATE INDEX idx_variant_question_source_app_create_time ON variant_question_source (app_id, create_time);


-- ============================================================
-- Agent 生图工具调用记录（agent_image_generate_record）（026_add_agent_image_generate_record.sql）
-- ============================================================
CREATE TABLE agent_image_generate_record (
    id bigint           NOT NULL,
    app_id varchar(64)      DEFAULT NULL,
    task_id bigint           DEFAULT NULL,
    task_type varchar(64)      DEFAULT NULL,
    agent_name varchar(128)     NOT NULL,
    tool_name varchar(64)      NOT NULL DEFAULT 'image_generate',
    provider varchar(32)      NOT NULL,
    model varchar(128)     NOT NULL,
    status varchar(32)      NOT NULL,
    prompt_summary text             DEFAULT NULL,
    prompt_hash varchar(64)      DEFAULT NULL,
    negative_prompt_summary text             DEFAULT NULL,
    size varchar(32)      DEFAULT NULL,
    requested_count integer              NOT NULL DEFAULT 0,
    generated_count integer              NOT NULL DEFAULT 0,
    uploaded_count integer              NOT NULL DEFAULT 0,
    reference_image_count integer              NOT NULL DEFAULT 0,
    seed integer              DEFAULT NULL,
    duration_ms bigint           NOT NULL DEFAULT 0,
    provider_duration_ms bigint           DEFAULT NULL,
    storage_duration_ms bigint           DEFAULT NULL,
    started_at timestamp(3)      NOT NULL,
    ended_at timestamp(3)      NOT NULL,
    image_urls_json text             DEFAULT NULL,
    temporary_urls_count integer              NOT NULL DEFAULT 0,
    error_code varchar(128)     DEFAULT NULL,
    error_message varchar(1024)    DEFAULT NULL,
    create_time timestamp(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp(3)      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint       NOT NULL DEFAULT 0,
    CONSTRAINT pk_agent_image_generate_record PRIMARY KEY (id)
);
COMMENT ON TABLE agent_image_generate_record IS 'Agent生图工具调用记录';
COMMENT ON COLUMN agent_image_generate_record.id IS '主键ID（雪花ID）';
COMMENT ON COLUMN agent_image_generate_record.app_id IS '接入方ID';
COMMENT ON COLUMN agent_image_generate_record.task_id IS '业务任务ID';
COMMENT ON COLUMN agent_image_generate_record.task_type IS '业务任务类型';
COMMENT ON COLUMN agent_image_generate_record.agent_name IS '调用工具的Agent名称';
COMMENT ON COLUMN agent_image_generate_record.tool_name IS '工具名称';
COMMENT ON COLUMN agent_image_generate_record.provider IS '生图供应商：dashscope/openai';
COMMENT ON COLUMN agent_image_generate_record.model IS '生图模型名称';
COMMENT ON COLUMN agent_image_generate_record.status IS '调用状态：SUCCESS/FAILED';
COMMENT ON COLUMN agent_image_generate_record.prompt_summary IS '提示词摘要（截断）';
COMMENT ON COLUMN agent_image_generate_record.prompt_hash IS '提示词SHA-256哈希';
COMMENT ON COLUMN agent_image_generate_record.negative_prompt_summary IS '反向提示词摘要（截断）';
COMMENT ON COLUMN agent_image_generate_record.size IS '请求图片尺寸，如1024x1024';
COMMENT ON COLUMN agent_image_generate_record.requested_count IS '请求生图张数（n）';
COMMENT ON COLUMN agent_image_generate_record.generated_count IS '供应商返回张数';
COMMENT ON COLUMN agent_image_generate_record.uploaded_count IS '成功上传到自有存储张数';
COMMENT ON COLUMN agent_image_generate_record.reference_image_count IS '参考图数量';
COMMENT ON COLUMN agent_image_generate_record.seed IS '随机种子';
COMMENT ON COLUMN agent_image_generate_record.duration_ms IS '总耗时毫秒';
COMMENT ON COLUMN agent_image_generate_record.provider_duration_ms IS '供应商调用耗时毫秒';
COMMENT ON COLUMN agent_image_generate_record.storage_duration_ms IS '下载和上传耗时毫秒';
COMMENT ON COLUMN agent_image_generate_record.started_at IS '工具开始时间';
COMMENT ON COLUMN agent_image_generate_record.ended_at IS '工具结束时间';
COMMENT ON COLUMN agent_image_generate_record.image_urls_json IS '自有图片URL列表JSON';
COMMENT ON COLUMN agent_image_generate_record.temporary_urls_count IS '供应商临时图片数量';
COMMENT ON COLUMN agent_image_generate_record.error_code IS '错误码';
COMMENT ON COLUMN agent_image_generate_record.error_message IS '错误摘要（截断）';
COMMENT ON COLUMN agent_image_generate_record.create_time IS '创建时间';
COMMENT ON COLUMN agent_image_generate_record.update_time IS '更新时间';
COMMENT ON COLUMN agent_image_generate_record.deleted IS '逻辑删除（0=正常，1=已删）';
CREATE INDEX idx_agent_image_generate_record_app_task ON agent_image_generate_record (app_id, task_id);
CREATE INDEX idx_agent_image_generate_record_provider_model ON agent_image_generate_record (provider, model);
CREATE INDEX idx_agent_image_generate_record_started_at ON agent_image_generate_record (started_at);
CREATE INDEX idx_agent_image_generate_record_status ON agent_image_generate_record (status);


-- ============================================================
-- 开放平台长期 API Key（042_open_api_key.sql）
-- ============================================================

CREATE TABLE open_api_key (
    id bigint       NOT NULL,
    app_id varchar(64)  NOT NULL,
    key_hash varchar(128) NOT NULL,
    key_prefix varchar(8)   NOT NULL DEFAULT 'ak_',
    name varchar(128) NOT NULL DEFAULT '',
    status smallint      NOT NULL DEFAULT 1,
    revoked_at timestamp     NULL,
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint      NOT NULL DEFAULT 0,
    CONSTRAINT pk_open_api_key PRIMARY KEY (id)
);
COMMENT ON TABLE open_api_key IS '开放平台长期 API Key';
COMMENT ON COLUMN open_api_key.id IS '雪花 ID，主键';
COMMENT ON COLUMN open_api_key.app_id IS '开放应用 ID';
COMMENT ON COLUMN open_api_key.key_hash IS 'API Key BCrypt 哈希（完整 Key 不存）';
COMMENT ON COLUMN open_api_key.key_prefix IS 'Key 前缀，固定 ak_';
COMMENT ON COLUMN open_api_key.name IS 'Key 名称（如 prod-key-1、auto-generated）';
COMMENT ON COLUMN open_api_key.status IS '状态：1=启用，0=吊销';
COMMENT ON COLUMN open_api_key.revoked_at IS '吊销时间（NULL=未吊销）';
COMMENT ON COLUMN open_api_key.create_time IS '创建时间';
COMMENT ON COLUMN open_api_key.update_time IS '更新时间';
COMMENT ON COLUMN open_api_key.deleted IS '逻辑删除：0=正常，1=已删';
CREATE INDEX idx_open_api_key_app ON open_api_key (app_id);
CREATE INDEX idx_open_api_key_app_status ON open_api_key (app_id, status);


-- ============================================================
-- 公共可恢复异步任务（057_async_task_runtime.sql）
-- ============================================================
-- ============================================================
-- 057_async_task_runtime.sql
-- cx-backend 公共可恢复异步任务：任务真相、步骤检查点与 Transactional Outbox。
-- 首期接入普通录题和单题录题；Redis 仅负责分发，PostgreSQL 为持久化真相。
-- ============================================================

CREATE TABLE async_task (
    id bigint NOT NULL,
    app_id varchar(64) NOT NULL,
    external_org_id varchar(64) DEFAULT NULL,
    external_user_id varchar(64) DEFAULT NULL,
    external_user_name varchar(128) DEFAULT NULL,
    task_type varchar(64) NOT NULL,
    definition_version integer NOT NULL DEFAULT 1,
    status varchar(20) NOT NULL DEFAULT 'PENDING',
    run_version bigint NOT NULL DEFAULT 1,
    current_phase varchar(64) DEFAULT NULL,
    progress_current integer NOT NULL DEFAULT 0,
    progress_total integer NOT NULL DEFAULT 0,
    progress_percent smallint NOT NULL DEFAULT 0,
    error_code varchar(128) DEFAULT NULL,
    error_message text DEFAULT NULL,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_async_task PRIMARY KEY (id),
    CONSTRAINT ck_async_task_definition_version CHECK (definition_version >= 1),
    CONSTRAINT ck_async_task_run_version CHECK (run_version >= 1),
    CONSTRAINT ck_async_task_progress CHECK (progress_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_async_task_status CHECK (
        status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')
    )
);
COMMENT ON TABLE async_task IS 'cx-backend 公共可恢复异步任务主表';
COMMENT ON COLUMN async_task.id IS '雪花 ID；录题首期与 question_capture_task.id 共用 taskId';
COMMENT ON COLUMN async_task.app_id IS '接入方应用标识，租户隔离键';
COMMENT ON COLUMN async_task.task_type IS '任务类型：首期 QUESTION_CAPTURE_BATCH/QUESTION_CAPTURE_SINGLE';
COMMENT ON COLUMN async_task.definition_version IS '持久化 Handler 定义版本';
COMMENT ON COLUMN async_task.status IS 'PENDING/RUNNING/COMPLETED/FAILED/CANCELLED';
COMMENT ON COLUMN async_task.run_version IS '执行轮次，用于隔离 rerun 的旧回调';
COMMENT ON COLUMN async_task.current_phase IS '最近可靠提交的业务阶段';
COMMENT ON COLUMN async_task.progress_percent IS '最近可靠进度百分比，0-100';
COMMENT ON COLUMN async_task.error_code IS '安全、可检索的错误分类';
COMMENT ON COLUMN async_task.error_message IS '面向用户的安全错误摘要';
CREATE INDEX idx_async_task_app_status_create
    ON async_task (app_id, status, create_time DESC);
CREATE INDEX idx_async_task_status_update
    ON async_task (status, update_time, id);
CREATE INDEX idx_async_task_type_create
    ON async_task (task_type, create_time DESC);

CREATE TABLE async_task_step (
    id bigint NOT NULL,
    app_id varchar(64) NOT NULL,
    task_id bigint NOT NULL,
    run_version bigint NOT NULL,
    step_key varchar(128) NOT NULL,
    step_type varchar(64) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'PENDING',
    claim_version bigint NOT NULL,
    active_message_id varchar(64) DEFAULT NULL,
    claimed_by varchar(128) DEFAULT NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    max_attempts integer NOT NULL,
    failure_policy varchar(20) NOT NULL DEFAULT 'FAIL_TASK',
    input_payload text DEFAULT NULL,
    result_payload text DEFAULT NULL,
    error_code varchar(128) DEFAULT NULL,
    error_message text DEFAULT NULL,
    start_time timestamp DEFAULT NULL,
    end_time timestamp DEFAULT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_async_task_step PRIMARY KEY (id),
    CONSTRAINT uk_async_task_step_task_run_key UNIQUE (task_id, run_version, step_key),
    CONSTRAINT ck_async_task_step_run_version CHECK (run_version >= 1),
    CONSTRAINT ck_async_task_step_claim_version CHECK (claim_version >= 1),
    CONSTRAINT ck_async_task_step_attempts CHECK (
        attempt_count >= 0 AND max_attempts >= 1
    ),
    CONSTRAINT ck_async_task_step_status CHECK (
        status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT ck_async_task_step_failure_policy CHECK (
        failure_policy IN ('FAIL_TASK', 'CONTINUE_TASK')
    )
);
COMMENT ON TABLE async_task_step IS '公共异步任务可独立恢复的步骤与检查点';
COMMENT ON COLUMN async_task_step.step_key IS '轮次内确定性唯一键';
COMMENT ON COLUMN async_task_step.step_type IS 'Handler 路由类型';
COMMENT ON COLUMN async_task_step.claim_version IS '逻辑分发与执行资格 fencing 版本';
COMMENT ON COLUMN async_task_step.active_message_id IS '当前唯一有效 Redis Stream Message ID';
COMMENT ON COLUMN async_task_step.claimed_by IS '当前 Redis consumer 实例标识';
COMMENT ON COLUMN async_task_step.failure_policy IS 'FAIL_TASK/CONTINUE_TASK';
COMMENT ON COLUMN async_task_step.input_payload IS '稳定输入 JSON，不允许 Base64 或临时文件作为唯一输入';
COMMENT ON COLUMN async_task_step.result_payload IS '已可靠提交的步骤检查点 JSON';
CREATE INDEX idx_async_task_step_task_status
    ON async_task_step (task_id, run_version, status);
CREATE INDEX idx_async_task_step_status_update
    ON async_task_step (status, update_time, id);
CREATE INDEX idx_async_task_step_app_task
    ON async_task_step (app_id, task_id);

CREATE TABLE async_task_outbox (
    id bigint NOT NULL,
    app_id varchar(64) NOT NULL,
    task_id bigint NOT NULL,
    step_id bigint NOT NULL,
    run_version bigint NOT NULL,
    claim_version bigint NOT NULL,
    dedup_key varchar(256) NOT NULL,
    payload text NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'PENDING',
    attempt_count integer NOT NULL DEFAULT 0,
    next_publish_time timestamp NOT NULL,
    published_message_id varchar(64) DEFAULT NULL,
    published_time timestamp DEFAULT NULL,
    last_error text DEFAULT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_async_task_outbox PRIMARY KEY (id),
    CONSTRAINT uk_async_task_outbox_dedup UNIQUE (dedup_key),
    CONSTRAINT ck_async_task_outbox_run_version CHECK (run_version >= 1),
    CONSTRAINT ck_async_task_outbox_claim_version CHECK (claim_version >= 1),
    CONSTRAINT ck_async_task_outbox_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT ck_async_task_outbox_status CHECK (
        status IN ('PENDING', 'PUBLISHED')
    )
);
COMMENT ON TABLE async_task_outbox IS 'PostgreSQL 到 Redis Stream 的 Transactional Outbox';
COMMENT ON COLUMN async_task_outbox.dedup_key IS 'taskId:runVersion:stepKey:claimVersion 逻辑分发键';
COMMENT ON COLUMN async_task_outbox.payload IS '只含寻址和 fencing 字段的 Redis 消息 JSON';
COMMENT ON COLUMN async_task_outbox.attempt_count IS 'Redis 发布尝试次数，不等于业务步骤尝试次数';
CREATE INDEX idx_async_task_outbox_due
    ON async_task_outbox (status, next_publish_time, id);
CREATE INDEX idx_async_task_outbox_step
    ON async_task_outbox (step_id, claim_version, create_time DESC);
CREATE INDEX idx_async_task_outbox_task
    ON async_task_outbox (task_id, run_version);

CREATE TABLE token_usage (
    id bigint NOT NULL,
    app_id varchar(64) NOT NULL,
    external_user_id varchar(128) DEFAULT NULL,
    external_user_name varchar(256) DEFAULT NULL,
    external_org_id varchar(128) DEFAULT NULL,
    biz_type varchar(64) NOT NULL,
    biz_stage varchar(64) DEFAULT NULL,
    biz_id varchar(64) DEFAULT NULL,
    session_id varchar(64) DEFAULT NULL,
    run_id varchar(64) DEFAULT NULL,
    agent_id varchar(128) DEFAULT NULL,
    model_provider varchar(32) NOT NULL,
    model_name varchar(128) NOT NULL,
    input_tokens bigint NOT NULL DEFAULT 0,
    output_tokens bigint NOT NULL DEFAULT 0,
    cached_tokens bigint NOT NULL DEFAULT 0,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_token_usage PRIMARY KEY (id),
    CONSTRAINT ck_token_usage_tokens CHECK (
        input_tokens >= 0 AND output_tokens >= 0 AND cached_tokens >= 0
    )
);
COMMENT ON TABLE token_usage IS '文本模型调用 token 消耗事实（含 embedding），按 app_id 隔离';
COMMENT ON COLUMN token_usage.app_id IS '接入方应用标识，取自鉴权上下文';
COMMENT ON COLUMN token_usage.external_user_id IS '操作人标识（接入方业务请求提供，原样归因）';
COMMENT ON COLUMN token_usage.external_user_name IS '操作人姓名快照（创建写入，不随改名更新）';
COMMENT ON COLUMN token_usage.external_org_id IS '接入方机构标识（仅归因记录，不做隔离）';
COMMENT ON COLUMN token_usage.biz_type IS '业务类型：question_capture/exam_generation/single_question_generation/variant_generation/knowledge_match/knowledge_embedding/question_difficulty/question_explanation';
COMMENT ON COLUMN token_usage.biz_stage IS '业务阶段：recognition/answer，可空';
COMMENT ON COLUMN token_usage.biz_id IS '业务单据标识（如录题任务ID、试卷ID）';
COMMENT ON COLUMN token_usage.session_id IS 'Agent 会话标识（异步任务链路可空）';
COMMENT ON COLUMN token_usage.run_id IS 'Agent 运行标识';
COMMENT ON COLUMN token_usage.agent_id IS 'Agent 标识/名称';
COMMENT ON COLUMN token_usage.model_provider IS '实际生效供应方：openai/anthropic/dashscope';
COMMENT ON COLUMN token_usage.model_name IS '实际生效模型名（备选切换后为备用模型）';
COMMENT ON COLUMN token_usage.input_tokens IS '输入 token 数';
COMMENT ON COLUMN token_usage.output_tokens IS '输出 token 数（embedding 记录恒为 0）';
COMMENT ON COLUMN token_usage.cached_tokens IS '缓存命中 token 数（input 子集，供应商未上报记 0）';
CREATE INDEX idx_token_usage_app_time ON token_usage (app_id, create_time, id);
CREATE INDEX idx_token_usage_app_biz ON token_usage (app_id, biz_type, create_time);
CREATE INDEX idx_token_usage_app_org ON token_usage (app_id, external_org_id, create_time);
CREATE INDEX idx_token_usage_app_user ON token_usage (app_id, external_user_id, create_time);

CREATE TABLE image_usage (
    id bigint NOT NULL,
    app_id varchar(64) NOT NULL,
    external_user_id varchar(128) DEFAULT NULL,
    external_user_name varchar(256) DEFAULT NULL,
    external_org_id varchar(128) DEFAULT NULL,
    biz_type varchar(64) NOT NULL,
    biz_id varchar(64) DEFAULT NULL,
    task_id varchar(64) DEFAULT NULL,
    agent_name varchar(128) DEFAULT NULL,
    provider varchar(32) NOT NULL,
    model varchar(128) NOT NULL,
    size varchar(32) DEFAULT NULL,
    image_count integer NOT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted smallint NOT NULL DEFAULT 0,
    CONSTRAINT pk_image_usage PRIMARY KEY (id),
    CONSTRAINT ck_image_usage_count CHECK (image_count > 0)
);
COMMENT ON TABLE image_usage IS '图像生成消耗事实（按张数计费），按 app_id 隔离';
COMMENT ON COLUMN image_usage.app_id IS '接入方应用标识，取自鉴权上下文';
COMMENT ON COLUMN image_usage.external_user_id IS '操作人标识（接入方业务请求提供，原样归因）';
COMMENT ON COLUMN image_usage.external_user_name IS '操作人姓名快照（创建写入，不随改名更新）';
COMMENT ON COLUMN image_usage.external_org_id IS '接入方机构标识（仅归因记录，不做隔离）';
COMMENT ON COLUMN image_usage.biz_type IS '业务类型，值域同 token_usage.biz_type';
COMMENT ON COLUMN image_usage.biz_id IS '业务单据标识';
COMMENT ON COLUMN image_usage.task_id IS '生图所属任务标识';
COMMENT ON COLUMN image_usage.agent_name IS '发起生图的 Agent 名称';
COMMENT ON COLUMN image_usage.provider IS '生图供应方：dashscope/openai';
COMMENT ON COLUMN image_usage.model IS '生图模型（如 wan2.7-image / dall-e-3）';
COMMENT ON COLUMN image_usage.size IS '输出分辨率（如 1024x1024），计费相关';
COMMENT ON COLUMN image_usage.image_count IS '成功生成并持久化的图片张数（>0）';
CREATE INDEX idx_image_usage_app_time ON image_usage (app_id, create_time, id);
CREATE INDEX idx_image_usage_app_biz ON image_usage (app_id, biz_type, create_time);
CREATE INDEX idx_image_usage_app_org ON image_usage (app_id, external_org_id, create_time);
CREATE INDEX idx_image_usage_app_user ON image_usage (app_id, external_user_id, create_time);
