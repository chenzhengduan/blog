import FetchService from "@common/tool/http/fetch-binary";
import { errorHandler, handler } from "./handler-binary";
import { useLoginInfo } from "../store";

let fetchBinary: FetchService = new FetchService(errorHandler);
fetchBinary.addRequestInterceptor(function (url, options) {
    Object.assign(options.headers!, {
        "WTwo-CompanyID":useLoginInfo().loginInfo.WTwo_CompanyID,
        "WTwo-AuthToken":useLoginInfo().loginInfo.WTwo_AuthToken
    })
})
handler(fetchBinary);

export default fetchBinary;
