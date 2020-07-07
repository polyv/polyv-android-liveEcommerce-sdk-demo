package com.easefun.polyv.livecommon.modules.player.live.presenter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.auxiliary.IPolyvAuxiliaryVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvCloudClassVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassListenerEvent;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.livecommon.contract.PLVAbsViewPresenter;
import com.easefun.polyv.livecommon.dataservice.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.modules.player.live.model.PLVLivePlayerData;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;
import com.easefun.polyv.livecommon.utils.imageloader.PLVImageLoader;

/**
 * mvp-直播播放器presenter层实现
 */
public class PLVLivePlayerPresenter extends PLVAbsViewPresenter<IPLVLivePlayerContract.IPLVLivePlayerView> implements IPLVLivePlayerContract.IPLVLivePlayerPresenter {
    private PolyvCloudClassVideoView videoView;
    private PolyvAuxiliaryVideoview subVideoView;

    private PLVLivePlayerData livePlayerData;

    public PLVLivePlayerPresenter(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
        livePlayerData = new PLVLivePlayerData();
    }

    // <editor-fold defaultstate="collapsed" desc="presenter方法">
    @Override
    protected void setPresenterToView() {
        if (isAlive()) {
            getView().setPresenter(this);
        }
    }

    @Override
    public void init() {
        if (!isAlive()) {
            return;
        }
        //init data
        videoView = getView().getCloudClassVideoView();
        subVideoView = getView().getSubVideoView();
        initSubVideoViewListener();
        initVideoViewListener();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (videoView != null) {
            videoView.destroy();
        }
        PLVReflectionUtils.cleanFields(this);
    }

    @Override
    public void startPlay() {
        PolyvCloudClassVideoParams cloudClassVideoParams = new PolyvCloudClassVideoParams(
                getConfig().getChannelId(),
                getConfig().getAccount().getUserId(),
                getConfig().getUser().getViewerId()
        );
        cloudClassVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(PolyvBaseVideoParams.PARAMS2, getConfig().getUser().getViewerName());
        if (videoView != null) {
            videoView.playByMode(cloudClassVideoParams, PolyvPlayOption.PLAYMODE_LIVE);
        }
    }

    @Override
    public void pause() {
        if (videoView != null) {
            videoView.pause();
        }
    }

    @Override
    public void resume() {
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    public void stop() {
        if (videoView != null) {
            videoView.stopPlay();
        }
    }

    @Override
    public int getRouteCount() {
        return (videoView == null || videoView.getModleVO() == null || videoView.getModleVO().getLines() == null)
                ? 1 : videoView.getModleVO().getLines().size();
    }

    @Override
    public int getMediaPlayMode() {
        if (videoView != null) {
            return videoView.getMediaPlayMode();
        }
        return PolyvMediaPlayMode.MODE_VIDEO;
    }

    @Override
    public void changeMediaPlayMode(int mediaPlayMode) {
        if (videoView != null) {
            videoView.changeMediaPlayMode(mediaPlayMode);
        }
    }

    @Override
    public void changeRoute(int routePos) {
        if (videoView != null) {
            videoView.changeLines(routePos);
        }
    }

    @NonNull
    @Override
    public PLVLivePlayerData getData() {
        return livePlayerData;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化subVideo, videoView的监听器配置">
    private void initSubVideoViewListener() {
        if (subVideoView != null) {
            subVideoView.setOnVideoPlayListener(new IPolyvVideoViewListenerEvent.OnVideoPlayListener() {
                @Override
                public void onPlay(boolean isFirst) {
                    if (isAlive()) {
                        getView().onSubVideoViewPlay(isFirst);
                    }
                }
            });
            subVideoView.setOnSubVideoViewLoadImage(new IPolyvAuxiliaryVideoViewListenerEvent.IPolyvOnSubVideoViewLoadImage() {
                @Override
                public void onLoad(String imageUrl, ImageView imageView) {
                    PLVImageLoader.getInstance().loadImage(subVideoView.getContext(), imageUrl, imageView);
                }
            });
        }
    }

    private void initVideoViewListener() {
        if (videoView != null) {
            videoView.setOnErrorListener(new IPolyvVideoViewListenerEvent.OnErrorListener() {
                @Override
                public void onError(int what, int extra) {/**/}

                @Override
                public void onError(PolyvPlayError error) {
                    setDefaultViewStatus();

                    String tips = error.playStage == PolyvPlayError.PLAY_STAGE_HEADAD ? "片头广告"
                            : error.playStage == PolyvPlayError.PLAY_STAGE_TAILAD ? "片尾广告"
                            : error.playStage == PolyvPlayError.PLAY_STAGE_TEASER ? "暖场视频"
                            : error.isMainStage() ? "主视频" : "";
                    tips += "播放异常\n" + error.errorDescribe + " (errorCode:" + error.errorCode +
                            "-" + error.playStage + ")\n" + error.playPath;
                    if (isAlive()) {
                        getView().onPlayError(error, tips);
                    }
                }
            });
            videoView.setOnNoLiveAtPresentListener(new IPolyvCloudClassListenerEvent.OnNoLiveAtPresentListener() {
                @Override
                public void onNoLiveAtPresent() {
                    videoView.removeRenderView();
                    livePlayerData.postNoLive();
                    if (isAlive()) {
                        getView().onNoLiveAtPresent();
                    }
                }

                @Override
                public void onLiveEnd() {
                    livePlayerData.postLiveEnd();
                    if (isAlive()) {
                        getView().onLiveEnd();
                    }
                }
            });
            videoView.setOnPreparedListener(new IPolyvVideoViewListenerEvent.OnPreparedListener() {
                @Override
                public void onPrepared() {
                    livePlayerData.postPrepared();
                    liveRoomData.setSessionId(videoView.getModleVO() != null ? videoView.getModleVO().getChannelSessionId() : null);
                    if (videoView.getMediaPlayMode() == PolyvMediaPlayMode.MODE_AUDIO) {
                        videoView.removeRenderView();//need clear&unregister
                    }
                    if (isAlive()) {
                        getView().onPrepared(videoView.getMediaPlayMode());
                    }
                }

                @Override
                public void onPreparing() {/**/}
            });
            videoView.setOnLinesChangedListener(new IPolyvCloudClassListenerEvent.OnLinesChangedListener() {
                @Override
                public void OnLinesChanged(int routePos) {
                    livePlayerData.postRouteChange(routePos);
                    if (isAlive()) {
                        getView().onRouteChanged(routePos);
                    }
                }
            });
        }
    }

    private void setDefaultViewStatus() {
        videoView.removeRenderView();
        if (isAlive() && getView().getBufferingIndicator() != null) {
            getView().getBufferingIndicator().setVisibility(View.GONE);
        }
        if (isAlive() && getView().getNoStreamIndicator() != null) {
            getView().getNoStreamIndicator().setVisibility(View.VISIBLE);
        }
    }
    // </editor-fold>
}
