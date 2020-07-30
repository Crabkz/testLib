package com.krab.net;

import java.util.concurrent.Callable;

/**
 * @author xkz
 * @date 2020/1/5 22:31
 */
public class GetApi<T> {
    private final String name;
    private Callable<String> url;
    private transient Class<T> clazz;//返回值的类型
    public String[] headersName;//头部
    private String[] paramsName;//参数

    public GetApi(String name, Class<T> clazz, Callable<String> url, String[] headersName, String[] paramsName) {
        this.name = name;
        this.url = url;
        this.clazz = clazz;
        this.paramsName = paramsName;
        this.headersName = headersName;
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
