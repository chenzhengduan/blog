import "virtual:uno.css";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
import { login, whoami } from "@/api";
import { useUser, useConfigs, useUserCampuses, useYears, useCurrentCampuses } from "@/store/index";
import '@/assets/iconfont/iconfont.js';
import '@/assets/iconfont/iconfont.css'
import '@/assets/scss/emoji.css';
// 导入全局样式（只导入一次）
import '@/assets/scss/wtwo-element-theme.scss';
import { setConfig } from "@/services/oss";
import './global';
import "vue-fantable/libs/theme-default.css"
// import * as Sentry from '@sentry/vue';
import { getEnv } from "@/utils/domain/env";
import Logger from "@/utils/sentry/sentry";
// 引入组件库
import { FanTable, VePagination, VeIcon, VeLoading, VeLocale } from "vue-fantable";

setConfig()

let app: any = null
let root:any=null
async function render(container: Element | string,isMountedOnce:boolean=false) {
    // 初始化
    app = createApp(App);
    let env = getEnv()
    // Sentry配置
    if (env !== 'dev') {
        // Sentry.init({
        //     app,
        //     dsn: "https://ae70e2be4396bb745064d48d62fecb8e@sentry2.xiaogj.com/12",
        //     tracesSampleRate: env === 'prod' ? 0.3 : 0.8,// 性能采样率，生产环境30%够了，其它环境80%
        //     sampleRate: 1.0,// 错误日志采样率 100%
        //     environment: env as string,// 用于查看日志时分环境显示
        //     maxValueLength: 10000,//发送到Sentry的事件上的每个字符串属性在被截断之前可以拥有的最大字符数。默认250
        //     normalizeDepth: 10,// 对象/数组数据层级深度，默认3
        //     beforeSend: (event, hint) => {
        //         console.log(event, hint)
        //         return event
        //     },
        //     denyUrls: [/chrome-extension:\/\//, /safari-web-extension:\/\//],// 屏蔽第三方脚本、浏览器插件的错误
        //     // allowUrls: [/https?:\/\/((cdn|www)\.)?example\.com/],// 是否指定
        // });
    }
    app.component('ve-table', FanTable)

    // 挂载 VeLoading 和 VeLocale 到全局
    window.$veLoading = VeLoading
    window.$locale = VeLocale
    

    app.use(createPinia());

    window.$xgj = {
        configDescript: {
            "校区": "校区",
            "老师": "老师",
            "班主任": "班主任",
            "咨询师": "咨询师",
            "学管师": "学管师",
            "师生信": "师生信",
            '公立学校': '公立学校',
            "课次": "课次",
            "次": "次",
        },
        fieldAliasObj: {
            '就读年级': '就读年级'
        },
        op: function () { },
        checkMenu: function () { }
    };

    // 定义全局变量
    interface GlobalData {
        emptyPng: string,
        emptyPng2: string,
        noRightPng: string,
        EMPTYGUID: string,
    }

    // 创建一个 Plugin，用于提供全局变量
    const globalPlugin = {
        install(): void {
            const globalData: GlobalData = {
                emptyPng: 'https://cdn01.xiaogj.com/uploads/fe/w1/img/empty.png',
                emptyPng2: 'https://cdn01.xiaogj.com/uploads/wtwo-pc/001.png',
                noRightPng: 'https://cdn01.xiaogj.com/uploads/fe/w1/pc-back-end/img/guanzai-no-right.png',
                EMPTYGUID: '00000000-0000-0000-0000-000000000000'
            };

            app.config.globalProperties.$global = globalData;
        }
    };
    app.use(globalPlugin);

    // 进入系统之前，需要获取及设置的公共信息
    function getPbulicInfo() {
        //获取用户信息
        let p1 = whoami().then(res => {
            if (res.ErrorCode == 200) {
                let data = res.Data
                var userInfo = res.Data || {};
                if (env !== 'dev') {
                    Logger.setUser({
                        username: userInfo.UserName,
                        id: userInfo.ID,
                    })
                    Logger.setTags({
                        CompanyID: data.CompanyID,
                        CompanyName: data.CompanyName,
                        AppVersion: data.AppVersion,
                        env: env,
                        FullName: userInfo.FullName,
                    })
                }
                const user = useUser()
                user.$state.user = userInfo

                // 保存SID到store中
                if (data.SID) {
                    user.$state.user.SID = data.SID
                    console.log('SID已保存到store:', data.SID)
                }
                const configs = useConfigs()
                configs.$state.configs = data.Config || {}
                const userCampuses = useUserCampuses()
                userCampuses.$state.userCampuses = data.CampusList || []

                //调试用
                const currentCampuses=useCurrentCampuses()
                currentCampuses.$state={
                    campusList:data.CampusList&&data.CampusList.length?data.CampusList.map((item:any)=>item.ID).join(','):'',
                    multi:true
                }

                //赋值默认选中校区
                // let localCampus = localStorage.getItem('sysDefaultCampus'),
                //     campusMap = localCampus ? JSON.parse(localCampus) : '',
                //     curCampus:any = []
                // if(data.CampusList.length){
                //     if(campusMap){
                //         let localIds:any=[]
                //         campusMap.forEach((i:any)=>{
                //             if (i.userId == userInfo.ID) {
                //                 localIds = i.campusIds.split(',');
                //             }
                //         })
                //         if(localIds.length){
                //             data.CampusList.forEach((item:any)=>{
                //                 let exist=localIds.find((j:any)=>j==item.ID)
                //                 if(exist){
                //                     curCampus.push(item)
                //                 }
                //             })
                //         }

                //     }
                //     if(curCampus.length==0){
                //         curCampus=[data.CampusList[0]]
                //     }
                // }
                // const currentCampuses=useCurrentCampuses()
                // currentCampuses.$state={
                //     campusList:curCampus,
                //     multi:true
                // }
                var rightsList = data.Rights || [];
                window.$xgj.op = function (pName: string) {
                    return data.IsAdmin || rightsList.indexOf(pName) !== -1;
                }
                // var com = data.CompanyName != "" ? "微前端 - " : "微前端";
                // //设置定制用户制定的网页标题
                // if (data.Config.System_Title != "") {

                //     window.document.title = com + data.Config.System_Title;
                // } else {
                //     //标准网页标题
                //     window.document.title = com + data.CompanyName;
                // }
                if (data.Config.System_FaviconUrl != '') {
                    document.querySelectorAll("link[rel*='icon']").forEach((i: any) => {
                        i.href = data.Config.System_FaviconUrl
                    })
                }
                if (data.PublicField && data.PublicField.length) {
                    (data.PublicField).forEach(function (item: any) {
                        if (window.$xgj.configDescript && item.Name != '年级') {
                            window.$xgj.configDescript[item.Name] = item.Value;
                        }
                        if (window.$xgj.fieldAliasObj) {
                            window.$xgj.fieldAliasObj[item.Name] = item.Value;
                        }
                    });
                }

                let config = data.Config

                window.$xgj.configDescript["校区"] = config["Title_Campus"];
                window.$xgj.configDescript["老师"] = config["Title_Teacher"];
                window.$xgj.configDescript["班主任"] = config["Title_ClassMaster"];
                window.$xgj.configDescript["咨询师"] = config["Title_SalePerson"];
                window.$xgj.configDescript["学管师"] = config["Title_StudentMaster"];
                window.$xgj.configDescript["课次"] = config["Title_Course"];
                window.$xgj.configDescript["次"] = config["Title_CourseUnit_2"];


                var nowyear = data.Date.substr(0, 4) * 1 - 5,
                    yeararr = [],
                    i = 16;
                while (i-- > 0) {
                    yeararr.push({
                        ID: nowyear,
                        Name: nowyear++
                    })
                };
                const years = useYears();
                years.$state.years = yeararr;



            }

        });
        return Promise.all([p1]);
    }

    await getPbulicInfo().catch(()=>{
        
    })
    console.log(container,'container')
    const containerEl=typeof container === 'string' ? container : document.querySelector('#wtwo-app');
    console.log(containerEl,'containerEl')
    console.log(document.querySelector('#wtwo-app'),'document.querySelector(#wtwo-app)')
    if(!document.querySelector('#wtwo-app')){
        return
    }
    if(window.microApp){
        if(isMountedOnce&&!root){
            root=app.use(router).mount(containerEl)
        }
        window.microApp.addDataListener((data:any)=>{
            if(data.type?.startsWith('app:init')){
                if(!root){
                    root=app.use(router).mount(containerEl)
                }
            }
        })
    }else{
        root=app.use(router).mount(containerEl)
    }
}
// 微前端场景
if((window as any).__MICRO_APP_ENVIRONMENT__){
    // micro会调用mount
    console.log('微前端场景')
    ;(window as any).mount=(props:any)=>{
        // 预加载时props为空
        if(!!props){
            console.log("没有开启预加载")
        }
        render(props?.container||'#wtwo-app',!!props)
        console.log("调用了mount")
    }
    // micro会调用unmount
    ;(window as any).unmount=()=>{
        if(app){
            console.log('微前端场景卸载')   
            app.unmount()
            app=null
            root=null
        }
    }
    // micro会调用bootstrap
    ;(window as any).bootstrap=()=>{}
}else{
    console.log('单应用场景')
    render('#wtwo-app')
}