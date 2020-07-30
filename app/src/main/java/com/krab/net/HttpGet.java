package com.krab.net;

/**
 * @author xkz
 * @date 2019/10/31 21:30
 */
public class HttpGet<T> extends HttpBase<T> {
    final GetApi<T> getApi;

    private HttpGet(GetApi<T> getApi) {
        this.getApi = getApi;
        this.name = getApi.getName();
    }

    public static <T> Builder<T> obtainBuilder(GetApi<T> getApi) {
        return new Builder<>(getApi);
    }


    public void call(NetUtil.Callback<T> callback) {
        this.callback = callback;
        NetUtil.httpGet(this);
    }


    public NetUtil.MResponse<T> execute() {
        return NetUtil.httpGetExec(this);
    }

    public static class Builder<T> {
        private final GetApi<T> getUrl;
        private String urlPostfix = "";
        private Object[] params;
        private String[] headers;
        private String callMark;

        private Builder(GetApi<T> getApi) {
            this.getUrl = getApi;
        }

        public Builder<T> urlPostfix(String urlPostfix) {
            this.urlPostfix = urlPostfix;
            return this;
        }

        public Builder<T> params(Object... params) {
            this.params = params;
            return this;
        }

        public Builder<T> headers(String... _headers) {
            headers = _headers;
            return this;
        }

        public Builder<T> callMark(String _callMark) {
            callMark = _callMark;
            return this;
        }

        public HttpGet<T> build() {
            HttpGet<T> httpGet = new HttpGet<>(getUrl);
            httpGet.urlPostfix = urlPostfix;

            if (params != null) {
                StringBuilder paramsSB = new StringBuilder();
                String[] paramsName = httpGet.getApi.getParamsName();
                for (int i = 0; i < paramsName.length; i++) {
                    if (i >= params.length) { break; }
                    if (params[i] != null) {
                        paramsSB.append(paramsSB.length() > 0 ? "&" : "?")
                                .append(paramsName[i])
                                .append("=")
                                .append(params[i]);
                    }
                }
                httpGet.params = paramsSB.toString();
            }

            if (headers != null) {
                for (int i = 0; i < headers.length; i++) {
                    httpGet.headers.put(httpGet.getApi.headersName[i], headers[i]);
                }
            }

            if (callMark != null) { httpGet.callMark = callMark; }

            httpGet.url = httpGet.getApi.getUrl() + httpGet.urlPostfix + httpGet.params;
            return httpGet;
        }
    }
}
