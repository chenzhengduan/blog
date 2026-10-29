# HTTP 路由源码索引

此索引记录当前 Java Controller 的映射注解位置。类级与方法级路径需组合；CX 还需加 `/cx`，经过网关再加 `/api`。不是运行时 OpenAPI，也不包含全部 WebSocket 或 RouterFunction。

| 仓库 | 文件及行号 | 注解 |
|---|---|---|
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentExamController.java:35` | `@RequestMapping("/aiagent/exams")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentExamController.java:66` | `@PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQueryController.java:51` | `@RequestMapping("/aiagent")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQueryController.java:98` | `@GetMapping("/exams")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQueryController.java:132` | `@GetMapping("/exams/detail")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQueryController.java:160` | `@GetMapping("/questions")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQuestionCaptureController.java:38` | `@RequestMapping("/aiagent/question-capture-tasks")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQuestionCaptureController.java:60` | `@PostMapping("/create")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQuestionCaptureController.java:76` | `@GetMapping("/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentQuestionCaptureController.java:87` | `@DeleteMapping("/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentVariantController.java:37` | `@RequestMapping("/aiagent/variants")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentVariantController.java:61` | `@PostMapping(value = "/exams/from-exam/{examId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentVariantController.java:71` | `@PostMapping(value = "/exams/from-images", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentVariantController.java:83` | `@PostMapping(value = "/questions/from-text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/aiagent/AiagentVariantController.java:98` | `@PostMapping(value = "/questions/from-image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/AiagentEnterChatController.java:24` | `@RequestMapping("/aiagent")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/AiagentEnterChatController.java:42` | `@PostMapping("/enter-chat")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoDiagramDetectController.java:42` | `@RequestMapping("demo/diagram-detect")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoDiagramDetectController.java:64` | `@PostMapping` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoImageAgentController.java:49` | `@PostMapping(value = "/demo/image-agent/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoOssController.java:34` | `@RequestMapping("demo/oss")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoOssController.java:54` | `@PostMapping("/getStsInfo")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoQuestionCaptureTaskController.java:84` | `@PostMapping(value = "/demo/question-capture-tasks/{taskId}/rerun",` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoQuestionCaptureTaskController.java:92` | `@PostMapping("/demo/question-capture-tasks/{taskId}/items/{questionIndex}/ai-answer/retry")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoQuestionCaptureTaskController.java:104` | `@GetMapping("/demo/question-capture-tasks/{taskId}/source-images/{pageIndex}/content")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoQuestionCaptureTaskController.java:118` | `@GetMapping("/demo/question-capture-tasks/list")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/demo/DemoQuestionCaptureTaskController.java:135` | `@PostMapping("/demo/question-capture-tasks/items/confirm")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/DocumentExportController.java:45` | `@PostMapping("/exports/pdf")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/DocumentExportController.java:71` | `@PostMapping("/exports/word")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamController.java:155` | `@PostMapping(value = "/question", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamController.java:210` | `@PostMapping(value = "/exam", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamController.java:238` | `@GetMapping("/exams/{examId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamController.java:271` | `@GetMapping("/exams")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamController.java:332` | `@GetMapping("/exams/{examId}/paper")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamController.java:400` | `@PostMapping(value = "/exams/{examId}/section-questions/{sectionQuestionId}/regenerate",` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamExportController.java:117` | `@GetMapping("/exams/{examId}/export/pdf")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamExportController.java:159` | `@GetMapping("/exams/{examId}/export/word")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamExportController.java:202` | `@GetMapping("/exams/{examId}/export/answers-pdf")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/ExamExportController.java:239` | `@GetMapping("/exams/{examId}/export/answers-word")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/HealthController.java:55` | `@GetMapping("/health")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgePointRecognitionController.java:34` | `@RequestMapping("/knowledge-points")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgePointRecognitionController.java:66` | `@PostMapping("/recognize")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgePointRecognitionController.java:96` | `@PostMapping("/resolve")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeAdminController.java:42` | `@RequestMapping("/knowledge-trees/admin")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeAdminController.java:88` | `@PostMapping("/import")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeAdminController.java:97` | `@GetMapping("/list")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeAdminController.java:114` | `@GetMapping("/{treeId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeAdminController.java:132` | `@PostMapping("/{treeId}/reindex")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeAdminController.java:151` | `@PostMapping("/reindex-all")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeController.java:42` | `@RequestMapping("/knowledge-trees")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeController.java:94` | `@PostMapping("/import")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeController.java:116` | `@GetMapping` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeController.java:142` | `@GetMapping("/{treeId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/KnowledgeTreeController.java:165` | `@GetMapping("/detail")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/oauth/OAuthAuthorizeController.java:37` | `@RequestMapping("/oauth/authorize")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/oauth/OAuthAuthorizeController.java:65` | `@GetMapping("/validate")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/oauth/OAuthAuthorizeController.java:96` | `@PostMapping("/confirm")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/oauth/OAuthTokenController.java:50` | `@RequestMapping("/oauth/token")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/oauth/OAuthTokenController.java:89` | `@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenApiKeyController.java:35` | `@RequestMapping("/open/api-keys")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenApiKeyController.java:62` | `@PostMapping` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenApiKeyController.java:86` | `@GetMapping` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenApiKeyController.java:117` | `@PostMapping("/{id}/revoke")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenPlatformController.java:43` | `@RequestMapping("open")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenPlatformController.java:82` | `@PostMapping("/token")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenUsageController.java:39` | `@RequestMapping("usage")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenUsageController.java:59` | `@GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenUsageController.java:91` | `@GetMapping(value = "/token-records", produces = MediaType.APPLICATION_JSON_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/OpenUsageController.java:120` | `@GetMapping(value = "/image-records", produces = MediaType.APPLICATION_JSON_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionAnalysisController.java:19` | `@RequestMapping("/questions")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionAnalysisController.java:45` | `@PostMapping(value = "/difficulty/recognize", consumes = MediaType.APPLICATION_JSON_VALUE,` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionAnalysisController.java:66` | `@PostMapping(value = "/explanation/generate", consumes = MediaType.APPLICATION_JSON_VALUE,` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:123` | `@PostMapping(value = "/question-capture-tasks/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:248` | `@PostMapping(value = "/question-capture-tasks/single/create", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:346` | `@GetMapping("/question-capture-tasks/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:370` | `@GetMapping("/question-capture-tasks/{taskId}/poll-status")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:394` | `@PostMapping("/question-capture-tasks/status/batch-query")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:434` | `@GetMapping("/question-capture-tasks/{taskId}/ai-results")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:468` | `@DeleteMapping("/question-capture-tasks/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionCaptureTaskController.java:491` | `@PostMapping("/question-capture-tasks/items/batch-confirm")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionController.java:46` | `@GetMapping("/questions")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/QuestionController.java:72` | `@GetMapping("/questions/{questionId}/preview")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:175` | `@PostMapping(value = "/exams/{examId}/submissions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:249` | `@PostMapping(value = "/exams/{examId}/submissions/photo", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:287` | `@GetMapping("/exams/{examId}/submissions")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:327` | `@GetMapping("/submissions/{submissionId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:360` | `@GetMapping("/submissions/{submissionId}/review")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:397` | `@GetMapping("/grading-tasks/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/SubmissionController.java:438` | `@DeleteMapping("/grading-tasks/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/TaskController.java:88` | `@GetMapping("/tasks/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/TaskController.java:117` | `@GetMapping("/tasks")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/TaskController.java:181` | `@DeleteMapping("/tasks/{taskId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/TaskController.java:213` | `@GetMapping("/questions/{questionId}")` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/VariantExamController.java:133` | `@PostMapping(value = "/exam/variant", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/VariantExamController.java:197` | `@PostMapping(value = "/exams/{examId}/variant", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/VariantQuestionController.java:116` | `@PostMapping(value = "/question/variant", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| cx-backend | `src/main/java/com/xiaogj/cx/backend/web/VariantQuestionController.java:189` | `@PostMapping(value = "/question/variant/image", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/a2a/XzagentA2aAgentCardController.java:21` | `@RequestMapping("/xzagent")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/a2a/XzagentA2aAgentCardController.java:35` | `@GetMapping(value = "/.well-known/agent-card.json", produces = MediaType.APPLICATION_JSON_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/a2a/XzagentA2aJsonRpcController.java:48` | `@RequestMapping("/xzagent")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/a2a/XzagentA2aJsonRpcController.java:81` | `@PostMapping(` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiActionController.java:26` | `@RequestMapping("/xzagent/api/user/a2ui/actions")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiActionController.java:38` | `@PostMapping("/submit")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:48` | `@RequestMapping("/xzagent/api/app/a2ui-catalogs")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:65` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:74` | `@GetMapping("/{catalogId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:89` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:116` | `@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:145` | `@PutMapping("/{catalogId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/A2uiCatalogAdminController.java:174` | `@DeleteMapping("/{catalogId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AgentAdminController.java:29` | `@RequestMapping("/xzagent/api/app/agents")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AgentAdminController.java:44` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AgentAdminController.java:53` | `@GetMapping("/{agentId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AgentAdminController.java:67` | `@PutMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AgentAdminController.java:78` | `@PutMapping("/{agentId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AguiChatController.java:87` | `@RequestMapping("/xzagent/api/user/agui")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AguiChatController.java:237` | `@PostMapping(path = "/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AguiSessionController.java:57` | `@RequestMapping("/xzagent/api/user/agui/sessions")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AguiSessionController.java:130` | `@GetMapping(path = "/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AguiSessionController.java:201` | `@PostMapping("/{sessionId}/cancel")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAgentManagementController.java:24` | `@RequestMapping("/xzagent/api/app/agent-definitions")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAgentManagementController.java:34` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAgentManagementController.java:41` | `@GetMapping("/{agentId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAgentOptionsController.java:24` | `@RequestMapping("/xzagent/api/app/agent-options")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAgentOptionsController.java:32` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAuthController.java:34` | `@RequestMapping("/xzagent/api/auth")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAuthController.java:54` | `@PostMapping("/app-token")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppAuthController.java:72` | `@PostMapping("/user-token")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppSessionController.java:33` | `@RequestMapping("/xzagent/api/app/sessions")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppSessionController.java:43` | `@GetMapping(produces = "application/json")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppSessionController.java:61` | `@GetMapping(value = "/{sessionId}/messages", produces = "application/json")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppToolStorageController.java:23` | `@RequestMapping("/xzagent/api/app/tool-storage")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppToolStorageController.java:34` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppToolStorageController.java:40` | `@PutMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AppToolStorageController.java:54` | `@DeleteMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AuthPrivateController.java:23` | `@RequestMapping("/xzagent/private/api/auth")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/AuthPrivateController.java:40` | `@PostMapping("/plugin-login")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/BundleImportController.java:29` | `@RequestMapping("/xzagent/api/app/bundles")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/BundleImportController.java:44` | `@PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/ChannelController.java:30` | `@RequestMapping("/xzagent/api/user/me/channels")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/ChannelController.java:51` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ConsoleCapabilitiesController.java:24` | `@RequestMapping("/xzagent/api/app/capabilities")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ConsoleCapabilitiesController.java:37` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ConsoleDemoController.java:28` | `@RequestMapping("/xzagent/api/app/demo/token")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ConsoleDemoController.java:36` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:43` | `@RequestMapping("/xzagent/api/admin/model-config")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:63` | `@PostMapping("/discover-models")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:72` | `@PostMapping("/test")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:82` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:88` | `@PostMapping("/providers")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:95` | `@PutMapping("/providers/{id}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:103` | `@DeleteMapping("/providers/{id}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:111` | `@PostMapping("/models")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:118` | `@PutMapping("/models/{id}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:126` | `@DeleteMapping("/models/{id}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:134` | `@PutMapping("/routes/{purpose}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/ModelConfigController.java:142` | `@GetMapping("/protocols")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/console/TemporaryNacosModelImportController.java:39` | `@PostMapping("/xzagent/api/admin/model-config/import-nacos")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/FeishuChannelController.java:61` | `@RequestMapping("/xzagent/api/user/me/channels/feishu/bindings")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/FeishuChannelController.java:100` | `@PostMapping(path = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/FeishuChannelController.java:152` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/FeishuChannelController.java:174` | `@DeleteMapping("/{bindingId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/FeishuChannelController.java:201` | `@PatchMapping("/{bindingId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/HtmlScreenshotAssetController.java:29` | `@RequestMapping(HtmlScreenshotConstants.PUBLIC_PATH_PREFIX)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/HtmlScreenshotAssetController.java:41` | `@GetMapping("/{fileName}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/InternalRunControlController.java:28` | `@RequestMapping("${xzagent.distributed-run.internal-control-path-prefix:/xzagent/private/internal}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/InternalRunControlController.java:57` | `@PostMapping("/sessions/{sessionId}/runs/{runId}/cancel")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/LocalToolCatalogController.java:20` | `@RequestMapping("/xzagent/api/app/local-tools")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/LocalToolCatalogController.java:35` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/McpAdminController.java:30` | `@RequestMapping("/xzagent/api/app/mcp-servers")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/McpAdminController.java:45` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/McpAdminController.java:55` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyApiKeyController.java:37` | `@RequestMapping("/xzagent/api/user/me/api-key")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyApiKeyController.java:65` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyApiKeyController.java:79` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyApiKeyController.java:120` | `@DeleteMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyMcpCredentialBindingController.java:35` | `@RequestMapping("/xzagent/api/user/me/mcp-credential-bindings")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyMcpCredentialBindingController.java:46` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyMcpCredentialBindingController.java:54` | `@PutMapping("/{serverId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyMcpCredentialBindingController.java:66` | `@DeleteMapping("/{serverId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyRemoteSourceCredentialBindingController.java:32` | `@RequestMapping("/xzagent/api/user/me/remote-source-credential-bindings")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyRemoteSourceCredentialBindingController.java:51` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyRemoteSourceCredentialBindingController.java:61` | `@PutMapping("/{sourceId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyRemoteSourceCredentialBindingController.java:78` | `@DeleteMapping("/{sourceId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyThirdPartyCredentialController.java:34` | `@RequestMapping("/xzagent/api/user/me/third-party-credentials")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyThirdPartyCredentialController.java:53` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyThirdPartyCredentialController.java:63` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyThirdPartyCredentialController.java:75` | `@PutMapping("/{credentialId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/MyThirdPartyCredentialController.java:88` | `@DeleteMapping("/{credentialId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/OAuthCallbackController.java:30` | `@RequestMapping("/xzagent/api/oauth")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/OAuthCallbackController.java:50` | `@GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/RemoteToolAdminController.java:36` | `@RequestMapping("/xzagent/api/app/remote-tool-sources")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/RemoteToolAdminController.java:53` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/RemoteToolAdminController.java:62` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/RemoteToolAdminController.java:80` | `@PostMapping(path = "/inline", consumes = MediaType.APPLICATION_JSON_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/RemoteToolAdminController.java:98` | `@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/RemoteToolAdminController.java:138` | `@DeleteMapping("/{sourceId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SessionController.java:25` | `@RequestMapping("/xzagent/api/user/sessions")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SessionController.java:40` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SessionController.java:57` | `@PutMapping("/{sessionId}/title")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SessionController.java:74` | `@GetMapping("/{sessionId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SessionController.java:91` | `@GetMapping("/{sessionId}/runs")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SessionController.java:107` | `@DeleteMapping("/{sessionId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillBindingAdminController.java:24` | `@RequestMapping("/xzagent/api/app/skills")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillBindingAdminController.java:39` | `@PutMapping("/{skillId}/bindings")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCapabilityController.java:22` | `@RequestMapping("/xzagent/api/app/skill-capabilities")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCapabilityController.java:37` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:39` | `@RequestMapping("/xzagent/api/app/skills")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:54` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:63` | `@GetMapping("/{skillId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:77` | `@PostMapping("/inline")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:92` | `@PutMapping("/{skillId}/inline")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:109` | `@PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)` |
| xzagent | `src/main/java/xiaogj/xzagent/web/SkillCatalogController.java:131` | `@DeleteMapping("/{skillId}")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/TenantAuthController.java:30` | `@RequestMapping("/xzagent/api/tenant/auth")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/TenantAuthController.java:48` | `@PostMapping("/user-login-bind")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UsageController.java:36` | `@RequestMapping("/xzagent/api/app/usage")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UsageController.java:49` | `@GetMapping("/summary")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UsageController.java:76` | `@GetMapping("/token-records")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UsageController.java:90` | `@GetMapping("/image-records")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UserAdminController.java:30` | `@RequestMapping("/xzagent/api/tenant/users")` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UserAdminController.java:45` | `@GetMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UserAdminController.java:54` | `@PostMapping` |
| xzagent | `src/main/java/xiaogj/xzagent/web/UserAdminController.java:65` | `@DeleteMapping("/{userId}")` |
