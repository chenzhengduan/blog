# 附录：HTTP 控制器与 Hub 方法索引

静态提取控制器声明和方法签名，方便定位完整 DTO。内网路由与公网网关前缀须分开；这里只列源码路径。访问控制、参数默认值和响应体以方法原文为准。

## AgentGatewayController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/AgentGatewayController.cs`

```csharp
17: [Route("agent/[controller]/[action]")]
46: [HttpPost]
47: public async Task<CallResult<SubmitTurnResponse>> SubmitTurn(
152: [HttpGet]
153: public async Task<CallResult<object>> GetTurnStatus([FromQuery] Guid turnId, CancellationToken cancellationToken)
```

## AgentManageController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/AgentManageController.cs`

```csharp
15: /// 使用 CentraHub，免租户切换（[NoAuth][NoTenant]），但需管理页固定账号密码（[AgentManageAuth]）。
19: [Route("agent/[controller]/[action]")]
20: [NoAuth]
21: [NoTenant]
22: [AgentManageAuth]
39: [HttpPost]
41: public CallResult<string> Login([FromBody] AgentManageLoginRequest request)
49: [HttpGet]
51: public IActionResult Viewer()
62: [HttpGet]
63: public CallResult<AgentManageOverviewResponse> GetOverview()
67: [HttpGet]
68: public CallResult<AgentInstanceDetailResponse> GetInstance(string agentName)
72: [HttpGet]
73: public CallResult<AgentVersionDetail> GetVersion(string agentName, int versionNo)
82: [HttpPost]
83: public CallResult<int> SaveVersion([FromBody] SaveAgentVersionRequest request)
97: [HttpPost]
98: public CallResult ActivateVersion([FromBody] ActivateVersionRequest request)
107: [HttpPost]
108: public CallResult DeleteVersion([FromBody] DeleteVersionRequest request)
122: [HttpPost]
123: public CallResult SaveInstanceMeta([FromBody] SaveInstanceMetaRequest request)
130: [HttpPost]
131: public CallResult DeleteInstance([FromBody] DeleteInstanceRequest request)
145: [HttpPost]
146: public CallResult CopyAgent([FromBody] CopyAgentRequest request)
160: [HttpPost]
161: public CallResult<int> Reload()
175: [HttpGet]
176: public CallResult<List<string>> GetMcpHiddenTools()
180: [HttpPost]
181: public CallResult SaveMcpHiddenTools([FromBody] SaveMcpHiddenToolsRequest request)
195: [HttpGet]
196: public CallResult<string> GetGlobalPrompt()
200: [HttpPost]
201: public CallResult SaveGlobalPrompt([FromBody] SaveGlobalPromptRequest request)
217: [HttpGet]
218: public CallResult<AgentSkillDetailResponse> GetSkill(string skillKey)
231: [HttpGet]
232: public CallResult<AgentSkillHistoryDetailResponse> GetSkillHistoryDetail(string skillKey, Guid historyId)
248: [HttpPost]
250: public CallResult SaveSkill([FromBody] SaveSkillRequest request)
264: [HttpPost]
265: public CallResult RecordSkillPublication([FromBody] RecordSkillPublicationRequest request)
272: [HttpPost]
273: public CallResult RollbackSkill([FromBody] RollbackSkillRequest request)
280: [HttpPost]
281: public CallResult DeleteSkill([FromBody] DeleteLibrarySkillRequest request)
```

## AgentTraceController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/AgentTraceController.cs`

```csharp
14: /// 免租户切换（[NoAuth][NoTenant]），与管理页共用固定账号密码（[AgentManageAuth]）。
17: [Route("agent/[controller]/[action]")]
18: [NoAuth]
19: [NoTenant]
20: [AgentManageAuth]
40: [HttpGet]
42: public IActionResult Viewer()
56: [HttpGet]
57: public async Task<CallResult<List<AgentRunTraceListItem>>> List(
72: [HttpGet]
73: public async Task<CallResult<AgentConversationContextTraceView?>> Context(
87: [HttpGet]
88: public async Task<CallResult<AgentRunTraceDetail>> Detail(
```

## AiUsageController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/AiUsageController.cs`

