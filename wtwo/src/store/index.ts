import { defineStore } from "pinia";

//新接口的访问路径 - 根据当前域名自动判断
const getCurrentApiUrl = () => {
  const hostname = window.location.hostname;
  
  // 测试环境：beta01、test、本地环境使用测试地址
  if (
    hostname === 'beta01.xiaogj.com' || 
    hostname === 'test.xiaogj.com' || 
    hostname === 'stage.xiaogj.com' ||
    hostname === 'localhost' || 
    hostname === '127.0.0.1' ||
    hostname.startsWith('192.168.') ||
    hostname.startsWith('10.') ||
    hostname.startsWith('172.16.')
  ) {
    return 'https://wtwotest.xiaogj.com';
  }
  
  // 生产环境：其他所有域名使用正式地址
  return 'https://next.xiaogj.com';
};

export const apiUrl = getCurrentApiUrl();
// export const testUrl = 'https://test.xiaogj.com';
export const testUrl = '';

//用户登录信息
export const useLoginInfo = defineStore("loginInfo", {
  state: () => {
    return {
      loginInfo: {
        WTwo_CompanyID:'',
        WTwo_AuthToken:''
      }
    }
  }
})

// 用户基本信息
export const useUser = defineStore("userInfo", {
  state: () => {
    return {
      user: {
        CompanyID: '',
        CompanyName: '',
        Date: '',
        FileServerURL: '',
        FullName: '',
        NickName:'',
        ID: '',
        IsAdmin: '',
        IsBind: 0,
        IsUpdatePwd: 0,
        Photo: '',
        SMSTel: '',
        Sex: '',
        SsxStatus: false,
        Time: '',
        UserName: '',
        UserNameSuffix: '',
        CustomerValue: 0,
        TrainingTermNo: '',
        SID: '' // 添加SID字段
      }

    };
  },
});


export const useConfigs = defineStore("configs", {
  state: () => {
    return {
      configs: {} as any
    }
  },
});

export const useUserCampuses = defineStore("userCampuses", {
  state: () => {
    return {
      userCampuses: [] as any
    }
  },
  actions:{
    setCampuses(val:number){
       this.userCampuses =val   
    }      
  },
});

export const useCurrentCampuses = defineStore("currentCampuses", {
  state: () => {
    return {
        campusList: '' as string,
        multi:true
    }
  },
  actions:{
    setCampuses(val:any){
       this.campusList =val   
    }      
  },
});

export const useYears = defineStore("years", {
  state: () => {
    return {
      years: [] as any
    }
  },
});

// 导出用户设置store
export { useUserSettings } from './userSettings';


