import FetchService from "@common/tool/http/fetch-blob";

import { errorHandler, handler } from "./handler-blob";
import { useLoginInfo } from "../store";

let fetchBlob: FetchService = new FetchService(errorHandler);
fetchBlob.addRequestInterceptor(function (url, options) {
    Object.assign(options.headers!, {
        "WTwo-CompanyID":useLoginInfo().loginInfo.WTwo_CompanyID,
        "WTwo-AuthToken":useLoginInfo().loginInfo.WTwo_AuthToken
    })
})
handler(fetchBlob);

export default fetchBlob;


