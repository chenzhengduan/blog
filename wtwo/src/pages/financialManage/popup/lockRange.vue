<template>
    <el-drawer 
        v-model="isShow" 
        title="锁定范围设置"
        class="lockRange"
        :size="800"
        :close-on-click-modal="false"
        :append-to-body="true"
        :destroy-on-close="true"
        @close="close"
    >
        <div class="drawer-body-wrap scroll-box" v-loading="loading">
            <div class="intro-area">
                <div class="intro-main-desc">
                    <el-icon size="16px" color="#ff7d00"><WarningFilled /></el-icon>
                    <span>财务期间锁定的日期范围内，以下涉及费用变动的相关操作禁止执行：</span>
                </div>
                <div class="intro-sub-desc">
                    <span v-for="item in DisableActions" class="sub-item">
                        <el-icon size="14px" color="#909399">
                            <svg aria-hidden="true">
                                <use xlink:href="#w2-jinyong"></use>
                            </svg>
                        </el-icon>
                        <span>{{ item }}</span>
                    </span>
                </div>
            </div>
            <div class="group-tips">财务期间锁定的日期范围内，是否允许操作以下业务：</div>
            <div class="group-area">
                <div v-for="group in ConfigGroups" class="config-group">
                    <div class="group-name">{{ group.groupName }}<span>({{ group.groupItems.length }})</span></div>
                    <div class="group-content">
                        <div class="group-item" v-for="item in group.groupItems">
                            <div class="item-name">{{ item.itemName }}</div>
                            <div class="item-desc">{{ item.itemDesc }}</div>
                            <div class="item-options">
                                <el-radio-group v-model="item.itemValue">
                                    <el-radio v-for="option in item.itemOptions" :key="option.value" :value="option.value">{{ option.label }}</el-radio>
                                </el-radio-group>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <div class="wtwo-drawer-footer flex-end">
                <el-button @click="close">取消</el-button>
                <el-button @click="submit" type="primary" :loading="loading">保存</el-button>
            </div>
        </template>

    </el-drawer>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { WarningFilled } from '@element-plus/icons-vue';
import { getSysConfig, PutOtherParameter } from '@/api/comm';

const DisableActions = ['收费', '退费', '转费', '结转', '点名上课', '撤销点名', '作废收据', '冲销收据', '修改收据'];
const ConfigGroups = ref([
    {
        groupName: '学员管理类',
        groupItems: [
            {
                itemName: '修改学员状态',
                itemDesc: '操作学员休学/复学/退学/恢复入学时，是否允许将日期选择到财务期间已锁定的日期。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'StudentStatusEdit', // 保存到后端的key
            },{
                itemName: '到访登记',
                itemDesc: '对意向学员操作到访登记时，是否允许将承诺到访和实际到访日期选择到财务期间已锁定的日期。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'VisitRegister', // 保存到后端的key
            },
        ]
    },{
        groupName: '班级管理类',
        groupItems: [
            {
                itemName: '进出班操作',
                itemDesc: '对学员操作入班和出班时，是否允许将进出班日期选择到财务期间已锁定的日期。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'ClassInOut', // 保存到后端的key
            },
        ]
    },{
        groupName: '仓库管理类',
        groupItems: [
            {
                itemName: '预出库管理（发货/取消发货）',
                itemDesc: '物品的购买时间已财务锁定，是否允许发货/取消发货',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'PreStockOut', // 保存到后端的key
            },{
                itemName: '进出库管理（进货/退货/调拨/报损/领用/退领/库存调整）',
                itemDesc: '操作进货/退货/调拨/报损/领用/退领/库存调整时，是否允许将日期选择到财务期间已锁定的日期。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'StockInOut', // 保存到后端的key
            },
        ]
    },{
        groupName: '收据相关类',
        groupItems: [
            {
                itemName: '收据附件上传/删除',
                itemDesc: '是否允许对已锁定日期范围内的收据附件操作继续上传/删除。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'ReceiptAttachmentUpload', // 保存到后端的key
            },{
                itemName: '收据备注修改',
                itemDesc: '包含已锁定日期范围收据的内部备注和外部备注修改。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'ReceiptRemarkUpdate', // 保存到后端的key
            },{
                itemName: '修改课程有效期',
                itemDesc: '是否允许修改收费日期在锁定日期内收据的课程有效期；不能影响已锁定日期的数据，且有效期不能改到已锁定日期范围。',
                itemOptions: [
                    {
                        label: '允许',
                        value: 1
                    },
                    {
                        label: '不允许',
                        value: 0
                    }
                ],
                itemValue: 1,
                itemKey: 'CourseValidDateEdit', // 保存到后端的key
            },
        ]
    },
])

