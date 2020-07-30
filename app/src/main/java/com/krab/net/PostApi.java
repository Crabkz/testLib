package com.krab.net;

import java.util.concurrent.Callable;

/**
 * @author xkz
 * @date 2020/1/4 01:26
 */
public class PostApi<T> {
    private final String name;
    private transient Class<T> clazz;
    private Callable<String> url;//URL
    public String[] headersName;//头部
    private String[] paramsName;//参数

    public PostApi(String name, Class<T> clazz, Callable<String> url, String[] headers, String[] paramsName) {
        this.name = name;
        this.url = url;
        this.paramsName = paramsName;
        this.headersName = headers;
        this.clazz = clazz;
    }

    public String getUrl() {
        try {
            return url.call();
        } catch (Exception e) {
            return "";
        }
    }

    public String[] getParamsName() {
        return paramsName == null ? new String[0] : paramsName;
    }

    public String getName() {
        return name;
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
