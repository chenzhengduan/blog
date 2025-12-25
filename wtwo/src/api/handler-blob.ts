import FetchService from "@common/tool/http/fetch-blob";
import { dialogOnce } from '@/assets/lib/network';
import { ElMessage,ElMessageBox  } from "element-plus";

export function handler(fetch: FetchService) {
  fetch.addResponseInterceptor((res) => {
    if (res.ErrorCode === 407) {
        // dialogOnce(res.ErrorCode,()=>{
        //     useLoginInfo().$state.timeout=1
        //     // ElMessageBox.alert(res.ErrorMsg,'提示',{
        //     //     showClose:false,
        //     //     dangerouslyUseHTMLString:true
        //     // }).then(()=>{
        //     //     window.location.href = "/login.aspx";
        //     // })
        // })
      return Promise.reject(407);
    }
    
    if (res.ErrorCode === 200) {
      return res;
    } else {
      setTimeout(() => {
        ElMessageBox.alert(res.ErrorMsg,'提示',{
            showClose:false,
            dangerouslyUseHTMLString:true
        });
      }, 0);
      return Promise.reject(res);
    }
  });
}

export function errorHandler(res: Response) {
  ElMessage.error(res.statusText);
}
