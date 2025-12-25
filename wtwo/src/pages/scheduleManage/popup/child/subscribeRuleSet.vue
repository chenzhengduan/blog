<template>
    <!-- 学员约课规则 -->
    <div class="subscribe-rule-setting drawer-body-wrap">
        <el-scrollbar class="drawer-content" v-loading="loading">
            <!-- 规则说明 -->
            <div class="rule-tips-wrap m-16px">
                <div class="flex-between">
                    <div class="flex-center">
                        <el-icon size="16px" color="#909399" class="mr-8px">
                            <InfoFilled />
                        </el-icon>
                        <span class="tips-title">规则描述</span>
                    </div>
                </div>
                <div class="tips-desc">学员自助在师生信约课的规则设置</div>
            </div>
            <!-- 约课显示配置 -->
            <div class="form-wrap">
                <el-form :model="rules" :scroll-to-error="true" ref="ruleFormRef">
                    <!-- 约课页面显示设置 -->
                    <div class="form-wrap-group">
                        <div class="main-title">约课页面显示设置</div>
                        <div class="form-wrap-item">
                            <div class="sub-label">显示方式</div>
                            <el-radio-group v-model="rules.ShowRule" disabled class="display-mode-group">
                                <el-radio :value="0">
                                    {{transToConfigDescript('按校区显示')}}
                                </el-radio>
                                <div class="radio-desc">
                                    {{transToConfigDescript('师生信约课页面的课节"内容组织方式"是按校区显示。注意：仅显示学员报读的课程。')}}
                                    <!-- <el-text type="primary" class="text-12px! cursor-pointer">查看示例</el-text> -->
                                </div>

                                <el-radio :value="1">
                                    {{transToConfigDescript('按老师显示')}}
                                </el-radio>
                                <div class="radio-desc">
                                    {{transToConfigDescript('师生信约课页面的课节"内容组织方式"是按任课老师显示。注意：仅显示学员报读课程下的任课老师。')}}
                                    <!-- <el-text type="primary" class="text-12px! cursor-pointer">查看示例</el-text> -->
                                </div>
                            </el-radio-group>
                        </div>
                    </div>
                    <!-- 约课规则设置 -->
                    <div class="form-wrap-group">
                        <div class="main-title">约课规则设置</div>
                        <el-form-item class="pl-11px" label="哪些学员可以预约" prop="StudentStatus">
                            <el-select disabled v-model="rules.StudentStatus" placeholder="不限" clearable multiple
                                class="!w-230px">
                                <el-option v-for="item in studentStatusOptions" :key="item.ID" :label="transToConfigDescript(item.Name)"
                                    :value="parseInt(item.ID)" />
                            </el-select>
                        </el-form-item>
                        <el-form-item class="no-padding-label" prop="MaxDays" :rules="[
                            { required: true, message: '请输入预约天数', trigger: 'blur' },
                            { pattern: /^[1-9][0-9]?$/, message: '请输入1-99的数字', trigger: 'blur' }
                        ]">
                            <template #label></template>
                            <div class="input-group-inline">
                                学员可预约
                                <el-input disabled v-model="rules.MaxDays" maxlength="2" placeholder="请输入"
                                    class="!w-100px mx-8px" />
                                天内的课
                                <el-tooltip effect="dark" content="例如，设置为2，表示家长端只能预约今、明两天的课。" placement="top">
                                    <el-icon size="16px" color="#999" class="ml-4px cursor-pointer">
                                        <InfoFilled />
                                    </el-icon>
                                </el-tooltip>
                            </div>
                        </el-form-item>
                        <el-form-item class="no-padding-label" prop="ApplyBeforeHours" :rules="[
                            { required: true, message: '请输入提前预约小时数', trigger: 'blur' },
                            { pattern: /^[1-9][0-9]?$/, message: '请输入1-99的数字', trigger: 'blur' }
                        ]">
                            <template #label></template>
                            <div class="input-group-inline">
                                学员须提前
                                <el-input disabled v-model="rules.ApplyBeforeHours" maxlength="2" placeholder="请输入"
                                    class="!w-100px mx-8px" />
                                小时预约
                            </div>
                        </el-form-item>
                        <el-form-item class="no-padding-label" prop="CancelBeforeHours" :rules="[
                            { required: true, message: '请输入取消预约限制小时数', trigger: 'blur' },
                            { pattern: /^[1-9][0-9]?$/, message: '请输入1-99的数字', trigger: 'blur' }
                        ]">
                            <template #label></template>
                            <div class="input-group-inline">
                                {{transToConfigDescript('上课前')}}
                                <el-input disabled v-model="rules.CancelBeforeHours" maxlength="2" placeholder="请输入"
                                    class="!w-127px mx-8px" />
                                小时不可取消预约
                            </div>
                        </el-form-item>
                        <div class="form-wrap-item">
                            <el-checkbox disabled v-model="autoBookingEnabled" @change="handleAutoBookingChange">
                                自动取消排课
                            </el-checkbox>
                            <div class="input-group-inline pl-22px" v-if="autoBookingEnabled">
                                {{transToConfigDescript('上课前')}}
                                <el-input disabled v-model="rules.AutoCancelHours" maxlength="2" placeholder="请输入"
                                    class="!w-100px mx-8px" />
                                小时未达到"开课人数"自动取消排课
                            </div>
                        </div>
                        <div class="form-wrap-item">
                            <div class="sub-label">
                                <el-checkbox disabled v-model="limitCountOpen">
                                    限制预约次数
                                </el-checkbox>
                            </div>
                            <div v-show="limitCountOpen" class="flex flex-col pl-22px">
                                <div class="limit-item flex items-center mb-12px">
                                    <el-checkbox disabled v-model="timeLimitEnabled" @change="handleTimeLimitChange"
                                        class="!mr-8px" />
                                    <el-select v-model="limitTimePeriod" placeholder="选择类型" class="!w-100px" disabled
                                        @change="updateLimitType" @click.stop>
                                        <el-option label="每天" :value="1"></el-option>
                                        <el-option label="每周" :value="2"></el-option>
                                    </el-select>
                                    <el-select v-model="rules.LimitShift" placeholder="选择类型" class="!w-150px ml-8px"
                                        disabled @click.stop>
                                        <el-option :label="transToConfigDescript('不限课程')" :value="0"></el-option>
                                        <el-option :label="transToConfigDescript('限制同一课程')" :value="1"></el-option>
                                    </el-select>
                                    <span class="mx-8px">最多可约</span>
                                    <el-form-item prop="LimitMaxCount" :rules="limitMaxCountRules" class="!mb-0">
                                        <el-input v-model="rules.LimitMaxCount" maxlength="2" placeholder="请输入"
                                            class="!w-100px" disabled @click.stop />
                                    </el-form-item>
                                    <span class="ml-8px">次</span>
                                </div>
                                <div class="normal-desc mt-[-5px]"
                                    v-if="(limitTimePeriod == 1 || limitTimePeriod == 2) && rules.LimitMaxCount && timeLimitEnabled">
                                    {{ limitTimePeriod == 1 ? "每天" : "每周" }}的约课计划，{{ transToConfigDescript(rules.LimitShift == 0 ? "不限课程" :
                                    "限制同一课程")}}最多可以预约{{ rules.LimitMaxCount }}次。<span
                                        v-if="limitTimePeriod == 2">假设开放两周内可约，合计就可预约{{ 2 * rules.LimitMaxCount }}次。</span>
                                </div>
                                <div class="limit-item flex items-center">
                                    <el-checkbox disabled v-model="courseTimeLimitEnabled" @change="updateLimitType">
                                        <div class="flex items-center">
                                            不能超过剩余课时
                                            <el-tooltip effect="dark" :content="transToConfigDescript('检查本约课课程的剩余数量，不包含关联课程的剩余数量。')"
                                                placement="top">
                                                <el-icon size="16px" color="#999" class="ml-4px">
                                                    <InfoFilled />
                                                </el-icon>
                                            </el-tooltip>
                                        </div>
                                    </el-checkbox>
                                </div>
                            </div>
                        </div>
                        <div class="form-wrap-item">
                            <el-checkbox disabled v-model="rules.SkipConflict" :true-value="1" :false-value="0">
                                {{transToConfigDescript('预约时不检查学员的上课冲突')}}
                            </el-checkbox>
                            <div class="normal-desc">
                                勾选后，如果学员约课的时间已有其他排课，则给出提示并由学员/家长决定是否继续预约。
                                <!-- <el-text type="primary" class="text-12px! cursor-pointer">查看示例</el-text> -->
                            </div>
                        </div>
                        <div class="form-wrap-item">
                            <el-checkbox v-model="rules.IsQueue" disabled :true-value="1" :false-value="0">
                                限制人数满员时，可以排队
                            </el-checkbox>
                            <div class="normal-desc">
                                勾选时，可约人数满员时学员能继续预约，此时学员的预约状态是"排队中"待可约人数释放名额后，排队中的学员可自动替补预约成功。
                                <!-- <el-text type="primary" class="text-12px! cursor-pointer">查看帮助</el-text> -->
                            </div>
                        </div>
                        <div class="form-wrap-item">
                            <el-checkbox v-model="rules.ShowStudentCount" disabled :true-value="1" :false-value="0">
                                约课列表，显示剩余名额 
                            </el-checkbox>
                            <div class="normal-desc pb-20px">
                                勾选后，约课列表会显示已约与剩余的名额。 
                            </div>
                        </div>
                    </div>
                </el-form>
            </div>
        </el-scrollbar>
        <div class="drawer-footer flex-end">
            <div class="btn-wrap flex-center">
                <el-button @click="close">关闭</el-button>
                <el-button @click="openBackEndNew" >前往“管理后台”编辑</el-button>
                <!-- <el-button @click="submit" type="primary" :loading="loading" v-if="hasEditPermission">保存</el-button> -->
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, FormInstance } from 'element-plus'
import { getSubscribeCourseRule } from '@/api/arrange'
import { transToConfigDescript } from '@/utils/filters/filters'

