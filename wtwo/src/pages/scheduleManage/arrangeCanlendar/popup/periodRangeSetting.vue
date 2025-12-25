<template>
    <el-dialog 
        v-model="dialogVisible" 
        class="periodRangeSetting" 
        width="500px" 
        top="5%" 
        title="设置时间范围"
        :append-to-body="true"
        :close-on-click-modal="false" 
        :destroy-on-close="true" 
        draggable
        @close="close"
    >
        <div class="box-wrapper-table" v-loading="loading">
            <page-attention-tips class="mb-10px">
                课节会基于“<span class="color-[var(--wtwo-color-warning)]">开始时间</span>”归属到下方所设的范围中，若存在课节显示不全的情况，<span class="color-[var(--wtwo-color-warning)]">请检查是否有设置对应的时间范围</span>
            </page-attention-tips>
            <div
                class="table-wrap"
                ref="tableContainerRef"
            >
                <el-table :data="list">
                    <el-table-column label="时间范围">
                        <template #default="scope">
                            <!-- :campus-id="selectedCampusId||currentCampus" -->
                            <course-time-range
                                v-if="scope.row.IsEdit"
                                pickerWidth="100px"
                                v-model="scope.row.TimeRange"
                                :disabled="!scope.row.IsEdit">
                            </course-time-range>
                            <div v-else>
                                {{ scope.row.TimeRange && scope.row.TimeRange.length > 0 ? scope.row.TimeRange.join('-') : '' }}
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120">
                        <template #default="scope">
                            <template v-if="scope.row.IsEdit">
                                <el-button @click="cancelEdit(scope.row)" size="small">取消</el-button>
                                <el-button type="primary" @click="saveEdit(scope.row)" size="small">保存</el-button>
                            </template>
                            <template v-else>
                                <el-button type="primary" link @click="startEdit(scope.row)" :disabled="isEditing">编辑</el-button>
                                <el-button type="danger" link @click="deleteRange(scope.$index)">删除</el-button>
                            </template>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="flex-center pt-5px pb-5px" style="width: 100%;">
                    <el-link type="primary" underline="never" @click="addRange" class="ml-6px" :disabled="isEditing">
                        <div class="flex-center">
                            <el-icon size="16px" class="mr-8px">
                                <svg aria-hidden="true">
                                    <use
                                        xlink:href="#w2-tianjia"
                                    ></use>
                                </svg>
                            </el-icon>
                            <span>添加范围</span>
                        </div>
                    </el-link>
                </div>
            </div>
        </div>
        <template #footer>
            <div class="flex-end">
                <el-button plain @click="close">取消</el-button>
                <el-button :disabled="loading" type="primary" @click="submit">确定</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue';
import CourseTimeRange from '../../components/course-time-range.vue';
import { useUserSettings } from '@/store';
import { ElMessage, ElMessageBox } from 'element-plus';
import { saveUserSettingsList } from '@/api/arrange';

// 定义事件
const emit = defineEmits(['settings-updated']);

// const currentCampus = computed(() => {
// 	return useCurrentCampuses().campusList
// })

// 定义时间范围设置项的类型
interface TimeRangeSetting {
  ID?: string
  PageKey?: string
  PageName?: string
  Type?: number
  IsPublic?: number
  UserSettingsDetailList?: any[]
  TimeRange: string[]
  IsEdit: boolean
  IsNew?: boolean
  originalData?: TimeRangeSetting
}

const dialogVisible = ref(false)
const loading = ref(false)
const list = ref<TimeRangeSetting[]>([])

// 检查是否有任何行正在编辑中
const isEditing = computed(() => {
    return list.value.some((row: any) => row.IsEdit === true);
})

// 编辑相关函数
function startEdit(row: TimeRangeSetting) {
    row.IsEdit = true;
    row.IsNew = false; // 确保编辑行的IsNew标记为false
    // 保存原始数据用于取消编辑
    row.originalData = JSON.parse(JSON.stringify(row));
}

