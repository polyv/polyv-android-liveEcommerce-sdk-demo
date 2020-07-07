package com.easefun.polyv.livecommon.contract;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.dataservice.PLVAbsBusinessDataImpl;

import java.lang.ref.WeakReference;

/**
 * mvp-抽象viewPresenter
 */
public abstract class PLVAbsViewPresenter<V extends IPLVBaseView> extends PLVAbsBusinessDataImpl implements IPLVAbsViewPresenter<V> {
    private WeakReference<V> vWeakReference;

    @Override
    public void registerView(@NonNull V v) {
        this.vWeakReference = new WeakReference<>(v);
        setPresenterToView();
    }

    @Override
    public void unregisterView() {
        if (vWeakReference != null) {
            vWeakReference.clear();
            vWeakReference = null;
        }
    }

    protected abstract void setPresenterToView();

    protected V getView() {
        return vWeakReference != null ? vWeakReference.get() : null;
    }

    protected boolean isAlive() {
        return getView() != null;
    }

    @Override
    public void destroy() {
        unregisterView();
    }
}
