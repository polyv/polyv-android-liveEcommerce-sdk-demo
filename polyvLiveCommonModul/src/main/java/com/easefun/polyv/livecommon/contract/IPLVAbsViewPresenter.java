package com.easefun.polyv.livecommon.contract;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.dataservice.IPLVBusinessDataProtocol;

/**
 * mvp-presenter关联view的接口
 */
public interface IPLVAbsViewPresenter<V extends IPLVBaseView> extends IPLVBusinessDataProtocol, IPLVBasePresenter {
    /**
     * 注册view
     */
    void registerView(@NonNull V v);

    /**
     * 解除注册的view
     */
    void unregisterView();
}
