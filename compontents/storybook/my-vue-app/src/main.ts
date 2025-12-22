import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入 vue-fantable
import "vue-fantable/libs/theme-default.css"
import VueFantable from 'vue-fantable'

const app = createApp(App)
app.use(ElementPlus)
app.use(VueFantable)
app.use(router)
app.mount('#app')