```csharp
16: [Route("agent/[controller]/[action]")]
17: [Route("[controller]/[action]")]
18: [WorkbenchAccess]
23: [HttpPost]
24: public async Task<CallResult<AiUsageOverviewResponse>> GetOverview([FromBody] AiUsageQueryRequest? request, CancellationToken ct = default)
31: [HttpPost]
32: public async Task<CallResult<AiUsageRecordsResponse>> GetRecords([FromBody] AiUsageQueryRequest? request, CancellationToken ct = default)
39: [HttpPost]
40: public async Task<CallResult<AiUsageDetailResponse>> GetDetail([FromBody] AiUsageDetailRequest request, CancellationToken ct = default)
44: [HttpPost]
45: public async Task<IActionResult> Export([FromBody] AiUsageQueryRequest? request, CancellationToken ct = default)
```

## CalibrationController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/CalibrationController.cs`

```csharp
13: [Route("agent/[controller]/[action]")]
14: [WorkbenchAccess]
47: [HttpPost]
48: public async Task<CallResult<StepBGenerateResponse>> GenerateKnowledgeBase(CancellationToken ct = default)
55: [HttpPost]
56: public async Task<CallResult> SaveProfileItem([FromBody] SaveProfileItemRequest request, CancellationToken ct = default)
63: [HttpPost]
64: public async Task<CallResult> ConfirmMetric([FromBody] ConfirmMetricRequest request, CancellationToken ct = default)
71: [HttpPost]
72: public async Task<CallResult<MetricSaveResponse>> AddCustomMetric([FromBody] AddCustomMetricRequest request, CancellationToken ct = default)
93: [HttpPost]
94: public CallResult<IReadOnlyList<BuiltinMetricCatalogItem>> GetMetricCatalog(
102: [HttpPost]
103: public CallResult<IReadOnlyList<BuiltinMetricCatalogItem>> GetStandardMetricCatalog(
108: [HttpPost]
109: public async Task<CallResult<MaterializeMetricRuleResponse>> MaterializeMetricRule(
117: [HttpPost]
118: public async Task<CallResult<UnderstandCustomMetricResponse>> UnderstandCustomMetric(
128: [HttpPost]
129: public async Task<CallResult<MetricListResponse>> GetMetrics([FromBody] GetMetricListRequest? request, CancellationToken ct = default)
138: [HttpPost]
139: public async Task<CallResult<AgentMetricDO>> GetMetric([FromBody] GetMetricRequest request, CancellationToken ct = default)
148: [HttpPost]
149: public async Task<CallResult<MetricSaveResponse>> CreateMetric([FromBody] SaveMetricRequest request, CancellationToken ct = default)
159: [HttpPost]
160: public async Task<CallResult<MetricSaveResponse>> UpdateMetric([FromBody] UpdateMetricRequest request, CancellationToken ct = default)
170: [HttpPost]
171: public async Task<CallResult> DeprecateMetric([FromBody] DeprecateMetricRequest request, CancellationToken ct = default)
178: [HttpPost]
179: public async Task<CallResult> RestoreMetric([FromBody] RestoreMetricRequest request, CancellationToken ct = default)
186: [HttpPost]
187: public async Task<CallResult> DeleteMetric([FromBody] DeleteMetricRequest request, CancellationToken ct = default)
196: [HttpPost]
197: public async Task<CallResult<IReadOnlyList<AgentMetricDO>>> GenerateStandardMetrics(CancellationToken ct = default)
206: [HttpPost]
207: public async Task<CallResult<SkillListResponse>> GetSkills([FromBody] GetSkillListRequest? request, CancellationToken ct = default)
218: [HttpPost]
219: public async Task<CallResult<QuickSkillListResponse>> GetQuickSkills([FromBody] GetQuickSkillListRequest? request, CancellationToken ct = default)
226: [HttpPost]
227: public async Task<CallResult<SkillAssistResponse>> AssistCreateSkill([FromBody] AssistCreateSkillRequest request, CancellationToken ct = default)
244: [HttpPost]
245: public async Task<CallResult<UserSkillDO>> CreateUserSkill([FromBody] CreateUserSkillRequest request, CancellationToken ct = default)
284: [HttpPost]
285: public async Task<CallResult<UserSkillDO>> UpdateUserSkill([FromBody] UpdateUserSkillRequest request, CancellationToken ct = default)
318: [HttpPost]
319: public async Task<CallResult<ProfileListResponse>> GetProfile([FromBody] GetProfileRequest? request, CancellationToken ct = default)
```

