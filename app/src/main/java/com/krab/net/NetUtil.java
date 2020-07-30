package com.krab.net;

import android.os.Looper;

import androidx.annotation.IntDef;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.gson.JsonSyntaxException;
import com.krab.GsonUtils;
import com.krab.LogUtil;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by kz on 2019/12/28.
 */

public class NetUtil {
    private static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final static int MAX_RETRY_COUNT = 20;
    private static String REQUEST_FAIL = "网络请求失败";
    private static String CODE_ERROR = "服务器返回码错误-";
    private static String VALUES_ERROR = "参数错误";
    private static String URL_ERROR = "URL错误";
    public static OkHttpClient client;
    private static int CODE_OK = 200;
    public static HashMap<String, DefaultHead> defaultHead = new HashMap<>();
    private static final String TAG = NetUtil.class.getSimpleName();
    private static Thread mainThread;
    private static int index;
    private static HashMap<String, List<Call>> callMap = new HashMap<>();
    private static ArrayList<NetLifecycleObserver> lifecycleObservers = new ArrayList<>();
    private static NetErrorListener errorListener;
    private static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1024),
                    r -> new Thread(r, "kz-net-" + index++));

    static {
        ConnectionPool connectionPool = new ConnectionPool(10, 5, TimeUnit.MINUTES);
        client = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(1000, TimeUnit.MILLISECONDS)
                .build();
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

//    public static void commonHead(final Callback callBack, String url) {
//        Request request;
//        try {
//            Request.Builder builder = new Request.Builder()
//                    .url(url)
//                    .head();
//            request = builder.build();
//        } catch (Exception ignore) {
//            completed(callBack, URL_FAILURE, URL_ERROR);
//            return;
//        }
//        commonCall(callBack, request);
//    }

    public static <T> void httpGet(HttpGet<T> httpGet) {
        Request request = composeGetRequest(httpGet);
        if (request == null) {
            completed(httpGet, new MResponse<T>(URL_FAILURE, null, URL_ERROR));
            return;
        }
        LogUtil.et(TAG, httpGet.name, request.url().toString());
        call(httpGet, httpGet.getApi.getClazz(), request);
    }

    public static <T> MResponse<T> httpGetExec(HttpGet<T> httpGet) {
        Request request = composeGetRequest(httpGet);
        MResponse<T> response;
        if (request == null) {
            response = new MResponse<>(URL_FAILURE, null, URL_ERROR);
        } else {
            response = syncCall(httpGet, httpGet.getApi.getClazz(), request);
            LogUtil.et(TAG, httpGet.name, "URL", request.url().toString());
        }
        LogUtil.et(TAG, httpGet.name, "Response", GsonUtils.to(response));
        recordFailedRequest(httpGet, response);
        return response;
    }

    private static <T> Request composeGetRequest(HttpGet<T> httpGet) {
        Request request = null;
        try {
            Request.Builder builder = new Request.Builder()
                    .url(httpGet.url)
                    .get();
            for (Map.Entry<String, DefaultHead> entry : defaultHead.entrySet()) {
                httpGet.headers.put(entry.getKey(), entry.getValue().getValue());
            }
            for (Map.Entry<String, String> entry : httpGet.headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            request = builder.build();
        } catch (Exception e) {
            LogUtil.et(TAG, httpGet.name, e.getMessage());
        }
        return request;
    }

    public static <T> void httpPost(HttpPost<T> httpPost) {
        Request request = composePostRequest(httpPost);
        if (request == null) {
            completed(httpPost, new MResponse<T>(URL_FAILURE, null, URL_ERROR));
            return;
        }
        LogUtil.et(TAG, httpPost.name, "URL", request.url().toString());
        LogUtil.et(TAG, httpPost.name, "PostData", httpPost.data);
        call(httpPost, httpPost.postApi.getClazz(), request);
    }

    public static <T> MResponse<T> httpPostExec(HttpPost<T> httpPost) {
        Request request = composePostRequest(httpPost);
        MResponse<T> response;
        if (request == null) {
            response = new MResponse<>(URL_FAILURE, null, URL_ERROR);
        } else {
            response = syncCall(httpPost, httpPost.postApi.getClazz(), request);
            LogUtil.et(TAG, httpPost.name, "URL", request.url().toString());
        }
        LogUtil.et(TAG, httpPost.name, "PostData", httpPost.data);
        LogUtil.et(TAG, httpPost.name, "Response", GsonUtils.to(response));
        recordFailedRequest(httpPost, response);
        return response;
    }

    /**
     * 获取Post的Request
     *
     * @param httpPost
     * @return
     */
    private static <T> Request composePostRequest(HttpPost<T> httpPost) {
        Request request = null;
        try {
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, httpPost.data == null ? "" : httpPost.data);
            Request.Builder builder = new Request.Builder()
                    .url(httpPost.url)
                    .post(body);
            for (Map.Entry<String, DefaultHead> entry : defaultHead.entrySet()) {
                httpPost.headers.put(entry.getKey(), entry.getValue().getValue());
            }
            for (Map.Entry<String, String> entry : httpPost.headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            request = builder.build();
        } catch (Exception e) {
            LogUtil.et(TAG, httpPost.name, e.getMessage());
        }
        return request;
    }

    private static <T> void call(final HttpBase<T> httpBase, final Class<T> clazz, final Request request) {
        Call call = NetUtil.client.newCall(request);
        addCall(httpBase.callMark, call);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                removeCall(httpBase.callMark, call);
                String data = String.format("%s:%s", e.getClass().getName(), e.getCause());
                completed(httpBase, new MResponse<T>(CALL_FAILURE, null, REQUEST_FAIL, data));
            }

            @Override
            public void onResponse(Call call, Response response) {
                removeCall(httpBase.callMark, call);
                completed(httpBase, analyzeResponse(clazz, response, false));
            }
        });
    }

    /**
     * 如果在主线程调用的,则切到子线程访问网络
     * 如果本身就在子线程中调用的,则不切换线程
     *
     * @param clazz
     * @param request
     * @param <T>
     * @return
     */
    private static <T> MResponse<T> syncCall(final HttpBase<T> httpBase, Class<T> clazz, final Request request) {
        Call call = NetUtil.client.newCall(request);
        addCall(httpBase.callMark, call);
        try {
            Response response;
            if (isMainThread()) {
                Future<Response> submit = threadPoolExecutor.submit(call::execute);
                response = submit.get();
            } else {
                response = call.execute();
            }
            removeCall(httpBase.callMark, call);
            return analyzeResponse(clazz, response, true);
        } catch (InterruptedException | ExecutionException | IOException e) {
            String data = String.format("%s:%s", e.getClass().getName(), e.getCause());
            return new MResponse<>(CALL_FAILURE, null, REQUEST_FAIL, data);
        }
    }

    /**
     * 新一套的网络调用方法中的分析response的方法!!!
     *
     * @param clazz
     * @param response
     */
    private static <T> MResponse<T> analyzeResponse(Class<T> clazz, Response response, boolean switchThread) {
        if (response.code() == NetUtil.CODE_OK) {
            ResponseBody body = response.body();
            if (body != null) {
                String s = null;
                try {
                    if (switchThread) {
                        Future<String> submit = threadPoolExecutor.submit(body::string);
                        s = submit.get();
                    } else {
                        s = body.string();
                    }
                    if (clazz == null) {
                        throw new ClassIsNullException("clazz is null");
                    }
                    T responseBean = GsonUtils.gson.fromJson(s, clazz);
                    return new MResponse<>(CALL_SUCCEED, responseBean, null, s);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return new MResponse<>(READ_DATA_ERROR, null, null);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return new MResponse<>(PARSE_DATA_ERROR, null, null, s);
                }
            } else {
                return new MResponse<>(BODY_IS_NULL, null, null);
            }
        } else {
            return new MResponse<>(OTHER_FAILURE, null, CODE_ERROR + response.code());
        }
    }

    private static <T> void completed(HttpBase<T> httpBase, MResponse<T> mResponse) {
        LogUtil.et(TAG, httpBase.name, "Response", GsonUtils.to(mResponse));
        recordFailedRequest(httpBase, mResponse);
        if (httpBase.callback != null) {
            httpBase.callback.completed(mResponse);
        }
    }

    /**
     * 记录错误的请求
     */
    private static <T> void recordFailedRequest(HttpBase<T> httpBase, MResponse<T> mResponse) {
        if (errorListener != null) { errorListener.recordFailedRequest(httpBase, mResponse); }
    }

    public static boolean isMainThread() {
        Thread currentThread = Thread.currentThread();
        if (mainThread == null) {
            mainThread = Looper.getMainLooper().getThread();
        }
        return currentThread == mainThread;
    }

    public static final int CALL_SUCCEED = 0;
    public static final int CALL_FAILURE = 1;
    public static final int URL_FAILURE = 2;
    public static final int PARSE_DATA_ERROR = 3;
    public static final int BODY_IS_NULL = 4;
    public static final int READ_DATA_ERROR = 5;
    public static final int OTHER_FAILURE = 6;

    @IntDef({CALL_FAILURE, CALL_SUCCEED, URL_FAILURE, PARSE_DATA_ERROR, BODY_IS_NULL, READ_DATA_ERROR, OTHER_FAILURE})
    @interface ResponseCode {

    }

    public static abstract class Callback<T> {
        public abstract void completed(MResponse<T> response);
    }

    public static class MResponse<T> {
        public int code;
        public T rBean;
        public String rString;
        public String data;

        public MResponse(int code, T rBean, String rString) {
            this(code, rBean, rString, null);
        }

        public MResponse(int code, T rBean, String rString, String data) {
            this.code = code;
            this.rBean = rBean;
            this.rString = rString;
            this.data = data;
        }
    }

    private static void addCall(String callMark, Call call) {
        if (callMark == null) {return;}
        List<Call> calls = callMap.get(callMark);
        if (calls == null) {
            calls = new ArrayList<>();
            callMap.put(callMark, calls);
        }
        calls.add(call);
    }

    private static void removeCall(String callMark, Call call) {
        if (callMark == null || call == null) {return;}
        List<Call> calls = callMap.get(callMark);
        if (calls == null) { return; }
        calls.remove(call);
    }

    public static void removeCall(String callMark) {
        List<Call> calls = NetUtil.callMap.remove(callMark);
        if (calls == null) {return;}
        for (Call call : calls) {
            if (call == null || call.isCanceled()) {continue;}
            LogUtil.et("NetUtil", "removeCall", callMark);
            call.cancel();
        }
    }

    public static void setLifecycleObserver(Lifecycle lifecycle, String... callMarks) {
        NetLifecycleObserver observer = new NetLifecycleObserver(callMarks);
        NetUtil.lifecycleObservers.add(observer);
        lifecycle.addObserver(observer);
    }

    private static class NetLifecycleObserver implements LifecycleObserver {
        private String[] callMarks;

        public NetLifecycleObserver(String... callMarks) {
            this.callMarks = callMarks;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            if (callMarks == null || callMarks.length <= 0) {return;}
            NetUtil.lifecycleObservers.remove(this);
            for (String callMark : callMarks) {
                removeCall(callMark);
            }
        }
    }

    public static void setNetErrorListener(NetErrorListener listener) {
        errorListener = listener;
    }

    public interface NetErrorListener {
        <T> void recordFailedRequest(HttpBase<T> httpBase, MResponse<T> mResponse);
    }
}
