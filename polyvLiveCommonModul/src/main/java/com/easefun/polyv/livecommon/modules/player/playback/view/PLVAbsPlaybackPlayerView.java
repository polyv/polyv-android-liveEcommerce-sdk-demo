package com.easefun.polyv.livecommon.modules.player.playback.view;

import android.view.View;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.livecommon.modules.player.playback.contract.IPLVPlaybackPlayerContract;

/**
 * mvp-回放播放器view层抽象类
 */
public abstract class PLVAbsPlaybackPlayerView implements IPLVPlaybackPlayerContract.IPLVPlaybackPlayerView {
    @Override
    public PolyvPlaybackVideoView getPlaybackVideoView() {
        return null;
    }

    @Override
    public View getBufferingIndicator() {
        return null;
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onPlayError(PolyvPlayError error, String tips) {

    }

    @Override
    public void onBufferStart() {

    }

    @Override
    public void onBufferEnd() {

    }

    @Override
    public void setPresenter(IPLVPlaybackPlayerContract.IPLVPlaybackPlayerPresenter presenter) {

    }
}
