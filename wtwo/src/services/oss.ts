import { getStsInfo2 } from "@/api/index";
import { IOSSFile,ISTSParams } from "@/types/base";

export function setConfig() {
  window.xgjzt.oss.setConfig({
    max: 9,
    env: import.meta.env.DEV || location.origin.includes("test")||location.origin.includes("beta")||location.origin.includes("stage") ? "test" : "prod",
    platform: "h5",
  });
  return;
}

export function uploadFile(file: File, params: any) {
  return new Promise<IOSSFile>((rsv, rej) => {
    window.xgjzt.oss.uploadFile({
      options: {
        file: file,
        getStsParams: getOssRequest(file, params),
        success(res: IOSSFile) {
          rsv(res);
        },
        fail(res: any) {
          rej(res);
        },
      },
    });
  });
}

export async function uploadFileMult(files: File[]) {
  const list = files.map((file) => {
    return {
      options: {
        file,
      },
    };
  });

  const res: IOSSFile[] = await window.xgjzt.oss.uploadFiles(list);
  return res;
}

function getOssRequest(file: File,params:ISTSParams) {
  return async function () {
    const ret = await getStsInfo2({
      fileName: file.name,
    //   fileSuffix: encodeURIComponent(file.name).split(".").pop()
        businessType:params.businessType||'w1_other',
        fileType:params.fileType,
        terminal:params.terminal||0
    });

    if (ret.ErrorCode !== 200) {
      return Promise.reject(ret.Data);
    }
    const t = ret.Data;
    const stsParams = {
      ...t,
      businessDto: t.businessVo,
    };
    return stsParams;
  };
}