## ChatController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ChatController.cs`

```csharp
20: [Route("agent/[controller]/[action]")]
21: [WorkbenchAccess]
51: [HttpPost]
52: public async Task<CallResult<GetHistoryChatList_ViewModel>> GetHistoryChatList([FromBody] GetHistoryChatList_QueryModel queryModel)
96: [HttpPost]
97: public async Task<CallResult<GetConversationObjects_ViewModel>> GetConversationObjects([FromBody] GetConversationObjects_QueryModel queryModel)
116: [HttpPost]
117: public async Task<CallResult<GetWorkbenchObjectVersionPayload_ViewModel>> GetWorkbenchObjectVersionPayload([FromBody] GetWorkbenchObjectVersionPayload_QueryModel queryModel)
136: [HttpPost]
137: public async Task<CallResult<bool>> MarkConversationRead([FromBody] MarkConversationRead_QueryModel queryModel)
156: [HttpPost]
157: public async Task<CallResult<GetHistoryChatMessagesDetail_ViewModel>> GetHistoryChatMessagesDetail([FromBody] GetHistoryChatMessagesDetail_QueryModel queryModel)
202: [HttpPost]
203: public async Task<CallResult<GetInitRecommendAction_ViewModel>> GetInitRecommendAction(CancellationToken cancellationToken)
```

## ClassQueryController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ClassQueryController.cs`

```csharp
12: [Route("agent/class-query")]
13: [WorkbenchAccess]
16: [HttpPost("list")]
17: public async Task<CallResult<ClassListResult>> QueryList(
21: [HttpPost("students")]
22: public async Task<CallResult<ClassStudentListResult>> QueryStudents(
26: [HttpPost("export")]
27: public async Task<IActionResult> Export(
```

## FamilySchoolController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/FamilySchoolController.cs`

```csharp
10: [Route("agent/family-school")]
16: [HttpPost("message-drafts")]
17: public async Task<CallResult<ParentMessageDraftResponse>> CreateDraft([FromBody] CreateParentMessageDraftRequest request, CancellationToken cancellationToken)
25: [HttpPost("message-drafts/{draftId:guid}/send")]
26: public async Task<CallResult<ParentMessageDraftResponse>> Send(Guid draftId, [FromBody] SendParentMessageRequest request, [FromHeader(Name = "Idempotency-Key")] string? idempotencyKey, CancellationToken cancellationToken)
34: [HttpGet("message-drafts/{draftId:guid}")]
35: public async Task<CallResult<ParentMessageDraftResponse>> Get(Guid draftId, CancellationToken cancellationToken)
```

## ImBindingController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ImBindingController.cs`

```csharp
28: [Route("agent/[controller]/[action]")]
29: [WorkbenchAccess]
79: [HttpPost]
80: public async Task<CallResult<ImBindingListResponse>> GetMyBindings(CancellationToken cancellationToken)
115: [HttpPost]
116: public async Task<CallResult<ImBindingListResponse>> GetManagedBindings(CancellationToken cancellationToken)
152: [HttpPost]
153: public CallResult<SelectableAgentListResponse> GetSelectableAgents()
170: [HttpPost]
171: public CallResult<AgentDisplayListResponse> GetAllAgents()
201: [HttpPost]
202: public async Task<CallResult<StartBotProvisionResponse>> StartBotProvision(
243: [HttpPost]
244: public async Task<CallResult<CreateDingTalkBotResponse>> CreateDingTalkBot(
289: [HttpPost]
290: public async Task<CallResult<ProvisionStatusResponse>> GetProvisionStatus(
318: [HttpPost]
319: public async Task<CallResult<TryPairBindingResponse>> TryPairBinding(
363: [HttpPost]
364: public async Task<CallResult<bool>> SaveImUserId(
414: [HttpPost]
415: public async Task<CallResult<bool>> UpdateBotStatus(
515: [HttpPost]
516: public async Task<CallResult<bool>> UpdateBotAgent(
561: [HttpPost]
562: public async Task<CallResult<ImUserBindStatusResponse>> GetImUserBindStatus(
597: [HttpPost]
598: public async Task<CallResult<bool>> ConfirmImUserBind(
623: [HttpPost]
624: [NoAuth]
625: public async Task<CallResult<ImBindQrResponse>> GetImBindQr(
643: [HttpPost]
644: [NoAuth]
645: public async Task<CallResult<ImBindResultResponse>> GetImBindResult(
657: [HttpGet]
658: [NoAuth]
659: public async Task<IActionResult> ImBindCallback([FromQuery] string key, [FromQuery] Guid userId)
694: [HttpPost]
695: public async Task<CallResult<MyImUserBindListResponse>> GetMyImUserBinds(CancellationToken cancellationToken)
730: [HttpPost]
731: public async Task<CallResult<bool>> UnbindMyImUser(
758: [HttpPost]
759: public async Task<CallResult<BotBoundUserListResponse>> GetBotBoundUsers(
784: [HttpPost]
785: public async Task<CallResult<bool>> UnbindBotUser(
```

