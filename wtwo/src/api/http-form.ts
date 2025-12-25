import FetchService from "@common/tool/http/fetch";
import { errorHandler, handler } from "./handler";
import { useLoginInfo } from "../store";

let fetchForm: FetchService = new FetchService(errorHandler);

fetchForm.addRequestInterceptor(function (url, options) {
  Object.assign(options.headers!, {
    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
    "WTwo-CompanyID":useLoginInfo().loginInfo.WTwo_CompanyID,
    "WTwo-AuthToken":useLoginInfo().loginInfo.WTwo_AuthToken
  })
  if (options.body) {
    const body = JSON.parse(options.body as string);

    const params = new URLSearchParams();

    for (let key in body) {
      if (Object.prototype.hasOwnProperty.call(body, key)) {
        params.append(key, body[key]);
      }
    }
    options.body = params;
  }
})

handler(fetchForm);

export default fetchForm;
