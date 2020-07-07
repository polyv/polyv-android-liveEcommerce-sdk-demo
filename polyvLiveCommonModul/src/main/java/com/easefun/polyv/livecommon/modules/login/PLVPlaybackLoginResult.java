package com.easefun.polyv.livecommon.modules.login;

/**
 * date: 2020-04-19
 * author: hwj
 * description: 回放登录结果
 */
public class PLVPlaybackLoginResult {
    private boolean isNormal;

    public PLVPlaybackLoginResult(boolean isNormal) {
        this.isNormal = isNormal;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public void setNormal(boolean normal) {
        isNormal = normal;
    }
}