## LeadQueryController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/LeadQueryController.cs`

```csharp
16: [Route("agent/[controller]/[action]")]
17: [WorkbenchAccess]
33: [HttpPost]
34: public async Task<CallResult<CustomerQueryResult>> QueryCustomers([FromBody] CustomerQueryForm form, CancellationToken ct = default)
42: [HttpPost]
43: public async Task<CallResult<CustomerQueryResult>> ListMyCustomers([FromBody] MyCustomerListQuery query, CancellationToken ct = default)
52: [HttpPost]
53: public async Task<CallResult<CampusesSourcesResult>> ListCampusesSources(CancellationToken ct = default)
```

## LeadScoreController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/LeadScoreController.cs`

```csharp
17: [Route("agent/[controller]/[action]")]
18: [WorkbenchAccess]
41: [HttpPost]
42: public async Task<CallResult<GetScoreRulesResponse>> GetScoreRules(CancellationToken ct = default)
49: [HttpPost]
50: public async Task<CallResult<GetScoreRulesResponse>> GetScoreRuleTemplate(CancellationToken ct = default)
57: [HttpPost]
58: public async Task<CallResult<GetScoreRulesResponse>> SaveScoreRules([FromBody] SaveScoreRulesRequest request, CancellationToken ct = default)
67: [HttpPost]
68: public async Task<CallResult<LeadScoreRuleItem>> SaveScoreRule([FromBody] SaveScoreRuleRequest request, CancellationToken ct = default)
77: [HttpPost]
78: public async Task<CallResult> DeleteScoreRule([FromBody] DeleteScoreRuleRequest request, CancellationToken ct = default)
87: [HttpPost]
88: public async Task<CallResult<LeadScoreProfileResponse>> GetLeadScoreProfile([FromBody] GetLeadScoreProfileRequest request, CancellationToken ct = default)
96: [HttpPost]
97: public async Task<CallResult<GetLeadScoreVersionsResponse>> GetLeadScoreVersions([FromBody] GetLeadScoreVersionsRequest request, CancellationToken ct = default)
108: [HttpPost]
109: public async Task<CallResult<GetLeadScoresBatchResponse>> GetLeadScoresBatch([FromBody] GetLeadScoresBatchRequest request, CancellationToken ct = default)
```

## LeadScriptController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/LeadScriptController.cs`

```csharp
15: [Route("agent/[controller]/[action]")]
16: [WorkbenchAccess]
39: [HttpPost]
40: public async Task<CallResult<GetScriptRulesResponse>> GetScriptRules(CancellationToken ct = default)
47: [HttpPost]
48: public async Task<CallResult<GetScriptRulesResponse>> GetScriptRuleTemplate(CancellationToken ct = default)
55: [HttpPost]
56: public async Task<CallResult<GetScriptRulesResponse>> SaveScriptRules([FromBody] SaveScriptRulesRequest request, CancellationToken ct = default)
65: [HttpPost]
66: public async Task<CallResult<ScriptRuleItem>> SaveScriptRule([FromBody] SaveScriptRuleRequest request, CancellationToken ct = default)
75: [HttpPost]
76: public async Task<CallResult> DeleteScriptRule([FromBody] DeleteScriptRuleRequest request, CancellationToken ct = default)
85: [HttpPost]
86: public async Task<CallResult<LeadScriptResponse>> GenerateLeadScript([FromBody] GenerateLeadScriptRequest request, CancellationToken ct = default)
```

## MonitorConfigController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/MonitorConfigController.cs`

