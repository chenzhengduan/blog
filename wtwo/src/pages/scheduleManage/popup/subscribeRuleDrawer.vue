<template>
  <el-drawer
    v-model="visible"
    title="学员约课规则"
    direction="rtl"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    :size="650"
    @close="handleClose"
  >
    <SubscribeRuleSet ref="ruleSetRef" @save="handleSave" @close="handleClose" />
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SubscribeRuleSet from './child/subscribeRuleSet.vue'

const visible = ref(false)
const ruleSetRef = ref()

let _resolve: any = null
let _reject: any = null

/** 对外暴露一个open方法 */
function open() {
  return new Promise((resolve, reject) => {
    _resolve = resolve
    _reject = reject
    visible.value = true
  })
}

function handleSave() {
  visible.value = false
  _resolve && _resolve()
}

function handleClose() {
  visible.value = false
  _reject && _reject()
}

defineExpose({
  open
})
</script>

<style lang="scss" scoped>
:deep(.el-drawer__body) {
  padding: 0;
}
</style>
