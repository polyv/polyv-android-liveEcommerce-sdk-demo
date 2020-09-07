package com.easefun.polyv.livecommon.modules.player.live.view;

import android.support.annotation.NonNull;
import android.view.View;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.livecommon.modules.player.live.contract.IPLVLivePlayerContract;

/**
 * mvp-直播播放器view层抽象类
 */
public abstract class PLVAbsLivePlayerView implements IPLVLivePlayerContract.ILivePlayerView {
    @Override
    public void setPresenter(@NonNull IPLVLivePlayerContract.ILivePlayerPresenter presenter) {

    }

    @Override
    public PolyvCloudClassVideoView getCloudClassVideoView() {
        return null;
    }

    @Override
    public PolyvAuxiliaryVideoview getSubVideoView() {
        return null;
    }

    @Override
    public View getBufferingIndicator() {
        return null;
    }

    @Override
    public IPolyvCloudClassAudioModeView getAudioModeView() {
        return null;
    }

    @Override
    public View getNoStreamIndicator() {
        return null;
    }

    @Override
    public IPolyvMediaController getMediaController() {
        return null;
    }

    @Override
    public void onSubVideoViewPlay(boolean isFirst) {

    }

    @Override
    public void onPlayError(PolyvPlayError error, String tips) {

    }

    @Override
    public void onNoLiveAtPresent() {

    }

    @Override
    public void onLiveEnd() {

    }

    @Override
    public void onPrepared(int mediaPlayMode) {

    }

    @Override
    public void onRouteChanged(int routePos) {

    }
}