```csharp
15: [Route("agent/[controller]/[action]")]
16: [WorkbenchAccess]
35: [HttpPost]
36: public async Task<CallResult<MonitorConfigListResponse>> GetList([FromBody] GetMonitorListRequest? request, CancellationToken ct = default)
43: [HttpPost]
44: public async Task<CallResult<MonitorConfigVO>> GetDetail([FromBody] GetMonitorDetailRequest request, CancellationToken ct = default)
53: [HttpPost]
54: public async Task<CallResult<Guid>> Save([FromBody] SaveMonitorConfigRequest request, CancellationToken ct = default)
61: [HttpPost]
62: public async Task<CallResult> Toggle([FromBody] ToggleMonitorConfigRequest request, CancellationToken ct = default)
72: [HttpPost]
73: public async Task<CallResult> Delete([FromBody] DeleteMonitorConfigRequest request, CancellationToken ct = default)
80: [HttpPost]
81: public async Task<CallResult> RunNow([FromBody] DeleteMonitorConfigRequest request, CancellationToken ct = default)
88: [HttpPost]
89: public async Task<CallResult<MonitorWidgetDataResponse>> GetWidget(CancellationToken ct = default)
96: [HttpPost]
97: public async Task<CallResult<MonitorRunVO>> GetReport([FromBody] GetMonitorReportRequest request, CancellationToken ct = default)
106: [HttpPost]
107: public async Task<CallResult<MonitorRunListResponse>> GetRuns([FromBody] GetMonitorReportRequest request, CancellationToken ct = default)
114: [HttpPost]
115: public async Task<CallResult<MonitorRunVO>> GetLatestRun([FromBody] GetMonitorReportRequest request, CancellationToken ct = default)
124: [HttpPost]
125: public async Task<CallResult> ReadAlert([FromBody] ReadMonitorAlertRequest request, CancellationToken ct = default)
132: [HttpPost]
133: public async Task<CallResult> ReadAllAlerts(CancellationToken ct = default)
140: [HttpPost]
141: public async Task<CallResult<ParseCronResponse>> ParseCron([FromBody] ParseCronRequest request, CancellationToken ct = default)
```

## OnboardingController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/OnboardingController.cs`

```csharp
13: [Route("agent/[controller]/[action]")]
14: [WorkbenchAccess]
24: [HttpPost]
25: public async Task<CallResult<OnboardingProgressResponse>> GetProgress(CancellationToken ct)
29: [HttpPost]
30: public async Task<CallResult<OnboardingProgressResponse>> CompleteTask([FromBody] CompleteOnboardingTaskRequest request, CancellationToken ct)
39: [HttpPost]
40: public async Task<CallResult<OnboardingProgressResponse>> DismissWelcome(CancellationToken ct)
```

## OrganizationController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/OrganizationController.cs`

```csharp
14: [Route("agent/organization")]
15: [WorkbenchAccess]
19: [HttpPost("employees/search")]
20: public async Task<CallResult<List<EmployeeOptionItem>>> SearchEmployees(
```

## PreferenceRuleController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/PreferenceRuleController.cs`

```csharp
20: [Route("agent/[controller]/[action]")]
21: [WorkbenchAccess]
43: [HttpPost]
44: public async Task<CallResult<PreferenceRuleListResponse>> GetMyRules(CancellationToken cancellationToken)
67: [HttpPost]
68: public async Task<CallResult<bool>> SaveRule(
101: [HttpPost]
102: public async Task<CallResult<bool>> DeleteRule(
```

## ReportController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ReportController.cs`

