export interface IPage {
  PageIndex: number;
  PageSize: number;
  TotalCount: number;
  PageCount: number;
}
export type IPageRequest = Pick<IPage, "PageIndex" | "PageSize">;

// 获取某个接口的响应的泛型参数
export type GetResponseType<T extends (...args: any) => any> =
  ReturnType<T> extends Promise<IResponse<infer U>> ? U : never;

export interface IResponse<T = any> {
  ErrorCode: number;
  Data: T;
  ErrorMsg: string;
  PageIndex?:number;
  PageSize?:number;
  TotalCount?:number;
  PageCount?:number;
}

export interface IRequestParams {
  url: string;
  data?: any;
}

interface ICancelMap {
  [key: symbol]: AbortController["abort"][];
}

type IResponseInterceptors = (
  params: IResponse
) => IResponse | Promise<IResponse>;

const cancelMap: ICancelMap = {};

const cancelQueue: symbol[] = [];

/**
 * 为后续同步执行代码中的fetch请求 生成取消请求cancel(), 执行该方法会取消这些fetch请求
 */
export function useSyncCancel() {
  if (cancelQueue.length) {
    console.error("同一个同步过程只能生成一个请求cancel");
    return function () {};
  }
  const sign = Symbol();
  cancelQueue.push(sign);

  // 下一个tick移除.
  Promise.resolve().then(() => {
    cancelQueue.shift();
  });
  return function cancel() {
    let list = cancelMap[sign];
    delete cancelMap[sign];
    if (list) {
      list.forEach((fn) => {
        console.log(
          "%c取消请求",
          "color: red; font-weight: bold; background: #ccc;"
        );
        fn();
      });
    }
  };
}

export default class FetchService {
  constructor(errorHandler: (params: Response) => void) {
    this.errorHandler = errorHandler;
  }
  private errorHandler: (params: Response) => void;

  private requestInterceptors: Array<
    (url: string, options: RequestInit) => void
  > = [];
  private responseInterceptors: Array<IResponseInterceptors> = [];

  // get接口没有请求体,键值对转化为参数
  async get<T = any>(params: IRequestParams): Promise<IResponse<T>> {
    let url =
      params.url.slice(-1) === "/" ? params.url.slice(0, -1) : params.url;
    if (params.data) {
      let data = params.data;
      url += Object.keys(data).reduce((prev: string, key: string) => {
        const value: string = data[key];
        return prev + `${key}=${value}&`;
      }, "?");
      url = url.slice(0, -1);
    }
    return this._request("GET", { url });
  }

  async post<T = any>(params: IRequestParams): Promise<IResponse<T>> {
    return this._request("POST", params);
  }

  async put<T = any>(params: IRequestParams): Promise<IResponse<T>> {
    return this._request("PUT", params);
  }

  async delete<T = any>(params: IRequestParams): Promise<IResponse<T>> {
    return this._request("DELETE", params);
  }

  addRequestInterceptor(
    interceptor: (url: string, options: RequestInit) => void
  ) {
    this.requestInterceptors.push(interceptor);
  }

  addResponseInterceptor(interceptor: IResponseInterceptors) {
    this.responseInterceptors.push(interceptor);
  }

  private async _request<T extends IResponse>(
    method: string,
    params: IRequestParams
  ): Promise<T> {
    const { url, data } = params;
    const controller = new AbortController();

    const syncFlag = cancelQueue[0];

    if (syncFlag) {
      if (cancelMap[syncFlag]) {
        cancelMap[syncFlag].push(controller.abort.bind(controller));
      } else {
        cancelMap[syncFlag] = [controller.abort.bind(controller)];
      }
    }

    let options: RequestInit = {
      method: method,
      headers: {},
      signal: controller.signal,
    };
    if (data) {
      options.body = data;
    }
    this.runRequestInterceptors(url, options);
    const response = await fetch(url, options);
    if (!response.ok) {
      this.errorHandler(response);
      throw new Error(response.statusText);
    }
    const res: T = await response.json();
    let result = await this.runResponseInterceptors(res);
    return result as T;
  }

  private runRequestInterceptors(url: string, options: RequestInit) {
    this.requestInterceptors.forEach((interceptor) =>
      interceptor(url, options)
    );
  }

  private async runResponseInterceptors(response: IResponse) {
    let result: IResponse = response;
    for (let i = 0; i < this.responseInterceptors.length; i++) {
      result = await this.responseInterceptors[i](result);
    }
    return result;
  }
}
