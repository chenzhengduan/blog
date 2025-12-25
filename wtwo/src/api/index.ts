import { testUrl } from "../store";
import fetch from "./http";
import fetchForm from "./http-form";
export * from './comm';
export * from './dept';
export * from './exam';

//登录
export function login(params:any) {
    return fetch.get({
        url: testUrl+'/api/login/login',
        data: params,
    })
}

// 退出登录
export function logout(data: any) {
  return fetch.post({
    url: "/api/login/logout",
    data,
  });
}

//获取用户信息
export function whoami(){
    return fetch.post({
        url: testUrl+'/api/user/whoami'
    });
}

export function getStsInfo2(params:any){
    return fetchForm.post({
        url: "/api/File/GetStsInfoRequest",
        data: params,
      });
}