```csharp
14: [Route("agent/report")]
15: [WorkbenchAccess]
21: [HttpPost("consumption/summary")]
22: public async Task<CallResult<ConsumptionSummaryResponse>> GetConsumptionSummary(
28: [HttpPost("consumption/details")]
29: public async Task<CallResult<ConsumptionDetailResponse>> GetConsumptionDetails(
35: [HttpPost("consumption/remain-fee")]
36: public async Task<CallResult<RemainFeeResponse>> GetRemainFee(
42: [HttpPost("fee/summary")]
43: public async Task<CallResult<FeeSummaryResponse>> GetFeeSummary(
49: [HttpPost("fee/refund-summary")]
50: public async Task<CallResult<RefundSummaryResponse>> GetRefundSummary(
56: [HttpPost("fee/arrears")]
57: public async Task<CallResult<ArrearResponse>> GetArrears(
63: [HttpPost("performance/teacher-consume")]
64: public async Task<CallResult<TeacherConsumeResponse>> GetTeacherConsumes(
70: [HttpPost("performance/staff-consume")]
71: public async Task<CallResult<StaffConsumeResponse>> GetStaffConsumes(
77: [HttpPost("performance/signing")]
78: public async Task<CallResult<SigningPerformanceResponse>> GetSigningPerformances(
84: [HttpPost("student-analysis/source")]
85: public async Task<CallResult<StudentSourceResponse>> GetStudentSourceSummary(
91: [HttpPost("student-analysis/stops")]
92: public async Task<CallResult<StudentStopResponse>> GetStudentStops(
98: [HttpPost("student-analysis/no-class")]
99: public async Task<CallResult<NoClassStudentResponse>> GetNoClassStudents(
105: [HttpPost("attendance/details")]
106: public async Task<CallResult<AttendanceDetailResponse>> GetAttendanceDetails(
112: [HttpPost("attendance/rates")]
113: public async Task<CallResult<AttendanceRateResponse>> GetAttendanceRates(
119: [HttpPost("overview")]
120: public async Task<CallResult<OperationsOverviewResponse>> GetOperationsOverview(
128: [HttpPost("enrollment/source")]
129: public async Task<CallResult<EnrollmentSourceReportResponse>> GetEnrollmentSource(
135: [HttpPost("enrollment/source-detail")]
136: public async Task<CallResult<EnrollmentSourceDetailResponse>> GetEnrollmentSourceDetail(
142: [HttpPost("enrollment/will-level")]
143: public async Task<CallResult<EnrollmentWillLevelReportResponse>> GetEnrollmentWillLevel(
149: [HttpPost("enrollment/funnel")]
150: public async Task<CallResult<EnrollmentFunnelResponse>> GetEnrollmentFunnel(
156: [HttpPost("enrollment/funnel-pivot")]
157: public async Task<CallResult<EnrollmentFunnelPivotResponse>> GetEnrollmentFunnelPivot(
163: [HttpPost("enrollment/funnel-detail")]
164: public async Task<CallResult<EnrollmentCustomerDetailResponse>> GetEnrollmentFunnelDetail(
170: [HttpPost("enrollment/funnel-pivot-detail")]
171: public async Task<CallResult<EnrollmentCustomerDetailResponse>> GetEnrollmentFunnelPivotDetail(
177: [HttpPost("enrollment/visit-details")]
178: public async Task<CallResult<EnrollmentVisitDetailResponse>> GetEnrollmentVisitDetails(
184: [HttpGet("enrollment/funnel-order")]
185: public async Task<CallResult<List<EnrollmentFunnelOrderItemModel>>> GetEnrollmentFunnelOrder(
191: [HttpPost("enrollment/funnel-order")]
192: public async Task<CallResult<bool>> SaveEnrollmentFunnelOrder(
200: [HttpPost("enrollment/options")]
201: public async Task<CallResult<EnrollmentOptionsResponse>> GetEnrollmentOptions(
```

## ScenarioCollectController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ScenarioCollectController.cs`

```csharp
12: [Route("agent/[controller]/[action]")]
36: [HttpPost]
37: public async Task<CallResult> SubmitScenarios([FromBody] SubmitScenariosRequest request, CancellationToken ct = default)
74: [HttpPost]
75: public async Task<CallResult<List<ScenarioCollectRecordResponse>>> GetSubmitRecords(CancellationToken ct = default)
```

## ScheduledRunController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ScheduledRunController.cs`

```csharp
23: [Route("agent/[controller]/[action]")]
68: [HttpPost]
69: public async Task<IActionResult> RunTask(
```

## ScheduleMigrationController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/ScheduleMigrationController.cs`

```csharp
15: /// 免租户切换（[NoAuth][NoTenant]），与管理页共用固定账号密码（[AgentManageAuth]）。
18: [Route("agent/[controller]/[action]")]
19: [NoAuth]
20: [NoTenant]
21: [AgentManageAuth]
32: [HttpGet]
33: public async Task<CallResult<List<OldScheduleTaskVO>>> ListOldTasks(CancellationToken ct = default)
37: [HttpPost]
38: public async Task<CallResult<ScheduleMigrationResult>> MigrateTask(
50: [HttpPost]
51: public async Task<CallResult> RunNow(
61: [HttpPost]
62: public async Task<CallResult<ScheduleMigrationResult>> StopOldTask(
```

