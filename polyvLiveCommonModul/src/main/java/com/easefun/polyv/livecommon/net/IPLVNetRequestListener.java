package com.easefun.polyv.livecommon.net;

/**
 * 接口请求监听器
 */
public interface IPLVNetRequestListener<T> {
    /**
     * 请求成功
     */
    void onSuccess(T t);

    /**
     * 请求失败
     *
     * @param msg       错误消息
     * @param throwable throwable
     */
    void onFailed(String msg, Throwable throwable);
}
