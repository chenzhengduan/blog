// oss返回的文件对象
export interface IOSSFile {
  url: string;
  name?: string;
  width?: string;
  height?: string;
  ext: string;
  fileId?: string;
  fileKey?: string;
  fileSize?: string;
  type: "image" | "file" | "video" | 'audio';
  mimeType?: string;
}

export interface ISTSParams {
  fileType: 1 | 2 | 3,//1图片 2文档 3音视频 
  businessType?: string,
  terminal?: 0 | 1 //0老师端 1学员端
}
