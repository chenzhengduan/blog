<template>
    <el-drawer v-model="drawer" title="添加考试" direction="rtl" size="386px" class="wtwo-drawer addExamForm"
        :close-on-click-modal="false" :append-to-body="true" @close="close" :destroy-on-close="true">
        <div class="drawer-body-wrap" v-loading="loading">
            <el-form label-position="top" ref="formRef" :model="form" :rules="rules" label-width="140px"
                class="exam-form" hide-required-asterisk>
                <div class="group-title">考试范围</div>
                <el-form-item prop="CampusIds">
                    <template #label>
                        <div class="flex-center" style="display: inline-flex !important;">
                            <span class="form-label mr-4px">适用校区<span class="required-asterisk">*</span></span>
                            <el-tooltip class="box-item" effect="dark" content="适用校区的老师可查看考试，适用校区的学员可参与考试。"
                                placement="top">
                                <el-icon size="18px">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-xinxitishi"></use>
                                    </svg>
                                </el-icon>
                            </el-tooltip>
                        </div>
                    </template>
                    <el-select v-model="form.CampusIds" filterable collapse-tags collapse-tags-tooltip placeholder="请选择" style="width: 100%" multiple>
                        <el-option v-for="item in userCampuses" :key="item.ID" :label="item.Name" :value="item.ID" />
                    </el-select>
                </el-form-item>
                <el-form-item prop="CourseIds">
                    <template #label>
                        <div class="flex-center" style="display: inline-flex !important;">
                            <span class="form-label mr-4px">适用课程</span>
                            <el-tooltip class="box-item" effect="dark" content="报读适用课程的学员可参与考试。" placement="top">
                                <el-icon size="18px">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-xinxitishi"></use>
                                    </svg>
                                </el-icon>
                            </el-tooltip>
                        </div>
                    </template>
                    <input-tag :selected="courseSelected" :multiple="true" placeholder="请选择" :show-delete="true"
                        @click="selectCourse">
                        <template #btn-icon>
                            <el-icon size="18px">
                                <svg aria-hidden="true">
                                    <use xlink:href="#w2-xuanke"></use>
                                </svg>
                            </el-icon>
                        </template>
                    </input-tag>
                </el-form-item>
                <div class="group-title">考试信息</div>
                <el-form-item prop="ExamName">
                    <template #label>
                        <span class="form-label">考试名称<span class="required-asterisk">*</span></span>
                    </template>
                    <el-input v-model="form.ExamName" maxlength="50" placeholder="请输入" />
                </el-form-item>
                <el-form-item prop="ExamItemIds">
                    <template #label>
                        <div class="flex-center" style="display: inline-flex !important;">
                            <span class="form-label mr-4px">考试项目<span class="required-asterisk">*</span></span>
                            <el-tooltip class="box-item" effect="dark" content="一场考试最多只能添加10项" placement="top">
                                <el-icon size="18px">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-xinxitishi"></use>
                                    </svg>
                                </el-icon>
                            </el-tooltip>
                        </div>
                    </template>
                    <input-tag 
                        :selected="examItemSelected" 
                        :multiple="true" 
                        placeholder="请选择" 
                        :show-delete="true"
                        @click="selectExamItem"
                    >
                        <template #text> 
                            <div>选择</div>
                        </template>
                    </input-tag>
                </el-form-item>
                <el-form-item label="考试科目" prop="SubjectIds">
                    <el-select v-model="form.SubjectIds" filterable placeholder="请选择" style="width: 100%">
                        <el-option v-for="item in SUBJECT" :key="item.ID" :label="item.Name" :value="item.ID" />
                    </el-select>
                </el-form-item>
                <el-form-item label="考试年级" prop="GradeIds">
                    <el-select v-model="form.GradeIds" filterable placeholder="请选择考试年级" style="width: 100%">
                        <el-option v-for="item in GRADE" :key="item.ID" :label="item.Name" :value="item.ID" />
                    </el-select>
                </el-form-item>
                <el-form-item label="考试类型" prop="ExamType">
                    <el-select v-model="form.ExamType" filterable placeholder="请选择" style="width: 100%">
                        <el-option v-for="item in EXAM_TYPE" :key="item.ID" :label="item.Name" :value="item.ID" />
                    </el-select>
                </el-form-item>
                <el-form-item label="考试日期" prop="ExamDate">
                    <el-date-picker v-model="form.ExamDate" placeholder="请选择" type="date" format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
                <el-form-item label="成绩公布时间" prop="PublicTime">
                    <template #label>
                        <div class="flex-center" style="display: inline-flex !important;">
                            <span class="form-label mr-4px">成绩公布时间</span>
                            <el-tooltip class="box-item" effect="dark" content="学员在师生信可查看到本次考试成绩的时间。" placement="top">
                                <el-icon size="18px">
                                    <svg aria-hidden="true">
                                        <use xlink:href="#w2-xinxitishi"></use>
                                    </svg>
                                </el-icon>
                            </el-tooltip>
                        </div>
                    </template>
                    <el-date-picker v-model="form.PublicTime" type="datetime" placeholder="请选择" style="width: 100%"
                        format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" />
                </el-form-item>
                <el-form-item label="备注" prop="Memo">
                    <el-input v-model="form.Memo" :rows="3" type="textarea" placeholder="请输入" maxlength="200" />
                </el-form-item>

            </el-form>
        </div>
        <template #footer>
            <div class="wtwo-drawer-footer flex-between">
                <div></div>
                <div class="flex-center">
                    <el-button @click="close" :disabled="loading">取消</el-button>
                    <el-button type="primary" @click="submit" :disabled="loading">确认</el-button>
                </div>
            </div>
        </template>

        <!-- 选择课程弹窗 -->
        <ChooseCourse ref="chooseCourseRef" />
        
        <!-- 选择考试项弹窗 -->
        <ChooseExamItem ref="chooseExamItemRef" />
    </el-drawer>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleClose } from '@element-plus/icons-vue'
