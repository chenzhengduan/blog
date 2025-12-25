<template>
	<div id="wtwo-app" class="scroll-box">
		<!-- <template v-if="user.ID"> -->
		<el-config-provider
			:locale="zhCn"
			namespace="wtwo"
		>
			<!-- <component v-if="pageName" :is="pageComponent[pageName]"></component>
				<router-view v-else></router-view> -->
			<router-view></router-view>
		</el-config-provider>
		<!-- </template> -->
	</div>
</template>

<script setup lang="ts">
import { onMounted, computed, watch, ref } from 'vue'
// import test from '@/pages/test/test.vue'

import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { useCurrentCampuses, useLoginInfo, useUser } from './store'
import useEvent from './config/event'
import 'dayjs/locale/zh-cn'
import { dayjs } from 'element-plus'
dayjs.locale('zh-cn')

// Pinia store
const user = computed(() => useUser().user)
const event = useEvent()

// 预加载el-empty的图片，优化体验
const preloadImage = (url: string) => {
  const img = new Image()
  img.src = url
}

function setCampus(campus:any){
	const currentCampuses=useCurrentCampuses()
	currentCampuses.$state={
		campusList:campus,
		multi:true
	}
}
// popperCoreConfigProps.strategy='fixed'
onMounted(() => {
    // 移除全局骨架屏
    const loadingSkeleton = document.getElementById('app-loading-skeleton')
    if (loadingSkeleton) {
        // 添加淡出动画
        loadingSkeleton.style.transition = 'opacity 0.3s ease-out'
        loadingSkeleton.style.opacity = '0'
        setTimeout(() => {
            loadingSkeleton.remove()
        }, 500)
    }

    // 预加载图片
    preloadImage('https://cdn01.xiaogj.com/uploads/wtwo-pc/001.png');

	if (window.microApp) {
		const data = window.microApp.getData()
		useLoginInfo().$state.loginInfo = {
			WTwo_CompanyID: data.loginInfo.CompanyID,
			WTwo_AuthToken: data.loginInfo.WTwo_AuthToken,
		}
		if(data.campus){
			setCampus(data.campus)
		}
        window.microApp.addGlobalDataListener((data:any)=> {
			setCampus(data.campus)
        })
		// window.microApp.addDataListener((data:any)=> {
		//     console.log(data)
		// })
	}else{
        let token:any = document.cookie.replace(/(?:(?:^|.*;\s*)WTwo-AuthToken\s*\=\s*([^;]*).*$)|^.*$/, "$1");
        useLoginInfo().$state.loginInfo = {
			WTwo_CompanyID: user.value.CompanyID,
			WTwo_AuthToken: decodeURIComponent(token)
		}
    }
})
</script>

<style lang="scss">
#wtwo-app {
	position: absolute;
	top: 0;
	bottom: 0;
	left: 0;
	right: 0;
	background:#F0F2F5;
    padding: 0 8px;
	// min-width: 1280px;
}
</style>
