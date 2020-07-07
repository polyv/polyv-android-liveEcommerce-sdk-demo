package com.easefun.polyv.livecommon.modules.player.live.model;

import android.arch.lifecycle.MutableLiveData;

/**
 * 直播播放器数据
 */
public class PLVLivePlayerData {

    public enum PlayerState {
        Idle, Prepared, NoLive, LiveEnd
    }

    //当前播放线路索引
    private MutableLiveData<Integer> routePos = new MutableLiveData<>();

    //播放器状态
    private MutableLiveData<PlayerState> playerState = new MutableLiveData<>();

    public void postPrepared() {
        playerState.postValue(PlayerState.Prepared);
    }

    public void postNoLive() {
        playerState.postValue(PlayerState.NoLive);
    }

    public void postLiveEnd() {
        playerState.postValue(PlayerState.LiveEnd);
    }

    public void postRouteChange(int routPos) {
        routePos.postValue(routPos);
    }

    public MutableLiveData<Integer> getRoutePos() {
        return routePos;
    }

    public MutableLiveData<PlayerState> getPlayerState() {
        return playerState;
    }
}