import { useCurrentCampuses, useUserCampuses } from '@/store'
import { useDictFieldsStore } from '@/store/dict'
import { storeToRefs } from 'pinia'
import { createExam, getExamDetail } from '@/api/exam'
import InputTag from '@/components/common/input-tag/inputTag.vue'
import ChooseCourse from '@/components/popup/chooseCourse.vue'
import ChooseExamItem from '@/components/popup/chooseExamItem.vue'

const drawer = ref(false)
const loading = ref(false)
const formRef = ref()

// 是否为复制模式
const isCopyMode = ref(false)

// 字典字段获取
const fieldsStore = useDictFieldsStore()
const { dictFields } = storeToRefs(fieldsStore)

// 可操作校区来自全局 store
const userCampusesStore = useUserCampuses()
const userCampuses = userCampusesStore.userCampuses || []
const currentCampus = computed(() => {
	return useCurrentCampuses().campusList
})
// 字典字段计算属性
const SUBJECT = computed(() => {
    return dictFields.value('SUBJECT').filter((item: any) => item.Status == 1)
})

const GRADE = computed(() => {
    return dictFields.value('SHIFT_GRADE').filter((item: any) => item.Status == 1)
})

const EXAM_TYPE = computed(() => {
    return dictFields.value('EXAM_TYPE').filter((item: any) => item.Status == 1)
})

// 表单数据
const form = reactive({
    Id:'',
    CampusId:'',  // 归属校区ID
    CampusIds: [] as string[], // 适用校区ID列表
    ExamName: '',
    ExamType: '',
    ExamItemIds: [] as string[],
    SubjectIds: '' as string,  // 界面单选，提交时转为数组
    GradeIds: '' as string,    // 界面单选，提交时转为数组
    CourseIds: [] as string[],
    ExamDate: '',
    PublicTime: '',
    Memo: ''
})


// 表单验证规则
const rules = {
    CampusIds: [
        {
            required: true,
            message: '请选择适用校区',
            trigger: 'change',
            validator: (rule: any, value: any, callback: any) => {
                if (!value || !Array.isArray(value) || value.length === 0) {
                    callback(new Error('请选择适用校区'))
                } else {
                    callback()
                }
            }
        }
    ],
    ExamName: [
        { required: true, message: '请输入考试名称', trigger: 'blur' }
    ],
    ExamItemIds: [
        {
            required: true,
            message: '请选择考试项',
            trigger: 'change',
            validator: (rule: any, value: any, callback: any) => {
                if (!value || !Array.isArray(value) || value.length === 0) {
                    callback(new Error('请选择考试项'))
                } else if (value.length > 20) {
                    callback(new Error('一场考试最多只能添加20项'))
                } else {
                    callback()
                }
            }
        }
    ],
}