const emit = defineEmits(['save', 'close'])

const hasEditPermission = ref(window.$xgj.op('NewCourse_SubscribeCourseRuleEdit')) // 修改约课规则权限
const loading = ref(false)
let id = ''

const openBackEndNew = () => {
    if(hasEditPermission.value){
        window.open(`${window.location.origin}/back_end#/businessRule?searchKey=学员约课规则`)
    }else{
        ElMessage.warning('暂无“编辑约课规则”的权限，请联系管理员!')
    }
}

// 学员状态选项
const studentStatusOptions = [
    { ID: '1', Name: '在读' },
    { ID: '3', Name: '休学' },
    { ID: '99', Name: '退学' },
]

// 规则配置
const rules = reactive({
    MaxDays: '', // 最多可预约多少天内的课
    ApplyBeforeHours: '', // 提前多少小时预约上课
    CancelBeforeHours: '', // 上课前多少小时内不可取消上课
    LimitType: '-1' as string, // 限制类型 -1不限制 1每天 2每周 10约课次数不超过课程的剩余课时，多选
    LimitShift: 0, // 0不限课程，1限制同一课程
    LimitMaxCount: undefined as number | undefined, // 最多可预约次数
    AutoCancelHours: -1 as number | string, // 上课前N小时未满足上课条件自动取消 -1不自动取消
    SkipConflict: 0, // 是否允许预约有冲突的排课
    StudentStatus: [] as number[], // 学员状态 多选数组
    IsQueue: 0, // 是否开启排队功能 1开启 0关闭
    ShowRule: 0, // 显示规则 0按校区（默认） 1按老师
    ShowStudentCount:0,
})

