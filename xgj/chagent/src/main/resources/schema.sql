create table if not exists agent_session (
    session_id varchar(128) primary key,
    user_id bigint null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    key idx_agent_session_user_id_updated_at (user_id, updated_at)
);

create table if not exists agent_definition (
    agent_id varchar(128) primary key,
    name varchar(255) not null,
    system_prompt text not null,
    agents_md text null,
    max_iterations integer not null,
    skill_ids_json varchar(2048) not null default '[]',
    enabled boolean not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table if not exists sys_user (
    id bigint auto_increment primary key,
    username varchar(128) not null unique,
    password_hash varchar(255) not null,
    role varchar(32) not null,
    enabled boolean not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table if not exists agent_skill (
    id bigint auto_increment primary key,
    name varchar(255) not null unique,
    description text not null,
    skill_content longtext not null,
    source varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table if not exists agent_skill_resource (
    id bigint not null,
    resource_path varchar(500) not null,
    resource_content longtext not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (id, resource_path),
    constraint fk_agent_skill_resource_skill
        foreign key (id) references agent_skill(id)
            on delete cascade
);

create table if not exists user_third_party_credential (
    id bigint auto_increment primary key,
    user_id bigint not null,
    credential_id varchar(128) not null,
    name varchar(255) not null,
    description text,
    headers_json_encrypted text not null,
    enabled boolean not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    unique key uk_user_third_party_credential (user_id, credential_id)
);

create table if not exists user_remote_source_credential_binding (
    id bigint auto_increment primary key,
    user_id bigint not null,
    source_id varchar(128) not null,
    credential_id varchar(128) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    unique key uk_user_remote_source_credential_binding (user_id, source_id)
);

create table if not exists user_api_key (
    id bigint auto_increment primary key,
    user_id bigint not null,
    key_prefix varchar(32) not null,
    key_hash varchar(255) not null,
    expires_at timestamp null,
    permanent boolean not null,
    enabled boolean not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    unique key uk_user_api_key_user_id (user_id),
    key idx_user_api_key_prefix (key_prefix)
);

create table if not exists agent_session_state (
    session_id varchar(128) not null,
    state_key varchar(128) not null,
    state_scope varchar(16) not null,
    list_index integer not null default 0,
    class_name varchar(255) not null,
    content_json longtext not null,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (session_id, state_key, state_scope, list_index)
);

create table if not exists session_a2ui_surface (
    session_id varchar(128) not null,
    surface_id varchar(128) not null,
    catalog_id varchar(128) not null,
    components_json longtext not null,
    data_model_json longtext not null,
    last_updated_msg_id varchar(128),
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (session_id, surface_id),
    key idx_session_a2ui_surface_msg (session_id, last_updated_msg_id),
    key idx_session_a2ui_surface_updated_at (session_id, updated_at)
);

create table if not exists session_message_index (
    session_id varchar(128) not null,
    message_id varchar(128) not null,
    role varchar(16) not null,
    content longtext,
    message_order integer not null,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (session_id, message_id),
    unique key uk_session_message_index_order (session_id, message_order),
    key idx_session_message_index_page (session_id, message_order)
);

create table if not exists session_history_meta (
    session_id varchar(128) not null,
    total_count integer not null,
    max_message_order integer null,
    last_message_id varchar(128) null,
    checksum_sum bigint not null,
    checksum_xor bigint not null,
    source_updated_at timestamp null,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    primary key (session_id)
);

create table if not exists agent_run (
    run_id varchar(128) primary key,
    session_id varchar(128) not null,
    status varchar(32) not null,
    owner_node_id varchar(128) null,
    generate_reason varchar(64),
    finished_reason varchar(255) null,
    error_message text,
    last_event_seq bigint not null default 0,
    last_heartbeat_at timestamp null default null,
    started_at timestamp not null default current_timestamp,
    finished_at timestamp null default null,
    key idx_agent_run_session_status (session_id, status),
    key idx_agent_run_owner_status (owner_node_id, status),
    key idx_agent_run_session_started_at (session_id, started_at)
);

create table if not exists agent_tool_audit (
    id bigint auto_increment primary key,
    run_id varchar(128) not null,
    tool_name varchar(128) not null,
    tool_args_json mediumtext,
    tool_result_json mediumtext,
    status varchar(32) not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists session_active_skill (
    session_id varchar(128) not null,
    skill_id varchar(128) not null,
    activated_at timestamp not null default current_timestamp,
    primary key (session_id, skill_id)
);

create table if not exists mcp_server (
    server_id varchar(128) primary key,
    name varchar(255) not null,
    transport_type varchar(16) not null,
    enabled boolean not null,
    config_json text,
    headers_json text,
    query_params_json text,
    timeout_seconds bigint not null default 60,
    initialization_timeout_seconds bigint not null default 30
);

create table if not exists skill_mcp_binding (
    skill_id varchar(128) not null,
    server_id varchar(128) not null,
    enable_tools_json text,
    disable_tools_json text,
    enabled boolean not null,
    primary key (skill_id, server_id)
);

create table if not exists skill_local_tool_binding (
    skill_id varchar(128) not null,
    tool_name varchar(128) not null,
    enabled boolean not null,
    primary key (skill_id, tool_name)
);

create table if not exists remote_tool_source (
    source_id varchar(128) primary key,
    name varchar(255) not null,
    meta_url varchar(1024),
    inline_content text,
    enabled boolean not null,
    headers_json text,
    timeout_seconds bigint not null default 10,
    refresh_interval_seconds bigint not null default 300,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table if not exists skill_remote_tool_binding (
    skill_id varchar(128) not null,
    source_id varchar(128) not null,
    tool_name varchar(128) not null,
    enabled boolean not null,
    primary key (skill_id, source_id, tool_name)
);

-- Token 消耗明细表，每次 agent.call() 写入一条记录，用于按用户/Agent/模型计费
create table if not exists token_usage (
    id             bigint auto_increment primary key,
    user_id        bigint        not null,           -- 用户 ID，关联 sys_user.id
    session_id     varchar(128)  not null,           -- 会话 ID，关联 agent_session.session_id
    run_id         varchar(128)  not null,           -- 运行 ID，关联 agent_run.run_id
    agent_id       varchar(128)  not null,           -- Agent 标识，来自 agent_definition.agent_id
    model_provider varchar(64)   not null,           -- 模型供应商（anthropic / openai）
    model_name     varchar(255)  not null,           -- 模型名称（具体型号）
    input_tokens   bigint        not null default 0, -- 累计输入 token 数（一次 call 内所有 LLM 推理之和）
    output_tokens  bigint        not null default 0, -- 累计输出 token 数（一次 call 内所有 LLM 推理之和）
    created_at     timestamp     not null default current_timestamp,
    key idx_token_usage_user_id (user_id, created_at),
    key idx_token_usage_run_id  (run_id)
);

-- 图片生成用量明细表，每次图片生成工具成功执行后写入一条记录，用于按张计费与对账
create table if not exists image_generate_usage (
    id           bigint auto_increment primary key,
    user_id      bigint        not null,           -- 用户 ID，关联 sys_user.id
    session_id   varchar(128)  not null,           -- 会话 ID，关联 agent_session.session_id
    run_id       varchar(128)  not null,           -- 运行 ID，关联 agent_run.run_id
    provider     varchar(64)   not null,           -- 图片供应商（dashscope / openai）
    model        varchar(255)  not null,           -- 图片模型名称（wan2.7-image / dall-e-3 等）
    size         varchar(32)   not null,           -- 请求尺寸，如 1024x1024
    image_count  int           not null default 1, -- 实际成功上传并返回的图片张数
    created_at   timestamp     not null default current_timestamp,
    key idx_image_generate_usage_user_id (user_id, created_at),
    key idx_image_generate_usage_run_id  (run_id)
);