// 选项数据（目前没有需要加载的选项数据）

// 课程选择相关
const courseSelected = ref<any[]>([])
const chooseCourseRef = ref()

// 考试项选择相关
const examItemSelected = ref<any[]>([])
const chooseExamItemRef = ref()

// 考试日期不再需要默认时间配置

// 选择课程
function selectCourse() {
    console.log('打开课程选择弹窗，当前已选择:', courseSelected.value)
    chooseCourseRef.value?.open({
        multi: true,
        choosed: courseSelected.value,
        showCampus: true,
        showShiftType: true
    }).then((res: any) => {
        if (res.data) {
            console.log('课程选择变化:', res.data)
            courseSelected.value = res.data
            form.CourseIds = res.data.map((item: any) => item.ID)
            console.log('courseSelected.value:', courseSelected.value)
            console.log('form.CourseIds:', form.CourseIds)
        }
    })
}

// 选择考试项
function selectExamItem() {
    console.log('打开考试项选择弹窗，当前已选择:', examItemSelected.value)
    chooseExamItemRef.value?.open({
        multi: true,
        choosed: examItemSelected.value,
        showCampus: false,
        showShiftType: false,
        maxNum: 20

    }).then((res: any) => {
        if (res.data) {
            
            console.log('考试项选择变化:', res.data)
            examItemSelected.value = res.data
            form.ExamItemIds = res.data.map((item: any) => item.ID)
            console.log('examItemSelected.value:', examItemSelected.value)
            console.log('form.ExamItemIds:', form.ExamItemIds)
        }
    })
}

// 加载考试详情（用于复制）
async function loadExamDetail(examId: string) {
    loading.value = true
    try {
        const res = await getExamDetail({ id: examId })
        if (res.ErrorCode === 200 && res.Data) {
            const detail = res.Data

            // 回显表单数据（复制时不填充考试日期和成绩公布时间）
            form.ExamName = (detail.ExamName || '') 
            form.ExamType = detail.ExamType || ''
            form.ExamDate = '' // 复制时不填充考试日期
            form.PublicTime = '' // 复制时不填充成绩公布时间
            form.SubjectIds = detail.SubjectIds && detail.SubjectIds.length > 0 ? detail.SubjectIds[0] : ''
            form.GradeIds = detail.GradeIds && detail.GradeIds.length > 0 ? detail.GradeIds[0] : ''
            form.CampusIds = detail.CampusIds || []
            form.CourseIds = detail.CourseIds || []
            form.ExamItemIds = detail.ExamItemIds || []
            form.Memo = detail.Memo || ''

            // 回显课程选择
            if (detail.Courses && Array.isArray(detail.Courses)) {
                courseSelected.value = detail.Courses.map((item:any) => ({
                    // 因后端返回字段不统一，暂时做兼容处理
                    ID: item.ID||item.Id,
                    Id: item.ID||item.Id,
                    Name: item.Name,
                    GradeName: item.GradeName || item.GradeDesc || ''
                }))
            }

            // 回显考试项选择
            if (detail.ExamItems && Array.isArray(detail.ExamItems)) {
                examItemSelected.value = detail.ExamItems?.map((item:any) => ({
                    // 因后端返回字段不统一，暂时做兼容处理
                    ID: item.ID||item.Id,
                    Id: item.ID||item.Id,
                    Name: item.Name,
                    status: item.status,  // status 态：1=正常，0=已删除
                    FullScore: item.FullScore,
                    PassScore: item.PassScore
                }))
            }

            console.log('复制考试数据加载成功:', detail)
        } else {
            ElMessage.error(res.ErrorMsg || '加载考试详情失败')
            // 接口报错不影响程序执行，不关闭抽屉，让用户可以手动关闭
        }
    } catch (error) {
        console.error('加载考试详情失败:', error)
        // ElMessage.error('加载考试详情失败，请重试')
        // 接口报错不影响程序执行，不关闭抽屉，让用户可以手动关闭
    } finally {
        loading.value = false
    }
}

