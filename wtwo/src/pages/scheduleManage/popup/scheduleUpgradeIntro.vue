<template>
    <el-dialog
        v-model="isShow"
        width="860"
        modal-class="scheduleUpgradeIntro"
        destroy-on-close
        :close-on-click-modal="false"
        @close="close"
    >
        <img src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/schedule-upgrade-intro1.png" width="860px" @click="close" class="cursor-pointer"/>
    </el-dialog>
</template>

<script lang="ts" setup>
import ls from '@common/tool/ls/ls';
import { ref } from 'vue';

const isShow = ref(false);

function close() {
    isShow.value = false;
    ls.setItem('hide_schedule_intro',1)
}

function open() {
    isShow.value = true;
}
defineExpose({
    open
})
</script>

<style lang="scss">
.scheduleUpgradeIntro {
    .wtwo-dialog {
        padding: 0;
        background:transparent;
        box-shadow: none;
        .wtwo-dialog__header{
            background: transparent;
            display: none;
        }
        .wtwo-dialog__body{
            padding: 0;
        }
    }
    
}
</style>