<template>
    <div class="page-attention-tips">
        <el-icon size="14px"><blubSVG /></el-icon>

        <!-- 提示文字 -->
        <span class="base-tips">
            <slot />
        </span>

        <!-- 提示文案样式以外的 -->
        <slot name="other"></slot>
    </div>
</template>

<script lang="ts" setup>

import blubSVG from '@/assets/svg/bulb.svg';
</script>

<style lang="scss" scoped>
    .page-attention-tips {
        display: flex;
        align-items: flex-start;
        line-height: 20px;
        .wtwo-icon {
            margin-right: 8px;
            line-height: 20px;
            height: 20px;
        }
        .base-tips {
            color: #909399;
            font-size: 12px;
            
        }
        :deep(.wtwo-link) {
            .wtwo-link__inner {
                font-size: 12px;
            }
        }

    }
</style>