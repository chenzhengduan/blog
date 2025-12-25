// 获取环境
export function getEnv(){
    let host = window.location.host
    if(!host.includes('xiaogj.com')){
        return 'dev'
    }else if(['test.xiaogj.com','beta01.xiaogj.com','stage.xiaogj.com'].includes(host)){
        return 'test'
    }else{
        return 'prod'
    }
}