## StudentOperationController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/StudentOperationController.cs`

```csharp
12: [Route("agent/student-operation")]
13: [WorkbenchAccess]
18: [HttpPost("overview")]
19: public async Task<CallResult<StudentOperationOverview>> GetOverview(
24: [HttpPost("renewal-opportunities")]
25: public async Task<CallResult<StudentOperationPage>> GetRenewalOpportunities(
30: [HttpPost("expansion-opportunities")]
31: public async Task<CallResult<StudentOperationPage>> GetExpansionOpportunities(
36: [HttpPost("stickiness")]
37: public async Task<CallResult<StudentStickinessResult>> GetStickiness(
42: [HttpPost("stickiness/evidence")]
43: public async Task<CallResult<StudentStickinessEvidenceResult>> GetStickinessEvidence(
48: [HttpPost("communication-quality")]
49: public async Task<CallResult<StudentCommunicationQualityResult>> GetCommunicationQuality(
54: [HttpPost("communication-quality/ai-analysis")]
55: public async Task<CallResult<StudentCommunicationQualityAiAnalysis>> GetCommunicationQualityAiAnalysis(
60: [HttpPost("teaching-service")]
61: public async Task<CallResult<TeachingServiceAnalysisResult>> GetTeachingServiceAnalysis(
66: [HttpPost("teaching-service/ai-analysis")]
67: public async Task<CallResult<TeachingServiceAnalysisAiAnalysis>> GetTeachingServiceAnalysisAi(
72: [HttpPost("analysis-reports/save")]
73: public async Task<CallResult<StudentAnalysisReportHistoryDetail>> SaveAnalysisReport(
78: [HttpPost("analysis-reports/list")]
79: public async Task<CallResult<StudentAnalysisReportHistoryPage>> GetAnalysisReportHistory(
84: [HttpPost("analysis-reports/detail")]
85: public async Task<CallResult<StudentAnalysisReportHistoryDetail>> GetAnalysisReportDetail(
90: [HttpPost("analysis-reports/delete")]
91: public async Task<CallResult<bool>> DeleteAnalysisReport(
98: [HttpPost("parent-satisfaction")]
99: public async Task<CallResult<ParentSatisfactionResult>> GetParentSatisfaction(
104: [HttpPost("parent-satisfaction/evidence-source")]
105: public async Task<CallResult<ParentSatisfactionEvidenceSourceDetail>> GetParentSatisfactionEvidenceSource(
110: [HttpPost("parent-satisfaction/ai-analysis")]
111: public async Task<CallResult<ParentSatisfactionAiAnalysis>> GetParentSatisfactionAiAnalysis(
116: [HttpGet("students/{studentId:guid}")]
117: public async Task<CallResult<StudentRenewalRiskResult>> GetStudentProfile(
122: [HttpGet("students/{studentId:guid}/growth")]
123: public async Task<CallResult<StudentGrowthDetail>> GetStudentGrowth(
131: [HttpGet("followups")]
132: public async Task<CallResult<IReadOnlyList<TodoItemVO>>> GetFollowups(
137: [HttpPost("followups")]
138: public async Task<CallResult<CreateStudentOperationFollowupResponse>> CreateFollowup(
143: [HttpGet("followups/page")]
144: public async Task<CallResult<TodoPageResponse>> GetFollowupPage(
```

## StudentSmartListController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/StudentSmartListController.cs`

```csharp
11: [Route("agent/student-smart-list")]
12: [WorkbenchAccess]
17: [HttpPost("query")]
18: public async Task<CallResult<StudentSmartListResult>> Query(
24: [HttpPost("script")]
25: public async Task<CallResult<StudentSmartListScript>> GenerateScript(
31: [HttpPost("scripts")]
32: public async Task<CallResult<StudentSmartListBatchScriptResult>> GenerateScripts(
38: [HttpPost("history/save")]
39: public async Task<CallResult<StudentSmartListHistorySummary>> SaveHistory(
46: [HttpPost("history/list")]
47: public async Task<CallResult<StudentSmartListHistoryPage>> GetHistoryList(
53: [HttpPost("history/detail")]
54: public async Task<CallResult<StudentSmartListHistoryDetail>> GetHistoryDetail(
60: [HttpPost("history/delete")]
61: public async Task<CallResult<bool>> DeleteHistory(
```