// 计算属性和辅助变量
const autoBookingEnabled = ref(false) // 自动取消排课开关
const limitCountOpen = ref(false) // 限制预约次数开关
const timeLimitEnabled = ref(false) // 时间限制开关（每天/每周）
const courseTimeLimitEnabled = ref(false) // 课时限制开关（不超过剩余课时）
const limitTimePeriod = ref(1) // 1=每天, 2=每周

// 限制次数校验规则
const limitMaxCountRules = computed(() => {
    if (timeLimitEnabled.value) {
        return [
            { required: true, message: '请输入预约次数', trigger: 'blur' },
            { pattern: /^[1-9][0-9]?$/, message: '请输入1-99的数字', trigger: 'blur' }
        ]
    }
    return []
})

// 自动取消排课开关变化处理
function handleAutoBookingChange(value: any) {
    if (!value) {
        rules.AutoCancelHours = -1
    } else {
        rules.AutoCancelHours = 1
    }
}

// 时间限制开关变化处理
function handleTimeLimitChange(value: any) {
    if (!value) {
        rules.LimitMaxCount = undefined
        // 清除表单验证错误
        ruleFormRef.value?.clearValidate(['LimitMaxCount'])
    }
    updateLimitType()
}

// 更新 LimitType 字段
function updateLimitType() {
    const limitTypes: string[] = []

    // 如果启用时间限制，添加时间周期类型
    if (timeLimitEnabled.value) {
        limitTypes.push(limitTimePeriod.value.toString())
    }

    // 如果启用课时限制，添加课时类型
    if (courseTimeLimitEnabled.value) {
        limitTypes.push('10')
    }

    // 更新 LimitType 字段
    if (limitTypes.length === 0 || !limitCountOpen.value) {
        rules.LimitType = '-1'
    } else {
        rules.LimitType = limitTypes.join(',')
    }
}