// 提交表单
function submit() {
    formRef.value?.validate((valid: boolean) => {
        if (valid) {
            loading.value = true

            // 构建API参数
            const apiParams: any = {
                ExamName: form.ExamName,
                ExamType: form.ExamType,
                ExamDate: form.ExamDate,
                PublicTime: form.PublicTime,
                SubjectIds: form.SubjectIds ? [form.SubjectIds] : [],  // 单选转数组
                GradeIds: form.GradeIds ? [form.GradeIds] : [],        // 单选转数组
                CourseIds: form.CourseIds,
                CampusIds: form.CampusIds,
                ExamItemIds: form.ExamItemIds,  // 考试项参数
                Memo: form.Memo,
                CampusId: currentCampus.value  // 归属校区ID
            }
            
            console.log('apiParams:', apiParams)
            
            createExam(apiParams)
                .then((res: any) => {
                    if (res.ErrorCode === 200) {
                        ElMessage.success('考试新增成功')
                        _resolve()
                        drawer.value = false
                    } else {
                        ElMessage.error(res.ErrorMsg || '考试新增失败，请重试')
                    }
                })
                .catch((error: any) => {
                    console.error('创建考试失败:', error)
                    ElMessage.error('考试新增失败，请重试')
                })
                .finally(() => {
                    loading.value = false
                })
        }
    })
}

// 重置表单
function resetForm() {
    Object.assign(form, {
        CampusIds: [],
        ExamName: '',
        ExamType: '',
        ExamItemIds: [],
        SubjectIds: '',  // 单选，重置为空字符串
        GradeIds: '',    // 单选，重置为空字符串
        CourseIds: [],
        ExamDate: '',
        PublicTime: '',
        Memo: ''
    })
    // 重置课程选择
    courseSelected.value = []
    // 重置考试项选择
    examItemSelected.value = []
    // 重置复制模式标记
    isCopyMode.value = false
    formRef.value?.clearValidate()
}

// 关闭抽屉
function close() {
    console.log('关闭抽屉')
    // 点击考试时，将isSingleCampus设置为false  校区是多选的
    window.microApp.dispatch({type:'examTab: isSingleCampus',isSingleCampus: false})
    drawer.value = false
    resetForm()
}

let _resolve: any = null, _reject: any = null

// 对外暴露的open方法
function open(params: any = {}) {
    return new Promise((resolve, reject) => {
        console.log('params', params)
        _resolve = resolve
        _reject = reject
        drawer.value = true;

        // 点击考试时，将isSingleCampus设置为true  校区是单选的
        window.microApp.dispatch({type:'examTab: isSingleCampus',isSingleCampus: true})

        
        // 如果传入了 ExamID，表示是复制模式
        if (params.ExamID) {
            isCopyMode.value = true
            loadExamDetail(params.ExamID)
        }

    })
}

onMounted(() => {
})

defineExpose({
    open
})
</script>

<style lang="scss" scoped>
.addExamForm {
    .exam-form {
        overflow-y: auto;
        padding: 20px;

        .group-title {
            font-size: 14px;
            color: #303133;
            font-weight: 600;
            margin-bottom: 10px;
        }

        .form-label {
            font-size: 14px;
            color: #606266;
            font-weight: 500;
        }
    }

    // 设置 label-top 模式下的标签样式
    // Element Plus 的 label-position="top" 对应的类名是 el-form--label-top
    :deep(.el-form--label-top .el-form-item__label) {
        display: inline-flex !important;
    }

    // 如果项目使用了 wtwo 前缀的类名
    :deep(.wtwo-form--label-top .wtwo-form-item__label) {
        display: inline-flex !important;
    }

    // 更通用的选择器，针对所有表单项标签
    :deep(.el-form-item__label) {
        display: inline-flex !important;
    }

    // 直接针对当前表单内的所有标签
    :deep(.exam-form .el-form-item__label) {
        display: inline-flex !important;
    }


    // 自定义必填星号样式
    .required-asterisk {
        color: #f56c6c;
        margin-left: 4px;
    }


    .form-label {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
    }

    // 针对 no-asterisk 类的样式
    :deep(.no-asterisk::before),
    :deep(.no-asterisk::after) {
        display: none !important;
        content: none !important;
        visibility: hidden !important;
        opacity: 0 !important;
    }

}
</style>