// 校验单个行的时间范围
function validateSingleTimeRange(row: TimeRangeSetting, index: number) {
    const timeRange = row.TimeRange;
    
    // 检查 TimeRange 是否为空或长度不足
    if (!timeRange || !Array.isArray(timeRange) || timeRange.length < 2) {
        ElMessage.error(`时间范围缺少起始或结束时间`);
        return false;
    }
    
    // 检查开始时间和结束时间是否都有值
    if (!timeRange[0] || !timeRange[1]) {
        ElMessage.error(`时间范围缺少起始或结束时间`);
        return false;
    }
    
    // 检查开始时间是否小于结束时间
    if (timeRange[0] >= timeRange[1]) {
        ElMessage.error(`时间范围的开始时间不能大于等于结束时间`);
        return false;
    }
    
    return true;
}

function saveEdit(row: TimeRangeSetting) {
    // 获取当前行的索引
    const index = list.value.indexOf(row);
    
    // 校验当前行的时间范围
    if (!validateSingleTimeRange(row, index)) {
        return;
    }
    
    // 检查与其他时段是否重叠
    const overlaps = [];
    for (let i = 0; i < list.value.length; i++) {
        // 跳过当前行本身
        if (i === index) {
            continue;
        }
        
        const otherRow = list.value[i];
        const range1 = row.TimeRange;
        const range2 = otherRow.TimeRange;
        
        // 检查两个时段是否重叠：range1.start < range2.end && range1.end > range2.start
        if (range1[0] < range2[1] && range1[1] > range2[0]) {
            overlaps.push(range2.join('-'));
        }
    }
    
    // 如果有重叠，显示所有重叠的时间范围
    if (overlaps.length > 0) {
        ElMessageBox.alert(`与以下时间范围存在重叠：${overlaps.join('、')}`, '提示', {
            confirmButtonText: '知道了',
            type: 'error'
        });
        return;
    }
    
    // 所有校验通过，保存编辑
    row.IsEdit = false;
    delete row.originalData;
    delete row.IsNew; // 保存后不再是新增行，移除IsNew标记
    
    // 按开始时间对时段进行排序
    list.value.sort((a, b) => {
        // 确保TimeRange有值且是数组
        if (!a.TimeRange || !Array.isArray(a.TimeRange) || a.TimeRange.length < 2) return 1;
        if (!b.TimeRange || !Array.isArray(b.TimeRange) || b.TimeRange.length < 2) return -1;
        
        // 比较开始时间
        return a.TimeRange[0] > b.TimeRange[0] ? 1 : -1;
    });
}

function cancelEdit(row: TimeRangeSetting) {
    // 检查是否为新增行
    if (row.IsNew) {
        // 新增行点击取消，直接移除该行
        const index = list.value.indexOf(row);
        if (index !== -1) {
            list.value.splice(index, 1);
        }
    } else {
        // 编辑行点击取消，恢复原始数据
        Object.assign(row, row.originalData);
        row.IsEdit = false;
        delete row.originalData;
    }
}

// 删除范围
function deleteRange(index: number) {
    list.value.splice(index, 1);
}

function addRange(){
    list.value.push({
        TimeRange: [],
        IsEdit:true,
        IsNew: true // 添加IsNew标记，用于区分新增行
    })
}

// 校验所有 TimeRange 是否都选择了起始值
function validateTimeRange() {
    for (let i = 0; i < list.value.length; i++) {
        if (!validateSingleTimeRange(list.value[i], i)) {
            return false;
        }
    }
    return true;
}

// 校验时段是否重叠
function validateTimeOverlap() {
    const overlaps = [];
    
    for (let i = 0; i < list.value.length; i++) {
        for (let j = i + 1; j < list.value.length; j++) {
            const range1 = list.value[i].TimeRange;
            const range2 = list.value[j].TimeRange;
            
            // 确保两个范围都有有效的TimeRange数据
            if (!range1 || range1.length < 2 || !range1[0] || !range1[1] || 
                !range2 || range2.length < 2 || !range2[0] || !range2[1]) {
                continue;
            }
            
            // 检查两个时段是否重叠：range1.start < range2.end && range1.end > range2.start
            if (range1[0] < range2[1] && range1[1] > range2[0]) {
                overlaps.push(`${range1.join('-')} 与 ${range2.join('-')}`);
            }
        }
    }
    
    // 如果有重叠，显示所有重叠的时间范围
    if (overlaps.length > 0) {
        ElMessageBox.alert(`以下时间范围存在重叠：${overlaps.join('、')}`, '提示', {
            confirmButtonText: '确定',
            type: 'error'
        });
        return false;
    }
    
    return true;
}