// 监听限制预约次数总开关
watch(limitCountOpen, (newVal) => {
    if (!newVal) {
        // 关闭所有子选项
        timeLimitEnabled.value = false
        courseTimeLimitEnabled.value = false
        rules.LimitMaxCount = undefined
        rules.LimitType = '-1'
    }
})

// 监听时间周期变化
watch(limitTimePeriod, () => {
    updateLimitType()
})

// 初始化数据
onMounted(() => {
    loadRuleData()
})

// 加载规则数据
function loadRuleData() {
    loading.value = true
    getSubscribeCourseRule().then((res: any) => {
        const { ApplyBeforeHours, AutoCancelHours, CancelBeforeHours, ID, IsQueue,ShowStudentCount, LimitMaxCount, LimitShift, LimitType, MaxDays, ShowRule, SkipConflict, StudentStatus } = res.Data

        Object.assign(rules, {
            MaxDays: MaxDays || '',
            ApplyBeforeHours: ApplyBeforeHours || '',
            CancelBeforeHours: CancelBeforeHours || '',
            LimitType: LimitType || '-1',
            LimitShift: LimitShift || 0,
            LimitMaxCount: LimitMaxCount || undefined,
            AutoCancelHours: AutoCancelHours || '',
            SkipConflict: SkipConflict || 0,
            StudentStatus: (!StudentStatus || StudentStatus === '-1') ? [] : StudentStatus.split(',').map(Number),
            IsQueue: IsQueue || 0,
            ShowRule: ShowRule || 0,
            ShowStudentCount:ShowStudentCount||0
        })

        // 设置辅助变量
        autoBookingEnabled.value = rules.AutoCancelHours !== -1
        limitCountOpen.value = rules.LimitType !== '-1'

        // 解析 LimitType 字符串
        if (rules.LimitType !== '-1') {
            const limitTypes = rules.LimitType.split(',').map(Number)

            // 检查是否包含时间限制（1或2）
            if (limitTypes.includes(1) || limitTypes.includes(2)) {
                timeLimitEnabled.value = true
                limitTimePeriod.value = limitTypes.includes(1) ? 1 : 2
            }

            // 检查是否包含课时限制（10）
            if (limitTypes.includes(10)) {
                courseTimeLimitEnabled.value = true
            }
        }

        id = ID
    }).finally(() => {
        loading.value = false
    })
}

const ruleFormRef = ref<FormInstance>()

