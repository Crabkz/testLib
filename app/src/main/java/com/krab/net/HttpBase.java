package com.krab.net;

import java.util.HashMap;

/**
 * @author xkz
 * @date 2020/1/6 16:27
 */
public class HttpBase<T> {
    public String name;
    public String urlPostfix = "";
    public String params = "";
    public String url;
    public String callMark;
    public HashMap<String, String> headers = new HashMap<>();
    public NetUtil.Callback<T> callback;
}
