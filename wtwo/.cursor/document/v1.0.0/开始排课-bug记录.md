# 记录AI生成代码过程中出现的bug，需要AI知晓且解决的，如果已经解决，我会标记为已解决

1. 点击开始排课，报错ReferenceError: CourseDraftPublishDraft is not defined，页面弹框还跳转到了排课完成，这是不对的。
   - 状态：已解决
   - 解决方案：
     - 在 class-table-course.vue 中导入缺失的 CourseDraftPublishDraft API 函数
     - 修复错误处理逻辑，在出错时关闭弹框而不是跳转到完成状态
     - 在 CourseDraftPublishDialog.vue 中添加 resetToConfirm 方法用于状态重置

2. publishDraft返回了限制，但是当前没有识别到，我想我们需要针对返回数据做依据，而不是errorCode
   - 返回JSON:{
                "IsSuccess": true,
                "ErrorMsg": "发现 1 个冲突或限制",
                "ErrorCode": 200,
                "Data": [
                    {
                        "DraftId": "7b2796ba-f9f0-41cc-9027-10d83f3c6454",
                        "ErrorFieldList": [],
                        "CheckFieldList": [
                            {
                                "FieldNameList": [
                                    "ClassID"
                                ],
                                "ErrorMessage": "被限制：排课前需完善班级的“老师、教室、预招人数”信息",
                                "CheckerConfigList": [
                                    "CheckClassSetWhenAddCourse"
                                ]
                            }
                        ],
                        "ConflictFieldList": {
                            "FieldNameList": [],
                            "ErrorMessage": null,
                            "ConflictingCourseList": [],
                            "ConflictingDraftList": [],
                            "ConflictingScheduleList": []
                        }
                    }
                ]
            }
   
   - 状态：已解决
   - 问题描述：即使 IsSuccess: true，但 Data 中有 CheckFieldList 数据时，系统没有识别到限制
   - 解决方案：
     - 修改判断逻辑：不仅依赖 IsSuccess，还要检查 Data 是否有内容
     - 优化冲突检测：支持 ConflictingDraftList、ConflictingCourseList、ConflictingScheduleList
     - 完善错误分类：正确区分限制字段、冲突字段、非法字段 

3. 确认排课后，接口返回了一个限制，现在也显示了，当我点击返回修改的时候，列表没有开启检查冲突与限制。然后被限制的字段作为非法字段了，这时候再去开启预检查冲突，会被因为非法而拦截，我想，是不是确认排课的时候调完接口，要自动开启预检查冲突与限制的开关呢？
   - 状态：已解决
   - 问题描述：排课返回限制后，点击"返回修改"时预检查开关未自动开启，被限制字段被当作非法字段
   - 解决方案：
     - 在 handleReturnModifyFromPublish 函数中自动开启预检查开关
     - 自动触发预检查以显示最新的限制信息
     - 提供用户友好的提示信息
   - 实现效果：用户从排课异常到修改的流程更加顺畅，能清楚看到具体限制信息


4. 确认排课的时候，如果当前数据都是被限制的，没有通过的或者代待检查的，那应该也没必要调接口吧，可以直接拦截提示没有可那个的排课数据了
   - 状态：已解决
   - 问题描述：当所有数据都是被限制的时，仍然会调用排课接口，浪费网络请求
   - 解决方案：
     - 在 handlePublishConfirm 函数中添加数据有效性检查
     - 基础检查：验证是否有有效行数据
     - 预检查验证：如果开启预检查，进一步检查是否有通过的数据
     - 避免无效请求：没有可排数据时直接提示，不调用接口
   - 实现效果：优化用户体验，减少不必要的网络请求，提供清晰的错误提示

5. 检查结果后，如果点击仅排通过检查的排课，这时如果没有符合排课的记录的话会提示没有排课的数据，然后关闭弹框，但是这时候。如果有限制的话没有开启预检查冲突与限制的开关，也有问题，所以这里也要处理下。
   - 状态：已解决
   - 问题描述：点击"仅排通过检查的排课"但没有可排数据时，预检查开关未自动开启，用户看不到具体限制信息
   - 解决方案：
     - 在 handleContinuePartialPublish 函数中添加自动开启预检查逻辑
     - 当没有可排数据时，自动开启预检查开关
     - 自动触发预检查以显示最新的限制信息
     - 提供用户友好的提示信息
   - 实现效果：用户能清楚看到具体的限制信息，避免"信息黑洞"问题

6. 检查接口通过，但是现在页面还是弹出了检查提示
   - 返回JSON：{
            "IsSuccess": true,
            "ErrorMsg": "草稿发布成功",
            "ErrorCode": 200,
            "Data": [
                {
                    "DraftId": "7b2796ba-f9f0-41cc-9027-10d83f3c6454",
                    "ErrorFieldList": [],
                    "CheckFieldList": [],
                    "ConflictFieldList": {
                        "FieldNameList": [],
                        "ErrorMessage": null,
                        "ConflictingCourseList": [],
                        "ConflictingDraftList": [],
                        "ConflictingScheduleList": []
                    }
                }
            ]
        }

7. 确认排课的时候，后台返回的：{"IsSuccess":false,"ErrorMsg":"草稿数据存在验证错误","ErrorCode":200,"Data":[]}，但是前端还是弹框了，这时候就提示后台返回的消息把，message.warning .根据IsSuccess判断