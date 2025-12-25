<!-- 打印点名表 -->
<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="print-arrange-student" 
        width="800px" 
        top="5%" 
        title="点名表"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close" 
    >
        <div class="min-h-600px" v-loading="loading">
            <!-- 头部信息 -->
            <div class="pt-20px">
                <div>
                    <div class="info-title mb-10px text-16px text-center">
                        {{ currentCourse?.ClassName || '-' }}
                        <span v-if="currentCourse?.SubjectName">{{ currentCourse.SubjectName }}</span>
                    </div>
                    <div class="grid grid-cols-3 gap-y-8px">
                        <div class="info-item">
                            <label>{{transToConfigDescript('上课校区：')}}</label>
                            <span>{{ currentCourse?.CampusName || '-' }}</span>
                        </div>
                        <div class="info-item">
                            <label>{{transToConfigDescript('上课时间：')}}</label>
                            <span>{{ currentCourse?.CourseTime || '-' }}</span>
                        </div>
                        <div class="info-item">
                            <label>{{transToConfigDescript('上课教室：')}}</label>
                            <span>{{ currentCourse?.ClassroomName || '-' }}</span>
                        </div>
                        <div class="info-item">
                            <label>{{transToConfigDescript('任课老师：')}}</label>
                            <span>{{ currentCourse?.TeacherName || '-' }}</span>
                        </div>
                        <div class="info-item">
                            <label>打印时间：</label>
                            <span>{{ printTime }}</span>
                        </div>
                        <div class="info-item">
                            <label>实到/应到：</label>
                            <span>{{ currentCourse?.AttendanceCount || 0 }}/{{ currentCourse?.StudentCount || 0 }}</span>
                        </div>
                        <div class="info-item">
                            <label>{{transToConfigDescript('上课内容：')}}</label>
                            <span>{{ currentCourse?.CourseContent || '-' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 学员表格 -->
            <div class="pt-16px">
                <table cellpadding="0" cellspacing="0" border="1" width="100%" class="student-table">
                    <thead>
                        <tr>
                            <th class="text-left" width="100px">学号</th>
                            <th class="text-left" width="150px">姓名</th>
                            <th class="text-left" width="100px">性别</th>
                            <th class="text-left" width="120px">手机号码</th>
                            <th class="text-left" width="150px">出勤</th>
                            <th v-if="!IsOpenShiftForDay || currentCourse?.Unit != 3" class="text-left" width="150px">计费</th>
                            <th class="text-left" width="150px">备注</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="item in currentCourse?.StudentList || []" :key="item.Serial">
                            <td class="text-left">{{ item.Serial }}</td>
                            <td class="text-left">
                                {{ item.Name }}{{ item.IsMend == 1 ? "_补课" : "" }}
                                <span v-if="(item.AdjustFlag == 2) || (item.AdjustFlag == 3 && item.IsSubscribeCourse == 0)">{{ item.AdjustFlag == 2 ? "[临调]" : "[临加]" }}</span>
                                <span v-if="item.IsAttendStauts == 1">[请假]</span>
                                <span v-if="item.StudentStatus == -1">[已删除]</span>
                                <span v-if="item.IsSubscribeCourse == 1">[约课]</span>
                            </td>
                            <td class="text-left">{{ item.Sex == 1 ? "男" : item.Sex == 2 ? "女" : "未知" }}</td>
                            <td class="text-left">{{ item.SMSTel }}</td>
                            <td class="text-left">
                                <span v-if="item.IsAttend == 1">✔</span>
                                <span v-else-if="item.IsAttend == 0">{{ item.AbsentCauseName }}</span>
                            </td>
                            <td v-if="!IsOpenShiftForDay || currentCourse?.Unit != 3" class="text-left">
                                <span v-if="item.IsCost > 0">✔</span>
                            </td>
                            <td class="text-left">{{ item.Describe }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- 分页 -->
            <div v-if="list.length > 1" class="flex justify-end items-center py-16px px-20px">
                <el-pagination
                    :current-page="currentPageIndex + 1"
                    :page-size="1"
                    :total="list.length"
                    layout="total, prev, pager, next, jumper"
                    :pager-count="5"
                    @current-change="handlePageChange"
                    background
                    small
                />
            </div>
        </div>

        <template #footer>
            <div class="flex justify-end gap-12px">
                <el-button @click="close">取消</el-button>
                <el-button type="primary" @click="handlePrint">打印</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { getAttendancePrint } from '@/api/arrange';
import { dayjs } from 'element-plus'
import { useConfigs } from '@/store';
import '@/assets/lib/webprint.js'
import { transToConfigDescript } from '@/utils/filters/filters';

const configs = computed(() => {
	return useConfigs().configs
})
const IsOpenShiftForDay=computed(()=>{ //开启按天计费或者按月计费功能：0按月计费（默认），1按天计费。
	return configs.value.IsOpenShiftForDay==1;
})
const dialogVisible = ref(false);
const loading = ref(false);
const list = ref<any[]>([]);
const currentPageIndex = ref(0);



// 当前课程数据
const currentCourse = computed(() => {
    return list.value[currentPageIndex.value] || null;
});

// 打印时间
const printTime = computed(() => {
    return dayjs().format('YYYY-MM-DD HH:mm:ss');
});

// 页面切换处理
function handlePageChange(page: number) {
    currentPageIndex.value = page - 1;
}


// 打印功能
async function handlePrint() {
    if (!list.value || list.value.length === 0) return;
    
    try {
        // 生成所有页面的打印内容
        const printContents = generateAllPagesPrintContent();
        console.log('Generated print contents for', printContents.length, 'pages');
        
        // 调用 webPrint 进行打印，每页一份
        (window as any).webPrint(printContents, [], false, false, false);
        console.log('Print command sent successfully');
    } catch (error) {
        console.error('Failed to print:', error);
    }
}

// 生成所有页面的打印内容
function generateAllPagesPrintContent() {
    if (!list.value || list.value.length === 0) return [];
    
    return list.value.map((course, index) => ({
        content: generateSinglePagePrintContent(course, index + 1),
        times: 1,
        margin: '1cm'
    }));
}

// 生成单页打印内容的 HTML
function generateSinglePagePrintContent(course: any, pageNumber: number) {
    if (!course) return '';
    
    const students = course.StudentList || [];
    
    // 生成学员表格行
    const studentRows = students.map((item: any) => {
        const nameWithStatus = `${item.Name}${item.IsMend == 1 ? "_补课" : ""}`;
        const statusTags = [];
        if (item.AdjustFlag == 2) statusTags.push("[临调]");
        if (item.AdjustFlag == 3 && item.IsSubscribeCourse == 0) statusTags.push("[临加]");
        if (item.IsAttendStauts == 1) statusTags.push("[请假]");
        if (item.StudentStatus == -1) statusTags.push("[已删除]");
        if (item.IsSubscribeCourse == 1) statusTags.push("[约课]");
        
        const attendanceStatus = item.IsAttend == 1 ? "✔" : item.AbsentCauseName || "";
        const costStatus = item.IsCost > 0 ? "✔" : "";
        const sexText = item.Sex == 1 ? "男" : item.Sex == 2 ? "女" : "未知";
        
        return `
            <tr>
                <td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">${item.Serial}</td>
                <td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">
                    ${nameWithStatus}${statusTags.join('')}
                </td>
                <td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">${sexText}</td>
                <td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">${item.SMSTel}</td>
                <td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">${attendanceStatus}</td>
                ${(!IsOpenShiftForDay.value || course.Unit != 3) ? `<td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">${costStatus}</td>` : ''}
                <td style="padding: 8px; border: 1px solid #dcdfe6; text-align: left;">${item.Describe || ''}</td>
            </tr>
        `;
    }).join('');
    
    // 生成完整的打印内容
    return `
        <div style="font-family: Arial, sans-serif; color: #000;">
            <!-- 标题 -->
            <div style="text-align: center; font-size: 16px; font-weight: bold; margin-bottom: 20px;">
                ${course.ClassName || '-'}
                ${course.SubjectName ? `<span>${course.SubjectName}</span>` : ''}
            </div>
            
            <!-- 课程信息 -->
            <div style="margin-bottom: 20px;">
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; font-size: 12px;">
                    <div>${transToConfigDescript('上课校区：')}${course.CampusName || '-'}</div>
                    <div>${transToConfigDescript('上课时间：')}${course.CourseTime || '-'}</div>
                    <div>${transToConfigDescript('上课教室：')}${course.ClassroomName || '-'}</div>
                    <div>${transToConfigDescript('任课老师：')}${course.TeacherName || '-'}</div>
                    <div>打印时间：${printTime.value}</div>
                    <div>实到/应到：${course.AttendanceCount || 0}/${course.StudentCount || 0}</div>
                    <div>${transToConfigDescript('上课内容：')}${course.CourseContent || '-'}</div>
                </div>
            </div>
            
            <!-- 学员表格 -->
            <table style="width: 100%; border-collapse: collapse; font-size: 12px; color: #303133;">
                <thead>
                    <tr>
                        <th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 100px;">学号</th>
                        <th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 150px;">姓名</th>
                        <th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 100px;">性别</th>
                        <th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 120px;">手机号码</th>
                        <th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 150px;">出勤</th>
                        ${(!IsOpenShiftForDay.value || course.Unit != 3) ? '<th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 150px;">计费</th>' : ''}
                        <th style="padding: 8px; border: 1px solid #dcdfe6; text-align: left; font-weight: bold; width: 150px;">备注</th>
                    </tr>
                </thead>
                <tbody>
                    ${studentRows}
                </tbody>
            </table>
        </div>
    `;
}

function close() {
    dialogVisible.value = false;
    _reject();
}

async function queryCourseDetail() {
    loading.value = true;
    try {
        const res = await getAttendancePrint({
            courseIds: ids.value,
            // courseIds: '5e518afd-e3b0-4f12-804f-eec124813975,10fe1300-d2fd-48ab-b793-b389af193314'
        });
        list.value = res.Data || [];
        currentPageIndex.value = 0; // 重置到第一页
    } catch (error) {
    } finally {
        loading.value = false;
    }
}

let _resolve: any = null,
    _reject: any = null,
    ids = ref('');

function open(params: any) { //ids: 逗号拼接：1,2,3,4,5
    dialogVisible.value = true;
    ids.value = params.ids;
    queryCourseDetail();
    
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    });
}

defineExpose({
    open
});
</script>

<style lang="scss" scoped>
.print-arrange-student {
    .info-title {
        font-size: 16px;
        line-height: 1.5;
    }
    .info-item {
        font-size: 12px;
        line-height: 1.5;
    }
    
    .student-table {
        border-collapse: collapse;
        font-size: 12px;
        color: #303133;
        
        th, td {
            padding: 8px;
            border: 1px solid #dcdfe6;
            text-align: left;
        }
        
        th {
            font-weight: 700;
        }
    }
}
</style>
