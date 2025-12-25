<template>
    <div class="h-100%">
        <component :is="comp[currentComponent]" @component-ready="handleComponentReady" />
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import classTableCourse from './class-table/class-table-course.vue'

// 定义 emit，向上传递事件
const emit = defineEmits<{
  'component-ready': []
}>()

interface IKeyValueMap {
	[key: string]: any
}
const comp: IKeyValueMap = {
    classTableCourse
}
const currentComponent = ref('classTableCourse')

// 子组件加载完成，向父组件传递
function handleComponentReady() {
  emit('component-ready')
}

</script>
