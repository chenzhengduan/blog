<template>
    <el-drawer 
        v-model="drawer" 
        direction="rtl" 
        size="700px" 
        :close-on-click-modal="false" 
        @close="close" 
        append-to="body" 
        class="packageTagHelperDrawerRef" 
        :fullscreen="isDrawerFullscreen">
        <template #header="{ close, titleId, titleClass }">
            <h4 :id="titleId" :class="titleClass">答疑</h4>
            <el-button @click="isDrawerFullscreen = !isDrawerFullscreen" type="primary" plain size="small">
                {{ isDrawerFullscreen ? '退出全屏' : '全屏' }}
            </el-button>
        </template>
        <el-scrollbar class="helper-article">
            <div class="helper-article-title">问：为什么显示“套餐中”标签？</div>
            <div class="helper-article-title-desc">答：表示商品在某个“优惠套餐”中，会显示套餐中的标签。</div>
            <div class="helper-article-title">问：“不可单独售卖”是什么意思？</div>
            <div class="helper-article-title-desc">答：商品所在的优惠套餐，设置了套餐中的商品不允许单独售卖，则商品不会单独显示在“师生信商城”只显示在套餐内。</div>
            <div class="helper-article-img">
                <img src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-mall/img/package-tag-tips1.png" width="556px"/>
            </div>
            <div class="helper-article-title">问：“可单独售卖”是什么意思？</div>
            <div class="helper-article-title-desc">答：商品所在的优惠套餐，设置了套餐中的商品允许单独售卖，则商品会单独显示在“师生信商城”即可单独售卖又可通过优惠套餐售卖。</div>
            <div class="helper-article-img">
                <img src="https://cdn01.xiaogj.com/uploads/fe/w1/pc-mall/img/package-tag-tips2.png" width="556px"/>
            </div>
        </el-scrollbar>
    </el-drawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';

const drawer = ref(false)
const isDrawerFullscreen = ref(false)

let _resolve:any = null,
    _reject:any = null;

const type = ref('')
function open(params:any) {
    return new Promise((resolve, reject) => {
        _resolve = resolve;
        _reject = reject;
        drawer.value = true;
    })
}
defineExpose({
    open
})
function close(){
    drawer.value = false;
    _reject&&_reject()
}
</script>
<style lang="scss">
.packageTagHelperDrawerRef{
    .el-drawer__header {
        margin-right: 0px; // 移除默认的margin
        padding-right: 45px; // 为关闭按钮留出空间
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .helper-article{
        padding: 16px;
        height: 100%;
        overflow: auto;
        .helper-article-title{
            font-size: 14px;
            line-height: 20px;
            font-weight: 600;
            margin-bottom: 6px;
            color: #303133;
        }
        .helper-article-title-desc{
            color: #303133;
            line-height: 20px;
            margin-bottom: 16px;
        }
        
        .compare-title{
            line-height: 20px;
        }
        .helper-article-img{
            max-width: 100%;
            margin-bottom: 16px;
        }
        .border-box{
            border: 1px solid #ccc;
            margin-bottom: 16px;
            .compare-title{
                border-bottom: 1px solid #ccc;
                padding: 5px 10px;
            }
            .helper-article-img{
                margin: 0;
                padding: 5px 10px;
            }
        }
    }

}

</style>