const isShow = ref(false);
const loading = ref(false);
let configId = '',
    _resolve = null as any,
    _reject = null;

function submit() {
    const configValue: Record<string, number> = {};
    ConfigGroups.value.forEach(group => {
        group.groupItems.forEach(item => {
            configValue[item.itemKey] = item.itemValue;
        });
    });
    console.log(configValue)
    loading.value = true;
    PutOtherParameter({
        id: configId,
        otherParameter: JSON.stringify(configValue)
    }).then((res: any) => {
        ElMessage.success('保存成功');
        _resolve();
        close();
    }).finally(() => {
        loading.value = false;
    })
}
function getDetail() {
    loading.value = true;
    getSysConfig({
        name: 'FinanceLockSetting'
    }).then((res: any) => {
        const data = res.Data;
        if (data.length) {
            configId = data[0].ID;
            const otherParameter = data[0].OtherParameter;
            const configValue = JSON.parse(otherParameter);
            ConfigGroups.value.forEach(group => {
                group.groupItems.forEach(item => {
                    item.itemValue = configValue[item.itemKey];
                });
            });
        }
    }).finally(() => {
        loading.value = false;
    })
}
function close() {
    isShow.value = false;

}
function open() {
    return new Promise((resolve, reject) => {
        isShow.value = true;
        getDetail();
        _resolve = resolve;
        _reject = reject;
    })
}
defineExpose({
    open
})
</script>

<style lang="scss" scoped>
.lockRange {
    .drawer-body-wrap {
        padding: 16px 16px 0;
        overflow: auto;
        .intro-area {
            padding: 12px 16px;
            background-color: #FFF7E8;
            border-radius: 4px;
            .intro-main-desc {
                display: flex;
                align-items: center;
                color: #303133;
                .wtwo-icon {
                    margin-right: 8px;
                }
            }
            .intro-sub-desc {
                display: flex;
                align-items: center;
                flex-wrap: wrap;
                padding-left: 22px;
                .sub-item {
                    display: flex;
                    align-items: center;
                    margin-right: 16px;
                    margin-top: 8px;
                    color: #909399;
                    .wtwo-icon {
                        margin-right: 6px;
                    }
                    &:last-child {
                        margin-right: 0;
                    }
                }
            }
        }
        .group-tips {
            margin-top: 16px;   
            color: #303133;
        }
        .group-area {
            margin-top: 16px;
            .config-group {
                margin-bottom: 16px;
                .group-name {
                    position: relative;
                    padding-left: 10px;
                    margin-bottom: 12px;
                    &::before {
                        content: '';
                        position: absolute;
                        left: 0;
                        top: 0;
                        width: 3px;
                        height: 14px;
                        border-radius: 2px;
                        background: #2878e8;
                    }
                    &>span {
                        margin-left: 2px;
                        font-size: 12px;
                        color: #909399;
                    }
                }
                .group-content {
                    .group-item {
                        margin-bottom: 12px;
                        padding: 10px 16px;
                        background-color: #F9FAFC;
                        border-radius: 4px;
                        .item-name {
                            color: #606266;
                        }
                        .item-desc {
                            margin-top: 6px;
                            color: #909399;
                            font-size: 12px;
                        }
                        .item-options {
                            margin-top: 8px;
                        }
                    }
                } 
            }
        }
    }
}
</style>