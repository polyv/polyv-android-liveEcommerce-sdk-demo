package com.easefun.polyv.livecommon.modules.player.live.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.auxiliary.IPolyvAuxiliaryVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvCloudClassVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveChannelVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveLinesVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassListenerEvent;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.livecommon.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.data.IPLVLiveRoomData;
import com.easefun.polyv.livecommon.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.modules.player.live.model.PLVLivePlayerData;
import com.easefun.polyv.livecommon.utils.PLVReflectionUtils;
import com.easefun.polyv.livecommon.utils.imageloader.PLVImageLoader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * mvp-直播播放器presenter层实现
 */
public class PLVLivePlayerPresenter implements IPLVLivePlayerContract.ILivePlayerPresenter {
    private IPLVLiveRoomData liveRoomData;
    private PLVLivePlayerData livePlayerData;
    private WeakReference<IPLVLivePlayerContract.ILivePlayerView> vWeakReference;

    private PolyvCloudClassVideoView videoView;
    private PolyvAuxiliaryVideoview subVideoView;

    public PLVLivePlayerPresenter(@NonNull IPLVLiveRoomData liveRoomData) {
        this.liveRoomData = liveRoomData;
        livePlayerData = new PLVLivePlayerData();
    }

    // <editor-fold defaultstate="collapsed" desc="presenter方法">
    @Override
    public void registerView(@NonNull IPLVLivePlayerContract.ILivePlayerView v) {
        this.vWeakReference = new WeakReference<>(v);
        v.setPresenter(this);
    }

    @Override
    public void unregisterView() {
        if (vWeakReference != null) {
            vWeakReference.clear();
            vWeakReference = null;
        }
    }

    @Override
    public void init() {
        if (getView() == null) {
            return;
        }
        //init data
        videoView = getView().getCloudClassVideoView();
        subVideoView = getView().getSubVideoView();
        initSubVideoViewListener();
        initVideoViewListener();
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

    @Nullable
    @Override
    public List<PolyvDefinitionVO> getBitrateVO() {
        List<PolyvDefinitionVO> definitionVOS = null;
        if (videoView != null) {
            PolyvLiveChannelVO channelVO = videoView.getModleVO();
            if (channelVO != null) {
                List<PolyvLiveLinesVO> liveLines = channelVO.getLines();
                if (liveLines != null) {
                    PolyvLiveLinesVO linesVO = liveLines.get(getRoutePos());//当前线路信息
                    if (linesVO != null && linesVO.getMultirateModel() != null) {
                        if (channelVO.isMutilrateEnable()) {
                            definitionVOS = linesVO.getMultirateModel().getDefinitions();
                        } else {
                            definitionVOS = new ArrayList<>();
                            definitionVOS.add(new PolyvDefinitionVO(linesVO.getMultirateModel().getDefaultDefinition()
                                    , linesVO.getMultirateModel().getDefaultDefinitionUrl()));
                        }
                    }
                }
            }
        }
        return definitionVOS;
    }

    @Override
    public int getRoutePos() {
        return videoView == null ? 0 : videoView.getLinesPos();
    }

    @Override
    public int getBitratePos() {
        return videoView == null ? 0 : videoView.getBitratePos();
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

    @Override
    public void changeBitRate(int bitRate) {
        if (videoView != null) {
            videoView.changeBitRate(bitRate);
        }
    }

    @NonNull
    @Override
    public PLVLivePlayerData getData() {
        return livePlayerData;
    }

    @Override
    public void destroy() {
        unregisterView();
        if (videoView != null) {
            videoView.destroy();
        }
        PLVReflectionUtils.cleanFields(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取view、config">
    private IPLVLivePlayerContract.ILivePlayerView getView() {
        return vWeakReference != null ? vWeakReference.get() : null;
    }

    private PLVLiveChannelConfig getConfig() {
        return liveRoomData.getConfig();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化subVideo, videoView的监听器配置">
    private void initSubVideoViewListener() {
        if (subVideoView != null) {
            subVideoView.setOnVideoPlayListener(new IPolyvVideoViewListenerEvent.OnVideoPlayListener() {
                @Override
                public void onPlay(boolean isFirst) {
                    if (getView() != null) {
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
                    if (getView() != null) {
                        getView().onPlayError(error, tips);
                    }
                }
            });
            videoView.setOnNoLiveAtPresentListener(new IPolyvCloudClassListenerEvent.OnNoLiveAtPresentListener() {
                @Override
                public void onNoLiveAtPresent() {
                    videoView.removeRenderView();
                    livePlayerData.postNoLive();
                    if (getView() != null) {
                        getView().onNoLiveAtPresent();
                    }
                }

                @Override
                public void onLiveEnd() {
                    livePlayerData.postLiveEnd();
                    if (getView() != null) {
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
                    if (getView() != null) {
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
                    if (getView() != null) {
                        getView().onRouteChanged(routePos);
                    }
                }
            });
        }
    }

    private void setDefaultViewStatus() {
        videoView.removeRenderView();
        if (getView() != null && getView().getBufferingIndicator() != null) {
            getView().getBufferingIndicator().setVisibility(View.GONE);
        }
        if (getView() != null && getView().getNoStreamIndicator() != null) {
            getView().getNoStreamIndicator().setVisibility(View.VISIBLE);
        }
    }
    // </editor-fold>
}