async function submit() { 
    // 检查是否有正在编辑中的时段
    if (isEditing.value) {
        ElMessage.warning('还有时段正在编辑中，请先保存或取消编辑');
        return;
    }
    
    // 检查是否至少设置了一个时间段
    if (list.value.length === 0) {
        ElMessage.warning('请至少设置一个时间段');
        return;
    }
    
    // 校验 TimeRange 起始值
    if (!validateTimeRange()) {
        return;
    }
    
    // 校验时段重叠
    if (!validateTimeOverlap()) {
        return;
    }
    
    // 获取当前用户设置的基础信息（使用与fetchUserSettings相同的参数）
    const pageKey = 'TeacherTimeRange';
    const pageName = '老师课表时段范围';
    const type = 1;
    const isPublic = 1;
    
    // 根据API返回的数据结构构建updatedSettings
    // 如果list.value为空，则直接使用空数组
    let updatedSettings;
    if (list.value.length > 0) {
        // 如果有数据，使用现有的ID（如果存在）或空字符串
        updatedSettings = [{
            ID: list.value[0].ID || '',
            PageKey: pageKey,
            PageName: pageName,
            Type: type,
            IsPublic: isPublic,
            UserSettingsDetailList: list.value.map(item => ({
                Field1: item.TimeRange[0],
                Field2: item.TimeRange[1]
            }))
        }];
    } else {
        // 如果没有数据，创建一个新的设置项
        updatedSettings = [{
            PageKey: pageKey,
            PageName: pageName,
            Type: type,
            IsPublic: isPublic,
            UserSettingsDetailList: []
        }];
    }
    
    try {
        loading.value = true;
        const apiSaveData={
            PageKey: updatedSettings[0].PageKey,
            PageName: updatedSettings[0].PageName,
            Type: updatedSettings[0].Type,
            ItemList:updatedSettings[0].UserSettingsDetailList
        }
        // 调用API保存用户设置
        await saveUserSettingsList(apiSaveData);
        
        // 更新store中的用户设置数据
        userSettingsStore.updateUserSettings(pageKey, updatedSettings);
        
        ElMessage.success('保存成功');
        
        // 返回数据给调用者
        if (_resolve) {
            _resolve(list.value);
            _resolve = null;
            _reject = null;
        }
        
        // 触发事件，通知父组件更新视图
        emit('settings-updated', list.value);
        
        dialogVisible.value = false;
    } catch (error) {
        ElMessage.error('保存失败，请重试');
        console.error('保存用户设置失败:', error);
    } finally {
        loading.value = false;
    }
}

// 定义Promise回调变量
let _resolve: any = null;
let _reject: any = null;

// 关闭对话框
function close() {
    dialogVisible.value = false;
    if (_reject) {
        _reject();
        // 重置回调
        _resolve = null;
        _reject = null;
    }
}

// 用户设置store
const userSettingsStore = useUserSettings()

// 打开对话框
function open(params: any) {
    dialogVisible.value = true;
    loading.value = true;
    
    // 请求用户设置接口
    userSettingsStore.fetchUserSettings('TeacherTimeRange', '老师课表时段范围', 1, 1).then(settings => {
        if (settings && settings.length > 0) {
            // 正确处理数据结构：从UserSettingsDetailList中提取时间范围
            const processedList: TimeRangeSetting[] = [];
            settings.forEach((settingItem: any) => {
                if (settingItem.UserSettingsDetailList && settingItem.UserSettingsDetailList.length > 0) {
                    settingItem.UserSettingsDetailList.forEach((detail: any) => {
                        if (detail.Field1 && detail.Field2) {
                            processedList.push({
                                ID: settingItem.ID,
                                PageKey: settingItem.PageKey,
                                PageName: settingItem.PageName,
                                Type: settingItem.Type,
                                IsPublic: settingItem.IsPublic,
                                TimeRange: [detail.Field1, detail.Field2],
                                IsEdit: false
                            });
                        }
                    });
                }
            });
            list.value = processedList;
        } else {
            list.value = [];
        }
    }).catch(error => {
        console.error('获取时间范围设置失败:', error);
        list.value = [];
    }).finally(() => {
        loading.value = false;
    });
    
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
    });
}

// 暴露公共方法
defineExpose({
    open
});
</script>
<style lang="scss" scoped></style>