// 数据转换函数 - 将表单数据转换为提交格式
function transformRulesForSubmit() {
    const submitRules = {
        ...rules,
        // 处理学员状态：如果为空数组，转换为字符串 '-1'
        StudentStatus: rules.StudentStatus.length === 0 ? '-1' : rules.StudentStatus.join(','),
        // 处理自动取消小时数
        AutoCancelHours: (!rules.AutoCancelHours || rules.AutoCancelHours === '') ? -1 : Number(rules.AutoCancelHours),
        // 处理限制次数
        LimitMaxCount: (!timeLimitEnabled.value || !rules.LimitMaxCount) ? 0 : Number(rules.LimitMaxCount),
        // 处理数值字段，确保是数字类型
        MaxDays: Number(rules.MaxDays),
        ApplyBeforeHours: Number(rules.ApplyBeforeHours),
        CancelBeforeHours: Number(rules.CancelBeforeHours)
    }

    return submitRules
}

// // 保存规则
// async function submit() {
//   if (!ruleFormRef.value) return

//   await ruleFormRef.value.validate((valid, fields) => {
//     if (valid) {
//       loading.value = true
//       putSubscribeCourseRule({
//         id,
//         value: JSON.stringify(transformRulesForSubmit())
//       }).then(() => {
//         ElMessage.success('修改成功')
//         emit('save')
//       }).finally(() => {
//         loading.value = false
//       })
//     } else {
//       console.log('表单验证失败！', fields)
//     }
//   })
// }

// 取消
function close() {
    // 重置表单数据
    Object.assign(rules, {
        MaxDays: '',
        ApplyBeforeHours: '',
        CancelBeforeHours: '',
        LimitType: '-1',
        LimitShift: 0,
        LimitMaxCount: undefined,
        AutoCancelHours: -1,
        SkipConflict: 0,
        StudentStatus: [],
        IsQueue: 0,
        ShowRule: 0,
        ShowStudentCount:0
    })

    // 重置辅助变量
    autoBookingEnabled.value = false
    limitCountOpen.value = false
    timeLimitEnabled.value = false
    courseTimeLimitEnabled.value = false
    limitTimePeriod.value = 1
    emit('close')
}
</script>

<style lang="scss" scoped>
.subscribe-rule-setting {
    height: 100%;
    display: flex;
    flex-direction: column;

    .drawer-content {
        flex: 1;
        overflow: auto;
    }

    .drawer-footer {
        flex-shrink: 0;
        padding: 16px;
        border-top: 1px solid #e4e7ed;

        .btn-wrap {
            // gap: 12px;
        }
    }

    .rule-tips-wrap {
        background: #f5f7fa;
        border-radius: 4px;
        padding: 12px;

        .tips-title {
            font-size: 14px;
            font-weight: 500;
            color: #606266;
        }

        .tips-desc {
            margin-top: 8px;
            font-size: 12px;
            color: #909399;
            line-height: 1.5;
        }
    }

    .form-wrap {
        padding: 0 16px;
        .form-wrap-group {
            margin-top: 20px;

            &:first-child {
                margin-top: 16px;
            }

            .form-wrap-item {
                margin-left: 14px;
                color: #606266;

                &+.form-wrap-item {
                    margin-top: 10px;
                }
            }

            .main-title {
                margin-bottom: 8px;
                font-weight: 600;
            }

            .sub-label {
                font-size: 14px;
                color: #333;
                margin: 16px 0 8px 0;
            }

            .sub-title {
                line-height: 1.3;
                margin-bottom: 8px;
                color: #909399;
                font-size: 12px;
                margin-left: 22px;
            }

            .display-mode-group {
                display: flex;
                flex-direction: column;
                align-items: flex-start;
                padding-left: 0;

                .wtwo-radio {
                    margin-bottom: 4px;
                }

                .radio-desc {
                    font-size: 12px;
                    color: #909399;
                    margin-bottom: 12px;
                    margin-left: 22px;
                    line-height: 1.3;
                }
            }

            .input-group-inline {
                display: flex;
                align-items: center;
                font-size: 14px;
                color: #606266;

                .wtwo-input {
                    :deep(.wtwo-input__inner) {
                        text-align: center;
                    }
                }
            }

            .limit-item {
                align-items: center;

                .wtwo-checkbox {
                    flex-shrink: 0;
                }
            }

            .normal-desc {
                font-size: 12px;
                color: #909399;
                margin-left: 22px;
                line-height: 1.3;
            }
        }
    }

    .no-padding-label {
        :deep(.wtwo-form-item__label) {
            padding: 0 !important;
        }
    }
}
</style>
