package com.krab.net;


/**
 * @author xkz
 * @date 2019/10/31 21:30
 */
public class HttpPost<T> extends HttpBase<T> {
    public final PostApi<T> postApi;
    public String data;

    private HttpPost(PostApi<T> postApi) {
        this.postApi = postApi;
        this.name = postApi.getName();
    }

    public static <T> Builder<T> obtainBuilder(PostApi<T> postApi) {
        return new Builder<>(postApi);
    }

    public void call(NetUtil.Callback<T> callback) {
        this.callback = callback;
        NetUtil.httpPost(this);
    }

    public NetUtil.MResponse<T> execute() {
        return NetUtil.httpPostExec(this);
    }

    public static class Builder<T> {
        private PostApi<T> postApi;
        private String urlPostfix = "";
        private Object[] params;
        private String[] headers;
        private String callMark;

        private String data;

        private Builder(PostApi<T> postApi) {
            this.postApi = postApi;
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

        public Builder<T> data(String data) {
            this.data = data;
            return this;
        }

        public HttpPost<T> build() {
            HttpPost<T> httpPost = new HttpPost<>(postApi);
            httpPost.urlPostfix = urlPostfix;
            httpPost.data = data;

            if (params != null) {
                StringBuilder paramsSB = new StringBuilder();
                String[] paramsName = httpPost.postApi.getParamsName();
                for (int i = 0; i < paramsName.length; i++) {
                    if (i >= params.length) { break; }
                    if (params[i] != null) {
                        paramsSB.append(paramsSB.length() > 0 ? "&" : "?")
                                .append(paramsName[i])
                                .append("=")
                                .append(params[i]);
                    }
                }
                httpPost.params = paramsSB.toString();
            }

            if (headers != null) {
                for (int i = 0; i < headers.length; i++) {
                    httpPost.headers.put(httpPost.postApi.headersName[i], headers[i]);
                }
            }

            if (callMark != null) { httpPost.callMark = callMark; }

            httpPost.url = httpPost.postApi.getUrl() + httpPost.urlPostfix + httpPost.params;
            return httpPost;
        }

    }
}
