package com.easefun.polyv.livecommon.modules.player.playback.model;

import android.arch.lifecycle.MutableLiveData;

/**
 * 回放播放器数据
 */
public class PLVPlaybackPlayerData {

    public enum PlayerState {
        Idle, Prepared
    }

    //播放器状态
    private MutableLiveData<PlayerState> playerState = new MutableLiveData<>();

    //播放信息，每隔一秒回调一次
    private MutableLiveData<PLVPlayInfoVO> playInfoVO = new MutableLiveData<>();

    public void postPlayInfoVO(PLVPlayInfoVO playInfo) {
        playInfoVO.postValue(playInfo);
    }

    public MutableLiveData<PLVPlayInfoVO> getPlayInfoVO() {
        return playInfoVO;
    }

    public void postPrepared() {
        playerState.postValue(PlayerState.Prepared);
    }

    public MutableLiveData<PlayerState> getPlayerState() {
        return playerState;
    }
}
