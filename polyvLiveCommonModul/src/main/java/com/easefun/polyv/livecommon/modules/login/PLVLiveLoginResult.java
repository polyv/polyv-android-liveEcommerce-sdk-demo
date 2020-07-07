package com.easefun.polyv.livecommon.modules.login;

/**
 * date: 2020-04-19
 * author: hwj
 * description: 直播登录结果
 */
public class PLVLiveLoginResult {
    private boolean isNormal;

    public PLVLiveLoginResult(boolean isNormal) {
        this.isNormal = isNormal;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public void setNormal(boolean normal) {
        isNormal = normal;
    }

}
