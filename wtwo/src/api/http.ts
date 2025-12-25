import FetchService from "@common/tool/http/fetch";

import { errorHandler, handler } from "./handler";
import { useLoginInfo } from "../store";

let fetch: FetchService = new FetchService(errorHandler);
fetch.addRequestInterceptor(function (url, options) {
    Object.assign(options.headers!, {
        "WTwo-CompanyID":useLoginInfo().loginInfo.WTwo_CompanyID,
        "WTwo-AuthToken":useLoginInfo().loginInfo.WTwo_AuthToken
    })
})
handler(fetch);

export default fetch;