## TodoController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/TodoController.cs`

```csharp
16: [Route("agent/[controller]/[action]")]
17: [WorkbenchAccess]
33: [HttpPost]
34: public async Task<CallResult<TodoListResponse>> GetList([FromBody] GetTodoListRequest? request, CancellationToken ct = default)
42: [HttpPost]
43: public async Task<CallResult> Complete([FromBody] CompleteTodoRequest request, CancellationToken ct = default)
59: [HttpPost]
60: public async Task<CallResult> Snooze([FromBody] SnoozeTodoRequest request, CancellationToken ct = default)
67: [HttpPost]
68: public async Task<CallResult> Update([FromBody] UpdateTodoRequest request, CancellationToken ct = default)
75: [HttpPost]
76: public async Task<CallResult> Dismiss([FromBody] TodoActionRequest request, CancellationToken ct = default)
```

## UserController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/UserController.cs`

```csharp
20: [Route("agent/[controller]/[action]")]
21: [NoAuth]
50: [HttpGet]
51: public async Task<CallResult<BindQrViewModel>> GetBindQr([FromQuery] string key)
81: [HttpGet]
82: public async Task<CallResult<BindStatusViewModel>> GetBindStatus([FromQuery] string key)
111: [HttpGet]
112: public async Task<IActionResult> BindUser([FromQuery] string key, [FromQuery] Guid userId)
223: [HttpGet]
224: [NoTenant]
225: public async Task<CallResult<List<BindBotViewModel>>> GetBindBot()
```

## WorkbenchFootprintController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/WorkbenchFootprintController.cs`

```csharp
9: [Route("agent/[controller]/[action]")]
11: [WorkbenchAccess]
14: [HttpPost]
15: public async Task<CallResult<ReportListPage>> List(FootprintListRequest request, CancellationToken ct)
23: [HttpPost]
24: public async Task<CallResult<ReportSnapshot>> Get(ReportGetRequest request, CancellationToken ct)
```

## WorkbenchReportController.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Controllers/WorkbenchReportController.cs`

```csharp
8: [Route("agent/[controller]/[action]")]
10: [WorkbenchAccess]
13: [HttpPost]
14: public async Task<CallResult<ReportListPage>> FootprintList([FromBody] FootprintListRequest request, CancellationToken ct)
22: [HttpPost]
23: public async Task<CallResult<ReportSnapshot>> FootprintGet([FromBody] ReportGetRequest request, CancellationToken ct)
36: [HttpPost]
37: public async Task<CallResult<ReportListPage>> List([FromBody] ReportListRequest request, CancellationToken ct)
46: [HttpPost]
47: public async Task<CallResult<ReportSnapshot>> Get([FromBody] ReportGetRequest request, CancellationToken ct)
68: [HttpPost]
69: public async Task<CallResult<ReportRecoverySnapshot>> Recover([FromBody] ReportGetRequest request, CancellationToken ct)
90: [HttpPost]
91: public async Task<CallResult<ReportDynamicRefreshResult>> Refresh([FromBody] ReportDynamicRefreshRequest request, CancellationToken ct)
110: [HttpPost]
111: public async Task<CallResult<ReportDynamicOptionsResult>> Options([FromBody] ReportGetRequest request, CancellationToken ct)
```

## WorkbenchHub.cs

源码：`wtwo-server/WTwo.Agent.Web/WTwo.Agent.Service/Workbench/Hubs/WorkbenchHub.cs`

```csharp
61: [WorkbenchAccess]
137: public async Task SwitchConversation(string conversationId)
223: public async Task SendChatMessage(WorkbenchChatRequest request)
342: public async Task StopChatMessage(string conversationId, string? turnId = null)
439: public async Task<object> GetConversationTurnStatus(string conversationId)
467: public async Task SubmitChatChoice(SubmitChatChoiceRequest request)
588: public async Task SubmitToolApproval(SubmitToolApprovalRequest request)
707: public async Task RegisterAppManifests(List<AppManifest> manifests)
741: public async Task SubmitOpResult(OpResult result)
778: public async Task GetObjectsByConversation(string conversationId)
```

